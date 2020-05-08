 package org.ekh.laboiteasons;
 
 import java.lang.reflect.Field;
 import java.util.Arrays;
 
 import org.ekh.adapter.ImageAdapter;
 
 import android.app.Activity;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.GestureDetector.OnGestureListener;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.GridView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ViewFlipper;
 
 public class MainActivity extends Activity implements OnGestureListener {
 	
 	GridView gridView;
 	static final String[] SOUNDS = new String[] { 
 		"dayum","fart","hallelujah","applause","cow", "check","bell","dayum","cat","ninja", "dayum","fart","hallelujah","applause","cow", "check","bell","dayum","cat","ninja" };
 	
 	MediaPlayer mp = null;
 	
 	private static final int SWIPE_MIN_DISTANCE = 120;
 	private static final int SWIPE_MAX_OFF_PATH = 250;
 	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
 	
 	private Animation slideLeftIn;
 	private Animation slideLeftOut;
 	private Animation slideRightIn;
 	private Animation slideRightOut;
 	
 	private GestureDetector detector;
 	private ViewFlipper view;
 	
 	private int nb_page;
 	
 	private static final String TAG = "LaBoiteASons";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		detector = new GestureDetector(this,this);
 		view = (ViewFlipper)findViewById(R.id.flipper);
 		
 		Log.v(TAG, "nb_sounds=" + SOUNDS.length);
 		
 		
		int nb_sounds_per_page = 9;
 		int current_length = SOUNDS.length;
 		int nb_sounds_remaining = SOUNDS.length;
 		
 		//Count the pages
 		nb_page = SOUNDS.length/nb_sounds_per_page;
 		Log.v(TAG, "nb_page=" + nb_page);
 		int reste = SOUNDS.length%nb_sounds_per_page;
 		Log.v(TAG, "reste=" + reste);
 		if(reste > 0){
 			++nb_page;
 		}
 		Log.v(TAG, "nb_page=" + nb_page);
 		
 		for(int i=0; i<nb_page; ++i){
 			Log.v(TAG, "in first for");
 
 			//Calculate the current length of the sounds array of the current page
 			//if it's the last page
 			if(i == (nb_page-1)){
 				current_length = nb_sounds_remaining;
 				Log.v(TAG, "else if");
 				
 			}
 			else if(nb_page > 1){
 				current_length = nb_sounds_per_page; 
 				nb_sounds_remaining -= nb_sounds_per_page;
 				Log.v(TAG, "else");
 			}
 			
 			
 			Log.v(TAG, "current_length="+current_length);
 			Log.v(TAG, "nb_sounds_in="+nb_sounds_remaining);
 			
 			//Fill the sounds array
 			String[] sounds = new String[current_length];
 			for(int j=0; j<current_length; ++j){
 				sounds[j] = SOUNDS[j*(i+1)];
 				//Log.v(TAG, "in second for");
 			}
 			
 			Log.v(TAG, "before switch");
 			//Call the correct gridView in other words the correct page
 			switch(i){
 				case 0:
 					gridView = (GridView) findViewById(R.id.gridView1);
 					break;
 				case 1:
 					gridView = (GridView) findViewById(R.id.gridView2);
 					break;
 			}
 
 			Log.v(TAG, "after switch");
 			//Fill the gridView
 			gridView.setAdapter(new ImageAdapter(this, sounds));
 			
 			gridView.setOnItemClickListener(new OnItemClickListener() {
 				public void onItemClick(AdapterView<?> parent, View v,
 						int position, long id) {
 					Toast.makeText(
 							getApplicationContext(),
 							((TextView) v.findViewById(R.id.grid_item_label))
 							.getText(), Toast.LENGTH_SHORT).show();
 					String soundString = ((TextView) v.findViewById(R.id.grid_item_label)).getText().toString();
 					playThatSound(soundString);
 				}
 			});
 			
 			gridView.setOnTouchListener(new OnTouchListener(){
 			    @Override
 			    public boolean onTouch(View v, MotionEvent event) {
 			        return detector.onTouchEvent(event);
 			    }
 			});
 		}
 		
 		slideLeftIn = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
 		slideLeftOut = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
 		slideRightIn = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
 		slideRightOut = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);
 		
 	}
 	
 	@Override
 	public boolean onTouchEvent(MotionEvent event){
 		return detector.onTouchEvent(event);
 	}
 	
 	@Override
 	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
 		if(nb_page>1){
 			if(Math.abs(e1.getY()-e2.getY()) > 250) return false;
 			
 			if((e1.getX()-e2.getX()) > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY){
 				view.setInAnimation(slideLeftIn);
 				view.setOutAnimation(slideLeftOut);
 				view.showNext();
 			}
 			else if((e2.getX()-e1.getX()) > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY){
 				view.setInAnimation(slideRightIn);
 				view.setOutAnimation(slideRightOut);
 				view.showPrevious();
 			}
 		}
 			
 		return false;
 	}
 
 	@Override
 	public boolean onDown(MotionEvent e) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void onLongPress(MotionEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
 			float distanceY) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void onShowPress(MotionEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean onSingleTapUp(MotionEvent e) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	
 	public static int getResId(String variableName, Class<?> c) {
 	    Field field = null;
 	    int resId = 0;
 	    try {
 	        field = c.getField(variableName);
 	        try {
 	            resId = field.getInt(null);
 	        } catch (Exception e) {
 	            e.printStackTrace();
 	        }
 	    } catch (Exception e) {
 	        e.printStackTrace();
 	    }
 	    return resId;
 	}
 	
 	protected void playThatSound(String theSoundString) {
         if (mp != null) {
             mp.reset();
             mp.release();
         }
         if (Arrays.asList(SOUNDS).contains(theSoundString))
             mp = MediaPlayer.create(this, getResId(theSoundString, R.raw.class));
         mp.start();
     }
 }
