 package com.danielvaughan.taxonomy.server.web;
 
 import com.danielvaughan.taxonomy.server.daos.TaxonDao;
 import com.danielvaughan.taxonomy.shared.model.Taxon;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
import java.util.List;

 @Controller
 @RequestMapping(value = "/taxon/**")
 public class TaxonController
 {
   
   @Autowired
   private TaxonDao taxonDao;
   
  @RequestMapping(value = "/id/{taxId}", method = RequestMethod.GET)
  public void getTaxonByTaxId(@PathVariable String taxId, ModelMap modelMap)
   {
     Taxon taxon = taxonDao.getTaxonByTaxId(taxId);
     modelMap.addAttribute("taxon", taxon);
   }
  
  @RequestMapping(value ="/{query}", method = RequestMethod.GET)
  public void getTaxonSuggestions(@PathVariable String query, ModelMap modelMap)
  {
    List<Taxon> taxons = taxonDao.searchTaxons(query, 100);
    modelMap.addAttribute("taxon", taxons);
  }
  
 }
