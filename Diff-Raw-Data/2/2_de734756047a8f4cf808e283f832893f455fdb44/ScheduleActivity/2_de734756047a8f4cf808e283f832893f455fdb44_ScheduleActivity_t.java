 package pt.up.fe.cmov;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 
 import pt.up.fe.cmov.display.ScheduleAdapter;
 import pt.up.fe.cmov.display.ScheduleButton;
 import pt.up.fe.cmov.entities.Appointment;
 import pt.up.fe.cmov.entities.Schedule;
 import pt.up.fe.cmov.entities.SchedulePlan;
 import pt.up.fe.cmov.listadapter.EntryAdapter;
 import pt.up.fe.cmov.operations.AppointmentOperations;
 import pt.up.fe.cmov.operations.DoctorOperations;
 import pt.up.fe.cmov.operations.PatientOperations;
 import pt.up.fe.cmov.operations.ScheduleOperations;
 import pt.up.fe.cmov.operations.SchedulePlanOperations;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.GridView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ScheduleActivity extends Activity {
 
 	public static final int VIEW_SCHEDULE = 1;
 	public static final int APPOINT_SCHEDULE = 2;
     public static final int DIALOG_CONFIRM_APPOINTMENT = 1;
     
     public static final String EXTRA_SCHEDULE_TYPE = "scheduleType";
     public static final String EXTRA_SCHEDULE_DOCTOR = "scheduleDoctor";
 	public static final String EXTRA_SCHEDULE_PATIENT = "schedulePatient";
     
     private int doctorId = -1;
     private int patientId = -1;
     private final int searchBtnId = Menu.FIRST;
 
     private HashMap<String, ScheduleAdapter> days;
     private ArrayList<String> panelOrder;
         
     private String[] weekdays = { "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday" };
     
     private int selectedPanel = 0;
     private ScheduleButton selectedSchedule = null;
     private int scheduleType;
     
     private OnClickListener backButtonListener =    
 		new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 	        selectedPanel--;
 	        buildGrid();
 		}
 
     };   
     
     private OnClickListener nextButtonListener =    
 		new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 	        selectedPanel++;
 	        buildGrid();
 		}
 
     };  
     
     private OnClickListener scheduleButtonListener =    
 		new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			selectedSchedule = (ScheduleButton) v;
 			if (scheduleType == APPOINT_SCHEDULE)
 				showDialog(DIALOG_CONFIRM_APPOINTMENT);
 			else if (scheduleType == VIEW_SCHEDULE) {
 				//Intent patientIntent = new Intent(ScheduleActivity.this, PatientViewActivity.class);
 				Log.i("SCHEDULE", "Show appointment info: " + selectedSchedule.getAppointment().getDate().toString());
 			}
 		}
 
     };     
     
     private OnClickListener createScheduleListener =    
 		new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			Intent plannerIntent = new Intent(ScheduleActivity.this, PlannerActivity.class);
 			plannerIntent.putExtra(PlannerActivity.PLANNER_DOCTOR_ID, doctorId);
			startActivityForResult(plannerIntent,0);
 		}
 
     }; 
     
     @Override
 	  public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuItem searchMItm = menu.add(Menu.NONE,searchBtnId ,searchBtnId,"Logout");
 	    searchMItm.setIcon(R.drawable.logout);
 	    return super.onCreateOptionsMenu(menu);
 	  }
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    switch (item.getItemId()) {
 	        case searchBtnId:
 	        	LoginActivity.loginDoctor = null;
 	        	Intent intent = new Intent(this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 	        	finish();
 	        	startActivity(intent);
 	        break;
 	    }
 	    return true;
 	}
     
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.scheduleplanner);
         
         scheduleType = getIntent().getIntExtra(EXTRA_SCHEDULE_TYPE, VIEW_SCHEDULE);
         
         days = new HashMap<String, ScheduleAdapter>();
         panelOrder = new ArrayList<String>();
         
         Button backButton = (Button) findViewById(R.id.backScheduleButton);
 		Button nextButton = (Button) findViewById(R.id.nextScheduleButton);
 
 		backButton.setOnClickListener(backButtonListener);
 		nextButton.setOnClickListener(nextButtonListener);
 		
         buildTableLayout();
         
         
         //linear.addView(text);
 	}
 	
 	private void buildTableLayout() {
 		Button createSchedule = (Button) findViewById(R.id.createSchedule);
 
 		switch(scheduleType) {
 		case VIEW_SCHEDULE:
 			createSchedule.setVisibility(View.VISIBLE);
 			createSchedule.setOnClickListener(createScheduleListener);
 			buildAppoint();
 			buildGrid();
             break;
 		case APPOINT_SCHEDULE:
 			createSchedule.setVisibility(View.GONE);
 			buildAppoint();
 			buildGrid();
 			break;
 		default:
 			break;
 		}
 	}
 	
 	private void buildAppoint() {
 		doctorId = getIntent().getIntExtra(EXTRA_SCHEDULE_DOCTOR, 0);
 		patientId = getIntent().getIntExtra(EXTRA_SCHEDULE_PATIENT, 0);
 		
 		ArrayList<SchedulePlan> plans = DoctorOperations.getRemoteCurrentPlans(this, doctorId);
 		
 		if (plans == null) {
 			if (scheduleType == VIEW_SCHEDULE) {
 				plans = SchedulePlanOperations.querySchedulePlans(this, "doctor_id = ?", new String[] {"" + doctorId}, null);
 			}
 			else {
 				Toast.makeText(this, "This doctor has no Schedule Plan", Toast.LENGTH_LONG).show();
 				this.finish();
 				return;
 			}
 		}
 		
 		Date lastStartDate = null;
 		
 		for (int a=0; a < plans.size(); a++) {
 			
 			SchedulePlan plan = plans.get(a);
 		
 			ArrayList<Schedule> schedules = ScheduleOperations.getRemoteSchedules(this, plan.getId());
 			boolean connected = true;
 			if (schedules == null) {
 				schedules = ScheduleOperations.querySchedules(this, "schedule_plan_id = ?", new String[] {"" + plan.getId()}, null);
 				connected = false;
 			}
 			
 			ArrayList<Appointment> appointments = null;
 			if (connected)
 				appointments = AppointmentOperations.getRemoteServerAllAppointment(this, DoctorOperations.DOCTOR_CONTROLER, doctorId);
 			else
 				appointments = new ArrayList<Appointment>();
 			
 			HashMap<Integer, ArrayList<Appointment>> scheduleAppointments = new HashMap<Integer, ArrayList<Appointment>>();
 	
 			for (int i=0; i < appointments.size(); i++) {
 				Appointment appointment = appointments.get(i);
 				int scheduleId = appointment.getScheduleId();
 				
 				if (scheduleAppointments.containsKey(scheduleId)) {
 					ArrayList<Appointment> apps = scheduleAppointments.get(scheduleId);
 					apps.add(appointment);
 				}
 				else {
 					ArrayList<Appointment> apps = new ArrayList<Appointment>();
 					apps.add(appointment);
 					scheduleAppointments.put(scheduleId, apps);
 				}
 			}
 			
 			for (int i=schedules.size()-1; i >= 0; i--) {
 				Schedule schedule = schedules.get(i);
 				Date startDate = schedule.getStartDate();	
 				Date endDate = schedule.getEndDate();
 				
 				ArrayList<Appointment> apps = scheduleAppointments.get(schedule.getId());
 				
 				if (apps == null) {
 					apps = new ArrayList<Appointment>();
 				}
 	
 				
 				long blocks = (endDate.getTime() - startDate.getTime()) / (1000*60*30);
 				
 				Calendar c1 = Calendar.getInstance();
 				c1.setTime(startDate);
 	
 				for (int j=0; j < blocks; j++) {
 					Appointment selectedApp = null;
 					for (int k=0; k < apps.size(); k++) {
 						Appointment app = apps.get(k);
 						long appBlock = blocks - ((endDate.getTime() - app.getDate().getTime()) / (1000*60*30));
 						if (appBlock == j) {
 							selectedApp = app;
 							break;
 						}
 					}
 					
 					if (lastStartDate == null || c1.getTime().getTime() < lastStartDate.getTime())
 					{
 						
 						int id = c1.get(Calendar.HOUR_OF_DAY) * 2 + c1.get(Calendar.MINUTE)/30;
 						ScheduleButton button = null;
 						if (selectedApp == null) {
 							button = new ScheduleButton(this, id, schedule.getId(), (Date) c1.getTime().clone());
 							
 							if (scheduleType == APPOINT_SCHEDULE)
 								button.setOnClickListener(scheduleButtonListener);
 						}
 						else {
 							button = new ScheduleButton(this, id, schedule.getId(), (Date) c1.getTime().clone(), selectedApp);
 							if (scheduleType == VIEW_SCHEDULE)
 								button.setOnClickListener(scheduleButtonListener);
 						}
 						String label = weekdays[c1.get(Calendar.DAY_OF_WEEK) - 1] + " - " + c1.get(Calendar.DAY_OF_MONTH) + "/" + (c1.get(Calendar.MONTH) + 1);
 						
 						if (!panelOrder.contains(label))
 						{
 							panelOrder.add(0, label);
 						}
 						
 						if (days.containsKey(label)) {
 							ScheduleAdapter adapter = days.get(label);
 							adapter.addSchedule(j, button);
 						}
 						else {
 							ScheduleAdapter adapter = new ScheduleAdapter();
 							adapter.addSchedule(button);
 							days.put(label, adapter);
 						}
 					}
 					c1.add(Calendar.MINUTE, 30);	
 				}
 			}	
 			
 			lastStartDate = plan.getStartDate();
 		}
 	}
 	
 	private void buildGrid() {
 		if (panelOrder.isEmpty())
 			return;
 		String label = panelOrder.get(selectedPanel);
 		ScheduleAdapter adapter = days.get(label);
 		GridView scheduleTable = (GridView)findViewById(R.id.scheduleTable);
 		scheduleTable.setAdapter(adapter);
 		
 		TextView scheduleLabel = (TextView) findViewById(R.id.scheduleLabel);
 		scheduleLabel.setText(label);
 		
 		Button backButton = (Button) findViewById(R.id.backScheduleButton);
 		Button nextButton = (Button) findViewById(R.id.nextScheduleButton);
 
 		if (selectedPanel == 0) {
 			backButton.setVisibility(View.INVISIBLE);
 		}
 		else {
 			backButton.setVisibility(View.VISIBLE);
 		}
 		
 		if (selectedPanel >= panelOrder.size() - 1) {
 			nextButton.setVisibility(View.INVISIBLE);
 		}
 		else {
 			nextButton.setVisibility(View.VISIBLE);
 		}		
 	}
 	
 	@Override
 	protected Dialog onCreateDialog(int id) {
 	    Dialog dialog = null;
 	    switch(id) {
 	    case DIALOG_CONFIRM_APPOINTMENT:
 	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
 	    	builder.setMessage("You are scheduling an appointment at " + selectedSchedule.getDate().toString() + ". Schedule this appointment?")
 	    	       .setCancelable(false)
 	    	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 	    	           public void onClick(DialogInterface dialog, int id) {
 	    	                scheduleAppointment();
 	    	           }
 	    	       })
 	    	       .setNegativeButton("No", new DialogInterface.OnClickListener() {
 	    	           public void onClick(DialogInterface dialog, int id) {
 	    	                dialog.cancel();
 	    	           }
 	    	       });
 	    	dialog = builder.create();
 	    	break;
 	    default:
 	        break;
 	    }
 	    return dialog;
 	}
 	
 	@Override
 	protected void onPrepareDialog (int id, Dialog dialog, Bundle args) {
 		AlertDialog alertDialog = (AlertDialog) dialog;
 		alertDialog.setMessage("You are scheduling an appointment at " + selectedSchedule.getDate().toString() + ". Schedule this appointment?");
 	}
 	
 	private void scheduleAppointment() {
 		Appointment appointment = new Appointment(-1, patientId, selectedSchedule.getScheduleId(), doctorId, selectedSchedule.getDate());
 		
 		AppointmentOperations.createAppointment(this, appointment, true);
 		
 		selectedSchedule.setAppointment(appointment);
 		selectedSchedule.toggleState();
 		selectedSchedule.setOnClickListener(null);
 		
 	}
 	
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Button createSchedule = (Button) findViewById(R.id.createSchedule);
 
 		switch(scheduleType) {
 		case VIEW_SCHEDULE:
 			createSchedule.setVisibility(View.VISIBLE);
 			createSchedule.setOnClickListener(createScheduleListener);
 			buildAppoint();
 			buildGrid();
             break;
 		case APPOINT_SCHEDULE:
 			createSchedule.setVisibility(View.GONE);
 			buildAppoint();
 			buildGrid();
 			break;
 		default:
 			break;
 		}
 	}
 }
