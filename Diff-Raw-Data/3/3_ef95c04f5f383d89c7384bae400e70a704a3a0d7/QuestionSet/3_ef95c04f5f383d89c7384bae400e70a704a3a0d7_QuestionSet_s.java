 package uk.ac.cam.sup.models;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.persistence.Column;
 import javax.persistence.Embedded;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.CascadeType;
 import org.hibernate.annotations.GenericGenerator;
 
 import uk.ac.cam.sup.form.QuestionSetEdit;
 
 @Entity
 @Table(name="QuestionSets")
 public class QuestionSet extends Model {
 	@Id
 	@GeneratedValue(generator="increment")
 	@GenericGenerator(name="increment", strategy="increment")
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
 	@Cascade(CascadeType.SAVE_UPDATE)
 	private Set<QuestionPlacement> questions = new TreeSet<QuestionPlacement>();
 	
 	@SuppressWarnings("unused")
 	private QuestionSet() {}
 	public QuestionSet(User owner){
 		this.owner = owner;
 	}
 	
 	public int getId(){return id;}
 	
 	public String getName(){return name;}
 	public void setName(String name){this.name = name;}
 	
 	public boolean isStarred(){return isStarred;}
 	public void setStarred(boolean s){isStarred = s;}
 	public void toggleStarred(){isStarred = !isStarred;}
 	
 	public User getOwner(){return owner;}
 	
 	public Data getPlan(){return plan;}
 	public void setPlan(Data plan){this.plan = plan;}
 	
 	public void setTags(Set<Tag> tags){this.tags = tags;}
 	public Set<Tag> getTags() {
 		return tags;
 	}
 	public void addTag(Tag tag){tags.add(tag);}
 	public void removeTag(Tag tag){tags.remove(tag);}
 	public void removeTagByString(String tag){tags.remove(new Tag(tag));}
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
 		for(QuestionPlacement q: questions) {
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
 		if (getQuestions().contains(question)) { return; }
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
 		//r.put("timeStamp", this.timeStamp); // for direct soy access use soyTimeStamp
 		r.put("soyTimeStamp", this.timeStamp.toString());
 		r.put("expectedDuration", this.getExpectedDuration());
 		r.put("starred", this.isStarred);
 		r.put("tags", this.tags);
 		
 		if (shadow && this.owner.getSupervisor()) {
 			r.put("plan", null);
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
 		
 		this.questions.clear();
 		
 		for (int i = 0; i < qse.getQuestions().size(); i++) {
 			QuestionPlacement qp = new QuestionPlacement(qse.getQuestions().get(i), i+1);
 			qp.save();
 			this.questions.add(qp);
 		}
 		this.update();
 	}
 }
