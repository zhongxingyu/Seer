 package com.artum.shootmaniacenter.adapters;
 
 import android.app.Activity;
 import android.content.Context;
 import android.text.Html;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 import com.artum.shootmaniacenter.R;
 import com.artum.shootmaniacenter.structures.nadeo.RankElement;
 import com.artum.shootmaniacenter.utilities.HtmlFormatter;
 
 import java.util.ArrayList;
 
 
 /**
  * Created by artum on 20/05/13.
  *
  * Adapter per gli elementi delle Ladder
  *
  */
 public class ladderAdapter extends BaseAdapter{
 
     private Activity activity;
     private ArrayList<RankElement> data;
     private static LayoutInflater inflater=null;
 
     public ladderAdapter(Activity a, ArrayList<RankElement> d) {
         activity = a;
         data=d;
         inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
     }
 
     @Override
     public int getCount() {
         return data.size();
     }
 
     @Override
     public Object getItem(int position) {
         return position;
     }
 
     @Override
     public long getItemId(int position) {
         return position;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         View vi=convertView;
         if(convertView==null)
             vi = inflater.inflate(R.layout.ladder_view, null);
         HtmlFormatter formatter = new HtmlFormatter();
 
         TextView name = (TextView)vi.findViewById(R.id.name);       // Name
         TextView rank = (TextView)vi.findViewById(R.id.rank);       // Rank
         TextView points = (TextView)vi.findViewById(R.id.points);   // Ladder Points
         TextView region = (TextView)vi.findViewById(R.id.region);   // Region
 
         RankElement rankItem = data.get(position);
 
         // Setting all values in listview
         if(rankItem != null && rankItem.rank != -1 )                //Controlla che non vi siano stati errori durante la request dal server.
         {
             name.setText(Html.fromHtml(formatter.fromStringToHtml(rankItem.playerData.nick)), TextView.BufferType.SPANNABLE);
             rank.setText(rankItem.rank + " ");
             points.setText(rankItem.points + "LP");
             region.setText(rankItem.playerData.region);
         }
         else
         {
             name.setText("Error retriving data from Nadeo...");
             name.setSelected(true);
            rank.setText("-1 ");
             points.setText("0LP");
             region.setText("GG");
         }
 
         return vi;
     }
 }
