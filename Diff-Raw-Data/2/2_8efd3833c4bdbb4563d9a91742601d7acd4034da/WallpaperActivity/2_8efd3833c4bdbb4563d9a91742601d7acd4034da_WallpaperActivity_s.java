 package com.joy.launcher2.wallpaper;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.joy.launcher2.R;
 import com.joy.launcher2.util.Constants;
 
 import android.os.Bundle;
 
 import android.animation.Animator;
 import android.animation.Animator.AnimatorListener;
 import android.animation.ObjectAnimator;
 import android.animation.ValueAnimator;
 import android.animation.ValueAnimator.AnimatorUpdateListener;
 import android.app.Activity;
 
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.content.res.Resources;
 
 import android.graphics.Color;
 import android.graphics.Rect;
 import android.graphics.drawable.Drawable;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.view.animation.LinearInterpolator;
 import android.view.animation.Animation.AnimationListener;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.BaseAdapter;
 import android.widget.FrameLayout;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * 本地壁纸，在线壁纸，其它壁纸显示
  * @author huangming
  *
  */
 public class WallpaperActivity extends Activity implements ImageLoader.Callback, OnPageChangeListener, OnItemClickListener, OnItemLongClickListener, OnClickListener, OnScrollListener{
 	
 	protected final static String XML_FILE_NAME = "wallpapers.xml";
 	private final static int ACTIVITY_TYPE = 1;
 	
 	private final static int RECOMMEND_ANIMATION_DURATION = 200;
 	private final static boolean DEBUG= false;
 	private final static String TAG = "WallpaperActivity";
 
 	private GridView mNativeGridView;
 	private GridView mOnlineGridView;
 	private ImageLoader mImageLoader;
 	private ViewPager mPager;
 	private RelativeLayout mSelectRL;
 	private IndicatorView mIndicatorView;
 	RecommendsView recommend;
 	TextView title;
 	TextView nativeTitle;
 	TextView onlineTitle;
 	TextView otherTitle;
 	TextView recommendTV;
 	
 	private ListView otherLV;
 	
 	LinearLayout online;
 	
 
     int page = 0;
 
     String[] recommends = new String[4];
     
     int screenWidth;
     int screenHeight;
     
     int previousPageIndex = 0;
     private boolean isLoaded = false;
     private boolean isLoading = false;
     private boolean isRecommandLoaded = false;
     private boolean isRecommandLoading = false;
     
     
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.joy_wallpaper_main);
 		
 		DisplayMetrics dm = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(dm);
 		screenWidth = dm.widthPixels;
 		screenHeight = dm.heightPixels;
 		
 		
 		//初始化ImageLoder
 		mImageLoader = ImageLoader.getInstance(getApplicationContext());
 		mImageLoader.setScreenSize(screenWidth, screenHeight);
 		mImageLoader.setCallback(this, ACTIVITY_TYPE);	
 		mImageLoader.unlock();
 		
 		
 		//初始化tab		
 		title = (TextView)findViewById(R.id.title);
 		nativeTitle = (TextView)findViewById(R.id.native_btn);
 		onlineTitle = (TextView)findViewById(R.id.online_btn);
 		otherTitle = (TextView)findViewById(R.id.other_btn);
 		title.setText(R.string.title);
 		nativeTitle.setText(R.string.native_wallpaper_tab);
 		onlineTitle.setText(R.string.online_wallpaper_tab);
 		otherTitle.setText(R.string.other_wallpaper_tab);
 		nativeTitle.setOnClickListener(this);
 		onlineTitle.setOnClickListener(this);
 		otherTitle.setOnClickListener(this);
 		nativeTitle.setBackgroundColor(Color.BLACK);
 		onlineTitle.setBackgroundColor(Color.BLACK);
 		otherTitle.setBackgroundColor(Color.BLACK);
 		
 			
 		//初始化indicator
 		mIndicatorView =(IndicatorView)findViewById(R.id.indicator);
 		moveIndicator(0.0f);
 	
 
 		//初始化本地壁纸和加载数据
 		mNativeGridView = (GridView)getLayoutInflater().inflate(R.layout.joy_wallpaper_gridview, null);
         mNativeGridView.setOnItemClickListener(this);
 		mNativeGridView.setOnItemLongClickListener(this);			
 		mNativeGridView.setOnScrollListener(this);
 		mImageLoader.parseJSON(ACTIVITY_TYPE, 1, previousPageIndex);
 		
 		
 		//初始化在线壁纸和加载数据:包括recommmedselect, recommmed, Girdviev
 		online = (LinearLayout)getLayoutInflater().inflate(R.layout.joy_wallpaper_online, null);
 		
 		recommendTV = (TextView)online.findViewById(R.id.recommend_tv);
 		recommendTV.setText(R.string.recommend_title);
 		
 		mSelectRL = (RelativeLayout)online.findViewById(R.id.select_rl);
 		mSelectRL.setOnClickListener(this);
 		
 		recommend = (RecommendsView)online.findViewById(R.id.recommend);
 		recommend.setDimens(screenWidth, getResources().getDimensionPixelSize(R.dimen.recommend_height));
 		for(int i= 0; i <recommend.getMaxCount(); i++)
 		{
 			ImageView image = (ImageView)getLayoutInflater().inflate(R.layout.joy_wallpaper_recommend_image, null);
 			RecommendsView.LayoutParams lp = new RecommendsView.LayoutParams(0, 0);
 			image.setLayoutParams(lp);
 			recommend.addView(image);
 		}
 		//加载recommend的四张图片
 		//
 		//
 		recommend.setOnClickListener(this);
 		
 		mOnlineGridView = (GridView)online.findViewById(R.id.gridview_online);
 		mOnlineGridView.setOnItemClickListener(this);
 		mOnlineGridView.setOnScrollListener(this);
 		//mImageLoader.parseJSON(ACTIVITY_TYPE, 2, previousPageIndex);
 		
 		
 		//初始化其他壁纸和数据
 		otherLV = (ListView)getLayoutInflater().inflate(R.layout.joy_wallpaper_other_listview, null);
 		otherLV.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				IntentHolder holder = (IntentHolder)view.getTag();
 				if(holder != null)
 				{
 					startActivity(holder.intent);
 				}
 				
 			}
 		});
 		Intent wallpaperIntent = new Intent(Intent.ACTION_SET_WALLPAPER, null);
 		PackageManager pm = getPackageManager();
 		List<ResolveInfo> wallpaperApps = pm.queryIntentActivities(wallpaperIntent, 0);	
 		ArrayList<String> appsName = new ArrayList<String>();
 		ArrayList<Drawable> appsDrawable = new ArrayList<Drawable>();
 		ArrayList<Intent> appsIntent = new ArrayList<Intent>();
 		for(int i = 0; i < wallpaperApps.size(); i++)
 		{
 			ResolveInfo info = wallpaperApps.get(i);
 			Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
 			String packageName = info.activityInfo.applicationInfo.packageName;
 			ComponentName componentName = new ComponentName(packageName, info.activityInfo.name);
 			intent.setComponent(componentName);
 			appsName.add(info.loadLabel(pm).toString());
 			appsDrawable.add(getAppDrawable(info));
 			appsIntent.add(intent);
 		}
 		otherLV.setAdapter(new LVAdapter(appsName, appsDrawable, appsIntent));
 		
 
 		//初始化pager
 		mPager = (ViewPager)findViewById(R.id.pager);
 		final ArrayList<View> viewList = new ArrayList<View>();
 		viewList.add(mNativeGridView);
 		viewList.add(online);
 		viewList.add(otherLV);				
 		PagerAdapter pa = new PagerAdapter() {
 			
 			@Override
 			public boolean isViewFromObject(View arg0, Object arg1) {
 				return arg0 == arg1;
 			}
 						
 			@Override
 			public void destroyItem(View container, int position, Object object) {
 				//((ViewPager)container).removeViewAt(position);
 				if ((object instanceof View) && (container instanceof ViewPager)) 
 				{
 					View child = (View)object;
 					ViewPager p = (ViewPager)container;
 					int count = p.getChildCount();
 					for(int i = 0; i < count; i++)
 					{
 						if(p.getChildAt(i) == child)
 						{
 							p.removeView(child);
 						}
 					}
 					
 				}
 			}
 
 			@Override
 			public Object instantiateItem(View container, int position) {
 				// TODO Auto-generated method stub
				((ViewPager)container).addView(viewList.get(position), position);
 				return viewList.get(position);
 			}
 
 			@Override
 			public int getCount() {
 				return viewList.size();
 			}
 		};
 		mPager.setOnPageChangeListener(this);
 		mPager.setAdapter(pa);
 		page = 0;
 		setTabTextColor();
 		mPager.setCurrentItem(page);
 		
 		
 		 
 	}
 	
 	
 	private Drawable getAppDrawable(ResolveInfo info)
 	{
 		ActivityInfo ai = info.activityInfo;
 		Drawable icon = null;
 		Resources res;
 		 try {
 			 res = getPackageManager().getResourcesForApplication(
 					 ai.applicationInfo);
 	        } catch (PackageManager.NameNotFoundException e) {
 	        	res = null;
 	        }
 		 if(res != null)
 		 {
 			 int iconId = ai.getIconResource();
 			 if(iconId != 0)
 			 {
 				 icon = res.getDrawable(iconId); 
 			 }
 		 }
 		 if(icon == null)
 		 {
 			 icon = getResources().getDrawable(R.drawable.ic_launcher);
 		 }
 		return icon;
 	}
 	
 	
 
 		@Override
 		public void onScrollStateChanged(AbsListView view, int scrollState) {
 			// TODO Auto-generated method stub
 			switch(scrollState)
 			{
 			    case OnScrollListener.SCROLL_STATE_FLING:
 			    	mImageLoader.lock();
 			    	break;
 			    case OnScrollListener.SCROLL_STATE_IDLE:
 			    	if(page == 1 && view.getLastVisiblePosition() == view.getCount() - 1)
 			    	{			  			    	    
 		                	previousPageIndex++;
 				    		showProgressBar();
 		                	mImageLoader.parseJSON(ACTIVITY_TYPE, 2, previousPageIndex);
 		            }
 			    	mImageLoader.unlock();
 			    	break;
 			    case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
 			    	mImageLoader.lock();
 			    	break;
 				default:
 					break;
 			}
 		}
 
 		@Override
 		public void onScroll(AbsListView view, int firstVisibleItem,
 				int visibleItemCount, int totalItemCount) {
 			// TODO Auto-generated method stub
 		}
 		
 	
 	
 	
 	@Override
 	public void onWindowFocusChanged(boolean hasFocus) {
 		// TODO Auto-generated method stub
 		super.onWindowFocusChanged(hasFocus);
 		moveIndicator(page);
 	}
 
 
 
 
 	private void moveIndicator(float process)
 	{
 		View parent = (View) mIndicatorView.getParent().getParent();
 		int width = parent.getWidth();
 		int x = (int)(process * width / 3) + width / 6;
 		
 		ViewGroup.LayoutParams lp = mIndicatorView.getLayoutParams();
 		if(lp instanceof FrameLayout.LayoutParams)
 		{
 			FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams)lp;
 			flp.leftMargin = x - mIndicatorView.getWidth() / 2;
 			mIndicatorView.requestLayout();
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		if(mImageLoader != null)
 		{
 			if(DEBUG)Log.e(TAG, "onDestroy");
 			mImageLoader.clearCache();
 		}
 	}
 	
 	class LVAdapter extends BaseAdapter
 	{
 		ArrayList<String> names;
 		ArrayList<Drawable> drawables;
 		ArrayList<Intent> intents;
 		LVAdapter(ArrayList<String> names, ArrayList<Drawable> drawables, ArrayList<Intent> intents)
 		{
 			this.names = names; 
 			this.drawables = drawables;
 			this.intents = intents;
 		}
 
 		@Override
 		public int getCount() {
 			// TODO Auto-generated method stub
 			return names.size();
 		}
 
 		@Override
 		public Object getItem(int position) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public long getItemId(int position) {
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 
 			IntentHolder holder;
 			if(convertView == null)
 			{
 				convertView = getLayoutInflater().inflate(R.layout.joy_wallpaper_other_item, null);
 				holder = new IntentHolder();
 				convertView.setTag(holder);
 				
 			}
 			else
 			{
 				holder = (IntentHolder)convertView.getTag();
 			}
 			holder.intent = intents.get(position);
 			ImageView appImage = (ImageView)convertView.findViewById(R.id.app_image);
 			TextView appName = (TextView)convertView.findViewById(R.id.app_name);
 			appName.setText(names.get(position));
 			appImage.setImageDrawable(drawables.get(position));
 			return convertView;
 		}
 		
 	}
 	
 	static class IntentHolder
 	{
 		Intent intent;
 	}
 
 	
 	static class SimpleAdapter extends BaseAdapter
 	{
 		final ArrayList<WallpaperInfo> wis;
 		ImageLoader imageLoader;
 		LayoutInflater inflater;
 		public SimpleAdapter(LayoutInflater inflater, ArrayList<WallpaperInfo> wis, ImageLoader imageLoader)
 		{
 			this.inflater = inflater;
 			this.wis = wis;
 			this.imageLoader = imageLoader;
 		}
 
 		@Override
 		public int getCount() {
 			// TODO Auto-generated method stub
 			return wis.size();
 		}
 
 		@Override
 		public Object getItem(int arg0) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public long getItemId(int arg0) {
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public View getView(int arg0, View arg1, ViewGroup arg2) {
 			// TODO Auto-generated method stub
 
 			ViewHolder holder;
             if (arg1 == null) {
             	arg1 = inflater.inflate(R.layout.joy_wallpaper_item, arg2, false);
                 holder = new ViewHolder();
                 arg1.setTag(holder);
             } else {
                 holder = (ViewHolder)arg1.getTag();
             }
 
             
             WallpaperInfo wi = wis.get(arg0);
             holder.image = (ImageView) arg1.findViewById(R.id.wallpaper_image);
             holder.image.setImageResource(R.drawable.joy_wallpaper_resource_preview_bg);
             holder.image.setTag(wi);
             imageLoader.addLoadAndDisplayTask(wi, holder.image);
 			return arg1;
 		}
 		
 		
 	}
 	
 
 	static class ViewHolder
 	{
 		ImageView image;
 	}
 
 
 	@Override
 	public void onPageScrollStateChanged(int arg0) {
 		
 	}
 
 
 	private void setTabTextColor()
 	{
 		nativeTitle.setTextColor((page == 0)?Color.WHITE:Color.GRAY);
 		onlineTitle.setTextColor((page == 1)?Color.WHITE:Color.GRAY);
 		otherTitle.setTextColor((page == 2)?Color.WHITE:Color.GRAY);
 	}
 
 	@Override
 	public void onPageScrolled(int arg0, float arg1, int arg2) {
 		moveIndicator(arg0 + arg1);
 		if(page != arg0)
 		{
 			page = arg0;
 			setTabTextColor();
 		}
 	}
 
 	@Override
 	public void onPageSelected(int arg0) {
 		// TODO Auto-generated method stub
 		if(DEBUG)Log.e(TAG, "isLoaded = " + isLoaded + " , isLoading = " + isLoading);
 		page = arg0;
 		//第一次加载。
 		if(page == 1 && !isLoaded && !isLoading)
 		{
 			isLoading = true;
 			if(DEBUG)Log.e(TAG, "load the " +previousPageIndex + " page");
 			mImageLoader.parseJSON(ACTIVITY_TYPE, 2, previousPageIndex);
 		}
 		if(page == 1 && !isRecommandLoaded && !isRecommandLoading)
 		{
 			isRecommandLoading = true;
 			showRecommendProgressBar();
 			mImageLoader.recommendJson(ACTIVITY_TYPE);
 		}
 		//加载完成，移除进度条
 		if((page == 1 && isLoaded) || page != 1)
 		{
 			dismissProgressBar();
 		}
 		//正在加载，显示进度条。
 		if(page == 1 && isLoading)
 		{
 			showProgressBar();
 		}
 		
 		if(page == 0)
 		{
 			if(mNativeGridView.getAdapter() instanceof SimpleAdapter)
 			{
 				((SimpleAdapter)mNativeGridView.getAdapter()).notifyDataSetChanged();
 			}
 		}
 		
 	}
 
 
 	@Override
 	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
 			long arg3) {
 
 		return true;
 
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
 		// TODO Auto-generated method stub
 		
 		ViewHolder holder = (ViewHolder)arg1.getTag();
 		Intent intent = new Intent(this, PreviewActivity.class);
 		intent.putExtra(Constants.POSITION, arg2);
 		intent.putExtra(Constants.CATEGORY_TYPE, (page + 1));
 		startActivity(intent);
 		if(holder != null)
 		{
 			WallpaperInfo wi = (WallpaperInfo)holder.image.getTag();
 		}
 		
 	}
 
 
 
 
 	@Override
 	public void setAdapter(ArrayList<WallpaperInfo> wis, int categoryType, boolean loadSuccess) {
 		// TODO Auto-generated method stub
 		if(DEBUG)Log.e(TAG, "Adapter size: " + wis.size() + "  (category = " + (categoryType - 2) + ")");
 		if(categoryType == 1)
 		{
 			mNativeGridView.setAdapter(new SimpleAdapter(getLayoutInflater(), wis, mImageLoader));
 			mImageLoader.unlock();
 		}
 		else if(categoryType == 2)
 		{
 			if(!loadSuccess)
 			{
 				previousPageIndex--;
 				previousPageIndex = Math.max(previousPageIndex, 0);
 			}
 			isLoaded = loadSuccess;
 			isLoading = false;
 			SimpleAdapter adapter = null;
 			if(mOnlineGridView.getAdapter() instanceof SimpleAdapter)
 			{
 				adapter = (SimpleAdapter)mOnlineGridView.getAdapter();
 				adapter.notifyDataSetChanged();
 			}
 			else
 			{
 				mOnlineGridView.setAdapter(new SimpleAdapter(getLayoutInflater(),wis, mImageLoader));
 				mImageLoader.unlock();
 			}
 			
 			dismissProgressBar(true);
 		}
 			
 		
 	}
 
 	@Override
 	public void onClick(View v) {
 		int id = v.getId();
 		View parent = (View)v.getParent();
 		if(parent instanceof RecommendsView)
 		{
 			RecommendsView rv = (RecommendsView)parent;
 			for(int i = 0; i < rv.getChildCount(); i++)
 			{
 				if(rv.getChildAt(i) == v)
 				{
 					if(v.getTag() instanceof CategoryInfo)
 					{
 						CategoryInfo ci = (CategoryInfo)v.getTag();
 						
 						
 						Intent intent = new Intent(this, CategoryActivity.class);
 						intent.putExtra(Constants.CATEGORY_TYPE, (ci.id + 2));
 						intent.putExtra(Constants.CATEGORY_NAME, ci.name);
 						intent.putExtra(Constants.CATEGORY_DESCRIPTION, ci.description);
 						
 						startActivity(intent);
 					}
 					
 					break;
 				}
 			}
 			return;
 		}
 		switch(id)
 		{
 		case R.id.native_btn:
 			page = 0;
 			mPager.setCurrentItem(page, true);
 			setTabTextColor();
 			break;
 		case R.id.online_btn:
 			page = 1;
 			mPager.setCurrentItem(page, true);
 			setTabTextColor();
 			break;
 		case R.id.other_btn:
 			page = 2;
 			mPager.setCurrentItem(page, true);
 			setTabTextColor();
 			break;
 		case R.id.select_rl:
 			if(isRecommandLoading)
 			{
 				display(R.string.loading_wallpaper_category);
 				break;
 			}
 			if(!isRecommandLoaded)
 			{
 				display(R.string.load_wallpaper_category_fail);
 				break;
 			}
 			int visible = recommend.getVisibility();
 			int recommendHeight = recommend.getActualHeight();
 			if(visible == View.GONE && recommendHeight > 0)
 			{
 				recommend.setVisibility(View.VISIBLE);
 				ImageView downOrUp = (ImageView)v.findViewById(R.id.down_or_up);
 				downOrUp.setImageResource(R.drawable.joy_wallpaper_arrow_up);
 				ValueAnimator downAnimator = ObjectAnimator.ofInt(0, recommendHeight);
 				downAnimator.setDuration(RECOMMEND_ANIMATION_DURATION);
 				downAnimator.setInterpolator(new LinearInterpolator());
 				downAnimator.addUpdateListener(new AnimatorUpdateListener() {
 					
 					@Override
 					public void onAnimationUpdate(ValueAnimator animation) {
 						// TODO Auto-generated method stub
 						LayoutParams lp = (LayoutParams)recommend.getLayoutParams();
 						lp.height = ((Integer)animation.getAnimatedValue()).intValue();
 						recommend.requestLayout();
 						
 						
 					}
 				});
 				downAnimator.start();
 				
 			}
 			
 			if(visible == View.VISIBLE && recommendHeight > 0)
 			{
 				ImageView downOrUp = (ImageView)v.findViewById(R.id.down_or_up);
 				downOrUp.setImageResource(R.drawable.joy_wallpaper_arrow_down);
 				ValueAnimator upAnimator = ObjectAnimator.ofInt(recommendHeight, 0);
 				upAnimator.setDuration(RECOMMEND_ANIMATION_DURATION);
 				upAnimator.setInterpolator(new LinearInterpolator());
 				upAnimator.addUpdateListener(new AnimatorUpdateListener() {
 					
 					@Override
 					public void onAnimationUpdate(ValueAnimator animation) {
 						// TODO Auto-generated method stub
 						LayoutParams lp = (LayoutParams)recommend.getLayoutParams();
 						lp.height = ((Integer)animation.getAnimatedValue()).intValue();
 						recommend.requestLayout();
 						
 						
 					}
 				});
 				upAnimator.addListener(new AnimatorListener() {
 					
 					@Override
 					public void onAnimationStart(Animator animation) {
 						// TODO Auto-generated method stub
 						
 					}
 					
 					@Override
 					public void onAnimationRepeat(Animator animation) {
 						// TODO Auto-generated method stub
 						
 					}
 					
 					@Override
 					public void onAnimationEnd(Animator animation) {
 						// TODO Auto-generated method stub
 						recommend.setVisibility(View.GONE);
 					}
 					
 					@Override
 					public void onAnimationCancel(Animator animation) {
 						// TODO Auto-generated method stub
 						
 					}
 				});
 				upAnimator.start();
 	
 			}
 			break;
 			default:
 				break;
 		}
 		
 	}
 
 	@Override
 	public void setCategoryNameAndDis(String name, String dis) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	private void showRecommendProgressBar()
 	{
 		if(mSelectRL != null)
 		{
 			View downOrUp = mSelectRL.findViewById(R.id.down_or_up);
 			View progressBar = mSelectRL.findViewById(R.id.recommend_progressbar);
 			if(downOrUp != null)downOrUp.setVisibility(View.GONE);
 			if(progressBar != null)progressBar.setVisibility(View.VISIBLE);
 		}
 	}
 	
 	private void dismissRecommendProgressBar()
 	{
 		if(mSelectRL != null)
 		{
 			View downOrUp = mSelectRL.findViewById(R.id.down_or_up);
 			View progressBar = mSelectRL.findViewById(R.id.recommend_progressbar);
 			if(downOrUp != null)downOrUp.setVisibility(View.VISIBLE);
 			if(progressBar != null)progressBar.setVisibility(View.GONE);
 		}
 	}
 
 	private void showProgressBar()
 	{
 		if(mPager != null)
 		{
 			View parent = (View)mPager.getParent();
 			View progressBar = parent.findViewById(R.id.progress_bar_bottom);
 			if(parent instanceof ViewGroup && progressBar == null)
 			{
 				progressBar = getLayoutInflater().inflate(R.layout.joy_wallpaper_progressbar_bottom, (ViewGroup) parent);
 			}
 			
 			if(progressBar != null)
 			{
 				final View p = progressBar;
 				p.setVisibility(View.VISIBLE);
 				Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_in_fast);
 				animation.setAnimationListener(new AnimationListener() {
 					
 					@Override
 					public void onAnimationStart(Animation arg0) {
 						// TODO Auto-generated method stub
 					}
 					
 					@Override
 					public void onAnimationRepeat(Animation arg0) {
 						// TODO Auto-generated method stub
 						
 					}
 					
 					@Override
 					public void onAnimationEnd(Animation arg0) {
 						// TODO Auto-generated method stub
 					}
 				});
 				p.startAnimation(animation);
 			}
 		}
 		
 	}
 	
 	private void dismissProgressBar()
 	{
 		dismissProgressBar(false);
 	}
 	
 	private void dismissProgressBar(boolean animate)
 	{
 		if(mPager != null)
 		{
 			View parent = (View)mPager.getParent();
 			final View progressBar = parent.findViewById(R.id.progress_bar_bottom);
 			if(progressBar != null)
 			{
 				if(!animate)
 				{
 					progressBar.setVisibility(View.GONE);
 					return;
 				}
 				final View p = progressBar;
 				Animation animation = AnimationUtils.loadAnimation(this, R.anim.fade_out_fast);
 				animation.setAnimationListener(new AnimationListener() {
 					
 					@Override
 					public void onAnimationStart(Animation arg0) {
 						// TODO Auto-generated method stub
 					}
 					
 					@Override
 					public void onAnimationRepeat(Animation arg0) {
 						// TODO Auto-generated method stub
 						
 					}
 					
 					
 					@Override
 					public void onAnimationEnd(Animation arg0) {
 						// TODO Auto-generated method stub
 						p.setVisibility(View.GONE);
 					}
 				});
 				p.startAnimation(animation);
 			}
 		}
 	}
 	
 	@Override
 	public void setRecommend(List<CategoryInfo> cis) {
 		// TODO Auto-generated method stub
 		if(DEBUG)Log.e(TAG, "recommend size:" + cis.size());
 		isRecommandLoaded = true;
 		isRecommandLoading = false;
 		dismissRecommendProgressBar();
 		for(int i = 0; i < recommend.getChildCount(); i++)
 		{
 			ImageView child = (ImageView)recommend.getChildAt(i);
 			if(i < cis.size())
 			{
 				child.setImageBitmap(cis.get(i).bm);
 				child.setTag(cis.get(i));
 				child.setVisibility(View.VISIBLE);
 			}
 			else
 			{
 				child.setVisibility(View.GONE);
 			}
 		}
 		if(cis.size() <=0)
 		{
 			isRecommandLoaded = false;
 			display(R.string.load_wallpaper_category_fail);
 		}
 	}
 
 
 	@Override
 	public void success(boolean s, WallpaperInfo wi) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	private void display(int textId)
 	{
 		Toast.makeText(this, textId,Toast.LENGTH_SHORT).show();
 	}
 	
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		if(mImageLoader != null)mImageLoader.unlock();
 		if(mNativeGridView != null && mNativeGridView.getAdapter() instanceof SimpleAdapter)
 		{
 			((SimpleAdapter)mNativeGridView.getAdapter()).notifyDataSetChanged();
 		}
 		
 		if(mOnlineGridView != null && mOnlineGridView.getAdapter() instanceof SimpleAdapter)
 		{
 			((SimpleAdapter)mOnlineGridView.getAdapter()).notifyDataSetChanged();
 		}
 	}
 	
 }
