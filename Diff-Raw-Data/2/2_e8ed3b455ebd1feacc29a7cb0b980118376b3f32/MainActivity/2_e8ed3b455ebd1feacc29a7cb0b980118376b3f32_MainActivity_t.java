 package br.com.caelum.fj59.carangos;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import br.com.caelum.fj59.carangos.adapter.BlogPostAdapter;
 import br.com.caelum.fj59.carangos.delegate.BuscaMaisPostsDelegate;
 import br.com.caelum.fj59.carangos.modelo.BlogPost;
 import br.com.caelum.fj59.carangos.tasks.BuscaMaisPostsTask;
 
 public class MainActivity extends Activity implements BuscaMaisPostsDelegate {
     private ListView postsList;
     private List<BlogPost> posts;
     private BlogPostAdapter adapter;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.posts_list);
 
         this.postsList = (ListView) findViewById(R.id.posts_list);
         this.posts = new ArrayList<BlogPost>();
         this.adapter = new BlogPostAdapter(this, this.posts);
         this.postsList.setAdapter(adapter);
 
         new BuscaMaisPostsTask(this).execute();
     }
 
     @Override
     public void lidaComRetorno(List<BlogPost> resultado){
         this.posts.clear();
         this.posts.addAll(resultado);
         this.adapter.notifyDataSetChanged();
     }
 
     @Override
     public void lidaComErro(Exception e){
        Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
 }
