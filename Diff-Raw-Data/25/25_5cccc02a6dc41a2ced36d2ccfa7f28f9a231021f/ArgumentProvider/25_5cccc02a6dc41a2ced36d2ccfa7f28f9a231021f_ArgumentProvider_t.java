 package org.hettoo.substitution;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class ArgumentProvider {
     private List<Argument> arguments;
     private List<String> actualArguments;

     private int minimumArguments;
     private int maximumArguments;
     private int variability;
 
     public ArgumentProvider(List<Argument> arguments,
             List<String> actualArguments) {
         this.arguments = arguments;
         this.actualArguments = actualArguments;
         minimumArguments = 0;
         maximumArguments = 0;
         variability = -1;
        boolean optional = false;
         for (int i = 0; i < arguments.size(); i++) {
             Argument argument = arguments.get(i);
             if (optional && !argument.isOptional()) {
                 System.err.println(
                         "All optional arguments must go at the end.");
                 System.exit(1);
             } else if (maximumArguments == -1) {
                 System.err.println(
                         "The variable argument must be the last one.");
                 System.exit(1);
             }
             if (argument.isOptional())
                 optional = true;
             else
                 minimumArguments++;
             if (argument.isVariable()) {
                 maximumArguments = -1;
                 variability = i;
             }
             if (maximumArguments != -1)
                 maximumArguments++;
         }
     }
 
     public boolean validate() {
         int size = actualArguments.size();
         return size >= minimumArguments
                 && (maximumArguments == -1 || size <= maximumArguments);
     }
 
     private int find(String name) {
         for (int i = 0; i < arguments.size(); i++) {
             if (arguments.get(i).getName() == name)
                 return i;
         }
         return -1;
     }
 
     public String getArgument(Argument argument) {
         int index = arguments.indexOf(argument);
         if (index < 0 || index >= actualArguments.size())
             return null;
         return actualArguments.get(index);
     }
 
     public List<String> getVariableArgument() {
         List<String> arguments = new ArrayList<String>();
         for (int i = 0; i < actualArguments.size() - this.arguments.size(); i++)
             arguments.add(actualArguments.get(variability + i));
         return arguments;
     }
 }
