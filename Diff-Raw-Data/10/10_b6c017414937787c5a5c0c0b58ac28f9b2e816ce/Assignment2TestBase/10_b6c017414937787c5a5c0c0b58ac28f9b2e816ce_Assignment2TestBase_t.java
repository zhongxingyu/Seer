 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2007 The Sakai Foundation.
  * 
  * Licensed under the Educational Community License, Version 1.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at
  * 
  *      http://www.opensource.org/licenses/ecl1.php
  * 
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  *
  **********************************************************************************/
 package org.sakaiproject.assignment2.logic.test;
 
 
 import org.sakaiproject.assignment2.dao.AssignmentDao;
 import org.sakaiproject.assignment2.logic.AssignmentBundleLogic;
 import org.sakaiproject.assignment2.logic.ExternalAnnouncementLogic;
 import org.sakaiproject.assignment2.logic.impl.AssignmentLogicImpl;
 import org.sakaiproject.assignment2.logic.impl.AssignmentPermissionLogicImpl;
 import org.sakaiproject.assignment2.logic.impl.AssignmentSubmissionLogicImpl;
 import org.sakaiproject.assignment2.logic.impl.ExternalAnnouncementLogicImpl;
import org.sakaiproject.assignment2.logic.impl.ExternalContentReviewLogicImpl;
 import org.sakaiproject.assignment2.logic.impl.ExternalGradebookLogicImpl;
 import org.sakaiproject.assignment2.logic.test.stubs.ExternalLogicStub;
 import org.sakaiproject.assignment2.logic.test.stubs.GradebookServiceStub;
 import org.sakaiproject.assignment2.test.AssignmentTestDataLoad;
 import org.sakaiproject.assignment2.test.PreloadTestData;
 import org.springframework.test.AbstractTransactionalSpringContextTests;
 
 
 /**
  * Base class for assignment test classes that provides the spring application
  * context.  The database used is an in-memory hsqldb by default, but this can
  * be overridden to test specific database configurations by setting the "mem"
  * system property to "false".  In the "mem=false" case, the database configuration
  * is set in the hibernate.properties file in the "hibernate.properties.dir" directory.
  *
  */
 public abstract class Assignment2TestBase extends AbstractTransactionalSpringContextTests {
     protected AssignmentDao dao;
     protected AssignmentTestDataLoad testData;
 
     protected ExternalAnnouncementLogic announcementLogic;
 
     protected ExternalGradebookLogicImpl gradebookLogic;
     protected AssignmentLogicImpl assignmentLogic;
     protected AssignmentSubmissionLogicImpl submissionLogic;
     protected AssignmentPermissionLogicImpl permissionLogic;
     protected AssignmentBundleLogic bundleLogic;
    protected ExternalContentReviewLogicImpl contentReviewLogic;
 
     protected GradebookServiceStub gradebookService;
 
     protected ExternalLogicStub externalLogic;
 
     protected void onSetUpBeforeTransaction() throws Exception {
 
     }
     protected void onSetUpInTransaction() throws Exception {
         PreloadTestData ptd = (PreloadTestData) applicationContext.getBean("org.sakaiproject.assignment2.test.PreloadTestData");
         if (ptd == null) {
             throw new NullPointerException("PreloadTestData could not be retrieved from spring");
         }
 
         testData = ptd.getAtdl();
 
         dao = (AssignmentDao)applicationContext.getBean("org.sakaiproject.assignment2.dao.AssignmentDao");
         if (dao == null) {
             throw new NullPointerException(
             "DAO could not be retrieved from spring");
         }
 
         bundleLogic = (AssignmentBundleLogic)applicationContext.getBean("org.sakaiproject.assignment2.logic.AssignmentBundleLogic");
         if (bundleLogic == null) {
             throw new NullPointerException(
             "bundleLogic could not be retrieved from spring");
         } 
 
         externalLogic = new ExternalLogicStub();
 
         gradebookService = new GradebookServiceStub();
         gradebookService.setExternalLogic(externalLogic);
 
         gradebookLogic = new ExternalGradebookLogicImpl();
         gradebookLogic.setGradebookService(gradebookService);
         gradebookLogic.setExternalLogic(externalLogic);
 
         announcementLogic = new ExternalAnnouncementLogicImpl();
        
        contentReviewLogic = new ExternalContentReviewLogicImpl();
        
         permissionLogic = new AssignmentPermissionLogicImpl();
         permissionLogic.setDao(dao);
         permissionLogic.setExternalGradebookLogic(gradebookLogic);
         permissionLogic.setExternalLogic(externalLogic);
 
         assignmentLogic = new AssignmentLogicImpl();
         assignmentLogic.setDao(dao);
         assignmentLogic.setExternalAnnouncementLogic(announcementLogic);
         assignmentLogic.setExternalGradebookLogic(gradebookLogic);
         assignmentLogic.setExternalLogic(externalLogic);
         assignmentLogic.setPermissionLogic(permissionLogic);
         assignmentLogic.setAssignmentBundleLogic(bundleLogic);
        assignmentLogic.setExternalContentReviewLogic(contentReviewLogic);
 
 
         submissionLogic = new AssignmentSubmissionLogicImpl();
         submissionLogic.setDao(dao);
         submissionLogic.setExternalGradebookLogic(gradebookLogic);
         submissionLogic.setExternalLogic(externalLogic);
         submissionLogic.setPermissionLogic(permissionLogic);
         submissionLogic.setAssignmentLogic(assignmentLogic);
 
     }
 
     /**
      * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
      */
     protected String[] getConfigLocations() {
         // point to the needed spring config files, must be on the classpath
         // (add component/src/webapp/WEB-INF to the build path in Eclipse),
         // they also need to be referenced in the pom.xml file
         return new String[] { "hibernate-test.xml", "spring-hibernate.xml",
                 "logic-beans-test.xml"
 
         };
 
     }
 
 }
