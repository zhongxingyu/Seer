 package billsplit.ui;
 import java.util.Collection;
 
 import billsplit.engine.Event;
 import billsplit.engine.Item;
 import billsplit.engine.Participant;
 import billsplit.engine.Transaction;
 
 import com.billsplit.R;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 import android.support.v4.app.NavUtils;
 import android.text.InputType;
 import android.annotation.TargetApi;
 import android.content.DialogInterface;
 import android.os.Build;
 
 public class ItemActivity extends Activity {
 
 	RelativeLayout layout;
 	boolean manualInputEntered=false;
 	double unassignedAmount;
 	private Collection<Participant> participants;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_item);
 
 		layout = (RelativeLayout) findViewById(R.id.item_participantsContainer);
 		
        // Toast.makeText(this, Item.currentItem.getName(), Toast.LENGTH_LONG).show();
		participants = Event.currentEvent.getParticipants();
		
         EditText name = (EditText)findViewById(R.id.item_txtItemName);
         EditText cost = (EditText)findViewById(R.id.item_txtItemCost);
         EditText unassigned = (EditText)findViewById(R.id.item_txtUnassigned);
         
         name.setText(Item.currentItem.getName());
         cost.setText("$"+String.valueOf(Item.currentItem.getCost()));
         unassigned.setText("$"+String.valueOf(Item.currentItem.getCost()));
         unassignedAmount = Item.currentItem.getCost();
         
         Button splitButton = (Button)findViewById(R.id.item_btnSplitEvenly);
 		splitButton.setVisibility(View.GONE);
 		
 		Button doneButton = (Button)findViewById(R.id.item_btnDone);
 		doneButton.setVisibility(View.GONE);
 		
 		Transaction.current.debtResetItem(Item.currentItem);
 		
         generateParticipants();
         // Show the Up button in the action bar.
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
 
     private void checkParticipantsChecked() {
     	boolean atLeastOneChecked = false;
     	Button splitButton = (Button)findViewById(R.id.item_btnSplitEvenly);
 		
 		for(int i=0;i<layout.getChildCount();i++){
 			ParticipantView btnPart = (ParticipantView)layout.getChildAt(i);
 			if(btnPart.isChecked()){
 				atLeastOneChecked = true;
 				break;
 			}
 		}
 		
 		if(atLeastOneChecked){
 			splitButton.setVisibility(View.VISIBLE);
 		}else{
 			splitButton.setVisibility(View.GONE);
 		}
 	}
     private void generateParticipants() {
 		layout.removeAllViews();
 		int i = 0;
 		for (Participant participant : participants) {
 
 			ParticipantView btnPart = new ParticipantView(getApplicationContext());
 			btnPart.isCheckable = true;
 			//btnPart.setTextOn(Event.currentEvent.getParticipants().get(i).getName());
 			//btnPart.setTextOff(Event.currentEvent.getParticipants().get(i).getName());
 			btnPart.setName(participant.getName());
 			btnPart.setTag(participant);
 			btnPart.setOnLongClickListener(new View.OnLongClickListener() {
 				
 				@Override
 				public boolean onLongClick(View v) {
 					
 					final ParticipantView part = (ParticipantView)v;
 					//part.toogleCheck();
 					final EditText input = new EditText(ItemActivity.this);
 					
 					input.setHint("Type amount: 0.0");
 					input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
 
 					AlertDialog.Builder alert;
 					alert = new AlertDialog.Builder(ItemActivity.this);
 					alert.setView(input);
 					alert.setTitle("Amount");
 
 					alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						
 							if(!manualInputEntered){
 								Transaction.current.debtResetItem(Item.currentItem);
 								uncheckAllParticipants();
 								checkParticipantsChecked();
 							}
 							Participant p = (Participant)part.getTag();
 							try{
 							Transaction.current.debtSet(Item.currentItem, p, Double.parseDouble(input.getText().toString()));
 							part.setAmount(Double.parseDouble(input.getText().toString()));
 							//unassignedAmount -= Double.parseDouble(input.getText().toString());
 							EditText unassigned = (EditText)findViewById(R.id.item_txtUnassigned);
 					        unassigned.setText("$"+String.valueOf(Transaction.current.debtGetItemDebtRemaining(Item.currentItem)));
 					        manualInputEntered = true;
 					        Button doneButton = (Button)findViewById(R.id.item_btnDone);
 					        if(Transaction.current.debtGetItemDebtRemaining(Item.currentItem)==0){
 					        	doneButton.setVisibility(View.VISIBLE);
 					        }
 					        else
 					        {
 					        	doneButton.setVisibility(View.GONE);
 					        }
 							}catch(Exception e){
 								Toast.makeText(ItemActivity.this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
 							}
 							//myEvent.getParticipantByName(part.getName().toString()).setName(input.getText().toString());
 							//part.setName(input.getText().toString());
 							
 					  }
 
 					
 					});
 
 					alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 					  public void onClick(DialogInterface dialog, int whichButton) {
 					    // Canceled.
 					  }
 					});
 					
 					alert.show();
 					
 					return true;
 				}
 			});
 
 			btnPart.setOnClickListener(new OnClickListener() {
 			    
 				@Override
 				public void onClick(View v) {
 					if(!manualInputEntered){
 						ParticipantView part = (ParticipantView)v;
 						part.toogleCheck();
 						checkParticipantsChecked();
 					}
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
     
     private void uncheckAllParticipants() {
     	for(int i=0;i<layout.getChildCount();i++){
     		ParticipantView btnPart = (ParticipantView)layout.getChildAt(i);
     		btnPart.setChecked(false);
     	}
 		
 	}
     public void button_split_evenly_clicked(View view){
     	
     	for(int i=0;i<layout.getChildCount();i++){
     		
     		ParticipantView btnPart = (ParticipantView)layout.getChildAt(i);
 			if(btnPart.isChecked()){
 				Participant p = (Participant)btnPart.getTag();
 				Transaction.current.debtAddParticipant(Item.currentItem, p);
 			}
     	}
     	
     	for(int i=0;i<layout.getChildCount();i++){
     		
     		ParticipantView btnPart = (ParticipantView)layout.getChildAt(i);
 			if(btnPart.isChecked()){
 				Participant p = (Participant)btnPart.getTag();
 				btnPart.setAmount(Transaction.current.debtGetItemAmountParticipant(p, Item.currentItem));
 				/*btnPart.setText(p.getName()+" $"+String.valueOf(Transaction.current.debtGetItemAmountParticipant(p, Item.currentItem)));
 				btnPart.setTextOn(p.getName()+" $"+String.valueOf(Transaction.current.debtGetItemAmountParticipant(p, Item.currentItem)));
 				btnPart.setTextOff(p.getName()+" $"+String.valueOf(Transaction.current.debtGetItemAmountParticipant(p, Item.currentItem)));
 				*/
 				btnPart.setChecked(false);
 			}
     	}
     	
     	finish();
     }
     
     public void button_done_clicked(View view)
     {
     	finish();
     }
     
 
 }
