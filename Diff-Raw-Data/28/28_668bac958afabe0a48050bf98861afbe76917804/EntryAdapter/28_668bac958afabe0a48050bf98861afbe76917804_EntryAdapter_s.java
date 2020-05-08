 package com.oppian.oikos.adaptors;
 
 import java.text.NumberFormat;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 import com.oppian.oikos.R;
 import com.oppian.oikos.model.Entry;
 import com.oppian.oikos.util.DateFormatter;
 
 public class EntryAdapter extends ArrayAdapter<Entry> {
 
     private Activity     activity;
     private NumberFormat numberFormat;
 
     public EntryAdapter(Context context, int textViewResourceId, List<Entry> objects, NumberFormat numberFormat) {
         super(context, textViewResourceId, objects);
         this.activity = (Activity) context;
         this.numberFormat = numberFormat;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         View row = convertView;
         if (null == row) {
             // set default row
             LayoutInflater inflator = activity.getLayoutInflater();
             row = inflator.inflate(R.layout.list_item, parent, false);
         }
         // find ui components and set them for this entry
         Entry entry = getItem(position);
         // amount
        TextView amount = (TextView) row.findViewById(R.id.amount);

        amount.setText(numberFormat.format(entry.getAmount() / 100.00));
         // description
         TextView description = (TextView) row.findViewById(R.id.description);
         description.setText(entry.getDescription());
         // date
         TextView entryDate = (TextView) row.findViewById(R.id.entryDate);
         entryDate.setText(DateFormatter.formatToYesterdayOrToday(entry.getEntryDate()));
 
         return row;
     }
 
 }
