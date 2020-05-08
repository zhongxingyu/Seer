   /*
    * Costituzione Italiana 4.0 
    * Copyright (C) 2012 
    *	 Gnufabio <me@gnufabio.com>, Enrix835 <enrico.trt@gmail.com>
    *
    * This program is free software: you can redistribute it and/or modify
    * it under the terms of the GNU General Public License as published by
    * the Free Software Foundation, either version 3 of the License, or
    * (at your option) any later version.
    *
    * This program is distributed in the hope that it will be useful,
    * but WITHOUT ANY WARRANTY; without even the implied warranty of
    * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    * GNU General Public License for more details.
    *
    * You should have received a copy of the GNU General Public License
    * along with this program.  If not, see <http://www.gnu.org/licenses/>.
    */
 
 package com.zephyrtream.costituzione.components;
 
 import java.util.List;
 
import com.zephyrtream.costituzione.R;

 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 public class EntriesAdapter extends BaseAdapter {
 	private Context context;
     private List<SingleEntry> entries;
     
 	public EntriesAdapter(Context context, List<SingleEntry> entries) {
 		this.context = context;
 		this.entries = entries;
 	}
 	
     
 	@Override
 	public int getCount() {
 		return entries.size();
 	}
 	
 	@Override
 	public Object getItem(int position) {
 		return entries.get(position);
 	}
 	
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 	
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		SingleEntry entry = entries.get(position);
         if (convertView == null) {
             LayoutInflater inflater = (LayoutInflater) context
                     .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_items, null);
         }
         TextView text1 = (TextView) convertView.findViewById(android.R.id.text1);
         text1.setText(entry.getTitle());
         
         TextView text2 = (TextView) convertView.findViewById(android.R.id.text2);
         text2.setText(entry.getBody());
 
         return convertView;
 	}
 	
 
 
 }
