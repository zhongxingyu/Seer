 package mwhs.ap.bkat.app;
 
 import java.util.ArrayList;
 
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class SearchResults extends ListActivity {
 	 private ArrayList<School> schools;
 	 private ArrayList<School> matchedSchools = new ArrayList<School>();
 	 private String[] matchedInfo;
 	 private String[] info;
 	 private String major = " ";
 	 private String sport  = " ";
 	 private int population = 0;
 	 private int cost = 0;
 	 private String housing  = " ";
 	 private String region = " ";
 	   /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       //Get the variables stored in the bundle passed from the main activity
       Bundle b = this.getIntent().getExtras();
       
       info = b.getStringArray("strings");
       schools = b.getParcelableArrayList("schools");
       setVariables();
       
       compareValues();
       matchedInfo = new String[matchedSchools.size()];
       for (int i = 0; i < matchedSchools.size(); i++) {
 		matchedInfo[i] = matchedSchools.get(i).getSchoolName();
       }
       setListAdapter(new ArrayAdapter<String>(this, mwhs.ap.doan.app.R.layout.results_list,matchedInfo));
      
       ListView lv = getListView();
       lv.setTextFilterEnabled(true);
       
       lv.setOnItemClickListener(new OnItemClickListener() {
         public void onItemClick(AdapterView<?> parent, View view,
             int position, long id) {
           // When clicked, show a toast with the TextView text
           Toast.makeText(getApplicationContext(), ((TextView) view).getText(),
               Toast.LENGTH_SHORT).show();
         }
       });
     }
 	private void setVariables() {
 		if (!info[0].equals("")) {
 			major = info[0];
 		}
 		if (!info[1].equals("")) {
 			sport = info[1];
 		}
 		if (!info[2].equals("")) {
 			population = Integer.parseInt(info[2]);
 		}
 		String a = info[3];
 		if (!a.equals("")) {
 			 cost = Integer.parseInt(info[3]);
 		} 
 		if (!info[4].equals("")) {
 			 housing = info[4];
 		}
 		if (!info[5].equals("")) {
 			 region = info[5];
 		}  
 	}
 	
 	private ArrayList<School> compareValues() {
 		for (int i = 0; i < schools.size(); i++) {
 				if(population == schools.get(i).getTotalUndergrads() ||
 				   major.equalsIgnoreCase(schools.get(i).getSchoolType())){
 					matchedSchools.add(schools.get(i));
 				}			
 		}
 		return matchedSchools;
 		
 	}
 
 }
