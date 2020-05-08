 package com.lecoding.activity;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 import android.widget.TextView;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.MenuItem;
 import com.lecoding.R;
 import com.lecoding.data.Comment;
 import com.lecoding.data.PicDetail;
 import com.lecoding.data.Status;
 import com.lecoding.util.CommentAdapter;
 import com.lecoding.util.JSONParser;
 import com.lecoding.view.PicList;
 import com.lecoding.view.Retweet;
 import com.loopj.android.image.SmartImageView;
 import com.weibo.sdk.android.WeiboException;
 import com.weibo.sdk.android.api.CommentsAPI;
 import com.weibo.sdk.android.api.WeiboAPI;
 import com.weibo.sdk.android.net.RequestListener;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: usbuild
  * DateTime: 13-4-18 上午9:38
  */
 public class ViewWeiboActivity extends SherlockActivity {
     private final int UPDATE_CMT = 0;
 
     private TextView weiboText;
     private SmartImageView profileImg;
     private SmartImageView thumbnail;
     private PicList picList;
     private Retweet retweet;
     private TextView commentCount;
     private TextView repostCount;
     private TextView attitudeCount;
     private ListView commentList;
     private CommentAdapter commentAdapter;
     private List<Comment> comments;
 
     private Handler handler;
 
 
     public void updateCommentList(List<Comment> comments) {
         this.comments.addAll(comments);
         commentAdapter.notifyDataSetChanged();
     }
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         commentList = new ListView(this);
        //set list options
        commentList.setHeaderDividersEnabled(false);
         commentList.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
 
         View view = getLayoutInflater().inflate(R.layout.view_weibo, null);
         commentList.addHeaderView(view);
 
         setContentView(commentList);
 
         ActionBar actionBar = getSupportActionBar();
         actionBar.setDisplayHomeAsUpEnabled(true);
 
         comments = new ArrayList<Comment>();
 
         weiboText = (TextView) findViewById(R.id.weibo_item_text);
         profileImg = (SmartImageView) findViewById(R.id.profile_img);
         thumbnail = (SmartImageView) findViewById(R.id.thumbnail);
         picList = (PicList) findViewById(R.id.piclist);
         retweet = (Retweet) findViewById(R.id.retweet);
 
         attitudeCount = (TextView) findViewById(R.id.weibo_item_attitude_cnt);
         commentCount = (TextView) findViewById(R.id.weibo_item_comment_cnt);
         repostCount = (TextView) findViewById(R.id.weibo_item_repost_cnt);
 
 
         Status status = (Status) getIntent().getSerializableExtra("status");
 
         attitudeCount.setText("赞(" + status.getAttitudesCount() + ")");
         commentCount.setText("评论(" + status.getCommentsCount() + ")");
         repostCount.setText("转发(" + status.getRepostsCount() + ")");
 
         weiboText.setText(status.getText());
         profileImg.setImageUrl(status.getUser().getProfileImageUrl());
 
        List<PicDetail> picDetails = status.getPicDetails();
 
         if (status.getRetweetedStatus() != null) {
             retweet.setVisibility(View.VISIBLE);
             retweet.setData(status.getRetweetedStatus());
         } else {
            if (picDetails.size() <= 1) {
                thumbnail.setImageUrl(status.getBmiddlePic());
            } else {
                List<String> urls = new ArrayList<String>();
                for (PicDetail picDetail : picDetails) {
                    urls.add(picDetail.getThumbnailPic());
                }
                picList.setImages(urls);
             }
         }
 
         handler = new Handler(new Handler.Callback() {
             @Override
             @SuppressWarnings("unchecked")
             public boolean handleMessage(Message message) {
                 switch (message.what) {
                     case UPDATE_CMT:
                         updateCommentList((List<Comment>) message.obj);
                         break;
                 }
                 return false;
             }
         });
 
         commentAdapter = new CommentAdapter(this, comments);
         commentList.setAdapter(commentAdapter);
 
         CommentsAPI commentsAPI = new CommentsAPI(BaseActivity.token);
         commentsAPI.show(status.getId(), 0, 0, 20, 1,
                 WeiboAPI.AUTHOR_FILTER.ALL
                 , new RequestListener() {
             @Override
             public void onComplete(String response) {
                 JSONTokener tokener = new JSONTokener(response);
                 try {
                     JSONArray jsonArray = ((JSONObject) tokener.nextValue()).getJSONArray("comments");
                     List<Comment> comments = new ArrayList<Comment>();
                     for (int i = 0; i < jsonArray.length(); ++i) {
                         JSONObject jsonObject = jsonArray.getJSONObject(i);
                         comments.add(JSONParser.parseComment(jsonObject));
                     }
 
                     Message message = new Message();
                     message.obj = comments;
                     message.what = UPDATE_CMT;
                     handler.sendMessage(message);
 
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
             }
 
             @Override
             public void onIOException(IOException e) {
 
             }
 
             @Override
             public void onError(WeiboException e) {
 
             }
         });
 
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 this.finish();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 }
