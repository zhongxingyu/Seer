 package classviewer.model;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 
 /**
  * Course record, includes offerings
  * 
  * @author TK
  */
 public class CourseRec implements Named, Linked {
 
 	protected Source source;  // TODO Should be private once all is converted.
 	private int id;
 	private String shortName;
 	private String name;
 	private String description;
 	private String instructor;
 	private String link;
 	private String language;
 	private Status status = Status.UNKNOWN;
 	private ArrayList<DescRec> categories = new ArrayList<DescRec>();
 	private ArrayList<DescRec> universities = new ArrayList<DescRec>();
 	private ArrayList<OffRec> offerings = new ArrayList<OffRec>();
 
 	public CourseRec(Source source, int id, String shortName, String name,
 			String description, String instructor, String link, String language) {
 		this.source = source;
 		this.id = id;
 		this.shortName = shortName;
 		this.name = name == null ? "" : name;
 		this.description = description == null ? "" : description;
 		this.instructor = instructor == null ? "" : instructor;
 		this.link = link == null ? "" : link;
 		this.setLanguage(language);
 	}
 
 	public void updateId(int newId) {
 		assert (this.id == 0);
 		this.id = newId;
 	}
 
 	@Override
 	public boolean equals(Object other) {
 		try {
			CourseRec o = (CourseRec) other; 
			return o.id == this.id && o.source == this.source;
 		} catch (ClassCastException e) {
 			return false;
 		}
 	}
 
 	@Override
 	public String toString() {
 		return id + " " + status + " " + shortName;
 	}
 
 	public void addCategory(DescRec descRec) {
 		categories.add(descRec);
 	}
 
 	public void addUniversity(DescRec descRec) {
 		universities.add(descRec);
 	}
 
 	public void addOffering(OffRec r) {
 		offerings.add(r);
 		r.setCourse(this);
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public Status getStatus() {
 		return status;
 	}
 
 	/**
 	 * If a course set to no, all offerings set to no, unless already done or
 	 * registered.
 	 */
 	public void setStatus(Status status) {
 		if (this.status == status)
 			return; // to avoid deep checks
 		this.status = status;
 
 		// If course status flipped to REGISTERED or DONE, all UNKNOWN or MAYBE
 		// offerings go to NO. No need to propagate from the offering back, so
 		// use setStatusDirect.
 		if (status == Status.REGISTERED || status == Status.DONE)
 			for (OffRec o : offerings)
 				if (o.getStatus() == Status.UNKNOWN
 						|| o.getStatus() == Status.MAYBE)
 					o.setStatusDirect(Status.NO);
 		// If course is a no, anything other than registered or done should be a
 		// NO. In fact, if there is a DONE/REGISTERED offering, we should
 		// probably not flip the course to NO
 		if (status == Status.NO)
 			for (OffRec o : offerings)
 				if (o.getStatus() != Status.REGISTERED
 						&& o.getStatus() != Status.DONE)
 					o.setStatusDirect(Status.NO);
 	}
 
 	public String getShortName() {
 		return shortName;
 	}
 
 	public void setShortName(String shortName) {
 		this.shortName = shortName;
 	}
 
 	public Source getSource() {
 		return source;
 	}
 	
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getInstructor() {
 		return instructor;
 	}
 
 	public void setInstructor(String instructor) {
 		this.instructor = instructor;
 	}
 
 	public String getLink() {
 		return link;
 	}
 
 	public void setLink(String link) {
 		this.link = link;
 	}
 
 	public String getLanguage() {
 		return language;
 	}
 
 	public void setLanguage(String language) {
 		this.language = language;
 	}
 
 	public ArrayList<DescRec> getCategories() {
 		return categories;
 	}
 
 	public ArrayList<DescRec> getUniversities() {
 		return universities;
 	}
 
 	public ArrayList<OffRec> getOfferings() {
 		return offerings;
 	}
 
 	public OffRec removeOffering(int offId) {
 		for (int i = 0; i < offerings.size(); i++)
 			if (offerings.get(i).getId() == offId)
 				return offerings.remove(i);
 		return null;
 	}
 
 	public String getLongHtml() {
 		String str = "<b>" + name + "</b> (" + id + ", " + status.getName()
 				+ ", " + shortName + ") on " + getSource().pretty() + "<br/>\n";
 		str += "<b>Instructor(s):</b> " + instructor + "<br/>";
 		str += "<b>Categories:</b> " + recNames(categories) + "<br/>";
 		str += "<b>University:</b> " + recNames(universities) + "<br/>";
 		if (language != null && !language.isEmpty())
 			str += "<b>Language:</b> " + this.language + "<br/>";
 		str += description + "<br/>\n";
 		str += "<a href=\"" + link + "\">" + link + "</a><br/>\n";
 		return str;
 	}
 
 	private String recNames(ArrayList<DescRec> recs) {
 		if (recs.isEmpty())
 			return "";
 		String res = recs.get(0).getName();
 		for (int i = 1; i < recs.size(); i++)
 			res = res + ", " + recs.get(i).getName();
 		return res;
 	}
 
 	public OffRec getOffering(int id) {
 		for (OffRec r : offerings)
 			if (r.getId() == id)
 				return r;
 		return null;
 	}
 
 	public void setStatusDirect(Status stat) {
 		this.status = stat;
 	}
 
 	public static HashSet<String> getIdSet(ArrayList<DescRec> list) {
 		HashSet<String> set = new HashSet<String>();
 		for (DescRec r : list) {
 			set.add(r.getId());
 		}
 		return set;
 	}
 
 	public boolean hasUnknown() {
 		for (OffRec r : offerings)
 			if (r.getStatus() == Status.UNKNOWN)
 				return true;
 		return false;
 	}
 
 	public boolean hasDateless() {
 		for (OffRec r : offerings)
 			if (r.getStart() == null)
 				return true;
 		return false;
 	}
 
 	public static HashSet<String> idSet(ArrayList<DescRec> set) {
 		HashSet<String> res = new HashSet<String>();
 		for (DescRec r : set)
 			res.add(r.getId());
 		return res;
 	}
 }
