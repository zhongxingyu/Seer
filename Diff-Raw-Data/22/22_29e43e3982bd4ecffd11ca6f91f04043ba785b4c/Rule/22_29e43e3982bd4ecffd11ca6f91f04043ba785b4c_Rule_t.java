 /*
  * Copyright 2010 the original author or authors.
  * Copyright 2009 Paxxis Technology LLC
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.paxxis.cornerstone.scripting;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 
 /**
  * This class represents a rule.  It is normally
  * populated by calling the rule parser, although it
  * can be populated by calling the methods directly.
  *
  * @author Robert Englander
  */
 public class Rule implements Serializable {
     private static final long serialVersionUID = 1L;
 
     // the name of the rule
     private String name = null;
 
     // the description
     private String description = "";
     
     // the conditions for firing can be boolean
     // conditions.
     private List<IValue> booleanConditions = new ArrayList<IValue>();
 
     // the instruction queue to run when this rule fires
     private InstructionQueue queue = new InstructionQueue();
 
     // the instruction queue to run when this rule fails
     private InstructionQueue errorQueue = new InstructionQueue();
 
     // the variable that holds the value that
     // is returned from invoke, if the rule is
     // defined to have a return value.
     private IValue returnValue = null;
 
     // the rule set that this rule belongs to
     private RuleSet ruleSet = null;
 
     public Rule() {
 
     }
     
     public Rule(RuleSet set) {
         this.ruleSet = set;
         queue.setRuleSet(ruleSet);
     	List<ReferenceVariable> refs = set.getReferenceVariables();
         for (ReferenceVariable var : refs) {
             try {
 		queue.addVariable(var);
             } catch (RuleCreationException e) {
         	// FIXME why are we eating this exception?
             }
         }
 
         errorQueue.setRuleSet(ruleSet);
     }
 
     /**
      * Determines if there is at least 1 return statement
      * within the entire rule.  This is provided as support
      * for tools that help the user to write good rules,
      * but does not ensure that the return statements
      * can ever be reached.  The existence of a return
      * statement is not a requirement when a return option
      * is declared.  If the rule execution ends without
      * reaching a return statement, the default value
      * of the declared return data type will be returned.
      * @return true if a return statement exists. false otherwise
      */
     public boolean hasReturnStatement() {
         return queue.hasReturnInstruction();
     }
 
     public boolean hasReturnValue() {
         return (returnValue != null);
     }
 
     public void setReturnValue(IValue rv) {
         returnValue = rv.evaluate();
     }
 
     public IValue getReturnValue() {
         return returnValue;
     }
 
     public boolean hasConditionals() {
         return (booleanConditions.size() > 0);
     }
 
     public void addCondition(IValue cnd) {
         booleanConditions.add(cnd);
     }
 
     public boolean evaluateConditionals() {
         for (IValue cond : booleanConditions) {
             if (cond.valueAsBoolean() == false) {
                 return false;
             }
         }
 
         return true;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getName() {
         return name;
     }
 
     public void setDescription(String desc) {
 	this.description = desc;
     }
     
     public String getDescription() {
 	return description;
     }
     
     public InstructionQueue getQueue() {
         return queue;
     }
 
     public InstructionQueue getErrorQueue() {
         return errorQueue;
     }
 
     /**
      * Process the rule using the parameter values supplied
      * in params.  params is an array of RuleVariables
      * whose names must match the parameters in the rule definition.
      * @param params
      * @return
      */
     public IValue invoke(List<IValue> params) {
         process(params);
 
         IValue result = null;
         if (hasReturnValue()) {
             result = getReturnValue();
         }
 
         return result;
     }
 
     public boolean process(List<IValue> params) {
         // tell the rule set that this is the current
         // rule. remember the previous rule so we can
         // set it back as the current when we're done
         Rule prevRule = ruleSet.getCurrentRule();
         ruleSet.setCurrentRule(this);
 
         try {
             // tell the monitor that we're about to
             // start running a rule
             ruleSet.getRuntime().ruleBoundary(this, true);
 
             // setup the parameters
             if (params != null) {
                 queue.setVariableValues(params);
             }
 
            IValue returnVal = getReturnValue();
            if (returnVal != null && returnVal instanceof RuleVariable) {
            	RuleVariable rv = (RuleVariable)returnVal;
            	rv.reset();
            }
            
             // run it
             boolean result = queue.process(queue);
 
             // tell the monitor that we've finished running the rule
             ruleSet.getRuntime().ruleBoundary(this, false);
             ruleSet.setCurrentRule(prevRule);
             return result;
         } catch (Exception rte) {
             StringBuilder buffer = new StringBuilder();
             buffer.append("Error while processing script: ")
        		.append(ruleSet.getName())
        		.append("\nMethod Name: ")
        		.append(ruleSet.getCurrentRule().getName())
        		.append("\nError occurred on line number: " + ruleSet.getCurrentInstruction().getLineNumber())
        		.append("\n" + rte.getMessage());
 
             if (errorQueue.isEmpty()) {
 		throw new RuntimeException(buffer.toString());
             } else { // process the error block instructions
 		errorQueue.setRuleSet(ruleSet);
 		errorQueue.process(queue);
 
 		// tell the monitor that we've finished running the rule
 		ruleSet.getRuntime().ruleBoundary(this, false);
 
 		ruleSet.setCurrentRule(prevRule);
 		return false;
             }
         }
     }
 
     public List<RuleVariable> getParameters() {
 	List<RuleVariable> params = new ArrayList<RuleVariable>();
 	HashMap<Integer, RuleVariable> p = queue.getParameters();
 	int cnt = p.size();
 	for (int i = 0; i < cnt; i++) {
 	    RuleVariable rv = p.get(i);
 	    params.add(rv);
 	}
 	
 	return params;
     }
 
 	public Object getParameterDefault(String varName) {
 		RuleVariable rv = queue.getVariable(varName);
 		String result = rv.getDefaultValue();
 		return result;
 	}
 }
