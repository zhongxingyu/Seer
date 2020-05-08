 package org.jboss.pressgang.ccms.wrapper;
 
 import java.util.List;
 
 import org.jboss.pressgang.ccms.model.contentspec.CSNode;
 import org.jboss.pressgang.ccms.model.contentspec.CSNodeToCSNode;
 import org.jboss.pressgang.ccms.model.contentspec.ContentSpec;
 import org.jboss.pressgang.ccms.model.utils.EnversUtilities;
 import org.jboss.pressgang.ccms.provider.DBProviderFactory;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.UpdateableCollectionWrapper;
 
 public class DBCSNodeWrapper extends DBBaseWrapper<CSNodeWrapper> implements CSNodeWrapper {
     private final CSNode csNode;
 
     public DBCSNodeWrapper(final DBProviderFactory providerFactory, final CSNode csNode, boolean isRevision) {
         super(providerFactory, isRevision);
         this.csNode = csNode;
     }
 
     protected CSNode getCSNode() {
         return csNode;
     }
 
     @Override
     public UpdateableCollectionWrapper<CSRelatedNodeWrapper> getRelatedToNodes() {
         final CollectionWrapper<CSRelatedNodeWrapper> collection = getWrapperFactory().createCollection(getCSNode().getRelatedToNodes(),
                 CSNodeToCSNode.class, isRevisionEntity());
         return (UpdateableCollectionWrapper<CSRelatedNodeWrapper>) collection;
     }
 
     @Override
     public void setRelatedToNodes(UpdateableCollectionWrapper<CSRelatedNodeWrapper> relatedToNodes) {
         if (relatedToNodes == null) return;
 
         final List<CSRelatedNodeWrapper> addRelatedToNodes = relatedToNodes.getAddItems();
         final List<CSRelatedNodeWrapper> removeRelatedToNodes = relatedToNodes.getRemoveItems();
         /*
          * There is no need to do update the related to nodes as when the original entities are altered they will automatically be updated
          * when using database entities.
          */
         //final List<PropertyTagInTopicWrapper> updateRelatedToNodes = relatedToNodes.getUpdateItems();
 
         // Add Related Nodes
         for (final CSRelatedNodeWrapper addRelatedToNode : addRelatedToNodes) {
             getCSNode().addRelatedTo((CSNodeToCSNode) addRelatedToNode.unwrap());
         }
 
         // Remove Related Nodes
         for (final CSRelatedNodeWrapper removeRelatedToNode : removeRelatedToNodes) {
             getCSNode().removeRelatedTo((CSNodeToCSNode) removeRelatedToNode.unwrap());
         }
     }
 
     @Override
     public UpdateableCollectionWrapper<CSRelatedNodeWrapper> getRelatedFromNodes() {
         final CollectionWrapper<CSRelatedNodeWrapper> collection = getWrapperFactory().createCollection(getCSNode().getRelatedFromNodes(),
                 CSNodeToCSNode.class, isRevisionEntity());
         return (UpdateableCollectionWrapper<CSRelatedNodeWrapper>) collection;
     }
 
     @Override
     public void setRelatedFromNodes(UpdateableCollectionWrapper<CSRelatedNodeWrapper> relatedFromNodes) {
         if (relatedFromNodes == null) return;
 
         final List<CSRelatedNodeWrapper> addRelatedFromNodes = relatedFromNodes.getAddItems();
         final List<CSRelatedNodeWrapper> removeRelatedFromNodes = relatedFromNodes.getRemoveItems();
         /*
          * There is no need to do update the related from nodes as when the original entities are altered they will automatically be
          * updated when using database entities.
          */
         //final List<CSRelatedNodeWrapper> updateRelatedFromNodes = relatedFromNodes.getUpdateItems();
 
         // Add Related Nodes
         for (final CSRelatedNodeWrapper addRelatedFromNode : addRelatedFromNodes) {
             getCSNode().addRelatedFrom((CSNodeToCSNode) addRelatedFromNode.unwrap());
         }
 
         // Remove Related Nodes
         for (final CSRelatedNodeWrapper removeRelatedFromNode : removeRelatedFromNodes) {
             getCSNode().removeRelatedFrom((CSNodeToCSNode) removeRelatedFromNode.unwrap());
         }
     }
 
     @Override
     public CSNodeWrapper getParent() {
         return getWrapperFactory().create(getCSNode().getParent(), isRevisionEntity());
     }
 
     @Override
     public void setParent(CSNodeWrapper parent) {
         getCSNode().setParent(parent == null ? null : (CSNode) parent.unwrap());
     }
 
     @Override
     public ContentSpecWrapper getContentSpec() {
         return getWrapperFactory().create(getCSNode().getContentSpec(), isRevisionEntity());
     }
 
     @Override
     public void setContentSpec(ContentSpecWrapper contentSpec) {
         getCSNode().setContentSpec(contentSpec == null ? null : (ContentSpec) contentSpec.unwrap());
     }
 
     @Override
     public UpdateableCollectionWrapper<CSNodeWrapper> getChildren() {
         final CollectionWrapper<CSNodeWrapper> collection = getWrapperFactory().createCollection(getCSNode().getChildren(), CSNode.class,
                 isRevisionEntity());
         return (UpdateableCollectionWrapper<CSNodeWrapper>) collection;
     }
 
     @Override
     public void setChildren(UpdateableCollectionWrapper<CSNodeWrapper> nodes) {
         if (nodes == null) return;
 
         final List<CSNodeWrapper> addNodes = nodes.getAddItems();
         final List<CSNodeWrapper> removeNodes = nodes.getRemoveItems();
         /*
          * There is no need to do update the child nodes as when the original entities are altered they will automatically be
          * updated when using database entities.
          */
         //final List<CSNodeWrapper> updateNodes = relatedFromNodes.getUpdateItems();
 
         // Add Child Nodes
         for (final CSNodeWrapper addNode : addNodes) {
             getCSNode().addChild((CSNode) addNode.unwrap());
         }
 
         // Remove Child Nodes
         for (final CSNodeWrapper removeNode : removeNodes) {
             getCSNode().removeChild((CSNode) removeNode.unwrap());
         }
     }
 
     @Override
     public String getTitle() {
         return getCSNode().getCSNodeTitle();
     }
 
     @Override
     public void setTitle(String title) {
         getCSNode().setCSNodeTitle(title);
     }
 
     @Override
     public String getTargetId() {
         return getCSNode().getCSNodeTargetId();
     }
 
     @Override
     public void setTargetId(String targetId) {
         getCSNode().setCSNodeTargetId(targetId);
     }
 
     @Override
     public String getAdditionalText() {
         return getCSNode().getAdditionalText();
     }
 
     @Override
     public void setAdditionalText(String additionalText) {
         getCSNode().setAdditionalText(additionalText);
     }
 
     @Override
     public String getCondition() {
         return getCSNode().getCondition();
     }
 
     @Override
     public void setCondition(String condition) {
         getCSNode().setCondition(condition);
     }
 
     @Override
     public Integer getEntityId() {
         return getCSNode().getEntityId();
     }
 
     @Override
     public void setEntityId(Integer id) {
         getCSNode().setEntityId(id);
     }
 
     @Override
     public Integer getEntityRevision() {
         return getCSNode().getEntityRevision();
     }
 
     @Override
     public void setEntityRevision(Integer revision) {
         getCSNode().setEntityRevision(revision);
     }
 
     @Override
     public CSNodeWrapper getNextNode() {
         return getWrapperFactory().create(getCSNode().getNext(), isRevisionEntity());
     }
 
     @Override
     public void setNextNode(CSNodeWrapper nextNode) {
        getCSNode().setNextAndClean(nextNode == null ? null : (CSNode) nextNode.unwrap());
     }
 
     @Override
     public Integer getNodeType() {
         return getCSNode().getCSNodeType();
     }
 
     @Override
     public void setNodeType(Integer typeId) {
         getCSNode().setCSNodeType(typeId);
     }
 
     @Override
     public Integer getId() {
         return getCSNode().getId();
     }
 
     @Override
     public void setId(Integer id) {
         getCSNode().setCSNodeId(id);
     }
 
     @Override
     public Integer getRevision() {
         return (Integer) getCSNode().getRevision();
     }
 
     @Override
     public CollectionWrapper<CSNodeWrapper> getRevisions() {
         return getWrapperFactory().createCollection(EnversUtilities.getRevisionEntities(getEntityManager(), getCSNode()), CSNode.class,
                 true);
     }
 
     @Override
     public CSNode unwrap() {
         return csNode;
     }
 }
