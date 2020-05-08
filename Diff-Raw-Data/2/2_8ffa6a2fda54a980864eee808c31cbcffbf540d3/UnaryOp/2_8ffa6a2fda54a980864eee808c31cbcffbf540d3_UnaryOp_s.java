 package banjo.dom;
 
 import org.eclipse.jdt.annotation.Nullable;
 
 import banjo.parser.util.FileRange;
 import fj.data.Option;
 
 public class UnaryOp extends AbstractOp implements SourceExpr {
 	private final SourceExpr operand;
 	public UnaryOp(Operator operator, OperatorRef opToken, SourceExpr operand, Option<OperatorRef> closeParenToken) {
 		super(operator.isSuffix()?
 				new FileRange(operand.getFileRange(), opToken.getFileRange()):
 				new FileRange(opToken.getFileRange(), operand.getFileRange()),
 				operator, opToken, closeParenToken);
 		this.operand = operand;
 	}
 	public SourceExpr getOperand() {
 		return operand;
 	}
 	
 	public void toSource(StringBuffer sb) {
 		if(operator.isParen()) {
 			sb.append(operator.getParenType().getStartChar());
		} if(!operator.isSuffix()) {
 			sb.append(operator.getOp());
 			if(operator.getPrecedence() != Precedence.UNARY_PREFIX || operand.getPrecedence() == Precedence.UNARY_PREFIX)
 				sb.append(' '); // Put a space for bullets
 		}
 		operand.toSource(sb, getPrecedence());
 		if(operator.isParen()) {
 			sb.append(operator.getParenType().getEndChar());
 		} else if(operator.isSuffix()) {
 			sb.append(operator.getOp());
 		}
 	}
 	
 	@Override
 	public Precedence getPrecedence() {
 		return operator.getPrecedence();
 	}
 	
 	@Override
 	public Expr transform(ExprTransformer transformer) {
 		SourceExpr newOperand = transformer.transform(operand);
 		OperatorRef newOpToken = transformer.transform(opToken);
 		Option<OperatorRef> newCloseParenToken = optTransform(closeParenToken, transformer);
 		if(newOperand == operand && newOpToken == opToken & newCloseParenToken == this.closeParenToken)
 			return this;
 		return new UnaryOp(operator, newOpToken, newOperand, newCloseParenToken);
 	}
 	@Override
 	public @Nullable <T> T acceptVisitor(SourceExprVisitor<T> visitor) {
 		return visitor.visitUnaryOp(this);
 	}
 }
