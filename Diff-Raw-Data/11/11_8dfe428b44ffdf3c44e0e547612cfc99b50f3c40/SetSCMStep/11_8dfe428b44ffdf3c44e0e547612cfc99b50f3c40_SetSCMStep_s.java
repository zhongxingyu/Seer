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
 import org.openengsb.core.common.ServiceManager;
 import org.openengsb.core.common.descriptor.ServiceDescriptor;
 import org.openengsb.core.common.service.DomainService;
 import org.openengsb.domains.scm.ScmDomain;
 import org.openengsb.opencit.core.projectmanager.model.Project;
 
 public class SetSCMStep extends DynamicWizardStep {
 
     @SpringBean
     private DomainService domainService;
 
     Project project;
    private Map<ServiceDescriptor, ServiceManager> managersMap= new HashMap<ServiceDescriptor, ServiceManager>();
 
 
     public SetSCMStep(Project project) {
         super(new CreateProjectStep(project), new ResourceModel("setUpSCM.title"),
             new ResourceModel("setUpSCM.summary"), new Model<Project>(project));
         this.project = project;
         List<ServiceManager> manager = domainService.serviceManagersForDomain(ScmDomain.class);
         DropDownChoice<ServiceDescriptor> descriptorDropDownChoice = initSCMDomains(manager, "project.scmDescriptor");
         add(descriptorDropDownChoice);
     }
 
     private DropDownChoice<ServiceDescriptor> initSCMDomains(List<ServiceManager> managers, String dropDownID) {
         final List<ServiceDescriptor> descritors = new ArrayList<ServiceDescriptor>();
 
         for (ServiceManager sm : managers) {
            managersMap.put(sm.getDescriptor(), sm);
             descritors.add(sm.getDescriptor());
         }
 
         IModel<List<ServiceDescriptor>> dropDownModel = new LoadableDetachableModel<List<ServiceDescriptor>>() {
             @Override
             protected List<ServiceDescriptor> load() {
                 return descritors;
             }
         };
 
 
         DropDownChoice<ServiceDescriptor> descriptorDropDownChoice = new DropDownChoice<ServiceDescriptor>(dropDownID,
             dropDownModel, new IChoiceRenderer<ServiceDescriptor>() {
 
                 public String getDisplayValue(ServiceDescriptor object) {
                    return object.getName().toString();
                 }
 
                 public String getIdValue(ServiceDescriptor object, int index) {
                     return object.getImplementationType().getSimpleName();
                 }
 
 
             });
 
         return descriptorDropDownChoice;
     }
 
     @Override
     public boolean isLastStep() {
         return false;
     }
 
     @Override
     public IDynamicWizardStep next() {
        ServiceManager serviceManager = managersMap.get(project.getScmDescriptor());
         return new SetAttributesStep(project,serviceManager);
     }
 }
