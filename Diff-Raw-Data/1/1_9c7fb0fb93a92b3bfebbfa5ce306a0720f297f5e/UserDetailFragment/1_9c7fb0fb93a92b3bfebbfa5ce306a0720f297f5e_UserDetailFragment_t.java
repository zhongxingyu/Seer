 
 package org.inftel.ssa.mobile.ui.fragments;
 
 import org.inftel.ssa.mobile.R;
 import org.inftel.ssa.mobile.provider.SsaContract.Users;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TabHost;
 import android.widget.TextView;
 
 public class UserDetailFragment extends Fragment implements LoaderCallbacks<Cursor> {
 
     protected final static String TAG = "UserDetailFragment";
 
     private static final String TAG_INFORMATION = "information";
     private static final String TAG_CONTACT = "Contact";
 
     protected Handler mHandler = new Handler();
     protected Activity mActivity;
     private Uri mContentUri;
     private String mUserId;
 
     String fullname;
     String nickname;
     String email;
     String project;
     String company;
     String number;
     String role;
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         mActivity = getActivity();
 
         if (mContentUri != null) {
             getLoaderManager().initLoader(0, null, this);
         } else {
             // New item (set default values)
         }
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.ssa_user_details, container, false);
 
         Bundle arguments = getArguments();
         // TODO buscar donde esta la constante _uri!
         if (arguments != null && arguments.get("_uri") != null) {
             mContentUri = (Uri) arguments.get("_uri");
         }
 
         setHasOptionsMenu(true);
 
         // Handle users click
         view.findViewById(R.id.user_btn_projects).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 startActivity(new Intent(Intent.ACTION_VIEW, Users.buildUserUri(mUserId)));
             }
         });
         // Handle tasks click
         view.findViewById(R.id.user_btn_tasks).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 startActivity(new Intent(Intent.ACTION_VIEW, Users.buildTasksDirUri(mUserId)));
             }
         });
 
         view.findViewById(R.id.number_layout).setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 String uri = "tel:" + number;
                 Intent intent = new Intent(Intent.ACTION_CALL);
                 intent.setData(Uri.parse(uri));
                 startActivity(intent);
 
             }
         });
 
         view.findViewById(R.id.email_layout).setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 String asunto = "Asunto";
                 String texto = "Algo de texto";
                 Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                 emailIntent.setType("plain/text");
                 emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, email);
                 emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, asunto);
                 emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, texto);
                 startActivity(Intent.createChooser(emailIntent, "Send your email in:"));
 
             }
         });
 
         TabHost tabHost = (TabHost) view.findViewById(android.R.id.tabhost);
         tabHost.setup();
         setupInformationTab(view);
         setupContactTab(view);
 
         return view;
     }
 
     @Override
     public void onResume() {
         super.onResume();
     }
 
     /**
      * {@inheritDoc} Query the {@link PlaceDetailsContentProvider} for the
      * Phone, Address, Rating, Reference, and Url of the selected venue. TODO
      * Expand the projection to include any other details you are recording in
      * the Place Detail Content Provider.
      */
     public Loader<Cursor> onCreateLoader(int id, Bundle args) {
         String[] projection = new String[] {
                 Users._ID,
                 Users.USER_FULLNAME, Users.USER_NICKNAME,
                 Users.USER_EMAIL, Users.USER_PROJECT_ID,
                 Users.USER_NUMBER,
                 Users.USER_COMPANY,
                 Users.USER_ROLE
         };
         return new CursorLoader(mActivity, mContentUri, projection, null, null, null);
     }
 
     /**
      * {@inheritDoc} When the Loader has completed, schedule an update of the
      * Fragment UI on the main application thread.
      */
     public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
         if (data.moveToFirst()) {
            mUserId = data.getString(data.getColumnIndex(Users._ID));
             fullname = data.getString(data.getColumnIndex(Users.USER_FULLNAME));
             nickname = data.getString(data.getColumnIndex(Users.USER_NICKNAME));
             email = data.getString(data.getColumnIndex(Users.USER_EMAIL));
             project = data.getString(data.getColumnIndex(Users.USER_PROJECT_ID));
             company = data.getString(data.getColumnIndex(Users.USER_COMPANY));
             number = data.getString(data.getColumnIndex(Users.USER_NUMBER));
             role = data.getString(data.getColumnIndex(Users.USER_ROLE));
             // Update UI
             mHandler.post(new Runnable() {
                 public void run() {
                     // Header
                     ((TextView) getView().findViewById(R.id.detail_title)).setText(fullname);
 
                     ((TextView) getView().findViewById(R.id.fullname)).setText(fullname);
                     ((TextView) getView().findViewById(R.id.nickname)).setText(nickname);
                     ((TextView) getView().findViewById(R.id.email)).setText(email);
                     ((TextView) getView().findViewById(R.id.project)).setText(project);
                     ((TextView) getView().findViewById(R.id.company)).setText(company);
                     ((TextView) getView().findViewById(R.id.number)).setText(number);
                     ((TextView) getView().findViewById(R.id.role)).setText(role);
                 }
             });
         }
     }
 
     private void setupContactTab(View view) {
         TabHost mTabHost = (TabHost) view.findViewById(android.R.id.tabhost);
         mTabHost.addTab(mTabHost.newTabSpec(TAG_CONTACT)
                 .setIndicator(buildIndicator(R.string.project_links, view))
                 .setContent(R.id.tab_user_contact));
     }
 
     private void setupInformationTab(View view) {
         TabHost mTabHost = (TabHost) view.findViewById(android.R.id.tabhost);
         mTabHost.addTab(mTabHost.newTabSpec(TAG_INFORMATION)
                 .setIndicator(buildIndicator(R.string.project_information, view))
                 .setContent(R.id.tab_user_information));
     }
 
     private View buildIndicator(int textRes, View view) {
         final TextView indicator = (TextView) getActivity().getLayoutInflater()
                 .inflate(R.layout.tab_indicator,
                         (ViewGroup) view.findViewById(android.R.id.tabs), false);
         indicator.setText(textRes);
         return indicator;
     }
 
     public void onLoaderReset(Loader<Cursor> loader) {
         mHandler.post(new Runnable() {
             public void run() {
                 // ((TextView)
                 // getView().findViewById(R.id.sprint_title)).setText("");
                 // ((TextView)
                 // getView().findViewById(R.id.sprint_subtitle)).setText("");
             }
         });
     }
 
 }
