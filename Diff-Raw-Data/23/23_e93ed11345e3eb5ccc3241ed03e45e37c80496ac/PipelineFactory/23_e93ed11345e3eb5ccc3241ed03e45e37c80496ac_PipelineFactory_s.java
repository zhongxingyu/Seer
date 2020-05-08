 package wiitracker.driver;
 
 import wiitracker.fingertracking.FingerLabeler;
 import wiitracker.fingertracking.IrCameraNotifier;
 import motej.Mote;
 
 public class PipelineFactory {
 
 	public PipelineFactory() {
 	}
 
 	public static IrCameraNotifier createPipe(Mote mote) {
 
 		FingerLabeler fingerLabel = new FingerLabeler();
		mote.addIrCameraListener(fingerLabel);
 		return fingerLabel;
 	}
 
 	
 
 }
