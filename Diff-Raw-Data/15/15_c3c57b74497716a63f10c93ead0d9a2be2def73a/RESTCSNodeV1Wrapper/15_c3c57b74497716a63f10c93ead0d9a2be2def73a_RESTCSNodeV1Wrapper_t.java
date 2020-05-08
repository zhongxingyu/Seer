 package org.jboss.pressgang.ccms.wrapper;
 
 import org.jboss.pressgang.ccms.provider.RESTProviderFactory;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTCSNodeCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.join.RESTCSRelatedNodeCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTCSNodeV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTContentSpecV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.enums.RESTCSNodeTypeV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.join.RESTCSRelatedNodeV1;
 import org.jboss.pressgang.ccms.wrapper.base.RESTBaseWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.UpdateableCollectionWrapper;
 
 public class RESTCSNodeV1Wrapper extends RESTBaseWrapper<CSNodeWrapper, RESTCSNodeV1> implements CSNodeWrapper {
 
     protected RESTCSNodeV1Wrapper(final RESTProviderFactory providerFactory, final RESTCSNodeV1 entity, boolean isRevision,
             boolean isNewEntity) {
         super(providerFactory, entity, isRevision, isNewEntity);
     }
 
     @Override
     public CollectionWrapper<CSNodeWrapper> getRevisions() {
         return getWrapperFactory().createCollection(getProxyEntity().getRevisions(), RESTCSNodeV1.class, true);
     }
 
     @Override
     public CSNodeWrapper clone(boolean deepCopy) {
         return new RESTCSNodeV1Wrapper(getProviderFactory(), getEntity().clone(deepCopy), isRevisionEntity(), isNewEntity());
     }
 
     @Override
     public UpdateableCollectionWrapper<CSNodeWrapper> getChildren() {
         final CollectionWrapper<CSNodeWrapper> collection = getWrapperFactory().createCollection(getProxyEntity().getChildren_OTM(),
                 RESTCSNodeV1.class, isRevisionEntity());
         return (UpdateableCollectionWrapper<CSNodeWrapper>) collection;
     }
 
     @Override
     public void setChildren(UpdateableCollectionWrapper<CSNodeWrapper> nodes) {
         getEntity().explicitSetChildren_OTM(nodes == null ? null : (RESTCSNodeCollectionV1) nodes.unwrap());
     }
 
     @Override
     public UpdateableCollectionWrapper<CSRelatedNodeWrapper> getRelatedToNodes() {
         final CollectionWrapper<CSRelatedNodeWrapper> collection = getWrapperFactory().createCollection(
                 getProxyEntity().getRelatedToNodes(), RESTCSRelatedNodeV1.class, isRevisionEntity());
         return (UpdateableCollectionWrapper<CSRelatedNodeWrapper>) collection;
     }
 
     @Override
     public void setRelatedToNodes(UpdateableCollectionWrapper<CSRelatedNodeWrapper> relatedToNodes) {
         getEntity().explicitSetRelatedToNodes(relatedToNodes == null ? null : (RESTCSRelatedNodeCollectionV1) relatedToNodes.unwrap());
     }
 
     @Override
     public UpdateableCollectionWrapper<CSRelatedNodeWrapper> getRelatedFromNodes() {
         final CollectionWrapper<CSRelatedNodeWrapper> collection = getWrapperFactory().createCollection(
                 getProxyEntity().getRelatedFromNodes(), RESTCSRelatedNodeV1.class, isRevisionEntity());
         return (UpdateableCollectionWrapper<CSRelatedNodeWrapper>) collection;
     }
 
     @Override
     public void setRelatedFromNodes(UpdateableCollectionWrapper<CSRelatedNodeWrapper> relatedFromNodes) {
         getEntity().explicitSetRelatedFromNodes(
                 relatedFromNodes == null ? null : (RESTCSRelatedNodeCollectionV1) relatedFromNodes.unwrap());
     }
 
     @Override
     public CSNodeWrapper getParent() {
         return getWrapperFactory().create(getProxyEntity().getParent(), isRevisionEntity());
     }
 
     @Override
     public void setParent(CSNodeWrapper parent) {
         getEntity().setParent(parent == null ? null : (RESTCSNodeV1) parent.unwrap());
     }
 
     @Override
     public ContentSpecWrapper getContentSpec() {
         return getWrapperFactory().create(getProxyEntity().getContentSpec(), isRevisionEntity());
     }
 
     @Override
     public void setContentSpec(ContentSpecWrapper contentSpec) {
         getEntity().setContentSpec(contentSpec == null ? null : (RESTContentSpecV1) contentSpec.unwrap());
     }
 
     @Override
     public String getTitle() {
         return getProxyEntity().getTitle();
     }
 
     @Override
     public void setTitle(String title) {
         getEntity().explicitSetTitle(title);
     }
 
     @Override
     public String getTargetId() {
        return getProxyEntity().getTargetId();
     }
 
     @Override
     public void setTargetId(String targetId) {
        getEntity().explicitSetTargetId(targetId);
     }
 
     @Override
     public String getAdditionalText() {
         return getProxyEntity().getAdditionalText();
     }
 
     @Override
     public void setAdditionalText(String additionalText) {
         getEntity().explicitSetAdditionalText(additionalText);
     }
 
     @Override
     public String getCondition() {
         return getProxyEntity().getCondition();
     }
 
     @Override
     public void setCondition(String condition) {
         getEntity().explicitSetCondition(getCondition());
     }
 
     @Override
     public Integer getEntityId() {
         return getProxyEntity().getEntityId();
     }
 
     @Override
     public void setEntityId(Integer id) {
         getEntity().explicitSetEntityId(id);
     }
 
     @Override
     public Integer getEntityRevision() {
         return getProxyEntity().getEntityRevision();
     }
 
     @Override
     public void setEntityRevision(Integer revision) {
         getEntity().explicitSetEntityRevision(revision);
     }
 
     @Override
     public CSNodeWrapper getNextNode() {
         return getWrapperFactory().create(getProxyEntity().getNextNode(), isRevisionEntity());
     }
 
     @Override
     public void setNextNode(CSNodeWrapper nextNode) {
         getEntity().explicitSetNextNode(nextNode == null ? null : (RESTCSNodeV1) nextNode.unwrap());
     }
 
     @Override
     public Integer getNodeType() {
         return RESTCSNodeTypeV1.getNodeTypeId(getProxyEntity().getNodeType());
     }
 
     @Override
     public void setNodeType(Integer typeId) {
         getEntity().explicitSetNodeType(RESTCSNodeTypeV1.getNodeType(typeId));
     }
 }
