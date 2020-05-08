 package tice.twitterwalk.List;
 
 import java.util.ArrayList;
 import java.util.regex.Pattern;
 
 import tice.twitterwalk.App;
 import tice.twitterwalk.R;
 import tice.twitterwalk.HttpClient.TwitterClient;
 import tice.twitterwalk.Util.TwitterItem;
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.text.TextUtils;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.AbsListView.OnScrollListener;
 
 
 public class ItemAdapter extends BaseAdapter {
 
 	public App _App = null;
 
 	public static Pattern p1 = Pattern.compile("https?://?((([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?)+)",Pattern.CASE_INSENSITIVE);
 	public static Pattern p2 = Pattern.compile("@[a-zA-Z0-9_]+", Pattern.CASE_INSENSITIVE);
 	public static Pattern p3 = Pattern.compile("#[a-zA-Z0-9_]+( |$)", Pattern.CASE_INSENSITIVE);
 
 	private static int IMAGE_CACHE_SIZE = 12;
 
     public class ViewHolder {
     	TextView title;
     	TextView text;
     	TextView retweeted_text;
     	TextView timesource;
     	ImageView icon;
     	//IconImageView icon_right;
     	LinearLayout itemline;
     	LinearLayout imagelayout;
     	//LinearLayout imagelayout_right;
     	ImageView unread;
     	ProgressBar progressbar;
     	ProgressBar retweeted_progressbar;
     	ImageView favorite;
     	ImageView conversation;
     	ImageView pic;
     }
 
     boolean mInRefresh = false;
     Bitmap mFav, mUnFav, mBlank, mUnRead, mConversation;
     TweetsListActivity mCtx;
 	private LayoutInflater mInflater;
 	private ArrayList<TwitterItem> mItem = new ArrayList<TwitterItem>();
 	private int mListbackground;
 	private int mListbackgroundat;
 	private int mListbackgroundmy;
 	private int mListbackgroundselset;
 	private int mtextviewcolor;
 	private int mtextviewcolorselect;
 	private int mScrollState;
 
     public ItemAdapter(Context context) {
 
     	_App = (App)context.getApplicationContext();
 
         mInflater = LayoutInflater.from(context);
         mCtx = (TweetsListActivity)context;
 
         mFav = BitmapFactory.decodeResource(context.getResources(), R.drawable.star_on);
         mUnFav = BitmapFactory.decodeResource(context.getResources(), R.drawable.star_off);
         mBlank = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_profile_1_normal);
         mUnRead = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_sms_unread_msg_indicator);
         mConversation = BitmapFactory.decodeResource(context.getResources(), R.drawable.conversationt);
 
 	    TypedArray atts = context.obtainStyledAttributes(new int []
 	                                  {R.attr.ListViewBackground,	R.attr.ListViewBackgroundAt,
 	                                   R.attr.ListViewBackgroundMy, R.attr.ListViewBackgroundSelect,
 	                                   R.attr.TextViewColor, R.attr.TextViewColorSelect});
 
 
 	    mListbackground = atts.getResourceId(0, R.drawable.theme_default_listbackground);
 	    mListbackgroundat = atts.getResourceId(1, R.drawable.theme_default_listbackgroundat);
 	    mListbackgroundmy = atts.getResourceId(2, R.drawable.theme_default_listbackgroundmy);
 	    mListbackgroundselset = atts.getResourceId(3, R.drawable.theme_default_listbackgroundselect);
 	    mtextviewcolor = atts.getColor(4, 0xff313031);
 	    mtextviewcolorselect = atts.getColor(5, 0xff313031);
 
 	    atts.recycle();
     }
 
     public int getCount() {
         return mItem.size();
     }
 
     public void Remove(int i) {
     	if (i >= mItem.size() && i < 0 ) return;
         mItem.remove(i);
         notifyDataSetChanged();
     }
 
     public void RemoveAll() {
         mItem.clear();
         notifyDataSetChanged();
     }
 
     public void Clear(){
     	mItem.clear();
     	notifyDataSetChanged();
     }
 
     public TwitterItem Get(int i){
     	if (i >= mItem.size() || i < 0 ) return null;
     	return mItem.get(i);
     }
 
     public Object getItem(int position) {
         return position;
     }
 
     public long getItemId(int position) {
         return position;
     }
 
     synchronized public View getView(int position, View convertView, ViewGroup parent) {
 
     	if(position >= mItem.size()) return convertView;
 
         ViewHolder holder;
         TwitterItem item = mItem.get(position);
 
         if (convertView == null) {
             convertView = mInflater.inflate(R.layout.list_item, null);
 
             holder = new ViewHolder();
 
 			holder.progressbar = (ProgressBar) convertView.findViewById(R.id.ProgressBar);
 			holder.progressbar.setVisibility(View.GONE);
 			
             holder.title = (TextView) convertView.findViewById(R.id.title);
             holder.text = (TextView) convertView.findViewById(R.id.text);
             holder.timesource = (TextView) convertView.findViewById(R.id.timesource);
             
             holder.retweeted_text = (TextView) convertView.findViewById(R.id.retweeted_text);
 
            	holder.title.setTextSize(_App._Fontsize);
            	holder.text.setTextSize(_App._Fontsize);
            	holder.timesource.setTextSize(_App._Fontsize - 4);
 
            	holder.imagelayout = (LinearLayout) convertView.findViewById(R.id.image_left);
            	holder.icon = (ImageView) convertView.findViewById(R.id.icon_left);
            	holder.pic = (ImageView) convertView.findViewById(R.id.status_pic);
            	//holder.icon = (ImageView) new IconImageView(mCtx);
            	//holder.imagelayout.addView(holder.icon, 50, 50);
 
            	//holder.imagelayout_right = (LinearLayout) convertView.findViewById(R.id.image_right);
            	//holder.icon_right = (ImageView) convertView.findViewById(R.id.icon_right);
            	//holder.icon_right = (IconImageView) new IconImageView(mCtx);
            	//holder.imagelayout_left.addView(holder.icon_right, 50, 50);
 
         	holder.itemline = (LinearLayout) convertView.findViewById(R.id.itemline);
         	holder.unread = (ImageView) convertView.findViewById(R.id.unreadimage);
         	holder.unread.setImageBitmap(mUnRead);
 
         	holder.favorite = (ImageView) convertView.findViewById(R.id.favoriteindicate);
         	holder.favorite.setImageBitmap(mFav);
 
         	holder.conversation = (ImageView) convertView.findViewById(R.id.conversation);
         	holder.conversation.setImageBitmap(mConversation);
 
         	if(item.mScreenname.length() != 0 && _App._twitter != null){
             	item.mImage = _App._twitter.LoadIcon(mCtx._Handler, item.mScreenname, item.mImageurl);
             	
            	}
         	if(item.mScreenname.length() != 0 && item.mPicurl.length() != 0 && _App._twitter != null){
         		item.mPic = _App._twitter.LoadPic(mCtx._Handler, item.mID, item.mPicurl);
         	}
         	
         	//holder.text.setLinksClickable(false);
         	//holder.text.setLinkTextColor(0xaa0000ff);
 
         	convertView.setTag(holder);
 
         } else {
             holder = (ViewHolder) convertView.getTag();
         }
 
 /*
         LinearLayout imagelayout = holder.imagelayout_left;
         ImageView icon = holder.icon_left;
         holder.imagelayout_right.setVisibility(View.GONE);
         holder.icon_right.setVisibility(View.GONE);
         if(mCtx._ActivityType == TwitterClient.HOME_STATUSES_SHOW){
         	if(position % 2 == 1){
                 holder.imagelayout_left.setVisibility(View.GONE);
                 holder.icon_left.setVisibility(View.GONE);
         		imagelayout = holder.imagelayout_right;
                 icon = holder.icon_right;
         	}
         }
 */
 	    if(position >= IMAGE_CACHE_SIZE){
 	    	int count = mItem.size() - 1;
 	    	if(position <= count){
 	    		TwitterItem temp = mItem.get(position - IMAGE_CACHE_SIZE);
 	    		if(temp.mImage != null){
 	    			temp.mImage.recycle();
 	    			temp.mImage = null;
 //	    			holder.icon.setImageBitmap(null);
 //	    			holder.unread.setImageBitmap(null);
 //	    			holder.favorite.setImageBitmap(null);
 //	    			holder.conversation.setImageBitmap(null);
 	    		}
 	    	}
 	    }else{
 	    	int count = mItem.size() - 1;
 	    	if (position + IMAGE_CACHE_SIZE <= count){
 	    		TwitterItem temp = mItem.get(position + IMAGE_CACHE_SIZE);
 	    		if(temp.mImage != null){
 	    			temp.mImage.recycle();
 	    			temp.mImage = null;
 //	    			holder.icon.setImageBitmap(null);
 //	    			holder.unread.setImageBitmap(null);
 //	    			holder.favorite.setImageBitmap(null);
 //	    			holder.conversation.setImageBitmap(null);
 	    		}
 	    	}
 	    }
 
 	    if (item.mRead == TweetsListActivity.READ_STATE_UNKNOW) item.mRead = TweetsListActivity.READ_STATE_UNREAD;
 
 	    holder.itemline.setBackgroundResource(mListbackground);
 
        	holder.title.setTextColor(mtextviewcolor);
       	holder.timesource.setTextColor(mtextviewcolor);
        	holder.text.setTextColor(mtextviewcolor);
 
         String search = "@" + _App._Username;
         if(item.mText.contains(search) == true){
         	holder.itemline.setBackgroundResource(mListbackgroundat);
         	//holder.itemline.setBackgroundColor(0xffeff5ff);
         }
 
         if(item.mScreenname.equalsIgnoreCase(_App._Username) == true){
         	holder.itemline.setBackgroundResource(mListbackgroundmy);
         	//holder.itemline.setBackgroundColor(0xffeafee7);
         }
 
         if(position == mCtx._CurrentThread){
         	holder.itemline.setBackgroundResource(mListbackgroundselset);
         	//holder.itemline.setBackgroundColor(0xffffc82e);
 
            	holder.title.setTextColor(mtextviewcolorselect);
           	holder.timesource.setTextColor(mtextviewcolorselect);
            	holder.text.setTextColor(mtextviewcolorselect);
         }
 
         holder.favorite.setVisibility(View.GONE);
         holder.conversation.setVisibility(View.GONE);
         holder.icon.setVisibility(View.VISIBLE);
     	holder.progressbar.setVisibility(View.GONE);
 
     	if(item.mRead == TweetsListActivity.READ_STATE_READ){
     		//holder.unread.setImageBitmap(null);
     		holder.unread.setVisibility(View.INVISIBLE);
     	}else{
     		//holder.unread.setImageBitmap(mUnRead);
     		holder.unread.setVisibility(View.VISIBLE);
     	}
 
        	if (item.mFavorite == true){
        		holder.favorite.setVisibility(View.VISIBLE);
        	}
 
        	if(TextUtils.isEmpty(item.mReplyID) == false && item.mReplyID.matches("null") == false){
        		holder.conversation.setVisibility(View.VISIBLE);
        	}
 
         if(item.mLoading == true){
         	holder.progressbar.setVisibility(View.VISIBLE);
         }
 
         if(_App._Displayicon == true){
         	holder.imagelayout.setVisibility(View.VISIBLE);
 
  	        switch (mScrollState) {
  	        case OnScrollListener.SCROLL_STATE_IDLE:
  	        case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
  	        	if(item.mImage == null && item.mScreenname.length() != 0 && _App._twitter != null){
  	            	item.mImage = _App._twitter.LoadIcon(mCtx._Handler, item.mScreenname, item.mImageurl);
  	           	}
  	        }
 
             if(item.mImage == null){
             	holder.icon.setImageBitmap(mBlank);
             }else{
             	holder.icon.setImageBitmap(item.mImage);
             }
 
         }else{
         	holder.imagelayout.setVisibility(View.GONE);
         }
 
        	holder.title.setText(item.mScreenname);
       	holder.timesource.setText(item.mTimeSource);
        	holder.text.setText(item.mText);
        	
        	holder.retweeted_text.setVisibility(View.VISIBLE);
        	LinearLayout shit = (LinearLayout) convertView.findViewById(R.id.retweeted_text_wrap);
        	
        	if (item.mRetweeted_Text.length() > 0){
        		holder.retweeted_text.setText("@"+item.mRetweeted_Screenname+": "+item.mRetweeted_Text);
        		shit.setVisibility(View.VISIBLE);
        	}else{
        		holder.retweeted_text.setText(item.mRetweeted_Text);
        	}
 
        	
 //       	holder.pic.setImageBitmap(mBlank);
 
 //       	switch (mScrollState) {
 //        case OnScrollListener.SCROLL_STATE_IDLE:
 //        case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
 //        	if(item.mPic == null && item.mID >= 0 && item.mPicurl.length() > 0 && _App._twitter != null){
 //            	item.mPic = _App._twitter.LoadPic(mCtx._Handler, item.mID, item.mPicurl);
 //           	}
 //        }
        	
     	if(item.mPic == null && item.mPicurl.length() > 0 && _App._twitter != null){
         	item.mPic = _App._twitter.LoadPic(mCtx._Handler, item.mID, item.mPicurl);
        	}
 
        	if(item.mPic != null ){
        		holder.pic.setVisibility(View.VISIBLE);
         	holder.pic.setImageBitmap(item.mPic);
         }else{
         	holder.pic.setVisibility(View.GONE);
         }
 
        	if (item.mScreenname.length() == 0){
         	holder.title.setText("");
         	holder.timesource.setText("");
         	holder.text.setText(item.mText);
         	holder.favorite.setVisibility(View.INVISIBLE);
         	holder.icon.setVisibility(View.INVISIBLE);
         }
 
  		//Linkify.addLinks(holder.text, p1, "");
  		//Linkify.addLinks(holder.text, p2, "");
  		//Linkify.addLinks(holder.text, p3, "");
 
         return convertView;
     }
 
     public void SetScrollState(int state){
     	mScrollState = state;
     }
 
     public void ReplaceThread(int index, TwitterItem obj,int type){
     	if(index >= mItem.size()) return;
 
     	TwitterItem item = mItem.get(index);
 
     	item.mScreenname = obj.mScreenname;
     	item.mTitle = obj.mTitle;
     	item.mTime = obj.mTime;
     	item.mSource = obj.mSource;
     	item.mText = obj.mText;
     	item.mID = obj.mID;
     	item.mReplyID = obj.mReplyID;
     	item.mFavorite = obj.mFavorite;
     	item.mFollowing = obj.mFollowing;
     	item.mImageurl = obj.mImageurl;
     	item.mImage = obj.mImage;
     	item.mRead = obj.mRead;
        	item.mTimeSource = obj.mTimeSource;
        	item.mPicurl = obj.mPicurl;
     	item.mPic = obj.mPic;
     	
     	item.mRetweeted_Screenname = obj.mRetweeted_Screenname;
     	item.mRetweeted_Text = obj.mRetweeted_Text;
     }
 
     public void addThread(int index, TwitterItem obj, int addtype, int type){
 
     	int count = mItem.size();
     	if(count >= TwitterClient.MAX_TWEETS_COUNT) return;
 
     	if(count != 0){
 			if(mItem.get(count - 1).mID == obj.mID && obj.mID != 0){
 				return;
 			}
 		}
 
     	if(addtype == TweetsListActivity.ADD_TYPE_APPEND){
     		mItem.add(new TwitterItem(obj));
     	}else{
     		if(index >= mItem.size() ) index = mItem.size();
     		mItem.add(index, new TwitterItem(obj));
     	}
 
     	notifyDataSetChanged();
     }
 
     public void addThread(int read, String screenname, String title, String text, long time, String source, long id, String replyid, boolean fav, boolean following, String iconrui, boolean append, String picurl){
 
     	int count = mItem.size();
     	//if(screenname.length() == 0 && count < _App._Tweetscount - 5) return;
     	if(count >= TwitterClient.MAX_TWEETS_COUNT) return;
 
     	if(append == true){
     		if(count != 0){
     			if(mItem.get(count - 1).mID == id ){
     				return;
     			}
     		}
     		mItem.add(new TwitterItem(read, screenname, title, text, time, source, id, replyid, fav, following, iconrui,mCtx._ActivityType, _App._Username, picurl));
     	}else{
    			mItem.add(0,new TwitterItem(read, screenname, title, text, time, source, id, replyid, fav, following, iconrui, mCtx._ActivityType, _App._Username, picurl));
     	}
 
     	notifyDataSetChanged();
     }
     
     public void addThread(int read, String screenname, String title, String text, long time, String source, long id, String replyid, boolean fav, boolean following, String iconrui, boolean append, String picurl, String retweeted_screenname, String retweeted_text){
 
     	int count = mItem.size();
     	//if(screenname.length() == 0 && count < _App._Tweetscount - 5) return;
     	if(count >= TwitterClient.MAX_TWEETS_COUNT) return;
 
     	if(append == true){
     		if(count != 0){
     			if(mItem.get(count - 1).mID == id ){
     				return;
     			}
     		}
     		mItem.add(new TwitterItem(read, screenname, title, text, time, source, id, replyid, fav, following, iconrui,mCtx._ActivityType, _App._Username, picurl, retweeted_screenname, retweeted_text));
     	}else{
    			mItem.add(0,new TwitterItem(read, screenname, title, text, time, source, id, replyid, fav, following, iconrui, mCtx._ActivityType, _App._Username, picurl, retweeted_screenname, retweeted_text));
     	}
 
     	notifyDataSetChanged();
     }
 
     public void SetLoadingItem(){
     	int count = mItem.size();
     	if(count == 0) return;
     	if(count >= TwitterClient.MAX_TWEETS_COUNT) return;
     	TwitterItem item = mItem.get(count - 1);
 		if (item.mScreenname.length() == 0){
 			item.mText = "Click here to load more tweets";
 			item.mLoading = false;
 		}
     }
 
     public void SetStartLoadingItem(){
     	int count = mItem.size();
     	if(count == 0) return;
     	if(count >= TwitterClient.MAX_TWEETS_COUNT) return;
     	TwitterItem item = mItem.get(count - 1);
     	if (item.mScreenname.length() == 0){
 			item.mText = "  Loading more tweets ...";
 			item.mLoading = true;
 		}
     	notifyDataSetChanged();
     }
 
 }
