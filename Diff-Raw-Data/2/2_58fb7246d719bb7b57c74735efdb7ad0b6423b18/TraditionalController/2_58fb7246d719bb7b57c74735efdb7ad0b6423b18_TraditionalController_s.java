 package demo.web;
 
 import java.security.Principal;
 import java.util.List;
 import java.util.Set;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.view.RedirectView;
 
 import demo.domain.Building;
 import demo.domain.Campus;
 import demo.domain.College;
 import demo.domain.Department;
 import demo.domain.Organization;
 import demo.domain.Person;
 import demo.domain.Room;
 import demo.service.BuildingService;
 import demo.service.CampusService;
 import demo.service.CollegeService;
 import demo.service.DepartmentService;
 import demo.service.FloorService;
 import demo.service.PersonService;
 import demo.service.RoomService;
 import demo.web.helper.FormBean;
 
 @Controller
 @RequestMapping(value = "/traditional")
 
 public class TraditionalController {
 
 	@Autowired private CampusService campusService;
 	@Autowired private CollegeService collegeService;
 	@Autowired private DepartmentService departmentService;
 	@Autowired private PersonService personService;
 	@Autowired private BuildingService buildingService;
 	@Autowired private FloorService floorService;
 	@Autowired private RoomService roomService;
 	
 	@RequestMapping("/")
 	public RedirectView index(FormBean formBean, Model model,  Principal principal) {
 		Campus campus = campusService.find(1);
 		Integer collegeId = campus.getChildOrganization().iterator().next().getId();
 		return new RedirectView("college/" + collegeId, true);
 	}
 	
 	@RequestMapping(value = "/college/{collegeId}", method = RequestMethod.GET)
 	public String college(@PathVariable Integer collegeId, FormBean formBean, Model model,  Principal principal) {
 		Campus campus = campusService.find(1);
 		model.addAttribute("campus", campus);
 		model.addAttribute("college", collegeService.find(collegeId));
 		model.addAttribute("formBean", formBean);
 
 		return "/traditional/index";
 	}
 	
 	@RequestMapping(value = "/college/{collegeId}", method = RequestMethod.POST)
 	public String collegePost(@PathVariable Integer collegeId, FormBean formBean, Model model,  Principal principal) {
 		Campus campus = campusService.find(1);
 		College college = collegeService.find(collegeId);
 		
 		// delete department if id exists
 		if(formBean.getDeletedId() != null) {
 			departmentService.delete(formBean.getDeletedId());
 		}
 		
 		// add new department
		if(formBean.getName() != null) {
 			Department department = new Department();
 			department.setName(formBean.getName());
 			department.setParentOrganization(college);
 			departmentService.save(department);
 		}
 		
 		model.addAttribute("campus", campus);
 		model.addAttribute("college", college);
 		model.addAttribute("formBean", formBean);
 
 		return "/traditional/index";
 	}
 	
 	@RequestMapping(value = "/department/{departmentId}", method = RequestMethod.GET)
 	public String department(@PathVariable Integer departmentId,  FormBean formBean, Model model,  Principal principal) {
 		Campus campus = campusService.find(1);
 		model.addAttribute("campus", campus);
 		model.addAttribute("department", departmentService.find(departmentId));
 		model.addAttribute("formBean", formBean);
 		
 		return "/traditional/index";
 	}
 	
 	@RequestMapping(value = "/department/{departmentId}", method = RequestMethod.POST)
 	public String departmentPost(@PathVariable Integer departmentId,  FormBean formBean, Model model,  Principal principal) {
 		Campus campus = campusService.find(1);
 		Department department = departmentService.find(departmentId);
 		
 		// delete person if id exists
 		if(formBean.getDeletedId() != null) {
 			Person person = personService.find(formBean.getDeletedId());
 			department.getEmployees().remove(person);
 			departmentService.save(department);
 		}
 		
 		// add new person
 		if(formBean.getPersonId() != null) {
 			Person person = personService.find(formBean.getPersonId());
 			department.getEmployees().add(person);
 			departmentService.save(department);
 		}
 		
 		model.addAttribute("campus", campus);
 		model.addAttribute("department", department);
 		model.addAttribute("formBean", formBean);
 		
 		return "/traditional/index";
 		
 	}
 	
 	@RequestMapping(value = "/person/{personId}", method = RequestMethod.GET)
 	public String person(@PathVariable Integer personId, FormBean formBean, Model model,  Principal principal) {
 		Campus campus = campusService.find(1);
 		Person person = personService.find(personId);
 		List<Room> personRooms = roomService.findBy(person);
 		
 		model.addAttribute("campus", campus);
 		model.addAttribute("person", person);
 		model.addAttribute("personRooms", personRooms);
 		model.addAttribute("formBean", formBean);
 
 		return "/traditional/index";
 	}
 
 	@RequestMapping(value = "/person/{personId}", method = RequestMethod.POST)
 	public String personPost(@PathVariable Integer personId, FormBean formBean, Model model,  Principal principal) {
 		Campus campus = campusService.find(1);
 		Person person = personService.find(personId);
 		// delete person if id exists
 		if(formBean.getDeletedId() != null) {
 			Room room = roomService.find(formBean.getDeletedId());
 			room.getOccuupant().remove(person);
 			roomService.save(room);
 		}
 		
 		// add new person
 		if(formBean.getRoomId() != null) {
 			Room room = roomService.find(formBean.getRoomId());
 			room.getOccuupant().add(person);
 			roomService.save(room);
 		}
 		
 		if(formBean.getShowAddRow() != null) {
 			model.addAttribute("buildings", campus.getChildFacility());
 			
 			Integer buildingId = formBean.getBuildingId();
 			if(buildingId == null) {
 				buildingId = campus.getChildFacility().iterator().next().getId();
 			}
 			
 			Building building = buildingService.find(buildingId);
 			Set<Organization> floors = building.getChildFacility();
 			model.addAttribute("floors", floors);
 			
 			Integer floorId = formBean.getFloorId();
 			
 			boolean restFloorId = true;
 			for (Organization organization : floors) {
 				if(organization.getId() == floorId) {
 					restFloorId = false;
 					break;
 				}
 			}
 			if(floorId == null || restFloorId) {
 				floorId = building.getChildFacility().iterator().next().getId();
 			}
 			model.addAttribute("rooms", floorService.find(floorId).getChildFacility());
 		}
 		
 		model.addAttribute("campus", campus);
 		model.addAttribute("person", person);
 		model.addAttribute("personRooms", roomService.findBy(person));
 		model.addAttribute("formBean", formBean);
 		return "/traditional/index";
 	}
 		
 }
