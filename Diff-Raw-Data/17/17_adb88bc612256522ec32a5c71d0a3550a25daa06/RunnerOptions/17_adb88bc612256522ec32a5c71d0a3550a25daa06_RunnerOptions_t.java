 /*
  *    Copyright 2011 The 99 Software Foundation
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package org.nnsoft.t2t;
 
 import java.io.File;
 
 import org.openrdf.model.impl.URIImpl;
 
 import com.beust.jcommander.IStringConverter;
 import com.beust.jcommander.Parameter;
 
 /**
  * 
  */
 public final class RunnerOptions {
 
    @Parameter(names = { "-h", "--help" }, description = "Display help information.")
     private boolean printHelp;
 
    @Parameter(names = { "-v", "--version" }, description = "Display version information.")
     private boolean printVersion;
 
     @Parameter(
         names = { "-c", "--configuration" },
        description = "Force the use of an alternate XML Configuration file.",
        converter = FileConverter.class
     )
    private File configurationFile = new File(System.getProperty("user.dir"), "t2t-config.xml");
 
     @Parameter(
         names = { "-e", "--entrypoint" },
        description = "The URL entrypoint.",
         converter = URIImplConverter.class,
         required = true
     )
     private URIImpl entryPoint;
 
     public boolean isPrintHelp() {
         return printHelp;
     }
 
     public boolean isPrintVersion() {
         return printVersion;
     }
 
     public File getConfigurationFile() {
         return configurationFile;
     }
 
     public URIImpl getEntryPoint() {
         return entryPoint;
     }
 
     /**
      * 
      */
     public static class FileConverter implements IStringConverter<File> {
 
         public File convert(String value) {
             return new File(value);
         }
 
     }
 
     /**
      * 
      */
     public static class URIImplConverter implements IStringConverter<URIImpl> {
 
         public URIImpl convert(String value) {
             return new URIImpl(value);
         }
 
     }
 
 }
