 /*
 * Copyright 2013, CMM, University of Queensland.
 *
 * This file is part of Paul.
 *
 * Paul is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Paul is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Paul. If not, see <http://www.gnu.org/licenses/>.
 */
 
 package au.edu.uq.cmm.paul.queue;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.Writer;
 import java.nio.file.Files;
 import java.nio.file.LinkOption;
 import java.nio.file.Path;
 
 import org.apache.commons.io.FilenameUtils;
 import org.slf4j.Logger;
 
 import au.edu.uq.cmm.paul.PaulConfiguration;
 import au.edu.uq.cmm.paul.PaulException;
 
 public abstract class AbstractQueueFileManager implements QueueFileManager {
 
     protected static final int RETRY = 10;
     protected final File archiveDirectory;
     protected final File captureDirectory;
     private Logger log;
 
     public AbstractQueueFileManager(PaulConfiguration config, Logger log) {
         this.log = log;
         this.archiveDirectory = new File(config.getArchiveDirectory());
         checkDirectory(this.archiveDirectory, "archive");
         this.captureDirectory = new File(config.getCaptureDirectory());
         checkDirectory(this.captureDirectory, "capture");
     }
 
     private void checkDirectory(File dir, String tag) {
         File testFile = new File(dir, "test.txt");
         try (OutputStream os = new FileOutputStream(testFile)) {
             os.write("1 2 3\n".getBytes());
         } catch (IOException ex) {
             throw new PaulException("Problem creating file in " + 
                     tag + " directory", ex);
         } finally {
             testFile.delete();
         }
     }
 
     protected File copyFile(File source, File target, String area)
             throws QueueFileException {
         // TODO - if the time taken to copy files is a problem, we could 
         // potentially improve this by using NIO or memory mapped files.
         long size = source.length();
         try (FileInputStream is = new FileInputStream(source);
                 FileOutputStream os = new FileOutputStream(target)) {
             byte[] buffer = new byte[(int) Math.min(size, 8192)];
             int nosRead;
             long totalRead = 0;
             while ((nosRead = is.read(buffer, 0, buffer.length)) > 0) {
                 os.write(buffer, 0, nosRead);
                 totalRead += nosRead;
             }
 
             // If these happen there is something wrong with our copying, locking
             // and / or file settling heuristics.
             if (totalRead != size) {
                 log.error("Copied file size discrepancy - initial file size was " + size +
                         "bytes but we copied " + totalRead + " bytes");
             } else if (size != source.length()) {
                 log.error("File size changed during copy - initial file size was " + size +
                         "bytes and current size is " +  source.length());
             }
             log.info("Copied " + totalRead + " bytes from " + source + " to " + target);
             return target;
         } catch (IOException ex) {
             throw new QueueFileException("Problem while copying file to " + area + " area", ex);
         }
     }
 
     @Override
     public final void enqueueFile(String contents, File target, boolean mayExist)
             throws QueueFileException {
         if (!mayExist && target.exists()) {
             throw new QueueFileException("File " + target + " already exists");
         }
         try (Writer w = new FileWriter(target)) {
             w.write(contents);
             w.close();
         } catch (IOException ex) {
             throw new QueueFileException("Problem while saving to a queue file", ex);
         }
     }
 
     @Override
     public final boolean isQueuedFile(File file) {
         return file.getParentFile().equals(captureDirectory);
     }
 
     @Override
     public final boolean isArchivedFile(File file) {
         return file.getParentFile().equals(archiveDirectory);
     }
 
     @Override
     public final File generateUniqueFile(String suffix, boolean regrabbing)
             throws QueueFileException {
         String template = regrabbing ? "regrabbed-%d-%d-%d%s" : "file-%d-%d-%d%s";
         long threadId = Thread.currentThread().getId();
         for (int i = 0; i < RETRY; i++) {
             long now = System.currentTimeMillis();
             String name = String.format(template, now, threadId, i, suffix);
             File file = new File(captureDirectory, name);
             if (!file.exists()) {
                 return file;
             }
         }
         throw new QueueFileException(
                 RETRY + " attempts to generate a unique filename failed!");
     }
 
     @Override
     public final File archiveFile(File file) throws QueueFileException {
         if (!Files.exists(file.toPath(), LinkOption.NOFOLLOW_LINKS)) {
             throw new QueueFileException("File or symlink " + file + " no longer exists");
         }
         if (!Files.exists(file.toPath())) {
             throw new QueueFileException("Symlink target for " + file + " no longer exists");
         }
         if (!isQueuedFile(file)) {
             throw new QueueFileException("File or symlink" + file + " is not in the queue");
         }
         File dest = new File(archiveDirectory, file.getName());
         if (dest.exists()) {
             throw new QueueFileException("Archived file " + dest + " already exists");
         }
     
         if (Files.isSymbolicLink(file.toPath())) {
             dest = copyFile(file, dest, "archive");
             try {
                 Files.delete(file.toPath());
             } catch (IOException ex) {
                 throw new QueueFileException("Could not remove symlink " + file);
             }
         } else {
             if (!file.renameTo(dest)) {
                 throw new QueueFileException("File " + file + " could not be renamed to " + dest);
             }
         }
         log.info("File " + file + " archived as " + dest);
         return dest;
     }
 
     @Override
     public void removeFile(File file) throws QueueFileException {
         if (!Files.exists(file.toPath(), LinkOption.NOFOLLOW_LINKS)) {
             throw new QueueFileException("File or symlink " + file + " no longer exists");
         }
         if (!isQueuedFile(file)) {
             throw new QueueFileException("File or symlink " + file + " is not in the queue");
         }
         try {
             Files.delete(file.toPath());
             log.info("File " + file + " deleted from queue area");
         } catch (IOException ex) {
             throw new QueueFileException("File or symlink " + file + " could not be deleted from queue area", ex);
         }
     }
 
     @Override
     public final boolean isCopiedFile(File file) throws QueueFileException {
         Path target = file.toPath();
         if (Files.isSymbolicLink(file.toPath())) {
             return false;
         } else if (!Files.exists(target)) {
             log.info("File " + file + " does not exist");
             return false;
         } else {
             return target.getParent().toFile().equals(captureDirectory) ||
                     target.getParent().toFile().equals(archiveDirectory);
         }
     }
 
     @Override
     public final File renameGrabbedDatafile(File file) throws QueueFileException {
         String extension = FilenameUtils.getExtension(file.toString());
         if (!extension.isEmpty()) {
             extension = "." + extension;
         }
         for (int i = 0; i < RETRY; i++) {
             File newFile = generateUniqueFile(extension, false);
             if (!file.renameTo(newFile)) {
                 if (!Files.exists(newFile.toPath(), LinkOption.NOFOLLOW_LINKS)) {
                     throw new QueueFileException(
                             "Unable to rename file or symlink " + file + " to " + newFile);
                 }
             } else {
                 return newFile;
             }
         }
         throw new QueueFileException(RETRY + " attempts to rename file / symlink failed!");
     }
 
 }
