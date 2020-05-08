 /*
  * The contents of this file are subject to the Mozilla Public
  * License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * The Original Code is Web Questionnaires 2
  *
  * The Initial Owner of the Original Code is European Environment
  * Agency. Portions created by TripleDev are Copyright
  * (C) European Environment Agency.  All Rights Reserved.
  *
  * Contributor(s):
  *        Anton Dmitrijev
  */
 package eionet.webq.dao;
 
 import java.util.Collection;
 
 import eionet.webq.dao.orm.ProjectFile;
 import eionet.webq.dto.WebFormType;
 
 /**
  * Interface for interactions with web forms repository.
  */
 public interface WebFormStorage {
     /**
      * Retrieves all web forms which are set as active and main form,
      * also xml schema must be set.
      *
      * @param type form type
      * @return collection of web forms
      */
     Collection<ProjectFile> getAllActiveWebForms(WebFormType type);
 
     /**
      * Get active web form by id.
      *
      *
      * @param id web form id
      * @param type form type
      * @return active web form
      */
     ProjectFile getActiveWebFormById(WebFormType type, int id);
 
     /**
      * Finds web forms for schemas.
      *
      * @param type form type
      * @param xmlSchemas xml schemas collection
      * @return matched web forms
      */
     Collection<ProjectFile> findWebFormsForSchemas(WebFormType type, Collection<String> xmlSchemas);
 
     /**
      * Get web form by id.
      *
      *
      * @param id web form id
     * @param type form type
      * @return active web form
      */
     ProjectFile getWebFormById(int id);
 }
