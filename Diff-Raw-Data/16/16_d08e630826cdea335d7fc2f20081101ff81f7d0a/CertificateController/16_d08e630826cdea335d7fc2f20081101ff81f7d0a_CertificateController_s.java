 package org.openmrs.module.hr.web.controller;
 
 import org.openmrs.api.context.Context;
 import org.openmrs.module.hr.HrCertificate;
 import org.openmrs.module.hr.api.HRQualificationService;
 import org.openmrs.module.hr.api.validator.CertificateValidator;
 import org.openmrs.web.WebConstants;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.List;
 
 @Controller
 public class CertificateController {
 
     private static final String SUCCESS_LIST_VIEW = "/module/hr/admin/certificates";
     private static final String SUCCESS_FORM_VIEW = "/module/hr/admin/certificate";
 
     @RequestMapping(value="module/hr/admin/certificate.form")
     @ModelAttribute("certificate")
     public HrCertificate showForm(@RequestParam(value="certificateId",required=false) Integer certificateId){
         HrCertificate hrCertificate;
         HRQualificationService hrQualificationService = Context.getService(HRQualificationService.class);
         if(certificateId != null)
             hrCertificate = hrQualificationService.getCertificateById(certificateId);
         else
             hrCertificate = new HrCertificate();
         return hrCertificate;
     }
 
 
     @RequestMapping(value = "module/hr/admin/certificates.list")
     public String showList(ModelMap model){
         HRQualificationService hrQualificationService=Context.getService(HRQualificationService.class);
         List<HrCertificate> hrCertificateList= hrQualificationService.getCertificates();
         model.addAttribute("certificatesList",hrCertificateList);
         return SUCCESS_LIST_VIEW;
     }
 
     @RequestMapping(value ="module/hr/admin/certificate.form", method = RequestMethod.POST)
     public ModelAndView createOrUpdateCertificate(HttpServletRequest request,@ModelAttribute("certificate")  HrCertificate certificate, BindingResult errors){
         HRQualificationService hrQualificationService = Context.getService(HRQualificationService.class);
         new CertificateValidator().validate(certificate,errors);
         if(errors.hasErrors())
             return new ModelAndView(SUCCESS_FORM_VIEW);
 
         if(request.getParameter("retireCertificate") != null)
             return checkAndRetireCertificate(request, certificate, hrQualificationService , errors);
 
        if(request.getParameter("unretireCertificate") != null)
            return unRetireCertificate(request, certificate, hrQualificationService, errors);

         hrQualificationService.saveCertificate(certificate);
         request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Certificate saved Successfully");
         return new ModelAndView(SUCCESS_LIST_VIEW).addObject("certificatesList",hrQualificationService.getCertificates());

     }
 
     private ModelAndView unRetireCertificate(HttpServletRequest request, HrCertificate certificate, HRQualificationService hrQualificationService, BindingResult errors) {
        hrQualificationService.unretireCertificate(certificate);
         request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Certificate Un-Retired Successfully");
         return new ModelAndView(SUCCESS_LIST_VIEW).addObject("certificatesList",hrQualificationService.getCertificates());
     }
 
     private ModelAndView checkAndRetireCertificate(HttpServletRequest request, HrCertificate certificate, HRQualificationService hrQualificationService, BindingResult errors) {
         String retireReason = request.getParameter("retireReason");
         if (certificate.getId() != null && (retireReason == null || retireReason.length() == 0)) {
             errors.reject("retireReason", "Retire reason cannot be empty");
             return new ModelAndView(SUCCESS_FORM_VIEW);
         }
        hrQualificationService.retireCertificate(certificate, retireReason);
         request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Certificate Retired Successfully");
         return new ModelAndView(SUCCESS_LIST_VIEW).addObject("certificatesList",hrQualificationService.getCertificates());
     }
 
 }
 
