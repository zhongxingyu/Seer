 package no.ovitas.compass2.config;
 
 import org.apache.log4j.Logger;
 
 /**
  * @author csanyi
  * 
  */
 public class KnowledgeBases extends BaseConfigContainer<KnowledgeBase> {
 
 	// Attributes
 
 	private Logger logger = Logger.getLogger(this.getClass());
 
 	// Constructors
 
 	public KnowledgeBases() {
 		super();
 		
 	}
 
 	// Methods
 	
 	public KnowledgeBase getKnowledgeBase(String name) {
 		if (name != null && elements.containsKey(name)) {
 			return elements.get(name);
 		} else {
			logger.error("The " + name + " KnowledgeBase is not exist!");
 			return null;
 		}
 	}
 	
 	public String dumpOut(String indent) {
 		String ind = indent + " ";
 		String toDumpOut = ind + "KnowledgeBases\n";
 		toDumpOut += super.dumpOut(ind);
 		
 		return toDumpOut;
 	}
 	
 }
