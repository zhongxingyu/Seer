 package com.quanleimu.view.fragment;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Message;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.quanleimu.activity.BaseFragment;
 import com.quanleimu.activity.BaseFragment.TabDef;
 import com.quanleimu.activity.BaseFragment.TitleDef;
 import com.quanleimu.activity.QuanleimuApplication;
 import com.quanleimu.activity.R;
 import com.quanleimu.entity.FirstStepCate;
 import com.quanleimu.entity.HotList;
 import com.quanleimu.entity.SecondStepCate;
 import com.quanleimu.imageCache.ImageLoaderCallback;
 import com.quanleimu.imageCache.LazyImageLoader;
 import com.quanleimu.jsonutil.JsonUtil;
 import com.quanleimu.util.BXStatsHelper;
 import com.quanleimu.util.Communication;
 import com.quanleimu.view.CategorySelectionView;
 import com.quanleimu.widget.CircleFlowIndicator;
 import com.quanleimu.widget.ViewFlow;
 
 public class HomeFragment extends BaseFragment implements CategorySelectionView.ICateSelectionListener{
 	
 	public static final String NAME = "HomeFragment";
 	
 	private ViewFlow glDetail;
 	private CircleFlowIndicator indicator;
 //	LinearLayout hotlistView = null;
 	RelativeLayout rlHotList = null;
 	private List<HotList> listHot = new ArrayList<HotList>();
 	private String cityName;
 	private String json;
 	private HotListAdapter adapter;
 	private List<HotList> tempListHot = new ArrayList<HotList>();
 	private List<Boolean> tempUpdated = new ArrayList<Boolean>();
 	
 	static private String locationAddr = "";
 	
 	protected void initTitle(TitleDef title) {
 		title.m_visible = false;
 	}
 	
 	public void initTab(TabDef tab){
 		tab.m_visible = true;
 		tab.m_tabSelected = ETAB_TYPE.ETAB_TYPE_MAINPAGE;
 	}
 	
 	
 
 	@Override
 	protected int getFirstRunId() {
 		return R.layout.first_run_main;
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		logCreateView(savedInstanceState);
 		
 		View v = inflater.inflate(R.layout.homepageview, null);
 		
 		LinearLayout hotlistView = (LinearLayout)inflater.inflate(R.layout.hotlist, null);
 		rlHotList = (RelativeLayout)hotlistView.findViewById(R.id.rlHotList);
 		glDetail = (ViewFlow) hotlistView.findViewById(R.id.glDetail);
 		glDetail.setFadingEdgeLength(10);
 		indicator = (CircleFlowIndicator) hotlistView.findViewById(R.id.viewflowindic);
 		glDetail.setFlowIndicator(indicator);
 
 		glDetail.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				if (listHot.get(arg2).getType() == 0) {
 					Bundle bundle = createArguments(listHot.get(arg2).getHotData().getTitle(), "首页");
 					bundle.putString("actType", "homepage");
 					bundle.putString("searchContent", (listHot.get(arg2)
 							.getHotData().getKeyword()));
 					
 					pushFragment(new SearchGoodsFragment(), bundle);
 				} else if (listHot.get(arg2).getType() == 1) {
 					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(listHot
 							.get(arg2).getHotData().getWeburl()));
 					startActivity(i);
 				}else if(listHot.get(arg2).getType() == 2){
 					Bundle bundle = createArguments(listHot.get(arg2).getHotData().getTitle(), "首页");
 					bundle.putString("actType", "homepage");
 					bundle.putString("categoryEnglishName", (listHot.get(arg2)
 							.getHotData().getKeyword()));
 					
 					pushFragment(new GetGoodFragment(), bundle);
 				}
 				BXStatsHelper.getInstance().increase(BXStatsHelper.TYPE_HOTS_SEND, null);
 			}
 		});
 
 		if (QuanleimuApplication.getApplication().getCityName() == null || QuanleimuApplication.getApplication().getCityName().equals("")) {
 			cityName = "上海";
 			QuanleimuApplication.getApplication().setCityName(cityName);
 			QuanleimuApplication.getApplication().setCityEnglishName("shanghai");
 		} else {
 			cityName = QuanleimuApplication.getApplication().getCityName();
 		}
 		
 		listHot = QuanleimuApplication.listHot;
 		if(listHot == null){	
 			//try to load from last-saved hot-list file
 			boolean lastSavedValid = true;
 			try {
 				FileInputStream jsonFile = getActivity().openFileInput("hotlist.json");
 				BufferedInputStream bufferedStream = new BufferedInputStream(jsonFile);
 				int bytesJson = bufferedStream.available();
 				byte[] json = new byte[bytesJson];
 				int readBytes = bufferedStream.read(json, 0, bytesJson);
 				
 				String jsonRaw = new String(json, 0, readBytes);
 				String jsonDecoded = Communication.decodeUnicode(jsonRaw);
 				listHot = JsonUtil.parseCityHotFromJson(jsonDecoded);
 				
 				for(int i = 0; i < listHot.size(); ++i){
 					if(!QuanleimuApplication.getImageLoader().checkWithImmediateIO(listHot.get(i).imgUrl)){
 						lastSavedValid = false;
 					}
 				}
 				
 				if(listHot == null || listHot.size() == 0)	lastSavedValid = false;
 				
 			} catch (FileNotFoundException e) {
 				lastSavedValid = false;
 			}catch(IOException e){
 				lastSavedValid = false;
 			}
 			
 			//if last-saved is not ready, parse from initial hot-list in asset 
 			if(!lastSavedValid){
 				try {
 					InputStream jsonFile = getActivity().getAssets().open("hotlist.json");
 					BufferedInputStream bufferedStream = new BufferedInputStream(jsonFile);
 					int bytesJson = bufferedStream.available();
 					byte[] json = new byte[bytesJson];
 					int readBytes = bufferedStream.read(json, 0, bytesJson);
 					
 					String jsonDecoded = Communication.decodeUnicode(new String(json, 0, readBytes));
 					listHot = JsonUtil.parseCityHotFromJson(jsonDecoded);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			
 
 			if(listHot == null)	listHot = new ArrayList<HotList>();
 			
//			new Thread(new HotListThread()).start(); 
 		}
 		
 		adapter = new HotListAdapter(getActivity(), 
 				listHot, 
 				tempListHot, 
 				QuanleimuApplication.getImageLoader());
 		glDetail.setAdapter(adapter);		
 		
 		
 		LinearLayout footer = (LinearLayout)inflater.inflate(R.layout.feedback_homepage, null);
 		footer.findViewById(R.id.feedback).setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				pushFragment(new FeedbackFragment(), createArguments(null, null));
 			}
 		});
 		
 		CategorySelectionView catesView = (CategorySelectionView)v.findViewById(R.id.cateSelection);
 		catesView.setHeaderFooterView(hotlistView, footer);
 		catesView.setExpendable(false);
 		catesView.setSelectionListener(this);
 		
 		LinearLayout changeCity = (LinearLayout) v.findViewById(R.id.llChangeCity);
 		changeCity.setOnClickListener(new View.OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				pushFragment(new CityChangeFragment(), createArguments("切换城市", "首页"));
 			}
 			
 		});
 		
 		final TextView editSearch = (TextView) v.findViewById(R.id.etSearch);
 		
 		v.findViewById(R.id.rlSearch).setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View v) {
 				Bundle bundle = new Bundle();
 				bundle.putString("backPageName", "首页");
 				bundle.putString("searchContent", editSearch.getText().toString());
 				bundle.putString("actType", "search");
 
 				pushFragment(new SearchFragment(), bundle);
 			}
 		});
 		
 		((TextView)v.findViewById(R.id.tvCityName)).setText(cityName);
 		
 		Log.w(TAG, "do we have view here homeFragmengCreatView ?? " + (this.getView() != null));
 		return v;
 	}
 	
	
	
	@Override
	public void onStackTop(boolean isBack) {
		new Thread(new HotListThread()).start(); 
	}



 	class HotListThread implements Runnable {
 
 		@Override
 		public void run() {
 			String apiName = "city_hotlist";
 			ArrayList<String> list = new ArrayList<String>();
 			list.add("v=_v2");
 			String url = Communication.getApiUrl(apiName, list);
 			try {
 				json = Communication.getDataByUrl(url, true);
 				if (json != null) {
 //					myHandler.sendEmptyMessage(1);
 					sendMessage(1, null);
 				} else {
 //					myHandler.sendEmptyMessage(2);
 					sendMessage(2, null);
 				}
 			} catch (UnsupportedEncodingException e) {
 //				myHandler.sendEmptyMessage(4);
 				sendMessage(4, null);
 			} catch (IOException e) {
 //				myHandler.sendEmptyMessage(4);
 				sendMessage(4, null);
 			} catch (Communication.BXHttpException e){
 				
 			}
 
 		}
 	}
 	
 	
 	@Override
 	protected void handleMessage(Message msg, Activity activity, View rootView) {
 
 		switch (msg.what) {
 		case 1:
 			tempListHot = JsonUtil.parseCityHotFromJson(Communication
 					.decodeUnicode(json));
 			if(tempListHot != null){
 				for(int i = 0; i < tempListHot.size(); ++i){
 					tempUpdated.add(false);
 				}
 
 				if(adapter != null)	adapter.SetLoadingHotList(tempListHot);
 				QuanleimuApplication.listHot = tempListHot;
 				
 				//save to context data
 				QuanleimuApplication.context.deleteFile("hotlist.json");
 				try {
 					BufferedOutputStream outFileStream = new BufferedOutputStream(QuanleimuApplication.context.openFileOutput("hotlist.json", Context.MODE_PRIVATE));
 					outFileStream.write(json.getBytes(), 0, json.length());
 					outFileStream.flush();
 					outFileStream.close();
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				}catch(IOException e){
 					e.printStackTrace();
 				}
 			}
 			
 
 			break;
 		case 2:
 			if (pd != null) {
 				pd.dismiss();
 			}
 			break;
 		case 4:
 			if (pd != null) {
 				pd.dismiss();
 			}
 			 Toast.makeText(QuanleimuApplication.context, "网络连接失败，请检查设置！", 3).show();
 			//tvInfo.setVisibility(View.VISIBLE);
 			break;
 		}
 	}
 
 	class HotListAdapter extends BaseAdapter {
 		Context context;
 		List<HotList> curList = new ArrayList<HotList>();
 		List<HotList> loadingList = new ArrayList<HotList>();
 		
 		private int nNotifyInstance = 0;
 		
 		final LazyImageLoader imgLoader;
 
 		private class AdapterNotifyChange extends AsyncTask<Boolean, Boolean, Boolean> { 
 			private HotListAdapter adapter = null;
 			
 			public AdapterNotifyChange(HotListAdapter adapter_){
 				this.adapter = adapter_;
 				nNotifyInstance++;
 				//Log.d("HomePage async task count:", " " + nNotifyInstance);
 			}
 			
 			protected Boolean doInBackground(Boolean... bs) {   
 				try {
 					Thread.sleep(50);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				return true;
 			}
 			
 			protected void onProgressUpdate(Boolean... bs) {
 				}    
 			
 			protected void onPostExecute(Boolean bool) {  
 				this.adapter.notifyDataSetChanged();
 //				indicator.setPadding(0, glDetail.getHeight(), 0, 0);
 				nNotifyInstance--;
 				//Log.d("HomePage async task count:", " " + nNotifyInstance);
 			}
 		};
 		
 		public HotListAdapter(Context context, 
 				List<HotList> listHot,
 				List<HotList> loadingListHot,
 				LazyImageLoader imgLoader_) {
 			this.context = context;
 			this.curList = listHot;
 			this.loadingList = loadingListHot;
 			this.imgLoader = imgLoader_;
 			this.imgLoader.disableSampleSize();
 		}
 
 		@Override
 		public int getCount() {
 			return curList.size() > 0 ?	curList.size() : loadingList.size() > 0 ? 1 : 0;
 		}
 
 		@Override
 		public Object getItem(int arg0) {
 			return null;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			return 0;
 		}
 		
 		public void SetLoadingHotList(List<HotList> loadingListHot_){
 			this.loadingList = loadingListHot_;
 			(new AdapterNotifyChange(this)).execute(true);
 		}
 		
 		public void releaseBitmap(){
 			if(loadingList != null){
 				for(int i = 0; i < loadingList.size(); ++ i){
 					QuanleimuApplication.getImageLoader().forceRecycle(loadingList.get(i).getImgUrl());
 				}
 			}
 		}
 		
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			
 			LayoutInflater inflater = LayoutInflater.from(context);
 			
 			View v = null;
 			if (convertView != null) {
 				v = convertView;
 			} else {
 				v = inflater.inflate(R.layout.hotdetail, null);
 			}
 			
 			ImageView iv = null;
 			
 			if(position < curList.size()){
 				iv = (ImageView) v.findViewById(R.id.ivHotDetail);
 				
 				// 设置图片填充布局				
 				iv.setTag(curList.get(position).getImgUrl());
 			}
 			
 			if(imgLoader != null){
 				//check whether real image has been valid
 
 				boolean needNotify = false;
 				Bitmap bitmapReal =  null;
 				final HotListAdapter thisAdapter = this;
 				
 				if(position < loadingList.size()){
 					final int position_f = position;					
 					final ImageView iv_f = iv;
 					
 					bitmapReal = imgLoader.getWithImmediateIO(loadingList.get(position).getImgUrl(), new ImageLoaderCallback(){					
 						@Override
 						public void refresh(final String url, final Bitmap bitmap){										
 							
 							if(position_f < curList.size()){
 								curList.set(position_f, loadingList.get(position_f));
 								tempUpdated.set(position_f, true);
 							}
 							else if(position_f == curList.size()){
 								curList.add(loadingList.get(position_f));
 								tempUpdated.set(position_f, true);
 							}
 							if(iv_f != null)	iv_f.setImageBitmap(bitmap);
 	
 							(new AdapterNotifyChange(thisAdapter)).execute(true);
 						}	
 						
 						@Override
 						public Object getObject(){
 							return iv_f;
 						}
 
 						@Override
 						public void fail(String url) {
 							
 						}
 					});
 					
 				}
 				
 				if(bitmapReal != null){
 					
 					if(position < curList.size() && !tempUpdated.get(position)){
 						curList.set(position, loadingList.get(position));
 						tempUpdated.set(position, true);
 						needNotify = true;		
 					}
 					else if(position == curList.size()){
 						curList.add(loadingList.get(position));
 						tempUpdated.set(position, true);
 						needNotify = true;
 						}
 					if(iv != null)	iv.setImageBitmap(bitmapReal);
 					
 					//if this is the last seen item, try to load next
 					int position_cur = position + 1;
 					while(position_cur == curList.size() && position_cur < loadingList.size()){										
 						final int position_next = position_cur;
 						Bitmap bitmapNext = imgLoader.getWithImmediateIO(loadingList.get(position_next).getImgUrl(), new ImageLoaderCallback(){
 										
 							public void refresh(final String url, final Bitmap bitmap){	
 															
 								if(position_next == curList.size()){
 									curList.add(loadingList.get(position_next));
 									tempUpdated.set(position_next, true);
 								}								
 								notifyChange();
 							}
 							
 							@Override
 							public Object getObject(){
 								return null;
 							}
 
 							@Override
 							public void fail(String url) {
 								
 							}
 						});
 						
 						if(bitmapNext != null){
 							curList.add(loadingList.get(position_next));
 							tempUpdated.set(position_next, true);
 							needNotify = true;
 							position_cur++;
 						}else{
 							position_cur++;
 						}
 						try{
 							Thread.sleep(1000);
 						}catch(Exception e){
 							e.printStackTrace();
 						}
 					}
 				}else if(position < curList.size()){				
 					
 					final Bitmap bitmap =  imgLoader.getWithImmediateIO(curList.get(position).getImgUrl(), new ImageLoaderCallback(){
 					
 						public void refresh(final String url, final Bitmap bitmap){	
 								Log.d( "HotList original image loader 1", "original hotlist picture missing!!");
 					    }	
 						
 						@Override
 						public Object getObject(){
 							return null;
 						}
 
 						@Override
 						public void fail(String url) {
 							
 						}
 					});
 					
 					if(bitmap != null){
 						if(iv != null)	iv.setImageBitmap(bitmap);	
 					}else{
 						Log.d( "HotList original image loader 2", "original hotlist picture missing!!");
 					}
 					
 				}
 				
 				if(needNotify)	notifyChange();
 				
 			}
 			//this.imgLoader.enableSampleSize();
 			return v;
 		}
 		
 		private void notifyChange(){
 			if(HotListAdapter.this.nNotifyInstance < 1){
 				(new AdapterNotifyChange(this)).execute(true);
 			}
 		}
 	}
 
 	@Override
 	public void OnMainCategorySelected(FirstStepCate selectedMainCate){
 		Bundle bundle = createArguments(selectedMainCate.name, "返回");
 		ArrayList<SecondStepCate> cates = new ArrayList<SecondStepCate>();
 		cates.addAll(selectedMainCate.getChildren());
 		bundle.putSerializable("cates", cates);
 		
 		pushFragment(new SubCateFragment(), bundle);
 	}
 	
 	@Override
 	public void OnSubCategorySelected(SecondStepCate selectedSubCate){
 
 		throw new RuntimeException("you should never goes here.");
 	}
 
 }
 
 
