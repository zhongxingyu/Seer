 package il.ac.huji.chores;
 
 import android.app.Fragment;
 import android.content.Context;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.telephony.TelephonyManager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import com.parse.ParseException;
 import il.ac.huji.chores.dal.RoommateDAL;
 import il.ac.huji.chores.exceptions.UserNotLoggedInException;
 
 public class LoginFragment extends Fragment {
 
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		View view = inflater.inflate(R.layout.fragment_login, container, false);
 		return view;
 	}
 	
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 		final boolean[] isLogin = {true}; //it's an array just so the value can change in the olClickListener inner function.
 		
 		
 		Button loginButton = (Button)((getActivity().findViewById(R.id.loginFragment_loginButton)));
 		loginButton.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
 
                 boolean err = false;
 
                 TextView msg = (TextView) (getActivity().findViewById(R.id.LoginFragment_badPasswordMsg));//error message
                 msg.setVisibility(View.INVISIBLE);
                 msg.setEnabled(false);
 
                 EditText usernameTxt = (EditText) (getActivity().findViewById(R.id.LoginFragment_username));
                 String username = usernameTxt.getText().toString();
 
                 EditText passwordTxt = (EditText) (getActivity().findViewById(R.id.LoginFragment_password));
                 String password = passwordTxt.getText().toString();
 
                 String verifyPassword;
                 if (!isLogin[0]) {
                     EditText passwordVerifyTxt = (EditText) (getActivity().findViewById(R.id.LoginFragment_password_verify));
                     verifyPassword = passwordVerifyTxt.getText().toString();
                     if (!password.equals(verifyPassword)) {
                         msg.setVisibility(View.VISIBLE);
                         msg.setEnabled(true);
                         return;
                     }
                     String phoneNumber = ((EditText) getActivity().findViewById(R.id.loginFragmentPhoneEdit))
                             .getText().toString();
 
                     try {
                         RoommateDAL.createRoommateUser(username, password, phoneNumber);
                     } catch (ParseException e) {
                         //unsuccessful sign-up
                         err = true;
                         msg.setVisibility(View.VISIBLE);
                         msg.setEnabled(true);
 
                         if (e.getCode() == ParseException.USERNAME_TAKEN) {
                             msg.setText(getResources().getString(R.string.login_signup_username_exist_msg));
                         } else if (e.getCode() == ParseException.CONNECTION_FAILED) {
                             msg.setText(getResources().getString(R.string.chores_connection_failed));
                         } else {
                             msg.setText(getResources().getString(R.string.login_bad_signup_msg));
                         }
                     } catch (UserNotLoggedInException e) {
                         // TODO Auto-generated catch block
                         e.printStackTrace();
                     }
                 } else {
                     try {
                         RoommateDAL.Login(username, password);
                     } catch (ParseException e) {
                         //unsuccessful login
                         err = true;
                         msg.setVisibility(View.VISIBLE);
                         msg.setEnabled(true);
 
                         if (e.getCode() == ParseException.CONNECTION_FAILED) {
                             msg.setText(getResources().getString(R.string.chores_connection_failed));
                         } else {
                             msg.setText(getResources().getString(R.string.login_bad_login_msg));//any other login failure
                         }
 
 
                     }
                 }
 
                 if (!err) {
                     getActivity().finish();
                 }
             }
         });
 		
 		
 
 		Button changePurposeButton = (Button)((getActivity().findViewById(R.id.LoginFragment_changeScreenPurposeButton)));
 		changePurposeButton.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
 
                 TextView msg = (TextView) (getActivity().findViewById(R.id.LoginFragment_badPasswordMsg));//error message
                 msg.setVisibility(View.INVISIBLE);
                 msg.setEnabled(false);
 
                 TextView verifyPwTxt = (TextView) (getActivity().findViewById(R.id.LoginFragment_password_verify_text));
                 TextView verifyPwButton = (TextView) (getActivity().findViewById(R.id.LoginFragment_password_verify));
                 Button button = (Button) (getActivity().findViewById(R.id.loginFragment_loginButton));
                 TextView changePurposeTxt = (TextView) (getActivity().findViewById(R.id.LoginFragment_changeScreenPurposeText));
                 Button changePurposeButton = (Button) (getActivity().findViewById(R.id.LoginFragment_changeScreenPurposeButton));
                 TextView enterPhoneMsg = (TextView) getActivity().findViewById(R.id.loginFragmentPhoneMsg);
                 EditText phoneEdit = (EditText) getActivity().findViewById(R.id.loginFragmentPhoneEdit);
 
                 if (isLogin[0] == true) {
                     //turn to sign-up
 
                     verifyPwTxt.setVisibility(View.VISIBLE);
                     verifyPwTxt.setEnabled(true);
 
                     verifyPwButton.setVisibility(View.VISIBLE);
                     verifyPwButton.setEnabled(true);
 
                     enterPhoneMsg.setVisibility(View.VISIBLE);
                    phoneEdit.setEnabled(true);
                    
                     phoneEdit.setVisibility(View.VISIBLE);
                    phoneEdit.setEnabled(true);
                    
                     String userPhoneNumber = findUserPhoneNumber();
                     // TODO add permissions!
                     phoneEdit.setText(userPhoneNumber != null ? userPhoneNumber : "");
 
                     button.setText(getResources().getString(R.string.login_signup_button));
 
                     changePurposeTxt.setText(getResources().getString(R.string.login_want_login_text));//turns to switch to login
 
                     changePurposeButton.setText(getResources().getString(R.string.login_login_button));
 
                     isLogin[0] = false;
                 } else {
                     verifyPwTxt.setVisibility(View.INVISIBLE);
                     verifyPwTxt.setEnabled(false);
 
                     verifyPwButton.setVisibility(View.INVISIBLE);
                     verifyPwButton.setEnabled(false);
                    
                    phoneEdit.setVisibility(View.INVISIBLE);
                    phoneEdit.setEnabled(false);
                    
                    enterPhoneMsg.setVisibility(View.INVISIBLE);
                    enterPhoneMsg.setEnabled(false);
 
                     button.setText(getResources().getString(R.string.login_login_button));
 
                     changePurposeTxt.setText(getResources().getString(R.string.login_want_signup_text));
 
                     changePurposeButton.setText(getResources().getString(R.string.login_signup_button));
 
                     isLogin[0] = true;
                 }
 
             }
 
         });
 	}
 
     private String findUserPhoneNumber() {
         String[] projection = new String[]{
                 ContactsContract.Profile._ID,
                 ContactsContract.Profile.DISPLAY_NAME_PRIMARY,
                 ContactsContract.Profile.LOOKUP_KEY,
                 ContactsContract.Profile.HAS_PHONE_NUMBER
         };
         TelephonyManager telephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
         String number = telephonyManager.getLine1Number();
         return number;
     }
 }
