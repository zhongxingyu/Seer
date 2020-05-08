 package com.ssttevee.cloudapp;
 
 import java.math.BigInteger;
 import java.security.SecureRandom;
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.graphics.drawable.ColorDrawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.MenuItem;
 import com.example.android.trivialdrivesample.util.IabHelper;
 import com.example.android.trivialdrivesample.util.IabResult;
 import com.example.android.trivialdrivesample.util.Inventory;
 import com.example.android.trivialdrivesample.util.Purchase;
 import com.example.android.trivialdrivesample.util.SkuDetails;
 
 public class DonateActivity extends SherlockActivity {
 
 	private CAApplication mApp;
 	private ProgressDialog mProgDiag;
     private IabHelper mHelper;
     private SecureRandom mRandom = new SecureRandom();
     
     private ArrayList<String> skuList;
     private ArrayAdapter<String> adapter;
 
     private static final int RC_REQUEST = 65712;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_donate);
 		getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF2F5577));
		getSupportActionBar().setTitle(getString(R.string.donate_title));
 		getSupportActionBar().setDisplayUseLogoEnabled(false);   
 		getSupportActionBar().setDisplayShowHomeEnabled(false);
 		getSupportActionBar().setDisplayShowTitleEnabled(true);
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 		
 		mApp = (CAApplication) getApplication();
 		
 		mProgDiag = new ProgressDialog(this);
 		mProgDiag.setMessage(getString(R.string.ui_loading));
 		mProgDiag.setCancelable(false);
         
         skuList = new ArrayList<String>();
         skuList.add("donate_1");
         skuList.add("donate_2");
         skuList.add("donate_3");
         skuList.add("donate_4");
         skuList.add("donate_5");
 	    
         mHelper = new IabHelper(this, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwcT6gdLLSxbA5C22do0s24/Iv4JuVCKlrsxKxxN+7gfPetDjjASKRAfbtEx22utlka9RMjZA0u+0yFWbCGdhqSwz1XjRJmtBCPzwajN477SOtWzYO/s9yWNIPBwR8ymCfxubEVNR+NXRapQb2wUygIdJ4QgSeX1tkeq3ewxFe3EO5+3HwczUsv9GWa2y0d57E7C8Ozdiio2KmOHtcEv7JmXCFXpVsCX9ZyxqklhxZZy8X9TAkEbgfe4P/46+e/8kI2ABstcf61NyaOamnnNDgLB4t7ob9VRCV9SZfyfHtd7CiTxfBKmcGZbl4KeDw1RCM9m1EXHiQO5kezrrl7IUQwIDAQAB");
 
         mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
             public void onIabSetupFinished(IabResult result) {
 
                 if (!result.isSuccess()) {
                     System.out.println("Problem setting up in-app billing: " + result);
                     return;
                 }
 
                 mHelper.queryInventoryAsync(true, skuList, mGotInventoryListener);
             }
         });
 
         adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
 
 		((ListView) findViewById(android.R.id.list)).setAdapter(adapter);
 		((ListView) findViewById(android.R.id.list)).setOnItemClickListener(new OnItemClickListener() {
 				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 			        setWaitScreen(true);
 
 			        mApp.payload = generatePayload();
 			        mHelper.launchPurchaseFlow(DonateActivity.this, skuList.get(position), RC_REQUEST, mPurchaseFinishedListener, mApp.payload);
 				}
 			});
 	}
 
     IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
         public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
             if (result.isFailure()) {
                 System.out.println("Failed to query inventory: " + result);
                 return;
             }
             
             for (String sku : skuList) {
             	if(inventory.hasDetails(sku)) {
 					SkuDetails sd = inventory.getSkuDetails(sku);
 	            	adapter.add(getString(R.string.donate_ui_donate) + " " + sd.getPrice());
             	}
 			}
 
             for (String sku : skuList) {
             	if(inventory.hasPurchase(sku)) {
 	                Purchase p = inventory.getPurchase(sku);
 	                if (verifyDeveloperPayload(p)) {
 	                    mHelper.consumeAsync(inventory.getPurchase(sku), mConsumeFinishedListener);
 	                    return;
 	                }
             	}
 			}
 
             adapter.notifyDataSetChanged();
             setWaitScreen(false);
         }
     };
 
     IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
         public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
             if (result.isFailure()) {
                 complain("Error purchasing: " + result);
                 setWaitScreen(false);
                 return;
             }
             if (!verifyDeveloperPayload(purchase)) {
                 complain("Error purchasing. Authenticity verification failed.");
                 setWaitScreen(false);
                 return;
             }
 
             // Purchase successful.
 
             for (String sku : skuList) {
                 if (purchase.getSku().equals(sku)) {
                     // Donated... Consuming donation...
                     mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                 }
 			}
         }
     };
 
     IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
         public void onConsumeFinished(Purchase purchase, IabResult result) {
 
             if (result.isSuccess()) {
             	
             }
             else {
                 System.out.println("Error while consuming: " + result);
             }
             updateUi();
             setWaitScreen(false);
         }
     };
 	
     @Override
     public void onDestroy() {
         if (mHelper != null) mHelper.dispose();
         mHelper = null;
         
         super.onDestroy();
     }
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			finish();
 			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 	    if (keyCode == KeyEvent.KEYCODE_BACK) {
 	    	finish();
 			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
 	        return true;
 	    }
 	    return super.onKeyDown(keyCode, event);
 	}
     
     public boolean verifyDeveloperPayload(Purchase p) {
         return mApp.payload.equals(p.getDeveloperPayload());
     }
     
     public void setWaitScreen(boolean set) {
         if(set) mProgDiag.show();
         else mProgDiag.hide();
     }
 
     public void complain(String message) {
         Log.e(this.getClass().getName(), "**** Donation Error: " + message);
         alert("Error: " + message);
     }
 
     public void alert(String message) {
         AlertDialog.Builder bld = new AlertDialog.Builder(this);
         bld.setMessage(message);
         bld.setNeutralButton("OK", null);
         Log.d(this.getClass().getName(), "Showing alert dialog: " + message);
         bld.create().show();
     }
     
     public void updateUi() {
         new AlertDialog.Builder(this)
         	.setMessage(getString(R.string.donate_thanks))
         	.setNeutralButton("OK", new OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					finish();
 					overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
 				}
 			})
         	.create().show();
     }
     
     public String generatePayload() {
       return new BigInteger(130, mRandom).toString(32);
     }
 
 }
