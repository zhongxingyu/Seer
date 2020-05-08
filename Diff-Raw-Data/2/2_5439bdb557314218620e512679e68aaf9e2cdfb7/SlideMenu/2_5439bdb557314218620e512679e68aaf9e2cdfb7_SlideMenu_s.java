 package com.lsg.app.lib;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Rect;
 import android.preference.PreferenceManager;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.view.ViewTreeObserver;
 import android.view.Window;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
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
 import com.lsg.app.Functions;
 import com.lsg.app.HelpAbout;
 import com.lsg.app.InfoActivity;
 import com.lsg.app.MainActivity;
 import com.lsg.app.R;
 import com.lsg.app.SMVBlog;
 import com.lsg.app.Settings;
 import com.lsg.app.SettingsAdvanced;
 import com.lsg.app.SetupAssistant;
 import com.lsg.app.VPlan;
 import com.lsg.app.tasks.TasksOverView;
 import com.lsg.app.timetable.TimeTableFragment;
 import com.lsg.app.widget.CustomFrameLayout;
 
 public class SlideMenu implements OnTouchListener {
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
 			public Class<?extends Activity> openActivity;
 			public Class<?extends Fragment> openFragment;
 			public Class<?extends Activity> containerActivity;
 			public Intent openIntent;
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
 	private static LinearLayout contentContainer;
 	private static FrameLayout decorView;
 	private static int menuSize;
 	private static int statusBarHeight = 0;
 	private Activity act;
 	private static Class<? extends Activity> curAct;
 	private static Class<? extends Fragment> fragment;
 	private SharedPreferences prefs;
 	SlideMenuAdapter.MenuDesc[] items;
 
 	public SlideMenu(Activity act, Class<? extends Activity> curAct) {
 		this.act = act;
 		SlideMenu.curAct = curAct;
 		prefs = PreferenceManager.getDefaultSharedPreferences(act);
 		contentContainer = ((LinearLayout) act.findViewById(
 				android.R.id.content).getParent());
 		(act.findViewById(android.R.id.content))
 				.setBackgroundResource(R.layout.background);
 
 		decorView = (FrameLayout) contentContainer.getParent();
 		LayoutInflater inflater = (LayoutInflater) act
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 		menu = inflater.inflate(R.layout.menu, null);
 		menuLayoutParams = new FrameLayout.LayoutParams(
 				FrameLayout.LayoutParams.WRAP_CONTENT,
 				FrameLayout.LayoutParams.MATCH_PARENT, 3);
 		menuLayoutParams.setMargins(20000, 20000, 0, 0);
 		menu.setLayoutParams(menuLayoutParams);
 
 		decorView.removeAllViews();
 		FrameLayout.LayoutParams parentLays = new FrameLayout.LayoutParams(
 				FrameLayout.LayoutParams.MATCH_PARENT,
 				FrameLayout.LayoutParams.MATCH_PARENT);
 		CustomFrameLayout parent = new CustomFrameLayout(act);
 		parent.setLayoutParams(parentLays);
 		decorView.addView(parent);
 
 		FrameLayout.LayoutParams contentLays = new FrameLayout.LayoutParams(
 				FrameLayout.LayoutParams.MATCH_PARENT,
 				FrameLayout.LayoutParams.MATCH_PARENT);
 		contentContainer.setLayoutParams(contentLays);
 		// menu added before content, to have in back
 		parent.addView(menu);
 		parent.addView(contentContainer);
 		parent.setOnTouchListener(this);
 		parent.setOnTouchIntercept(new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				motionStartX = event.getX();
 				if (event.getAction() == MotionEvent.ACTION_DOWN
 						&& event.getX() > menuSize && menuShown) {
 					contentContainerLayoutParams = (FrameLayout.LayoutParams) contentContainer
 							.getLayoutParams();
 					contentContainerLayoutParams.setMargins(0, 0, 0, 0);
 					contentContainer.setLayoutParams(contentContainerLayoutParams);
 					contentContainer.scrollTo(-menuSize, 0);
 					return true;
 				} else
 				return false;
 			}
 		});
     	
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
 	public boolean handleBack() {
 		if(menuShown) {
 			hide();
 			return true;
 		}
 		return false;
 	}
 	public void setFragment(Class<?extends Fragment> fragment) {
 		this.fragment = fragment;
 	}
 	public void show() {
 		if(statusBarHeight == 0) {
 			Rect rectgle = new Rect();
 			Window window = act.getWindow();
 			window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
 			statusBarHeight = rectgle.top;
 			}
 		this.show(true, 0);
 	}
 
 	public void show(boolean animate) {
 		show(animate, 0);
 	}
 	//LayoutParams to move content & menu around
 	private FrameLayout.LayoutParams contentContainerLayoutParams;
 	private FrameLayout.LayoutParams menuLayoutParams;
 	//store motion events
 	private float motionStartX;
 	private float lastX;
 	private float previousX;
 	private int maxDiff = 0;
 	private int lastDiff;
 	public void show(boolean animate, int offset) {
     	menuSize = Functions.dpToPx(250, act);
     	if(offset == 0)
     		offset = menuSize;
     	
     	//move content & ActionBar out to right
     	contentContainerLayoutParams = (FrameLayout.LayoutParams) contentContainer.getLayoutParams();
     	contentContainerLayoutParams.setMargins(menuSize, 0, -menuSize, 0);
     	contentContainer.setLayoutParams(contentContainerLayoutParams);
     	
     	//set menu to left side
     	menuLayoutParams = new FrameLayout.LayoutParams(-1, -1, 3);
     	menuLayoutParams.setMargins(0, statusBarHeight, 0, 0);
     	menu.setLayoutParams(menuLayoutParams);
     	
     	//onClick management for menu ListView
     	ListView list = (ListView) act.findViewById(R.id.menu_listview);
     	list.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				// if not clicked item for current Activity
 				if(fragment != null && fragment.equals(items[position].openFragment))
 					hide();
 				else if ((items[position].openActivity == null
 						|| !items[position].openActivity.equals(curAct))) {
 					// mark this menu to be hidden
 					if (items[position].useSlideMenu)
 						menuToHide = true;
 					// start new activity / intent
 					if (items[position].openActivity != null) {
 						Intent intent = new Intent(act,
 								items[position].openActivity);
 						intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
 						act.startActivity(intent);
 
 					} else if (items[position].containerActivity != null) {
 						if (!items[position].containerActivity.equals(curAct)) {
 							Intent intent = new Intent(act,
 									items[position].containerActivity);
 							intent.putExtra("fragment",
 									items[position].openFragment);
 							act.startActivity(intent);
 						} else {
 							Log.d("slide", "change fragment");
 							((FragmentActivityCallbacks) act)
 									.changeFragment(items[position].openFragment);
 							hide();
 						}
 					} else
 						act.startActivity(items[position].openIntent);
 				} else {
 					hide();
 				}
 			}
 		});
     	
 		if (animate) {
 			// slide out content
 			TranslateAnimation contentSlideOut = new TranslateAnimation(
 					-offset, 0, 0, 0);
 			contentSlideOut.setDuration(Math.abs(offset) * 500 / menuSize);
 			contentContainer.startAnimation(contentSlideOut);
 
 			// slide in menu
 			TranslateAnimation menuSlideIn = new TranslateAnimation(
 					-(offset / 2), 0, 0, 0);
 			menuSlideIn.setDuration(Math.abs(offset) * 500 / menuSize);
 			menu.startAnimation(menuSlideIn);
 		}
 		menuShown = true;
 	}
 	public void hide() {
 		hide(0);
 	}
 	public void hide(int offset) {
 		//slide out menu to left
		TranslateAnimation menuSlideOut = new TranslateAnimation(0, -((menuSize - offset) / 3), 0, 0);
 		menuSlideOut.setDuration(Math.abs(menuSize - offset) *500 / menuSize);
 		menu.startAnimation(menuSlideOut);
 		
 		//slide in content from right
 		TranslateAnimation contentSlideIn = new TranslateAnimation(menuSize - offset, 0, 0, 0);
 		contentSlideIn.setDuration(Math.abs(menuSize - offset) *500 / menuSize);
 		contentContainer.startAnimation(contentSlideIn);
 		
 		contentContainerLayoutParams = (FrameLayout.LayoutParams) contentContainer.getLayoutParams();
     	contentContainerLayoutParams.setMargins(0, 0, 0, 0);
     	contentContainer.setLayoutParams(contentContainerLayoutParams);
     	
     	menuShown = false;
 		
 		menuSlideOut.setAnimationListener(new AnimationListener() {
 			@Override
 			public void onAnimationStart(Animation animation) {
 				// not needed here
 			}
 			@Override
 			public void onAnimationRepeat(Animation animation) {
 				// not needed here	
 			}
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				// move menu out of visible scope
 				menuLayoutParams = (FrameLayout.LayoutParams) menu.getLayoutParams();
 				menuLayoutParams.setMargins(20000, 20000, 0, 0);
 				menu.setLayoutParams(menuLayoutParams);
 			}
 		});
 	}
 	public void fill() {
 		ListView list = (ListView) act.findViewById(R.id.menu_listview);
 		if (prefs.getBoolean(Functions.IS_LOGGED_IN, false)) {
 			if (prefs.getBoolean(Functions.RIGHTS_TEACHER, false)
 					|| prefs.getBoolean(Functions.RIGHTS_ADMIN, false))
 				items = new SlideMenuAdapter.MenuDesc[10];
 			else
 				items = new SlideMenuAdapter.MenuDesc[9];
 			for (int i = 0; i < items.length; i++) {
 				items[i] = new SlideMenuAdapter.MenuDesc();
 			}
 			//TimeTable
 			items[0].icon = R.drawable.ic_timetable;
 			items[0].label = "Stundenplan";
 			items[0].openFragment = TimeTableFragment.class;
 			items[0].containerActivity = MainActivity.class;
 			//VPlan
 			items[1].icon = R.drawable.ic_vplan_green;
 			items[1].label = "Vertretungsplan";
 			items[1].openFragment = VPlan.class;
 			items[1].containerActivity = MainActivity.class;
 			//Tasks
 			items[2].icon = R.drawable.ic_tasks;
 			items[2].label = "Aufgaben";
 			items[2].containerActivity = MainActivity.class;
 			items[2].openFragment = TasksOverView.class;
 			//Events
 			items[3].icon = R.drawable.ic_events;
 			items[3].label = "Termine";
 			items[3].openFragment = Events.class;
 			items[3].containerActivity = MainActivity.class;
 			//SMVBlog
 			items[4].icon = R.drawable.ic_smv;
 			items[4].label = "SMVBlog";
 			items[4].openFragment = SMVBlog.class;
 			items[4].containerActivity = MainActivity.class;
 			//Settings
 			items[5].icon = R.drawable.ic_settings;
 			items[5].label = "Einstellungen";
 			items[5].openActivity = (Functions.getSDK() >= 11) ? SettingsAdvanced.class
 					: Settings.class;
 			//Help
 			items[6].icon = R.drawable.ic_help;
 			items[6].label = "Hilfe";
 			items[6].openActivity = null;
 			items[6].openIntent = new Intent(act, HelpAbout.class);
 			items[6].openIntent.putExtra(Functions.HELPABOUT, Functions.help);
 			items[6].useSlideMenu = false;
 			//About
 			items[7].icon = R.drawable.ic_about;
 			items[7].label = "Über";
 			items[7].openActivity = null;
 			items[7].openIntent = new Intent(act, HelpAbout.class);
 			items[7].openIntent.putExtra(Functions.HELPABOUT, Functions.about);
 			items[7].useSlideMenu = false;
 			//News 4 Pupils
 			String news_pupils = prefs.getString(Functions.NEWS_PUPILS, "");
 			items[8].type = Functions.TYPE_INFO;
 			items[8].title = "Aktuell";
 			items[8].icon = R.drawable.ic_launcher;
 			items[8].label = news_pupils.substring(0,
 					((news_pupils.length() > 60) ? 60 : news_pupils.length()))
 					+ ((news_pupils.length() > 60) ? "..." : "");
 			items[8].openActivity = null;
 			items[8].openIntent = new Intent(act, InfoActivity.class);
 			items[8].openIntent.putExtra("type", "info");
 			items[8].openIntent.putExtra("info_type", "pupils");
 			items[8].useSlideMenu = false;
 			if (prefs.getBoolean(Functions.RIGHTS_TEACHER, false)
 					|| prefs.getBoolean(Functions.RIGHTS_ADMIN, false)) {
 				String news_teachers = prefs.getString(Functions.NEWS_TEACHERS,
 						"");
 				items[9].type = Functions.TYPE_INFO;
 				items[9].title = "Lehrerinfo";
 				items[9].icon = R.drawable.ic_launcher;
 				items[9].label = news_teachers.substring(0, ((news_teachers
 						.length() > 60) ? 60 : news_teachers.length()))
 						+ ((news_teachers.length() > 60) ? "..." : "");
 				items[9].openIntent = new Intent(act, InfoActivity.class);
 				items[9].openIntent.putExtra("type", "info");
 				items[9].openIntent.putExtra("info_type", "teachers");
 				items[9].useSlideMenu = false;
 			}
 		} else {
 
 			items = new SlideMenuAdapter.MenuDesc[5];
 			for (int i = 0; i < items.length; i++) {
 				items[i] = new SlideMenuAdapter.MenuDesc();
 			}
 			items[0].icon = R.drawable.ic_settings;
 			items[0].label = "Setup-Assistent";
 			items[0].openActivity = SetupAssistant.class;
 			items[1].icon = R.drawable.ic_events;
 			items[1].label = "Termine";
 			items[1].openFragment = Events.class;
 			items[1].containerActivity = MainActivity.class;
 			items[2].icon = R.drawable.ic_smv;
 			items[2].label = "SMVBlog";
 			items[2].openFragment = SMVBlog.class;
 			items[2].containerActivity = MainActivity.class;
 			items[3].icon = R.drawable.ic_help;
 			items[3].label = "Hilfe";
 			items[3].openActivity = null;
 			items[3].openIntent = new Intent(act, HelpAbout.class);
 			items[3].openIntent.putExtra(Functions.HELPABOUT, Functions.help);
 			items[3].useSlideMenu = false;
 			items[4].icon = R.drawable.ic_about;
 			items[4].label = "Über";
 			items[4].openActivity = null;
 			items[4].openIntent = new Intent(act, HelpAbout.class);
 			items[4].openIntent.putExtra(Functions.HELPABOUT, Functions.about);
 			items[4].useSlideMenu = false;
 		}
 		SlideMenuAdapter adap = new SlideMenuAdapter(act, items);
 		list.setAdapter(adap);
 	}
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		switch (event.getAction()) {
 		case MotionEvent.ACTION_DOWN:
 				break;
 		case MotionEvent.ACTION_MOVE:
 			previousX = lastX;
 			lastX = event.getX();
 			int positionDiff = Float.valueOf(motionStartX - lastX).intValue();
 			if (positionDiff < 0)
 				positionDiff = 0;
 			if (lastDiff == positionDiff || lastDiff - 1 == positionDiff)
 				break;
 			lastDiff = positionDiff;
 			if (lastDiff < maxDiff)
 				maxDiff = lastDiff;
 			contentContainer.scrollTo(-menuSize + positionDiff, 0);
 			menu.scrollTo(positionDiff / 2, 0);
 			break;
 		case MotionEvent.ACTION_UP:
 			contentContainer.scrollTo(0, 0);
 			menu.scrollTo(0, 0);
 			if (previousX < event.getX() && lastDiff > Functions.dpToPx(5, act)) {
 				show(true, lastDiff);
 			} else
 				hide(lastDiff);
 			break;
 		}
 		return true;
 	}
 }
