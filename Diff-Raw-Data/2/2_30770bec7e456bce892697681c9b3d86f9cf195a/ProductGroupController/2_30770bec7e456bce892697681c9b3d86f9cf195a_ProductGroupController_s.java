 package com.cqlybest.admin.controller;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.UUID;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.cqlybest.common.bean.DepartureCity;
 import com.cqlybest.common.bean.DictProductGrade;
 import com.cqlybest.common.bean.DictProductType;
 import com.cqlybest.common.bean.DictTraffic;
 import com.cqlybest.common.bean.Keyword;
 import com.cqlybest.common.bean.ProductGroup;
 import com.cqlybest.common.bean.ProductGroupFilterItem;
 import com.cqlybest.common.bean.ProductGroupItem;
 import com.cqlybest.common.service.DestinationService;
 import com.cqlybest.common.service.DictService;
 import com.cqlybest.common.service.ProductGroupService;
 
 @Controller
 public class ProductGroupController {
 
   @Autowired
   private ProductGroupService productGroupService;
 
   @Autowired
   private DictService dictService;
 
   @Autowired
   private DestinationService destinationService;
 
   @RequestMapping(value = "/product_group/list.html", method = RequestMethod.GET)
   public void list(Model model) {
     model.addAttribute("groups", productGroupService.getAllProductGroup());
   }
 
   @RequestMapping(value = "/product_group/add.html", method = RequestMethod.GET)
   public void add(Model model) {
     model.addAttribute("traffics", dictService.getDict(DictTraffic.class));
     model.addAttribute("types", dictService.getDict(DictProductType.class));
     model.addAttribute("grades", dictService.getDict(DictProductGrade.class));
   }
 
   @RequestMapping(value = "/product_group/modify.html", method = RequestMethod.GET)
   public void modify(@RequestParam String id, Model model) {
     model.addAttribute("group", productGroupService.getProductGroup(id));
 
     model.addAttribute("traffics", dictService.getDict(DictTraffic.class));
     model.addAttribute("types", dictService.getDict(DictProductType.class));
     model.addAttribute("grades", dictService.getDict(DictProductGrade.class));
     model.addAttribute("keywords", dictService.getDict(Keyword.class));
     model.addAttribute("departureCities", dictService.getDict(DepartureCity.class));
     model.addAttribute("destinations", destinationService.getTree());
   }
 
   @RequestMapping(value = {"/product_group/add.html", "/product_group/modify.html"}, method = RequestMethod.POST)
   @ResponseBody
   public void edit(@RequestParam(required = false) String id, @RequestParam String name,
       @RequestParam(required = false) List<Integer> groupTypes,
       @RequestParam(required = false) List<String> groupValues,
       @RequestParam(required = false) List<Integer> filterTypes,
       @RequestParam(required = false) List<String> filterValues) {
     ProductGroup group = new ProductGroup();
     group.setId(id == null ? UUID.randomUUID().toString() : id);
     group.setName(name);
 
     Set<ProductGroupItem> groupItems = new HashSet<>();
     if (groupTypes != null) {
       for (int i = 0; i < groupTypes.size(); i++) {
         ProductGroupItem item = new ProductGroupItem();
         item.setGroupType(groupTypes.get(i));
         item.setGroupValue(groupValues.get(i));
         groupItems.add(item);
       }
     }
     group.setGroupItems(groupItems);
 
     Set<ProductGroupFilterItem> filterItems = new HashSet<>();
    if (groupTypes != null) {
       for (int i = 0; i < filterTypes.size(); i++) {
         ProductGroupFilterItem item = new ProductGroupFilterItem();
         item.setFilterType(filterTypes.get(i));
         item.setFilterValue(filterValues.get(i));
         filterItems.add(item);
       }
     }
     group.setFilterItems(filterItems);
 
     group.setPublished(false);
     productGroupService.edit(group);
   }
 
   @RequestMapping("/product_group/delete.html")
   @ResponseBody
   public void delete(@RequestParam String id) {
     productGroupService.delete(id);
   }
 
   @RequestMapping("/product_group/toggle.html")
   @ResponseBody
   public void toggle(@RequestParam String id, @RequestParam boolean published) {
     productGroupService.togglePublished(id, published);
   }
 
 }
