 package ambibright.engine.colorAnalyser.Algo;
 
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import ambibright.engine.colorAnalyser.SquareAnalyserAlgorithm;
 
 public class SquareAnalyzerAlgoMainAverage implements SquareAnalyserAlgorithm {
 
 	public static final int nbMain = 2; // Minus one
 	public static final int mainPourcent = 60;
 
 	private Map<Integer, Integer> map;
 	private int posX, posY, current, nbPixel;
 	private int red, green, blue, maxValue, nbOccur;
 	private Object[] sorted;
 
 	public SquareAnalyzerAlgoMainAverage() {
 		map = new HashMap<Integer, Integer>();
 	}
 
 	public Integer[] getColor(BufferedImage image, Rectangle bound, int screenAnalysePitch) {
 		// Init vars
 		map.clear();
 		nbPixel = 0;
 		red = 0;
 		green = 0;
 		blue = 0;
 
 		// Get all colors
 		for (posX = 0; posX < bound.width && posX + bound.x < image.getWidth(); posX += screenAnalysePitch) {
 			for (posY = 0; posY < bound.height && posY + bound.y < image.getHeight(); posY += screenAnalysePitch) {
 				current = image.getRGB(bound.x + posX, bound.y + posY);
 				if (map.containsKey(current)) {
 					map.put(current, map.get(current) + 1);
 				} else {
 					map.put(current, 1);
 				}
 				nbPixel++;
 			}
 		}
 
 		// Order the colors per occurence
 		sorted = map.keySet().toArray();
 		Arrays.sort(sorted, Collections.reverseOrder());
 
 		// There is a main color
 		if ((Integer) sorted[0] >= (nbPixel * mainPourcent / 100)) {
 			maxValue = (Integer) sorted[0];
 		} else if (map.size() > nbMain) {
 			maxValue = (Integer) sorted[nbMain - 1];
 		} else {
 			maxValue = 0;
 		}
 
 		// Get the average
 		nbPixel = 0;
 		for (Integer current : map.keySet()) {
 			nbOccur = map.get(current);
 			if (nbOccur >= maxValue) {
 				red += ((current & 0x00ff0000) >> 16) * nbOccur;
 				green += ((current & 0x0000ff00) >> 8) * nbOccur;
 				blue += (current & 0x000000ff) * nbOccur;
 				nbPixel += nbOccur;
 			}
 		}
 		return new Integer[] { red / nbPixel, green / nbPixel, blue / nbPixel };
 	}
 
 }
