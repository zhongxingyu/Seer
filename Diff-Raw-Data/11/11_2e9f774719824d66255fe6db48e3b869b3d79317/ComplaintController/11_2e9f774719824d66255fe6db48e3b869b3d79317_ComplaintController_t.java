 /**
  * Written by Long Nguyen <chautinhlong@gmail.com>
  * FREE FOR ALL BUT DOES NOT MEAN THERE IS NO PRICE.
  */
 package mantech.controller;
 
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.SessionAttributes;
 
 import mantech.domain.CategoryPriority;
 import mantech.domain.Complaint;
 import mantech.domain.ComplaintStatus;
 import mantech.domain.Equipment;
 import mantech.domain.User;
 import mantech.repository.CategoryPriorityRepository;
 import mantech.repository.ComplaintRepository;
 import mantech.repository.ComplaintStatusRepository;
 import mantech.repository.EquipmentRepository;
 import mantech.repository.UserRepository;
 import mantech.service.ComplaintService;
 
 import net.lilylnx.springnet.core.exception.ValidationException;
 
 /**
  * @author Long Nguyen
  * @version $Id: ComplaintController.java,v 1.0 Sep 9, 2011 12:56:51 AM nguyenlong Exp $
  */
 @Controller
 @SessionAttributes("complaint")
 public class ComplaintController {
   
   @Autowired
   private ComplaintService complaintService;
 
   @Autowired
   private ComplaintRepository complaintRepo;
   
   @Autowired
   private EquipmentRepository equipmentRepo;
   
   @Autowired
   private UserRepository userRepo;
   
   @Autowired
   private ComplaintStatusRepository statusRepo;
   
   @Autowired
   private CategoryPriorityRepository priorityRepo;
 
   @RequestMapping(value = {"/complaint", "/complaint/list"}, method = RequestMethod.GET)
  public String list(ModelMap model) throws Exception {
     List<Complaint> listAllComplaint = complaintRepo.findAll();
     List<ComplaintStatus> status = statusRepo.findAll();
     List<CategoryPriority> priority = priorityRepo.findAll();
     
     model.addAttribute("listAll", listAllComplaint);
     model.addAttribute("listStatus", status);
     model.addAttribute("listPriority", priority);
     return "complaint/list";
   }
 
   public int noOfComplaintInPeriod() {
     return complaintRepo.count().intValue();
   }
 
   public List<Complaint> listComplaintDaily(int i) {
     return complaintRepo.findRange(new int[] { i - 1, i + 2 }, true, "id");
   }
 
   @RequestMapping(value = "/complaint/add", method = RequestMethod.GET)
   public String insert(@RequestParam(value = "uid") int id, ModelMap model){
     User user = userRepo.get(id);
     model.addAttribute("user", user);
     List<Equipment> equip = equipmentRepo.findAll();
     model.addAttribute("list", equip);
     return "/complaint/add";
   }
   
   @RequestMapping(value = "/complaint/addSave", method = RequestMethod.POST)
   public String insertSave(@RequestParam(value = "equipId") int equipId,
       @RequestParam(value = "title") String title,
       @RequestParam(value = "content") String content, ModelMap model)
   {
     Complaint complaint = new Complaint();
     User user = userRepo.get(2);
     Equipment equipment = equipmentRepo.get(equipId);
     CategoryPriority priority = equipment.getCategory().getPriority();
     complaint.setUser(user);
     complaint.setEquipment(equipment);
     complaint.setPriority(priority);
     complaint.setTitle(title);
     complaint.setContent(content);
     try {
       complaintService.insert(complaint);
     }
     catch (ValidationException e) {
       model.addAttribute("complaint", complaint);
       model.addAttribute("errorMsg", e.getMessage());
       return insert(user.getId(), model);
     }
     catch (Exception e) {
       model.addAttribute("errorMsg", e.getMessage());
       e.printStackTrace();
     }
     return "redirect:/complaint/list";
   }
   
   @RequestMapping(value = "/complaint/edit", method = RequestMethod.GET)
   public String update(ModelMap model) {
     Complaint complaint = complaintRepo.get(1);
     
     List<CategoryPriority> priority = priorityRepo.findAll(false, "id");
     List<ComplaintStatus> status = statusRepo.findAll(false, "id");
     
     model.addAttribute("complaint", complaint);
     model.addAttribute("listStatus", status);
     model.addAttribute("listPriority", priority);
     return "/complaint/edit";
   }
   
   @RequestMapping(value = "/complaint/editSave", method = RequestMethod.POST)
   public String updateSave(@RequestParam("status") byte statusId, 
       @RequestParam("priority") byte priorityId, ModelMap model)
   {
     Complaint complaint = (Complaint)model.get("complaint");
     ComplaintStatus status = statusRepo.get(statusId);
     CategoryPriority priority = priorityRepo.get(priorityId);
 
     complaint.setStatus(status);
     complaint.setPriority(priority);
     complaintRepo.update(complaint);
     return "redirect:/complaint/list";
   }
   
   @RequestMapping(value = "/complaint/search", method = RequestMethod.POST)
   public String search( @RequestParam("q") String searchText,
                         @RequestParam("f") byte choice,
                         @RequestParam(value="dateFrom", required=false) Date dateFrom,
                         @RequestParam(value="dateTo", required=false) Date dateTo,
                         @RequestParam(value="status") byte status,
                         @RequestParam(value="priority") byte priority,
                         ModelMap model) {
     List<Complaint> complaints = null;
     searchText = StringUtils.isBlank(searchText) ? null : searchText.trim();
     
     switch (choice) {
       case 1: 
         complaints = complaintRepo.search(searchText, null, dateFrom, dateTo, status, priority);
         break;
       case 2: complaints = complaintRepo.search(null, searchText, dateFrom, dateTo, status, priority); break;
       default: complaints = complaintRepo.search(null, null, dateFrom, dateTo, status, priority); break;
     }
    
     if (complaints.size() != 0) {
       model.addAttribute("listAll", complaints);
       return "/complaint/search";
     }
     else {
       return "null";
     }
   }
 }
