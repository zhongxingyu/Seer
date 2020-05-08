 package uk.ac.cam.sup.models;
 
import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
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
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.hibernate.annotations.Cascade;
 import org.hibernate.annotations.CascadeType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.cam.sup.exceptions.FormValidationException;
 import uk.ac.cam.sup.exceptions.InvalidInputException;
 import uk.ac.cam.sup.form.QuestionEdit;
 import uk.ac.cam.sup.queries.QuestionQuery;
 import uk.ac.cam.sup.queries.QuestionSetQuery;
 import uk.ac.cam.sup.queries.TagQuery;
 import uk.ac.cam.sup.util.DataType;
 import uk.ac.cam.sup.util.Mappable;
 
 @Entity()
 @Table(name="Questions")
 public class Question extends Model implements Cloneable, Mappable {
 	@Transient
 	private static Logger log = LoggerFactory.getLogger(Question.class);
 	
 	@Id
 	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="questionIdGen")
 	@SequenceGenerator(name="questionIdGen", sequenceName="QUESTION_SEQ", allocationSize=1)
 	private Integer id;
 	
 	private boolean isStarred = false;
 	private int usageCount = 0;
 	private Date timeStamp;
 	
 	@ManyToOne
 	@Cascade(CascadeType.SAVE_UPDATE)
 	private Question parent;
 	
 	@ManyToMany
 	@Cascade(CascadeType.SAVE_UPDATE)
 	private Set<Tag> tags = new HashSet<Tag>();
 	
 	@ManyToOne
 	private User owner;
 	
 	private int expectedDuration = 0;
 	
 	@Embedded @Column(nullable=false)
 	private Data content = new Data(DataType.EMPTY, null);
 	
 	@Embedded @Column(nullable=false)
 	private Data notes = new Data(DataType.EMPTY, null);
 	
 	@SuppressWarnings("unused")
 	private Question() {}
 	public Question(User owner){
 		this.owner = owner;
 		this.timeStamp = new Date();
 	}
 	
 	public Integer getId(){
 		return this.id;
 	}
 	
 	public boolean isStarred(){return isStarred;}
 	public void star(boolean s){isStarred = s;}
 	public void toggleStarred(){isStarred=!isStarred;}
 	
 	public int getUsageCount(){	return this.usageCount; }
 	public void setUsageCount(int c){usageCount = c;}
 	
 	public Date getTimeStamp(){return timeStamp;}
 	public void setTimeStamp(Date d){timeStamp = d;}
 	
 	public Question getParent(){return parent;}
 	public void setParent(Question p){parent = p;}
 	
 	public User getOwner(){return owner;}
 	
 	// Duration in minutes
 	public int getExpectedDuration(){return expectedDuration;}
 	public void setExpectedDuration(int expDuration){expectedDuration = expDuration;}
 	
 	public Data getContent(){return content;}
 	public void setContent(Data c){content = c;}
 	
 	public Data getNotes(){return notes;}
 	public void setNotes(Data n){notes = n;}
 	
 	public void setTags(Set<Tag> tags){this.tags = tags;}
 	public Set<Tag> getTags(){return tags;}
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
 	
 	@Override
 	public boolean equals(Object x){
 		if (x == null || !(x instanceof Question)) {
 			return false;
 		} else if ((this.id == null) ^ (((Question)x).id == null)) {
 			return false;
 		} else if ((this.id == null) && (((Question)x).id == null)) {
 			return ((Question)x) == this;
 		}
 		return ((Question)x).id == this.id;
 	}
 	
 	@Override
 	public int hashCode(){
 		return (this.id == null ? 0 : this.id);
 	}
 	
 	private Question fork(QuestionSet qs, User forker){
 		Question result = fork(forker);
 		
 		qs.swapFor(this, result);
 		qs.update();
 		
 		return result;
 	}
 	
 	private Question fork(User forker){
 		Question result = new Question(forker);
 		result.isStarred = isStarred;
 		result.timeStamp = new Date();
 		result.parent = this;
 		result.tags = new HashSet<Tag>(tags);
 		result.expectedDuration = expectedDuration;
 		result.content = new Data(content);
 		
 		if (this.getOwner().getSupervisor() && !forker.getSupervisor()) {
 			result.notes = new Data(DataType.EMPTY, null);
 		} else {
 			result.notes = new Data(notes);
 		}
 		
 		result.save();
 		
 		return result;
 	}
 	
 	private Question inPlaceEdit(QuestionEdit qe) throws FormValidationException {
 		log.debug("In place edit for question " + qe.getId());
 		qe.store(this);
 		this.update();
 		
 		return this;
 	}
 	
 	private Question forkAndEdit(User editor, QuestionEdit qe) throws FormValidationException {
 		log.debug("Fork and edit for question " + qe.getId());
 		Question q = this.fork(QuestionSetQuery.get(qe.getSetId()), editor);
 		q.owner = editor;
 		
 		qe.store(q);
 		q.update();
 		
 		return q;
 	}
 	
 	private Question forkAndEditMultiple(User editor, QuestionEdit qe) throws FormValidationException, InvalidInputException{
 		log.debug("Fork and Edit multiple for question " + qe.getId());
 		List<QuestionSet> sets;
 		if(qe.isMinor()){
 			sets = QuestionSetQuery.all().withUser(editor).list();
 		} else {
 			sets = qe.getSets();
 		}
 		/*if(sets == null || sets.size() < 1){
 			throw new InvalidInputException("No sets found to edit the question in.");
 		}*/
 		
 		Question q = this.fork(editor);
 		
 		qe.store(q);
 		q.update();
 		
 		if (sets != null) {
 			for (QuestionSet qs : sets) {
 				qs.swapFor(this, q);
 				qs.update();
 			}
 		}
 		return q;
 	}
 	
 	public Question edit(User editor, QuestionEdit qe) throws FormValidationException, InvalidInputException {
 		log.debug(editor.getId() + " is editing question " + qe.getId() + " in set " + qe.getSetId());
 		boolean inPlace = false;
 		
 		List<Question> forks;
 		forks = QuestionQuery.all().withParent(qe.getId()).list();
 		if(forks == null){forks = new ArrayList<Question>();}
 		
 		if(editor.equals(owner)) {
 			if(qe.isMinor()
 					|| (forks.size() < 1 && 
 						(this.usageCount <= 0 || (qe.getSetId() == -1 && this.usageCount <= qe.getSets().size())))){
 				
 				inPlace = true;
 			}
 		}/* else {
 			List<User> userlist = new ArrayList<User>();
 			userlist.add(editor);
 			if(QuestionSetQuery.all().withOwners(userlist).have(qe.getId()).list().size() < 1){
 				throw new InvalidInputException("No sets available in which to edit this question!");
 			}
 		}*/
 		
 		if (inPlace) {
 			return this.inPlaceEdit(qe);
 		} else if(qe.getSetId() == -1) {
 			return this.forkAndEditMultiple(editor, qe);
 		} else {
 			return this.forkAndEdit(editor, qe);
 		}
 	}
 	
 	public Object clone() throws CloneNotSupportedException {
 		Question q = (Question) super.clone();
 		q.content = (Data) this.content.clone();
 		if (this.notes != null) {
 			q.notes = (Data) this.notes.clone();
 		}
 		
 		return q;
 	}
 	
 	/**
 	 * Puts the question's content into a map.
 	 * 
 	 * @param shadow Determines whether or not the question notes are returned. Shadowed -> don't return notes.
 	 * @return
 	 */
 	public Map<String,Object> toMap(boolean shadow) {
 		Map<String,Object> r =	new HashMap<String,Object>();
 		r.put("id", this.id);
 		r.put("parentid", (parent == null ? null : this.parent.getId()));
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.UK);
		r.put("soyTimeStamp", dateFormat.format(timeStamp));
 		r.put("owner", this.owner);
 		r.put("content", this.content);
 		r.put("starred", this.isStarred);
 		r.put("expectedDuration", this.expectedDuration);
 		r.put("tags", this.tags);
 		
 		if (shadow && this.owner.getSupervisor()) {
 			r.put("notes", null);
 		} else {
 			r.put("notes", this.notes);
 		}
 		
 		
 		return r;
 	}
 	
 	public Map<String,Object> toMap() {
 		return toMap(true);
 	}
 	
 	void use() {
 		this.usageCount++;
 	}
 	
 	void unuse() {
 		this.usageCount--;
 	}
 	
 }
