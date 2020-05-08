 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
  * 
  * Licensed under the Educational Community License, Version 1.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at
  * 
  *      http://www.opensource.org/licenses/ecl1.php
  * 
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  *
  **********************************************************************************/
 
 package org.sakaiproject.metaobj.shared.mgt;
 
 import org.jdom.Element;
import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.exception.*;
 import org.sakaiproject.metaobj.shared.mgt.home.StructuredArtifactHomeInterface;
 import org.sakaiproject.metaobj.shared.model.Id;
 import org.sakaiproject.metaobj.shared.model.StructuredArtifactDefinitionBean;
 import org.sakaiproject.metaobj.shared.model.Artifact;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 public interface StructuredArtifactDefinitionManager {
    public final static String GLOBAL_SAD_QUALIFIER = "theospi.share.sad.global";
 
    public Map getHomes();
 
    /**
     * @param worksiteId
     * @return a map with all worksite and global homes
     */
    public Map getWorksiteHomes(Id worksiteId);
 
    public List findHomes();
 
    /**
     * @return list of all published globals or global sad owned by current user
     */
    public List findGlobalHomes();
 
    /**
     * @param currentWorksiteId
     * @return list of globally published sads or published sad in currentWorksiteId or sads in
     *         currentWorksiteId owned by current user
     */
    public List findHomes(Id currentWorksiteId);
   
   /**
    * 
    * @param resource The ContentResource of a file in resources that is the xsd file
    * @return A list of StructuredArtifactDefinitionBean objects
    */
   public List findBySchema(ContentResource resource);
 
    public StructuredArtifactDefinitionBean loadHome(String type);
 
    public StructuredArtifactDefinitionBean loadHome(Id id);
 
    public StructuredArtifactDefinitionBean loadHomeByExternalType(String externalType, Id worksiteId);
 
    public StructuredArtifactDefinitionBean save(StructuredArtifactDefinitionBean sad);
    public StructuredArtifactDefinitionBean save(StructuredArtifactDefinitionBean sad, boolean updateModDate);
 
    public void delete(StructuredArtifactDefinitionBean sad);
 
    /**
     * @return true if user is in a SAD tool that is configured to manipulate globals SADs
     */
    public boolean isGlobal();
 
    public Collection getRootElements(StructuredArtifactDefinitionBean sad);
 
    public void validateSchema(StructuredArtifactDefinitionBean sad);
 
    public StructuredArtifactHomeInterface convertToHome(StructuredArtifactDefinitionBean sad);
 
    public boolean importSADResource(Id worksiteId, String resourceId, boolean findExisting)
          throws IOException, ServerOverloadException, PermissionException, 
                 IdUnusedException, ImportException, UnsupportedFileTypeException;
 
    /**
     * This is the default method for exporting a form into a stream.  This method does check the
     * form export permission.
     * @param formId String
     * @param os OutputStream
     * @throws IOException
     */
    public void packageFormForExport(String formId, OutputStream os) throws IOException;
    
    /**
     * This method will export a form into a stream.  It has the ability to turn off checking
     * for the export form permission.
     * @param formId String
     * @param os OutputStream
     * @param checkPermission boolean
     * @throws IOException
     */
    public void packageFormForExport(String formId, OutputStream os, boolean checkPermission) throws IOException;
 
    public StructuredArtifactDefinitionBean importSad(Id worksiteId, InputStream in,
                 boolean findExisting, boolean publish)
          throws IOException, ImportException;
    
    public StructuredArtifactDefinitionBean importSad(Id worksiteId, InputStream in,
                 boolean findExisting, boolean publish, boolean foundThrowsException)
          throws IOException, ImportException;
 
    public Element createFormViewXml(String formId, String returnUrl);
 
    public Element createFormViewXml(Artifact art, String returnUrl);
 }
