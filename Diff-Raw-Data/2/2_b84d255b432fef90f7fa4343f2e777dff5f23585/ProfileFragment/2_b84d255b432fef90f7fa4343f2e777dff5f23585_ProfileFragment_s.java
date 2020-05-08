 package com.wehuibao;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.Html;
 import android.text.Spanned;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.wehuibao.json.Auth;
 import com.wehuibao.json.AuthList;
 import com.wehuibao.util.net.ImageDownloader;
 import com.wehuibao.util.net.UserFetchTask;
 
 public class ProfileFragment extends SherlockFragment implements
 		OnClickListener {
 	private AuthList authList = null;
 	private TextView profileName;
 	private TextView profileDesc;
 	private Button homeButton;
 	private Button logoutButton;
 	private Button sinaButton;
 	private Button qqButton;
 	private Button doubanButton;
 	private Button fanfouButton;
 	private String cookie;
 	private static final String LOGOUT_URL = "http://wehuibao.com/api/logout";
 	private static final String AUTH_URL = "http://weihuibao.com/apilogin/";
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		setRetainInstance(true);
 		setHasOptionsMenu(true);
 		View view = inflater.inflate(R.layout.profile, container, false);
 		Intent intent = this.getActivity().getIntent();
 		String userId = intent.getStringExtra(ProfileActivity.USERID);
 		profileName = (TextView) view.findViewById(R.id.profileName);
 		profileDesc = (TextView) view.findViewById(R.id.profileDescription);
 		homeButton = (Button) view.findViewById(R.id.homeButton);
 		homeButton.setOnClickListener(this);
 		sinaButton = (Button) view.findViewById(R.id.sinaButton);
 		sinaButton.setOnClickListener(this);
 		qqButton = (Button) view.findViewById(R.id.qqButton);
 		qqButton.setOnClickListener(this);
 		doubanButton = (Button) view.findViewById(R.id.doubanButton);
 		doubanButton.setOnClickListener(this);
 		fanfouButton = (Button) view.findViewById(R.id.fanfouButton);
 		fanfouButton.setOnClickListener(this);
 		logoutButton = (Button) view.findViewById(R.id.logout);
 		SharedPreferences prefs = PreferenceManager
 				.getDefaultSharedPreferences(getActivity()
 						.getApplicationContext());
 		cookie = prefs.getString("cookie", null);
		if (!userId.isEmpty() && authList == null) {
 			String url = "http://wehuibao.com/api/user/" + userId;
 			new FetchUserTask().execute(url);
 		} else {
 			setUpView();
 		}
 
 		if (cookie != null) {
 			logoutButton.setVisibility(View.VISIBLE);
 			logoutButton.setOnClickListener(this);
 		}
 		return view;
 	}
 
 	private Spanned buildAuthLink(String url, String desc) {
 		StringBuffer buffer = new StringBuffer();
 		buffer.append("<a href=\"");
 		buffer.append(url);
 		buffer.append("\">");
 		buffer.append(desc);
 		buffer.append("</a>");
 		return Html.fromHtml(buffer.toString());
 	}
 
 	private void setUpButtonText(Button button, Auth auth) {
 		button.setTag(auth);
 		if (auth.isInstalled) {
 			button.setText(buildAuthLink(auth.auth_url, auth.screen_name));
 		} else {
 			String unAuthorizedUser = getString(R.string.unauthorized_user);
 			if (authList.is_self) {
 				button.setText(buildAuthLink(
 						AUTH_URL + AuthService.SINA.toString(),
 						unAuthorizedUser));
 			} else {
 				button.setText(unAuthorizedUser);
 			}
 
 		}
 		button.setClickable(authList.is_self || auth.isInstalled);
 	}
 
 	private void setUpView() {
 		String unAuthorizedUser = getString(R.string.unauthorized_user);
 		if (authList == null) {
 			sinaButton.setText(buildAuthLink(
 					AUTH_URL + AuthService.SINA.toString(), unAuthorizedUser));
 			qqButton.setText(buildAuthLink(
 					AUTH_URL + AuthService.QQ.toString(), unAuthorizedUser));
 			doubanButton
 					.setText(buildAuthLink(
 							AUTH_URL + AuthService.DOUBAN.toString(),
 							unAuthorizedUser));
 			fanfouButton
 					.setText(buildAuthLink(
 							AUTH_URL + AuthService.FANFOU.toString(),
 							unAuthorizedUser));
 			profileDesc.setVisibility(View.GONE);
 
 		} else {
 			if (authList.is_self) {
 				homeButton.setText(getString(R.string.menu_home));
 			} else {
 				homeButton.setText(authList.name
 						+ getString(R.string.user_home));
 			}
 			homeButton.setVisibility(View.VISIBLE);
 			homeButton.setTag(authList.userId);
 			if (authList.is_self) {
 				logoutButton.setVisibility(View.VISIBLE);
 			} else {
 				logoutButton.setVisibility(View.GONE);
 			}
 
 			profileName.setText(authList.name);
 			if (authList.profile_image_path != null) {
 				Bitmap bm = BitmapFactory
 						.decodeFile(authList.profile_image_path);
 				BitmapDrawable avatar = new BitmapDrawable(
 						ProfileFragment.this.getResources(), bm);
 				avatar.setBounds(0, 0, avatar.getIntrinsicWidth(),
 						avatar.getIntrinsicHeight());
 				profileName.setCompoundDrawables(avatar, null, null, null);
 			}
 			if (authList.description != null
 					&& authList.description.length() > 0
 					&& !authList.description.equalsIgnoreCase("none")) {
 				profileDesc.setText(authList.description);
 			} else {
 				profileDesc.setVisibility(View.GONE);
 			}
 			for (Auth auth : authList.auth_list) {
 				switch (AuthService.getAuthService(auth.service_id)) {
 				case SINA:
 					setUpButtonText(sinaButton, auth);
 					break;
 				case QQ:
 					setUpButtonText(qqButton, auth);
 					break;
 
 				case DOUBAN:
 					setUpButtonText(doubanButton, auth);
 					break;
 				default:
 					setUpButtonText(fanfouButton, auth);
 					break;
 				}
 			}
 		}
 	}
 
 	class FetchUserTask extends UserFetchTask {
 		
 		private ProgressDialog dialog;
 		
 		@Override
 		public void onPreExecute() {
 			dialog = new ProgressDialog(getActivity());
 			dialog.setMessage(getString(R.string.loading));
 			dialog.show();
 		}
 		
 		@Override
 		public void updateConnection(HttpURLConnection connection) {
 			if (cookie != null) {
 				connection.setRequestProperty("Cookie", cookie);
 			}
 		}
 
 		@Override
 		public void updateAuthList(AuthList authList) {
 			if (authList.profile_image_url != null) {
 				authList.profile_image_path = ImageDownloader.downloadImage(
 						getActivity(), authList.profile_image_url, "/avatar/"
 								+ authList.userId);
 			}
 		}
 
 		@Override
 		public void onPostExecute(AuthList authList) {
 			if (authList == null) {
 				Toast.makeText(getActivity(),
 						getString(R.string.err_msg_cannot_connet),
 						Toast.LENGTH_SHORT).show();
 				return;
 			}
 			ProfileFragment.this.authList = authList;
 			setUpView();
 			dialog.dismiss();
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.logout:
 			new LogOutTask().execute();
 			break;
 		case R.id.homeButton:
 			if (authList != null) {
 				Intent listIntent = new Intent(getActivity(),
 						DocListActivity.class);
 				if (authList.is_self) {
 					listIntent.putExtra(DocListActivity.LIST_TYPE,
 							ListType.ME.toString());
 				} else {
 					listIntent.putExtra(DocListActivity.LIST_TYPE,
 							authList.userId);
 				}
 				startActivity(listIntent);
 			}
 			break;
 		case R.id.sinaButton:
 			onAuthButtonClick(sinaButton, AuthService.SINA);
 			break;
 		case R.id.qqButton:
 			onAuthButtonClick(qqButton, AuthService.QQ);
 			break;
 		case R.id.doubanButton:
 			onAuthButtonClick(doubanButton, AuthService.DOUBAN);
 			break;
 		case R.id.fanfouButton:
 			onAuthButtonClick(fanfouButton, AuthService.FANFOU);
 			break;
 		}
 	}
 
 	private void onAuthButtonClick(Button button, AuthService authService) {
 		Auth auth = (Auth) button.getTag();
 		if (auth == null) {
 			Intent authIntent = new Intent(getActivity(), AuthActivity.class);
 			authIntent.putExtra(AuthFragment.AUTH_SERVICE,
 					authService.toString());
 			authIntent.putExtra(AuthActivity.AUTH_SERVICE_NAME,
 					getString(authService.getServiceNameId()));
 			startActivity(authIntent);
 		} else if (!auth.isInstalled) {
 			if (authList.is_self) {
 				Intent authIntent = new Intent(getActivity(),
 						AuthActivity.class);
 				authIntent.putExtra(AuthFragment.AUTH_SERVICE,
 						authService.toString());
 				authIntent.putExtra(AuthActivity.AUTH_SERVICE_NAME, auth.name);
 				startActivity(authIntent);
 			}
 		} else {
 			Intent browserIntent = new Intent(Intent.ACTION_VIEW);
 			browserIntent.setData(Uri.parse(auth.service_profile_url));
 			startActivity(browserIntent);
 		}
 	}
 
 	class LogOutTask extends AsyncTask<Void, Void, Void> {
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			if (cookie == null) {
 				return null;
 			}
 			try {
 				URL url = new URL(LOGOUT_URL);
 				HttpURLConnection connection = (HttpURLConnection) url
 						.openConnection();
 				connection.setReadTimeout(5000);
 				connection.setRequestMethod("GET");
 				connection.setRequestProperty("Cookie", cookie);
 				connection.connect();
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(connection.getInputStream()));
 				reader.close();
 			} catch (MalformedURLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void unUsed) {
 			SharedPreferences prefs = PreferenceManager
 					.getDefaultSharedPreferences(getActivity()
 							.getApplicationContext());
 			prefs.edit().remove("cookie").commit();
 			Intent profileIntent = new Intent(getActivity(),
 					ProfileActivity.class);
 			startActivity(profileIntent);
 		}
 	}
 }
