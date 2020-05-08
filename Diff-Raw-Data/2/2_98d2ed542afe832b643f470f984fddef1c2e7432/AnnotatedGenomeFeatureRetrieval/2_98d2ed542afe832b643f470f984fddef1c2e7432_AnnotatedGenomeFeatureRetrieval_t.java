 /*
 $Revision: 1.1 $
 $Date: 2007-03-29 17:03:36 $
 
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
 
 package org.rti.webgenome.sandbox.benchmark;
 
 import java.util.SortedSet;
 
 import org.rti.webgenome.domain.AnnotatedGenomeFeature;
 import org.rti.webgenome.domain.AnnotationType;
 import org.rti.webgenome.domain.Organism;
 import org.rti.webgenome.service.dao.AnnotatedGenomeFeatureDao;
 import org.rti.webgenome.service.dao.OrganismDao;
 import org.rti.webgenome.util.StopWatch;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 /**
  * Class for benchmarking the retrieval of annotated genome features
  * from the database.
  * @author dhall
  *
  */
 public final class AnnotatedGenomeFeatureRetrieval {
 	
 	//
 	//     STATICS
 	//
 	
 	/** Chromosome number. */
 	private static final short CHROMOSOME = (short) 1;
 	
 	/** Chromosome start position. */
 	private static final long START_POS = 50000000;
 	
 	/** Chromosome end position. */
 	private static final long END_POS = 60000000;
 	
 	/** Annotation type that is queried for in benchmark. */
 	private static final AnnotationType ANNOTATION_TYPE =
 		AnnotationType.GENE;
 	
 	//
 	//     CONSTRUCTORS
 	//
 	
 	/**
 	 * Constructor.
 	 *
 	 */
 	private AnnotatedGenomeFeatureRetrieval() {
 		
 	}
 
 	//
 	//     MAIN METHOD
 	//
 	
 	/**
 	 * Main methods.
 	 * @param args Command line arguments
 	 */
 	public static void main(final String[] args) {
 		
 		// Get DAO beans
 		ApplicationContext ctx = new ClassPathXmlApplicationContext(
 				"test/benchmark/applicationContext.xml");
 		OrganismDao oDao = (OrganismDao) ctx.getBean("organismDao");
 		AnnotatedGenomeFeatureDao hDao = (AnnotatedGenomeFeatureDao)
 			ctx.getBean("hibernateAnnotatedGenomeFeatureDao");
 		
 		// Get default organism
 		Organism organism = oDao.loadDefault();
 		
 		StopWatch stopWatch = new StopWatch();
 		SortedSet<AnnotatedGenomeFeature> feats = null;
 		
		// Perform benchmark query using hibernate
 		System.out.println("Querying via Hibernate for features of type '"
 				+ ANNOTATION_TYPE.toString()
 				+ "' over genome interval '" + CHROMOSOME + ":"
 				+ START_POS + "-" + END_POS + "'");
 		stopWatch.start();
 		feats =
 			hDao.load(CHROMOSOME, START_POS, END_POS, ANNOTATION_TYPE,
 					organism);
 		stopWatch.stop();
 		System.out.println("Finished.  Elapsed time: "
 				+ stopWatch.getFormattedElapsedTime());
 		System.out.println(feats.size() + " features retrieved");
 		if (feats.size() > 0) {
 			AnnotatedGenomeFeature feat = feats.first();
 			System.out.println("Feature '" + feat.getId() + "' has "
 					+ feat.getChildFeatures().size() + " child features");
 		}
 	}
 }
