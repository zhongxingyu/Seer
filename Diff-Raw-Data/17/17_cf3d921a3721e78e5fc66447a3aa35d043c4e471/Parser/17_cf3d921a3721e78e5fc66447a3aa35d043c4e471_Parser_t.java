 package malice;
 
 import malice.symbols.SymbolTable;
 import malice.symbols.Type;
 import malice.expressions.Expression;
 import malice.expressions.CharacterExpression;
 import malice.expressions.ArithmeticExpression;
 import malice.commands.SpeakCommand;
 import malice.commands.IncrementCommand;
 import malice.commands.DecrementCommand;
 import malice.commands.VariableDeclarationCommand;
 import malice.commands.VariableAssignmentCommand;
 import malice.commands.Command;
 import java.util.ArrayList;
 import java.util.List;
import java.util.Set;
 import org.antlr.runtime.tree.Tree;
 
 public class Parser {
 
     private List<Command> commands;
     private SymbolTable symbolTable;
 
     public Parser() {
         commands = new ArrayList<Command>();
         symbolTable = new SymbolTable();
     }
 
     public List<Command> getCommands() {
         return commands;
     }
 
     public SymbolTable getSymbolTable() {
         return symbolTable;
     }
 
     public void parseProg(Tree tree) {
         tree = tree.getChild(0);
         for (int i = 0; i < tree.getChildCount(); i++) {
             Tree child = tree.getChild(i);
             parseStatement(child);
         }
         int breakpoint = 1;
     }
 
     private void parseStatement(Tree tree) {
         for (int i = 0; i < tree.getChildCount(); i++) {
             Tree child = tree.getChild(i);
 
             if ("command".equals(child.getText())) {
                 parseCommand(child);
             }
         }
     }
 
     private void parseCommand(Tree tree) {
         Tree commandTree = tree.getChild(0);
         String commandName = commandTree.getText();
 
         if ("variable_declaration".equals(commandName)) {
             commands.add(parseVariableDeclaration(commandTree));
         } else if ("variable_assignment".equals(commandName)) {
             commands.add(parseVariableAssignment(commandTree));
         } else {
             commands.add(parseProcedure(tree));
         }
     }
 
     private Command parseVariableDeclaration(Tree tree) {
         String variableName = tree.getChild(0).getText();
         Type variableType = Type.valueOf(tree.getChild(2).getText());
 
         if (symbolTable.containsVariable(variableName)) {
             throw new VariableAlreadyDeclaredException(variableName);
         }
 
         symbolTable.addVariable(variableName, variableType);
 
         return new VariableDeclarationCommand(variableName, variableType);
     }
 
     private Command parseVariableAssignment(Tree tree) {
         String variableName = tree.getChild(0).getText();
 
         if (!symbolTable.containsVariable(variableName)) {
             throw new VariableNotDeclaredException(variableName);
         }
 
         String expressionText = tree.getChild(2).getChild(0).getText();
         Expression expression = null;
 
         Type variableType = symbolTable.getVariableType(variableName);
         if ('\'' == expressionText.charAt(0)) {
             // character expression
             if (Type.letter != variableType) {
                 throw new IncompatibleTypeException(variableName, variableType);
             }
 
             expression = new CharacterExpression(expressionText.charAt(1));
         } else {
             // arithmetic expression
             if (Type.number != variableType) {
                 throw new IncompatibleTypeException(variableName, variableType);
             }
 
             expression = parseArithmeticExpression(tree.getChild(2));
            
            Set<String> variablesUsed = expression.getUsedVariables();
            for (String variableUsed : variablesUsed) {
                System.out.println("XXX - " + variableUsed + " " + expression);
            }
            
            
            for (String variableUsed : variablesUsed) {
                if (!symbolTable.containsVariable(variableUsed)) {
                    throw new VariableNotDeclaredException(variableUsed + variableName);
                }
                if (!symbolTable.isInitialisedVariable(variableUsed) && !variableName.equals(variableUsed)) {
                    throw new VariableNotInitialisedException(variableUsed);
                }
            }
         }
         
         symbolTable.initialiseVariable(variableName);
 
         return new VariableAssignmentCommand(variableName, expression);
     }
 
     private Command parseProcedure(Tree tree) {
         String variableName = tree.getChild(0).getText();
         
         if (!symbolTable.containsVariable(variableName)) {
             throw new VariableNotDeclaredException(variableName);
         }
         if (!symbolTable.isInitialisedVariable(variableName)) {
             throw new VariableNotInitialisedException(variableName);
         }
         
         String procedureName = tree.getChild(1).getText();
 
         if ("ate".equals(procedureName)) {
             return new IncrementCommand(variableName);
         } else if ("drank".equals(procedureName)) {
             return new DecrementCommand(variableName);
         } else if ("spoke".equals(procedureName)) {
             return new SpeakCommand(variableName);
         } else {
             // in case of syntax connectors such as and
             return null;
         }
     }
 
     private ArithmeticExpression parseArithmeticExpression(Tree tree) {
 
         if (tree.getText().equals("factor")) {
             boolean tilda = false;
             String first = tree.getChild(0).getText();
             String terminal;
             if(first.equals("~")) {
                 tilda = true;
                 terminal = tree.getChild(1).getText();
             } else {
                 terminal = tree.getChild(0).getText();
             }
             try {
                 int value = Integer.valueOf(terminal);
                 return new ArithmeticExpression(value,tilda);
             }
             catch (Throwable e) {
                 return new ArithmeticExpression(terminal,tilda);
             }
             
         } else {
             Tree leftExp = tree.getChild(0);
             if (tree.getChildCount() > 1) {
                 ArithmeticExpression rightExp = parseArithmeticExpression(leftExp);
                 for(int i = 2; i < tree.getChildCount(); i += 2) {
                     leftExp = tree.getChild(i);
                     rightExp = new ArithmeticExpression(rightExp,parseArithmeticExpression(leftExp), tree.getChild(i-1).toString().charAt(0));
                 }
                 return rightExp;
             }
             return parseArithmeticExpression(leftExp);
         }
 
     }
 
     @Override
     public String toString() {
         StringBuilder builder = new StringBuilder();
         for (Command command : commands) {
             builder.append(command);
             builder.append('\n');
         }
         return builder.toString();
     }
 
     public static class VariableAlreadyDeclaredException extends RuntimeException {
 
         public VariableAlreadyDeclaredException(String variableName) {
             super(variableName + " was already declared");
         }
     }
 
     public static class VariableNotDeclaredException extends RuntimeException {
 
         public VariableNotDeclaredException(String variableName) {
             super(variableName + " was not declared");
         }
     }
     
     public static class VariableNotInitialisedException extends RuntimeException {
 
         public VariableNotInitialisedException(String variableName) {
             super(variableName + " was not initialised");
         }
     }
     
     public static class IncompatibleTypeException extends RuntimeException {
 
         public IncompatibleTypeException(String variableName, Type type) {
             super(variableName + " can only be assigned a value of type " + type);
         }
     }
 }
