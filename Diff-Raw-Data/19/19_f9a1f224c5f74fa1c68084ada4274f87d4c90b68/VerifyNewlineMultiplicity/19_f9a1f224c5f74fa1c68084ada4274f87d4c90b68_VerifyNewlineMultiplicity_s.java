 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package verifiers;
 
 import java.util.ArrayList;
 import verifiers.util.VerifyUtils;
 import verifiers.util.VerifyUtils.PairIdx;
 
 /**
  * Verify that no data is sent prior to receiving EOL and that the last chunk received 
  * is identical with the last chunk sent before last EOL. Sent buffer might contain additional data
  * after the last EOL but this data will be ignored.
  * @author visoft
  */
 public class VerifyNewlineMultiplicity extends AbstractVerifier {
     public VerifyNewlineMultiplicity() {
         super("Verify if the read data respects the \"echo back at newline\" specification. It checks the data before the last \\n");
     }
     
     
     @Override
     protected String doVerification() {
        return VerifyUtils.verifyNewlineMultiplicity(RWResult.getIn_buff(), RWResult.getOut_buff());
     }    
 }
