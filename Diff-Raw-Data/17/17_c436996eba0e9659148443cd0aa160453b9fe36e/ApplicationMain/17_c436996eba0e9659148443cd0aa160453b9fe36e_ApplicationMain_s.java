 package rltut;
 
 import javax.swing.JFrame;
 import asciiPanel.AsciiPanel;
 
 public class ApplicationMain extends JFrame {
     private static final long serialVersionUID = 1060623638149583738L;
 
     private AsciiPanel terminal;
 
     public ApplicationMain() {
	super();
	terminal = new AsciiPanel();
	terminal.write("rl tutorial", 1, 1);
	add(terminal);
	pack();
     }
 
     public static void main(String[] args) {
	ApplicationMain app = new ApplicationMain();
	app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	app.setVisible(true);
     }
 }
