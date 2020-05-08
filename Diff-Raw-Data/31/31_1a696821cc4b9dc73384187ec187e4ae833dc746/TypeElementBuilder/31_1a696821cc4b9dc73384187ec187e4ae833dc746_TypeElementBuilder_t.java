 package jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.expression;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.BuildDataManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.token.AstToken;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.token.ConstantToken;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.visitor.AstVisitEvent;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.PrimitiveTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedReferenceTypeInfo;
 
 /**
  * @author kou-tngt
  *
  */
 public class TypeElementBuilder extends ExpressionBuilder{
     
     /**
      * @param expressionManager
      */
     public TypeElementBuilder(ExpressionElementManager expressionManager,BuildDataManager buildManager) {
         super(expressionManager);
         
         if (null == buildManager){
             throw new NullPointerException("buildManager is null.");
         }
         
         this.buildManager = buildManager;
     }
     
     protected void afterExited(AstVisitEvent event){
         AstToken token = event.getToken();
         if (token.isPrimitiveType()){
             buildPrimitiveType(token.toString());
         }
         else if (token.isTypeDefinition()){
             buildType();
         } else if (token instanceof ConstantToken){
             buildConstantElement((ConstantToken)token);
         }
     }
     
     protected void buildType(){
         ExpressionElement[] elements = getAvailableElements();
         
         if (elements.length == 1){
             if (elements[0] instanceof IdentifierElement){
                String[] typeName = ((IdentifierElement)elements[0]).getQualifiedName();
                String[] trueTypeName = buildManager.resolveAliase(typeName);
                UnresolvedReferenceTypeInfo type = new UnresolvedReferenceTypeInfo(buildManager.getAvailableNameSpaceSet(),trueTypeName);
                 pushElement(new TypeElement(type));
             } else if (elements[0] instanceof TypeElement){
                 pushElement(elements[0]);
             }
         }
     }
     
     protected void buildPrimitiveType(String name){
         pushElement(new TypeElement(PrimitiveTypeInfo.getType(name)));
     }
     
     protected void buildConstantElement(ConstantToken token){
         pushElement(new TypeElement(token.getType()));
     }
 
     @Override
     protected boolean isTriggerToken(AstToken token) {
         return token.isPrimitiveType() || token.isTypeDefinition() || (token instanceof ConstantToken);
     }
     
     private final BuildDataManager buildManager;
 
 }
