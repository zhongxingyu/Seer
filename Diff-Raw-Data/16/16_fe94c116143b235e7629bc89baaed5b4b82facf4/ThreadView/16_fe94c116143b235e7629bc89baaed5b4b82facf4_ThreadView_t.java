 package cc.hughes.droidchatty;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Hashtable;
 import java.util.List;
 
 import android.R.color;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.text.Spannable;
 import android.text.SpannableString;
 import android.text.Spanned;
 import android.text.style.ForegroundColorSpan;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class ThreadView extends ListActivity {
 
     static final int POST_THREAD = 1;
     
     private LoadingThreadAdapter _adapter;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         // setup the list
         ArrayList<Thread> threads = getSavedThreads();
         _adapter = new LoadingThreadAdapter(this, threads);
         setListAdapter(_adapter);
 
         // listen for clicks
         ListView lv = getListView();
         lv.setOnItemClickListener(new OnItemClickListener()
         {
             public void onItemClick(AdapterView<?> parent, View view, int position, long id)
             {
                 Thread thread = _adapter.getItem(position);
                if (thread != null)
                    displayThread(thread);
             }
         });
     }
 
     @SuppressWarnings("unchecked")
     private ArrayList<Thread> getSavedThreads()
     {
         final Object data = getLastNonConfigurationInstance();
         if (data == null)
             return new ArrayList<Thread>();
         return (ArrayList<Thread>)data;
     }
 
     public Object onRetainNonConfigurationInstance()
     {
         return _adapter.getItems();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.main_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         switch (item.getItemId())
         {
             case R.id.refresh:
                 startRefresh();
                 return true;
             case R.id.add:
                 post();
                 return true;
             case R.id.settings:
                 showSettings();
                 return true;
             case R.id.search:
                 showSearch();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     private void post()
     {
         Intent i = new Intent(this, ComposePostView.class);
         startActivityForResult(i, POST_THREAD);
     }
     
     private void showSettings()
     {
         Intent i = new Intent(this, PreferenceView.class);
         startActivity(i);
     }
     
     private void showSearch()
     {
         Intent i = new Intent(this, SearchView.class);
         startActivity(i);
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data)
     {
         switch (requestCode)
         {
             case POST_THREAD:
                 if (resultCode == RESULT_OK)
                 {
                     // read the resulting thread id from the post
                     int postId = data.getExtras().getInt(SingleThreadView.THREAD_ID);
                     
                     // hey, thats the same thing I just wrote!
                     Intent i = new Intent(this, SingleThreadView.class);
                     i.putExtra(SingleThreadView.THREAD_ID, postId);
                     startActivity(i);
                 }
                 break;
             default:
                 break;
         }
     }
 
     private void startRefresh()
     {
         _adapter.clear();
     }
 
     private void displayThread(Thread thread)
     {
         Intent i = new Intent(this, SingleThreadView.class);
         i.putExtra(SingleThreadView.THREAD_ID, thread.getThreadId());
         i.putExtra(SingleThreadView.THREAD_AUTHOR, thread.getUserName());
         i.putExtra(SingleThreadView.THREAD_POSTED, thread.getPosted());
         i.putExtra(SingleThreadView.THREAD_CONTENT, thread.getContent());
         startActivity(i);
     }
 
     private void updatePostCounts(ArrayList<Thread> threads)
     {
         // set the number of replies that are new
         Hashtable<Integer, Integer> counts = getPostCounts();
         for (Thread t : threads)
         {
             if (counts.containsKey(t.getThreadId()))
                 t.setReplyCountPrevious(counts.get(t.getThreadId()));
         }
 
         try
         {
             storePostCounts(counts, threads);
         } catch (IOException e)
         {
             // yeah, who cares
             Log.e("ThreadView", "Error storing post counts.", e);
         }
     }
 
     private Hashtable<Integer, Integer> getPostCounts()
     {
         Hashtable<Integer, Integer> counts = new Hashtable<Integer, Integer>();
 
         if (getFileStreamPath("post_count.cache").exists())
         {
             // look at that, we got a file
             try {
                 FileInputStream input = openFileInput("post_count.cache");
                 try
                 {
                     DataInputStream in = new DataInputStream(input);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                     String line = reader.readLine();
                     while (line != null)
                     {
                         if (line.length() > 0)
                         {
                             String[] parts = line.split("=");
                             counts.put(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                         }
                         line = reader.readLine();
                     }
                 }
                 finally
                 {
                     input.close();
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
 
         return counts;
     }
 
     final static int POST_CACHE_HISTORY = 1000;
     private void storePostCounts(Hashtable<Integer, Integer> counts, ArrayList<Thread> threads) throws IOException
     {
         // update post counts for threads viewing right now
         for (Thread t : threads)
             counts.put(t.getThreadId(), t.getReplyCount());
 
         List<Integer> postIds = Collections.list(counts.keys());
         Collections.sort(postIds);
 
         // trim to last 1000 posts
         if (postIds.size() > POST_CACHE_HISTORY)
             postIds.subList(postIds.size() - POST_CACHE_HISTORY, postIds.size() - 1);
 
         FileOutputStream output = openFileOutput("post_count.cache", MODE_PRIVATE);
         try
         {
             DataOutputStream out = new DataOutputStream(output);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
 
             for (Integer postId : postIds)
             {
                 writer.write(postId + "=" + counts.get(postId));
                 writer.newLine();
             }
             writer.flush();
         }
         finally
         {
             output.close();
         }
     }
     
     class LoadingThreadAdapter extends LoadingAdapter<Thread>
     {
         private int _pageNumber = 0;
         
         public LoadingThreadAdapter(Context context, List<Thread> objects)
         {
             super(context, R.layout.row, R.layout.row_loading, objects);
         }
 
         @Override
         public void clear()
         {
             _pageNumber = 0;
             super.clear();
         }
 
         @Override
         protected View createView(int position, View convertView, ViewGroup parent)
         {
             View v = convertView;
             if (v == null)
             {
                 LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 v = vi.inflate(R.layout.row, null);
             }
             
             TextView tvUserName = (TextView)v.findViewById(R.id.textUserName);
             TextView tvContent = (TextView)v.findViewById(R.id.textContent);
             TextView tvPosted = (TextView)v.findViewById(R.id.textPostedTime);
             TextView tvReplyCount = (TextView)v.findViewById(R.id.textReplyCount);
             
             // get the thread to display and populate all the data into the layout
             Thread t = getItem(position);
             
             if (t != null)
             {
                 if (tvUserName != null)
                     tvUserName.setText(t.getUserName());
                 if (tvContent != null)
                     tvContent.setText(t.getPreview());
                 if (tvPosted != null)
                     tvPosted.setText(t.getPosted());
                 if (tvReplyCount != null)
                     tvReplyCount.setText(formatReplyCount(t));
 
                 // special highlight for shacknews posts
                 if (t.getUserName().equalsIgnoreCase("Shacknews"))
                     v.setBackgroundColor(Color.rgb(0x19, 0x26, 0x35));
                 else
                     v.setBackgroundColor(color.background_dark);
 
                 // special highlight for employee and mod names
                 tvUserName.setTextColor(User.getColor(t.getUserName()));
             }
             
             return v;
         }
         
         private Spanned formatReplyCount(Thread thread)
         {
             String first = "(" + thread.getReplyCount();
             String second = "";
             String third = ")";
 
             if (thread.getReplyCount() > thread.getReplyCountPrevious())
             {
                 int new_replies = thread.getReplyCount() - thread.getReplyCountPrevious();
                 second = " +" + new_replies;
             }
 
             SpannableString formatted = new SpannableString(first + second + third);
             if (second.length() > 0)
                 formatted.setSpan(new ForegroundColorSpan(Color.GREEN), first.length(), first.length() + second.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
             return formatted;
         }
 
         @Override
         protected ArrayList<Thread> loadData()
         {
             ArrayList<Thread> new_threads = null;
             try
             {
                 // grab threads from the api
                 new_threads = ShackApi.getThreads(_pageNumber + 1);
                 _pageNumber++;
                 
                 // update the "new" post counts
                 updatePostCounts(new_threads);
             } catch (Exception e)
             {
                 Log.e("ThreadView", "Error loading next page.", e);
             }
             
             return new_threads;
         }
         
     }
 }
