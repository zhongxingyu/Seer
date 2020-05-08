 /**
  * 
  */
 package xapn.javapro.foobarqix.dsl;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import xapn.javapro.foobarqix.dsl.command.ContainCommand;
 import xapn.javapro.foobarqix.dsl.command.DivideCommand;
 
 /**
  * FooBarQix DSL applicable to a numeric range.
  * 
  * @author Xavier Pigeon
  */
 public class EachNumber extends AbstractFooBarQixDSL {
     
     private static final String SEPARATOR = "\n";
     
     /**
      * Initiate a DSL-based phrase for each integer into the range.
      * 
      * @return a {@link Between} object
      */
     static Between eachNumber() {
        throw new RuntimeException("Not yet implemented");
     }
     
     /**
      * Initiate a DSL-based phrase for each integer into the range.
      * 
      * @param start the range start
      * @param end the end range
      * @return a {@link EachNumber} object
      */
     static EachNumber eachNumberBetween(int start, int end) {
         EachNumber eachNumber = new EachNumber();
         eachNumber.start = start;
         eachNumber.end = end;
         
         return eachNumber;
     }
     
     private int start;
     private int end;
     private ThisNumber thisNumber;
     private List<ICommand> commands;
     
     /**
      * Default constructor.
      */
     EachNumber() {
         thisNumber = new ThisNumber();
         commands = new ArrayList<ICommand>();
     }
     
     /**
      * Build the text resulting from the recorded command sequence, according to
      * the numeric range and the DSL rules.
      */
     private void build() {
         for (int current = start; current <= end; current++) {
             thisNumber.setNumber(current);
             for (ICommand command : commands) {
                 command.execute();
             }
             thisNumber.next();
             
             if (current < end) {
                 thisNumber.getSubstitution().append(SEPARATOR);
             }
         }
     }
     
     /**
      * Apply the textual transformation of the numeric range according to the
      * DSL rules if the range numbers contain one of the given digits.
      * 
      * @param digits the digits that will cause the transformation if a number
      *            contains one of them
      * @return the {@link EachNumber} object for chaining method calls
      */
     public EachNumber contains(int... digits) {
         commands.add(new ContainCommand(thisNumber, digits));
         return this;
     }
     
     /**
      * Get the final text.
      * 
      * @return the result of the textual transformation
      */
     public String fooBarQix() {
         build();
         return thisNumber.getTextAndClean();
     }
     
     /**
      * Apply the textual transformation of the numeric range according to the
      * DSL rules if the range numbers are divisible by a given divisor.
      * 
      * @param divisor the divisor by which the numbers will be attempted
      *            to divide
      * @return the {@link EachNumber} object for chaining method calls
      */
     public EachNumber isDivisibleBy(int divisor) {
         commands.add(new DivideCommand(thisNumber, divisor));
         return this;
     }
     
     @Override
     public String toString() {
         StringBuilder to = new StringBuilder();
         to.append("From: " + start);
         to.append(", to: " + end);
         
         for (Entry<Integer, String> rule : rules.entrySet()) {
             to.append("\n\t").append(rule.getKey()).append(" => ").append(rule.getValue());
         }
         for (ICommand command : commands) {
             to.append("\n\t").append(command.toString());
         }
         
         return to.toString();
     }
     
     /**
      * Change the rules that provide the string replacement for a given number.
      * 
      * @param rules the rules to be applied
      * @return the {@link ThisNumber} object for chaining method calls
      */
     public EachNumber withRules(Map<Integer, String> rules) {
         thisNumber.withRules(rules);
         return this;
     }
 }
