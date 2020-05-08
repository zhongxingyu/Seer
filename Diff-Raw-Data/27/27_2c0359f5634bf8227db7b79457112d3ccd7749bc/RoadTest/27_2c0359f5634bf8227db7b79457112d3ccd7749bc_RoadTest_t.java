 package tk.bnbm.clockdrive4j.model;
 
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
 import static org.hamcrest.MatcherAssert.assertThat;
 
 import java.awt.geom.Point2D;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Date;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.experimental.runners.Enclosed;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
 import org.junit.runner.RunWith;
 
 /**
  * Roadクラスのテスト。
 *
  * @author kazuhito_m
  */
 @RunWith(Enclosed.class)
 public class RoadTest {
 
 	@RunWith(Theories.class)
 	public static class RoadクラスのgetRoadPositionメソッドのテスト {
 
 		private Road sut;
 
 		@Before
 		public void setUp() throws NumberFormatException, IOException {
			sut = new Road("target/classes/datas/roadData.csv");
 		}
 
 		@Test
 		public void 時間を与えられ_道路上の座標を得られる() throws Exception {
 			sut.getRoadPosition(new Date());
 		}
 
 		@DataPoints
 		public static Object[][] VALUES = { { 0, 0, 0, 0, 0, 30, "異なるはず" },
 				{ 0, 1, 0, 0, 2, 0, "異なるはず" }, { 1, 59, 0, 2, 0, 0, "異なるはず" },
 				{ 11, 59, 0, 12, 0, 0, "異なるはず" },
 				{ 0, 0, 0, 12, 0, 0, "同じはず" }, { 6, 0, 0, 18, 0, 0, "同じはず" } };
 
 		@Theory
 		public void 与えた時間に応じて_道路上の座標が変化する(Object[] values) {
 
 			int hourA = ((Integer) values[0]).intValue();
 			int minuteA = ((Integer) values[1]).intValue();
 			int secondA = ((Integer) values[2]).intValue();
 			int hourB = ((Integer) values[3]).intValue();
 			int minuteB = ((Integer) values[4]).intValue();
 			int secondB = ((Integer) values[5]).intValue();
 			String expectTo = (String) values[6];

 			Calendar c = Calendar.getInstance();
 
 			c.set(2001, 1, 1, hourA, minuteA, secondA);
 			Point2D.Double from = sut.getRoadPosition(c.getTime());
 
 			c.set(2001, 1, 1, hourB, minuteB, secondB);
 			Point2D.Double to = sut.getRoadPosition(c.getTime());
 
 			if (expectTo.contains("異")) {
 				assertThat(from, is(not(to)));
 			} else {
 				assertThat(from, is(to));
 			}
 		}
 	}
 
 	@RunWith(Theories.class)
 	public static class RoadクラスのcalcPositionRatioメソッドパラメタライズドテスト {
 
 		@DataPoints
 		public static double[][] VALUES = { { 0, 0, 0, 0.0 }, { 6, 0, 0, 0.5 },
 				{ 12, 0, 0, 0 }, { 23, 0, 0, 1.0 - (1.0 / 12) } };
 
 		@Theory
 		public void 与えた時刻に応じて文字盤上の角度が得られる(double[] values) {
 			double angle = values[3];
 			Calendar c = Calendar.getInstance();
 			c.set(2000, 1, 1, (int) values[0], (int) values[1], (int) values[2]);
 
 			assertThat(angle, is(Road.calcPositionRatio(c.getTime())));
 		}
 	}
 
 }
