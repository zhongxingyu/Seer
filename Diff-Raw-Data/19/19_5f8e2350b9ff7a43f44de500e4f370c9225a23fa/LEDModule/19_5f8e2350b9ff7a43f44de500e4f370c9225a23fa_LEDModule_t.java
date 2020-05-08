 package modules;
 
 import io.LEDOutput;
 
 public abstract class LEDModule implements Module {
 	protected LEDOutput output;
 	protected boolean stop;
 	protected boolean pause;
 	
 	public LEDModule(LEDOutput output){
 		this.output = output;
 		stop = false;
 		pause = false;
 	}
 	
 	@Override
 	public void stop() {
	
 		try {
			stop = true; //important that this is set before sendRGB(0,0,0), so the run method terminates
			Thread.sleep(200);
			output.sendRGB(0, 0, 0);
 			Thread.sleep(500);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
		
 	}
 
 	@Override
 	public void resume() {
 		pause=false;
 	}
 
 	@Override
 	public void pause() {
 		pause=true;
 	}
 }
