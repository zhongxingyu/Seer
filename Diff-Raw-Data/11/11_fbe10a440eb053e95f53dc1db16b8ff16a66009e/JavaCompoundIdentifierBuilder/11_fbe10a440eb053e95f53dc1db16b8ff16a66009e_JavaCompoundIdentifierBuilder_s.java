 package jp.ac.osaka_u.ist.sel.metricstool.main.ast.java;
 
 
 import java.util.HashSet;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.BuildDataManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.expression.CompoundIdentifierBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.expression.ExpressionElement;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.expression.ExpressionElementManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.expression.FieldOrMethodElement;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.expression.IdentifierElement;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.expression.InstanceSpecificElement;
import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.expression.TypeElement;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.expression.UsageElement;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.VariableInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.AvailableNamespaceInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedClassReferenceInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedClassTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedEntityUsageInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedFullQualifiedNameClassReferenceInfo;
import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedVariableInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedVariableUsageInfo;
 
 
 /**
  * @author kou-tngt, t-miyake
  *
  */
 public class JavaCompoundIdentifierBuilder extends CompoundIdentifierBuilder {
     public JavaCompoundIdentifierBuilder(ExpressionElementManager expressionManager,
             BuildDataManager buildManager) {
         super(expressionManager, buildManager);
         this.buildDataManager = buildManager;
     }
 
     @Override
     protected void buildCompoundIdentifierElement(ExpressionElement[] elements) {
 
         assert (2 == elements.length) : "Illega state: two element must be usable.";
 
         ExpressionElement left = elements[0];
         ExpressionElement right = elements[1];
         if (right.equals(JavaExpressionElement.CLASS)) {
             UnresolvedClassReferenceInfo classReference = UnresolvedClassReferenceInfo
                     .createClassReference(JAVA_LANG_CLASS);
             pushElement(UsageElement.getInstance(classReference));
         } else if (right.equals(InstanceSpecificElement.THIS)) {
             UnresolvedClassInfo classInfo = getSpecifiedOuterClass((IdentifierElement) left);
 
             if (classInfo != null) {
                 pushElement(UsageElement.getInstance(classInfo.getClassReference()));
             } else {
                 assert (false) : "Illegal state: specified this class "
                         + ((IdentifierElement) left).getName()
                         + " was not found from outer classes.";
             }
         } else if (left.equals(JavaExpressionElement.SUPER)) {
             if (right instanceof IdentifierElement) {
                 UnresolvedClassInfo classInfo = buildDataManager.getCurrentClass();
                 UnresolvedClassTypeInfo superClassType = classInfo.getSuperClasses().iterator()
                         .next();
                 UnresolvedClassReferenceInfo superClassReference = UnresolvedClassReferenceInfo
                         .createClassReference(superClassType);
                 pushElement(new FieldOrMethodElement(superClassReference,
                         ((IdentifierElement) right).getName()));
             }
         } else if (right.equals(JavaExpressionElement.SUPER)) {
             UnresolvedClassInfo classInfo = null;
             if (left instanceof IdentifierElement) {
                 //܂ϐ.super()ƂRXgN^ĂяoǂmF
                 IdentifierElement identifier = (IdentifierElement) left;
                 UnresolvedEntityUsageInfo ownerUsage = identifier
                         .resolveReferencedEntityIfPossible(buildDataManager);
                 UnresolvedVariableInfo<VariableInfo> variable = null;
                 if (null != ownerUsage && ownerUsage instanceof UnresolvedVariableUsageInfo) {
                     UnresolvedVariableUsageInfo variableUsage = (UnresolvedVariableUsageInfo) ownerUsage;
                     variable = buildDataManager.getCurrentScopeVariable(variableUsage
                             .getUsedVariableName());
                 }
 
                 if (null != variable) {
                     //ϐ
 
                     boolean match = false;
                     UnresolvedClassInfo currentClass = buildDataManager.getCurrentClass();
                     UnresolvedClassTypeInfo currentSuperClass = currentClass.getSuperClasses()
                             .iterator().next();
                     String[] names = null;
                     if (null != currentSuperClass) {
                         //names = currentSuperClass.getFullReferenceName();
                         names = currentSuperClass.getReferenceName();
                     }
                     if (null != names && variable.getType() instanceof UnresolvedClassTypeInfo) {
                         // TODO UnresolvedReferenceTypeɂׂ veXg
                         UnresolvedClassTypeInfo variableType = (UnresolvedClassTypeInfo) variable
                                 .getType();
                         for (String name : names) {
                             if (name.equals(variableType.getTypeName())) {
                                 match = true;
                                 break;
                             }
                         }
                     }
 
                     if (match) {
                         classInfo = currentClass;
                     }
                 }
 
                 if (null == classInfo) {
                     //ϐ.superƂĂяoƂĉ悤ƂĂ݂ǖ̂
                     //OuterClass.super.method()Ƃ\bhĂяô悤
                     classInfo = getSpecifiedOuterClass((IdentifierElement) left);
                 }
             } else if (left.getUsage() instanceof UnresolvedFullQualifiedNameClassReferenceInfo) {
                 classInfo = ((UnresolvedFullQualifiedNameClassReferenceInfo) left.getUsage())
                         .getReferencedClass();
             } else {
                 classInfo = buildDataManager.getCurrentClass();
             }
 
            UnresolvedTypeInfo superClassType = classInfo.getSuperClasses().iterator().next();
            if (classInfo != null) {
                pushElement(TypeElement.getInstance(superClassType));
             }
         } else {
             super.buildCompoundIdentifierElement(elements);
         }
     }
 
     private UnresolvedClassInfo getSpecifiedOuterClass(IdentifierElement identifier) {
         String name = identifier.getName();
         UnresolvedClassInfo classInfo = buildDataManager.getCurrentClass();
         while (null != classInfo && !name.equals(classInfo.getClassName())) {
             classInfo = classInfo.getOuterClass();
         }
         return classInfo;
     }
 
     private final static UnresolvedClassTypeInfo JAVA_LANG_CLASS = new UnresolvedClassTypeInfo(
             new HashSet<AvailableNamespaceInfo>(), new String[] { "java", "lang", "Class" });
 
     private final BuildDataManager buildDataManager;
 
 }
