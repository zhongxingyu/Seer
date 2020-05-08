 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import java.util.List;
 import java.util.Set;
 import java.util.SortedSet;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.Settings;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.CallableUnitInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassReferenceInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.EntityUsageInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldUsageInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.Members;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetFieldInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetInnerClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeParameterInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.UnknownEntityUsageInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.UnknownTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.external.ExternalClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.external.ExternalFieldInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.DefaultMessagePrinter;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessagePrinter;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessageSource;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessagePrinter.MESSAGE_TYPE;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.util.LANGUAGE;
 
 
 /**
  * GeBeBgpۑ邽߂̃NXD GeBeBgpƂ́CpbP[WNX̎Q \D
  * 
  * @author higo
  * 
  */
 public final class UnresolvedUnknownUsageInfo extends UnresolvedEntityUsageInfo {
 
     /**
      * GeBeBgpIuWFNg쐬D
      * 
      * @param availableNamespaces p\ȖO
      * @param name GeBeBgp
      */
     public UnresolvedUnknownUsageInfo(final Set<AvailableNamespaceInfo> availableNamespaces,
             final String[] name, final int fromLine, final int fromColumn, final int toLine,
             final int toColumn) {
 
         this.availableNamespaces = availableNamespaces;
         this.name = name;
 
         this.setFromLine(fromLine);
         this.setFromColumn(fromColumn);
         this.setToLine(toLine);
         this.setToColumn(toColumn);
 
         this.resolvedIndo = null;
     }
 
     /**
      * GeBeBgpĂ邩ǂԂ
      * 
      * @return Ăꍇ trueCłȂꍇ false
      */
     @Override
     public boolean alreadyResolved() {
         return null != this.resolvedIndo;
     }
 
     /**
      * ς݃GeBeBgpԂ
      * 
      * @return ς݃GeBeBgp
      * @throws ĂȂꍇɃX[
      */
     @Override
     public EntityUsageInfo getResolvedEntityUsage() {
 
         if (!this.alreadyResolved()) {
             throw new NotResolvedException();
         }
 
         return this.resolvedIndo;
     }
 
     @Override
     public EntityUsageInfo resolveEntityUsage(final TargetClassInfo usingClass,
             final CallableUnitInfo usingMethod, final ClassInfoManager classInfoManager,
             final FieldInfoManager fieldInfoManager, final MethodInfoManager methodInfoManager) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if ((null == usingClass) || (null == usingMethod) || (null == classInfoManager)
                 || (null == methodInfoManager)) {
             throw new NullPointerException();
         }
 
         // ɉς݂łꍇ́CLbVԂ
         if (this.alreadyResolved()) {
             return this.getResolvedEntityUsage();
         }
 
         // GeBeBQƖ擾
         final String[] name = this.getName();
 
         // ʒu擾
         final int fromLine = this.getFromLine();
         final int fromColumn = this.getFromColumn();
         final int toLine = this.getToLine();
         final int toColumn = this.getToColumn();
 
         // p\ȃCX^XtB[hGeBeB
         {
             // ̃NXŗp\ȃCX^XtB[hꗗ擾
             final List<TargetFieldInfo> availableFieldsOfThisClass = Members
                     .<TargetFieldInfo> getInstanceMembers(NameResolver
                             .getAvailableFields(usingClass));
 
             for (final TargetFieldInfo availableFieldOfThisClass : availableFieldsOfThisClass) {
 
                 // vtB[hꍇ
                 if (name[0].equals(availableFieldOfThisClass.getName())) {
                     // usingMethod.addReferencee(availableFieldOfThisClass);
                     // availableFieldOfThisClass.addReferencer(usingMethod);
 
                     // ě^𐶐
                     final ClassTypeInfo usingClassType = new ClassTypeInfo(usingClass);
                     for (final TypeParameterInfo typeParameter : usingClass.getTypeParameters()) {
                         usingClassType.addTypeArgument(typeParameter);
                     }
 
                     // availableField.getType() 玟word(name[i])𖼑O
                     EntityUsageInfo entityUsage = new FieldUsageInfo(usingClassType,
                             availableFieldOfThisClass, true, fromLine, fromColumn, toLine, toColumn);
                     for (int i = 1; i < name.length; i++) {
 
                         // e UnknownTypeInfo Cǂ悤Ȃ
                         if (entityUsage.getType() instanceof UnknownTypeInfo) {
 
                             this.resolvedIndo = new UnknownEntityUsageInfo(fromLine, fromColumn,
                                     toLine, toColumn);
                             return this.resolvedIndo;
 
                             // eNX^̏ꍇ
                         } else if (entityUsage.getType() instanceof ClassTypeInfo) {
 
                             final ClassInfo ownerClass = ((ClassTypeInfo) entityUsage.getType())
                                     .getReferencedClass();
 
                             // eΏۃNX(TargetClassInfo)̏ꍇ
                             if (ownerClass instanceof TargetClassInfo) {
 
                                 // ܂͗p\ȃtB[hꗗ擾
                                 boolean found = false;
                                 {
                                     // p\ȃCX^XtB[hꗗ擾
                                     final List<TargetFieldInfo> availableFields = Members
                                             .getInstanceMembers(NameResolver.getAvailableFields(
                                                     (TargetClassInfo) ownerClass, usingClass));
 
                                     for (final TargetFieldInfo availableField : availableFields) {
 
                                         // vtB[hꍇ
                                         if (name[i].equals(availableField.getName())) {
                                             // usingMethod.addReferencee(availableField);
                                             // availableField.addReferencer(usingMethod);
 
                                             entityUsage = new FieldUsageInfo(entityUsage.getType(),
                                                     availableField, true, fromLine, fromColumn,
                                                     toLine, toColumn);
                                             found = true;
                                             break;
                                         }
                                     }
                                 }
 
                                 // p\ȃtB[hȂꍇ́CONXłeNX͂D
                                 // ̃NX̃tB[hgpĂƂ݂Ȃ
                                 {
                                     if (!found) {
 
                                         final ClassInfo referencedClass = ((ClassTypeInfo) entityUsage
                                                 .getType()).getReferencedClass();
                                         final ExternalClassInfo externalSuperClass = NameResolver
                                                 .getExternalSuperClass((TargetClassInfo) referencedClass);
                                         if (!(referencedClass instanceof TargetInnerClassInfo)
                                                 && (null != externalSuperClass)) {
 
                                             final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(
                                                     name[i], externalSuperClass);
 
                                             // usingMethod.addReferencee(fieldInfo);
                                             // fieldInfo.addReferencer(usingMethod);
                                             fieldInfoManager.add(fieldInfo);
 
                                             entityUsage = new FieldUsageInfo(entityUsage.getType(),
                                                     fieldInfo, true, fromLine, fromColumn, toLine,
                                                     toColumn);
 
                                         } else {
                                             assert false : "Can't resolve entity usage1 : "
                                                     + this.toString();
                                         }
                                     }
                                 }
 
                                 // eONX(ExternalClassInfo)̏ꍇ
                             } else if (ownerClass instanceof ExternalClassInfo) {
 
                                 final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(name[i],
                                         ownerClass);
 
                                 // usingMethod.addReferencee(fieldInfo);
                                 // fieldInfo.addReferencer(usingMethod);
                                 fieldInfoManager.add(fieldInfo);
 
                                 entityUsage = new FieldUsageInfo(entityUsage.getType(), fieldInfo,
                                         true, fromLine, fromColumn, toLine, toColumn);
                             }
 
                         } else {
                             assert false : "Here shouldn't be reached!";
                         }
                     }
 
                     this.resolvedIndo = entityUsage;
                     return this.resolvedIndo;
                 }
             }
         }
 
         // p\ȃX^eBbNtB[hGeBeB
         {
             // ̃NXŗp\ȃX^eBbNtB[hꗗ擾
             final List<TargetFieldInfo> availableFieldsOfThisClass = Members
                     .<TargetFieldInfo> getStaticMembers(NameResolver.getAvailableFields(usingClass));
 
             for (final TargetFieldInfo availableFieldOfThisClass : availableFieldsOfThisClass) {
 
                 // vtB[hꍇ
                 if (name[0].equals(availableFieldOfThisClass.getName())) {
                     // usingMethod.addReferencee(availableFieldOfThisClass);
                     // availableFieldOfThisClass.addReferencer(usingMethod);
 
                     // ě^𐶐
                     final ClassTypeInfo usingClassType = new ClassTypeInfo(usingClass);
                     for (final TypeParameterInfo typeParameter : usingClass.getTypeParameters()) {
                         usingClassType.addTypeArgument(typeParameter);
                     }
 
                     // availableField.getType() 玟word(name[i])𖼑O
                     EntityUsageInfo entityUsage = new FieldUsageInfo(usingClassType,
                             availableFieldOfThisClass, true, fromLine, fromColumn, toLine, toColumn);
                     for (int i = 1; i < name.length; i++) {
 
                         // e UnknownTypeInfo Cǂ悤Ȃ
                         if (entityUsage.getType() instanceof UnknownTypeInfo) {
 
                             this.resolvedIndo = new UnknownEntityUsageInfo(fromLine, fromColumn,
                                     toLine, toColumn);
                             return this.resolvedIndo;
 
                             // eNX^̏ꍇ
                         } else if (entityUsage.getType() instanceof ClassTypeInfo) {
 
                             final ClassInfo ownerClass = ((ClassTypeInfo) entityUsage.getType())
                                     .getReferencedClass();
 
                             // eΏۃNX(TargetClassInfo)̏ꍇ
                             if (ownerClass instanceof TargetClassInfo) {
 
                                 // ܂͗p\ȃtB[hꗗ擾
                                 boolean found = false;
                                 {
                                     // p\ȃX^eBbNtB[hꗗ擾
                                     final List<TargetFieldInfo> availableFields = Members
                                             .getStaticMembers(NameResolver.getAvailableFields(
                                                     (TargetClassInfo) ownerClass, usingClass));
 
                                     for (final TargetFieldInfo availableField : availableFields) {
 
                                         // vtB[hꍇ
                                         if (name[i].equals(availableField.getName())) {
                                             // usingMethod.addReferencee(availableField);
                                             // availableField.addReferencer(usingMethod);
 
                                             entityUsage = new FieldUsageInfo(entityUsage.getType(),
                                                     availableField, true, fromLine, fromColumn,
                                                     toLine, toColumn);
                                             found = true;
                                             break;
                                         }
                                     }
                                 }
 
                                 // X^eBbNtB[hŌȂꍇ́CCi[NXT
                                 {
                                     if (!found) {
                                         // Ci[NXꗗ擾
                                         final SortedSet<TargetInnerClassInfo> innerClasses = NameResolver
                                                 .getAvailableDirectInnerClasses((TargetClassInfo) ownerClass);
                                         for (final TargetInnerClassInfo innerClass : innerClasses) {
 
                                             // vNXꍇ
                                             if (name[i].equals(innerClass.getClassName())) {
                                                 // TODO p֌W\zR[hKvH
 
                                                 final ClassTypeInfo referenceType = new ClassTypeInfo(
                                                         innerClass);
                                                 entityUsage = new ClassReferenceInfo(referenceType,
                                                         fromLine, fromColumn, toLine, toColumn);
                                                 found = true;
                                                 break;
                                             }
                                         }
                                     }
                                 }
 
                                 // p\ȃtB[hȂꍇ́CONXłeNX͂D
                                 // ̃NX̃tB[hgpĂƂ݂Ȃ
                                 {
                                     if (!found) {
 
                                         final ClassInfo referencedClass = ((ClassTypeInfo) entityUsage
                                                 .getType()).getReferencedClass();
                                         final ExternalClassInfo externalSuperClass = NameResolver
                                                 .getExternalSuperClass((TargetClassInfo) referencedClass);
                                         if (!(referencedClass instanceof TargetInnerClassInfo)
                                                 && (null != externalSuperClass)) {
 
                                             final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(
                                                     name[i], externalSuperClass);
 
                                             // usingMethod.addReferencee(fieldInfo);
                                             // fieldInfo.addReferencer(usingMethod);
                                             fieldInfoManager.add(fieldInfo);
 
                                             entityUsage = new FieldUsageInfo(entityUsage.getType(),
                                                     fieldInfo, true, fromLine, fromColumn, toLine,
                                                     toColumn);
 
                                         } else {
                                             assert false : "Can't resolve entity usage2 : "
                                                     + this.toString();
                                         }
                                     }
                                 }
 
                                 // eONX(ExternalClassInfo)̏ꍇ
                             } else if (ownerClass instanceof ExternalClassInfo) {
 
                                 final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(name[i],
                                         ownerClass);
 
                                 // usingMethod.addReferencee(fieldInfo);
                                 // fieldInfo.addReferencer(usingMethod);
                                 fieldInfoManager.add(fieldInfo);
 
                                 entityUsage = new FieldUsageInfo(entityUsage.getType(), fieldInfo,
                                         true, fromLine, fromColumn, toLine, toColumn);
                             }
 
                         } else {
                             assert false : "Here shouldn't be reached!";
                         }
                     }
 
                     this.resolvedIndo = entityUsage;
                     return this.resolvedIndo;
                 }
             }
         }
 
         // GeBeBS薼łꍇ
         {
 
             for (int length = 1; length <= name.length; length++) {
 
                 // 閼O(String[])쐬
                 final String[] searchingName = new String[length];
                 System.arraycopy(name, 0, searchingName, 0, length);
 
                 final ClassInfo searchingClass = classInfoManager.getClassInfo(searchingName);
                 if (null != searchingClass) {
 
                     EntityUsageInfo entityUsage = new ClassReferenceInfo(new ClassTypeInfo(
                             searchingClass), fromLine, fromColumn, toLine, toColumn);
                     for (int i = length; i < name.length; i++) {
 
                         // e UnknownTypeInfo Cǂ悤Ȃ
                         if (entityUsage.getType() instanceof UnknownTypeInfo) {
 
                             this.resolvedIndo = new UnknownEntityUsageInfo(fromLine, fromColumn,
                                     toLine, toColumn);
                             return this.resolvedIndo;
 
                             // eNX^̏ꍇ
                         } else if (entityUsage.getType() instanceof ClassTypeInfo) {
 
                             final ClassInfo ownerClass = ((ClassTypeInfo) entityUsage.getType())
                                     .getReferencedClass();
 
                             // eΏۃNX(TargetClassInfo)̏ꍇ
                             if (ownerClass instanceof TargetClassInfo) {
 
                                 // ܂͗p\ȃtB[hꗗ擾
                                 boolean found = false;
                                 {
                                     // p\ȃtB[hꗗ擾
                                     final List<TargetFieldInfo> availableFields = Members
                                             .getStaticMembers(NameResolver.getAvailableFields(
                                                     (TargetClassInfo) ownerClass, usingClass));
 
                                     for (final TargetFieldInfo availableField : availableFields) {
 
                                         // vtB[hꍇ
                                         if (name[i].equals(availableField.getName())) {
                                             // usingMethod.addReferencee(availableField);
                                             // availableField.addReferencer(usingMethod);
 
                                             entityUsage = new FieldUsageInfo(entityUsage.getType(),
                                                     availableField, true, fromLine, fromColumn,
                                                     toLine, toColumn);
                                             found = true;
                                             break;
                                         }
                                     }
                                 }
 
                                 // X^eBbNtB[hŌȂꍇ́CCi[NXT
                                 {
                                     if (!found) {
                                         // Ci[NXꗗ擾
                                         final SortedSet<TargetInnerClassInfo> innerClasses = NameResolver
                                                 .getAvailableDirectInnerClasses((TargetClassInfo) ownerClass);
                                         for (final TargetInnerClassInfo innerClass : innerClasses) {
 
                                             // vNXꍇ
                                             if (name[i].equals(innerClass.getClassName())) {
                                                 // TODO p֌W\zR[hKvH
 
                                                 final ClassTypeInfo referenceType = new ClassTypeInfo(
                                                         innerClass);
                                                 entityUsage = new ClassReferenceInfo(referenceType,
                                                         fromLine, fromColumn, toLine, toColumn);
                                                 found = true;
                                                 break;
                                             }
                                         }
                                     }
                                 }
 
                                 // p\ȃtB[hȂꍇ́CONXłeNX͂D
                                 // ̃NX̃tB[hgpĂƂ݂Ȃ
                                 {
                                     if (!found) {
 
                                         final ClassInfo referencedClass = ((ClassTypeInfo) entityUsage
                                                 .getType()).getReferencedClass();
                                         final ExternalClassInfo externalSuperClass = NameResolver
                                                 .getExternalSuperClass((TargetClassInfo) referencedClass);
                                         if (!(referencedClass instanceof TargetInnerClassInfo)
                                                 && (null != externalSuperClass)) {
 
                                             final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(
                                                     name[i], externalSuperClass);
 
                                             // usingMethod.addReferencee(fieldInfo);
                                             // fieldInfo.addReferencer(usingMethod);
                                             fieldInfoManager.add(fieldInfo);
 
                                             entityUsage = new FieldUsageInfo(entityUsage.getType(),
                                                     fieldInfo, true, fromLine, fromColumn, toLine,
                                                     toColumn);
 
                                         } else {
                                             assert false : "Can't resolve entity usage3 : "
                                                     + this.toString();
                                         }
                                     }
                                 }
 
                                 // eONX(ExternalClassInfo)̏ꍇ
                             } else if (ownerClass instanceof ExternalClassInfo) {
 
                                 final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(name[i],
                                         ownerClass);
 
                                 // usingMethod.addReferencee(fieldInfo);
                                 // fieldInfo.addReferencer(usingMethod);
                                 fieldInfoManager.add(fieldInfo);
 
                                 entityUsage = new FieldUsageInfo(entityUsage.getType(), fieldInfo,
                                         true, fromLine, fromColumn, toLine, toColumn);
                             }
 
                         } else {
                             assert false : "Here shouldn't be reached!";
                         }
                     }
 
                     this.resolvedIndo = entityUsage;
                     return this.resolvedIndo;
                 }
             }
         }
 
         // p\ȃNXGeBeB
         {
 
             // NX猟
             {
                 final TargetClassInfo outestClass;
                 if (usingClass instanceof TargetInnerClassInfo) {
                     outestClass = NameResolver.getOuterstClass((TargetInnerClassInfo) usingClass);
                 } else {
                     outestClass = usingClass;
                 }
 
                 for (final TargetInnerClassInfo innerClassInfo : NameResolver
                         .getAvailableInnerClasses(outestClass)) {
 
                     // NXƎQƖ̐擪ꍇ́C̃NXQƐłƌ肷
                     final String innerClassName = innerClassInfo.getClassName();
                     if (innerClassName.equals(name[0])) {
 
                         EntityUsageInfo entityUsage = new ClassReferenceInfo(new ClassTypeInfo(
                                 innerClassInfo), fromLine, fromColumn, toLine, toColumn);
                         for (int i = 1; i < name.length; i++) {
 
                             // e UnknownTypeInfo Cǂ悤Ȃ
                             if (entityUsage.getType() instanceof UnknownTypeInfo) {
 
                                 this.resolvedIndo = new UnknownEntityUsageInfo(fromLine,
                                         fromColumn, toLine, toColumn);
                                 return this.resolvedIndo;
 
                                 // eNX^̏ꍇ
                             } else if (entityUsage.getType() instanceof ClassTypeInfo) {
 
                                 final ClassInfo ownerClass = ((ClassTypeInfo) entityUsage.getType())
                                         .getReferencedClass();
 
                                 // eΏۃNX(TargetClassInfo)̏ꍇ
                                 if (ownerClass instanceof TargetClassInfo) {
 
                                     // ܂͗p\ȃtB[hꗗ擾
                                     boolean found = false;
                                     {
                                         // p\ȃtB[hꗗ擾
                                         final List<TargetFieldInfo> availableFields = NameResolver
                                                 .getAvailableFields((TargetClassInfo) ownerClass,
                                                         usingClass);
 
                                         for (final TargetFieldInfo availableField : availableFields) {
 
                                             // vtB[hꍇ
                                             if (name[i].equals(availableField.getName())) {
                                                 // usingMethod.addReferencee(availableField);
                                                 // availableField.addReferencer(usingMethod);
 
                                                 entityUsage = new FieldUsageInfo(entityUsage
                                                         .getType(), availableField, true, fromLine,
                                                         fromColumn, toLine, toColumn);
                                                 found = true;
                                                 break;
                                             }
                                         }
                                     }
 
                                     // X^eBbNtB[hŌȂꍇ́CCi[NXT
                                     {
                                         if (!found) {
                                             // Ci[NXꗗ擾
                                             final SortedSet<TargetInnerClassInfo> innerClasses = NameResolver
                                                     .getAvailableDirectInnerClasses((TargetClassInfo) ownerClass);
                                             for (final TargetInnerClassInfo innerClass : innerClasses) {
 
                                                 // vNXꍇ
                                                 if (name[i].equals(innerClass.getClassName())) {
                                                     // TODO p֌W\zR[hKvH
 
                                                     final ClassTypeInfo referenceType = new ClassTypeInfo(
                                                             innerClassInfo);
                                                     entityUsage = new ClassReferenceInfo(
                                                             referenceType, fromLine, fromColumn,
                                                             toLine, toColumn);
                                                     found = true;
                                                     break;
                                                 }
                                             }
                                         }
                                     }
 
                                     // p\ȃtB[hȂꍇ́CONXłeNX͂D
                                     // ̃NX̃tB[hgpĂƂ݂Ȃ
                                     {
                                         if (!found) {
 
                                             final ClassInfo referencedClass = ((ClassTypeInfo) entityUsage
                                                     .getType()).getReferencedClass();
                                             final ExternalClassInfo externalSuperClass = NameResolver
                                                     .getExternalSuperClass((TargetClassInfo) referencedClass);
                                             if (!(referencedClass instanceof TargetInnerClassInfo)
                                                     && (null != externalSuperClass)) {
 
                                                 final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(
                                                         name[i], externalSuperClass);
 
                                                 // usingMethod.addReferencee(fieldInfo);
                                                 // fieldInfo.addReferencer(usingMethod);
                                                 fieldInfoManager.add(fieldInfo);
 
                                                 entityUsage = new FieldUsageInfo(entityUsage
                                                         .getType(), fieldInfo, true, fromLine,
                                                         fromColumn, toLine, toColumn);
 
                                             } else {
                                                 assert false : "Can't resolve entity usage3.5 : "
                                                         + this.toString();
                                             }
                                         }
                                     }
 
                                     // eONX(ExternalClassInfo)̏ꍇ
                                 } else if (ownerClass instanceof ExternalClassInfo) {
 
                                     final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(
                                             name[i], ownerClass);
 
                                     // usingMethod.addReferencee(fieldInfo);
                                     // fieldInfo.addReferencer(usingMethod);
                                     fieldInfoManager.add(fieldInfo);
 
                                     entityUsage = new FieldUsageInfo(entityUsage.getType(),
                                             fieldInfo, true, fromLine, fromColumn, toLine, toColumn);
                                 }
 
                             } else {
                                 assert false : "Here shouldn't be reached!";
                             }
                         }
 
                         this.resolvedIndo = entityUsage;
                         return this.resolvedIndo;
                     }
                 }
             }
 
             // p\ȖOԂ猟
             {
                 for (final AvailableNamespaceInfo availableNamespace : this
                         .getAvailableNamespaces()) {
 
                     // OԖ.* ƂȂĂꍇ
                     if (availableNamespace.isAllClasses()) {
                         final String[] namespace = availableNamespace.getNamespace();
 
                         // OԂ̉ɂeNXɑ΂
                         for (final ClassInfo classInfo : classInfoManager.getClassInfos(namespace)) {
                             final String className = classInfo.getClassName();
 
                             // NXƎQƖ̐擪ꍇ́C̃NXQƐłƌ肷
                             if (className.equals(name[0])) {
 
                                 EntityUsageInfo entityUsage = new ClassReferenceInfo(
                                         new ClassTypeInfo(classInfo), fromLine, fromColumn, toLine,
                                         toColumn);
                                 for (int i = 1; i < name.length; i++) {
 
                                     // e UnknownTypeInfo Cǂ悤Ȃ
                                     if (entityUsage.getType() instanceof UnknownTypeInfo) {
 
                                         this.resolvedIndo = new UnknownEntityUsageInfo(fromLine,
                                                 fromColumn, toLine, toColumn);
                                         return this.resolvedIndo;
 
                                         // eNX^̏ꍇ
                                     } else if (entityUsage.getType() instanceof ClassTypeInfo) {
 
                                         final ClassInfo ownerClass = ((ClassTypeInfo) entityUsage
                                                 .getType()).getReferencedClass();
 
                                         // eΏۃNX(TargetClassInfo)̏ꍇ
                                         if (ownerClass instanceof TargetClassInfo) {
 
                                             // ܂͗p\ȃtB[hꗗ擾
                                             boolean found = false;
                                             {
                                                 // p\ȃtB[hꗗ擾
                                                 final List<TargetFieldInfo> availableFields = NameResolver
                                                         .getAvailableFields(
                                                                 (TargetClassInfo) ownerClass,
                                                                 usingClass);
 
                                                 for (TargetFieldInfo availableField : availableFields) {
 
                                                     // vtB[hꍇ
                                                     if (name[i].equals(availableField.getName())) {
                                                         // usingMethod.addReferencee(availableField);
                                                         // availableField.addReferencer(usingMethod);
 
                                                         entityUsage = new FieldUsageInfo(
                                                                 entityUsage.getType(),
                                                                 availableField, true, fromLine,
                                                                 fromColumn, toLine, toColumn);
                                                         found = true;
                                                         break;
                                                     }
                                                 }
                                             }
 
                                             // X^eBbNtB[hŌȂꍇ́CCi[NXT
                                             {
                                                 if (!found) {
                                                     // Ci[NXꗗ擾
                                                     final SortedSet<TargetInnerClassInfo> innerClasses = NameResolver
                                                             .getAvailableDirectInnerClasses((TargetClassInfo) ownerClass);
                                                     for (final TargetInnerClassInfo innerClass : innerClasses) {
 
                                                         // vNXꍇ
                                                         if (name[i].equals(innerClass
                                                                 .getClassName())) {
                                                             // TODO p֌W\zR[hKvH
 
                                                             final ClassTypeInfo referenceType = new ClassTypeInfo(
                                                                     innerClass);
                                                             entityUsage = new ClassReferenceInfo(
                                                                     referenceType, fromLine,
                                                                     fromColumn, toLine, toColumn);
                                                             found = true;
                                                             break;
                                                         }
                                                     }
                                                 }
                                             }
 
                                             // p\ȃtB[hȂꍇ́CONXłeNX͂D
                                             // ̃NX̃tB[hgpĂƂ݂Ȃ
                                             {
                                                 if (!found) {
 
                                                     final ClassInfo referencedClass = ((ClassTypeInfo) entityUsage
                                                             .getType()).getReferencedClass();
                                                     final ExternalClassInfo externalSuperClass = NameResolver
                                                             .getExternalSuperClass((TargetClassInfo) referencedClass);
                                                     if (!(referencedClass instanceof TargetInnerClassInfo)
                                                             && (null != externalSuperClass)) {
 
                                                         final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(
                                                                 name[i], externalSuperClass);
 
                                                         // usingMethod.addReferencee(fieldInfo);
                                                         // fieldInfo.addReferencer(usingMethod);
                                                         fieldInfoManager.add(fieldInfo);
 
                                                         entityUsage = new FieldUsageInfo(
                                                                 entityUsage.getType(), fieldInfo,
                                                                 true, fromLine, fromColumn, toLine,
                                                                 toColumn);
 
                                                     } else {
                                                         assert false : "Can't resolve entity usage4 : "
                                                                 + this.toString();
                                                     }
                                                 }
                                             }
 
                                             // eONX(ExternalClassInfo)̏ꍇ
                                         } else if (ownerClass instanceof ExternalClassInfo) {
 
                                             final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(
                                                     name[i], ownerClass);
 
                                             // usingMethod.addReferencee(fieldInfo);
                                             // fieldInfo.addReferencer(usingMethod);
                                             fieldInfoManager.add(fieldInfo);
 
                                             entityUsage = new FieldUsageInfo(entityUsage.getType(),
                                                     fieldInfo, true, fromLine, fromColumn, toLine,
                                                     toColumn);
                                         }
 
                                     } else {
                                         assert false : "Here shouldn't be reached!";
                                     }
                                 }
 
                                 this.resolvedIndo = entityUsage;
                                 return this.resolvedIndo;
                             }
                         }
 
                         // O.NX ƂȂĂꍇ
                     } else {
 
                         final String[] importName = availableNamespace.getImportName();
 
                         // NXƎQƖ̐擪ꍇ́C̃NXQƐłƌ肷
                         if (importName[importName.length - 1].equals(name[0])) {
 
                             ClassInfo specifiedClassInfo = classInfoManager
                                     .getClassInfo(importName);
                             if (null == specifiedClassInfo) {
                                 specifiedClassInfo = new ExternalClassInfo(importName);
                                 classInfoManager.add((ExternalClassInfo) specifiedClassInfo);
                             }
 
                             EntityUsageInfo entityUsage = new ClassReferenceInfo(new ClassTypeInfo(
                                     specifiedClassInfo), fromLine, fromColumn, toLine, toColumn);
                             for (int i = 1; i < name.length; i++) {
 
                                 // e UnknownTypeInfo Cǂ悤Ȃ
                                 if (entityUsage.getType() instanceof UnknownTypeInfo) {
 
                                     this.resolvedIndo = new UnknownEntityUsageInfo(fromLine,
                                             fromColumn, toLine, toColumn);
                                     return this.resolvedIndo;
 
                                     // eNX^̏ꍇ
                                 } else if (entityUsage.getType() instanceof ClassTypeInfo) {
 
                                     final ClassInfo ownerClass = ((ClassTypeInfo) entityUsage
                                             .getType()).getReferencedClass();
 
                                     // eΏۃNX(TargetClassInfo)̏ꍇ
                                     if (ownerClass instanceof TargetClassInfo) {
 
                                         // ܂͗p\ȃtB[hꗗ擾
                                         boolean found = false;
                                         {
                                             // p\ȃtB[hꗗ擾
                                             final List<TargetFieldInfo> availableFields = NameResolver
                                                     .getAvailableFields(
                                                             (TargetClassInfo) ownerClass,
                                                             usingClass);
 
                                             for (final TargetFieldInfo availableField : availableFields) {
 
                                                 // vtB[hꍇ
                                                 if (name[i].equals(availableField.getName())) {
                                                     // usingMethod.addReferencee(availableField);
                                                     // availableField.addReferencer(usingMethod);
 
                                                     entityUsage = new FieldUsageInfo(entityUsage
                                                             .getType(), availableField, true,
                                                             fromLine, fromColumn, toLine, toColumn);
                                                     found = true;
                                                     break;
                                                 }
                                             }
                                         }
 
                                         // X^eBbNtB[hŌȂꍇ́CCi[NXT
                                         {
                                             if (!found) {
                                                 // Ci[NXꗗ擾
                                                 final SortedSet<TargetInnerClassInfo> innerClasses = NameResolver
                                                         .getAvailableDirectInnerClasses((TargetClassInfo) ownerClass);
                                                 for (final TargetInnerClassInfo innerClass : innerClasses) {
 
                                                     // vNXꍇ
                                                     if (name[i].equals(innerClass.getClassName())) {
                                                         // TODO p֌W\zR[hKvH
 
                                                         final ClassTypeInfo referenceType = new ClassTypeInfo(
                                                                 innerClass);
                                                         entityUsage = new ClassReferenceInfo(
                                                                 referenceType, fromLine,
                                                                 fromColumn, toLine, toColumn);
                                                         found = true;
                                                         break;
                                                     }
                                                 }
                                             }
                                         }
 
                                         // p\ȃtB[hȂꍇ́CONXłeNX͂D
                                         // ̃NX̃tB[hgpĂƂ݂Ȃ
                                         {
                                             if (!found) {
 
                                                 final ClassInfo referencedClass = ((ClassTypeInfo) entityUsage
                                                         .getType()).getReferencedClass();
                                                 final ExternalClassInfo externalSuperClass = NameResolver
                                                         .getExternalSuperClass((TargetClassInfo) referencedClass);
                                                 if (!(referencedClass instanceof TargetInnerClassInfo)
                                                         && (null != externalSuperClass)) {
 
                                                     final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(
                                                             name[i], externalSuperClass);
 
                                                     // usingMethod.addReferencee(fieldInfo);
                                                     // fieldInfo.addReferencer(usingMethod);
                                                     fieldInfoManager.add(fieldInfo);
 
                                                     entityUsage = new FieldUsageInfo(entityUsage
                                                             .getType(), fieldInfo, true, fromLine,
                                                             fromColumn, toLine, toColumn);
 
                                                 } else {
                                                     assert false : "Can't resolve entity usage5 : "
                                                             + this.toString();
                                                 }
                                             }
                                         }
 
                                         // eONX(ExternalClassInfo)̏ꍇ
                                     } else if (ownerClass instanceof ExternalClassInfo) {
 
                                         final ExternalFieldInfo fieldInfo = new ExternalFieldInfo(
                                                 name[i], ownerClass);
 
                                         // usingMethod.addReferencee(fieldInfo);
                                         // fieldInfo.addReferencer(usingMethod);
                                         fieldInfoManager.add(fieldInfo);
 
                                         entityUsage = new FieldUsageInfo(entityUsage.getType(),
                                                 fieldInfo, true, fromLine, fromColumn, toLine,
                                                 toColumn);
                                     }
 
                                 } else {
                                     assert false : "Here shouldn't be reached!";
                                 }
                             }
 
                             this.resolvedIndo = entityUsage;
                             return this.resolvedIndo;
                         }
                     }
                 }
             }
         }
 
         // javȁꍇ́CjavajavaxŎn܂C3ȏUnknownEntityUsageInfoJDK̃NXƂ݂Ȃ
         if (Settings.getLanguage().equals(LANGUAGE.JAVA)) {
 
             if ((name[0].equals("java") || name[0].equals("javax")) && (3 <= name.length)) {
                 final ExternalClassInfo externalClass = new ExternalClassInfo(name);
                 final ClassTypeInfo externalClassType = new ClassTypeInfo(externalClass);
                 this.resolvedIndo = new ClassReferenceInfo(externalClassType, fromLine, fromColumn,
                         toLine, toColumn);
                 classInfoManager.add(externalClass);
             }
         }
 
         err.println("Remain unresolved \"" + this.toString() + "\"" + " line:" + this.getFromLine()
                 + " column:" + this.getFromColumn() + " on \""
                 + usingClass.getFullQualifiedName(LANGUAGE.JAVA.getNamespaceDelimiter()));
 
         // Ȃs
         usingMethod.addUnresolvedUsage(this);
 
         this.resolvedIndo = new UnknownEntityUsageInfo(fromLine, fromColumn, toLine, toColumn);
         return this.resolvedIndo;
     }
 
     /**
      * GeBeBgpԂD
      * 
      * @return GeBeBgp
      */
     public String[] getName() {
         return this.name;
     }
 
     @Override
     public String toString() {
         final StringBuilder sb = new StringBuilder(this.name[0]);
         for (int i = 1; i < this.name.length; i++) {
             sb.append(".");
             sb.append(this.name[i]);
         }
         return sb.toString();
     }
 
     /**
      * ̖GeBeBgpp邱Ƃ̂ł閼OԂԂD
      * 
      * @return ̖GeBeBgpp邱Ƃ̂ł閼O
      */
     public Set<AvailableNamespaceInfo> getAvailableNamespaces() {
         return this.availableNamespaces;
     }
 
     /**
      * ̖GeBeBgpp邱Ƃ̂ł閼OԂۑ邽߂̕ϐ
      */
     private final Set<AvailableNamespaceInfo> availableNamespaces;
 
     /**
      * ̖GeBeBgpۑ邽߂̕ϐ
      */
     private final String[] name;
 
     /**
      * ς݃GeBeBgpۑ邽߂̕ϐ
      */
     private EntityUsageInfo resolvedIndo;
 
     /**
      * G[bZ[Wo͗p̃v^
      */
     private static final MessagePrinter err = new DefaultMessagePrinter(new MessageSource() {
         public String getMessageSourceName() {
             return "UnresolvedUnknownEntityUsage";
         }
     }, MESSAGE_TYPE.ERROR);
 }
