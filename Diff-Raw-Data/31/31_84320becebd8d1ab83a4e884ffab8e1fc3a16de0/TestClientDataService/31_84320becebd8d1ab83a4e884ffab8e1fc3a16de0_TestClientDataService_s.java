 /*
$Revision: 1.7 $
$Date: 2006-11-29 03:14:07 $
 
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
 
 package org.rti.webcgh.service.client;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.rti.webcgh.domain.BioAssay;
 import org.rti.webcgh.domain.DataContainingBioAssay;
 import org.rti.webcgh.domain.Experiment;
 import org.rti.webcgh.domain.QuantitationType;
 import org.rti.webgenome.client.BioAssayDTO;
 import org.rti.webgenome.client.BioAssayDTOGenerator;
 import org.rti.webgenome.client.BioAssayDataConstraints;
 import org.rti.webgenome.client.ExperimentDTO;
 import org.rti.webgenome.client.ExperimentDTOGenerator;
 
 /**
  * Implementation of <code>ClientDataService</code>
  * for testing.  All data returned are randomly
  * generated values and positions.
  * @author dhall
  *
  */
 public class TestClientDataService implements ClientDataService {
 	
 	/** Gap between generated reporters in base pairs. */
 	private static final long GAP = 10000000;
 	
 	/** Number of bioassays per experiment generated. */
 	private static final int NUM_BIO_ASSAYS = 3;
 	
 	/** Experiment data transfer object generator. */
 	private final ExperimentDTOGenerator experimentDTOGenerator =
 		new ExperimentDTOGenerator(GAP, NUM_BIO_ASSAYS);
 	
 	/** Bioassay data transfer object generator. */
 	private final BioAssayDTOGenerator bioAssayDtoGenerator =
 		new BioAssayDTOGenerator(GAP);
 	
 	/** Quantitation type. */
 	private QuantitationType quantitationType = null;
 	
 	
 	/**
 	 * Set quantitation type.
 	 * @param quantitationTypeId Quantitation type ID.  Must match
 	 * one of the IDs in org.rti.webcgh.domain.QuantitationType.
 	 */
 	public final void setQuantitationType(final String quantitationTypeId) {
 		this.quantitationType =
 			QuantitationType.getQuantitationType(quantitationTypeId);
 		if (this.quantitationType == null) {
 			throw new IllegalArgumentException("Unknown quantitation type '"
 					+ quantitationTypeId + "'");
 		}
 	}
 
 	/**
 	 * Generate random data with given experiment IDs and constraints.
 	 * @param constraints Query constraints
 	 * @param experimentIds Experiment identifiers
 	 * @param clientID Application client ID
 	 * @return Experiments from application client
 	 */
     public final Collection<Experiment> getClientData(
     		final BioAssayDataConstraints[] constraints,
     		final String[] experimentIds, final String clientID) {
     	Collection<Experiment> experiments = new ArrayList<Experiment>();
     	for (int i = 0; i < experimentIds.length; i++) {
     		ExperimentDTO dto = this.experimentDTOGenerator.newExperimentDTO(
     				experimentIds[i], constraints);
     		Experiment exp = new Experiment(dto);
     		experiments.add(exp);
    		exp.setQuantitationType(this.quantitationType);
     	}
     	return experiments;
     }
     
     
     /**
      * Add data to given experiments.
      * @param experiments Experiments
      * @param constraints Query constraints
      * @param clientId Application client ID
      */
     public final void addData(final Collection<Experiment> experiments,
     		final BioAssayDataConstraints[] constraints,
     		final String clientId) {
     	for (Experiment exp : experiments) {
     		for (BioAssay ba : exp.getBioAssays()) {
     			if (!(ba instanceof DataContainingBioAssay)) {
     				throw new IllegalArgumentException(
     					"Expecting BioAssay of type DataContainingBioAssay");
     			}
     			BioAssayDTO dto =
     				this.bioAssayDtoGenerator.newBioAssayDTO(ba.getName(),
     						constraints);
     			((DataContainingBioAssay) ba).addData(dto);
     		}
     	}
     }
 }
