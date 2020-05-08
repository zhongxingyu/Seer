 package com.android.qiushi;
 
 
 import java.util.ArrayList;
 
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.app.ProgressDialog;
 import android.app.ActionBar.Tab;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.support.v13.app.FragmentPagerAdapter;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.ActionMode;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.SubMenu;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.android.qiushi.Control.*;
 
 
 
 public class MainActivity extends Activity {
     /** Called when the activity is first created. */
 	private static final int MENU_SCENE_ID = 1;
 	private static final int MENU_ELECTRICAL_ID=2;
 	ActionMode mActionMode;
 	private ViewPager mPager;
 	TabAdapter mTabAdapter;
 	View LoginView;
 	AlertDialog dlg;
 	MenuItem menuCurrent;
 	
 	ActionBar bar;
 	private ProgressDialog mProDialog;
 	public static SharedPreferences.Editor mEditor=null;
 	public static SharedPreferences mSharedPreferences=null;
 	
 	Handler mHandler = new Handler(){
 		@Override
 		public void handleMessage(Message msg) {
 			int i;
 			switch(msg.what)
 			{
 			case ControlThread.CMD_HTTP_LOGIN:
 			case ControlThread.CMD_HTTP_GETSCENEEA:
 				if(msg.arg1 == 0)
 				{
 					menuCurrent.setTitle("ǰ䣺"+Global.room.roomNm);
 					bar.removeAllTabs();
 					mTabAdapter.mTabs.clear();
 					mTabAdapter.addTab(bar.newTab().setText(""),
 			                SceneControl.class, null);
 					mTabAdapter.addTab(bar.newTab().setText("ƹ"),
 			                LightControl.class, null);
 					mTabAdapter.addTab(bar.newTab().setText(""),
 			                DianShiControl.class, null);
 			        mTabAdapter.addTab(bar.newTab().setText("յ"),
 			                KongTiaoControl.class, null);
 			        mTabAdapter.addTab(bar.newTab().setText(""),
 			                YinXiangControl.class, null);
 			        mTabAdapter.addTab(bar.newTab().setText(""),
 			                FengShanControl.class, null);
 
 			        
 			        ///for(i=0; i< Global.room.eaList.length; i++)
 //					{
 //						Ea ea = Global.room.eaList[i];
 //						
 //						addUITab(ea.eaNm, ea.tpId, ea.idKey);
 //					}
 			        
 					bar.setSelectedNavigationItem(0);
 					ControlThread.getRooms();
 			        mProDialog.hide();
 				}
 				break;
 			case ControlThread.CMD_HTTP_GETROOMS:
 				
 				break;
 			default:
 				break;
 			}
 		}
 	};
 	
 	public void addUITab(String eanm, int tpid, int key)
 	{
 		Bundle arg = new Bundle();
 		
 		arg.putInt("key", key);
 		
 		switch(tpid)
 		{
 		case ControlThread.TPID_LIGHT:
 	        mTabAdapter.addTab(bar.newTab().setText(eanm),
 	                LightControl.class, arg);			
 			break;
 		case ControlThread.TPID_TV:
 	        mTabAdapter.addTab(bar.newTab().setText(eanm),
 	                DianShiControl.class, arg);
 	        break;
 		case ControlThread.TPID_AIR:
 	        mTabAdapter.addTab(bar.newTab().setText(eanm),
 	                KongTiaoControl.class, arg);
         	break;
 		case ControlThread.TPID_AV:
 	        mTabAdapter.addTab(bar.newTab().setText(eanm),
 	                YinXiangControl.class, arg);
         break;
 		case ControlThread.TPID_FAN:
 	        mTabAdapter.addTab(bar.newTab().setText(eanm),
 	                FengShanControl.class, arg);
 		}
 	}
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Global.getInstance();
         ControlThread.setUIHandler(mHandler);
         setContentView(R.layout.main);
         
         //ActionBar actionBar = getActionBar();
        // actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
         //actionBar.setDisplayOptions(1, ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME);
         //actionBar.setHomeButtonEnabled(true);
         //actionBar.setLogo(R.drawable.icon_0);
         mPager = (ViewPager) findViewById(R.id.pager);
 		mPager.setOffscreenPageLimit(0);
 
         bar = getActionBar();
         
         bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
         bar.setLogo(R.drawable.icon);
         //bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME);
         mTabAdapter = new TabAdapter(this, mPager);
 //        mTabAdapter.addTab(bar.newTab().setText(""),
 //                SceneControl.class, null);
         ///mTabAdapter.addTab(bar.newTab().setText("յ"),
         ///        KongTiaoControl.class, null);
 	
         ///mTabAdapter.addTab(bar.newTab().setText(""),
         ///        YinXiangControl.class, null);
         ///mTabAdapter.addTab(bar.newTab().setText(""),
         ///        FengShanControl.class, null);
 	    
         mSharedPreferences=getSharedPreferences("octohome", 0);
         mEditor=mSharedPreferences.edit();
         
         if(Global.familyId == -1)
         {
         	if(mSharedPreferences.getString("username", "").equals("") 
         			|| mSharedPreferences.getString("userpassword", "").equals("") 
         			|| mSharedPreferences.getString("gw", "").equals(""))
         	{
         		ControlThread.target = mSharedPreferences.getString("gw", "");
         		Global.userId = mSharedPreferences.getString("username", "");
         		Global.userPwd = mSharedPreferences.getString("userpassword", "");
  
         		login("½");
         	}else{
         		ControlThread.target = mSharedPreferences.getString("gw", "");
         		Global.userId = mSharedPreferences.getString("username", "");
         		Global.userPwd = mSharedPreferences.getString("userpassword", "");
         		
 				ControlThread.login(Global.userId, Global.userPwd);
 				
 				mProDialog=new ProgressDialog(MainActivity.this);
 				mProDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 			    mProDialog.setTitle("");
 			    mProDialog.setMessage("ӣԵ...");
 				mProDialog.show();
         	}
         }else{
 			mProDialog=new ProgressDialog(MainActivity.this);
 			mProDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		    mProDialog.setTitle("");
 		    mProDialog.setMessage("ӣԵ...");
 			mProDialog.show();
         	ControlThread.getSceneEa(Global.room.roomId);
         }
         
     }
     
     
     
     public void login(String reason)
     {
    		LayoutInflater inflater = LayoutInflater.from(this); 
 		LoginView = inflater.inflate(R.layout.login_layout, null); 
 		AlertDialog.Builder ad =new AlertDialog.Builder(this);
 		
 		ad.setView(LoginView);
 		dlg= ad.create();
 		dlg.setTitle(reason);
 		
 		EditText eUsername = (EditText)LoginView.findViewById(R.id.editUsername);
 		EditText ePassword = (EditText)LoginView.findViewById(R.id.editPassword);
 		EditText eGW = (EditText)LoginView.findViewById(R.id.editGW);
 		
		eUsername.setText(Global.userId);
		ePassword.setText(Global.userPwd);
		eGW.setText(ControlThread.target);
 		
 		dlg.setButton("ȷ", new DialogInterface.OnClickListener(){
 			public void onClick(DialogInterface arg0, int arg1) {
 				EditText username= (EditText)LoginView.findViewById(R.id.editUsername);
 				EditText password =(EditText)LoginView.findViewById(R.id.editPassword);
 				EditText editGW = (EditText)LoginView.findViewById(R.id.editGW);
 				Global.userPwd = password.getText().toString();
 				Global.userId = username.getText().toString();
 				ControlThread.target = editGW.getText().toString();
 				ControlThread.login(Global.userId, Global.userPwd);
 				
 				mEditor.putString("username", Global.userId);
 				mEditor.putString("userpassword", Global.userPwd);
 				mEditor.putString("gw", ControlThread.target);
 				
 				mProDialog=new ProgressDialog(MainActivity.this);
 				mProDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 			    mProDialog.setTitle("");
 			    mProDialog.setMessage("ӣԵ...");
 				mProDialog.show();
 				
 				dlg.dismiss();
 
 			}
 		});
 	
 		dlg.show();
     }
     
     
     
     /* (non-Javadoc)
 	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 	
 		if(requestCode==0 && resultCode==1){
 			
 			///System.out.println("id=="+data.getStringExtra("roomid"));
 			
 			Global.room.roomId = Integer.valueOf(data.getStringExtra("roomid"));
 			ControlThread.getSceneEa(Global.room.roomId);
 			for(int i=0;i<Global.rooms.length;i++){
 				if(Global.room.roomId==Global.rooms[i].roomId){
 					Global.room.roomNm = Global.rooms[i].roomNm;
 					Global.room.roomImg = Global.rooms[i].roomImg;
 					break;
 				}
 			}
 			this.mProDialog.show();
 		}
 		
 		
 		//super.onActivityResult(requestCode, resultCode, data);
 	}
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
 	 */
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		// TODO Auto-generated method stub
 		//super.onSaveInstanceState(outState);
 	}
 
 
 
 	public static class TabAdapter extends FragmentPagerAdapter 
     	implements ActionBar.TabListener,ViewPager.OnPageChangeListener{
 
     	private final Activity mContext;
         private final ActionBar mActionBar;
         private final ViewPager mViewPager;
         private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();
 
         static final class TabInfo {
             private final Class<?> clss;
             private final Bundle args;
             private Fragment fragment;
 
             TabInfo(Class<?> _class, Bundle _args) {
                 clss = _class;
                 args = _args;
             }
         }
 
         public TabAdapter(Activity activity, ViewPager pager) {
             super(activity.getFragmentManager());
             mContext = activity;
             mActionBar = activity.getActionBar();
             mViewPager = pager;
             mViewPager.setAdapter(this);
             mViewPager.setOnPageChangeListener(this);
         }
 
         public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
             TabInfo info = new TabInfo(clss, args);
             tab.setTag(info);
             tab.setTabListener(this);
             mTabs.add(info);
             mActionBar.addTab(tab);
             notifyDataSetChanged();
         }
 
 		@Override
 		public Fragment getItem(int position) {
 			// TODO Auto-generated method stub
 			TabInfo info = mTabs.get(position);
             if (info.fragment == null) {
                 info.fragment = Fragment.instantiate(mContext, info.clss.getName(), info.args);
             }
             return info.fragment;
 		}
 		
 		@Override
 		public int getCount() {
 			// TODO Auto-generated method stub
 			return mTabs.size();
 		}
 
 		
 		public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		
 		public void onTabSelected(Tab tab, FragmentTransaction arg1) {
 			// TODO Auto-generated method stub
 			Object tag = tab.getTag();
             for (int i=0; i<mTabs.size(); i++) {
                 if (mTabs.get(i) == tag) {
 					int id = -1;
 					//Intent intent = mContext.getIntent();
 					switch(i) {
 			            case 0:
 //							id = R.id.artisttab;
 //							intent.setDataAndType(Uri.EMPTY, "vnd.android.cursor.dir/artistalbum");
 			                break;
 			            case 1:
 //							id = R.id.albumtab;
 //							intent.setDataAndType(Uri.EMPTY, "vnd.android.cursor.dir/album");
 			                break;
 			            case 2:
 //							id = R.id.songtab;
 //							intent.setDataAndType(Uri.EMPTY, "vnd.android.cursor.dir/track");
 			                break;
 			            case 3:
 //							id = R.id.playlisttab;
 //							intent.setDataAndType(Uri.EMPTY, MediaStore.Audio.Playlists.CONTENT_TYPE);
 			                break;
 			            default:
 			                break;
 			        }
 //			        if (id != R.id.nowplayingtab) {
 //			            MusicUtils.setIntPref(mContext, "activetab", id);
 //			        }
 				
 					//mContext.setIntent(intent);
                     mViewPager.setCurrentItem(i);
                 }
             }
 //            if(!tab.getText().equals(mContext.getString(R.string.app_name))) {
 //                ActionMode actionMode = ((MainActivity) mContext).getActionMode();
 //                if (actionMode != null) {
 //                    actionMode.finish();
 //                }
 //            }
 		}
 
 		
 		
 		
 		public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		
 		public void onPageScrollStateChanged(int arg0) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		
 		public void onPageScrolled(int arg0, float arg1, int arg2) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		
 		public void onPageSelected(int position) {
 			// TODO Auto-generated method stub
 			mActionBar.setSelectedNavigationItem(position);
 		}
     }
     public ActionMode getActionMode() {
         return mActionMode;
     }
     public void setActionMode(ActionMode actionMode) {
         mActionMode = actionMode;
     }
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
 	 */
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 		
 		
 		return super.onContextItemSelected(item);
 	}
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
 	 */
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		// TODO Auto-generated method stub
 		super.onCreateContextMenu(menu, v, menuInfo);
 	}
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onCreatePanelMenu(int, android.view.Menu)
 	 */
 	@Override
 	public boolean onCreatePanelMenu(int featureId, Menu menu) {
 		// TODO Auto-generated method stub
 		menuCurrent = menu.add(1, 1, 0, "ǰ:");
 		menuCurrent.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
 		menuCurrent.setIcon(R.drawable.imagejpg);
 		menu.add(0, 2, 0, "Խ").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
 		menu.add(0, 3, 0, "").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
 		menu.add(0, 4, 0, "").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
 		return true;
 	}
     
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 		switch(item.getItemId()){
 		case 1:
 			//startActivity(new Intent(this,ShowRoom.class));
 			if(Global.rooms!=null){
 				startActivityForResult(new Intent(this,ShowRoom.class), 0);
 			}else{
 				Toast.makeText(this, "ڳʼԺ...", Toast.LENGTH_SHORT).show();
 			}
 			break;
 		case 4:
 			login("");
 		}
 		
 		return super.onOptionsItemSelected(item);
 	}
     
     
     
 }
