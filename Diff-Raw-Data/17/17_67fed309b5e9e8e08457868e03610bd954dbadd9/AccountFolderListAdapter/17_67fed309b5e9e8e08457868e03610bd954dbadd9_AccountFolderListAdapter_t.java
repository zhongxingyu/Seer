 package com.radicaldynamic.groupinform.adapters;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.radicaldynamic.groupinform.R;
 import com.radicaldynamic.groupinform.activities.AccountFolderActivity;
 import com.radicaldynamic.groupinform.application.Collect;
 import com.radicaldynamic.groupinform.logic.AccountFolder;
 
 public class AccountFolderListAdapter extends ArrayAdapter<AccountFolder>
 {
     private ArrayList<AccountFolder> mItems;
     
     public AccountFolderListAdapter(Context context, int textViewResourceId, ArrayList<AccountFolder> items) {
         super(context, textViewResourceId, items);
         mItems = items;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent)
     {
        String selectedDb = Collect.getInstance().getInformOnlineState().getSelectedDatabase();

        if (selectedDb == null)
            selectedDb = ""; 
                
         View v = convertView;        
 
         if (v == null) {            
             LayoutInflater vi = (LayoutInflater) Collect.getInstance().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             v = vi.inflate(R.layout.folder_list_item, null);
         } 
 
         AccountFolder f = mItems.get(position);
 
         if (f != null) {
             ImageView fi = (ImageView) v.findViewById(R.id.folderIcon);
             TextView tt = (TextView) v.findViewById(R.id.firstLine);
             TextView bt = (TextView) v.findViewById(R.id.secondLine);
             int icon;
             
             if (fi != null) {
                 if (f.getVisibility().equals(AccountFolderActivity.PRIVATE_FOLDER)) {
                     if (f.isReplicated()) {
                        if (selectedDb.equals(f.getId()))
                             icon = R.drawable.folder_remote_backup_blue;
                         else    
                             icon = R.drawable.folder_blue_backup;
                     } else { 
                        if (selectedDb.equals(f.getId()))
                             icon = R.drawable.folder_remote_blue;
                         else    
                             icon = R.drawable.folder_blue;
                     }
                 } else {
                     if (f.isReplicated()) {
                        if (selectedDb.equals(f.getId()))
                             icon = R.drawable.folder_remote_backup_green;
                         else 
                             icon = R.drawable.folder_green_backup;
                     } else {                         
                        if (selectedDb.equals(f.getId()))
                             icon = R.drawable.folder_remote_green;
                         else
                             icon = R.drawable.folder_green;
                     }
                 }        
                 
                 fi.setImageDrawable(Collect.getInstance().getResources().getDrawable(icon));
             }
 
             if (tt != null) {
                 tt.setText("[" + f.getVisibility().toUpperCase() + "] " + f.getName());
             }
             
             if (bt != null) {
                 bt.setText(f.getDescription());
             }
 
         }
 
         return v;
     }
 }
