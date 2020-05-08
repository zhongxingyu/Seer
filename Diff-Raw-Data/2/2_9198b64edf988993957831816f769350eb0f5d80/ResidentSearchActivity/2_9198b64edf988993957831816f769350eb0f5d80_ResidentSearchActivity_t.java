 package org.vcs.medmanage;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 
 import com.j256.ormlite.dao.DaoManager;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import db.DatabaseHelper;
 import entities.Resident;
 import entities.ResidentUtils;
 
 public class ResidentSearchActivity extends Activity {
 
     private List<String> corridorsList = Arrays.asList(new String[]{"Corridor 1", "Corridor 2", "Corridor 3"});
     private List<String> residentStatusList = Arrays.asList(new String[]{"Red", "Yellow", "Green"});
     private List<String> alphabeticRangeList = Arrays.asList(new String[]{"A-F", "G-M", "N-Z"});
 
     private List<Resident> residentList = new ArrayList<Resident>();
 
     private ArrayAdapter<String> corridorsAdapter;
     private ArrayAdapter<String> residentStatusesAdapter;
     private ArrayAdapter<String> alphabeticRangeAdapter;
 
     private ArrayAdapter<Resident> residentAdapter;
 
     private ResidentService residentService;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.resident_search);
 
         ApplicationInitializer.init(this);
         setupUI();
     }
 
     private void setupUI() {
         corridorsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, corridorsList);
         residentStatusesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, residentStatusList);
         alphabeticRangeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, alphabeticRangeList);
 
        residentAdapter = new ArrayAdapter<Resident>(this, R.layout.list_item, R.id.listTextView, residentList);
 
         residentService = new ResidentService(this);
 
         final ListView residentListView = (ListView) findViewById(R.id.residentListView);
         residentListView.setAdapter(residentAdapter);
 
         final ListView searchOptionsListView = (ListView) findViewById(R.id.searchOptionsListView);
         searchOptionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 ((ListView)parent).setItemChecked(position, true);
                 fetchAndDisplayResidents(searchOptionsListView.getAdapter(), position);
             }
         });
 
         // Emulate room search button getting clicked by default.
         RadioButton roomSearchRadioButton = (RadioButton) findViewById(R.id.roomSearchRadioButton);
         roomSearchRadioButton.setChecked(true);
         selectSearchType(roomSearchRadioButton);
         fetchAndDisplayResidents(searchOptionsListView.getAdapter(), 0);
     }
 
     private void fetchAndDisplayResidents(ListAdapter adapter, int position) {
         RadioGroup searchButtonsRadioGroup = (RadioGroup) findViewById(R.id.searchButtonsRadioGroup);
 
         List<Resident> searchedResidents;
         String searchTerm = (String)adapter.getItem(position);
 
         switch (searchButtonsRadioGroup.getCheckedRadioButtonId()) {
             case R.id.roomSearchRadioButton:
             default:
                 searchedResidents = residentService.getResidentsForCorridor(searchTerm);
                 break;
             case R.id.statusSearchRadioButton:
                 searchedResidents = residentService.getResidentsForStatus(searchTerm);
                 break;
             case R.id.alphabeticSearchRadioButton:
                 searchedResidents = residentService.getResidentsForAlphabetRange(searchTerm);
                 break;
         }
 
         residentAdapter.clear();
         residentAdapter.addAll(searchedResidents);
     }
 
     public void selectSearchType(View view) {
         ListView searchOptionsListView = (ListView) findViewById(R.id.searchOptionsListView);
 
         switch (view.getId()) {
             case R.id.roomSearchRadioButton:
             default:
                 searchOptionsListView.setAdapter(corridorsAdapter);
                 break;
             case R.id.statusSearchRadioButton:
                 searchOptionsListView.setAdapter(residentStatusesAdapter);
                 break;
             case R.id.alphabeticSearchRadioButton:
                 searchOptionsListView.setAdapter(alphabeticRangeAdapter);
                 break;
         }
         searchOptionsListView.setItemChecked(0, true);
     }
 
 }
