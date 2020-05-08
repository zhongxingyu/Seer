 package ghostsheep.com.emergency;
 
 
 import ghostsheep.com.classes.EmergencyView;
 import ghostsheep.com.classes.ReserveCallView;
 import ghostsheep.com.classes.SettingView;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 
 import com.google.ads.AdRequest;
 import com.google.ads.AdView;
 
 public class MainActivity extends Activity implements SensorEventListener {
 
 	private AdView adView;  // Banner
 	
 	private ViewPager viewPager;
 	private PagerAdapterClass pAdapterClass;
 	
 	// 필수 View
 	private EmergencyView emergencyView;
 	private ReserveCallView reserveCallView;
 	private SettingView settingView;
 	
 	private int prePosition;
 	
 	// 진동 확인을 위한 변수
 	private long lastTime;
     private float speed;
     private float lastX;
     private float lastY;
     private float lastZ;
    
     private float x, y, z;
     private static final int SHAKE_THRESHOLD = 800;
     private int cnt;
     
 	private SensorManager sensorManager;
     private Sensor accelerormeterSensor;
     
     // siren 변수
     private SoundPool soundPool;
     private int siren;
     private int streamID;
     private AudioManager audioManager;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         
         cnt = 0;
         sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
         accelerormeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
         
         initSound();
         
         prePosition = 0;
         //<--- ViewPage
         viewPager = (ViewPager)findViewById(R.id.viewPager);
         if (null == pAdapterClass) {
         	pAdapterClass = new PagerAdapterClass(getApplicationContext());
         }
         viewPager.setAdapter(pAdapterClass);
         viewPager.setOnPageChangeListener(new OnPageChangeListener() {
 			
 			@Override
 			public void onPageSelected(int arg0) {
 				// TODO Auto-generated method stub
 				
 				if (2 == arg0) {
 					prePosition = arg0;
 				}
 				
 				if (2 == prePosition && 2 != arg0 && null != settingView) {
 					settingView.SaveSetting();
 				}
 			}
 			
 			@Override
 			public void onPageScrolled(int arg0, float arg1, int arg2) {
 				// TODO Auto-generated method stub
 			}
 			
 			@Override
 			public void onPageScrollStateChanged(int arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
         //---> ViewPage
         
         adView = (AdView)findViewById(R.id.ADMobAD);
         
         emergencyView = new EmergencyView(this);
         reserveCallView = new ReserveCallView(this);
         settingView = new SettingView(this);
     }
     
     private void initSound() {
     	soundPool = new SoundPool( 5, AudioManager.STREAM_MUSIC, 0 );
     	siren = soundPool.load( getApplicationContext(), R.raw.siren, 1 );
     }
     
     @Override
     protected void onStart() {
     	if (2 != viewPager.getCurrentItem()) {
     		if (reserveCallView.chronometerRunning == false) {
     			emergencyView.callEmergency();
     		}
     	}
     	
     	if (accelerormeterSensor != null) {
             sensorManager.registerListener(this, accelerormeterSensor, SensorManager.SENSOR_DELAY_GAME);
     	}
     	
     	super.onStart();
     }
     
     @Override
     protected void onStop() {
     	if (sensorManager != null) {
             sensorManager.unregisterListener(this);
     	}
     	
     	super.onStop();
     }
     
     @Override
     protected void onDestroy() {
     	// TODO Auto-generated method stub
     	super.onDestroy();
     	
     	if (adView != null) {
     		adView.destroy();
     	}
     }
     
     @Override
     protected void onPause() {
     	// TODO Auto-generated method stub
     	super.onPause();
     	
     	if (2 == viewPager.getCurrentItem() && null != settingView) {
     		settingView.SaveSetting();
     		viewPager.setCurrentItem(0);
     	}
     	
     	adView.stopLoading();
     }
     
     @Override
     protected void onResume() {
     	// TODO Auto-generated method stub
     	super.onResume();
     	
     	AdRequest request = new AdRequest();
     	request.addTestDevice(AdRequest.TEST_EMULATOR);
     	request.addTestDevice("6FE8400FADAD3AE9046E42E6A06D9470");
     	adView.loadAd(request);
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return super.onCreateOptionsMenu(menu);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch (item.getItemId()) {
 		
 		case R.id.menu_main:
 		{
 			viewPager.setCurrentItem(0);
 			break;
 		}
 		
 		case R.id.menu_reserv_call:
 		{
 			viewPager.setCurrentItem(1);
 			break;
 		}
 		
 		case R.id.menu_settings:
 		{
 			viewPager.setCurrentItem(2);
 			break;
 		}
 		
 		default:
 			break;
 		}
     	
     	return super.onOptionsItemSelected(item);
     }
     
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
     	switch(keyCode) {
     	case KeyEvent.KEYCODE_BACK:
    		AlertDialog.Builder alert = new Builder(MainActivity.this);
 			alert.setMessage(getString(R.string.exitApp));
 			alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					System.exit(0);
 				}
 			});
 			alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					dialog.dismiss();
 				}
 			});
 			alert.show();
     		
     		break;
     	}
     	return true;
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	// TODO Auto-generated method stub
     	super.onActivityResult(requestCode, resultCode, data);
     	
     	if (null != settingView) {
     		settingView.setResult(requestCode, resultCode, data);
     		viewPager.setCurrentItem(2);
     	}
     }
     
     @Override
     public void onAccuracyChanged(Sensor sensor, int accuracy) {
     	// TODO Auto-generated method stub
     	
     }
 
     @Override
     public void onSensorChanged(SensorEvent event) {
     	// TODO Auto-generated method stub
     	if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
             long currentTime = System.currentTimeMillis();
             long gabOfTime = (currentTime - lastTime);
 
             if (gabOfTime > 150) {
                 lastTime = currentTime;
 
                 x = event.values[0];
                 y = event.values[1];
                 z = event.values[2];
 
                 speed = Math.abs(x + y + z - lastX - lastY - lastZ) /
                         gabOfTime * 10000;
 
                 // 흔들림 감지 시
                 if (speed > SHAKE_THRESHOLD) {
                     cnt++;
                     if (cnt > 4) {
                     	if (0 >= streamID) {
                     		if (null == audioManager) {
                     			audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
                     		}
                     		final int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                     		int volumeMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                     		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeMax, AudioManager.FLAG_PLAY_SOUND);
 	                    	streamID = soundPool.play( siren, 1f, 1f, 0, -1, 1f );
 	                    	cnt = 0;
 	                    	
 	                    	AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
 	                    	builder.setTitle(getString(R.string.turn_off_a_sound));
 	                    	builder.setNegativeButton(getString(R.string.stop), new DialogInterface.OnClickListener() {
 								
 								@Override
 								public void onClick(DialogInterface dialog, int which) {
 									// TODO Auto-generated method stub
 									soundPool.stop(streamID);
 									streamID = 0;
 									audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_PLAY_SOUND);
 								}
 							});
 	                    	builder.setCancelable(false);
                 		builder.show();
                     	}
                     }
                 }
                 lastX = x;
                 lastY = y;
                 lastZ = z;
             }
         }
     }
     
 private class PagerAdapterClass extends PagerAdapter {
 	    
 	    private LayoutInflater mInflater;
 
 	    public PagerAdapterClass(Context c){
 	        super();
 	        mInflater = LayoutInflater.from(c);
 	    }
 	     
 	    @Override
 	    public int getCount() {
 	        return 3;
 	    }
 
 	    @Override
 	    public Object instantiateItem(View pager, int position) {
 	        View v = null;
 	        if (0 == position) {
 	            v = mInflater.inflate(R.layout.emergency, null);
 	            emergencyView.initEvent(v);
 	            
 	        } else if (1 == position) {
 	        	v = mInflater.inflate(R.layout.reserve_call, null);
 	        	reserveCallView.initView(v);
 	        	reserveCallView.initEvent();
 	        }
 	        else {
 	            v = mInflater.inflate(R.layout.setting, null);
 	            settingView.initView(v);
 	            settingView.initEvent(v);
 	        }
 	         
 	        ((ViewPager)pager).addView(v, 0);
 	         
 	        return v; 
 	    }
 
 	    @Override
 	    public void destroyItem(View pager, int position, Object view) {    
 	        ((ViewPager)pager).removeView((View)view);
 	    }
 	     
 	    @Override
 	    public boolean isViewFromObject(View pager, Object obj) {
 	        return pager == obj; 
 	    }
 
 	    @Override public void restoreState(Parcelable arg0, ClassLoader arg1) {}
 	    @Override public Parcelable saveState() { return null; }
 	    @Override public void startUpdate(View arg0) {}
 	    @Override public void finishUpdate(View arg0) {}
 	}
 }
