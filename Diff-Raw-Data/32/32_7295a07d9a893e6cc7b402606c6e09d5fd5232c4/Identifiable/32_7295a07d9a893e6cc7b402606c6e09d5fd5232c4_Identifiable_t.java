 package org.gesis.ddi;
 
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.Version;
 
 import org.gesis.ddi.util.URNBuilder;
import org.gesis.rdfs.Resource;
 
 @Entity
 @Inheritance( strategy = InheritanceType.JOINED )
public class Identifiable extends Resource
 {
 
 	// properties
 
 	@Column
 	private String URN;
 
 	@Column
 	private String agencyId;
 
 	@Column
 	private String objectId;
 
 	@Column
 	private int majorVersion;
 
 	@Column( updatable = false )
 	@Version
 	private int minorVersion;
 
 	// relations
 
 	@ManyToMany( cascade = CascadeType.ALL, fetch = FetchType.LAZY )
 	@JoinTable( 
 			name = "Identifiable_Note", 
 			joinColumns = @JoinColumn( name = "identifiable_id" ), 
 			inverseJoinColumns = @JoinColumn( name = "note_id" ) )
	private Set<Identifiable> identifiables;
 
 	// getter/setter
 
 	public Identifiable()
 	{
 
 	}
 
 	public Identifiable( final String agencyId, final String objectId, final int majorVersion )
 	{
 		super();
 		this.agencyId = agencyId;
 		this.objectId = objectId;
 		this.majorVersion = majorVersion;
 
 		this.URN = URNBuilder.buildFrom( this );
 	}
 
 	public String getURN()
 	{
 		return this.URN;
 	}
 
 	public void setURN( final String uRN )
 	{
 		this.URN = uRN;
 	}
 
 	public String getAgencyId()
 	{
 		return this.agencyId;
 	}
 
 	public Identifiable setAgencyId( final String agencyId )
 	{
 		this.agencyId = agencyId;
 
 		return this;
 	}
 
 	public String getObjectId()
 	{
 		return this.objectId;
 	}
 
 	public Identifiable setObjectId( final String objectId )
 	{
 		this.objectId = objectId;
 
 		return this;
 	}
 
 	public int getMajorVersion()
 	{
 		return this.majorVersion;
 	}
 
 	public Identifiable setMajorVersion( final int majorVersion )
 	{
 		this.majorVersion = majorVersion;
 
 		return this;
 	}
 
 	public int getMinorVersion()
 	{
 		return this.minorVersion;
 	}
 
 	public void setMinorVersion( final int minorVersion )
 	{
 		this.minorVersion = minorVersion;
 	}
 
	public Identifiable addIdentifiable( final Identifiable identifiable )
 	{
		if ( this.identifiables == null )
			this.identifiables = new LinkedHashSet<Identifiable>();
 
		this.identifiables.add( identifiable );
 
 		return this;
 	}
 
	public Set<Identifiable> getIdentifiables()
 	{
		return this.identifiables;
 	}
 
	public void setIdentifiables( final Set<Identifiable> identifiables )
 	{
		this.identifiables = identifiables;
 	}
 
 }
