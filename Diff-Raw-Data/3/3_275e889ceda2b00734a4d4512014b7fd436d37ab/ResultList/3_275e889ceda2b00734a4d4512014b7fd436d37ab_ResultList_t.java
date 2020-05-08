 package com.delauneconsulting.AMION;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class ResultList extends ListActivity {
 
     MyAMIONPersonAdapter adapter;
     private final Context context = this;
     private ArrayList<AMIONPerson> persons = null;
 
     private String pwd;
     private String filter;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // Setup the click listener for the ListView items
         OnItemClickListener itemListener = new OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View arg1, int position, long arg3) {
                 Toast.makeText(
                         context,
                         ((AMIONPerson) parent.getItemAtPosition(position)).comment + " "
                                 + ((AMIONPerson) parent.getItemAtPosition(position)).phoneNumber,
                         Toast.LENGTH_SHORT).show();
             }
         };
         getListView().setOnItemClickListener(itemListener);
 
         // Register the ContextMenu for the ListView
         registerForContextMenu(getListView());
 
         // Pull the data that was passed by the calling function
         Intent intent = getIntent();
         // String httpResponse = intent.getDataString();
         Bundle extras = intent.getExtras();
         pwd = extras.getString("pwd");
         filter = extras.getString("filter");
 
         // TODO: Find a better place to store this URL
         String origUrl = "http://www.amion.com/cgi-bin/ocs?Lo=%s&Rpt=619";
         String httpResponse = Helper.getHttpResponseAsString(String.format(origUrl, pwd));
 
         // This is a meant to be a generic List page, so you can set the title
         // to whatever here.
         setPageTitle(httpResponse);
 
         // parse the httpResponse into an ArrayList of AMIONPerson objects
         persons = createAMIONPersonList(httpResponse);
 
         // Setup and bind the adapter for the ListView with the ArrayList
         adapter = new MyAMIONPersonAdapter(this, R.layout.list_item, persons);
         setListAdapter(adapter);
     }
 
     // Set the title of the page
     public void setPageTitle(String s) {
         try {
             // split the result set into lines
             String[] lines = null;
             lines = s.split("\r\n|\r|\n");
 
             String date = lines[0].replace("Assignments for", "").trim();
             this.setTitle(pwd + " | " + filter + " | " + date);
         } catch (Exception e) {
         }
     }
 
     // Create the list of AMIONPerson objects from the httpResonse
     public ArrayList<AMIONPerson> createAMIONPersonList(String s) {
         ArrayList<AMIONPerson> peeps = new ArrayList<AMIONPerson>();
 
         try {
 
             // split the result set into lines
             String[] lines = null;
             lines = s.split("\r\n|\r|\n");
 
             AMIONPerson p;
             for (int i = 0; i < lines.length; i++) {
 
                 if (lines[i].length() > 0 && lines[i].startsWith("\"")) {
 
                     p = new AMIONPerson();
                     p.comment = lines[i];
 
                     int index = lines[i].indexOf("\"", 2);
                     String personName = lines[i].substring(1, index);
 
                     // TODO: Split first and last names out
                     p.lastName = personName;
 
                     // get rid of the name field, since we already have it, then
                     // clean up everything else
                     lines[i] = lines[i].replace("\"" + personName + "\",", "");
                     String[] temp = lines[i].split(",");
                     for (int j = 0; j < temp.length; j++) {
                         temp[j] = temp[j].trim();
                         if (temp[j].contains("\"")) {
                             temp[j] = temp[j].replace("\"", "").trim();
                         }
                     }
 
                     // this is just the "job"
                     p.currentJob = temp[2];
 
                     if (filter.equalsIgnoreCase("OnCall") && !p.currentJob.contains("Bpr Coverage")) {
                         peeps.add(p);
                     } else if (filter.equalsIgnoreCase("BprCoverage")
                             && p.currentJob.contains("Bpr Coverage")) {
                         peeps.add(p);
                     }
                 }
 
             }
 
             // sort the custom ArrayList of AMIONPerson objects
             Collections.sort(peeps, new AMIONPersonComparator());
 
         } catch (Exception e) {
 
         }
         return peeps;
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
 
         menu.setHeaderTitle(((AMIONPerson) getListAdapter().getItem(info.position)).toString());
 
         menu.add(0, Menu.FIRST, Menu.NONE, "View Schedule");
         menu.add(0, Menu.FIRST + 1, Menu.NONE, "Send Page");
         menu.add(0, Menu.FIRST + 2, Menu.NONE, "Link Contact");
     }
 
    @Override
     public boolean onContextItemSelected(MenuItem item) {
 
         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                 .getMenuInfo();
         AMIONPerson p = (AMIONPerson) getListAdapter().getItem(info.position);
 
         AlertDialog.Builder builder = new AlertDialog.Builder(context);
         builder.setMessage(item.getTitle() + " " + p.toString());
         builder.setCancelable(true);
         builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 dialog.cancel();
             }
         });
         builder.show();
 
         return true;
     }
 
     @Override
     // Create the main options menu
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
 
         menu.add(0, Menu.FIRST, Menu.NONE, "Refresh"); // .setIcon(android.R.drawable.ic_menu_refresh);
 
         return true;
     }
 
     // The custom adapter that will handle the ArrayList of AMIONPerson objects
     // in the ListView
     private class MyAMIONPersonAdapter extends ArrayAdapter<AMIONPerson> {
 
         public MyAMIONPersonAdapter(Context context, int textViewResourceId,
                 ArrayList<AMIONPerson> items) {
             super(context, textViewResourceId, items);
             ResultList.this.persons = items;
         }
 
        @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             View v = convertView;
             if (v == null) {
                 LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 v = vi.inflate(R.layout.list_item, null);
             }
             AMIONPerson p = persons.get(position);
             if (p != null) {
                 TextView txtName = (TextView) v.findViewById(R.id.txtName);
                 TextView txtJob = (TextView) v.findViewById(R.id.txtJob);
                 if (txtName != null) {
                     txtName.setText(p.toString());
                 }
                 if (txtJob != null) {
                     txtJob.setText(p.currentJob);
                 }
             }
             return v;
         }
     }
 }
