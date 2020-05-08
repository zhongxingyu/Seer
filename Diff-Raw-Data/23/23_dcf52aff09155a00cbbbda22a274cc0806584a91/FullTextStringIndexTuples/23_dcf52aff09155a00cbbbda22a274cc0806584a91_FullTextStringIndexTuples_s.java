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
 
 package org.mulgara.resolver.lucene;
 
 // Java 2 standard packages
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Collections;
 import java.util.ArrayList;
 import java.util.List;
 
 // Third party packages
 import org.apache.log4j.Logger;
 import org.apache.lucene.document.Document;
 
 // JRDf
 import org.jrdf.graph.URIReference;
 
 // local packages
 import org.mulgara.query.Constraint;
 import org.mulgara.query.ConstraintElement;
 import org.mulgara.query.LocalNode;
 import org.mulgara.query.QueryException;
 import org.mulgara.query.TuplesException;
 import org.mulgara.query.Variable;
 import org.mulgara.query.rdf.LiteralImpl;
 import org.mulgara.query.rdf.URIReferenceImpl;
 import org.mulgara.resolver.spi.GlobalizeException;
 import org.mulgara.resolver.spi.LocalizeException;
 import org.mulgara.resolver.spi.Resolution;
 import org.mulgara.resolver.spi.ResolverSession;
 import org.mulgara.store.tuples.AbstractTuples;
 import org.mulgara.store.tuples.Tuples;
 
 /**
  * A {@link Tuples} backed by a {@link FullTextStringIndex}.
  *
  * @created 2002-03-27
  *
  * @author <a href="http://staff.pisoftware.com/raboczi">Simon Raboczi</a>
  *
  * @version $Revision: 1.10 $
  *
  * @modified $Date: 2005/05/02 20:07:57 $
  *
  * @maintenanceAuthor $Author: raboczi $
  *
  * @company <A href="mailto:info@PIsoftware.com">Plugged In Software</A>
  *
  * @copyright &copy; 2002-2004 <A href="http://www.PIsoftware.com/">Plugged In
  *      Software Pty Ltd</A>
  *
  * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
  */
 class FullTextStringIndexTuples extends AbstractTuples implements Resolution, Cloneable {
   /** Logger.  */
   private final static Logger logger = Logger.getLogger(FullTextStringIndexTuples.class);
 
   /**
    * The native Lucene query result to represent as a {@link Tuples}.
    */
   private FullTextStringIndex.Hits hits;
 
   /**
    * The current document within the {@link #hits}.
    *
    * A Lucene document hit corresponds to a {@link Tuples} row.
    */
   private Document document;
 
   /**
    * The index of the next {@link #document} within the {@link #hits}.
    */
   private int nextDocumentIndex = 0;
 
   /**
    * Session used to localize Lucene text into string pool nodes.
    */
   private ResolverSession session;
 
   /**
    * The number of items in to tuples
    */
   private long rowCount = -1;
 
   private final List<Variable> variableList = new ArrayList<Variable>(3);
   private final List<String> luceneKeyList = new ArrayList<String>(3);
 
   private LuceneConstraint constraint;
 
   //
   // Constructor
   //
 
   /**
    * Find the answer to a single constraint.
    *
    * The {@link org.mulgara.query.Answer}'s columns will
    * match the variables in the <var>constraint</var>, except that a magical
    * column called <code>$score</code> is added at the end, containing Lucene's
    * reckoning of how close the matches are.
    *
    * @param fullTextStringIndex PARAMETER TO DO
    * @param constraint the single constraint
    * @param session a session context for globalization, etc
    * @throws QueryException if the set of triples couldn't be determined
    */
   FullTextStringIndexTuples(FullTextStringIndex fullTextStringIndex,
       LuceneConstraint constraint, ResolverSession session) throws QueryException {
     this.session = session;
     this.constraint = constraint;
 
     try {
       // Validate and globalize subject
       String subject = null;
       ConstraintElement subjectElement = constraint.getSubject();
 
       if (subjectElement instanceof Variable) {
         variableList.add((Variable)subjectElement);
         luceneKeyList.add(FullTextStringIndex.SUBJECT_KEY);
       } else if (subjectElement instanceof LocalNode) {
         try {
           URIReference subjectURI = (URIReference) session.globalize(((
               LocalNode) subjectElement).getValue());
           subject = subjectURI.getURI().toString();
         } catch (ClassCastException ec) {
           throw new QueryException("Bad subject in Lucene constraint", ec);
         }
       }
 
       // Validate and globalize predicate
       String predicate = null;
       ConstraintElement predicateElement = constraint.getPredicate();
       if (predicateElement instanceof Variable) {
         variableList.add((Variable)predicateElement);
         luceneKeyList.add(FullTextStringIndex.PREDICATE_KEY);
       } else if (predicateElement instanceof LocalNode) {
         try {
           URIReference predicateURI = (URIReference) session.globalize(((
               LocalNode) predicateElement).getValue());
           predicate = predicateURI.getURI().toString();
         } catch (ClassCastException ec) {
           throw new QueryException("Bad predicate in Lucene constraint", ec);
         }
       }
 
       // Validate and globalize object
       String object;
       ConstraintElement objectElement = constraint.getObject();
       try {
         LiteralImpl objectLiteral = (LiteralImpl) session.globalize(((LocalNode)
             objectElement).getValue());
         object = objectLiteral.getLexicalForm();
       } catch (ClassCastException e) {
         throw new QueryException("The object of any rdf:object statement in a mulgara:LuceneModel " +
                                  "must be a literal.", e);
       }
 
       // Get the score variable
       Variable score = constraint.getScoreVar();
       if (score != null) {
         variableList.add(score);
       }
 
       if (logger.isInfoEnabled()) {
         logger.info("Searching for " + subject + " : " + predicate + " : " + object);
       }
       // Initialize fields
       hits = fullTextStringIndex.find(subject, predicate, object);
       setVariables(variableList);
     } catch (GlobalizeException e) {
       throw new QueryException("Couldn't globalize constraint elements", e);
     } catch (FullTextStringIndexException e) {
       throw new QueryException("Couldn't generate answer from text index", e);
     }
   }
 
   //
   // Implementation of AbstractTuples methods
   //
 
   public void beforeFirst(long[] prefix,
       int suffixTruncation) throws TuplesException {
     document = null;
     nextDocumentIndex = 0;
   }
 
   public void close() throws TuplesException {
     try {
       hits.close();
     } catch (IOException ioe) {
       throw new TuplesException("Error closing fulltext index hits", ioe);
     }
   }
 
   public long getColumnValue(int column) throws TuplesException {
     try {
       if (column >= 0 && column < luceneKeyList.size()) {
         URI uri = new URI(document.get((String) luceneKeyList.get(column)));
         return session.localize(new URIReferenceImpl(uri));
       } else if (column == luceneKeyList.size()) {
         // Generate the score column
         return session.localize(new LiteralImpl(hits.score(nextDocumentIndex - 1)));
       } else {
         throw new TuplesException("Column " + column + " does not exist");
       }
     } catch (IOException e) {
       throw new TuplesException("Couldn't get column " + column + " value", e);
     } catch (LocalizeException e) {
       throw new TuplesException("Couldn't localize column " + column + " value",
           e);
     } catch (URISyntaxException e) {
       throw new TuplesException("Couldn't get column " + column + " value", e);
     }
   }
 
   public long getRowCount() throws TuplesException {
     if ((rowCount == -1) && (hits != null)) {
       rowCount = hits.length();
     }
 
     return rowCount;
   }
 
   public long getRowUpperBound() throws TuplesException {
     return getRowCount();
   }
 
   /**
    * Lucene never generates unbound columns.
    *
    * @return <code>false</code>
    */
   public boolean isColumnEverUnbound(int column) throws TuplesException {
     return false;
   }
 
   public boolean hasNoDuplicates() throws TuplesException {
     return false;
   }
 
   public List<Tuples> getOperands() {
     return Collections.<Tuples>emptyList();
   }
 
   public boolean next() throws TuplesException {
     try {
       if (nextDocumentIndex < getRowCount()) {
         document = hits.doc(nextDocumentIndex++);
         return true;
       } else {
         document = null;
         return false;
       }
     } catch (IOException e) {
       throw new TuplesException("Couldn't obtain next Lucene hit", e);
     }
   }
 
   public Constraint getConstraint() {
     return constraint;
   }
 
   //!!FIXME: I have no idea if this is correct.
   public boolean isComplete() {
     return false;
   }
 }
