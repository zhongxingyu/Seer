 package org.knime.knip.trackingrevised.data.features;
 
 import net.imglib2.meta.ImgPlus;
 import net.imglib2.type.numeric.RealType;
 
 import org.knime.knip.base.data.img.ImgPlusValue;
 import org.knime.knip.trackingrevised.data.graph.Node;
 import org.knime.knip.trackingrevised.data.graph.TransitionGraph;
 import org.knime.network.core.api.PersistentObject;
 
 public class IntensityFeatures<T extends RealType<T>> extends FeatureClass {
 	@Override
 	public String getName() {
 		return "Intensity features";
 	}
 
 	@Feature(name = "Intensity difference histogram")
 	public static <T extends RealType<T>> double diffHisto(final TransitionGraph<T> tg) {
 		//diff inten hist
 		return traverseConnectedDiffMV(tg, new CalculationMV() {
 			
 			@Override
 			public double[] calculate(PersistentObject po) {
 				Node<T> node = new Node<T>(tg.getNet(), po);
 				ImgPlusValue<T> img = node.getSegmentImage();
 				return createHistogram(img.getImgPlus(), 64);
 			}
 		});
 	}
 	
 	@Feature(name = "Intensity difference sum")
 	public static <T extends RealType<T>> double diffSum(final TransitionGraph<T> tg) {
 		//diff inten sum
 		return traverseConnectedDiffSV(tg, new CalculationSV() {
 			
 			@Override
 			public double calculate(PersistentObject po) {
 				Node<T> node = new Node<T>(tg.getNet(), po);
 				ImgPlusValue<T> img = node.getSegmentImage();
 				double sum = 0.0;
 				for(T val : img.getImgPlus()) {
 					sum += val.getRealDouble();
 				}
 				return sum;
 			}
 		});
 	}
 	
 	@Feature(name = "Intensity difference mean")
 	public static <T extends RealType<T>> double diffMean(final TransitionGraph<T> tg) {
 		//diff inten mean
 		return traverseConnectedDiffSV(tg, new CalculationSV() {
 			
 			@Override
			public double calculate(PersistentObject po) {
 				Node<T> node = new Node<T>(tg.getNet(), po);
 				ImgPlusValue<T> img = node.getSegmentImage();
				double sum = 0.0;
				long count = 0;
				for(T val : img.getImgPlus()) {
					sum += val.getRealDouble();
					count++;
				}
				return sum / count;
 			}
 		});
 	}
 	
 	@Feature(name = "Intensity difference deviation")
 	public static <T extends RealType<T>> double diffDeviation(final TransitionGraph<T> tg) {
 		//diff inten devia
		return traverseConnectedDiffSV(tg, new CalculationSV() {
 			
 			@Override
			public double calculate(PersistentObject po) {
 				Node<T> node = new Node<T>(tg.getNet(), po);
 				ImgPlusValue<T> img = node.getSegmentImage();
				double sum = 0.0;
				long count = 0;
				for(T val : img.getImgPlus()) {
					sum += val.getRealDouble();
					count++;
				}
				double mean = sum / count;
				double deviation = 0.0;
				for(T val : img.getImgPlus()) {
					deviation += Math.abs(val.getRealDouble()-mean);
				}
				return deviation;
 			}
 		});
 	}
 	
 	private static <T extends RealType<T>> double[] createHistogram(ImgPlus<T> img,int noBins) {
 		double[] hist = new double[noBins];
 		double min = img.firstElement().createVariable().getMinValue();
 		double max = img.firstElement().createVariable().getMinValue();
 		double scale = (hist.length-1)*(max-min);
 		for(T val : img) {
 			hist[(int)((val.getRealDouble()-min)*scale)]++;
 		}
 		return hist;
 	}
 }
