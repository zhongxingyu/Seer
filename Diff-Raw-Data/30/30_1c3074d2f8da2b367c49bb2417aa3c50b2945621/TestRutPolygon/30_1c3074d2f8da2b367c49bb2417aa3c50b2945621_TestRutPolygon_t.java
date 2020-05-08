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

    @Test
    public void rutPoly3() {

        Polygon p = new Polygon();
        p.addPoint( 12.461793988551225,55.67976726842135 ).addPoint( 12.461579411830385,55.67156381784342 ).addPoint( 12.462952702845898,55.671660623749055 ).addPoint( 12.463124364222589,55.676113436533356 ).addPoint( 12.463596433009696, 55.67971887552748 );
        p.endPolygon();

        Point point1_1 = new Point( 12.4487090944424, 55.6797463411036 );
        Point point1_2 = new Point( 12.4466566276518, 55.6797500964789 );

        Point point2_1 = new Point( 12.4622486075482, 55.6779172094357 );

        /* Outside */
        assertThat( p.contains( point1_1 ), is( false ) );
        assertThat( p.contains( point1_2 ), is( false ) );

        /* Inside */
        assertThat( p.contains( point2_1 ), is( true ) );
    }
 }
