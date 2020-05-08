 package de.hska.shareyourspot.android.activites;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.google.android.gms.internal.em;
 
 import de.hska.shareyourspot.android.R;
 import de.hska.shareyourspot.android.domain.Party;
 import de.hska.shareyourspot.android.domain.Picture;
 import de.hska.shareyourspot.android.domain.Post;
 import de.hska.shareyourspot.android.domain.Posts;
 import de.hska.shareyourspot.android.domain.User;
 import de.hska.shareyourspot.android.helper.UserStore;
 import de.hska.shareyourspot.android.restclient.RestClient;
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class PostList extends Activity {
 	private UserStore uStore = new UserStore();
 	private Context ctx = this;
 	protected Posts posts;
 	private Posts returnPost;
 	private List<String> postsTitle;
 	private int maxPostLength = 35;
 	private int count = 1;
 	private RestClient restclient = new RestClient();
 
 	public final String postId = "postId";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.activity_post_list);
 		this.posts = new Posts();
 		
 		User user = null;
 		try {
 			user = uStore.getUser(ctx);
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 			
 		try {
 			returnPost = restclient.getPostByUser(uStore.getUser(ctx));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 			
			if(returnPost != null)
 			{
				this.postsTitle = new ArrayList<String>();
 			
 			for (Post post : this.posts.getAllPosts()) {
 				Date createDate = new Date(post.getCreated());
 				String username = post.getCreatedByUser().getName();
 				String posttext = username + " spottet: " + post.getText();
 				
 				if(posttext.length() >= this.maxPostLength)
 				{
 					posttext = count + "#- " + posttext.substring(0, maxPostLength) + "...";
 				}
 				else
 				{
 					posttext = count +  "#- " + posttext;
 				}
 
 				this.postsTitle.add(posttext);
 				count++;
 			}
 			ListAdapter listenAdapter = new ArrayAdapter<String>(this,
 					android.R.layout.simple_list_item_1, this.postsTitle);
 
 			ListView listUsers = (ListView) findViewById(R.id.list);
 			listUsers.setAdapter(listenAdapter);
 
 			listUsers.setOnItemClickListener(new OnItemClickListener() {
 				@Override
 				public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
 					List<Post> postList = posts.getAllPosts();
 					Long postId = postList.get(position).getPostId();
 					startPostDetail(postId);
 				}
 			});
 
			}
			else
			{
			
			returnPost = new Posts();
			this.postsTitle = new ArrayList<String>();
			this.postsTitle.add("No Spots...you better start sharing!");
			ListAdapter listenAdapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, this.postsTitle);

			ListView listUsers = (ListView) findViewById(R.id.list);
			listUsers.setAdapter(listenAdapter);
			}
 			
 	}
 
 	public void startPostDetail(Long id)
 	{
 		System.out.println(this.posts.toString());
 		Intent intent = new Intent(this, Post_Detail.class);
 		intent.putExtra(postId, id);
 		startActivity(intent);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	  switch (item.getItemId()) {
 	  case R.id.action_logout:
 		  	uStore.logout(ctx);
 	        finish();       
 	        break;
 	        
 	  case R.id.action_groups:
 		  showGroups();
 	        finish();
 	    break;
 	    
 	  case R.id.action_new_post:
 		  newPostCreate();
 	        finish();
 	    break;
 
 	  default:
 	    break;
 	  }
 
 	  return true;
 	} 
 	
 	public void newPostCreate() {
 		Intent intent = new Intent(this, NewPost.class);
 		startActivity(intent);
 	}
 	
 	public void showGroups() {
 		Intent intent = new Intent(this, Groups.class);
 		startActivity(intent);
 	}
 	
 }
