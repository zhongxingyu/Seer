 package net.anei.cadpage;
 
 import android.content.Context;
 import android.graphics.Typeface;
 import android.text.format.DateFormat;
 import android.util.AttributeSet;
 import android.view.View;
 import android.widget.TextView;
 
 public class HistoryMsgTextView extends TextView {
   
   private SmsMmsMessage msg;
   
   public HistoryMsgTextView(Context context) {
     super(context);
     setup();
   }
   
   public HistoryMsgTextView(Context context, AttributeSet attrs) {
     super(context, attrs);
     setup();
   }
   
   public HistoryMsgTextView(Context context, AttributeSet attrs, int defStyle) {
     super(context, attrs, defStyle);
     setup();
   }
   
   private void setup() {
     this.setOnClickListener(new OnClickListener(){
 
       @Override
       public void onClick(View v) {
         ManageNotification.clear(getContext());
         
         if (msg == null) return;
         
         // display message popup
         SmsPopupActivity.launchActivity(getContext(), msg);
       }});
   }
   
   public void setMessage(SmsMmsMessage message) {
     this.msg = message;
 
     Context context = getContext();
 
     long time = msg.getTimestamp();
     String text = DateFormat.getLongDateFormat(context).format(time) + " " +
                   DateFormat.getTimeFormat(context).format(time) +
                   (msg.isLocked() ? " (Locked)" : "") +
                   "\n" + msg.getCall();
     setText(text);
     if (! msg.isRead()) setTypeface(Typeface.DEFAULT_BOLD);
     else setTypeface(Typeface.DEFAULT);
   }
 
   public SmsMmsMessage getMessage() {
     return msg;
   }
 }
