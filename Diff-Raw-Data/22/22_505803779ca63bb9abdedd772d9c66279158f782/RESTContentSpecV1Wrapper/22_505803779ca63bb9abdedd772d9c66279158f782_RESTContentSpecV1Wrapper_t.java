 /*
   Copyright 2011-2014 Red Hat, Inc
 
   This file is part of PressGang CCMS.
 
   PressGang CCMS is free software: you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
 
   PressGang CCMS is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Lesser General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License
   along with PressGang CCMS.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package org.jboss.pressgang.ccms.wrapper;
 
 import java.util.Collection;
 
import org.jboss.pressgang.ccms.provider.RESTCSNodeProvider;
 import org.jboss.pressgang.ccms.provider.RESTProviderFactory;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTCSNodeCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.join.RESTAssignedPropertyTagCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.components.ComponentContentSpecV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTLocaleV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTCSNodeV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTCSTranslationDetailV1;
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
                 .providerFactory(getProviderFactory())
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
                 .providerFactory(getProviderFactory())
                 .collection(getProxyEntity().getChildren_OTM())
                .expandedEntityMethods(RESTCSNodeProvider.DEFAULT_METHODS)
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
     public void setLocale(LocaleWrapper locale) {
         getEntity().explicitSetLocale(locale == null ? null : (RESTLocaleV1) locale.unwrap());
     }
 
     @Override
     public void setTranslationDetails(CSTranslationDetailWrapper translationDetails) {
         getEntity().explicitSetTranslationDetails(
                 translationDetails == null ? null : (RESTCSTranslationDetailV1) translationDetails.unwrap());
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
