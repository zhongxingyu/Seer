 package org.commonjava.routem.rest.live;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.junit.Assert.assertThat;
 
 import org.apache.http.HttpResponse;
 import org.commonjava.routem.model.Group;
 import org.commonjava.routem.model.MirrorOf;
 import org.jboss.arquillian.container.test.api.Deployment;
 import org.jboss.arquillian.junit.Arquillian;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 @RunWith( Arquillian.class )
 public class RedirectoryResourceTest
     extends AbstractRouteMLiveTest
 {
 
     @Deployment
     public static WebArchive createWar()
     {
         return createWar( RedirectoryResourceTest.class );
     }
 
     @Test
     public void pomRequestIsRedirectedToMirror()
         throws Exception
     {
         disableRedirection();
 
         final Group g = new Group( "org.apache.maven", "http://repo1.maven.apache.org/maven2/" );
 
         assertThat( dataManager.store( g ), equalTo( true ) );
 
         final MirrorOf m = new MirrorOf( g.getCanonicalUrl(), "http://mirrors.ibiblio.org/pub/mirrors/maven2/" );
 
         assertThat( dataManager.store( m ), equalTo( true ) );
 
         final String path = "org/apache/maven/maven/3.0.3/maven-3.0.3.pom";
 
        final HttpResponse response = getWithResponse( resourceUrl( "redirectory", path ), 307 );
         assertLocationHeader( response, "http://mirrors.ibiblio.org/pub/mirrors/maven2/" + path );
     }
 
     @Test
     public void metadataRequestIsRedirectedToMirror()
         throws Exception
     {
         disableRedirection();
 
         final Group g = new Group( "org.apache.maven", "http://repo1.maven.apache.org/maven2/" );
 
         assertThat( dataManager.store( g ), equalTo( true ) );
 
         final MirrorOf m = new MirrorOf( g.getCanonicalUrl(), "http://mirrors.ibiblio.org/pub/mirrors/maven2/" );
 
         assertThat( dataManager.store( m ), equalTo( true ) );
 
         final String path = "org/apache/maven/maven/3.0.3/maven-metadata.xml";
 
        final HttpResponse response = getWithResponse( resourceUrl( "redirectory", path ), 307 );
         assertLocationHeader( response, "http://mirrors.ibiblio.org/pub/mirrors/maven2/" + path );
     }
 
 }
