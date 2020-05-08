 package com.mycompany.rest;
 
 import com.mycompany.dao.CompanyDAO;
import com.mycompany.dao.CompanyNotFoundException;
import com.mycompany.dao.DuplicatedCompanyException;
 import com.mycompany.entity.Company;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.*;
 
 import java.util.Iterator;
 import java.util.List;
 
 @Controller
 public class CompanyController {
 
     @Autowired
     CompanyDAO dao;
 
     @RequestMapping(value = "/company/{name}", method= RequestMethod.GET)
     public @ResponseBody String getCompany(@PathVariable String name) {
         try {
             Company c1 = dao.load(name);
             String s = "<company>" + c1.getName() + "</company>\n";
             // TODO: ensure 200 is returned as HTTP response code
             return s;
         }
         catch (CompanyNotFoundException e) {
             // TODO: ensure 404 is returned as HTTP response code
             return "company not found\n";
         }
     }
 
     @RequestMapping(value = "/company", method= RequestMethod.GET)
     public @ResponseBody String getAllCompanies() {
         String s = "<companies>\n";
         List<Company> l;
         // TODO unhardwire (they should be parameters in the URL, e.g /company?max=100,offset=20
         l = dao.findAll(100,0 );
 
         for (Iterator<Company> i = l.iterator(); i.hasNext(); ) {
            Company c = i.next();
            s += "   <company>" + c.getName() + "</company>\n";
         }
         s += "</companies>\n";
         // TODO: ensure 200 is returned as HTTP response code
         return s;
     }
 
     @RequestMapping(value = "/company", method= RequestMethod.POST)
     public @ResponseBody String createCompany(@RequestParam("name") String name) {
         try {
             dao.create(name);
             // TODO: ensure 200 is returned as HTTP response code
             return "ok\n";
         }
         catch (Exception e) {
             // TODO: ensure 400 is returned as HTTP response code
             return "duplicated company name\n";
         }
     }
 
     @RequestMapping(value = "/company/{name}", method= RequestMethod.DELETE)
     public @ResponseBody String deleteCompany(@PathVariable String name) {
         try {
             dao.delete(name);
             // TODO: ensure 200 is returned as HTTP response code
             return "ok\n";
         }
         catch (CompanyNotFoundException e) {
             // TODO: ensure 404 is returned as HTTP response code
             return "company not found\n";
         }
     }
 
 }
