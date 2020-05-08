 package au.gov.ga.conn4d.test.impl.collision;
 
 
 import org.junit.Before;
 import org.junit.Test;
 
 import au.gov.ga.conn4d.impl.collision.Intersector_3D_Raster;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.LineSegment;
 
 public class Intersector_3D_RasterTest {
 
 	Intersector_3D_Raster i3d = new Intersector_3D_Raster();
 	
 	@Before
 	public void setup(){
 		
 	}
 	
 	@Test
 	public void testIntersect() {
 		Coordinate a = new Coordinate(113.25900917753951, -23.12858311295413, -51.019166911993054);
 		Coordinate b = new Coordinate(113.25463365242233, -23.1601362814392, -51.02637400036136);
 		
 		LineSegment ls = new LineSegment(a,b);
 		Coordinate[] vertices = new Coordinate[]
 		{new Coordinate(113.25261561937987, -23.137438714125906, -318.65625), 
 		 new Coordinate (113.26261561878555, -23.137438714125906, -302.859375), 
 		 new Coordinate (113.26261561878555, -23.127438714720235, -309.171875), 
 		 new Coordinate (113.25261561937987, -23.127438714720235, -327.875)
 		};
 		
 		Coordinate isect = i3d.intersect(ls, vertices);
		
		//System.out.println(isect);
		
 	}

 }
