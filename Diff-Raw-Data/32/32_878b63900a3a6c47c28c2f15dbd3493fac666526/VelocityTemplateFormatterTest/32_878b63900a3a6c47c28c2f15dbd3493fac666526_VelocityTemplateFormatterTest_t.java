 package com.redhat.rcm.nexus.capture.template;
 
import static org.junit.Assert.assertEquals;

import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.velocity.DefaultVelocityComponent;
 import org.junit.Test;
 
 import com.redhat.rcm.nexus.protocol.CaptureSessionResource;
 import com.redhat.rcm.nexus.protocol.CaptureTargetResource;
 
 import edu.emory.mathcs.backport.java.util.Collections;
 
 public class VelocityTemplateFormatterTest
 {
 
     @Test
     @SuppressWarnings( "unchecked" )
     public void formatCaptureSessionResource_RemoteURLsOnly()
        throws Exception
     {
         final String[] hosts = { "foo", "bar", "baz" };
 
         final List<CaptureTargetResource> targets = new ArrayList<CaptureTargetResource>();
         for ( final String host : hosts )
         {
             targets.add( new CaptureTargetResource( null, null, null, null, null, false, null, "http://www." + host
                             + ".com/path/to/artifact.jar" ) );
         }
 
         final CaptureSessionResource resource = new CaptureSessionResource( null, null, null, null, targets, null );
 
        final DefaultVelocityComponent velocity = new DefaultVelocityComponent();
        velocity.enableLogging( new ConsoleLogger( Logger.LEVEL_DEBUG, "test" ) );
        velocity.initialize();

        final VelocityTemplateFormatter formatter =
            new VelocityTemplateFormatter( velocity, new File( "does/not/exist" ) );

         formatter.initialize();
 
         final Map<String, Object> ctx = Collections.singletonMap( "data", resource );
 
         final String result = formatter.format( TemplateConstants.LOG_TEMPLATE_BASEPATH, "remote-urls", ctx );

        final StringBuilder sb = new StringBuilder();
        for ( final String host : hosts )
        {
            sb.append( "http://www." ).append( host ).append( ".com/path/to/artifact.jar\n" );
        }

         System.out.println( result );

        assertEquals( sb.toString(), result );
     }
 
 }
