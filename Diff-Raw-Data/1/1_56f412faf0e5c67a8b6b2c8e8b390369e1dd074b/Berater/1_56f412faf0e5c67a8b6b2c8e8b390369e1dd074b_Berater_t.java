 package de.htw.berater;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import com.hp.hpl.jena.ontology.OntClass;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.Restriction;
 import com.hp.hpl.jena.ontology.UnionClass;
 import com.hp.hpl.jena.rdf.model.Literal;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFList;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.ResourceFactory;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.util.iterator.ExtendedIterator;
 import com.hp.hpl.jena.vocabulary.OWL;
 import com.hp.hpl.jena.vocabulary.RDF;
 import com.hp.hpl.jena.vocabulary.RDFS;
 import com.hp.hpl.jena.vocabulary.XSD;
 
 import de.htw.berater.controller.Answer;
 import de.htw.berater.controller.Choice;
 import de.htw.berater.controller.ChoiceType;
 import de.htw.berater.controller.ChoicesBuilder;
 import de.htw.berater.controller.Controller;
 import de.htw.berater.controller.Question;
 import de.htw.berater.db.DBException;
 import de.htw.berater.db.SQLClient;
 
 public class Berater {
 
 	public static final int BERATER_1 = 0;
 	public static final int BERATER_2 = 1;
 
 	protected List<OntClass> rememberList = new LinkedList<OntClass>();
 	protected Question nextQuestion;
 	private Question oldQuestion;
 	protected String ns;
 	private String rdfPath;
 	private Set<OntClass> properties = new LinkedHashSet<OntClass>();
 	private Set<OntClass> oldproperties = new LinkedHashSet<OntClass>();
 	protected int context; // irgendwie den kontext beachten um sinnvoll die
 							// naechste frage zu stellen
 	private int oldContext;
 	protected Customer customer = new Customer();
 	private Customer oldCustmer = new Customer();
 	protected OntModel model;
 	private String brand = "";
 	private String oldBrand = "";
 
 	private Controller controller;
 	
 	public Berater(String rdfPath, String ns, boolean loadOntology) {
 		this.ns = ns;
 		this.rdfPath = rdfPath;
 		this.brand = "";
 
 		if (loadOntology) {
 			if (System.getProperty("log4j.configuration") == null) {
 				System.setProperty("log4j.configuration", "jena-log4j.properties");
 			}
 	
 			model = ModelFactory.createOntologyModel();
 	
 			model.read("file:" + rdfPath);
 		}
 	}
 	
 	public void goBack() {
 		context = oldContext;
 		properties.clear();
 		properties.addAll(oldproperties);
 		nextQuestion = oldQuestion;
 		brand = oldBrand;
 		customer = oldCustmer;
 	}
 	
 	
 	public void setController(Controller controller) {
 		this.controller = controller;
 	}
 	
 	public void addCustomerInfo(int info) {
 		customer.addCustomerInfo(info);
 	}
 	
 	
 	public void evaluateAnswer(Answer answer) throws Exception {
 		oldContext = context;
		oldproperties.clear();
 		oldproperties.addAll(properties);
 		oldQuestion = nextQuestion;
 		oldBrand = brand;
 		oldCustmer = customer;
 		switch (context) {
 		case 0:
 			Berater berater;
 			if (answer.getSingleValue().equals("DAU")) {
 				berater = StaticFactory.getNewBerater1(rdfPath, ns);
 			} else if (answer.getSingleValue().equals("VollPro")) {
 				berater = StaticFactory.getNewBerater2(rdfPath, ns);
 			} else {
 				throw new IllegalStateException("Im Szenario nicht vorgesehen");
 			}
 			controller.setBerater(berater);
 			berater.setController(controller);
 			berater.nextQuestion = berater.firstSpecificQuestion();
 			break;
 		}
 	}
 	
 	
 	protected Question firstSpecificQuestion() {
 		//Override this
 		throw new UnsupportedOperationException();
 	}
 
 	public Question firstQuestion() {
 		//context = 0;
 		HashMap<Integer, List<Choice>> choices = new ChoicesBuilder()
 //				.add("Hallo! Ich will ein iPhone! Darf aber nicht mehr als 100 € kosten!",
 //						"Loser", ChoiceType.RADIO)
 				.add("Ich möchte ein Smartphone und habe keine Ahnung!",
 						"DAU", ChoiceType.RADIO)
 				.add("Ich benötige ein neues Smartphone, da mir mein altes nicht mehr genügt.",
 						"VollPro", ChoiceType.RADIO)
 				.build();
 		return new Question("Guten Tag! Wie kann ich Ihnen helfen?", choices);
 	}
 	
 	public final Question generateQuestion() {
 		switch (context) {
 		case 0:
 			return firstQuestion();
 		default:
 			return nextQuestion;
 		}
 	}
 
 	public final Set<OntClass> getProperties() {
 		return properties;
 	}
 
 	protected final List<Restriction> getRestrictionsDeep(OntClass ontClass) {
 		List<Restriction> restrictions = new LinkedList<Restriction>();
 		for (Iterator<OntClass> supers = ontClass.listSuperClasses(true); supers
 				.hasNext();) {
 			OntClass superClass = supers.next();
 			restrictions.addAll(getRestrictionsRecursively(superClass));
 		}
 		return restrictions;
 	}
 	
 	protected boolean isSmartphoneOkForCustumer(OntClass displaySmartphone, int sehbehindert) throws DBException {
 		List<OntClass> restrictions = getClassProperties(displaySmartphone);
 		for (OntClass restriction : restrictions) {
 			if (customer.isCustomer(sehbehindert)) {
 				if (!testSmartphoneOkForCustomerRecursively(restriction, true, "Sehbehindert")) {
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 	
 	protected boolean testSmartphoneOkForCustomerRecursively(OntClass clazz, boolean isOk, String what) throws DBException {
 		if (clazz.isRestriction()) {
 			Restriction restriction = clazz.asRestriction();
 			ReadableProperty constraint = getReadablePropertyFromRestriction(restriction);
 			if (constraint.getKey().equals("fuerKunde")) {
 				if (!constraint.getValue().contains(what)) {
 					isOk = true;
 				}
 			}
 		} else if (clazz.isIntersectionClass()) { 
 			for (Iterator<? extends OntClass> it = clazz.asIntersectionClass()
 					.listOperands(); it.hasNext();) {
 				OntClass op = it.next();
 				isOk = testSmartphoneOkForCustomerRecursively(op, isOk, what);
 				if (!isOk) {
 					break;
 				}
 			}
 		} else if (clazz.isComplementClass()) {
 			for (Iterator<? extends OntClass> it = clazz.asComplementClass()
 					.listOperands(); it.hasNext();) {
 				OntClass op = it.next();
 				isOk = testSmartphoneOkForCustomerRecursively(op, !isOk, what);
 			}
 		}
 		return isOk;
 	}
 	
 	protected final List<OntClass> getClassProperties(OntClass ontClass) {
 		List<OntClass> restrictions = new LinkedList<OntClass>();
 		for (Iterator<OntClass> supers = ontClass.listSuperClasses(true); supers
 				.hasNext();) {
 			OntClass superClass = supers.next();
 			if (superClass.isAnon())
 				restrictions.add(superClass);
 		}
 		return restrictions;
 	}
 	
 	private List<Restriction> getRestrictionsRecursively(OntClass clazz) {
 		List<Restriction> restrictions = new LinkedList<Restriction>();
 		if (clazz.isRestriction()) {
 			Restriction restriction = clazz.asRestriction();
 			restrictions.add(restriction);
 		} else if (clazz.isIntersectionClass()) { 
 			for (Iterator<? extends OntClass> it = clazz.asIntersectionClass()
 					.listOperands(); it.hasNext();) {
 				OntClass op = it.next();
 				restrictions.addAll(getRestrictionsRecursively(op));
 			}
 		} else if (clazz.isComplementClass()) {
 			for (Iterator<? extends OntClass> it = clazz.asComplementClass()
 					.listOperands(); it.hasNext();) {
 				OntClass op = it.next();
 				restrictions.addAll(getRestrictionsRecursively(op));
 			}
 		} else if (clazz.isUnionClass()) {
 			for (Iterator<? extends OntClass> it = clazz.asUnionClass()
 					.listOperands(); it.hasNext();) {
 				OntClass op = it.next();
 				restrictions.addAll(getRestrictionsRecursively(op));
 			}
 		}
 		return restrictions;
 	}
 
 	/* is covering axiom(abstract class)? */
 	protected boolean isCoveringAxiom(OntClass ontClass) {
 		boolean isCoveringAxiom = false;
 		for (Iterator<OntClass> supers = ontClass.listSuperClasses(true); supers.hasNext();) {
 			OntClass superClass = supers.next();
 			if (superClass.isUnionClass()) {
 				isCoveringAxiom = true;
 				for (Iterator<? extends OntClass> it = superClass.asUnionClass().listOperands(); it.hasNext();) {
 					OntClass op = it.next();
 
 					if (!op.isRestriction()) {
 						boolean found = false;
 						Iterator<OntClass> it2 = ontClass.listSubClasses();
 						while (it2.hasNext()) {
 							if (it2.next().equals(op)) {
 								found = true;
 							}
 						}
 						if (!found) {
 							isCoveringAxiom = false;
 						}
 					} else {
 						return false;
 					}
 				}
 				if (isCoveringAxiom) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/* real super classes, no properties */
 	protected final List<OntClass> getSuperclasses(OntClass ontClass) {
 		List<OntClass> superClasses = new LinkedList<OntClass>();
 		for (Iterator<OntClass> supers = ontClass.listSuperClasses(true); supers
 				.hasNext();) {
 			OntClass superClass = supers.next();
 			if (!superClass.isAnon() && !superClass.isUnionClass()) {
 				superClasses.add(superClass);
 			}
 		}
 		return superClasses;
 	}
 
 	/* get properties of a certain class */
 	protected final List<OntClass> getProperties(OntClass ontClass) {
 		List<OntClass> tmpProperties = new LinkedList<OntClass>();
 		for (Iterator<OntClass> supers = ontClass.listSuperClasses(true); supers
 				.hasNext();) {
 			OntClass superClass = supers.next();
 			if (superClass.isRestriction() || superClass.isUnionClass()
 					|| superClass.isIntersectionClass()
 					|| superClass.isComplementClass()
 					&& !superClass.hasSubClass()) { //nur blatt-eigenschaften
 				tmpProperties.add(superClass);
 			}
 		}
 		return tmpProperties;
 	}
 
 	protected List<OntClass> getDisjointSmartphones(OntClass ontClass) {
 		OntClass smartphone = searchClassContaining("Smartphone", "Smartphone");
 		List<OntClass> list = new LinkedList<OntClass>();
 		for (Iterator<OntClass> it = smartphone.listSubClasses(); it.hasNext();) {
 			OntClass subClass = it.next();
 			if (subClass.isDisjointWith(ontClass)) {
 				list.add(subClass);
 			}
 		}
 		return list;
 	}
 	
 	/* add all properties of a certain class */
 	protected final List<OntClass> setCurrentProperties(OntClass ontClass) {
 		if (ontClass.getLocalName().equals("Smartphone")) {
 			return null;
 		} else {
 			List<OntClass> superClasses = getSuperclasses(ontClass);
 			for (OntClass superClass : superClasses) {
 				setCurrentProperties(superClass);
 			}
 		}
 		List<OntClass> tmpProperties = getProperties(ontClass);
 		properties.addAll(tmpProperties);
 		return tmpProperties;
 	}
 
 	/* remove all properties of a certain class */
 	protected final void removeSQLConstraints(OntClass ontClass) {
 		List<OntClass> tmpProperties = getProperties(ontClass);
 		properties.removeAll(tmpProperties);
 	}
 
 	public String getSQLString() throws DBException {
 		String s = "select * from Smartphones where ";
 		for (OntClass property : properties) {
 			String expression = processPropertyToSQL(property);
 			if (!expression.equals("1") && !expression.equals("()")) {
 				s += "(" + expression + ")" + " and ";
 			}
 		}
 		if (!brand.equals("")) {
 			s += "Marke like '%"  + brand + "%' and ";
 		}
 		if (s.equals("select * from Smartphones where ")) {
 			return "select * from Smartphones";
 		}
 		return s.substring(0, s.length() - 5); //wegen " and "
 	}
 	
 	private String extractActualIdentifier(String propertyName) {
 		if (!propertyName.matches("^[A-Z].*")) throw new RuntimeException("Die Properties müssen immer mit einem grossbuchstaben beginnen");
 		boolean lowerCaseFound = false;
 		for (int i = 0; i < propertyName.length(); i++) {
 			if (Character.isLowerCase(propertyName.charAt(i))) {
 				lowerCaseFound = true;
 			}
 			if (lowerCaseFound) {
 				if (Character.isUpperCase(propertyName.charAt(i))) {
 					return propertyName.substring(0, i);
 				}
 			}
 		}
 		return propertyName;
 	}
 	
 	private String extractGeneralIdentifier(String propertyName) {
 		boolean lowerCaseFound = false;
 		for (int i = 0; i < propertyName.length(); i++) {
 			if (Character.isLowerCase(propertyName.charAt(i))) {
 				lowerCaseFound = true;
 			}
 			if (lowerCaseFound) {
 				if (Character.isUpperCase(propertyName.charAt(i))) {
 					return propertyName.substring(i);
 				}
 			}
 		}
 		return propertyName;
 	}
 
 	private String processPropertyToSQL(OntClass property) throws DBException {
 		if (property.isRestriction()) {
 			ReadableProperty constraint = getReadablePropertyFromRestriction(property
 					.asRestriction());
 			if (constraint.getKey().equals("hatEigenschaft")) {
 				if (constraint.isBooleanValue()) {
 					return constraint.getValue() + " = 1 ";
 				} else {
 					return constraint.getValue();
 				}
 			} else {
 				if (constraint.getKey().equals("hatZweck")) { //braucht man nicht
 					return "1";
 				} else if (constraint.getKey().equals("fuerKunde")) { //braucht man nicht
 					return "1";
 				} else {
 					return constraint.getKey() + " " + constraint.getValue();
 				}
 			}
 		} else {
 			if (property.isIntersectionClass()) {
 				String s = "";
 				for (Iterator<? extends OntClass> it = property.asIntersectionClass()
 						.listOperands(); it.hasNext();) {
 					OntClass op = it.next();
 					String expression = processPropertyToSQL(op);
 					if (!expression.equals("1") && !expression.equals("()")) {
 						s += expression + " and ";
 					}
 				}
 				if (s.length() > 0)
 					s = s.substring(0, s.length() - 5);
 				return "(" + s + ")";
 			} else {
 				if (property.isUnionClass()) {
 					String s = "";
 					for (Iterator<? extends OntClass> it = property.asUnionClass()
 							.listOperands(); it.hasNext();) {
 						OntClass op = it.next();
 						String expression = processPropertyToSQL(op);
 						if (!expression.equals("1") && !expression.equals("()")) {
 							s += expression + " or ";
 						}
 					}
 					if (s.length() > 0)
 						s = s.substring(0, s.length() - 4);
 					return "(" + s + ")";
 				} else {
 					if (property.isComplementClass()) {
 						String s = "";
 						for (Iterator<? extends OntClass> it = property.asComplementClass()
 								.listOperands(); it.hasNext();) {
 							OntClass op = it.next();
 							String expression = processPropertyToSQL(op);
 							if (!expression.equals("1") && !expression.equals("()")) {
 								s = "not(" + expression + ")";
 							}
 							
 						}
 						return "(" + s + ")";
 					}
 				}
 			}
 		}
 		return "1";
 	}
 
 	
 	protected List<OntClass> getClassesWithProperty(String property) {
 		OntClass smartphone = model.getOntClass(ns + "Smartphone");
 		ExtendedIterator<OntClass> ri = smartphone.listSubClasses();
 		List<OntClass> smartphones = new ArrayList<OntClass>();
 		while (ri.hasNext()) {
 			OntClass subClass = ri.next();
 			List<Restriction> restrictions = getRestrictionsDeep(subClass);
 			for (Restriction restriction : restrictions) {
 				if (restriction.getOnProperty().getLocalName().contains(property)) {
 					if (restriction.isSomeValuesFromRestriction()) {
 						if (!smartphones.contains(subClass)) {
 							smartphones.add(subClass);
 						}
 					} else {
 						throw new RuntimeException("not defined in ontology");
 					}
 				}
 			}
 		}
 		return smartphones;
 	}
 	
 	protected void setCustomerInfo() throws DBException {
 		for (OntClass property : properties) {
 			if (property.isRestriction()) {
 				ReadableProperty sqlConstraint = getReadablePropertyFromRestriction(property
 						.asRestriction());
 				if (sqlConstraint.getKey().equals("fuerKunde")) {
 					if (sqlConstraint.getValue().toLowerCase()
 							.contains("sudokuhengst")) {
 						this.customer.addCustomerInfo(Customer.SUDOKUHENGST);
 					} else if (sqlConstraint.getValue().toLowerCase()
 							.contains("spielefreak")) { 
 						this.customer.addCustomerInfo(Customer.SPIELEFREAK);
 					}
 				}
 			}
 		}
 	}
 
 	protected ReadableProperty getReadablePropertyFromRestriction(
 			Restriction restriction) throws DBException {
 		Resource res = null;
 		if (restriction.isSomeValuesFromRestriction()) {
 			res = restriction.asSomeValuesFromRestriction().getSomeValuesFrom();
 		} else {
 			throw new RuntimeException("nur \"some\" properties sind vorgesehen, Ontologie neu?");
 		}
 
 		ReadableProperty sqlConstraint = new ReadableProperty();
 		sqlConstraint.setKey(restriction.getOnProperty().getLocalName());
 		if (res.hasProperty(RDF.type, RDFS.Datatype)) {
 			Property owlWithRestrictions = ResourceFactory.createProperty(
 					OWL.getURI(), "withRestrictions");
 			Property minInclusive = ResourceFactory.createProperty(
 					XSD.getURI(), "minInclusive"); // >= x
 			Property minExclusive = ResourceFactory.createProperty(
 					XSD.getURI(), "minExclusive"); // > x
 			Property maxInclusive = ResourceFactory.createProperty(
 					XSD.getURI(), "maxInclusive"); // <= x
 			Property maxExclusive = ResourceFactory.createProperty(
 					XSD.getURI(), "maxExclusive"); // < x
 
 			// the datatype restrictions are represented as a list
 			// we make some assumptions about the content of the list; this code
 			// could be more defensive about testing for expected values
 			Resource wr = res.getProperty(owlWithRestrictions).getResource();
 			RDFList wrl = wr.as(RDFList.class);
 
 			for (Iterator<RDFNode> k = wrl.iterator(); k.hasNext();) {
 				Resource wrClause = (Resource) k.next();
 				Statement stmt = wrClause.getProperty(minInclusive);
 				String str = ">=";
 				if (stmt == null) {
 					stmt = wrClause.getProperty(minExclusive);
 					str = ">";
 				}
 				if (stmt == null) {
 					stmt = wrClause.getProperty(maxExclusive);
 					str = "<";
 				}
 				if (stmt == null) {
 					stmt = wrClause.getProperty(maxInclusive);
 					str = "<=";
 				}
 				Literal literal = stmt.getLiteral();
 				sqlConstraint.setValue(str + literal.getInt(), true);
 			}
 		} else {
 			if (res.canAs(UnionClass.class)) {
 				String unionStr = "";
 				UnionClass union = res.as(UnionClass.class);
 				boolean isBooleanValue = false;
 				for (Iterator<? extends OntClass> it = union.listOperands(); it.hasNext();) {
 					OntClass op = it.next();
 					isBooleanValue = SQLClient.getInstance().doesColumnExist(op.getLocalName());
 					if (isBooleanValue)
 						unionStr += op.getLocalName() + " = 1 " + (it.hasNext() ? " or " : "");
 					else
 						unionStr += extractGeneralIdentifier(op.getLocalName()) + " like '" + extractActualIdentifier(op.getLocalName()) + "%' " + (it.hasNext() ? " or " : "");
 				}
 				sqlConstraint.setValue(unionStr, isBooleanValue);
 			} else {
 				boolean isBooleanValue = SQLClient.getInstance().doesColumnExist(res.getLocalName());
 				String s = res.getLocalName();
 				if (!isBooleanValue) {
 					String general = extractGeneralIdentifier(s);
 					
 					s = general + " like '" + extractActualIdentifier(s) + "%'";
 				} 
 				sqlConstraint.setValue(s, isBooleanValue);
 			}
 		}
 		return sqlConstraint;
 	}
 
 	protected List<List<OntClass>> getCoveringAxiomClasses(List<OntClass> classes) {
 		List<List<OntClass>> result = new LinkedList<List<OntClass>>();
 		List<OntClass> concrereClasses = new LinkedList<OntClass>();
 		for (OntClass clazz : classes) {
 			if (isCoveringAxiom(clazz)) {
 				ExtendedIterator<OntClass> ri = clazz.listSubClasses();
 				List<OntClass> subClasses = new LinkedList<OntClass>();
 				while (ri.hasNext()) {
 					OntClass subClass = ri.next();
 					subClasses.add(subClass);
 				}
 				result.addAll(getCoveringAxiomClasses(subClasses));
 			} else {
 				concrereClasses.add(clazz);
 			}
 		}
 		result.add(concrereClasses);
 		return result;
 	}
 
 	protected OntClass searchClassContaining(String keyword, String type) {
 		keyword = keyword.toLowerCase();
 		OntClass smartphone = model.getOntClass(ns + type);
 		for (Iterator<OntClass> it = smartphone.listSubClasses(); it.hasNext();) {
 			OntClass subClass = it.next();
 			if (subClass.getLocalName().toLowerCase().contains(keyword)) {
 				return subClass;
 			}
 		}
 		return null;
 	}
 
 	public void setBrand(String brand) {
 		this.brand = brand;
 	}
 
 }
