 package com.cqlybest.www.controller;
 
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import com.cqlybest.common.bean.ProductGroup;
 import com.cqlybest.common.bean.ProductGroupFilterItem;
 import com.cqlybest.common.bean.ProductGroupItem;
 import com.cqlybest.common.bean.template1.Template1Menu;
 import com.cqlybest.common.service.ProductService;
 import com.cqlybest.common.service.SiteService;
 import com.cqlybest.common.service.Template1Service;
 import com.cqlybest.common.service.TemplateService;
 
 @Controller
 public class Template1Controller {
 
   @Autowired
   private Template1Service template1Service;
   @Autowired
   private TemplateService templateService;
   @Autowired
   private SiteService siteService;
   @Autowired
   private ProductService productService;
 
   @RequestMapping("/template1/index.html")
   public void index(Model model) {
     setCommonData(model);
   }
 
   @RequestMapping("/template1/register.html")
   public void register(Model model) {
     setCommonData(model);
   }
 
   @RequestMapping("/template1/login.html")
   public void login(Model model) {
     setCommonData(model);
   }
 
   @RequestMapping("/template1/pg/{id}.html")
   public Object pg(@PathVariable String id, Model model) {
     Template1Menu menu = template1Service.get(id);
     if (menu == null || menu.getMenuType() != 0) {
       // 菜单不存在或者不是产品聚合菜单
       return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.NOT_FOUND);
     }
 
     ProductGroup group = menu.getProductGroup();
     Set<ProductGroupItem> groupItems = group.getGroupItems();
     Set<ProductGroupFilterItem> filterItems = group.getFilterItems();
     model.addAttribute("total", productService.queryProductsTotal(groupItems, filterItems));
     model.addAttribute("products", productService.queryProducts(groupItems, filterItems, 0, 10));
     model.addAttribute("menu", menu);
     setCommonData(model);
     return "/template1/product_group";
   }
 
   @RequestMapping("/template1/page/{id}.html")
   public Object page(@PathVariable String id, Model model) {
     Template1Menu menu = template1Service.get(id);
     if (menu == null || menu.getMenuType() != 1) {
       // 菜单不存在或者不是产品聚合菜单
       return new ResponseEntity<String>(StringUtils.EMPTY, HttpStatus.NOT_FOUND);
     }
 
     model.addAttribute("page", menu);
     setCommonData(model);
     return "/template1/page";
   }
 
   public void setCommonData(Model model) {
     model.addAttribute("Site", siteService.getSite());
    model.addAttribute("Template1Menu", template1Service.getPublishedMenus());
   }
 
 }
