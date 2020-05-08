 package net.praqma.hudson.remoting;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.Set;
 
 import net.praqma.clearcase.ucm.UCMException;
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.ucm.entities.Stream;
 import net.praqma.clearcase.ucm.entities.UCMEntity;
 import net.praqma.clearcase.ucm.view.SnapshotView;
 import net.praqma.hudson.scm.ClearCaseChangeset;
 import net.praqma.hudson.scm.ClearCaseChangeset.Element;
 import net.praqma.util.debug.Logger;
 import net.praqma.util.debug.appenders.StreamAppender;
 
 import hudson.FilePath.FileCallable;
 import hudson.model.BuildListener;
 import hudson.remoting.Pipe;
 import hudson.remoting.VirtualChannel;
 
 public class RemoteDeliverComplete implements FileCallable<Boolean> {
 
 	private static final long serialVersionUID = 2506984544940354996L;
 
 	private boolean complete;
 	private BuildListener listener;
 	private Pipe pipe;
 
 	private Baseline baseline;
 	private Stream stream;
 	private SnapshotView view;
 	private ClearCaseChangeset changeset;
 
 	private Set<String> subscriptions;
 
 	public RemoteDeliverComplete( Baseline baseline, Stream stream, SnapshotView view, ClearCaseChangeset changeset, boolean complete, BuildListener listener, Pipe pipe, Set<String> subscriptions ) {
 		this.complete = complete;
 		this.listener = listener;
 		this.pipe = pipe;
 
 		this.baseline = baseline;
 		this.stream = stream;
 		this.view = view;
 		this.changeset = changeset;
 
 		this.subscriptions = subscriptions;
 	}
 
 	@Override
 	public Boolean invoke( File f, VirtualChannel channel ) throws IOException, InterruptedException {
 
 		PrintStream out = listener.getLogger();
 
 		Logger logger = Logger.getLogger();
     	StreamAppender app = null;
     	if( pipe != null ) {
 	    	PrintStream toMaster = new PrintStream( pipe.getOut() );
 	    	app = new StreamAppender( toMaster );
 	    	Logger.addAppender( app );
 	    	app.setSubscriptions( subscriptions );
     	}
 
     	if( complete ) {
 
 			try {
 				baseline.deliver( baseline.getStream(), stream, view.getViewRoot(), view.getViewtag(), true, true, true );
 			} catch (UCMException ex) {
 
 				try {
 					baseline.cancel( view.getViewRoot() );
 				} catch (UCMException ex1) {
 	        		Logger.removeAppender( app );
 					throw new IOException( "Completing the deliver failed. Could not cancel." );
 				}
 	        	Logger.removeAppender( app );
 				throw new IOException( "Completing the deliver failed. Deliver was cancelled." );
 			}
 
 		} else {
 			out.println( "Cancelling" );
 			try {
 				baseline.cancel( view.getViewRoot() );
 			} catch (UCMException ex) {
 				Logger.removeAppender( app );
 				throw new IOException( "Could not cancel the deliver." );
 			}
 		}
 
 		Logger.removeAppender( app );
 		return true;
 	}
 
 }
