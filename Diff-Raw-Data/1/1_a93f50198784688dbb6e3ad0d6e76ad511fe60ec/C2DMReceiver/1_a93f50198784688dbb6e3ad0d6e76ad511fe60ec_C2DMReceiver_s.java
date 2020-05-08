 /*
  * Copyright (C) 2007-2008 Esmertec AG.
  * Copyright (C) 2007-2008 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package net.anei.cadpage;
 
 
 
 import net.anei.cadpage.HttpService.HttpRequest;
 import net.anei.cadpage.donation.DonationManager;
 import net.anei.cadpage.parsers.SmsMsgParser;
 import net.anei.cadpage.vendors.VendorManager;
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 
 /**
  * Receives M2DM related intents 
  */
 public class C2DMReceiver extends BroadcastReceiver {
   
   private static final String ACTION_C2DM_REGISTER = "com.google.android.c2dm.intent.REGISTER";
   private static final String ACTION_C2DM_UNREGISTER = "com.google.android.c2dm.intent.UNREGISTER";
   private static final String ACTION_C2DM_REGISTERED = "com.google.android.c2dm.intent.REGISTRATION";
   private static final String ACTION_C2DM_RECEIVE = "com.google.android.c2dm.intent.RECEIVE";
   private static final String C2DM_SENDER_EMAIL = "alerts@cadpage.org";
   
   private static Activity curActivity = null;
 
   @Override
   public void onReceive(Context context, Intent intent) {
     if (Log.DEBUG) Log.v("C2DMReceiver: onReceive()");
     
     // If initialization failure in progress, shut down without doing anything
     if (TopExceptionHandler.isInitFailure()) return;
     
     // Free version doesn't do C2DM stuff
     if (DonationManager.instance().isFreeVersion()) return;
     
     if (ACTION_C2DM_REGISTERED.equals(intent.getAction())) {
       handleRegistration(context, intent);
     }
     
     else if (ACTION_C2DM_RECEIVE.equals(intent.getAction())) {
       handleMessage(context, intent);
     }
   }
   
   private void handleRegistration(Context context, Intent intent) {
     String error = intent.getStringExtra("error");
     if (error != null) {
       Log.w("C2DM registration failed: " + error);
       ManagePreferences.setRegistrationId(null);
       VendorManager.instance().failureC2DMId(context, error);
       return;
     }
     
     String regId = intent.getStringExtra("unregistered");
     if (regId != null) {
       Log.w("C2DM registration cancelled: " + regId);
       ManagePreferences.setRegistrationId(null);
       VendorManager.instance().unregisterC2DMId(context, regId);
       return;
     }
     
     regId = intent.getStringExtra("registration_id");
     if (regId != null) {
       Log.w("C2DM registration succeeded: " + regId);
       ManagePreferences.setRegistrationId(regId);
       VendorManager.instance().registerC2DMId(context, regId);
       return;
     }
     
   }
 
   private void handleMessage(final Context context, final Intent intent) {
     
     // If registration has been canceled, all C2DM messages should be ignored
     if (ManagePreferences.registrationId() == null) return;
     
     // See what kind of message this is
     String type = intent.getStringExtra("type");
     
     // Ping just needs to be acknowledged
     if (type.equals("PING")) {
       sendAutoAck(context, intent);
       return;
     }
     
     // Register and unregister requests are handled by VendorManager
     if (type.equals("REGISTER") || type.equals("UNREGISTER")) {
       String vendor = intent.getStringExtra("vendor");
       String account = intent.getStringExtra("account");
       String token = intent.getStringExtra("token");
       VendorManager.instance().vendorRequest(context, type, vendor, account, token);
       sendAutoAck(context, intent);
       return;
     }
     
     // Save timestamp
     final long timestamp = System.currentTimeMillis();
     
     // Retrieve message content from intent for from URL
     String content = intent.getStringExtra("content");
     if (content != null) {
       processContent(context, intent, content, timestamp);
       sendAutoAck(context, intent);
       return;
     }
     
     String contentURL = intent.getStringExtra("content_url");
     if (contentURL != null) {
       HttpService.addHttpRequest(context, new HttpRequest(Uri.parse(contentURL)){
         @Override
         public void processResponse(int status, String result) {
           if (status % 100 == 2) {
             processContent(context, intent, result, timestamp);
           }
         }
       });
       return;
     }
     Log.w("C2DM message has no content");
   }
   
   /**
    * Send auto acknowledgment when message is received
    * @param context current context
    * @param intent received intent
    */
   private void sendAutoAck(Context context, Intent intent) {
     String ackURL = intent.getStringExtra("ack_url");
     sendResponseMsg(context, ackURL, "auto");
   }
     
   private void processContent(Context context, Intent intent, String content, 
                                 long timestamp) {
     
     // Reconstruct message from data from intent fields
     String from = intent.getStringExtra("from");
     if (from == null) from = "C2DM";
     String subject = intent.getStringExtra("subject");
     if (subject == null) subject = "";
     String location = intent.getStringExtra("format");
     if (location != null) {
       if (location.equals("Active911")) location = "Cadpage";
       location = ManagePreferences.convertOldLocationCode(context, location);
     }
     String sponsor = intent.getStringExtra("sponsor");
     
     // Get the acknowledge URL and request code
     String ackURL = intent.getStringExtra("ack_url");
     String ackReq = intent.getStringExtra("ack_req");
     if (ackURL == null) ackReq = null;
     if (ackReq == null) ackReq = "";
     
     SmsMmsMessage message = 
       new SmsMmsMessage(from, subject, content, timestamp,
                         location, sponsor, ackReq, ackURL);
     
     // Add to log buffer
     if (!SmsMsgLogBuffer.getInstance().add(message)) return;
     
     // See if the current parser will accept this as a CAD page
     boolean isPage = message.isPageMsg(true, false);
     
     // If it didn't, accept it as a general page.  We have to do something with
     // it or it will just disappear
     if (! isPage) {
       SmsMsgParser genAlertParser = ManageParsers.getInstance().getAlertParser();
       isPage = genAlertParser.isPageMsg(message, true, true);
     }
     
     // This should never happen, but if the general alert parser can't handle
     // it bail out
     if (!isPage) return;
     SmsReceiver.processCadPage(context, message);
   }
   
   /**
    * Register activity to be used to generate any alert dialogs
    * @param activity
    */
   public static void registerActivity(Activity activity) {
     curActivity = activity;
   }
   
   /**
    * Unregister activity previously registered for any generated alert dialogs
    * @param activity
    */
   public static void unregisterActivity(Activity activity) {
     if (curActivity == activity) curActivity = null;
   }
 
   
   /**
    * send response messages
    * @param context current context
    * @param ackURL acknowledge URL
    * @param type request type to be sent 
    */
   public static void sendResponseMsg(Context context, String ackURL, String type) {
     if (ackURL == null) return;
     Uri uri = Uri.parse(ackURL).buildUpon().appendQueryParameter("type", type).build();
     HttpService.addHttpRequest(context, new HttpRequest(uri));
   }
   
   
   /**
    * Request a new C2DM registration ID
    * @param context current context
    */
   public static void register(Context context) {
     Intent intent = new Intent(ACTION_C2DM_REGISTER);
     intent.putExtra("app", PendingIntent.getBroadcast(context, 0, new Intent(), 0));
     intent.putExtra("sender", C2DM_SENDER_EMAIL);
     context.startService(intent);
   }
 
   
   /**
    * Request that current C2DM registration be dropped
    * @param context current context
    */
   public static void unregister(Context context) {
     Intent intent = new Intent(ACTION_C2DM_UNREGISTER);
     intent.putExtra("app", PendingIntent.getBroadcast(context, 0, new Intent(), 0));
     context.startService(intent);
   }
 
   /**
    * Generate an Email message with the current registration ID
    * @param context current context
    */
   public static void emailRegistrationId(Context context) {
     
     // Build send email intent and launch it
     Intent intent = new Intent(Intent.ACTION_SEND);
     String emailSubject = CadPageApplication.getNameVersion() + " C2DM registrion ID";
     intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
     intent.putExtra(Intent.EXTRA_TEXT, "My C2DM registration ID is " + ManagePreferences.registrationId());
     intent.setType("message/rfc822");
     context.startActivity(Intent.createChooser(
         intent, context.getString(R.string.pref_sendemail_title)));
   }
 }
 
