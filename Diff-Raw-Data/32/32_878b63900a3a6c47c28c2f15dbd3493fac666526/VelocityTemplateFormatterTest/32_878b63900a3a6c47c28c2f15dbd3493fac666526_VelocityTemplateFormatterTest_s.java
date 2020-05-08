 package com.redhat.rcm.nexus.capture.template;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
 import org.junit.Test;
 
 import com.redhat.rcm.nexus.protocol.CaptureSessionResource;
 import com.redhat.rcm.nexus.protocol.CaptureTargetResource;
 
 import edu.emory.mathcs.backport.java.util.Collections;
 
 public class VelocityTemplateFormatterTest
 {
 
     @Test
     @SuppressWarnings( "unchecked" )
     public void formatCaptureSessionResource_RemoteURLsOnly()
        throws InitializationException,
            TemplateException
     {
         final String[] hosts = { "foo", "bar", "baz" };
 
         final List<CaptureTargetResource> targets = new ArrayList<CaptureTargetResource>();
         for ( final String host : hosts )
         {
             targets.add( new CaptureTargetResource( null, null, null, null, null, false, null, "http://www." + host
                             + ".com/path/to/artifact.jar" ) );
         }
 
         final CaptureSessionResource resource = new CaptureSessionResource( null, null, null, null, targets, null );
 
        final VelocityTemplateFormatter formatter = new VelocityTemplateFormatter();
         formatter.initialize();
 
         final Map<String, Object> ctx = Collections.singletonMap( "data", resource );
 
         final String result = formatter.format( TemplateConstants.LOG_TEMPLATE_BASEPATH, "remote-urls", ctx );
         System.out.println( result );
     }
 
 }
