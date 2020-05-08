 package nabhack.localz.activity;
 
 import nabhack.localz.LocalzApp;
 import nabhack.localz.R;
 import nabhack.localz.models.Deal;
 import nabhack.localz.ui.ImageUtils;
 import android.app.Activity;
 import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.Handler;
 import android.view.Display;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.google.zxing.WriterException;
 import com.googlecode.androidannotations.annotations.AfterViews;
 import com.googlecode.androidannotations.annotations.App;
 import com.googlecode.androidannotations.annotations.EActivity;
 import com.googlecode.androidannotations.annotations.Extra;
 import com.googlecode.androidannotations.annotations.SystemService;
 import com.googlecode.androidannotations.annotations.ViewById;
 
 @EActivity(R.layout.activity_redeem)
 
 public class RedeemActivity extends Activity {
 	
 	@App
 	LocalzApp application;
 	
 	@SystemService
 	WindowManager windowsManager;
 	
 	@Extra(SecureDealActivity.DEALID_INTENT_EXTRAS)
 	String dealid;
 	
 	@ViewById(R.id.title)
 	TextView title;
 
 	@ViewById(R.id.image)
 	ImageView image;
 
 	@ViewById(R.id.special)
 	TextView special;
 
 	@ViewById(R.id.time)
 	TextView time;
 
 	@ViewById(R.id.remaining)
 	TextView remaining;
 	
 
 	@ViewById(R.id.discountCode)
 	TextView discountCode;
 	
 	@AfterViews
 	void initDisplay() {
 		getActionBar().setHomeButtonEnabled(true);
 		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2d3e50")));
 		application.setCurrentDeal(application.getDeal(3));
 		Deal deal = application.getCurrentDeal();
 		title.setText(deal.getTitle());
 		
 		// Comment next block when data available online
 		String uri = "drawable/"
 				+ deal.getDescImgs()[0].replaceFirst("[.][^.]+$", "");
 		int imageResource = getResources().getIdentifier(uri, null,
 				RedeemActivity.this.getPackageName());
 		Drawable drawImage = RedeemActivity.this.getResources().getDrawable(
 				imageResource);
 		image.setImageDrawable(drawImage);
 		discountCode.setText("Provide Discount Code to " + deal.getStore().getName() + " to claim!");
 
 		time.setText(getTimeFormat(deal.getSecondsToExpire()));
 		timerCountDown(deal.getSecondsToExpire(), time);
 		
 	}
 	
 	public void timerCountDown(final int secondCount, final TextView textView) {
 
 		Handler handler = new Handler();
 		handler.postDelayed(new Runnable() {
 			public void run() {
 				if (secondCount > 0) {
 					// format time and display
 					textView.setText(getTimeFormat(secondCount));
 					timerCountDown(secondCount - 1, textView);
 				} else {
 
 				}
 			}
 		}, 1000);
 	}
 
 	private String getTimeFormat(int secs) {
 		int hours = secs / 3600, remainder = secs % 3600, minutes = remainder / 60, seconds = remainder % 60;
 
 		String disHour = (hours < 10 ? "0" : "") + hours, disMinu = (minutes < 10 ? "0"
 				: "")
 				+ minutes, disSec = (seconds < 10 ? "0" : "") + seconds;
 		return disHour + ":" + disMinu + ":" + disSec;
 	}
 
 }
