 package verifier.smtlib.z3;
 
 import java.util.LinkedList;
 
 import ast.Position;
 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 
 import ast.ASTRoot;
 
 import misc.Pair;
 
 import verifier.KindOfProgram;
 import verifier.Verifier;
 
 import verifier.smtlib.SMTLibTranslator;
 
 public class Z3 extends Verifier {
     private SMTLibTranslator translator = new SMTLibTranslator();
 
     public Z3(String template) {
        super(template, "smt2");
     }
 
     /**
      * {@inheritDoc}
      * @see Verifier#translate(ASTRoot)
      */
     protected String translate(ASTRoot ast) {
         return translator.getWPTree(ast).toString();
     }
 
     /**
      * Returns a list of program descriptions.
      * @return list of program descriptions
      */
     public LinkedList<Pair<KindOfProgram, Position>> getDescriptions() {
         return translator.getDescriptions();
     }
 
     /**
      * {@inheritDoc}
      * @see Verifier#parseVerifierOutput(String)
      */
     public LinkedList<Pair<Boolean,String>> parseVerifierOutput(String verifierOutput) {
         CharStream in = new ANTLRStringStream(verifierOutput.toString());
         z3Lexer lexer = new z3Lexer(in);
         CommonTokenStream tokens = new CommonTokenStream();
         tokens.setTokenSource(lexer);
         z3Parser parser = new z3Parser(tokens);
         LinkedList<Pair<Boolean, String>> result = new LinkedList<Pair<Boolean, String>>();
         try {
             result = parser.start();
         } catch (RecognitionException e) {
             e.printStackTrace();
         }
         return result;
     }
 }
