 package org.idiginfo.docsvc.jpa.citagora;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Vector;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.DiscriminatorValue;
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.Lob;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.UniqueConstraint;
 
 import org.apache.commons.lang.StringUtils;
 import org.idiginfo.docsvc.model.citagora.Author;
 import org.idiginfo.docsvc.model.citagora.CitagoraAgent;
 import org.idiginfo.docsvc.model.citagora.Container;
 import org.idiginfo.docsvc.model.citagora.Reference;
 import org.idiginfo.docsvc.model.apisvc.GsonTransient;
 
 import com.google.gson.annotations.SerializedName;
 
 /**
  * Class to implement the Citagora persistence Reference object
  * 
  * @author griccardi
  * 
  */
 
 @Entity
 @Table(name = "citagora_references", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "ref_source", "doi" }),
		@UniqueConstraint(columnNames = { "ref_source", "pmid" }) })
 @DiscriminatorValue(value = "reference")
 public class ReferenceImpl extends CitagoraObjectImpl implements Reference {
 
 	@Lob
 	// @Column(length = 10000)
 	@SerializedName("abstract_")
 	String abstractText; // 'abstract' is a Java keyword
 	@Column(length = 1000)
 	@SerializedName("title")
 	String title;
 	@Column(length = 1000)
 	@SerializedName("subject")
 	String subject;
 	@SerializedName("language")
 	String language;
 	@SerializedName("pageStart")
 	Integer pageStart;
 	@SerializedName("pageEnd")
 	Integer pageEnd;
 	@SerializedName("volume")
 	String volume;
 	@SerializedName("biboType")
 	String biboType;
 	@Temporal(TemporalType.TIMESTAMP)
 	@SerializedName("issued")
 	Date issued;
 	@SerializedName("pmid")
 	String pmid;
 	@SerializedName("doi")
 	String doi;
 	// refSource is duplicated with superclass to allow for unique constraint
 	@Column(name = "ref_source")
 	@SerializedName("refSource")
 	String refSource;
 	@Lob
 	// @Column(length = 1000)
 	@SerializedName("authorString")
 	String authorString;
 	@Column(unique = true)
 	@SerializedName("issn")
 	String issn;
 	@Column(unique = true)
 	@SerializedName("isbn")
 	String isbn;
 	@SerializedName("issue")
 	String issue;
 	@SerializedName("url")
 	String url;
 	@Lob
 	@SerializedName("keywords")
 	String keywords;
 	@SerializedName("meshTerms")
 	String meshTerms;
 
 	@ManyToOne(targetEntity = ReferenceImpl.class, cascade = CascadeType.ALL)
 	@JoinColumn(name = "isPartOf")
 	@GsonTransient
 	Reference isPartOf;
 
 	@OneToMany(mappedBy = "isPartOf", targetEntity = ReferenceImpl.class, cascade = CascadeType.ALL)
 	List<Reference> contains;
 
 	@OneToMany(mappedBy = "isAbout", targetEntity = ContainerImpl.class, cascade = CascadeType.ALL)
 	@GsonTransient
 	List<Container> containers;
 
 	@ManyToMany(targetEntity = PersonImpl.class, cascade = CascadeType.ALL)
 	@JoinTable(name = "reference_authors")
 	List<Author> authorList;
 
 	@ManyToMany(targetEntity = ReferenceImpl.class, cascade = CascadeType.ALL)
 	@JoinTable(name = "reference_citations")
 	@GsonTransient
 	List<Reference> isCitedBy;
 	@ManyToMany(mappedBy = "isCitedBy", targetEntity = ReferenceImpl.class, cascade = CascadeType.ALL)
 	List<Reference> citationList;
 
 	@SerializedName("seeAlso")
 	List<String> seeAlso;
 
 	@ManyToOne(targetEntity = PersonImpl.class, cascade = CascadeType.ALL)
 	CitagoraAgent contributedBy;
 
 	@SerializedName("shortTitle")
 	String shortTitle;
 	@SerializedName("publisher")
 	String publisher;
 	@SerializedName("pages")
 	String pages;
 
 	@SerializedName("readabilityRating")
 	Double readabilityRating;
 	@SerializedName("overallRating")
 	Double overallRating;
 	@SerializedName("accuracyRating")
 	Double accuracyRating;
 	@SerializedName("originalityRating")
 	Double originalityRating;
 
 	@SerializedName("aggregationType")
 	String aggregationType;
 	@SerializedName("arXivId")
 	String arXivId;
 	@SerializedName("coverDate")
 	String coverDate;
 	@SerializedName("edition")
 	String edition;
 	@SerializedName("eIssn")
 	String eIssn;
 	@SerializedName("genre")
 	String genre;
 	@SerializedName("authorNotes")
 	String authorNotes;
 	@SerializedName("number")
 	String itemNumber;
 	@SerializedName("publicationDate")
 	String publicationDate;
 	@Column(name = "doc_rights")
 	@SerializedName("docRights")
 	String docRights;
 	@SerializedName("isPartOf")
 	String seriesTitle;
 
 	public ReferenceImpl() {
 		type = Reference.TYPE;
 		setCollection(Reference.COLLECTION);
 		// initId();
 	}
 
 	@Override
 	public String getType() {
 		if (biboType != null)
 			return biboType;
 		return Reference.TYPE;
 	}
 
 	public String getAbstract() {
 		return abstractText;
 	}
 
 	public void setAbstract(String abstractText) {
 		this.abstractText = abstractText;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public Integer getPageStart() {
 		return pageStart;
 	}
 
 	public void setPageStart(Integer pageStart) {
 		this.pageStart = pageStart;
 	}
 
 	public Integer getPageEnd() {
 		return pageEnd;
 	}
 
 	public void setPageEnd(int pageEnd) {
 		this.pageEnd = pageEnd;
 	}
 
 	public String getVolume() {
 		return volume;
 	}
 
 	public void setVolume(String volume) {
 		this.volume = volume;
 	}
 
 	public Date getIssued() {
 		return issued;
 	}
 
 	public void setIssued(Date issued) {
 		this.issued = issued;
 	}
 
 	public String getPmid() {
 		return pmid;
 	}
 
 	public void setPmid(String pmid) {
 		this.pmid = pmid;
 	}
 
 	public String getDoi() {
 		return doi;
 	}
 
 	public void setDoi(String doi) {
 		if (doi == null || !doi.startsWith("10.")) {
 			doi = null;
 		}
 		this.doi = doi;
 	}
 
 	public Date getGeneratedAtTime() {
 		return generated;
 	}
 
 	public void setGeneratedAtTime(Date generatedAtTime) {
 		this.generated = generatedAtTime;
 	}
 
 	public Reference getIsPartOf() {
 		return isPartOf;
 	}
 
 	/**
 	 * Set both sides of the relationship, carefully. This code is repeated for
 	 * every ManyToOne field.
 	 */
 	@Override
 	public void setIsPartOf(Reference isPartOf) {
 		// do nothing if relationship not changed
 		if (this.isPartOf == isPartOf)
 			return;
 		// remove from inverse relationship
 		if (this.isPartOf != null) {
 			this.isPartOf.getContains().remove(this);
 		}
 		// set forward relationship
 		this.isPartOf = isPartOf;
 		if (isPartOf == null)
 			return;
 		// set inverse relationship
 		isPartOf.getContains().add(this);
 	}
 
 	public List<Author> getAuthors() {
 		if (authorList == null)
 			authorList = new Vector<Author>();
 		return authorList;
 	}
 
 	public List<Reference> getCitationList() {
 		if (citationList == null)
 			citationList = new Vector<Reference>();
 		return citationList;
 	}
 
 	public List<String> getSeeAlso() {
 		if (seeAlso == null)
 			seeAlso = new Vector<String>();
 		return seeAlso;
 	}
 
 	public List<Container> getContainers() {
 		if (containers == null)
 			containers = new Vector<Container>();
 		return containers;
 	}
 
 	public Double getOverallRating() {
 		return overallRating;
 	}
 
 	public void setOverallRating(Double overallRating) {
 		this.overallRating = overallRating;
 	}
 
 	@Override
 	public Reference isPartOf() {
 		return isPartOf;
 	}
 
 	@Override
 	public Double getReadabilityRating() {
 		return readabilityRating;
 	}
 
 	@Override
 	public Double getAccuracyRating() {
 		return accuracyRating;
 	}
 
 	@Override
 	public Double getOriginalityRating() {
 		return originalityRating;
 	}
 
 	@Override
 	public void addContainer(Container document) {
 		if (document != null)
 			document.setIsAbout(this);
 	}
 
 	public void addSeeAlso(String link) {
 		if (link == null)
 			return;
 		getSeeAlso().add(link);
 	}
 
 	public String getAbstractText() {
 		return abstractText;
 	}
 
 	public void setAbstractText(String abstractText) {
 		this.abstractText = abstractText;
 	}
 
 	public String getLanguage() {
 		return language;
 	}
 
 	public void setLanguage(String language) {
 		this.language = language;
 	}
 
 	public void setSubject(String subject) {
 		this.subject = subject;
 	}
 
 	public String getSubject() {
 		return subject;
 	}
 
 	public String getShortTitle() {
 		return shortTitle;
 	}
 
 	public void setShortTitle(String shortTitle) {
 		this.shortTitle = shortTitle;
 	}
 
 	public String getPublisher() {
 		return publisher;
 	}
 
 	public void setPublisher(String publisher) {
 		this.publisher = publisher;
 	}
 
 	public String getPages() {
 		return pages;
 	}
 
 	public void setPages(String pages) {
 		this.pages = pages;
 	}
 
 	@Override
 	public List<Reference> getContains() {
 		if (contains == null)
 			contains = new Vector<Reference>();
 		return contains;
 	}
 
 	@Override
 	public List<Reference> getIsCitedBy() {
 		if (isCitedBy == null)
 			isCitedBy = new Vector<Reference>();
 		return isCitedBy;
 	}
 
 	@Override
 	public CitagoraAgent getContributedBy() {
 		return contributedBy;
 	}
 
 	@Override
 	public String getBiboType() {
 		return biboType;
 	}
 
 	@Override
 	public void setBiboType(String biboType) {
 		this.biboType = biboType;
 	}
 
 	/**
 	 * Set both sides of the relationship, carefully. This code is repeated for
 	 * every ManyToOne field.
 	 */
 	@Override
 	public void setContributedBy(CitagoraAgent contributedBy) {
 		// do nothing if relationship not changed
 		if (this.contributedBy == contributedBy)
 			return; // no change
 		// remove from inverse relationship
 		if (this.contributedBy != null) {
 			this.contributedBy.getAgentReferences().remove(this);
 		}
 		// set forward relationship
 		this.contributedBy = contributedBy;
 		// set inverse relationship
 		if (contributedBy == null)
 			return;
 		contributedBy.getAgentReferences().add(this);
 	}
 
 	@Override
 	public void setOverallRating(double rating) {
 		this.overallRating = rating;
 	}
 
 	@Override
 	public void setReadabilityRating(double rating) {
 		readabilityRating = rating;
 	}
 
 	@Override
 	public void setAccuracyRating(double rating) {
 		accuracyRating = rating;
 	}
 
 	@Override
 	public void setOriginalityRating(double rating) {
 		originalityRating = rating;
 	}
 
 	public List<Author> getAuthorList() {
 		if (authorList == null)
 			authorList = new Vector<Author>();
 		return authorList;
 	}
 
 	public void setPageEnd(Integer pageEnd) {
 		this.pageEnd = pageEnd;
 	}
 
 	public void setReadabilityRating(Double readabilityRating) {
 		this.readabilityRating = readabilityRating;
 	}
 
 	public void setAccuracyRating(Double accuracyRating) {
 		this.accuracyRating = accuracyRating;
 	}
 
 	public void setOriginalityRating(Double originalityRating) {
 		this.originalityRating = originalityRating;
 	}
 
 	public Integer getMyId() {
 		return myId;
 	}
 
 	/**
 	 * Set authors from comma-separated string of author names
 	 */
 	@Override
 	public void setAuthors(String authors) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void setAuthorString(String authorString) {
 		// TODO look up authors?
 		this.authorString = authorString;
 	}
 
 	@Override
 	public String getAuthorString() {
 		if (getAuthorList().size() > 0) {
 			// TODO return concat of author names!
 		}
 		return authorString;
 	}
 
 	// owner side of many-to-many
 	@Override
 	public void addAuthor(Author author) {
 		List<Author> authorList = getAuthorList();
 		// check for existing relationship
 		if (author == null || authorList.contains(author))
 			return;
 		// add both sides of relationship
 		author.getAuthorReferences().add(this);
 		authorList.add(author);
 	}
 
 	// owner side of many-to-many
 	@Override
 	public void removeAuthor(Author author) {
 		List<Author> authorList = getAuthorList();
 		// check for existing relationship
 		if (author == null || !authorList.contains(author))
 			return;
 		// add both sides of relationship
 		author.getAuthorReferences().remove(this);
 		authorList.remove(author);
 	}
 
 	// dependent side of many-to-many
 	@Override
 	public void addCitation(Reference citation) {
 		if (citation != null)
 			citation.addIsCitedBy(this);
 
 	}
 
 	// dependent side of many-to-many
 	@Override
 	public void removeCitation(Reference citation) {
 		if (citation != null)
 			citation.removeIsCitedBy(this);
 	}
 
 	// owner side of many-to-many
 	@Override
 	public void addIsCitedBy(Reference reference) {
 		List<Reference> isCitedBy = getIsCitedBy();
 		// check for existing relationship
 		if (reference == null || isCitedBy.contains(reference))
 			return;
 		// add both sides of relationship
 		reference.getCitationList().add(this);
 		isCitedBy.add(reference);
 	}
 
 	// owner side of many-to-many
 	@Override
 	public void removeIsCitedBy(Reference reference) {
 		List<Reference> isCitedBy = getIsCitedBy();
 		// check for no existing relationship
 		if (reference == null || !isCitedBy.contains(reference))
 			return;
 		// remove both sides of relationship
 		reference.getCitationList().remove(this);
 		isCitedBy.remove(reference);
 
 	}
 
 	@Override
 	public void setIssn(String issn) {
 		this.issn = issn;
 	}
 
 	@Override
 	public String getIssn() {
 		return issn;
 	}
 
 	@Override
 	public void setIssue(String issue) {
 		this.issue = issue;
 	}
 
 	@Override
 	public String getIssue() {
 		return issue;
 	}
 
 	static final String DOI_PREFIX = "doi:";
 	static final String ISSN_PREFIX = "urn:issn:";
 	static final String ISBN_PREFIX = "urn:isbn:";
 
 	@Override
 	/**
 	 * Select usable URI for a reference object
 	 */
 	public String getUri() {
 		if (doi != null) {
 			if (doi.startsWith(DOI_PREFIX))
 				return doi;
 			return DOI_PREFIX + doi;
 		}
 		if (issn != null) {
 			if (issn.startsWith(ISSN_PREFIX))
 				return issn;
 			return ISSN_PREFIX + issn;
 		}
 		if (isbn != null) {
 			if (issn.startsWith(ISBN_PREFIX))
 				return isbn;
 			return ISBN_PREFIX + isbn;
 		}
 		return uri;
 	}
 
 	@Override
 	public String getIsbn() {
 		return isbn;
 	}
 
 	@Override
 	public void setIsbn(String isbn) {
 		this.isbn = isbn;
 	}
 
 	@Override
 	public String getUrl() {
 		return url;
 	}
 
 	@Override
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	public String getKeywords() {
 		return keywords;
 	}
 
 	public void setKeywords(List<String> keywords) {
 		String keywordString = StringUtils.join(keywords, ',');
 		this.keywords = keywordString;
 	}
 
 	public String getMeshTerms() {
 		return meshTerms;
 	}
 
 	public void setMeshTerms(List<String> meshTerms) {
 		String meshString = StringUtils.join(meshTerms, ',');
 		this.meshTerms = meshString;
 	}
 
 	public String getAggregationType() {
 		return aggregationType;
 	}
 
 	public void setAggregationType(String aggregationType) {
 		this.aggregationType = aggregationType;
 	}
 
 	public String getArXivId() {
 		return arXivId;
 	}
 
 	public void setArXivId(String arXivId) {
 		this.arXivId = arXivId;
 	}
 
 	public String getCoverDate() {
 		return coverDate;
 	}
 
 	public void setCoverDate(String coverDate) {
 		this.coverDate = coverDate;
 	}
 
 	public String getEdition() {
 		return edition;
 	}
 
 	public void setEdition(String edition) {
 		this.edition = edition;
 	}
 
 	public String geteIssn() {
 		return eIssn;
 	}
 
 	public void seteIssn(String eIssn) {
 		this.eIssn = eIssn;
 	}
 
 	public String getGenre() {
 		return genre;
 	}
 
 	public void setGenre(String genre) {
 		this.genre = genre;
 	}
 
 	public String getAuthorNotes() {
 		return authorNotes;
 	}
 
 	public void setAuthorNotes(String authorNotes) {
 		this.authorNotes = authorNotes;
 	}
 
 	public String getItemNumber() {
 		return itemNumber;
 	}
 
 	public void setItemNumber(String itemNumber) {
 		this.itemNumber = itemNumber;
 	}
 
 	public String getPublicationDate() {
 		return publicationDate;
 	}
 
 	public void setPublicationDate(String publicationDate) {
 		this.publicationDate = publicationDate;
 	}
 
 	public String getRights() {
 		return docRights;
 	}
 
 	public void setRights(String rights) {
 		this.docRights = rights;
 	}
 
 	public String getSeriesTitle() {
 		return seriesTitle;
 	}
 
 	public void setSeriesTitle(String seriesTitle) {
 		this.seriesTitle = seriesTitle;
 	}
 
 	@Override
 	public String getSource() {
 		// make sure that this.source and super.source are consistent
 		if (super.source != refSource) {
 			if (refSource == null) {
 				refSource = super.source;
 			} else {
 				super.source = refSource;
 			}
 		}
 		return refSource;
 	}
 
 	@Override
 	public void setSource(String source) {
 		this.refSource = source;
 		super.source = source;
 	}
 
 }
