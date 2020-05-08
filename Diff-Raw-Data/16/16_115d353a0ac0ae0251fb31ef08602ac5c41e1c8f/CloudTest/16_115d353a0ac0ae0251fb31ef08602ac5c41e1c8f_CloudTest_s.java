package tk.bnbm.clockdrive4j.model;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.not;
 import static org.hamcrest.MatcherAssert.assertThat;
 
 import java.awt.geom.Point2D;
 import java.awt.geom.Point2D.Double;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Cloudのテストクラス。
  *
  * @author kazuhito_m
  */
 public class CloudTest {
 
 	private Cloud sut;
 
 	@Before
 	public void setUp() {
 		sut = new Cloud(512, 512, 10);
 	}
 
 	@Test
 	public void 雲はすべてバラバラの位置() {
 		Point2D.Double prev = null;
 		for (Point2D.Double now : sut.getPositions()) {
 			if (prev != null) {
 				assertThat(now, is(not(prev)));
 			}
 			prev = now;
 		}
 	}
 
 	@Test
 	public void 徐々に移動していく() {
 		Point2D.Double prev = (Double) sut.getPositions().get(0).clone();
 		sut.move(1);
 		Point2D.Double now = sut.getPositions().get(0);
 		assertThat(now, is(not(prev)));
 	}
 }
