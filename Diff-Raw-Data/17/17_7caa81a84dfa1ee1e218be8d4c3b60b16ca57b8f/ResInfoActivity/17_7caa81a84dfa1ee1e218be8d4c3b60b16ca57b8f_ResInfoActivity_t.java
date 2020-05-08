 package st.gaw.resinfo;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Point;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.view.Display;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.TextView;
 
 public class ResInfoActivity extends Activity {
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		@SuppressWarnings("deprecation")
 		final String apiVersion = android.os.Build.VERSION.SDK;
		String manufacturer = "";
 		try {
 			manufacturer = ApiLevel4.getBuildManufacturer()+" ";
		} catch (NoSuchMethodError e) {
 		} catch (VerifyError e) {
 		}
 
 		Display display = getWindowManager().getDefaultDisplay();
 		final Point screenSize = new Point();
 		try {
 			ApiLevel13.getScreenSize(this, screenSize);
		} catch (NoSuchMethodError e) {
			screenSize.x = display.getWidth();
			screenSize.y = display.getHeight();
 		} catch (VerifyError e) {
 			screenSize.x = display.getWidth();
 			screenSize.y = display.getHeight();
 		}
 
 		TextView device = (TextView) findViewById(R.id.textViewDevice);
 		final String deviceName = manufacturer + android.os.Build.MODEL + " (" + android.os.Build.DISPLAY + ")";
 		device.setText(deviceName);
 
 		TextView api = (TextView) findViewById(R.id.textViewAPI);
 		api.setText("API v" + apiVersion + " ("+android.os.Build.VERSION.RELEASE+")");
 
 		TextView size = (TextView) findViewById(R.id.textViewSize);
 		if (screenSize.y > screenSize.x)
 			size.setText(getString(R.string.res_size) + " (" + screenSize.y+"x"+screenSize.x+")");
 		else
 			size.setText(getString(R.string.res_size) + " (" + screenSize.x+"x"+screenSize.y+")");
 
 		TextView density = (TextView) findViewById(R.id.textViewDensity);
 		final DisplayMetrics metrics = new DisplayMetrics();
 		display.getMetrics(metrics);
 		density.setText(getString(R.string.res_density) + " ("+((int) (metrics.density*160))+" dpi)");
 
 		View share = findViewById(R.id.buttonShare);
 		share.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND); 
 				emailIntent.setType("text/plain"); 
 				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_subjet, deviceName));
 				StringBuilder shareText = new StringBuilder();
 
 				shareText.append("API v");
 				shareText.append(apiVersion);
 				shareText.append(" (");
 				shareText.append(android.os.Build.VERSION.RELEASE);
 				shareText.append(")\n");
 
 				if (screenSize.y > screenSize.x)
 					shareText.append(getString(R.string.share_size, getString(R.string.res_size).toLowerCase() + " (" + screenSize.y+"x"+screenSize.x+")"));
 				else
 					shareText.append(getString(R.string.share_size, getString(R.string.res_size).toLowerCase() + " (" + screenSize.x+"x"+screenSize.y+")"));
 				shareText.append("\n");
 
 				shareText.append(getString(R.string.share_density, getString(R.string.res_density).toLowerCase() + " ("+((int) (metrics.density*160))+" dpi)"));
 				shareText.append("\n");
 
 				shareText.append(getString(R.string.share_orientation, getString(R.string.res_orientation).toLowerCase()));
 				shareText.append("\n");
 
 				shareText.append(getString(R.string.share_uimode, getString(R.string.res_uimode).toLowerCase()));
 				shareText.append("\n");
 
 				shareText.append(getString(R.string.share_aspect, getString(R.string.res_aspect).toLowerCase()));
 				shareText.append("\n");
 
 				shareText.append(getString(R.string.share_touchscreen, getString(R.string.res_touchscreen).toLowerCase()));
 				shareText.append("\n");
 
 				shareText.append(getString(R.string.share_night, getString(R.string.res_night).toLowerCase()));
 				shareText.append("\n");
 
 				shareText.append(getString(R.string.share_navstate, getString(R.string.res_navstate).toLowerCase()));
 				shareText.append("\n");
 
 				shareText.append(getString(R.string.share_navmethod, getString(R.string.res_navmethod).toLowerCase()));
 				shareText.append("\n");
 
 				shareText.append(getString(R.string.share_keyboard, getString(R.string.res_keyboard).toLowerCase()));
 				shareText.append("\n");
 
 				shareText.append(getString(R.string.share_textinput, getString(R.string.res_textinput).toLowerCase()));
 				shareText.append("\n");
 
 				shareText.append("\nhttps://play.google.com/store/apps/details?id=st.gaw.resinfo");
 
 				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText.toString());
 				startActivity(Intent.createChooser(emailIntent, getString(R.string.share_subjet, android.os.Build.MODEL))); 
 			}
 		});
 	}
 }
