 package com.github.urlchopper.web;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import com.github.urlchopper.service.GeneratorService;
 
 /**
  * Controller to handle short url requests.
  * @author Marton_Sereg
  *
  */
 @Controller
 public class IndexController {
 
     private static int id;
 
     @Autowired
     private GeneratorService generatorService;
 
     /**
      * Controller for index page.
      * @return tiles name.
      */
    @RequestMapping("/index")
     public String index() {
         return "index";
     }
 
     /**
      * Method to handle url generation requests.
      * @param url Original Url to be replaced
      * @param model MVC model
      * @return tiles name
      */
     @RequestMapping("/generateUrl")
     public String generate(@RequestParam String url, RedirectAttributes model) {
 
         String shortUrl = generatorService.generate(url);
 
         model.addFlashAttribute("shortUrl", shortUrl);
        return "redirect:/index";
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

        if (shortUrl.equals("resourceNotFound")) {
            retUrl = "redirect:/index";
        } else {

            try {
                //todo
                String url = generatorService.findActiveOriginalUrl(shortUrl);
                retUrl = "redirect:" + url;
            } catch (Exception e) {
                model.addFlashAttribute("errorMsg", "This URL is not valid!");
                retUrl = "redirect:/index";
            }
         }
 
         return retUrl;
     }
 
     private String generate() {
         return "genUrl" + id++;
     }
 }
