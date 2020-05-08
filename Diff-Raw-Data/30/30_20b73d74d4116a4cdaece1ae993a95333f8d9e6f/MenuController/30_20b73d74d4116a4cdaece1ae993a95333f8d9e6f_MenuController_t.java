 package com.exadel.borsch.web.controllers;
 
 import com.exadel.borsch.entity.Course;
 import com.exadel.borsch.entity.Dish;
 import com.exadel.borsch.entity.PriceList;
 import com.exadel.borsch.managers.ManagerFactory;
 import com.exadel.borsch.managers.PriceManager;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 
 import com.exadel.borsch.util.DateTimeUtils;
import org.joda.time.format.DateTimeFormat;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.annotation.Secured;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import org.joda.time.DateTime;
 
 /**
  * @author Vlad
  */
 @Controller
 public class MenuController {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(MenuController.class);
     @Autowired
     private ManagerFactory managerFactory;
 
     @Secured("ROLE_EDIT_MENU_SELF")
     @RequestMapping("/menu")
     public ModelAndView processPageRequest() {
         ModelAndView mav = new ModelAndView(ViewURLs.MENU_PAGE);
         PriceManager manager = managerFactory.getPriceManager();
         List<PriceList> prices = manager.getAllPriceLists();
         if ((prices != null) && (!prices.isEmpty())) {
             PriceList dishes = prices.get(prices.size() - 1);
             Map<Integer, Collection<Dish>> map = dishes.getCourses().asMap();
             map = this.setMap(map);
             mav.addObject("courseList", map);
         }
         return mav;
     }
 
    @Secured("ROLE_EDIT_MENU_SELF")
    @RequestMapping("/menu/{date}")
    public ModelAndView processPageRequestWithDate(@PathVariable String date) {
        ModelAndView mav = new ModelAndView(ViewURLs.MENU_PAGE);
        PriceManager manager = managerFactory.getPriceManager();
        DateTime orderDate = DateTime.parse(date, DateTimeFormat.forPattern("dd-MM-yyyy"));
        PriceList price= manager.getPriceListByCreationTime(orderDate);
        if (price != null) {
            Map<Integer, Collection<Dish>> map = price.getCourses().asMap();
            map = this.setMap(map);
            mav.addObject("courseList", map);
        }
        return mav;
    }

     private Map<Integer, Collection<Dish>> setMap(Map<Integer, Collection<Dish>> map) {
         Map<Integer, Collection<Dish>> temp = new TreeMap<>(map);
         for (int i = 0; i < Course.COUNT_COURSE; i++) {
             if (!temp.containsKey(i)) {
                 temp.put(i, new ArrayList<Dish>());
             }
         }
         return Collections.unmodifiableMap(temp);
     }
 
     @Secured("ROLE_EDIT_PRICE")
     @RequestMapping("/edit/dish/add/{course}")
     public String processAddPageRequest(@PathVariable String course, ModelMap model) {
         model.addAttribute("course", course);
         model.addAttribute("action", "add");
         return ViewURLs.DISH_ADD_PAGE;
     }
     @Secured("ROLE_EDIT_PRICE")
     @RequestMapping("/menu/newCurrentList")
     public ModelAndView processNewListRequest() {
         PriceManager manager = managerFactory.getPriceManager();
         DateTime startOfNextWeek = DateTimeUtils.getStartOfNextWeek();
         PriceList list = new PriceList();
         PriceList priceList = manager.getCurrentPriceList();
         if (DateTimeUtils.sameDates(priceList.getCreationTime(), startOfNextWeek)) {
             return this.processPageRequest();
         }
         list.setCreationTime(startOfNextWeek);
         list.setExpirationTime(startOfNextWeek.plusWeeks(1));
         manager.addPriceList(list);
         return this.processPageRequest();
     }
     @ResponseBody
     @Secured("ROLE_EDIT_PRICE")
     @RequestMapping(value = "/edit/dish/add/save", method = RequestMethod.POST)
     public DishJSON processSaveDishRequest(HttpServletRequest request) {
         String name = request.getParameter("name");
         int price = Integer.parseInt(request.getParameter("price"));
         String description = request.getParameter("description");
         String course = request.getParameter("course");
         Dish dish = new Dish(name, price, Course.getCourse(course), description);
         PriceManager manager = managerFactory.getPriceManager();
         List<PriceList> prices = manager.getAllPriceLists();
         PriceList dishes = null;
         if ((prices != null) && (!prices.isEmpty())) {
             dishes = prices.get(prices.size() - 1);
         } else {
             dishes = new PriceList();
         }
 
         manager.addDishToPriceList(dish, dishes);
         return DishJSON.mapDishToJSON(dish);
     }
 
     @Secured("ROLE_EDIT_PRICE")
     @RequestMapping("/edit/dish/{id}/edit")
     public String processEditPageRequest(@PathVariable Long id, ModelMap model) {
         PriceManager manager = managerFactory.getPriceManager();
         List<PriceList> prices = manager.getAllPriceLists();
         if ((prices != null) && (!prices.isEmpty())) {
             PriceList dishes = prices.get(prices.size() - 1);
             Dish dish = dishes.getDishById(id);
             model.addAttribute("dish", dish);
         }
         model.addAttribute("action", "edit");
         return ViewURLs.DISH_ADD_PAGE;
     }
 
     @ResponseBody
     @Secured("ROLE_EDIT_PRICE")
     @RequestMapping(value = "/edit/dish/edit/save", method = RequestMethod.POST)
     public DishJSON processUpdateDishRequest(HttpServletRequest request) {
         String name = request.getParameter("name");
         String price = request.getParameter("price");
         String description = request.getParameter("description");
         Long id = Long.parseLong(request.getParameter("id"));
         PriceManager manager = managerFactory.getPriceManager();
         List<PriceList> prices = manager.getAllPriceLists();
         Dish dish = new Dish();
         if ((prices != null) && (!prices.isEmpty())) {
             PriceList dishes = prices.get(prices.size() - 1);
             dish = dishes.getDishById(id);
             dish.setName(name);
             dish.setPrice(Integer.parseInt(price));
             dish.setDescription(description);
             manager.updateDishInPriceList(dish, dishes);
         }
         return DishJSON.mapDishToJSON(dish);
     }
 
     @Secured("ROLE_EDIT_PRICE")
     @RequestMapping("/edit/dish/{id}/remove")
     public String processRemoveDishRequest(@PathVariable Long id) {
         PriceManager manager = managerFactory.getPriceManager();
         List<PriceList> prices = manager.getAllPriceLists();
         if ((prices != null) && (!prices.isEmpty())) {
             PriceList dishes = prices.get(prices.size() - 1);
             Dish forRemove = dishes.getDishById(id);
             manager.removeDishFromPriceList(forRemove, dishes);
         }
         return ViewURLs.MENU_PAGE;
     }
 
     public static class DishJSON {
 
         private String id;
         private String name;
         private Integer price;
         private String description;
         private String index;
 
         public String getId() {
             return id;
         }
 
         public void setId(String id) {
             this.id = id;
         }
 
         public String getName() {
             return name;
         }
 
         public void setName(String name) {
             this.name = name;
         }
 
         public Integer getPrice() {
             return price;
         }
 
         public void setPrice(Integer price) {
             this.price = price;
         }
 
         public String getDescription() {
             return description;
         }
 
         public void setDescription(String description) {
             this.description = description;
         }
 
         public String getIndex() {
             return index;
         }
 
         public void setIndex(String index) {
             this.index = index;
         }
 
         public static DishJSON mapDishToJSON(Dish dish) {
             DishJSON json = new DishJSON();
 
             json.setName(dish.getName());
             json.setId(dish.getId().toString());
             json.setDescription(dish.getDescription());
             json.setPrice(dish.getPrice());
             json.setIndex(String.valueOf(Course.getIndex(dish.getCourse())));
             return json;
         }
     }
 }
