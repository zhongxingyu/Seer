 package com.example.ContextRequesterApp;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.*;
 import android.os.Process;
 import android.text.InputType;
 import android.util.Log;
 import android.util.SparseBooleanArray;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.*;
 import org.json.JSONObject;
 
 import java.util.ArrayList;
 
 public class ContextRequestAct extends Activity implements View.OnClickListener {
     // declare variables
     private Button bInitServiceBtn;
     private Button bUnbindServiceBtn;
     private Button bLaunchContextActBtn;
     private Button bStartClassifierBtn;
     private Button bLaunchTrainingActBtn;
     private Button bResetRequestBtn;
     private Button bCloseAppBtn;
 
     private ListView bFeatureSelection;
     private Spinner bClassifierSelection;
     private ListView bLabelsSelection;
     private EditText bPeriodText;
 
     boolean ServiceIsBound;
 
     // variables for selecting inputs, these need to be initialized by querying service
     private String[] allowedFeatures;
     private String[] allowedLabels;
     private String[] allowedAlgorithms;
 
     // initialize variables needed for training data
     private ArrayList<String> featuresToUse=null;
     private ArrayList<String> contextLabels=null;
     private String TrainingFileName=null;
     private String TrainContextGroup=null;
     private Bundle bundleServiceInput = new Bundle();
     private Intent InputIntent;
     private JSONObject jsonInput;
     private Messenger ReturnMessenger;
     Messenger serviceMessenger;
     Message OutMessage;
 
     // Define output labels and strings
     public static String ACTION_KEY = "action";
     public static String INIT_ACTION = "init";
     public static String START_ACTION = "start";
     public static String TRAIN_ACTION = "train";
     public static String CLASSIFY_ACTION = "classify";
     public static String LABELS_KEY = "contextLabels";
     public static String FEATURES_KEY = "features";
     public static String ALGORITHM_KEY = "algorithm";
     public static String LABEL_RATE_KEY = "rate";
     public static String MESSENGER_KEY = "MESSENGER";
 
     // Define return message values
     static String LABEL_KEY = "label";
     static String CLASSIFIER_ID_KEY = "classifierID";
     static String MESSAGE_KEY = "message";
     static String RETURNVAL_KEY = "return";
     static int INIT_RETURN_VALUE = 2;
     static int NOM_RETURN_VALUE = 0;
     static int MESSAGE_RETURN_VALUE = 1;
     static int CLASSIFIER_EXISTS_NOT_RUNNING = 3;
     static int CLASSIFIER_RUNNING = 4;
 
     // Variables for saving classifier to run
     // todo - eventually add more classifiers to run at once, for now get one classifier working
     private int classifierID1 = -1; //initialize to a nonvalid number
 
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         // Initialize buttons
 
         bInitServiceBtn = (Button)findViewById(R.id.btnInitService);
         bInitServiceBtn.setOnClickListener(this);
         bInitServiceBtn.requestFocus();
 //
 //        bUnbindServiceBtn = (Button)findViewById(R.id.btnUnbindService);
 //        bUnbindServiceBtn.setOnClickListener(this);
 
         bLaunchContextActBtn = (Button)findViewById(R.id.btnGetContext);
         bLaunchContextActBtn.setOnClickListener(this);
         bLaunchContextActBtn.setEnabled(false);
 
         bLaunchTrainingActBtn = (Button)findViewById(R.id.btnTrainData);
         bLaunchTrainingActBtn.setOnClickListener(this);
         bLaunchTrainingActBtn.setEnabled(false);
 
         bResetRequestBtn = (Button)findViewById(R.id.btnResetID);
         bResetRequestBtn.setOnClickListener(this);
         bResetRequestBtn.setEnabled(false);
 
         // Add button for starting classifier if not running already
         bStartClassifierBtn = (Button)findViewById(R.id.btnStartClassify);
         bStartClassifierBtn.setOnClickListener(this);
         bStartClassifierBtn.setEnabled(false);
 
         bPeriodText = (EditText)findViewById(R.id.classifier_period);
         bPeriodText.setInputType(InputType.TYPE_CLASS_NUMBER);
         this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 
         bCloseAppBtn = (Button)findViewById(R.id.btnCloseApp);
         bCloseAppBtn.setOnClickListener(this);
 
         bClassifierSelection = (Spinner) findViewById(R.id.classifier_spinner);
 
         bLabelsSelection = (ListView) findViewById(R.id.labels_list);
         bLabelsSelection.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
         bFeatureSelection = (ListView) findViewById(R.id.features_list);
         bFeatureSelection.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
 
         // Setup inputs and intents for calling service
         bundleServiceInput = new Bundle();
         InputIntent = new Intent("org.jingbling.ContextEngine.ContextService");
         // Create a new Messenger for the communication back
         ReturnMessenger = new Messenger(handler);
         OutMessage = Message.obtain();
         OutMessage.replyTo=ReturnMessenger;
 
         ServiceIsBound = false;
     }
 
 
     private ServiceConnection appServiceConn = new ServiceConnection() {
         public void onServiceConnected(ComponentName className,
                                        IBinder service) {
             // This is called when the connection with the service has been
             // established, giving us the service object we can use to
             // interact with the service.  We are communicating with our
             // service through an IDL interface, so get a client-side
             // representation of that from the raw service object.
             serviceMessenger = new Messenger(service);
             // On bind, send init data request
             bundleServiceInput.putString(ACTION_KEY,INIT_ACTION);
             OutMessage.setData(bundleServiceInput);
             try {
                 serviceMessenger.send(OutMessage);
             } catch (RemoteException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
 
 
         }
 
         public void onServiceDisconnected(ComponentName className) {
             // This is called when the connection with the service has been
             // unexpectedly disconnected -- that is, its process crashed.
             serviceMessenger = null;
         }
     };
 
 
     @Override
     public void onClick(View v) {
         switch ( v.getId() ) {
             case R.id.btnInitService:
                 // initialize values for requesting service classification
                 // This also binds to service if not bound already
                 if (ServiceIsBound==false) {
                     bindService(new Intent("org.jingbling.ContextEngine.ContextService"), appServiceConn, Context.BIND_AUTO_CREATE);
                     ServiceIsBound=true;
                 }
 
 //                InputIntent.putExtras(bundleServiceInput);
 //                startService(InputIntent);
 
                 break;
             case R.id.btnStartClassify:
                 // initialize values for requesting service classification
                 bundleServiceInput.putString(ACTION_KEY, START_ACTION);
                 bundleServiceInput.putInt(CLASSIFIER_ID_KEY, classifierID1);
                 // Specify desired period, convert to ms
                 // if text is empty, prompt user
                 Log.d("START_CLASSIFY_BTN", "Period: "+bPeriodText.getText().toString());
                 if (bPeriodText.getText().toString().equals("")){
                     Toast.makeText(getApplicationContext(),
                             "ENTER MIN PERIOD FOR CLASSIFIER", Toast.LENGTH_LONG)
                             .show();
                     break;
                 }
                 int desiredPeriod = Integer.parseInt(bPeriodText.getText().toString())*1000;
                 // if period is zero, specify default of 1 second
                 if (desiredPeriod<=0) {
                     desiredPeriod = 1000;
                 }
                 Log.d("CLASSIFY_BTN","parsed Period for classifier labels:"+desiredPeriod);
                 bundleServiceInput.putInt(LABEL_RATE_KEY,desiredPeriod);
                 OutMessage.setData(bundleServiceInput);
                 try {
                     serviceMessenger.send(OutMessage);
                 } catch (RemoteException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
 //                InputIntent.putExtras(bundleServiceInput);
 //                startService(InputIntent);
 
                 break;
 //            case R.id.btnBindService:
 ////                bindService(new Intent(IContextService.class.getName()),
 ////                        appServiceConn, Context.BIND_AUTO_CREATE);
 ////                bInitServiceBtn.setEnabled(false);
 ////                bLaunchContextActBtn.setEnabled(true);
 ////                bLaunchTrainingActBtn.setEnabled(true);
 ////                bUnbindServiceBtn.setEnabled(true);
 //                break;
 //            case R.id.btnUnbindService:
 ////                unbindService(appServiceConn);
 ////                bInitServiceBtn.setEnabled(true);
 ////                bLaunchContextActBtn.setEnabled(false);
 ////                bLaunchTrainingActBtn.setEnabled(false);
 ////                bUnbindServiceBtn.setEnabled(false);
 //                break;
             case R.id.btnGetContext:
                 // Pass intent to service
                 // first save default set of inputs
                 bundleServiceInput=bundleInputs();
                 //then add ones specific to this button
                 bundleServiceInput.putString(ACTION_KEY,CLASSIFY_ACTION);
                 bundleServiceInput.putInt(CLASSIFIER_ID_KEY, classifierID1);
 
                 OutMessage.setData(bundleServiceInput);
                 try {
                     serviceMessenger.send(OutMessage);
                 } catch (RemoteException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
 
 //                InputIntent.putExtras(bundleServiceInput);
 //                startService(InputIntent);
 
                 break;
             case R.id.btnTrainData:
 
                 // first save default set of inputs
                 bundleServiceInput=bundleInputs();
                 //then add ones specific to this button
 //                Log.d("GETContextButton", jsonInput.toString());
                 bundleServiceInput.putString(ACTION_KEY,TRAIN_ACTION);
 
                 OutMessage.setData(bundleServiceInput);
                 try {
                     serviceMessenger.send(OutMessage);
                 } catch (RemoteException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
 //
 //                InputIntent.putExtras(bundleServiceInput);
 //                startService(InputIntent);
 
                 break;
             case R.id.btnResetID:
                 // allow resetting request by clearing the current classifier ID
                 classifierID1 = -1;
                 Toast.makeText(getApplicationContext(),
                         "Classifier ID reset, request context again to set", Toast.LENGTH_LONG)
                         .show();
                break;
             case R.id.btnCloseApp:
                 Process.killProcess(Process.myPid());
                 // if bound, unbind
                 if (ServiceIsBound) {
                     unbindService(appServiceConn);
                     ServiceIsBound = false;
                 }
                 bInitServiceBtn.setEnabled(true);
                 bLaunchContextActBtn.setEnabled(false);
                 bLaunchTrainingActBtn.setEnabled(false);
                 break;
 
         }
     }
 
     private Bundle bundleInputs() {
         // this subroutine bundles the required inputs for calling the classifier and returns it
         Bundle bundleToSend = new Bundle();
         bundleToSend.putString(ALGORITHM_KEY,bClassifierSelection.getSelectedItem().toString());
         // save features checked
         featuresToUse = new ArrayList<String>();
         SparseBooleanArray checked = new SparseBooleanArray();
 //        checked.clear();
         checked = bFeatureSelection.getCheckedItemPositions();
         for (int i = 0; i < bFeatureSelection.getCount(); i++)
         {
             //added if statement to check for true. The SparseBooleanArray
             //seems to maintain the keys for the checked items, but it sets
             //the value to false. Adding a boolean check returns the correct result.
             if(checked.valueAt(i)) {
                 featuresToUse.add(featuresToUse.size(), allowedFeatures[checked.keyAt(i)]);
             }
         }
 
         bundleToSend.putStringArrayList(FEATURES_KEY, featuresToUse);
 
         // repeat for labels
         contextLabels = new ArrayList<String>();
         checked.clear();
         checked = bLabelsSelection.getCheckedItemPositions();
         for (int i = 0; i < bLabelsSelection.getCount(); i++)
         {
             //added if statement to check for true. The SparseBooleanArray
             //seems to maintain the keys for the checked items, but it sets
             //the value to false. Adding a boolean check returns the correct result.
             if(checked.valueAt(i)) {
                 contextLabels.add(contextLabels.size(), allowedLabels[checked.keyAt(i)]);
             }
         }
 
         bundleToSend.putStringArrayList(LABELS_KEY, contextLabels);
 
         return bundleToSend;
     }
 
     private Handler handler = new Handler() {
         public void handleMessage(Message message) {
             Bundle output = message.getData();
             if (output != null) {
                 if (output.getInt(RETURNVAL_KEY) == NOM_RETURN_VALUE) {
                     Toast.makeText(getApplicationContext(),
                             "Classified: " + output.getString(LABELS_KEY), Toast.LENGTH_LONG)
                             .show();
                 } else if (output.getInt(RETURNVAL_KEY) == INIT_RETURN_VALUE) {
                     // if return value is 2, this is an initialization bundle
                     allowedFeatures =output.getStringArray("allowedFeatures");
                     allowedLabels=output.getStringArray("allowedLabels");
                     allowedAlgorithms=output.getStringArray("allowedAlgorithms");
                     String returnedMessage = output.getString(MESSAGE_KEY);
 
                     // set lists with return values
                     ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(getApplicationContext(),
                             android.R.layout.simple_spinner_dropdown_item, allowedAlgorithms);
                     bClassifierSelection.setAdapter(spinnerArrayAdapter);
 
                     ArrayAdapter featuresListAdapter = new ArrayAdapter(getApplicationContext(),
                             android.R.layout.simple_list_item_checked, allowedFeatures);
                     bFeatureSelection.setAdapter(featuresListAdapter);
                     ArrayAdapter labelsListAdapter = new ArrayAdapter(getApplicationContext(),
                             android.R.layout.simple_list_item_checked, allowedLabels);
                     bLabelsSelection.setAdapter(labelsListAdapter);
 
                     //after initializing input values, allow context and training mode to be selected
                     bLaunchContextActBtn.setEnabled(true);
                     bLaunchTrainingActBtn.setEnabled(true);
 
                     Toast.makeText(getApplicationContext(),
                             returnedMessage, Toast.LENGTH_LONG)
                             .show();
                 } else if  (output.getInt(RETURNVAL_KEY) == CLASSIFIER_EXISTS_NOT_RUNNING){
                     // print message that classifier needs to be run, and enable start classifier button
                     classifierID1 = output.getInt(CLASSIFIER_ID_KEY);
                     if (classifierID1 >= 0){
                         bStartClassifierBtn.setEnabled(true);
                         bLaunchContextActBtn.setEnabled(false);
                     }
                     String returnedMessage = output.getString(MESSAGE_KEY);
                     Toast.makeText(getApplicationContext(),
                             returnedMessage, Toast.LENGTH_LONG)
                             .show();
 
                 } else if  (output.getInt(RETURNVAL_KEY) == CLASSIFIER_RUNNING){
                     // classifier is currently running, so get classifier integer identifier to request labels
                     classifierID1 = output.getInt(CLASSIFIER_ID_KEY);
                     bLaunchContextActBtn.setEnabled(true);
                     bStartClassifierBtn.setEnabled(false);
                     bResetRequestBtn.setEnabled(true);
                     String returnedMessage = output.getString(MESSAGE_KEY);
                     Toast.makeText(getApplicationContext(),
                             returnedMessage, Toast.LENGTH_LONG)
                             .show();
 
                 }else {
                     // classifier was not found or other error, print return message
                     String returnedMessage = output.getString(MESSAGE_KEY);
                     Toast.makeText(getApplicationContext(),
                             returnedMessage, Toast.LENGTH_LONG)
                             .show();
                 }
             } else {
                 Toast.makeText(getApplicationContext(), "Classification failed",
                         Toast.LENGTH_LONG).show();
             }
 
         };
     };
     @Override
     public void onDestroy() {
         super.onDestroy();
     }
 
 }
