 package de.skuzzle.polly.parsing.tree.functions;
 
 import de.skuzzle.polly.parsing.Type;
 import de.skuzzle.polly.parsing.declarations.FunctionDeclaration;
 import de.skuzzle.polly.parsing.declarations.VarDeclaration;
 import de.skuzzle.polly.parsing.tree.literals.IdentifierLiteral;
 
 
 public class BinomialFunction extends FunctionDeclaration {
 
     private static final long serialVersionUID = 1L;
     
     
     public BinomialFunction() {
         super(new IdentifierLiteral("bino"), true);
         this.getFormalParameters().add(new VarDeclaration(
                 new IdentifierLiteral("n"), Type.NUMBER));
         this.getFormalParameters().add(new VarDeclaration(
             new IdentifierLiteral("k"), Type.NUMBER));
         
        this.setExpression(new Functions.Binomial());
     }
 }
