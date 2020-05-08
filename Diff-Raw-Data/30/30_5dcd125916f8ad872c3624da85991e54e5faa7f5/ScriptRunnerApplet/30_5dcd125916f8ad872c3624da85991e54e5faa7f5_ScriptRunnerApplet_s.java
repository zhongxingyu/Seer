 package org.dhappy.mimis;
 
 import java.util.logging.Logger;
 import java.util.logging.Level;
 
 import java.util.List;
 
 import java.io.Reader;
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.IOException;
 import java.io.StringReader;
 
 import javax.script.ScriptEngineManager;
 import javax.script.ScriptEngineFactory;
 import javax.script.ScriptEngine;
 import javax.script.ScriptException;
 
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 
 import netscape.javascript.JSObject;
 
 import javax.swing.JApplet;
 
 import telehash.switchimpl.api.InterestingEndsHolderImpl;
 
 public class ScriptRunnerApplet extends JApplet {
     private static Logger log =
 	Logger.getLogger( ScriptRunnerApplet.class.getName() );
 
     ScriptEngineManager engines = new ScriptEngineManager();
     ScriptEngine js = engines.getEngineByName( "javascript" );
 
     public void init() {
 	log.info( "Initialized: " + ScriptRunnerApplet.class.getName() );
     }
 
     public void start() {
         js.put( "hostApplet", this );
 
        // AccessController.doPrivileged( new PrivilegedAction() {
        //         public Object run() {
        //             js.put( "interestingEndsHolder", new InterestingEndsHolderImpl() );
        //             return null;
        //         }
        //     } );

        InterestingEndsHolderImpl ends = new InterestingEndsHolderImpl();
 
 	String script = this.getParameter( "script" );
 	if( script != null ) {
 	    eval( script );
 	}
     }
 
     public void eval( JSObject script ) {
 	log.info( "Eval script" );
     }
 
     public Object eval( String script ) {
 	InputStream stream = null;
 	
 	String line;
 	try {
 	    try {
 		stream = ScriptRunnerApplet.class.getResourceAsStream( script );
                 Reader reader = null;
 		if( stream == null ) {
 		    log.log( Level.WARNING, "Interpreting as script: " + script );
                     reader = new StringReader( script );
 		} else {
                     reader = new InputStreamReader( stream );
                 }
                 final BufferedReader bufferedReader = new BufferedReader( reader );
 
                 return AccessController.doPrivileged( new PrivilegedAction() {
                         public Object run() {
                             try {
                                 return js.eval( bufferedReader );
                             } catch( ScriptException se ) {
                                 log.log( Level.WARNING, se.getMessage(), se );
                             }
                             return null;
                         }
                     } );
             } finally {
 		if( stream != null ) {
 		    stream.close();
 		}
 	    }
 	} catch( IOException ioe ) {
 	    log.warning( ioe.getMessage() );
 	}
         return null;
     }
 
     public Object guidClicked( final int guid ) {
         return AccessController.doPrivileged( new PrivilegedAction() {
                 public Object run() {
                     try {
                         String callback = "guidClicked(" + guid + ")";
                         log.info( "ScriptRunnerApplet/guidClicked: " + callback );
                         
                         return js.eval( callback );
                     } catch( ScriptException se ) {
                         log.warning( se.getMessage() );
                     }
                     return null;
                 }
             } );
     }
 
     public void stop() {
 	log.info( "Stop: " + ScriptRunnerApplet.class.getName() );
     }
 
     public void destroy() {
 	log.info( "Destroying: " + ScriptRunnerApplet.class.getName() );
     }
 
     public static void main( String[] args ) {
         ScriptRunnerApplet applet = new ScriptRunnerApplet();
         for( String arg : args ) {
             applet.eval( arg );
         }
     }
 }
