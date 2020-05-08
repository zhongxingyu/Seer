 /****************************************************************************
  * Copyright (c) 2011, Monnet Project
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the Monnet Project nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  ********************************************************************************/
 package eu.monnetproject.coal.test;
 
 import java.io.File;
 import java.io.IOException;
 
 import eu.monnetproject.align.Aligner;
 import eu.monnetproject.align.Alignment;
 import eu.monnetproject.align.AlignmentSerializer;
 import eu.monnetproject.align.Matcher;
 import eu.monnetproject.ontology.OntologySerializer;
 import eu.monnetproject.ontology.Ontology;
 import eu.monnetproject.data.FileDataSource;
 import aQute.bnd.annotation.component.*;
 import eu.monnetproject.framework.test.TestMonitor;
 import eu.monnetproject.framework.test.annotation.TestCase;
 import eu.monnetproject.framework.test.annotation.TestSuite;
 
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.logging.Logger;
 
 @Component(provide=Object.class)
 @TestSuite(label="eu.monnetproject.coal.test.Test")
 public class Test{
 
     
 	private Logger log = Logger.getLogger(Test.class.getName());
 //	OntologyTranslator ontologyTranslator;
 	OntologySerializer ontologySerializer;
 
 	private String srcFileName = "load/xbrl/output/ifrs-cor_2009-04-01.rdf";
 	private String tgtFileName = "load/xbrl/output/itcc-ci-2011-01-04.nt";
 	private String inputFileName = "load/coal/AlignmentForTraining.rdf";
 	private String inputFileName2 = "load/coal/AlignmentForTraining_2.rdf";
 	//private String inputFileName3 = "load/coal/AlignmentForTraining_3.rdf";
 	private String outputFileName = "load/coal/OutputAlignment.rdf";
 	
 	private Aligner aligner;
 	private AlignmentSerializer alignmentSerializer;
 	private Matcher matcher;
 	
 	@TestCase(identifier="1",label="start")
 	public void start(TestMonitor monitor){
 		log.info("Starting test");
 		
 		Collection<Alignment> alignments = new HashSet<Alignment>();
 		
 		alignments.add(this.alignmentSerializer.readAlignment(new File(inputFileName)));
 		alignments.add(this.alignmentSerializer.readAlignment(new File(inputFileName2)));
 		//alignments.add(this.alignmentSerializer.readAlignment(new File(inputFileName3)));
 		
		this.matcher.train(alignments);
 		
 		log.info("Reading source ontology");
 		Ontology srcOnt = ontologySerializer.read(new FileDataSource(srcFileName));
 		log.info("Reading target ontology");
 		Ontology tgtOnt = ontologySerializer.read(new FileDataSource(tgtFileName));
 		
 		log.info(srcOnt.getEntities().size()+" entities in source ontology");
 		log.info(tgtOnt.getEntities().size()+" entities in target ontology");
 		
		Alignment alignment = this.alignmentSerializer.createAlignment();
 		
 		log.info("Calling aligner.align");
		this.aligner.align(srcOnt, tgtOnt, alignment);
 		log.info("Done aligning");
 
 		try {
 			alignmentSerializer.writeAlignment(alignment,new File(outputFileName));
 		} catch (IOException e) {
 			log.severe(e.getMessage());
 		}
 	}
 	
 	@Deactivate
 	public void deactivate() {
 		log.info("deactivating test");
 		
 	}
 	
 	@Reference public void bindAligner(Aligner aligner) {
 		log.info("Binding aligner");
 		this.aligner = aligner;
 		log.info("Bound aligner to "+this.aligner.toString());
 	}
 	
 	@Reference public void bindMatcher(Matcher matcher) {
 		log.info("Binding matcher");
 		this.matcher = matcher;
 		log.info("Bound matcher to "+this.matcher.toString());
 	}
 
 	@Reference public void bindAlignmentSerializer(AlignmentSerializer alignmentSerializer) {
 		log.info("Binding alignment serializer");
 		this.alignmentSerializer = alignmentSerializer;
 		log.info("Bound alignmentSerializer to "+this.alignmentSerializer.toString());
 	}
 	
 	@Reference
 	public void bindOntologySerializer(OntologySerializer ontologySerializer){
 		log.info("Binding ontology serializer");
 		this.ontologySerializer =  ontologySerializer;
 		log.info("Bound ontology serializer to "+this.ontologySerializer.toString());
 	}
 	
 }
