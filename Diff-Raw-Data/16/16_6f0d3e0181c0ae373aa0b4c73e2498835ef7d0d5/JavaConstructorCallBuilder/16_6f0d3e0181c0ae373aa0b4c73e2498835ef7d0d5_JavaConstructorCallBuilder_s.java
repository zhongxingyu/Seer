 package jp.ac.osaka_u.ist.sel.metricstool.main.ast.java;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.BuildDataManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.expression.ConstructorCallBuilder;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.expression.ExpressionElement;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.expression.ExpressionElementManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.expression.MethodCallElement;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.expression.TypeElement;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.token.AstToken;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.visitor.AstVisitEvent;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedArrayTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedMethodCall;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedReferenceTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedTypeInfo;
 
 public class JavaConstructorCallBuilder extends ConstructorCallBuilder{
 
     public JavaConstructorCallBuilder(ExpressionElementManager expressionManager, BuildDataManager buildDataManager) {
         super(expressionManager,buildDataManager);
         this.buildDataManager = buildDataManager;
     }
     
     @Override
     protected void afterExited(AstVisitEvent event) {
         super.afterExited(event);
         
         AstToken token = event.getToken();
         if (token.equals(JavaAstToken.CONSTRUCTOR_CALL)){
             buildInnerConstructorCall(buildDataManager.getCurrentClass());
         } else if (token.equals(JavaAstToken.SUPER_CONSTRUCTOR_CALL)){
             buildInnerConstructorCall(buildDataManager.getCurrentClass().getSuperClasses().iterator().next());
         }
     }
     
     protected void buildNewConstructorCall(){
         ExpressionElement[] elements = getAvailableElements();
         
         assert(elements.length > 0) : "Illegal state: constructor element not found.";
         
         assert(elements[0] instanceof TypeElement) : "Illegal state: constructor owner is not type.";
         
         if (isJavaArrayInstantiation(elements)){
             UnresolvedTypeInfo type = (UnresolvedTypeInfo)elements[0].getType();
             
             pushElement(new TypeElement(resolveArrayElement(type,elements)));
         } else {
             super.buildNewConstructorCall();
         }
     }
     
     protected void buildInnerConstructorCall(UnresolvedTypeInfo ownerClass){
         ExpressionElement[] elements = getAvailableElements();
         
         String superClassName = "";
         
         if (ownerClass instanceof UnresolvedClassInfo){
             superClassName = ((UnresolvedClassInfo)ownerClass).getClassName();
         } else if (ownerClass instanceof UnresolvedReferenceTypeInfo){
             String[] referenceName = ((UnresolvedReferenceTypeInfo)ownerClass).getReferenceName();
             superClassName = referenceName[referenceName.length-1];
         }
         
         assert(null != superClassName) : "Illegal state: unexpected ownerClass type.";
         
         UnresolvedMethodCall constructorCall = new UnresolvedMethodCall(ownerClass,superClassName,true);
        resolveParameters(constructorCall, elements);
         pushElement(new MethodCallElement(constructorCall));
         buildDataManager.addMethodCall(constructorCall);
     }
 
     
     protected boolean isJavaArrayInstantiation(ExpressionElement[] elements){
         for(ExpressionElement element : elements){
             if (element.equals(JavaArrayInstantiationElement.getInstance())){
                 return true;
             }
         }
         return false;
     }
     
     protected UnresolvedArrayTypeInfo resolveArrayElement(UnresolvedTypeInfo type,ExpressionElement[] elements){
         int i=1;
         int dimension = 0;
         while(i < elements.length && elements[i].equals(JavaArrayInstantiationElement.getInstance())){
             dimension++;
             i++;
         }
         
         if (dimension > 0){
             return UnresolvedArrayTypeInfo.getType(type,dimension);
         } else {
             return null;
         }
     }
     
     @Override
     protected boolean isTriggerToken(AstToken token) {
         return super.isTriggerToken(token) || token.equals(JavaAstToken.CONSTRUCTOR_CALL) || token.equals(JavaAstToken.SUPER_CONSTRUCTOR_CALL);
     }
     
     private final BuildDataManager buildDataManager;
 }
