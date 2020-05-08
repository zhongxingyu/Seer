 package org.SdkYoungHeads.DoIKnowYou;
 
 
 import org.SdkYoungHeads.DoIKnowYou.ListOfGroupsActivity.MyGroupAdapter;
 
 import android.app.Activity;
 import android.content.Context;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class AddNewGroupActivity extends Activity {
 	protected Group group;
 	
 	protected ListView personList;
 	SelectedPersonsAdapter adapter;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		group = new Group();
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.addnewgroup);
		adapter.notifyDataSetInvalidated();
 		
 //		Person[] p = ((Application)getApplication()).selectedPersons;
 //		
 //		if(p == null) {
 //			p = new Person[1];
 //			p[0] = new Person();
 //			p[0].setName("No record");
 //		}
 		
 //		personList = (ListView) this.findViewById(R.id.list_of_groups);
 //		adapter = new SelectedPersonsAdapter(this.getBaseContext(), p);
 		
 //		Button addPerson = (Button) findViewById(R.id.addPersonToGroupBtn);
 //        addPerson.setOnClickListener(new View.OnClickListener() {
 //            public void onClick(View view) {
 //                Intent myIntent = new Intent(view.getContext(), SelectPersonsActivity.class);
 //                startActivityForResult(myIntent, 0);
 //            }
 //
 //        });
         
         final TextView name = (TextView)findViewById(R.id.groupNameEdit);
                 
         Button addGroup = (Button) findViewById(R.id.createGroupBtn);
         addGroup.setOnClickListener(new View.OnClickListener() {
         	public void onClick(View view) {
         		group.setName(name.getText().toString());
         		GroupContainer gc = ((Application)getApplication()).getDatabase();
         		if (gc.getGroupByName(name.getText().toString()) != null) {
         			Builder builder = new AlertDialog.Builder(AddNewGroupActivity.this);
         			builder.setMessage(R.string.group_already_exists).
         			setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
         	               public void onClick(DialogInterface dialog, int id) {
         	                    dialog.cancel();
         	               }
         	           });
         			builder.show();
         			return;
         		}
             	gc.addGroup(group);
 				try {
 					gc.save(getBaseContext());
 					finish();
 				} catch (IllegalArgumentException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IllegalStateException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
         	}
         });
 	}
 	
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 //		setAdapter();
 	}
 	
 //	protected void setAdapter() {
 //		
 //		Person[] p = ((Application)getApplication()).selectedPersons;
 //		
 //		if(p == null || p.length == 0) {
 //			p = new Person[1];
 //			p[0] = new Person();
 //			p[0].setName("No record");
 //		}
 //		((Application)getApplication()).selectedPersons = null;
 //		Log.d("????? >>>>>>", ">>>>>>>>>>>>>>>>>>>>>>>>" + p.length);
 //		
 //		personList.clearChoices();
 //		adapter.setData(p);
 //		personList.setAdapter(adapter);
 //		
 ////		personList.invalidate();
 ////		adapter.notifyDataSetChanged();
 //		
 //	}
 	
 class SelectedPersonsAdapter extends ArrayAdapter<Person> {
 		
 		protected Context context;
 		protected Person[] persons;
 		
 
 		public SelectedPersonsAdapter(Context context, Person[] persons) {
 			super(AddNewGroupActivity.this, R.layout.group_row, persons);
 			this.context = context;
 			this.persons = persons;
 		}
 		
 		public void setData(Person[] p) {
 			this.persons = p;
 		}
 		
 		public View getView(int position, View convertView, ViewGroup parent) {
 			LayoutInflater inflater = (LayoutInflater) context
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			View rowView = inflater.inflate(R.layout.group_row, parent, false);
 
 //			ImageView groupIcon = (ImageView) rowView.findViewById(R.id.group_icon);
 //			ImageView groupArrow = (ImageView) rowView.findViewById(R.id.group_arrow);
 			
 			TextView personName = (TextView) rowView.findViewById(R.id.person_name);
 			
 			personName.setText(persons[position].getName());
 			
 		
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
 }
