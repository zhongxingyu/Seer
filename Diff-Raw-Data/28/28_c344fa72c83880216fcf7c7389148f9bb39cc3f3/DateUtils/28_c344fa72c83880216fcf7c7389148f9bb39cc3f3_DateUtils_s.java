 /**
  *  Copyright 2010 Society for Health Information Systems Programmes, India (HISP India)
  *
  *  This file is part of Hospital-core module.
  *
  *  Hospital-core module is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
 
  *  Hospital-core module is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Hospital-core module.  If not, see <http://www.gnu.org/licenses/>.
  *
  **/
 
 
 
 package org.openmrs.module.hospitalcore.util;
 
 import java.sql.Timestamp;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * <p> Class: DateUtils </p>
  * <p> Package: com.mss.cms.utils.cronjob </p> 
  * <p> Author: Nguyen manh chuyen </p>
  * <p> Update by: Nguyen manh chuyen </p>
  * <p> Version: $1.0 </p>
  * <p> Create date: Jun 17, 2009 10:36:05 AM </p>
  * <p> Update date: Jun 17, 2009 10:36:05 AM </p>
  **/
 public class DateUtils {
 	private static SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
 	private static SimpleDateFormat DDMMYYYY_HHMM = new SimpleDateFormat("ddMMyyyy-HHmm");
 	private static SimpleDateFormat formatterDDMMYYYY = new SimpleDateFormat("dd/MM/yyyy");
 	private static SimpleDateFormat formatterYYYYMMDD = new SimpleDateFormat("yyyyMMdd");
 	private static SimpleDateFormat formatterYYYYMMDDEx = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss");
 	private static SimpleDateFormat formatterYYYY = new SimpleDateFormat("yyyy");
 	private static SimpleDateFormat formatterMM = new SimpleDateFormat("MM");
 	private static SimpleDateFormat formatterDDMMYYYYEx = new SimpleDateFormat("yyyy-MM-dd");
 	/**
 	 * <p> Method: main() </p>
 	 * <p> Objective: main's objective </p>
 	 * <p> Params:  </p> 
 	 * <p> Return: void </p>
 	 * <p> Author: Nguyen manh chuyen </p>
 	 * <p> Update by: Nguyen manh chuyen </p>
 	 * <p> Version: $1.0 </p>
 	 * <p> Create date: Jan 13, 2009 9:32:36 AM </p>
 	 * <p> Update date: Jan 13, 2009 9:32:36 AM </p>
 	 **/
 	  
 
 	public static void main(String[] args)throws Exception {
 
 
 	//System.out.println(timestampPlusDay(createTimestamp(), -2));	
 		Date tt = getDateFromStr("13/12/2020");
 		System.out.println(getAgeFromBirthday(tt));
 		
 
 		
 	}
 	
 	public static Integer getAgeFromBirthday(Date birthday){
 		if(birthday == null){
 			return null;
 		}
 		// Create a calendar object with the date of birth
 		Calendar dateOfBirth =  Calendar.getInstance();
 		dateOfBirth.setTime(birthday);
 		// Create a calendar object with today's date
 		Calendar today = Calendar.getInstance();
 
 		// Get age based on year
 		int age = today.get(Calendar.YEAR) - dateOfBirth.get(Calendar.YEAR);
 
 		// Add the tentative age to the date of birth to get this year's birthday
 		dateOfBirth.add(Calendar.YEAR, age);
 
 		// If this year's birthday has not happened yet, subtract one from age
 		if (today.before(dateOfBirth)) {
 		    age--;
 		}
 		
 		return age;
 	} 
 	public static long getDateStr(String date) {
 		long result = 0;
 	    try {
 	      SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
 	      sdf.setLenient(false);
 	      result =  sdf.parse(date).getTime();
 	    }
 	    catch (Exception e) {
 	     System.out.println("Error: "+e.toString());
 	    }
 	    
 	    return result;
 	    }
 	public static Date getDateFromStr(String date) {
 		Date result = null;
 	    try {
 	      SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
 	      sdf.setLenient(false);
 	      result =  sdf.parse(date);
 	    }
 	    catch (Exception e) {
 	     System.out.println("Error: "+e.toString());
 	    }
 	    
 	    return result;
 	    }
 	
 	public static boolean isValidDateStr(String date, String format) {
 		
 	    try {
 	    	if(date.length() != format.length()){
 	    		return false;
 	    	}
 	      SimpleDateFormat sdf = new SimpleDateFormat(format);
 	      sdf.setLenient(false);
 	      sdf.parse(date);
 	    }
 	    catch (Exception e) {
 	      return false;
 	    }
 	    
 	    return true;
 	    }
 
 	public static long getCurrentDate(){
 		return Calendar.getInstance().getTimeInMillis();
 	}
 	public static String getYYYYMMDD(){
 		return formatterYYYYMMDD.format(Calendar.getInstance().getTime());
 	}
 	public static String getYYYYMMDDEx(){
 		return formatterYYYYMMDDEx.format(Calendar.getInstance().getTime());
 	}
 	public static String getYYYYMMDDHHMM(){
 		return formatter.format(Calendar.getInstance().getTime());
 	}
 	public static String getDDMMYYYY(){
 		return formatterDDMMYYYY.format(Calendar.getInstance().getTime());
 	}
 	public static String getDDMMYYY_HHMM(){
 		return DDMMYYYY_HHMM.format(Calendar.getInstance().getTime());
 	}
 	public static String getYYYY(){
 		return formatterYYYY.format(Calendar.getInstance().getTime());
 	}
 	public static String getMM(){
 		return formatterMM.format(Calendar.getInstance().getTime());
 	}
 	public static String getMorMM(){
 		String ret = formatterMM.format(Calendar.getInstance().getTime());		
 		return (ret.startsWith("0")?ret.substring(1, 2):ret);
 	}
 	
 	public static String convertFromLongToString(SimpleDateFormat fm,long date){
 		return fm.format(date);
 	}
 	public static java.sql.Timestamp createTimestamp() {
         java.util.Calendar calendar = java.util.Calendar.getInstance();
         return new java.sql.Timestamp((calendar.getTime()).getTime());
     }
 
     public static java.sql.Timestamp createDateTimestamp(java.util.Date date) {
         java.util.Calendar calendar = java.util.Calendar.getInstance();
         calendar.setTime(date);
         return new java.sql.Timestamp((calendar.getTime()).getTime());
     }
 
     public static java.sql.Timestamp String2Timestamp(String strInputDate) {
         String strDate = strInputDate;
         int i, nYear, nMonth, nDay;
         String strSub = null;
         i = strDate.indexOf("/");
         if (i < 0) {
             return createTimestamp();
         }
         strSub = strDate.substring(0, i);
         nDay = (new Integer(strSub.trim())).intValue();
         strDate = strDate.substring(i + 1);
         i = strDate.indexOf("/");
         if (i < 0) {
             return createTimestamp();
         }
         strSub = strDate.substring(0, i);
         nMonth = (new Integer(strSub.trim())).intValue() - 1; // Month begin from 0 value
         strDate = strDate.substring(i + 1);
         if (strDate.length() < 4) {
             if (strDate.substring(0, 1).equals("9")) {
                 strDate = "19" + strDate.trim();
             } else {
                 strDate = "20" + strDate.trim();
             }
         }
         nYear = (new Integer(strDate)).intValue();
         java.util.Calendar calendar = java.util.Calendar.getInstance();
         calendar.set(nYear, nMonth, nDay);
         return new java.sql.Timestamp((calendar.getTime()).getTime());
     }
 
 
     public static String getDateTimeString(java.sql.Timestamp ts) {
         if (ts == null) {
             return "";
         }
         return Timestamp2DDMMYYYY(ts) + " " + Timestamp2HHMMSS(ts, 1);
     }
 
     /*return date with format: dd/mm/yyyy */
     public static String getDateString(java.sql.Timestamp ts) {
         if (ts == null) {
             return "";
         }
         return Timestamp2DDMMYYYY(ts);
     }
 
     public static String getTimeString(java.sql.Timestamp ts) {
         java.util.Calendar calendar = java.util.Calendar.getInstance();
         calendar.setTime(new java.util.Date(ts.getTime()));
         return calendar.get(calendar.HOUR_OF_DAY) + ":" +
                 calendar.get(calendar.MINUTE) + ":" +
                 calendar.get(calendar.SECOND);
     }
 
     /*return date with format: dd/mm/yyyy */
     public static String Timestamp2DDMMYYYY(java.sql.Timestamp ts) {
         if (ts == null) {
             return "";
         } else {
             java.util.Calendar calendar = java.util.Calendar.getInstance();
             calendar.setTime(new java.util.Date(ts.getTime()));
 
             String strTemp = Integer.toString(calendar.get(calendar.
                     DAY_OF_MONTH));
             if (calendar.get(calendar.DAY_OF_MONTH) < 10) {
                 strTemp = "0" + strTemp;
             }
             if (calendar.get(calendar.MONTH) + 1 < 10) {
                 return strTemp + "/0" + (calendar.get(calendar.MONTH) + 1) +
                         "/" + calendar.get(calendar.YEAR);
             } else {
                 return strTemp + "/" + (calendar.get(calendar.MONTH) + 1) + "/" +
                         calendar.get(calendar.YEAR);
             }
         }
     }
 
     /*return date with format: mm/dd/yyyy */
     public String Timestamp2MMDDYYYY(java.sql.Timestamp ts) {
         if (ts == null) {
             return "";
         } else {
             java.util.Calendar calendar = java.util.Calendar.getInstance();
             calendar.setTime(new java.util.Date(ts.getTime()));
 
             String strTemp = Integer.toString(calendar.get(calendar.
                     DAY_OF_MONTH));
             if (calendar.get(calendar.DAY_OF_MONTH) < 10) {
                 strTemp = "0" + strTemp;
             }
             if (calendar.get(calendar.MONTH) + 1 < 10) {
                 return "0" + (calendar.get(calendar.MONTH) + 1) + "/" + strTemp +
                         "/" + calendar.get(calendar.YEAR);
             } else {
                 return (calendar.get(calendar.MONTH) + 1) + "/" + strTemp + "/" +
                         +calendar.get(calendar.YEAR);
             }
         }
     }
 
     /*return date with format: dd/mm/yy */
     public String Timestamp2DDMMYY(java.sql.Timestamp ts) {
         int endYear;
         if (ts == null) {
             return "";
         } else {
             java.util.Calendar calendar = java.util.Calendar.getInstance();
             calendar.setTime(new java.util.Date(ts.getTime()));
 
             String strTemp = Integer.toString(calendar.get(calendar.
                     DAY_OF_MONTH));
             endYear = calendar.get(calendar.YEAR) % 100;
             if (calendar.get(calendar.DAY_OF_MONTH) < 10) {
                 strTemp = "0" + strTemp;
             }
             if (calendar.get(calendar.MONTH) + 1 < 10) {
                 if (endYear < 10) {
                     return strTemp + "/0" + (calendar.get(calendar.MONTH) + 1) +
                             "/0" + endYear;
                 } else {
                     return strTemp + "/0" + (calendar.get(calendar.MONTH) + 1) +
                             "/" + endYear;
                 }
             } else {
                 if (endYear < 10) {
                     return strTemp + "/" + (calendar.get(calendar.MONTH) + 1) +
                             "/0" + endYear;
                 } else {
                     return strTemp + "/" + (calendar.get(calendar.MONTH) + 1) +
                             "/" + endYear;
                 }
             }
         }
     }
 
     /*return date with format: d/m/yy */
     public String Timestamp2DMYY(java.sql.Timestamp ts) {
         int endYear = 0;
         if (ts == null) {
             return "";
         } else {
             java.util.Calendar calendar = java.util.Calendar.getInstance();
             calendar.setTime(new java.util.Date(ts.getTime()));
             endYear = calendar.get(calendar.YEAR) % 100;
             String strTemp = Integer.toString(calendar.get(calendar.
                     DAY_OF_MONTH));
             if (endYear < 10) {
                 return strTemp + "/" + (calendar.get(calendar.MONTH) + 1) +
                         "/0" + endYear;
             } else {
                 return strTemp + "/" + (calendar.get(calendar.MONTH) + 1) + "/" +
                         endYear;
             }
         }
     }
 
     /*return date with format: d/m/yyyy */
     public String Timestamp2DMYYYY(java.sql.Timestamp ts) {
         if (ts == null) {
             return "";
         } else {
             java.util.Calendar calendar = java.util.Calendar.getInstance();
             calendar.setTime(new java.util.Date(ts.getTime()));
 
             String strTemp = Integer.toString(calendar.get(calendar.
                     DAY_OF_MONTH));
             return strTemp + "/" + (calendar.get(calendar.MONTH) + 1) + "/" +
                     calendar.get(calendar.YEAR);
         }
     }
 
     /*return date with format: YYYY/MM/DD to sort*/
     public String Timestamp2YYYYMMDD(java.sql.Timestamp ts) {
         int endYear;
         if (ts == null) {
             return "";
         } else {
             java.util.Calendar calendar = java.util.Calendar.getInstance();
             calendar.setTime(new java.util.Date(ts.getTime()));
 
             String strTemp = Integer.toString(calendar.get(calendar.
                     DAY_OF_MONTH));
             endYear = calendar.get(calendar.YEAR);
             if (calendar.get(calendar.DAY_OF_MONTH) < 10) {
                 strTemp = "0" + strTemp;
             }
             if (calendar.get(calendar.MONTH) + 1 < 10) {
                 return endYear + "/0" + (calendar.get(calendar.MONTH) + 1) +
                         "/" + strTemp;
             } else {
                 return endYear + "/" + (calendar.get(calendar.MONTH) + 1) + "/" +
                         strTemp;
             }
         }
     }
 
     public static String Timestamp2HHMMSS(java.sql.Timestamp ts, int iStyle) {
         if (ts == null) {
             return "";
         }
         java.util.Calendar calendar = java.util.Calendar.getInstance();
         calendar.setTime(new java.util.Date(ts.getTime()));
 
         String strTemp;
         if (iStyle == 0) {
             strTemp = Integer.toString(calendar.get(calendar.HOUR_OF_DAY));
         } else {
             strTemp = Integer.toString(calendar.get(calendar.HOUR_OF_DAY));
         }
 
         if (strTemp.length() < 2) {
             strTemp = "0" + strTemp;
         }
         if (calendar.get(calendar.MINUTE) < 10) {
             strTemp += ":0" + calendar.get(calendar.MINUTE);
         } else {
             strTemp += ":" + calendar.get(calendar.MINUTE);
         }
         if (calendar.get(calendar.SECOND) < 10) {
             strTemp += ":0" + calendar.get(calendar.SECOND);
         } else {
             strTemp += ":" + calendar.get(calendar.SECOND);
         }
 
         if (iStyle != 0) {
             if (calendar.get(calendar.AM_PM) == calendar.AM) {
                 strTemp += " AM";
             } else {
                 strTemp += " PM";
             }
         }
         return strTemp;
     }
 
     /**
      *  return date time used for 24 hour clock
      */
     public String getDateTime24hString(java.sql.Timestamp ts) {
         if (ts == null) {
             return "";
         }
         return Timestamp2DDMMYYYY(ts) + " " + Timestamp2HHMMSS(ts, 0);
     }
 
     /**
      *  return date time used for 12 hour clock
      */
     public String getDateTime12hString(java.sql.Timestamp ts) {
         if (ts == null) {
             return "";
         }
         return Timestamp2DDMMYYYY(ts) + " " + Timestamp2HHMMSS(ts, 1);
     }
 
     /**
      *   return string dd/mm/yyyy from a Timestamp + a addtional day
      * @param ts
      * @param iDayPlus    number of day to add
      * @return
      */
     public static String timestampPlusDay2DDMMYYYY(java.sql.Timestamp ts, int iDayPlus) {
         if (ts == null) {
             return "";
         }
         java.util.Calendar calendar = java.util.Calendar.getInstance();
         calendar.setTime(new java.util.Date(ts.getTime()));
         int iDay = calendar.get(calendar.DAY_OF_MONTH);
         calendar.set(calendar.DAY_OF_MONTH, iDay + iDayPlus);
 
         java.sql.Timestamp tsNew = new java.sql.Timestamp((calendar.getTime()).
                 getTime());
         return Timestamp2DDMMYYYY(tsNew);
     }
     public static long timestampPlusDay(java.sql.Timestamp ts, int iDayPlus) {
         if (ts == null) {
             return -1;
         }
         java.util.Calendar calendar = java.util.Calendar.getInstance();
         calendar.setTimeInMillis(ts.getTime());
         int iDay = calendar.get(calendar.DAY_OF_MONTH);
         calendar.set(calendar.DAY_OF_MONTH, iDay + iDayPlus);
         java.sql.Timestamp tsNew = new java.sql.Timestamp((calendar.getTime()).getTime());
         return tsNew.getTime();
     }
     
     public static String getDateFromRange(String date, int rangeDate){
     	java.util.Calendar calendar = java.util.Calendar.getInstance();
         calendar.setTime(getDateFromStr(date));
         int iDay = calendar.get(calendar.DAY_OF_MONTH);
 	    calendar.set(calendar.DAY_OF_MONTH, iDay + rangeDate);
 	    return formatterDDMMYYYYEx.format(calendar.getTime());
     	
     }
 
     public Timestamp getPreviousDate(Timestamp ts) {
         if (ts == null) {
             return null;
         }
         java.util.Calendar calendar = java.util.Calendar.getInstance();
         calendar.setTime(new java.util.Date(ts.getTime()));
         int iDay = calendar.get(calendar.DAY_OF_MONTH);
         calendar.set(calendar.DAY_OF_MONTH, iDay - 1);
 
         java.sql.Timestamp tsNew = new java.sql.Timestamp((calendar.getTime()).
                 getTime());
         return tsNew;
     }
 
     public Timestamp getNextDate(Timestamp ts) {
         if (ts == null) {
             return null;
         }
         java.util.Calendar calendar = java.util.Calendar.getInstance();
         calendar.setTime(new java.util.Date(ts.getTime()));
         int iDay = calendar.get(calendar.DAY_OF_MONTH);
         calendar.set(calendar.DAY_OF_MONTH, iDay + 1);
 
         java.sql.Timestamp tsNew = new java.sql.Timestamp((calendar.getTime()).
                 getTime());
         return tsNew;
     }
 
     public int getDayOfWeek(Timestamp ts) {
         if (ts == null) {
             return -1;
         }
         java.util.Calendar calendar = java.util.Calendar.getInstance();
         calendar.setTime(new java.util.Date(ts.getTime()));
         int iDay = calendar.get(calendar.DAY_OF_WEEK);
         return iDay;
     }
 
     /**
      * return the dd/mm/yyyy of current month
      *   eg:   05/2002  -->   31/05/2002
      *
      * @param strMonthYear  : input string mm/yyyy
      * @return
      */
     public String getLastestDateOfMonth(String strMonthYear) {
         String strDate = strMonthYear;
         int i, nYear, nMonth, nDay;
         String strSub = null;
 
         i = strDate.indexOf("/");
         if (i < 0) {
             return "";
         }
         strSub = strDate.substring(0, i);
         nMonth = (new Integer(strSub)).intValue(); // Month begin from 0 value
         strDate = strDate.substring(i + 1);
         nYear = (new Integer(strDate)).intValue();
 
         boolean leapyear = false;
         if (nYear % 100 == 0) {
             if (nYear % 400 == 0) {
                 leapyear = true;
             }
         } else
         if ((nYear % 4) == 0) {
             leapyear = true;
         }
 
         if (nMonth == 2) {
             if (leapyear) {
                 return "29/" + strDate;
             } else {
                 return "28/" + strDate;
             }
         } else {
             if ((nMonth == 1) || (nMonth == 3) || (nMonth == 5) || (nMonth == 7) ||
                 (nMonth == 8) || (nMonth == 10) || (nMonth == 12)) {
                 return "31/" + strDate;
             } else
             if ((nMonth == 4) || (nMonth == 6) || (nMonth == 9) ||
                 (nMonth == 11)) {
                 return "30/" + strDate;
             }
         }
         return "";
     }
 
     public Timestamp getFriday(Timestamp ts) {
         if (ts == null) {
             return null;
         }
 
         java.util.Calendar calendar = java.util.Calendar.getInstance();
         int iDoW = getDayOfWeek(ts);
         if (iDoW == calendar.SUNDAY) {
             iDoW = 8;
         }
         int k = calendar.FRIDAY - iDoW;
         calendar.setTime(new java.util.Date(ts.getTime()));
         int iDay = calendar.get(calendar.DAY_OF_MONTH);
         calendar.set(calendar.DAY_OF_MONTH, iDay + k);
         java.sql.Timestamp tsNew = new java.sql.Timestamp((calendar.getTime()).
                 getTime());
         return tsNew;
     }
 
     public boolean isFriday(Timestamp ts) {
         if (ts == null) {
             return false;
         }
         java.util.Calendar calendar = java.util.Calendar.getInstance();
         if (getDayOfWeek(ts) == calendar.FRIDAY) {
             return true;
         }
         return false;
     }
 
     public static Timestamp getTimestamp(String dateStr) {
         try {
             SimpleDateFormat dateFormat = new SimpleDateFormat(
                     "dd/MM/yyyy kk:mm:ss");
             Timestamp date = new Timestamp(dateFormat.parse(dateStr).getTime());
             return date;
         } catch (Exception ex) {
             System.out.println("Error: " + ex.toString());
             return null;
         }
     }
 
     public Timestamp getNextDateN(Timestamp ts, int n) {
         if (ts == null) {
             return null;
         }
         java.util.Calendar calendar = java.util.Calendar.getInstance();
         calendar.setTime(new java.util.Date(ts.getTime()));
         calendar.add(calendar.DAY_OF_MONTH, n);
         java.sql.Timestamp tsNew = new java.sql.Timestamp((calendar.getTime()).
                 getTime());
         return tsNew;
     }
 	
     public static int getDaysDiff(Date date1, Date date2)
     {
     Calendar cal = Calendar.getInstance();
     cal.setTime(date1);
 
     int d1 = cal.get(Calendar.DATE);
 
     cal.setTime(date2);
 
     int d2 = cal.get(Calendar.DATE);
 
     return d2 - d1;
     }
 	
    
 }
 
