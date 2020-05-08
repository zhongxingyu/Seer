 package net.praqma.geometry;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import static org.hamcrest.CoreMatchers.*;
 
 
 public class TestPolygon extends Geometry {
 	
 	/**
 	 * <pre>
 	 *  3  a1 a2 a3 a4 a5
 	 *                  
 	 *  2  b1 b2-b3-b4 b5
 	 *        |     |
 	 *  1  c1 c2 c3 c4 c5
 	 *        |     |
 	 *  0  d1 d2-d3-d4 d5
 	 *                  
 	 * -1  e1 e2 e3 e4 e5
	 *  
 	 *     -1 0  1  2  3
 	 * </pre>
 	 */
 	@Test
 	public void isPointInside() {
 		
 		Polygon p = new Polygon();
 		p.addPoint( 0, 0 ).addPoint( 2, 0 ).addPoint( 2, 2 ).addPoint( 0, 2 );
 		p.endPolygon();
 		
 		Point a1 = new Point( -1.0, 3.0 );
 		Point a2 = new Point( 0.0, 3.0 );
 		Point a3 = new Point( 1.0, 3.0 );
 		Point a4 = new Point( 2.0, 3.0 );
 		Point a5 = new Point( 3.0, 3.0 );
 		
 		assertThat( p.contains( a1 ), is( false ) );
 		assertThat( p.contains( a2 ), is( false ) );
 		assertThat( p.contains( a3 ), is( false ) );
 		assertThat( p.contains( a4 ), is( false ) );
 		assertThat( p.contains( a5 ), is( false ) );
 		
 		Point b1 = new Point( -1.0, 2.0 );
 		Point b2 = new Point( 0.0, 2.0 );
 		Point b3 = new Point( 1.0, 2.0 );
 		Point b4 = new Point( 2.0, 2.0 );
 		Point b5 = new Point( 3.0, 2.0 );
 		
 		assertThat( p.contains( b1 ), is( false ) );
 		assertThat( p.contains( b2 ), is( true ) );
 		assertThat( p.contains( b3 ), is( true ) );
 		assertThat( p.contains( b4 ), is( false ) );
 		assertThat( p.contains( b5 ), is( false ) );
 		
 		Point c1 = new Point( -1.0, 1.0 );
 		Point c2 = new Point( 0.0, 1.0 );
 		Point c3 = new Point( 1.0, 1.0 );
 		Point c4 = new Point( 2.0, 1.0 );
 		Point c5 = new Point( 3.0, 1.0 );
 		
 		assertThat( p.contains( c1 ), is( false ) );
 		assertThat( p.contains( c2 ), is( true ) );
 		assertThat( p.contains( c3 ), is( true ) );
 		assertThat( p.contains( c4 ), is( false ) );
 		assertThat( p.contains( c5 ), is( false ) );
 		
 		Point d1 = new Point( -1.0, 0.0 );
 		Point d2 = new Point( 0.0, 0.0 );
 		Point d3 = new Point( 1.0, 0.0 );
 		Point d4 = new Point( 2.0, 0.0 );
 		Point d5 = new Point( 3.0, 0.0 );
 		
 		assertThat( p.contains( d1 ), is( false ) );
 		assertThat( p.contains( d2 ), is( false ) );
 		assertThat( p.contains( d3 ), is( false ) );
 		assertThat( p.contains( d4 ), is( false ) );
 		assertThat( p.contains( d5 ), is( false ) );
 		
 		Point e1 = new Point( -1.0, -1.0 );
 		Point e2 = new Point( 0.0, -1.0 );
 		Point e3 = new Point( 1.0, -1.0 );
 		Point e4 = new Point( 2.0, -1.0 );
 		Point e5 = new Point( 3.0, -1.0 );
 		
 		assertThat( p.contains( e1 ), is( false ) );
 		assertThat( p.contains( e2 ), is( false ) );
 		assertThat( p.contains( e3 ), is( false ) );
 		assertThat( p.contains( e4 ), is( false ) );
 		assertThat( p.contains( e5 ), is( false ) );
 
 	}
 	
 	/**
 	 * 
 	 * <pre>
 	 * 4      a1
 	 *       /  \
 	 * 3   a2    a3
 	 *     /      \
 	 * 2  a4      a5
 	 *     \      /
 	 * 1   a6    a7
 	 *       \  /
 	 * 0      a8
 	 * 
 	 *    0   1   2
 	 *      
 	 * </pre>
 	 */
 	@Test
 	public void containsSkew() {
 		
 		Polygon p = new Polygon();
 		p.addPoint( 1, 0 ).addPoint( 2, 2 ).addPoint( 1, 4 ).addPoint( 0, 2 );
 		p.endPolygon();
 		
 		Point a1 = new Point( 1.0, 4.0 );
 		Point a2 = new Point( 0.5, 3.0 );
 		Point a3 = new Point( 1.5, 3.0 );
 		Point a4 = new Point( 0.0, 2.0 );
 		Point a5 = new Point( 2.0, 2.0 );
 		Point a6 = new Point( 0.5, 1.0 );
 		Point a7 = new Point( 1.5, 1.0 );
 		Point a8 = new Point( 1.0, 0.0 );
 		
 		assertThat( p.contains( a1 ), is( false ) );
 		assertThat( p.contains( a2 ), is( true ) );
 		assertThat( p.contains( a3 ), is( false ) );
 		assertThat( p.contains( a4 ), is( true ) );
 		assertThat( p.contains( a5 ), is( false ) );
 		assertThat( p.contains( a6 ), is( true ) );
 		assertThat( p.contains( a7 ), is( false ) );
 		assertThat( p.contains( a8 ), is( false ) );
 	}
 }
