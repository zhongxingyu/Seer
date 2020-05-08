 package com.danielvaughan.taxonomy.server.web;
 
 import com.danielvaughan.taxonomy.server.daos.TaxonDao;
 import com.danielvaughan.taxonomy.shared.model.Taxon;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 @RequestMapping(value = "/taxon/**")
 public class TaxonController
 {
   
   @Autowired
   private TaxonDao taxonDao;
   
  @RequestMapping(value = "/{taxId}", method = RequestMethod.GET)
  public void getTaxon(@PathVariable String taxId, ModelMap modelMap)
   {
     Taxon taxon = taxonDao.getTaxonByTaxId(taxId);
     modelMap.addAttribute("taxon", taxon);
   }
 }
