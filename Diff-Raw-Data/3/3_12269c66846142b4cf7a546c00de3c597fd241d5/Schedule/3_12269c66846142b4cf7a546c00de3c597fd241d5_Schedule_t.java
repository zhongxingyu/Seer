 package cours.ulaval.glo4003.domain;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import cours.ulaval.glo4003.domain.conflictdetection.conflict.Conflict;
 
 public class Schedule {
 
 	private String id;
 	private String year;
 	private Semester semester;
 	private Map<String, Section> sections;
 	private String personInCharge;
 	private List<Conflict> conflicts;
 	private Integer score;
 
 	public Schedule() {
 	}
 
 	public Schedule(String id) {
 		this.id = id;
 		sections = new HashMap<String, Section>();
 		conflicts = new ArrayList<Conflict>();
 	}
 
 	public void add(Section section) {
 		if (!sectionExist(section.getNrc())) {
 			sections.put(section.getNrc(), section);
 		}
 	}
 
 	public void add(Conflict conflict) {
 		this.conflicts.add(conflict);
 	}
 
 	public void addAll(List<Conflict> conflicts) {
 		this.conflicts.addAll(conflicts);
 	}
 
 	public void delete(String sectionNrc) {
 		sections.remove(sectionNrc);
 	}
 
 	public void calculateScore() {
 		score = 0;
 		for (Conflict conflict : conflicts) {
 			score += conflict.getScore();
 		}
 	}
 
 	private boolean sectionExist(String nrc) {
 		return sections.containsKey(nrc);
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public String getYear() {
 		return year;
 	}
 
 	public void setYear(String year) {
 		this.year = year;
 	}
 
 	public Semester getSemester() {
 		return semester;
 	}
 
 	public void setSemester(Semester semester) {
 		this.semester = semester;
 	}
 
 	public Map<String, Section> getSections() {
 		return sections;
 	}
 
 	public List<Section> getSectionsList() {
 		return new ArrayList<Section>(sections.values());
 	}
 
 	public void setSections(Map<String, Section> sections) {
 		this.sections = sections;
 	}
 
 	public String getPersonInCharge() {
 		return personInCharge;
 	}
 
 	public void setPersonInCharge(String personInCharge) {
 		this.personInCharge = personInCharge;
 	}
 
 	public List<Conflict> getConflicts() {
		if (conflicts == null) {
			conflicts = new ArrayList<Conflict>();
		}
 		return conflicts;
 	}
 
 	public void setConflicts(List<Conflict> conflicts) {
 		this.conflicts = conflicts;
 	}
 
 	public Integer getScore() {
 		return score;
 	}
 
 	public void setScore(Integer score) {
 		this.score = score;
 	}
 
 }
