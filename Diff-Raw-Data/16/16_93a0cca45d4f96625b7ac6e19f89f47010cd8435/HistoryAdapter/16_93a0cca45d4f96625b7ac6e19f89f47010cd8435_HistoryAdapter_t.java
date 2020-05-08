 package com.Ticketline.Ticketline;
 
 /**
  * This file is part of AdvancedListViewDemo.
  * You should have downloaded this file from www.intransitione.com, if not, 
  * please inform me by writing an e-mail at the address below:
  *
  * Copyright [2011] [Marco Dinacci <marco.dinacci@gmail.com>]
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 
  * The license text is available online and in the LICENSE file accompanying the distribution
  * of this program.
  */
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.graphics.drawable.Drawable;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class HistoryAdapter extends ArrayAdapter<String> {
 
 	private LayoutInflater mInflater;
 	Drawable drawable;
 	Context ctx;
 		
 	private ArrayList<String> events;
 	//private ArrayList<String> status;
 	//private ArrayList<String> dates;
 	//private ArrayList<String> venues;
 	//private ArrayList<String> quantity;
 	//private ArrayList<String> total;
 	//private ArrayList<String> ids;
 	
 	private ArrayList<History> his;
 	private int mViewResourceId;
 	private int divider;
 	private ViewHolder holder = new ViewHolder();
 	private DividerHolder hold = new DividerHolder();
 	ImageManager im;
 	Typeface first, folksFont;
 	Functions f;
 	
 	public Boolean isScrolling;
 
 	
 	public HistoryAdapter(Context ctx, int viewResourceId,
 			ArrayList<String> event, ArrayList<History> his, Typeface first, Typeface folksFont, int divider) {
			//ArrayList<String> names, ArrayList<String> dates,ArrayList<String> available,ArrayList<String> status,ArrayList<String>ids,ArrayList<String>sta,
 		super(ctx, viewResourceId, event);
 		this.ctx = ctx;
 		mInflater = (LayoutInflater)ctx.getSystemService(
 				Context.LAYOUT_INFLATER_SERVICE);
 		
 		this.events = event;
 		//this.dates = dates;
 		//this.venues = available;
 		//this.quantity = status;
 		//this.total = event;
 		//this.ids = ids;
 		//this.status = sta;
 		this.first = first;
 		this.folksFont = folksFont;
 		
 		this.his =his;
 		mViewResourceId = viewResourceId;
 		
 		this.divider = divider;
 	}
 
 	@Override
 	public int getCount() {
 		return events.size() + 1; // + 1 for the divider. This offsets the array so that the indices can be properly calculated without
 									// nasty out of bounds errors. Looks like a hacky fix but it's not.
 	}
 
 	@Override
 	public String getItem(int position) {
 		return events.get(position);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return 0;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		
 		f = new Functions();
 		
         if(position != divider)
         	if(convertView != null && convertView.getTag() instanceof ViewHolder)
         		holder = (ViewHolder) convertView.getTag();
         	else
         	{
     			holder = new ViewHolder();
     			convertView = mInflater.inflate(mViewResourceId, null);
     	        holder.name = (TextView)convertView.findViewById(R.id.txtVenueName);
     	        holder.ename = (TextView)convertView.findViewById(R.id.txtEventName);
     	        holder.date = (TextView)convertView.findViewById(R.id.txtDate);
     	        holder.sta = (TextView)convertView.findViewById(R.id.txtStatus);
     	        holder.avai = (TextView)convertView.findViewById(R.id.txtAvailable);
         		convertView.setTag(holder);
         	}
         else
         	if(convertView != null && convertView.getTag() instanceof DividerHolder)
         		hold = (DividerHolder)convertView.getTag();
         	else
         	{
         		hold = new DividerHolder();
     			convertView = mInflater.inflate(android.R.layout.simple_list_item_1, null);
         		if(hold.text == null)
         			hold.text = (TextView)convertView.findViewById(android.R.id.text1);
         		convertView.setTag(hold);
         	}
         if(convertView.getTag() instanceof ViewHolder)
 		{
 			//holder.name.setText(events.get(position));
 			History a;
 			if(position >= divider)
 				a = (History)his.get(position-1);
 			else
 				a = (History)his.get(position);
 			holder.name.setText(a.event);
 			holder.name.setTypeface(folksFont);// Folks
 			
 			//String dat = dates.get(position).substring(0,10);
 			//String time = dates.get(position).substring(11).trim();
 			
 			//holder.ename.setText(venues.get(position)+ ", "+f.createDate(dat,time));
 			holder.ename.setText(a.venue+ ", "+f.createDate(a.date.substring(0,10),a.date.substring(11).trim()));
 			holder.ename.setTypeface(folksFont); // Folks
 			
 			
 			holder.date.setVisibility(View.GONE);
 			holder.date.setTypeface(folksFont); // Folks 
 			
 	
 			//holder.sta.setText(quantity.get(position)+" tickets ("+total.get(position)+") "+ status.get(position));
 			holder.sta.setText(a.qt+" tickets ("+a.total+") "+ a.status);
 			holder.sta.setTypeface(folksFont); // Folks
 			
 			
 			//holder.avai.setText(ids.get(position));
 			holder.avai.setText(a.id);
 			holder.avai.setVisibility(View.GONE);
 			holder.avai.setTypeface(folksFont); // Folks
 		}
 		else
 		{
 			hold.text.setText("Previous Events");
 			hold.text.setTypeface(folksFont);
 			hold.text.setTextSize(18);
 			hold.text.setTextColor(Color.rgb(0, 178, 255));
 			hold.text.setGravity(Gravity.CENTER);
 		}
 			
 		return convertView;
 	}
 	static class DividerHolder
 	{
 		public TextView text;
 	}
     static class ViewHolder 
     {
          public TextView name;
          public TextView ename;
          public TextView date;
          public TextView sta;
          public TextView avai;
     }	
 	
 	
 }
