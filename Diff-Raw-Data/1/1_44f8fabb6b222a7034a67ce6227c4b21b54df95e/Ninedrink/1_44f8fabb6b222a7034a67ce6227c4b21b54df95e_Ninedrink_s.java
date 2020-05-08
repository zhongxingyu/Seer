 package com.wazzup.ninedrink;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Service;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.Button;
 import android.widget.ImageView;
 
 import com.google.ads.AdRequest;
 import com.google.ads.AdView;
 
 public class Ninedrink extends Activity implements SensorEventListener {
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		mOpenHelper = new NDDBOpenHelper(this);
 		setPokers();
 		set_number = getRandom();
 		findView();
 		otherObject();
 		setLisenter();
 		shake();
 	}
 
 	@Override
 	public void onPause(){
 		
 		super.onPause();
 		pokerList.clear();
 		if (mSensorManager != null) {   
 			mSensorManager.unregisterListener(this);   
 			mSensorManager = null;   
         }
 	}
 
 	@Override
     protected void onRestart() {
 		super.onRestart();
 		this.onCreate(null);
     }
 	//Ҧܼƫŧi
 	private ImageView btn_1;
 	private ImageView btn_close;
 	private ImageView btn_set;
 	private ImageView btn_description;
 	private AdView adView;
 	private int[] set_number;
 
 	//ΨӧP_O_w½}
 	private boolean is_btn_pressed = false;
 
 	//ѤdҦܼ
 	private int cheat_mod = 0; // 0:NoStatus,1:AlwaysLose,2:AlwaysWin
 
 	//n
 	private static final int MIN_FORCE = 10;
 	private static final int MIN_DIRECTION_CHANGE = 3;
 	private static final int MAX_PAUSE_BETHWEEN_DIRECTION_CHANGE = 200;
 	private static final int MAX_TOTAL_DURATION_OF_SHAKE = 400;
 	private long mFirstDirectionChangeTime = 0;
 	private long mLastDirectionChangeTime;
 	private int mDirectionChangeCount = 0;
 	private float lastX = 0;
 	private float lastY = 0;
 	private float lastZ = 0;
 	private OnShakeListener mShakeListener;
 	private SensorManager mSensorManager;
 	private Ninedrink mSensorListener;
 
 	//_ʪ
 	private Vibrator mVibrator;
 
 	//Pw
 	private int[] pokerPic = {
 			R.drawable.icon_jocker,R.drawable.icon_1,R.drawable.icon_2,
 			R.drawable.icon_3,R.drawable.icon_4,R.drawable.icon_5,
 			R.drawable.icon_6,R.drawable.icon_7,R.drawable.icon_8,
 			R.drawable.icon_9,R.drawable.icon_10,R.drawable.icon_11,
 			R.drawable.icon_12,R.drawable.icon_13
 	};
 	private List<Integer> pokerList = new ArrayList<Integer>();
 
 	//Ī
 	private MediaPlayer mp;
 
 	//]wƮw
 	public NDDBOpenHelper mOpenHelper;
 
 
 	//n̤
 	public interface OnShakeListener {
 		void onShake();
 	}
 
 	public void setOnShakeListener(OnShakeListener listener){
 		mShakeListener = listener;
 	}
 
 	private void findView(){
 		btn_1 = (ImageView)findViewById(R.id.imgbtn_1);
 		btn_close = (ImageView)findViewById(R.id.imgbtn_close);
 		btn_set = (ImageView)findViewById(R.id.imgbtn_set);
 		btn_description = (ImageView)findViewById(R.id.imgbtn_description);
 		//Ads--Start
 		adView = (AdView)findViewById(R.id.adView);
 		AdRequest adRequest = new AdRequest();
 		//adRequest.setTesting(true);
 		adView.loadAd(adRequest);
 		//Ads--End
 		//]wL
 		mp = MediaPlayer.create(getBaseContext(), R.raw.dealsound);
 	}
 
 	private void setLisenter(){
 		btn_close.setOnClickListener(close);
 		btn_1.setOnClickListener(open);
 		btn_set.setOnClickListener(setting);
 		btn_description.setOnClickListener(description);
 	}
 
 	private void otherObject(){
 		mVibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
 	}
 
 	//n̨ƥ]w
 	@Override
 	public void onSensorChanged(SensorEvent se){
 		float x = se.values[SensorManager.DATA_X];
 		float y = se.values[SensorManager.DATA_Y];
 		float z = se.values[SensorManager.DATA_Z];
 		float totalMovement = Math.abs(x + y + z - lastX - lastY - lastZ);
 		if (totalMovement > MIN_FORCE){
 			long now = System.currentTimeMillis();
 			if (mFirstDirectionChangeTime == 0){
 				mFirstDirectionChangeTime = now;
 				mLastDirectionChangeTime = now;
 			}
 			long lastChangeWasAgo = now - mLastDirectionChangeTime;
 			if (lastChangeWasAgo < MAX_PAUSE_BETHWEEN_DIRECTION_CHANGE){
 				mLastDirectionChangeTime = now;
 				mDirectionChangeCount++;
 				lastX = x;
 				lastY = y;
 				lastZ = z;
 				if (mDirectionChangeCount >= MIN_DIRECTION_CHANGE){
 					long totalDuration = now - mFirstDirectionChangeTime;
 					if (totalDuration < MAX_TOTAL_DURATION_OF_SHAKE){
 						mShakeListener.onShake();
 						resetShakeParameters();
 					}
 				}
 			}else{
 				resetShakeParameters();
 			}
 		}
 	}
 
 	private void resetShakeParameters(){
 		mFirstDirectionChangeTime = 0;
 		mDirectionChangeCount = 0;
 		mLastDirectionChangeTime = 0;
 		lastX = 0;
 		lastY = 0;
 		lastZ = 0;
 	}
 
 	@Override
 	public void onAccuracyChanged(Sensor sensor, int accuracy){
 		// TODO Auto-generated method stub
 	}
 
 	//n̰ʧ@
 	private void shake(){
 		mSensorListener = new Ninedrink();
 		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
 		mSensorManager.registerListener(mSensorListener,
 		mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
 			SensorManager.SENSOR_DELAY_UI);
 		mSensorListener.setOnShakeListener(new Ninedrink.OnShakeListener(){
 			public void onShake(){
 				reset_card();
 			}
 		});
 	}
 
 	//c~Pƥ
 	private void reset_card(){
 		if(is_btn_pressed){
 		Animation animation = AnimationUtils.loadAnimation(Ninedrink.this, R.anim.back_scale);
 		animation.setAnimationListener(new Animation.AnimationListener(){
 			@Override
 			public void onAnimationStart(Animation animation){ }
 
 			@Override
 			public void onAnimationRepeat(Animation animation){ }
 
 			@Override
 			public void onAnimationEnd(Animation animation){
 				  btn_1.setImageResource(R.drawable.selector);
 				  btn_1.startAnimation(AnimationUtils.loadAnimation(Ninedrink.this, R.anim.front_scale));
 				}
 			});
 			btn_1.startAnimation(animation);
 		}
 		is_btn_pressed = false;
 		cheat_mod = 0; //٭w]
 
 		set_number = getRandom();
 		mVibrator.vibrate(500);
 	}
 
 	//½Pƥ
 	private View.OnClickListener open = new View.OnClickListener(){
 		@Override
 		public void onClick(View v){
 			switch_cards(0);
 
 			Animation animation = AnimationUtils.loadAnimation(Ninedrink.this, R.anim.back_scale);
 			animation.setAnimationListener(new Animation.AnimationListener(){
 				@Override
 				public void onAnimationStart(Animation animation){ }
 
 				@Override
 				public void onAnimationRepeat(Animation animation){ }
 
 				@Override
 				public void onAnimationEnd(Animation animation){
 					btn_1.setImageResource(set_number[0]);
 					btn_1.startAnimation(AnimationUtils.loadAnimation(Ninedrink.this, R.anim.front_scale));
 				}
 			});
 			if(!is_btn_pressed){
 				btn_1.startAnimation(animation);
 			    mp.start();
 				call_v(0);
 				is_btn_pressed = true;
 			}
 			else{
 				reset_card();
 			}
 		}
 	};
 
 	//iJ]w
 	private View.OnClickListener setting = new View.OnClickListener(){
 		@Override
 		public void onClick(View v){
 			// TODO Auto-generated method stub
 			Intent i = new Intent();
 			i.setClass(Ninedrink.this, Settings.class);
 			startActivity(i);
 			
 		}
 	};
 
 	//iJ
 	private View.OnClickListener description = new View.OnClickListener(){
 		@Override
 		public void onClick(View v){
 			// TODO Auto-generated method stub
 			Intent i = new Intent();
 			i.setClass(Ninedrink.this, Description.class);
 			startActivity(i);
 		}
 	};
 
 	//]wPw
 	private void setPokers(){
 		int i = 0;
 		Cursor result = mOpenHelper.getAll();
 		result.moveToFirst();
 		while (!result.isAfterLast()){
 			i = result.getInt(0);
 			if(Boolean.valueOf(result.getString(1).equals("1"))){
 				pokerList.add(pokerPic[i]);
 			}else{}
 			result.moveToNext();
 		}
 	}
 
 	//üƱƧ
 	private int[] getRandom(){
 		//l
 		Object in[] = pokerList.toArray();
 		int x[] = new int[in.length];   
 		int tmp,y,z;
 		Random r = new Random(System.currentTimeMillis());
 		//~P
 		for(int i = 0; i < in.length; i++){
 			x[i] = ((Integer)in[i]).intValue();
 		}
 		for(int i = 0; i < 100; i++){
 			y = r.nextInt(x.length);
 			z = r.nextInt(x.length);
 			tmp = x[z];
 			x[z] = x[y];
 			x[y] = tmp;
 		}
 		return x;
 	}
 
 	//
 	protected static final int MENU_ABOUT = Menu.FIRST;
 	protected static final int MENU_Quit = Menu.FIRST+1;
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu){
 		super.onCreateOptionsMenu(menu);
 		menu.add(0,MENU_ABOUT,0,"");
 		menu.add(0,MENU_Quit,0,"");
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item){
 		super.onOptionsItemSelected(item);
 		switch(item.getItemId()){
 		case MENU_ABOUT:
 			openOptionsDialog();
 			break;
 		case MENU_Quit:
 			finish();
 			break;
 		}
 		return true;
 	}
 
 	private void openOptionsDialog(){
 		new AlertDialog.Builder(Ninedrink.this).setTitle(R.string.about_title)
 			.setMessage(R.string.about_msg)
 			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
 				public void onClick(DialogInterface dialog, int i){
 				}
 			})
 			.setNegativeButton(R.string.lb_homepage, new DialogInterface.OnClickListener(){
 				public void onClick(DialogInterface dialog, int i){
 					Uri uri = Uri.parse(getString(R.string.url_homepage));
 					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
 					startActivity(intent);
 				}
 			})
 			.show();
 	}
 
 	//{
 	private Button.OnClickListener close = new Button.OnClickListener(){
 		@Override
 		public void onClick(View v){
 			int pid = android.os.Process.myPid();
 			android.os.Process.killProcess(pid);
 		}
 	};
 
 	//ѤdҦ:ťq
 	@Override
 	public boolean onKeyDown(int keyCode,KeyEvent event){
 		if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
 			cheat_mod = 1; //Always lose
 			return true;
 		}else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP){
 			cheat_mod = 2; //Always win and Plus Drink XD
 			return true;
 		}else{
 			cheat_mod = 0; //Cancel
 			return false;
 		}
 		
 	}
 
 	public void switch_cards(int n){
 		if(cheat_mod == 1){
 			if(set_number[n] != R.drawable.icon_9 && is_btn_pressed == false){
 				if(pokerList.indexOf(pokerPic[9]) > 0){
 					set_number[n] = R.drawable.icon_9;
 				}else{}
 			}
 		}else if(cheat_mod == 2){
 			if(set_number[n] != R.drawable.icon_7 && is_btn_pressed == false){
 				if(pokerList.indexOf(pokerPic[7]) > 0){
 					set_number[n] = R.drawable.icon_7;
 				}else{}
 			}
 		}else{}
 	}
 
 	//F_
 	private void call_v(int n){
 		if (set_number[n] == R.drawable.icon_9){
 			mVibrator.vibrate(500);
 		}
 	}
 	/*//հT
 	private void openTestDialog(){
 		Toast popup =  Toast.makeText(Ninedrink.this, R.string.about_title, Toast.LENGTH_SHORT);
 		popup.show();
 	}
 	*/
 }
