 package ru.smsl.webkrona.controllers;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import javax.annotation.Resource;
 import org.springframework.mobile.device.site.SitePreference;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.Errors;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import ru.smsl.webkrona.exception.NotAllowedRoleException;
 import ru.smsl.webkrona.form.IndexForm;
 import ru.smsl.webkrona.form.ValidatationUtil;
 import ru.smsl.webkrona.service.LoginService;
 import ru.smsl.webkrona.service.xmlrpc.client.KronaClientService;
 
 @Controller
 public class IndexController extends BaseController {
     
     private static final String MOBILE_INDEX_PAGE = "mobile/index2";
 
     @Resource
     private LoginService loginService;
     @Resource
     private KronaClientService kronaClientService;
 
     @RequestMapping(value = "/test")
    public String testError(Model model) throws NotAllowedRoleException {
         //TODO удалить контроллер
         throw new NotAllowedRoleException("oops! i did it again!");
     }
 
     @RequestMapping(value = "/", method = RequestMethod.GET)
     public String index(SitePreference sitePreference, Model model) {
         if (sitePreference.isMobile()) {
             return MOBILE_INDEX_PAGE;
         }
         if (model.asMap().get("indexForm") == null) {
             model.addAttribute("indexForm", new IndexForm());
         }
         return "index";
     }
 
    /*
     @RequestMapping(value = "/", params = {"actionName=add"}, method = RequestMethod.POST)
     public String addSubmit(@ModelAttribute IndexForm indexForm,
             Errors errors,
             Model model) {
         ValidatationUtil.validateMsisdn(indexForm.getAddedMsisdn(), errors);
         if (!errors.hasErrors()) {
             indexForm.addMsisdn(normalizeMsisdn(indexForm.getAddedMsisdn()));
             indexForm.setAddedMsisdn(null);
         }
         return "index";
     }
 
     @RequestMapping(value = "/", params = {"actionName=create"}, method = RequestMethod.POST)
     public String createSubmit(@ModelAttribute IndexForm indexForm,
             Errors errors,
             Model model) {
         indexForm.validate(errors);
         if (errors.hasErrors()) {
             return "index";
         }
         String msisdn = normalizeMsisdn(indexForm.getMsisdn());
 
         if (loginService.isRegistered(msisdn)) {
             messageUtil.addInfoCode("info.already.registered");
             return "redirect:/login";
         }
         kronaClientService.createClosed(msisdn, "", 3, indexForm.getMsisdns());
         loginService.authentication(msisdn);
         return "redirect:/wizard";
     }
    */
     
    @RequestMapping(value = "/", params = {"actionName=reg"}, method = RequestMethod.POST)
     public String regMobileSubmit(
             @RequestParam(value="msisdn_reg", defaultValue = "") String msisdn,
             @RequestParam(defaultValue = "") String email,
             @RequestParam(defaultValue = "") String nick){
         if (!isValidMsisdn(msisdn)) {
             return MOBILE_INDEX_PAGE;
         }
         String normalisedMsisdn = normalizeMsisdn(msisdn);
         if (loginService.isRegistered(normalisedMsisdn)) {
             messageUtil.addInfoCode("info.already.registered");
             return MOBILE_INDEX_PAGE;
         }
         if (!isValidEmail(email)) {
             return MOBILE_INDEX_PAGE;
         }
         if (!isValidNick(nick)) {
             return MOBILE_INDEX_PAGE;
         }
         
         kronaClientService.registerUser(normalisedMsisdn, email, nick);
         loginService.authentication(normalisedMsisdn);        
         return "redirect:/";
     }
     
    @RequestMapping(value = "/", params = {"actionName=login"}, method = RequestMethod.POST)
     public String loginMobileSubmit(
             @RequestParam(value="msisdn_login", defaultValue = "") String msisdn,
             @RequestParam(defaultValue = "") String password){
         if (!isValidMsisdn(msisdn)) {
             return MOBILE_INDEX_PAGE;
         }
         String normalisedMsisdn = normalizeMsisdn(msisdn);
         if (!loginService.isAuthorized(normalisedMsisdn, password)) {
             messageUtil.addErrorCode("auth.wrong.pair");
             return MOBILE_INDEX_PAGE;
         }
         loginService.authentication(normalisedMsisdn);        
         return "redirect:/";
     }
             
     
    @RequestMapping(value = "/", params = {"actionName=create"}, method = RequestMethod.POST)
     public String createMobileSubmit(
             @RequestParam(defaultValue = "") String msisdn,
             @RequestParam Integer hours,
             @RequestParam(defaultValue = "opened") String groupType,
             @RequestParam(defaultValue = "") String groupName,
             @RequestParam String[] invited) {
         if (!isValidMsisdn(msisdn)) {
             return MOBILE_INDEX_PAGE;
         }
         String normalisedMsisdn = normalizeMsisdn(msisdn);
         List<String> normalisedInvited = new ArrayList<String>();
         for (String invite : invited) {
             if( invite.isEmpty() )
                 continue;
             if (!isValidMsisdn(invite)) {
                 return MOBILE_INDEX_PAGE;
             }
             normalisedInvited.add(normalizeMsisdn(invite));
         }
         if( groupType.equals("opened") ){
             kronaClientService.createOpened(normalisedMsisdn, groupName, hours, normalisedInvited);
         } else {
             kronaClientService.createClosed(normalisedMsisdn, groupName, hours, normalisedInvited);
         }
         return "redirect:/";
     }
     
     @RequestMapping(value = "/pay", method = RequestMethod.GET)
     public String pay(){
         return "mobile/pay";
     }
     
     @RequestMapping(value = "/help", method = RequestMethod.GET)
     public String help(){
         return "mobile/help";
     }
     
     @RequestMapping(value = "/terms", method = RequestMethod.GET)
     public String terms(){
         return "mobile/terms";
     }
 }
