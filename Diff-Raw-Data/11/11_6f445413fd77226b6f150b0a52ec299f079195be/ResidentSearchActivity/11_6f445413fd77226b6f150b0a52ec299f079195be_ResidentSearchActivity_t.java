 package org.vcs.medmanage;
 
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.LinearLayout;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import entities.Resident;
 
 public class ResidentSearchActivity extends FragmentActivity {
 
    private List<String> corridorsList = Arrays.asList(new String[]{"Corridor 1", "Corridor 2", "Corridor 3", "Corridor 4"});
     private List<String> residentStatusList = Arrays.asList(new String[]{"Red", "Yellow", "Green"});
     private List<String> alphabeticRangeList = Arrays.asList(new String[]{"A-C", "D-G", "H-L", "M-Q", "R-T", "U-Z"});
 
     private ArrayAdapter<String> corridorsAdapter;
     private ArrayAdapter<String> residentStatusesAdapter;
     private ArrayAdapter<String> alphabeticRangeAdapter;
 
     private ResidentService residentService;
     private LinearLayout residentsLayout;
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.resident_search);
 
         setupUI();
     }
 
     private void setupUI() {
         corridorsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, corridorsList);
         residentStatusesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, residentStatusList);
         alphabeticRangeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1, alphabeticRangeList);
 
         residentsLayout = (LinearLayout)findViewById(R.id.residentSearchLinearLayout);
 
         residentService = new ResidentService(this);
 
         final ListView searchOptionsListView = (ListView) findViewById(R.id.searchOptionsListView);
         searchOptionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 ((ListView) parent).setItemChecked(position, true);
                 fetchAndDisplayResidents(searchOptionsListView.getAdapter(), position);
             }
         });
 
         // Emulate room search button getting clicked by default.
         RadioButton roomSearchRadioButton = (RadioButton) findViewById(R.id.roomSearchRadioButton);
         roomSearchRadioButton.setChecked(true);
         selectSearchType(roomSearchRadioButton);
     }
 
     private void fetchAndDisplayResidents(ListAdapter adapter, int position) {
         RadioGroup searchButtonsRadioGroup = (RadioGroup) findViewById(R.id.searchButtonsRadioGroup);
 
         List<Resident> searchedResidents;
         String searchTerm = (String) adapter.getItem(position);
 
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
 
         residentsLayout.removeAllViews();
         addResidentsToView(searchedResidents);
     }
 
     public void addResidentsToView(List<Resident> residents){
         for(int i = 0; i < residents.size(); i++){
             Bundle residentArgs = new Bundle();
             residentArgs.putString("ResidentName", residents.get(i).getName());
             residentArgs.putInt("RoomNumber", residents.get(i).getRoomNumber());
             FragmentManager fragmentManager = getSupportFragmentManager();
             FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
 
             ResidentFragment recentFragment = new ResidentFragment();
             recentFragment.setArguments(residentArgs);
             fragmentTransaction.add(residentsLayout.getId(), recentFragment);
             fragmentTransaction.commit();
         }
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
         fetchAndDisplayResidents(searchOptionsListView.getAdapter(), 0);
     }
 
 }
