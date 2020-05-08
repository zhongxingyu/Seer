 package us.digitalasylum.controller;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import org.apache.log4j.Logger;
 import org.springframework.web.servlet.ModelAndView;
 import us.digitalasylum.repository.CategoryRepository;
 import us.digitalasylum.repository.FeedRepository;
 import us.digitalasylum.repository.ItemRepository;
 import us.digitalasylum.repository.entities.Category;
import us.digitalasylum.repository.entities.Channel;
 import us.digitalasylum.repository.entities.Feed;
 import us.digitalasylum.repository.entities.Item;
 
import java.util.ArrayList;
import java.util.List;

 
 @Controller
 @RequestMapping("/")
 public class IndexController {
     private static final Logger logger = Logger.getLogger(IndexController.class);
 
     @Autowired
     private CategoryRepository categoryRepository;
 
     @Autowired
     private ItemRepository itemRepository;
 
     @RequestMapping("")
     public ModelAndView index()
     {
         logger.debug("entering index.");
 
         ModelAndView mav = new ModelAndView("index");
         Iterable<Category> categories = categoryRepository.findAll();
 
         mav.addObject("categories", categories);
 
         return mav;
     }
 
     @RequestMapping("category/{categoryId}")
     public ModelAndView index(@PathVariable("categoryId") Long categoryId )
     {
         ModelAndView mav = new ModelAndView("public.item");
         Iterable<Category> categories = categoryRepository.findAll();
 
         mav.addObject("categories", categories);
 
        Iterable<Item> itemList = itemRepository.findByChannel_Feed_Category_Id(categoryId);

         mav.addObject("items", itemList);
 
         return mav;
     }
 }
