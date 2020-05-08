 package cs444.parser.symbols.ast.expressions;
 
 import cs444.CompilerException;
 import cs444.ast.ISymbolVisitor;
 import cs444.parser.symbols.ISymbol;
 
 public class ReturnExprSymbol extends BaseExprSymbol{
     public ReturnExprSymbol(ISymbol child) {
         super("Return");
         if(child != null)children.add(child);
     }
 
     @Override
     public void accept(ISymbolVisitor visitor) throws CompilerException {
        visitor.open(this);
         if(!children.isEmpty()) children.get(0).accept(visitor);
         visitor.close(this);
     }
 }
