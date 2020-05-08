 package it.sevenbits.conferences.web.controller;
 
 import it.sevenbits.conferences.domain.Company;
 import it.sevenbits.conferences.domain.Role;
 import it.sevenbits.conferences.domain.User;
 import it.sevenbits.conferences.service.CompanyService;
 import it.sevenbits.conferences.service.RoleService;
 import it.sevenbits.conferences.service.UserService;
 import it.sevenbits.conferences.service.VkontakteProfileService;
 import it.sevenbits.conferences.service.common.CustomUserDetailsService;
 import it.sevenbits.conferences.utils.file.FileManager;
 import it.sevenbits.conferences.utils.mail.MailSenderUtility;
 import it.sevenbits.conferences.utils.vkontakteAuthorization.VkontakteProfile;
 import it.sevenbits.conferences.utils.vkontakteAuthorization.VkontakteProfiles;
 import it.sevenbits.conferences.web.form.UserRegistrationForm;
 import org.apache.commons.lang3.RandomStringUtils;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.impl.client.HttpClientBuilder;
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.type.TypeReference;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContext;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
 import org.springframework.security.crypto.password.PasswordEncoder;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 import org.springframework.validation.Validator;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.UUID;
 
 @Controller
 @RequestMapping(value = "social")
 public class VkAuthorizationController {
 
     private static final Logger LOGGER = Logger.getLogger(VkAuthorizationController.class);
     private static final String CLIENT_ID = "4193643";
     private static final String CLIENT_SECRET = "C4VA3AUYfyB5wgng2U8o";
     private static final String RESPONSE_TYPE_CODE = "code";
     private static final String REDIRECT_URI = "http://saturdays.omskit.org/social/vkAuthentication";
 
     @Autowired
     private UserService userService;
     @Autowired
     private CustomUserDetailsService customUserDetailsService;
     @Autowired
     private VkontakteProfileService vkontakteProfileService;
     @Autowired
     private CompanyService companyService;
     @Autowired
     private MailSenderUtility mailSenderUtility;
     @Autowired
    private RoleService roleService;
    @Autowired
     @Qualifier("userSocialRegistrationValidator")
     private Validator userSocialRegistrationValidator;
 
     @RequestMapping(value = "vkAuthorization", method = RequestMethod.GET)
     public String getAuthorizationUrl() {
         String url =
                 "https://oauth.vk.com/authorize?client_id=" + CLIENT_ID +
                 "&scope=SETTINGS&redirect_uri=" + REDIRECT_URI +
                 "&response_type=" + RESPONSE_TYPE_CODE;
         return "redirect:" + url;
     }
 
     @RequestMapping(value = "vkAuthentication", method = RequestMethod.GET)
    public ModelAndView authetication(final HttpServletRequest request) {
         String code = request.getParameter("code");
         String userIdGetUrl =
                 "https://oauth.vk.com/access_token?client_id=" + CLIENT_ID +
                 "&client_secret=" + CLIENT_SECRET +
                 "&code=" + code +
                 "&redirect_uri=" + REDIRECT_URI;
         HttpClient httpClient = HttpClientBuilder.create().build();
         HttpUriRequest accessTokenGet = new HttpPost(userIdGetUrl);
         try {
             HttpResponse httpResponse = httpClient.execute(accessTokenGet);
             Map<String, String> map = new HashMap<>();
             ObjectMapper mapper = new ObjectMapper();
             map = mapper.readValue(httpResponse.getEntity().getContent(), new TypeReference<HashMap<String, String>>(){});
             VkontakteProfile vkontakteProfile = getVkontakteProfile(map.get("user_id"), map.get("access_token"));
             if (vkontakteProfile != null) {
                 User user = userService.getUserByVkontakteId(Long.parseLong(vkontakteProfile.getId()));
                 if (user != null && user.getEnabled()) {
                     authorizeUser(user);
                 } else {
                     UserRegistrationForm userRegistrationForm = new UserRegistrationForm();
                     userRegistrationForm.setFirstName(vkontakteProfile.getFirst_name());
                     userRegistrationForm.setSecondName(vkontakteProfile.getLast_name());
                     request.getSession().setAttribute("vkontakteUserId", vkontakteProfile.getId());
                    ModelAndView modelAndView = new ModelAndView("user-social-registration");
                     modelAndView.addObject("userRegistrationForm", userRegistrationForm);
                     return modelAndView;
                 }
             } else {
                 LOGGER.error("Vkontakte authorization: User isn't accessible");
             }
         } catch (IOException exception) {
             LOGGER.error("Vkontakte authorization: " + exception.getMessage());
         }
        return new ModelAndView("archive");
     }
 
    @RequestMapping(value = "social-registration", method = RequestMethod.POST)
     public ModelAndView socialUserRegistration(
             @ModelAttribute(value = "userRegistrationForm") final UserRegistrationForm userSocialRegistrationForm,
             final BindingResult bindingResult,
             final HttpSession httpSession
     ) {
         ModelAndView response;
         userSocialRegistrationValidator.validate(userSocialRegistrationForm, bindingResult);
         if (bindingResult.hasErrors()) {
             Map<String, String> errors = new HashMap<>();
             for (FieldError fieldError: bindingResult.getFieldErrors()) {
                 if (!errors.containsKey(fieldError.getField())) {
                     errors.put(fieldError.getField(), fieldError.getDefaultMessage());
                 }
             }
             response = new ModelAndView("user-social-registration");
         } else {
             User user = new User();
             user.setFirstName(userSocialRegistrationForm.getFirstName());
             user.setSecondName(userSocialRegistrationForm.getSecondName());
             user.setEmail(userSocialRegistrationForm.getEmail());
             user.setLogin(userSocialRegistrationForm.getEmail());
             String randomPassword = RandomStringUtils.random(15, true, true);
             PasswordEncoder encoder = new BCryptPasswordEncoder();
             String encodedRandomPassword = encoder.encode(randomPassword);
             user.setPassword(encodedRandomPassword);
             Company company = companyService.findCompanyByName(userSocialRegistrationForm.getCompany());
             user.setCompany(company);
             user.setJobPosition(userSocialRegistrationForm.getJobPosition());
             user.setEnabled(false);
             FileManager fileManager = new FileManager();
             if (!userSocialRegistrationForm.getPhoto().isEmpty()) {
                 String photoName = fileManager.saveImage(userSocialRegistrationForm.getPhoto());
                 user.setPhoto(photoName);
             } else {
                 user.setPhoto(null);
             }
             Role role = roleService.findRoleById(1L);
             user.setRole(role);
             String confirmationToken = UUID.randomUUID().toString();
             user.setConfirmationToken(confirmationToken);
             User updatedUser = userService.updateUser(user);
             it.sevenbits.conferences.domain.VkontakteProfile vkontakteProfile = new it.sevenbits.conferences.domain.VkontakteProfile();
             Long vkontakteUserId = Long.parseLong(httpSession.getAttribute("vkontakteUserId").toString());
             vkontakteProfile.setVkontakteId(vkontakteUserId);
             vkontakteProfile.setUser(updatedUser);
             vkontakteProfileService.updateVkontakteProfile(vkontakteProfile);
             mailSenderUtility.sendConfirmationToken(userSocialRegistrationForm.getEmail(), confirmationToken);
             LOGGER.error("Вроде все окау" + httpSession.getAttribute("vkontakteUserId").toString());
             response = new ModelAndView("user-social-registration");
         }
         return response;
     }
 
     private void authorizeUser(final User user) {
         UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
         Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
        LOGGER.error(userDetails.getPassword() + " " + userDetails.getAuthorities() + " " + userDetails.getUsername());
        LOGGER.error(authentication.isAuthenticated());
         SecurityContext securityContext = SecurityContextHolder.getContext();
         securityContext.setAuthentication(authentication);
     }
 
     private VkontakteProfile getVkontakteProfile(final String userId, final String accessToken) {
         String getProfileRequestUrl =
                 "https://api.vk.com/method/users.get?user_id=" + userId +
                 "&v=5.14" +
                 "&access_token=" + accessToken +
                 "&fields=photo_100";
         HttpClient httpClient = HttpClientBuilder.create().build();
         HttpUriRequest getProfileRequest = new HttpPost(getProfileRequestUrl);
         Map<String, ArrayList> map = new HashMap<>();
         VkontakteProfile vkontakteProfile = null;
         try {
             HttpResponse getProfileResponse = httpClient.execute(getProfileRequest);
             ObjectMapper mapper = new ObjectMapper();
             VkontakteProfiles vkontakteProfiles = mapper.readValue(getProfileResponse.getEntity().getContent(), VkontakteProfiles.class);
             if (!vkontakteProfiles.getProfiles().isEmpty()) {
                 vkontakteProfile = vkontakteProfiles.getProfiles().get(0);
             }
         } catch (IOException exception) {
             LOGGER.error("Vkontakte profile information: " + exception.getMessage());
         }
         return vkontakteProfile;
     }
 
 }
