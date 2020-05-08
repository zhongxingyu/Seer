 package com.example;
 
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.widget.ArrayAdapter;
 import android.widget.Toast;
 
 import java.text.DateFormat;
 import java.text.MessageFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User: rok
  * Date: 15.3.12
  * Time: 23:17
  */
 public class MessagesListActivity extends ListActivity implements MessagesLoadable {
     private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     private ArrayAdapter<String> listAdapter;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         listAdapter = new ArrayAdapter<String>(this, R.layout.regions_list_item);
         setListAdapter(listAdapter);
         getListView().setBackgroundResource(R.drawable.main_bg);
        getListView().setCacheColorHint(0);
         reloadMessages();
     }
 
     public void reloadMessages() {
         Toast.makeText(this, "Nalagam sporočila...", Toast.LENGTH_SHORT).show();
         new AsyncMessagesLoader(this).execute();
     }
 
     @Override
     public void loadMessages(List<Message> messages) {
         listAdapter.clear();
         for (String message : flattenMessages(messages)) {
             listAdapter.add(message);
         }
         Toast.makeText(this, "Sporočila osvežena.", Toast.LENGTH_SHORT).show();
     }
 
     private List<String> flattenMessages(List<Message> messagesList) {
         List<String> messages = new ArrayList<String>();
         for (Message message : messagesList) {
             messages.add(MessageFormat.format("{0} - {1}\n{2}",
                     message.getAuthor(), DATE_FORMAT.format(message.getDate()), message.getMessage()));
         }
         return messages;
     }
 }
