 package com.quanleimu.activity;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Debug;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.view.ViewGroup;
 import android.view.ViewParent;
 
 import android.widget.CheckBox;
 import android.widget.TextView;
 
 import com.mobclick.android.MobclickAgent;
 import com.quanleimu.util.LocationService;
 import com.quanleimu.util.ShortcutUtil;
 import com.quanleimu.view.BaseView;
 import com.quanleimu.view.BaseView.EBUTT_STYLE;
 import com.quanleimu.view.BaseView.ETAB_TYPE;
 import com.quanleimu.view.BaseView.TabDef;
 import com.quanleimu.view.BaseView.TitleDef;
 import com.quanleimu.view.PersonalCenterEntryView;
 import com.quanleimu.view.SetMainView;
 
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.Button;
 import android.widget.RelativeLayout;
 
 import com.quanleimu.view.CateMainView;
 import com.quanleimu.view.PersonalCenterView;
 import com.quanleimu.view.HomePageView;
 import com.quanleimu.view.PostGoodsView;
 public class QuanleimuMainActivity extends BaseActivity implements BaseView.ViewInfoListener{
 	private BaseView currentView;
 	private boolean needClearViewStack = false;
 	
 	public QuanleimuMainActivity(){
 		super();
 		
 		QuanleimuApplication.getApplication().setErrorHandler(this);
 	}
 	
 	public void onSwitchToTab(ETAB_TYPE tabType){
 		switch(tabType){
 		case ETAB_TYPE_MAINPAGE:
 			if(currentView.getTabDef().m_tabSelected == BaseView.ETAB_TYPE.ETAB_TYPE_MAINPAGE)break;
 			
 			needClearViewStack = true;
 			onNewView(new HomePageView(this, bundle));
 			
 			break;
 		case ETAB_TYPE_CATEGORY:				
 			if(currentView.getTabDef().m_tabSelected == BaseView.ETAB_TYPE.ETAB_TYPE_CATEGORY)break;
 			
 			needClearViewStack = true;
 			onNewView(new CateMainView(this));
 			
 			break;
 		case ETAB_TYPE_PUBLISH:
 			if(currentView.getTabDef().m_tabSelected == BaseView.ETAB_TYPE.ETAB_TYPE_PUBLISH)break;
 			
 			needClearViewStack = false;
 //			onNewView(new PostGoodsCateMainView(this, bundle));
 			onNewView(new PostGoodsView(this, bundle, ""));
 			
 			break;
 		case ETAB_TYPE_MINE:
 			if(currentView.getTabDef().m_tabSelected == BaseView.ETAB_TYPE.ETAB_TYPE_MINE)break;
 			
 			needClearViewStack = true;
 //			onNewView(new PersonalCenterView(this, bundle));
 			onNewView(new PersonalCenterEntryView(this, bundle));
 
 			break;
 		case ETAB_TYPE_SETTING:
 			if(currentView.getTabDef().m_tabSelected == BaseView.ETAB_TYPE.ETAB_TYPE_SETTING)break;
 			
 			needClearViewStack = true;
 			onNewView(new SetMainView(this));
 			
 			break;			
 		}
 
 	}
 	
 	@Override
 	public void onBack(int message, Object obj){
 		this.onBack();
 		if(currentView != null){
 			currentView.onPreviousViewBack(message, obj);
 		}
 	}
 
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if(currentView != null){
 			currentView.onActivityResult(requestCode, resultCode, data);
 		}
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	@Override
 	public void onSetResult(int requestCode, int resultCode, Bundle data){
 		if(currentView != null){
 			Intent intent = new Intent();
 			if(null != data)
 				intent.putExtras(data);
 			currentView.onActivityResult(requestCode, resultCode, intent);
 		}		
 	}
 	
 	protected boolean replaceDuplicate(ViewGroup viewGroup, View view)
     {
         boolean isDuplicate = false;
         //TODO Below comment codes need verify in android platform.. this codes is for RIM platform...
         ViewParent parent = view.getParent();
         if(parent instanceof ViewGroup)
         {
             ViewGroup groupParent = (ViewGroup)parent;
             groupParent.removeView(view);
         }
         int childCount = viewGroup.getChildCount();
         for(int next = 0; next < childCount; next++)
         {
             if(viewGroup.getChildAt(next).equals(view))
             {
                 isDuplicate = true;
                 break;
             }
         }
         
       
         
         if(isDuplicate)
         {
             viewGroup.removeView(view);
             viewGroup.addView(view);
         }
         else
         {
             viewGroup.addView(view);
         }
         
         return isDuplicate;
     }
 	
 	protected View getLeafFocusedView(View view)
     {
         View leafFocusedView = view;
         while(leafFocusedView instanceof ViewGroup)
         {
             View focusedChild = ((ViewGroup)leafFocusedView).getFocusedChild();
             if(focusedChild == null)
             {
                 break;
             }
             else
             {
                 leafFocusedView = focusedChild;
             }
             
         }
         return leafFocusedView;
     }
 	
 	@Override
 	public void onBack(){
         InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
         imm.hideSoftInputFromWindow(this.findViewById(R.id.contentLayout).getWindowToken(), 0); 
 
 		try{
 			if(!currentView.onBack()){
 		    	if(QuanleimuApplication.getApplication().getViewStack().size() > 0){
 		    		LinearLayout scroll = (LinearLayout)this.findViewById(R.id.contentLayout);
 		    		scroll.removeAllViews();
 		    		currentView.onDestroy();
 		    		currentView = QuanleimuApplication.getApplication().getViewStack().pop();
 		    		setBaseLayout(currentView);  
 		    		scroll.addView(currentView);
 		    		
 		    		
 		    	 	 final View leafFocusedView = currentView.hasFocus() ? getLeafFocusedView(currentView) : null;
 			    	 View rootView = currentView.getRootView();
 		         if (leafFocusedView == null)
 		         {
 		        	 	 if(!currentView.isInTouchMode())
 		             {
 		                 rootView.requestFocus(View.FOCUS_FORWARD);
 		             }
 		         }
 		         else
 		         {
 		             leafFocusedView.clearFocus();
 		             leafFocusedView.requestFocus();
 		         }
 		     	
 		    		
 		    	}else{
 		
 		            SharedPreferences settings = getSharedPreferences(SHARE_PREFS_NAME, 0);
 		            String hasShowShortcutMessage = settings.getString("hasShowShortcut", "no");
 		
 		            AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		
 		            LayoutInflater adbInflater = LayoutInflater.from(QuanleimuMainActivity.this);
 		            View shortcutLayout = adbInflater.inflate(R.layout.shortcutshow, null);
 		
 		            final CheckBox shortcutCheckBox = (CheckBox) shortcutLayout.findViewById(R.id.shortcut);
 		            final boolean needShowShortcut = "no".equals(hasShowShortcutMessage) && !ShortcutUtil.hasShortcut(this);
 		            if (needShowShortcut)
 		            {
 		                builder.setView(shortcutLayout);
 		            }
 		
 		            builder.setTitle("提示:").setMessage("是否退出?").setNegativeButton("否", null).setPositiveButton("是", new DialogInterface.OnClickListener()
 		            {
 		
 		                @Override
 		                public void onClick(DialogInterface dialog, int which)
 		                {
 		
 		                    if (needShowShortcut && shortcutCheckBox.isChecked())
 		                    {
 		                        ShortcutUtil.addShortcut(QuanleimuMainActivity.this);
 		                    }
 		
 		                    if (QuanleimuApplication.list != null && QuanleimuApplication.list.size() != 0)
 		                    {
 		                        for (String s : QuanleimuApplication.list)
 		                        {
 		                            deleteFile(s);
 		                        }
 		                    }
 		
 		                    SharedPreferences settings = getSharedPreferences(SHARE_PREFS_NAME, 0);
 		                    SharedPreferences.Editor editor = settings.edit();
 		                    editor.putString("hasShowShortcut", "yes");
 		                    // Commit the edits!
 		                    editor.commit();
 		            		Intent pushIntent = new Intent(QuanleimuMainActivity.this, com.quanleimu.broadcast.BXNotificationService.class);
 		            		QuanleimuMainActivity.this.startService(pushIntent);
 //		            		Debug.stopMethodTracing();
 		                    System.exit(0);
 		                }
 		            });
 		            builder.create().show();
 		    	}	
 			}
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public void onExit(BaseView view){
         InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
         imm.hideSoftInputFromWindow(this.findViewById(R.id.contentLayout).getWindowToken(), 0); 
 		
     	if(view == currentView ){
     		LinearLayout scroll = (LinearLayout)this.findViewById(R.id.contentLayout);
     		scroll.removeAllViews();    		
     		currentView.onDestroy();
     		
     		currentView = QuanleimuApplication.getApplication().getViewStack().pop();
     		if(null != currentView){
 	    		setBaseLayout(currentView);
 	    		scroll.addView(currentView);
 	    		
     		}/*else{
     			onNewView(new HomePageView(this));
     		}*/
     	}
 	}
 	
 	@Override
 	public void onNewView(BaseView newView){
         InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
         imm.hideSoftInputFromWindow(this.findViewById(R.id.contentLayout).getWindowToken(), 0); 
 		
 		LinearLayout scroll = (LinearLayout)this.findViewById(R.id.contentLayout);
 		
 //		long time_start =  System.currentTimeMillis();
 //		if(null != currentView){
 //			Log.d("page switching performance log", "from current:" + currentView.getClass().getName() + " at " + time_start + "ms" );
 //		}else{
 //			Log.d("page switching performance log", "from current:" + "N/A" + " at " + time_start + "ms" );
 //		}
 		
 		if(null != currentView){
 			currentView.onPause();
 			QuanleimuApplication.getApplication().getViewStack().push(currentView);
 			
 //			Animation hyperspaceJumpAnimation_vanishing = AnimationUtils.loadAnimation(this, R.anim.animation_vanishing);
 //			currentView.startAnimation(hyperspaceJumpAnimation_vanishing);
 		}
 		try{
 			scroll.removeAllViews();
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		currentView = newView;
 		newView.setInfoChangeListener(this);//NOTE: MUST be called before addView is called, coz addView will call View.onAttatchedToWindow which could then call methods that will use ViewInfoListener
 		setBaseLayout(newView);
 		
 		if(needClearViewStack){
 			QuanleimuApplication.getApplication().getViewStack().clear();
 			needClearViewStack = false;
 		}
 		
 //		Animation hyperspaceJumpAnimation_emerging = AnimationUtils.loadAnimation(this, R.anim.animation_emerging);
 //		currentView.startAnimation(hyperspaceJumpAnimation_emerging);		
 		scroll.addView(currentView);		
 
 		
 //		long time_end =  System.currentTimeMillis();
 //		Log.d("page switching performance log", "to current:" + currentView.getClass().getName() + " at " + time_end + "ms" );
 //		Log.d("page switching performance log", "cost is " + (time_end-time_start) + "ms");
 	}
 	
 //	@Override
 //	public void onRightBtnTextChanged(String newText){
 //		Button right = (Button)this.findViewById(R.id.btnRight);
 //		
 //		if(null != newText && newText.length() > 0){
 //			right.setText(newText);
 //			right.setVisibility(View.VISIBLE);
 //		}else{
 //			right.setVisibility(View.GONE);
 //		}
 //	}
 //	
 //	public void onLeftBtnTextChanged(String newText){
 //		Button left = (Button)this.findViewById(R.id.btnLeft);
 //		
 //		if(null != newText && newText.length() > 0){
 //			left.setText(newText);
 //			left.setVisibility(View.VISIBLE);
 //		}else{
 //			left.setVisibility(View.GONE);
 //		}		
 //	}
 
 	@Override
 	public void onTitleChanged(TitleDef title){
 
 		if(null == title) return;
 		RelativeLayout top = (RelativeLayout)findViewById(R.id.linearTop);
 		if(title.m_visible){
 			top.setVisibility(View.VISIBLE);
 			TextView tTitle = (TextView)findViewById(R.id.tvTitle);
 			tTitle.setText(title.m_title);
 			if(null != title.m_leftActionHint && !title.m_leftActionHint.equals("")){
 				Button left = (Button)findViewById(R.id.btnLeft);
 
 				if(title.m_leftActionStyle == EBUTT_STYLE.EBUTT_STYLE_BACK ){					
 					left.setBackgroundResource(R.drawable.btn_jj);
 				}
 				else //if(title.m_leftActionStyle == EBUTT_STYLE.EBUTT_STYLE_NORMAL )
 				{
 					left.setBackgroundResource(R.drawable.btn_editx);
 				}
 				
 				left.setText(title.m_leftActionHint);				
 				left.setVisibility(View.VISIBLE);				
 			}else{
 				Button left = (Button)findViewById(R.id.btnLeft);
 				left.setVisibility(View.GONE);
 			}
 			
 			if(null != title.m_rightActionHint && !title.m_rightActionHint.equals("")){
 				Button right = (Button)findViewById(R.id.btnRight);
 				right.setText(title.m_rightActionHint);
 				right.setVisibility(View.VISIBLE);
 				
 				if(title.m_rightActionStyle == EBUTT_STYLE.EBUTT_STYLE_BACK ){
 					right.setBackgroundResource( R.drawable.btn_jj);
 				}
 				else //if(title.m_rightActionStyle == EBUTT_STYLE.EBUTT_STYLE_NORMAL )
 				{
 					right.setBackgroundResource(R.drawable.btn_search);
 				}
 			}else{
 				Button right = (Button)findViewById(R.id.btnRight);
 				right.setVisibility(View.GONE);
 			}
 		}
 		else{
 			top.setVisibility(View.GONE);
 		}
 	}
 	
 	@Override
 	public void onTabChanged(TabDef tab){
 
 		if(null == tab) return;
 		LinearLayout bottom = (LinearLayout)findViewById(R.id.linearBottom);
 		if(tab.m_visible){
 			bottom.setVisibility(View.VISIBLE);
 			findViewById(R.id.ivBottomNull).setVisibility(View.VISIBLE);
 		}
 		else{
 			bottom.setVisibility(View.GONE);
 			findViewById(R.id.ivBottomNull).setVisibility(View.GONE);
 		}
 		
 		if(tab.m_tabSelected != BaseView.ETAB_TYPE.ETAB_TYPE_PREV){
 			ivHomePage.setBackgroundResource((tab.m_tabSelected == BaseView.ETAB_TYPE.ETAB_TYPE_MAINPAGE) ? R.drawable.tabbar_cate_selected : R.drawable.iv_homepage_xml);
 //			ivCateMain.setImageResource(tab.m_tabSelected == BaseView.ETAB_TYPE.ETAB_TYPE_CATEGORY ? R.drawable.iv_cate_press : R.drawable.iv_cate);
 			ivPostGoods.setBackgroundResource(tab.m_tabSelected == BaseView.ETAB_TYPE.ETAB_TYPE_PUBLISH ? R.drawable.tabbar_add_selected : R.drawable.iv_postgoods_xml);
 			ivMyCenter.setBackgroundResource(tab.m_tabSelected == BaseView.ETAB_TYPE.ETAB_TYPE_MINE ? R.drawable.tabbar_my_selected : R.drawable.iv_mycenter_xml);
 //			ivSetMain.setImageResource(tab.m_tabSelected == BaseView.ETAB_TYPE.ETAB_TYPE_SETTING ? R.drawable.iv_setmain_press : R.drawable.iv_setmain);
 		}
 	}
 	
 	public void onPopView(String viewClassName_){
 		String viewClassName = QuanleimuApplication.getApplication().getViewStack().peerClassName();
 		
 		if(null != viewClassName && null != viewClassName_ && viewClassName.equals(viewClassName_) ){
 			QuanleimuApplication.getApplication().getViewStack().pop();
 		}
 	}
 	
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		LocationService.getInstance().stop();
 
 		super.onPause();
 //		
 //		MobclickAgent.onPause(this);
 //		
 //		Log.d("Umeng SDK API call", "onPause() called from QuanleimuMainActivity:onPause()!!");
 	}
 	
 	@Override
 	protected void onResume() {
 		bundle.putString("backPageName", "");
 		super.onResume();
 //		MobclickAgent.onResume(this);
 //		
 //		Log.d("Umeng SDK API call", "onResume() called from QuanleimuMainActivity:onResume()!!");
 	} 
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 //		Debug.startMethodTracing();
 		super.onCreate(savedInstanceState);
 				
 		Intent pushIntent = new Intent(this, com.quanleimu.broadcast.BXNotificationService.class);
 		this.stopService(pushIntent);
 
 		setContentView(R.layout.main_activity);
 		LinearLayout scroll = (LinearLayout)this.findViewById(R.id.contentLayout);
 		
 		//ImageView vHomePage = (ImageView)findViewById(R.id.ivHomePage);
 		//vHomePage.setImageResource(R.drawable.iv_homepage_press);
 		
 		Button left = (Button)findViewById(R.id.btnLeft);
 		left.setOnClickListener(this);
 		Button right = (Button)findViewById(R.id.btnRight);
 		right.setOnClickListener(this);
 		
 		ivHomePage = (ImageView)findViewById(R.id.ivHomePage);
 		ivHomePage.setOnClickListener(this);
 //		ivCateMain = (ImageView)findViewById(R.id.ivCateMain);
 //		ivCateMain.setOnClickListener(this);
 		ivPostGoods = (ImageView)findViewById(R.id.ivPostGoods);
 		ivPostGoods.setOnClickListener(this);
 		ivMyCenter = (ImageView)findViewById(R.id.ivMyCenter);
 		ivMyCenter.setOnClickListener(this);
 //		ivSetMain = (ImageView)findViewById(R.id.ivSetMain);
 //		ivSetMain.setOnClickListener(this);
 		
 		if(!QuanleimuApplication.update){
 			QuanleimuApplication.update = true;
 			MobclickAgent.setUpdateOnlyWifi(false);
 			MobclickAgent.update(this);
 		}
 				
 		BaseView childView = new HomePageView(this, bundle);		
 		currentView = childView;
 		childView.setInfoChangeListener(this);		
 		setBaseLayout(childView);
 		scroll.addView(childView);		
 	}
 	
 	private void setBaseLayout(BaseView view){
 		
 		if(view == null) return;
 		
 		onTabChanged(view.getTabDef());		
 		
 		onTitleChanged(view.getTitleDef());	
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.btnRight:
 			currentView.onRightActionPressed();
 //			intent.setClass(HomePage.this, Search.class);
 //			bundle.putString("searchType", "homePage");
 //			intent.putExtras(bundle);
 //			startActivity(intent);
 			break;
 		case R.id.btnLeft:
 			if(!currentView.onLeftActionPressed()){
 				this.onBack();
 			}
 			break;
 		case R.id.ivHomePage:{
 			this.onSwitchToTab(BaseView.ETAB_TYPE.ETAB_TYPE_MAINPAGE);
 			break;
 		}
 //		case R.id.ivCateMain:				
 //			this.onSwitchToTab(BaseView.ETAB_TYPE.ETAB_TYPE_CATEGORY);
 //			break;
 		case R.id.ivPostGoods:
 			this.onSwitchToTab(BaseView.ETAB_TYPE.ETAB_TYPE_PUBLISH);			
 			break;
 		case R.id.ivMyCenter:
 			this.onSwitchToTab(BaseView.ETAB_TYPE.ETAB_TYPE_MINE);			
 			break;
 //		case R.id.ivSetMain:
 //			this.onSwitchToTab(BaseView.ETAB_TYPE.ETAB_TYPE_SETTING);	
 //			break;
 		}
 		super.onClick(v);
 	}
 
 	private final static String SHARE_PREFS_NAME = "baixing_shortcut_app";
 	
 	@Override
     public boolean onKeyDown(int keyCode, KeyEvent event)
     {
         if (keyCode == KeyEvent.KEYCODE_BACK)
         {
         	onBack();
         }
         
         else{
         	return super.onKeyDown(keyCode, event);
         }
         
         return true;
     }
 }
