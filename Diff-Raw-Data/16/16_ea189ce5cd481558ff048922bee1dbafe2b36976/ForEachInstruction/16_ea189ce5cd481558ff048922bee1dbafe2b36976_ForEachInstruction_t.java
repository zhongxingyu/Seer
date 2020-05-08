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
 
 /**
  * @author Robert Englander
  */
 public class ForEachInstruction extends Instruction {
     
     private static final long serialVersionUID = 1L;
 
     /** the instruction queue */
     private InstructionQueue block = new InstructionQueue();
 
     /** the incrementing index variable */
     private IntegerVariable indexVariable = null;
     
     /** the range values */
     private IValue indexStartValue = null;
     private IValue indexEndValue = null;
     private boolean increasing = true;
     
     public ForEachInstruction() {
     }
     
     public ForEachInstruction(InstructionQueue queue) {
         block.setRuleSet(queue.getRuleSet());
     }
 
     public void setIndexVariable(IntegerVariable indexVariable) throws RuleCreationException {
         this.indexVariable = indexVariable;
         block.addVariable(indexVariable);
     }
 
     public void setIndexStartValue(IValue start) {
 	indexStartValue = start;
     }
     
     public void setIndexEndValue(IValue end) {
 	indexEndValue = end;
     }
     
     @Override
     public boolean hasReturnStatement() {
         return block.hasReturnInstruction();
     }
 
     public InstructionQueue getBlock() {
         return block;
     }
 
     public boolean process(InstructionQueue queue) {
	increasing = indexEndValue.valueAsInteger() >= indexStartValue.valueAsInteger();

	// set the rule set
         block.setRuleSet(queue.getRuleSet());
 
         // foreach instructions want the break
         block.setWantsBreak(true);
 
         // clear the break before we start
         block.setBreak(false);
 
         boolean result = true;
         
         // push this context onto the monitor stack
         block.getRuleSet().getRuntime().push(block);
 
         indexVariable.setValueUnchecked(indexStartValue.valueAsInteger());
         
         int incr = (increasing ? 1 : -1);
         boolean done;
         do {
             if (!block.process(block)) {
                 result = block.getBreak();
                 break;
             }
 
             block.getRuleSet().setCurrentInstruction(this);
             block.getRuleSet().getRuntime().setPoised();
 
       	    indexVariable.setValueUnchecked(indexVariable.valueAsInteger() + incr);
       	    if (increasing) {
       		done = indexVariable.valueAsInteger() > indexEndValue.valueAsInteger();
       	    } else {
       		done = indexVariable.valueAsInteger() < indexEndValue.valueAsInteger();
       	    }
         } while (!done);
 
         block.getRuleSet().getRuntime().pop(block);
         return result;
     }
 }
 
