 package ch.hsr.hsrbuddy.activity;
 
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.Menu;
 import android.widget.TextView;
 import ch.hsr.hsrbuddy.R;
 
 public class BadgeActivity extends Activity {
 	
 	final Handler badgeHandler = new Handler();
 	private ProgressDialog mDialog;
 	private DecimalFormat dFormat = new DecimalFormat("0.00");
 	private DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy - HH:mm");
 	private double latestBalance;
 	private ArrayList<ExpenseItem> lastPurchases = new ArrayList<ExpenseItem>();
 	private Date lastUpdatedBalance;
 	private double mensaTotal;
 	private double printerTotal;
 	private double total;
 	private Date lastUpdatedWholeBalance;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_badge);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_badge, menu);
 		return true;
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 		mDialog = new ProgressDialog(this);
 		mDialog.setMessage("Loading...");
         mDialog.setCancelable(false);
         mDialog.show();
 		
 		/*
 		 * Starts a seperate thread which is absolutely detached to this UI-thread.
 		 * This thread contains heavy calculation which can be done asynchronous. 
 		 */
 		new Thread(getBadgeValues, "getBadgeValuesThread").start();		
 	}
 	
 	private final Runnable getBadgeValues = new Runnable(){
 		public void run(){
 			System.out.println("The Thread getBadgeValues has been started.");
 						
 			//TODO: get real balance from server
 			try {
 				//Simulate loading time
 				Thread.sleep(1500);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			} 
 			
 			latestBalance = 17.70;
 				
 			ExpenseItem expenseItem; 
 			for (int i = 0; i < 5; i++) {
 				expenseItem = new ExpenseItem();
 				expenseItem.date = new Date();
 				expenseItem.location = "Mensa"; 
 				expenseItem.amount = 8.60;
 				lastPurchases.add(expenseItem);
 			}
 			
 			lastUpdatedBalance = new Date();
 			mensaTotal = 780.60;
 			printerTotal = 277.40;
 			total = 1027.20;
 			lastUpdatedWholeBalance = new Date ();
 			
 			System.out.println("The variables have been set.");
 			
 			/*
 			 * The badgeHandler represents the UI-Msg-Queue to the BadgeActivityInstance.
 			 * With post you can add threads to the handler which will then be executed
 			 * from the same thread in which the handler is instanced. This exact functionality
 			 * is needed to update your UI, because you can only update your UI with threads
 			 * started by this UI-instance. 
 			 */
             badgeHandler.post(updateUI);
 			System.out.println("The Thread getBadgeValues ended.");
 		}
 	};
 
     // The handler will create this thread which will eventually update the UI.
     final Runnable updateUI = new Runnable() {
         public void run() {
         	
     		TextView currentBalanceView = (TextView) findViewById(R.id.currentBalanceValue);
     		currentBalanceView.setText(dFormat.format(latestBalance) + " CHF");
 
     		TextView expenseTitel1 = (TextView) findViewById(R.id.lastExpenseRowTitel1);
     		expenseTitel1.setText(dateFormat.format(lastPurchases.get(0).date));
     		
     		TextView expenseLocationAndAmount1 = (TextView) findViewById(R.id.lastExpenseRowValue1);
     		expenseLocationAndAmount1.setText(lastPurchases.get(0).location + " " + dFormat.format(lastPurchases.get(0).amount) + " CHF");
     		
     		TextView expenseTitel2 = (TextView) findViewById(R.id.lastExpenseRowTitel2);
     		expenseTitel2.setText(dateFormat.format(lastPurchases.get(1).date));
     		
     		TextView expenseLocationAndAmount2 = (TextView) findViewById(R.id.lastExpenseRowValue2);
     		expenseLocationAndAmount2.setText(lastPurchases.get(1).location + " " + dFormat.format(lastPurchases.get(1).amount) + " CHF");
     		
     		TextView expenseTitel3 = (TextView) findViewById(R.id.lastExpenseRowTitel3);
     		expenseTitel3.setText(dateFormat.format(lastPurchases.get(2).date));
     		
     		TextView expenseLocationAndAmount3 = (TextView) findViewById(R.id.lastExpenseRowValue3);
     		expenseLocationAndAmount3.setText(lastPurchases.get(2).location + " " + dFormat.format(lastPurchases.get(2).amount) + " CHF");
     		
    		TextView lastUpdatedBalance = (TextView) findViewById(R.id.lastUpdatedBalanceLabel);
    		lastUpdatedBalance.setText("Aktualisiert: " + dateFormat.format(lastUpdatedBalance));
     		
     		TextView mensaTotalView = (TextView) findViewById(R.id.totalMensaValue);
     		mensaTotalView.setText(dFormat.format(mensaTotal) + " CHF");
     		
     		TextView printerTotalView = (TextView) findViewById(R.id.totalPrinterValue);
     		printerTotalView.setText(dFormat.format(printerTotal) + " CHF");
     		
     		TextView totalView = (TextView) findViewById(R.id.totalValue);
     		totalView.setText(dFormat.format(total) + " CHF");
     		
    		
    		//TODO: FIX ERROR
    		//TextView lastUpdatedTotalBalance = (TextView) findViewById(R.id.lastUpdatedWholeBalanceLabel);
    		//lastUpdatedTotalBalance.setText("Aktualisiert: " + dateFormat.format(lastUpdatedWholeBalance));
     		
 			mDialog.dismiss();
     		
     		System.out.println("The values has been set in the UI.");
         }
     };
     
     private class ExpenseItem{
     	private Date date;
     	private String location;
     	private Double amount;
     }
 }
 
