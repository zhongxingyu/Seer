 package ar.com.jmfsg.documentation.controller;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.RedirectView;
 
 import ar.com.jmfsg.documentation.DocumentationLoader;
 import ar.com.jmfsg.documentation.support.Utils;
 
 
 /**
  * Controller mapping /docs/**
  *  
  * @author jformoso
  */
 
 @Controller
 public class DocumentationController
     implements InitializingBean {
 
     private Map<String, Object> documentationByGroup = new HashMap<String, Object>();
     private Map<String, Object> documentationByMethod = new HashMap<String, Object>();
 
     private ModelAndView createView(String name) {
         return new ModelAndView(name);
     }
 
    @RequestMapping(value = {"/", "/docs"}, method = RequestMethod.GET)
     public ModelAndView getIndex() {
         ModelAndView modelAndView = this.createView("index");
         modelAndView.addObject("controllers", this.documentationByGroup);
         modelAndView.addObject("general", this.getDocumentationLoader().getGeneralDoc());
         modelAndView.addObject("tags", this.documentationLoader.getTags());
         return modelAndView;
     }
 
     @RequestMapping(value = "/docs/raw", method = RequestMethod.GET)
     public ModelAndView getRaw() {
         ModelAndView modelAndView = this.createView("rawJsonView");
         modelAndView.addObject("data", this.getDocumentationLoader().getRawDoc());
         return modelAndView;
     }
 
     @RequestMapping(value = "/favicon.ico", method = RequestMethod.GET)
     public ModelAndView getFavicon() {
         return new ModelAndView(new RedirectView("/static/img/favicon.gif"));
     }
 
     @RequestMapping(value = "/docs/method/{method}", method = RequestMethod.GET)
     public ModelAndView getDetail(@PathVariable String method) {
         ModelAndView modelAndView = this.createView("detail");
         modelAndView.addObject("m", this.documentationByMethod.get(method));
         modelAndView.addObject("general", this.getDocumentationLoader().getGeneralDoc());
         modelAndView.addObject("dictionary", this.getDocumentationLoader().getDictionary());
         modelAndView.addObject("tags", this.documentationLoader.getTags());
         return modelAndView;
     }
 
     @RequestMapping(value = "/docs/page/{name}", method = RequestMethod.GET)
     public ModelAndView getPage(@PathVariable String name) {
         ModelAndView modelAndView = this.createView(name);
         modelAndView.addObject("general", this.getDocumentationLoader().getGeneralDoc());
         return modelAndView;
     }
 
     @RequestMapping(value = "/docs/jsonResult/", method = RequestMethod.GET)
     public String jsonResult(Model model) {
     	model.addAttribute("status" , "loading response...");
     	return "rawJsonView";
     }
     
     private DocumentationLoader documentationLoader;
 
     @Override
     public void afterPropertiesSet() throws Exception {
         Utils.normalizeDocumentationData(this.getDocumentationLoader().getDocumentation(), this.getDocumentationLoader()
             .getGroupDocs(), this.documentationByGroup, this.documentationByMethod);
     }
 
     public DocumentationLoader getDocumentationLoader() {
         return this.documentationLoader;
     }
 
     public void setDocumentationLoader(DocumentationLoader documentationLoader) {
         this.documentationLoader = documentationLoader;
     }
 }
