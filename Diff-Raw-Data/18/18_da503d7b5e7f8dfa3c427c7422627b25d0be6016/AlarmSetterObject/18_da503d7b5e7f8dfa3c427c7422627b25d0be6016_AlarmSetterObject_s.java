 package com.MobiSeeker.PrescriptionWatcher.data;
 
 import java.util.Calendar;
import java.util.Date;
 import java.util.List;

import com.MobiSeeker.PrescriptionWatcher.activities.Prescription;
import com.MobiSeeker.PrescriptionWatcher.connection.ConnectionConstant;
 
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 
 public class AlarmSetterObject {
 	
 	
 	
 	public static void  setAlaram(Context context,Entry prescription_entry)
 	{
 		
 		if(prescription_entry.getPrescriptionType().equalsIgnoreCase(ConnectionConstant.PRESCRIPTION_WATCHER))
 		{
 			
 			
 		}else
 		{
 			
 		}
 		Calculator calculator=new Calculator();
 		Schedule schudle=calculator.getSchedule(prescription_entry.getMedicineName(), prescription_entry.getStartDate(), prescription_entry.getEndDate(),
 		prescription_entry.getStartTime(),prescription_entry.getEndTime(), prescription_entry.getDosage(), prescription_entry.getTimesPerDay(), prescription_entry.getComment());
 		List<Dosage> listofDosage=schudle.getPrescriptions();
 		for(int i=0;i<listofDosage.size();i++)
 		{
 			Dosage dosage=listofDosage.get(i);
 			Intent intent = new Intent(context, AlarmRecieverBroadCast.class);
 			intent.putExtra(ConnectionConstant.PRESCRIPTION_ENTRY,prescription_entry);
 			intent.setAction("packagename.ACTION");
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
 			Calendar calendar = Calendar.getInstance();
 			calendar.setTime(dosage.getTime());
 			System.out.println(calendar.toString());
 			System.out.println(System.currentTimeMillis()+"   "+calendar.getTimeInMillis() + (calendar.getTimeInMillis()-System.currentTimeMillis()));
 		    AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

 		}
 	}
 	
 }
