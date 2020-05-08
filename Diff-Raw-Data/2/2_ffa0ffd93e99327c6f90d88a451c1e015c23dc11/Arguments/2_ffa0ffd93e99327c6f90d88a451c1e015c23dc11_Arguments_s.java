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
 package merge.largest;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.List;
 import merge.largest.exception.ArgumentException;
 import merge.largest.exception.DirectoryExpectedException;
 
 /**
  * 
  * @author Sam Malone
  */
 public class Arguments {
     
     private boolean isRecursive;
     private boolean isTestMode;
     private List<File> inputDirectories;
     
     /**
      * The destination directory File is where the input directories\' largest
      * file will be merged to. The default directory is the current working
      * directory.
      */
     private File destinationDirectory;
     
     /**
      * Creates a new Arguments instance
      */
     private Arguments() {
         inputDirectories = new ArrayList<File>();
         destinationDirectory = new File(".");
     }
 
     /**
      * Check if the recursive flag is set
      * @return true if the recursive flag is set, false otherwise.
      */
     public boolean isRecursive() {
         return isRecursive;
     }
 
     /**
      * Check is the test mode flag is set
      * @return true if test mode flag is set, false otherwise.
      */
     public boolean isTestMode() {
         return isTestMode;
     }
 
     /**
      * Get the destination directory File
      * @return Destination directory File
      */
     public File getDestinationFile() {
         return destinationDirectory;
     }
 
     /**
      * Gets the input directories
      * @return Input Directories
      */
     public List<File> getInputDirectories() {
         return inputDirectories;
     }
     
     /**
      * Gets a platform independent File based on the path given. This can be
      * used to convert virtual paths to real locations i.e. /cygdrive/c/ = C:\
      * if using Cygwin.
      * @param path Path to create a File from
      * @return Platform Independent File
      */
     private static File getPlatformIndependentFile(String path) {
         if(path.startsWith("/cygdrive/")) {
             return new File(new StringBuilder().append(path.charAt(10)).append(":\\").append(path.substring(12)).toString());
         }
         return new File(path);
     }
     
     /**
      * Attempt to parse the arguments array into an Arguments object.
      * @param args Program Arguments
      * @return Arguments or null if help flag is set
      * @throws ArgumentException if a destination directory value is not given
      * @throws ArgumentException if no input directories are given
      */
     public static Arguments parse(String[] args) throws ArgumentException {
         for(String curArg : args) {
             if (curArg.equals("-h") || curArg.equals("--help")) {
                 return null;
             }
         }
         Arguments arguments = new Arguments();
         boolean isArg = false;
         for (int i = 0; i < args.length; i++) {
             if(isArg) {
                 isArg = false;
                 continue;
             }
             if (args[i].equals("-d") || args[i].equals("--dest-dir")) {
                 if (i + 1 < args.length) {
                     arguments.destinationDirectory = getPlatformIndependentFile(args[i + 1]);
                     isArg = true;
                     continue;
                 }
                 throw new ArgumentException("The destination directory requires an argument");
             }
             if (args[i].equals("-r") || args[i].equals("--recursive")) {
                 arguments.isRecursive = true;
                 continue;
             }
             if (args[i].equals("-t") || args[i].equals("--test")) {
                 arguments.isTestMode = true;
                 continue;
             }
             arguments.inputDirectories.add(getPlatformIndependentFile(args[i]));
         }
         if (arguments.inputDirectories.isEmpty()) {
             throw new ArgumentException("No input directories were given");
         }
         return arguments;
     }
     
     /**
      * Attempt to validate the given Arguments
      * @param args Parsed Arguments
      * @throws FileNotFoundException if any of the input directories do not exist
      * @throws FileNotFoundException if the destination directory does not exist
      */
     public static void validate(Arguments args) throws FileNotFoundException, DirectoryExpectedException {
         for(File arg : args.inputDirectories) {
             if (!arg.exists()) {
                 throw new FileNotFoundException("Could not find the directory: " + arg.getAbsolutePath());
             }
             if (!arg.isDirectory()) {
                throw new DirectoryExpectedException("The input file " + arg.getAbsolutePath() + "is not a directory");
             }
         }
         if (args.destinationDirectory != null) {
             if (!args.destinationDirectory.exists()) {
                 throw new FileNotFoundException("The destination directory could not be found at: " + args.destinationDirectory.getAbsolutePath());
             }
             if(!args.destinationDirectory.isDirectory()) {
                 throw new DirectoryExpectedException("The destination directory argument given is not a directory");
             }
         }
     }
     
 }
