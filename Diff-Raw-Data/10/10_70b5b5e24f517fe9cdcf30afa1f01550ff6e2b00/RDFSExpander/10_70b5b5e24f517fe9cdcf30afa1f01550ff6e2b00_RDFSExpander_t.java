 package unibz.it.edu;
 
 import java.util.List;
 
 import unibz.it.edu.rdfElements.RDFGraph;
 import unibz.it.edu.rdfElements.RDFObject;
 import unibz.it.edu.rdfElements.RDFTriplet;
 import unibz.it.edu.rdfElements.RDFUri;
 
 /**
  * Expands the RDF graph with entailed RDFS triplets
  * 
  * @author digy
  * 
  */
 public class RDFSExpander extends RDFExpander {
 
 	private final static String rdfsns = "http://www.w3.org/2000/01/rdf-schema#";
 
 	private final static String[][] axioms = {
 			{ "rdf:type", "rdfs:domain", "rdfs:Resource" },
 			{ "rdfs:domain", "rdfs:domain", "rdf:Property" },
 			{ "rdfs:range", "rdfs:domain", "rdf:Property" },
 			{ "rdfs:subPropertyOf", "rdfs:domain", "rdf:Property" },
 			{ "rdfs:subClassOf", "rdfs:domain", "rdfs:Class" },
 			{ "rdf:subject", "rdfs:domain", "rdf:Statement" },
 			{ "rdf:predicate", "rdfs:domain", "rdf:Statement" },
 			{ "rdf:object", "rdfs:domain", "rdf:Statement" },
 			{ "rdfs:member", "rdfs:domain", "rdfs:Resource" },
 			{ "rdf:first", "rdfs:domain", "rdf:List" },
 			{ "rdf:rest", "rdfs:domain", "rdf:List" },
 			{ "rdfs:seeAlso", "rdfs:domain", "rdfs:Resource" },
 			{ "rdfs:isDefinedBy", "rdfs:domain", "rdfs:Resource" },
 			{ "rdfs:comment", "rdfs:domain", "rdfs:Resource" },
 			{ "rdfs:label", "rdfs:domain", "rdfs:Resource" },
 			{ "rdf:value", "rdfs:domain", "rdfs:Resource" },
 
 			{ "rdf:type", "rdfs:range", "rdfs:Class" },
 			{ "rdfs:domain", "rdfs:range", "rdfs:Class" },
 			{ "rdfs:range", "rdfs:range", "rdfs:Class" },
 			{ "rdfs:subPropertyOf", "rdfs:range", "rdf:Property" },
 			{ "rdfs:subClassOf", "rdfs:range", "rdfs:Class" },
 			{ "rdf:subject", "rdfs:range", "rdfs:Resource" },
 			{ "rdf:predicate", "rdfs:range", "rdfs:Resource" },
 			{ "rdf:object", "rdfs:range", "rdf:Resource" },
 			{ "rdfs:member", "rdfs:range", "rdfs:Resource" },
 			{ "rdf:first", "rdfs:range", "rdfs:Resource" },
 			{ "rdf:rest", "rdfs:range", "rdf:List" },
 			{ "rdfs:seeAlso", "rdfs:range", "rdfs:Resource" },
 			{ "rdfs:isDefinedBy", "rdfs:range", "rdfs:Resource" },
 			{ "rdfs:comment", "rdfs:range", "rdfs:Resource" },
 			{ "rdfs:label", "rdfs:range", "rdfs:Literal" },
 			{ "rdf:value", "rdfs:range", "rdfs:Resource" },
 
 			{ "rdfs:ContainerMembershipProperty", "rdfs:subClassOf",
 					"rdf:Property" },
 			{ "rdf:Alt", "rdfs:subClassOf", "rdfs:Container" },
 			{ "rdf:Bag", "rdfs:subClassOf", "rdfs:Container" },
 			{ "rdf:Seq", "rdfs:subClassOf", "rdfs:Container" } };
 
 	@Override
 	public void build_axioms() {
 		super.build_axioms();
 
 		for (int i = 0; i < axioms.length; ++i) {
 
 			String sub_name = uri_name(axioms[i][0]);
 			String sub_ns = uri_namespace(axioms[i][0]);
 
 			String pred_name = uri_name(axioms[i][1]);
 			String pred_ns = uri_namespace(axioms[i][1]);
 
 			String obj_name = uri_name(axioms[i][2]);
 			String obj_ns = uri_namespace(axioms[i][2]);
 
 			axiomatic_tiplets.add(new RDFTriplet(new RDFUri(sub_name, sub_ns),
 					new RDFUri(pred_name, pred_ns),
 					new RDFUri(obj_name, obj_ns)));
 		}
 
 	}
 
 	private String uri_name(String value) {
 		return value.split(":")[1];
 	}
 
 	private String uri_namespace(String value) {
 		String ns = value.split(":")[0];
 		if (ns.equals("rdf")) {
 			return rdfns;
 		} else {
 			return rdfsns;
 		}
 	}
 
 	@Override
 	public void expand(RDFGraph data) {
 		boolean cnt = true;
 		do {
 			cnt = false;
 			if (rdf1(data)) {
 				cnt = true;
 			}
 			if (rdfs2(data)) {
 				cnt = true;
 			}
 			if (rdfs3(data)) {
 				cnt = true;
 			}
 			if (rdfs4(data)) {
 				cnt = true;
 			}
 			if (rdfs5(data)) {
 				cnt = true;
 			}
 			if (rdfs6(data)) {
 				cnt = true;
 			}
 			if (rdfs7(data)) {
 				cnt = true;
 			}
 			if (rdfs8(data)) {
 				cnt = true;
 			}
 			if (rdfs9(data)) {
 				cnt = true;
 			}
 			if (rdfs10(data)) {
 				cnt = true;
 			}
 			if (rdfs11(data)) {
 				cnt = true;
 			}
 
 		} while (cnt);
 	}
 
 	private boolean rdfs2(RDFGraph data) {
 		List<RDFTriplet> triples = data.getTriplets();
 		for (RDFTriplet trp : triples) {
 			RDFObject sub = trp.getSubject();
 			RDFObject pred = trp.getPredicate();
 			RDFObject obj = trp.getObject();
 			if (pred.equals(new RDFUri(rdfsns + "rdfs:domain"))) {
 				for (RDFTriplet trp2 : triples) {
 					RDFObject sub2 = trp2.getSubject();
 					RDFObject pred2 = trp2.getPredicate();
 					RDFObject obj2 = trp2.getObject();
 					RDFTriplet _type = new RDFTriplet(sub2, new RDFUri(rdfns
 							+ "rdf:type"), obj);
 					if ((pred2.equals(sub)) && (!triples.contains(_type))) {
 						triples.add(_type);
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	private boolean rdfs3(RDFGraph data) {
 		List<RDFTriplet> triples = data.getTriplets();
 		for (RDFTriplet trp : triples) {
 			RDFObject sub = trp.getSubject();
 			RDFObject pred = trp.getPredicate();
 			RDFObject obj = trp.getObject();
 			if (pred.equals(new RDFUri(rdfsns + "rdfs:range"))) {
 				for (RDFTriplet trp2 : triples) {
 					RDFObject sub2 = trp2.getSubject();
 					RDFObject pred2 = trp2.getPredicate();
 					RDFObject obj2 = trp2.getObject();
 					RDFTriplet _type = new RDFTriplet(obj2, new RDFUri(rdfns
 							+ "rdf:type"), obj);
 					if ((pred2.equals(sub)) && (!triples.contains(_type))) {
 						triples.add(_type);
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Subjects and Objects must be rdfs:Resource
 	 * 
 	 * @param data
 	 * @return
 	 */
 	private boolean rdfs4(RDFGraph data) {
 		List<RDFTriplet> triples = data.getTriplets();
 		for (RDFTriplet trp : triples) {
 
 			RDFObject sub = trp.getSubject();
 			RDFObject obj = trp.getObject();
 
 			RDFTriplet _res1 = new RDFTriplet(sub, new RDFUri(rdfns
 					+ "rdf:type"), new RDFUri(rdfsns + "rdfs:Resource"));
 			RDFTriplet _res2 = new RDFTriplet(obj, new RDFUri(rdfns
 					+ "rdf:type"), new RDFUri(rdfsns + "rdfs:Resource"));
 			if (!triples.contains(_res1)) {
 				triples.add(_res1);
 				return true;
 			} else if (!triples.contains(_res2)) {
 				triples.add(_res2);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Subproperties
 	 * 
 	 * @param data
 	 * @return
 	 */
 	private boolean rdfs5(RDFGraph data) {
 		List<RDFTriplet> triples = data.getTriplets();
 		for (RDFTriplet trp : triples) {
 			RDFObject sub = trp.getSubject();
 			RDFObject pred = trp.getPredicate();
 			RDFObject obj = trp.getObject();
 			if (pred.equals(new RDFUri(rdfsns + "rdfs:SubPropertyOf"))) {
 				for (RDFTriplet trp2 : triples) {
 					RDFObject sub2 = trp2.getSubject();
 					RDFObject pred2 = trp2.getPredicate();
 					RDFObject obj2 = trp2.getObject();
 					RDFTriplet _sub = new RDFTriplet(sub, new RDFUri(rdfsns
 							+ "rdfs:SubPropertyOf"), obj2);
 					if (pred2.equals(new RDFUri(rdfsns + "rdfs:SubPropertyOf"))
 							&& obj.equals(sub2) && !triples.contains(_sub)) {
 						triples.add(_sub);
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	private boolean rdfs6(RDFGraph data) {
 		List<RDFTriplet> triples = data.getTriplets();
 		for (RDFTriplet trp : triples) {
 			RDFObject sub = trp.getSubject();
 			RDFObject pred = trp.getPredicate();
 			RDFObject obj = trp.getObject();
 			RDFTriplet _sub = new RDFTriplet(sub, new RDFUri(rdfsns
 					+ "rdfs:subPropertyOf"), sub);
 
 			if (pred.equals(new RDFUri(rdfns + "rdf:type"))
 					&& obj.equals(new RDFUri(rdfns + "rdf:Property"))
 					&& !triples.contains(_sub)) {
 				triples.add(_sub);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private boolean rdfs7(RDFGraph data) {
 		List<RDFTriplet> triples = data.getTriplets();
 		for (RDFTriplet trp : triples) {
 			RDFObject sub = trp.getSubject();
 			RDFObject pred = trp.getPredicate();
 			RDFObject obj = trp.getObject();
 			if (pred.equals(new RDFUri(rdfsns + "rdfs:subPropertyOf"))) {
 				for (RDFTriplet trp2 : triples) {
 					RDFObject sub2 = trp2.getSubject();
 					RDFObject pred2 = trp2.getPredicate();
 					RDFObject obj2 = trp2.getObject();
 					RDFTriplet _subprop = new RDFTriplet(sub2, obj, obj2);
 					if (sub.equals(pred2) && !triples.contains(_subprop)) {
 						triples.add(_subprop);
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	private boolean rdfs8(RDFGraph data) {
 		List<RDFTriplet> triples = data.getTriplets();
 		for (RDFTriplet trp : triples) {
 			RDFObject sub = trp.getSubject();
 			RDFObject pred = trp.getPredicate();
 			RDFObject obj = trp.getObject();
 			RDFTriplet _subclass = new RDFTriplet(sub, new RDFUri(rdfsns
 					+ "rdfs:subClassOf"), new RDFUri(rdfns + "rdfs:Resource"));
 			if (pred.equals(new RDFUri(rdfns + "rdf:type"))
 					&& obj.equals(new RDFUri(rdfsns + "rdfs:Class"))
 					&& !triples.contains(_subclass)) {
 				triples.add(_subclass);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private boolean rdfs9(RDFGraph data) {
 		List<RDFTriplet> triples = data.getTriplets();
 		for (RDFTriplet trp : triples) {
 			RDFObject sub = trp.getSubject();
 			RDFObject pred = trp.getPredicate();
 			RDFObject obj = trp.getObject();
 			if (pred.equals(new RDFUri(rdfsns + "rdfs:subClassOf"))) {
 				for (RDFTriplet trp2 : triples) {
 					RDFObject sub2 = trp2.getSubject();
 					RDFObject pred2 = trp2.getPredicate();
 					RDFObject obj2 = trp2.getObject();
 					RDFTriplet _type = new RDFTriplet(sub2, new RDFUri(rdfns
 							+ "rdf:type"), obj);
 					if (pred2.equals(new RDFUri(rdfns + "rdf:type"))
 							&& obj2.equals(sub) && !triples.contains(_type)) {
 						triples.add(_type);
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	private boolean rdfs10(RDFGraph data) {
 		List<RDFTriplet> triples = data.getTriplets();
 		for (RDFTriplet trp : triples) {
 			RDFObject sub = trp.getSubject();
 			RDFObject pred = trp.getPredicate();
 			RDFObject obj = trp.getObject();
 			RDFTriplet _subclass = new RDFTriplet(sub, new RDFUri(rdfsns
 					+ "rdfs:subClassOf"), sub);
 			if (pred.equals(new RDFUri(rdfns + "rdf:type"))
 					&& obj.equals(new RDFUri(rdfsns + "rdfs:Class"))
 					&& !triples.contains(_subclass)) {
 				triples.add(_subclass);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private boolean rdfs11(RDFGraph data) {
 		List<RDFTriplet> triples = data.getTriplets();
 		for (RDFTriplet trp : triples) {
 			RDFObject sub = trp.getSubject();
 			RDFObject pred = trp.getPredicate();
 			RDFObject obj = trp.getObject();
 			if (pred.equals(new RDFUri(rdfsns + "rdfs:subClassOf"))) {
 				for (RDFTriplet trp2 : triples) {
 					RDFObject sub2 = trp2.getSubject();
 					RDFObject pred2 = trp2.getPredicate();
 					RDFObject obj2 = trp2.getObject();
 					RDFTriplet _ss = new RDFTriplet(sub, new RDFUri(rdfsns
 							+ "rdfs:subClassOf"), obj2);
 					if (pred2.equals(new RDFUri(rdfsns + "rdfs:subClassOf"))
 							&& !triples.contains(_ss)) {
 						triples.add(_ss);
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 }
