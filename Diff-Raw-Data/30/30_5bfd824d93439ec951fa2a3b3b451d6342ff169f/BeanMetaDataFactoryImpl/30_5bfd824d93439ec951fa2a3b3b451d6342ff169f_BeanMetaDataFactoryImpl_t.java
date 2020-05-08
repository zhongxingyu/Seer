 /*
  * Copyright 2004-2007 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.dao.impl;
 
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
import java.util.Collections;
 import java.util.Set;
 
 import javax.sql.DataSource;
 
 import org.seasar.dao.AnnotationReaderFactory;
 import org.seasar.dao.BeanAnnotationReader;
 import org.seasar.dao.BeanEnhancer;
 import org.seasar.dao.BeanMetaData;
 import org.seasar.dao.BeanMetaDataFactory;
 import org.seasar.dao.DaoNamingConvention;
 import org.seasar.dao.Dbms;
 import org.seasar.dao.NotFoundModifiedPropertiesRuntimeException;
 import org.seasar.dao.ValueTypeFactory;
 import org.seasar.dao.dbms.DbmsManager;
 import org.seasar.dao.impl.BeanMetaDataImpl.ModifiedPropertySupport;
 import org.seasar.extension.jdbc.util.ConnectionUtil;
 import org.seasar.extension.jdbc.util.DataSourceUtil;
 import org.seasar.framework.beans.BeanDesc;
 import org.seasar.framework.beans.PropertyDesc;
 import org.seasar.framework.beans.factory.BeanDescFactory;
 
 /**
  * @author jflute
  * @author manhole
  */
 public class BeanMetaDataFactoryImpl implements BeanMetaDataFactory {
 
     public static final String annotationReaderFactory_BINDING = "bindingType=must";
 
     public static final String valueTypeFactory_BINDING = "bindingType=must";
 
     public static final String dataSource_BINDING = "bindingType=must";
 
     public static final String daoNamingConvention_BINDING = "bindingType=must";
 
     public static final String beanEnhancer_BINDING = "bindingType=must";
 
     protected AnnotationReaderFactory annotationReaderFactory;
 
     protected ValueTypeFactory valueTypeFactory;
 
     protected DataSource dataSource;
 
     protected DaoNamingConvention daoNamingConvention;
 
     protected BeanEnhancer beanEnhancer;
 
     public BeanMetaData createBeanMetaData(final Class beanClass) {
         return createBeanMetaData(beanClass, 0);
     }
 
     public BeanMetaData createBeanMetaData(final Class beanClass,
             final int relationNestLevel) {
         if (beanClass == null) {
             throw new NullPointerException("beanClass");
         }
         final Connection con = DataSourceUtil.getConnection(dataSource);
         try {
             final DatabaseMetaData metaData = ConnectionUtil.getMetaData(con);
             return createBeanMetaData(metaData, beanClass, relationNestLevel);
         } finally {
             ConnectionUtil.close(con);
         }
     }
 
     protected BeanMetaData createBeanMetaData(
             final DatabaseMetaData dbMetaData, final Class beanClass,
             final int relationNestLevel) {
 
         final BeanMetaDataImpl bmd = createBeanMetaDataImpl();
         final BeanEnhancer enhancer = getBeanEnhancer();
         final Class originalBeanClass = enhancer.getOriginalClass(beanClass);
         bmd.setDatabaseMetaData(dbMetaData);
         final Dbms dbms = getDbms();
         bmd.setDbms(dbms);
         final BeanAnnotationReader bar = annotationReaderFactory
                 .createBeanAnnotationReader(originalBeanClass);
         bmd.setBeanAnnotationReader(bar);
         bmd.setValueTypeFactory(valueTypeFactory);
         bmd
                 .setStopRelationCreation(isLimitRelationNestLevel(relationNestLevel));
         bmd.setBeanMetaDataFactory(this);
         bmd.setRelationNestLevel(relationNestLevel);
         final DaoNamingConvention namingConvention = getDaoNamingConvention();
 
         final String versionNoPropertyName = bar.getVersionNoPropertyName();
         if (versionNoPropertyName != null) {
             bmd.setVersionNoPropertyName(versionNoPropertyName);
         } else {
             bmd.setVersionNoPropertyName(namingConvention
                     .getVersionNoPropertyName());
         }
         final String timestampPropertyName = bar.getTimestampPropertyName();
         if (timestampPropertyName != null) {
             bmd.setTimestampPropertyName(timestampPropertyName);
         } else {
             bmd.setTimestampPropertyName(namingConvention
                     .getTimestampPropertyName());
         }
 
         bmd.setBeanClass(originalBeanClass);
         bmd.initialize();
         final Class enhancedBeanClass = enhancer.enhanceBeanClass(beanClass,
                 versionNoPropertyName, timestampPropertyName);
        if (enhancer instanceof NullBeanEnhancer) {
            bmd.setModifiedPropertySupport(new NullModifiedPropertySupport());
        } else {
            final ModifiedPropertySupportImpl modifiedPropertySupportImpl = new ModifiedPropertySupportImpl();
            modifiedPropertySupportImpl
                    .setDaoNamingConvention(namingConvention);
            bmd.setModifiedPropertySupport(modifiedPropertySupportImpl);
        }
         bmd.setBeanClass(enhancedBeanClass);
         return bmd;
     }
 
     protected Dbms getDbms() {
         return DbmsManager.getDbms(dataSource);
     }
 
     protected BeanMetaDataImpl createBeanMetaDataImpl() {
         return new BeanMetaDataImpl();
     }
 
     protected boolean isLimitRelationNestLevel(final int relationNestLevel) {
         return relationNestLevel == getLimitRelationNestLevel();
     }
 
     protected int getLimitRelationNestLevel() {
         // You can change relation creation range by changing this.
         return 1;
     }
 
     public void setDataSource(final DataSource dataSource) {
         this.dataSource = dataSource;
     }
 
     public void setAnnotationReaderFactory(
             final AnnotationReaderFactory annotationReaderFactory) {
         this.annotationReaderFactory = annotationReaderFactory;
     }
 
     public void setValueTypeFactory(final ValueTypeFactory valueTypeFactory) {
         this.valueTypeFactory = valueTypeFactory;
     }
 
     public DaoNamingConvention getDaoNamingConvention() {
         return daoNamingConvention;
     }
 
     public void setDaoNamingConvention(
             final DaoNamingConvention daoNamingConvention) {
         this.daoNamingConvention = daoNamingConvention;
     }
 
     public BeanEnhancer getBeanEnhancer() {
         return beanEnhancer;
     }
 
     public void setBeanEnhancer(final BeanEnhancer beanEnhancer) {
         this.beanEnhancer = beanEnhancer;
     }
 
    public static class ModifiedPropertySupportImpl implements
             ModifiedPropertySupport {
 
         private DaoNamingConvention daoNamingConvention;
 
         public Set getModifiedPropertyNames(final Object bean) {
             final BeanDesc beanDesc = BeanDescFactory.getBeanDesc(bean
                     .getClass());
             final String propertyName = getDaoNamingConvention()
                     .getModifiedPropertyNamesPropertyName();
             if (!beanDesc.hasPropertyDesc(propertyName)) {
                 throw new NotFoundModifiedPropertiesRuntimeException(bean
                         .getClass().getName(), propertyName);
             }
             final PropertyDesc propertyDesc = beanDesc
                     .getPropertyDesc(propertyName);
             final Object value = propertyDesc.getValue(bean);
             final Set names = (Set) value;
             return names;
         }
 
         public DaoNamingConvention getDaoNamingConvention() {
             return daoNamingConvention;
         }
 
         public void setDaoNamingConvention(
                 final DaoNamingConvention daoNamingConvention) {
             this.daoNamingConvention = daoNamingConvention;
         }
 
     }
 
    public static class NullModifiedPropertySupport implements
            ModifiedPropertySupport {

        private static final Set EMPTY_SET = Collections.EMPTY_SET;

        public Set getModifiedPropertyNames(final Object bean) {
            return EMPTY_SET;
        }

    }

 }
