 package ambibright.engine.squareAnalyser;
 
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 
import ambibright.engine.squareAnalyser.Algo.SquareAnalyzerAlgoAverage;
import ambibright.engine.squareAnalyser.Algo.SquareAnalyzerAlgoMain;
 
 public enum SquareAnalyser {
 
 	AverageColors(new SquareAnalyzerAlgoAverage()), MainColor(new SquareAnalyzerAlgoMain());
 
 	private SquareAnalyserAlgorithm squareAnalyserAlgorithm;
 
 	SquareAnalyser(SquareAnalyserAlgorithm squareAnalyserAlgorithm) {
 		this.squareAnalyserAlgorithm = squareAnalyserAlgorithm;
 	}
 
 	public int[] getColor(BufferedImage image, Rectangle bound, int screenAnalysePitch) {
 		return squareAnalyserAlgorithm.getColor(image, bound, screenAnalysePitch);
 	}
 
 }
