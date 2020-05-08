 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Vector;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 
 
 public class GameOfLife extends JFrame {
 	private Grid grid;
 	private Engine engine;
 	private int coreN = 4;
 	private boolean running = false;
 	private boolean finish = true;
 	private int nButtons = 5;
 	private Integer workingPosition = 0;
 	private int speed = 100; //default
 
 	public static void main(String[] args) {
 		new GameOfLife();
 	}
 
 	public GameOfLife(){
 		grid = new Grid();
 		engine = new Engine(grid);
 		initFrame();
 		getContentPane().add(grid);
 		getContentPane().add(new startButton());
 		getContentPane().add(new pauseButton());
 		getContentPane().add(new stepButton());
 		getContentPane().add(new resetButton());
 		getContentPane().add(new clearButton());
 		initMenu();
 		setVisible(true);
 		setOff();
 	}
 
 	private void initFrame(){
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		getContentPane().setLayout(null);
 		setSize(grid.getXSize(), grid.getYSize() + (8 * Cell.CELL_SIZE));
 		setResizable(true);
 		setLocation(10,10);
 	}
 
 
 	private void initMenu() {
 		JMenuBar menu = new JMenuBar();
 		menu.setOpaque(true);
 		menu.setBackground(Color.WHITE);
 		menu.setPreferredSize(new Dimension(0,20));
 		JMenu file = new JMenu("File");
 		JMenu size = new JMenu("Size");
 		JMenu edit = new JMenu("Edit");
 		JMenuItem exit = new JMenuItem("Exit");
 		JMenuItem size1 = new JMenuItem("50 x 50");
 		JMenuItem size2 = new JMenuItem("100 x 100");
 		JMenuItem size3 = new JMenuItem("200 x 200");
 		edit.add(size);
 		size.add(size1);
 		size.add(size2);
 		size.add(size3);
 		file.addSeparator();
 		file.add(exit);
 		menu.add(file);
 		menu.add(edit);
 		setJMenuBar(menu);
 	}
 
 	private void setOff(){
 		coreN = 4 ;//Runtime.getRuntime().availableProcessors();
 
 		grid.test();
		
 		while(true){
 			try {
 				Thread.sleep(speed);
 			} catch (InterruptedException e) {
 				System.err.println("Error in setOff() => " + e.getMessage());
 			}
 			if(running){
 				finish = false;
 				synchronized(grid){
 					engine.reset();
 					engine.computeNextGen();					
 				}				
 				grid.forceUpdate();
 				finish = true;
 			}
 		}
 	}
 
 	private class startButton extends JButton{
 		protected startButton(){
 			super("START");
 			setBounds(0, grid.getYSize(), grid.getXSize() / 5, 3 * Cell.CELL_SIZE	);
 			this.addActionListener(new ActionListener(){
 				@Override
 				public void actionPerformed(ActionEvent e) {
					if(!running)
					//	grid.saveSnapShot();
					running = true;					
 				}
 			});
 		}
 	}
 
 	private class pauseButton extends JButton{
 		protected pauseButton(){
 			super("PAUSE");
 			setBounds(grid.getXSize() / nButtons, grid.getYSize(), grid.getXSize() / nButtons, 3 * Cell.CELL_SIZE	);
 			this.addActionListener(new ActionListener(){
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					running = false;
 				}
 			});
 		}
 	}
 
 	private class stepButton extends JButton{
 		protected stepButton(){
 			super("STEP");
 			setBounds(2 * (grid.getXSize() / nButtons), grid.getYSize(), grid.getXSize() / nButtons, 3 * Cell.CELL_SIZE	);
 			this.addActionListener(new ActionListener(){
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					if(running)
 						running = false;
 
 					else{
 						engine.reset();
 						engine.computeNextGen();					
 						grid.forceUpdate();						
 					}
 				}
 			});
 		}
 	}
 
 	private class resetButton extends JButton{
 		protected resetButton(){
 			super("RESET");
 			setBounds(3 * (grid.getXSize() / nButtons), grid.getYSize(), grid.getXSize() / nButtons, 3 * Cell.CELL_SIZE	);
 			this.addActionListener(new ActionListener(){
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					running = false;
 					while(!finish);
					engine.reset();
 					grid.loadSnapShot();
 					grid.forceUpdate();
 				}
 			});
 		}
 	}
 
 	private class clearButton extends JButton{
 		protected clearButton(){
 			super("CLEAR");
 			setBounds(4 * (grid.getXSize() / nButtons), grid.getYSize(), grid.getXSize() / nButtons, 3 * Cell.CELL_SIZE	);
 			this.addActionListener(new ActionListener(){
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					running = false;
 					while(!finish);
					engine.reset();
 					grid.clearGrid();
 					grid.forceUpdate();
 				}
 			});
 		}
 	}
 }
