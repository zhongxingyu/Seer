 package screen;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import main.Main;
 
 public class GamePanel extends JPanel {
 	protected int boardWidth, boardHeight;
 	final int size = 30;
 	int beginX, beginY;
 	
 	int xpos, ypos;
 	
 	GamePanel() {
 		boardWidth = 8;
 		boardHeight = 10;
 		beginX = 160;
 		beginY = 60;
 		this.setBackground(Color.BLACK);
 		ml = new mouseListener();
 		mml = new mouseMotionListener();
 		this.setClickable(false);
 		xpos = ypos = 0;
 	}
 	
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		
 		if(Main.controller.getGameStatus()==1) {
 		
 			beginX = this.getWidth()/2 - (boardWidth+2)*size/2;
 			beginY = this.getHeight()/2 - (boardHeight+4)*size/2;
 		
 			//g.setColor(Color.BLACK);
 			//g.fillRect(0, 0, getWidth(), getHeight());
 			//g.setColor(Color.GRAY);
 			//System.out.println("w: "+Main.controller.getBoardHeight()+"h: "+Main.controller.getBoardWidth());
 	  	
 			for(int i=0; i<=Main.controller.getBoardHeight(); i++) {
 				for(int j=0; j<=Main.controller.getBoardWidth(); j++) {
 					for(int k=0; k<=3; k++) {
 						int tmp=Main.controller.boardLinks(i,j,k);
 						if(tmp==1) g.setColor(Color.BLUE);
 						else if(tmp==2) g.setColor(Color.RED);
 						else if(tmp==3) g.setColor(Color.YELLOW);
 						if(tmp!=0&&tmp!=4) {
 							if(k==0) g.drawLine(j*size+beginX, i*size+beginY, (j+1)*size+beginX, i*size+beginY);
 							else if(k==1) g.drawLine(j*size+beginX, i*size+beginY, j*size+beginX, (i+1)*size+beginY);
 							else if(k==2) g.drawLine(j*size+beginX, i*size+beginY, (j+1)*size+beginX, (i+1)*size+beginY);
 							else if(k==3) g.drawLine((j+1)*size+beginX, i*size+beginY, j*size+beginX, (i+1)*size+beginY);
 						}
 					}
 				}
 			}
 			//current position
 			g.setColor(Color.WHITE);
 			g.fillOval(beginX+Main.controller.returnPosX()*size-4, beginY+Main.controller.returnPosY()*size-4, 8, 8);
 
 			//mouse position
 			g.setColor(Color.GREEN);
 			g.fillOval(beginX+xpos*size-4, beginY+ypos*size-4, 8, 8);
 	  	}		
 	}
 	
 	class mouseListener implements MouseListener {
 		@Override
 		public void mouseClicked(MouseEvent e) {
 			int tmp = Main.controller.makeMove(xpos, ypos);
 			repaint();
 			if (tmp == 1) {				
 				JOptionPane.showMessageDialog(null, "Player 1 wins!", "Winner", JOptionPane.INFORMATION_MESSAGE);
 				Main.controller.stop();
 				MainFrame.unlockButtons();
 			}
 			else if (tmp == 2) {				
 				JOptionPane.showMessageDialog(null, "Player 2 wins!", "Winner", JOptionPane.INFORMATION_MESSAGE);
 				Main.controller.stop();
 				MainFrame.unlockButtons();
 			}			
 		}
 		@Override
 		public void mouseEntered(MouseEvent arg0) {}
 		@Override
 		public void mouseExited(MouseEvent arg0) {}
 		@Override
 		public void mousePressed(MouseEvent arg0) {}
 		@Override
 		public void mouseReleased(MouseEvent arg0) {}
 	}
 	
 	class mouseMotionListener implements MouseMotionListener {
 		@Override
 		public void mouseDragged(MouseEvent e) {}
 		@Override
 		public void mouseMoved(MouseEvent e) {
 			int x = (int) Math.round((double)(e.getX()-beginX)/size);
 			int y = (int) Math.round((double)(e.getY()-beginY)/size);
 			if (x != xpos || y != ypos) {
 				xpos = x;
 				ypos = y;
 				repaint();
 			}
 		}		
 	}
 	
 	mouseListener ml;
 	mouseMotionListener mml;
 	
 	void setClickable(boolean b) {
 		if (b == false) {
 			this.removeMouseListener(ml);
 			this.removeMouseMotionListener(mml);
 		}
 		else {
 			this.addMouseListener(ml);
 			this.addMouseMotionListener(mml);
 			repaint();			
 		}
 	}
 	
 	
 	
 }
