 package org.openmrs.module.htmlformflowsheet.web.controller;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.Concept;
 import org.openmrs.ConceptAnswer;
 import org.openmrs.Drug;
 import org.openmrs.DrugOrder;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.htmlformflowsheet.DrugOrderEditCmdObj;
 import org.openmrs.propertyeditor.DrugEditor;
 import org.openmrs.util.OpenmrsUtil;
 import org.springframework.beans.propertyeditors.CustomNumberEditor;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 
 @Controller
 @RequestMapping("/module/htmlformflowsheet/drugOrderEdit.form")
 public class DrugOrderEditController  {
     
      private Log log = LogFactory.getLog(this.getClass());
 
      @ModelAttribute("allDrugs")
      public Map<Drug, String> populateDrugTypes() {
          List<Drug> drugs = Context.getConceptService().getAllDrugs();
          //remove non-voided
          List<Drug> ret = new ArrayList<Drug>();
          for (Drug d : drugs){
              if (!d.isRetired())
                  ret.add(d);
          }
          Collections.sort(ret, new Comparator<Drug>() {
              public int compare(Drug left, Drug right) {
                  if (left.getName() == null)
                      return -1;
                  else if (right.getName() == null)
                      return 1;
                  else
                      return left.getName().toUpperCase().compareTo(right.getName().toUpperCase());
               } 
           });
          Map<Drug,String> drugMap = new LinkedHashMap<Drug, String>();
          for (Drug drug : ret){
              String drugName = "";
              if (drug.getName() == null)
                  drugName = drug.getConcept().getBestName(Context.getLocale()).getName();
              else 
                  drugName = drug.getName();
              drugMap.put(drug, drugName + " (" + drug.getUnits() + ")");
          
          }
          return drugMap;
      }
      
      
      private Concept getDiscontinueReason(){
          Concept disConcept = null;
          String discontinueReasonId = Context.getAdministrationService().getGlobalProperty("htmlformflowsheet.drugDiscontinueReason");
          if (discontinueReasonId != null && !discontinueReasonId.equals("")){
              try {
                  disConcept = Context.getConceptService().getConcept(Integer.valueOf(discontinueReasonId));
              } catch (Exception ex){
                  disConcept = Context.getConceptService().getConceptByUuid(discontinueReasonId);
              } finally {
                  if (disConcept == null)
                      log.warn("no discontinue reason concept found for global property htmlformflowsheet.drugDiscontinueReason");
              }
          }
          return disConcept;
      }
      
      @ModelAttribute("discontinueReasons")
      public List<Concept> getDiscontinueReasons(){
          List<Concept> ret = new ArrayList<Concept>();
          Concept c = getDiscontinueReason();
          if (c != null){
              for (ConceptAnswer ca : c.getAnswers(false)){
                  //for lazy initialization
                  Concept answerConcept = Context.getConceptService().getConcept(ca.getAnswerConcept().getConceptId());
                  ret.add(answerConcept);
              }
          }
          return ret;
      }
 
      
     @RequestMapping(method = RequestMethod.GET)
     public ModelAndView setupForm(HttpServletRequest request,
            @RequestParam(value="drugOrderId", required=true) String drugOrderId,
            @RequestParam(value="dialogToClose", required=true) String dialogToClose ){
 
         DrugOrder d;
         try {
             d = (DrugOrder) Context.getOrderService().getOrder(Integer.valueOf(drugOrderId));
         } catch (Exception ex){
             throw new RuntimeException("You must provide a valid drugOrderId in the drugOrderId parameter.");
         }
         if (d.getEncounter() != null)
            throw new RuntimeException("that's weird -- this controller is supposed to be for editing drugOrders with no encounter attached.");    
         if (d == null)
             throw new RuntimeException("DrugOrder not found. Make sure URL parameter drugOrderId is valid. Right now its set to " + drugOrderId);
         DrugOrderEditCmdObj model = new DrugOrderEditCmdObj();
         //model.put("patient", p);
         model.setDrugOrder(d);
         model.setDialogToClose(dialogToClose);
         return new ModelAndView("module/htmlformflowsheet/drugOrderEdit", "model", model);
         
     }    
     
 
     
     @RequestMapping(method = RequestMethod.POST)
     public String handleSubmit(
             @RequestParam(value="refCloseAfterSubmission", required=false) String closeAfterSubmission,
             @RequestParam(value="refDrugOrderId", required=true) String drugOrderId, 
             @RequestParam(value="refDrugName", required=false) String drugNameStr,
             @RequestParam(value="refDose", required=false) String dose,
             @RequestParam(value="refFrequency", required=false) String frequency,
             @RequestParam(value="refStartDate", required=false) String startDateStr,
             @RequestParam(value="refAutoExpireDate", required=false) String autoExpireDateStr,
             @RequestParam(value="refDiscontinedDate", required=false) String discontinuedDateStr,
            @RequestParam(value="refDiscontinueReason", required=false) String discontinueReason,
             @RequestParam(value="refInstructions", required=false) String instructions,
             @RequestParam(value="refPrn", required=false) Boolean prn,
             @RequestParam(value="refVoided", required=false) Boolean voided,
             @RequestParam(value="refVoidReason", required=false) String voidReason
             ){
//                System.out.println("closeAfterSubmission " + closeAfterSubmission);
//                System.out.println("drugOrderId " + drugOrderId);
//                System.out.println("drugNameStr " + drugNameStr);
//                System.out.println("dose " + dose);
//                System.out.println("frequency " + frequency);
//                System.out.println("startDateStr " + startDateStr);
//                System.out.println("autoExpireDateStr " + autoExpireDateStr);
//                System.out.println("discontinuedDateStr " + discontinuedDateStr);
//                System.out.println("discontinueReason " + discontinueReason);
//                System.out.println("instructions " + instructions);
//                System.out.println("prn " + prn);
//                System.out.println("voided " + voided);
//                System.out.println("voidReason " + voidReason);
                
                 DrugOrder dor = (DrugOrder) Context.getOrderService().getOrder(Integer.valueOf(drugOrderId)); 
                 boolean shouldSave = false;
                 if (drugNameStr != null && !drugNameStr.equals("")){
                     Drug drug = null;
                     // pattern to match a uuid, i.e., five blocks of alphanumerics separated by hyphens
                     if (Pattern.compile("\\w+-\\w+-\\w+-\\w+-\\w+").matcher(drugNameStr.trim()).matches()) {
                         drug = Context.getConceptService().getDrugByUuid(drugNameStr.trim());
                     } else {
                         drug = Context.getConceptService().getDrugByNameOrId(drugNameStr.trim());           
                     }
                     if (drug == null){
                         dor.setVoided(true);
                         dor.setVoidedBy(Context.getAuthenticatedUser());
                         dor.setVoidReason("htmlformflowsheet - drug removed");
                         dor.setDateVoided(new Date());
                         shouldSave = true;
                    } else if (!OpenmrsUtil.nullSafeEquals(dor.getDrug().getDrugId(), drug.getDrugId())){
                         dor.setDrug(drug);
                         shouldSave = true;
                     }   
                 }
                
                 if ((dose == null || dose.equals("")) && dor.getDose() != null){
                     dor.setDose(null);
                     shouldSave = true;
                 }
                 else if (dose != null){
                     try {
                         if (!OpenmrsUtil.nullSafeEquals(Double.valueOf(dose), dor.getDose())){
                             dor.setDose(Double.valueOf(dose));
                             shouldSave = true;
                         }
                     } catch (Exception ex){
                         throw new RuntimeException(ex);
                     }
                 } 
             
                 if (!OpenmrsUtil.nullSafeEquals(frequency, dor.getFrequency())){
                     dor.setFrequency(frequency);
                     shouldSave = true;
                 }
                 SimpleDateFormat sdf = Context.getDateFormat();
                 Date startDate = null;
                 try {
                     startDate = sdf.parse(startDateStr);
                 } catch (Exception ex){
                     throw new RuntimeException("You must provide a valid date.");
                 }
                 if (startDate == null)
                     throw new RuntimeException("You can't create a drug order without a start date.");
                 if (!OpenmrsUtil.nullSafeEquals(startDate, dor.getStartDate())){
                     dor.setStartDate(startDate);
                     shouldSave = true;
                 }
                 
                 Date discontinedDate = null;
                 try {
                     discontinedDate = sdf.parse(discontinuedDateStr);
                 } catch (Exception ex){
                     
                 }
                 if (!OpenmrsUtil.nullSafeEquals(discontinedDate, dor.getDiscontinuedDate())){
                     dor.setDiscontinuedDate(discontinedDate);
                     shouldSave = true;
                 }
                 
                 Date autoExpireDate = null;
                 try {
                     autoExpireDate = sdf.parse(autoExpireDateStr);
                 } catch (Exception ex){
                     //pass
                 }
                 if (!OpenmrsUtil.nullSafeEquals(autoExpireDate, dor.getAutoExpireDate())){
                     dor.setAutoExpireDate(autoExpireDate);
                     shouldSave = true;
                 }
                 
                 if (discontinueReason != null && !discontinueReason.equals("")){
                     Concept discReason = Context.getConceptService().getConcept(Integer.valueOf(discontinueReason));
                     if (!OpenmrsUtil.nullSafeEquals(discReason, dor.getDiscontinuedReason())){
                         dor.setDiscontinuedReason(discReason);
                         shouldSave = true;
                     }
                 
                 } else if (discontinueReason == null || discontinueReason.equals("") && dor.getDiscontinuedReason() != null){
                     dor.setDiscontinuedReason(null);
                     shouldSave = true;
                 }
                 
                 if (!OpenmrsUtil.nullSafeEquals(instructions, dor.getInstructions())){
                     dor.setInstructions(instructions);
                     shouldSave = true;
                 }
                 
                 if (!OpenmrsUtil.nullSafeEquals(prn, dor.getPrn())){
                     dor.setPrn(prn);
                     shouldSave = true;
                 }
                 
                 if (!OpenmrsUtil.nullSafeEquals(voided, dor.getVoided())){
                     dor.setVoided(voided);
                     shouldSave = true;
                 }
                  
                 if (!OpenmrsUtil.nullSafeEquals(voidReason, dor.getVoidReason())){
                     dor.setVoidReason(voidReason);
                     shouldSave = true;
                 }
                 if (shouldSave)
                     Context.getOrderService().saveOrder(dor);
                     
                 return "redirect:/module/htmlformflowsheet/closeDialog.form?dialogToClose=" + closeAfterSubmission;
       
     }
     
     @InitBinder
     protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
         binder.registerCustomEditor(Drug.class, new DrugEditor());
         binder.registerCustomEditor(Double.class, new CustomNumberEditor(Double.class, true));
     }
     
     
 }
