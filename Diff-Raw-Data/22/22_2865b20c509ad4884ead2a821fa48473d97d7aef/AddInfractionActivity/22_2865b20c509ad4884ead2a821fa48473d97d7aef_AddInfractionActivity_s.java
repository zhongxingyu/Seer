 package com.sternerlearn;
 
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.ksoap2.serialization.SoapObject;
 import org.ksoap2.serialization.SoapPrimitive;
 import org.ksoap2.serialization.SoapSerializationEnvelope;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.RadioGroup;
 import android.widget.Toast;
 
 public class AddInfractionActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_add_infraction);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.add_infraction, menu);
 		return true;
 	}
 	
     public void handleSubmit
 		(
 		View aView
 		)
 	{
 		AsyncCall task = new AsyncCall();
 		task.execute();
 	} 	
     
     String addInfraction()
     {
     	/* Get time */
     	String time = getTimestamp();
     	
     	/* Get ID and password */
     	Account acct = SharedData.getInstance().getAccount();
     	int id = acct.mId;
         String pass = acct.mPassword;
         
         // Get name and infraction description
     	String name = ((EditText)findViewById( R.id.student_name )).getText().toString();
     	
     	String ret = "Error adding infraction";
     	
     	// Try to get the student ID
     	int studentID = -1;
 		try
 		{
 			List<PropertyWrapper> properties = new ArrayList<PropertyWrapper>();
 			properties.add(new PropertyWrapper("aTeacherID", id));
 			properties.add(new PropertyWrapper("aPassword", pass));
 			properties.add(new PropertyWrapper("aStudentName", name));
 			
 			SoapSerializationEnvelope env = WebServiceWrapper.getInstance().call(Types.STUDENT_URL, Types.STUDENT_GET_STUDENT, properties);
 			
			studentID = java.lang.Integer.parseInt( ((SoapObject)env.getResponse()).toString() );
 			
 		}
 		catch( Exception ex ) {}
 		
 		if( studentID > 0 )
 		{
 	    	String desc = ((EditText)findViewById( R.id.desc )).getText().toString();  
 	    	
 	    	RadioGroup group = ((RadioGroup)findViewById( R.id.radioGroup1 ));
 	    	View button = findViewById(group.getCheckedRadioButtonId());
 	    	int type = group.indexOfChild(button);        
 	    	
 	    	/* Create list of web method parameters */
 	        List<PropertyWrapper> paramList = new ArrayList<PropertyWrapper>();
 	        paramList.add( new PropertyWrapper( "aTeacherID" , id ) );
 	        paramList.add( new PropertyWrapper( "aPassword" , pass ) );
 	        
 	        paramList.add( new PropertyWrapper( "aStudentID" , studentID ) );
 	        paramList.add( new PropertyWrapper( "aInfractionType" , type ) );
 	        paramList.add( new PropertyWrapper( "aDescription" , desc ) );
 	        paramList.add( new PropertyWrapper( "aDate" , time ) );
 	        
 	        /* Log to database */
 	        SoapSerializationEnvelope envelope = WebServiceWrapper.getInstance().call(Types.STUDENT_URL, Types.STUDENT_ADD_INFRACTION, paramList );
 	        try
 	        {
 	        	SoapPrimitive primitive = (SoapPrimitive)envelope.getResponse();
 	        	
 	        	if( primitive.toString().compareTo("true") == 0 )
 	        	{
 	        		ret = "Successfully added infraction!";
 	        	}
 	        }
 	        catch( Exception ex) {}	    
 		}
 		
 		return ret;
     }
     
     void finishAsync( String res )
     {
 		Toast toast = Toast.makeText(getApplicationContext(), res, Toast.LENGTH_LONG);
 		toast.show();	   
 		
     	finish();
     }
     
 	@SuppressLint("SimpleDateFormat")
 	public static String getTimestamp()
 	{
 		/* Create time object */
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
 		String dt = sdf.format(new Date());
 		
     	return dt;
 	}
     
     private class AsyncCall extends AsyncTask<Void, Void, String> {
         @Override
         protected String doInBackground(Void... params) {
             return addInfraction();
         }
 
         @Override
         protected void onPostExecute(String result) {
     		finishAsync( result );        	
         }
 
         @Override
         protected void onPreExecute() {
         }
 
         @Override
         protected void onProgressUpdate(Void... values) {
         }
 
     }	     
 
 }
