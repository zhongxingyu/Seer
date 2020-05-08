 package edu.mit.moneyManager.listUtils;
 
 import java.util.ArrayList;
 
 import edu.mit.moneyManager.R;
 import edu.mit.moneyManager.view.ViewCategoryActivity;
 
 import android.content.Context;
 import android.content.Intent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class SharedListAdapter extends ArrayAdapter<String>{
 
     private Context context;
     private ArrayList<String> usernames;
     private LayoutInflater inflator;
     
     public SharedListAdapter(Context context, ArrayList<String> usernames) {
         super(context, 0, usernames);
         this.usernames = usernames;
         this.context = context;
         inflator = (LayoutInflater) context
                 .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 
     }
     
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         View view = convertView;
         final String username = usernames.get(position);
         if (username != null) {
             view = inflator.inflate(R.layout.list_entry_shared, null);
             
            view.setOnClickListener(null);
            view.setOnLongClickListener(null);
            view.setLongClickable(false);
             
             TextView userName = (TextView) view.findViewById(R.id.user_shared_name);            
 
             userName.setText(username);
             
         }
         return view;
     }
 
 }
