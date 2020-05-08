 package eionet.eunis.dto;
 
 import java.io.Serializable;
 
 import org.simpleframework.xml.Attribute;
 import org.simpleframework.xml.Root;
 
 @Root
 public class SpeciesSynonymDto implements Serializable {
 
 	/**
 	 * serial.
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private Integer speciesId;
 
 	public SpeciesSynonymDto() {
 		//blank
 	}
 	
 	public SpeciesSynonymDto(Integer idSpeciesLink) {
 		this.speciesId = idSpeciesLink;
 	}
 
 	@Attribute(required = false, name = "rdf:resource")
 	public String getSpeciesId() {
		return "http://eunis.eea.europa.eu/species/" + speciesId; 
 	}
 	
 	
 }
