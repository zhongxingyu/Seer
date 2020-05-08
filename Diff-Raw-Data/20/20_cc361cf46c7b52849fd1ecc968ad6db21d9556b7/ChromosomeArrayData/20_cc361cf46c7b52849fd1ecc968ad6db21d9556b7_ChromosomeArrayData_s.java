 /*
 $Revision$
 $Date$
 
 The Web CGH Software License, Version 1.0
 
 Copyright 2003 RTI. This software was developed in conjunction with the
 National Cancer Institute, and so to the extent government employees are
 co-authors, any rights in such works shall be subject to Title 17 of the
 United States Code, section 105.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 
 1. Redistributions of source code must retain the above copyright notice, this 
 list of conditions and the disclaimer of Article 3, below. Redistributions in 
 binary form must reproduce the above copyright notice, this list of conditions 
 and the following disclaimer in the documentation and/or other materials 
 provided with the distribution.
 
 2. The end-user documentation included with the redistribution, if any, must 
 include the following acknowledgment:
 
 "This product includes software developed by the RTI and the National Cancer 
 Institute."
 
 If no such end-user documentation is to be included, this acknowledgment shall 
 appear in the software itself, wherever such third-party acknowledgments 
 normally appear.
 
 3. The names "The National Cancer Institute", "NCI", 
 Research Triangle Institute, and "RTI" must not be used to endorse or promote 
 products derived from this software.
 
 4. This license does not authorize the incorporation of this software into any 
 proprietary programs. This license does not authorize the recipient to use any 
 trademarks owned by either NCI or RTI.
 
 5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, 
 (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
 FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE
 NATIONAL CANCER INSTITUTE, RTI, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 package org.rti.webcgh.domain;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import org.rti.webcgh.util.SystemUtils;
 
 /**
  * Aggregation of <code>ArrayDatum</code> objects from same chromosome.
  * @author dhall
  *
  */
 public class ChromosomeArrayData implements Serializable {
     
     /** Serialized version ID. */
     private static final long serialVersionUID = 
 		SystemUtils.getLongApplicationProperty("serial.version.uid");
     
     /**
      * Extra "headroom" is added to the min and max value
      * for chromosome alteration data so that alterations
      * are not on the absolute top and bottom of plot.
      * This is a multiplier that is used to calculate the
      * headroom.
      */
     private static final float HEAD_ROOM_MULTIPLE = (float) 0.1;
     
     // ======================================
     //         Attributes
     // ======================================
     
     /** Identifier used for persistence. */
     private Long id = null;
     
     /**
      * Identifier of bioassay data to which this belongs.
      * A referece to the actual bioassay data object is not included
      * to support controling memory usage.  Chromosome array
      * data may be loaded apart from the bioassay data object
      * that owns it.
      */
     private Long bioAssayDataId = null;
     
     /** Array data from chromosome. This list is kept sorted. */
     private SortedSet<ArrayDatum> arrayData = new TreeSet<ArrayDatum>(
     		new ArrayDatum.ChromosomeLocationComparator());
     
     /** Chromosome number. */
     private short chromosome = -1;
     
     /**
      * Minimum value.  This value is the sum of
      * the value and error of some <code>ArrayDatum</code>
      * object.
      */
     private float minValue = Float.NaN;
     
     /**
      * Maximum value.  This value is the sum of
      * the value and error of some <code>ArrayDatum</code>
      * object.
      */
     private float maxValue = Float.NaN;
     
     /** Chromosome alterations. */
     private List<AnnotatedGenomeFeature> chromosomeAlterations = null;
     
     // =================================
     //    Getters/setters
     // =================================
     
     /**
      * Get maximum value.  This value is the sum of
      * the value and error of some <code>ArrayDatum</code>
      * object.
      * @return Maximum value
      */
     public final float getMaxValue() {
 		return maxValue;
 	}
 
     
     /**
      * Get chromosome alterations.
      * @return Chromosome alterations.
      */
     public final List<AnnotatedGenomeFeature> getChromosomeAlterations() {
 		return chromosomeAlterations;
 	}
 
 
     /**
      * Set chromosome alterations.
      * @param chromosomeAlteration Chromosome alterations.
      */
 	public final void setChromosomeAlterations(
 			final List<AnnotatedGenomeFeature> chromosomeAlteration) {
 		this.chromosomeAlterations = chromosomeAlteration;
 		if (chromosomeAlteration != null && chromosomeAlteration.size() > 0) {
 			this.minValue = Float.MAX_VALUE;
 			this.maxValue = Float.MIN_VALUE;
 			for (AnnotatedGenomeFeature f : chromosomeAlteration) {
 				if (f.getQuantitation() < this.minValue) {
 					this.minValue = f.getQuantitation();
 				}
 				if (f.getQuantitation() > this.maxValue) {
 					this.maxValue = f.getQuantitation();
 				}
 			}
 			float headroom = HEAD_ROOM_MULTIPLE
 					* (this.maxValue - this.minValue);
 			this.minValue -= headroom;
 			this.maxValue += headroom;
 		}
 	}
 
 
 	/**
      * Get minimum value.  This value is the sum of
      * the value and error of some <code>ArrayDatum</code>
      * object.
      * @return Minimum value
      */
 	public final float getMinValue() {
 		return minValue;
 	}
 
 	/**
      * Get identifier of bioassay data
      * to which this chromosome array data belongs.
      * @return Bioassay identifier
      */
     public final Long getBioAssayDataId() {
         return bioAssayDataId;
     }
 
     /**
      * Set identifier of bioassay data
      * to which this chromosome array data belongs.
      * @param bioAssayDataId Bioassay identifier
      */
     public final void setBioAssayDataId(final Long bioAssayDataId) {
         this.bioAssayDataId = bioAssayDataId;
     }
 
     /**
      * Get array data from chromosome.  The list
      * returned will be sorted.
      * @return Array data
      */
     public final List<ArrayDatum> getArrayData() {
         return new ArrayList<ArrayDatum>(this.arrayData);
     }
 
     /**
      * Set array data from chromosome.
      * @param arrayData Array data
      */
     public final void setArrayData(final List<ArrayDatum> arrayData) {
         for (ArrayDatum ad : arrayData) {
         	this.add(ad);
         }
     }
 
     /**
      * Get chromosome number.
      * @return Chromosome number.
      */
     public final short getChromosome() {
         return chromosome;
     }
 
     /**
      * Set chromosome number.
      * @param chromosome Chromosome number
      */
     public final void setChromosome(final short chromosome) {
         this.chromosome = chromosome;
     }
 
     /**
      * Get identifier used for persistence.
      * @return Identifier
      */
     public final Long getId() {
         return id;
     }
 
     /**
      * Set identifier used for persistence.
      * @param id Identifier
      */
     public final void setId(final Long id) {
         this.id = id;
     }
 
     
     // ==================================
     //         Constructors
     // ==================================
     
     /**
      * Default constructor.
      */
     public ChromosomeArrayData() {
         
     }
     
     /**
      * Constructor.
      * @param chromosome Chromosome number.
      */
     public ChromosomeArrayData(final short chromosome) {
         this.chromosome = chromosome;
     }
     
     
     // ===========================================
     //      Other public methods
     // ===========================================
     
     /**
      * Add an array datum.
      * @param arrayDatum An array datum
      */
     public final void add(final ArrayDatum arrayDatum) {
         if (!this.onChromosome(arrayDatum.getReporter())) {
             throw new IllegalArgumentException(
                     "Array datum not on same chromosome");
         }
         this.arrayData.add(arrayDatum);
         this.updateMinAndMax(arrayDatum);
     }
     
     
     /**
      * Return all array data between the given chromosomal
      * location endpoints inclusive.
      * @param from Left endpoint
      * @param to Right endpoint
      * @return Array data
      */
     public final List<ArrayDatum> getArrayData(
             final long from, final long to) {
         
         // Check args
         if (from > to) {
             throw new IllegalArgumentException("From cannot be larget than to");
         }
         
         // Find indices of endpoints of data that fall within given
         // range
         Reporter r1 = new Reporter(null, this.chromosome, from);
         Reporter r2 = new Reporter(null, this.chromosome, to);
         ArrayDatum a1 = new ArrayDatum(Float.NaN, r1);
         ArrayDatum a2 = new ArrayDatum(Float.NaN, r2);
         Comparator<ArrayDatum> c =
         	new ArrayDatum.ChromosomeLocationComparator();
         List<ArrayDatum> arrayDataList =
         	new ArrayList<ArrayDatum>(this.arrayData);
         int p = Collections.binarySearch(arrayDataList, a1, c);
         int q = Collections.binarySearch(arrayDataList, a2, c);
         if (p < 0) {
             p = -p - 1;
         }
         if (q < 0) {
             q = -q - 2;
         }
         assert p <= q && p >= 0 && q < this.arrayData.size();
         
         // Fill set to be returned
         List<ArrayDatum> included = new ArrayList<ArrayDatum>();
         for (int i = p; i <= q; i++) {
             included.add(arrayDataList.get(i));
         }
         
         return included;
     }
     
     
     /**
      * Is given reporter on this chromosome?
      * @param reporter A reporter
      * @return T/F
      */
     public final boolean onChromosome(final Reporter reporter) {
         return reporter.getChromosome() == this.chromosome;
     }
     
     
     /**
      * Get all reporters.
      * @return Reporters
      */
     public final SortedSet<Reporter> getReporters() {
         SortedSet<Reporter> reporters = new TreeSet<Reporter>();
         for (ArrayDatum d : this.arrayData) {
             reporters.add(d.getReporter());
         }
         return reporters;
     }
     
     
     /**
      * Get inferred chromosome size.  This will be equal to
      * the position of the rightmost reporter.
      * @return Inferred chromosome size
      */
     public final long inferredChromosomeSize() {
         long size = (long) 0;
         if (this.chromosomeAlterations != null) {
        	size = this.chromosomeAlterations.get(
        			this.chromosomeAlterations.size() - 1).getEndLocation();
         } else {
 	        int n = this.arrayData.size();
 	        if (n > 0) {
 	        	size = this.arrayData.last().getReporter().getLocation();
 	        }
         }
         return size;
     }
     
     
     /**
      * Find minimum value.  If <code>includeError</code>
      * is <code>true</code>, then method looks for minimum
      * <code>value</code> - <code>error</code>.
      * @param includeError Should errors be subtracted
      * from values?
      * @return Minimum value or NaN if data are empty
      */
     public final float minValue(final boolean includeError) {
         if (this.arrayData.size() < 1) {
             return Float.NaN;
         }
         float min = Float.MAX_VALUE;
         for (ArrayDatum datum : this.arrayData) {
             float candidateMin = datum.getValue();
             if (includeError) {
                 candidateMin -= datum.getError() / (float) 2.0;
             }
             if (candidateMin < min) {
                 min = candidateMin;
             }
         }
         return min;
     }
     
     
     /**
      * Find maximum value.  If <code>includeError</code>
      * is <code>true</code>, then method looks for maximum
      * <code>value</code> + <code>error</code>.
      * @param includeError Should errors be added
      * to values?
      * @return Maximum value or NaN if data are empty
      */
     public final float maxValue(final boolean includeError) {
         if (this.arrayData.size() < 1) {
             return (float) Float.NaN;
         }
         float max = Float.MIN_VALUE;
         for (ArrayDatum datum : this.arrayData) {
             float candidateMax = datum.getValue();
             if (includeError) {
                 candidateMax += datum.getError() / (float) 2.0;
             }
             if (candidateMax > max) {
                 max = candidateMax;
             }
         }
         return max;
     }
     
     
     /**
      * Get an iterator for altered chromosomal
      * segments which are identified de novo.
      * @param threshold Threshold value for determining if array data
      * are in altered segment.
      * @param interpolateEndPoints If <code>false</code>, then
      * segment endpoints are set to be the outermost reporter locations
      * within a segment.  If <code>true</code>, then endpoints
      * are interpolated distally to the points where linearly descending
      * lines drawn to the next data point cross the threshold.
      * @param alterationType Alteration type
      * @return Iterator for altered segments.  The <code>quantitation</code>
 	 * of this feature will be the weighted average value of the segment.
      */
     public final Iterator<AnnotatedGenomeFeature> alteredSegmentIterator(
     		final float threshold,
     		final boolean interpolateEndPoints,
     		final AnnotationType alterationType) {
     	return new DeNovoAlteredSegmentIterator(threshold, interpolateEndPoints,
     			new ArrayList<ArrayDatum>(this.arrayData), this.chromosome,
     			alterationType);
     }
     
     
     /**
      * Get an iterator for altered chromosomal segments which
      * have been previously generated by another agent.
      * @param alterationType Type of alteration
      * @return Iterator for altered segments.
      */
     public final Iterator<AnnotatedGenomeFeature> alteredSegmentIterator(
     		final AnnotationType alterationType) {
     	Iterator<AnnotatedGenomeFeature> it = null;
     	if (this.chromosomeAlterations != null
     			&& this.chromosomeAlterations.size() > 0) {
     		AnnotatedGenomeFeature f = this.chromosomeAlterations.get(0);
     		if (alterationType == f.getAnnotationType()) {
     			it = this.chromosomeAlterations.iterator();
     		}
     	}
     	return it;
     }
     
     
     /**
      * Update <code>minValue</code> and
      * <code>maxValue</code>.
      * @param datum An array datum that may
      * or may not contain the maximum or
      * minimum value.
      */
     private void updateMinAndMax(final ArrayDatum datum) {
     	if (!Float.isNaN(datum.getValue())) {
     		float candidateMin = datum.getValue();
     		float candidateMax = candidateMin;
     		if (!Float.isNaN(datum.getError())) {
     			float error = datum.getError();
     			candidateMin -= error;
     			candidateMax += error;
     		}
     		if (Float.isNaN(this.minValue)
     				|| candidateMin < this.minValue) {
     			this.minValue = candidateMin;
     		}
     		if (Float.isNaN(this.maxValue)
     				|| candidateMax > this.maxValue) {
     			this.maxValue = candidateMax;
     		}
     	}
     }
     
     
     // ==============================
     //     Helper classes
     // ==============================
     
     /**
      * Iterator for altered genome segments.  These may
      * be amplifications, deletions, or LOH segments.
      * The segments are identified de novo--i.e., they
      * have not already been pre-computed.
      */
     static class DeNovoAlteredSegmentIterator
     implements Iterator<AnnotatedGenomeFeature> {
     	
     	/**
     	 * Threshold for determining if an array datum
     	 * is in an altered segment.
     	 */
     	private final float threshold;
     	
     	
     	/**
     	 * If <code>false</code>, then
 	     * segment endpoints are set to be the outermost reporter locations
 	     * within an segment.  If <code>true</code>, then endpoints
 	     * are interpolated distally to the points where linearly descending
 	     * lines drawn to the next data point cross the threshold.
     	 */
     	private final boolean interpolateEndPoints;
     	
     	/** Loop index whose state must be retained between method calls. */
     	private int i = 0;
     	
     	/** Next segment. */
     	private AnnotatedGenomeFeature nextSeg = null;
     	
     	/**
     	 * Data in which to identify altered segments.  This
     	 * list must be sorted on chromosome location.
     	 */
     	private final List<ArrayDatum> data;
     	
     	/** Chromosome number. */
     	private final short chromosome;
     	
     	/** Segment type. */
     	private final AnnotationType alterationType;
     	
     	
     	/**
     	 * Constructor.
     	 * @param threshold Threshold value for determining if
     	 * an array datum is in an altered segment.
 	     * @param interpolateEndPoints If <code>false</code>, then
 	     * segment endpoints are set to be the outermost reporter locations
 	     * within an segment.  If <code>true</code>, then endpoints
 	     * are interpolated distally to the points where linearly descending
 	     * lines drawn to the next data point cross the threshold.
 	     * @param data Data in which to search for altered segments.  The
 	     * list must be sorted on chromosome location.
 	     * @param chromosome Chromosome number
 	     * @param alterationType Alteration type
     	 */
     	DeNovoAlteredSegmentIterator(final float threshold,
     			final boolean interpolateEndPoints,
     			final List<ArrayDatum> data, final short chromosome,
     			final AnnotationType alterationType) {
     		this.threshold = threshold;
     		this.interpolateEndPoints = interpolateEndPoints;
     		this.data = data;
     		this.chromosome = chromosome;
     		this.alterationType = alterationType;
     		this.advance();
     	}
     	
     	/**
     	 * Return next feature.  The <code>quantitation</code>
     	 * of this feature will be the weighted average value
     	 * of the segment.
     	 * @return Next feature.
     	 */
     	public AnnotatedGenomeFeature next() {
     		AnnotatedGenomeFeature next = this.nextSeg;
     		this.advance();
     		return next;
     	}
     	
     	
     	/**
     	 * Are there any more features?
     	 * @return T/F
     	 */
     	public boolean hasNext() {
     		return this.nextSeg != null;
     	}
     	
     	
     	/**
     	 * Remove method that does nothing.
     	 */
     	public void remove() {
     		
     	}
     	
     	
     	/**
     	 * Advance state to next segment.
     	 */
     	private void advance() {
     		this.nextSeg = null;
     		int startIdx = -1;
         	int endIdx = -1;
     		while (this.i < this.data.size() && this.nextSeg == null) {
     			ArrayDatum datum = this.data.get(this.i);
     			if (this.inSegment(datum)) {
         			if (startIdx < 0) {
         				startIdx = i;
         			}
         			endIdx = i;
         		}
     			if (!this.inSegment(datum) || i == this.data.size() - 1) {
         			if (startIdx >= 0) {
         				this.nextSeg = newSegment(startIdx, endIdx);
         			}
     			}
     			this.i++;
     		}
     	}
     	
     	
     	/**
     	 * Generate new altered segment.
     	 * @param startIdx Start index of segment in data.
     	 * @param endIdx End index of segment in data.
     	 * @return A new altered segment
     	 */
     	private AnnotatedGenomeFeature newSegment(
     			final int startIdx, final int endIdx) {
 			ArrayDatum startDatum = this.data.get(startIdx);
 			ArrayDatum endDatum = this.data.get(endIdx);
 			ArrayDatum startInterpolatedDatum = null;
 			ArrayDatum endInterpolatedDatum = null;
 			if (this.interpolateEndPoints) {
 				if (startIdx > 0) {
 					startInterpolatedDatum =
 						ArrayDatum.generateIntermediate(
 							this.data.get(startIdx - 1),
 							startDatum, this.threshold);
 				}
 				if (endIdx < this.data.size() - 1) {
 					endInterpolatedDatum =
 						ArrayDatum.generateIntermediate(
 							endDatum, this.data.get(endIdx + 1),
 							this.threshold);
 				}
 			}
         				
 			// Get weighted average value
 			long start = startDatum.getReporter().getLocation();
 			long end = endDatum.getReporter().getLocation();
 			float sum = (float) 0.0;
 			if (startIdx == endIdx
 					&& startInterpolatedDatum == null
 					&& endInterpolatedDatum == null) {
 				sum = startDatum.getValue();
 			} else {
 				for (int j = startIdx; j < endIdx; j++) {
 					sum += this.weightedAvg(this.data.get(j),
 							this.data.get(j + 1));
 				}
 			}
 			if (startInterpolatedDatum != null) {
 				sum += this.weightedAvg(startInterpolatedDatum,
 						startDatum);
 				start = startInterpolatedDatum.getReporter().
 					getLocation();
 			}
 			if (endInterpolatedDatum != null) {
 				sum += this.weightedAvg(endDatum,
 						endInterpolatedDatum);
 				end = endInterpolatedDatum.getReporter().
 					getLocation();
 			}
 			float mean = Float.NaN;
 			if (start == end) {
 				mean = sum;
 			} else {
 				mean = sum / (float) (end - start);
 			}
 			
 			return new AnnotatedGenomeFeature(this.chromosome, start, end,
 					this.alterationType, mean);
     	}
     	
     	
     	/**
     	 * Is given array datum in an alteration?
     	 * @param datum An array datum.
     	 * @return T/F
     	 */
     	private boolean inSegment(final ArrayDatum datum) {
     		boolean in = false;
     		if (this.alterationType == AnnotationType.AMPLIFIED_SEGMENT
     				|| this.alterationType == AnnotationType.LOH_SEGMENT) {
     			in = datum.getValue() >= this.threshold;
     		} else if (this.alterationType == AnnotationType.DELETED_SEGMENT) {
     			in = datum.getValue() < this.threshold;
     		}
     		return in;
     	}
     	
         /**
          * Find average value of two datum weighted (i.e., multiplied) by
          * the distance between the two reporters.
          * @param d1 First datum
          * @param d2 Second datum
          * @return Weighted average value
          */
         private float weightedAvg(final ArrayDatum d1, final ArrayDatum d2) {
     		float avg = (d1.getValue() + d2.getValue()) / (float) 2.0;
     		long gap = d2.getReporter().getLocation()
     			- d1.getReporter().getLocation();
     		return (float) ((double) avg * (double) gap);
         }
     }
 }
