 package com.mpower.controller;
 
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.validation.BindException;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.SimpleFormController;
 
 import com.mpower.domain.DistributionLine;
 import com.mpower.domain.Gift;
 import com.mpower.domain.Person;
 import com.mpower.service.GiftService;
 import com.mpower.service.PersonService;
 import com.mpower.service.SessionServiceImpl;
 import com.mpower.service.SiteService;
 import com.mpower.type.PageType;
 
 public class GiftFormController extends SimpleFormController {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     private GiftService giftService;
 
     private PersonService personService;
 
     public void setGiftService(GiftService giftService) {
         this.giftService = giftService;
     }
 
     public void setPersonService(PersonService personService) {
         this.personService = personService;
     }
 
     private SiteService siteService;
 
     public void setSiteService(SiteService siteService) {
         this.siteService = siteService;
     }
 
     @Override
     protected Object formBackingObject(HttpServletRequest request) throws ServletException {
         String giftId = request.getParameter("giftId");
         Gift gift = null;
         if (giftId == null) {
             // create person
             gift = giftService.createDefaultGift(SessionServiceImpl.lookupUserSiteName());
             String personId = request.getParameter("personId");
             // TODO: if the user navigates directly to gift.htm with no personId, we should redirect to giftSearch.htm
             Person person = null;
             if (personId != null) {
                 person = personService.readPersonById(Long.valueOf(personId));
                 if (person == null) {
                     logger.error("**** person not found for id: " + personId);
                     return gift;
                 }
             }
             gift.setPerson(person);
         } else {
             gift = giftService.readGiftById(new Long(giftId));
         }
         if (isFormSubmission(request)){
             Set<String> requiredFields = siteService.readRequiredFields(SessionServiceImpl.lookupUserSiteName(), PageType.valueOf(getCommandName()), SessionServiceImpl.lookupUserRoles());
             gift.setRequiredFields(requiredFields);
 
             Map<String, String> fieldLabelMap = siteService.readFieldLabels(SessionServiceImpl.lookupUserSiteName(), PageType.valueOf(getCommandName()), SessionServiceImpl.lookupUserRoles(), request.getLocale());
             gift.setFieldLabelMap(fieldLabelMap);
 
             Map<String, String> validationMap = siteService.readFieldValidations(SessionServiceImpl.lookupUserSiteName(), PageType.valueOf(getCommandName()), SessionServiceImpl.lookupUserRoles());
             gift.setValidationMap(validationMap);
 
             Map<String, Object> valueMap = siteService.readFieldValues(SessionServiceImpl.lookupUserSiteName(), PageType.valueOf(getCommandName()), SessionServiceImpl.lookupUserRoles(), gift);
             gift.setFieldValueMap(valueMap);
         }
         return gift;
     }
 
     @Override
    protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors) throws Exception {
        String giftId = request.getParameter("giftId");
        String personId = request.getParameter("personId");
        boolean redirect = false;
        if (giftId == null || personId == null) {
            redirect = true;
        } else {
            Person person = personService.readPersonById(Long.valueOf(personId));
            if (person == null) {
                redirect = true;
            }
        }

        if (redirect) {
            return new ModelAndView("redirect:/invalidGift.htm", errors.getModel());
        }
        return super.showForm(request, response, errors);
    }

    @Override
     public ModelAndView onSubmit(Object command, BindException errors) throws ServletException {
         Gift gift = (Gift) command;
 
         // validate required fields
 
         // TODO: This code is temporary validation to strip out invalid distribution lines.
         Iterator<DistributionLine> distLineIter = gift.getDistributionLines().iterator();
         while (distLineIter.hasNext()) {
             DistributionLine line = distLineIter.next();
             if (line == null || line.getAmount() == null) {
                 distLineIter.remove();
             }
         }
 
         Gift current = giftService.maintainGift(gift);
 
         // TODO: Adding errors.getModel() to our ModelAndView is a "hack" to allow our
         // form to post results back to the same page. We need to get the
         // command from errors and then add our search results to the model.
         ModelAndView mav = new ModelAndView("redirect:/giftView.htm", errors.getModel());
         mav.addObject("giftId", current.getId());
         return mav;
     }
 }
