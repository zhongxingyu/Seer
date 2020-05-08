 package org.gesis.skos;
 
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 import javax.persistence.Inheritance;
 import javax.persistence.InheritanceType;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 
import org.gesis.ddi.ontology.Union_RdfsDataTypeSkosConceptScheme;
 
 @Entity
 @Inheritance( strategy = InheritanceType.JOINED )
public class ConceptScheme extends Union_RdfsDataTypeSkosConceptScheme
 {
 
 	@ElementCollection
 	@ManyToMany
 	@JoinTable(
 			name="ConceptScheme_Concept",
 			joinColumns=@JoinColumn(name="conceptScheme_id"),
 			inverseJoinColumns=@JoinColumn(name="concept_id")
 	)
 	protected Set<Concept> skos_hasTopConcept;
 
 	// getter/setter
 
 	public ConceptScheme( final String agencyId, final String objectId, final int majorVersion )
 	{
 		super( agencyId, objectId, majorVersion );
 	}
 
 	public Set<Concept> getSkos_hasTopConcept()
 	{
 		return this.skos_hasTopConcept;
 	}
 
 	public void setSkos_hasTopConcept( final Set<Concept> skos_hasTopConcept )
 	{
 		this.skos_hasTopConcept = skos_hasTopConcept;
 	}
 
 	public ConceptScheme addSkos_hasTopConcept( final Concept concept )
 	{
 		if ( this.skos_hasTopConcept == null )
 			this.skos_hasTopConcept = new LinkedHashSet<Concept>();
 
 		this.skos_hasTopConcept.add( concept );
 
 		return this;
 	}
 
 }
