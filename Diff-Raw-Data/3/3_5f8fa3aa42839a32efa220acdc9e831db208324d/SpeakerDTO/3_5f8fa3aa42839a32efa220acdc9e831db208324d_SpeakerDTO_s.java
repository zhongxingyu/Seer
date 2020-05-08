 package albin.oredev.year2012.server.model;
 
 import org.simpleframework.xml.Attribute;
 import org.simpleframework.xml.Element;
 import org.simpleframework.xml.Root;
 
 import albin.oredev.year2012.model.Speaker;
 
 @Root(strict = false, name="speaker")
 public class SpeakerDTO {
 
 	@Attribute
 	private String id;
 
 	@Attribute
 	private String name;
 
 	@Attribute
 	private String photoFile;
 	
 	@Element
 	private String biography;
 
 	public Speaker toSpeaker() {
 		return new Speaker(id, name, photoFile, biography);
 	}
 }
