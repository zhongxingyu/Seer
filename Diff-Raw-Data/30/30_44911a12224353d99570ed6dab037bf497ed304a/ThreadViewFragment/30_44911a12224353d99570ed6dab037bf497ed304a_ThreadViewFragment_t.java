 package cc.hughes.droidchatty;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.text.method.LinkMovementMethod;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 public class ThreadViewFragment extends ListFragment
 {
     public static ThreadViewFragment newInstance(int postId, String userName, String posted, String content)
     {
         ThreadViewFragment f = new ThreadViewFragment();
         
         Bundle args = new Bundle();
         args.putInt("postId", postId);
         args.putString("userName", userName);
         args.putString("posted", posted);
         args.putString("content", content);
         
         f.setArguments(args);
         
         return f;
     }
     
     PostLoadingAdapter _adapter;
     int _rootPostId;
     int _currentPostId;
     boolean _postDisplayed = false;
     
     public int getPostId()
     {
        return getArguments().getInt("postId"); 
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
     {
         _rootPostId = getArguments().getInt("postId");
         _currentPostId = _rootPostId;
         
         _adapter = new PostLoadingAdapter(getActivity(), new ArrayList<Post>());
         setListAdapter(_adapter);
         
         return inflater.inflate(R.layout.thread_view, null);
     }
     
     @Override
     public void onActivityCreated(Bundle savedInstanceState)
     {
         super.onActivityCreated(savedInstanceState);
         
         getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
         
         Bundle args = getArguments();
        String action = getActivity().getIntent().getAction();
        Uri uri = getActivity().getIntent().getData();
        
         if (args.containsKey("content"))
         {
             String userName = args.getString("userName");
             String content = args.getString("content");
             String posted = args.getString("posted");
             Post post = new Post(_rootPostId, userName, content, posted, 0);
             displayPost(post);
         }
        else if (action != null && action.equals(Intent.ACTION_VIEW) && uri != null)
        {
            String id = uri.getQueryParameter("id");
            if (id == null)
            {
                ErrorDialog.display(getActivity(), "Error", "Invalid URL Found");
                return;
            }
            
            _currentPostId = Integer.parseInt(id);
            _rootPostId = _currentPostId;
                
        }
         
         // makes links actually clickable
         TextView content = (TextView)getView().findViewById(R.id.textContent);
         content.setMovementMethod(new LinkMovementMethod());
         
         setHasOptionsMenu(true);
     }
 
     @Override
     public void onListItemClick(ListView l, View v, int position, long id)
     {
         displayPost(position);
     }
     
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
     {
         inflater.inflate(R.menu.thread_menu, menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         switch(item.getItemId())
         {
             case R.id.refreshThread:
                 _adapter.clear();
                 return true;
             case R.id.reply:
                 postReply();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
     private static final int POST_REPLY = 0;
     private void postReply()
     {
         Intent i = new Intent(getActivity(), ComposePostView.class);
         i.putExtra(SingleThreadView.THREAD_ID, _currentPostId);
         startActivityForResult(i, POST_REPLY);
     }
     
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data)
     {
         switch (requestCode)
         {
             case POST_REPLY:
                 if (resultCode == Activity.RESULT_OK)
                 {
                     // read the resulting thread id from the post
                     int postId = data.getExtras().getInt("postId");
                     
                     _rootPostId = postId;
                     _currentPostId = postId;
                     _adapter.clear();
                 }
                 break;
             default:
                 break;
         }
     } 
     
     private void displayPost(int position)
     {
         getListView().setItemChecked(position, true);
         Post post = _adapter.getItem(position);
         
         if (!_postDisplayed)
             getListView().setSelection(position);
         
         displayPost(post);
     }
 
     private void displayPost(Post post)
     {
         View view = getView();
         
         TextView userName = (TextView)view.findViewById(R.id.textUserName);
         TextView posted = (TextView)view.findViewById(R.id.textPostedTime);
         TextView content = (TextView)view.findViewById(R.id.textContent);
         ScrollView scroll = (ScrollView)view.findViewById(R.id.scroll);
         
         userName.setText(post.getUserName());
         posted.setText(post.getPosted());
         content.setText(PostFormatter.formatContent(post, content, true));
         scroll.scrollTo(0, 0);
         
         _currentPostId = post.getPostId();
         _postDisplayed = true;
     }
     
     private class PostLoadingAdapter extends LoadingAdapter<Post>
     {
         private boolean _loaded = false;
         
         public PostLoadingAdapter(Context context, ArrayList<Post> items)
         {
             super(context, R.layout.thread_row, R.layout.row_loading, items);
         }
         
         @Override
         public void clear()
         {
             _loaded = false;
             super.clear();
         }
         
         @Override
         protected boolean getMoreToLoad()
         {
             return !_loaded;
         }
 
         @Override
         protected View createView(int position, View convertView, ViewGroup parent)
         {
             TextView content = (TextView)convertView.getTag();
             if (content == null)
             {
                 content = (TextView)convertView.findViewById(R.id.textPreview);
                 convertView.setTag(content);
             }
 
             // get the thread to display and populate all the data into the layout
             Post t = getItem(position);
             content.setPadding(15 * t.getLevel(), 0, 0, 0);
             content.setText(t.getPreview());
 
             return convertView;
         }
 
         @Override
         protected ArrayList<Post> loadData() throws Exception
         {
             ArrayList<Post> posts = ShackApi.getPosts(_rootPostId);
             _loaded = true;
             return posts;
         }
         
         @Override
         protected void afterDisplay()
         {
             try
             {
                 int length = getCount();
                 for (int i = 0; i < length; i++)
                 {
                     if (getItem(i).getPostId() == _currentPostId)
                     {
                         if (!_postDisplayed)
                             displayPost(i);
                         break;
                     }
                 }
             } catch (Exception e)
             {
                 Log.w("DroidChatty", "Error selecting root post", e);
             }
             
         }
     }
     
 }
