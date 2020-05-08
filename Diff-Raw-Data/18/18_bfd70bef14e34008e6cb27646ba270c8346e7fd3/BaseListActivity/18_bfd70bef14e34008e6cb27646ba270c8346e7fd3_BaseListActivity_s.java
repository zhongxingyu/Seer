 package ua.newproject.ui;
 
 import java.util.HashMap;
 
 import ua.newproject.R;
 import ua.newproject.Util;
 import ua.newproject.ui.MyListFragment.setDataListener;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
import android.widget.Toast;
 
 import com.nostra13.universalimageloader.core.DisplayImageOptions;
 import com.nostra13.universalimageloader.core.ImageLoader;
 import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
 import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
 import com.slidingmenu.lib.SlidingMenu;
 import com.slidingmenu.lib.app.SlidingFragmentActivity;
 
 public class BaseListActivity extends SlidingFragmentActivity implements setDataListener,
 		OnClickListener {
 
 	final String	LOG_TAG	= getClass().getSimpleName();
 	TextView		tvTitleItem, tvBodyItem, tvAddressItem;
 	ImageView		imageItem;
 	SlidingMenu		slidingMenu;
 	Button			btnShowMenu;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		setBehindContentView(R.layout.activity_menu);
 		initUI();
 	}
 
 
 	public void initUI() {
 		tvTitleItem = (TextView) findViewById(R.id.tvTitleItem);
 		tvBodyItem = (TextView) findViewById(R.id.tvBodyItem);
 		tvAddressItem = (TextView) findViewById(R.id.tvAddressItem);
 		imageItem = (ImageView) findViewById(R.id.imageItem);
 		slidingMenu = getSlidingMenu();
 		slidingMenu.setBehindOffset(getDisplayWidth() / 2);
 		btnShowMenu = (Button) findViewById(R.id.btnShowMenu);
 		btnShowMenu.setOnClickListener(this);
 	}
 
 	public int getDisplayWidth() {
 		DisplayMetrics metrics = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(metrics);
 		Log.d(LOG_TAG, "resolution: " + metrics.widthPixels + " x " + metrics.heightPixels);
 		return metrics.widthPixels;
 	}
 
 	@Override
 	public void onClick(View v) {
 		Log.d(LOG_TAG, "showMenu");
 		slidingMenu.showMenu();
 	}
 
 	@Override
 	public void setData(Integer pos) {
 		slidingMenu.showContent();
 
 		int position = 0;
 		if (pos == null) {
 			return;
 		} else {
 			position = pos;
 		}
 		HashMap<String, String> stringMap = new Util().getStringHashMap(position);
 		Log.d(LOG_TAG, "String stringMap = " + stringMap.get("title"));
 		tvTitleItem.setText("title : " + stringMap.get("title"));
 		tvBodyItem.setText("body : " + stringMap.get("body"));
 		tvAddressItem.setText("address : " + stringMap.get("address"));
 
 		DisplayImageOptions options = new DisplayImageOptions.Builder()
 				.showStubImage(R.drawable.ic_launcher).showImageForEmptyUri(R.drawable.ic_launcher)
 				.showImageOnFail(R.drawable.ic_launcher).cacheInMemory().cacheOnDisc()
 				.displayer(new RoundedBitmapDisplayer(20)).build();
 
 		ImageLoader imageLoader = ImageLoader.getInstance();
 		imageLoader.init(ImageLoaderConfiguration.createDefault(BaseListActivity.this));
 		imageLoader.displayImage(stringMap.get("pictureURL"), imageItem, options);
 
 	}
 
 }
