 package project.phase2;
 
 import project.phase2.file.StringMatchOperations;
 import project.phase2.structs.StringMatchList;
 import project.scangen.ScannerGenerator;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 
 public class Interpreter {
     private static final String MINIRE_SPEC_PATH = "doc/minire_spec.txt";
 
     private final MiniREParser parser;
     private final Map<String, StringMatchList> varTable;
 
     public Interpreter(MiniREParser parser) {
         this.parser = parser;
         varTable = new HashMap<String, StringMatchList>();
     }
 
     public void interpret() throws ParseException {
         ASTNode<String> root = parser.parse().root;
         ASTNode<String> minire_program = root.get(0);
         statement_list(minire_program.get(1));
     }
 
     private String fromQuotedString(final String asciiString) {
         return asciiString.substring(1, asciiString.length() - 1);
     }
 
     public void statement_list(final ASTNode<String> statement_list) {
         ASTNode<String> statement = statement_list.get(0);
 
         if (statement.children.size() == 0) {
             return;
         }
 
         String nextTokenType = statement.get(0).value;
 
         if (nextTokenType.equals("ID")) {
             assignment(statement.get(0), statement.get(2));
         } else if (nextTokenType.equals("PRINT")) {
             print(statement.get(2));
         }
 
         statement_list(statement_list.get(1));
     }
 
     private void assignment(ASTNode<String> dest, ASTNode<String> exp) {
         String id = dest.get(0).value;
         varTable.put(id, expression(exp));
     }
 
     private void print(ASTNode<String> exp_list) {
         System.out.println(expression(exp_list.get(0)));
 
         if (exp_list.children.size() > 1) {
             print(exp_list.get(2));
         }
     }
 
     private StringMatchList expression(ASTNode<String> exp) {
         ASTNode<String> toke = exp.get(0);
 
         if (toke.value.equals("ID")) {
             return varTable.get(toke.get(0).value);
         } else if (toke.value.equals("OPEN-PAREN")) {
             return expression(toke.get(1));
         } else if (toke.value.equals("term")) {
             StringMatchList res = term(toke);
             ASTNode<String> exp_tail = exp.get(1);
             return expression_tail(res, exp_tail);
         } else {
             throw new RuntimeException();
         }
     }
 
     private StringMatchList expression_tail(StringMatchList res, ASTNode<String> exp_tail) {
         if (exp_tail.children.size() == 1) {
             return res;
         }
 
         StringMatchList stuff = term(exp_tail.get(1));
         StringMatchList next = expression_tail(stuff, exp_tail.get(2));
 
        String op = exp_tail.get(0).value;
 
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
         String regex = fromQuotedString(term.get(1).get(0).value);
         String filename = fromQuotedString(term.get(3).get(0).get(0).value);
         return StringMatchOperations.find(new File(filename), regex);
     }
 
     public static void main(String[] args) {
         if (args.length != 1) {
             System.err.println("Parameters: <program-file>");
             System.exit(1);
         }
 
         String programFilePath = args[0];
 
         ScannerGenerator scannerGenerator = null;
 
         try {
             InputStream specFileInputStream = new FileInputStream(MINIRE_SPEC_PATH);
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
             System.out.println(ex);
             System.exit(1);
         }
     }
 }
