 package tice.weibo.Activities;
 
 import tice.weibo.R;
 import tice.weibo.DB.DBTweetsHelper;
 import tice.weibo.HttpClient.TwitterClient;
 import tice.weibo.List.TweetsListActivity;
 import tice.weibo.Util.TweetsData;
 import tice.weibo.Util.TwitterItem;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 
 
 public class StatusesShowActivity extends TweetsListActivity {
 	
 	private static int ACTOVITY_TYPE_ID = TwitterClient.HOME_STATUSES_SHOW;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
 
     	_Handler = mHandler;
     	_ActivityType = ACTOVITY_TYPE_ID;
     	_Context = StatusesShowActivity.this;
     	
         super.onCreate(savedInstanceState);
     
     	_list.setOnItemClickListener(OnItemClickListener);
     	
         InitButtons();
         SetWindowTitle(ACTOVITY_TYPE_ID); 
         
         setMyProgressBarVisibility(true);
         
         Intent i = getIntent();
         String replyid = i.getStringExtra("replyid");
         ReadStatuses(replyid);
         //_twitter.Get_statuses_show(mHandler, replyid);
     }
     
     
     @Override
     protected void onResume() {
         super.onResume();
         _Items.Clear();
 //        _PrevID = Long.MAX_VALUE;
     }
     
     @Override
     public void defaultonItemClick(int position, Bundle b){
     	
 		_CurrentThread = position;
 		PanelAnimationOn(false, _Toolbarpanel);
 		PanelAnimationOff(false, _Statuspanel);
 		_Items.notifyDataSetChanged();
 		
     }
 	
     OnItemClickListener OnItemClickListener = new OnItemClickListener(){
 
 		public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
 			defaultonItemClick(position,null);
 		}
     };
     
     private void DecodeJSON(TwitterItem item){
 
  		long id = item.mID;
  		String replyid = item.mReplyID;
 			
  		if(replyid.length() == 0 || replyid.matches("null") == true){
  			setMyProgressBarVisibility(false);
  		}else{
  			setMyProgressBarVisibility(true);
  			//_twitter.Get_statuses_show(mHandler, replyid);
  			ReadStatuses(replyid);
  		}
 
  		if(_App._twitter != null) _App._twitter.FrechImg(mHandler, item.mImageurl,item.mScreenname);
 
 // 		if(id > _LastID){
 // 			_LastID = id;
 // 		}
 // 		if(id < _PrevID){
 // 			_PrevID = id;
 // 		}
 
  		//boolean read = _DbHelper.FindTweet(DbAdapter.DATABASE_TABLES[_ActivityType], item.mID);
  		_Items.addThread(READ_STATE_READ, item.mScreenname, item.mTitle, item.mText, item.mTime, item.mSource, id, item.mReplyID, item.mFavorite, false, item.mImageurl, true, item.mPicurl);
     }
  	
  	private void UpdateListView(Bundle bundle,boolean append, boolean order){
  		
  		TweetsData data = (TweetsData) bundle.getSerializable(TwitterClient.KEY);
 
  		if(data == null || data.items == null) return;
  		
  		DecodeJSON(data.Get(0));
 		_Items.notifyDataSetChanged();
 		SaveTweetItemsToDB();
  	}
  	
     public void InitButtons(){
     	
     	View.inflate(this.getBaseContext(),R.layout.statusshow_toolbar, _Toolbarpanel);
     	View.inflate(this.getBaseContext(),R.layout.home_statusbar, _Statuspanel);
     	
     	super.InitButtons();
     }
     
     private final Handler mHandler = new Handler() {
         @Override
          public void handleMessage(final Message msg) {
         	defaulthandleMessage(msg);
         	processMessage(msg);
         }
     }; 
 
     public void processMessage(Message msg) {
     	switch (msg.what){
     	case TwitterClient.HTTP_STATUSES_SHOW:
     		UpdateListView(msg.getData(),true, true );
     		break;
     	}
     }
     
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
 
     	switch(item.getItemId()) {
         case R.id.refresh:
         	if(_Refresh == true) break;
         	_Refresh = true;
         	setMyProgressBarVisibility(true);
 	        Intent i = getIntent();
 	        String replyid = i.getStringExtra("replyid");
 	        //_twitter.Get_statuses_show(mHandler, replyid);
 	        ReadStatuses(replyid);
 			PanelAnimationOff(false, _Statuspanel);
 			PanelAnimationOff(false, _Toolbarpanel);
         	break;
         }
 
         return super.onMenuItemSelected(featureId, item);
     }
     
     private void ReadStatuses(String id){
     	
     	Cursor c = _App._DbHelper.QueryTweet(_App._Username, -1, id);
     	
     	if(c != null){
     		if(c.getCount() != 0){
     			c.moveToFirst();
     			
     			Bundle b = new Bundle();
     			TweetsData data = new TweetsData();
     			TwitterItem item = new TwitterItem();
 
 				item.mScreenname = c.getString(DBTweetsHelper.COL_SCREENNAME);
 				item.mTitle = c.getString(DBTweetsHelper.COL_TITLE);
 				item.mText = c.getString(DBTweetsHelper.COL_TEXT);
 				item.mTime = c.getLong(DBTweetsHelper.COL_TIME);
 				item.mID = c.getLong(DBTweetsHelper.COL_ID);
 				item.mSource = c.getString(DBTweetsHelper.COL_SOURCE);
 				item.mReplyID = c.getString(DBTweetsHelper.COL_REPLYID);
 				item.mFavorite = (c.getInt(DBTweetsHelper.COL_FAVORITE) == 1)? true : false;
 				item.mFollowing = (c.getInt(DBTweetsHelper.COL_FOLLOWING) == 1)? true : false;
 				item.mRead = c.getInt(DBTweetsHelper.COL_READ);
 				item.mImageurl = c.getString(DBTweetsHelper.COL_ICONURL);
     			
     			data.items.add(item);
  				b.putSerializable(TwitterClient.KEY, data);
  				TwitterClient.SendMessage(mHandler, TwitterClient.HTTP_STATUSES_SHOW, b);
     			
  				c.close();
     			return;
     		}
     		c.close();
     	}
     	
     	if (_App._twitter != null) _App._twitter.Get_statuses_show(mHandler, id);
     }
 }
