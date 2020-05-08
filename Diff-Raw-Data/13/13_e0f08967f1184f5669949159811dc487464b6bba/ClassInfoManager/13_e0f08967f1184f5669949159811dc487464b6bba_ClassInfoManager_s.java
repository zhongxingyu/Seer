 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.SortedMap;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.Settings;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.external.ExternalClassInfo;
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
      * NXǗĂCX^XԂD VOgp^[ĂD
      * 
      * @return NXǗĂCX^X
      */
     public static ClassInfoManager getInstance() {
         return SINGLETON;
     }
 
     /**
      * ΏۃNXǉ
      * 
      * @param classInfo ǉNX
      * @return NXǉꍇ true,Ȃꍇfalse
      */
     public boolean add(final TargetClassInfo classInfo) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == classInfo) {
             throw new NullPointerException();
         }
 
         // do^`FbN
         if (this.targetClassInfos.contains(classInfo)) {
             err.println(classInfo.getFullQualifiedName(".") + " is already registered!");
             return false;
         } else if (this.externalClassInfos.contains(classInfo)) {
             // ONXƏdĂꍇ̓G[o͂Ȃ
             return false;
         }
 
         this.targetClassInfos.add(classInfo);
         this.packageInfo.add(classInfo);
 
         // NXɑ΂čċAIɏ
         for (final TargetInnerClassInfo innerClassInfo : classInfo.getInnerClasses()) {
             this.add(innerClassInfo);
         }
 
         return true;
     }
 
     /**
      * ONXǉ
      * 
      * @param classInfo ǉNX
      * @return NXǉꍇ true,Ȃꍇfalse
      */
     public boolean add(final ExternalClassInfo classInfo) {
 
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == classInfo) {
             throw new NullPointerException();
         }
 
         // do^`FbN
         if (this.targetClassInfos.contains(classInfo)
                 || (this.externalClassInfos.contains(classInfo))) {
             // ONX̏ꍇ͓do^G[͏o͂Ȃ
             //err.println(classInfo.getFullQualifiedtName(".") + " is already registered!");
             return false;
         }
 
         this.externalClassInfos.add(classInfo);
         this.packageInfo.add(classInfo);
 
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
         return this.targetClassInfos.size();
     }
 
     /**
      * Ŏw肵S薼NXԂ
      * 
      * @param fullQualifiedName S薼
      * @return NX
      */
     public ClassInfo getClassInfo(final String[] fullQualifiedName) {
 
         if (null == fullQualifiedName) {
             throw new NullPointerException();
         }
 
         return this.packageInfo.getClassInfo(fullQualifiedName);
     }
 
     /**
      * Ŏw肵OԂNX Collection Ԃ
      * 
      * @param namespace O
      * @return Ŏw肵OԂNX Collection
      */
     public Collection<ClassInfo> getClassInfos(final String[] namespace) {
 
         if (null == namespace) {
             throw new NullPointerException();
         }
 
         return this.packageInfo.getClassInfos(namespace);
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
      * RXgN^D VOgp^[ŎĂ邽߂ private ĂD
      */
     private ClassInfoManager() {
         this.targetClassInfos = new TreeSet<TargetClassInfo>();
         this.externalClassInfos = new TreeSet<ExternalClassInfo>();
         this.packageInfo = new PackageInfo("DEFAULT", 0);
 
         // javȁꍇ́CÖقɃC|[gNXǉĂ
         if (Settings.getLanguage().equals(LANGUAGE.JAVA)) {
             for (int i = 0; i < ExternalClassInfo.JAVA_PREIMPORTED_CLASSES.length; i++) {
                 this.add(ExternalClassInfo.JAVA_PREIMPORTED_CLASSES[i]);
             }
         }
     }
 
     /**
      * 
      * VOgp^[邽߂̕ϐD
      */
     private static final ClassInfoManager SINGLETON = new ClassInfoManager();
 
     /**
      * 
      * ΏۃNX(TargetClassInfo)i[ϐD
      */
     private final SortedSet<TargetClassInfo> targetClassInfos;
 
     /**
      * ONXiExternalClassInfoji[ϐ
      */
     private final SortedSet<ExternalClassInfo> externalClassInfos;
 
     /**
      * NXKw\ŕۂ߂̕ϐ
      */
     private final PackageInfo packageInfo;
 
     /**
      * NXꗗKwIOԁipbP[WKwjŎf[^NX
      * 
      * @author higo
      */
     class PackageInfo {
 
         /**
          * OԂDOԖƐ[^DftHgpbP[W0D
          * 
          * @param packageName OԖ
          */
         PackageInfo(final String packageName, final int depth) {
 
             if (null == packageName) {
                 throw new NullPointerException();
             }
             if (depth < 0) {
                 throw new IllegalArgumentException("Depth must be 0 or more!");
             }
 
             this.packageName = packageName;
             this.depth = depth;
             this.classInfos = new TreeMap<String, ClassInfo>();
             this.subPackages = new TreeMap<String, PackageInfo>();
         }
 
         /**
          * Ŏw肳ꂽNXǉ
          * 
          * @param classInfo ǉNX
          */
         void add(final ClassInfo classInfo) {
 
             if (null == classInfo) {
                 throw new NullPointerException();
             }
 
             String[] packageNames = classInfo.getNamespace().getName();
 
             // ǉNX̖OԊKwC̖OԊKw[ꍇ́CYTuOԂĂяo
             if (this.getDepth() < packageNames.length) {
                 PackageInfo subPackage = this.subPackages.get(packageNames[this.getDepth()]);
                 if (null == subPackage) {
                     subPackage = new PackageInfo(packageNames[this.getDepth()], this.getDepth() + 1);
                     this.subPackages.put(subPackage.getPackageName(), subPackage);
                 }
                 subPackage.add(classInfo);
 
                 // ǉNX̖OԊKwC̖OԊKwƓꍇ́C̖OԂɃNXǉ
             } else if (this.getDepth() == packageNames.length) {
                 this.classInfos.put(classInfo.getClassName(), classInfo);
 
                 final PackageInfo innerPackage = new PackageInfo(classInfo.getClassName(), this
                         .getDepth() + 1);
                 this.subPackages.put(classInfo.getClassName(), innerPackage);
 
                 // ǉNX̖OԊKwC̖OԊKw󂢏ꍇ́CG[
             } else {
                 throw new IllegalArgumentException("Illegal class Info: " + classInfo.toString());
             }
         }
 
         /**
          * ̖OԂ̐[Ԃ
          * 
          * @return ̖OԂ̐[
          */
         int getDepth() {
             return this.depth;
         }
 
         /**
          * ̖OԖԂ
          * 
          * @return ̖OԖ
          */
         String getPackageName() {
             return this.packageName;
         }
 
         /**
          * ŗ^ꂽ Full Qualified Name NXԂ
          * 
          * @param fullQualifiedName قNX Full Qualified Name
          * @return NX
          */
         ClassInfo getClassInfo(final String[] fullQualifiedName) {
 
             if (null == fullQualifiedName) {
                 throw new NullPointerException();
             }
 
             int namespaceLength = fullQualifiedName.length - 1;
 
             // قNX̖OԊKwC̖OԊKw[ꍇ́CYTuOԂĂяo
             if (this.getDepth() < namespaceLength) {
                 final PackageInfo subPackage = this.subPackages.get(fullQualifiedName[this
                         .getDepth()]);
                 return null == subPackage ? null : subPackage.getClassInfo(fullQualifiedName);
 
                 // قNX̖OԑwC̖OԊKwƓꍇ́CNXԂ
             } else if (this.getDepth() == namespaceLength) {
                 final ClassInfo classInfo = this.classInfos.get(fullQualifiedName[namespaceLength]);
                 return classInfo;
 
                 // قNX̖OԊKwC̖OԊKw󂢏ꍇ́CG[
             } else {
                 throw new IllegalArgumentException("Illegal full qualified name: "
                         + fullQualifiedName.toString());
             }
         }
 
         /**
          * ŗ^ꂽOԂNX Collection Ԃ
          * 
          * @param namespace O
          * @return ŗ^ꂽOԂNX Collection
          */
         Collection<ClassInfo> getClassInfos(final String[] namespace) {
 
             if (null == namespace) {
                 throw new NullPointerException();
             }
 
             // قNX̖OԑwC̖Oԑw[ꍇ́CYTuOԖĂяo
             if (this.getDepth() < namespace.length) {
                 final PackageInfo subPackage = this.subPackages.get(namespace[this.getDepth()]);
                 return null == subPackage ? Collections
                         .unmodifiableCollection(new HashSet<ClassInfo>()) : subPackage
                         .getClassInfos(namespace);
 
                 // قNX̖OԑwC̖OԑwƓꍇ́CNXԂ
             } else if (this.getDepth() == namespace.length) {
 
                 return Collections.unmodifiableCollection(this.classInfos.values());
                 // قNX̖OԊKwC̖OԊKw󂢏ꍇ́CG[
             } else {
                 throw new IllegalArgumentException("Illegal namepace: " + namespace.toString());
             }
         }
 
         /**
          * ̃pbP[W̖Oۑ
          */
         private final String packageName;
 
         /**
          * ̃pbP[W̐[D ftHgpbP[W0łD
          */
         private final int depth;
 
         /**
          * ̃pbP[WɂNXꗗۑ
          */
         private final SortedMap<String, ClassInfo> classInfos;
 
         /**
          * ̃pbP[W̃TupbP[Wꗗۑ
          */
         private final SortedMap<String, PackageInfo> subPackages;
     }
 }
