 /*
  * This file is part of muCommander, http://www.mucommander.com
  * Copyright (C) 2002-2009 Maxence Bernard
  *
  * muCommander is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * muCommander is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.mucommander.job;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import com.mucommander.file.AbstractFile;
 import com.mucommander.file.DummyFile;
 import com.mucommander.file.FilePermissions;
 import com.mucommander.file.FileURL;
 import com.mucommander.file.util.FileSet;
 import com.mucommander.io.BufferPool;
 import com.mucommander.io.ChecksumInputStream;
 import com.mucommander.io.StreamUtils;
 import com.mucommander.text.Translator;
 import com.mucommander.ui.dialog.file.FileCollisionDialog;
 import com.mucommander.ui.dialog.file.ProgressDialog;
 import com.mucommander.ui.main.MainFrame;
 
 /**
  * This job split the file into parts with given size.
  * @author Mariusz Jakubowski
  */
 public class SplitFileJob extends AbstractCopyJob {
 
     private int partSize;
 	private AbstractFile sourceFile;
 	private InputStream origFileStream;
 	private AbstractFile destFolder;
 	private long sizeLeft;
		
 
 	/**
 	 * A class for holding file name and size of one part.
 	 * @author Mariusz Jakubowski
 	 *
 	 */
 	private static class DummyDestFile extends DummyFile {
 		private long size;
 		
 		public DummyDestFile(FileURL url, long size) {
 			super(url);
 			this.size = size;
 		}
 		
 		public long getSize() {
 			return size;
 		}
 	}
 
 	
 	public SplitFileJob(ProgressDialog progressDialog, MainFrame mainFrame, 
 			AbstractFile file, AbstractFile destFolder, int partSize, int parts) {
         super(progressDialog, mainFrame, new FileSet(), destFolder, null, FileCollisionDialog.ASK_ACTION);
         this.partSize = partSize;
         this.nbFiles = parts;
         this.sourceFile = file;
         this.destFolder = destFolder;
        this.errorDialogTitle = Translator.get("split_file_dialog.error_title");
         createInputStream();
         sizeLeft = sourceFile.getSize();
         for (int i=1; i<=parts; i++) {
         	addDummyFile(i, Math.min(partSize, sizeLeft));
         	sizeLeft -= partSize;
         }
         sizeLeft = sourceFile.getSize();
     }
 
 	/**
 	 * Adds a dummy output file (used in progress monitoring). 
 	 * @param i index of a file
 	 * @param size size of a file
 	 */
 	private void addDummyFile(int i, long size) {
     	String num;
     	if (i<10) {
     		num = "00" + Integer.toString(i); 
     	} else if (i<100) {
     		num = "0" + Integer.toString(i); 
     	} else {
     		num = Integer.toString(i); 
     	}
         FileURL childURL = (FileURL)destFolder.getURL().clone();
         childURL.setPath(destFolder.addTrailingSeparator(childURL.getPath()) + sourceFile.getName() + "." + num);
     	DummyDestFile fileHolder = new DummyDestFile(childURL, size);
     	files.add(fileHolder);
 	}
 	
 	
 	protected void jobStarted() {
 		super.jobStarted();
 		createInputStream();
 	}
 
 	/**
 	 * Creates an input stream from the file. 
 	 */
 	private void createInputStream() {
         try {
 			origFileStream = sourceFile.getInputStream();
 		} catch (IOException e) {
 			e.printStackTrace();
             showErrorDialog(errorDialogTitle,
                     Translator.get("error_while_transferring", sourceFile.getName()),
                     new String[]{CANCEL_TEXT},
                     new int[]{CANCEL_ACTION}
                     );
             setState(INTERRUPTED);
             return;
 		}
         origFileStream = setCurrentInputStream(origFileStream);
         // init checksum calculation
         if (isIntegrityCheckEnabled()) {
 			try {
 				origFileStream = new ChecksumInputStream(origFileStream, MessageDigest.getInstance("CRC32"));
 			} catch (NoSuchAlgorithmException e) {
 				setIntegrityCheckEnabled(false);
 				e.printStackTrace();
 			}
         }
 	}
 	
 
     protected boolean processFile(AbstractFile file, Object recurseParams) {
         if(getState()==INTERRUPTED)
             return false;
         
         // Create destination AbstractFile instance
         AbstractFile destFile = createDestinationFile(baseDestFolder, file.getName());
         if (destFile == null)
             return false;
 
         destFile = checkForCollision(sourceFile, baseDestFolder, destFile, false);
         if (destFile == null)
             return false;
         
         OutputStream out = null;
         try {
 			out = destFile.getOutputStream(false);
 			
 			long written = StreamUtils.copyStream(origFileStream, out, BufferPool.getDefaultBufferSize(), partSize);
 			
 	        // Preserve source file's date
 	        destFile.changeDate(sourceFile.getDate());
 
 	        // Preserve source file's permissions: preserve only the permissions bits that are supported by the source file
 	        // and use default permissions for the rest of them.
 	        destFile.importPermissions(sourceFile, FilePermissions.DEFAULT_FILE_PERMISSIONS);  // use #importPermissions(AbstractFile, int) to avoid isDirectory test
 			
 			if (written < 0) {
 				// out of disk space - ask a user for a new disk
 				out.close();
 				out = null;
 				sizeLeft += written;
 				showErrorDialog(Translator.get("split_file_dialog.title"), 
 						Translator.get("split_file_dialog.insert_new_media"), 
 						new String[]{OK_TEXT, CANCEL_TEXT}, 
 						new int[]{OK_ACTION, CANCEL_ACTION});
 				if (getState()==INTERRUPTED) {
 					return false;
 				}
 				// create new output file if necessary
 				if ((sizeLeft>0) && (getCurrentFileIndex() == getNbFiles()-1)) {
 					this.nbFiles++;
 					addDummyFile(this.nbFiles, sizeLeft);
 				}
 			} else {
 				sizeLeft -= written;
 			}
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 			
             showErrorDialog(errorDialogTitle,
                     Translator.get("error_while_transferring", destFile.getName()),
                     new String[]{CANCEL_TEXT},
                     new int[]{CANCEL_ACTION}
                     );
 			return false;
 			
 		} finally {
             try {
             	if (out!=null)
             		out.close();
             }
             catch(IOException e2) {
             }
 		}
 		
     	return true;
     }
 
 
     // This job modifies baseDestFolder and its subfolders
     protected boolean hasFolderChanged(AbstractFile folder) {
         return baseDestFolder.isParentOf(folder);
     }
     
     protected void jobCompleted() {
     	// create checksum file
     	if (isIntegrityCheckEnabled()) {
             if(origFileStream!=null && (origFileStream instanceof ChecksumInputStream)) {
             	String crcFileName = sourceFile.getName() + ".sfv"; 
                 String sourceChecksum = ((ChecksumInputStream)origFileStream).getChecksumString();
                 try {
 					AbstractFile crcFile = baseDestFolder.getDirectChild(crcFileName);
 					OutputStream crcStream = crcFile.getOutputStream(false);
 					String line = sourceFile.getName() + " " + sourceChecksum;
 					crcStream.write(line.getBytes("utf-8"));
 					crcStream.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 		            showErrorDialog(errorDialogTitle,
 		                    Translator.get("error_while_transferring", crcFileName),
 		                    new String[]{CANCEL_TEXT},
 		                    new int[]{CANCEL_ACTION}
 		                    );
 				}
             }
     	}
     	super.jobCompleted();
     }
 
 
 }
