 /*
  * Copyright 2009 Google Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.jstestdriver.coverage;
 
 import java.util.List;
 
 import com.google.common.collect.Lists;
 import com.google.jstestdriver.Action;
 import com.google.jstestdriver.DefaultActionListProvider;
 
 import junit.framework.TestCase;
 
 /**
  * @author corysmith@google.com (Cory Smith)
  *
  */
 public class CoverageActionListProviderTest extends TestCase {
 
   public void testGet() throws Exception {
     ActionStub action = new ActionStub();
     List<Action> list = new CoverageActionListProvider(new StubProvider(action), null, null).get();
     assertSame(action, list.get(0));
     assertTrue(list.get(1) instanceof CoverageReporterAction);
   }
   
   class StubProvider extends DefaultActionListProvider {
     private final ActionStub action;
 
     public StubProvider(ActionStub action) {
       super(null, null, null, null, null, false, null, false, -1, null, null, null, null, null,
          null, null, null, null);
       this.action = action;
     }
 
     @Override
     public List<Action> get() {
       return Lists.<Action>newArrayList(action);
     }
   }
   
   class ActionStub implements Action {
     public void run() {}
   }
 }
