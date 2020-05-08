 package proj.Position;
 
 import java.io.File;
 
 import ntu.com.google.zxing.client.android.R;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class PositionActivity extends Activity {
 
 	private MyWebView mapView;
 	private Button scan;
 
 	private TextView pointTitle;
 	private String jsonURL;
 	private String mapID;
 	private String mapVer;
 	private String pointID;
 	private String title;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		findViews();
 		setListeners();
 		makeRootDir();
 		mapJsonDownload();
 		// startScan();
 		// Download.downImg();
 		// showImage();// For test showing map without scan (just press return)
 
 	}
 
 	private void findViews() {
 		pointTitle = (TextView) findViewById(R.id.plainText);
 
 		mapView = (MyWebView) findViewById(R.id.myWebView1);
 		// Enable to zoom in/out
 		mapView.getSettings().setBuiltInZoomControls(true);
 		// Enable to zoom in/out by double tap
 		mapView.getSettings().setUseWideViewPort(true);
 
 		scan = (Button) findViewById(R.id.scan);
 	}
 
 	private void setListeners() {
 		scan.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				startScan();
 			}
 		});
 	}
 
 	private void showImage() {
 
 		// Enable to zoom in/out
 
 		mapView.getSettings().setBuiltInZoomControls(true);
 		// Enable to zoom in/out by double tap
 		mapView.getSettings().setUseWideViewPort(true);
 
 		// Bind the image path
 		// String data =
 		// "<img src = \"file:///android_res/drawable/abc.png\" />";
 		// String data =
 		// "<img src = \"http:///indoorposition.appspot.com/image/map.jpg\" />";
 
 		String data = "<img src = \"file:///sdcard/somefile.jpg\" />";
 
 		final String mimeType = "text/html";
 		final String encoding = "utf-8";
 		mapView.loadDataWithBaseURL("about:blank", data, mimeType, encoding, "");
 
 	}
 
 	private void startScan() {
 		Intent intent = new Intent("ntu.com.google.zxing.client.android.SCAN");
 		intent.setPackage("ntu.com.google.zxing.client.android");
 		intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
 		startActivityForResult(intent, 0);
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		if (requestCode == 0) {
 			if (resultCode == RESULT_OK) {
 				String contents = intent.getStringExtra("SCAN_RESULT");
 				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
 
 				String[] contentArray = contents.split("\\?");
 				String[] contentPart = contentArray[1].split("&");
 
 				// Assign value
 				jsonURL = contents;
 				mapID = contentPart[0];
 				mapVer = contentPart[1];
 				pointID = contentPart[2];
 				title = contentPart[3];
 
 				// Show title
 				pointTitle.setText(title);
 
 				// Handle successful scan
 			} else if (resultCode == RESULT_CANCELED) {
 				// Handle cancel
 			}
 		}
 	}
 
 	private void makeRootDir() {
 		// Make root dir if not in SD card yet
 
 		Boolean isExistent;
 		LookHelper looker = new LookHelper();
 		isExistent = looker.look(Global.SDPathRoot, Global.MapDirName);
 
 		if (!isExistent) {
 			MakeDirHelper maker = new MakeDirHelper();
 			maker.make(Global.SDPathRoot, Global.MapDirName);
 		}
 
 	}
 
 	private Boolean searchMapDir(String mapID) {
		// Check whether the Map Dir existss
 
 		LookHelper looker = new LookHelper();
 
 		return looker.look(Global.MapDirRoot, mapID);
 	}
 
 	private void mapJsonDownload() {
		//Downlaod the Json file
		
 		DownloadHelper downloader = new DownloadHelper();
 		Boolean isExistent;
		File jsonPath = new File(Global.MapDirRoot + Global.LastJson);
 
		LookHelper looker = new LookHelper();
 		isExistent = looker.look(Global.MapDirRoot, Global.LastJson);
 
 		if (isExistent) {
			//jsonPath.delete();
 		}
 
 		downloader.downFile("http://dl.dropbox.com/u/22034772/example.json",
 				Global.MapDirRoot, Global.LastJson);
 	}
 
 	private Boolean checkMapVerUpdate() {
 		return true;
 	}
 
 }
