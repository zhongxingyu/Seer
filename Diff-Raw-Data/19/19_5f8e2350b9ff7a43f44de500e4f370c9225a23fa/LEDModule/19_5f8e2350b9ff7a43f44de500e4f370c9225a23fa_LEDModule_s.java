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
		output.sendRGB(0, 0, 0);
 		try {
 			Thread.sleep(500);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
		stop = true;
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
