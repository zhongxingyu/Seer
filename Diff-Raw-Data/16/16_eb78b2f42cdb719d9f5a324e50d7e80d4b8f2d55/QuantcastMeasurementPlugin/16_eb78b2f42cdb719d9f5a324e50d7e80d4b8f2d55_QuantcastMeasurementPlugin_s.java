 package com.quantcast.phonegapplugin;
 
 import com.quantcast.measurement.service.QuantcastClient;
import org.apache.cordova.api.CallbackContext;
import org.apache.cordova.api.CordovaPlugin;
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import java.util.Arrays;
 
 public class QuantcastMeasurementPlugin extends CordovaPlugin {
 
     private static final String VERSION_LABEL = "_sdk.phonegap.android.v110";
     @Override
     public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
         boolean retval = true;
         if (action.equals("beginMeasurementSession")) {
             String apiKey = args.getString(0);
             String userId = args.getString(1);
             String[] labels = this.getLabels(args.get(2), VERSION_LABEL);
            QuantcastClient.activityStart(cordova.getActivity(), apiKey, userId, labels);
         } else if (action.equals("endMeasurementSession")) {
             QuantcastClient.activityStop(this.getLabels(args.get(0)));
         } else if (action.equals("pauseMeasurementSession")) {
             QuantcastClient.activityStop(this.getLabels(args.get(0)));
         } else if (action.equals("resumeMeasurementSession")) {
             String[] labels = this.getLabels(args.get(0), VERSION_LABEL);
             QuantcastClient.activityStart(cordova.getActivity(), labels);
         } else if (action.equals("recordUserIdentifier")) {
            QuantcastClient.recordUserIdentifier(args.getString(0));
         } else if (action.equals("logEvent")) {
             QuantcastClient.logEvent(args.getString(0), this.getLabels(args.get(1)));
         } else if (action.equals("setGeolocation")) {
             QuantcastClient.setEnableLocationGathering(args.getBoolean(0));
         } else if (action.equals("setDebugLogging")) {
             boolean logOn = args.getBoolean(0);
             QuantcastClient.enableLogging(logOn);
         } else if (action.equals("displayUserPrivacyDialog")) {
             QuantcastClient.showAboutQuantcastScreen(cordova.getActivity());
         } else if (action.equals("uploadEventCount")) {
             QuantcastClient.setUploadEventCount(args.getInt(0));
         } else {
             //anything else I don't know
             retval = false;
         }
         return retval;
     }
 
     private String[] getLabels(Object labels, String appendingItem) throws JSONException {
         String[] label = getLabels(labels);
         if(appendingItem != null){
             label = Arrays.copyOf(label, label.length+1);
             label[label.length-1] = appendingItem;
         }
         return label;
     }
 
     private String[] getLabels(Object labels) throws JSONException {
         String[] label;
         if (labels instanceof JSONArray) {
             label = new String[((JSONArray) labels).length()];
             for (int i = 0; i < ((JSONArray) labels).length(); ++i) {
                 label[i] = ((JSONArray) labels).getString(i);
             }
         } else if (labels instanceof String) {
             label = new String[]{(String) labels};
         } else {
             label = new String[0];
         }
         return label;
     }
 }
 
 
 
