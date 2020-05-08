 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import java.util.Collection;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.DataManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.CallableUnitInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ExternalClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ImportStatementInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * ASTp[X̍ہCQƌ^ϐ̗p\ȖOԖC܂͊S薼\NX
  * 
  * @author higo
  * 
  */
 public final class UnresolvedImportStatementInfo extends UnresolvedUnitInfo<ImportStatementInfo> {
 
     /**
      * p\OԖƂȉ̃NXSẴNXp\ǂ\boolean^ăIuWFNg.
      * <p>
      * import aaa.bbb.ccc.DDDG // new AvailableNamespace({"aaa","bbb","ccc","DDD"}, false); <br>
      * import aaa.bbb.ccc.*; // new AvailableNamespace({"aaa","bbb","ccc"},true); <br>
      * </p>
      * 
      * @param namespace p\OԖ
      * @param allClasses SẴNXp\ǂ
      */
     public UnresolvedImportStatementInfo(final String[] namespace, final boolean allClasses) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == namespace) {
             throw new NullPointerException();
         }
 
         this.importName = namespace;
         this.allClasses = allClasses;
     }
 
     /**
      * ΏۃIuWFNgƓǂԂ
      * 
      * @param o ΏۃIuWFNg
      * @return ꍇ trueCłȂꍇ false
      */
     @Override
     public boolean equals(Object o) {
 
         if (null == o) {
             throw new NullPointerException();
         }
 
         if (!(o instanceof UnresolvedImportStatementInfo)) {
             return false;
         }
 
         String[] importName = this.getImportName();
         String[] correspondImportName = ((UnresolvedImportStatementInfo) o).getImportName();
         if (importName.length != correspondImportName.length) {
             return false;
         }
 
         for (int i = 0; i < importName.length; i++) {
             if (!importName[i].equals(correspondImportName[i])) {
                 return false;
             }
         }
 
         return true;
     }
 
     @Override
     public ImportStatementInfo resolve(final TargetClassInfo usingClass,
             final CallableUnitInfo usingMethod, final ClassInfoManager classInfoManager,
             final FieldInfoManager fieldInfoManager, final MethodInfoManager methodInfoManager) {
 
         // sȌĂяołȂ`FbN
         MetricsToolSecurityManager.getInstance().checkAccess();
         if (null == classInfoManager) {
             throw new NullPointerException();
         }
 
         // ɉς݂łꍇ́CLbVԂ
         if (this.alreadyResolved()) {
             return this.getResolved();
         }
 
         final int fromLine = this.getFromLine();
         final int fromColumn = this.getFromColumn();
         final int toLine = this.getToLine();
         final int toColumn = this.getToColumn();
 
         final SortedSet<ClassInfo> accessibleClasses = new TreeSet<ClassInfo>();
         if (this.isAllClasses()) {
             final String[] namespace = this.getNamespace();
             final Collection<ClassInfo> specifiedClasses = classInfoManager
                     .getClassInfos(namespace);
             accessibleClasses.addAll(specifiedClasses);
         } else {
             final String[] importName = this.getImportName();
             ClassInfo specifiedClass = classInfoManager.getClassInfo(importName);
             if (null == specifiedClass) {
                 specifiedClass = new ExternalClassInfo(importName);
                accessibleClasses.add(specifiedClass);
             }
         }
 
         this.resolvedInfo = new ImportStatementInfo(fromLine, fromColumn, toLine, toColumn,
                 accessibleClasses);
         return this.resolvedInfo;
     }
 
     /**
      * OԖԂ
      * 
      * @return OԖ
      */
     public String[] getImportName() {
         return this.importName;
     }
 
     /**
      * OԖԂD
      * 
      * @return OԖ
      */
     public String[] getNamespace() {
 
         final String[] importName = this.getImportName();
         if (this.isAllClasses()) {
             return importName;
         }
 
         final String[] namespace = new String[importName.length - 1];
         System.arraycopy(importName, 0, namespace, 0, importName.length - 1);
         return namespace;
     }
 
     /**
      * ̃IuWFNg̃nbVR[hԂ
      * 
      * @return ̃IuWFNg̃nbVR[h
      */
     @Override
     public int hashCode() {
 
         int hash = 0;
         String[] namespace = this.getNamespace();
         for (int i = 0; i < namespace.length; i++) {
             hash += namespace.hashCode();
         }
 
         return hash;
     }
 
     /**
      * SẴNXp\ǂ
      * 
      * @return p\łꍇ true, łȂꍇ false
      */
     public boolean isAllClasses() {
         return this.allClasses;
     }
 
     /**
      * OԖ\ϐ
      */
     private final String[] importName;
 
     /**
      * SẴNXp\ǂ\ϐ
      */
     private final boolean allClasses;
 }
