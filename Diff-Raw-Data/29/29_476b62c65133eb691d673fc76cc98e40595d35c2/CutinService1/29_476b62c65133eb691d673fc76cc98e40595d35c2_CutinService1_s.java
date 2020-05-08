 package cutin.sample;
 
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.RotateAnimation;
 
 import com.garlicg.cutinlib.CutinService;
 
 public class CutinService1 extends CutinService{
 	
 	private View mDroid;
 
	/**
	 * Create root view which is inflated to full screen window.
	 */
 	@Override
 	protected View create() {
 		LayoutInflater inflater = LayoutInflater.from(this);
 		View layout = inflater.inflate(R.layout.cutin_service_layout,null);
 		
 		// Keep Droid view for using on start()
 		mDroid = layout.findViewById(R.id.droid);
 		
 		// need to return root view.
 		return layout;
 	}
 	
	/**
	 * start() is called after create(). At this time, view size is possible to get.
	 * You must call finishCutin() or stopSelf() after your animation ending.
	 */
 	@Override
 	protected void start() {
 		// Animation which droid 2 rotate.
 		int centerX = mDroid.getWidth()/2;
 		int centerY = mDroid.getHeight()/2;
 		
 		RotateAnimation anim = new RotateAnimation(0.f, 720f , centerX , centerY);
 		anim.setDuration(1000);
 		anim.setAnimationListener(new AnimationListener() {
 			@Override
 			public void onAnimationStart(Animation animation) {
 			}
 			
 			@Override
 			public void onAnimationRepeat(Animation animation) {
 			}
 			
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				// Call absolutely!
 				finishCutin(); 
 			}
 		});
 		mDroid.startAnimation(anim);
 	}
 
	/**
	 * Release the resources, etc.
	 */
 	@Override
 	protected void destroy() {
 	}
 }
