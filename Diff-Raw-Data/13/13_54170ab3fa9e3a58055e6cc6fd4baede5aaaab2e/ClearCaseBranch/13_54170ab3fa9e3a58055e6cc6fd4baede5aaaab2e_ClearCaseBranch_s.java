 package net.praqma.vcs.model.clearcase;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import net.praqma.clearcase.PVob;
 import net.praqma.clearcase.ucm.UCMException;
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.ucm.entities.Component;
 import net.praqma.clearcase.ucm.entities.Stream;
 import net.praqma.clearcase.ucm.view.SnapshotView;
 import net.praqma.clearcase.ucm.view.SnapshotView.Components;
 import net.praqma.util.debug.Logger;
 import net.praqma.vcs.VersionControlSystems;
 import net.praqma.vcs.model.AbstractBranch;
 import net.praqma.vcs.model.AbstractCommit;
 import net.praqma.vcs.model.exceptions.ElementAlreadyExistsException;
 import net.praqma.vcs.model.exceptions.ElementDoesNotExistException;
 import net.praqma.vcs.model.exceptions.ElementException;
 import net.praqma.vcs.model.exceptions.ElementNotCreatedException;
 import net.praqma.vcs.model.exceptions.UnableToCheckoutCommitException;
 
 /**
  * An implementation of {@link AbstractBranch} for Clearcase, where {@link Baseline}'s are used as commit separator.
  * @author wolfgang
  *
  */
 public class ClearCaseBranch extends AbstractBranch{
 	
 	/**
 	 * The input part of the ClearCase branch
 	 */
 	protected ClearCaseBranchPart input;
 	
 	/**
 	 * The output part of the ClearCase branch
 	 */
 	protected ClearCaseBranchPart output;
 	
 	/**
 	 * The PVob
 	 */
 	protected PVob pvob;
 	
 	/**
 	 * The Component
 	 */
 	protected Component component;
 	
 	/**
 	 * The foundation baseline
 	 */
 	protected Baseline baseline;
 	
 	/**
 	 * Determines whether to care if one or the other part of the ClearCase branch fails its initialization
 	 */
 	protected boolean dontCare = false;
 	
 	transient protected static Logger logger = Logger.getLogger();
 	
 	/**
 	 * If the Stream does not exist, it will be created as a child of the Stream parent.
 	 * @param pvob
 	 * @param parent
 	 * @param baseline
 	 * @param viewroot
 	 * @param viewtag
 	 * @param name
 	 * @throws ElementNotCreatedException
 	 */
 	public ClearCaseBranch( PVob pvob, Stream parent, Baseline baseline, File viewroot, String viewtag, String name ) throws ElementNotCreatedException {
 		super(viewroot, name);
 		
 		input = new ClearCaseBranchPart( pvob, parent, baseline, new File( viewroot, "_in" ), viewtag, name );
 		output = new ClearCaseBranchPart( pvob, parent, baseline, new File( viewroot, "_out" ), viewtag + "_out", name + "_out" );
 		
 		this.pvob = input.getPVob();
 		this.component = input.getComponent();
 		this.baseline = input.getBaseline();
 
 		/* Set this to the view root of the out view */
 		this.localRepositoryPath = output.getPath();
 	}
 	
 	/**
 	 * Constructor used for branches where everything is created.
 	 * @param pvob
 	 * @param component
 	 * @param viewroot
 	 * @param viewtag
 	 * @param name
 	 * @throws ElementNotCreatedException
 	 */
 	public ClearCaseBranch( PVob pvob, Component component, File viewroot, String viewtag, String name ) throws ElementNotCreatedException {
 		super(viewroot, name);
 		
 		input = new ClearCaseBranchPart( pvob, component, new File( viewroot, "_in" ), viewtag, name );
 		output = new ClearCaseBranchPart( pvob, component, new File( viewroot, "_out" ), viewtag + "_out", name + "_out" );
 		
 		this.pvob = input.getPVob();
 		this.component = input.getComponent();
 		this.baseline = input.getBaseline();
 		
 		/* Set this to the view root of the out view */
 		this.localRepositoryPath = output.getPath();
 	}
 	
 	public ClearCaseBranch( ClearCaseBranchPart input, ClearCaseBranchPart output ) throws ElementNotCreatedException {
 		super(input.getPath(), input.getStreamName());
 		
 		this.input = input;
 		this.output = output;
 		
 		this.pvob = input.getPVob();
 		this.component = input.getComponent();
 		this.baseline = input.getBaseline();
 		
 		/* Set this to the view root of the out view */
 		this.localRepositoryPath = output.getPath();
 	}
 	
 	public ClearCaseBranch( ClearCaseBranchPart input, boolean isInput ) throws ElementNotCreatedException {
 		super(input.getPath(), input.getStreamName());
 		
 		if( isInput ) {
 			this.input = input;
 		} else {
 			this.output = input;
 		}
 		
 		this.pvob = input.getPVob();
 		this.component = input.getComponent();
 		this.baseline = input.getBaseline();
 		
 		/* Set this to the view root of the out view */
 		this.localRepositoryPath = input.getPath();
 	}
 
 	@Override
 	public void initialize() throws ElementNotCreatedException, ElementAlreadyExistsException {
 		try {
 			initialize( false );
 		} catch( ElementDoesNotExistException e ) {
 			/* This shouldn't be possible */
 			logger.fatal( "FAIL!! Shouldn't throw exist exceptions!!!" );
 		}
 	}
 	
 	public void initialize( boolean get ) throws ElementNotCreatedException, ElementAlreadyExistsException, ElementDoesNotExistException {
 		logger.info( "Creating Clearcase branch/stream " + name );
 		if( !doInitialize( new InitializeImpl( get ) ) ) {
 			throw new ElementNotCreatedException( "Could not create Clearcase branch" );
 		}
 	}
 	
 	protected class InitializeImpl extends Initialize {
 		public InitializeImpl( boolean get ) {
 			super( get );
 			logger.debug( "InitializeIMPL" );
 		}
 
 		public boolean initialize() throws ElementDoesNotExistException, ElementNotCreatedException, ElementAlreadyExistsException {
 
 			/* TODO Make exception handling less verbose! If possible */
 			logger.debug( "Trying to initialize input" );
 			try {
 				if( input != null ) {
 					logger.debug( "Initializing input" );
 					input.initialize( get );
 				}
 			} catch( ElementDoesNotExistException e ) {
 				if( !dontCare ) {
 					throw e;
 				}
 			} catch( ElementNotCreatedException e ) {
 				if( !dontCare ) {
 					throw e;
 				}
 			} catch( ElementAlreadyExistsException e ) {
 				if( !dontCare ) {
 					throw e;
 				}
 			}
 			
 			logger.debug( "Trying to initialize output" );
 			try {
 				if( output != null ) {
 					if( output.getParent() == null && output.getParent() == null && input.getStream() != null ) {
 						logger.debug( "The ouput parent was null, setting it to input stream" );
 						output.setParentStream( input.getStream() );
 					}
 					logger.debug( "Initializing output" );
 					output.initialize( get );
 				}
 			} catch( ElementDoesNotExistException e ) {
 				if( !dontCare ) {
 					throw e;
 				}
 			} catch( ElementNotCreatedException e ) {
 				if( !dontCare ) {
 					throw e;
 				}
 			} catch( ElementAlreadyExistsException e ) {
 				if( !dontCare ) {
 					throw e;
 				}
 			}
 			
 			return true;
 		}
 	}
 	
 	
 	
 	public boolean exists() {
 		boolean result = true;
 		
 		if( input != null ) {
 			if( !input.exists() ) {
 				result = false;
 			}
 		}
 		
 		if( output != null ) {
 			if( !output.exists() ) {
 				result = false;
 			}
 		}
 		
 		return result;
 	}
 	
 	public void get() throws ElementDoesNotExistException {
 		try {
 			get( false );
 		} catch( ElementNotCreatedException e ) {
 			/* This should not happen */
 			/* TODO Should we throw DoesNotExist? */
 			logger.fatal( "This shouldn't be possible..." );
 		}
 	}
 	
 	public void get( boolean initialize ) throws ElementDoesNotExistException, ElementNotCreatedException {
 		
 		boolean exists = exists();
 		
 		if( !exists && initialize ) {
 			logger.debug( "Must initialize" );
 			try {
 				initialize( true );
 			} catch (ElementAlreadyExistsException e) {
 				/* This should not happen */
 				/* TODO Should we throw ElementAlreadyExistsException? */
 				logger.fatal( "This shouldn't be possible..." );
 			}
 		} else {
 			logger.debug( "DONT initialize" );
 			if( input != null ) {
 				input.initializeView();
 				input.initializeStream();
 			}
 			
 			if( output != null ) {
 				output.initializeView();
 				output.initializeStream();
 			}
 		}
 	}
 	
 	
 	public void update() {
 		doUpdate( new UpdateImpl() );
 	}
 
 	/**
 	 * This Update implementation only updates the input view, because the output view 
 	 * does not need to be updated, only when demanded.
 	 * @author wolfgang
 	 *
 	 */
 	public class UpdateImpl extends Update {
 
 		public boolean setup() {
 			//if( snapshot_in == null || devStream_in == null ) {
 			if( input.getSnapshotView() == null || input.getStream() == null ) {
 				try {
 					get();
 				} catch (ElementDoesNotExistException e) {
 		        	logger.error("Error while getting stream: " + e.getMessage());
 		        	return false;
 				}
 			}
 			
 			return true;
 		}
 		
 		public boolean update() {
 			try {
 				input.getSnapshotView().Update( true, true, true, false, Components.ALL, null );
 			} catch (UCMException e) {
 	        	logger.error("Error while updating view: " + e.getMessage());
 	        	return false;
 			}
 			
 			return true;
 		}
 	}
 	
 	@Override
 	public void checkoutCommit( AbstractCommit commit ) throws UnableToCheckoutCommitException {
 		this.currentCommit = commit;
 		
 		if( commit instanceof ClearCaseCommit ) {
 			ClearCaseCommit cccommit = (ClearCaseCommit)commit;
 			//this.devStream_out.rebase( this.snapshot_out, cccommit.getBaseline(), true );
			this.output.getStream().rebase( this.output.getSnapshotView(), cccommit.getBaseline(), true );
 			try {
 				//this.snapshot_out.Update(true, true, true, false, COMP.ALL, null);
 				this.output.getSnapshotView().Update(true, true, true, false, Components.ALL, null);
 			} catch (UCMException e) {
 				throw new UnableToCheckoutCommitException( "Could not checkout " + cccommit.getBaseline() );
 			}
 		} else {
 			logger.warning( "I don't know how to do this!!!" );
 			throw new UnableToCheckoutCommitException( "This is not a ClearCase commit!" );
 		}
 	}
 	
 	@Override
 	public List<AbstractCommit> getCommits() {
 		return getCommits( false, null );
 	}
 	
 	@Override
 	public List<AbstractCommit> getCommits( boolean load ) {
 		return getCommits( load, null );
 	}
 	
 	@Override
 	public List<AbstractCommit> getCommits( boolean load, Date offset ) {
 		
 		List<AbstractCommit> commits = new ArrayList<AbstractCommit>();
 		
 		logger.debug( "Getting CC commits after " + null );
 				
 		try {
 			//List<Baseline> baselines = this.devStream_in.getBaselines( getComponent(), null, offset );
 			List<Baseline> baselines = null;
 			if( output.getParent() != null ) {
 				logger.debug( "Getting baselines from parent" );
 				baselines = this.output.getParent().getBaselines( getComponent(), null, offset );
 			} else if( input.getStream() != null ) {
 				logger.debug( "Getting baselines from input" );
 				baselines = this.input.getStream().getBaselines( getComponent(), null, offset );
 			} else if( output.getStream() != null ) {
 				logger.debug( "Getting baselines from output" );
 				baselines = this.output.getStream().getBaselines( getComponent(), null, offset );
 			} else {
 				logger.debug( "Couldn't get any baselines" );
 				return commits;
 			}
 			
 			logger.debug( "I got " + baselines.size() + " baselines" );
 			for( int i = 0 ; i < baselines.size() ; i++ ) {				
 				ClearCaseCommit commit = new ClearCaseCommit( baselines.get( i ), ClearCaseBranch.this, i );
 				
 				if( load ) {
 					commit.load();
 				}
 				commits.add( commit );
 			}
 		} catch (UCMException e) {
 			logger.error( "Could not list baselines: " + e.getMessage() );
 		}
 		
 		logger.debug( "Done" );
 		
 		return commits;
 	}
 	
 	/**
 	 * If the initialization of one of the ClearCase pars fails, we don't care
 	 */
 	public void iDontCare() {
 		dontCare = true;
 	}	
 	
 	public SnapshotView getOuputSnapshotView() {
 		return output.getSnapshotView();
 	}
 	
 	public SnapshotView getInputSnapshotView() {
 		return input.getSnapshotView();
 	}
 	
 	public PVob getPVob() {
 		return pvob;
 	}
 	
 	public Baseline getBaseline() {
 		return baseline;
 	}
 	
 	public void setInputPath( File path ) {
 		//this.viewroot_in = path;
 		this.input.setPath( path );
 	}
 	
 	public File getInputPath() {
 		//return this.viewroot_in;
 		return this.input.getPath();
 	}
 	
 	public void setOutputPath( File path ) {
 		//this.viewroot_out = path;
 		this.output.setPath( path );
 	}
 	
 	public File getOutputPath() {
 		//return this.viewroot_out;
 		return this.output.getPath();
 	}
 	
 	public ClearCaseBranchPart getInput() {
 		return input;
 	}
 	
 	public ClearCaseBranchPart getOutput() {
 		return output;
 	}
 	
 	@Override
 	public File getPath() {
 		//return this.developmentPath_out;
 		//return this.viewroot_out;
 		return this.output.getPath();
 	}
 	
 	/**
 	 * @deprecated
 	 * @return
 	 */
 	public File getPathIn() {
 		//return this.developmentPath_out;
 		//return this.viewroot_in;
 		return this.input.getPath();
 	}
 		
 	public Component getComponent() {
 		return this.component;
 	}
 	
 	public Stream getInputStream() {
 		//return devStream_in;
 		return this.input.getStream();
 	}
 	
 	public Stream getOutputStream() {
 		//return devStream_out;
 		return this.input.getStream();
 	}
 	
 	public Stream getParentOutputStream() {
 		return output.getStream();
 	}
 	
 	public String getInputViewtag() {
 		return input.getViewtag();
 	}
 	
 	public String getOutputViewtag() {
 		return output.getViewtag();
 	}
 
 	@Override
 	public boolean cleanup() {
 		return true;
 	}
 	
 	public String toString() { 
 		StringBuffer sb = new StringBuffer();
 		
 		sb.append( "ClearCase UCM branch\n" );
 		
 		sb.append( "Input:\n-------------------\n" );
 		if( input != null ) {
 			sb.append( input.toString() );
 		} else {
 			sb.append( "Null\n" );
 		}
 		
 		sb.append( "\nOuput:\n-------------------\n" );
 		if( output != null ) {
 			sb.append( output.toString() );
 		} else {
 			sb.append( "Null\n" );
 		}
 		
 		return sb.toString();
 	}
 
 	@Override
 	public VersionControlSystems getVersionControlSystem() {
 		return VersionControlSystems.ClearCase;
 	}
 
 }
