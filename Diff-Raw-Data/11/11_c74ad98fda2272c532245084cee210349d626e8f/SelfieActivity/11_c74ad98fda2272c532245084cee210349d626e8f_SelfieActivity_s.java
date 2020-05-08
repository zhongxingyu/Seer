 package info.guardianproject.justpayphone.app;
 
 import info.guardianproject.justpayphone.R;
import info.guardianproject.justpayphone.utils.Constants.App;
 import info.guardianproject.justpayphone.utils.Constants.Codes.Extras;
 
 import java.io.File;
 import java.io.IOException;
import java.util.logging.Logger;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.hardware.Camera;
 import android.hardware.Camera.CameraInfo;
 import android.os.Bundle;
 import android.widget.TextView;
 
 public class SelfieActivity extends SurfaceGrabberActivity {
	private final static String LOG = App.LOG;
 	
 	public SelfieActivity() {
 		super();
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		if (getIntent().getBooleanExtra(info.guardianproject.justpayphone.utils.Constants.Codes.Extras.IS_SIGNING_OUT, false))
 		{
 			((TextView)findViewById(R.id.tvTakeYourPhoto)).setText(R.string.take_your_photo_to_clock_out);
 		}
 	}
 
 	@Override
 	protected int getLayout()
 	{
 		return R.layout.activity_selfie;
 	}
 	
 	@Override
 	protected int getCameraDirection() {
 		return CameraInfo.CAMERA_FACING_FRONT;
 	}
 
 	@Override
 	public void onPictureTaken(final byte[] data, Camera camera) {		
 		File tempFile;
 		try {
 			tempFile = new File(IOUtility.buildPublicPath(new String[] { System.currentTimeMillis() + "_selfie.jpg" }));
 			InformaCam.getInstance().ioService.saveBlob(data, tempFile, true);
 			Logger.d(LOG, "NEW SELFIE AT : " + tempFile.getAbsolutePath());
 			setResult(Activity.RESULT_OK, new Intent().putExtra(Extras.PATH_TO_FILE, tempFile.getAbsolutePath()));
 			finish();
 		} catch (IOException e) {
 			e.printStackTrace();
 			setResult(Activity.RESULT_CANCELED);
 			finish();
 		}
 		finish();
 	}
 
 	
 }
