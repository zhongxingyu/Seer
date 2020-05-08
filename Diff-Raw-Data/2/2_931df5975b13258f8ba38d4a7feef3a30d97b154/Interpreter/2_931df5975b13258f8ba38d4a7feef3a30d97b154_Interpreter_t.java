 package project.phase2;
 
 import project.phase2.file.StringMatchOperations;
 import project.phase2.ll1parsergenerator.ASTNode;
 import project.phase2.structs.StringMatchList;
 import project.scangen.ScannerGenerator;
 
 import java.io.*;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 public class Interpreter {
     private class Variable {
         final public Object val;
 
         public Variable(Object val) {
             this.val = val;
         }
 
         @Override
         public String toString() {
             return this.val.toString();
         }
     }
 
     private class MiniRERuntimeException extends RuntimeException {
         public MiniRERuntimeException(final String message) {
             super(message);
         }
     }
 
     private final MiniREParser parser;
     private final Map<String, Variable> varTable;
 
     public Interpreter(MiniREParser parser) {
         this.parser = parser;
         varTable = new HashMap<String, Variable>();
     }
 
     public void interpret() throws ParseException {
         ASTNode<String> root = parser.parse().getRoot();
         ASTNode<String> minire_program = root.get(0);
         statement_list(minire_program.get(1));
     }
 
     private String fromQuotedString(final String asciiString) {
         return asciiString.substring(1, asciiString.length() - 1);
     }
 
     public void statement_list(final ASTNode<String> statement_list) {
         ASTNode<String> statement = statement_list.get(0);
 
         if (statement.getChildren().size() == 0) {
             return;
         }
 
         String nextTokenType = statement.get(0).getValue();
 
         if (nextTokenType.equals("ID")) {
             assignment(statement);
         } else if (nextTokenType.equals("REPLACE")) {
             replace(statement, false);
         } else if (nextTokenType.equals("RECURSIVEREPLACE")) {
             replace(statement, true);
         } else if (nextTokenType.equals("PRINT")) {
             print(statement.get(2));
         }
 
         statement_list(statement_list.get(1));
     }
 
     private void assignment(ASTNode<String> statement) {
         String id = statement.get(0).get(0).getValue();
 
         if (statement.get(2).getValue().equals("OCTOTHORPE")) {
             Variable foo = expression(statement.get(3));
             if (foo.val instanceof Integer) {
                 varTable.put(id, foo);
             } else {
                 varTable.put(id, new Variable(((StringMatchList) foo.val).size()));
             }
         } else {
             varTable.put(id, expression(statement.get(2)));
         }
     }
 
     private void print(ASTNode<String> exp_list) {
         System.out.println(expression(exp_list.get(0)));
 
         if (exp_list.getChildren().size() > 1) {
             print(exp_list.get(2));
         }
     }
 
     private Variable expression(ASTNode<String> exp) {
         ASTNode<String> toke = exp.get(0);
 
         if (toke.getValue().equals("ID")) {
             return varTable.get(toke.get(0).getValue());
         } else if (toke.getValue().equals("OPEN-PAREN")) {
             return expression(toke.get(1));
         } else if (toke.getValue().equals("term")) {
             StringMatchList res = term(toke);
             ASTNode<String> exp_tail = exp.get(1);
             return new Variable(expression_tail(res, exp_tail));
         } else {
             throw new RuntimeException();
         }
     }
 
     private StringMatchList expression_tail(StringMatchList res, ASTNode<String> exp_tail) {
         if (exp_tail.getChildren().size() == 1) {
             return res;
         }
 
         StringMatchList stuff = term(exp_tail.get(1));
         StringMatchList next = expression_tail(stuff, exp_tail.get(2));
 
         String op = exp_tail.get(0).get(0).getValue();
 
         if (op.equals("DIFF")) {
             return res.difference(next);
         } else if (op.equals("UNION")) {
             return res.union(next);
         } else if (op.equals("INTERS")) {
             return res.intersection(next);
         } else {
             throw new RuntimeException();
         }
     }
 
     private StringMatchList term(ASTNode<String> term) {
         String regex = fromQuotedString(term.get(1).get(0).getValue());
         String filename = fromQuotedString(term.get(3).get(0).get(0).getValue());
         return StringMatchOperations.find(new File(filename), regex);
     }
 
     private void replace(ASTNode<String> statement, boolean recursive) {
         String regex = fromQuotedString(statement.get(1).get(0).getValue());
         String replaceText = fromQuotedString(statement.get(3).get(0).getValue());
 
        if (recursive && Pattern.compile(regex).matcher(replaceText).find()) {
             throw new MiniRERuntimeException(String.format("Replacement text `%s' must not match regex `%s'.",
                     replaceText, regex));
         }
 
         String srcFile = fromQuotedString(statement.get(5).get(0).get(0).get(0).getValue());
         String dstFile = fromQuotedString(statement.get(5).get(2).get(0).get(0).getValue());
         StringMatchOperations.replace(regex, replaceText, new File(srcFile), new File(dstFile), recursive);
     }
 
     public static void main(String[] args) {
         if (args.length != 1) {
             System.err.println("Parameters: <program-file>");
             System.exit(1);
         }
 
         String programFilePath = args[0];
 
         ScannerGenerator scannerGenerator = null;
 
         try {
             InputStream specFileInputStream = new ByteArrayInputStream(MiniRESpec.spec.getBytes());
             InputStream programFileInputStream = new FileInputStream(programFilePath);
             scannerGenerator = new ScannerGenerator(specFileInputStream, programFileInputStream);
         } catch (FileNotFoundException ex) {
             System.err.println(ex);
             System.exit(1);
         }
 
         MiniREParser parser = new MiniREParser(scannerGenerator);
         Interpreter interpreter = new Interpreter(parser);
 
         try {
             interpreter.interpret();
         } catch (ParseException ex) {
             System.out.println(String.format("%s (%s:%d:%d)", ex, programFilePath, ex.getToken().line,
                     ex.getToken().pos));
             System.exit(2);
         } catch (MiniRERuntimeException ex) {
             System.out.println(ex);
             System.exit(3);
         }
     }
 }
