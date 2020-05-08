 import com.relayClient.raspi.PowerController;
 
 public class Runner
 {
    public static void main(String args)
     {
         // create controller instance and start it up
         PowerController pc = new PowerController();
         
         while(true) {
             System.out.println("");
         	System.out.println("Turn Relay ON/OFF?");
   
 	        String event = System.console().readLine();
 	        
 	        if (event == "On" || event == "on") {
 	        	pc.turnOn();
 	        }
 	        
 	        if (event == "Off" || event == "off") {
 	        	pc.turnOff();
 	        }
         }
     }
 }
