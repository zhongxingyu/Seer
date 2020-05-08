 package com.esn.idea.liquibaseejb.model.ejb.clazz;
 
 import com.esn.idea.liquibaseejb.model.database.DatabaseIndex;
 import com.esn.idea.liquibaseejb.model.database.DatabaseModel;
 import com.esn.idea.liquibaseejb.model.ejb.context.DefaultEjbModelContext;
 import com.esn.idea.liquibaseejb.model.ejb.context.EjbModelContext;
 import com.esn.idea.liquibaseejb.model.ejb.context.OverridingEjbModelContext;
 import com.esn.idea.liquibaseejb.model.ejb.member.MemberModel;
 import com.esn.idea.liquibaseejb.model.ejb.module.ModuleModel;
 import com.esn.idea.liquibaseejb.util.EsnPsiUtils;
 import com.intellij.javaee.model.common.persistence.mapping.Entity;
 import com.intellij.javaee.model.common.persistence.mapping.SecondaryTable;
 import com.intellij.persistence.model.PersistentEntity;
 import com.intellij.psi.PsiAnnotation;
 import com.intellij.psi.PsiAnnotationMemberValue;
 import com.intellij.psi.PsiClass;
 import com.intellij.psi.PsiModifierList;
 
 import javax.persistence.*;
 import java.util.*;
 
 /**
  * Author: Marcus Nilsson
  * Date: 2008-nov-09
  * Time: 18:02:02
  */
 public class EntityModel extends ClassModel<Entity>
 {
     public EntityModel(ModuleModel moduleModel, Entity persistentObject)
     {
         super(moduleModel, persistentObject);
     }
 
     public Collection<EjbModelContext> findAllContexts()
     {
         Collection<EjbModelContext> res = new ArrayList<EjbModelContext>();
         res.add(new DefaultEjbModelContext());
         return res;
     }
 
     public EjbModelContext createMemberContext(EjbModelContext parent)
     {
         PsiClass psiClass = persistentObject.getClazz().getValue();
 
         if (psiClass != null && parent.getTableName() == null)
         {
             String tableName = entityTableName();
             OverridingEjbModelContext subContext = new OverridingEjbModelContext(parent);
             subContext.setTableName(tableName);
 
             for (SecondaryTable secondaryTable : persistentObject.getSecondaryTables())
             {
                 String secondaryTableName = secondaryTable.getTableName().getStringValue();
                 if (secondaryTableName != null && !secondaryTableName.isEmpty())
                 {
                     subContext.addSecondaryTableName(secondaryTableName);
                 }
             }
 
             return subContext;
         }
 
         return parent;
     }
 
     public EjbModelContext createSuperclassContext(EjbModelContext context)
     {
 
         OverridingEjbModelContext subContext = new OverridingEjbModelContext(super.createSuperclassContext(context));
 
         // TODO: Add when IDEA-20920 is fixed
         /**
         for (AssociationOverride associationOverride : (List<? extends AssociationOverride>) persistentObject.getAssociationOverrides())
         {
             List<? extends JoinColumnBase> bases = associationOverride.getJoinColumns();
             subContext.setAssociationOverride(associationOverride.getName().getStringValue(), (List<JoinColumnBase>) bases);
         }
          */
 
         for (com.intellij.javaee.model.common.persistence.mapping.AttributeOverride attributeOverride : persistentObject.getAttributeOverrides())
         {
             subContext.setAttributeOverride(attributeOverride.getName().getStringValue(), attributeOverride.getColumn());
         }
 
         return subContext;
     }
 
     public String entityName()
     {
         String overrideEntityName = persistentObject.getName().getStringValue();
 
 
         if (overrideEntityName != null && !overrideEntityName.isEmpty()) return overrideEntityName;
         else
         {
             PsiClass value = persistentObject.getClazz().getValue();
             if (value != null)
             {
                 return value.getName();
             }
             else
             {
                 return "unknown";
             }
         }
     }
 
     private String entityTableName()
     {
        String declaredTableName = persistentObject.getTable().getTableName().getStringValue();
 
         if ("".equals(declaredTableName))
         {
             return entityName();
         }
         else return declaredTableName;
     }
 
     public void execute(EjbModelContext context, DatabaseModel databaseModel)
     {
         PsiClass psiClass = persistentObject.getClazz().getValue();
 
         if (psiClass != null)
         {
             /**
              * For some inheritance strategies, an entity may act as both a mapped superclass (not creating table)
              * and an entity creating a table. We check our context to see if the table was already created,
              * and if so act as a mapped superclass.
              */
             String tableName = entityTableName();
             if (context.getTableName() == null)
             {
                 databaseModel.addTable(tableName);
 
                 executeGenerators(context, databaseModel, psiClass);
 
                 if (!context.isOnlyId())
                 {
                     for (SecondaryTable secondaryTable : persistentObject.getSecondaryTables())
                     {
                         String secondaryTableName = secondaryTable.getTableName().getStringValue();
                         if (secondaryTableName != null && !secondaryTableName.isEmpty())
                         {
                             databaseModel.addTable(secondaryTableName);
 
                             MemberModel.createPrimaryKeyJoinColumns(databaseModel, secondaryTableName, psiClass,
                                     secondaryTable.getPrimaryKeyJoinColumns(), "", moduleModel, "fk_primary", true);
                         }
                     }
                 }
             }
 
             super.execute(context, databaseModel);
 
             if (context.getTableName() == null)
             {
                 List<String> primaryKey = new ArrayList<String>();
                 for (String columnName : databaseModel.getColumnNames(tableName))
                 {
                     if (databaseModel.getColumnModel(tableName, columnName).isPrimaryKey()) primaryKey.add(columnName);
                 }
     
 
                 if (!primaryKey.isEmpty()) databaseModel.addPrimaryKeyConstraint(tableName, "pk", primaryKey);
 
                 {
                     OverridingEjbModelContext subContext = new OverridingEjbModelContext();
                     subContext.setTableName(tableName);
                     executeClassUniqueConstraints(subContext, databaseModel, psiClass);
                 }
 
                 for (Map.Entry<String, DatabaseIndex> indexEntry : classToIndexes(psiClass).entrySet())
                 {
                     databaseModel.addIndex(tableName, indexEntry.getKey(), indexEntry.getValue().getColumnNames());
 
                 }
             }
         }
     }
 
     private static void executeClassUniqueConstraints(EjbModelContext context, DatabaseModel databaseModel, PsiClass psiClass)
     {
         PsiModifierList modifierList = psiClass.getModifierList();
         PsiAnnotation annotation;
         if (modifierList != null)
         {
             annotation = modifierList.findAnnotation(Table.class.getName());
             if (annotation != null)
             {
                 executeAnnotationUniqueConstraints(context, databaseModel, annotation);
             }
         }
     }
 
     public static Map<String, DatabaseIndex> classToIndexes(PsiClass psiClass)
     {
         Map<String, DatabaseIndex> res = new HashMap<String, DatabaseIndex>();
 
         PsiModifierList modifierList = psiClass.getModifierList();
         PsiAnnotation annotation;
         if (modifierList != null)
         {
             annotation = modifierList.findAnnotation("org.hibernate.annotations.Table");
             if (annotation != null)
             {
                 Collection<PsiAnnotationMemberValue> initializerValues = EsnPsiUtils.getInitializerValues(annotation.findAttributeValue("indexes"));
 
                 for (PsiAnnotationMemberValue initializerValue : initializerValues)
                 {
                     if (initializerValue instanceof PsiAnnotation)
                     {
                         PsiAnnotation indexAnnotation = (PsiAnnotation) initializerValue;
 
                         List<String> columnNames = EsnPsiUtils.getInitializerValues(indexAnnotation.findAttributeValue("columnNames"), String.class);
                         String name = EsnPsiUtils.getInitializerValue(indexAnnotation.findAttributeValue("name"), String.class);
 
                         if (name != null)
                         {
                             res.put(name, new DatabaseIndex(columnNames));
                         }
                     }
                 }
             }
         }
         return res;
     }
 }
