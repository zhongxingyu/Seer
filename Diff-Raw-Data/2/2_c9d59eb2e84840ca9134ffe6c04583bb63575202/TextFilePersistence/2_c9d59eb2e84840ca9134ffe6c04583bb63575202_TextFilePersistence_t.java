 package uk.ac.ebi.arrayexpress.utils.persistence;
 
 /*
  * Copyright 2009-2010 European Molecular Biology Laboratory
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import uk.ac.ebi.arrayexpress.utils.StringTools;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 
 public class TextFilePersistence<Object extends Persistable>
 {
     // logging machinery
     private final Logger logger = LoggerFactory.getLogger(getClass());
 
     // persistence file handle
     private File persistenceFile;
 
     // internal object holder
     private Object object;
 
     public TextFilePersistence( Object obj, File file )
     {
         // TODO: check object and file
         object = obj;
         persistenceFile = file;
     }
 
     public Object getObject() throws Exception
     {
         if (null != object) {
             if (object.isEmpty()) {
                 loadObject();
             }
         }
         return object;
     }
 
     public void setObject( Object obj ) throws Exception
     {
         object = obj;
         save(object.toPersistence());
     }
 
     private void loadObject() throws Exception
     {
         String text = load();
         if (null != text) {
             object.fromPersistence(text);
         }
     }
 
     private String load() throws Exception
     {
         logger.debug("Retrieving persistable object [{}] from [{}]"
                 , object.getClass().toString()
                 , persistenceFile.getName());
 
         String text = null;
         try {
            text = StringTools.fileToString(persistenceFile, "UTF-8");
         } catch (FileNotFoundException x) {
             logger.error(x.getMessage());
         }
         return text;
     }
 
     private void save( String objectString ) throws Exception
     {
         logger.debug("Saving persistable object [{}] to [{}]"
                 , object.getClass().toString()
                 , persistenceFile.getName());
 
         StringTools.stringToFile(objectString, persistenceFile);
     }
 }
