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
import static com.flexive.shared.EJBLookup.getContentEngine;
import static com.flexive.shared.EJBLookup.getTreeEngine;
 import com.flexive.shared.content.FxContent;
 import com.flexive.shared.content.FxContentVersionInfo;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxStreamException;
 import com.flexive.shared.security.LifeCycleInfo;
 import com.flexive.shared.structure.FxDataType;
 import com.flexive.shared.structure.FxPropertyAssignment;
 import com.flexive.shared.tree.FxTreeMode;
 import com.flexive.shared.tree.FxTreeNode;
 import com.flexive.shared.tree.FxTreeNodeEdit;
 import com.flexive.shared.value.BinaryDescriptor;
 import com.flexive.shared.value.FxBinary;
 import com.flexive.shared.value.FxString;
 import com.flexive.shared.value.FxValue;
 import com.google.common.collect.Lists;
import static com.google.common.collect.Lists.newArrayList;
 import org.apache.chemistry.*;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
import org.apache.chemistry.impl.simple.SimpleContentStream;
import org.apache.commons.lang.StringUtils;
 
 /**
  * A non-folder FxContent instance.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class FlexiveDocument extends FlexiveObjectEntry implements Document {
     private final FxPK pk;    // only setting PK leads to lazy loading of the content
     private final String treePathName;
     private final long folderParentId;
 
     private String newTreeName;
 
     private FxContent _cachedContent;
     private FxString _cachedCaption;
     private long _cachedTypeId = -1;
     private FxTreeNode _cachedFolderParent; // the parent if the document was loaded from a folder
     private LifeCycleInfo _cachedLifeCycleInfo;
     private FxTreeNode _cachedFolderNode;
 
     FlexiveDocument(FlexiveConnection.Context context, FxContent content) {
         super(context);
         this._cachedContent = content;
         this.pk = content.getPk();
         this.folderParentId = -1;
         this.treePathName = null;
     }
 
     FlexiveDocument(FlexiveConnection.Context context, FxPK pk) {
         this(context, pk, -1, null, null);
     }
 
     FlexiveDocument(FlexiveConnection.Context context, FxTreeNode node, FxTreeNode folderParent) {
         this(
                 context,
                 node.getReference(),
                 folderParent.getId(),
                 folderParent,
                 node.getLabel().toString()
         );
         applyNodeInfo(node);
     }
 
     FlexiveDocument(FlexiveConnection.Context context, FxTreeNode node, long folderParentId) {
         this(
                 context,
                 node.getReference(),
                 folderParentId,
                 null,
                 node.getLabel().toString()
         );
         applyNodeInfo(node);
     }
 
     FlexiveDocument(FlexiveConnection.Context context, FxPK pk, long folderParentId, FxTreeNode folderParent,
                               String treePathName) {
         super(context);
         this.pk = pk;
         this._cachedFolderParent = folderParent;
         this.folderParentId = folderParent == null ? folderParentId : folderParent.getId();
         this.treePathName = treePathName;
     }
 
     protected final void applyNodeInfo(FxTreeNode node) {
         this._cachedCaption = node.getLabel();
         this._cachedTypeId = node.getReferenceTypeId();
         this._cachedLifeCycleInfo = node.getReferenceLifeCycleInfo();
         this._cachedFolderNode = node;
     }
 
     @Override
     protected FxContent getContent() {
         if (_cachedContent == null) {
             try {
                 _cachedContent = EJBLookup.getContentEngine().load(pk);
             } catch (FxApplicationException e) {
                 throw e.asRuntimeException();
             }
         }
         return _cachedContent;
     }
 
     @Override
     public String getId() {
         return getPK().toString();
     }
 
     @Override
     protected long getFxTypeId() {
         if (_cachedTypeId == -1) {
             _cachedTypeId = getContent().getTypeId();
         }
         return _cachedTypeId;
     }
 
     @Override
     protected LifeCycleInfo getLifeCycleInfo() {
         if (_cachedLifeCycleInfo != null) {
             return _cachedLifeCycleInfo;
         } else {
             return super.getLifeCycleInfo();
         }
     }
 
     protected FxPK getPK() {
         return pk;
     }
 
     protected FxTreeNode getFolderParentNode() {
         if (_cachedFolderParent == null && folderParentId != -1) {
             try {
                 _cachedFolderParent = getTreeEngine().getNode(FxTreeMode.Edit, folderParentId);
             } catch (FxApplicationException e) {
                 throw e.asRuntimeException();
             }
         }
         return _cachedFolderParent;
     }
 
     @Override
     protected void addCustomProperties(Map<String, Property> properties) {
         // add PK
         addVirtualProperty(properties, VirtualProperties.OBJECT_ID, getPK().toString());
         // add direct parent folder if the content was loaded from a folder
         if (folderParentId != -1) {
             addVirtualProperty(properties, VirtualProperties.PARENT_ID, String.valueOf(folderParentId));
         }
         // set content name (= caption)
         addVirtualProperty(properties, VirtualProperties.NAME, getName());
         // add binary stream, if available
         final FxPropertyAssignment binaryAssignment = SPIUtils.getContentStreamAssignment(getFxType());
         if (binaryAssignment != null && getContent().containsValue(binaryAssignment.getXPath())) {
             if (binaryAssignment.getProperty().getDataType() == FxDataType.Binary) {
                 final BinaryDescriptor binary = ((FxBinary) getContent().getValue(binaryAssignment.getXPath())).getBestTranslation();
                 //addVirtualProperty(properties, VirtualProperties.CONTENT_STREAM_FILENAME, binary.getName());
                 // TODO: the AtomPub provider currently assumes it's an Integer, but should be a Long really
                 addVirtualProperty(properties, VirtualProperties.CONTENT_STREAM_LENGTH, (int) binary.getSize());
                 addVirtualProperty(properties, VirtualProperties.CONTENT_STREAM_MIME_TYPE, binary.getMimeType());
                 // TODO: currently, the URI does not seem to be necessary, at least for the AtomPub binding, since that
                 // will be handled by Abdera
                 //addVirtualProperty(properties, VirtualProperties.CONTENT_STREAM_URI, context.getConfig().getContentStreamURI() + binary.getId());
             } else {
                 final String value = getContent().getValue(binaryAssignment.getXPath()).getBestTranslation().toString();
                 addVirtualProperty(properties, VirtualProperties.CONTENT_STREAM_LENGTH, value.getBytes().length);
                 // TODO: mime type?
                 addVirtualProperty(properties, VirtualProperties.CONTENT_STREAM_MIME_TYPE, "unknown/unknown");
             }
         }
     }
 
     public Document checkOut() {
         return new FlexiveDocument(context, context.getPwcManager().checkout(getContent()));
     }
 
     public void cancelCheckOut() {
         context.getPwcManager().cancelCheckout(getPK());
     }
 
     public Document checkIn(boolean major, String comment) {
         return new FlexiveDocument(context, context.getPwcManager().checkin(getContent()));
     }
 
     public Document getLatestVersion(boolean major) {
         // TODO: support major version flag
         try {
             final FxContentVersionInfo info = getContentEngine().getContentVersionInfo(getPK());
             assert info.getVersions().length > 0 : "Content must have at least one version";
             final Integer versionId = info.getVersions()[info.getVersions().length - 1];
              return (versionId == getPK().getVersion())
                      ? this
                      : new FlexiveDocument(context, new FxPK(getPK().getId(), versionId));
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
     }
 
     public Collection<Document> getAllVersions() {
         final List<Document> result = newArrayList();
         try {
             final FxContentVersionInfo info = getContentEngine().getContentVersionInfo(getPK());
             for (int version : info.getVersions()) {
                 if (version == getPK().getVersion()) {
                     result.add(this);
                 } else {
                     result.add(new FlexiveDocument(
                             context,
                             getContentEngine().load(new FxPK(getPK().getId(), version))
                     ));
                 }
             }
             return result;
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
     }
 
     public void deleteAllVersions() {
         try {
             getContentEngine().remove(getPK());
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
     }
 
     public InputStream getStream() throws IOException {
         final ContentStream stream = getContentStream();
         return stream != null ? stream.getStream() : null;
     }
 
     public ContentStream getContentStream() throws IOException {
         final FxPropertyAssignment assignment = SPIUtils.getContentStreamAssignment(getFxType());
         if (assignment == null || !getContent().containsValue(assignment.getXPath())) {
             return null;
         }
         if (assignment.getProperty().getDataType() == FxDataType.Binary) {
             return new FlexiveContentStream((FxBinary) getContent().getValue(assignment.getXPath()));
         } else {
             final String streamOption = assignment.getOption(SPIUtils.OPTION_CONTENTSTREAM).getValue();
             return new SimpleContentStream(
                     getContent().getValue(assignment.getXPath()).getBestTranslation().toString().getBytes("UTF-8"),
                     // use MIME type set in stream value if available, otherwise default to text/plain
                     StringUtils.countMatches(streamOption, "/") == 1 ? streamOption : "text/plain",
                     "content"
             );
         }
     }
 
     public void setContentStream(ContentStream contentStream) throws IOException {
         final FxPropertyAssignment assignment = SPIUtils.getContentStreamAssignment(getFxType());
         if (assignment == null) {
             throw new IllegalArgumentException("Content of type " + getFxType().getName() + " does not have a suitable binary property.");
         }
         try {
             if (contentStream == null) {
                 getContent().remove(assignment.getXPath());
             } else {
                 if (assignment.getProperty().getDataType() == FxDataType.Binary) {
                     getContent().setValue(assignment.getXPath(), new BinaryDescriptor(
                             contentStream.getFileName(),
                             contentStream.getLength(),
                             contentStream.getMimeType(),
                             contentStream.getStream())
                     );
                 } else {
                     final String content = FxSharedUtils.loadFromInputStream(contentStream.getStream());
                     final FxValue value = assignment.getEmptyValue();
                     value.setValue(value.fromString(content));
                     getContent().setValue(assignment.getXPath(),  value);
                 }
             }
         } catch (FxStreamException e) {
             throw e.asRuntimeException();
         }
     }
 
     public void move(Folder targetFolder, Folder sourceFolder) {
         if (sourceFolder == null && getParents().size() > 1) {
             throw new IllegalArgumentException("Document " + getId() + " is multi-filed, but no source folder was specified.");
         }
         final Folder source = sourceFolder != null ? sourceFolder : getParent();
         try {
             // get our node ID
             final FxTreeNode node = getTreeEngine().findChild(
                     FxTreeMode.Edit,
                     SPIUtils.getNodeId(source.getId()),
                     getPK()
             );
 
             // move to new location
             getTreeEngine().move(
                     FxTreeMode.Edit,
                     node.getId(),
                     SPIUtils.getNodeId(targetFolder.getId()),
                     Integer.MAX_VALUE
             );
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
     }
 
     public void delete() {
         try {
             getContentEngine().removeVersion(getPK());
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
     }
 
     public void unfile() {
         try {
             final List<FxTreeNode> nodes = getTreeEngine().getNodesWithReference(FxTreeMode.Edit, getPK().getId());
             for (FxTreeNode node : nodes) {
                 // remove node with our document reference. Since there could be children that CMIS cannot see,
                 // unfile the children as well.
                getTreeEngine().remove(node, false, true);
             }
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
     }
 
     public Folder getParent() {
         final List<FxTreeNode> nodes = getTreeNodes();
         if (nodes.size() > 1) {
             throw new IllegalArgumentException("Document " + getPK() + " is multi-filed.");
         }
         return nodes.isEmpty() ? null : new FlexiveFolder(context, nodes.get(0).getParentNodeId());
     }
 
     public Collection<Folder> getParents() {
         final List<FxTreeNode> nodes = getTreeNodes();
         final List<Folder> result = Lists.newArrayListWithCapacity(nodes.size());
         for (FxTreeNode node : nodes) {
             if (node.getParentNodeId() > 0) {
                 result.add(new FlexiveFolder(context, node.getParentNodeId()));
             }
         }
         return result;
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
         return new FlexiveType(getFxType());
     }
 
     @Override
     public String getBaseTypeId() {
         return BaseType.DOCUMENT.getId();
     }
 
     public Property getProperty(String name) {
         return getProperties().get(name);
     }
 
     public void save() {
         try {
             // TODO: sync folders
             getContent().save();
 
             // sync folder name
             if (newTreeName != null && getFolderNode() != null) {
                 final FxTreeNodeEdit editNode = getFolderNode().asEditable();
                 editNode.getLabel().setValue(newTreeName);
                 editNode.setName(newTreeName);
                 getTreeEngine().save(editNode);
             }
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
     }
 
     @Override
     public String getName() {
         if (treePathName != null) {
             // loaded from a tree node, use the tree node path
             return treePathName;
         } if (_cachedCaption != null) {
             // avoid content load if caption was provided from the caller
             return _cachedCaption.getBestTranslation();
         } else if (getContent().hasCaption()) {
             return getContent().getCaption().getBestTranslation();
         } else {
             return getPK().toString();
         }
     }
 
     @Override
     public void setName(String name) {
         if (getFolderParentNode() != null) {
             checkUniqueName(getNodeWithChildren(getFolderParentNode()), name);
         }
         final String captionXPath = getCaptionXPath();
         if (captionXPath != null) {
             getContent().setValue(captionXPath, name);
             _cachedCaption = null;
         } else {
             throw new IllegalArgumentException("No caption property available for type: " + getTypeId());
         }
         this.newTreeName = name;
     }
 
     public ContentStream getContentStream(String contentStreamId) throws IOException {
         // TODO: support contentStreamId
         return getContentStream();
     }
 
     /**
      * Return an independent copy of this document.
      *
      * @return  an independent copy of this document.
      *
      * // TODO remove
      */
     public FlexiveObjectEntry copy() {
         return new FlexiveNewDocument(context, getContent().copyAsNewInstance(), null);
     }
 
     public Document copy(Folder folder) throws NameConstraintViolationException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public String getPathSegment() {
         return getName();
     }
 
     private List<FxTreeNode> getTreeNodes() {
         final List<FxTreeNode> nodes;
         try {
             nodes = getTreeEngine().getNodesWithReference(FxTreeMode.Edit, getContent().getId());
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
         return nodes;
     }
 
     /**
      * @return  the tree node if this content was loaded from a folder, or null otherwise
      */
     private FxTreeNode getFolderNode() {
         if (_cachedFolderNode == null && folderParentId != -1) {
             try {
                 _cachedFolderNode = getTreeEngine().findChild(FxTreeMode.Edit, folderParentId, getPK());
             } catch (FxApplicationException e) {
                 throw e.asRuntimeException();
             }
         }
         return _cachedFolderNode;
     }
 
 }
