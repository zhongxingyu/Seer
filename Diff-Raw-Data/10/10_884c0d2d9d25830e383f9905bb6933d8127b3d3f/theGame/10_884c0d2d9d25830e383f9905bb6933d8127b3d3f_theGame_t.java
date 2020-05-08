 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.util.*;
 import java.util.Timer;
 
 import javax.swing.*;
 
 
 public class theGame extends JFrame{
 	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 	private Dimension minSize = Toolkit.getDefaultToolkit().getScreenSize();
	private boolean paused = true;
 	private JButton pause;
 	private Timer time = new Timer();
 	RectangleSpace space = new RectangleSpace();	
 	
 	public theGame(){
 		setTitle("Brick Breaker Prototype");
 		setResizable(false);
 		setUndecorated(true);
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		Container Content = getContentPane();
 		Content.setLayout(new BorderLayout());
 		Content.add(space, BorderLayout.CENTER);
 		
 		
 		JMenuBar menubar = new JMenuBar();
 		
 		JMenu game = new JMenu("Game");
 		
		pause = new JButton("Start");
 		pause.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e){
 				if(!paused){
 					pause();
 					pause.setText("Unpause");
 				}
 				else{
 					unPause();
 					pause.setText("Pause");
 				}
 			}
 		});
 		
 		JMenuItem newGame = new JMenuItem("New Game");
 		newGame.setToolTipText("Create a new game");
 		newGame.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e){
 				newGame();
 			}
 		});
 		
 		JMenuItem exit = new JMenuItem("Exit");
 		exit.setToolTipText("Exit game");
 		exit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e){
 				System.exit(0);
 			}
 		});
 		
 		game.add(newGame);
 		game.add(exit);
 			
 		menubar.add(game);
 		menubar.add(pause);
 		setJMenuBar(menubar);
 		pack();
 	}
 
	public static void main(String[] args){;
 		JFrame game = new theGame();
 		game.setVisible(true);
 
 	}
 	
 	private void pause(){
 		paused = true;
 		space.pause();
 	}
 	
 	private void newGame(){}
 	
 	private void unPause(){
 		paused = false;
 		space.unPause();
 	}
 	
 	public Dimension getMinimumSize() {
         return minSize;
     }
  
     public Dimension getPreferredSize() {
         return minSize;
     }
 
 }
