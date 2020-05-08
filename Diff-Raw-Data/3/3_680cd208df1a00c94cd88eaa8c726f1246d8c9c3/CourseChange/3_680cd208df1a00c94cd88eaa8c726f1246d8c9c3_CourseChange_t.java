 package classviewer.changes;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import classviewer.model.CourseModel;
 import classviewer.model.CourseRec;
 
 /**
  * Add/delete course or change course field.
  * 
  * @author TK
  */
 public class CourseChange extends Change {
 	/** Field that changed */
 	private String field = null;
 	/** Affected existing course, if any */
 	private CourseRec course = null;
 	/** Incoming chunk of JSON, unless deletion */
 	private HashMap<String, Object> json = null;
 	private CourseRec created = null;
 
 	public CourseChange(String type, String field, CourseRec course,
 			HashMap<String, Object> json, CourseModel model) {
 		super(type);
 		this.field = field;
 		this.course = course;
 		this.json = json;
 		if (json != null)
 			created = makeCourse(model);
 		if (type == ADD)
 			order = 2;
 		else if (type == DELETE)
 			order = 6;
 		else
 			order = 4;
 	}
 
 	public String getDescription() {
 		return field;
 	}
 
 	public Object getTarget() {
 		if (course != null)
 			return course.getName();
 		return created.getName();
 	}
 
 	public Object getNewValue() {
 		if (type == ADD)
 			return created.getName();
 		if (type == DELETE)
 			return course.getName();
 		return valueOf(created);
 	}
 
 	public Object valueOf(CourseRec rec) {
 		if ("Name".equals(field))
 			return rec.getName();
 		if ("Description".equals(field))
 			return rec.getDescription();
 		if ("Short name".equals(field))
 			return rec.getShortName();
 		if ("Instructor".equals(field))
 			return rec.getInstructor();
 		if ("Language".equals(field))
 			return rec.getLanguage();
 		if ("Link".equals(field))
 			return rec.getLink();
 		if ("Categories".equals(field))
 			return json.get("category-ids");
 		if ("Universities".equals(field))
 			return json.get("university-ids");
 		return "??? " + field;
 	}
 
 	public Object getOldValue() {
 		if (type == ADD || type == DELETE)
 			return null;
 		return valueOf(course);
 	}
 
 	@Override
 	public void apply(CourseModel model) {
 		if (type == ADD) {
 			setCategories(created, model);
 			setUniverisities(created, model);
 			// Add offerings
 			created.getOfferings().clear();
 			@SuppressWarnings("unchecked")
 			ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) json
 					.get("courses");
 			for (HashMap<String, Object> map : list)
 				created.addOffering(OfferingChange.makeOffering(map));
 			model.addCourse(created);
 		} else if (type == DELETE) {
			model.removeCourse(course.getId());
 		} else {
 			// Assume we have a pointer to the record we can change in place.
 			assert (json != null);
 			assert (course != null);
 			if ("Name".equals(field)) {
 				course.setName(created.getName());
 			} else if ("Description".equals(field)) {
 				course.setDescription(created.getDescription());
 			} else if ("Short name".equals(field)) {
 				course.setShortName(created.getShortName());
 			} else if ("Instructor".equals(field)) {
 				course.setInstructor(created.getInstructor());
 			} else if ("Language".equals(field)) {
 				course.setLanguage(created.getLanguage());
 			} else if ("Link".equals(field)) {
 				course.setLink(created.getLink());
 			} else if ("Categories".equals(field)) {
 				setCategories(course, model);
 			} else if ("Universities".equals(field)) {
 				setUniverisities(course, model);
 			} else {
 				throw new UnsupportedOperationException("Unknown field "
 						+ field);
 			}
 		}
 	}
 
 	private void setCategories(CourseRec rec, CourseModel model) {
 		rec.getCategories().clear();
 		@SuppressWarnings("unchecked")
 		ArrayList<String> lst = (ArrayList<String>) json.get("category-ids");
 		for (String s : lst)
 			rec.addCategory(model.getCategory(s));
 	}
 
 	private void setUniverisities(CourseRec rec, CourseModel model) {
 		rec.getUniversities().clear();
 		@SuppressWarnings("unchecked")
 		ArrayList<String> lst = (ArrayList<String>) json.get("university-ids");
 		for (String s : lst)
 			rec.addUniversity(model.getUniversity(s));
 	}
 
 	private CourseRec makeCourse(CourseModel model) {
 		Integer id = (Integer) json.get("id");
 		String shortName = (String) json.get("short_name");
 		String name = (String) json.get("name");
 		String dsc = (String) json.get("short_description");
 		String instructor = (String) json.get("instructor");
 		String language = (String) json.get("language");
 		String link = (String) json.get("social_link");
 		CourseRec res = new CourseRec(id, shortName, name, dsc, instructor,
 				link, language);
 		return res;
 	}
 }
