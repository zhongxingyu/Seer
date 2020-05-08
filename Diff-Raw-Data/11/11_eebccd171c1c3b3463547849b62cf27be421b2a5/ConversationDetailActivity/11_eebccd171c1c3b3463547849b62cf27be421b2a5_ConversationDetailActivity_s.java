 package edu.ucsb.cs290.touch.to.text;
 
 import java.io.Serializable;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.view.MenuItem;
 
 public class ConversationDetailActivity extends KeyActivity {
 
     private static final String FRAG_TAG = "edu.ucsb.cs290.touch.to.text.conversationDA.CDF";
 	private ConversationDetailFragment fragment;
     private boolean fragNeedsAdded = false;
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
             fragNeedsAdded = true;
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
 		if(fragment == null) {
 			fragment = (ConversationDetailFragment) getFragmentManager().findFragmentByTag(FRAG_TAG);
 		}
 		if(fragNeedsAdded) {
 			getFragmentManager().beginTransaction()
                     .add(R.id.conversation_detail_container, fragment,FRAG_TAG)
                     .commit();
 			fragNeedsAdded = false;
 		}
 		fragment.onServiceConnected();
 		
 	}
 	
 	@Override
 	protected void refresh() {
 		if(fragment == null) {
 			fragment = (ConversationDetailFragment) getFragmentManager().findFragmentByTag(FRAG_TAG);
 		}
 		fragment.refresh();
 	}
 }
