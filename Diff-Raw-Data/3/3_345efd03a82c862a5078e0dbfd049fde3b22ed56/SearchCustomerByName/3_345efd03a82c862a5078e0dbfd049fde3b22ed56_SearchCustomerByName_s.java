 package com.example.myfirstapp;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AlphabetIndexer;
 import android.widget.CursorAdapter;
 import android.widget.ListView;
 import android.widget.SectionIndexer;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class SearchCustomerByName extends Activity {
 
 	private ListView mListView;
 	private Cursor mCursor;
 	private String [] from;
 	private int[] to;
 	
 	Context Ctxt;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_search_customer_by_name);
         
         Ctxt = this;
         Bundle custDetailsBundle = this.getIntent().getExtras();        
         final String custName = custDetailsBundle.getString("custName");
         
         TextView headerText = (TextView)findViewById(R.id.textView_searchCust_Name_Header);  
                
         mCursor = getCustDetailsByName(custName);
         
         if(mCursor.getCount() != 0){
         	
         	headerText.setText(" Customer names with '" + custName + "'");
         	
         	mListView = (ListView)findViewById(R.id.listView_searchCust_Name);
 	        
 	        mListView.setFastScrollEnabled(true);
 	        
 	        to = new int[]{R.id.text2,R.id.text1};        
 	
 	        mListView.setAdapter(new MyCursorAdapter(getApplicationContext(),
 	        		R.layout.cust_list_byname,
 	                mCursor,from,to));              
 	        
 	        //handle the click on each item in the list
 	        mListView.setOnItemClickListener(new OnItemClickListener() {
 				
 				public void onItemClick(AdapterView<?> parent, View view, int arg2,
 						long arg3) {
 					String custNumSelected = ((TextView)(view).findViewById(R.id.text2)).getText().toString();
					String custNum =custNumSelected.substring(custNumSelected.length()- 2); 
 					long custNumber = Long.parseLong(custNum);
 					
 					//Toast.makeText(Ctxt, "you pressed on customer:"+ custNumber, Toast.LENGTH_SHORT).show();
 					
 					Cursor custDetailsCursor = getCustDetailsById(custNumber);
 					
 					if(custDetailsCursor.getCount() != 0){
 						
 						Bundle custDetailsBundle = getBundle(custDetailsCursor);
 						
 						Intent intent = new Intent(Ctxt,SearchCustomerResult.class);				
 						/* sending the customer details to next activity 			 */
 						
 						intent.putExtras(custDetailsBundle);			
 						
 						//start the activity
 						
 						Ctxt.startActivity(intent);
 					}
 					else{
 						
 						Toast.makeText(Ctxt, R.string.alertTxt_FindCustomer_byMob_fail, Toast.LENGTH_SHORT).show();
 					}
 					
 				}
 			});
         
         }
         else{
         	
         	headerText.setText(" No customer found with name '" + custName + "'");
         	
         }
         
     }
     
     private Cursor getCustDetailsByName(String custName) {
 		
     	CustomerTable custTable = new CustomerTable(Ctxt);
     	custTable.open();
     	Cursor nCursor = custTable.fetchCustomerByName(custName);
 		custTable.close();	
 		from = new String[] {custTable.KEY_ROWID, custTable.KEY_NAME};
 		return nCursor;		
 	}
     
     /**
      * Adapter that exposes data from a Cursor to a ListView widget. The Cursor must include a column named "_id"
      * or this class will not work.
      */
    public class MyCursorAdapter extends CursorAdapter  implements SectionIndexer
     {
 
         AlphabetIndexer mAlphabetIndexer;
 
         public MyCursorAdapter(Context context, int simpleListItem1,
                 Cursor cursor, String[] strings, int[] is)
         {
             super(context, cursor);
 
             mAlphabetIndexer = new AlphabetIndexer(cursor,
                     cursor.getColumnIndex(from[1]),
                     " ABCDEFGHIJKLMNOPQRTSUVWXYZ");
             mAlphabetIndexer.setCursor(cursor);//Sets a new cursor as the data set and resets the cache of indices.
 
         }
 
         /**
          * Performs a binary search or cache lookup to find the first row that matches a given section's starting letter.
          */
        public int getPositionForSection(int sectionIndex)
         {
             return mAlphabetIndexer.getPositionForSection(sectionIndex);
         }
 
         /**
          * Returns the section index for a given position in the list by querying the item and comparing it with all items
          * in the section array.
          */
         public int getSectionForPosition(int position)
         {
             return mAlphabetIndexer.getSectionForPosition(position);
         }
 
         /**
          * Returns the section array constructed from the alphabet provided in the constructor.
          */
        public Object[] getSections()
         {
             return mAlphabetIndexer.getSections();
         }
 
         /**
          * Bind an existing view to the data pointed to by cursor
          */
         public void bindView1(View view, Context context, Cursor cursor)
         {
         	TextView txtView1 = (TextView)view.findViewById(to[1]);
 	        txtView1.setText(cursor.getString(
 	                        cursor.getColumnIndex(from[1])));
             
             TextView txtView2 = (TextView)view.findViewById(to[0]);
 	        txtView2.setText("Customer# :" + cursor.getString(
 	                cursor.getColumnIndex(from[0])));
         }
 
         /**
          * Makes a new view to hold the data pointed to by cursor.
          */
         @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent)
         {
             LayoutInflater inflater = LayoutInflater.from(context);
             View newView = inflater.inflate(
             		R.layout.cust_list_byname, parent, false);
             return newView;
         }
 
 		@Override
 		public void bindView(View view, Context context, Cursor cursor)
 	    {
 	        TextView txtView1 = (TextView)view.findViewById(to[1]);
 	        txtView1.setText(cursor.getString(
 	                        cursor.getColumnIndex(from[1])));
 	        
 	        TextView txtView2 = (TextView)view.findViewById(to[0]);
 	        txtView2.setText("Customer# :" + cursor.getString(
 	                cursor.getColumnIndex(from[0])));
 	    }
 
 
 		
       }
 
    private Bundle getBundle(Cursor custDetails) {
 		
 	CustomerTable custTable = new CustomerTable(Ctxt);
 	long custNum = custDetails.getLong(custDetails.getColumnIndex(custTable.KEY_ROWID));
 	String custName = custDetails.getString(custDetails.getColumnIndex(custTable.KEY_NAME));
    	String custMob = custDetails.getString(custDetails.getColumnIndex(custTable.KEY_MOBILE));    		
    	String custAdrs = custDetails.getString(custDetails.getColumnIndex(custTable.KEY_ADDRESS));
    	String custShirtDetails = custDetails.getString(custDetails.getColumnIndex(custTable.KEY_SHIRTDETAILS));
    	String custPantDetails = custDetails.getString(custDetails.getColumnIndex(custTable.KEY_PANTDETAILS));
    	
    	custDetails.close();
    	String details = "\tCustomer Number:\t\t"+ custNum+ 
    			"\n\tName:\t"+ custName+"\n\tMobile:\t"+custMob+"\n\tAddress:\t\t"+custAdrs;  
    	Bundle bundle = new Bundle();			
 		bundle.putString("custDetails",details );
 		bundle.putLong("custNum",custNum);
 		bundle.putString("custName",custName);
 		bundle.putString("custMob",custMob);
 		bundle.putString("custAdrs",custAdrs);
 		bundle.putString("custShirtDetails",custShirtDetails);
 		bundle.putString("custPantDetails",custPantDetails);
    	
 		return bundle;
 	}
 
 	private Cursor getCustDetailsById(long custId) {
 		Cursor  custDetails = null ;
    	if(custId != 0 ){
    		CustomerTable custTable = new CustomerTable(Ctxt);
 	    	custTable.open();
    		custDetails = custTable.fetchCustomerById(custId);
    		custTable.close();		    		
    	}   	
    	return custDetails;
 		
 	}
     
 }
