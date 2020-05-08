 package dk.dbc.opensearch.common.config.tests;
 
 
 import dk.dbc.opensearch.common.config.FileSystemConfig;
 
 import org.apache.log4j.Logger;
 import org.junit.Test;
 import static org.junit.Assert.assertTrue;
 
 
 
 public class FileSystemConfigTest
 {
     Logger logger = Logger.getLogger( FileSystemConfigTest.class );
 
 
     @Test
     public void testGetTrunkPath() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException
     {
         String trunk = FileSystemConfig.getFileSystemTrunkPath();
        System.out.println( "trunk: " + trunk );
         boolean endsWith = trunk.endsWith( "opensearch/trunk/" ); 
         assertTrue( endsWith );
     }
     
     
     @Test
     public void testGetPluginsPath()
     {
     	String plugins = FileSystemConfig.getFileSystemPluginsPath();
    	System.out.println( "plugins: " + plugins );
         boolean endsWith = plugins.endsWith( "opensearch/trunk/build/classes/dk/dbc/opensearch/plugins/" ); 
         assertTrue( endsWith );    	
     }
 }
