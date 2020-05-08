 package com.example.ap.verifier;
 
 
 //import com.example.ap.comparison_strings.*;
 
 import java.util.Arrays;
 import java.util.List;
 
 import android.nfc.NfcAdapter;
 import android.nfc.Tag;
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class HomeActivity extends Activity {
     private NfcAdapter mNfcAdapter;  
     private IntentFilter[] mWriteTagFilters;  
     private PendingIntent mNfcPendingIntent;  
     private boolean silent=false;  
     private boolean writeProtect = false;  
     private Context context;  
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_home);
 		
 		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);  
 		mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP  
             | Intent.FLAG_ACTIVITY_CLEAR_TOP), 0);  
   
 		IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);  
 		
 		// Intent filters for writing to a tag  
 		mWriteTagFilters = new IntentFilter[] { techDetected };  		
 		
 		Button buttonCalc = (Button) findViewById(R.id.calculate);
         buttonCalc.setOnClickListener(new OnClickListener()
         {
      	   public void onClick(View v){
      		   //ssc s = new ssc();
      		   //String comparison = s.get_comparison_string();
      		   //TextView text = (TextView) findViewById(R.id.shortStringValue);
      		   //text.setText(comparison);
      		   }
      	   }
         );
         
         Button buttonWriteTag = (Button) findViewById(R.id.writeTag);
         buttonWriteTag.setOnClickListener(new OnClickListener()
         {
      	   public void onClick(View v){
     		   byte[] file = null;
     		   Tag tag = null;
     		   TagWriter m = new TagWriter(); 		   
      		   
     		   m.writeMifareClassic(tag, file);
      	   }
         }
         );
         
         Button buttonAbout = (Button) findViewById(R.id.about);
         buttonAbout.setOnClickListener(new OnClickListener()
         {
      	   public void onClick(View v){
      		   Intent i = new Intent(HomeActivity.this, About.class); 
      		   startActivity(i);
      		   }
      	   }
         );
 	}
 
 	@Override  
 	protected void onResume() 
 	{  
 		super.onResume();  
 		
 		if(mNfcAdapter != null)
 			mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mWriteTagFilters, null);
 	}
 
     @Override  
     protected void onPause() 
     {  
          super.onPause();  
          if(mNfcAdapter != null) 
         	 mNfcAdapter.disableForegroundDispatch(this);  
     }  
     
     @Override  
     protected void onNewIntent(Intent intent) 
     {  
     	super.onNewIntent(intent);   
     	String action = intent.getAction();
         
         if(NfcAdapter.ACTION_TECH_DISCOVERED.equals(action))
         {
             Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
             
             //Get tech list (tag type) from intent
             String[] techList_array = tag.getTechList();
             
             //Print out tech list to screen
             String techList_string = "";
             for(String tech : techList_array)
             {
             	techList_string += tech;
             	techList_string += "\n";
             }
             
             List<String> techList = Arrays.asList(techList_array);
             
             if(techList.contains("android.nfc.tech.MifareClassic"))
             {
             	MifareClassicParser mcp = new MifareClassicParser();
             	mcp.readMifareClassic(tag);
             }  
         }
     }      
 			
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_home, menu);
 		return true;
 	}
 
 }
