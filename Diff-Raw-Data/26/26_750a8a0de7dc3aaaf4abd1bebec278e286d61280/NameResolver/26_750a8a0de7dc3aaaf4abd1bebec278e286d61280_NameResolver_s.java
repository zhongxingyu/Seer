 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ExternalClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetConstructorInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetFieldInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetInnerClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetMethodInfo;
 
 
 /**
  * ^邽߂̃[eBeBNX
  * 
  * @author higo
  * 
  */
 public final class NameResolver {
 
     /**
      * ŗ^ꂽNX̐eNXłCONX(ExternalClassInfo)ł̂ԂD NXKwIɍłʂɈʒuONXԂD
      * YNX݂Ȃꍇ́C null ԂD
      * 
      * @param classInfo ΏۃNX
      * @return ŗ^ꂽNX̐eNXłCNXKwIɍłʂɈʒuONX
      */
     public static ExternalClassInfo getExternalSuperClass(final TargetClassInfo classInfo) {
 
         if (null == classInfo) {
             throw new NullPointerException();
         }
 
         for (final ClassInfo superClassInfo : ClassTypeInfo.convert(classInfo.getSuperClasses())) {
 
             if (superClassInfo instanceof ExternalClassInfo) {
                 return (ExternalClassInfo) superClassInfo;
             }
 
             final ExternalClassInfo superSuperClassInfo = NameResolver
                     .getExternalSuperClass((TargetClassInfo) superClassInfo);
             if (null != superSuperClassInfo) {
                 return superSuperClassInfo;
             }
         }
 
         return null;
     }
 
     /**
      * ŗ^ꂽNXNXƂĎCłÓiCi[NXłȂjNXԂ
      * 
      * @param innerClass Ci[NX
      * @return łÕNX
      */
     public static TargetClassInfo getOuterstClass(final TargetInnerClassInfo innerClass) {
 
         if (null == innerClass) {
             throw new NullPointerException();
         }
 
         final TargetClassInfo outerClass = innerClass.getOuterClass();
         return outerClass instanceof TargetInnerClassInfo ? NameResolver
                 .getOuterstClass((TargetInnerClassInfo) outerClass) : outerClass;
     }
 
     /**
      * ŗ^ꂽNX̗p\ȓNX SortedSet Ԃ
      * 
      * @param classInfo NX
      * @return ŗ^ꂽNX̗p\ȓNX SortedSet
      */
     public static SortedSet<TargetInnerClassInfo> getAvailableInnerClasses(
             final TargetClassInfo classInfo) {
 
         if (null == classInfo) {
             throw new NullPointerException();
         }
 
         final SortedSet<TargetInnerClassInfo> innerClasses = new TreeSet<TargetInnerClassInfo>();
         for (final TargetInnerClassInfo innerClass : classInfo.getInnerClasses()) {
 
             innerClasses.add(innerClass);
             final SortedSet<TargetInnerClassInfo> innerClassesInInnerClass = NameResolver
                     .getAvailableInnerClasses(innerClass);
             innerClasses.addAll(innerClassesInInnerClass);
         }
 
         return Collections.unmodifiableSortedSet(innerClasses);
     }
 
     /**
      * ŗ^ꂽNXŗp\ȃNX́@List@Ԃ
      * 
      * @param classInfo NX
      * @return@ŗ^ꂽNXŗp\ȃNX́@List
      */
     public static List<ClassInfo> getAvailableClasses(final TargetClassInfo classInfo) {
 
         if (null == classInfo) {
             throw new NullPointerException();
         }
 
         // p\ȕϐ邽߂̃Xg
         final List<ClassInfo> availableClasses = new LinkedList<ClassInfo>();
 
         // łÕNX擾
         final TargetClassInfo outestClass;
         if (classInfo instanceof TargetInnerClassInfo) {
 
             outestClass = NameResolver.getOuterstClass((TargetInnerClassInfo) classInfo);
 
             // OъONX̐eNXǉ
             for (TargetClassInfo outerClass = classInfo; !outerClass.equals(outestClass); outerClass = ((TargetInnerClassInfo) outerClass)
                     .getOuterClass()) {
 
                 availableClasses.add(outerClass);
                 NameResolver.getAvailableSuperClasses(classInfo, outerClass, availableClasses);
             }
 
         } else {
             outestClass = classInfo;
         }
 
         //@łOтƂÕNX̐eNXǉ
         availableClasses.add(outestClass);
         for (final ClassInfo superClass : ClassTypeInfo.convert(outestClass.getSuperClasses())) {
             if (superClass instanceof TargetClassInfo) {
                 NameResolver.getAvailableSuperClasses(outestClass, (TargetClassInfo) superClass,
                         availableClasses);
             } else if (superClass instanceof TargetClassInfo) {
                 availableClasses.add(superClass);
             }
         }
         NameResolver.getAvailableSuperClasses(classInfo, outestClass, availableClasses);
 
         // NXǉ
         for (final TargetInnerClassInfo innerClass : classInfo.getInnerClasses()) {
             NameResolver.getAvailableInnerClasses(innerClass, availableClasses);
         }
 
         return Collections.unmodifiableList(availableClasses);
     }
 
     public static void getAvailableSuperClasses(final TargetClassInfo subClass,
             final TargetClassInfo superClass, final List<ClassInfo> availableClasses) {
 
         if ((null == subClass) || (null == superClass) || (null == availableClasses)) {
             throw new NullPointerException();
         }
 
         // Ƀ`FbNNXłꍇ͉ɏI
         if (availableClasses.contains(superClass)) {
             return;
         }
 
         // NXǉ
         // qNXƐeNX̖OԂꍇ́COԉ͌p΂悢
         if (subClass.getNamespace().equals(superClass.getNamespace())) {
 
             if (superClass.isInheritanceVisible() || superClass.isNamespaceVisible()) {
                 availableClasses.add(superClass);
                 for (final TargetClassInfo innerClass : superClass.getInnerClasses()) {
                     NameResolver.getAvailableInnerClasses(innerClass, availableClasses);
                 }
             }
 
             //qNXƐeNX̖OԂႤꍇ́Cp΂悢
         } else {
 
             if (superClass.isInheritanceVisible()) {
                 availableClasses.add(superClass);
                 for (final TargetClassInfo innerClass : superClass.getInnerClasses()) {
                     NameResolver.getAvailableInnerClasses(innerClass, availableClasses);
                 }
             }
         }
 
         // eNXǉ
         for (final ClassInfo superSuperClass : ClassTypeInfo.convert(superClass.getSuperClasses())) {
             if (superSuperClass instanceof TargetClassInfo) {
                 NameResolver.getAvailableSuperClasses(subClass, (TargetClassInfo) superSuperClass,
                         availableClasses);
             }
         }
     }
 
     public static void getAvailableInnerClasses(final TargetClassInfo classInfo,
             final List<ClassInfo> availableClasses) {
 
         if ((null == classInfo) || (null == availableClasses)) {
             throw new NullPointerException();
         }
 
         // Ƀ`FbNNXłꍇ͉ɏI
         if (availableClasses.contains(classInfo)) {
             return;
         }
 
         availableClasses.add(classInfo);
 
         // NXǉ
         for (final TargetInnerClassInfo innerClass : classInfo.getInnerClasses()) {
             NameResolver.getAvailableInnerClasses(innerClass, availableClasses);
         }
 
         return;
     }
 
     /**
      * ũ݂NXvŗp\ȃtB[hꗗԂD
      * ŁCup\ȃtB[hvƂ́Cũ݂NXvŒ`ĂtB[hCũ݂NXṽCi[NXŒ`ĂtB[hC
      * yт̐eNXŒ`ĂtB[ĥqNXANZX\ȃtB[hłD p\ȃtB[h List Ɋi[ĂD
      * Xg̐擪D揇ʂ̍tB[hi܂C NXKwɂĉʂ̃NXɒ`ĂtB[hji[ĂD
      * 
      * @param currentClass ݂̃NX
      * @return p\ȃtB[hꗗ
      */
     public static List<TargetFieldInfo> getAvailableFields(final TargetClassInfo currentClass) {
 
         if (null == currentClass) {
             throw new NullPointerException();
         }
 
         // `FbNNX邽߂̃LbVCLbVɂNX͓xڂ̓tB[h擾Ȃi[v\΍j
         final Set<TargetClassInfo> checkedClasses = new HashSet<TargetClassInfo>();
 
         // p\ȕϐ邽߂̃Xg
         final List<TargetFieldInfo> availableFields = new LinkedList<TargetFieldInfo>();
 
         // łÕNX擾
         final TargetClassInfo outestClass;
         if (currentClass instanceof TargetInnerClassInfo) {
             outestClass = NameResolver.getOuterstClass((TargetInnerClassInfo) currentClass);
 
             for (TargetClassInfo outerClass = currentClass; !outerClass.equals(outestClass); outerClass = ((TargetInnerClassInfo) outerClass)
                     .getOuterClass()) {
 
                 // NXсCONXŒ`ꂽ\bhǉ
                 availableFields.addAll(outerClass.getDefinedFields());
                 checkedClasses.add(outerClass);
             }
 
             // NXŒ`ꂽtB[hǉ
             for (final TargetInnerClassInfo innerClass : currentClass.getInnerClasses()) {
                 final List<TargetFieldInfo> availableFieldsDefinedInInnerClasses = NameResolver
                         .getAvailableFieldsDefinedInInnerClasses(innerClass, checkedClasses);
                 availableFields.addAll(availableFieldsDefinedInInnerClasses);
             }
 
             // eNXŒ`ꂽtB[hǉ
             for (final ClassInfo superClass : ClassTypeInfo.convert(currentClass.getSuperClasses())) {
                 if (superClass instanceof TargetClassInfo) {
                     final List<TargetFieldInfo> availableFieldsDefinedInSuperClasses = NameResolver
                             .getAvailableFieldsDefinedInSuperClasses(currentClass,
                                     (TargetClassInfo) superClass, checkedClasses);
                     availableFields.addAll(availableFieldsDefinedInSuperClasses);
                 }
             }
 
         } else {
             outestClass = currentClass;
         }
 
         // łÕNXŒ`ꂽtB[hǉ
         availableFields.addAll(outestClass.getDefinedFields());
         checkedClasses.add(outestClass);
 
         // NXŒ`ꂽtB[hǉ
         for (final TargetInnerClassInfo innerClass : outestClass.getInnerClasses()) {
             final List<TargetFieldInfo> availableFieldsDefinedInInnerClasses = NameResolver
                     .getAvailableFieldsDefinedInInnerClasses(innerClass, checkedClasses);
             availableFields.addAll(availableFieldsDefinedInInnerClasses);
         }
 
         // eNXŒ`ꂽtB[hǉ
         for (final ClassInfo superClass : ClassTypeInfo.convert(outestClass.getSuperClasses())) {
             if (superClass instanceof TargetClassInfo) {
                 final List<TargetFieldInfo> availableFieldsDefinedInSuperClasses = NameResolver
                         .getAvailableFieldsDefinedInSuperClasses(outestClass,
                                 (TargetClassInfo) superClass, checkedClasses);
                 availableFields.addAll(availableFieldsDefinedInSuperClasses);
             }
         }
 
         return Collections.unmodifiableList(availableFields);
     }
 
     /**
      * ŗ^ꂽNXƂ̓NXŒ`ꂽtB[ĥCÕNXŗp\ȃtB[h List Ԃ
      * 
      * @param classInfo NX
      * @param checkedClasses Ƀ`FbNNX̃LbV
      * @return ÕNXŗp\ȃtB[h List
      */
     public static List<TargetFieldInfo> getAvailableFieldsDefinedInInnerClasses(
             final TargetInnerClassInfo classInfo, final Set<TargetClassInfo> checkedClasses) {
 
         if ((null == classInfo) || (null == checkedClasses)) {
             throw new NullPointerException();
         }
 
         // Ƀ`FbNNXłꍇ͉ɏI
         if (checkedClasses.contains(classInfo)) {
             return new LinkedList<TargetFieldInfo>();
         }
 
         final List<TargetFieldInfo> availableFields = new LinkedList<TargetFieldInfo>();
 
         // NXŒ`ĂCOԉtB[hǉ
         // for (final TargetFieldInfo definedField : classInfo.getDefinedFields()) {
         // if (definedField.isNamespaceVisible()) {
         // availableFields.add(definedField);
         // }
         // }
         availableFields.addAll(classInfo.getDefinedFields());
         checkedClasses.add(classInfo);
 
         // NXŒ`ꂽtB[hǉ
         for (final TargetInnerClassInfo innerClass : classInfo.getInnerClasses()) {
             final List<TargetFieldInfo> availableFieldsDefinedInInnerClasses = NameResolver
                     .getAvailableFieldsDefinedInInnerClasses(innerClass, checkedClasses);
             availableFields.addAll(availableFieldsDefinedInInnerClasses);
         }
 
         // eNXŒ`ꂽtB[hǉ
         for (final ClassInfo superClass : ClassTypeInfo.convert(classInfo.getSuperClasses())) {
             if (superClass instanceof TargetClassInfo) {
                 final List<TargetFieldInfo> availableFieldsDefinedInSuperClasses = NameResolver
                         .getAvailableFieldsDefinedInSuperClasses(classInfo,
                                 (TargetClassInfo) superClass, checkedClasses);
                 availableFields.addAll(availableFieldsDefinedInSuperClasses);
             }
         }
 
         return Collections.unmodifiableList(availableFields);
     }
 
     /**
      * ŗ^ꂽNXƂ̐eNXŒ`ꂽtB[ĥCqNXŗp\ȃtB[h List Ԃ
      * 
      * @param classInfo NX
      * @param checkedClasses Ƀ`FbNNX̃LbV
      * @return qNXŗp\ȃtB[h List
      */
     private static List<TargetFieldInfo> getAvailableFieldsDefinedInSuperClasses(
             final TargetClassInfo subClass, final TargetClassInfo superClass,
             final Set<TargetClassInfo> checkedClasses) {
 
         if ((null == subClass) || (null == superClass) || (null == checkedClasses)) {
             throw new NullPointerException();
         }
 
         // Ƀ`FbNNXłꍇ͉ɏI
         if (checkedClasses.contains(superClass)) {
             return new LinkedList<TargetFieldInfo>();
         }
 
         final List<TargetFieldInfo> availableFields = new LinkedList<TargetFieldInfo>();
 
         // NXŒ`ĂCNXKwtB[hǉ
         for (final TargetFieldInfo definedField : superClass.getDefinedFields()) {
 
             // qNXƐeNX̖OԂꍇ́COԉ͌p΂悢
             if (subClass.getNamespace().equals(superClass.getNamespace())) {
 
                 if (definedField.isInheritanceVisible() || definedField.isNamespaceVisible()) {
                     availableFields.add(definedField);
                 }
 
                 //qNXƐeNX̖OԂႤꍇ́Cp΂悢
             } else {
                 if (definedField.isInheritanceVisible()) {
                     availableFields.add(definedField);
                 }
             }
         }
         checkedClasses.add(superClass);
 
         // NXŒ`ꂽtB[hǉ
         for (final TargetInnerClassInfo innerClass : superClass.getInnerClasses()) {
             final List<TargetFieldInfo> availableFieldsDefinedInInnerClasses = NameResolver
                     .getAvailableFieldsDefinedInInnerClasses(innerClass, checkedClasses);
             for (final TargetFieldInfo field : availableFieldsDefinedInInnerClasses) {
 
                 // qNXƐeNX̖OԂꍇ́COԉ͌p΂悢
                 if (subClass.getNamespace().equals(superClass.getNamespace())) {
 
                     if (field.isInheritanceVisible() || field.isNamespaceVisible()) {
                         availableFields.add(field);
                     }
 
                 } else {
 
                     //qNXƐeNX̖OԂႤꍇ́Cp΂悢
                     if (field.isInheritanceVisible()) {
                         availableFields.add(field);
                     }
                 }
             }
         }
 
         // eNXŒ`ꂽtB[hǉ
         for (final ClassInfo superSuperClass : ClassTypeInfo.convert(superClass.getSuperClasses())) {
             if (superSuperClass instanceof TargetClassInfo) {
                 final List<TargetFieldInfo> availableFieldsDefinedInSuperClasses = NameResolver
                         .getAvailableFieldsDefinedInSuperClasses(subClass,
                                 (TargetClassInfo) superSuperClass, checkedClasses);
                 availableFields.addAll(availableFieldsDefinedInSuperClasses);
             }
         }
 
         return Collections.unmodifiableList(availableFields);
     }
 
     /**
      * ũ݂NXvŗp\ȃ\bhꗗԂD
      * ŁCup\ȃ\bhvƂ́Cũ݂NXvŒ`Ă郁\bhCyт̐eNXŒ`Ă郁\bĥqNXANZX\ȃ\bhłD
      * p\ȃ\bh List Ɋi[ĂD Xg̐擪D揇ʂ̍\bhi܂CNXKwɂĉʂ̃NXɒ`Ă郁\bhji[ĂD
      * 
      * @param thisClass ݂̃NX
      * @return p\ȃ\bhꗗ
      */
     private static List<TargetMethodInfo> getAvailableMethods(final TargetClassInfo currentClass) {
 
         if (null == currentClass) {
             throw new NullPointerException();
         }
 
         // `FbNNX邽߂̃LbVCLbVɂNX͓xڂ̓tB[h擾Ȃi[v\΍j
         final Set<TargetClassInfo> checkedClasses = new HashSet<TargetClassInfo>();
 
         // p\ȕϐ邽߂̃Xg
         final List<TargetMethodInfo> availableMethods = new LinkedList<TargetMethodInfo>();
 
         // łÕNX擾
         final TargetClassInfo outestClass;
         if (currentClass instanceof TargetInnerClassInfo) {
             outestClass = NameResolver.getOuterstClass((TargetInnerClassInfo) currentClass);
 
             // NXŒ`ꂽ\bhǉ
             availableMethods.addAll(currentClass.getDefinedMethods());
             checkedClasses.add(currentClass);
 
             // NXŒ`ꂽ\bhǉ
             for (final TargetInnerClassInfo innerClass : currentClass.getInnerClasses()) {
                 final List<TargetMethodInfo> availableMethodsDefinedInInnerClasses = NameResolver
                         .getAvailableMethodsDefinedInInnerClasses(innerClass, checkedClasses);
                 availableMethods.addAll(availableMethodsDefinedInInnerClasses);
             }
 
             // eNXŒ`ꂽ\bhǉ
             for (final ClassInfo superClass : ClassTypeInfo.convert(currentClass.getSuperClasses())) {
                 if (superClass instanceof TargetClassInfo) {
                     final List<TargetMethodInfo> availableMethodsDefinedInSuperClasses = NameResolver
                             .getAvailableMethodsDefinedInSuperClasses(outestClass,
                                     (TargetClassInfo) superClass, checkedClasses);
                     availableMethods.addAll(availableMethodsDefinedInSuperClasses);
                 }
             }
 
         } else {
             outestClass = currentClass;
         }
 
         // łÕNXŒ`ꂽ\bhǉ
         availableMethods.addAll(outestClass.getDefinedMethods());
         checkedClasses.add(outestClass);
 
         // NXŒ`ꂽ\bhǉ
         for (final TargetInnerClassInfo innerClass : outestClass.getInnerClasses()) {
             final List<TargetMethodInfo> availableMethodsDefinedInInnerClasses = NameResolver
                     .getAvailableMethodsDefinedInInnerClasses(innerClass, checkedClasses);
             availableMethods.addAll(availableMethodsDefinedInInnerClasses);
         }
 
         // eNXŒ`ꂽ\bhǉ
         for (final ClassInfo superClass : ClassTypeInfo.convert(outestClass.getSuperClasses())) {
             if (superClass instanceof TargetClassInfo) {
                 final List<TargetMethodInfo> availableMethodsDefinedInSuperClasses = NameResolver
                         .getAvailableMethodsDefinedInSuperClasses(outestClass,
                                 (TargetClassInfo) superClass, checkedClasses);
                 availableMethods.addAll(availableMethodsDefinedInSuperClasses);
             }
         }
 
         return Collections.unmodifiableList(availableMethods);
     }
 
     /**
      * ŗ^ꂽNXƂ̓NXŒ`ꂽ\bĥCÕNXŗp\ȃ\bh List Ԃ
      * 
      * @param classInfo NX
      * @param checkedClasses Ƀ`FbNNX̃LbV
      * @return ÕNXŗp\ȃ\bh List
      */
     private static List<TargetMethodInfo> getAvailableMethodsDefinedInInnerClasses(
             final TargetInnerClassInfo classInfo, final Set<TargetClassInfo> checkedClasses) {
 
         if ((null == classInfo) || (null == checkedClasses)) {
             throw new NullPointerException();
         }
 
         // Ƀ`FbNNXłꍇ͉ɏI
         if (checkedClasses.contains(classInfo)) {
             return new LinkedList<TargetMethodInfo>();
         }
 
         final List<TargetMethodInfo> availableMethods = new LinkedList<TargetMethodInfo>();
 
        // NXŒ`ĂCOԉ\bhǉ
        // for (final TargetFieldInfo definedField : classInfo.getDefinedFields()) {
        // if (definedField.isNamespaceVisible()) {
        // availableFields.add(definedField);
        // }
        // }
         availableMethods.addAll(classInfo.getDefinedMethods());
         checkedClasses.add(classInfo);
 
         // NXŒ`ꂽ\bhǉ
         for (final TargetInnerClassInfo innerClass : classInfo.getInnerClasses()) {
             final List<TargetMethodInfo> availableMethodsDefinedInInnerClasses = NameResolver
                     .getAvailableMethodsDefinedInInnerClasses(innerClass, checkedClasses);
             availableMethods.addAll(availableMethodsDefinedInInnerClasses);
         }
 
         // eNXŒ`ꂽ\bhǉ
         for (final ClassInfo superClass : ClassTypeInfo.convert(classInfo.getSuperClasses())) {
             if (superClass instanceof TargetClassInfo) {
                 final List<TargetMethodInfo> availableMethodsDefinedInSuperClasses = NameResolver
                         .getAvailableMethodsDefinedInSuperClasses(classInfo,
                                 (TargetClassInfo) superClass, checkedClasses);
                 availableMethods.addAll(availableMethodsDefinedInSuperClasses);
             }
         }
 
         return Collections.unmodifiableList(availableMethods);
     }
 
     /**
      * ŗ^ꂽNXƂ̐eNXŒ`ꂽ\bĥCqNXŗp\ȃ\bh List Ԃ
      * 
      * @param classInfo NX
      * @param checkedClasses Ƀ`FbNNX̃LbV
      * @return qNXŗp\ȃ\bh List
      */
     private static List<TargetMethodInfo> getAvailableMethodsDefinedInSuperClasses(
             final TargetClassInfo subClass, final TargetClassInfo superClass,
             final Set<TargetClassInfo> checkedClasses) {
 
         if ((null == subClass) || (null == superClass) || (null == checkedClasses)) {
             throw new NullPointerException();
         }
 
         // Ƀ`FbNNXłꍇ͉ɏI
         if (checkedClasses.contains(superClass)) {
             return new LinkedList<TargetMethodInfo>();
         }
 
         final List<TargetMethodInfo> availableMethods = new LinkedList<TargetMethodInfo>();
 
         // NXŒ`ĂCNXKw\bhǉ
         for (final TargetMethodInfo definedMethod : superClass.getDefinedMethods()) {
 
             // qNXƐeNX̖OԂꍇ́COԉ͌p΂悢
             if (subClass.getNamespace().equals(superClass.getNamespace())) {
 
                 if (definedMethod.isInheritanceVisible() || definedMethod.isNamespaceVisible()) {
                     availableMethods.add(definedMethod);
                 }
 
                 // qNXƐeNX̖OԂقȂꍇ́Cp΂悢
             } else {
 
                 if (definedMethod.isInheritanceVisible()) {
                     availableMethods.add(definedMethod);
                 }
             }
         }
         checkedClasses.add(superClass);
 
         // NXŒ`ꂽ\bhǉ
         for (final TargetInnerClassInfo innerClass : superClass.getInnerClasses()) {
             final List<TargetMethodInfo> availableMethodsDefinedInInnerClasses = NameResolver
                     .getAvailableMethodsDefinedInInnerClasses(innerClass, checkedClasses);
             for (final TargetMethodInfo method : availableMethodsDefinedInInnerClasses) {
 
                 // qNXƐeNX̖OԂꍇ́COԉ͌p΂悢
                 if (subClass.getNamespace().equals(superClass.getNamespace())) {
 
                     if (method.isInheritanceVisible() || method.isNamespaceVisible()) {
                         availableMethods.add(method);
                     }
 
                     // qNXƐeNX̖OԂقȂꍇ́Cp΂悢
                 } else {
 
                     if (method.isInheritanceVisible()) {
                         availableMethods.add(method);
                     }
                 }
             }
         }
 
         // eNXŒ`ꂽ\bhǉ
         for (final ClassInfo superSuperClass : ClassTypeInfo.convert(superClass.getSuperClasses())) {
             if (superSuperClass instanceof TargetClassInfo) {
                 final List<TargetMethodInfo> availableMethodsDefinedInSuperClasses = NameResolver
                         .getAvailableMethodsDefinedInSuperClasses(subClass,
                                 (TargetClassInfo) superSuperClass, checkedClasses);
                 availableMethods.addAll(availableMethodsDefinedInSuperClasses);
             }
         }
 
         return Collections.unmodifiableList(availableMethods);
     }
 
     /**
      * ugpNXvugpNXvɂĎgpꍇɁCp\ȃtB[hꗗԂD
      * ŁCup\ȃtB[hvƂ́CugpNXvŒ`ĂtB[hCyт̐eNXŒ`ĂtB[ĥqNXANZX\ȃtB[hłD
      * ܂CugpNXvƁugpNXv̖OԂrC萳mɗp\ȃtB[h擾D qNXŗp\ȃtB[hꗗ List Ɋi[ĂD
      * Xg̐擪D揇ʂ̍tB[hi܂CNXKwɂĉʂ̃NXɒ`ĂtB[hji[ĂD
      * 
      * @param usedClass gpNX
      * @param usingClass gpNX
      * @return p\ȃtB[hꗗ
      */
     public static List<TargetFieldInfo> getAvailableFields(final TargetClassInfo usedClass,
             final TargetClassInfo usingClass) {
 
         if ((null == usedClass) || (null == usingClass)) {
             throw new NullPointerException();
         }
 
         // gpNX̍łÕNX擾
         final TargetClassInfo usedOutestClass;
         if (usedClass instanceof TargetInnerClassInfo) {
             usedOutestClass = NameResolver.getOuterstClass((TargetInnerClassInfo) usedClass);
         } else {
             usedOutestClass = usedClass;
         }
 
         // gpNX̍łÕNX擾
         final TargetClassInfo usingOutestClass;
         if (usingClass instanceof TargetInnerClassInfo) {
             usingOutestClass = NameResolver.getOuterstClass((TargetInnerClassInfo) usingClass);
         } else {
             usingOutestClass = usingClass;
         }
 
         // ̃NXŒ`ĂtB[ĥCgpNXŗp\ȃtB[h擾
         // 2̃NXꍇCSẴtB[hp\
         if (usedOutestClass.equals(usingOutestClass)) {
 
             return NameResolver.getAvailableFields(usedClass);
 
             // 2̃NXOԂĂꍇ
         } else if (usedOutestClass.getNamespace().equals(usingOutestClass.getNamespace())) {
 
             final List<TargetFieldInfo> availableFields = new LinkedList<TargetFieldInfo>();
 
             // OԉtB[ĥ݂p\
             for (final TargetFieldInfo field : NameResolver.getAvailableFields(usedClass)) {
                 if (field.isNamespaceVisible()) {
                     availableFields.add(field);
                 }
             }
 
             return Collections.unmodifiableList(availableFields);
 
             // ႤOԂĂꍇ
         } else {
 
             final List<TargetFieldInfo> availableFields = new LinkedList<TargetFieldInfo>();
 
             // StB[ĥ݂p\
             for (final TargetFieldInfo field : NameResolver.getAvailableFields(usedClass)) {
                 if (field.isPublicVisible()) {
                     availableFields.add(field);
                 }
             }
 
             return Collections.unmodifiableList(availableFields);
         }
     }
 
     /**
      * ugpNXvugpNXvɂĎgpꍇɁCp\ȃ\bhꗗԂD
      * ŁCup\ȃ\bhvƂ́CugpNXvŒ`Ă郁\bhCyт̐eNXŒ`Ă郁\bĥqNXANZX\ȃ\bhłD
      * ܂CugpNXvƁugpNXv̖OԂrC萳mɗp\ȃ\bh擾D qNXŗp\ȃ\bhꗗ List Ɋi[ĂD
      * Xg̐擪D揇ʂ̍\bhi܂CNXKwɂĉʂ̃NXɒ`Ă郁\bhji[ĂD
      * 
      * @param usedClass gpNX
      * @param usingClass gpNX
      * @return p\ȃ\bhꗗ
      */
     public static List<TargetMethodInfo> getAvailableMethods(final TargetClassInfo usedClass,
             final TargetClassInfo usingClass) {
 
         if ((null == usedClass) || (null == usingClass)) {
             throw new NullPointerException();
         }
 
         // gpNX̍łÕNX擾
         final TargetClassInfo usedOutestClass;
         if (usedClass instanceof TargetInnerClassInfo) {
             usedOutestClass = NameResolver.getOuterstClass((TargetInnerClassInfo) usedClass);
         } else {
             usedOutestClass = usedClass;
         }
 
         // gpNX̍łÕNX擾
         final TargetClassInfo usingOutestClass;
         if (usingClass instanceof TargetInnerClassInfo) {
             usingOutestClass = NameResolver.getOuterstClass((TargetInnerClassInfo) usingClass);
         } else {
             usingOutestClass = usingClass;
         }
 
         // ̃NXŒ`Ă郁\bĥCgpNXŗp\ȃ\bh擾
         // 2̃NXꍇCSẴ\bhp\
         if (usedOutestClass.equals(usingOutestClass)) {
 
             return NameResolver.getAvailableMethods(usedClass);
 
             // 2̃NXOԂĂꍇ
         } else if (usedOutestClass.getNamespace().equals(usingOutestClass.getNamespace())) {
 
             final List<TargetMethodInfo> availableMethods = new LinkedList<TargetMethodInfo>();
 
             // Oԉ\bĥ݂p\
             for (final TargetMethodInfo method : NameResolver.getAvailableMethods(usedClass)) {
                 if (method.isNamespaceVisible()) {
                     availableMethods.add(method);
                 }
             }
 
             return Collections.unmodifiableList(availableMethods);
 
             // ႤOԂĂꍇ
         } else {
 
             final List<TargetMethodInfo> availableMethods = new LinkedList<TargetMethodInfo>();
 
             // S\bĥ݂p\
             for (final TargetMethodInfo method : NameResolver.getAvailableMethods(usedClass)) {
                 if (method.isPublicVisible()) {
                     availableMethods.add(method);
                 }
             }
 
             return Collections.unmodifiableList(availableMethods);
         }
     }
 
     /**
      * ŗ^ꂽNX^ŌĂяo\ȃRXgN^ListԂ
      * 
      * @param classType
      * @return
      */
     public static final List<TargetConstructorInfo> getAvailableConstructors(
             final ClassTypeInfo classType) {
 
         final List<TargetConstructorInfo> constructors = new LinkedList<TargetConstructorInfo>();
         final ClassInfo classInfo = classType.getReferencedClass();
 
         if (classInfo instanceof TargetClassInfo) {
             final TargetClassInfo targetClassInfo = (TargetClassInfo) classInfo;
             constructors.addAll(targetClassInfo.getDefinedConstructors());
 
             for (final ClassTypeInfo superClassType : targetClassInfo.getSuperClasses()) {
                 final List<TargetConstructorInfo> superConstructors = NameResolver
                         .getAvailableConstructors(superClassType);
                 constructors.addAll(superConstructors);
             }
         }
 
         return constructors;
     }
 
     /**
      * ŗ^ꂽNX̒ڂ̃Ci[NXԂDeNXŒ`ꂽCi[NX܂܂D
      * 
      * @param classInfo NX
      * @return ŗ^ꂽNX̒ڂ̃Ci[NXCeNXŒ`ꂽCi[NX܂܂D
      */
     public static final SortedSet<TargetInnerClassInfo> getAvailableDirectInnerClasses(
             final TargetClassInfo classInfo) {
 
         if (null == classInfo) {
             throw new NullPointerException();
         }
 
         final SortedSet<TargetInnerClassInfo> availableDirectInnerClasses = new TreeSet<TargetInnerClassInfo>();
 
         // ŗ^ꂽNX̒ڂ̃Ci[NXǉ
         availableDirectInnerClasses.addAll(classInfo.getInnerClasses());
 
         // eNXɑ΂čċAIɏ
         for (final ClassInfo superClassInfo : ClassTypeInfo.convert(classInfo.getSuperClasses())) {
 
             if (superClassInfo instanceof TargetClassInfo) {
                 final SortedSet<TargetInnerClassInfo> availableDirectInnerClassesInSuperClass = NameResolver
                         .getAvailableDirectInnerClasses((TargetClassInfo) superClassInfo);
                 availableDirectInnerClasses.addAll(availableDirectInnerClassesInSuperClass);
             }
         }
 
         return Collections.unmodifiableSortedSet(availableDirectInnerClasses);
     }
 
 }
