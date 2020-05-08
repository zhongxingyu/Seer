 package sturla.atitp.imageprocessing;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.jfree.data.Range;
 
 import sturla.atitp.imageprocessing.edgeDetector.EdgeDetector;
 import sturla.atitp.imageprocessing.synthesization.SynthesizationFunction;
 import sturla.atitp.imageprocessing.synthesization.SynthesizationType;
 
 class SingleChannel implements Cloneable {
 	
 	static final int MIN_CHANNEL_COLOR = 0;
 	static final int MAX_CHANNEL_COLOR = 255;
 
 	private int width;
 	private int height;
 
 	private double[] channel;
 
 	public SingleChannel(int width, int height){
 		if(width <= 0 || height <= 0) {
 			throw new IllegalArgumentException("Images must have at least 1x1 pixel size");
 		}
 
 		this.width = width;
 		this.height = height;
 		this.channel = new double[width*height];
 	}
 
 	public SingleChannel(int width, int height, BufferedImage bi, int chnl){
 		this(width, height);
 
 		if(chnl < 0 || chnl > 2 ) {
 			throw new IllegalArgumentException("SingleChannel must be between 0 and 2");
 		}
 
 		for(int x = 0 ; x < width ; x++ ) {
 			for(int y = 0 ; y < height ; y++) {
 				setPixel(x, y, bi.getData().getPixel(x, y, new double[3])[chnl]);
 			}
 		}
 	}
 
 	public int getHeight() {
 		return height;
 	}
 
 	public int getWidth() {
 		return width;
 	}
 
 	public double getPixel(int x, int y) {
 		if (!validPixel(x, y)) {
 			throw new IndexOutOfBoundsException();
 		}
 
 		return  channel[y * this.getWidth() + x];
 	}
 
 	public void setPixel(int x, int y, double color){
 		if (!validPixel(x, y)) {
 			throw new IndexOutOfBoundsException();
 		}
 
 		channel[y * this.getWidth() + x] = color;
 	}
 
 	public SingleChannel cropImage(int x1, int y1, int x2, int y2) {
 		if(!validSquare(x1,y1, x2, y2)){
 			throw new IllegalArgumentException("Not a valid square.");
 		}
 
 		SingleChannel newImage = new SingleChannel(y2 - y1, x2 - x1);	
 		int i = 0;
 		for(int y = y1; y < y2; y++) {
 			for(int x = x1; x < x2 ; x++) {
 				newImage.channel[i++] = this.getPixel(x, y);				
 			}
 		}
 
 		return newImage;
 	}
 
 	public boolean validPixel(int x, int y) {
 		boolean validX = x >= 0 && x < this.getWidth();
 		boolean validY = y >= 0 && y < this.getHeight(); 
 		return validX && validY;
 	}
 
 	public boolean validSquare(int x1, int y1, int x2, int y2){
 		boolean validX = this.validPixel(x1, y1);
 		boolean validY = this.validPixel(x2, y2);
 		boolean validSquare = (x1 < x2) && (y1 < y2);
 		return validX && validY && validSquare;
 	}
 
 	public double[] getPixels() {
 		return this.channel;
 	}
 
 	public void add(SingleChannel SingleChannel2) {
 		for( int x = 0 ; x < width ; x++ ) {
 			for( int y = 0 ; y < height ; y++) {
 				double color = this.getPixel(x, y) + SingleChannel2.getPixel(x, y);
 				this.setPixel(x, y, color);
 			}
 		}
 	}
 
 	public void substract(SingleChannel SingleChannel2) {
 		for( int x = 0 ; x < width ; x++ ) {
 			for( int y = 0 ; y < height ; y++) {
 				double color = this.getPixel(x, y) - SingleChannel2.getPixel(x, y);
 				this.setPixel(x, y, color);
 			}
 		}
 	}
 
 	public void multiply(SingleChannel SingleChannel2) {
 		for( int x = 0 ; x < width ; x++ ) {
 			for( int y = 0 ; y < height ; y++) {
 				double color = this.getPixel(x, y) * SingleChannel2.getPixel(x, y);
 				this.setPixel(x, y, color);
 			}
 		}
 	}
 
 	public void dynamicRangeCompression(double min, double max) {
 		double c = (MAX_CHANNEL_COLOR - 1) / Math.log(1 + max - min);		
 		for( int x = 0 ; x < width ; x++ ) {
 			for( int y = 0 ; y < height ; y++) {
 				double color = (double) (c * Math.log(1 + this.getPixel(x, y) - min));
 				this.setPixel(x, y, color);
 			}
 		}
 	}
 
 	public void negative() {
 		for( int x = 0 ; x < width ; x++ ) {
 			for( int y = 0 ; y < height ; y++) {
 				double color = this.getPixel(x, y);
 				this.setPixel(x, y, MAX_CHANNEL_COLOR - color);
 			}
 		}
 	}
 
 	public void threshold(double thresholdLimit) {
 		double blackColor = MIN_CHANNEL_COLOR;
 		double whiteColor = MAX_CHANNEL_COLOR;
 
 		for( int x = 0 ; x < width ; x++ ) {
 			for( int y = 0 ; y < height ; y++) {
 				double colorToApply = this.getPixel(x, y) > thresholdLimit? whiteColor : blackColor;
 				this.setPixel(x, y, colorToApply);
 			}
 		}
 	}
 
 	public void incrementContrast(double x1, double x2, double y1, double y2) {
 		if(!(x1 < x2) || !(y1 < y2)) {
 			throw new IllegalArgumentException("The params are incorrect because they have no order");
 		}
 
 		for( int x = 0 ; x < width ; x++ ){
 			for( int y = 0 ; y < height ; y++){
 				double thisPixel = this.getPixel(x, y);
 
 				double m = 0;
 				double b = 0;
 				if(thisPixel < x1) {
 					m = y1 / x1;
 					b = 0;
 				} else if(thisPixel > x2) {
 					m = (255 - y2) / (255 - x2);
 					b = y2 - m * x2;
 				} else {
 					m = (y2 - y1) / (x2 - x1);
 					b = y1 - m * x1;
 				}
 				double newPixel = m * thisPixel + b;
 
 				this.setPixel(x, y, newPixel);
 			}
 		}
 	}
 
 	public void equalize() {
 		int[] histData = this.getColorOccurrences();
 		double[] newSingleChannel = new double[this.channel.length];
 
 		for( int i = 0 ; i < newSingleChannel.length ; i++ ){
 				int grayLevel = truncatePixel((int)Math.floor(this.channel[i]));
 
 				double newValue = 0;
 				for( int k = 0 ; k < grayLevel ; k++){
 					newValue += histData[k];
 				}
 
 				newValue = newValue * (255.0/newSingleChannel.length);
 				newSingleChannel[i] = newValue;
 		}
 
 		this.channel = newSingleChannel;
 	}
 
 	private int[] getColorOccurrences() {
 		int[] dataset = new int[Image.GRAY_LEVEL_AMOUNT];
 
 		for( int i = 0 ; i < this.channel.length ; i++ ){
 				int grayLevel = truncatePixel((int)Math.floor(this.channel[i]));
 				dataset[grayLevel] += 1;
 		}
 
 		return dataset;
 	}
 
 	public void multiply(double scalar) {
 		for(int i = 0 ; i < this.channel.length ; i++){
 			double newValue = this.channel[i] * scalar;
 			this.channel[i] = newValue;
 		}
 	}
 
 	int truncatePixel(double notTruncatedValue) {
 		if(notTruncatedValue > MAX_CHANNEL_COLOR) {
 			return MAX_CHANNEL_COLOR;
 		} else if(notTruncatedValue < MIN_CHANNEL_COLOR) {
 			return MIN_CHANNEL_COLOR;
 		}
 		return (int)notTruncatedValue;
 	}
 
 	@Override
 	public SingleChannel clone() {
 		SingleChannel newSingleChannel = new SingleChannel(width, height);
 
 		for(int i = 0 ; i < width ; i++ ){
 			for(int j = 0 ; j < height ; j++){
 				newSingleChannel.setPixel(i, j, this.getPixel(i, j));
 			}
 		}
 		return newSingleChannel;
 	}
 
 	public void applyMask(Mask mask, int w, int h, int endW, int endH){
 		// save values first
 		int pixels = (endW - w + 1) * (endH - h + 1);
 		double[] vals = new double[pixels];
 		int i = 0;
 		for( int y = h ; y <= endH ; y++ ){
 			for( int x = w ; x <= endW ; x++){
 				vals[i] = applyPixelMask(x, y, mask);
 				i++;
 			}
 		}
 		// copy values to vector
 		i = 0;
 		for( int y = h ; y <= endH ; y++ ){
 			for( int x = w ; x <= endW ; x++){
 				channel[getWidth() * y + x] = vals[i];
 				i++;
 			}
 		}
 	}
 
 	private double applyPixelMask(int x, int y, Mask mask) {
 		double newColor = 0;
 		for(int i = - mask.getWidth() / 2 ; i <= mask.getWidth() / 2; i++) {
 			for(int j = - mask.getHeight() / 2; j <= mask.getHeight() / 2; j++) {
 				if(this.validPixel(x + i, y + j)) {
 					double oldColor = this.getPixel(x + i, y + j);
 					newColor += oldColor * mask.getValue(i, j);
 				}
 			}
 		}
 		return newColor;
 	}
 
 	public void applyMedianMask(Point maskSize, int w, int endw, int h, int endh) {
 		for( int y = h ; y <= endh ; y++ ){
 			for( int x = w ; x <= endw ; x++){
 				channel[getWidth() * y + x] = applyMedianPixelMask(x, y, maskSize);
 			}
 		}
 	}
 
 	private double applyMedianPixelMask(int x, int y, Point maskSize) {
 		TreeSet<Double> pixelsAffected = new TreeSet<Double>(); 
 		for(int i = - maskSize.x / 2 ; i <= maskSize.x / 2; i++) {
 			for(int j = - maskSize.y / 2; j <= maskSize.y / 2; j++) {
 				if(this.validPixel(x + i, y + j)) {
 					double oldColor = this.getPixel(x + i, y + j); 
 					pixelsAffected.add(oldColor);
 				}
 			}
 		}
 
 		double valueToReturn = 0;
 		int indexToReturn = pixelsAffected.size() / 2;
 		Iterator<Double> iterator = pixelsAffected.iterator();
 		for(int i = 0; iterator.hasNext(); i++) {
 			double each = iterator.next();
 			if(i == indexToReturn) {
 				valueToReturn = each;
 			}
 		}
 		return valueToReturn;
 	}
 	
 	public SingleChannel copy() {
 		SingleChannel copy = new SingleChannel(this.width, this.height);
 		copy.channel = this.channel.clone();
 		return copy;
 	}
 
 	public void applyIsotropicDiffusion(int iterations) {
 		SingleChannel auxiliarChannel = this.clone();
 		
 		for(int n = 0 ; n < iterations ; n++ ){
 			auxiliarChannel = applyIsotropicDiffusion(auxiliarChannel);
 		}
 		this.channel = auxiliarChannel.channel;
 	}
 	
 	
 	private SingleChannel applyIsotropicDiffusion(SingleChannel oldChannel) {
 		SingleChannel modifiedChannel = new SingleChannel(width, height);
 		for(int i = 0 ; i < width ; i++ ){
 			for(int j = 0 ; j < height ; j++){
 				double oldValueIJ = oldChannel.getPixel(i, j);
 				
 				double DnIij = oldValueIJ;
 				double DsIij = oldValueIJ;
 				double DeIij = oldValueIJ;
 				double DoIij = oldValueIJ;
 				
 				if(i > 0) {
 					DnIij = oldChannel.getPixel(i - 1, j);
 				}
 				if(i < width - 1) {
 					DsIij = oldChannel.getPixel(i + 1, j);
 				}
 				if(j < height - 1) {
 					DeIij = oldChannel.getPixel(i, j + 1);
 				}
 				if(j > 0) {
 					DoIij = oldChannel.getPixel(i, j - 1);						
 				}
 				
 				DnIij -= oldValueIJ;
 				DsIij -= oldValueIJ;
 				DeIij -= oldValueIJ;
 				DoIij -= oldValueIJ;
 				
 				double Cnij = 1;
 				double Csij = 1;
 				double Ceij = 1;
 				double Coij = 1;
 				
 				double resultColor = oldValueIJ + 0.25 * (DnIij*Cnij + DsIij*Csij + DeIij*Ceij + DoIij*Coij);
 				modifiedChannel.setPixel(i, j, resultColor);
 			}
 		}
 		
 		return modifiedChannel;
 	}
 
 	public void applyAnisotropicDiffusion(double lambda, int iterations, EdgeDetector bd){
 		SingleChannel auxiliarChannel = this.clone();
 		
 		for(int n = 0 ; n < iterations ; n++ ){
 			auxiliarChannel = applyAnisotropicDiffusion(auxiliarChannel, lambda, bd);
 		}
 		this.channel = auxiliarChannel.channel;
 	}
 	
 	private SingleChannel applyAnisotropicDiffusion(SingleChannel oldChannel, double lambda, EdgeDetector bd) {
 		SingleChannel modifiedChannel = new SingleChannel(width, height);
 		for(int i = 0 ; i < width ; i++ ){
 			for(int j = 0 ; j < height ; j++){
 				double oldValueIJ = oldChannel.getPixel(i, j);
 				
 				double DnIij = oldValueIJ;
 				double DsIij = oldValueIJ;
 				double DeIij = oldValueIJ;
 				double DoIij = oldValueIJ;
 				
 				if(i > 0) {
 					DnIij = oldChannel.getPixel(i - 1, j);
 				}
 				if(i < width - 1) {
 					DsIij = oldChannel.getPixel(i + 1, j);
 				}
 				if(j < height - 1) {
 					DeIij = oldChannel.getPixel(i, j + 1);
 				}
 				if(j > 0) {
 					DoIij = oldChannel.getPixel(i, j - 1);						
 				}
 				
 				DnIij -= oldValueIJ;
 				DsIij -= oldValueIJ;
 				DeIij -= oldValueIJ;
 				DoIij -= oldValueIJ;
 				
 				double Cnij = bd.g(DnIij);
 				double Csij = bd.g(DsIij);
 				double Ceij = bd.g(DeIij);
 				double Coij = bd.g(DoIij);
 				
 				double resultColor = oldValueIJ + lambda*(DnIij*Cnij + DsIij*Csij + DeIij*Ceij + DoIij*Coij);
 				modifiedChannel.setPixel(i, j, resultColor);
 			}
 		}
 		
 		return modifiedChannel;
 	}
 		
 	private void synthesize(SynthesizationFunction fn, SingleChannel ... chnls) {
 		double[] result = new double[width*height];
 		
 		for(int i = 0 ; i < channel.length ; i++ ){
 			double[] colors = new double[chnls.length+1];
 			colors[0] = this.channel[i];
 			for(int j = 1 ; j < chnls.length ; j++ ){
 				colors[j] = chnls[j-1].channel[i];
 			}
 			result[i] = fn.synth(colors);
 		}
 		this.channel = result;
 	}
 	
 	public void synthesize(SynthesizationType st, SingleChannel ... chnls){
 		if( st == SynthesizationType.MAX){
 			synthesize(new SynthesizationFunction() {
 				@Override
 				public double synth(double ... color) {
 					double max = color[0];
 					for(double d: color){
 						max = Math.max(max, d);
 					}
 					return max;
 				}
 			}, chnls);
 			return;
 		}
 		if( st == SynthesizationType.MIN){
 			synthesize(new SynthesizationFunction() {
 				@Override
 				public double synth(double ... color) {
 					double min = color[0];
 					for(double d: color){
 						min = Math.min(min, d);
 					}
 					return min;
 				}
 			}, chnls);
 			return;
 		}
 		if( st == SynthesizationType.AVG){
 			synthesize(new SynthesizationFunction() {
 				@Override
 				public double synth(double ... color) {
 					double sum = 0;
 					for(double d: color){
 						sum += d;
 					}
 					return sum/2;
 				}
 			}, chnls);
 			return;
 		}
 		if( st == SynthesizationType.ABS){
 			synthesize(new SynthesizationFunction() {
 				@Override
 				public double synth(double ... color) {
 					double sum = 0;
 					for(double d: color){
 						sum += Math.pow(d, 2);
 					}
 					return Math.sqrt(sum);
 				}
 			}, chnls);
 			return;
 		}
 		throw new IllegalStateException();
 	}
 	
 	public void globalThreshold() {
 		double globalThreshold = getGlobalThresholdValue();
 		threshold(globalThreshold);
 	}
 	
 	private double getGlobalThresholdValue() {
 		double maxDeltaThresholdAllowed = 1;
 		double currentT = 128;
 		double previousT = currentT + 2 * maxDeltaThresholdAllowed;
 		
 		while(Math.abs((currentT - previousT)) > maxDeltaThresholdAllowed) {
 			previousT = currentT;
 			currentT = getAdjustedThreshold(currentT);
 		}
 		
 		return currentT;
 	}
 	
 	private double getAdjustedThreshold(double previousThreshold) {
 		double amountOfHigher = 0;
 		double amountOfLower = 0;
 		
 		double sumOfHigher = 0;
 		double sumOfLower = 0;
 		
 		for( int x = 0 ; x < width ; x++ ) {
 			for( int y = 0 ; y < height ; y++) {
 				double aPixel = this.getPixel(x, y); 
 				if(aPixel >= previousThreshold) {
 					amountOfHigher += 1;
 					sumOfHigher += aPixel;
 				}
 				if(aPixel < previousThreshold) {
 					amountOfLower += 1;
 					sumOfLower += aPixel;
 				}
 			}
 		}
 		
 		double m1 = (1 / amountOfHigher) * sumOfHigher;
 		double m2 = (1 / amountOfLower) * sumOfLower;
 		
 		return 0.5 * (m1 + m2);
 	}
 	
 	public void otsuThreshold() {
 		double maxSigma = 0;
 		int threshold = 0;
 		double[] probabilities = getProbabilitiesOfEachColorLevel();
 		for(int i = 0; i < MAX_CHANNEL_COLOR; i++) {
 			double aSigma = getSigma(i, probabilities);
 			if(aSigma > maxSigma) {
 				maxSigma = aSigma;
 				threshold = i;
 			}
 		}
 		threshold(threshold);
 	}
 	
 	private double getSigma(int threshold, double[] probabilities) {
 		double w1 = 0;
 		double w2 = 0;
 		for(int i = 0; i < probabilities.length; i++) {
 			if(i <= threshold) {
 				w1 += probabilities[i];
 			} else {
 				w2 += probabilities[i];
 			}
 		}
 		
 		if(w1 == 0 || w2 == 0) {
 			return 0;
 		}
 		
 		double mu1 = 0;
 		double mu2 = 0;
 		for(int i = 0; i < probabilities.length; i++) {
 			if(i <= threshold) {
 				mu1 += i * probabilities[i] / w1;
 			} else {
 				mu2 += i * probabilities[i] / w2;
 			}
 		}
 		
 		double mu_t = mu1 * w1 + mu2 * w2;
 		double sigma_B = w1 * Math.pow((mu1 - mu_t), 2) + w2 * Math.pow((mu2 - mu_t), 2);
 		return sigma_B;
 	}
 	
 	private double[] getProbabilitiesOfEachColorLevel() {
 		double[] probabilities = new double[MAX_CHANNEL_COLOR + 1];
 	
 		for( int x = 0 ; x < width ; x++ ) {
 			for( int y = 0 ; y < height ; y++) {
 				int aColorPixel = (int)this.getPixel(x, y);
 				if (aColorPixel < 0) aColorPixel = 0;
 				if (aColorPixel > MAX_CHANNEL_COLOR) aColorPixel = MAX_CHANNEL_COLOR;
 				probabilities[aColorPixel] += 1;
 			}
 		}
 		for(int i = 0; i < probabilities.length; i++) {
 			probabilities[i] /= (width * height);
 		}
 		
 		return probabilities;
 	}
 
 public void zeroCross(double th){
 		
 		double[] resultChannel = new double[channel.length];
 		
 		for(int x = 0 ; x < this.getWidth() ; x++){
 			for(int y = 0 ; y < this.getHeight() ; y++){
 				
 				double max = Double.MIN_VALUE;
 				double min = Double.MAX_VALUE;
 				
 				for(int i = -1 ; i <= 1 ; i++ ){
 					for(int j = -1 ; j <= 1 ; j++ ){
 						if( validPixel(x + i, y + j) && !(i == 0 && j == 0)){
 							max = Math.max(max, this.getPixel(x + i, y + j));
 							min = Math.min(min, this.getPixel(x + i, y + j));
 						}
 					}
 				}
 				double diff = max - min;
 				if ( min < 0 && max > 0 && diff > th) {
 					resultChannel[y * this.getWidth() + x] = MAX_CHANNEL_COLOR;					
 				} else {
 					resultChannel[y * this.getWidth() + x] = MIN_CHANNEL_COLOR;
 					
 				}
 				
 			}
 		}
 		
 		this.channel = resultChannel;
 	}
 	
 	//TODO: expose in UI
 	public void zeroCrossOriginal(){
 		
 		SingleChannel chnl = new SingleChannel(this.width, this.height);
 		for(int i = 1 ; i < this.channel.length ; i++ ){
 			if( channel[i-1] < 0 && channel[i] > 0){
 				chnl.channel[i] = MAX_CHANNEL_COLOR;
 			} else if( channel[i-1] > 0 && channel[i] < 0){
 				chnl.channel[i] = MAX_CHANNEL_COLOR;
 			} else {
 				chnl.channel[i] = MIN_CHANNEL_COLOR;
 			}
 		}
 		this.channel = chnl.channel;
 	}
 	
 	public void localVarianceEvaluation(int threshold){
 		SingleChannel chnl = new SingleChannel(this.width, this.height);
 		for( int i = 1 ; i < this.channel.length ; i++ ){
 			double previous = channel[i-1];
 			double current = channel[i];
 			if( previous > 0 && current < 0 && Math.abs(previous + -1*current) > threshold ) {
 				chnl.channel[i] = MAX_CHANNEL_COLOR;
 			} else if( previous < 0 && current > 0 && Math.abs(-1*previous + current) > threshold ){
 				chnl.channel[i] = MAX_CHANNEL_COLOR;
 			} else {
 				chnl.channel[i] = MIN_CHANNEL_COLOR;
 			}
 		}
 		this.channel = chnl.channel;
 	}
 	
 	public void applyCannyEdgeDetection() {
 		List<SingleChannel> channelList = new ArrayList<SingleChannel>();
 //		for(int maskSize = 3; maskSize <= 5; maskSize += 2) {
 //			for(double sigma = 0.05; sigma <= 0.25; sigma += 0.05) {
 //				SingleChannel each = applyCannyEdgeDetection(maskSize, sigma);
 //				channelList.add(each);
 //			}
 //		}
 		for(int maskSize = 3; maskSize <= 11; maskSize += 4) {
 			double sigma = 0.04 * maskSize;
 			SingleChannel each = applyCannyEdgeDetection(maskSize, sigma);
 			channelList.add(each);
 			
 		}
 		
 		SingleChannel initialChannel = channelList.get(0); 
 		SingleChannel[] restOfChannels = channelList.subList(1, channelList.size()).toArray(new SingleChannel[channelList.size() - 1]);
 		initialChannel.synthesize(SynthesizationType.MAX, restOfChannels);
 		this.channel = initialChannel.channel;
 	}
 	
 	private void applyMask(Mask mask) {
 		applyMask(mask, 0, 0, getWidth() - 1, getHeight() - 1);
 	}
 	
 	
 	public void applyHarrisCornerDetector(int maskSize, double sigma){
 		applyMask(MaskFactory.buildGaussianMask(maskSize, sigma));
 	    TwoMaskContainer sobelOperator = MaskFactory.buildSobelMasks();
 	    double[] iX, iY;
         double[] iX2, iY2, iXiY;
         double[] hXY;
         int wh = width*height;
         
         SingleChannel Dx = this.clone();
         SingleChannel Dy = this.clone();		
 		Dx.applyMask(sobelOperator.getDXMask());
 		Dy.applyMask(sobelOperator.getDYMask());		
 		iX = Dx.channel;
 		iY = Dy.channel;
         
         iX2 = new double[iX.length];
         iY2 = new double[iY.length];
         iXiY = new double[iX.length];
         hXY = new double[iX.length];
         
         for (int i = 0 ; i < iX2.length ; i++) {
                 iX2[i] = iX[i] * iX[i];
                 iY2[i] = iY[i] * iY[i];
                 iXiY[i] = iX[i] * iY[i];
         }
 
         for (int i = 0 ; i < wh ; i++) {
                 hXY[i] = iX2[i]*iY2[i] - iXiY[i]*iXiY[i] / (iX2[i] + iY2[i] + Double.MAX_VALUE);
         }
         
         findLocalMax(hXY, width, height);
         
         this.channel = hXY;	    
 	}
 	
 	 private void findLocalMax(double[] hXY, int w, int h) {         
          for (int i = 0 ; i < w * h ; i++) {
                  if (i < w || i >= w*h - w || i % w == 0 || i % w == w - 1) {
                          hXY[i] = 0d;
                  } else {                         
                          if (!isCornerLocalMax(hXY, i, w, h)) {
                                  hXY[i] = 0;
                          } else {
                                  hXY[i] = 255;
                          }
                  }
          }
  }
  
 	 private boolean isCornerLocalMax(double[] hXY, int idx, int w, int h) {
          boolean isMax = true;
          int voffset;
          int hoffset;         
          for (int j = 0 ; j < 3 && isMax == true; j++) {
                  voffset = (j - 1) * w;
                  for (int i = 0 ; i < 3 && isMax == true ; i++) {
                          hoffset = i - 1;                         
                          if (i != 1 || j != 1) {
                                  if (hXY[idx] <= hXY[idx + voffset + hoffset]) {
                                          isMax = false;
                                  }
                          }
                  }
          }         
          return isMax;
 	 }
 	
 	 private SingleChannel applyCannyEdgeDetection(int maskSize, double sigma) {
 		SingleChannel channelToModify = clone();
 		channelToModify.applyMask(MaskFactory.buildGaussianMask(maskSize, sigma));
 		channelToModify.suppressNoMaxs();
 		
 //		double globalThresholdValue = channelToModify.getGlobalThresholdValue();
 		channelToModify.thresholdWithHysteresis(70, 120);
 		return channelToModify;
 	}
 	
 	public void suppressNoMaxs() {
 		
 		TwoMaskContainer mc = MaskFactory.buildSobelMasks();
 		SingleChannel G1 = clone();
 		G1.applyMask(mc.getDXMask());
 		SingleChannel G2 = clone();
 		G2.applyMask(mc.getDYMask());
 		
 		SingleChannel directionChannel = new SingleChannel(width, height);
 		for( int x = 0 ; x < width ; x++ ) {
 			for( int y = 0 ; y < height ; y++) {
 				double pxG1 = G1.getPixel(x, y);
 				double pxG2 = G2.getPixel(x, y);
 				double anAngle = 0;
 				if(pxG2 != 0) {
 					anAngle = Math.atan(pxG1 / pxG2);
 				}
 				anAngle *= (180 / Math.PI);
 				anAngle = Math.abs(anAngle);
 				directionChannel.setPixel(x, y, anAngle);
 			}
 		}
 		
 		G1.synthesize(SynthesizationType.ABS, G2);
 		this.channel = G1.channel;
 		
 		for( int x = 1 ; x < width - 1 ; x++ ) {
 			for( int y = 1 ; y < height - 1 ; y++) {
 				double pixel = getPixel(x, y);
 				if(pixel == MIN_CHANNEL_COLOR) {
 					continue;
 				}
 				
 				double direction = directionChannel.getPixel(x, y);
 				int[] dir1 = null;
 				int[] dir2 = null;
 				int[][] directions = {
						{0, -1},
						{0, 1},
						{1, -1},
						{-1, 1},
						{1, 0},
 						{-1, 0},
 						{1, 1},
 						{-1, -1}
 				}; 
 				if(direction >= 67.5 && direction < 112.5) {
 					dir1 = directions[0];
 					dir2 = directions[1];
 				} else if(direction >= 22.5 && direction < 67.5) {
 					dir1 = directions[2];
 					dir2 = directions[3];
 				} else if((direction >= 0 && direction < 22.5) || (direction >= 157.5 && direction < 180)) {
 					dir1 = directions[4];
 					dir2 = directions[5];
 				} else {
 					dir1 = directions[6];
 					dir2 = directions[7];
 				}
 				List<Double> neighborPixels = new ArrayList<Double>();
				for (int i = 1; i < 2; i ++) {
 					if (validPixel(x + dir1[0] * i, y + dir1[1] * i)) {
 						neighborPixels.add(getPixel(x + dir1[0] * i, y + dir1[1] * i));
 					}
 					if (validPixel(x + dir2[0] * i, y + dir2[1] * i)) {
 						neighborPixels.add(getPixel(x + dir2[0] * i, y + dir2[1] * i));
 					}
 				}
 				for (Double neighbor : neighborPixels) {
 					if (neighbor > getPixel(x, y)) {
 						setPixel(x, y, MIN_CHANNEL_COLOR);
 						break;
 					}
 				}
 			}
 		}
 	}
 	
 	public void thresholdWithHysteresis(double lowThreshold, double highThreshold) {
 		double blackColor = MIN_CHANNEL_COLOR;
 		double whiteColor = MAX_CHANNEL_COLOR;
 		
 		SingleChannel thresholdedChannelOutsider = clone();
 		for( int x = 0 ; x < width ; x++ ) {
 			for( int y = 0 ; y < height ; y++) {
 				double pixel = this.getPixel(x, y);
 				double colorToApply = pixel;
 				if(pixel < lowThreshold) {
 					colorToApply = blackColor;
 				} else if(pixel > highThreshold) {
 					colorToApply = whiteColor;
 				}
 				thresholdedChannelOutsider.setPixel(x, y, colorToApply);
 			}
 		}
 		
 		SingleChannel thresholdedChannelInBetween = thresholdedChannelOutsider.clone();
 		for( int x = 0 ; x < width ; x++ ) {
 			for( int y = 0 ; y < height; y++) {
 				double pixel = this.getPixel(x, y);
 				if(pixel >= lowThreshold && pixel <= highThreshold) {
 					boolean isEdgeNeighbor1 = y > 0 && thresholdedChannelOutsider.getPixel(x, y - 1) == whiteColor;
 					boolean isEdgeNeighbor2 = x > 0 && thresholdedChannelOutsider.getPixel(x - 1, y) == whiteColor;
 					boolean isEdgeNeighbor3 = y < height - 1 && thresholdedChannelOutsider.getPixel(x, y + 1) == whiteColor;
 					boolean isEdgeNeighbor4 = x < width - 1 && thresholdedChannelOutsider.getPixel(x + 1, y) == whiteColor;
 					if(isEdgeNeighbor1 || isEdgeNeighbor2 || isEdgeNeighbor3 || isEdgeNeighbor4) {
 						thresholdedChannelInBetween.setPixel(x, y, whiteColor);
 					} else {
 						thresholdedChannelInBetween.setPixel(x, y, blackColor);
 					}
 				}
 			}
 		}
 		
 		this.channel = thresholdedChannelInBetween.channel;
 	}
 	
 	public double applySusanPixelMask(int x, int y, Mask mask) {
 		
 		final int maxThreshold = 27;
 		int amountOfPixelSameColor = 0;
 		double centralPixel = this.getPixel(x, y);
 		for(int i = - mask.getWidth() / 2 ; i <= mask.getWidth() / 2; i++) {
 			for(int j = - mask.getHeight() / 2; j <= mask.getHeight() / 2; j++) {
 				if(this.validPixel(x + i, y + j) && mask.getValue(i, j) == 1) {
 					double eachPixel = this.getPixel(x + i, y + j);
 					if(Math.abs(centralPixel - eachPixel) < maxThreshold) {
 						amountOfPixelSameColor += 1;
 					}
 				}
 			}
 		}
 		
 		final int amountOfPixelsInMask = 37;
 		double s = 1 - (amountOfPixelSameColor / (amountOfPixelsInMask * 1.0));
 		return s;
 	}
 	
 	/*
 	 * Hough for lines...
 	 * 
 	 */
 	
 	public void houghTransformForLines(double eps, double roDiscretization,
 			double thetaDiscretization, RGBImage image, int minLines) {
 //		long time = System.currentTimeMillis();
 		Color markingColor = Color.MAGENTA;
 		double whiteColor = MAX_CHANNEL_COLOR;
 		double D = Math.max(width, height);
 		Range roRange = new Range(- Math.sqrt(2) * D, Math.sqrt(2) * D + roDiscretization);
 		Range thetaRange = new Range(-90, 90 + thetaDiscretization);
 		
 		int roSize = (int)(Math.abs(roRange.getUpperBound() - roRange.getLowerBound()) / roDiscretization);
 		int thetaSize = (int)(Math.abs(thetaRange.getUpperBound() - thetaRange.getLowerBound()) / thetaDiscretization);
 		int[][] A = new int[roSize][thetaSize];
 		
 //		time = System.currentTimeMillis() - time;
 //		System.out.println("Initialization: " + time);
 //		time = System.currentTimeMillis();
 		
 		for(int x = 0 ; x < width ; x++) {
 			for(int y = 0 ; y < height ; y++) {
 				double pixel = getPixel(x, y);
 				if(pixel == whiteColor) {
 					for(int theta = 0; theta < thetaSize; theta++) {
 						double thetaValue = thetaRange.getLowerBound() + theta * thetaDiscretization;
 						double thetaTerm = x * Math.cos(thetaValue * Math.PI / 180) - y * Math.sin(thetaValue * Math.PI / 180);
 						for(int ro = 0; ro < roSize; ro++) {
 							double roValue = roRange.getLowerBound() + ro * roDiscretization;
 							double total = roValue - thetaTerm;
 							if(Math.abs(total) < eps) {
 								A[ro][theta] += 1;
 							}
 						}
 					}
 				}
 			}
 		}
 		
 //		time = System.currentTimeMillis() - time;
 //		System.out.println("Fill matrix Buckets: " + time);
 //		time = System.currentTimeMillis();
 		
 		Set<BucketForLines> allBuckets = new HashSet<BucketForLines>();
 		for(int ro = 0; ro < roSize; ro++) {
 			for(int theta = 0; theta < thetaSize; theta++) {
 				BucketForLines newBucket = new BucketForLines(ro, theta, A[ro][theta]);
 				allBuckets.add(newBucket);
 			}
 		}
 		
 //		time = System.currentTimeMillis() - time;
 //		System.out.println("Fill matrix A: " + time);
 //		time = System.currentTimeMillis();
 		
 		List<BucketForLines> allBucketsAsList = new ArrayList<BucketForLines>(allBuckets);
 		Collections.sort(allBucketsAsList);
 		
 //		time = System.currentTimeMillis() - time;
 //		System.out.println("Sort list: " + time);
 //		time = System.currentTimeMillis();
 		for (int i = 0; i <= minLines; i++) {
 			BucketForLines b = allBucketsAsList.get(i);
 			if (b.hits < 2) {
 				break;
 			}
 			double roValue = roRange.getLowerBound() + b.ro * roDiscretization /*+ roDiscretization / 2*/;
 			double thetaValue = thetaRange.getLowerBound() + b.theta * thetaDiscretization/* + thetaDiscretization / 2*/;
 
 			for(int x = 0 ; x < width ; x++) {
 				for(int y = 0 ; y < height; y++) {
 					double thetaTerm = x * Math.cos(thetaValue * Math.PI / 180) - y * Math.sin(thetaValue * Math.PI / 180);
 					double total = roValue - thetaTerm;
 					if(Math.abs(total) < eps && validPixel(x, y)) {
 						image.setRGBPixel(x, y, markingColor.getRGB());
 					}
 				}
 			}
 		}
 		
 //		time = System.currentTimeMillis() - time;
 //		System.out.println("Draw lines: " + time);
 //		time = System.currentTimeMillis();
 
 	}
 	
 	private static class BucketForLines implements Comparable<BucketForLines> {
 		double ro;
 		double theta;
 		int hits;
 
 		public BucketForLines(double ro, double theta, int hits) {
 			this.ro = ro;
 			this.theta = theta;
 			this.hits = hits;
 		}
 		
 		@Override
 		public boolean equals(Object obj) {
 			return ro == ((BucketForLines)obj).ro && theta == ((BucketForLines)obj).theta;
 		}
 		
 		@Override
 		public int hashCode() {
 			return (int)(3 * ro + 5 * theta);
 		}
 
 		@Override
 		public int compareTo(BucketForLines obj) {
 			return obj.hits - hits;
 		}
 		
 		@Override
 		public String toString() {
 			return "Ro: " + ro + " Theta: " + theta + " Hits: " + hits;
 		}
 			
 	}
 	
 	
 	/*
 	 * Hough for circles...
 	 * 
 	 */
 	
 	public void houghTransformForCircles(double eps, double aDiscretization, double bDiscretization,
 			double rDiscretization, RGBImage image, int minCircles) {
 //		long time = System.currentTimeMillis();
 		
 		Color markingColor = Color.MAGENTA;
 		double whiteColor = MAX_CHANNEL_COLOR;
 		Range aRange = new Range(0, width);
 		Range bRange = new Range(0, height);
 		double maxRad = Math.min(width, height) / 2;
 		Range rRange = new Range(5, maxRad);
 		
 		int aSize = (int)(Math.abs(aRange.getUpperBound() - aRange.getLowerBound()) / aDiscretization);
 		int bSize = (int)(Math.abs(bRange.getUpperBound() - bRange.getLowerBound()) / bDiscretization);
 		int rSize = (int)(Math.abs(rRange.getUpperBound() - rRange.getLowerBound()) / rDiscretization);
 		int[][][] A = new int[aSize][bSize][rSize];
 		
 //		time = System.currentTimeMillis() - time;
 //		System.out.println("Initialization: " + time);
 //		time = System.currentTimeMillis();
 		
 		for(int x = 0 ; x < width ; x++) {
 			for(int y = 0 ; y < height ; y++) {
 				double pixel = getPixel(x, y);
 				if(pixel == whiteColor) {
 					for(int a = 0; a < aSize; a++) {
 						double aValue = aRange.getLowerBound() + a * aDiscretization;
 						double aTerm = Math.pow(x - aValue, 2);
 						for(int b = 0; b < bSize; b++) {
 							double bValue = bRange.getLowerBound() + b * bDiscretization;
 							double bTerm = Math.pow(y - bValue, 2);
 							for(int r = 0; r < rSize; r++) {
 								double rValue = rRange.getLowerBound() + r * rDiscretization;
 								double rTerm = Math.pow(rValue, 2);
 								double total = rTerm - aTerm - bTerm;
 								if(Math.abs(total) < eps) {
 //									System.out.println("Por el punto: " + "(" + x + ", " + y + ")" + " pasa el: " + aValue + ", " + bValue + ", " + rValue);
 									A[a][b][r] += 1;
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		
 //		time = System.currentTimeMillis() - time;
 //		System.out.println("Fill matrix Buckets: " + time);
 //		time = System.currentTimeMillis();
 		
 		Set<BucketForCircles> allBuckets = new HashSet<BucketForCircles>();
 		for(int a = 0; a < aSize; a++) {
 			for(int b = 0; b < bSize; b++) {
 				for(int r = 0; r < rSize; r++) {
 					if(A[a][b][r] > 0) {
 						BucketForCircles newBucket = new BucketForCircles(a, b, r, A[a][b][r]);
 						allBuckets.add(newBucket);
 					}
 				}
 			}
 		}
 		
 //		time = System.currentTimeMillis() - time;
 //		System.out.println("Fill matrix A: " + time);
 //		time = System.currentTimeMillis();
 		if(allBuckets.isEmpty())
 			return;
 		
 		List<BucketForCircles> allBucketsAsList = new ArrayList<BucketForCircles>(allBuckets);
 		Collections.sort(allBucketsAsList);
 		
 //		time = System.currentTimeMillis() - time;
 //		System.out.println("Sort list: " + time);
 //		time = System.currentTimeMillis();
 		
 		for (int i = 0 ; i <= minCircles; i ++) {
 			BucketForCircles b = allBucketsAsList.get(i);
 			double aValue = aRange.getLowerBound() + b.a * aDiscretization/* + aDiscretization / 2*/;
 			double bValue = bRange.getLowerBound() + b.b * bDiscretization /*+ bDiscretization / 2*/;
 			double rValue = rRange.getLowerBound() + b.r * rDiscretization/* + rDiscretization / 2*/;
 //			System.out.println("Draw: " + aValue + " - " + bValue + " - " + rValue);
 			for(int x = 0 ; x < width ; x++) {
 				for(int y = 0 ; y < height; y++) {
 					double aTerm = Math.pow(x - aValue, 2);
 					double bTerm = Math.pow(y - bValue, 2);
 					double rTerm = Math.pow(rValue, 2);
 					double total = rTerm - aTerm - bTerm;
 					if(Math.abs(total) < 10 * eps && validPixel(x, y)) {
 						image.setRGBPixel(x, y, markingColor.getRGB());
 					}
 				}
 			}
 		}
 		
 //		time = System.currentTimeMillis() - time;
 //		System.out.println("Draw lines: " + time);
 //		time = System.currentTimeMillis();
 	}
 	
 	private static class BucketForCircles implements Comparable<BucketForCircles> {
 		double a;
 		double b;
 		double r; 
 		int hits;
 
 		public BucketForCircles(double a, double b, double r, int hits) {
 			this.a = a;
 			this.b = b;
 			this.r = r;
 			this.hits = hits;
 		}
 		
 		@Override
 		public boolean equals(Object obj) {
 			boolean equalA = a == ((BucketForCircles)obj).a;
 			boolean equalB = b == ((BucketForCircles)obj).b;
 			boolean equalR = r == ((BucketForCircles)obj).r;
 			return equalA && equalB && equalR;
 		}
 		
 		@Override
 		public int hashCode() {
 			return (int)(3 * a + 5 * b + 7 * r);
 		}
 
 		@Override
 		public int compareTo(BucketForCircles obj) {
 			return obj.hits - hits;
 		}
 		
 		@Override
 		public String toString() {
 			return "A: " + a + " B: " + b + " R: " + r + " Hits: " + hits;
 		}
 			
 	}
 	
 }
