 package pl.edu.agh.io.coordinator;
 
 import java.io.InputStream;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class ShowUserFragment extends Fragment {
 	private static final String ARG_USER_NAME = "userName";
 	private static final String ARG_USER_SURNAME = "userSurname";
 	private static final String ARG_USER_NICK = "userNick";
 	private static final String ARG_USER_PHONE = "userPhone";
 	private static final String ARG_USER_EMAIL = "userEmail";
 	private static final String ARG_USER_AVATAR = "userAvatar";
 
 	private String userName;
 	private String userSurname;
 	private String userNick;
 	private String userPhone;
 	private String userEmail;
 	private String userAvatar;
 
 	private TextView nameView;
 	private TextView surnameView;
 	private TextView nickView;
 	private TextView telephoneView;
 	private TextView mailView;
 	private ImageView avatarView;
 	private Button callButton;
 	private Button composeButton;
 
 	public static ShowUserFragment newInstance(String userName,
 			String userSurname, String userNick, String userPhone,
 			String userEmail, String userAvatar) {
 		ShowUserFragment fragment = new ShowUserFragment();
 		Bundle args = new Bundle();
 		args.putString(ARG_USER_NAME, userName);
 		args.putString(ARG_USER_SURNAME, userSurname);
 		args.putString(ARG_USER_NICK, userNick);
 		args.putString(ARG_USER_PHONE, userPhone);
 		args.putString(ARG_USER_EMAIL, userEmail);
 		args.putString(ARG_USER_AVATAR, userAvatar);
 		fragment.setArguments(args);
 		return fragment;
 	}
 
 	public ShowUserFragment() {
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		if (getArguments() != null) {
 			userName = getArguments().getString(ARG_USER_NAME);
 			userSurname = getArguments().getString(ARG_USER_SURNAME);
 			userNick = getArguments().getString(ARG_USER_NICK);
 			userPhone = getArguments().getString(ARG_USER_PHONE);
 			userEmail = getArguments().getString(ARG_USER_EMAIL);
 			userAvatar = getArguments().getString(ARG_USER_AVATAR);
 		}
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.fragment_show_user, container,
 				false);
 
 		nameView = (TextView) (view.findViewById(R.id.userName));
 		surnameView = (TextView) (view.findViewById(R.id.userSurname));
 		nickView = (TextView) (view.findViewById(R.id.userNick));
 		telephoneView = (TextView) (view.findViewById(R.id.userPhone));
 		mailView = (TextView) (view.findViewById(R.id.userEmail));
 		avatarView = (ImageView) (view.findViewById(R.id.userAvatar));
 		callButton = (Button) (view.findViewById(R.id.buttonCall));
 		composeButton = (Button) (view.findViewById(R.id.buttonCompose));
 
 		nameView.setText(userName);
 		surnameView.setText(userSurname);
 		nickView.setText(userNick);
 		telephoneView.setText(userPhone);
 		mailView.setText(userEmail);
 
 		if (telephoneView.getText().equals(""))
 			callButton.setVisibility(View.INVISIBLE);
 		else {
 			callButton.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					String tel = "tel:" + userPhone;
 					Intent intent = new Intent(Intent.ACTION_CALL, Uri
 							.parse(tel));
 					startActivity(intent);
 
 				}
 			});
 		}
 
 		if (mailView.getText().equals(""))
 			composeButton.setVisibility(View.INVISIBLE);
 		else {
 			composeButton.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					Intent intent = new Intent(Intent.ACTION_SENDTO);
 					intent.setType("text/plain");
 					// intent.putExtra(Intent.EXTRA_EMAIL, userEmail);
 					intent.setData(Uri.parse("mailto:" + userEmail));
 					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 					startActivity(intent);
 
 				}
 			});
 		}
 
 		if (!userAvatar.equals(""))
 			new GetImageInBackground().execute();
 
 		return view;
 	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 	}
 
 	@Override
 	public void onDetach() {
 		super.onDetach();
 	}
 
 	private class GetImageInBackground extends AsyncTask<Void, Void, Bitmap> {
 
 		protected Bitmap doInBackground(Void... urls) {
 			Bitmap bitmapImage = null;
 			try {
 				InputStream in = new java.net.URL(userAvatar).openStream();
 				bitmapImage = BitmapFactory.decodeStream(in);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return bitmapImage;
 		}
 
 		protected void onPostExecute(Bitmap bitmap) {
 			if (bitmap != null)
 				avatarView.setImageBitmap(bitmap);
 		}
 	}
 
 }
