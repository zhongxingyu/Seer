 /**************************************************************************************************
  * This file is part of [SpringAtom] Copyright [kornicameister@gmail.com][2013]                   *
  *                                                                                                *
  * [SpringAtom] is free software: you can redistribute it and/or modify                           *
  * it under the terms of the GNU General Public License as published by                           *
  * the Free Software Foundation, either version 3 of the License, or                              *
  * (at your option) any later version.                                                            *
  *                                                                                                *
  * [SpringAtom] is distributed in the hope that it will be useful,                                *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of                                 *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                  *
  * GNU General Public License for more details.                                                   *
  *                                                                                                *
  * You should have received a copy of the GNU General Public License                              *
  * along with [SpringAtom].  If not, see <http://www.gnu.org/licenses/gpl.html>.                  *
  **************************************************************************************************/
 
 package org.agatom.springatom.ip.component.builder;
 
 import com.google.common.collect.Lists;
 import org.agatom.springatom.component.builders.ComponentBuilders;
 import org.agatom.springatom.component.builders.EntityAware;
 import org.agatom.springatom.component.builders.annotation.ComponentBuilds;
 import org.agatom.springatom.component.builders.exception.ComponentException;
 import org.agatom.springatom.component.builders.exception.ComponentPathEvaluationException;
 import org.agatom.springatom.component.data.ComponentDataRequest;
 import org.agatom.springatom.component.data.ComponentDataResponse;
 import org.agatom.springatom.component.elements.value.BuilderLink;
 import org.agatom.springatom.component.elements.value.DelegatedLink;
 import org.agatom.springatom.component.meta.LayoutType;
 import org.agatom.springatom.core.invoke.InvokeUtils;
 import org.agatom.springatom.ip.SEntityInfoPage;
 import org.agatom.springatom.ip.component.elements.InfoPageComponent;
 import org.agatom.springatom.ip.component.elements.InfoPagePanelComponent;
 import org.agatom.springatom.ip.component.elements.attributes.InfoPageAttributeComponent;
 import org.agatom.springatom.ip.component.elements.meta.AttributeDisplayAs;
 import org.agatom.springatom.ip.data.EntityInfoPageResponse;
 import org.agatom.springatom.ip.mapping.InfoPageMappings;
 import org.agatom.springatom.server.model.beans.PersistentVersionedObject;
 import org.agatom.springatom.server.model.beans.car.SCar;
 import org.agatom.springatom.server.model.beans.car.SCarMaster;
 import org.agatom.springatom.server.model.beans.user.SUser;
 import org.agatom.springatom.server.model.descriptors.EntityDescriptor;
 import org.agatom.springatom.server.model.descriptors.descriptor.EntityDescriptors;
 import org.agatom.springatom.server.repository.SBasicRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Persistable;
 import org.springframework.hateoas.Link;
 import org.springframework.util.ClassUtils;
 import org.springframework.util.StringUtils;
 
 import javax.persistence.metamodel.Attribute;
 import javax.persistence.metamodel.EntityType;
 import javax.persistence.metamodel.PluralAttribute;
 import java.io.Serializable;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author kornicameister
  * @version 0.0.1
  * @since 0.0.1
  */
 abstract public class EntityInfoPageComponentBuilder<Y extends Persistable<?>>
         extends InfoPageComponentBuilder
         implements EntityAware<Y> {
 
     protected Class<Y>                          entity;
     protected SBasicRepository<Y, Serializable> repository;
     @Autowired
     protected EntityDescriptors                 descriptors;
     @Autowired
     private   InfoPageMappings                  pageMappings;
     @Autowired
     private   ComponentBuilders                 componentBuilders;
 
     @Override
     public void setEntity(final Class<Y> entity) {
         this.entity = entity;
     }
 
     @Override
     public void setRepository(final SBasicRepository<Y, Serializable> repository) {
         this.repository = repository;
     }
 
     @Override
     protected void postProcessDefinition(final InfoPageComponent definition) {
         if (ClassUtils.isAssignable(PersistentVersionedObject.class, this.entity)) {
             this.populateSystemPanel(this.helper.newSystemPanel(definition, LayoutType.VERTICAL));
         }
     }
 
     @Override
     protected ComponentDataResponse buildData(final ComponentDataRequest dataRequest) throws ComponentException {
         final Long objectId = dataRequest.getLong("objectId");
         final Y object = this.repository.findOne(objectId);
         this.logger.trace(String.format("processing object %s=%s", ClassUtils.getShortName(object.getClass()), object.getId()));
         return new EntityInfoPageResponse() {
             @Override
             public Object getValueForPath(final String path) throws ComponentException {
                 logger.trace(String.format("processing path %s", path));
                 Object value = InvokeUtils.invokeGetter(object, path);
 
                 if (value != null) {
                     final InfoPageAttributeComponent attributeComponent = getAttributeForPath(path);
 
                     switch (attributeComponent.getDisplayAs()) {
                         case INFOPAGE: {
                             if (value instanceof Persistable) {
 
                                 final String label = getInfoPageLinkLabel((Persistable<?>) value);
                                 final Class<?> assocClass = getEntityAttribute(path).getJavaType();
 
                                 final SEntityInfoPage domain = pageMappings.getInfoPageForEntity(assocClass);
                                 if (domain != null) {
                                     value = new DelegatedLink(
                                             helper.getInfoPageLink(
                                                     domain.getPath(),
                                                     Long.parseLong(String.valueOf(((Persistable) value).getId()))
                                             ))
                                             .withLabel(label)
                                             .withRel(domain.getRel());
                                 } else {
                                     value = label;
                                 }
                             } else {
                                 throw new ComponentPathEvaluationException(String
                                         .format("%s does not match %s as value", path, ClassUtils.getShortName(Persistable.class)));
                             }
                         }
                         break;
                         case TABLE: {
                             if (value instanceof Iterable || value instanceof Map) {
                                 final PluralAttribute<?, ?, ?> entityAttribute = (PluralAttribute<?, ?, ?>) getEntityAttribute(path);
                                 final Class<?> javaType = entityAttribute.getElementType().getJavaType();
                                 if (componentBuilders.hasBuilder(javaType, ComponentBuilds.Produces.TABLE_COMPONENT)) {
                                     final String builderId = componentBuilders.getBuilderId(javaType, ComponentBuilds.Produces.TABLE_COMPONENT);
                                     final Link builderLink = helper.getBuilderLink();
 
                                     final Object contextValue = InvokeUtils.invokeGetter(object, path.split("\\.")[0]);
                                     if (!ClassUtils.isAssignable(Persistable.class, contextValue.getClass())) {
                                         value = new BuilderLink(
                                                 builderId,
                                                 object.getClass(),
                                                 object.getId(),
                                                 builderLink
                                         );
                                     } else {
                                         value = new BuilderLink(
                                                 builderId,
                                                 contextValue.getClass(),
                                                 ((Persistable<?>) contextValue).getId(),
                                                 builderLink
                                         );
                                     }
                                 }
                             }
                         }
                     }
                 }
 
                 if (value == null) {
                     logger.trace(String
                             .format("path %s does not correspond to any property of %s", path, ClassUtils.getShortName(object.getClass())));
                     value = "???";
                 }
 
                 logger.trace(String.format("processed path %s to %s", path, value));
                 return value;
             }
         }.setValue(object);
     }
 
     protected String getInfoPageLinkLabel(final Persistable<?> value) {
         if (value instanceof SCarMaster) {
             return InvokeUtils.invokeGetter(value, "manufacturingData.identity", String.class);
         } else if (value instanceof SUser) {
             return InvokeUtils.invokeGetter(value, "person.identity", String.class);
         } else if (value instanceof SCar) {
             return InvokeUtils.invokeGetter(value, "licencePlate", String.class);
         }
         return null;
     }
 
     protected EntityDescriptor<Y> getEntityDescriptor() {
        return this.descriptors.getEntityDescriptor(this.entity);
     }
 
     protected String getEntityName() {
         return this.getEntityDescriptor().getEntityType().getName();
     }
 
     protected Attribute<?, ?> getEntityAttribute(final String path) {
         EntityType<?> entityType = getEntityDescriptor().getEntityType();
         Attribute<?, ?> attribute = null;
         try {
             final List<String> paths = Lists.newArrayListWithExpectedSize(1);
             if (path.contains(".")) {
                 paths.addAll(Lists.newArrayList(StringUtils.split(path, ".")));
             } else {
                 paths.add(path);
             }
             if (paths.size() == 1) {
                 attribute = entityType.getAttribute(path);
             } else {
                 for (int i = 0 ; i < paths.size() ; i++) {
                     final String nestedPath = paths.get(i);
                     attribute = entityType.getAttribute(nestedPath);
                     if (i != paths.size() - 1) {
                        entityType = this.descriptors.getEntityDescriptor(attribute.getJavaType()).getEntityType();
                     }
                 }
             }
         } finally {
             if (attribute != null) {
                 this.logger.trace(String.format("%s has no attribute %s", entityType.getName(), path));
             }
         }
         return attribute;
     }
 
     private void populateSystemPanel(final InfoPagePanelComponent panel) {
         if (!panel.isSystemAttributesHolder()) {
             return;
         }
         this.helper.newAttribute(panel, "version", "persistentversionableobject.version", AttributeDisplayAs.VALUE);
         this.helper.newAttribute(panel, "createdBy", "persistentversionableobject.createdBy", AttributeDisplayAs.INFOPAGE);
         this.helper.newAttribute(panel, "createdDate", "persistentversionableobject.createdDate", AttributeDisplayAs.VALUE);
         this.helper.newAttribute(panel, "lastModifiedBy", "persistentversionableobject.lastModifiedBy", AttributeDisplayAs.INFOPAGE);
         this.helper.newAttribute(panel, "lastModifiedDate", "persistentversionableobject.lastModifiedDate", AttributeDisplayAs.VALUE);
     }
 }
