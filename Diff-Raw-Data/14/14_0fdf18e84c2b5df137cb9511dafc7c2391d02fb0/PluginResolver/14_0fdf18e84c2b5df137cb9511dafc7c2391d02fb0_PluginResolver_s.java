 package dk.dbc.opensearch.common.pluginframework;
 
 
 import dk.dbc.opensearch.common.config.FileSystemConfig;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.lang.IllegalAccessException;
 import java.lang.InstantiationException;
 import java.util.ArrayList;
 import java.util.Vector;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.log4j.Logger;
 
 
 /**
  *
  */
 public class PluginResolver implements IPluginResolver
 {
     static Logger log = Logger.getLogger( PluginResolver.class );
 
     static String path;
     static DocumentBuilderFactory docBuilderFactory;
     static DocumentBuilder docBuilder;
     static ClassLoader pluginClassLoader;
     static PluginFinder PFinder;
     static PluginLoader PLoader;
     static boolean constructed = false;
     
 
     /**
      * @throws IOException 
      * @throws NullPointerException
      * @throws PluginResolverException if the PluginFinder has trouble while reading the .plugin files
      * @throws ParserConfigurationException from PluginFinder if it cant parse the .plugin files
      * @throws FileNotFoundException when the PluginFinder cant find the .plugin files
      */
     public PluginResolver() throws NullPointerException, PluginResolverException, ParserConfigurationException, FileNotFoundException
     {      
         if( ! constructed )
         {
             docBuilderFactory = DocumentBuilderFactory.newInstance();
             docBuilder = docBuilderFactory.newDocumentBuilder();
             
             pluginClassLoader = new PluginClassLoader();
             PLoader = new PluginLoader( pluginClassLoader );
             path = FileSystemConfig.getFileSystemPluginsPath();
            //            path = FileSystemConfig.getFileSystemTrunkPath() + "src/dk/dbc/opensearch/plugins";
             
             PFinder = new PluginFinder( docBuilder, path );
             
             constructed = true;            
         }
     }
 
     
     /**
      * @param submitter, the submitter of the data the plugin works on
      * @param format, the format of the data the plugin works on
      * @param the task to be solved
      * @returns a plugin matching the key made out of the params  
      * @throws InstantitionException if the PluginLoader cant load the desired plugin
      * @throws FileNotFoundException if the desired plugin file cannot be found
      * @throws IllegalAccessException if the plugin file cant be accessed by the PluginLoader
      * @throws ClassNotFoundException if the class of the plugin cannot be found
      * @throws PluginResolverException if key doesnot give a value from the PluginFinder
      */
     public IPluggable getPlugin( String submitter, String format, String task ) throws FileNotFoundException, InstantiationException, IllegalAccessException, ClassNotFoundException, PluginResolverException
     {  
     	//int key = ( submitter + format + task ).hashCode();
     	int key = task.hashCode();
     	String pluginClassName = PFinder.getPluginClassName( key );
         
         return PLoader.getPlugin( pluginClassName );
     }
 
     
     /**
      * @param submitter, the submitter to solve tasks for
      * @param format, the format of the data to perform tasks on
      * @param taskList, the tasks to be validated that plugins exists to perform
      * @returns a Vector of tasks there are no plugins to perform og the specified format from the submitter. If it is empty execution can continue in the calling thread
      * @throws PluginResolverException when there are parser and reader errors from the PluginFinder
      */
     public Vector<String> validateArgs( String submitter, String format, ArrayList< String > taskList ) throws PluginResolverException
     {
         Vector< String > pluginNotFoundVector = new Vector< String >();
 
         // Loop through list of tasks finding tasks without matching plugin.
         for( int i = 0; i < taskList.size(); i++ )
         {
             //String hashSubject = submitter + format + taskList.get( i ).toString();
         	//System.out.println( "plugin: " + taskList.get( i ).toString() );
         	String hashSubject = taskList.get( i ).toString();
             int key = hashSubject.hashCode();
             
             try
             {
             	PFinder.getPluginClassName( key );
             }
             catch( FileNotFoundException fnfe )
             {
             	// Add "missing" plugins to return Vector
             	pluginNotFoundVector.add( taskList.get( i ) );
          	}
         }
         
         return pluginNotFoundVector;
     }
 
 
     public void clearPluginRegistration()
     {
         //clear the classNameMap in PluginFinder
         PFinder.clearClassNameMap();
     }
 }
