 package authenticmdtest.implementation;
 
import com.undeadscythes.authenticmd.*;
import com.undeadscythes.tipscript.*;
 
 /**
  * @author UndeadScythes
  */
 public class TheProgram extends AuthentiCmd {
     public TheProgram() {
         super(System.in, new TipRedirect());
     }
 }
