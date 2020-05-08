 package billsplit.ui;
 
 import java.util.Collection;
 
 import billsplit.engine.Event;
 import billsplit.engine.Item;
 import billsplit.engine.Participant;
 import billsplit.engine.Transaction;
 
 import com.billsplit.R;
 import com.billsplit.R.layout;
 import com.billsplit.R.menu;
 
 import android.os.Build;
 import android.os.Bundle;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 import android.support.v4.app.NavUtils;
 import android.text.InputType;
 
 public class PaymentActivity extends Activity {
 	RelativeLayout layout;
 	private boolean manualInputEntered=false;
 	private double debitsTotal;
 	private Collection<Participant> participants;
 	private String TAG = "PaymentActivity"; 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_payment);
 		
 		//view to hold the unassigned value
         EditText unassigned = (EditText)findViewById(R.id.item_txtUnassigned);
         
         //TODO: get the total debt owed by all participants from the transaction/event
         debitsTotal = Transaction.current.getDebitsTotal();//Transaction.current.getDebitCreditDiff();
         Log.e(TAG, ""+debitsTotal);
         unassigned.setText(Double.toString(debitsTotal));
         
         //get the participants from the transaction/event
         if(debitsTotal > 0)//we're coming in from txn, there are some items that have been assgnd.
         	participants = Transaction.current.getParticipants();
         else
         	participants = Event.currentEvent.getParticipants();
         
 		Button doneButton = (Button)findViewById(R.id.item_btnDone);
 		doneButton.setVisibility(View.GONE);
 		doneButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v){
 				Intent intent = new Intent(PaymentActivity.this, EventActivity.class);
 				startActivity(intent);
 				finish();
 			}
 		});
 
 		//view to hold the participants
 		layout = (RelativeLayout) findViewById(R.id.item_participantsContainer);
 
 		//add participants to the current Payment screen
         generateParticipants();
 
         setupActionBar();
     }
 
     /**
      * Set up the {@link android.app.ActionBar}, if the API is available.
      */
     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     private void setupActionBar() {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
         getActionBar().setDisplayHomeAsUpEnabled(true);
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.payment, menu);
         return true;
     }
     
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
     private void generateParticipants() {
 		layout.removeAllViews();
 		int i = 0;
 		for (Participant participant : participants) {
 
 			ParticipantView btnPart = new ParticipantView(getApplicationContext());
 			btnPart.isCheckable = true;
 			btnPart.setName(participant.getName());
			btnPart.setAmount(Transaction.current.debtGetTotalAmountParticipant(participant));
 			btnPart.setTag(participant);
 			btnPart.setOnClickListener(new View.OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					
 					final ParticipantView part = (ParticipantView)v;
 					final EditText input = new EditText(PaymentActivity.this);
 					Participant selectedParticipant = (Participant)part.getTag();
 					
 					input.setHint("Enter an amount to pay..");
 					input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
 
 					AlertDialog.Builder alert;
 					alert = new AlertDialog.Builder(PaymentActivity.this);
 					alert.setView(input);
 					alert.setTitle(selectedParticipant.getName());
 
 					alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 							Participant p = (Participant)part.getTag();
 							Button doneButton = (Button)findViewById(R.id.item_btnDone);
 							//ParticipantView btnUpdate = new ParticipantView(getApplicationContext());
 							try{
 								double amountEntered = Double.parseDouble(input.getText().toString()); 
 								
 								//TODO: add the payment to the transaction
 								double currentCredit = Transaction.current.getCredit(p);
 								Transaction.current.setCredit(p, amountEntered+currentCredit);
 								//btnUpdate.setAmount(Transaction.current.debtGetTotalAmountParticipant(p));
								
 								EditText temp = (EditText)findViewById(R.id.item_txtUnassigned);
 								    
 								
 								//TODO: get the resulting balance
 								double debitCreditDiff = Transaction.current.getDebitCreditDiff();
 								
 								//find unassigned view
 								EditText unassigned = (EditText)findViewById(R.id.item_txtUnassigned);
 								//update it with debitCreditDiff
 								unassigned.setText(""+debitCreditDiff);
 
 								//TODO: check if we are done with the payments, i.e. everything is 0
 								boolean isPaymentComplete = Transaction.current.isPaymentComplete();
 								
 						        if(isPaymentComplete){
 						        	doneButton.setVisibility(View.VISIBLE);
 						        }
 						        else{
 						        	doneButton.setVisibility(View.GONE);
 						        }
 						        generateParticipants();
 							}catch(Exception e){
 								Log.e(TAG, e.getMessage());
 								Toast.makeText(PaymentActivity.this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
 							}							
 					  }					
 					});
 
 					alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 					  public void onClick(DialogInterface dialog, int whichButton) {
 					    // Canceled.
 					  }
 					});
 					
 					alert.show();
 				}
 			});
 
 			// setting image resource
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
     
     private void uncheckAllParticipants() {
     	for(int i=0;i<layout.getChildCount();i++){
     		ParticipantView btnPart = (ParticipantView)layout.getChildAt(i);
     		btnPart.setChecked(false);
     	}
 		
 	}
 }
