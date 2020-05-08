 package cs444.parser.symbols.ast.expressions;
 
 import cs444.CompilerException;
 import cs444.ast.ISymbolVisitor;
 import cs444.parser.symbols.ISymbol;
 import cs444.parser.symbols.ast.TypeSymbol;
 
 public class CastExpressionSymbol extends BaseExprSymbol{
     public final TypeSymbol castExprType;
    public final ISymbol operandExpression;
 
     public CastExpressionSymbol(String value, TypeSymbol castExprType,
                                 ISymbol operandExpression) {
         super("CastExpression");
         this.castExprType = castExprType;
        this.operandExpression = operandExpression;
     }
 
     @Override
     public boolean empty() {
         return false;
     }
 
     @Override
     public boolean isCollapsable() {
         return false;
     }
 
     @Override
     public void accept(ISymbolVisitor visitor) throws CompilerException {
         castExprType.accept(visitor);
        operandExpression.accept(visitor);
         visitor.visit(this);
     }
 }
