 package gr.teilar.dionysos;
 
 import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
 import android.os.Bundle;
 import android.view.Window;
import android.widget.TextView;
 
 public class AboutAppScreen extends Activity {
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.about_app_screen);
		
		try {
			((TextView) findViewById(R.id.version))
			.setText(
					getResources().getString(R.string.version) + 
					getPackageManager().getPackageInfo(getPackageName(), 0).versionName
			);
		} catch (NotFoundException e) {
			((TextView) findViewById(R.id.version))
			.setText(getResources().getString(R.string.version)+" 1.0");
		} catch (NameNotFoundException e) {
			((TextView) findViewById(R.id.version))
			.setText(getResources().getString(R.string.version)+" 1.0");
		}
 	}
 }
