 import com.aliasi.chunk.AbstractCharLmRescoringChunker;
 import com.aliasi.chunk.Chunk;
 import com.aliasi.chunk.Chunking;
 import com.aliasi.chunk.HmmChunker;
 import com.aliasi.corpus.ObjectHandler;
 
 import com.aliasi.hmm.HiddenMarkovModel;
 import com.aliasi.hmm.HmmDecoder;
 
 import com.aliasi.tag.Tagging;
 import com.aliasi.tokenizer.RegExTokenizerFactory;
 import com.aliasi.tokenizer.TokenizerFactory;
 import com.aliasi.util.AbstractExternalizable;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.TreeSet;
 import java.util.regex.Pattern;
 
 
 public class Tagger {
 
 	static StringBuilder result;
 
 	/**
 	 * @param args
 	 * @throws IOException
 	 * @throws ClassNotFoundException 
 	 */
 	public static void main(String[] args) throws IOException, ClassNotFoundException {
 		long startTime = System.currentTimeMillis();
 
 		GeniaParser parser = new GeniaParser();
 		result = new StringBuilder("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<results>\n");
 		GeniaHandler handler = new GeniaHandler(result);
 		parser.setHandler(handler);
 		if (args.length > 0)
 		{
 			FileInputStream fileIn = new FileInputStream(args[0]);
 			parser.parse(fileIn);
 		} else {
 			System.out.println("Error: Please supply a genia input file.");
 			System.exit(0);
 		}
 
 		result.append("</results>");
 		System.out.println(result);
 		System.err.println("\ngenes tagged: " + handler.numFoundGenes);
 		System.err.println("time: " + (System.currentTimeMillis() - startTime)
 				/ 1000.0 + "s");
 	}
 
 	static class GeniaHandler implements ObjectHandler<GeniaMedlineCitation> {
 		long numFoundGenes = 0L;
 
 		HashSet<String> stopSet = new HashSet<String>(),
 				geneSet = new HashSet<String>();
 		TreeSet<String> tokenSet = new TreeSet<String>();
 
 		String pmid;
 		StringBuilder result;
 		TokenizerFactory factory;
 		
 		HmmDecoder POSDecoder, NERDecoder;	// part-of-speech tagging hidden markov model decoder and NER-decoder
 		
 		Pattern pGt;
 		Pattern pLt;
 		
 		public GeniaHandler(StringBuilder result) throws IOException, ClassNotFoundException {			
 			String line;
 		
 			// read in gene dictionary
 			BufferedReader geneNames = new BufferedReader(new FileReader(
 					"genenames-2.txt"));
 			while ((line = geneNames.readLine()) != null) {
 				if (line.length() == 0)
 					continue;
 				geneSet.add(line);
 			}
 			geneNames.close();
 			
 			// load models
 			FileInputStream fileIn = new FileInputStream("pos-en-bio-genia.HiddenMarkovModel");
 			ObjectInputStream objIn = new ObjectInputStream(fileIn);
 			HiddenMarkovModel hmm = (HiddenMarkovModel) objIn.readObject();
 			objIn.close(); fileIn.close();
 			this.POSDecoder = new HmmDecoder(hmm);
 			
 			fileIn = new FileInputStream("ne-en-bio-genetag.HmmChunker");
 			objIn = new ObjectInputStream(fileIn);
 			HmmChunker hmmChunker = (HmmChunker) objIn.readObject();
 			objIn.close(); fileIn.close();
 			this.NERDecoder = hmmChunker.getDecoder();
 		
 			factory = new RegExTokenizerFactory("(-|'|\\d|\\p{L})+|\\S");
 
 			this.result = result;
 		}
 
 		public String tag(List<String> text) throws Exception {
 			/*
 			 * actual tagging method
 			 * in here we want
 			 * - tokenization
 			 * - gene tagging with dictionary
 			 * - POS
 			 * - gene tagging with language(medline) model (NER)
 			 * 
 			 * returns input with tags added
 			 */
 	    	
 			List<String> tagged_text = new LinkedList<String>(text);
 			tagged_text = escapeXML(text);
 	    	
 			StringBuilder localResult = new StringBuilder();
 			
 			//check dict first	
 			for(int tokenIndex = 0; tokenIndex < text.size(); tokenIndex++)
 			{
 				String token = text.get(tokenIndex);
 				if (geneSet.contains(token)){
 					tagged_text.set(tokenIndex, "<gene>"+token+"</gene>");
 					this.numFoundGenes++;
 				}
 			}
 			
 			// Part Of Speech (POS) - tagging
 			Tagging<String> POSTagging = POSDecoder.tag(text);	// contains lists of tokens and assigned tags
 
 			// Named Entity Recognition (NER) decoder
 			//Tagging<String> NERTagging = NERDecoder.tag(text);
 
 			//NER chunker
			AbstractCharLmRescoringChunker chunker = (AbstractCharLmRescoringChunker) AbstractExternalizable
			.readObject(new File("test_chunker"));
    
 			Chunking chunking = chunker.chunk(join(text, " "));
 			//System.out.println("Chunking=" + chunking);
 			for (Chunk chunk : chunking.chunkSet()){
 			    //System.out.println(chunk.toString() + "[" + chunk.type() + "]: "+  chunking.charSequence().subSequence(chunk.start(), chunk.end()));
 
 			    String text_linear = join(text, " ");
 				String token = text_linear.substring(chunk.start(), chunk.end());
 
 				//double check if we really have a gene using dict and pos
 				boolean check_pos = false, check_dict = false;
 			    int index_start = 0;
 			    int index_end = 0;
 			    
 			    //get index of tokens in sequence
 				int start = chunk.start();
 			    int end = chunk.end();
 			    int count = 0;
 			    while(count < end){
 			    	index_end++;
 			    	if (count < start){
 			    		index_start++;
 			    	}
 			    	count+= text.get(index_end).length()+1;
 			    }
 			    
 				if(geneSet.contains(token)) {
 					check_dict = true;
 				} else {
 				    //pos with last token (likely to be the noun of a multi-word gene; for english text)
 				    String tag = POSTagging.tag(index_end);
 					if(tag.equals("NN") || tag.equals("NNP") || tag.equals("NNS") || tag.equals("NNPS")){
 						check_pos = true;
 					}
 				}
 			    
 			    if (check_dict || check_pos) {
			    	if(!tagged_text.get(index_start).startsWith("<gene>")){
 			    		tagged_text.set(index_start, "<gene>"+tagged_text.get(index_start));
 			    		tagged_text.set(index_end, tagged_text.get(index_end)+"</gene>");
 			    		this.numFoundGenes++;
 			    	}
 			    }
 			}
 			
 			localResult.append(joinSentences(tagged_text, " "));
 			
 			return localResult.toString();
 		}
 
 		
 		public String join(Collection<String> s, String delimiter)
 		{
 	        StringBuffer buffer = new StringBuffer();
 	        Iterator<String> iter = s.iterator();
 	        while (iter.hasNext()) {
 	            buffer.append(iter.next());
 	            if (iter.hasNext()) {
 	                buffer.append(delimiter);
 	            }
 	        }
 	        return buffer.toString();
 	    }
 
 		public String joinSentences(Collection<String> s, String delimiter)
 		{
 			//additionally insert \n on . 
 	        StringBuffer buffer = new StringBuffer();
 	        Iterator<String> iter = s.iterator();
 	        String current = "";
 	        while (iter.hasNext()) {
 	        	current = iter.next();
 	            buffer.append(current);
 	            if (current.equals(".")){
 	            	buffer.append("\n");
 	            }
 	            if (iter.hasNext()) {
 	                buffer.append(delimiter);
 	            }
 	        }
 	        return buffer.toString();
 	    }
 		
 		public static String escapeXML(String toBeEscaped)
 		{
 			return toBeEscaped.replaceAll("&", "&amp;").replaceAll(">", "&gt;").replaceAll("<", "&gt;");
 		}
 
 		private List<String> escapeXML(List<String> text) {
 			List<String> escaped = new LinkedList<String>();
 			for(String line : text)
 				escaped.add(escapeXML(line));
 			
 			return escaped;
 		}
 		
 		public void handle(GeniaMedlineCitation citation)
 		{	
 			result.append("<MedlineCitation>\n");
 
 			pmid = citation.pmid;
 			result.append("<PMID>" + pmid + "</PMID>\n");
 			result.append("<AbstractText>\n");
 			
 			try {
 				result.append(tag(citation.body));
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			} catch (Exception e)
 			{
 				e.printStackTrace();
 			}
 			
 			result.append("\n</AbstractText>\n");
 			result.append("</MedlineCitation>\n");
 		}
 		
 		public void delete(String pmid) {
 			throw new UnsupportedOperationException(
 					"not expecting deletes. found pmid=" + pmid);
 		}
 	}
 }
