 package no.feide.moria.directory;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.util.TimerTask;
 
 import no.feide.moria.directory.index.DirectoryManagerIndex;
 import no.feide.moria.log.MessageLogger;
 
 /**
  * This is the task responsible for periodically checking for a new index file,
  * and, if necessary update the existing index.
  */
 public class IndexUpdater
 extends TimerTask {
 
     /** The message logger. */
     private final static MessageLogger log = new MessageLogger(IndexUpdater.class);
 
     /** The location of the index file. */
     private final String filename;
 
     /** The timestamp of the last read index file. Initially set to 0 (zero). */
     private long timestamp = 0;
 
     /** The instance of Directory Manager that created this task. */
     private final DirectoryManager owner;
 
 
     /**
      * Constructor.
      * @param dm
      *            The instance of Directory Manager that created this instance.
      *            Required since <code>IndexUpdater</code> uses this object
      *            directly to update its index. Cannot be <code>null</code>.
      * @param indexFilename
      *            The index filename. Cannot be <code>null</code>.
      */
     public IndexUpdater(DirectoryManager dm, final String indexFilename) {
 
         super();
 
         // Sanity checks.
         if (dm == null)
             throw new IllegalArgumentException("Directory Manager cannot be NULL");
         if (indexFilename == null)
             throw new IllegalArgumentException("Index file name cannot be NULL");
 
         // Set some local variables.
         owner = dm;
         filename = indexFilename;
 
     }
 
 
     /**
      * Performs the periodic update of the DirectoryManager's index, by calling
      * the <code>DirectoryManager.updateIndex(DirectoryManagerIndex)</code>
      * method.
      * @see java.lang.Runnable#run()
      * @see DirectoryManager#updateIndex(DirectoryManagerIndex)
      */
     public void run() {
 
         owner.updateIndex(readIndex());
 
     }
 
 
     /**
      * Reads an index file and create a new index object. <br>
      * <br>
      * Note that this method is also called by
      * <code>DirectoryManager.setConfig(Properties)</code> to force through an
      * initial update of the index.
      * @param filename
      *            The filename of the index. Cannot be <code>null</code>.
      * @return The newly read index, as an object implementing the
      *         <code>DirectoryManagerIndex</code> interface. Will return
      *         <code>null</code> if this method has already been used to
      *         successfully read an index file, and the file has not been
      *         updated since (based on the file's timestamp on disk, as per the
      *         <code>File.lastModified()</code> method).
      * @throws IllegalArgumentException
      *             If <code>filename</code> is <code>null</code>.
      * @throws DirectoryManagerConfigurationException
      *             If the index file does not exist, or if unable to read from
      *             the file, or if unable to instantiate the index as a
      *             <code>DirectoryManagerIndex</code> object.
     * @see java.io.File#lastModified()
      * @see DirectoryManager#setConfig(Properties)
      */
     protected DirectoryManagerIndex readIndex() {
 
         // Sanity check.
         if (filename == null)
             throw new IllegalArgumentException("Index filename cannot be NULL");
 
         // Check if the index file exists.
         File indexFile = new File(filename);
         if (!indexFile.isFile())
             throw new DirectoryManagerConfigurationException("Index file " + filename + " does not exist");
 
         // Check if we have received a newer index file than the one previously
         // read.
         if (timestamp >= indexFile.lastModified())
             return null; // No update necessary.
         timestamp = indexFile.lastModified();
 
         DirectoryManagerIndex index = null;
         try {
 
             // Read the new index from file.
             ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
             index = (DirectoryManagerIndex) in.readObject();
             in.close();
 
         } catch (FileNotFoundException e) {
             throw new DirectoryManagerConfigurationException("Index file " + filename + " does not exist");
         } catch (IOException e) {
             throw new DirectoryManagerConfigurationException("Unable to read index from file " + filename, e);
         } catch (ClassNotFoundException e) {
             throw new DirectoryManagerConfigurationException("Unable to instantiate index object", e);
         }
 
         timestamp = indexFile.lastModified();
         return index;
 
     }
 
 }
