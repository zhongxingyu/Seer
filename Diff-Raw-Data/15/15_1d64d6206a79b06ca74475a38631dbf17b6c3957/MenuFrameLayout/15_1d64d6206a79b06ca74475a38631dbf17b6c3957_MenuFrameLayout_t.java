 package com.joy.launcher2;
 
 
 import java.util.ArrayList;
 
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.graphics.Rect;
 import android.graphics.drawable.ColorDrawable;
 import android.graphics.drawable.Drawable;
 import android.provider.Settings;
 import android.util.AttributeSet;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.AnimationUtils;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.BaseAdapter;
 import android.widget.FrameLayout;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.joy.launcher2.preference.Preferences;
 
 //import com.joy.launcher.preference.Preferences;
 
 public class MenuFrameLayout extends FrameLayout implements OnItemClickListener{
 
 	private LinearLayout menuContent;
 	private ListView lvBody;
 	
 	MenuBodyAdapter bodyAdapter;
 	
 	private int iconSize;
 	private Launcher launcher;
 	private Rect rect;
 	
     private boolean firstLoadData = true;
 	
 	private final static int MENU_ITEM_IN_WORKSPACE = 0;
 	private final static int MENU_ITEM_IN_ALLAPP = 1;
 	private final static int MENU_ITEM_IN_BOTH = 2;
 	
 	private final static int MENU_ADD_TO_DESKTOP = 0;
 	private final static int MENU_WALLPAPER = MENU_ADD_TO_DESKTOP+1;
 	private final static int MENU_MANAGE_APPLICATION = MENU_WALLPAPER + 1;
 	//private final static int MENU_EDIT_SCREEN = MENU_MANAGE_APPLICATION + 1;
 	private final static int MENU_DESKTOP_SETTINGS = MENU_MANAGE_APPLICATION + 1;
 	//private final static int MENU_MAINMENU_EDIT = MENU_DESKTOP_SETTINGS + 1;
 	private final static int MENU_ICON_SORT = MENU_DESKTOP_SETTINGS + 1;
 	private final static int MENU_SHOW_APPS = MENU_ICON_SORT + 1;
 	private final static int MENU_HIDE_APPS = MENU_SHOW_APPS + 1;
 	//private final static int MENU_MAINMENU_SETTINGS = MENU_HIDE_APPS + 1;
 	private final static int MENU_SYSTEM_SETTINGS = MENU_HIDE_APPS + 1;
 	
 	private boolean animationFinished = false;
 	
 	ArrayList<Integer> itemPositions;	
 	ArrayList<MenuItemInfo> itemsAll;
 	
 	public MenuFrameLayout(Context context) {
 		super(context);
 		init(context);
 		// TODO Auto-generated constructor stub
 	}
 	
 	public MenuFrameLayout(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init(context);
 		// TODO Auto-generated constructor stub
 	}
 	
 	public MenuFrameLayout(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init(context);
 		// TODO Auto-generated constructor stub
 	}
 	
 	void init(Context context)
 	{
 		Resources res = context.getResources();
 		iconSize = res.getDimensionPixelSize(R.dimen.menu_item_icon_size);
 		rect = new Rect(0, 0, iconSize, iconSize);
 	}
 
 	public void setLauncher(Launcher l)
 	{
 		launcher = l;
 		if(firstLoadData)setAdapterData(false);
 	}
 	
 	@Override
 	protected void onFinishInflate() {
 		// TODO Auto-generated method stub
 		super.onFinishInflate();
 		menuContent = (LinearLayout)findViewById(R.id.menu_content);
 		resetLayout();
 		
 	}
 	
 	void resetLayout()
 	{
 		lvBody = new ListView(getContext());
 		lvBody.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
 		lvBody.setSelector(new ColorDrawable(Color.TRANSPARENT));
 
 		lvBody.setBackgroundResource(R.drawable.menu_listview_bg);
 		lvBody.setDividerHeight(4);
 		bodyAdapter = new MenuBodyAdapter(getContext());
 		lvBody.setAdapter(bodyAdapter);
 		lvBody.setOnItemClickListener(this);
 		setAdapterData(false);
 		menuContent.addView(lvBody);
 	}
 	
 
 	private void setItemsEnable()
 	{
 		boolean isHideAppsEmpty = launcher.isHideAppsEmpty();
 		boolean isShowAppsView = launcher.isShowAppsView();
 		for(int i = 0; i < itemsAll.size(); i++)
 		{
 			MenuItemInfo info = itemsAll.get(i);
 			if(i >= MENU_ICON_SORT && i <= MENU_HIDE_APPS)
 			{
 				if(i == MENU_SHOW_APPS)
 				{
 					info.enabled = isShowAppsView && !isHideAppsEmpty;
 				}
 				else
 				{
 					info.enabled = isShowAppsView;
 				}
 			}
 			else
 			{
 				info.enabled = true;
 			}
 		}
 	}
 	
 	private void setAdapterData(boolean isAllAppVisible)
 	{
 		if(launcher == null)return;
 		if(firstLoadData)
 		{
 			itemsAll = null;
 		}
 		if(itemsAll == null)
 		{
 			itemsAll = new ArrayList<MenuItemInfo>();
 			itemPositions = new ArrayList<Integer>();
 			
 			MenuItemInfo itemInfo;
 			//add online folder
 			itemInfo = new MenuItemInfo();
 	        itemInfo.textId = R.string.menu_add_folder;
 	        itemInfo.iconId = R.drawable.menu_add_folder;
 	        itemInfo.intent = null;
 	        itemInfo.selectId = MENU_ADD_TO_DESKTOP;
 	        itemsAll.add(itemInfo);
 	        itemPositions.add(MENU_ITEM_IN_WORKSPACE);
 //shield by yongjian.he for live_wallpaper app.			
 //			Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
 //	        Intent chooser = Intent.createChooser(pickWallpaper,
 //	        		getContext().getResources().getString(R.string.chooser_wallpaper));
 	        Intent chooser = new Intent();
 	        ComponentName componentName = new ComponentName("com.joy.launcher2",
 	        		"com.joy.launcher2.wallpaper.WallpaperActivity");
 	        chooser.setComponent(componentName);
 	        
 	        itemInfo = new MenuItemInfo();
 	        itemInfo.textId = R.string.menu_wallpaper;
 	        itemInfo.iconId = R.drawable.menu_wallpaper;
 	        itemInfo.intent = chooser;
 	        itemInfo.selectId = MENU_WALLPAPER;
 	        itemsAll.add(itemInfo);
 	        itemPositions.add(MENU_ITEM_IN_WORKSPACE);
 			
 			Intent manageApps = new Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS);
 	        manageApps.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
 	                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
 	        itemInfo = new MenuItemInfo();
 	        itemInfo.textId = R.string.menu_manage_apps;
 	        itemInfo.iconId = R.drawable.menu_manage_apps;
 	        itemInfo.intent = manageApps;
 	        itemInfo.selectId = MENU_MANAGE_APPLICATION;
 	        itemsAll.add(itemInfo);
 	        itemPositions.add(MENU_ITEM_IN_WORKSPACE);	        	        
 	        
 	       /* itemInfo = new MenuItemInfo();
 	        itemInfo.textId = R.string.menu_edit_screen;
 	        itemInfo.iconId = R.drawable.menu_edit_screen;
 	        itemInfo.intent = null;
 	        itemInfo.selectId = MENU_EDIT_SCREEN;
 	        itemsAll.add(itemInfo);
 	        itemPositions.add(MENU_ITEM_IN_WORKSPACE);*/
 	        
 	        itemInfo = new MenuItemInfo();
 	        itemInfo.textId = R.string.menu_desktop_settings;
 	        itemInfo.iconId = R.drawable.menu_desktop_settings;
 	        
 	        //open desktop settings
 	        Intent preferences = new Intent().setClass(launcher, Preferences.class);
 	        preferences.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
 	                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
 	        itemInfo.intent = preferences;
 	        itemInfo.selectId = MENU_DESKTOP_SETTINGS;
 	        itemsAll.add(itemInfo);
 	        itemPositions.add(MENU_ITEM_IN_WORKSPACE);
 	        
 	        /*itemInfo = new MenuItemInfo();
 	        itemInfo.textId = R.string.menu_mainmenu_edit;
 	        itemInfo.iconId = R.drawable.menu_mainmenu_edit;
 	        itemInfo.intent = null;
 	        itemInfo.selectId = MENU_MAINMENU_EDIT;
 	        itemsAll.add(itemInfo);
 	        itemPositions.add(MENU_ITEM_IN_ALLAPP);*/
 	        
 	        itemInfo = new MenuItemInfo();
 	        itemInfo.textId = R.string.menu_icon_sort;
 	        itemInfo.iconId = R.drawable.menu_icon_sort;
 	        itemInfo.intent = null;
 	        itemInfo.selectId = MENU_ICON_SORT;
 	        itemsAll.add(itemInfo);
 	        itemPositions.add(MENU_ITEM_IN_ALLAPP);
 	        
 	        itemInfo = new MenuItemInfo();
 	        itemInfo.textId = R.string.menu_show_apps;
 	        itemInfo.iconId = R.drawable.menu_show_apps;
 	        itemInfo.intent = null;
 	        itemInfo.selectId = MENU_SHOW_APPS;
 	        itemsAll.add(itemInfo);
 	        itemPositions.add(MENU_ITEM_IN_ALLAPP);
 	        
 	        itemInfo = new MenuItemInfo();
 	        itemInfo.textId = R.string.menu_hide_apps;
 	        itemInfo.iconId = R.drawable.menu_hide_apps;
 	        itemInfo.intent = null;
 	        itemInfo.selectId = MENU_HIDE_APPS;
 	        itemsAll.add(itemInfo);
 	        itemPositions.add(MENU_ITEM_IN_ALLAPP);
 	        
 	        /*itemInfo = new MenuItemInfo();
 	        itemInfo.textId = R.string.menu_mainmenu_settings;
 	        itemInfo.iconId = R.drawable.menu_mainmenu_settings;
 	        itemInfo.intent = null;
 	        itemInfo.selectId = MENU_MAINMENU_SETTINGS;
 	        itemsAll.add(itemInfo);
 	        itemPositions.add(MENU_ITEM_IN_ALLAPP);*/
 	        
 	        Intent settings = new Intent(android.provider.Settings.ACTION_SETTINGS);
 	        settings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
 	                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);	        
 	        itemInfo = new MenuItemInfo();
 	        itemInfo.textId = R.string.menu_settings;
 	        itemInfo.iconId = R.drawable.menu_system_settings;
 	        itemInfo.intent = settings;
 	        itemInfo.selectId = MENU_SYSTEM_SETTINGS;
 	        itemsAll.add(itemInfo);
 	        itemPositions.add(MENU_ITEM_IN_BOTH);
 	        
 	        
 		}
 		
 		setItemsEnable();
 
 		if(bodyAdapter.items == null)
 		{
 			bodyAdapter.items = new ArrayList<MenuFrameLayout.MenuItemInfo>();
 			for(int i = 0; i < itemsAll.size(); i++)
 			{
 				bodyAdapter.items.add(itemsAll.get(i));
 			}			
 		}
 
 		
 		if(bodyAdapter.items.size() == 0)
 		{
 			for(int i = 0; i < itemsAll.size(); i++)
 			{
 				bodyAdapter.items.add(itemsAll.get(i));
 			}
 		}
 		else
 		{
 			for(int i = 0; i < itemPositions.size(); i++)
 			{
 				if(bodyAdapter.items.get(i) != itemsAll.get(i))
 				{
 					bodyAdapter.items.add(i, itemsAll.get(i));
 				}
 			}
 		}
 		
 		int removeItem = isAllAppVisible?MENU_ITEM_IN_WORKSPACE:MENU_ITEM_IN_ALLAPP; 
 		for(int i = 0; i < itemPositions.size(); i++)
 		{
 			int position = itemPositions.get(i);
 			if(removeItem == position)
 			{
 				bodyAdapter.items.remove(itemsAll.get(i));
 			}
 		}
 		
 		if(firstLoadData)
 		{
 			firstLoadData = false;
 		}
 		else
 		{
 			bodyAdapter.notifyDataSetChanged();
 		}
 		
 	}
 	
 	static class MenuItemInfo
 	{
 		int textId;
 		int iconId;
 		Intent intent;
 		int selectId;
 		boolean enabled = true;
 	}
 
 	public void show(boolean animate, boolean isAllAppVisible)
 	{
 		setAdapterData(isAllAppVisible);
 		if(getVisibility() != View.VISIBLE)
 		{
 			if(animate)
 			{
 				if(menuContent != null)
 				{
 					if(!animationFinished)
 					{
 						bringToFront();
 						setVisibility(View.VISIBLE);
 					
 						Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.menu_enter);
 						animation.setAnimationListener(new AnimationListener() {
 							
 							@Override
 							public void onAnimationStart(Animation arg0) {
 								// TODO Auto-generated method stub
 								animationFinished = true;
 							}
 							
 							@Override
 							public void onAnimationRepeat(Animation arg0) {
 								// TODO Auto-generated method stub
 								
 							}
 							
 							@Override
 							public void onAnimationEnd(Animation arg0) {
 								// TODO Auto-generated method stub
 								animationFinished = false;
 							}
 						});
 						menuContent.startAnimation(animation);
 					}
 					
 				}
 			}
 			else
 			{
 				bringToFront();
 				setVisibility(View.VISIBLE);
 			}
 			
 		}
 	}
 	
 	public void dismiss(boolean animate)
 	{
 		if(getVisibility() != View.GONE)
 		{
 			if(animate)
 			{
 				//setVisibility(View.GONE);
 				if(menuContent != null)
 				{
 					if(!animationFinished)
 					{
 						Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.menu_exit);
 						animation.setAnimationListener(new AnimationListener() {
 							
 							@Override
 							public void onAnimationStart(Animation animation) {
 								// TODO Auto-generated method stub
 								animationFinished = true;
 							}
 							
 							@Override
 							public void onAnimationRepeat(Animation animation) {
 								// TODO Auto-generated method stub
 								
 							}
 							
 							@Override
 							public void onAnimationEnd(Animation animation) {
 								// TODO Auto-generated method stub
 								MenuFrameLayout.this.setVisibility(View.GONE);
 								animationFinished = false;
 							}
 						});
 						menuContent.startAnimation(animation);
 					}
 					
 				}
 			}
 			else
 			{
 				setVisibility(View.GONE);
 			}
 			
 		}
 	}
	
	public boolean isAnimationFinished()
	{
		return animationFinished;
	}
 
 	@Override
 	public boolean onInterceptHoverEvent(MotionEvent event) {
 		// TODO Auto-generated method stub
 		return super.onInterceptHoverEvent(event);
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 		// TODO Auto-generated method stub
 		super.onTouchEvent(event);
 		int action = event.getAction();
 		int x = (int)event.getX();
 		int y = (int)event.getY();
 		if(action == MotionEvent.ACTION_UP)
 		{
 			if(menuContent != null)
 			{
 				Rect frame = new Rect();
 				menuContent.getHitRect(frame);
 				if(!frame.contains(x,y))
 				{
 					dismiss(true);
 				}
 			}
 		}
 		return true;
 	}
 	
     class MenuBodyAdapter extends BaseAdapter {
 		private Context mContext;
 
 		private LayoutInflater inflater;
 		
 		
 		ArrayList<MenuItemInfo> items;
 
 		public MenuBodyAdapter(Context context) 
 		{
 			this.mContext = context;
 			this.inflater = LayoutInflater.from(context);
 		}
 		public int getCount() {
 			if(items == null)return 0;
 			return items.size();
 		}
 		public Object getItem(int position) {
 			
 			return null;
 		}
 		public long getItemId(int position) {
 			return 0;
 		}
 		
 		public View getView(int position, View convertView, ViewGroup parent) {
 			
 			if(convertView == null)
 			{
 				convertView = (TextView)inflater.inflate(R.layout.menu_item, null);
 				
 			}
 			convertView.setTag(items.get(position));
 			TextView tv = (TextView)convertView;
 			tv.setText(items.get(position).textId);
 			Drawable d = mContext.getResources().getDrawable(items.get(position).iconId);
 			d.setBounds(rect);
 			tv.setCompoundDrawables(d, null, null, null);
 			convertView.setEnabled(items.get(position).enabled);
 			return convertView;
 		}
 		
 		@Override
 		public boolean isEnabled(int position) {
 			// TODO Auto-generated method stub
 			return items.get(position).enabled;
 		}
 		
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
 	{
 		if(!(view.getTag() instanceof MenuItemInfo))
 		{
 			dismiss(true);
 			return;
 		}
 	
 		MenuItemInfo menuInfo = ((MenuItemInfo)view.getTag());
 		int selectId = menuInfo.selectId;
 		Intent data = menuInfo.intent;
 		switch(selectId)
 		{
 		case MENU_ADD_TO_DESKTOP:
 			launcher.showAddToDesktop();
 			break;
 		case MENU_WALLPAPER:
 			if(data != null && launcher != null)
 			{
 				launcher.startWallpaper(data);
 			}
 			break;
 		case MENU_MANAGE_APPLICATION:
 			if(data != null && launcher != null)
 			{
 				launcher.startActivity(data);
 			}	
 			break;
 		/*case MENU_EDIT_SCREEN:
 			break;*/
 		case MENU_DESKTOP_SETTINGS:
 			if(data != null && launcher != null)
 			{
 				launcher.startActivity(data);
 			}	
 			break;
 		/*case MENU_MAINMENU_EDIT:
 			break;*/
 		case MENU_ICON_SORT:
 			if(launcher!= null)
 			{
 				launcher.showSortMenu();
 			}
 			break;
 		case MENU_SHOW_APPS:
 			if(launcher != null)
 			{
 				launcher.setAppsShowOrHide(true);
 			}
 			break;
 		case MENU_HIDE_APPS:
 			if(launcher != null)
 			{
 				launcher.setAppsShowOrHide(false);
 			}
 			break;
 		/*case MENU_MAINMENU_SETTINGS:
 			break;*/
 		case MENU_SYSTEM_SETTINGS:
 			if(data != null && launcher != null)
 			{
 				launcher.startActivity(data);
 			}	
 			break;
 		default:
 			break;
 		}
 		dismiss(true);
 	}
 	
 
 }
