 package com.megalogika.sv.controller;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.acls.NotFoundException;
 import org.springframework.security.annotation.Secured;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ExtendedModelMap;
 import org.springframework.ui.ModelMap;
 import org.springframework.util.StringUtils;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.multipart.MultipartHttpServletRequest;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.megalogika.sv.model.Comment;
 import com.megalogika.sv.model.Confirmation;
 import com.megalogika.sv.model.E;
 import com.megalogika.sv.model.Product;
 import com.megalogika.sv.model.ProductCategory;
 import com.megalogika.sv.model.ProductChange;
 import com.megalogika.sv.model.Report;
 import com.megalogika.sv.model.User;
 import com.megalogika.sv.model.conversion.ProductCategoryPropertyEditor;
 import com.megalogika.sv.service.CategoryService;
 import com.megalogika.sv.service.CommentService;
 import com.megalogika.sv.service.CommentsCriteria;
 import com.megalogika.sv.service.EService;
 import com.megalogika.sv.service.EmailActions;
 import com.megalogika.sv.service.ProductService;
 import com.megalogika.sv.service.UploadService;
 import com.megalogika.sv.service.UserService;
 
 @Controller("productController")
 public class ProductController {
 	public transient Logger logger = Logger.getLogger(ProductController.class);
 
 	public static final String KEY_VIEWED_IN_THIS_SESSION = "viewedInThisSession";
 	public final static String KEY_PRODUCT = "product";
 	public final static String KEY_PRODUCT_SERVICE = "productService";
 	public final static String KEY_ADDED_TO_BASKET = "addedToBasket";
 	public final static String KEY_REMOVED_FROM_BASKET = "removedFromBasket";
 	public final static String KEY_CREATE_PRODUCT_IN_PROGRESS = "createProductInProgress";
 	public final static String KEY_EDITING = "editing";
 	public final static String KEY_PRODUCT_LIST = "productList";
 	
 	@Autowired 
 	private FrontendService frontendService;
 	@Autowired
 	private ProductService productService;
 	@Autowired
 	private CommentService commentService;
 	@Autowired
 	private EmailActions emailActions;
 	@Autowired
 	private EService eService;
 	@Autowired
 	private UserService userService;
 	@Autowired
 	private CategoryService categoryService;
 	@Autowired
 	private UploadService uploadService;
 
 	@ModelAttribute("cmd")
 	public ProductEditCommand getCmd() {
 		return new ProductEditCommand();
 	}
 
 	@InitBinder
 	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) {
         binder.registerCustomEditor(ProductCategory.class, "category", new ProductCategoryPropertyEditor(frontendService.getCategoryService()));
     }	
 	
 	@ModelAttribute(FrontendService.KEY_CONTEXT_PATH)
 	public String getContextPath(HttpServletRequest request) {
 		return frontendService.getContextPath(request);
 	}
 	
 	@ModelAttribute(FrontendService.KEY_HAZARD_DESCRIPTIONS)
 	public Map<String,String> getHazardDescriptions() {
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
 	
 	@ModelAttribute(KEY_PRODUCT)
 	public Product getProduct(@RequestParam(required=false, value="id") String id) {
 		if (! StringUtils.hasText(id)) {
 			return null;
 		} else {
 			Product p = productService.loadProduct(id);
 			if (null == p) {
 				throw new NotFoundException("No such product!");
 			}
 			
 			return p;
 		}
 	}
 	
 //	@ModelAttribute
 //	public Comment getComment(@RequestParam(required=true, value="id") String id) {
 //		if (null != frontendService.getUserService().getCurrentUser()) {
 //			Comment ret = commentService.createComment(getProduct(id));
 //			logger.debug("GET COMMENT RET: " + ret);
 //			return ret;
 //		} else {
 //			return null;
 //		}
 //	}
 	
 	public boolean isEditing(HttpSession session) {
 		return (null != session.getAttribute(KEY_EDITING) && (Boolean) session.getAttribute(KEY_EDITING));
 	}
 	
 	private ExtendedModelMap prepareProductModel(ExtendedModelMap m, Product p, HttpServletRequest request, HttpSession session) throws InstantiationException, IllegalAccessException {
 		if (null != frontendService.getUserService().getCurrentUser()) {
 			m.addAttribute(commentService.createComment(frontendService.getUserService().getCurrentUser(), p));
 		}
 		
 		CommentsCriteria criteria = (CommentsCriteria) frontendService.getCriteria(session, CommentsCriteria.class);
 		criteria.setProduct(p);
 		m.addAttribute(FrontendService.CRITERIA, criteria);
 		
 		m.addAttribute(KEY_CREATE_PRODUCT_IN_PROGRESS, request.getParameter(KEY_CREATE_PRODUCT_IN_PROGRESS));
 		m.addAttribute(KEY_PRODUCT_SERVICE, productService);
 
 		if(productService.isActionPerformedByAdmin()) {
 			List<Product> sameBarcodeProducts = productService.getByBarcode(p.getBarcode());
 			m.addAttribute("sameBarcodeProducts", sameBarcodeProducts);
 		}
 		
 		return m;
 	}
 
 	@RequestMapping("/foundBarcode")
 	@Secured({"ROLE_ANONYMOUS", "ROLE_USER", "ROLE_ADMIN", "IS_AUTHENTICATED_ANONYMOUSLY"})
 	public ModelAndView foundBarcode(HttpServletRequest request, HttpServletResponse response, HttpSession session,
 				@RequestParam(required=true, value="id") String id) throws Exception 
 	{
 		return new ModelAndView("foundBarcode", getProduct(request, response, session, id));
 	}
 	
 	@RequestMapping("/product")
 	@Secured({"ROLE_ANONYMOUS", "ROLE_USER", "ROLE_ADMIN", "IS_AUTHENTICATED_ANONYMOUSLY"})
 	public ExtendedModelMap getProduct(HttpServletRequest request, HttpServletResponse response, HttpSession session,
 				@RequestParam(required=true, value="id") String id
 				) 
 		throws Exception 
 	{
 		String productId = id;
 		if (! StringUtils.hasText(productId)) {
 			productId = (String) session.getAttribute(KEY_PRODUCT);
 		}
 		
 		if (! StringUtils.hasText(id)) {
 			response.sendError(HttpServletResponse.SC_NOT_FOUND);
 			return null;
 		}
 		
 		session.setAttribute(KEY_PRODUCT, id);
 		
 		Product p = productService.loadProduct(id);
 		if (null == p) {
 			response.sendError(HttpServletResponse.SC_NOT_FOUND);
 			return null;
 		}
 		
 		if (null == session.getAttribute(KEY_VIEWED_IN_THIS_SESSION)) {
 			p = productService.addView(p);
 			session.setAttribute(KEY_VIEWED_IN_THIS_SESSION, Boolean.TRUE);
 		}
 		
 		if (isEditing(session)) {
 			session.setAttribute(KEY_EDITING, Boolean.FALSE);
 		}
 		
 		return prepareProductModel(new ExtendedModelMap(), p, request, session);	
 		
 	}
 	
 	@RequestMapping("/product/edit")
 	@Secured({"ROLE_ANONYMOUS", "ROLE_USER", "ROLE_ADMIN", "IS_AUTHENTICATED_ANONYMOUSLY"})
 	public ModelAndView getProductEdit(HttpServletRequest request, HttpServletResponse response, HttpSession session,
 				@RequestParam(required=true, value="id") String id
 				) throws Exception 
 	{
 		Product p = productService.loadProduct(id);
 		if (null == p || p.isConfirmed() || ! p.canBeEditedBy(frontendService.getUserService().getCurrentUser())) {
 			logger.error("Product " + p + " can not be edited now by " + frontendService.getUserService().getCurrentUser());
 			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
 			return null;
 		} else {
 			ModelMap ret = getProduct(request, response, session, id);
 			session.setAttribute(KEY_EDITING, Boolean.TRUE);
 			return new ModelAndView("productEdit", ret);
 		}
 	}
 	
 	@RequestMapping("/comment/page")
 	public String pageComments(HttpServletRequest request, HttpServletResponse response, HttpSession session,
 			@RequestParam(required=false, value="page") String page,
 			@RequestParam(required=false, value="pageSize") String pageSize
 			) 
 	throws Exception 
 	{
 		CommentsCriteria criteria = (CommentsCriteria) frontendService.getCriteria(session, CommentsCriteria.class);
 		
 		if (null == criteria.getProduct()) {
 			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			return "redirect:/spring/404";
 		}
 		
 		if (StringUtils.hasText(page) || StringUtils.hasText(pageSize)) {
 			criteria.updatePage(page);
 			criteria.updatePageSize(pageSize);
 		}
 		
 		return "redirect:/spring/product?id=" + criteria.getProduct().getId();
 	}
 	
 	@RequestMapping(value="/comment/add", method={RequestMethod.POST})
 	@Secured({"ROLE_USER", "ROLE_ADMIN"})
 	public String addComment(HttpServletResponse response, 
 			@RequestParam(required=true, value="id") String id,
 			@ModelAttribute(value="comment") Comment comment) throws IOException {
 		
 		Product p = productService.loadProduct(id);
 		if (null == p) {
 			logger.error("Product not found, cannot add comment: " + comment);
 			response.sendError(HttpServletResponse.SC_NOT_FOUND);
 			return "redirect:/spring/404";
 		}
 
 		try {
 			commentService.addCommentToProduct(p, (Comment) comment.clone());
 		} catch (CloneNotSupportedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return "redirect:/spring/product?id=" + p.getId();
 	}
 	
 	@RequestMapping(value="/comment/delete")
 	@Secured({"ROLE_ADMIN"})
 	public String deleteComment(HttpServletResponse response,
 					@RequestParam(required=true, value="cid") String cid) throws IOException {
 	
 		if (! StringUtils.hasText(cid)) {
 			logger.error("No Comment ID, can not delete! ");
 			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			return "redirect:/spring/500";
 		} else {
 			Comment comment = commentService.load(cid);
 			if (null == comment) {
 				logger.error("No comment for id: " + cid);
 				response.sendError(HttpServletResponse.SC_NOT_FOUND);
 				return "redirect:/spring/404";
 			} else {
 				if (null == comment.getProduct()) {
 					logger.error("Comment for id: " + cid + "(" + comment + ") has no product assigned!");
 					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 					return "redirect:/spring/500";
 					
 				}
 				Product p = comment.getProduct();
 				commentService.deleteComment(p, comment);
 				
 				return "redirect:/spring/product?id=" + p.getId();
 			}
 		}
 	}
 	
 	@RequestMapping(value="/product/delete")
 	@Secured({"ROLE_ADMIN"})
 	public String deleteProduct(HttpServletResponse response, @RequestParam(required=true, value="id") String id) throws IOException {
 		
 		if (! StringUtils.hasText(id)) {
 			logger.warn("No product id specified to delete!");
 			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			return "redirect:/spring/500";
 		} else {
 			Product p = productService.loadProduct(id);
 			if (null == p) {
 				logger.warn("Null product to delete for ID (" + id + ")");
 				response.sendError(HttpServletResponse.SC_NOT_FOUND);
 				return "redirect:/spring/404";
 			} 
 			
 			productService.delete(p);
 		}
 		
 		return "redirect:/spring/productList";
 	}
 	
 	@RequestMapping(value="/product/confirm")
 	@Secured({"ROLE_USER", "ROLE_ADMIN"})
 	public String confirmProductDescription(HttpServletResponse response, HttpSession session, 
 			@RequestParam(required=true, value="id") String id) throws IOException {
 		
 		if (! StringUtils.hasText(id)) {
 			logger.warn("No product id specified to confirm description!");
 			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			return "redirect:/spring/500";
 		} else {
 			Product p = productService.loadProduct(id);
 			if (null == p) {
 				logger.warn("Null product to confirm description for ID (" + id + ")");
 				response.sendError(HttpServletResponse.SC_NOT_FOUND);
 				return "redirect:/spring/404";
 			} 
 			
 			User u = frontendService.getUserService().getCurrentUser();
 			if (p.canBeConfirmedBy(u)) {
 				Confirmation c = new Confirmation(p, u);
 				productService.confirm(p, c);
 			} else {
 				logger.warn("User " + u + " should not be confirming product " + p);
 				response.sendError(HttpServletResponse.SC_FORBIDDEN);
 				return "redirect:/spring/403";
 			}
 			
 			session.setAttribute(KEY_EDITING, Boolean.FALSE);
 			
 			return "redirect:/spring/product?id=" + p.getId();
 		}
 	}
 	
 	@RequestMapping(value="/product/email",method={RequestMethod.POST})
 	public String emailProductLink(HttpServletResponse response,
 			@RequestParam(required=true, value="id") String id,
 			@RequestParam(required=true, value="emailTo") String emailTo,
 			@RequestParam(required=false, value="emailLinkText") String emailLinkText
 									) throws IOException 
 	{
 		if (! StringUtils.hasText(id)) {
 			logger.warn("No product id specified to email link to!");
 			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			return "redirect:/spring/500";
 		} else {
 			Product p = productService.loadProduct(id);
 			if (null == p) {
 				logger.warn("Null product to email link to for ID (" + id + ")");
 				response.sendError(HttpServletResponse.SC_NOT_FOUND);
 				return "redirect:/spring/404";
 			} 
 			
 			emailActions.sendProductLink(p, emailTo, emailLinkText);
 			
 			return "redirect:/spring/product?id=" + p.getId();
 		}
 	}
 	
 	@RequestMapping(value="/product/report")
 	public String reportProduct(HttpServletResponse response, HttpSession session,
 			@RequestParam(required=true, value="id") String id
 									) throws IOException 
 	{
 		if (! StringUtils.hasText(id)) {
 			logger.warn("No product id specified to report!");
 			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			return "redirect:/spring/500";
 		} else {
 			Product p = productService.loadProduct(id);
 			if (null == p) {
 				logger.warn("Null product to report for ID (" + id + ")");
 				response.sendError(HttpServletResponse.SC_NOT_FOUND);
 				return "redirect:/spring/404";
 			} 
 			
 			User u = frontendService.getUserService().getCurrentUser();
 			Report r = new Report(p, u);
 			productService.report(p, r);
 //			emailActions.sendProductProblemEmail(p, emailFrom, emailText);
 			
 			if (isEditing(session)) {
 				return "redirect:/spring/product/edit?id=" + p.getId();
 			} else {
 				return "redirect:/spring/product?id=" + p.getId();
 			}
 		}
 	}	
 	
 	@RequestMapping(value="/product/remove")
 	public String removeAdditive(HttpServletResponse response,
 			@RequestParam(required=true, value="id") String id,
 			@RequestParam(required=true, value="eid") String eid)
 		throws IOException
 	{
 		if (! StringUtils.hasText(id)) {
 			logger.warn("No product id specified to remove e " + eid + " from!");
 			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			return "redirect:/spring/500";
 		}
 		
 		if (! StringUtils.hasText(eid)) {
 			logger.warn("No e id specified to remove from product " + id);
 			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			return "redirect:/spring/500";
 		}
 
 		Product p = productService.loadProduct(id);
 		if (null == p) {
 			logger.warn("Null product to remove e " + eid + " for ID (" + id + ")");
 			response.sendError(HttpServletResponse.SC_NOT_FOUND);
 			return "redirect:/spring/404";
 		} 
 		
 		E e = eService.load(eid);
 		if (null == e) {
 			logger.warn("Additive not found for id " + eid + "// was intended to be removed from " + p);
 			response.sendError(HttpServletResponse.SC_NOT_FOUND);
 			return "redirect:/spring/404";
 		}
 		
 		productService.removeAdditive(p, e);
 		productService.addChange(p, new ProductChange(p, frontendService.getUserService().getCurrentUser()));
 		productService.removeConfirmations(p);
 		
 		return "redirect:/spring/product/edit?id=" + p.getId();
 	}
 	
 	@RequestMapping(value="/product/updateValue",method=RequestMethod.GET)
 	public String updateProductValue(HttpServletResponse response, HttpServletRequest request, ModelMap model,
 			@ModelAttribute("cmd") ProductEditCommand cmd)
 		throws IOException
 	{
 		for (Object name: request.getParameterMap().keySet()) {
 			logger.debug(name + ": " + request.getParameter((String)name));
 		}
 		
 		String reply = cmd.value;
 		
 		logger.debug("CMD " + cmd);
 		logger.debug("VALUE " + cmd.value);
 		
 		if (! StringUtils.hasText(cmd.value)) {
 			reply = "No new value to update " + cmd.field + " to " + cmd.value + "!";
 			logger.warn(reply);
 			response.sendError(HttpServletResponse.SC_CONFLICT, reply);
 		} else
 		if (! StringUtils.hasText(cmd.id)) {
 			reply = "No product id specified to update " + cmd.field + " to " + cmd.value + "!";
 			logger.warn(reply);
 			response.sendError(HttpServletResponse.SC_CONFLICT, reply);
 		} else {
 			if (! StringUtils.hasText(cmd.field)) {
 				reply = "No field specified to update at product " + cmd.id;
 				logger.warn(reply);
 				response.sendError(HttpServletResponse.SC_CONFLICT, reply);
 			} else {
 				if (null == cmd.value) {
 					reply = "No value specified to update field " + cmd.field + " at product " + cmd.id;
 					logger.warn(reply);
 					response.sendError(HttpServletResponse.SC_CONFLICT, reply);
 				} else {
 					boolean error = false;
 					
 					if (cmd.unique) {
 						Product existing = null;
 						try {
 							existing = productService.loadProductByField(cmd.field, cmd.value);
 						} catch (Exception e) {
 							reply = "Error while checking for existing field " + cmd.field + " with value " + cmd.value + " at product " + cmd.id;
 							logger.warn(reply, e);
 							response.sendError(HttpServletResponse.SC_CONFLICT, reply);
 							error = true;
 						}
 						if (null != existing) {
 							reply = "This value already present in the database for some other product ( " + existing + ")" + cmd.field + " at product " + cmd.id;
 							logger.warn(reply);
 							response.sendError(HttpServletResponse.SC_CONFLICT, reply);
 							error = true;
 						}
 					} else {
 						logger.debug("UNIQUE NOT REQUESTED");
 					}
 					
 					if (! error) {
 						Product p = productService.loadProduct(cmd.id);
 						
 						if (null == p) {
 							reply = "Null product for id " + cmd.id + " to update " + cmd.field + " to " + cmd.value + "!";
 							logger.warn(reply);
 							response.sendError(HttpServletResponse.SC_CONFLICT, reply);
 						} else {
 							try {
 								productService.updateProduct(p, cmd.field, cmd.value, frontendService.getUserService().getCurrentUser());
 							} catch (Exception e) {
 								reply = "Could not update field " + cmd.field + " to " + cmd.value + " on product " + p;
 								logger.error(reply, e);
 								response.sendError(HttpServletResponse.SC_CONFLICT, reply);
 							}
 						}
 					}
 				}
 			}
 		}
 		model.addAttribute("reply", reply);
 		
 		logger.debug("AJAX REPLY: " + reply);
 		
 		return "ajaxReply";
 	}
 
 	@RequestMapping(value="/product/updateBarcode", method=RequestMethod.GET)
 	public String updateBarcode(HttpServletResponse response, ModelMap model,
 			@ModelAttribute("cmd") ProductEditCommand cmd)
 		throws IOException
 	{
 		String reply = cmd.getValue();
 		
 		if (! StringUtils.hasText(cmd.value)) {
 			reply = "No new value to update " + cmd.field + " to " + cmd.getValue() + "!";
 			logger.warn(reply);
 			response.sendError(HttpServletResponse.SC_CONFLICT, reply);
 		}
 		if (! StringUtils.hasText(cmd.id)) {
 			reply = "No product id specified to update " + cmd.field + " to " + cmd.getValue() + "!";
 			logger.warn(reply);
 			response.sendError(HttpServletResponse.SC_CONFLICT, reply);
 		} else {
 			if (! StringUtils.hasText(cmd.field)) {
 				reply = "No field specified to update at product " + cmd.id;
 				logger.warn(reply);
 				response.sendError(HttpServletResponse.SC_CONFLICT, reply);
 			} else {
 				if (null == cmd.getValue()) {
 					reply = "No value specified to update field " + cmd.field + " at product " + cmd.id;
 					logger.warn(reply);
 					response.sendError(HttpServletResponse.SC_CONFLICT, reply);
 				} else {
 					boolean error = false;
 
 					if (! productService.isValidBarcode(cmd.value)) {
 						reply = "Incorrect barcode " + cmd.value;
 						logger.warn(reply);
 						response.sendError(HttpServletResponse.SC_CONFLICT, reply);
 					} else {
 						if (cmd.unique) {
 							Product existing = null;
 							try {
 								existing = productService.loadProductByField(cmd.field, Long.parseLong(cmd.value));
 							} catch (Exception e) {
 								reply = "Error while checking for existing field " + cmd.field + " with value " + cmd.value + " at product " + cmd.id;
 								logger.warn(reply, e);
 								response.sendError(HttpServletResponse.SC_CONFLICT, reply);
 								error = true;
 							}
 							if (null != existing) {
 								reply = "This value already present in the database for some other product ( " + existing + ")" + cmd.field + " at product " + cmd.id;
 								logger.warn(reply);
 								response.sendError(HttpServletResponse.SC_CONFLICT, reply);
 								error = true;
 							}
 						} else {
 							logger.debug("UNIQUE NOT REQUESTED");
 						}
 						
 						if (! error) {
 							Product p = productService.loadProduct(cmd.id);
 							
 							if (null == p) {
 								reply = "Null product for id " + cmd.id + " to update " + cmd.field + " to " + cmd.value + "!";
 								logger.warn(reply);
 								response.sendError(HttpServletResponse.SC_CONFLICT, reply);
 							} else {
 								try {
 									productService.updateProduct(p, "barcode", cmd.value, frontendService.getUserService().getCurrentUser());
 								} catch (Exception e) {
 									reply = "Could not update field " + cmd.field + " to " + cmd.value + " on product " + p;
 									logger.error(reply, e);
 									response.sendError(HttpServletResponse.SC_CONFLICT, reply);
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		model.addAttribute("reply", reply);
 		
 		return "ajaxReply";
 	}		
 	
 	@RequestMapping(value="/product/updateCategory", method=RequestMethod.GET)
 	public String updateCategory(HttpServletResponse response, ModelMap model,
 			@ModelAttribute("cmd") ProductEditCommand cmd)
 		throws IOException
 	{
 		logger.debug("CMD " + cmd);
 		logger.debug("VALUE " + cmd.value);
 		
 		if (! StringUtils.hasText(cmd.id)) {
 			logger.warn("No product id specified to update category to"  + cmd.category + "!");
 			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			return "redirect:/spring/500";
 		}
 		
 		if (null == cmd.category) {
 			logger.warn("No new category specified to update at product " + cmd.id);
 			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			return "redirect:/spring/500";
 		}
 		
 		Product p = productService.loadProduct(cmd.id);
 		
 		if (null == p) {
 			logger.warn("Null product for id " + cmd.id + " to update category to" + cmd.category);
 			response.sendError(HttpServletResponse.SC_NOT_FOUND);
 			return "redirect:/spring/404";
 		} 
 		
 		try {
 			productService.updateProduct(p, "category", cmd.category, frontendService.getUserService().getCurrentUser());
 		} catch (Exception e) {
 			logger.error("Could not update category to " + cmd.category + " on product " + p, e);
 			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 			return "redirect:/spring/500";
 		}
 		
 		model.addAttribute("reply", cmd.category.getName());
 		
 		return "ajaxReply";
 	}	
 	
 	@RequestMapping(value="/product/updateConservants", method=RequestMethod.GET)
 	public String updateConservants(HttpServletResponse response, ModelMap model,
 			@ModelAttribute("cmd") ProductEditCommand cmd)
 		throws IOException
 	{
 		logger.debug("CMD " + cmd);
 		logger.debug("VALUE " + cmd.value);
 		
 		String reply = cmd.value;
 		
 		if (! StringUtils.hasText(cmd.id)) {
 			reply = "No product id specified to update conservants list to"  + cmd.value + "!";
 			response.sendError(HttpServletResponse.SC_CONFLICT, reply);
 		} else {
 			Product p = productService.loadProduct(cmd.id);
 			
 			if (null == p) {
 				reply = "Null product for id " + cmd.id + " to update conservants list to" + cmd.value;
 				response.sendError(HttpServletResponse.SC_CONFLICT, reply);
 			} else { 
 				try {
 					productService.updateProduct(p, cmd.field, cmd.value, frontendService.getUserService().getCurrentUser());
 					productService.updateConservants(p);
 					reply = p.getConservantsText();
 				} catch (Exception e) {
 					reply = "Could not update conservants list to " + cmd.value + " on product " + p;
 					logger.debug(reply, e);
 					response.sendError(HttpServletResponse.SC_CONFLICT, reply);
 				}
 			}
 		}
 		
 		model.addAttribute("reply", reply);
 		
 		return "ajaxReply";
 	}	
 	
 	@RequestMapping("/product/eTable")
 	@Secured({"ROLE_USER", "ROLE_ADMIN", "IS_AUTHENTICATED_ANONYMOUSLY"})
 	public String getProduct(HttpServletRequest request, HttpServletResponse response, HttpSession session, ModelMap model,
 				@RequestParam(required=true, value="id") String id
 				) 
 		throws Exception 
 	{
 		if (! StringUtils.hasText(id)) {
 			response.sendError(HttpServletResponse.SC_NOT_FOUND);
 			return null;
 		}
 		
 		Product p = productService.loadProduct(id);
 		if (null == p) {
 			response.sendError(HttpServletResponse.SC_NOT_FOUND);
 			return null;
 		}
 		
 		
 		return "eTable";	
 	}
 	
 	public void setProductService(ProductService productService) {
 		this.productService = productService;
 	}
 
 	public ProductService getProductService() {
 		return productService;
 	}
 
 	public void setCommentService(CommentService commentService) {
 		this.commentService = commentService;
 	}
 
 	public CommentService getCommentService() {
 		return commentService;
 	}
 
 	public void setFrontendService(FrontendService frontendService) {
 		this.frontendService = frontendService;
 	}
 
 	public FrontendService getFrontendService() {
 		return frontendService;
 	}
 
 	public void setEmailActions(EmailActions emailActions) {
 		this.emailActions = emailActions;
 	}
 
 	public EmailActions getEmailActions() {
 		return emailActions;
 	}
 
 	public void seteService(EService eService) {
 		this.eService = eService;
 	}
 
 	public EService geteService() {
 		return eService;
 	}
 	
 	@RequestMapping("/products/byBarcode.*")
 	public ModelMap getProductListByName(HttpServletResponse response,
 			@RequestParam(required=true, value="q") String q) throws Exception 
 	{
 		long barcode;
 		if (! StringUtils.hasText(q)) {
 			logger.warn("get product by barcode: no query!");
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No query!");
 			return null;
 		}
 		
 		try {
 			barcode = Long.parseLong(q.trim());
 		} catch (NumberFormatException e) {
 			logger.warn("get product by barcode: requested barcode is not a number!");
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "requested barcode is not a number!");
 			return null;
 		}
 
 		if (! productService.isValidBarcode(q.trim())) {
 			logger.warn("get product by barcode: requested barcode is not a valid barcode!");
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "requested barcode is not a valid barcode!");
 			return null;
 		}
 		
 		ModelMap m = new ModelMap();
 		m.addAttribute(KEY_PRODUCT_LIST, productService.getByBarcode(q.trim()));
 		
 		return m;
 	}
 	
 	@RequestMapping(value="/product/add", method=RequestMethod.POST)
 	@Secured({"ROLE_ANONYMOUS", "ROLE_USER", "ROLE_ADMIN", "IS_AUTHENTICATED_ANONYMOUSLY"})
 	public String addProduct(MultipartHttpServletRequest request, HttpServletResponse response, HttpSession session,
 				@RequestParam(required=true, value="barcode") String barcode,
 				@RequestParam(required=true, value="name") String name,
 				@RequestParam(required=true, value="company") String company,
 				@RequestParam(required=true, value="category") String category,
 				@RequestParam(required=true, value="conservantsText") String conservantsText
 				) throws Exception 
 	{
 		Product p = productService.createProduct(request, userService.getCurrentUser(), barcode);
 		
 		logger.debug("CREATED PRODUCT: " + p);
 		
 		p.setName(name);
 		p.setCompany(company);
 		p.setCategory(categoryService.getProductCategory(Long.parseLong(category)));
 		
 		if (null == p.getCategory()) {
 			p.setCategory(categoryService.getProductCategory(CategoryService.CATEGORY_MISC));
 		}
 		
 		p.setConservantsText(conservantsText);
 		
 		logger.debug("FILLED IN SOME FIELDS: " + p);
 		
 		p.setLabel(uploadService.getPhoto(request, "labelPhoto"));
 		p.setIngredients(uploadService.getPhoto(request, "ingredientsPhoto"));
 
 		logger.debug("ASSIGNED PHOTOS: " + p);
 		
 		productService.updateConservants(p);
 		
		logger.debug("UPDATED CONSERVANTS: " + p);
		
		p.confirm(new Confirmation(p, getCurrentUser()));
		
		logger.debug("CONFIRMED PRODUCT: " + p);
		
 		p = productService.saveNew(p);
 		
 		logger.debug("SAVED PRODUCT: " + p);
 		
 		return "redirect:/spring/product?id=" + p.getId();
 		
 	}
 }
