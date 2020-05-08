 package pt.isel.pdm.yamba;
 
 import pt.isel.pdm.yamba.TwitterAsync.services.UserInfoPullService;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.widget.TextView;
 
 
 public class UserInfoActivity extends MenuActivity
 {
 	private AsyncUserInfoPullService _params;
 	
 	private IGetUserInfoCompletedListener _listener;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.userinfo);
 		
 		final TextView 
 			_name = (TextView)findViewById(R.id.userInfoName),
 			_subscriptions = (TextView)findViewById(R.id.userInfoSubscriptions),
 			_subscribers = (TextView)findViewById(R.id.userInfoSubscribers),
 			_status = (TextView)findViewById(R.id.userInfoStatus);
 		
 		if(_listener == null)
 		{
 			_listener = new IGetUserInfoCompletedListener() {
 		
 				@Override
 				public void onGetUsernameCompleted(String username) {
 					
 					if(username == null)
 						return;
 					
 					_name.setText(username);				
 				}
 				
 				@Override
 				public void onGetSubscriptionsCountCompleted(int subscriptions) {
 					_subscriptions.setText(Integer.toString(subscriptions));					
 				}
 				
 				@Override
 				public void onGetSubscribersCountCompleted(int subscribers) {
 					_subscribers.setText(Integer.toString(subscribers));						
 				}
 				
 				@Override
 				public void onGetStatusCountCompleted(int status) {
 					_status.setText(Integer.toString(status));					
 				}
 			};
 		}
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 		if(_params == null)
 		{
 			_params = new AsyncUserInfoPullService();
 		}
 		
 		bindService(new Intent(this, UserInfoPullService.class), _params, BIND_AUTO_CREATE);
 		
 		fillActivityWithUserInfo();
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		
 		unbindService(_params);
 	}
 	
 	@Override
     protected void doModifyMenu(Menu menu) {
    	enableMenuItem(menu, R.id.show_timeline);
    	enableMenuItem(menu, R.id.create_status);
     }
 	
 	private void fillActivityWithUserInfo()
 	{
 		_params.getUserInfo(_listener);
 	}
 }
