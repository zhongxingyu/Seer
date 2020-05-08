 package verifier;
 
 import ast.ASTRoot;
 
 import java.io.IOException;
 import java.util.LinkedList;
 
 import ast.Position;
 import misc.Pair;
 import org.antlr.runtime.RecognitionException;
 import verifier.smtlib.z3.Z3;
 
 /**
  *
  */
 public class VerifierInterface {
     private Verifier verifier;
 
     public VerifierInterface(Verifier verifier) {
         this.verifier = verifier;
     }
 
     public String getVerifierOutput(ASTRoot ast) throws IOException, InterruptedException {
         return verifier.call(ast);
     }
 
     public LinkedList<Pair<Boolean, String>> verify(ASTRoot ast) throws IOException, InterruptedException, RecognitionException {
         String verifierResult = verifier.call(ast);
         return verifier.parseVerifierOutput(verifierResult);
     }
    
    public final void setTimeout(String timeout) {
        this.verifier.timeout = timeout;
    }

    public final void setMemoryLimit(String memoryLimit) {
        this.verifier.memoryLimit = memoryLimit;
    }
 
     /**
      * Returns a list of program descriptions.
      * @return list of program descriptions
      */
     public LinkedList<Pair<KindOfProgram, Position>> getDescriptions() {
         return ((Z3) verifier).getDescriptions();
     }
 }
