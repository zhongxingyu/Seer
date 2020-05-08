 /*
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 
package org.openmrs.module.emr.reporting.cohort.definition.evaluator;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.openmrs.Cohort;
 import org.openmrs.api.ConceptService;
 import org.openmrs.api.LocationService;
 import org.openmrs.api.UserService;
 import org.openmrs.module.emr.reporting.cohort.definition.VisitCohortDefinition;
 import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
 import org.openmrs.module.reporting.common.DateUtil;
 import org.openmrs.test.BaseModuleContextSensitiveTest;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import static java.util.Arrays.asList;
 import static org.hamcrest.Matchers.not;
 import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 /**
  *
  */
 public class VisitCohortDefinitionEvaluatorTest extends BaseModuleContextSensitiveTest {
 
     VisitCohortDefinition cd;
 
     @Autowired
     LocationService locationService;
 
     @Autowired
     ConceptService conceptService;
 
     @Autowired
     UserService userService;
 
     @Autowired
     CohortDefinitionService cohortDefinitionService;
 
     @Before
     public void setUp() throws Exception {
         cd = new VisitCohortDefinition();
     }
 
     @Test
     public void testEvaluateWithNoProperties() throws Exception {
         Cohort c = cohortDefinitionService.evaluate(cd, null);
         assertThat(c.size(), is(2));
     }
 
     @Test
     public void testEvaluateWithManyProperties() throws Exception {
         setManyProperties();
 
         Cohort c = cohortDefinitionService.evaluate(cd, null);
         assertThat(c.size(), is(1));
         assertThat(c.getMemberIds(), containsInAnyOrder(2));
     }
 
     @Test
     public void testEvaluateInverse() throws Exception {
         setManyProperties();
         cd.setReturnInverse(true);
 
         Cohort c = cohortDefinitionService.evaluate(cd, null);
         assertThat(c.size(), is(3));
         assertThat(c.getMemberIds(), not(containsInAnyOrder(2)));
     }
 
     private void setManyProperties() {
         cd.setStartedOnOrAfter(DateUtil.parseDate("2005-01-01", "yyyy-MM-dd"));
         cd.setStartedOnOrBefore(DateUtil.parseDate("2005-01-01", "yyyy-MM-dd"));
 
         cd.setLocationList(asList(locationService.getLocation(1)));
         cd.setIndicationList(asList(conceptService.getConcept(5497)));
 
         cd.setCreatedBy(userService.getUser(1));
         cd.setCreatedOnOrAfter(DateUtil.parseDate("2005-01-01", "yyyy-MM-dd"));
         cd.setCreatedOnOrBefore(DateUtil.parseDate("2005-01-01", "yyyy-MM-dd"));
     }
 }
