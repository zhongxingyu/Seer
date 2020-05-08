 package org.jboss.pressgang.ccms.wrapper.base;
 
 import java.util.Collection;
 import java.util.Date;
 
 import org.jboss.pressgang.ccms.provider.RESTProviderFactory;
 import org.jboss.pressgang.ccms.rest.v1.components.ComponentBaseRESTEntityWithPropertiesV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.base.RESTBaseContentSpecV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.enums.RESTContentSpecTypeV1;
 import org.jboss.pressgang.ccms.wrapper.PropertyTagInContentSpecWrapper;
 import org.jboss.pressgang.ccms.wrapper.RESTEntityWrapperBuilder;
 import org.jboss.pressgang.ccms.wrapper.TagWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.RESTCollectionWrapperBuilder;
 import org.jboss.pressgang.ccms.wrapper.collection.UpdateableCollectionWrapper;
 
 public abstract class RESTBaseContentSpecV1Wrapper<T extends BaseContentSpecWrapper<T>, U extends RESTBaseContentSpecV1<U, ?,
         ?>> extends RESTBaseEntityWrapper<T, U> implements BaseContentSpecWrapper<T> {
 
     protected RESTBaseContentSpecV1Wrapper(RESTProviderFactory providerFactory, U entity, boolean isRevision, boolean isNewEntity) {
         super(providerFactory, entity, isRevision, isNewEntity);
     }
 
     protected RESTBaseContentSpecV1Wrapper(RESTProviderFactory providerFactory, U entity, boolean isRevision, boolean isNewEntity,
             Collection<String> expandedMethods) {
         super(providerFactory, entity, isRevision, isNewEntity, expandedMethods);
     }
 
     @Override
     public CollectionWrapper<TagWrapper> getTags() {
         return RESTCollectionWrapperBuilder.<TagWrapper>newBuilder()
                .providerFactory(getProviderFactory())
                 .collection(getProxyEntity().getTags())
                 .isRevisionCollection(isRevisionEntity())
                 .build();
     }
 
     @Override
     public UpdateableCollectionWrapper<PropertyTagInContentSpecWrapper> getProperties() {
         return (UpdateableCollectionWrapper<PropertyTagInContentSpecWrapper>)
                 RESTCollectionWrapperBuilder.<PropertyTagInContentSpecWrapper>newBuilder()
                        .providerFactory(getProviderFactory())
                         .collection(getProxyEntity().getProperties())
                         .isRevisionCollection(isRevisionEntity())
                         .parent(getProxyEntity())
                         .entityWrapperInterface(PropertyTagInContentSpecWrapper.class)
                         .build();
     }
 
     @Override
     public String getLocale() {
         return getProxyEntity().getLocale();
     }
 
     @Override
     public Integer getType() {
         return RESTContentSpecTypeV1.getContentSpecTypeId(getProxyEntity().getType());
     }
 
     @Override
     public Date getLastModified() {
         return getProxyEntity().getLastModified();
     }
 
     @Override
     public String getErrors() {
         return getProxyEntity().getErrors();
     }
 
     @Override
     public void setErrors(String errors) {
         getEntity().setErrors(errors);
     }
 
     @Override
     public String getFailed() {
         return getProxyEntity().getFailedContentSpec();
     }
 
     @Override
     public void setFailed(String failed) {
         getEntity().setFailedContentSpec(failed);
     }
 
     @Override
     public PropertyTagInContentSpecWrapper getProperty(int propertyId) {
         return RESTEntityWrapperBuilder.newBuilder()
                 .providerFactory(getProviderFactory())
                 .entity(ComponentBaseRESTEntityWithPropertiesV1.returnProperty(getProxyEntity(), propertyId))
                 .isRevision(isRevisionEntity())
                 .parent(getProxyEntity())
                 .wrapperInterface(PropertyTagInContentSpecWrapper.class)
                 .build();
     }
 }
