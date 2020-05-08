 package solver;
 
 /**
 * ,    Solver.
  * 
 *       , 
 * (...)        
 *  i-  j- .
  * 
 *    /,  
 * /         .
  */
 
 public class Data {
 	public Data(int[] mines, int[] factories, int[][] cost) {
 		this.mines = new int[mines.length];
 		this.factories = new int[factories.length];
 		this.cost = new int[mines.length][factories.length];
 		this.correctMines = new boolean[mines.length];
 		this.correctFactories = new boolean[factories.length];
 
 		for (int i = 0; i < correctMines.length; ++i) {
 			correctMines[i] = true;
 		}
 
 		for (int i = 0; i < correctFactories.length; ++i) {
 			correctFactories[i] = true;
 		}
 
 		for (int i = 0; i < mines.length; ++i) {
 			this.mines[i] = mines[i];
 		}
 
 		for (int i = 0; i < factories.length; ++i) {
 			this.factories[i] = factories[i];
 		}
 
 		for (int i = 0; i < cost.length; ++i) {
 			for (int j = 0; j < cost[0].length; ++j) {
 				this.cost[i][j] = cost[i][j];
 			}
 		}
 	}
 
 	/**
	 *    
	 * 
	 * @return        , 
	 *         
 	 */
 	public boolean isDataValid() {
 		int minesSum = 0;
 		int factoriesSum = 0;
 
 		for (int i = 0; i < mines.length; ++i) {
 			minesSum += mines[i];
 		}
 		for (int i = 0; i < factories.length; ++i) {
 			factoriesSum += factories[i];
 		}
 
 		return (minesSum == factoriesSum);
 	}
 
 	public boolean isAllCellsForbidden() {
 		for (int i = 0; i < correctMines.length; ++i) {
 			if (correctMines[i] == true)
 				return false;
 		}
 		for (int i = 0; i < correctFactories.length; ++i) {
 			if (correctFactories[i] == true)
 				return false;
 		}
 
 		return true;
 	}
 
 	public void decMineProposal(int mine, int number) {
 		mines[mine] -= number;
 	}
 
 	public void decFactoryConsume(int factory, int number) {
 		factories[factory] -= number;
 	}
 
 	public void removeMine(int num) {
 		this.correctMines[num] = false;
 	}
 
 	public void removeFactory(int num) {
 		this.correctFactories[num] = false;
 	}
 
 	public boolean isAlowedMine(int num) {
 		return correctMines[num];
 	}
 
 	public boolean isAlowedFactory(int num) {
 		return correctFactories[num];
 	}
 
 	public int[] getMines() {
 		return this.mines;
 	}
 
 	public int[] getFactories() {
 		return this.factories;
 	}
 
 	public int[][] getCost() {
 		return this.cost;
 	}
 
 	private boolean[] correctMines;
 	private boolean[] correctFactories;
 	private int[] mines;
 	private int[] factories;
 	private int[][] cost;
 }
