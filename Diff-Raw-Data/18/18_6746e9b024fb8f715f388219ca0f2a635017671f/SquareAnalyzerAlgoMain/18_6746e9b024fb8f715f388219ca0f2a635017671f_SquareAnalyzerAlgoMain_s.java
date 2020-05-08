package ambibright.engine.squareAnalyser.algo;
 
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 import java.util.HashMap;
 import java.util.Map;
 
 import ambibright.engine.squareAnalyser.SquareAnalyserAlgorithm;
 
 public class SquareAnalyzerAlgoMain implements SquareAnalyserAlgorithm {
 
 	private Map<Integer, Integer> map;
 	private int posX, posY, current, nbPixel;
 
 	public SquareAnalyzerAlgoMain() {
 		map = new HashMap<Integer, Integer>();
 	}
 
 	public int[] getColor(BufferedImage image, Rectangle bound, int screenAnalysePitch) {
 		map.clear();
 		for (posX = 0; posX < bound.width && posX + bound.x < image.getWidth(); posX += screenAnalysePitch) {
 			for (posY = 0; posY < bound.height && posY + bound.y < image.getHeight(); posY += screenAnalysePitch) {
 				current = image.getRGB(bound.x + posX, bound.y + posY);
 				if (map.containsKey(current)) {
 					map.put(current, map.get(current) + 1);
 				} else {
 					map.put(current, 1);
 				}
 			}
 		}
 		nbPixel = -1;
 		for (int key : map.keySet()) {
 			if (map.get(key) > nbPixel) {
 				nbPixel = map.get(key);
 				current = key;
 			}
 		}
 		return new int[] { (current & 0x00ff0000) >> 16, (current & 0x0000ff00) >> 8, current & 0x000000ff };
 	}
 
 }
