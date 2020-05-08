 package net.anei.cadpage;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.anei.cadpage.donation.DonationManager;
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.ActivityNotFoundException;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.Uri;
 import android.telephony.SmsManager;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 
 /**
  * Class to handle all of the menus and popup buttons associated with a message
  */
 public class MsgOptionManager {
   
   private Activity activity;
   private SmsMmsMessage message;
   
   // View group and list of button handlers associated with response menu buttons
   private ViewGroup respButtonGroup = null;
   private List<ButtonHandler> respButtonList = new ArrayList<ButtonHandler>();
   
   // View group and list of button handlers associated with main menu buttons
   private ViewGroup mainButtonGroup = null;
   private List<ButtonHandler> mainButtonList = new ArrayList<ButtonHandler>();
   
   // Broadcast receiver logging results of text send messages
   private SendSMSReceiver receiver = null;;
   
   public MsgOptionManager(Activity activity) {
     this(activity, null);
   }
   
   public MsgOptionManager(Activity activity, SmsMmsMessage message) {
     this.activity = activity;
     this.message = message;
   }
   
   public void setMessage(SmsMmsMessage message) {
     this.message = message;
     
     // If we are in the process of building button lists for the call display
     // then we have to set up a response array with this message
     if (respButtonGroup != null) setupResponseButtons();
   }
 
   /**
    * Create option or context menu for message
    * @param context current context
    * @param menu menu to be constructed
    * @param display true if called from popup menu display
    */
   public void createMenu(Menu menu, boolean display) {
     MenuInflater inflater = activity.getMenuInflater();
     inflater.inflate(R.menu.message_menu, menu);
     if (display) {
       menu.removeItem(R.id.open_item);
     } else {
       menu.removeItem(R.id.resp_menu_item);
       menu.removeItem(R.id.close_item);
     }
   }
   
   /**
    * Make any final changes to menu before actually displaying it
    * @param context current context
    * @param menu Message menu needed to be adjusted
    */
   public void prepareMenu(Menu menu) {
     for (int ndx = 0; ndx < menu.size(); ndx++) {
       final MenuItem item = menu.getItem(ndx);
       prepareItem(new ItemObject() {
         
         @Override
         public int getId() {
           return item.getItemId();
         }
 
         @Override
         public void setEnabled(boolean enabled) {
           item.setEnabled(enabled);
         }
 
         @Override
         public void setTitle(int resId) {
           item.setTitle(resId);
         }
 
         @Override
         public void setVisible(boolean visible) {
           item.setVisible(visible);
         }
         
       });
     }
   }
 
   // List of menu items associated with each button index.
   private static final int[] ITEM_ID_LIST = new int[]{
     0, 
     R.id.resp_menu_item, 
     R.id.map_item, 
     R.id.toggle_lock_item, 
     R.id.delete_item, 
     R.id.close_item, 
     R.id.email_item,
     R.id.publish_item, 
     R.id.close_app_item,
     R.id.more_info_item
   };
   
   // List of item title resources associated with each button index
   private static final int[] ITEM_TEXT_LIST = new int[]{
     0, 
     R.string.resp_menu_item_text, 
     R.string.map_item_text, 
     0, 
     R.string.delete_item_text, 
     R.string.close_item_text, 
     R.string.email_item_text,
     R.string.publish_item_text, 
     R.string.close_app_item_text,
     R.string.more_info_item_text
   };
   
   public void setupButtons(ViewGroup respButtonGroup, ViewGroup mainButtonGroup) {
     this.respButtonGroup = respButtonGroup;
     this.mainButtonGroup = mainButtonGroup;
     
     // If we have a message, use it to set up the response menu
     if (message != null) setupResponseButtons();
 
     // Setup the regular button list
     boolean hasMoreInfo = false;
     mainButtonList.clear();
     mainButtonGroup.removeAllViews();
     for (int btn = 1; btn <= ManagePreferences.POPUP_BUTTON_CNT; btn++) {
       int itemNdx = ManagePreferences.popupButton(btn);
       if (itemNdx == 0) continue;
       if (itemNdx == 9) hasMoreInfo = true;
       mainButtonList.add(new ButtonHandler(ITEM_ID_LIST[itemNdx], ITEM_TEXT_LIST[itemNdx], mainButtonGroup));
     }
     
     // If user doesn't have a More info button configured, add it at the end
     if (!hasMoreInfo) {
       mainButtonList.add(new ButtonHandler(R.id.more_info_item, R.string.more_info_item_text, mainButtonGroup));
     }
   }
   
   /**
    * Setup up the response button menu.  This is called when we finally have
    * both a message and response button viewgroup
    */
   private void setupResponseButtons() {
     
     // Start by clearing any previous arrays
     respButtonList.clear();
     respButtonGroup.removeAllViews();
 
   
     // If response options have been requested by a direct paging vendor, they
     // preempt everything
     boolean menu = setupDirectPageButtons();
     
     // If there are is no direct paging menu, or the user has requested that
     // user response buttons be merged with direct paging menus, add in
     // the user response buttons
     if (!menu || ManagePreferences.responseMerge()) {
       if (setupUserButtons()) menu = true;
     }
     
     // If we have set up anything, add any user extra buttons to end
     if (menu) {
       setupExtraButtons();
     }
     
     // Otherwise, if there are any pending notifications set up a single ack button
     else if (ManageNotification.isAckNeeded()) {
       respButtonList.add(new ButtonHandler(R.id.ack_item, R.string.ack_item_text, respButtonGroup));
     }
   }
 
   /**
    * Set up response menu with buttons defined by C2DM direct paging vendors
    * @return true if we set up any buttons, false otherwise
    */
   public boolean setupDirectPageButtons() {
     boolean result = false;
     
     // First see if normal responding and non-responding buttons were requested
     // We don't generate a not responding button unless a responding button was
     // requested.  If a responding button is requested without a not responding
     // button, generate a dummy not responding button that doesn't do anything
     String ackReq = message.getAckReq();
     if (ackReq != null && ackReq.contains("R")) {
       if (ackReq.contains("N")) {
         respButtonList.add(new ButtonHandler(R.id.resp_http_item, R.string.not_responding_text, "NO", respButtonGroup));
       } else {
         respButtonList.add(new ButtonHandler(R.id.ack_item, R.string.not_responding_text, respButtonGroup));
       }
       respButtonList.add(new ButtonHandler(R.id.resp_http_item, R.string.responding_text, "RESP", respButtonGroup));
       result = true;
     }
     
     // Next see if they requested any custom menus, and if they did, set those up
     String respMenu = message.getResponseMenu();
     if (respMenu != null) {
       for (String btnDef : respMenu.split(";")) {
         String respCode, respDesc;
         int pt = btnDef.indexOf('=');
         if (pt >= 0) {
           respCode = btnDef.substring(0,pt).trim();
           respDesc = btnDef.substring(pt+1).trim();
         } else {
           respCode = "";
           respDesc = btnDef.trim();
         }
         if (respCode.length() > 0) {
           respButtonList.add(new ButtonHandler(R.id.resp_http_item, respDesc, respCode, respButtonGroup));
         } else {
           respButtonList.add(new ButtonHandler(R.id.ack_item, respDesc, null, respButtonGroup));
         }
         result = true;
       }
     }
     
     return result;
   }
   
   /**
    * Set up the custom user response button menu 
    * @return true if anything was set up
    */
   private boolean setupUserButtons() {
     
     // See if we are setting up phone call or text reply response buttons
     int buttonId = (ManagePreferences.responseType().equals("T") 
                         ? R.id.resp_text_item : R.id.resp_call_item);
     
     // There may be buttons with title but no codes.  But if all of the buttons
     // have no codes, then there is no point in setting anything up.  But this
     // means we have to make two passes through the buttons.  The first
     // gets the codes and determines if any are non-empty
     String[] respCodes = new String[ManagePreferences.CALLBACK_BUTTON_CNT];
     String[] respDesc = new String[ManagePreferences.CALLBACK_BUTTON_CNT];
     boolean found = false;
     for (int btn = 1; btn <= ManagePreferences.CALLBACK_BUTTON_CNT; btn++) {
       String code = ManagePreferences.callbackButtonCode(btn);
       String desc = ManagePreferences.callbackButtonTitle(btn);
       respCodes[btn-1] = code;
       respDesc[btn-1] = desc;
       if (desc.length() > 0 && code.length() > 0) found = true;
     }
     if (!found) return false;
     
     // We have at least one, so make another pass to actually set up the user
     // defined buttons
     for (int btn = 1; btn <= ManagePreferences.CALLBACK_BUTTON_CNT; btn++) {
       String desc = respDesc[btn-1];
       if (desc.length() == 0) continue;
       String code = respCodes[btn-1];
       if (code.length() > 0) {
         respButtonList.add(new ButtonHandler(buttonId, desc, code, respButtonGroup));
       } else {
         respButtonList.add(new ButtonHandler(R.id.ack_item, desc, null, respButtonGroup));
       }
     }
     return true;
   }
   
   /**
    * Set up and "extra" regular buttons user wants to appear in response menu
    */
   private void setupExtraButtons() {
    for (int btn = 1; btn <= ManagePreferences.EXTRA_BUTTON_CNT; btn++) {
       int itemNdx = ManagePreferences.extraButton(btn);
       if (itemNdx == 0) continue;
       respButtonList.add(new ButtonHandler(ITEM_ID_LIST[itemNdx], ITEM_TEXT_LIST[itemNdx], respButtonGroup));
     }
   }
 
   /**
    * Make any last minute corrections to button statuses
    */
   public void prepareButtons() {
     
     // First step is to see which button menu should be visible
     // If we do not have a response button menu, then force main menu display mode
     boolean responseMenu = message.isResponseMenuVisible();
     if (responseMenu && respButtonList.size() == 0) {
       responseMenu = false;
       message.setResponseMenuVisible(false);
     }
     
     // Set the menu visibility and prepare all buttons in the visible menu
     if (responseMenu) {
       respButtonGroup.setVisibility(View.VISIBLE);
       mainButtonGroup.setVisibility(View.GONE);
       prepareButtons(respButtonList);
     } else {
       respButtonGroup.setVisibility(View.GONE);
       mainButtonGroup.setVisibility(View.VISIBLE);
       prepareButtons(mainButtonList);
     }
   }
 
   /**
    * Prepare all buttons in a button menu
    * @param buttonList list of buttons to be prepared
    */
   public void prepareButtons(List<ButtonHandler> buttonList) {
     for (ButtonHandler btnHandler : buttonList) {
       btnHandler.prepareButton();
     }
   }
   
   /*
    * Internal class to handle dynamic button functions on popup
    */
   private class ButtonHandler implements OnClickListener {
     final private int itemId;
     final private Button button;
     private String respCode = null;
 
     /**
      * Normal constructor for regular button items
      * @param itemId button item ID
      * @param resId button title resource ID
      * @param parent parent ViewGroup
      */
     public ButtonHandler(int itemId, int resId, ViewGroup parent) {
       this(itemId, resId, null, null, parent);
     }
     
     /**
      * Response button constructor
      * @param itemId button item ID
      * @param title button title
      * @param respCode button response code
      * @param parent parent ViewGroup
      */
     public ButtonHandler(int itemId, String title, String respCode, ViewGroup parent) {
       this(itemId, 0, title, respCode, parent);
     }
     
     /**
      * Response button constructor
      * @param itemId button item ID
      * @param resId button title resource ID
      * @param respCode button response code
      * @param parent parent ViewGroup
      */
     public ButtonHandler(int itemId, int resId, String respCode, ViewGroup parent) {
       this(itemId, resId, null, respCode, parent);
     }
     
     /**
      * Common constructor 
      * @param itemId button item ID
      * @param resId button title resource ID
      * @param title button title
      * @param respCode button response code
      * @param parent parent ViewGroup
      */
     private ButtonHandler(int itemId, int resId, String title, String respCode, ViewGroup parent) {
       this.itemId = itemId;
       button = (Button)LayoutInflater.from(activity).inflate(R.layout.popup_button, parent, false);
       button.setId(itemId);
       if (resId != 0) button.setText(resId);
       else button.setText(title);
       button.setOnClickListener(this);
       parent.addView(button);
       this.respCode = respCode;
     }
     
     public void prepareButton() {
       prepareItem(new ItemObject(){
         
         @Override
         public int getId() {
           return itemId;
         }
 
         @Override
         public void setEnabled(boolean enabled) {
           button.setEnabled(enabled);
         }
 
         @Override
         public void setTitle(int resId) {
           button.setText(resId);
         }
 
         @Override
         public void setVisible(boolean visible) {
           button.setVisibility(visible ? View.VISIBLE : View.GONE);
         }
       });
     }
 
     public void onClick(View v) {
       
       // Perform the requested action
       menuItemSelected(itemId, true, respCode);
       
       // Reset button status in case anything has changed
       prepareButtons();
     }
   }
   
   private interface ItemObject {
     public int getId();
     public void setTitle(int resId);
     public void setEnabled(boolean enabled);
     public void setVisible(boolean visible);
   }
   
   private void prepareItem(ItemObject item) {
     
     switch (item.getId()) {
     
     // Response menu button is only visible if response menu has more than one button
     // A single button would be a Ack button which never has to be repeated
     case R.id.resp_menu_item:
       item.setVisible(respButtonList.size() > 1);
       break;
     
     // Disable map item if we have no address
     case R.id.map_item:
       item.setEnabled(message.getInfo().getAddress().length() > 0);
       break;
     
     // Change label on toggle lock item depending on current lock state
     case  R.id.toggle_lock_item:
       item.setTitle(message.isLocked() ? R.string.unlock_item_text : R.string.lock_item_text);
       break;
       
     // Delete is only enabled if message has been read and is not locked
     case R.id.delete_item:
       item.setEnabled(message.canDelete());
       break;
     
     // Email is disabled for the free version
     case R.id.email_item:
       item.setEnabled(! DonationManager.instance().isFreeVersion());
       break;
       
       // More info disappears if there is no info to display
     case R.id.more_info_item:
       item.setVisible(message.getInfoURL() != null);
       break;
     }
   }
 
   /**
    * Handle a menu selection concerning this message
    * @param itemId Selected Menu ID
    * @param display true if called from message display dialog
    * @return true if menu item processed, false otherwise
    */
   public boolean menuItemSelected(int itemId, boolean display) {
     return menuItemSelected(itemId, display, null);
   }
   
   public boolean menuItemSelected(int itemId, boolean display, String respCode) {
     
     // Any button clears the notice
     ManageNotification.clear(activity);
     message.acknowledge(activity);
 
     switch (itemId) {
     
     case R.id.resp_menu_item:
       message.setResponseMenuVisible(true);
       return true;
       
     case R.id.open_item:
       SmsPopupActivity.launchActivity(activity, message);
       return true;
       
     case R.id.map_item:
       mapMessage(activity, false);
       return true;
       
     case R.id.toggle_lock_item:
       message.toggleLocked();
       return true;
       
     case R.id.delete_item:
       SmsMessageQueue.getInstance().deleteMessage(message);
       if (display) activity.finish();
       return true;
       
     case R.id.close_item:
       activity.finish();
       return true;
       
     case R.id.close_app_item:
       activity.finish();
       CallHistoryActivity.shutdown(activity);
       return true;
       
     case R.id.email_item:
       EmailDeveloperActivity.sendMessageEmail(activity,  message.getMsgId());
       return true;
       
     case R.id.publish_item:
       message.broadcastIntent(activity, true);
       return true;
       
     case R.id.more_info_item:
       message.showMoreInfo(activity);
       return true;
       
     case R.id.ack_item:
       message.setResponseMenuVisible(false);
       return true;
       
     case R.id.resp_call_item:
       message.setResponseMenuVisible(false);
       try {
         String urlPhone = "tel:" + respCode;
         Intent intent = new Intent(Intent.ACTION_CALL);
         intent.setData(Uri.parse(urlPhone));
         activity.startActivity(intent);
       } catch (Exception e) {
         Log.v("SMSPopupActivity: Phone call failed" + e.getMessage());
       }
       return true;
       
     case R.id.resp_text_item:
       message.setResponseMenuVisible(false);
       sendSMS(message.getFromAddress(), respCode);
       return true;
       
     case R.id.resp_http_item:
       message.setResponseMenuVisible(false);
       message.sendResponse(activity, respCode);
       return true;
     
     default:
       return false;
     }
   }
 
   /**
    * Request map location for message
    * @param context current context
    * @param useGPS use GPS location in preference to regular address
    */
   private void  mapMessage(Context context, boolean useGPS)  {
     if (Log.DEBUG) Log.v("Request Received to Map Call");
     
     String searchStr = message.getMapAddress(useGPS);
     if (searchStr == null) return;
     
     if (!SmsPopupUtils.haveNet(context)) return;
 
     searchStr = searchStr.replaceAll(" *& *", " AT ");
     Uri uri = Uri.parse("geo:0,0?q=" + Uri.encode(searchStr));
     if (Log.DEBUG) Log.v("mapMessage: SearchStr=" + searchStr);
     Intent intent = new Intent(Intent.ACTION_VIEW, uri);
     
     try {
         context.startActivity(intent);
     } catch (ActivityNotFoundException ex) {
         Log.e("Could not find com.google.android.maps.Maps activity");
     }
   }
 
   /**
    * Send SMS response message
    * @param target target phone number or address
    * @param message message to be sent
    */
   private void sendSMS(String target, String message){ 
     Log.v("Sending text reponse to " + target + " : " + message);
     
     if (receiver == null) {
       receiver = new SendSMSReceiver();
       activity.registerReceiver(receiver, new IntentFilter(SMS_SENT));
       activity.registerReceiver(receiver, new IntentFilter(SMS_DELIVERED));;
     }
     
     Intent sendIntent = new Intent(SMS_SENT);
     sendIntent.setFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
     PendingIntent sentPI = PendingIntent.getBroadcast(activity, 0, sendIntent, 0);                
     Intent deliverIntent = new Intent(SMS_DELIVERED);
     deliverIntent.setFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
     PendingIntent deliveredPI = PendingIntent.getBroadcast(activity, 0, deliverIntent, 0);   
 
     /**
      * The send logic apparently isn't as bulletproof as we like.  It sometimes
      * throws a null pointer exception on the other side of an RPC.  We can't
      * do much about it.
      */
     SmsManager sms = SmsManager.getDefault();
     try {
       sms.sendTextMessage(target, null, message, sentPI, deliveredPI);
     } catch (NullPointerException ex) {
       Log.e(ex);
     }
   }
   
   public static class SendSMSReceiver extends BroadcastReceiver {
 
     @Override
     public void onReceive(Context context, Intent intent) {
       if (intent == null) return;
       String action = intent.getAction();
       int pt = action.lastIndexOf('.');
       if (pt >= 0) action = action.substring(pt+1);
       String status;
       switch (getResultCode()) {
       
       case Activity.RESULT_OK:
         status = "OK";
         break;
         
       case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
         status = "Generic failure"; 
         break;
         
       case SmsManager.RESULT_ERROR_NO_SERVICE:
         status = "No service"; 
         break;
         
       case SmsManager.RESULT_ERROR_NULL_PDU:
         status = "Null PDU";
         break;
         
       case SmsManager.RESULT_ERROR_RADIO_OFF:
         status = "Radio off";
         break;
         
       case Activity.RESULT_CANCELED:
         status = "Canceled";
         break;                        
       
       default:
         status = "" + getResultCode();
       }
       Log.v("SMS " + action + " status:" + status);
     }
   }
   private static final String SMS_SENT = "net.anei.cadpage.MsgOptionManager.SMS_SENT";
   private static final String SMS_DELIVERED = "net.anei.cadpage.MsgOptionManager.SMS_DELIVERED";
 }
