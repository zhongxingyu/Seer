 package com.devcamp.messagetimer;
 
 import java.util.ArrayList;
 
 import com.devcamp.messagetimer.model.Message;
 import com.devcamp.messagetimer.presenter.MessageListPresenter;
 import com.devcamp.messagetimer.tools.Database;
 
 
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.widget.Button;
 
 public class MessageListActivity extends ListActivity {
 	
 	private Database mDb = null;
	private Button mAddMessage = null;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
         setContentView(R.layout.message_list);
         init();
  		new MessageListPresenter(this);
     }
     
     private void init() {
     	mDb = new Database(this);
         this.setListAdapter(new MessageAdapter(this,R.layout.message_list_item,(ArrayList<Message>)mDb.getMessages()));
         
        mAddMessage = (Button) findViewById(R.id.btn_message_add);
 	}
     
    public Button GetAddMessage(){
     	return mAddMessage;
     }
     
     public Database GetDb(){
     	return mDb;
     }
 }
