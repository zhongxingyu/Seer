 package org.jboss.pressgang.ccms.wrapper;
 
 import java.util.Date;
 
 import org.jboss.pressgang.ccms.provider.RESTProviderFactory;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTCSNodeCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.join.RESTAssignedPropertyTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.components.ComponentBaseRESTEntityWithPropertiesV1;
 import org.jboss.pressgang.ccms.rest.v1.components.ComponentContentSpecV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTagV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTCSNodeV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTContentSpecV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTTranslatedContentSpecV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.enums.RESTContentSpecTypeV1;
 import org.jboss.pressgang.ccms.wrapper.base.RESTBaseWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.UpdateableCollectionWrapper;
 
 public class RESTContentSpecV1Wrapper extends RESTBaseWrapper<ContentSpecWrapper, RESTContentSpecV1> implements ContentSpecWrapper {
 
     protected RESTContentSpecV1Wrapper(final RESTProviderFactory providerFactory, final RESTContentSpecV1 entity, boolean isRevision,
             boolean isNewEntity) {
         super(providerFactory, entity, isRevision, isNewEntity);
     }
 
     @Override
     public CollectionWrapper<TagWrapper> getTags() {
         return getWrapperFactory().createCollection(getProxyEntity().getTags(), RESTTagV1.class, isRevisionEntity());
     }
 
     @Override
     public void setTags(CollectionWrapper<TagWrapper> tags) {
         getEntity().explicitSetTags(tags == null ? null : (RESTTagCollectionV1) tags.unwrap());
     }
 
     @Override
     public CollectionWrapper<TagWrapper> getBookTags() {
         return getWrapperFactory().createCollection(getProxyEntity().getBookTags(), RESTTagV1.class, isRevisionEntity());
     }
 
     @Override
     public void setBookTags(CollectionWrapper<TagWrapper> bookTags) {
         getEntity().explicitSetBookTags(bookTags == null ? null : (RESTTagCollectionV1) bookTags.unwrap());
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
     public UpdateableCollectionWrapper<PropertyTagInContentSpecWrapper> getProperties() {
         final CollectionWrapper<PropertyTagInContentSpecWrapper> collection = getWrapperFactory().createCollection(
                 getProxyEntity().getProperties(), RESTAssignedPropertyTagCollectionV1.class, isRevisionEntity(), getProxyEntity(),
                 PropertyTagInContentSpecWrapper.class);
         return (UpdateableCollectionWrapper<PropertyTagInContentSpecWrapper>) collection;
     }
 
     @Override
     public void setProperties(UpdateableCollectionWrapper<PropertyTagInContentSpecWrapper> properties) {
         getEntity().explicitSetProperties(properties == null ? null : (RESTAssignedPropertyTagCollectionV1) properties.unwrap());
     }
 
     @Override
     public CollectionWrapper<TranslatedContentSpecWrapper> getTranslatedContentSpecs() {
         return getWrapperFactory().createCollection(getProxyEntity().getTranslatedContentSpecs(), RESTTranslatedContentSpecV1.class,
                 isRevisionEntity());
     }
 
     @Override
     public String getTitle() {
         final CSNodeWrapper node = getWrapperFactory().create(ComponentContentSpecV1.returnMetaData(getProxyEntity(), "Title"),
                 isRevisionEntity(), CSNodeWrapper.class);
         return node == null ? null : node.getAdditionalText();
     }
 
     @Override
     public String getProduct() {
         final CSNodeWrapper node = getWrapperFactory().create(ComponentContentSpecV1.returnMetaData(getProxyEntity(), "Product"),
                 isRevisionEntity(), CSNodeWrapper.class);
         return node == null ? null : node.getAdditionalText();
     }
 
     @Override
     public String getVersion() {
         final CSNodeWrapper node = getWrapperFactory().create(ComponentContentSpecV1.returnMetaData(getProxyEntity(), "Version"),
                 isRevisionEntity(), CSNodeWrapper.class);
         return node == null ? null : node.getAdditionalText();
     }
 
     @Override
     public String getLocale() {
         return getProxyEntity().getLocale();
     }
 
     @Override
     public void setLocale(String locale) {
         getEntity().explicitSetLocale(locale);
     }
 
     @Override
     public Integer getType() {
         return RESTContentSpecTypeV1.getContentSpecTypeId(getProxyEntity().getType());
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
     public Date getLastModified() {
         return getProxyEntity().getLastModified();
     }
 
     @Override
     public PropertyTagInContentSpecWrapper getProperty(int propertyId) {
         return getWrapperFactory().create(ComponentBaseRESTEntityWithPropertiesV1.returnProperty(getProxyEntity(), propertyId),
                isRevisionEntity(), PropertyTagInContentSpecWrapper.class);
     }
 
     @Override
     public CSNodeWrapper getMetaData(String metaDataTitle) {
         return getWrapperFactory().create(ComponentContentSpecV1.returnMetaData(getProxyEntity(), metaDataTitle), isRevisionEntity(),
                 CSNodeWrapper.class);
     }
 
     @Override
     public CollectionWrapper<ContentSpecWrapper> getRevisions() {
         return getWrapperFactory().createCollection(getProxyEntity().getRevisions(), RESTContentSpecV1.class, true);
     }
 
     @Override
     public ContentSpecWrapper clone(boolean deepCopy) {
         return new RESTContentSpecV1Wrapper(getProviderFactory(), getEntity().clone(deepCopy), isRevisionEntity(), isNewEntity());
     }
 }
