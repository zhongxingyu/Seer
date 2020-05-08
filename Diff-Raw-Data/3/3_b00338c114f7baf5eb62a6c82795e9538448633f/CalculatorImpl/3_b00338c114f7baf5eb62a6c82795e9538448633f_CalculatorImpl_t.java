 package de.frvabe.sample.calculator.ws;
 
import de.frvabe.sample.calculator.ws.Calculator;

 import de.frvabe.sample.calculator.types.Operator;
 import de.frvabe.sample.calculator.types.Result;
 import de.frvabe.sample.calculator.types.Term;
 import de.frvabe.sample.calculator.types.TermPart;
 
 public class CalculatorImpl implements Calculator {
 
   @Override
   public Result calculate(final Term term) {
 
     Result result = new Result();
     result.setValue(0d);
     
     // first term part has ADD operator per definition
     term.getPartList().get(0).setOperator(Operator.ADD);
  
     for (TermPart part : term.getPartList()) {
       switch (part.getOperator()) {
         case SUBTRACT:
           result.setValue(result.getValue() - part.getValue());
           break;
         case MULTIPLY:
           result.setValue(result.getValue() * part.getValue());
           break;
         case DIVIDE:
           result.setValue(result.getValue() / part.getValue());
           break;
         default:
           result.setValue(result.getValue() + part.getValue());
       }
     }
 
     return result;
 
   }
 }
