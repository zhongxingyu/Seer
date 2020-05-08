 package edu.berkeley.cs160.theccertservice.splist;
 
 import java.util.ArrayList;
 import android.app.Activity;
 import android.app.DialogFragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 public class ListActivity extends Activity implements View.OnClickListener {
     private ListView myList;
     private static ArrayList<String> sharedLists = new ArrayList<String>();
     Spinner currentList;
     ArrayAdapter<String> arrayAdapter;
 //    private MyAdapter myAdapter;
     private Button shareButton;
     
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.list);
 		
		shareButton = (Button) findViewById(R.id.calculate);
 		
 		shareButton.setOnClickListener(this);
 		
 		
 
 		if(sharedLists.size() == 0){
 			showCreateListDialog(null);
 		}
 		currentList = (Spinner) findViewById(R.id.lists);
 		arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, sharedLists);
 		currentList.setAdapter(arrayAdapter);
 		currentList.setOnItemSelectedListener(new ChooseListListener());
 //        myList = (ListView) findViewById(R.id.MyList);
 //        myList.setItemsCanFocus(true);
 //        myAdapter = new MyAdapter();
 //        myList.setAdapter(myAdapter);
 
 	}
 
 	public void showCreateListDialog(View v) {
 	    DialogFragment newFragment = new CreateListDialog();
 	    newFragment.show(getFragmentManager(), "createList");
 	}
 	
 	public void onFinishCreateList(String listName){
 		sharedLists.add(listName);
 	}
 	
 	public class CreateListDialog extends DialogFragment {
 		private EditText mEditText;
 
 		public CreateListDialog() {
 			// Empty constructor required for DialogFragment
 		}
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState) {
 			View view = inflater.inflate(R.layout.create_list, container);
 			mEditText = (EditText) view.findViewById(R.id.list_name);
 			Button create = (Button) view.findViewById(R.id.create_button);
 			Button cancel = (Button) view.findViewById(R.id.cancel_button);
 			create.setOnClickListener(new View.OnClickListener() {
 	            public void onClick(View v) {
 	            	ListActivity parentAct = (ListActivity) getActivity();
 	                parentAct.onFinishCreateList(mEditText.getText().toString());                
 	                done();
 	            }
 	        });
 			cancel.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					done();
 				}
 			});
 			getDialog().setTitle("Create List");
 
 			return view;
 		}
 		
 		public void done() {
 			this.dismiss();
 		}
 	}
 
 	public void onClick(View view) {
 	    switch (view.getId()) {
 	    case R.id.share_with_others:
 	    	Intent myIntent = new Intent(view.getContext(), ShareListActivity.class);
 	        startActivity(myIntent);
 			break;
 	    }
 
 	}
 	
 	public class ChooseListListener extends Activity implements OnItemSelectedListener {
 		
 		@Override
 		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
 			String chosenList = ((Spinner) parent).getSelectedItem().toString();
 		}
 		
 		@Override
 		public void onNothingSelected(AdapterView<?> parent) {
 		}
 	}
 	
 	public static ArrayList<String> getLists(){
 		return sharedLists;
 	}
 }
