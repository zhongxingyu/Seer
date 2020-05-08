 package net.praqma.jenkins.plugin.drmemory;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import net.praqma.drmemory.DrMemoryResult;
 import net.praqma.drmemory.exceptions.InvalidInputException;
 import net.praqma.jenkins.plugin.drmemory.graphs.AbstractGraph;
 import net.praqma.jenkins.plugin.drmemory.graphs.ActualLeaksGraph;
 import net.praqma.jenkins.plugin.drmemory.graphs.AllLeaksGraph;
 import net.praqma.jenkins.plugin.drmemory.graphs.BytesOfLeakGraph;
 import net.praqma.jenkins.plugin.drmemory.graphs.InvalidHeapArgumentsGraph;
 import net.praqma.jenkins.plugin.drmemory.graphs.StillReachableAllocationsGraph;
 import net.praqma.jenkins.plugin.drmemory.graphs.TotalLeaksGraph;
 import net.praqma.jenkins.plugin.drmemory.graphs.UnaddressableAccessesGraph;
 import net.praqma.jenkins.plugin.drmemory.graphs.UninitializedAccessesGraph;
 import net.praqma.jenkins.plugin.drmemory.graphs.WarningsGraph;
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.Util;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.Action;
 import hudson.model.BuildListener;
 import hudson.model.Result;
 import hudson.model.Descriptor;
 import hudson.model.Descriptor.FormException;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.BuildStepMonitor;
 import hudson.tasks.Publisher;
 import hudson.tasks.Recorder;
 
 public class DrMemoryPublisher extends Recorder {
 
 	private static final Logger logger = Logger.getLogger( DrMemoryPublisher.class.getName() );
 
 	public static final String __OUTPUT = "drmemory.txt";
 	
 	public static Map<String, AbstractGraph> graphTypes = new HashMap<String, AbstractGraph>();
 	
 	static {
 		graphTypes.put( "total-leaks", new TotalLeaksGraph() );
 		graphTypes.put( "all-leaks", new AllLeaksGraph() );
 		graphTypes.put( "actual-leaks", new ActualLeaksGraph() );
 		graphTypes.put( "bytes-of-leak", new BytesOfLeakGraph() );
 		graphTypes.put( "allocations", new StillReachableAllocationsGraph() );
 		graphTypes.put( "uninitialized-accesses", new UninitializedAccessesGraph() );
 		graphTypes.put( "unaddressable-accesses", new UnaddressableAccessesGraph() );
 		graphTypes.put( "warnings", new WarningsGraph() );
 		graphTypes.put( "invalid-heap-arguments", new InvalidHeapArgumentsGraph() );
 	}
 
 	@DataBoundConstructor
 	public DrMemoryPublisher() {
 	}
 
 	@Override
 	public BuildStepMonitor getRequiredMonitorService() {
 		return BuildStepMonitor.BUILD;
 	}
 
 	@Override
 	public boolean perform( AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener ) throws InterruptedException, IOException {
 		
 		/*
 		if( build.getResult().isWorseOrEqualTo( Result.FAILURE ) ) {
 			return true;
 		}
 		*/
 		
 		FilePath workspaceResult = null;
 		File path = build.getRootDir();
 		File result = new File( path, __OUTPUT );
 		FilePath buildTarget = new FilePath( build.getRootDir() );
 		PrintStream out = listener.getLogger();
 		
 		DrMemoryBuildAction action = build.getAction( DrMemoryBuildAction.class );
 
 		out.println( "My workspace is " + build.getWorkspace() );
		out.println( "My workspace is " + action.getBuilder().getFinalLogPath() );
 		FilePath resultPath = new FilePath( build.getWorkspace(), action.getBuilder().getFinalLogPath() );
 		
 		FilePath[] rr = resultPath.list( "**/results.txt" );
 		
 		if( rr.length < 1 ) {
 			out.println( "No results to parse" );
 			return true;
 		}
 
 		workspaceResult = rr[0];
 
 		out.println( "I got " + workspaceResult );
 
 		if( workspaceResult != null ) {
 
 			/* Save output to build path */
 			final FilePath targetPath = new FilePath( result );
 			try {
 				workspaceResult.copyTo( targetPath );
 			} catch( IOException e ) {
 				Util.displayIOException( e, listener );
 				e.printStackTrace( listener.fatalError( "Unable to copy result file from " + workspaceResult + " to " + buildTarget ) );
 				build.setResult( Result.FAILURE );
 			}
 		}
 
 		DrMemoryResult dresult = null;
 		try {
 			dresult = DrMemoryResult.parse( result );
 
 			action.setPublisher( this );
 			action.setResult( dresult );
 
 		} catch( InvalidInputException e ) {
 			out.println( "Invalid input: " + e.getMessage() );
 			return false;
 		}
 
 		return true;
 	}
 		
 	private List<Graph> graphs;
 	
 	
     @Override
     public Action getProjectAction(AbstractProject<?, ?> project) {
         return new DrMemoryProjectAction( project );
     }
     
     public Map<String, AbstractGraph> getGraphTypes() {
     	return graphTypes;
     }
     
     public void setGraphs( List<Graph> graphs ) {
     	this.graphs = graphs;
     }
     
 	public AbstractGraph getGraph( String type ) {
 		return graphTypes.get( type );
 	}
     
     public List<Graph> getGraphs() {
     	return graphs;
     }
 
 	@Extension
 	public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {
 		public String getDisplayName() {
 			return "Dr. Memory Report";
 		}
 
 		@Override
 		public boolean isApplicable( Class<? extends AbstractProject> arg0 ) {
 			return true;
 		}
 
 		@Override
 		public DrMemoryPublisher newInstance( StaplerRequest req, JSONObject formData ) throws FormException {
 			DrMemoryPublisher instance = req.bindJSON( DrMemoryPublisher.class, formData );
 			
 			/* total leaks */
 			/*
 			boolean total_leaks = req.getParameter( "graph.total-leaks" ) != null;
 			instance.selectedGraphs.put( "total-leaks", total_leaks );
 			*/
 			
 			System.out.println( formData.toString( 2 ) );
 			
 			List<Graph> graphs = req.bindParametersToList(Graph.class, "drmemory.graph.");
 			instance.setGraphs( graphs );
 			save();
 			return instance;
 		}
 		
 		public List<Graph> getGraphs( DrMemoryPublisher instance ) {
 			if( instance == null ) {
 				return new ArrayList<Graph>();
 			} else {
 				return instance.getGraphs();
 			}
 		}
 		
 		public AbstractGraph getGraph( String type ) {
 			return graphTypes.get( type );
 		}
 		
 		public Set<String> getGraphTypes() {
 			return graphTypes.keySet();
 		}
 		
 		/*
 		public List<String> getGraphTypes() {
 			Set<String> keys = graphTypes.keySet();
 			List<>
 			for( String key : keys ) {
 				
 			}
 		}
 		*/
 
 		/*GLOBAL*/
 		@Override
 		public boolean configure( StaplerRequest req, JSONObject formData ) throws FormException {
 			logger.warning( "CONFIGURE" );
 			req.bindParameters( this, "drmemory." );
 			boolean total_leak = req.getParameter( "graph.total-leak" ) != null;
 			logger.warning( "tll: " + total_leak );
 			//DrMemoryPublisher.this.selectedGraphs.put( "total-leak", total_leak );
 			save();
 			return super.configure( req, formData );
 		}
 
 	}
 }
