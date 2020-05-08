 package com.wingman;
 
 import java.net.MalformedURLException;
 import android.app.ListActivity; 
 import android.os.Bundle; 
 import android.view.View; 
 import android.view.Window;
 import android.widget.AdapterView; 
 import android.widget.ArrayAdapter; 
 import android.widget.LinearLayout;
 import android.widget.ListView; 
 import android.widget.TextView; 
 import android.widget.Toast; 
 import android.widget.AdapterView.OnItemClickListener; 
 import java.util.List;
 
 import com.google.ads.AdRequest;
 import com.google.ads.AdSize;
 import com.google.ads.AdView;
 import com.wingman.models.PersonModel;
 import com.wingman.parsers.AppharborWrapper;
 import com.wingman.parsers.FaceComWrapper;
 
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.os.AsyncTask;
 
 public class DisplayDescriptionActivity extends ListActivity {
 	private static final String MY_AD_UNIT_ID = "a14e50830f3a4d4";
 	private List<PersonModel> m_result;
 	private AdView adView;
 	
 	public void onCreate(Bundle bundle) {
 		super.onCreate(bundle);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		//setContentView(R.layout.addusertag);
 		setContentView(R.layout.displaydescription);
 	    
 		Intent i = getIntent();
 		Bundle extras = i.getExtras();
 		PersonModel pm = (PersonModel) extras.get("PersonModel");
 		
 		//TextView tv = (TextView) findViewById(R.id.textView1);
 		//tv.setText("Loading, please wait.");
 
 		Toast.makeText(getApplicationContext(), "Loading, please wait.",
 		          Toast.LENGTH_LONG).show();
 		
 		new QueryAppharborTask().execute(pm);
		
		
 	}
 	
 	
 	
 	class QueryAppharborTask extends AsyncTask<PersonModel, Void, List<PersonModel>> {
 
 		@Override
 		protected List<PersonModel> doInBackground(PersonModel... pms) {
 			int count = pms.length;
             PersonModel pm;
             if(count > 0)
             {
             	pm = pms[0];
             	// Load picture taken from cam
             	//List<PersonModel> result = null;
         		AppharborWrapper ahw = new AppharborWrapper(pm);
         		FaceComWrapper fcw = new FaceComWrapper();
         		try {        			
         			fcw.addPerson(pm);
         			m_result = ahw.FindAllDescriptions(pm);
         			//return m_result;
         		} catch (MalformedURLException e) {
         			// TODO Auto-generated catch block
         			e.printStackTrace();
         		}
             } 
 			return m_result;
 		}
 		
 		 protected void onPostExecute(List<PersonModel> result) {
 			//StringBuilder sb = new StringBuilder();
 			
 		 
 			 //TextView tv = (TextView) findViewById(R.id.textView1);
 			 //tv.setText(sb.toString());
      		m_result = result;
 			int resultCount = result.toArray().length;
  			String[] listContent = new String[resultCount];
       		for (int i=0; i< resultCount; i++) {
       			
       			PersonModel elem = result.get(i);
       			String listItem = elem.getFirstName() + " " + elem.getLastName();
       			listContent[i] = listItem;
       			
       			/*sb.append("Name: " + elem.getFirstName() + " " + elem.getLastName()
       					+ " Desc: " + elem.getComment() 
       					+ " Rating: " + elem.getRating() + "\n");
       					*/
       		}
 			 
     		setListAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.descriptionlist_item, listContent));
     		ListView lv = getListView();
     		//ListView lv = (ListView) findViewById(R.id.description_list);
   		  lv.setTextFilterEnabled(true);
   		  
   		  
   		  // Y U NO DO IT IN XML? :<< Could not find a way to do it. android:background="" and others tried, no effect.
   		  //Resources res = getResources();
   		  //lv.setBackgroundColor(res.getColor(R.color.default_background));
 
   		  lv.setOnItemClickListener(new OnItemClickListener() {
   		    public void onItemClick(AdapterView<?> parent, View view,
   		        int position, long id) {
   		      // When clicked, show a toast with the TextView text
   		    	CharSequence magic = ((TextView) view).getText();
   		      Toast.makeText(getApplicationContext(), magic,
   		          Toast.LENGTH_SHORT).show();
   		      
   		      PersonModel pm = m_result.get(position);
   		      Intent i = new Intent(getBaseContext(), DisplayDescriptionSelectActivity.class);
   		      i.putExtra("PersonModel", pm);
   		      startActivity(i);
   		    }
   		  });
 		 }
 	}}
