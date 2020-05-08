 /**
  * 
  */
 package core;
 
 import gui.OutputPane;
 import io.BasicInput;
 
 import java.io.IOException;
 
 /**
  * @author Phillipp Ohl
  * @version 0.1
  */
 public class HillClimberImplementation extends Thread {
 
 	static OutputPane gui;
 	private static HillClimber hc;
 	private static int parentSetLength;
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {	
 		gui = new OutputPane();
 	}
 	
 	public static void startAlgorithm() {
 		try {
 			getInitialInformation();
 			Set parent = new Set(parentSetLength);
 			hc = new HillClimber(parent);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		hc.defineInitialState();
 				
 		try {
 			hc.pickRandomSolution();
 			System.out.println("Random pick subset1: " + hc.getCurrentSolution()[0] + '\n' + "Random pick subset2: " + hc.getCurrentSolution()[1]);
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 		
 		try {
 			startNeighborhoodSearch();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private static void getInitialInformation() throws IOException {
		parentSetLength = gui.getSizeOfParentSet();
 	}
 	
 	private static void startNeighborhoodSearch() throws Exception {
 		new Thread() {
 			@Override public void run() {
 				try {
 			  		do {
 			  			Thread.sleep(2000);
 			  			
 						hc.computePossibleSolutions();
 						
 						setLabels();
 						hc.printPossibleSolutions();
 						
 						if (hc.getPending() == false) {
 							break;
 						}
 						
 						hc.findMinimum();	
 						
 						if (hc.getReachedLocalMinimum() == true) {
 							gui.showLocalMinimumDialog();
 						}	
 						if (hc.getFitnessValue() == 0) {
 							gui.showGlobalMinimumDialog();
 						}						
 					} while(hc.getPending() == true);
 				} catch ( InterruptedException e ) { e.printStackTrace(); } catch (Exception e) {
 				  e.printStackTrace();
 				}
 			}
 	    }.start();
 	}
 	
 	private static void setLabels() {
 		int[] labelInput = hc.getPossibleSolutions();
 		gui.setLabels(labelInput[0], labelInput[1], labelInput[2], labelInput[3], labelInput[4], labelInput[5], labelInput[6], labelInput[7]);
 		gui.setCurrentSolutionLabel(hc.getFitnessValue());
 	}
 }
