 package com.eldridge.twitsync.fragment;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.os.Bundle;
 import android.text.Html;
 import android.text.format.DateUtils;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.eldridge.twitsync.R;
 import com.eldridge.twitsync.activity.TweetDetailActivity;
 import com.eldridge.twitsync.beans.MediaUrlEntity;
import com.eldridge.twitsync.util.LinkifyWithTwitter;
 import com.eldridge.twitsync.util.TypeEnum;
 import com.squareup.picasso.Picasso;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import butterknife.InjectView;
 import butterknife.Views;
 import twitter4j.MediaEntity;
 import twitter4j.Status;
 import twitter4j.URLEntity;
 
 /**
  * Created by reldridge1 on 8/19/13.
  */
 public class TweetDetailFragment extends SherlockFragment {
 
     private static final String TAG = TweetDetailFragment.class.getSimpleName();
 
     private LinearLayout detailLoadingWrapper;
 
     @InjectView(R.id.profileImage) ImageView profileImage;
     @InjectView(R.id.retweetCount) TextView retweetCount;
     @InjectView(R.id.tweetInfo) TextView tweetInfo;
     @InjectView(R.id.tweetText) TextView tweetText;
     @InjectView(R.id.tweetStatusText) TextView tweetStatusText;
 
     @InjectView(R.id.mediaLayout) LinearLayout mediaLayout;
     @InjectView(R.id.mediaWebView) WebView mediaWebView;
     @InjectView(R.id.mediaLoadingIndicator) ProgressBar mediaLoadingIndicator;
 
     @InjectView(R.id.imageLayout) LinearLayout imageLayout;
     @InjectView(R.id.mediaImage) ImageView mediaImage;
 
     private List<MediaUrlEntity> mediaUrlEntities = new ArrayList<MediaUrlEntity>();
     private static SimpleDateFormat sdf = new SimpleDateFormat("MMM d - k:ma");
 
     private String userName, userHandle;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setHasOptionsMenu(true);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.tweet_detail_layout, container, false);
         Views.inject(this, v);
         detailLoadingWrapper = (LinearLayout) v.findViewById(R.id.detailLoadingWrapper);
         return v;
     }
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
     }
 
     @SuppressLint("SetJavaScriptEnabled")
     @Override
     public void onViewCreated(View view, Bundle savedInstanceState) {
         super.onViewCreated(view, savedInstanceState);
         //toggle view to show loading view (won't be there long)
         toggleLoadingView();
         getSherlockActivity().getSupportActionBar().setTitle(R.string.tweet_details_ab_title);
         getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
         //Setup webview
         mediaWebView.getSettings().setJavaScriptEnabled(true);
         mediaWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
         mediaWebView.setWebViewClient(new DetailWebViewClient());
         mediaWebView.setWebChromeClient(new DetailWebChromeClient());
         //Access parent activity and get status object from parent activity
         TweetDetailActivity tweetDetailActivity = (TweetDetailActivity)getSherlockActivity();
         Status status = tweetDetailActivity.getStatus();
         //Load launcher icon as placeholder
         Picasso.with(tweetDetailActivity.getApplicationContext()).load(R.drawable.ic_launcher).resize(100, 100).into(profileImage);
         //Set retweet count
         retweetCount.setText(String.format(getResources().getString(R.string.retweet_count_text), String.valueOf(status.getRetweetCount())));
         //Setup string buffer for tweet status info
         StringBuffer sb = new StringBuffer();
         sb.append(formatDate(status.getCreatedAt()));
         sb.append(" ");
         sb.append(String.format(getSherlockActivity().getResources().getString(R.string.detail_via_info), Html.fromHtml(status.getSource())));
 
         //Check if tweet was retweeted and load proper info based on that info
         if (status.isRetweet()) {
             userName = status.getRetweetedStatus().getUser().getName();
             userHandle = status.getRetweetedStatus().getUser().getScreenName();
 
             Picasso.with(tweetDetailActivity.getApplicationContext())
                     .load(status.getRetweetedStatus().getUser().getBiggerProfileImageURLHttps())
                     .resize(125, 125)
                     .into(profileImage);
             sb.append(" ");
             sb.append(String.format(getSherlockActivity().getResources().getString(R.string.detail_retweet_username), status.getUser().getScreenName()));
         } else {
             userName = status.getUser().getName();
             userHandle = status.getUser().getScreenName();
 
             Picasso.with(tweetDetailActivity.getApplicationContext())
                     .load(status.getUser().getBiggerProfileImageURLHttps())
                     .resize(125, 125)
                     .into(profileImage);
         }
         //Load text for tweet info
         tweetInfo.setText(String.format(getSherlockActivity().getString(R.string.detail_tweet_info), userName, userHandle));
         //Load tweetStatus info
         tweetStatusText.setText(sb.toString());
 
         //Load tweet text
         tweetText.setText(status.getText());
        LinkifyWithTwitter.addLinks(tweetText, LinkifyWithTwitter.ALL);
 
         //Add all known media url's to the list of URL's
         if ( status.getMediaEntities() != null && status.getMediaEntities().length > 0 ) {
             MediaEntity[] mediaEntities = status.getMediaEntities();
             for (MediaEntity entity : mediaEntities) {
                 mediaUrlEntities.add(new MediaUrlEntity(entity, TypeEnum.parse(entity.getType()) == TypeEnum.PHOTO));
             }
         }
         //Add all known url's to the list of URL's
         if ( status.getURLEntities() != null && status.getURLEntities().length > 0 ) {
             URLEntity[] urlEntities = status.getURLEntities();
             for (URLEntity entity : urlEntities) {
                 mediaUrlEntities.add(new MediaUrlEntity(entity, false));
             }
         }
 
         if ( !mediaUrlEntities.isEmpty() ) {
             MediaUrlEntity entity = mediaUrlEntities.get(0);
             if ( entity.getUrlEntity() instanceof MediaEntity ) {
                 MediaEntity mediaEntity = (MediaEntity) entity.getUrlEntity();
                 if ( entity.isPhoto() ) {
                     imageLayout.setVisibility(View.VISIBLE);
 
                     Picasso.with(getSherlockActivity().getApplicationContext())
                             .load(mediaEntity.getMediaURL())
                             .placeholder(R.drawable.ic_launcher)
                             .error(R.drawable.ic_launcher)
                             .into(mediaImage);
                 } else {
                     mediaLayout.setVisibility(View.VISIBLE);
                     mediaWebView.loadUrl(mediaEntity.getMediaURL());
                 }
             } else if ( entity.getUrlEntity() instanceof URLEntity ) {
                 URLEntity urlEntity = (URLEntity) entity.getUrlEntity();
                 mediaLayout.setVisibility(View.VISIBLE);
                 mediaWebView.loadUrl(urlEntity.getURL());
             } else {
                 Log.d(TAG, "*** Unknown Entity ***");
             }
         }
 
         //Toggle Loading Progress
         toggleLoadingView();
     }
 
     private class DetailWebViewClient extends WebViewClient {
         @Override
         public boolean shouldOverrideUrlLoading(WebView view, String url) {
             view.loadUrl(url);
             return true;
         }
 
     }
 
     private class DetailWebChromeClient extends WebChromeClient {
         @Override
         public void onProgressChanged(WebView view, int progress) {
             super.onProgressChanged(view, progress);
             if (progress < 100 && mediaLoadingIndicator.getVisibility() == ProgressBar.GONE) {
                 mediaLoadingIndicator.setVisibility(View.VISIBLE);
             }
             mediaLoadingIndicator.setProgress(progress);
             if (progress == 100) {
                 mediaWebView.setVisibility(View.VISIBLE);
                 mediaLoadingIndicator.setVisibility(View.GONE);
             }
         }
     }
 
     private void toggleLoadingView() {
         if (detailLoadingWrapper != null) {
            if (detailLoadingWrapper.getVisibility() == View.GONE) {
                detailLoadingWrapper.setVisibility(View.VISIBLE);
            } else {
                detailLoadingWrapper.setVisibility(View.GONE);
            }
         }
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         super.onCreateOptionsMenu(menu, inflater);
         menu.clear();
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getItemId() == android.R.id.home) {
             getSherlockActivity().finish();
         }
         return super.onOptionsItemSelected(item);
     }
 
     private String formatDate(Date d) {
         return sdf.format(d);
     }
 
 }
