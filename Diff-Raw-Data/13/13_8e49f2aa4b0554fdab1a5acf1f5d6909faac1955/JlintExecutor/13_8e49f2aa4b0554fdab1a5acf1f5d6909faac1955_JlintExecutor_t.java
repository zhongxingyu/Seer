 /* Jlint Ant Task
     Copyright (C) 2012 Keita Kita
 
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU Lesser General Public
     License as published by the Free Software Foundation; either
     version 2.1 of the License, or (at your option) any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     Lesser General Public License for more details.
 
     You should have received a copy of the GNU Lesser General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 package com.github.mikanbako.ant.jlinttask;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * This object executes Jlint.
  *
  * This class is independent with Ant.
  */
 /* package */ final class JlintExecutor {
     /**
      * Path of executable file of Jlint.
      */
     private final File mJlintExecutable;
 
     /**
      * List of class file.
      */
     private final ArrayList<File> mClassFiles;
 
     /**
      * Path of directory that contains source files.
      */
     private File mSourceDirectory;
 
     /**
      * Set of option.
      */
     private final HashSet<String> mOptions = new HashSet<String>();
 
     /**
      * Object that executes command.
      */
     private final CommandExecutor mCommandExecutor;
 
     /**
      * Constructor.
      *
      * @param jlintExecutable Jlint executable file
      * @param classFiles Class files
      */
     public JlintExecutor(File jlintExecutable, List<File> classFiles) {
         this(new ProcessExecutor(), jlintExecutable, classFiles);
     }
 
     /**
      * Constructor with {@link CommandExecutor}.
      *
      * @param executor Object that executes command
      * @param jlintExecutable Jlint executable file
      * @param classFiles List of class file
      * @throws IllegalArgumentException Argument is null
      */
     /* package */ JlintExecutor(CommandExecutor executor,
             File jlintExecutable, List<File> classFiles) {
         if (executor == null || jlintExecutable == null || classFiles == null) {
             throw new IllegalArgumentException("Argument is null.");
         }
 
         mCommandExecutor = executor;
         mJlintExecutable = jlintExecutable;
         mClassFiles = new ArrayList<File>(classFiles);
     }
 
     /**
      * Set path of directory that contains source files.
      *
      * @param sourceDirectory Path of directory that contains source files
      */
     public void setSourceDirectory(File sourceDirectory) {
         mSourceDirectory = sourceDirectory;
     }
 
     /**
      * Set options.
      *
      * The option does not have its argument.
      *
      * @param options Set of options.
      */
     public void setOptions(Set<String> options) {
         mOptions.clear();
         mOptions.addAll(options);
     }
 
     /**
      * Execute Jlint.
      *
      * @param output {@link StringBuilder} that is appended for the standard
      *  output from Jlint
      * @param error {@link StringBuilder} that is appended for the standard
      *  error from Jlint
      * @return Exit code
      * @throws IOException If I/O error occurs
      * @throws InterruptedException If this thread is interrupted
      */
    public int execute(StringBuilder output, StringBuilder error) throws
            IOException, InterruptedException {
         ArrayList<String> command = new ArrayList<String>();
 
         command.add(mJlintExecutable.getAbsolutePath());
 
         if (mSourceDirectory != null) {
             command.add("-source");
             command.add(mSourceDirectory.getAbsolutePath());
         }
 
         for (String option : mOptions) {
             command.add(option);
         }
 
         for (File classFile : mClassFiles) {
             command.add(classFile.getAbsolutePath());
         }
 
         return mCommandExecutor.execute(command, output, error);
     }
 }
