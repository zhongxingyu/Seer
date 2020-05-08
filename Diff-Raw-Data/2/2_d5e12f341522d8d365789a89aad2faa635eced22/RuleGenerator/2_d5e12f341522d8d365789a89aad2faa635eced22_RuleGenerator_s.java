 /*
  * Copyright 2008 Fedora Commons, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.mulgara.krule.rlog.ast.output;
 
 import org.jrdf.vocabulary.RDF;
 import org.mulgara.krule.rlog.ParseException;
 import org.mulgara.krule.rlog.ast.Predicate;
 import org.mulgara.krule.rlog.ast.Rule;
 import org.mulgara.krule.rlog.parser.URIParseException;
 import org.mulgara.krule.rlog.rdf.Literal;
 import org.mulgara.krule.rlog.rdf.RDFNode;
 import org.mulgara.krule.rlog.rdf.Var;
 import org.mulgara.query.rdf.LiteralImpl;
 import org.mulgara.query.rdf.URIReferenceImpl;
 import org.mulgara.resolver.spi.LocalizeException;
 import org.mulgara.resolver.spi.ResolverSession;
 import org.mulgara.store.nodepool.NodePoolException;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import static org.mulgara.krule.KruleLoader.ARGUMENT;
 import static org.mulgara.krule.KruleLoader.CONSTRAINT_CONJUNCTION;
 import static org.mulgara.krule.KruleLoader.DIFFERENCE;
 import static org.mulgara.krule.KruleLoader.HAS_QUERY;
 import static org.mulgara.krule.KruleLoader.HAS_WHERE_CLAUSE;
 import static org.mulgara.krule.KruleLoader.HAS_SUBJECT;
 import static org.mulgara.krule.KruleLoader.HAS_PREDICATE;
 import static org.mulgara.krule.KruleLoader.HAS_OBJECT;
 import static org.mulgara.krule.KruleLoader.HAS_GRAPH;
 import static org.mulgara.krule.KruleLoader.NAME;
 import static org.mulgara.krule.KruleLoader.QUERY;
 import static org.mulgara.krule.KruleLoader.RULE;
 import static org.mulgara.krule.KruleLoader.SELECTION_VARS;
 import static org.mulgara.krule.KruleLoader.SIMPLE_CONSTRAINT;
 import static org.mulgara.krule.KruleLoader.TRIGGERS;
 import static org.mulgara.krule.KruleLoader.VARIABLE;
 
 /**
  * Writes rules to a list of triples.
  * 
  * @created May 16, 2008
  * @author Paul Gearon
  * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
  */
 public class RuleGenerator extends TripleGenerator {
 
   /** The collection of rules that this class emits. */
   public Collection<Rule> rules;
 
   /** The node for the rdf:Sequence type */
   private final long rdfSeq;
 
   /** The node for the krule:Rule type */
   private final long kruleRule;
 
   /** The node for the krule:Query type */
   private final long kruleQuery;
 
   /** The node for the krule:Difference type */
   private final long kruleDifference;
 
   /** The node for the krule:ConstraintConjunction type */
   private final long kruleConjunction;
 
   /** The node for the krule:SimpleConstraint type */
   private final long kruleSimple;
 
   /** The node for the krule:Variable type */
   private final long kruleVariable;
 
   /** The node for the krule:hasQuery predicate */
   private final long kruleHasQuery;
 
   /** The node for the krule:triggers predicate */
   private final long kruleTriggers;
 
   /** The node for the krule:selectionVariables predicate */
   private final long kruleSelVars;
 
   /** The node for the krule:hasWhereClause predicate */
   private final long kruleHasWhereClause;
 
   /** The node for the krule:hasSubject predicate */
   private final long kruleHasSubject;
 
   /** The node for the krule:hasPredicate predicate */
   private final long kruleHasPredicate;
 
   /** The node for the krule:hasObject predicate */
   private final long kruleHasObject;
 
   /** The node for the krule:hasModel predicate */
   private final long kruleHasGraph;
 
   /** The node for the krule:argument predicate */
   private final long kruleArgument;
 
   /** The node for the krule:name predicate */
   private final long kruleName;
 
   /** The nodes for RDF sequence membership */
   private final List<Long> cachedSeq = new ArrayList<Long>();
 
   /**
    * Creates a new writer for a collection of rules.
    * @param rules The rules to be written.
    * @throws LocalizeException If localized nodes could not be accessed.
    */
   public RuleGenerator(Collection<Rule> rules, ResolverSession resolverSession) throws LocalizeException {
     super(resolverSession);
     this.rules = rules;
     rdfSeq = resolverSession.localize(new URIReferenceImpl(RDF.SEQ));
     kruleRule = resolverSession.localize(RULE);
     kruleQuery = resolverSession.localize(QUERY);
     kruleDifference = resolverSession.localize(DIFFERENCE);
     kruleConjunction = resolverSession.localize(CONSTRAINT_CONJUNCTION);
     kruleSimple = resolverSession.localize(SIMPLE_CONSTRAINT);
     kruleVariable = resolverSession.localize(VARIABLE);
     kruleHasQuery = resolverSession.localize(HAS_QUERY);
     kruleHasSubject = resolverSession.localize(HAS_SUBJECT);
     kruleHasPredicate = resolverSession.localize(HAS_PREDICATE);
     kruleHasObject = resolverSession.localize(HAS_OBJECT);
     kruleHasGraph = resolverSession.localize(HAS_GRAPH);
     kruleTriggers = resolverSession.localize(TRIGGERS);
     kruleSelVars = resolverSession.localize(SELECTION_VARS);
     kruleHasWhereClause = resolverSession.localize(HAS_WHERE_CLAUSE);
     kruleArgument = resolverSession.localize(ARGUMENT);
     kruleName = resolverSession.localize(NAME);
     initSeqTo(3);
   }
 
 
   /**
    * {@inheritDoc} 
    * @throws ParseException Constructing URIs for the output resulted in an invalid URI.
    * @throws NodePoolException If blank nodes could not be created.
    */
   public List<long[]> emit(List<long[]> triples) throws ParseException, NodePoolException {
     try {
       for (Rule r: rules) emitRule(triples, r);
     } catch (LocalizeException e) {
       throw new NodePoolException("Unable to localize a node", e);
     } catch (URISyntaxException e) {
       throw new ParseException("Malformed URI in rules: " + e.getMessage());
     } catch (URIParseException e) {
       throw new ParseException("Malformed URI in rules: " + e.getMessage());
     }
     return triples;
   }
 
 
   /**
    * Create the the triple representation of a rule and append it.
    * @param triples the List to append the triples to.
    * @param rule The rule to emit.
    * @throws NodePoolException If blank nodes could not be created.
    * @throws ParseException If any bad URIs or non-object literals are encountered.
    * @throws URISyntaxException URIs in the rule are not formed correctly.
    * @throws LocalizeException Due to an error localizing a URI.
    * @throws URIParseException  URIs in the rule are not formed correctly.
    */
   private List<long[]> emitRule(List<long[]> triples, Rule rule) throws NodePoolException, ParseException, LocalizeException, URISyntaxException, URIParseException {
     long ruleNode = toKruleNode(rule.getName());
 
     // rule rdf:type kruleRule
     add(triples, ruleNode, rdfType, kruleRule);
 
     emitTriggers(triples, ruleNode, rule.getTriggers());
 
     // query rdf:type krule:Query
     // rule krule:hasQuery query
     long query = newBlankNode();
     add(triples, query, rdfType, kruleQuery);
     add(triples, ruleNode, kruleHasQuery, query);
 
     emitSelection(triples, query, rule.getHead());
     emitWhereClause(triples, query, rule.getBody(), rule.getBodySubtractions());
 
     return triples;
   }
 
 
   /**
    * Adds the triggers for a rule to the triples.
    * @param triples The list of triples to append to.
    * @param triggers The rules that get triggered by the current rule.
    * @throws URISyntaxException IF a trigger rule has a malformed URI.
    * @throws LocalizeException If the URI in a trigger rule cannot be localized.
    */
   private void emitTriggers(List<long[]> triples, long rule, Collection<Rule> triggers) throws LocalizeException, URISyntaxException {
     for (Rule r: triggers) add(triples, rule, kruleTriggers, toKruleNode(r.getName()));
   }
 
 
   /**
    * Adds the head of a rule to the triples.
    * @param triples The list of triples to append to.
    * @param selection The selection that makes up the head of a rule.
    * @throws LocalizeException Unable to create a new blank node.
    * @throws URISyntaxException If a selected URI is incorrectly formed.
    */
   private void emitSelection(List<long[]> triples, long query, Predicate selection) throws URIParseException, LocalizeException, URISyntaxException {
     // seq rdf:type rdf:Seq
     // query krule:selectionVariables seq
     long seq = newBlankNode();
     add(triples, seq, rdfType, rdfSeq);
     add(triples, query, kruleSelVars, seq);
 
     // seq rdf:_1 getSubject()
     // seq rdf:_2 getPredicate()
     // seq rdf:_3 getObject()
     add(triples, seq, getSeq(1), toKruleNode(selection.getSubject().getRdfLabel()));
     add(triples, seq, getSeq(2), toKruleNode(selection.getPredicate().getRdfLabel()));
     add(triples, seq, getSeq(3), toKruleNode(selection.getObject().getRdfLabel()));
   }
 
 
   /**
    * Prints the body for a rule to a PrintStream.
    * @param out The PrintStream to send the where clause to.
    * @param body The where clause that makes up the body of the rule.
    * @param subs The part of a where clause to be removed from the body.
    * @throws URIParseException If one of the URIs in the constraint expression has an invalid syntax.
    * @throws LocalizeException Unable to localize some of the URIs in the constraints.
    * @throws URISyntaxException If one of the URIs in the constraint has an invalid syntax.
    */
   private void emitWhereClause(List<long[]> triples, long query, List<Predicate> body, List<Predicate> subs) throws URIParseException, LocalizeException, URISyntaxException {
     long constraintExpr = newBlankNode();
     // query krule:hasWhereClause constraintExpr
     add(triples, query, kruleHasWhereClause, constraintExpr);
     
     if (subs.isEmpty()) emitConjunction(triples, constraintExpr, body);
     else emitSubtractions(triples, constraintExpr, body, subs);
   }
 
 
   /**
    * Emit the operation for the difference between the body and the subtractions as a set of triples.
    * @param triples The 
    * @param body The main body to be selected (the minuend).
    * @param subs The constraints to be subtracted from the body (the subtrahends).
    * @throws URIParseException The constraints contain URIs that are invalid.
    * @throws LocalizeException Unable to localize some of the URIs in the constraint.
    * @throws URISyntaxException If one of the URIs in the constraint has an invalid syntax.
    */
   private void emitSubtractions(List<long[]> triples, long diff, List<Predicate> body, List<Predicate> subs) throws URIParseException, LocalizeException, URISyntaxException {
     // constraintExpr rdf:type krule:Difference
     add(triples, diff, rdfType, kruleDifference);
 
     long argument = newBlankNode();
     // diff krule:argument argument
     add(triples, diff, kruleArgument, argument);
 
     int lastElt = subs.size() - 1;
     if (lastElt == 0) emitConjunction(triples, argument, body);
     else emitSubtractions(triples, argument, body, subs.subList(0, lastElt));
 
     // last argument in subtraction
     // diff krule:argument argument
     argument = newBlankNode();
     add(triples, diff, kruleArgument, argument);
 
     emitSimpleConstraint(triples, argument, subs.get(lastElt));
   }
 
 
   /**
    * Adds a list of constraints as the arguments to a single ConstraintConjunction.
    * @param triples The list of triples to add the statements to.
    * @param conj The node representing the conjunction being emitted.
    * @param conjunction The list of constraints that form the conjunction to be added.
    * @throws URIParseException If one of the URIs in the constraint has an invalid syntax.
    * @throws LocalizeException Unable to localize a URI in the expression.
    * @throws URISyntaxException If one of the URIs in the constraint has an invalid syntax.
    */
   private void emitConjunction(List<long[]> triples, long conjNode, List<Predicate> conjunction) throws URIParseException, LocalizeException, URISyntaxException {
     if (conjunction.size() == 1) emitSimpleConstraint(triples, conjNode, conjunction.get(0));
     else {
       // conj rdf:type krule:ConstraintConjunction
       add(triples, conjNode, rdfType, kruleConjunction);
 
       for (Predicate op: conjunction) {
         long argument = newBlankNode();
         // conj krule:argument argument
         add(triples, conjNode, kruleArgument, argument);
 
         emitSimpleConstraint(triples, argument, op);
       }
     }
   }
 
 
   /**
    * Adds a single constraint to the generated triples.
    * @param triples The triples to add to.
    * @param constraintNode The local node representing the constraint.
    * @param constraint The constraint to write.
    * @throws URIParseException If one of the URIs in the constraint has an invalid syntax.
    * @throws LocalizeException Unable to localize a URI in the expression.
    * @throws URISyntaxException If one of the URIs in the constraint has an invalid syntax.
    */
   private void emitSimpleConstraint(List<long[]> triples, long constraintNode, Predicate constraint) throws URIParseException, LocalizeException, URISyntaxException {
     // constraintNode rdf:type krule:SimpleConstraint
     add(triples, constraintNode, rdfType, kruleSimple);
 
     // constraintNode krule:hasSubject subjNode
     // constraintNode krule:hasPredicate predNode
     // constraintNode krule:hasObject objNode
     // constraintNode krule:hasModel graphNode  [OPTIONAL]
 
     long node = emitNode(triples, constraint.getSubject());
     add(triples, constraintNode, kruleHasSubject, node);
 
     node = emitNode(triples, constraint.getPredicate());
     add(triples, constraintNode, kruleHasPredicate, node);
 
     node = emitNode(triples, constraint.getObject());
     add(triples, constraintNode, kruleHasObject, node);
 
     if (constraint.hasGraphAnnotation()) {
       node = emitNode(triples, constraint.getGraphAnnotation());
       add(triples, constraintNode, kruleHasGraph, node);
     }
   }
 
 
   /**
    * Add the details for a node to the list of triples.
    * @param triples The triples to add the node for.
    * @param node The structured data for the node being emitted.
    * @return The localized node created for this node.
    * @throws LocalizeException Unable to localize the URI for the node, or structure describing the node.
    * @throws URISyntaxException The node URI, or a URI associated with this node is malformed.
    */
   private long emitNode(List<long[]> triples, RDFNode node) throws LocalizeException, URISyntaxException {
     if (node.isReference()) {
       // node rdf:type krule:URIReference
       long r = toKruleNode(node.getRdfLabel());
       add(triples, r, rdfType, kruleUriReference);
       return r;
     } else if (node.isVariable()) {
       // node rdf:type krule:Variable
       long r = toKruleNode(node.getRdfLabel());
       add(triples, r, rdfType, kruleVariable);
       add(triples, r, kruleName, localizeString(((Var)node).getName()));
       return r;
     } else {
       // node rdf:type krule:Literal
       // node rdf:value "......"
       long r = newBlankNode();
       add(triples, r, rdfType, getKruleLiteral());
       add(triples, r, getRdfValue(), toLiteral(node));
       return r;
     }
   }
 
 
   /**
    * Converts a node representing a literal to a localized literal.
    * If krule Literals start to support data types, then so should this method.
    * @param node The RDFNode that can be cast to a literal.
    * @return A localized gNode for the literal.
    * @throws LocalizeException The string could not be written to or accessed from the string pool.
    */
   private long toLiteral(RDFNode node) throws LocalizeException {
     Literal l = (Literal)node;
     return resolverSession.localize(new LiteralImpl(l.getLexical()));
   }
 
 
   /**
    * Localizes a string.
    * @param str The string to localize.
    * @return The gNode which represents the string in local form.
    * @throws LocalizeException The string could not be created in the string pool.
    */
   private long localizeString(String str) throws LocalizeException {
     return resolverSession.localize(new LiteralImpl(str));
   }
 
 
   /**
    * Get a cached sequence number. This will get more if they have not been cached yet.
    * @param i The sequence number to retrieve. Starts at 1.
    * @return The localized value of the URI rdf:_i
    * @throws LocalizeException If an uncached URI could not be localized.
    */
   private long getSeq(int i) throws LocalizeException {
     assert i >= 1;
     if (i > cachedSeq.size()) initSeqTo(i);
     // retrieving a 1-based sequence from a 0-based list
     return cachedSeq.get(i - 1);
   }
 
 
   /**
    * Fill the sequence array to the element given.
    * @param largest The largest sequence element to initialize.
    * @throws LocalizeException If a URI cannot be localized.
    */
   private void initSeqTo(int largest) throws LocalizeException {
     String seqBase = RDF.BASE_URI.toString() + "_";
     for (int i = cachedSeq.size() + 1; i <= largest; i++) {
       URI s = URI.create(seqBase + i);
      cachedSeq.add(resolverSession.lookup(new URIReferenceImpl(s)));
     }
   }
 
 }
