 /**
  * Program: Ch16Prog1.java
  * Programmer: Andrew Buskov
  * Class: CIS 249
  * Date: Feb 16, 2013
  * Purpose: Write a program that moves the ball in a panel. You should define
  * a panel class for displaying the ball and provide the methods for moving the ball
  * left, right, up, and down, as shown in Figure 16.21b. Check the boundary to prevent
  * the ball from moving out of sight completely.
  */
 
 package Ch16.Prog1;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 public class Ch16Prog1 extends JFrame {
 	
 	private static final long serialVersionUID = 1L; // eclipse complains
 	private static final int PANEL_WIDTH = 400;
 	private static final int PANEL_HEIGHT = 300;
 	protected static int sizeX = 20;
 	protected static int sizeY = 20;
	protected static int x = 350;
	protected static int y = 200;
 	
 	public Ch16Prog1(){
 		final CreateOval myOval = new CreateOval();
 		
 		this.setLayout(new BorderLayout());
 		
 		// button panel
 		JPanel buttonPanel = new JPanel(new FlowLayout());
 		
 		JButton jbtnLeft = new JButton("Left");
 		JButton jbtnRight = new JButton("Right");
 		JButton jbtnUp = new JButton("Up");
 		JButton jbtnDown= new JButton("Down");
 		
 		jbtnLeft.addActionListener(new ActionListener(){
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
				if (x <= 0){
 					x = 0;
 					repaint();
 					JOptionPane.showMessageDialog(null, "You cannot move the oval off the frame");
 				}
 				else{
 					x = x - 10;
 					repaint();
 				}
 			}
 		});
 
 		jbtnRight.addActionListener(new ActionListener(){
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (x + 2*sizeX >= myOval.getWidth()){
 					x = myOval.getWidth() - sizeX - 1;
 					repaint();
 					JOptionPane.showMessageDialog(null, "You cannot move the oval off the frame");
 				}
 				else{
 					x = x + 10;
 					repaint();
 				}
 			}
 		});
 
 		jbtnUp.addActionListener(new ActionListener(){
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
				if (y <= 0){
 					y = 0;
 					repaint();
 					JOptionPane.showMessageDialog(null, "You cannot move the oval off the frame");
 				}
 				else{
 					y = y - 10;
 					repaint();
 				}
 			}
 		});
 
 		jbtnDown.addActionListener(new ActionListener(){
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (y + 2*sizeY >= myOval.getHeight()){
 					y = myOval.getHeight() - sizeY - 1;
 					repaint();
 					JOptionPane.showMessageDialog(null, "You cannot move the oval off the frame");
 				}
 				else{
 					y = y + 10;
 					repaint();
 				}
 			}
 		});
 
 		buttonPanel.setLayout(new FlowLayout());
 		
 		buttonPanel.add(jbtnLeft);
 		buttonPanel.add(jbtnRight);
 		buttonPanel.add(jbtnUp);
 		buttonPanel.add(jbtnDown);
 		
 		this.add(myOval, BorderLayout.CENTER);
 		this.add(buttonPanel, BorderLayout.SOUTH);
 		
 	}
 	
 	static class CreateOval extends JComponent {
 		
 		private static final long serialVersionUID = 1L; // eclipse complains
 
 		public void paintComponent(Graphics g){
 			super.paintComponent(g);
 			g.setColor(Color.BLACK);
 			g.drawOval(x, y, sizeX, sizeY);
 		}
 	}
 	
 	public static void main(String[]args){
 		Ch16Prog1 frame = new Ch16Prog1();
 		frame.setTitle("Chapter 16 Program 1");
 		frame.setSize(PANEL_WIDTH,PANEL_HEIGHT);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 	}
 }
