 package net.tuschhcm.routercontrol.router;
 
 /**
  * Router implementation for a ShinyBow SB-5544
  * 
  */
 public class ShinyBow5544Router implements Router {
 
     /**
      * Create a new ShinyBow router using the given comm port.
      * 
      * @param portName Com port name
      */
     public ShinyBow5544Router(final String portName) {
         
     }
     
     @Override
     public void switchInput(int output, int input)
             throws IllegalArgumentException {
        System.out.println(">>> Switching output " + output + " to " + input); 
     }
 
     @Override
     public void setPower(boolean on) {
        System.out.println(">>> Setting power to " + on); 
     }
 
     @Override
     public void setLockControls(boolean enabled) {
       System.out.println(">>> Setting control lock to " + enabled); 
     }
 
 }
