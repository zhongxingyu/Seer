 package intnet13.project.contacts;
 
 import android.content.Intent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ViewSwitcher;
 
 /**
 * Flytta all control?
  * @author Jesper
  *
  */
 public class Control {
 	private Intnet13_projectActivity This;
 	private Add_Activity ThisAdd;
 	private modelDB mdb;
 	
 	private ButtonListener bl = new ButtonListener();
 	private ListListener ll = new ListListener();
 	private SpinnerListener sl = new SpinnerListener();
 	
 	private Control c = this;
 	
 	public Control(Intnet13_projectActivity _This, modelDB _mdb){		
 		This = _This;
 		mdb = _mdb;
 	}
 	
 	public void addActivity(Add_Activity _This){		
 		ThisAdd = _This;
 	}
 	
 	public ButtonListener getButtonListener(){return bl;}
 	public class ButtonListener implements OnClickListener{
 
 		@Override
 		public void onClick(View v) {
 			switch (v.getId()){
 			case R.id.loginButton:
 				if(mdb.authenticate(This.userName.getText().toString(), 
 						This.password.getText().toString())){
 					This.vs.showNext();
 					This.fillGroup(mdb.getGroups());
 					This.fillcontactList(mdb.getContacts());
 				}
 				else
 					Toast.makeText(This,
 							"Fel anvndarnamn/lsenord",
 							Toast.LENGTH_SHORT).show();
 				break;
 			
 			case R.id.deleteButton:
 				mdb.deleteGroup((String)((TextView) This.groupSpinner.getSelectedView()).getText());
 				This.fillGroup(mdb.getGroups());
 				This.fillcontactList(mdb.getContacts());
 				break;
 				
 			case R.id.addButton:
 				System.out.println("start");
 					Add_Activity.mdb = mdb;
 					Add_Activity.c = c;
 					Intent i = new Intent(This, Add_Activity.class);	
 					//i.putExtra("mdb", mdb);
 					System.out.println("middle");
 					This.startActivityForResult(i, 0);
 					System.out.println("done");
 					This.fillcontactList(mdb.getContacts());
					This.fillGroup(mdb.getGroups());
 				break;
 				
 			case R.id.searchButton:
 				//skicka med grupp plz
				//This.fillcontactList(mdb.search(This.searchText.getText().toString()));
				This.fillcontactList(mdb.search(This.searchText.getText().toString(),
						(String)((TextView) This.groupSpinner.getSelectedView()).getText()));
 				break;
 			
 			case R.id.editContact:
 				ThisAdd.editable(true);
 				break;
 				
 			case R.id.save:
 				String group = ThisAdd.groupSpinner.getSelectedItem().toString();
				System.out.println("At pos: " + ThisAdd.groupSpinner.getSelectedItemPosition() + "\nGroup name: "+group);
				if(ThisAdd.groupSpinner.getSelectedItemPosition() == 0 &&
						ThisAdd.newGroup.getText().toString().equals(""))
 					group = ThisAdd.newGroup.getText().toString();
 				System.out.println("Group: " + group);
 				mdb.saveContact(ThisAdd.name.getText().toString(),
 						ThisAdd.phoneNumber.getText().toString(),
 						ThisAdd.email.getText().toString(),
 						group);
 				ThisAdd.editable(false);
				This.fillcontactList(mdb.getContacts());
				This.fillGroup(mdb.getGroups());
 				break;		
 			
 			case R.id.remove:
 				mdb.remove(ThisAdd.name.getText().toString());
 				ThisAdd.setResult(ThisAdd.RESULT_OK);
 				ThisAdd.finish();
 				This.fillcontactList(mdb.getContacts());
 				This.fillGroup(mdb.getGroups());
 		        break;
 			}
 		}
 		
 	}
 	
 	public ListListener getListListener(){return ll;}
 	public class ListListener implements OnItemClickListener{
 
 		@Override
 		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 				long arg3) {
 			Add_Activity.mdb = mdb;
 			Add_Activity.c = c;
 			Intent i = new Intent(This, Add_Activity.class);	
 	    	//i.putExtra("mdb", mdb);
 	    	i.putExtra("contactName", (String) ((TextView) arg1).getText());
 			This.startActivityForResult(i, 0);
 			This.fillcontactList(mdb.getContacts());
 		}
 	}
 	
 	public SpinnerListener getSpinnerListener(){return sl;}
 	public class SpinnerListener implements OnItemSelectedListener{
 
 		@Override
 		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
 				long arg3) {
 			This.fillcontactList(mdb.getByGroup((String) ((TextView) arg1).getText()));
 		}
 
 		@Override
 		public void onNothingSelected(AdapterView<?> arg0) {}		
 	}
 
 }
