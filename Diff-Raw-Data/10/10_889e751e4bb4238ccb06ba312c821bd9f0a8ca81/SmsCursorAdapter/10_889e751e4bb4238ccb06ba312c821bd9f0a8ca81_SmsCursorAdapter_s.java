 package net.retsat1.starlab.smssender.ui.adapter;
 
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import net.retsat1.starlab.smssender.R;
 import net.retsat1.starlab.smssender.dto.SmsMessage;
 import android.app.Activity;
 import android.content.Context;
 import android.database.Cursor;
 import android.telephony.SmsManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ImageView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 
 public class SmsCursorAdapter extends SimpleCursorAdapter implements OnCheckedChangeListener {
 
     private static final String TAG = "SmsCursorAdapter";
     private Cursor c;
     private Context context;
     private ConcurrentLinkedQueue<Integer> checkList = new ConcurrentLinkedQueue<Integer>();
     static String[] valuePosition = { SmsMessage.RECEIVER, SmsMessage.MESSAGE, SmsMessage.MESSAGE_STATUS };
     static int[] uiPosition = { R.id.numberText, R.id.messageText, R.id.stat };
     private int[] mFrom;
     private LayoutInflater li;
 
     public SmsCursorAdapter(Context context, int layout, Cursor c) {
         super(context, layout, c, valuePosition, uiPosition);
         this.c = c;
         this.context = context;
         findColumns(valuePosition);
         li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         c.setNotificationUri(context.getContentResolver(), SmsMessage.CONTENT_URI);
 
     }
 
     /**
      * Create a map from an array of strings to an array of column-id integers
      * in mCursor. If mCursor is null, the array will be discarded.
      * 
      * @param from
      *            the Strings naming the columns of interest
      */
     private void findColumns(String[] from) {
         if (c != null) {
             int i;
             int count = from.length;
             if (mFrom == null || mFrom.length != count) {
                 mFrom = new int[count];
             }
             for (i = 0; i < count; i++) {
                 mFrom[i] = c.getColumnIndexOrThrow(from[i]);
             }
         } else {
             mFrom = null;
         }
     }
 
     @Override
     public synchronized View newView(Context context, Cursor cursor, ViewGroup parent) {
         LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         View v = li.inflate(R.layout.list_item, parent, false);
         CheckBox cb = (CheckBox) v.findViewById(R.id.checked);
         Integer id = cursor.getInt(cursor.getColumnIndex(SmsMessage.SMS_ID));
         Log.d(TAG, "newView ID=" + id + " checked= " + cb.isChecked());
         cb.setOnCheckedChangeListener(this);
         cb.setTag(id);
         return v;
     }
 
     @Override
     public void bindView(View view, Context context, Cursor cursor) {
         Log.d(TAG, "bindView view " + view + " context  " + context + " cursor " + cursor);
         final ViewBinder binder = getViewBinder();
         final int count = uiPosition.length;
         final int[] from = mFrom;
         final int[] to = uiPosition;
 
         for (int i = 0; i < count; i++) {
             final View v = view.findViewById(to[i]);
             if (v != null) {
                 boolean bound = false;
                 if (binder != null) {
                     Log.d(TAG, "from[i]  " + from[i] + " " + valuePosition[i]);
                     bound = binder.setViewValue(v, cursor, from[i]);
                 }
 
                 if (!bound) {
                     String text = cursor.getString(from[i]);
                     if (text == null) {
                         text = "";
                     }
 
                     if (v instanceof TextView) {
                         setViewText((TextView) v, text);
                     } else if (v instanceof ImageView) {
                         setViewImage((ImageView) v, text);
                     } else {
                         throw new IllegalStateException(v.getClass().getName() + " is not a " + " view that can be bounds by this SimpleCursorAdapter");
                     }
                 }
             }
         }
     }
 
     /**
      * Tested on android 2.1
      * 
      */
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         Log.d(TAG, "convertView " + convertView);
         View v = null;
         if (convertView == null) {
             convertView = li.inflate(R.layout.list_item, parent, false);
         }
         Log.d(TAG, "v = " + " convertView = " + convertView);
         v = super.getView(position, convertView, parent);
         this.c.moveToPosition(position);
         Integer id = this.c.getInt(this.c.getColumnIndex(SmsMessage.SMS_ID));
         Integer messageStatus = this.c.getInt(this.c.getColumnIndex(SmsMessage.MESSAGE_STATUS));
         Integer deliveryStatus = this.c.getInt(this.c.getColumnIndex(SmsMessage.DELIVERY_STATUS));
         TextView statusTextView = (TextView) v.findViewById(R.id.stat);
         Log.d(TAG, "messageStatus " + messageStatus + " deliveryStatus " + deliveryStatus);
         String text = getStatusText(messageStatus, deliveryStatus);
         statusTextView.setText(text);
         Log.d(TAG, "IDID = " + id + "text " + text);
         CheckBox cBox = (CheckBox) v.findViewById(R.id.checked);
         cBox.setTag(id);
         cBox.setOnCheckedChangeListener(this);
         boolean b = checkList.contains(id);
         cBox.setChecked(b);
         Log.d(TAG, "position " + position);
         return v;
     }
 
     private String getStatusText(Integer messageStatus, Integer deliveryStatus) {
         String status = "";
         switch (messageStatus) {
         case SmsMessage.STATUS_UNSENT:
             status += "Message not send";
             break;
         case SmsMessage.STATUS_SENDING:
             status += "Message sending";
         case Activity.RESULT_OK:
             status += "Message send OK";
         case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
             status += "Message generic error";
             break;
         case SmsManager.RESULT_ERROR_NO_SERVICE:
             status += "No service error";
             break;
         case SmsManager.RESULT_ERROR_NULL_PDU:
             status += "Error NULL PDU";
             break;
         case SmsManager.RESULT_ERROR_RADIO_OFF:
             status += "Radio off";
             break;
         default:
             status += "Message unknown status";
         }
         status += "||";
         switch (deliveryStatus) {
         case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
             status += "General raport failure";
             break;
         case Activity.RESULT_OK:
             break;
         default:
             status += "delivery status unknown";
             break;
         }
         return status;
 
     }
 
     public void deleteAllCheckedItems() {
 
         String where = SmsMessage.SMS_ID + " =?";
         if (!checkList.isEmpty()) {
             for (Integer id : checkList) {
                 Log.d(TAG, "Delete " + id);
                 String[] args = new String[] { "" + id };
                 context.getContentResolver().delete(SmsMessage.CONTENT_URI, where, args);
                 checkList.remove(id);
             }
         }
         notifyDataSetInvalidated();
         notifyDataSetChanged();
     }
 
     @Override
     public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
         Log.d(TAG, "TAG " + buttonView.getTag());
         if (isChecked) {
             checkList.add((Integer) buttonView.getTag());
         } else {
             checkList.remove((Integer) buttonView.getTag());
         }
         Log.d(TAG, "checkList " + checkList.size());
     }
 
     public void selectAllItems() {
         if (getCount() > 0) {
             c.moveToFirst();
             do {
                 Integer id = c.getInt(c.getColumnIndex(SmsMessage.SMS_ID)); // crashes
                                                                             // here
                 checkList.add(id);
             } while (c.moveToNext());
             notifyDataSetInvalidated();
             notifyDataSetChanged();
         }
     }
 }
