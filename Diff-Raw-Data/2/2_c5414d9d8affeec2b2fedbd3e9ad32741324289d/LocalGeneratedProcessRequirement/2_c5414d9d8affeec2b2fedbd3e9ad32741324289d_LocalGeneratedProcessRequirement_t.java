 /*****************************************************************************************
  * *** BEGIN LICENSE BLOCK *****
  *
  * Version: MPL 2.0
  *
  * echocat Jomon, Copyright (c) 2012-2013 echocat
  *
  * This Source Code Form is subject to the terms of the Mozilla Public
  * License, v. 2.0. If a copy of the MPL was not distributed with this
  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
  *
  * *** END LICENSE BLOCK *****
  ****************************************************************************************/
 
 package org.echocat.jomon.process.local;
 
 import org.echocat.jomon.process.BaseGeneratedProcessRequirement;
 
 import javax.annotation.Nonnull;
 import java.io.File;
 
 import static java.lang.System.getProperty;
 
 public class LocalGeneratedProcessRequirement extends BaseGeneratedProcessRequirement<File, LocalGeneratedProcessRequirement> {
 
     @Nonnull
     public static LocalGeneratedProcessRequirement process(@Nonnull File executable) {
         return new LocalGeneratedProcessRequirement(executable);
     }
 
     @Nonnull
     public static LocalGeneratedProcessRequirement process(@Nonnull String executable) {
         return new LocalGeneratedProcessRequirement(executable);
     }
 
     public LocalGeneratedProcessRequirement(@Nonnull File executable) {
         super(executable);
         withWorkingDirectory(getProperty("user.dir", "."));
     }
 
     public LocalGeneratedProcessRequirement(@Nonnull String executable) {
         this(new File(executable));
     }
 
     @Override
     @Nonnull
     public LocalGeneratedProcessRequirement withWorkingDirectory(@Nonnull File workingDirectory) {
        if (!workingDirectory.isDirectory()) {
             throw new IllegalArgumentException(workingDirectory + " does not exist.");
         }
         return super.withWorkingDirectory(workingDirectory);
     }
 
     @Nonnull
     public LocalGeneratedProcessRequirement withWorkingDirectory(@Nonnull String workingDirectory) {
         return withWorkingDirectory(new File(workingDirectory));
     }
 
     @Nonnull
     @Override
     public File getWorkingDirectory() {
         return super.getWorkingDirectory();
     }
 
 }
