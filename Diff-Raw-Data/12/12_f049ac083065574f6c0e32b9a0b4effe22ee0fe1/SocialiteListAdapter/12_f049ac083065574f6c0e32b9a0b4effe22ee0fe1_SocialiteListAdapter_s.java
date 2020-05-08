 package ca.ryerson.scs.rus.adapters;
 
 import java.util.ArrayList;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import ca.ryerson.scs.rus.R;
 import ca.ryerson.scs.rus.socialite.objects.User;
 import ca.ryerson.scs.rus.util.DefaultUser;
 import ca.ryerson.scs.rus.util.IntentRes;
 import ca.ryerson.scs.rus.util.URLResource;
 
 import android.content.Context;
 import android.content.Intent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class SocialiteListAdapter extends ArrayAdapter<User> {
 
 	private LayoutInflater inflater;
 	ArrayList<User> users = new ArrayList<User>();
 	Context context;
 	
 	public SocialiteListAdapter(Context context, int textViewResourceId,
 			ArrayList<User> users) {
 		super(context, textViewResourceId, users);
 		this.users = users;
 		this.inflater = LayoutInflater.from(context);
 		this.context = context;
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		final User user = getItem(position);
 		
 		if (convertView==null){
 			convertView =  inflater.inflate(R.layout.listview_socialite, null);
 		}
 		
 		TextView username = (TextView) convertView.findViewById(R.id.SocialUsername);
 		username.setText(user.getUsername());
 		
 		TextView program = (TextView) convertView.findViewById(R.id.SocialProgram);
 		program.setText(user.getProgram());
 		
 		TextView about = (TextView) convertView.findViewById(R.id.SocialAbout);
 		about.setText(user.getAbout());
 		
 		TextView longitude = (TextView) convertView.findViewById(R.id.SocialLong);
 		longitude.setText("(Long: "+user.getLongitude());
 		
 		TextView latitude = (TextView) convertView.findViewById(R.id.SocialLat);
 		latitude.setText(",Lat: "+user.getLatitude()+")");
 		
 		TextView profileBtn = (TextView) convertView.findViewById(R.id.BtnFLProfile);
 		final TextView requestBtn = (TextView) convertView.findViewById(R.id.BtnFLFriend);
 		
 		profileBtn.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				Intent showProfile = new Intent(IntentRes.PROFILE_STRING);
 				showProfile.putExtra("username", user.getUsername());
 				showProfile.putExtra("email", user.getEmail());
 				showProfile.putExtra("status", user.getAbout());
 				context.startActivity(showProfile); 
 			}
 		});		
 		
 		requestBtn.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				//TODO: CORRECT THE URL, SHOW A TOAST
 				
 				JSONObject json = new JSONObject();
 				try {
 					json.put("username", DefaultUser.getUser());
 					json.put("requestUser", user.getUsername());
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
				HttpRequestAdapter.httpRequest(context, URLResource.REGISTER, json,
 						new FriendRequestHandler());
 				requestBtn.setClickable(false);
 			}
 		});
 		
 		
 		/*
 		ImageView avatar = (ImageView) convertView.findViewById(R.id.SocialAvatar);
 		byte[] decodedString = Base64.decode(user.getPicture(), Base64.DEFAULT);
 		Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length); 
 		avatar.setImageBitmap(decodedByte);
 		*/		
 		
 		return convertView;
 	}
 	
 	private class FriendRequestHandler implements HttpRequestAdapter.ResponseHandler {
 
 		@Override
 		public void postResponse(JSONObject response) {
 			Toast.makeText(context, "Friend Request Sent", Toast.LENGTH_LONG)
 			.show();
 		}
 
 		@Override
 		public void postTimeout() {
 			// TODO Auto-generated method stub
 
 		}
 
 	}
 
 }
