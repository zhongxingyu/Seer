 package cs444.parser.symbols.ast.factories.expressions;
 
import java.util.List;

 import cs444.lexer.Token;
 import cs444.parser.symbols.ANonTerminal;
 import cs444.parser.symbols.ATerminal;
 import cs444.parser.symbols.ISymbol;
 import cs444.parser.symbols.JoosNonTerminal;
 import cs444.parser.symbols.ast.expressions.NegOpExprSymbol;
 import cs444.parser.symbols.ast.expressions.NotOpExprSymbol;
 import cs444.parser.symbols.ast.factories.ASTSymbolFactory;
 import cs444.parser.symbols.exceptions.IllegalModifierException;
 import cs444.parser.symbols.exceptions.OutOfRangeException;
 import cs444.parser.symbols.exceptions.UnsupportedException;
 
 public class UniOpExprFactory extends ASTSymbolFactory{
 
 
     @Override
     protected ISymbol convert(ISymbol from) throws OutOfRangeException, UnsupportedException, IllegalModifierException {
         if(!JoosNonTerminal.unaryExpressions.contains(from.getName().toUpperCase())) return from;
         ANonTerminal nonTerm = (ANonTerminal) from;
 
        List<ISymbol> children = nonTerm.children;
        ISymbol child = children.get(children.size()-1);
 
        for (int i = children.size() - 2; i >= 0 ; i--) {
            ATerminal type = (ATerminal)children.get(i);
            String typeName = type.getName().toUpperCase();
            if(typeName.equals(Token.Type.EXCLAMATION.toString())) child = new NotOpExprSymbol(child);
            else if(typeName.equals(Token.Type.MINUS.toString())) child = new NegOpExprSymbol(child);
            else throw new UnsupportedException("Unary operator " + typeName);
        }
        return child;
     }
 }
