 package yang.oculusheadtracking.samples;
 
import yang.model.App;
 import yang.model.PathSpecs;
import yang.oculusheadtracking.OculusSensor;
 import yang.pc.gles.YangGLESFrame;
 import yang.samples.statesystem.SampleStateSystem;
 import yang.systemdependent.YangSensor;
 
 public class SampleHeadTrackingMain {
 
 	public static void main(String[] args) {
 		PathSpecs.ASSET_PATH = "../yangAndroid/assets/";
		App.sensor = new OculusSensor();
 		final YangGLESFrame frame = new YangGLESFrame().init(1280,800);
 		frame.setSurface(new SampleStateSystem());
 		frame.mSurface.handleArgs(args,0);
 		frame.run();
 		frame.mSurface.waitUntilInitialized();
 //		frame.mSurface.mSensor.startSensor(YangSensor.TYPE_ROTATION_VECTOR);
 		frame.mSurface.mSensor.startSensor(YangSensor.TYPE_EULER_ANGLES);
 	}
 
 }
