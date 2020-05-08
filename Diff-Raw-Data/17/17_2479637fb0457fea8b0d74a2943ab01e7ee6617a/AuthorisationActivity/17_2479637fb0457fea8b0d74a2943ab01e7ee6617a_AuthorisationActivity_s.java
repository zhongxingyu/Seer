 package org.booncode.android.loco;
 
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.widget.ArrayAdapter;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.view.View;
 import android.widget.ListView;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 
 import java.util.ArrayList;
 
 import android.widget.Toast;
 
 public class AuthorisationActivity extends ListActivity
 {
   private static final String[] ACTION_LIST = new String[]{"Remove", "Cancel"};
   private ArrayAdapter<String> m_adapter;
   protected AuthList m_auth;
   
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
     super.onCreate(savedInstanceState);
     
     SharedPreferences settings = this.getSharedPreferences(AuthList.SETTINGS_FILE, Context.MODE_PRIVATE);
     m_auth = new AuthList(settings);
     
     m_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
       m_auth.sharedList);
     
     setListAdapter(m_adapter);
     getListView().setTextFilterEnabled(true);
   }
   
   @Override
   protected void onStart()
   {
     super.onStart();
   }
   
   @Override
   protected void onStop()
   {
     m_auth.writeCurrent();
     super.onStop();
   }
   
   protected void removePosition(int position)
   {
     String item = m_adapter.getItem(position);
     m_adapter.remove(item);
     m_adapter.notifyDataSetChanged();
   }
   
   @Override
	protected void onListItemClick(ListView l, View v, int position, long id)
   {
     final int item_position = position;
     AlertDialog.Builder builder = new AlertDialog.Builder(this);
     builder.setTitle("Choose Action");
     builder.setItems(ACTION_LIST, new DialogInterface.OnClickListener()
       {
         public void onClick(DialogInterface dialog, int item)
         {
           if (item >= 0 && item < ACTION_LIST.length)
           {
             switch(item)
             {
               case 0:
                 AuthorisationActivity.this.removePosition(item_position);
                 break;
                 
               case 1:
                 AuthorisationActivity.this.finish();
                 break;
             }
           }
         }
       });
     AlertDialog dialog = builder.create();
     dialog.show();
	}
 }
