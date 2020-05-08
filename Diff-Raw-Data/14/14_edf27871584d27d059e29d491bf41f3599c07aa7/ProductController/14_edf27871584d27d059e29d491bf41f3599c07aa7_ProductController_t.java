 package com.controller;
 
 import com.domain.customer.UserRepository;
 import com.domain.shop.Product;
 import com.domain.shop.ProductRepository;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.data.domain.Sort;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.*;
 
 import javax.inject.Inject;
 import javax.validation.Valid;
 import java.util.Map;
 
 /**
  * @author Stanislav Kurilin
  */
 @Controller
 @RequestMapping("/products")
 public class ProductController {
 
     private final ProductRepository repository;
     private final UserRepository userRepository;
 
     private final Logger logger = LoggerFactory.getLogger(ProductController.class);
 
     private final String DEFAULT_PAGE_SIZE = "4";
     private final String DEFAULT_SORT_BY = "title";
     private final String DEFAULT_DIRECTION = "ASC";
 
     @Inject
     public ProductController(ProductRepository repository, UserRepository userRepository) {
         this.repository = repository;
         this.userRepository = userRepository;
     }
 
     @RequestMapping(method = RequestMethod.GET)
     public String showAll(@RequestParam(value = "page", defaultValue = "0") Integer page,
                           @RequestParam(value = "pageSize", defaultValue = DEFAULT_PAGE_SIZE) Integer size,
                           @RequestParam(value = "sortBy", defaultValue = DEFAULT_SORT_BY) String sortBy,
                           @RequestParam(value = "direction", defaultValue = DEFAULT_DIRECTION) Sort.Direction direction,
                           Map<String, Object> model) {
         Sort.Order order = new Sort.Order(direction,sortBy);
         model.put("page", repository.findAll(new PageRequest(page, size, new Sort(order))));
         return "products";
     }
 
     @RequestMapping(value = "/new", method = RequestMethod.GET)
     public String createPage(Map<String, Object> model) {
         model.put("product", new Product());
         return "productNew";
     }
 
     @RequestMapping(value = "/{title}", method = RequestMethod.GET)
     public String showPage(@PathVariable("title") String titleOfProduct, Map<String, Object> model) {
         model.put("product", repository.findByTitle(titleOfProduct));
         return "productView";
     }
 
     @RequestMapping(value = "/new", method = RequestMethod.POST)
     public String create(@ModelAttribute @Valid Product product, BindingResult bindingResult) {
         product.setSeller(userRepository.findOne(1l));// get user from id
         if (bindingResult.hasErrors()) {
             return "productNew";
         }
         final Product entity = repository.save(product);
         return "redirect:/products/" + entity.getTitle();
     }
 
 
     @RequestMapping(value = "/isTitleAvailable", method = RequestMethod.GET)
     @ResponseBody
     public String isTitleAvailable(@RequestParam String title) {
         Product product = repository.findByTitle(title);
         if (product == null) {
             return Boolean.TRUE.toString();
         } else {
             return Boolean.FALSE.toString();
         }
     }
 }
