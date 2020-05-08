 package com.megalogika.sv.controller;
 
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.ui.ModelMap;
 import org.springframework.util.Assert;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.webflow.context.ExternalContext;
 
 import com.megalogika.sv.model.ProductCategory;
 import com.megalogika.sv.service.BlogReader;
 import com.megalogika.sv.service.CategoryService;
 import com.megalogika.sv.service.ESearchCriteria;
 import com.megalogika.sv.service.EService;
 import com.megalogika.sv.service.SearchCriteria;
 import com.megalogika.sv.service.UserService;
 import com.sun.syndication.feed.synd.SyndFeed;
 
 @Component("frontendService")
 public class FrontendService {
 	public static final String CRITERIA = "criteria";
 
 	protected transient Logger logger = Logger.getLogger(FrontendService.class);
 	
 	public final static String KEY_CURRENT_USER = "currentUser";
 	public final static String KEY_HAZARD_DESCRIPTIONS = "hazardDescriptions";
 	public final static String KEY_CATEGORY_LIST = "categoryList";
 	public final static String KEY_CONTEXT_PATH = "cp";
 
 	@Autowired
 	private EService eService;
 	@Autowired
 	private UserService userService;
 	@Autowired
 	private BlogReader blogReader;
 	@Autowired
 	private CategoryService categoryService;
 
 	public final static String KEY_PRODUCTS = "products";

 	
 	@RequestMapping("/zymejimoPaaiskinimai")
 	public String zymejimoPaaiskinimai() {
 		return "zymejimoPaaiskinimai";
 	}
 	
 	public Map<String,String> getHazardDescriptions() {
 		return geteService().getHazardDescriptions();
 	}
 	
 	public ModelMap addHazardDescriptions(ModelMap m) {
 		return m.addAttribute(FrontendService.KEY_HAZARD_DESCRIPTIONS, getHazardDescriptions());
 	}
 	
 	public ModelMap addCurrentUser(ModelMap m) {
 		return m.addAttribute(KEY_CURRENT_USER, getUserService().getCurrentUser());
 	}
 
 	public ModelMap addBlogFeed(ModelMap m, HttpServletRequest request) {
 		SyndFeed feed = blogReader.getSyndFeed();
 		blogReader.fixBlogEntryLinks(feed, request);
 		return m.addAttribute("feed", blogReader.getSyndFeed());
 	}
 	
 	public List<ProductCategory> getCategoryList() {
 		return categoryService.getList();
 	}
 	
 	public ModelMap addCategoryList(ModelMap m) {
 		return m.addAttribute(KEY_CATEGORY_LIST, getCategoryList());
 	}
 	
 	public ModelMap addCriteria(ModelMap m, HttpSession session, Class<? extends SearchCriteria> criteriaClass) throws InstantiationException, IllegalAccessException {
 		SearchCriteria criteria =  getCriteria(session, criteriaClass);
 		return m.addAttribute(CRITERIA, criteria);
 	}
 
 	protected SearchCriteria createCriteria(Class<? extends SearchCriteria> criteriaClass) throws InstantiationException, IllegalAccessException {
 		SearchCriteria criteria = criteriaClass.newInstance();
 		criteria.clear();
 		
 		return criteria;
 	}
 	
 	protected SearchCriteria getCriteria(HttpSession session, Class<? extends SearchCriteria> criteriaClass) throws InstantiationException, IllegalAccessException {
 		SearchCriteria criteria = criteriaClass.cast(session.getAttribute(criteriaClass.getName()));
 		
 		if (null == criteria) {
 			criteria = criteriaClass.newInstance();
 			setCriteria(session, criteriaClass.cast(criteria));
 		} else {
 			logger.debug("CRITERIA " + criteriaClass.getName() + " FROM SESSION");
 		}
 
 		logger.debug("FILTERS: " + criteria.getFilters());
 		
 		return criteria;
 	}
 	
 	public ESearchCriteria getESearchCriteria(ExternalContext extCtx) throws InstantiationException, IllegalAccessException {
 		return (ESearchCriteria) getCriteria(((HttpServletRequest) extCtx.getNativeRequest()).getSession(false), ESearchCriteria.class);
 	}
 	
 	public SearchCriteria setCriteria(HttpSession session, SearchCriteria criteria) {
 		session.setAttribute(criteria.getClass().getName(), criteria);
 		
 		return criteria;
 	}
 
 	public void seteService(EService eService) {
 		this.eService = eService;
 	}
 	
 	public EService geteService() {
 		return eService;
 	}
 	
 	public void setUserService(UserService userService) {
 		this.userService = userService;
 	}
 	
 	public UserService getUserService() {
 		return userService;
 	}
 
 	public void setBlogReader(BlogReader blogReader) {
 		this.blogReader = blogReader;
 	}
 
 	public BlogReader getBlogReader() {
 		return blogReader;
 	}
 
 	public void setCategoryService(CategoryService categoryService) {
 		this.categoryService = categoryService;
 	}
 
 	public CategoryService getCategoryService() {
 		return categoryService;
 	}
 
 	public String getContextPath(String cp) {
 		Assert.notNull(cp);
 		
 		logger.debug(cp);
 		
 		if ("/".equals(cp)) {
 			return "/";
 		} else {
 			return cp + "/";
 		}
 	}
 	
 	public String getContextPath(HttpServletRequest request) {
 		return getContextPath(request.getContextPath());
 	}
 	
 	public void addContextPath(ModelMap m, HttpServletRequest request) {
 		m.addAttribute(KEY_CONTEXT_PATH, getContextPath(request.getContextPath()));
 		
 	}
 	
 	protected ModelMap makeModelMap(HttpServletRequest request) {
 		ModelMap map = new ModelMap();
 		map.addAttribute(KEY_CONTEXT_PATH, getContextPath(request.getContextPath()));
 		addCurrentUser(map);
 		return map;
 	}
 }
