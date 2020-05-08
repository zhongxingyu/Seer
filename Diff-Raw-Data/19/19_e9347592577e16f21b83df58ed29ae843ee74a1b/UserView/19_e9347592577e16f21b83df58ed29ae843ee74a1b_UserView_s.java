 package com.ntnu.eit.common.view;
 
 import android.content.Context;
import android.graphics.Canvas;
 import android.preference.PreferenceManager;
 import android.util.AttributeSet;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.ntnu.eit.R;
 import com.ntnu.eit.common.model.User;
 import com.ntnu.eit.common.service.ServiceFactory;
 
 public class UserView extends RelativeLayout{
 
 	public UserView(Context context, AttributeSet attrs) {
 		//Super
 		super(context, attrs);
 		
 		//Init
 		inflate(context, R.layout.logged_in_user_view, this);
 		
 		//Text
 		TextView firstNameView = (TextView) findViewById(R.id.user_view_first_name);
 		TextView lastNameView = (TextView) findViewById(R.id.user_view_last_name);
 		
 		//Getting user info
 		User user = ServiceFactory.getInstance().getAuthenticationService().getLoggedInUser();
 		firstNameView.setText(user.getFirstname());
 		lastNameView.setText(user.getLastname());
 	}
 	
 	@Override
 	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
 		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
 		
 		//Init
 		TextView firstNameView = (TextView) findViewById(R.id.user_view_first_name);
 		TextView lastNameView = (TextView) findViewById(R.id.user_view_last_name);
 		
 		//Text Size
 		int size = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("text_size", 50);
 		firstNameView.setTextSize(50*size/100);
 		lastNameView.setTextSize(50*size/100);
 	}
 }
