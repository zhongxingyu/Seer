 package mwhs.ap.bkat.app;
 
 import java.util.ArrayList;
 
 import mwhs.ap.doan.app.R;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class SearchResults extends ListActivity {
 	 private ArrayList<School> schools;
 	 private ArrayList<School> matchedSchools = new ArrayList<School>();
 	 private String[] matchedInfo;
 	 private String[] info;
 	 private String major = " ";
 	 private String population = " ";
 	 private String cost = " ";
 	 private String region = " ";
 	 private String setting = " ";
 	 private int minCost;
 	 private int maxCost;
 	 private int minPop;
 	 private int maxPop;
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
       
       if (matchedInfo.length == 0) {
 		matchedInfo = new String[1];
		matchedInfo[0] = "NO SEARCH RESULTS FOUND";
 	}
       
       setListAdapter(new ArrayAdapter<String>(this, mwhs.ap.doan.app.R.layout.results_list,matchedInfo));
       
       ListView lv = getListView();
       lv.setTextFilterEnabled(true);
       
       lv.setOnItemClickListener(new OnItemClickListener() {
         public void onItemClick(AdapterView<?> parent, View view,
             int position, long id) {
           
 //        	Bundle b = new Bundle();
 //			b.putString("schoolPicked", matchedInfo[position]);
 //			Bundle b2 = new Bundle();
 //			b2.putParcelableArrayList("schoolList", schools);
 			Intent i = new Intent();
 //			i.putExtras(b);
 //			i.putExtras(b2);
 			i.setClass(getApplicationContext(), SchoolPage.class);
 //			startActivity(i);
 			
 			String name = matchedInfo[position];
 			
 			School s=null;
 			for (int j = 0; j < schools.size(); j++) {
 				if (schools.get(j).getSchoolName().equals(name)) {
 					s = schools.get(j);
 				}
 			}
 			if(s != null){
 				Bundle bundle = new Bundle();
 				bundle.putParcelable("school", s);
 				i.putExtras(bundle);
 				startActivity(i);
 			}
         }
       });
     }
 	private void setVariables() {
 		if (!info[0].equals("")) {
 			major = info[0];
 		}else{
 			major =" ";
 		}
 		String b = info[1];
 		if (b.equals("1-1,000")) {
 			minPop = 0;
 			maxPop = 1000;
 		} else if(b.equals("1,001-5,000")){
 			minPop = 1001;
 			maxPop = 5000;
 		} else if(b.equals("5,001-10,000")){
 			minPop = 5001;
 			maxPop = 10000;
 		} else if(b.equals("10,001-15,000")){
 			minPop = 10001;
 			maxPop = 15000;
 		} else if(b.equals("15,001-20,000")){
 			minPop = 15001;
 			maxPop = 20000;
 		} else if(b.equals("20,001-25,000")){
 			minPop = 20001;
 			maxPop = 25000;
 		} else if(b.equals("25,001-30,000")){
 			minPop = 25001;
 			maxPop = 30000;
 		} else if(b.equals("30,001-35,000")){
 			minPop = 30001;
 			maxPop = 35000;
 		} else if(b.equals("35,001-40,000")){
 			minPop = 35001;
 			maxPop = 40000;
 		} else if(b.equals("40,001-45,000")){
 			minPop = 40001;
 			maxPop = 45000;
 		} else if(b.equals("45,001-50,000+")){
 			minPop = 45001;
 			maxPop = Integer.MAX_VALUE;
 		} else{
 			minPop = 0;
 			maxPop = Integer.MAX_VALUE;
 		}
 		String a = info[2];
 		if(a.equals("$1,000-$10,000" )){
 			minCost = 0;
 			maxCost = 10000;
 		} else if(a.equals("$10,001-$20,000" )){
 			minCost = 10001;
 			maxCost = 20000;			
 		}else if(a.equals("$20,001-$30,000" )){
 			minCost = 20001;
 			maxCost = 30000;
 		}else if(a.equals("$30,001-$40,000" )){
 			minCost = 30001;
 			maxCost = 40000;
 		}else if(a.equals("40,001-$50,000+" )){
 			minCost = 40001;
 			maxCost = Integer.MAX_VALUE;
 		}else{
 			minCost = 0;
 			maxCost = Integer.MAX_VALUE;
 		}
 		if (!info[3].equals("")) {
 			 region = info[3];
 		}else{
 			region = " ";
 		}
 		if (!info[4].equals("")) {
 			setting = info[4];
 		}else{
 			setting = " ";
 		}
 		
 		
 		
 	}
 	
 	private ArrayList<School> compareValues() {
 		for (int i = 0; i < schools.size(); i++) {
 //				if(minPop <= schools.get(i).getTotalUndergrads() && 
 //				   maxPop >= schools.get(i).getTotalUndergrads() ||
 //				   region.equals(schools.get(i).getState()) ||
 //				   setting.equals(schools.get(i).getSetting()) ||
 //				   minCost <= a && maxCost >= a){
 //						matchedSchools.add(schools.get(i));
 //				}
 				if (minPop <= schools.get(i).getTotalUndergrads() && 
 				   maxPop >= schools.get(i).getTotalUndergrads()) {
 					matchedSchools.add(schools.get(i));
 				}
 		}		
 				for (int j = 0; j < matchedSchools.size(); j++) {
 					int a = (matchedSchools.get(j).getTuitionInState());
 					School l = matchedSchools.get(j);
 					boolean majorFound = false;
 					boolean remove = false;
 					if (!region.equals(" ")) {
 						if (!region.equals(matchedSchools.get(j).getState())) {
 						remove = true;
 					}
 					}
 					if (!setting.equals(" ")) {
 						if (!setting.equals(matchedSchools.get(j).getSetting())) {
 						remove = true;
 					}
 					}
 					if (!(minCost <= a) || !(maxCost >= a)) {
 						remove = true;
 						
 					}
 					if (!major.equals(" ")) {
 						for (int j2 = 0; j2 < matchedSchools.get(j).getMajors().length; j2++) {
 						
 						if (major.equalsIgnoreCase(matchedSchools.get(j).getMajors()[j2])) {
 							majorFound = true;
 							break;
 						}
 						}if (!majorFound) {
 							remove = true;
 						}
 					}
 					if (remove) {
 						matchedSchools.remove(j--);
 					}
 				}
 		return matchedSchools;
 		
 	}
 
 }
