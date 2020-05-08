 package cl.votainteligente.legislativo.model;
 
import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Date;
 import java.util.Set;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.hibernate.annotations.Type;
 
 import cl.votainteligente.legislativo.model.domainobjects.DebateDO;
 
 @Entity
 @Table(name = "debate")
 public class Debate {
 	@Id
 	@GeneratedValue
 	@Column
 	private Long id;
 
 	@ManyToOne
 	@JoinColumn(name = "bill_id", nullable = false)
 	private Bill bill;
 
 	@ManyToOne
 	@JoinColumn(name = "chamber_id", nullable = false)
 	private Chamber chamber;
 
 	@Column
 	private Date date;
 
 	@ManyToOne
 	@JoinColumn(name = "discussion_type_id")
 	private DiscussionType discussionType;
 
 	@Column
 	private String topic;
 
 	@Column
 	@Type(type = "text")
 	private String debate;
 
 	@ManyToMany
 	@JoinColumn(name = "debate_id", nullable = true)
 	private Set<Tag> tags;
 
 	@Column(name = "doc_url")
	private String docUrl;
 
 	@Column(name = "abstract_title")
 	private String abstractTitle;
 
 	@Column(name = "abstract_text")
 	@Type(type = "text")
 	private String abstractText;
 
 	@Column(name = "created_at")
 	private Date createdAt;
 
 	@Column(name = "updated_at")
 	private Date updatedAt;
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public Bill getBill() {
 		return bill;
 	}
 
 	public void setBill(Bill bill) {
 		this.bill = bill;
 	}
 
 	public Chamber getChamber() {
 		return chamber;
 	}
 
 	public void setChamber(Chamber chamber) {
 		this.chamber = chamber;
 	}
 
 	public Date getDate() {
 		return date;
 	}
 
 	public void setDate(Date date) {
 		this.date = date;
 	}
 
 	public DiscussionType getDiscussionType() {
 		return discussionType;
 	}
 
 	public void setDiscussionType(DiscussionType discussionType) {
 		this.discussionType = discussionType;
 	}
 
 	public String getTopic() {
 		return topic;
 	}
 
 	public void setTopic(String topic) {
 		this.topic = topic;
 	}
 
 	public String getDebate() {
 		return debate;
 	}
 
 	public void setDebate(String debate) {
 		this.debate = debate;
 	}
 
 	public Set<Tag> getTags() {
 		return tags;
 	}
 
 	public void setTags(Set<Tag> tags) {
 		this.tags = tags;
 	}
 
 	public URL getDocUrl() {
		try {
			return new URL(docUrl);
		} catch (MalformedURLException e) {
			return null;
		}
 	}
 
	public void setDocUrl(String docUrl) {
 		this.docUrl = docUrl;
 	}
 
 	public String getAbstractTitle() {
 		return abstractTitle;
 	}
 
 	public void setAbstractTitle(String abstractTitle) {
 		this.abstractTitle = abstractTitle;
 	}
 
 	public String getAbstractText() {
 		return abstractText;
 	}
 
 	public void setAbstractText(String abstractText) {
 		this.abstractText = abstractText;
 	}
 
 	public Date getCreatedAt() {
 		return createdAt;
 	}
 
 	public void setCreatedAt(Date createdAt) {
 		this.createdAt = createdAt;
 	}
 
 	public Date getUpdatedAt() {
 		return updatedAt;
 	}
 
 	public void setUpdatedAt(Date updatedAt) {
 		this.updatedAt = updatedAt;
 	}
 
 	@Transient
 	public DebateDO asDomainObject() {
 		return new DebateDO(this);
 	}
 
 }
