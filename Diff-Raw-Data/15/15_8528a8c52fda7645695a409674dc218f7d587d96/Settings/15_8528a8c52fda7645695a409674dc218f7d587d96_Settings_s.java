 package org.vamdc.portal;
 
 /**
  * Various configuration properties names and their default values.
  * @author doronin
  *
  */
 public enum Settings {
 
 	HTTP_CONNECT_TIMEOUT("HTTPConnTimeout", "2000"),
 	HTTP_DATA_TIMEOUT("HTTPDataTimeout","20000"),
 	STATIC_STORAGE_PATH("fileStoragePath","/tmp"),
 	STATIC_ACCESS_URL("fileStorageURL","http://localhost/"),
 	DISPLAY_URL("displayURL","http://localhost/"),
 	REGISTRY_URL("registryURL","http://casx019-zone1.ast.cam.ac.uk/registry/services/RegistryQueryv1_0"),
 	REGISTRY_UPDATE_INTERVAL("registryUpdateInterval","300000"),
 	HTTP_HEAD_TIMEOUT("HTTPHeadTimeout","30000"),
 	;
 	
 	
 	private final String key;
 	private final String defValue;
 	private String value;
 	
 	Settings(String key, String def){
 		this.key = key;
 		this.defValue=def;
 	}
 	
 	public final String get(){
 		if (value!=null)
 			return value;
 		return (value=System.getProperty(this.key, this.defValue));
 	}
 	
 	public final int getInt(){
 		if (value!=null)
 			return Integer.valueOf(value);
 		return Integer.valueOf(value=System.getProperty(this.key, this.defValue));
 	}
 	
 }
