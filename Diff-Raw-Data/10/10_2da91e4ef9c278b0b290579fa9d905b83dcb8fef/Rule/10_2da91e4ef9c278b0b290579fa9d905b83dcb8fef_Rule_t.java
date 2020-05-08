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
 import java.io.Serializable;
 import java.util.*;
 
 // Third party packages
 import org.apache.log4j.Logger;
 
 // Locally written packages
 import org.mulgara.query.*;
 import org.mulgara.resolver.OperationContext;
 import org.mulgara.resolver.spi.LocalizedTuples;
 import org.mulgara.resolver.spi.Resolver;
 import org.mulgara.resolver.spi.ResolverException;
 import org.mulgara.resolver.spi.SystemResolver;
 import org.mulgara.resolver.spi.TuplesWrapperStatements;
 import static org.mulgara.krule.RuleStructure.UNINITIALIZED;
 
 /**
  * Represents a single executable rule.
  *
  * @created 2005-5-16
  * @author <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
  * @version $Revision: 1.2 $
  * @modified $Date: 2005/06/30 01:12:28 $
  * @maintenanceAuthor $Author: pgearon $
  * @copyright &copy; 2005 <a href="mailto:pgearon@users.sorceforge.net">Paul Gearon</a>
  * @licence <a href="{@docRoot}/../../LICENCE">Mozilla Public License v1.1</a>
  */
 public class Rule implements Serializable {
 
   private static final long serialVersionUID = 3080424724378196982L;
 
   /** Logger.  */
   private static Logger logger = Logger.getLogger(Rule.class.getName());
 
   /** The name of this rule. */
   private String name;
 
   /** The rules to be triggered when this rule generates statements.*/
   private Set<Rule> triggerSet;
 
   /** The query for this rule. This contains the information for the base model. */
   private Query query;
 
   /** The graph receiving the inferred data. */
   private long targetGraph = UNINITIALIZED;
 
   /** The most recent size of the data matching this rule. */
   private long lastCount;
 
   /** The structure containing this rule */
   private RuleStructure ruleStruct;
 
   // TODO: Change this to a map of constraints to longs
 
   /**
    * Principle constructor.
    *
    * @param name The name of the rule.
    */
   public Rule(String name) {
     triggerSet = new HashSet<Rule>();
     this.name = name;
   }
 
 
   /**
    * Gets the name of the rule.
    *
    * @return The rule name.
    */
   public String getName() {
     return name;
   }
 
 
   /**
    * Adds a target for triggering.
    *
    * @param target The rule to be triggered when this rule is executed.
    */
   public void addTriggerTarget(Rule target) {
     triggerSet.add(target);
   }
 
 
   /**
    * Set the target graph for the rule.
    *
    * @param target The URI of the model to insert inferences into.
    */
   public void setTargetGraph(long target) {
     targetGraph = target;
   }
 
 
   /**
    * Sets the query for this rule.
    *
    * @param queryStruct The query which retrieves data for this rule.
    */
   public void setQueryStruct(QueryStruct queryStruct) {
     this.query = queryStruct.extractQuery();
   }
 
 
   /**
    * Retrieves the query from this rule.
    *
    * @return The query which retrieves data for this rule.
    */
   public Query getQuery() {
     return query;
   }
 
 
   /**
    * Retrieves the list of subordinate rules.
    *
    * @return an immutable set of the subordinate rules.
    */
   public Set<Rule> getTriggerTargets() {
     return Collections.unmodifiableSet(triggerSet);
   }
 
 
   /**
    * Sets the rule system structure containing this rule.
    * 
    * @param ruleStruct The structure for this rule.
    */
   public void setRuleStruct(RuleStructure ruleStruct) {
     this.ruleStruct = ruleStruct;
   }
 
 
   /**
    * Runs this rule.
    * TODO: count the size of each individual constraint
    * TODO: Go back to using a Session once they have been properly refactored for transactions
    * 
    * @param context The context to query against.
    * @param resolver The resolver to add data with.
    * @param sysResolver The resolver to localize data with.
    */
   public void execute(OperationContext context, Resolver resolver, SystemResolver sysResolver) throws QueryException, TuplesException, ResolverException {
     if (targetGraph == UNINITIALIZED) throw new IllegalStateException("Target graph has not been set");
     // see if this rule needs to be run
     Answer answer = null;
     try {
      answer = context.doQuery(query);
    } catch (Exception e) {
      throw new QueryException("Unable to access data in rule.", e);
    }
    try {
       // compare the size of the result data
       long newCount = answer.getRowCount();
       if (newCount == lastCount) {
         logger.debug("Rule <" + name + "> is up to date.");
         // this rule does not need to be run
         return;
       }
       logger.debug("Rule <" + name + "> has increased by " + (newCount - lastCount) + " entries");
       logger.debug("Inserting results of: " + query);
       if (answer instanceof AnswerImpl) {
         AnswerImpl a = (AnswerImpl)answer;
         String list = "[ ";
         Variable[] v = a.getVariables();
         for (int i = 0; i < v.length; i++) {
           list += v[i] + " ";
         }
         list += "]";
         logger.debug("query has " + a.getNumberOfVariables() + " variables: " + list);
       }
       // insert the resulting data
       insertData(answer, resolver, sysResolver);
       // update the count
       lastCount = newCount;
       logger.debug("Insertion complete, triggering rules for scheduling.");
     } finally {
       answer.close();
     }
     // trigger subsequent rules
     scheduleTriggeredRules();
   }
 
 
   /**
    * Schedule subsequent rules.
    */
   private void scheduleTriggeredRules() {
     Iterator<Rule> it = triggerSet.iterator();
     while (it.hasNext()) ruleStruct.schedule(it.next());
   }
 
 
   /**
    * Inserts an Answer into the data store on the current transaction.
    * @param answer The data to be inserted.
    * @param resolver The mechanism for adding data in the current transaction.
    * @param sysResolver Used for localizing the globalized data in the answer parameter.
    *        TODO: use a localized Tuples instead of Answer when src and dest are on the same server.
    * @throws TuplesException There was an error localizing the answer.
    * @throws ResolverException There was an error inserting the data.
    */
   private void insertData(Answer answer, Resolver resolver, SystemResolver sysResolver) throws TuplesException, ResolverException {
     TuplesWrapperStatements statements = convertToStatements(answer, sysResolver);
     try {
       resolver.modifyModel(targetGraph, statements, true);
     } finally {
       statements.close();
     }
   }
 
 
   /**
    * Converts an Answer with 3 selection values to a set of statements for insertion.
    * @param answer The answer to convert.
    * @param resolver The resolver used for localizing the results, since Answers are globalized.
    *        TODO: remove this round trip of local->global->local.
    * @return A set of Statements.
    * @throws TuplesException The statements could not be instantiated.
    */
   private TuplesWrapperStatements convertToStatements(Answer answer, SystemResolver resolver) throws TuplesException {
     Variable[] vars = answer.getVariables();
     assert vars.length == 3;
     return new TuplesWrapperStatements(new LocalizedTuples(resolver, answer, true), vars[0], vars[1], vars[2]);
   }
 }
