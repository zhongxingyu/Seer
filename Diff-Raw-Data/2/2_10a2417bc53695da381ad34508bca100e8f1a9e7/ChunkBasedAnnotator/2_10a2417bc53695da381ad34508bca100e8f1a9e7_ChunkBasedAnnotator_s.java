 package fr.lipn.yasemir.ontology.annotation;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.ca.CatalanAnalyzer;
 import org.apache.lucene.analysis.de.GermanAnalyzer;
 import org.apache.lucene.analysis.en.EnglishAnalyzer;
 import org.apache.lucene.analysis.es.SpanishAnalyzer;
 import org.apache.lucene.analysis.fr.FrenchAnalyzer;
 import org.apache.lucene.analysis.it.ItalianAnalyzer;
 import org.apache.lucene.analysis.nl.DutchAnalyzer;
 import org.apache.lucene.analysis.pt.PortugueseAnalyzer;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.TextField;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.queryparser.classic.QueryParser;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.search.similarities.BM25Similarity;
 import org.apache.lucene.store.FSDirectory;
 import org.apache.lucene.util.Version;
 import org.semanticweb.owlapi.model.OWLClass;
 import org.tartarus.snowball.ext.EnglishStemmer;
 
 import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
 import edu.stanford.nlp.process.CoreLabelTokenFactory;
 import edu.stanford.nlp.process.DocumentPreprocessor;
 import edu.stanford.nlp.process.PTBTokenizer;
 import edu.stanford.nlp.process.TokenizerFactory;
 import edu.stanford.nlp.trees.Tree;
 import edu.stanford.nlp.ling.CoreLabel;
 import edu.stanford.nlp.ling.HasWord;
 import fr.lipn.yasemir.Yasemir;
 import fr.lipn.yasemir.tools.Tools;
 /**
  * This class uses the Stanford NLP Parser to search terminology clues in Noun Phrases
  * @author buscaldi
  *
  */
 public class ChunkBasedAnnotator implements SemanticAnnotator {
 	private String termIndexPath;
 	private LexicalizedParser parser;
 	
 	private static int MAX_ANNOTS=10;
 
 	public ChunkBasedAnnotator(String termIndexPath) {
 		this.termIndexPath=termIndexPath;
 		 parser = LexicalizedParser.loadModel("lib/englishPCFG.ser.gz");
 	}
 	
 	//TODO: finire il ChunkedAnnotator
 	@Override
 	public DocumentAnnotation annotate(String document) {
 		DocumentAnnotation ret = new DocumentAnnotation();
 		
 		try {
 			IndexReader reader = IndexReader.open(FSDirectory.open(new File(termIndexPath)));
 			IndexSearcher searcher = new IndexSearcher(reader);
 			searcher.setSimilarity(new BM25Similarity());
 			
 
 			//Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_44);
 			
 			Reader r = new BufferedReader(new StringReader(document));
 			Vector<String> fragments = new Vector<String>();
 			
 			for(List<HasWord> sentence : new DocumentPreprocessor(r)) {
 				Tree parse = parser.apply(sentence);
 				for(Tree p : parse){
 					if(p.label().value().equals("NP") && p.isPrePreTerminal()) {
 						//p.pennPrint();
 						StringBuffer tmpstr = new StringBuffer();
 						for(Tree l : p.getLeaves()){
 							
 							tmpstr.append(l.label().toString());
 							tmpstr.append(" ");
 						}
 						fragments.add(tmpstr.toString().trim());
 						System.err.println("[YaSemIR - CBA] Chunk found: "+tmpstr);
 					}
 					
 				}
 			}
 			
 			
 			for(String fragment :  fragments) {
 				
 				if(fragment.length()==0) continue;
 				//System.err.println("Annotating: "+fragment);
 						
 				QueryParser parser = new QueryParser(Version.LUCENE_44, "labels", Yasemir.analyzer);
 				Query query = parser.parse(fragment);
				System.err.println("Searching for: " + query.toString("terms"));
 				
 				TopDocs results = searcher.search(query, 20);
 			    ScoreDoc[] hits = results.scoreDocs;
 			    
 			    int numTotalHits = results.totalHits;
 			    //System.err.println(numTotalHits + " total matching classes");
 			    
 			    if(numTotalHits > 0) {
 				    hits = searcher.search(query, numTotalHits).scoreDocs;
 				    for(int i=0; i< Math.min(numTotalHits, MAX_ANNOTS); i++){
 				    	Document doc = searcher.doc(hits[i].doc);
 				    	String ptrn = "(?i)("+doc.get("labels").replaceAll(", ", "|")+")";
 				    	//System.err.println("OWLClass="+doc.get("id")+" score="+hits[i].score);
 				    	if(Tools.checkPattern(fragment, ptrn)){
 				    		//System.err.println("OK: OWLClass="+doc.get("id")+" score="+hits[i].score);
 				    		Annotation ann = new Annotation(doc.get("id"));
 				    		String ontoID = ann.getRelatedOntology().getOntologyID();
 				    		
 				    		Vector<Annotation> annotations = ret.get(ontoID);
 				    		if(annotations == null) annotations = new Vector<Annotation>();
 					    	annotations.add(ann);
 					    	ret.put(ontoID, annotations);
 				    	}
 				    }
 			    }
 								 
 			}
 			
 			reader.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
 
 }
