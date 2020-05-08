 package com.devcamp.messagetimer;
 
 import java.util.ArrayList;
 
 import com.devcamp.messagetimer.model.Message;
 import com.devcamp.messagetimer.presenter.MessageListPresenter;
 import com.devcamp.messagetimer.tools.Database;
 
 
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.widget.Button;
import android.widget.ImageButton;
 
 public class MessageListActivity extends ListActivity {
 	
 	private Database mDb = null;
	private ImageButton mAddMessage = null;
 	
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
         
        mAddMessage = (ImageButton) findViewById(R.id.btn_message_add);
 	}
     
    public ImageButton GetAddMessage(){
     	return mAddMessage;
     }
     
     public Database GetDb(){
     	return mDb;
     }
 }
