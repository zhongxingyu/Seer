 package viewdemo;
 
 import java.awt.MouseInfo;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JFrame;
 import javax.swing.Timer;
 
 public class ViewDemo {
 	public static void main(String[] args) {
 		new ViewDemo();
 	}

 	JFrame testFrame = null;

 	public ViewDemo() {
 		System.out.println("Space is risky shiet");
 		System.out.println("Oh yea");
 		testFrame = new JFrame();
 		TestPanel testPanel = new TestPanel();
 		testFrame.add(testPanel);
 		testFrame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
 		testFrame.setVisible(true);
 		testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		testFrame.setIgnoreRepaint(true);
		Timer timer = new Timer(1000 / 60, new MoveListener());
 		timer.start();
 	}

 	private class MoveListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent event) {
 			Point loc = MouseInfo.getPointerInfo().getLocation();
			if (loc.x == Toolkit.getDefaultToolkit().getScreenSize().getWidth() - 1) {
				TestPanel.camera.x = Math.min(TestPanel.squareSize* 24 - (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth(), TestPanel.camera.x + 4);
 			} else if (loc.x == 0) {
 				TestPanel.camera.x = Math.max(0, TestPanel.camera.x - 4);
 			}
			if (loc.y == Toolkit.getDefaultToolkit().getScreenSize()
					.getHeight() - 1) {
				TestPanel.camera.y = Math.min(TestPanel.squareSize * 23 - (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight(),
						TestPanel.camera.y + 4);
 			} else if (loc.y == 0) {
 				TestPanel.camera.y = Math.max(0, TestPanel.camera.y - 4);
 			}
 			testFrame.repaint();
 		}
 	}
 }
