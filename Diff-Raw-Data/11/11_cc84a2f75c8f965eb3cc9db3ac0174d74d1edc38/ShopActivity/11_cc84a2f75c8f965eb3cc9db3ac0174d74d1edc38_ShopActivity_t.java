 /*
  * Copyright (c) 2013 Byron Sanchez (hackbytes.com)
  * www.chompix.com
  *
  * Licensed under the MIT License.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package net.globide.creepypasta_files_07;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import net.globide.creepypasta_files_07.util.IabHelper;
 import net.globide.creepypasta_files_07.util.IabResult;
 import net.globide.creepypasta_files_07.util.Inventory;
 import net.globide.creepypasta_files_07.util.Purchase;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.RelativeLayout.LayoutParams;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Outputs a list of items that can be bought via the marketplace as well as
  * corresponding buy buttons.
  */
 
 public class ShopActivity extends Activity implements OnClickListener {
     
     // Define our small db API object for database interaction.
     private NodeDatabase mDbNodeHelper = null;
     // Define a variable object that will store the variable data from the
     // database.
     private Variable[] mVariableData;
 
     // (arbitrary) request code for the purchase flow
     private static final int RC_REQUEST = 10001;
 
     // The helper object
     private IabHelper mHelper;
 
     // Boolean check for whether or not the user is currently online.
     // private boolean mIsOnline;
 
     // Boolean hash map for whether a user owns a product
     private HashMap<String, Boolean> mHas = new HashMap<String, Boolean>();
     
     // Declare hashmaps for the dynamically generated data.
     private HashMap<String, RelativeLayoutHolder> mRlHashMap = new HashMap<String, RelativeLayoutHolder>();
     private HashMap<String, TextViewHolder> mTvHashMap = new HashMap<String, TextViewHolder>();
     private HashMap<String, ImageButtonHolder> mIbHashMap = new HashMap<String, ImageButtonHolder>();
     
     // Define the necessary views.
     private LinearLayout mLlShopScrollView;
     private RelativeLayout mRlShopProgress;
     public ProgressBar pbShopQuery;
     private RelativeLayout mRlTemplate;
     private TextView mTvTemplate;
     private ImageButton mIbTemplate;
     
     // Tablet vs. phone boolean. Defaults to phone.
     public static boolean sIsTablet = false;
     public static boolean sIsSmall = false;
     public static boolean sIsNormal = false;
     public static boolean sIsLarge = false;
     public static boolean sIsExtraLarge = false;
 
     /**
      *  Holders for dynamically generated relative layouts.
      */
     private class RelativeLayoutHolder {
 
         // Tag so we can access each view when updating the UI.
         private String tag;
         private String sku;
     }
 
     /**
      *  Holders for dynamically generated textviews.
      */
     private class TextViewHolder {
 
         // Tag so we can access each view when updating the UI.
         private String tag;
         private String sku;
     }
 
     /**
      * Holders for dynamically generated image buttons.
      */
     private class ImageButtonHolder {
 
         // Tag so we can access each view when updating the UI.
         private String tag;
         private String sku;
     }
     
 	    /**
 	     * Implements onCreate().
 	     */
 	    @Override
 	    protected void onCreate(Bundle savedInstanceState) {
 	        super.onCreate(savedInstanceState);
 
 	        // Set the activity to full screen mode.
 	        requestWindowFeature(Window.FEATURE_NO_TITLE);
 	        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 	                WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 	        // Add default content.
 	        setContentView(R.layout.activity_shop);
 
 	        // Determine whether or not the current device is a tablet.
 	        ShopActivity.sIsTablet = getResources().getBoolean(R.bool.isTablet);
 	        ShopActivity.sIsSmall = getResources().getBoolean(R.bool.isSmall);
 	        ShopActivity.sIsNormal = getResources().getBoolean(R.bool.isNormal);
 	        ShopActivity.sIsLarge = getResources().getBoolean(R.bool.isLarge);
 	        ShopActivity.sIsExtraLarge = getResources().getBoolean(R.bool.isExtraLarge);
 
 	        // Attach views to their corresponding resource ids.
 	        mLlShopScrollView = (LinearLayout) findViewById(R.id.llShopScrollView);
 	        mRlShopProgress = (RelativeLayout) findViewById(R.id.rlShopProgress);
 
 	        // Create the progressbar view
	        pbShopQuery = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
 
 	        // Give the progressbar some parameters.
 	        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
 	                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 	        params.addRule(RelativeLayout.CENTER_IN_PARENT);
 
 	        pbShopQuery.setLayoutParams(params);
 	        pbShopQuery.setIndeterminate(true);
 
 	        // Add the view to the layout.
 	        mRlShopProgress.addView(pbShopQuery);
 
 	        // load state data
 	        loadData();
 
 	        // Create all buttons based on the data in the categories table in the
 	        // db.
 	        createButtons();
 
 	        // Find out whether or not the user is currently online.
 	        // mIsOnline = isOnline();
 
 	        /*
 	         * base64EncodedPublicKey should be YOUR APPLICATION'S PUBLIC KEY (that
 	         * you got from the Google Play developer console). This is not your
 	         * developer public key, it's the *app-specific* public key.
 	         * Instead of just storing the entire literal string here embedded in
 	         * the program, construct the key at runtime from pieces or use bit
 	         * manipulation (for example, XOR with some other string) to hide the
 	         * actual key. The key itself is not secret information, but we don't
 	         * want to make it easy for an attacker to replace the public key with
 	         * one of their own and then fake messages from the server.
 	         */
	        String base64EncodedPublicKey = "YOUR_DEV_KEY_HERE";
 
           // TODO: Implement the preceeding comment in some way.
 
 	        // Create the helper, passing it our context and the public key to
 	        // verify signatures with
 	        mHelper = new IabHelper(this, base64EncodedPublicKey);
 
 	        // disable debug logging (for a production application, you should
 	        // set this to false).
 	        mHelper.enableDebugLogging(false);
 
 	        // Start setup. This is asynchronous and the specified listener
 	        // will be called once setup completes.
 	        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
 	            public void onIabSetupFinished(IabResult result) {
 
 	                if (!result.isSuccess()) {
 	                    // Oh noes, there was a problem.
 	                    return;
 	                }
 
 	                // Hooray, IAB is fully set up. Now, let's get an inventory of
 	                // stuff we own.
 	                mHelper.queryInventoryAsync(mGotInventoryListener);
 	            }
 	        });
 	    }
 
 	    /**
 	     * Listener that's called when we finish querying the items we own
 	     */
 	    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
 	        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
 	            if (result.isFailure()) {
 	                return;
 	            }
 
 	            // Do we have the items.
 	            for (Variable variable : mVariableData) {
 	                mHas.put(variable.sku, inventory.hasPurchase(variable.sku));
 	            }
 
 	            // If we can't find it via the inventory, we might not have an
 	            // internet connection, so check the database.
 
 	            // Ensure that the database contains the purchase information.
 	            checkDatabase();
 
 	            updateUi();
 	            setWaitScreen(false);
 	        }
 	    };
 
 	    /**
 	     * Callback for when a purchase is finished
 	     */
 	    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
 	        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
 	            if (result.isFailure()) {
 	                setWaitScreen(false);
 	                return;
 	            }
 
 	            for (Variable variable : mVariableData) {
 	                if (purchase.getSku().equals(variable.sku)) {
 	                    // bought the second coloring book!
 	                    alert(variable.key + " feature has been added! You will no longer see ads.");
 	                    mHas.put(variable.sku, true);
 
 	                    // Ensure that the database contains the purchase
 	                    // information.
 	                    checkDatabase();
 
 	                    updateUi();
 	                    setWaitScreen(false);
 	                }
 	            }
 	        }
 	    };
 
 	    /**
 	     * Updates UI to reflect model
 	     */
 	    public void updateUi() {
 
 	        // update the shop buttons to reflect whether or not the user has bought
 	        // the item.
 	        for (String sku : mIbHashMap.keySet()) {
 
 	            // Store a reference to the current iteration's Image Holder.
 	            ImageButtonHolder ibHolder = mIbHashMap.get(sku);
 
 	            ImageButton currentImageButton = (ImageButton) mLlShopScrollView.findViewWithTag(ibHolder.tag);
 
 	            // The buy button should only work when this product has not yet
 	            // been purchased.
 	            currentImageButton.setBackgroundResource(mHas.get(sku) ? R.drawable.button_shop_disabled
 	                    : R.drawable.button_shop);
 
 	            currentImageButton.setEnabled(mHas.get(sku) ? false : true);
 	        }
 
 	        // Redraw the view.
 	        mLlShopScrollView.invalidate();
 	    }
 
 	    /**
 	     * Enables or disables the "please wait" screen.
 	     */
 	    void setWaitScreen(boolean set) {
 	        // Update the views.
 	        findViewById(R.id.svShop).setVisibility(set ? View.GONE : View.VISIBLE);
 	        findViewById(R.id.rlShopProgress).setVisibility(set ? View.VISIBLE : View.GONE);
 
 	        // Invalidate to redraw them
 	        findViewById(R.id.svShop).invalidate();
 	        findViewById(R.id.rlShopProgress).invalidate();
 	    }
 
 	    /**
 	     * Creates buttons for all coloring books in the categories table.
 	     */
 	    private void createButtons() {
 
 	        // Attach views to their corresponding resource ids.
 	        mRlTemplate = (RelativeLayout) findViewById(R.id.rlTemplate);
 	        mTvTemplate = (TextView) findViewById(R.id.tvTemplate);
 	        mIbTemplate = (ImageButton) findViewById(R.id.ibTemplate);
 
 	        // Retrieve the layout parameters for each view.
 	        LinearLayout.LayoutParams rlTemplateParams = (LinearLayout.LayoutParams) mRlTemplate
 	                .getLayoutParams();
 	        RelativeLayout.LayoutParams tvTemplateParams = (RelativeLayout.LayoutParams) mTvTemplate
 	                .getLayoutParams();
 	        RelativeLayout.LayoutParams ibTemplateParams = (RelativeLayout.LayoutParams) mIbTemplate
 	                .getLayoutParams();
 
 	        // Iterate through each variable row.
 	        for (Variable variable : mVariableData) {
 
 	            /*
 	             * RelativeLayout
 	             */
 	            // Create a new relative layout and add it to the main ScrollView.
 	            RelativeLayout newRelativeLayout = new RelativeLayout(this);
 	            newRelativeLayout.setLayoutParams(rlTemplateParams);
 
 	            // Set view styles
 	            if (ShopActivity.sIsTablet) {
 	                if (ShopActivity.sIsSmall) {
 	                    newRelativeLayout.setPadding(0, 12, 0, 12);
 	                }
 	                else if (ShopActivity.sIsNormal) {
 	                    newRelativeLayout.setPadding(0, 15, 0, 15);
 	                }
 	                else if (ShopActivity.sIsLarge) {
 	                    newRelativeLayout.setPadding(0, 18, 0, 18);
 	                }
 	                else if (ShopActivity.sIsExtraLarge) {
 	                    newRelativeLayout.setPadding(0, 20, 0, 20);
 	                }
 	            } else {
 	                newRelativeLayout.setPadding(0, 8, 0, 8);
 	            }
 
 	            // Set tag as the viewtype _ sku
 	            newRelativeLayout.setTag("RelativeLayout_" + variable.sku);
 
 	            // Store this data in the corresponding holder hash map
 	            RelativeLayoutHolder rlHolder = new RelativeLayoutHolder();
 	            rlHolder.sku = variable.sku;
 	            rlHolder.tag = "RelativeLayout_" + variable.sku;
 	            mRlHashMap.put(variable.sku, rlHolder);
 
 	            mLlShopScrollView.addView(newRelativeLayout);
 
 	            /*
 	             * TextView
 	             */
 
 	            // Create a new textview and add it to the relative layout.
 	            TextView newTextView = new TextView(this);
 	            newTextView.setLayoutParams(tvTemplateParams);
 	            newTextView.setText(variable.key + " Feature");
 	            newTextView.setTextColor(Color.WHITE);
 
 	            // Set view styles
 	            if (ShopActivity.sIsTablet) {
 	                if (ShopActivity.sIsSmall) {
 	                    newTextView.setTextSize(24);
 	                }
 	                else if (ShopActivity.sIsNormal) {
 	                    newTextView.setTextSize(30);
 	                }
 	                else if (ShopActivity.sIsLarge) {
 	                    newTextView.setTextSize(36);
 	                }
 	                else if (ShopActivity.sIsExtraLarge) {
 	                    newTextView.setTextSize(40);
 	                }
 	            } else {
 	                newTextView.setTextSize(16);
 	            }
 
 	            // Set tag as the viewtype _ sku
 	            newTextView.setTag("TextView_" + variable.sku);
 
 	            // Store this data in the corresponding holder hash map
 	            TextViewHolder tvHolder = new TextViewHolder();
 	            tvHolder.sku = variable.sku;
 	            tvHolder.tag = "TextView_" + variable.sku;
 	            mTvHashMap.put(variable.sku, tvHolder);
 
 	            newRelativeLayout.addView(newTextView);
 
 	            /*
 	             * ImageButton.
 	             */
 
 	            // Create a new imagebutton and add it to the relative layout.
 	            ImageButton newImageButton = new ImageButton(this);
 	            newImageButton.setLayoutParams(ibTemplateParams);
 	            newImageButton.setOnClickListener(this);
 
 	            // Set tag as the viewtype _ sku
 	            newImageButton.setTag("ImageButton_" + variable.sku);
 
 	            // Store this data in the corresponding holder hash map
 	            ImageButtonHolder ibHolder = new ImageButtonHolder();
 	            ibHolder.sku = variable.sku;
 	            ibHolder.tag = "ImageButton_" + variable.sku;
 	            mIbHashMap.put(variable.sku, ibHolder);
 
 	            newRelativeLayout.addView(newImageButton);
 	        }
 
 	        // Remove the template views from the layout.
 	        mLlShopScrollView.removeView(mRlTemplate);
 
 	        // Redraw the views.
 	        mLlShopScrollView.invalidate();
 	    }
 
 	    /**
 	     * Checks whether or not the user currently has internet access.
 	     */
 	    //public boolean isOnline() {
 	     //   ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 
 	     //   NetworkInfo netInfo = cm.getActiveNetworkInfo();
 
 	      //  if (netInfo != null && netInfo.isConnected()) {
 	     //       return true;
 	      //  }
 
 	      //  return false;
 	    //}
 
 	    /**
 	     * Renders a message to the screen for user confirmation.
 	     */
 	    private void alert(String message) {
 	        AlertDialog.Builder bld = new AlertDialog.Builder(this);
 	        bld.setMessage(message);
 	        bld.setNeutralButton("OK", null);
 	        bld.create().show();
 	    }
 
 	    /**
 	     * Checks to see if the database contains the purchase information.
 	     */
 	    private void checkDatabase() {
 	        // Check to see if the corresponding row exists in the database
 	        // and is enabled. If not, we need to enable it.
 	        for (Variable variable : mVariableData) {
 
 	            // If the user doesn't have it in the data base but has it in
 	            // the inventory, the product hasn't yet been locally registered
 	            // so register it.
 	            if (variable.value.equals("0")) {
 	                if (mHas.get(variable.sku)) {
 	                    // Database check!
 
 	                    // Create our database access object.
 	                    mDbNodeHelper = new NodeDatabase(this);
 
 	                    // Call the create method right just in case the user
 	                    // has
 	                    // never run the app before. If a database does not
 	                    // exist,
 	                    // the prepopulated one will be copied from the assets
 	                    // folder. Else, a connection is established.
 	                    mDbNodeHelper.createDatabase();
 	                    // Enable no ads in the database.
 	                    mDbNodeHelper.updateVariable(variable.id, "value", "1");
 	                    // Update the local cache for consistency
 	                    variable.value = "1";
 
 	                    // Flush the buffer.
 	                    mDbNodeHelper.flushQuery();
 
 	                    // This activity no longer needs the connection, so
 	                    // close
 	                    // it.
 	                    mDbNodeHelper.close();
 	                    
 	                    // A quick toast to confirm updated content in cases where a new purchase has just occurred or a previous purchase has been reinstated.
 	                    // TODO: Decouple both cases and display an alert dialog for each case so its more obvious.
 	                    Toast.makeText(this, "Your content has been updated.", Toast.LENGTH_LONG).show();
 	                }
 	            }
 	            // Else the product has been locally registered, so just set the
 	            // local boolean to match correspondingly.
 	            else if (variable.value.equals("1")) {
 	                mHas.put(variable.sku, true);
 	            }
 	        }
 	    }
 
 	    /**
 	     * Loads current variable status from the database.
 	     */
 	    private void loadData() {
 	        // Database check!
 
 	        // Create our database access object.
 	        mDbNodeHelper = new NodeDatabase(this);
 
 	        // Call the create method right just in case the user has never run the
 	        // app before. If a database does not exist, the prepopulated one will
 	        // be copied from the assets folder. Else, a connection is established.
 	        mDbNodeHelper.createDatabase();
 
 	        // Query the database for all purchased categories.
 
 	        // Execute the query.
 	        mVariableData = mDbNodeHelper.getVariableListData();
 	        // Flush the buffer.
 	        mDbNodeHelper.flushQuery();
 
 	        // This activity no longer needs the connection, so close it.
 	        mDbNodeHelper.close();
 	    }
 
 	    /**
 	     * Implements onActivityResult();
 	     */
 	    @Override
 	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 
 	        // Pass on the activity result to the helper for handling
 	        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
 	            // not handled, so handle it ourselves (here's where you'd
 	            // perform any handling of activity results not related to in-app
 	            // billing...
 	            super.onActivityResult(requestCode, resultCode, data);
 	        }
 	        else {
 	            // Log.d(TAG, "onActivityResult handled by IABUtil.");
 	        }
 	    }
 
 	    /**
 	     * Implements onDestroyed().
 	     */
 	    @Override
 	    public void onDestroy() {
 	        // very important:
 	        if (mHelper != null) {
 	            mHelper.dispose();
 	        }
 	        mHelper = null;
 
 	        super.onDestroy();
 	    }
 
 	    /**
 	     * Implements onClick().
 	     */
 	    @Override
 	    public void onClick(View view) {
 
 	        // Iterate through the views.
 	        // update the shop buttons to reflect whether or not the user has bought
 	        // the item.
 	        for (String sku : mIbHashMap.keySet()) {
 
 	            // Store a reference to the current iteration's Image Holder.
 	            ImageButtonHolder ibHolder = mIbHashMap.get(sku);
 
 	            // If the clicked view tag matches this iteration's view tag, launch the purchase flow for this item.
 	            if (view.getTag().equals(ibHolder.tag)) {
 	                setWaitScreen(true);
 	                mHelper.launchPurchaseFlow(this, ibHolder.sku, RC_REQUEST,
 	                        mPurchaseFinishedListener);
 	            }
 	        }
 	    }
 	}
