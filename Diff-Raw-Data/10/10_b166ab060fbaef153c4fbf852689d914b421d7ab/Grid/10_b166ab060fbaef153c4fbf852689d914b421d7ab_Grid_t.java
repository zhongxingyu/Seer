 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Vector;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 
 
 public class Grid extends JPanel{
 	private Cell[][] cells;
 	private final static boolean ALIVE = true;
 	private final static boolean DEAD = false;
 	private static boolean killing = false;
 	private static boolean addingFigure = false;
 	private static String figureName ="";
 	private int xSize;
 	private int ySize;
 	private int size = 60; // default
 	private Vector<Cell> snapShot = new Vector<Cell>();
 	private Vector<Cell> changedCells = new Vector<Cell>();
 	private int generation = 0;
 
 	public Grid(int size){
 		this.size = size;
 		initCells();
 		initFrame();
 	}
 
 	public void setGridSize(int newSize){
 		//serve un metodo per uccidere le cellule vive fuori dal frame
 		//quando si fa il resize in diminuire
 		size = newSize;
 		xSize = (Cell.CELL_SIZE * size);
 		ySize = (Cell.CELL_SIZE * size);
 		setSize(xSize,ySize);
 	}
 
 	private void initCells(){ 
 		cells = new Cell[size][size];
 		for(int i = 0;i < size;i++ )
 			for(int j = 0;j < size;j++)
 				cells[i][j] = new GridCell(i,j);
 	}
 
 	private void initFrame(){
 		setLayout(null);
 		xSize = (Cell.CELL_SIZE * size);
 		ySize = (Cell.CELL_SIZE * size);
 		setSize(xSize,ySize);
 		addCellsToFrame();
 		setVisible(true);
 	}	
 
 	private void addCellsToFrame() {
 		for(int i = 0;i < size;i++)
 			for(int j = 0;j < size;j++){
 				add(cells[i][j]);
 			}
 	}
 
 	private void removeCellsFromFrame(){
 		for(int i = 0;i < size;i++)
 			for(int j = 0;j < size;j++){
 				remove(cells[i][j]);
 			}		
 	}
 
 	private void switchCell(Cell cell){
 		synchronized(this){
 			((GridCell)cell).changeNow();
 			forceUpdate();
 		}
 	}
 
 	public Cell getCell(int i, int j) {
		i = (i < 0)?(size + i):(i % size);
		j = (j < 0)?(size + j):(j % size);
		
		return cells[i][j];
 	}
 
 	public void changeState(Cell cell){
 		changedCells.add(cell);
 		((GridCell)cell).changeNext();
 	}
 
 	public void forceUpdate(){
 		repaint();
 	}
 
 
 	public boolean isLivingCell(Cell neighborCell) {
 		return neighborCell.isAliveNow();
 	}
 
 	public int getXSize(){
 		return xSize;
 	}
 
 	public int getYSize(){
 		return ySize;
 	}
 
 	public int getGridSize(){
 		return size;
 	}
 
 	public void nextGeneration(){
 		generation++;
 
 		if(generation == 1){
 			System.out.println("Sono1");
 			for(Cell[] c : cells){
 				for(Cell cell : c)
 					if(((GridCell)cell).isAliveNow())
 						snapShot.add(cell);
 			}
 		}
 
 		synchronized(this){
 			for(Cell cell : changedCells)
 				((GridCell)cell).swap();
 		}
 
 		changedCells = new Vector<Cell>();
 		forceUpdate();
 	}
 
 	private class GridCell extends Cell{
 		private boolean definitelyDead = false;
 		private boolean actualGeneration = DEAD;
 		private boolean nextGeneration = DEAD;
 		private static final long serialVersionUID = 1L;
 		protected GridCell(int x, int y) {
 			super(x, y);
 			setBackground(Color.BLACK);
 			this.addActionListener(new ActionListener(){
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					if(addingFigure)
 						printFigure(GridCell.this);
 					else if(killing){
 						GridCell.this.definitelyDead = true;
 						GridCell.this.actualGeneration = DEAD;
 						GridCell.this.setBackground(Color.BLACK);
 					}
 					else{
 						if(GridCell.this.definitelyDead)
 							GridCell.this.definitelyDead = false;
 						Grid.this.switchCell(GridCell.this);
 					}
 					System.out.println("("+ isAliveNow()+") CLICK ON => " + "(" + GridCell.this.auxGetX() + ", " + GridCell.this.auxGetY() + ")");
 				}
 			});
 		}		
 
 		public void changeNow(){
 			nextGeneration = (actualGeneration)?DEAD:ALIVE;
 			actualGeneration = nextGeneration;
 			setBackground((actualGeneration)?Color.WHITE:Color.BLACK);
 		}
 
 		private void changeNext(){
 			nextGeneration = (actualGeneration)?DEAD:ALIVE;
 		}
 
 		@Override
 		public boolean isAliveNow(){
 			return actualGeneration;
 		}
 
 		@Override
 		public boolean isAliveNext(){
 			return nextGeneration;
 		}
 		@Override
 		public void swap() {//fai modifica solo se necessario
 			if(nextGeneration != actualGeneration){
 				actualGeneration = nextGeneration;
 				setBackground((nextGeneration)?Color.WHITE:Color.BLACK);	
 			}
 		}
 
 		@Override
 		public void reset(){
 			if(definitelyDead)
 				definitelyDead = false;
 			actualGeneration = DEAD;
 			setBackground(Color.BLACK);
 		}
 		@Override
 		public boolean isDefDead(){
 			return definitelyDead;
 		}
 
 	}
 	public void setFigure(String figureName){
 		addingFigure = true;
 		killing = false;
 		this.figureName = figureName;
 	}
 
 	public void printFigure(Cell cell){
 		addingFigure = false;
 		int x = cell.auxGetX();
 		int y = cell.auxGetY();
 		int posX = 0;
 		int posY = 0;
 
 		int[][] coordinates = Figures.getCoordinates(figureName);
 		for(int[] pos : coordinates){
 			posX = pos[0];
 			posY = pos[1];
 			cell = getCell(x + posX, y + posY );
 			cell.reset();/// o faccio questo o faccio un metodo che fa quello che fanno questi 2 con qualche accorgimento, ma visto che ci sono[:
 			cell.changeNow();
 		}
 	}
 	public boolean isAddingFigure(){
 		return addingFigure;
 	}
 
 	public void stopAddingFigure(){
 		addingFigure = false;
 	}
 
 	public void setKilling(){
 		killing = (killing)? false : true;
 	}
 
 	public void clearGrid(){ 
 		for(int i = 0;i < size;i++)
 			for(int j = 0;j < size;j++)
 				reset(cells[i][j]);
 	}
 
 	private void reset(Cell cell){
 		if(cell.isAliveNow() || cell.isDefDead())
 			cell.reset();
 	}
 
 
 	public void loadSnapShot(){
 		clearGrid();
 		for(Cell cell : snapShot){
 			int x = cell.auxGetX();
 			int y = cell.auxGetY();
 			((GridCell)cells[x][y]).changeNow();
 		}
 		resetGeneration();
 	}
 
 	public void resetGeneration() {
 		snapShot = new Vector<Cell>();
 		generation = 0;		
 	}
 
 	public int getGeneration() {
 		return generation;
 	}
 }
