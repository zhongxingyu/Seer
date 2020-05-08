package ambibright.engine.squareAnalyser.Algo;
 
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 
 import ambibright.engine.squareAnalyser.SquareAnalyserAlgorithm;
 
 public class SquareAnalyzerAlgoAverage implements SquareAnalyserAlgorithm {
 
 	private int current, red, green, blue, nbPixel, posX, posY;
 
 	public int[] getColor(BufferedImage image, Rectangle bound, int screenAnalysePitch) {
 		red = 0;
 		green = 0;
 		blue = 0;
 		nbPixel = 0;
 		for (posX = 0; posX < bound.width; posX += screenAnalysePitch) {
 			for (posY = 0; posY < bound.height; posY += screenAnalysePitch) {
 				current = image.getRGB(bound.x + posX, bound.y + posY);
 				red += (current & 0x00ff0000) >> 16;
 				green += (current & 0x0000ff00) >> 8;
 				blue += current & 0x000000ff;
 				nbPixel++;
 			}
 		}
 		return new int[] { red / nbPixel, green / nbPixel, blue / nbPixel };
 	}
 }
