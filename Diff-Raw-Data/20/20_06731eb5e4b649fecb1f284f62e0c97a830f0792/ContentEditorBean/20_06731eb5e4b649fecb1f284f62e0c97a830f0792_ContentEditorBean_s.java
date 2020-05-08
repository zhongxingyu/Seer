 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2007
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/copyleft/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.war.beans.admin.content;
 
 import com.flexive.faces.FxJsfUtils;
 import com.flexive.faces.beans.ActionBean;
 import com.flexive.faces.messages.FxFacesMsgErr;
 import com.flexive.faces.messages.FxFacesMsgInfo;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.FxArrayUtils;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.content.*;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.interfaces.ContentEngine;
 import com.flexive.shared.interfaces.TreeEngine;
 import com.flexive.shared.security.ACL;
 import com.flexive.shared.security.UserTicket;
 import com.flexive.shared.structure.*;
 import com.flexive.shared.tree.FxTreeMode;
 import com.flexive.shared.tree.FxTreeNode;
 import com.flexive.shared.tree.FxTreeNodeEdit;
 import com.flexive.shared.value.FxBinary;
 import com.flexive.shared.value.FxString;
 import com.flexive.shared.workflow.Step;
 import com.flexive.shared.workflow.StepDefinition;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.myfaces.custom.fileupload.UploadedFile;
 
 import javax.faces.event.ActionEvent;
 import javax.faces.model.SelectItem;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 
 public class ContentEditorBean implements ActionBean, Serializable {
     private static final Log LOG = LogFactory.getLog(ContentEditorBean.class);
 
     private ContentEngine co;
     protected TreeEngine tree;
     private List<FxPropertyAssignment> properties;
     private List<FxGroupAssignment> groups;
     private FxContent content;
     private FxContentVersionInfo versionInfo;
     private CeDataWrapper data;
     private CeDisplayProvider displayProv;
     private CeAddElementOptions addElementOptions;
     private FxEnvironment environment;
     private String[] elements;
     private long type;
     private CeIdGenerator idGenerator;
     private FxData element;
     private ACL acl;
     private ArrayList<SelectItem> steps;
     private long id = -1;
     private int version = -1;
     private FxPK pk;
     private List<FxTreeNode> treeNodes;
     private boolean readOnly;
     private long treeNodeParent;
     private FxTreeNode treeNode;
     private Hashtable<String, String> fileInputValues = new Hashtable<String, String>(10);
     private String editorActionName;
     private String editorActionXpath;
     protected boolean isDummy;
 
     public String getEditorType() {
         return "NORMAL";
     }
 
     public void setEditorType(String editorType) {
         // dummy
     }
 
     protected String getEditorPage() {
         return "contentEditor";
     }
 
     protected String getSessionCacheId() {
         return this.getClass().getName();
     }
 
     public ContentEditorBean() {
         if (isDummy) return;
         try {
             co = EJBLookup.getContentEngine();
             tree = EJBLookup.getTreeEngine();
             environment = CacheAdmin.getFilteredEnvironment();
             FxJsfUtils.getSession().setAttribute(getSessionCacheId(), this);
         } catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
     @SuppressWarnings("UnusedDeclaration")
     ContentEditorBean(boolean dummy) {
         // to create a singleton for lookups
         isDummy = true;
     }
 
     public static ContentEditorBean getSingleton() {
         return new ContentEditorBean(true);
     }
 
 
     public String getEditorActionXpath() {
         return "";//editorActionXpath;
     }
 
     public void setEditorActionXpath(String editorActionXpath) {
         this.editorActionXpath = editorActionXpath;
     }
 
     public void setEditorActionName(String editorActionName) {
         this.editorActionName = editorActionName;
     }
 
     public String getEditorActionName() {
         return "";//editorActionName;
     }
 
     public boolean isReadOnly() {
         return readOnly;
     }
 
     public void setReadOnly(boolean readOnly) {
         this.readOnly = readOnly;
     }
 
     private void processAction() {
         if (StringUtils.isBlank(editorActionName) || StringUtils.isBlank(editorActionXpath)) {
             return;
         }
         try {
             final List<FxData> data = content.getData(editorActionXpath);
             setElement(data.get(0));
             if (editorActionName.equalsIgnoreCase("addProperty")) {
                 addElement(null);
             } else if (editorActionName.equalsIgnoreCase("removeProperty")) {
                 removeElement(null);
             }
             editorActionName = null;
             editorActionXpath = null;
         } catch (Exception e) {
             LOG.warn("Failed to execute content editor action " + editorActionName + " for XPath "
                     + editorActionXpath + ": " + e.getMessage(), e);
         }
     }
 
 
     public void setBinary(String xpath, FxBinary binary) {
         try {
             FxBinary binProperty = new FxBinary(binary);
             content.setValue(xpath, binProperty);
         } catch (Throwable t) {
             System.err.println("Failed to set binary for xpath=" + xpath + ": " + t.getMessage());
         }
     }
 
     protected void finalize() throws Throwable {
         super.finalize();
         FxJsfUtils.getSession().removeAttribute(getSessionCacheId());
     }
 
     public ContentEngine getContentEngine() {
         return co;
     }
 
     public Hashtable<String, String> getFileInputValues() {
         return fileInputValues;
     }
 
     public void setFileInputValues(Hashtable<String, String> fileInputValues) {
         this.fileInputValues = fileInputValues;
     }
 
     /**
      * Returns the active content editor beans of the calling user session, or null id none is available at the call time
      *
      * @param request the request
      * @return the content editor beans, or null
      */
     public ContentEditorBean getInstance(HttpServletRequest request) {
         return _getInstance(request.getSession());
     }
 
     /**
      * Returns the active content editor beans of the calling jsf user session,
      * or null id none is available at the call time
      *
      * @return the content editor beans, or null
      */
     public ContentEditorBean getInstance() {
         return _getInstance(FxJsfUtils.getSession());
     }
 
     /**
      * Helper function.
      *
      * @param session the session
      * @return a reference to the content editor beans bound in the session.
      */
     private ContentEditorBean _getInstance(HttpSession session) {
         try {
             ContentEditorBean ceb = (ContentEditorBean) session.getAttribute(getSessionCacheId());
             if (ceb == null) {
                 // Fallback: look under the JSF name
                 ceb = (ContentEditorBean) session.getAttribute(getJsfAttributeName());
             }
             return ceb;
         } catch (Throwable t) {
             return null;
         }
     }
 
     /**
      * Returns the name of the beans, like JSF stores it in the session.
      *
      * @return the jsf name of the beans
      */
     protected String getJsfAttributeName() {
         String splitup[] = this.getClass().getName().split("\\.");
         // Take just the beans name, and convert the first char to lowercase
         String name = String.valueOf(this.getClass().getName().split("\\.")[splitup.length - 1].charAt(0)).toLowerCase();
         name += splitup[splitup.length - 1].substring(1);
         return name;
     }
 
     public String getUserCd() {
         try {
             long id = Long.valueOf(FxJsfUtils.getRequest().getAttribute("cdId").toString());
             int vers = Integer.valueOf(FxJsfUtils.getRequest().getAttribute("vers").toString());
             // type is always 'contactData' here...
             this.type = CacheAdmin.getFilteredEnvironment().getType(FxType.CONTACTDATA).getId();
             // clear fields...
             release();
             if (id != -1) {
                 this.id = id;
                 this.version = vers;
                 // init all necessary fields...as this.id is set no new contact data set will be created but the existing one loaded
                 _init();
                 //content = co.load(new FxPK(id, vers));
             }
         } catch (Exception e) {
             // TODO possibly pass some error message to the HTML page
             LOG.error("Failed to parse request parameters: " + e.getMessage(), e);
         }
         return null;
     }
 
 
     /**
      * {@inheritDoc}
      */
     public String getParseRequestParameters() throws FxApplicationException {
         processAction();    // execute from content editor javascript
         try {
             String action = FxJsfUtils.getParameter("action");
             if (StringUtils.isBlank(action)) {
                 return null;
             }
             if ("newInstance".equals(action)) {
                 long typeId = FxJsfUtils.getLongParameter("typeId", -1);
                 if (typeId == -1) {
                     typeId = CacheAdmin.getFilteredEnvironment().getType(FxJsfUtils.getParameter("typeName")).getId();
                 }
                 long nodeId = FxJsfUtils.getLongParameter("nodeId", -1);
                 release();
                 setType(typeId);
                 initNew();
                 if (nodeId != -1) {
                     setTreeNodeParent(nodeId);
                     addTreeNode(null);
                 }
             } else if ("editInstance".equals(action)) {
                 FxPK newPk;
                 if (FxJsfUtils.getParameter("pk") != null) {
                     String split[] = FxJsfUtils.getParameter("pk").split("\\.");
                     Long id = Long.valueOf(split[0].trim());
                     Integer ver = Integer.valueOf(split[1].trim());
                     newPk = new FxPK(id, ver);
                 } else {
                     newPk = new FxPK(FxJsfUtils.getLongParameter("id"), FxPK.MAX);
                 }
                 release();
                 setPk(newPk);
                 load();
                 this.readOnly = FxJsfUtils.getBooleanParameter("readOnly");
                 if (this.readOnly) {
                     compact();
                 }
             }
             // hack!
             FxJsfUtils.resetFaceletsComponent("frm");
         } catch (Throwable t) {
             // TODO possibly pass some error message to the HTML page
             LOG.error("Failed to parse request parameters: " + t.getMessage(), t);
         }
         return null;
     }
 
 
     UploadedFile file = null;
 
     public void setFiletest(UploadedFile file) {
         this.file = file;
     }
 
     public UploadedFile getFiletest() {
         return file;
     }
 
     public List<FxTreeNode> getTreeNodes() {
         return treeNodes;
     }
 
     public void setTreeNodes(List<FxTreeNode> treeNodes) {
         this.treeNodes = treeNodes;
     }
 
     public long getTreeNodeParent() {
         return treeNodeParent;
     }
 
     public void setTreeNodeParent(long treeNode) {
         this.treeNodeParent = treeNode;
     }
 
     public boolean isNew() {
         return content == null || this.content.getPk() == null || this.content.getPk().isNew();
     }
 
 
     public FxTreeNode getTreeNode() {
         return treeNode;
     }
 
     public void setTreeNode(FxTreeNode treeNode) {
         this.treeNode = treeNode;
     }
 
     /**
      * Ajax call for tree nodes.
      *
      * @param event the event
      */
     public void removeTreeNode(ActionEvent event) {
         if (readOnly) return;
         FxJsfUtils.resetFaceletsComponent("frm:treeNodes");
         for (FxTreeNode node : treeNodes) {
             if (node.getId() == treeNode.getId())
                 node.setMarkForDelete(true);
         }
     }
 
     /**
      * Ajax call for tree nodes.
      *
      * @param event the event
      */
     public void addTreeNode(ActionEvent event) {
         if (readOnly) return;
         FxJsfUtils.resetFaceletsComponent("frm:treeNodes");
         addTreeNode(treeNodeParent);
     }
 
     public void addTreeNode(long _node) {
         try {
             // Only add the path if it does not already exist
             for (FxTreeNode node : treeNodes) {
                 if (node.getParentNodeId() == _node) {
                     if (node.isMarkForDelete()) {
                         // was removed before .. just take it in again
                         node.setMarkForDelete(false);
                         return;
                     } else {
                         // exists
                         return;
                     }
                 }
             }
 
             // Add the path if the parent node is valid
             try {
                 FxTreeNode tn = tree.getNode(FxTreeMode.Edit, _node);
                 if (tn != null)
                     treeNodes.add(FxTreeNode.createNewTemporaryChildNode(tn));
             } catch (Throwable t) {
                 /* ignore */
             }
         } catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
     public void setType(long type) {
         this.type = type;
     }
 
     public String initNew() {
         release();
         _init();
         return null;
     }
 
     public FxPK getPk() {
         return pk;
     }
 
     public void setPk(FxPK pk) {
         this.pk = pk;
         if (pk != null) {
             this.id = pk.getId();
             this.version = pk.getVersion();
         }
     }
 
     public String load() {
         content = null;
         _init();
         return getEditorPage();
     }
 
     public String loadUserContent() {
         content = null;
         _init();
         return "showContentEditor";
     }
 
     public String reload() {
         content = null;
         _init();
         //return getEditorPage();
         FxJsfUtils.resetFaceletsComponent("frm:all");
         return null;
     }
 
 
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     public int getVersion() {
         return version;
     }
 
     public void setVersion(int version) {
         this.version = version;
     }
 
     public Boolean _init() {
         if (!isInitialized()) {
             try {
                 environment = CacheAdmin.getFilteredEnvironment();
                 if (id != -1) {
                    FxPK pk = (version == -1) ? new FxPK(id) : new FxPK(id, version);
                     // Load the content itself
                     content = co.load(pk);
                     content.loadReferences(co);
                     type = content.getTypeId();
                     version = content.getVersion();
                     versionInfo = co.getContentVersionInfo(pk);
                     // Load the tree nodes assigned to this content
                     initTreeData(pk.getId());
                 } else {
                     // Create a new empty content
                     content = co.initialize(type);
                     versionInfo = FxContentVersionInfo.createEmpty();
                     version = FxPK.MAX;
                     treeNodes = new ArrayList<FxTreeNode>(5);
                 }
                 properties = environment.getType(type).getAssignedProperties();
                 groups = environment.getType(type).getAssignedGroups();
                 acl = CacheAdmin.getEnvironment().getACL(content.getAclId());
                 data = new CeDataWrapper(this);
                 displayProv = new CeDisplayProvider(this);
                 idGenerator = new CeIdGenerator();
                 addElementOptions = new CeAddElementOptions(this);
                 _initSteps();
                 return true;
             } catch (Throwable t) {
                 release();
                 new FxFacesMsgErr(t).addToContext();
                 return null;
             }
         } else {
             return false;
         }
     }
 
     private void initTreeData(long _id) {
         // Load the tree nodes assigned to this content
         try {
             treeNodes = tree.getNodesWithReference(FxTreeMode.Edit, _id);
         } catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
     /**
      * Determines all possible steps for the current content.
      */
     public void _initSteps() {
         UserTicket ticket = FxContext.get().getTicket();
         FxType fxType = environment.getType(type);
 
         List<Step> steps;
         boolean isNew = content.getPk().isNew();
         if (isNew) {
             steps = fxType.getWorkflow().getSteps();
         } else {
             steps = fxType.getWorkflow().getTargets(content.getStepId());
             if (steps.size() == 0 || !steps.contains(environment.getStep(content.getStepId())))
                 steps.add(environment.getStep(content.getStepId()));
         }
         ArrayList<SelectItem> result = new ArrayList<SelectItem>(steps.size());
         for (Step step : steps) {
             if (!fxType.useStepPermissions() ||
                     (isNew
                             ? ticket.mayCreateACL(step.getAclId())
                             : ticket.mayEditACL(step.getAclId()))) {
                 StepDefinition def = environment.getStepDefinition(step.getStepDefinitionId());
                 result.add(new SelectItem(String.valueOf(step.getId()), def.getLabel().getDefaultTranslation()));
             }
         }
         result.trimToSize();
         this.steps = result;
     }
 
     /**
      * Gets the acl used by the content
      *
      * @return the acl
      */
     public ACL getAcl() {
         return acl;
     }
 
     /**
      * Sets the acl used by the content
      *
      * @param acl the acl
      */
     public void setAcl(ACL acl) {
         content.setAclId(acl.getId());
         this.acl = acl;
     }
 
     public CeIdGenerator getIdGenerator() {
         return idGenerator;
     }
 
     /**
      * Returns all editable types.
      *
      * @return all editable types
      */
     public List<SelectItem> getEditableTypes() {
         List<FxType> types = CacheAdmin.getFilteredEnvironment().getTypes(true, true, true, false);
         ArrayList<FxType> result = new ArrayList<FxType>(types.size());
         for (FxType t : types) {
             if (!t.getName().equalsIgnoreCase("ROOT")) {
                 result.add(t);
             }
         }
         return FxJsfUtils.asSelectListWithLabel(result);
     }
 
     public String cancel() {
        return release();
     }
 
     protected String release() {
         FxJsfUtils.resetFaceletsComponent("frm:all");
         FxJsfUtils.resetFaceletsComponent("frm:body");
         environment = null;
         properties = null;
         groups = null;
         content = null;
         data = null;
         displayProv = null;
         addElementOptions = null;
         idGenerator = null;
         steps = null;
         acl = null;
         version = -1;
         id = -1;
         readOnly = false;
         //return getEditorPage();
         return null;
     }
 
     public List<SelectItem> getPossibleWorkflowSteps() {
         return steps == null ? new ArrayList<SelectItem>(0) : steps;
     }
 
 
     public String getStep() {
         return content == null ? "-1" : String.valueOf(content.getStepId());
     }
 
     /**
      * @return the label of the current step
      */
     public String getStepDescription() {
         try {
             long stepDef = environment.getStep(content.getStepId()).getStepDefinitionId();
             return environment.getStepDefinition(stepDef).getLabel().getBestTranslation();
         } catch (Throwable t) {
             return getStep();
         }
     }
 
     public void setStep(String value) {
         long stepId = Long.valueOf(value);
         content.setStepId(stepId);
     }
 
     public boolean isInitialized() {
         return content != null;
     }
 
 
     public CeDisplayProvider getDisplay() {
         return displayProv;
     }
 
 
     public String getElements() {
         return elements == null ? "" : FxArrayUtils.toSeparatedList(elements, ",");
     }
 
     public void setElements(String elements) {
         this.elements = elements == null || elements.trim().length() == 0 ? new String[0] : elements.split(",");
     }
 
     public long getType() {
         return type;
     }
 
     public String getTypeDisplay() {
         String display = environment.getType(type).getDescription().getBestTranslation();
         if (display == null || display.trim().length() == 0) {
             display = environment.getType(type).getName();
         }
         return display;
     }
 
     public boolean isSupportSecurity() {
         return environment.getType(type).usePermissions();
     }
 
     public CeDataWrapper getData() {
         return data;
     }
 
 
     public CeAddElementOptions getAddElementOptions() {
         return addElementOptions;
     }
 
     public List<FxData> getDataList() {
         return content.getRootGroup().getChildren();
     }
 
     public FxEnvironment getEnvironment() {
         return environment;
     }
 
     public List<FxPropertyAssignment> getProperties() {
         return properties;
     }
 
     public List<FxGroupAssignment> getGroups() {
         return groups;
     }
 
 
     public FxContent getContent() {
         return content;
     }
 
     /**
      * Get all available information about versions
      *
      * @return version info
      */
     public FxContentVersionInfo getVersionInfo() {
         return versionInfo;
     }
 
     /**
      * Persists all fields in the session without saving to the database.
      *
      * @return the next page to render (= the content editor)
      */
     public String saveInSession() {
         processAction();
         return getEditorPage();
     }
 
 
     /**
      * Removes all empty elements which are not required.
      *
      * @return the next page to render (= the content editor)
      */
     public String compact() {
         content.getRootGroup().removeEmptyEntries();
         FxJsfUtils.resetFaceletsComponent("frm:all");
         return getEditorPage();
     }
 
     /**
      * Deletes the instance
      *
      * @return the next page to render (= the content editor)
      */
     public String delete() {
         try {
             long id = content.getPk().getId();
             co.remove(content.getPk());
             release();
             new FxFacesMsgInfo("Content.nfo.deleted", id).addToContext();
         } catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
         //return getEditorPage();
         return null;
     }
 
 
     /**
      * Saves the data in a new version.
      *
      * @return the next page to render (= the content editor)
      */
     public String saveInNewVersion() {
         return _save(true);
     }
 
     /**
      * Saves the data.
      *
      * @return the next page to render (= the content editor)
      */
     public String save() {
         return _save(false);
     }
 
     /**
      * Saves the data in the current or in a new version.
      *
      * @param newVersion if true a new version is created.
      * @return the next page to render (=the editor)
      */
     private String _save(boolean newVersion) {
         FxPK pk;
         try {
             String msg = isNew() ? "Content.nfo.created" : "Content.nfo.updated";
             //Store the content
             pk = newVersion ? co.createNewVersion(content) : co.save(content);
             new FxFacesMsgInfo(msg, pk.getId()).addToContext();
             // Handle the tree
             final List<FxTreeNode> treeNodes = tree.getNodesWithReference(FxTreeMode.Edit, pk.getId());
             for (FxTreeNode node : this.treeNodes) {
                 if (node.isMarkForDelete()) {
                     tree.remove(node, false, true);
                 } else if (node.isTemporary()) {
                     boolean assignmentExists = false;
                     for (FxTreeNode child : treeNodes) {
                         if (child.getParentNodeId() == node.getParentNodeId()) {
                             // avoid duplicate tree entries
                             assignmentExists = true;
                         }
                     }
                     if (!assignmentExists) {
                         String name = null;
                         if (content.containsValue("/CAPTION")) {
                             name = ((FxString) content.getValue("/CAPTION")).getBestTranslation();
                         }
                         if (StringUtils.isBlank(name)) {
                             name = environment.getType(type).getName() + "_" + pk.getId() + "." + pk.getVersion();
                         }
                         tree.save(FxTreeNodeEdit.createNew(name).setParentNodeId(node.getParentNodeId()).setReference(pk).setPosition(Integer.MIN_VALUE));
                     }
                 }
             }
             // Reload changes
             setPk(pk);
             reload();
             FxJsfUtils.resetFaceletsComponent("frm:all");
         } catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
         return null;
         //return getEditorPage();
     }
 
     public String enableEdit() {
         reload(); // TODO: locking
         this.readOnly = false;
         return getEditorPage();
     }
 
     /**
      * Ajax call for remove element.
      *
      * @param event the event
      */
     public void removeElement(ActionEvent event) {
         try {
             content.remove(element.getXPathFull());
             FxJsfUtils.resetFaceletsComponent("frm:all");
         } catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
     /**
      * Ajax call for addElement.
      *
      * @param event the event
      */
     public void addElement(ActionEvent event) {
         try {
             element.createNew(element.getPos() + 1);
             FxJsfUtils.resetFaceletsComponent("frm:all");
         } catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
     /**
      * Ajax call for addElement.
      *
      * @param event the event
      */
     public void moveElementUp(ActionEvent event) {
         try {
             element.getParent().moveChild(element.getXPathElement(), -1);
             FxJsfUtils.resetFaceletsComponent("frm:all");
         } catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
     /**
      * Ajax call for addElement.
      *
      * @param event the event
      */
     public void moveElementDown(ActionEvent event) {
         try {
             element.getParent().moveChild(element.getXPathElement(), +1);
             FxJsfUtils.resetFaceletsComponent("frm:all");
         } catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
 
     /**
      * Ajax call for addElement.
      *
      * @param event the event
      */
     public void addChilds(ActionEvent event) {
         try {
             FxGroupData grp;
             int pos;
 
             if (element instanceof FxGroupData) {
                 grp = (FxGroupData) element;
                 pos = 1;
             } else {
                 grp = element.getParent();
                 pos = element.getPos() + 1;
             }
 
             for (String ele : elements) {
                 grp.addEmptyChild(ele, pos++);
             }
             FxJsfUtils.resetFaceletsComponent("frm:all");
 
         } catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
     public FxData getElement() {
         return element;
     }
 
     public void setElement(FxData elememt) {
         this.element = elememt;
     }
 
 
     /**
      * Gets the display text for a assignment.
      *
      * @param ass the assignment
      * @return the display text
      */
     protected static FxString getDisplay(FxAssignment ass) {
         if (ass == null) return new FxString("");
         String display = ass.getDisplayName();
         if ((display == null || display.trim().length() == 0) && ass instanceof FxPropertyAssignment) {
             FxString result = ((FxPropertyAssignment) ass).getProperty().getLabel();
             return (result == null) ? new FxString("[empty displayname]") : result;
         }
         return new FxString(display);
     }
 
     /**
      * Prepare the content for save operation
      *
      * @throws FxApplicationException on errors
      */
     public void prepareSave() throws FxApplicationException {
         content = getContentEngine().prepareSave(content);
     }
 }
