 package net.praqma.hudson;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.praqma.clearcase.ucm.UCMException;
 import net.praqma.clearcase.ucm.entities.Project;
 import net.praqma.clearcase.ucm.entities.Stream;
 import net.praqma.clearcase.ucm.entities.UCM;
 import net.praqma.clearcase.ucm.entities.UCMEntity;
 import net.praqma.hudson.exception.ScmException;
 import net.praqma.util.Debug;
 
 public class Config
 {
 	protected static Debug logger = Debug.GetLogger();
 
 	private Config()
 	{
 	}
 
 	public static List<String> getLevels()
 	{
 		logger.trace_function();
 		List<String> levels = new ArrayList<String>();
 		levels.add( "INITIAL" );
 		levels.add( "BUILT" );
 		levels.add( "TESTED" );
 		levels.add( "RELEASED" );
 		levels.add( "REJECTED" );
 		return levels;
 	}
 
 	public static void setContext()
 	{
 		boolean useTestbase = false;
 		if ( useTestbase )
 		{
 			/*
 			 * Examples to use from testbase.xml: stream =
 			 * "STREAM_TEST1@\PDS_PVOB" component = "COMPONENT_TEST1@\PDS_PVOB"
 			 * Level to poll = "INITIAL"
 			 */
 			UCM.SetContext( UCM.ContextType.XML );
 			System.out.println( "PUCM is running on a testbase" );
 		}
 		else
 		{
 			UCM.SetContext( UCM.ContextType.CLEARTOOL );
 		}
 	}
 
 	public static Stream devStream( String pvob ) throws ScmException
 	{
 		Stream devStream = null;
		// debugrev - remove nexxt
		System.out.println("linie 57 - pvob "+pvob);
 		try
 		{
			//devStream = UCMEntity.GetStream( "Hudson_Server_dev@" + pvob, false );
			devStream = UCMEntity.GetStream( "HardcodedStreamInConfigJava@" + pvob, false );
			// debugrev - remove nexxt
			System.out.println("linie 62 - devstream "+devStream);
 		}
 		catch ( UCMException e )
 		{
 			throw new ScmException( "Could not get developer stream. " + e.getMessage() );
 		}
		// debugrev - remove nexxt
		System.out.println("linie 67 - pvob "+pvob);
 		return devStream;
 	}
 
 	public static Stream getIntegrationStream( String pvob ) throws ScmException
 	{
 		Stream stream = null;
 		Project project = null;
 		try
 		{
 			project = UCMEntity.GetProject( "hudson" + pvob, false );
 
 		}
 		catch ( Exception e )
 		{
 			throw new ScmException( "Could not find project 'hudson' in " + pvob + " - please check and retry" );
 		}
 		try
 		{
 			stream = project.GetStream();
 		}
 		catch ( Exception e )
 		{
 			throw new ScmException( "Could not get integration stream from " + project.GetShortname() );
 		}
 
 		return stream;
 	}
 
 	public static String getPvob( Stream stream )
 	{
 
 		return "@" + stream.GetPvob();
 	}
 
 }
