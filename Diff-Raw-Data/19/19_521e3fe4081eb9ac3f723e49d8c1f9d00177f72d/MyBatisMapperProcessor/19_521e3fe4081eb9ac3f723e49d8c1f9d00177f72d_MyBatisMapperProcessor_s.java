 /*
  * Copyright 2012 and beyond, Juan Luis Paz
  *
  * This file is part of Uaithne.
  *
  * Uaithne is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Uaithne is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with Uaithne. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.uaithne.generator.processors.database.myBatis;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.annotation.processing.RoundEnvironment;
 import javax.annotation.processing.SupportedAnnotationTypes;
 import javax.annotation.processing.SupportedSourceVersion;
 import javax.lang.model.SourceVersion;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.ElementKind;
 import javax.lang.model.element.TypeElement;
 import javax.tools.Diagnostic;
 import javax.tools.FileObject;
 import javax.tools.StandardLocation;
 import org.uaithne.annotations.myBatis.MyBatisBackendConfiguration;
 import org.uaithne.annotations.myBatis.MyBatisCustomSqlStatementId;
 import org.uaithne.annotations.myBatis.MyBatisMapper;
 import org.uaithne.generator.commons.*;
 import org.uaithne.generator.processors.database.QueryGenerator;
 import org.uaithne.generator.templates.operations.myBatis.MyBatisTemplate;
 
 @SupportedSourceVersion(SourceVersion.RELEASE_6)
 @SupportedAnnotationTypes("org.uaithne.annotations.myBatis.MyBatisMapper")
 public class MyBatisMapperProcessor extends TemplateProcessor {
     
     @Override
     public boolean process(Set<? extends TypeElement> set, RoundEnvironment re) {
         for (Element element : re.getElementsAnnotatedWith(MyBatisMapper.class)) {
             if (element.getKind() == ElementKind.CLASS) {
                 MyBatisMapper myBatisMapper = element.getAnnotation(MyBatisMapper.class);
                 if (myBatisMapper != null) {
                     MyBatisBackendConfiguration[] configurations = myBatisMapper.backendConfigurations();
                     if (configurations.length <= 0) {
                         configurations = getGenerationInfo().getMyBatisBackends();
                     }
                     for (MyBatisBackendConfiguration configuration : configurations) {
                         try {
                             process(re, element, configuration);
                         } catch(Exception ex) {
                             Logger.getLogger(MyBatisMapperProcessor.class.getName()).log(Level.SEVERE, null, ex);
                         }
                     }
                 }
             }
         }
         return true; // no further processing of this annotation type
     }
 
     //<editor-fold defaultstate="collapsed" desc="Process">
     public void process(RoundEnvironment re, Element element, MyBatisBackendConfiguration configuration) {
         TypeElement classElement = (TypeElement) element;
         
         QueryGenerator sqlGenerator = configuration.backend().getGenerator();
         boolean useAutoIncrementId = configuration.useAutoIncrementId().solve(configuration.backend().useAutoIncrementId());
         sqlGenerator.setProcessingEnv(processingEnv);
         sqlGenerator.setUseAutoIncrementId(useAutoIncrementId);
         sqlGenerator.setSequenceRegex(configuration.sequenceRegex());
         sqlGenerator.setSequenceReplacement(configuration.sequenceReplacement());
 
         ExecutorModuleInfo module = getGenerationInfo().getExecutorModuleByRealName(classElement);
         if (module == null) {
             processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "For create a myBatis mapper the annotated class must be an operation module", element);
             return;
         }
 
         String packageName = NamesGenerator.createGenericPackageName(classElement, configuration.subPackageName());
         String name = configuration.mapperPrefix() + module.getNameUpper() + configuration.mapperSuffix();
 
         String namespace;
         if (packageName == null || packageName.isEmpty()) {
             namespace = name;
         } else {
             namespace = packageName + "." + name;
         }
 
         Writer writer = null;
         boolean hasUnimplementedOperations = false;
         try {
             FileObject fo = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, packageName, name + ".xml", element);
             writer = fo.openWriter();
             writer.write("<?xml version='1.0' encoding='UTF-8'?>\n");
             writer.write("<!DOCTYPE mapper PUBLIC '-//mybatis.org//DTD Mapper 3.0//EN' 'http://mybatis.org/dtd/mybatis-3-mapper.dtd'>\n");
             writer.write("<mapper namespace='");
             writer.write(namespace);
             writer.write("'>\n\n");
             for (OperationInfo operation : module.getOperations()) {
                 processOperation(sqlGenerator, operation, namespace, writer, useAutoIncrementId);
                 hasUnimplementedOperations = hasUnimplementedOperations || operation.isManually();
             }
             writer.write("</mapper>");
         } catch (IOException ex) {
             Logger.getLogger(MyBatisMapperProcessor.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             try {
                 if (writer != null) {
                     writer.close();
                 }
             } catch (IOException ex) {
                 Logger.getLogger(MyBatisMapperProcessor.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         processClassTemplate(new MyBatisTemplate(module, packageName, name, namespace, sqlGenerator.useAliasInOrderByTranslation(), hasUnimplementedOperations), element);
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Process operation">
     public void processOperation(QueryGenerator sqlGenerator, OperationInfo operation, String namespace, Writer writer, boolean useAutoIncrementId) throws IOException {
         if (operation.isManually()) {
             return;
         }
         MyBatisCustomSqlStatementId myBatisCustomSqlStatement = operation.getAnnotation(MyBatisCustomSqlStatementId.class);
         if (myBatisCustomSqlStatement != null) {
             String statementId = myBatisCustomSqlStatement.value();
             if (statementId.trim().isEmpty()) {
                 processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "You must provide a valid mybatis statement id", operation.getElement());
             }
             operation.setQueryId(statementId);
             if (operation.getOperationKind() == OperationKind.SELECT_PAGE) {
                 String countStatementId = myBatisCustomSqlStatement.countStatementId();
                 if (countStatementId.trim().isEmpty()) {
                     processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "You must provide a valid mybatis statement id for count the numbers of the rows", operation.getElement());
                 }
                 operation.setCountQueryId(countStatementId);
             } else {
                 String countStatementId = myBatisCustomSqlStatement.countStatementId();
                 if (!countStatementId.isEmpty()) {
                     processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "For this kind of operation is not allowed provide a mybatis statement id for count the numbers of the rows", operation.getElement());
                 }
             }
             return;
         }
         EntityInfo entity = operation.getEntity();
         if (entity != null) {
             entity = entity.getCombined();
         }
 
         switch (operation.getOperationKind()) {
             case CUSTOM: {
             }
             break;
             case SELECT_ONE: {
                 operation.setQueryId(namespace + "." + operation.getMethodName());
                 String[] query = sqlGenerator.getSelectOneQuery(operation);
                 if (query != null) {
                     writeSelect(writer,
                             operation.getMethodName(),
                             operation.getDataType().getQualifiedNameWithoutGenerics(),
                             operation.getReturnDataType().getQualifiedNameWithoutGenerics(),
                             query);
                 }
             }
             break;
             case SELECT_MANY: {
                 operation.setQueryId(namespace + "." + operation.getMethodName());
                 String[] query = sqlGenerator.getSelectManyQuery(operation);
                 if (query != null) {
                     writeSelect(writer,
                             operation.getMethodName(),
                             operation.getDataType().getQualifiedNameWithoutGenerics(),
                             operation.getOneItemReturnDataType().getQualifiedNameWithoutGenerics(),
                             query);
                 }
             }
             break;
             case SELECT_PAGE: {
                 operation.setQueryId(namespace + "." + operation.getMethodName() + "Page");
                 operation.setCountQueryId(namespace + "." + operation.getMethodName() + "Count");
                 String[] query = sqlGenerator.getSelectPageQuery(operation);
                 if (query != null) {
                     writeSelect(writer,
                             operation.getMethodName() + "Page",
                             operation.getDataType().getQualifiedNameWithoutGenerics(),
                             operation.getOneItemReturnDataType().getQualifiedNameWithoutGenerics(),
                             query);
                 }
                 query = sqlGenerator.getSelectPageCountQuery(operation);
                 if (query != null) {
                     writeSelect(writer,
                             operation.getMethodName() + "Count",
                             operation.getDataType().getQualifiedNameWithoutGenerics(),
                             DataTypeInfo.PAGE_INFO_DATA_TYPE.getQualifiedNameWithoutGenerics(),
                             query);
                 }
             }
             break;
             case DELETE_BY_ID: {
                 if (entity == null) {
                     processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to find the entity related to the operation", operation.getElement());
                     break;
                 }
                 String[] query = sqlGenerator.getEntityDeleteByIdQuery(entity, operation);
                 if (query != null) {
                     writeDelete(writer,
                             operation.getMethodName(),
                             entity.getFirstIdField().getDataType().getQualifiedNameWithoutGenerics(),
                             query);
                 }
             }
             break;
             case INSERT: {
                 if (entity == null) {
                     processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to find the entity related to the operation", operation.getElement());
                     break;
                 }
                 FieldInfo id = entity.getFirstIdField();
                 boolean useGeneratedKey = id.isIdentifierAutogenerated();
                 if (useGeneratedKey) {
                    String[] query = sqlGenerator.getEntityLastInsertedIdQuery(entity, operation);
                    if (query != null) {
                        writeSelectWithoutParameter(writer,
                                "lastInsertedIdFor" + entity.getDataType().getSimpleNameWithoutGenerics(),
                                operation.getReturnDataType().getQualifiedNameWithoutGenerics(),
                                query);
                     }
                    useGeneratedKey = query == null && useAutoIncrementId;
                 } else {
                     operation.setReturnIdFromObjectWhenInsert(true);
                 }
                 String[] query = sqlGenerator.getEntityInsertQuery(entity, operation);
                 if (query != null) {
                     if (useGeneratedKey) {
                         String keyProperty = id.getName();
                         String keyColumn = id.getMappedNameOrName();
                         operation.setReturnIdFromObjectWhenInsert(true);
                         writeInsertWithGeneratedKey(writer,
                                 operation.getMethodName(),
                                 entity.getDataType().getQualifiedNameWithoutGenerics(),
                                 query,
                                 keyProperty,
                                 keyColumn);
                     } else {
                         writeInsert(writer,
                                 operation.getMethodName(),
                                 entity.getDataType().getQualifiedNameWithoutGenerics(),
                                 query);
                     }
                 }
             }
             break;
             case JUST_INSERT: {
             }
             break;
             case SAVE: {
                 if (entity == null) {
                     processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to find the entity related to the operation", operation.getElement());
                     break;
                 }
                 FieldInfo id = entity.getFirstIdField();
                 boolean useGeneratedKey = id.isIdentifierAutogenerated();
                 if (useGeneratedKey) {
                     if (useAutoIncrementId) {
                         String[] query = sqlGenerator.getEntityLastInsertedIdQuery(entity, operation);
                         if (query == null) {
                             operation.setReturnIdFromObjectWhenInsert(true);
                         }
                     }
                 } else {
                     operation.setReturnIdFromObjectWhenInsert(true);
                 }
             }
             break;
             case JUST_SAVE: {
             }
             break;
             case SELECT_BY_ID: {
                 if (entity == null) {
                     processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to find the entity related to the operation", operation.getElement());
                     break;
                 }
                 String[] query = sqlGenerator.getEntitySelectByIdQuery(entity, operation);
                 if (query != null) {
                     writeSelect(writer,
                             operation.getMethodName(),
                             entity.getFirstIdField().getDataType().getQualifiedNameWithoutGenerics(),
                             operation.getReturnDataType().getQualifiedNameWithoutGenerics(),
                             query);
                 }
             }
             break;
             case UPDATE: {
                 if (entity == null) {
                     processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to find the entity related to the operation", operation.getElement());
                     break;
                 }
                 String[] query = sqlGenerator.getEntityUpdateQuery(entity, operation);
                 if (query != null) {
                     writeUpdate(writer,
                             operation.getMethodName(),
                             entity.getDataType().getQualifiedNameWithoutGenerics(),
                             query);
                 }
             }
             break;
             case CUSTOM_INSERT: {
                 operation.setQueryId(namespace + "." + operation.getMethodName());
                 String[] query = sqlGenerator.getCustomInsertQuery(operation);
                 if (query != null) {
                     writeInsert(writer,
                             operation.getMethodName(),
                             operation.getDataType().getQualifiedNameWithoutGenerics(),
                             query);
                 }
             }
             break;
             case CUSTOM_UPDATE: {
                 operation.setQueryId(namespace + "." + operation.getMethodName());
                 String[] query = sqlGenerator.getCustomUpdateQuery(operation);
                 if (query != null) {
                     writeUpdate(writer,
                             operation.getMethodName(),
                             operation.getDataType().getQualifiedNameWithoutGenerics(),
                             query);
                 }
             }
             break;
             case CUSTOM_DELETE: {
                 operation.setQueryId(namespace + "." + operation.getMethodName());
                 String[] query = sqlGenerator.getCustomDeleteQuery(operation);
                 if (query != null) {
                     writeDelete(writer,
                             operation.getMethodName(),
                             operation.getDataType().getQualifiedNameWithoutGenerics(),
                             query);
                 }
             }
             break;
             case CUSTOM_INSERT_WITH_ID: {
                 operation.setQueryId(namespace + "." + operation.getMethodName());
                 String[] query = sqlGenerator.getCustomInsertQuery(operation);
                 if (query != null) {
                     writeInsert(writer,
                             operation.getMethodName(),
                             operation.getDataType().getQualifiedNameWithoutGenerics(),
                             query);
                 }
             }
             break;
             case MERGE: {
                 if (entity == null) {
                     processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Unable to find the entity related to the operation", operation.getElement());
                     break;
                 }
                 String[] query = sqlGenerator.getEntityMergeQuery(entity, operation);
                 if (query != null) {
                     writeUpdate(writer,
                             operation.getMethodName(),
                             entity.getDataType().getQualifiedNameWithoutGenerics(),
                             query);
                 }
             }
             break;
         }
     }
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc="Write xml entries">
     public void writeUpdate(Writer writer, String id, String parameterType, String[] lines) throws IOException {
         writer.write("    <update id='");
         writer.write(id);
         writer.write("' parameterType='");
         writer.write(parameterType);
         writer.write("'>\n");
         if (lines != null) {
             for (String line : lines) {
                 writer.write("        ");
                 writer.write(line);
                 writer.write("\n");
             }
         }
         writer.write("    </update>\n\n");
     }
 
     public void writeSelect(Writer writer, String id, String parameterType, String resultType, String[] lines) throws IOException {
         writer.write("    <select id='");
         writer.write(id);
         writer.write("' parameterType='");
         writer.write(parameterType);
         writer.write("' resultType='");
         writer.write(resultType);
         writer.write("'>\n");
         if (lines != null) {
             for (String line : lines) {
                 writer.write("        ");
                 writer.write(line);
                 writer.write("\n");
             }
         }
         writer.write("    </select>\n\n");
     }
 
     public void writeSelectWithoutParameter(Writer writer, String id, String resultType, String[] lines) throws IOException {
         writer.write("    <select id='");
         writer.write(id);
         writer.write("' resultType='");
         writer.write(resultType);
         writer.write("'>\n");
         if (lines != null) {
             for (String line : lines) {
                 writer.write("        ");
                 writer.write(line);
                 writer.write("\n");
             }
         }
         writer.write("    </select>\n\n");
     }
 
     public void writeInsert(Writer writer, String id, String parameterType, String[] lines) throws IOException {
         writer.write("    <insert id='");
         writer.write(id);
         writer.write("' parameterType='");
         writer.write(parameterType);
         writer.write("'>\n");
         if (lines != null) {
             for (String line : lines) {
                 writer.write("        ");
                 writer.write(line);
                 writer.write("\n");
             }
         }
         writer.write("    </insert>\n\n");
     }
 
     public void writeInsertWithGeneratedKey(Writer writer, String id, String parameterType, String[] lines, String keyProperty, String keyColumn) throws IOException {
         writer.write("    <insert id='");
         writer.write(id);
         writer.write("' parameterType='");
         writer.write(parameterType);
         writer.write("' useGeneratedKeys='true' keyProperty='");
         writer.write(keyProperty);
         writer.write("' keyColumn='");
         writer.write(keyColumn);
         writer.write("'>\n");
         if (lines != null) {
             for (String line : lines) {
                 writer.write("        ");
                 writer.write(line);
                 writer.write("\n");
             }
         }
         writer.write("    </insert>\n\n");
     }
 
     public void writeDelete(Writer writer, String id, String parameterType, String[] lines) throws IOException {
         writer.write("    <delete id='");
         writer.write(id);
         writer.write("' parameterType='");
         writer.write(parameterType);
         writer.write("'>\n");
         if (lines != null) {
             for (String line : lines) {
                 writer.write("        ");
                 writer.write(line);
                 writer.write("\n");
             }
         }
         writer.write("    </delete>\n\n");
     }
     //</editor-fold>
 }
