 package com.twistlet.falcon.controller;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.twistlet.falcon.model.entity.FalconStaff;
 import com.twistlet.falcon.model.entity.FalconUser;
 import com.twistlet.falcon.model.service.StaffService;
 
 @Controller
 public class ListStaffController {
 
 	private final StaffService staffService;
 
 	@Autowired
 	public ListStaffController(StaffService staffService) {
 		this.staffService = staffService;
 	}
 
 	@RequestMapping("/list-staff/{admin}/{date}")
 	@ResponseBody
 	public List<FalconStaff> listStaffs(@PathVariable String admin, @PathVariable(value="date") String date) {
 		List<FalconStaff> staffs = staffService.listStaffByAdmin(admin);
 		for (FalconStaff staff : staffs) {
 			staff.setFalconUser(null);
 			staff.setFalconAppointments(null);
 		}
 		return staffs;
 	}
 	
 	
 	@RequestMapping("/list-staff/{admin}/{date}/{startTime}/{endTime}")
 	@ResponseBody
 	public Set<FalconStaff> listAvailableStaffs(@PathVariable String admin,
 			@PathVariable(value="date") String date,
 			@PathVariable("startTime") String start,
 			@PathVariable("endTime") String end) {
 		final SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy HHmm");
 		FalconUser falconUser = new FalconUser();
 		falconUser.setUsername(admin);
 		Set<FalconStaff> staffs = new HashSet<>();
 		try {
 			final Date startDate = sdf.parse(date + " " + start);
 			final Date endDate = sdf.parse(date + " " + end);
 			staffs = staffService.listAvailableStaff(falconUser, startDate, endDate);
 			for (FalconStaff staff : staffs) {
 				staff.setFalconUser(null);
 				staff.setFalconAppointments(null);
 			}
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 		return staffs;
 	}
 	
 	
 
 	@RequestMapping("/list-staff-name/{admin}/{date}")
 	@ResponseBody
 	public List<String> listStaffNames(@PathVariable("admin") String username, @PathVariable("date") String date,
 			@RequestParam("term") String name) {
 		FalconUser admin = new FalconUser();
 		admin.setUsername(username);
 		List<FalconStaff> staffs = staffService.listStaffByAdminNameLike(admin,
 				name);
 		List<String> names = new ArrayList<>();
 		for (FalconStaff staff : staffs) {
 			names.add(staff.getName() + " (" + staff.getHpTel() + ")");
 		}
 		return names;
 	}
 
 	@RequestMapping("/list-staff-nric/{admin}/{date}")
 	@ResponseBody
 	public List<String> listStaffNric(@PathVariable("admin") String username, @PathVariable("date") String date,
 			@RequestParam("term") String nric) {
 		FalconUser admin = new FalconUser();
 		admin.setUsername(username);
 		List<FalconStaff> staffs = staffService.listStaffByAdminNricLike(admin,
 				nric);
 		List<String> nrics = new ArrayList<>();
 		for (FalconStaff staff : staffs) {
 			nrics.add(staff.getNric());
 		}
 		return nrics;
 	}
 
 	@RequestMapping("/list-staff-email/{admin}/{date}")
 	@ResponseBody
 	public List<String> listStaffEmail(@PathVariable("admin") String username, @PathVariable("date") String date,
 			@RequestParam("term") String email) {
 		FalconUser admin = new FalconUser();
 		admin.setUsername(username);
 		List<FalconStaff> staffs = staffService.listStaffByAdminEmailLike(
 				admin, email);
 		List<String> emails = new ArrayList<>();
 		for (FalconStaff staff : staffs) {
 			emails.add(staff.getEmail());
 		}
 		return emails;
 	}
 	
 	@RequestMapping("/list-staff-mobile/{admin}/{date}")
 	@ResponseBody
 	public List<String> listStaffMobile(@PathVariable("admin") String username, @PathVariable("date") String date,
 			@RequestParam("term") String mobile) {
 		FalconUser admin = new FalconUser();
 		admin.setUsername(username);
 		List<FalconStaff> staffs = staffService.listStaffByAdminMobileLike(admin, mobile);
 		List<String> mobiles = new ArrayList<>();
 		for (FalconStaff staff : staffs) {
 			mobiles.add(staff.getHpTel());
 		}
 		return mobiles;
 	}
 
 	@RequestMapping("/search-staff/{admin}/{date}")
 	@ResponseBody
 	public FalconStaff searchStaff(@PathVariable("admin") String username, @PathVariable("date") String date,
 			@RequestParam(value = "name", required = false) String name,
 			@RequestParam(value = "mobile", required = false) String mobile,
 			@RequestParam(value = "nric", required = false) String nric,
 			@RequestParam(value = "email", required = false) String email) {
 		FalconUser admin = new FalconUser();
 		admin.setUsername(username);
 		FalconStaff staff = new FalconStaff();
 		staff.setEmail(email);
 		staff.setName(name);
 		staff.setHpTel(mobile);
 		staff.setNric(nric);
 		List<FalconStaff> staffs = staffService.listStaffByAdminStaffLike(admin, staff);
 		FalconStaff matchingStaff = null;
 		if (CollectionUtils.size(staffs) == 1) {
 			matchingStaff = staffs.get(0);
 			matchingStaff.setFalconAppointments(null);
 			matchingStaff.setFalconUser(null);
 		}
 		return matchingStaff;
 
 	}
 	
 	
 	@RequestMapping("/validate-staff")
 	@ResponseBody
 	public String validateStaff(final HttpServletRequest request) {
 		final String stringId = request.getParameter("fieldId");
 		final String value = request.getParameter("fieldValue");
		final String id = request.getParameter("id-staff");
 		FalconStaff staff = new FalconStaff();
 		FalconUser admin = new FalconUser();
 		if("identificationnum-staff".equals(stringId)){
 			staff.setNric(value);
 		}else if("mobilenum-staff".equals(stringId)){
 			staff.setHpTel(value);
 		}else if("email-staff".equals(stringId)){
 			staff.setEmail(value);
 		}
 		boolean isValid = true;
 		List<FalconStaff> staffs = staffService.listStaffByAdminStaffLike(admin, staff);
 		if(CollectionUtils.isNotEmpty(staffs)){
			//check if current id passed is equal to retrieved id. Valid is id is equal
			for(FalconStaff theStaff : staffs){
				if(StringUtils.isNotBlank(id)){
					if(theStaff.getId().equals(Integer.valueOf(id))){
						break;
					}
				}
				isValid = false;
			}
 		}
 		return "[\""+ stringId + "\", " + isValid +"]";
 	}
 }
