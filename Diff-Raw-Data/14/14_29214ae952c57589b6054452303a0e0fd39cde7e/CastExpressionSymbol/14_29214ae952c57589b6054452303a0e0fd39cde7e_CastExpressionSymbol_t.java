 package cs444.parser.symbols.ast.expressions;
 
 import cs444.CompilerException;
 import cs444.ast.ISymbolVisitor;
 import cs444.parser.symbols.ISymbol;
 import cs444.parser.symbols.ast.TypeSymbol;
 
 public class CastExpressionSymbol extends BaseExprSymbol{
     public final TypeSymbol castExprType;
 
     public CastExpressionSymbol(String value, TypeSymbol castExprType,
                                 ISymbol operandExpression) {
         super("CastExpression");
         this.castExprType = castExprType;
        this.children.add(operandExpression);
     }
 
     @Override
     public boolean empty() {
         return false;
     }
 
     @Override
     public boolean isCollapsable() {
         return false;
     }
 
    public ISymbol getOperandExpression(){
        return this.children.get(0);
    }

     @Override
     public void accept(ISymbolVisitor visitor) throws CompilerException {
         castExprType.accept(visitor);
        for (ISymbol child : children) {
            child.accept(visitor);
        }
         visitor.visit(this);
     }
 }
