 package unibz.it.edu.parsers;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import unibz.it.edu.rdfElements.RDFGraph;
 import unibz.it.edu.rdfElements.RDFLiteral;
 import unibz.it.edu.rdfElements.RDFObject;
 import unibz.it.edu.rdfElements.RDFTriplet;
 import unibz.it.edu.rdfElements.RDFUri;
 
 public class TurtleParser {
 
 	public static RDFGraph decode(File file) {
 		return null;
 	}
 
 	public static StringBuilder encode(RDFGraph data) {
 
 		StringBuilder result = new StringBuilder();
 
 		Map<String, String> ns = data.getNamespaces();
 		for (String name : ns.keySet()) {
 			result.append(String.format("@prefix %s: <%s>\n", ns.get(name), name));
 		}
 
 		List<RDFTriplet> triplets = data.getTriplets();
 		
 		Map<RDFUri, List<RDFTriplet>> subject_groups = new HashMap<RDFUri, List<RDFTriplet>>();
 		for (RDFTriplet trp : triplets) {
 			if (subject_groups.containsKey(trp.getSubject())) {
 				subject_groups.get(trp.getSubject()).add(trp);
 			} else {
 				subject_groups.put(trp.getSubject(), new LinkedList<RDFTriplet>());
 				subject_groups.get(trp.getSubject()).add(trp);
 			}
 		}
 		for (RDFUri sub: subject_groups.keySet()) {
 			result.append(toPrefix(sub, ns));
 			for (RDFTriplet trp: subject_groups.get(sub)) {
 				String pred = toPrefix(trp.getPredicate(), ns);
 				String obj = _toPrefix(trp.getObject(), ns);
 				result.append(pred);
 				result.append(" ");
 				result.append(obj);
				result.append(";\n");	
 			}
 			result.replace(result.length()-2, result.length()-1, ".");
 		}
 		return result;
 	}
 	
 	private static String toPrefix(RDFUri elem, Map<String, String> ns) {
 		if (ns.containsKey(elem.getNamespace())) {
 			return ns.get(elem.getNamespace())+ ":" + elem.getValue();
 		} else {
 			return String.format("<%s%s>", elem.getNamespace(), elem.getValue());
 		}	
 	}
 	private static String _toPrefix(RDFObject elem, Map<String, String> ns) {
 		if (elem instanceof RDFUri) {
 			return toPrefix((RDFUri)elem, ns);
 		} else if (elem instanceof RDFLiteral){
 			System.out.println(elem);
 			return String.format("\"%s\"", elem.getValue());
 		}
 		return ""; //Just to shut up java compiler
 	}
 
 }
