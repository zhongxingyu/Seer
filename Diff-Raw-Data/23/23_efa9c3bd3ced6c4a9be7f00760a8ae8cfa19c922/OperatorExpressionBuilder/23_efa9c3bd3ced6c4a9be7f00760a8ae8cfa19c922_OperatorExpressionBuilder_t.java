 package jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.expression;
 
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.BuildDataManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.token.AstToken;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.token.OperatorToken;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.visitor.AstVisitEvent;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ExpressionInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.OPERATOR;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.OPERATOR_TYPE;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedArrayElementUsageInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedArrayTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedArrayTypeReferenceInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedBinominalOperationInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedCastUsageInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedClassTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedExpressionInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedMonominalOperationInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedTernaryOperationInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved.UnresolvedTypeInfo;
 
 
 public class OperatorExpressionBuilder extends ExpressionBuilder {
 
     public OperatorExpressionBuilder(final ExpressionElementManager expressionManager,
             final BuildDataManager buildManager) {
         super(expressionManager, buildManager);
     }
 
     @Override
     protected void afterExited(final AstVisitEvent event) {
         final AstToken token = event.getToken();
         if (isTriggerToken(token)) {
             this.buildOperatorElement(((OperatorToken) token), event);
         }
     }
 
     protected void buildOperatorElement(final OperatorToken token, final AstVisitEvent event) {
         //ZqKvƂ鍀̐
         final int term = token.getTermCount();
 
         //^Ɋւ鍀̃CfbNX̔z
         final int[] typeSpecifiedTermIndexes = token.getTypeSpecifiedTermIndexes();
 
         final ExpressionElement[] elements = this.getAvailableElements();
 
         assert (term > 0 && term == elements.length) : "Illegal state: unexpected element count.";
 
         if (term > 0 && term == elements.length) {
             final OPERATOR_TYPE operatorType = token.getOperator();
             final OPERATOR operator = OPERATOR.getOperator(event.getText());
 
             //ě^L^z
             final UnresolvedExpressionInfo<? extends ExpressionInfo>[] termTypes = new UnresolvedExpressionInfo<?>[elements.length];
 
             //ōӒlɂ
             final ExpressionElement primary = elements[0];
             if (primary instanceof IdentifierElement) {
                 //ʎq̏ꍇ
                 final IdentifierElement leftElement = (IdentifierElement) elements[0];
 
                 //Ӓlւ̑邩ǂ
                 boolean assignmentLeft = false;
                 //Ӓlւ̎QƂ邩ǂ
                 boolean referenceLeft = true;
                 if (null != operator) {
                     assignmentLeft = operator.isFirstIsAssignmentee();
                     referenceLeft = operator.isFirstIsReferencee();
                 }
 
                 //QƂȂQƕϐƂĉČʂ̌^擾
                 termTypes[0] = leftElement.resolveAsVariable(this.buildDataManager, referenceLeft,
                         assignmentLeft);
             } else if (primary instanceof TypeElement) {
                 TypeElement typeElement = (TypeElement) primary;
                 if (typeElement.getType() instanceof UnresolvedClassTypeInfo) {
                     // LXgƂ炭ɓB
                     // TODO UnresolvedReferenceTypeInfoɂׂ
                     termTypes[0] = ((UnresolvedClassTypeInfo) typeElement.getType()).getUsage(
                             typeElement.getFromLine(), typeElement.getFromColumn(), typeElement
                                     .getToLine(), typeElement.getToColumn());
                 } else if (typeElement.getType() instanceof UnresolvedArrayTypeInfo) {
                     UnresolvedArrayTypeInfo arrayType = (UnresolvedArrayTypeInfo) typeElement
                             .getType();
                    termTypes[0] = new UnresolvedArrayTypeReferenceInfo(arrayType,
                            typeElement.fromLine, typeElement.fromColumn, typeElement.toLine,
                            typeElement.toColumn);
                 } else {
 
                     termTypes[0] = elements[0].getUsage();
                 }
             } else {
                 //ȊȌꍇ͒ڌ^擾
                 termTypes[0] = primary.getUsage();
             }
 
             //2ڈȍ~ɂ
             for (int i = 1; i < term; i++) {
                 if (elements[i] instanceof IdentifierElement) {
                     //ʎqȂ珟ɎQƂƂĉĕ擾
                     termTypes[i] = ((IdentifierElement) elements[i]).resolveAsVariable(
                             this.buildDataManager, true, false);
                 } else if (elements[i] instanceof TypeElement) {
                     TypeElement typeElement = (TypeElement) elements[i];
                     if (typeElement.getType() instanceof UnresolvedClassTypeInfo) {
                         termTypes[i] = ((UnresolvedClassTypeInfo) typeElement.getType()).getUsage(
                                 typeElement.getFromLine(), typeElement.getFromColumn(), typeElement
                                         .getToLine(), typeElement.getToColumn());
                     } else if (typeElement.getType() instanceof UnresolvedArrayTypeInfo) {
                         // ɓB̂instanceof type[]Ƃ
                         UnresolvedArrayTypeInfo arrayType = (UnresolvedArrayTypeInfo) typeElement
                                 .getType();
                        termTypes[i] = new UnresolvedArrayTypeReferenceInfo(arrayType,
                                typeElement.fromLine, typeElement.fromColumn, typeElement.toLine,
                                typeElement.toColumn);
                     } else {
                         termTypes[i] = elements[i].getUsage();
                     }
                 } else {
                     //ȊOȂ璼ڌ^擾
                     termTypes[i] = elements[i].getUsage();
                 }
             }
 
             if (2 == term && null != operatorType) {
                 //Iy[^[CX^XZbgĂ2ZqOɌ^菈Ϗ
                 assert (null != termTypes[0]) : "Illega state: first term type was not decided.";
                 assert (null != termTypes[1]) : "Illega state: second term type was not decided.";
                 assert null != operator : "Illegal state: operator is null";
 
                 final UnresolvedBinominalOperationInfo operation = new UnresolvedBinominalOperationInfo(
                         operator, termTypes[0], termTypes[1]);
                 operation.setFromLine(termTypes[0].getFromLine());
                 operation.setFromColumn(termTypes[0].getFromColumn());
                 operation.setToLine(termTypes[1].getToLine());
                 operation.setToColumn(termTypes[1].getToColumn());
 
                 pushElement(new UsageElement(operation));
 
             } else if (3 == term && token.equals(OperatorToken.TERNARY)) {
                 assert null != termTypes[0] : "Illegal stete : first term type was not decided.";
                 assert null != termTypes[1] : "Illegal stete : first term type was not decided.";
                 assert null != termTypes[2] : "Illegal stete : first term type was not decided.";
 
                 final UnresolvedTernaryOperationInfo operation = new UnresolvedTernaryOperationInfo(
                         termTypes[0], termTypes[1], termTypes[2]);
                 operation.setFromLine(termTypes[0].getFromLine());
                 operation.setFromColumn(termTypes[0].getFromColumn());
                 operation.setToLine(termTypes[2].getToLine());
                 operation.setToColumn(termTypes[2].getToColumn());
 
                 pushElement(new UsageElement(operation));
             } else {
                 //Ō^肷
                 UnresolvedExpressionInfo<? extends ExpressionInfo> resultType = null;
 
                 if (null != operator && null != operator.getSpecifiedResultType()) {
                     //Iy[^ɂĂłɌʂ̌^肵Ă
                     assert null != operator : "Illegal state: operator is null";
                     resultType = new UnresolvedMonominalOperationInfo(termTypes[0], operator);
 
                     if ((termTypes[0].getFromLine() < event.getStartLine())
                             || (termTypes[0].getFromLine() == event.getStartLine() && termTypes[0]
                                     .getFromColumn() < event.getStartColumn())) {
                         resultType.setFromLine(termTypes[0].getFromLine());
                         resultType.setFromColumn(termTypes[0].getFromColumn());
                         resultType.setToLine(event.getEndLine());
                         resultType.setToColumn(event.getEndColumn());
                     } else {
                         resultType.setFromLine(event.getStartLine());
                         resultType.setFromColumn(event.getStartColumn());
                         resultType.setToLine(termTypes[0].getToLine());
                         resultType.setToColumn(termTypes[0].getToColumn());
                     }
                 } else if (1 == term
                         && (OPERATOR.MINUS.equals(operator) || OPERATOR.PLUS.equals(operator))) {
                     resultType = new UnresolvedMonominalOperationInfo(termTypes[0], operator);
                     resultType.setFromLine(event.getStartLine());
                     resultType.setFromColumn(event.getStartColumn());
                     resultType.setToLine(termTypes[0].getToLine());
                     resultType.setToColumn(termTypes[0].getToColumn());
                 } else if (token.equals(OperatorToken.ARRAY)) {
                     //zLqq̏ꍇ͓ʏ
                     final UnresolvedExpressionInfo<? extends ExpressionInfo> ownerType;
                     if (elements[0] instanceof IdentifierElement) {
                         ownerType = ((IdentifierElement) elements[0]).resolveAsVariable(
                                 this.buildDataManager, true, false);
                     } else {
                         ownerType = elements[0].getUsage();
                     }
                     assert null != elements[1] : "Illegal state: expression that show index of array is not found.";
                     resultType = new UnresolvedArrayElementUsageInfo(ownerType, elements[1]
                             .getUsage());
                     resultType.setFromLine(ownerType.getFromLine());
                     resultType.setFromColumn(ownerType.getFromColumn());
                     resultType.setToLine(event.getEndLine());
                     resultType.setToColumn(event.getEndColumn());
                 } else if (token.equals(OperatorToken.CAST) && elements[0] instanceof TypeElement) {
                     final UnresolvedTypeInfo<? extends TypeInfo> castType = ((TypeElement) elements[0])
                             .getType();
                     final UnresolvedExpressionInfo<? extends ExpressionInfo> castedUsage = elements[1]
                             .getUsage();
                     resultType = new UnresolvedCastUsageInfo(castType, castedUsage);
                     resultType.setFromLine(event.getStartLine());
                     resultType.setFromColumn(event.getStartColumn());
                     resultType.setToLine(castedUsage.getToLine());
                     resultType.setToColumn(castedUsage.getToColumn());
                 } else {
                     //^Ɋ֘A鍀珇ԂɋĂčŏɌłzɏɌ߂
                     for (int i = 0; i < typeSpecifiedTermIndexes.length; i++) {
                         resultType = termTypes[typeSpecifiedTermIndexes[i]];
                         if (null != resultType) {
                             break;
                         }
                     }
                 }
 
                 assert (null != resultType) : "Illegal state: operation resultType was not decided.";
 
                 this.pushElement(new UsageElement(resultType));
             }
         }
     }
 
     @Override
     protected boolean isTriggerToken(final AstToken token) {
         return token.isOperator();
     }
 
 }
