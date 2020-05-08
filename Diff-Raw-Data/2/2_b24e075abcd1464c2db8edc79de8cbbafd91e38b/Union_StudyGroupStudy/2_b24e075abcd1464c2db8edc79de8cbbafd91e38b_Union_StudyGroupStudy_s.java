 package org.gesis.discovery;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.FetchType;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.MappedSuperclass;
 import javax.persistence.OneToOne;
 
 import org.gesis.dcterms.Location;
 import org.gesis.dcterms.PeriodOfTime;
 import org.gesis.foaf.Agent;
 import org.gesis.foaf.Document;
 import org.gesis.rdf.LangString;
 import org.gesis.rdfs.Resource;
 import org.gesis.skos.Concept;
 
 @MappedSuperclass
 public abstract class Union_StudyGroupStudy extends Resource
 {
 	// properties
 
 	@OneToOne( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
	@Column( name = "abstract" )
 	private LangString abstract_;
 
 	@OneToOne( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	private LangString alternative;
 
 	@Column
 	private Date available;
 
 	@OneToOne( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	private LangString title;
 
 	@OneToOne( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	private LangString purpose;
 
 	@OneToOne( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	private LangString subtitle;
 
 	// relations
 
 	@ManyToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	private Set<PeriodOfTime> temporal;
 
 	@ManyToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	private Set<Concept> subject;
 
 	@ManyToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	private Set<Location> spacial;
 
 	@ManyToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	private Set<Document> ddiFile;
 
 	@ManyToOne( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	@JoinColumn( name="concept_id" )
 	private Concept kindOfData;
 
 	@ManyToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	protected Set<AnalysisUnit> analysisUnit;
 
 	@ManyToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	protected Set<Universe> universe;
 
 	@ManyToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	protected Set<Agent> publisher;
 
 	@ManyToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	protected Set<Agent> contributer;
 
 	@ManyToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	protected Set<Agent> creator;
 
 	@ManyToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	protected Set<Agent> fundedBy;
 
 	// getter/setter
 
 	public LangString getAbstract()
 	{
 		return abstract_;
 	}
 
 	public void setAbstract( final LangString dcterms_abstract )
 	{
 		abstract_ = dcterms_abstract;
 	}
 
 	public LangString getAlternative()
 	{
 		return alternative;
 	}
 
 	public void setAlternative( final LangString dcterms_alternative )
 	{
 		alternative = dcterms_alternative;
 	}
 
 	public Date getAvailable()
 	{
 		return available;
 	}
 
 	public void setAvailable( final Date dcterms_available )
 	{
 		available = dcterms_available;
 	}
 
 	public LangString getTitle()
 	{
 		return title;
 	}
 
 	public void setTitle( final LangString dcterms_title )
 	{
 		title = dcterms_title;
 	}
 
 	public LangString getPurpose()
 	{
 		return purpose;
 	}
 
 	public void setPurpose( final LangString purpose )
 	{
 		this.purpose = purpose;
 	}
 
 	public LangString getSubtitle()
 	{
 		return subtitle;
 	}
 
 	public void setSubtitle( final LangString subtitle )
 	{
 		this.subtitle = subtitle;
 	}
 
 	public Set<PeriodOfTime> getTemporal()
 	{
 		return temporal;
 	}
 
 	public void setTemporal( final Set<PeriodOfTime> dcterms_temporal )
 	{
 		temporal = dcterms_temporal;
 	}
 
 	public Union_StudyGroupStudy addTemporal( final PeriodOfTime periodOfTime )
 	{
 		if ( temporal == null )
 			temporal = new HashSet<PeriodOfTime>();
 
 		temporal.add( periodOfTime );
 
 		return this;
 	}
 
 	public Set<Concept> getSubject()
 	{
 		return subject;
 	}
 
 	public void setSubject( final Set<Concept> dcterms_subject )
 	{
 		subject = dcterms_subject;
 	}
 
 	public Union_StudyGroupStudy addSubject( final Concept concept )
 	{
 		if ( subject == null )
 			subject = new HashSet<Concept>();
 
 		subject.add( concept );
 
 		return this;
 	}
 
 	public Set<Location> getSpacial()
 	{
 		return spacial;
 	}
 
 	public void setSpacial( final Set<Location> dcterms_spacial )
 	{
 		spacial = dcterms_spacial;
 	}
 
 	public Union_StudyGroupStudy addSpatial( final Location location )
 	{
 		if ( spacial == null )
 			spacial = new HashSet<Location>();
 
 		spacial.add( location );
 
 		return this;
 	}
 
 	public Set<Document> getDdiFile()
 	{
 		return ddiFile;
 	}
 
 	public void setDdiFile( final Set<Document> ddiFile )
 	{
 		this.ddiFile = ddiFile;
 	}
 
 	public Union_StudyGroupStudy addDdiFile( final Document document )
 	{
 		if ( ddiFile == null )
 			ddiFile = new HashSet<Document>();
 
 		ddiFile.add( document );
 
 		return this;
 	}
 
 	public Concept getKindOfData()
 	{
 		return kindOfData;
 	}
 
 	public void setKindOfData( final Concept kindOfData )
 	{
 		this.kindOfData = kindOfData;
 	}
 
 	public Set<Universe> getUniverse()
 	{
 		return universe;
 	}
 
 	public void setUniverse( final Set<Universe> universe )
 	{
 		this.universe = universe;
 	}
 
 	public Union_StudyGroupStudy addUniverse( final Universe universe )
 	{
 		if ( this.universe == null )
 			this.universe = new HashSet<Universe>();
 
 		this.universe.add( universe );
 
 		return this;
 	}
 
 	public Set<AnalysisUnit> getAnalysisUnit()
 	{
 		return analysisUnit;
 	}
 
 	public void setAnalysisUnit( final Set<AnalysisUnit> analysisUnit )
 	{
 		this.analysisUnit = analysisUnit;
 	}
 
 	public Union_StudyGroupStudy addAnalysisUnit( final AnalysisUnit analysisUnit )
 	{
 		if ( this.analysisUnit == null )
 			this.analysisUnit = new HashSet<AnalysisUnit>();
 
 		this.analysisUnit.add( analysisUnit );
 
 		return this;
 	}
 
 	public Set<Agent> getPublisher()
 	{
 		return publisher;
 	}
 
 	public void setPublisher( final Set<Agent> publisher )
 	{
 		this.publisher = publisher;
 	}
 
 	public Union_StudyGroupStudy addPublisher( final Agent agent )
 	{
 		if ( publisher == null )
 			publisher = new HashSet<Agent>();
 
 		publisher.add( agent );
 
 		return this;
 	}
 
 	public Set<Agent> getContributer()
 	{
 		return contributer;
 	}
 
 	public void setContributer( final Set<Agent> contributor )
 	{
 		contributer = contributor;
 	}
 
 	public Union_StudyGroupStudy addContributor( final Agent agent )
 	{
 		if ( contributer == null )
 			contributer = new HashSet<Agent>();
 
 		contributer.add( agent );
 
 		return this;
 	}
 
 	public Set<Agent> getCreator()
 	{
 		return creator;
 	}
 
 	public void setCreator( final Set<Agent> creator )
 	{
 		this.creator = creator;
 	}
 
 	public Union_StudyGroupStudy addCreator( final Agent agent )
 	{
 		if ( creator == null )
 			creator = new HashSet<Agent>();
 
 		creator.add( agent );
 
 		return this;
 	}
 
 	public Set<Agent> getFundedBy()
 	{
 		return fundedBy;
 	}
 
 	public void setFundedBy( final Set<Agent> fundedBy )
 	{
 		this.fundedBy = fundedBy;
 	}
 
 	public Union_StudyGroupStudy addFundedBy( final Agent agent )
 	{
 		if ( fundedBy == null )
 			fundedBy = new HashSet<Agent>();
 
 		fundedBy.add( agent );
 
 		return this;
 	}
 
 }
