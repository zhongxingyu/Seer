 package org.idiginfo.docsvc.jpa.citagora;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Vector;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToMany;
 import javax.persistence.PrePersist;
 import javax.persistence.PreUpdate;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.Transient;
 
 import org.idiginfo.docsvc.model.citagora.Author;
 import org.idiginfo.docsvc.model.citagora.CitagoraAgent;
 import org.idiginfo.docsvc.model.citagora.CitagoraDocument;
 import org.idiginfo.docsvc.model.citagora.CitagoraObject;
 import org.idiginfo.docsvc.model.citagora.Comment;
 import org.idiginfo.docsvc.model.citagora.Person;
 import org.idiginfo.docsvc.model.citagora.Reference;
 import org.idiginfo.docsvc.model.citagora.Review;
 import org.idiginfo.docsvc.model.citagora.Tag;
 
 @Entity(name = "people")
 public class PersonImpl implements Person, CitagoraAgent, Author {
     @Id
     @GeneratedValue(strategy = GenerationType.TABLE)
     Integer myId;
     String type;
     String uri;
 
     String givenName;
     String familyName;
     String name;
     String accountName;
     String account;
     String homePage;
     @Temporal(TemporalType.TIMESTAMP)
     Date created;
     @Temporal(TemporalType.TIMESTAMP)
     Date updated;
    Boolean isAuthor = false;
    Boolean isAgent = false;
    Boolean isPerson = false;
 
     // Author fields
     @ManyToMany(mappedBy = "authorList", targetEntity = ReferenceImpl.class, cascade = CascadeType.ALL)
     List<Reference> authorReferences;
 
     // Agent fields
     @OneToMany(mappedBy = "generator", targetEntity = CitagoraObjectImpl.class, cascade = CascadeType.ALL)
     List<CitagoraObject> agentDocuments;
     @OneToMany(mappedBy = "reviewer", targetEntity = CommentImpl.class, cascade = CascadeType.ALL)
     List<Comment> agentComments;
     @OneToMany(mappedBy = "annotator", targetEntity = TagImpl.class, cascade = CascadeType.ALL)
     List<Tag> agentTags;
     @OneToMany(mappedBy = "contributedBy", targetEntity = ReferenceImpl.class, cascade = CascadeType.ALL)
     List<Reference> agentReferences;
     @OneToMany(mappedBy = "reviewer", targetEntity = ReviewImpl.class)
     List<Review> agentReviews;
 
     // Non-persistent members
     @Transient
     transient static int objectId = 0;
     @Transient
     transient String myCollection;
     @Transient
     String id = null;
 
     public PersonImpl() {
 	setPersonType(Person.TYPE);
 	myCollection = Person.COLLECTION;
     }
 
     public PersonImpl(String type) {
 	this();
 	setPersonType(type);
     }
 
     public PersonImpl(Class<?> subclass) {
 	this();
	isAuthor = false;
	isAgent = false;
	isPerson = false;
 	if (Author.class.isInstance(subclass)) {
 	    isAuthor = true;
 	}
 	if (CitagoraAgent.class.isInstance(subclass)) {
 	    isAuthor = true;
 	}
 	if (Person.class.isInstance(subclass)) {
 	    isPerson = true;
 	}
     }
 
     public static Person createCitagoraAgent() {
 	return new PersonImpl(CitagoraAgent.TYPE);
     }
 
     public static Person createAuthor() {
 	return new PersonImpl(Author.TYPE);
     }
 
     /**
      * Perform operations required before persisting an object
      */
     @PrePersist
     protected void onCreate() {
 	updated = new Date();
 	// created may not be the database timestamp. It may be set by the
 	// creator
 	if (created == null)
 	    created = updated;
 	getUri();// make sure uri is not null
     }
 
     /**
      * Perform operations required before updating an object
      */
     @PreUpdate
     protected void onUpdate() {
 	updated = new Date();
     }
 
     public static String makeId(String collection, int myId) {
 	String id = CitagoraObject.NAMESPACE + collection + "/" + myId;
 	return id;
     }
 
     public String getUri() {
 	if (uri == null && myId != null)
 	    uri = makeId(myCollection, myId);
 	return uri;
     }
 
     public String getId() {
 	return getUri();
     }
 
     // private void setMyId(Integer id) {
     // this.myId = id;
     // }
 
     public String getType() {
 	return type;
     }
 
     public void setPersonType(String type) {
 	this.type = type;
     }
 
     public String getGivenName() {
 	return givenName;
     }
 
     public void setGivenName(String givenName) {
 	this.givenName = givenName;
     }
 
     public String getFamilyName() {
 	return familyName;
     }
 
     public void setFamilyName(String familyName) {
 	this.familyName = familyName;
     }
 
     public String getName() {
 	return name;
     }
 
     public void setName(String name) {
 	this.name = name;
     }
 
     public String getAccountName() {
 	return accountName;
     }
 
     public void setAccountName(String accountName) {
 	this.accountName = accountName;
     }
 
     public String getAccount() {
 	return account;
     }
 
     public void setAccount(String account) {
 	this.account = account;
     }
 
     public String getHomePage() {
 	return homePage;
     }
 
     public void setHomePage(String homePage) {
 	this.homePage = homePage;
     }
 
     public Date getCreated() {
 	return created;
     }
 
     public void setCreated(Date created) {
 	this.created = created;
     }
 
     @Override
     public Boolean getIsAuthor() {
 	return isAuthor;
     }
 
     public void setIsAuthor(boolean isAuthor) {
 	this.isAuthor = isAuthor;
     }
 
     @Override
     public Boolean getIsAgent() {
 	return isAgent;
     }
 
     public void setIsAgent(boolean isAgent) {
 	this.isAgent = isAgent;
     }
 
     @Override
     public Boolean getIsPerson() {
 	return isPerson;
     }
 
     public void setIsPerson(boolean isPerson) {
 	this.isPerson = isPerson;
     }
 
     public String getMyCollection() {
 	return myCollection;
     }
 
     public void setMyCollection(String myCollection) {
 	this.myCollection = myCollection;
     }
 
     public Integer getMyId() {
 	return myId;
     }
 
     public Date getUpdated() {
 	return updated;
     }
 
     public List<Reference> getAuthorReferences() {
 	if (authorReferences == null)
 	    authorReferences = new Vector<Reference>();
 	return authorReferences;
     }
 
     public List<Reference> getAgentReferences() {
 	if (agentReferences == null)
 	    agentReferences = new Vector<Reference>();
 	return agentReferences;
     }
 
     @Override
     public List<Comment> getAgentComments() {
 	if (agentComments == null)
 	    agentComments = new Vector<Comment>();
 	return agentComments;
     }
 
     @Override
     public List<Tag> getAgentTags() {
 	if (agentTags == null)
 	    agentTags = new Vector<Tag>();
 	return agentTags;
     }
 
     @Override
     public List<CitagoraObject> getAgentObjects() {
 	if (agentDocuments == null)
 	    agentDocuments = new Vector<CitagoraObject>();
 	return agentDocuments;
     }
 
     @Override
     public List<Review> getAgentReviews() {
 	if (agentReviews == null)
 	    agentReviews = new Vector<Review>();
 	return agentReviews;
     }
 
     // dependent side of many-to-many
     @Override
     public void addAuthorReference(Reference reference) {
 	if (reference != null)
 	    reference.addAuthor(this);
     }
 
     // dependent side of many-to-many
     @Override
     public void removeAuthorReference(Reference reference) {
 	if (reference != null)
 	    reference.removeAuthor(this);
     }
 
     @Override
     public void addAgentDocument(CitagoraDocument document) {
 	if (document != null)
 	    document.setGenerator(this);
     }
 
     @Override
     public void addAgentTag(Tag tag) {
 	if (tag != null)
 	    tag.setAnnotator(this);
     }
 
     @Override
     public void addAgentComment(Comment comment) {
 	if (comment != null)
 	    comment.setAnnotator(this);
     }
 
     @Override
     public void addAgentReview(Review review) {
 	if (review != null)
 	    review.setReviewer(this);
     }
 }
