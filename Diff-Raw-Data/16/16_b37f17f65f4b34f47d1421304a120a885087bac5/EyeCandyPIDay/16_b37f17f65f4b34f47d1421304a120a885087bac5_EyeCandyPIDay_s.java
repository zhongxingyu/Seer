 package org.kamehamehaaa.android.livewallpaper.candies;
 
import java.io.WriteAbortedException;
 
 import org.kamehamehaaa.android.livewallpaper.engine.LCDLiveWallpaper;
 import org.kamehamehaaa.android.livewallpaper.engine.WriteClass;
 
 public class EyeCandyPIDay extends EyeCandy {
 
	static int increment = 0;
 	static boolean color = false;
 	static int pi_small[][] = {
 			{ 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2 },
 			{ 0, 0, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2 },
 			{ 0, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2 },
 			{ 0, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2 },
 			{ 2, 2, 1, 2, 2, 2, 2, 1, 1, 2, 2, 2, 2, 1, 1, 1, 2, 2, 2, 2, 2, 2 },
 			{ 2, 1, 2, 2, 0, 2, 2, 1, 1, 2, 0, 0, 2, 1, 1, 1, 2, 0, 0, 0, 0, 0 },
 			{ 2, 2, 2, 0, 0, 2, 1, 1, 1, 2, 0, 0, 2, 1, 1, 2, 0, 0, 0, 0, 0, 0 },
 			{ 0, 0, 0, 0, 0, 2, 1, 1, 1, 2, 0, 0, 2, 1, 1, 2, 0, 0, 0, 0, 0, 0 },
 			{ 0, 0, 0, 0, 0, 2, 1, 1, 1, 2, 0, 0, 2, 1, 1, 2, 0, 0, 0, 0, 0, 0 },
 			{ 0, 0, 0, 0, 0, 2, 1, 1, 1, 2, 0, 0, 2, 1, 1, 2, 0, 0, 0, 0, 0, 0 },
 			{ 0, 0, 0, 0, 0, 2, 1, 1, 2, 2, 0, 0, 2, 1, 1, 2, 0, 0, 0, 0, 0, 0 },
 			{ 0, 0, 0, 0, 2, 2, 1, 1, 2, 0, 0, 0, 2, 1, 1, 2, 0, 0, 0, 2, 2, 2 },
 			{ 0, 0, 0, 0, 2, 1, 1, 1, 2, 0, 0, 0, 2, 1, 1, 2, 2, 0, 0, 2, 1, 2 },
 			{ 0, 0, 0, 2, 2, 1, 1, 1, 2, 0, 0, 0, 2, 1, 1, 1, 2, 0, 0, 2, 1, 2 },
 			{ 0, 0, 2, 2, 1, 1, 1, 1, 2, 0, 0, 0, 2, 1, 1, 1, 1, 2, 2, 1, 2, 2 },
 			{ 0, 0, 2, 1, 1, 1, 1, 2, 2, 0, 0, 0, 2, 1, 1, 1, 1, 1, 1, 1, 2, 0 },
 			{ 0, 0, 2, 1, 1, 1, 1, 2, 0, 0, 0, 0, 2, 2, 1, 1, 1, 1, 1, 2, 2, 0 },
 			{ 0, 0, 2, 2, 1, 1, 2, 2, 0, 0, 0, 0, 0, 2, 2, 1, 1, 1, 2, 2, 0, 0 },
 
 			{ 0, 0, 2, 2, 2, 2, 2, 2, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0 },
 			{ 0, 0, 2, 1, 1, 1, 2, 2, 2, 2, 1, 1, 1, 2, 2, 1, 2, 0, 2, 1, 2, 0 },
 			{ 0, 0, 2, 1, 2, 2, 1, 2, 2, 1, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 2, 0 },
 			{ 0, 0, 2, 1, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 2, 2, 1, 2, 1, 2, 0, 0 },
 			{ 0, 0, 2, 1, 2, 2, 2, 1, 2, 1, 1, 1, 1, 1, 2, 2, 1, 2, 1, 2, 0, 0 },
 			{ 0, 0, 2, 1, 2, 2, 2, 1, 2, 1, 2, 2, 2, 1, 2, 2, 2, 1, 2, 2, 0, 0 },
 			{ 0, 0, 2, 1, 2, 2, 1, 2, 2, 1, 2, 0, 2, 1, 2, 0, 2, 1, 2, 0, 0, 0 },
 			{ 0, 0, 2, 1, 1, 1, 2, 2, 2, 1, 2, 0, 2, 1, 2, 0, 2, 1, 2, 0, 0, 0 },
 			{ 0, 0, 2, 2, 2, 2, 2, 0, 2, 2, 2, 0, 2, 2, 2, 0, 2, 2, 2, 0, 0, 0 } };
 
 	public EyeCandyPIDay() {
 		init();
 	}
 
 	@Override
 	public boolean[][] draw(boolean[][] matrix) {
		if (increment++ == 60) {
 			color = !color;
			increment = 0;
 		}
		WriteClass.setBigBinary(false);
 		if (LCDLiveWallpaper.getLCD_WIDTH() >= 40) {
 			drawSmall(matrix);
 		} else {
 			drawSmall(matrix);
 		}
 		return matrix;
 	}
 
 	private void drawSmall(boolean[][] matrix) {
 		int startw = (LCDLiveWallpaper.getLCD_WIDTH() / 2) - 11;
		int starth = 46; // (LCDLiveWallpaper.getLCD_HEIGHT() / 2) - 14;
 		
 		for (int i = 0; i < 22; i++)
 			for (int j = 0; j < 27; j++) {
 				try {
 					if (pi_small[j][i] == 1) {
 						matrix[i + startw][j + starth] = color;
 					} else if (pi_small[j][i] == 2) {
 						matrix[i + startw][j + starth] = !color;
 					}
 				} catch (Exception e) {
 				}
 			}
 
 	}
 
 	@Override
 	public void init() {
 
 	}
 
 }
