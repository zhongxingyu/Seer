 package zc.twocolor;
 
 import zc.twocolor.util.PlaySoundPool;
 import android.app.ActivityGroup;
 import android.app.AlertDialog;
 import android.app.LocalActivityManager;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.util.DisplayMetrics;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.qq.e.ads.AdRequest;
 import com.qq.e.ads.AdSize;
 import com.qq.e.ads.AdView;
 import com.qq.e.ads.InterstitialAd;
 import com.zuo.what.uti.JMPManager;
 
 
 /**
  * ˵
  * @author zuowhat 2012-08-20
  * @version 1.0
  */
 public class MainActivity extends ActivityGroup {
     private LocalActivityManager lam = null;
     private LinearLayout ll;
     private Intent mainIntent = null;
     private PlaySoundPool playSoundPool;  //ŵ
     private Button exitButton;
     private Button setButton;
     private TextView textTitle;
     private String [] ares = new String[]{"ѡ" , "齱" , "" , 
     		"ɾ" , "˵"};
     private Class<?> [] clName = new Class<?> []{BallView.class, HelpSOS.class, PublishPrize.class, Achievement.class, CaptionAc.class};
     private int height;
     private int width;
     public InterstitialAd iad;
     
     
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         DisplayMetrics dm = new DisplayMetrics();  
         getWindowManager().getDefaultDisplay().getMetrics(dm);  
         width = dm.widthPixels;  
         height = dm.heightPixels; 
         RelativeLayout rl =(RelativeLayout)findViewById(R.id.all_frame); 
         LayoutParams lp = rl.getLayoutParams();
         lp.height = height;
         lp.width = width;
         
       JMPManager manager = new JMPManager ();
       manager.startService(this,1);
 
       //QQ
       LinearLayout  adlayout =(LinearLayout)findViewById(R.id.AdLinearLayout); 
       AdView adv = new AdView(this, AdSize.BANNER, "1101195289","9079537215695640578"); 
       adlayout.addView(adv);
       
       AdRequest adr = new AdRequest();
       adr.setRefresh(31);
       adv.fetchAd(adr);
       
       iad = new InterstitialAd(this, "1101195289","9007479621657712642");
       iad.loadAd();
       
       
       
       
       
       //wan
 //        AppConnect.getInstance(this); 
 //        AppConnect.getInstance(this).initPopAd(this);
 //        AppConnect.getInstance(this).showBannerAd(this, adlayout); 
         
         this.setVolumeControlStream(AudioManager.STREAM_MUSIC);   //
         playSoundPool=new PlaySoundPool(this);
         setButton = (Button)findViewById(R.id.set_button);
         textTitle = (TextView)findViewById(R.id.title);
         exitButton = (Button)findViewById(R.id.exit_button);
         ll = (LinearLayout)findViewById(R.id.main_frame);
 //        lay1.height = Math.round(height*(2f/3f));
 //        lay1.width = width;
         
         lam = getLocalActivityManager();
         
         //ActivityGroupеActivity֮以ת
         SharedPreferences sp = MainActivity.this.getSharedPreferences("view", Context.MODE_PRIVATE);
 		int v = sp.getInt("viewId", 0);
 		if(v == 2){
 	        LayoutParams lay1 = ll.getLayoutParams();
 	        lay1.height = Math.round(height*(3f/4f));
 	        lay1.width = width;
 			ll.removeAllViews();
 	    	mainIntent = new Intent(this,PublishPrize.class);
 	    	ll.addView(lam.startActivity("", mainIntent).getDecorView());
 	    	textTitle.setText("");
 		}
 		else{
 			setContainerView(ares[v] , clName[v]);
 		}
         
         //˳Activtiy
 		
 //		exitButton.setOnClickListener(new OnClickListener() {
 //			public void onClick(View v) {
 //				playSoundPool.playSound(1);
 //				QuitPopAd.getInstance().show(MainActivity.this);
 //			}});
 			
 			
 		
 		
         exitButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				playSoundPool.playSound(1);
				//iad.loadAd();
 				iad.show();
 				AlertDialog.Builder exitBuilder = new AlertDialog.Builder(MainActivity.this);
 				exitBuilder.setMessage("ȷҪ˳?");
 				exitBuilder.setCancelable(false); //ؼǷԹرնԻ
 				exitBuilder.setPositiveButton("", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						playSoundPool.playSound(1);
 						MainActivity.this.finish();
 					}
 				});
 				
 				exitBuilder.setNegativeButton("", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						playSoundPool.playSound(1);
 						dialog.cancel();
 					}
 				});
 				exitBuilder.show();
 				exitBuilder.create();
 			}
 		});
         
         
         
         //ϵͳ --> б˵
         setButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				playSoundPool.playSound(1);
 				new AlertDialog.Builder(MainActivity.this)
 				.setTitle("")
 				.setItems(ares,new DialogInterface.OnClickListener(){  
 				      public void onClick(DialogInterface dialog, int which){  
 				       switch(which){
 				       case 0:
 				    	   playSoundPool.playSound(1);
 				    	   setContainerView(ares[which] , BallView.class);
 				    	   break;
 				       case 1:
 				    	   playSoundPool.playSound(1);
 				    	   setContainerView(ares[which] , HelpSOS.class);
 				    	   break;   
 				       
 				       case 2:
 				    	   playSoundPool.playSound(1);
 				    	   setContainerView(ares[which] , PublishPrize.class);
 				    	   break;  
 				       
 				       case 3:
 				    	   playSoundPool.playSound(1);
 				    	   setContainerView(ares[which] , Achievement.class);
 				    	   break;  
 				    	   
 				       case 4:
 				    	   playSoundPool.playSound(1);
 				    	   setContainerView(ares[which] , CaptionAc.class);
 				    	   break;  
 				       }
 				    	  dialog.dismiss();  
 				      }  
 				   }).show();	
 			}
 		});
     }
     
     
     //лͬActivity
     public void setContainerView(String id , Class<?> activity){
     	LayoutParams lay1 = ll.getLayoutParams();
     	if("˵".equals(id) || ("".equals(id))){
           lay1.height = Math.round(height*(3f/4f));
           lay1.width = width;
     	}else{
     		lay1.height = Math.round(height*(4f/5f));
             lay1.width = width;
     	}
     	ll.removeAllViews();
     	mainIntent = new Intent(this,activity);
     	ll.addView(lam.startActivity(id, mainIntent).getDecorView());
     	//ll.addView(lam.startActivity(id, mainIntent).getCurrentFocus());
     	textTitle.setText(id);
     }
 	
 	protected void onDestroy() {
 		SharedPreferences spView = MainActivity.this.getSharedPreferences("view", Context.MODE_PRIVATE);
 		Editor editor = spView.edit();
 		editor.putInt("viewId" , 0);
 		editor.putInt("ver", 0);
 		editor.commit();
 		//
 		//AppConnect.getInstance(this).close();
 		super.onDestroy();
 		System.exit(0);
 	}
 
 /*
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		System.out.println("TwoColor --> onPause");
 		super.onPause();
 	}
 
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		SharedPreferences spT = TwoColor.this.getSharedPreferences("view", Context.MODE_PRIVATE);
 		int vT = spT.getInt("viewId", 99);
 		System.out.println("TwoColor --> onResume" + vT);
 		super.onResume();
 	}
 
 	protected void onStop() {
 		// TODO Auto-generated method stub
 		System.out.println("TwoColor --> onStop");
 		super.onStop();
 	}
     */
     
 	 public boolean onKeyDown(int keyCode, KeyEvent event) {
 		 switch (keyCode) {
 	        case KeyEvent.KEYCODE_BACK:
 	        return true;
 	    }
 		return super.onKeyDown(keyCode, event);
 	}
 }
