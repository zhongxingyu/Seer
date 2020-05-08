 /***************************************************************
  *  This file is part of the [fleXive](R) backend application.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) backend application is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/licenses/gpl.html.
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
 import com.flexive.faces.beans.FxContentEditorBean;
 import com.flexive.faces.beans.SearchResultBean;
 import com.flexive.faces.components.content.FxWrappedContent;
 import com.flexive.faces.messages.FxFacesMsgErr;
 import com.flexive.faces.messages.FxFacesMsgInfo;
 import com.flexive.shared.*;
 import com.flexive.shared.content.FxContent;
 import com.flexive.shared.content.FxContentVersionInfo;
 import com.flexive.shared.content.FxDelta;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.interfaces.ContentEngine;
 import com.flexive.shared.interfaces.TreeEngine;
 import com.flexive.shared.security.LifeCycleInfo;
 import com.flexive.shared.security.UserTicket;
 import com.flexive.shared.structure.FxType;
 import com.flexive.shared.tree.FxTreeMode;
 import com.flexive.shared.tree.FxTreeNode;
 import com.flexive.shared.tree.FxTreeNodeEdit;
 import com.flexive.shared.tree.FxTreeRemoveOp;
 import com.flexive.shared.value.FxReference;
 import com.flexive.shared.value.FxValue;
 import com.flexive.shared.value.ReferencedContent;
 import com.flexive.shared.value.renderer.FxValueFormatter;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.myfaces.custom.fileupload.UploadedFile;
 
 import javax.faces.model.SelectItem;
 import javax.servlet.http.HttpSession;
 import java.io.Serializable;
 import java.util.*;
 
 /**
  * Backend content editor bean. Use
  * {@link com.flexive.war.beans.admin.content.BeContentEditorBean#initEditor(com.flexive.shared.content.FxContent, boolean)},
  * {@link com.flexive.war.beans.admin.content.BeContentEditorBean#initEditor(com.flexive.shared.content.FxPK, boolean)}, or
  * {@link com.flexive.war.beans.admin.content.BeContentEditorBean#initEditor(long, boolean)}, to initialize the content
  * editor from other beans, or use the url to pass parameters.
  */
 public class BeContentEditorBean implements ActionBean, Serializable {
     private static final long serialVersionUID = 1255372771864031452L;
     private static final Log LOG = LogFactory.getLog(BeContentEditorBean.class);
 
     private static final String FORM_ID = "frm";
     private static final String EDITOR_ID = "CE_ID";
     // edit new content of given type
     private long typeId = -1;
     // edit existing content of given pk
     private FxPK pk;
     // edit given content instance
     private FxContent content;
 
     private int version;
     private List<FxTreeNode> treeNodes;
     private long treeNodeParent;
     private FxTreeNode treeNode;
     private String infoPanelState;
     private int compareSourceVersion;
     private int compareDestinationVersion;
     private FxValueFormatter valueFormatter;
     private Map<Long, String> treeLabelMap;
     private Map<Long, String> displayTreeLabelMap;
     // content injected from the content editor component
     private FxWrappedContent wrappedContent;
     // edit mode
     private boolean editMode;
     private boolean reset;
     // type id for creating new contents
     private long newTypeId;
 
     /*import*/
     // uploaded file for import
     private UploadedFile importUpload;
     // Pasted content for import
     private String importPasted;
     // Save after an import or keep editing?
     private boolean importSave;
 
     private long contentIdToInit = -1;
 
     private String fxCeRenderFieldsJSFunction = null;
     private boolean beBeanInUse;
 
     // Flag indicating if the content is opened/edited from a result set
     private boolean fromResultSet;
     // reload the content tree - flag checked in contenteditor.xhtml main page
     private boolean reloadContentTree = false;
 
     // hold the PKs in the order the user want them
     private String sortedPKs = null;
 
     private boolean changed = false;
 
     private CallbackOpts nextOp = null;
 
     // keep the index of a requested PK for the reqeuest in the hashtable so that we don't need to search for it every time
     private Hashtable<FxPK, Integer> currentIndexCache = new Hashtable<FxPK, Integer>();
 
     private ArrayList<FxPK> sortedPKArray = new ArrayList<FxPK>();
     private transient HttpSession session = null;
     private final static String SORTED_PK_KEY = "BeContentEditorBean/sortedPK";
 
     /**
      * {@inheritDoc}
      */
     public String getParseRequestParameters() throws FxApplicationException {
         try {
             String action = FxJsfUtils.getParameter("action");
             if (StringUtils.isBlank(action)) {
                 return null;
             }
             resetViewStateVars();
             if ("newInstance".equals(action)) {
                 long typeId = FxJsfUtils.getLongParameter("typeId", -1);
                 if (typeId == -1) {
                     typeId = CacheAdmin.getFilteredEnvironment().getType(FxJsfUtils.getParameter("typeName")).getId();
                 }
                 long nodeId = FxJsfUtils.getLongParameter("nodeId", -1);
                 this.typeId = typeId;
                 if (nodeId != -1) {
                     setTreeNodeParent(nodeId);
                     addTreeNode();
                 }
                 beBeanInUse = true;
                 editMode = true;
             } else if ("editInstance".equals(action)) {
                 FxPK newPk;
                 if (FxJsfUtils.getParameter("pk") != null) {
                     String split[] = FxJsfUtils.getParameter("pk").split("\\.");
                     Long id = Long.valueOf(split[0].trim());
                     Integer ver = Integer.valueOf(split[1].trim());
                     newPk = new FxPK(id, ver);
                 } else {
                     newPk = new FxPK(
                             FxJsfUtils.getLongParameter("id"),
                             FxJsfUtils.getBooleanParameter("liveMode", false)
                             ? FxPK.LIVE : FxPK.MAX
                     );
                 }
                 pk = newPk;
                 beBeanInUse = true;
                 editMode = FxJsfUtils.getBooleanParameter("editMode", false);
             }
         } catch (Throwable t) {
             // TODO possibly pass some error message to the HTML page
             LOG.error("Failed to parse request parameters: " + t.getMessage(), t);
         }
         return null;
     }
 
     /**
      * Resets all variables that are stored in the view state.
      */
     private void resetViewStateVars() {
         this.content = null;
         this.typeId = -1;
         this.pk = null;
         this.infoPanelState = null;
         this.editMode = false;
         this.reset = true;
         this.treeNodes = null;
         this.fromResultSet = false;
         this.beBeanInUse = false;
         // hack!
         FxJsfUtils.resetFaceletsComponent(FORM_ID + ":" + EDITOR_ID);
     }
 
     /**
      * Returns if the content editor is initialized for editing or content viewing
      *
      * @return if the content editor is initialized
      */
 
     public boolean isInitialized() {
         return pk != null || typeId != -1 || content != null;
     }
 
     public boolean isReset() {
         return reset;
     }
 
     public boolean isEditMode() {
         return editMode;
     }
 
     public void setEditMode(boolean editMode) {
         this.editMode = editMode;
     }
 
     public String getEditorId() {
         return EDITOR_ID;
     }
 
     public String getFormId() {
         return FORM_ID;
     }
 
     public FxContent getContent() {
         return content;
     }
 
     public void setContent(FxContent content) {
         this.content = content;
     }
 
     public long getTypeId() {
         return typeId;
     }
 
     public void setTypeId(long typeId) {
         this.typeId = typeId;
     }
 
     public long getNewTypeId() {
         return newTypeId;
     }
 
     public void setNewTypeId(long newTypeId) {
         this.newTypeId = newTypeId;
     }
 
     public void setWrappedContent(FxWrappedContent wrappedContent) {
         this.wrappedContent = wrappedContent;
     }
 
     public UploadedFile getImportUpload() {
         return importUpload;
     }
 
     public void setImportUpload(UploadedFile importUpload) {
         this.importUpload = importUpload;
     }
 
     public String getImportPasted() {
         return importPasted;
     }
 
     public void setImportPasted(String importPasted) {
         this.importPasted = importPasted;
     }
 
     public boolean isImportSave() {
         return importSave;
     }
 
     public void setImportSave(boolean importSave) {
         this.importSave = importSave;
     }
 
     public void setFromResultSet(boolean fromResultSet) {
         this.fromResultSet = fromResultSet;
     }
 
     public boolean isFromResultSet() {
         return fromResultSet;
     }
 
     public boolean isBeBeanInUse() {
         return beBeanInUse;
     }
 
     public void setBeBeanInUse(boolean beBeanInUse) {
         this.beBeanInUse = beBeanInUse;
     }
 
     public boolean isReloadContentTree() {
         return reloadContentTree;
     }
 
     public void setReloadContentTree(boolean reloadContentTree) {
         this.reloadContentTree = reloadContentTree;
     }
 
     /**
      * Import a content
      *
      * @return next page
      */
     public String doImport() {
         FxContent _con = null;
         if (importUpload != null && importUpload.getSize() > 0) {
             System.out.println("Uploaded " + importUpload.getSize() + " bytes for " + importUpload.getName());
             try {
                 _con = EJBLookup.getContentEngine().importContent(new String(importUpload.getBytes(), "UTF-8"), true);
             } catch (Exception e) {
                 new FxFacesMsgErr(e).addToContext();
             }
         } else if (!StringUtils.isEmpty(importPasted)) {
             try {
                 _con = EJBLookup.getContentEngine().importContent(importPasted, true);
             } catch (FxApplicationException e) {
                 new FxFacesMsgErr(e).addToContext();
             }
         } else {
             new FxFacesMsgInfo("Content.nfo.import.noData").addToContext();
         }
         if (_con != null) {
             this.importPasted = "";
             this.importUpload = null;
             this.infoPanelState = "";
             try {
                 wrappedContent.getContent().replaceData(_con);
                 ((FxContentEditorBean) FxJsfUtils.getManagedBean("fxContentEditorBean")).setEditorId(wrappedContent.getEditorId());
                 compact();
                 if (importSave) {
                     wrappedContent.getGuiSettings().setEditMode(false);
                     save();
                 }
                 new FxFacesMsgInfo("Content.nfo.imported").addToContext();
             } catch (Exception e) {
                 new FxFacesMsgErr(e).addToContext();
             }
         }
         return null;
     }
 
     /**
      * Returns content versions as select items
      *
      * @return content versions as select items
      * @throws FxApplicationException on errors
      */
     public List<SelectItem> getCompareVersions() throws FxApplicationException {
         FxContentVersionInfo versionInfo = pk.isNew() ? FxContentVersionInfo.createEmpty() : EJBLookup.getContentEngine().getContentVersionInfo(pk);
         List<SelectItem> items = new ArrayList<SelectItem>(versionInfo.getVersionCount());
         if (versionInfo.getVersionCount() > 0) {
             for (int v : versionInfo.getVersions()) {
                 LifeCycleInfo lci = versionInfo.getVersionData(v).getLifeCycleInfo();
                 String name = "unknown";
                 try {
                     name = EJBLookup.getAccountEngine().load(lci.getModificatorId()).getName();
                 } catch (FxApplicationException e) {
                     //ignore
                 }
                 items.add(new SelectItem(v, "Version " + v + " by " + name + " at " + FxFormatUtils.getDateTimeFormat().format(new Date(lci.getModificationTime()))));
             }
         }
         return items;
     }
 
     /**
      * Returns FxDeltas between two selected versions.
      *
      * @return FxDeltas between two selected versions
      */
     public List<FxDelta.FxDeltaChange> getCompareEntries() {
         List<FxDelta.FxDeltaChange> emptyResult = new ArrayList<FxDelta.FxDeltaChange>(0);
         try {
             final ContentEngine ce = EJBLookup.getContentEngine();
             FxContentVersionInfo versionInfo = pk.isNew() ? FxContentVersionInfo.createEmpty() : ce.getContentVersionInfo(pk);
             if ("compare".equals(infoPanelState) &&
                     compareSourceVersion > 0 &&
                     compareSourceVersion <= versionInfo.getMaxVersion() &&
                     compareDestinationVersion > 0 &&
                     compareDestinationVersion <= versionInfo.getMaxVersion()) {
                 FxContent content1 = ce.load(new FxPK(getId(), compareSourceVersion));
                 FxContent content2 = ce.load(new FxPK(getId(), compareDestinationVersion));
                 FxDelta delta = FxDelta.processDelta(content1, content2);
                 List<FxDelta.FxDeltaChange> changes = delta.getDiff(content1, content2);
                 //filter internal
                 List<FxDelta.FxDeltaChange> internal = new ArrayList<FxDelta.FxDeltaChange>(5);
                 for (FxDelta.FxDeltaChange d : changes)
                     if (d.isInternal())
                         internal.add(d);
                 changes.removeAll(internal);
                 return changes;
             } else {
                 return emptyResult;
             }
         } catch (FxApplicationException e) {
             new FxFacesMsgErr(e).addToContext();
             return emptyResult;
         }
     }
 
     /**
      * Get the content as XML
      *
      * @return content as XML
      */
     public Map<FxWrappedContent, String> getExportData() {
         return new HashMap<FxWrappedContent, String>() {
             public String get(Object key) {
                 if ("export".equals(infoPanelState) && key instanceof FxWrappedContent) {
                     FxWrappedContent con = (FxWrappedContent) key;
                     if (con.getContent() != null) {
                         try {
                             return EJBLookup.getContentEngine().exportContent(con.getContent());
                         } catch (FxApplicationException e) {
                             LOG.error(e);
                         }
                     }
                 }
                 return "";
             }
         };
     }
 
     public int getCompareSourceVersion() {
         return compareSourceVersion;
     }
 
     public void setCompareSourceVersion(int compareSourceVersion) {
         this.compareSourceVersion = compareSourceVersion;
     }
 
     public int getCompareDestinationVersion() {
         return compareDestinationVersion;
     }
 
     public void setCompareDestinationVersion(int compareDestinationVersion) {
         this.compareDestinationVersion = compareDestinationVersion;
     }
 
     public String getInfoPanelState() {
         return infoPanelState;
     }
 
     public void setInfoPanelState(String togglePanelState) {
         this.infoPanelState = togglePanelState;
     }
 
     public String getEditorPage() {
         return "contentEditor";
     }
 
     /**
      * Returns referencing tree nodes for stored content instances.
      *
      * @return referencing tree nodes for stored content instances
      */
     public List<FxTreeNode> getTreeNodes() {
         if (treeNodes == null) {
             if (pk != null && !pk.isNew()) {
                 try {
                     treeNodes = EJBLookup.getTreeEngine().getNodesWithReference(FxTreeMode.Edit, pk.getId());
                 } catch (Throwable t) {
                     new FxFacesMsgErr(t).addToContext();
                 }
             } else treeNodes = new ArrayList<FxTreeNode>(0);
         }
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
 
     public FxTreeNode getTreeNode() {
         return treeNode;
     }
 
     public void setTreeNode(FxTreeNode treeNode) {
         this.treeNode = treeNode;
     }
 
     /**
      * Ajax call to detach content from tree node set via treeNode variable.
      */
     public void removeTreeNode() {
         //FxJsfUtils.resetFaceletsComponent(FORM_ID+":"+EDITOR_ID);
         for (FxTreeNode node : getTreeNodes()) {
             if (node.getId() == treeNode.getId())
                 node.setMarkForDelete(true);
         }
     }
 
     /**
      * Ajax call to attach content to tree node id set via treeNodeParent variable.
      */
     public void addTreeNode() {
         addTreeNode(treeNodeParent);
     }
 
     /**
      * Attach content to a given tree node
      *
      * @param _node tree node id
      */
     public void addTreeNode(long _node) {
         try {
             // Only add the path if it does not already exist
             for (FxTreeNode node : getTreeNodes()) {
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
                 FxTreeNode tn = EJBLookup.getTreeEngine().getNode(FxTreeMode.Edit, _node);
                 if (tn != null)
                     getTreeNodes().add(FxTreeNode.createNewTemporaryChildNode(tn));
             } catch (Throwable t) {
                 /* ignore */
             }
         } catch (Exception t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
     /**
      * Mapped function to return the label path for a tree node (id)
      *
      * @return label path for a given tree node in the calling users locale
      */
     public Map<Long, String> getTreeLabelPath() {
         if (treeLabelMap == null) {
             treeLabelMap = FxSharedUtils.getMappedFunction(new FxSharedUtils.ParameterMapper<Long, String>() {
                 public String get(Object key) {
                     try {
                         long id = (Long) key;
                         if (id < 0) {
                             for (FxTreeNode node : getTreeNodes()) {
                                 if (node.getId() == id)
                                     return EJBLookup.getTreeEngine().getLabels(FxTreeMode.Edit, node.getParentNodeId()).get(0) + "/*";
                             }
                             return "unknown/*";
                         } else
                             return EJBLookup.getTreeEngine().getLabels(FxTreeMode.Edit, id).get(0);
                     } catch (FxApplicationException e) {
                         return "unknown";
                     }
                 }
             }, true);
         }
         return treeLabelMap;
     }
 
     /**
      * Like {@link #getTreeLabelPath()}, but trimmed to a sensible maximum length for showing the path in the backend.
      *
      * @return  trimmed label path for a given tree node
      */
     public Map<Long, String> getTreeLabelPathDisplay() {
         if (displayTreeLabelMap == null) {
             displayTreeLabelMap = FxSharedUtils.getMappedFunction(new FxSharedUtils.ParameterMapper<Long, String>() {
 
                 public String get(Object key) {
                     final int maxLen = 100;
                     final String label = getTreeLabelPath().get((Long) key);
                     if (label != null && label.length() > maxLen) {
                         return label.substring(0, maxLen - 3) + "...";
                     } else {
                         return label;
                     }
                 }
                 
             }, false);
         }
         return displayTreeLabelMap;
     }
 
     /**
      * JSF action to create a new instance for a given type id.
      *
      * @return contentEditor navigation case
      */
     public String createNewContent() {
         resetViewStateVars();
         this.typeId = newTypeId;
         beBeanInUse = true;
         this.editMode = true;
         return getEditorPage();
     }
 
     public FxPK getPk() {
         return pk;
     }
 
     public void setPk(FxPK pk) {
         this.pk = pk;
     }
 
     /**
      * JSF action used to reload currently edited content.
      * Note: Pending changes are lost.
      *
      * @return next page
      */
     public String reload() {
         this.reset = true;
         this.treeNodes = null;
         this.infoPanelState = null;
         FxJsfUtils.resetFaceletsComponent(FORM_ID + ":" + EDITOR_ID);
         return null;
     }
 
     /**
      * JSF- action to load the given version
      */
     public void loadVersion() {
         reload();
         this.pk = new FxPK(wrappedContent.getContent().getId(), version);
         this.editMode = wrappedContent.getGuiSettings().isEditMode();
         new FxFacesMsgInfo("ContentEditor.nfo.version.loaded", version, pk).addToContext();
     }
 
     public long getId() {
         return pk != null ? pk.getId() : -1;
     }
 
     public void setId(long id) {
         this.contentIdToInit = id;
     }
 
     public long getContentIdToInit() {
         return contentIdToInit;
     }
 
     public void setContentIdToInit(long contentIdToInit) {
         this.contentIdToInit = contentIdToInit;
     }
 
     public void setVersion(int version) {
         this.version = version;
     }
 
     public int getVersion() {
         return version;
     }
 
     /**
      * Initialize for an existing content instance.
      * To be used by other beans.
      *
      * @param con      content instance
      * @param editMode edit mode
      * @return editor page
      */
     public String initEditor(FxContent con, boolean editMode) {
         resetViewStateVars();
         content = con;
         this.editMode = editMode;
         beBeanInUse = true;
         return getEditorPage();
     }
 
     /**
      * Initialize for a given pk (which must not be new).
      * To be used by other beans.
      *
      * @param pk       of a stored content instance
      * @param editMode edit mode
      * @return editor page
      */
     public String initEditor(FxPK pk, boolean editMode) {
         resetViewStateVars();
         this.pk = pk;
         this.editMode = editMode;
         beBeanInUse = true;
         return getEditorPage();
     }
 
     /**
      * Initialize for a given type id.
      * To be used by other beans.
      *
      * @param typeId   type id
      * @param editMode edit mode
      * @return editor page
      */
     public String initEditor(long typeId, boolean editMode) {
         resetViewStateVars();
         this.typeId = typeId;
         this.editMode = editMode;
         beBeanInUse = true;
         return getEditorPage();
     }
 
     /**
      * Initialize the content editor from the context menu of a result set
      * with the set version and content id.
      *
      * @return editor page
      */
     public String initEditorFromResultSet() {
         // save edit mode
         boolean editMode = this.editMode;
         // loose the lock (FX828)
         if (editMode && wrappedContent != null) {
             ((FxContentEditorBean) FxJsfUtils.getManagedBean("fxContentEditorBean")).setEditorId(wrappedContent.getEditorId());
             ((FxContentEditorBean) FxJsfUtils.getManagedBean("fxContentEditorBean")).cancel();
         }
         resetViewStateVars();
         // restore edit mode
         this.editMode = editMode;
         // set result set flag
         this.fromResultSet = true;
         this.pk = new FxPK(contentIdToInit, version);
         beBeanInUse = true;
         return getEditorPage();
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
 
     /**
      * Cancel editing
      *
      * @return next page
      */
 
     public String cancel() {
         if ("import".equals(infoPanelState))
             infoPanelState = null;
         boolean isReferenced = wrappedContent.isReferenced();
         ((FxContentEditorBean) FxJsfUtils.getManagedBean("fxContentEditorBean")).cancel();
         if (!isReferenced) {
             if (wrappedContent.isNew()) {
                 resetViewStateVars();
             } else {
                 editMode = false;
                 wrappedContent.setReset(true);
                 treeNodes = null;
             }
         }
         return null;
     }
 
     public void callBack() {
         switch (nextOp) {
             case CANCEL:
                 cancel();
                 break;
             case SET_NEXT_PK:
                 initEditorFromResultSet();
                 break;
         }
     }
 
     public CallbackOpts getNextOp() {
         return nextOp;
     }
 
     public void setNextOp(CallbackOpts nextOp) {
         this.nextOp = nextOp;
     }
 
     public void preCancel(){
         changed = wrappedContent.wasChanged();
         if (changed) {
             nextOp = CallbackOpts.CANCEL;
         } else {
             cancel();
         }
     }
 
     public void preInitEditorFromResultSet() {
         changed = wrappedContent.wasChanged();
         if (changed) {
             nextOp = CallbackOpts.SET_NEXT_PK;
         } else {
             initEditorFromResultSet();
         }
     }
 
     /**
      * Removes all empty elements which are not required.
      *
      * @return the next page to render.
      */
     public String compact() {
         ((FxContentEditorBean) FxJsfUtils.getManagedBean("fxContentEditorBean")).compact();
         return null;
     }
 
     /**
      * Deletes the instance
      *
      * @return the next page to render (= the content editor)
      */
     public String delete() {
         try {
             boolean isReferenced = wrappedContent.isReferenced();
             // check if deleted content was part of result set
             // and update result set if necessary
             FxPK newPK;
             boolean editModeSaved = editMode;
             ((FxContentEditorBean) FxJsfUtils.getManagedBean("fxContentEditorBean"))._delete();
             newPK = removePKindex(wrappedContent.getContent().getPk());
             if (!isReferenced) {
                 resetViewStateVars();
             }
             if (newPK != null) {
                 // reinit content editor with saved values
                 this.editMode = editModeSaved;
                 this.pk = newPK;
                 this.fromResultSet = true;
             }
         } catch (Exception e) {
             new FxFacesMsgErr(e).addToContext();
         } finally {
             FxJsfUtils.resetFaceletsComponent(FORM_ID + ":" + EDITOR_ID);
         }
         return null;
     }
 
     /**
      * Deletes the current version
      *
      * @return the next page to render (= the content editor)
      */
     public String deleteCurrentVersion() {
         try {
             boolean isReferenced = wrappedContent.isReferenced();
             FxContentEditorBean ceBean = (FxContentEditorBean) FxJsfUtils.getManagedBean("fxContentEditorBean");
             // check if deleted content was part of result set
             // and update result set if necessary
             ceBean._deleteCurrentVersion();
             // update search result
             removePKindex(getPk());
             if (!isReferenced) {
                 // retrieve new pk from content storage
                 pk = new FxPK(pk.getId(), FxPK.MAX);
             }
         } catch (Exception t) {
             new FxFacesMsgErr(t).addToContext();
         } finally {
             FxJsfUtils.resetFaceletsComponent(FORM_ID + ":" + EDITOR_ID);
         }
         return null;
     }
 
     /**
      * Deletes a specific version set in "version"
      *
      * @return the next page to render (= the content editor)
      */
     public String deleteVersion() {
         try {
             FxPK pkToDelete = new FxPK(wrappedContent.getContent().getPk().getId(), version);
             EJBLookup.getContentEngine().removeVersion(pkToDelete);
             new FxFacesMsgInfo("Content.nfo.deletedVersion", pkToDelete).addToContext();
             // update search result
             removePKindex(pkToDelete);
             // load highest available version
             this.pk = new FxPK(pk.getId(), FxPK.MAX);
         } catch (Exception t) {
             new FxFacesMsgErr(t).addToContext();
         } finally {
             FxJsfUtils.resetFaceletsComponent(FORM_ID + ":" + EDITOR_ID);
         }
         return null;
     }
 
     /**
      * Saves the data in a new version.
      */
     public void saveInNewVersion() {
         _save(true);
         FxJsfUtils.resetFaceletsComponent(FORM_ID + ":" + EDITOR_ID);
     }
 
     /**
      * Action method: Saves the data.
      */
     public void save() {
         final UserTicket ticket = FxContext.getUserTicket();
         boolean ownerChange = false;
         // no owner change check if the current user is a supervisor
         if (!ticket.isGlobalSupervisor() || !ticket.isMandatorSupervisor()) {
             ownerChange = checkOwnerChange();
         }
 
         if (ownerChange) {
             wrappedContent.getGuiSettings().setTakeOver(true);
             new FxFacesMsgErr("ContentEditor.msg.takeOver.warning").addToContext();
 
         } else {
             _save(false);
         }
         FxJsfUtils.resetFaceletsComponent(FORM_ID + ":" + EDITOR_ID);
     }
 
     /**
      * Action method: save the content and cancel editing (remove loose lock)
      */
     public void saveAndCancel() {
         final UserTicket ticket = FxContext.getUserTicket();
         try {
             boolean ownerChange = false;
             // no owner change check if the current user is a supervisor
             if (!ticket.isGlobalSupervisor() || !ticket.isMandatorSupervisor()) {
                 ownerChange = checkOwnerChange();
             }
 
             if (ownerChange) {
                 wrappedContent.getGuiSettings().setTakeOver(true);
                 new FxFacesMsgErr("ContentEditor.msg.takeOver.warning").addToContext();
 
             } else {
                 _save(false);
                 cancel();
             }
         } catch (Exception t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
     /**
      * Checks whether the currently opened content had it's lock revoked by another user
      * and displays the appropriate message
      *
      * @return returns true if the owner (lock) of the currently opened content changed
      */
     public boolean checkOwnerChange() {
         return ((FxContentEditorBean) FxJsfUtils.getManagedBean("fxContentEditorBean")).checkOwnerChange();
     }
 
     /**
      * Action method: override a content's lock
      *
      * @return returns the current page
      */
     public String lockOverride() {
         ((FxContentEditorBean) FxJsfUtils.getManagedBean("fxContentEditorBean")).lockOverride();
         return getEditorPage();
     }
 
     /**
      * Action method: override a content's lock and enter edit mode
      *
      * @return returns the current page
      */
     public String lockOverrideAndEdit() {
         ((FxContentEditorBean) FxJsfUtils.getManagedBean("fxContentEditorBean")).lockOverrideAndEdit();
         return getEditorPage();
     }
 
     /**
      * Action method: edit the content in a new (max) version
      *
      * @return returns the current page
      */
     public String enableEditInNewVersion() {
         ((FxContentEditorBean) FxJsfUtils.getManagedBean("fxContentEditorBean")).enableEditInNewVersion();
         return getEditorPage();
     }
 
     /**
      * Saves the data in the current or in a new version.
      *
      * @param newVersion if true a new version is created.
      */
     private void _save(boolean newVersion) {
         try {
             // save old view state
             FxPK oldPk = pk;
             long oldTypeId = typeId;
             FxContent oldContent = content;
             boolean isReferenced = wrappedContent.isReferenced();
             // check if saved content was part of result set
             // and update result set if necessary
             boolean updateSearchResult = isPkInSearchResult(wrappedContent.getContent().getPk());
 
             FxContentEditorBean ceBean = (FxContentEditorBean) FxJsfUtils.getManagedBean("fxContentEditorBean");
             this.pk = ceBean._save(newVersion);
             // update search result
             if (updateSearchResult) {
                 ((SearchResultBean) FxJsfUtils.getManagedBean("fxSearchResultBean")).refresh();
             }
             if (!isReferenced) {
                 // if content was new, reset typeId, content and wrapped content (-> use newPk after save)
                 if (wrappedContent.getContent().getPk().isNew()) {
                     typeId = -1;
                     content = null;
                     this.editMode = false;
                     // retrieve saved content from storage and set reset flag,
                     // for edit mode to be deactivated
                     ceBean.getContentStorage().get(wrappedContent.getEditorId()).setReset(true);
                 }
 
                 // if no error happened, handle tree
                 TreeEngine tree = EJBLookup.getTreeEngine();
                 final List<FxTreeNode> treeNodes = tree.getNodesWithReference(FxTreeMode.Edit, pk.getId());
                 for (FxTreeNode node : getTreeNodes()) {
                     if (node.isMarkForDelete() && !node.isTemporary()) {
                         tree.remove(node, FxTreeRemoveOp.Unfile, true);
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
                             if (wrappedContent.getContent().hasCaption()) {
                                 name = wrappedContent.getContent().getCaption().getBestTranslation();
                             }
                             if (StringUtils.isBlank(name)) {
                                 name = CacheAdmin.getFilteredEnvironment().getType(wrappedContent.getContent().getTypeId()).getName() + "_" + pk.getId() + "." + pk.getVersion();
                             }
                             tree.save(FxTreeNodeEdit.createNew(name).setParentNodeId(node.getParentNodeId()).setReference(pk).setPosition(Integer.MIN_VALUE));
                         }
                     }
                     // Reload changes
                     this.treeNodes = null;
                     // TODO: only call when tree node labels need to be manually updated
                     // (i.e. after an existing content is edited and caption changed)
                     FxContext.get().setTreeWasModified();
                 }
             }
             if (isReferenced) {
                 // if content was referenced, reset to saved view state vars
                 // (so that base content is displayed again)
                 pk = oldPk;
                 content = oldContent;
                 typeId = oldTypeId;
             }
         } catch (Exception t) {
             new FxFacesMsgErr(t).addToContext();
         }
     }
 
     /**
      * Action method: enable edit mode
      *
      * @return returns the current view
      */
     public String enableEdit() {
         ((FxContentEditorBean) FxJsfUtils.getManagedBean("fxContentEditorBean")).enableEdit();
         return null;
     }
 
     /**
      * Action method: acquire a permanent lock on a content
      */
     public void acquirePermLock() {
         ((FxContentEditorBean) FxJsfUtils.getManagedBean("fxContentEditorBean")).acquirePermLock();
     }
 
     /**
      * Action method: acquire a loose lock on a content
      */
     public void acquireLooseLock() {
         ((FxContentEditorBean) FxJsfUtils.getManagedBean("fxContentEditorBean")).acquireLooseLock();
     }
 
     /**
      * Action method: unlock a content
      */
     public void unLock() {
         ((FxContentEditorBean) FxJsfUtils.getManagedBean("fxContentEditorBean")).unLock();
     }
 
     /**
      * Action method: compute the remaining lock duration
      */
     public void computeRemainingLockTime() {
         ((FxContentEditorBean) FxJsfUtils.getManagedBean("fxContentEditorBean")).computeRemainingLockTime();
     }
 
     /**
      * Action method: extend the lock duration
      */
     public void extendLock() {
         ((FxContentEditorBean) FxJsfUtils.getManagedBean("fxContentEditorBean")).extendLock();
     }
 
     /**
      * Action method: cancel the lock override, resets form and gui settings
      */
     public void noLockOverride() {
         ((FxContentEditorBean) FxJsfUtils.getManagedBean("fxContentEditorBean")).noLockOverride();
     }
 
     /**
      * Return a custom value formatter for FxValueInput if edit mode is disabled.
      *
      * @return the value formatter for FxValueInput
      */
     public FxValueFormatter getValueFormatter() {
         if (valueFormatter == null) {
             valueFormatter = new ReferenceValueFormatter();
         }
         return valueFormatter;
     }
 
 
     public boolean isChanged() {
         return changed;
     }
 
     /**
      * A custom value formatter for content references. Renders content references as
      * links, that trigger the referenced content to be opened in content editor.
      */
     private static class ReferenceValueFormatter implements FxValueFormatter, Serializable {
         private static final long serialVersionUID = 2283199607456062352L;
 
         public String format(FxValue container, Object value, FxLanguage outputLanguage) {
             if (container instanceof FxReference && value instanceof ReferencedContent) {
                 return "<a href=\"adm/content/contentEditor.jsf?action=editInstance&readOnly=true&id="
                         + ((ReferencedContent) value).getId() + "\">"
                         + ((ReferencedContent) value).getCaption() + "</a>";
             }
             return null;
         }
     }
 
     /**
      * Returns whether the given pk is part of the search result.
      *
      * @param pk pk
      * @return whether the content instance with the pk currently being edited
      *         is part of the search result.
      */
     private boolean isPkInSearchResult(FxPK pk) {
         return isFromResultSet() && pk != null && sortedPKArray.size() > 0 && sortedPKArray.contains(pk);
     }
 
     /**
      * Update the search result and fetch the new PK to edit
      *
      * @param matchVersion if true, the updated search result is
      *                     matched for the new version, otherwise for index.
      * @return the new pk to edit
      */
     private FxPK updateSearchResult(boolean matchVersion) {
         SearchResultBean sb = (SearchResultBean) FxJsfUtils.getManagedBean("fxSearchResultBean");
         int currentIndex = getIndex().get(getPk());
         sb.refresh();
         if (!matchVersion && currentIndex > 0 && sb.getResult().getRowCount() > 0) {
             // index handled by the getPkByIndex function
                 return getPkByIndex().get(currentIndex);
         } else if (matchVersion) {
             if (sb.getResult().getResultRow(getPk()) != null)
                 return getPk();
         }
         return null;
     }
 
     /**
      * Mapped access to search result row, matching given pk.
      *
      * @return Mapped access to search result row, matching given pk.
      */
     public Map<FxPK, FxContent> getResultRow() {
         return new HashMap<FxPK, FxContent>() {
             public FxContent get(Object key) {
                 FxPK pk = (FxPK) key;
                 try {
                     return EJBLookup.getContentEngine().load(pk);
                 } catch (FxApplicationException e) {
                     return get(removePKindex(pk));
                 }
             }
         };
     }
 
 
     public String getSortedPKs() {
         if (sortedPKs != null) {
             if (session == null)
                 session = FxJsfUtils.getSession();
             session.setAttribute(SORTED_PK_KEY, sortedPKs);
         }
         return sortedPKs;
     }
 
     /**
      * Removes a FxPK and return the following FxPK
      * @param currentPK the FxPK to remove
      * @return the following FxPK or <code>null</code> if the current PK was not found)
      */
     private FxPK removePKindex(FxPK currentPK) {
         int currentIndex = getIndex().get(currentPK);
         if (currentIndex < 0 || sortedPKArray.size() == 0)
             return null;
         sortedPKArray.remove(currentIndex);
         if (currentIndex >= sortedPKArray.size()) {
             currentIndex = sortedPKArray.size() - 1;
         }
         StringBuilder sb = new StringBuilder(sortedPKs.length());
         for (FxPK tmpPK : sortedPKArray) {
             sb.append(',').append(tmpPK);
         }
         sortedPKs = sb.toString().substring(1); // remove the starting ','
         currentIndexCache.clear();
         return sortedPKArray.get(currentIndex);
     }
 
     /**
      * returns the index of the given PK according to the sortedPKArray
      * @return the index in the sortedPKArray or -1 if not found
      */
     public Map<FxPK, Integer> getIndex() {
         return new HashMap<FxPK, Integer>() {
             public Integer get(Object key) {
                 Integer tmp = currentIndexCache.get(key);
                 if (tmp == null) {
                     if (StringUtils.isBlank(sortedPKs))
                         setSortedPKs(null);
                     tmp = sortedPKArray.indexOf(FxPK.fromObject(key));
                     currentIndexCache.put((FxPK) key, tmp);
                 }
                 return tmp;
             }
         };
     }
 
     public void setSortedPKs(String sortedPKs) {
         if (StringUtils.isBlank(sortedPKs)) {
             if (session == null)
                 session = FxJsfUtils.getSession();
             this.sortedPKs = (String) session.getAttribute(SORTED_PK_KEY);
            if (StringUtils.isBlank(this.sortedPKs)) {
                return;
            }
         }
         else {
             this.sortedPKs = sortedPKs;
         }
         for (String tmpS : this.sortedPKs.split(",")) {
             sortedPKArray.add(FxPK.fromString(tmpS));
         }
         this.currentIndexCache.clear();
     }
 
     /**
      * Mapped access to the pk of the search result row, matching given index.
      *
      * @return mapped access to the pk of the search result row, matching given index.
      */
     public Map<Integer, FxPK> getPkByIndex() {
         return new HashMap<Integer, FxPK>() {
 
             public FxPK get(Object key) {
                 // avoid JSF integer/long conversion bug
                 Integer i = new Integer(key.toString());
                 // cap possible overflows
                 if (sortedPKArray.size() <= i) {
                     i = sortedPKArray.size() - 1;
                 }
                 if (i < 0) {
                     i = 0;
                 }
                 return sortedPKArray.get(i);
             }
         };
     }
 
     /**
      * @return the confirmKey if in editMode or <code>null</code> if not in editMode
      */
     public String getConfirm() {
         return this.editMode ? "Content.confirm.abort" : null;
     }
 
     public int getRowCount() {
         return sortedPKArray.size();
     }
 
     private static enum CallbackOpts {
         CANCEL,
         SET_NEXT_PK
     }
 }
