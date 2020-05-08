 
 package com.wh.dm.activity;
 
 import com.umeng.analytics.MobclickAgent;
 import com.umeng.api.sns.UMSnsService;
 import com.wh.dm.R;
 import com.wh.dm.WH_DMApi;
 import com.wh.dm.WH_DMApp;
 import com.wh.dm.preference.Preferences;
 import com.wh.dm.type.Comment;
 import com.wh.dm.type.PostResult;
 import com.wh.dm.type.Reply;
 import com.wh.dm.type.Review;
 import com.wh.dm.util.NotificationUtil;
 import com.wh.dm.util.TextUtil;
 import com.wh.dm.widget.NewsReplyFloorAdapter;
 import com.wh.dm.widget.NewsReplyMoreAdapter;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import java.util.ArrayList;
 
 public class PhotoReplyActivity extends Activity {
     private static final int MSG_GET_COMMENT = 0;
     public static final int MSG_PUSH_TOP = 1;
     public static final int MSG_REPLY = 2;
     private static final int MSG_REVIEW = 3;
 
     private LayoutInflater mInflater;
     private EditText edtxMyReplyforBtn;
     private Button btnReply;
     private EditText edtReply;
     private Button btnShare;
 
     private LinearLayout bottomLayout1;
     private RelativeLayout bottomLayout2;
     private ListView lv;
     private View footer;
     private ProgressDialog progressDialog;
     private NewsReplyMoreAdapter adapter;
     private NewsReplyFloorAdapter floorAdapter;
     private GetCommentTask getCommentTask = null;
     private PushTopTask pushTopTask = null;
     private ReplyTask replyTask = null;
     private ReviewTask reviewTask = null;
     private WH_DMApi wh_dmApi;
     private WH_DMApp wh_dmApp;
     private int id;
     private boolean isReply = false;
     private boolean isReview = true;
     private String fid;
     private int curPage = 1;
     private boolean isFirstLauncher = true;
     public final Handler handler = new Handler() {
         @Override
         public void handleMessage(android.os.Message msg) {
 
             switch (msg.what) {
                 case MSG_GET_COMMENT:
                     if (getCommentTask != null) {
                         getCommentTask.cancel(true);
                         getCommentTask = null;
                     }
                     getCommentTask = new GetCommentTask();
                     getCommentTask.execute(id);
                     break;
                 case MSG_PUSH_TOP:
                     if (pushTopTask != null) {
                         pushTopTask.cancel(true);
                         pushTopTask = null;
                     }
 
                     if (WH_DMApp.isLogin) {
                         Bundle bundle = msg.getData();
                         pushTopTask = new PushTopTask();
                         pushTopTask.execute(bundle.getString("fid"));
                     } else {
                         NotificationUtil.showShortToast(getString(R.string.please_login),
                                 PhotoReplyActivity.this);
                         Intent intent = new Intent(PhotoReplyActivity.this, LoginActivity.class);
                         startActivity(intent);
                     }
 
                     break;
                 case MSG_REPLY:
                     Bundle bundle = msg.getData();
                     isReply = bundle.getBoolean("isReply");
                     if (isReply) {
                         if (replyTask != null) {
                             replyTask.cancel(true);
                             replyTask = null;
                         }
 
                         replyTask = new ReplyTask();
                         replyTask.execute(fid);
                         isReview = true;
 
                     } else {
                         fid = bundle.getString("fid");
                         isReview = false;
                         bottomLayout1.setVisibility(View.GONE);
                         bottomLayout2.setVisibility(View.VISIBLE);
 
                         edtReply.requestFocus();
                         ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                                 .showSoftInput(edtReply, 0);
                     }
 
                     break;
                 case MSG_REVIEW:
                     if (reviewTask != null) {
                         reviewTask.cancel(true);
                         reviewTask = null;
                     }
                     String str = getFcontent();
                     if (!TextUtil.isEmpty(getFcontent())) {
                         reviewTask = new ReviewTask();
                         reviewTask.execute(getFcontent());
                     } else {
                         NotificationUtil.showShortToast(getString(R.string.review_null),
                                 PhotoReplyActivity.this);
                     }
 
             }
         };
     };
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
 
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.activity_news_reply);
         id = getIntent().getIntExtra("id", 323);
         initViews();
         wh_dmApp = (WH_DMApp) getApplication();
         wh_dmApi = wh_dmApp.getWH_DMApi();
         handler.sendEmptyMessage(MSG_GET_COMMENT);
 
     }
 
     @Override
     public void onBackPressed() {
 
         if (bottomLayout2.getVisibility() == View.VISIBLE) {
             bottomLayout2.setVisibility(View.GONE);
             bottomLayout1.setVisibility(View.VISIBLE);
         } else {
             super.onBackPressed();
         }
     }
 
     @Override
     public void onResume() {
 
         super.onResume();
         MobclickAgent.onResume(this);
     }
 
     @Override
     public void onPause() {
 
         super.onPause();
         MobclickAgent.onPause(this);
     }
 
     private void initViews() {
 
         // add data for listview
         progressDialog = new ProgressDialog(PhotoReplyActivity.this);
         progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
         lv = (ListView) findViewById(R.id.lv_news_reply);
         adapter = new NewsReplyMoreAdapter(this);
        //floorAdapter = new NewsReplyFloorAdapter(this);
         mInflater = getLayoutInflater();
         footer = mInflater.inflate(R.layout.news_list_footer, null);
         Button btnFoolter = (Button) footer.findViewById(R.id.btn_news_footer);
         btnFoolter.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
 
                 curPage++;
                 handler.sendEmptyMessage(MSG_GET_COMMENT);
             }
         });
         lv.addFooterView(footer);
         adapter.setHandler(handler);
         lv.setAdapter(adapter);
 
         // init header
         TextView txtTitle = (TextView) findViewById(R.id.txt_header3_title);
         txtTitle.setText(getResources().getString(R.string.reply));
         TextView txtReply = (TextView) findViewById(R.id.txt_total_reply);
         txtReply.setText(getResources().getString(R.string.context));
         txtReply.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
 
                 PhotoReplyActivity.this.finish();
 
             }
         });
 
         // inti reply views
         bottomLayout1 = (LinearLayout) findViewById(R.id.linear1_news_details_bottom);
         bottomLayout2 = (RelativeLayout) findViewById(R.id.linear2_news_details_bottom);
 
         edtxMyReplyforBtn = (EditText) findViewById(R.id.edtx_news_my_reply);
         edtxMyReplyforBtn.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
 
                 bottomLayout1.setVisibility(View.GONE);
                 bottomLayout2.setVisibility(View.VISIBLE);
 
                 edtReply.requestFocus();
                 ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(
                         edtReply, 0);
             }
         });
 
         edtReply = (EditText) findViewById(R.id.edt_news_details_input);
         btnReply = (Button) findViewById(R.id.btn_news_details_reply);
 
         btnReply.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
 
                 bottomLayout1.setVisibility(View.VISIBLE);
                 bottomLayout2.setVisibility(View.GONE);
                 ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                         .hideSoftInputFromWindow(edtReply.getWindowToken(), 0);
                 if (!WH_DMApp.isLogin) {
                     NotificationUtil.showShortToast(getString(R.string.please_login),
                             PhotoReplyActivity.this);
                     Intent intent = new Intent(PhotoReplyActivity.this, LoginActivity.class);
                     startActivity(intent);
                 }
                 if (isReview) {
                     handler.sendEmptyMessage(MSG_REVIEW);
 
                 } else {
                     Message msg = new Message();
                     msg.what = MSG_REPLY;
                     Bundle bundle = new Bundle();
                     bundle.putBoolean("isReply", true);
                     msg.setData(bundle);
                     handler.sendMessage(msg);
                 }
             }
         });
         lv.setVisibility(View.INVISIBLE);
         ImageButton btnBack = (ImageButton) findViewById(R.id.img_header3_back);
         btnBack.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
 
                 finish();
 
             }
 
         });
         UMSnsService.UseLocation = true;
         UMSnsService.LocationAuto = true;
         UMSnsService.LOCATION_VALID_TIME = 180000; // 30MIN
         btnShare = (Button) findViewById(R.id.btn_news_share);
         btnShare.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
 
                 UMSnsService.share(PhotoReplyActivity.this, "˵Щʲô...", null);
             }
         });
 
     }
 
     private String getFcontent() {
 
         return edtReply.getText().toString();
     }
 
     private class GetCommentTask extends AsyncTask<Integer, Void, ArrayList<Review>> {
 
         Exception reason = null;
 
         @Override
         protected void onPreExecute() {
 
             progressDialog.show();
             super.onPreExecute();
         }
 
         @Override
         protected ArrayList<Review> doInBackground(Integer... params) {
 
             ArrayList<Comment> comments = null;
             ArrayList<Reply> replys = null;
             ArrayList<Review> reviews = new ArrayList<Review>();
 
             try {
                 comments = wh_dmApi.getPhotoComment(params[0], curPage);
                 int length = comments.size();
                 for (int i = 0; i < length; i++) {
                     int id = comments.get(i).getId();
                     replys = wh_dmApi.getReply(comments.get(i).getId());
                     Review review = new Review();
                     review.setComment(comments.get(i));
                     if (replys != null) {
                         review.setReply(replys);
                     }
                     reviews.add(review);
                 }
                 return reviews;
             } catch (Exception e) {
                 e.printStackTrace();
                 reason = e;
                 return null;
             }
 
         }
 
         @Override
         protected void onPostExecute(ArrayList<Review> result) {
 
             if (result != null && result.size() > 0) {
                 lv.setVisibility(View.VISIBLE);
                 if (isFirstLauncher && result.size() < 6) {
                     lv.removeFooterView(footer);
                     isFirstLauncher = false;
                 }
                adapter.clearItem();
                 for (int i = 0; i < result.size(); i++) {
                     ArrayList<Reply> replys = result.get(i).getReply();
                     Comment comment = result.get(i).getComment();
                     if (replys != null && replys.size() > 0) {
                    	floorAdapter = new NewsReplyFloorAdapter(PhotoReplyActivity.this);
                         floorAdapter.setList(result.get(i).getReply());
                         adapter.addItem(getString(R.string.review_name), comment.getDtime(),
                                 comment.getMsg(), "" + comment.getGood(), floorAdapter,
                                 comment.getId());
                     } else {
                         adapter.addItem(getString(R.string.review_name), comment.getDtime(),
                                 comment.getMsg(), "" + comment.getGood(), null, comment.getId());
                     }
                 }
 
             } else {
                 NotificationUtil.showShortToast(getString(R.string.review_no_more),
                         PhotoReplyActivity.this);
                 if (adapter.getCount() == 0) {
                     lv.setVisibility(View.INVISIBLE);
                 }
             }
             progressDialog.dismiss();
             super.onPostExecute(result);
 
         }
 
     }
 
     private class PushTopTask extends AsyncTask<String, Void, PostResult> {
         Exception reason = null;
 
         @Override
         protected PostResult doInBackground(String... params) {
 
             PostResult result = null;
             try {
                 result = wh_dmApi.addTop(params[0],
                         Preferences.getMachineId(PhotoReplyActivity.this));
                 return result;
             } catch (Exception e) {
                 e.printStackTrace();
                 reason = e;
                 return null;
             }
         }
 
         @Override
         protected void onPostExecute(PostResult result) {
 
             if (result != null && result.getResult()) {
                 NotificationUtil.showShortToast(getString(R.string.thanks_gelivable),
                         PhotoReplyActivity.this);
                 handler.sendEmptyMessage(MSG_GET_COMMENT);
             } else {
                 if (result != null) {
                     NotificationUtil.showShortToast(result.getMsg(), PhotoReplyActivity.this);
                 } else if (wh_dmApp.isConnected()) {
                     NotificationUtil.showShortToast(getString(R.string.badconnect),
                             PhotoReplyActivity.this);
                 } else {
                     NotificationUtil.showShortToast(getString(R.string.check_network),
                             PhotoReplyActivity.this);
                 }
             }
             super.onPostExecute(result);
         }
 
     }
 
     private class ReplyTask extends AsyncTask<String, Void, Boolean> {
         Exception reason = null;
 
         @Override
         protected Boolean doInBackground(String... params) {
 
             boolean isReply = false;
             try {
                 isReply = wh_dmApi.addReply(getFcontent(), params[0]);
                 return isReply;
             } catch (Exception e) {
                 e.printStackTrace();
                 reason = e;
                 return false;
             }
 
         }
 
         @Override
         protected void onPostExecute(Boolean result) {
 
             if (result) {
                 NotificationUtil.showShortToast(getString(R.string.reply_succeed),
                         PhotoReplyActivity.this);
                handler.sendEmptyMessage(MSG_GET_COMMENT);
             } else {
                 NotificationUtil.showShortToast(getString(R.string.reply_fail),
                         PhotoReplyActivity.this);
             }
             super.onPostExecute(result);
         }
 
     }
 
     private class ReviewTask extends AsyncTask<String, Void, Boolean> {
         Exception reason = null;
 
         @Override
         protected void onPreExecute() {
 
             progressDialog.show();
             super.onPreExecute();
         }
 
         @Override
         protected Boolean doInBackground(String... params) {
 
             boolean result = false;
             try {
                 result = wh_dmApi.addPhotoReview(params[0], id);
                 return true;
             } catch (Exception e) {
                 reason = e;
                 e.printStackTrace();
                 return false;
             }
         }
 
         @Override
         protected void onPostExecute(Boolean result) {
 
             if (result) {
                 NotificationUtil.showShortToast(getString(R.string.review_succeed),
                         PhotoReplyActivity.this);
                handler.sendEmptyMessage(MSG_GET_COMMENT);
             } else {
                 NotificationUtil.showShortToast(getString(R.string.review_fail),
                         PhotoReplyActivity.this);
             }
             progressDialog.dismiss();
             super.onPostExecute(result);
         }
 
     }
 
 }
