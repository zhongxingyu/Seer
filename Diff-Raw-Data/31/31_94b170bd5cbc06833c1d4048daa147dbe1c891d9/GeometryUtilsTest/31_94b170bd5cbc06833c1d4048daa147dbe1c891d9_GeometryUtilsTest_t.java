 package au.gov.ga.conn4d.test.utils;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertArrayEquals;
 
 import org.junit.Test;
 
 import au.gov.ga.conn4d.utils.GeometryUtils;
 
 public class GeometryUtilsTest {
 	
 	private double eps = 1E-8;
 
 	@Test
 	public void testProjectionConversions() {
 		assertEquals(GeometryUtils.lonlat2ceqd(new double[]{1,0})[0], 111319.5, 0.1);
 		assertEquals(GeometryUtils.lonlat2ceqd(new double[]{0,1})[1], 111319.5, 0.1);
 		assertEquals(GeometryUtils.ceqd2lonlat(new double[]{111319.5,0})[0], 1, 0.001);
 		assertEquals(GeometryUtils.ceqd2lonlat(new double[]{0,111319.5})[1], 1, 0.001);
 		double[] input = new double[]{113.40437496675112,-26.983227605170704};
 		double[] output = GeometryUtils.lonlat2ceqd(input);
 		assertArrayEquals(new double[]{1.2624117275028195E7, -3003759.156966605},output,eps);
 		double[] backagain = GeometryUtils.ceqd2lonlat(output);
 		assertArrayEquals(new double[]{113.40437496675112,-26.983227605170704},backagain,eps);
 	}
 	
 	@Test
 	public void testDistanceSphere(){
 		double rln1 = 0;
 		double rlt1 = 0; 
 		double rln2 = 1;
 		double rlt2 = 0;
 		
		assertEquals(GeometryUtils.distance_Sphere(rln1, rlt1, rln2, rlt2),111319.49079327358,1E-9);
		assertEquals(GeometryUtils.distance_Sphere(0, 0, 0, 1),17717.047222222223,1E-9);
 		double dist = Math.acos(Math.cos(rlt1) * Math.cos(rlt2) * Math.cos(rln2 -
 		 rln1) + Math.sin(rlt1) * Math.sin(rlt2));
		assertEquals((6378137d * Math.toDegrees(dist) / 360),1015112.031267312,1E-9);
 	}
 }
