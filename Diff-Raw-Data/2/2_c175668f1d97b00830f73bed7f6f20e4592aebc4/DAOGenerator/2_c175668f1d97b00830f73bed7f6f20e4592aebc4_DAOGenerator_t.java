 package com.digitolio.jdbi.codegen.codemodel;
 
 import com.digitolio.jdbi.StrategyAwareMapBean;
 import com.digitolio.jdbi.annotations.AutoInsert;
 import com.digitolio.jdbi.annotations.AutoUpdateByPK;
 import com.digitolio.jdbi.annotations.PK;
 import com.digitolio.jdbi.auto.SqlDeleteByPk;
 import com.digitolio.jdbi.auto.SqlSelectByPK;
 import com.digitolio.jdbi.table.Column;
 import com.digitolio.jdbi.table.Table;
 import com.google.common.base.Optional;
 import com.google.common.io.Files;
 import com.sun.codemodel.*;
 import com.sun.jersey.core.impl.provider.entity.Inflector;
 import org.skife.jdbi.v2.Binding;
 import org.skife.jdbi.v2.sqlobject.*;
 import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
 import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * @author C.Koc
  */
 public class DAOGenerator {
 
     private final Table table;
     private final Class<?> clazz;
     private final File targetDir;
     private Inflector inflector = Inflector.getInstance();
     public DAOGenerator(Class<?> aClass, Table table, File targetDir) {
         this.clazz = aClass;
         this.table = table;
         this.targetDir = targetDir;
     }
 
     public void generate() throws JClassAlreadyExistsException, IOException {
         JCodeModel codeModel = new JCodeModel();
 
         JDefinedClass classDefinition = createClassDefinition(codeModel, clazz);
         addSelectById(codeModel, classDefinition, table, clazz);
         addInsert(classDefinition, table, clazz);
         addUpdate(classDefinition, table, clazz);
         addDeleteById(classDefinition, table, clazz);
         try {
             if (clazz.getDeclaredField("dirty") != null) {
                 addGetDirty(codeModel, classDefinition, table, clazz);
                 if(!table.getPrimaryKeyColumns().isEmpty()){
                     addCleanDirty(codeModel, classDefinition, table, clazz);
                 }
             }
         } catch (NoSuchFieldException ignored) {
 
         }
         Files.createParentDirs(targetDir);
         codeModel.build(targetDir);
 
     }
 
 /*
     @StrategyAwareMapBean
     @SingleValueResult
     @SqlQuery("SELECT * FROM BASIC_PROFILE WHERE DIRTY = 1 LIMIT :limit")
     List<BasicProfile> getDirtyBasicProfiles(@Bind("limit") Integer limit);
 */
 
 
     private void addGetDirty(JCodeModel codeModel, JDefinedClass classDefinition, Table table, Class<?> clazz) {
         JClass ref = codeModel.ref(List.class);
         JClass narrowed = ref.narrow(clazz);
         JMethod method = classDefinition.method(JMod.ABSTRACT, narrowed, "getDirty" + inflector.pluralize(clazz.getSimpleName()));
         JAnnotationUse queryAnnotation = method.annotate(SqlQuery.class);
         method.annotate(StrategyAwareMapBean.class);
         method.annotate(SingleValueResult.class);
         SqlDirtyGet sqlDirtyGet = new SqlDirtyGet(table);
         queryAnnotation.param("value", sqlDirtyGet.generate(new Binding()));
         JVar limit = method.param(Integer.class, "limit");
         JAnnotationUse bind = limit.annotate(Bind.class);
         bind.param("value", "limit");
     }
     /*
     @SqlBatch("UPDATE BASIC_PROFILE SET DIRTY = 0 WHERE USER_ID = :userId")
     @BatchChunkSize(100)
     void cleanDirty(@BindBean Iterator<BasicProfile> iterator);
     */
     private void addCleanDirty(JCodeModel codeModel,JDefinedClass classDefinition, Table table, Class<?> clazz) {
        JMethod method = classDefinition.method(JMod.ABSTRACT, JType.parse(codeModel, "void"), "cleanDirty");
         JAnnotationUse batchAnnotation = method.annotate(SqlBatch.class);
         SqlDirtyClean sqlDirtyClean = new SqlDirtyClean(table);
         batchAnnotation.param("value", sqlDirtyClean.generate(new Binding()));
 
         JAnnotationUse batchAnnotate = method.annotate(BatchChunkSize.class);
         batchAnnotate.param("value", 100);
 
 
         final JClass ref = codeModel.ref(Iterator.class);
         final JClass gen = codeModel.ref(clazz);
         JClass narrowed = ref.narrow(gen);
         JVar iterator = method.param(narrowed, "iterator");
         iterator.annotate(BindBean.class);
     }
 
     private void addDeleteById(JDefinedClass classDefinition, Table table, Class clazz) {
         JMethod method = classDefinition.method(JMod.ABSTRACT, Integer.class, "delete" + clazz.getSimpleName());
         JAnnotationUse deleteAnnotation = method.annotate(SqlUpdate.class);
         SqlDeleteByPk sqlDeleteById = new SqlDeleteByPk(table);
         deleteAnnotation.param("value", sqlDeleteById.generate(new Binding()));
         for (Column column : table.getPrimaryKeyColumns()) {
             JVar param = method.param(column.getField().getType(), uncapitalize(column.getFieldName()));
             JAnnotationUse bind = param.annotate(Bind.class);
             bind.param("value", uncapitalize(column.getFieldName()));
         }
     }
 
     private void addSelectById(JCodeModel codeModel, JDefinedClass classDefinition, Table table, Class<?> clazz) {
         JClass jClass = codeModel.ref(Optional.class).narrow(codeModel.ref(clazz));
         JMethod method = classDefinition.method(JMod.ABSTRACT, jClass, "select" + clazz.getSimpleName());
         method.type();
         method.annotate(StrategyAwareMapBean.class);
         method.annotate(SingleValueResult.class);
         JAnnotationUse selectAnnotation = method.annotate(SqlQuery.class);
         SqlSelectByPK sqlSelectById = new SqlSelectByPK(table);
         selectAnnotation.param("value", sqlSelectById.generate(new Binding()));
         for (Column column : table.getPrimaryKeyColumns()) {
             JVar param = method.param(column.getField().getType(), uncapitalize(column.getFieldName()));
             JAnnotationUse bind = param.annotate(Bind.class);
             bind.param("value", uncapitalize(column.getFieldName()));
         }
     }
 
     private void addUpdate(JDefinedClass classDefinition, Table table, Class entity) {
         JMethod method = classDefinition.method(JMod.ABSTRACT, Integer.class, "update");
         method.annotate(AutoUpdateByPK.class);
         method.annotate(SqlUpdate.class);
         JVar param = method.param(entity, uncapitalize(entity.getSimpleName()));
         param.annotate(BindBean.class);
     }
 
     private void addInsert(JDefinedClass classDefinition, Table table, Class entity) {
         List<Column> pkColumns = table.getPrimaryKeyColumns();
         Field field = getAutoIncrementField(pkColumns);
         Class returnType = field != null ? field.getType() : Integer.class;
         JMethod method = classDefinition.method(JMod.ABSTRACT, returnType, "insert");
         method.annotate(AutoInsert.class);
         if (field != null) {
             method.annotate(GetGeneratedKeys.class);
         }
         method.annotate(SqlUpdate.class);
         JVar param = method.param(entity, uncapitalize(entity.getSimpleName()));
         param.annotate(BindBean.class);
     }
 
     private Field getAutoIncrementField(List<Column> pkColumns) {
         for (Column pkColumn : pkColumns) {
             PK annotation = pkColumn.getField().getAnnotation(PK.class);
             if (annotation != null && annotation.autoIncrement()) {
                 return pkColumn.getField();
             }
         }
         return null;
     }
 
     private String uncapitalize(String s) {
         return s.substring(0, 1).toLowerCase().concat(s.substring(1, s.length()));
     }
 
     private JDefinedClass createClassDefinition(JCodeModel codeModel,
                                                 Class entityClass) throws JClassAlreadyExistsException {
         Package aPackage = clazz.getPackage();
 
         String daoPackage =
                 aPackage == null ?
                         getDaoPackage(clazz.getName().split(".".concat(clazz.getSimpleName()))[0]) :
                         getDaoPackage(aPackage);
 
         return codeModel._class(getFullDaoClassName(daoPackage, clazz), ClassType.INTERFACE);
     }
 
     private String getDaoPackage(Package aPackage) {
         return getDaoPackage(aPackage.getName());
     }
 
     private String getDaoPackage(String name) {
         return name.concat(".").substring(0, name.lastIndexOf(".") + 1).concat("dao");
     }
 
     private String getFullDaoClassName(String aPackage, Class entity) {
         return aPackage.concat("._").concat(entity.getSimpleName().concat("DAO"));
     }
 
 }
