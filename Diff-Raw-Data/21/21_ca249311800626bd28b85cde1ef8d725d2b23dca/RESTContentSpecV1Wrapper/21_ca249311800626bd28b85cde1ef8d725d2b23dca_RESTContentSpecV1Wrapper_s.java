 package org.jboss.pressgang.ccms.wrapper;
 
 import java.util.Collection;
 
 import org.jboss.pressgang.ccms.provider.RESTProviderFactory;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTCSNodeCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.join.RESTAssignedPropertyTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.components.ComponentContentSpecV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTCSNodeV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTContentSpecV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.enums.RESTContentSpecTypeV1;
 import org.jboss.pressgang.ccms.utils.constants.CommonConstants;
 import org.jboss.pressgang.ccms.wrapper.base.RESTBaseContentSpecV1Wrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.RESTCollectionWrapperBuilder;
 import org.jboss.pressgang.ccms.wrapper.collection.UpdateableCollectionWrapper;
 
 public class RESTContentSpecV1Wrapper extends RESTBaseContentSpecV1Wrapper<ContentSpecWrapper, RESTContentSpecV1>
         implements ContentSpecWrapper {
 
     protected RESTContentSpecV1Wrapper(final RESTProviderFactory providerFactory, final RESTContentSpecV1 entity, boolean isRevision,
             boolean isNewEntity) {
         super(providerFactory, entity, isRevision, isNewEntity);
     }
 
     protected RESTContentSpecV1Wrapper(final RESTProviderFactory providerFactory, final RESTContentSpecV1 entity, boolean isRevision,
             boolean isNewEntity, final Collection<String> expandedMethods) {
         super(providerFactory, entity, isRevision, isNewEntity, expandedMethods);
     }
 
     @Override
     public void setTags(CollectionWrapper<TagWrapper> tags) {
         getEntity().explicitSetTags(tags == null ? null : (RESTTagCollectionV1) tags.unwrap());
     }
 
     @Override
     public CollectionWrapper<TagWrapper> getBookTags() {
         return RESTCollectionWrapperBuilder.<TagWrapper>newBuilder()
                 .collection(getProxyEntity().getBookTags())
                 .isRevisionCollection(isRevisionEntity())
                 .build();
     }
 
     @Override
     public void setBookTags(CollectionWrapper<TagWrapper> bookTags) {
         getEntity().explicitSetBookTags(bookTags == null ? null : (RESTTagCollectionV1) bookTags.unwrap());
     }
 
     @Override
     public UpdateableCollectionWrapper<CSNodeWrapper> getChildren() {
         return (UpdateableCollectionWrapper<CSNodeWrapper>) RESTCollectionWrapperBuilder.<CSNodeWrapper>newBuilder()
                 .collection(getProxyEntity().getChildren_OTM())
                 .isRevisionCollection(isRevisionEntity())
                 .build();
     }
 
     @Override
     public void setChildren(UpdateableCollectionWrapper<CSNodeWrapper> nodes) {
         getEntity().explicitSetChildren_OTM(nodes == null ? null : (RESTCSNodeCollectionV1) nodes.unwrap());
     }
 
     @Override
     public void setProperties(UpdateableCollectionWrapper<PropertyTagInContentSpecWrapper> properties) {
         getEntity().explicitSetProperties(properties == null ? null : (RESTAssignedPropertyTagCollectionV1) properties.unwrap());
     }
 
     @Override
     public CollectionWrapper<TranslatedContentSpecWrapper> getTranslatedContentSpecs() {
         return RESTCollectionWrapperBuilder.<TranslatedContentSpecWrapper>newBuilder()
                 .providerFactory(getProviderFactory())
                 .collection(getProxyEntity().getTranslatedContentSpecs())
                 .isRevisionCollection(isRevisionEntity())
                 .build();
     }
 
     @Override
     public String getTitle() {
         final RESTCSNodeV1 node = ComponentContentSpecV1.returnMetaData(getProxyEntity(), CommonConstants.CS_TITLE_TITLE);
         return node == null ? null : node.getAdditionalText();
     }
 
     @Override
     public String getProduct() {
         final RESTCSNodeV1 node = ComponentContentSpecV1.returnMetaData(getProxyEntity(), CommonConstants.CS_PRODUCT_TITLE);
         return node == null ? null : node.getAdditionalText();
     }
 
     @Override
     public String getVersion() {
         final RESTCSNodeV1 node = ComponentContentSpecV1.returnMetaData(getProxyEntity(), CommonConstants.CS_VERSION_TITLE);
         return node == null ? null : node.getAdditionalText();
     }
 
     @Override
     public void setLocale(String locale) {
         getEntity().explicitSetLocale(locale);
     }
 
     @Override
     public void setType(Integer typeId) {
         getEntity().explicitSetType(RESTContentSpecTypeV1.getContentSpecType(typeId));
     }
 
     @Override
     public String getCondition() {
         return getProxyEntity().getCondition();
     }
 
     @Override
     public void setCondition(String condition) {
         getEntity().explicitSetCondition(condition);
     }
 
     @Override
     public CSNodeWrapper getMetaData(String metaDataTitle) {
         return RESTEntityWrapperBuilder.newBuilder()
                 .providerFactory(getProviderFactory())
                 .entity(ComponentContentSpecV1.returnMetaData(getProxyEntity(), metaDataTitle))
                 .isRevision(isRevisionEntity())
                 .build();
     }
 
     @Override
     public ContentSpecWrapper clone(boolean deepCopy) {
         return new RESTContentSpecV1Wrapper(getProviderFactory(), getEntity().clone(deepCopy), isRevisionEntity(), isNewEntity());
     }
 }
