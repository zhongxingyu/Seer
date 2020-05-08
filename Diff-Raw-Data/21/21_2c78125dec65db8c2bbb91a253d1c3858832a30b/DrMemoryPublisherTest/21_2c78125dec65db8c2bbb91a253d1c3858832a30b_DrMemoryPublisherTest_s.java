 package net.praqma.jenkins.plugin.drmemory;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.concurrent.ExecutionException;
 
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.BuildListener;
 import hudson.model.FreeStyleBuild;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.Cause;
 import hudson.model.FreeStyleProject;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.Builder;
 import hudson.tasks.Notifier;
 import hudson.tasks.Publisher;
 import hudson.tasks.junit.TestDataPublisher;
 
 import net.praqma.drmemory.DrMemory;
 
 import org.junit.Test;
 import org.jvnet.hudson.test.HudsonTestCase;
 import org.jvnet.hudson.test.TestBuilder;
 
 public class DrMemoryPublisherTest extends HudsonTestCase {
 	
 	@Test
 	public void test() throws IOException, InterruptedException, ExecutionException {
 		
 		FreeStyleProject project = createFreeStyleProject( "drmemory" );

 		DrMemory.skipRun();
 		final InputStream in = getClass().getClassLoader().getResourceAsStream( "results.txt" );
 		System.out.println( "[TEST] " + in );
 		
 		/* To get the real workspace and copy the results.txt */
 		project.getBuildersList().add(new TestBuilder() {
 		    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
 		        BuildListener listener) throws InterruptedException, IOException {
 		        build.getWorkspace().child("drmemory").child( "1" ).child( "results.txt" ).copyFrom( in );
 		        return true;
 		    }
 		});
 		
 		/* DrMemoryBuilder*/
 		Builder builder = new DrMemoryBuilder( "", "", "drmemory" );
 		project.getBuildersList().add( builder );
 		
 		/* Recorder */
 		DrMemoryPublisher publisher = new DrMemoryPublisher();
 		project.getPublishersList().add( publisher );
 		
 		//project.getPublishersList().add( new RemoveFileNotifier( "drmemory/1/results.txt" ) );
 		//project.getPublishersList().add( new RemoveFileNotifier( "results.txt", Location.BUILD ) );
 
 		
 		
 		FreeStyleBuild b = project.scheduleBuild2( 0, new Cause.UserIdCause() ).get();
 		
 		System.out.println( "Workspace: " + b.getWorkspace() );
 		
 		DrMemoryBuildAction action = b.getAction( DrMemoryBuildAction.class );
 		
 		System.out.println( "Action: " + action );
 		System.out.println( "Logfile: " + b.getLogFile() );
 		
 		BufferedReader br = new BufferedReader( new FileReader( b.getLogFile() ) );
 		String line = "";
 		while( ( line = br.readLine() ) != null ) {
 			System.out.println( "[JENKINS] " + line );
 		}
 		
 		if( action != null ) {
 			System.out.println( "Action: " + action.getResult() );
 		} else {
 			System.out.println( "ACTION IS NULL" );
 		}
 		
 		assertNotNull( action );
 	}
 }
