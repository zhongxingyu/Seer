 package browser;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import confdb.converter.ConverterBase;
 import confdb.converter.ConverterEngine;
 import confdb.converter.ConverterException;
 import confdb.converter.ConverterFactory;
 import confdb.converter.DbProperties;
 import confdb.converter.OfflineConverter;
 import confdb.converter.IConfigurationWriter.WriteProcess;
 import confdb.data.ConfigurationModifier;
 import confdb.data.IConfiguration;
 import confdb.data.ModifierInstructions;
 import confdb.db.ConfDB;
 import confdb.db.ConfDBSetups;
 
 public class BrowserConverter extends OfflineConverter
 {
     static private HashMap<Integer,BrowserConverter> map = new HashMap<Integer,BrowserConverter>();
     
     private BrowserConverter(String dbType,String dbUrl,
 			     String dbUser,String dbPwrd)
 	throws ConverterException
     {
 	super( "HTML", dbType, dbUrl, dbUser, dbPwrd );	
     }
     
     public String getConfigString(int configId,
 				  String format,
 				  ModifierInstructions modifications,
 				  boolean asFragment)
 	throws ConverterException,
 	       ClassNotFoundException,
 	       InstantiationException,
 	       IllegalAccessException
     {
     	IConfiguration config = getConfiguration(configId);
     	ConfigurationModifier modifier = new ConfigurationModifier(config);
     	modifier.modify(modifications);
 	
     	ConverterEngine engine = ConverterFactory.getConverterEngine( format );
     	if (asFragment)
 	    return engine.getConfigurationWriter().toString(modifier,
 							    WriteProcess.NO);
     	else
 	    return engine.getConfigurationWriter().toString(modifier,
 							    WriteProcess.YES);
     }
 
 	
 	protected void finalize() throws Throwable
 	{
 		super.finalize();
 		ConfDB db = getDatabase();
 		if ( db != null )
 			db.disconnect();
 	}
 	
 	static public BrowserConverter getConverter( int dbIndex ) throws ConverterException 
 	{
	    ConfDBSetups dbs = new ConfDBSetups();
 	    DbProperties dbProperties = new DbProperties( dbs, dbIndex, "convertme!" );
 	   	dbProperties.setDbUser( "cms_hlt_reader" );
 		BrowserConverter converter = map.get( new Integer( dbIndex ) );
 		if ( converter == null )
 		{
 			converter = new BrowserConverter( dbs.type( dbIndex ), dbProperties.getDbURL(), dbProperties.getDbUser(), "convertme!" );		
 			map.put( new Integer( dbIndex ), converter );
 		}
 		return converter;
 	}
 
 	static public void deleteConverter( ConverterBase converter )
 	{
 		Set<Map.Entry<Integer,BrowserConverter>> entries = map.entrySet();
 		for ( Map.Entry<Integer,BrowserConverter> entry : entries )
 		{
 			if ( entry.getValue() == converter )
 			{
 				map.remove( entry.getKey() );
 				return;
 			}
 		}
 	}
 	
 	
 }
