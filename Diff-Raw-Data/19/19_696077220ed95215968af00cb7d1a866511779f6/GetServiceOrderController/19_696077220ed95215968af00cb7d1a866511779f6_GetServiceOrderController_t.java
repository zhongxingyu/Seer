 package com.twobytes.repair.controller;
 
 import java.io.UnsupportedEncodingException;
 import java.text.SimpleDateFormat;
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
 
 import com.twobytes.master.service.TypeService;
 import com.twobytes.model.CustomGenericResponse;
 import com.twobytes.model.Customer;
 import com.twobytes.model.Employee;
 import com.twobytes.model.GridResponse;
 import com.twobytes.model.ServiceOrder;
 import com.twobytes.model.Type;
 import com.twobytes.repair.form.ServiceOrderForm;
 import com.twobytes.repair.form.ServiceOrderGridData;
 import com.twobytes.repair.form.ServiceOrderSearchForm;
 import com.twobytes.repair.service.ServiceOrderService;
 import com.twobytes.security.form.LoginForm;
 
 @Controller
 public class GetServiceOrderController {
 	
 	@Autowired
 	private ServiceOrderService soService;
 	
 	@Autowired
 	private TypeService typeService;
 	
 	@Autowired
 	private MessageSource messages;
 	
 	public void setMessages(MessageSource messages) {
 		this.messages = messages;
 	}
 	
 	private String VIEWNAME_SEARCH = "getServiceOrder.search";
 	private String VIEWNAME_FORM = "getServiceOrder.form";
 	
 //	private SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("th", "TH"));
 	private SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale ( "US" ));
 	
 	@RequestMapping(value = "/getServiceOrder")
 	public String view(ModelMap model, HttpServletRequest request) {
 		if(null == request.getSession().getAttribute("UserLogin")){
 			LoginForm loginForm = new LoginForm();
 			model.addAttribute(loginForm);
 			return "loginScreen";
 		}
 		ServiceOrderSearchForm searchForm = new ServiceOrderSearchForm();
 		model.addAttribute("searchForm", searchForm);
 		List<Type> typeList = new ArrayList<Type>();
 		try {
 			typeList = typeService.getAll();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		model.addAttribute("typeList", typeList);
 		return VIEWNAME_SEARCH;
 	}
 	
 	@RequestMapping(value="/searchGetServiceOrder")
 	public @ResponseBody GridResponse getData(@RequestParam(value="name", required=false) String name, @RequestParam(value="date", required=false) String date, @RequestParam(value="type", required=false) String type, @RequestParam("rows") Integer rows, @RequestParam("page") Integer page, @RequestParam("sidx") String sidx, @RequestParam("sord") String sord){
 		// Because default Tomcat URI encoding is iso-8859-1 so it must encode back to tis620
 		String[] datePart;
 		String searchDate = null;
 		try{
 			if(null != name){
 				name = new String(name.getBytes("iso-8859-1"), "tis620");	
 			}
 //			if(null != surname){
 //				surname = new String(surname.getBytes("iso-8859-1"), "tis620");	
 //			}
 			if(null != date && !date.equals("")){
 				date = new String(date.getBytes("iso-8859-1"), "tis620");
 				datePart = date.split("/");
 				// Change year to Christ year
 //				Integer year = Integer.parseInt(datePart[2]);				
 //				year = year - 543;
 				searchDate = datePart[2]+"-"+datePart[1]+"-"+datePart[0];
 			}
 			if(null != type){
 				type = new String(type.getBytes("iso-8859-1"), "tis620");	
 			}
 		}catch(UnsupportedEncodingException e){
 			e.printStackTrace();
 		}
 		
 		List<ServiceOrder> soList = soService.selectNewSOByCriteria(name, searchDate, type, rows, page, sidx, sord);
 		GridResponse response = new GridResponse();
 		
 		List<ServiceOrderGridData> rowsList = new ArrayList<ServiceOrderGridData>();
 		
 		Integer total_pages = 0;
 		if(soList.size() > 0){
 			int endData = 0;
 			if(soList.size() < (rows*page)){
 				endData = soList.size();
 			}else{
 				endData = (rows*page);
 			}
 			for(int i=(rows*page - rows); i<endData; i++){
 				ServiceOrder so = soList.get(i);
 				ServiceOrderGridData gridData = new ServiceOrderGridData();
 				gridData.setServiceOrderID(so.getServiceOrderID());
 				gridData.setServiceOrderDate(sdfDateTime.format(so.getServiceOrderDate()));
 				Customer customer = so.getCustomer();
 				gridData.setName(customer.getName());
 //				gridData.setSurname(customer.getSurname());
 				gridData.setTel(customer.getTel());
 				gridData.setMobileTel(customer.getMobileTel());
 				gridData.setStatus(so.getStatus());
				gridData.setProductID(so.getProduct().getProductID());
				gridData.setTypeID(so.getProduct().getType().getTypeID());
				gridData.setType(so.getProduct().getType().getName());
				gridData.setBrandID(so.getProduct().getBrand().getBrandID());
				gridData.setBrand(so.getProduct().getBrand().getName());
				gridData.setModelID(so.getProduct().getModel().getModelID());
				gridData.setModel(so.getProduct().getModel().getName());
				gridData.setSerialNo(so.getProduct().getSerialNo());
				gridData.setAccessories(so.getAccessories());
				gridData.setProblem(so.getProblem());
				gridData.setDescription(so.getDescription());
				gridData.setEmpOpen(so.getEmpOpen().getName() + " "
						+ so.getEmpOpen().getSurname());

				if(so.getEmpFix() != null){
					gridData.setEmpFix(so.getEmpFix().getName() + " "
						+ so.getEmpFix().getSurname());
				}

 				rowsList.add(gridData);
 			}
 			total_pages = new Double(Math.ceil(((double)soList.size()/(double)rows))).intValue();
 		}
 		
 		if (page > total_pages) page=total_pages;
 		response.setPage(page.toString());
 		response.setRecords(String.valueOf(soList.size()));
 		response.setTotal(total_pages.toString());
 		response.setRows(rowsList);
 		return response;
 	}
 	
 	@RequestMapping(value = "/getServiceOrder", params = "do=preview")
 	public String preview(@RequestParam(value="serviceOrderID") String serviceOrderID, ModelMap model, HttpServletRequest request){
 		if (null == request.getSession().getAttribute("UserLogin")) {
 			LoginForm loginForm = new LoginForm();
 			model.addAttribute(loginForm);
 			return "loginScreen";
 		}
 		
 		ServiceOrder so = soService.selectByID(serviceOrderID);
 		ServiceOrderForm form = new ServiceOrderForm();
 		form.setServiceOrderID(so.getServiceOrderID());
 		form.setServiceOrderDate(sdfDateTime.format(so.getServiceOrderDate()));
 		form.setServiceType(so.getServiceType());
 		if(so.getAppointmentDate() != null){
 			form.setAppointmentDate(sdfDateTime.format(so.getAppointmentDate()));
 		}
 		form.setRefJobID(so.getRefJobID());
 		form.setRefServiceOrder(so.getRefServiceOrder());
 		form.setCustomerType(so.getCustomerType());
 		form.setCustomerID(so.getCustomer().getCustomerID().toString());
 		form.setDeliveryCustomer(so.getDeliveryCustomer());
 		form.setDeliveryEmail(so.getDeliveryEmail());
 		form.setDeliveryMobileTel(so.getDeliveryMobileTel());
 		form.setDeliveryTel(so.getDeliveryTel());
 		form.setProductID(so.getProduct().getProductID());
 		form.setTypeID(so.getProduct().getType().getTypeID());
 		form.setBrandID(so.getProduct().getBrand().getBrandID());
 		form.setModel(so.getProduct().getModel().getName());
 		form.setSerialNo(so.getProduct().getSerialNo());
 		form.setAccessories(so.getAccessories());
 		form.setDesc(so.getDescription());
 		form.setProblem(so.getProblem());
 		form.setStatus(so.getStatus());
 
 		model.addAttribute("form", form);
 
 		model.addAttribute("customer", so.getCustomer());
 
 		model.addAttribute(
 				"fullAddr",
 				so.getCustomer().getAddress()
 						+ " "
 						+ this.messages.getMessage("subdistrict_abbr", null,
 								new Locale("th", "TH"))
 						+ " "
 						+ so.getCustomer().getSubdistrict().getName()
 						+ " "
 						+ this.messages.getMessage("district_abbr", null,
 								new Locale("th", "TH"))
 						+ " "
 						+ so.getCustomer().getDistrict().getName()
 						+ " "
 						+ this.messages.getMessage("province_abbr", null,
 								new Locale("th", "TH")) + " "
 						+ so.getCustomer().getProvince().getName());
 
 		model.addAttribute("product", so.getProduct());		
 		
 		return VIEWNAME_FORM;
 	}
 	
 	@RequestMapping(value = "/getServiceOrder", params = "do=getServiceOrder")
 	public @ResponseBody CustomGenericResponse getServiceOrder(HttpServletRequest request){
 		Employee user = (Employee)request.getSession().getAttribute("UserLogin");
 		CustomGenericResponse response = new CustomGenericResponse();
 		response.setSuccess(true);
 		response.setMessage(this.messages.getMessage("msg.getServiceOrder_success", null, new Locale("th", "TH")));
 		
 		boolean success = true;
 		try{
 			Date now = new Date();
 			ServiceOrder so = soService.selectByID(request.getParameter("serviceOrderID"));
 			
 			so.setEmpFix(user);
 			so.setStartFix(now);
 			so.setStatus(ServiceOrder.FIXING);
 			so.setUpdatedBy(user.getEmployeeID());
 			so.setUpdatedDate(now);
 			success = soService.edit(so);
 		}catch(Exception e){
 			response.setSuccess(false);
 			response.setMessage(this.messages.getMessage("msg.getServiceOrder_failure", null, new Locale("th", "TH")));
 			success = false;
 		}
 		if(!success){
 			response.setSuccess(false);
 			response.setMessage(this.messages.getMessage("msg.getServiceOrder_failure", null, new Locale("th", "TH")));
 		}
 		return response;
 	}
 
 	@RequestMapping(value = "/getServiceOrder", params = "do=accept")
 	public String accept(@ModelAttribute("form") ServiceOrderForm form,
 			HttpServletRequest request, ModelMap model, @RequestParam String mode) {
 		if (null == request.getSession().getAttribute("UserLogin")) {
 			LoginForm loginForm = new LoginForm();
 			model.addAttribute(loginForm);
 			return "loginScreen";
 		}
 		
 		Employee user = (Employee)request.getSession().getAttribute("UserLogin");
 		Date now = new Date();
 		ServiceOrder so = soService.selectByID(request.getParameter("serviceOrderID"));
 		
 		so.setEmpFix(user);
 		so.setStartFix(now);
 		so.setStatus(ServiceOrder.FIXING);
 		so.setUpdatedBy(user.getEmployeeID());
 		so.setUpdatedDate(now);
 		
 		String msg = "";
 		try {
 			soService.edit(so);
 			msg = this.messages.getMessage("msg.getServiceOrder_success", null,
 					new Locale("th", "TH"));
 		}catch(Exception e){
 			e.printStackTrace();
 			model.addAttribute("form", form);
 			
 			msg = this.messages.getMessage("msg.getServiceOrder_failure", null,
 					new Locale("th", "TH"));
 			
 			model.addAttribute("errMsg", msg);
 			return VIEWNAME_FORM;
 		}
 		model.addAttribute("msg", msg);
 		
 		ServiceOrderSearchForm searchForm = new ServiceOrderSearchForm();
 		model.addAttribute("searchForm", searchForm);
 		List<Type> typeList = new ArrayList<Type>();
 		try {
 			typeList = typeService.getAll();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		model.addAttribute("typeList", typeList);
 		
 		return VIEWNAME_SEARCH;
 	}
 }
