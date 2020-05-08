 package com.Litterfeldt.AStory.fragments;
 
 import com.Litterfeldt.AStory.R;
 
 import android.content.Context;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 import android.os.AsyncTask;
 import com.Litterfeldt.AStory.dbConnector.dbBook;
 import com.Litterfeldt.AStory.models.FileSystem;
 import com.Litterfeldt.AStory.services.AudioplayerService;
 import com.handmark.pulltorefresh.library.*;
 import com.Litterfeldt.AStory.models.Book;
 import com.Litterfeldt.AStory.pagerView;
 import java.util.*;
 import com.Litterfeldt.AStory.adapters.LibraryAdapter;
 
 public class LibraryFragment extends Fragment {
 
     private LibraryAdapter adapter;
     private PullToRefreshListView list;
     private TextView emptyText;
 
     @Override
     public void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
     }
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
     }
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
         View view = inflater.inflate(R.layout.library, container, false);
 
         list =(PullToRefreshListView) view.findViewById(R.id.pull_to_refresh_listview);
         emptyText = (TextView) view.findViewById(R.id.emptyText);
 
         bookListAtStartup(view);
 
 
         list.setOnRefreshListener(new PullToRefreshListView.OnRefreshListener<ListView>() {
             @Override
             public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                     new GetDataTask().execute();
             }
         });
 
         list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 getService().getMediaPlayer().playBook(adapter.getItem(i), 0);
             }
         });
 
         return view;
     }
 
     private class GetDataTask extends AsyncTask<Void, Book, ArrayList<Book>> {
         @Override
         protected void onPreExecute(){
             adapter = new LibraryAdapter(getActivity().getApplicationContext(),
                     R.id.pull_to_refresh_listview,
                     new ArrayList<Book>(),
                     Typeface.createFromAsset(((pagerView) getActivity()).getAssets(), "font.ttf"));
             list.setAdapter(adapter);
         }
         @Override
         protected ArrayList<Book> doInBackground(Void...v) {
             Context c = getActivity().getApplicationContext();
            FileSystem f = FileSystem.getInstance();
             dbBook.purge(c);
             ArrayList<ArrayList<String>> bookFolderContent = f.allocateBookFolderContent();
 
             for (ArrayList<String> chapters : bookFolderContent){
                 Book b = f.mockBookFromPath(chapters);
                 dbBook.addBook(c,b);
                ArrayList<Book> books = getService().getBookList();
                publishProgress(books.get(books.size()-1));
             }
             return getService().getBookList();
         }
         @Override
         protected void onProgressUpdate(Book...book) {
             adapter.add(book[0]);
         }
         @Override
         protected void onPostExecute(ArrayList<Book> result) {
             if(!result.isEmpty()){
                 emptyText.setVisibility(View.GONE);
             }else {
                 Log.e("##########","Nothing IN audiobook folder");
                 Toast.makeText(getActivity(),"Your Audiobook-folder is empty, please put books in your /AudioBooks folder on your external storage drive", Toast.LENGTH_LONG);
             }
             list.onRefreshComplete();
             super.onPostExecute(result);
         }
     }
     private void bookListAtStartup(View view){
         AudioplayerService as = getService();
 
        if(as != null && !as.getBookList().isEmpty()){
             adapter = new LibraryAdapter(view.getContext(),
                     R.id.pull_to_refresh_listview,
                    as.getBookList(),
                     Typeface.createFromAsset(((pagerView) getActivity()).getAssets(), "font.ttf"));
             list.setAdapter(adapter);
             emptyText.setVisibility(View.GONE);
         }
         else{
             emptyText.setVisibility(View.VISIBLE);
         }
 
     }
     private AudioplayerService getService(){
         try{
             return ((pagerView) getActivity()).apService;
         }catch (Exception Ignored){} return null;
     }
 
 }
