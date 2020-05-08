 package danmw3.games.blocker;
 
 import java.awt.FontFormatException;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 
 import javax.swing.JButton;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 
 import org.lwjgl.opengl.Display;
 
 public class BtnTesting extends JButton implements ActionListener {
 
 	FrameTesting frameTesting;
 	Blocker app;
 
 	String[] cmds = { "exit" };
 
 	public BtnTesting(String text, int sizeX, int sizeY, int locX, int locY) {
 		super(text);
 		addActionListener(this);
 		setSize(sizeX, sizeY); // 120 40
 		setLocation(locX, locY); // 50 50
 	}
	
 	@Override
 	public void actionPerformed(ActionEvent event) {
 		String btn = event.getActionCommand();
 		if (btn.equals("Start Game")) {
 			Blocker app = new Blocker();
 			Blocker.initDisplay(false);
 			Blocker.initGL();
 			try {
 				app.run();
 			} catch (FontFormatException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} else if (btn.equals("Exit Game")) {
 			Display.destroy();
 			System.exit(0);
 		} else if (btn.equals("Send")) {
 			String cmd = FrameTesting.cmdInput.getText();
 			for (String s : cmds) {
 				if (cmd.startsWith("!" + cmds[0])) {
 					append("Closing Game");
 					Display.destroy();
 					System.exit(0);
 				} else {
 					System.out.println("fsdbggb");
 				}
 			}
 		}
 	}
 
 	public void append(String s) {
 		try {
 			Document doc = FrameTesting.consoleOutput.getDocument();
 			doc.insertString(doc.getLength(), s + "\n", null);
 		} catch (BadLocationException exc) {
 			exc.printStackTrace();
 		}
 	}
 }
