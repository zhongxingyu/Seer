 package xs.ww.ruisi;
 
 import java.util.ArrayList;
 
 import xs.ww.ruisi.util.SingtelUtil;
 import android.app.ListActivity;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public class SingtelReminderActivity extends ListActivity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         SingtelUtil util = new SingtelUtil();
         util.initialMap();
       //Listen for button clicks
   		Button button = (Button)findViewById(R.id.query);
   		button.setOnClickListener(queryCall);
   		
   		
     }
     
     private OnClickListener queryCall = new OnClickListener()
 	{
 		public void onClick(View v)
 		{
 			ArrayList<Integer> durationList = new ArrayList<Integer>();
 			// define the search criteria
 			String mSelectionClause = null;
 			String[] mSelectionArgs = {"","",""};
 			
 			// get the call type
 			Spinner fieldcallType = (Spinner)findViewById(R.id.calltype);
 			String selectedType = fieldcallType.getSelectedItem().toString();
 			// get the search date
 			DatePicker datePicker = (DatePicker)findViewById(R.id.datePicker1);
 			int day = datePicker.getDayOfMonth();
 			int month = datePicker.getMonth();
 			int year = datePicker.getYear();
 			
 			if (TextUtils.isEmpty(selectedType)) {
 			    // Setting the selection clause to null will return all words
 			    mSelectionClause = null;
 			    mSelectionArgs[0] = "";
 
 			} else {
 			    // Constructs a selection clause that matches the word that the user entered.
 			    mSelectionClause = " type = ? and date >= ? and date < ? ";
 			    
 			    // Moves the user's input string to the selection arguments.
 			    mSelectionArgs[0] = SingtelUtil.map.get(selectedType);
 			    mSelectionArgs[1] = SingtelUtil.msAfterBaseDateWithInputYMD(year, month, day);
 			    mSelectionArgs[2] = SingtelUtil.msAfterBaseDateWithInputYMD(year, month+1, day);
 			}
 			
 			// do the query
 			String[] strFields = {
 			        android.provider.CallLog.Calls.NUMBER, 
 			        android.provider.CallLog.Calls.TYPE,
 			        android.provider.CallLog.Calls.DURATION,
 			        android.provider.CallLog.Calls.DATE,
 			        };
 			String strOrder = android.provider.CallLog.Calls.DATE + " DESC"; 
 			 
 			Cursor mCallCursor = getContentResolver().query(
 			        android.provider.CallLog.Calls.CONTENT_URI,
 			        strFields,
 			        mSelectionClause,
 			        mSelectionArgs,
 			        strOrder
 			        );
 			
 			int totalCnt = mCallCursor.getCount();
 			System.out.println("totalCnt"+totalCnt);
 			if(mCallCursor.moveToFirst()){
 				do{
 					String number = mCallCursor.getString(0);
 					String type = mCallCursor.getString(1);
 					Integer duration = new Integer(mCallCursor.getInt(2));
 					String date = mCallCursor.getString(3);
 					if(duration != 0 && !SingtelUtil.noChargeNumbers(number)){
 						durationList.add(duration);
 					}
 				}while(mCallCursor.moveToNext());
 				
 			}
 			
 			int totalMins = 0;
 			if(durationList.size()>0){
 				totalMins = SingtelUtil.totalCallTime(durationList);
 			}
 			
 //			Intent intent = new Intent(SingtelReminderActivity.this, CallHistoryActivity.class);
 //			startActivity(intent);
 			// set to result text
 			TextView result = (TextView)findViewById(R.id.Result);
 			result.setText(""+ totalMins +" minutes");
 			if(totalMins>200){
 				result.setTextColor(Color.RED);
 			}
 		}
 	};
 }
