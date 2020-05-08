 package org.gesis.ddi.ontology;
 
 import javax.persistence.MappedSuperclass;
 
import org.gesis.rdfs.Resource;
 
 @MappedSuperclass
public abstract class Union_RdfsDataTypeSkosConceptScheme extends Resource
 {
 
 	public Union_RdfsDataTypeSkosConceptScheme( String agencyId, String objectId, int majorVersion )
 	{
 		super( agencyId, objectId, majorVersion );
 	}
 
 }
