 package com.epam.urlchopper.web;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import com.epam.urlchopper.service.CookieService;
 import com.epam.urlchopper.service.GeneratorService;
 
 /**
  * Controller to handle short url requests.
  * @author Marton_Sereg
  *
  */
 @Controller
 public class IndexController {
 
     private static final String TOP_SECRET_URL = "http://www.youtube.com/watch?v=SLmmfYd8dos";
     private static final String TOP_SECRET = "pinapinapunci";
     private static final Logger LOG = LoggerFactory.getLogger(IndexController.class);
 
     @Autowired
     private GeneratorService generatorService;
 
     @Autowired
     private CookieService cookieService;
 
     /**
      * Controller for index page.
      * @return tiles name.
      */
     @RequestMapping("/")
     public String index(HttpServletRequest request, HttpServletResponse response) {
         return "index";
     }
 
     /**
      * Method to handle url generation requests.
      * @param url Original Url to be replaced
      * @param model MVC model
      * @return tiles name
      */
     @RequestMapping("/generateUrl")
     public String generate(@RequestParam String url, HttpServletRequest request, HttpServletResponse response, RedirectAttributes model) {
 
         LOG.debug("start");
 
         String nUrl = url;
         if (url.equals(TOP_SECRET)) {
             nUrl = TOP_SECRET_URL;
         }
 
         String shortUrl = generatorService.generate(nUrl, cookieService.getUserId(request.getCookies()));
 
         model.addFlashAttribute("shortUrl", shortUrl);
         return "redirect:/";
     }
 
     /**
      * Method to handle redirects from short urls to original urls.
      * @param shortUrl Short url
      * @param model MVC model
      * @return returnUrl
      */
     @RequestMapping("/{shortUrl}")
     public String redirect(@PathVariable String shortUrl, RedirectAttributes model) {
 
         String retUrl = "";
         try {
            String url = generatorService.findOriginalUrlByShortUrl(shortUrl);
             model.addFlashAttribute("url", url);
             retUrl = "redirect:/waitpage";
         } catch (Exception e) {
             model.addFlashAttribute("errorMsg", "This URL is not valid!");
             retUrl = "redirect:/";
         }
 
         return retUrl;
     }
 
     /**
      * Before redirected to the original URL, a wait page is shown for a few seconds.
      * @return view page
      */
     @RequestMapping("/waitpage")
     public String waitpage() {
         return "waitpage";
     }
 }
