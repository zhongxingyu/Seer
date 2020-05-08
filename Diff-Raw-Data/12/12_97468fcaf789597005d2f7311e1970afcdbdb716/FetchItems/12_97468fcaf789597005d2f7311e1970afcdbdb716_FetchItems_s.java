 package org.omships.omships;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class FetchItems extends AsyncTask<Feed,Integer,List<RSSItem> > {
 	static Map<String, List<RSSItem> > memo = new HashMap<String, List<RSSItem> >();
 	ProgressDialog mDialog;
 	Context context;
 	ListView view;
 	
 	
 	public static void invalidate(Feed feed){
 		if(memo.containsKey(feed.getUrl()))
 			memo.remove(feed.getUrl());
 	}//end invalidate
 	
 	
 	public FetchItems(Context context,ListView view){
 		this.context=context;
 		this.view=view;
 	}
 	
 	protected void onPreExecute(){
 		mDialog = new ProgressDialog(context);
         mDialog.setMessage("Loading...");
         mDialog.setCancelable(false);
         mDialog.show();
 	}
 	
 	@Override
 	protected List<RSSItem> doInBackground(Feed... args) {
 		List<RSSItem> items = new ArrayList<RSSItem>();
 		for(Feed feed:args){
 			List<RSSItem> feed_items=null;
 			if(!feed.getType().equals("rss"))continue;
 			if(memo.containsKey(feed.getUrl())){
 				feed_items=memo.get(feed.getUrl());
 			}else{
 				try {
 					RSSReader rssReader = new RSSReader(feed.getUrl());
 					feed_items=rssReader.getItems();
 					if(feed_items!=null && feed_items.size()>0)
 						memo.put(feed.getUrl(),feed_items);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}//end if memo'd
 			items.addAll(feed_items);
 		}//end for feeds
 		return items;
 	}//end doInBackground
 	
 	protected void onPostExecute(List<RSSItem> items){
		ArrayAdapter<RSSItem> adapter = new ArrayAdapter<RSSItem>(context,
				android.R.layout.simple_list_item_1,items);
		view.setAdapter(adapter);
		mDialog.dismiss();
 	}
 }//end class FetchItems
