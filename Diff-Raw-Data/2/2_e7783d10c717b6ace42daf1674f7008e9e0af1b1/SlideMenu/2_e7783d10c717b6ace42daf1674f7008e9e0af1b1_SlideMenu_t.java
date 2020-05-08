 package com.lsg.app.lib;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Rect;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.ViewTreeObserver;
 import android.view.Window;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.AnimationSet;
 import android.view.animation.TranslateAnimation;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.lsg.app.Events;
 import com.lsg.app.ExtendedPagerTabStrip;
 import com.lsg.app.ExtendedViewPager;
 import com.lsg.app.Functions;
 import com.lsg.app.HelpAbout;
 import com.lsg.app.InfoActivity;
 import com.lsg.app.R;
 import com.lsg.app.SMVBlog;
 import com.lsg.app.Settings;
 import com.lsg.app.SettingsAdvanced;
 import com.lsg.app.SetupAssistant;
 import com.lsg.app.TimeTable;
 import com.lsg.app.VPlan;
 
 public class SlideMenu {
 	public static class SlideMenuAdapter extends ArrayAdapter<SlideMenu.SlideMenuAdapter.MenuDesc> {
 		Activity act;
 		SlideMenu.SlideMenuAdapter.MenuDesc[] items;
 		class MenuItem {
 			public TextView label;
 			public TextView title;
 			public ImageView icon;
 		}
 		static class MenuDesc {
 			public boolean useSlideMenu = true;
 			public int type = Functions.TYPE_PAGE;
 			public int icon;
 			public String label;
 			public String title;
 			public Class<?extends Activity> action;
 			public Intent actIntent;
 		}
 		public SlideMenuAdapter(Activity act, SlideMenu.SlideMenuAdapter.MenuDesc[] items) {
 			super(act, R.id.menu_label, items);
 			this.act = act;
 			this.items = items;
 			}
 		@Override
         public int getItemViewType(int position) {
             return items[position].type;
         }
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View rowView = convertView;
 			if (rowView == null
 					|| (items[position].type == Functions.TYPE_INFO && rowView
 							.findViewById(R.id.title) == null)
 					|| (items[position].type == Functions.TYPE_PAGE && rowView
 							.findViewById(R.id.title) != null)) {
 				LayoutInflater inflater = act.getLayoutInflater();
 				MenuItem viewHolder = new MenuItem();
 				switch(getItemViewType(position)) {
 				case Functions.TYPE_PAGE:
 					rowView = inflater.inflate(R.layout.menu_listitem, null);
 					viewHolder.title = null;
 					break;
 				case Functions.TYPE_INFO:
 					rowView = inflater.inflate(R.layout.menu_info, null);
 					viewHolder.title = (TextView) rowView.findViewById(R.id.title);
 					break;
 				}
 				viewHolder.icon = (ImageView) rowView.findViewById(R.id.menu_icon);
 				viewHolder.label = (TextView) rowView.findViewById(R.id.menu_label);
 				rowView.setTag(viewHolder);
 			}
 
 			MenuItem holder = (MenuItem) rowView.getTag();
 			String s = items[position].label;
 			holder.label.setText(s);
 			holder.icon.setImageResource(items[position].icon);
 			
 			if(holder.title != null) {
 				if(items[position].title != null) {
 					holder.title.setText(items[position].title);
 					holder.title.setVisibility(View.VISIBLE);
 				}
 				else
 					holder.title.setVisibility(View.GONE);
 			}
 
 			return rowView;
 		}
 	}
 	
 	private static boolean menuShown = false;
 	private static boolean menuToHide = false;
 	private static View menu;
 	private static LinearLayout content;
 	private static FrameLayout parent;
 	private static int menuSize;
 	private static int statusHeight = 0;
 	private Activity act;
 	private static Class<? extends Activity> curAct;
 	private SharedPreferences prefs;
 	SlideMenuAdapter.MenuDesc[] items;
 	public SlideMenu(Activity act, Class<? extends Activity> curAct) {
 		this.act = act;
 		SlideMenu.curAct = curAct;
 		prefs = PreferenceManager.getDefaultSharedPreferences(act);
 	}
 	public void checkEnabled() {
     	content = ((LinearLayout) act.findViewById(android.R.id.content).getParent());
 		content.setBackgroundResource(R.layout.background);
 		
 		parent = (FrameLayout) content.getParent();
     	LayoutInflater inflater = (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     	menu = inflater.inflate(R.layout.menu, null);
     	FrameLayout.LayoutParams lays = new FrameLayout.LayoutParams(-1, -1, 3);
     	lays.setMargins(20000, 20000, 0, 0);
     	menu.setLayoutParams(lays);
     	content.bringToFront();
     	parent.addView(menu);
     	
     	menu.getViewTreeObserver().addOnGlobalLayoutListener(
 				new ViewTreeObserver.OnGlobalLayoutListener() {
 					@SuppressWarnings("deprecation")
 					@Override
 					public void onGlobalLayout() {
 						menu.getViewTreeObserver().removeGlobalOnLayoutListener(this);
 						if(menuToHide) {
 							hide();
 							menuToHide = false;
 						}
 						}
 					});
     	fill();
 		if(menuShown)
 			this.show(false);
 	}
 	public void show() {
 		if(statusHeight == 0) {
 			Rect rectgle = new Rect();
 			Window window = act.getWindow();
 			window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
 			statusHeight = rectgle.top;
 			}
 		this.show(true, 0);
 	}
 
 	public void show(boolean animate) {
 		show(animate, 0);
 	}
 	ViewGroup _content;
 	public void show(boolean animate, int offset) {
     	menuSize = Functions.dpToPx(250, act);
     	if(offset == 0)
     		offset = menuSize;
     	FrameLayout.LayoutParams parm = (FrameLayout.LayoutParams) content.getLayoutParams();
     	parm.setMargins(menuSize, 0, -menuSize, 0);
     	content.setLayoutParams(parm);
     	FrameLayout.LayoutParams lays = new FrameLayout.LayoutParams(-1, -1, 3);
     	lays.setMargins(0,statusHeight, 0, 0);
     	menu.setLayoutParams(lays);
     	try {
 			_content = ((LinearLayout) act.findViewById(android.R.id.content).getParent());
 		}
 		catch(ClassCastException e) {
 			/*
 			 * When there is no title bar (android:theme="@android:style/Theme.NoTitleBar"),
 			 * the android.R.id.content FrameLayout is directly attached to the DecorView,
 			 * without the intermediate LinearLayout that holds the titlebar plus content.
 			 */
 			_content = (FrameLayout) act.findViewById(android.R.id.content);
 		}
 		FrameLayout.LayoutParams parms = new FrameLayout.LayoutParams(-1, -1, 3);
 		parms.setMargins(menuSize, 0, -menuSize, 0);
 		content.setLayoutParams(parms);
     	
     	ListView list = (ListView) act.findViewById(R.id.menu_listview);
     	list.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				if (items[position].action == null || !items[position].action.equals(curAct)) {
 					Log.d("pos", Long.valueOf(id).toString());
 					if (items[position].useSlideMenu)
 						menuToHide = true;
 					if (items[position].action != null) {
 						Intent intent = new Intent(act, items[position].action);
 						intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
 						act.startActivity(intent);
 					} else
 						act.startActivity(items[position].actIntent);
 				} else {
 					hide();
 				}
 				}
 			});
     	
     	Functions.enableDisableViewGroup((LinearLayout) parent.findViewById(android.R.id.content).getParent(), false);
     	try {    		
     		((ExtendedViewPager) act.findViewById(R.id.viewpager)).setPagingEnabled(false);
 			((ExtendedPagerTabStrip) act.findViewById(R.id.viewpager_tabs))
 					.setNavEnabled(false);
 		} catch (Exception e) {
 			// no viewpager to disable :)
 		}
 		if (animate) {
 			TranslateAnimation slideoutanim = new TranslateAnimation(-offset, 0,
 					0, 0);
 			slideoutanim.setDuration(Math.abs(offset) * 500 / menuSize);
 			
 			TranslateAnimation slideinanim = new TranslateAnimation(
 					-(offset / 2), 0, 0, 0);
 			slideinanim.setDuration(Math.abs(offset) * 500 / menuSize);
 			menu.startAnimation(slideinanim);
 			content.startAnimation(slideoutanim);
 
 			content.bringToFront();
 			slideinanim.setAnimationListener(new AnimationListener() {
 				@Override
 				public void onAnimationEnd(Animation animation) {
 					// to enable view that is clicked for slide-back
 					menu.bringToFront();
 				}
 				@Override
 				public void onAnimationRepeat(Animation animation) {
 					// not needed here
 				}
 				@Override
 				public void onAnimationStart(Animation animation) {
 					// not needed here
 				}
 			});
 		}
 		menuShown = true;
     	menu.findViewById(R.id.overlay).setOnClickListener(new OnClickListener() {
     		@Override
     		public void onClick(View v) {
     			//SlideMenu.this.hide();
     			//need this to get onTouch, don't know why
     		}
     	});
     	
 		((FrameLayout) menu.findViewById(R.id.overlay)).setOnTouchListener(new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				
 				switch(event.getAction()) {
 				case MotionEvent.ACTION_DOWN:
 					xPos = event.getX();
 					Log.d("xpos", Float.valueOf(xPos).toString());
 					content_lays = (FrameLayout.LayoutParams) content.getLayoutParams();
 					menu_lays = (FrameLayout.LayoutParams) menu.getLayoutParams();
 			    	content.bringToFront();
 					break;
 				case MotionEvent.ACTION_MOVE:
 					int diff = Float.valueOf(xPos - event.getX()).intValue();
 			    	prevX = lastX;
 					lastX = event.getX();
 					if(diff < 0)
 						diff = 0;
 					if(lastDiff == diff || lastDiff -1 == diff)
 						break;
 					lastDiff = diff;
 					if(lastDiff < maxDiff)
 						maxDiff = lastDiff;
 
 			    	content_lays.setMargins(menuSize - diff, 0, -menuSize + diff, 0);
 			    	content.setLayoutParams(content_lays);
 			    	
 			    	menu_lays.setMargins(-diff / 2, statusHeight, diff / 2, 0);
 			    	menu.setLayoutParams(menu_lays);
 					break;
 				case MotionEvent.ACTION_UP:
 					Log.d("xPos", Float.valueOf(xPos).toString());
 					Log.d("lastX", Float.valueOf(lastX).toString());
 					Log.d("prevX", Float.valueOf(prevX).toString());
 					Log.d("pos", Float.valueOf(event.getX()).toString());
 					if(prevX < event.getX() && lastDiff > Functions.dpToPx(5, act)) {
 						menu.bringToFront();
 						show(true, lastDiff);
 					} else
 						hide(lastDiff);
 					break;
 				}
 				return false;
 			}
 		});
 	}
 	private FrameLayout.LayoutParams content_lays;
 	private FrameLayout.LayoutParams menu_lays;
 	private float xPos;
 	private float lastX;
 	private float prevX;
 	private int maxDiff = 0;
 	private int lastDiff;
 	public void hide() {
 		hide(0);
 	}
 	public void hide(int offset) {
 		AnimationSet menuAnimations = new AnimationSet(true);
 		menuAnimations.setDuration(500);
 		/*AlphaAnimation menuFadeOut = new AlphaAnimation(1.0F, 0.0F);
 		menuFadeOut.setDuration(500);*/
 		TranslateAnimation menuSlideOut = new TranslateAnimation(0, -((menuSize - offset) / 3), 0, 0);
 		menuSlideOut.setDuration(Math.abs(menuSize - offset) *500 / menuSize);
 		//menuAnimations.addAnimation(menuFadeOut);
 		menuAnimations.addAnimation(menuSlideOut);
 		menu.startAnimation(menuSlideOut);
 		
 		TranslateAnimation content_in = new TranslateAnimation(menuSize - offset, 0, 0, 0);
 		content_in.setDuration(Math.abs(menuSize - offset) *500 / menuSize);
 		content.startAnimation(content_in);
 		//((LinearLayout) act.findViewById(android.R.id.content).getParent()).bringToFront();
 		FrameLayout.LayoutParams parm = (FrameLayout.LayoutParams) content.getLayoutParams();
     	parm.setMargins(0, 0, 0, 0);
     	content.setLayoutParams(parm);
     	
     	Functions.enableDisableViewGroup((LinearLayout) parent.findViewById(android.R.id.content).getParent(), true);
     	try {
     		((ExtendedViewPager) act.findViewById(R.id.viewpager)).setPagingEnabled(true);
     		((ExtendedPagerTabStrip) act.findViewById(R.id.viewpager_tabs)).setNavEnabled(true);
     	} catch(Exception e) {
     		//no viewpager :)
     	}
     	menuShown = false;
 		content.bringToFront();
 		_content.bringToFront();
 		parent.invalidate();
 		menu.invalidate();
 		content.invalidate();
 		menuSlideOut.setAnimationListener(new AnimationListener() {
 			
 			@Override
 			public void onAnimationStart(Animation animation) {
 			}
 			
 			@Override
 			public void onAnimationRepeat(Animation animation) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void onAnimationEnd(Animation animation) {
 
 				FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) menu.getLayoutParams();
 				params.setMargins(20000, 20000, 0, 0);
 				menu.setLayoutParams(params);
 			}
 		});
 	}
 	public void fill() {
 		ListView list = (ListView) act.findViewById(R.id.menu_listview);
 		if (prefs.getBoolean(Functions.IS_LOGGED_IN, false)) {
 			items = new SlideMenuAdapter.MenuDesc[9];
 			for (int i = 0; i < 9; i++) {
 				items[i] = new SlideMenuAdapter.MenuDesc();
 			}
 			items[0].icon = R.drawable.ic_timetable;
 			items[0].label = "Stundenplan";
 			items[0].action = TimeTable.class;
			items[1].icon = R.drawable.ic_vplan_green;
 			items[1].label = "Vertretungsplan";
 			items[1].action = VPlan.class;
 			items[2].icon = R.drawable.ic_events;
 			items[2].label = "Termine";
 			items[2].action = Events.class;
 			items[3].icon = R.drawable.ic_smv;
 			items[3].label = "SMVBlog";
 			items[3].action = SMVBlog.class;
 			items[4].icon = R.drawable.ic_settings;
 			items[4].label = "Einstellungen";
 			items[4].action = (Functions.getSDK() >= 11) ? SettingsAdvanced.class
 					: Settings.class;
 			items[5].icon = R.drawable.ic_help;
 			items[5].label = "Hilfe";
 			items[5].action = null;
 			items[5].actIntent = new Intent(act, HelpAbout.class);
 			items[5].actIntent.putExtra(Functions.HELPABOUT, Functions.help);
 			items[5].useSlideMenu = false;
 			items[6].icon = R.drawable.ic_about;
 			items[6].label = "Über";
 			items[6].action = null;
 			items[6].actIntent = new Intent(act, HelpAbout.class);
 			items[6].actIntent.putExtra(Functions.HELPABOUT, Functions.about);
 			items[6].useSlideMenu = false;
 			String news_pupils = prefs.getString(Functions.NEWS_PUPILS, "");
 			items[7].type = Functions.TYPE_INFO;
 			items[7].title = "Aktuell";
 			items[7].icon = R.drawable.ic_launcher;
 			items[7].label = news_pupils.substring(0,
 					((news_pupils.length() > 60) ? 60 : news_pupils.length()))
 					+ ((news_pupils.length() > 60) ? "..." : "");
 			items[7].action = null;
 			items[7].actIntent = new Intent(act, InfoActivity.class);
 			items[7].actIntent.putExtra("type", "info");
 			items[7].actIntent.putExtra("info_type", "pupils");
 			items[7].useSlideMenu = false;
 			if (prefs.getBoolean(Functions.RIGHTS_TEACHER, false)
 					|| prefs.getBoolean(Functions.RIGHTS_ADMIN, false)) {
 				String news_teachers = prefs.getString(Functions.NEWS_TEACHERS,
 						"");
 				items[8].type = Functions.TYPE_INFO;
 				items[8].title = "Lehrerinfo";
 				items[8].icon = R.drawable.ic_launcher;
 				items[8].label = news_teachers.substring(0, ((news_teachers
 						.length() > 60) ? 60 : news_teachers.length()))
 						+ ((news_teachers.length() > 60) ? "..." : "");
 				items[8].actIntent = new Intent(act, InfoActivity.class);
 				items[8].actIntent.putExtra("type", "info");
 				items[8].actIntent.putExtra("info_type", "teachers");
 				items[8].useSlideMenu = false;
 			}
 		} else {
 
 			items = new SlideMenuAdapter.MenuDesc[3];
 			for (int i = 0; i < 3; i++) {
 				items[i] = new SlideMenuAdapter.MenuDesc();
 			}
 			items[0].icon = R.drawable.ic_launcher;
 			items[0].label = "Setup-Assistent";
 			items[0].action = SetupAssistant.class;
 			items[1].icon = R.drawable.ic_launcher;
 			items[1].label = "Termine";
 			items[1].action = Events.class;
 			items[2].icon = R.drawable.ic_launcher;
 			items[2].label = "SMVBlog";
 			items[2].action = SMVBlog.class;
 		}
 		SlideMenuAdapter adap = new SlideMenuAdapter(act, items);
 		list.setAdapter(adap);
 	}
 }
