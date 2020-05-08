 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.Settings;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.DefaultMessagePrinter;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessagePrinter;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessageSource;
 import jp.ac.osaka_u.ist.sel.metricstool.main.io.MessagePrinter.MESSAGE_TYPE;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.util.LANGUAGE;
 
 
 /**
  * NXǗNXD
  * 
  * @author higo
  * 
  */
 public final class ClassInfoManager {
 
     /**
      * ΏۃNXǉ
      * 
      * @param classInfo ǉNX
      * @return NXǉꍇ true,Ȃꍇfalse
      */
     public boolean add(final ClassInfo classInfo) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == classInfo) {
             throw new IllegalArgumentException();
         }
 
         // do^`FbN
         if (this.targetClassInfos.contains(classInfo)) {
             err.println(classInfo.getFullQualifiedName(".") + " is already registered!");
             return false;
         } else if (this.externalClassInfos.contains(classInfo)) {
             // ONXƏdĂꍇ̓G[o͂Ȃ
             return false;
         }
 
         // NXꗗ̃Zbgɓo^
         if (classInfo instanceof TargetClassInfo) {
             this.targetClassInfos.add((TargetClassInfo) classInfo);
         } else if (classInfo instanceof ExternalClassInfo) {
             this.externalClassInfos.add((ExternalClassInfo) classInfo);
         } else {
             assert false : "Here shouldn't be reached!";
         }
 
         // NXNXIuWFNg𓾂邽߂̃}bvɒǉ
         {
             final String name = classInfo.getClassName();
             SortedSet<ClassInfo> classInfos = this.classNameMap.get(name);
             if (null == classInfos) {
                 classInfos = new TreeSet<ClassInfo>();
                 this.classNameMap.put(name, classInfos);
             }
             classInfos.add(classInfo);
         }
 
         //@OԂNXIuWFNg𓾂邽߂̃}bvɒǉ
         {
             final NamespaceInfo namespace = classInfo.getNamespace();
             SortedSet<ClassInfo> classInfos = this.namespaceMap.get(namespace);
             if (null == classInfos) {
                 classInfos = new TreeSet<ClassInfo>();
                 this.namespaceMap.put(namespace, classInfos);
             }
             classInfos.add(classInfo);
         }
 
         return true;
     }
 
     /**
      * ΏۃNXSortedSetԂ
      * 
      * @return ΏۃNXSortedSet
      */
     public SortedSet<TargetClassInfo> getTargetClassInfos() {
         return Collections.unmodifiableSortedSet(this.targetClassInfos);
     }
 
     /**
      * ONXSortedSetԂ
      * 
      * @return ONXSortedSet
      */
     public SortedSet<ExternalClassInfo> getExternalClassInfos() {
         return Collections.unmodifiableSortedSet(this.externalClassInfos);
     }
 
     /**
      * ΏۃNX̐Ԃ
      * 
      * @return ΏۃNX̐
      */
     public int getTargetClassCount() {
         return this.targetClassInfos.size();
     }
 
     /**
      * ONX̐Ԃ
      * 
      * @return ONX̐
      */
     public int getExternalClassCount() {
        return this.externalClassInfos.size();
     }
 
     /**
      * Ŏw肵S薼NXԂ.
      * w肳ꂽS薼NX݂ȂƂnullԂ
      * 
      * @param fullQualifiedName S薼
      * @return NX
      */
     public ClassInfo getClassInfo(final String[] fullQualifiedName) {
 
         if ((null == fullQualifiedName) || (0 == fullQualifiedName.length)) {
             throw new IllegalArgumentException();
         }
 
         final int namespaceLength = fullQualifiedName.length - 1;
         final String[] namespace = Arrays.<String> copyOf(fullQualifiedName,
                 fullQualifiedName.length - 1);
         final String className = fullQualifiedName[namespaceLength];
 
         // NXNXꗗ擾
         final SortedSet<ClassInfo> classInfos = this.classNameMap.get(className);
         if (null != classInfos) {
             // OԂNXԂ
             for (final ClassInfo classInfo : classInfos) {
                 if (classInfo.getNamespace().equals(namespace)) {
                     return classInfo;
                 }
             }
         }
         return null;
     }
 
     /**
      * Ŏw肵S薼NX邩肷
      * 
      * @param fullQualifiedName NX̊S薼
      * @return NXꍇtrue, Ȃꍇfalse
      */
     public boolean hasClassInfo(final String[] fullQualifiedName) {
 
         if ((null == fullQualifiedName) || (0 == fullQualifiedName.length)) {
             throw new IllegalArgumentException();
         }
 
         final int namespaceLength = fullQualifiedName.length - 1;
         final String[] namespace = Arrays.<String> copyOf(fullQualifiedName,
                 fullQualifiedName.length - 1);
         final String className = fullQualifiedName[namespaceLength];
 
         //NXNXꗗ擾
         final SortedSet<ClassInfo> classInfos = this.classNameMap.get(className);
         if (null != classInfos) {
 
             // OԂNX΁CtrueԂ
             for (final ClassInfo classInfo : classInfos) {
                 if (classInfo.getNamespace().equals(namespace)) {
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     /**
      * Ŏw肵OԂNX Collection Ԃ
      * 
      * @param namespace O
      * @return Ŏw肵OԂNX Collection
      */
     public Collection<ClassInfo> getClassInfos(final String[] namespace) {
 
         if (null == namespace) {
             throw new IllegalArgumentException();
         }
 
         return this.getClassInfos(new NamespaceInfo(namespace));
     }
 
     /**
      * Ŏw肵OԂNX Collection Ԃ
      * 
      * @param namespace O
      * @return Ŏw肵OԂNX Collection
      */
     public Collection<ClassInfo> getClassInfos(final NamespaceInfo namespace) {
 
         if (null == namespace) {
             throw new IllegalArgumentException();
         }
 
         final SortedSet<ClassInfo> classInfos = this.namespaceMap.get(namespace);
         return null != classInfos ? Collections.unmodifiableSortedSet(classInfos) : Collections
                 .unmodifiableSortedSet(new TreeSet<ClassInfo>());
     }
 
     /**
      * Ŏw肵NXNX Collection Ԃ
      * 
      * @param className NX
      * @return Ŏw肵NXNX Collection
      */
     public Collection<ClassInfo> getClassInfos(final String className) {
 
         if (null == className) {
             throw new IllegalArgumentException();
         }
 
         final SortedSet<ClassInfo> classInfos = this.classNameMap.get(className);
         return null != classInfos ? Collections.unmodifiableSortedSet(classInfos) : Collections
                 .unmodifiableSortedSet(new TreeSet<ClassInfo>());
     }
 
     /**
      * G[bZ[Wo͗p̃v^
      */
     private static final MessagePrinter err = new DefaultMessagePrinter(new MessageSource() {
         public String getMessageSourceName() {
             return "main";
         }
     }, MESSAGE_TYPE.ERROR);
 
     /**
      * 
      * RXgN^D 
      */
     public ClassInfoManager() {
 
         this.classNameMap = new HashMap<String, SortedSet<ClassInfo>>();
         this.namespaceMap = new HashMap<NamespaceInfo, SortedSet<ClassInfo>>();
 
         this.targetClassInfos = new TreeSet<TargetClassInfo>();
         this.externalClassInfos = new TreeSet<ExternalClassInfo>();
 
         // javȁꍇ́CÖقɃC|[gNXǉĂ
         final Settings settings = Settings.getInstance();
         if (settings.getLanguage().equals(LANGUAGE.JAVA15)
                 || settings.getLanguage().equals(LANGUAGE.JAVA14)
                 || settings.getLanguage().equals(LANGUAGE.JAVA13)) {
             for (int i = 0; i < ExternalClassInfo.JAVA_PREIMPORTED_CLASSES.length; i++) {
                 this.add(ExternalClassInfo.JAVA_PREIMPORTED_CLASSES[i]);
             }
         }
     }
 
     /**
      * NXCNXIuWFNg𓾂邽߂̃}bv
      */
     private final Map<String, SortedSet<ClassInfo>> classNameMap;
 
     /**
      * OԖCNXIuWFNg𓾂邽߂̃}bv
      */
     private final Map<NamespaceInfo, SortedSet<ClassInfo>> namespaceMap;
 
     /**
      * ΏۃNXꗗۑ邽߂̃Zbg
      */
     private final SortedSet<TargetClassInfo> targetClassInfos;
 
     /**
      * ONXꗗۑ邽߂̃Zbg
      */
     private final SortedSet<ExternalClassInfo> externalClassInfos;
 }
