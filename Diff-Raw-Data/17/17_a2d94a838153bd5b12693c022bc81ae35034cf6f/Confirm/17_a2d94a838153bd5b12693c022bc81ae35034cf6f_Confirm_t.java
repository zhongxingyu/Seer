 package net.m_kawato.tabletpos;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.EditorInfo;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 public class Confirm extends Activity implements OnClickListener, OnEditorActionListener {
     private static final String TAG = "Confirm";
     private Globals globals;
     private OrderListAdapter orderListAdapter;
     private List<OrderItem> orderItemList;
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_confirm);
  
         this.globals = (Globals) this.getApplication();
         OrderInputHelper orderInputHelper = new OrderInputHelper(this, globals);
 
         // Set transaction data in globals
         globals.transaction.clearOrderItems();
         for (Product p: globals.products) {
             if (p.orderItem != null) {
                 globals.transaction.addOrderItem(p.orderItem);
             }
         }
 
         // Build ListView of order items
         ListView orderListView = (ListView) findViewById(R.id.list);
         LayoutInflater inflater = this.getLayoutInflater();
         View header = inflater.inflate(R.layout.confirm_header, null);
         View footer = inflater.inflate(R.layout.confirm_footer, null);
         orderListView.addHeaderView(header);
         orderListView.addFooterView(footer);
 
         this.orderItemList = new ArrayList<OrderItem>(); 
         for(OrderItem orderItem: globals.transaction.orderItems) {
             this.orderItemList.add(orderItem);
         }
 
         this.orderListAdapter = new OrderListAdapter(this, this.orderItemList, this, this);
         orderListView.setAdapter(this.orderListAdapter);        
 
         // Loading sheet number
         TextView loadingSheetNumberView = (TextView) findViewById(R.id.loading_sheet_number);
         loadingSheetNumberView.setText(globals.loadingSheetNumber);
         
         // Spinner for route selection
         orderInputHelper.buildRouteSelector();
 
         // Spinner for place selection
         orderInputHelper.buildPlaceSelector();
 
         // total amount, credit amount and cash amount
         TextView totalAmountView = (TextView) findViewById(R.id.total_amount);
         totalAmountView.setText(globals.transaction.getFormattedTotalAmount());
 
         EditText creditAmountView = (EditText) findViewById(R.id.credit_amount);
         creditAmountView.setText(globals.transaction.getCreditAmount().toString());
         creditAmountView.setOnEditorActionListener(this);
         Button btnUpdate = (Button) findViewById(R.id.btn_update);
         btnUpdate.setOnClickListener(this);
 
         TextView cashAmountView = (TextView) findViewById(R.id.cash_amount);
         cashAmountView.setText(globals.transaction.getFormattedCashAmount());        
         
         // "Order" button
         Button btnOrder = (Button) findViewById(R.id.btn_order);
         btnOrder.setOnClickListener(this);
 
         // "Receipt" button
         Button btnReceipt = (Button) findViewById(R.id.btn_receipt);
         btnReceipt.setOnClickListener(this);        
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.confirm, menu);
         return true;
     }
 
     // Event handler for TextEdit
     @Override  
     public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
         Log.d(TAG, "onEditorAction: actionId=" + actionId);
         if (v.getId() == R.id.credit_amount && actionId == EditorInfo.IME_ACTION_GO) {  
             updateCreditAmount((EditText) v);
         } else if (v.getId() == R.id.quantity && actionId == EditorInfo.IME_ACTION_GO) {
             int position = (Integer) v.getTag();
             updateQuantity((EditText) v, position);
         }
         return true;  
     }  
 
     // Event handler for buttons
     @Override
     public void onClick(View v) {
         Log.d(TAG, "onClick");
         Intent i;
         switch (v.getId()) {
         case R.id.btn_update_quantity:
             LinearLayout confirmItemView = (LinearLayout) v.getParent();
             EditText quantityView = (EditText) confirmItemView.findViewById(R.id.quantity);
             int position = (Integer) quantityView.getTag();
             Log.d(TAG, "onclick-btn_update_quantity: position = " + position);
             updateQuantity(quantityView, position);
             break;
         case R.id.btn_update:
             EditText creditAmountView = (EditText) findViewById(R.id.credit_amount);
             updateCreditAmount(creditAmountView);
             break;
         case R.id.btn_order:
             i = new Intent(this, Order.class);
             startActivity(i);
             break;
         case R.id.btn_receipt:
            globals.transaction.removeZeroQuantity();
             i = new Intent(this, Receipt.class);
             startActivity(i);
             break;
         }        
     } 
 
     // Update TextView for credit amount
     private void updateCreditAmount(EditText creditAmountView) {
         String creditAmountText = creditAmountView.getText().toString();
         if (creditAmountText.equals("")) {
             return;
         }
         BigDecimal creditAmount = new BigDecimal(creditAmountText);
         if (globals.transaction.getTotalAmount().compareTo(creditAmount) <= 0) {
             // TODO show toast (issue #36)
             return;
         }
         globals.transaction.creditAmount = creditAmount;
 
         // Update creditAmount and cashAmount
         creditAmountView.setText(creditAmount.toString());
         TextView cashAmountView = (TextView) findViewById(R.id.cash_amount);
         cashAmountView.setText(globals.transaction.getFormattedCashAmount());        
     }
     
     // Update quantity for order item
     private void updateQuantity(EditText quantityView, int position) {
         Log.d(TAG, String.format("updateQuantity: position = %d", position));
         String quantityText = quantityView.getText().toString();
         if (quantityText.equals("")) {
             return;
         }
         int quantity = Integer.parseInt(quantityText);
         Log.d(TAG, String.format("updateQuantity: quantity = %d", quantity));
         this.orderItemList.get(position).quantity = quantity;
         this.orderListAdapter.notifyDataSetChanged();
 
         // Update totalAmount and cashAmount
         TextView totalAmountView = (TextView) findViewById(R.id.total_amount);
         totalAmountView.setText(globals.transaction.getFormattedTotalAmount());
         TextView cashAmountView = (TextView) findViewById(R.id.cash_amount);
         cashAmountView.setText(globals.transaction.getFormattedCashAmount());        
     }
 }
