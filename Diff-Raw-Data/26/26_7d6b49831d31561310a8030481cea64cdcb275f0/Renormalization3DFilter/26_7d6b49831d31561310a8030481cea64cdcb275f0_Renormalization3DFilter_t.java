 /* ***** BEGIN LICENSE BLOCK *****
  * 
  * Copyright (c) 2011 Colin J. Fuller
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  * 
  * ***** END LICENSE BLOCK ***** */
 
 package edu.stanford.cfuller.imageanalysistools.filter;
 
 import edu.stanford.cfuller.imageanalysistools.image.Histogram;
 import edu.stanford.cfuller.imageanalysistools.image.Image;
 import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
 
 /**
 * A filter that performs prefiltering on a 3D image to aid in segmentation by normalizing for local variations in image intensity.
  * <p>
  * The argument to the apply method should be the image to be filtered.
  * <p>
  * This filter does not use a reference Image.
  * 
  * 
  * @author Colin J. Fuller
  *
  */
 public class Renormalization3DFilter extends Filter {
 
 	/* (non-Javadoc)
 	 * @see edu.stanford.cfuller.imageanalysistools.filter.Filter#apply(edu.stanford.cfuller.imageanalysistools.image.Image)
 	 */
 	@Override
 	public void apply(Image im) {
 		
 		Image mean = new Image(im);
 
 		VariableSizeMeanFilter VSMF = new VariableSizeMeanFilter();
 		VSMF.setBoxSize(5);
 		VSMF.apply(mean);
 		
         KernelFilterND kf = new KernelFilterND();
         double[] d = {0.1, 0.2, 0.4, 0.2, 0.1};
         kf.addDimensionWithKernel(edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate.X, d);
         kf.addDimensionWithKernel(edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate.Y, d);
         kf.addDimensionWithKernel(edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate.Z, d);
         
         LaplacianFilterND lfnd = new LaplacianFilterND();
         ZeroPointFilter zpf = new ZeroPointFilter();
         
         kf.apply(mean);
         
         
         kf.setParameters(this.params);
         lfnd.setParameters(this.params);
         zpf.setParameters(this.params);
 
         Image lf = new Image(mean);
         
         lfnd.apply(lf);
         zpf.apply(lf);
         kf.apply(lf);
         
 		float min = Float.MAX_VALUE;
 		float max = 0;
 		
 		Histogram h = new Histogram(im);
 		
 		for (ImageCoordinate ic : im) {
 			float value = im.getValue(ic)/(1+lf.getValue(ic) + mean.getValue(ic));
 			if (value < min) {min = value;}
 			if (value > max) {max = value;}
 			im.setValue(ic, value);
 		}
 		
 		for (ImageCoordinate ic : im) {
 			im.setValue(ic, (im.getValue(ic) - min)/(max-min)*h.getMaxValue());
 		}
 		
 		kf.apply(im);
 
 		
 	}
 
 }
