 package tk.bnbm.clockdrive4j.model;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.not;
 import static org.hamcrest.CoreMatchers.nullValue;
 import static org.hamcrest.MatcherAssert.assertThat;
 
 import java.awt.geom.Point2D;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.experimental.runners.Enclosed;
 import org.junit.experimental.theories.DataPoints;
 import org.junit.experimental.theories.Theories;
 import org.junit.experimental.theories.Theory;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 
 /**
  * Roadクラスのテスト。
  * @author kazuhito_m
  */
 @RunWith(Enclosed.class)
 public class RoadTest {
 
    public static class 正常系or異常系テスト {
         @Test(expected = FileNotFoundException.class)
         public void ファイルが存在しない場合正しい例外が発生する() throws Exception {
             new Road("target/test-classes/datas/notFound.csv");
         }
 
         @Test(expected = ArithmeticException.class)
         public void 地点データが存在しなければ例外が発生する() throws Exception {
             Road sut = new Road("target/test-classes/datas/roadData.csv");
             sut.clearPosition();
 
             sut.getPosition(new Date());
         }
 
         @Test
         public void 地点データが一つもない状態から追加出来る() {
             Road sut = new Road();
             Point2D.Double point = new Point2D.Double(1D, 2D);
 
             sut.addPosition(point);
 
             assertThat(sut.getPosition(new Date()), is(not(nullValue())));
         }
     }
 
     @RunWith(Theories.class)
     public static class RoadクラスのgetRoadPositionメソッドのテスト {
 
         // 意味履き違えてTheories(総当たり)で作ってしまったが、良いサンプルなので残しておく。
         // (本来であれば、Parameterized(パラメタライズ)テストとして作るべき。)
 
         private Road sut;
 
         @Before
         public void setUp() throws NumberFormatException, IOException {
             sut = new Road("target/test-classes/datas/roadData.csv");
         }
 
         @Test
         public void 時間を与えられ_道路上の座標を得られる() throws Exception {
             sut.getPosition(new Date());
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
             Point2D.Double from = sut.getPosition(c.getTime());
 
             c.set(2001, 1, 1, hourB, minuteB, secondB);
             Point2D.Double to = sut.getPosition(c.getTime());
 
             if (expectTo.contains("異")) {
                 assertThat(from, is(not(to)));
             } else {
                 assertThat(from, is(to));
             }
         }
     }
 
     @RunWith(Parameterized.class)
     public static class RoadクラスのcalcPositionRatioメソッドパラメタライズドテスト {
 
         private int hour;
         private int minite;
         private int second;
         private double angle;
 
         public RoadクラスのcalcPositionRatioメソッドパラメタライズドテスト(int hour, int minite,
                 int second, double angle) {
             this.hour = hour;
             this.minite = minite;
             this.second = second;
             this.angle = angle;
         }
 
         @Parameters
         public static Collection<Object[]> getParam() {
             Object[][] params = { { 0, 0, 0, 0.0 }, { 6, 0, 0, 0.5 },
                     { 12, 0, 0, 0 }, { 23, 0, 0, 1.0 - (1.0 / 12) } };
             return Arrays.asList(params);
         }
 
         @Test
         public void 与えた時刻に応じて文字盤上の角度が得られる() {
             Calendar c = Calendar.getInstance();
             c.set(2000, 1, 1, hour, minite, second);
 
             assertThat(Road.calcPositionRatio(c.getTime()), is(angle));
         }
     }
 
 }
