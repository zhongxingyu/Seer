 package modules.led;
 import output.LEDOutput;
 import modules.*;
 
 public class Fade extends LEDModule implements Runnable{
 
 	private int fadeInterval; // in milliseconds
 	
 	/**
 	 * 
 	 * @param out
 	 * @param fadeinterval time to fade up in milliseconds
 	 */
 	public Fade(LEDOutput out, int fadeinterval){
 		super(out);
 		this.fadeInterval = fadeinterval;
 		stop = false;
 		pause = false;
 	}
 	
 	@Override
 	public void run() {
 		try {
 			while(!stop){
 				if(pause)
 					Thread.sleep(fadeInterval);
 				else{
 					for(int i = 0; i <= 254; i++){
 						if(stop) break;
						output.sendRGB(i, i, i);
 						Thread.sleep(fadeInterval/255);
 					}
 					for(int i = 254; i >= 0; i--){
 						if(stop) break;
						output.sendRGB(i, i, i);		
 						Thread.sleep(fadeInterval/255);			
 					}
 				}
 			}
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
