 package org.wyona.yarep.impl.repo.vfs;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.Logger;
 
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.Field.Index;
 import org.apache.lucene.document.Field.Store;
 import org.apache.lucene.index.IndexWriter;
 
 import org.wyona.yarep.core.NoSuchRevisionException;
 import org.wyona.yarep.core.Node;
 import org.wyona.yarep.core.NodeStateException;
 import org.wyona.yarep.core.NodeType;
 import org.wyona.yarep.core.Path;
 import org.wyona.yarep.core.Property;
 import org.wyona.yarep.core.PropertyType;
 import org.wyona.yarep.core.RepositoryException;
 import org.wyona.yarep.core.Revision;
 import org.wyona.yarep.core.UID;
 import org.wyona.yarep.core.attributes.VersionableV1;
 import org.wyona.yarep.impl.AbstractNode;
 import org.wyona.yarep.impl.DefaultProperty;
 
 /**
  * This class represents a repository node.
  * A repository node may be either a collection ("directory") or a resource ("file").
  */
 public class VirtualFileSystemNode extends AbstractNode implements VersionableV1 {
     private static Logger log = Logger.getLogger(VirtualFileSystemNode.class);
 
     protected static final String META_FILE_NAME = "meta";
     protected static final String REVISIONS_BASE_DIR = "revisions";
     protected static final String DATE_INDEX_BASE_DIR = "index_date";
     protected static final String DATE_INDEX_ID_FILENAME = "id.txt";
     protected static final String META_DIR_SUFFIX = ".yarep";
     
     //protected FileSystemRepository repository;
     protected File contentDir;
     protected File contentFile;
     protected File metaDir;
     protected File metaFile;
     
     // NOTE: Flag to indicate if revisions already have been read/initialized from file system
     protected boolean areRevisionsRead = false;
     
     protected RevisionDirectoryFilter revisionDirectoryFilter = new RevisionDirectoryFilter();
     
     
     /**
      * Constructor
      * @throws RepositoryException
      */
     public VirtualFileSystemNode(VirtualFileSystemRepository repository, String path, String uuid) throws RepositoryException {
         super(repository, path, uuid);
         init();
     }
     
     /**
      * Constructor
      * @throws RepositoryException
      */
     protected VirtualFileSystemNode(VirtualFileSystemRepository repository, String path, String uuid, boolean doInit) throws RepositoryException {
         super(repository, path, uuid);
         
         if (doInit) {
             init();
         }
     }
     
     /**
      * Init repository
      */
     protected void init() throws RepositoryException {
         
         this.contentDir = getRepository().getContentDir();
         this.contentFile = new File(this.contentDir, this.uuid);
         if (getRepository().getMetaDir() != null) {
             this.metaDir = new File(getRepository().getMetaDir(), uuid + META_DIR_SUFFIX);
         } else {
             this.metaDir = new File(this.contentDir, uuid + META_DIR_SUFFIX);
         }
         this.metaFile = new File(this.metaDir, META_FILE_NAME);
         
         if (log.isDebugEnabled()) {
             log.debug("VirtualFileSystemNode: path=" + path + " uuid=" + uuid);
             log.debug("contentDir=" + contentDir);
             log.debug("contentFile=" + contentFile);
             log.debug("metaDir=" + metaDir);
             log.debug("metaFile=" + metaFile);
         }
         
         if (!metaFile.exists()) {
             createMetaFile();
         }
         readProperties();
         // defer reading of revisions for performance reasons
         // readRevisions();
     }
     
     protected void createMetaFile() throws RepositoryException {
         log.debug("creating new meta file in dir: " + metaDir);
         if (!metaDir.exists()) {
             metaDir.mkdirs();
         }
         this.properties = new HashMap();
         if (this.contentFile.isDirectory()) {
             this.setProperty(PROPERTY_TYPE, NodeType.TYPENAME_COLLECTION);
         } else {
             this.setProperty(PROPERTY_TYPE, NodeType.TYPENAME_RESOURCE);
             //this.setProperty(PROPERTY_SIZE, this.contentFile.length());
             //this.setProperty(PROPERTY_LAST_MODIFIED, this.contentFile.lastModified());
         }
     }
     
     /**
      * Read properties from meta file
      */
     protected void readProperties() throws RepositoryException {
         try {
             log.debug("Reading meta file: " + this.metaFile);
             this.properties = new HashMap();
             BufferedReader reader = new BufferedReader(new FileReader(this.metaFile));
             String line;
             while ((line = reader.readLine()) != null) {
                 line = line.trim();
                 String name;
                 String typeName;
                 String value;
                 try {
                     name = line.substring(0, line.indexOf("<")).trim();
                     typeName = line.substring(line.indexOf("<")+1, line.indexOf(">")).trim();
                     value = line.substring(line.indexOf(":")+1).trim();
                 } catch (StringIndexOutOfBoundsException e) {
                     throw new RepositoryException("Error while parsing meta file: " + this.metaFile 
                             + " at line " + line);
                 }
                 Property property = new DefaultProperty(name, PropertyType.getType(typeName), this);
                 property.setValueFromString(value);
                 this.properties.put(name, property);
             }
             reader.close();
         } catch (IOException e) {
             throw new RepositoryException("Error while reading meta file: " + metaFile + ": " 
                     + e.getMessage());
         }
     }
     
     /**
      * FIXME: this implementation does not work correctly when a string property contains a line-break. 
      * @throws RepositoryException
      */
     protected void saveProperties() throws RepositoryException {
         try {
             log.debug("Writing meta file: " + this.metaFile);
             PrintWriter writer = new PrintWriter(new FileOutputStream(this.metaFile));
             
             Iterator iterator = this.properties.values().iterator();
             while (iterator.hasNext()) {
                 Property property = (Property)iterator.next();
                 writer.println(property.getName() + "<" + PropertyType.getTypeName(property.getType()) + ">:" + property.getValueAsString());
                 if (((VirtualFileSystemRepository)repository).isAutoPropertyIndexingEnabled()) {
                     repository.getIndexer().index(this, property);
                 }
             }
             writer.flush();
             writer.close();
         } catch (Exception e) {
             throw new RepositoryException("Error while writing meta file: " + metaFile + ": " + e.getMessage(), e);
         }
     }
     
     /**
      * @see org.wyona.yarep.core.Node#getNodes()
      */
     public Node[] getNodes() throws RepositoryException {
         Path[] childPaths = getRepository().getMap().getChildren(new Path(this.path));
         
         Node[] childNodes = new Node[childPaths.length];
         for (int i=0; i<childPaths.length; i++) {
             childNodes[i] = this.repository.getNode(childPaths[i].toString());
         }
         return childNodes;
     }
     
     /**
      * @see org.wyona.yarep.core.Node#addNode(java.lang.String, int)
      */
     public Node addNode(String name, int type) throws RepositoryException {
         String newPath = getPath() + "/" + name;
         log.debug("adding node: " + newPath);
         if (this.repository.existsNode(newPath)) {
             throw new RepositoryException("Node exists already: " + newPath);
         }
         UID uid = getRepository().getMap().create(new Path(newPath), type);
         // create file:
         File file = new File(this.contentDir, uid.toString());
         try {
             if (type == NodeType.COLLECTION) {
                 file.mkdirs();
             } else if (type == NodeType.RESOURCE) {
                 file.createNewFile();
             } else {
                 throw new RepositoryException("Unknown node type: " + type);
             }
             return this.repository.getNode(newPath);
         } catch (IOException e) {
             throw new RepositoryException("Could not access file " + file, e);
         }
     }
     
     /**
      * @see org.wyona.yarep.core.Node#removeProperty(java.lang.String)
      */
     public void removeProperty(String name) throws RepositoryException {
         this.properties.remove(name);
         saveProperties();
     }
     
     /**
      * @see org.wyona.yarep.core.Node#setProperty(org.wyona.yarep.core.Property)
      */
     public void setProperty(Property property) throws RepositoryException {
         this.properties.put(property.getName(), property);
         saveProperties();
     }
 
     /**
      * @see org.wyona.yarep.core.Node#getInputStream()
      */
     public InputStream getInputStream() throws RepositoryException {
         try {
             if (isCollection()) {
                 if (getRepository().getAlternative() != null) {
                     File alternativeFile = new File(contentFile, getRepository().getAlternative());
                     if (alternativeFile.isFile()) {
                         return new FileInputStream(alternativeFile);
                     } else {
                         log.warn("Is Collection (" + contentFile + ") and no alternative File exists (" + alternativeFile + ")");
                         return new java.io.StringBufferInputStream(getDirectoryListing(contentFile, getRepository().getDirListingMimeType()));
                     }
                 } else {
                     log.warn("Is Collection: " + contentFile);
                     return new java.io.StringBufferInputStream(getDirectoryListing(contentFile, getRepository().getDirListingMimeType()));
                 }
             } else {
                 return new FileInputStream(contentFile);
             }
         } catch (FileNotFoundException e) {
             throw new RepositoryException(e.getMessage(), e);
         }
         //return getProperty(PROPERTY_CONTENT).getInputStream();
     }
     
     /**
      * @see org.wyona.yarep.core.Node#getOutputStream()
      */
     public OutputStream getOutputStream() throws RepositoryException {
         try {
             if (isCollection()) {
                 if (getRepository().getAlternative() != null) {
                     File alternativeFile = new File(contentFile, getRepository().getAlternative());
                     if (alternativeFile.isFile()) {
                         return new VirtualFileSystemOutputStream(this, alternativeFile);
                     } else {
                         throw new RepositoryException("Is not a file: " + alternativeFile);
                     }
                 } else {
                     throw new RepositoryException("Is a directory: " + this.contentFile);
                 }
             } else {
                 //return new FileOutputStream(this.contentFile);
                 return new VirtualFileSystemOutputStream(this, this.contentFile);
             }
         } catch (FileNotFoundException e) {
             throw new RepositoryException(e.getMessage(), e);
         }
         //return getProperty(PROPERTY_CONTENT).getOutputStream();
     }
     
     /**
      * @see org.wyona.yarep.core.Node#checkin()
      */
     public Revision checkin() throws NodeStateException, RepositoryException {
         return checkin("");
     }
     
     /**
      * @see org.wyona.yarep.core.Node#checkin()
      */
     public Revision checkin(String comment) throws NodeStateException, RepositoryException {
         if (!isCheckedOut()) {
             throw new NodeStateException("Node " + path + " is not checked out.");
         }
         Revision revision = createRevision(comment);
         
         setProperty(PROPERTY_IS_CHECKED_OUT, false);
         setProperty(PROPERTY_CHECKIN_DATE, new Date());
         
         return revision;
     }
     
     public void cancelCheckout() throws NodeStateException, RepositoryException {
         if (!isCheckedOut()) {
             throw new NodeStateException("Node " + path + " is not checked out.");
         }
         
         setProperty(PROPERTY_IS_CHECKED_OUT, false);
         setProperty(PROPERTY_CHECKIN_DATE, new Date());
     }
     
 
     /**
      * @see org.wyona.yarep.core.Node#checkout(java.lang.String)
      */
     public void checkout(String userID) throws NodeStateException, RepositoryException {
         // TODO: this should be somehow synchronized
         if (isCheckedOut()) {
             throw new NodeStateException("Node " + path + " is already checked out by: " + getCheckoutUserID());
         }
         
         setProperty(PROPERTY_IS_CHECKED_OUT, true);
         setProperty(PROPERTY_CHECKOUT_USER_ID, userID);
         setProperty(PROPERTY_CHECKOUT_DATE, new Date());
 
         /*if (getRevisions().length == 0) {
             // create a backup revision
             createRevision("initial revision");
         }*/
     }
     
     /**
      * Create revision of this node
      * @param comment Comment re this new revision
      */
     protected Revision createRevision(String comment) throws RepositoryException {
         if (!areRevisionsRead) {
             readRevisions();
         }
         try {
             String revisionName = String.valueOf(System.currentTimeMillis());
 
             File destContentFile = getRevisionContentFile(revisionName);
             FileUtils.copyFile(this.contentFile, destContentFile);
         
             File destMetaFile = getRevisionMetaFile(revisionName);
             FileUtils.copyFile(this.metaFile, destMetaFile);
         
             Revision revision = new VirtualFileSystemRevision(this, revisionName);
             revision.setProperty(PROPERTY_IS_CHECKED_OUT, false);
             ((VirtualFileSystemRevision)revision).setCreationDate(new Date());
             ((VirtualFileSystemRevision)revision).setCreator(getCheckoutUserID());
             ((VirtualFileSystemRevision)revision).setComment(comment);
             this.revisions.put(revisionName, revision);
 
             log.warn("TODO: Add to date index!");
 
             return revision;
         } catch (IOException e) {
             log.error(e.getMessage(), e);
             throw new RepositoryException(e.getMessage(), e);
         }
     }
     
     /**
      * Read revisions
      */
     protected void readRevisions() throws RepositoryException {
         File revisionsBaseDir = new File(this.metaDir, REVISIONS_BASE_DIR);
         if (log.isDebugEnabled()) log.debug("Read revisions: " + revisionsBaseDir);
         
         File[] revisionDirs = revisionsBaseDir.listFiles(this.revisionDirectoryFilter);
         
         this.revisions = new LinkedHashMap();
         
         if (revisionDirs != null) {
             if (log.isDebugEnabled()) log.debug("Number of revisions which made it through the filter: " + revisionDirs.length);
             Arrays.sort(revisionDirs);
             for (int i = 0; i < revisionDirs.length; i++) {
                 String revisionName = revisionDirs[i].getName();
                 Revision revision = new VirtualFileSystemRevision(this, revisionName);
                 this.revisions.put(revisionName, revision);
             }
         }
         areRevisionsRead = true;
     }
     
     /**
      * @see org.wyona.yarep.core.Node#restore(java.lang.String)
      */
     public void restore(String revisionName) throws NoSuchRevisionException, RepositoryException {
         try {
             File revisionsBaseDir = new File(this.metaDir, REVISIONS_BASE_DIR);
             File revisionDir = new File(revisionsBaseDir, revisionName);
             
             File srcContentFile = new File(revisionDir, VirtualFileSystemRevision.CONTENT_FILE_NAME);
             FileUtils.copyFile(srcContentFile, this.contentFile);
         
             File srcMetaFile = new File(revisionDir, META_FILE_NAME);
             FileUtils.copyFile(srcMetaFile, this.metaFile);
             
             setProperty(AbstractNode.PROPERTY_LAST_MODIFIED, this.contentFile.lastModified());
         } catch (IOException e) {
             log.error(e.getMessage(), e);
             throw new RepositoryException(e.getMessage(), e);
         }
     }
     
     /**
      * Check if file is a revision
      */
     protected class RevisionDirectoryFilter implements FileFilter {
         public boolean accept(File pathname) {
             if (pathname.getName().matches("[0-9]+") && pathname.isDirectory()) {
                 return true;
             } else {
                 if (!pathname.getName().startsWith(".")) { // Ignore hidden files
                     log.warn("Does not seem to be a revision: " + pathname);
                 }
                 return false;
             }
         }
     }
     
     /**
      * Changing a property should update the last modified date.
      * FIXME: This implementation does not change the last modified date if a property changes.
      * @see org.wyona.yarep.impl.AbstractNode#getLastModified()
      */
     public long getLastModified() throws RepositoryException {
         return this.contentFile.lastModified();
     }
     
     /**
      * @see org.wyona.yarep.impl.AbstractNode#getSize()
      */
     public long getSize() throws RepositoryException {
         return this.contentFile.length();
     }
 
     /**
      *
      */
     public VirtualFileSystemRepository getRepository() {
         return (VirtualFileSystemRepository)this.repository;
     }
 
     /**
      * @see org.wyona.yarep.core.Node#delete()
      */
     public void delete() throws RepositoryException {
         deleteRec(this);
     }
        
     protected void deleteRec(Node node) throws RepositoryException {
         Node[] children = node.getNodes();
         for (int i=0; i<children.length; i++) {
             deleteRec(children[i]);
         }
         //boolean success = getRepository().getMap().delete(new Path(getPath()));
         try {
             if (getRepository().getMap().isCollection(new Path(getPath()))) {
                 FileUtils.deleteDirectory(this.contentFile);
             } else {
                 this.contentFile.delete();
             }
             FileUtils.deleteDirectory(this.metaDir);
         } catch (IOException e) {
             throw new RepositoryException("Could not delete node: " + node.getPath() + ": " + 
                     e.toString(), e);
         }
     }
 
     /**
      *
      */
     public int getType() throws RepositoryException {
         if (getRepository().getMap().isCollection(new Path(path))) {
             return NodeType.COLLECTION;
         } else if (getRepository().getMap().isResource(new Path(path))) {
             return NodeType.RESOURCE;
         } else {
             return -1;
         }
     }
 
     /**
      * Get directory listing
      */
     public String getDirectoryListing(File file, String mimeType) {
         StringBuffer dirListing = new StringBuffer("<?xml version=\"1.0\"?>");
         if(mimeType.equals("application/xhtml+xml")) {
             dirListing.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
             dirListing.append("<head>");
             dirListing.append("<title>"+path+"</title>");
             dirListing.append("</head>");
             dirListing.append("<body>");
             dirListing.append("<ul>");
             String[] children = file.list();
             for (int i = 0; i < children.length; i++) {
                 File child = new File(file, children[i]);
                 if (child.isFile()) {
                     dirListing.append("<li>File: <a href=\"" + children[i] + "\">" + children[i] + "</a></li>");
                 } else if (child.isDirectory()) {
                     dirListing.append("<li>Directory: <a href=\"" + children[i] + "/\">" + children[i] + "/</a></li>");
                 } else {
                     dirListing.append("<li>Child: <a href=\"" + children[i] + "\">" + children[i] + "</a></li>");
                 }
             }
             dirListing.append("</ul>");
             dirListing.append("</body>");
             dirListing.append("</html>");
         } else if (mimeType.equals("application/xml")) {
             dirListing.append("<directory xmlns=\"http://www.wyona.org/yarep/1.0\" path=\""+path+"\" fs-path=\""+file+"\">");
             String[] children = file.list();
             for (int i = 0; i < children.length; i++) {
                 File child = new File(file, children[i]);
                 if (child.isFile()) {
                     dirListing.append("<file name=\"" + children[i] + "\"/>");
                 } else if (child.isDirectory()) {
                     dirListing.append("<directory name=\"" + children[i] + "\"/>");
                 } else {
                     dirListing.append("<child name=\"" + children[i] + "\"/>");
                 }
             }
             dirListing.append("</directory>");
         } else {
             dirListing.append("<no-such-mime-type-supported>" + mimeType + "</no-such-mime-type-supported>");
         }
         return dirListing.toString();
     }
 
     /**
      * @see org.wyona.yarep.core.attributes.VersionableV1#getRevision(Date)
      */
     public Revision getRevision(Date date) throws Exception {
         log.warn("DEBUG: Use vfs-repo specific implementation: " + getPath());
 
         // New implementation
         File dateIndexBaseDir = new File(this.metaDir, DATE_INDEX_BASE_DIR);
         if (dateIndexBaseDir.isDirectory()) {
             Revision revision = getRevisionViaDateIndex(date);
             if (revision != null) {
                 return revision;
             } else {
                 //log.warn("No revision found via data index, try to find otherwise ...");
                 log.warn("No revision found for node '" + path + "' and point in time '" + date + "'");
                 return null;
             }
         } else {
             log.warn("No date index yet: " + dateIndexBaseDir);
             buildDateIndex();
             return getRevision(date);
         }
 
         // Old implementation
 /*
         if(log.isDebugEnabled()) log.debug("Use vfs-repo specific implementation ...");
         Revision[] revisions = getRevisions();
         for (int i = revisions.length - 1; i >= 0; i--) {
             Date creationDate = new Date(Long.parseLong(revisions[i].getRevisionName())); // INFO: The name of a revision is based on System.currentTimeMillis() (see createRevision(String))
             //Date creationDate = revisions[i].getCreationDate(); // INFO: This method is slower than the above
             if (creationDate.before(date) || creationDate.equals(date)) {
                 if (log.isDebugEnabled()) log.debug("Revision found: " + revisions[i].getRevisionName());
                 return revisions[i];
             }
         }
         log.warn("No revision found for node '" + path + "' and point in time '" + date + "'");
         return null;
 */
     }
 
     /**
      * Get revision via date index
      */
     private Revision getRevisionViaDateIndex(Date date) throws Exception {
         File dateIndexBaseDir = new File(this.metaDir, DATE_INDEX_BASE_DIR);
         log.warn("DEBUG: Use vfs-repo specific implementation: " + getPath() + ", " + date);
         Calendar cal = Calendar.getInstance();
         cal.setTime(date);
 
         return getRevisionByYear(dateIndexBaseDir, cal);
     }
 
     /**
      * Get revision by year, whereas the algorithm assumes that the order is ascending: 2007, 2008, 2009, 2010, ...
      * @param dateIndexBaseDir Directory where date index is located
      * @param cal Point in time for which a revision shall be found
      */
     private Revision getRevisionByYear(File dateIndexBaseDir, Calendar cal) throws Exception {
         String[] years = dateIndexBaseDir.list();
         for (int i = years.length - 1; i >= 0; i--) {
             log.warn("DEBUG: Year: " + years[i]);
             try {
                 int year = new Integer(years[i]).intValue();
                if (year <= cal.get(Calendar.YEAR)) {
                    log.warn("DEBUG: Year matched: " + year);
                    Revision revision = getRevisionByMonth(new File(dateIndexBaseDir, years[i]), cal);
                    if (revision != null) {
                        return revision;
                    } else {
                        if (year < cal.get(Calendar.YEAR)) {
                            return getYoungestRevisionOfYear(new File(dateIndexBaseDir, years[i]));
                        } else {
                            log.warn("Youngest does not make sense! Try next year lower ...");
                        }
                    }
                 }
             } catch(NumberFormatException e) {
                 log.warn("Does not seem to be a year '" + years[i] + "' and hence will be ignored.");
             }
         }
         return null;
     }
 
     /**
      * Get youngest revision of year, whereas the algorithm assumes that the order of months is ascending 01, 02, ..., 12
      * @param yearDir Directory of year containing months
      */
     private Revision getYoungestRevisionOfYear(File yearDir) throws Exception {
         String[] months = yearDir.list();
         for (int k = months.length - 1; k >= 0; k--) {
             try {
                 int month = Integer.parseInt(months[k]);
                 //int month = new Integer(months[k]).intValue();
                 if (1 <= month && month <= 12) {
                     log.warn("DEBUG: Youngest month '" + month + "' of year '" + yearDir + "' found");
                     return getYoungestRevisionOfMonth(new File(yearDir, months[k]));
                 } else {
                     log.warn("Does not seem to be a month '" + month + "' and hence will be ignored.");
                 }
             } catch(NumberFormatException e) {
                 log.warn("Does not seem to be a month '" + months[k] + "' and hence will be ignored.");
             }
         }
         log.warn("No youngest month found within year '" + yearDir + "'");
         return null;
     }
 
     /**
      * Get youngest revision of month, whereas the algorithm assumes that the order of days is ascending 01, 02, ..., 31
      * @param monthDir Directory of month containing days
      */
     private Revision getYoungestRevisionOfMonth(File monthDir) throws Exception {
         String[] days = monthDir.list();
         for (int k = days.length - 1; k >= 0; k--) {
             try {
                 int day = Integer.parseInt(days[k]);
                 if (1 <= day && day <= 31) {
                     log.warn("DEBUG: Youngest day '" + day + "' of month '" + monthDir + "' found");
                     return getYoungestRevisionOfDay(new File(monthDir, days[k]));
                 } else {
                     log.warn("Does not seem to be a day '" + day + "' and hence will be ignored.");
                 }
             } catch(NumberFormatException e) {
                 log.warn("Does not seem to be a day '" + days[k] + "' and hence will be ignored.");
             }
         }
         log.warn("No youngest day found within month '" + monthDir + "'");
         return null;
     }
 
     /**
      * Get youngest revision of day, whereas the algorithm assumes that the order of hours is ascending 00, 01, ..., 23
      * @param dayDir Directory of day containing hours
      */
     private Revision getYoungestRevisionOfDay(File dayDir) throws Exception {
         String[] hours = dayDir.list();
         for (int k = hours.length - 1; k >= 0; k--) {
             try {
                 int hour = Integer.parseInt(hours[k]);
                 if (0 <= hour && hour <= 23) {
                     log.warn("DEBUG: Youngest hour '" + hour + "' of day '" + dayDir + "' found");
                     return getYoungestRevisionOfHour(new File(dayDir, hours[k]));
                 } else {
                     log.warn("Does not seem to be a hour '" + hour + "' and hence will be ignored.");
                 }
             } catch(NumberFormatException e) {
                 log.warn("Does not seem to be a hour '" + hours[k] + "' and hence will be ignored.");
             }
         }
         log.warn("No youngest hour found within day '" + dayDir + "'");
         return null;
     }
 
     /**
      * Get youngest revision of hour, whereas the algorithm assumes that the order of minutes is ascending 00, 01, ..., 59
      * @param hourDir Directory of hour containing minutes
      */
     private Revision getYoungestRevisionOfHour(File hourDir) throws Exception {
         String[] minutes = hourDir.list();
         for (int k = minutes.length - 1; k >= 0; k--) {
             try {
                 int minute = Integer.parseInt(minutes[k]);
                 if (0 <= minute && minute <= 59) {
                     log.warn("DEBUG: Youngest minute '" + minute + "' of hour '" + hourDir + "' found");
                     return getYoungestRevisionOfMinute(new File(hourDir, minutes[k]));
                 } else {
                     log.warn("Does not seem to be a minute '" + minute + "' and hence will be ignored.");
                 }
             } catch(NumberFormatException e) {
                 log.warn("Does not seem to be a minute '" + minutes[k] + "' and hence will be ignored.");
             }
         }
         log.warn("No youngest hour found within hour '" + hourDir + "'");
         return null;
     }
 
     /**
      * Get youngest revision of minute, whereas the algorithm assumes that the order of seconds is ascending 00, 01, ..., 59
      * @param minuteDir Directory of minute containing seconds
      */
     private Revision getYoungestRevisionOfMinute(File minuteDir) throws Exception {
         String[] seconds = minuteDir.list();
         for (int k = seconds.length - 1; k >= 0; k--) {
             try {
                 int second = Integer.parseInt(seconds[k]);
                 if (0 <= second && second <= 59) {
                     log.warn("DEBUG: Youngest second '" + second + "' of minute '" + minuteDir + "' found");
                     return getYoungestRevisionOfSecond(new File(minuteDir, seconds[k]));
                 } else {
                     log.warn("Does not seem to be a second '" + second + "' and hence will be ignored.");
                 }
             } catch(NumberFormatException e) {
                 log.warn("Does not seem to be a second '" + seconds[k] + "' and hence will be ignored.");
             }
         }
         log.warn("No youngest second found within minute '" + minuteDir + "'");
         return null;
     }
 
     /**
      * Get youngest revision of second, whereas the algorithm assumes that the order of milliseconds is ascending 0, 1, ..., 999
      * @param secondDir Directory of second containing milliseconds 
      */
     private Revision getYoungestRevisionOfSecond(File secondDir) throws Exception {
         String[] millis = secondDir.list();
         for (int k = millis.length - 1; k >= 0; k--) {
             try {
                 int milli = Integer.parseInt(millis[k]);
                 if (0 <= milli && milli <= 999) {
                     log.warn("DEBUG: Youngest millisecond '" + milli + "' of second '" + secondDir + "' found");
 
                     String path = secondDir.getAbsolutePath() + File.separator + millis[k] + File.separator + DATE_INDEX_ID_FILENAME;
                     log.warn("DEBUG: ID File: " + path);
                     BufferedReader br = new BufferedReader(new FileReader(new File(path)));
                     String revisionName = br.readLine();
                     br.close();
                     return getRevision(revisionName);
                 } else {
                     log.warn("Does not seem to be a millisecond '" + milli + "' and hence will be ignored.");
                 }
             } catch(NumberFormatException e) {
                 log.warn("Does not seem to be a millisecond '" + millis[k] + "' and hence will be ignored.");
             }
         }
         log.warn("No youngest millisecond found within second '" + secondDir + "'");
         return null;
     }
 
     /**
      * Get revision by month
      */
     private Revision getRevisionByMonth(File yearDir, Calendar cal) throws Exception {
         String[] months = yearDir.list(); // IMPORTANT: Make sure the order is ascending: 1, 2, ..., 12
         for (int k = months.length - 1; k >= 0; k--) {
             log.warn("DEBUG: Month: " + months[k]);
             //log.warn("DEBUG: Month: " + months[k] + " (" + cal + ")");
             try {
                 int month = new Integer(months[k]).intValue();
                 if (month <= cal.get(Calendar.MONTH) + 1) {
                     log.warn("DEBUG: Month matched: " + month);
                     Revision revision = getRevisionByDay(new File(yearDir, months[k]), cal);
                     if (revision != null) {
                         return revision;
                     } else {
                         if (month < cal.get(Calendar.MONTH) + 1) {
                             return getYoungestRevisionOfMonth(new File(yearDir, months[k]));
                         } else {
                             log.warn("Youngest does not make sense!");
                         }
 
 /*
                         if (k - 1 >= 0) {
                             log.warn("DEBUG: Try next month lower: " + months[k - 1]);
                             cal.set(Calendar.DAY_OF_MONTH, 31);
                             cal.set(Calendar.HOUR_OF_DAY, 23);
                             cal.set(Calendar.MINUTE, 59);
                             cal.set(Calendar.SECOND, 59);
                             cal.set(Calendar.MILLISECOND, 999);
                             return getRevisionByDay(new File(yearDir, months[k - 1]), cal);
                         } else {
                             log.warn("DEBUG: No other month available.");
                             return null;
                         }
 */
                     }
                 }
             } catch(NumberFormatException e) {
                 log.warn("Does not seem to be a month: " + months[k]);
             }
         }
         return null;
     }
 
     /**
      * Get revision by day
      */
     private Revision getRevisionByDay(File monthDir, Calendar cal) throws Exception {
         String[] days = monthDir.list(); // IMPORTANT: Make sure the order is ascending: 1, 2, ..., 31
         for (int k = days.length - 1; k >= 0; k--) {
             log.warn("DEBUG: Day: " + days[k]);
             try {
                 int day = new Integer(days[k]).intValue();
                 if (day <= cal.get(Calendar.DAY_OF_MONTH)) {
                     log.warn("DEBUG: Day matched: " + day);
                     Revision revision = getRevisionByHour(new File(monthDir, days[k]), cal);
                     if (revision != null) {
                         return revision;
                     } else {
                         if (day < cal.get(Calendar.DAY_OF_MONTH)) {
                             return getYoungestRevisionOfDay(new File(monthDir, days[k]));
                         } else {
                             log.warn("Youngest does not make sense!");
                         }
 
 
 /*
                         if (k - 1 >= 0) {
                             log.warn("DEBUG: Try next day lower: " + days[k - 1]);
                             cal.set(Calendar.HOUR_OF_DAY, 23);
                             cal.set(Calendar.MINUTE, 59);
                             cal.set(Calendar.SECOND, 59);
                             cal.set(Calendar.MILLISECOND, 999);
                             return getRevisionByHour(new File(monthDir, days[k - 1]), cal);
                         } else {
                             log.warn("DEBUG: No other day available.");
                             return null;
                         }
 */
                     }
                 }
             } catch(NumberFormatException e) {
                 log.warn("Does not seem to be a day: " + days[k]);
             }
         }
         return null;
     }
 
     /**
      * Get revision by hour 
      */
     private Revision getRevisionByHour(File dayDir, Calendar cal) throws Exception {
         String[] hours = dayDir.list(); // IMPORTANT: Make sure the order is ascending: 1, 2, 3, ...
         for (int k = hours.length - 1; k >= 0; k--) {
             log.warn("DEBUG: Hour: " + hours[k]);
             try {
                 int hour = Integer.parseInt(hours[k]);
                log.warn("DEBUG: Compare: " + hour + ", " + cal.get(Calendar.HOUR_OF_DAY));
                 if (hour <= cal.get(Calendar.HOUR_OF_DAY)) {
                     log.warn("DEBUG: Hour matched: " + hour);
                     Revision revision = getRevisionByMinute(new File(dayDir, hours[k]), cal);
                     if (revision != null) {
                         return revision;
                     } else {
                         if (hour < cal.get(Calendar.HOUR_OF_DAY)) {
                             return getYoungestRevisionOfHour(new File(dayDir, hours[k]));
                         } else {
                             log.warn("Youngest does not make sense!");
                         }
 
 /*
                         if (k - 1 >= 0) {
                             log.warn("DEBUG: Try next hour lower: " + hours[k - 1]);
                             cal.set(Calendar.MINUTE, 59);
                             cal.set(Calendar.SECOND, 59);
                             cal.set(Calendar.MILLISECOND, 999);
                             return getRevisionByMinute(new File(dayDir, hours[k - 1]), cal);
                         } else {
                             log.warn("DEBUG: No other hour available.");
                             return null;
                         }
 */
                     }
                 }
             } catch(NumberFormatException e) {
                 log.warn("Does not seem to be a hour: " + hours[k]);
             }
         }
         return null;
     }
 
     /**
      * Get revision by minute 
      */
     private Revision getRevisionByMinute(File hourDir, Calendar cal) throws Exception {
         String[] minutes = hourDir.list(); // IMPORTANT: Make sure the order is ascending: 1, 2, 3, ...
         for (int k = minutes.length - 1; k >= 0; k--) {
             log.warn("DEBUG: Minute: " + minutes[k]);
             try {
                 int minute = Integer.parseInt(minutes[k]);
                 if (minute <= cal.get(Calendar.MINUTE) ) {
                     log.warn("DEBUG: Minute matched: " + minute);
                     Revision revision = getRevisionBySecond(new File(hourDir, minutes[k]), cal);
                     if (revision != null) {
                         return revision;
                     } else {
                         if (minute < cal.get(Calendar.MINUTE)) {
                             return getYoungestRevisionOfMinute(new File(hourDir, minutes[k]));
                         } else {
                             log.warn("Youngest does not make sense!");
                         }
 
 /*
                         if (k - 1 >= 0) {
                             log.warn("DEBUG: Try next minute lower: " + minutes[k - 1]);
                             cal.set(Calendar.SECOND, 59);
                             cal.set(Calendar.MILLISECOND, 999);
                             return getRevisionBySecond(new File(hourDir, minutes[k - 1]), cal);
                         } else {
                             log.warn("DEBUG: No other minute available.");
                             return null;
                         }
 */
                     }
                 }
             } catch(NumberFormatException e) {
                 log.warn("Does not seem to be a minute: " + minutes[k]);
             }
         }
         return null;
     }
 
     /**
      * Get revision by second
      */
     private Revision getRevisionBySecond(File minuteDir, Calendar cal) throws Exception {
         String[] seconds = minuteDir.list(); // IMPORTANT: Make sure the order is ascending: 0, 1, 2, 3, ..., 60
         for (int k = seconds.length - 1; k >= 0; k--) {
             log.warn("DEBUG: Second: " + seconds[k]);
             try {
                 int second = Integer.parseInt(seconds[k]);
                 if (second <= cal.get(Calendar.SECOND) ) {
                     log.warn("DEBUG: Second matched: " + second);
                     Revision revision = getRevisionByMillisecond(new File(minuteDir, seconds[k]), cal);
                     if (revision != null) {
                         return revision;
                     } else {
                         if (second < cal.get(Calendar.SECOND)) {
                             return getYoungestRevisionOfSecond(new File(minuteDir, seconds[k]));
                         } else {
                             log.warn("Youngest does not make sense!");
                         }
 
 
 /*
                         if (k - 1 >= 0) {
                             log.warn("DEBUG: Try next second lower: " + seconds[k - 1]);
                             cal.set(Calendar.MILLISECOND, 999);
                             return getRevisionByMillisecond(new File(minuteDir, seconds[k - 1]), cal);
                         } else {
                             log.warn("DEBUG: No other second available.");
                             return null;
                         }
 */
                     }
                 }
             } catch(NumberFormatException e) {
                 log.warn("Does not seem to be a second: " + seconds[k]);
             }
         }
         return null;
     }
 
     /**
      * Get revision by millisecond
      */
     private Revision getRevisionByMillisecond(File secondDir, Calendar cal) throws Exception {
         String[] millis = secondDir.list(); // IMPORTANT: Make sure the order is ascending: 0, 1, 2, 3, ..., 999
         for (int k = millis.length - 1; k >= 0; k--) {
             log.warn("DEBUG: Millisecond: " + millis[k]);
             try {
                 int milli = new Integer(millis[k]).intValue();
                 if (milli <= cal.get(Calendar.MILLISECOND) ) {
                     log.warn("DEBUG: Millisecond matched: " + milli);
 
                     String path = secondDir.getAbsolutePath() + File.separator + millis[k] + File.separator + DATE_INDEX_ID_FILENAME;
                     log.warn("DEBUG: ID File: " + path);
                     BufferedReader br = new BufferedReader(new FileReader(new File(path)));
                     String revisionName = br.readLine();
                     br.close();
                     return getRevision(revisionName);
                 }
             } catch(NumberFormatException e) {
                 log.warn("Does not seem to be a millisecond: " + millis[k]);
             }
         }
         return null;
     }
 
     /**
      * Build date index in order to retrieve revisions more quickly based on creation date
      */
     private void buildDateIndex() throws Exception {
         File dateIndexBaseDir = new File(this.metaDir, DATE_INDEX_BASE_DIR);
         log.warn("DEBUG: Build date index: " + dateIndexBaseDir);
 
         if (!dateIndexBaseDir.isDirectory()) {
             dateIndexBaseDir.mkdirs();
         }
 
         Revision[] revisions = getRevisions();
         for (int i = revisions.length - 1; i >= 0; i--) {
             Date creationDate = new Date(Long.parseLong(revisions[i].getRevisionName())); // INFO: The name of a revision is based on System.currentTimeMillis() (see createRevision(String))
             log.warn("DEBUG: Creation date: " + creationDate);
 
             String dateDirS = new java.text.SimpleDateFormat("yyyy/MM/dd/HH/mm/ss/S").format(creationDate);
             log.warn("DEBUG: Date directory of revision '" + revisions[i].getRevisionName() + "': " + dateDirS);
             File dateDirF = new File(dateIndexBaseDir, dateDirS);
             if (!dateDirF.isDirectory()) {
                 dateDirF.mkdirs();
                 File revisionIdFile = new File(dateDirF, DATE_INDEX_ID_FILENAME);
                 PrintWriter pw = new PrintWriter(new FileOutputStream(revisionIdFile));
                 pw.print(revisions[i].getRevisionName());
                 pw.close();
             } else {
                log.error("Revision '" + revisions[i].getRevisionName() + "' seems to exists twice!");
             }
         }
     }
 
     /**
      * @see org.wyona.yarep.core.Node#getRevision(String)
      */
     public Revision getRevision(String revisionName) throws NoSuchRevisionException, RepositoryException {
         VirtualFileSystemRevision revision = new VirtualFileSystemRevision(this, revisionName);
         if (!revision.contentFile.exists()) {
             String logMessage = "Node '" + getPath() + "' has no such revision: " + revisionName;
             //log.error(logMessage);
             throw new NoSuchRevisionException(logMessage);
         }
         return revision;
     }
 
     public Revision getRevisionByTag(String tag) throws NoSuchRevisionException,
             RepositoryException {
         if (!areRevisionsRead) {
             readRevisions();
         }
         return super.getRevisionByTag(tag);
     }
 
     /**
      * @see org.wyona.yarep.core.Node#getRevisions()
      */
     public Revision[] getRevisions() throws RepositoryException {
         if (!areRevisionsRead) {
             readRevisions();
         }
         return super.getRevisions();
     }
 
     public boolean hasRevisionWithTag(String tag) throws RepositoryException {
         if (!areRevisionsRead) {
             readRevisions();
         }
         return super.hasRevisionWithTag(tag);
     }
 
     /**
      * Get revision content file
      */
     public File getRevisionContentFile(String revisionName) {
         File revisionsBaseDir = new File(this.metaDir, REVISIONS_BASE_DIR);
         File revisionDir = new File(revisionsBaseDir, revisionName);
             
         return new File(revisionDir, VirtualFileSystemRevision.CONTENT_FILE_NAME);
     }
 
     /**
      * Get revision meta file
      */
     public File getRevisionMetaFile(String revisionName) {
         File revisionsBaseDir = new File(this.metaDir, REVISIONS_BASE_DIR);
         File revisionDir = new File(revisionsBaseDir, revisionName);
             
         return new File(revisionDir, META_FILE_NAME);
     }
 
     /**
      * Get meta file
      */
     public File getMetaFile() {
         return metaFile;
     }
 }
