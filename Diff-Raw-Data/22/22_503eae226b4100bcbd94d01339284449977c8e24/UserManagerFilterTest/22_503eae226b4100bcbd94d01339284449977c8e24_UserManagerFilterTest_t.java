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
 import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
 import org.bonitasoft.engine.bpm.process.ProcessDefinition;
 import org.bonitasoft.engine.bpm.process.ProcessInstance;
 import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
 import org.bonitasoft.engine.expression.ExpressionBuilder;
 import org.bonitasoft.engine.identity.User;
 import org.bonitasoft.engine.identity.UserCreator;
 import org.bonitasoft.engine.test.APITestUtil;
 import org.bonitasoft.engine.test.TestStates;
 import org.bonitasoft.engine.test.wait.WaitForStep;
 import org.bonitasoft.userfilter.identity.UserManagerUserFilter;
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * @author Emmanuel Duchastenier
  */
 public class UserManagerFilterTest extends APITestUtil {
 
     @Test
     public void testProvidedUserManagerFilter() throws Exception {
         final String qualityGuys = "Quality Guys";
         final String chiefName = "chief";
         final String subordinateName = "grouillot";
         final String activityName = "step1";
 
         login();
 
         final User chief = getIdentityAPI().createUser(chiefName, "bpm");
         final User grouillot = getIdentityAPI().createUser(
                 new UserCreator(subordinateName, "bpm").setManagerUserId(chief.getId()).setManagerUserId(chief.getId()));
 
         final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
         final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(
                 "test with Provided UserManager implem of UserFilter", "1.0");
         designProcessDefinition.addActor(qualityGuys);
         final String userFilterDefinitionId = "user-manager";
         designProcessDefinition.addUserTask(activityName, qualityGuys).addUserFilter("Filters the manager of the given user", userFilterDefinitionId, "1.0.0")
                 .addInput("userId", new ExpressionBuilder().createConstantLongExpression(grouillot.getId()))
                 .addInput("autoAssign", new ExpressionBuilder().createConstantBooleanExpression(true));
         businessArchiveBuilder.setProcessDefinition(designProcessDefinition.done());
 
         final InputStream inputStream = UserManagerUserFilter.class.getResourceAsStream("/user-manager-impl-1.0.0.impl");
         Assert.assertNotNull(inputStream);
 
         businessArchiveBuilder.addUserFilters(new BarResource("user-manager-impl-1.0.0.impl", IOUtils.toByteArray(inputStream)));
         inputStream.close();
 
         final ProcessDefinition definition = getProcessAPI().deploy(businessArchiveBuilder.done());
         addMappingOfActorsForUser(qualityGuys, chief.getId(), definition);
         addMappingOfActorsForUser(qualityGuys, grouillot.getId(), definition);
         getProcessAPI().enableProcess(definition.getId());
 
         logout();
         loginWith(subordinateName, "bpm");
 
         final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
 
         logout();
         loginWith(chiefName, "bpm");
 
        final WaitForStep task = waitForStep(30, 2000, activityName, processInstance, TestStates.getReadyState());
         Assert.assertEquals(chief.getId(), ((HumanTaskInstance) task.getResult()).getAssigneeId());
 
         disableAndDeleteProcess(definition);
         deleteUser(chief);
         deleteUser(grouillot);
         logout();
     }
 }
