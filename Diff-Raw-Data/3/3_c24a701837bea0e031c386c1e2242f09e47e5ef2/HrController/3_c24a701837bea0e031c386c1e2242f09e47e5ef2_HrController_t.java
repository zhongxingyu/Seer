 package hk.com.novare.tempoplus.accountsystem;
 
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.multipart.MultipartFile;
 
 @Controller
 @RequestMapping("hr")
 public class HrController {
 
 	@Autowired
 	@Qualifier("hrService")
 	HrService hrService;
 	
 	@Autowired
 	SingleFileUploadForm singleFileUploadForm;
 	

 	@RequestMapping(value = "/employeemanager")
 	public String subPage(ModelMap modelMap) {		
 		modelMap.addAttribute("employeeList", hrService.retrieveAllEmployee());
 		return "ViewHr";
 	}
 
 	@RequestMapping(value = "/selectAllJSON")
 	public @ResponseBody
 	List<EmployeePartialInfoDTO> subPage() {
 		return hrService.retrieveAllEmployee();
 
 	}
 	
 	@RequestMapping(value = "/searchEmployee")
 	public @ResponseBody
 	EmployeeFullInfoDTO searchEmployee( @RequestParam(value = "employeeId") String searchString) {
 		return hrService.searchEmployee(searchString, "employeeId");
 	}
 	
 	@RequestMapping(value = "/saveEditEmployee", method = RequestMethod.POST)
 	public String saveEditEmployee(ModelMap modelMap, 
 			@RequestParam(value = "firstName") String firstName,
 			@RequestParam(value = "middleName") String middleName,
 			@RequestParam(value = "lastName") String lastName,
 			@RequestParam(value = "employeeId") String employeeId,
 			@RequestParam(value = "biometrics") String biometrics,
 			@RequestParam(value = "department") String department,
 			@RequestParam(value = "position") String position,
 			@RequestParam(value = "shift") String shift,
 			@RequestParam(value = "level") String level,
 			@RequestParam(value = "hireDate") String hireDate,
 			@RequestParam(value = "regularizationDate") String regularizationDate,
 			@RequestParam(value = "resignationDate") String resignationDate,
 			@RequestParam(value = "supervisorName") String supervisorName,
 			@RequestParam(value = "supervisorEmail") String supervisorEmail,
 			@RequestParam(value = "employeeEmail") String employeeEmail) {
 		
 		
 		EmployeeFullInfoDTO employeeFullInfoDTO = new EmployeeFullInfoDTO();
 		
 		employeeFullInfoDTO.setFirstName(firstName);
 		employeeFullInfoDTO.setMiddleName(middleName);
 		employeeFullInfoDTO.setLastName(lastName);
 		employeeFullInfoDTO.setBiometrics(biometrics);
 		employeeFullInfoDTO.setEmployeeId(employeeId);
 		employeeFullInfoDTO.setDepartment(department);
 		employeeFullInfoDTO.setPosition(position);
 		employeeFullInfoDTO.setShift(shift);
 		employeeFullInfoDTO.setLevel(level);
 		employeeFullInfoDTO.setHiredDate(hireDate);
 		employeeFullInfoDTO.setRegularizationDate(regularizationDate);
 		employeeFullInfoDTO.setResignationDate(resignationDate);
 		employeeFullInfoDTO.setSupervisorName(supervisorName);
 		employeeFullInfoDTO.setSupervisorEmail(supervisorEmail);
 		employeeFullInfoDTO.setEmployeeEmail(employeeEmail);		
 		
 		hrService.saveEmployeeDetail(employeeFullInfoDTO);
 		
 		return "ViewHr";
 	}
 	
 	
 
 	@RequestMapping(value = "/createEmployee")
 	public String employeeForm(ModelMap model,
 			@RequestParam(value = "firstName") String firstName,
 			@RequestParam(value = "middleName") String middleName,
 			@RequestParam(value = "lastName") String lastName,
 			@RequestParam(value = "employeeId") String employeeId,
 			@RequestParam(value = "biometrics") String biometrics,
 			@RequestParam(value = "department") int departmentId,
 			@RequestParam(value = "position") int positionId,
 			@RequestParam(value = "shift") String shift,
 			@RequestParam(value = "level") String level,
 			@RequestParam(value = "hireDate") String hireDate,
 			@RequestParam(value = "supervisorName") String supervisorName,
 			@RequestParam(value = "supervisorEmail") String supervisorEmail,
 			@RequestParam(value = "employeeEmail") String employeeEmail) {
 		
 		EmployeeFullInfoDTO employeeFullInfoDTO = new EmployeeFullInfoDTO();
 		
 		employeeFullInfoDTO.setFirstName(firstName);
 		employeeFullInfoDTO.setMiddleName(middleName);
 		employeeFullInfoDTO.setLastName(lastName);
 		employeeFullInfoDTO.setEmployeeId(employeeId);
 		employeeFullInfoDTO.setBiometrics(biometrics);
 		employeeFullInfoDTO.setDepartmentId(departmentId);
 		employeeFullInfoDTO.setPositionId(positionId);
 		employeeFullInfoDTO.setShift(shift);
 		employeeFullInfoDTO.setLevel(level);
 		employeeFullInfoDTO.setHiredDate(hireDate);
 		employeeFullInfoDTO.setSupervisorEmail(supervisorEmail);
 		employeeFullInfoDTO.setSupervisorName(supervisorName);
 		employeeFullInfoDTO.setEmployeeEmail(employeeEmail);
 		hrService.createEmployeeDetail(employeeFullInfoDTO);
 		
 		return "ViewHr";
 	}
 	
 	@RequestMapping(value = "/retrieveDepartmentJSON")
 	public @ResponseBody
 	Map<Integer, String> retrieveDepartment() {
 		return hrService.retrieveDepartment();
 	}
 	
 	@RequestMapping(value = "/retrievePositionJSON")
 	public @ResponseBody
 	Map<Integer, String> retrievePosition(
 			@RequestParam(value = "department") int departmentId) {		
 		return hrService.retievePosition(departmentId);
 	}
 	
 	@RequestMapping(value = "/retrieveSupervisorJSON")
 	public @ResponseBody
 	Map<Integer, String> retrieveSupervisor(
 			@RequestParam(value = "department") int departmentId) {		
 		return hrService.retieveSupervisor(departmentId);
 	}
 	
 	/*@RequestMapping(value = "/uploadUserDB", method = RequestMethod.POST)
 	public String getFileUpload(
 			@ModelAttribute("uploadForm") SingleFileUploadForm uploadForm,
 			ModelMap modelMap) {
 
 		// upload file
 		final MultipartFile files = uploadForm.getFile();
 
 		// transform to direct category userDb
 		TransformFile tf;
 		tf = new UserDb();
 		List<EmployeeFullInfoDTO> pList = tf.toExcel(files);
 
 		// pass to add_profile.jsp
 		//modelMap.addAttribute("employeeList", hrService.retieveAllEmployee());
 		return "ViewHr";
 
 	}*/
 	
 }
