package tk.bnbm.clockdrive4j.model;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.hamcrest.MatcherAssert.assertThat;
 
 import java.io.File;
 import java.util.Calendar;
 import java.util.Date;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.experimental.runners.Enclosed;
 import org.junit.experimental.theories.DataPoints;
 import org.junit.experimental.theories.Theories;
 import org.junit.experimental.theories.Theory;
 import org.junit.runner.RunWith;
 
 @RunWith(Enclosed.class)
 public class BackGroundTest {
 
 	private static final String IMAGES_PATH = "target/classes/images/";
 
 	public static class 基本的なテスト {
 
 		private BackGround sut;
 
 		@Before
 		public void setUp() {
 			// 環境依存を減らすため、現在pathから類推
 			sut = new BackGround(IMAGES_PATH);
 		}
 
 		@Test
 		public void 時間を与えられる() {
 			sut.setTime(new Date());
 		}
 
 		@Test
 		public void プロパティが読める() {
 			assertThat(sut.getSrcImagePath(), is(notNullValue()));
 			assertThat(sut.getDestImagePath(), is(notNullValue()));
 			assertThat(sut.getBlendRatio(), is(notNullValue()));
 		}
 
 		@Test
 		public void プロパティが読み込み可能なファイルパスを返す() {
 			String path = sut.getSrcImagePath();
 			assertThat(new File(IMAGES_PATH, path).exists(), is(true));
 			path = sut.getDestImagePath();
 			assertThat(new File(IMAGES_PATH, path).exists(), is(true));
 		}
 
 	}
 
 	@RunWith(Theories.class)
 	public static class 背景4枚の時テスト {
 
 		private BackGround sut;
 
 		@Before
 		public void setUp() {
 			// 環境依存を減らすため、現在pathから類推
 			sut = new BackGround(IMAGES_PATH);
 		}
 
 		@DataPoints
 		public static Object[][] VALUES = {
 				{ 0, 0, 0, "bg01.png", "bg02.png", 0D },
 				{ 5, 59, 59, "bg01.png", "bg02.png",
 						1.0D - (1.0D / 60.0D / 60.0D / 6.0D) },
 				{ 6, 0, 0, "bg02.png", "bg03.png", 0D },
 				{ 11, 59, 59, "bg02.png", "bg03.png",
 						1.0D - (1.0D / 60.0D / 60.0D / 6.0D) },
 				{ 12, 0, 0, "bg03.png", "bg04.png", 0D },
 				{ 23, 59, 59, "bg04.png", "bg01.png",
 						1.0D - (1.0D / 60.0D / 60.0D / 6.0D) } };
 
 		@Theory
 		public void 時刻指定に応じた_適切な画像ファイルパスのペアとブレンド率を返す(Object[] values) {
 			sut.imageFileNames = new String[] { "bg01.png", "bg02.png",
 					"bg03.png", "bg04.png" };
 
 			int hour = ((Integer) values[0]).intValue();
 			int minute = ((Integer) values[1]).intValue();
 			int second = ((Integer) values[2]).intValue();
 			String bgA = (String) values[3];
 			String bgB = (String) values[4];
 			double blend = ((Double) values[5]).doubleValue();
 
 			Calendar c = Calendar.getInstance();
 			c.set(2000, 1, 1, hour, minute, second);
 
 			sut.setTime(c.getTime());
 			assertThat(sut.getSrcImagePath(), is(bgA));
 			assertThat(sut.getDestImagePath(), is(bgB));
 			assertThat(sut.getBlendRatio(), is(blend));
 		}
 	}
 
 	@RunWith(Theories.class)
 	public static class 背景6枚の時テスト {
 
 		private BackGround sut;
 
 		@Before
 		public void setUp() {
 			// 環境依存を減らすため、現在pathから類推
 			sut = new BackGround(IMAGES_PATH);
 		}
 
 		@DataPoints
 		public static Object[][] VALUES = {
 				{ 0, 0, 0, "bg01.png", "bg02.png", 0.0D },
 				{ 3, 59, 59, "bg01.png", "bg02.png",
 						1.0D - (1.0D / 60.0D / 60.0D / 4.0D) },
 				{ 4, 0, 0, "bg02.png", "bg03.png", 0.0D },
 				{ 7, 59, 59, "bg02.png", "bg03.png",
 						1.0D - (1.0D / 60.0D / 60.0D / 4.0D) },
 				{ 8, 0, 0, "bg03.png", "bg04.png", 0.0D },
 				{ 23, 59, 59, "bg06.png", "bg01.png",
 						1.0D - (1.0D / 60.0D / 60.0D / 4.0D) } };
 
 		@Theory
 		public void 時刻指定に応じた_適切な画像ファイルパスのペアとブレンド率を返す(Object[] values) {
 			sut.imageFileNames = new String[] { "bg01.png", "bg02.png",
 					"bg03.png", "bg04.png", "bg05.png", "bg06.png" };
 
 			int hour = ((Integer) values[0]).intValue();
 			int minute = ((Integer) values[1]).intValue();
 			int second = ((Integer) values[2]).intValue();
 			String bgA = (String) values[3];
 			String bgB = (String) values[4];
 			double blend = ((Double) values[5]).doubleValue();
 
 			Calendar c = Calendar.getInstance();
 			c.set(2000, 1, 1, hour, minute, second);
 
 			sut.setTime(c.getTime());
 			assertThat(sut.getSrcImagePath(), is(bgA));
 			assertThat(sut.getDestImagePath(), is(bgB));
 			assertThat(sut.getBlendRatio(), is(blend));
 		}
 	}
 
 }
