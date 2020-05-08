 package cz.zcu.kiv.bp.namespaces;
 
 /**
  * provides prefix and uri for UniMocker
  * @author Michal
  */
 public class UniMocker
 {
 	public static final String SCENARIO_PREFIX = "um";
 	public static final String SCENARIO_URI = "http://www.kiv.zcu.cz/component-testing/mocker";
	public static final String SCENARIO_SCHEMA = "https://raw.github.com/michalriha/kivComponentTesting/custom_code_injection/UniMockerBindings/schema/unimocker.xsd";
 	public static final String SCENARIO_SCHEMA_LOCATION = SCENARIO_URI + " " + SCENARIO_SCHEMA;
 	
 	private UniMocker() { }
 }
