 package org.agileinsider.concordion.command;
 
 /*
  * Copyright 2011 Mark Barnes (mark@agileinsider.org)
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 import org.agileinsider.concordion.event.*;
 
 import junit.framework.AssertionFailedError;
 import ognl.DefaultMemberAccess;
 import ognl.Ognl;
 import ognl.OgnlContext;
 import org.concordion.api.AbstractCommand;
 import org.concordion.api.CommandCall;
 import org.concordion.api.Evaluator;
 import org.concordion.api.ResultRecorder;
 import org.concordion.internal.OgnlEvaluator;
 import org.concordion.internal.util.Announcer;
 import org.junit.After;
 import org.junit.Before;
 
 public class ScenarioCommand extends AbstractCommand {
     private final Announcer<ScenarioListener> listeners = Announcer.to(ScenarioListener.class);
     private final MethodInvoker methodInvoker;
 
     public ScenarioCommand() {
         this(new MethodInvoker());
     }
 
     public ScenarioCommand(MethodInvoker methodInvoker) {
         this.methodInvoker = methodInvoker;
     }
 
     public void addScenarioListener(ScenarioListener listener) {
         listeners.addListener(listener);
     }
 
     @Override
     public void execute(CommandCall commandCall, Evaluator evaluator, ResultRecorder resultRecorder) {
         Object scenarioFixture = null;
         try {
             OgnlContext permissiveContext = new OgnlContext();
             permissiveContext.setMemberAccess(new DefaultMemberAccess(true));
             Object rootObject = Ognl.getValue("rootObject", permissiveContext, evaluator);
             scenarioFixture = rootObject.getClass().newInstance();
        } catch (Exception e) {
             throw new RuntimeException("Nasty hack exposed!!!", e);
         }
 
         String scenarioName = commandCall.getExpression();
         listeners.announce().scenarioStarted(new ScenarioStartEvent(scenarioName));
 
         ScenarioResultRecorder scenarioResultRecorder = new ScenarioResultRecorder();
 
         methodInvoker.invokeAnnotatedMethods(scenarioFixture, Before.class, scenarioResultRecorder);
 
         OgnlEvaluator scenarioEvaluator = new OgnlEvaluator(scenarioFixture);
 
         commandCall.getChildren().processSequentially(scenarioEvaluator, scenarioResultRecorder);
         if (scenarioResultRecorder.getExceptionCount() > 0) {
             listeners.announce().scenarioError(new ScenarioErrorEvent(scenarioName, commandCall.getElement(), new AssertionFailedError("Scenario has errors.")));
         } else if (scenarioResultRecorder.getFailureCount() > 0) {
             listeners.announce().failureReported(new ScenarioFailureEvent(scenarioName, commandCall.getElement()));
         } else {
             listeners.announce().successReported(new ScenarioSuccessEvent(scenarioName, commandCall.getElement()));
         }
 
         methodInvoker.invokeAnnotatedMethods(scenarioFixture, After.class, scenarioResultRecorder);
         listeners.announce().scenarioFinished(new ScenarioFinishEvent(scenarioName));
     }
 }
