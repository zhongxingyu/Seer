 package com.indicrowd.util;
 
 import java.text.DateFormatSymbols;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 
 public class DateUtil {
 	public static void main(String[] args){
 		Calendar cal = getCalendarWithInteger(20130412);
 		cal = getNextMinutes(cal, 80);
 		System.out.println(cal.get(Calendar.HOUR_OF_DAY) +"," + cal.get(Calendar.MINUTE));
 		System.out.println(calendarToInteger(cal));
 	}
 	
 	public static Integer calendarToInteger(Calendar cal){
 		return cal.get(Calendar.YEAR)*10000 + (cal.get(Calendar.MONTH)+1)*100 + cal.get(Calendar.DAY_OF_MONTH);
 	}
 	public static Calendar getNextMinutes(Calendar cal, int count){
 		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE) + count);
 		return cal;
 	}
 	
 	public static Calendar getNextDay(Calendar cal, int count){
 		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH) + count);
 		return cal;
 	}
 	
 	public static Calendar getCalendarWithInteger(Integer date){
 		return getCalendar(date/10000, date/100%100 - 1, date%100);
 	}
 	
 	public static String getFormedDate(Calendar calendar) {
 		return getMonthString(calendar) + " " + calendar.get(Calendar.DATE) + "일, " + calendar.get(Calendar.YEAR) + "년 ";
 	}
 
 	public static String getMonthString(Calendar calendar) {
 
 		return new DateFormatSymbols().getMonths()[calendar.get(Calendar.MONTH)];
 	}
 	
	public static String[] dayNames={"일","월","화","수","목","금","토"}; 
 	static String zone="Asia/Seoul";
 	
 	/**
 	 * 오늘의 Calendar를 가져옴 // 다른 함수에서 쓰기 용도
 	 * @return
 	 */
 	public static Calendar getCalendar()
     {
         TimeZone tz = TimeZone.getTimeZone(zone);
         Calendar cal = new GregorianCalendar(tz);
         return cal;
     }
 	
 	/**
 	 * 특정 년, 월의 calendar를 가져옴 (주의 : month 는 0부터 시작 ex) 1월 - 0 ,2월 - 1 ....)
 	 * @param year
 	 * @param month
 	 * @return
 	 */
 	public static Calendar getCalendar(int year,int month)
     {
         Calendar cal = new GregorianCalendar(year,month,1);
         return cal;
     }
 	
 	/**
 	 * 특정 년, 월,일의 calendar를 가져옴 (주의 : month 는 0부터 시작 ex) 1월 - 0 ,2월 - 1 ....)
 	 * @param year
 	 * @param month
 	 * @param day
 	 * @return
 	 */
 	public static Calendar getCalendar(int year,int month,int day)
     {
         Calendar cal = new GregorianCalendar(year,month,day);
         return cal;
     }
 	
 	public static Calendar getCalendar(int year,int month,int day,int hour)
     {
         Calendar cal = new GregorianCalendar(year,month,day,hour,0);
         return cal;
     }
 	
 	public static Calendar getCalendar(int year,int month,int day,int hour ,int minute)
     {
         Calendar cal = new GregorianCalendar(year,month,day,hour,minute);
         return cal;
     }
 	
 	/**
 	 * 오늘 날짜를 넘어온 dateForm에 따라 리턴 시켜줌 ex) getToday("YYYY년 MM월 DD일") -> "2011년 11월 28일" 리턴
 	 * @param dateForm
 	 * @return
 	 */
 	public static String getToday(String dateForm)
     {
 		String date=dateForm;
         Calendar cal = getCalendar();
         //cal.add(2, 1);
         String year=dayIntToString(cal.get(1));
         String month=dayIntToString(cal.get(2)+1);
         String day=dayIntToString(cal.get(5));
         String hour=dayIntToString(cal.get(11));
         date=date.replaceAll("YYYY", year);
         date=date.replaceAll("MM", month);
         date=date.replaceAll("DD", day);
         date=date.replaceAll("HH", hour);
         return date;
     }
 	
 	/**
 	 * 넘어온 date를 dateForm에 맞게 변형 시켜줌( 주의: date는 YYYYMMDD 형식이여야함)
 	 * ex) getDayString("20111128","YYYY-MM-DD") -> 2011-11-28 리턴
 	 * @param date
 	 * @param dateForm
 	 * @return
 	 */
 	public static String getDayString(String date,String dateForm)
     {
 		String result=dateForm;
 		if(date!=null){
 			if(date.length()>7){
         		String year=date.substring(0, 4);
         		String month=date.substring(4, 6);
         		String day=date.substring(6, 8);
         	 	result=result.replaceAll("YYYY", year);
              	result=result.replaceAll("MM", month);
              	result=result.replaceAll("DD", day);
              	return result;
         	}else{
         		return date;
         	}
 		}else{
 			return date;
 		}
         
     }
 	
 	/**
 	 * 주어진 Calendar의 날짜를 넘어온 dateForm에 따라 리턴해줌
 	 * @param calendar
 	 * @param dateForm
 	 * @return
 	 */
 	public static String getDateString(Calendar calendar,String dateForm){
 		String date=dateForm;
         Calendar cal = calendar;
         String year=dayIntToString(cal.get(1));
         String month=dayIntToString(cal.get(2)+1);
         String day=dayIntToString(cal.get(5));
         String hour=dayIntToString(cal.get(11));
         date=date.replaceAll("YYYY", year);
         date=date.replaceAll("MM", month);
         date=date.replaceAll("DD", day);
         date=date.replaceAll("HH", hour);
         return date;
 	}
 	
 	/**
 	 * 오늘의 요일을 구하는 함수
 	 * @return
 	 */
 	public static String getTodayDay(){
         Calendar cal = getCalendar();
         return weekDay(cal.get(Calendar.DAY_OF_WEEK));
 	}
 	
 	/**
 	 * 넘어온 date의 요일을 구하는 함수 (주의 : date는 YYYYMMDD 형식이여야함)
 	 * @param date
 	 * @return
 	 */
 	public static String getDayName(String date){
 		int year=Integer.parseInt(date.substring(0,4));
 		int month=Integer.parseInt(date.substring(4,6))-1;
 		int day=Integer.parseInt(date.substring(6,8));
 		Calendar cal = getCalendar();
 		cal.set(year, month, day);
 		 return weekDay(cal.get(Calendar.DAY_OF_WEEK));
 	}
 	
 	/**
 	 * 요일을 int값을 한글로 변환
 	 * @param dayOfWeek
 	 * @return
 	 */
 	public static String weekDay(int dayOfWeek) {
 		return dayNames[dayOfWeek-1];
 	}
 
 	/**
 	 * 10일 미만의 날은 앞에 0을 붙여주는 함수(2자리수 유지를 위함)
 	 * @param day
 	 * @return
 	 */
 	public static String dayIntToString(int day){
 		if(day<10){
 			return "0"+String.valueOf(day);
 		}else{
 			return String.valueOf(day);
 		}
 	}
 	
 
 	
 	/**
 	 * 이전달 or 다음달을 구해줌, plusMinus : 0 -> 이번달을 리턴 , plusMinus : 1 -> 한달뒤
 	 * plusMinus:2 -> 두달뒤, plusMinus: -1 -> 한달 전 ....
 	 * @param year
 	 * @param month
 	 * @param plusMinus
 	 * @param dateForm
 	 * @return
 	 */
 	public static String getBeforeOrNextMonth(int year,int month,int plusMinus){
 		return getDateString(getCalendar(year,month-1+plusMinus),"YYYYMM");
 	}
 	
 	public static String getBeforeOrNextDay(int year,int month,int day,int plusMinus){
 		return getDateString(getCalendar(year,month-1,day+plusMinus),"YYYYMMDD");
 	}
 	
 	public static String getBeforeOrNextHour(int year,int month,int day,int hour,int plusMinus){
 		int sum=hour+plusMinus;
 		int tempDay=day;
 		int tempHour=0;
 		if(sum>=0){
 			tempDay+=(sum/24);
 			tempHour=sum%24;
 		}else{
 			tempDay--;
 			tempDay+=(sum/24);
 			tempHour=24+sum%24;
 		}
 		return getDateString(getCalendar(year,month-1,tempDay,tempHour),"YYYYMMDDHH");
 	}
 	
 	
 	
 	
 
 	public static String getFormedDate(String day){
 		if(day!=null){
 			String month=day.substring(4,6);
 			String date=day.substring(6,8);
 			Integer monthInt=Integer.parseInt(month);
 			Integer dateInt=Integer.parseInt(date);
 			month=monthInt<10?String.valueOf(monthInt):month;
 			date=dateInt<10?String.valueOf(dateInt):date;
 			return month+"월 "+date+"일("+getDayName(day)+")";
 		}else{
 			return "";
 		}
 	}
 
 }
