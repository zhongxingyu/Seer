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
 
 package org.jboss.pressgang.ccms.wrapper.base;
 
 import java.util.Collection;
 
 import org.jboss.pressgang.ccms.provider.RESTProviderFactory;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseAuditedEntityV1;
 import org.jboss.pressgang.ccms.wrapper.collection.CollectionWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.RESTCollectionWrapperBuilder;
 
 public abstract class RESTBaseAuditedEntityWrapper<T extends AuditedEntityWrapper<T>, U extends RESTBaseAuditedEntityV1<U, ?,
         ?>> extends RESTBaseEntityWrapper<T, U> implements AuditedEntityWrapper<T> {
 
     private final boolean isRevision;
 
     protected RESTBaseAuditedEntityWrapper(final RESTProviderFactory providerFactory, U entity, boolean isRevision, boolean isNewEntity) {
         this(providerFactory, entity, isRevision, null, isNewEntity);
     }
 
     protected RESTBaseAuditedEntityWrapper(final RESTProviderFactory providerFactory, U entity, boolean isRevision, boolean isNewEntity,
             final Collection<String> expandedMethods) {
         this(providerFactory, entity, isRevision, null, isNewEntity, expandedMethods);
     }
 
     protected RESTBaseAuditedEntityWrapper(final RESTProviderFactory providerFactory, U entity, boolean isRevision,
             final RESTBaseAuditedEntityV1<?, ?, ?> parent, boolean isNewEntity) {
         this(providerFactory, entity, isRevision, parent, isNewEntity, null);
     }
 
     protected RESTBaseAuditedEntityWrapper(final RESTProviderFactory providerFactory, U entity, boolean isRevision,
             final RESTBaseAuditedEntityV1<?, ?, ?> parent, boolean isNewEntity, final Collection<String> expandedMethods) {
        super(providerFactory, entity, parent, isNewEntity, expandedMethods);
         this.isRevision = isRevision;
     }
 
     @Override
     public Integer getRevision() {
         return getProxyEntity().getRevision();
     }
 
     @Override
     public boolean isRevisionEntity() {
         return isRevision;
     }
 
     @Override
     public boolean equals(final Object o) {
         if (o instanceof RESTBaseAuditedEntityWrapper && getProxyEntity() != null) {
             return o == null ? false : getEntity().equals(((RESTBaseAuditedEntityWrapper) o).getEntity());
         } else {
             return super.equals(o);
         }
     }
 
     @Override
     public CollectionWrapper<T> getRevisions() {
         return RESTCollectionWrapperBuilder.<T>newBuilder()
                 .providerFactory(getProviderFactory())
                 .collection(getProxyEntity().getRevisions())
                 .isRevisionCollection()
                 .parent(getParentEntity())
                 .build();
     }
 }
