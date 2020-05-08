 package com.twobytes.sale.controller;
 
 import java.io.UnsupportedEncodingException;
 import java.text.ParseException;
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
 
 import com.twobytes.master.form.CustomerForm;
 import com.twobytes.master.form.ProductForm;
 import com.twobytes.master.service.BrandService;
 import com.twobytes.master.service.CustomerService;
 import com.twobytes.master.service.DistrictService;
 import com.twobytes.master.service.EmployeeService;
 import com.twobytes.master.service.ModelService;
 import com.twobytes.master.service.ProductService;
 import com.twobytes.master.service.ProvinceService;
 import com.twobytes.master.service.SubdistrictService;
 import com.twobytes.master.service.TypeService;
 import com.twobytes.model.Brand;
 import com.twobytes.model.CustomGenericResponse;
 import com.twobytes.model.Customer;
 import com.twobytes.model.District;
 import com.twobytes.model.Employee;
 import com.twobytes.model.GridResponse;
 import com.twobytes.model.Product;
 import com.twobytes.model.Province;
 import com.twobytes.model.SaleOrder;
 import com.twobytes.model.Subdistrict;
 import com.twobytes.model.Type;
 
 import com.twobytes.sale.form.SaleOrderForm;
 import com.twobytes.sale.form.SaleOrderGridData;
 import com.twobytes.sale.form.SaleOrderSearchForm;
 import com.twobytes.sale.service.SaleOrderService;
 import com.twobytes.security.form.LoginForm;
 
 @Controller
 public class SaleOrderController {
 
 	@Autowired
 	private CustomerService customerService;
 	
 	@Autowired
 	private ProductService productService;
 	
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
 	private EmployeeService employeeService;
 	
 	@Autowired
 	private SaleOrderService saleOrderService;
 	
 	@Autowired
 	private MessageSource messages;
 	
 	public void setMessages(MessageSource messages) {
 		this.messages = messages;
 	}
 	
 	private String VIEWNAME_SEARCH = "saleOrder.search";
 	private String VIEWNAME_FORM = "saleOrder.form";
 	
 	private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale ("US"));
 	
 	@RequestMapping(value = "/saleOrder")
 	public String view(ModelMap model, HttpServletRequest request) {
 		if(null == request.getSession().getAttribute("UserLogin")){
 			LoginForm loginForm = new LoginForm();
 			model.addAttribute(loginForm);
 			return "loginScreen";
 		}
 		
 		SaleOrderSearchForm searchForm = new SaleOrderSearchForm();
 		model.addAttribute("searchForm", searchForm);
 		
 		List<Employee> empList = employeeService.getAll();
 		model.addAttribute("employeeList", empList);
 		return VIEWNAME_SEARCH;
 	}
 	
 	@RequestMapping(value = "/searchSaleOrder")
 	public @ResponseBody
 	GridResponse getData(
 			@RequestParam(value = "date", required = false) String date,
 			@RequestParam(value = "employeeID", required = false) String employeeID,
 			@RequestParam("rows") Integer rows,
 			@RequestParam("page") Integer page,
 			@RequestParam("sidx") String sidx, @RequestParam("sord") String sord) {
 		String[] datePart;
 		String searchDate = null;
 		// Because default Tomcat URI encoding is iso-8859-1 so it must encode
 		// back to tis620
 		try {
 			if (null != date && !date.equals("")) {
 				date = new String(date.getBytes("iso-8859-1"),
 						"tis620");
 				datePart = date.split("/");
 				searchDate = datePart[2] + "-" + datePart[1] + "-"
 						+ datePart[0];
 			}
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		
 		List<SaleOrder> soList = saleOrderService.selectByCriteria(searchDate, employeeID, rows, page,
 				sidx, sord);
 		GridResponse response = new GridResponse();
 		
 		List<SaleOrderGridData> rowsList = new ArrayList<SaleOrderGridData>();
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
 				SaleOrder so = soList.get(i);
 				SaleOrderGridData gridData = new SaleOrderGridData();
 				gridData.setSaleOrderID(so.getSaleOrderID().toString());
 				gridData.setSaleDate(sdf.format(so.getSaleDate()));
 
 				Employee employee = so.getEmployee();
 				gridData.setName(employee.getName());
 				gridData.setSurname(employee.getSurname());
 				gridData.setType(so.getProduct().getType().getName());
 				gridData.setBrand(so.getProduct().getBrand().getName());
 				gridData.setModel(so.getProduct().getModel().getName());
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
 	
 	@RequestMapping(value = "/saleOrder", params = "do=preAdd")
 	public String preAdd(ModelMap model, HttpServletRequest request) {
 		if (null == request.getSession().getAttribute("UserLogin")) {
 			LoginForm loginForm = new LoginForm();
 			model.addAttribute(loginForm);
 			return "loginScreen";
 		}
 		SaleOrderForm form = new SaleOrderForm();
 		ProductForm productForm = new ProductForm();
 		
 		List<Type> typeList = new ArrayList<Type>();
 		try {
 			typeList = typeService.getAll();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		Type type = typeList.get(0);
 //		form.setTypeID(type.getTypeID());
 		List<Brand> brandList = new ArrayList<Brand>();
 		if (type.getBrands().size() > 0) {
 			brandList = type.getBrands();
 			Brand brand = brandList.get(0);
 //			form.setBrandID(brand.getBrandID());
 		} else {
 			Brand blankBrand = new Brand();
 			blankBrand.setBrandID(null);
 			blankBrand.setName("");
 			brandList.add(blankBrand);
 		}
 
 		model.addAttribute("typeList", typeList);
 		model.addAttribute("brandList", brandList);
 		
 		CustomerForm custForm = new CustomerForm();
 		custForm.setProvinceID(7);
 		// set default district to Muang
 		custForm.setDistrictID(160);
 		model.addAttribute("customerForm", custForm);
 		
 		List<Province> provinceList = provinceService.getAll();
 		List<District> districtList = districtService.getByProvince(7);
 		// set subdistrict from Muang district
 		List<Subdistrict> subdistrictList = sdService.getByDistrict(160);
 		
 		model.addAttribute("provinceList", provinceList);
 		model.addAttribute("districtList", districtList);
 		model.addAttribute("subdistrictList", subdistrictList);
 		
 		List<Employee> empList = employeeService.getAll();
 		model.addAttribute("employeeList", empList);
 		
 		model.addAttribute("form", form);
 		model.addAttribute("productForm", productForm);
 		model.addAttribute("mode", "add");
 		return VIEWNAME_FORM;
 	}
 	
 	@RequestMapping(value = "/saleOrder", params = "do=save")
 	public String doSave(@ModelAttribute("form") SaleOrderForm form,
 			HttpServletRequest request, ModelMap model, @RequestParam String mode) {
 		if (null == request.getSession().getAttribute("UserLogin")) {
 			LoginForm loginForm = new LoginForm();
 			model.addAttribute(loginForm);
 			return "loginScreen";
 		}
 		Date now = new Date();
 		Employee user = (Employee) request.getSession().getAttribute("UserLogin");
 		SaleOrder so = new SaleOrder();
 		String msg = "";
 		
 		if(null != form.getSaleOrderID()){
 			// update
 			msg = this.messages.getMessage("msg.updateComplete", null, new Locale("th", "TH"));
 		}else{
 			// add
 			so.setCreatedBy(user.getEmployeeID());
 			so.setCreatedDate(now);
 			msg = this.messages.getMessage("msg.addComplete", null, new Locale("th", "TH"));
 		}
 			
 		Employee emp = new Employee();
 		
 		try {
 			emp = employeeService.selectByID(Integer.valueOf(form.getEmployeeID()));
 		} catch (NumberFormatException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		so.setEmployee(emp);
 		try {
 			so.setSaleDate(sdf.parse(form.getSaleDate()));
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 		try {
 			so.setCustomer(customerService.selectByID(form.getCustomerID()));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		so.setUpdatedBy(user.getEmployeeID());
 		so.setUpdatedDate(now);
 		
 		Product product = new Product();
 		product.setProductID(form.getProductID());
 		try {
 			product.setType(typeService.selectByID(form.getTypeID()));
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 		try {
 			product.setBrand(brandService.selectByID(form.getBrandID()));
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 		product.setModel(modelService.selectByID(form.getModelID()));
 		product.setSerialNo(form.getSerialNo());
 		product.setDescription(form.getDescription());
 		try {
 			product.setWarrantyDate(sdf.parse(form.getWarrantyDate()));
 		} catch (ParseException e1) {
 			e1.printStackTrace();
 		}
 		try {
 			product.setWarrantyExpire(sdf.parse(form.getWarrantyExpire()));
 		} catch (ParseException e1) {
 			e1.printStackTrace();
 		}
 		product.setRemark(form.getRemark());
 		product.setUpdatedBy(user.getEmployeeID());
 		product.setUpdatedDate(now);
 		product.setCreatedBy(user.getEmployeeID());
 		product.setCreatedDate(now);
 		
 		try{
 			saleOrderService.save(so, product);
 		}catch(Exception e){
 			e.printStackTrace();
 			model.addAttribute("errMsg", e.getMessage());
 			
 			ProductForm productForm = new ProductForm();
 			
 			List<Type> typeList = new ArrayList<Type>();
 			try {
 				typeList = typeService.getAll();
 			} catch (Exception e2) {
 				e2.printStackTrace();
 			}
 //			Type type = typeList.get(0);
 			Type type = new Type();
 			try {
 				type = typeService.selectByID(form.getTypeID());
 			} catch (Exception e1) {
 				e1.printStackTrace();
 			}
 //			form.setTypeID(type.getTypeID());
 			List<Brand> brandList = new ArrayList<Brand>();
 			if (type.getBrands().size() > 0) {
 				brandList = type.getBrands();
 //				Brand brand = brandList.get(0);
 //				form.setBrandID(brand.getBrandID());
 			} else {
 				Brand blankBrand = new Brand();
 				blankBrand.setBrandID(null);
 				blankBrand.setName("");
 				brandList.add(blankBrand);
 			}
 
 			model.addAttribute("typeList", typeList);
 			model.addAttribute("brandList", brandList);
 			
 			CustomerForm custForm = new CustomerForm();
 			custForm.setProvinceID(7);
 			// set default district to Muang
 			custForm.setDistrictID(160);
 			model.addAttribute("customerForm", custForm);
 			
 			Customer customer = new Customer();
 			try {
 				customer = customerService.selectByID(form.getCustomerID());
 			} catch (Exception e1) {
 				e1.printStackTrace();
 			}
 			model.addAttribute("customer", customer);
 			model.addAttribute("fullAddr", customer.getAddress()
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
 			
 			List<Province> provinceList = provinceService.getAll();
 			List<District> districtList = districtService.getByProvince(7);
 			// set subdistrict from Muang district
 			List<Subdistrict> subdistrictList = sdService.getByDistrict(160);
 			
 			model.addAttribute("provinceList", provinceList);
 			model.addAttribute("districtList", districtList);
 			model.addAttribute("subdistrictList", subdistrictList);
 			
 			List<Employee> empList = employeeService.getAll();
 			model.addAttribute("employeeList", empList);
 						
 			model.addAttribute("productForm", productForm);
 			model.addAttribute("mode", mode);
 			return VIEWNAME_FORM;
 		}
 		
 		model.addAttribute("msg", msg);
 		SaleOrderSearchForm searchForm = new SaleOrderSearchForm();
 		model.addAttribute("searchForm", searchForm);
 
 		return VIEWNAME_SEARCH;
 	}
 	
 	@RequestMapping(value = "/saleOrder", params = "do=delete")
 	public @ResponseBody
 	CustomGenericResponse delete(HttpServletRequest request) {
 		CustomGenericResponse response = new CustomGenericResponse();
 		response.setSuccess(true);
 		try {
 			saleOrderService.delete(Integer.valueOf(request.getParameter("saleOrderID")));
 			response.setMessage(this.messages.getMessage("msg.deleteSuccess", null, new Locale("th", "TH")));
 		} catch (Exception e) {
 			response.setSuccess(false);
 			response.setMessage(this.messages.getMessage("error.cannotDelete", null, new Locale("th", "TH")));
 		}
 		return response;
 	}
 }
