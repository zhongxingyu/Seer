 package cs444.parser.symbols.ast.factories.expressions;
 
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
        ATerminal type = (ATerminal)nonTerm.children.get(0);
        String typeName = type.getName().toUpperCase();
        ISymbol child = nonTerm.children.get(1);
 
        if(typeName.equals(Token.Type.EXCLAMATION.toString())) return new NotOpExprSymbol(child);
        if(typeName.equals(Token.Type.MINUS.toString())) return new NegOpExprSymbol(child);
        throw new UnsupportedException("Unary operator " + typeName);
 
     }
 }
