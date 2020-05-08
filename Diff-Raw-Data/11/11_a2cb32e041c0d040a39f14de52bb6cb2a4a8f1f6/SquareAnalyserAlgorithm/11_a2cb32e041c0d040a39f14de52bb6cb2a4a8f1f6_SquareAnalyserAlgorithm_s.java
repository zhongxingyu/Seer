 package ambibright.engine.colorAnalyser;
 
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 
 public interface SquareAnalyserAlgorithm {
 
 	public Integer[] getColor(BufferedImage image, Rectangle bound, int screenAnalysePitch);
 
 }
