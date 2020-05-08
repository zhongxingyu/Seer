 package core.alarm;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.GregorianCalendar;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 
 import db.Appointment;
 
 public class AlarmHandler implements Runnable{
 
 	private AlarmEventSupport aes;
 	private ArrayList<Alarm> alarms;
 	private ArrayList<Appointment> appointments;
 	
 	//constructor gets called when client-program loads. gets appointments from server,extract the alarms from them.
 	public AlarmHandler(ArrayList<Appointment> appointments){
 		addAppointments(appointments);
 		alarms = new ArrayList<Alarm>();
 		extractAlarms();
 		aes = new AlarmEventSupport(appointments);
 	}
 	private void extractAlarms() {
 		for (Appointment appointment : appointments) {
 			if(appointment.hasAlarm())
 				alarms.add(appointment.getAlarm());
 		}
 		Collections.sort(alarms,new alarmComparator());
 	}
 	//let client listen for alarm events
 	public void addAlarmEventListener(AlarmListener listener){
 		aes.addAlarmEventListener(listener);
 	}
 	//When someone makes an new/edits appointment
 	public void addAppointment(Appointment app){
 		if(app.hasAlarm()){
 			appointments.add(app);
 			alarms.add(app.getAlarm());
 			Collections.sort(alarms,new alarmComparator());
 		}
 		else
 			throw new IllegalArgumentException("This appointment does not have an alarm");
 	}
 	public void addAppointments(ArrayList<Appointment> appointmentList){
 		appointments = new ArrayList<Appointment>();
 		for (Appointment appointment : appointmentList) {
 			if(appointment.hasAlarm())
 				appointments.add(appointment);
 		}
 	}
 	//deletes an alarm
 	public void removeAppointment(Appointment app){
 		appointments.remove(app);
 		alarms.remove(app.getAlarm());
 		aes.removeAlarm(app.getAlarm());
 	}
 	@Override
 	public void run() {
 		// TODO here it checks for new alarms
 	while(!alarms.isEmpty()){
 		Alarm current = alarms.get(0);
 		long timeUntilNextAlarm = current.getMillis()-System.currentTimeMillis();
 		try {
 			Thread.sleep(timeUntilNextAlarm);
 		} catch (InterruptedException e) {
			System.out.println("Something unexpected woke the alarm-thread up");
 			e.printStackTrace();
 		}
 		soundAlarm(current);
 	}
 	}
 	private void soundAlarm(Alarm alarm){
 		aes.fireAlarmEvent(alarm);
 	}
 }
