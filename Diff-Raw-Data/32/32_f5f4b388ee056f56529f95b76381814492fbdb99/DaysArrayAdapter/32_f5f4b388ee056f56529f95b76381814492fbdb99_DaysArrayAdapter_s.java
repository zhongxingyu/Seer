 package com.github.groupENIGMA.journalEgocentrique;
 
 import android.content.Context;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.List;
 
 
 public class DaysArrayAdapter extends ArrayAdapter<Calendar> {
 
     private Context context;
     private int resource;
     private SimpleDateFormat dayFormat;
 
 
     public DaysArrayAdapter(Context context, int resource, List<Calendar> days) {
         super(context, resource, days);
         this.context = context;
         this.resource = resource;
         this.dayFormat = new SimpleDateFormat("d MMMMMMMMMM y");
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         // Get the row layout
        View rowView;
         if (convertView == null) {
             LayoutInflater inflater = (LayoutInflater) context
                     .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = convertView;
             rowView = inflater.inflate(resource, parent, false);
         }
        else {
            rowView = convertView;
        }
 
         // Set a personalized text in the textViewList
         TextView dayTextView = (TextView) rowView.findViewById(R.id.textViewList);
         String dayText = dayFormat.format(getItem(position).getTime());
         dayTextView.setText(dayText);
 
         return rowView;
     }
 }
