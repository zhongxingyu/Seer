 package edu.uga.radiant.printTree;
 
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.semanticweb.owlapi.model.OWLClass;
 import org.semanticweb.owlapi.model.OWLOntologyCreationException;
 
 import edu.uga.radiant.ontology.OntologyManager;
 
 public class LoadOWLTree {
 
 	final static String CLASSIMG = "";
 	private static int count = 0;
 	
 	public static void drawTree(StringBuffer buf, OntologyManager mgr) throws IOException {
 		OWLClass thing = mgr.getThing();
 		buf.append("<div id=\"root\"><ul><li id='Thing'><span>"+CLASSIMG+"Thing</span>");
 		buf.append("<ul>");
 		if (thing == null){
 			Set<OWLClass> roots = getTopClasses(mgr);
 			printHierarchy(roots , buf, mgr);
 		}else{
 			printHierarchy(thing, buf, mgr);
 		}
 		buf.append("</ul>");
 		buf.append("</li></ul></div>");
 	}
 
 	private static void printHierarchy(OWLClass clazz, StringBuffer buf, OntologyManager mgr) throws IOException {
 		boolean ul = false;
 		String value = mgr.getClassLabel(clazz);
 		if (!value.contains("Nothing")) {
         	String id = clazz.getIRI().toString();
         	if (!clazz.getIRI().toString().equalsIgnoreCase("http://www.w3.org/2002/07/owl#Thing")){
         		count++;
         		String definition = mgr.getClassDefinition(clazz);
         		if(definition == null){
                     definition = "";
                 }else{
                     definition = definition.replaceAll("\n", "");
                     definition = definition.replaceAll("'", "");
                 }
         		String spanId = "spanidowlclass"+count;
                 String owlClassIRIFragment = charReplace(clazz.getIRI().toString());
                 buf.append("<li id='"+owlClassIRIFragment+"' data='"+definition+"' ><span id='" + spanId + "' title=" + clazz.getIRI() + " value='" + id + "' onclick=\"selectOWLNode('" + definition + "','" + value + "','" + spanId + "')\">" + value + "</span>");
             }
         	
         	Set<OWLClass> subcls = mgr.getDirectSubClasses(clazz);
        	if(subcls.size() >= 1){
                 buf.append("<ul>");
                 ul = true;
             }
             /* Find the children and recurse */
             for (OWLClass child : subcls){
                 if (!child.equals(clazz)) {
                     printHierarchy(child, buf, mgr);
                 }
             }
             if(ul) buf.append("</ul>");
             buf.append("</li>");
         }
 	 		
 	}
 	
 	private static void printHierarchy(Set<OWLClass> subcls, StringBuffer buf, OntologyManager mgr) throws IOException {
 		/* Find the children and recurse */
         for (OWLClass child : subcls){
             printHierarchy(child, buf, mgr);
         }
         buf.append("</li>");
 	}
 
 	public static String charReplace(String iri) {
 		return iri.replace('.', '_').replace('/', '_').replace(':', '_').replace('#', '_');
 	}
 	
 	public static Set<OWLClass> getTopClasses(OntologyManager mgr) {
 		Set<OWLClass> subclasses = new HashSet<OWLClass>();
         for ( OWLClass cls : mgr.getAllOWLclass().values()){
             for ( OWLClass sbcls : mgr.getDirectSubClasses(cls)){
             	subclasses.add(sbcls);
             }
         }
         Set<OWLClass> classes = new HashSet<OWLClass>();
         classes.addAll(mgr.getAllOWLclass().values());
         classes.removeAll(subclasses);
         return classes;
 	}
 	
 	public static void main(String[] args) throws IOException, OWLOntologyCreationException {
 		
 		OntologyManager mgr = OntologyManager.getInstance("D:/SUMO_Finance.owl");
 		
 		System.out.println("mgr class size = " + mgr.getAllOWLclass().keySet().size());
 		
 		OntologyManager mgr1 = OntologyManager.getInstance("D:/webService.owl");
 		
 		System.out.println("mgr1 class size = " + mgr1.getAllOWLclass().keySet().size());
 		
 		OntologyManager mgr2 = OntologyManager.getInstance("D:/SUMO_Finance.owl");
 		
 		System.out.println("mgr2 class size = " + mgr2.getAllOWLclass().keySet().size());
 		
 		
 	}
 	
 }
