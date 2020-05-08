 package controllers;
 
 import java.util.List;
 import java.util.Date;
 import java.text.SimpleDateFormat;
 import play.mvc.*;
 import play.data.validation.*;
 import models.*;
 
 @With(Secure.class)
 public class cal extends Controller
 {
 	public static void ls_evts (
 		String id,
 		String year,
 		String month,
 		String day
 	) {
 		ESECalendar c;
 		ESEMonth emonth = null;
 		List<ESEEvent> le = null;
 
 		if ((c = ESECalendar.getCalendar(id)) != null) {
 			emonth = new ESEMonth(year, month, day, c);
 			le = permitted(c)
				?c.getListOfPubEventsRunningAtDay(
 					emonth.date_human)
				:c.getListOfEventsRunningAtDay(
 					emonth.date_human);
 		}
 		render(id, le, emonth);
 	}
 
 	public static void ls_evts (
 		String id
 	) {
 		cal.ls_evts(id, null, null, null);
 	}
 
 	public static void add_evt (
 		String id,
 		String eid,
 		String ename,
 		String ebeg,
 		String eend,
		String epub
 	) {
		render(id, eid, ename, ebeg, eend, epub);
 	}
 
 	public static void add_evt_post (
 		String id,
 		String eid,
 		@Required String name,
 		@Required String beg,
 		@Required String end,
 		String pub
 	) {
 		Date dbeg, dend;
 		Boolean err_date = false;
 		ESECalendar c = ESECalendar.getCalendar(id);
 		SimpleDateFormat sdf = new SimpleDateFormat(
 			"dd.MM.yyyy HH:mm");
 
 		try {
 			dbeg = sdf.parse(beg);
 			dend = sdf.parse(end);
 			if (dend.compareTo(dbeg) < 0) {
 				throw new Exception();
 			}
 		} catch (Exception e) {
 			err_date = true;
 		}
 		if (!validation.hasErrors() && !err_date && c != null) {
 			if (permitted(c)) {
 				pub = pub==null ?"false" :"true";
 				if (eid != null) {
 					c.removeEvent(Long.parseLong(eid));
 				}
 				ESEFactory.createEvent(
 					name, beg, end, pub, c);
 			}
 			cal.ls_evts(id);
 		}
 		params.flash();
 		validation.keep();
		add_evt(id, eid, name, beg, end, pub);
 	}
 
 	public static void edit_evt (
 		String id,
 		String eid,
 		String year,
 		String month,
 		String day
 	) {
 		ESEEvent e = ESEEvent.findById(Long.parseLong(eid));
 		add_evt(id, eid, e.getEventName(),
 			ESEConversionHelper.convertDateToString(
 				e.getStartDate()),
 			ESEConversionHelper.convertDateToString(
 				e.getEndDate()),
			((Boolean)e.isPublic()).toString());
 	}
 
 	public static void rm_evt (
 		String id,
 		String eid,
 		String year,
 		String month,
 		String day
 	) {
 		ESECalendar c = ESECalendar.getCalendar(id);
 		if (c != null && permitted(c)) {
 			c.removeEvent(Long.parseLong(eid));
 		}
 		cal.ls_evts(id, year, month, day);
 	}
 
 	public static Boolean permitted (
 		ESECalendar c
 	) {
 		String authid = Secure.Security.connected();
 		String cal_owner = c.getOwner().getUsername();
 		return authid.equals(cal_owner);
 	}
 }
