 package animations;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.geom.QuadCurve2D;
 
 import javax.swing.JApplet;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 
 
 public class DoubleRotationLeft extends JApplet {
 	public static void main(String s[]) {
 		JFrame frame = new JFrame();
 		frame.setTitle("Insert");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		JApplet applet = new DoubleRotationLeft();
 		applet.init();
 		frame.getContentPane().add(applet);
 		frame.pack();
 		frame.setVisible(true);
 	}
 
 	public void init(){
 		getContentPane().add(new DrawingPanel());
 	}
 
 	private class DrawingPanel extends AbstractDrawingPanel{
 		private Thread thread;
 		Color red = Color.RED, green = Color.GREEN, blue = Color.BLUE;
 
 		private JButton button, stopButton, playButton;
 		private GrahpicalNode node1, node2, node4, node5, node6, node8, node10, node12;
 		private int iteration, maxIteration = 9, y = 0, j = 0;
 		public DrawingPanel(){
 			this.setLayout(new BorderLayout());
 			button = new JButton("Next");
 			button.addActionListener(new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					if(iteration <maxIteration){iteration++; j = y = 0;}
 					else if(iteration >= maxIteration) {iteration = j = y = 0;}
 					start(); 
 					System.out.println(iteration);
 					repaint();
 				}
 			});
 
 			playButton = new JButton("Play");
 			playButton.addActionListener(new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					start();
 
 				}
 			});
 
 			stopButton = new JButton("Pause");
 			stopButton.addActionListener(new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					stop();
 
 				}
 			});
 			JPanel southButtonPanel = new JPanel();
 			southButtonPanel.setLayout(new FlowLayout());
 			southButtonPanel.add(stopButton);
 			southButtonPanel.add(playButton);
 			southButtonPanel.add(button);
 			this.add(southButtonPanel, BorderLayout.SOUTH);
 			iteration = 0;
 		}
 
 
 
 
 		public void paintComponent(Graphics g){
 			super.paintComponents(g);
 			Graphics2D g2 = (Graphics2D) g;
 			g2.clearRect(0, 0, getWidth(), getHeight());
 			if(iteration == 0){
 				node2 = new GrahpicalNode("2", 240, 240);
 				paintNode(node2, g2);
 				node4 = new GrahpicalNode("4", 310, 170);
 				paintNodeWithColor(node4, g2, blue);
 				node5 = new GrahpicalNode("5", 325, 310);
 				paintNodeWithColor(node5, g2, green);
 				node6 = new GrahpicalNode("6", 355, 240);
 				paintNode(node6, g2);
 				node8 = new GrahpicalNode("8", 380, 100);
 				paintNodeWithColor(node8, g2, red);
 				node10 = new GrahpicalNode("10", 425, 170);
 				paintNode(node10, g2);
 				node12 = new GrahpicalNode("12", 450, 30);
 				paintNode(node12, g2);
 				paintLine(node6, node5, g2);
 				paintLine(node4, node2, g2);
 				paintLine(node4, node6, g2);
 				paintLine(node8, node4, g2);
 				paintLine(node8, node10, g2);
 				paintLine(node12, node8, g2);
 				j++;
 				if(j >= 30) {iteration++; y = j = 0;}
 			}
 			
 			
 
 			if(iteration == 1){
 				paintNode(node2, g2);
 				paintNodeWithColor(node4, g2, blue);
 				paintNodeWithColor(node5, g2, green);
 				paintNode(node6, g2);
 				paintNodeWithColor(node8, g2, red);
 				paintNode(node10, g2);
 				paintNode(node12, g2);
 				paintLine(node6, node5, g2);
 				paintLine(node4, node2, g2);
 				paintLine(node8, node4, g2);
 				paintLine(node8, node10, g2);
 				paintLine(node12, node8, g2);
 				if(j < node6.getTopPoint().getX()-node4.getBottomPoint().getX()) j+=2;
 				if(y < node6.getTopPoint().getY() - node4.getBottomPoint().getY()) y++;
 				removeLine(node4, node6, g2, j, y);
 				if(j >= node6.getTopPoint().getX()-node4.getBottomPoint().getX() && y >= node6.getTopPoint().getY() - node4.getBottomPoint().getY()){iteration++; y = j = 0;}
 			}
 
 			if(iteration == 2){
 				paintNode(node2, g2);
 				paintNodeWithColor(node4, g2, blue);
 				paintNodeWithColor(node5, g2, green);
 				paintNode(node6, g2);
 				paintNodeWithColor(node8, g2, red);
 				paintNode(node10, g2);
 				paintNode(node12, g2);
 				paintLine(node4, node2, g2);
 				paintLine(node8, node4, g2);
 				paintLine(node8, node10, g2);
 				paintLine(node12, node8, g2);
 				paintLine(node6, node5, g2);
 				if(j < node5.getTopPoint().getX() - node4.getBottomPoint().getX()) j++;
 				if(y < node5.getTopPoint().getY() - node4.getBottomPoint().getY()) y+=6;
 				paintNewLine(j, y, node4, g2);
 				if(j >= node5.getTopPoint().getX() - node4.getBottomPoint().getX() && y >= node5.getTopPoint().getY() - node4.getBottomPoint().getY()){iteration++; y = j = 0;}
 			}
 			
 			if(iteration == 3){
 				paintNode(node2, g2);
 				paintNodeWithColor(node4, g2, blue);
 				paintNodeWithColor(node5, g2, green);
 				paintNode(node6, g2);
 				paintNodeWithColor(node8, g2, red);
 				paintNode(node10, g2);
 				paintNode(node12, g2);
 				paintLine(node4, node2, g2);
 				paintLine(node4, node5, g2);
 				paintLine(node8, node4, g2);
 				paintLine(node8, node10, g2);
 				paintLine(node12, node8, g2);
 				if(j < node6.getBottomPoint().getX() - node5.getTopPoint().getX()) j++;
 				if(y < node5.getTopPoint().getY() - node6.getBottomPoint().getY()) y++;
 				removeLine(node6, node5, g2, -j, y);
 				if(j >= node6.getBottomPoint().getX() - node5.getTopPoint().getX() && y >= node5.getTopPoint().getY() - node6.getBottomPoint().getY()) {iteration++; y = j = 0;}
 			}
 			
 			if(iteration == 4){
 				if(j < 35) j++;
 				if(y < 70) y+=2;
 				node4 = new GrahpicalNode("4", 310-j, 170+y);
 				node2 = new GrahpicalNode("2", 240-j, 240+y);
 				paintNode(node2, g2);
 				paintNodeWithColor(node4, g2, blue);
 				paintNodeWithColor(node5, g2, green);
 				paintNode(node6, g2);
 				paintNodeWithColor(node8, g2, red);
 				paintNode(node10, g2);
 				paintNode(node12, g2);
 				paintLine(node4, node2, g2);
 				paintLine(node4, node5, g2);
 				paintLine(node8, node4, g2);
 				paintLine(node8, node10, g2);
 				paintLine(node12, node8, g2);
 				if(j>=35&&y>=70){iteration++; y = j = 0;}
 			}
 			
 			if(iteration == 5){
 				node4 = new GrahpicalNode("4", 275, 240);
 				node2 = new GrahpicalNode("2", 205, 310);
 				if(j < 35) j++;
 				if(y < 70) y+=2;
 				node6 = new GrahpicalNode("6", 355-j, 240-y);
 				paintNode(node2, g2);
 				paintNodeWithColor(node4, g2, blue);
 				paintNodeWithColor(node5, g2, green);
 				paintNode(node6, g2);
 				paintNodeWithColor(node8, g2, red);
 				paintNode(node10, g2);
 				paintNode(node12, g2);
 				paintLine(node4, node2, g2);
 				paintLine(node4, node5, g2);
 				paintLine(node8, node4, g2);
 				paintLine(node8, node10, g2);
 				paintLine(node12, node8, g2);
 				if(j>=35&&y>=70){iteration++; y = j = 0;}
 			}
 			
 			if(iteration == 6){
 				node6 = new GrahpicalNode("6", 320, 170);
 				paintNode(node2, g2);
 				paintNodeWithColor(node4, g2, blue);
 				paintNodeWithColor(node5, g2, green);
 				paintNode(node6, g2);
 				paintNodeWithColor(node8, g2, red);
 				paintNode(node10, g2);
 				paintNode(node12, g2);
 				paintLine(node4, node2, g2);
 				paintLine(node4, node5, g2);
 				paintLine(node8, node4, g2);
 				paintLine(node8, node10, g2);
 				paintLine(node12, node8, g2);
 				if(j < node6.getBottomPoint().getX() - node4.getTopPoint().getX()) j+=2;
 				if(y < node4.getTopPoint().getY() - node6.getBottomPoint().getY()) y++;
 				paintNewLine(-j, y, node6, g2);
 				if(j >= node6.getBottomPoint().getX() - node4.getTopPoint().getX()&&y >= node4.getTopPoint().getY() - node6.getBottomPoint().getY()){iteration++; y = j = 0;}
 			}
 				
 			if(iteration == 7){
 				paintNode(node2, g2);
 				paintNodeWithColor(node4, g2, blue);
 				paintNodeWithColor(node5, g2, green);
 				paintNode(node6, g2);
 				paintNodeWithColor(node8, g2, red);
 				paintNode(node10, g2);
 				paintNode(node12, g2);
 				paintLine(node4, node2, g2);
 				paintLine(node4, node5, g2);
 				paintLine(node6, node4, g2);
 				paintLine(node8, node10, g2);
 				paintLine(node12, node8, g2);
 				if(j < node8.getBottomPoint().getX() - node4.getTopPoint().getX()) j+=3;
 				if(y < node4.getTopPoint().getY() - node8.getBottomPoint().getY()) y+=3;
 				removeLine(node8, node4, g2, -j, y);
 				if(j >= node8.getBottomPoint().getX() - node4.getTopPoint().getX() && y >= node4.getTopPoint().getY() - node8.getBottomPoint().getY()) {iteration++; y = j = 0;}
 			}
 			
 			if(iteration == 8){
 				paintNode(node2, g2);
 				paintNodeWithColor(node4, g2, blue);
 				paintNodeWithColor(node5, g2, green);
 				paintNode(node6, g2);
 				paintNodeWithColor(node8, g2, red);
 				paintNode(node10, g2);
 				paintNode(node12, g2);
 				paintLine(node4, node2, g2);
 				paintLine(node4, node5, g2);
 				paintLine(node6, node4, g2);
 				paintLine(node8, node10, g2);
 				paintLine(node12, node8, g2);
 				if(j < node8.getBottomPoint().getX() - node6.getTopPoint().getX()) j+=3;
 				if(y < node6.getTopPoint().getY() - node8.getBottomPoint().getY()) y++;
 				paintNewLine(-j, y, node8, g2);
 				if(j >= node8.getBottomPoint().getX() - node6.getTopPoint().getX() && y >= node6.getTopPoint().getY() - node8.getBottomPoint().getY()) {iteration++; y = j = 0;}
 			}
 			
			if(iteration == 8){
 				paintNode(node2, g2);
 				paintNodeWithColor(node4, g2, blue);
 				paintNodeWithColor(node5, g2, green);
 				paintNode(node6, g2);
 				paintNodeWithColor(node8, g2, red);
 				paintNode(node10, g2);
 				paintNode(node12, g2);
 				paintLine(node4, node2, g2);
 				paintLine(node4, node5, g2);
 				paintLine(node6, node4, g2);
 				paintLine(node8, node10, g2);
 				paintLine(node12, node8, g2);
 				paintLine(node8, node6, g2);
 			}
 			
 
 		}
 
 		public void stop() {
 			if(thread != null) thread = null;
 		}
 
 		public void start() {
 
 			if (thread == null) {
 				thread = new Thread(this);
 				thread.start();
 			}
 		}
 
 		@Override
 		public void run() {
 			while (thread != null) {
 				System.out.println(iteration);
 				try{
 					Thread.sleep(100);
 				}
 				catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				repaint();
 			}
 		}
 	}
 }
