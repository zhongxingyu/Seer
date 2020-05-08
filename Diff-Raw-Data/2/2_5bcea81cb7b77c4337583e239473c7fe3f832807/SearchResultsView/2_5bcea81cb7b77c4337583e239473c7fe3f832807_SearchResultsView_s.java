 package cc.hughes.droidchatty;
 
 import java.util.ArrayList;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class SearchResultsView extends ListActivity
 {
     ArrayList<SearchResult> _results;
     SearchResultsAdapter _adapter;
     
     String _term;
     String _author;
     String _parentAuthor;
     
     int _pageNumber = 0;
 
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.search_results);
         
         _term = getIntent().getExtras().getString("terms");
         _author = getIntent().getExtras().getString("author");
         _parentAuthor = getIntent().getExtras().getString("parentAuthor");
         
         _results = new ArrayList<SearchResult>();
         _adapter = new SearchResultsAdapter(this, _results);
         setListAdapter(_adapter);
         
         ListView lv = getListView();
         lv.setOnItemClickListener(new OnItemClickListener()
         {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id)
             {
                 displayThread(_adapter.getItem(position));
             }
         });
         
     }
     
     private void displayThread(SearchResult result)
     {
         Intent i = new Intent(this, SingleThreadView.class);
        i.putExtra(SingleThreadView.THREAD_ID, result.getPostId());
         startActivity(i);
     }
     
     private class SearchResultsAdapter extends LoadingAdapter<SearchResult>
     {
         public SearchResultsAdapter(Context context, ArrayList<SearchResult> items)
         {
             super(context, R.layout.search_result_row, R.layout.row_loading, items);
         }
 
         @Override
         protected View createView(int position, View convertView, ViewGroup parent)
         {
             ViewHolder holder = (ViewHolder)convertView.getTag();
             
             if (holder == null)
             {
                 holder = new ViewHolder();
                 holder.userName = (TextView)convertView.findViewById(R.id.textUserName);
                 holder.content = (TextView)convertView.findViewById(R.id.textContent);
                 holder.posted = (TextView)convertView.findViewById(R.id.textPostedTime);
                 convertView.setTag(holder);
             }
 
             // get the thread to display and populate all the data into the layout
             SearchResult t = getItem(position);
             holder.userName.setText(t.getAuthor());
             holder.content.setText(t.getContent());
             holder.posted.setText(t.getPosted());
                     
             return convertView;
         }
         
         @Override
         protected ArrayList<SearchResult> loadData() throws Exception
         {
             ArrayList<SearchResult> results = ShackApi.search(_term, _author, _parentAuthor, _pageNumber + 1);
             _pageNumber++;
             
             return results;
         }
         
         class ViewHolder
         {
             TextView userName;
             TextView content;
             TextView posted;
         }
         
     }
 
 }
