 package com.twobytes.repair.controller;
 
 import java.io.UnsupportedEncodingException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.MessageSource;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.twobytes.express.model.Armas;
 import com.twobytes.express.service.ArmasService;
 import com.twobytes.master.form.CustomerForm;
 import com.twobytes.master.form.ProductForm;
 import com.twobytes.master.service.BrandService;
 import com.twobytes.master.service.CustomerService;
 import com.twobytes.master.service.CustomerTypeService;
 import com.twobytes.master.service.DistrictService;
 import com.twobytes.master.service.DocRunningService;
 import com.twobytes.master.service.EmployeeService;
 import com.twobytes.master.service.ModelService;
 import com.twobytes.master.service.ProductService;
 import com.twobytes.master.service.ProvinceService;
 import com.twobytes.master.service.SubdistrictService;
 import com.twobytes.master.service.TypeService;
 import com.twobytes.model.Brand;
 import com.twobytes.model.CustomGenericResponse;
 import com.twobytes.model.Customer;
 import com.twobytes.model.CustomerType;
 import com.twobytes.model.District;
 import com.twobytes.model.Employee;
 import com.twobytes.model.GridResponse;
 import com.twobytes.model.Model;
 import com.twobytes.model.Product;
 import com.twobytes.model.Province;
 import com.twobytes.model.ServiceOrder;
 import com.twobytes.model.Subdistrict;
 import com.twobytes.model.Type;
 import com.twobytes.repair.form.ServiceOrderDocForm;
 import com.twobytes.repair.form.ServiceOrderForm;
 import com.twobytes.repair.form.ServiceOrderGridData;
 import com.twobytes.repair.form.ServiceOrderSearchForm;
 import com.twobytes.repair.service.ServiceOrderService;
 import com.twobytes.security.form.LoginForm;
 import com.twobytes.util.DocRunningUtil;
 
 @Controller
 public class ServiceOrderController {
 
 	@Autowired
 	private ServiceOrderService soService;
 
 	@Autowired
 	private TypeService typeService;
 
 	@Autowired
 	private BrandService brandService;
 
 	@Autowired
 	private ModelService modelService;
 	
 	@Autowired
 	private SubdistrictService sdService;
 
 	@Autowired
 	private DistrictService districtService;
 
 	@Autowired
 	private ProvinceService provinceService;
 
 	@Autowired
 	private CustomerService customerService;
 
 	@Autowired
 	private DocRunningService docRunningService;
 
 	@Autowired
 	private EmployeeService empService;
 
 	@Autowired
 	private ProductService productService;
 	
 	@Autowired
 	private ArmasService armasService;
 
 	@Autowired
 	private CustomerTypeService customerTypeService;
 	
 	@Autowired
 	private DocRunningUtil docRunningUtil;
 
 	@Autowired
 	private MessageSource messages;
 
 	public void setMessages(MessageSource messages) {
 		this.messages = messages;
 	}
 
 	private String VIEWNAME_SEARCH = "serviceOrder.search";
 	private String VIEWNAME_FORM = "serviceOrder.form";
 
 	private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale ("US"));
 	/*private SimpleDateFormat sdfDateTime = new SimpleDateFormat(
 			"dd/MM/yyyy HH:mm", new Locale("th", "TH"));*/
 	private SimpleDateFormat sdfDateTime = new SimpleDateFormat(
 			"dd/MM/yyyy HH:mm", new Locale ( "US" ));
 	private SimpleDateFormat sdfDateTime2 = new SimpleDateFormat(
 			"dd/MM/yyyy HH:mm", new Locale("th", "th"));
 	private SimpleDateFormat sdfDateTimeNoLocale = new SimpleDateFormat(
 			"dd/MM/yyyy HH:mm");
 	private SimpleDateFormat sdfDateTimeUSLocale = new SimpleDateFormat(
 			"dd/MM/yyyy HH:mm", new Locale("US"));
 	private SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm", new Locale("US"));
 
 	@RequestMapping(value = "/serviceOrder")
 	public String view(ModelMap model, HttpServletRequest request) {
 		if (null == request.getSession().getAttribute("UserLogin")) {
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
 
 	@RequestMapping(value = "/searchServiceOrder")
 	public @ResponseBody
 	GridResponse getData(
 			@RequestParam(value = "name", required = false) String name,
 			@RequestParam(value = "startDate", required = false) String startDate,
 			@RequestParam(value = "endDate", required = false) String endDate,
 			@RequestParam(value = "type", required = false) String type,
 			@RequestParam(value = "serialNo", required = false) String serialNo,
 			@RequestParam("rows") Integer rows,
 			@RequestParam("page") Integer page,
 			@RequestParam("sidx") String sidx, @RequestParam("sord") String sord) {
 		String[] datePart;
 		String searchStartDate = null;
 		String searchEndDate = null;
 		// Because default Tomcat URI encoding is iso-8859-1 so it must encode
 		// back to tis620
 		try {
 			if (null != name) {
 				name = new String(name.getBytes("iso-8859-1"), "tis620");
 			}
 			if (null != startDate && !startDate.equals("")) {
 				startDate = new String(startDate.getBytes("iso-8859-1"),
 						"tis620");
 				datePart = startDate.split("/");
 				// Change year to Christ year
 				/*Integer year = Integer.parseInt(datePart[2]);
 				year = year - 543;*/
 				//searchStartDate = year.toString() + "-" + datePart[1] + "-"
 				//		+ datePart[0];
 				searchStartDate = datePart[2] + "-" + datePart[1] + "-"
 						+ datePart[0];
 			}
 			if (null != endDate && !endDate.equals("")) {
 				endDate = new String(endDate.getBytes("iso-8859-1"), "tis620");
 				datePart = endDate.split("/");
 				// Change year to Christ year
 				/*Integer year = Integer.parseInt(datePart[2]);
 				year = year - 543;*/
 				//searchEndDate = year.toString() + "-" + datePart[1] + "-"
 				//		+ datePart[0];
 				searchEndDate = datePart[2] + "-" + datePart[1] + "-"
 						+ datePart[0];
 			}
 			if (null != type) {
 				type = new String(type.getBytes("iso-8859-1"), "tis620");
 			}
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 
 		List<ServiceOrder> soList = soService.selectByCriteria(name,
 				searchStartDate, searchEndDate, type, serialNo, rows, page,
 				sidx, sord);
 		GridResponse response = new GridResponse();
 
 		List<ServiceOrderGridData> rowsList = new ArrayList<ServiceOrderGridData>();
 
 		Integer total_pages = 0;
 		if (soList.size() > 0) {
 //			int i=0;
 //			for (ServiceOrder so : soList) {
 //				if(i >= (rows*page - rows) && i <= (rows*page - 1)){
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
 				// System.out.println("so.getServiceOrderDate() = "+so.getServiceOrderDate());
 				// System.out.println("sdf.format(so.getServiceOrderDate()) = "+sdf.format(so.getServiceOrderDate()));
 				// System.out.println("sdfDateTime.format(so.getServiceOrderDate()) = "+sdfDateTime.format(so.getServiceOrderDate()));
 				// System.out.println("sdfDateTime2.format(so.getServiceOrderDate()) = "+sdfDateTime2.format(so.getServiceOrderDate()));
 				// System.out.println("sdfDateTimeNoLocale.format(so.getServiceOrderDate()) = "+sdfDateTimeNoLocale.format(so.getServiceOrderDate()));
 				// System.out.println("sdfDateTimeUSLocale.format(so.getServiceOrderDate()) = "+sdfDateTimeUSLocale.format(so.getServiceOrderDate()));
 				gridData.setServiceOrderDate(sdfDateTime.format(so
 						.getServiceOrderDate()));
 				String serviceType = "";
 				
 //				<form:radiobutton path="serviceType" value="1" cssStyle="margin-top:4px" id="serviceType_guarantee" onclick="checkServiceType()" /><label style="float:left; margin-top:4px"><fmt:message key="serviceOrderType_guarantee" /></label><form:select path="guaranteeNo" id="guaranteeNo" ><c:forEach var="i" begin="1" end="7" step="1"><form:option value="${i}" /></c:forEach></form:select>
 //				<form:radiobutton path="serviceType" value="2" cssStyle="margin-top:4px;" id="serviceType_repair" onclick="checkServiceType()" /><label style="float:left; margin-top:4px"><fmt:message key="serviceOrderType_repair" /></label>
 //				<form:radiobutton path="serviceType" value="3" cssStyle="margin-top:4px" id="serviceType_claim" onclick="checkServiceType()" /><label style="float:left; margin-top:4px"><fmt:message key="serviceOrderType_claim" /></label>
 //				<form:radiobutton path="serviceType" value="4" cssStyle="margin-top:4px" id="serviceType_outsiteService" onclick="checkServiceType()" /><label style="float:left; margin-top:4px"><fmt:message key="serviceOrderType_outsiteService" /></label><form:input path="refJobID" class="textboxMockup" id="refJobID" maxlength="30" />
 //				<form:radiobutton path="serviceType" value="5" cssStyle="margin-top:4px" id="serviceType_refix" onclick="checkServiceType()" /><label style="float:left; margin-top:4px;"><fmt:message key="serviceOrderType_refix" /></label><form:input path="refServiceOrder" class="textboxMockup" id="refServiceOrder" maxlength="20" />
 				
 				
 				if (so.getServiceType() == 1) {
 					serviceType = this.messages.getMessage(
 							"serviceOrderType_guarantee", null, new Locale("th", "TH"));
 					if(so.getGuaranteeNo() != null){
 						serviceType = serviceType + " " + this.messages.getMessage("guarantee_No", null, new Locale("th", "TH")) + " " + so.getGuaranteeNo().toString();
 					}
 				} else if (so.getServiceType() == 2) {
 					serviceType = this.messages.getMessage(
 							"serviceOrderType_repair", null, new Locale("th", "TH"));
 				} else if (so.getServiceType() == 3) {
 					serviceType = this.messages.getMessage(
 							"serviceOrderType_claim", null, new Locale("th", "TH"));
 				} else if (so.getServiceType() == 4) {
 					serviceType = this.messages.getMessage(
 							"serviceOrderType_outsiteService", null, new Locale("th", "TH")) + " " +
 							this.messages.getMessage("reference", null, new Locale("th", "TH")) + " " +
 							so.getRefJobID();
 				} else if (so.getServiceType() == 5) {
 					serviceType = this.messages.getMessage(
 							"serviceOrderType_refix", null, new Locale("th", "TH"))+ " " +
 							this.messages.getMessage("reference", null, new Locale("th", "TH")) + " " +
 							so.getRefServiceOrder();
 				}
 				gridData.setServiceType(serviceType);
				try{
					gridData.setAppointmentDate(sdfDateTime.format(so.getAppointmentDate()));
				}catch(NullPointerException npe){
					gridData.setAppointmentDate("-");
				}
 //				if(so.getCustomerType().equals(ServiceOrder.CUSTOMERTYPE_WALKIIN)){
 					Customer customer = so.getCustomer();
 					gridData.setName(customer.getName());
 //					gridData.setSurname(customer.getSurname());
 //					gridData.setFullName(customer.getName()+" "+customer.getSurname());
 					gridData.setEmail(customer.getEmail());
 					gridData.setTel(customer.getTel());
 					gridData.setMobileTel(customer.getMobileTel());
 					
 					gridData.setCustomer(customer);
 					gridData.setCustomerFullAddress(customer.getAddress()
 							+ " "
 							+ this.messages.getMessage("subdistrict_abbr", null,
 									new Locale("th", "TH"))
 							+ " "
 							+ customer.getSubdistrict().getName()
 							+ " "
 							+ this.messages.getMessage("district_abbr", null,
 									new Locale("th", "TH"))
 							+ " "
 							+ customer.getDistrict().getName()
 							+ " "
 							+ this.messages.getMessage("province_abbr", null,
 									new Locale("th", "TH")) + " "
 							+ customer.getProvince().getName());
 //				}
 				gridData.setDeliveryCustomer(so.getDeliveryCustomer());
 				gridData.setDeliveryEmail(so.getDeliveryEmail());
 				gridData.setDeliveryTel(so.getDeliveryTel());
 				gridData.setDeliveryMobileTel(so.getDeliveryMobileTel());
 				gridData.setStatus(so.getStatus());
 				gridData.setProductID(so.getProduct().getProductID());
 				gridData.setType(so.getProduct().getType().getName());
 				gridData.setBrand(so.getProduct().getBrand().getName());
 				gridData.setModel(so.getProduct().getModel().getName());
 				gridData.setSerialNo(so.getProduct().getSerialNo());
 				gridData.setAccessories(so.getAccessories());
 				gridData.setProblem(so.getProblem());
 				gridData.setDescription(so.getDescription());
 				gridData.setEmpOpen(so.getEmpOpen().getName() + " "
 						+ so.getEmpOpen().getSurname());
 
 				
 				rowsList.add(gridData);
 			}
 			total_pages = new Double(
 					Math.ceil(((double) soList.size() / (double) rows)))
 					.intValue();
 		}
 
 		if (page > total_pages)
 			page = total_pages;
 		response.setPage(page.toString());
 		response.setRecords(String.valueOf(soList.size()));
 		response.setTotal(total_pages.toString());
 		response.setRows(rowsList);
 		return response;
 	}
 
 	@RequestMapping(value = "/serviceOrder", params = "do=preAdd")
 	public String preAdd(ModelMap model, HttpServletRequest request)
 			throws ParseException {
 		if (null == request.getSession().getAttribute("UserLogin")) {
 			LoginForm loginForm = new LoginForm();
 			model.addAttribute(loginForm);
 			return "loginScreen";
 		}
 		ServiceOrderForm form = new ServiceOrderForm();
 		ProductForm productForm = new ProductForm();
 		Date now = new Date();
 		// System.out.println("now = "+now);
 		form.setServiceOrderDate(sdfDateTime.format(now));
 		// System.out.println("ServiceOrderController:preAdd");
 		// System.out.println("sdfDateTime.format(now) = "+sdfDateTime.format(now));
 		// System.out.println("sdfDateTime.parse(form.getServiceOrderDate()) = "+sdfDateTime.parse(form.getServiceOrderDate()));
 		// System.out.println("sdfDateTimeNoLocale.parse(form.getServiceOrderDate()) = "+sdfDateTimeNoLocale.parse(form.getServiceOrderDate()));
 		// System.out.println("sdfDateTimeUSLocale.parse(form.getServiceOrderDate()) = "+sdfDateTimeUSLocale.parse(form.getServiceOrderDate()));
 		// System.out.println("sdfDateTime2.parse(form.getServiceOrderDate()) = "+sdfDateTime2.parse(form.getServiceOrderDate()));
 		form.setServiceType(2);
 		form.setCustomerType(ServiceOrder.CUSTOMERTYPE_SHOP);
 		List<Type> typeList = new ArrayList<Type>();
 		try {
 			typeList = typeService.getAll();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		Type type = typeList.get(0);
 		form.setTypeID(type.getTypeID());
 		List<Brand> brandList = new ArrayList<Brand>();
 		if (type.getBrands().size() > 0) {
 			brandList = type.getBrands();
 			Brand brand = brandList.get(0);
 			form.setBrandID(brand.getBrandID());
 		} else {
 			Brand blankBrand = new Brand();
 			blankBrand.setBrandID(null);
 			blankBrand.setName("");
 			brandList.add(blankBrand);
 		}
 
 		model.addAttribute("form", form);
 		model.addAttribute("typeList", typeList);
 		model.addAttribute("brandList", brandList);
 
 		// get data for customer form
 		List<Province> provinceList = provinceService.getAll();
 		List<District> districtList = districtService.getByProvince(7);
 		// set subdistrict from Muang district
 		List<Subdistrict> subdistrictList = sdService.getByDistrict(160);
 
 		List<CustomerType> customerTypeList = customerTypeService.getAll();
 		
 		model.addAttribute("provinceList", provinceList);
 		model.addAttribute("districtList", districtList);
 		model.addAttribute("subdistrictList", subdistrictList);
 		model.addAttribute("customerTypeList", customerTypeList);
 
 		CustomerForm custForm = new CustomerForm();
 		custForm.setProvinceID(7);
 		// set default district to Muang
 		custForm.setDistrictID(160);
 		// set subdistrict from Muang district
 		custForm.setSubdistrictID(((Subdistrict)subdistrictList.get(0)).getSubdistrictID());
 		model.addAttribute("customerForm", custForm);
 
 		// get form for print document
 		ServiceOrderDocForm docForm = new ServiceOrderDocForm();
 		model.addAttribute("docForm", docForm);
 		
 		model.addAttribute("productForm", productForm);
 		
 		model.addAttribute("mode", "add");
 		return VIEWNAME_FORM;
 	}
 
 	@RequestMapping(value = "/serviceOrder", params = "do=save")
 	public String doSave(@ModelAttribute("form") ServiceOrderForm form,
 			HttpServletRequest request, ModelMap model, @RequestParam String mode) {
 		if (null == request.getSession().getAttribute("UserLogin")) {
 			LoginForm loginForm = new LoginForm();
 			model.addAttribute(loginForm);
 			return "loginScreen";
 		}
 		Date now = new Date();
 		Employee user = (Employee) request.getSession().getAttribute(
 				"UserLogin");
 		ServiceOrder so = new ServiceOrder();
 		String msg = "";
 		if (!form.getServiceOrderID().equals("")) {
 			// update
 			so = soService.selectByID(form.getServiceOrderID());
 			msg = this.messages.getMessage("msg.updateComplete", null,
 					new Locale("th", "TH"));
 		} else {
 			// add
 			// DocRunning docRunning = docRunningService.getDoc("SO");
 			// String serviceOrderID =
 			// docRunning.getPref ix()+"-"+docRunning.getYear()+docRunning.getMonth()+"-"+docRunning.getRunningNumber();
 			// String serviceOrderID = genDocNo();
 			String serviceOrderID = docRunningUtil.genDoc("SO");
 			so.setServiceType(form.getServiceType());
 			if(form.getServiceType() == 1){
 				so.setGuaranteeNo(form.getGuaranteeNo());
 			}
 			
 			if(form.getServiceType() == 4){
 				so.setRefJobID(form.getRefJobID());
 			}
 			
 			if(form.getServiceType() == 5){
 				so.setRefServiceOrder(form.getRefServiceOrder());
 			}
 			// System.out.println("serviceOrderID = "+serviceOrderID);
 			try {
 				so.setServiceOrderDate(sdfDateTime.parse(form
 						.getServiceOrderDate()));
 				// so.setServiceOrderDate(sdfDateTimeUSLocale.parse(form.getServiceOrderDate()));
 				// System.out.println("form.getServiceOrderDate() = "+form.getServiceOrderDate());
 				// System.out.println("sdfDateTime.parse(form.getServiceOrderDate()) = "+sdfDateTime.parse(form.getServiceOrderDate()));
 				// System.out.println("so.getServiceOrderDate = "+so.getServiceOrderDate());
 			} catch (ParseException e) {
 				e.printStackTrace();
 				so.setServiceOrderDate(new Date());
 			}
 
 			so.setServiceOrderID(serviceOrderID);
 			so.setEmpOpen(user);
 			so.setCreatedBy(user.getEmployeeID());
 			so.setCreatedDate(now);
 			so.setStatus(ServiceOrder.NEW);
 			msg = this.messages.getMessage("msg.addComplete", null, new Locale(
 					"th", "TH"));
 		}
 		
 		if(!form.getAppointmentDate().equals(null) && !form.getAppointmentDate().equals("")){
 			try {
 				so.setAppointmentDate(sdfDateTime.parse(form.getAppointmentDate()));
 			} catch (ParseException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		
 //		if(form.getCustomerType().equals("walkin")){
 			Customer customer = new Customer();
 			try {
 				customer = customerService.selectByID(form.getCustomerID());
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			so.setCustomer(customer);
 			model.addAttribute("customer", customer);
 			
 			
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
 
 			
 			
 //		}else if(form.getCustomerType().equals("shop")){
 //			Armas armas = new Armas();
 //			armas = armasService.selectByID(form.getCustomerID());
 //			so.setShopCustomerID(armas.getCuscod());
 //			model.addAttribute("armas", armas);
 //		}
 //		Type type = new Type();
 //		try {
 //			type = typeService.selectByID(form.getTypeID());
 //		} catch (Exception e1) {
 //			e1.printStackTrace();
 //		}
 //		Brand brand = new Brand();
 //		try {
 //			brand = brandService.selectByID(form.getBrandID());
 //		} catch (Exception e) {
 //			e.printStackTrace();
 //		}
 		Product product = new Product();
 		product = productService.selectByID(form.getProductID());
 		so.setProduct(product);
 //		so.setType(type);
 //		so.setBrand(brand);
 //		so.setModel(form.getModel());
 //		so.setSerialNo(form.getSerialNo());
 		so.setAccessories(form.getAccessories());
 		so.setDescription(form.getDesc());
 		so.setProblem(form.getProblem());
 
 		so.setUpdatedBy(user.getEmployeeID());
 		so.setUpdatedDate(now);
 		// if(form.getTypeID().length > 0){
 		// Set<Type> typeList = new HashSet<Type>();
 		// for(Integer typeID:form.getTypeID()){
 		// Type type = typeService.selectByID(typeID);
 		// typeList.add(type);
 		// }
 		// brand.setTypes(typeList);
 		// }
 		boolean canSave;
 		try {
 			canSave = soService.save(so);
 		} catch (Exception e) {
 			e.printStackTrace();
 			model.addAttribute("errMsg", e.getMessage());
 			
 //			if(form.getCustomerType().equals("walkin")){
 				Customer customer1 = new Customer();
 				try {
 					customer1 = customerService.selectByID(form.getCustomerID());
 				} catch (Exception e1) {
 					e1.printStackTrace();
 				}
 				so.setCustomer(customer1);
 				model.addAttribute("customer", customer1);
 //			}else if(form.getCustomerType().equals("shop")){
 //				Armas armas = new Armas();
 //				armas = armasService.selectByID(form.getCustomerID());
 //				so.setShopCustomerID(armas.getCuscod());
 //				model.addAttribute("armas", armas);
 //			}
 			
 			List<Type> typeList = new ArrayList<Type>();
 			try {
 				typeList = typeService.getAll();
 			} catch (Exception e1) {
 				e1.printStackTrace();
 			}
 			Type type = typeList.get(0);
 			
 			List<Model> modelList = new ArrayList<Model>();
 			List<Brand> brandList = new ArrayList<Brand>();
 			if (type.getBrands().size() > 0) {
 				brandList = type.getBrands();
 				form.setBrandID(form.getBrandID());
 				
 				Brand brand = brandList.get(0);
 				modelList = modelService.getModelByTypeAndBrand(type.getTypeID(), brand.getBrandID());
 			} else {
 				Brand blankBrand = new Brand();
 				blankBrand.setBrandID(null);
 				blankBrand.setName("");
 				brandList.add(blankBrand);
 			}
 
 			model.addAttribute("typeList", typeList);
 			model.addAttribute("brandList", brandList);
 			model.addAttribute("modelList", modelList);
 
 			// get data for customer form
 			List<Province> provinceList = provinceService.getAll();
 			List<District> districtList = districtService.getByProvince(7);
 			// set subdistrict from Muang district
 			List<Subdistrict> subdistrictList = sdService.getByDistrict(160);
 
 			List<CustomerType> customerTypeList = customerTypeService.getAll();
 			
 			model.addAttribute("provinceList", provinceList);
 			model.addAttribute("districtList", districtList);
 			model.addAttribute("subdistrictList", subdistrictList);
 			model.addAttribute("customerTypeList", customerTypeList);
 			
 			CustomerForm custForm = new CustomerForm();
 			custForm.setProvinceID(7);
 			// set default district to Muang
 			custForm.setDistrictID(160);
 			model.addAttribute("customerForm", custForm);
 
 			// get form for print document
 			ServiceOrderDocForm docForm = new ServiceOrderDocForm();
 			model.addAttribute("docForm", docForm);
 			model.addAttribute("mode", mode);
 			return VIEWNAME_FORM;
 		}
 		if (!canSave) {
 			model.addAttribute("errMsg", this.messages.getMessage(
 					"error.cannotSave", null, new Locale("th", "TH")));
 			
 //			if(form.getCustomerType().equals("walkin")){
 				Customer customer1 = new Customer();
 				try {
 					customer1 = customerService.selectByID(form.getCustomerID());
 				} catch (Exception e1) {
 					e1.printStackTrace();
 				}
 				so.setCustomer(customer1);
 				model.addAttribute("customer", customer1);
 	//		}else if(form.getCustomerType().equals("shop")){
 	//			Armas armas = new Armas();
 	//			armas = armasService.selectByID(form.getCustomerID());
 	//			so.setShopCustomerID(armas.getCuscod());
 	//			model.addAttribute("armas", armas);
 	//		}
 			
 			List<Type> typeList = new ArrayList<Type>();
 			try {
 				typeList = typeService.getAll();
 			} catch (Exception e1) {
 				e1.printStackTrace();
 			}
 			Type type = typeList.get(0);
 			
 			List<Model> modelList = new ArrayList<Model>();
 			List<Brand> brandList = new ArrayList<Brand>();
 			if (type.getBrands().size() > 0) {
 				brandList = type.getBrands();
 				form.setBrandID(form.getBrandID());
 				
 				Brand brand = brandList.get(0);
 				modelList = modelService.getModelByTypeAndBrand(type.getTypeID(), brand.getBrandID());
 			} else {
 				Brand blankBrand = new Brand();
 				blankBrand.setBrandID(null);
 				blankBrand.setName("");
 				brandList.add(blankBrand);
 			}
 	
 			model.addAttribute("typeList", typeList);
 			model.addAttribute("brandList", brandList);
 			model.addAttribute("modelList", modelList);
 
 			// get data for customer form
 			List<Province> provinceList = provinceService.getAll();
 			List<District> districtList = districtService.getByProvince(7);
 			// set subdistrict from Muang district
 			List<Subdistrict> subdistrictList = sdService.getByDistrict(160);
 			
 			List<CustomerType> customerTypeList = customerTypeService.getAll();
 			
 			model.addAttribute("provinceList", provinceList);
 			model.addAttribute("districtList", districtList);
 			model.addAttribute("subdistrictList", subdistrictList);
 			model.addAttribute("customerTypeList", customerTypeList);
 
 			CustomerForm custForm = new CustomerForm();
 			custForm.setProvinceID(7);
 			// set default district to Muang
 			custForm.setDistrictID(160);
 			model.addAttribute("customerForm", custForm);
 
 			// get form for print document
 			ServiceOrderDocForm docForm = new ServiceOrderDocForm();
 			model.addAttribute("docForm", docForm);
 			model.addAttribute("mode", mode);
 			return VIEWNAME_FORM;
 		}
 		model.addAttribute("msg", msg);
 		// ServiceOrderSearchForm searchForm = new ServiceOrderSearchForm();
 		// model.addAttribute("searchForm", searchForm);
 		// return VIEWNAME_SEARCH;
 
 		// model.addAttribute("action", "print");
 		List<Type> typeList = new ArrayList<Type>();
 		try {
 			typeList = typeService.getAll();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		Type type = typeList.get(0);
 		
 		List<Brand> brandList = new ArrayList<Brand>();
 		List<Model> modelList = new ArrayList<Model>();
 		if (type.getBrands().size() > 0) {
 			brandList = type.getBrands();
 			form.setBrandID(form.getBrandID());
 			
 			Brand brand = brandList.get(0);
 			modelList = modelService.getModelByTypeAndBrand(type.getTypeID(), brand.getBrandID());
 		} else {
 			Brand blankBrand = new Brand();
 			blankBrand.setBrandID(null);
 			blankBrand.setName("");
 			brandList.add(blankBrand);
 		}
 
 		model.addAttribute("typeList", typeList);
 		model.addAttribute("brandList", brandList);
 		model.addAttribute("modelList", modelList);
 
 		// get data for customer form
 		List<Province> provinceList = provinceService.getAll();
 		List<District> districtList = districtService.getByProvince(7);
 		// set subdistrict from Muang district
 		List<Subdistrict> subdistrictList = sdService.getByDistrict(160);
 		
 		List<CustomerType> customerTypeList = customerTypeService.getAll();
 		
 		model.addAttribute("provinceList", provinceList);
 		model.addAttribute("districtList", districtList);
 		model.addAttribute("subdistrictList", subdistrictList);
 		model.addAttribute("customerTypeList", customerTypeList);
 
 		CustomerForm custForm = new CustomerForm();
 		custForm.setProvinceID(7);
 		// set default district to Muang
 		custForm.setDistrictID(160);
 		model.addAttribute("customerForm", custForm);
 
 		// get data for print document
 		ServiceOrderDocForm docForm = setDocPrintForm(so);
 
 		model.addAttribute("docForm", docForm);
 		model.addAttribute("action", "print");
 		model.addAttribute("mode", "edit");
 		return VIEWNAME_FORM;
 	}
 
 	@RequestMapping(value = "/serviceOrder", params = "do=preEdit")
 	public String preEdit(
 			@RequestParam(value = "serviceOrderID") String serviceOrderID,
 			ModelMap model, HttpServletRequest request) {
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
 		form.setRefServiceOrder(so.getRefServiceOrder());
 		form.setCustomerType(so.getCustomerType());
 		form.setCustomerID(so.getCustomer().getCustomerID().toString());
 		form.setProductID(so.getProduct().getProductID());
 		form.setTypeID(so.getProduct().getType().getTypeID());
 		form.setBrandID(so.getProduct().getBrand().getBrandID());
 		form.setModel(so.getProduct().getModel().getName());
 		form.setSerialNo(so.getProduct().getSerialNo());
 		form.setAccessories(so.getAccessories());
 		form.setDesc(so.getDescription());
 		form.setProblem(so.getProblem());
 		// form.setName(brand.getName());
 		// List<Integer> l = new ArrayList<Integer>();
 		// for(Type type:brand.getTypes()){
 		// l.add(type.getTypeID());
 		// }
 		// Integer ia[] = new Integer[l.size()];
 		// ia = l.toArray(ia);
 		// form.setTypeID(ia);
 
 		List<Type> typeList = new ArrayList<Type>();
 		try {
 			typeList = typeService.getAll();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
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
 		
 		Type type = typeList.get(0);
 		List<Brand> brandList = new ArrayList<Brand>();
 		List<Model> modelList = new ArrayList<Model>();
 		if (so.getProduct().getType().getBrands().size() > 0) {
 			brandList = so.getProduct().getType().getBrands();
 			form.setBrandID(form.getBrandID());
 			
 			Brand brand = brandList.get(0);
 			modelList = modelService.getModelByTypeAndBrand(type.getTypeID(), brand.getBrandID());
 		} else {
 			Brand blankBrand = new Brand();
 			blankBrand.setBrandID(null);
 			blankBrand.setName("");
 			brandList.add(blankBrand);
 		}
 		
 		model.addAttribute("typeList", typeList);
 		model.addAttribute("brandList", brandList);
 		model.addAttribute("modelList", modelList);
 
 		CustomerForm custForm = new CustomerForm();
 		custForm.setProvinceID(7);
 		// set default district to Muang
 		custForm.setDistrictID(160);
 		model.addAttribute("customerForm", custForm);
 
 		ServiceOrderDocForm docForm = setDocPrintForm(so);
 		
 		model.addAttribute("docForm", docForm);
 
 		model.addAttribute("mode", "edit");
 		return VIEWNAME_FORM;
 	}
 
 	@RequestMapping(value = "/serviceOrder", params = "do=delete")
 	public @ResponseBody
 	CustomGenericResponse delete(HttpServletRequest request) {
 		Employee user = (Employee) request.getSession().getAttribute(
 				"UserLogin");
 		CustomGenericResponse response = new CustomGenericResponse();
 		response.setSuccess(true);
 		try {
 			soService.delete(request.getParameter("serviceOrderID"),
 					user.getEmployeeID());
 			response.setMessage(this.messages.getMessage("msg.deleteSuccess", null, new Locale("th", "TH")));
 		} catch (Exception e) {
 			response.setSuccess(false);
 			response.setMessage(this.messages.getMessage("error.cannotDelete", null, new Locale("th", "TH")));
 		}
 		return response;
 	}
 
 	/*
 	 * private String genDocNo(){ // Calendar now = Calendar.getInstance(new
 	 * Locale ( "th", "TH" )); Calendar now = Calendar.getInstance(new Locale (
 	 * "US" )); // System.out.println("month = "+now.get(Calendar.MONTH)); //
 	 * System.out.println("year = "+now.get(Calendar.YEAR)); String docNo = "";
 	 * Integer nMonth = now.get(Calendar.MONTH) + 1; Integer nYear =
 	 * now.get(Calendar.YEAR); Integer nDate = now.get(Calendar.DATE); //
 	 * Integer nYear = now.get(Calendar.YEAR) + 1; // DocRunning docRunning =
 	 * docRunningService.getDoc("SO"); // DocRunning docRunning =
 	 * docRunningService.getDocByYearMonth("SO", nYear, nMonth); DocRunning
 	 * docRunning = docRunningService.getDocByYearMonthDate("SO", nYear, nMonth,
 	 * nDate); if(null == docRunning){ // save new docrunning docRunning = new
 	 * DocRunning(); docRunning.setDocument("SO"); docRunning.setPrefix("so");
 	 * docRunning.setMonth(now.get(Calendar.MONTH) + 1);
 	 * docRunning.setYear(now.get(Calendar.YEAR));
 	 * docRunning.setDate(now.get(Calendar.DATE));
 	 * docRunning.setRunningNumber(1);
 	 * docRunningService.createNewDocRunning(docRunning); } String month =
 	 * docRunning.getMonth().toString(); if(month.length() == 1){ month =
 	 * "0"+month; } String year = docRunning.getYear().toString(); year =
 	 * year.substring(2, 4); String date = docRunning.getDate().toString();
 	 * if(date.length() == 1){ date = "0"+date; } // docNo =
 	 * docRunning.getPrefix()+"-"+year+month+"-"+docRunning.getRunningNumber();
 	 * docNo = year+month+date+"-"+docRunning.getRunningNumber();
 	 * 
 	 * // increse running no
 	 * 
 	 * docRunning.setRunningNumber(docRunning.getRunningNumber()+1);
 	 * docRunningService.updateDocRunning(docRunning);
 	 * 
 	 * return docNo; }
 	 */
 
 	@RequestMapping(value = "/serviceOrder", params = "do=print")
 	// public String doPrint(ModelMap model,
 	// @RequestParam(value="serviceOrderID") String serviceOrderID,
 	// @RequestParam(required=false) String serviceOrderDate){
 	public String doPrint(@ModelAttribute ServiceOrderDocForm docForm,
 			HttpServletRequest request, ModelMap model) {
 
 		Type type = new Type();
 		try {
 			type = typeService.selectByID(docForm.getTypeID());
 		} catch (NumberFormatException e1) {
 			e1.printStackTrace();
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 		Brand brand = new Brand();
 		try {
 			brand = brandService.selectByID(Integer.parseInt(docForm
 					.getBrandID()));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		Employee empOpen = new Employee();
 		try {
 			empOpen = empService.selectByID(Integer.parseInt(docForm
 					.getEmpOpenID()));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		docForm.setType(type.getName());
 		docForm.setBrand(brand.getName());
 		docForm.setEmpOpen(empOpen.getName() + " " + empOpen.getSurname());
 
 		List<ServiceOrderDocForm> resultList = new ArrayList<ServiceOrderDocForm>();
 		resultList.add(docForm);
 
 		model.addAttribute("serviceOrderResultList", resultList);
 
 //		return "serviceOrderDoc";
 		
 		// test excel view
 		
 		Map map = new HashMap();
         List wordList = new ArrayList();
         wordList.add("hello");
         wordList.add("world");
         map.put("wordList", wordList);
         
         model.addAttribute("wordList", wordList);
         
         return "serviceOrderDocExcel";
 	}
 	
 	@RequestMapping(value = "/serviceOrder", params = "do=printExcel")
 	public String doPrintExcel(@ModelAttribute ServiceOrderDocForm docForm,
 			HttpServletRequest request, ModelMap model) {
 		
         List wordList = new ArrayList();
         wordList.add("hello");
         wordList.add("world");
 		
         model.addAttribute("wordList", wordList);
         model.addAttribute("form", docForm);
         
 		return "serviceOrderDocExcel";
 	}
 	
 	private ServiceOrderDocForm setDocPrintForm(ServiceOrder so){
 		ServiceOrderDocForm docForm = new ServiceOrderDocForm();
 		docForm.setServiceOrderID(so.getServiceOrderID());
 //		docForm.setServiceOrderDate(sdfDateTime.format(so.getServiceOrderDate()));
 		docForm.setServiceOrderDate(sdf.format(so.getServiceOrderDate()));
 		docForm.setServiceOrderTime(sdfTime.format(so.getServiceOrderDate()));
 		docForm.setAppointmentDate(sdfDateTime.format(so.getServiceOrderDate()));
 		docForm.setServiceType(so.getServiceType());
 		docForm.setServiceType(so.getServiceType());
 		if(so.getServiceType() == 1){
 			docForm.setGuaranteeNo(so.getGuaranteeNo());
 		}
 		if(so.getServiceType() == 4){
 			docForm.setRefJobID(so.getRefJobID());
 		}
 		if(so.getServiceType() == 5){
 			docForm.setRefServiceOrder(so.getRefServiceOrder());
 		}
 		
 //		docForm.setContactName(so.getCustomer().getName() + " "
 //				+ so.getCustomer().getSurname());
 		
 		
 //		if(so.getCustomerType().equals("walkin")){
 //			docForm.setContactName(so.getCustomer().getName() + " "
 //				+ so.getCustomer().getSurname());
 //		}else if(so.getCustomerType().equals("shop")){
 //			docForm.setContactName("");
 //		}
 		
 		
 		docForm.setCustomerID(so.getCustomer().getCustomerID());
 		docForm.setName(so.getCustomer().getName());
 		docForm.setEmail(so.getCustomer().getEmail());
 		docForm.setTel(so.getCustomer().getTel());
 		docForm.setMobileTel(so.getCustomer().getMobileTel());
 		docForm.setAddress(so.getCustomer().getAddress());
 		docForm.setSubdistrict(so.getCustomer().getSubdistrict().getName());
 		docForm.setDistrict(so.getCustomer().getDistrict().getName());
 		docForm.setProvince(so.getCustomer().getProvince().getName());
 		docForm.setZipcode(so.getCustomer().getSubdistrict().getZipcode().toString());
 		docForm.setDeliveryCustomer(so.getDeliveryCustomer());
 		docForm.setDeliveryEmail(so.getDeliveryEmail());
 		docForm.setDeliveryMobileTel(so.getDeliveryMobileTel());
 		docForm.setDeliveryTel(so.getDeliveryTel());
 		docForm.setTypeID(so.getProduct().getType().getTypeID().toString());
 		docForm.setType(so.getProduct().getType().getName());
 		docForm.setBrandID(so.getProduct().getBrand().getBrandID().toString());
 		docForm.setBrand(so.getProduct().getBrand().getName());
 		docForm.setModel(so.getProduct().getModel().getName());
 		docForm.setSerialNo(so.getProduct().getSerialNo());
 		docForm.setAccessories(so.getAccessories());
 		docForm.setDesc(so.getDescription());
 		docForm.setProblem(so.getProblem());
 		docForm.setEmpOpenID(so.getEmpOpen().getEmployeeID().toString());
 		docForm.setEmpOpen(so.getEmpOpen().getName() + " "
 				+ so.getEmpOpen().getSurname());
 
 		return docForm; 
 	}
 }
