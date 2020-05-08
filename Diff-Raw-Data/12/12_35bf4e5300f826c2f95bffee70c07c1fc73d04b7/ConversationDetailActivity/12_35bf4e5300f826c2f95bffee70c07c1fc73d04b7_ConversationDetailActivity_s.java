 package edu.ucsb.cs290.touch.to.chat;
 
 import java.io.Serializable;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.view.MenuItem;
 
 public class ConversationDetailActivity extends KeyActivity {
 
     private ConversationDetailFragment fragment;

 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_conversation_detail);
 
         getActionBar().setDisplayHomeAsUpEnabled(true);
 
         if (savedInstanceState == null) {
             Bundle arguments = new Bundle();
             Serializable serializableExtra = getIntent().getSerializableExtra(ConversationDetailFragment.ARG_ITEM_ID);
 			arguments.putSerializable(ConversationDetailFragment.ARG_ITEM_ID,
                     serializableExtra);
             fragment = new ConversationDetailFragment();
             fragment.setArguments(arguments);
            
         }
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getItemId() == android.R.id.home) {
             NavUtils.navigateUpTo(this, new Intent(this, ConversationListActivity.class));
             return true;
         }
 
         return super.onOptionsItemSelected(item);
     }
 
 	@Override
 	public void onServiceConnected() {
		getFragmentManager().beginTransaction()
                     .add(R.id.conversation_detail_container, fragment)
                     .commit();
 		fragment.onServiceConnected();
 	}
 }
