 package com.adm.findme;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.NavUtils;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.Window;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TabHost;
 import android.widget.TabHost.TabSpec;
 import android.widget.Toast;
 
 
 public class Contact extends android.support.v4.app.FragmentActivity {
 
 	List<DataContact> contactos = new ArrayList<DataContact>();
 	List<DataGroup> grupos = new ArrayList<DataGroup>();
 	TabHost host; 
 
 	ArrayAdapter<String> adapter;
 	ListView listViewContacts;
 	ListView listViewGroups; 
 	ArrayAdapter<String> adapter_contact;
 	ArrayAdapter<String> adapter_group;
 	private ContactDAO contactsource;
 	private GroupDAO groupsource;
 
 	private ListAdapter listContacts; 	//Adapter that relates the listViewContacts with contactsList
 	private ListAdapter listgroups;		//Adapter that relates the listViewGroups with groupsList
 	private List<String> contactsList;	//Contains the names of the contacts that will be shown in the listview 
 	private List<String> groupsList;  	//Contains the names of the groups that will be shown in the listview
 	private EditText editTextSearchContact;
 	
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		
 		/*Se define todo lo que es la interfaz de la activity*/
 		setContentView(R.layout.activity_contact);
 		
 		listViewContacts = (ListView) findViewById(R.id.listViewContacts);
 		listViewGroups = (ListView) findViewById(R.id.listViewGroups);
 		
 		listViewContacts.setTextFilterEnabled(true);
 		listViewGroups.setTextFilterEnabled(true);
 
 		groupsList = new ArrayList<String>();
 		
 		for (int i = 0; i < grupos.size(); i++) {
 			groupsList.add(grupos.get(i).getName());
 		}
 		
 		editTextSearchContact = (EditText) findViewById(R.id.searchEditText);
 
 		
 		host = (TabHost) findViewById(R.id.tabHost); 
 		host.setup(); 
 		
 		TabSpec spec = host.newTabSpec("TABCONTACTS"); 
 		spec.setIndicator("Contactos"); 
 		spec.setContent(R.id.listViewContacts); 
 		host.addTab(spec); 
 		
 		spec = host.newTabSpec("TABGROUPS"); 
 		spec.setIndicator("Grupos"); 
 		 
 		spec.setContent(R.id.listViewGroups); 
 		host.addTab(spec); 
 		host.setCurrentTabByTag("TABCONTACTS");
 		
 		/*Se leen los contactos del telefono y se muestran en pantalla a trav�s de AsyncTask*/
 		new LeerContactosAsyncTask().execute();
 		new GetGroupsAsyncTask().execute();
 		
 		listViewContacts.setTextFilterEnabled(true);
 		editTextSearchContact.addTextChangedListener(new TextWatcher() {
 
 			@Override
 			public void afterTextChanged(Editable s) {
 				// TODO Auto-generated method stub
 				Contact.this.adapter_contact.getFilter().filter(s);
 				Contact.this.adapter_group.getFilter().filter(s);
 				
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 				// TODO Auto-generated method stub	
 			}
 			
 		}
 		);
 	}
 	
 	/**Lee los nombres de los contactos de la agenda telef�nica (SOLO DE AQUELLOS QUE TIENEN NUMERO DE TEL�FONO) y devuelve un ArrayList 
 	 * de objetos de clase Contacto (es decir, con nombre y numero de telefono). SOLO 1 NUMERO DE TELEFONO POR CADA CONTACTO. No recibe nada como par�metro. 
 
 	 * @author Carexcer
 	 * @return ArrayList<Contacto> con los nombres y los telefonos de la agenda del telefono.
 	 * */
 	public ArrayList<DataContact> leerContactos(){
 		
 		ArrayList<DataContact> contactos = new ArrayList<DataContact>();		
 		
 		contactsource = new ContactDAO(Contact.this);
 		contactsource.open();
 		contactos = (ArrayList<DataContact>) contactsource.getAllContacts();
 		
 		contactsList = new ArrayList<String>();
 		
 		Uri uri = ContactsContract.Contacts.CONTENT_URI;
 		String[] projection = new String[] { ContactsContract.Contacts._ID,ContactsContract.Contacts.DISPLAY_NAME};
 		
 		String selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + " = '1'";
 				
 		String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + "= 'ASC'";
 		Cursor cursor = getContentResolver().query(uri, projection, selection, null, sortOrder);
 		while (cursor.moveToNext()) {
 			String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
 			Uri PURI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
 			String CID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
 			String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
 			Cursor pCur = getContentResolver().query(PURI,  null, CID + " = ?",  new String[]{id}, null);
 			pCur.moveToNext();
 			String PNUM = ContactsContract.CommonDataKinds.Phone.NUMBER;
 			String displayNumber = pCur.getString(pCur.getColumnIndex(PNUM));
 			pCur.close();
 			DataContact c1 = new DataContact(displayName, displayNumber, false, false);
 			contactos.add(c1);
 
 		}
 		cursor.close();
 		
 		contactos = formatPhoneNumbers(contactos);
 		
 		for (int i = 0; i < contactos.size(); i++) 
 			contactsList.add(contactos.get(i).getName());
 
 		groupsource = new GroupDAO(Contact.this);
 		groupsource.open();
 		grupos = groupsource.getAllgroups();
 		
 		return contactos;
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_contact, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 
 		//Management of visible menu items 
 		int tab = host.getCurrentTab();
 		if (tab==0){
 			menu.getItem(0).setVisible(true);
 			menu.getItem(1).setVisible(false);
 		}else{
 			menu.getItem(1).setVisible(true);
 			menu.getItem(0).setVisible(false);
 		}
 		return super.onPrepareOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {	
 
 		case R.id.menu_update:
 			return true;
 		case R.id.menu_newGroup:
 			DialogFragment dialog = new CreateGroupDialog();
 			dialog.show(getSupportFragmentManager(), "CreateGroupDialog");
 			
 			
 			return true;
 		default:
 			Toast.makeText(Contact.this, "Invalid menu item selection.", Toast.LENGTH_LONG).show();
 			return false;
 				
 		}
 	}
 //		switch (host.getCurrentTab()) {
 //		/*case android.R.id.home:
 //			// This ID represents the Home or Up button. In the case of this
 //			// activity, the Up button is shown. Use NavUtils to allow users
 //			// to navigate up one level in the application structure. For
 //			// more details, see the Navigation pattern on Android Design:
 //			//
 //			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 //			//
 //			NavUtils.navigateUpFromSameTask(this);
 //			return true;*/
 //		case 0:
 //			Toast.makeText(this, "pulsado item update", Toast.LENGTH_SHORT).show();
 //			return true;
 //		case 1:
 //			DialogFragment dialog = new CreateGroupDialog();
 //	        dialog.show(getSupportFragmentManager(), "CreateGroupDialog");
 //			return true;
 //			default:
 //				Toast.makeText(Contact.this, "Algo ha ido mal al clickar en el menu", Toast.LENGTH_LONG).show();
 //		}
 //		return super.onOptionsItemSelected(item);
 //	}
 
 
 	private class LeerContactosAsyncTask extends AsyncTask<Void, Integer, Void>{
 
 		
 		@Override
 		protected void onPreExecute() {			
 			Contact.this.setProgressBarIndeterminateVisibility(true);
 			super.onPreExecute();
 		}
 
 		@Override
 		protected Void doInBackground(Void... params) {
 //			leerContactos();
 			leerContactosBDLocal();
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 
 			//Update graphical interface
 			adapter_contact = new ArrayAdapter<String>(Contact.this,android.R.layout.simple_list_item_1,contactsList);	
 			listViewContacts.setAdapter(adapter_contact);
 			Contact.this.setProgressBarIndeterminateVisibility(false);
 			super.onPostExecute(result);
 		}
 		
 	}
 	
 	private class GetGroupsAsyncTask extends AsyncTask<Void, Integer, Void>{
 
 		
 		@Override
 		protected void onPreExecute() {			
 			Contact.this.setProgressBarIndeterminateVisibility(true);
 			super.onPreExecute();
 		}
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			if(groupsource != null && groupsource.getAllGroupsName().size()>0)
 				groupsList = groupsource.getAllGroupsName();
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(Void result) {
 
 			//Update graphical interface
 			if(groupsList!=null && groupsList.size() > 0 ){
 				adapter_group = new ArrayAdapter<String>(Contact.this,android.R.layout.simple_list_item_1,groupsList);	
 				listViewGroups.setAdapter(adapter_group);
 			}
 			Contact.this.setProgressBarIndeterminateVisibility(false);
 			super.onPostExecute(result);
 		}
 		
 	}
 	
 
 	/**This method format the phone number of each contact, obtaining a format like 6XXXXXXXX.
 	 * The first digit of the number will be 6 and the phone will have 9 digits*
 	 * @param contacts - ArrayList of DataContact with phone numbers unformatted
 	 * @return ArrayList<DataContact> with all contacts with a number format like 6XXXXXXXX
 	 * @author carexcer*/
 	public static ArrayList<DataContact> formatPhoneNumbers(ArrayList<DataContact> contacts){
 
 		int tam = contacts.size();
 		ArrayList<Integer> markedContacts = new ArrayList<Integer>();
 
 		for(int i=0; i<tam; i++){
 
 			if(contacts.get(i).getPhoneNumber() != null){ 
 				/*if the contact has phone number*/
 				contacts.get(i).setPhoneNumber(contacts.get(i).getPhoneNumber().replace(" ", "")); //remove blank spaces
 				int auxLength = contacts.get(i).getPhoneNumber().length();
 				String auxNum = contacts.get(i).getPhoneNumber();
 				if(auxLength!=9){									//if number has more than 9 digits, I get the lasts 9				
 					if(auxLength<9)
 						contacts.get(i).setPhoneNumber("000000000"); //we set this number because like don't start with 6, this will be removed 
 					else if(auxLength>9)
 						contacts.get(i).setPhoneNumber(auxNum.substring(auxLength-9, auxLength));
 				}
 				
 				if(contacts.get(i).getPhoneNumber().startsWith("6")==false ) //If is a fix number, I mark to remove then
 					markedContacts.add(i);
 			}else{			//if the contact hasn't a phone number, we remove it from the list
 				markedContacts.add(i);
 			}
 			contacts.get(i).setName(contacts.get(i).getName()+ " - " + contacts.get(i).getPhoneNumber());
 		}
 
 		int i=0;
 		while(markedContacts.size()>0){
 			contacts.remove(markedContacts.get(0).intValue()-i);
 			markedContacts.remove(0);
 			i++;
 		}		
 		return contacts;
 	}
 	
 
 	
 	
 	public void onUpdateGroup() {
 		Toast.makeText(this, "update", Toast.LENGTH_SHORT).show();
 		adapter_group.clear();
 		new GetGroupsAsyncTask().execute();
 		adapter_group.notifyDataSetChanged();
 		
 	}
 	
 	private void leerContactosBDLocal() {
 		
 		ContactDAO contactDAO = new ContactDAO(Contact.this);
 		contactDAO.open();
 		if(contactDAO.getAllContactsName() != null && contactDAO.getAllContactsName().size()>0)
 			contactsList =contactDAO.getAllContactsName(); 
 		
 	}
 }
