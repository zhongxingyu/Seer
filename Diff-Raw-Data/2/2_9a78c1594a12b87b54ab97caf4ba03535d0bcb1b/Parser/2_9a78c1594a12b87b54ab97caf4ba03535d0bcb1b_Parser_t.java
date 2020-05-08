 package malice;
 
 import malice.commands.SpeakCommand;
 import malice.commands.IncrementCommand;
 import malice.commands.DecrementCommand;
 import malice.commands.VariableDeclarationCommand;
 import malice.commands.VariableAssignmentCommand;
 import malice.commands.Command;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.antlr.runtime.tree.Tree;
 
 public class Parser {
 
     private List<Command> commands;
     private Map<String, Symbol> symbolTable;
 
     public Parser() {
         commands = new ArrayList<Command>();
         symbolTable = new HashMap<String, Symbol>();
     }
 
     @Override
     public String toString() {
         StringBuilder b = new StringBuilder();
         for (Command command : commands) {
             b.append(command);
             b.append('\n');
         }
         return b.toString();
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
         
         if (symbolTable.containsKey(variableName)) {
             throw new VariableAlreadyDeclaredException(variableName);
         }
         
         symbolTable.put(variableName, new Symbol(variableType));
 
         return new VariableDeclarationCommand(variableName, variableType);
     }
 
     private Command parseVariableAssignment(Tree tree) {
         String variableName = tree.getChild(0).getText();
         
         if (!symbolTable.containsKey(variableName)) {
             throw new VariableNotDeclaredException(variableName);
         }
 
         String expressionText = tree.getChild(2).getChild(0).getText();
         Expression expression = null;
 
         if ('\'' == expressionText.charAt(0)) {
             // character expression
             Type variableType = symbolTable.get(variableName).getType();
             if (Type.letter != variableType) {
                 throw new IncompatibleTypeException(variableName, variableType);
             }
 
             expression = new CharacterExpression(expressionText.charAt(1));
         } else {
             // arithmetic expression
             Type variableType = symbolTable.get(variableName).getType();
             if (Type.number != variableType) {
                 throw new IncompatibleTypeException(variableName, variableType);
             }
 
             //TODO
 
             expression = new ArithmeticExpression("a");
         }
 
         return new VariableAssignmentCommand(variableName, expression);
     }
 
     private Command parseProcedure(Tree tree) {
         String variableName = tree.getChild(0).getText();
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
          Tree firstTerm = tree.getChild(0);
 
          if(firstTerm.getText().equals("factor")) {
              return new ArithmeticExpression(Integer.parseInt(firstTerm.getChild(0).getText()));
          } else if (firstTerm.getText().equals("expression")) {
              Tree Exp = firstTerm.getChild(0);
             ArithmeticExpression Exp2 = parseArithmeticExpression(Exp);
              for (int i = 3; i < firstTerm.getChildCount(); i += 2) {
                 Exp = firstTerm.getChild(i);
                 Exp2 = new ArithmeticExpression(Exp2,parseArithmeticExpression(Exp),'+');
              }
              return Exp2;
          } else {
              Tree left = firstTerm.getChild(0);
              Tree right = firstTerm.getChild(2);
              char op = firstTerm.getChild(1).getText().charAt(0);
              return new ArithmeticExpression(parseArithmeticExpression(left),parseArithmeticExpression(right),op);
          }
 
     }
     
     public static class VariableAlreadyDeclaredException extends RuntimeException {
         public VariableAlreadyDeclaredException(String variableName) {
             super(variableName + " was already declared");
         }
     }
     
     public static class VariableNotDeclaredException extends RuntimeException {
         public VariableNotDeclaredException(String variableName) {
             super(variableName + " was not declared and therefore cannot be assigned a value");
         }
     }
     
     public static class IncompatibleTypeException extends RuntimeException {
         public IncompatibleTypeException(String variableName, Type type) {
             super(variableName + " can only be assigned a value of type " + type);
         }
     }
 }
