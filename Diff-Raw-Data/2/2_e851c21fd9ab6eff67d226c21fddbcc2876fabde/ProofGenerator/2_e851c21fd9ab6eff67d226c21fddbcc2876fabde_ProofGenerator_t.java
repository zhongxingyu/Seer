 package ru.georgeee.mathlogic.propositionalcalculus;
 
 import ru.georgeee.mathlogic.propositionalcalculus.expression.Expression;
 import ru.georgeee.mathlogic.propositionalcalculus.parser.token.TokenHolder;
 
 import java.util.ArrayList;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: georgeee
  * Date: 29.10.13
  * Time: 17:02
  * To change this template use File | Settings | File Templates.
  */
 public class ProofGenerator {
     Expression[] expressions;
     TokenHolder tokenHolder;
 
     public ProofGenerator(Expression[] expressions, TokenHolder tokenHolder) {
         this.expressions = expressions;
         this.tokenHolder = tokenHolder;
     }
 
     public ProofGenerator(String proofText, TokenHolder tokenHolder) {
         this(proofText.split("\n"), tokenHolder);
     }
 
     public ProofGenerator(String[] lines, TokenHolder tokenHolder) {
         ArrayList<Expression> expressions = new ArrayList<Expression>(lines.length);
         for (String line : lines) {
             line = Main.removeComments(line).trim();
             if (!line.isEmpty()) {
                 expressions.add(tokenHolder.getExpressionCompiler().compile(line));
             }
         }
         this.expressions = new Expression[expressions.size()];
//        System.out.println(expressions.get(expressions.size()-1)+" -> "+expressions.size());
         expressions.toArray(this.expressions);
     }
 
     public boolean addToProof(Proof proof, Map<String, Expression> substitution) {
         boolean failed = false;
         for (Expression expression : expressions) {
             failed |= (proof.addCheckTautology(expression.replaceVarsWithExpressions(substitution)) == null);
             if (failed) break;
         }
         return !failed;
     }
 }
