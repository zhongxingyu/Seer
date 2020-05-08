 /**
  * Copyright (C) 2012 BonitaSoft S.A.
  * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
 
 import java.io.InputStream;
 
 import org.apache.commons.io.IOUtils;
 import org.bonitasoft.engine.bpm.bar.BarResource;
 import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
 import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
 import org.bonitasoft.engine.bpm.flownode.UserTaskInstance;
 import org.bonitasoft.engine.bpm.process.ProcessDefinition;
 import org.bonitasoft.engine.bpm.process.ProcessInstance;
 import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
 import org.bonitasoft.engine.expression.ExpressionBuilder;
 import org.bonitasoft.engine.identity.User;
 import org.bonitasoft.engine.test.APITestUtil;
 import org.bonitasoft.engine.test.TestStates;
 import org.bonitasoft.engine.test.wait.WaitForStep;
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * @author Celine Souchet
  */
 public class SingleUserFilterTest extends APITestUtil {
 
     @Test
     public void testSingleUserFilter() throws Exception {
         final String qualityGuys = "Quality Guys";
         final String chiefName = "chief";
         final String grouillotName = "grouillot";
         final String activityName = "step1";
 
         login();
 
         final User chief = getIdentityAPI().createUser(chiefName, "bpm");
         final User grouillot = getIdentityAPI().createUser(grouillotName, "bpm");
 
         final ExpressionBuilder expressionBuilder = new ExpressionBuilder();
         final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(
                 "test with Provided UserManager implem of UserFilter", "1.0");
         designProcessDefinition.addActor(qualityGuys).addUserTask(activityName, qualityGuys).addUserFilter("Filters the single user", "single-user", "1.0.0")
                 .addInput("userId", expressionBuilder.createConstantLongExpression(grouillot.getId()))
                 .addInput("autoAssign", expressionBuilder.createConstantBooleanExpression(true));
 
         final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
         businessArchiveBuilder.setProcessDefinition(designProcessDefinition.done());
 
         final InputStream inputStream = SingleUserFilterTest.class.getResourceAsStream("/single-user-impl-1.0.0.impl");
         Assert.assertNotNull(inputStream);
 
         businessArchiveBuilder.addUserFilters(new BarResource("single-user-impl-1.0.0.impl", IOUtils.toByteArray(inputStream)));
         inputStream.close();
 
         final ProcessDefinition definition = getProcessAPI().deploy(businessArchiveBuilder.done());
         addMappingOfActorsForUser(qualityGuys, chief.getId(), definition);
         addMappingOfActorsForUser(qualityGuys, grouillot.getId(), definition);
         getProcessAPI().enableProcess(definition.getId());
 
         logout();
         loginWith(grouillotName, "bpm");
 
         final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
 
         logout();
         loginWith(chiefName, "bpm");
 
        final WaitForStep waitForStep = waitForStep(30, 2000, activityName, processInstance, TestStates.getReadyState(null));
         Assert.assertTrue(waitForStep.waitUntil());
         final ActivityInstance task = waitForStep.getResult();
         Assert.assertEquals(grouillot.getId(), ((UserTaskInstance) task).getAssigneeId());
 
         disableAndDeleteProcess(definition);
         deleteUser(chief);
         deleteUser(grouillot);
         logout();
     }
 
 }
