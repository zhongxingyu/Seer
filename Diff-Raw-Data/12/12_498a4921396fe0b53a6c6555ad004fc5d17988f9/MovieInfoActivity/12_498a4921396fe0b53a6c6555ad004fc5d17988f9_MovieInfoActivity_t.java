 package suggestcorp.suggestible;
 
 
 
 import java.io.InputStream;
 import java.net.URL;
 
 import org.w3c.dom.Document;
 
 
 import com.google.gson.Gson;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import com.google.gson.Gson;
 
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class MovieInfoActivity extends Activity {
 
 	
 	 public void onCreate(Bundle savedInstanceState) {
 
 	        super.onCreate(savedInstanceState);
 	        setContentView(R.layout.movielayout);
 
 	        Button theaterButton = (Button) findViewById(R.id.theaterbutton);
 	        theaterButton.setOnClickListener(new OnClickListener() {
 	            @Override
 	            public void onClick(View v) {
 	                String titlePlus = title.replace(" ", "+");
 	                String url = "http://www.google.com/movies?q="+titlePlus+"&btnG=Search+Movies&near=02139";
 	                final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
 	                MovieInfoActivity.this.startActivity(intent);
 	            }
 	        });
 
 	        Button imdbButton = (Button) findViewById(R.id.imdbbutton);
 	        imdbButton.setOnClickListener(new OnClickListener() {
 	            @Override
 	            public void onClick(View v) {
 	                String titlePlus = title.replace(" ", "+");
 	                String url = "http://www.imdbapi.com/?t=" + titlePlus; 
 	                String json = GetServerResponse.getURLContents(url); 
 	                Gson gson = new Gson();
 	                String id = gson.fromJson("imdbID", String.class);
 	                url = "http://www.imdb.com/"+ id;
 	                final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
 	                MovieInfoActivity.this.startActivity(intent);
 	            }
 	        });
 	        
 	        
 	       
 	        String imagesrc = getIntent().getStringExtra("imagesrc");
 	        if (imagesrc != null)
 	            new ImageFetcher().execute(imagesrc);
 	        
 	        String title = getIntent().getStringExtra("title");
 	        if (title != null)
	        	((TextView) findViewById(R.id.title)).setText(title);
 	        
 	        String mpaa = getIntent().getStringExtra("mpaa");
 	        if (mpaa != null)
 	        	((TextView) findViewById(R.id.txtmpaa)).setText(mpaa);
 	        
 	        Double rating = getIntent().getDoubleExtra("rating",0.0);
 	        fillStars(rating);
 	        
 	        String runtime = getIntent().getStringExtra("runtime");
 	        if (runtime != null){
 	        	((TextView) findViewById(R.id.txttime)).setText(runtime + " min");
 	        }
 	        	        
 	        String description = getIntent().getStringExtra("description");
 	        if (description != null){
 	        	((TextView) findViewById(R.id.moviedescriptionLarge)).setText(description);
 	        	((TextView) findViewById(R.id.moviedescriptionSmall)).setText(description);
 	        }
 	        
 	        /* not functional at moment
 	         * 
 	        Button moreButton = (Button) findViewById(R.id.showMoreButton);
 	        moreButton.setOnClickListener(new OnClickListener() {
 	            @Override
 	            public void onClick(View v) {
 	                TextView description = (TextView) findViewById(R.id.descriptionSmall);
 	                description.setHeight(LayoutParams.WRAP_CONTENT);
 	            }
 	        });
 	*/
 	    }
 	 
 	public void showMore(View view){		
     	((Button)(findViewById(R.id.movieshowMoreButton))).setVisibility(8);//make it gone
     	((TextView)(findViewById(R.id.moviedescriptionSmall))).setVisibility(8);//make it gone
     	((TextView)(findViewById(R.id.moviedescriptionLarge))).setVisibility(2);//make it visible
     	
     }
 	
 	private class ImageFetcher extends AsyncTask<String, Void, Drawable> {
 
         @Override
         protected Drawable doInBackground(String... params) {
             try {
                 InputStream is = (InputStream) new URL(params[0]).getContent();
                 Drawable d = Drawable.createFromStream(is, "src name");
                 return d;
             } catch (Exception e) {
                 Log.e("Suggestible", "Could not load image at " + params[0]);
                 e.printStackTrace();
                 return null;
             }
         }
 
         @Override
         protected void onPostExecute(Drawable imgDrawable) {
             ImageView image = (ImageView) findViewById(R.id.imageView1);
             image.setImageDrawable(imgDrawable);
         }
     }
 	
 	
 	public void fillStars(double rating){
 		double stars = rating/20.00;
 		if (stars >= 1){
 			((View)(findViewById(R.id.star1))).setBackgroundResource(R.drawable.full_star);
 		}
 		else if(stars >=.5){
 			((View)(findViewById(R.id.star1))).setBackgroundResource(R.drawable.half_star);
 		}
 
 		if (stars >= 2){
 			((View)(findViewById(R.id.star2))).setBackgroundResource(R.drawable.full_star);
 		}
 		else if(stars >=1.5){
 			((View)(findViewById(R.id.star2))).setBackgroundResource(R.drawable.half_star);
 		}
 		if (stars >= 3){
 			((View)(findViewById(R.id.star3))).setBackgroundResource(R.drawable.full_star);
 		}
 		else if(stars >=2.5){
 			((View)(findViewById(R.id.star3))).setBackgroundResource(R.drawable.half_star);
 		}
 		if (stars >= 4){
 			((View)(findViewById(R.id.star4))).setBackgroundResource(R.drawable.full_star);
 		}
 		else if(stars >=3.5){
 			((View)(findViewById(R.id.star4))).setBackgroundResource(R.drawable.full_star);
 		}
 		if (stars >= 5){
 			((View)(findViewById(R.id.star5))).setBackgroundResource(R.drawable.full_star);
 		}
 		else if(stars >=4.5){
 			((View)(findViewById(R.id.star5))).setBackgroundResource(R.drawable.full_star);
 		}
 		
 	}
 
     String title = "Hunger Games";
 
    
 
 
 }
