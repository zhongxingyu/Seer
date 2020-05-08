 package com.twobytes.master.controller;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.MessageSource;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.twobytes.master.form.CustomerForm;
 import com.twobytes.master.form.CustomerGridData;
 import com.twobytes.master.form.CustomerSearchForm;
 import com.twobytes.master.service.CustomerService;
 import com.twobytes.master.service.CustomerTypeService;
 import com.twobytes.master.service.DistrictService;
 import com.twobytes.master.service.ProvinceService;
 import com.twobytes.master.service.SubdistrictService;
 import com.twobytes.model.CustomGenericResponse;
 import com.twobytes.model.Customer;
 import com.twobytes.model.CustomerType;
 import com.twobytes.model.District;
 import com.twobytes.model.Employee;
 import com.twobytes.model.GridResponse;
 import com.twobytes.model.Province;
 import com.twobytes.model.Subdistrict;
 import com.twobytes.security.form.LoginForm;
 import com.twobytes.util.DocRunningUtil;
 
 @Controller
 public class CustomerController {
 	
 	@Autowired
 	private CustomerService customerService;
 	
 	@Autowired
 	private SubdistrictService sdService;
 	
 	@Autowired
 	private DistrictService districtService;
 	
 	@Autowired
 	private ProvinceService provinceService;
 	
 	@Autowired
 	private DocRunningUtil docRunningUtil;
 	
 	@Autowired
 	private CustomerTypeService customerTypeService;
 	
 	@Autowired
 	private MessageSource messages;
 	
 	public void setMessages(MessageSource messages) {
 		this.messages = messages;
 	}
 	
 	private String VIEWNAME_SEARCH = "customer.search";
 	private String VIEWNAME_FORM = "customer.form";
 	
 	@RequestMapping(value = "/customer")
 	public String view(ModelMap model, HttpServletRequest request) {
 		if(null == request.getSession().getAttribute("UserLogin")){
 			LoginForm loginForm = new LoginForm();
 			model.addAttribute(loginForm);
 			return "loginScreen";
 		}
 		CustomerSearchForm searchForm = new CustomerSearchForm();
 		model.addAttribute("searchForm", searchForm);
 		return VIEWNAME_SEARCH;
 	}
 	
 	@RequestMapping(value="/searchCustomer")
 	public @ResponseBody GridResponse getData(@RequestParam(value="name", required=false) String name, @RequestParam("rows") Integer rows, @RequestParam("page") Integer page, @RequestParam("sidx") String sidx, @RequestParam("sord") String sord){
 		// Because default Tomcat URI encoding is iso-8859-1 so it must encode back to tis620
 		try{
 			if(null != name){
 				name = new String(name.getBytes("iso-8859-1"), "tis620");	
 			}
 		}catch(UnsupportedEncodingException e){
 			e.printStackTrace();
 		}
 		List<Customer> customerList = new ArrayList<Customer>();
 		try{
 			customerList = customerService.selectByCriteria(name, rows, page, sidx, sord);
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		GridResponse response = new GridResponse();
 		
 		List<CustomerGridData> rowsList = new ArrayList<CustomerGridData>();
 		
 		Integer total_pages = 0;
 		if(customerList.size() > 0){
 			int endData = 0;
 			if(customerList.size() < (rows*page)){
 				endData = customerList.size();
 			}else{
 				endData = (rows*page);
 			}
 			for(int i=(rows*page - rows); i<endData; i++){
 				Customer customer = customerList.get(i);
 				CustomerGridData gridData = new CustomerGridData();
 				gridData.setCustomerID(customer.getCustomerID().toString());
 				gridData.setName(customer.getName());
 				gridData.setTel(customer.getTel());
 				gridData.setMobileTel(customer.getMobileTel());
 				if(customer.getZipcode() != null){
 					gridData.setAddress(customer.getAddress()+" "+this.messages.getMessage("subdistrict_abbr", null, new Locale("th", "TH"))+" "+customer.getSubdistrict().getName()+" "+this.messages.getMessage("district_abbr", null, new Locale("th", "TH"))+" "+customer.getDistrict().getName()+" "+this.messages.getMessage("province_abbr", null, new Locale("th", "TH"))+" "+customer.getProvince().getName()+" "+customer.getZipcode());
 				}else{
 					gridData.setAddress(customer.getAddress()+" "+this.messages.getMessage("subdistrict_abbr", null, new Locale("th", "TH"))+" "+customer.getSubdistrict().getName()+" "+this.messages.getMessage("district_abbr", null, new Locale("th", "TH"))+" "+customer.getDistrict().getName()+" "+this.messages.getMessage("province_abbr", null, new Locale("th", "TH"))+" "+customer.getProvince().getName());
 				}
 				gridData.setEmail(customer.getEmail());
 				
 				rowsList.add(gridData);
 			}
 			total_pages = new Double(Math.ceil(((double)customerList.size()/(double)rows))).intValue();
 		}
 				
 		if (page > total_pages) page=total_pages;
 		response.setPage(page.toString());
 		response.setRecords(String.valueOf(customerList.size()));
 		response.setTotal(total_pages.toString());
 		response.setRows(rowsList);
 		return response;
 	}
 	
 	@RequestMapping(value = "/customer", params = "do=preAdd")
 	public String preAdd(ModelMap model, HttpServletRequest request){
 		if(null == request.getSession().getAttribute("UserLogin")){
 			LoginForm loginForm = new LoginForm();
 			model.addAttribute(loginForm);
 			return "loginScreen";
 		}
 		CustomerForm form = new CustomerForm();
 		// set default province to Chanthaburi
 		form.setProvinceID(7);
 		// set default district to Muang
 		form.setDistrictID(160);
 		
 		List<Province> provinceList = provinceService.getAll();
 		
 		List<District> districtList = districtService.getByProvince(7);
 		
 		// set subdistrict from Muang district
 		List<Subdistrict> subdistrictList = sdService.getByDistrict(160);
 		Subdistrict sd = (Subdistrict)subdistrictList.get(0);
 		form.setSubdistrictID(sd.getSubdistrictID());
 		form.setZipcode(sd.getZipcode().toString());
 		
 		List<CustomerType> customerTypeList = customerTypeService.getAll();
 		
 		form.setCustomerTypeID(customerTypeList.get(0).getCustomerTypeID());
 		
 		model.addAttribute("provinceList", provinceList);
 		model.addAttribute("districtList", districtList);
 		model.addAttribute("subdistrictList", subdistrictList);
 		//model.addAttribute("zipcode", sd.getZipcode());
 		model.addAttribute("customerTypeList", customerTypeList);
 		model.addAttribute("form", form);
 		return VIEWNAME_FORM;
 	}
 	
 	@RequestMapping(value = "/customer", params = "do=save")
 	public String doSave(@ModelAttribute("form") CustomerForm form, HttpServletRequest request, ModelMap model){
 		if(null == request.getSession().getAttribute("UserLogin")){
 			LoginForm loginForm = new LoginForm();
 			model.addAttribute(loginForm);
 			return "loginScreen";
 		}
 		Date now = new Date();
 		Employee user = (Employee)request.getSession().getAttribute("UserLogin");
 		Customer customer = new Customer();
 		String msg = "";
 //		if(null != form.getCustomerID()){
 		if (!form.getCustomerID().equals("")) {
 			// update
 			try{
 				customer = customerService.selectByID(form.getCustomerID());
 			}catch(Exception e){
 				e.printStackTrace();
 				model.addAttribute("errMsg", e.getMessage());
 				List<Province> provinceList = provinceService.getAll();
 				List<District> districtList = districtService.getByProvince(form.getProvinceID());
 				List<Subdistrict> subdistrictList = sdService.getByDistrict(form.getDistrictID());
 				List<CustomerType> customerTypeList = customerTypeService.getAll();
 				
 				Subdistrict sd = (Subdistrict) sdService.selectByID(form.getSubdistrictID());
 				
 				model.addAttribute("provinceList", provinceList);
 				model.addAttribute("districtList", districtList);
 				model.addAttribute("subdistrictList", subdistrictList);
 				model.addAttribute("customerTypeList", customerTypeList);
 				model.addAttribute("zipcode", sd.getZipcode());
 				return VIEWNAME_FORM;
 			}
 			msg = this.messages.getMessage("msg.updateComplete", null, new Locale("th", "TH"));
 		}else{
 			// add
 //			customer.setCustomerID(docRunningUtil.genDoc("customer"));
 			customer.setCreatedBy(user.getEmployeeID());
 			customer.setCreatedDate(now);
 			msg = this.messages.getMessage("msg.addComplete", null, new Locale("th", "TH"));
 		}
 		CustomerType ct = customerTypeService.selectByID(form.getCustomerTypeID());
 		customer.setCustomerType(ct);
 		customer.setName(form.getName());
 		customer.setAddress(form.getAddress());
 		Subdistrict sd = sdService.selectByID(form.getSubdistrictID());
 		District district = districtService.selectByID(form.getDistrictID());
 		Province province = provinceService.selectByID(form.getProvinceID());
 		customer.setSubdistrict(sd);
 		customer.setDistrict(district);
 		customer.setProvince(province);
 		try{
 			if(form.getZipcode() != null && form.getZipcode() != ""){
 				if(form.getZipcode() == "-"){
 					customer.setZipcode(0);
 				}else{
 					customer.setZipcode(Integer.parseInt(form.getZipcode()));
 				}
 			}
 		}catch(Exception e){
 			customer.setZipcode(0);
 		}
 		customer.setTel(form.getTel());
 		customer.setMobileTel(form.getMobileTel());
 		customer.setEmail(form.getEmail());
 		
 		customer.setUpdatedBy(user.getEmployeeID());
 		customer.setUpdatedDate(now);
 //		boolean canSave;
 		String result = "false";
 		try{
 			result = customerService.save(customer);
 		}catch(Exception e){
 			e.printStackTrace();
 			model.addAttribute("errMsg", e.getMessage());
 			List<Province> provinceList = provinceService.getAll();
 			List<District> districtList = districtService.getByProvince(form.getProvinceID());
 			List<Subdistrict> subdistrictList = sdService.getByDistrict(form.getDistrictID());
 			List<CustomerType> customerTypeList = customerTypeService.getAll();
 			
 			//Subdistrict sd1 = (Subdistrict) sdService.selectByID(form.getSubdistrictID());
 			
 			model.addAttribute("provinceList", provinceList);
 			model.addAttribute("districtList", districtList);
 			model.addAttribute("subdistrictList", subdistrictList);
 			model.addAttribute("customerTypeList", customerTypeList);
 			//model.addAttribute("zipcode", sd1.getZipcode());
 			return VIEWNAME_FORM;
 		}
 		if (result.equals("false")) {
 			model.addAttribute("errMsg", this.messages.getMessage("error.cannotSave", null, new Locale("th", "TH")));
 			List<Province> provinceList = provinceService.getAll();
 			List<District> districtList = districtService.getByProvince(form.getProvinceID());
 			List<Subdistrict> subdistrictList = sdService.getByDistrict(form.getDistrictID());
 
 			//Subdistrict sd1 = (Subdistrict) sdService.selectByID(form.getSubdistrictID());
 			
 			List<CustomerType> customerTypeList = customerTypeService.getAll();
 			
 			model.addAttribute("provinceList", provinceList);
 			model.addAttribute("districtList", districtList);
 			model.addAttribute("subdistrictList", subdistrictList);
 			model.addAttribute("customerTypeList", customerTypeList);
 			//model.addAttribute("zipcode", sd1.getZipcode());
 			return VIEWNAME_FORM;
 		}
 		model.addAttribute("msg", msg);
 		CustomerSearchForm searchForm = new CustomerSearchForm();
 		model.addAttribute("searchForm", searchForm);
 		return VIEWNAME_SEARCH;
 	}
 	
 	@RequestMapping(value = "/customer", params = "do=preEdit")
 	public String preEdit(@RequestParam(value="customerID") String customerID, ModelMap model, HttpServletRequest request){
 		if(null == request.getSession().getAttribute("UserLogin")){
 			LoginForm loginForm = new LoginForm();
 			model.addAttribute(loginForm);
 			return "loginScreen";
 		}
 		Customer customer = new Customer();
 		try{
 			customer = customerService.selectByID(customerID);
 		}catch(Exception e){
 			e.printStackTrace();
 			model.addAttribute("errMsg", e.getMessage());
 			CustomerSearchForm searchForm = new CustomerSearchForm();
 			model.addAttribute("searchForm", searchForm);
 			return VIEWNAME_SEARCH;
 		}
 		CustomerForm form = new CustomerForm();
 		form.setCustomerID(customer.getCustomerID());
 		form.setCustomerTypeID(customer.getCustomerType().getCustomerTypeID());
 		form.setName(customer.getName());
 		form.setTel(customer.getTel());
 		form.setMobileTel(customer.getMobileTel());
 		form.setAddress(customer.getAddress());
 		form.setSubdistrictID(customer.getSubdistrict().getSubdistrictID());
 		form.setDistrictID(customer.getDistrict().getDistrictID());
 		form.setProvinceID(customer.getProvince().getProvinceID());
 		if(customer.getZipcode() != null){
 			if(customer.getZipcode() == 0){
 				form.setZipcode("-");
 			}else{
 				form.setZipcode(customer.getZipcode().toString());
 			}
 		}
 		form.setEmail(customer.getEmail());
 		
 		List<Province> provinceList = provinceService.getAll();
 		List<District> districtList = districtService.getByProvince(form.getProvinceID());
 		List<Subdistrict> subdistrictList = sdService.getByDistrict(form.getDistrictID());
 		List<CustomerType> customerTypeList = customerTypeService.getAll();
 		
 		model.addAttribute("form", form);
 		model.addAttribute("provinceList", provinceList);
 		model.addAttribute("districtList", districtList);
 		model.addAttribute("subdistrictList", subdistrictList);
 		model.addAttribute("customerTypeList", customerTypeList);
 		model.addAttribute("zipcode", customer.getSubdistrict().getZipcode());
 		return VIEWNAME_FORM;
 	}
 	
 	@RequestMapping(value = "/customer", params = "do=delete")
 	public @ResponseBody CustomGenericResponse delete(HttpServletRequest request){
 		CustomGenericResponse response = new CustomGenericResponse();
 		response.setSuccess(true);
 		try{
 			customerService.delete((String)request.getParameter("customerID"));
 			response.setMessage(this.messages.getMessage("msg.deleteSuccess", null, new Locale("th", "TH")));
 		}catch(Exception e){
 			e.printStackTrace();
 			response.setSuccess(false);
 			response.setMessage(this.messages.getMessage("error.cannotDelete", null, new Locale("th", "TH")));
 		}
 		return response;
 	}
 	
 	@RequestMapping(value = "/customer", params = "do=savePopup")
 	//@ModelAttribute("result")
 //	public boolean doSavePopup(@ModelAttribute("form") CustomerForm form, HttpServletRequest request, ModelMap model){
 	public @ResponseBody CustomGenericResponse doSavePopup(@ModelAttribute CustomerForm form, HttpServletRequest request, ModelMap model){
 		CustomGenericResponse response = new CustomGenericResponse();
 		if(null == request.getSession().getAttribute("UserLogin")){
 			LoginForm loginForm = new LoginForm();
 			model.addAttribute(loginForm);
 //			return "loginScreen";
 			response.setSuccess(false);
 			response.setMessage(this.messages.getMessage("error.cannotSave", null, new Locale("th", "TH")));
 			return response;
 //			return false;
 		}
 		// Because default Tomcat URI encoding is iso-8859-1 so it must encode back to tis620
 		try{
 			if(null != form.getName()){
 //				form.setName(new String(form.getName().getBytes("iso-8859-1"), "tis620"));
 				form.setName(new String(form.getName().getBytes("iso-8859-1"), "UTF-8"));
 			}
 			if(null != form.getAddress()){
 //				form.setAddress(new String(form.getAddress().getBytes("iso-8859-1"), "tis620"));
 				form.setAddress(new String(form.getAddress().getBytes("iso-8859-1"), "UTF-8"));
 			}
 			if(null != form.getTel()){
 //				form.setTel(new String(form.getTel().getBytes("iso-8859-1"), "tis620"));
 				form.setTel(new String(form.getTel().getBytes("iso-8859-1"), "UTF-8"));
 			}
 			if(null != form.getMobileTel()){
 //				form.setMobileTel(new String(form.getMobileTel().getBytes("iso-8859-1"), "tis620"));
 				form.setMobileTel(new String(form.getMobileTel().getBytes("iso-8859-1"), "UTF-8"));
 			}
 		}catch(UnsupportedEncodingException e){
 			e.printStackTrace();
 		}
 		Date now = new Date();
 		Employee user = (Employee)request.getSession().getAttribute("UserLogin");
 		Customer customer = new Customer();
 
 //		customer.setCustomerID(docRunningUtil.genDoc("customer"));
 //		System.out.println("form.getName() = "+form.getName());
 //		try {
 //			String name1 = new String(form.getName().getBytes("iso-8859-1"), "tis-620");
 //			String name2 = new String(form.getName().getBytes("iso-8859-1"), "UTF8");
 //			String name3 = new String(name2.getBytes("UTF8"), "tis620");
 //			String name4 = new String(form.getName().getBytes("UTF8"), "tis620");
 //			System.out.println("name 1 = "+name1);
 //			System.out.println("name 2 = "+name2);
 //			System.out.println("name 3 = "+name3);
 //			System.out.println("name 4 = "+name4);
 //		} catch (UnsupportedEncodingException e1) {
 //			// TODO Auto-generated catch block
 //			e1.printStackTrace();
 //		}
 //		System.out.println("After form.getName() = "+form.getName());
 //		System.out.println("form.getAddress() = "+form.getAddress());
 		CustomerType customerType = customerTypeService.selectByID(form.getCustomerTypeID());
 		customer.setCustomerType(customerType);
 		customer.setName(form.getName());
 		customer.setAddress(form.getAddress());
 		Subdistrict sd = sdService.selectByID(form.getSubdistrictID());
 		District district = districtService.selectByID(form.getDistrictID());
 		Province province = provinceService.selectByID(form.getProvinceID());
 		customer.setSubdistrict(sd);
 		customer.setDistrict(district);
 		customer.setProvince(province);
 		try{
 			if(form.getZipcode() != null && form.getZipcode() != ""){
 				if(form.getZipcode() == "-"){
 					customer.setZipcode(0);
 				}else{
 					customer.setZipcode(Integer.parseInt(form.getZipcode()));
 				}
 			}
 		}catch(Exception e){
 			customer.setZipcode(0);
 		}
 		customer.setTel(form.getTel());
 		customer.setMobileTel(form.getMobileTel());
 		customer.setEmail(form.getEmail());
 
 		customer.setCreatedBy(user.getEmployeeID());
 		customer.setCreatedDate(now);
 		customer.setUpdatedBy(user.getEmployeeID());
 		customer.setUpdatedDate(now);
 //		boolean canSave = false;
 		String result = "";
 		try{
			result = customerService.save(customer);
 			response.setData(result);
 		}catch(Exception e){
 			e.printStackTrace();
 //			model.addAttribute("errMsg", e.getMessage());
 //			List<Province> provinceList = provinceService.getAll();
 //			List<District> districtList = districtService.getByProvince(form.getProvinceID());
 //			List<Subdistrict> subdistrictList = sdService.getByDistrict(form.getDistrictID());
 //
 //			model.addAttribute("provinceList", provinceList);
 //			model.addAttribute("districtList", districtList);
 //			model.addAttribute("subdistrictList", subdistrictList);
 //			return VIEWNAME_FORM;
 //			return canSave;
 			response.setSuccess(false);
 			response.setMessage(this.messages.getMessage("error.cannotSave", null, new Locale("th", "TH"))+"\n"+e.getMessage());
 			return response;
 		}
 		if (result.equals("false")) {
 			response.setSuccess(false);
 			response.setMessage(this.messages.getMessage("error.cannotSave", null, new Locale("th", "TH")));
 			return response;
 //			model.addAttribute("errMsg", this.messages.getMessage("error.cannotSave", null, new Locale("th", "TH")));
 //			List<Province> provinceList = provinceService.getAll();
 //			List<District> districtList = districtService.getByProvince(form.getProvinceID());
 //			List<Subdistrict> subdistrictList = sdService.getByDistrict(form.getDistrictID());
 //
 //			model.addAttribute("provinceList", provinceList);
 //			model.addAttribute("districtList", districtList);
 //			model.addAttribute("subdistrictList", subdistrictList);
 //			return VIEWNAME_FORM;
 		//	return false;
 		}
 //		model.addAttribute("msg", msg);
 //		CustomerSearchForm searchForm = new CustomerSearchForm();
 //		model.addAttribute("searchForm", searchForm);
 //		return VIEWNAME_SEARCH;
 //		return canSave;
 		response.setSuccess(true);
 		response.setMessage(this.messages.getMessage("msg.addComplete", null, new Locale("th", "TH")));
 		return response;
 	}
 
 }
