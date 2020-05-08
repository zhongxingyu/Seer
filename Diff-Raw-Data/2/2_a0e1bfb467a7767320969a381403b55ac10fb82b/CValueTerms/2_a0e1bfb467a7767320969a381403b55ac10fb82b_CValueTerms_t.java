 package uk.co.jbothma.protege.protplug.extraction;
 
 import gate.Annotation;
 import gate.AnnotationSet;
 import gate.Document;
 import gate.Factory;
 import gate.Utils;
 import gate.corpora.SerialCorpusImpl;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import uk.co.jbothma.protege.protplug.Util;
 import uk.co.jbothma.protege.protplug.candidate.TermCandidate;
 import uk.co.jbothma.terms.CValueComparator;
 import uk.co.jbothma.terms.CValueSess;
 import uk.co.jbothma.terms.Candidate;
 
 public class CValueTerms {
 	public static void doCValue(SerialCorpusImpl corp, ArrayList<TermCandidate> termCandidates) {
 		CValueSess cvals = new CValueSess();
 		Iterator<Document> docIter = corp.iterator();
 		Document doc;
 		while (docIter.hasNext()) {
 			doc = docIter.next();
 
 			String inputASName = "Original markups";
 			String inputASType = "TermCandidate";
 			
 			AnnotationSet inputAS = doc.getAnnotations(inputASName);
 			
 			Iterator<Annotation> phrasIter = inputAS.get(inputASType).iterator();
 			
 			while (phrasIter.hasNext()) {
 				String phrase = "";
 				Annotation phrasAnnot = (Annotation) phrasIter.next();
				phrase = Util.termAsLemmas(inputAS, phrasAnnot);
 				cvals.observe(phrase.toLowerCase().trim());
 			}
 
 			corp.unloadDocument(doc, false);
 			Factory.deleteResource(doc);
 		}
 		
 		cvals.calculate();
 		
 		ArrayList<Candidate> candList = new ArrayList<Candidate>(cvals.getCandidates());
 		Collections.sort(candList, new CValueComparator());
 		
 		// roughly put confidence between 0 and 1.
 		double minCVal = candList.get(0).getCValue();
 		double maxCVal = candList.get(candList.size()-1).getCValue();
 		double cValInterval = maxCVal-minCVal;
 		
 		for (Candidate cand : candList) {
 			float conf = (float) ((cand.getCValue()+(-minCVal))/cValInterval);
 			TermCandidate termCand = new TermCandidate(cand.getString(), conf);
 			termCandidates.add(termCand);
 		}
 	}
 }
