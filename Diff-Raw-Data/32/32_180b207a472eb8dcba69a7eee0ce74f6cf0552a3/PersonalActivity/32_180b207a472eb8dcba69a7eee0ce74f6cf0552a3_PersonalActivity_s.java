 package com.quanleimu.activity;
 
 import java.net.URLEncoder;
 import java.util.ArrayList;
 
 import android.content.Intent;
 import android.os.Bundle;
 
 import com.baixing.broadcast.CommonIntentAction;
 import com.baixing.entity.GoodsDetail;
 import com.baixing.entity.GoodsList;
 import com.baixing.jsonutil.JsonUtil;
 import com.baixing.util.Communication;
 import com.baixing.util.GoodsListLoader;
 import com.baixing.view.fragment.GoodDetailFragment;
 import com.baixing.view.fragment.PersonalInfoFragment;
 import com.baixing.view.fragment.PersonalPostFragment;
 import com.baixing.view.fragment.PostGoodsFragment;
 
 public class PersonalActivity extends BaseTabActivity {
 
 	@Override
 	protected int getTabIndex() {
 		return TAB_INDEX_PERSONAL;
 	}
 	
 	private void jumpToPersonalPost(Intent intent){
 		String action = intent.getAction();
 		if (action != null && action.equals(CommonIntentAction.ACTION_BROADCAST_POST_FINISH)) {
 			Bundle extras = intent.getExtras();
 			if(extras != null){
 				if(this.getSupportFragmentManager().getBackStackEntryCount() > 1 
 						&& !(getCurrentFragment() instanceof PersonalPostFragment)){
 					pushFragment(new PersonalInfoFragment(), bundle, true);
 				}
 				pushFragment(new PersonalPostFragment(), extras, false);
 			}		
 		}
 	}
 	
 	@Override
 	public void onCreate(Bundle savedBundle){
 		super.onCreate(savedBundle);
 		this.setContentView(R.layout.main_post);
 		
 		if (this.getSupportFragmentManager().getBackStackEntryCount() == 0){
 			pushFragment(new PersonalInfoFragment(), bundle, true);
 		}
		Intent intent = getIntent();
		
		jumpToPersonalPost(intent);
 		
 		globalTabCtrl.attachView(findViewById(R.id.common_tab_layout), 	this);
 		initTitleAction();
 	}
 	
 	@Override
	protected void onNewIntent(final Intent intent) {		
 		super.onNewIntent(intent);
		Thread t = new Thread(new Runnable(){
			@Override
			public void run(){
				jumpToPersonalPost(intent);
			}
		});
		t.start();
 	}
 	
 
 }
