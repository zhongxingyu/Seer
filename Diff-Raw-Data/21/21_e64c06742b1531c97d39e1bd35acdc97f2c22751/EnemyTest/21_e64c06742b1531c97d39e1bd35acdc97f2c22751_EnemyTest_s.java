 import static org.junit.Assert.*;
 
import java.awt.Frame;
 import java.awt.geom.Point2D;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class EnemyTest {
 
 	private Enemy FireEnemy, WaterEnemy, LightEnemy, EarthEnemy, AirEnemy;
 	private Map map = new Map();
 
 	@Before
 	public void setUp() {
 		int id = 0;
 		this.FireEnemy = new Firebat(id++, new Point2D.Double(0, 0));
 		this.WaterEnemy = new Magikarp(id++, new Point2D.Double(0, 0));
 		this.LightEnemy = new Sunbird(id++, new Point2D.Double(0, 0));
 		this.EarthEnemy = new Geodude(id++, new Point2D.Double(0, 0));
 		this.AirEnemy = new Tornadus(id++, new Point2D.Double(0, 0));
 	}
 
 	@After
 	public void tearDown() {
 		this.FireEnemy = null;
 		this.WaterEnemy = null;
 		this.LightEnemy = null;
 		this.EarthEnemy = null;
 		this.AirEnemy = null;
 	}
 
 	@Test
 	public void testIsInstantiated() {
 		assertNotNull(this.FireEnemy);
 		assertNotNull(this.WaterEnemy);
 		assertNotNull(this.LightEnemy);
 		assertNotNull(this.EarthEnemy);
 		assertNotNull(this.AirEnemy);
 	}
 
 	@Test
 	public void testCanGetElement() {
		assertEquals(Enemy.element.FIRE, this.FireEnemy.getElement());
		assertEquals(Enemy.element.WATER, this.WaterEnemy.getElement());
		assertEquals(Enemy.element.LIGHT, this.LightEnemy.getElement());
		assertEquals(Enemy.element.EARTH, this.EarthEnemy.getElement());
		assertEquals(Enemy.element.AIR, this.AirEnemy.getElement());
 	}
 
 	@Test
 	public void testCorrectSpawnPlacement() {
 		assertEquals(new Point2D.Double(0, 0), this.FireEnemy.getLocation());
 		assertEquals(new Point2D.Double(0, 0), this.WaterEnemy.getLocation());
 		assertEquals(new Point2D.Double(0, 0), this.LightEnemy.getLocation());
 		assertEquals(new Point2D.Double(0, 0), this.EarthEnemy.getLocation());
 		assertEquals(new Point2D.Double(0, 0), this.AirEnemy.getLocation());
 	}
 
 	@Test
 	public void testCorrectLocationChange() {
 		this.FireEnemy.setLocation(new Point2D.Double(0, 5));
 		assertEquals(new Point2D.Double(0, 5), this.FireEnemy.getLocation());
 		this.WaterEnemy.setLocation(new Point2D.Double(0, 5));
 		assertEquals(new Point2D.Double(0, 5), this.WaterEnemy.getLocation());
 		this.LightEnemy.setLocation(new Point2D.Double(0, 5));
 		assertEquals(new Point2D.Double(0, 5), this.LightEnemy.getLocation());
 		this.EarthEnemy.setLocation(new Point2D.Double(0, 5));
 		assertEquals(new Point2D.Double(0, 5), this.EarthEnemy.getLocation());
 		this.AirEnemy.setLocation(new Point2D.Double(0, 5));
 		assertEquals(new Point2D.Double(0, 5), this.AirEnemy.getLocation());
 	}
 
 	@Test
 	public void testIDIsIncremented() {
 		int id = this.FireEnemy.getID();
 		assertEquals(id, this.FireEnemy.getID());
 		assertEquals(id + 1, this.WaterEnemy.getID());
 		assertEquals(id + 2, this.LightEnemy.getID());
 		assertEquals(id + 3, this.EarthEnemy.getID());
 		assertEquals(id + 4, this.AirEnemy.getID());
 	}
 
 	@Test
 	public void testNameisCorrect() {
 		assertEquals("Firebat", this.FireEnemy.getName());
 		assertEquals("Magikarp", this.WaterEnemy.getName());
 		assertEquals("Sunbird", this.LightEnemy.getName());
 		assertEquals("Geodude", this.EarthEnemy.getName());
 		assertEquals("Tornadus", this.AirEnemy.getName());
 	}
 
 	@Test
 	public void testSpeedIsCorrect() {
 		float delta = 0.01f;
 		assertEquals(1.2f, this.FireEnemy.getSpeed(), delta);
 		assertEquals(2.0f, this.WaterEnemy.getSpeed(), delta);
 		assertEquals(1.5f, this.LightEnemy.getSpeed(), delta);
 		assertEquals(0.8f, this.EarthEnemy.getSpeed(), delta);
 		assertEquals(2.3f, this.AirEnemy.getSpeed(), delta);
 	}
 
 	@Test
 	public void testArmorIsCorrect() {
 		assertEquals(2, this.FireEnemy.getArmor());
 		assertEquals(1, this.WaterEnemy.getArmor());
 		assertEquals(2, this.LightEnemy.getArmor());
 		assertEquals(5, this.EarthEnemy.getArmor());
 		assertEquals(3, this.AirEnemy.getArmor());
 	}
 
 	@Test
 	public void testHPIsCorrect() {
 		assertEquals(300, this.FireEnemy.getHP());
 		assertEquals(100, this.WaterEnemy.getHP());
 		assertEquals(100, this.LightEnemy.getHP());
 		assertEquals(400, this.EarthEnemy.getHP());
 		assertEquals(500, this.AirEnemy.getHP());
 	}
 
 	@Test
 	public void testDamageIsCorrect() {
 		assertEquals(300, this.FireEnemy.getHP());
 		assertEquals(100, this.WaterEnemy.getHP());
 		assertEquals(100, this.LightEnemy.getHP());
 		assertEquals(400, this.EarthEnemy.getHP());
 		assertEquals(500, this.AirEnemy.getHP());
 
 		this.FireEnemy.damage(50);
 		this.WaterEnemy.damage(50);
 		this.LightEnemy.damage(50);
 		this.EarthEnemy.damage(50);
 		this.AirEnemy.damage(50);
 
 		assertEquals(255, this.FireEnemy.getHP());
 		assertEquals(54, this.WaterEnemy.getHP());
 		assertEquals(55, this.LightEnemy.getHP());
 		assertEquals(360, this.EarthEnemy.getHP());
 		assertEquals(458, this.AirEnemy.getHP());
 
 		this.FireEnemy.damage(70);
 		this.WaterEnemy.damage(70);
 		this.LightEnemy.damage(70);
 		this.EarthEnemy.damage(70);
 		this.AirEnemy.damage(70);
 
 		assertEquals(190, this.FireEnemy.getHP());
 		assertEquals(0, this.WaterEnemy.getHP());
 		assertEquals(0, this.LightEnemy.getHP());
 		assertEquals(300, this.EarthEnemy.getHP());
 		assertEquals(396, this.AirEnemy.getHP());
 	}
 }
