 package eu.hack4europe.postcard;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Gallery;
 import android.widget.ImageView;
 import android.widget.Toast;
 import eu.hack4europe.europeana4j.EuropeanaConnection;
 import eu.hack4europe.europeana4j.EuropeanaItem;
 import eu.hack4europe.europeana4j.EuropeanaQuery;
 import eu.hack4europe.europeana4j.EuropeanaResults;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 public class PostcardActivity extends Activity implements View.OnClickListener {
 
     public static final String MIME_TYPE = "image/jpeg";
     private static final String API_KEY = "HTMQFSCKKB";
 
     public Gallery gallery;
     public EditText editText;
     private Button shareButton;
     public Button findButton;
     public String info;
     public ImageView largeImg;
 
     public final List<String> urlList = new ArrayList<String>();
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         largeImg = (ImageView) findViewById(R.id.bigImage);
         gallery = (Gallery) findViewById(R.id.gallery1);
         editText = (EditText) findViewById(R.id.editText1);
         findButton = (Button) findViewById(R.id.button1);
         shareButton = (Button) findViewById(R.id.share);
 
         findButton.setOnClickListener(this);
         shareButton.setOnClickListener(this);
 
         gallery.setOnItemSelectedListener(new OnItemSelectedListener() {
             @Override
             public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 try {
                     Bitmap bitmap = BitmapFactory.decodeStream((InputStream) new URL(
                             urlList.get(position)).getContent());
                     largeImg.setImageBitmap(bitmap);
                 } catch (IOException ioe) {
                     Toast.makeText(getApplicationContext(), "URL broken", 5000);
                 }
                 // Maybe you can try
                 // i.setImageDrawable(((ImageView) view).getDrawable());
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> arg0) {
                 // TODO Auto-generated method stub
 
             }
         });
     }
 
     @Override
     public void onClick(View view) {
         if (view == shareButton) {
             shareThis();
         } else if (view == findButton) {
             findIt();
         }
     }
 
     private void findIt() {
         info = editText.getText().toString();
         try {
             EuropeanaQuery query = new EuropeanaQuery(info);
             query.setType("IMAGE");
             query.setSubject("postcard");
             //query.setCountry("Riga");
             EuropeanaConnection europeana = new EuropeanaConnection(API_KEY);
            EuropeanaResults res = europeana.search(query, 12);
 
             if (res.getItemCount() > 0) {
                 List<EuropeanaItem> items = res.getAllItems();
                 List<String> itemURLs = new ArrayList<String>();
                 for (EuropeanaItem item : items) {
                     itemURLs.add(item.getBestThumbnail());
                     urlList.add(item.getEnclosure());
                 }
 
 
                 ImageAdapter imga = new ImageAdapter(getApplicationContext(), itemURLs);
                 gallery.setAdapter(imga);
             }
 
         } catch (Exception e) {
             Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), 5000);
         }
     }
 
     private void shareThis() {
         Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
         sharingIntent.setType(MIME_TYPE);
         sharingIntent.putExtra(Intent.EXTRA_TEXT, "Sharing this text");
 
         startActivity(Intent.createChooser(sharingIntent, "Select a Way to Share"));
     }
 
 }
