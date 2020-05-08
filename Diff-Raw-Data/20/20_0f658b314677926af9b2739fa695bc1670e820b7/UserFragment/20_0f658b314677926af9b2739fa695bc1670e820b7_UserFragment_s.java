 package com.task;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentManager;
 import android.text.InputType;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.DatePicker;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.facebook.*;
 import com.facebook.model.GraphUser;
 import com.facebook.widget.LoginButton;
 import com.facebook.widget.ProfilePictureView;
 import com.task.db.UserDbHelper;
 import com.task.dialog.DatePickerDialog;
 import com.task.dialog.EditFieldDialog;
 import com.task.dialog.EditTwoFieldsDialog;
 import com.task.dialog.IntroductionDialog;
 
 import java.util.Calendar;
 
 import static com.task.Utils.*;
 
 /**
  * @author Leus Artem
  * @since 17.06.13
  */
 public class UserFragment extends SherlockFragment {
 
     private static final String TAG = "UserFragment";
 
     private UiLifecycleHelper uiHelper;
     private TextView nameView, surnameView, birthdateView, bioView, emailView;
     private ProfilePictureView profilePictureView;
     private ImageView defaultProfilePic;
     private  TestApplication app;
     LoginButton authButton;
    private boolean firstFBLogin = true;
 
     private Session.StatusCallback callback = new Session.StatusCallback() {
         @Override
         public void call(Session session, SessionState state, Exception exception) {
             onSessionStateChange(session, state, exception);
         }
     };
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setRetainInstance(true);
         uiHelper = new UiLifecycleHelper(this.getActivity(), callback);
         uiHelper.onCreate(savedInstanceState);
 
         app = (TestApplication) getActivity().getApplicationContext();
     }
 
 
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.user_fragment, container, false);
         authButton = (LoginButton) view.findViewById(R.id.authButton);
         authButton.setReadPermissions(Constants.FB_PERMISSIONS);
         authButton.setFragment(this);
         if(isFbAuthenticated()){
             authButton.setVisibility(View.VISIBLE);
         }
         createButtonsListeners(view);
         return view;
     }
 
     private void createButtonsListeners(View rootView){
         final UserDbHelper.UserEntity iAm = app.getMyself();
 
         ImageView editNameSurnameBtn = (ImageView) rootView.findViewById(R.id.editNameAndSurname);
         ImageView editBirthdayBtn = (ImageView) rootView.findViewById(R.id.editBirthdate);
         ImageView editBioBtn = (ImageView) rootView.findViewById(R.id.editBio);
         ImageView editEmailBtn = (ImageView) rootView.findViewById(R.id.editEmail);
 
         final Runnable onUpdateUiRunnable = new Runnable() {
             @Override
             public void run() {
                 updateUserUI();
             }
         };
 
         final FragmentManager fm = getFragmentManager();
 
         editNameSurnameBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 new EditTwoFieldsDialog(iAm.name, iAm.surname, "Edit", new EditTwoFieldsDialog.OnDataChangedLitener() {
                     @Override
                     public void onChanged(String val1, String val2) {
                         iAm.name = val1;
                         iAm.surname = val2;
                         app.saveMyself(onUpdateUiRunnable);
                     }
                 }).show(fm, "editName");
             }
         });
 
         editBirthdayBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 new DatePickerDialog(iAm.birthdate, new android.app.DatePickerDialog.OnDateSetListener() {
                     @Override
                     public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                         Calendar c = Calendar.getInstance();
                         c.set(year, monthOfYear, dayOfMonth);
                         iAm.birthdate = c.getTime();
                         app.saveMyself(onUpdateUiRunnable);
                     }
                 }).show(fm, "editBirth");
             }
         });
 
         editBioBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 new EditFieldDialog(iAm.bio, "Edit your bio", InputType.TYPE_CLASS_TEXT
                         | InputType.TYPE_TEXT_FLAG_MULTI_LINE, new EditFieldDialog.OnDataChangedLitener() {
                     @Override
                     public void onChanged(String val1) {
                         iAm.bio = val1;
                         app.saveMyself(onUpdateUiRunnable);
                     }
                 }).show(fm, "editBio");
             }
         });
 
         editEmailBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 new EditFieldDialog(iAm.email, "Edit your email", InputType.TYPE_CLASS_TEXT
                         | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS, new EditFieldDialog.OnDataChangedLitener() {
                     @Override
                     public void onChanged(String val1) {
                         if(!Utils.isValidEmail(val1)){
                             Toast.makeText(getActivity(), "Not valid email address", Toast.LENGTH_SHORT).show();
                         } else {
                             iAm.email = val1;
                             app.saveMyself(onUpdateUiRunnable);
                         }
                     }
                 }).show(fm, "editMail");
             }
         });
     }
 
     @Override
     public void onViewCreated(View view, Bundle savedInstanceState) {
         super.onViewCreated(view, savedInstanceState);
         nameView = (TextView) view.findViewById(R.id.name);
         surnameView = (TextView) view.findViewById(R.id.surname);
         birthdateView = (TextView) view.findViewById(R.id.birthdate);
         bioView = (TextView) view.findViewById(R.id.bio);
         emailView = (TextView) view.findViewById(R.id.email);
 
         profilePictureView = (ProfilePictureView) view.findViewById(R.id.profile_pic);
         defaultProfilePic = (ImageView) view.findViewById(R.id.default_user_pic);
         updateUserUI();
     }
 
     private void updateUserUI(){
         UserDbHelper.UserEntity iAm = app.getMyself();
         if(iAm == null) return; // test case
         if(iAm.fbId != null){    // user is already synchronized with FB so we can use his FB photo
             profilePictureView.setProfileId(iAm.fbId);
             profilePictureView.setVisibility(View.VISIBLE);
             defaultProfilePic.setVisibility(View.GONE);
         }
         if(iAm.name != null) nameView.setText(iAm.name);
         if(iAm.surname != null) surnameView.setText(iAm.surname);
         if(iAm.birthdate != null) birthdateView.setText(Utils.convertDateToString(iAm.birthdate));
         /*if(iAm.bio != null)*/ bioView.setText(iAm.bio != null ? iAm.bio : "You have no bio");
         /*if(iAm.email != null) */emailView.setText(iAm.email != null ? iAm.email : "You have no email");
     }
 
     private void syncWithFb(Session session, final Runnable onCompleteRunnable){
         if (!Utils.isOnline(getActivity())) {
             Toast.makeText(getActivity(), "No internet connection!", Toast.LENGTH_SHORT);
             return;
         }
         Bundle params = new Bundle();
         params.putString("fields", "first_name,last_name,birthday,bio,email");
         new Request(session, "me", params, HttpMethod.GET, new Request.Callback() {
             @Override
             public void onCompleted(Response response) {
                 GraphUser fbUser = response.getGraphObjectAs(GraphUser.class);
 
                 UserDbHelper.UserEntity iAm = app.getMyself();
                 iAm.name = fbUser.getFirstName();
                 iAm.surname = fbUser.getLastName();
                 iAm.birthdate = Utils.convertStringToDate(fbUser.getBirthday());
                 iAm.bio = (String) fbUser.getProperty("bio");
                 iAm.email = (String) fbUser.getProperty("email");
                 iAm.fbId = fbUser.getId();
 
                 UserDbHelper userDb = new UserDbHelper(getActivity().getBaseContext());
                 userDb.update(iAm, new Runnable() {
                     @Override
                     public void run() {
                         onCompleteRunnable.run();
                     }
                 });
                 userDb.close();
             }
         }).executeAsync();
     }
 
     private void onSessionStateChange(Session session, SessionState state, Exception exception) {
         if (state.isOpened()) {
             Log.i(TAG, "Logged in...");
            if(firstFBLogin){
                firstFBLogin = false;
                 syncWithFb(Session.getActiveSession(), new Runnable() {
                     @Override
                     public void run() {
                         Log.i(TAG, "synchronized with FB");
                         updateUserUI();
                     }
                 });
             }
             authButton.setVisibility(View.VISIBLE);
         } else if (state.isClosed()) {
             if(authButton.getVisibility() == View.VISIBLE){ // case when user pressed "Logout" button
                 getActivity().finish();
                 authButton.setVisibility(View.GONE);
             }
             Log.i(TAG, "Logged out...");
         }
     }
 
    public boolean isSessionOpened(){
        return Session.getActiveSession() == null ? false : Session.getActiveSession().isOpened();
    }

     @Override
     public void onResume() {
         super.onResume();
 //        updateUserUI();
         // For scenarios where the main activity is launched and user
         // session is not null, the session state change notification
         // may not be triggered. Trigger it if it's open/closed.
         Session session = Session.getActiveSession();
         if (session != null &&
                 (session.isOpened() || session.isClosed()) ) {
             onSessionStateChange(session, session.getState(), null);
         }
         uiHelper.onResume();
 
         // check connection availability
         if (!Utils.isOnline(getActivity())) {
             Toast.makeText(getActivity(), "No internet connection!", Toast.LENGTH_SHORT);
         } else {
             // show dialog in case user isn't logged in FB
             if (!isFbAuthenticated()) {
                 FragmentManager fm = getChildFragmentManager();
                 DialogFragment dialog = new IntroductionDialog();
                 dialog.show(fm, "dialog");
             }
         }
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         uiHelper.onActivityResult(requestCode, resultCode, data);
     }
 
     @Override
     public void onPause() {
         super.onPause();
         app.saveMyself(null);
         uiHelper.onPause();
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         uiHelper.onDestroy();
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         uiHelper.onSaveInstanceState(outState);
     }
 }
