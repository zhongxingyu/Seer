 package ucn.datamatiker.afr_rmlp.housingenabler;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import ucn.datamatiker.afr_rmlp.housingenabler.datasources.ResultDataSource;
 import ucn.datamatiker.afr_rmlp.housingenabler.helpers.CalculationHelper;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.support.v4.widget.DrawerLayout;
 import android.text.Html;
 import android.text.InputFilter.LengthFilter;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ExpandableListView;
 import android.widget.ExpandableListView.OnChildClickListener;
 import android.widget.ExpandableListView.OnGroupCollapseListener;
 import android.widget.ExpandableListView.OnGroupExpandListener;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class HousingEnablerTest extends Activity {
 	
 	private int lastExpandedPosition = -1;
 	View _lastColored;
 	List<String> groupList;
     List<String> childList;
     Map<String, List<String>> questionCollection;
     ExpandableListView expListView;
 	
 	CalculationHelper calHelp;
 	private Integer[] testUser;
 	
 	int mode = Activity.MODE_PRIVATE;
 	private SharedPreferences mPrefs;
     
     ArrayList<Integer> resultArray;
 	
 	private ResultDataSource resultDataSource;
 	
 	private DrawerLayout mDrawerLayout;
     private ListView mDrawerListLeft;
     private ListView mDrawerListRight;
 
     String[] resultList = new String[4];
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		testUser = new Integer[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
 		
 		SharedPreferences mPrefs = getSharedPreferences("appPrefs", mode);
 		
 		setContentView(R.layout.activity_housing_enabler_test);
 		
 		resultDataSource = new ResultDataSource(this);
 		resultDataSource.open();
 		
 		Log.d("Housing test", String.valueOf(mPrefs.getInt("resultId", 0)));
 		
 		resultArray = new ArrayList<Integer>();
 		
 		if (mPrefs.getInt("resultId", 0) == 0) {
 			
 			resultArray = resultDataSource.createResult();
 			
 			long aTempId = resultArray.get(resultArray.size() - 1);
 			
 			long oneMoreTemp = resultDataSource.createCaseInfo(aTempId, "Text case " + aTempId, "21-05-2012", "Christiansgade 54, 1. th.", "Aalborg", "9000");
 			
 			Log.d("In the test class", resultArray.toString());
 			Log.d("In the test class", String.valueOf(oneMoreTemp));
 			
 		} else {
 			resultArray = resultDataSource.getResult(mPrefs.getInt("resultId", 0));
 		}
 		
 		calHelp = new CalculationHelper(this);
 		
 		this.createGroupList();
 		this.createCollection();
 		
         expListView = (ExpandableListView) findViewById(R.id.question_list);
         final ucn.datamatiker.afr_rmlp.housingenabler.adapters.ExpandableListAdapter expListAdapter = new ucn.datamatiker.afr_rmlp.housingenabler.adapters.ExpandableListAdapter(this, groupList, questionCollection);
         expListView.setAdapter(expListAdapter);
         
         expListView.setOnChildClickListener(new OnChildClickListener() {
         	 
             public boolean onChildClick(ExpandableListView parent, View v,
                     int groupPosition, int childPosition, long id) {
 
                 final String selected = (String) expListAdapter.getChild(groupPosition, childPosition);
                 
                 /**
                  * for(int i=0; i<((ViewGroup)v).getChildCount(); ++i) {
                     View nextChild = ((ViewGroup)v).getChildAt(i);
                     ((TextView)nextChild).setText(((TextView)nextChild).getText() + " (Valgt)");
                 }
                 */
                 
                 expListAdapter.setChild(groupPosition, childPosition);
                 expListAdapter.notifyDataSetChanged();
                 
                 
                 // Update database
                 String columnToUpdate = calHelp.getQuestionKeys().get(groupPosition);
                 int idToUpdate = resultArray.get(resultArray.size() - 1);
                 int theValue = 0;
                 
                 if (childPosition == 0) {
                 	theValue = 1; // Yes
                 } else if (childPosition == 1) {
                 	theValue = 2; // No
                 } else if (childPosition == 2) {
                 	theValue = 3; // Not selected
                 }
                 
                resultDataSource.updateResult(idToUpdate, columnToUpdate, theValue);
                 
                // Log.d("something", String.valueOf(((ViewGroup)v).getChildCount()));
                 
                 /**
                 if(_lastColored != null) {
 	                _lastColored.setBackgroundColor(Color.TRANSPARENT);
 	                _lastColored.invalidate();
                 }
                 _lastColored = v;
                 v.setBackgroundColor(Color.rgb(214, 214, 214));
                 */
                 
                 //Toast.makeText(getBaseContext(), selected, Toast.LENGTH_SHORT).show();
                 //Toast.makeText(getBaseContext(), String.valueOf(v.getId()), Toast.LENGTH_SHORT).show();
                 
                 updateScores();
  
                 return false;
             }
         });
         
   
         expListView.setOnGroupExpandListener(new OnGroupExpandListener() {
 
             @Override
             public void onGroupExpand(int groupPosition) {
                     if (lastExpandedPosition != -1
                             && groupPosition != lastExpandedPosition) {
                     	expListView.collapseGroup(lastExpandedPosition);
                     }
                     lastExpandedPosition = groupPosition;
             }
         });
         
         
         String[] listOfHandicaps = getResources().getStringArray(R.array.handicaps_array);
         mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         mDrawerListLeft = (ListView) findViewById(R.id.left_drawer);
         mDrawerListRight = (ListView) findViewById(R.id.right_drawer);
 
         // Set the adapter for the list view
         mDrawerListLeft.setAdapter(new ArrayAdapter<String>(this,
                 R.layout.drawer_list_item, listOfHandicaps));
         // Set the list's click listener
         mDrawerListLeft.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView parent, View view, int position, long id) {
 				view.setBackgroundColor(Color.rgb(214, 214, 214));
 	
 				
 				if (testUser[position] == 0) {
 					testUser[position] = 1;
 					view.setBackgroundColor(Color.rgb(214, 214, 214));
 					Log.d("Test color", "Fun " + view.getBackground().toString());
 				} else {
 					testUser[position] = 0;
 					view.setBackgroundColor(Color.parseColor("#C2C2C2"));
 					Log.d("Test color", view.getBackground().toString());
 				}
 				
 				updateScores();
 				
 				//Toast.makeText(getApplicationContext(), String.valueOf(position), Toast.LENGTH_SHORT).show();
 			}
         	
         });
 		
         // Set the list's click listener
         mDrawerListRight.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView parent, View view, int position, long id) {
 				view.setBackgroundColor(Color.rgb(214, 214, 214));
 				
 				if (testUser[position] == 0) {
 					testUser[position] = 1;
 					view.setBackgroundColor(Color.rgb(214, 214, 214));
 					Log.d("Test color", "Fun " + view.getBackground().toString());
 				} else {
 					testUser[position] = 0;
 					view.setBackgroundColor(Color.parseColor("#C2C2C2"));
 					Log.d("Test color", view.getBackground().toString());
 				}
 				
 				Toast.makeText(getApplicationContext(), String.valueOf(position), Toast.LENGTH_SHORT).show();
 			}
         	
         });
         
         
 		this.updateScores();
 	}
 	
 
 	
 	private void updateScores() {
 	    
         mDrawerListRight.setAdapter(new ArrayAdapter<String>(this,
                 R.layout.drawer_list_item, resultList));
 		
 		Integer[] total = calHelp.generateTotal(testUser, resultArray);
 		Integer superTotal = 0;
 		
 		int index = 0;
 		while (index < total.length) {
 			superTotal += total[index];
 			index++;
 		}
 		
         // Set the adapter for the list view
         resultList[0] = "TOTAL SCORE: " + superTotal;
         resultList[1] = "DEL SCORE 1: ";
         resultList[2] = "DEL SCORE 2: ";
         resultList[3] = "DEL SCORE 3: ";
 
 	}
 	
 	private void createGroupList() {
         groupList = calHelp.getAllQuestions();
 		//groupList = new ArrayList<String>();
 		//groupList.add("Test");
 	}
  
     private void createCollection() {
         questionCollection = new LinkedHashMap<String, List<String>>();
  
         int count = 0;
         for (String question : groupList) {
         	
         	ArrayList<String> models = new ArrayList<String>();
         	
         	String val1 = "Ja";
         	String val2 = "Nej";
         	String val3 = "Ej vurderet";
             
         	if (resultArray.get(count) == 1) {
         		val1 = "Ja (Valgt)";
         	} else if (resultArray.get(count) == 2) {
         		val2 = "Nej (Valgt)";
         	} else if (resultArray.get(count) == 3) {
         		val3 = "Ej vurderet (Valgt)";
         	}
         	
         	models.add(val1);
         	models.add(val2);
         	models.add(val3);
 
             questionCollection.put(question, models);
            
             count++;
             
         }
     }
     
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.housing_enabler_test, menu);
 		return true;
 	}
 	
 	@Override
 	protected void onResume() {
 		resultDataSource.open();
 		super.onResume();
 	}
 	
 
 	@Override
 	protected void onPause() {
 		
 		mPrefs = getSharedPreferences("appPrefs", mode);
 		SharedPreferences.Editor ed = mPrefs.edit();
 		
 		if (resultArray.size() > 0) {
 			ed.putInt("resultId", resultArray.get(resultArray.size() - 1));
 			ed.commit();
 		}
 		
 		resultDataSource.close();
 		super.onPause();
 	}
 
 }
