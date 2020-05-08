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
 import org.jboss.pressgang.ccms.proxy.RESTEntityProxyFactory;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseEntityV1;
 
 public abstract class RESTBaseEntityWrapper<T extends EntityWrapper<T>, U extends RESTBaseEntityV1<U>> extends RESTBaseWrapper<T,
         U> implements EntityWrapper<T> {
 
     private final RESTBaseEntityV1<?> parent;
     private final U proxyEntity;
     private final boolean isNewEntity;
 
     protected RESTBaseEntityWrapper(final RESTProviderFactory providerFactory, U entity, boolean isNewEntity) {
         this(providerFactory, entity, null, isNewEntity);
     }
 
     protected RESTBaseEntityWrapper(final RESTProviderFactory providerFactory, U entity, boolean isNewEntity,
             final Collection<String> expandedMethods) {
         this(providerFactory, entity, null, isNewEntity, expandedMethods);
     }
 
     protected RESTBaseEntityWrapper(final RESTProviderFactory providerFactory, U entity,
             final RESTBaseEntityV1<?> parent, boolean isNewEntity) {
         this(providerFactory, entity, parent, isNewEntity, null);
     }
 
     protected RESTBaseEntityWrapper(final RESTProviderFactory providerFactory, U entity,
             final RESTBaseEntityV1<?> parent, boolean isNewEntity, final Collection<String> expandedMethods) {
        this(providerFactory, entity, false, parent, isNewEntity, expandedMethods);
    }

    protected RESTBaseEntityWrapper(final RESTProviderFactory providerFactory, U entity, boolean isRevision, final RESTBaseEntityV1<?> parent, boolean isNewEntity,
            final Collection<String> expandedMethods) {
         super(providerFactory, entity);
         this.isNewEntity = isNewEntity;
         this.parent = parent;
         if (isNewEntity) {
             proxyEntity = entity;
         } else {
            proxyEntity = RESTEntityProxyFactory.createProxy(providerFactory, entity, isRevision, parent, expandedMethods);
         }
     }
 
     protected U getProxyEntity() {
         return proxyEntity;
     }
 
     @Override
     public Integer getId() {
         return getProxyEntity().getId();
     }
 
     @Override
     public void setId(Integer id) {
         getEntity().setId(id);
     }
 
     @Override
     public boolean equals(final Object o) {
         if (o instanceof RESTBaseEntityWrapper && getProxyEntity() != null) {
             return o == null ? false : getEntity().equals(((RESTBaseEntityWrapper) o).getEntity());
         } else {
             return super.equals(o);
         }
     }
 
     @Override
     public int hashCode() {
         if (getProxyEntity() != null) {
             return getEntity().hashCode();
         } else {
             return super.hashCode();
         }
     }
 
     protected boolean isNewEntity() {
         return isNewEntity;
     }
 
     protected RESTBaseEntityV1<?> getParentEntity() {
         return parent;
     }
 }
