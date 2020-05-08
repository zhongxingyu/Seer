 package com.basicapps.redditpics.activity;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 import com.basicapps.redditpics.R;
 import com.basicapps.redditpics.adapter.PostAdapter;
 import com.basicapps.redditpics.model.ListingResponse;
 import com.basicapps.redditpics.model.Post;
 import com.fasterxml.jackson.core.JsonParseException;
 import com.fasterxml.jackson.databind.JsonMappingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 public class ImageListActivity extends Activity {
 	
 	private ListView imageListView;
 	private PostAdapter postAdapter;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.image_list_activity_layout);
 		
 		imageListView = (ListView)findViewById(R.id.imageList_listView);
 		imageListView.setOnItemClickListener( new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position,
 					long id) {
 				Post clickedPost = (Post)parent.getAdapter().getItem(position);
 				Intent fullscreenImageIntent = new Intent(ImageListActivity.this, FullscreenImageActivity.class);
 				fullscreenImageIntent.putExtra("image_url", clickedPost.getPostData().getImageUrl());
 				fullscreenImageIntent.putExtra("thumbnail_url", clickedPost.getPostData().getThumbnailUrl());
 				ImageListActivity.this.startActivity(fullscreenImageIntent);
 			}
 		});
 		
 		new ListingDownloaderAsyncTask().execute("test");
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 	}
 	
 	private class ListingDownloaderAsyncTask extends AsyncTask<String, Void, ListingResponse> {
 
 		@Override
 		protected ListingResponse doInBackground(String... params) {
 			ListingResponse listingResponse = null;
 			
 			try {
 				ObjectMapper mapper = new ObjectMapper();
 				URL url = new URL("http://www.reddit.com/r/pics/hot.json");
 				listingResponse = mapper.readValue(url.openStream(), ListingResponse.class);
 			} catch (JsonParseException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (JsonMappingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (MalformedURLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			return listingResponse;
 		}
 		
 		@Override
 		protected void onPostExecute(ListingResponse result) {
			if(result == null){
				return ;
			}
 			ImageListActivity.this.postAdapter = new PostAdapter(ImageListActivity.this, result.getPosts());
 			ImageListActivity.this.imageListView.setAdapter(ImageListActivity.this.postAdapter);
 		}
 		
 	}
 
 }
