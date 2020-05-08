 package net.sibcolombia.portal.web.controller;
 
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.context.MessageSource;
 import org.springframework.web.servlet.ModelAndView;
 
 
 public abstract class AlphabetBrowserController extends org.gbif.portal.web.controller.AlphabetBrowserController {
 
   protected MessageSource messageSource;
 
   /**
    * The method to override. Implementations will perform the search by calling a Service Layer method
    * and then add the content to the model.
    * 
    * @param searchChar the search character
    * @param mav the model and view to populate
    * @param request the http request
    * @param response the http response
    */
   @Override
   public abstract ModelAndView alphabetSearch(char searchChar, ModelAndView mav, HttpServletRequest request,
     HttpServletResponse response);
 
   /**
    * @see org.gbif.portal.web.controller.RestController#handleRequest(java.util.Map,
    *      javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
   @Override
   public ModelAndView handleRequest(Map<String, String> properties, HttpServletRequest request,
     HttpServletResponse response) throws Exception {
     ModelAndView mav = new ModelAndView(modelViewName);
     List<Character> alphabet = retrieveAlphabet(request, response);
     alphabet = sortAlphabetForDisplay(alphabet);
     mav.addObject(alphabetModelKey, alphabet);
     String searchString = properties.get(searchStringRequestKey);
     char searchChar = defaultSearchChar;
    if (StringUtils.isNotEmpty(searchString) && !searchString.contentEquals("conjuntos") && !searchString.contentEquals("departamentos")) {
       if (logger.isDebugEnabled())
         logger.debug("Search string before trim: " + searchString);
       searchString = searchString.trim();
 
       if (searchString.length() > 1) {
         // try parsing the number
         try {
           searchChar = (char) Integer.parseInt(searchString);
         } catch (NumberFormatException e) {
           // expected behaviour
         }
       } else {
         if (logger.isDebugEnabled())
           logger.debug("Search string after decoding: " + searchString);
         searchChar = searchString.charAt(0);
       }
       if (logger.isDebugEnabled())
         logger.debug("Char to search with: " + searchChar + ", unicode value:" + searchChar);
     } else if (alphabet != null && !alphabet.isEmpty()) {
       searchChar = '0';
     }
     mav.addObject("messageSource", messageSource);
     mav.addObject(selectedCharModelKey, searchChar);
     return alphabetSearch(searchChar, mav, request, response);
   }
 
   /**
    * Retrieve an alphabet for this set of entities.
    * 
    * @param mav
    * @param request
    * @param response
    */
   @Override
   public abstract List<Character> retrieveAlphabet(HttpServletRequest request, HttpServletResponse response);
 
   /**
    * @param messageSource the messageSource to set
    */
   public void setMessageSource(MessageSource messageSource) {
     this.messageSource = messageSource;
   }
 }
