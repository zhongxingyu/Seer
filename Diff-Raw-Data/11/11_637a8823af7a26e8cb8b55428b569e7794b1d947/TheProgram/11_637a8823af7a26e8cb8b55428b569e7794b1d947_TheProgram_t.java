 package authenticmdtest.implementation;
 
import com.undeadscythes.authenticmd.AuthentiCmd;
import com.undeadscythes.tipscript.TipRedirect;
 
 /**
  * @author UndeadScythes
  */
 public class TheProgram extends AuthentiCmd {
     public TheProgram() {
         super(System.in, new TipRedirect());
     }
 }
