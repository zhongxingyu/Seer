 /*
  * Copyright (c) 2004 UNINETT FAS
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option)
  * any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place - Suite 330, Boston, MA 02111-1307, USA.
  *
  */
 
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
  * and if necessary, update the existing index. <br>
  * <br>
  * Note that this implementation relies on
  * <code>no.feide.moria.directory.index.SerializableIndex</code> as the index
  * implementation. To change the index implementation, the method
  * <code>readIndex()</code> needs to be modified.
  */
 public class IndexUpdater
 extends TimerTask {
 
     /** Used for logging. */
     private final MessageLogger log = new MessageLogger(IndexUpdater.class);
 
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
      * @throws NullPointerException
      *             If <code>dm</code> or <code>indexFilename</code> is
      *             <code>null</code>.
      */
     public IndexUpdater(final DirectoryManager dm, final String indexFilename) {
 
         super();
 
         // Sanity checks.
         if (dm == null)
             throw new NullPointerException("Directory Manager cannot be NULL");
         if (indexFilename == null)
             throw new NullPointerException("Index file name cannot be NULL");
 
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
     public final void run() {
 
         owner.updateIndex(readIndex());
 
     }
 
 
     /**
      * Reads an index file and create a new index object. <br>
      * <br>
      * Note that this method is also called by
      * <code>DirectoryManager.setConfig(Properties)</code> to force through an
      * initial update of the index.
      * @return The newly read index, as an object implementing the
      *         <code>DirectoryManagerIndex</code> interface. Will return
      *         <code>null</code> if this method has already been used to
      *         successfully read an index file, and the file has not been
      *         updated since (based on the file's timestamp on disk, as per the
      *         <code>File.lastModified()</code> method).
      * @see java.io.File#lastModified()
     * @see DirectoryManager#setConfig(Properties)
      */
     protected final DirectoryManagerIndex readIndex() {
 
         // Check if the index file exists.
         File indexFile = new File(filename);
         if (!indexFile.isFile())
             log.logInfo("Index file '" + filename + "' does not exist");
 
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
             // Unlikely we'll ever get here, but we have to catch the exception.
             log.logInfo("Index file '" + filename + "' does not exist");
         } catch (IOException e) {
             log.logInfo("Unable to read index from file '" + filename + "'", e);
         } catch (ClassNotFoundException e) {
             log.logInfo("Unable to instantiate index object", e);
         }
 
         timestamp = indexFile.lastModified();
         log.logInfo("Index has been updated");
         return index;
 
     }
 
 }
