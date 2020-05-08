 package com.data;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Wolfram
  * Date: 11.11.13
  * Time: 14:56
  */
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 import com.activities.R;
 
 import java.util.ArrayList;
 
public class ChatAdapter extends ArrayAdapter<ChatMessage> {
     private final Context context;
    private final ArrayList<ChatMessage> messages;
     private final User you;
 
    public ChatAdapter(Context context, ArrayList<ChatMessage> msg, User you) {
         //requires the test ArrayList, msg.toStringArrayList for some reason didn't do the trick
        super(context, R.layout.layout_chathistory_list_you, msg);
         this.context = context;
         this.you = you;
         messages = msg;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         ChatMessage currentMessage = messages.get(position);
         LayoutInflater inflater = (LayoutInflater) context
                 .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         View rowView;
        //Contains q should be removed as soon as testing is complete
        if (currentMessage.getSender().equals(you) && !(currentMessage.getMessage().contains("q"))) {
             rowView = inflater.inflate(R.layout.layout_chathistory_list_you, parent, false);
         } else {
             rowView = inflater.inflate(R.layout.layout_chathistory_list_notyou, parent, false);
         }
         TextView msg = (TextView) rowView.findViewById(R.id.msg);
         TextView time = (TextView) rowView.findViewById(R.id.timestamp);
         msg.setText(currentMessage.getMessage());
         time.setText(currentMessage.getTimestamp());
         return rowView;
     }
 }
