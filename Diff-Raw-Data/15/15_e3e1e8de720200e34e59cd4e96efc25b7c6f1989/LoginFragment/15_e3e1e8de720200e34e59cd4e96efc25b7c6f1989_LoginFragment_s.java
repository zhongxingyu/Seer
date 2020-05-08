 package com.jcertif.android.fragments;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.IntentSender.SendIntentException;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
 import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
 import com.google.android.gms.common.Scopes;
 import com.google.android.gms.common.SignInButton;
 import com.google.android.gms.internal.bt;
 import com.google.android.gms.plus.PlusClient;
 import com.google.android.gms.plus.PlusClient.OnAccessRevokedListener;
 import com.google.android.gms.plus.model.people.Person;
 import com.google.android.gms.plus.model.people.Person.Emails;
 import com.google.gson.Gson;
 import com.jcertif.android.JcertifApplication;
 import com.jcertif.android.R;
 import com.jcertif.android.fragments.RegistrationFormFragment.OnUserDialogReturns;
 import com.jcertif.android.model.Participant;
 import com.jcertif.android.service.RESTService;
 
 
 public class LoginFragment extends RESTResponderFragment implements
 		ConnectionCallbacks, OnConnectionFailedListener,
 		OnAccessRevokedListener, OnClickListener,
 		PlusClient.OnPersonLoadedListener,OnUserDialogReturns {
 
 	private static final int REQUEST_CODE_RESOLVE_ERR = 9000;
 	private static final String TAG = "Login Fragment";
 	private static final String REGISTER_URI = JcertifApplication.BASE_URL+"/participant/register";
 
 	PlusClient mPlusClient;
 	private ConnectionResult mConnectionResult;
 	private ProgressDialog mConnectionProgressDialog;
 	private SignInButton mPlusOneButton;
 	private EditText et_email;
 	private EditText et_password;
 	private Participant user;
 	private Button btn_registerButon;
 
 	
 	 OnSignedInListener mSignedCallback;
 
 	    // Container Activity must implement this interface
 	    public interface OnSignedInListener {
 	        public void onSignedIn(Participant user);
 	    }
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		setRetainInstance(true);
 		View rootView = inflater.inflate(R.layout.fragment_login, container,
 				false);
 		getActivity().setTitle(getString(R.string.login));
 		mPlusOneButton = (SignInButton) rootView
 				.findViewById(R.id.sign_in_button);
 
 		mPlusOneButton.setOnClickListener(this);
 		et_email = (EditText) rootView.findViewById(R.id.et_email);
 		et_password = (EditText) rootView.findViewById(R.id.et_password);
 		btn_registerButon=(Button)rootView.findViewById(R.id.btn_register2);
 		btn_registerButon.setOnClickListener(this);
 		return rootView;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onActivityCreated(savedInstanceState);
 		 
 		
 		getSherlockActivity().getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
 
 		mPlusClient = new PlusClient.Builder(this.getActivity()
 				.getApplicationContext(), this, this)
 				.setVisibleActivities("http://schemas.google.com/AddActivity",
 						"http://schemas.google.com/BuyActivity")
 				.setScopes(Scopes.PLUS_LOGIN,Scopes.PLUS_PROFILE).build();
 
 		mConnectionProgressDialog = new ProgressDialog(this.getActivity());
 		mConnectionProgressDialog.setTitle("Login");
 		// mConnectionProgressDialog.setCancelable(false);
 		mConnectionProgressDialog.setMessage("Google Login...");
 	}
 
 	
 	
 	 @Override
 	    public void onAttach(Activity activity) {
 	        super.onAttach(activity);
 	       try {
 	        	mSignedCallback = (OnSignedInListener) activity;
 	        } catch (ClassCastException e) {
 	            throw new ClassCastException(activity.toString()
 	                    + " must implement OnSignedInListener");
 	        }
 	    }
 	
 
 	@Override
 	public void onDestroy() {
 		// TODO Auto-generated method stub
 		super.onDestroy();
 		if (mPlusClient.isConnected())
 			mPlusClient.disconnect();
 	}
 
 	@Override
 	public void onDetach() {
 		// TODO Auto-generated method stub
 		super.onDetach();
 		if (mPlusClient.isConnected())
 			mPlusClient.disconnect();
 	}
 
 	@Override
 	public void onAccessRevoked(ConnectionResult res) {
 		Log.e(TAG, res.toString() + "");
 
 		Toast.makeText(this.getActivity(), "Access revoked!", Toast.LENGTH_LONG)
 				.show();
 	}
 
 	@Override
 	public void onConnectionFailed(ConnectionResult res) {
 		if (mConnectionProgressDialog.isShowing()) {
 
 			if (res.hasResolution()) {
 				try {
 					res.startResolutionForResult(this.getActivity(),
 							REQUEST_CODE_RESOLVE_ERR);
 				} catch (SendIntentException e) {
 					mPlusClient.connect();
 				}
 			}
 		}
 
 		// Save the intent so that we can start an activity when the user clicks
 		// the sign-in button.
 		mConnectionResult = res;
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int responseCode,
 			Intent intent) {
 		if (requestCode == REQUEST_CODE_RESOLVE_ERR
 				&& responseCode == Activity.RESULT_OK) {
 			mConnectionResult = null;
 			mPlusClient.connect();
 		}
 	}
 
 	@Override
 	public void onConnected(Bundle connectionHint) {
 	if(mConnectionProgressDialog.isShowing()){
 		mConnectionProgressDialog.dismiss();
 	}
 
 		String accountName = mPlusClient.getAccountName();
 		mPlusClient.loadPerson(this, "me");
 		Log.d(TAG, "Display Name: " + accountName);
 		Toast.makeText(this.getActivity(), "Conncted:" + accountName,
 				Toast.LENGTH_LONG).show();
 	}
 
 	@Override
 	public void onDisconnected() {
 
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (v.getId() == R.id.sign_in_button && !mPlusClient.isConnected()) {
 			if (mConnectionResult == null) {
 				mPlusClient.connect();
			//	mConnectionProgressDialog.show();
 			}
 			 else {
 				try {
 					mConnectionResult.startResolutionForResult(
 							this.getActivity(), REQUEST_CODE_RESOLVE_ERR);
 				
 				} catch (SendIntentException e) {
 					// Try connecting again.
 					mConnectionResult = null;
 					mPlusClient.connect();
 				}
 			}
 		}else if(v==btn_registerButon){
 			showRegisterDialog();
 		}
 
 	}
 	 private void showRegisterDialog() {
 	        FragmentManager fm = getSherlockActivity().getSupportFragmentManager();
 	        RegistrationFormFragment registerDialog = new RegistrationFormFragment();
 	        registerDialog.register(this);
 	        registerDialog.show(fm, "fragment_register");
 	    }
 	@Override
 	public void onPersonLoaded(ConnectionResult status, Person person) {
 		if (status.getErrorCode() == ConnectionResult.SUCCESS) {
 			et_email.setText(person.getDisplayName());
 			user= new Participant();
 			String fullName=person.getDisplayName();
 			user.setFirstname(fullName.substring(0, fullName.indexOf(' ')));
 			user.setLastname(fullName.substring(fullName.indexOf(' ')));
 			user.setEmail(mPlusClient.getAccountName());
 			user.setBiography(person.getTagline());
 			user.setCity((person.getCurrentLocation()==null)?"N/A":person.getCurrentLocation());
 			user.setCountry((person.getCurrentLocation()==null)?"N/A":person.getCurrentLocation());
 			user.setCompany(person.getOrganizations().get(0).getName());
			user.setPhoto(getBestPictureSizeSize(person.getImage().getUrl()));
 			user.setWebsite(person.getUrl());
 			user.setPassword(getFakePassword());
 			user.setPhone("N/A");
 			
 			registerUser();					
 		
 		
 		}
 		else{
 			//TODO handle this
 		}
 	}
 	
 /**
  * G+ return pic size of 50, we need a bigger size, say 200
  * Url are like : https://lh6.googleusercontent.com/-MgVQQ8F_Buk/AAAAAAAAAAI/AAAAAAAACSQ/8mfy3fB3xcs/photo.jpg?sz=50
  * @param url
  * @return
  */
	private String getBestPictureSizeSize(String url) {
 		String url_param= url.substring(0,url.indexOf('=')+1);
 		String best = url_param+"200";
 		return best;
 	}
 
 	/**
 	 * The backend need a not null password
 	 * @return
 	 */
 	private String getFakePassword() {	
 		return "vfdvbdpfjvjvperj5455vre";
 	}
 
 	private void registerUser() {
 		Activity activity = getActivity();
 		Intent intent = new Intent(activity,RESTService.class);
 		intent.setData(Uri.parse(REGISTER_URI));
 
 		// Here we are going to place our REST call parameters.
 		Bundle params = new Bundle();
 		String payload=new Gson().toJson(user, Participant.class);
 		params.putString(RESTService.KEY_JSON_PLAYLOAD,payload);
 		intent.putExtra(RESTService.EXTRA_HTTP_VERB,RESTService.POST);
 		intent.putExtra(RESTService.EXTRA_PARAMS, params);
 		intent.putExtra(RESTService.EXTRA_RESULT_RECEIVER,getResultReceiver());
 
 		activity.startService(intent);
 	}
 	
 	@Override
 	public void onRESTResult(int code, String result) {
 		Activity activity = getActivity();
 		// Here is where we handle our REST response.
 		// Check to see if we got an HTTP 200 code and have some data.
 		if (code == 200 && result != null) {
 			
 			mSignedCallback.onSignedIn(user);
 			
 			Log.d(TAG, result);
 			Toast.makeText(
 					activity,
 					"Successfully Registered !",
 					Toast.LENGTH_SHORT).show();
 		} else if(code == 409 && result != null){
 			mSignedCallback.onSignedIn(user);
 			Toast.makeText(
 					activity,
 					"Welcome back, "+user.getFirstname(),
 					Toast.LENGTH_SHORT).show();
 		}
 		else
 			
 			if (activity != null) {
 				Log.d(TAG, result);
 				Toast.makeText(
 						activity,
 						"Failed to Register.Check your internet settings.",
 						Toast.LENGTH_SHORT).show();
 			}
 		
 	}
 
 	@Override
 	public void onUserEdited(Participant p) {
 		this.user=p;
 		registerUser();
 	}
 }
