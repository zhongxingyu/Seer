 /***************************************************************
  *  This file is part of the [fleXive](R) backend application.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) backend application is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/licenses/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.war.beans.admin.main;
 
 import com.flexive.faces.FxJsfUtils;
 import com.flexive.faces.messages.FxFacesMsgErr;
 import com.flexive.faces.messages.FxFacesMsgInfo;
 import com.flexive.shared.CacheAdmin;
 import static com.flexive.shared.EJBLookup.getWorkflowStepDefinitionEngine;
 import com.flexive.shared.value.FxString;
 import com.flexive.shared.workflow.StepDefinition;
 import com.flexive.shared.workflow.StepDefinitionEdit;
 
 import javax.faces.model.SelectItem;
 import java.util.List;
 import java.util.ArrayList;
 
 /**
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class StepDefinitionBean {
     private long stepDefinitionId = -1;
     private StepDefinitionEdit stepDefinition;
 
     /**
      * Step definition overview.
      *
      * @return the overview page
      */
     public String overview() {
         return "stepDefinitionOverview";
     }
 
     /**
      * Initialize the edit form (load selected step definition)
      *
      * @return the edit form
      */
     public String edit() {
         stepDefinition =CacheAdmin.getEnvironment().getStepDefinition(stepDefinitionId).asEditable();
         return "stepDefinitionEdit";
     }
 
     /**
      * Delete the selected step definition.
      *
      * @return the overview page
      */
     public String delete() {
         try {
             String name = CacheAdmin.getEnvironment().getStepDefinition(stepDefinitionId).getName();
             getWorkflowStepDefinitionEngine().remove(stepDefinitionId);
             new FxFacesMsgInfo("StepDefinition.nfo.deleted", name).addToContext();
         } catch (Exception e) {
             new FxFacesMsgErr(e).addToContext();
         }
         return overview();
     }
 
     /**
      * Create a new stepdefinition with the posted values.
      *
      * @return the overview page if creation was successful, or redisplay the edit form if it was not
      */
     public String create() {
         try {
             stepDefinitionId = getWorkflowStepDefinitionEngine().create(stepDefinition);
             new FxFacesMsgInfo("StepDefinition.nfo.created", stepDefinition.getName()).addToContext();
             return overview();
         } catch (Exception e) {
             new FxFacesMsgErr(e, "StepDefinition.err.create", e).addToContext();
             return "stepDefinitionEdit";
         }
     }
 
     /**
      * Save an existing step definition.
      *
      * @return overview page if the update was successful, otherwise redisplay the edit form
      */
     public String save() {
         try {
             getWorkflowStepDefinitionEngine().update(stepDefinition);
             new FxFacesMsgInfo("StepDefinition.nfo.updated", stepDefinition.getName()).addToContext();
             return overview();
         } catch (Exception e) {
             new FxFacesMsgErr(e).addToContext();
             return "stepDefinitionEdit";
         }
     }
 
     public List<StepDefinition> getList() {
         return CacheAdmin.getFilteredEnvironment().getStepDefinitions();
     }
 

     public long getStepDefinitionId() {
         return stepDefinitionId;
     }
 
     public void setStepDefinitionId(long stepDefinitionId) {
         this.stepDefinitionId = stepDefinitionId;
     }
 
     public StepDefinitionEdit getStepDefinition() {
         if (stepDefinition != null) {
             return stepDefinition;
         }
         stepDefinition = new StepDefinitionEdit(new StepDefinition(-1, new FxString(""), null, -1));
         return stepDefinition;
     }
 
     public void setStepDefinition(StepDefinitionEdit stepDefinition) {
         this.stepDefinition = stepDefinition;
     }
 
     public List<SelectItem> getStepDefinitions() {
         List<StepDefinition> sdList = CacheAdmin.getFilteredEnvironment().getStepDefinitions();
         List<StepDefinition> filteredList = new ArrayList<StepDefinition>(sdList.size());
         for (StepDefinition sd : sdList)
             if (sd.getId() != stepDefinition.getId()) {
                 filteredList.add(sd);
             }
        return FxJsfUtils.asIdSelectListWithLabel(filteredList);
     }
 }
