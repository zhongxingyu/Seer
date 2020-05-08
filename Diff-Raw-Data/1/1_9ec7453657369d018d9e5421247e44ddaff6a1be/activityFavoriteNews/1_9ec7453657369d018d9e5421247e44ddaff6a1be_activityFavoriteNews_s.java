 package ru.news.tagil.activity;
 
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.util.Base64;
 import android.util.Log;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 
 import org.json.JSONObject;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import ru.news.tagil.R;
 import ru.news.tagil.composite.compositeAdsPreview;
 import ru.news.tagil.composite.compositeFirstButton;
 import ru.news.tagil.composite.compositeHeaderSimple;
 import ru.news.tagil.composite.compositeTapePreview;
 import ru.news.tagil.utility.ScrollUpdateActivity;
 
 /**
  * Created by turbo_lover on 12.07.13.
  */
 public class activityFavoriteNews extends ScrollUpdateActivity implements View.OnClickListener {
     private compositeHeaderSimple h_simple;
     private compositeFirstButton cfb;
     @Override
     protected void onCreate(Bundle s) {
         super.onCreate(s);
         needAutoUpdate = h_simple.GetUpdateButtonVisibility();
        Set(Get(CreateJsonForGetNew()),true);
     }
     @Override
     protected void onResume(){
         super.onResume();
         for (int i=0;i< container.getChildCount(); i++) {
             if(container.getChildAt(i).getClass().equals(compositeTapePreview.class)) {
                 compositeTapePreview cp = (compositeTapePreview) container.getChildAt(i);
                 cp.SetFont();
             }
         }
     }
     @Override
     protected void InitializeComponent() {
         super.InitializeComponent();
         h_simple = new compositeHeaderSimple(this);
         cfb = new compositeFirstButton(this);
         tableName = "favorites";
         scriptAddress = getString(R.string.getFavoritesUrl);
         totalCount = GetTotalCount(preferencesWorker.get_login(),null);
     }
     @Override
     protected void SetEventListeners() {
         super.SetEventListeners();
         h_simple.SetHeaderButtonsListener(this);
     }
     @Override
     protected void SetCompositeElements() {
         h_simple.Set(getString(R.string.favoriteText));
         h_simple.UpdateWeather(weatherToday, weatherTomorow);
         h_simple.SetUpdateButtonVisibility(false);
         header.addView(h_simple);
         LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                 LinearLayout.LayoutParams.WRAP_CONTENT);
         footer.addView(cfb,p);
 
     }
     @Override
     protected View CreateViewToAdd(JSONObject obj){
         View v = null;
         try {
             if(obj.getString("type").equals("advert")) {
                 compositeAdsPreview adsPreview = new compositeAdsPreview(this);
                 byte[] e = obj.getString("advert_image").getBytes();
                 byte[] imgbyte = Base64.decode(e, 0);
                 Bitmap bmp = BitmapFactory.decodeByteArray(imgbyte, 0, imgbyte.length);
                 adsPreview.Set(obj.getString("header"),obj.getString("login"),obj.getString("pub_date"),bmp,obj.getString("is_favorite"));
                 adsPreview.setOnClickListener(this);
                 adsPreview.setTag(obj.getString("id"));
                 v = adsPreview;
             } else {
                 compositeTapePreview tapePreview = new compositeTapePreview(this);
                 String[] s = obj.getString("pub_time").split(" ");
                 tapePreview.Set(s[0],s[1],obj.getString("header"),obj.getString("like_count"),obj.getString("text"));
                 tapePreview.setOnClickListener(this);
                 tapePreview.setTag(obj.getString("id"));
                 v = tapePreview;
             }
         } catch (Exception ex) {
             ex.printStackTrace();
             Log.d("CreateViewToAdd_Exception", ex.getMessage() + "\n\n" + ex.toString());
         }
         return v;
     }
     @Override
     protected JSONObject CreateJsonForGet() {
         JSONObject jo = new JSONObject();
         try {
             jo.put("count",GET_COUNT);
             jo.put("login",preferencesWorker.get_login());
             jo.put("pass",preferencesWorker.get_pass());
             String send_time = null;
             if(container.getChildCount() == 0) {
                 send_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
             } else {
                 View v = container.getChildAt(container.getChildCount() - 1);
                 if(v.getClass().equals(compositeTapePreview.class)) {
                     send_time = ((compositeTapePreview) v).getDateTime();
                 } else {
                     send_time = ((compositeAdsPreview) v).getDate();
                 }
             }
             jo.put("time",send_time);
         } catch (Exception ex) {
             ex.printStackTrace();
             Log.d("CreateJsonForGet_Exception", ex.getMessage() + "\n\n" + ex.toString());
         }
         return jo;
     }
     @Override
     public void onClick(View view) {
         Intent i;
        if(view.getClass().equals(compositeTapePreview.class)) {
            i = new Intent(this,activityNewsContent.class);
            compositeTapePreview c = (compositeTapePreview) view;
            i.putExtra("time",c.getTime());
            i.putExtra("date",c.getDate());
            i.putExtra("header",c.getHeader());
            i.putExtra("id_news",(String) c.getTag());
            startActivity(i);
        } else {
            i = new Intent(this,activityReadAds.class);
            compositeAdsPreview c = (compositeAdsPreview) view;
            i.putExtra("title",c.getTitle());
            i.putExtra("img",c.getImg());
            i.putExtra("id_advert",(String) c.getTag());
            i.putExtra("login",c.getPublisher());
            i.putExtra("favorite",c.IsFavorite());
            startActivity(i);
        }
     }
 }
