 package kabbadi.controller;
 
 import kabbadi.domain.Invoice;
 import kabbadi.domain.Money;
 import kabbadi.service.InvoiceService;
 import kabbadi.spring.util.MoneyPropertyEditor;
 import kabbadi.spring.util.NullSafeDatePropertyEditor;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.RedirectView;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 @Controller
 @RequestMapping(value = "/invoice")
 public class InvoiceController {
 
     private final InvoiceService invoiceService;
 
     @Autowired
     public InvoiceController(InvoiceService invoiceService) {
         this.invoiceService = invoiceService;
     }
 
     @InitBinder
     protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) {
         binder.registerCustomEditor(Date.class, new NullSafeDatePropertyEditor());
         binder.registerCustomEditor(Money.class, new MoneyPropertyEditor());
     }
 
     @RequestMapping(value = "/save", method = RequestMethod.POST)
     public ModelAndView add(@ModelAttribute Invoice invoice,
                            @RequestParam(defaultValue = "admin") String role) {
         if (invoice.valid()) {
             invoiceService.saveOrUpdate(invoice);
             return new ModelAndView(new RedirectView("/invoice/list#" + role, true));
         }
         return new ModelAndView(new RedirectView("/invoice/create#" + role, true));
     }
 
     @RequestMapping(value = "/create", method = RequestMethod.GET)
     public ModelAndView create() {
         return new ModelAndView("invoice/edit", "invoice", new Invoice());
     }
 
     @RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
     public ModelAndView edit(@PathVariable("id") Integer id) {
         return new ModelAndView("invoice/edit", "invoice", invoiceService.get(id));
     }
 
     @RequestMapping(value = "/list", method = RequestMethod.GET)
     public ModelAndView list() {
         ModelAndView modelAndView = new ModelAndView("invoice/list");
         List<Invoice> invoices = invoiceService.list();
         Collections.sort(invoices);
         modelAndView.addObject("invoices", invoices);
         return modelAndView;
     }
 
     @RequestMapping(value = "/{id}", method = RequestMethod.GET)
     public ModelAndView viewDetails(@PathVariable("id") Integer id) {
         ModelAndView modelAndView = new ModelAndView("invoice/view");
         modelAndView.addObject("invoice", invoiceService.get(id));
         return modelAndView;
     }
 
 }
