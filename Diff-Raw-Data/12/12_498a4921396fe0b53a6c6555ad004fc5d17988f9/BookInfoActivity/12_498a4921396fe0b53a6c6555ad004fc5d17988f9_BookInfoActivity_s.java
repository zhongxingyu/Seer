 package suggestcorp.suggestible;
 
 import java.io.InputStream;
 import java.net.URL;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class BookInfoActivity extends Activity {
     String author = "Herman Melville";
     String title = "Moby Dick";
     
     public void showMore(View view){        
         ((Button)(findViewById(R.id.bookshowMoreButton))).setVisibility(8);//make it gone
         ((TextView)(findViewById(R.id.bookdescriptionSmall))).setVisibility(8);//make it gone
         ((TextView)(findViewById(R.id.bookdescriptionLarge))).setVisibility(2);//make it visible
         
     }
     
 	public void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.booklayout);
 		
 		Button libraryButton = (Button) findViewById(R.id.librarybutton);
         libraryButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 String url = "http://www.yelp.com/biz/cambridge-public-library-cambridge-2";
                 final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
                 BookInfoActivity.this.startActivity(intent);
             }
         });
         
         Button bookstoreButton = (Button) findViewById(R.id.bookstorebutton);
         bookstoreButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 String url = "http://www.yelp.com/biz/mit-coop-cambridge";
                 final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
                 BookInfoActivity.this.startActivity(intent);
             }
         });
         
         Button kindleButton = (Button) findViewById(R.id.kindlebutton);
         kindleButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 String keyword = title.replace(" ", "+") + "+" + author.replace(" ", "+");
                 String url = "http://www.amazon.com/s/ref=nb_sb_noss?rh=n%3A133140011%2Ck%3A"+keyword+"&keywords="+keyword+"&ie=UTF8";
                 final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
                 BookInfoActivity.this.startActivity(intent);
             }
         });
 		
         
        String title = getIntent().getStringExtra("title");
         if (title != null)
         	((TextView) findViewById(R.id.txttitle)).setText(title);
         
         String imagesrc = getIntent().getStringExtra("imagesrc");
         if (imagesrc != null)
             new ImageFetcher().execute(imagesrc);
         
        String author = getIntent().getStringExtra("author");
         if (author != null)
             ((TextView) findViewById(R.id.txtauthor)).setText(author);
         
         String description = getIntent().getStringExtra("description");
         if (description != null){
         	((TextView) findViewById(R.id.bookdescriptionLarge)).setText(description);
         	((TextView) findViewById(R.id.bookdescriptionSmall)).setText(description);
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
 	
 }
