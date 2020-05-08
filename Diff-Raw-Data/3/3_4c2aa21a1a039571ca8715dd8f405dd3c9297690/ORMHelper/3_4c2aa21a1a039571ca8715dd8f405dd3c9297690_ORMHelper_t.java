 package com.hcalendar.data.orm.impl;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.hcalendar.data.exception.BusinessException;
 import com.hcalendar.data.orm.exception.ORMException;
 import com.hcalendar.data.utils.DateHelper;
 import com.hcalendar.data.utils.DateIterator;
 import com.hcalendar.data.utils.exception.DateException;
 import com.hcalendar.data.xml.userconfiguration.ObjectFactory;
 import com.hcalendar.data.xml.userconfiguration.UserConfiguration;
 import com.hcalendar.data.xml.userconfiguration.UserConfiguration.User;
 import com.hcalendar.data.xml.userconfiguration.UserConfiguration.User.YearConf;
 import com.hcalendar.data.xml.userconfiguration.UserConfiguration.User.YearConf.FreeDays;
 import com.hcalendar.data.xml.userconfiguration.UserConfiguration.User.YearConf.FreeDays.FreeDay;
 import com.hcalendar.data.xml.userconfiguration.UserConfiguration.User.YearConf.WorkingDays;
 import com.hcalendar.data.xml.workedhours.AnualHours;
 import com.hcalendar.data.xml.workedhours.AnualHours.UserInput;
 import com.hcalendar.data.xml.workedhours.AnualHours.UserInput.Holidays;
 import com.hcalendar.data.xml.workedhours.AnualHours.UserInput.WorkedHours;
 
 /**
  * ORM operations helper class. Utility methos are here
  * */
 public class ORMHelper {
 
 	private static List<Date> freeDay2DateList(List<FreeDay> freedays) {
 		List<Date> result = new ArrayList<Date>();
 		for (FreeDay day : freedays) {
 			result.add(DateHelper.xmlGregorianCalendar2Date(day.getDay()));
 		}
 		return result;
 	}
 
 	private static void removeFromWorkedDays(AnualHours anualHours, Date date,
 			String profileName) {
 		List<WorkedHours> workedDays = getUsersWorkedHourList(anualHours,
 				profileName);
 		List<WorkedHours> copy = new ArrayList<WorkedHours>(workedDays);
 		for (WorkedHours day : copy) {
 			if (DateHelper.compareDates(date, day.getDate()) == 0)
 				workedDays.remove(day);
 		}
 
 	}
 
 	private static void removeFromHolidays(AnualHours anualHours, Date date,
 			String profileName) {
 		List<Holidays> holidays = getUserHolidaysList(anualHours, profileName);
 		List<Holidays> copy = new ArrayList<Holidays>(holidays);
 		for (Holidays day : copy) {
 			if (DateHelper.compareDates(date, day.getDate()) == 0)
 				holidays.remove(day);
 		}
 	}
 
 	@SuppressWarnings("deprecation")
 	private static void removeFromFreeDays(UserConfiguration userConfig,
 			Date date, String profileName) {
 		FreeDays freeDays = getCalendarFreeDays(userConfig, profileName,
 				date.getYear() + 1900);
 		List<FreeDay> copy = new ArrayList<FreeDay>(freeDays.getFreeDay());
 		for (FreeDay day : copy) {
 			if (DateHelper.compareDates(date, day.getDay()) == 0)
 				freeDays.getFreeDay().remove(day);
 		}
 	}
 
 	private static FreeDay createFreeDay() {
 		ObjectFactory fac = new ObjectFactory();
 		return fac.createUserConfigurationUserYearConfFreeDaysFreeDay();
 	}
 
 	private static Holidays createHoliday() {
 		com.hcalendar.data.xml.workedhours.ObjectFactory fac = new com.hcalendar.data.xml.workedhours.ObjectFactory();
 		return fac.createAnualHoursUserInputHolidays();
 	}
 
 	private static WorkedHours createWorkedHours() {
 		com.hcalendar.data.xml.workedhours.ObjectFactory fac = new com.hcalendar.data.xml.workedhours.ObjectFactory();
 		return fac.createAnualHoursUserInputWorkedHours();
 	}
 
 	/**
 	 * get calendar free days
 	 * 
 	 * @param anualConfig
 	 *            anual configuration java bean
 	 * @param date
 	 *            date
 	 * @param profileName
 	 *            profile name which get the hour input
 	 * 
 	 * @return calendar free days
 	 * */
 	public static FreeDays getCalendarFreeDays(UserConfiguration anualConfig,
 			String profileName, int year) {
 		for (User user : anualConfig.getUser()) {
 			if (!user.getName().equals(profileName))
 				continue;
 			for (YearConf yearconf : user.getYearConf()) {
 				if (year == yearconf.getYear())
 					return yearconf.getFreeDays();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * get users worked days list for a given profile
 	 * 
 	 * @param aHours
 	 *            input hours java bean
 	 * @param profileName
 	 *            profile name which get the hour input
 	 * 
 	 * @return worked days list
 	 * */
 	public static List<WorkedHours> getUsersWorkedHourList(AnualHours hours,
 			String profileName) {
 		for (UserInput user : hours.getUserInput()) {
 			if (user.getUserName().equals(profileName))
 				return user.getWorkedHours();
 		}
 		return null;
 	}
 
 	/**
 	 * get users worked days list for a given profile filtered by the given
 	 * dates
 	 * 
 	 * @param aHours
 	 *            input hours java bean
 	 * @param profileName
 	 *            profile name which get the hour input
 	 * 
 	 * @return worked days list
 	 * @throws DateException
 	 * */
 	public static List<WorkedHours> getUsersWorkedHourList(AnualHours hours,
 			String profileName, Date fromDate, Date toDate) throws ORMException {
 		List<WorkedHours> result = new ArrayList<WorkedHours>();
 		try {
 			for (UserInput user : hours.getUserInput()) {
 				if (user.getUserName().equals(profileName))
 					for (WorkedHours worked : user.getWorkedHours()) {
 						if (fromDate != null) {
 							if (DateHelper.isBetween(
 									DateHelper.xmlGregorianCalendar2Date(worked
 											.getDate()), fromDate, toDate))
 								result.add(worked);
 						} else
 							result.add(worked);
 					}
 			}
 		} catch (DateException e) {
 			throw new ORMException(e);
 		}
 		return result;
 	}
 
 	/**
 	 * get users holiday list for a given profile
 	 * 
 	 * @param aHours
 	 *            input hours java bean
 	 * @param profileName
 	 *            profile name which get the hour input
 	 * 
 	 * @return holidays list
 	 * */
 	public static List<Holidays> getUserHolidaysList(AnualHours anualHours,
 			String profileName) {
 		List<Holidays> holidays = new ArrayList<Holidays>();
 		for (UserInput user : anualHours.getUserInput()) {
 			if (user.getUserName().equals(profileName))
 				return user.getHolidays();
 		}
 		return holidays;
 	}
 
 	/**
 	 * get profiles list from the data layer
 	 * 
 	 * @param anualConfig
 	 *            anual configuration java bean
 	 * 
 	 * @return profiles list
 	 * */
 	public static List<String> getCurrentProfiles(UserConfiguration anualConfig) {
 		UserConfiguration user = anualConfig;
 		List<String> result = new ArrayList<String>();
 		for (User us : user.getUser()) {
 			result.add(us.getName());
 		}
 		return result;
 	}
 
 	/**
 	 * get users holiday list for a given profile
 	 * 
 	 * @param aHours
 	 *            input hours java bean
 	 * @param profileName
 	 *            profile name which get the hour input
 	 * 
 	 * @return holidays list
 	 * */
 	public static List<Date> getUserHolidays(AnualHours anualHours,
 			String profileName) {
 		List<Date> result = new ArrayList<Date>();
 		List<Holidays> holidays = getUserHolidaysList(anualHours, profileName);
 		for (Holidays ho : holidays) {
 			result.add(DateHelper.xmlGregorianCalendar2Date(ho.getDate()));
 		}
 		return result;
 	}
 
 	/**
 	 * get calendar free days list
 	 * 
 	 * @param anualConfig
 	 *            anual configuration java bean
 	 * @param date
 	 *            date
 	 * @param profileName
 	 *            profile name which get the hour input
 	 * 
 	 * @return calendar free days list
 	 * */
 	public static List<Date> getCalendarFreeDaysDate(
 			UserConfiguration anualConfig, String profileName, Integer year)
 			throws BusinessException {
 		return freeDay2DateList(getCalendarFreeDays(anualConfig, profileName,
 				year.intValue()).getFreeDay());
 	}
 
 	/**
 	 * get users filtered holiday list for a given profile
 	 * 
 	 * @param aHours
 	 *            input hours java bean
 	 * @param profileName
 	 *            profile name which get the hour input
 	 * @param fromDate
 	 *            from
 	 * @param toDate
 	 *            to
 	 * 
 	 * @return holidays list
 	 * */
 	public static List<Holidays> getUserHolidaysList(AnualHours anualHours,
 			String profileName, Date fromDate, Date toDate) throws ORMException {
 		try {
 			List<Holidays> result = new ArrayList<AnualHours.UserInput.Holidays>();
 			List<Holidays> list = getUserHolidaysList(anualHours, profileName);
 			for (Holidays hol : list) {
				if ((fromDate==null && toDate==null) ||
					DateHelper.isBetween(
 						DateHelper.xmlGregorianCalendar2Date(hol.getDate()),
 						fromDate, toDate))
 					result.add(hol);
 			}
 			return result;
 		} catch (Exception e) {
 			throw new ORMException(e);
 		}
 	}
 
 	/**
 	 * get calendar free days
 	 * 
 	 * @param aHours
 	 *            input hours java bean
 	 * @param profileName
 	 *            profile name which get the hour input
 	 * @param fromDate
 	 *            from
 	 * @param toDate
 	 *            to
 	 * 
 	 * @return calendar free days
 	 * */
 	public static List<FreeDay> getCalendarFreeDays(
 			UserConfiguration anualConfiguration, String profileName, int year,
 			Date fromDate, Date toDate) throws ORMException {
 		try {
 			List<FreeDay> result = new ArrayList<FreeDay>();
 			FreeDays list = getCalendarFreeDays(anualConfiguration,
 					profileName, year);
 			for (FreeDay day : list.getFreeDay()) {
 				if (fromDate == null) {
 					result.add(day);
 					continue;
 				}
 				if (DateHelper.isBetween(
 						DateHelper.xmlGregorianCalendar2Date(day.getDay()),
 						fromDate, toDate))
 					result.add(day);
 			}
 			return result;
 		} catch (Exception e) {
 			throw new ORMException(e);
 		}
 	}
 
 	/**
 	 * get calendar hours for a given profile
 	 * 
 	 * @param anualConfig
 	 *            anual configuration java bean
 	 * @param profileName
 	 *            profile name which get the hour input
 	 * 
 	 * @return Calendar hours from the data layer
 	 * */
 	public static Float getCalendarHours(UserConfiguration anualConfig,
 			String profileName, int year) {
 		UserConfiguration userConf = anualConfig;
 		for (com.hcalendar.data.xml.userconfiguration.UserConfiguration.User user : userConf
 				.getUser()) {
 			if (user.getName().equals(profileName)) {
 				for (YearConf conf : user.getYearConf()) {
 					if (conf.getYear() == year)
 						return conf.getCalendarHours();
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * get input hours for a given profile and date
 	 * 
 	 * @param anualConfig
 	 *            anual configuration java bean
 	 * @param date
 	 *            date
 	 * @param profileName
 	 *            profile name which get the hour input
 	 * 
 	 * @return Calendar hours from the data layer
 	 * */
 	public static Map<Float, String> getInputHours(AnualHours anualConfig,
 			Date date, String profileName) {
 		Map<Float, String> result = new HashMap<Float, String>();
 		// Worked hours
 		for (WorkedHours wHours : getUsersWorkedHourList(anualConfig,
 				profileName)) {
 			if (DateHelper.compareDates(date, wHours.getDate()) == 0)
 				result.put(wHours.getHours(), wHours.getDescription());
 		}
 		// Holidays
 		for (Holidays hol : getUserHolidaysList(anualConfig, profileName)) {
 			if (DateHelper.compareDates(date, hol.getDate()) == 0)
 				result.put((float) 0,
 						hol.getComment() == null ? "" : hol.getComment());
 		}
 		return result;
 	}
 
 	/**
 	 * get calendar not working days
 	 * 
 	 * @param anualConfig
 	 *            anual configuration java bean
 	 * @param date
 	 *            date
 	 * @param profileName
 	 *            profile name which get the hour input
 	 * 
 	 * @return calendar not working days
 	 * */
 	@SuppressWarnings("deprecation")
 	public static List<Date> getCalendarNotWorkingDays(
 			UserConfiguration anualConfig, String username, int selectedYear) {
 		List<Integer> dayList = new ArrayList<Integer>();
 		List<Integer> notWorkingDays = new ArrayList<Integer>();
 		List<Date> result = new ArrayList<Date>();
 
 		for (User user : anualConfig.getUser()) {
 			if (user.getName().equals(username)) {
 				for (YearConf year : user.getYearConf()) {
 					if (year.getYear() != selectedYear)
 						return null;
 					// Calcular los findes a partir de los laborales
 					for (WorkingDays wd : year.getWorkingDays()) {
 						dayList.add(Integer.valueOf(wd.getWorkingDay()));
 					}
 					for (int i = 1; i <= Calendar.SATURDAY; i++) {
 						if (!dayList.contains(i))
 							notWorkingDays.add(i);
 					}
 				}
 			}
 		}
 
 		// Ya tenemos los dias que no son laborables a la semana. Ahora recorrer
 		// el ao y sacar las fechas.
 		// Sacamos todas sin ms calculo, puede pasar que se haya trabajado en
 		// un dia que sea festivo. Al pintar lo haremos en el orden correcto y
 		// punto
 		DateIterator dateIt = new DateIterator(new Date(selectedYear - 1900, 0,
 				1), new Date(selectedYear - 1900, 11, 31));
 		Date today;
 		while (dateIt.hasNext()) {
 			today = dateIt.next();
 			if (notWorkingDays.contains(today.getDay() + 1))
 				result.add(today);
 		}
 		return result;
 	}
 
 	// Mtodos add y remove
 	/**
 	 * Add free day to the anual configuration java bean
 	 * 
 	 * @param anualConfig
 	 *            anual configuration java bean
 	 * @param anualHours
 	 *            anual input hours java bean
 	 * @param date
 	 *            date
 	 * @param comment
 	 *            Comment of the freeday
 	 * @param profileName
 	 *            profile name to get the hour input
 	 * @throws ORMException
 	 * */
 	public static void addFreeDays(UserConfiguration anualConfig,
 			AnualHours anualHours, Date date, String comment, String profileName)
 			throws ORMException {
 		boolean repeated = false;
 		try {
 			@SuppressWarnings("deprecation")
 			FreeDays freeDays = getCalendarFreeDays(anualConfig, profileName,
 					date.getYear() + 1900);
 			List<FreeDay> freeDaysList = freeDays.getFreeDay();
 			for (FreeDay day : freeDaysList) {
 				if (DateHelper.compareDates(date, day.getDay()) == 0)
 					repeated = true;
 			}
 			if (!repeated) {
 				FreeDay freeDay = createFreeDay();
 				freeDay.setDay(DateHelper.date2XMLGregorianCalendar(date));
 				freeDay.setComment(comment);
 				freeDaysList.add(freeDay);
 			}
 			removeFromHolidays(anualHours, date, profileName);
 			removeFromWorkedDays(anualHours, date, profileName);
 		} catch (DateException e) {
 			throw new ORMException(e);
 		}
 	}
 
 	/**
 	 * Add holiday to the anual configuration java bean
 	 * 
 	 * @param anualHours
 	 *            anual input hours java bean
 	 * @param anualConfig
 	 *            anual configuration java bean
 	 * @param date
 	 *            date
 	 * @param comment
 	 *            Comment of the holiday
 	 * @param profileName
 	 *            profile name to get the hour input
 	 * @throws ORMException
 	 * */
 	public static void addHolidays(AnualHours anualHours,
 			UserConfiguration anualConfig, Date date, String comment,
 			String profileName) throws ORMException {
 		boolean repeated = false;
 		try {
 			AnualHours xmlObject = anualHours;
 			List<Holidays> holidays = getUserHolidaysList(xmlObject,
 					profileName);
 			for (Holidays holiday : holidays) {
 				if (DateHelper.compareDates(date, holiday.getDate()) == 0)
 					repeated = true;
 			}
 			if (!repeated) {
 				Holidays hol = createHoliday();
 				hol.setDate(DateHelper.date2XMLGregorianCalendar(date));
 				hol.setComment(comment);
 				holidays.add(hol);
 			}
 			removeFromFreeDays(anualConfig, date, profileName);
 			removeFromWorkedDays(xmlObject, date, profileName);
 		} catch (DateException e) {
 			throw new ORMException(e);
 		}
 
 	}
 
 	/**
 	 * Add holiday to the anual configuration java bean
 	 * 
 	 * @param anualHours
 	 *            anual input hours java bean
 	 * @param anualConfig
 	 *            anual configuration java bean
 	 * @param date
 	 *            date
 	 * @param hours
 	 *            input hours for the day
 	 * @param comment
 	 *            Comment of the worked day
 	 * @param profileName
 	 *            profile name to get the hour input
 	 * @throws ORMException
 	 * */
 	public static void addWorkDays(AnualHours anualHours,
 			UserConfiguration anualConfig, Date date, Float hours,
 			String description, String profileName) throws ORMException {
 		try {
 			boolean repeated = false;
 			AnualHours xmlObject = anualHours;
 			List<WorkedHours> workedDays = getUsersWorkedHourList(xmlObject,
 					profileName);
 			for (WorkedHours day : workedDays) {
 				if (DateHelper.compareDates(date, day.getDate()) == 0) {
 					repeated = true;
 					day.setDate(DateHelper.date2XMLGregorianCalendar(date));
 					day.setDescription(description);
 					day.setHours(hours);
 				}
 			}
 			if (!repeated) {
 				WorkedHours workedDay = createWorkedHours();
 				workedDay.setDate(DateHelper.date2XMLGregorianCalendar(date));
 				workedDay.setDescription(description);
 				workedDay.setHours(hours);
 				workedDays.add(workedDay);
 			}
 			removeFromFreeDays(anualConfig, date, profileName);
 			removeFromHolidays(anualHours, date, profileName);
 		} catch (Exception e) {
 			throw new ORMException(e);
 		}
 	}
 }
