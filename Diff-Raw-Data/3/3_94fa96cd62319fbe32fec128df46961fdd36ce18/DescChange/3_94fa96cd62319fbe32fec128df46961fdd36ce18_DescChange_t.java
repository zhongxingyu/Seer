 package classviewer.changes;
 
 import java.util.HashMap;
 
 import classviewer.model.CourseModel;
 import classviewer.model.DescRec;
 
 /**
  * Add/delete/modify university or category.
  * 
  * @author TK
  */
 public class DescChange extends Change {
 	public static final int UNIVERSITY = 1;
 	public static final int CATEGORY = 2;
 	/**
 	 * Is this University or Category? Don't want to subclass for just 2 options
 	 */
 	private int what;
 	/** Field that changed, NULL of ADD/DELETE */
 	private String field = null;
 	/** Affected description, if exists */
 	private DescRec desc = null;
 	/** Affecting chunk of JSON, unless DELETE */
 	private HashMap<String, Object> json = null;
 
 	public DescChange(int what, String type, String field, DescRec desc,
 			HashMap<String, Object> json) {
 		super(type);
 		this.what = what;
 		this.field = field;
 		this.desc = desc;
 		this.json = json;
 		if (type == ADD)
 			order = 1;
 		else if (type == DELETE)
 			order = 7;
 		else
 			order = 4;
 	}
 
 	public String getDescription() {
 		return field;
 	}
 
 	public Object getTarget() {
 		if (desc != null)
 			return desc.getId();
 		return json.get("short_name");
 	}
 
 	public Object getNewValue() {
 		if (type == ADD)
 			return json.get("name");
 		if (type == DELETE)
 			return desc.getName();
 		if ("Name".equals(field))
 			return json.get("name");
 		return json.get("description");
 	}
 
 	public Object getOldValue() {
 		if (type == ADD || type == DELETE)
 			return null;
 		if ("Name".equals(field))
 			return desc.getName();
 		return desc.getDescription();
 	}
 
 	@Override
 	public void apply(CourseModel model) {
 		if (type == ADD) {
 			DescRec dr = makeDesc();
 			switch (what) {
 			case UNIVERSITY:
 				model.addUniversity(dr);
 				return;
 			case CATEGORY:
 				model.addCategory(dr);
 				return;
 			default:
 				throw new UnsupportedOperationException("Unknown object type "
 						+ what);
 			}
 		} else if (type == DELETE) {
			// This is deletion, so we have a record but no JSON
			String id =  desc.getId();
 			switch (what) {
 			case UNIVERSITY:
 				model.removeUniversity(id);
 				return;
 			case CATEGORY:
 				model.removeCategory(id);
 				return;
 			default:
 				throw new UnsupportedOperationException("Unknown object type "
 						+ what);
 			}
 		} else {
 			// Assume we have a pointer to the record we can change in place.
 			// Assume short-name is the same, since it acts as an id.
 			assert (json != null);
 			assert (desc != null);
 			if ("Name".equals(field)) {
 				desc.setName((String) json.get("name"));
 			} else if ("Description".equals(field)) {
 				desc.setDescription((String) json.get("description"));
 			} else {
 				throw new UnsupportedOperationException("Unknown field "
 						+ field);
 			}
 		}
 	}
 
 	private DescRec makeDesc() {
 		String id = (String) json.get("short_name");
 		String name = (String) json.get("name");
 		String dsc = (String) json.get("description");
 		return new DescRec(id, name, dsc);
 	}
 }
