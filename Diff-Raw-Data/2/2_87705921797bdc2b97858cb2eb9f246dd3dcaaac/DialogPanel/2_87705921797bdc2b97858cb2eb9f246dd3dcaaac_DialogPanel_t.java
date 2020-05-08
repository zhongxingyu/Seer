 package uk.co.thomasc.wordmaster.view;
 
 import uk.co.thomasc.wordmaster.R;
 import uk.co.thomasc.wordmaster.R.anim;
 
 import android.content.Context;
 import android.os.Handler;
 import android.os.Message;
 import android.util.AttributeSet;
 import android.view.View;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.AnimationUtils;
 import android.widget.FrameLayout;
 import android.widget.TextView;
 
 public class DialogPanel extends FrameLayout {
 
 	private View container;
 	private static Handler h;
	private final int displayTime = 5000;
 	
 	public DialogPanel(Context context) {
 		super(context);
 		init();
 	}
 	
 	public DialogPanel(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init();
 	}
 	
 	public DialogPanel(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init();
 	}
 	
 	public void init() {
 		h = new TimerHandler(this);
 		
 		inflate(getContext(), R.layout.network_error, this);
 		container = findViewById(R.id.network_error);
 		container.setVisibility(View.GONE);
 	}
 	
 	public void show(Errors error) {
 		if (container.getVisibility() == View.GONE) {
 			container.setVisibility(View.VISIBLE);
 			Animation showAnim = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down);
 			showAnim.setAnimationListener(new AnimationListener() {
 				@Override
 				public void onAnimationStart(Animation animation) {}
 				@Override
 				public void onAnimationRepeat(Animation animation) {}
 				@Override
 				public void onAnimationEnd(Animation animation) {
 					h.sendEmptyMessageDelayed(0, displayTime); // Time to show dialog for
 				}
 			});
 			container.startAnimation(showAnim);
 		} else {
 			h.removeMessages(0);
 			h.sendEmptyMessageDelayed(0, displayTime);
 		}
 		((TextView) findViewById(R.id.errortxt)).setText(error.getTitle());
 		((TextView) findViewById(R.id.errorhelp)).setText(error.getSubtitle());
 	}
 	
 	public void setTitle(String title) {
 		((TextView) findViewById(R.id.errortxt)).setText(title);
 	}
 	
 	public void setSubtitle(String subtitle) {
 		((TextView) findViewById(R.id.errorhelp)).setText(subtitle);
 	}
 	
 	private static class TimerHandler extends Handler {
 		private DialogPanel dialogPanel;
 		
 		public TimerHandler(DialogPanel dialogPanel) {
 			this.dialogPanel = dialogPanel;
 		}
 
 		@Override
 		public void handleMessage(Message msg) {
 			if (dialogPanel.container.getVisibility() == View.VISIBLE) {
 				Animation removeAnim = AnimationUtils.loadAnimation(dialogPanel.getContext(), anim.slide_up);
 				removeAnim.setAnimationListener(new AnimationListener() {
 					@Override
 					public void onAnimationEnd(Animation animation) {
 						dialogPanel.container.setVisibility(View.GONE);
 					}
 					@Override
 					public void onAnimationRepeat(Animation animation) {}
 					@Override
 					public void onAnimationStart(Animation animation) {}
 				});
 				dialogPanel.container.startAnimation(removeAnim);
 			}
 		}
 	}
 	
 }
