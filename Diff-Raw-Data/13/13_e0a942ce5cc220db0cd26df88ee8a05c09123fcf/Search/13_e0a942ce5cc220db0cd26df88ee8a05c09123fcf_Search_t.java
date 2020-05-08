 package com.example.compareit;
 
 
 import android.app.Fragment;
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ListView;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Vector;
 
 /**
  * Created by Nacho on 11/21/13.
  */
 public class Search extends Fragment implements AsyncStuff{
 
     ArrayList<IdString> items;
     // collectionPagerAdapter PagerAdapter;
     CustomAdapter<IdString> adapter;
     //ViewPager mViewPager;
     DisplayMetrics metrics;
     Context ctx;
 
 
    public Search(final Context ctx){
 
         this.ctx = ctx;
 
         this.metrics = new DisplayMetrics();
         WindowManager wm = (WindowManager) this.ctx.getSystemService(Context.WINDOW_SERVICE);
         wm.getDefaultDisplay().getMetrics(metrics);
 
         this.items = new ArrayList<IdString>();
 
 
 
        adapter = new CustomAdapter<IdString>(ctx,
                android.R.layout.simple_list_item_1, items, metrics);
 
 
 
    }
 
     public View onCreateView(LayoutInflater inflater,
                              ViewGroup container, Bundle savedInstanceState) {
         // The last two arguments ensure LayoutParams are inflated
         // properly.
         View vi = inflater.inflate(
                 R.layout.search, container, false);
 
 
 
         // Items
 
         final EditText search_str = (EditText) vi.findViewById(R.id.search_string);
         final ImageButton button = (ImageButton) vi.findViewById(R.id.search_button);
         final ListView resultsView = (ListView) vi.findViewById(R.id.search_results);
 
 
         resultsView.setAdapter(adapter);
 
         button.setOnClickListener(new View.OnClickListener() {
            String query="";
             public void onClick(View v) {
                try {
                     query = URLEncoder.encode(search_str.getText().toString(), "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                 httpTask lookup = new httpTask(ctx, httpTask.GET,
                        "http://compareit.obedmr.com/filter_product/?format=json&search=" + query);
                 lookup.setListener(Search.this);
                 lookup.execute(null);
 
             }
         });
 
 
         return vi;
     }
 
 
     @Override
     public void doStuff(String output) {
 
         Log.i("GET", output);
         String json = output;//.replace("\"{","{").replace(",\"","");
         Gson gson = new Gson();
 
         java.lang.reflect.Type collectionType = new TypeToken<Vector<jsonProduct>>(){}.getType();
         Vector<jsonProduct> carpets = gson.fromJson(json,collectionType);
 
         items.clear();
         for(jsonProduct item : carpets){
 
 
             items.add(new IdString(1, item.name, item.price, "http://compareit.obedmr.com/media/" + item.product_image));
 
 
         }
         adapter.refresh(items);
         adapter.notifyDataSetChanged();
 
 
 
     }
 }
