 package com.ucb.dcm.list;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.text.TextUtils;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 import com.ucb.dcm.R;
 import com.ucb.dcm.data.Performance;
 import com.ucb.dcm.data.Show;
 import com.ucb.dcm.data.Venue;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 /**
  * Created by kurtguenther on 6/9/13.
  */
 public class ShowAdapter extends BaseAdapter {
 
     LayoutInflater mInflater;
     ArrayList<DataAdapter> items;
     Show mShow;
 
     public ShowAdapter(LayoutInflater inflater, Show show){
         mInflater = inflater;
         mShow = show;
 
         //Hook up the show data
         items = new ArrayList<DataAdapter>();
         items.add(new SummaryAdapter(mShow));
 
         //Showtimes
         items.add(new HeaderAdapter("Showtimes"));
         Cursor c = show.getPerformances();
         while(c.moveToNext()){
             Performance p = new Performance(c);
             Venue v = new Venue(c);
             items.add(new PerformanceAdapter(p,v));
         }
 
         //Cast
         items.add(new HeaderAdapter("Cast"));
         ArrayList<String> perfs = mShow.getPerformers();
         for(int i = 0; i < perfs.size(); i++){
             items.add(new PerformerAdapter(perfs.get(i)));
         }
     }
 
     @Override
     public boolean isEnabled(int position) {
         return false;
     }
 
     @Override
     public int getCount() {
         return items.size();
     }
 
     @Override
     public Object getItem(int i) {
         return items.get(i);
     }
 
     @Override
     public long getItemId(int i) {
         //TODO ahoy hoy?
         return 0;
     }
 
     @Override
     public View getView(int i, View view, ViewGroup parent) {
         return items.get(i).getView(i, view, parent);
     }
 
     public interface DataAdapter {
         View getView(int i, View view, ViewGroup parent);
     }
 
     private class SummaryAdapter implements DataAdapter{
         Show mShow;
 
         public SummaryAdapter(Show show){
             this.mShow = show;
         }
 
         private class ImageSetter implements Runnable{
 
             Bitmap bmp;
             ImageView image;
 
             public ImageSetter(ImageView image, Bitmap bmp){
                 this.image = image;
                 this.bmp = bmp;
             }
 
             @Override
             public void run() {
                 image.setImageBitmap(bmp);
 //                bmp.recycle();
 //                bmp = null;
             }
         }
 
         public View getView(int i, View view, ViewGroup parent) {
             View retVal = mInflater.inflate(R.layout.list_show_summary, parent, false);
 
             TextView title = (TextView) retVal.findViewById(R.id.show_title);
             title.setText(mShow.name);
             TextView city = (TextView) retVal.findViewById(R.id.show_city);
             city.setText(mShow.city);
             TextView promo = (TextView) retVal.findViewById(R.id.show_promo);
             promo.setText(mShow.promo);
 
             if(mShow.image != null){
                 final ImageView image = (ImageView) retVal.findViewById(R.id.show_image);
                 image.setVisibility(View.VISIBLE);
 
                 AsyncTask<String, Integer, String> o = new AsyncTask<String, Integer, String>() {
 
                     @Override
                     protected String doInBackground(String... params) {
                         try {
                             Bitmap bitmap = BitmapFactory
                                     .decodeStream((InputStream) new URL(params[0])
                                             .getContent());
 
                            Activity aaa = (Activity) image.getContext();

                            aaa.runOnUiThread(new ImageSetter(image, bitmap));
 
                         } catch (MalformedURLException e) {
                             e.printStackTrace();
                         } catch (IOException e) {
                             e.printStackTrace();
                         } catch (OutOfMemoryError e){
                             e.printStackTrace();
                         }
 
                         return null;
                     }
 
                 };
 
                 o.execute(mShow.image);
             }
 
 
             return retVal;
         }
     }
 
     private class HeaderAdapter implements DataAdapter{
         String headerText;
 
         public HeaderAdapter(String text){
             headerText = text;
         }
 
         public View getView(int i, View view, ViewGroup parent) {
             View retVal = mInflater.inflate(R.layout.list_show_header, parent, false);
             TextView title = (TextView) retVal.findViewById(R.id.header_title);
             title.setText(headerText);
             return retVal;
         }
     }
 
     private class PerformanceAdapter implements DataAdapter{
         Performance p;
         Venue v;
 
         public PerformanceAdapter(Performance performance, Venue venue){
             p = performance;
             v = venue;
         }
 
         public View getView(int i, View view, ViewGroup parent) {
             View retVal = mInflater.inflate(R.layout.list_show_performance, parent, false);
             TextView time = (TextView) retVal.findViewById(R.id.show_performance_time);
 
             Date date = new Date(((long)p.start_date) * 1000);
             SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE hh:mm a");
             String display = simpleDateFormat.format(date);
             time.setText(display);
 
             TextView venue = (TextView) retVal.findViewById(R.id.show_performance_venue);
             venue.setText(v.short_name);
             return retVal;
         }
     }
 
 
     private class PerformerAdapter implements DataAdapter{
         String name;
 
         public PerformerAdapter(String name){
             this.name = name;
         }
 
         public View getView(int i, View view, ViewGroup parent) {
             View retVal = mInflater.inflate(R.layout.list_show_performer, parent, false);
             TextView nameView = (TextView) retVal.findViewById(R.id.show_performer_name);
             nameView.setText(name);
             return retVal;
         }
     }
 }
