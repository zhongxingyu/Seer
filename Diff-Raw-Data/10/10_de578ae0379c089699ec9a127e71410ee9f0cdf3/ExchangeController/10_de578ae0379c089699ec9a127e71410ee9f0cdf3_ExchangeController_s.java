 package com.telstra.webtest.controller;
 
 import com.telstra.webtest.domain.Currency;
 import com.telstra.webtest.form.ExchangeForm;
 import com.telstra.webtest.form.ExchangeFormBuilder;
 import com.telstra.webtest.services.ExchangeService;
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 
 @Controller
 @RequestMapping("/exchange")
 public class ExchangeController {
 
     @Autowired
     private ExchangeService exchangeService;
 
     @RequestMapping("/")
     public ModelAndView index() {
         return new ModelAndView("exchange/index");
     }
 
     @RequestMapping(value = "/", method = RequestMethod.POST)
     public ModelAndView exchange(@ModelAttribute("exchange") ExchangeForm exchangeForm, BindingResult result) {
         if (result.hasErrors()) {
             return new ModelAndView("exchange/index", "error", "Please check your amount");
         }
         if (exchangeForm.getFromAmount() < 0) {
             return new ModelAndView("exchange/index", "error", "Please check your amount");
         }
 
         double toAmount = exchangeService.exchange(exchangeForm.getFromCurrency(), exchangeForm.getToCurrency(), exchangeForm.getFromAmount());
         exchangeForm.setToAmount(toAmount);
 
        return new ModelAndView("redirect:exchange/result");
     }
 
     @ModelAttribute("exchange")
     public ExchangeForm setUpForm(ExchangeForm exchangeForm, HttpServletRequest request) {
         if (!StringUtils.isEmpty(request.getParameter("fromCurrency"))) {
             exchangeForm.setFromCurrency(request.getParameter("fromCurrency"));
         }
         if (!StringUtils.isEmpty(request.getParameter("toCurrency"))) {
             exchangeForm.setToCurrency(request.getParameter("toCurrency"));
         }
         return exchangeForm;
     }
 
     @ModelAttribute("currencies")
     public Currency[] setUpCurrencies() {
         return Currency.values();
     }
 }
