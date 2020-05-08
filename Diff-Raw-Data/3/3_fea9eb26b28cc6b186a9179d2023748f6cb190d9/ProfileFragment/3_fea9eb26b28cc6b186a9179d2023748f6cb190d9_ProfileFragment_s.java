 package com.nurun.activemtl.ui.fragment;
 
 import java.net.URL;
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.view.PagerTabStrip;
 import android.support.v4.view.ViewPager;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.facebook.LoggingBehavior;
 import com.facebook.Request;
 import com.facebook.Response;
 import com.facebook.Session;
 import com.facebook.SessionState;
 import com.facebook.Settings;
 import com.facebook.model.GraphUser;
 import com.nurun.activemtl.ActiveMtlConfiguration;
 import com.nurun.activemtl.PreferenceHelper;
 import com.nurun.activemtl.R;
 import com.nurun.activemtl.ui.view.StreamDrawable;
 
 public class ProfileFragment extends Fragment {
 
     public enum Area {
         ME, DISTRICT, MONTREAL
     }
 
     private Session.StatusCallback statusCallback = new SessionStatusCallback();
     private View loginButton;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         initFacebook(savedInstanceState);
         View view = inflater.inflate(R.layout.profile, container, false);
         initFormLogin(view);
         initViewPager(view);
         initLogoutButton(view);
         return view;
     }
 
     private void initLogoutButton(View view) {
         view.findViewById(R.id.buttonDisconect).setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 
             }
         });
     }
 
     private void initFormLogin(View view) {
         loginButton = view.findViewById(R.id.sign_in_button);
         loginButton.setVisibility(View.VISIBLE);
         loginButton.setOnClickListener(onClickListener);
     }
 
     private OnClickListener onClickListener = new OnClickListener() {
         @Override
         public void onClick(View view) {
             Session session = Session.getActiveSession();
             switch (view.getId()) {
             case R.id.sign_in_button:
                 if (!session.isOpened() && !session.isClosed()) {
                     session.openForRead(new Session.OpenRequest(ProfileFragment.this).setCallback(statusCallback));
                 } else {
                     Session.openActiveSession(getActivity(), ProfileFragment.this, true, statusCallback);
                 }
                 break;
             case R.id.buttonDisconect:
                 if (!session.isClosed()) {
                     session.closeAndClearTokenInformation();
                 }
                 PreferenceHelper.clearUserInfos(getActivity());
             default:
                 break;
             }
             
         }
     };
 
     private void initViewPager(View view) {
         PagerTabStrip pagerTabStrip = (PagerTabStrip) view.findViewById(R.id.pagerTabStrip);
         pagerTabStrip.setTabIndicatorColorResource(R.color.background);
         pagerTabStrip.setTextColor(getResources().getColor(R.color.background));
         ViewPager viewPager = (ViewPager) view.findViewById(R.id.profile_pager);
         viewPager.setAdapter(new ProfilePagerAdapter(getFragmentManager()));
     }
 
     private void initFacebook(Bundle savedInstanceState) {
         Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
         Session session = Session.getActiveSession();
         if (session == null) {
             if (savedInstanceState != null) {
                 session = Session.restoreSession(getActivity(), null, statusCallback, savedInstanceState);
             }
             if (session == null) {
                 session = new Session(getActivity());
             }
             Session.setActiveSession(session);
             if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
                 session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
             }
         }
     }
 
     @Override
     public void onStart() {
         super.onStart();
         Session.getActiveSession().addCallback(statusCallback);
         updateView();
     }
 
     @Override
     public void onStop() {
         super.onStop();
         Session.getActiveSession().removeCallback(statusCallback);
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         Session.getActiveSession().onActivityResult(getActivity(), requestCode, resultCode, data);
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         Session session = Session.getActiveSession();
         Session.saveSession(session, outState);
     }
 
     private void updateView() {
         Session session = Session.getActiveSession();
         if (session.isOpened()) {
             showProfile(true);
 
             String userName = PreferenceHelper.getUserName(getActivity());
             if (TextUtils.isEmpty(userName)) {
                 // make request to the /me API
                 Request.newMeRequest(session, new Request.GraphUserCallback() {
                     // callback after Graph API response with user object
                     @Override
                     public void onCompleted(GraphUser user, Response response) {
                         if (user != null) {
                             PreferenceHelper.setUserId(getActivity(), user.getId());
                             PreferenceHelper.setUserName(getActivity(), user.getName());
                            new UserProfilePictureTask().execute();
                             ((TextView) getView().findViewById(R.id.userName)).setText(user.getName());
                         }
                     }
                 }).executeAsync();
             } else {
                 ((TextView) getView().findViewById(R.id.userName)).setText(userName);
             }
         } else {
             showProfile(false);
         }
 
     }
 
     public static Fragment newFragment() {
         return new ProfileFragment();
     }
 
     public class ProfilePagerAdapter extends FragmentStatePagerAdapter {
 
         private Fragment meFragment;
         private Fragment cityFragment;
         private Fragment districtFragment;
 
         public ProfilePagerAdapter(FragmentManager fm) {
             super(fm);
             meFragment = StatFragment.newInstance(Area.ME);
             districtFragment = StatFragment.newInstance(Area.DISTRICT);
             cityFragment = StatFragment.newInstance(Area.MONTREAL);
         }
 
         @Override
         public int getCount() {
             return 3;
         }
 
         @Override
         public Fragment getItem(int position) {
             switch (position) {
             case 0:
                 return meFragment;
             case 1:
                 return districtFragment;
             case 2:
                 return cityFragment;
             }
             throw new IllegalStateException("Mauvais id d'onglet");
         }
 
         @Override
         public CharSequence getPageTitle(int position) {
             switch (position) {
             case 0:
                 return getString(R.string.me);
             case 1:
                 return getString(R.string.district);
             case 2:
                 return getString(R.string.montreal);
             }
             throw new IllegalStateException("Mauvais id d'onglet");
         }
     }
 
     class UserProfilePictureTask extends AsyncTask<Void, Void, StreamDrawable> {
 
         @Override
         protected StreamDrawable doInBackground(Void... params) {
             try {
                 URL profPict = new URL(ActiveMtlConfiguration.getInstance(getActivity()).getProfileUrl(getActivity()));
                 Bitmap mBitmap = BitmapFactory.decodeStream(profPict.openStream());
                 return new StreamDrawable(mBitmap);
             } catch (Exception e) {
                 Log.e(getClass().getSimpleName(), e.getMessage(), e);
                 Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ali_g);
                 return new StreamDrawable(mBitmap);
             }
         }
 
         @Override
         protected void onPostExecute(StreamDrawable streamDrawable) {
             ((ImageView) getView().findViewById(R.id.imageProfile)).setImageDrawable(streamDrawable);
         }
     };
 
     @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
     public void showProfile(Boolean success) {
         final View mProfileView = getView().findViewById(R.id.profileView);
         // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
         // for very easy animations. If available, use these APIs to fade-in
         // the progress spinner.
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
             int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
 
             if (success) {
                 loginButton.setVisibility(View.GONE);
                 mProfileView.setVisibility(View.VISIBLE);
                 mProfileView.animate().setDuration(shortAnimTime).alpha(1).setListener(new AnimatorListenerAdapter() {
                     @Override
                     public void onAnimationEnd(Animator animation) {
                         mProfileView.setVisibility(View.VISIBLE);
                     }
                 });
             } else {
                 mProfileView.setVisibility(View.GONE);
                 loginButton.setVisibility(View.VISIBLE);
                 loginButton.animate().setDuration(shortAnimTime).alpha(1).setListener(new AnimatorListenerAdapter() {
                     @Override
                     public void onAnimationEnd(Animator animation) {
                         loginButton.setVisibility(View.VISIBLE);
                     }
                 });
             }
         } else {
             // The ViewPropertyAnimator APIs are not available, so simply show
             // and hide the relevant UI components.
             loginButton.setVisibility(View.GONE);
             if (success) {
                 mProfileView.setVisibility(View.VISIBLE);
                 loginButton.setVisibility(View.GONE);
             } else {
                 mProfileView.setVisibility(View.GONE);
                 loginButton.setVisibility(View.VISIBLE);
             }
         }
     }
 
     private class SessionStatusCallback implements Session.StatusCallback {
         @Override
         public void call(Session session, SessionState state, Exception exception) {
             updateView();
         }
     }
 }
