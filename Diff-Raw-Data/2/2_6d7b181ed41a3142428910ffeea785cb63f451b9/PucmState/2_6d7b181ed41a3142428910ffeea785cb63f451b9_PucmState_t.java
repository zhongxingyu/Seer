 package net.praqma.hudson.scm;
 
 import hudson.model.AbstractProject;
 import hudson.model.Build;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.ucm.entities.Component;
 import net.praqma.clearcase.ucm.entities.Project;
 import net.praqma.clearcase.ucm.entities.Stream;
 
 /**
  * 
  * @author wolfgang
  *
  */
 public class PucmState
 {
 	private List<State> states          = new ArrayList<State>();
 	private static final String linesep = System.getProperty( "line.separator" );
 	
 	/**
 	 * Get a state given job name and job number
 	 * @param jobName the hudson job name
 	 * @param jobNumber the hudson job number
 	 * @return
 	 */
 	public State getState( String jobName, Integer jobNumber )
 	{
 		for( State s : states )
 		{
 			if( s.getJobName().equals( jobName ) && s.getJobNumber() == jobNumber )
 			{
 				return s;
 			}
 		}
 		
 		State s = new State( jobName, jobNumber );
 		states.add( s );
 		return s;
 	}
 	
 	public boolean removeState( String jobName, Integer jobNumber )
 	{
 		for( State s : states )
 		{
 			if( s.getJobName().equals( jobName ) && s.getJobNumber() == jobNumber )
 			{
 				states.remove( s );
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	public State getStateByBaseline( String jobName, String baseline )
 	{
 		for( State s : states )
 		{
 			if( s.getJobName().equals( jobName ) && s.getBaseline() != null && s.getBaseline().GetFQName().equals( baseline ) )
 			{
 				return s;
 			}
 		}
 		
 		return null;		
 	}
 	
 	
 	public void addState( State state )
 	{
 		this.states.add( state );
 	}
 	
 	public boolean stateExists( State state )
 	{
 		return stateExists( state.jobName, state.jobNumber );
 	}
 	
 	public boolean stateExists( String jobName, Integer jobNumber )
 	{
 		for( State s : states )
 		{
 			if( s.getJobName().equals( jobName ) && s.getJobNumber() == jobNumber )
 			{
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	public boolean removeState( State state )
 	{
 		return states.remove( state );
 	}
 	
	public synchronized int recalculate( AbstractProject<?, ?> project )
 	{
 		int count = 0;
 		
 		for( State s : states )
 		{
 			Integer bnum = s.getJobNumber();
 			Object o = project.getBuildByNumber( bnum );
 			Build bld = (Build)o;
 			
 			/* The job is not running */
 			if( !bld.isLogUpdated() )
 			{
 				s.remove();
 				count++;
 			}
 		}
 		
 		return count;
 	}
 
 	
 	public int size()
 	{
 		return states.size();
 	}
 	
 	public String stringify()
 	{
 		return net.praqma.util.structure.Printer.listPrinterToString( states );
 	}
 
 
 	public class State
 	{
 		private Baseline  baseline;
 		private Stream    stream;
 		private Component component;
 		private boolean   doPostBuild = true;
 		
 		private Project.Plevel plevel;
 		
 		private String    jobName;
 		private Integer   jobNumber;
 		
 		
 		public State(){}
 		public State( String jobName, Integer jobNumber )
 		{
 			this.jobName   = jobName;
 			this.jobNumber = jobNumber;
 		}
 		public State( String jobName, Integer jobNumber, Baseline baseline, Stream stream, Component component, boolean doPostBuild )
 		{
 			this.jobName     = jobName;
 			this.jobNumber   = jobNumber;
 			this.baseline    = baseline;
 			this.stream      = stream;
 			this.component   = component;
 			this.doPostBuild = doPostBuild;
 		}
 		
 		@Deprecated
 		public void save()
 		{
 			PucmState.this.addState( this );
 		}
 		
 		public boolean remove()
 		{
 			return PucmState.this.removeState( this );
 		}
 		
 		public Baseline getBaseline()
 		{
 			return baseline;
 		}
 		public void setBaseline( Baseline baseline )
 		{
 			this.baseline = baseline;
 		}
 		public Stream getStream()
 		{
 			return stream;
 		}
 		public void setStream( Stream stream )
 		{
 			this.stream = stream;
 		}
 		public Component getComponent()
 		{
 			return component;
 		}
 		public void setComponent( Component component )
 		{
 			this.component = component;
 		}
 		public boolean doPostBuild()
 		{
 			return doPostBuild;
 		}
 		public void setPostBuild( boolean doPostBuild )
 		{
 			this.doPostBuild = doPostBuild;
 		}
 		public String getJobName()
 		{
 			return jobName;
 		}
 		public void setJobName( String jobName )
 		{
 			this.jobName = jobName;
 		}
 		public Integer getJobNumber()
 		{
 			return jobNumber;
 		}
 		public void setJobNumber( Integer jobNumber )
 		{
 			this.jobNumber = jobNumber;
 		}
 		public void setPlevel( Project.Plevel plevel )
 		{
 			this.plevel = plevel;
 		}
 		public Project.Plevel getPlevel()
 		{
 			return plevel;
 		}
 		
 		public String stringify()
 		{
 			StringBuffer sb = new StringBuffer();
 			
 			sb.append( "Job name  : " + this.jobName + linesep );
 			sb.append( "Job number: " + this.jobNumber + linesep );
 			sb.append( "Component : " + this.component + linesep );
 			sb.append( "Stream    : " + this.stream + linesep );
 			sb.append( "Baseline  : " + this.baseline + linesep );
 			sb.append( "Plevel    : " + ( this.plevel != null ? this.plevel.toString() : "Missing" ) + linesep );
 			sb.append( "postBuild : " + this.doPostBuild + linesep );
 			
 			return sb.toString();
 		}
 		
 		public String toString()
 		{
 			return "(" + jobName + ", " + jobNumber + ")";
 		}
 	}
 	
 	
 
 }
