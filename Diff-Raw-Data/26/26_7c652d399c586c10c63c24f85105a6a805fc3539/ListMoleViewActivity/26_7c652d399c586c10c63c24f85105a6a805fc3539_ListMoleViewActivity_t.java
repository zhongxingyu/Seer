 package cs310w10.MoleFinder.View;
 
 import cs310w10.MoleFinder.Controller.ListMoleController;
 import cs310w10.MoleFinder.Model.ListMole;
//import cs310w10.MoleFinder.Model.MoleAdapter;
 import cs310w10.MoleFinder.View.MoleViewActivity;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ImageButton;
import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 import android.widget.AdapterView;
 
 
 public class ListMoleViewActivity extends Activity implements
 		fView<ListMole>, OnItemClickListener {
 	ImageButton trashButton;
 	ImageButton addButton;
 	ListView moleListView;
 	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.list_mole);
 
 		trashButton = (ImageButton) findViewById(R.id.ListMoleViewTrashButton);
 		trashButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				pressTrashButton();
 			}
 		});
 
 		addButton = (ImageButton) findViewById(R.id.ListMoleViewAddButton);
 		addButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				pressAddButton();
 			}
 		});
 
 		ListMole moleList = MoleFinderApplication.getListMoleController().getMoles(); 
 		
 		moleListView = (ListView) findViewById(R.id.ListMoleViewMoleList);
		//moleListView.setAdapter(new ListAdapter(moleList.get(), getApplicationContext()));
 		moleListView.setOnItemClickListener(this);
 	        registerForContextMenu(moleListView);
 	}
 
 	protected void pressTrashButton() {
 		ListMoleController.deleteAllMoles();
 		Toast.makeText(getBaseContext(), "Database purged", Toast.LENGTH_SHORT).show();
 	}
 
 	protected void pressAddButton() {
 		Intent intent;
 		intent = new Intent(this, NewMoleViewActivity.class);
 		startActivityForResult(intent, 1);
 	}
 
	public void update(ListMole moles) {
             
	    //moleListView.setAdapter(new MoleAdapter(moles.get(), getApplicationContext()));
 	    
 	}
 	
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 	    super.onActivityResult(requestCode, resultCode, data);
 	    if(resultCode==RESULT_OK){
 	        ListMole moleList = MoleFinderApplication.getListMoleController().getMoles(); 
 	        update(moleList);
 	    }
 
 	}
 	
         public void onItemClick(AdapterView<?> l, View v, int position,
                 long id)
         {
            // Toast.makeText(this, String.valueOf(id), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MoleViewActivity.class);
            intent.putExtra(Intent.EXTRA_SUBJECT, id);
            startActivityForResult(intent, 3);
 
         }
 
 
     
 }
