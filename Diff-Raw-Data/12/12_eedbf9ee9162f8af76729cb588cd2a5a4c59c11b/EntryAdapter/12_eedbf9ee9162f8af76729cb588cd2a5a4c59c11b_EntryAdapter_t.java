 package com.github.groupENIGMA.journalEgocentrique;
 
 import java.util.List;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.github.groupENIGMA.journalEgocentrique.model.Entry;
 
 public class EntryAdapter extends ArrayAdapter<Entry> {
 
 	private Context context;
 	private List<Entry> entries;
 	private int resource;
 	
 	public EntryAdapter(Context context, int resource, List<Entry> entries){
 		super(context, resource, entries);
 		this.context = context;
 		this.entries = entries;
 		this.resource = resource;
 	}
 	
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         View rowView = convertView;
         if(rowView == null){
             // This a new view we inflate the new layout
             LayoutInflater inflater = (LayoutInflater) context
                     .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(resource, parent, false);
         }
         // Now we can fill the layout with the right values
         TextView tv = (TextView) rowView.findViewById(R.id.EntryNote);
         tv.setText(entries.get(position).getNote());
         ImageView img = (ImageView)rowView.findViewById(R.id.EntryMoodEmote);
         if(entries.get(position).getMood() != null){
             img.setImageResource(entries.get(position).getMood().getEmoteId(context));
         }
        else
        	img.setImageResource(R.drawable.ic_launcher);
         return rowView;
     }
 }
