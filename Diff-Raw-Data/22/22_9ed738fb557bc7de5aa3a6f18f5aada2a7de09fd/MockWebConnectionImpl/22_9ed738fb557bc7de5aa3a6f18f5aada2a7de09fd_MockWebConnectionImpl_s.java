 /**
  * 
  */
 package com.edwise.dedicamns.connections.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 import android.app.Activity;
 import android.util.Log;
 
 import com.edwise.dedicamns.beans.ActivityDay;
 import com.edwise.dedicamns.beans.DayRecord;
 import com.edwise.dedicamns.beans.MonthListBean;
 import com.edwise.dedicamns.connections.ConnectionException;
 import com.edwise.dedicamns.connections.WebConnection;
 import com.edwise.dedicamns.utils.DayUtils;
 
 /**
  * @author edwise
  * 
  */
 public class MockWebConnectionImpl implements WebConnection {
 
     private List<String> arrayProjects = null;
     private List<String> months = null;
     private List<String> years = null;
 
     /*
      * (non-Javadoc)
      * 
      * @see com.edwise.dedicamns.connections.WebConnection#isOnline(android.app.Activity )
      */
     public boolean isOnline(Activity activity) {
 	return true;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.edwise.dedicamns.connections.WebConnection#connectWeb(java.lang.String , java.lang.String)
      */
     public Integer connectWeb(String userName, String password) {
 	try {
	    TimeUnit.SECONDS.sleep(3);
 	} catch (InterruptedException e) {
 	    Log.e(MockWebConnectionImpl.class.toString(), "connectWeb: Error en TimeUnit...", e);
 	    e.printStackTrace();
 	}
 	return 200;
     }
 
     public List<String> getMonths() {
 	return months;
     }
 
     public List<String> getYears() {
 	return years;
     }
 
     public List<String> getArrayProjects() {	
 	return arrayProjects;
     }
 
     public List<String> getArraySubProjects(String projectId) {
 	List<String> arraySubProjects = new ArrayList<String>();
 	if (projectId != null && projectId.equals("BBVA58")) {
 	    arraySubProjects.add("1 - Tarea mierda");
 	    arraySubProjects.add("2 - Marronacos");
 	    arraySubProjects.add("3 - Calentar silla");
 	} else {
 	    arraySubProjects.add("0 - Sin cuenta");
 	}
 
 	return arraySubProjects;
     }
 
     public void fillProyectsAndSubProyectsCached() {
 	try {
 	    TimeUnit.SECONDS.sleep(3);
 	} catch (InterruptedException e) {
 	    Log.e(MockWebConnectionImpl.class.toString(), "fillListMock: Error en TimeUnit...", e);
 	    e.printStackTrace();
 	}
 	
 	arrayProjects = new ArrayList<String>();
 
 	arrayProjects.add("Selecciona proyecto...");
 	arrayProjects.add("BBVA58");
 	arrayProjects.add("Educared09");
 	arrayProjects.add("BBVA68");
 	arrayProjects.add("NIELHUEVO32");
 	arrayProjects.add("ISBAN12");
     }
 
     public void fillMonthsAndYearsCached() {
 	months = new ArrayList<String>();
 	months.add("Enero");
 	months.add("Febrero");
 	months.add("Marzo");
 	months.add("Abril");
 	months.add("Mayo");
 	months.add("Junio");
 	months.add("Julio");
 	months.add("Agosto");
 	months.add("Septiembre");
 	months.add("Octubre");
 	months.add("Noviembre");
 	months.add("Diciembre");
 
 	years = new ArrayList<String>();
 	years.add("2011");
 	years.add("2012");
 	years.add("2013");
     }
 
     public MonthListBean getListDaysAndActivitiesForCurrentMonth() {
 	List<DayRecord> list = new ArrayList<DayRecord>();
 
 	fillListMock(list, true);
 
 	MonthListBean monthList = new MonthListBean("Noviembre", "2012", list);
 
 	return monthList;
     }
 
     public List<DayRecord> getListDaysAndActivitiesForMonthAndYear(int month, String year, boolean withActivities) {
 	List<DayRecord> list = new ArrayList<DayRecord>();
 
 	fillListMock(list, withActivities);
 
 	return list;
     }
 
     private void fillListMock(List<DayRecord> list, boolean withActivity) {
 	try {
 	    TimeUnit.SECONDS.sleep(3);
 	} catch (InterruptedException e) {
 	    Log.e(MockWebConnectionImpl.class.toString(), "fillListMock: Error en TimeUnit...", e);
 	    e.printStackTrace();
 	}
 	
 	for (int i = 1; i < 31; i++) {
 	    DayRecord dayRecord = new DayRecord();
 	    dayRecord.setDayNum(i);
 	    dayRecord.setDayName(generateDayName(i));
 	    dayRecord.setIsHoliday(false);
 	    if (DayUtils.isWeekend(dayRecord.getDayName())) {
 		dayRecord.setIsWeekend(true);
 	    }
 
 	    if (i < 10 && !DayUtils.isWeekend(dayRecord.getDayName())) {
 		dayRecord.setHours("08:30");
 
 		if (withActivity) {
 		    ActivityDay activityDay = new ActivityDay();
 		    activityDay.setHours("08:30");
 		    activityDay.setProjectId("BBVA58");
 		    activityDay.setSubProject("3 - Calentar silla");
 		    activityDay.setSubProjectId("3");
 		    activityDay.setUpdate(true);
 		    activityDay.setIdActivity(Math.random() + "");
 		    dayRecord.getActivities().add(activityDay);
 		}
 	    } else if (i == 15) {
 		dayRecord.setHours("16:30");
 		
 		if (withActivity) {
 		    ActivityDay activityDay = new ActivityDay();
 		    activityDay.setHours("06:30");
 		    activityDay.setProjectId("BBVA58");
 		    activityDay.setSubProject("3 - Calentar silla");
 		    activityDay.setSubProjectId("3");
 		    activityDay.setUpdate(true);
 		    activityDay.setIdActivity(Math.random() + "");
 		    dayRecord.getActivities().add(activityDay);
 		    
 		    ActivityDay activityDay2 = new ActivityDay();
 		    activityDay2.setHours("05:00");
 		    activityDay2.setProjectId("BBVA58");
 		    activityDay2.setSubProject("2 - Marronacos");
 		    activityDay2.setSubProjectId("2");
 		    activityDay2.setUpdate(true);
 		    activityDay2.setIdActivity(Math.random() + "");
 		    dayRecord.getActivities().add(activityDay2);
 		    
 		    ActivityDay activityDay3 = new ActivityDay();
 		    activityDay3.setHours("04:30");
 		    activityDay3.setProjectId("Educared09");
 		    activityDay3.setSubProject("0 - Sin cuenta");
 		    activityDay3.setSubProjectId("0");
 		    activityDay3.setTask("Hacer el tonto");
 		    activityDay3.setUpdate(true);
 		    activityDay3.setIdActivity(Math.random() + "");
 		    dayRecord.getActivities().add(activityDay3);
 		}
 	    }  else if (i == 16) {
 		dayRecord.setHours("23:30");
 		
 		if (withActivity) {
 		    ActivityDay activityDay = new ActivityDay();
 		    activityDay.setHours("01:30");
 		    activityDay.setProjectId("BBVA58");
 		    activityDay.setSubProject("3 - Calentar silla");
 		    activityDay.setSubProjectId("3");
 		    activityDay.setUpdate(true);
 		    activityDay.setIdActivity(Math.random() + "");
 		    dayRecord.getActivities().add(activityDay);
 		    
 		    ActivityDay activityDay2 = new ActivityDay();
 		    activityDay2.setHours("04:00");
 		    activityDay2.setProjectId("BBVA58");
 		    activityDay2.setSubProject("2 - Marronacos");
 		    activityDay2.setSubProjectId("2");
 		    activityDay2.setUpdate(true);
 		    activityDay2.setIdActivity(Math.random() + "");
 		    dayRecord.getActivities().add(activityDay2);
 		    
 		    ActivityDay activityDay3 = new ActivityDay();
 		    activityDay3.setHours("04:30");
 		    activityDay3.setProjectId("Educared09");
 		    activityDay3.setSubProject("0 - Sin cuenta");
 		    activityDay3.setSubProjectId("0");
 		    activityDay3.setTask("Hacer el tonto");
 		    activityDay3.setUpdate(true);
 		    activityDay3.setIdActivity(Math.random() + "");
 		    dayRecord.getActivities().add(activityDay3);
 		    
 		    ActivityDay activityDay4 = new ActivityDay();
 		    activityDay4.setHours("03:30");
 		    activityDay4.setProjectId("ISBAN12");
 		    activityDay4.setSubProject("0 - Sin cuenta");
 		    activityDay4.setSubProjectId("0");
 		    activityDay4.setUpdate(true);
 		    activityDay4.setIdActivity(Math.random() + "");
 		    dayRecord.getActivities().add(activityDay4);
 		    
 		    ActivityDay activityDay5 = new ActivityDay();
 		    activityDay5.setHours("02:00");
 		    activityDay5.setProjectId("BBVA58");
 		    activityDay5.setSubProject("2 - Marronacos");
 		    activityDay5.setSubProjectId("2");
 		    activityDay5.setUpdate(true);
 		    activityDay5.setIdActivity(Math.random() + "");
 		    dayRecord.getActivities().add(activityDay5);
 		    
 		    ActivityDay activityDay6 = new ActivityDay();
 		    activityDay6.setHours("01:30");
 		    activityDay6.setProjectId("Educared09");
 		    activityDay6.setSubProject("0 - Sin cuenta");
 		    activityDay6.setSubProjectId("0");
 		    activityDay6.setTask("Hacer el tonto");
 		    activityDay6.setUpdate(true);
 		    activityDay6.setIdActivity(Math.random() + "");
 		    dayRecord.getActivities().add(activityDay6);
 		    
 		    ActivityDay activityDay7 = new ActivityDay();
 		    activityDay7.setHours("03:30");
 		    activityDay7.setProjectId("BBVA58");
 		    activityDay7.setSubProject("1 - Tarea mierda");
 		    activityDay7.setSubProjectId("1");
 		    activityDay7.setUpdate(true);
 		    activityDay7.setIdActivity(Math.random() + "");
 		    dayRecord.getActivities().add(activityDay7);
 		    
 		    ActivityDay activityDay8 = new ActivityDay();
 		    activityDay8.setHours("01:00");
 		    activityDay8.setProjectId("BBVA58");
 		    activityDay8.setSubProject("2 - Marronacos");
 		    activityDay8.setSubProjectId("2");
 		    activityDay8.setUpdate(true);
 		    activityDay8.setIdActivity(Math.random() + "");
 		    dayRecord.getActivities().add(activityDay8);
 		    
 		    ActivityDay activityDay9 = new ActivityDay();
 		    activityDay9.setHours("01:30");
 		    activityDay9.setProjectId("Educared09");
 		    activityDay9.setSubProject("0 - Sin cuenta");
 		    activityDay9.setSubProjectId("0");
 		    activityDay9.setTask("Hacer el tonto");
 		    activityDay9.setUpdate(true);
 		    activityDay9.setIdActivity(Math.random() + "");
 		    dayRecord.getActivities().add(activityDay9);
 		    
 		    ActivityDay activityDay10 = new ActivityDay();
 		    activityDay10.setHours("00:30");
 		    activityDay10.setProjectId("Educared09");
 		    activityDay10.setSubProject("0 - Sin cuenta");
 		    activityDay10.setSubProjectId("0");
 		    activityDay10.setTask("Hacer el tonto más");
 		    activityDay10.setUpdate(true);
 		    activityDay10.setIdActivity(Math.random() + "");
 		    dayRecord.getActivities().add(activityDay10);
 		    
 		}
 	    } else {
 		dayRecord.setHours("00:00");
 	    }
 
 	    list.add(dayRecord);
 	}
     }
 
     final static String[] dayNames = { "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom" };
 
     private String generateDayName(int i) {
 	String dayName = null;
 
 	if (i <= 7) {
 	    dayName = dayNames[i - 1];
 	} else {
 	    int ind = i % 7;
 	    if (ind == 0) {
 		dayName = dayNames[6];
 	    } else {
 		dayName = dayNames[ind - 1];
 	    }
 	}
 
 	return dayName;
     }
 
     public Integer saveDay(ActivityDay activityDay, String dateForm, int dayNum) {
 	try {
 	    TimeUnit.SECONDS.sleep(3);
 	} catch (InterruptedException e) {
 	    Log.e(MockWebConnectionImpl.class.toString(), "fillListMock: Error en TimeUnit...", e);
 	    e.printStackTrace();
 	}
 	
 	if (!activityDay.isUpdate()) {
 	    activityDay.setIdActivity(Math.random() + "");
 	}
 	return 1;
     }
 
     public Integer removeDay(ActivityDay activityDay) {
 	try {
 	    TimeUnit.SECONDS.sleep(3);
 	} catch (InterruptedException e) {
 	    Log.e(MockWebConnectionImpl.class.toString(), "fillListMock: Error en TimeUnit...", e);
 	    e.printStackTrace();
 	}
 	
 	return 1;
     }
 
     public Integer saveDayBatch(DayRecord dayRecord) throws ConnectionException {
 	try {
 	    TimeUnit.SECONDS.sleep(1);
 	} catch (InterruptedException e) {
 	    Log.e(MockWebConnectionImpl.class.toString(), "saveDayBatch: Error en TimeUnit...", e);
 	    e.printStackTrace();
 	}
 	return 1;
     }
 
 }
