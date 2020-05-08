 package org.jtrace.cameras;
 
 import org.jtrace.primitives.Point3D;
 import org.jtrace.primitives.Vector3D;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 public class PinHoleCameraUnitTest {
 
 	private static Point3D ORIGIN = new Point3D(0, 0, 0);
 	private static Point3D EYE = new Point3D(0, 0, 10);
 	private static Vector3D UNIT_Y = new Vector3D(0, 1, 0);
 	
 	private static Camera PIN_HOLE_CAMERA = new PinHoleCamera(EYE, ORIGIN, UNIT_Y);
 	
 	@Test
 	public void testCalculateJayDirection_CenterJay() {
 		final int hres = 1;
 		final int vres = 1;
 		
 		Vector3D jayDirection = PIN_HOLE_CAMERA.createJay(0, 0, vres, hres).getDirection();
		Vector3D expectedJay = new Vector3D(0, 0, -1);
 		
		Assert.assertEquals(jayDirection, expectedJay);
 	}
 	
 	@Test
 	public void testCalculateJayDirection_TopLeftJay() {
 		final int hres = 10;
 		final int vres = 10;
 		
 		Vector3D jayDirection = PIN_HOLE_CAMERA.createJay(9, 0, vres, hres).getDirection();
 		Vector3D expectedJay = new Vector3D(-4.5, 4.5, -10).normal();
 		
 		Assert.assertEquals(jayDirection.getCoordinate().getX(), -0.379642, 0.000001);
 		Assert.assertEquals(jayDirection.getCoordinate().getY(), 0.379642, 0.000001);
 		Assert.assertEquals(jayDirection.getCoordinate().getZ(), -0.843649, 0.000001);
 
 		Assert.assertEquals(jayDirection, expectedJay);
 	}
 }
