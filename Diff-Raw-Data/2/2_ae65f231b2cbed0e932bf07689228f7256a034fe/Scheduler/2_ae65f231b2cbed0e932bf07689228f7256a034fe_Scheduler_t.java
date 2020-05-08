 package com.nexus.scheduler;
 
 import java.lang.reflect.Field;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.logging.Logger;
 
 import com.nexus.JSONPacket;
 import com.nexus.MySQLHelper;
 import com.nexus.client.NexusClientScheduler;
 import com.nexus.logging.NexusLogger;
 import com.nexus.users.User;
 
 
 public class Scheduler implements Runnable{
 
 	private Thread SchedulerThread;
 	private boolean Running = false;
 
 	public List<NexusClientScheduler> SubscribedUsers;
 	public List<Scheduled> Timeline;
 
 	private Logger Log = Logger.getLogger("Scheduler");
 
 	public Scheduler(){
 		this.SchedulerThread = new Thread(this);
 		this.SubscribedUsers = new ArrayList<NexusClientScheduler>();
 		this.Timeline = new ArrayList<Scheduled>();
 		this.Log.setParent(NexusLogger.getLogger());
 
 		this.CacheTimeline();
 	}
 
 	public void Start(){
 		this.Running = true;
 		this.SchedulerThread.start();
 		this.Log.info("Starting scheduler thread");
 	}
 
 	public void Stop(){
 		this.Running = false;
 		this.Log.info("Stopping scheduler thread");
 	}
 
 	public void run(){
 		while(Running){
 			try{
 				Date CurrentTime = new Date();
 				for(int i = 0; i < Timeline.size(); i++){
 					Scheduled Object = Timeline.get(i);
 					if(DateIsEarlier(Object.Endtime, CurrentTime)){
 						//Timeline.remove(i);
 						Object.SetIsRunned();
 						continue;
 					}
 					if(DateIs15SecAfter(Object.Starttime, CurrentTime)){
 						Object.BeforeStart();
 					}
 					if(DateIsLater(Object.Starttime, CurrentTime)){
 						continue;
 					}
 					if(DatesAreEqual(Object.Starttime, CurrentTime)){
 						Object.Start();
 					}else if(DateIs15SecAfter(Object.Endtime, CurrentTime)){
 						Object.BeforeEnd();
 					}else if(DatesAreEqual(Object.Endtime, CurrentTime)){
 						Object.End();
 					}else if(DateIsEarlier(Object.Starttime, CurrentTime) && DateIsLater(Object.Endtime, CurrentTime)){
 						Object.Tick();
 					}
 				}
 			}catch(Exception e){
 
 			}
 			try{
 				Thread.sleep(998);
 			}catch(Exception e){}
 		}
 	}
 
 	private boolean DateIs15SecAfter(Date date1, Date date2){
 		Calendar cal1 = Calendar.getInstance();
 		Calendar cal2 = Calendar.getInstance();
 	    cal1.setTime(date1);
 	    cal2.setTime(date2);
 	    cal2.add(Calendar.SECOND, 15);
 	    cal1.set(Calendar.MILLISECOND, 0);
 	    cal2.set(Calendar.MILLISECOND, 0);
 	    return cal1.equals(cal2);
 	}
 
 	private boolean DatesAreEqual(Date date1, Date date2){
 		Calendar cal1 = Calendar.getInstance();
 		Calendar cal2 = Calendar.getInstance();
 	    cal1.setTime(date1);
 	    cal2.setTime(date2);
 	    cal1.set(Calendar.MILLISECOND, 0);
 	    cal2.set(Calendar.MILLISECOND, 0);
 	    return cal1.equals(cal2);
 	}
 
 	private boolean DateIsEarlier(Date date1, Date date2){
 		Calendar cal1 = Calendar.getInstance();
 		Calendar cal2 = Calendar.getInstance();
 	    cal1.setTime(date1);
 	    cal2.setTime(date2);
 	    cal1.set(Calendar.MILLISECOND, 0);
 	    cal2.set(Calendar.MILLISECOND, 0);
 	    return cal1.before(cal2);
 	}
 
 	private boolean DateIsLater(Date date1, Date date2){
 		Calendar cal1 = Calendar.getInstance();
 		Calendar cal2 = Calendar.getInstance();
 	    cal1.setTime(date1);
 	    cal2.setTime(date2);
 	    cal1.set(Calendar.MILLISECOND, 0);
 	    cal2.set(Calendar.MILLISECOND, 0);
 	    return cal1.after(cal2);
 	}
 
 	public void CacheTimeline(){
 		Log.info("Creating local copy of the scheduler timeline");
 		try {
 			this.Timeline.clear();
 		    Connection conn = MySQLHelper.GetConnection();
 		    Statement stmt = conn.createStatement();
 		    //ResultSet rs = stmt.executeQuery("SELECT * FROM schedulerTimeline WHERE ID >= (SELECT ID FROM schedulerTimeline WHERE Type = 0 AND Starttime < NOW() ORDER BY ID DESC LIMIT 1) OR Endtime > NOW()");
 		    ResultSet rs = stmt.executeQuery("SELECT * FROM schedulerTimeline");
 		    while(rs.next()){
 		    	this.Timeline.add(Scheduled.FromResultSet(rs));
 		    }
 		    rs.close();
 		    stmt.close();
 		    conn.close();
 		}catch(SQLException e){
 		    e.printStackTrace();
 		}
 	}
 
 	public List<Scheduled> GetAllAirtimesForUser(User u){
 		List<Scheduled> list = new ArrayList<Scheduled>();
 		for(int i = 0; i < this.Timeline.size(); i++){
 			for(int j = 0; j < this.Timeline.get(i).Broadcasters.size(); j++){
 				if(this.Timeline.get(i).Broadcasters.get(j).Username.equalsIgnoreCase(u.Username)){
 					list.add(this.Timeline.get(i));
 				}
 			}
 		}
 		return list;
 	}
 
 	public List<Scheduled> GetAllAirtimes(){
 		return this.Timeline;
 	}
 
 	public List<Scheduled> GetAirtimesForUserNextWeek(User u){
 		List<Scheduled> list = this.GetAllAirtimesForUser(u);
 		List<Scheduled> newList = new ArrayList<Scheduled>();
 		Date Week = AddDaysToDate(new Date(), 7);
 		for(Scheduled s : list){
			if(s.Starttime.before(Week) && s.Type != EnumScheduledEventType.Header){
 				newList.add(s);
 			}
 		}
 		return newList;
 	}
 
 	public List<Scheduled> GetAllAirtimesNextWeek(){
 		List<Scheduled> list = this.GetAllAirtimes();
 		List<Scheduled> newList = new ArrayList<Scheduled>();
 		Date Week = AddDaysToDate(new Date(), 7);
 		for(Scheduled s : list){
 			if(s.Starttime.before(Week)){
 				newList.add(s);
 			}
 		}
 		return newList;
 	}
 
 	private Date AddDaysToDate(Date date, int noOfDays) {
 	    Date newDate = new Date(date.getTime());
 
 	    GregorianCalendar calendar = new GregorianCalendar();
 	    calendar.setTime(newDate);
 	    calendar.add(Calendar.DATE, noOfDays);
 	    newDate.setTime(calendar.getTime().getTime());
 
 	    return newDate;
 	}
 
 	private Date AddSecondsToDate(Date date, long seconds) {
 	    Date newDate = new Date(date.getTime());
 
 	    GregorianCalendar calendar = new GregorianCalendar();
 	    calendar.setTime(newDate);
 	    calendar.add(Calendar.SECOND, (int) seconds);
 	    newDate.setTime(calendar.getTime().getTime());
 
 	    return newDate;
 	}
 
 	public void HandleMetadataChange(SchedulerMetadataUpdateArray Updates){
 		this.SynchroniseMetadataChange(Updates);
 		try{
 			for(SchedulerMetadataUpdate Update : Updates.Updates){
 				Scheduled object = Scheduled.FromOID(Update.OID);
 				if(Update.Field.equalsIgnoreCase("ThrustartType")){
 					Update.Data = EnumThrustartType.FromID(Integer.valueOf(Update.Data.toString()));
 				}else if(Update.Field.equalsIgnoreCase("Type")){
 					Update.Data = EnumScheduledEventType.FromID(Integer.valueOf(Update.Data.toString()));
 				}else if(Update.Field.equalsIgnoreCase("Starttime")){
 					if(object.ThrustartType == EnumThrustartType.Time){
 						Update.Data = ParseTimestamp(Update.Data);
 					}
 				}else if(Update.Field.equalsIgnoreCase("Endtime")){
 					if(object.ThrustartType == EnumThrustartType.Time){
 						Update.Data = ParseTimestamp(Update.Data);
 					}
 				}else if(Update.Field.equalsIgnoreCase("Duration")){
 					Update.Data = new Double(Update.Data.toString()).longValue();
 				}
 				try{
 					Field UpdatedField = object.getClass().getDeclaredField(Update.Field);
 					UpdatedField.set(object, Update.Data);
 					object.SaveChanges();
 
 					if(Update.Field.equalsIgnoreCase("Starttime")){
 						if(object.ThrustartType == EnumThrustartType.Time){
 							SchedulerMetadataUpdateArray ASyncUpdates = new SchedulerMetadataUpdateArray();
 
 							object.Endtime = AddSecondsToDate(object.Starttime, object.Duration);
 							SchedulerMetadataUpdate EndtimeUpdate = new SchedulerMetadataUpdate();
 							EndtimeUpdate.OID = object.ID;
 							EndtimeUpdate.Field = "Endtime";
 							EndtimeUpdate.Data = object.Endtime;
 							ASyncUpdates.Updates.add(EndtimeUpdate);
 							object.SaveChanges();
 
 							for(int i = 0; i < Timeline.size(); i++){
 								if(i == 0) continue;
 								if(Timeline.get(i - 1).Type == EnumScheduledEventType.Header && Timeline.get(i).ID == object.ID){
 									Timeline.get(i - 1).Starttime = object.Starttime;
 									Timeline.get(i - 1).Endtime = object.Starttime;
 									Timeline.get(i - 1).SaveChanges();
 
 									SchedulerMetadataUpdate HeaderStarttimeUpdate = new SchedulerMetadataUpdate();
 									SchedulerMetadataUpdate HeaderEndtimeUpdate = new SchedulerMetadataUpdate();
 									HeaderStarttimeUpdate.OID = Timeline.get(i - 1).ID;
 									HeaderStarttimeUpdate.Field = "Starttime";
 									HeaderStarttimeUpdate.Data = Timeline.get(i - 1).Starttime;
 									HeaderEndtimeUpdate.OID = Timeline.get(i - 1).ID;
 									HeaderEndtimeUpdate.Field = "Endtime";
 									HeaderEndtimeUpdate.Data = Timeline.get(i - 1).Endtime;
 									ASyncUpdates.Updates.add(HeaderStarttimeUpdate);
 									ASyncUpdates.Updates.add(HeaderEndtimeUpdate);
 								}
 							}
 							this.SynchroniseMetadataChange(ASyncUpdates);
 						}
 					}else if(Update.Field.equalsIgnoreCase("Endtime")){
 						if(object.ThrustartType == EnumThrustartType.Time){
 							SchedulerMetadataUpdateArray ASyncUpdates = new SchedulerMetadataUpdateArray();
 
 							object.Duration = (int) (object.Endtime.getTime() - object.Starttime.getTime()) / 1000;
 							SchedulerMetadataUpdate DurationUpdate = new SchedulerMetadataUpdate();
 							DurationUpdate.OID = object.ID;
 							DurationUpdate.Field = "Duration";
 							DurationUpdate.Data = object.Duration;
 							ASyncUpdates.Updates.add(DurationUpdate);
 							object.SaveChanges();
 
 							boolean Read = false;
 							for(int i = 0; i < Timeline.size(); i++){
 								if(Read && Timeline.get(i).ThrustartType == EnumThrustartType.Time){
 									Read = false;
 								}
 								if(Read){
 									SchedulerMetadataUpdate StartUpdate = new SchedulerMetadataUpdate();
 									SchedulerMetadataUpdate EndUpdate = new SchedulerMetadataUpdate();
 
 									Timeline.get(i).Starttime = Timeline.get(i - 1).Endtime;
 									Timeline.get(i).Endtime = AddSecondsToDate(Timeline.get(i).Starttime, Timeline.get(i).Duration);
 									Timeline.get(i).SaveChanges();
 
 									StartUpdate.OID = Timeline.get(i).ID;
 									StartUpdate.Field = "Starttime";
 									StartUpdate.Data = Timeline.get(i).Starttime;
 									EndUpdate.OID = Timeline.get(i).ID;
 									EndUpdate.Field = "Endtime";
 									EndUpdate.Data = Timeline.get(i).Endtime;
 									ASyncUpdates.Updates.add(StartUpdate);
 									ASyncUpdates.Updates.add(EndUpdate);
 								}
 								if(Timeline.get(i).ID == object.ID){
 									Read = true;
 								}
 							}
 							this.SynchroniseMetadataChange(ASyncUpdates);
 						}
 					}else if(Update.Field.equalsIgnoreCase("Duration")){
 						if(object.ThrustartType == EnumThrustartType.Time){
 							SchedulerMetadataUpdateArray ASyncUpdates = new SchedulerMetadataUpdateArray();
 
 							object.Endtime = AddSecondsToDate(object.Starttime, object.Duration);
 							SchedulerMetadataUpdate EndtimeUpdate = new SchedulerMetadataUpdate();
 							EndtimeUpdate.OID = object.ID;
 							EndtimeUpdate.Field = "Endtime";
 							EndtimeUpdate.Data = object.Endtime;
 							ASyncUpdates.Updates.add(EndtimeUpdate);
 							object.SaveChanges();
 
 							boolean Read = false;
 							for(int i = 0; i < Timeline.size(); i++){
 								if(Read && Timeline.get(i).ThrustartType == EnumThrustartType.Time){
 									Read = false;
 								}
 								if(Read){
 									SchedulerMetadataUpdate StartUpdate = new SchedulerMetadataUpdate();
 									SchedulerMetadataUpdate EndUpdate = new SchedulerMetadataUpdate();
 
 									Timeline.get(i).Starttime = Timeline.get(i - 1).Endtime;
 									Timeline.get(i).Endtime = AddSecondsToDate(Timeline.get(i).Starttime, Timeline.get(i).Duration);
 									Timeline.get(i).SaveChanges();
 
 									StartUpdate.OID = Timeline.get(i).ID;
 									StartUpdate.Field = "Starttime";
 									StartUpdate.Data = Timeline.get(i).Starttime;
 									EndUpdate.OID = Timeline.get(i).ID;
 									EndUpdate.Field = "Endtime";
 									EndUpdate.Data = Timeline.get(i).Endtime;
 									ASyncUpdates.Updates.add(StartUpdate);
 									ASyncUpdates.Updates.add(EndUpdate);
 								}
 								if(Timeline.get(i).ID == object.ID){
 									Read = true;
 								}
 							}
 							this.SynchroniseMetadataChange(ASyncUpdates);
 						}
 					}
 				}catch(Exception e){
 					this.Log.warning("Metadata update error:");
 					e.printStackTrace();
 				}
 			}
 		}catch(Exception e){
 			this.Log.warning("Metadata update error:");
 			e.printStackTrace();
 		}
 	}
 
 	public Timestamp ParseTimestamp(Object input){
 		Double obj = new Double(input.toString());
 		return new Timestamp(obj.longValue());
 	}
 
 	public void SynchroniseMetadataChange(SchedulerMetadataUpdateArray Updates){
 		JSONPacket Packet = new JSONPacket();
 		Packet.put("SynchronisationUpdates", Updates.Updates);
 		for(int i = 0; i < this.SubscribedUsers.size(); i++){
 			this.SubscribedUsers.get(i).SendQueue.addToSendQueue(Packet);
 		}
 	}
 
 	public void Subscribe(NexusClientScheduler Client){
 		this.SubscribedUsers.add(Client);
 	}
 
 	public void Unsubscribe(NexusClientScheduler Client){
 		this.SubscribedUsers.remove(Client);
 	}
 }
