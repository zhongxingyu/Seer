package tk.bnbm.clockdrive4j.model;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.not;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.hamcrest.MatcherAssert.assertThat;
 
 import java.awt.geom.Point2D;
 import java.util.Calendar;
 import java.util.Date;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.experimental.theories.DataPoints;
 import org.junit.experimental.theories.Theories;
 import org.junit.experimental.theories.Theory;
 import org.junit.runner.RunWith;
 
 @RunWith(Theories.class)
 public class CarTest {
 
 	private Road road;
 	private Car car;
 
 	@Before
 	public void setUp() throws Exception {
 		// 環境依存を減らすため、現在pathから類推
 		road = new Road("target/classes/datas/roadData.csv");
 		car = new Car(road);
 	}
 
 	@Test
 	public void 時間を与えられる() {
 		car.setTime(new Date());
 	}
 
 	@Test
 	public void プロパティが読める() {
 
 		car.setTime(new Date());
 
 		Point2D.Double position = car.getPosition();
 		double angle = car.getAngle();
 
 		assertThat(position, is(notNullValue()));
 		assertThat(angle, is(not(0D)));
 
 	}
 
 	@DataPoints
 	public static Object[][] VALUES = { { 0, 0, 0, 0, 0, 30, "異なるはず" },
 			{ 0, 1, 0, 0, 2, 0, "異なるはず" }, { 1, 59, 0, 2, 0, 0, "異なるはず" },
 			{ 11, 59, 0, 12, 0, 0, "異なるはず" }, { 0, 0, 0, 12, 0, 0, "同じはず" },
 			{ 6, 0, 0, 18, 0, 0, "同じはず" } };
 
 	@Theory
 	public void 与えた時間に応じて_角度が変化する(Object[] values) {
 		// パラメータ解析。
 		int hourA = ((Integer) values[0]).intValue();
 		int minuteA = ((Integer) values[1]).intValue();
 		int secondA = ((Integer) values[2]).intValue();
 		int hourB = ((Integer) values[3]).intValue();
 		int minuteB = ((Integer) values[4]).intValue();
 		int secondB = ((Integer) values[5]).intValue();
 		String expectTo = (String) values[6];
 		Calendar c = Calendar.getInstance();
 
 		// Act(実行)
 		c.set(2001, 1, 1, hourA, minuteA, secondA);
 		car.setTime(c.getTime());
 		Double angleA = car.getAngle();
 
 		c.set(2001, 1, 1, hourB, minuteB, secondB);
 		car.setTime(c.getTime());
 		Double angleB = car.getAngle();
 
 		// Assert(検証)
 		if (expectTo.contains("異")) {
 			assertThat(angleA, is(not(angleB)));
 		} else {
 			assertThat(angleA, is(angleB));
 		}
 	}
 }
