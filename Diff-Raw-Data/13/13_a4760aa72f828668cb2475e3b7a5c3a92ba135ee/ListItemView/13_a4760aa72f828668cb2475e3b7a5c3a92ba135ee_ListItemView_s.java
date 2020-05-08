 package org.daum.library.android.messages.view;
 
 import android.graphics.Color;
 import android.util.Pair;
 import android.widget.TextView;
 import android.content.Context;
 import android.widget.LinearLayout;
 import org.sitac.MessageAmbiance;
 
 /**
  * Created with IntelliJ IDEA.
  * User: max
  * Date: 01/06/12
  * Time: 15:10
  * To change this template use File | Settings | File Templates.
  */
 public class ListItemView extends LinearLayout {
 
     public enum MessageType {
         IN, // messages received from one another
         OUT // messages emitted by the user
     }
 
     private Context ctx;
     private MessageAmbiance msg;
     private MessageType type;
     private TextView tv_header, tv_message;
 
     public ListItemView(Context context, MessageAmbiance message, MessageType type) {
         super(context);
         this.ctx = context;
         this.msg = message;
         this.type = type;
         initUI();
         configUI();
     }
 
     private void initUI() {
         tv_header = new TextView(ctx);
         tv_message = new TextView(ctx);
     }
 
     private void configUI() {
         // configuring this view layout
         setOrientation(LinearLayout.VERTICAL);
         setPadding(10, 5, 0, 5);
 
         // change UI according to message type
         switch (type) {
             case IN:
                 setBackgroundColor(Color.WHITE);
                 break;
 
             case OUT:
                 setBackgroundColor(Color.LTGRAY);
                 break;
         }
 
        tv_header.setText(msg.getGroupeHoraire()+" - "+msg.getSender());
         tv_header.setTextSize(25f);
         tv_header.setTextColor(Color.RED);
 
         tv_message.setText(msg.getText());
         tv_message.setTextSize(18f);
 
         addView(tv_header);
         addView(tv_message);
     }
 
     public void updateData(Pair<MessageType, MessageAmbiance> pair) {
         this.type = pair.first;
         this.msg = pair.second;
 
         removeAllViews();
         configUI();
 
         requestLayout();
     }
 }
