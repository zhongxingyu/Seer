 package fi.cie.chiru.servicefusionar.calendar;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import commands.Command;
 
 import util.IO;
 import util.Vec;
 
 import fi.cie.chiru.servicefusionar.R;
 import fi.cie.chiru.servicefusionar.serviceApi.ServiceManager;
 import gl.GLFactory;
 import gl.scenegraph.MeshComponent;
 import android.content.Context;
 import android.os.Handler;
 import android.os.Looper;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.GridLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.GridLayout.Spec;
 
 public class ServiceFusionCalendar 
 {
 	private static final String LOG_TAG = "ServiceFusionCalendar";
 	private ServiceManager serviceManager;
 	private String eventsFile = "calendar_events.txt";
 	private List<CalendarEvent> events;
 	int currentdayOfMonth;
 	private MeshComponent calendar;
 	private MeshComponent eventsToday;
 	private int curCol, curRow;
	private float dayGridWidth = 1.7f;
	private float dayGridHeight = 1.4f;
 	private boolean eventsVisible;
	private float camOffsetX = 12.0f;
 	private float camOffsetY = 6.0f;
 	private float camOffsetZ = -40.0f;
	private float eventsOffsetX = 10.0f;
	private float eventsOffsetY = 6.1f;
     
     private String months[] =  { "TAMMIKUU", "HELMIKUU", "MAALISKUU",
                                  "HUHTIKUU", "TOUKOKUU", "KESÄKUU",
                                  "HEINÄKUU", "ELOKUU", "SYYSKUU",
                                  "LOKAKUU", "MARRASKUU", "JOULUKUU" };
 	
 	public ServiceFusionCalendar(ServiceManager serviceManager)
 	{
 		events = new ArrayList<CalendarEvent>();
 	    this.serviceManager = serviceManager;
 	    eventsVisible = false;
     	currentdayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
 //    	initEventsList();
 	    createCalendar();
 	}
     
     private void createCalendar()
     {
     	Handler mHandler = new Handler(Looper.getMainLooper());
 		mHandler.post(new Runnable() {
 
 			@Override
 			public void run() 
 			{
 				LayoutInflater li = (LayoutInflater) serviceManager.getSetup().myTargetActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
 				View v = li.inflate(R.layout.calendar2, null); 
 
 				GridLayout gl = (GridLayout)v.findViewById(R.id.kalenteri_gridlayout);
 				populateCalendar(gl);
 				
 				TextView month = (TextView)v.findViewById(R.id.kuukausi);
				month.setText(months[Calendar.MONTH]);
 				
 				TextView year = (TextView)v.findViewById(R.id.vuosi);
 				year.setText(""+Calendar.getInstance().get(Calendar.YEAR));
 				
 				RelativeLayout root = (RelativeLayout) serviceManager.getSetup().getGuiSetup().getMainContainerView();
 					
 				root.addView(v);
 				v.setVisibility(View.GONE);
 				
 				calendar = GLFactory.getInstance().newTexturedSquare("calendar", IO.loadBitmapFromView(v, 35, 35));
 				calendar.setOnClickCommand(new FlipEventsVisibility());
 				
 				Vec camPos = serviceManager.getSetup().getCamera().getPosition();
 				serviceManager.getSetup().world.add(calendar);
 				calendar.setPosition(new Vec(camPos.x + camOffsetX, camPos.y + camOffsetY, camPos.z + camOffsetZ));
 				calendar.setRotation(new Vec(90.0f, 0.0f, 180.0f));
 				calendar.setScale(new Vec(12.0f, 1.0f, 10.0f));
 				
 				View eventv = createEvents(li);
 				root.addView(eventv);
 				eventv.setVisibility(View.GONE);
 				
                 eventsToday = GLFactory.getInstance().newTexturedSquare("eventsToday", IO.loadBitmapFromView(eventv, 35, 35));
     		    eventsToday.setPosition(new Vec(camPos.x + eventsOffsetX +((float)curCol)*dayGridWidth , camPos.y + eventsOffsetY-((float)curRow)*dayGridHeight, camPos.z + camOffsetZ));
     		    eventsToday.setRotation(new Vec(90.0f, 0.0f, 180.0f));
     		    eventsToday.setScale(new Vec(8.0f, 1.0f, 7.0f));
                 serviceManager.getSetup().camera.attachToCamera(calendar);
                 serviceManager.getSetup().camera.attachToCamera(eventsToday);
 			}
 		});
     }
   
     private void populateCalendar(GridLayout gl)
     {
     	Spec colSpec;
         Spec rowSpec;
 		GridLayout.LayoutParams p;
     	TextView tv;
    	GregorianCalendar gCal = new GregorianCalendar(2013, Calendar.MONTH, 1);
     	int currentDay = gCal.get(Calendar.DAY_OF_WEEK);
 
        gCal.add(Calendar.DAY_OF_YEAR, Calendar.SUNDAY - currentDay);
         
         for (int i = 0; i < gl.getRowCount()-1; i++)
         {
             for (int j = 0; j < gl.getColumnCount()-1; j++)
             {
             	colSpec = GridLayout.spec(j, 1);
 			    rowSpec = GridLayout.spec(i, 1);
                 p = new GridLayout.LayoutParams(rowSpec, colSpec);
                 p.setGravity(Gravity.START|Gravity.END);
                 //p.setMargins(0, 10, 0, 0);
             	tv = new TextView(serviceManager.getSetup().myTargetActivity);
             	tv.setTextSize(40);
             	tv.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
 
            	if(gCal.get(Calendar.DAY_OF_MONTH) == currentdayOfMonth)
             	{
                 	tv.setBackgroundResource(R.drawable.bordersblue);
                 	curCol = j;
                 	curRow = i;
             	}
             	else
                 	tv.setBackgroundResource(R.drawable.borders);
             		
             	tv.setText((""+ gCal.get(Calendar.DAY_OF_MONTH)));
             	gl.addView(tv, p);
 
                 gCal.add(Calendar.DAY_OF_YEAR, 1);
             }
         }
     }
     
     public View createEvents(LayoutInflater li)
     {
     	View v = li.inflate(R.layout.calendar_events, null); 
     			
     	TextView curDay = (TextView)v.findViewById(R.id.tänään);
 		curDay.setText(""+currentdayOfMonth);
 		
 		return v;
     	
     }
     
     public void addEvent(final CalendarEvent ce)
     {
     	Handler mHandler = new Handler(Looper.getMainLooper());
 		mHandler.post(new Runnable() {
 
 			@Override
 			public void run() 
 			{
 				LayoutInflater li = (LayoutInflater) serviceManager.getSetup().myTargetActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
 				View v = li.inflate(R.layout.calendar_events, null);
 				
 				TextView curDay = (TextView)v.findViewById(R.id.tänään);
 				curDay.setText(""+currentdayOfMonth);
     			
 		    	TextView newTime = (TextView)v.findViewById(R.id.aika_tapahtuma3);
 		    	newTime.setText(ce.getTime());
 		    	newTime.setVisibility(View.VISIBLE);
 		    	
 		    	TextView newEvent = (TextView)v.findViewById(R.id.tapahtuma3);
 		    	newEvent.setText(ce.getEvent());
 		    	newEvent.setVisibility(View.VISIBLE);
 		    	
 		    	RelativeLayout root = (RelativeLayout) serviceManager.getSetup().getGuiSetup().getMainContainerView();
 		    	root.addView(v);
 				v.setVisibility(View.GONE);
 		    	
 				Vec eventPos = eventsToday.getPosition();
 				Vec eventRot = calendar.getRotation();
 				
 				serviceManager.getSetup().camera.detachFromCamera(eventsToday);
 		    	serviceManager.getSetup().world.remove(eventsToday);
 		    	//eventsToday = null;
 		    	eventsToday = GLFactory.getInstance().newTexturedSquare("eventsTodayUpdated", IO.loadBitmapFromView(v, 35, 35));
 		    	serviceManager.getSetup().world.add(eventsToday);
 
 		    	eventsToday.setPosition(new Vec(eventPos.x, eventPos.y, eventPos.z));
     		    eventsToday.setRotation(new Vec(90.0f, eventRot.y, 180.0f));
     		    eventsToday.setScale(new Vec(8.0f, 1.0f, 7.0f));
     		    serviceManager.getSetup().camera.attachToCamera(eventsToday);
 		    	showEvents(true);
 			}
 		});
     }
     
     public void showEvents(boolean visible)
     {
     	
     	if(visible)
     	{
     		//calendar.addChild(eventsToday);
 //    		Vec camPos = serviceManager.getSetup().getCamera().getPosition();
     	    serviceManager.getSetup().world.add(eventsToday);
 //		    eventsToday.setPosition(new Vec(camPos.x + eventsOffsetX +((float)curCol)*dayGridWidth , camPos.y + eventsOffsetY-((float)curRow)*dayGridHeight, camPos.z + camOffsetZ));
 //		    eventsToday.setRotation(new Vec(90.0f, 0.0f, 180.0f));
 //		    eventsToday.setScale(new Vec(8.0f, 1.0f, 7.0f));
 		    eventsVisible = true;
     	}
     	else
     	{
     		serviceManager.getSetup().world.remove(eventsToday);
     		eventsVisible = false;
     	}
     }
     
     public void visible(boolean visible)
     {
     	if(!visible)
     	{
     		serviceManager.getSetup().world.remove(calendar);
     		serviceManager.getSetup().world.remove(eventsToday);
     	}
     	else
     	{
     		serviceManager.getSetup().world.add(calendar);
     		serviceManager.getSetup().world.add(eventsToday);
     	}
     }
     
     public void initEventsList()
     {
     	try 
 	    {
 	        InputStream in;
 			in = serviceManager.getSetup().myTargetActivity.getAssets().open(eventsFile);
 		
 		    String fileContent = IO.convertInputStreamToString(in);
 		    String tmpEvents[] = fileContent.split("/n");
 		    
 		    for(int i=0; i<tmpEvents.length; i++)
 		    {
 		    	String singleEvent[] = tmpEvents[i].split("#");
 		    	events.add(new CalendarEvent(singleEvent[0], singleEvent[1]));
 		    }
 		    
 		}
 	    catch (IOException e) 
 	    {
 			Log.d(LOG_TAG, "couldn't open file: " + eventsFile);
 			e.printStackTrace();
 		}
     }
     
     private class FlipEventsVisibility extends Command
 	{
 
 		@Override
 		public boolean execute() 
 		{
 			if(eventsVisible)
 				showEvents(false);
 				
 			else
 				showEvents(true);
 			
 			return true;
 		}
 	
 	}
 }
