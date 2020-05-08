 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.Set;
 
 
 /**
  * Ci[NX\NXD
  * 
  * @author higo
  */
 @SuppressWarnings("serial")
 public class TargetInnerClassInfo extends TargetClassInfo implements InnerClassInfo {
 
     /**
      * Ci[NXIuWFNg
      * 
      * @param modifiers Cq Set
      * @param namespace O
      * @param className NX
      * @param outerUnit ÕjbgCTargetClassInfo@ TargetMethodInfo łȂ΂ȂȂ
      * @param privateVisible NX̂ݎQƉ\
      * @param namespaceVisible OԂQƉ\
      * @param inheritanceVisible qNXQƉ\
      * @param publicVisible ǂłQƉ\
      * @param instance CX^Xo[ǂ
      * @param isInterface C^tF[Xǂ
      * @param fileInfo ̃NX錾Ăt@C
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      */
     public TargetInnerClassInfo(final Set<ModifierInfo> modifiers, final NamespaceInfo namespace,
             final String className, final UnitInfo outerUnit, final boolean privateVisible,
             final boolean namespaceVisible, final boolean inheritanceVisible,
             final boolean publicVisible, final boolean instance, final boolean isInterface,
             final FileInfo fileInfo, final int fromLine, final int fromColumn, final int toLine,
             final int toColumn) {
 
         super(modifiers, namespace, className, privateVisible, namespaceVisible,
                 inheritanceVisible, publicVisible, instance, isInterface, fileInfo, fromLine,
                 fromColumn, toLine, toColumn);
 
         if (null == outerUnit) {
             throw new NullPointerException();
         }
 
        if (!(outerUnit instanceof TargetClassInfo) && !(outerUnit instanceof TargetMethodInfo)
                && !(outerUnit instanceof TargetConstructorInfo)
                && !(outerUnit instanceof InitializerInfo)) {
             throw new IllegalArgumentException();
         }
 
         this.outerUnit = outerUnit;
     }
 
     /**
      * Ci[NXIuWFNg
      * 
      * @param modifiers Cq Set
      * @param fullQualifiedName S薼
      * @param outerUnit ÕjbgCTargetClassInfo@ TargetMethodInfo łȂ΂ȂȂ
      * @param privateVisible NX̂ݎQƉ\
      * @param namespaceVisible OԂQƉ\
      * @param inheritanceVisible qNXQƉ\
      * @param publicVisible ǂłQƉ\
      * @param instance CX^Xo[ǂ
      * @param isInterface C^tF[Xǂ
      * @param fileInfo ̃NX錾Ăt@C
      * @param fromLine Jns
      * @param fromColumn Jn
      * @param toLine Is
      * @param toColumn I
      */
     public TargetInnerClassInfo(final Set<ModifierInfo> modifiers,
             final String[] fullQualifiedName, final UnitInfo outerUnit,
             final boolean privateVisible, final boolean namespaceVisible,
             final boolean inheritanceVisible, final boolean publicVisible, final boolean instance,
             final boolean isInterface, final FileInfo fileInfo, final int fromLine,
             final int fromColumn, final int toLine, final int toColumn) {
 
         super(modifiers, fullQualifiedName, privateVisible, namespaceVisible, inheritanceVisible,
                 publicVisible, instance, isInterface, fileInfo, fromLine, fromColumn, toLine,
                 toColumn);
 
         if (null == outerUnit) {
             throw new NullPointerException();
         }
 
         if (!(outerUnit instanceof TargetClassInfo) && !(outerUnit instanceof TargetMethodInfo)
                 && !(outerUnit instanceof TargetConstructorInfo)
                 && !(outerUnit instanceof InitializerInfo)) {
             throw new IllegalArgumentException();
         }
 
         this.outerUnit = outerUnit;
     }
 
     /**
      * ÕjbgԂ
      * 
      * @return Õjbg
      */
     @Override
     public final UnitInfo getOuterUnit() {
         return this.outerUnit;
     }
 
     /**
      * ÕNXԂ.
      * ܂CgetOuterUnit ̕ԂlTargetClassInfołꍇ́C̃IuWFNgԂC
      * ԂlCTargetMethodInfołꍇ́C̃IuWFNg ownerClass ԂD
      * 
      * @return@ÕNX
      */
     @Override
     public final ClassInfo getOuterClass() {
 
         final UnitInfo unitInfo = this.getOuterUnit();
 
         // ÕjbgNXł΂̂܂ܕԂ
         if (unitInfo instanceof TargetClassInfo) {
             return (TargetClassInfo) unitInfo;
 
            // ÕjbgTargetCallableUnitInfoł΁C̏LNXԂ
        } else if (unitInfo instanceof TargetMethodInfo || unitInfo instanceof TargetConstructorInfo
                || unitInfo instanceof InitializerInfo) {
 
            final ClassInfo ownerClass = ((CallableUnitInfo) unitInfo).getOwnerClass();
             return (TargetClassInfo) ownerClass;
         }
 
         assert false : "here shouldn't be reached!";
         return null;
     }
 
     /**
      * Õjbg̃IuWFNgۑϐ
      */
     private final UnitInfo outerUnit;
 }
