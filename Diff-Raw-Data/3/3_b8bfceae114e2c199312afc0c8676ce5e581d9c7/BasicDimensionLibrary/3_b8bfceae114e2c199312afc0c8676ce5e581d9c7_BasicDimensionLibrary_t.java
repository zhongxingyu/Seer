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
 
 package org.openmrs.module.emr.reporting.library;
 
 import org.openmrs.annotation.Handler;
 import org.openmrs.module.emr.reporting.BaseDefinitionLibrary;
 import org.openmrs.module.emr.reporting.DocumentedDefinition;
 import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
 import org.openmrs.module.reporting.evaluation.parameter.Parameter;
 import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
 import org.openmrs.module.reporting.indicator.dimension.Dimension;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.util.Date;
 
 /**
  *
  */
 @Handler(supports = Dimension.class)
 public class BasicDimensionLibrary extends BaseDefinitionLibrary<Dimension> {
 
     public static final String PREFIX = "emr.dimension.";
 
     @Autowired
     CohortDefinitionService cohortDefinitionService;
 
    public BasicDimensionLibrary() {
    }

     /**
      * Used for tests
      * @param cohortDefinitionService
      */
     public BasicDimensionLibrary(CohortDefinitionService cohortDefinitionService) {
         this.cohortDefinitionService = cohortDefinitionService;
     }
 
     @Override
     public String getUuidPrefix() {
         return PREFIX;
     }
 
     @DocumentedDefinition(value = "gender", definition = "males | females")
     public CohortDefinitionDimension getGenderDimension() {
         CohortDefinitionDimension gender = new CohortDefinitionDimension();
         gender.addCohortDefinition("female", map(cohortDefinitionService.getDefinitionByUuid(BasicCohortDefinitionLibrary.PREFIX + "females"), ""));
         gender.addCohortDefinition("male", map(cohortDefinitionService.getDefinitionByUuid(BasicCohortDefinitionLibrary.PREFIX + "males"), ""));
         return gender;
     }
 
     @DocumentedDefinition(value = "age two levels (cutoff in years)", definition = "young = < $cutoff years , old = >= $cutoff years, age taken on $date")
     public CohortDefinitionDimension getTwoLevelAgeDimensionInYears() {
         CohortDefinitionDimension age = new CohortDefinitionDimension();
         age.addParameter(new Parameter("date", "Date", Date.class));
         age.addParameter(new Parameter("cutoff", "Cutoff (< $cutoff years , >= $cutoff years)", Integer.class));
         age.addCohortDefinition("young",
                 map(cohortDefinitionService.getDefinitionByUuid(BasicCohortDefinitionLibrary.PREFIX + "up to age on date"),
                         "maxAge=${cutoff-1},effectiveDate=${date}"));
         age.addCohortDefinition("old",
                 map(cohortDefinitionService.getDefinitionByUuid(BasicCohortDefinitionLibrary.PREFIX + "at least age on date"),
                         "minAge=${cutoff},effectiveDate=${date}"));
         return age;
     }
 
 }
