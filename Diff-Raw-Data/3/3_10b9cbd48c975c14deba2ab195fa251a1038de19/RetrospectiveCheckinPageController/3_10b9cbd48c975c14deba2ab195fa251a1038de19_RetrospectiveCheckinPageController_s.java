 package org.openmrs.module.emr.page.controller;
 
 import org.openmrs.Concept;
 import org.openmrs.ConceptAnswer;
 import org.openmrs.Location;
 import org.openmrs.Obs;
 import org.openmrs.Patient;
 import org.openmrs.Provider;
 import org.openmrs.api.ConceptService;
 import org.openmrs.api.LocationService;
 import org.openmrs.api.ProviderService;
 import org.openmrs.module.emr.EmrProperties;
 import org.openmrs.module.emr.adt.AdtService;
 import org.openmrs.ui.framework.SimpleObject;
 import org.openmrs.ui.framework.UiUtils;
 import org.openmrs.ui.framework.annotation.SpringBean;
 import org.openmrs.ui.framework.page.PageModel;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.List;
 
 public class RetrospectiveCheckinPageController {
 
     public void get(@SpringBean("locationService") LocationService locationService,
                     @SpringBean("providerService") ProviderService providerService,
                     @SpringBean("conceptService") ConceptService conceptService,
                     @RequestParam("patientId") Patient patient,
                    @RequestParam("uiOption") Integer uiOption,
                     UiUtils ui,
                     PageModel model) {
 
         Concept amountPaidConcept = conceptService.getConceptByUuid("5d1bc5de-6a35-4195-8631-7322941fe528");
         model.addAttribute("patient", patient);
         model.addAttribute("locations", getLocations(locationService));
         model.addAttribute("providers", getClerkProviders(providerService));
         model.addAttribute("paymentReasons", getPaymentReasons(conceptService));
         model.addAttribute("paymentAmounts", getPossiblePaymentAmounts());
        model.addAttribute("uiOption", uiOption);
     }
 
     public String post(UiUtils ui,
                        @SpringBean("adtService") AdtService adtService,
                        @SpringBean("conceptService") ConceptService conceptService,
                        @SpringBean("providerService")ProviderService providerService,
                        @SpringBean("emrProperties")EmrProperties emrProperties,
                        @RequestParam("patientId") Patient patient,
                        @RequestParam("locationId") Location location,
                        @RequestParam("providerId") Provider provider,
                        @RequestParam("checkinDate_day") Integer checkinDateDay,
                        @RequestParam("checkinDate_month") Integer checkinDateMonth,
                        @RequestParam("checkinDate_year") Integer checkinDateYear,
                        @RequestParam(value = "checkinDate_hour", required = false, defaultValue = "0") Integer checkinDateHour,
                        @RequestParam(value = "checkinDate_minutes", required = false, defaultValue = "0") Integer checkinDateMinutes,
                        @RequestParam("paymentReasonId") Integer paymentReasonId,
                        @RequestParam("paidAmountId") Double paidAmount,
                        @RequestParam("receiptNumber") String receiptNumber) {
 
         Obs paymentReason = createPaymentReasonObservation(conceptService, emrProperties, paymentReasonId);
         Obs paymentAmount = createPaymentAmountObservation(emrProperties, paidAmount);
         Obs paymentReceipt = createPaymentReceiptObservation(emrProperties, receiptNumber);
 
         Calendar calendar = Calendar.getInstance();
         calendar.set(checkinDateYear, checkinDateMonth-1, checkinDateDay, checkinDateHour, checkinDateMinutes);
         adtService.createCheckinInRetrospective(patient, location, provider, paymentReason, paymentAmount, paymentReceipt, calendar.getTime());
         return "redirect:" + ui.pageLink("emr", "patient", SimpleObject.create("patientId", patient.getId()));
     }
 
     private Obs createPaymentReceiptObservation(EmrProperties emrProperties, String receiptNumber) {
         Obs paymentReceipt = new Obs();
         paymentReceipt.setConcept(emrProperties.getPaymentReceiptNumberConcept());
         paymentReceipt.setValueText(receiptNumber);
         return paymentReceipt;
     }
 
     private Obs createPaymentAmountObservation(EmrProperties emrProperties, Double paidAmount) {
         Obs paymentAmount = new Obs();
         paymentAmount.setConcept(emrProperties.getPaymentAmountConcept());
         paymentAmount.setValueNumeric(paidAmount);
         return paymentAmount;
     }
 
     private Obs createPaymentReasonObservation(ConceptService conceptService, EmrProperties emrProperties, Integer paymentReasonId) {
         Obs paymentReason = new Obs();
         paymentReason.setConcept(emrProperties.getPaymentReasonsConcept());
         paymentReason.setValueCoded(conceptService.getConcept(paymentReasonId));
         return paymentReason;
     }
 
     private List<SimpleObject> getPossiblePaymentAmounts() {
         List<SimpleObject> items = new ArrayList<SimpleObject>();
         SimpleObject exempt = new SimpleObject();
         exempt.put("value", 0);
         exempt.put("label", "Exempt");
 
         SimpleObject fiftyGourdes = new SimpleObject();
         fiftyGourdes.put("value", 50);
         fiftyGourdes.put("label", "50 Gourdes");
 
         SimpleObject hundredGourdes = new SimpleObject();
         hundredGourdes.put("value", 100);
         hundredGourdes.put("label", "100 Gourdes");
 
         items.add(hundredGourdes); items.add(fiftyGourdes); items.add(exempt);
         return items;
     }
 
     private List<SimpleObject> getPaymentReasons(ConceptService conceptService) {
         List<SimpleObject> items = new ArrayList<SimpleObject>();
         Concept paymentReason = conceptService.getConceptByUuid("36ba7721-fae0-4da4-aef2-7e476cc04bdf");
         Collection<ConceptAnswer> paymentReasonAnswers = paymentReason.getAnswers();
         for(ConceptAnswer reason : paymentReasonAnswers) {
             Concept answerConcept = reason.getAnswerConcept();
             SimpleObject item = new SimpleObject();
             item.put("value", answerConcept.getConceptId());
             item.put("label", answerConcept.getName().getName());
             items.add(item);
         }
         return items;
     }
 
     private List<SimpleObject> getLocations(LocationService locationService) {
         List<SimpleObject> items = new ArrayList<SimpleObject>();
         List<Location> locations = locationService.getAllLocations(false);
         for (Location location: locations) {
             SimpleObject item = new SimpleObject();
             item.put("value", location.getLocationId());
             item.put("label", location.getName());
             items.add(item);
         }
         return items;
 
     }
 
     private List<SimpleObject> getClerkProviders(ProviderService providerService) {
         List<SimpleObject> items = new ArrayList<SimpleObject>();
         List<Provider> clerks = providerService.getAllProviders(false);
         for(Provider clerk: clerks) {
             SimpleObject item = new SimpleObject();
             item.put("value", clerk.getProviderId());
             item.put("label", clerk.getName());
             items.add(item);
         }
 
         return items;
     }
 }
