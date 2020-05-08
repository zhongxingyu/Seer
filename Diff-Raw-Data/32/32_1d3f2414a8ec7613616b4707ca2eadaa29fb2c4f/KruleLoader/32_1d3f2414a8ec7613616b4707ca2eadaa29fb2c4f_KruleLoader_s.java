 /*
  * The contents of this file are subject to the Mozilla Public License
  * Version 1.1 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
  * the License for the specific language governing rights and limitations
  * under the License.
  *
  * The Original Code is the Kowari Metadata Store.
  *
  * The Initial Developer of the Original Code is Plugged In Software Pty
  * Ltd (http://www.pisoftware.com, mailto:info@pisoftware.com). Portions
  * created by Plugged In Software Pty Ltd are Copyright (C) 2001,2002
  * Plugged In Software Pty Ltd. All Rights Reserved.
  *
  * Contributor(s): N/A.
  *
  * [NOTE: The text of this Exhibit A may differ slightly from the text
  * of the notices in the Source Code files of the Original Code. You
  * should use the text of this Exhibit A rather than the text found in the
  * Original Code Source Code for Your Modifications.]
  *
  */
 
 package org.mulgara.krule;
 
 // Java 2 standard packages
 import java.net.*;
 import java.util.*;
 import java.rmi.RemoteException;
 
 // Third party packages
 import org.apache.log4j.Logger;
 import org.jrdf.graph.*;
 
 // Locally written packages
 import org.mulgara.itql.*;
 import org.mulgara.query.*;
 import org.mulgara.query.rdf.LiteralImpl;
 import org.mulgara.query.rdf.TripleImpl;
 import org.mulgara.query.rdf.URIReferenceImpl;
 import org.mulgara.rules.*;
 import org.mulgara.server.*;
 
 /**
  * This object is used for parsing an RDF graph and building a rules structure
  * from it, according to the krule.owl ontology.
  *
  * @created 2005-5-17
  * @author <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
  * @version $Revision: 1.3 $
  * @modified $Date: 2005/07/03 12:57:44 $
  * @maintenanceAuthor $Author: pgearon $
  * @copyright &copy; 2005 <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
  * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
  */
 public class KruleLoader implements RuleLoader {
 
   /** Logger.  */
   private static Logger logger = Logger.getLogger(KruleLoader.class.getName());
 
   /** The system model. */
   private URI systemModel;
 
   /** The database session for querying. */
   private Session session;
 
   /** The interpreter for parsing queries. */
   private TqlInterpreter interpreter;
 
   /** The rules. */
   private RuleStructure rules;
 
   /** The URI of the model containing the rule data. */
   private URI ruleModel;
 
   /** The URI of the model containing the base data. */
   private URI baseModel;
 
   /** The URI of the model to receive the entailed data. */
   private URI destModel;
 
   /** The string of the prefix model URI. */
   private String prefixModel;
 
   /** A map of namespace names to the URIs. */
   private Map<String,URI> aliases;
 
   /** Map of krule:URIReference nodes to their associated URIs. */
   private Map<URIReference,URIReference> uriReferences;
 
   /** Map of krule:Variable nodes to their associated nodes. */
   private Map<URIReference,Variable> varReferences;
 
   /** Map of krule:Literal nodes to their associated strings. */
   private Map<Node,Literal> literalReferences;
 
   /** Map of Constraint nodes to the associated constraint object. */
   private Map<Node,ConstraintExpression> constraintMap;
 
   /** URI for the Krule namespace. */
   private static final String KRULE = "http://mulgara.org/owl/krule/#";
 
   /** URI for a constraint subject. */
   private static final String HAS_SUBJECT_STR = KRULE + "hasSubject";
 
   /** URI for a constraint predicate. */
   private static final String HAS_PREDICATE_STR = KRULE + "hasPredicate";
 
   /** URI for a constraint object. */
   private static final String HAS_OBJECT_STR = KRULE + "hasObject";
 
   /** URI for a constraint model. */
   private static final String HAS_MODEL_STR = KRULE + "hasModel";
 
   /** URI for the constraint conjunction type. */
   private static final String CONSTRAINT_CONJUNCTION_STR = KRULE + "ConstraintConjunction";
 
   /** URI for the constraint disjunction type. */
   private static final String CONSTRAINT_DISJUNCTION_STR = KRULE + "ConstraintDisjunction";
 
   /** URI for the transitive constraint argument. */
   private static final String TRANSITIVE_ARGUMENT_STR = KRULE + "transitiveArgument";
 
   /** URI for the transitive constraint anchor argument. */
   private static final String ANCHOR_ARGUMENT_STR = KRULE + "anchorArgument";
 
   /** URI for the Value type. */
   private static final String URI_REF_STR = KRULE + "URIReference";
 
   /** URI for the Variable type. */
   private static final String VARIABLE_STR = KRULE + "Variable";
 
   /** URI for the Variable type. */
   private static final String LITERAL_STR = KRULE + "Literal";
 
   /** RDF reference for constraint subject. */
   private static URIReference HAS_SUBJECT;
 
   /** RDF reference for constraint predicate. */
   private static URIReference HAS_PREDICATE;
 
   /** RDF reference for constraint object. */
   private static URIReference HAS_OBJECT;
 
   /** RDF reference for constraint model. */
   private static URIReference HAS_MODEL;
 
   /** RDF reference for constraint conjunction class. */
   private static URIReference CONSTRAINT_CONJUNCTION;
 
   /** RDF reference for constraint disjunction class. */
   private static URIReference CONSTRAINT_DISJUNCTION;
 
   /** RDF reference for the transitive constraint argument. */
   private static URIReference TRANSITIVE_ARGUMENT;
 
   /** RDF reference for the transitive constraint anchor argument. */
   private static URIReference ANCHOR_ARGUMENT;
 
   /** RDF reference for the Value type. */
   static URIReference URI_REF;
 
   /** RDF reference for the Variable type. */
   static URIReference VARIABLE;
 
   /** RDF reference for the Literal type. */
   static URIReference LITERAL;
 
 
   // initialise the URIs
   static {
     try {
       HAS_SUBJECT = new URIReferenceImpl(new URI(HAS_SUBJECT_STR));
       HAS_PREDICATE = new URIReferenceImpl(new URI(HAS_PREDICATE_STR));
       HAS_OBJECT = new URIReferenceImpl(new URI(HAS_OBJECT_STR));
       HAS_MODEL = new URIReferenceImpl(new URI(HAS_MODEL_STR));
       CONSTRAINT_CONJUNCTION = new URIReferenceImpl(new URI(CONSTRAINT_CONJUNCTION_STR));
       CONSTRAINT_DISJUNCTION = new URIReferenceImpl(new URI(CONSTRAINT_DISJUNCTION_STR));
       TRANSITIVE_ARGUMENT = new URIReferenceImpl(new URI(TRANSITIVE_ARGUMENT_STR));
       ANCHOR_ARGUMENT = new URIReferenceImpl(new URI(ANCHOR_ARGUMENT_STR));
       URI_REF = new URIReferenceImpl(new URI(URI_REF_STR));
       VARIABLE = new URIReferenceImpl(new URI(VARIABLE_STR));
       LITERAL = new URIReferenceImpl(new URI(LITERAL_STR));
     } catch (URISyntaxException u) {
       logger.error("Unable to build URI: " + u);
     }
   }
 
 
   /**
    * Principle constructor.
    *
    * @param ruleModel The name of the model with the rules to run.
    * @param baseModel The name of the model with the base data.
    * @param destModel The name of the model which will receive the entailed data.
    */
   KruleLoader(URI ruleModel, URI baseModel, URI destModel) {
     this.ruleModel = ruleModel;
     this.baseModel = baseModel;
     this.destModel = destModel;
 
     // set the query objects to null
     session = null;
     interpreter = null;
 
     // initialize the aliases
     newAliases();
 
     // initialize the constriant map
     constraintMap = new HashMap<Node,ConstraintExpression>();
   }
 
 
   /**
    * Factory method.
    *
    * @param ruleModel The name of the model with the rules to run.
    * @param baseModel The name of the model with the base data.
    * @param destModel The name of the model which will receive the entailed data.
    * @return A new KruleLoader instance.
    */
   public static RuleLoader newInstance(URI ruleModel, URI baseModel, URI destModel) {
     return new KruleLoader(ruleModel, baseModel, destModel);
   }
 
 
   /**
    * Reads the ruleModel in the database and constructs the rules from it.
    *
    * @param sessionParam The session for querying on.
    * @param systemModel The system model.
    * @return A new rule structure.
    * @throws InitializerException There was a problem reading and creating the rules.
    */
   public Rules readRules(Object sessionParam, URI systemModel) throws InitializerException, RemoteException {
     this.session = (Session)sessionParam;
     this.systemModel = systemModel;
 
     // get a new interpreter
     interpreter = new TqlInterpreter(aliases);
 
     rules = null;
     try {
       session.setAutoCommit(false);
       logger.debug("Initializing for rule queries.");
       // initialise the utility models
       initializeUtilityModels();
 
       // load the objects
       loadRdfObjects();
 
       logger.debug("Querying for rules");
       rules = findRules();
       // set the base and target models
       rules.setBaseModel(baseModel);
       rules.setTargetModel(destModel);
 
       // find the triggers
       loadTriggers();
 
       // find the queries for each rule
       loadQueries();
 
       // load the axioms
       rules.setAxioms(findAxioms());
 
       if (rules.getRuleCount() == 0 && rules.getAxiomCount() == 0) {
         throw new InitializerException("No valid rules found");
       }
 
     } catch (TuplesException te) {
       logger.error("Exception while accessing rule data.", te);
       throw new InitializerException("Problem accessing rule data", te);
     } catch (QueryException qe) {
       logger.error("Exception while reading rules.", qe);
       throw new InitializerException("Problem reading rules", qe);
     } catch (KruleStructureException ke) {
       logger.error("Error in rule RDF data:" + ke.getMessage(), ke);
       throw new InitializerException("Problem in rules RDF", ke);
     } catch (Throwable t) {
       logger.error("Unexpected error during loading: " + t.getMessage(), t);
       throw new InitializerException("Unexpected error loading rules", t);
     } finally {
       try {
         session.setAutoCommit(true);
       } catch (QueryException e) {
         logger.error("Unable to close transaction.", e);
         throw new InitializerException("Unable to close transaction", e);
       }
     }
 
     return rules;
   }
 
 
   /**
    * Loads all objects desribed in the RDF graph.
    */
   private void loadRdfObjects() throws QueryException, TuplesException, InitializerException, KruleStructureException {
     // get all the URIReferences
     findUriReferences();
     logger.debug("Got URI References");
     findVarReferences();
     logger.debug("Got Variable references");
     findLiteralReferences();
     logger.debug("Got Literal references");
 
     // pre-load all constraints
     loadSimpleConstraints();
     logger.debug("Got simple constraints");
     loadTransitiveConstraints();
     logger.debug("Got transitive constraints");
     loadJoinConstraints();
     logger.debug("Got join constraints");
     loadHavingConstraints();
     logger.debug("Got having constraints");
   }
 
 
   /**
    * Finds all the rules, and creates empty Rule objects to represent each one.
    *
    * @return A Rules structure containing all found rules.
    * @throws TuplesException There was an error retrieving data from the model.
    * @throws QueryException When there is an exception finding the rules.
    */
   private RuleStructure findRules() throws QueryException, TuplesException {
   	Query query;
     try {
       // find all of the rules
       query = interpreter.parseQuery("select $rule from <" + ruleModel + "> where $rule <rdf:type> <krule:Rule> ;");
     } catch (Exception e) {
       throw new QueryException("Invalid query.", e);
     }
     
     Answer ruleAnswer = session.query(query);
     logger.debug("Got response for rule query");
 
     // create the rule structure for all the rules
     RuleStructure rules = new RuleStructure();
 
     try {
       // create all the rules
       while (ruleAnswer.next()) {
         // create the rule and add it to the set
         rules.add(new Rule(ruleAnswer.getObject(0).toString()));
       }
     } finally {
       ruleAnswer.close();
     }
     logger.debug("Created rules" + rules.toString());
     return rules;
   }
 
 
   /**
    * Finds all the rule triggers, and links the rule objects accordingly.
    *
    * @throws TuplesException When there is an exception accessing the data.
    * @throws QueryException When there is an exception finding the triggers.
    * @throws InitializerException Data structures did not meet preconditions.
    */
   private void loadTriggers() throws QueryException, TuplesException, InitializerException {
     Query query;
     try {
       query = interpreter.parseQuery("select $src $dest from <" + ruleModel + "> where $src <krule:triggers> $dest ;");
     } catch (Exception e) {
       throw new QueryException("Invalid query.", e);
     }
 
     Answer answer = session.query(query);
 
     try {
       // link all the rules together
       while (answer.next()) {
         String src = answer.getObject(0).toString();
         String dest = answer.getObject(1).toString();
         logger.debug("Linking <" + src + "> -> <" + dest + ">");
         rules.setTrigger(src, dest);
       }
     } finally {
       answer.close();
     }
   }
 
 
   /**
    * Finds all the rule queries, and attach them to the rules.
    * Does not yet consider a HAVING clause.
    *
    * @throws TuplesException When there is an exception accessing the data.
    * @throws QueryException When there is an exception finding the queries.
    * @throws KruleStructureException When there is an error in the RDF data structure.
    * @throws InitializerException When there is an intialization error.
    */
   private void loadQueries() throws TuplesException, QueryException, KruleStructureException, InitializerException {
     logger.debug("Loading Queries");
     // go through the rules to set their queries
     Iterator<Rule> ri = rules.getRuleIterator();
     while (ri.hasNext()) {
       Rule rule = ri.next();
 
       logger.debug("Reading query for rule: " + rule.getName());
       Query query;
       try {
         // get the query data for this rule
         query = interpreter.parseQuery("select $pre $v $t from <" + ruleModel +
             "> where <" + rule.getName() + "> <krule:hasQuery> $q and $q <krule:selectionVariables> $vs and" +
             " $vs $pre $v and $pre <mulgara:prefix> <rdf:_> in <"+ prefixModel +
             "> and $v <rdf:type> $t ;");
       } catch (Exception e) {
         throw new QueryException("Invalid query.", e);
       }
       Answer answer = session.query(query);
 
       // get the length of the sequence prefix
       int prefixLength = ((URI)aliases.get("rdf")).toString().length() + 1;
       // get the variables and values as elements with the appropriate type
       URIReference[] elements = new URIReference[3];
       URIReference[] types = new URIReference[3];
       try {
         while (answer.next()) {
           logger.debug("Getting element from " + answer.getObject(0));
           // work out the position of the element.  Subject=0 Predicate=1 Object=2
           int seqNr = Integer.parseInt(answer.getObject(0).toString().substring(prefixLength)) - 1;
           logger.debug("parsed: " + seqNr);
           if (seqNr > elements.length) {
             throw new KruleStructureException("Rule " + rule.getName() + " has too many insertion elements");
           }
           // get the selection element and its type
           elements[seqNr] = (URIReference)answer.getObject(1);
           types[seqNr] = (URIReference)answer.getObject(2);
           logger.debug("Nr: " + seqNr + ", v: " + elements[seqNr] + ", type: " + types[seqNr]);
         }
       } finally {
         answer.close();
       }
       for (int select = 0; select < elements.length; select++) {
         if (elements[select] == null || types[select] == null) {
          throw new KruleStructureException("Rule " + rule.getName() + " does not have enough insertion elements");
         }
       }
       // convert these elements into ConstraintElements for the query
       QueryStruct queryStruct = new QueryStruct(elements, types, aliases, uriReferences, varReferences, literalReferences);
 
       // read in the WHERE reference
       try {
         // get the WHERE clause for this rule
         query = interpreter.parseQuery("select $w from <" + ruleModel +
             "> where <" + rule.getName() + "> <krule:hasQuery> $q and $q <krule:hasWhereClause> $w;");
       } catch (Exception e) {
         throw new QueryException("Invalid query.", e);
       }
       answer = session.query(query);
 
       try {
         // attach the correct constraint tree to the query structure
         if (answer.next()) {
           logger.debug("Setting where clause for rule: " + rule.getName() + "");
           Node whereClauseNode = (Node)answer.getObject(0);
           logger.debug("Where clause is: " + whereClauseNode);
           ConstraintExpression ce = (ConstraintExpression)constraintMap.get(whereClauseNode);
           logger.debug("where clause expression: " + ce);
           if (ce == null) {
             throw new KruleStructureException("Rule " + rule.getName() + " has no where clause");
           }
           queryStruct.setWhereClause(ce);
         }
 
         if (answer.next()) {
           throw new KruleStructureException("Rule " + rule.getName() + " has more than one query");
         }
       } finally {
         answer.close();
       }
 
       logger.debug("Setting models for the query");
       // set the models
       queryStruct.setModelExpression(baseModel, destModel);
 
       logger.debug("Setting query structure for the rule");
       // create a new query and set it for the rule
       rule.setQueryStruct(queryStruct);
     }
   }
 
 
   /**
    * Finds all the axioms, and builds up the statements.
    *
    * @return A set of Triples which form the axiom statements for the rules.
    * @throws TuplesException When there is an exception accessing the data.
    * @throws QueryException When there is an exception finding the queries.
    * @throws KruleStructureException When there is an error in the RDF data structure.
    * @throws InitializerException When there is an intialization error.
    */
   private Set<org.jrdf.graph.Triple> findAxioms() throws TuplesException, QueryException, KruleStructureException, InitializerException {
     logger.debug("Loading Axioms");
 
     Query query;
     try {
       // get the query data for this rule
       query = interpreter.parseQuery("select $s $p $o from <" + ruleModel +
           "> where $axiom <rdf:type> <krule:Axiom> and $axiom <krule:subject> $s" +
           " and $axiom <krule:predicate> $p and $axiom <krule:object> $o;");
     } catch (Exception e) {
       throw new QueryException("Invalid query.", e);
     }
     Answer answer = session.query(query);
 
     // prepare the set of axioms
     Set<org.jrdf.graph.Triple> axioms = new HashSet<org.jrdf.graph.Triple>();
 
     try {
       Node sn = null;
       Node pn = null;
       Node on = null;
       try {
         while (answer.next()) {
           // use general nodes first to get the data from the answer
           sn = (Node)answer.getObject(0);
           pn = (Node)answer.getObject(1);
           on = (Node)answer.getObject(2);
           // convert to URIReference, catch any problems in this structure
           URIReference subjectRef = (URIReference)sn;
           URIReference predicateRef = (URIReference)pn;
           URIReference objectRef = (URIReference)on;
           // get the referred nodes
           ConstraintElement subject = convertToElement(subjectRef);
           ConstraintElement predicate = convertToElement(predicateRef);
           ConstraintElement object = convertToElement(objectRef);
           // convert these to a triple
           org.jrdf.graph.Triple jrdfTriple = new TripleImpl(
                         (SubjectNode) subject, (PredicateNode) predicate, (ObjectNode) object);
           // add to the set of axiom statements
           axioms.add(jrdfTriple);
 
         }
       } catch (ClassCastException cce) {
         throw new KruleStructureException("Axioms must be built using references to Nodes.  Faulty axiom: {" +
             sn + "," + pn + "," + on + "}");
       }
     } finally {
       // make sure the answer is cleanly closed
       answer.close();
     }
     return axioms;
   }
 
 
   /**
    * Set up query aliases.
    *
    * @return A map of aliases to their fully qualified names
    */
   private Map<String,URI> newAliases() {
     aliases = new HashMap<String,URI>();
     try {
       aliases.put("rdf", new URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
       aliases.put("rdfs", new URI("http://www.w3.org/2000/01/rdf-schema#"));
       aliases.put("owl", new URI("http://www.w3.org/2002/07/owl#"));
       aliases.put("mulgara", new URI("http://mulgara.org/mulgara#"));
       aliases.put("krule", new URI("http://mulgara.org/owl/krule/#"));
     } catch (URISyntaxException e) {
       /* get those aliases which we could */
       logger.error("Error defining internal aliases: ", e);
     }
     return aliases;
   }
 
 
   /**
    * Confirm that required model types exist, and get their names.
    *
    * @throws TuplesException There was an error accessing the data.
    * @throws QueryException There was an error querying the model.
    */
   private void initializeUtilityModels() throws TuplesException, QueryException {
     logger.debug("reading the system model");
 
     Query query;
     try {
       // find the names of all prefix models
       query = interpreter.parseQuery("select $model from <" + systemModel +
           "> where $model <rdf:type> <mulgara:PrefixModel> ;");
     } catch (Exception e) {
       throw new QueryException("Invalid query.", e);
     }
     
     Answer answer = session.query(query);
     logger.debug("Found prefix models");
 
     try {
       // create all the rules
       if (answer.next()) {
         // create the rule and add it to the set
         prefixModel = answer.getObject(0).toString();
       } else {
         throw new QueryException("No prefix models available");
       }
     } finally {
       answer.close();
     }
   }
 
 
   /**
    * Queries for all URI references and loads their string representations.
    *
    * @throws TuplesException There was an error acessing the data.
    * @throws QueryException There was an error querying the model.
    * @throws InitializerException There was an error in the method preconditions.
    */
   private void findUriReferences() throws TuplesException, QueryException, InitializerException {
     logger.debug("Querying for URI reference objects.");
 
     Query query;
     try {
       // find the URI references and the referred URIs.
       query = interpreter.parseQuery("select $ref $uri from <" +
           ruleModel + "> where $ref <rdf:type> <krule:URIReference> and $ref <rdf:value> $uri ;");
     } catch (Exception e) {
       throw new QueryException("Invalid query.", e);
     }
     
     Answer answer = session.query(query);
     logger.debug("Found all URI references.");
 
     // create the mapping
     uriReferences = new HashMap<URIReference,URIReference>();
     // map each reference to the associated URI
     try {
       while (answer.next()) {
         URIReference ref = (URIReference)answer.getObject(0);
         URIReference uri = (URIReference)answer.getObject(1);
         logger.debug("Mapping <" + ref + "> to <" + uri + ">");
         uriReferences.put(ref, uri);
       }
     } finally {
       answer.close();
     }
     logger.debug("Mapped all URI references.");
   }
 
 
   /**
    * Queries for all variable references and loads their names.
    *
    * @throws TuplesException There was an error acessing the data.
    * @throws QueryException There was an error querying the model.
    * @throws InitializerException There was an error in the method preconditions.
    */
   private void findVarReferences() throws TuplesException, QueryException, InitializerException {
     logger.debug("Querying for variable reference objects.");
 
     Query query;
     try {
       // find the URI references and the referred URIs.
       query = interpreter.parseQuery("select $ref $name from <" +
           ruleModel + "> where $ref <rdf:type> <krule:Variable> and $ref <krule:name> $name ;");
     } catch (Exception e) {
       throw new QueryException("Invalid query.", e);
     }
     
     Answer answer = session.query(query);
     logger.debug("Found all variable references.");
 
     // create the mapping
     varReferences = new HashMap<URIReference,Variable>();
     try {
       // map each reference to the associated variable
       while (answer.next()) {
         URIReference ref = (URIReference)answer.getObject(0);
         Literal name = (Literal)answer.getObject(1);
         logger.debug("Mapping <" + ref + "> to <" + name + ">");
         varReferences.put(ref, new Variable(name.toString()));
       }
     } finally {
       answer.close();
     }
     logger.debug("Mapped all Variable references.");
   }
 
 
   /**
    * Queries for all Literal references and loads their string representations.
    *
    * @throws TuplesException There was an error acessing the data.
    * @throws QueryException There was an error querying the model.
    * @throws InitializerException There was an error in the method preconditions.
    */
   private void findLiteralReferences() throws TuplesException, QueryException, InitializerException {
     logger.debug("Querying for Literal objects.");
 
     Query query;
     try {
       // find the URI references and the referred URIs.
       query = interpreter.parseQuery("select $lit $str from <" +
           ruleModel + "> where $lit <rdf:type> <krule:Literal> and $lit <rdf:value> $str ;");
     } catch (Exception e) {
       throw new QueryException("Invalid query.", e);
     }
     
     Answer answer = session.query(query);
     logger.debug("Found all Literals.");
 
     // create the mapping
     literalReferences = new HashMap<Node,Literal>();
     try {
       // map each reference to the associated String
       while (answer.next()) {
         Node litRef = (Node)answer.getObject(0);
         Literal lit = (Literal)answer.getObject(1);
         logger.debug("Mapping <" + litRef + "> to <" + lit + ">");
         literalReferences.put(litRef, lit);
       }
     } finally {
       answer.close();
     }
     logger.debug("Mapped all Literals.");
   }
 
 
   /**
    * Finds all Simple constraints, and stores them by node.
    *
    * @throws TuplesException There was an error retrieving data from the model.
    * @throws QueryException There was an error querying the model.
    * @throws KruleStructureException There was an error in the krule model.
    */
   private void loadSimpleConstraints() throws KruleStructureException, TuplesException, QueryException {
     logger.debug("Querying for Simple constraints.");
 
     Query query;
     try {
       // find the URI references and the referred URIs.
       query = interpreter.parseQuery("select $c $p $o from <" + ruleModel +
           "> where $c <rdf:type> <krule:SimpleConstraint> and $c $p $o and " +
           "($p <mulgara:is> <krule:hasSubject> or $p <mulgara:is> <krule:hasPredicate> or " +
           "$p <mulgara:is> <krule:hasObject> or $p <mulgara:is> <krule:hasModel>);");
     } catch (Exception e) {
       throw new QueryException("Invalid query.", e);
     }
 
     Answer answer = session.query(query);
     logger.debug("Found all simple constraints.");
 
     // create a mapping of URIs to simple constraint structures
     Map<Node,Map<Node,Node>> simpleConstraints = new HashMap<Node,Map<Node,Node>>();
     try {
       // map each reference to the associated property/values
       while (answer.next()) {
         Node constraintNode = (Node)answer.getObject(0);
         URIReference predicate = (URIReference)answer.getObject(1);
         Node object = (Node)answer.getObject(2);
         logger.debug("setting <" + constraintNode + ">.<" + predicate + "> = " + object);
         addProperty(simpleConstraints, constraintNode, predicate, object);
       }
     } finally {
       answer.close();
     }
 
     logger.debug("Mapped all constraints to their property/values");
 
     // collect all property/values together into constraints
     for (Map.Entry<Node,Map<Node,Node>> entry: simpleConstraints.entrySet()) {
       // get the node in question
       Node constraintNode = entry.getKey();
       // get its properties
       Map<Node,Node> pv = entry.getValue();
       // get the individual properties
       ConstraintElement s = convertToElement(pv.get(HAS_SUBJECT));
       ConstraintElement p = convertToElement(pv.get(HAS_PREDICATE));
       ConstraintElement o = convertToElement(pv.get(HAS_OBJECT));
       // check if there is a "from" property
       Node from = pv.get(HAS_MODEL);
       // build the appropriate constraint
       // add it to the map
       if (from == null) {
         logger.debug("Creating <" + constraintNode + "> as (<" + s + "> <" + p + "> <" + o +">)");
         constraintMap.put(constraintNode, ConstraintFactory.newConstraint(s, p, o));
       } else {
         logger.debug("Creating <" + constraintNode + "> as (<" + s + "> <" + p + "> <" + o +">) in <" + from + ">");
         constraintMap.put(constraintNode, ConstraintFactory.newConstraint(s, p, o, convertToElement(from)));
       }
     }
   }
 
 
   /**
    * Finds all join constraints which contain other constraints, and stores them by node.
    *
    * @throws TuplesException There was an error retrieving data from the model.
    * @throws QueryException There was an error querying the model.
    * @throws KruleStructureException There was an error querying the model.
    */
   private void loadJoinConstraints() throws KruleStructureException, TuplesException, QueryException {
     // build constraints in place, recursively constructing child constraints until all are found
     logger.debug("Querying for Join constraints.");
 
     Query query;
     try {
       // find the URI references and the referred URIs.
       query = interpreter.parseQuery("select $constraint $constraint2 $type from <" + ruleModel +
           "> where $constraint <krule:argument> $constraint2 and $constraint <rdf:type> $type and " +
          "($type <mulgara:is> <krule:ConstraintConjunction> or $type <mulgara:is> <krule:ConstraintDisjuntion>);");
     } catch (Exception e) {
       throw new QueryException("Invalid query.", e);
     }
 
     Answer answer = session.query(query);
     logger.debug("Found all join constraints.");
 
     // accumulate all the constraint links and types
 
     // create a map of join constraints to the constraints that they join
     Map<Node,Set<Node>> constraintLinks = new HashMap<Node,Set<Node>>();
 
     // map the join constraints to the type of join
     Map<Node,URIReference> joinTypes = new HashMap<Node,URIReference>();
 
     try {
       // map each reference to the associated argument and type
       while (answer.next()) {
         Node constraintNode = (Node)answer.getObject(0);
         Node constraintNode2 = (Node)answer.getObject(1);
         URIReference type = (URIReference)answer.getObject(2);
         logger.debug("constraint (" + type + ")<" + constraintNode + "> -> <" + constraintNode2 + ">");
         // map the constraint to its argument
         addLink(constraintLinks, constraintNode, constraintNode2);
         // map the type
         joinTypes.put(constraintNode, type);
       }
     } finally {
       answer.close();
     }
 
     logger.debug("mapping join constraint RDF nodes to join constraint objects");
     // collect all arguments together into constraints and map the node to the constraint
     for (Map.Entry<Node,Set<Node>> entry: constraintLinks.entrySet()) {
       // get the constraint node in question
       Node constraintNode = entry.getKey();
       // see if it maps to a constraint
       if (constraintMap.get(constraintNode) == null) {
         // the constraint does not exist
         // get the argument nodes
         Set<Node> args = entry.getValue();
         // get the constraint's type
         Node type = joinTypes.get(constraintNode);
         if (type == null) {
         	  throw new KruleStructureException("No type available on join constraint");
         }
         // convert the RDF nodes to constraints
         List<ConstraintExpression> constraintArgs = getConstraints(args, constraintLinks, joinTypes);
         ConstraintExpression joinConstraint = newJoinConstraint(type, constraintArgs);
         logger.debug("mapped " + constraintNode + " -> " + joinConstraint);
         // build the join constraint, and map the node to it
         constraintMap.put(constraintNode, joinConstraint);
       } else {
         logger.debug("constraint <" + constraintNode + "> already exists");
       }
     }
     // every key should now be mapped to a constraint object
     logger.debug("mapped all constraint nodes to constraints");
   }
 
 
   /**
    * Finds all having constraints.
    *
    * @throws TuplesException There was an error retrieving data from the model.
    * @throws QueryException There was an error querying the model.
    * @throws KruleStructureException There was an error querying the model.
    */
   private void loadHavingConstraints() throws KruleStructureException, TuplesException, QueryException {
     logger.debug("Querying for Having constraints.");
 
     Query query;
     try {
       // find the URI references and the referred URIs.
       query = interpreter.parseQuery("select $constraint from <" + ruleModel +
           "> where $rule <krule:hasHavingClause> $constraint;");
     } catch (Exception e) {
       throw new QueryException("Invalid query.", e);
     }
 
     Answer answer = session.query(query);
     logger.debug("Found all having constraints.");
 
     try {
       if (answer.next()) {
         throw new KruleStructureException("Having structures not implemented");
       }
     } finally {
       answer.close();
     }
   }
 
 
   /**
    * Finds all Transitive constraints, and stores them by node.
    *
    * @throws TuplesException There was an error retrieving data from the model.
    * @throws QueryException There was an error querying the model.
    * @throws KruleStructureException There was an error in the krule model.
    */
   private void loadTransitiveConstraints() throws KruleStructureException, TuplesException, QueryException {
     logger.debug("Querying for Transitive constraints.");
 
     Query query;
     try {
       // find the URI references and the referred URIs.
       query = interpreter.parseQuery("select $c $p $arg from <" + ruleModel +
           "> where $c <rdf:type> <krule:TransitiveConstraint> and $c $p $arg and " +
           "($p <mulgara:is> <krule:transitiveArgument> or $p <mulgara:is> <krule:anchorArgument>);");
     } catch (Exception e) {
       throw new QueryException("Invalid query.", e);
     }
     Answer answer = session.query(query);
 
     logger.debug("Retrieved all transitive constraints.");
 
     // set up a mapping of constraints to predicate/SimpleConstraint pairs
     Map<Node,Map<Node,Node>> transMap = new HashMap<Node,Map<Node,Node>>();
 
     try {
       // accumulate the transitive arguments
       while (answer.next()) {
         Node transConstraint = (Node)answer.getObject(0);
         URIReference predicate = (URIReference)answer.getObject(1);
         Node argument = (Node)answer.getObject(2);
         addProperty(transMap, transConstraint, predicate, argument);
         logger.debug("mapping <" + transConstraint + "> to <" + predicate + ">.<" + argument +">");
       }
     } finally {
       answer.close();
     }
     logger.debug("Mapped all transitive properties");
 
     // build a new transconstraint for each transitive constraint node
     for (Map.Entry<Node,Map<Node,Node>> tEntry: transMap.entrySet()) {
       Node constraintNode = tEntry.getKey();
       Map<Node,Node> arguments = tEntry.getValue();
       Constraint constraint;
       // build the constraint based on the arguments
       if (arguments.size() == 1) {
         Node sc = arguments.get(TRANSITIVE_ARGUMENT);
         if (sc == null) {
           throw new KruleStructureException("Transitive argument not correct");
         }
         logger.debug("Mapping transitive constraint <" + constraintNode +"> to <" + sc +">");
         // get the simple constraint and build the transitive constraint around it
         constraint = new SingleTransitiveConstraint((Constraint)constraintMap.get(sc));
       } else if (arguments.size() == 2) {
         Node sc = arguments.get(TRANSITIVE_ARGUMENT);
         Node anchor = arguments.get(ANCHOR_ARGUMENT);
         if (sc == null || anchor == null) {
           throw new KruleStructureException("Transitive arguments not correct");
         }
         logger.debug("Mapping transitive constraint <" + constraintNode +"> to <" + sc +">,<" + anchor + ">");
         // get the simple constraint and build the transitive constraint around it
         constraint = new TransitiveConstraint((Constraint)constraintMap.get(anchor), (Constraint)constraintMap.get(sc));
       } else {
         throw new KruleStructureException("Expected 1 or 2 arguments for Transitive constraint, got: " + arguments.size());
       }
       // map the transitive constraint node to the transitive constraint
       constraintMap.put(constraintNode, constraint);
     }
     logger.debug("Mapped all transitive constraints");
   }
 
 
   /**
    * Converts a set of constraint Nodes from an RDF graph into a List of Constraint objects.
    * If the object for a Node already exists, then this is returned, otherwise create a new
    * Constraint object.  All simple constraints should exist, only leaving join constraints
    * to be created.  The constraintLinks and typeMap arguments are for constructing new
    * constraint objects.
    *
    * @param constraints The set of constraint nodes to get the constraints for.  Whenever possible,
    *             the constraints come from constraintMap.
    * @param constraintLinks Linkage of join constraints to their arguments.  Used to create a new constraint.
    * @param typeMap Maps constraint nodes to their type.  Used to create a new constraint.
    * @throws KruleStructureException There was an error in the RDF data structure.
    */
   private List<ConstraintExpression> getConstraints(Set<Node> constraints, Map<Node,Set<Node>> constraintLinks, Map<Node,URIReference> typeMap) throws KruleStructureException {
     logger.debug("converting nodes to constraint list: " + constraints);
 
     // build the return list
     List<ConstraintExpression> cList = new ArrayList<ConstraintExpression>();
     // go through the arguments
     for (Node cNode: constraints) {
       logger.debug("converting: " + cNode);
       // get the constraint expression object
       ConstraintExpression constraintExpr = (ConstraintExpression)constraintMap.get(cNode);
       if (constraintExpr == null) {
         logger.debug(cNode.toString() + " not yet mapped to constraint");
         // constraint expression object does not yet exist, get its arguments
         Set<Node> constraintArgNodes = constraintLinks.get(cNode);
         // build the constraint expression - get the arguments as a list of constraints
         List<ConstraintExpression> constraintArgs = getConstraints(constraintArgNodes, constraintLinks, typeMap);
         constraintExpr = newJoinConstraint((Node)typeMap.get(cNode), constraintArgs);
       }
       // add the constraint argument to the list
      cList.add(constraintExpr);
     }
     return cList;
   }
 
 
   /**
    * Create a new join constraint.
    *
   * @param type The URI for the type to create.
    * @param args The list of arguments for the constraint.
   * @return a new join constraint of the correct type.
    */
   private ConstraintExpression newJoinConstraint(Node type, List<ConstraintExpression> args) throws KruleStructureException {
     logger.debug("Building join constraint of type <" + type + ">: " + args);
 
     if (type.equals(CONSTRAINT_CONJUNCTION)) {
       return new ConstraintConjunction(args);
     } else if (type.equals(CONSTRAINT_DISJUNCTION)) {
       return new ConstraintDisjunction(args);
     }
     throw new KruleStructureException("Unknown constraint type: " + type);
   }
 
 
   /**
    * Converts an RDF Node to a constraint element.
    *
    * @param node The node to convert.
    * @throws KruleStructureException If node cannot be converted.
    */
   private ConstraintElement convertToElement(Node node) throws KruleStructureException {
     logger.debug("converting " + node + " to ConstraintElement");
     // check that this is a named node
     if (node instanceof URIReference) {
       // get the referred node
       URIReferenceImpl ref = (URIReferenceImpl)uriReferences.get(node);
       if (ref != null) {
         return ref;
       }
       // not referred, so look in the variables
       Variable var = (Variable)varReferences.get(node);
       if (var != null) {
         return var;
       }
     } else {
       // This could be an anonymous Literal
       LiteralImpl lit = (LiteralImpl)literalReferences.get(node);
       if (lit != null) {
         return lit;
       }
     }
     throw new KruleStructureException("Invalid constraint element: " + node);
   }
 
 
   /**
    * Sets a property for a node, creating the entry if it does not exist yet.
    *
    * @param map The mapping of nodes to property/values.
    * @param node The node to set the property for.
    * @param predicate The property to set.
    * @param object The value to set the property to.
    */
   private static void addProperty(Map<Node,Map<Node,Node>> map, Node node, URIReference predicate, Node object) {
     // get the current set of properties
     Map<Node,Node> pv = map.get(node);
     // check that the map exists
     if (pv == null) {
       // no, so create
       pv = new HashMap<Node,Node>();
       pv.put(predicate, object);
       // add to the map
       map.put(node, pv);
     } else {
       // update the map to hold the new value
       pv.put(predicate, object);
     }
   }
 
 
   /**
    * Maps a node to another node, creating the entry if it does not exist yet.
    *
    * @param map The mapping of nodes to tuples.
    * @param node1 The node to map.
    * @param node2 The node to map it to.
    */
   private static void addLink(Map<Node,Set<Node>> map, Node node1, Node node2) {
     // get the current set of properties
     Set<Node> links = map.get(node1);
     // check that the set exists
     if (links == null) {
       // no, so create
       links = new HashSet<Node>();
       links.add(node2);
       // add to the map
       map.put(node1, links);
     } else {
       // update the map to hold the new value
       links.add(node2);
     }
   }
 
 }
