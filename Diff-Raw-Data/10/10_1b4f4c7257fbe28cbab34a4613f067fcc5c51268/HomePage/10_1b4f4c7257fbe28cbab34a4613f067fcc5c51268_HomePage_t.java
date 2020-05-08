 package com.quanleimu.activity;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
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
 import com.quanleimu.view.SetMain;
 
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.Button;
 import android.widget.RelativeLayout;
 
 import com.quanleimu.view.PersonalCenterView;
 import com.quanleimu.view.PostGoodsCateMainView;
 
 import com.quanleimu.view.HomePageView;
 public class HomePage extends BaseActivity implements BaseView.ViewInfoListener{
 	private BaseView currentView;
 	
 	public void onSwitchToTab(ETAB_TYPE tabType){
 		switch(tabType){
 		case ETAB_TYPE_MAINPAGE:
 			if(currentView.getTabDef().m_tabSelected == BaseView.ETAB_TYPE.ETAB_TYPE_MAINPAGE)break;
 			onNewView(new HomePageView(this, bundle));
 			
 			MyApplication.getApplication().getViewStack().clear();
 			break;
 		case ETAB_TYPE_CATEGORY:				
 			if(currentView.getTabDef().m_tabSelected == BaseView.ETAB_TYPE.ETAB_TYPE_CATEGORY)break;
 			onNewView(new CateMain(this));
 			
 			MyApplication.getApplication().getViewStack().clear();
 			break;
 		case ETAB_TYPE_PUBLISH:
 			if(currentView.getTabDef().m_tabSelected == BaseView.ETAB_TYPE.ETAB_TYPE_PUBLISH)break;
 			onNewView(new PostGoodsCateMainView(this, bundle));
 			MyApplication.getApplication().getViewStack().clear();
 			break;
 		case ETAB_TYPE_MINE:
 			if(currentView.getTabDef().m_tabSelected == BaseView.ETAB_TYPE.ETAB_TYPE_MINE)break;
			onNewView(new PersonalCenterView(this, bundle));
 			MyApplication.getApplication().getViewStack().clear();
 			break;
 		case ETAB_TYPE_SETTING:
 			if(currentView.getTabDef().m_tabSelected == BaseView.ETAB_TYPE.ETAB_TYPE_SETTING)break;
 			onNewView(new SetMain(this));
 			
 			MyApplication.getApplication().getViewStack().clear();
 			
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
 	public void onBack(){
 		if(!currentView.onBack()){
 	    	if(MyApplication.getApplication().getViewStack().size() > 0){
 	    		LinearLayout scroll = (LinearLayout)this.findViewById(R.id.contentLayout);
 	    		scroll.removeAllViews();
 	    		
 	    		currentView.onDestroy();
 	    		currentView = MyApplication.getApplication().getViewStack().pop();
 	    		setBaseLayout(currentView);            		
 	
 	    		scroll.addView(currentView);
 	    		
 	    	}else{
 	
 	            SharedPreferences settings = getSharedPreferences(SHARE_PREFS_NAME, 0);
 	            String hasShowShortcutMessage = settings.getString("hasShowShortcut", "no");
 	
 	            AlertDialog.Builder builder = new AlertDialog.Builder(this);
 	
 	            LayoutInflater adbInflater = LayoutInflater.from(HomePage.this);
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
 	                        ShortcutUtil.addShortcut(HomePage.this);
 	                    }
 	
 	                    if (MyApplication.list != null && MyApplication.list.size() != 0)
 	                    {
 	                        for (String s : MyApplication.list)
 	                        {
 	                            deleteFile(s);
 	                        }
 	                        for (int i = 0; i < fileList().length; i++)
 	                        {
 	                            System.out.println("fileList()[i]----------->" + fileList()[i]);
 	                        }
 	                    }
 	
 	                    SharedPreferences settings = getSharedPreferences(SHARE_PREFS_NAME, 0);
 	                    SharedPreferences.Editor editor = settings.edit();
 	                    editor.putString("hasShowShortcut", "yes");
 	                    // Commit the edits!
 	                    editor.commit();
 	
 	                    System.exit(0);
 	                }
 	            });
 	            builder.create().show();
 	    	}	
 		}
 	}
 	
 	@Override
 	public void onExit(BaseView view){
     	if(view == currentView && MyApplication.getApplication().getViewStack().size() > 0){
     		LinearLayout scroll = (LinearLayout)this.findViewById(R.id.contentLayout);
     		scroll.removeAllViews();
     		
     		currentView.onDestroy();
     		currentView = MyApplication.getApplication().getViewStack().pop();
     		setBaseLayout(currentView);            		
 
     		scroll.addView(currentView);
     	}
 	}
 	
 	@Override
 	public void onNewView(BaseView newView){
 		long time_start =  System.currentTimeMillis();
 		Log.d("page switching performance log", "from current:" + currentView.getClass().getName() + " at " + time_start + "ms" );
 		
 		currentView.onPause();
 		MyApplication.getApplication().getViewStack().push(currentView);
		
 		currentView = newView;
		newView.setInfoChangeListener(this);//NOTE: MUST be called before addView is called, coz addView will call View.onAttatchedToWindow which could then call methods that will use ViewInfoListener
 		LinearLayout scroll = (LinearLayout)this.findViewById(R.id.contentLayout);
 		scroll.removeAllViews();
 		scroll.addView(currentView);
 		
 		setBaseLayout(newView);
 		
 		long time_end =  System.currentTimeMillis();
 		Log.d("page switching performance log", "to current:" + currentView.getClass().getName() + " at " + time_end + "ms" );
 		Log.d("page switching performance log", "cost is " + (time_end-time_start) + "ms");
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
 		LinearLayout top = (LinearLayout)findViewById(R.id.linearTop);
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
 					left.setBackgroundResource(R.drawable.btn_back_xml);
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
 		}
 		else{
 			bottom.setVisibility(View.GONE);
 		}
 		
 		ivHomePage.setImageResource((tab.m_tabSelected == BaseView.ETAB_TYPE.ETAB_TYPE_MAINPAGE) ? R.drawable.iv_homepage_press : R.drawable.iv_homepage);
 		ivCateMain.setImageResource(tab.m_tabSelected == BaseView.ETAB_TYPE.ETAB_TYPE_CATEGORY ? R.drawable.iv_cate_press : R.drawable.iv_cate);
 		ivPostGoods.setImageResource(tab.m_tabSelected == BaseView.ETAB_TYPE.ETAB_TYPE_PUBLISH ? R.drawable.iv_postgoods_press : R.drawable.iv_postgoods);
 		ivMyCenter.setImageResource(tab.m_tabSelected == BaseView.ETAB_TYPE.ETAB_TYPE_MINE ? R.drawable.iv_mycenter_press : R.drawable.iv_mycenter);
 		ivSetMain.setImageResource(tab.m_tabSelected == BaseView.ETAB_TYPE.ETAB_TYPE_SETTING ? R.drawable.iv_setmain_press : R.drawable.iv_setmain);
 	}
 	
 	public void onPopView(String viewClassName_){
 		String viewClassName = MyApplication.getApplication().getViewStack().peerClassName();
 		
 		if(null != viewClassName && null != viewClassName_ && viewClassName.equals(viewClassName_) ){
 			MyApplication.getApplication().getViewStack().pop();
 		}
 	}
 	
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		LocationService.getInstance().stop();
 		super.onPause();
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	protected void onResume() {
 		bundle.putString("backPageName", "");
 		super.onResume();
 	} 
 
 	private void changeTabView(BaseView view){
 		view.setInfoChangeListener(this);
 		if(currentView instanceof HomePageView){
 			ivHomePage.setImageResource(R.drawable.iv_homepage);
 		}
 		else if(currentView instanceof PersonalCenterView){
 			ivMyCenter.setImageResource(R.drawable.iv_mycenter);
 		}
 		
 		
 		if(view instanceof HomePageView){
 			ivHomePage.setImageResource(R.drawable.iv_homepage_press);
 		}
 		else if(view instanceof PersonalCenterView){
 			ivMyCenter.setImageResource(R.drawable.iv_mycenter_press);
 		}
 		
 		LinearLayout scroll = (LinearLayout)this.findViewById(R.id.contentLayout);
 		scroll.removeAllViews();
 		scroll.addView(view);
 		currentView = view;
 		setBaseLayout(view);
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) { 
 		setContentView(R.layout.homepage);
 		LinearLayout scroll = (LinearLayout)this.findViewById(R.id.contentLayout);
 		
 		ImageView vHomePage = (ImageView)findViewById(R.id.ivHomePage);
 		vHomePage.setImageResource(R.drawable.iv_homepage_press);
 		
 		Button left = (Button)findViewById(R.id.btnLeft);
 		left.setOnClickListener(this);
 		Button right = (Button)findViewById(R.id.btnRight);
 		right.setOnClickListener(this);
 		
 		ivHomePage = (ImageView)findViewById(R.id.ivHomePage);
 		ivHomePage.setOnClickListener(this);
 		ivCateMain = (ImageView)findViewById(R.id.ivCateMain);
 		ivCateMain.setOnClickListener(this);
 		ivPostGoods = (ImageView)findViewById(R.id.ivPostGoods);
 		ivPostGoods.setOnClickListener(this);
 		ivMyCenter = (ImageView)findViewById(R.id.ivMyCenter);
 		ivMyCenter.setOnClickListener(this);
 		ivSetMain = (ImageView)findViewById(R.id.ivSetMain);
 		ivSetMain.setOnClickListener(this);
 		
 		if(!MyApplication.update){
 			MyApplication.update = true;
 			MobclickAgent.setUpdateOnlyWifi(false);
 			MobclickAgent.update(this);
 		}
 				
 		BaseView childView = new HomePageView(this, bundle);		
 		currentView = childView;
 		childView.setInfoChangeListener(this);		
 		setBaseLayout(childView);
 		scroll.addView(childView);
 		
 		super.onCreate(savedInstanceState);
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
 		case R.id.ivCateMain:				
 			this.onSwitchToTab(BaseView.ETAB_TYPE.ETAB_TYPE_CATEGORY);
 			break;
 		case R.id.ivPostGoods:
 			this.onSwitchToTab(BaseView.ETAB_TYPE.ETAB_TYPE_PUBLISH);			
 			break;
 		case R.id.ivMyCenter:
 			this.onSwitchToTab(BaseView.ETAB_TYPE.ETAB_TYPE_MINE);			
 			break;
 		case R.id.ivSetMain:
 			this.onSwitchToTab(BaseView.ETAB_TYPE.ETAB_TYPE_SETTING);	
 			break;
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
