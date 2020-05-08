 package com.controller;
 
 import com.domain.customer.*;
 import com.domain.shop.ProductRepository;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.propertyeditors.CustomDateEditor;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.multipart.commons.CommonsMultipartFile;
 
 import javax.inject.Inject;
 import javax.validation.Valid;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Map;
 
 /**
  * @author Stanislav Kurilin
  */
 @Controller
 @RequestMapping("/users")
 public class UserController {
 
     private final UserRepository repository;
     private final AvatarRepository avatarRepository;
     private final ProductRepository productRepository;
 
     private final Logger logger = LoggerFactory.getLogger(UserController.class);
 
     @Inject
     public UserController(UserRepository repository, AvatarRepository avatarRepository, ProductRepository productRepository) {
         this.repository = repository;
         this.avatarRepository = avatarRepository;
         this.productRepository = productRepository;
     }
 
     @InitBinder
     public void initBinder(WebDataBinder binder) {
         SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
         dateFormat.setLenient(false);
         // true passed to CustomDateEditor constructor means convert empty String to null
         binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
     }
 
     @RequestMapping(method = RequestMethod.GET)
     public String showAll(Map<String, Object> model) {
         model.put("users", repository.findAll());
         return "users";
     }
 
     @RequestMapping(value = "/new", method = RequestMethod.GET)
     public String createPage(Map<String, Object> model) {
         model.put("userForm", new UserForm(new User(), null));
         return "userNew";
     }
 
     @RequestMapping(value = "/{id}", method = RequestMethod.GET)
     public String showPage(@PathVariable("id") Long id, Map<String, Object> model) {
         final User user = repository.findOne(id);
         model.put("user", user);
         model.put("products", productRepository.findBySeller(user));
         return "userView";
     }
 
     @RequestMapping(value = "/{id}/edit", method = RequestMethod.GET)
     public String editPage(@PathVariable("id") Long id, Map<String, Object> model) {
         model.put("userForm", new UserForm(repository.findOne(id), null));
        model.put("id", id);
         return "usersEdit";
     }
 
     @RequestMapping(value = "/new", method = RequestMethod.POST)
     public String create(@ModelAttribute @Valid UserForm userForm, BindingResult bindingResult) {
         if (bindingResult.hasErrors()) {
             return "userNew";
         }
         final User user = userForm.getUser();
         final Avatar avatar = userForm.getAvatar();
         final User entity = repository.save(user);
         avatarRepository.assign(entity.getId(), avatar);
         return "redirect:/users/" + entity.getId();
     }
 
     @RequestMapping(value = "/{id}", method = RequestMethod.POST)
     public String edit(@PathVariable("id") Long id, @Valid UserForm userForm, BindingResult bindingResult, Map<String, Object> model) {
         if (bindingResult.hasErrors()) {
             model.put("id", id);
             return "usersEdit";
         }
         repository.updateEntity(id, userForm.getUser());
         avatarRepository.assign(id, userForm.getAvatar());
         return "redirect:/users/" + id;
     }
 
 
     @RequestMapping(value = "/findByEmail", method = RequestMethod.GET, params = "email")
     public String findByEmail(@RequestParam("email") EmailAddress email) {
         final User user = repository.findByEmail(email);
         if (user == null) {
             return "redirect:/users/";
         }
         return "redirect:/users/" + user.getId();
     }
 
     @RequestMapping(value = "/{id}/avatar", method = RequestMethod.GET)
     public ResponseEntity<byte[]> avatar(@PathVariable("id") Long id) {
         Avatar avatar = avatarRepository.load(id);
 
         HttpHeaders responseHeaders = new HttpHeaders();
         responseHeaders.set("Content-Type", avatar.getContentType());
 
         return new ResponseEntity<byte[]>(avatar.getContent(), responseHeaders, HttpStatus.OK);
     }
 
     @RequestMapping(value = "/throwException", method = RequestMethod.GET)
     public void throwException(@PathVariable("id") Long id) {
         throw new IllegalArgumentException("bl bla");
     }
 
     @RequestMapping(value = "/isEmailAvailable", method = RequestMethod.GET)
     @ResponseBody
     public String isEmailAvailable(@RequestParam String email) {
         User user = repository.findByEmail(new EmailAddress(email));
         if (user == null) {
             return Boolean.TRUE.toString();
        } else {
            return Boolean.FALSE.toString();
         }
     }
 
     public static class UserForm {
         @Valid
         private User user;
         @Valid
         private CommonsMultipartFile avatarFile;
 
         public UserForm() {
         }
 
         public UserForm(User user, CommonsMultipartFile avatarFile) {
             this.user = user;
             this.avatarFile = avatarFile;
         }
 
         public User getUser() {
             return user;
         }
 
         public void setUser(User user) {
             this.user = user;
         }
 
         public CommonsMultipartFile getAvatarFile() {
             return avatarFile;
         }
 
         public void setAvatarFile(CommonsMultipartFile avatarFile) {
             this.avatarFile = avatarFile;
         }
 
         public Avatar getAvatar() {
             if (avatarFile.isEmpty()) {
                 return null;
             }
             return new Avatar(avatarFile.getBytes(), avatarFile.getContentType());
         }
 
     }
 }
 
 
