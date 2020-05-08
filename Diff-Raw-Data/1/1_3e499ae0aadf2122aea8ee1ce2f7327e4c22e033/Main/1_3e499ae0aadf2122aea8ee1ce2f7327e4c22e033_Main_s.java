 package ru.skupriyanov.src;
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Random;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.SwingWorker;
 
 import com.sun.jna.NativeLibrary;
 
 public class Main {
 
 	private static final int SLEEPING_TIME_MILLIS = 10000;
 	private static final NativeLibrary USER32_LIBRARY_INSTANCE = NativeLibrary
 			.getInstance("user32");
 
 	private static JButton startButton;
 	private static JButton stopButton;
 	private static boolean continueCycle = true;
 
 	public static void main(String[] args) {
 		drawFrame();
 	}
 
 	private static void drawFrame() {
 		final JFrame frame = new JFrame("Cursor mover");
 		frame.setSize(200, 100);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setLocationRelativeTo(null);
 
 		JPanel panel = new JPanel();
 
 		startButton = new JButton("Start");
 		stopButton = new JButton("Stop");
 
 		panel.add(startButton);
 		panel.add(stopButton);
 		frame.add(panel);
 
 		final CursorController cursorController = new CursorController();
 		startButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				cursorController.execute();
 			}
 		});
 
 		stopButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				continueCycle = false;
 			}
 		});
 
 		frame.setVisible(true);
 	}
 
 	private static class CursorController extends SwingWorker<Void, Void> {
 
 		@Override
 		protected Void doInBackground() throws Exception {
 			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 			Random randomX = new Random();
 			Random randomY = new Random();
 			while (continueCycle) {
 				Object[] callArguments = {
 						randomX.nextInt(screenSize.width - 1),
 						randomY.nextInt(screenSize.height - 1) };
 				USER32_LIBRARY_INSTANCE.getFunction("SetCursorPos").invoke(
 						callArguments);
 				Thread.sleep(SLEEPING_TIME_MILLIS);
 			}
 			return null;
 		}
 
 	}
 
 }
