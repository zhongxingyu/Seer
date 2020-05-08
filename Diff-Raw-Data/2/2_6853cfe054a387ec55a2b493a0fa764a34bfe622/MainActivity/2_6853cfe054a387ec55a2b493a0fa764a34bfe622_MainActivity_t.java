 package com.example.myauto;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.example.myauto.adapter.ListAdapter;
 import com.example.myauto.data.DataContainer;
 import com.example.myauto.event.MyChangeEvent;
 import com.example.myauto.fetcher.ImageDownloader;
 import com.example.myauto.fetcher.ItemFetcher;
 import com.example.myauto.item.CarFacade;
 import com.example.myauto.item.CarItem;
 import com.example.myauto.item.Item;
 import com.example.myauto.listener.CallbackListener;
 import com.example.myauto.listener.ImageDownloadListener;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 public class MainActivity extends MasterPageActivity implements ImageDownloadListener, CallbackListener{
 	private ListAdapter adapter;
 	private static ImageDownloader downloader = new ImageDownloader();
 	private ArrayList<CarFacade> ls;
 	private Activity thisActivity;
 	private ItemFetcher fetcher;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.tab1);
 		
 		thisActivity = this;
 		ls = (ArrayList<CarFacade>) getIntent().getExtras().getSerializable(FirstPageActivity.bundleKey);
 		
 		adapter = new ListAdapter(ls, this);
 		ListView lv = (ListView)findViewById(R.id.tab1);
 		lv.setAdapter(adapter);
 		setClickListener(lv);
 
 		downloader.addMyChangeListener(this);
 	}
 	
 	private void setClickListener(final ListView lv){
 		lv.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				
 				CarFacade car = (CarFacade) lv.getItemAtPosition(arg2);
 				String id = car.getValueFromProperty(CarItem.ID);
 				HashMap<String, String> params = new HashMap<String, String>();
 				params.put("car_id", id);
 				
 				runDownloader(params);
 				
 			}
 		});
 	}
 	
 	private void runDownloader(HashMap<String, String> params){
 		fetcher = new ItemFetcher(thisActivity);
 		fetcher.addMyChangeListener(this);
 		fetcher.execute(params);
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		fetchImages(adapter.getList());
 	}
 	
 	private void fetchImages(ArrayList<CarFacade> ls){
 		for(CarFacade car : ls){
 			ImageDownloader.fetchImageFor(car);
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		downloader.removeMyChangeListener(this);
 		ImageDownloader.clearImageQueue();
 		
 		Bundle extras = getIntent().getExtras();
 		extras.putSerializable(FirstPageActivity.bundleKey, adapter.getList());
 		getIntent().putExtras(extras);
 	}
 	
 	
 	@Override
 	public void imageDownloaded(MyChangeEvent evt) {
 		adapter.notifyDataSetInvalidated();
 	}
 
 	@Override
 	public void onFinished(MyChangeEvent evt) {
 		fetcher.removeMyChangeListener(this);
 		Item item = (Item) evt.source;
 		
 		Intent detailActivity = new Intent(MainActivity.this, DetailActivity.class);
 		Bundle extras = new Bundle();
 		extras.putSerializable(FirstPageActivity.bundleKey, (Serializable) item);
 		detailActivity.putExtras(extras);
 		
 		startActivity(detailActivity);
 	}
 }
