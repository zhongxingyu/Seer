 package com.mde.potdroid3.fragments;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.Loader;
 import android.text.Html;
 import android.text.Spanned;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.webkit.WebChromeClient;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.widget.ImageButton;
 import android.widget.Toast;
 import com.loopj.android.http.AsyncHttpResponseHandler;
 import com.mde.potdroid3.BaseActivity;
 import com.mde.potdroid3.R;
 import com.mde.potdroid3.helpers.*;
 import com.mde.potdroid3.models.Post;
 import com.mde.potdroid3.models.Topic;
 import com.mde.potdroid3.parsers.TopicParser;
 import org.apache.http.Header;
 
 public class TopicFragment extends PaginateFragment
         implements LoaderManager.LoaderCallbacks<Topic> {
 
     private Topic mTopic;
     private WebView mWebView;
     private TopicJSInterface mJsInterface;
     private BaseActivity mActivity;
 
     public static TopicFragment newInstance(int thread_id, int page, int post_id) {
         TopicFragment f = new TopicFragment();
 
         // Supply index input as an argument.
         Bundle args = new Bundle();
         args.putInt("thread_id", thread_id);
         args.putInt("page", page);
         args.putInt("post_id", post_id);
         f.setArguments(args);
 
         return f;
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         mActivity = (BaseActivity) getSupportActivity();
 
         mWebView = (WebView)getView().findViewById(R.id.topic_webview);
         mJsInterface = new TopicJSInterface(mWebView, getSupportActivity(), this);
 
         // if there is a post_id from the bookmarks call, we set it as the currently
         // visible post.
         mJsInterface.registerScroll(getArguments().getInt("post_id", 0));
 
         registerForContextMenu(mWebView);
 
         mWebView.getSettings().setJavaScriptEnabled(true);
         mWebView.getSettings().setDomStorageEnabled(true);
         mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
         mWebView.getSettings().setAllowFileAccess(true);
         mWebView.addJavascriptInterface(mJsInterface, "api");
         mWebView.setWebChromeClient(new WebChromeClient());
         mWebView.loadData("", "text/html", "utf-8");
         mWebView.setBackgroundColor(0x00000000);
 
         startLoader(this);
 
     }
 
     @Override
     public Loader<Topic> onCreateLoader(int id, Bundle args) {
         int page = getArguments().getInt("page", 1);
         int tid = getArguments().getInt("thread_id", 0);
         int pid = getArguments().getInt("post_id", 0);
         AsyncContentLoader l = new AsyncContentLoader(getSupportActivity(), page, tid, pid);
         showLoadingAnimation();
 
         return l;
     }
 
     @Override
     public void onLoadFinished(Loader<Topic> loader, Topic data) {
         hideLoadingAnimation();
 
         if(data != null) {
 
             // update the topic data
             mTopic = data;
 
             // update html
             mWebView.loadDataWithBaseURL("file:///android_asset/", mTopic.getHtmlCache(),
                     "text/html", "UTF-8", null);
 
             // set title and subtitle of the ActionBar and reload the OptionsMenu
             Spanned subtitleText = Html.fromHtml("Seite <b>"
                     + mTopic.getPage()
                     + "</b> von <b>"
                     + mTopic.getNumberOfPages()
                     + "</b>");
 
            getSupportActivity().supportInvalidateOptionsMenu();
            getActionbar().setTitle(mTopic.getTitle());
            getActionbar().setSubtitle(subtitleText);
 
             // call the onLoaded function
             mActivity.getSidebar().refreshBookmarks();
 
             // populate right sidebar
             mActivity.getRightSidebar().setIsNewPost(mTopic);
 
         } else {
             showError("Fehler beim Laden der Daten.");
         }
     }
 
     @Override
     public void onLoaderReset(Loader<Topic> loader) {
         hideLoadingAnimation();
     }
 
     @Override
     public void onPrepareOptionsMenu(Menu menu) {
         super.onPrepareOptionsMenu(menu);
 
         // only show the paginate buttons if there are before or after the current
         if(!isLastPage()) {
             menu.findItem(R.id.nav_next).setIcon(R.drawable.dark_navigation_fwd).setEnabled(true);
             menu.findItem(R.id.nav_lastpage).setIcon(R.drawable.dark_navigation_ffwd).setEnabled(true);
         }
 
         if(!isFirstPage()) {
             menu.findItem(R.id.nav_firstpage).setIcon(R.drawable.dark_navigation_frwd).setEnabled(true);
             menu.findItem(R.id.nav_previous).setIcon(R.drawable.dark_navigation_rwd).setEnabled(true);
         }
 
     }
 
     public void goToNextPage() {
         // whether there is a next page was checked in onCreateOptionsMenu
         getArguments().putInt("page", mTopic.getPage()+1);
         getArguments().remove("post_id");
         mJsInterface.registerScroll(0);
         restartLoader(this);
     }
 
     public void goToPrevPage() {
         // whether there is a previous page was checked in onCreateOptionsMenu
         getArguments().putInt("page", mTopic.getPage()-1);
         getArguments().remove("post_id");
         mJsInterface.registerScroll(0);
         restartLoader(this);
     }
 
     public void goToFirstPage() {
         // whether there is a previous page was checked in onCreateOptionsMenu
         getArguments().putInt("page", 1);
         getArguments().remove("post_id");
         mJsInterface.registerScroll(0);
         restartLoader(this);
     }
 
     @Override
     public boolean isLastPage() {
         return mTopic == null || mTopic.isLastPage();
     }
 
     @Override
     public boolean isFirstPage() {
         return mTopic == null || mTopic.getPage() == 1;
     }
 
     @Override
     public void refreshPage() {
         restartLoader(this);
     }
 
     public void goToLastPage() {
         // whether there is a previous page was checked in onCreateOptionsMenu
         getArguments().putInt("page", mTopic.getNumberOfPages());
         getArguments().remove("post_id");
         mJsInterface.registerScroll(0);
         restartLoader(this);
     }
 
     public void goToLastPost(int pid) {
         // whether there is a previous page was checked in onCreateOptionsMenu
         getArguments().putInt("post_id", pid);
         getArguments().remove("page");
         mJsInterface.registerScroll(pid);
         restartLoader(this);
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
         // long touch is only resolved if it happened on an image. If so, we
         // offer to open the image with an image application
         WebView.HitTestResult hitTestResult = mWebView.getHitTestResult();
         if (hitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE ||
             hitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
 
             Intent intent = new Intent();
             intent.setAction(Intent.ACTION_VIEW);
             intent.setDataAndType(Uri.parse(hitTestResult.getExtra()), "image/*");
             startActivity(intent);
         }
     }
 
     public void showPostDialog(int post_id) {
         PostDialogFragment menu = new PostDialogFragment(post_id);
         menu.show(mActivity.getSupportFragmentManager(), "postmenu");
     }
 
     protected int getLayout() {
         return R.layout.layout_topic;
     }
 
     static class AsyncContentLoader extends AsyncHttpLoader<Topic> {
         AsyncContentLoader(Context cx, int page, int thread_id, int post_id) {
             super(cx, Topic.Xml.getUrl(thread_id, page, post_id));
         }
 
         @Override
         public Topic processNetworkResponse(String response) {
             try {
                 TopicParser parser = new TopicParser();
                 Topic t = parser.parse(response);
 
                 TopicBuilder b = new TopicBuilder(getContext());
                 t.setHtmlCache(b.parse(t));
 
                 return t;
             } catch (Exception e) {
                 return null;
             }
         }
     }
 
     public class PostDialogFragment extends DialogFragment {
         private Integer mPostId;
 
         PostDialogFragment(int post_id) {
             super();
 
             mPostId = post_id;
         }
 
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             LayoutInflater inflater = getSupportActivity().getLayoutInflater();
             AlertDialog.Builder builder = new AlertDialog.Builder(getSupportActivity());
             View dialog_view = inflater.inflate(R.layout.dialog_post_actions, null);
             builder.setView(dialog_view)
                     .setTitle("Post Aktionen")
                     .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int id) {}
                     });
 
             // Create the AlertDialog object and return it
             final Dialog d = builder.create();
 
             ImageButton quote_button = (ImageButton) dialog_view.findViewById(R.id.button_quote);
             quote_button.setOnClickListener(new View.OnClickListener()
             {
                 @Override
                 public void onClick(View v)
                 {
                     BaseActivity a = (BaseActivity)getSupportActivity();
                     Post p = mTopic.getPostById(mPostId);
 
                     if(mTopic.isClosed())
                         Toast.makeText(a, "Vorsicht, Topic geschlossen!", Toast.LENGTH_LONG).show();
 
                     String text = "[quote=" + mTopic.getId() + "," + p.getId() + ",\""
                             + p.getAuthor().getNick() + "\"][b]\n" + p.getText() + "\n[/b][/quote]";
 
                     a.getRightSidebar().appendText(text);
                     a.openRightSidebar();
 
                     d.cancel();
                 }
             });
 
             ImageButton edit_button = (ImageButton) dialog_view.findViewById(R.id.button_edit);
             edit_button.setOnClickListener(new View.OnClickListener()
             {
                 @Override
                 public void onClick(View v)
                 {
                     BaseActivity a = (BaseActivity)getSupportActivity();
                     Post p = mTopic.getPostById(mPostId);
 
                     SettingsWrapper settings = new SettingsWrapper(a);
 
                     if(p.getAuthor().getId() == settings.getUserId()) {
 
                         a.getRightSidebar().setIsEditPost(mTopic, p);
                         a.getRightSidebar().appendText(p.getText());
                         a.openRightSidebar();
 
                         d.cancel();
 
                     } else {
                         Toast.makeText(a, "Nicht dein Post!", Toast.LENGTH_LONG).show();
                     }
 
                 }
             });
 
             ImageButton bookmark_button = (ImageButton) dialog_view.findViewById(R.id.button_bookmark);
             bookmark_button.setOnClickListener(new View.OnClickListener()
             {
                 @Override
                 public void onClick(View v)
                 {
                     BaseActivity a = (BaseActivity)getSupportActivity();
                     Post p = mTopic.getPostById(mPostId);
 
                     final String url = "set-bookmark.php?PID=" + p.getId()
                             + "&token=" + p.getBookmarktoken();
 
                     Network network = new Network(getActivity());
                     network.get(url, null, new AsyncHttpResponseHandler() {
                         @Override
                         public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                             Toast.makeText(getSupportActivity(), "Bookmark hinzugefügt.",
                                     Toast.LENGTH_SHORT).show();
                             d.cancel();
                         }
                     });
 
                 }
             });
 
             ImageButton url_button = (ImageButton) dialog_view.findViewById(R.id.button_link);
             url_button.setOnClickListener(new View.OnClickListener()
             {
                 @Override
                 public void onClick(View v)
                 {
                     BaseActivity a = (BaseActivity)getSupportActivity();
                     Post p = mTopic.getPostById(mPostId);
 
                     String url = Network.BASE_URL + "thread.php?PID=" + p.getId()
                             + "&TID=" + mTopic.getId() + "#reply_" + p.getId();
 
                     d.cancel();
 
                     Intent i = new Intent(Intent.ACTION_VIEW);
                     i.setData(Uri.parse(url));
                     startActivity(i);
                 }
             });
 
             return d;
         }
     }
 
 }
