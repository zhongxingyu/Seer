 package com.ouchadam.podcast.activity;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import com.ouchadam.podcast.R;
 import com.ouchadam.podcast.adapter.FeedItemAdapter;
 import com.ouchadam.podcast.builder.IntentFactory;
 import com.ouchadam.podcast.parser.interfaces.OnParseFinished;
 import com.ouchadam.podcast.pojo.Message;
 import com.ouchadam.podcast.receiver.ParseReceiver;
 
 import java.util.List;
 
 public class MessageList extends ListActivity implements OnParseFinished {
 	
     private FeedItemAdapter adapter;
     private ParseReceiver receiver;
     private ProgressBar progressBar;
 	
     @Override
     public void onCreate(Bundle icicle) {
         super.onCreate(icicle);
         setContentView(R.layout.main);
         initReceiver();
         progressBar = (ProgressBar) findViewById(R.id.progress_bar);
         startLoadingFeed();
         initListFooter();
     }
 
     private void initListFooter() {
         View footerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.footer_layout, null, false);
         getListView().addFooterView(footerView);
     }
 
     private void initReceiver() {
         receiver = new ParseReceiver();
         receiver.setOnParseListener(this);
     }
 
     private void startLoadingFeed(){
         startService(IntentFactory.getParseService());
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         IntentFilter filter = new IntentFilter();
         filter.addAction(ParseReceiver.ACTION_ON_PARSE_FINISHED);
         registerReceiver(receiver, filter);
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         unregisterReceiver(receiver);
     }
 
     @Override
     public void onParseFinished(List<Message> messages) {
         initAdapter(messages);
         progressBar.setVisibility(View.INVISIBLE);
     }
 
 
     private void initAdapter(List<Message> messages) {
         adapter = new FeedItemAdapter(this, R.layout.item_feed, messages);
         this.setListAdapter(adapter);
     }
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		return true;
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		startActivity(IntentFactory.getMessageDetails(((Message) l.getItemAtPosition(position)).getTitle()));
 	}
 

 }
 
 
