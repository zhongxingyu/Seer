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
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import org.rti.webcgh.core.WebcghSystemException;
 import org.rti.webcgh.util.StringUtils;
 import org.rti.webcgh.util.SystemUtils;
 import org.rti.webgenome.client.BioAssayDTO;
 import org.rti.webgenome.client.BioAssayDataConstraints;
 import org.rti.webgenome.client.ExperimentDTO;
 
 /**
  * Represents a microarray experiment.  Essentially this class is
  * an aggregation of <code>BioAssay</code> objects.
  * @author dhall
  *
  */
 public class Experiment implements Serializable {
     
     /** Serialized version ID. */
     private static final long serialVersionUID = 
 		SystemUtils.getLongApplicationProperty("serial.version.uid");
     
     // ======================================
     //         Attributes
     // ======================================
     
     /** Identifier used for persistence. */
     private Long id = null;
     
     /** Name of experiment. */
     private String name = null;
     
     /** Identifier of experiment in source database. */
     private String sourceDbId = null;
     
     /** Bioassays performed during experiment. */
     private Set<BioAssay> bioAssays = new HashSet<BioAssay>();
     
     /** Quantitation type. */
     private QuantitationType quantitationType =
     	QuantitationType.LOG_2_RATIO_FOLD_CHANGE;
     
     /** Organism. */
     private Organism organism = null;
     
     /**
      * Can analytic operations be performed on
      * this experiment?
      */
     private boolean terminal = false;
     
     /**
      * Bioassay data constraints that have been of data
      * added from client application.
      */  
     private final Collection<BioAssayDataConstraints> bioAssayDataConstraints =
     		new ArrayList<BioAssayDataConstraints>();
     
     /** Identifier of data source where experiment is stored. */
     private DataSourceProperties dataSourceProperties = null;
     
 
         
     // ===============================
     //     Getters/setters
     // ===============================
 
 	/**
      * Get organism.
      * @return Organism
      */
     public final Organism getOrganism() {
 		return organism;
 	}
 
     /**
      * Set organism.
      * @param organism Organism
      */
 	public final void setOrganism(final Organism organism) {
 		this.organism = organism;
 	}
 	
 	
 	/**
 	 * Get data source properties.
 	 * @return Data source properties
 	 */
 	public final DataSourceProperties getDataSourceProperties() {
 		return dataSourceProperties;
 	}
 
 	/**
 	 * Set data source properties.
 	 * @param dataSourceProperties Data source properties
 	 */
 	public final void setDataSourceProperties(
 			final DataSourceProperties dataSourceProperties) {
 		this.dataSourceProperties = dataSourceProperties;
 	}
 
 	/**
 	 * Get identifier used in source database.
 	 * @return Identifier in source database
 	 */
 	public final String getSourceDbId() {
 		return sourceDbId;
 	}
 
 	
 	/**
 	 * Set identifier used in source database.
 	 * @param sourceDbId Identifier used in source database
 	 */
 	public final void setSourceDbId(final String sourceDbId) {
 		this.sourceDbId = sourceDbId;
 	}
 
 	/**
 	 * Can analytic operations be performed on this experiment?
 	 * @return T/F
 	 */
 	public final boolean isTerminal() {
 		return terminal;
 	}
 
 	/**
 	 * Set whether analytic operations can be performed
 	 * on this experiment.
 	 * @param terminal Can analytic operations be performed
 	 * on this experiment?
 	 */
 	public final void setTerminal(final boolean terminal) {
 		this.terminal = terminal;
 	}
 
 	/**
      * Get bioassays performed during experiment.
      * @return Bioassays
      */
     public final Set<BioAssay> getBioAssays() {
         return bioAssays;
     }
 
     /**
      * Set bioassays performed during experiment.
      * @param bioAssays Bioassays
      */
     public final void setBioAssays(final Set<BioAssay> bioAssays) {
         this.bioAssays = bioAssays;
     }
     
     
     /**
      * Get bioassay data constraints of data added from client.
      * @return Bioassay data constraints
      */
     public final Collection<BioAssayDataConstraints>
     getBioAssayDataConstraints() {
 		return bioAssayDataConstraints;
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
 
     /**
      * Get experiment name.
      * @return Name of experiment
      */
     public final String getName() {
         return name;
     }
 
     /**
      * Set experiment name.
      * @param name Name of experiment.
      */
     public final void setName(final String name) {
         this.name = name;
     }
     
     
     /**
      * Get quantitation type.
      * @return Quantitation type
      */
     public final QuantitationType getQuantitationType() {
         return quantitationType;
     }
 
     /**
      * Set quantitation type.
      * @param quantitationType Quantitation type
      */
     public final void setQuantitationType(
             final QuantitationType quantitationType) {
         this.quantitationType = quantitationType;
     }
     
     // ==================================
     //       Constructors
     // ==================================
 
     /**
      * Default constructor.
      */
     public Experiment() {
         
     }
 
     /**
      * Constructor.
      * @param name Name of experiment
      */
     public Experiment(final String name) {
         this.name = name;
     }
     
     
     /**
      * Constructor.
      * @param name Name of experiment
      * @param organism Organism
      * @param quantitationType Quantitation type
      */
     public Experiment(final String name, final Organism organism,
             final QuantitationType quantitationType) {
         this.name = name;
         this.quantitationType = quantitationType;
        this.organism = organism;
     }
     
     
     /**
      * Constructor.
      * @param experimentDto Experiment data transfer object.
      * @param bioAssayDataConstraints Bioassay data constraints
      * associated with given experiment data transfer object
      */
     public Experiment(final ExperimentDTO experimentDto,
     		final BioAssayDataConstraints[] bioAssayDataConstraints) {
     	if (experimentDto.getExperimentID() == null) {
     		throw new IllegalArgumentException(
     				"ExperimentDTO.experimentId cannot be null");
     	}
     	if (bioAssayDataConstraints == null) {
     		throw new IllegalArgumentException(
     				"Bioassay data constraints cannot be null");
     	}
     	this.name = experimentDto.getExperimentID();
     	this.sourceDbId = experimentDto.getExperimentID();
     	BioAssayDTO[] bioAssayDto = experimentDto.getBioAssays();
     	this.add(bioAssayDto);
     	if (bioAssayDto.length > 0) {
     		for (int i = 0; i < bioAssayDto.length; i++) {
 	    		String qtName = bioAssayDto[i].getQuantitationType();
 	    		QuantitationType qt = QuantitationType.getQuantitationType(
 	    				qtName);
 	    		if (qt == null) {
 	    			throw new IllegalArgumentException(
 	    					"Unknown quantitation type '"
 	    					+ qtName + "'");
 	    		}
 	    		if (i == 0) {
 	    			this.quantitationType = qt;
 	    		} else {
 	    			if (this.quantitationType != qt) {
 	    				throw new IllegalArgumentException(
 	    						"Cannot have mixed quantitation types in "
 	    						+ "the same experiment");
 	    			}
 	    		}
     		}
     	}
     	addAll(this.bioAssayDataConstraints, bioAssayDataConstraints);
     }
     
     
     /**
      * Add all constraints from given array to given collection.
      * This is essentially a set operation in that for any
      * constraints in array that are equivalent to any
      * constraints in the collection, they will not be added.
      * Equivalency in this case is based on chromosome number and physical
      * location only. 
      * @param constraintsCol Target collection
      * @param constraintsArr Source array
      */
     private static void addAll(
     		final Collection<BioAssayDataConstraints> constraintsCol,
     		final BioAssayDataConstraints[] constraintsArr) {
     	for (int i = 0; i < constraintsArr.length; i++) {
     		BioAssayDataConstraints c = constraintsArr[i];
     		add(constraintsCol, c);
     	}	
     }
     
     
     /**
      * Add given constraints to given collection in 'set-like'
      * fashion such that for any
      * constraints in array that are equivalent to any
      * constraints in the collection, they will not be added.
      * Equivalency in this case is based on chromosome number and physical
      * location only. 
      * @param constraintsCol Collection
      * @param constraints Constraints
      */
     private static void add(
     		final Collection<BioAssayDataConstraints> constraintsCol,
     		final BioAssayDataConstraints constraints) {
     	boolean found = false;
 		for (BioAssayDataConstraints d : constraintsCol) {
 			if (constraints.getChromosome().equals(d.getChromosome())
 					&& constraints.getStartPosition().equals(
 							d.getStartPosition())
 					&& constraints.getEndPosition().equals(
 							d.getEndPosition())) {
 				found = true;
 				break;
 			}
 		}
 		if (!found) {
 			constraintsCol.add(constraints);
 		}
     }
     
     
     /**
      * Add data from given data transfer objects.
      * @param bioAssayDtos Bioassay data transfer objects.
      */
     private void add(final BioAssayDTO[] bioAssayDtos) {
     	if (bioAssayDtos != null) {
     		for (int i = 0; i < bioAssayDtos.length; i++) {
     			BioAssayDTO dto = bioAssayDtos[i];
     			String bioAssayName = dto.getName();
     			if (bioAssayName == null) {
     				throw new IllegalArgumentException(
     						"BioAssayDTO.name cannot be null");
     			}
     			BioAssay ba = this.getBioAssayByName(bioAssayName);
     			if (ba == null) {
     				this.add(new DataContainingBioAssay(dto));
     			} else {
     				if (!(ba instanceof DataContainingBioAssay)) {
         				throw new WebcghSystemException(
         						"Cannot add BioAssayDTO data to a "
         						+ "non-DataContainingBioAssay object");
         			}
     				((DataContainingBioAssay) ba).addData(dto);
     			}
     		}
     	}
     }
     
     
     /**
      * Get bioassay whose name matches the given name.
      * @param bioAssayName Bioassay name
      * @return A bioassay
      */
     private BioAssay getBioAssayByName(final String bioAssayName) {
     	if (bioAssayName == null) {
     		throw new IllegalArgumentException("Bioassay name is null");
     	}
     	BioAssay bioAssay = null;
     	for (BioAssay ba : this.bioAssays) {
     		if (bioAssayName.equals(ba.getName())) {
     			bioAssay = ba;
     			break;
     		}
     	}
     	return bioAssay;
     }
         
     
     // ====================================
     //        Business methods
     // ====================================
     
     
     /**
      * Add data from given data transfer object.
      * @param experimentDto Experiment data transfer object.
      * @param constraints Bioassay data constraints
      */
     public final void addData(final ExperimentDTO experimentDto,
     		final BioAssayDataConstraints[] constraints) {
     	if (experimentDto == null) {
     		throw new IllegalArgumentException("ExperimentDTO cannot be null");
     	}
     	if (experimentDto.getExperimentID() == null) {
     		throw new IllegalArgumentException(
     				"ExperimentDTO.experimentId cannot be null");
     	}
     	if (!this.name.equals(experimentDto.getExperimentID())) {
     		throw new IllegalArgumentException(
     				"Trying to add data to different experiment.  "
     				+ "Invalid experimentId");
     	}
     	this.add(experimentDto.getBioAssays());
     	for (int i = 0; i < constraints.length; i++) {
     		boolean found = false;
     		BioAssayDataConstraints c = constraints[i];
     		for (BioAssayDataConstraints d : this.bioAssayDataConstraints) {
     			if (c.getChromosome().equals(d.getChromosome())
     					&& c.getStartPosition().equals(d.getStartPosition())
     					&& c.getEndPosition().equals(d.getEndPosition())) {
     				found = true;
     				break;
     			}
     		}
 			if (!found) {
 				this.bioAssayDataConstraints.add(c);
 			}
     	}
     }
     
     /**
      * Add a bioassay to this experiment.
      * @param bioAssay A bioassay
      */
     public final void add(final BioAssay bioAssay) {
         this.bioAssays.add(bioAssay);
     }
     
     
     /**
      * Is given experiment "synonymous" with this?  Synonymous
      * is like "equals," except the id properties do not have
      * to match.
      * @param exp An experiment
      * @return T/F
      */
     public final boolean synonymousWith(final Experiment exp) {
         boolean match = StringUtils.equal(this.name, exp.name);
         for (Iterator<BioAssay> it1 = this.bioAssays.iterator();
             it1.hasNext() && match;) {
             match = false;
             BioAssay ba1 = it1.next();
             for (Iterator<BioAssay> it2 = exp.bioAssays.iterator();
                 it2.hasNext() && !match;) {
                 BioAssay ba2 = it2.next();
                 if (ba1.synonymousWith(ba2)) {
                     match = true;
                 }
             }
         }
         return match;
     }
     
     
     /**
      * Get set of chromosomes within this experiment.
      * @return Chromosomes
      */
     public final SortedSet<Short> getChromosomes() {
         SortedSet<Short> chroms = new TreeSet<Short>();
         for (BioAssay ba : this.bioAssays) {
             chroms.addAll(ba.getChromosomes());
         }
         return chroms;
     }
     
     /**
      * Get size of chromosome inferred from data.
      * @param chromosome Chromosome number
      * @return Size of chromosome inferred from data
      */
     public final long inferredChromosomeSize(final short chromosome) {
     	long max = 0;
     	for (BioAssay ba : this.bioAssays) {
     		long candidateMax = ba.inferredChromosomeSize(chromosome);
     		if (candidateMax > max) {
     			max = candidateMax;
     		}
     	}
     	return max;
     }
     
     
     /**
      * Round up all bioassay data constraints in given experiments.
      * @param experiments Experiments
      * @return All bioassay data constraints in given experiments
      */
     public static final Collection<BioAssayDataConstraints>
     getBioAssayDataConstraints(final Collection<Experiment> experiments) {
     	Collection<BioAssayDataConstraints> c =
     		new ArrayList<BioAssayDataConstraints>();
     	for (Experiment exp : experiments) {
     		Collection<BioAssayDataConstraints> constraints =
     			exp.bioAssayDataConstraints;
     		for (BioAssayDataConstraints d : constraints) {
     			add(c, d);
     		}
     	}
     	return c;
     }
     
     
     /**
      * Does experiment contain data from given constraints?
      * @param constraints Constraints?
      * @return T/F
      */
     public final boolean containsData(
     		final BioAssayDataConstraints constraints) {
     	boolean contains = false;
     	for (BioAssayDataConstraints c : this.bioAssayDataConstraints) {
     		if (c.getChromosome().equals(constraints.getChromosome())
     				&& c.getStartPosition().equals(
     						constraints.getStartPosition())
     				&& c.getEndPosition().equals(
     						constraints.getEndPosition())) {
     			contains = true;
     			break;
     		}
     	}
     	return contains;
     }
     
     
     /**
      * Get minimum value.  This value is the sum of
      * <code>value</code> and <code>error</code>
      * for some <code>ArrayDatum</code> object contained herein.
      * @return Minimum value or 0.0 if there are
      * no nested <code>ArrayDatum</code> objects.
      */
     public final float minValue() {
     	float min = Float.NaN;
     	for (BioAssay ba : this.bioAssays) {
     		float candidateMin = ba.minValue();
     		if (!Float.isNaN(candidateMin)) {
     			if (Float.isNaN(min) || candidateMin < min) {
     				min = candidateMin;
     			}
     		}
     	}
     	if (Float.isNaN(min)) {
     		min = (float) 0.0;
     	}
     	return min;
     }
     
     
     /**
      * Get maximum value.  This value is the sum of
      * <code>value</code> and <code>error</code>
      * for some <code>ArrayDatum</code> object contained herein.
      * @return Maximum value or 0.0 if there are
      * no nested <code>ArrayDatum</code> objects.
      */
     public final float maxValue() {
     	float max = Float.NaN;
     	for (BioAssay ba : this.bioAssays) {
     		float candidateMax = ba.maxValue();
     		if (!Float.isNaN(candidateMax)) {
     			if (Float.isNaN(max) || candidateMax > max) {
     				max = candidateMax;
     			}
     		}
     	}
     	if (Float.isNaN(max)) {
     		max = (float) 0.0;
     	}
     	return max;
     }
     
     
     /**
      * Get minimum value for given chromosomes.
      * This value is the sum of
      * <code>value</code> and <code>error</code>
      * for some <code>ArrayDatum</code> object contained herein.
      * @param chromosomes Chromosome numbers
      * @return Minimum value for given chromosomes or 0.0 if there are
      * no nested <code>ArrayDatum</code> objects.
      */
     public final float minValue(final Collection<Short> chromosomes) {
     	float min = Float.NaN;
     	for (BioAssay ba : this.bioAssays) {
     		float candidateMin = ba.minValue(chromosomes);
     		if (!Float.isNaN(candidateMin)) {
     			if (Float.isNaN(min) || candidateMin < min) {
     				min = candidateMin;
     			}
     		}
     	}
     	if (Float.isNaN(min)) {
     		min = (float) 0.0;
     	}
     	return min;
     }
     
     
     /**
      * Get maximum value for given chromosomes. 
      * This value is the sum of
      * <code>value</code> and <code>error</code>
      * for some <code>ArrayDatum</code> object contained herein.
      * @param chromosomes Chromosome numbers
      * @return Maximum value for given chromosomes or 0.0 if there are
      * no nested <code>ArrayDatum</code> objects.
      */
     public final float maxValue(final Collection<Short> chromosomes) {
     	float max = Float.NaN;
     	for (BioAssay ba : this.bioAssays) {
     		float candidateMax = ba.maxValue(chromosomes);
     		if (!Float.isNaN(candidateMax)) {
     			if (Float.isNaN(max) || candidateMax > max) {
     				max = candidateMax;
     			}
     		}
     	}
     	if (Float.isNaN(max)) {
     		max = (float) 0.0;
     	}
     	return max;
     }
     
     
     /**
      * Get maximum value from given
      * experiments.  This value is the sum of
      * <code>value</code> and <code>error</code>
      * for some <code>ArrayDatum</code> object contained herein.
      * @param experiments Experiments
      * @return Maximum value or 0.0 if there are
      * no nested <code>ArrayDatum</code> objects.
      */
     public static final float findMaxValue(
     		final Collection<Experiment> experiments) {
     	Float max = Float.NaN;
     	for (Experiment exp : experiments) {
     		float candidateMax = exp.maxValue();
     		if (!Float.isNaN(candidateMax)) {
     			if (Float.isNaN(max) || candidateMax > max) {
     				max = candidateMax;
     			}
     		}
     	}
     	if (Float.isNaN(max)) {
     		max = (float) 0.0;
     	}
     	return max;
     }
     
     
     /**
      * Get minimum value from given
      * experiments.  This is the sum of
      * <code>value</code> and <code>error</code>
      * for some <code>ArrayDatum</code> object contained herein.
      * @param experiments Experiments
      * @return Minimum value or 0.0 if there are
      * no nested <code>ArrayDatum</code> objects.
      */
     public static final float findMinValue(
     		final Collection<Experiment> experiments) {
     	Float min = Float.NaN;
     	for (Experiment exp : experiments) {
     		float candidateMin = exp.minValue();
     		if (!Float.isNaN(candidateMin)) {
     			if (Float.isNaN(min) || candidateMin < min) {
     				min = candidateMin;
     			}
     		}
     	}
     	if (Float.isNaN(min)) {
     		min = (float) 0.0;
     	}
     	return min;
     }
     
     
     /**
      * Get minimum value from given
      * experiments and chromosomes.  This is the sum of
      * <code>value</code> and <code>error</code>
      * for some <code>ArrayDatum</code> object contained herein.
      * @param experiments Experiments
      * @param chromosomes Chromosome numbers
      * @return Minimum value or 0.0 if there are
      * no nested <code>ArrayDatum</code> objects.
      */
     public static final float findMaxValue(
     		final Collection<Experiment> experiments,
     		final Collection<Short> chromosomes) {
     	Float max = Float.NaN;
     	for (Experiment exp : experiments) {
     		float candidateMax = exp.maxValue(chromosomes);
     		if (!Float.isNaN(candidateMax)) {
     			if (Float.isNaN(max) || candidateMax > max) {
     				max = candidateMax;
     			}
     		}
     	}
     	if (Float.isNaN(max)) {
     		max = (float) 0.0;
     	}
     	return max;
     }
     
     
     /**
      * Get minimum value from given
      * experiments and chromosomes.  This is the sum of
      * <code>value</code> and <code>error</code>
      * for some <code>ArrayDatum</code> object contained herein.
      * @param experiments Experiments
      * @param chromosomes Chromosome numbers
      * @return Minimum value or 0.0 if there are
      * no nested <code>ArrayDatum</code> objects.
      */
     public static final float findMinValue(
     		final Collection<Experiment> experiments,
     		final Collection<Short> chromosomes) {
     	Float min = Float.NaN;
     	for (Experiment exp : experiments) {
     		float candidateMin = exp.minValue(chromosomes);
     		if (!Float.isNaN(candidateMin)) {
     			if (Float.isNaN(min) || candidateMin < min) {
     				min = candidateMin;
     			}
     		}
     	}
     	if (Float.isNaN(min)) {
     		min = (float) 0.0;
     	}
     	return min;
     }
     
     
     /**
      * Determine the quantitation type from given experiments.
      * @param experiments Experiments
      * @return Quantitation type
      */
     public static QuantitationType getQuantitationType(
     		final Collection<Experiment> experiments) {
     	QuantitationType qt = null;
     	for (Experiment exp : experiments) {
     		qt = exp.quantitationType;
     		if (qt != null) {
     			break;
     		}
     	}
     	return qt;
     }
     
     /**
      * Get organism from experiments.
      * @param experiments Experiments
      * @return Organism
      */
     public static Organism getOrganism(
     		final Collection<Experiment> experiments) {
     	Organism org = null;
     	for (Experiment exp : experiments) {
     		org = exp.organism;
     		if (org != null) {
     			break;
     		}
     	}
     	return org;
     }
     
     
     /**
      * Are data in this experiment in memory, as opposed
      * to serialized on disk?
      * @return T/F
      */
     public final boolean dataInMemory() {
     	boolean inMemory = false;
     	for (BioAssay ba : this.bioAssays) {
 			if (!inMemory) {
     			if (ba instanceof DataContainingBioAssay) {
     				inMemory = true;
     				break;
     			}
 			}
 		}
     	return inMemory;
     }
     
     
     /**
      * Are data in this experiment in memory as opposed to
      * serialized on disk?
      * @param experiments Experiments
      * @return T/F
      */
     public static boolean dataInMemory(
     		final Collection<Experiment> experiments) {
     	boolean inMemory = false;
     	for (Experiment exp : experiments) {
     		if (exp.dataInMemory()) {
     			inMemory = true;
     			break;
     		}
     	}
     	return inMemory;
     }
     
     
     /**
      * Get size of chromosome inferred from data in given
      * experiments.
      * @param experiments Experiments
      * @param chromosome Chromosome number
      * @return Size of chromosome inferred from data in given
      * experiments.
      */
     public static final long inferredChromosomeSize(
     		final Collection<Experiment> experiments,
     		final short chromosome) {
     	long max = 0;
     	for (Experiment exp : experiments) {
     		long candidateMax = exp.inferredChromosomeSize(chromosome);
     		if (candidateMax > max) {
     			max = candidateMax;
     		}
     	}
     	return max;
     }
     
     
     /**
      * Get all chromosomes in input collection.
      * @param experiments Experiments
      * @return All chromosome numbers
      */
     public static final SortedSet<Short> chromosomes(
     		final Collection<Experiment> experiments) {
     	SortedSet<Short> chroms = new TreeSet<Short>();
     	for (Experiment exp : experiments) {
     		chroms.addAll(exp.getChromosomes());
     	}
     	return chroms;
     }
     
     
     /**
      * Create new experiments from given data transfer objects.
      * @param experimentDtos Experiment data transfer objects.
      * @param bioAssayDataConstraints Constraints associated with
      * given experiment data transfer object
      * @return Experiments
      */
     public static final Collection<Experiment> newExperiments(
     		final Collection<ExperimentDTO> experimentDtos,
     		final BioAssayDataConstraints[] bioAssayDataConstraints) {
     	Collection<Experiment> experiments = new ArrayList<Experiment>();
     	for (ExperimentDTO dto : experimentDtos) {
     		boolean found = false;
     		if (dto.getExperimentID() == null) {
     			throw new IllegalArgumentException(
     					"ExperimentDTO.experimentId property is null");
     		}
     		for (Experiment exp : experiments) {
     			if (exp.getName().equals(dto.getExperimentID())) {
     				exp.addData(dto, bioAssayDataConstraints);
     				found = true;
     				break;
     			}
     		}
     		if (!found) {
     			experiments.add(new Experiment(dto, bioAssayDataConstraints));
     		}
     	}
     	return experiments;
     }
 }
