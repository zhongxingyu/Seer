 /*
  * Copyright (c) 2013, Sam Malone. All rights reserved.
  * 
  * Redistribution and use of this software in source and binary forms, with or
  * without modification, are permitted provided that the following conditions
  * are met:
  * 
  *  - Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  *  - Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  *  - Neither the name of Sam Malone nor the names of its contributors may be
  *    used to endorse or promote products derived from this software without
  *    specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package swapf;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Sam Malone
  */
 public class Swapper {
     
     public static final int SWAP_ID_INVALID = -1;
     public static final int EMPTY_INPUT = -2;
     public static final int DUPLICATE_SWAP_ID = -3;
     
     private List<File> fileList;
     private List<Integer> swapIds;
     private TempFile tempFile;
     
     public Swapper(List<File> list) {
         this.fileList = list;
         swapIds = new ArrayList<Integer>(list.size());
         tempFile = new TempFile();
     }
     
     /**
      * Swaps all the files in fileList with the user entered swapIds
      * @throws IOException if there is an error when swapping the
      * files.
      */
     public void swap() throws IOException {
         assertFilesToSwapWritable();
         renameToTemp();
         renameToDestination();
     }
     
     /**
      * Renames each file in fileList that is to be swapped, from its  
      * temporary location to its destination. If there is an error when
      * renaming, an attempt will be made to roll back the files to their 
      * existing location
      * @throws IOException if there was an error moving a temporary file to
      * its destination
      */
     private void renameToDestination() throws IOException {
         for(int i = 0; i < swapIds.size(); i++) {
             if(swapIds.get(i) != EMPTY_INPUT) {
                 File tmp = tempFile.getTempFile(fileList.get(i));
                 try {
                    rename(tmp, fileList.get(swapIds.get(i)));
                 } catch(IOException e) {
                     rollbackToTemp(i);
                     rollbackTemp(fileList.size());
                     throw e;
                 }
             }
         }
     }
       
     /**
      * Renames each file in fileList that is to be swapped, to a temporary 
      * location in the same directory as the original file and its file
      * name will also be based on the original file. If there is an error
      * when renaming, then an attempt will be made to roll back the files
      * to their existing location
      * @throws IOException if there was an error when renaming a file to
      * its temporary location
      */
     private void renameToTemp() throws IOException {
         for(int i = 0; i < swapIds.size(); i++) {
             if(swapIds.get(i) != EMPTY_INPUT) {
                 try {
                    rename(fileList.get(i), tempFile.getTempFile(fileList.get(i)));
                 } catch(IOException e) {
                     rollbackTemp(i);
                     throw e;
                 }
             }
         }
     }
     
     /**
     * Renames the File srcFile to destFile if destFile doesn't already exist.
      * @param srcFile File to be renamed
     * @param destFile File to be renamed to
     * @throws IOException if the destination file already exists
      * @throws IOException if write access was not granted to rename
      * @throws IOException if renaming failed
      */
    private void rename(File srcFile, File destFile) throws IOException {
         String message = String.format("Unable to rename %s to the destination %s", srcFile.getAbsolutePath(), destFile.getAbsolutePath());
         if(destFile.exists()) {
             throw new IOException(message + " because the destination file already exists");
         }
         try {
             if(!srcFile.renameTo(destFile)) {
                 throw new IOException(message);
             }
         } catch(SecurityException e) {
             throw new IOException(message + " because write access was denied to one/both files");
         }
     }
     
     /**
      * Attempts to roll back the file that has been swapped to its temporary 
      * location.
      * @param count number of files to roll back. 0 < count <= fileList.size()
      */
     private void rollbackToTemp(int count) {
         for(int i = 0; i < count; i++) {
             if(swapIds.get(i) != EMPTY_INPUT) {
                 File tmp = tempFile.getTempFile(fileList.get(i));
                 rollback(fileList.get(swapIds.get(i)), tmp);
             }
         }
     }
     
     /**
      * Attempts to roll back the temporary files to their original location
      * @param count number of files to roll back. 0 < count <= fileList.size()
      */
     private void rollbackTemp(int count) {
         for(int i = 0; i < count; i++) {
             if(swapIds.get(i) != EMPTY_INPUT) {
                 File tmp = tempFile.getTempFile(fileList.get(i));
                 rollback(tmp, fileList.get(i));
             }
         }
     }
     
     /**
      * Attempts to roll back the file that has been swapped to its temporary 
      * file only if the destination file doesn't exist.
      */
     private void rollback(File srcFile, File destFile) {
         if(destFile.exists()) {
             return;
         }
         if(srcFile.exists()) {
             try {
                rename(srcFile, destFile);
             } catch(IOException e) {
 
             }
         }
     }
     
     /**
      * Asserts that the files in fileList that are to be swapped, are
      * writable. Both original file and swap location directories 
      * are also checked for write access
      * @throws IOException If any file does not have write access
      */
     private void assertFilesToSwapWritable() throws IOException {
         for(int i = 0; i < swapIds.size(); i++) {
             if(swapIds.get(i) != EMPTY_INPUT) {
                 String message = "The file %s is not writable";
                 if(!fileList.get(i).canWrite()) {
                     throw new IOException(String.format(message, fileList.get(i).getAbsolutePath()));
                 }
                 if(!fileList.get(i).getAbsoluteFile().getParentFile().canWrite()) {
                     throw new IOException(String.format(message, fileList.get(i).getAbsoluteFile().getParentFile().getAbsolutePath()));
                 }
                 File tmp = tempFile.getTempFile(fileList.get(swapIds.get(i)));
                 if(!tmp.getAbsoluteFile().getParentFile().canWrite()) {
                     throw new IOException(String.format(message, tmp.getAbsoluteFile().getParentFile().getAbsolutePath()));
                 }
             }
         }
     }
     
     /**
      * Checks if the ID's the user has entered are valid swaps
      * @return true if the swaps are valid, false otherwise
      */
     public boolean isSwapsValid() {
         boolean valid = true;
         for (int i = 0; i < swapIds.size(); i++) {
             if (swapIds.get(i) != EMPTY_INPUT) {
                 // (1) swapIds.get(i) is the index of the filename to be swapped to.
                 // (2) swapIds.get(swapIds.get(i)) is the index of the filename at the position
                 // where the filename at (1) would be renamed to.
                 // If (2) has no index to rename to, then (2) filename would stay the same
                 // and (1) filename cant be renamed because (2) already exists.
                 if (swapIds.get(swapIds.get(i)) == EMPTY_INPUT) {
                     valid = false;
                 }
             }
         }
         return valid;
     }
     
     /**
      * Checks if the user has entered any swaps.
      * @return true if swap list is empty, false otherwise
      */
     public boolean isSwapListEmpty() {
         boolean empty = true;
         for (int i = 0; i < swapIds.size(); i++) {
             if (swapIds.get(i) != EMPTY_INPUT) {
                 empty = false;
                 break;
             }
         }
         return empty;
     }
     
     /**
      * Gets the formatted text previews of the files to be swapped
      * @return formatted preview string
      */
     public String preview() {
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < swapIds.size(); i++) {
             if (swapIds.get(i) != EMPTY_INPUT) {
                 sb.append(fileList.get(i).getName());
                 sb.append("\n => ");
                 sb.append(fileList.get(swapIds.get(i)).getName());
                 sb.append('\n');
             }
         }
         return sb.toString();
     }
     
     /**
      * Prompts the user for the input ID's to be swapped
      */
     public void promptIds() {
         int tmpInput;
         for (int i = 0; i < fileList.size(); i++) {
             Display.displayPromptForId(fileList, i);
             while ((tmpInput = readUserInput()) < 0) {
                 if(tmpInput == SWAP_ID_INVALID) {
                     Display.displayPromptIdsError(fileList, i, "Invalid filename ID");
                 } else if (tmpInput == DUPLICATE_SWAP_ID) {
                     Display.displayPromptIdsError(fileList, i, "You can only use each ID once");
                 } else {
                     break;
                 }
             }
             // only add if input id is not same as current file
             if (tmpInput == i) {
                 swapIds.add(EMPTY_INPUT);
             } else {
                 swapIds.add(tmpInput);
             }
         }
     }
     
     /**
      * Reads a list index from user input filename ID
      * @return list index or error as SWAP_ID_INVALID, DUPLICATE_SWAP_ID, EMPTY_INPUT
      */
     public int readUserInput() {
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
         int i = SWAP_ID_INVALID;
         try {
             String input = br.readLine();
             if (input.isEmpty()) {
                 return EMPTY_INPUT;
             }
             // user display index start from 1, so decrement for real index
             i = Integer.parseInt(input) - 1;
             if (i < 0 || i >= fileList.size()) {
                 return SWAP_ID_INVALID;
             }
             if (swapIds.contains(i)) {
                 return DUPLICATE_SWAP_ID;
             }
         } catch (NumberFormatException e) {
 
         } catch (IOException e) {
             return EMPTY_INPUT;
         }
         return i;
     }
     
     /**
      * Prompts the user for confirmation
      * @return true if the user chose yes, false otherwise
      */
     public boolean confirmSwaps() {
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
         try {
             if(br.readLine().equalsIgnoreCase("y")) {
                 return true;
             }
         } catch(IOException ex) {
             
         }
         return false;
     }
     
 }
