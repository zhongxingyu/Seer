 package eu.fiveminutes.cipele46.fragment;
 
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 import eu.fiveminutes.cipele46.R;
 import eu.fiveminutes.cipele46.activity.UserSettingsActivity;
 import eu.fiveminutes.cipele46.activity.UserSettingsActivity.UserSettingsScreen;
 import eu.fiveminutes.cipele46.api.CipeleAPI;
 import eu.fiveminutes.cipele46.api.UserRegistrationListener;
 import eu.fiveminutes.cipele46.model.User;
 
 public class RegisterFragment extends SherlockFragment{
 
 	// UI references.
 	private EditText mTxtName;
 	private EditText mTxtEmail;
 	private EditText mTxtPhone;
 	private EditText mTxtPassword;
 	private EditText mTxtPasswordRepeated;
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View v = inflater.inflate(R.layout.register, container, false);
 		return v;
 	}
 	
 	@Override
 	public void onViewCreated(View view, Bundle savedInstanceState) {
 		super.onViewCreated(view, savedInstanceState);
 		
 		mTxtName = (EditText)view.findViewById(R.id.txtName);
 		mTxtEmail = (EditText)view.findViewById(R.id.txtEmail);
 		mTxtPhone = (EditText)view.findViewById(R.id.txtPhone);
 		mTxtPassword = (EditText)view.findViewById(R.id.txtPassword);
 		mTxtPasswordRepeated = (EditText)view.findViewById(R.id.txtPasswordRepeated);
 		
 		view.findViewById(R.id.btn_register).setOnClickListener(
 			new View.OnClickListener() {
 				@Override
 				public void onClick(View view) {
 					final String password = mTxtPassword.getText().toString();
 					String passwordRepeated = mTxtPasswordRepeated.getText().toString();
 					
 					if(password.equals(passwordRepeated) == false) {
 						return;
 					}
 					
 					final User user = collectUserData();
 					String[] firstLastName = user.getName().split(" ");
 					String firstName = "";
 					String lastName = "";
 					if(firstLastName.length > 0) {
 						firstName = firstLastName[0];
 					}
 					
 					if(firstLastName.length > 1) {
 						lastName = firstLastName[1];
 					}
 					
 					CipeleAPI.get().registerUser(firstName, lastName, user.getEmail(), user.getPhone(), password, 
 							new UserRegistrationListener() {
 								
 								@Override
 								public void onSuccess() {
 									user.setBasicAuth(CipeleAPI.basicAuthHeaderValue(user.getEmail(), password));
 									User.setUserAsActive(getActivity(), user);
 									startActivity(UserSettingsActivity.buildIntent(getActivity(), UserSettingsScreen.USER_DETAILS));
 								}
 								
 								@Override
								public void onFailure(Throwable t) {
									Toast.makeText(getActivity(), "There was an error during registration", Toast.LENGTH_LONG).show();
 								}
 							});
 				}
 			});
 	}
 	
 	public User collectUserData() {
 		User user = new User();
 		user.setName(mTxtName.getText().toString());
 		user.setEmail(mTxtEmail.getText().toString());
 		user.setPhone(mTxtPhone.getText().toString());
 		return user;
 	}
 }
