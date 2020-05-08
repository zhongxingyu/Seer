 package com.mpower.controller;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.propertyeditors.CustomDateEditor;
 import org.springframework.beans.propertyeditors.StringTrimmerEditor;
 import org.springframework.validation.BindException;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.SimpleFormController;
 
 import com.mpower.domain.Commitment;
 import com.mpower.domain.PaymentSource;
 import com.mpower.domain.Person;
 import com.mpower.service.CommitmentService;
 import com.mpower.service.PaymentSourceService;
 import com.mpower.service.PersonService;
 import com.mpower.service.SessionServiceImpl;
 import com.mpower.service.SiteService;
 import com.mpower.type.PageType;
 
 public class CommitmentFormController extends SimpleFormController {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     private CommitmentService commitmentService;
 
     private PaymentSourceService paymentSourceService;
 
     private PersonService personService;
 
     private SiteService siteService;
 
     public void setCommitmentService(CommitmentService commitmentService) {
         this.commitmentService = commitmentService;
     }
 
     public void setPaymentSourceService(PaymentSourceService paymentSourceService) {
         this.paymentSourceService = paymentSourceService;
     }
 
     public void setPersonService(PersonService personService) {
         this.personService = personService;
     }
 
     public void setSiteService(SiteService siteService) {
         this.siteService = siteService;
     }
 
     @Override
     protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
         binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("MM/dd/yyyy"), true));
         binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
         binder.registerCustomEditor(PaymentSource.class, new PaymentSourceEditor(paymentSourceService));
     }
 
    //    @SuppressWarnings("unchecked")
    //    @Override
    //    protected Map referenceData(HttpServletRequest request) throws Exception {
    //        Map refData = new HashMap();
    //        String personId = request.getParameter("personId");
    //        List<PaymentSource> paymentSources = paymentSourceService.readActivePaymentSources(Long.valueOf(personId));
    //        refData.put("paymentSources", paymentSources);
    //        return refData;
    //    }
 
     @Override
     protected Object formBackingObject(HttpServletRequest request) throws ServletException {
         String commitmentId = request.getParameter("commitmentId");
         Commitment commitment = null;
         if (commitmentId == null) {
             // create person
             String personId = request.getParameter("personId");
             // TODO: if the user navigates directly to gift.htm with no personId, we should redirect to giftSearch.htm
             Person person = null;
             if (personId != null) {
                 person = personService.readPersonById(Long.valueOf(personId));
                 if (person == null) {
                     logger.error("**** person not found for id: " + personId);
                     return commitment;
                 }
                 commitment = commitmentService.createDefaultCommitment(person);
                 commitment.setPerson(person);
             }
         } else {
             commitment = commitmentService.readCommitmentById(new Long(commitmentId));
         }
         if (isFormSubmission(request)) {
             Map<String, String> fieldLabelMap = siteService.readFieldLabels(SessionServiceImpl.lookupUserSiteName(), PageType.valueOf(getCommandName()), SessionServiceImpl.lookupUserRoles(), request.getLocale());
             commitment.setFieldLabelMap(fieldLabelMap);
 
             Map<String, Object> valueMap = siteService.readFieldValues(SessionServiceImpl.lookupUserSiteName(), PageType.valueOf(getCommandName()), SessionServiceImpl.lookupUserRoles(), commitment);
             commitment.setFieldValueMap(valueMap);
         }
         return commitment;
     }
 
     @Override
     public ModelAndView onSubmit(Object command, BindException errors) throws ServletException {
         Commitment commitment = (Commitment) command;
 
         // validate required fields
         Commitment current = commitmentService.maintainCommitment(commitment);
 
         // TODO: Adding errors.getModel() to our ModelAndView is a "hack" to allow our
         // form to post results back to the same page. We need to get the
         // command from errors and then add our search results to the model.
         ModelAndView mav = new ModelAndView("redirect:/commitmentView.htm", errors.getModel());
         mav.addObject("commitmentId", current.getId());
         mav.addObject("saved", true);
         mav.addObject("id", current.getId());
         return mav;
     }
 }
