 /*******************************************************************************
  * Copyright 2012 Sorin Otescu <sorin.otescu@gmail.com>
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package com.udesign.cashlens;
 
 import java.text.DateFormat;
 
 import com.udesign.cashlens.CashLensStorage.Expense;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.text.Html;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 public final class ViewExpenseActivity extends Activity
 {
 	protected CashLensStorage mStorage = null;
 	protected Expense mExpense = null;
 	protected ZoomableImageView mImageView;
 	protected OutlineTextView mText;
 	protected Bitmap mImage = null;
 	
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.view_expense);
 
 		int expenseId = getIntent().getIntExtra("expense_id", 0);
 		if (expenseId == 0)
 		{
 			finish();
 			return;
 		}
 		
 		try
 		{
 			mStorage = CashLensStorage.instance(this);
 		} catch (Exception e)
 		{
 			e.printStackTrace();
 			Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
 			finish();
 			return;
 		}
 		
 		mExpense = mStorage.getExpense(expenseId);
 		Log.d("ViewExpense.onCreate", "viewing expense with id " + Integer.toString(expenseId) + ", resolved to " + mExpense.toString());
 		
 		mImageView = (ZoomableImageView)findViewById(android.R.id.background);
 		mText = (OutlineTextView)findViewById(android.R.id.text1);
 		
 		AppSettings settings = AppSettings.instance(this);
 		mText.setTextColor(settings.getExpenseTextColor());
 		mText.setOutlineColor(settings.getExpenseOutlineColor());
 		
 		if (mExpense.imagePath != null)
 		{
 			// Reuse the already decoded image (onCreate is called again after a configuration change) 
 			Object data = getLastNonConfigurationInstance(); 
 
 			if (data != null)
 			{
 				mImage = (Bitmap)data;
 				Log.d("ExpenseImage", "reusing saved image: width " + Integer.toString(mImage.getWidth()) + 
 						", height " + Integer.toString(mImage.getHeight()));
 			}
 			else
 				mImage = CashLensUtils.createCorrectlyRotatedBitmapIfNeeded(BitmapFactory.decodeFile(mExpense.imagePath),
 								mExpense.imagePath, 1.0f);
 			
 			mImageView.setImageBitmap(mImage);
 		}
 
 		setExpenseText();
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onDestroy()
 	 */
 	@Override
 	protected void onDestroy()
 	{
 		// Try to free memory by releasing objects manually
		mImageView.getDrawable().setCallback(null);
		mImageView.setImageDrawable(null);
 		
 		super.onDestroy();
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onRetainNonConfigurationInstance()
 	 * @see http://android-developers.blogspot.com/2009/02/faster-screen-orientation-change.html
 	 */
 	@Override
 	public Object onRetainNonConfigurationInstance()
 	{
 		return mImage;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.view_expense_menu, menu);
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 	    // Handle item selection
 	    switch (item.getItemId()) 
 	    {
 	    case R.id.editExpense:
 			Intent myIntent = new Intent(this,
 					EditExpenseActivity.class);
 			myIntent.putExtra("expense_id", (int)mExpense.id);
 			startActivityForResult(myIntent, 0);
 	        return true;
 	        
 	    case R.id.delExpense:
 	    	delExpenseWithConfirm();
 	        return true;
 	        
 	    default:
 	        return super.onOptionsItemSelected(item);
 	    }
 	}
 	
 	protected void delExpenseWithConfirm()
 	{
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 		
 		alert.setTitle(getString(R.string.delete_expense));
 		alert.setMessage(getString(R.string.are_you_sure));
 
 		alert.setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				onDelExpenseOK();
 			}
 		});
 
 		alert.setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				// do nothing
 			}
 		});
 
 		alert.show();
 	}
 
 	private void onDelExpenseOK()
 	{
 		try
 		{
 			mStorage.deleteExpense(mExpense);
 			Toast.makeText(this, R.string.expense_deleted, Toast.LENGTH_SHORT).show();
 			
 			finish();
 		} 
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	private void setExpenseText()
 	{
 		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
 		
 		String html = "<b>" + df.format(mExpense.date) + "</b>" +  "<br />";
 		if (mExpense.description != null)
 			html += mExpense.description + "<br />";
 		
         html += mExpense.amountToString() + " " + mExpense.currencyCode() + ", " 
         	+ mExpense.accountName();
 
         // formatted text
 		mText.setText(Html.fromHtml(html));
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data)
 	{
 		super.onActivityResult(requestCode, resultCode, data);
 		
 		setExpenseText();	// update expense text
 	}
 }
