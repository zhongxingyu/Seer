 /*
  * Copyright 2012 Joseph Spencer.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.xforj.productions;
 
 import com.xforj.*;
 import java.util.regex.Matcher;
 import java.io.*;
 
 /**
  *
  * @author Joseph Spencer
  */
 public class ImportStatement extends Production {
    public ImportStatement(Output output) {
       super(output);
    }
 
    @Override
    public void execute(CharWrapper characters, ProductionContext context) throws Exception {
       Matcher _import = characters.match(IMPORT);       
       if(_import.find()){
          String matchedImportTag = _import.group(1);
          characters.shift(matchedImportTag.length());
 
          LOGGER.debug("Matching import tag: '"+matchedImportTag+"'.");
 
          Matcher path = characters.match(IMPORT_PATH);
          if(path.find()){
             String importedPath = path.group(1);
             characters.shift(importedPath.length());
 
            importedPath = importedPath.trim().replaceAll("\\\\\\}", "\\}");//Remove leading / trailing whitespace.
 
             LOGGER.debug("Using given import path: '"+importedPath+"'.");
 
             String pathToUseForImport;
             File testAbsolutePathFile = new File(importedPath);
 
             if(testAbsolutePathFile.exists()){//it's an absolute path
                LOGGER.debug("Absolute path given.");
 
                pathToUseForImport = testAbsolutePathFile.getCanonicalPath();
             } else {//it must be relative
                LOGGER.debug("Attempting relative path.");
 
                pathToUseForImport = context.currentFile.getParent();
 
                LOGGER.debug("Using parent path: "+pathToUseForImport);
 
                if(!pathToUseForImport.endsWith(File.separator)){
                   pathToUseForImport+=File.separator;
                }
                pathToUseForImport+=importedPath;
             }
 
             LOGGER.debug("Preparing to import: "+pathToUseForImport);
             if(!new File(pathToUseForImport).exists()){
                throw new Exception("The following file could not be found: "+pathToUseForImport);
             }
 
             if(characters.charAt(0) == '}'){
                characters.shift(1);
                Output importFile = context.importFile(pathToUseForImport);
                output.add(importFile);
                context.removeProduction();
                return;
             }
          }
       }
       throw new Exception("Invalid Statement found while parsing ImportStatement.");
    }
 }
