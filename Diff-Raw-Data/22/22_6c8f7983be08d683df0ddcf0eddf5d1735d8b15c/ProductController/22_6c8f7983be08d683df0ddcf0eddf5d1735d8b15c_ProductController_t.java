 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.posabro.ocsys.web.controller;
 
 import com.posabro.ocsys.domain.CustomerPK;
 import com.posabro.ocsys.domain.Facility;
 import com.posabro.ocsys.domain.Product;
 import com.posabro.ocsys.domain.ProductType;
 import com.posabro.ocsys.services.ProductService;
 import com.posabro.ocsys.web.UserInfoProvider;
 import com.posabro.web.controller.ValidationController;
 import java.util.List;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 /**
  *
  * @author Carlos Juarez
  */
 @Controller
 @RequestMapping("/productController/*")
 public class ProductController extends ValidationController{
     
     @Autowired
     private ProductService productService;
     
     @RequestMapping(value="findProductById", method = RequestMethod.POST)
    public @ResponseBody Product findProductById(@RequestBody String id){
        try{
            ProductType productType = ProductType.valueOf(id);
            return productService.findProduct(productType);
        }catch(IllegalArgumentException ex){
            return null;
        }
     }
     
 }
