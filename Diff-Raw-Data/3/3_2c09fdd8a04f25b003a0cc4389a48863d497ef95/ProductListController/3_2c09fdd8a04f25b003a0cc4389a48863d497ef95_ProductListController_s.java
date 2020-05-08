 package com.megalogika.sv.controller;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.util.StringUtils;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.megalogika.sv.model.E;
 import com.megalogika.sv.model.Product;
 import com.megalogika.sv.model.ProductCategory;
 import com.megalogika.sv.model.User;
 import com.megalogika.sv.service.CategoryService;
 import com.megalogika.sv.service.EService;
 import com.megalogika.sv.service.ProductSearchCriteria;
 import com.megalogika.sv.service.ProductService;
 import com.megalogika.sv.service.SearchCriteria;
 import com.megalogika.sv.service.SearchResults;
 import com.megalogika.sv.service.SearchService;
 import com.megalogika.sv.service.filter.RankingFilterException;
 
 @Controller
 public class ProductListController {
 
 	protected transient Logger logger = Logger
 			.getLogger(ProductListController.class);
 
 	public final static String KEY_PRODUCT_LIST = "productList";
 	public final static String KEY_PRODUCTS = "products";
 	public static final String KEY_SEARCH_RESULTS = "searchResults";
 
 	@Autowired
 	private FrontendService frontendService;
 	@Autowired
 	private SearchService searchService;
 	@Autowired
 	private CategoryService categoryService;
 	@Autowired
 	private EService eService;
 	@Autowired
 	private ProductService productService;
 
 	@ModelAttribute(FrontendService.KEY_CONTEXT_PATH)
 	public String getContextPath(HttpServletRequest request) {
 		return frontendService.getContextPath(request);
 	}
 
 	@ModelAttribute(FrontendService.KEY_HAZARD_DESCRIPTIONS)
 	public Map<String, String> getHazardDescriptions() {
 		return frontendService.getHazardDescriptions();
 	}
 
 	@ModelAttribute(FrontendService.KEY_CATEGORY_LIST)
 	public List<ProductCategory> getCategoryList() {
 		return frontendService.getCategoryList();
 	}
 
 	@ModelAttribute(FrontendService.KEY_CURRENT_USER)
 	public User getCurrentUser() {
 		return frontendService.getUserService().getCurrentUser();
 	}
 
 	@RequestMapping("/index")
 	public ModelMap getIndexProductList(HttpSession session,
 			HttpServletRequest request, ModelMap m)
 			throws InstantiationException, IllegalAccessException {
 
 		ProductSearchCriteria criteria = (ProductSearchCriteria) frontendService
 				.getCriteria(session, ProductSearchCriteria.class);
 		criteria.clear();
 
 		frontendService.addCriteria(m, session, ProductSearchCriteria.class);
 		frontendService.addBlogFeed(m, request);
 
 		m.addAttribute(KEY_PRODUCTS, searchService.getList(criteria, false));
 		m.addAttribute("index", Boolean.TRUE);
 
 		return m;
 	}
 
 	@RequestMapping("/productList")
 	public ModelAndView getProductListForFrontend(HttpSession session,
 			HttpServletRequest request, ModelMap m,
 			@RequestParam(required = false, value = "clear") String clear,
 			@RequestParam(required = false, value = "page") String page,
 			@RequestParam(required = false, value = "pageSize") String pageSize)
 			throws InstantiationException, IllegalAccessException {
 		ProductSearchCriteria criteria = (ProductSearchCriteria) frontendService
 				.getCriteria(session, ProductSearchCriteria.class);
 		if (StringUtils.hasText(clear)) {
 			criteria.clear();
 		}
 
 		if (!criteria.containsFilter("UnapprovedProductFilter") &&
 				!criteria.containsFilter("ApprovedProductFilter")) {
 			criteria.addApprovedProductFilter();
 		}
 
 		criteria.updatePage(page);
 		criteria.updatePageSize(pageSize);
 
 		m.addAttribute("productList", Boolean.TRUE);
 
 		return prepareProductList(session, request, m);
 	}
 
 	private ModelAndView prepareProductList(HttpSession session,
 			HttpServletRequest request, ModelMap m)
 			throws InstantiationException, IllegalAccessException {
 
 		ProductSearchCriteria criteria = (ProductSearchCriteria) frontendService
 				.getCriteria(session, ProductSearchCriteria.class);
 
 		frontendService.addCriteria(m, session, ProductSearchCriteria.class);
 
 		List<Product> pList = searchService.getList(criteria,
				criteria.containsFilter("ApprovedProductFilter"));
 
 		m.addAttribute(KEY_PRODUCTS, pList);
 
 		return new ModelAndView("productList", m);
 	}
 
 	@RequestMapping("/productList/filterByApprovedContent")
 	public String filterByApprovedContent(HttpSession session)
 			throws InstantiationException, IllegalAccessException {
 		ProductSearchCriteria criteria = (ProductSearchCriteria) frontendService
 				.getCriteria(session, ProductSearchCriteria.class);
 		criteria.clear();
 		criteria.addApprovedContentFilter();
 
 		return "redirect:/spring/productList";
 	}
 
 	@RequestMapping("/productList/filterByUnapprovedProducts")
 	public String filterByUnapprovedProducts(HttpSession session)
 			throws InstantiationException, IllegalAccessException {
 		ProductSearchCriteria criteria = (ProductSearchCriteria) frontendService
 				.getCriteria(session, ProductSearchCriteria.class);
 		criteria.clear();
 		criteria.addUnapprovedProductFilter();
 
 		return "redirect:/spring/productList";
 	}
 
 	@RequestMapping("/productList/resetToFilter")
 	public String resetToFilter(
 			HttpSession session,
 			@RequestParam(required = true, value = "filterIndex") String filterIndex)
 			throws InstantiationException, IllegalAccessException {
 		SearchCriteria criteria = frontendService.getCriteria(session,
 				ProductSearchCriteria.class);
 		criteria.resetToFilter(filterIndex);
 
 		return "redirect:/spring/productList";
 	}
 
 	@RequestMapping("/productList/remove")
 	public String removeFilter(
 			HttpSession session,
 			@RequestParam(required = true, value = "filterIndex") String filterIndex)
 			throws InstantiationException, IllegalAccessException {
 		SearchCriteria criteria = frontendService.getCriteria(session,
 				ProductSearchCriteria.class);
 		criteria.removeFilter(filterIndex);
 
 		return "redirect:/spring/productList";
 	}
 
 	@RequestMapping("/productList/filterByHazard")
 	public String filterByHazard(HttpSession session,
 			@RequestParam(required = true, value = "hazard") String hazard)
 			throws InstantiationException, IllegalAccessException {
 		ProductSearchCriteria criteria = (ProductSearchCriteria) frontendService
 				.getCriteria(session, ProductSearchCriteria.class);
 		criteria.addHazardFilter(hazard, frontendService.geteService());
 
 		return "redirect:/spring/productList";
 	}
 
 	@RequestMapping("/productList/filterByGmo")
 	public String filterByGmo(HttpSession session,
 			@RequestParam(required = true, value = "hazard") String hazard)
 			throws InstantiationException, IllegalAccessException {
 		ProductSearchCriteria criteria = (ProductSearchCriteria) frontendService
 				.getCriteria(session, ProductSearchCriteria.class);
 		criteria.addHazardFilter(hazard, frontendService.geteService());
 
 		return "redirect:/spring/productList";
 	}
 
 	@RequestMapping("/productList/sort")
 	public String setOrderBy(HttpSession session,
 			@RequestParam(required = true, value = "orderBy") String orderBy)
 			throws InstantiationException, IllegalAccessException {
 		SearchCriteria criteria = frontendService.getCriteria(session,
 				ProductSearchCriteria.class);
 		criteria.setOrderBy(orderBy);
 
 		return "redirect:/spring/productList";
 	}
 
 	@RequestMapping("/productList/filterByCategory")
 	public String filterByCategory(HttpSession session,
 			@RequestParam(required = true, value = "category") long category)
 			throws InstantiationException, IllegalAccessException {
 		ProductSearchCriteria criteria = (ProductSearchCriteria) frontendService
 				.getCriteria(session, ProductSearchCriteria.class);
 		criteria.addCategoryFilter(getCategoryService().getProductCategory(
 				category));
 
 		return "redirect:/spring/productList";
 	}
 
 	@RequestMapping("/productList/filterByCompany")
 	public String filterByCompany(HttpSession session,
 			@RequestParam(required = true, value = "company") String company)
 			throws InstantiationException, IllegalAccessException {
 		ProductSearchCriteria criteria = (ProductSearchCriteria) frontendService
 				.getCriteria(session, ProductSearchCriteria.class);
 		criteria.addCompanyFilter(company);
 
 		return "redirect:/spring/productList";
 	}
 
 	@RequestMapping("/productList/filterByE")
 	public String filterByE(HttpSession session,
 			@RequestParam(required = true, value = "eid") String eid,
 			@RequestParam(required = false, value = "name") String name)
 			throws InstantiationException, IllegalAccessException {
 		if (null == name) {
 			name = eService.load(Long.parseLong(eid)).getName();
 		}
 		ProductSearchCriteria criteria = (ProductSearchCriteria) frontendService
 				.getCriteria(session, ProductSearchCriteria.class);
 		criteria.addEFilter(Long.parseLong(eid), name);
 
 		return "redirect:/spring/productList";
 	}
 
 	@RequestMapping("/search")
 	public ModelAndView search(HttpServletRequest request,
 			HttpServletResponse response, HttpSession session, ModelMap m,
 			@RequestParam(required = true, value = "q") String q,
 			@RequestParam(required = false, value = "page") String page,
 			@RequestParam(required = false, value = "pageSize") String pageSize)
 			throws IOException, InstantiationException, IllegalAccessException {
 
 		logger.debug("SEARCHING FOR Q: " + q);
 
 		ProductSearchCriteria criteria = (ProductSearchCriteria) frontendService
 				.getCriteria(session, ProductSearchCriteria.class);
 		if (StringUtils.hasText(q)) {
 			criteria.clear();
 			criteria.setQ(q);
 			try {
 				criteria.addTextKeywordFilter(q);
 			} catch (RankingFilterException e) {
 				logger.error("While processing search query ==" + q + "==", e);
 				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 				return null;
 			}
 		}
 
 		criteria.updatePageSize(pageSize);
 		criteria.updatePage(page);
 
 		frontendService.addCriteria(m, session, ProductSearchCriteria.class);
 
 		m.addAttribute("q", q);
 
 		SearchResults searchResults = searchService.search(criteria);
 		if (searchResults.isSearchedByBarcode() && !searchResults.isProduct()) {
 			session.setAttribute("barcode", searchResults.getQuery().trim());
 			if (null == frontendService.getUserService().getCurrentUser()) {
 				response.sendRedirect(frontendService.getContextPath(request)
 						+ "spring/barcodeNotFound");
 			} else {
 				response.sendRedirect(frontendService.getContextPath(request)
 						+ "spring/createProduct");
 			}
 			return null;
 		} else {
 			m.addAttribute(KEY_SEARCH_RESULTS, searchResults);
 			m.addAttribute("productList", Boolean.TRUE);
 			return new ModelAndView("searchResults", m);
 		}
 	}
 
 	@RequestMapping("/products.*")
 	public ModelMap getProductList(HttpSession session, ModelMap m,
 			@RequestParam(required = false, value = "q") String q)
 			throws Exception {
 		String query = q;
 
 		ProductSearchCriteria c = (ProductSearchCriteria) frontendService
 				.createCriteria(ProductSearchCriteria.class);
 		c.addTextKeywordFilter(query);
 		c.setItemsPerPage(0);
 
 		m.addAttribute(KEY_PRODUCT_LIST, searchService.getProductList(c));
 		return m;
 	}
 
 	@RequestMapping("/productsHelper.*")
 	public ModelMap getProductListByName(ModelMap m,
 			@RequestParam(required = false, value = "q") String q)
 			throws Exception {
 		String query = q;
 
 		ProductSearchCriteria c = (ProductSearchCriteria) frontendService
 				.createCriteria(ProductSearchCriteria.class);
 		c.setItemsPerPage(SearchCriteria.ITEMS_PER_PAGE);
 
 		if (!StringUtils.hasText(query)) {
 			m.addAttribute(KEY_PRODUCT_LIST, new ArrayList<Product>());
 		} else {
 			c.addTextNameKeywordFilter(query);
 			m.addAttribute(KEY_PRODUCT_LIST, searchService.getProductList(c));
 		}
 
 		return m;
 	}
 
 	@RequestMapping("/suggestProducts.*")
 	public ModelMap getSuggestProductsList(
 			@RequestParam(required = true, value = "id") String productId,
 			@RequestParam(required = false, value = "page") String page,
 			@RequestParam(required = false, value = "pageSize") String pageSize)
 			throws Exception {
 		ModelMap m = new ModelMap();
 
 		if (!StringUtils.hasText(productId)) {
 			m.addAttribute(KEY_PRODUCT_LIST, new ArrayList<Product>());
 		} else {
 			Product p = productService.loadProduct(productId);
 			if (null == p) {
 				m.addAttribute(KEY_PRODUCT_LIST, new ArrayList<Product>());
 			} else {
 				ProductSearchCriteria c = (ProductSearchCriteria) frontendService
 						.createCriteria(ProductSearchCriteria.class);
 				c.addCategoryFilter(p.getCategory());
 				c.addHazardFilter(E.NO_HAZARD, eService);
 				c.addHazardFilter(E.MIN_HAZARD, eService);
 				c.setOrderBy(ProductSearchCriteria.ORDER_BY_HAZARD_ASC);
 
 				c.updatePage(page);
 				c.updatePageSize(pageSize);
 
 				m.addAttribute(KEY_PRODUCT_LIST,
 						searchService.getSuggestList(c));
 			}
 		}
 
 		return m;
 	}
 
 	public void setFrontendService(FrontendService frontendService) {
 		this.frontendService = frontendService;
 	}
 
 	public FrontendService getFrontendService() {
 		return frontendService;
 	}
 
 	public void setCategoryService(CategoryService categoryService) {
 		this.categoryService = categoryService;
 	}
 
 	public CategoryService getCategoryService() {
 		return categoryService;
 	}
 
 	public void seteService(EService eService) {
 		this.eService = eService;
 	}
 
 	public EService geteService() {
 		return eService;
 	}
 
 	public ProductService getProductService() {
 		return productService;
 	}
 
 	public void setProductService(ProductService productService) {
 		this.productService = productService;
 	}
 
 }
