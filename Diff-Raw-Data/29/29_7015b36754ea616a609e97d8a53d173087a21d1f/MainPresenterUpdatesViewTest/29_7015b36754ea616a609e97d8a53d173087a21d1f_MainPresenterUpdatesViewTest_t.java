 package net.todd.games.balls.client.ui;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import net.todd.common.uitools.IListener;
 import net.todd.games.balls.common.Ball;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class MainPresenterUpdatesViewTest {
 	private MockView mockView;
 	private MockModel mockModel;
 
 	@Before
 	public void setUp() {
 		mockView = new MockView();
 		mockModel = new MockModel();
 	}

 	@Test
 	public void testPresenterUpdatesBallsInView() {
 		new MainPresenter(mockView, mockModel);

 		mockModel.ballData = new Ball[3];
 		mockModel.ballData[0] = new Ball();
 		mockModel.ballData[0].setColorRed(255);
 		mockModel.ballData[0].setPositionX(0);
 		mockModel.ballData[0].setPositionY(0);
 
 		mockModel.ballData[1] = new Ball();
 		mockModel.ballData[1].setColorBlue(255);
 		mockModel.ballData[1].setPositionX(1);
 		mockModel.ballData[1].setPositionY(2);
 
 		mockModel.ballData[2] = new Ball();
 		mockModel.ballData[2].setColorGreen(255);
 		mockModel.ballData[2].setPositionX(2);
 		mockModel.ballData[2].setPositionY(3);
 
 		assertNull(mockView.ballData);
 		mockModel.listener.fireEvent();
 		assertNotNull(mockView.ballData);
 		assertEquals(255, mockView.ballData[0].getColorRed());
		assertEquals(0.0f, mockView.ballData[0].getPositionX());
		assertEquals(0.0f, mockView.ballData[0].getPositionY());
 
 		assertEquals(255, mockView.ballData[1].getColorBlue());
		assertEquals(1.0f, mockView.ballData[1].getPositionX());
		assertEquals(2.0f, mockView.ballData[1].getPositionY());
 
 		assertEquals(255, mockView.ballData[2].getColorGreen());
		assertEquals(2.0f, mockView.ballData[2].getPositionX());
		assertEquals(3.0f, mockView.ballData[2].getPositionY());
 	}
 
 	private static class MockView extends AbstractMainView {
 		private Ball[] ballData;
 
 		@Override
 		public void setBallPositions(Ball[] ballData) {
 			this.ballData = ballData;
 		}
 
 		@Override
 		public void showView() {
 		}
 	}

 	private static class MockModel extends AbstractMainModel {
 		private String ballPositionText;
 		private Ball[] ballData;
 		private IListener listener;
 
 		@Override
 		public Ball[] getBallData() {
 			return ballData;
 		}

 		@Override
 		public void addModelListener(IListener listener) {
 			this.listener = listener;
 		}

 		@Override
 		public String getBallPositionText() {
 			return ballPositionText;
 		}
 	}
 }
