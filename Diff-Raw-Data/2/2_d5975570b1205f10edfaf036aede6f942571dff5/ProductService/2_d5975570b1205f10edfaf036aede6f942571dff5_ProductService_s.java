 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mne.advertmanager.service;
 
import com.mne.advertmanager.api.Product;
 import com.mne.advertmanager.dao.GenericDao;
 
 
 
 /**
  *
  * @author Nina Eidelshtein and Misha Lebedev
  */
 public class ProductService {
     
     private GenericDao<Product,Long> productDao;
 
     public void setProductDao(GenericDao<Product,Long> productDao) {
         this.productDao = productDao;
     }
     
 }
