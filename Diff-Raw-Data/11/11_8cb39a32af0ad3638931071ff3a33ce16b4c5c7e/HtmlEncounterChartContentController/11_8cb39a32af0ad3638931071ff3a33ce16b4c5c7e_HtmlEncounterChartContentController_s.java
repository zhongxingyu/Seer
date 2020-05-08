 package org.openmrs.module.htmlformflowsheet.web.controller;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Concept;
 import org.openmrs.Encounter;
 import org.openmrs.Form;
 import org.openmrs.Obs;
 import org.openmrs.Patient;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.htmlformentry.FormEntrySession;
 import org.openmrs.module.htmlformentry.HtmlForm;
 import org.openmrs.module.htmlformentry.HtmlFormEntryService;
 import org.openmrs.module.htmlformentry.FormEntryContext.Mode;
 import org.openmrs.module.htmlformentry.schema.HtmlFormField;
 import org.openmrs.module.htmlformentry.schema.HtmlFormSchema;
 import org.openmrs.module.htmlformentry.schema.HtmlFormSection;
 import org.openmrs.module.htmlformentry.schema.ObsField;
 import org.openmrs.module.htmlformentry.schema.ObsGroup;
 import org.openmrs.module.htmlformentry.web.controller.HtmlFormEntryController;
 import org.springframework.ui.ModelMap;
 import org.springframework.util.StringUtils;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.Controller;
 
 public class HtmlEncounterChartContentController implements Controller {
 
     private Log log = LogFactory.getLog(this.getClass());
     
    public HtmlEncounterChartContentController(){super();}
 //    
 //    private Integer count;
 //    private Integer patientId;
 //    private Integer encounterTypeId;
 //    private Integer formId;
 //    boolean showAddAnother = true;
     
     public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
         
         ModelMap model = new ModelMap();
         Integer count = Integer.valueOf(request.getParameter("count"));
         Integer patientId;
         try {
             patientId = Integer.valueOf(request.getParameter("patientId"));
         } catch (Exception ex){
             FormEntrySession fes = (FormEntrySession)  Context.getVolatileUserData(HtmlFormEntryController.FORM_IN_PROGRESS_KEY);
             patientId = fes.getPatient().getPatientId();
         }
         Integer encounterTypeId = Integer.valueOf(request.getParameter("encounterTypeId"));
         Integer formId = Integer.valueOf(request.getParameter("formId"));
         String portletUUID = request.getParameter("portletUUID");
         String view = request.getParameter("view");
         model.put("view", view);
         String readOnly = request.getParameter("readOnly");
         if (readOnly == null || readOnly.equals(""))
             readOnly="false";
         model.put("readOnly", readOnly);
         model.put("personId", patientId);
         
         
         model.put("portletUUID", portletUUID.replace("-", ""));
         Patient patient = Context.getPatientService().getPatient(patientId);
         model.put("patient", patient);
         //TODO:  read-only version?
         model.put("patientEncounters", Context.getEncounterService().getEncountersByPatient(patient));
         model.put("formId", formId);
         model.put("encounterTypeId", encounterTypeId);
         model.put("conceptsToShow", "");
         
         
         
         
         List<Encounter> allEncs = (List<Encounter>) model.get("patientEncounters");
         Form form = Context.getFormService().getForm(formId);
         Map<Integer, List<Encounter>> encountersByType = (Map<Integer, List<Encounter>>) model.get("encountersByType");
         
         if (encountersByType == null) {
             // map from encounterTypeId to list of encounters of that type 
             encountersByType = new HashMap<Integer, List<Encounter>>();
             
             if (allEncs != null) {
                 for (Encounter e : allEncs) {
                     if (e.getEncounterType() != null){
                         Integer id = e.getEncounterType().getEncounterTypeId();
                         List<Encounter> list = encountersByType.get(id);
                         if (list == null) {
                             list = new ArrayList<Encounter>();
                             encountersByType.put(id, list);
                         }
                         list.add(e);    
                     }    
                 }
             }
             model.put("encountersByType", encountersByType);
         }
         
         List<Encounter> encountersToUse;
         if ("*".equals(model.get("encounterTypeId"))) {
             encountersToUse = allEncs;
         } else {
             encountersToUse = encountersByType.get(encounterTypeId);
             if (encountersToUse == null) {
                 encountersToUse = new ArrayList<Encounter>();
             }
         }
         List<Encounter> encs = new ArrayList<Encounter>();
         for (Encounter encTmp : encountersToUse){
             if (encTmp.getForm() != null && encTmp.getForm().equals(form))
                 encs.add(encTmp);
         }
         
         model.put("encounterListForChart", encs);
         // now figure out which concepts we want to display as columns
         Set<Concept> concepts = new LinkedHashSet<Concept>();
         {
             String conceptsToShowString = (String) model.get("conceptsToShow");
             if (StringUtils.hasText(conceptsToShowString)) {
                 for (String idAsString : conceptsToShowString.split(",")) {
                     Concept c = Context.getConceptService().getConceptByIdOrName(idAsString);
                     concepts.add(c);
                 }
             } else if (form != null) {
                 HtmlForm htmlForm = Context.getService(HtmlFormEntryService.class).getHtmlFormByForm(form);
                 Patient p = (Patient) model.get("patient");
                 try {
                     FormEntrySession fes = new FormEntrySession(p, null, Mode.ENTER, htmlForm);
                     HtmlFormSchema schema = fes.getContext().getSchema();
                     for (HtmlFormSection section : schema.getSections()) {
                         for (HtmlFormField field : section.getFields()) {
                             fieldHelper(field, concepts);
                         }
                     }
                 } catch (Exception ex) {
                     log.error("Failure inspecting form " + form.getFormId() + " schema for obs " + ex);
                     ex.printStackTrace();
                 }
             }
         }
         
         // now go through the Obs in the encounters we want to view and find the ones that will go in the table
         Map<Encounter, Map<Integer, List<Obs>>> encounterToObsMap = new HashMap<Encounter, Map<Integer,List<Obs>>>();
         for (Encounter e : encountersToUse) {
             Map<Integer, List<Obs>> holder = new HashMap<Integer, List<Obs>>();
             encounterToObsMap.put(e, holder);
             for (Concept c : concepts) {
                 holder.put(c.getConceptId(), new ArrayList<Obs>());
             }
             for (Obs o : e.getObs()) {
                 Concept c = o.getConcept();
                 if (concepts.contains(c)) {
                     holder.get(c.getConceptId()).add(o);
                 }
                 for (Obs oInner : o.getGroupMembers()){
                     Concept cInner = oInner.getConcept();
                     holder.get(cInner.getConceptId()).add(o);
                 }
             }
         }
         model.put("encounterChartConcepts", concepts);
         model.put("encounterChartObs", encounterToObsMap);
         
         return new ModelAndView("/module/htmlformflowsheet/encounterChartContent", "model", model);
     }
     
     /**
      * Gets all Concepts out of this field (and subfields) and adds them to the set.
      * TODO: handle obs groups
      * 
      * @param field
      * @param concepts
      */
     private void fieldHelper(HtmlFormField field, Set<Concept> concepts) {
         if (field instanceof ObsField) {
             ObsField of = (ObsField) field;
             concepts.add(of.getQuestion());
         } else if (field instanceof ObsGroup){
             ObsGroup og = (ObsGroup) field;
             for (ObsField ofInner : og.getChildren()){
                 concepts.add(ofInner.getQuestion());
             }
         } else {
             log.debug(field.getClass() + " not yet implemented");
         }
     }
     
     
 }
