 package hu.schmidtsoft.timeboss.server;
 
 import hu.schmidtsoft.timeboss.standalone.TimeBossApplication;
 import hu.schmidtsoft.timeboss.tray.DisplayState;
 import hu.schmidtsoft.timeboss.util.UtilEvent;
 import hu.schmidtsoft.timeboss.util.UtilTime;
 
 import java.io.IOException;
 
 public abstract class AbstractTimeBossServer implements ITimeBossServer {
 	private long lastAlertTime=-1;
 	UtilEvent setTitleEvent;
 
 	public AbstractTimeBossServer(UtilEvent setTitleEvent) {
 		this.setTitleEvent=setTitleEvent;
 	}
 
 	@Override
 	public synchronized long getLastAlertTime() {
 		return lastAlertTime;
 	}
 
 	@Override
 	public synchronized void alertOpened() {
 		lastAlertTime=System.currentTimeMillis();
 	}
	protected void setLastRealActivity(Activity activity)
	{
		lastRealActivity=activity;
	}
 	protected void setLastActivity(Activity activity)
 	{
 		lastActivity=activity;
 		lastAlertTime=-1;
 		if(activity.getWork())
 		{
 			lastRealActivity=activity;
 		}
 		String finishTime=""+UtilTime.timeToString(
 				activity.getStartTime()<0?-1:
 				(activity.getStartTime()+
 				UtilTime.minToMilli(activity.getLength())));
 		DisplayState state;
 
 		if(activity.getWork())
 		{
 			state=new DisplayState(
 							DisplayState.State.work,
 					TimeBossApplication.applicatonName+" - "+activity.getTitle()+
 					(
 					activity.getProject().length()>0?(" - "+activity.getProject()):""
 							)+" UNTIL "+finishTime
 					);
 		}else
 		{
 			state=	new DisplayState(DisplayState.State.rest,
 					TimeBossApplication.applicatonName+" - REST UNTIL "+finishTime);
 		}
 		setTitleEvent.eventHappened(state);
 	}
 	@Override
 	public synchronized void saveActivity(Activity activity) throws IOException {
 		doSaveActivity(activity);
 		setLastActivity(activity);
 	}
 	abstract protected void doSaveActivity(Activity activity) throws IOException;
 
 	Activity lastActivity=new Activity(false,
 			
 			"", "", "", "", -1, 1, 0);
 	Activity lastRealActivity=new Activity(false, "", "", "", "", -1, -1, 0);
 	@Override
 	public synchronized Activity getLastActivity() {
 		return lastActivity;
 	}
 	@Override
 	public synchronized Activity getLastRealActivity() {
 		return lastRealActivity;
 	}
 }
