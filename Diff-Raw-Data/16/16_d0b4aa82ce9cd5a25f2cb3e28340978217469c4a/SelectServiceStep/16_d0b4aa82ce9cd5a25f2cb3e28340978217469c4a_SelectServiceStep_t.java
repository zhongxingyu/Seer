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
 
 package org.openengsb.opencit.ui.web.WizardSteps;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.wicket.extensions.wizard.dynamic.DynamicWizardStep;
 import org.apache.wicket.extensions.wizard.dynamic.IDynamicWizardStep;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.IChoiceRenderer;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.ResourceModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.openengsb.core.common.Domain;
 import org.openengsb.core.common.ServiceManager;
 import org.openengsb.core.common.service.DomainService;
 import org.openengsb.opencit.core.projectmanager.model.Project;
 
 public class SelectServiceStep extends DynamicWizardStep {
 
     @SpringBean
     private DomainService domainService;
     private String serviceDescriptor;
 
    private Project project;
     private Map<String, ServiceManager> managersMap = new HashMap<String, ServiceManager>();
     private Class<? extends Domain> currentDomain;
 
 
     public SelectServiceStep(Project project, Class<? extends Domain> currentDomain) {
         super(new CreateProjectStep(project), new ResourceModel("selectService.title"),
             new ResourceModel("selectService.summary"), new Model<Project>(project));
         this.project = project;
         this.currentDomain = currentDomain;
         List<ServiceManager> serviceManagers = domainService.serviceManagersForDomain(currentDomain);
         DropDownChoice<String> descriptorDropDownChoice = initSCMDomains(serviceManagers,
             "serviceDescriptor");
         add(descriptorDropDownChoice);
     }
 
 
     private DropDownChoice<String> initSCMDomains(List<ServiceManager> managers, String dropDownID) {
         final List<String> descriptors = new ArrayList<String>();
 
         for (ServiceManager sm : managers) {
             managersMap.put(sm.getDescriptor().getName().getString(getLocale()), sm);
             descriptors.add(sm.getDescriptor().getName().getString(getLocale()));
         }
 
        if (descriptors.size() == 1) {
            serviceDescriptor = descriptors.get(0);
        }

         IModel<List<String>> dropDownModel = new LoadableDetachableModel<List<String>>() {
             @Override
             protected List<String> load() {
                 return descriptors;
             }
         };
 
         DropDownChoice<String> descriptorDropDownChoice = new DropDownChoice<String>(dropDownID,
             dropDownModel, new IChoiceRenderer<String>() {
 
                 public String getDisplayValue(String object) {
                     return object;
                 }
 
                 public String getIdValue(String object, int index) {
                     return object;
                 }
             }) {
 
             protected boolean wantOnSelectionChangedNotifications() {
                 return true;
             }
 
             @Override
             protected void onSelectionChanged(String newSelection) {
                 super.onSelectionChanged(newSelection);
                 serviceDescriptor = newSelection;
             }
         };
 
         return descriptorDropDownChoice;
     }
 
     @Override
     public boolean isLastStep() {
         return false;
     }
 
     @Override
     public IDynamicWizardStep next() {
         ServiceManager serviceManager = managersMap.get(serviceDescriptor);
         return new SetAttributesStep(project, serviceManager);
     }
 
     @Override
     public boolean isComplete() {
         if (serviceDescriptor != null) {
             return (managersMap.containsKey(serviceDescriptor));
         } else {
             return false;
         }
     }
 }
