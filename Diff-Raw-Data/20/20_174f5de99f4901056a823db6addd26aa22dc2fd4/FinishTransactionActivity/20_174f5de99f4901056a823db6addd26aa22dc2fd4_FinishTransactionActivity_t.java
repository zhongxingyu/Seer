 package me.rohitmishra.groupbanker;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.lang.Math;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RelativeLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class FinishTransactionActivity extends Activity implements View.OnClickListener { 
 	private GroupBankerApplication mApplication ;
 	private static String TAG = "FinishTransactionActivity" ;
 	
 	private TextView mDescription ;
 	private TextView mAmount ;
 	private String []selectedIds ;
 	private TextView[] names ;
 	private EditText[] paid ;
 	private EditText userPaid ;
 	private TransactionDbAdapter mTransactionDbHelper;
 	private DetailsDbAdapter detailsHelper;
 	private overviewDbAdapter overviewHelper;
 	private String description;
 	private String amount;
 	private float amount1;
 	private Bundle overviewBundle = new Bundle();
 	
 	
 	private int mID = 1234567 ;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState)	{
 		super.onCreate(savedInstanceState);
 				
 		Log.v("TAG", "in onCreate of FinishTransactionActivity");
 		
 		mApplication = (GroupBankerApplication) getApplication();
 		
 		Intent intent = getIntent();
 		Bundle bundle = intent.getExtras();
 		
		mTransactionDbHelper = new TransactionDbAdapter(this);
		mTransactionDbHelper.open();
 		
 		detailsHelper = new DetailsDbAdapter(this);
 		detailsHelper.open();
 		
 		overviewHelper = new overviewDbAdapter(this);
 		overviewHelper.open();
 		
 		description = bundle.getString("description");
 		overviewBundle.putString("description", description);
 		
 		amount = bundle.getString("amount");
 		amount1 = Float.valueOf(amount);
 		overviewBundle.putFloat("amount", amount1);
 		
 		selectedIds = bundle.getStringArray("selectedIds");
 		
 		ScrollView scrollView = new ScrollView(this);
 		RelativeLayout relativeLayout = new RelativeLayout(this) ;
 		scrollView.addView(relativeLayout);
 		
 		
 		mDescription = new TextView(this);
 		
 		mAmount = new TextView(this);
 		
 		mDescription.setText(description);
 		mDescription.setId(mID);	// Set a random id for alignment 
 		
 		mAmount.setText("Amount = Rs. " + amount);
 		mAmount.setId(getId());
 		
 		final RelativeLayout.LayoutParams paramsDescription = 
 			new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 		paramsDescription.addRule(RelativeLayout.ALIGN_LEFT);
 		
 		
 		final RelativeLayout.LayoutParams paramsAmount = 
 			new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 		paramsAmount.addRule(RelativeLayout.ALIGN_LEFT);
 		paramsAmount.addRule(RelativeLayout.BELOW, mDescription.getId());
 		
 		
 		relativeLayout.addView(mDescription, paramsDescription);
 		relativeLayout.addView(mAmount, paramsAmount);
 		
 		
 		TextView formTitle = new TextView(this) ;
 		formTitle.setId(getId());
 		formTitle.setText(R.string.finish_question);
 		
 		final RelativeLayout.LayoutParams paramsFormTitle = 
 			new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 		paramsFormTitle.addRule(RelativeLayout.BELOW, mAmount.getId());
 		
 		relativeLayout.addView(formTitle, paramsFormTitle);
 		
 		/* Set up a separate textview and edittext for the user 
 		 * We are getting username from GroupBankerApplication
 		 */
 		
 		
 		TextView userName = new TextView(this) ;
 		userName.setId(getId());
 		userName.setText(mApplication.getUserName());
 		Log.v(TAG, "username value:" + mApplication.getUserName()) ;
 		userName.setPadding(10, 30, 60, 0);
 		
 		final RelativeLayout.LayoutParams paramsUserName = 
 			new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 		paramsUserName.addRule(RelativeLayout.ALIGN_LEFT);
 		paramsUserName.addRule(RelativeLayout.BELOW, formTitle.getId());
 		relativeLayout.addView(userName, paramsUserName);
 		
 		
 		userPaid = new EditText(this);
 		userPaid.setId(getId());
 
 		//TODO Will be good if we can control the width without hardcoding
 		
 		final RelativeLayout.LayoutParams paramsUserPaid = 
 			new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 		paramsUserPaid.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
 		paramsUserPaid.addRule(RelativeLayout.RIGHT_OF, userName.getId());
 		paramsUserPaid.addRule(RelativeLayout.ALIGN_TOP, userName.getId());
 		relativeLayout.addView(userPaid, paramsUserPaid);
 		
 		// Now lets set the textViews and edittexts for all who have paid 
 		
 		names = new TextView[selectedIds.length] ;
 		paid = new EditText[selectedIds.length] ;
 		TextView rowTextView;
 		EditText rowEditText ;
 		int i ;
 	
 		for (i = 0; i <  selectedIds.length; i++)	{ 
 			rowTextView = new TextView(this) ;
 			rowTextView.setId(getId());
 			
 			rowEditText = new EditText(this) ;
 			rowEditText.setId(getId());
 			
 			String name = mApplication.getFriendsDbAdapter().fetchFriendName(selectedIds[i]);
 			rowTextView.setText(name);
 			
 			//rowTextView.setMinimumWidth(50);
 			rowTextView.setPadding(10, 30, 60, 0);
 			
 		//	rowEditText.setMinWidth(200);
 			
 			final RelativeLayout.LayoutParams paramsTextView = 
 				new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 			
 			// The first TextView will be below userName TextView. Rest below names[i-1]
 			if ( i == 0)	{
 				paramsTextView.addRule(RelativeLayout.BELOW, userName.getId());
 			}
 			else {
 				paramsTextView.addRule(RelativeLayout.BELOW, names[i-1].getId());
 			}
 			paramsTextView.addRule(RelativeLayout.ALIGN_LEFT);
 			
 			
 			final RelativeLayout.LayoutParams paramsEditText = 
 				new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 			paramsEditText.addRule(RelativeLayout.RIGHT_OF, rowTextView.getId());
 			paramsEditText.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
 			paramsEditText.addRule(RelativeLayout.ALIGN_TOP, rowTextView.getId());
 			
 				
 		
 			relativeLayout.addView(rowTextView, paramsTextView);
 			relativeLayout.addView(rowEditText, paramsEditText);
 			
 			names[i] = rowTextView ;
 			paid[i] = rowEditText ;
 		}
 		
 		Button btn = new Button(this);
 		btn.setText(R.string.finishTransaction);
 		btn.setId(getId());
 		btn.setOnClickListener(this);
 		final RelativeLayout.LayoutParams paramsButton = 
 				new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
 		paramsButton.addRule(RelativeLayout.BELOW, names[selectedIds.length-1].getId());
 		paramsButton.addRule(RelativeLayout.BELOW, paid[selectedIds.length-1].getId());
 		
 		relativeLayout.addView(btn, paramsButton);
 		
 		this.setContentView(scrollView) ;
 	}
 	
 	public int getId()	{
 		mID = mID + 1 ;
 		return mID ;
 	}
 	
 	
 
 	@Override
 	public void onClick(View arg0) {
 		
 		int lastId,i;
 		float paid1, difference;
 		String[] userId = new String[selectedIds.length+1];
 		float[] diff = new float[selectedIds.length+1];
 		long[] finalIds;
 		Date d = new Date();
 		String formatted = new SimpleDateFormat("yyyy-MM-dd:HH-mm-ss").format(d);
 		Log.v(TAG, "values going in the transaction table are:" + amount1 + "description" + description);
 		
 		//entering details in transaction database
 		lastId =(int)mTransactionDbHelper.createTransaction(amount1, description, formatted);
 		Log.v(TAG, "The ID of the last inserted row:" + lastId);
 		
 		//entering details in Details table
 		
 		//amount equally divided among people involved
 		float toPay = amount1/(selectedIds.length+1);
 		
 		// inserting the app user details in the details table
 		
 		String paidVal1 = userPaid.getText().toString();
 		paid1 = Float.valueOf(paidVal1);
 		detailsHelper.createDetails(lastId, "0", toPay, paid1);
 		
 		difference = paid1 - toPay;
 		userId[0] = "0";
 		diff[0] = difference;
 		
 		//entering the details for rest of the people involved in transaction
 		for (i=0; i<selectedIds.length; i++)	{
 			
 			difference = 0;
 		    String paidVal = paid[i].getText().toString();
 			paid1 = Float.valueOf(paidVal);
 			difference = paid1-toPay;
 			
 			//inserting the values in corresponding arrays
 			userId[i+1] = selectedIds[i];
 			diff[i+1] = difference;
 						
 			Log.v(TAG, "values going in the details table are: transID = " + lastId + "fbid=" + selectedIds[i] + "paid = " + paid1 + "topay = " + toPay);
 			detailsHelper.createDetails(lastId,selectedIds[i], toPay, paid1);
 		}
 		
 		//a bit of housekeeping i want to know the arrays are created well
 		
 		Log.v(TAG, "the userID array: ");
 		for(i=0; i<= selectedIds.length; i++)	{
 			
 			Log.v(TAG, userId[i] + ",");
 		}
 		
 		Log.v(TAG, "the difference array: " );
 		for(i=0; i<= selectedIds.length; i++)	{
 					
 			Log.v(TAG, diff[i] + ",");
 		}
 		
 		//sorting the arrays
 		sort(userId, diff);
 		Log.v(TAG, "Sorted Arrays are");
 		
 		Log.v(TAG, "the userID array: ");
 		for(i=0; i<= selectedIds.length; i++)	{
 			
 			Log.v(TAG, userId[i] + ",");
 		}
 		
 		Log.v(TAG, "the difference array: " );
 		for(i=0; i<= selectedIds.length; i++)	{
 					
 			Log.v(TAG, diff[i] + ",");
 		}
 		
 		 //sending the sorted array to update the overview table
 		updateOverview(userId, diff);	 
 		 				
 		 Toast.makeText(getApplicationContext(), "Transaction successfully saved",
                  Toast.LENGTH_LONG).show();
 		mTransactionDbHelper.close();
 		detailsHelper.close();
 		Log.v("TAG", "starting activity overview");
 		Intent intent = new Intent(getApplicationContext(), OverviewActivity.class);
 		intent.putExtras(overviewBundle);
 				
 		//displaying the overview of the current transaction in the same tab
 		
 		TabGroupActivity parentActivity1 = (TabGroupActivity)getParent();
 		parentActivity1.startChildActivity("OverViewActivity", intent);
 		
 		
 	}
 	
 	//selection sort function
 	
 	public void sort(String userId[], float diff[])	{
 		
 		int l = userId.length;
 		int i,j,maxIndex;
 		float temp1;
 		String temp2;
 		
 		for(i = 0; i < l-1; i++)	{
 			
 			maxIndex = i;
 			
 			for (j=i+1; j < l; j++)	{
 				
 				if(diff[j] > diff[maxIndex])	{
 					maxIndex = j;					
 				}
 				
 				if(maxIndex != i)	{
 					temp1 = diff[i];
 					diff[i] = diff[maxIndex];
 					diff[maxIndex] = temp1;
 					
 					temp2 = userId[i];
 					userId[i]= userId[maxIndex];
 					userId[maxIndex] = temp2;
 				}
 			}
 		}
 	}
 	
 	//function definition for updateOverview
 	public void updateOverview(String userId[], float diff[])	{
 		
 		int l = selectedIds.length; 	//last index of the arrays
 		int f = 0, i = 0;    					//first index of the array
 		long[] overviewIds = new long[selectedIds.length];
 		String user1, user2;
 		float amount;
 		
 		//loop the diff array till first index <= last index
 		
 		while(f < l)	{
 			
 			user1 = userId[f];
 			user2 = userId[l];
 			
 			// 3 conditions to be taken care of while comparing the difference values
 			if(diff[f] > java.lang.Math.abs(diff[l]))	{
 				
 				diff[f] = diff[f] - java.lang.Math.abs(diff[l]);
 				amount = java.lang.Math.abs(diff[l]);
 				diff[l] = 0;
 				l--;
 				if(diff[f] == 0)
 					f++;
 			}
 			
 			else if(diff[f] < java.lang.Math.abs(diff[l]))	{
 				
 				diff[l] = java.lang.Math.abs(diff[l]) - diff[f];
 				amount = diff[f];
 				diff[f] = 0;
 				f++;
 				if(diff[l] == 0)
 					l--;
 			}
 			
 			else	{
 				
 				amount = diff[f];
 				l--;
 				f++;
 			}
 			
 			int id1 = Integer.valueOf(user1);
 			int id2 = Integer.valueOf(user2);
 			String temp;
 			
 			if(id1 > id2)	{
 				temp = user1;
 				user1 = user2;
 				user2 = temp;
 				amount = -(amount);
 			}
 			
 			Log.v(TAG, "Value going in overview update function user1:" + user1 + "user2:" + user2 + "amount:" + amount);
 		//updating the overview table
 			overviewIds[i] = overviewHelper.updateAmount(user1, user2, amount);
 			Log.v(TAG, "id returned from update:" + overviewIds[i]);
 			i++;
 		}
 		
 		overviewBundle.putLongArray("overviewIds", overviewIds);
 		overviewBundle.putStringArray("selectedIds", selectedIds);
 		overviewBundle.putInt("idLength", i);
 		
 	}
 	
 	@Override
 	public void onStart()	{
 		super.onStart();
 		Log.v("TAG", "In onStart of FinishTransactionActivity");
 	}
 	
	
 
 }
