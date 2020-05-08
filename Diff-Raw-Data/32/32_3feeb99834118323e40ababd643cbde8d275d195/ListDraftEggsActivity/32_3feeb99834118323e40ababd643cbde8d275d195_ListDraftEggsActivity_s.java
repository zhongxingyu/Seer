 package org.fourdnest.androidclient.ui;
 
 import java.util.List;
 
 import org.fourdnest.androidclient.Egg;
 import org.fourdnest.androidclient.EggManager;
 import org.fourdnest.androidclient.FourDNestApplication;
 import org.fourdnest.androidclient.R;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 
 public class ListDraftEggsActivity extends NestSpecificActivity {
 
 	private static final int DIALOG_CONFIRM_DELETE = 0;
 	private EggManager draftManager;
 	ListView draftListView;
 	Button sendAllButton;
 	private int eggBeingDeletedId;
 	private EggAdapter adapter;
 	
 	/** Called when this Activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		this.draftManager = ((FourDNestApplication) getApplication())
 				.getDraftEggManager();
 
 		setContentView(R.layout.list_drafts_activity);
 
 		this.draftListView = (ListView) findViewById(R.id.draft_list);
 		this.sendAllButton = (Button) findViewById(R.id.send_all_button);
 		initializeDraftList(this.draftManager, this.draftListView);
 		initializeSendAllButton(this.sendAllButton);
 		
 		super.onCreate(savedInstanceState);
 	}
 	
 	@Override
 	protected void onResume() {
 		initializeDraftList(this.draftManager, this.draftListView);
 		super.onResume();
 	}
 	
 	/**
 	 * 
 	 * This method creates the dialogues that the user uses to make selections on what ever 
 	 * to use the capture device or browse existing items, and the back button dialog.
 	 * 
 	 */
 	
 	protected Dialog onCreateDialog(int id) {
 	    Dialog dialog = null;
 	    switch(id) {
 	    case DIALOG_CONFIRM_DELETE:
 	    	AlertDialog.Builder backBuilder = new AlertDialog.Builder(this);
         	backBuilder.setMessage(getString(R.string.draft_list_dialogue_delete))
         	       .setCancelable(true)
         	       .setPositiveButton(
         	    		   getString(R.string.draft_list_dialogue_confirm),
         	    		   new DialogInterface.OnClickListener() {
         	           public void onClick(DialogInterface dialog, int id) {
         	        	   ListDraftEggsActivity.this.confirmedDelete();
         	           }
         	       })
         	       .setNegativeButton(
         	    		   getString(R.string.draft_list_dialogue_cancel),
         	    		   new DialogInterface.OnClickListener() {
         	           public void onClick(DialogInterface dialog, int id) {
         	        	   dialog.cancel();
         	           }
         	       });
         	dialog = backBuilder.create();
 	    	break;
 	    
 	    default:
 	        dialog = null;
 	    }
 	    return dialog; //the requested dialogue is returned for displaying
 	}
 	
 	/**
 	 * This idiotic function exists because too much internal functionality of this class
 	 * has been delegated to EggAdapter, making them tightly coupled.
 	 * @param id Id of draft egg that is being deleted. The user must still confirm the deletion.
 	 */
 	public void askConfirmDeletion(Integer id) {
 		this.eggBeingDeletedId = id;
 		this.showDialog(DIALOG_CONFIRM_DELETE);
 	}
 	/**
 	 * Now the user has confirmed the deletion, so we can perform it.
 	 * This idiotic function exists because too much internal functionality of this class
 	 * has been delegated to EggAdapter, making them tightly coupled.
 	 */
	protected void confirmedDelete() {
 		FourDNestApplication.getApplication().getDraftEggManager()
 		.deleteEgg(this.eggBeingDeletedId);
 		this.adapter.refreshList();
 	}
 
 	private void initializeDraftList(EggManager manager, ListView draftListView) {
 		List<Egg> draftEggs = manager.listEggs();
 		if (draftEggs.isEmpty()) {
 			findViewById(R.id.no_drafts_overlay).setVisibility(View.VISIBLE);
 		} else {
 			findViewById(R.id.no_drafts_overlay).setVisibility(View.GONE);
 		}
 		this.adapter = new EggAdapter(draftListView,
 				R.layout.egg_element_draft, manager.listEggs());
 		this.adapter.setDraftActivity(this);
 		draftListView.setAdapter(adapter);
 		draftListView.setOnItemClickListener(new EggItemOnClickListener(draftListView));
 	}
 	
 	private void initializeSendAllButton(Button button) {
 		button.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				EggAdapter draftAdapter = (EggAdapter) ListDraftEggsActivity.this.draftListView.getAdapter();
 				Egg current = null;
 				while (!draftAdapter.isEmpty()) {
 					current = draftAdapter.getItem(0);
 					//TODO: Wait for sendAll in Services. Use that instead of sending each egg individually.
 //					SendQueueService.sendEgg(getApplicationContext(), current, true);
 					draftAdapter.remove(current);
 				}
 				draftAdapter.notifyDataSetChanged();
 				
 			}
 		});
 	}
 
 }
