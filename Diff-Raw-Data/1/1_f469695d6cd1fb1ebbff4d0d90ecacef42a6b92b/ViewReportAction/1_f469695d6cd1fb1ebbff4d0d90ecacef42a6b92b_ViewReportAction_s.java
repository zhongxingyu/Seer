 package com.ai.action;
 
 import com.ai.model.*;
 import com.ai.service.*;
 import com.ai.service.impl.*;
 import com.ai.util.ActiveUser;
 import com.ai.util.Permission;
 import com.ai.util.Return;
 import com.ai.util.Status;
 import com.ai.validator.Validator;
 import com.ai.validator.impl.ValidatorImpl;
 import com.opensymphony.xwork2.ActionContext;
 import com.opensymphony.xwork2.ActionSupport;
 import com.opensymphony.xwork2.ModelDriven;
 import org.apache.struts2.interceptor.SessionAware;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created by IntelliJ IDEA.
  * User: ishara
  * Date: 4/23/13
  * Time: 10:15 AM
  * To change this template use File | Settings | File Templates.
  */
 public class ViewReportAction extends ActionSupport implements ModelDriven<Invoice>, SessionAware {
     private Map session = ActionContext.getContext().getSession();
     private ActiveUser activeUser;
     private Logger logger = LoggerFactory.getLogger(ViewReportAction.class);
     @Autowired
     private InvoiceService invoiceService;
     @Autowired
     private PermissionService permissionService;
     @Autowired
     private Validator validator;
     @Autowired
     private UserService userService;
     @Autowired
     private ProductService productService;
     @Autowired
     private BranchService branchService;
 
     private Invoice invoice = new Invoice();
     private List<PurchaseItem> purchaseItemList = new ArrayList<PurchaseItem>();
     private String iId;
     private String sellerName;
     private Branch branch;
 
 
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
 
     public String execute() {
         logger.info("View invoice by id:[{}]", iId);
         try {
             activeUser = (ActiveUser) session.get("activeUser");
 
             Return r = validator.execute(activeUser, permissionService.findPermissionById(Permission.VIEW_REPORT.getPermission()), hasActionErrors(), hasFieldErrors());
             if (r == Return.VALIDATOR_OK) {
                 invoice = invoiceService.findInvoiceById(Long.parseLong(iId));
                 for (PurchaseItem purchaseItem : invoice.getPurchaseItems()) {
                     purchaseItem.setProduct(productService.findProductById(purchaseItem.getProduct().getProductId()));
                 }
                 purchaseItemList.addAll(invoice.getPurchaseItems());
 
                 User u = userService.findUserById(invoice.getEmployer());
                 sellerName = u.getPersonalDetails().getFirstName() + " " + u.getPersonalDetails().getLastName();
                 branch = branchService.findBranchById(Long.parseLong(invoice.getTenant()));
                 return Return.SUCCESS.getReturnCode();
             } else {
                 return handelValidator(r);
             }
 
         } catch (Exception e) {
             logger.info("Exception occurred in load add user");
             e.printStackTrace();
             addActionError("Internal error.Try again later");
             return Return.INTERNAL_ERROR.getReturnCode();
         }
     }
 
     public String reportReturn() {
         logger.info("Return the invoice:[{}]", iId);
         invoice = invoiceService.findInvoiceById(Long.parseLong(iId));
         if (invoice != null && invoice.getStatus().equals(Status.ACTIVE.getStatus())) {
             invoice.setStatus(Status.RETURNED.getStatus());
             invoiceService.updateInvoice(invoice);
             updateThePurchaseItems(invoice);
             execute();
         }
         return Return.SUCCESS.getReturnCode();
     }
 
     private void updateThePurchaseItems(Invoice invoice1) {
         for (PurchaseItem purchaseItem : invoice1.getPurchaseItems()) {
             Product productInStock = productService.findProductById(purchaseItem.getProduct().getProductId());
             productInStock.setQuantity(productInStock.getQuantity() + purchaseItem.getQuantity());
             productService.updateProduct(productInStock);
         }
     }
 
     @Override
     public Invoice getModel() {
         return invoice;
     }
 
     @Override
     public void setSession(Map session1) {
         this.session = session1;
     }
 
     public Invoice getInvoice() {
         return invoice;
     }
 
     public void setInvoice(Invoice invoice) {
         this.invoice = invoice;
     }
 
     public String getiId() {
         return iId;
     }
 
     public void setiId(String iId) {
         this.iId = iId;
     }
 
     public String getSellerName() {
         return sellerName;
     }
 
     public void setSellerName(String sellerName) {
         this.sellerName = sellerName;
     }
 
     public List<PurchaseItem> getPurchaseItemList() {
         return purchaseItemList;
     }
 
     public void setPurchaseItemList(List<PurchaseItem> purchaseItemList) {
         this.purchaseItemList = purchaseItemList;
     }
 
     public Branch getBranch() {
         return branch;
     }
 
     public void setBranch(Branch branch) {
         this.branch = branch;
     }
 }
