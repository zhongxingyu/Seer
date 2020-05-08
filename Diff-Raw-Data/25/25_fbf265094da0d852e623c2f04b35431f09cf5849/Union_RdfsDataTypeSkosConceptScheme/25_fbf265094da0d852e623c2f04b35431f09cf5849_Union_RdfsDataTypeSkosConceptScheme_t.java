 package org.gesis.ddi.ontology;
 
 import javax.persistence.MappedSuperclass;
 
import org.gesis.skos.ConceptScheme;
 
 @MappedSuperclass
public abstract class Union_RdfsDataTypeSkosConceptScheme extends ConceptScheme
 {
 
 	public Union_RdfsDataTypeSkosConceptScheme( String agencyId, String objectId, int majorVersion )
 	{
 		super( agencyId, objectId, majorVersion );
 	}
 
 }
