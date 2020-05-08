 /*
  * Copyright  1999-2004 The Apache Software Foundation
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *
  */
 
 package org.apache.lenya.pubs.wiki;
 
 import java.io.File;
 
 import org.apache.lenya.cms.authoring.DefaultCreator;
 import org.apache.log4j.Category;
 
 public class NewWikiDocCreator extends DefaultCreator {
     Category log = Category.getInstance(NewWikiDocCreator.class);
 
     /**
      * Return the child type.
      *
      * @param childType a <code>short</code> value
      *
      * @return a <code>short</code> value
      *
      * @exception Exception if an error occurs
      */
     public short getChildType(short childType) throws Exception {
         return BRANCH_NODE;
     }
 
     /** (non-Javadoc)
      * @see org.apache.lenya.cms.authoring.DefaultCreator#getChildFileName(java.io.File, java.lang.String)
      */
     protected String getChildFileName(
         File parentDir,
         String childId,
         String language) {
 
        log.info("Parent directory: " + parentDir);
 
         // TODO: Quite a hack, but needs to be replaced by JCR anyway
         File file = new File(parentDir.getParentFile().getParent()
             + File.separator + "repository" + File.separator + "paths" + File.separator + "authoring"
             + File.separator
             + childId
             + File.separator
             + "index"
             + getLanguageSuffix(language)
             + ".xml"
 	    + File.separator + "resource-content"
             );
 
        log.info("Filename: " + file.getAbsolutePath());
 
         return file.getAbsolutePath();
     }
 }
