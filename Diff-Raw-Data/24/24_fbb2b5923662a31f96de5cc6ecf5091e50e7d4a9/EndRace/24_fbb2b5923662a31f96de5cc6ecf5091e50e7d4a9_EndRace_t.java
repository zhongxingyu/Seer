 package p0_Race;
 
 import general.ClaudisMain;
 import general.SensorCache;
 import lejos.robotics.subsumption.Behavior;
 
 public class EndRace implements Behavior {
 	
 
 	@Override
 	public boolean takeControl() {
 		// first stripe of barcode detected
 		return (SensorCache.getInstance().normalizedLightValue > 400);
 	}
 
 	@Override
 	public void action() {
 		System.out.println("Race Ende");
 		ClaudisMain.searchBarcode();
 	}
 
 	@Override
 	public void suppress() {
 		
 	}
 
 }
