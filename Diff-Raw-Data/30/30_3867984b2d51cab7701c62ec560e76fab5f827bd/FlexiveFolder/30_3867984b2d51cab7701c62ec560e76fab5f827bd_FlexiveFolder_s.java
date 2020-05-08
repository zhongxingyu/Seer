 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2014
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
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
 package com.flexive.cmis.spi;
 
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.FxSharedUtils;
 import com.flexive.shared.configuration.SystemParameters;
 import com.flexive.shared.content.FxContent;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.interfaces.TreeEngine;
 import com.flexive.shared.security.LifeCycleInfo;
 import com.flexive.shared.structure.FxEnvironment;
 import com.flexive.shared.structure.FxPropertyAssignment;
 import com.flexive.shared.structure.FxType;
 import com.flexive.shared.tree.FxTreeMode;
 import com.flexive.shared.tree.FxTreeNode;
 import com.flexive.shared.tree.FxTreeNodeEdit;
 import com.flexive.shared.tree.FxTreeRemoveOp;
 import com.flexive.shared.value.FxString;
 import com.google.common.collect.Lists;
 import org.apache.chemistry.*;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.xml.namespace.QName;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.*;
import org.apache.chemistry.impl.simple.SimpleTree;
 
 import static com.flexive.shared.CacheAdmin.getEnvironment;
 import static com.flexive.shared.EJBLookup.getTreeEngine;
 import static com.google.common.collect.Lists.newArrayList;
 
 /**
  * A folder based on a FxTreeNode and the backing FxContent instance, which must be derived from flexive's
  * FOLDER type.
  * 
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class FlexiveFolder extends FlexiveObjectEntry implements Folder {
     private static final Log LOG = LogFactory.getLog(FlexiveFolder.class);
     private static final String ROOT_FOLDER_NAME = "";  // from chemistry testcases
 
     private final long nodeId;
     private final FxTreeMode treeMode;
 
     private FxTreeNodeEdit _cachedNode;
     private FxContent _cachedContent;    // cached content instance, use getContent()
     private List<CMISObject> toAdd;     // objects to be filed in the next save operation
     private List<CMISObject> toRemove;  // objects to be unfiled in the next save operation
 
     /**
      * Create a new folder representation of a tree node.
      *
      * @param context   the connection context
      * @param node      the tree node
      */
     FlexiveFolder(FlexiveConnection.Context context, FxTreeNode node) {
         super(context);
         FxSharedUtils.checkParameterNull(node, "node");
         this.nodeId = node.getId();
         this.treeMode = node.getMode();
         this._cachedNode = node.asEditable();
     }
 
     /**
      * Create a new folder based on a node ID. The node will not be loaded immediately, but only
      * when node information is required for fulfilling a request.
      *
      * @param context   the connection context
      * @param nodeId    the tree node ID
      */
     FlexiveFolder(FlexiveConnection.Context context, long nodeId) {
         this(context, FxTreeMode.Edit, nodeId);
     }
 
     /**
      * Create a new folder based on a node ID. The node will not be loaded immediately, but only
      * when node information is required for fulfilling a request.
      *
      * @param context   the connection context
      * @param treeMode  the tree mode
      * @param nodeId    the tree node ID
      */
     FlexiveFolder(FlexiveConnection.Context context, FxTreeMode treeMode, long nodeId) {
         super(context);
         if (nodeId == -1) {
             throw new IllegalArgumentException("Folder not found.");
         }
         this.nodeId = nodeId;
         this.treeMode = treeMode;
     }
 
     public FxTreeNodeEdit getNode() {
         if (_cachedNode == null) {
             try {
                 _cachedNode = getTreeEngine().getNode(getTreeMode(), getNodeId()).asEditable();
             } catch (FxApplicationException e) {
                 throw e.asRuntimeException();
             }
         }
         return _cachedNode;
     }
 
     /**
      * Indicate that the node should be refreshed for the next operation.
      */
     protected void refreshNode() {
         _cachedNode = null;
     }
 
     protected FxTreeMode getTreeMode() {
         return treeMode;
     }
 
     protected long getNodeId() {
         return nodeId;
     }
 
     @Override
     protected long getFxTypeId() {
         return getNode().getReferenceTypeId();
     }
 
     @Override
     protected LifeCycleInfo getLifeCycleInfo() {
         return getNode().getReferenceLifeCycleInfo();
     }
 
     @Override
     protected void addCustomProperties(Map<String, Property> properties) {
         addVirtualProperty(properties, VirtualProperties.PARENT_ID, String.valueOf(getNode().getParentNodeId()));
         addVirtualProperty(properties, VirtualProperties.OBJECT_ID, String.valueOf(getNode().getId()));
         addVirtualProperty(properties, VirtualProperties.NAME, getName());
         addVirtualProperty(properties, VirtualProperties.PATH, getNode().getPath());
     }
 
     @Override
     public Serializable getValue(String name) {
         // override some common attributes that do not require a content access
         if (VirtualProperties.NAME.getId().equalsIgnoreCase(name)) {
             return getName();
         } else {
             return super.getValue(name);
         }
     }
 
     @Override
     protected synchronized FxContent getContent() {
         if (_cachedContent == null) {
             try {
                 _cachedContent = EJBLookup.getContentEngine().load(getNode().getReference());
             } catch (FxApplicationException e) {
                 throw e.asRuntimeException();
             }
         }
         return _cachedContent;
     }
 
     @Override
     public String getId() {
         return String.valueOf(getNodeId());
     }
 
     public synchronized void add(CMISObject object) {
         if (toAdd == null) {
             toAdd = Lists.newArrayList();
         }
         toAdd.add(object);
         if (object instanceof FlexiveFolder && ((FlexiveFolder) object).getNode().getParentNodeId() == -1) {
             // set parent in newly created node
             // TODO: this doesn't work when this is a new folder, since the ID hasn't yet been set.
             ((FlexiveFolder) object).getNode().setParentNodeId(getNodeId());
         }
     }
 
     public synchronized void remove(CMISObject object) {
         if (toRemove == null) {
             toRemove = Lists.newArrayList();
         }
         toRemove.add(object);
     }
 
     public Collection<ObjectId> deleteTree(Unfiling unfiling) {
         try {
             if (unfiling == null) {
                 unfiling = Unfiling.DELETE; // according to javadoc
             }
             switch(unfiling) {
                 case DELETE:
                     EJBLookup.getTreeEngine().remove(getNode(), FxTreeRemoveOp.Remove, true);
                     break;
                 case UNFILE:
                     EJBLookup.getTreeEngine().remove(getNode(), FxTreeRemoveOp.Unfile, true);
                     break;
                 case DELETE_SINGLE_FILED:
                     EJBLookup.getTreeEngine().remove(getNode(), FxTreeRemoveOp.RemoveSingleFiled, true);
                     break;
                 default:
                     throw new IllegalArgumentException("Unkown argument: " + unfiling);
             }
             return new ArrayList<ObjectId>(0);
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
     }
 
     public List<Folder> getAncestors() {
         try {
             final List<Folder> result = newArrayList();
             for (long pathNodeId : EJBLookup.getTreeEngine().getIdChain(getTreeMode(), getNodeId())) {
                 if (pathNodeId != getNodeId()) {
                     result.add(new FlexiveFolder(context, pathNodeId));
                 }
             }
             return result;
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
     }
 
     public List<CMISObject> getChildren() {
         return getChildren(null);
     }
 
     public List<CMISObject> getChildren(BaseType type) {
         return getChildren(type, 1);
     }
 
     public List<CMISObject> getChildren(BaseType type, int depth) {
         try {
             final FxTreeNode node = depth > 1 || getNode().getChildren().isEmpty()
                     ? getTreeEngine().getTree(getNode().getMode(), getNodeId(), depth)
                     : getNode();
             final FxEnvironment environment = getEnvironment();
 
             // create a set of all types derived from the folder root type
             final Set<Long> folderTypeIds = SPIUtils.getFolderTypeIds();
 
             final List<CMISObject> result = newArrayList();
             if (type == null) {
                 for (FxTreeNode child : node) {
                     addChild(result, folderTypeIds, child);
                 }
             } else {
                 switch (type) {
                     case DOCUMENT:
                         for (FxTreeNode child : node) {
                             if (!folderTypeIds.contains(child.getReferenceTypeId())) {
                                 addChild(result, folderTypeIds, child);
                             }
                         }
                         break;
                     case FOLDER:
                         for (FxTreeNode child : node) {
                             if (folderTypeIds.contains(child.getReferenceTypeId())) {
                                 addChild(result, folderTypeIds, child);
                             }
                         }
                         break;
                     case RELATIONSHIP:
                         for (FxTreeNode child : node) {
                             if (environment.getType(child.getReferenceTypeId()).isRelation()) {
                                 addChild(result, folderTypeIds, child);
                             }
                         }
                         break;
                     case POLICY:
                         throw new IllegalArgumentException("Policies cannot be filed in the tree.");
                 }
             }
 
             return result;
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
     }
 
     protected Tree<ObjectEntry> getFolderTree(int depth) {
         return getTree(depth, false, true);
     }
 
 
     protected Tree<ObjectEntry> getDescendantTree(int depth) {
         return getTree(depth, true, true);
     }
 
     private Tree<ObjectEntry> getTree(int depth, boolean includeDocuments, boolean includeFolders) {
         try {
             return convertNodeTree(
                     EJBLookup.getTreeEngine().getTree(FxTreeMode.Edit, nodeId, depth),
                     includeDocuments,
                     includeFolders
             );
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
     }
 
     private Tree<ObjectEntry> convertNodeTree(FxTreeNode treeNode, boolean includeDocuments, boolean includeFolders) {
         final Set<Long> folderTypeIds = SPIUtils.getFolderTypeIds();
 
         if (SPIUtils.treatDocumentAsFolder(folderTypeIds, context, treeNode)) {
             // process direct folder children
             final List<Tree<ObjectEntry>> children = Lists.newArrayList();
 
             for (FxTreeNode child : treeNode.getChildren()) {
                 // check if child should be included
                 if (includeDocuments && !folderTypeIds.contains(child.getReferenceTypeId())
                         || includeFolders && folderTypeIds.contains(child.getReferenceTypeId())) {
 
                     // include subtree for child node
                     final Tree<ObjectEntry> childTree = convertNodeTree(child, includeDocuments, includeFolders);
                     if (childTree != null) {
                         children.add(childTree);
                     }
                 }
             }
 
             // create tree
             return new SimpleTree<ObjectEntry>(
                     new FlexiveFolder(context, treeNode),
                     children);
         } else if (includeDocuments) {
             return new SimpleTree<ObjectEntry>(
                     new FlexiveDocument(context, treeNode, treeNode.getParentNodeId()),
                     Lists.<Tree<ObjectEntry>>newArrayListWithCapacity(0)
             );
         } else {
             return null;
         }
 
     }
 
     private void addChild(List<CMISObject> result, Set<Long> folderTypeIds, FxTreeNode child) {
         if (child.getId() != getNodeId()) {
             result.add(SPIUtils.treatDocumentAsFolder(folderTypeIds, context, child)
                     ? new FlexiveFolder(context, child)
                     : new FlexiveDocument(context, child, getNode())
             );
         }
     }
 
     public Document newDocument(String typeId) {
         return new FlexiveNewDocument(context, typeId, this);
     }
 
     public Folder newFolder(String typeId) {
         return new FlexiveNewFolder(context, typeId, this);
     }
 
     public void move(Folder targetFolder, Folder sourceFolder) {
         try {
             // check if the name is available in the target folder
             checkUniqueName(getNodeWithChildren(((FlexiveFolder) targetFolder).getNode()), getName());
 
             // perform move
             getTreeEngine().move(getTreeMode(), getNodeId(), SPIUtils.getNodeId(targetFolder.getId()), -1);
 
             // perform update
             if (_cachedNode != null) {
                 _cachedNode.setParentNodeId(SPIUtils.getNodeId(targetFolder.getId()));
             }
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
     }
 
     public void delete() {
         if (!getChildren().isEmpty()) {
             throw new ConstraintViolationException("Folder not empty.");
         }
         try {
            getTreeEngine().remove(getTreeMode(), getNodeId(), true, true);
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
     }
 
     public void unfile() {
         throw new UnsupportedOperationException();
     }
 
     public FlexiveFolder getParent() {
         return (getNode().getParentNodeId() > 0) ? new FlexiveFolder(context, getTreeMode(), getNode().getParentNodeId()) : null;
     }
 
     public Collection<Folder> getParents() {
         try {
             final List<Folder> result = newArrayList();
             for (long id : getTreeEngine().getIdChain(getTreeMode(), getNodeId())) {
                 if (id != getNodeId()) {
                     result.add(new FlexiveFolder(context, getTreeMode(), id));
                 }
             }
             return result;
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
     }
 
     public List<Relationship> getRelationships(RelationshipDirection direction, String typeId, boolean includeSubRelationshipTypes) {
         throw new UnsupportedOperationException();
     }
 
     public void applyPolicy(Policy policy) {
         throw new UnsupportedOperationException();
     }
 
     public void removePolicy(Policy policy) {
         throw new UnsupportedOperationException();
     }
 
     public Collection<Policy> getPolicies() {
         throw new UnsupportedOperationException();
     }
 
     public Type getType() {
         return new FlexiveType(getEnvironment().getType(
                 isRootNode()
                         ? FxType.ROOT_ID    // tests assume that the root node uses the root type
                         : getNode().getReferenceTypeId()
         ));
     }
 
     @Override
     public String getTypeId() {
         return getType().getId();
     }
 
     @Override
     public String getBaseTypeId() {
         return BaseType.FOLDER.getId();
     }
 
     public Property getProperty(String name) {
         return getProperties().get(name);
     }
 
     public synchronized void save() {
         try {
             getNode().setId(
                     getTreeEngine().save(getNode())
             );
             getContent().save();
 
             processAdd();
             processRemove();
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
     }
 
     protected void processRemove() throws FxApplicationException {
         if (toRemove == null) {
             return;
         }
         for (CMISObject object : toRemove) {
             final FxTreeNode removeNode = getTreeEngine().findChild(getTreeMode(), getNodeId(), FxPK.fromString(object.getId()));
             getTreeEngine().remove(getTreeMode(), removeNode.getId(), FxTreeRemoveOp.RemoveSingleFiled, false);
         }
         toRemove.clear();
     }
 
     protected void processAdd() throws FxApplicationException {
         if (toAdd == null) {
             return;
         }
         final FxTreeNode node = getNodeWithChildren(getNode());
         for (CMISObject object : toAdd) {
             checkUniqueName(node, object.getName());
             getTreeEngine().save(
                     FxTreeNodeEdit.createNew(StringUtils.defaultString(object.getName(), ""))
                             .setParentNodeId(getNodeId())
                             .setReference(SPIUtils.getDocumentId(object.getId()))
             );
         }
         toAdd.clear();
     }
 
     @Override
     public void setName(String name) {
         if (!getNode().isNew() && getParent() != null) {
             checkUniqueName(getNodeWithChildren(getParent().getNode()), name);
         }
         // set as node name
         getNode().setName(name);
         getNode().setLabel(new FxString(false, name));
 
         // also store in all caption properties of the folder instance (usually there is only one, CAPTION)
         final List<FxPropertyAssignment> captionAssignments;
         try {
             captionAssignments = getFxType().getAssignmentsForProperty(EJBLookup.getConfigurationEngine().get(SystemParameters.TREE_CAPTION_PROPERTY));
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
         if (!captionAssignments.isEmpty()) {
             for (FxPropertyAssignment assignment : captionAssignments) {
                 getContent().setValue("/" + assignment.getAlias(), name);
             }
         }
     }
 
     @Override
     public String getName() {
         return isRootNode() ? ROOT_FOLDER_NAME : getNode().getLabel().toString().trim();
     }
 
     public ContentStream getContentStream(String contentStreamId) throws IOException {
         return null;    // no content streams for folders
     }
 
     protected boolean isRootNode() {
         return getNodeId() == FxTreeNode.ROOT_NODE;
     }
 
     @Override
     public Set<QName> getAllowableActions() {
         final Set<QName> actions = super.getAllowableActions();
         // currently any user can add objects to a folder (as long as he can create the objects)
         actions.add(AllowableAction.CAN_ADD_OBJECT_TO_FOLDER);
         actions.add(AllowableAction.CAN_CREATE_DOCUMENT);
         actions.add(AllowableAction.CAN_CREATE_FOLDER);
         return actions;
     }
 
     public String getPathSegment() {
         return getName();
     }
 
 
     /**
      * Proprietary copy method.
      *
      * @param targetFolder  the target folder
      * @param includeContents   whether the folder content should be copied too
      * @return              the new folder instance
      */
     public FlexiveFolder copyTo(FlexiveFolder targetFolder, String newName, boolean includeContents) {
         try {
             if (includeContents) {
                 final TreeEngine treeEngine = EJBLookup.getTreeEngine();
                 final long newFolderId = treeEngine.copy(
                         getTreeMode(), getNodeId(), targetFolder.getNodeId(), -1, true
                 );
                 if (newName != null && !newName.equals(getName())) {
                     // set new name on copied node
                     final FxTreeNodeEdit newNode = treeEngine.getNode(getTreeMode(), newFolderId).asEditable();
                     newNode.setName(newName);
                     newNode.setLabel(new FxString(true, newName));
                     treeEngine.save(newNode);
                 }
                 return new FlexiveFolder(context, newFolderId);
             } else {
                 final FlexiveFolder newFolder = (FlexiveFolder) targetFolder.newFolder(getTypeId());
                 newFolder.setName(newName);
                 newFolder.save();
                 return new FlexiveFolder(context, newFolder.getNodeId());
             }
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
     }
 
     private void copyNodes(long targetNodeId, List<FxTreeNode> nodes) {
         // TODO: should be performed by tree engine  (FX-702)
         for (FxTreeNode node : nodes) {
             try {
                 final FxContent refCopy = EJBLookup.getContentEngine().load(node.getReference()).copyAsNewInstance();
                 final FxPK newRefPK = EJBLookup.getContentEngine().save(refCopy);
                 final long newNodeId = getTreeEngine().save(
                         FxTreeNodeEdit.createNew(node.getName()).setParentNodeId(targetNodeId).setReference(newRefPK)
                 );
                 if (node.getDirectChildCount() > 0) {
                     // copy children recursively
                     copyNodes(newNodeId, node.getChildren());
                 }
             } catch (FxApplicationException e) {
                 if (LOG.isWarnEnabled()) {
                     LOG.warn("Failed to copy node " + node.getId() + ": " + e.getMessage(), e);
                 }
             }
         }
     }
 
     @Override
     public String toString() {
         return "FlexiveFolder[id=" + (getNode().isNew() ? -1 : getNodeId()) + ", name='" + getNode().getName() + "']";
     }
 
 
 }
