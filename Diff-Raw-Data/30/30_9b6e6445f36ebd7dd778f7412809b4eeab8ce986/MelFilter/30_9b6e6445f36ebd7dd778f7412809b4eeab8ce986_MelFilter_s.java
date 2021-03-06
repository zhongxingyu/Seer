 /**
  * [[[copyright]]]
  */
 
 package edu.cmu.sphinx.frontend;
 
 import edu.cmu.sphinx.util.SphinxProperties;
 import edu.cmu.sphinx.util.Timer;
 
 import java.io.IOException;
 
 /**
  * Define a filter used by the MelFilterbank class.  MelFilterbank
  * uses this class to create filters and operate on them.  
  */
 public class MelFilter {
 
     private double[] weight;
 
     private int initialFreqIndex;
 
     public MelFilter() {
     }
 
     /**
      * Constructs a filter from the parameters.
      *
      * In the current implementation, the filter is a bandpass filter
      * with a triangular shape.  We're given the left and right edges
      * and the center frequency, so we can determine the right and
      * left slopes, which could be not only assymmetric but completely
      * different. We're also given the initial frequency, which may or
      * may not coincide with the left edge, and the frequency step.
      *
      * @param leftEdge the filter's lowest passing frequency
      * @param centerFreq the filter's center frequency
      * @param rightEdge the filter's highest passing frequency
      * @param initialFreq the first frequency bin in the pass band
      * @param deltaFreq the step in the frequency axis between
      *               frequency bins
      * @param samplingRate sampling frequency
      *
      */
     public void createMelFilter(double leftEdge, 
 				double centerFreq, 
 				double rightEdge, 
 				double initialFreq, 
 				double deltaFreq,
 				int samplingRate) {
 
 	double filterHeight;
 	double leftSlope;
 	double rightSlope;
 	double currentFreq;
 	int indexFilterWeight;
 	int numberElementsWeightField;
 
 	/**
 	 * Let's compute the number of elements we need in the
 	 * <code>weight</code> field by computing how many frequency
 	 * bins we can fit in the current frequency range.
 	 */
 	numberElementsWeightField = 
 	    (int) Math.round((rightEdge - leftEdge) / deltaFreq + 1);
 	/**
 	 * Initialize the <code>weight</code> field.
 	 */
 	weight = new double[numberElementsWeightField];
 
 	/**
 	 * Let's make the filter area equal to 1.
 	 */
 	filterHeight = 2.0f / (rightEdge - leftEdge);
 
 	/**
 	 * Now let's compute the slopes based on the height.
 	 */
 	leftSlope = filterHeight / (centerFreq - leftEdge);
	rightSlope = filterHeight / (rightEdge - centerFreq);
 
 	/** 
 	 * Now let's compute the weight for each frequency bin.  We
 	 * initialize and update two variables in the <code>for</code>
 	 * line.
 	 */
 	for (currentFreq = initialFreq, indexFilterWeight = 0;
 	     currentFreq <= rightEdge; 
 	     currentFreq += deltaFreq, indexFilterWeight++) {
 	    /**
 	     * A straight line that contains point <b>(x0, y0)</b> and
 	     * has slope <b>m</b> is defined by:
 	     *
 	     * <b>y = y0 + m * (x - x0)</b>
 	     *
 	     * This is used for both "sides" of the triangular filter
 	     * below.
 	     */
 	    if (currentFreq < centerFreq) {
 		weight[indexFilterWeight] = leftSlope 
 		    * (currentFreq - leftEdge);
 	    } else {
 		weight[indexFilterWeight] = filterHeight + rightSlope 
 		    * (currentFreq - centerFreq);
 	    }
 	}
 	/**
 	 * Initializing frequency related fields.
 	 */
 	this.initialFreqIndex = (int) Math.round
 	    (initialFreq / deltaFreq);
     }
 
     /**
      * Compute the output of a filter. We're given a power spectrum,
      * to which we apply the appropriate weights.
      *
      * @param spectrum the input power spectrum to be filtered
      *
      * @return the filtered value, in fact a weighted average of power in
      *          the frequency range of the filter pass band
      */
     public double filterOutput(double[] spectrum) {
 	double output = 0.0f;
 	int indexSpectrum;
 
 	for (int i = 0; i < this.weight.length; i++) {
 	    indexSpectrum = this.initialFreqIndex + i;
 	    if (indexSpectrum < spectrum.length) {
		output += spectrum[this.initialFreqIndex + i] * this.weight[i];
 	    }
 	}
 	return output;
     }
 }
