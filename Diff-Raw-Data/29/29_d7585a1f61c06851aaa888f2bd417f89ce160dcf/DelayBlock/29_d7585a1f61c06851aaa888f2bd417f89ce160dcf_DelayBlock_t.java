 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package plcstatecharteditor;
 
 /**
  *
  * @author huangkf
  */
public class DelayBlock extends CodeBlock {
     private long delaytime = 0;
     
     
     public DelayBlock() {
         this.uid = UIDGenerator.getNext();
         this.ctype = CodeType.Delay;
     }
     
     public void setDelayTime(long newtime)
     {
         delaytime = newtime;
     }
     
     public long getDelayTime()
     {
         return delaytime;
     }
     
     public String getStringDelayTime()
     {
         return String.valueOf(delaytime);
     }
 
     @Override
     public String getTypeString() {
         return "DELAY";
     }
 
     @Override
     public String getCode() {
                 return getUIDStringLabel() + newline +
                "//////////////////////////////////////" + newline +
                "//        DELAY                     //" + newline +
                "//////////////////////////////////////" + newline +
                "delayms(" + String.valueOf(delaytime) + ");";
     }
 }
