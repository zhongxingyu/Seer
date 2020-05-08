 package de.skuzzle.polly.parsing.tree;
 
 import java.util.ArrayList;
 import java.util.Arrays;
import java.util.Iterator;
 import java.util.List;
 import java.util.Stack;
 
 import de.skuzzle.polly.parsing.Context;
 import de.skuzzle.polly.parsing.ExecutionException;
 import de.skuzzle.polly.parsing.Parameter;
 import de.skuzzle.polly.parsing.ParseException;
 import de.skuzzle.polly.parsing.Position;
 import de.skuzzle.polly.parsing.Type;
 
 
 
 public class FunctionDefinition extends Expression implements Cloneable {
 
     private static final long serialVersionUID = 1L;
     
     private IdentifierLiteral name;
     protected Expression rhs;
     private List<Parameter> formalParameters;
     protected List<Expression> actualParameters;
     
     
     public FunctionDefinition(IdentifierLiteral name, Expression rhs, 
             List<Parameter> formalParameters) {
         super(Position.EMPTY);
         
         this.name = name;
         this.rhs = rhs;
         this.formalParameters = formalParameters;
         this.actualParameters = new ArrayList<Expression>();
     }
     
     
     
     public FunctionDefinition(IdentifierLiteral name, Expression rhs) {
         this(name, rhs, new ArrayList<Parameter>());
     }
     
     
     
     public FunctionDefinition(IdentifierLiteral name) {
         this(name, null, new ArrayList<Parameter>());
     }
     
     
     
     public FunctionDefinition(String name, Expression rhs, Parameter...formalParameters) {
         this(new IdentifierLiteral(name), rhs, Arrays.asList(formalParameters));
     }
     
     
     
     public IdentifierLiteral getName() {
         return this.name;
     }
     
     
     
     public List<Parameter> getFormalParameters() {
         return this.formalParameters;
     }
     
     
     
     public void setActualParameters(List<Expression> actualParameters) {
         this.actualParameters = actualParameters;
     }
     
     
     
     /*@Override
     public Expression contextCheck(Context context) throws ParseException {
         throw new ParseException("Context check on FunctionDefinition: '" + this.getName() + "'. This is a known bug!", Position.EMPTY);
     }*/
     
     
     
     @Deprecated
     public FunctionDefinition match(IdentifierLiteral name, List<Expression> actualParameters) {
         if (!this.name.equals(name)) {
             return null;
         }
         
         if (this.formalParameters.size() != actualParameters.size()) {
             return null;
         }
         
         for (int i = 0; i < this.formalParameters.size(); ++i) {
             Parameter formal = this.formalParameters.get(i);
             Expression actual = actualParameters.get(i);
             
             if (formal.getType().check(actual.getType())) {
                 return null;
             }
         }
         
         return this;
     }
     
     
     
     @Override
     public Expression contextCheck(Context context) throws ParseException {
         //Declarations local = new Declarations();
         context.getCurrentNamespace().enter();
         
         if (this.formalParameters.size() != this.actualParameters.size()) {
             throw new ParseException("Falsche Parameteranzahl: " + 
                     this.actualParameters.size() + ". Erwartet: " + 
                     this.formalParameters.size(), this.getPosition());
         }
         
         for (int i = 0; i < this.formalParameters.size(); ++i) {
             Parameter formal = this.formalParameters.get(i);
             Expression actual = actualParameters.get(i);
             
             if (!formal.getType().check(actual.getType())) {
                 Type.typeError(actual.getType(), formal.getType(), actual.getPosition());
             }
             
             actual.setType(formal.getType());
             //local.add(formal.getName(), actual);
             context.getCurrentNamespace().add(formal.getName(), actual);
         }
         
         /*
          * If this is a hard coded FunctionDefinition 
          */
         if (this.rhs == null) {
             return this;
         }
         
         /*
          * Create new local context.
          */
         //Declarations d = context.getDeclarations().join(local);
         //Context c = new Context(d);
         
         Expression functionExpression = (this.rhs.contextCheck(context));
         context.getCurrentNamespace().leave(); 
         this.setType(functionExpression.getType());
         
         return functionExpression;
     }
     
  
     
     @Override
     public Object clone() {
         FunctionDefinition func = new FunctionDefinition(
                 (IdentifierLiteral) this.name.clone(), 
                 this.rhs == null ? null : (Expression) this.rhs.clone(), 
                 this.formalParameters);
         for (Expression e : this.actualParameters) {
             func.actualParameters.add((Expression) e.clone());
         }
         
         func.setType(this.getType());
         func.setPosition(this.getPosition());
         return func;
     }
 
     
 
     @Override
     public void collapse(Stack<Literal> stack) throws ExecutionException {
         System.err.println("fuckfuck");
         // do nothing;
     }
    
    
    
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(this.getType().getTypeName().getIdentifier());
        b.append(": ");
        b.append(this.name.getIdentifier());
        if (!this.formalParameters.isEmpty()) {
            b.append("(");
            Iterator<Parameter> it = this.formalParameters.iterator();
            while (it.hasNext()) {
                Parameter param = it.next();
                b.append(param.getType().getTypeName().getIdentifier());
                b.append(" ");
                b.append(param.getName().getIdentifier());
                if (it.hasNext()) {
                    b.append(", ");
                }
            }
            b.append(")");
        }
        return b.toString();
    }
 }
