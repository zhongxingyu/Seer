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
 
 package org.sakaiproject.metaobj.shared.model;
 
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.site.api.Site;
 import org.sakaiproject.site.cover.SiteService;
 
 import java.text.MessageFormat;
 import java.util.Date;
 
 /**
  * @author chmaurer
  */
 public class StructuredArtifactDefinitionBean extends IdentifiableObject implements Comparable {
 
    public static final int STATE_UNPUBLISHED = 0;
    public static final int STATE_WAITING_APPROVAL = 1;
    public static final int STATE_PUBLISHED = 2;
 
    private Id id;
    private String documentRoot;
    private Agent owner;
    private Date created = new Date();
    private Date modified = new Date();
    private String schemaHash;
 
    /**
     * system only SAD's are not available to users to populate via a web form, they are for internal system use only
     */
    private boolean systemOnly = false;
    private String description;
    private boolean modifiable = true;
    private Id xslConversionFileId;
    private String schemaFileName;
    private String xslFileName;
    private byte[] schema;
    private String siteId;
    private String externalType;
    private String instruction;
    private String filePickerAction;
 
    private transient String decoratedDescription;
 
    /**
     * should be one of the following states
     * <p/>
     * unpublished -> active
     */
    private int siteState;
 
    /**
     * should be one of the following states
     * <p/>
     * unpublished -> waiting for approval-> active
     */
    private int globalState;
 
    /**
     * used during edit process to store whether or not xsl transform is necessary
     */
    private boolean requiresXslFile = false;
 
    /**
     * file id of schema file - used when add/editing artifact homes
     */
    private Id schemaFile;
 
    /**
     * used in publishing web form to set action (publish to site, global, approve, etc)
     */
    private String action;
 
    private static final MessageFormat format =
          new MessageFormat("<META HTTP-EQUIV=\"Refresh\" CONTENT=\"0;URL={0}/member/viewArtifact.osp?artifactId={1}&artifactType={2}\">");
 
 
    /**
     * @return Returns the action.
     */
    public String getAction() {
       return action;
    }
 
    /**
     * @param action The action to set.
     */
    public void setAction(String action) {
       this.action = action;
    }
 
    /**
     * @return Returns the created.
     */
    public Date getCreated() {
       return created;
    }
 
    /**
     * @param created The created to set.
     */
    public void setCreated(Date created) {
       this.created = created;
    }
 
    /**
     * @return Returns the description.
     */
    public String getDescription() {
       return description;
    }
 
    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
       this.description = description;
    }
 
    /**
     * @return Returns the documentRoot.
     */
    public String getDocumentRoot() {
       return documentRoot;
    }
 
    /**
     * @param documentRoot The documentRoot to set.
     */
    public void setDocumentRoot(String documentRoot) {
       this.documentRoot = documentRoot;
    }
 
    /**
     * gets the global published state
     *
     * @return Returns the globalState.
     */
    public int getGlobalState() {
       return globalState;
    }
 
    /**
     * sets the global published state
     *
     * @param globalState The globalState to set.
     */
    public void setGlobalState(int globalState) {
       this.globalState = globalState;
    }
 
    /**
     * @return Returns the modifiable.
     */
    public boolean isModifiable() {
       return modifiable;
    }
 
    /**
     * @param modifiable The modifiable to set.
     */
    public void setModifiable(boolean modifiable) {
       this.modifiable = modifiable;
    }
 
    /**
     * @return Returns the modified.
     */
    public Date getModified() {
       return modified;
    }
 
    /**
     * @param modified The modified to set.
     */
    public void setModified(Date modified) {
       this.modified = modified;
    }
 
    /**
     * @return Returns the owner.
     */
    public Agent getOwner() {
       return owner;
    }
 
    /**
     * @param owner The owner to set.
     */
    public void setOwner(Agent owner) {
       this.owner = owner;
    }
 
    /**
     * @return Returns the requiresXslFile.
     */
    public boolean getRequiresXslFile() {
       return requiresXslFile;
    }
 
    /**
     * @param requiresXslFile The requiresXslFile to set.
     */
    public void setRequiresXslFile(boolean requiresXslFile) {
       this.requiresXslFile = requiresXslFile;
    }
 
    /**
     * @return Returns the schemaFile.
     */
    public Id getSchemaFile() {
       return schemaFile;
    }
 
    /**
     * @param schemaFile The schemaFile to set.
     */
    public void setSchemaFile(Id schemaFile) {
       this.schemaFile = schemaFile;
    }
 
    /**
     * @return Returns the schemaFileName.
     */
    public String getSchemaFileName() {
       return schemaFileName;
    }
 
    /**
     * @param schemaFileName The schemaFileName to set.
     */
    public void setSchemaFileName(String schemaFileName) {
       this.schemaFileName = schemaFileName;
    }
 
    /**
     * gets the published state
     *
     * @return Returns the siteState.
     */
    public int getSiteState() {
       return siteState;
    }
 
    /**
     * sets the published state
     *
     * @param siteState The siteState to set.
     */
    public void setSiteState(int siteState) {
       this.siteState = siteState;
    }
 
    /**
     * @return Returns the systemOnly.
     */
    public boolean isSystemOnly() {
       return systemOnly;
    }
 
    /**
     * @param systemOnly The systemOnly to set.
     */
    public void setSystemOnly(boolean systemOnly) {
       this.systemOnly = systemOnly;
    }
 
    /**
     * @return Returns the xslConversionFileId.
     */
    public Id getXslConversionFileId() {
       return xslConversionFileId;
    }
 
    /**
     * @param xslConversionFileId The xslConversionFileId to set.
     */
    public void setXslConversionFileId(Id xslConversionFileId) {
       this.xslConversionFileId = xslConversionFileId;
    }
 
    /**
     * @return Returns the xslFileName.
     */
    public String getXslFileName() {
       return xslFileName;
    }
 
    /**
     * @param xslFileName The xslFileName to set.
     */
    public void setXslFileName(String xslFileName) {
       this.xslFileName = xslFileName;
    }
 
    public String getSiteId() {
       return siteId;
    }
 
    public void setSiteId(String siteId) {
       this.siteId = siteId;
    }
 
    public byte[] getSchema() {
       return schema;
    }
 
    public void setSchema(byte[] schema) {
       this.schema = schema;
    }
 
    public String getExternalType() {
       return externalType;
    }
 
    public void setExternalType(String externalType) {
       this.externalType = externalType;
    }
 
    public String getInstruction() {
       return instruction;
    }
 
    public void setInstruction(String instruction) {
       this.instruction = instruction;
    }
 
    /**
     * @return Returns the type.
     */
    public Type getType() {
       Type type = new Type();
       if (getId() != null) {
          type.setId(getId());
       }
       if (getDescription() != null) {
          type.setDescription(getDecoratedDescription());
       }
       type.setSystemOnly(isSystemOnly());
       return type;
    }
 
    /**
     * This method doesn't do any authz, it simply checks the state
     *
     * @return true, if sad can be published to site.
     */
    public boolean getCanPublish() {
       return (siteState == STATE_UNPUBLISHED && globalState != STATE_PUBLISHED);
    }
 
    public boolean getCanGlobalPublish() {
       return (globalState == STATE_UNPUBLISHED);
    }
 
    /**
     * This method doesn't do any authz, it simply checks the state
     *
     * @return true, if sad can be suggested for global publish
     */
    public boolean getCanSuggestGlobalPublish() {
       return (globalState == STATE_UNPUBLISHED);
    }
 
    /**
     * This method doesn't do any authz, it simply checks the state
     *
     * @return true, if sad can be published globally
     */
    public boolean getCanApproveGlobalPublish() {
       return (globalState == STATE_WAITING_APPROVAL);
    }
 
    public boolean isGlobal() {
       //TODO fix this stubbed out method
       return false;
    }
 
    public int compareTo(Object o) {
       StructuredArtifactDefinitionBean that =
             (StructuredArtifactDefinitionBean) o;
       return this.getType().getDescription().toLowerCase().compareTo(that.getType().getDescription().toLowerCase());
    }
 
    public String getFilePickerAction() {
       return filePickerAction;
    }
 
    public void setFilePickerAction(String filePickerAction) {
       this.filePickerAction = filePickerAction;
    }
 
    public String getSchemaHash() {
       return schemaHash;
    }
 
    public void setSchemaHash(String schemaHash) {
       this.schemaHash = schemaHash;
    }
 
    public boolean isPublished() {
       return getGlobalState() == STATE_PUBLISHED || getSiteState() == STATE_PUBLISHED;
    }
 
    public String getDecoratedDescription() {
       if (decoratedDescription == null) {
          decoratedDescription = getDescription() + calcSiteName();
       }
       return decoratedDescription;
    }
 
    protected String calcSiteName() {
       if (siteId == null) {
          return "";
       }
 
       Site site = null;
       try {
          site = SiteService.getSite(siteId);
       } catch (IdUnusedException ignore) {
          return "";
       }
 
      return " (" + site.getTitle() + ")";
    }
 
    public void setDecoratedDescription(String decoratedDescription) {
       this.decoratedDescription = decoratedDescription;
    }
 }
