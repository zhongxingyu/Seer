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
 
 package org.openmrs.module.emr.page.controller;
 
 import org.openmrs.Concept;
 import org.openmrs.Patient;
 import org.openmrs.Provider;
 import org.openmrs.api.ConceptService;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.emr.EmrConstants;
 import org.openmrs.ui.framework.SimpleObject;
 import org.openmrs.ui.framework.UiUtils;
 import org.openmrs.ui.framework.annotation.SpringBean;
 import org.openmrs.ui.framework.page.PageModel;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Locale;
 
 public class OrderXrayPageController {
 
     public void controller(@RequestParam("patientId") Patient patient,
                            @SpringBean("conceptService") ConceptService conceptService,
                            UiUtils ui,
                            PageModel model) {
 
         Collection<Provider> providers = Context.getProviderService().getProvidersByPerson(Context.getAuthenticatedUser().getPerson());
         model.addAttribute("currentProvider", providers.iterator().next());
 
         model.addAttribute("xrayOrderables", ui.toJson(getXrayOrderables(conceptService, Context.getLocale())));
         model.addAttribute("patient", patient);
     }
 
     private List<SimpleObject> getXrayOrderables(ConceptService conceptService, Locale locale) {
         List<SimpleObject> items = new ArrayList<SimpleObject>();
        Concept xrayOrderable = conceptService.getConceptByUuid(EmrConstants.GP_XRAY_ORDERABLE_CONCEPTS);
         for (Concept concept : xrayOrderable.getSetMembers()) {
             SimpleObject item = new SimpleObject();
             item.put("value", concept.getId());
             item.put("label", concept.getFullySpecifiedName(locale));
             items.add(item);
         }
         return items;
     }
 
 }
