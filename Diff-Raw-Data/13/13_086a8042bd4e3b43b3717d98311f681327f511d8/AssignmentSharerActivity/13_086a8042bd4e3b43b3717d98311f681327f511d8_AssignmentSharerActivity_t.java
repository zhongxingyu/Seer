 package com.moac.android.opensecretsanta.activity;
 
 import android.accounts.*;
 import android.app.*;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.telephony.SmsManager;
 import android.text.Editable;
 import android.text.InputType;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.*;
 import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
 import com.moac.android.opensecretsanta.R;
 import com.moac.android.opensecretsanta.database.DatabaseManager;
 import com.moac.android.opensecretsanta.mail.GmailOAuth2Sender;
 import com.moac.android.opensecretsanta.types.*;
 import com.moac.drawengine.DrawEngine;
 import com.moac.drawengine.DrawEngineProvider;
 import com.moac.drawengine.DrawFailureException;
 import com.moac.drawengine.InvalidDrawEngineException;
 
 import java.io.IOException;
 import java.util.*;
 
 //import android.content.BroadcastReceiver;
 //import android.content.IntentFilter;
 
 public class AssignmentSharerActivity extends Activity {
 
     final static String TAG = "AssignmentSharerActivity";
 
     //	public static final String SENT_SMS = "com.moac.android.opensecretsanta.SMS_SENT";
     //	public static final String SENT_EMAIL = "com.moac.android.opensecretsanta.EMAIL_SENT";
 
     static final int PROGRESS_DIALOG = 0;
     static final int CONFIRM_SEND_DIALOG = 1;
     static final int DIALOG_ASSIGNMENT = 2;
     static final int CONFIRM_REDRAW_DIALOG = 3;
 
     DrawResult mDrawResult;
     Group mGroup;
 
     DatabaseManager mDatabase;
     DrawEngineProvider mDrawEngineProv;
 
     private List<DrawResultEntry> items;
     private ArrayAdapter<DrawResultEntry> aa;
     ListView mList;
 
     ImageView mShareButton;
     ImageView mRedrawButton;
 
     AlertDialog mViewerDialog;
     ProgressDialog mProgressDialog;
 
     ViewSwitcher mSwitcher;
 
     boolean mSendMultipartSMS;
 
     Account mAccount = null;
     String mToken = null;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.draw_sharer_view);
 
         mDatabase = OpenSecretSantaApplication.getDatabase();
 
         Bundle extras = getIntent().getExtras();
         if(extras != null) {
             long groupId = extras.getLong(Constants.GROUP_ID);
             Log.v(TAG, "onCreate() - got mGroupId: " + groupId);
             mGroup = mDatabase.queryById(groupId, Group.class);
         }
 
         mDrawEngineProv = new DrawEngineProvider();
         mSwitcher = (ViewSwitcher) findViewById(R.id.drawSharerViewSwitcher);
 
         //mDateTextView = (TextView)findViewById(R.id.dateValue);
         mList = (ListView) findViewById(R.id.assignmentsListView);
 
         items = new ArrayList<DrawResultEntry>();
         aa = new AssignmentsListAdapter(this, R.layout.assignment_row, items, new OnClickListener() {
 
             @Override
             public void onClick(View view) {
 
                 Log.v(TAG, "setOnItemClickListener() onItemClick - start");
 
                 DrawResultEntry selectedItem = (DrawResultEntry) view.getTag();
                 // Has been viewed now - so updated the DB (async)
                 updateDrawResultEntry(selectedItem);
 
                 AlertDialog.Builder builder;
 
                 Context mContext = AssignmentSharerActivity.this;
                 LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
                 View layout = inflater.inflate(R.layout.assignee_viewer_dialog,
                   (ViewGroup) findViewById(R.id.layout_root));
 
                 ImageView image = (ImageView) layout.findViewById(R.id.image);
                 image.setImageResource(R.drawable.ic_present);
 
                 TextView text = (TextView) layout.findViewById(R.id.assignmentMemberText);
                 text.setText(selectedItem.getGiverName() + " was assigned - ");
                 TextView text2 = (TextView) layout.findViewById(R.id.assignmentAssigneeText);
                 Log.v(TAG, "onItemClick() - assignee: " + selectedItem.getReceiverName());
                 text2.setText(selectedItem.getReceiverName());
 
                 builder = new AlertDialog.Builder(mContext);
                 builder.setView(layout);
                 mViewerDialog = builder.create();
                 mViewerDialog.show();
             }
         });
         mList.setAdapter(aa);
 
         mShareButton = (ImageView) findViewById(R.id.shareDrawButton);
         mShareButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Log.d(TAG, "Do Share clicked");
                 openShareDialog();
             }
         });
 
         mRedrawButton = (ImageView) findViewById(R.id.redrawButton);
         mRedrawButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Log.d(TAG, "Do Redraw clicked");
                 showDialog(CONFIRM_REDRAW_DIALOG);
             }
         });
 
         //		// SMS Receipt Filter.
         //		IntentFilter attemptedDeliveryfilter = new IntentFilter(SENT_SMS);
         //		registerReceiver(attemptedDeliveryReceiver,
         //				attemptedDeliveryfilter);
 
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
         Resources res = getResources();
         boolean defaultUseMultipartSms = res.getBoolean(R.bool.defaultUseMultipartSms);
         mSendMultipartSMS = prefs.getBoolean("use_multipart_sms", defaultUseMultipartSms);
 
         // Initialise the Draw Engine
         String defaultName = res.getString(R.string.defaultDrawEngine);
         String classname = prefs.getString("engine_preference",
           defaultName);
 
         Log.i(TAG, "initialise() - setting draw engine to: " + classname);
 
         try {
             mDrawEngineProv.setDrawEngine(classname);
         } catch(InvalidDrawEngineException exp) {
             // Hmmm that failed. If we're not using the default name, then try that instead
             if(!classname.equals(defaultName)) {
                 Log.i(TAG, "Failed to initialise draw engine class: " + classname);
                 // Try to set the default then.
                 try {
                     mDrawEngineProv.setDrawEngine(defaultName);
                     // Update pref to use the default.
                     prefs.edit().putString("engine_preference", defaultName).commit();
                 } catch(InvalidDrawEngineException exp2) {
                     Log.e(TAG, "Unable to initialise default draw engine class: " + classname, exp);
                 }
             }
             // Catch don't bail, give use a chance to change.
         }
     }
 
     @Override
     public void onStart() {
         super.onStart();
         mSwitcher.reset();
     }
 
     @Override
     public void onResume() {
         super.onResume();
         Log.v(TAG, "onResume() - start");
         Log.v(TAG, "onResume() mGroupId: " + mGroup.getId());
         Log.v(TAG, "onResume() mResultId: " + (mDrawResult == null ? "null" : mDrawResult.getId()));
         initialiseViewContent();
         Log.v(TAG, "onResume() - end");
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         Log.v(TAG, "onCreateDialog() - start");
 
         Dialog dialog;
         switch(id) {
             case PROGRESS_DIALOG:
                 mProgressDialog = new ProgressDialog(this);
                 mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                 mProgressDialog.setMessage("Notifying members...");
                 return mProgressDialog;
             case CONFIRM_REDRAW_DIALOG:
                 // Confirm redraw of group
                 AlertDialog.Builder redrawConfirmDialog = new AlertDialog.Builder(this);
                 redrawConfirmDialog.setTitle("Redraw assignments?")
                   .setIcon(R.drawable.ic_santa)
                   .setNegativeButton("Cancel", null)
                   .setCancelable(true)
                   .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int id) {
                           executeDraw();
                       }
                   });
                 return redrawConfirmDialog.create();
             default:
                 dialog = null;
         }
         return dialog;
     }
 
     private void initialiseViewContent() {
         // If group is not ready, then switch to the error screen.
         executeDrawIfRequired();
     }
 
     /*
      *
      * This area of code calculates draws if necessary and possible.
      *
      *
      */
     private void executeDrawIfRequired() {
 
         AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
 
             @Override
             protected Boolean doInBackground(Void... params) {
 
                 // Check if the draw is required
 
                 // If we allow deleting of draws, then perhaps
                 // a ready group could exist with no draws.
 
                 // See if there's a draw result already there.
                 mDrawResult = mDatabase.queryLatestDrawResultForGroup(mGroup.getId());
 
                 // Having a latest draw is not good enough - it might not
                 // reflect the current structure of the Group.
                 Log.v(TAG, "executeDrawIfRequired() - isReady: " + mGroup.isReady());
                 // If there's no draw or the group is not ready to share
                 return (mDrawResult == null || !mGroup.isReady());
             }
 
             @Override
             protected void onPostExecute(Boolean doDraw) {
                 if(doDraw) {
                     executeDraw();
                 } else {
                     // We have a valid draw already - so show it.
                     populateAssignmentsList(false);
                 }
             }
         };
 
         task.execute();
     }
 
     private class DrawStatus {
         private String msg = "";
         private boolean successful;
 
         public DrawStatus(boolean successful, String msg) {
             this.successful = successful;
             this.msg = msg;
         }
     }
 
     /**
      * Load contact information on a background thread.
      */
     private void executeDraw() {
 
         AsyncTask<Void, Void, DrawStatus> task = new AsyncTask<Void, Void, DrawStatus>() {
 
             ProgressDialog dialog = null;
             String mStatusMsg = "";
 
             @Override
             protected void onPreExecute() {
                 dialog = ProgressDialog.show(AssignmentSharerActivity.this, "",
                   "Drawing assignments. Please wait...", true);
             }
 
             @Override
             protected DrawStatus doInBackground(Void... params) {
                 mStatusMsg = "Starting...";
                 Log.v(TAG, "performDraw() - doInBackgrounnd()");
 
                 // Build these assignments.
                 Map<Long, Long> assignments = null;
 
                 try {
 
 					/*
                      * TODO This can stil return null, the exception isn't enough.
 					 */
 
                     // Try to generate the draw
                     DrawEngine engine = mDrawEngineProv.getDrawEngine();
 
                     long before = System.currentTimeMillis();
                     // Let's get some database values
                     List<Member> members = mDatabase.queryAllMembersForGroup(mGroup.getId());
                     Log.v(TAG, "performDraw() - Group: " + mGroup.getId() + " has member count: " + members.size());
                     Map<Long, Set<Long>> participants = new HashMap<Long, Set<Long>>();
 
                     for(Member m : members) {
 
                         List<Restriction> restrictions = mDatabase.queryAllRestrictionsForMemberId(m.getId());
                         Set<Long> restrictionIds = new HashSet<Long>();
                         for(Restriction r : restrictions) {
                             restrictionIds.add(r.getOtherMemberId());
                         }
 
                         participants.put(m.getId(), restrictionIds);
 
                         Log.v(TAG, "performDraw() - " + m.getName() + "(" + m.getId() + ") has restrictions: " + restrictions);
                     }
 
                     Log.v(TAG, "performDraw() - Group: " + mGroup.getId() + " has member count: "
                       + participants.size());
 
                     try {
                         assignments = engine.generateDraw(participants);
                         long after = System.currentTimeMillis();
                         Log.v(TAG, "Assignments size: " + assignments.size());
 
                         // TODO Could arguably do some further checks here to make sure the draw is ok.
                         // i.e. don't rely on the engine implementation to check.
                         mStatusMsg = "Draw was successful! Took " + (after - before) + "ms";
 
                         // Now write the DRE
                         saveDrawResult(assignments, mGroup);
 
                         // Successful draw, so update the group flag.
                         mGroup.setReady(true);
                         mDatabase.update(mGroup);
 
                         // Return the successful result indication.
                         return new DrawStatus(true, mStatusMsg);
 
                     } catch(DrawFailureException e) {
                         // Not necessarily an error. But log it - in just it is.
                         Log.w(TAG, "Couldn't produce assignments", e);
                         mStatusMsg = e.getMessage();
                     }
                 } catch(InvalidDrawEngineException drex) {
                     Log.e(TAG, "No Draw Engine available to create draw");
                     mStatusMsg = "Oops, can't build a draw without an engine";
                 }
 
                 // Failed to draw - not ready to share.
                 mGroup.setReady(false);
                 mDatabase.update(mGroup);
 
                 return new DrawStatus(false, mStatusMsg);
             }
 
             @Override
             protected void onPostExecute(DrawStatus status) {
                 Log.v(TAG, "performDraw() - onPostExecute() ");
                 dialog.cancel();
 
                 // This happens in the GUI thread - so these operations are safe.
                 if(status.successful) {
                     // So populate the assignment list with the values (done in background).
                     populateAssignmentsList(true);
                 } else {
                     // No assignments; so switch to the error panel.
                     mSwitcher.setDisplayedChild(0);
                    if(status.msg != null && !(status.msg.length() > 0)) {
                         Toast.makeText(getBaseContext(), status.msg, Toast.LENGTH_SHORT).show();
                     }
                 }
             }
         };
 
         task.execute();
     }
 
     /*
      * Should be called in a background thread.
      */
     protected long saveDrawResult(Map<Long, Long> _assignments, Group _group) {
 
         Log.v(TAG, "saveDrawResult() - start");
         Log.v(TAG, "saveDrawResult() - length: " + _assignments.size());
 
         DrawResult dr = new DrawResult();
         dr.setDrawDate(System.currentTimeMillis());
         dr.setGroup(_group);
         mDatabase.update(dr);
 
         // Ok translate the member ids (in the assignments) into their names
         long id = mDatabase.create(dr);
 
         for(Long m1Id : _assignments.keySet()) {
             // Now add the corresponding draw result entries.
 
             // We are notifying m1 that they have been assigned m2.
             // => so we use m1's details and send them m2's name.
             Member m1 = mDatabase.queryById(m1Id, Member.class);
 
             String name2 = mDatabase.queryById(_assignments.get(m1Id), Member.class).getName();
             Log.v(TAG, "saveDrawResult() - saving dre: " + m1.getName() + " - " + name2 + " with: " + m1.getContactMode() + " " + m1.getContactDetail());
 
             DrawResultEntry dre = new DrawResultEntry();
             dre.setGiverName(m1.getName());
             dre.setReceiverName(name2);
             dre.setContactMode(m1.getContactMode());
             dre.setContactDetail(m1.getContactDetail());
             dre.setDrawResult(dr);
             mDatabase.create(dre);
         }
 
         Log.v(TAG, "saveDrawResult() - inserted DrawResult: " + id);
 
         return id;
     }
 
     private static class DrawResultDetails {
         DrawResult dr;
         List<DrawResultEntry> dres = new ArrayList<DrawResultEntry>();
     }
 
     private void populateAssignmentsList(final boolean isNewDraw) {
 
         AsyncTask<Void, Void, DrawResultDetails> task = new AsyncTask<Void, Void, DrawResultDetails>() {
 
             @Override
             protected DrawResultDetails doInBackground(Void... params) {
 
                 DrawResultDetails drDetails = null;
 
                 Log.v(TAG, "populateAssignmentsList() - populating assignments list");
 
                 // Always get the latest result - there might not be one?
                 mDrawResult = mDatabase.queryLatestDrawResultForGroup(mGroup.getId());
 
                 // Really just a safe guard, in case this is called after a FAILED draw (which it shouldn't)
                 if(mDrawResult == null)
                     return null;
 
                 // Populate
                 drDetails = new DrawResultDetails();
                 drDetails.dr = mDrawResult;
                 drDetails.dres = mDatabase.queryAllDrawResultEntriesForDrawId(mDrawResult.getId());
                 Collections.sort(drDetails.dres);
 
                 Log.v(TAG, "populateAssignmentsList() - row count: " + drDetails.dres.size());
 
                 return drDetails;
             }
 
             @Override
             protected void onPostExecute(DrawResultDetails drDetails) {
 
                 boolean success = (drDetails != null);
 
                 Log.v(TAG, "onPostExecute() success: " + success);
 
                 if(success) {
                     items = drDetails.dres;
 
                     // Enable/Disable the share button if appropriate
                     aa.clear();
                     for(DrawResultEntry dre : drDetails.dres) {
                         aa.add(dre);
                     }
                     Log.v(TAG, "onPostExecute() rows: " + drDetails.dres.size());
                     aa.notifyDataSetChanged();
 
                     // Enable Share only if appropriate
                     boolean isShareable = Utilities.containsSendableEntry(drDetails.dres);
                     setShareEnabled(isShareable);
 
                     mSwitcher.setDisplayedChild(1);
 
                     if(isNewDraw) {
                         // Display the appropriate toast message.
                         Toast ImageToast = new Toast(getBaseContext());
                         LinearLayout toastLayout = new LinearLayout(getBaseContext());
                         toastLayout.setOrientation(LinearLayout.HORIZONTAL);
                         ImageView image = new ImageView(getBaseContext());
 
                         if(isShareable) {
                             image.setImageResource(R.drawable.successful_draw_toast_with_notify);
                         } else {
                             image.setImageResource(R.drawable.successful_draw_toast_no_notify);
                         }
 
                         toastLayout.addView(image);
                         ImageToast.setView(toastLayout);
                         ImageToast.setDuration(Toast.LENGTH_LONG);
                         ImageToast.show();
                     }
                 } else {
                     // I don't expect this to happen...
                     Log.e(TAG, "onPostExecute() Failed to get entries! gid: " + mGroup.getId()
                       + " rid: " + mDrawResult.getId());
                     mSwitcher.reset();
                     items.clear();
                     aa.clear();
                     aa.notifyDataSetChanged();
                 }
             }
         };
 
         task.execute();
     }
 
     private void setShareEnabled(boolean _enabled) {
         mShareButton.setVisibility(_enabled ? View.VISIBLE : View.GONE);
     }
 
     private void updateDrawResultEntry(DrawResultEntry assignment) {
 
         final DrawResultEntry assign = assignment;
 
         AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
 
             @Override
             protected Void doInBackground(Void... params) {
 
                 assign.setViewedDate(System.currentTimeMillis());
                 mDatabase.update(assign);
 
                 // Creates another background thread.
                 populateAssignmentsList(false);
 
                 return null;
             }
         };
 
         task.execute();
     }
 
     /*
      * Returns the number of SMS messages sent.
      */
     private int sendSms(String to, String txt, boolean multipart) {
 
         Log.v(TAG, "sendSMS() - sending to: " + to);
         Log.v(TAG, "sendSMS() - sending msg: " + txt);
 
         // TODO What about the encoding types?
 
 		/*
          * See Professional Android 2 Development. pg402
 		 */
         SmsManager smsManager = SmsManager.getDefault();
 
         // Split long messages
         ArrayList<String> messages = smsManager.divideMessage(txt);
 
         Log.v(TAG, "sendSMS() - divided into: " + messages.size());
 
         Intent sentIntent = new Intent("SENT_SMS_ACTION");
         PendingIntent sentPI = PendingIntent.getBroadcast(getApplicationContext(), 0, sentIntent, 0);
 
         // Some carriers don't support multipart SMS, so that's configurable.
         if(multipart) {
             Log.v(TAG, "sendSMS() - sending multipart message: " + messages.size());
 
             // Build the multipart SMS before sending.
             ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
             for(int i = 0; i < messages.size(); i++) {
                 sentIntents.add(sentPI);
             }
             smsManager.sendMultipartTextMessage(to, null, messages, sentIntents, null);
         } else {
             Log.v(TAG, "sendSMS() - sending multiple single messages: " + messages.size());
 
             // Just iterate manually.
             for(String msg : messages) {
                 smsManager.sendTextMessage(to, null, msg, sentPI, null);
             }
         }
         Log.v(TAG, "sendSMS() - end");
 
         // TODO MIME TYPES ARE CONFUSING IT - not sure if people get billed for 2 sms for 1 message with say french/english content
         // easier to say just sent 1. but hmmmm.
         return messages.size();
     }
 
     private void sendEmail(String to, String body, String subject, String from, String token) throws Exception {
 
         Log.v(TAG, "sendEmail(): to: " + to);
         GmailOAuth2Sender sender = new GmailOAuth2Sender();
         sender.sendMail(subject, body, from,
           token, to);
     }
 
     @Override
     protected void onPrepareDialog(int id, Dialog dialog) {
         switch(id) {
             case PROGRESS_DIALOG:
                 mProgressDialog.setProgress(0);
                 // TODO this is prob risky because that value might change under us.
                 mProgressDialog.setMax(Utilities.getShareableCount(items));
         }
     }
 
     private void openShareDialog() {
 
         AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
 
             @Override
             protected void onPostExecute(Boolean isAuth) {
 
                 if(!isAuth) {
                     // Sorry - got
                     // Take the values and populate the dialog
                     AlertDialog.Builder builder = new AlertDialog.Builder(AssignmentSharerActivity.this);
                     builder.setTitle("No Gmail Account Found");
                     builder.setMessage("Please check that your phone has a Gmail account configured");
                     builder.setPositiveButton("OK", null);
                     AlertDialog alert = builder.create();
                     alert.setOwnerActivity(AssignmentSharerActivity.this);
                     alert.show();
                 } else {
 
                     String message = mDrawResult.getMessage();
 
                     Log.v(TAG, "openShareDialog() existing msg: " + message);
 
                     // Take the values and populate the dialog
                     AlertDialog.Builder builder = new AlertDialog.Builder(AssignmentSharerActivity.this);
                     builder.setTitle("Notify Group");
                     builder.setIcon(R.drawable.ic_mailbox);
 
                     // Create the layout - add the callback to update the length.
                     LinearLayout dialogContents = new LinearLayout(AssignmentSharerActivity.this);
                     String inflator = Context.LAYOUT_INFLATER_SERVICE;
                     LayoutInflater vi = (LayoutInflater) AssignmentSharerActivity.this.getSystemService(inflator);
                     vi.inflate(R.layout.message_view, dialogContents, true);
                     builder.setView(dialogContents);
 
                     // Add the callback to the field
                     final EditText msgField = (EditText) dialogContents.findViewById(R.id.messageTxtEditText);
                     msgField.setInputType(msgField.getInputType() | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                     msgField.setText(message);
 
                     final TextView charCountView = (TextView) dialogContents.findViewById(R.id.msg_char_count);
                     charCountView.setText(String.valueOf(msgField.length()));
 
                     Log.v(TAG, "openShareDialog() msgField: " + msgField);
                     Log.v(TAG, "openShareDialog() charCountView: " + charCountView);
 
                     msgField.addTextChangedListener(new TextWatcher() {
                         @Override
                         public void onTextChanged(CharSequence s, int start, int before, int count) {}
 
                         @Override
                         public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 
                         @Override
                         public void afterTextChanged(Editable s) {
                             // Update the reported character length
                             charCountView.setText(String.valueOf(s.length()));
                         }
                     });
 
                     builder.setIcon(android.R.drawable.ic_dialog_email);
                     builder.setCancelable(true);
                     builder.setNegativeButton("Cancel", null);
                     builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             shareAllAssignments(msgField.getText().toString());
                             dialog.dismiss();
                         }
                     });
                     AlertDialog alert = builder.create();
                     alert.setOwnerActivity(AssignmentSharerActivity.this);
                     alert.show();
                 }
             }
 
             @Override
             protected Boolean doInBackground(Void... voids) {
                 boolean authRequired = Utilities.containsEmailSendableEntry(mDatabase.queryAllDrawResultEntriesForDrawId(mDrawResult.getId()));
                 // Reset.
                 mToken = null;
                 mAccount = null;
                 if(authRequired) {
                     try {
                         mAccount = ((OpenSecretSantaApplication) getApplication()).getAvailableGmailAccount();
                         if(mAccount != null) {// Now let's get the token
                             AccountManagerFuture<Bundle> authTokenBundle = AccountManager.get(AssignmentSharerActivity.this).
                              getAuthToken(mAccount, Constants.GMAIL_TOKEN_TYPE, null, null, null, null);
                             mToken = authTokenBundle.getResult().getString(AccountManager.KEY_AUTHTOKEN);
                         }
                     } catch(Exception e) {
                         Log.e(TAG, "openShareDialog() - Exception when obtaining authToken", e);
                     }
                 }
                 return authRequired ? mToken != null : true;
             }
         };
 
         task.execute();
     }
 
     private void shareAllAssignments(final String msg) {
 
         AsyncTask<Void, Integer, ShareResults> task = new AsyncTask<Void, Integer, ShareResults>() {
 
             @Override
             protected void onPreExecute() {
                 // Start the end progress dialog.
                 showDialog(PROGRESS_DIALOG);
             }
 
             @Override
             protected ShareResults doInBackground(Void... params) {
                 Log.v(TAG, "shareAllAssignments() - start");
                 ShareResults results = new ShareResults();
 
                 // Update the details before starting.
                 mDrawResult.setSendDate(System.currentTimeMillis());
                 mDrawResult.setMessage(msg);
                 mDatabase.update(mDrawResult);
 
                 Log.v(TAG, "Updating DR: " + mDrawResult.getId() + "," + mDrawResult.getDrawDate() + "," + mDrawResult.getSendDate() + "," + mDrawResult.getMessage());
 
                 // Currently 3 contact modes
                 // - None (are ignored)
                 // - Email
                 // - SMS
 
                 // Is better to prevent failures, than to send out some SMSs
                 // then fail on one... much confusion would result.
                 List<DrawResultEntry> drawEntries = mDatabase.queryAllDrawResultEntriesForDrawId(mDrawResult.getId());
 
                 // Ok, Iterate through these objects.
                 for(DrawResultEntry entry : drawEntries) {
                     try {
 
                         Log.v(TAG, "Sharing using mode: " + entry.getContactMode());
                         if(entry.getContactMode() == Constants.NAME_ONLY_CONTACT_MODE) {
                             // Manual! no sending... (expect one less)
                             Log.v(TAG, "shareAllAssignments() - manual entry");
                         } else if(entry.getContactMode() == Constants.EMAIL_CONTACT_MODE) {
                             Log.v(TAG, "shareAllAssignments() - email entry");
 
                             // Email-ru
                             sendEmail(entry.getContactDetail(),
                               Utilities.buildPersonalisedMsg(msg, entry.getGiverName(), entry.getReceiverName()), "Your Secret Santa assignment", mAccount.name, mToken);
                         } else if(entry.getContactMode() == Constants.SMS_CONTACT_MODE) {
                             Log.v(TAG, "shareAllAssignments() - SMS entry");
 
                             // SMS
                             results.sentSMSCount += sendSms(entry.getContactDetail(),
                               Utilities.buildPersonalisedMsg(msg, entry.getGiverName(), entry.getReceiverName()),
                               mSendMultipartSMS);
                         } else {
                             // We didn't get a known contact mode - so fail.
                             results.failedMembers.add(entry);
                         }
 
                         // Update for those entries that are sendable.
                         if(entry.isSendable()) {
                             Log.v(TAG, "Marking as sent: " + entry.getGiverName() + " at: " + System.currentTimeMillis());
                             entry.setSentDate(System.currentTimeMillis());
                             mDatabase.update(entry);
                         }
                     } catch(Exception exp) {
                         // TODO This is wrong
                         Log.i(TAG, "Failed to send entry: " + entry.getGiverName() + " (" + entry.getContactDetail() + ")" + " (" + entry.getContactMode() + ")" + exp);
                         results.failedMembers.add(entry);
                     } finally {
                         // Always do this - ensure dialog gets to 100% (otherwise looks weird).
                         if(entry.isSendable())
                             results.sentRecipientCount++;
 
                         publishProgress(results.sentRecipientCount);
                     }
                 }
 
                 Log.v(TAG, "shareAllAssignments() - end");
 
                 return results;
             }
 
             @Override
             protected void onProgressUpdate(Integer... progress) {
                 mProgressDialog.setProgress(progress[0].intValue());
             }
 
             @Override
             protected void onPostExecute(ShareResults _result) {
 
                 removeDialog(PROGRESS_DIALOG);
 
                 if(_result.failedMembers.size() > 0) {
                     // Something went wrong
                     AlertDialog.Builder builder = new AlertDialog.Builder(AssignmentSharerActivity.this);
                     builder.setTitle("Notification Error");
                     builder.setIcon(android.R.drawable.ic_dialog_alert);
                     builder.setMessage(Utilities.buildSharedErrorMessage(_result.failedMembers))
                       .setCancelable(false)
                       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int id) {
                               dialog.cancel();
                           }
                       });
                     AlertDialog alert = builder.create();
                     alert.show();
                 } else {
                     Toast.makeText(AssignmentSharerActivity.this, Utilities.buildSuccessMessage(_result), Toast.LENGTH_LONG).show();
                 }
 
                 // Updated the sent dates (probably), so need to redraw.
                 // TODO Might not need to do this if we make the dates updated on the send receipt.
                 populateAssignmentsList(false);
             }
         };
 
         task.execute();
     }
 
     //	/*
     //	 * TODO Make this work - Send stuff to notifications.
     //	 */
     //	private BroadcastReceiver attemptedDeliveryReceiver = new
     //	BroadcastReceiver() {
     //		@Override
     //		public void onReceive(Context _context, Intent _intent) {
     //			Log.v(TAG, "onReceive() - start");
     //
     //			if (_intent.getAction().equals(SENT_SMS)
     //					|| _intent.getAction().equals(SENT_EMAIL)) {
     //				if (getResultCode() != Activity.RESULT_OK) {
     //					String recipient = _intent.getStringExtra("recipient");
     //					//requestReceived(recipient);
     //					// This will be totally async, so won't get
     //					// Alternatively update the members list.
     //					// or just say count + 1 / total sent.
     //					Log.v(TAG, "onReceive() - got SMS receipt for: " + recipient);
     //
     //				}
     //			}
     //			Log.v(TAG, "onReceive() - end");
     //
     //		}
     //	};
 }
