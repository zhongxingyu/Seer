 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.CallableUnitInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassImportStatementInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ExternalClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.MethodInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.security.MetricsToolSecurityManager;
 
 
 /**
  * ASTp[X̍ہCQƌ^ϐ̗p\ȖOԖC܂͊S薼\NX
  * 
  * @author higo
  * 
  */
 public final class UnresolvedClassImportStatementInfo extends
         UnresolvedImportStatementInfo<ClassImportStatementInfo> {
 
     public static List<UnresolvedClassImportStatementInfo> getClassImportStatements(
             final Collection<UnresolvedImportStatementInfo<?>> importStatements) {
 
         final List<UnresolvedClassImportStatementInfo> classImportStatements = new LinkedList<UnresolvedClassImportStatementInfo>();
         for (final UnresolvedImportStatementInfo<?> importStatement : importStatements) {
             if (importStatement instanceof UnresolvedClassImportStatementInfo) {
                 classImportStatements.add((UnresolvedClassImportStatementInfo) importStatement);
             }
         }
         return Collections.unmodifiableList(classImportStatements);
     }
 
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
     public UnresolvedClassImportStatementInfo(final String[] namespace, final boolean allClasses) {
         super(namespace, allClasses);
     }
 
     @Override
     public ClassImportStatementInfo resolve(final TargetClassInfo usingClass,
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
         if (this.isAll()) {
             final String[] namespace = this.getNamespace();
             final Collection<ClassInfo> specifiedClasses = classInfoManager
                     .getClassInfos(namespace);
             accessibleClasses.addAll(specifiedClasses);
         } else {
             final String[] importName = this.getImportName();
             ClassInfo specifiedClass = classInfoManager.getClassInfo(importName);
             if (null == specifiedClass) {
                 specifiedClass = new ExternalClassInfo(importName);
                 classInfoManager.add(specifiedClass);
                accessibleClasses.add(specifiedClass);
             }
         }
 
         this.resolvedInfo = new ClassImportStatementInfo(accessibleClasses, fromLine, fromColumn,
                 toLine, toColumn);
         return this.resolvedInfo;
     }
 
     /**
      * OԖԂD
      * 
      * @return OԖ
      */
     public String[] getNamespace() {
 
         final String[] importName = this.getImportName();
         if (this.isAll()) {
             return importName;
         }
 
         final String[] namespace = new String[importName.length - 1];
         System.arraycopy(importName, 0, namespace, 0, importName.length - 1);
         return namespace;
     }
 }
