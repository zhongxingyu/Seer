 package me.rainoshockzz.devconsole;
 
 import javax.swing.JFrame;
 
 public class Window extends JFrame {
 	
 	private static final long serialVersionUID = 1L;
 
 	public Window() {
		this.setTitle(DevConsole.NAME);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		this.setSize(800, 600);
		this.setVisible(true);
		this.setLocationRelativeTo(null);
 	}
 
 }
