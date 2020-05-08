 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: SMSFlatFileObject.java,v 1.2 2005-11-04 18:53:47 veiming Exp $
  *
  * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.sm.flatfile;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.text.ParseException;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.naming.directory.Attribute;
 import javax.naming.directory.ModificationItem;
 
 import com.iplanet.am.util.Debug;
 import com.iplanet.am.util.SystemProperties;
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.sun.identity.common.CaseInsensitiveHashMap;
 import com.sun.identity.common.CaseInsensitiveHashSet;
 import com.sun.identity.common.CaseInsensitiveProperties;
 import com.sun.identity.common.CaseInsensitiveTreeSet;
 import com.sun.identity.common.Constants;
 import com.sun.identity.common.ReaderWriterLock;
 import com.sun.identity.sm.SMSEntry;
 import com.sun.identity.sm.SMSException;
 import com.sun.identity.sm.SMSObject;
 import com.sun.identity.sm.SMSObjectListener;
 import com.sun.identity.sm.SchemaException;
 import com.sun.identity.sm.ServiceAlreadyExistsException;
 import com.sun.identity.sm.ServiceNotFoundException;
 
 /**
  * This class represents a configuration object stored in a file. Each file
  * lives in a file system under a directory of configuration objects organized
  * in a hierarchy. Each level in the hierarchy is represented by a directory.
  * The naming convention of a configuration object is hierarchy levels separated
  * by a comma, for example "ou=serviceName,ou=services,dc=sun,dc=com". This
  * object would live in the directory
  * <code>&lt;config-dir>/dc=com/dc=sun/ou=services/ou=serviceName</code>. The
  * directory has a file with the object's attributes in
  * <code>java.util.Properties</code> format. The file name is
  * <code>Attributes.properties</code>. Attributes with multi-values are
  * seperated by a comma. A comma within a value is encoded as %2C, and a %
  * within a value is encoded with <code>%25</code>.
  */
 public class SMSFlatFileObject extends SMSObject {
 
     // File specific parameters
     private String mRootDir = null;
 
     private File mRootDirHandle = null;
 
     private String mRootDN = null;
 
     // Other parameters
     private boolean mInitialized = false;
 
     private Debug mDebug = null;
 
     private Properties mNameMap = null;
 
     private File mNameMapHandle = null;
 
     private ReaderWriterLock mRWLock = new ReaderWriterLock();
 
     static final String SMS_FLATFILE_ROOTDIR_PROPERTY = 
         "com.sun.identity.sm.flatfile.root_dir";
 
     static final String DEFAULT_ATTRIBUTE_FILENAME = "Attributes.properties";
 
     static final String DEFAULT_NAMEMAP_FILENAME = "NameMap.properties";
 
     static final String DEFAULT_ROOT_DIR = "/var/opt/SUNWam/sms";
 
     static final String DEFAULT_ORG_DN = "dc=sun,dc=com";
 
     // 
     // utility routines
     // 
 
     /**
      * Simple class that looks for subentries with name matching the filter.
      * Only wildcard '*' character is supported in the filter.
      */
     private class FilenameFilter implements FileFilter {
         private String mFilter = null;
 
         private String mBegin = null;
 
         private int mBeginLen = 0;
 
         private String mEnd = null;
 
         private int mEndLen = 0;
 
         private int mWildcard = -1;
 
         public FilenameFilter(String filter) {
             mFilter = filter;
             mWildcard = mFilter.indexOf('*');
             if (mWildcard >= 0) {
                 if (mWildcard > 0) {
                     mBegin = FileNameEncoder.encode(mFilter.substring(0,
                             mWildcard));
                     mBeginLen = mBegin.length();
                 }
                 if (mWildcard < filter.length() - 1) {
                     mEnd = FileNameEncoder.encode(mFilter
                             .substring(mWildcard + 1));
                     mEndLen = mEnd.length();
                 }
             }
         }
 
         public boolean accept(File file) {
             String filename = file.getName();
             boolean doAccept = false;
             // case ignore comparison of file name.
             if (mWildcard >= 0) {
                 doAccept = ((mBegin == null || filename.regionMatches(true, 0,
                         mBegin, 0, mBeginLen)) && (mEnd == null || filename
                         .regionMatches(true, filename.length() - mEndLen, mEnd,
                                 0, mEndLen)));
             } else {
                 doAccept = filename.equalsIgnoreCase(mFilter);
             }
             if (mDebug.messageEnabled()) {
                 mDebug.message("SMSFlatFileObject: subentry file " + filename
                         + " matched " + mFilter + ": " + doAccept);
             }
             return doAccept;
         }
     }
 
     /**
      * Recursively deletes the given directory and all files and directories
      * underneath.
      */
     private void deleteDir(File dirhandle) throws SMSException {
         File[] files = dirhandle.listFiles();
         // remove all files in each sub-dir
         if (files != null && files.length > 0) {
             for (int i = 0; i < files.length; i++) {
                 if (files[i].isDirectory()) {
                     deleteDir(files[i]);
                 } else if (!files[i].delete()) {
                     String errmsg = "SMSFlatFileObject.delete: File "
                             + files[i].getPath() + " could not be removed!";
                     mDebug.error(errmsg);
                     throw new SMSException(errmsg);
                 }
             }
         }
         dirhandle.delete();
     }
 
     //
     // private methods
     // 
 
     /**
      * Gets the flat file directory and default org from AMConfig.properties,
      * and creates the root directory if it does not exist. This function must
      * be called in a single thread.
      * 
      * @throws SMSException
      *             if any error occurs.
      */
     private void init() throws SMSException {
         String errmsg = null;
 
         // get the flat file root dir
         mRootDir = SystemProperties.get(SMS_FLATFILE_ROOTDIR_PROPERTY,
                 DEFAULT_ROOT_DIR);
 
         // get the default org dn
         mRootDN = SystemProperties
                 .get(Constants.AM_DEFAULT_ORG, DEFAULT_ORG_DN);
 
         // look for the object name mapper if any.
         // create the flat file directory up to the org if it doesn't exist.
         mRootDirHandle = new File(mRootDir);
         if (mRootDirHandle.isDirectory()) {
             if (!mRootDirHandle.canRead() || !mRootDirHandle.canWrite()) {
                 errmsg = "SMSFlatFileObject.initialize: "
                         + "cannot read or write to the root directory."
                         + mRootDir;
                 mDebug.error(errmsg);
                 throw new SMSException(errmsg);
             } else {
                 if (mDebug.messageEnabled()) {
                     mDebug.message("SMSFlatFileObject: Root Directory: "
                             + mRootDir);
                 }
             }
         } else {
             if (!mRootDirHandle.mkdir()) {
                 errmsg = "SMSFlatFileObject.initialize: "
                         + "Cannot create the root directory." + mRootDir;
                 mDebug.error(errmsg);
                 throw new SMSException(errmsg);
             }
             if (mDebug.messageEnabled()) {
                 mDebug.message("SMSFlatFileObject: Created root directory: "
                         + mRootDir);
             }
         }
 
         // load the name mapper, create it if it doesn't exist.
         StringBuffer nameMapFilename = new StringBuffer(mRootDir);
         nameMapFilename.append(File.separatorChar);
         nameMapFilename.append(DEFAULT_NAMEMAP_FILENAME);
         mNameMapHandle = new File(nameMapFilename.toString());
         if (mNameMapHandle.isFile()) {
             if (!mNameMapHandle.canRead()) {
                 errmsg = "SMSFlatFileObject.initialize: cannot read file "
                         + mNameMapHandle.getPath();
                 mDebug.error(errmsg);
                 throw new SMSException(errmsg);
             }
             mNameMap = loadProperties(mNameMapHandle, null);
         } else {
             try {
                 mNameMapHandle.createNewFile();
             } catch (IOException e) {
                 errmsg = "SMSFlatFileObject.initialize: "
                         + "cannot create file " + nameMapFilename
                         + ". Exception " + e.getMessage();
                 mDebug.error(errmsg);
                 throw new SMSException(errmsg);
             } catch (SecurityException e) {
                 errmsg = "SMSFlatFileObject.initialize: "
                         + "cannot create file " + nameMapFilename
                         + ". Exception " + e.getMessage();
                 mDebug.error(errmsg);
                 throw new SMSException(errmsg);
             }
             mNameMap = new CaseInsensitiveProperties();
             // create root dn if this is a new directory.
             try {
                 create(null, mRootDN, new HashMap());
                 if (mDebug.messageEnabled()) {
                     mDebug.message("SMSFlatFileObject.initialize: "
                             + "created SMS object for " + mRootDN);
                 }
             } catch (SSOException e) {
                 // not possible
             } catch (ServiceAlreadyExistsException e) {
                 if (mDebug.messageEnabled()) {
                     mDebug.message("SMSFlatFileObject.initialize: " + mRootDN
                             + " already exists");
                 }
             }
             // also create ou=services this is a new directory.
             try {
                 create(null, "ou=services," + mRootDN, new HashMap());
                 if (mDebug.messageEnabled()) {
                     mDebug.message("SMSFlatFileObject.initialize: "
                             + "created SMS object for ou=services," + mRootDN);
                 }
             } catch (SSOException e) {
                 // not possible
             } catch (ServiceAlreadyExistsException e) {
                 if (mDebug.messageEnabled()) {
                     mDebug.message("SMSFlatFileObject.initialize: "
                             + "ou=services," + mRootDN + " already exists");
                 }
             }
         }
     }
 
     //
     // protected methods
     //
 
     /**
      * Returns a path to object's attributes file.
      */
     protected String getAttrFile(String objName) {
         String attrFile = null;
         StringBuffer sb = new StringBuffer(mRootDir.length() + objName.length()
                 + 20);
         sb.append(mRootDir);
         sb.append(File.separatorChar);
         // objName is assumed to be a dn so construct the filepath
         // backwards from the top of directory tree.
         char[] objchars = objName.toCharArray();
         int i, j;
         for (i = j = objchars.length - 1; i >= 0; i--) {
             if (objchars[i] == ',') {
                 if (i == j) {
                     j--;
                 } else {
                     String rdn = new String(objchars, i + 1, j - i).trim();
                     // encode file name in case there are characters
                     // unsupported in the FS such as '/' or '\' and '*'
                     // on windows.
                     String encodedRdn = FileNameEncoder.encode(rdn);
                     sb.append(encodedRdn);
                     sb.append(File.separatorChar);
                     j = i - 1;
                 }
             }
         }
         if (i != j) {
             String lastRdn = new String(objchars, 0, j - i);
             String encodedLastRdn = FileNameEncoder.encode(lastRdn);
             sb.append(encodedLastRdn);
             sb.append(File.separatorChar);
         }
         sb.append(DEFAULT_ATTRIBUTE_FILENAME);
         attrFile = sb.toString();
         return attrFile;
     }
 
     /**
      * Loads properties from the attribute file handle.
      * 
      * @return Properties object of the configuration object.
      * @throws ServiceNotFoundException
      *             if the attributes file is not found.
      * @throws SMSException
      *             if an IO error occurred while reading the attributes
      *             properties file.
      * @throws SchemaException
      *             if a format error occurred while reading the attributes
      *             properties file.
      */
     Properties loadProperties(File filehandle, String objName)
             throws SMSException {
 
         // read file contents into properties and
         // form the attributes map to be returned from the properties.
         FileInputStream fileistr = null;
         Properties props = new CaseInsensitiveProperties();
         String errmsg = null;
         try {
             fileistr = new FileInputStream(filehandle);
             props.load(fileistr);
             return props;
         } catch (FileNotFoundException e) {
             // from the FileInputStream creation.
             errmsg = "SMSFlatFileObject.loadProperties: "
                     + (objName == null ? "" : objName + ": ")
                     + "File not found: " + filehandle.getPath();
             mDebug.error(errmsg);
             throw new ServiceNotFoundException(errmsg);
         } catch (IOException e) {
             // IO Error occurred while reading file.
             errmsg = "SMSFlatFileObject.loadProperties: "
                     + (objName == null ? "" : objName + ": ")
                     + "IO Exception encountered: " + e.getMessage();
             mDebug.error(errmsg);
             throw new SMSException(errmsg);
         } catch (IllegalArgumentException e) {
             // Incorrect syntax found in file.
             errmsg = "SMSFlatFileObject.loadProperties: "
                     + (objName == null ? "" : objName + ": ")
                     + "Incorrect syntax found in file " + filehandle.getPath()
                     + ". Exception: " + e.getMessage();
             mDebug.error(errmsg);
             throw new SchemaException(errmsg);
         } finally {
             if (fileistr != null) {
                 try {
                     fileistr.close();
                 } catch (IOException e) {
                     if (mDebug.messageEnabled()) {
                         mDebug.message("SMSFileFileObject: "
                                 + "IO Exception encountered while closing "
                                 + "properties file " + filehandle.getPath());
                     }
                 }
             }
         }
     }
 
     /**
      * Saves properties to the attributes file handle, with given objName in the
      * file header.
      */
     void saveProperties(Properties props, File filehandle, String header)
             throws SMSException {
 
         // create header for properties file.
         /*
          * StringBuffer sb = new StringBuffer("# "); if (objName != null)
          * sb.append(objName); } String header = sb.toString();
          */
 
         // create output stream for file.
         FileOutputStream fileostr = null;
         String errmsg = null;
         try {
             fileostr = new FileOutputStream(filehandle);
         } catch (FileNotFoundException e) {
             errmsg = "SMSFlatFileObject.saveProperties: "
                     + (header == null ? "" : header + ": ")
                     + "FileNotFoundException encountered while creating an "
                     + "output stream to file " + filehandle.getPath()
                     + ". Exception: " + e.getMessage();
             mDebug.error(errmsg);
             throw new ServiceNotFoundException(errmsg);
         }
         // store properties into file.
         try {
             props.store(fileostr, header);
         } catch (IOException e) {
             errmsg = "SMSFlatFileObject.saveProperties: "
                     + (header == null ? "" : header + ": ")
                     + "IO Exception encountered writing attributes to file "
                     + filehandle.getPath() + ". Exception: " + e.getMessage();
             mDebug.error(errmsg);
             throw new SMSException(errmsg);
         } finally {
             try {
                 fileostr.close();
             } catch (IOException e) {
                 if (mDebug.messageEnabled()) {
                     mDebug.message("SMSFileFileObject: "
                             + "IO Exception encountered while closing "
                             + "properties file " + filehandle.getPath());
                 }
             }
         }
     }
 
     /**
      * Converts a Set of values for an attribute into a string, encoding special
      * characters in the values as necessary.
      */
     String toValString(Set vals) {
         Iterator valsIter = vals.iterator();
         StringBuffer sb = new StringBuffer();
         boolean first = true;
         while (valsIter.hasNext()) {
             // encode any comma's and percent's in case there's
             // more than one value.
             String val = (String) valsIter.next();
             val = encodeVal(val);
             if (first) {
                 first = false;
             } else {
                 sb.append(',');
             }
             sb.append(val);
         }
         return sb.toString();
     }
 
     /**
      * Converts an enumeration of values for an attribute into a string,
      * encoding special characters in the values as necessary. This is used by
      * the modify() method where the values needs to be in a particular order.
      */
     String toValString(Enumeration en) {
         StringBuffer sb = new StringBuffer();
         boolean first = true;
         while (en.hasMoreElements()) {
             String val = (String) en.nextElement();
             val = encodeVal(val);
             // now append the encoded value to the string of values.
             if (first) {
                 first = false;
             } else {
                 sb.append(',');
             }
             sb.append(val);
         }
         return sb.toString();
     }
 
     /**
      * Encodes special characters in a value. percent to %25 and comma to %2C.
      */
     String encodeVal(String v) {
         char[] chars = v.toCharArray();
         StringBuffer sb = new StringBuffer(chars.length + 20);
         int i = 0, lastIdx = 0;
         for (i = 0; i < chars.length; i++) {
             if (chars[i] == '%') {
                 if (lastIdx != i) {
                     sb.append(chars, lastIdx, i - lastIdx);
                 }
                 sb.append("%25");
                 lastIdx = i + 1;
             } else if (chars[i] == ',') {
                 if (lastIdx != i) {
                     sb.append(chars, lastIdx, i - lastIdx);
                 }
                 sb.append("%2C");
                 lastIdx = i + 1;
             }
         }
         if (lastIdx != i) {
             sb.append(chars, lastIdx, i - lastIdx);
         }
         return sb.toString();
     }
 
     /**
      * Converts a string of values from the attributes properties file to a Set,
      * decoding special characters in each value.
      */
     protected Set toValSet(String attrName, String vals) {
         Set valset = null;
         if (SMSEntry.isAttributeCaseSensitive(attrName)) {
             valset = new HashSet();
         } else {
             valset = new CaseInsensitiveHashSet();
         }
         char[] valchars = vals.toCharArray();
         int i, j;
         for (i = 0, j = 0; j < valchars.length; j++) {
             char c = valchars[j];
             if (c == ',') {
                 if (i == j) {
                     i = j + 1;
                 } else { // separator found
                     String val = new String(valchars, i, j - i).trim();
                     if (val.length() > 0) {
                         val = decodeVal(val);
                     }
                     valset.add(val);
                     i = j + 1;
                 }
             }
         }
         if (j == valchars.length && i < j) {
             String val = new String(valchars, i, j - i).trim();
             if (val.length() > 0) {
                 val = decodeVal(val);
             }
             valset.add(val);
         }
         return valset;
     }
 
     /**
      * Decodes a value, %2C to comma and %25 to percent.
      */
     String decodeVal(String v) {
         char[] chars = v.toCharArray();
         StringBuffer sb = new StringBuffer(chars.length);
         int i = 0, lastIdx = 0;
         for (i = 0; i < chars.length; i++) {
             if (chars[i] == '%' && i + 2 < chars.length && chars[i + 1] == '2')
             {
                 if (lastIdx != i) {
                     sb.append(chars, lastIdx, i - lastIdx);
                 }
                 if (chars[i + 2] == 'C') {
                     sb.append(',');
                 } else if (chars[i + 2] == '5') {
                     sb.append('%');
                 } else {
                     sb.append(chars, i, 3);
                 }
                 i += 2;
                 lastIdx = i + 1;
             }
         }
         if (lastIdx != i) {
             sb.append(chars, lastIdx, i - lastIdx);
         }
         return sb.toString();
     }
 
     /**
      * Creates sunserviceid files with the given values under the given
      * directory. sunserviceid files are created so getting schemasubentries
      * does not have to read in every attribute properrties file and look for
      * the serviceid attribute. we just need to look for sub directories with
      * the sunserviceid file.
      */
     void createSunServiceIdFiles(File dirHandle, Set sunserviceids)
             throws SMSException {
         StringBuffer sb = new StringBuffer(dirHandle.getPath());
         sb.append(File.separatorChar);
         sb.append(SMSEntry.ATTR_SERVICE_ID);
         sb.append('=');
         String fileprefix = sb.toString();
 
         Iterator ids = sunserviceids.iterator();
         while (ids.hasNext()) {
             String id = (String) ids.next();
             File idFile = new File(fileprefix + id);
             try {
                 idFile.createNewFile();
             } catch (IOException e) {
                 String errmsg = "SMSFlatFileObject.createSunServiceIdFiles "
                         + ": IO Exception encountered for file "
                         + idFile.getPath() + ". Msg: " + e.getMessage();
                 mDebug.error(errmsg);
                 throw new SMSException(errmsg);
             }
         }
     }
 
     /**
      * Real routine to get sub entries, used by subEntries() and
      * schemaSubEntries().
      * 
      * @throws ServiceNotFoundException
      *             if the configuration object is not found.
      * @throws SchemaException
      *             if a sub directory name is not in the expected "ou=..."
      *             format.
      */
     Set getSubEntries(String objName, String filter, String sidFilter,
             int numOfEntries, boolean sortResults, boolean ascendingOrder)
             throws SMSException {
 
         String objKey = objName.toLowerCase();
         String errmsg = null;
         Set subentries = null;
         mRWLock.readRequest(); // wait indefinitely for the read lock.
         try {
             // Check if object exists.
             String filepath = mNameMap.getProperty(objKey);
             if (filepath == null) {
                 errmsg = "SMSFlatFileObject.getSubEntries: " + objName
                         + ": not found in objects map.";
                 mDebug.warning(errmsg);
                 throw new ServiceNotFoundException(errmsg);
             }
 
             File filehandle = new File(filepath);
             File parentDir = filehandle.getParentFile();
             if (!parentDir.isDirectory()) {
                 errmsg = "SMSFlatFileObject.getSubEntries: " + objName + ": "
                         + filehandle.getPath()
                         + " does not exist or is not a directory.";
                 mDebug.error(errmsg);
                 throw new ServiceNotFoundException(errmsg);
             }
 
             // Create file filter for filter and sid filter.
             FilenameFilter subentFileFilter = 
                     new FilenameFilter("ou=" + filter);
             FilenameFilter sidFileFilter = null;
             if (sidFilter != null && sidFilter.length() > 0) {
                 // filter also needs to be encoded since the file names
                 // are encoded.
                 sidFileFilter = new FilenameFilter(SMSEntry.ATTR_SERVICE_ID
                         + "=" + sidFilter);
             }
 
             // Create set for return, use sorted set if sortResults is true.
             if (sortResults) {
                 subentries = new CaseInsensitiveTreeSet(ascendingOrder);
             } else {
                 subentries = new CaseInsensitiveHashSet();
             }
 
             // Set all entries that match filter, and that match
             // sunserviceid if sidFilter was not null.
             File[] subentriesFound = parentDir.listFiles(subentFileFilter);
             int numEntriesAdded = 0;
             for (int i = 0; i < subentriesFound.length; i++) {
                 File[] sunserviceidFiles = null;
                 if (sidFileFilter == null
                         || ((sunserviceidFiles = subentriesFound[i]
                                 .listFiles(sidFileFilter)) != null 
                                 && sunserviceidFiles.length > 0)) {
                     String filename = subentriesFound[i].getName();
                     int equalSign = filename.indexOf('=');
                     if (equalSign < 0 || equalSign == (filename.length() - 1)) {
                         errmsg = "SMSFlatFileObject.getSubEntries: "
                                 + "Invalid sub entry name found: " + filename;
                         mDebug.error(errmsg);
                         throw new SchemaException(errmsg);
                     }
                     String subentryname = FileNameDecoder.decode(filename
                             .substring(equalSign + 1));
                     subentries.add(subentryname);
                     numEntriesAdded++;
                     // stop if number of entries requested has been reached.
                     // if sort results, need to get the whole list first.
                     if (!sortResults && numOfEntries > 0
                             && numEntriesAdded == numOfEntries) {
                         if (mDebug.messageEnabled()) {
                             mDebug.message("SMSFlatFileObject.getSubEntries: "
                                     + "sub entries maximum number "
                                     + numOfEntries + "reached out of "
                                     + subentriesFound.length + "found.");
                         }
                         break;
                     }
                 }
             }
             if (sortResults && numOfEntries > 0
                     && numEntriesAdded > numOfEntries) {
                 // remove extra entries from the bottom.
                 for (int j = numEntriesAdded - numOfEntries; j > 0; j--) {
                     Object l = ((CaseInsensitiveTreeSet) subentries).last();
                     subentries.remove(l);
                 }
             }
         } finally {
             mRWLock.readDone();
         }
         if (mDebug.messageEnabled()) {
             StringBuffer sb = new StringBuffer("SMSFlatFileObject(");
             sb.append(objName);
             sb.append("): \n");
             sb.append("Returning sub entries: ");
             if (subentries == null) {
                 sb.append("null");
             } else {
                 Iterator iter = subentries.iterator();
                 while (iter.hasNext()) {
                     sb.append('\t');
                     sb.append((String) iter.next());
                 }
             }
             mDebug.message(sb.toString());
         }
         return subentries;
     }
 
     //
     // public methods
     //
 
     /**
      * Constructor for SMSFlatFileObject.
      */
     public SMSFlatFileObject() throws SMSException {
         // Check if initialized (should be called only once by SMSEntry)
         if (!mInitialized)
             initialize();
     }
 
     /**
      * Initializes the SMSFlatFileObject: Gets the flat file directory and
      * default organization DN from AMConfig.properties, creates the root
      * directory if it does not exist.
      */
     public synchronized void initialize() throws SMSException {
         if (mInitialized)
             return;
 
         mDebug = super.debug();
 
         // Create the root directory if it does not exist.
         init();
 
         mInitialized = true;
     }
 
     /**
      * Reads in attributes of a configuration object.
      * 
      * @return A Map with the coniguration object's attributes or null if the
      *         configuration object does not exist or no attributes are found.
      * 
      * @param token
      *            Ignored argument. Access check is assumed to have occurred
      *            before reaching this method.
      * @param objName
      *            Name of the configuration object, expected to be a dn.
      * 
      * @throws SMSException
      *             if an IO error occurred during the read.
      * @throws SchemaException
      *             if a format error occurred while reading the attributes
      *             properties file.
      * @throws IllegalArgumentException
      *             if objName argument is null or empty.
      */
     public Map read(SSOToken token, String objName) throws SMSException,
             SSOException {
 
         // check args
         if (objName == null || objName.length() == 0) {
             throw new IllegalArgumentException(
                     "SMSFlatFileObject.read: object name is null or empty.");
         }
 
         String objKey = objName.toLowerCase();
         String errmsg = null;
         Map attrMap = null;
         mRWLock.readRequest();
         try {
             // check if object exists.
             String filepath = mNameMap.getProperty(objKey);
             if (filepath == null) {
                 if (mDebug.messageEnabled()) {
                     errmsg = "SMSFlatFileObject.read: object " + objName
                             + " not found.";
                     mDebug.message(errmsg);
                 }
             } else {
                 // Read in file as properties.
                 File filehandle = new File(filepath);
                 Properties props = null;
                 try {
                     props = loadProperties(filehandle, objName);
                 } catch (ServiceNotFoundException e) {
                     // props will be null if object does not exist and
                     // this func subsequently returns null
                 }
 
                 // convert each value string to a Set.
                 if (props != null) {
                     attrMap = new CaseInsensitiveHashMap();
                     Enumeration keys = props.propertyNames();
                     while (keys.hasMoreElements()) {
                         String key = (String) keys.nextElement();
                         String vals = props.getProperty(key);
                         Set valset = null;
                         if (vals != null && vals.length() > 0) {
                             valset = toValSet(key, vals);
                             attrMap.put(key, valset);
                         }
                     }
                 }
             }
         } finally {
             mRWLock.readDone();
         }
 
         if (mDebug.messageEnabled()) {
             if (attrMap != null) {
                 mDebug.message("SMSFlatFileObject: read " + objName
                         + " success");
             } else {
                 mDebug.message("SMSFlatFileObject: read " + objName
                         + " returns null.");
             }
         }
         return attrMap;
     }
 
     /**
      * Creates the configuration object. Creates the directory for the object
      * and the attributes properties file with the given attributes.
      * 
      * @param token
      *            Ignored argument. Access check is assumed to have occurred
      *            before reaching this method.
      * @param objName
      *            Name of the configuration object to create. Name is expected
      *            to be a dn.
      * @param attrs
      *            Map of attributes for the object.
      * 
      * @throws IllegalArgumentException
      *             if the objName or attrs argument is null or empty.
      * @throws ServiceAlreadyExistsException
      *             if the configuration object already exists.
      * @throws SMSException
      *             if an IO error occurred while creating the configuration
      *             object.
      */
     public void create(SSOToken token, String objName, Map attrs)
             throws SMSException, SSOException {
 
         if (objName == null || objName.length() == 0 || attrs == null) {
             throw new IllegalArgumentException("SMSFlatFileObject.create: "
                     + "One or more arguments is null or empty");
         }
 
         String objKey = objName.toLowerCase();
         String filepath = null;
         String errmsg = null;
         mRWLock.readRequest();
         try {
             // Check if object already exists.
             filepath = mNameMap.getProperty(objKey);
             if (filepath != null) {
                 errmsg = "SMSFlatFileObject.create: object " + objName
                         + " already exists in " + filepath;
                 mDebug.error(errmsg);
                 throw new ServiceAlreadyExistsException(errmsg);
             }
         } finally {
             mRWLock.readDone();
         }
 
         // Now Create the object.
         mRWLock.writeRequest();
         try {
             filepath = mNameMap.getProperty(objKey); // recheck
             if (filepath != null) {
                 errmsg = "SMSFlatFileObject.create: object " + objName
                         + " already exists in " + filepath;
                 mDebug.error(errmsg);
                 throw new ServiceAlreadyExistsException(errmsg);
             }
             filepath = getAttrFile(objName);
             File filehandle = new File(filepath);
             File parentDir = filehandle.getParentFile();
             if (parentDir.isDirectory()) {
                 errmsg = "SMSFlatFileObject.create: object " + objName
                         + " directory " + parentDir.getPath()
                         + "exists before create!";
                 mDebug.error(errmsg);
                 throw new ServiceAlreadyExistsException(errmsg);
             }
 
             // Put attrs into in properties format,
             // replacing any percent's with %25 and commas with %2C
             // in the values.
             Set sunserviceids = null;
             // there's no need for case insensitive properties here since
             // we are not reading from it.
             Properties props = new Properties();
             Set keys = attrs.keySet();
             if (keys != null) {
                 Iterator keyIter = keys.iterator();
                 while (keyIter.hasNext()) {
                     String key = (String) keyIter.next();
                     Set vals = (Set) attrs.get(key);
                     if (key.equalsIgnoreCase(SMSEntry.ATTR_SERVICE_ID)) {
                         // used later for sunserviceid files.
                         sunserviceids = vals;
                     }
                     String valstr = toValString(vals);
                     props.put(key, valstr);
                 }
             }
 
             // Create directory, property file, etc.
             try {
                 // create directory
                 if (!parentDir.mkdirs()) {
                     errmsg = "SMSFlatFileObject.create: object " + objName
                             + ": Could not create directory "
                             + parentDir.getPath();
                     mDebug.error(errmsg);
                     throw new SMSException(errmsg);
                 }
                 // create the attributes properties file.
                 try {
                     if (!filehandle.createNewFile()) {
                         errmsg = "SMSFlatFileObject.create: object " + objName
                                 + ": Could not create file " + filepath;
                         mDebug.error(errmsg);
                         throw new SMSException(errmsg);
                     }
                 } catch (IOException e) {
                     errmsg = "SMSFlatFileObject: object " + objName
                             + ": IOException encountered when creating file "
                             + filehandle.getPath() + ". Msg: " + e.getMessage();
                     mDebug.error(errmsg);
                     throw new SMSException(errmsg);
                 }
                 // write the attributes properties file.
                 saveProperties(props, filehandle, objName);
                 // create sunserviceid files for faster return in
                 // schemaSubEntries method.
                 if (sunserviceids != null && !sunserviceids.isEmpty()) {
                     createSunServiceIdFiles(parentDir, sunserviceids);
                 }
 
                 // add the name in the name map and save.
                 mNameMap.setProperty(objKey, filepath);
                 saveProperties(mNameMap, mNameMapHandle, null);
             } catch (SMSException e) {
                 // If any error occurred, clean up - remove the directory
                 // and files created.
                 deleteDir(parentDir);
                 mNameMap.remove(objKey);
                 throw e;
             }
         } finally {
             mRWLock.writeDone();
         }
 
         if (mDebug.messageEnabled()) {
             mDebug.message("SMSFlatFileObject.create: " + "Created " + objName
                     + " in file " + filepath);
         }
     }
 
     /**
      * Modify the attributes for the given configuration object.
      * 
      * @param token
      *            Ignored argument. Access check is assumed to have occurred
      *            before reaching this method.
      * @param objName
      *            Name of the configuration object to modify. Name is expected
      *            to be a dn.
      * @param mods
      *            Array of attributes to modify.
      * 
      * @throws IllegalArgumentException
      *             if objName or mods argument is null or empty, or if an error
      *             was encountered getting attributes from the mods argument.
      * @throws ServiceNotFoundException
      *             if the attributes properties file for the configuration
      *             object is not found.
      * @throws SchemaException
      *             if a format error occurred while reading in the existing
      *             attributes properties file.
      * @throws SMSException
      *             if an IO error occurred while reading or writing to the
      *             attributes properties file.
      */
     public void modify(SSOToken token, String objName, ModificationItem[] mods)
             throws SMSException, SSOException {
 
         // check args
         if (objName == null || objName.length() == 0 || mods == null
                 || mods.length == 0) {
             throw new IllegalArgumentException("SMSFlatFileObject.modify: "
                     + "One or more arguments is null or empty");
         }
 
         String objKey = objName.toLowerCase();
         String filepath = null;
         String errmsg = null;
         mRWLock.readRequest();
         try {
             // Check if object exists.
             filepath = mNameMap.getProperty(objKey);
             if (filepath == null) {
                 errmsg = "SMSFlatFileObject.modify: object " + objName
                         + " not found.";
                 mDebug.error(errmsg);
                 throw new ServiceNotFoundException(errmsg);
             }
         } finally {
             mRWLock.readDone();
         }
 
         // Now do the modification.
         mRWLock.writeRequest();
         try {
             filepath = mNameMap.getProperty(objKey); // recheck
             if (filepath == null) {
                 errmsg = "SMSFlatFileObject.modify: object " + objName
                         + " not found.";
                 mDebug.error(errmsg);
                 throw new ServiceNotFoundException(errmsg);
             }
             File filehandle = new File(filepath);
             if (!filehandle.isFile()) {
                 errmsg = "SMSFlatFileObject.modify: "
                         + "Attributes file for object " + objName
                         + " not found.";
                 mDebug.error(errmsg);
                 throw new ServiceNotFoundException(errmsg);
             }
 
             // Read in attributes in existing file first.
             Properties props = loadProperties(filehandle, objName);
 
             // Replace modification items in attributes properties
             for (int i = 0; i < mods.length; i++) {
                 Attribute attr = mods[i].getAttribute(); // will not be null
                 String key = attr.getID(); // will not be null
                 NamingEnumeration en = null;
                 try {
                     en = attr.getAll(); // will not be null
                 } catch (NamingException e) {
                     errmsg = "SMSFlatFileObject.modify: " + objName
                             + ": Error getting attributes: " + e.getMessage();
                     mDebug.error(errmsg);
                     throw new IllegalArgumentException(errmsg);
                 }
                 String valstr = toValString(en);
                 props.put(key, valstr);
             }
 
             // save attributes properties file
             // sunserviceid's are never modified so don't worry about
             // renaming them in modify().
             saveProperties(props, filehandle, objName);
             if (mDebug.messageEnabled()) {
                 mDebug.message("SMSFlatFileObject: " + "Successfully modified "
                         + mods.length + " attributes for " + objName);
             }
         } finally {
             mRWLock.writeDone();
         }
     }
 
     /**
      * Deletes the configuration object and all objects below it.
      * 
      * @param token
      *            Ignored argument. Access check is assumed to have occurred
      *            before reaching this method.
      * @param objName
      *            Name of the configuration object to delete. Name is expected
      *            to be a dn.
      * 
      * @throws IllegalArgumentException
      *             if objName argument is null or empty.
      * @throws SMSException
      *             if any files for or under the configuration object could not
      *             be removed.
      */
     public void delete(SSOToken token, String objName) throws SMSException,
             SSOException {
 
         // Check args
         if (objName == null || objName.length() == 0) {
             throw new IllegalArgumentException(
                     "SMSFlatFileObject.delete: object name is null or empty.");
         }
 
         String objKey = objName.toLowerCase();
         String filepath = null;
         String errmsg = null;
         mRWLock.readRequest();
         try {
             // Check if object exists
             filepath = mNameMap.getProperty(objKey);
             if (filepath == null) {
                 if (mDebug.messageEnabled()) {
                     errmsg = "SMSFlatFileObject.delete: " + objName
                             + ": object not found.";
                     mDebug.message(errmsg);
                 }
             }
         } finally {
             mRWLock.readDone();
         }
 
         if (filepath != null) {
             mRWLock.writeRequest();
             try {
                 filepath = mNameMap.getProperty(objKey); // recheck.
                 if (filepath == null) {
                     if (mDebug.messageEnabled()) {
                         errmsg = "SMSFlatFileObject.delete: " + objName
                                 + ": object not found.";
                         mDebug.message(errmsg);
                     }
                 } else {
                     File filehandle = new File(filepath);
                     File parentDir = filehandle.getParentFile();
 
                     // delete everything from the file dir on.
                     // decend into each directory, removing everything in it.
                     deleteDir(parentDir);
 
                     // remove all names from name map under the objname.
                     objName = objName.toLowerCase();
                     Enumeration keysEnum = mNameMap.keys();
                     while (keysEnum.hasMoreElements()) {
                         String key = (String) keysEnum.nextElement();
                         if (key.endsWith(objName)) {
                             mNameMap.remove(key);
                             if (mDebug.messageEnabled()) {
                                 mDebug.message("SMSFlatFileObject.delete: "
                                         + "deleted " + objName + " from map");
                             }
                         }
                     }
                     saveProperties(mNameMap, mNameMapHandle, null);
                     if (mDebug.messageEnabled()) {
                         mDebug.message("SMSFlatFileObject.delete: "
                                 + "Successfully deleted " + objName
                                 + " in path " + filepath);
                     }
                 }
             } finally {
                 mRWLock.writeDone();
             }
         }
     }
 
     /**
      * Returns a Set of sub-entry names that match the given filter.
      * 
      * @return Set of sub entry names that match the given filter, or an empty
      *         Set if the objName is not found or if no sub entries are found
      *         with the given filter.
      * 
      * @param token
      *            Ignored argument. Access check is assumed to have occurred
      *            before reaching this method.
      * @param objName
      *            Name of the configuration object to get sub entries for. Name
      *            is expected to be a dn.
      * @param filter
      *            Filter of sub entry names to get. Only the wildcard character
      *            '*' is currently supported.
      * @param numOfEntries
      *            Number of entries to return, or 0 to return all entries.
      * @param sortResults
      *            Whether to sort results. If true will return a Set that will
      *            return entries in a sorted order.
      * @param ascendingOrder
      *            Whether the sorted results should be in alphabetically
      *            ascending or decending order. This argument is ignored if
      *            sortResults is false.
      * 
      * @throws IllegalArgumentException
      *             if objName or filter is null or empty, or if numOfEntries is
      *             less than 0.
      * @throws SchemaException
      *             if a sub directory name is not in the expected "ou=..."
      *             format.
      */
     public Set subEntries(SSOToken token, String objName, String filter,
             int numOfEntries, boolean sortResults, boolean ascendingOrder)
             throws SMSException, SSOException {
 
         // Check args
         if (objName == null || objName.length() == 0 || filter == null
                 || filter.length() == 0 || numOfEntries < 0) {
             throw new IllegalArgumentException(
                     "SMSFlatFileObject.subEntries(): "
                             + "One or more arguments is null or empty: "
                             + "objName [" + objName == null ? "null" : objName
                             + "] filter ]" + filter == null ? "null" : filter
                             + "]");
 
         }
 
         Set subentries = null;
         try {
             subentries = getSubEntries(objName, filter, null, numOfEntries,
                     sortResults, ascendingOrder);
         } catch (ServiceNotFoundException e) {
             // return empty set if object does not exist.
             subentries = new CaseInsensitiveHashSet();
         }
 
         if (mDebug.messageEnabled()) {
             mDebug.message("SMSFlatFileObject: " + "SubEntries search "
                     + filter + " for " + objName + " returned "
                     + subentries.size() + " items");
         }
 
         return (subentries);
     }
 
     /**
      * Returns a Set of sub entry names that match the given filter and the
      * given sun service id filter.
      * 
      * @return Set of sub entry names that match the given filter and sun
      *         service id filter, or an empty Set if the objName is not found or
      *         if no sub entries are found with the given filters.
      * 
      * @param token
      *            Ignored argument. Access check is assumed to have occurred
      *            before reaching this method.
      * @param objName
      *            Name of the configuration object to get sub entries for. Name
      *            is expected to be a dn.
      * @param filter
      *            Filter of sub entry names to get. Only the wildcard character
      *            '*' is currently supported.
      * @param sidFilter
      *            Filter of Sun Service ID for the sub entries.
      * @param numOfEntries
      *            Number of entries to return, or 0 to return all entries.
      * @param sortResults
      *            Whether to sort results. If true will return a Set that will
      *            return entries in a sorted order.
      * @param ascendingOrder
      *            Whether the sorted results should be in alphabetically
      *            ascending or decending order. This argument is ignored if
      *            sortResults is false.
      * 
      * @throws IllegalArgumentException
      *             if objName or filter is null or empty, or if numOfEntries is
      *             less than 0.
      * @throws SchemaException
      *             if a sub directory name is not in the expected "ou=..."
      *             format.
      */
     public Set schemaSubEntries(SSOToken token, String objName, String filter,
             String sidFilter, int numOfEntries, boolean sortResults,
             boolean ascendingOrder) throws SMSException, SSOException {
 
         // Check args
         if (objName == null || objName.length() == 0 || filter == null
                 || filter.length() == 0 || sidFilter == null
                 || sidFilter.length() == 0) {
             throw new IllegalArgumentException(
                     "SMSFlatFileObject.schemaSubEntries: "
                             + "One or more arguments is null or empty.");
         }
 
         Set subentries = null;
         try {
             subentries = getSubEntries(objName, filter, sidFilter,
                     numOfEntries, sortResults, ascendingOrder);
         } catch (ServiceNotFoundException e) {
             // return empty set if service does not exist.
             subentries = new CaseInsensitiveHashSet();
         }
 
         if (mDebug.messageEnabled()) {
             mDebug.message("SMSFlatFileObject: " + "SchemaSubEntries search "
                     + filter + " for " + objName + " returned "
                     + subentries.size() + " items");
         }
 
         return (subentries);
     }
 
     /**
      * Checks if the configuration object exists.
      * 
      * @return true if the configuration object exists.
      * 
      * @param token
      *            Ignored argument. Access check is assumed to have occurred
      *            before reaching this method.
      * @param objName
      *            Name of the configuration object to check.
      * 
      * @throws IllegalArgumentException
      *             if objName is null or empty.
      */
     public boolean entryExists(SSOToken token, String objName) {
 
         boolean exists = false;
 
         if (objName == null || objName.length() == 0) {
             throw new IllegalArgumentException(
                     "SMSFlatFileObject.entryExists: "
                             + "One or more arguments is null or empty.");
         }
 
         mRWLock.readRequest();
         try {
             String filepath = mNameMap.getProperty(objName.toLowerCase());
             if (filepath != null) {
                 exists = true;
             }
         } finally {
             mRWLock.readDone();
         }
         if (mDebug.messageEnabled()) {
             mDebug.message("SMSFlatFileObject.exists: config object " + objName
                     + " exists: " + exists);
         }
         return exists;
     }
 
     /**
      * Search for a config object with the given filter. Do some cheating here -
      * callers of this method only pass service name and version in the filter
      * in a ldap filter format. So return entries matching service name and
      * version in the filter.
      * 
      * @return a Set of entries (dn's) that match the given filter.
      * 
      * @param token
      *            Ignored argument. Access check is assumed to have occurred
      *            before reaching this method.
      * @param objName
      *            Name of the configuration object to begin search. Name is
      *            expected to be a dn.
      * @param filter
      *            Filter of service name and version. Expected to be in
      *            SMSEntry.FILTER_PATTERN_SERVICE format.
      * 
      * @throws IllegalArgumentException
      *             if objName or filter is null or empty, or if filter is not in
      *             the expected format.
      */
     public Set search(SSOToken token, String objName, String filter)
             throws SSOException, SMSException {
 
         if (objName == null || objName.length() == 0 || filter == null
                 || filter.length() == 0) {
             throw new IllegalArgumentException("SMSFlatFileObject.search: "
                     + "One or more arguments is null or empty.");
         }
 
         try {
             String filterPattern = SMSEntry.getFilterPatternService();
             MessageFormat format = new MessageFormat(filterPattern);
             Object[] args = format.parse(filter);
             if (args.length != 2 || !(args[0] instanceof String)
                     || !(args[1] instanceof String)) {
                 throw new IllegalArgumentException(
                         "SMSFlatFile.search: Error parsing filter pattern "
                                 + filter);
             }
             String serviceName = (String) args[0];
             String sunservice = (String) args[1];
             String theObjName = "ou=" + serviceName + ",ou=services," + mRootDN;
             Set subentries = null;
             subentries = getSubEntries(theObjName, "*", "ou=" + sunservice, 0,
                     false, false);
             return subentries;
         } catch (ParseException e) {
             throw new IllegalArgumentException(
                     "SMSFlatFileObject.search: Unexpected filter pattern "
                             + filter);
         }
     }
 
     /**
      * Returns the suborganization names. Returns a set of SMSEntry objects that
      * are suborganization names. The paramter <code>numOfEntries</code>
      * identifies the number of entries to return, if <code>0</code> returns
      * all the entries.
      */
     public Set searchSubOrgNames(SSOToken token, String dn, String filter,
             int numOfEntries, boolean sortResults, boolean ascendingOrder,
             boolean recursive) throws SMSException, SSOException {
 
         // not yet implemented
        return null;
     }
 
     /**
      * Returns the organization names. Returns a set of SMSEntry objects that
      * are organization names. The paramter <code>numOfEntries</code>
      * identifies the number of entries to return, if <code>0</code> returns
      * all the entries.
      */
     public Set searchOrganizationNames(SSOToken token, String dn,
             int numOfEntries, boolean sortResults, boolean ascendingOrder,
             String serviceName, String attrName, Set values)
             throws SMSException, SSOException {
 
         // not yet implemented
        return null;
     }
 
     /**
      * Register a listener. Not yet implemented.
      */
     public String registerCallbackHandler(SSOToken token,
             SMSObjectListener changeListener) throws SMSException, SSOException
     {
         // not yet implemented
         return null;
     }
 
     /**
      * De-Register a listener. Not yet implemented
      */
     public void deregisterCallbackHandler(String id) {
         // not yet implemented
     }
 
     /**
      * Returns the default Org of this server.
      * 
      * @return DN of the default org.
      */
     public String getRootSuffix() {
         return (mRootDN);
     }
 
     /**
      * @return a String representing the name of this class.
      */
     public String toString() {
         return ("SMSFlatFileObject");
     }
 
 }
