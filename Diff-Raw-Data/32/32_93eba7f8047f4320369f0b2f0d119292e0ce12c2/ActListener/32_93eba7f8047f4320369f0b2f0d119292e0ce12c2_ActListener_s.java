 package listeners;
 
 import imgMain.JxReader;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 public class ActListener implements ActionListener, MouseListener {
 
 	private static JxReader jxReader;
 
 	public ActListener(JxReader reader) {
 		jxReader = reader;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
		// System.out.println(e.getActionCommand());
 		String s = e.getActionCommand();
 
 		if (s.equals("Exit")) {
 			jxReader.exit();
 		} else if (s.equals("Fullscreen")) {
 			jxReader.fullScreen();
 		} else if (s.equals("Open Archive")) {
 			jxReader.archiveChooser();
 		} else if (s.equals("Open Folder")) {
 			jxReader.folderChooser();
 		} else if (s.equals("Author")) {
 			jxReader.InfoBox("Author: Koro\nVersion: " + JxReader.VERSION, "About"); // Date last modified?
 		} else if (s.equals("Program")) {
 			jxReader.InfoBox(
 					"JxReader is a cross-platform port of my VB.NET application JReader.\n\n"
 							+ "Both programs are designed to load image files into ram and display them at full size.\n\n"
 							+ "Unfortunately, due to the way JVM interacts with your Operating System, JxReader "
 							+ "appears to use significantly more RAM than it really does.",
 					"About");// Replace with .txt file
 		} else if (s.equals("Hotkeys")) {
 			jxReader.InfoBox("Toggle fullscreen: F"
 					+ "\n"
 					+ // Arrows, comma, period, archive, folder
 					"Toggle always on top: A" + "\n"
 					+ "Toggle hide menu bar: H" + "\n" + "Open folder: O"
 					+ "\n" + "Open archive: Z" + "\n" + "Exit: X" + "\n",
 					"Hotkeys"); // Overload MsgBox to get title
 		} else if (s.equals("Primary")) {
 			jxReader.setScreen(0);
 		} else if (s.equals("Secondary")) {
 			jxReader.setScreen(1);
 		} else if (s.equals("Minimize")) {
 			jxReader.getFrame().setState(JFrame.ICONIFIED);
 		} else if (s.equals("Auto") || s.endsWith("%")) {
 			jxReader.flickCount();
 		} // Auto, %
 
 		// jxReader.repaint();
 
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		Object c = e.getSource();
 
 		if (e.getSource() instanceof JLabel) {
 			JLabel j = (JLabel) c;
 			String s = j.getName();
 
 			if (s.equals("Exit")) {
 				jxReader.exit();
 			} else if (s.equals("Fullscreen")) {
 				jxReader.fullScreen();
 			} else if (s.equals("Minimize")) {
 				jxReader.getFrame().setState(JFrame.ICONIFIED);
 			}
 
 		}
 
 	}
 
 	@Override
	public void mouseEntered(MouseEvent arg0) {
	}
 
 	@Override
	public void mouseExited(MouseEvent arg0) {
	}
 
 	@Override
	public void mousePressed(MouseEvent arg0) {
	}
 
 	@Override
	public void mouseReleased(MouseEvent arg0) {
	}
 
 }
