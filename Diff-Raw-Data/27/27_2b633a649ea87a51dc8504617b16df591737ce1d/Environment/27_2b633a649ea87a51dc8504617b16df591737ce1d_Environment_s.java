 package daisy;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jfree.ui.RefineryUtilities;
 
 public class Environment implements Runnable {
 	private int white;
 	private int black;
 	private double temperature; //Between 0-1
 	private int oldWhite;
 	private int oldBlack;
 	private int delay;
 	private int loops;
 	private double growthRate;
 	
 	public Environment(int white, int black, double temp, int loops, double growthRate) {
 		this.white = white;
 		this.black = black;
 		temperature = temp;
 		delay = 0;
 		this.loops = loops;
 		this.growthRate = growthRate;
 	}
 	
 	
 	@Override
 	public void run() {
 		while(loops-- > 0) {
 			int deadWhite = 0;
 			for (int i = 0; i < white; i++) {
 				if (Math.random() > temperature) {
 					deadWhite++;
 				}
 			}
 			
 			int deadBlack = 0;
 			for (int i = 0; i < black; i++) {
 				if (Math.random() < temperature) {
 					deadBlack++;
 				}
 			}
 			
 			white -= deadWhite;
 			black -= deadBlack;
 			int newFlowers = (int)(0.5 + (white + black) * growthRate);
 			double proportion = ((double)black / (double)(black + white));
 			
 			for (int i = 0; i < newFlowers; i++) {
 				if (Math.random() < proportion) {
 					black++;
 				} else {
 					white++;
 				}
 			}
			
 			System.out.println("White: " + white + " Black: " + black + " Temp: " + temperature + " proportion " + proportion);
 //			System.out.println("Black: " + black);
 		}
 		
		List<Integer> blackFlowerList = new ArrayList<Integer>();
		List<Integer> whiteFlowerList = new ArrayList<Integer>();
 		
		blackFlowerList.add(black);
		whiteFlowerList.add(white);
         final DaisyLineFrame demo = new DaisyLineFrame("Line Chart Demo 6", blackFlowerList, whiteFlowerList);
         demo.pack();
         RefineryUtilities.centerFrameOnScreen(demo);
         demo.setVisible(true);
 
 	}
 	
 }
