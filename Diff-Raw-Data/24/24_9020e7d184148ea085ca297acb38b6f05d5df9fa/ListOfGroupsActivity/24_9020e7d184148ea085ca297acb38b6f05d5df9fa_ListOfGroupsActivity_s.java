 package org.SdkYoungHeads.DoIKnowYou;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ListOfGroupsActivity extends Activity implements OnItemClickListener {
 
 
 	protected ListView groups;
 	
 	Group[] listItems;
 	protected ArrayAdapter<Group> adapter;
 		
 	public void onCreate(Bundle savedInstanceState) {
 		// TODO: Tady vypsat seznam skupin...
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.listofgroups);
 		
 		groups = (ListView) this.findViewById(R.id.list_of_groups);
 		
 		/*
 		 * listener pro tlacitko na pridani nove osoby
 		 */
 		 Button next = (Button) findViewById(R.id.addNewPersonButton);
 	        next.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View view) {
 	                Intent myIntent = new Intent(view.getContext(), AddNewPersonActivity.class);
 	                startActivityForResult(myIntent, 0);
 	            }
 
 	        });
 	        
 			/*
 			 * listener pro tlacitko na pridani nove skupiny
 			 */
 	     Button newGroup = (Button) findViewById(R.id.addNewGroupButton);
 	        newGroup.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View view) {
 	                Intent myIntent = new Intent(view.getContext(), AddNewGroupActivity.class);
 	                startActivityForResult(myIntent, 0);
 	            }
 
 	        });
 	}
 	
 	public void onResume() {
 		super.onResume();
 		refill();
 		((Application)getApplication()).selectedGroup = null;
 	}
 	
 	protected void refill() {
 		// TODO: tohle neni hezky...
 		groups.clearChoices();
 		groups.setAdapter(new MyGroupAdapter(this.getBaseContext(), ((Application)getApplication()).getDatabase()));
 		groups.setOnItemClickListener(this);
 	}
 	
	@Override  
	   public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
	super.onCreateContextMenu(menu, v, menuInfo);  
	    menu.setHeaderTitle("Group actions");  
	    menu.add(0, v.getId(), 0, "Edit");
	    menu.add(0, v.getId(), 0, "Delete");  
	}
	
    @Override  
    public boolean onContextItemSelected(MenuItem item) {  
        if(item.getTitle()=="Delete"){function1(item.getItemId());}   // TODO: make group deleting work
        else if (item.getTitle()=="Edit"){function1(item.getItemId());} // TODO: make group editing work
        else {return false;}  
    return true;  
    }  
  
    public void function1(int id){  
        Toast.makeText(this, "function 1 called", Toast.LENGTH_SHORT).show();  
    }  
	
 	class MyGroupAdapter extends ArrayAdapter<Group> {
 		
 		protected Context context;
 		protected GroupContainer gc;
 		
 
 		public MyGroupAdapter(Context context, GroupContainer gc) {
 			super(ListOfGroupsActivity.this, R.layout.listofgroups_row, gc.getGroups());
 			this.context = context;
 			this.gc = gc;
 		}
 		
 		public View getView(int position, View convertView, ViewGroup parent) {
 			LayoutInflater inflater = (LayoutInflater) context
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			View rowView = inflater.inflate(R.layout.listofgroups_row, parent, false);
 			
 			TextView groupName = (TextView) rowView.findViewById(R.id.group_name);
 			TextView groupCount = (TextView) rowView.findViewById(R.id.group_count);
 			ImageView groupIcon = (ImageView) rowView.findViewById(R.id.group_icon);
 //			ImageView groupArrow = (ImageView) rowView.findViewById(R.id.group_arrow);
 			
 			Group g = gc.getGroups()[position];
 			groupName.setText(g.getName());
 			groupCount.setText(Integer.toString(g.getCount()));
 			Bitmap b = g.getIcon();
 			if (b != null) {
 				groupIcon.setImageBitmap(b);
 			}
 			
 			rowView.setTag(g);
 			// Change the icon for Windows and iPhone
 //			String s = values[position];
 //			if (s.startsWith("iPhone")) {
 //				imageView.setImageResource(R.drawable.no);
 //			} else {
 //				imageView.setImageResource(R.drawable.ok);
 //			}
 
 			return rowView;
 		}
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
 		Application app = (Application)getApplication();
 		app.selectedGroup = app.getDatabase().getGroups()[position];
 		Intent i = new Intent(this, GroupActivity.class);
 		startActivity(i);
 	}
 
 	protected Group currentlySelectedGroup;
 	
 	@Override  
 	   public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
 	super.onCreateContextMenu(menu, v, menuInfo);  
 	    menu.setHeaderTitle("Group actions");  
 	    currentlySelectedGroup = (Group)v.getTag();
 	    menu.add(0, v.getId(), 0, "Edit");
 	    menu.add(0, v.getId(), 0, "Delete");  
 	}
 		
     @Override  
     public boolean onContextItemSelected(final MenuItem item) {  
         if(item.getTitle()=="Delete") {
 			new AlertDialog.Builder(this).setMessage(R.string.really_delete_group).
 			setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
 	               public void onClick(DialogInterface dialog, int id) {
 	            	   ListOfGroupsActivity.this.deleteSelectedGroup();
 	            	   dialog.dismiss();
 	               }
 	           }).
 	        setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
 	               public void onClick(DialogInterface dialog, int id) {
 	                    dialog.cancel();
 	               }
 	           }).show();
         } else if (item.getTitle()=="Edit") {
         	function1(item.getItemId());
         } // TODO: make group editing work
         else {
         	return false;
         }  
         return true;  
     }  
   
     protected void deleteSelectedGroup() {
     	((Application)getApplication()).getDatabase().removeGroup(currentlySelectedGroup);
     	refill();
 	}
 
 	public void function1(int id){  
         Toast.makeText(this, "function 1 called", Toast.LENGTH_SHORT).show();  
     }  
 }
