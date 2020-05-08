 package com.redshape.utils.config;
 
 import com.redshape.utils.SimpleStringUtils;
 import com.redshape.utils.config.sources.IConfigSource;
 
 /**
  * @author Cyril A. Karpenko <self@nikelin.ru>
  * @package com.redshape.utils.config
  * @date 10/20/11 1:05 PM
  */
 public class PropertiesConfig extends AbstractConfig {
 	public static final String[] STANDARD_LINE_DELIMITERS = new String[] { "\n", "\r", ";" };
 
     public PropertiesConfig() {
         this(null, null, null);
     }
 
     protected PropertiesConfig(PropertiesConfig parent, String name, String value) {
         super(parent, name, value);
     }
 
     protected PropertiesConfig(String name, String value) {
         this(null, name, value);
     }
 
     public PropertiesConfig(IConfigSource source) throws ConfigException {
 		super(source);
     }
 
     @Override
     protected void actualInit() throws ConfigException {
         String data = this.source.read();
         if ( data.isEmpty() ) {
             return;
         }
 
         this.processData( data );
     }
 
 	@Override
 	protected IConfig createNull() {
 		PropertiesConfig config = new PropertiesConfig(this, null, null);
 		config.nulled = true;
 		return config;
 	}
 
 	protected String[] getLineDelimiters() {
 		return STANDARD_LINE_DELIMITERS;
 	}
 
 	protected void processData( String data ) throws ConfigException {
 		String[] lines = data.split( SimpleStringUtils.join(this.getLineDelimiters(), "") );
 		for ( String line : lines ) {
 			String[] parts = line.split("=");
 			if ( parts.length != 2 ) {
 				throw new ConfigException("Config file is corrupted");
 			}
 
 			this.append( new PropertiesConfig(this, parts[0], parts[1] ) );
 		}
 	}
 
 	@Override
 	public String serialize() throws ConfigException {
 		return null;  //To change body of implemented methods use File | Settings | File Templates.
 	}
 
 	@Override
 	public IConfig createChild(String name) {
 		return new PropertiesConfig(this, name, null);
 	}
 
    public static IConfig fromSource( IConfig config ) throws ConfigException {
        IConfig result = new PropertiesConfig( config.name(), config.value() );
         for ( IConfig child : config.childs() ) {
             result.append( fromSource(child) );
         }
 
         for ( String name : config.attributeNames() ) {
             result.attribute(name, config.attribute(name) );
         }
 
         return result;
     }
 }
