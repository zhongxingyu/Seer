 /**
  * Copyright (C) 2013 BonitaSoft S.A.
  * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
  * This library is free software; you can redistribute it and/or modify it under the terms
  * of the GNU Lesser General Public License as published by the Free Software Foundation
  * version 2.1 of the License.
  * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public License along with this
  * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
  * Floor, Boston, MA 02110-1301, USA.
  **/
 package org.bonitasoft.userfilter.test;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.InputStream;
 
 import org.apache.commons.io.IOUtils;
 import org.bonitasoft.engine.bpm.bar.BarResource;
 import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
 import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
 import org.bonitasoft.engine.bpm.process.ProcessDefinition;
 import org.bonitasoft.engine.bpm.process.ProcessInstance;
 import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
 import org.bonitasoft.engine.expression.ExpressionBuilder;
 import org.bonitasoft.engine.identity.User;
 import org.bonitasoft.engine.test.APITestUtil;
 import org.bonitasoft.engine.test.TestStates;
 import org.bonitasoft.userfilter.identity.UserManagerUserFilter;
 import org.junit.Test;
 
 /**
  * @author Emmanuel Duchastenier
  */
 public class SameTaskUserFilterTest extends APITestUtil {
 
     @Test
     public void testSameTaskUserFilter() throws Exception {
         final String qualityGuys = "Quality Guys";
         final String devName = "aDeveloper";
 
         login();
         final User aDev = getIdentityAPI().createUser(devName, "bpm");
         logoutThenloginAs(devName, "bpm");
 
         final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(
                 "test with Provided SameTask User implem of UserFilter", "1.0");
         designProcessDefinition.addActor(qualityGuys);
         final String userFilterDefinitionId = "same-task-user";
         final String task1Name = "step1";
         designProcessDefinition.addUserTask(task1Name, qualityGuys);
         final String task2Name = "step2";
         designProcessDefinition.addUserTask(task2Name, qualityGuys).addUserFilter("Filters the executor of a previous task", userFilterDefinitionId, "1.0.0")
                 .addInput("usertaskName", new ExpressionBuilder().createConstantStringExpression(task1Name))
                 .addInput("autoAssign", new ExpressionBuilder().createConstantBooleanExpression(true));
         designProcessDefinition.addTransition(task1Name, task2Name);
         final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
         businessArchiveBuilder.setProcessDefinition(designProcessDefinition.done());
 
         final InputStream inputStream = UserManagerUserFilter.class.getResourceAsStream("/same-task-user-impl-1.0.0.impl");
         businessArchiveBuilder.addUserFilters(new BarResource("same-task-user-impl-1.0.0.impl", IOUtils.toByteArray(inputStream)));
         inputStream.close();
 
         final ProcessDefinition definition = getProcessAPI().deploy(businessArchiveBuilder.done());
         addMappingOfActorsForUser(qualityGuys, aDev.getId(), definition);
         final long processDefinitionId = definition.getId();
         getProcessAPI().enableProcess(processDefinitionId);
 
         final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinitionId);
 
        final HumanTaskInstance task1 = (HumanTaskInstance) waitForTaskInState(task1Name, processInstance, TestStates.getReadyState(null));
         final long userId = aDev.getId();
         assignAndExecuteStep(task1, userId);
 
        final HumanTaskInstance task2 = (HumanTaskInstance) waitForTaskInState(task2Name, processInstance, TestStates.getReadyState(null));
         assertEquals(aDev.getId(), task2.getAssigneeId());
        assertEquals(TestStates.getReadyState(null), task2.getState());
         logout();
 
         login();
         disableAndDeleteProcess(definition);
         deleteUser(aDev);
         logout();
     }
 }
