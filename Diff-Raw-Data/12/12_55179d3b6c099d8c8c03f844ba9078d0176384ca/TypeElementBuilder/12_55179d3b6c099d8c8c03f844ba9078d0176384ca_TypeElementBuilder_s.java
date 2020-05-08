 package jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.expression;
 
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.BuildDataManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.token.AstToken;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.token.ConstantToken;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.visitor.AstVisitEvent;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.PrimitiveTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedArrayTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedReferenceTypeInfo;
 
 
 /**
  * @author kou-tngt
  *
  */
 public class TypeElementBuilder extends ExpressionBuilder {
 
     /**
      * @param expressionManager
      */
     public TypeElementBuilder(ExpressionElementManager expressionManager,
             BuildDataManager buildManager) {
         super(expressionManager);
 
         if (null == buildManager) {
             throw new NullPointerException("buildManager is null.");
         }
 
         this.buildManager = buildManager;
     }
 
     protected void afterExited(AstVisitEvent event) {
         AstToken token = event.getToken();
         if (token.isPrimitiveType()) {
             buildPrimitiveType(token.toString());
         } else if (token.isTypeDescription()) {
             buildType();
         } else if (token.isArrayDeclarator()){
             buildArrayType();
         } else if (token instanceof ConstantToken) {
             buildConstantElement((ConstantToken) token);
         }
     }
     
     protected void buildArrayType(){
         ExpressionElement[] elements = getAvailableElements();
         
         assert (elements.length == 1) : "Illegal state: type description was not found.";
 
         TypeElement typeElement = null;
         if (elements.length == 1) {
             if (elements[0] instanceof IdentifierElement) {
                 UnresolvedReferenceTypeInfo referenceType = buildReferenceType((IdentifierElement)elements[0]);
                 typeElement = new TypeElement(UnresolvedArrayTypeInfo.getType(referenceType,1));
             } else if (elements[0] instanceof TypeElement) {
                 typeElement = (TypeElement)elements[0];
                 typeElement.arrayDimensionIncl();
             }
         }
         
         if (null != typeElement){
             pushElement(typeElement);
         }
     }
 
     protected void buildType() {
         ExpressionElement[] elements = getAvailableElements();
 
         assert (elements.length == 1) : "Illegal state: type description was not found.";
         
         if (elements.length == 1) {
             if (elements[0] instanceof IdentifierElement) {
                 pushElement(new TypeElement(buildReferenceType((IdentifierElement)elements[0])));
             } else if (elements[0] instanceof TypeElement) {
                 pushElement(elements[0]);
             }
         }
     }
     
     protected UnresolvedReferenceTypeInfo buildReferenceType(IdentifierElement element){
         String[] typeName = element.getQualifiedName();
         String[] trueTypeName = buildManager.resolveAliase(typeName);
         return new UnresolvedReferenceTypeInfo(buildManager
                 .getAvailableNameSpaceSet(), trueTypeName);
     }
 
     protected void buildPrimitiveType(String name) {
         pushElement(new TypeElement(PrimitiveTypeInfo.getType(name)));
     }
 
     protected void buildConstantElement(ConstantToken token) {
         pushElement(new TypeElement(token.getType()));
     }
 
     @Override
     protected boolean isTriggerToken(AstToken token) {
        return token.isPrimitiveType() || token.isTypeDescription()
                 || (token instanceof ConstantToken) || token.isArrayDeclarator();
     }
 
     private final BuildDataManager buildManager;
 
 }
