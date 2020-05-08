 package it.sevenbits.space.web.controller;
 
import it.sevenbits.space.dao.IEventDao;
 import it.sevenbits.space.dao.ISubscriptionDao;
 import it.sevenbits.space.domain.Subscription;
 import it.sevenbits.space.service.SearchingEvent;
 import it.sevenbits.space.web.form.SearchEventForm;
 import it.sevenbits.space.web.form.SubscriptionForm;
 import it.sevenbits.space.web.validator.SubscriptionValidator;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.validation.Valid;
 
 @Controller
 public class DashboardController {
 
     //public final String IMG_SRC = "/space_adventures/resources/img/";
 
     @Autowired
     private ISubscriptionDao ISubscriptionDao;
 
     @Autowired
     private SearchingEvent searchingEvent;
 
     @Autowired
     private SubscriptionValidator subscriptionValidator;
 
     /**
      * Displays a list of events on the main page.
      * @return index page view.
      */
     @RequestMapping(value = {"/index.html", "/"}, method = RequestMethod.GET)
     public ModelAndView showListEvent(@RequestParam(value = "query", required = false, defaultValue="") String search) {
 
         ModelAndView modelAndView = new ModelAndView("index");
 
         SearchEventForm searchEventForm = new SearchEventForm();
         modelAndView.addObject("searchEventForm", searchEventForm);
 
         modelAndView.addObject("events", searchingEvent.findCustom(search));
 
         return modelAndView;
     }
 
     /**
      * Saves an subscriber email in the database in case of successful validation.
      * In case of an error displays alert form.
      * @param subscriptionForm Object with subscription information.
      * @param result result of email validation.
      * @return null if success, subscription form view else.
     */
     @RequestMapping(value = {"/subsribe.html"}, method = RequestMethod.POST)
     public ModelAndView addSubscription(@Valid final SubscriptionForm subscriptionForm, final BindingResult result) {
         if (result.hasErrors()) {
             ModelAndView modelAndView = new ModelAndView("subscribe-form");
             modelAndView.addObject("subscriptionForm", subscriptionForm);
             return modelAndView;
         }
         Subscription subscription = new Subscription();
         subscription.setEmail(subscriptionForm.getEmail());
         ISubscriptionDao.addSubscription(subscription);
         return null;
     }
 
     /**
      * Displays subscription form.
      * @return subscription form view.
      */
     @RequestMapping(value = "/subscribe-form.html", method = RequestMethod.GET)
     public ModelAndView displaySubscription() {
         SubscriptionForm subscriptionForm = new SubscriptionForm();
         ModelAndView modelAndView = new ModelAndView("subscribe-form");
         modelAndView.addObject("subscriptionForm", subscriptionForm);
         return modelAndView;
     }
 
 /*    @InitBinder
     protected void initBinder(final WebDataBinder binder) {
         binder.setValidator(subscriptionValidator);
     }    */
 }
