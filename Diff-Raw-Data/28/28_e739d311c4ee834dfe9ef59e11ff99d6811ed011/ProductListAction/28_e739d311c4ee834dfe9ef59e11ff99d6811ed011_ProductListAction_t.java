 package com.ai.action;
 
 import com.ai.model.Branch;
 import com.ai.model.Product;
 import com.ai.service.BranchService;
 import com.ai.service.PermissionService;
 import com.ai.service.ProductService;
 import com.ai.service.impl.BranchServiceImpl;
 import com.ai.service.impl.PermissionServiceImpl;
 import com.ai.service.impl.ProductServiceImpl;
 import com.ai.util.Permission;
import com.ai.util.StaticLists;
 import com.ai.validator.Validator;
 import com.ai.validator.impl.ValidatorImpl;
 import com.opensymphony.xwork2.ActionContext;
 import com.opensymphony.xwork2.ActionSupport;
 import com.ai.util.ActiveUser;
 import com.ai.util.Return;
 import org.apache.struts2.interceptor.SessionAware;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created by IntelliJ IDEA.
  * User: ishara
  * Date: 3/1/13
  * Time: 12:47 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ProductListAction extends ActionSupport implements SessionAware {
     List<Product> productsList = new ArrayList<Product>();
    public Map<String, String> categoryList = new StaticLists().getCategoryList();
     private String pName;
     private String pCategory;
     private Map session = ActionContext.getContext().getSession();
     private ActiveUser activeUser;
 
     private Logger logger = LoggerFactory.getLogger(ProductListAction.class);
     ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring.xml");
     ProductService productService = (ProductServiceImpl) applicationContext.getBean("productService");
     Validator validator = (ValidatorImpl) applicationContext.getBean("validator");
     PermissionService permissionService = (PermissionServiceImpl) applicationContext.getBean("permissionService");
     BranchService branchService = (BranchServiceImpl) applicationContext.getBean("branchService");
 
     private String handelValidator(Return r) {
         if (r == Return.VALIDATION_FAIL) {
             addActionError("Invalid request");
             return Return.INVALID_REQUEST.getReturnCode();
         } else if (r == Return.AUTHENTICATION_FAIL) {
             addActionError("Please login to the system");
             return Return.INVALID_SESSION.getReturnCode();
         } else {
             addActionError("User not authorised");
             return Return.INVALID_SESSION.getReturnCode();
         }
     }
 
     private List<Long> getBranchIds(String activeUserTenant) {
         if (isANumber(activeUserTenant)) {
             return Arrays.asList(Long.parseLong(activeUserTenant));
         } else {
             List<Long> l = new ArrayList<Long>();
             for (Branch branch : branchService.findAllByTenant(activeUserTenant)) {
                 l.add(branch.getBranchId());
             }
             return l;
         }
     }
 
     //action
     public String execute() {
         logger.info("Add new branch");
         activeUser = (ActiveUser) session.get("activeUser");
         Return r = validator.execute(activeUser, permissionService.findPermissionById(Permission.ADD_PRODUCT.getPermission()), hasActionErrors(), hasFieldErrors());
         if (r == Return.VALIDATOR_OK) {
             for (Long aLong : getBranchIds(activeUser.getTenant())) {
                 Branch b = branchService.findBranchById(aLong);
                 productsList.addAll(productService.findAllProductsByBranchId(b));
             }
             return Return.SUCCESS.getReturnCode();
         } else {
             return handelValidator(r);
         }
     }
 
     //action
     public String searchProduct() {
         logger.info("Search by name:[{}] category:[{}]", pName, pCategory);
         activeUser = (ActiveUser) session.get("activeUser");
         try {
             Return r = validator.execute(activeUser, permissionService.findPermissionById(Permission.ADD_PRODUCT.getPermission()), hasActionErrors(), hasFieldErrors());
             if (r == Return.VALIDATOR_OK) {
                 productsList = productService.searchProduct(pName, pCategory, getBranchList(activeUser.getTenant()));
                 return Return.SUCCESS.getReturnCode();
             } else {
                 return handelValidator(r);
             }
         } catch (Exception e) {
             addActionError("Internal error.Try again later");
             e.printStackTrace();
             return Return.INTERNAL_ERROR.getReturnCode();
         }
     }
 
     private List<Branch> getBranchList(String userTenant) {
         List<Branch> branches = new ArrayList<Branch>();
         if (isANumber(userTenant)) {
             branches.add(branchService.findBranchById(Long.parseLong(userTenant)));
         } else {
             branches.addAll(branchService.findAllByTenant(userTenant));
         }
         return branches;
     }
 
     private boolean isANumber(String s) {
         try {
             Long.parseLong(s);
             return true;
         } catch (Exception e) {
             return false;
         }
     }
 
     public List<Product> getProductsList() {
         return productsList;
     }
 
     public void setProductsList(List<Product> productsList) {
         this.productsList = productsList;
     }
 
     public String getpName() {
         return pName;
     }
 
     public void setpName(String pName) {
         this.pName = pName;
     }
 
     public String getpCategory() {
         return pCategory;
     }
 
     public void setpCategory(String pCategory) {
         this.pCategory = pCategory;
     }
 
     @Override
     public void setSession(Map session1) {
         this.session = session1;
     }
 
    public Map<String, String> getCategoryList() {
        return categoryList;
    }

    public void setCategoryList(Map<String, String> categoryList) {
        this.categoryList = categoryList;
    }
 }
