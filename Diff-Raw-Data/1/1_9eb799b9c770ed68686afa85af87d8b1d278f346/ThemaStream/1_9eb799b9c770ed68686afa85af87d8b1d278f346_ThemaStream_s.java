 package obir.ws;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import edu.stanford.smi.protegex.owl.model.OWLIndividual;
 import edu.stanford.smi.protegex.owl.model.RDFProperty;
 
 import obir.otr.ObirProject;
 import obir.www.AnnotationHandler;
 import obir.www.annotation.RelationAnnotation;
 
 /**
  * Servlet that implements the ThemaStream web service
  * @author davide buscaldi
  */
 public class ThemaStream extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	/**
 	 * Parameters used to add relations to the result or not
 	 * Change manually this value if you want to modify the behaviour of the Web Service
 	 */
 	private boolean RELATIONS_ENABLED=false; //do not use static otherwise all users will have the same configuration
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public ThemaStream() {
         super();
     }
     /**
      * Returns the term offset given a concept instance
      * @param inst
      * @return the term offset (as a string)
      */
     private String getTermOffset(OWLIndividual inst){
     	Collection<RDFProperty> props = inst.getRDFProperties();
     	for(RDFProperty prop : props){
 			if(prop.getName().endsWith("term_offset")){
 				return((String)inst.getPropertyValue(prop));
 			}
     	}
     	return "";
     }
     
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		response.setContentType("text/xml;charset=UTF-8");
 		try {
 			request.setCharacterEncoding("UTF-8");
 		} catch (UnsupportedEncodingException e1) {
 			e1.printStackTrace();
 		}
 		response.setCharacterEncoding("UTF-8");
 		String charset = "UTF-8";
 		request.setCharacterEncoding(charset);
 		
 		HashMap<Integer, Integer> offMap = new HashMap<Integer, Integer>(); //maps character position -> word
 		
 		try {
 			PrintWriter out = response.getWriter();
 			String text=request.getParameter("text");
 			String rel=request.getParameter("rels");
 			if(rel != null) {
 				if(rel.equals("on")) RELATIONS_ENABLED=true;
 			}
 			System.err.println(text);
 			if(text==null){ //TODO:verificare lettura dei files
 				String sourceFile=request.getParameter("file");
 				URL inputfile = new URL(sourceFile);
 		        BufferedReader in = new BufferedReader(new InputStreamReader(inputfile.openStream()));
 		        String inputLine;
 		        StringBuffer contentBuffer = new StringBuffer();
 		        while ((inputLine = in.readLine()) != null)
 		            contentBuffer.append(inputLine);
 		        	//System.out.println(inputLine);
 		        in.close();
 		        text=contentBuffer.toString().trim();
 			}
 			String [] paragraphs=text.split("/n");
 			
 			StringBuffer dataBuffer =  new StringBuffer();
 			
 			//init offMap
 			int currentPosition=0;
 			int totalPosition=0;
 			int termCount=0;
 			
 			dataBuffer.append("<doc>");
 			for(int i=0; i < paragraphs.length; i++){
 				dataBuffer.append("<paragraphe id=\""+(i+1)+"\">");
 				StringTokenizer tokenizer = new StringTokenizer(paragraphs[i]);	
 				while(tokenizer.hasMoreTokens()){
 					String term = tokenizer.nextToken();
 					termCount++;
 					dataBuffer.append("<token id=\""+(termCount)+"\">");
 					dataBuffer.append("<text>"+term+"</text>");
 					dataBuffer.append("</token>");
 					offMap.put(new Integer(currentPosition), new Integer(termCount));
 					totalPosition=currentPosition+term.length()+1;
 					currentPosition=totalPosition;
 				}
 				dataBuffer.append("</paragraphe>");
 			}
 			
 			dataBuffer.append("</doc>");
 			
 			String ontoID=ObirProject.getOWLModel().getNamespaceManager().getDefaultNamespace();
 			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
 			out.println("<ontologie id=\""+ontoID+"\">");
 			String docID="";
 			
 			HttpSession sess = request.getSession(true);
 			AnnotationHandler ann_hdlr=new AnnotationHandler(sess.getId());
 			
 			HashSet<OWLIndividual> termOccurrences = ann_hdlr.getOWLAnnotation(text);
 			Vector<RelationAnnotation> rels = ann_hdlr.getOWLAnnotatedRelations();
 			
 			HashMap<String, Integer> termID = new HashMap<String, Integer>();
 			HashMap<String, OWLIndividual> termOccMap = new HashMap<String, OWLIndividual>();
 			dataBuffer.append("<sem>");
 			int countAnn=0;
 			for(OWLIndividual termOcc : termOccurrences){
 				countAnn++;
 				Collection<RDFProperty> props = termOcc.getRDFProperties();
 				String spottedWords="";
 				String term_offset="";
 				String className="";
 				String instanceName="";
 				for(RDFProperty prop : props){
 					//System.err.println(prop.getName());
 					if(prop.getName().endsWith("document_id")){
 						docID=(String)termOcc.getPropertyValue(prop);
 						//System.err.println(termOcc.getPropertyValue(prop));
 					}
 					if(prop.getName().endsWith("spotted_words")){
 						spottedWords=(String)termOcc.getPropertyValue(prop);
 						//System.err.println(termOcc.getPropertyValue(prop));
 					}
 					if(prop.getName().endsWith("term_offset")){
 						term_offset=(String)termOcc.getPropertyValue(prop);
 						//System.err.println(termOcc.getPropertyValue(prop));
 					}
 					if(prop.getName().endsWith("désigne")){
 						className=((OWLIndividual)termOcc.getPropertyValue(prop)).getRDFType().getLocalName();
 						instanceName=((OWLIndividual)termOcc.getPropertyValue(prop)).getLocalName();
 						//System.err.println(termOcc.getPropertyValue(prop));
 					}
 				}
 				termID.put(instanceName, new Integer(countAnn));
 				termOccMap.put(instanceName, termOcc);
 				//System.err.println("-"+instanceName);
 				dataBuffer.append("<concept id=\""+countAnn+"\">\n");
 				dataBuffer.append("<spotted_word>"+spottedWords+"</spotted_word>\n");
 				dataBuffer.append("<term_offset>"+term_offset+"</term_offset>\n");
 				String cleanTO=term_offset.replace('[', ' ');
 				cleanTO=cleanTO.replace(']', ' ');
 				cleanTO=cleanTO.trim();
 				Integer posFromTV;
 				try{
 					posFromTV= new Integer(cleanTO);
 				} catch(NumberFormatException e){
 					String [] mpos=cleanTO.split(",");
 					posFromTV = new Integer(mpos[0]);
 				}
 				
 				int ourPosition=this.closestValue(posFromTV, offMap.keySet(), spottedWords.length());
 				dataBuffer.append("<paragraph_id>"+getPID(paragraphs, posFromTV.intValue())+"</paragraph_id>\n");
 				dataBuffer.append("<token_id>"+offMap.get(new Integer(ourPosition))+"</token_id>\n");
 				dataBuffer.append("<instance_of>"+className+"</instance_of>\n");
 				dataBuffer.append("</concept>\n");
 			}
 			
 			if(RELATIONS_ENABLED){
 				int countRel=0;
 				for(RelationAnnotation ann : rels){
 					countRel++;
 					OWLIndividual inst1=ann.getDomainItem(); 
 					OWLIndividual inst2=ann.getRangeItem();
 					OWLIndividual term1=termOccMap.get(inst1.getLocalName()); 
 					OWLIndividual term2=termOccMap.get(inst2.getLocalName()); 
 					int c1ID=termID.get(inst1.getLocalName()).intValue();
 					int c2ID=termID.get(inst2.getLocalName()).intValue();
 					dataBuffer.append("<relation id=\""+countRel+"\" c1_id=\""+c1ID+"\" c2_id=\""+c2ID+"\" >\n");
 					dataBuffer.append("<spotted_word>"+ann.getSpottedWords()+"</spotted_word>\n");
 					dataBuffer.append("<spotted_label>"+ann.getSpottedLabel()+"</spotted_label>\n");
 					dataBuffer.append("<instance_of>"+ann.getRelation().getLocalName()+"</instance_of>\n");
 					String domainPos=getTermOffset(term1);
 					String rangePos=getTermOffset(term2);
 					String cleanDTO=domainPos.replace('[', ' ');
 					cleanDTO=cleanDTO.replace(']', ' ');
 					cleanDTO=cleanDTO.trim();
 					//Integer dtoPosTV= new Integer(cleanDTO);
 					Integer dtoPosTV;
 					try{
 						dtoPosTV= new Integer(cleanDTO);
 					} catch(NumberFormatException e){
 						String [] mpos=cleanDTO.split(",");
 						dtoPosTV = new Integer(mpos[0]);
 					}
 					String cleanRTO=rangePos.replace('[', ' ');
 					cleanRTO=cleanRTO.replace(']', ' ');
 					cleanRTO=cleanRTO.trim();
 					Integer rtoPosTV;
 					try{
 						rtoPosTV= new Integer(cleanRTO);
 					} catch(NumberFormatException e){
 						String [] mpos=cleanRTO.split(",");
 						rtoPosTV = new Integer(mpos[0]);
 					}
 					int ourDTOP=this.closestValue(dtoPosTV, offMap.keySet(), domainPos.length());
 					int ourRTOP=this.closestValue(rtoPosTV, offMap.keySet(), rangePos.length());
 					dataBuffer.append("<term_offset>"+domainPos+", "+rangePos+"</term_offset>\n");
 					dataBuffer.append("<paragraph_id>"+getPID(paragraphs, dtoPosTV.intValue())+"</paragraph_id>\n");
 					dataBuffer.append("<token_id>"+offMap.get(new Integer(ourDTOP))+" , "+offMap.get(new Integer(ourRTOP))+"</token_id>\n");
 					dataBuffer.append("</relation>\n");
 				}
 			}
 			dataBuffer.append("</sem>");
 			
 			out.println("<document id = \""+docID+"\">");
 			out.print(dataBuffer.toString());
 			out.println("</document>");
 			out.println("</ontologie>");
 			
 		} catch (IOException e){
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Method that calculates a paragraph ID given the offset of a term occurrence
 	 * @param paragraphs array of paragraphs
 	 * @param parseInt the offset of a term occurrence
 	 * @return the paragraph ID 
 	 */
 	private int getPID(String[] paragraphs, int parseInt) {
 		int charCount=0;
 		int prev=0;
 		int paraCount=1;
 		for(String para : paragraphs){
 			charCount+=para.length();
 			if(parseInt < charCount && parseInt >= prev) return paraCount;
 			charCount++; //for removed \n symbol
 			prev=charCount;
 			paraCount++;
 		}
 		return -1; //FIXME: non dovrebbe mai arrivarci
 		
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		doGet(request, response);
 	}
 	/**
 	 * Method that calculates the alignment between a word offset and the possible offsets of the annotations
 	 * @param value the word offset
 	 * @param candidates the set of offsets of the instances that can be related to the word
 	 * @param wordSize the size of the word
 	 * @return the offset (according to TextViz) of the instance matching the word
 	 */
 	private Integer closestValue(Integer value, Set<Integer> candidates, int wordSize){
 		Integer nearest = new Integer(-1);
 		int bestDistanceFoundYet = Integer.MAX_VALUE;
 		for (Integer i : candidates) {
 		  // if we found the desired number, we return it.
 		  //System.err.println("looking for closest annotation to word: '"+posMap.get(value)+"' at position "+value);
 		  if (i.intValue()==value.intValue()) {
 			//System.err.println("exact match at position"+i);
 		    return i;
 		  } else {
 			if(i.intValue() > value.intValue()){
 				int d = i-value;
 			    if (d < bestDistanceFoundYet && (d < wordSize) ) {
 			    	//System.err.println("near match at position"+i);
 			    	nearest = i;
 			    	bestDistanceFoundYet=d;
 			    }
 			}
 			
 		  }
 		}
 		return nearest;
 	}
 	
 	/*
 
 <ontologie id="http://...">
 <document id = "http://">
 <concept id="1">
 <spotted_word>Glaïeuls hybrides</spotted_word>
 <term_offset>181, 192</term_offset>
 <paragraph_id>...</paragraph_id> (ricomincia a 1 in ogni documento)
 <token_id>...</token_id> (ricomincia a 1 in ogni paragrafo)
 <instance_of>#E_Glaieul_Hybride</instance_of>
 </concept>
 ...
 <relation id="17" c1_id="1" c2_id="2">
 <spotted_word>fleurissent</spotted_word>
 <spotted_label>fleurir</spotted_label>
 <instance_of>#fleuritEn</instance_of>
 <term_offset>181, 192</term_offset>
 <paragraph_id>...</paragraph_id>
 <token_id>...</paragraph_id>
 </relation>
 </document>
 </ontologie>
 
 	 */
 
 }
