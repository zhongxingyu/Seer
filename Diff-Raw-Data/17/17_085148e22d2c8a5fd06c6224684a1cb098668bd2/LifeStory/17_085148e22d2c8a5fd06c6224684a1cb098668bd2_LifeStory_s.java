 package models;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.*;
 
 import play.data.validation.Constraints.*;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.hibernate.annotations.Type;
 import org.joda.time.DateTime;
 
 import play.db.ebean.Model;
 import play.i18n.Messages;
 
 @Entity
 @Table(name = "Life_Event")
 public class LifeStory extends Model {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -4442579968783942558L;
 
 	@Id
 	@GeneratedValue
 	@Column(name = "life_event_id")
 	private Long lifeStoryId;
 
 	@Required
 	@Column
 	private String headline;
 
 	@Column
 	private String text;
 
 	@Column
 	private String richtext;
 
 	@Column
 	private String type;
 
 	@Column
 	private Integer visibility;
 
 	@Column(name = "contributor_id")
 	private Long contributorId;
 
 	@Temporal(TemporalType.DATE)
 	@Column
 	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
 	private DateTime creationDate;
 
 	@Column
 	private String locale;
 
 	@ManyToOne
 	@MapsId
 	@JoinColumn(name = "location_id", updatable=true, insertable=true)
 	private Location location;
 
 	@ManyToOne
 	@MapsId
 	@JoinColumn(name = "question_id")
 	private Question question;
 
 	@ManyToOne
 	@MapsId
 	@JoinColumn(name = "fuzzy_startdate", updatable=true, insertable=true)
 	private FuzzyDate startDate;
 
 	@ManyToOne
 	@MapsId
 	@JoinColumn(name = "fuzzy_enddate", updatable=true, insertable=true)
 	private FuzzyDate endDate;
 
 	@OneToMany(mappedBy = "lifeStory", cascade = CascadeType.ALL)
 	private List<Memento> mementoList;
 
 	@JsonIgnore
 	@OneToMany(mappedBy = "lifeStory", cascade = CascadeType.ALL)
 	private List<Participation> participationList;
 
 	@Transient
 	private boolean synced;
 
 	public static Model.Finder<Long, LifeStory> find = new Model.Finder<Long, LifeStory>(
 			Long.class, LifeStory.class);
 
 	public static List<LifeStory> all() {
 		return find.all();
 	}
 
 	public static void create(LifeStory lifestory) {
 
 		// 1. Data to save before creating the new life story
 		FuzzyDate start = lifestory.getStartDate();
 		FuzzyDate end = lifestory.getEndDate();
 		Location place = lifestory.getLocation();
 		String storyLang = lifestory.getLocale();
 		
 		if (start!=null)
 			start.setLocale(storyLang);
 		
 		if (end!=null) 
 			end.setLocale(storyLang);
 		
 		if (place!=null)
 			place.setLocale(storyLang);
 		
 		// we need at least a "start"
 		lifestory.setStartDate(FuzzyDate.createOrUpdateIfNotExist(start));
 		if (end != null) {
 			lifestory.setEndDate(FuzzyDate.createOrUpdateIfNotExist(end));	
 		}
 		
 		lifestory.setLocation(Location.createOrUpdateIfNotExist(place));
 		
 		// 2. Save the new life story
 		lifestory.setSynced(true);
 		lifestory.setCreationDate(DateTime.now());
 		lifestory.save();
 
 		// 3. things to save after creating the life story
 		List<Participation> participants = lifestory.getParticipationList();
 		for (Participation participation : participants) {
 			Participation.create(participation);
 		}
 
 		List<Memento> mementoList = lifestory.getMementoList();
 		for (Memento memento : mementoList) {
 			Memento.create(memento);
 		}
 	}
 
 	public static LifeStory createObject(LifeStory lifestory) {
 		lifestory.save();
 		return lifestory;
 	}
 
 	public static LifeStory update(LifeStory lifestory) {
 		// 1. Update first dates and locations
 		FuzzyDate start = lifestory.getStartDate();
 		FuzzyDate end = lifestory.getEndDate();
 		Location place = lifestory.getLocation();
 
 		if (start != null) {
 			start = FuzzyDate.createOrUpdateIfNotExist(start);
 			lifestory.setStartDate(start);
 		}
 
 		if (end != null) {
 			end = FuzzyDate.createOrUpdateIfNotExist(start);
 			lifestory.setStartDate(end);
 		}
 
 		if (place != null) {
 			place = Location.createOrUpdateIfNotExist(place);
 			lifestory.setLocation(place);
 		}
 
 		Long id = lifestory.getLifeStoryId();
 		lifestory.update(id);
 		lifestory.refresh();
 		return lifestory;
 	}
 	
 	public static void delete(Long id) {
 		find.ref(id).delete();
 	}
 
 	public static LifeStory read(Long id) {
 		return find.byId(id);
 	}
 
 	public static List<LifeStory> readByPerson(Long personId) {
 		List<Participation> participationList = Participation
 				.participationByPersonProtagonist(personId);
 		List<LifeStory> lifeStories = new ArrayList<LifeStory>();
 		for (Participation participation : participationList) {
 			LifeStory ls = participation.getLifeStory();
 			lifeStories.add(ls);
 		}
 		return lifeStories;
 	}
 
 	public static List<LifeStory> readByPersonWithLimits(Long personId,
 			Long from, Long to) {
 		// TODO change to a specialized query
 		List<Participation> participationList = Participation
 				.participationByPersonProtagonist(personId);
 		List<LifeStory> lifeStories = new ArrayList<LifeStory>();
 		for (int i = from.intValue(); i < participationList.size(); i++) {
 			Participation participation = participationList.get(i);
 			LifeStory ls = participation.getLifeStory();
 			lifeStories.add(ls);
 			if (i >= to.intValue()) {
 				break;
 			}
 		}
 		return lifeStories;
 	}
 	
 
 	public static List<LifeStory> readByPersonByDecade(Long personId,
 			Long decade) {
 		// TODO change to a specialized query
 		List<Participation> participationList = Participation
 			.participationByPersonProtagonist(personId);
 //		find.where()
 //			.eq("participationList.person.personId", personId)
 //			.or(		
 //		
 //		
 		List<LifeStory> lifeStories = new ArrayList<LifeStory>();
 		for (int i = 0 ; i < participationList.size(); i++) {
 			Participation participation = participationList.get(i);
 			LifeStory ls = participation.getLifeStory();
 			FuzzyDate d = ls.getStartDate();
 			
 			Long fuzzydecade = d.getDecade();
 			Long year = d.getYear();
 			DateTime exact = d.getExactDate();
 			
 			if (exact != null) {
 				year = new Long(exact.getYear());
 			} else if (fuzzydecade != null) {
 				year = fuzzydecade;
 			}
 			
 			if (year >= decade && year < (decade+10)) {
 				lifeStories.add(ls);
 			}
 		}
 		return lifeStories;
 	}
 
 	public static List<LifeStory> readByPersonIncludingNonProtagonist(
 			Long personId) {
 		List<Participation> participationList = Participation
 				.participationByPerson(personId);
 		List<LifeStory> lifeStories = new ArrayList<LifeStory>();
 		for (Participation participation : participationList) {
 			LifeStory ls = participation.getLifeStory();
 			lifeStories.add(ls);
 		}
 		return lifeStories;
 	}
 
 	public void addParticipant(Participation p) {
 		List<Participation> participationList = this.participationList;
 		Long id = p.getPerson().getPersonId();
 		Boolean alreadyIn = false;
 		for (Participation participation : participationList) {
 			alreadyIn = id == participation.getPerson().getPersonId();
 			if (alreadyIn) {
 				break;
 			}
 		}
 		if (!alreadyIn) {
 			Participation.create(p);
 			this.participationList.add(p);
 		}
 
 	}
 
 	public void deleteParticipant(Long id) throws Exception {
 		List<Participation> participationList = this.participationList;
 		for (Participation participation : participationList) {
 			if (id == participation.getPerson().getPersonId()) {
 				// found!
 				if (!participation.isProtagonist()) {
 					participation.delete();
 				} else {
 					throw new Exception(
 							"Participant is a protagonist. Protagonist can only be deleted by themselves.");
 				}
 				break;
 			}
 		}
 	}
 
 	public void deleteProtagonist(Long id) {
 		List<Participation> participationList = this.participationList;
 		for (Participation participation : participationList) {
 			if (id == participation.getPerson().getPersonId()) {
 				// found!
 				participation.delete();
 				break;
 			}
 		}
 	}
 
 	public List<Memento> getMementoList() {
 		return mementoList;
 	}
 
 	public void setMementoList(List<Memento> mementoList) {
 		this.mementoList = mementoList;
 	}
 
 	public Long getLifeStoryId() {
 		return lifeStoryId;
 	}
 
 	public void setLifeStoryId(Long lifeStoryId) {
 		this.lifeStoryId = lifeStoryId;
 	}
 
 	public String getHeadline() {
 		return headline;
 	}
 
 	public void setHeadline(String headline) {
 		this.headline = headline;
 	}
 
 	public String getText() {
 		return text;
 	}
 
 	public void setText(String text) {
 		this.text = text;
 	}
 
 	public String getRichtext() {
 		return richtext;
 	}
 
 	public void setRichtext(String richtext) {
 		this.richtext = richtext;
 	}
 
 	public String getType() {
 		return type;
 	}
 
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	public Integer getVisibility() {
 		return visibility;
 	}
 
 	public void setVisibility(Integer visibility) {
 		this.visibility = visibility;
 	}
 
 	public Long getContributorId() {
 		return contributorId;
 	}
 
 	public void setContributorId(Long contributorId) {
 		this.contributorId = contributorId;
 	}
 
 	public DateTime getCreationDate() {
 		return creationDate;
 	}
 
 	public void setCreationDate(DateTime creation_date) {
 		this.creationDate = creation_date;
 	}
 
 	public String getLocale() {
 		return locale;
 	}
 
 	public void setLocale(String locale) {
 		this.locale = locale;
 	}
 
 	public Location getLocation() {
 		return location;
 	}
 
 	public void setLocation(Location location) {
 		this.location = location;
 	}
 
 	public Question getQuestion() {
 		return question;
 	}
 
 	public void setQuestion(Question question) {
 		this.question = question;
 	}
 
 	public FuzzyDate getStartDate() {
 		return startDate;
 	}
 
 	public void setStartDate(FuzzyDate startDate) {
 		this.startDate = startDate;
 	}
 
 	public FuzzyDate getEndDate() {
 		return endDate;
 	}
 
 	public void setEndDate(FuzzyDate endDate) {
 		this.endDate = endDate;
 	}
 
 	public boolean isSynchronized() {
 		return this.synced;
 	}
 
 	public void setSynced(boolean synced) {
 		this.synced = synced;
 	}
 
 	public List<Participation> getParticipationList() {
 		return participationList;
 	}
 
 	public void setParticipationList(List<Participation> participationList) {
 		this.participationList = participationList;
 	}
 
 	public boolean isSynced() {
 		return synced;
 	}
 
 	public static void createBirthStory(User user) {
 		LifeStory birth = new LifeStory();
 		birth.setHeadline(Messages.get("reminiscens.birth.headline"));
 		birth.setText(Messages.get("reminiscens.birth.text"));
 		birth.setContributorId(user.getUserId());
 		birth.setCreationDate(DateTime.now());
 		birth.setLocale(user.getLocale());
 
 		DateTime birthdate = user.getPerson().getBirthdate();
 		FuzzyDate fuzzyBirth = new FuzzyDate();
 		try {
 			fuzzyBirth.setExactDate(birthdate);
 		} catch (ParseException e) {
 			// TODO change to stop user from being created if no birthdate is provided
 		}
 		FuzzyDate.createOrUpdateIfNotExist(fuzzyBirth);
 		birth.setStartDate(fuzzyBirth);
 		
 		City birthPlace = user.getPerson().getBirthplace();
 		Location loc = new Location();
 		loc.setCity(birthPlace);
 		birth.setLocation(loc);
 		
 		Participation part = new Participation();
 		part.setContributorId(user.getUserId());
 		part.setProtagonist(true);
 		part.setPerson(user.getPerson());
 		part.setLifeStory(birth);
		birth.addParticipant(part);
		
		
		birth.save();
		birth.refresh();
 	}
 }
