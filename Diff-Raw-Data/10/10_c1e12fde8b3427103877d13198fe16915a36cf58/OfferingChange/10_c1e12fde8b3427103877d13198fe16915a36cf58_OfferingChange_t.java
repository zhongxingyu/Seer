 package classviewer.changes;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 
 import classviewer.model.CourseModel;
 import classviewer.model.CourseRec;
 import classviewer.model.OffRec;
 
 public class OfferingChange extends Change {
 
 	private String field;
 	private CourseRec course;
 	private OffRec offering;
 	private HashMap<String, Object> json;
 	private OffRec created = null;
 
 	public OfferingChange(String type, CourseRec course, String field,
 			OffRec offering, HashMap<String, Object> json) {
 		super(type);
 		this.course = course;
 		this.field = field;
 		this.offering = offering;
 		this.json = json;
 		if (json != null)
 			created = makeOffering(json);
 		if (type == ADD)
 			order = 3;
 		else if (type == DELETE)
 			order = 5;
 		else
 			order = 4;
 	}
 
 	@Override
 	public String getDescription() {
 		if (type == ADD || type == DELETE)
 			return "Offering";
 		return field;
 	}
 
 	public Object getTarget() {
 		if (offering != null)
 			return offering.getCourse().getName() + " [" + offering.getId()
 					+ "]";
 		return course.getName() + " [" + json.get("id") + "]";
 	}
 
 	@Override
 	public Object getNewValue() {
 		if (type == ADD)
 			return created.getStartStr();
 		if (type == DELETE)
 			return offering.getStartStr();
 		return getField(created);
 	}
 
 	private Object getField(OffRec rec) {
 		if ("Spread".equals(field))
 			return rec.getSpread();
 		if ("Active".equals(field))
 			return rec.isActive();
 		if ("Start".equals(field))
 			return rec.getStartStr();
 		if ("Duration".equals(field))
 			return rec.getDurStr();
 		if ("Link".equals(field))
 			return rec.getLink();
 		return "??? " + field;
 	}
 
 	@Override
 	public Object getOldValue() {
 		if (type == MODIFY)
 			return getField(offering);
 		return null;
 	}
 
 	@Override
 	public void apply(CourseModel model) {
 		if (type == ADD) {
 			course.addOffering(created);
 		} else if (type == DELETE) {
			course.removeOffering(this.offering.getId());
 		} else {
 			if ("Spread".equals(field)) {
 				offering.setSpread(created.getSpread());
 			} else if ("Active".equals(field)) {
 				offering.setActive(created.isActive());
 			} else if ("Start".equals(field)) {
 				offering.setStart(created.getStart());
 				offering.setStartStr(created.getStartStr());
 			} else if ("Duration".equals(field)) {
 				offering.setDuration(created.getDuration());
 				offering.setDurStr(created.getDurStr());
 			} else if ("Link".equals(field)) {
 				offering.setLink(created.getLink());
 			} else
 				throw new UnsupportedOperationException("Unsupported field "
 						+ field);
 		}
 	}
 
 	public static OffRec makeOffering(HashMap<String, Object> json) {
 		Integer id = (Integer) json.get("id");
 
 		// Some of these might be missing
 		Integer startDay = (Integer) json.get("start_day");
 		Integer startMon = (Integer) json.get("start_month");
 		Integer startYear = (Integer) json.get("start_year");
 		int spread = 1;
 		Date start = null;
 		String startStr = (String) json.get("start_date_string");
 
 		Calendar cal = Calendar.getInstance();
 		if (startDay == null)
 			startDay = 1;
 		cal.set(Calendar.DAY_OF_MONTH, startDay);
 		if (startMon == null) {
 			// TODO: pull season?
 			spread = 4;
 			startMon = 1;
 		}
 		cal.set(Calendar.MONTH, startMon - 1);
 		if (startYear != null) {
 			// No year => no date
 			cal.set(Calendar.YEAR, startYear);
 			start = cal.getTime();
 		} else {
 			spread = 1; // no data
 		}
 		if (startStr == null && start != null)
 			startStr = OffRec.dformat.format(start);
 
 		String durStr = (String) json.get("duration_string");
 		if ("".equals(durStr))
 			durStr = null;
 		int duration = 1;
 		if (durStr != null)
 			try {
 				String s = durStr.trim();
				if (s.indexOf(" ") > 0)
					s = s.substring(0, s.indexOf(" "));
				if (s.indexOf("-") > 0)
					s = s.substring(0, s.indexOf("-"));
				duration = Integer.parseInt(s);
 			} catch (Exception e) {
 				System.err.println("Cannot parse duration " + durStr);
 			}
 
 		Boolean active = (Boolean) json.get("active");
 		String home = (String) json.get("home_link");
 		return new OffRec(id, start, duration, spread, home, active, startStr,
 				durStr);
 	}
 }
