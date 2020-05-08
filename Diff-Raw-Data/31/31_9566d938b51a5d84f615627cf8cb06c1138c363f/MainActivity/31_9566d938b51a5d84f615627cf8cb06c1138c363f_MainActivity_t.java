 package com.PsichiX.JustIDS;
 
 import com.PsichiX.XenonCoreDroid.XeActivity;
 import com.PsichiX.XenonCoreDroid.XeApplication;
 import com.PsichiX.XenonCoreDroid.XePhoton;
 import com.PsichiX.XenonCoreDroid.XeSense;
 import com.PsichiX.XenonCoreDroid.Framework.Graphics.Graphics;
 import com.PsichiX.XenonCoreDroid.Framework.Utils.Utils;
 
 public class MainActivity extends XeActivity
 {
 	public static XeApplication app;
 	RecorderService rs;
 	
 	@Override
 	public void onCreate(android.os.Bundle savedInstanceState) 
 	{
 		// setup application before running it
 		XeApplication.SETUP_SOUND_STREAMS = 1;
 		XeApplication.SETUP_WINDOW_HAS_TITLE = false;
 		XeApplication.SETUP_WINDOW_FULLSCREEN = true;
 		XeApplication.SETUP_SCREEN_ORIENTATION = XeApplication.Orientation.PORTRAIT;
 		XeApplication.SETUP_SENSORS_RATE = android.hardware.SensorManager.SENSOR_DELAY_GAME;
 		
 		// create application
 		super.onCreate(savedInstanceState);
 		
 		//obsluga mikrofonu
 		rs = new RecorderService();
 		rs.startRecording();
 
 		//obsluga wibracji
 		VibratorUtil.v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 		VibratorUtil.v.vibrate(1000);
 		
 		// run state
 		app = getApplicationCore();
 		Utils.initModule(getApplicationCore().getAssets());
 		Graphics.initModule(getApplicationCore().getAssets(), getApplicationCore().getPhoton());
 		getApplicationCore().getTimer().setFixedStep(1000 / 30);
 		getApplicationCore().getPhoton().getRenderer().getTimer().setFixedStep(1000 / 30);
 		getApplicationCore().getPhoton().setRenderMode(XePhoton.RenderMode.QUEUE, true);
 		getApplicationCore().getPhoton().getRenderer().setClearBackground(true, 0.0f, 0.0f, 0.0f, 1.0f);
 		getApplicationCore().getSense().use(XeSense.Type.LINEAR_ACCELERATION);
 		getApplicationCore().run(new GameState());
 		
 		//startActivity(new Intent(this, AudioRecordTest.class));
 	}
 }
