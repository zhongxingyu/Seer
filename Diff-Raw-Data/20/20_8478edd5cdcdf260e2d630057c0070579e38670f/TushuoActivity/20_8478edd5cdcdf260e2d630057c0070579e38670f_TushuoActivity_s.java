 package com.dreaner.penti.core.ui;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.widget.ListView;
 
 import com.dreaner.penti.R;
 import com.dreaner.penti.core.api.TushuoApi;
 import com.dreaner.penti.core.api.result.TushuoPartItem;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
 import com.nostra13.universalimageloader.core.ImageLoader;
 import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
 import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
 
 public class TushuoActivity extends Activity {
 	private static final String TAG = "TushuoActivity";
 	private List<TushuoPartItem> mData;
 	private Handler mHandler;
 	private TushuoAdapter mAdapter;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		mHandler = new Handler();
 		initImageLoader();
 		setContentView(R.layout.ac_image_list);
 		ListView listview = (ListView) findViewById(R.id.list);
 		mAdapter = new TushuoAdapter(getLayoutInflater());
 		listview.setAdapter(mAdapter);
 
 		TushuoApi api = new TushuoApi();
 		api.requestTushuo(new AsyncHttpResponseHandler() {
 			@Override
 			public void onSuccess(String content) {
 				Log.d(TAG, "onSuccess: " + content);
 				mData = DataTransHelper.transFromJsonStr(content);
 				mHandler.post(new Runnable() {
 
 					@Override
 					public void run() {
 						mAdapter.setData(mData);
 						mAdapter.notifyDataSetChanged();
 					}
 				});
 			}
 
 			@Override
 			public void onFailure(Throwable error, String content) {
 				Log.d(TAG, "onFailure: " + content);
 				super.onFailure(error, content);
 			}
 
 			@Override
 			public void onFinish() {
 				Log.d(TAG, "onFinish: ");
 				super.onFinish();
 			}
 
 			@Override
 			public void onStart() {
 				Log.d(TAG, "onStart: ");
 				super.onStart();
 			}
 		});
 	}
 
 	private void initImageLoader() {
 		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
				.threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory()
 				.discCacheFileNameGenerator(new Md5FileNameGenerator())
 				.tasksProcessingOrder(QueueProcessingType.LIFO).enableLogging().build();
 		// Initialize ImageLoader with configuration.
 		ImageLoader.getInstance().init(config);
 	}
 }
