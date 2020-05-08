 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.WindowConstants;
 
 
 public class GameBoard extends JFrame implements ActionListener{
 
 	private final int ROWS = 50 ,COLUMNS = 50;
 	private JPanel gridPanel;
 	private Cell[][] cells;
 	private GameLoop gameLoop;
 
 	public GameBoard (){
 		this.setLayout(new BorderLayout());
 
 		this.gridPanel = new JPanel();
 		gridPanel.setLayout(new GridLayout(ROWS, COLUMNS));
 		this.add(gridPanel, BorderLayout.CENTER);
 		this.gridPanel.setPreferredSize(new Dimension(11*COLUMNS,11*COLUMNS));
 		initCells();
 		this.add(gridPanel);
 
 		JPanel controlPanel = new JPanel();
 		//Use GridPanels width and a dummy height.
 		controlPanel.setPreferredSize(new Dimension(gridPanel.getWidth(), 30));
 		this.add(controlPanel, BorderLayout.SOUTH);
 
 		JButton start = new JButton("Start");
 		start.setActionCommand("Start");
 		start.addActionListener(this);
 		controlPanel.add(start);
 
 		JButton stop = new JButton("Stop");
 		stop.setActionCommand("Stop");
 		stop.addActionListener(this);
 		controlPanel.add(stop);
 
 		this.pack();
 		this.setVisible(true);
 		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 
 
 	}
 
 	private void initCells() {
 		this.cells = new Cell [ROWS][COLUMNS];
 		for (int i = 0; i < ROWS; i++){
 			for (int l = 0; l < COLUMNS; l++){
 				this.cells[i][l] = new Cell(i,l);
 				this.cells[i][l].addActionListener(this);
 				this.cells[i][l].setActionCommand(i + "," + l);
 				this.gridPanel.add(this.cells[i][l]);
 			}
 		}
 	}
 
 	public static void main (String [] args){
		System.out.println("Initializing new GameBoard");
 		new GameBoard();
 	}
 
 	private int countNeighborsAlive(Cell cell) {
 		int top = cell.getColumn() + 1;
 		int buttom = cell.getColumn() - 1;
 		int right = cell.getRow() + 1;
 		int left = cell.getRow() - 1;
 
 		int count = 0;
 
 		if (top < ROWS) {
 			if (cells[cell.getRow()][top].getState() == State.ALIVE) {
 				count +=1;
 			}

 			if (right < ROWS){
 				if (cells[right][top].getState() == State.ALIVE){
 					count+=1;
 				}
 			}
 			if (left >= 0 ) {
 				if (cells[left][top].getState() == State.ALIVE){
 					count +=1;
 				}
 			}
 		}
 		if (buttom >= 0 ) {
 			if (cells[cell.getRow()][buttom].getState() == State.ALIVE){
 				count +=1;
 			}
 
 			if (right < ROWS) {
 				if (cells[right][buttom].getState() == State.ALIVE){
 					count +=1;
 				}
 			}
 
 			if (left >= 0) {
 				if (cells[left][buttom].getState() == State.ALIVE){
 					count +=1;
 				}
 			}
		}
 
 		if (right < ROWS ) {
 			if (cells[right][cell.getColumn()].getState() == State.ALIVE){
 			}
 
 			if (left >= 0) {
 				if (cells[left][cell.getColumn()].getState() == State.ALIVE){
 					count +=1;
 				}
 			}
 		}
 		return count;
 	}
 
 	public void doTurn (){
 		updateNeighbors();
 		updateCells();
 	}
 
 	private void updateCells() {
 		for (int i = 0; i < this.ROWS; i ++){
 			for (int l = 0; l < this.COLUMNS; l ++){
 				cells[i][l].newTurn();
 			}
 		}
 	}
 
 	private void updateNeighbors() {
 		for (int i = 0; i < this.ROWS; i ++){
 			for (int l = 0; l < this.COLUMNS; l ++){
 				cells[i][l].setNeighbors(countNeighborsAlive(cells[i][l]));
 			}
 		}
 	}
 
 	
 	@Override
 	public void actionPerformed(ActionEvent evt) {
 		if (evt.getActionCommand() == "Start"){
 			gameLoop = new GameLoop(this);
			gameLoop.run();
			
 		}
 		else if (evt.getActionCommand() == "Stop"){
 			if (gameLoop != null){
 				gameLoop.stop();
 			}
 		}
 		for (int i = 0; i < this.ROWS; i ++){
 			for (int l = 0; l < this.COLUMNS; l ++){
 				if (evt.getActionCommand().equals(((i + "," + l)))){
 					this.cells[i][l].changeState();
 				}
 			}
 		}
 	}//End actionPerfocmed
 }
