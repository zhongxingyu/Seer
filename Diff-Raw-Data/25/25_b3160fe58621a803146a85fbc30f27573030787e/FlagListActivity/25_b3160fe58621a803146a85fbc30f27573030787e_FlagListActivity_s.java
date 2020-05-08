 package com.quiz.flag;
 
 import java.util.List;

 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 /**
 *
 * Screen shows all country name and their respective flags.
 * @author Muammil
 */
 
 public class FlagListActivity extends Activity {
 
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.listview_screen);
     ListView flagList = (ListView) findViewById(R.id.lv_listview);
     FlagListAdapter adapter = new FlagListAdapter(R.layout.listflag_row, Countries.getAllCountries());
     flagList.setAdapter(adapter);
   }
 
   private final class FlagListAdapter extends ArrayAdapter<Countries> {
     private final int layoutResourceId;
 
     public FlagListAdapter(int layoutResourceId, List<Countries> countries) {
       super(getApplicationContext(), layoutResourceId, countries);
       this.layoutResourceId = layoutResourceId;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
       View view = convertView;
       if (view == null) {
         LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         view = inflater.inflate(layoutResourceId, parent, false);
       }
       Countries country = getItem(position);
       if (country != null) {
         TextView textView = (TextView) view.findViewById(R.id.tv_listflag);
         ImageView imageView = (ImageView) view.findViewById(R.id.iv_listflag);
         textView.setText(country.getName());
         imageView.setImageResource(country.getDrawableId());
       }
       return view;
     }
   }
 }
