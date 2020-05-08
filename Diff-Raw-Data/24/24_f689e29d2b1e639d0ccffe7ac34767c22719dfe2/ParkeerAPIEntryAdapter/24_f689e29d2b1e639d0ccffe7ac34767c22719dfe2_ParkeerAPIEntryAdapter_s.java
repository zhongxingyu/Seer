 package nl.miraclethings.parkeerbeter.api;
 
 import nl.miraclethings.parkeerbeter.MainActivity;
 import nl.miraclethings.parkeerbeter.R;
 import android.app.Activity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class ParkeerAPIEntryAdapter extends ArrayAdapter<ParkeerAPIEntry>
 {
 
     MainActivity context;
     ParkeerAPIEntry data[] = null;
    
     public ParkeerAPIEntryAdapter(MainActivity context, ParkeerAPIEntry[] data) {
         super(context, R.layout.listitem, data);
         this.context = context;
         this.data = data;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         View view;
 
         LayoutInflater inflater = ((Activity)context).getLayoutInflater();
         view = inflater.inflate(R.layout.listitem, parent, false);
            
         Button button = (Button)view.findViewById(R.id.listitem_button);
         TextView kenteken= (TextView)view.findViewById(R.id.listitem_kenteken);
         TextView line1= (TextView)view.findViewById(R.id.listitem_line1);
         TextView line2= (TextView)view.findViewById(R.id.listitem_line2);
            
         final ParkeerAPIEntry entry = data[position];
 
        kenteken.setText(entry.kenteken);
         line1.setText(entry.gestart);
         line2.setText(entry.stad + ", " + entry.locatie);
 
         button.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				ParkeerAPIEntryAdapter.this.context.stopParking(entry);
 			}});
         return view;
         
     }
     
     static class EntryHolder
     {
         Button button;
         TextView kenteken;
         TextView line1;
         TextView line2;
     }
     
 }
