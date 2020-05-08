 package wiitracker.driver;
 
 import wiitracker.fingertracking.FingerLabeler;
 import wiitracker.fingertracking.IrCameraNotifier;
import wiitracker.fingertracking.TransformNotifier;
 import motej.Mote;
 
 public class PipelineFactory {
 
 	public PipelineFactory() {
 	}
 
 	public static IrCameraNotifier createPipe(Mote mote) {
 
		TransformNotifier transformNotifier = TransformNotifier.getInstance();
		mote.addIrCameraListener(transformNotifier);
		
 		FingerLabeler fingerLabel = new FingerLabeler();
		transformNotifier.addIrCameraListener(fingerLabel);
		
 		return fingerLabel;
 	}
 
 	
 
 }
