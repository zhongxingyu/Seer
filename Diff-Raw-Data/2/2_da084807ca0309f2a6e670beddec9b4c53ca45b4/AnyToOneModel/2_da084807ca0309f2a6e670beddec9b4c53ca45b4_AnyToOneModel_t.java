 package com.esn.idea.liquibaseejb.model.ejb.member;
 
 import com.esn.idea.liquibaseejb.model.database.DatabaseModel;
 import com.esn.idea.liquibaseejb.model.ejb.context.EjbModelContext;
 import com.esn.idea.liquibaseejb.model.ejb.module.ModuleModel;
 import com.intellij.javaee.model.common.persistence.mapping.JoinColumnBase;
 import com.intellij.javaee.model.common.persistence.mapping.OneToOne;
 import com.intellij.javaee.model.common.persistence.mapping.RelationAttributeBase;
 import com.intellij.persistence.model.PersistentAttribute;
 import com.intellij.psi.*;
 import com.intellij.util.xml.GenericValue;
 
 import javax.persistence.PrimaryKeyJoinColumn;
 import javax.persistence.PrimaryKeyJoinColumns;
 import java.util.List;
 import java.util.Collections;
 
 /**
  * Author: Marcus Nilsson
  * Date: 2008-nov-09
  * Time: 19:01:04
  */
 public class AnyToOneModel extends RelationModel<RelationAttributeBase.AnyToOneBase>
 {
     public AnyToOneModel(ModuleModel moduleModel, RelationAttributeBase.AnyToOneBase attribute)
     {
         super(moduleModel, attribute);
     }
 
     protected void executeMember(EjbModelContext context, DatabaseModel databaseModel, PsiMember psiMember, PsiType memberType)
     {
         if (memberType instanceof PsiClassType && !isAttributeInverse())
         {
             PsiClass memberClass = ((PsiClassType) memberType).resolve();
 
             PsiModifierList modifierList = psiMember.getModifierList();
             if (modifierList != null)
             {
                 if (modifierList.findAnnotation(PrimaryKeyJoinColumn.class.getName()) != null ||
                         modifierList.findAnnotation(PrimaryKeyJoinColumns.class.getName()) != null)
                 {
 
                     // No extra fields are needed
 
                     return;
                 }
             }
 
 
             String columnPrefix = psiMember.getName() + "_";
 
             Boolean isOptional = attribute.getOptional().getValue();
             if (isOptional == null) isOptional = true;
 
             String tableName = context.getTableName();
             RelationAttributeBase.NonManyToManyBase nonManyToMany = (RelationAttributeBase.NonManyToManyBase) attribute;
             List<? extends JoinColumnBase> joinColumnBases = nonManyToMany.getJoinColumns();
 
             List<JoinColumnBase> overridingJoinColumnBases = context.getAssociationOverride(getMember().getName());
             if (overridingJoinColumnBases != null && !overridingJoinColumnBases.isEmpty())
             {
                 joinColumnBases = overridingJoinColumnBases;
             }
 
            List<String> sourceColumns = createJoinColumns(databaseModel, tableName, memberClass, joinColumnBases, columnPrefix, isOptional, true, moduleModel, "fk_" + attribute.getName());
 
             if (attribute instanceof OneToOne)
             {
                 databaseModel.addUniqueConstraint(tableName, null, sourceColumns);
             }
         }
     }
 
     private boolean isAttributeInverse()
     {
         boolean isInverse = false;
 
         if (attribute instanceof OneToOne)
         {
             GenericValue<PersistentAttribute> mappedBy = ((OneToOne) attribute).getMappedBy();
             String mappedByStringValue = mappedBy.getStringValue();
             if (mappedByStringValue == null) mappedByStringValue = "";
 
             isInverse = !("".equals(mappedByStringValue));
         }
         return isInverse;
     }
 }
