 // $Id$
 package org.yajul.io;
 
 import org.apache.log4j.Logger;
 import org.yajul.util.StringUtil;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.TimeZone;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 /**
  * Javabean that can store objects in the filesystem.  The objects are stored in a hierarchy of directories
  * corresponding to the date value passed in.  Callers can persist the file name passed back from storeObject()
  * so that the objects can easily be retrieved without knowing the date, etc.
  * When an object or document is stored with the same filename more than once, the archiver
  * will make a backup copy so the data is not lost.  If this is not desired, set the overwrite property
  * to true.
  * <ul>
  * <li>Use storeObject() / retrieveObject() to store and retrieve serialized Java objects.</li>
  * <li>To store XML documents, XMLDocumentArchiver.</li>
  * <li>For more control over what is stored, use getSource() / getSink() which provides the generated file names
  * and input / output streams.</li>
  * </ul>
  * See the setter / getter method javadoc for a description of the properties and the default values.
  * <br>
  * An example of a Spring initializer is listed here:
  * <pre>
  * &lt;bean id="documentArchiver" class="org.yajul.io.DocumentArchiver"
  *    init-method="init">
  *    &lt;property name="storeageDirectoryName">&lt;value>/archive/documents&lt;/value>&lt;/property>
  *    &lt;property name="retrieveDirectories">
  *        &lt;list>
  *            &lt;value>/archive/old/documents&lt;/value>
  *        &lt;/list>
  *    &lt;/property>
  *    &lt;property name="extension">&lt;value>.dat.gz&lt;/value>&lt;/property>
  * &lt;/bean>
  * </pre>
  * User: jdavis<br>
  * Date: Mar 5, 2004<br>
  * Time: 6:07:28 PM<br>
  * @author josh May 6, 2004 11:41:01 PM
  */
 public class DocumentArchiver
 {
     private static Logger log = Logger.getLogger(DocumentArchiver.class.getName());
 
     public static final String DEFAULT_EXTENSION = ".dat.gz";
 
     private File storeageDirectory;
     private String extension = DEFAULT_EXTENSION;
     private List retrieveDirectories;
     private boolean gzip = true;
     private boolean buffered = true;
     private boolean overwrite = false;
 
     /**
      * Returns the storage directory.
      * @return the storage directory.
      */
     public File getStoreageDirectory()
     {
         return storeageDirectory;
     }
 
     /**
      * Sets the directory where documents will be stored.  This will also be the <i>first</i> directory where
      * documents are retrieved from. No default.
      * @param storeageDirectory The document storeage directory.
      */
     public void setStoreageDirectory(File storeageDirectory)
     {
         if (storeageDirectory == null)
             throw new IllegalArgumentException("Storeage directory cannot be null!");
         if (storeageDirectory.exists() && !storeageDirectory.isDirectory())
             throw new IllegalArgumentException(storeageDirectory.toString() + " is not a directory!");
         this.storeageDirectory = storeageDirectory;
     }
 
     /**
      * Returns the filename extension that will be used for the stored documents.
      * @return the filename extension that will be used for the stored documents.
      */
     public String getExtension()
     {
         return extension;
     }
 
     /**
      * Sets the filename extension used for stored documents.
      * @param extension The filename extension, defaults to '.dat.gz'.
      */
     public void setExtension(String extension)
     {
         if (extension == null)
             throw new IllegalArgumentException("Extension cannot be null!");
         this.extension = extension;
     }
 
     /**
      * Returns the list of retrieval directories.   Documents not found in the storeage directory will
      * be searched along this path of directories.
      * @return the list of retrieval directories
      */
     public List getRetrieveDirectories()
     {
         return retrieveDirectories;
     }
 
     /**
      * Returns the number of retrieval directories.
      * @return the number of retrieval directories.
      */
     public int getRetrieveDirectoryCount()
     {
         return (retrieveDirectories == null) ? 0 : retrieveDirectories.size();
     }
 
     /**
      * Sets the retrieval directories, which will be scanned in order if a document is not found in the
      * storeage directory.
      *
      * @param retrieveDirectories A list of directory names.
      */
     public void setRetrieveDirectories(List retrieveDirectories)
     {
         this.retrieveDirectories = retrieveDirectories;
     }
 
     /**
      * Returns true if documents are being GZIP compressed.
      * @return true if documents are being GZIP compressed.
      */
     public boolean isGzip()
     {
         return gzip;
     }
 
     /**
      * Enbales/disables GZIP compression.
      * @param gzip True for gzip compression, false for uncompressed.
      */
     public void setGzip(boolean gzip)
     {
         this.gzip = gzip;
     }
 
     /**
      * Returns true if the streams will be buffered.
      * @return true if the streams will be buffered.
      */
     public boolean isBuffered()
     {
         return buffered;
     }
 
     /**
      * Enables/disables stream buffering, the default is 'true'.  It is recommended that
      * this value be set to true (the default) as performance can degrade rapidly if
      * no buffering is used.
      * @param buffered If true, streams will be buffered.
      */
     public void setBuffered(boolean buffered)
     {
         this.buffered = buffered;
     }
 
     /**
      * Initializes the bean.
      * @throws IOException if something goes wrong.
      */
     public void init() throws IOException
     {
         log.info("init() : ENTER");
         try
         {
             if (storeageDirectory == null)
                 throw new IllegalStateException("Storeage directory has not been set!");
             if (storeageDirectory.exists())
             {
                 if (!storeageDirectory.isDirectory())
                     throw new IOException("'" + storeageDirectory + "' is not a directory.");
             }
             else
             {
                 storeageDirectory.mkdirs();
             }
         }
         catch (IOException e)
         {
             log.error(e, e);
             throw e;
         }
         finally
         {
             log.info("init() : LEAVE");
         }
     }
 
     /**
      * Stores an object, given the id, date and sub-directory.
      *
      * @param subDirectory The sub-directory of the storeage directory where documents of this type are stored.
      * @param id           The id object that will be used to generate the file name.
      * @param date         The date, which will be used to generate the directory name.
      * @param object       The object that will be stored.
      * @return The name of the file that was used to store the object.
      * @throws IOException if something goes wrong.
      */
     public String storeObject(String subDirectory, Object id, Date date, Object object) throws IOException
     {
         if (log.isDebugEnabled())
             log.debug("storeObject() : ENTER");
         try
         {
             Sink docOut = getSink(subDirectory, id, date);
             ObjectOutputStream oos = new ObjectOutputStream(docOut.getStream());
             oos.writeObject(object);
             oos.flush();
             oos.close();
             if (log.isDebugEnabled())
                 log.debug("storeObject() : Object sucessfully stored.");
             return docOut.getFilename();    // Return the relative file name.
         }
         catch (IOException e)
         {
             log.error(e, e);
             throw e;
         }
         finally
         {
             if (log.isDebugEnabled())
                 log.debug("storeObject() : LEAVE");
         }
     }
 
     /**
      * Retrieves an object given the sub-directory, the id, and the date.
      * @param subDirectory The sub-directory.
      * @param id           The object id.
      * @param date         The date.
      * @return The object.
      * @throws IOException if something goes wrong.
      */
     public Object retrieveObject(String subDirectory, Object id, Date date) throws IOException
     {
         return retrieveObject(subDirectory, generateFileName(id, date));
     }
 
     /**
      * Retrieves an object given the sub-directory and the file name.
      *
      * @param subDirectory The sub-directory.
      * @param fileName     The name of the file, as returned by the storeObject() method.
      * @return The object.
      * @throws IOException if something goes wrong.
      */
     public Object retrieveObject(String subDirectory, String fileName) throws IOException
     {
         if (log.isDebugEnabled())
             log.debug("retrieveObject() : ENTER");
         try
         {
             Source source = getSource(subDirectory, fileName);
             if (log.isDebugEnabled())
                 log.debug("retrieveObject() : " + source.getFilename());
             ObjectInputStream ois = new ObjectInputStream(source.getStream());
             Object o = ois.readObject();
             if (log.isDebugEnabled())
                 log.debug("retrieveObject() : Object sucessfully retrieved.");
             return o;
         } // try
         catch (FileNotFoundException e)
         {
             throw e;
         }
         catch (IOException e)
         {
             log.error(e, e);
             throw e;
         }
         catch (ClassNotFoundException e)
         {
             log.error(e, e);
             throw new IOException("Class not found! " + e.getMessage());
         }
         finally
         {
             if (log.isDebugEnabled())
                 log.debug("retrieveObject() : LEAVE");
         }
     }
 
     /**
      * Returns the Sink (information for writing a document) for a given id, date and sub-directory.
      * @param subDirectory The sub-directory of the storeage directory where documents of this type are stored.
      * @param id           The id object that will be used to generate the file name.
      * @param date         The date, which will be used to generate the diretory name.
      * @return the Sink (information for writing a document) for a given id, date and sub-directory.
      * @throws IOException if something goes wrong.
      */
     public Sink getSink(String subDirectory, Object id, Date date)
             throws IOException
     {
         if (storeageDirectory == null)
             throw new IOException("Storeage directory cannot be null!  (Did you forget to invoke setStoreageDirectory()?)");
         String fileName = generateFileName(id, date);
         // If a storeage sub-directory was specified, use it.
         File dir = getSubDirectory(storeageDirectory, subDirectory);
         File path = new File(dir, fileName);
         String pathname = path.getAbsolutePath();
         File f = new File(pathname);
         if (f.exists())
         {
             if (!overwrite)
             {
                 // This might need to be optimized a bit.  We could enumerate the directory
                 // to find a good filename for the backup.
                 int i = 1;
                 File backup = new File(pathname + "." + i);
                 while (backup.exists())
                 {
                     i++;
                     backup = new File(pathname + "." + i);
                 }
                log.info("getSink() : Renaming existing file to " + backup.getAbsolutePath());
                 f.renameTo(backup);
                 f = new File(pathname);
             }
             else
             {
                log.info("getSink() : deleting " + f.getAbsolutePath());
                 f.delete();
                 f = new File(pathname);
             }
         }
        log.info("getSink() : " + f.getAbsolutePath());
         OutputStream os = getOutputStream(f);
         Sink docOut = new Sink(fileName, os);
         return docOut;
     }
 
     /**
      * Generates a file name from an object id and a date and file extension.
      * @param id   The object id, used to generate the unique file name.
      * @param date The date, used to create the directory path.
      * @return The filename.
      */
     public String generateFileName(Object id, Date date)
     {
         // Generate the file name.
         String fileName = id.toString();
         fileName = fileName.replace('/', '-');
         fileName = fileName.replace(' ', '_');
         fileName = fileName.replace('\t', '_');
         fileName = fileName.replace('\r', '_');
         fileName = fileName.replace('\n', '_');
         fileName = fileName + this.extension;
         // Get the month and year as a string, with the file separator in the middle.
         SimpleDateFormat df = new SimpleDateFormat(File.separator +
                 "yyyy" +
                 File.separator +
                 "yyyy_MM" +
                 File.separator +
                 "yyyy_MM_dd" +
                 File.separator);
         df.setTimeZone(TimeZone.getTimeZone("GMT"));
         fileName = df.format(date) + fileName;
         return fileName;
     }
 
     /**
      * Returns a source (filename and input stream) given the sub-directory, the id, and the date.
      * @param subDirectory The sub-directory.
      * @param id           The object id.
      * @param date         The date.
      * @return The object.
      * @throws IOException if something goes wrong.
      */
     public Source getSource(String subDirectory, Object id, Date date) throws IOException
     {
         return getSource(subDirectory, generateFileName(id, date));
     }
 
     /**
      * Retrieves an source given the sub-directory and the file name.
      * @param subDirectory The sub-directory.
      * @param fileName     The name of the file, as returned by the storeObject() method.
      * @return The source : an input stream and a file name.
      * @throws IOException if something goes wrong.
      */
     public Source getSource(String subDirectory, String fileName) throws IOException
     {
         if (storeageDirectory == null)
             throw new IOException("Storeage directory cannot be null!  (Did you forget to invoke setStoreageDirectory()?)");
 
         // If a storeage sub-directory was specified, use it.
         File dir = getSubDirectory(storeageDirectory, subDirectory);
         File f = new File(dir, fileName);
 
         // If the file doesn't exist in the primary storeage directory, then
         // check the other directories.
         if (!f.exists())
         {
             if (log.isDebugEnabled())
                 log.debug("getSource() : " + f.getAbsolutePath() + " doesn't exist.");
 
             if (retrieveDirectories == null || getRetrieveDirectoryCount() == 0)
                 throw new FileNotFoundException("Unable to find " + fileName + " in the storeage directory.");
 
             for (Iterator iterator = retrieveDirectories.iterator(); iterator.hasNext();)
             {
                 String baseString = (String) iterator.next();
                 File base = new File(baseString);
                 if (!base.exists())
                 {
                     if (log.isDebugEnabled())
                         log.debug("getSource() : directory " + base + " does not exist, skipping.");
                     continue;
                 }
                 dir = getSubDirectory(base, subDirectory);
                 f = new File(dir, fileName);
                 if (f.exists())
                     return getSource(f);   // Return the source.
             } // for
             throw new FileNotFoundException("Unable to find " + fileName + " in the any directories ("
                     + (getRetrieveDirectoryCount() + 1) + " directories searched).");
         } // if !f.exists()
         else
             return getSource(f);   // Return the object.
     }
 
     /**
      * Returns a source for the given file.
      * @param f the file
      * @return a source for the file
      * @throws IOException if something goes wrong
      */
     private Source getSource(File f) throws IOException
     {
         return new Source(f.getAbsolutePath(), getInputStream(f));
     }
 
     /**
      * Returns a sub directory of the base directory, if a sub-directory
      * was specified.
      * @param subDirectory The sub-directory (optional.
      * @return a sub directory of the base directory, if a sub-directory
      * was specified.
      */
     private File getSubDirectory(File base, String subDirectory)
     {
         return (StringUtil.isEmpty(subDirectory)) ? base : new File(base, subDirectory);
     }
 
     /**
      * Returns an output stream for the file.
      * @param f the file
      * @return an output stream for the file.
      * @throws IOException if something goes wrong
      */
     private OutputStream getOutputStream(File f)
             throws IOException
     {
         f.getParentFile().mkdirs();
        OutputStream os = new FileOutputStream(f);
         if (buffered)
             os = new BufferedOutputStream(os);
         if (gzip)
             os = new GZIPOutputStream(os);
         return os;
     }
 
     /**
      * Returns an input stream for the file.
      * @param f the file
      * @return an input stream for the file
      * @throws IOException if something goes wrong.
      */
     private InputStream getInputStream(File f)
             throws IOException
     {
         InputStream is = new FileInputStream(f);
         if (buffered)
             is = new BufferedInputStream(is);
         if (gzip)
             is = new GZIPInputStream(is);
         return is;
     }
 
     /**
      * Provides the filename and output stream for a given document.
      */
     public static class Sink
     {
         private String filename;
         private OutputStream stream;
 
         /**
          * Creates a sink.
          * @param filename The filename that the output stream is pointing to.
          * @param out      The output stream.
          */
         Sink(String filename, OutputStream out)
         {
             this.filename = filename;
             this.stream = out;
         }
 
         /**
          * Returns the name of the file that the output stream will write to.
          * @return the name of the file that the output stream will write to.
          */
         public String getFilename()
         {
             return filename;
         }
 
         /**
          * Returns the output stream, which will write to a file.
          * @return the output stream, which will write to a file.
          */
         public OutputStream getStream()
         {
             return stream;
         }
     } // class Sink
 
 
     /**
      * Provides the filename and input stream for a given document.
      */
     public static class Source
     {
         private String filename;
         private InputStream stream;
 
         /**
          * Creates a source.
          * @param filename The filename that the input stream is pointing to.
          * @param in       The input stream.
          */
         Source(String filename, InputStream in)
         {
             this.filename = filename;
             this.stream = in;
         }
 
         /**
          * Returns the name of the file that the input stream will read from.
          * @return the name of the file that the input stream will read from.
          */
         public String getFilename()
         {
             return filename;
         }
 
         /**
          * Returns the input stream, which will read from a file.
          * @return the input stream, which will read from a file.
          */
         public InputStream getStream()
         {
             return stream;
         }
     } // class Source
 }
