 package docketplace.stocktakr.activities.PurchaseOrders;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.MenuItem;
 
 import docketplace.stocktakr.R;
 import docketplace.stocktakr.components.SubmissionListener;
 import docketplace.stocktakr.data.Database;
 import docketplace.stocktakr.webservice.SubmitPurchaseOrder;
 import docketplace.stocktakr.webservice.TransferHandler;
 
 public class PurchaseOrderSubmit extends SherlockActivity implements OnClickListener, SubmissionListener {
 	
 	private EditText name;
 	private Button   submit;
 	private TextView itemCounter;
 	
 	private SubmitPurchaseOrder submitOrder;
 	private TransferHandler    handler;
 	
 	
 	
 	@Override
    public void onCreate(Bundle savedInstanceState) 
 	{
 		setTheme(R.style.Theme_Sherlock_Light);
 		super.onCreate(savedInstanceState);
        
    		setContentView(R.layout.submit_itemlist);
    		name   = (EditText)findViewById(R.id.person_name);
    		submit = (Button)findViewById(R.id.submit_button);
    
    		itemCounter = (TextView)findViewById(R.id.product_counter);
         
    		updateStockCount();
    
    		submit.setOnClickListener(this);   
    
    		handler = new TransferHandler(this, "Uploading Purchase Orders", "Order Uploaded", "Upload Error");
    
    		getSupportActionBar().setHomeButtonEnabled(true);
    		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
 	
 	
 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, PurchaseOrderHome.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
 
            default:
                return super.onOptionsItemSelected(item);
        }
    }
   
 	public void onClick(View v) 
 	{
 		String personName;
 		
 		if (v == submit) 
 		{
 			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 			imm.hideSoftInputFromWindow(name.getWindowToken(), 0);
 			
 			personName = name.getText().toString().trim();
 			
 			if (personName.length() == 0) 
 			{
 				Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
 			}
 			else
 			{
 				Log.d("SUBMIT", "starting");				
 
 				submitOrder = new SubmitPurchaseOrder(handler, name.getText().toString(), getBaseContext(), this);
 				
 				submitOrder.start();
 				
 				Log.d("SUBMIT", "started");				
 			}
 		} 
 	}
 	
 	public void updateStockCount() 
 	{		 
 		itemCounter.setText("Order Items: " + Database.getPurchaseOrderItemCount());
 	}
 	
 	public void submitComplete()
 	{
 		this.finish();	
 	}
 }
 
