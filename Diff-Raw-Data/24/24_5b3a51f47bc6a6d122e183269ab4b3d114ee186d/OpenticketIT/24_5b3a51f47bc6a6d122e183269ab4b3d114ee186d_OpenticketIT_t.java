 /**
  * Copyright 2010 OpenEngSB Division, Vienna University of Technology
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.openengsb.openticket.integrationtest.exam;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.api.context.ContextHolder;
 import org.openengsb.core.api.workflow.TaskboxService;
 import org.openengsb.core.api.workflow.RuleManager;
 import org.openengsb.openticket.integrationtest.util.AbstractExamTestHelper;
 
 import org.ops4j.pax.exam.junit.JUnit4TestRunner;
 
 @RunWith(JUnit4TestRunner.class)
 public class OpenticketIT extends AbstractExamTestHelper {
     private TaskboxService taskboxService;
     private RuleManager ruleManager;
 
     @Before
     public void setUp() throws Exception {
         super.beforeClass();
 
         ContextCurrentService contextService = getOsgiService(ContextCurrentService.class);
         if (!contextService.getAvailableContexts().contains("openticket-it")) {
             contextService.createContext("openticket-it");
         }
        ContextHolder.get().setCurrentContextId("it-taskbox");
         ruleManager = getOsgiService(RuleManager.class);
         taskboxService = getOsgiService(TaskboxService.class);
     }
 
     @Test
     public void test_Name() {
 
     }
 }
