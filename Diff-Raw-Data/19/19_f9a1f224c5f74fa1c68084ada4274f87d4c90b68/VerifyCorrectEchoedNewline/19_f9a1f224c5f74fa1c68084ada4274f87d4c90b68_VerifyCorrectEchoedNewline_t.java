 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package verifiers;
 
 import java.util.ArrayList;
import java.util.Arrays;
 import java.util.TreeSet;
 import pa3_test.ThreadedReadWrite;
 import verifiers.util.VerifyUtils;
 
 /**
  * Verifier that checks if the data between newlines is echoed correct.
  * The newlines are matched and the buffers between them is checked.
  * The reference buffer is the sent buffer, so the threadID's are ignored at this step.
  * If the content is ok, the method collects the threadID's and prints them in the output.
  * The method will output the set of ID's, not each ID that was met.
  * @author visoft
  */
 public class VerifyCorrectEchoedNewline extends AbstractVerifier{
 
     public VerifyCorrectEchoedNewline() {
         super("Verify that the content returned after each EOL is correct. Collect thread ID's if they exist.");
     }
     public VerifyCorrectEchoedNewline(ThreadedReadWrite RW) {
         this();
         RWResult=RW;
     }
     @Override
     protected String doVerification() {
         byte[] rec_buf,sent_buf;
        rec_buf=Arrays.copyOf(RWResult.getIn_buff(), RWResult.getTotalReadBytes());
        sent_buf=Arrays.copyOf(RWResult.getOut_buff(), RWResult.getTotalWrittenBytes());
         String rez;
         rez=VerifyUtils.verifyNewlineEchoedCorrect(rec_buf,sent_buf);
         //get out if errors
         if(!rez.contains("All Ok!"))
             return rez;
         //Count the newlines
         ArrayList<Integer> recNL=VerifyUtils.markNewlines(rec_buf);
         
         //Collect thread ID's
         ArrayList<String> id=VerifyUtils.collectThreadID(rec_buf, sent_buf);
         TreeSet<String> keys=new TreeSet<>(id);
         
         rez="All Ok! Received newlines: "+recNL.size()+" Received ThreadID's: "+id.size()+ " Detected set of thread ID's: ";
         for(String s:keys){
             rez+=s+" ";
         }
         
         return rez;
     }
     
 }
