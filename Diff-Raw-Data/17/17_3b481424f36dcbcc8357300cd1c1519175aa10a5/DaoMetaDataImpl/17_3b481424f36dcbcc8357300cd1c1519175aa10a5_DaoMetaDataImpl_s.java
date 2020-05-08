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
 
 import java.io.InputStream;
 import java.io.Reader;
 import java.lang.reflect.Array;
 import java.lang.reflect.Method;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.sql.DataSource;
 
 import org.seasar.dao.AnnotationReaderFactory;
 import org.seasar.dao.BeanMetaData;
 import org.seasar.dao.BeanMetaDataFactory;
 import org.seasar.dao.DaoAnnotationReader;
 import org.seasar.dao.DaoMetaData;
 import org.seasar.dao.DaoNamingConvention;
 import org.seasar.dao.DaoNotFoundRuntimeException;
 import org.seasar.dao.Dbms;
 import org.seasar.dao.DtoMetaData;
 import org.seasar.dao.DtoMetaDataFactory;
 import org.seasar.dao.IllegalAnnotationRuntimeException;
 import org.seasar.dao.IllegalSignatureRuntimeException;
 import org.seasar.dao.InjectDaoClassSupport;
 import org.seasar.dao.MethodSetupFailureRuntimeException;
 import org.seasar.dao.RelationRowCreator;
 import org.seasar.dao.ResultSetHandlerFactory;
 import org.seasar.dao.SqlCommand;
 import org.seasar.dao.SqlFileNotFoundRuntimeException;
 import org.seasar.dao.ValueTypeFactory;
 import org.seasar.dao.dbms.DbmsManager;
 import org.seasar.dao.handler.ProcedureHandlerImpl;
 import org.seasar.extension.jdbc.PropertyType;
 import org.seasar.extension.jdbc.ResultSetFactory;
 import org.seasar.extension.jdbc.ResultSetHandler;
 import org.seasar.extension.jdbc.StatementFactory;
 import org.seasar.extension.jdbc.impl.MapListResultSetHandler;
 import org.seasar.extension.jdbc.impl.MapResultSetHandler;
 import org.seasar.extension.jdbc.impl.ObjectResultSetHandler;
 import org.seasar.extension.jdbc.util.ConnectionUtil;
 import org.seasar.extension.jdbc.util.DataSourceUtil;
 import org.seasar.framework.beans.BeanDesc;
 import org.seasar.framework.beans.MethodNotFoundRuntimeException;
 import org.seasar.framework.beans.factory.BeanDescFactory;
 import org.seasar.framework.exception.NoSuchMethodRuntimeException;
 import org.seasar.framework.exception.SRuntimeException;
 import org.seasar.framework.util.ClassUtil;
 import org.seasar.framework.util.InputStreamReaderUtil;
 import org.seasar.framework.util.MethodUtil;
 import org.seasar.framework.util.ReaderUtil;
 import org.seasar.framework.util.ResourceUtil;
 import org.seasar.framework.util.StringUtil;
 
 /**
  * @author higa
  * @author azusa
  * 
  */
 public class DaoMetaDataImpl implements DaoMetaData {
 
     private static final Pattern startWithOrderByPattern = Pattern.compile(
             "(/\\*[^*]+\\*/)*order by", Pattern.CASE_INSENSITIVE);
 
     private static final Pattern startWithSelectPattern = Pattern.compile(
             "^\\s*select\\s", Pattern.CASE_INSENSITIVE);
 
     private static final Pattern startWithBeginCommentPattern = Pattern
             .compile("/\\*BEGIN\\*/\\s*WHERE .+", Pattern.CASE_INSENSITIVE);
 
     private static final Pattern startWithIfCommentPattern = Pattern.compile(
             "/\\*IF .+", Pattern.CASE_INSENSITIVE);
 
     private static final String NOT_SINGLE_ROW_UPDATED = "NotSingleRowUpdated";
 
     protected Class daoClass;
 
     protected Class daoInterface;
 
     protected BeanDesc daoBeanDesc;
 
     protected DataSource dataSource;
 
     protected DaoAnnotationReader annotationReader;
 
     protected StatementFactory statementFactory;
 
     protected ResultSetFactory resultSetFactory;
 
     protected AnnotationReaderFactory annotationReaderFactory;
 
     protected String sqlFileEncoding = "JISAutoDetect";
 
     protected Dbms dbms;
 
     protected Class beanClass;
 
     protected BeanMetaData beanMetaData;
 
     protected Map sqlCommands = new HashMap();
 
     protected ValueTypeFactory valueTypeFactory;
 
     protected BeanMetaDataFactory beanMetaDataFactory;
 
     public static final String dtoMetaDataFactory_BINDING = "bindingType=may";
 
     protected DtoMetaDataFactory dtoMetaDataFactory;
 
     protected ResultSetHandlerFactory resultSetHandlerFactory;
 
     protected DaoNamingConvention daoNamingConvention;
 
     protected boolean useDaoClassForLog = false;
 
     public DaoMetaDataImpl() {
     }
 
     public void initialize() {
         Class daoClass = getDaoClass();
         daoInterface = getDaoInterface(daoClass);
         daoBeanDesc = BeanDescFactory.getBeanDesc(daoClass);
         annotationReader = getAnnotationReaderFactory()
                 .createDaoAnnotationReader(daoBeanDesc);
         setBeanClass(annotationReader.getBeanClass());
         final Connection con = DataSourceUtil.getConnection(dataSource);
         try {
             final DatabaseMetaData dbMetaData = ConnectionUtil.getMetaData(con);
             dbms = DbmsManager.getDbms(dbMetaData);
         } finally {
             ConnectionUtil.close(con);
         }
         this.beanMetaData = beanMetaDataFactory.createBeanMetaData(beanClass);
         dtoMetaDataFactory = createDtoMetaDataFactory();
         resultSetHandlerFactory = createResultSetHandlerFactory();
         setupSqlCommand();
     }
 
     protected ResultSetHandlerFactory createResultSetHandlerFactory() {
         return new ResultSetHandlerFactoryImpl(beanMetaData, annotationReader,
                 dtoMetaDataFactory);
     }
 
     protected DtoMetaDataFactory createDtoMetaDataFactory() {
         DtoMetaDataFactoryImpl factory = new DtoMetaDataFactoryImpl();
         factory.setAnnotationReaderFactory(getAnnotationReaderFactory());
         factory.setValueTypeFactory(valueTypeFactory);
         return factory;
     }
 
     protected void setupSqlCommand() {
         BeanDesc idbd = BeanDescFactory.getBeanDesc(daoInterface);
         String[] names = idbd.getMethodNames();
         for (int i = 0; i < names.length; ++i) {
             Method[] methods = daoBeanDesc.getMethods(names[i]);
             if (methods.length == 1 && MethodUtil.isAbstract(methods[0])) {
                 setupMethod(methods[0]);
             }
         }
     }
 
     protected void setupMethod(Method method) {
         setupMethod(daoInterface, method);
     }
 
     protected void setupMethod(Class daoInterface, Method method) {
         try {
             assertAnnotation(method);
 
             setupMethodByAnnotation(daoInterface, method);
 
             if (!completedSetupMethod(method)) {
                 setupMethodBySqlFile(daoInterface, method);
             }
 
             if (!completedSetupMethod(method)) {
                 setupMethodByInterfaces(daoInterface, method);
             }
 
             if (!completedSetupMethod(method)) {
                 setupMethodBySuperClass(daoInterface, method);
             }
 
             if (!completedSetupMethod(method)
                     && annotationReader.isSqlFile(method)) {
                 throw new SqlFileNotFoundRuntimeException(daoInterface, method);
             }
 
             if (!completedSetupMethod(method)) {
                 setupMethodByAuto(method);
             }
         } catch (SRuntimeException e) {
             throw new MethodSetupFailureRuntimeException(
                     daoInterface.getName(), method.getName(), e);
         }
     }
 
     protected void setupMethodByAnnotation(Class daoInterface, Method method) {
         String sql = annotationReader.getSQL(method, dbms.getSuffix());
         if (sql != null) {
             setupMethodByManual(method, sql);
         }
         String procedureName = annotationReader.getStoredProcedureName(method);
         if (procedureName != null) {
             setupProcedureMethod(method, procedureName);
         }
     }
 
     protected void assertAnnotation(Method method) {
         if (isInsert(method.getName()) || isUpdate(method.getName())) {
             if (annotationReader.getQuery(method) != null) {
                 throw new IllegalAnnotationRuntimeException("Query");
             }
         }
     }
 
     protected void setupProcedureMethod(final Method method,
             final String procedureName) {
 
         final ProcedureHandlerImpl handler = new ProcedureHandlerImpl();
         handler.setDataSource(dataSource);
         handler.setDbms(dbms);
         handler.setDaoMethod(method);
         handler.setProcedureName(procedureName);
         handler.setResultSetHandlerFactory(resultSetHandlerFactory);
         handler.setStatementFactory(statementFactory);
         handler.initialize();
 
         final StaticStoredProcedureCommand command = new StaticStoredProcedureCommand(
                 handler);
         sqlCommands.put(method.getName(), command);
     }
 
     protected String readText(String path) {
         InputStream is = ResourceUtil.getResourceAsStream(path);
         Reader reader = InputStreamReaderUtil.create(is, getSqlFileEncoding());
         return ReaderUtil.readText(reader);
     }
 
     protected void setupMethodBySqlFile(Class daoInterface, Method method) {
         String base = daoInterface.getName().replace('.', '/') + "_"
                 + method.getName();
         String dbmsPath = base + dbms.getSuffix() + ".sql";
         String standardPath = base + ".sql";
         if (ResourceUtil.isExist(dbmsPath)) {
             String sql = readText(dbmsPath);
             setupMethodByManual(method, sql);
         } else if (ResourceUtil.isExist(standardPath)) {
             String sql = readText(standardPath);
             setupMethodByManual(method, sql);
         } else if (isDelete(method.getName())) {
             String query = annotationReader.getQuery(method);
             if (StringUtil.isNotBlank(query)) {
                 if (query.trim().toUpperCase().startsWith("WHERE")) {
                     setupMethodByManual(method, "DELETE FROM "
                             + beanMetaData.getTableName() + " " + query);
                 } else {
                     setupMethodByManual(method, "DELETE FROM "
                             + beanMetaData.getTableName() + " WHERE " + query);
                 }
             }
         }
     }
 
     protected void setupMethodByInterfaces(Class daoInterface, Method method) {
         Class[] interfaces = daoInterface.getInterfaces();
         if (interfaces == null) {
             return;
         }
         for (int i = 0; i < interfaces.length; i++) {
             Method interfaceMethod = getSameSignatureMethod(interfaces[i],
                     method);
             if (interfaceMethod != null) {
                 setupMethod(interfaces[i], interfaceMethod);
             }
         }
     }
 
     protected void setupMethodBySuperClass(Class daoInterface, Method method) {
         Class superDaoClass = daoInterface.getSuperclass();
         if (superDaoClass != null && !Object.class.equals(superDaoClass)) {
             Method superClassMethod = getSameSignatureMethod(superDaoClass,
                     method);
             if (superClassMethod != null) {
                 setupMethod(superDaoClass, method);
             }
         }
     }
 
     protected boolean completedSetupMethod(Method method) {
         return sqlCommands.get(method.getName()) != null;
     }
 
     private Method getSameSignatureMethod(Class clazz, Method method) {
         try {
             String methodName = method.getName();
             Class[] parameterTypes = method.getParameterTypes();
             return ClassUtil.getMethod(clazz, methodName, parameterTypes);
         } catch (NoSuchMethodRuntimeException e) {
             return null;
         }
     }
 
     protected void setupMethodByManual(Method method, String sql) {
         if (isSelect(method)) {
             setupSelectMethodByManual(method, sql);
         } else {
             setupUpdateMethodByManual(method, sql);
         }
     }
 
     protected void setupMethodByAuto(Method method) {
         if (isInsert(method.getName())) {
             setupInsertMethodByAuto(method);
         } else if (isUpdate(method.getName())) {
             setupUpdateMethodByAuto(method);
         } else if (isDelete(method.getName())) {
             setupDeleteMethodByAuto(method);
         } else {
             setupSelectMethodByAuto(method);
         }
     }
 
     protected void setupSelectMethodByManual(Method method, String sql) {
         SelectDynamicCommand cmd = createSelectDynamicCommand(createResultSetHandler(method));
         cmd.setSql(sql);
         cmd.setArgNames(annotationReader.getArgNames(method));
         cmd.setArgTypes(method.getParameterTypes());
         sqlCommands.put(method.getName(), cmd);
     }
 
     protected SelectDynamicCommand createSelectDynamicCommand(
             ResultSetHandler rsh) {
         return new SelectDynamicCommand(dataSource, statementFactory, rsh,
                 resultSetFactory);
     }
 
     protected SelectDynamicCommand createSelectDynamicCommand(
             ResultSetHandler resultSetHandler, String query) {
 
         SelectDynamicCommand cmd = createSelectDynamicCommand(resultSetHandler);
         StringBuffer buf = new StringBuffer(255);
         if (startsWithSelect(query)) {
             buf.append(query);
         } else {
             String sql = dbms.getAutoSelectSql(getBeanMetaData());
             buf.append(sql);
             if (query != null) {
                 boolean began = false;
                 if (startsWithOrderBy(query)) {
                     buf.append(" ");
                 } else if (startsWithBeginComment(query)) {
                     buf.append(" ");
                } else if (sql.lastIndexOf("WHERE") < 0) {
                     if (startsWithIfComment(query)) {
                         buf.append("/*BEGIN*/");
                         began = true;
                     }
                     buf.append(" WHERE ");
                 } else {
                     if (startsWithIfComment(query)) {
                         buf.append("/*BEGIN*/");
                         began = true;
                     }
                     buf.append(" AND ");
                 }
                buf.append(query);
                 if (began) {
                     buf.append("/*END*/");
                 }
             }
         }
         cmd.setSql(buf.toString());
         return cmd;
     }
 
     protected boolean startsWithIfComment(String query) {
         Matcher m = startWithIfCommentPattern.matcher(query);
         if (m.lookingAt()) {
             return true;
         }
         return false;
     }
 
     protected boolean startsWithBeginComment(String query) {
         Matcher m = startWithBeginCommentPattern.matcher(query);
         if (m.lookingAt()) {
             return true;
         }
         return false;
     }
 
     protected static boolean startsWithSelect(String query) {
         if (query != null) {
             Matcher m = startWithSelectPattern.matcher(query);
             if (m.lookingAt()) {
                 return true;
             }
         }
         return false;
     }
 
     protected static boolean startsWithOrderBy(String query) {
         if (query != null) {
             Matcher m = startWithOrderByPattern.matcher(query);
             if (m.lookingAt()) {
                 return true;
             }
         }
         return false;
     }
 
     protected ResultSetHandler createResultSetHandler(Method method) {
         return resultSetHandlerFactory.createResultSetHandler(method);
     }
 
     protected boolean isBeanClassAssignable(Class clazz) {
         return beanClass.isAssignableFrom(clazz)
                 || clazz.isAssignableFrom(beanClass);
     }
 
     // update & insert & delete
     protected void setupUpdateMethodByManual(Method method, String sql) {
         UpdateDynamicCommand cmd = new UpdateDynamicCommand(dataSource,
                 statementFactory);
         cmd.setSql(sql);
         String[] argNames = annotationReader.getArgNames(method);
         if (argNames.length == 0 && isUpdateSignatureForBean(method)) {
             argNames = new String[] { StringUtil.decapitalize(ClassUtil
                     .getShortClassName(beanClass)) };
         }
         cmd.setArgNames(argNames);
         cmd.setArgTypes(method.getParameterTypes());
         cmd
                 .setNotSingleRowUpdatedExceptionClass(getNotSingleRowUpdatedExceptionClass(method));
         sqlCommands.put(method.getName(), cmd);
     }
 
     protected boolean isUpdateSignatureForBean(Method method) {
         return method.getParameterTypes().length == 1
                 && isBeanClassAssignable(method.getParameterTypes()[0]);
     }
 
     protected Class getNotSingleRowUpdatedExceptionClass(Method method) {
         Class[] exceptionTypes = method.getExceptionTypes();
         if (exceptionTypes != null) {
             for (int i = 0; i < exceptionTypes.length; ++i) {
                 Class exceptionType = exceptionTypes[i];
                 if (exceptionType.getName().indexOf(NOT_SINGLE_ROW_UPDATED) >= 0) {
                     return exceptionType;
                 }
             }
         }
         return null;
     }
 
     protected void setupInsertMethodByAuto(Method method) {
         checkAutoUpdateMethod(method);
         String[] propertyNames = getPersistentPropertyNames(method);
         final SqlCommand command;
         if (isUpdateSignatureForBean(method)) {
             InsertAutoDynamicCommand cmd = new InsertAutoDynamicCommand();
             cmd.setBeanMetaData(getBeanMetaData());
             cmd.setDataSource(dataSource);
             cmd
                     .setNotSingleRowUpdatedExceptionClass(getNotSingleRowUpdatedExceptionClass(method));
             cmd.setPropertyNames(propertyNames);
             cmd.setStatementFactory(statementFactory);
             command = cmd;
         } else {
             InsertBatchAutoStaticCommand cmd = new InsertBatchAutoStaticCommand(
                     dataSource, statementFactory, getBeanMetaData(),
                     propertyNames);
             command = cmd;
         }
         sqlCommands.put(method.getName(), command);
     }
 
     // update
     protected void setupUpdateMethodByAuto(Method method) {
         checkAutoUpdateMethod(method);
         String[] propertyNames = getPersistentPropertyNames(method);
         AbstractSqlCommand cmd;
         if (isUpdateSignatureForBean(method)) {
             if (isUnlessNull(method.getName())) {
                 cmd = createUpdateAutoDynamicCommand(method, propertyNames);
             } else if (isModifiedOnly(method.getName())) {
                 cmd = createUpdateModifiedOnlyCommand(method, propertyNames);
             } else {
                 cmd = new UpdateAutoStaticCommand(dataSource, statementFactory,
                         beanMetaData, propertyNames);
             }
         } else {
             cmd = new UpdateBatchAutoStaticCommand(dataSource,
                     statementFactory, beanMetaData, propertyNames);
         }
         sqlCommands.put(method.getName(), cmd);
     }
 
     /**
      * @param method
      * @param propertyNames
      * @return
      */
     private AbstractSqlCommand createUpdateAutoDynamicCommand(Method method,
             String[] propertyNames) {
         AbstractSqlCommand cmd;
         UpdateAutoDynamicCommand uac = new UpdateAutoDynamicCommand(dataSource,
                 statementFactory);
         uac.setBeanMetaData(beanMetaData);
         uac.setPropertyNames(propertyNames);
         uac
                 .setNotSingleRowUpdatedExceptionClass(getNotSingleRowUpdatedExceptionClass(method));
         cmd = uac;
         return cmd;
     }
 
     private AbstractSqlCommand createUpdateModifiedOnlyCommand(
             final Method method, final String[] propertyNames) {
         UpdateModifiedOnlyCommand uac = new UpdateModifiedOnlyCommand(
                 dataSource, statementFactory);
         uac.setBeanMetaData(beanMetaData);
         uac.setPropertyNames(propertyNames);
         uac
                 .setNotSingleRowUpdatedExceptionClass(getNotSingleRowUpdatedExceptionClass(method));
         return uac;
     }
 
     protected void setupDeleteMethodByAuto(Method method) {
         checkAutoUpdateMethod(method);
         String[] propertyNames = getPersistentPropertyNames(method);
         SqlCommand cmd = null;
         if (isUpdateSignatureForBean(method)) {
             cmd = new DeleteAutoStaticCommand(dataSource, statementFactory,
                     beanMetaData, propertyNames);
         } else {
             cmd = new DeleteBatchAutoStaticCommand(dataSource,
                     statementFactory, beanMetaData, propertyNames);
         }
         sqlCommands.put(method.getName(), cmd);
     }
 
     protected String[] getPersistentPropertyNames(Method method) {
         List names = new ArrayList();
         String[] props = annotationReader.getNoPersistentProps(method);
         if (props != null) {
             for (int i = 0; i < beanMetaData.getPropertyTypeSize(); ++i) {
                 PropertyType pt = beanMetaData.getPropertyType(i);
                 if (pt.isPersistent()
                         && !isPropertyExist(props, pt.getPropertyName())) {
                     names.add(pt.getPropertyName());
                 }
             }
         } else {
             props = annotationReader.getPersistentProps(method);
             if (props != null) {
                 names.addAll(Arrays.asList(props));
                 for (int i = 0; i < beanMetaData.getPrimaryKeySize(); ++i) {
                     String pk = beanMetaData.getPrimaryKey(i);
                     PropertyType pt = beanMetaData
                             .getPropertyTypeByColumnName(pk);
                     names.add(pt.getPropertyName());
                 }
                 if (beanMetaData.hasVersionNoPropertyType()) {
                     names.add(beanMetaData.getVersionNoPropertyName());
                 }
                 if (beanMetaData.hasTimestampPropertyType()) {
                     names.add(beanMetaData.getTimestampPropertyName());
                 }
             }
         }
         if (names.size() == 0) {
             for (int i = 0; i < beanMetaData.getPropertyTypeSize(); ++i) {
                 PropertyType pt = beanMetaData.getPropertyType(i);
                 if (pt.isPersistent()) {
                     names.add(pt.getPropertyName());
                 }
             }
         }
         return (String[]) names.toArray(new String[names.size()]);
     }
 
     protected boolean isPropertyExist(String[] props, String propertyName) {
         for (int i = 0; i < props.length; ++i) {
             if (props[i].equalsIgnoreCase(propertyName)) {
                 return true;
             }
         }
         return false;
     }
 
     protected void setupSelectMethodByAuto(Method method) {
         final ResultSetHandler handler = createResultSetHandler(method);
         final String[] argNames = annotationReader.getArgNames(method);
         final String query = annotationReader.getQuery(method);
         SelectDynamicCommand cmd = null;
         if (query != null && !startsWithOrderBy(query)) {
             cmd = setupQuerySelectMethodByAuto(method, handler, argNames, query);
         } else {
             cmd = setupNonQuerySelectMethodByAuto(method, handler, argNames,
                     query);
         }
         sqlCommands.put(method.getName(), cmd);
     }
 
     protected boolean isQuerySelectMethodByAuto(Method method, String query) {
         return query != null && !startsWithOrderBy(query);
     }
 
     protected SelectDynamicCommand setupQuerySelectMethodByAuto(Method method,
             ResultSetHandler handler, String[] argNames, String query) {
         Class[] types = method.getParameterTypes();
         final SelectDynamicCommand cmd = createSelectDynamicCommand(handler,
                 query);
         cmd.setArgNames(argNames);
         cmd.setArgTypes(types);
         return cmd;
     }
 
     protected SelectDynamicCommand setupNonQuerySelectMethodByAuto(
             Method method, ResultSetHandler handler, String[] argNames,
             String query) {
         if (isAutoSelectSqlByDto(method, argNames)) {
             return setupNonQuerySelectMethodByDto(method, handler, argNames,
                     query);
         } else {
             return setupNonQuerySelectMethodByArgs(method, handler, argNames,
                     query);
         }
     }
 
     protected boolean isAutoSelectSqlByDto(Method method, String[] argNames) {
         return argNames.length == 0 && method.getParameterTypes().length == 1;
     }
 
     protected SelectDynamicCommand setupNonQuerySelectMethodByDto(
             Method method, ResultSetHandler handler, String[] argNames,
             String query) {
         final SelectDynamicCommand cmd = createSelectDynamicCommand(handler);
         Class clazz = method.getParameterTypes()[0];
         if (isUpdateSignatureForBean(method)) {
             clazz = beanClass;
         }
         final Class[] types = new Class[] { clazz };
         String sql = createAutoSelectSqlByDto(clazz);
         if (query != null) {
             sql = sql + " " + query;
         }
         cmd.setSql(sql);
         cmd.setArgNames(argNames);
         cmd.setArgTypes(types);
         return cmd;
     }
 
     protected SelectDynamicCommand setupNonQuerySelectMethodByArgs(
             Method method, ResultSetHandler handler, String[] argNames,
             String query) {
         final SelectDynamicCommand cmd = createSelectDynamicCommand(handler);
         final Class[] types = method.getParameterTypes();
         String sql = createAutoSelectSql(argNames);
         if (query != null) {
             sql = sql + " " + query;
         }
         cmd.setSql(sql);
         cmd.setArgNames(argNames);
         cmd.setArgTypes(types);
         return cmd;
     }
 
     protected String createAutoSelectSqlByDto(Class dtoClass) {
         String sql = dbms.getAutoSelectSql(getBeanMetaData());
         StringBuffer buf = new StringBuffer(sql);
         // TODO どうするか要検討
         if (dtoClass.isPrimitive()) {
             return sql;
         }
         DtoMetaData dmd = createDtoMetaData(dtoClass);
         boolean began = false;
         if (!(sql.lastIndexOf("WHERE") > 0)) {
             buf.append("/*BEGIN*/ WHERE ");
             began = true;
         }
         for (int i = 0; i < dmd.getPropertyTypeSize(); ++i) {
             PropertyType pt = dmd.getPropertyType(i);
             String aliasName = pt.getColumnName();
             if (!beanMetaData.hasPropertyTypeByAliasName(aliasName)) {
                 continue;
             }
             if (!beanMetaData.getPropertyTypeByAliasName(aliasName)
                     .isPersistent()) {
                 continue;
             }
             String columnName = beanMetaData.convertFullColumnName(aliasName);
             String propertyName = "dto." + pt.getPropertyName();
             buf.append("/*IF ");
             buf.append(propertyName);
             buf.append(" != null*/");
             buf.append(" ");
             if (!began || i != 0) {
                 buf.append("AND ");
             }
             buf.append(columnName);
             buf.append(" = /*");
             buf.append(propertyName);
             buf.append("*/null");
             buf.append("/*END*/");
         }
         if (began) {
             buf.append("/*END*/");
         }
         return buf.toString();
     }
 
     private DtoMetaDataImpl createDtoMetaData(Class dtoClass) {
         final DtoMetaDataImpl dtoMetaData = new DtoMetaDataImpl();
         dtoMetaData.setBeanClass(dtoClass);
         dtoMetaData.setBeanAnnotationReader(getAnnotationReaderFactory()
                 .createBeanAnnotationReader(dtoClass));
         dtoMetaData.setValueTypeFactory(getValueTypeFactory());
         dtoMetaData.initialize();
         for (int i = 0; i < beanMetaData.getPropertyTypeSize(); i++) {
             PropertyType master = beanMetaData.getPropertyType(i);
             String name = master.getPropertyName();
             if (dtoMetaData.hasPropertyType(name)) {
                 PropertyType slave = dtoMetaData.getPropertyType(name);
                 slave.setColumnName(master.getColumnName());
             }
         }
         return dtoMetaData;
     }
 
     protected String createAutoSelectSql(String[] argNames) {
         String sql = dbms.getAutoSelectSql(getBeanMetaData());
         StringBuffer buf = new StringBuffer(sql);
         if (argNames.length != 0) {
             boolean began = false;
             if (!(sql.lastIndexOf("WHERE") > 0)) {
                 buf.append("/*BEGIN*/ WHERE ");
                 began = true;
             }
             for (int i = 0; i < argNames.length; ++i) {
                 String columnName = beanMetaData
                         .convertFullColumnName(argNames[i]);
                 buf.append("/*IF ");
                 buf.append(argNames[i]);
                 buf.append(" != null*/");
                 buf.append(" ");
                 if (!began || i != 0) {
                     buf.append("AND ");
                 }
                 buf.append(columnName);
                 buf.append(" = /*");
                 buf.append(argNames[i]);
                 buf.append("*/null");
                 buf.append("/*END*/");
             }
             if (began) {
                 buf.append("/*END*/");
             }
         }
         return buf.toString();
     }
 
     protected void checkAutoUpdateMethod(Method method) {
         if (method.getParameterTypes().length != 1
                 || !isBeanClassAssignable(method.getParameterTypes()[0])
                 && !method.getParameterTypes()[0].isAssignableFrom(List.class)
                 && !method.getParameterTypes()[0].isArray()) {
             throw new IllegalSignatureRuntimeException("EDAO0006", method
                     .toString());
         }
     }
 
     protected boolean isSelect(Method method) {
         if (isInsert(method.getName())) {
             return false;
         }
         if (isUpdate(method.getName())) {
             return false;
         }
         if (isDelete(method.getName())) {
             return false;
         }
         return true;
     }
 
     protected boolean isInsert(String methodName) {
         final String[] insertPrefixes = getDaoNamingConvention()
                 .getInsertPrefixes();
         for (int i = 0; i < insertPrefixes.length; ++i) {
             if (methodName.startsWith(insertPrefixes[i])) {
                 return true;
             }
         }
         return false;
     }
 
     protected boolean isUpdate(String methodName) {
         final String[] updatePrefixes = getDaoNamingConvention()
                 .getUpdatePrefixes();
         for (int i = 0; i < updatePrefixes.length; ++i) {
             if (methodName.startsWith(updatePrefixes[i])) {
                 return true;
             }
         }
         return false;
     }
 
     protected boolean isDelete(String methodName) {
         final String[] deletePrefixes = getDaoNamingConvention()
                 .getDeletePrefixes();
         for (int i = 0; i < deletePrefixes.length; ++i) {
             if (methodName.startsWith(deletePrefixes[i])) {
                 return true;
             }
         }
         return false;
     }
 
     protected boolean isUnlessNull(String methodName) {
         final String[] unlessNullSuffixes = getDaoNamingConvention()
                 .getUnlessNullSuffixes();
         for (int i = 0; i < unlessNullSuffixes.length; i++) {
             if (methodName.endsWith(unlessNullSuffixes[i])) {
                 return true;
             }
         }
         return false;
     }
 
     protected boolean isModifiedOnly(final String methodName) {
         final String[] modifiedOnlySuffixes = getDaoNamingConvention()
                 .getModifiedOnlySuffixes();
         for (int i = 0; i < modifiedOnlySuffixes.length; i++) {
             if (methodName.endsWith(modifiedOnlySuffixes[i])) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * @see org.seasar.dao.DaoMetaData#getBeanClass()
      */
     public Class getBeanClass() {
         return beanClass;
     }
 
     protected void setBeanClass(Class beanClass) {
         this.beanClass = beanClass;
     }
 
     /**
      * @see org.seasar.dao.DaoMetaData#getBeanMetaData()
      */
     public BeanMetaData getBeanMetaData() {
         return beanMetaData;
     }
 
     /**
      * @see org.seasar.dao.DaoMetaData#getSqlCommand(java.lang.String)
      */
     public SqlCommand getSqlCommand(String methodName)
             throws MethodNotFoundRuntimeException {
 
         SqlCommand cmd = (SqlCommand) sqlCommands.get(methodName);
         if (cmd == null) {
             throw new MethodNotFoundRuntimeException(daoClass, methodName, null);
         }
         if (useDaoClassForLog) {
             if (cmd instanceof InjectDaoClassSupport) {
                 ((InjectDaoClassSupport) cmd).setDaoClass(daoClass);
             }
         }
         return cmd;
     }
 
     /**
      * @see org.seasar.dao.DaoMetaData#hasSqlCommand(java.lang.String)
      */
     public boolean hasSqlCommand(String methodName) {
         return sqlCommands.containsKey(methodName);
     }
 
     /**
      * @see org.seasar.dao.DaoMetaData#createFindCommand(java.lang.String)
      */
     public SqlCommand createFindCommand(String query) {
         return createSelectDynamicCommand(new BeanListMetaDataResultSetHandler(
                 beanMetaData, new RelationRowCreatorImpl()), query);
     }
 
     public SqlCommand createFindArrayCommand(String query) {
         return createSelectDynamicCommand(
                 new BeanArrayMetaDataResultSetHandler(beanMetaData,
                         new RelationRowCreatorImpl()), query);
     }
 
     /**
      * @see org.seasar.dao.DaoMetaData#createFindBeanCommand(java.lang.String)
      */
     public SqlCommand createFindBeanCommand(String query) {
         return createSelectDynamicCommand(new BeanMetaDataResultSetHandler(
                 beanMetaData, new RelationRowCreatorImpl()), query);
     }
 
     /**
      * @see org.seasar.dao.DaoMetaData#createFindObjectCommand(java.lang.String)
      */
     public SqlCommand createFindObjectCommand(String query) {
         return createSelectDynamicCommand(new ObjectResultSetHandler(), query);
     }
 
     public Class getDaoInterface(Class clazz) {
         if (clazz.isInterface()) {
             return clazz;
         }
         final String[] daoSuffixes = getDaoNamingConvention().getDaoSuffixes();
         for (Class target = clazz; target != AbstractDao.class; target = target
                 .getSuperclass()) {
             Class[] interfaces = target.getInterfaces();
             for (int i = 0; i < interfaces.length; ++i) {
                 Class intf = interfaces[i];
                 for (int j = 0; j < daoSuffixes.length; j++) {
                     if (intf.getName().endsWith(daoSuffixes[j])) {
                         return intf;
                     }
                 }
             }
         }
         throw new DaoNotFoundRuntimeException(clazz);
     }
 
     public void setDbms(Dbms dbms) {
         this.dbms = dbms;
     }
 
     public AnnotationReaderFactory getAnnotationReaderFactory() {
         return annotationReaderFactory;
     }
 
     public void setAnnotationReaderFactory(
             AnnotationReaderFactory annotationReaderFactory) {
         this.annotationReaderFactory = annotationReaderFactory;
     }
 
     public void setResultSetFactory(ResultSetFactory resultSetFactory) {
         this.resultSetFactory = resultSetFactory;
     }
 
     public void setStatementFactory(StatementFactory statementFactory) {
         this.statementFactory = statementFactory;
     }
 
     protected String getSqlFileEncoding() {
         return sqlFileEncoding;
     }
 
     public void setSqlFileEncoding(String sencoding) {
         this.sqlFileEncoding = sencoding;
     }
 
     public ValueTypeFactory getValueTypeFactory() {
         return valueTypeFactory;
     }
 
     public void setValueTypeFactory(ValueTypeFactory valueTypeFactory) {
         this.valueTypeFactory = valueTypeFactory;
     }
 
     public void setDataSource(DataSource dataSource) {
         this.dataSource = dataSource;
     }
 
     protected Class getDaoClass() {
         return daoClass;
     }
 
     public void setDaoClass(Class daoClass) {
         this.daoClass = daoClass;
     }
 
     public void setBeanMetaDataFactory(BeanMetaDataFactory beanMetaDataFactory) {
         this.beanMetaDataFactory = beanMetaDataFactory;
     }
 
     /**
      * @return Returns the dtoMetaDataFactory.
      */
     public DtoMetaDataFactory getDtoMetaDataFactory() {
         return dtoMetaDataFactory;
     }
 
     /**
      * @param dtoMetaDataFactory The dtoMetaDataFactory to set.
      */
     public void setDtoMetaDataFactory(DtoMetaDataFactory dtoMetaDataFactory) {
         this.dtoMetaDataFactory = dtoMetaDataFactory;
     }
 
     public DaoNamingConvention getDaoNamingConvention() {
         return daoNamingConvention;
     }
 
     public void setDaoNamingConvention(
             final DaoNamingConvention daoNamingConvention) {
         this.daoNamingConvention = daoNamingConvention;
     }
 
     public static class ResultSetHandlerFactoryImpl implements
             ResultSetHandlerFactory {
 
         final BeanMetaData beanMetaData;
 
         final DaoAnnotationReader annotationReader;
 
         final DtoMetaDataFactory dtoMetaDataFactory;
 
         public ResultSetHandlerFactoryImpl(BeanMetaData beanMetaData,
                 DaoAnnotationReader annotationReader,
                 DtoMetaDataFactory dtoMetaDataFactory) {
             this.beanMetaData = beanMetaData;
             this.annotationReader = annotationReader;
             this.dtoMetaDataFactory = dtoMetaDataFactory;
         }
 
         public ResultSetHandler createResultSetHandler(final Method method) {
             DtoMetaData dtoMetaData = null;
             Class beanClass = beanMetaData.getBeanClass();
             Class clazz = annotationReader.getBeanClass(method);
             if (clazz != null && !clazz.isAssignableFrom(beanClass)) {
                 if (Map.class.isAssignableFrom(clazz)) {
                     if (List.class.isAssignableFrom(method.getReturnType())) {
                         return createMapListResultSetHandler();
                     } else if (method.getReturnType().isArray()) {
                         return createMapArrayResultSetHandler();
                     } else {
                         return createMapResultSetHandler();
                     }
                 }
                 dtoMetaData = dtoMetaDataFactory.getDtoMetaData(clazz);
                 if (List.class.isAssignableFrom(method.getReturnType())) {
                     return createDtoListMetaDataResultSetHandler(dtoMetaData);
                 } else if (method.getReturnType() == clazz) {
                     return createDtoMetaDataResultSetHandler(dtoMetaData);
                 } else if (method.getReturnType().isArray()) {
                     return createDtoArrayMetaDataResultSetHandler(dtoMetaData);
                 }
             } else {
                 if (List.class.isAssignableFrom(method.getReturnType())) {
                     return createBeanListMetaDataResultSetHandler();
                 } else if (isBeanClassAssignable(beanClass, method
                         .getReturnType())) {
                     return createBeanMetaDataResultSetHandler();
                 } else if (method.getReturnType().isAssignableFrom(
                         Array.newInstance(beanClass, 0).getClass())) {
                     return createBeanArrayMetaDataResultSetHandler();
                 }
             }
             return createObjectResultSetHandler();
         }
 
         protected ResultSetHandler createDtoListMetaDataResultSetHandler(
                 DtoMetaData dtoMetaData) {
             return new DtoListMetaDataResultSetHandler(dtoMetaData);
         }
 
         protected ResultSetHandler createDtoMetaDataResultSetHandler(
                 DtoMetaData dtoMetaData) {
             return new DtoMetaDataResultSetHandler(dtoMetaData);
         }
 
         protected ResultSetHandler createDtoArrayMetaDataResultSetHandler(
                 DtoMetaData dtoMetaData) {
             return new DtoArrayMetaDataResultSetHandler(dtoMetaData);
         }
 
         protected ResultSetHandler createMapListResultSetHandler() {
             return new MapListResultSetHandler();
         }
 
         protected ResultSetHandler createMapResultSetHandler() {
             return new MapResultSetHandler();
         }
 
         protected ResultSetHandler createMapArrayResultSetHandler() {
             return new MapArrayResultSetHandler();
         }
 
         protected ResultSetHandler createBeanListMetaDataResultSetHandler() {
             return new BeanListMetaDataResultSetHandler(beanMetaData,
                     createRelationRowCreator());
         }
 
         protected ResultSetHandler createBeanMetaDataResultSetHandler() {
             return new BeanMetaDataResultSetHandler(beanMetaData,
                     createRelationRowCreator());
         }
 
         protected ResultSetHandler createBeanArrayMetaDataResultSetHandler() {
             return new BeanArrayMetaDataResultSetHandler(beanMetaData,
                     createRelationRowCreator());
         }
 
         protected ResultSetHandler createObjectResultSetHandler() {
             return new ObjectResultSetHandler();
         }
 
         protected RelationRowCreator createRelationRowCreator() {
             return new RelationRowCreatorImpl();
         }
 
         protected boolean isBeanClassAssignable(Class beanClass, Class clazz) {
             return beanClass.isAssignableFrom(clazz)
                     || clazz.isAssignableFrom(beanClass);
         }
 
     }
 
     public boolean isUseDaoClassForLog() {
         return useDaoClassForLog;
     }
 
     public void setUseDaoClassForLog(boolean setUserDaoClassForLog) {
         this.useDaoClassForLog = setUserDaoClassForLog;
     }
 
 }
