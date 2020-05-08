 package cl.votainteligente.inspector.model;
 
 import net.sf.gilead.pojo.gwt.LightEntity;
 
 import java.util.Date;
 import java.util.Set;
 
public class Society extends LightEntity implements Comparable<Society> {
 	private Long id;
 	private SocietyType societyType;
 	private SocietyStatus societyStatus;
 	private Date creationDate;
 	private Date publishDate;
 	private Notary notary;
 	private String address;
 	private String name;
 	private String fantasyName;
 	private String uid;
 	private String classification;
 	private Set<Person> members;
 	private String subject;
 	private Long initialStock;
 	private Long currentStock;
 	private Set<Category> categories;
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public SocietyType getSocietyType() {
 		return societyType;
 	}
 
 	public void setSocietyType(SocietyType societyType) {
 		this.societyType = societyType;
 	}
 
 	public SocietyStatus getSocietyStatus() {
 		return societyStatus;
 	}
 
 	public void setSocietyStatus(SocietyStatus societyStatus) {
 		this.societyStatus = societyStatus;
 	}
 
 	public Date getCreationDate() {
 		return creationDate;
 	}
 
 	public void setCreationDate(Date creationDate) {
 		this.creationDate = creationDate;
 	}
 
 	public Date getPublishDate() {
 		return publishDate;
 	}
 
 	public void setPublishDate(Date publishDate) {
 		this.publishDate = publishDate;
 	}
 
 	public Notary getNotary() {
 		return notary;
 	}
 
 	public void setNotary(Notary notary) {
 		this.notary = notary;
 	}
 
 	public String getAddress() {
 		return address;
 	}
 
 	public void setAddress(String address) {
 		this.address = address;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getFantasyName() {
 		return fantasyName;
 	}
 
 	public void setFantasyName(String fantasyName) {
 		this.fantasyName = fantasyName;
 	}
 
 	public String getUid() {
 		return uid;
 	}
 
 	public void setUid(String uid) {
 		this.uid = uid;
 	}
 
 	public String getClassification() {
 		return classification;
 	}
 
 	public void setClassification(String classification) {
 		this.classification = classification;
 	}
 
 	public Set<Person> getMembers() {
 		return members;
 	}
 
 	public void setMembers(Set<Person> members) {
 		this.members = members;
 	}
 
 	public String getSubject() {
 		return subject;
 	}
 
 	public void setSubject(String subject) {
 		this.subject = subject;
 	}
 
 	public Long getInitialStock() {
 		return initialStock;
 	}
 
 	public void setInitialStock(Long initialStock) {
 		this.initialStock = initialStock;
 	}
 
 	public Long getCurrentStock() {
 		return currentStock;
 	}
 
 	public void setCurrentStock(Long currentStock) {
 		this.currentStock = currentStock;
 	}
 
 	public Set<Category> getCategories() {
 		return categories;
 	}
 
 	public void setCategories(Set<Category> categories) {
 		this.categories = categories;
 	}

	@Override
	public int compareTo(Society obj) {
		return (obj == null || obj.getName() == null) ? -1 : -obj.getName().compareTo(name);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}

		if (!(other instanceof Society)) {
			return false;
		}

		if (getId() == null) {
			return false;
		}

		return getId().equals(((Society) other).getId());
	}

	@Override
	public int hashCode() {
			return this.id.hashCode();
	}
 }
