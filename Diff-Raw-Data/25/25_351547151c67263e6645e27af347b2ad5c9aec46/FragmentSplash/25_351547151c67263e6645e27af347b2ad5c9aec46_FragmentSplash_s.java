 package com.jeffthefate.dmbquiz.fragment;
 
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnFocusChangeListener;
 import android.view.ViewGroup;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 import com.jeffthefate.dmbquiz.ApplicationEx;
 import com.jeffthefate.dmbquiz.R;
 import com.parse.FindCallback;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.ParseUser;
 
 public class FragmentSplash extends FragmentBase {
     
     private EditText loginUsername;
     private EditText loginPassword;
    private RelativeLayout emailButtonLayout;
     private LinearLayout buttonLayout;
     private TextView loginButton;
     private TextView signupButton;
     private TextView resetButton;
     private ImageView facebookLogin;
     private ImageView twitterLogin;
     private TextView playButton;
     private TextView emailButton;
     private LinearLayout emailLayout;
     
     private String username;
     private String password;
     
     private boolean isSignedUp = false;
     
     public FragmentSplash() {}
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         setRetainInstance(true);
         View view = inflater.inflate(R.layout.splash, container, false);
         loginUsername = (EditText) view.findViewById(R.id.LoginUsername);
         loginUsername.setOnEditorActionListener(new OnEditorActionListener() {
             @Override
             public boolean onEditorAction(TextView v, int actionId,
                     KeyEvent event) {
                 if (actionId == EditorInfo.IME_ACTION_NEXT) {
                     String username = v.getText().toString().trim();
                     checkSignedUp(username);
                 }
                 return false;
             }
         });
         loginUsername.setOnFocusChangeListener(new OnFocusChangeListener() {
             @Override
             public void onFocusChange(View v, boolean hasFocus) {
                 if (!hasFocus) {
                     String username = ((TextView)v).getText().toString().trim();
                     checkSignedUp(username);
                 }
             } 
         });
         loginPassword = (EditText) view.findViewById(R.id.LoginPassword);
         loginPassword.setOnEditorActionListener(new OnEditorActionListener() {
             @Override
             public boolean onEditorAction(TextView v, int actionId,
                     KeyEvent event) {
                 if (actionId == EditorInfo.IME_ACTION_DONE) {
                     InputMethodManager imm = 
                         (InputMethodManager) getActivity().getSystemService(
                                 Context.INPUT_METHOD_SERVICE);
                     imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                     processLogin(!isSignedUp);
                     return true;
                 }
                 else
                     return false;
             } 
         });
         emailButtonLayout = 
                (RelativeLayout) view.findViewById(R.id.EmailButtonLayout);
         buttonLayout = (LinearLayout) view.findViewById(R.id.ButtonLayout);
         loginButton = (TextView) view.findViewById(R.id.LoginButton);
         loginButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 InputMethodManager imm = 
                     (InputMethodManager) getActivity().getSystemService(
                             Context.INPUT_METHOD_SERVICE);
                 imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                 processLogin(false);
             } 
         });
         signupButton = (TextView) view.findViewById(R.id.SignupButton);
         signupButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 InputMethodManager imm = 
                     (InputMethodManager) getActivity().getSystemService(
                             Context.INPUT_METHOD_SERVICE);
                 imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                 processLogin(true);
             } 
         });
         resetButton = (TextView) view.findViewById(R.id.ResetButton);
         resetButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 InputMethodManager imm = 
                     (InputMethodManager) getActivity().getSystemService(
                             Context.INPUT_METHOD_SERVICE);
                 imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                 if (mCallback != null)
                 	mCallback.resetPassword(
                 			loginUsername.getText().toString().trim());
             } 
         });
         facebookLogin = (ImageView) view.findViewById(R.id.FacebookLoginButton);
         facebookLogin.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (mCallback != null)
                     mCallback.onLoginPressed(FragmentBase.LOGIN_FACEBOOK, null,
                             null);
             }
         });
         twitterLogin = (ImageView) view.findViewById(R.id.TwitterLoginButton);
         twitterLogin.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (mCallback != null)
                     mCallback.onLoginPressed(FragmentBase.LOGIN_TWITTER, null,
                             null);
             }
         });
         playButton = (TextView) view.findViewById(R.id.JustPlayButton);
         playButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 if (mCallback != null)
                     mCallback.onLoginPressed(FragmentBase.LOGIN_ANON, null,
                             null);
             } 
         });
         emailLayout = (LinearLayout) view.findViewById(R.id.EmailLayout);
         emailButton = (TextView) view.findViewById(R.id.EmailButton);
         emailButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 playButton.clearAnimation();
                 playButton.setVisibility(View.INVISIBLE);
                 emailButton.clearAnimation();
                 emailButton.setVisibility(View.INVISIBLE);
                 emailLayout.setVisibility(View.VISIBLE);
                 if (buttonLayout != null)
                     buttonLayout.setVisibility(View.GONE);
             } 
         });
         return view;
     }
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         if (!sharedPrefs.contains(getString(R.string.notification_key)))
             sharedPrefs.edit().putBoolean(getString(R.string.notification_key),
                     true).commit();
         setHasOptionsMenu(true);
     }
     
     private void processLogin(boolean signUp) {
         username = loginUsername.getText().toString().trim();
         password = loginPassword.getText().toString().trim();
         if (loginUsername.getText().length() > 0 &&
                 username.matches(
                         "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?" +
                         "^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+" +
                         "[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")) {
             if (loginPassword.getText().length() >= 4) {
                 if (signUp) {
                     if (mCallback != null)
                         mCallback.onLoginPressed(FragmentBase.SIGNUP_EMAIL,
                                 username, password);
                 }
                 else {
                     if (mCallback != null)
                         mCallback.onLoginPressed(FragmentBase.LOGIN_EMAIL,
                                 username, password);
                 }
             }
             else {
                 ApplicationEx.mToast.setText(
                         "Password must be at least 4 characters");
                 ApplicationEx.mToast.show();
             }
         }
         else {
             ApplicationEx.mToast.setText("Enter valid email");
             ApplicationEx.mToast.show();
         }
     }
     /*
     private TranslateAnimation createAnimation(long offset, boolean right) {
         int xDest = dm.widthPixels;
         if (!right)
             xDest = xDest*-1;
         TranslateAnimation anim = new TranslateAnimation(xDest, 0, 0, 0);
         anim.setDuration(800);
         anim.setFillAfter(true);
         anim.setStartOffset(offset);
         return anim;
     }
     
     private TranslateAnimation createUnAnimation(boolean right) {
         int xDest = dm.widthPixels;
         if (!right)
             xDest = xDest*-1;
         TranslateAnimation anim = new TranslateAnimation(0, xDest, 0 , 0);
         anim.setDuration(0);
         anim.setFillAfter(true);
         return anim;
     }
     */
     private void checkSignedUp(String username) {
         ParseQuery query = ParseUser.getQuery();
        query.whereContains("username", username);
         query.findInBackground(new FindCallback() {
             @Override
             public void done(List<ParseObject> userList,
                     ParseException e) {
                 if (e == null) {
                     if (emailButtonLayout != null)
                         emailButtonLayout.setVisibility(View.VISIBLE);
                     if (userList.isEmpty()) {
                         loginButton.setVisibility(View.GONE);
                         signupButton.setVisibility(View.VISIBLE);
                         resetButton.setVisibility(View.GONE);
                         isSignedUp = false;
                     }
                     else {
                         signupButton.setVisibility(View.GONE);
                         loginButton.setVisibility(View.VISIBLE);
                         resetButton.setVisibility(View.VISIBLE);
                         isSignedUp = true;
                     }
                 }
             } 
         });
     }
     
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         inflater.inflate(R.menu.menu_splash, menu);
     }
     
     @Override
     public void onPrepareOptionsMenu(Menu menu) {
         menu.findItem(R.id.Notifications)
             .setCheckable(true)
             .setChecked(sharedPrefs.getBoolean(
                     getString(R.string.notification_key), true));
         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
             if (sharedPrefs.getBoolean(
                     getString(R.string.notification_key), true))
                 menu.findItem(R.id.Notifications).setTitle(
                 "\u2714  Notifications");
             else
                 menu.findItem(R.id.Notifications).setTitle("Notifications");
         }
         super.onPrepareOptionsMenu(menu);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch(item.getItemId()) {
         case R.id.SwitchBackground:
             if (mCallback != null)
                 mCallback.setBackground(mCallback.getBackground(), true);
             break;
         case R.id.Notifications:
             sharedPrefs.edit().putBoolean(getString(R.string.notification_key),
                     !sharedPrefs.getBoolean(
                             getString(R.string.notification_key), true))
                 .commit();
             break;
         case R.id.ExitMenu:
             getActivity().moveTaskToBack(true);
             break;
         case R.id.InfoMenu:
             if (mCallback != null)
                 mCallback.onInfoPressed();
             break;
         case R.id.FollowMenu:
             Uri uri = Uri.parse("http://www.twitter.com/dmbtrivia");
             Intent intent = new Intent(Intent.ACTION_VIEW, uri);
             startActivity(intent);
             break;
         case R.id.LikeMenu:
             startActivity(getOpenFacebookIntent(ApplicationEx.getApp()));
             break;
         default:
             super.onOptionsItemSelected(item);
             break;
         }
         return true;
     }
     
     public static Intent getOpenFacebookIntent(Context context) {
 
         try {
             context.getPackageManager().getPackageInfo("com.facebook.katana",
                     0);
             return new Intent(Intent.ACTION_VIEW,
                     Uri.parse("fb://profile/401123586629428"));
         } catch (Exception e) {
             return new Intent(Intent.ACTION_VIEW,
                     Uri.parse("https://www.facebook.com/DMBTrivia"));
         }
     }
     
 }
