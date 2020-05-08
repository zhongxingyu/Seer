 /**
  * Written by Long Nguyen <chautinhlong@gmail.com>
  * FREE FOR ALL BUT DOES NOT MEAN THERE IS NO PRICE.
  */
 package mantech.controller;
 
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import mantech.domain.Department;
 import mantech.domain.User;
 import mantech.domain.UserRole;
 import mantech.repository.DepartmentRepository;
 import mantech.repository.UserRepository;
 import mantech.repository.UserRoleRepository;
 
 /**
  * @author Long Nguyen
  * @version $Id: UserController.java,v 1.0 Sep 9, 2011 3:59:57 AM nguyenlong Exp $
  */
 @Controller
 public class UserController {
 
   @Autowired
   private UserRepository userRepo;
   
   @Autowired
   private UserRoleRepository roleRepo;
   
   @Autowired
   private DepartmentRepository departmentRepo;
   
   @RequestMapping(value = {"/user", "/user/list"}, method = RequestMethod.GET)
   public String list(ModelMap model){
     List<User> users = userRepo.findAll();
     model.addAttribute("listUser", users);
     return "user/list";
   }
   
   @RequestMapping(value="/user/add", method = RequestMethod.GET)
   public String insert(ModelMap model) {
     List<Department> departs = departmentRepo.findAll();
     List<UserRole> roles = roleRepo.findAll();
     
     model.addAttribute("departList", departs);
     model.addAttribute("roleList", roles);
     return "user/add";
   }
   
   @RequestMapping(value = "/user/addSave", method = RequestMethod.POST)
   public String insertSave(@RequestParam(value = "username") String username,
                             @RequestParam(value = "passwd") String pass,
                             @RequestParam(value = "email") String email,
                             @RequestParam(value = "department") byte departId,
                             @RequestParam(value = "role") byte roleId,
                             @RequestParam(value = "firstName") String firstName,
                             @RequestParam(value = "lastName") String lastName,
                             @RequestParam(value = "gender") String gender,
                             @RequestParam(value = "address") String address,
                             ModelMap model) {
     
     Department department = departmentRepo.get(departId);
     UserRole role = roleRepo.get(roleId);
     
 //    if (userRepo.isExistUser(100)) {
 //      model.addAttribute("msg", "The User ID is used.");
 //      //A(??)
 //      return "msg";
 //    }
     
     User user = new User();
     user.setUsername(username);
     user.setPassword(pass);
     user.setEmail(email); 
     user.setFirstName(firstName);
     user.setLastName(lastName);
     user.setGender(gender);
     user.setDepartment(department);
     user.setHomeAddress(address);
     user.setRole(role);
     
     userRepo.save(user);
     
 //    model.addAttribute("msg", "Insert thanh cong!");
     return "redirect:/user/list";
   }
   
   @RequestMapping(value = "/user/edit", method = RequestMethod.GET)
   public String update(@RequestParam("uId") int id, ModelMap modal) {
     List<Department> departs = departmentRepo.findAll();
     List<UserRole> roles = roleRepo.findAll();
     User user = userRepo.get(id);
     Department depart = user.getDepartment();
     modal.addAttribute("departList", departs);
     modal.addAttribute("roleList", roles);
     modal.addAttribute("user", user);
     modal.addAttribute("depart", depart);
     return "user/edit";
   }
   
   @RequestMapping(value = "/user/editSave", method = RequestMethod.POST)
   public String updateSave(@RequestParam("uid") int userId, 
                       @RequestParam("email") String email,
                       @RequestParam("department") byte depart,
                       @RequestParam("role") byte role,
                       @RequestParam("address") String address) {
     User user = userRepo.get(userId);
     user.setEmail(email);
     Department department = departmentRepo.get(depart);
     user.setDepartment(department);
     UserRole uRole = roleRepo.get(role);
     user.setRole(uRole);
     user.setHomeAddress(address);
     userRepo.save(user);
     return "msg";
   }
 
   @RequestMapping(value = "/user/search", method = RequestMethod.POST)
   public String search(@RequestParam("q") String searchText,
      @RequestParam("yourChoice") String choice, ModelMap model) {
 
//    List<User> users = StringUtils.isNotBlank(searchText.trim())
//        ? userRepo.searchByUsername(searchText)
//        : userRepo.findAll();
     List<User> users = null;
    if (choice.equals("1") && StringUtils.isNotBlank(searchText.trim())) {
       users = userRepo.searchByUsername(searchText);
     }
    else if (choice.equals("2")&& StringUtils.isNotBlank(searchText.trim())) {
       users = userRepo.searchByDepartment(searchText);
     }
     else {
       users = userRepo.findAll();
     }
     
     if (users.size() != 0) {
       model.addAttribute("listUser", users);
       return "/user/search";
     }
     else {
       return "null";
     }
   }
 
 }
