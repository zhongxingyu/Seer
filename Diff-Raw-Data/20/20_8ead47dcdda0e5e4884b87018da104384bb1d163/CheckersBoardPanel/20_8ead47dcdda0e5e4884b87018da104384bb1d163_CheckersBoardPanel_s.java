 package seg.project.checkers.gui;
 
 import java.awt.Component;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 import seg.project.checkers.CheckerBoard;
 import seg.project.checkers.CheckerGame;
 import seg.project.checkers.CheckerSquare;
 
 public class CheckersBoardPanel extends JPanel  implements ActionListener{
 	private CheckerSquare currentButton;
 	private CheckerBoard board;
 	private CheckersFrame frame;
 	private static final long serialVersionUID = 1L;
 	public CheckersBoardPanel(CheckersFrame frame){
 		this.frame = frame;
 		board = CheckerGame.getInstance().getBoard();
 		this.setLayout(new GridLayout(8, 8));
 		for(int i =0; i < 8;i++){
 			for(int u = 0;u<8;u++){
 				JButton temp = new JButton();
 				temp.setBorder(BorderFactory.createEmptyBorder());
 				temp.setContentAreaFilled(false);
 				temp.setBorderPainted(false);
 				temp.addActionListener(this);
 				if(i%2==0){
 					if(u%2==0)
 						temp.setIcon(new ImageIcon("data/white.png"));
 					else{
 						if(i>=5)
 							temp.setIcon(new ImageIcon("data/redpiece.png"));
 						else if(i <3){
 							temp.setIcon(new ImageIcon("data/blackpiece.png"));
 						}
 						else{
 							temp.setIcon(new ImageIcon("data/brown.png"));
 						}
 					}
 				}
 				else{
 					if(u%2 == 0){
 						if(i>=5)
 							temp.setIcon(new ImageIcon("data/redpiece.png"));
 						else if(i <3){
 							temp.setIcon(new ImageIcon("data/blackpiece.png"));
 						}
 						else{
 							temp.setIcon(new ImageIcon("data/brown.png"));
 						}
 					}
 					else	
 						temp.setIcon(new ImageIcon("data/white.png"));	
 				}
 				this.add(temp);
 			}
 		}
 	}
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if(!CheckerGame.getInstance().isTurn()){
 			CheckerGame.getInstance().addText("It is not your turn, please wait");
 			frame.updateText();
 			return;
 		}
 		if(e.getSource() instanceof JButton)
 		{
 			JButton button = (JButton) e.getSource();
 			int [] pos = findLoc(button);
 			CheckerSquare square = board.getGrid()[pos[0]][pos[1]];
 			if(currentButton == null){					
 				if(square != null&& board.performMove(pos[0], pos[1], pos[0], pos[1], CheckerGame.getInstance().isBlack())){
 					updateSquare(pos[0],pos[1]);
 					currentButton = square;
 				}
 				else{
 					CheckerGame.getInstance().addText("The place: X-" + pos[0] + ", Y-" + pos[1]+ " is not a valid piece");
 					frame.updateText();
 				}
 			}
 			else{
 				if(board.performMove(currentButton.getxPos(),currentButton.getyPos(), pos[0], pos[1], CheckerGame.getInstance().isBlack())){
 					updateSquare(pos[0],pos[1]);
					updateSquare(currentButton.getxPos(),currentButton.getyPos());
					if(!(currentButton.getxPos() == pos[0]&& currentButton.getyPos()== pos[1])){
 						CheckerGame.getInstance().setTurn(false);
						CheckerGame.getInstance().sendCommand("move:"+currentButton.getxPos()+":"+currentButton.getyPos()+":"+pos[0]+":"+pos[1]);
 					}
 					currentButton=null;
 				}
 				else{
 					CheckerGame.getInstance().addText("The move: X-" + pos[0] + ", Y-" + pos[1]+ " is not a valid move");
 					frame.updateText();
 				}
 			}
 		}	
 	}
 	public void updateSquare(int x, int y){
 		Component comp = this.getComponent(x*8+y);
 		if(comp instanceof JButton){
 			JButton button = (JButton) comp;
 			CheckerSquare square = board.getGrid()[x][y];
 			if(square == null)
 				button.setIcon(getSquareImage(x,y));
 			else
 				button.setIcon(new ImageIcon(square.getImage()));
 			frame.repaint();
 		}
 	}
 	public void reDraw(){
 		CheckerSquare[][] squareGrid = board.getGrid();
 		this.removeAll();
 		this.setLayout(new GridLayout(8, 8));
 		for(int i =0; i < 8;i++){
 			for(int u = 0;u<8;u++){
 				JButton temp = new JButton();
 				temp.setBorder(BorderFactory.createEmptyBorder());
 				temp.setContentAreaFilled(false);
 				temp.setBorderPainted(false);
 				temp.addActionListener(this);
 				if(squareGrid[i][u] != null)
 					temp.setIcon(new ImageIcon(squareGrid[i][u].getImage()));
 				else
 					temp.setIcon(getSquareImage(i,u));
 				add(temp);
 			}
 		}
 		this.validate();
 	}
 	private ImageIcon getSquareImage(int x, int y){
 		if(x%2==0){
 			if(y%2==0)
 				return(new ImageIcon("data/white.png"));
 			else
 				return (new ImageIcon("data/brown.png"));
 		}
 		else{
 			if(y%2 == 0)
 				return (new ImageIcon("data/brown.png"));
 			else
 				return(new ImageIcon("data/white.png"));
 		}	 
 	}
 	private int[] findLoc(JButton button){
 		int [] ret=null;
 		Component[] comps = this.getComponents();
 		for(int i = 0; i < comps.length;i++){
 			if(comps[i].equals(button)){
 				ret = new int[2];
 				ret[0]=i/8;
 				ret[1]=i%8;
 				break;
 			}
 		}
 		return ret;
 	}
 }
