 package net.praqma.util.eniro.map;
 
 import com.google.gson.JsonArray;
 import org.junit.Test;
 
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.List;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertThat;
 
 /**
  * User: cwolfgang
  * Date: 05-11-12
  * Time: 23:13
  */
 public class RoadGeometryTest {
 
     @Test
     public void test1() throws IOException, EniroMapException, URISyntaxException {
         JsonArray points = RoadGeometry.get( "Ejbyvej, 2610" );
         assertNotNull( points );
         assertThat( points.size(), is( 10 ) );
     }
 
     @Test( expected = EniroMapException.class )
     public void test2() throws IOException, EniroMapException, URISyntaxException {
        JsonArray points = RoadGeometry.get( "Ingenvej, 2610" );
     }
 }
