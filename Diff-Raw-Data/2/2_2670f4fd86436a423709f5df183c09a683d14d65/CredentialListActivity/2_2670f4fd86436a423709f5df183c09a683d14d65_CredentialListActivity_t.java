 /**
  * CredentialListActivity.java
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  * 
  * Copyright (C) Wouter Lueks, Radboud University Nijmegen, Februari 2013.
  */
 
 package org.irmacard.androidmanagement;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import net.sourceforge.scuba.smartcards.CardServiceException;
 import net.sourceforge.scuba.smartcards.IsoDepCardService;
 
 import org.irmacard.android.util.credentials.CredentialPackage;
 import org.irmacard.androidmanagement.dialogs.AlertDialogFragment;
 import org.irmacard.androidmanagement.dialogs.CardMissingDialogFragment;
 import org.irmacard.androidmanagement.dialogs.ChangePinDialogFragment;
 import org.irmacard.androidmanagement.dialogs.ConfirmDeleteDialogFragment;
 import org.irmacard.androidmanagement.dialogs.ConfirmDeleteDialogFragment.ConfirmDeleteDialogListener;
 import org.irmacard.androidmanagement.util.TransmitResult;
 import org.irmacard.credentials.idemix.IdemixCredentials;
 import org.irmacard.credentials.info.CredentialDescription;
 
 import org.irmacard.credentials.util.CardVersion;
 import org.irmacard.credentials.util.log.LogEntry;
 import org.irmacard.idemix.IdemixService;
 import android.app.DialogFragment;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.nfc.NfcAdapter;
 import android.nfc.Tag;
 import android.nfc.tech.IsoDep;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 
 /**
  * An activity representing a list of Credentials. This activity has different
  * presentations for handset and tablet-size devices. On handsets, the activity
  * presents a list of items, which when touched, lead to a
  * {@link CredentialDetailActivity} representing item details. On tablets, the
  * activity presents the list of items and item details side-by-side using two
  * vertical panes.
  * <p>
  * The activity makes heavy use of fragments. The list of items combined with
  * log and settings buttons is a {@link MenuFragment} and the item details (if
  * present) is a {@link CredentialDetailFragment}.
  * <p>
  * This activity also implements the required {@link MenuFragment.Callbacks}
  * interface to listen for item selections and button callbacks.
  */
 public class CredentialListActivity extends FragmentActivity implements
 		MenuFragment.Callbacks, ConfirmDeleteDialogListener,
 		CardMissingDialogFragment.CardMissingDialogListener,
 		ChangePinDialogFragment.ChangePinDialogListener,
 		AlertDialogFragment.AlertDialogListener,
 		CredentialDetailFragment.Callbacks,
 		SettingsFragment.Callbacks {
 
 	/**
 	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
 	 * device.
 	 */
 	private boolean mTwoPane;
 	
 	private ArrayList<CredentialPackage> credentials;
 	private ArrayList<LogEntry> logs;
 	private Tag tag;
 
 	private NfcAdapter nfcA;
 	private PendingIntent mPendingIntent;
 	private IntentFilter[] mFilters;
 	private String[][] mTechLists;
 
 	private String TAG = "CredentialListActivity";
 
 	private interface CardProgram {
 		public TransmitResult run(IdemixService is) throws CardServiceException;
 	}
 
 	private enum Action {
 		NONE,
 		DELETE_CREDENTIAL,
 		CHANGE_CARD_PIN,
 		CHANGE_CREDENTIAL_PIN
 	}
 
 	private enum State {
 		NORMAL,
 		TEST_CARD_PRESENCE,
 		WAITING_FOR_CARD,
 		CONFIRM_ACTION,
 		PERFORM_ACTION,
 		ACTION_PERFORMED,
 		ACTION_FAILED
 	}
 
 	private Action currentAction = Action.NONE;
 	private State currentState = State.NORMAL;
 	private DialogFragment cardMissingDialog;
 
 	// Needed to delete credentials
 	private CredentialDescription toBeDeleted;
 
 	// Needed to change PINs
 	private String old_pin;
 	private String new_pin;
 	private int tries;
 
 	// Deal properly with failures
 	private Exception exception;
 
 	private String cardPin;
 	private CardVersion cardVersion;
 
 	// Requests
 	private int SETTINGS_REQUEST = 11;
 	private int DETAIL_REQUEST = 12;
 
 	// PIN lengths
 	private int CRED_PIN_LENGTH = 4;
 	private int CARD_PIN_LENGTH = 6;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		// Pass the list of CredentialPackages on to the ListFragement
 		Intent intent = getIntent();
 		@SuppressWarnings("unchecked")
 		ArrayList<CredentialPackage> credentials = (ArrayList<CredentialPackage>) intent
 				.getSerializableExtra(WaitingForCardActivity.EXTRA_CREDENTIAL_PACKAGES);
 		setCredentials(credentials);
 		
 		@SuppressWarnings("unchecked")
 		ArrayList<LogEntry> logs = (ArrayList<LogEntry>) intent
 				.getSerializableExtra(WaitingForCardActivity.EXTRA_LOG_ENTRIES);
 		setLogs(logs);
 
 		Tag tag = (Tag) intent
 				.getParcelableExtra(WaitingForCardActivity.EXTRA_TAG);
 		setTag(tag);
 
 		cardPin = (String) intent.getSerializableExtra(WaitingForCardActivity.EXTRA_CARD_PIN);
 		cardVersion = (CardVersion) intent.getSerializableExtra(WaitingForCardActivity.EXTRA_CARD_VERSION);
 
 		setContentView(R.layout.activity_credential_list);
 
 		if (findViewById(R.id.credential_detail_container) != null) {
 			// The detail container view will be present only in the
 			// large-screen layouts (res/values-large and
 			// res/values-sw600dp). If this view is present, then the
 			// activity should be in two-pane mode.
 			mTwoPane = true;
 
 			// In two-pane mode, list items should be given the
 			// 'activated' state when touched.
 			((MenuFragment) getSupportFragmentManager()
 					.findFragmentById(R.id.credential_menu_fragment))
 					.setTwoPaneMode(true);
 
 			InitFragment initFragment = new InitFragment();
 			getSupportFragmentManager().beginTransaction()
 					.replace(R.id.credential_detail_container, initFragment)
 					.commit();
 
 			Log.i("blaat", "Simulating initial click!!");
 			((MenuFragment) getSupportFragmentManager()
 					.findFragmentById(R.id.credential_menu_fragment)).simulateListClick(0);
 			
 			// Do not show action bar in two-pane mode
 			getActionBar().hide();
 		}
 
         // NFC stuff
         nfcA = NfcAdapter.getDefaultAdapter(getApplicationContext());
         mPendingIntent = PendingIntent.getActivity(this, 0,
                 new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
 
         // Setup an intent filter for all TECH based dispatches
         IntentFilter tech = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
         mFilters = new IntentFilter[] { tech };
 
         // Setup a tech list for all IsoDep cards
         mTechLists = new String[][] { new String[] { IsoDep.class.getName() } };
 	}
 
     @Override
     public void onResume() {
         super.onResume();
         if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent().getAction())) {
             processIntent(getIntent());
         }
         if (nfcA != null) {
         	nfcA.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
         }
     }
 
     @Override
     public void onPause() {
     	super.onPause();
     	if (nfcA != null) {
     		nfcA.disableForegroundDispatch(this);
     	}
     }
 
     @Override
     public void onNewIntent(Intent intent) {
         Log.i(TAG, "Discovered tag with intent: " + intent);
         setIntent(intent);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	// Menu is only inflated in single pane mode
 
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return !mTwoPane;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
         case R.id.menu_history:
         	onLogSelected();
         	return true;
         case R.id.menu_settings:
         	onSettingsSelected();
         	return true;
         default:
         	return super.onOptionsItemSelected(item);
         }
     }
 
     public void processIntent(Intent intent) {
         tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
     	IsoDep isotag = IsoDep.get(tag);
     	if (isotag != null) {
     		// We are waiting for the card, notify dialog
     		if (currentState == State.WAITING_FOR_CARD) {
     			cardMissingDialog.dismiss();
     			gotoState(State.TEST_CARD_PRESENCE);
     		}
     	}
     }
 
 	private void setCredentials(ArrayList<CredentialPackage> credentials) {
 		this.credentials = credentials;
 	}
 
 	private void setLogs(ArrayList<LogEntry> logs) {
 		this.logs = logs;
 	}
 
 	private void setTag(Tag tag) {
 		this.tag = tag;
 	}
 
 	/**
 	 * Callback method from {@link MenuFragment.Callbacks} indicating
 	 * that the item with the given ID was selected.
 	 */
 	@Override
 	public void onItemSelected(short id) {
 		CredentialPackage credential = null;
 		for(CredentialPackage cp : credentials) {
 			if(cp.getCredentialDescription().getId() == id) {
 				credential = cp;
 			}
 		}
 
 		if (mTwoPane) {
 			// In two-pane mode, show the detail view in this activity by
 			// adding or replacing the detail fragment using a
 			// fragment transaction.
 			Bundle arguments = new Bundle();
 			arguments.putSerializable(CredentialDetailFragment.ARG_ITEM, credential);
 			CredentialDetailFragment fragment = new CredentialDetailFragment();
 			fragment.setArguments(arguments);
 			getSupportFragmentManager().beginTransaction()
 					.replace(R.id.credential_detail_container, fragment)
 					.commit();
 
 		} else {
 			// In single-pane mode, simply start the detail activity
 			// for the selected item ID.
 			Log.i(TAG, "Starting detail activity");
 			Intent detailIntent = new Intent(this,
 					CredentialDetailActivity.class);
 			detailIntent.putExtra(CredentialDetailFragment.ARG_ITEM, credential);
 			startActivityForResult(detailIntent, DETAIL_REQUEST);
 		}
 	}
 	
 	/**
 	 * Callback method from {@link MenuFragment.Callbacks} indicating
 	 * that the log was selected.
 	 */
 	public void onLogSelected() {
 		Log.i("cla", "log selected");
 		if (mTwoPane) {
 			// In two-pane mode, show the detail view in this activity by
 			// adding or replacing the detail fragment using a
 			// fragment transaction.
 			Bundle arguments = new Bundle();
 			arguments.putSerializable(LogFragment.ARG_LOG, logs);
 			LogFragment fragment = new LogFragment();
 			fragment.setArguments(arguments);
 			getSupportFragmentManager().beginTransaction()
 					.replace(R.id.credential_detail_container, fragment)
 					.commit();
 		} else {
 			// In single-pane mode, simply start the detail activity
 			// for the the log
 			Intent logIntent = new Intent(this, LogActivity.class);
 			logIntent.putExtra(LogFragment.ARG_LOG, logs);
 			startActivity(logIntent);
 		}
 	}
 
 	/**
 	 * Callback method from {@link MenuFragment.Callbacks} indicating
 	 * that the settings were selected.
 	 */
 	public void onSettingsSelected() {
 		Log.i("cla", "settings selected");
 		if (mTwoPane) {
 			// In two-pane mode, show the detail view in this activity by
 			// adding or replacing the detail fragment using a
 			// fragment transaction.
 			Bundle arguments = new Bundle();
 			arguments.putSerializable(SettingsFragment.ARG_CARD_VERSION, cardVersion);
 			SettingsFragment fragment = new SettingsFragment();
 			fragment.setArguments(arguments);
 			getSupportFragmentManager().beginTransaction()
 					.replace(R.id.credential_detail_container, fragment)
 					.commit();
 
 		} else {
 			// In single-pane mode, simply start the detail activity
 			// for the selected item ID.
 			Intent settingsIntent = new Intent(this, SettingsActivity.class);
 			settingsIntent.putExtra(SettingsFragment.ARG_CARD_VERSION, cardVersion);
 			startActivityForResult(settingsIntent, SETTINGS_REQUEST);
 		}
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if(requestCode == SETTINGS_REQUEST) {
 			if(resultCode == SettingsActivity.RESULT_CHANGE_CARD_PIN)
 				onChangeCardPIN();
 			else if(resultCode == SettingsActivity.RESULT_CHANGE_CRED_PIN)
 				onChangeCredPIN();
 		} else if(requestCode == DETAIL_REQUEST) {
 			if (resultCode == CredentialDetailActivity.RESULT_DELETE) {
 				CredentialDescription cd = (CredentialDescription) data
 						.getSerializableExtra(CredentialDetailActivity.ARG_RESULT_DELETE);
 				onDeleteCredential(cd);
 			}
 		}
 	}
 
 	public void onCardMissingCancel() {
 		// If cancelled we cannot continue with the action
 		gotoState(State.NORMAL);
 	}
 
 	public void onCardMissingRetry() {
 		// We should retry
 		gotoState(State.TEST_CARD_PRESENCE);
 	}
 
 	@Override
 	public void onPINChange(String old_pin, String new_pin) {
 		this.old_pin = old_pin;
 		this.new_pin = new_pin;
 		gotoState(State.PERFORM_ACTION);
 	}
 
 	@Override
 	public void onPINChangeCancel() {
 		// If cancelled we cannot continue, returning to normal
 		gotoState(State.NORMAL);
 	}
 
 	protected ArrayList<CredentialPackage> getCredentials() {
 		return credentials;
 	}
 
 	public ArrayList<LogEntry> getLogs() {
 		return logs;
 	}
 
 	public CardVersion getCardVersion() {
 		return cardVersion;
 	}
 
 	private void gotoState(State state) {
 		String TAG = "CLA:State";
 
 		//State previousState = currentState;
 		currentState = state;
 
 		switch(state) {
 		case NORMAL:
 			Log.i(TAG, "Returning to default state");
 			break;
 		case TEST_CARD_PRESENCE:
 			Log.i(TAG, "Checking card presence");
 
 			// This is the start of a new action so we should reset some state
 			tries = -1;
 
 			new CheckCardPresentTask(this).execute(tag);
 			break;
 		case WAITING_FOR_CARD:
 			Log.i(TAG, "Going to wait for card");
 			cardMissingDialog = new CardMissingDialogFragment();
 			cardMissingDialog.show(getFragmentManager(), "cardmissing");
 			break;
 		case CONFIRM_ACTION:
 			Log.i(TAG, "Confirming action");
 			ChangePinDialogFragment pinDialog;
 
 			switch(currentAction) {
 			case DELETE_CREDENTIAL:
 				ConfirmDeleteDialogFragment confirmDialog = ConfirmDeleteDialogFragment.getInstance(toBeDeleted);
 				confirmDialog.show(getFragmentManager(), "confirm_delete");
 				break;
 			case CHANGE_CARD_PIN:
 				pinDialog = ChangePinDialogFragment.getInstance("card", tries,
 						new_pin, CARD_PIN_LENGTH);
 				pinDialog.show(getFragmentManager(), "change_card_pin");
 				break;
 			case CHANGE_CREDENTIAL_PIN:
 				pinDialog = ChangePinDialogFragment.getInstance("credential",
 						tries, new_pin, CRED_PIN_LENGTH);
 				pinDialog.show(getFragmentManager(), "change_cred_pin");
 				break;
 			case NONE:
 				Log.i(TAG, "Illegal state, returning to normal");
 				currentState = State.NORMAL;
 				break;
 			}
 
 			break;
 		case ACTION_FAILED:
 			Log.i(TAG, "Action failed");
 			AlertDialogFragment f = AlertDialogFragment.getInstance(
 					"Action Failed", exception.getMessage());
 			f.show(getFragmentManager(), "alert");
 			break;
 		case ACTION_PERFORMED:
 			Log.i(TAG, "Action succeeded");
 			completeAction();
 			gotoState(State.NORMAL);
 			break;
 		case PERFORM_ACTION:
 			Log.i(TAG, "Performing action");
 			runAction();
 			break;
 		}
 	}
 
     private class CheckCardPresentTask extends AsyncTask<Tag, Void, TransmitResult> {
     	private final String TAG = "CheckCardPresentTask";
 
 		protected CheckCardPresentTask(Context context) {
     	}
 
 		@Override
 		protected TransmitResult doInBackground(Tag... arg0) {
 			IsoDep tag = IsoDep.get(arg0[0]);
 
 			// Make sure time-out is long enough (10 seconds)
 			tag.setTimeout(10000);
 
 			IdemixService is = new IdemixService(new IsoDepCardService(tag));
 			TransmitResult result = null;
 
 			try {
 				is.open();
 				is.close();
 				result = new TransmitResult(TransmitResult.Result.SUCCESS);
 			} catch (CardServiceException e) {
 				Log.e(TAG, "Unable to select idemix applet");
 				e.printStackTrace();
 				return new TransmitResult(e);
 			} finally {
 				try {
 					tag.close();
 				} catch (IOException e) {
 					Log.e(TAG, "Failed to close tag connection");
 					e.printStackTrace();
 				}
 			}
 
 			return result;
 		}
 
 		@Override
 		protected void onPostExecute(TransmitResult tresult) {
 			switch(tresult.getResult()) {
 			case FAILURE:
 				gotoState(State.WAITING_FOR_CARD);
 				Log.i(TAG, "Cannot connect to card, proceeding to waiting for card");
 				break;
 			case SUCCESS:
 				gotoState(State.CONFIRM_ACTION);
 				break;
 			default:
 				// Nothing to do?
 				break;
 			}
 		}
     }
 
     private class TransmitAPDUsTask extends AsyncTask<Tag, Void, TransmitResult> {
     	private final String TAG = "TransmitAPDUsTask";
     	private CardProgram cardProgram;
 
 		protected TransmitAPDUsTask(Context context, CardProgram cardProgram) {
     		this.cardProgram = cardProgram;
     	}
 
 		@Override
 		protected TransmitResult doInBackground(Tag... arg0) {
 			IsoDep tag = IsoDep.get(arg0[0]);
 
 			// Make sure time-out is long enough (10 seconds)
 			tag.setTimeout(10000);
 
 			IdemixService is = new IdemixService(new IsoDepCardService(tag));
 			TransmitResult result = null;
 
 			try {
 				is.open();
 
 
 				Log.i(TAG,"Performing requested actions now");
 				result = cardProgram.run(is);
 				is.close();
 				Log.i(TAG, "Performed action succesfully!");
 			} catch (Exception e) {
 				Log.e(TAG, "Reading verification caused exception");
 				e.printStackTrace();
 				return new TransmitResult(e);
 			} finally {
 				try {
 					tag.close();
 				} catch (IOException e) {
 					Log.e(TAG, "Failed to close tag connection");
 					e.printStackTrace();
 				}
 			}
 
 			return result;
 		}
 
 		@Override
 		protected void onPostExecute(TransmitResult tresult) {
 			switch(tresult.getResult()) {
 			case SUCCESS:
 				Log.i(TAG, "Complete succesfully, finishing task");
 				gotoState(State.ACTION_PERFORMED);
 				break;
 			case FAILURE:
 				Log.i(TAG, "Action failed, notifying user");
 				exception = tresult.getException();
 				gotoState(State.ACTION_FAILED);
 				break;
 			case INCORRECT_PIN:
 				Log.i(TAG, "Pincode incorrect, notifying user");
 				tries = tresult.getTries();
 				if(tries > 0) {
 					gotoState(State.CONFIRM_ACTION);
 				} else {
 					exception = new Exception("You have no more pin tries left, the card is now blocked");
 					gotoState(State.ACTION_FAILED);
 				}
 			}
 		}
     }
 
 	@Override
 	public void onConfirmDeleteOK() {
 		gotoState(State.PERFORM_ACTION);
 	}
 
 	@Override
 	public void onConfirmDeleteCancel() {
 		gotoState(State.NORMAL);
 		// If in twoPane mode return to detailed view upon cancel
 		if(!mTwoPane) {
 			onItemSelected(toBeDeleted.getId());
 		}
 	}
 
 	public void onChangeCardPIN() {
 		Log.i(TAG, "Change card PIN called");
 		currentAction = Action.CHANGE_CARD_PIN;
 		gotoState(State.TEST_CARD_PRESENCE);
 	}
 
 	public void onChangeCredPIN() {
 		Log.i(TAG, "Change cred PIN called");
 		currentAction = Action.CHANGE_CREDENTIAL_PIN;
 		gotoState(State.TEST_CARD_PRESENCE);
 	}
 
 	public void runAction() {
 		CardProgram program = null;
 
 		switch (currentAction) {
 		case DELETE_CREDENTIAL:
 			program = new CardProgram() {
 				@Override
 				public TransmitResult run(IdemixService is) throws CardServiceException {
 					is.sendCardPin(cardPin.getBytes());
 					IdemixCredentials ic = new IdemixCredentials(is);
 					ic.removeCredential(toBeDeleted);
 					return new TransmitResult(TransmitResult.Result.SUCCESS);
 				}
 			};
 			break;
 		case CHANGE_CARD_PIN:
 			program = new CardProgram() {
 				@Override
 				public TransmitResult run(IdemixService is) throws CardServiceException {
 					int tries;
 					tries = is.sendCardPin(old_pin.getBytes());
 
 					// Only continu if Card Pin was correct initially
 					if(tries == -1) {
 						tries = is.updateCardPin(old_pin.getBytes(), new_pin.getBytes());
 					}
 
 					if (tries == -1) {
 						return new TransmitResult(TransmitResult.Result.SUCCESS);
 					} else {
 						return new TransmitResult(tries);
 					}
 				}
 			};
 			break;
 		case CHANGE_CREDENTIAL_PIN:
 			program = new CardProgram() {
 				@Override
 				public TransmitResult run(IdemixService is) throws CardServiceException {
 					is.sendCardPin(cardPin.getBytes());
					int tries = is.updateCredentialPin(new_pin.getBytes());
 					if (tries == -1) {
 						return new TransmitResult(TransmitResult.Result.SUCCESS);
 					} else {
 						return new TransmitResult(tries);
 					}
 				}
 			};
 			break;
 		default:
 			// Nothing to do?
 			break;
 		}
 		new TransmitAPDUsTask(this, program).execute(tag);
 	}
 
 	public void completeAction() {
 		// This is run after action completes succesfully
 		switch (currentAction) {
 		case DELETE_CREDENTIAL:
 			Log.i("CLA:completeAction", "Removing item from list");
 
 			// Find deleted package
 			int deletedIdx = -1;
 			for(int i = 0 ; i <  credentials.size(); i++) {
 				CredentialPackage cp = credentials.get(i);
 				System.out.println("Examining credential: " + cp.toString());
 				if(cp.getCredentialDescription().getId() == toBeDeleted.getId()) {
 					deletedIdx = i;
 				}
 			}
 
 			if(deletedIdx == -1) {
 				Log.i("CLA:completeAction", "Failed to locate credential");
 			} else {
 				credentials.remove(deletedIdx);
 			}
 
 			if(mTwoPane) {
 				if(credentials.size() > 0) {
 					int new_idx = deletedIdx > 0 ? deletedIdx - 1 : 0;
 					((MenuFragment) getSupportFragmentManager()
 							.findFragmentById(R.id.credential_menu_fragment)).simulateListClick(new_idx);
 				} else {
 					InitFragment initFragment = new InitFragment();
 					getSupportFragmentManager().beginTransaction()
 							.replace(R.id.credential_detail_container, initFragment)
 							.commit();
 				}
 			} else {
 				((MenuFragment) getSupportFragmentManager()
 						.findFragmentById(R.id.credential_menu_fragment)).updateList();
 			}
 			break;
 		case CHANGE_CARD_PIN:
 			// We need to cache the new PIN now
 			cardPin = new_pin;
 
 			// Return to settings in single-pane mode
 			if(!mTwoPane)
 				onSettingsSelected();
 			break;
 		case CHANGE_CREDENTIAL_PIN:
 			if(!mTwoPane)
 				onSettingsSelected();
 			break;
 		default:
 			// Nothing to do?
 		}
 	}
 
 	public void onDeleteCredential(CredentialDescription cd) {
 		Log.i("blaat", "Delete credential called");
 		toBeDeleted = cd;
 		currentAction = Action.DELETE_CREDENTIAL;
 		Log.i("blaat", "Will delete: " + cd.toString());
 
 		gotoState(State.TEST_CARD_PRESENCE);
 	}
 
 	@Override
 	public void onAlertDismiss() {
 		// Returning to normal state
 		gotoState(State.NORMAL);
 	}
 }
