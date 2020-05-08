 package nl.miraclethings.parkeerbeter;
 
 import android.app.Activity;
 import android.content.Context;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class ParkeerAPIEntryAdapter extends ArrayAdapter<ParkeerAPIEntry>{
 
     MainActivity context;
     ParkeerAPIEntry data[] = null;
    
     public ParkeerAPIEntryAdapter(MainActivity context, ParkeerAPIEntry[] data) {
         super(context, R.layout.listitem, data);
         this.context = context;
         this.data = data;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         View row = convertView;
         EntryHolder holder = null;
        
         if(row == null)
         {
             LayoutInflater inflater = ((Activity)context).getLayoutInflater();
             row = inflater.inflate(R.layout.listitem, parent, false);
            
             holder = new EntryHolder();
             
             holder.button = (Button)row.findViewById(R.id.listitem_button);
             holder.kenteken= (TextView)row.findViewById(R.id.listitem_kenteken);
             holder.line1= (TextView)row.findViewById(R.id.listitem_line1);
             holder.line2= (TextView)row.findViewById(R.id.listitem_line2);
            
             row.setTag(holder);
         }
         else
         {
             holder = (EntryHolder)row.getTag();
         }
        
         final ParkeerAPIEntry entry = data[position];

         holder.kenteken.setText(entry.kenteken);
         holder.line1.setText(entry.gestart);
         holder.line2.setText(entry.stad + ", " + entry.locatie);
 
         holder.button.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				ParkeerAPIEntryAdapter.this.context.stopParking(entry);
 			}});
         return row;
     }
   
     static class EntryHolder
     {
         Button button;
         TextView kenteken;
         TextView line1;
         TextView line2;
     }
     
 }
