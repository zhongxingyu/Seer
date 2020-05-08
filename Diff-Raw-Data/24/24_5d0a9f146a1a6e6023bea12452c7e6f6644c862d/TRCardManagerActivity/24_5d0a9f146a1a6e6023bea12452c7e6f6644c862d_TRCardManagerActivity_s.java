 package com.trcardmanager;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Window;
 import android.widget.ArrayAdapter;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.trcardmanager.action.MovementListAction;
 import com.trcardmanager.adapter.MovementsListViewAdapter;
 import com.trcardmanager.application.TRCardManagerApplication;
 import com.trcardmanager.dao.CardDao;
 import com.trcardmanager.dao.MovementDao;
 import com.trcardmanager.dao.UserDao;
 import com.trcardmanager.views.TRCardManagerListViewBottomLoad;
 import com.trcardmanager.views.TRCardManagerListViewBottomLoad.OnRefreshListenerBottomLoad;
 
 /**
  * 
  * @author angelcereijo
  *
  */
 public class TRCardManagerActivity extends Activity {
 
 	final private static String TAG = TRCardManagerActivity.class.getName();
 	
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         
         setContentView(R.layout.main);
         
 		UserDao user = TRCardManagerApplication.getUser();
 		//view actions
 		addCardsToView(user);		
 		TRCardManagerApplication.setActualActivity(this);
     }
     
     
     @Override
     public void onBackPressed() {
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 		alert.setTitle(R.string.exit_dialog_question_title);
 		alert.setMessage(R.string.exit_dialog_question_message);
 		alert.setPositiveButton(android.R.string.yes,
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						closeApplication();
 					}
 				});
 		alert.setNegativeButton(android.R.string.no, null);
 		alert.show();
     }
     
     private void closeApplication(){
     	this.finish();
     }
         
     
     private void addMovementsToView(List<MovementDao> movements){
     	List<MovementDao> movementsCopy = new ArrayList<MovementDao>();
     	movementsCopy.addAll(movements);
     	final TRCardManagerListViewBottomLoad linearMovements = (TRCardManagerListViewBottomLoad)findViewById(R.id.layout_listview_movements);
     	final ArrayAdapter<MovementDao> adapter = new MovementsListViewAdapter(this, R.id.layout_listview_movements, movementsCopy);
     	final TextView balanceView = (TextView)findViewById(R.id.card_balance);
     	linearMovements.setOnRefreshListener(new OnRefreshListenerBottomLoad() {
             public void onRefresh() {
             	new MovementListAction(linearMovements,adapter,balanceView).execute();
             }
         });
 		linearMovements.setAdapter(adapter);
     }
     
     
   
    
     @Override
     protected void onRestart() {
     	super.onRestart();
    	TRCardManagerApplication.setActualActivity(this);
     }
     
     
     
 //    public void clickAddCard(View v) {
 //		//show view to add cardNumber
 //    	//insertCardNumber
 //    	
 //    	 AlertDialog.Builder alert = new AlertDialog.Builder(this);  
 //
 //         alert.setTitle("Add new card");  
 //         alert.setMessage("Type de card number");  
 //
 //         // Set an EditText view to get user input   
 //         final EditText inputName = new EditText(this);  
 //         alert.setView(inputName);  
 //         alert.setCancelable(true);
 //         alert.setPositiveButton("Add Card", new DialogInterface.OnClickListener() {  
 //         public void onClick(DialogInterface dialog, int whichButton) {  
 //             String cardNumber = inputName.getText().toString();
 //             addNewCardAdded(cardNumber);
 //           }  
 //         }); 
 //
 //         alert.show();
 //	}
     
 //    private void addNewCardAdded(String cardNumber){
 //    	TRCardManagerDbHelper dbHelper = new TRCardManagerDbHelper(getApplicationContext());
 //    	CardDao cardDao = new CardDao(cardNumber);
 //    	dbHelper.addCard(TRCardManagerApplication.getUser().getRowId(), cardDao);
 //    	
 //    	LinearLayout cardsLayout = (LinearLayout)findViewById(R.id.cards_layout);
 //		cardsLayout.addView(createCardLayout(cardDao,false));
 //    }
     
     
     
     private void addCardsToView(UserDao user){
     	RelativeLayout cardsLayout = (RelativeLayout)findViewById(R.id.layout_card_information);
     	
     	CardDao actualCard = user.getActualCard();
 	    TextView cardNumber = (TextView)cardsLayout.findViewById(R.id.card_number);
 	    cardNumber.setText(actualCard.getCardNumber());
 	    TextView cardBalance = (TextView)cardsLayout.findViewById(R.id.card_balance);
     	cardBalance.setText(actualCard.getBalance());
     	
     	
 	    addMovementsToView(actualCard.getMovementsData().getMovements());
 		
     }
     
 //    public void removeListCard(final long cardId){
 //    	new AlertDialog.Builder(this)
 //        .setTitle("Delete entry")
 //        .setMessage("Are you sure you want to delete this entry?")
 //        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 //            public void onClick(DialogInterface dialog, int which) { 
 //            	TRCardManagerDbHelper dbHelper = new TRCardManagerDbHelper(getApplicationContext());
 //            	dbHelper.deleteCard(cardId);
 //            	UserDao user = TRCardManagerApplication.getUser();
 //            	updateInMemoryCardList(user, cardId);
 //            	addCardsToView(user);
 //            }
 //         })
 //        .setNegativeButton("No", new DialogInterface.OnClickListener() {
 //            public void onClick(DialogInterface dialog, int which) { 
 //               dialog.cancel();
 //            }
 //         })
 //         .show();
 //    }
 //    
 //    private void updateInMemoryCardList(UserDao user,long cardId){
 //    	List<CardDao> cards = user.getCards();
 //    	for(CardDao actualCard:cards){
 //    		if(actualCard.getId() == cardId){
 //    			cards.remove(actualCard);
 //    			break;
 //    		}
 //    	}
 //    }
 //    
 //    private LinearLayout createCardLayout(CardDao card, boolean actualCard){
 //    	
 //    	LinearLayout cardLayout = createCardLinearLayout();
 //    	
 // 		RelativeLayout cardDataLayout = createNumberCardLayout(card,actualCard);
 // 		cardLayout.addView(cardDataLayout);
 // 		
 // 		LinearLayout cardDataBalanceLayout = createCardDataLinearLayout();
 // 		cardDataBalanceLayout.addView(createCardDataTextView(
 // 				getApplicationContext().getString(R.string.cards_list_card_balance),true,false)); 		
 // 			TextView textActualCardBalance = createCardDataTextView(
 // 					card.getBalance()+"€", false,true);
 // 			cardDataBalanceLayout.addView(textActualCardBalance);
 // 		
 // 		cardLayout.addView(cardDataBalanceLayout);
 // 		
 //    	
 //    	return cardLayout;
 //    }
     
     
    
 //    private void changeActiveCard(long cardId){
 //    	UserDao user = TRCardManagerApplication.getUser();
 //    	CardDao newActiveCard = findCardById(user.getCards(),cardId);
 //    	//http active card
 //    	TRCardManagerHttpAction httpAction = new TRCardManagerHttpAction();
 //    	try {
 //			httpAction.activateCard(user, newActiveCard);
 //			//refresh in memory list
 //			user.getCards().remove(newActiveCard);
 //			CardDao tempCard = user.getActualCard().getCopy();
 //			user.setActualCard(newActiveCard);
 //			user.getCards().add(tempCard);
 //			
 //			httpAction.getActualCardBalanceAndMovements(user);
 //			//update balance card
 //			TRCardManagerDbHelper dbHelper = new TRCardManagerDbHelper(getApplicationContext());
 //			dbHelper.updateCardBalance(newActiveCard);
 //	    	//show
 //			addCardsToView(user);
 //		} catch (IOException e) {
 //			e.printStackTrace();
 //			showErrorDialog(R.string.activate_card_error);
 //		} catch (TRCardManagerUpdateCardException e) {
 //			e.printStackTrace();
 //			showErrorDialog(R.string.activate_card_error);
 //		} catch (TRCardManagerDataException e) {
 //			e.printStackTrace();
 //			//TODO change message
 //			showErrorDialog(R.string.activate_card_error);
 //		}
 //    	
 //    }
 //    
 //    private void showErrorDialog(int messageCode){
 //    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
 //    	builder.setMessage(messageCode)
 //    	       .setCancelable(false)
 //    	       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
 //    	           public void onClick(DialogInterface dialog, int id) {
 //    	                dialog.cancel();
 //    	           }
 //    	       });
 //    	AlertDialog alert = builder.create();
 //    	alert.show();
 //    }
     
     
     
     
 //    private CardDao findCardById(List<CardDao> cards, long id){
 //    	for(CardDao card:cards){
 //    		if(card.getId() == id){
 //    			return card;
 //    		}
 //    	}
 //    	return null;
 //    }
     
    
 }
