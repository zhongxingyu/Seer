 package in.ccl.ui;
 
 import in.ccl.adapters.ScheduleAdapter;
 import in.ccl.helper.Util;
 import in.ccl.model.ScheduleItem;
 
 import java.util.ArrayList;
 
 import android.graphics.Color;
 import android.os.Bundle;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class ScheduleActivity extends TopActivity {
 
 	private ListView listSchedule;
 
 	private ScheduleAdapter scheduleAdapter;
 	
 	private ScheduleItem scheduleItem;
 
 	private ArrayList <ScheduleItem> scheduleList = new ArrayList <ScheduleItem>();
 
 
 
 	@Override
 	public void onCreate (Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		addContent(R.layout.shedule_layout);
   	TextView scheduleTitle =  (TextView)findViewById(R.id.schedule_title);
 		Util.setTextFont(this, scheduleTitle);
 		
 		listSchedule = (ListView) findViewById(R.id.list_schedule);
		TextView schedule_title = (TextView)findViewById(R.id.schedule_title);
	/*	Util.setTextFont(this, schedule_title);
		schedule_title.setText(getResources().getString(R.string.schedule));*/
 		scheduleItem = new ScheduleItem();
 		scheduleItem.setDate("09 FEB");
 		scheduleItem.setDay("SATURDAY");
 		scheduleItem.setPalce("KOCHI");
 		scheduleItem.setFirstMatchTeamOne("Chennai Rhinos".toUpperCase());
 		scheduleItem.setFirstMatchTeamTwo("Bhojpuri Dabangs".toUpperCase());
 		scheduleItem.setFirstMatchTime("3:00pm - 7:00pm".toUpperCase());
 		scheduleItem.setSecondMatchTeamOne("Kerala Strikers".toUpperCase());
 		scheduleItem.setSecondMatchTeamTwo("Mumbai Heroes".toUpperCase());
 		scheduleItem.setSecondMatchTime("7:00pm - 11:00pm".toUpperCase());
 		scheduleItem.setThirdMatchTeamOne("");
 		scheduleItem.setThirdMatchTeamTwo("");
 		scheduleItem.setThirdMatchTime("");		
 		scheduleList.add(scheduleItem);
 		
 		scheduleItem = new ScheduleItem();
 		scheduleItem.setDate("10 FEB");
 		scheduleItem.setDay("SUNDAY");
 		scheduleItem.setPalce("VIZAG");
 		scheduleItem.setFirstMatchTeamOne("Veer Marathi".toUpperCase());
 		scheduleItem.setFirstMatchTeamTwo("Bengal Tigers".toUpperCase());
 		scheduleItem.setFirstMatchTime("3:00pm - 7:00pm".toUpperCase());
 		scheduleItem.setSecondMatchTeamOne("Telugu Warriors".toUpperCase());
 		scheduleItem.setSecondMatchTeamTwo("Karnataka Bulldozers".toUpperCase());
 		scheduleItem.setSecondMatchTime("7:00pm - 11:00pm".toUpperCase());
 		scheduleItem.setThirdMatchTeamOne("");
 		scheduleItem.setThirdMatchTeamTwo("");
 		scheduleItem.setThirdMatchTime("");		
 		scheduleList.add(scheduleItem);
 		
 		scheduleItem = new ScheduleItem();
 		scheduleItem.setDate("16 FEB");
 		scheduleItem.setDay("SATURDAY".toUpperCase());
 		scheduleItem.setPalce("Bengaluru".toUpperCase());
 		scheduleItem.setFirstMatchTeamOne("Telugu Warriors".toUpperCase());
 		scheduleItem.setFirstMatchTeamTwo("Bhojpuri Dabanggs".toUpperCase());
 		scheduleItem.setFirstMatchTime("3:00pm - 7:00pm".toUpperCase());
 		scheduleItem.setSecondMatchTeamOne("Chennai Rhinos".toUpperCase());
 		scheduleItem.setSecondMatchTeamTwo("Karnataka Bulldozers".toUpperCase());
 		scheduleItem.setSecondMatchTime("7:00pm - 11:00pm".toUpperCase());
 		scheduleItem.setThirdMatchTeamOne("");
 		scheduleItem.setThirdMatchTeamTwo("");
 		scheduleItem.setThirdMatchTime("");		
 		scheduleList.add(scheduleItem);
 		
 		scheduleItem = new ScheduleItem();
 		scheduleItem.setDate("17 FEB");
 		scheduleItem.setDay("Sunday".toUpperCase());
 		scheduleItem.setPalce("Pune".toUpperCase());
 		scheduleItem.setFirstMatchTeamOne("Kerala Strikers".toUpperCase());
 		scheduleItem.setFirstMatchTeamTwo("Bengal Tigers".toUpperCase());
 		scheduleItem.setFirstMatchTime("3:00pm - 7:00pm".toUpperCase());
 		scheduleItem.setSecondMatchTeamOne("Veer Marathi".toUpperCase());
 		scheduleItem.setSecondMatchTeamTwo("Mumbai Heroes".toUpperCase());
 		scheduleItem.setSecondMatchTime("7:00pm - 11:00pm".toUpperCase());
 		scheduleItem.setThirdMatchTeamOne("");
 		scheduleItem.setThirdMatchTeamTwo("");
 		scheduleItem.setThirdMatchTime("");		
 		scheduleList.add(scheduleItem);
 		
 		scheduleItem = new ScheduleItem();
 		scheduleItem.setDate("23 FEB");
 		scheduleItem.setDay("Saturday".toUpperCase());
 		scheduleItem.setPalce("Sharjah".toUpperCase());
 		scheduleItem.setFirstMatchTeamOne("Chennai Rhinos".toUpperCase());
 		scheduleItem.setFirstMatchTeamTwo("Mumbai Heroes".toUpperCase());
 		scheduleItem.setFirstMatchTime("3:00pm - 7:00pm".toUpperCase());
 		scheduleItem.setSecondMatchTeamOne("Kerala Strikers".toUpperCase());
 		scheduleItem.setSecondMatchTeamTwo("Bhojpuri Dabanggs".toUpperCase());
 		scheduleItem.setSecondMatchTime("7:00pm - 11:00pm".toUpperCase());
 		scheduleItem.setThirdMatchTeamOne("");
 		scheduleItem.setThirdMatchTeamTwo("");
 		scheduleItem.setThirdMatchTime("");		
 		scheduleList.add(scheduleItem);
 		
 		scheduleItem = new ScheduleItem();
 		scheduleItem.setDate("24 FEB");
 		scheduleItem.setDay("Sunday".toUpperCase());
 		scheduleItem.setPalce("Kolkata".toUpperCase());
 		scheduleItem.setFirstMatchTeamOne("Veer Marathi".toUpperCase());
 		scheduleItem.setFirstMatchTeamTwo("Karnataka Bulldozers".toUpperCase());
 		scheduleItem.setFirstMatchTime("3:00pm - 7:00pm".toUpperCase());
 		scheduleItem.setSecondMatchTeamOne("Telugu Warriors".toUpperCase());
 		scheduleItem.setSecondMatchTeamTwo("Bengal Tigers".toUpperCase());
 		scheduleItem.setSecondMatchTime("7:00pm - 11:00pm".toUpperCase());
 		scheduleItem.setThirdMatchTeamOne("");
 		scheduleItem.setThirdMatchTeamTwo("");
 		scheduleItem.setThirdMatchTime("");		
 		scheduleList.add(scheduleItem);
 		
 		scheduleItem = new ScheduleItem();
 		scheduleItem.setDate("2 MAR");
 		scheduleItem.setDay("Saturday".toUpperCase());
 		scheduleItem.setPalce("Ranchi".toUpperCase());
 		scheduleItem.setFirstMatchTeamOne("Kerala Strikers".toUpperCase());
 		scheduleItem.setFirstMatchTeamTwo("Karnataka Bulldozers".toUpperCase());
 		scheduleItem.setFirstMatchTime("3:00pm - 7:00pm".toUpperCase());
 		scheduleItem.setSecondMatchTeamOne("Veer Marathi".toUpperCase());
 		scheduleItem.setSecondMatchTeamTwo("Bhojpuri Dabanggs".toUpperCase());
 		scheduleItem.setSecondMatchTime("7:00pm - 11:00pm".toUpperCase());
 		scheduleItem.setThirdMatchTeamOne("");
 		scheduleItem.setThirdMatchTeamTwo("");
 		scheduleItem.setThirdMatchTime("");		
 		scheduleList.add(scheduleItem);
 		
 		scheduleItem = new ScheduleItem();
 		scheduleItem.setDate("3 MAR");
 		scheduleItem.setDay("Sunday".toUpperCase());
 		scheduleItem.setPalce("Hyderabad".toUpperCase());
 		scheduleItem.setFirstMatchTeamOne("Chennai Rhinos".toUpperCase());
 		scheduleItem.setFirstMatchTeamTwo("Bengal Tigers".toUpperCase());
 		scheduleItem.setFirstMatchTime("3:00pm - 7:00pm".toUpperCase());
 		scheduleItem.setSecondMatchTeamOne("Telugu warriors".toUpperCase());
 		scheduleItem.setSecondMatchTeamTwo("Mumbai Heroes".toUpperCase());
 		scheduleItem.setSecondMatchTime("7:00pm - 11:00pm".toUpperCase());
 		scheduleItem.setThirdMatchTeamOne("");
 		scheduleItem.setThirdMatchTeamTwo("");
 		scheduleItem.setThirdMatchTime("");		
 		scheduleList.add(scheduleItem);
 		
 		scheduleItem = new ScheduleItem();
 		scheduleItem.setDate("9 MAR");
 		scheduleItem.setDay("Saturday".toUpperCase());
 		scheduleItem.setPalce("Hyderabad".toUpperCase());
 		scheduleItem.setFirstMatchTeamOne("Semi Final 1".toUpperCase());
 		scheduleItem.setFirstMatchTeamTwo("");
 		scheduleItem.setFirstMatchTime("3:00pm - 7:00pm".toUpperCase());
 		scheduleItem.setSecondMatchTeamOne("Semi Final 2".toUpperCase());
 		scheduleItem.setSecondMatchTeamTwo("");
 		scheduleItem.setSecondMatchTime("7:00pm - 11:00pm".toUpperCase());
 		scheduleItem.setThirdMatchTeamOne("");
 		scheduleItem.setThirdMatchTeamTwo("");
 		scheduleItem.setThirdMatchTime("");		
 		scheduleList.add(scheduleItem);
 		
 		scheduleItem = new ScheduleItem();
 		scheduleItem.setDate("10 MAR");
 		scheduleItem.setDay("Sunday".toUpperCase());
 		scheduleItem.setPalce("Bengaluru".toUpperCase());
 		scheduleItem.setFirstMatchTeamOne("Final".toUpperCase());
 		scheduleItem.setFirstMatchTeamTwo("");
 		scheduleItem.setFirstMatchTime("7:00pm - 11:00pm".toUpperCase());
 		scheduleItem.setSecondMatchTeamOne("");
 		scheduleItem.setSecondMatchTeamTwo("");
 		scheduleItem.setSecondMatchTime("");
 		scheduleItem.setThirdMatchTeamOne("");
 		scheduleItem.setThirdMatchTeamTwo("");
 		scheduleItem.setThirdMatchTime("");		
 		scheduleList.add(scheduleItem);
 		
 		scheduleAdapter = new ScheduleAdapter(ScheduleActivity.this, scheduleList);
 		listSchedule.setAdapter(scheduleAdapter);
 		listSchedule.setFadingEdgeLength(1);
 		listSchedule.setCacheColorHint(Color.TRANSPARENT);
 
 	}
 
 }
