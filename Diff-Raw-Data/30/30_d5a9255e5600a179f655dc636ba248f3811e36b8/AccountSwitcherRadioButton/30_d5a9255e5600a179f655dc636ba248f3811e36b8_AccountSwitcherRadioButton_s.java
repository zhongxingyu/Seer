 /**
  * 
  */
 package de.geotweeter.activities;
 
 import java.util.Observable;
 import java.util.Observer;
 
 import de.geotweeter.Account;
 import de.geotweeter.Constants;
 import de.geotweeter.R;
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.CompoundButton;
 import android.widget.RadioButton;
 import android.widget.RadioGroup.LayoutParams;
 
 /**
  * @author Julian Kuerby
  *
  */
 public class AccountSwitcherRadioButton extends RadioButton implements Observer {
 	
 	private final Account account;
 	private final Context context;
 	
 	public AccountSwitcherRadioButton(Context context, Account account) {
 		super(context);
 		this.account = account;
 		this.context = context;
 		
 		LayoutParams layout = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		layout.setMargins(pixelToDIP(5), pixelToDIP(2), pixelToDIP(1), 0);
 		setLayoutParams(layout);
 		setPadding(pixelToDIP(35), pixelToDIP(4), pixelToDIP(0), pixelToDIP(4));
		setBackgroundResource(R.drawable.account_switcher_selector);
 		// TODO # of unread Tweets
 		setText(account.getUser().getScreenName());
 		
 		setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
 			}
 		});
 		
 		account.addObserver(this);
 	}
 	
 	@Override
 	public void setButtonDrawable(Drawable d) {
 		// TODO Auto-generated method stub
 		super.setButtonDrawable(d);
 	}
 	
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		// TODO # of unread Tweets
 		
 	}
 	
 	private int pixelToDIP(int pixel) {
 		return (int) (pixel * context.getResources().getDisplayMetrics().density + 0.5f);
 	}
 	
 	private class AlphaBitmapDrawable extends BitmapDrawable {
 		private boolean checked;
 		
 		public AlphaBitmapDrawable(Resources res, Bitmap bitmap) {
 			super(res, bitmap);
 			setAlpha(Constants.UNCHECKED_ALPHA_VALUE);
 			checked = false;
 		}
 
 		@Override
 		protected boolean onStateChange(int[] state) {
 			if(state != null) {
 				for (int s : state) {
 					if (s == android.R.attr.state_checked) {
 						if (!checked) {
 							setAlpha(Constants.CHECKED_ALPHA_VALUE);
 							checked = true;
 							return true;
 						}
 						return false;
 					}
 				}
 			}
 			if (checked) {
 				setAlpha(Constants.UNCHECKED_ALPHA_VALUE);
 				checked = false;
 				return true;
 			}
 			return false;
 		}	
 	}
 
 }
