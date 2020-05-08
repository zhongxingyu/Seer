 package org.openhds.mobile.fragment;
 
 import org.openhds.mobile.R;
 import org.openhds.mobile.activity.LocationHierarchyActivity;
 import org.openhds.mobile.activity.ServerPreferencesActivity;
 import org.openhds.mobile.model.Individual;
 import org.openhds.mobile.model.Location;
 import org.openhds.mobile.model.LocationHierarchy;
 import org.openhds.mobile.model.LocationVisit;
 import org.openhds.mobile.model.Round;
 import org.openhds.mobile.model.StateMachine;
 import org.openhds.mobile.model.StateMachine.State;
 import org.openhds.mobile.model.StateMachine.StateListener;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class SelectionFragment extends Fragment implements OnClickListener {
 
     public static interface Listener {
         void onHierarchy1();
 
         void onHierarchy2();
 
         void onHierarchy3();
 
         void onHierarchy4();
 
         void onLocation();
 
         void onRound();
 
         void onIndividual();
     }
 
     private Listener listener;
     private LocationVisit locationVisit;
 
     private Button hierarchy1Btn, hierarchy2Btn, hierarchy3Btn, hierarchy4Btn, locationBtn, roundBtn, individualBtn;
 
     private TextView loginGreetingText, hierarchy1NameText, hierarchy1ExtIdText, hierarchy1Header, hierarchy2NameText,
             hierarchy2ExtIdText, hierarchy2Header,hierarchy3NameText, hierarchy3ExtIdText, hierarchy3Header,
             hierarchy4NameText, hierarchy4ExtIdText, hierarchy4Header,
             roundNumberText, roundStartDateText, roundEndDateText, locationNameText, locationExtIdText,
             locationLatitudeText, locationLongitudeText, individualFirstNameText, individualLastNameText,
             individualExtIdText, individualDobText;
     
     String hierarchy1, hierarchy2, hierarchy3, hierarchy4;
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         listener = (Listener) activity;
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
     	Activity thisActivity = this.getActivity();
    	SharedPreferences sp = thisActivity.getPreferences(0);
 		hierarchy1 = sp.getString(LocationHierarchyActivity.HIERARCHY_1, getString(R.id.hierarchy1));
 		hierarchy2 = sp.getString(LocationHierarchyActivity.HIERARCHY_2, getString(R.id.hierarchy2));
 		hierarchy3 = sp.getString(LocationHierarchyActivity.HIERARCHY_3, getString(R.id.hierarchy3));
 		hierarchy4 = sp.getString(LocationHierarchyActivity.HIERARCHY_4, getString(R.id.hierarchy4));
     	View view = inflater.inflate(R.layout.selection, container, false);
         bindViews(view);
         return view;
     }
 
     private void bindViews(View view) {
         loginGreetingText = (TextView) view.findViewById(R.id.loginGreetingText);
         hierarchy1Btn = (Button) view.findViewById(R.id.hierarchy1Btn);
        hierarchy1Btn.setText(hierarchy1);
         hierarchy1Btn.setOnClickListener(this);
         hierarchy1Header.setText(hierarchy1+":");
         hierarchy1NameText = (TextView) view.findViewById(R.id.hierarchy1Name);
         hierarchy1ExtIdText = (TextView) view.findViewById(R.id.hierarchy1ExtId);
 
         hierarchy2Btn = (Button) view.findViewById(R.id.hierarchy2Btn);
         hierarchy2Btn.setOnClickListener(this);
         hierarchy2Header.setText(hierarchy2+":");
         hierarchy2NameText = (TextView) view.findViewById(R.id.hierarchy2Name);
         hierarchy2ExtIdText = (TextView) view.findViewById(R.id.hierarchy2ExtId);
 
         hierarchy3Btn = (Button) view.findViewById(R.id.hierarchy3Btn);
         hierarchy3Btn.setOnClickListener(this);
         hierarchy3NameText = (TextView) view.findViewById(R.id.hierarchy3Name);
         hierarchy3ExtIdText = (TextView) view.findViewById(R.id.hierarchy3ExtId);
         hierarchy3Header.setText(hierarchy3+":");
 
         hierarchy4Btn = (Button) view.findViewById(R.id.hierarchy4Btn);
         hierarchy4Btn.setOnClickListener(this);
         hierarchy4NameText = (TextView) view.findViewById(R.id.hierarchy4Name);
         hierarchy4ExtIdText = (TextView) view.findViewById(R.id.hierarchy4ExtId);
         hierarchy4Header.setText(hierarchy4+":");
 
         locationBtn = (Button) view.findViewById(R.id.locationBtn);
         locationBtn.setOnClickListener(this);
         locationNameText = (TextView) view.findViewById(R.id.locationNameText);
         locationExtIdText = (TextView) view.findViewById(R.id.locationExtIdText);
         locationLatitudeText = (TextView) view.findViewById(R.id.locationLatitudeText);
         locationLongitudeText = (TextView) view.findViewById(R.id.locationLongitudeText);
 
         roundBtn = (Button) view.findViewById(R.id.roundBtn);
         roundBtn.setOnClickListener(this);
         roundNumberText = (TextView) view.findViewById(R.id.roundNumberText);
         roundStartDateText = (TextView) view.findViewById(R.id.roundStartDateText);
         roundEndDateText = (TextView) view.findViewById(R.id.roundEndDateText);
 
         individualBtn = (Button) view.findViewById(R.id.individualBtn);
         individualBtn.setOnClickListener(this);
         individualExtIdText = (TextView) view.findViewById(R.id.individualExtIdText);
         individualFirstNameText = (TextView) view.findViewById(R.id.individualFirstNameText);
         individualLastNameText = (TextView) view.findViewById(R.id.individualLastNameText);
         individualDobText = (TextView) view.findViewById(R.id.individualDobText);
     }
 
     private void setHierarchy1() {
         LocationHierarchy region = locationVisit.getHierarchy1();
         if (region == null) {
             region = LocationHierarchy.emptyHierarchy();
         }
 
         hierarchy1NameText.setText(region.getName());
         hierarchy1ExtIdText.setText(region.getExtId());
     }
 
     private void setHierarchy2() {
         LocationHierarchy subRegion = locationVisit.getHierarchy2();
         if (subRegion == null) {
             subRegion = LocationHierarchy.emptyHierarchy();
         }
 
         hierarchy2NameText.setText(subRegion.getName());
         hierarchy2ExtIdText.setText(subRegion.getExtId());
     }
 
     private void setHierarchy3() {
         LocationHierarchy hierarchy3 = locationVisit.getHierarchy3();
         if (hierarchy3 == null) {
             hierarchy3 = LocationHierarchy.emptyHierarchy();
         }
 
         hierarchy3NameText.setText(hierarchy3.getName());
         hierarchy3ExtIdText.setText(hierarchy3.getExtId());
     }
 
     private void setHierarchy4() {
         LocationHierarchy village = locationVisit.getHierarchy4();
         if (village == null) {
             village = LocationHierarchy.emptyHierarchy();
         }
 
         hierarchy4NameText.setText(village.getName());
         hierarchy4ExtIdText.setText(village.getExtId());
     }
 
     private void setRound() {
         Round round = locationVisit.getRound();
         if (round == null) {
             round = Round.getEmptyRound();
         }
 
         roundNumberText.setText(round.getRoundNumber());
         roundStartDateText.setText(round.getStartDate());
         roundEndDateText.setText(round.getEndDate());
     }
 
     public void onClick(View view) {
         switch (view.getId()) {
         case R.id.hierarchy1Btn:
             listener.onHierarchy1();
             break;
         case R.id.hierarchy2Btn:
             listener.onHierarchy2();
             break;
         case R.id.hierarchy3Btn:
             listener.onHierarchy3();
             break;
         case R.id.hierarchy4Btn:
             listener.onHierarchy4();
             break;
         case R.id.locationBtn:
             listener.onLocation();
             break;
         case R.id.roundBtn:
             listener.onRound();
             break;
         case R.id.individualBtn:
             listener.onIndividual();
             break;
         }
     }
 
     public void setLocationVisit(LocationVisit locationVisit) {
         this.locationVisit = locationVisit;
         loginGreetingText.setText("Hello, " + locationVisit.getFieldWorker().getFirstName() + " "
                 + locationVisit.getFieldWorker().getLastName());
     }
 
     public void registerTransitions(StateMachine stateMachine) {
         registerHierarchy1Listener(stateMachine);
         registerHierarchy2Listener(stateMachine);
         registerHierarchy3Listener(stateMachine);
         registerHierarchy4Listener(stateMachine);
         registerRoundListener(stateMachine);
         registerLocationListener(stateMachine);
         registerVisitListener(stateMachine);
         registerIndividualListener(stateMachine);
         registerEventListener(stateMachine);
         registerFinishVisitListener(stateMachine);
     }
 
     private void registerFinishVisitListener(StateMachine stateMachine) {
         stateMachine.registerListener(State.FINISH_VISIT, new StateListener() {
             public void onEnterState() {
             }
 
             public void onLeaveState() {
                 hierarchy1Btn.setEnabled(true);
                 hierarchy2Btn.setEnabled(true);
                 hierarchy3Btn.setEnabled(true);
                 hierarchy4Btn.setEnabled(true);
                 roundBtn.setEnabled(true);
             }
         });
     }
 
     private void registerEventListener(StateMachine stateMachine) {
         stateMachine.registerListener(State.SELECT_EVENT, new StateListener() {
             public void onEnterState() {
                 setIndividual();
             }
 
             public void onLeaveState() {
             }
         });
     }
 
     private void registerVisitListener(StateMachine stateMachine) {
         stateMachine.registerListener(State.CREATE_VISIT, new StateListener() {
             public void onEnterState() {
             }
 
             public void onLeaveState() {
                 hierarchy1Btn.setEnabled(false);
                 hierarchy2Btn.setEnabled(false);
                 hierarchy3Btn.setEnabled(false);
                 hierarchy4Btn.setEnabled(false);
                 roundBtn.setEnabled(false);
                 locationBtn.setEnabled(false);
             }
         });
     }
 
     private void registerIndividualListener(StateMachine stateMachine) {
         stateMachine.registerListener(State.SELECT_INDIVIDUAL, new StateListener() {
             public void onEnterState() {
                 setIndividual();
                 individualBtn.setEnabled(true);
             }
 
             public void onLeaveState() {
                 individualBtn.setEnabled(false);
             }
         });
     }
 
     private void registerLocationListener(StateMachine stateMachine) {
         stateMachine.registerListener(State.SELECT_LOCATION, new StateListener() {
             public void onEnterState() {
                 resetToDefaultState(5, false);
                 locationBtn.setEnabled(true);
             }
 
             public void onLeaveState() {
                 setLocation();
             }
         });
     }
 
     private void registerRoundListener(StateMachine stateMachine) {
         stateMachine.registerListener(State.SELECT_ROUND, new StateListener() {
             public void onEnterState() {
                 resetToDefaultState(4, false);
                 roundBtn.setEnabled(true);
 
             }
 
             public void onLeaveState() {
                 setRound();
             }
         });
     }
 
     private void registerHierarchy3Listener(StateMachine stateMachine) {
         stateMachine.registerListener(State.SELECT_HIERARCHY_3, new StateListener() {
             public void onEnterState() {
                 resetToDefaultState(2, false);
                 hierarchy3Btn.setEnabled(true);
             }
 
             public void onLeaveState() {
                 setHierarchy3();
             }
         });
     }
 
     private void registerHierarchy4Listener(StateMachine stateMachine) {
         stateMachine.registerListener(State.SELECT_HIERARCHY_4, new StateListener() {
             public void onEnterState() {
                 resetToDefaultState(3, false);
                 hierarchy4Btn.setEnabled(true);
             }
 
             public void onLeaveState() {
                 setHierarchy4();
             }
         });
     }
 
     private void registerHierarchy2Listener(StateMachine stateMachine) {
         stateMachine.registerListener(State.SELECT_HIERARCHY_2, new StateListener() {
             public void onEnterState() {
                 resetToDefaultState(1, false);
                 hierarchy2Btn.setEnabled(true);
             }
 
             public void onLeaveState() {
                 setHierarchy2();
             }
         });
     }
 
     private void registerHierarchy1Listener(StateMachine stateMachine) {
         stateMachine.registerListener(State.SELECT_HIERARCHY_1, new StateListener() {
             public void onEnterState() {
                 resetToDefaultState(0, false);
                 hierarchy1Btn.setEnabled(true);
             }
 
             public void onLeaveState() {
                 setHierarchy1();
             }
         });
     }
 
     private void resetToDefaultState(int level, boolean enabled) {
         switch (level) {
         case 0:
             hierarchy1Btn.setEnabled(enabled);
             setHierarchy1();
         case 1:
             hierarchy2Btn.setEnabled(enabled);
             setHierarchy2();
         case 2:
             hierarchy3Btn.setEnabled(enabled);
             setHierarchy3();
         case 3:
             hierarchy4Btn.setEnabled(enabled);
             setHierarchy4();
         case 4:
             roundBtn.setEnabled(enabled);
             setRound();
         case 5:
             locationBtn.setEnabled(enabled);
             setLocation();
         case 6:
             individualBtn.setEnabled(enabled);
             setIndividual();
         }
     }
 
     private void setIndividual() {
         Individual selectedIndividual = locationVisit.getSelectedIndividual();
         if (selectedIndividual == null) {
             selectedIndividual = Individual.emptyIndividual();
         }
 
         individualFirstNameText.setText(selectedIndividual.getFirstName());
         individualLastNameText.setText(selectedIndividual.getLastName());
         individualExtIdText.setText(selectedIndividual.getExtId());
         individualDobText.setText(selectedIndividual.getDob());
     }
 
     private void setLocation() {
         Location location = locationVisit.getLocation();
         if (location == null) {
             location = Location.emptyLocation();
         }
 
         locationNameText.setText(location.getName());
         locationExtIdText.setText(location.getExtId());
         locationLatitudeText.setText(location.getLatitude());
         locationLongitudeText.setText(location.getLongitude());
     }
 }
