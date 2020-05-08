 package info.pishen.gameoflife;
 
 import java.util.logging.Logger;
 
 public class ParallelGenerator {
 	private static Logger log = Logger.getLogger(ParallelGenerator.class.getName());
 	
 	private boolean[][] oldGrid, newGrid;
 	private CellGrid cellGrid;
 	private volatile Updater updater;
 	private volatile int parallel = 1;
 	
 	public ParallelGenerator(CellGrid cellGrid){
 		this.cellGrid = cellGrid;
 	}
 	
 	public void run(){
 		updater = new Updater();
 		updater.start();
 	}
 	
 	public void pause(MainGUI mainGUI){
 		updater.mainGUI = mainGUI;
 		updater.toPause = true;
 	}
 	
 	public void setParallel(int value){
 		parallel = value;
 	}
 	
 	private class Updater extends Thread{
 		volatile MainGUI mainGUI;
 		volatile boolean toPause = false;
 		
 		@Override
 		public void run() {
 			while(true){
 				oldGrid = cellGrid.getDuplicateGrid();
 				newGrid = new boolean[oldGrid.length][oldGrid[0].length];
 				
				long startTime = System.currentTimeMillis();
				
 				SubUpdater[] subUpdaters = new SubUpdater[parallel];
 				int blockSize = oldGrid.length / subUpdaters.length;
 				for(int i = 0; i < subUpdaters.length; i++){
 					subUpdaters[i] = new SubUpdater(i * blockSize, Math.min((i+1) * blockSize, oldGrid.length));
 					subUpdaters[i].start();
 				}
 				
 				for(int i = 0; i < subUpdaters.length; i++){
 					try {
 						subUpdaters[i].join();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 				
				long endTime = System.currentTimeMillis();
				log.info("update time: " + ((endTime - startTime) / 1000.0) + " secs");
				
 				if(toPause){
 					mainGUI.enableRun();
 					break;
 				}
 				
 				cellGrid.setNewGrid(newGrid);
 			}
 		}
 	}
 	
 	private class SubUpdater extends Thread{
 		private int iStart, iEnd;
 		
 		public SubUpdater(int iStart, int iEnd){
 			this.iStart = iStart;
 			this.iEnd = iEnd;
 		}
 		
 		@Override
 		public void run() {
 			//iEnd is excluded
 			for(int i = iStart; i < iEnd; i++){
 				for(int j = 0; j < oldGrid[0].length; j++){
 					int count = 0;
 					for(int iSub = Math.max(0, i - 1); iSub < Math.min(i + 2, oldGrid.length); iSub++){
 						for(int jSub = Math.max(0, j - 1); jSub < Math.min(j + 2, oldGrid[i].length); jSub++){
 							if(iSub == i && jSub == j){
 								continue;
 							}
 							if(oldGrid[iSub][jSub]){
 								count++;
 							}
 						}
 					}
 					if(count < 2 || count > 3){
 						newGrid[i][j] = false;
 					}else if(count == 3){
 						newGrid[i][j] = true;
 					}else{
 						newGrid[i][j] = oldGrid[i][j];
 					}
 				}
 			}
 		}
 	}
 }
