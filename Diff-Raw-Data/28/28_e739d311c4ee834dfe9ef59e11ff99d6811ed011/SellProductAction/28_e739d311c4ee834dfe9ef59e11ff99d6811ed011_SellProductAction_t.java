 package com.ai.action;
 
 import com.ai.model.Branch;
 import com.ai.model.Invoice;
 import com.ai.model.Product;
 import com.ai.model.PurchaseItem;
 import com.ai.service.*;
 import com.ai.service.impl.*;
 import com.ai.util.ActiveUser;
 import com.ai.util.Permission;
 import com.ai.util.Return;
import com.ai.util.StaticLists;
 import com.ai.validator.Validator;
 import com.ai.validator.impl.ValidatorImpl;
 import com.opensymphony.xwork2.ActionContext;
 import com.opensymphony.xwork2.ActionSupport;
 import com.opensymphony.xwork2.ModelDriven;
 import org.apache.struts2.interceptor.SessionAware;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import java.math.BigDecimal;
 import java.util.*;
 
 
 public class SellProductAction extends ActionSupport implements SessionAware {
     private List<Product> products = new ArrayList<Product>();
     private transient List<PurchaseItem> purchaseItems = new ArrayList<PurchaseItem>();
     private String pName;
     private String pCategory;
     private String pId;
     private String index;
     private String invoiceId;
     private int quantity = 0;
     private String quantity_1 = "";
     private Date date;
     private BigDecimal payedAmount = new BigDecimal(0.0);
     private BigDecimal discount = new BigDecimal(0.0);
     private BigDecimal totalAmount = new BigDecimal(0.0);
     private BigDecimal balance = new BigDecimal(0.0);
    public Map<String, String> categoryList = new StaticLists().getCategoryList();
 
     private Map session = ActionContext.getContext().getSession();
     private ActiveUser activeUser;
     private Logger logger = LoggerFactory.getLogger(AddProductAction.class);
     ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:spring.xml");
     ProductService productService = (ProductServiceImpl) applicationContext.getBean("productService");
     PermissionService permissionService = (PermissionServiceImpl) applicationContext.getBean("permissionService");
     InvoiceService invoiceService = (InvoiceServiceImpl) applicationContext.getBean("invoiceService");
     PurchaseItemService purchaseItemService = (PurchaseItemServiceImpl) applicationContext.getBean("purchaseItemService");
     Validator validator = (ValidatorImpl) applicationContext.getBean("validator");
     BranchService branchService = (BranchServiceImpl) applicationContext.getBean("branchService");
 
 
     private void init() {
         if (session.get("purchaseItem") != null) {
             purchaseItems = (List<PurchaseItem>) session.get("purchaseItem");
         }
     }
 
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
 
     //action
     public String searchProduct() {
         logger.info("Search by name:[{}] category:[{}]", pName, pCategory);
         activeUser = (ActiveUser) session.get("activeUser");
         try {
             Return r = validator.execute(activeUser, permissionService.findPermissionById(Permission.SELL_PRODUCT.getPermission()), hasActionErrors(), hasFieldErrors());
             if (r == Return.VALIDATOR_OK) {
                 products = productService.searchProduct(pName, pCategory, getBranchList(activeUser.getTenant()));
                 init();
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
 
     //action
     public String addToCart() {
         logger.info("Add to cart pId:[{}]..quantity:[{}]", pId, quantity_1);
         activeUser = (ActiveUser) session.get("activeUser");
         Product p = new Product();
         try {
             Return r = validator.execute(activeUser, permissionService.findPermissionById(Permission.SELL_PRODUCT.getPermission()), hasActionErrors(), hasFieldErrors());
             if (r == Return.VALIDATOR_OK) {
                 init();
                 if (quantity_1 != null && !quantity_1.trim().equals("") && isANumber(pId)) {
                     quantity = Integer.parseInt(quantity_1.trim());
                     p = productService.findProductById(Long.valueOf(pId));
                 }
                 if (p.getQuantity() > quantity) {
                     purchaseItems.add(new PurchaseItem(0l, p, quantity, null));
                     session.put("purchaseItem", purchaseItems);
                 } else {
                     addActionError("Required quantity not available");
                 }
                 return Return.SUCCESS.getReturnCode();
             } else {
                 return handelValidator(r);
             }
         } catch (Exception e) {
             e.printStackTrace();
             return Return.INTERNAL_ERROR.getReturnCode();
         }
     }
 
     //action
     public String removeFromList() {
         logger.info("Remove from cart index:[{}]", index);
         activeUser = (ActiveUser) session.get("activeUser");
         try {
             Return r = validator.execute(activeUser, permissionService.findPermissionById(Permission.SELL_PRODUCT.getPermission()), hasActionErrors(), hasFieldErrors());
             if (r == Return.VALIDATOR_OK) {
                 init();
                 int i = Integer.parseInt(index);
                 purchaseItems.remove(Integer.parseInt(index));
                 session.put("purchaseItem", purchaseItems);
                 return Return.SUCCESS.getReturnCode();
             } else {
                 return handelValidator(r);
             }
         } catch (Exception e) {
             e.printStackTrace();
             return Return.INTERNAL_ERROR.getReturnCode();
         }
     }
 
     //action
     public String execute() {
         try {
             session.remove("purchaseItem");
             activeUser = (ActiveUser) session.get("activeUser");
 
             Return r = validator.execute(activeUser, permissionService.findPermissionById(Permission.SELL_PRODUCT.getPermission()), hasActionErrors(), hasFieldErrors());
             if (r == Return.VALIDATOR_OK) {
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
 
     //action
     public String sellProduct() {
         try {
             activeUser = (ActiveUser) session.get("activeUser");
             init();
             calculateBalance();
 
             Return r = validator.execute(activeUser, permissionService.findPermissionById(Permission.SELL_PRODUCT.getPermission()), hasActionErrors(), hasFieldErrors());
             if (r == Return.VALIDATOR_OK) {
                 if (balance.doubleValue() < 0) {
                     addActionError("Invalid request.Balance can not less than 0.Please check the payed amount");
                     return Return.INVALID_REQUEST.getReturnCode();
                 } else {
                     date = new Date(System.currentTimeMillis());
                     Invoice invoice = saveInDB(purchaseItems, date, payedAmount.doubleValue(), totalAmount.doubleValue(),
                             discount.doubleValue(), balance.doubleValue(), activeUser.getTenant(), activeUser.getUserId());
                     removeSoledProductsFromStock(purchaseItems);
                     addActionMessage("Successfully sold the product");
                     invoiceId = invoice.getId().toString();
                     return Return.PARENT.getReturnCode();
                 }
             } else {
                 return handelValidator(r);
             }
         } catch (Exception e) {
             e.printStackTrace();
             addActionError("Internal error.Try again later");
             return Return.INTERNAL_ERROR.getReturnCode();
         }
     }
     
     
     private void removeSoledProductsFromStock(List<PurchaseItem> purchaseItemList){
         for (PurchaseItem purchaseItem : purchaseItemList) {
             Product p = productService.findProductById(purchaseItem.getProduct().getProductId());
             p.setQuantity( p.getQuantity() - purchaseItem.getQuantity() );
             productService.updateProduct(p);
         }
     }
 
     private Invoice saveInDB(List<PurchaseItem> pi, Date date1, double payed, double total, double dis, double bal, String tenant, Long empId) {
         Invoice invoice = new Invoice(null, date1, tenant, empId, payed, total, dis, bal, new HashSet<PurchaseItem>());
         invoice = (Invoice) invoiceService.saveInvoice(invoice);
 
         for (PurchaseItem purchaseItem : pi) {
             purchaseItem.setInvoice(invoice);
             purchaseItem = (PurchaseItem) purchaseItemService.savePurchaseItem(purchaseItem);
         }
         return invoice;
     }
 
     //action
     public String calculateBalance() {
         activeUser = (ActiveUser) session.get("activeUser");
         try {
             Return r = validator.execute(activeUser, permissionService.findPermissionById(Permission.SELL_PRODUCT.getPermission()), hasActionErrors(), hasFieldErrors());
             if (r == Return.VALIDATOR_OK) {
                 init();
                 double d;
                 for (PurchaseItem purchaseItem : purchaseItems) {
                     d = totalAmount.doubleValue() + purchaseItem.getProduct().getMarketPrice() * purchaseItem.getQuantity();
                     setTotalAmount(new BigDecimal(d));
                 }
 
                 d = payedAmount.doubleValue() - totalAmount.doubleValue() + discount.doubleValue();
                 setBalance(new BigDecimal(d));
                 setDate(null);
                 return Return.SUCCESS.getReturnCode();
             } else {
                 return handelValidator(r);
             }
         } catch (Exception e) {
             e.printStackTrace();
             addActionError("Internal error.Try again later");
             return Return.INTERNAL_ERROR.getReturnCode();
         }
     }
 
     @Override
     public void setSession(Map session1) {
         this.session = session1;
     }
 
     public List<Product> getProducts() {
         return products;
     }
 
     public void setProducts(List<Product> products) {
         this.products = products;
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
 
     public List<PurchaseItem> getPurchaseItems() {
         return purchaseItems;
     }
 
     public void setPurchaseItems(List<PurchaseItem> purchaseItems) {
         this.purchaseItems = purchaseItems;
     }
 
     public void setpCategory(String pCategory) {
         this.pCategory = pCategory;
     }
 
     public String getpId() {
         return pId;
     }
 
     public void setpId(String pId) {
         this.pId = pId;
     }
 
     public int getQuantity() {
         return quantity;
     }
 
     public void setQuantity(int quantity) {
         this.quantity = quantity;
     }
 
     public String getIndex() {
         return index;
     }
 
     public void setIndex(String index) {
         this.index = index;
     }
 
     public String getQuantity_1() {
         return quantity_1;
     }
 
     public void setQuantity_1(String quantity_1) {
         this.quantity_1 = quantity_1;
     }
 
     public Date getDate() {
         return date;
     }
 
     public void setDate(Date date) {
         this.date = date;
     }
 
     public BigDecimal getPayedAmount() {
         return payedAmount;
     }
 
     public void setPayedAmount(BigDecimal payedAmount) {
         this.payedAmount = payedAmount;
     }
 
     public BigDecimal getDiscount() {
         return discount;
     }
 
     public void setDiscount(BigDecimal discount) {
         this.discount = discount;
     }
 
     public BigDecimal getTotalAmount() {
         return totalAmount;
     }
 
     public void setTotalAmount(BigDecimal totalAmount) {
         this.totalAmount = totalAmount;
     }
 
     public BigDecimal getBalance() {
         return balance;
     }
 
     public void setBalance(BigDecimal balance) {
         this.balance = balance;
     }
 
     public String getInvoiceId() {
         return invoiceId;
     }
 
     public void setInvoiceId(String invoiceId) {
         this.invoiceId = invoiceId;
     }

    public Map<String, String> getCategoryList() {
        return categoryList;
    }

    public void setCategoryList(Map<String, String> categoryList) {
        this.categoryList = categoryList;
    }
 }
