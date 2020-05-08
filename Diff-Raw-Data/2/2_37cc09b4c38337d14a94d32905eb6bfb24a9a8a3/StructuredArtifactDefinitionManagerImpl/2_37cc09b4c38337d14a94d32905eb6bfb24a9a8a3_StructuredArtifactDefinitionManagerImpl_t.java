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
 
 package org.sakaiproject.metaobj.shared.mgt.impl;
 
 import org.jdom.CDATA;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.XMLOutputter;
 import org.sakaiproject.authz.cover.FunctionManager;
 import org.sakaiproject.component.cover.ComponentManager;
 import org.sakaiproject.component.cover.ServerConfigurationService;
 import org.sakaiproject.content.api.ContentHostingService;
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.exception.*;
 import org.sakaiproject.metaobj.security.AuthenticationManager;
 import org.sakaiproject.metaobj.security.AuthorizationFacade;
 import org.sakaiproject.metaobj.shared.ArtifactFinder;
 import org.sakaiproject.metaobj.shared.DownloadableManager;
 import org.sakaiproject.metaobj.shared.SharedFunctionConstants;
 import org.sakaiproject.metaobj.shared.mgt.IdManager;
 import org.sakaiproject.metaobj.shared.mgt.PresentableObjectHome;
 import org.sakaiproject.metaobj.shared.mgt.ReadableObjectHome;
 import org.sakaiproject.metaobj.shared.mgt.StructuredArtifactDefinitionManager;
 import org.sakaiproject.metaobj.shared.mgt.home.StructuredArtifactDefinition;
 import org.sakaiproject.metaobj.shared.mgt.home.StructuredArtifactHomeInterface;
 import org.sakaiproject.metaobj.shared.model.*;
 import org.sakaiproject.metaobj.utils.xml.SchemaFactory;
 import org.sakaiproject.metaobj.utils.xml.SchemaNode;
 import org.sakaiproject.metaobj.worksite.mgt.WorksiteManager;
 import org.sakaiproject.service.legacy.resource.DuplicatableToolService;
 import org.sakaiproject.site.api.Site;
 import org.sakaiproject.tool.api.Placement;
 import org.sakaiproject.tool.api.ToolManager;
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 
 import javax.xml.transform.TransformerException;
 import java.io.*;
 import java.util.*;
 import java.util.zip.*;
 
 
 /**
  * @author chmaurer
  * @author jbush
  */
 public class StructuredArtifactDefinitionManagerImpl extends HibernateDaoSupport
       implements StructuredArtifactDefinitionManager, DuplicatableToolService, DownloadableManager {
 
    static final private String DOWNLOAD_FORM_ID_PARAM = "formId";
 
    private AuthorizationFacade authzManager = null;
    private IdManager idManager;
    private WorksiteManager worksiteManager;
    private ContentHostingService contentHosting;
    private ToolManager toolManager;
    private List globalSites;
    private List globalSiteTypes;
    private ArtifactFinder artifactFinder;
    private int expressionMax = 999;
 
    public StructuredArtifactDefinitionManagerImpl() {
    }
 
    public Map getHomes() {
       Map returnMap = new HashMap();
       List list = findHomes();
       for (Iterator iter = list.iterator(); iter.hasNext();) {
          StructuredArtifactDefinitionBean sad = (StructuredArtifactDefinitionBean) iter.next();
          returnMap.put(sad.getId().getValue(), sad);
       }
 
       return returnMap;
    }
 
    /**
     * @param worksiteId
     * @return a map with all worksite and global homes
     */
    public Map getWorksiteHomes(Id worksiteId) {
       Map returnMap = new HashMap();
       List list = findGlobalHomes();
       list.addAll(findHomes(worksiteId));
       for (Iterator iter = list.iterator(); iter.hasNext();) {
          StructuredArtifactDefinitionBean sad = (StructuredArtifactDefinitionBean) iter.next();
          returnMap.put(sad.getId().getValue(), sad);
       }
 
       return returnMap;
    }
 
    /**
     * @return list of published sads or sads owned by current user
     */
    public List findHomes() {
       // only for the appropriate worksites
       List sites = getWorksiteManager().getUserSites();
       List returned = new ArrayList();
       while (sites.size() > getExpressionMax()) {
          returned.addAll(findHomes(sites.subList(0, getExpressionMax() - 1), false));
          sites.subList(0, getExpressionMax() - 1).clear();
       }
       returned.addAll(findHomes(sites, true));
       return returned;
    }
 
    protected List findHomes(List sites, boolean includeGlobal) {
       String query;
       Object[] params;
 
       if (includeGlobal) {
          query = "from StructuredArtifactDefinitionBean where owner = ? or globalState = ? or (siteState = ?  and siteId in (";
          params = new Object[]{getAuthManager().getAgent(),
                                new Integer(StructuredArtifactDefinitionBean.STATE_PUBLISHED),
                                new Integer(StructuredArtifactDefinitionBean.STATE_PUBLISHED)};
       }
       else {
          query = "from StructuredArtifactDefinitionBean where owner != ? and (siteState = ?  and siteId in (";
          params = new Object[]{getAuthManager().getAgent(),
                                new Integer(StructuredArtifactDefinitionBean.STATE_PUBLISHED)};
       }
 
       for (Iterator i = sites.iterator(); i.hasNext();) {
          Site site = (Site) i.next();
          query += "'" + site.getId() + "'";
          query += ",";
       }
 
       query += "''))";
 
       return getHibernateTemplate().find(query, params);
    }
 
    /**
     * @return list of all published globals or global sad owned by current user or waiting for approval
     */
    public List findGlobalHomes() {
       Object[] params = new Object[]{new Integer(StructuredArtifactDefinitionBean.STATE_PUBLISHED),
                                     getAuthManager().getAgent()};
       return getHibernateTemplate().findByNamedQuery("findGlobalHomes", params);
    }
 
    /**
     * @param currentWorksiteId
     * @return list of globally published sads or published sad in currentWorksiteId or sads in
     *         currentWorksiteId owned by current user
     */
    public List findHomes(Id currentWorksiteId) {
       Object[] params = new Object[]{new Integer(StructuredArtifactDefinitionBean.STATE_PUBLISHED),
                                      currentWorksiteId.getValue(),
                                      getAuthManager().getAgent(),
                                      new Integer(StructuredArtifactDefinitionBean.STATE_PUBLISHED)};
       return getHibernateTemplate().findByNamedQuery("findHomes", params);
    }
 
    public StructuredArtifactDefinitionBean loadHome(String type) {
       return loadHome(getIdManager().getId(type));
    }
 
    public StructuredArtifactDefinitionBean loadHome(Id id) {
       return (StructuredArtifactDefinitionBean) getHibernateTemplate().get(StructuredArtifactDefinitionBean.class, id);
    }
 
    public StructuredArtifactDefinitionBean loadHomeByExternalType(String externalType, Id worksiteId) {
       List homes = (List) getHibernateTemplate().findByNamedQuery("loadHomeByExternalType", new Object[]{
                externalType, new Integer(StructuredArtifactDefinitionBean.STATE_PUBLISHED),
                worksiteId.getValue()});
 
       if (homes.size() == 0) {
          return null;
       }
 
       if (homes.size() == 1) {
          return (StructuredArtifactDefinitionBean) homes.get(0);
       }
       else {
          for (Iterator i = homes.iterator(); i.hasNext();) {
             StructuredArtifactDefinitionBean def = (StructuredArtifactDefinitionBean) i.next();
             if (def.getSiteId() != null) {
                if (def.getSiteId().equals(worksiteId.getValue())) {
                   return def;
                }
             }
          }
          return (StructuredArtifactDefinitionBean) homes.get(0);
       }
    }
 
    public StructuredArtifactDefinitionBean save(StructuredArtifactDefinitionBean bean) {
       return save(bean, true);
    }
 
    public StructuredArtifactDefinitionBean save(StructuredArtifactDefinitionBean bean, boolean updateModTime) {
       if (!sadExists(bean)) {
          if (updateModTime) {
             bean.setModified(new Date(System.currentTimeMillis()));
          }
 
          boolean loadSchema = false;
 
          StructuredArtifactDefinition sad = null;
          try {
             if (bean.getId() == null) {
                loadSchema = true;
                loadNode(bean);
                bean.setCreated(new Date(System.currentTimeMillis()));
             }
             else if (bean.getSchemaFile() != null) {
                loadSchema = true;
                loadNode(bean);
                sad = new StructuredArtifactDefinition(bean);
                updateExistingArtifacts(sad);
             }
          }
          catch (Exception e) {
             throw new OspException("Invlaid schema", e);
          }
          sad = new StructuredArtifactDefinition(bean);
          bean.setExternalType(sad.getExternalType());
          bean.setSchemaHash(calculateSchemaHash(bean));
          getHibernateTemplate().saveOrUpdate(bean);
          //         getHibernateTemplate().saveOrUpdateCopy(bean);
       }
       else {
          throw new PersistenceException("Form name {0} exists", new Object[]{bean.getDescription()}, "description");
       }
       return bean;
    }
 
    public void delete(StructuredArtifactDefinitionBean sad) {
       if (sad.isPublished()) {
          throw new PersistenceException("unable_to_delete_published", new Object[]{}, "siteState");
       }
       getHibernateTemplate().delete(sad);
    }
 
    /**
     * @return Returns the idManager.
     */
    public IdManager getIdManager() {
       return idManager;
    }
 
    /**
     * @param idManager The idManager to set.
     */
    public void setIdManager(IdManager idManager) {
       this.idManager = idManager;
    }
 
    public boolean isGlobal() {
       String siteId = getWorksiteManager().getCurrentWorksiteId().getValue();
       return isGlobal(siteId);
    }
 
    protected boolean isGlobal(String siteId) {
 
       if (getGlobalSites().contains(siteId)) {
          return true;
       }
 
       Site site = getWorksiteManager().getSite(siteId);
       if (site.getType() != null && getGlobalSiteTypes().contains(site.getType())) {
          return true;
       }
 
       return false;
    }
 
    protected Site getCurrentSite() {
       String siteId = getWorksiteManager().getCurrentWorksiteId().getValue();
       return getWorksiteManager().getSite(siteId);
    }
 
    public Collection getRootElements(StructuredArtifactDefinitionBean sad) {
       try {
          SchemaNode node = loadNode(sad);
          return node.getRootChildren();
       }
       catch (Exception e) {
          throw new OspException("Invalid schema.", e);
       }
    }
 
    public void validateSchema(StructuredArtifactDefinitionBean sad) {
       SchemaNode node = null;
 
       try {
          node = loadNode(sad);
       }
       catch (Exception e) {
          throw new OspException("Invlid schema file.", e);
       }
 
       if (node == null) {
          throw new OspException("Invlid schema file.");
       }
    }
 
    public StructuredArtifactHomeInterface convertToHome(StructuredArtifactDefinitionBean sad) {
       return new StructuredArtifactDefinition(sad);
    }
 
    protected SchemaNode loadNode(StructuredArtifactDefinitionBean sad)
          throws TypeException, IdUnusedException, PermissionException, ServerOverloadException {
       if (sad.getSchemaFile() != null) {
          ContentResource resource = getContentHosting().getResource(sad.getSchemaFile().getValue());
          sad.setSchema(resource.getContent());
       }
 
       if (sad.getSchema() == null) {
          return null;
       }
 
       SchemaFactory schemaFactory = SchemaFactory.getInstance();
       return schemaFactory.getSchema(new ByteArrayInputStream(sad.getSchema()));
    }
 
    protected boolean sadExists(StructuredArtifactDefinitionBean sad) throws PersistenceException {
       String query = "from StructuredArtifactDefinitionBean where description = ? ";
       List params = new ArrayList();
       params.add(sad.getDescription());
 
       if (sad.getId() != null) {
          query += " and id != ? ";
          params.add(sad.getId());
       }
 
       if (sad.getSiteId() != null) {
          query += " and siteId = ? ";
          params.add(sad.getSiteId());
       }
       else {
          query += " and siteId is null";
       }
 
       List sads = getHibernateTemplate().find(query, params.toArray());
 
       return sads.size() > 0;
    }
 
    /**
     * @param sad
     * @param artifact
     * @throws OspException if artifact doesn't validate
     */
    protected void validateAfterTransform(StructuredArtifactDefinition sad, StructuredArtifact artifact) throws OspException {
       //TODO figure out how to do the validator
 //      StructuredArtifactValidator validator = new StructuredArtifactValidator();
 //      artifact.setHome(sad);
 //      Errors artifactErrors = new BindExceptionBase(artifact, "bean");
 //      validator.validate(artifact, artifactErrors);
 //      if (artifactErrors.getErrorCount() > 0) {
 //         StringBuffer buf = new StringBuffer();
 //         for (Iterator i=artifactErrors.getAllErrors().iterator();i.hasNext();){
 //            ObjectError error = (ObjectError) i.next();
 //            buf.append(error.toString() + " ");
 //         }
 //         throw new OspException(buf.toString());
 //      }
    }
 
    protected void saveAll(StructuredArtifactDefinition sad, Collection artifacts) {
       for (Iterator i = artifacts.iterator(); i.hasNext();) {
          StructuredArtifact artifact = (StructuredArtifact) i.next();
          try {
             sad.store(artifact);
          }
          catch (PersistenceException e) {
             logger.error("problem saving artifact with id " + artifact.getId().getValue() + ":" + e);
          }
       }
    }
 
    /**
     * Uses the submitted xsl file to transform the existing artifacts into the schema.
     * This process puts the artifact home into system only start while is does its work.
     * This is necessary so that users won't be able to update artifacts while this is going on.
     * The system transforms every object in memory and validates before writing any artifact back out.
     * This way if something fails the existing data will stay intact.
     * <p/>
     * TODO possible memory issues
     * TODO all this work need to be atomic
     *
     * @param sad
     * @throws OspException
     */
    protected void updateExistingArtifacts(StructuredArtifactDefinition sad) throws OspException {
 
       //if we don't have an xsl file and don't need one, return
       if (!sad.getRequiresXslFile()) {
          return;
       }
 
       if (sad.getRequiresXslFile() && (sad.getXslConversionFileId() == null || sad.getXslConversionFileId().getValue().length() == 0)) {
          throw new OspException("xsl conversion file required");
       }
 
       // put artifact home in system only state while we do this work.
       // this along with repository authz prevents someone from updating an artifact
       // while this is going on
       StructuredArtifactDefinitionBean currentHome = this.loadHome(sad.getId());
       boolean originalSystemOnlyState = currentHome.isSystemOnly();
       currentHome.setSystemOnly(true);
       getHibernateTemplate().saveOrUpdate(currentHome);
 
       boolean finished = false;
       String type = sad.getType().getId().getValue();
       //ArtifactFinder artifactFinder = getArtifactFinderManager().getArtifactFinderByType(type);
       Collection artifacts = getArtifactFinder().findByType(type);
       Collection modifiedArtifacts = new ArrayList();
 
       // perform xsl transformations on existing artifacts
       try {
          for (Iterator i = artifacts.iterator(); i.hasNext();) {
             StructuredArtifact artifact = (StructuredArtifact) i.next();
             try {
                transform(sad, artifact);
                validateAfterTransform(sad, artifact);
                // don't persist yet, in case error is found in some other artifact
                modifiedArtifacts.add(artifact);
             }
             catch (TransformerException e) {
                throw new OspException("problem transforming item with id=" + artifact.getId().getValue(), e);
             }
             catch (IOException e) {
                throw new OspException(e);
             }
             catch (JDOMException e) {
                throw new OspException("problem with xsl file: " + e.getMessage(), e);
             }
          }
          finished = true;
       } finally {
          // reset systemOnly state back to whatever if was
          // but only if there was an error
          if (!originalSystemOnlyState && !finished) {
             currentHome.setSystemOnly(false);
             getHibernateTemplate().saveOrUpdate(currentHome);
          }
       }
 
       // since all artifacts validated go ahead and persist changes
       saveAll(sad, modifiedArtifacts);
    }
 
    protected Element getStructuredArtifactRootElement(StructuredArtifactDefinition sad, StructuredArtifact artifact) {
       return sad.getArtifactAsXml(artifact).getChild("structuredData").getChild(sad.getRootNode());
    }
 
    protected void transform(StructuredArtifactDefinition sad, StructuredArtifact artifact) throws IOException, TransformerException, JDOMException {
       /* todo transform
       logger.debug("transforming artifact " + artifact.getId().getValue() + " owned by " + artifact.getOwner().getDisplayName());
       JDOMResult result = new JDOMResult();
       SAXBuilder builder = new SAXBuilder();
       Document xslDoc = builder.build(sad.getXslConversionFileStream());
       Transformer transformer = TransformerFactory.newInstance().newTransformer(new JDOMSource(xslDoc));
       Element rootElement = getStructuredArtifactRootElement(sad, artifact);
 
       transformer.transform(new JDOMSource(rootElement), result);
 
       artifact.setBaseElement((Element) result.getResult().get(0));
       */
    }
 
    public AuthenticationManager getAuthManager() {
       return (AuthenticationManager) ComponentManager.getInstance().get("authManager");
    }
 
    public AuthorizationFacade getAuthzManager() {
       return authzManager;
    }
 
    public void setAuthzManager(AuthorizationFacade authzManager) {
       this.authzManager = authzManager;
    }
 
    public WorksiteManager getWorksiteManager() {
       return worksiteManager;
    }
 
    public void setWorksiteManager(WorksiteManager worksiteManager) {
       this.worksiteManager = worksiteManager;
    }
 
    public ToolManager getToolManager() {
       return toolManager;
    }
 
    public void setToolManager(ToolManager toolManager) {
       this.toolManager = toolManager;
    }
 
    protected Id getToolId() {
       Placement placement = toolManager.getCurrentPlacement();
       return idManager.getId(placement.getId());
    }
 
    public void importResources(String fromContext, String toContext, List resourceIds) {
       // select all this worksites forms and create them for the new worksite
       Map homes = getWorksiteHomes(getIdManager().getId(fromContext));
 
       for (Iterator i = homes.entrySet().iterator(); i.hasNext();) {
          Map.Entry entry = (Map.Entry) i.next();
          StructuredArtifactDefinitionBean bean = (StructuredArtifactDefinitionBean) entry.getValue();
 
          if (fromContext.equals(bean.getSiteId())) {
             getHibernateTemplate().evict(bean);
             bean.setSiteId(toContext);
             bean.setId(null);
 
             //Check for an existing form
             if (findBean(bean) == null) {
                getHibernateTemplate().save(bean);
             }
 
             //            getHibernateTemplate().saveOrUpdateCopy(bean);
          }
       }
    }
 
    public ContentHostingService getContentHosting() {
       return contentHosting;
    }
 
    public void setContentHosting(ContentHostingService contentHosting) {
       this.contentHosting = contentHosting;
    }
 
    protected void init() throws Exception {
       // register functions
       FunctionManager.registerFunction(SharedFunctionConstants.CREATE_ARTIFACT_DEF);
       FunctionManager.registerFunction(SharedFunctionConstants.EDIT_ARTIFACT_DEF);
       FunctionManager.registerFunction(SharedFunctionConstants.EXPORT_ARTIFACT_DEF);
       FunctionManager.registerFunction(SharedFunctionConstants.DELETE_ARTIFACT_DEF);
       FunctionManager.registerFunction(SharedFunctionConstants.PUBLISH_ARTIFACT_DEF);
       FunctionManager.registerFunction(SharedFunctionConstants.SUGGEST_GLOBAL_PUBLISH_ARTIFACT_DEF);
       updateSchemaHash();
    }
 
    protected void updateSchemaHash() {
       List forms = getHibernateTemplate().findByNamedQuery("findByNullSchemaHash");
 
       for (Iterator i = forms.iterator(); i.hasNext();) {
          StructuredArtifactDefinitionBean bean = (StructuredArtifactDefinitionBean) i.next();
          bean.setSchemaHash(calculateSchemaHash(bean));
          getHibernateTemplate().saveOrUpdate(bean);
 
          //         getHibernateTemplate().saveOrUpdateCopy(bean);
       }
    }
 
    protected String calculateSchemaHash(StructuredArtifactDefinitionBean bean) {
       String hashString = "";
       if (bean.getSchema() != null) {
          hashString += new String(bean.getSchema());
       }
       hashString += bean.getDocumentRoot();
       hashString += bean.getDescription();
       hashString += bean.getInstruction();
       return hashString.hashCode() + "";
    }
 
    public String packageForDownload(Map params, OutputStream out) throws IOException {
 
       String[] formIdObj = (String[]) params.get(DOWNLOAD_FORM_ID_PARAM);
       packageFormForExport(formIdObj[0], out);
       
       //Blank filename for now -- no more dangerous, since the request is in the form of a filename
       return "";
    }
 
    
    /**
     * This is the default method for exporting a form into a stream.  This method does check the
     * form export permission.
     * @param formId String
     * @param os OutputStream
     * @throws IOException
     */
    public void packageFormForExport(String formId, OutputStream os)
          throws IOException {
       packageFormForExport(formId, os, true);
    }
 
 
    /**
     * This method will export a form into a stream.  It has the ability to turn off checking
     * for the export form permission.
     * @param formId String
     * @param os OutputStream
     * @param checkPermission boolean
     * @throws IOException
     */
    public void packageFormForExport(String formId, OutputStream os, boolean checkPermission)
          throws IOException {
       if (checkPermission) {
          getAuthzManager().checkPermission(SharedFunctionConstants.EXPORT_ARTIFACT_DEF,
             getToolId());
       }
 
       CheckedOutputStream checksum = new CheckedOutputStream(os,
             new Adler32());
       ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(checksum));
 
       StructuredArtifactDefinitionBean bean = loadHome(formId);
       writeSADtoZip(bean, zos, "");
 
       zos.finish();
       zos.flush();
    }
 
    /**
     * Given a bean this method will convert it into a new XML document.
     * This does not put the schema into XML
     * @param bean StructuredArtifactDefinitionBean
     * @return Document - XML
     */
    public Document exportSADAsXML(StructuredArtifactDefinitionBean bean) {
       Element rootNode = new Element("metaobjForm");
 
       rootNode.setAttribute("formatVersion", "2.1");
 
       Element attrNode = new Element("description");
       attrNode.addContent(new CDATA(bean.getDescription()));
       rootNode.addContent(attrNode);
 
       attrNode = new Element("instruction");
       attrNode.addContent(new CDATA(bean.getInstruction()));
       rootNode.addContent(attrNode);
 
       attrNode = new Element("documentRootNode");
       attrNode.addContent(new CDATA(bean.getDocumentRoot()));
       rootNode.addContent(attrNode);
 
       return new Document(rootNode);
    }
 
    
    /**
     * Given a bean, this method puts it into a stream via UTF-8 encoding
     * @param bean StructuredArtifactDefinitionBean
     * @param os OutputStream
     * @throws IOException
     */
    public void writeSADasXMLtoStream(StructuredArtifactDefinitionBean bean, OutputStream os) throws IOException {
       Document doc = exportSADAsXML(bean);
       String docStr = (new XMLOutputter()).outputString(doc);
       os.write(docStr.getBytes("UTF-8"));
    }
 
    public void writeSADtoZip(StructuredArtifactDefinitionBean bean, ZipOutputStream zos) throws IOException {
       writeSADtoZip(bean, zos, "");
    }
 
    public void writeSADtoZip(StructuredArtifactDefinitionBean bean, ZipOutputStream zos, String path) throws IOException {
       // if the path is a directory without an end slash, then add one
       if (!path.endsWith("/") && path.length() > 0) {
          path += "/";
       }
       ZipEntry definitionFile = new ZipEntry(path + "formDefinition.xml");
 
       zos.putNextEntry(definitionFile);
       writeSADasXMLtoStream(bean, zos);
       zos.closeEntry();
 
       ZipEntry schemeFile = new ZipEntry(path + "schema.xsd");
 
       zos.putNextEntry(schemeFile);
       zos.write(bean.getSchema());
       zos.closeEntry();
 
    }
 
    /**
     * Given a resource id, this parses out the Form from its input stream.
     * Once the enties are found, they are inserted into the given worksite.
     *
     * @param worksiteId   Id
     * @param resourceId   an String
     * @param findExisting
     */
    public boolean importSADResource(Id worksiteId, String resourceId, boolean findExisting)
          throws IOException, ServerOverloadException, PermissionException, 
                IdUnusedException, ImportException, UnsupportedFileTypeException
          {
       String id = getContentHosting().resolveUuid(resourceId);
 
       try {
          ContentResource resource = getContentHosting().getResource(id);
          MimeType mimeType = new MimeType(resource.getContentType());
 
          if (mimeType.equals(new MimeType("application/zip")) ||
                mimeType.equals(new MimeType("application/x-zip-compressed"))) {
             InputStream zipContent = resource.streamContent();
             StructuredArtifactDefinitionBean bean = importSad(worksiteId, zipContent, findExisting, false);
 
             return bean != null;
          }
          else {
             throw new UnsupportedFileTypeException("The import file must be a zip file.");
          }
       }
       catch (TypeException te) {
          logger.error(te);
       }
       return false;
    }
 
    public StructuredArtifactDefinitionBean importSad(Id worksiteId, InputStream in,
                                                      boolean findExisting, boolean publish)
          throws IOException, ImportException {
       return importSad(worksiteId, in, findExisting, publish, true);
    }
    public StructuredArtifactDefinitionBean importSad(Id worksiteId, InputStream in,
                                                      boolean findExisting, boolean publish, boolean foundThrowsException)
          throws IOException, ImportException {
       ZipInputStream zis = new ZipInputStream(in);
 
       StructuredArtifactDefinitionBean bean = readSADfromZip(zis, worksiteId.getValue(), publish);
       if (bean != null) {
          if (findExisting) {
             StructuredArtifactDefinitionBean found = findBean(bean);
             if (found != null) {
                if (foundThrowsException) {
                   throw new ImportException("The Form being imported already exists and has been published");
                } else {
                   return found;
                }
             }
          }
 
          String origTitle = bean.getDescription();
          int index = 0;
          while (sadExists(bean)) {
             index++;
             bean.setDescription(origTitle + " " + index);
          }
 
          save(bean);
          // doesn't like imported beans in batch mode???
          getHibernateTemplate().flush();
       }
       return bean;
    }
 
    protected StructuredArtifactDefinitionBean findBean(StructuredArtifactDefinitionBean bean) {
       Object[] params = new Object[]{new Integer(StructuredArtifactDefinitionBean.STATE_PUBLISHED),
                                      new Integer(StructuredArtifactDefinitionBean.STATE_PUBLISHED),
                                      bean.getSiteId(), bean.getSchemaHash()};
       List beans = getHibernateTemplate().findByNamedQuery("findBean", params);
 
       if (beans.size() > 0) {
          return (StructuredArtifactDefinitionBean) beans.get(0);
       }
       return null;
    }
 
 
    public StructuredArtifactDefinitionBean readSADfromZip(ZipInputStream zis,
                                                           String worksite, boolean publish)
          throws IOException {
       StructuredArtifactDefinitionBean bean = new StructuredArtifactDefinitionBean();
       boolean hasXML = false, hasXSD = false;
 
       bean.setCreated(new Date(System.currentTimeMillis()));
       bean.setModified(bean.getCreated());
 
       bean.setOwner(getAuthManager().getAgent());
       bean.setSiteId(worksite);
       bean.setSiteState(publish ? StructuredArtifactDefinitionBean.STATE_PUBLISHED :
             StructuredArtifactDefinitionBean.STATE_UNPUBLISHED);
 
       if (isGlobal(worksite)) {
          bean.setGlobalState(publish ? StructuredArtifactDefinitionBean.STATE_PUBLISHED :
             StructuredArtifactDefinitionBean.STATE_UNPUBLISHED);
          bean.setSiteId(null);
       }
 
       ZipEntry currentEntry = zis.getNextEntry();
 
       if (currentEntry == null) {
          return null;
       }
       
       // If the zip was opened and re-zipped, then the directory was
       //    compressed with the files.  we need to deal with 
       //    the directory
       if(currentEntry.getName().endsWith("/")) {
          zis.closeEntry();
          currentEntry = zis.getNextEntry();
       }
       
       if (currentEntry != null) {
          if (currentEntry.getName().endsWith("xml")) {
             readSADfromXML(bean, zis);
             hasXML = true;
          }
          if (currentEntry.getName().endsWith("xsd")) {
             readSADSchemaFromXML(bean, zis);
             hasXSD = true;
          }
          zis.closeEntry();
       }
       currentEntry = zis.getNextEntry();
       if (currentEntry != null) {
          if (currentEntry.getName().endsWith("xml")) {
             readSADfromXML(bean, zis);
             hasXML = true;
          }
          if (currentEntry.getName().endsWith("xsd")) {
             readSADSchemaFromXML(bean, zis);
             hasXSD = true;
          }
          zis.closeEntry();
       }
       if (!hasXML || !hasXSD) {
          return null;
       }
 
       bean.setSchemaHash(calculateSchemaHash(bean));
       return bean;
    }
 
    private StructuredArtifactDefinitionBean readSADfromXML(StructuredArtifactDefinitionBean bean, InputStream inStream) {
       SAXBuilder builder = new SAXBuilder();
 
       try {
          byte[] bytes = readStreamToBytes(inStream);
          //  for some reason the SAX Builder sometimes won't recognize
          //these bytes as correct utf-8 characters.  So we want to read it in
          //as utf-8 and spot it back out as utf-8 and this will correct the
          //bytes.  In my test, it added two bytes somewhere in the string.
          //and adding those two bytes made the string work for saxbuilder.
          //
          bytes = (new String(bytes, "UTF-8")).getBytes("UTF-8");
          Document document = builder.build(new ByteArrayInputStream(bytes));
 
          Element topNode = document.getRootElement();
 
          bean.setDescription(new String(topNode.getChildTextTrim("description").getBytes(), "UTF-8"));
          bean.setInstruction(new String(topNode.getChildTextTrim("instruction").getBytes(), "UTF-8"));
          bean.setDocumentRoot(new String(topNode.getChildTextTrim("documentRootNode").getBytes(), "UTF-8"));
       }
       catch (Exception jdome) {
          logger.error(jdome);
          throw new RuntimeException(jdome);
       }
       return bean;
    }
 
    private byte[] readStreamToBytes(InputStream inStream) throws IOException {
       ByteArrayOutputStream bytes = new ByteArrayOutputStream();
       byte data[] = new byte[10 * 1024];
 
       int count;
       while ((count = inStream.read(data, 0, 10 * 1024)) != -1) {
          bytes.write(data, 0, count);
       }
       byte[] tmp = bytes.toByteArray();
       bytes.close();
       return tmp;
    }
 
    private StructuredArtifactDefinitionBean readSADSchemaFromXML(StructuredArtifactDefinitionBean bean, InputStream inStream) throws IOException {
       bean.setSchema(readStreamToBytes(inStream));
       return bean;
    }
 
    public List getGlobalSites() {
       return globalSites;
    }
 
    public void setGlobalSites(List globalSites) {
       this.globalSites = globalSites;
    }
 
    public List getGlobalSiteTypes() {
       return globalSiteTypes;
    }
 
    public void setGlobalSiteTypes(List globalSiteTypes) {
       this.globalSiteTypes = globalSiteTypes;
    }
 
    public Element createFormViewXml(String formId, String returnUrl) {
       formId = getContentHosting().getUuid(formId);
       Artifact art = getArtifactFinder().load(getIdManager().getId(formId));
       Element root = new Element("formView");
       Element data = new Element("formData");
 
       ReadableObjectHome home = (ReadableObjectHome) art.getHome();
       if (home instanceof PresentableObjectHome) {
          data.addContent(((PresentableObjectHome) home).getArtifactAsXml(art));
       }
 
       root.addContent(data);
 
       if (returnUrl != null) {
          Element returnUrlElement = new Element("returnUrl");
          returnUrlElement.addContent(new CDATA(returnUrl));
          root.addContent(returnUrlElement);
       }
 
       Element css = new Element("css");
       String skin = null;
       try {
          skin = getCurrentSite().getSkin();
       }
       catch (NullPointerException npe) {
          //Couldn't find the site, just use default skin
       }
       if (skin == null || skin.length() == 0) {
          skin = ServerConfigurationService.getString("skin.default");
       }
       String skinRepo = ServerConfigurationService.getString("skin.repo");
       Element uri = new Element("uri");
       uri.setText(skinRepo + "/tool_base.css");
       css.addContent(uri);
       uri = new Element("uri");
       uri.setText(skinRepo + "/" + skin + "/tool.css");
       css.addContent(uri);
       root.addContent(css);
       return root;
    }
 
    public ArtifactFinder getArtifactFinder() {
       return artifactFinder;
    }
 
    public void setArtifactFinder(ArtifactFinder artifactFinder) {
       this.artifactFinder = artifactFinder;
    }
 
    public int getExpressionMax() {
       return expressionMax;
    }
 
    public void setExpressionMax(int expressionMax) {
       this.expressionMax = expressionMax;
    }
 
 }
