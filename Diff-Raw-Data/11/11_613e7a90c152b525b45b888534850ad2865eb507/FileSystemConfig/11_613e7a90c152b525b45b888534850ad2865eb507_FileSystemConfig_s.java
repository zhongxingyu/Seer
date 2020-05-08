 /**
  * 
  */
 package dk.dbc.opensearch.common.config;
 
 
 /**
  * @author mro
  *
  */
 public class FileSystemConfig extends Config
 {
 	private String sanitize( String path )
 	{
 		if( path.endsWith( "/" ) )
 			return path;
 		else
 			return path + "/";
 	}
 	
 	
 	/* ******************
 	 * FILESYSTEM TRUNK *
 	 * ******************/
 	private String getTrunkPath()
 	{
 		String ret = config.getString( "filesystem.trunk" );
 		return sanitize( ret );
 	}
 	
 	
 	public static String getFileSystemTrunkPath() 
 	{
 		FileSystemConfig f = new FileSystemConfig();
 		return f.getTrunkPath();
 	} 
 	
 	
 	/* ********************
 	 * FILESYSTEM PLUGINS *
 	 * ********************/
 	private String getPluginsPath()
 	{

 		FileSystemConfig fc = new FileSystemConfig();
 		String ret = config.getString( "filesystem.plugins" );
 		ret = fc.getTrunkPath() + ret;		
 		return sanitize( ret );
 	}
 	
 	
 	public static String getFileSystemPluginsPath()
 	{		
 		FileSystemConfig fc = new FileSystemConfig();		
 		return fc.getPluginsPath();
 	}
 	
 	
 	/* *********************
 	 * FILESYSTEM DATADOCK *
 	 * *********************/
 	private String getDatadock()
 	{
 		FileSystemConfig fc = new FileSystemConfig();
 		String ret = config.getString( "filesystem.datadock" );
 		ret = fc.getTrunkPath() + ret;
 		return ret;
 	}
 	
 	/**
 	 * @return Path to the config/datadock_jobs.xml file
 	 */
 	public static String getFileSystemDatadock() 
 	{
 		FileSystemConfig fc = new FileSystemConfig();
 		return fc.getDatadock();
 	} 
 	
 	
 	/* ****************
 	 * FILESYSTEM PTI *
 	 * ***************/
 	private String getPti()
 	{
 		FileSystemConfig fc = new FileSystemConfig();
 		String ret = config.getString( "filesystem.pti" );
 		ret = fc.getTrunkPath() + ret;
 		return ret;
 	}
 	
 	/**
 	 * @return Path to the config/pti_jobs.xml file
 	 */	
 	public static String getFileSystemPti()
 	{		
 		FileSystemConfig fc = new FileSystemConfig();
 		return fc.getPti();
 	}
 }
