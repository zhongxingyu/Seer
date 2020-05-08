 package es.uc3m.setichat.activity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import es.uc3m.setichat.activity.SeTIChatConversationActivity;
 import es.uc3m.setichat.service.SeTIChatService;
 import android.app.Activity;
 import android.app.ListFragment;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import es.uc3m.setichat.service.SeTIChatService;
 import es.uc3m.setichat.utils.DatabaseManager;
 import es.uc3m.setichat.utils.Quicksort;
 import es.uc3m.setichat.utils.datamodel.Contact;
 import es.uc3m.setichat.utils.datamodel.Conversation;
 
 /**
  * This activity will show the list of contacts. If a contact is clicked, a new
  * activity will be loaded with a conversation.
  * 
  * 
  * @author Guillermo Suarez de Tangil <guillermo.suarez.tangil@uc3m.es>
  * @author Jorge Blasco Als <jbalis@inf.uc3m.es>
  */
 public class RecentConversationFragment extends ListFragment {
 
 	// Service, that may be used to access chat features
 	private SeTIChatService mService;
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		// TODO Auto-generated method stub
 		super.onAttach(activity);
 		Log.i("CONTACTS", "ATTACHED");
 		mService = ((MainActivity) activity).getService();
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// Populate list with contacts.
 		// Ey, a more fancy layout could be used! You dare?!
 		Log.i("CONTACTS", "CREATED");
 		refreshContactList();
 		
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		// We need to launch a new activity to display
 		// the dialog fragment with selected text.
 		Intent intent = new Intent();
 		intent.setClass(getActivity(), SeTIChatConversationActivity.class);
 		// Meter la informacion del nmero de telfono en el intent para luego
 		// reconocer los mensajes
 		String name = (String) l.getAdapter().getItem(position);
 		String [] parts = name.split(" ");
 		
 		Log.i("Click on conversation", "Conversation: " + position + " opened");
 		intent.putExtra("position", Integer.valueOf(parts[0]).intValue() - 1);
 
 		startActivity(intent);
 	}
 	
 	public void refreshContactList(){
 		Log.i("CONTACTS", "refreshing");
 		DatabaseManager dbm = new DatabaseManager(getActivity());		
 		
 		List<Contact> list = dbm.getAllContacts();
 		List<Conversation> conv = dbm.getAllConversations();
 		
 		
 		//Quicksort sorter = new Quicksort();
 		//conv = sorter.sortByDate(conv);
 		
 		List<String> results = new ArrayList<String>();
 		
 		
 		for(int i = 1; i < conv.size() + 1; i ++){
 			Conversation aux = conv.get(conv.size() - i);
 			boolean repeated = false;
 			
 			Contact cont = dbm.getContact(aux.getidsource());
 			
 			if(cont != null){
 				for(int j = 0; j< results.size(); j++){
					if(results.get(j) != null && results.get(j).equalsIgnoreCase(cont.getId() + " " + cont.getName())) repeated = true;
 					if(results.get(j) == null || repeated) break;
 				}
 				if(repeated == false && !cont.getName().equalsIgnoreCase("setichat@appspot.com")){
 					results.add((cont.getId() + 1) + " " + cont.getName());
 				}
 			}
 					
 		}
 		
 		dbm.close();
 		setListAdapter(new ArrayAdapter<String>(getActivity(),
 				android.R.layout.simple_list_item_activated_1, results));
 	}
 
 }
