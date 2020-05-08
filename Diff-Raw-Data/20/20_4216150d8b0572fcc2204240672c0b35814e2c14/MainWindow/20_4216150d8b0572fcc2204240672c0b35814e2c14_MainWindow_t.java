 package dans.controller;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import javax.swing.JFrame;
 
 import dans.model.Road;
 import dans.model.RoadStatus;
 import dans.model.Warning;
 import dans.view.Sign;
 
 public class MainWindow {
 	
 	private JFrame frame;
 	private Sign sign;
 	private Road road;
 	private Warning warning;
 	
 	public MainWindow() {
 		frame = new JFrame("DANS - DIGITEK's Avalanche Notification System");
		frame.setUndecorated(true);
 		sign = new Sign();
 		road = new Road("E6", "Dovrefjell", RoadStatus.OPEN);
		warning = new Warning(road, new String[]{"Vind: 2.7m/s", "Lav skredfare"});
 		sign.setWarning(warning);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.add(sign);
		frame.setSize(640, 252);
//		frame.pack();
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 		frame.addKeyListener(listener);
 	}
 	
 	public static void main(String[] args) {
 		new MainWindow();
 	}
 	
 	KeyListener listener = new KeyListener() {
 		
 		@Override
 		public void keyTyped(KeyEvent e) {
 			switch (Character.toUpperCase(e.getKeyChar())) {
 			case '+': sign.setCapacity(sign.getCapacity()+1); break;
 			case '-': sign.setCapacity(sign.getCapacity()-1); break;
 			case ' ': sign.setCapacity(2); break;
 			case 'X': road.setStatus(RoadStatus.CLOSED); break;
 			case 'O': road.setStatus(RoadStatus.OPEN); break;
 			case 'C': road.setStatus(RoadStatus.CONVOY); break;
 			case 'S': 
 				road.setStatus(RoadStatus.CLOSED);
				warning.addWarning("Omkjøring via &lt;vei&gt;", 0);
//				warning.addWarning("", 0);
 				break;
 			case 'A': warning.removeWarning(0); break;
 			}
 		}	
 		@Override
 		public void keyReleased(KeyEvent e) {}		
 		@Override
 		public void keyPressed(KeyEvent e) {}
 	};
 
 }
