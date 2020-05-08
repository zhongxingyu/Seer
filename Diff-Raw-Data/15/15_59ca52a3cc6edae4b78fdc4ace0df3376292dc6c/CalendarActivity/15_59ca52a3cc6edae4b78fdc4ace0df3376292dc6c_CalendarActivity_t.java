 package com.chips;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.chips.gsCalendar.gsCalendarColorParam;
 import com.chips.dataclient.MealClient;
 import com.chips.datarecord.MealRecord;
 import com.chips.homebar.HomeBar;
 import com.chips.homebar.HomeBarAction;
 import com.chips.user.PersistentUser;
 		
 
 public class CalendarActivity extends Activity 
     implements OnClickListener, HomeBar
 {
 	 
 	TextView tvs[] ;
 	Button btns[] ;
 	Context con;   
 	TextView caltv;
 
 	class myGsCalendar extends gsCalendar
 	{
 	    private static final String LIST_MEALS_URL 
 	    = "http://cs110chips.phpfogapp.com/index.php/mobile/list_meals/";
 	    private MealClient client;
 	    
 		public myGsCalendar(Context context, LinearLayout layout) 
 		{
 			super(context, layout);
 			con = context;
 			loadHistoryMeals();
 			// TODO Auto-generated constructor stub
 		}
 		
 		public void loadHistoryMeals()
 		{
 			client = new MealClient();
 	        client.setURL(LIST_MEALS_URL, PersistentUser.getSessionID());
 	        client.logURL();
 	        client.asynchronousLoadClientData();
 		}
 		
 		@Override
 		public void myClickEvent(int yyyy, int MM, int dd) 
 		{
 			// TODO Auto-generated method stub
 	        
 			
 			cal.redraw( ) ;
 			
 			cal.applyHoliday( ) ;
 			
 	        cal.setSelectedDay( CalendarActivity.this.getResources( ).getDrawable( R.drawable.icon ) ) ;
 	        
 	        cal.setSelectedDayTextColor( 0xff009999 ) ;
 	        
 	        String date = String.format("%04d-%02d-%02d", yyyy, MM + 1, dd);
 	        String data = "\n";
 	        List<MealRecord> meals = client.getMealRecords();
         	data += date + "\n";
 	        for(MealRecord meal : meals) {
 	        	if(meal.getScheduledDateString().equals(date))
 	        	{
 	        		data += meal.calendarToString();
 	        	}
 	        }
 			
 			//activity new window.
 			gsCalendarDlg dlg = new gsCalendarDlg( con, data ) ; 
 			dlg.show( ) ;
 		}
 		
 	}
 	
 	
 	myGsCalendar cal ;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) 
     {
     	super.onCreate(savedInstanceState);
         //setContentView(R.layout.calendar);
         
         //here is error.
         HomeBarAction.inflateHomeBarView(this, R.layout.calendar);
         
         LinearLayout lv = (LinearLayout)findViewById( R.id.calendar_lLayout ) ;
        
         
         tvs = new TextView[3] ;
         tvs[0] = (TextView)findViewById( R.id.tv1 ) ;
         tvs[1] = (TextView)findViewById( R.id.tv2 ) ;
         tvs[2] = null ; 
         
         btns = new Button[4] ;
         btns[0] = null ; 
         btns[1] = null ; 
         btns[2] = (Button)findViewById( R.id.Button03 ) ;
         btns[3] = (Button)findViewById( R.id.Button04 ) ;
         
         cal = new myGsCalendar( this, lv ) ;
 
         gsCalendarColorParam cParam = new gsCalendarColorParam( ) ;
         
         cParam.m_cellColor = 0x00000000 ;
         cParam.m_textColor = 0xffffffff ;
         cParam.m_saturdayTextColor = 0xff33ccff ;
         cParam.m_lineColor = 0x99999999 ;
         cParam.m_topCellColor = 0xff003333 ;
         cParam.m_topTextColor = 0xffffffff ;
         cParam.m_topSundayTextColor = 0xffffffff ;
         cParam.m_topSaturdatTextColor = 0xffffffff ;
         
         cal.setColorParam( cParam ) ;
         
         Drawable img = getResources( ).getDrawable( R.drawable.bg ) ;
         cal.setBackground( img ) ;
         
         cal.setCalendarSize( 478, 600 ) ;
         
         cal.setTopCellSize( 35 ) ;
         
         cal.setControl( btns ) ;
         
         cal.setViewTarget( tvs ) ;
         
         cal.initCalendar( ) ;
         
         cal.setSelectedDay( getResources( ).getDrawable( R.drawable.icon ) ) ;
         
         cal.setSelectedDayTextColor( 0xff009999 ) ;
         
         //cal.setSelectedDay( 0xff000000, 0xffffffff ) ;
         
         cal.addHoliday( 324 ) ;
         
         cal.applyHoliday( ) ;	        
     }
 
 
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 	}
 
 
     @Override
     public void goHomeClicked(View view) {
         HomeBarAction.goHomeClicked(this, view);
     }
 
 
     @Override
     public void addFavoriteClicked(View view) {
         HomeBarAction.addFavoriteClicked(this, view);
     }
 }
