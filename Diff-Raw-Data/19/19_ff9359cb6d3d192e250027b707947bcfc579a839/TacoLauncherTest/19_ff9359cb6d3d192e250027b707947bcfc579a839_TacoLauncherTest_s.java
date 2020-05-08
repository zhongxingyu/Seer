 package FProj;
 
 import static org.junit.Assert.*;
 import junit.framework.Assert;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class TacoLauncherTest {
 	public static FProjMain main;
 	public static TacoLauncher tacoLauncher;
 	Block testBlock;
 
 	@BeforeClass
 	public static void setUp() {
 		main = new FProjMain();
 		tacoLauncher = new TacoLauncher();
 	}
 	
 	@Test
 	public void testCastleLocation() {
 		//Test top left corner
 		Assert.assertTrue(main.getBlocks().get(197).isCastleBlock());
 		//Test top middle edge
 		Assert.assertTrue(main.getBlocks().get(202).isCastleBlock());
 		//Test top right corner
 		Assert.assertTrue(main.getBlocks().get(207).isCastleBlock());
 		//Test random top edge block
 		Assert.assertTrue(main.getBlocks().get(283).isCastleBlock());
 		//Test bottom right corner
 		Assert.assertTrue(main.getBlocks().get(441).isCastleBlock());
 		//Test bottom left corner
 		Assert.assertTrue(main.getBlocks().get(431).isCastleBlock());
 		//Test random internal block
 		Assert.assertTrue(main.getBlocks().get(356).isCastleBlock());
 	}
 	
 	@Test
 	public void testTacoLauncherLocation() {
 		//Test launcher location
 		Assert.assertTrue(main.getBlocks().get(366).isLauncherBlock());
 	}
 	
 	@Test
 	public void testGrassLocation() {
 		//Test ends of grass
 		Assert.assertTrue(main.getBlocks().get(442).isGrassBlock());
 		Assert.assertTrue(main.getBlocks().get(467).isGrassBlock());
 	}
 	
 	@Test
 	public void testCloudLocation() {
 		//Test cloud locations
 		Assert.assertTrue(main.getBlocks().get(2).isCloudBlock());
 		Assert.assertTrue(main.getBlocks().get(84).isCloudBlock());
 		Assert.assertTrue(main.getBlocks().get(106).isCloudBlock());
 	}
 	
 	@Test
 	public void testLauncherChange() {
 		tacoLauncher.changeLauncher(25, 12);
 		//Test that changeLauncher changed angle
 		Assert.assertEquals(25.0, tacoLauncher.getAngle());
 		//Test that changeLauncher changed velocity
 		Assert.assertEquals(12.0, tacoLauncher.getVelocity());
 		
 		tacoLauncher.changeLauncher(90, 5);
 		//Test that changeLauncher changed angle
 		Assert.assertEquals(90.0, tacoLauncher.getAngle());
 		//Test that changeLauncher changed velocity
 		Assert.assertEquals(5.0, tacoLauncher.getVelocity());
 	}
 	
 	@Test
 	public void testLaunch() {
 		tacoLauncher.changeLauncher(30, 5);
 		Projectile testProjectile1 = tacoLauncher.throwProjectile();
 		
 		//Test inital location of projectile with angle = 30
 		Assert.assertEquals((10 + 5 * Math.cos(30)), testProjectile1.getX());
 		Assert.assertEquals(10 + 5 * Math.sin(30), testProjectile1.getY());
 		
 		//Test location at 3 time steps
 		testProjectile1.stepTimer(3);
 		Assert.assertTrue((10 + 5 * Math.cos(30) + testProjectile1.getDx()*3- testProjectile1.getX())<.000001);
 		Assert.assertTrue((10 + 5 * Math.cos(30) + testProjectile1.getDy()*3- testProjectile1.getY())<.000001);
 		
		//Test landing
		testProjectile1.setTimer(true);
		while(testProjectile1.getY()>0){
			//Wait while projectile is falling
			System.out.println("waiting...");
		}
		Assert.assertTrue(testProjectile1.isLanded());
 		
 		tacoLauncher.changeLauncher(45, 5);
 		Projectile testProjectile2 = tacoLauncher.throwProjectile();
 		//Test initial location of projectile with angle = 45
 		Assert.assertEquals((10 + 5 * Math.cos(45)), testProjectile2.getX());
 		Assert.assertEquals(10 + 5 * Math.sin(45), testProjectile2.getY());
 		
 	}
 
 }
