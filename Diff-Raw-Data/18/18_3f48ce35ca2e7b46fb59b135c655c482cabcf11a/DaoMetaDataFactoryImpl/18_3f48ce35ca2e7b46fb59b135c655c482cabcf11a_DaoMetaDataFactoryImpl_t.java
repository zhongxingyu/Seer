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
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.sql.DataSource;
 
 import org.seasar.dao.AnnotationReaderFactory;
 import org.seasar.dao.BeanMetaDataFactory;
 import org.seasar.dao.DaoMetaData;
 import org.seasar.dao.DaoMetaDataFactory;
 import org.seasar.dao.DaoNamingConvention;
 import org.seasar.dao.ValueTypeFactory;
 import org.seasar.extension.jdbc.ResultSetFactory;
 import org.seasar.extension.jdbc.StatementFactory;
 import org.seasar.framework.util.Disposable;
 import org.seasar.framework.util.DisposableUtil;
 
 /**
  * @author higa
  * @author manhole
  * @author jflute
  */
 public class DaoMetaDataFactoryImpl implements DaoMetaDataFactory, Disposable {
 
     public static final String daoMetaDataCache_BINDING = "bindingType=must";
 
     public static final String dataSource_BINDING = "bindingType=must";
 
     public static final String statementFactory_BINDING = "bindingType=must";
 
     public static final String resultSetFactory_BINDING = "bindingType=must";
 
     public static final String annotationReaderFactory_BINDING = "bindingType=must";
 
     public static final String valueTypeFactory_BINDING = "bindingType=must";
 
     public static final String beanMetaDataFactory_BINDING = "bindingType=must";
 
     public static final String daoNamingConvention_BINDING = "bindingType=must";
 
     protected Map daoMetaDataCache = new HashMap();
 
     protected DataSource dataSource;
 
     protected StatementFactory statementFactory;
 
     protected ResultSetFactory resultSetFactory;
 
     protected AnnotationReaderFactory annotationReaderFactory;
 
     protected ValueTypeFactory valueTypeFactory;
 
     protected String sqlFileEncoding;
 
     protected BeanMetaDataFactory beanMetaDataFactory;
 
     protected DaoNamingConvention daoNamingConvention;
 
     protected boolean initialized;
 
     protected boolean userDaoClassForLog = false;
 
     public DaoMetaDataFactoryImpl() {
     }
 
     public DaoMetaDataFactoryImpl(DataSource dataSource,
             StatementFactory statementFactory,
             ResultSetFactory resultSetFactory,
             AnnotationReaderFactory readerFactory) {
 
         this.dataSource = dataSource;
         this.statementFactory = statementFactory;
         this.resultSetFactory = resultSetFactory;
         this.annotationReaderFactory = readerFactory;
     }
 
     public void setSqlFileEncoding(String encoding) {
         this.sqlFileEncoding = encoding;
     }
 
    public DaoMetaData getDaoMetaData(Class daoClass) {
         if (!initialized) {
             DisposableUtil.add(this);
             initialized = true;
         }
         String key = daoClass.getName();
        DaoMetaData dmd;
        synchronized (daoMetaDataCache) {
            dmd = (DaoMetaData) daoMetaDataCache.get(key);
        }
         if (dmd != null) {
             return dmd;
         }
         DaoMetaData dmdi = createDaoMetaData(daoClass);
        synchronized (daoMetaDataCache) {
            dmd = (DaoMetaData) daoMetaDataCache.get(daoClass);
            if (dmd != null) {
                return dmd;
            } else {
                daoMetaDataCache.put(key, dmdi);
            }
        }
         return dmdi;
     }
 
     protected DaoMetaData createDaoMetaData(Class daoClass) {
         DaoMetaDataImpl daoMetaData = createDaoMetaDataImpl();
         daoMetaData.setDaoClass(daoClass);
         daoMetaData.setDataSource(dataSource);
         daoMetaData.setStatementFactory(statementFactory);
         daoMetaData.setResultSetFactory(resultSetFactory);
         daoMetaData.setAnnotationReaderFactory(annotationReaderFactory);
         daoMetaData.setValueTypeFactory(valueTypeFactory);
         daoMetaData.setBeanMetaDataFactory(getBeanMetaDataFactory());
         daoMetaData.setDaoNamingConvention(getDaoNamingConvention());
         daoMetaData.setUseDaoClassForLog(userDaoClassForLog);
         if (sqlFileEncoding != null) {
             daoMetaData.setSqlFileEncoding(sqlFileEncoding);
         }
         daoMetaData.initialize();
         return daoMetaData;
     }
 
     protected DaoMetaDataImpl createDaoMetaDataImpl() {
         return new DaoMetaDataImpl();
     }
 
     public void setValueTypeFactory(ValueTypeFactory valueTypeFactory) {
         this.valueTypeFactory = valueTypeFactory;
     }
 
     protected BeanMetaDataFactory getBeanMetaDataFactory() {
         return beanMetaDataFactory;
     }
 
     public void setBeanMetaDataFactory(BeanMetaDataFactory beanMetaDataFactory) {
         this.beanMetaDataFactory = beanMetaDataFactory;
     }
 
     public synchronized void dispose() {
         daoMetaDataCache.clear();
         initialized = false;
     }
 
     public DaoNamingConvention getDaoNamingConvention() {
         return daoNamingConvention;
     }
 
     public void setDaoNamingConvention(DaoNamingConvention daoNamingConvention) {
         this.daoNamingConvention = daoNamingConvention;
     }
 
     public void setAnnotationReaderFactory(
             AnnotationReaderFactory annotationReaderFactory) {
         this.annotationReaderFactory = annotationReaderFactory;
     }
 
     public void setDataSource(DataSource dataSource) {
         this.dataSource = dataSource;
     }
 
     public void setResultSetFactory(ResultSetFactory resultSetFactory) {
         this.resultSetFactory = resultSetFactory;
     }
 
     public void setStatementFactory(StatementFactory statementFactory) {
         this.statementFactory = statementFactory;
     }
 
     public void setUserDaoClassForLog(boolean userDaoClassForLog) {
         this.userDaoClassForLog = userDaoClassForLog;
     }
 
 }
