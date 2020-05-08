 package com.bigpupdev.synodroid.wizard;
 
 import java.util.HashMap;
 
 import javax.jmdns.ServiceInfo;
 
 import com.bigpupdev.synodroid.Synodroid;
 import com.bigpupdev.synodroid.data.DSMVersion;
 import com.bigpupdev.synodroid.ui.DownloadPreferenceActivity;
import com.bigpupdev.synodroid.utils.Utils;
 import com.bigpupdev.synodroid.R;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Message;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 
 /**
  * The wizard which try to find server on a local network
  * 
  * @author Eric Taix
  */
 public class ServerWizard {
 
 	public static final String META_NAME = "NAME";
 	public static final String META_PORT = "PORT";
 	public static final String META_HOST = "HOST";
 	public static final String META_SCHEME = "SCHEME";
 	public static final String META_PASSWORD = "PASSWORD";
 	public static final String META_USERNAME = "USERNAME";
 	public static final String META_DSM = "DSM";
 	public static final String META_DDNS_NAME = "DDNS-NAME";
 	public static final String META_DDNS = "DDNS";
 	public static final String META_HTTPS = "HTTPS";
 	public static final String META_WIFI = "WIFI";
 
 	private static final int MSG_SERVER_SELECTED = 1;
 	private static final int MSG_USER_EDITED = 2;
 	private static final int MSG_DSM_SELECTED = 3;
 	private static final int MSG_OPTIONS_HTTPS_EDITED = 4;
 	private static final int MSG_OPTIONS_INTERNET_EDITED = 5;
 
 	private HashMap<String, Object> metaData = new HashMap<String, Object>();
 	private boolean canceled = false;
 	private boolean DEBUG;
 
 	// ====================================================================
 	// The message handler
 	private AddHandler handler = new AddHandler() {
 		@Override
 		public void handleMessage(Message msg) {
 			if (!canceled) {
 				switch (msg.what) {
 				// A server was found
 				case MSG_SERVER_FOUND:
 					try {
 						searchDialog.dismiss();
 						searchDialog = null;
 						ServiceInfo[] servInfos = (ServiceInfo[]) msg.obj;
 						// At least one or more servers
 						if (servInfos != null && servInfos.length > 0) {
 							selectServer(servInfos);
 						}
 						// No server could be found
 						else {
 							context.onWizardFinished(null);
 						}
 					} catch (Exception e) {
 						if (DEBUG) Log.e(Synodroid.DS_TAG, "Exception thrown: ", e);
 					}
 					break;
 				// A server was selected
 				case MSG_SERVER_SELECTED:
 					try {
 						serverDialog.dismiss();
 						serverDialog = null;
 						editUser();
 					} catch (Exception e) {
 						if (DEBUG) Log.e(Synodroid.DS_TAG, "Exception thrown: ", e);
 					}
 					break;
 				// User informations has been edited
 				case MSG_USER_EDITED:
 					try {
 						userDialog.dismiss();
 						userDialog = null;
 						selectDSM();
 					} catch (Exception e) {
 						if (DEBUG) Log.e(Synodroid.DS_TAG, "Exception thrown: ", e);
 					}
 					break;
 				// User informations has been edited
 				case MSG_DSM_SELECTED:
 					try {
 						dsmDialog.dismiss();
 						dsmDialog = null;
 						editOptionsSecure();
 					} catch (Exception e) {
 						if (DEBUG) Log.e(Synodroid.DS_TAG, "Exception thrown: ", e);
 					}
 					break;
 				// Options have been edited
 				case MSG_OPTIONS_HTTPS_EDITED:
 					try {
 						optionsHTTPSDialog.dismiss();
 						optionsHTTPSDialog = null;
 						editOptionsInternet();
 					} catch (Exception e) {
 						if (DEBUG) Log.e(Synodroid.DS_TAG, "Exception thrown: ", e);
 					}
 					break;
 				// Options have been edited
 				case MSG_OPTIONS_INTERNET_EDITED:
 					try {
 						optionsInternetDialog.dismiss();
 						optionsInternetDialog = null;
 						createServers();
 					} catch (Exception e) {
 						if (DEBUG) Log.e(Synodroid.DS_TAG, "Exception thrown: ", e);
 					}
 					break;
 				default:
 					break;
 				}
 			}
 		}
 	};
 
 	// The cancel listener available for every dialogs
 	private DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
 		public void onClick(DialogInterface dialog, int which) {
 			// If we want to add a message when the user cancel the wizard: it's here
 			// !
 			canceled = true;
 		}
 	};
 
 	// The current context in which this wizard is executed
 	private DownloadPreferenceActivity context;
 	// The view inflater
 	private LayoutInflater inflater;
 	// The search server dialog
 	private AlertDialog searchDialog;
 	// The server list dialog
 	private AlertDialog serverDialog;
 	// Often used label
 	private CharSequence cancelSeq;
 	// The user dialog
 	private AlertDialog userDialog;
 	// The DSM dialog
 	private AlertDialog dsmDialog;
 	// The HTTPS option dialog
 	private AlertDialog optionsHTTPSDialog;
 	// The Internet option dialog
 	private AlertDialog optionsInternetDialog;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param ctxP
 	 */
 	public ServerWizard(DownloadPreferenceActivity ctxP, String wifiSSIDP, boolean debug) {
 		context = ctxP;
 		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		cancelSeq = context.getText(R.string.button_cancel);
		metaData.put(META_WIFI, Utils.validateSSID(wifiSSIDP));
 		DEBUG = debug;
 	}
 
 	/**
 	 * Start the wizard
 	 * 
 	 * @param contextP
 	 * @return
 	 */
 	public void start() {
 		discoverServer();
 	}
 
 	/**
 	 * Create server(s)
 	 */
 	private void createServers() {
 		context.onWizardFinished(metaData);
 	}
 
 	/**
 	 * Edit HTTPS options
 	 */
 	private void editOptionsSecure() {
 		View ll = inflater.inflate(R.layout.wizard_options_secured, null);
 		final CheckBox httpsCB = (CheckBox) ll.findViewById(R.id.https_option);
 		optionsHTTPSDialog = new WizardBuilder(context).setTitle(context.getText(R.string.wizard_options_title)).setView(ll).setPositiveButton(context.getText(R.string.button_ok), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				metaData.put(META_HTTPS, httpsCB.isChecked());
 				Message msg = new Message();
 				msg.what = MSG_OPTIONS_HTTPS_EDITED;
 				handler.sendMessage(msg);
 			}
 		}).create();
 		optionsHTTPSDialog.show();
 	}
 
 	/**
 	 * Edit Internet options
 	 */
 	private void editOptionsInternet() {
 		View ll = inflater.inflate(R.layout.wizard_options_internet, null);
 		final CheckBox ddnsCB = (CheckBox) ll.findViewById(R.id.ddns_option);
 		final EditText ddnsET = (EditText) ll.findViewById(R.id.ddns_hostname);
 		ddnsET.setEnabled(false);
 		ddnsCB.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				ddnsET.setEnabled(isChecked);
 			}
 		});
 		optionsInternetDialog = new WizardBuilder(context).setTitle(context.getText(R.string.wizard_options_title)).setView(ll).setPositiveButton(context.getText(R.string.button_ok), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				metaData.put(META_DDNS, ddnsCB.isChecked());
 				metaData.put(META_DDNS_NAME, ddnsET.getText().toString());
 				Message msg = new Message();
 				msg.what = MSG_OPTIONS_INTERNET_EDITED;
 				handler.sendMessage(msg);
 			}
 		}).create();
 		optionsInternetDialog.show();
 	}
 
 	/**
 	 * Select the DSM version
 	 */
 	private void selectDSM() {
 		final DSMVersion[] versions = DSMVersion.values();
 		dsmDialog = new WizardBuilder(context).setTitle(context.getText(R.string.wizard_selectdsm_title)).setAdapter(new ArrayAdapter<DSMVersion>(context, R.layout.wizard_dsmrow, R.id.label, versions), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				DSMVersion dsm = versions[which];
 				metaData.put(META_DSM, dsm.getTitle());
 				Message msg = new Message();
 				msg.what = MSG_DSM_SELECTED;
 				handler.sendMessage(msg);
 			}
 		}).create();
 		dsmDialog.show();
 	}
 
 	/**
 	 * Show and edit user informations (username & password)
 	 */
 	private void editUser() {
 		View ll = inflater.inflate(R.layout.wizard_user_pass_form, null);
 		final TextView uView = (TextView) ll.findViewById(R.id.user);
 		final TextView pView = (TextView) ll.findViewById(R.id.password);
 		userDialog = new WizardBuilder(context).setTitle(context.getText(R.string.wizard_user_title)).setView(ll).setPositiveButton(context.getText(R.string.button_ok), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				metaData.put(META_USERNAME, uView.getText().toString());
 				metaData.put(META_PASSWORD, pView.getText().toString());
 				Message msg = new Message();
 				msg.what = MSG_USER_EDITED;
 				handler.sendMessage(msg);
 			}
 		}).create();
 		userDialog.show();
 	}
 
 	/**
 	 * Select the server to create
 	 */
 	private void selectServer(final ServiceInfo[] infos) {
 		serverDialog = new WizardBuilder(context).setTitle(context.getText(R.string.wizard_selectserver_title)).setAdapter(new ServerAdapter(context, infos), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				ServiceInfo si = infos[which];
 				metaData.put(META_SCHEME, si.getApplication());
 				metaData.put(META_NAME, si.getName());
 				metaData.put(META_PORT, si.getPort());
 				metaData.put(META_HOST, si.getHostAddress());
 				Message msg = new Message();
 				msg.what = MSG_SERVER_SELECTED;
 				handler.sendMessage(msg);
 			}
 		}).create();
 		serverDialog.show();
 	}
 
 	/**
 	 * Discover servers which are on the current WLAN
 	 */
 	private void discoverServer() {
 		// Create or show the search dialog
 		if (searchDialog == null) {
 			View ll = inflater.inflate(R.layout.wizard_discover_server, null);
 			TextView tv = (TextView) ll.findViewById(R.id.searching_text_id);
 			tv.setText(context.getText(R.string.wizard_searching_msg));
 			searchDialog = new WizardBuilder(context).setTitle(context.getText(R.string.wizard_searching_title)).setView(ll).create();
 			searchDialog.show();
 		} else {
 			searchDialog.show();
 		}
 		// Launch the thead to search for servers
 		DiscoveringThread thread = new DiscoveringThread(context, handler, DEBUG);
 		thread.start();
 	}
 
 	/* ======================================================================== */
 	/**
 	 * An AlertDailog builder which add some default behaviour
 	 */
 	private class WizardBuilder extends AlertDialog.Builder {
 		/**
 		 * Default constructor
 		 * 
 		 * @param ctxP
 		 */
 		public WizardBuilder(Context ctxP) {
 			super(ctxP);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see android.app.AlertDialog.Builder#create()
 		 */
 		@Override
 		public AlertDialog create() {
 			// First add default values
 			setCancelable(false);
 			setNegativeButton(cancelSeq, cancelListener);
 			// Then create the dialog
 			return super.create();
 		}
 	}
 }
