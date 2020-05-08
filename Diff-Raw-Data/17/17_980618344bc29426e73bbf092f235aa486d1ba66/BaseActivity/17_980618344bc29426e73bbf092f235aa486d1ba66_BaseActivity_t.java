 package com.quanleimu.activity;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.res.Configuration;
 import android.net.ConnectivityManager;
import android.net.NetworkInfo;
 import android.net.NetworkInfo.State;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
 import android.support.v4.app.FragmentTransaction;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import com.mobclick.android.MobclickAgent;
 import com.quanleimu.activity.R.color;
 import com.quanleimu.entity.CityDetail;
 import com.quanleimu.view.fragment.FirstRunFragment;
 import com.tencent.mm.sdk.platformtools.Log;
 /**
  * 父类Activity
  * @author henry_yang
  *
  */
 public class BaseActivity extends FragmentActivity implements OnClickListener{
 
 	public static final String TAG = "QLM";// "BaseActivity";
 	
 	public static final String PREF_FIRSTRUN  = "firstRunFlag";
 	
 	//定义Intent和Bundle
 	protected Intent intent = null;
 	protected Bundle bundle = null;
 	protected QuanleimuApplication myApp; 
 	protected ImageView ivHomePage,ivPostGoods,ivMyCenter;
 	protected View v = null; 
 	protected ProgressDialog pd;
 	//public LoadImage LoadImage;
 	
 	private int stackSize;
 	
 	@Override
     public void onSaveInstanceState(Bundle savedInstanceState) {
 		
 		savedInstanceState.putString("cityEnglishName", myApp.getCityEnglishName());
 		savedInstanceState.putString("cityName", myApp.getCityName());
 		
 		ArrayList<String>strDetails = new ArrayList<String>();
 		for(int i = 0; i < myApp.getListCityDetails().size(); ++ i){
 			CityDetail detail = myApp.getListCityDetails().get(i);
 			String tstrDetail = "englishName=" + detail.getEnglishName()
 					+ ",id=" + detail.getId()
 					+ ",name=" + detail.getName()
 					+ ",sheng=" + detail.getSheng(); 
 			strDetails.add(tstrDetail);
 		}
 		
 		savedInstanceState.putStringArrayList("cityDetails", strDetails);
 		
         super.onSaveInstanceState(savedInstanceState);
     }
 
     @Override
     public void onRestoreInstanceState(Bundle savedInstanceState) {
     	Log.w(TAG, "start restore instance for activity " + this.getClass().getName());
     	super.onRestoreInstanceState(savedInstanceState);
 		myApp.setCityEnglishName(savedInstanceState.getString("cityEnglishName"));
 		myApp.setCityName(savedInstanceState.getString("cityName"));
 		
 		ArrayList<String>listDetails = savedInstanceState.getStringArrayList("cityDetails");
 		
 		List<CityDetail> cityDetails = new ArrayList<CityDetail>();
 		for(int i = 0; i < listDetails.size(); ++ i){
 			String strDetail = listDetails.get(i);
 			String[] strDetails = strDetail.split(",");
 			CityDetail detail = new CityDetail();
 			for(int j = 0; j < strDetails.length; ++ j){
 				String[] subItems = strDetails[j].split("=");
 				if(subItems[0].equals("englishName")){
 					detail.setEnglishName(subItems[1]);
 				}
 				else if(subItems[0].equals("id")){
 					detail.setId(subItems[1]);
 				}
 				else if(subItems[0].equals("name")){
 					detail.setName(subItems[1]);
 				}
 				else if(subItems[0].equals("sheng")){
 					detail.setSheng(subItems[1]);
 				}				
 			}
 			cityDetails.add(detail);
 		}
 		myApp.setListCityDetails(cityDetails);
     }
 	
 	protected TextView tvAddMore ;
 	protected LinearLayout loadingLayout;
 	 /** 
      * 设置布局显示为目标有多大就多大 
      */  
 	protected LayoutParams WClayoutParams =new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);  
     /** 
      * 设置布局显示目标最大化 
      */  
 	protected LayoutParams FFlayoutParams =new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.FILL_PARENT);  
 	
 	protected ProgressBar progressBar;  
 	
 	//防止滑盖手机滑盖刷新
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		// TODO Auto-generated method stub
 		super.onConfigurationChanged(newConfig);
 	} 
 	 
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 		MobclickAgent.onPause(this);
 		
 		//Log.d("Umeng SDK API call", "onPause() called from BaseActivity:onPause()!!");
 	}
 
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		MobclickAgent.onResume(this);
 		
 		//Log.d("Umeng SDK API call", "onResume() called from BaseActivity:onResume()!!");
 		
 		
 	}
 
 
 
 	@Override
 	public void onAttachFragment(Fragment fragment) {
 		// TODO Auto-generated method stub
 		super.onAttachFragment(fragment);
 	}
 	
 	protected final void notifyStackTop()
 	{
 		BaseFragment f = getCurrentFragment();
 		if (f != null)
 		{
 			int newStackSize = getSupportFragmentManager().getBackStackEntryCount();
 			
 			Log.e(TAG, "notify stack top " + f.getClass().getName());
 			try
 			{
 				f.notifyOnStackTop(newStackSize < stackSize);
 			} catch( Throwable t) {
 				
 			}
 			finally
 			{
 				stackSize = newStackSize;
 			}
 			findViewById(R.id.splash_cover).setVisibility(View.GONE);
 			
 		}
 	
 	}
 	
 	public final void showFirstRun(BaseFragment f)
 	{
 		if (f.getFirstRunId() == -1)
 		{
 			return; //No need first run.
 		}
 		String key = f.getClass().getName() + QuanleimuApplication.version;
 		SharedPreferences share = this.getSharedPreferences(PREF_FIRSTRUN, MODE_PRIVATE);
 		boolean shown = share.getBoolean(key, false);
 		if (!shown)
 		{
 			Editor edit = share.edit();
 			edit.putBoolean(key, true);
 			edit.commit();
 			
 			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
 			Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
 	        if (prev != null) {
 	            ft.remove(prev);
 	        }
 	        ft.addToBackStack(null);
 
 	        // Create and show the dialog.
 	        DialogFragment newFragment = FirstRunFragment.create(key, f.getFirstRunId());
 	        newFragment.show(ft, "dialog");
 		}
 	}
 	
 	public final void onHideFirstRun(String key)
 	{
 		SharedPreferences share = this.getSharedPreferences(PREF_FIRSTRUN, MODE_PRIVATE);
 		Editor edit = share.edit();
 		edit.putBoolean(key, true);
 		edit.commit();
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		getSupportFragmentManager().addOnBackStackChangedListener(new OnBackStackChangedListener() {
 			@Override
 			public void onBackStackChanged() {
 				notifyStackTop();
 			}});
 		
 		if (savedInstanceState  != null)
 		{
 			Log.w(TAG, "recreate activity from saved instance" + this.hashCode());
 		}
 //		v =findViewById(R.id.linearBottom);
 		MobclickAgent.onError(this);
 		myApp = (QuanleimuApplication) getApplication();
 		//LoadImage = new LoadImage();
 		//判断Intent和Bundle
 		intent = getIntent();
 		if(intent == null)
 		{
 			intent = new Intent();
 		}
 		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		bundle = intent.getExtras();
 		if(bundle == null)
 		{
 			bundle = new Bundle();
 		}
 		
 //		//通过ID获取控件
 //		
 //		ivHomePage = (ImageView)v.findViewById(R.id.ivHomePage);
 //		ivCateMain = (ImageView)v.findViewById(R.id.ivCateMain);
 //		ivPostGoods = (ImageView)v.findViewById(R.id.ivPostGoods);
 //		ivMyCenter = (ImageView)v.findViewById(R.id.ivMyCenter);
 //		ivSetMain = (ImageView)v.findViewById(R.id.ivSetMain);
 //		
 //		//设置监听器
 //		ivHomePage.setOnClickListener(this);
 //		ivCateMain.setOnClickListener(this);
 //		ivPostGoods.setOnClickListener(this);
 //		ivMyCenter.setOnClickListener(this);
 //		ivSetMain.setOnClickListener(this);
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch(v.getId())
 		{
 //			case R.id.ivHomePage:
 //				if(!myApp.getActivity_type().equals("homepage"))
 //				{
 //					intent.setClass(this, HomePage.class);
 //					intent.putExtras(bundle);
 //					startActivity(intent);
 //					ivHomePage.setImageResource(R.drawable.iv_homepage_press);
 //					ivCateMain.setImageResource(R.drawable.iv_cate);
 //					ivPostGoods.setImageResource(R.drawable.iv_postgoods);
 //					ivMyCenter.setImageResource(R.drawable.iv_mycenter);
 //					ivSetMain.setImageResource(R.drawable.iv_setmain);
 //				}
 //				break;
 //			case R.id.ivCateMain:
 //				if(!myApp.getActivity_type().equals("catemain"))
 //				{
 //					intent.setClass(this, CateMain.class);
 //					intent.putExtras(bundle);
 //					startActivity(intent);
 //					ivHomePage.setImageResource(R.drawable.iv_homepage);
 //					ivCateMain.setImageResource(R.drawable.iv_cate_press);
 //					ivPostGoods.setImageResource(R.drawable.iv_postgoods);
 //					ivMyCenter.setImageResource(R.drawable.iv_mycenter);
 //					ivSetMain.setImageResource(R.drawable.iv_setmain);
 //				}
 //				break;
 //			case R.id.ivPostGoods:
 //				if(!myApp.getActivity_type().equals("postgoods"))
 //				{
 //					intent.setClass(this, PostGoods.class);
 //					intent.putExtras(bundle);
 //					startActivity(intent);
 //					ivHomePage.setImageResource(R.drawable.iv_homepage);
 //					ivCateMain.setImageResource(R.drawable.iv_cate);
 //					ivPostGoods.setImageResource(R.drawable.iv_postgoods_press);
 //					ivMyCenter.setImageResource(R.drawable.iv_mycenter);
 //					ivSetMain.setImageResource(R.drawable.iv_setmain);
 //				}
 //				break;
 //			case R.id.ivMyCenter:
 //				if(!myApp.getActivity_type().equals("mycenter"))
 //				{
 //					intent.setClass(this, MyCenter.class);
 //					intent.putExtras(bundle);
 //					startActivity(intent);
 //					ivHomePage.setImageResource(R.drawable.iv_homepage);
 //					ivCateMain.setImageResource(R.drawable.iv_cate);
 //					ivPostGoods.setImageResource(R.drawable.iv_postgoods);
 //					ivMyCenter.setImageResource(R.drawable.iv_mycenter_press);
 //					ivSetMain.setImageResource(R.drawable.iv_setmain);
 //				}
 //				break;
 //			case R.id.ivSetMain:
 //				if(!myApp.getActivity_type().equals("setmain"))
 //				{
 //					intent.setClass(this, SetMain.class);
 //					intent.putExtras(bundle);
 //					startActivity(intent);
 //					ivHomePage.setImageResource(R.drawable.iv_homepage);
 //					ivCateMain.setImageResource(R.drawable.iv_cate);
 //					ivPostGoods.setImageResource(R.drawable.iv_postgoods);
 //					ivMyCenter.setImageResource(R.drawable.iv_mycenter);
 //					ivSetMain.setImageResource(R.drawable.iv_setmain_press);
 //				}
 //				break;
 		}
 	}
 	
 	//判断网络是否连接成功
 	public boolean JadgeConnection ()throws Exception
 	{
 		
 		boolean a = false;
 		ConnectivityManager connManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connManager == null)
		{
			return false;
		}
 		
		NetworkInfo mobileInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		State mobile = mobileInfo == null ? null : mobileInfo.getState();
		State wifi = wifiInfo== null ? null : wifiInfo.getState();

		if((mobile != null && mobile.toString().equals("CONNECTED")) || (wifi != null && wifi.toString().equals("CONNECTED")))
 		{
 			a = true;
 		}
 		else 
 		{
 			a = false;
 		}
 		return a;
 	}
 	
 	//弹出等待框
 	public void pdShow(Context context)
 	{
 		pd = ProgressDialog.show(context, "提示", "请稍候...");
 		pd.setCancelable(true);
 	}
 	
 	//取消等待框
 	public void pdDismiss(Context context)
 	{
 		pd.dismiss();
 	}
 	
 	public final void pushFragment(BaseFragment fragment, Bundle bundle, String popTo)
 	{
 		FragmentManager fm = getSupportFragmentManager();
 		FragmentTransaction ft = fm.beginTransaction();
 		fm.popBackStack(popTo == null ? null : popTo , FragmentManager.POP_BACK_STACK_INCLUSIVE);
 		
 		if (bundle != null)
 		{
 			fragment.setArguments(bundle);
 		}
 		
 		ft.replace(R.id.contentLayout, fragment);
 		ft.addToBackStack(fragment.getName());
 		ft.commit();
 	}
 	
 	public final void pushFragment(BaseFragment f, Bundle bundle, boolean clearStack)
 	{
 		pushFragment(f, bundle, clearStack ? null : "");
 		
 	}
 	
 	
 	public final void popFragment(BaseFragment f)
 	{
 			FragmentManager fm = getSupportFragmentManager();
 			FragmentTransaction ft = fm.beginTransaction();
 			//Pop current
 			fm.popBackStackImmediate();
 			
 			ft.commit();
 	}
 	
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 		return super.onContextItemSelected(item);
 	}
 
 	public BaseFragment getCurrentFragment()
 	{
 		
 		FragmentManager fm = this.getSupportFragmentManager();
 //		
 		return (BaseFragment) fm.findFragmentById(R.id.contentLayout);
 	}
 
 //	public static String cn2Spell(String chinese) {
 //		StringBuffer pybf = new StringBuffer();
 //		char[] arr = chinese.toCharArray();
 //		HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
 //		defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
 //		defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
 //		for (int i = 0; i < arr.length; i++) {
 //			if (arr[i] > 128) {
 //				try {
 //					pybf.append(PinyinHelper.toHanyuPinyinStringArray(arr[i],
 //							defaultFormat)[0]);
 //				} catch (BadHanyuPinyinOutputFormatCombination e) {
 //					e.printStackTrace();
 //				}
 //			} else {
 //				pybf.append(arr[i]);
 //			}
 //		}
 //		return pybf.toString();
 //	}
 	
 	/**
 	 * This is used to append the Fragment which is recorved by auto save.
 	 * @param f
 	 */
 	void restoreFragment(BaseFragment f)
 	{
 		Log.d(TAG, "append fragment from restore : " + f.getClass().getName());
 	}
 }
