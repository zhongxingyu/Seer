 package billsplit.ui;
 //comment
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import billsplit.engine.Account;
 import billsplit.engine.DataCapture;
 import billsplit.engine.Event;
 import billsplit.engine.Item;
 import billsplit.engine.Participant;
 import billsplit.engine.Transaction;
 
 import com.billsplit.R;
 
 import edu.sfsu.cs.orange.ocr.CaptureActivity;
 
 //import edu.sfsu.cs.orange.ocr.CaptureActivity;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Toast;
 import android.support.v4.app.NavUtils;
 
 public class NewTransactionActivity extends Activity {
 	private static final String TAG = "TransactionActivity";
 	public static DataCapture dataCapture;
 	private boolean isOCRdone;
 	private Collection<Participant> participants;
 
 	RelativeLayout layout;
 	private ItemAdapter adapter;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_new_transaction);
 		layout = (RelativeLayout) findViewById(R.id.participantsContainer);
 		
 		
 		TextView lblName = (TextView) findViewById(R.id.new_transaction_lblTranName);
 		
 		lblName.setText(Transaction.current.getName());
 
 		participants = Event.currentEvent.getParticipants();	 
 		// Show the Up button in the action bar.
 		setupActionBar();
 		isOCRdone = false;
 		
 		Button btnPay = (Button)this.findViewById(R.id.btn_pay);
 		btnPay.setVisibility(View.GONE);
 		
 	}
 
 	//Once we come back from the OCR world, lets read some data from file and add 
 	//it to the list of items in the transaction
 	//TODO: Transaction.current is updated here 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		generateParticipants();
 	    ArrayList<Item> newItems = getItemList();
 		for(Item item : newItems){
 			Transaction.current.addItem(item);
 		}
 		
 		adapter = new ItemAdapter(this,R.layout.item_description_price_row, Transaction.current.getItems(), Transaction.current.getItemsBools());
 		 ListView items = (ListView) findViewById(R.id.items_list);
 		 OnItemClickListener itemClicked = new OnItemClickListener() {
 				public void onItemClick(AdapterView parent, View v, int position,
 						long id) {
 					Intent intent = new Intent(getApplicationContext(),
 							ItemActivity.class);
 					Item.currentItem = Transaction.current.getItems().get(position);
 					startActivity(intent);
 				}
 			};
 
 			items.setOnItemClickListener(itemClicked);
 		 
 		 items.setAdapter(adapter);
 		adapter.notifyDataSetChanged();
 		
 		checkItemsAreDone();
 	}
 	
 	private void checkItemsAreDone() {
 
 
 		Button btnPay = (Button)this.findViewById(R.id.btn_pay);
 		if(Transaction.current.debtAllItemsDone()){
 			btnPay.setVisibility(View.VISIBLE);
 		}
 		else{
 			btnPay.setVisibility(View.GONE);
 		}
 		
 		
 		
 		
 	}
 
 	private void generateParticipants() {
 		layout.removeAllViews();
 		int i = 0;
 		for (Participant participant : participants) {
 
 			ParticipantView btnPart = new ParticipantView(getApplicationContext());
 
 			btnPart.setName(participant.getName());
 			btnPart.setAmount(Transaction.current.debtGetTotalAmountParticipant(participant));
 
 			btnPart.setOnClickListener(new View.OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					ParticipantView btn = (ParticipantView)v;
 					//showParticipantDialog(Integer.parseInt((String) btn.getText()));//change to participant ID
 					
 				}
 			});
 			// setting image resource
 			// imageView.setImageResource(R.drawable.ic_camera);
 			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 					RelativeLayout.LayoutParams.WRAP_CONTENT,
 					RelativeLayout.LayoutParams.WRAP_CONTENT);
 			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
 					RelativeLayout.TRUE);
 			params.topMargin = i * 90;
 			i++;
 			layout.addView(btnPart, params);
 		}
 	}
 	/**
 	 * Set up the {@link android.app.ActionBar}.
 	 */
 	private void setupActionBar() {
 
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.transaction, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	public void btn_done_clicked(View view)
 	{
 		finish();
 	}
 	
 	public void lblTranName_clicked(View view){
 		final EditText input = new EditText(this);
 		
 		final TextView lblName = (TextView) findViewById(R.id.new_transaction_lblTranName);
 		input.setText(lblName.getText());
 
 		AlertDialog.Builder alert;
 		alert = new AlertDialog.Builder(this);
 		alert.setView(input);
 		alert.setTitle("Transaction Name");
 
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 		public void onClick(DialogInterface dialog, int whichButton) {
 		//  String value = input.getText();
 		  Transaction.current.setName(input.getText().toString());
 		  lblName.setText(input.getText().toString());
 		//  alert.
 		  }
 		});
 
 		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 		  public void onClick(DialogInterface dialog, int whichButton) {
 		    // Canceled.
 		  }
 		});
 		
 		alert.show();
 	}
 	public void ibtn_keyboard_clicked(View view)
 	{
 		Intent intent = new Intent(this, ManualInputActivity.class);
 		startActivity(intent);
 	}
 	
 	public void ibtn_camera_clicked(View view)
 	{
 		isOCRdone = true;
 		Intent intent = new Intent(this, CaptureActivity.class);
 	    startActivity(intent);
 	}
 	public ArrayList<billsplit.engine.Item> getItemList(){
 		String itemListString = load("ItemList.txt");
 		Log.e(TAG, "itemListString=" + itemListString);
 		dataCapture = new DataCapture(itemListString);
 		
 		//get an item from the dataCapture object
 		return dataCapture.getItemList();
 	}
 	private String load(String filename){
 	    try
 	    {
 	    	FileInputStream fis = openFileInput(filename);
 	        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
 	        String line = null, input="";
 	        while ((line = reader.readLine()) != null)
 	            input += line;
 	        reader.close();
 	        fis.close();
 	        //toast("File successfully loaded.");
 	        
 	        
 	        boolean worked = this.deleteFile(filename);
 	        if(worked){
 	        	// Toast.makeText(this,"File Deleted",Toast.LENGTH_LONG).show();
 	        	 this.openFileOutput(filename, Context.MODE_PRIVATE);
 	        }else
 	        {
 	        	//Toast.makeText(this,"File NOT Deleted",Toast.LENGTH_LONG).show();
 	        }
 	        return input;
 	    }
 	    catch (Exception ex)
 	    {
 	       // Toast.makeText(this,"Error loading file: " + ex.getLocalizedMessage(),Toast.LENGTH_LONG).show();
 	        return "";
 	    }
 	}
 	
 	public void btn_pay_clicked(View view){
 		//Account.getCurrentAccount().saveAccount();
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
         alert.setTitle("Payment Options");
         alert.setItems(R.array.select_dialog_items, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
 
                 String[] items = getResources().getStringArray(R.array.select_dialog_items);
                 
                 if(items[which].equals("Fairly"))
                 {
                 	Transaction.current.payFairly();
                 	finish();
                 }
                 if(items[which].equals("Evenly"))
                 {
                 	Transaction.current.payEvenly();     
                 	finish();
                 }
                 if(items[which].equals("Custom"))
                 {
                 	Intent intent = new Intent(getApplicationContext(), PaymentActivity.class);
 					startActivity(intent);
					finish();
                 }
                // Toast.makeText(NewTransactionActivity.this, items[which], Toast.LENGTH_LONG).show();
                 /*
                 new AlertDialog.Builder(this)
                         .setMessage("You selected: " + which + " , " + items[which])
                         .show();*/
             }
         });
         alert.show();
 	}
 	
 }
