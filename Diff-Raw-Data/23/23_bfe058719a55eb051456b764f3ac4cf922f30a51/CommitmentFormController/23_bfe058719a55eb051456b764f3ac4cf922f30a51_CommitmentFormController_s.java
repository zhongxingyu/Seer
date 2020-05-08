 package com.mpower.controller.commitment;
 
 import java.util.Map;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.validation.BindException;
 import org.springframework.validation.Errors;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.mpower.controller.TangerineConstituentAttributesFormController;
 import com.mpower.domain.model.AbstractEntity;
 import com.mpower.domain.model.paymentInfo.Commitment;
 import com.mpower.service.CommitmentService;
 import com.mpower.type.CommitmentType;
 import com.mpower.util.StringConstants;
 
 public class CommitmentFormController extends TangerineConstituentAttributesFormController {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     @Resource(name="commitmentService")
     protected CommitmentService commitmentService;
     protected CommitmentType commitmentType;
     protected String formUrl;
 
     public void setCommitmentType(CommitmentType commitmentType) {
         this.commitmentType = commitmentType;
     }
     
     public void setFormUrl(String formUrl) {
         this.formUrl = formUrl;
     }
 
     @Override
     protected void onBindAndValidate(HttpServletRequest request, Object command, BindException errors) throws Exception {
        if (isFormSubmission(request) && errors.hasErrors()) {
             Commitment commitment = (Commitment) command;
             commitment.removeEmptyMutableDistributionLines();
         }
         super.onBindAndValidate(request, command, errors);
     }
 
     @Override
     protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
         Commitment commitment = (Commitment) command;        
         Commitment current = commitmentService.maintainCommitment(commitment);
         
         String url = current.getGifts().isEmpty() ? formUrl : getSuccessView();
         return new ModelAndView(super.appendSaved(url + "?" + StringConstants.COMMITMENT_ID + "=" + current.getId() + "&" + StringConstants.PERSON_ID + "=" + super.getConstituentId(request)));
     }
 
     @SuppressWarnings("unchecked")
     @Override
     protected Map referenceData(HttpServletRequest request, Object command, Errors errors) throws Exception {
         Map refData = super.referenceData(request, command, errors);
         commitmentService.findGiftSum(refData, (Commitment)command);
         return refData;
     }
 
     @Override
     protected AbstractEntity findEntity(HttpServletRequest request) {
         return commitmentService.readCommitmentByIdCreateIfNull(request.getParameter(StringConstants.COMMITMENT_ID), super.getConstituent(request), commitmentType);
     }
 }
