 package cz.zcu.kiv.bp.namespaces;
 
 /**
  * provides prefix and uri for UniPlayer
  * @author Michal
  */
 public class UniPlayer
 {
 	public static final String SCENARIO_PREFIX = "up";
 	public static final String SCENARIO_URI = "http://www.kiv.zcu.cz/component-testing/player";
	public static final String SCENARIO_SCHEMA = "https://raw.github.com/michalriha/kivComponentTesting/master/UniPlayerBindings/schema/uniplayer.xsd";
 	public static final String SCENARIO_SCHEMA_LOCATION = SCENARIO_URI + " " + SCENARIO_SCHEMA;
 	
 	private UniPlayer() { }
 }
