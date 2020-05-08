 package info.guardianproject.justpayphone.app;
 
 import info.guardianproject.justpayphone.R;
 import info.guardianproject.justpayphone.app.screens.wizard.WizardCreateDB;
 import info.guardianproject.justpayphone.app.screens.wizard.WizardLawyerInformation;
 import info.guardianproject.justpayphone.app.screens.wizard.WizardSelectLanguage;
 import info.guardianproject.justpayphone.app.screens.wizard.WizardTakePhoto;
 import info.guardianproject.justpayphone.app.screens.wizard.WizardWaitForKey;
 import info.guardianproject.justpayphone.utils.Constants;
 import info.guardianproject.justpayphone.utils.Constants.Settings;
 import info.guardianproject.justpayphone.utils.Constants.WizardActivityListener;
 import info.guardianproject.justpayphone.utils.Constants.Codes.Extras;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 import org.spongycastle.openpgp.PGPException;
 import org.witness.informacam.InformaCam;
 import org.witness.informacam.crypto.KeyUtility;
 import org.witness.informacam.models.notifications.INotification;
 import org.witness.informacam.models.organizations.IInstalledOrganizations;
 import org.witness.informacam.models.organizations.IOrganization;
 import org.witness.informacam.models.transport.ITransportStub;
 import org.witness.informacam.storage.FormUtility;
 import org.witness.informacam.transport.TransportUtility;
 import org.witness.informacam.utils.Constants.Codes;
 import org.witness.informacam.utils.Constants.InformaCamEventListener;
 import org.witness.informacam.utils.Constants.Logger;
 import org.witness.informacam.utils.Constants.Models;
 import org.witness.informacam.utils.Constants.App.Storage.Type;
 import org.witness.informacam.utils.Constants.Models.IUser;
 import org.witness.informacam.utils.Constants.Models.IMedia.MimeType;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 
 public class WizardActivity extends SherlockFragmentActivity implements WizardActivityListener, InformaCamEventListener
 {
 	private static final boolean COLLECT_USER_NAME_EMAIL = false;
 	private static final boolean COLLECT_LAWYER_INFORMATION = true;
 	private static final String HARDCODED_LAWYER_NUMBER = "";
 	
 	private InformaCam informaCam;
 	private WizardWaitForKey mWaitForKeyFragment;
 	private Handler mHandler;
 	
 	private final static String LOG = Constants.App.Wizard.LOG;
 	
 	public WizardActivity()
 	{
 		super();
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 
 		mHandler = new Handler();
 
 		informaCam =  InformaCam.getInstance();
 		informaCam.setEventListener(this);
 		
 		setContentView(R.layout.activity_wizard);
 
 		Fragment step1 = Fragment.instantiate(this, WizardSelectLanguage.class.getName());
 
 		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
 		ft.add(R.id.wizard_holder, step1);
 		//ft.addToBackStack(null);
 		ft.commit();
 	}
 
 	@Override
 	public void onBackPressed()
 	{
 		if (getSupportFragmentManager().getBackStackEntryCount() == 0)
 		{
 			this.setResult(RESULT_CANCELED);
 			finish();
 		}
 		else
 		{
 			super.onBackPressed();
 		}
 	}
 
 	@Override
 	public void onLanguageSelected(String languageCode)
 	{
 		SharedPreferences.Editor sp = PreferenceManager.getDefaultSharedPreferences(this).edit();
 		sp.putString(Codes.Extras.LOCALE_PREF_KEY, languageCode).commit();
 
 		Configuration configuration = new Configuration();
 		configuration.locale = new Locale(languageCode);
 
 		getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
 
 		setResult(Activity.RESULT_FIRST_USER, new Intent().putExtra(Codes.Extras.CHANGE_LOCALE, true));
 		finish();
 	}
 
 	@Override
 	public void onLanguageConfirmed()
 	{
 		if (COLLECT_USER_NAME_EMAIL)
 		{
 			Fragment step2 = Fragment.instantiate(this, WizardCreateDB.class.getName(), createWizardStepArgumentBundle(1));
 
 			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
 			ft.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left, R.anim.slide_in_from_left, R.anim.slide_out_to_right);
 			ft.replace(R.id.wizard_holder, step2);
 			ft.addToBackStack(null);
 			ft.setTransitionStyle(2);
 			ft.commit();
 		}
 		else
 		{
 			onUsernameCreated(getTelephoneNumber(), getEmailAddress(), WizardCreateDB.autoGeneratePassword(this.getBaseContext()));
 		}
 	}
 	
 	private String getTelephoneNumber() {
 		TelephonyManager tm = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
 		String phone_number = tm.getLine1Number();
 		
 		return phone_number == null ? "" : phone_number;
 	}
 	
 	private String getEmailAddress() {
 		Account account = AccountManager.get(this).getAccounts()[0];
 		String email_address = account.name;
 		
 		return email_address == null ? "" : email_address;
 	}
 
 	@Override
 	public void onUsernameCreated(String username, String email, String password)
 	{
 		try {
 			informaCam.user.put(IUser.ALIAS, username);
 			informaCam.user.put(IUser.EMAIL, email);
 			informaCam.user.put(IUser.PASSWORD, password);
 			informaCam.user.isInOfflineMode = false;
 			
 			Fragment step3 = Fragment.instantiate(this, WizardTakePhoto.class.getName(), createWizardStepArgumentBundle(1 + (COLLECT_USER_NAME_EMAIL ? 1 : 0)));
 
 			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
 			ft.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left, R.anim.slide_in_from_left, R.anim.slide_out_to_right);
 			ft.replace(R.id.wizard_holder, step3);
 			ft.addToBackStack(null);
 			ft.commit();
 
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void onTakePhotoClicked()
 	{
 		Intent surfaceGrabberIntent = new Intent(this, WizardPhotoActivity.class);
 		startActivityForResult(surfaceGrabberIntent, Codes.Routes.IMAGE_CAPTURE);
 	}
 	
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if(resultCode == Activity.RESULT_OK) {
 			switch(requestCode) {
 			case Codes.Routes.IMAGE_CAPTURE:
 				
 				if (COLLECT_LAWYER_INFORMATION)
 				{
 					Fragment step4 = Fragment.instantiate(this, WizardLawyerInformation.class.getName(), createWizardStepArgumentBundle(2 + (COLLECT_USER_NAME_EMAIL ? 1 : 0)));
 
 					FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
 					ft.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left, R.anim.slide_in_from_left, R.anim.slide_out_to_right);
 					ft.replace(R.id.wizard_holder, step4);
 					ft.addToBackStack(null);
 					ft.commit();
 				}
 				else
 				{
 					onLawyerInfoSet(HARDCODED_LAWYER_NUMBER);
 				}
 				break;
 			}
 		}
 	}
 
 	@Override
 	public void onLawyerInfoSet(String phoneNumber) {
 		
 		// Save number in prefs
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
 		prefs.edit().putString(Settings.LAWYER_PHONE, phoneNumber).commit();
 		
 		mWaitForKeyFragment = (WizardWaitForKey) Fragment.instantiate(this, WizardWaitForKey.class.getName());
 
 		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
 		ft.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_left, R.anim.slide_in_from_left, R.anim.slide_out_to_right);
 		ft.replace(R.id.wizard_holder, mWaitForKeyFragment);
 		ft.addToBackStack(null);
 		ft.commit();	
 		
 		generateKey();
 	}
 
 	@Override
 	public void onUpdate(Message message) {
 		int code = message.getData().getInt(Codes.Extras.MESSAGE_CODE);
 
 		switch (code)
 		{
 		// TODO - handle error case!
 //		case org.witness.informacam.utils.Constants.Codes.Messages.Transport.GENERAL_FAILURE:
 //			mHandlerUI.post(new Runnable()
 //			{
 //				@Override
 //				public void run()
 //				{
 //					Toast.makeText(HomeActivity.this, message.getData().getString(Codes.Extras.GENERAL_FAILURE), Toast.LENGTH_LONG).show();
 //				}
 //			});
 //			break;
 		
 		case Codes.Messages.UI.UPDATE:
 			if (mWaitForKeyFragment != null)
 				mWaitForKeyFragment.setProgress((Integer) message.getData().get(Codes.Keys.UI.PROGRESS));
 			break;
 		
 		case org.witness.informacam.utils.Constants.Codes.Messages.UI.REPLACE:
 			// Ok, done!
 			setResult(RESULT_OK);
 			finish();
 			break;
 		}
 	}
 	
 	private void generateKey()
 	{
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				if(KeyUtility.initDevice()) {
 					
 					mHandler.post(new Runnable()
 					{
 						@Override
 						public void run()
 						{
 							// save everything
 							InformaCam informaCam = (InformaCam)getApplication();
 							
 							informaCam.user.hasCompletedWizard = true;
 							informaCam.user.lastLogIn = System.currentTimeMillis();
 							informaCam.user.isLoggedIn = true;
 							
 							informaCam.saveState(informaCam.user);
 							informaCam.saveState(informaCam.languageMap);
 							
 							try {
 								informaCam.initData();
 							} catch (PGPException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							}
 							
 							installOrganization();
 							
 							// Tell others we are done!
 							Bundle data = new Bundle();
 							data.putInt(org.witness.informacam.utils.Constants.Codes.Extras.MESSAGE_CODE, org.witness.informacam.utils.Constants.Codes.Messages.UI.REPLACE);
 							
 							Message message = new Message();
 							message.setData(data);
 							
 							informaCam.update(data);
 						}
 					});
 				}
 			}
 		}).start();
 	}
 	
 	private void installOrganization() {
 		Log.d(LOG, "OK WIZARD COMPLETED!");
 		try {
 			for(String s : informaCam.getAssets().list("includedOrganizations")) {
 				
 				InputStream ictdIS = informaCam.ioService.getStream("includedOrganizations/" + s, Type.APPLICATION_ASSET);
 				
 				byte[] ictdBytes = new byte[ictdIS.available()];
 				ictdIS.read(ictdBytes);
 				Logger.d(LOG, "NEW ICTD:\n" + new String(ictdBytes));
 				
 				IOrganization organization = informaCam.installICTD((JSONObject) new JSONTokener(new String(ictdBytes)).nextValue(), null, this);
 				if(organization != null && !informaCam.user.isInOfflineMode) {
 					INotification notification = new INotification(getResources().getString(R.string.key_sent), getResources().getString(R.string.you_have_sent_your_credentials_to_x, organization.organizationName), Models.INotification.Type.NEW_KEY);
 					notification.taskComplete = false;
 					informaCam.addNotification(notification, null);
 					
 					ITransportStub transportStub = new ITransportStub(organization, notification);
					transportStub.setAsset(IUser.PUBLIC_CREDENTIALS, IUser.PUBLIC_CREDENTIALS, MimeType.ZIP);
 					TransportUtility.initTransport(transportStub);
 				} else {
 					Logger.d(LOG, "USER IS PROBABLY IN OFFLINE MODE");
 				}
 			}
 		} catch(IOException e) {
 			Log.e(LOG, e.toString());
 			e.printStackTrace();
 		} catch (JSONException e) {
 			Log.e(LOG, e.toString());
 			e.printStackTrace();
 		}
 	}
 	
 	private Bundle createWizardStepArgumentBundle(int currentStep)
 	{
 		int totalSteps = 1 + (COLLECT_USER_NAME_EMAIL ? 1 : 0) + (COLLECT_LAWYER_INFORMATION ? 1 : 0);
 		Bundle args = new Bundle();
 		if (totalSteps > 1)
 			args.putString(Extras.WIZARD_STEP, getString(R.string.wizard_step_n_of_n, currentStep, totalSteps));
 		else
 			args.putString(Extras.WIZARD_STEP, ""); // "Step 1 of 1" looks kind of ridiculous
 		return args;
 	}
 }
