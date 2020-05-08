 package jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.expression;
 
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.BuildDataManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.token.AstToken;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.token.OperatorToken;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.visitor.AstVisitEvent;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.OPERATOR;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedArrayElementUsage;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedBinominalOperation;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedTypeInfo;
 
 
 public class OperatorExpressionBuilder extends ExpressionBuilder {
 
     public OperatorExpressionBuilder(final ExpressionElementManager expressionManager,
             final BuildDataManager buildManager) {
         super(expressionManager);
         this.buildDataManager = buildManager;
     }
 
     @Override
     protected void afterExited(final AstVisitEvent event) {
         final AstToken token = event.getToken();
         if (isTriggerToken(token)) {
             this.buildOperatorElement((OperatorToken) token);
         }
     }
 
     protected void buildOperatorElement(final OperatorToken token) {
         //ZqKvƂ鍀̐
         final int term = token.getTermCount();
         //Ӓlւ̑邩ǂ
         final boolean assignmentLeft = token.isAssignmentOperator();
         //Ӓlւ̎QƂ邩ǂ
         final boolean referenceLeft = token.isLeftTermIsReferencee();
         //^Ɋւ鍀̃CfbNX̔z
         final int[] typeSpecifiedTermIndexes = token.getTypeSpecifiedTermIndexes();
 
         final ExpressionElement[] elements = this.getAvailableElements();
 
         assert (term > 0 && term == elements.length) : "Illegal state: unexpected element count.";
         
         //ě^L^z
         final UnresolvedTypeInfo[] termTypes = new UnresolvedTypeInfo[elements.length];
 
         //ōӒlɂ
         if (elements[0] instanceof IdentifierElement) {
             //ʎq̏ꍇ
             final IdentifierElement leftElement = (IdentifierElement) elements[0];
             if (referenceLeft) {
                 //QƂȂQƕϐƂĉČʂ̌^擾
                 termTypes[0] = leftElement.resolveAsReferencedVariable(this.buildDataManager);
             }
 
             if (assignmentLeft) {
                 //ȂϐƂĉČʂ̌^擾
                 termTypes[0] = leftElement.resolveAsAssignmetedVariable(this.buildDataManager);
             }
         } else {
             //ȊȌꍇ͒ڌ^擾
             termTypes[0] = elements[0].getType();
         }
 
        //2ڂɂ
         for (int i = 1; i < term; i++) {
             if (elements[i] instanceof IdentifierElement) {
                 //ʎqȂ珟ɎQƂƂĉĕ擾
                 termTypes[i] = ((IdentifierElement) elements[i])
                         .resolveAsReferencedVariable(this.buildDataManager);
             } else {
                 //ȊOȂ璼ڌ^擾
                 termTypes[i] = elements[i].getType();
             }
         }
 
         
         
         final OPERATOR operator = token.getOperator();
         
         if (2 == term && null != operator){
             //Iy[^[CX^XZbgĂ2ZqOɌ^菈Ϗ
             assert(null != termTypes[0]) : "Illega state: first term type was not decided.";
             assert(null != termTypes[1]) : "Illega state: second term type was not decided.";
             
             UnresolvedBinominalOperation operation = new UnresolvedBinominalOperation(operator,termTypes[0],termTypes[1]);
             pushElement(TypeElement.getInstance(operation));
             
         } else{
             //Ō^肷
             UnresolvedTypeInfo resultType = null;
             
             //Iy[^ɂĂłɌ肵Ă߂ľ^Cm肵ĂȂnull
             final UnresolvedTypeInfo type = token.getSpecifiedResultType();
             
             if (null != type) {
                 //Iy[^ɂĂłɌʂ̌^肵Ă
                 resultType = type;
             } else if (token.equals(OperatorToken.ARRAY)) {
                 //zLqq̏ꍇ͓ʏ
                 UnresolvedTypeInfo ownerType;
                 if (elements[0] instanceof IdentifierElement) {
                     ownerType = ((IdentifierElement) elements[0])
                             .resolveAsReferencedVariable(this.buildDataManager);
                 } else {
                     ownerType = elements[0].getType();
                 }
                 resultType = new UnresolvedArrayElementUsage(ownerType);
             } else {
                 //^Ɋ֘A鍀珇ԂɋĂčŏɌłzɏɌ߂
                 for (int i = 0; i < typeSpecifiedTermIndexes.length; i++) {
                     resultType = termTypes[typeSpecifiedTermIndexes[i]];
                     if (null != resultType){
                         break;
                     }
                 }
             }
             
             assert (null != resultType) : "Illegal state: operation resultType was not decided.";
             
             this.pushElement(TypeElement.getInstance(resultType));
         }
     }
 
     @Override
     protected boolean isTriggerToken(final AstToken token) {
         return token.isOperator();
     }
 
     private final BuildDataManager buildDataManager;
  
 }
