 package maro.wrapper;
 
 // java imports
 import java.io.File;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.SortedSet;
 import java.util.HashMap;
 import java.util.Iterator;
 
 // owl-api imports
 import org.semanticweb.owlapi.model.*;
 import org.semanticweb.owlapi.reasoner.*;
 import org.semanticweb.owlapi.vocab.OWL2Datatype;
 import org.semanticweb.owlapi.apibinding.OWLManager;
 import org.semanticweb.owlapi.util.DefaultPrefixManager;
 
 // others imports
 //import org.semanticweb.HermiT.Configuration;
 import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
 
 // LATER maybe, a cache here?
 public class OwlApi implements java.lang.Iterable<Dumper> {
 
 	protected Boolean dirty = null;
 	protected OWLOntologyManager manager = null;
 	protected ReasonerFactory factoryReasoner = null;
 	protected OWLDataFactory factoryData = null;
 	protected OWLOntology ontology = null;
 	protected OWLReasoner reasoner = null;
 	protected DefaultPrefixManager pm = null;
 	// lower case X name real
 	protected HashMap<Integer, HashMap<String, String>> allTranslated = null;
 	// name real X type domain with range
 	protected HashMap<String, String> relationsType = null;
 	final private String sameas = "sameAs";
 
 	public OwlApi() {
 		manager = OWLManager.createOWLOntologyManager();
 		factoryReasoner = new ReasonerFactory();
 		factoryData = manager.getOWLDataFactory();
 		dirty = false;
 	}
 
 	public void loadOntologyFromIRI(IRI iri) throws Exception {
 		if (iri == null) {
 			throw new Exception("Parameter expected `iri' is nulled");
 		}
 
 		try {
 			ontology = manager.loadOntologyFromOntologyDocument(iri);
 
 			if (ontology == null) {
 				throw new Exception("Load failed by unknow error!");
 			}
 
 			loadAllConceptsAndRelations();
 			//} catch (java.lang.NullPointerException e) {
 			//    Probable manager fault...
 	} catch (OWLOntologyCreationException e) {
 		throw e;
 		//e.printStackTrace();
 		//System.out.println(e.toString());
 	}
 
 	dirty = true;
 	reloadReasoner();
 	pm = new DefaultPrefixManager();
 	pm.setDefaultPrefix(ontology.getOntologyID().getOntologyIRI() + "#");
 	}
 
 	public void loadOntologyFromFile(String filename) throws Exception {
 		File file = new File(filename);
 
 		if (file.canRead() == false) {
 			throw new Exception("File `" + filename + "' can't be read!");
 			//return false;
 		}
 
 		this.loadOntologyFromIRI(IRI.create(file));
 	}
 
 	protected void loadAllConceptsAndRelations() throws Exception {
 		HashMap<String, String> depth;
 
 		allTranslated = new HashMap<Integer, HashMap<String, String>>();
 		relationsType = new HashMap<String, String>();
 
 		depth = allTranslated.get(1);
 		if (depth == null) {
 			depth = new HashMap<String, String>();
 			allTranslated.put(1, depth);
 		}
 
 		for (Relation o : getConcepts()) {
 			String target = o.relation().toString();
 			String str = o.relation().getFragment().toLowerCase();
 			if (relationsType.get(target) != null) {
 				throw new Exception("Element `" + target + "' exist! This "
 						+ "class to work need that all concepts and "
 						+ "relations have a unique name because a "
 						+ "context global are built!");
 			}
 			relationsType.put(target, "C" + o.type()); // (C)oncept
 			depth.put(str, target);
 		}
 
 		depth = allTranslated.get(2);
 		if (depth == null) {
 			depth = new HashMap<String, String>();
 			allTranslated.put(2, depth);
 		}
 
 		for (Relation o : getObjectProperties()) {
 			String target = o.relation().toString();
 			String str = o.relation().getFragment().toLowerCase();
 			if (relationsType.get(target) != null) {
 				throw new Exception("Element `" + target + "' exist! This "
 						+ "class to work need that all concepts and "
 						+ "relations have a unique name because a "
 						+ "context global are built!");
 			}
 			relationsType.put(target, "O" + o.type()); // (O)bject
 			depth.put(str, target);
 		}
 
 		for (Relation o : getDataProperties()) {
 			String target = o.relation().toString();
 			String str = o.relation().getFragment().toLowerCase();
 			if (relationsType.get(target) != null) {
 				throw new Exception("Element `" + target + "' exist! This "
 						+ "class to work need that all concepts and "
 						+ "relations have a unique name because a "
 						+ "context global are built!");
 			}
 			relationsType.put(target, "D" + o.type()); // (D)ata
 			depth.put(str, target);
 		}
 
 		depth = allTranslated.get(0);
 		if (depth == null) {
 			depth = new HashMap<String, String>();
 			allTranslated.put(0, depth);
 		}
 
 		if (relationsType.get(sameas) != null) {
 			throw new Exception("Element `" + sameas + "' exist! This "
 					+ "class to work need that all concepts and "
 					+ "relations have a unique name because a "
 					+ "context global are built!");
 		}
 		relationsType.put(sameas, "SI"); // (S) pecial
 		depth.put("sameas", sameas); // Special Case
 
 		if (relationsType.get("~" + sameas) != null) {
 			throw new Exception("Element `~" + sameas + "' exist! This "
 					+ "class to work need that all concepts and "
 					+ "relations have a unique name because a "
 					+ "context global are built!");
 		}
 		relationsType.put("~" + sameas, "SI"); // (S) pecial
 		depth.put("~sameas", "~" + sameas); // Special Case
 
 	}
 
 	public Set<Relation> getConcepts() {
 		Set<Relation> rw = new HashSet<Relation>();
 		Set<OWLClass> soc;
 
 		if (ontology == null) {
 			return rw;
 		}
 		reloadReasoner();
 
 		soc = ontology.getClassesInSignature(true);
 
 		for (OWLClass oc : soc) {
 			if (oc.isOWLThing() == false) {
 				rw.add(new Relation(oc.getIRI()));
 			}
 		}
 
 		return rw;
 	}
 
 	public Set<Relation> getObjectProperties() {
 		Set<Relation> rw = new HashSet<Relation>();
 		Set<OWLObjectProperty> soop;
 
 		if (ontology == null) {
 			return rw;
 		}
 		reloadReasoner();
 
 		soop = ontology.getObjectPropertiesInSignature(true);
 
 		for (OWLObjectProperty oop : soop) {
 			if (oop.isOWLTopObjectProperty() == false) {
 				rw.add(new Relation(oop.getIRI()));
 			}
 		}
 
 		return rw;
 	}
 
 	public Set<Relation> getDataProperties() {
 		Set<Relation> rw = new HashSet<Relation>();
 		Set<OWLDataProperty> sodp;
 
 		if (ontology == null) {
 			return rw;
 		}
 		reloadReasoner();
 
 		sodp = ontology.getDataPropertiesInSignature(true);
 
 		for (OWLDataProperty odp : sodp) {
 			if (odp.isOWLTopDataProperty() == false) {
 				Set<OWLDataRange> odt = odp.getRanges(manager.getOntologies());
 				if (odt.size() > 0) {
 					rw.add(new Relation(
 								odp.getIRI(),
 								odt.toArray()[0].toString()));
 				} else {
 					rw.add(new Relation(
 								odp.getIRI(),
 								"myd:V"));
 				}
 			}
 		}
 
 		return rw;
 	}
 
 	protected void reloadReasoner() {
 		if (dirty == false) {
 			return;
 		}
 
 		if (reasoner != null) {
 			reasoner.dispose();
 			reasoner = null;
 		}
 
 		//// This code is used to debug and get explanation, only ignore it
 		//Configuration configuration=new Configuration();
 		//configuration.throwInconsistentOntologyException=false;
 		//reasoner=factoryReasoner.createReasoner(ontology, configuration);
 		reasoner = factoryReasoner.createReasoner(ontology);
 		//// Aparentemente nao executando isso nao deu problema
 		//precomputeInferences();
 		dirty = false;
 	}
 
 	//	protected void
 	//	precomputeInferences() {
 	//		Set<InferenceType> inferences=new HashSet<InferenceType>();
 	//		inferences.add(InferenceType.CLASS_HIERARCHY);
 	//		inferences.add(InferenceType.OBJECT_PROPERTY_HIERARCHY);
 	//		inferences.add(InferenceType.DATA_PROPERTY_HIERARCHY);
 	//		inferences.add(InferenceType.CLASS_ASSERTIONS);
 	//		inferences.add(InferenceType.OBJECT_PROPERTY_ASSERTIONS);
 	//		inferences.add(InferenceType.DATA_PROPERTY_ASSERTIONS);
 	//		inferences.add(InferenceType.SAME_INDIVIDUAL);
 	//		inferences.add(InferenceType.DIFFERENT_INDIVIDUALS);
 	//		inferences.add(InferenceType.DISJOINT_CLASSES);
 	//		reasoner.precomputeInferences(inferences.toArray(new InferenceType[0]));
 	//	}
 	protected String getCorrectName(int arity, String functor) {
 		String fin;
 		HashMap<String, String> ret;
 
 		fin = functor.toLowerCase();
 		ret = allTranslated.get(arity);
 		if (ret == null) {
 			return null;
 		}
 		return ret.get(fin);
 	}
 
 	public boolean isRelevant(int arity, String functor) {
 		String ret = getCorrectName(arity, functor);
 		return ret != null;
 	}
 
 	protected boolean changeBelief(int arity, String functor,
 			String[] terms, Dumper[] annots, boolean isAdd) {
 		Set<OWLAnnotation> sa;
 		String fullName;
 		String type; // domain -> target
 		boolean ret;
 
 		fullName = getCorrectName(arity, functor);
 		if (fullName == null) {
 			return false;
 		}
 		type = relationsType.get(fullName);
 		if (type == null) {
 			return false;
 		}
 
 		sa = convertToAnnotation(annots);
 
 		ret = true;
 		switch (type.charAt(0)) {
 			case 'C': // oncept
 				changeInstance(terms, fullName, sa, isAdd);
 				break;
 			case 'O': // bject relation
 				changeObjectAssertion(terms, fullName, sa, isAdd);
 				break;
 			case 'D': // ata relation
 				changeDataAssertion(terms, fullName, sa, isAdd);
 				break;
 			case 'S': // pecial or same/different relation
 				changeSpecialAssertion(terms, fullName, sa, isAdd);
 				break;
 			default:
 				ret = false;
 				break;
 		}
 		return ret;
 	}
 
 	public boolean add(int arity, String functor, String[] terms, Dumper[] annots) {
 		return changeBelief(arity, functor, terms, annots, true);
 	}
 
 	public boolean remove(int arity, String functor, String[] terms, Dumper[] annots) {
 		if (terms == null && annots == null) {
 			return abolish(arity, functor);
 		}
 		return changeBelief(arity, functor, terms, annots, false);
 	}
 
 	protected Set<OWLAnnotation> convertToAnnotation(Dumper[] d) {
 		HashSet<OWLAnnotation> ret = new HashSet<OWLAnnotation>();
 		OWLAnnotationProperty annotP =
 			factoryData.getOWLAnnotationProperty(":jason", pm);
 
 		if (d == null) {
 			return ret;
 		}
 		d = Dumper.addOntology(d,
 				ontology.getOntologyID().getOntologyIRI().toString());
 
 		OWLAnnotation annotation =
 			factoryData.getOWLAnnotation(
 					annotP,
 					factoryData.getOWLLiteral(
 						Dumper.asXmlBean(d)));
 		ret.add(annotation);
 		return ret;
 	}
 
 	protected Dumper[] convertFromAnnotation(OWLAxiom axiom) {
 		LinkedList<Dumper> ret = new LinkedList<Dumper>();
 		OWLAnnotationProperty annotP =
 			factoryData.getOWLAnnotationProperty(":jason", pm);
 		if (axiom == null) {
 			return ret.toArray(new Dumper[0]);
 		}
 		Set<OWLAnnotation> annot =
 			axiom.getAnnotations(annotP);
 		if (annot == null) {
 			return ret.toArray(new Dumper[0]);
 		}
 		for (OWLAnnotation a : annot) {
 			String s = ((OWLLiteral) a.getValue()).getLiteral();
 			Object array = Dumper.fromXmlBean(s);
 
 			for (Dumper du : (Dumper[]) array) {
 				if (ret.contains(du) == false) {
 					ret.add(du);
 				}
 			}
 		}
 		return ret.toArray(new Dumper[0]);
 	}
 
 	protected void afterChange(OWLAxiom a, boolean isAdd) {
 		if (isAdd == true) {
 			manager.addAxiom(ontology, a);
 		} else {
 			manager.removeAxiom(ontology, a);
 		}
 
 		dirty = true;
 	}
 
 	// className eh full
 	// individualName eh abreviado
 	protected void changeInstance(String[] individualName, String className,
 			Set<OWLAnnotation> annot, boolean isAdd) {
 		if (individualName == null) {
 			return;
 		}
 
 		OWLNamedIndividual ni =
 			factoryData.getOWLNamedIndividual(individualName[0], pm);
 		OWLClass cl = factoryData.getOWLClass(IRI.create(className));
 		OWLAxiom a = factoryData.getOWLClassAssertionAxiom(cl, ni, annot);
 
 		// So pode ter um axioma
 		Set<OWLClassAssertionAxiom> ocaas =
 			ontology.getClassAssertionAxioms(ni);
 		if (ocaas != null) {
 			for (OWLClassAssertionAxiom ocaa : ocaas) {
 				if (ocaa.getClassExpression() == (OWLClassExpression) cl) {
 					if (isAdd) {
 						manager.removeAxiom(ontology, ocaa);
 					} else {
 						a = ocaa; // como so pode ter um pegamos o que esta lah
 					}
 				}
 			}
 		}
 
 		afterChange(a, isAdd);
 	}
 
 	protected void changeDataAssertion(String[] individualName,
 			String relationName, Set<OWLAnnotation> annot, boolean isAdd) {
 		OWLDatatype odt;
 		OWLLiteral oli;
 		OWLDataProperty odp;
 		OWLNamedIndividual ni;
 		OWLAxiom a;
 
 		String relationValid = relationsType.get(relationName);
 		if (relationValid.equals("Ds")) {
 			odt = factoryData.getOWLDatatype(OWL2Datatype.XSD_STRING.getIRI());
 		} else if (relationValid.equals("Di")) {
 			odt = factoryData.getOWLDatatype(OWL2Datatype.XSD_INTEGER.getIRI());
 		} else {
 			return;
 		}
 
 		ni = factoryData.getOWLNamedIndividual(individualName[0], pm);
 		oli = factoryData.getOWLLiteral(individualName[1], odt);
 		odp = factoryData.getOWLDataProperty(IRI.create(relationName));
 		a = factoryData.getOWLDataPropertyAssertionAxiom(odp, ni, oli, annot);
 
 		//So pode ter um axioma
 		for (OWLDataPropertyAssertionAxiom odpa : ontology.getDataPropertyAssertionAxioms(ni)) {
			if (odpa.getProperty() == odp && odpa.getSubject() == ni && odpa.getObject() == oli) {
 				if (isAdd) {
 					manager.removeAxiom(ontology, odpa);
 				} else {
 					a = odpa;
 				}
 			}
 		}
 
 		afterChange(a, isAdd);
 	}
 
 	protected void changeObjectAssertion(String[] individualName, String relationName,
 			Set<OWLAnnotation> annot, boolean isAdd) {
 		OWLObjectProperty oop;
 		OWLNamedIndividual niTarget;
 		OWLNamedIndividual ni;
 		OWLAxiom a;
 
 		String relationValid = relationsType.get(relationName);
 		if (relationValid == null) {
 			return;
 		}
 
 		ni = factoryData.getOWLNamedIndividual(individualName[0], pm);
 		niTarget = factoryData.getOWLNamedIndividual(individualName[1], pm);
 		oop = factoryData.getOWLObjectProperty(IRI.create(relationName));
 		a = factoryData.getOWLObjectPropertyAssertionAxiom(oop, ni, niTarget, annot);
 
 		//So pode ter um axioma
 		for (OWLObjectPropertyAssertionAxiom oopa : ontology.getObjectPropertyAssertionAxioms(ni)) {
			//System.out.println(oopa.getProperty()+" "+oopa.getSubject()+" "+oopa.getObject());
			if (oopa.getProperty() == oop && oopa.getSubject() == ni && oopa.getObject() == niTarget) {
 				if (isAdd) {
 					manager.removeAxiom(ontology, oopa);
 				} else {
 					a = oopa;
 				}
 			}
 		}
 
 		afterChange(a, isAdd);
 	}
 
 	protected Set<OWLNamedIndividual> setAsOWLNamedIndividual(String[] individualName) {
 		Set<OWLNamedIndividual> ni = new HashSet<OWLNamedIndividual>();
 
 		for (String name : individualName) {
 			ni.add(factoryData.getOWLNamedIndividual(name, pm));
 		}
 
 		return ni;
 	}
 
 	protected void changeSpecialAssertion(String[] individualName, String relationName,
 			Set<OWLAnnotation> annot, boolean isAdd) {
 		OWLAxiom a;
 
 		if (relationName.equals(sameas)) {
 			a = factoryData.getOWLSameIndividualAxiom(setAsOWLNamedIndividual(individualName), annot);
 		} else {
 			//if (relationName.equals("~"+sameas))
 			a = factoryData.getOWLDifferentIndividualsAxiom(setAsOWLNamedIndividual(individualName), annot);
 		}
 
 		if (isAdd == false) {
 			String i = null;
 			i.equals("not implemented");
 		}
 
 		afterChange(a, isAdd);
 	}
 
 	public Iterator<Dumper> getCandidatesByFunctorAndArityIter(int arity, String functor) {
         Set<Dumper> hsd = getCandidatesByFunctorAndArity(arity, functor);
         if (hsd == null) return null;
         return hsd.iterator();
     }
 
 	public Set<Dumper> getCandidatesByFunctorAndArity(int arity, String functor) {
 		NodeSet<OWLNamedIndividual> noni;
 		Set<Dumper> d;
 		String fullName;
 		String type; // domain -> target
 
 		if (ontology == null) {
 			return null;
 		}
 
 		fullName = getCorrectName(arity, functor);
 		if (fullName == null) {
 			return null;
 		}
 		type = relationsType.get(fullName);
 		if (type == null) {
 			return null;
 		}
 		reloadReasoner();
 		noni = getAllInstances();
 		if (noni == null) {
 			return null;
 		}
 
 		//System.err.println("OWLapi " + functor + " passed by all tests: "+ type);
 		d = new HashSet<Dumper>();
 		switch (type.charAt(0)) {
 			case 'C': // oncept
 				filterByClass(d, fullName, noni);
 				return d;
 			case 'O': // bject relation
 				filterByObjectRelation(d, fullName, noni);
 				return d;
 			case 'D': // ata relation
 				filterByDataRelation(d, fullName, noni);
 				return d;
 			case 'S':
 				filterBySpecialRelation(d, fullName, noni);
 				return d;
 			default:
 				break;
 		}
 
 		return null;
 	}
 
 	// TODO esse codigo nao foi analisado!!
 	protected Dumper[] joinAndPutUserData(Set<Dumper[]> dumper) {
 		LinkedList<Dumper> all = new LinkedList<Dumper>();
 
 		for (Dumper[] di : dumper) {
 			for (Dumper index : di) {
 				if (all.contains(index) == false) {
 					all.add(index);
 				}
 			}
 		}
 
 		return Dumper.addOntology(all.toArray(new Dumper[0]),
 				ontology.getOntologyID().getOntologyIRI().toString());
 	}
 
 	protected NodeSet<OWLNamedIndividual> getAllInstances() {
 		OWLClass cl;
 
 		cl = factoryData.getOWLClass("owl:Thing", pm);
 		return reasoner.getInstances(cl, false);
 	}
 
 	@Override
 	public Iterator<Dumper> iterator() {
 		Set<Dumper> dumper = new HashSet<Dumper>();
 
 		if (ontology == null) {
 			return dumper.iterator();
 		}
 		reloadReasoner();
 
 		for (OWLNamedIndividual ni : getAllInstances().getFlattened()) {
 			NodeSet<OWLClass> nsoc = reasoner.getTypes(ni, false);
 
 			filterBySpecialRelation(dumper, null, ni);
 			filterByClass(dumper, ni, null);
 
 			for (OWLDataProperty dp : ontology.getDataPropertiesInSignature()) {
 				Set<OWLLiteral> targets;
 				if (dp.isOWLTopDataProperty()) {
 					continue;
 				}
 
 				targets = reasoner.getDataPropertyValues(ni, dp);
 				filterByDataRelation(dumper, ni, dp, targets);
 			}
 
 			for (OWLObjectProperty op : ontology.getObjectPropertiesInSignature()) {
 				Set<OWLNamedIndividual> targets;
 				if (op.isOWLTopObjectProperty()) {
 					continue;
 				}
 				targets = reasoner.getObjectPropertyValues(ni, op).getFlattened();
 				filterByObjectRelation(dumper, ni, op, targets);
 			}
 		}
 
 		return dumper.iterator();
 	}
 
 	protected void filterByClass(Set<Dumper> dumper,
 			String fullName, NodeSet<OWLNamedIndividual> nodesIndividual) {
 		OWLClass cl = null;
 
 		if (fullName != null) {
 			cl = factoryData.getOWLClass(IRI.create(fullName));
 		}
 
 		for (OWLNamedIndividual ni : nodesIndividual.getFlattened()) {
 			filterByClass(dumper, ni, cl);
 		}
 	}
 
 	protected void filterByClass(Set<Dumper> dumper,
 			OWLNamedIndividual ni, OWLClass cl) {
 		for (OWLClass clse : reasoner.getTypes(ni, false).getFlattened()) {
 			if (clse.isOWLThing() == true) {
 				continue;
 			}
 
 			if (cl != null && cl != clse) {
 				continue;
 			}
 
 			Dumper d = new Dumper();
 			d.setFunctor(clse.getIRI().getFragment());
 			d.setNot("");
 			d.setTerms(new String[]{'"' + ni.getIRI().getFragment() + '"'});
 
 			Set<Dumper[]> a = new HashSet<Dumper[]>();
 			for (OWLClassAssertionAxiom ocaa : ontology.getClassAssertionAxioms(ni)) {
 				if (ocaa.getClassExpression() == (OWLClassExpression) clse) {
 					a.add(convertFromAnnotation(ocaa));
 				}
 			}
 			d.setAnnots(joinAndPutUserData(a));
 
 			dumper.add(d);
 		}
 	}
 
 	protected void filterByObjectRelation(Set<Dumper> dumper,
 			String fullName, NodeSet<OWLNamedIndividual> nodesIndividual) {
 		OWLObjectProperty oop = factoryData.getOWLObjectProperty(IRI.create(fullName));
 
 		if (oop.isOWLTopObjectProperty()) {
 			return;
 		}
 
 		for (OWLNamedIndividual ni : nodesIndividual.getFlattened()) {
 			NodeSet<OWLNamedIndividual> targets = reasoner.getObjectPropertyValues(ni, oop);
 			filterByObjectRelation(dumper, ni, oop, targets.getFlattened());
 		}
 	}
 
 	protected void filterByObjectRelation(Set<Dumper> dumper,
 			OWLNamedIndividual ni, OWLObjectProperty oop, Set<OWLNamedIndividual> targets) {
 		for (OWLNamedIndividual target : targets) {
 			Dumper d = new Dumper();
 			d.setFunctor(oop.getIRI().getFragment());
 			d.setNot("");
 			d.setTerms(new String[]{'"' + ni.getIRI().getFragment() + '"', '"' + target.getIRI().getFragment() + '"'});
 
 			Set<Dumper[]> a = new HashSet<Dumper[]>();
 			for (OWLObjectPropertyAssertionAxiom oopa :
 					ontology.getObjectPropertyAssertionAxioms(ni)) {
 				a.add(convertFromAnnotation(oopa));
 			}
 			d.setAnnots(joinAndPutUserData(a));
 
 			dumper.add(d);
 		}
 	}
 
 	protected void filterByDataRelation(Set<Dumper> dumper,
 			String fullName, NodeSet<OWLNamedIndividual> nodesIndividual) {
 		OWLDataProperty odp = factoryData.getOWLDataProperty(IRI.create(fullName));
 		if (odp.isOWLTopDataProperty()) {
 			return;
 		}
 
 		for (OWLNamedIndividual ni : nodesIndividual.getFlattened()) {
 			Set<OWLLiteral> targets = reasoner.getDataPropertyValues(ni, odp);
 			filterByDataRelation(dumper, ni, odp, targets);
 		}
 	}
 
 	protected void filterByDataRelation(Set<Dumper> dumper,
 			OWLNamedIndividual ni, OWLDataProperty odp, Set<OWLLiteral> targets) {
 		String relationValid = relationsType.get(odp.getIRI().toString());
 		boolean isNumber = false;
 		if (relationValid.equals("Di")) {
 			isNumber = true;
 		}
 
 		for (OWLLiteral target : targets) {
 			Dumper d = new Dumper();
 			d.setFunctor(odp.getIRI().getFragment());
 			d.setNot("");
 			if (isNumber) {
 				d.setTerms(new String[]{'"' + ni.getIRI().getFragment() + '"', target.getLiteral()});
 			} else {
 				d.setTerms(new String[]{'"' + ni.getIRI().getFragment() + '"', '"' + target.getLiteral() + '"'});
 			}
 
 			Set<Dumper[]> a = new HashSet<Dumper[]>();
 			for (OWLDataPropertyAssertionAxiom odpa :
 					ontology.getDataPropertyAssertionAxioms(ni)) {
 				a.add(convertFromAnnotation(odpa));
 			}
 			d.setAnnots(joinAndPutUserData(a));
 
 			dumper.add(d);
 		}
 	}
 
 	protected void filterBySpecialRelation(Set<Dumper> dumper,
 			String fullName, NodeSet<OWLNamedIndividual> nodesIndividual) {
 		for (OWLNamedIndividual ni : nodesIndividual.getFlattened()) {
 			filterBySpecialRelation(dumper, fullName, ni);
 		}
 	}
 
 	protected void filterBySpecialRelation(Set<Dumper> dumper,
 			String fullName, OWLNamedIndividual ni) {
 		if (fullName == null || fullName.equals(sameas)) {
 			for (OWLNamedIndividual eq : reasoner.getSameIndividuals(ni)) {
 				if (eq != ni) {
 					Dumper d = new Dumper();
 					d.setFunctor(sameas);
 					d.setNot("");
 					d.setTerms(new String[]{'"' + ni.getIRI().getFragment() + '"',
 							'"' + eq.getIRI().getFragment() + '"'});
 
 					Set<Dumper[]> a = new HashSet<Dumper[]>();
 					for (OWLSameIndividualAxiom osia : ontology.getSameIndividualAxioms(ni)) {
 						a.add(convertFromAnnotation(osia));
 					}
 					d.setAnnots(joinAndPutUserData(a));
 
 					dumper.add(d);
 				}
 			}
 		}
 
 		if (fullName == null || fullName.equals("~" + sameas)) {
 			NodeSet<OWLNamedIndividual> nsoni = reasoner.getDifferentIndividuals(ni);
 			for (OWLNamedIndividual di : nsoni.getFlattened()) {
 				Dumper d = new Dumper();
 				d.setFunctor(sameas); // final eh prefixado do not!
 				d.setNot("~");
 				d.setTerms(new String[]{'"' + ni.getIRI().getFragment() + '"',
 						'"' + di.getIRI().getFragment() + '"'});
 
 				Set<Dumper[]> a = new HashSet<Dumper[]>();
 				for (OWLDifferentIndividualsAxiom odia : ontology.getDifferentIndividualAxioms(ni)) {
 					a.add(convertFromAnnotation(odia));
 				}
 				d.setAnnots(joinAndPutUserData(a));
 
 				dumper.add(d);
 			}
 		}
 	}
 
 	public Integer summaryOf(String name, String emotionType, int step, HashMap<Integer, Integer> valencesByStep) {
 		reloadReasoner();
 		String emotion = getCorrectName(1, emotionType);
 		OWLClass ce = factoryData.getOWLClass(IRI.create(emotion));
 		String translatedA = getCorrectName(2, "isAppraisalOf");
 		OWLObjectProperty oo = factoryData.getOWLObjectProperty(IRI.create(translatedA));
 		OWLNamedIndividual agent = factoryData.getOWLNamedIndividual(name, pm);
 		OWLObjectHasValue ooHm = factoryData.getOWLObjectHasValue(oo, agent);
 		OWLClassExpression oce = factoryData.getOWLObjectIntersectionOf(ce, ooHm);
 		NodeSet<OWLNamedIndividual> noni;
 		Integer sum = null;
 
 		noni = reasoner.getInstances(oce, true); // direct only
 		for (OWLNamedIndividual oni : noni.getFlattened()) {
 			if (sum == null) {
 				sum = new Integer(0);
 			}
 			for (Set<OWLLiteral> sl : oni.getDataPropertyValues(ontology).values()) {
 				for (OWLLiteral ls : sl) {
 					if (ls.isInteger()) {
 						sum = summaryOf(name, emotionType, step, sum, ls.parseInteger(), valencesByStep);
 					}
 				}
 			}
 		}
 
 		return sum;
 	}
 
 	/**
 	  This method can be override.
 
 	  @param name,				the name of agent
 	  @param emotionType,			the name of the emotion
 	  @param step,				current value of the step (begin at zero)
 	  @param valueCurrent,		current value of the accumulator
 	  @param valueToAccumulate,	value of a data relation
 	  @param valencesByStep,		brute value of emotions by steps (step x intensity). Not decay!
 	 */
 	public Integer summaryOf(String name, String emotionType, int step,
 			Integer valueCurrent, Integer valueToAccumulate,
 			HashMap<Integer, Integer> valencesByStep) {
 		//Exemplo para pegar do outro turno e acumular.
 		//if (valueCurrent == 0 && step > 0) {
 		//	valueCurrent = valencesByStep.get(step-1);
 		//}
 		return valueCurrent + Math.abs(valueToAccumulate);
 	}
 
 	public Set<String> getAllEmotions() {
 		SortedSet<String> ret;
 		NodeSet<OWLClass> ns;
 		String emo = getCorrectName(1, "Emotion");
 
 		reloadReasoner();
 		ns = reasoner.getSubClasses(
 				factoryData.getOWLClass(IRI.create(emo)),
 				false);
 
 		ret = new TreeSet<String>();
 		for (OWLClass cls : ns.getFlattened()) {
 			if (cls.isDefined(manager.getOntologies())) {
 				ret.add(cls.getIRI().getFragment().toLowerCase());
 			}
 		}
 
 		return ret;
 	}
 
 	protected boolean abolish(int arity, String functor) {
 		NodeSet<OWLNamedIndividual> noni;
 		String fullName;
 		String type; // domain -> target
 		boolean ret;
 
 		fullName = getCorrectName(arity, functor);
 		if (fullName == null) {
 			return false;
 		}
 		type = relationsType.get(fullName);
 		if (type == null) {
 			return false;
 		}
 		noni = getAllInstances();
 		if (noni == null) {
 			return false;
 		}
 
 		ret = true;
 		switch (type.charAt(0)) {
 			case 'C': // oncept
 				abolishInstance(fullName, noni);
 				break;
 			case 'O': // bject relation
 				abolishObjectAssertion(fullName, noni);
 				break;
 			case 'D': // ata relation
 				abolishDataAssertion(fullName, noni);
 				break;
 			case 'S': // pecial or same/different relation
 				abolishSpecialAssertion(fullName, noni);
 				break;
 			default:
 				ret = false;
 				break;
 		}
 		return ret;
 	}
 
 	protected void abolishInstance(String relation, NodeSet<OWLNamedIndividual> individuals) {
 		OWLClass cl;
 
 		if (relation == null) {
 			return;
 		}
 		cl = factoryData.getOWLClass(IRI.create(relation));
 
 		for (OWLOntology onto : manager.getOntologies()) {
 			Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
 			for (OWLNamedIndividual ni : individuals.getFlattened()) {
 				for (OWLClass clse : reasoner.getTypes(ni, true).getFlattened()) {
 					if (clse.isOWLThing() == true) {
 						continue;
 					}
 					if (cl != null && cl != clse) {
 						continue;
 					}
 
 					Set<OWLClassAssertionAxiom> ocaas = onto.getClassAssertionAxioms(ni);
 					for (OWLClassAssertionAxiom ocaa : ocaas) {
 						axioms.add(ocaa);
 					}
 				}
 			}
 			if (axioms.size() > 0) {
 				manager.removeAxioms(onto, axioms);
 				dirty = true;
 			}
 		}
 	}
 
 	protected void abolishObjectAssertion(String relation, NodeSet<OWLNamedIndividual> individuals) {
 		OWLObjectProperty oop;
 		if (relation == null) {
 			return;
 		}
 
 		oop = factoryData.getOWLObjectProperty(IRI.create(relation));
 
 		for (OWLOntology onto : manager.getOntologies()) {
 			Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
 			for (OWLNamedIndividual ni : individuals.getFlattened()) {
 				for (OWLObjectPropertyAssertionAxiom oopa : onto.getObjectPropertyAssertionAxioms(ni)) {
 					if (oopa.getProperty() == oop) {
 						axioms.add(oopa);
 					}
 				}
 			}
 
 			if (axioms.size() > 0) {
 				manager.removeAxioms(onto, axioms);
 				dirty = true;
 			}
 		}
 	}
 
 	protected void abolishDataAssertion(String relation, NodeSet<OWLNamedIndividual> individuals) {
 		OWLDataProperty odp;
 		if (relation == null) {
 			return;
 		}
 
 		odp = factoryData.getOWLDataProperty(IRI.create(relation));
 
 		for (OWLOntology onto : manager.getOntologies()) {
 			Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
 			for (OWLNamedIndividual ni : individuals.getFlattened()) {
 				for (OWLDataPropertyAssertionAxiom odpa : onto.getDataPropertyAssertionAxioms(ni)) {
 					if (odpa.getProperty() == odp) {
 						axioms.add(odpa);
 					}
 				}
 			}
 
 			if (axioms.size() > 0) {
 				manager.removeAxioms(onto, axioms);
 				dirty = true;
 			}
 		}
 	}
 
 	protected void abolishSpecialAssertion(String relation, NodeSet<OWLNamedIndividual> individuals) {
 		System.out.println("lucca TODO");
 	}
 
 	public void dumpOntology(String filename)
 		throws org.semanticweb.owlapi.model.OWLOntologyStorageException {
 			if (filename == null || ontology == null) {
 				return;
 			}
 			reloadReasoner();
 			manager.saveOntology(ontology, IRI.create(new File(filename).toURI()));
 		}
 
 	public class Relation {
 
 		protected IRI name;
 		// 'I' ndividual
 		// 'i' nteger
 		// 'd' ouble
 		// 's' tring
 		// 'V' azio
 		protected char range;
 
 		Relation(IRI relation) {
 			this(relation, "myd:I");
 		}
 
 		Relation(IRI relation, String target) {
 			name = relation;
 			range = target.charAt(4); // 4 == i de int de xsd:int
 		}
 
 		public IRI relation() {
 			return name;
 		}
 
 		public char type() {
 			return range;
 		}
 
 		public String translate(char c) {
 			if (c == 'I') {
 				return "Individual";
 			} else if (c == 'i') {
 				return "integer";
 			} else if (c == 'd') {
 				return "double";
 			} else if (c == 's') {
 				return "string";
 			} else if (c == 'V') {
 				return "empty or not specified";
 			}
 			return "Incorrect type";
 		}
 
 		@Override
 		public String toString() {
 			return relation().getFragment() + " - " + translate(type());
 		}
 	}
 }
