 package com.subspace.android;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.view.View;
 import android.view.ViewGroup;
 
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 import com.subspace.redemption.R;
 import com.subspace.redemption.dataobjects.Zone;
 
 public class ZoneAdapter extends ArrayAdapter<Zone> {
 	private List<Zone> items;        
 
     public ZoneAdapter(Context context, int textViewResourceId, List<Zone> items) {
             super(context, textViewResourceId, items);
             this.items = items;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
     	ZoneViewItem holder;
     	Zone o = items.get(position);
     	if(convertView==null)
     	{
     		convertView = View.inflate(getContext(), R.layout.zone_item, null);
     		holder = new ZoneViewItem();    		
     		holder.topText = (TextView)convertView.findViewById(R.id.toptext);
     		holder.bottomText = (TextView)convertView.findViewById(R.id.bottomtext);
     		convertView.setTag(holder);
     	}
     	else {
     		holder = (ZoneViewItem)convertView.getTag();
     	}
     	
     	if(o!=null)
     	{    		
     		holder.topText.setText(o.Name + " : Pinging...");
     		
     		if(o.Ping!=0)
     		{
     			holder.topText.setText(o.Name + " : " + o.Population + " Players");
    			holder.bottomText.setText("Ping " + Math.round(o.Ping / 10)*10);
     		}
     		//change color depending on ping
     		if(o.Ping < 0)
     		{
        			holder.topText.setTextColor(Color.DKGRAY);
     			holder.bottomText.setTextColor(Color.DKGRAY);
     			holder.topText.setText(o.Name);
     			holder.bottomText.setText("Ping - Failed to connnect");
     		} 
     		else if(o.Ping > 500)
     		{
     			holder.topText.setTextColor(Color.RED);
     			holder.bottomText.setTextColor(Color.RED);
     		}
     		else if(o.Ping > 150)
     		{
     			holder.topText.setTextColor(Color.YELLOW);
     			holder.bottomText.setTextColor(Color.YELLOW);
     		} else {
     			holder.topText.setTextColor(Color.GREEN);
     			holder.bottomText.setTextColor(Color.GREEN);
     		}
     	}
         return convertView;
     }
 }
