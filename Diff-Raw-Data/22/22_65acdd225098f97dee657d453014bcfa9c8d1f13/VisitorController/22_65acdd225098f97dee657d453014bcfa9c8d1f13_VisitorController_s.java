 package org.catamarancode.faq.web;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.StringWriter;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.solr.client.solrj.response.QueryResponse;
 import org.catamarancode.faq.entity.Audit;
 import org.catamarancode.faq.entity.Comment;
 import org.catamarancode.faq.entity.Faq;
 import org.catamarancode.faq.entity.NestedTag;
 import org.catamarancode.faq.service.ApplicationConfiguration;
 import org.catamarancode.faq.service.MessageContext;
 import org.catamarancode.faq.service.SolrService;
 import org.catamarancode.faq.service.UserContext;
 import org.catamarancode.faq.util.CatamaranMarkdown;
 import org.catamarancode.faq.util.FaqUtils;
 import org.catamarancode.faq.web.support.NestedTagNode;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Controller;
 import org.springframework.util.StringUtils;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.servlet.ModelAndView;
 
 
 @Controller
 @Scope("request")
 public class VisitorController {
 
     private Logger logger = LoggerFactory.getLogger(VisitorController.class);
 
     @Autowired
     private SolrService solrService;
     
     @Autowired
 	private MessageContext messageContext;    
 
     @Autowired
 	private ApplicationConfiguration applicationConfiguration;    
 
     @Autowired
 	private UserContext userContext;        
 
     @RequestMapping("/")
     public ModelAndView home(HttpServletRequest request,
             HttpServletResponse response) throws Exception {
         return index(request, response);
     }
 
     @RequestMapping("/faqs")
     public ModelAndView faqs(HttpServletRequest request,
             HttpServletResponse response) throws Exception {
 
         // Get parameters
         String query = request.getParameter("query");
         
         // A topic (tag) search?
         String tagFilter = null;
         if (StringUtils.hasText(query) && query.toLowerCase().indexOf("topic:") > -1) {
         	tagFilter = query.toLowerCase().replace("topic:", "").trim();
         	if (tagFilter.indexOf(":") > -1) {
         		tagFilter = tagFilter.replace(":", "|");
         	}
         }
         
         // Logged in user?  If so show non-public FAQs
         String contextId = userContext.getEffectiveContextId(request);
         
         // Find faqs filtered by parameters
         List<Faq> faqs = null;
         NestedTag tagParam = null;
         if (StringUtils.hasText(tagFilter)) {
         	tagParam = NestedTag.createFromPipeSeparatedString(tagFilter);
             faqs = new ArrayList<Faq>(solrService.findByTag(tagParam, true, contextId));
         } else if (StringUtils.hasText(query)) {
         	
         	// Strip out characters we don't want in query
         	String filteredQuery = query.replace(":", " ");
             QueryResponse queryResponse = solrService.search(filteredQuery + " AND document-type:FAQ", contextId);
             faqs = solrService.extractFaqs(queryResponse);    
         } else {
             
             // Default to showing all faqs (TODO: paginate in the future)
             faqs = solrService.listFaqs(contextId);    
         }
         
         ModelAndView mv = new ModelAndView();
         userContext.prepareModel(mv.getModel());
         messageContext.addPendingToModel(mv.getModel());
         
         // Group faqs by category
         // NOTE: We limit ourselves to 4 levels deep (to avoid headache of recursion)
         NestedTagNode top = new NestedTagNode(null);
         for (Faq faq : faqs) {
         	for (int i = 0; i < faq.getNestedTags().length; i++) {
 	            NestedTag tag = faq.getNestedTags()[i];
 	            NestedTagNode currentNode = null;
 	            if (tag != null) {
 	            	
 	            	// If tag parameter is specified, skip nodes that don't match
 	            	if (tagParam != null) {
 	            		if (!tag.match(tagParam, true)) {
 	            			continue;
 	            		}
 	            	}
 	            	
 	                for (int j = 0; j < tag.getElements().size(); j++) {
 	                    if (j == 0) {
 	                        currentNode = top;                    
 	                    }
 	                    currentNode = currentNode.getOrCreateChild(tag.getElements().get(j));                
 	                }
 	                
 	                // Add the FAQ itself
 	                currentNode.addFaq(faq);
 	            }
         	}
         }
         
         // Create keyword list from tags and categories
         Set<String> keywords = FaqUtils.extractKeywords(faqs);
         mv.addObject("keywords", keywords);
 
         mv.addObject("top", top);
         return mv;
     }
   
     @RequestMapping("/faq")
     public ModelAndView faq(HttpServletRequest request,
             HttpServletResponse response) throws Exception {
 
         ModelAndView mv = new ModelAndView();
         messageContext.addPendingToModel(mv.getModel());
         userContext.prepareModel(mv.getModel());
         String contextId = userContext.getEffectiveContextId(request);
         
         // One specific faq?
         String faqId = request.getParameter("key");        
         Faq faq = solrService.loadFaq(faqId);
         if (faq == null) {
        	messageContext.setMessage("Invalid FAQ key", false);            
            return mv;
         }
         
         // Audits and comments
         List<Audit> audits = solrService.listAudits(faq);
         mv.addObject("audits",  audits);
         List<Comment> comments = solrService.listComments(faq);
         mv.addObject("comments", comments);
         
         // Create keyword list from tags and categories
         List<Faq> faqs = new ArrayList<Faq>();
         faqs.add(faq);
         Set<String> keywords = FaqUtils.extractKeywords(faqs);
         mv.addObject("keywords", keywords);
         
         // Nothing
         mv.addObject("faq", faq);
         return mv;
     }
     
     @RequestMapping("/index")
     public ModelAndView index(HttpServletRequest request,
             HttpServletResponse response) throws Exception {
 
         ModelAndView mv = new ModelAndView("index");
         userContext.prepareModel(mv.getModel());
         return mv;
     }
 
     @RequestMapping("/tutorial")
     public ModelAndView tutorial(HttpServletRequest request,
             HttpServletResponse response) throws Exception {
 
         ModelAndView mv = new ModelAndView();
         userContext.prepareModel(mv.getModel());
         
         // We maintain page contents as markdown for easy editing/formatting
         
         // Check for external tutorial file override
         BufferedReader b = null;
         if (applicationConfiguration != null && applicationConfiguration.getTutorialFileOverridePath() != null && applicationConfiguration.getTutorialFileOverridePath().length() > 2) {
         	
         	// Load tutorial file
         	URL loadedResource = this.getClass().getClassLoader().getResource("tutorial.md");
             File file = new File(applicationConfiguration.getTutorialFileOverridePath());
             b = new BufferedReader(new FileReader(file));
         } else {
         	
         	// Load from classpath by default
         	URL loadedResource = this.getClass().getClassLoader().getResource("tutorial.md");
             b = new BufferedReader(new FileReader(loadedResource.getFile()));	
         }
         
     	StringWriter w = new StringWriter();
     	String line = null;
 		boolean indent = false;
 		while ((line = b.readLine()) != null) {
 			w.write(line);
 			w.write("\r\n");
 		}
     	
     	String s = w.toString();    	
     	CatamaranMarkdown m = new CatamaranMarkdown(); 
     	mv.addObject("htmlContent", m.markdown(s));
         
         return mv;
     }
 
     @RequestMapping("/reference")
     public ModelAndView reference(HttpServletRequest request,
             HttpServletResponse response) throws Exception {
 
         ModelAndView mv = new ModelAndView();
         userContext.prepareModel(mv.getModel());
         return mv;
     }
 
     @RequestMapping("/source")
     public ModelAndView source(HttpServletRequest request,
             HttpServletResponse response) throws Exception {
 
         ModelAndView mv = new ModelAndView();
         userContext.prepareModel(mv.getModel());
         return mv;
     }
 
     @RequestMapping("/download")
     public ModelAndView download(HttpServletRequest request,
             HttpServletResponse response) throws Exception {
 
         ModelAndView mv = new ModelAndView();
         userContext.prepareModel(mv.getModel());
         return mv;
     }
 
     @RequestMapping("/questions")
     public ModelAndView questions(HttpServletRequest request,
             HttpServletResponse response) throws Exception {
 
         ModelAndView mv = new ModelAndView();
         userContext.prepareModel(mv.getModel());
         return mv;
     }
 
 }
