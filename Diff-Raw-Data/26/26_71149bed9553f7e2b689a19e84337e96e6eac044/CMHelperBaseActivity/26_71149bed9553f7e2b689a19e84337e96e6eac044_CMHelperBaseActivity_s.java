 package com.softmo.smssafe;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.softmo.smssafe.R;
 import com.softmo.smssafe.dbengine.IMSetting;
 import com.softmo.smssafe.dbengine.IMDbQuerySetting.TTypSetting;
 import com.softmo.smssafe.main.IMEvent;
 import com.softmo.smssafe.main.IMEventErr;
 import com.softmo.smssafe.main.IMEventSimpleID;
 import com.softmo.smssafe.main.IMListener;
 import com.softmo.smssafe.main.IMMain;
 import com.softmo.smssafe.main.IMMain.TMainState;
 import com.softmo.smssafe.main.TTypEvent;
 import com.softmo.smssafe.safeservice.CMSafeService;
 import com.softmo.smssafe.utils.CMLocator;
 import com.softmo.smssafe.utils.IMLocator;
 import com.softmo.smssafe.utils.MyException;
 import com.softmo.smssafe.utils.MyException.TTypMyException;
 
 public class CMHelperBaseActivity implements IMBaseActivity, IMListener {
 	
 	private static final int SET_PASS_RESULT = 1003;
 	private static final int ENTER_PASS_RESULT = 1004;
 
 	private static final int IDD_GENERATE = 50;
 	private static final int IDD_IMPORT	= 51;
 	
 	public static final String KILL_ACTION = "com.softmo.smssafe.CMHelperBaseActivity.KILL_ACTION";
 	
 	private Activity mOwner;
 	private IMLocator mLocator;
 	private IMMain mMain;
 	
 	private String mSettedPass;
 	private String mEnteredPass;
 	
 	private ProgressDialog mDlg;
 	
 	private KillReceiver mKillReceiver;
 	private boolean mKillReceived = false;
 	
     private final class KillReceiver extends BroadcastReceiver {
         @Override
         public void onReceive(Context context, Intent intent) {
         	Log.d("!!!", "Kill recv: "+mOwner.toString());
         	mKillReceived = true;
             mOwner.finish();
         }
     }
 	
 	private ServiceConnection serviceConncetion = new ServiceConnection() {
 
     	public void onServiceConnected(ComponentName name, IBinder service) {
     		CMHelperBaseActivity.this.mMain = ((CMSafeService.MyBinder)service).getMain();
     		try {
     			getMain().Dispatcher().addListener(CMHelperBaseActivity.this);
     			onMainBind();
 			} catch (MyException e) {
 				ErrorDisplayer.displayError(mOwner, e.getId().getValue());
 			}
     	}
 
 	    public void onServiceDisconnected(ComponentName name) {
 	    	CMHelperBaseActivity.this.mMain = null;
 	        Log.d("!!!", "Service disconnected "+this.toString());
 	    }
     };
 	
     public IMLocator getLocator() {
     	return mLocator;
     }
     
 	public IMMain getMain() throws MyException {
 		if (mMain == null) {
 			Log.w("!!!", "mMain == null");
 			throw new MyException(TTypMyException.EErrServiceNotBinded);
 		}
 		return mMain;
 	}
     
 	private boolean checkPassSet() throws MyException {
 		IMSetting set = mLocator.createSetting();
 		getMain().DbReader().QuerySetting().getById(set, TTypSetting.ERsaPub);
 		
 		if (set.getStrVal().length()>0)
 			return true;
 		
 		if (mSettedPass!=null) {
 			String pass = mSettedPass;
 			mSettedPass=null;
 			
 			try {
 				getMain().changePass(null, pass);
 			} catch (MyException e) {
 				Log.e("!!!", "Error in getMain().changePass:"+e.getId());
 				
 				ErrorDisplayer.displayError(mOwner, e.getId().getValue());
 				
 				Intent i = new Intent(mOwner, SetPassActivity.class);
 				i.putExtra(SetPassActivity.MODE, SetPassActivity.TMode.ESetPass);
 				mOwner.startActivityForResult(i, SET_PASS_RESULT);
 				return false;
 			}
 			return true;
 		}
 		Intent i = new Intent(mOwner, SetPassActivity.class);
 		i.putExtra(SetPassActivity.MODE, SetPassActivity.TMode.ESetPass);
 		mOwner.startActivityForResult(i, SET_PASS_RESULT);
 		return false;
 	}
 	
 	private boolean checkPassActual() throws MyException {
 		if (getMain().isPassValid())
 			return true;
 		
 		if (mEnteredPass!=null) {
 			String pass = mEnteredPass;
 			mEnteredPass=null;
 			
 			try {
 				getMain().enterPass(pass);
 			} catch (MyException e) {
 				Log.e("!!!", "Error in getMain().enterPass:"+e.getId());
 				
 				ErrorDisplayer.displayError(mOwner, e.getId().getValue());
 				
 				Intent i = new Intent(mOwner, EnterPassActivity.class);
 				mOwner.startActivityForResult(i, ENTER_PASS_RESULT);
 				return false;
 			}
 			
 			return true;
 		}
 		
 		ErrorDisplayer.displayError(mOwner, TTypMyException.EPassExpired.getValue());
 		Intent i = new Intent(mOwner, EnterPassActivity.class);
 		mOwner.startActivityForResult(i, ENTER_PASS_RESULT);
 		return false;			
 	}
 	
 	private boolean internalMainBind() throws MyException {
 		if (mKillReceived) {
 			mKillReceived = false;
 			return false;
 		}
 		
 		if (!checkPassSet()) 
 			return false;
 		
 		if (!checkPassActual())
 			return false;
 		
 		getMain().guiResume();
		mOwner.setVisible(true);
 		
 		if (getMain().getState()==TMainState.EImport) {
 			mOwner.showDialog(IDD_IMPORT);
 		}
 		
 		return true;
 	}
 	
 	protected void onMainBind() throws MyException {
 		Log.d("!!!", "Service connected "+mOwner.toString());
 		try {
 			if (!internalMainBind()) return;
 			
 			if (mOwner instanceof IMHelperBaseActivityObserver) {
 				IMHelperBaseActivityObserver ba = (IMHelperBaseActivityObserver)mOwner;
 				ba.onMainBind();
 			}
 		} catch (MyException e) {
 			ErrorDisplayer.displayError(mOwner, e.getId().getValue());
 		}
 	}
 	
 	public CMHelperBaseActivity(Activity owner) {
 		mOwner = owner;
 		mLocator = new CMLocator();
 	}
 	
     public void onCreate(Bundle savedInstanceState) {
     	Log.d("!!!", "onCreate "+mOwner.toString());
         mKillReceiver = new KillReceiver();
         mOwner.registerReceiver(mKillReceiver, new IntentFilter(KILL_ACTION));    	
     }
 	   
     public void onDestroy() {
 		Log.d("!!!", "onDestroy "+mOwner.toString());
 		mOwner.unregisterReceiver(mKillReceiver);
 	}
 	
     public void onStart() {
 		Log.d("!!!", "onStart "+mOwner.toString());
 	}
 
     public void onStop() {
 		Log.d("!!!", "onStop "+mOwner.toString());
 	}
 	
     public void onResume() {
         Log.d("!!!", "onResume "+mOwner.toString());
         
         Intent stIntent = new Intent(mOwner, CMSafeService.class);
         mOwner.startService(stIntent);
         mOwner.bindService(stIntent, serviceConncetion, Activity.BIND_AUTO_CREATE);
 	}
 	
 
     public void onPause() {
 		Log.d("!!!", "onPause "+mOwner.toString()); 
 		if (mDlg != null) mDlg.dismiss(); mDlg = null;
 		mOwner.setVisible(false); //for disable moment show activity before passEnter
 		
 		try {
 			getMain().guiPause();
 			getMain().Dispatcher().delListener(this);
 		} catch (MyException e) {
 			e.printStackTrace();
 		}
 		
 		mMain=null;
 		mOwner.unbindService(serviceConncetion);
 	}
 	
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (resultCode == Activity.RESULT_OK) {
 		    switch (requestCode) {
 		    case SET_PASS_RESULT:
 		    	mSettedPass = data.getStringExtra(SetPassActivity.PASS);
 		        break;
 		    case ENTER_PASS_RESULT:
 		    	mEnteredPass = data.getStringExtra(EnterPassActivity.PASS);
 		    	break;
 		    }
 		} 
 		else if (resultCode == Activity.RESULT_CANCELED){
 		    switch (requestCode) {
 		    case SET_PASS_RESULT:
 		    case ENTER_PASS_RESULT:
 		   		//mOwner.finish();
 		        
 		        Intent i = new Intent(CMHelperBaseActivity.KILL_ACTION);
 		        mOwner.sendBroadcast(i);
 		        break;
 		    }
 		}
 		else {
 			Log.w("!!!", "Unknown result code: "+resultCode);
 		}
     }
     				
 	public void listenerEvent(IMEvent event) throws Exception {
 		
 		TTypEvent typ = event.getTyp();
 		Log.d("!!!", "listenerEvent: "+typ);
 		
 		switch (typ) {
 			case ERsaKeyPairGenerateStart:
 				mOwner.showDialog(IDD_GENERATE);
 				Log.d("!!!", "showDialog: "+IDD_GENERATE);
 				break;
 			case ERsaKeyPairGenerated:
 				if (mDlg!=null) mDlg.dismiss(); mDlg = null;
 				Toast.makeText(mOwner, R.string.pair_generated, Toast.LENGTH_LONG).show();
 				break;
 			case ERsaKeyPairGenerateError:
 				IMEventErr evErr = (IMEventErr)event;
 				if (mDlg!=null) mDlg.dismiss(); mDlg = null;
 				ErrorDisplayer.displayError(mOwner, evErr.getErr());
 				break;
 			case EPassExpired:
 				Log.d("!!!", "EPassExpired received");
 				checkPassActual();
 				break;
 			case EImportStart:
 				mOwner.showDialog(IDD_IMPORT);
 				break;
 			case EImportFinish:
 				if (mDlg!=null) mDlg.dismiss(); mDlg = null;
 				Toast.makeText(mOwner, R.string.importfinish, Toast.LENGTH_SHORT).show();
 				break;
 			case EImportError:
 				if (mDlg!=null) mDlg.dismiss(); mDlg = null;
 				ErrorDisplayer.displayError(mOwner, ((IMEventErr)event).getErr());
 				break;
 			case EImportProgress:
 				if (mDlg!=null) {
 					int p = ((IMEventSimpleID)event).getId();
 					mDlg.setProgress(p);
 					Log.d("!!!", "listenerEvent: setProgress");
 				}
 				break;
 		}
 		
 		if (mOwner instanceof IMListener) {
 			((IMListener) mOwner).listenerEvent(event);
 		}
 	}
 	
 	public Dialog onCreateDialog(int id) {
 		Log.d("!!!", "Helper onCreateDialog: "+id);
 		Dialog dlg = null;
 		
 		switch (id) {
 		case IDD_GENERATE:
 			mDlg = new ProgressDialog(mOwner);
 			mDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 			mDlg.setCancelable(false);
 			mDlg.setMessage(mOwner.getResources().getString(R.string.rsa_generate));
 			dlg = mDlg;
 			break;
 		case IDD_IMPORT:
 			mDlg = new ProgressDialog(mOwner);
 			mDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 			mDlg.setCancelable(false);
 			mDlg.setMessage(mOwner.getResources().getString(R.string.dlg_import));
 			mDlg.setButton(mOwner.getResources().getString(android.R.string.cancel), 
 					new android.content.DialogInterface.OnClickListener() {
 				
 						public void onClick(DialogInterface dialog, int which) {
 							try {
 								Log.d("!!!", "CancelImport pressed");
 								getMain().importSmsCancel();
 							} catch (MyException e) {
 								e.printStackTrace();
 							}
 						}
 					}
 			);
 			dlg = mDlg;
 			break;
 		}
 		
 		Log.d("!!!", "dlg created: "+dlg);
 		return dlg;
 	}
 	
 	public void lockNow() {
 		try {	        
 			getMain().lockNow();			
 	        mOwner.sendBroadcast( new Intent(CMHelperBaseActivity.KILL_ACTION) );			
 		} catch (MyException e) {
 			e.printStackTrace();
 			ErrorDisplayer.displayError(mOwner, e);
 		}
 	}
 
 	public void importSms() {
 		try {
 			getMain().importSms();
 		} catch (MyException e) {
 			ErrorDisplayer.displayError(mOwner, e);
 		}
 	}	
 	
 	public void onPrepareDialog(int id, Dialog dialog) {
 		Log.d("!!!", "Helper onPrepareDialog: "+id);
 		switch(id) {
 		case IDD_GENERATE:
 		case IDD_IMPORT:
 			if (mDlg!=null) mDlg.dismiss(); 
 			mDlg = (ProgressDialog) dialog;
 			break;
 		}
 	}	
 }
