 package ixa.entity.linking;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.HashMap;
 
 import ixa.kaflib.KAFDocument;
 import ixa.kaflib.WF;
 import ixa.kaflib.Entity;
 import ixa.kaflib.Term;
 import ixa.kaflib.ExternalRef;
 
 import org.dbpedia.spotlight.exceptions.AnnotationException;
 import org.dbpedia.spotlight.model.DBpediaResource;
 import org.dbpedia.spotlight.model.Text;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Node;
 import org.w3c.dom.Element;
 
 public class Annotate {
 
     DBpediaSpotlightClient c;
     public Annotate(){
 	c = new DBpediaSpotlightClient ();
     }
 
     public void disambiguateNEsToKAF (KAFDocument kaf) throws Exception { 
 	kaf.addLinguisticProcessor("entity-linking", "ixa-dbpedia-spotlight", "1.0");
 	String lang = kaf.getLang();
 	String annotation = KAF2XMLSpot(kaf);
 	Document response = annotate(annotation,lang);
 	XMLSpot2KAF(kaf,response);
     }
 
     private String KAF2XMLSpot(KAFDocument kaf){ 
 	/*
 	  <annotation text="Brazilian oil giant Petrobras and U.S. oilfield service company Halliburton have signed a technological cooperation agreement, Petrobras announced Monday. The two companies agreed on three projects: studies on contamination of fluids in oil wells, laboratory simulation of well production, and research on solidification of salt and carbon dioxide formations, said Petrobras. Twelve other projects are still under negotiation.">
 	  <surfaceForm name="oil" offset="10"/>
 	  <surfaceForm name="company" offset="56"/>
 	  <surfaceForm name="Halliburton" offset="64"/>
 	  <surfaceForm name="oil" offset="237"/>
 	  <surfaceForm name="other" offset="383"/>
 	  </annotation>
 	*/
 	
 	String text = "";
 	List<List<WF>> sentences = kaf.getSentences();
 	for (List<WF> sentence : sentences) {
 	    for (int i = 0; i < sentence.size(); i++) {
 		if (!text.isEmpty()) {
 		    text += " ";
 		}
		String tok = sentence.get(i).getForm();
		if (tok.contains("\"")){
		    tok = tok.replaceAll("\"","'");
		}
		text += tok;
 	    }
 	}
 	
 	int offset = -1;
 	String entStr = "";
 	String forms = "";
 	List<Entity> entities = kaf.getEntities();
 	for (Entity entity : entities){
 	    List<List<Term>> references = entity.getReferences();
 	    for (List<Term> ref : references){
 		offset = -1;
 		entStr = "";
 		for (Term t: ref){
 		    List<WF> words = t.getWFs();
 		    for (int i = 0; i < words.size(); i++){
 			if (!entStr.isEmpty()){
 			    entStr += " ";
 			}
 			WF word = words.get(i);
 			entStr += word.getForm();
 			if (offset == -1){
 			    if (word.hasOffset()){
 				offset = word.getOffset();
 			    }
 			    else{
 				System.out.println("There is not offset for word id " + word.getId());
 			    }
 			}
 		    }
 		}
 	    }
 	    // Each reference is a spot to disambiguate
 	    forms += "<surfaceForm name=\"" + entStr + "\" offset=\"" + offset + "\"/>\n";
 	}
 	
 	String annotation = "<annotation text=\"" + text + "\">\n" + forms + "</annotation>";
 	return annotation;
     }
 
     private Document annotate(String annotation,String lang) throws AnnotationException {
 	Document response = c.extract(new Text(annotation),lang);
 	return response;
     }
     
     private void XMLSpot2KAF(KAFDocument kaf, Document doc){
 	// Store the References into a hash. Key: offset
 	HashMap<Integer,String> refs = new HashMap<Integer,String>();
 
 
 	doc.getDocumentElement().normalize();
 	NodeList nList = doc.getElementsByTagName("Resource");
 		    
 	for (int temp = 0; temp < nList.getLength(); temp++) {
 	    
 	    Node nNode = nList.item(temp);
 	    
 	    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
 		
 		Element eElement = (Element) nNode;
 		refs.put(new Integer(eElement.getAttribute("offset")),eElement.getAttribute("URI"));		
 
 	    }
 	}
 	
 
 	String resource = "spotlight_en_v1";
 	List<Entity> entities = kaf.getEntities();
 	for (Entity entity : entities){
 	    // Get the offset of the entity
 	    int offset = getEntityOffset(entity);
	    if (refs.containsKey(offset)){
		String reference = refs.get(offset);
		// Create ExternalRef
		ExternalRef externalRef = kaf.createExternalRef(resource,reference);	    
		// addExternalRef to Entity
		entity.addExternalRef(externalRef);
	    }
 	}
     }
 
     private int getEntityOffset(Entity ent){
 	int offset = -1;
 	List<List<Term>> references = ent.getReferences();
 	for (List<Term> ref : references){
 	    for (Term t: ref){
 		List<WF> words = t.getWFs();
 		for (int i = 0; i < words.size(); i++){
 		    WF word = words.get(i);
 		    if (word.hasOffset()){
 			return word.getOffset();
 		    }
 		}
 	    }
 	}
 	return offset;
     }
 }
