 package interpreter;
 
 
 
 public class Main {
 
     public static void main(String args[]) {
         Parser parser = new Parser("src/token_spec.txt", "src/script.txt");
         SyntaxTreeNode head = parser.parse();
         head.printLevels();
         System.out.println();
        System.out.println("Starting evaluation.");
         Evaluator evaluator = new Evaluator();
         evaluator.eval(head);
     }
 
 
 }
