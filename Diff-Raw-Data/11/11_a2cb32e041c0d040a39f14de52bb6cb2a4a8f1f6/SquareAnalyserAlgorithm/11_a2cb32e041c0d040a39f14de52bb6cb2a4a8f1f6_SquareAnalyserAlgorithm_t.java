 package ambibright.engine.colorAnalyser;
 
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 
 public interface SquareAnalyserAlgorithm {
 
	public static final Integer[] defaultColor = new Integer[] { 0, 0, 0 };

 	public Integer[] getColor(BufferedImage image, Rectangle bound, int screenAnalysePitch);
 
 }
