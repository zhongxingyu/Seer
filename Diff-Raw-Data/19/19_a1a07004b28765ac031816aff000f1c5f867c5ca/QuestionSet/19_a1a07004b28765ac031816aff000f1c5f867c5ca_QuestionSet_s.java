 package uk.ac.cam.sup.models;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import javax.persistence.Column;
 import javax.persistence.Embedded;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.CascadeType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.cam.sup.form.QuestionSetEdit;
 import uk.ac.cam.sup.queries.TagQuery;
 import uk.ac.cam.sup.util.DataType;
 import uk.ac.cam.sup.util.Identifiable;
 import uk.ac.cam.sup.util.Mappable;
 
 @Entity
 @Table(name="QuestionSets")
 public class QuestionSet extends Model implements Mappable, Cloneable, Identifiable {
 	private static Logger log = LoggerFactory.getLogger(QuestionSet.class);
 	
 	@Id
 	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="setIdGen")
 	@SequenceGenerator(name="setIdGen", sequenceName="SET_SEQ", allocationSize=1)
 	private int id;
 	
 	private String name;
 	private boolean isStarred = false;
 	
 	@Column(nullable=false)
 	private Date timeStamp = new Date();
 	
 	@SuppressWarnings("unused")
 	private Integer expectedDuration;
 	
 	@ManyToOne
 	private User owner;
 	
 	@Embedded
 	@Column(nullable=false)
 	private Data plan = new Data();
 	
 	@ManyToMany
 	@Cascade(CascadeType.MERGE)
 	private Set<Tag> tags = new HashSet<Tag>();
 	
 	@OneToMany
 	@Cascade(CascadeType.ALL)
 	private Set<QuestionPlacement> questions = new HashSet<QuestionPlacement>();
 	
 	@SuppressWarnings("unused")
 	private QuestionSet() {}
 	public QuestionSet(User owner){
 		this.owner = owner;
 	}
 	
 	public Integer getId(){return id;}
 	
 	public String getName(){return name;}
 	public void setName(String name){this.name = name;}
 	
 	public boolean isStarred(){return isStarred;}
 	public void setStarred(boolean s){isStarred = s;}
 	public void toggleStarred(){isStarred = !isStarred;}
 	
 	public User getOwner(){return owner;}
 	public void setOwner(User owner){this.owner = owner;}
 	
 	public Data getPlan(){return plan;}
 	public void setPlan(Data plan){this.plan = plan;}
 		
 	public void setTags(Set<Tag> tags){this.tags = tags;}
 	public Set<Tag> getTags() {
 		return tags;
 	}
 	public void addTag(Tag tag){tags.add(tag);}
 	public void removeTag(Tag tag){tags.remove(tag);}
 	public void removeTagByString(String tag){tags.remove(TagQuery.get(tag));}
 	public Set<String> getTagsAsString() {
 		Set<String> result = new HashSet<String>();
 		for(Tag t : tags){
 			result.add(t.getName());
 		}
 		return result;
 	}
 	
	public List<Question> getQuestions(){
		List<Question> result = new ArrayList<Question>();
		while (result.size() < questions.size()) { result.add(null); }
 		log.debug("Trying to get questions of set " + id + " which has " + questions.size() + " questions...");
 		for(QuestionPlacement q: questions) {
 			log.debug("    -> trying to set question in place " + (q.getPlace()-1) + "...");
			result.set(q.getPlace()-1, q.getQuestion());
 		}
		return result;
 	}
 	public List<Map<String,Object>> getQuestionsAsMaps(boolean shadow) {
		List<Question> questions = getQuestions();
 		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
 		for (Question q: questions) {
 			result.add(q.toMap(shadow));
 		}
 		return result;
 	}
 	public List<Map<String,Object>> getQuestionsAsMaps() {
 		return getQuestionsAsMaps(true);
 	}
 	
 	public Question getQuestion(int place) {
 		for (QuestionPlacement qp: questions) {
 			if (qp.getPlace() == place) {
 				return qp.getQuestion();
 			}
 		}
 		throw new IndexOutOfBoundsException("index: " + place + " size: " + questions.size());
 	}
 	
 	public synchronized void addQuestion(Question question) {
 		if (getQuestions().contains(question)) { 
 			log.debug("Question " + question.getId() + " is already in set " + id + ". Doing nothing.");
 			return; 
 		}
 		log.debug("Adding question " + question.getId() + " to set " + id);
 		question.use();
 		QuestionPlacement qp = new QuestionPlacement(question, questions.size()+1);
 		questions.add(qp);
 	}
 	
 	public synchronized void removeQuestion(Question question) {
 		for (QuestionPlacement qp: questions) {
 			if (qp.getQuestion().equals(question)) {
 				this.removeQuestion(qp.getPlace());
 				break;
 			}
 		}
 	}
 	
 	public synchronized void removeQuestion(int place) {
 		Iterator<QuestionPlacement> i = questions.iterator();
 		
 		while (i.hasNext()) {
 			QuestionPlacement qp = i.next();
 			if (qp.getPlace() < place) {
 				continue;
 			} else if (qp.getPlace() == place) {
 				qp.getQuestion().unuse();
 				i.remove();
 				qp.delete();
 			} else {
 				qp.moveUp();
 				qp.update();
 			}
 		}
 	}
 	
 	public synchronized void addBefore(int place, Question q) {
 		Iterator<QuestionPlacement> i = questions.iterator();
 		
 		while (i.hasNext()) {
 			QuestionPlacement qp = i.next();
 			if (qp.getPlace() > place) {
 				qp.moveDown();
 				qp.update();
 			}
 		}
 		
 		q.use();
 		questions.add(new QuestionPlacement(q, place));
 	}
 	
 	public synchronized void addAfter(int place, Question q) {
 		if (place < questions.size()-1) {
 			addBefore(place+1, q);
 		} else {
 			addQuestion(q);
 		}
 	}
 	
 	public synchronized void swapFor(int place, Question q) {
 		Iterator<QuestionPlacement> i = questions.iterator();
 		while (i.hasNext()) {
 			QuestionPlacement qp = i.next();
 			if (qp.getPlace() == place) {
 				qp.getQuestion().unuse();
 				qp.setQuestion(q);
 				q.use();
 				qp.save();
 				break;
 			}
 		}
 	}
 	
 	public synchronized void swapFor(Question old, Question q) {
 		Iterator<QuestionPlacement> i = questions.iterator();
 		while (i.hasNext()) {
 			QuestionPlacement qp = i.next();
 			if (qp.getQuestion().equals(old)) {
 				old.unuse();
 				qp.setQuestion(q);
 				q.use();
 				qp.save();
 				break;
 			}
 		}
 	}
 	
 	public Date getTimeStamp() { return (this.timeStamp == null ? new Date(0) : this.timeStamp); }
 	public void setTimeStamp(Date timeStamp) { this.timeStamp = timeStamp; }
 	
 	public int getExpectedDuration() {
 		int r = 0;
 		
 		for (QuestionPlacement q: questions) {
 			r += q.getQuestion().getExpectedDuration();
 		}
 		this.expectedDuration = r;
 		
 		return r;
 	}
 	
 	@Override
 	public boolean equals(Object x){
 		if (x == null || !(x instanceof QuestionSet)) {
 			return false;
 		}
 		return ((QuestionSet)x).getId() == getId();
 	}
 	
 	@Override
 	public int hashCode(){
 		return id;
 	}
 	
 	public Object clone() throws CloneNotSupportedException {
 		QuestionSet newSet = (QuestionSet) super.clone();
 		newSet.name = new String(name);
 		newSet.plan = (Data) this.plan.clone();
 		
 		newSet.questions = new HashSet<QuestionPlacement>();
 		for (QuestionPlacement qp: questions) {
 			newSet.questions.add((QuestionPlacement)qp.clone());
 		}
 		
 		newSet.tags = new HashSet<Tag>();
 		newSet.tags.addAll(tags);
 		
 		newSet.timeStamp = new Date();
 		
 		return newSet;
 	}
 	
 	public Map<String,Object> toMap(boolean shadow) {
 		Map<String,Object> r = toShortMap(shadow);
 		
 		r.put("questions", this.getQuestionsAsMaps(shadow));
 		
 		return r;
 	}
 	
 	public Map<String,Object> toShortMap(boolean shadow) {
 		Map<String,Object> r = new HashMap<String,Object>();
 		
 		r.put("id", this.id);
 		r.put("name", this.name);
 		r.put("owner", this.owner);
 		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.UK);
 		r.put("timeStamp", dateFormat.format(timeStamp));
 		r.put("expectedDuration", this.getExpectedDuration());
 		r.put("starred", this.isStarred);
 		r.put("tags", this.tags);
 		
 		if (shadow && this.owner.getSupervisor()) {
 			r.put("plan", new Data(DataType.EMPTY, null));
 		} else {
 			r.put("plan", this.plan);
 		}
 		
 		return r;
 	}
 	
 	public Map<String,Object> toMap() {
 		return toMap(true);
 	}
 	
 	public Map<String,Object> toShortMap() {
 		return toShortMap(true);
 	}
 	
 	public void edit(QuestionSetEdit qse) throws Exception {
 		this.name = qse.getName();
 		this.plan.updateWith(qse.getPlan());
 		
 		for (QuestionPlacement qp: this.questions) {
 			qp.delete();
 		}
 		this.questions.clear();
 		
 		for (int i = 0; i < qse.getQuestions().size(); i++) {
 			QuestionPlacement qp = new QuestionPlacement(qse.getQuestions().get(i), i+1);
 			qp.save();
 			this.questions.add(qp);
 		}
 		this.update();
 	}
 	
 	public QuestionSet fork(User u, String name) throws CloneNotSupportedException {
 		QuestionSet fork = (QuestionSet) this.clone();
 		if (this.owner.getSupervisor() && !u.getSupervisor()) {
 			fork.plan = new Data();
 		}
 		
 		fork.owner = u;
 		fork.name = name;
 		fork.id = 0;
 		
 		return fork;
 	}
 }
