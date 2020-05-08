 package net.praqma.geometry;
 
 import org.junit.Test;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 
 public class TestRutPolygon extends Geometry {
 
 	@Test
 	public void rutPoly() {
 		
 		Polygon p = new Polygon();
 		p.addPoint( 12.334562619445387, 55.67289341268228 ).addPoint( 12.33499177288854, 55.67093310816091 ).addPoint( 12.33816750836057, 55.67135663906081 ).addPoint( 12.338038762329136, 55.672966014667445 );
 		p.endPolygon();
 		
 		Point point = new Point( 12.3304366092813, 55.6721631491783 );
 		
 		assertThat( p.contains( point ), is( false ) );
 	}
 
     @Test
    public void rutPoly2() {
 
         Polygon p = new Polygon();
         p.addPoint( 4.562619445387, 2.89341268228 ).addPoint( 4.99177288854, 0.93310816091 ).addPoint( 8.16750836057, 1.35663906081 ).addPoint( 8.038762329136, 2.966014667445 );
         p.endPolygon();
 
         Point point = new Point( 0.4366092813, 2.1631491783 );
 
         assertThat( p.contains( point ), is( false ) );
     }
 }
