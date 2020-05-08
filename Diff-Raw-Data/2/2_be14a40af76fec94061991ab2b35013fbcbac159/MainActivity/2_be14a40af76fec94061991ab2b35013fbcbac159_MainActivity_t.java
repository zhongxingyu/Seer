 package com.calendar.demo;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.Display;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.ListView;
 import android.widget.PopupWindow;
 import android.widget.RelativeLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.calendar.demo.view.NoteTaking;
 import com.calendar.demo.view.widget.OnWheelChangedListener;
 import com.calendar.demo.view.widget.WheelView;
 import com.calendar.demo.view.widget.adapters.ArrayWheelAdapter;
 import com.calendar.demo.view.widget.adapters.NumericWheelAdapter;
 import com.calendar.util.NumMonthOfYear;
 import com.calendar.util.util;
 
 /**
  * Androidʵؼ
  * @Description: Androidʵؼ
 
  * @File: MainActivity.java
 
  * @Package com.calendar.demo
 
  * @Author zhanglanyun 
 
  * @Date 2013-5-21 
 
  * @Version V1.0
  * 
  * 
  *   1. ޸ÿĶ̬ʾ56
  *   2. ʵʾ
  *   3. ʵĻ,
  *   4. Ӽʱֻʾǰܣ,ȥ£򻬶ʧȥ
  *   5. ̬Ӽ¹
  *   6. ·ֱÿ棬л
  *   7. ʾȷǽʾ
  *   8. ӵڵıڶԻֻʾպ
  *   9. ڣҪӣҪԣǼƻǱԱԺ
  *   10. ֮ʾ,ת¸֮ĬϻǵǵӲ
  *   11. ¸֮ѡڣڵʾȷbug޸
  *   12. ʵּƶʱԶ̬仯
  *   13. ƻͱлʵ 
  *   14. ߼ѡӣͬʱгӵʱ
  *   
  *   15. ¼棬ȥлıѡ߼
  */
 public class MainActivity extends Activity{
 	// 
 	private LinearLayout layContent = null; //
 	private ArrayList<DateWidgetDayCell> days = new ArrayList<DateWidgetDayCell>();
 	private ArrayList<View> layrow = new ArrayList<View>();
 	
 	public ArrayList<String> arr;
 
 	// ڱ
 	public static Calendar calStartDate = Calendar.getInstance();
 	private Calendar calToday = Calendar.getInstance();
 	private Calendar calCalendar = Calendar.getInstance();
 	private Calendar calSelected = Calendar.getInstance();
 
 	// ǰ
 	private int iMonthViewCurrentMonth_Dialog = 0;
 	private int iMonthViewCurrentYear_Dialog = 0;
 	
 	private int iMonthViewCurrentMonth = 0;
 	private int iMonthViewCurrentYear = 0;
 	private int iFirstDayOfWeek = Calendar.MONDAY;
 	private int iDay = 0;
 	private int numOfDay = 0;
 	private int currow = -1;
 	private int prerow = -1;
 	private int selectday = -1;
 	
 	
 	private int Calendar_Width = 0;
 	private int Cell_Width = 0;
 	private boolean isFiveRowExist = false;
 	private boolean isOff = false;
 
 	// ҳؼ
 	TextView Top_Date = null;
 	Button btn_pre_month = null;
 	Button btn_next_month = null;
 	TextView arrange_text = null;
 	RelativeLayout mainLayout = null;
 	LinearLayout arrange_layout = null;
 	NoteTaking nt = null;
 	View  addnote = null;
 	EditText addeventcontent = null;
 	TextView save = null;
 	ImageView iv = null;
 	ImageButton b_date = null;
 	ImageButton b_alarm = null;
 	DatePickerDialog mDialog = null;
 	
 	
 	//--------------listview----------------
 	ListView listview = null;
 	//SimpleAdapter sa = null;
 	List<Map<String,String>> noteitem = new ArrayList<Map<String,String>>();
 	MyAdapter adapter = null;
 	
 
 	// Դ
 	ArrayList<String> Calendar_Source = null;
 	Hashtable<Integer, Integer> calendar_Hashtable = new Hashtable<Integer, Integer>();
 	Boolean[] flag = null;
 	Calendar startDate = null;
 	Calendar endDate = null;
 	int dayvalue = -1;
 
 	public static int Calendar_WeekBgColor = 0;
 	public static int Calendar_DayBgColor = 0;
 	public static int isHoliday_BgColor = 0;
 	public static int unPresentMonth_FontColor = 0;
 	public static int isPresentMonth_FontColor = 0;
 	public static int isToday_BgColor = 0;
 	public static int special_Reminder = 0;
 	public static int common_Reminder = 0;
 	public static int Calendar_WeekFontColor = 0;
 
 	String UserName = "";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		
 		System.out.println(getApplicationContext().getFilesDir().getAbsolutePath());
 		
 		// Ļ͸ߣӋĻȷߵȷݵĴС
 		WindowManager windowManager = getWindowManager();
 		Display display = windowManager.getDefaultDisplay();
 		int screenWidth = display.getWidth();
 		Calendar_Width = screenWidth;
 		Cell_Width = Calendar_Width / 7 + 1;
 		util.setWidth(Cell_Width,Calendar_Width);
 		
 		// ƶļ
 		mainLayout = (RelativeLayout) getLayoutInflater().inflate(
 				R.layout.calendar_main, null);
 		// mainLayout.setPadding(2, 0, 2, 0);
 		setContentView(mainLayout);
 
 		listview = (ListView)mainLayout.findViewById(R.id.listview);
 		//listview = (ListView)getLayoutInflater().inflate(R.layout.list, null);
 		adapter = new MyAdapter(MainActivity.this);
 		listview.setAdapter(adapter);
 		listview.setDividerHeight(0);
 		listview.setCacheColorHint(Color.TRANSPARENT);
 		
 		
 		// ؼ¼
 		Top_Date = (TextView) findViewById(R.id.Top_Date);
 		btn_pre_month = (Button) findViewById(R.id.btn_pre_month);
 		btn_next_month = (Button) findViewById(R.id.btn_next_month);
 		btn_pre_month.setOnClickListener(new Pre_MonthOnClickListener());
 		btn_next_month.setOnClickListener(new Next_MonthOnClickListener());
 
 		// 㱾еĵһ(һµĳ)
 		//mainLayout.addView(generateCalendarMain());
 		generateCalendarMain();
 		//listviewּtextview
 		//mainLayout.addView(listview);
 		nt = new NoteTaking(MainActivity.this,Calendar_Width,Cell_Width);
 		nt.setOnClickListener(new tvClicklistener());
 		nt.setData(getString(R.string.writeit));
 		nt.setBackgroundDrawable(getResources().getDrawable(R.drawable.add_event_edit_bg));
 		//mainLayout.addView(nt);
 		
 		calStartDate = getCalendarStartDate();
 		DateWidgetDayCell daySelected = updateCalendar();
 
 		if (daySelected != null)
 			daySelected.requestFocus();
 
 		LinearLayout.LayoutParams Param1 = new LinearLayout.LayoutParams(
 				ViewGroup.LayoutParams.MATCH_PARENT,
 				ViewGroup.LayoutParams.MATCH_PARENT);
 
 		ScrollView view = new ScrollView(this);
 		arrange_layout = createLayout(LinearLayout.VERTICAL);
 		arrange_layout.setPadding(5, 2, 0, 0);
 		arrange_text = new TextView(this);
 		mainLayout.setBackgroundColor(Color.WHITE);
 		arrange_text.setTextColor(Color.BLACK);
 		arrange_text.setTextSize(18);
 		arrange_layout.addView(arrange_text);
 
 		startDate = GetStartDate();
 		calToday = GetTodayDate();
 
 		endDate = GetEndDate(startDate);
 		view.addView(arrange_layout, Param1);
 		
 		//mainLayout.addView(view);
 
 		// ½߳
 		new Thread() {
 			@Override
 			public void run() {
 				int day = GetNumFromDate(calToday, startDate);
 				
 				if (calendar_Hashtable != null
 						&& calendar_Hashtable.containsKey(day)) {
 					dayvalue = calendar_Hashtable.get(day);
 				}
 			}
 			
 		}.start();
 
 		Calendar_WeekBgColor = this.getResources().getColor(
 				R.color.Calendar_WeekBgColor);
 		Calendar_DayBgColor = this.getResources().getColor(
 				R.color.Calendar_DayBgColor);
 		isHoliday_BgColor = this.getResources().getColor(
 				R.color.isHoliday_BgColor);
 		unPresentMonth_FontColor = this.getResources().getColor(
 				R.color.unPresentMonth_FontColor);
 		isPresentMonth_FontColor = this.getResources().getColor(
 				R.color.isPresentMonth_FontColor);
 		isToday_BgColor = this.getResources().getColor(R.color.isToday_BgColor);
 		special_Reminder = this.getResources()
 				.getColor(R.color.specialReminder);
 		common_Reminder = this.getResources().getColor(R.color.commonReminder);
 		Calendar_WeekFontColor = this.getResources().getColor(
 				R.color.Calendar_WeekFontColor);
 		//ʼԴadapter
 		
 	/*	initListData();
 		listview.setAdapter(sa);*/
 		
 		iv = (ImageView)findViewById(R.id.iv);
 		iv.setOnClickListener(new tvClicklistener());
 		//mainLayout.addView(iv);
 		
 		initAddView();
 		//ѡ·ݵļ
 		Top_Date.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View arg0) {
 				mDialog = new CustomerDatePickerDialog(MainActivity.this ,
 						listener ,iMonthViewCurrentYear,iMonthViewCurrentMonth,
 						calToday.get(Calendar.DAY_OF_MONTH)
 						);
 				//mDialog.setTitle(calToday.get(Calendar.YEAR )+""+(calToday.get(Calendar.MONTH )+1)+"");
 				mDialog.setTitle(iMonthViewCurrentYear + ""+(iMonthViewCurrentMonth+1)+"");
 				mDialog.show();
 		        
 			}
 		});
 		
 		selectday =iDay + calToday.get(Calendar.DAY_OF_MONTH);
 		System.out.println("onCreate---"+selectday);
 		
 	}
 	
 	class CustomerDatePickerDialog extends DatePickerDialog {
 
         public CustomerDatePickerDialog(Context context,
                 OnDateSetListener callBack, int year, int monthOfYear,
                 int dayOfMonth) {
             super(context, callBack, year, monthOfYear, dayOfMonth);
         }
 
         @Override
         public void onDateChanged(DatePicker view, int year, int month, int day) {
             super.onDateChanged(view, year, month, day);
             mDialog.setTitle(year + "" + (month+1) + "");
             iMonthViewCurrentMonth_Dialog = month;
             iMonthViewCurrentYear_Dialog = year;
             
           
         }
        
         @Override
 		public void onClick(DialogInterface dialog, int which) {
 			//õ£ɺ£ֻǻӦòԽ硣½
         	if(which == DialogInterface.BUTTON1){
 				if(iMonthViewCurrentMonth_Dialog != iMonthViewCurrentMonth || 
 						iMonthViewCurrentYear_Dialog != iMonthViewCurrentYear){
 					
 					System.out.println(iMonthViewCurrentMonth_Dialog);
 					System.out.println(iMonthViewCurrentYear_Dialog);
 					toMonthYear(iMonthViewCurrentMonth_Dialog,iMonthViewCurrentYear_Dialog);
 					
 				}
         	}
        }
         
         @Override
     	public void show() {
     		// TODO Auto-generated method stub
     		super.show();
     		 DatePicker dp = findDatePicker((ViewGroup) this.getWindow().getDecorView());
     	        if (dp != null) {
     	        	Class c=dp.getClass();
     	        	Field f;
     				try {
     						if(Build.VERSION.SDK_INT>14){
     							f = c.getDeclaredField("mDaySpinner");
     							f.setAccessible(true );  
     							LinearLayout l= (LinearLayout)f.get(dp);   
     							l.setVisibility(View.GONE);
     						}else{
     							f = c.getDeclaredField("mDayPicker");
     							f.setAccessible(true );  
     							LinearLayout l= (LinearLayout)f.get(dp);   
     							l.setVisibility(View.GONE);
     						}
     				} catch (SecurityException e) {
     					// TODO Auto-generated catch block
     					e.printStackTrace();
     				} catch (NoSuchFieldException e) {
     					// TODO Auto-generated catch block
     					e.printStackTrace();
     				} catch (IllegalArgumentException e) {
     					// TODO Auto-generated catch block
     					e.printStackTrace();
     				} catch (IllegalAccessException e) {
     					// TODO Auto-generated catch block
     					e.printStackTrace();
     				}  
     	        	
     	        } 
     	}
     }
 	//ӵǰdialogвDatePicker
 	private DatePicker findDatePicker(ViewGroup group) {
         if (group != null) {
             for (int i = 0, j = group.getChildCount(); i < j; i++) {
                 View child = group.getChildAt(i);
                 if (child instanceof DatePicker) {
                     return (DatePicker) child;
                 } else if (child instanceof ViewGroup) {
                     DatePicker result = findDatePicker((ViewGroup) child);
                     if (result != null)
                         return result;
                 }
             }
         }
         return null;
     } 
 	
 	 //תmonth   year ض·
     public void toMonthYear(int month,int year){
 
 			calSelected.setTimeInMillis(0);
 			iMonthViewCurrentMonth = month;
 			iMonthViewCurrentYear  = year;
 			
 			calStartDate.set(Calendar.DAY_OF_MONTH, 1);
 			calStartDate.set(Calendar.MONTH, iMonthViewCurrentMonth);
 			calStartDate.set(Calendar.YEAR, iMonthViewCurrentYear);
 			calStartDate.set(Calendar.HOUR_OF_DAY, 0);
 			calStartDate.set(Calendar.MINUTE, 0);
 			calStartDate.set(Calendar.SECOND, 0);
 			calStartDate.set(Calendar.MILLISECOND, 0);
 			
 			
 			UpdateStartDateForMonth();
 			startDate = (Calendar) calStartDate.clone();
 			endDate = GetEndDate(startDate);
 
 			// ½߳
 			new Thread() {
 				@Override
 				public void run() {
 
 					int day = GetNumFromDate(calToday, startDate);
 					
 					if (calendar_Hashtable != null
 							&& calendar_Hashtable.containsKey(day)) {
 						dayvalue = calendar_Hashtable.get(day);
 					}
 				}
 			}.start();
 
 			updateCalendar();
 			selectday =iDay + calToday.get(Calendar.DAY_OF_MONTH);
 			System.out.println("toMonthYear-----"+selectday);
     }
 
 	protected String GetDateShortString(Calendar date) {
 		String returnString = date.get(Calendar.YEAR) + "/";
 		returnString += date.get(Calendar.MONTH) + 1 + "/";
 		returnString += date.get(Calendar.DAY_OF_MONTH);
 		
 		return returnString;
 	}
 
 	// õе
 	private int GetNumFromDate(Calendar now, Calendar returnDate) {
 		Calendar cNow = (Calendar) now.clone();
 		Calendar cReturnDate = (Calendar) returnDate.clone();
 		setTimeToMidnight(cNow);
 		setTimeToMidnight(cReturnDate);
 		
 		long todayMs = cNow.getTimeInMillis();
 		long returnMs = cReturnDate.getTimeInMillis();
 		long intervalMs = todayMs - returnMs;
 		int index = millisecondsToDays(intervalMs);
 		
 		return index;
 	}
 
 	private int millisecondsToDays(long intervalMs) {
 		return Math.round((intervalMs / (1000 * 86400)));
 	}
 
 	private void setTimeToMidnight(Calendar calendar) {
 		calendar.set(Calendar.HOUR_OF_DAY, 0);
 		calendar.set(Calendar.MINUTE, 0);
 		calendar.set(Calendar.SECOND, 0);
 		calendar.set(Calendar.MILLISECOND, 0);
 	}
 
 	// ɲ
 	private LinearLayout createLayout(int iOrientation) {
 		LinearLayout lay = new LinearLayout(this);
 		lay.setLayoutParams(new LayoutParams(
 				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
 				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
 		lay.setOrientation(iOrientation);
 		
 		return lay;
 	}
 
 	// ͷ
 	private View generateCalendarHeader() {
 		LinearLayout layRow = createLayout(LinearLayout.HORIZONTAL);
 		// layRow.setBackgroundColor(Color.argb(255, 207, 207, 205));
 		
 		for (int iDay = 0; iDay < 7; iDay++) {
 			DateWidgetDayHeader day = new DateWidgetDayHeader(this, Cell_Width,
 					35);
 			
 			final int iWeekDay = DayStyle.getWeekDay(iDay, iFirstDayOfWeek);
 			day.setData(iWeekDay);
 			layRow.addView(day);
 		}
 		
 		return layRow;
 	}
 
 	// 
 	private View generateCalendarMain() {
 		//layContent = createLayout(LinearLayout.VERTICAL);
 		layContent = (LinearLayout)mainLayout.findViewById(R.id.lly);
 		// layContent.setPadding(1, 0, 1, 0);
 		layContent.setBackgroundColor(Color.argb(255, 105, 105, 103));
 		layContent.addView(generateCalendarHeader());
 		days.clear();
 		
 		for (int iRow = 0; iRow < 6; iRow++) {
 			View view = generateCalendarRow();
 			layContent.addView(view);
 			//viewŵһȥȻػʾһview
 			layrow.add(view);
 		}
 		
 		return layContent;
 	}
 
 	// еһУ
 	private View generateCalendarRow() {
 		LinearLayout layRow = createLayout(LinearLayout.HORIZONTAL);
 		
 		for (int iDay = 0; iDay < 7; iDay++) {
 			DateWidgetDayCell dayCell = new DateWidgetDayCell(this, Cell_Width,
 					Cell_Width);
 			dayCell.setItemClick(mOnDayCellClick);
 			days.add(dayCell);
 			layRow.addView(dayCell);
 		}
 		
 		return layRow;
 	}
 
 	// õںͱѡ
 	private Calendar getCalendarStartDate() {
 		calToday.setTimeInMillis(System.currentTimeMillis());
 		calToday.setFirstDayOfWeek(iFirstDayOfWeek);
 		selectday = GetNumFromDate(calToday, GetStartDate());
 		
 
 		if (calSelected.getTimeInMillis() == 0) {
 			calStartDate.setTimeInMillis(System.currentTimeMillis());
 			calStartDate.setFirstDayOfWeek(iFirstDayOfWeek);
 		} else {
 			calStartDate.setTimeInMillis(calSelected.getTimeInMillis());
 			calStartDate.setFirstDayOfWeek(iFirstDayOfWeek);
 		}
 		
 		UpdateStartDateForMonth();
 		return calStartDate;
 	}
 
 	// ڱϵڶǴһʼģ˷ڱʾ
 	private void UpdateStartDateForMonth() {
 		iMonthViewCurrentMonth = calStartDate.get(Calendar.MONTH);
 		iMonthViewCurrentYear = calStartDate.get(Calendar.YEAR);
 		calStartDate.set(Calendar.DAY_OF_MONTH, 1);
 		calStartDate.set(Calendar.HOUR_OF_DAY, 0);
 		calStartDate.set(Calendar.MINUTE, 0);
 		calStartDate.set(Calendar.SECOND, 0);
 		// update days for week
 		UpdateCurrentMonthDisplay();
 		int iDay = 0;
 		int iStartDay = iFirstDayOfWeek;
 		
 		if (iStartDay == Calendar.MONDAY) {
 			iDay = calStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
 			if (iDay < 0)
 				iDay = 6;
 		}
 		
 		if (iStartDay == Calendar.SUNDAY) {
 			iDay = calStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
 			if (iDay < 0)
 				iDay = 6;
 		}
 		this.iDay=iDay;
 		calStartDate.add(Calendar.DAY_OF_WEEK, -iDay);
 		
 		
 		//calStartDate----------------------------------------------
 		int currow = rowOfMonth(calStartDate);
 		System.out.println(currow);
 		if (currow == 5){
 			layrow.get(5).setVisibility(View.GONE);
 			isFiveRowExist = false;
 		}
 		else if (currow == 6){
 			layrow.get(5).setVisibility(View.VISIBLE);
 			isFiveRowExist = true;
 		}
 		
 	}
 
 	// ,ҲҪ
 	private DateWidgetDayCell updateCalendar() {
 		DateWidgetDayCell daySelected = null;
 		boolean bSelected = false;
 		final boolean bIsSelection = (calSelected.getTimeInMillis() != 0);
 		final int iSelectedYear = calSelected.get(Calendar.YEAR);
 		final int iSelectedMonth = calSelected.get(Calendar.MONTH);
 		final int iSelectedDay = calSelected.get(Calendar.DAY_OF_MONTH);
 		calCalendar.setTimeInMillis(calStartDate.getTimeInMillis());
 		
 		for (int i = 0; i < days.size(); i++) {
 			final int iYear = calCalendar.get(Calendar.YEAR);
 			final int iMonth = calCalendar.get(Calendar.MONTH);
 			final int iDay = calCalendar.get(Calendar.DAY_OF_MONTH);
 			final int iDayOfWeek = calCalendar.get(Calendar.DAY_OF_WEEK);
 			DateWidgetDayCell dayCell = days.get(i);
 
 			// жǷ
 			boolean bToday = false;
 			
 			if (calToday.get(Calendar.YEAR) == iYear) {
 				if (calToday.get(Calendar.MONTH) == iMonth) {
 					if (calToday.get(Calendar.DAY_OF_MONTH) == iDay) {
						//selectday = GetNumFromDate(calSelected, GetStartDate());
 						bToday = true;
 					}
 				}
 			}
 
 			// check holiday
 			boolean bHoliday = false;
 			if ((iDayOfWeek == Calendar.SATURDAY)
 					|| (iDayOfWeek == Calendar.SUNDAY))
 				bHoliday = true;
 			if ((iMonth == Calendar.JANUARY) && (iDay == 1))
 				bHoliday = true;
 
 			// Ƿѡ
 			bSelected = false;
 			
 			if (bIsSelection)
 				if ((iSelectedDay == iDay) && (iSelectedMonth == iMonth)
 						&& (iSelectedYear == iYear)) {
 					bSelected = true;
 				}
 			
 			dayCell.setSelected(bSelected);
 
 			// Ƿм¼
 			boolean hasRecord = false;
 			
 			if (flag != null && flag[i] == true && calendar_Hashtable != null
 					&& calendar_Hashtable.containsKey(i)) {
 				// hasRecord = flag[i];
 				hasRecord = Calendar_Source.get(calendar_Hashtable.get(i))
 						.contains(UserName);
 			}
 
 			if (bSelected)
 				daySelected = dayCell;
 
 			dayCell.setData(iYear, iMonth, iDay, bToday, bHoliday,
 					iMonthViewCurrentMonth, hasRecord);
 
 			calCalendar.add(Calendar.DAY_OF_MONTH, 1);
 		}
 		//ػ֮ǰжǷҪʾ
 		
 		layContent.invalidate();
 		
 		return daySelected;
 	}
 
 	// ʾ
 	private void UpdateCurrentMonthDisplay() {
 		String date = calStartDate.get(Calendar.YEAR) + ""
 				+ (calStartDate.get(Calendar.MONTH) + 1) + "";
 		Top_Date.setText(date);
 	}
 
 	// °ť¼
 	class Pre_MonthOnClickListener implements OnClickListener {
 		@Override
 		public void onClick(View v) {
 			// TODO Auto-generated method stub
 			arrange_text.setText("");
 			calSelected.setTimeInMillis(0);
 			iMonthViewCurrentMonth--;
 			
 			if (iMonthViewCurrentMonth == -1) {
 				iMonthViewCurrentMonth = 11;
 				iMonthViewCurrentYear--;
 			}
 			calStartDate.set(Calendar.DAY_OF_MONTH, 1);
 			calStartDate.set(Calendar.MONTH, iMonthViewCurrentMonth);
 			calStartDate.set(Calendar.YEAR, iMonthViewCurrentYear);
 			calStartDate.set(Calendar.HOUR_OF_DAY, 0);
 			calStartDate.set(Calendar.MINUTE, 0);
 			calStartDate.set(Calendar.SECOND, 0);
 			calStartDate.set(Calendar.MILLISECOND, 0);
 			
 			UpdateStartDateForMonth();
 			startDate = (Calendar) calStartDate.clone();
 			endDate = GetEndDate(startDate);
 
 			// ½߳
 			new Thread() {
 				@Override
 				public void run() {
 
 					int day = GetNumFromDate(calToday, startDate);
 					
 					if (calendar_Hashtable != null
 							&& calendar_Hashtable.containsKey(day)) {
 						dayvalue = calendar_Hashtable.get(day);
 					}
 				}
 			}.start();
 
 			updateCalendar();
 			selectday =iDay + calToday.get(Calendar.DAY_OF_MONTH);
 			System.out.println("prev_month---"+selectday);
 		}
 
 	}
 
 	// °ť¼
 	class Next_MonthOnClickListener implements OnClickListener {
 		@Override
 		public void onClick(View v) {
 			// TODO Auto-generated method stub
 			arrange_text.setText("");
 			calSelected.setTimeInMillis(0);
 			iMonthViewCurrentMonth++;
 			
 			if (iMonthViewCurrentMonth == 12) {
 				iMonthViewCurrentMonth = 0;
 				iMonthViewCurrentYear++;
 			}
 			
 			calStartDate.set(Calendar.DAY_OF_MONTH, 1);
 			calStartDate.set(Calendar.MONTH, iMonthViewCurrentMonth);
 			calStartDate.set(Calendar.YEAR, iMonthViewCurrentYear);
 			calStartDate.set(Calendar.HOUR_OF_DAY, 0);
 			calStartDate.set(Calendar.MINUTE, 0);
 			calStartDate.set(Calendar.SECOND, 0);
 			calStartDate.set(Calendar.MILLISECOND, 0);
 			
 			
 			
 			
 			UpdateStartDateForMonth();
 			startDate = (Calendar) calStartDate.clone();
 			endDate = GetEndDate(startDate);
 
 			// ½߳
 			new Thread() {
 				@Override
 				public void run() {
 					int day = 5;
 					
 					if (calendar_Hashtable != null
 							&& calendar_Hashtable.containsKey(day)) {
 						dayvalue = calendar_Hashtable.get(day);
 					}
 				}
 			}.start();
 
 			updateCalendar();
 			//selectday
 			selectday =iDay + calToday.get(Calendar.DAY_OF_MONTH);
 			System.out.println("next_month---"+selectday);
 			
 		}
 	}
 	// ¼
 	private DateWidgetDayCell.OnItemClick mOnDayCellClick = new DateWidgetDayCell.OnItemClick() {
 		public void OnClick(DateWidgetDayCell item) {
 			calSelected.setTimeInMillis(item.getDate().getTimeInMillis());
 			int day = GetNumFromDate(calSelected, startDate);
 			selectday = day+1;
 			System.out.println("onClick----"+selectday);
 			item.setSelected(true);
 			updateCalendar();
 			//ıݣѯݿ-------------------------------------
 			arr.clear();
 			arr.add(selectday+"");
 			adapter.notifyDataSetChanged();
 		}
 	};
 
 	public Calendar GetTodayDate() {
 		Calendar cal_Today = Calendar.getInstance();
 		cal_Today.set(Calendar.HOUR_OF_DAY, 0);
 		cal_Today.set(Calendar.MINUTE, 0);
 		cal_Today.set(Calendar.SECOND, 0);
 		cal_Today.setFirstDayOfWeek(Calendar.MONDAY);
 
 		return cal_Today;
 	}
 
 	// õǰеĵһ
 	public Calendar GetStartDate() {
 		int iDay = 0;
 		Calendar cal_Now = Calendar.getInstance();
 		cal_Now.set(Calendar.DAY_OF_MONTH, 1);
 		cal_Now.set(Calendar.HOUR_OF_DAY, 0);
 		cal_Now.set(Calendar.MINUTE, 0);
 		cal_Now.set(Calendar.SECOND, 0);
 		cal_Now.setFirstDayOfWeek(Calendar.MONDAY);
 
 		iDay = cal_Now.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
 		
 		if (iDay < 0) {
 			iDay = 6;
 		}
 		this.iDay = iDay;
 		
 		cal_Now.add(Calendar.DAY_OF_WEEK, -iDay);
 		
 		return cal_Now;
 	}
 
 	public Calendar GetEndDate(Calendar startDate) {
 		// Calendar end = GetStartDate(enddate);
 		Calendar endDate = Calendar.getInstance();
 		endDate = (Calendar) startDate.clone();
 		//row = rowOfMonth(startDate) / 7;
 		
 		endDate.add(Calendar.DAY_OF_MONTH, 41);
 		return endDate;
 	}
 	
 	public int rowOfMonth(Calendar startDate){
 		
 		int day_num = NumMonthOfYear.dayOfMonth(iMonthViewCurrentYear, iMonthViewCurrentMonth) + iDay;
 //		System.out.println(iMonthViewCurrentYear+ " " + iMonthViewCurrentMonth + " " + iDay+" ");
 //		System.out.println("day_num" + day_num);
 		if(day_num < 36)
 			numOfDay = 5;
 		else
 			numOfDay = 6;
 		System.out.println("rowofnum="+numOfDay);
 		return numOfDay;
 	}
 	
 	public void setViewGone(){
 		
 		System.out.println("in setViewGone " + selectday);
 		int row = (selectday-1) / 7 ;
 		//زrow
 		for(int i=0;i<layrow.size();i++){
 			if(i != row)
 				layrow.get(i).setVisibility(View.GONE);
 		}
 	}
 	
 	public void setViewVisble(){
 		int row = (selectday-1) / 7;
 		for(int i=0;i<layrow.size();i++){
 			if(i != row )
 				layrow.get(i).setVisibility(View.VISIBLE);
 		}
 		if(!isFiveRowExist)
 			layrow.get(5).setVisibility(View.GONE);
 	}
 	//listviewadapter,޸Ϊֻһtextviewʾ
 	private class MyAdapter extends BaseAdapter {  
         private Context context;  
         private LayoutInflater inflater;  
           
         public MyAdapter(Context context) {  
             super();  
             this.context = context;  
             inflater = LayoutInflater.from(context);  
             arr = new ArrayList<String>();  
         }  
         @Override  
         public int getCount() {  
             // TODO Auto-generated method stub  
             return arr.size();  
         }  
         @Override  
         public Object getItem(int arg0) {  
             // TODO Auto-generated method stub  
             return arg0;  
         }  
         @Override  
         public long getItemId(int arg0) {  
             // TODO Auto-generated method stub  
             return arg0;  
         }  
         @Override  
         public View getView(final int position, View view, ViewGroup arg2) {  
             // TODO Auto-generated method stub  
             if(view == null){  
                 view = inflater.inflate(R.layout.item_events, null);  
             }
             final TextView tv = (TextView)view.findViewById(R.id.text);
             /*final EditText edit = (EditText) view.findViewById(R.id.edit); 
             edit.setText(arr.get(position));    //عadapterʱݴ  
             Button del = (Button) view.findViewById(R.id.del);  */
             tv.setText(arr.get(position));
             tv.setOnClickListener(new OnClickListener(){
 				@Override
 				public void onClick(View arg0) {
 					System.out.println("textview clicked!");
 				}
 			});
             return view;  
         }  
         
     }
 	
 	public void clickText(){
 		//ntأʾӽ棬ͬӦĲ
 		iv.setVisibility(View.GONE);
 		listview.setVisibility(View.GONE);
 		addNote();
 		setViewGone();
 		
 		addeventcontent.setVisibility(View.VISIBLE);
 		addeventcontent.setText(null);
 		save.setVisibility(View.VISIBLE);
 		b_date.setVisibility(View.VISIBLE);
 		b_alarm.setVisibility(View.VISIBLE);
 		//Զ
 		InputMethodManager imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);  
 		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);  
 		imm.showSoftInput(null, InputMethodManager.SHOW_IMPLICIT); 
 	}
 	
 	public void addNote(){
 		//¼
 		save.setOnClickListener(new addnoteclicklistener());
 	}
 	public class tvClicklistener implements View.OnClickListener{
 
 		@Override
 		public void onClick(View v) {
 			//Ӧ¼
 			clickText();
 		}
 		
 	}
 	
 	public class addnoteclicklistener implements View.OnClickListener{
 
 		@Override
 		public void onClick(View v) {
 			
 			String content = addeventcontent.getText().toString();
 			if(content.length() == 0 || content.equals("") || content == null || 
 					content.trim().length() == 0){
 				Toast.makeText(MainActivity.this, getString(R.string.contentisnull),
 						Toast.LENGTH_SHORT).show();
 			}else{
 				//ΪյĻַʾּʾlistviewӦ
 				//addnote.setVisibility(View.GONE);
 				addeventcontent.setVisibility(View.GONE);
 				save.setVisibility(View.GONE);
 				b_date.setVisibility(View.GONE);
 				b_alarm.setVisibility(View.GONE);
 				
 				listview.setVisibility(View.VISIBLE);
 				iv.setVisibility(View.VISIBLE);
 				setViewVisble();
 				String text = content.trim();
 				arr.add(text);
 				adapter.notifyDataSetChanged();
 				//ص
 				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
 				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
 				
 			}
 		}
 		
 	}
 	/**
 	 * Ƕ¼һЩؼгʼ
 	 */
 	public void initAddView(){
 		addeventcontent = (EditText)findViewById(R.id.add_event_content);
 		save = (TextView)findViewById(R.id.save);
 		b_date = (ImageButton)findViewById(R.id.b_mp);
 		b_date.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View view) {
 				if(!isOff){
 					b_date.setBackgroundDrawable(
 							getResources().getDrawable(R.drawable.setting_switch_default_off));
 					isOff = true;
 				}else{
 					b_date.setBackgroundDrawable(getResources().getDrawable(R.drawable.setting_switch_default_on));
 					isOff = false;
 				}
 			}
 			
 		});
 		b_alarm = (ImageButton)findViewById(R.id.b_alarm);
 		b_alarm.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View view) {
 				initPopUpWindow();
 			}
 			
 		});
 		addeventcontent.setVisibility(View.GONE);
 		save.setVisibility(View.GONE);
 		b_date.setVisibility(View.GONE);
 		b_alarm.setVisibility(View.GONE);
 		
 	}
 	
 	private void initPopUpWindow(){
 		View popupView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.pop_up_date, null);
 		final PopupWindow  popupWindow = new PopupWindow(popupView,LayoutParams.WRAP_CONTENT,
 				LayoutParams.WRAP_CONTENT);
 		popupWindow.showAsDropDown(layContent,((layContent.getWidth()-popupView.getWidth())/4),0);
 		
 		// set the view of alarm
 		Calendar calendar = Calendar.getInstance();
 
         final WheelView month = (WheelView) popupView.findViewById(R.id.month);
         final WheelView year = (WheelView) popupView.findViewById(R.id.year);
         final WheelView day = (WheelView) popupView.findViewById(R.id.day);
         
         OnWheelChangedListener listener = new OnWheelChangedListener() {
             public void onChanged(WheelView wheel, int oldValue, int newValue) {
                 updateDays(year, month, day);
             }
         };
 
         // month
         int curMonth = calendar.get(Calendar.MONTH);
         String months[] = new String[] {"January", "February", "March", "April", "May",
                 "June", "July", "August", "September", "October", "November", "December"};
         month.setViewAdapter(new DateArrayAdapter(this, months, curMonth));
         month.setCurrentItem(curMonth);
         month.addChangingListener(listener);
     
         // year
         int curYear = calendar.get(Calendar.YEAR);
         year.setViewAdapter(new DateNumericAdapter(this, curYear, curYear + 10, 0));
         year.setCurrentItem(curYear);
         year.addChangingListener(listener);
         
         //day
         updateDays(year, month, day);
         day.setCurrentItem(calendar.get(Calendar.DAY_OF_MONTH) - 1);
 	}
 	
 	/**
      * Updates day wheel. Sets max days according to selected month and year
      */
     void updateDays(WheelView year, WheelView month, WheelView day) {
         Calendar calendar = Calendar.getInstance();
         calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) + year.getCurrentItem());
         calendar.set(Calendar.MONTH, month.getCurrentItem());
         
         int maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
         day.setViewAdapter(new DateNumericAdapter(this, 1, maxDays, calendar.get(Calendar.DAY_OF_MONTH) - 1));
         int curDay = Math.min(maxDays, day.getCurrentItem() + 1);
         day.setCurrentItem(curDay - 1, true);
     }
     
     /**
      * Adapter for numeric wheels. Highlights the current value.
      */
     private class DateNumericAdapter extends NumericWheelAdapter {
         // Index of current item
         int currentItem;
         // Index of item to be highlighted
         int currentValue;
         
         /**
          * Constructor
          */
         public DateNumericAdapter(Context context, int minValue, int maxValue, int current) {
             super(context, minValue, maxValue);
             this.currentValue = current;
             setTextSize(16);
         }
         
         @Override
         protected void configureTextView(TextView view) {
             super.configureTextView(view);
             if (currentItem == currentValue) {
                 view.setTextColor(0xFF0000F0);
             }
             view.setTypeface(Typeface.SANS_SERIF);
         }
         
         @Override
         public View getItem(int index, View cachedView, ViewGroup parent) {
             currentItem = index;
             return super.getItem(index, cachedView, parent);
         }
     }
     
     /**
      * Adapter for string based wheel. Highlights the current value.
      */
     private class DateArrayAdapter extends ArrayWheelAdapter<String> {
         // Index of current item
         int currentItem;
         // Index of item to be highlighted
         int currentValue;
         
         /**
          * Constructor
          */
         public DateArrayAdapter(Context context, String[] items, int current) {
             super(context, items);
             this.currentValue = current;
             setTextSize(16);
         }
         
         @Override
         protected void configureTextView(TextView view) {
             super.configureTextView(view);
             if (currentItem == currentValue) {
                 view.setTextColor(0xFF0000F0);
             }
             view.setTypeface(Typeface.SANS_SERIF);
         }
         
         @Override
         public View getItem(int index, View cachedView, ViewGroup parent) {
             currentItem = index;
             return super.getItem(index, cachedView, parent);
         }
     }
     
 	/**
 	 * Զhandler¼Ϣ
 	 * 
 	 */
 	public class MyHandler extends Handler{
 		
 		public void handleMessage(Message msg){
 		//Ҫÿؽ޸ĵģ̬ĸıҪview	
 		}
 
 	}
 	//ѡԻ
 	private DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener(){  //
 		@Override
 		public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
 			System.out.println("֮󴥷");
 		}
 	};
 	
 }
