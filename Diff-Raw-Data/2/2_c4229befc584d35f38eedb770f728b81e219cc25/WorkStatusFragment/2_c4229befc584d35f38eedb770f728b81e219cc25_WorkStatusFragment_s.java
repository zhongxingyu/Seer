 package info.guardianproject.justpayphone.app.screens;
 
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.json.JSONException;
 import org.witness.informacam.InformaCam;
 import org.witness.informacam.models.forms.IForm;
 import org.witness.informacam.models.j3m.IData;
 import org.witness.informacam.models.j3m.IRegionData;
 import org.witness.informacam.models.media.ILog;
 import org.witness.informacam.models.media.IRegion;
 import org.witness.informacam.storage.FormUtility;
 import org.witness.informacam.ui.CameraActivity;
 import org.witness.informacam.utils.Constants.App;
 import org.witness.informacam.utils.Constants.Codes;
 import org.witness.informacam.utils.Constants.Models;
 import org.witness.informacam.utils.Constants.App.Camera;
 import org.witness.informacam.utils.InformaCamBroadcaster.InformaCamStatusListener;
 import org.witness.informacam.utils.TimeUtility;
 
 import info.guardianproject.justpayphone.R;
 import info.guardianproject.justpayphone.app.SelfieActivity;
 import info.guardianproject.justpayphone.app.popups.KeypadPopup;
 import info.guardianproject.justpayphone.utils.Constants.Forms;
 import info.guardianproject.justpayphone.utils.Constants.HomeActivityListener;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class WorkStatusFragment extends Fragment implements OnClickListener, InformaCamStatusListener {
 	View rootView;
 	Activity a;
 
 	private enum WorkStatusFragmentMode
 	{
 		Uninitialized, Normal, SigningIn, Working, SigningOut, LunchForm
 	}
 
 	WorkStatusFragmentMode mCurrentMode = WorkStatusFragmentMode.Uninitialized;
 	
 	ProgressBar waiter;
 	View btnLunchNo, btnLunch10, btnLunch20, btnLunch30, btnLunch45, btnLunch60;
 	LinearLayout lunchQuestionnaire, lunchMinutesChoiceRoot, lunchTakenChoiceRoot;
 	RadioGroup lunchTakenProxy;
 	EditText lunchMinutesProxy;
 
 	Timer t;
 	TimerTask tt;
 	Handler h = new Handler();
 
 	TextView timeAtWork;
 	long timeWorked = 0;
 	boolean timerIsRunning = false;
 
 	InformaCam informaCam = null;
 	private View mWorkSignInView;
 	private View mBtnSignIn;
 	private View mWorkSignOutView;
 	private View mBtnSignOut;
 	private View mWorkLunchView;
 	private boolean hasBeenInited = false;
 	private RadioButton lunchTakenProxyYes;
 	private RadioButton lunchTakenProxyNo;
 	private IForm mLunchForm;
 	
 	private final static String LOG = App.LOG;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState) {
 		super.onCreateView(li, container, savedInstanceState);
 
 		rootView = li.inflate(R.layout.fragment_home_work_status, null);
 		waiter = (ProgressBar) rootView.findViewById(R.id.work_status_waiter);
 
 		mWorkSignInView = rootView.findViewById(R.id.work_sign_in_view);
 		mBtnSignIn = rootView.findViewById(R.id.btnSignIn);
 		mBtnSignIn.setOnClickListener(this);
 		
 		mWorkSignOutView = rootView.findViewById(R.id.work_sign_out_view);
 		mBtnSignOut = rootView.findViewById(R.id.btnSignOut);
 		mBtnSignOut.setOnClickListener(this);
 		
 		timeAtWork = (TextView) rootView.findViewById(R.id.tvTimeWorked);
 
 		mWorkLunchView = rootView.findViewById(R.id.work_lunch_view);
 		
 		lunchQuestionnaire = (LinearLayout) rootView.findViewById(R.id.work_status_lunch_questionnaire);
 		btnLunchNo = rootView.findViewById(R.id.btnLunchNo);
 		btnLunchNo.setOnClickListener(mLunchButtonClickedListener);
 		btnLunch10 = rootView.findViewById(R.id.btnLunch10);
 		btnLunch10.setOnClickListener(mLunchButtonClickedListener);
 		btnLunch20 = rootView.findViewById(R.id.btnLunch20);
 		btnLunch20.setOnClickListener(mLunchButtonClickedListener);
 		btnLunch30 = rootView.findViewById(R.id.btnLunch30);
 		btnLunch30.setOnClickListener(mLunchButtonClickedListener);
 		btnLunch45 = rootView.findViewById(R.id.btnLunch45);
 		btnLunch45.setOnClickListener(mLunchButtonClickedListener);
 		btnLunch60 = rootView.findViewById(R.id.btnLunch60);
 		btnLunch60.setOnClickListener(mLunchButtonClickedListener);
 
 		setCurrentMode(mCurrentMode);
 		
 		return rootView;
 	}
 
 	@Override
 	public void onAttach(Activity a) {
 		super.onAttach(a);
 		this.a = a;
 		informaCam = InformaCam.getInstance();
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 		if(((HomeActivityListener) a).getInitFlag()) {
 			init();
 		}
 	}
 
 	private void init() {
 		if (waiter != null)
 			waiter.setVisibility(View.GONE);
 		
 		if (!hasBeenInited)
 		{
 			hasBeenInited = true;
 		boolean isAtWork = false;
 		if(((HomeActivityListener) a).getCurrentLog() != null && !((HomeActivityListener) a).getCurrentLog().has(Models.IMedia.ILog.IS_CLOSED)) {
 			informaCam.informaService.associateMedia(((HomeActivityListener) a).getCurrentLog());
 			isAtWork = true;
 		} else {
 			isAtWork = false;
 		}
 		setCurrentMode(isAtWork ? WorkStatusFragmentMode.Working : WorkStatusFragmentMode.Normal);
 		}
 	}
 
 	private void initLog() {
 		ILog iLog = new ILog();
 
 		iLog.startTime = 0; //informaCam.informaService.getCurrentTime();
 		iLog._id = iLog.generateId("log_" + System.currentTimeMillis());
 
 		info.guardianproject.iocipher.File rootFolder = new info.guardianproject.iocipher.File(iLog._id);
 		if(!rootFolder.exists()) {
 			rootFolder.mkdir();
 		}
 
 		iLog.rootFolder = rootFolder.getAbsolutePath();
 		informaCam.mediaManifest.addMediaItem(iLog);
 		informaCam.mediaManifest.save();
 
 		((HomeActivityListener) a).setCurrentLog(iLog);
 
 		informaCam.informaService.associateMedia(((HomeActivityListener) a).getCurrentLog());
 	}
 
 	private void setCurrentMode(WorkStatusFragmentMode mode)
 	{
 		Log.d(LOG, "Changing to mode: " + mode.toString());
 		
 		mCurrentMode = mode;
 		if (mCurrentMode == WorkStatusFragmentMode.Uninitialized)
 		{
 			mWorkSignInView.setVisibility(View.GONE);
 			mWorkSignOutView.setVisibility(View.GONE);
 			mWorkLunchView.setVisibility(View.GONE);
 		}
 		else if (mCurrentMode == WorkStatusFragmentMode.Normal || mCurrentMode == WorkStatusFragmentMode.SigningIn)
 		{
 			mWorkSignInView.setVisibility(View.VISIBLE);
 			mWorkSignOutView.setVisibility(View.GONE);
 			mWorkLunchView.setVisibility(View.GONE);
 		}
 		else if (mCurrentMode == WorkStatusFragmentMode.Working || mCurrentMode == WorkStatusFragmentMode.SigningOut)
 		{
 			mWorkSignInView.setVisibility(View.GONE);
 			mWorkSignOutView.setVisibility(View.VISIBLE);
 			mWorkLunchView.setVisibility(View.GONE);
 		}
 		
 		if (mCurrentMode == WorkStatusFragmentMode.Normal)
 		{
 			((HomeActivityListener) a).showNavigationDots(true);
 		}
 		else if (mCurrentMode == WorkStatusFragmentMode.SigningIn)
 		{
 			getSelfie(true);
 		}
 		else if (mCurrentMode == WorkStatusFragmentMode.Working)
 		{
 			startWorkTimer();
 			((HomeActivityListener) a).showNavigationDots(true);
 		}
 		else if (mCurrentMode == WorkStatusFragmentMode.SigningOut)
 		{
 			stopWorkTimer();
 			getSelfie(false);
 		}
 		else if (mCurrentMode == WorkStatusFragmentMode.LunchForm)
 		{
 			((HomeActivityListener) a).showNavigationDots(false);
 			mWorkSignInView.setVisibility(View.GONE);
 			mWorkSignOutView.setVisibility(View.GONE);
 			mWorkLunchView.setVisibility(View.VISIBLE);
 		}
 	}
 	
 	private void startWorkTimer()
 	{
 		long currentTime = informaCam.informaService.getCurrentTime();
 
 		ILog log = ((HomeActivityListener) a).getCurrentLog();
 		
 		// If not started, start now
 		if (log.startTime == 0)
 			log.startTime = currentTime;
 
 		timeWorked = (currentTime - log.startTime);
 
 		if(!timerIsRunning) {
 			t = new Timer();
 			t.schedule(new TimerTask() {
 				@Override
 				public void run() {
 
 					if(mCurrentMode == WorkStatusFragmentMode.Working) {
 						timeWorked += 1000;
 
 						h.post(new Runnable() {
 							@Override
 							public void run() {
 								//timeAtWork.setText(WorkStatusFragment.this.a.getString(R.string.at_work_x, TimeUtility.millisecondsToTimestamp(timeWorked)));
 								timeAtWork.setText(TimeUtility.millisecondsToTimestamp(timeWorked));
 							}
 						});
 					}
 				}
 			}, 0L, 1000);
 			timerIsRunning = true;
 		}
 	}
 	
 	private void stopWorkTimer()
 	{
 		if(timerIsRunning) {
 			t.cancel();
 			t = null;
 
 			timerIsRunning = false;
 		}
 	}
 
 	@SuppressWarnings("deprecation")
 	@Override
 	public void onClick(View v) {
 		if (v == mBtnSignIn) {
 			
 			HomeActivityListener hal = (HomeActivityListener) a;
			if (hal.getCurrentLog() != null && !hal.getCurrentLog().optBoolean(Models.IMedia.ILog.IS_CLOSED, false))
 			{
 				// Already log in progress
 				Toast.makeText(a, getString(R.string.you_have_already_logged), Toast.LENGTH_LONG).show();
 				return;
 			}
 			setCurrentMode(WorkStatusFragmentMode.SigningIn);
 		}
 		else if (v == mBtnSignOut) {
 
 			ILog log = ((HomeActivityListener) a).getCurrentLog();
 			log.endTime = informaCam.informaService.getCurrentTime();
 
 			for(IForm form : FormUtility.getAvailableForms()) {
 				if(form.namespace.equals(Forms.LUNCH_QUESTIONNAIRE)) {
 					info.guardianproject.iocipher.File formContent = new info.guardianproject.iocipher.File(((HomeActivityListener) a).getCurrentLog().rootFolder, "form");
 
 					mLunchForm = new IForm(form, a);
 					mLunchForm.answerPath = formContent.getAbsolutePath();
 					IRegion topRegion = log.getTopLevelRegion();
 					if (topRegion == null)
 						topRegion = log.addRegion(a, null);
 					topRegion.addForm(mLunchForm);
 
 					lunchTakenProxy = new RadioGroup(a);
 					lunchTakenProxyYes = new RadioButton(a);
 					lunchTakenProxyNo = new RadioButton(a);
 					lunchTakenProxy.addView(lunchTakenProxyYes);
 					lunchTakenProxy.addView(lunchTakenProxyNo);
 
 					lunchMinutesProxy = new EditText(a);
 					lunchMinutesProxy.setText(a.getString(R.string.x_minutes, 0));
 					
 					// attach elements to form
 					mLunchForm.associate(lunchTakenProxy, Forms.LunchQuestionnaire.LUNCH_TAKEN);
 					mLunchForm.associate(lunchMinutesProxy, Forms.LunchQuestionnaire.LUNCH_MINUTES);
 
 					break;
 				}
 			}
 
 			setCurrentMode(WorkStatusFragmentMode.SigningOut);
 		} 
 	}
 
 	@Override
 	public void onInformaCamStart(Intent intent) {}
 
 	@Override
 	public void onInformaCamStop(Intent intent) {}
 
 	@Override
 	public void onInformaStop(Intent intent) {
 //		if(((HomeActivityListener) a).getCurrentLog() != null) {
 //			Log.d(LOG, "LOG IS NOW: " + ((HomeActivityListener) a).getCurrentLog().asJson().toString());
 //			((HomeActivityListener) a).getCurrentLog().export(new Handler() {
 //				@Override
 //				public void handleMessage(Message msg) {
 //					Log.d(LOG, "MSG DATA: " + msg.getData().toString());
 //				}
 //			}, null, true);
 //
 //			((HomeActivityListener) a).setCurrentLog(null);
 //		}
 	}
 
 	@Override
 	public void onInformaStart(Intent intent) {
 		init();
 	}	
 	
 	private void getSelfie(boolean signingIn)
 	{
 		Intent surfaceGrabberIntent = new Intent(a, SelfieActivity.class);
 		if (!signingIn)
 			surfaceGrabberIntent.putExtra(info.guardianproject.justpayphone.utils.Constants.Codes.Extras.IS_SIGNING_OUT, true);
 		startActivityForResult(surfaceGrabberIntent, Codes.Routes.IMAGE_CAPTURE);
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if (resultCode == Activity.RESULT_CANCELED) {
 			switch(requestCode) {
 			case Codes.Routes.IMAGE_CAPTURE:
 				if (mCurrentMode == WorkStatusFragmentMode.SigningIn)
 					setCurrentMode(WorkStatusFragmentMode.Normal);
 				else if (mCurrentMode == WorkStatusFragmentMode.SigningOut)
 					setCurrentMode(WorkStatusFragmentMode.Working);
 				break;
 			}
 		}
 		else if(resultCode == Activity.RESULT_OK) {
 			switch(requestCode) {
 			case Codes.Routes.IMAGE_CAPTURE:
 				
 				if (mCurrentMode == WorkStatusFragmentMode.SigningIn)
 				{
 					initLog();
 					setCurrentMode(WorkStatusFragmentMode.Working);
 				}
 				else if (mCurrentMode == WorkStatusFragmentMode.SigningOut)
 					setCurrentMode(WorkStatusFragmentMode.LunchForm);
 				
 //				Log.d(LOG, "THIS RETURNS:\n" + data.getStringExtra(Codes.Extras.RETURNED_MEDIA));
 //				try {
 //					JSONArray returnedMedia = ((JSONObject) new JSONTokener(data.getStringExtra(Codes.Extras.RETURNED_MEDIA)).nextValue()).getJSONArray("dcimEntries");
 //
 //					// add to current log's attached media
 //					IMedia m = new IMedia();
 //					m.inflate(returnedMedia.getJSONObject(0));
 //						
 //					((HomeActivityListener) a).getCurrentLog().attachedMedia.add(m._id);
 //					((HomeActivityListener) a).persistLog();
 //						
 //					// update UI
 //					//updateWorkspaces();
 //						
 //				} catch(JSONException e) {
 //					Log.e(LOG, e.toString());
 //					e.printStackTrace();
 //				}
 				break;
 			}
 		}
 	}
 	
 	private View.OnClickListener mLunchButtonClickedListener = new View.OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			
 			boolean lunchTaken = true;
 			int lunchMinutes = 0;
 			
 			if (v == btnLunchNo)
 				lunchTaken = false;
 			else if (v == btnLunch10)
 				lunchMinutes = 10;
 			else if (v == btnLunch20)
 				lunchMinutes = 20;
 			else if (v == btnLunch30)
 				lunchMinutes = 30;
 			else if (v == btnLunch45)
 				lunchMinutes = 45;
 			else if (v == btnLunch60)
 				lunchMinutes = 60;
 			
 			if (lunchTaken)
 			{
 				lunchTakenProxyYes.setChecked(true);
 				lunchTakenProxyNo.setChecked(false);
 			}
 			else
 			{
 				lunchTakenProxyYes.setChecked(false);
 				lunchTakenProxyNo.setChecked(true);
 			}
 			lunchMinutesProxy.setText(String.valueOf(lunchMinutes));
 			h.post(new Runnable()
 			{
 				@Override
 				public void run() {
 					saveLunchInformation();
 				}
 			});
 		}
 	};
 	
 	private void saveLunchInformation()
 	{
 		try {
 			mLunchForm.answer(Forms.LunchQuestionnaire.LUNCH_TAKEN);
 			mLunchForm.answer(Forms.LunchQuestionnaire.LUNCH_MINUTES);
 					
 			info.guardianproject.iocipher.FileOutputStream fos = new info.guardianproject.iocipher.FileOutputStream(mLunchForm.answerPath);			
 			mLunchForm.save(fos);
 
 //			if(((HomeActivityListener) a).getCurrentLog().data == null) {
 //				((HomeActivityListener) a).getCurrentLog().data = new IData();
 //				((HomeActivityListener) a).getCurrentLog().data.userAppendedData = new ArrayList<IRegionData>();
 //			}
 //
 //			//TODO - fix all this!
 //			IRegionData regionData = new IRegionData();
 //			
 ////			regionData.metadata.put(Models.IMedia.ILog.START_TIME, ((HomeActivityListener) a).getCurrentLog().startTime);
 ////			regionData.metadata.put(Models.IMedia.ILog.END_TIME, ((HomeActivityListener) a).getCurrentLog().endTime);
 //			regionData.timestamp = informaCam.informaService.getCurrentTime();
 //			((HomeActivityListener) a).getCurrentLog().data.userAppendedData.add(regionData);
 
 			((HomeActivityListener) a).persistLog();
 
 			informaCam.stopInforma();
 
 		} catch (FileNotFoundException e) {
 			Log.e(LOG, e.toString());
 			e.printStackTrace();
 		}
 
 		((HomeActivityListener) a).showLogView(true);
 		setCurrentMode(WorkStatusFragmentMode.Normal);
 	}
 }
