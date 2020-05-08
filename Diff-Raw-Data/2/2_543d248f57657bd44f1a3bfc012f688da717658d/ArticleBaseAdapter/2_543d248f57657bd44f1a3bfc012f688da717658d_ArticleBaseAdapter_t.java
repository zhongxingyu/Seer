 package dk.whooper.mobilsiden.service;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 import dk.whooper.mobilsiden.R;
 import dk.whooper.mobilsiden.business.Article;
 import dk.whooper.mobilsiden.business.Images;
 import org.jsoup.Jsoup;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 
 public class ArticleBaseAdapter extends BaseAdapter {
 
     private List<Article> articleList;
     private Context mContext;
     private LayoutInflater inflator;
 
     /**
      * @param context
      * @param itemArray
      */
     public ArticleBaseAdapter(Context context, List<Article> itemArray) {
         this.mContext = context;
         this.articleList = itemArray;
         Collections.sort(this.articleList);
         Collections.reverse(this.articleList);
         this.inflator = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     }
 
     @Override
     public int getCount() {
         return articleList.size();
     }
 
     @Override
     public Article getItem(int position) {
         return articleList.get(position);
     }
 
     @Override
     public long getItemId(int position) {
         return position;
     }
 
     public void setItemArray(List<Article> itemArray) {
         this.articleList = itemArray;
         Collections.sort(this.articleList);
         Collections.reverse(this.articleList);
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
 
         final MainListHolder mHolder;
         View v = convertView;
         if (convertView == null) {
             mHolder = new MainListHolder();
             v = inflator.inflate(dk.whooper.mobilsiden.R.layout.article_row_view, null);
             mHolder.thumbnail = (ImageView) v.findViewById(R.id.articleThumbnail);
             mHolder.txt1 = (TextView) v.findViewById(dk.whooper.mobilsiden.R.id.title1);
             mHolder.txt2 = (TextView) v.findViewById(dk.whooper.mobilsiden.R.id.description);
             mHolder.txt2.setTextSize(12f);
             mHolder.txt3 = (TextView) v.findViewById(R.id.date);
             mHolder.txt3.setTextColor(Color.GRAY);
             mHolder.txt3.setTextSize(10f);
             v.setTag(mHolder);
         } else {
             mHolder = (MainListHolder) v.getTag();
         }
 
         if (articleList.get(position).isUnread()) {
             mHolder.txt1.setTypeface(null, Typeface.BOLD);
         } else {
             mHolder.txt1.setTypeface(null, Typeface.NORMAL);
         }
 
         ImageDownloader imageDownloader = new ImageDownloader();
         DatabaseHelper dbConn = new DatabaseHelper(mContext);
         Bitmap image = null;
         try {
             List<Images> images = dbConn.getImagesForArticle(articleList.get(position).getId());
             image = imageDownloader.execute(images.get(0).getThumbUrl()).get();
             mHolder.thumbnail.setVisibility(1);
             mHolder.thumbnail.setImageBitmap(image);
         } catch (InterruptedException e) {
             Log.d("BaseAdapter", "Interrupted exeption " + articleList.get(position).getImages().size() + " - " + articleList.get(position).getHeader());
             mHolder.thumbnail.setVisibility(0);
         } catch (ExecutionException e) {
             Log.d("BaseAdapter", "Execution exeption " + articleList.get(position).getImages().size() + " - " + articleList.get(position).getHeader());
             mHolder.thumbnail.setVisibility(0);
         } catch (NullPointerException e) {
             Log.d("BaseAdapter", "Nullpointer exeption " + articleList.get(position).getImages().size() + " - " + articleList.get(position).getHeader());
             mHolder.thumbnail.setVisibility(0);
         }
         dbConn.close();
 
         String articleText = Jsoup.parse(articleList.get(position).getBodytext()).text();
         String[] articleSentences = articleText.split("\\.");
 
         mHolder.txt1.setText(articleList.get(position).getHeader());
         try {
             Integer.parseInt(articleSentences[1].substring(0, 1)); //To avoid a number (4.000), passing as a sentence
             mHolder.txt2.setText(articleSentences[0] + "." + articleSentences[1] + ".");
         } catch (NumberFormatException e) {
             mHolder.txt2.setText(articleSentences[0] + ".");
        } catch (ArrayIndexOutOfBoundsException e) {
            mHolder.txt2.setText(articleSentences[0] + ".");
         }
 
 
         String[] dateSplitted = articleList.get(position).getPublished().split("T");
         String date = dateSplitted[0];
         String time = dateSplitted[1].split("\\+")[0];
         time = time.substring(0, time.length() - 3);
         mHolder.txt3.setText(date + " - " + time);
 
         return v;
     }
 
     class MainListHolder {
         private TextView txt1;
         private TextView txt2;
         private TextView txt3;
         private ImageView thumbnail;
     }
 
 
 }
