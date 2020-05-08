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
 
 import java.io.File;
import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  *
  * @author Sam Malone
  */
 public class Args {
     
     private List<File> fileList;
     
     private Args() {
         
     }
 
     /**
      * Get the input FILE list
      * @return input FILE list
      */
     public List<File> getFileList() {
         return fileList;
     }
     
     /**
      * Get a valid path from a given input FILE argument.
      * Cygwin paths will be converted into Windows format
      * @param arg input FILE argument
      * @return valid path
      */
     private static String getPath(String arg) {
         if (arg.startsWith("/cygdrive/")) {
             return arg.charAt(10) + ":\\" + arg.substring(12);
         }
         return arg;
     }
     
     /**
      * Parses the given arguments into an Args object
      * @param args Program arguments
      * @return Args or null if help flag set
      */
     public static Args parse(String[] args) {
         Args arguments = new Args();
         List<File> list = new ArrayList<File>();
         File input;
         for(String arg : args) {
             if(arg.equals("-h") || arg.equals("--help")) {
                 return null;
             } else {
                 input = new File(getPath(arg));
                 if (input.isDirectory()) {
                     list.addAll(Arrays.asList(input.listFiles(new FileFilter())));
                 } else {
                     list.add(input);
                 }
             }
         }
         arguments.fileList = list;
         return arguments;
     }
     
     /**
      * Validates the argument given
      * @param args Parsed Arguments to validate
     * @throws FileNotFoundException if any of the input FILE arguments do
     * not exists
     * @throws FileNotFoundException if there are no files to swap or if only
     * 1 file to swap
      */
    public static void validate(Args args) throws FileNotFoundException {
         for(File f : args.fileList) {
             if (!f.exists()) {
                throw new FileNotFoundException(String.format("The input file %s does not exist", f.getAbsolutePath()));
             }
         }
        switch(args.fileList.size()) {
            case 0: throw new FileNotFoundException("There are no files to swap");
            case 1: throw new FileNotFoundException("You cannot swap only one file. You must have at least 2 files to swap file names");
        }
     }
     
 }
