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
 
 
 
 public class SingleRotateLeft extends JApplet {
 	public static void main(String s[]) {
 		JFrame frame = new JFrame();
 		frame.setTitle("Insert");
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		JApplet applet = new SingleRotateLeft();
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
 		private GrahpicalNode node1, node2, node4, node6, node8, node10, node12;
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
 				node1 = new GrahpicalNode("1", 170, 310);
 				paintNode(node1, g2);
 				node2 = new GrahpicalNode("2", 240, 240);
 				paintNode(node2, g2);
 				node4 = new GrahpicalNode("4", 310, 170);
 				paintNodeWithColor(node4, g2, blue);
 				node6 = new GrahpicalNode("6", 355, 240);
 				paintNode(node6, g2);
 				node8 = new GrahpicalNode("8", 380, 100);
 				paintNodeWithColor(node8, g2, red);
 				node10 = new GrahpicalNode("10", 425, 170);
 				paintNode(node10, g2);
 				node12 = new GrahpicalNode("12", 450, 30);
 				paintNode(node12, g2);
 				paintLine(node2, node1, g2);
 				paintLine(node4, node2, g2);
 				paintLine(node4, node6, g2);
 				paintLine(node8, node4, g2);
 				paintLine(node8, node10, g2);
 				paintLine(node12, node8, g2);
 				j++;
				if(j >= 20) {iteration++; y = j = 0;}
 			}
 			if(iteration == 1){
 				paintNode(node1, g2);
 				paintNode(node2, g2);
 				paintNodeWithColor(node4, g2, blue);
 				paintNode(node6, g2);
 				paintNodeWithColor(node8, g2, red);
 				paintNode(node10, g2);
 				paintNode(node12, g2);
 				paintLine(node2, node1, g2);
 				paintLine(node4, node2, g2);
 				paintLine(node8, node10, g2);
 				paintLine(node12, node8, g2);
 				paintLine(node8, node4, g2);
 				if(y < 20)y++; 
 				if(j < 45)j+=2;
 				removeLine(node4, node6, g2, j, y);
 				if(y >= 20 && j >= 45){iteration++; y = j = 0;}
 			}
 			
 			if(iteration == 2){
 				paintNode(node1, g2);
 				paintNode(node2, g2);
 				paintNodeWithColor(node4, g2, blue);
 				paintNode(node6, g2);
 				paintNodeWithColor(node8, g2, red);
 				paintNode(node10, g2);
 				paintNode(node12, g2);
 				paintLine(node2, node1, g2);
 				paintLine(node4, node2, g2);
 				paintLine(node8, node10, g2);
 				paintLine(node12, node8, g2);
 				if(j < 70) j+=3;
 				if(y < 20) y++;
 				removeLine(node8, node4, g2, -j, y);
 				if(j >= 70 && y >= 20){iteration++; j = y = 0;}
 			}
 			if(iteration == 3){
 				paintNode(node1, g2);
 				paintNode(node2, g2);
 				paintNodeWithColor(node4, g2, blue);
 				paintNode(node6, g2);
 				paintNodeWithColor(node8, g2, red);
 				paintNode(node10, g2);
 				paintNode(node12, g2);
 				paintLine(node2, node1, g2);
 				paintLine(node4, node2, g2);
 				paintLine(node8, node10, g2);
 				paintLine(node12, node8, g2);
 				if(j < (node8.getBottomPoint().getX() - node6.getTopPoint().getX())) j++;
 				if(y < (node6.getTopPoint().getY() - node8.getBottomPoint().getY())) y+=3;
 				paintNewLine(-j, y, node8, g2);	
 				if(j >= (node8.getBottomPoint().getX() - node6.getTopPoint().getX()) && y >= (node6.getTopPoint().getY() - node8.getBottomPoint().getY())){iteration++; j = y = 0;}
 			}
 			
 			if(iteration == 4){
 				paintNode(node1, g2);
 				paintNode(node2, g2);
 				paintNodeWithColor(node4, g2, blue);
 				paintNode(node6, g2);
 				paintNodeWithColor(node8, g2, red);
 				paintNode(node10, g2);
 				paintNode(node12, g2);
 				paintLine(node2, node1, g2);
 				paintLine(node4, node2, g2);
 				paintLine(node8, node10, g2);
 				paintLine(node8, node6, g2);
 				if(j < (node12.getBottomPoint().getX() - node8.getTopPoint().getX())) j+=3;
 				if(y < (node8.getTopPoint().getY() - node12.getBottomPoint().getY())) y++;
 				removeLine(node12, node8, g2, -j, y);
 				if(j >= (node12.getBottomPoint().getX() - node8.getTopPoint().getX()) && y >= (node8.getTopPoint().getY() - node12.getBottomPoint().getY())){iteration++; j = y = 0;}
 			}
 			
 			if(iteration == 5){
 				paintNode(node1, g2);
 				paintNode(node2, g2);
 				paintNodeWithColor(node4, g2, blue);
 				paintNode(node6, g2);
 				paintNodeWithColor(node8, g2, red);
 				paintNode(node10, g2);
 				paintNode(node12, g2);
 				paintLine(node2, node1, g2);
 				paintLine(node4, node2, g2);
 				paintLine(node8, node10, g2);
 				paintLine(node8, node6, g2);
 				if(j < (node12.getBottomPoint().getX() - node4.getTopPoint().getX())) j+=3;
 				if(y < (node4.getTopPoint().getY() - node12.getBottomPoint().getY())) y+=2;
 				System.out.println((node12.getBottomPoint().getX() - node4.getTopPoint().getX()) + "y: " + (node4.getTopPoint().getY() - node12.getBottomPoint().getY()));
 				g2.draw(new QuadCurve2D.Double(node12.getBottomPoint().getX(), node12.getBottomPoint().getY(), node12.getBottomPoint().getX() - 2*j, node12.getBottomPoint().getY()-50, node12.getBottomPoint().getX() - j, node12.getBottomPoint().getY()+y));
 				if((j >= (node12.getBottomPoint().getX() - node4.getTopPoint().getX())) && (y >= (node4.getTopPoint().getY() - node12.getBottomPoint().getY()))) {iteration++; j = y = 0;}
 			}
 			
 			if(iteration == 6){
 				if(y < 70) y+=2;
 				node8 = new GrahpicalNode("8", 380+y/6, 100+y);
 				node10 = new GrahpicalNode("10", 425, 170+y);
 				paintNode(node1, g2);
 				paintNode(node2, g2);
 				paintNodeWithColor(node4, g2, blue);
 				paintNode(node6, g2);
 				paintNodeWithColor(node8, g2, red);
 				paintNode(node10, g2);
 				paintNode(node12, g2);
 				paintLine(node2, node1, g2);
 				paintLine(node4, node2, g2);
 				paintLine(node8, node10, g2);
 				paintLine(node8, node6, g2);
 				g2.draw(new QuadCurve2D.Double(node12.getBottomPoint().getX(), node12.getBottomPoint().getY(), node12.getBottomPoint().getX() - 280, node12.getBottomPoint().getY()-50, node4.getTopPoint().getX(),  node4.getTopPoint().getY()));
 				if(y >=70) {iteration++; j = y = 0;}
 			}
 			
 			if(iteration == 7) {
 				node8 = new GrahpicalNode("8", 391, 170);
 				node10 = new GrahpicalNode("10", 425, 240);
 				if(y < 70) y+=2;
 				if(j < 45) j++;
 				node4 = new GrahpicalNode("4", 310+j, 170-y);
 				node2 = new GrahpicalNode("2", 240+j, 240-y);
 				node1 = new GrahpicalNode("1", 170+j, 310-y);
 				paintNode(node1, g2);
 				paintNode(node2, g2);
 				paintNodeWithColor(node4, g2, blue);
 				paintNode(node6, g2);
 				paintNodeWithColor(node8, g2, red);
 				paintNode(node10, g2);
 				paintNode(node12, g2);
 				paintLine(node2, node1, g2);
 				paintLine(node4, node2, g2);
 				paintLine(node8, node10, g2);
 				paintLine(node8, node6, g2);
 				g2.draw(new QuadCurve2D.Double(node12.getBottomPoint().getX(), node12.getBottomPoint().getY(), node12.getBottomPoint().getX() - 280+j*4, node12.getBottomPoint().getY()-50+j, node4.getTopPoint().getX(),  node4.getTopPoint().getY()));
 				if(y >= 70 && j >= 45){iteration++; j = y = 0;}
 			}
 			
 			if(iteration == 8) {
 				node4 = new GrahpicalNode("4", 355, 100);
 				paintNode(node1, g2);
 				paintNode(node2, g2);
 				paintNodeWithColor(node4, g2, blue);
 				paintNode(node6, g2);
 				paintNodeWithColor(node8, g2, red);
 				paintNode(node10, g2);
 				paintNode(node12, g2);
 				paintLine(node2, node1, g2);
 				paintLine(node4, node2, g2);
 				paintLine(node8, node10, g2);
 				paintLine(node8, node6, g2);
 				g2.draw(new QuadCurve2D.Double(node12.getBottomPoint().getX(), node12.getBottomPoint().getY(), node12.getBottomPoint().getX()-10, node12.getBottomPoint().getY()-5, node4.getTopPoint().getX(),  node4.getTopPoint().getY()));
 				if(j < (node8.getTopPoint().getX() - node4.getBottomPoint().getX())) j+=2;
 				if(y < (node8.getTopPoint().getY() - node4.getBottomPoint().getY())) y++;
 				paintNewLine(j, y, node4, g2);
 				if(j >= (node8.getTopPoint().getX() - node4.getBottomPoint().getX()) && y >= (node8.getTopPoint().getY() - node4.getBottomPoint().getY())){iteration++; j = y = 0;}
 				}
 			
 			if(iteration == 9) {
				j++;
 				node4 = new GrahpicalNode("4", 355, 100);
 				paintNode(node1, g2);
 				paintNode(node2, g2);
 				paintNodeWithColor(node4, g2, blue);
 				paintNode(node6, g2);
 				paintNodeWithColor(node8, g2, red);
 				paintNode(node10, g2);
 				paintNode(node12, g2);
 				paintLine(node2, node1, g2);
 				paintLine(node4, node2, g2);
 				paintLine(node8, node10, g2);
 				paintLine(node8, node6, g2);
 				g2.draw(new QuadCurve2D.Double(node12.getBottomPoint().getX(), node12.getBottomPoint().getY(), node12.getBottomPoint().getX()-10, node12.getBottomPoint().getY()-5, node4.getTopPoint().getX(),  node4.getTopPoint().getY()));
				paintLine(node4, node8, g2);
				if(j >= 20){iteration = j = y = 0;}
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
