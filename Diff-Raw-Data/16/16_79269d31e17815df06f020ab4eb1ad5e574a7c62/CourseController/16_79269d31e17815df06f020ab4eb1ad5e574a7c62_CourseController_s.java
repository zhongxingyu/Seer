 package nl.codebasesoftware.produx.controller;
 
 import nl.codebasesoftware.produx.domain.dto.entity.CourseEntityDTO;
 import nl.codebasesoftware.produx.domain.dto.entity.CourseRequestEntityDTO;
 import nl.codebasesoftware.produx.domain.optionlists.NumberOfParticipants;
 import nl.codebasesoftware.produx.domain.optionlists.Prefixes;
 import nl.codebasesoftware.produx.exception.ProduxServiceException;
 import nl.codebasesoftware.produx.exception.ResourceNotFoundException;
 import nl.codebasesoftware.produx.formdata.CourseRequestFormData;
 import nl.codebasesoftware.produx.net.mail.CourseRequestMailer;
 import nl.codebasesoftware.produx.search.criteria.SearchCriteria;
 import nl.codebasesoftware.produx.search.criteria.filter.Filter;
 import nl.codebasesoftware.produx.search.criteria.filter.NormalFilter;
 import nl.codebasesoftware.produx.search.result.SearchResult;
 import nl.codebasesoftware.produx.service.CourseRequestService;
 import nl.codebasesoftware.produx.service.CourseService;
 import nl.codebasesoftware.produx.service.PageBlockService;
 import nl.codebasesoftware.produx.service.SearchService;
 import nl.codebasesoftware.produx.service.business.url.CourseUrl;
 import nl.codebasesoftware.produx.validator.CourseRequestValidator;
 import org.apache.log4j.Logger;
 import org.apache.log4j.spi.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 import org.springframework.web.servlet.support.RequestContextUtils;
 
 import javax.mail.MessagingException;
 import javax.servlet.http.HttpServletRequest;
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Locale;
 import java.util.Map;
 
 /**
  * User: rvanloen
  * Date: 20-9-12
  * Time: 1:25
  */
 @Controller
 public class CourseController {
 
     private CourseService courseService;
     private CourseRequestValidator courseRequestValidator;
     private CourseRequestService courseRequestService;
     private SearchService searchService;
     private CourseRequestMailer courseRequestMailer;
 
     private static final Logger LOG = Logger.getLogger(CourseController.class);
 
     @Autowired
     public CourseController(CourseService courseService, CourseRequestValidator courseRequestValidator,
                             CourseRequestService courseRequestService, SearchService searchService,
                            CourseRequestMailer courseRequestMailer) {
         this.courseService = courseService;
         this.courseRequestValidator = courseRequestValidator;
         this.courseRequestService = courseRequestService;
         this.searchService = searchService;
         this.courseRequestMailer = courseRequestMailer;
     }
 
     @RequestMapping(value = "/{category}/{id:[\\d]+}/{title}", method = RequestMethod.GET)
     public String get(@PathVariable("id") Long id,
                       @PathVariable("category") String category,
                       @PathVariable("title") String title,
                       Model model) throws ProduxServiceException, ArithmeticException, IOException {
         CourseRequestFormData courseRequestFormData = new CourseRequestFormData();
         courseRequestFormData.setCourseId(id);
 
         CourseEntityDTO course = courseService.findFull(id);
         validateUrl(category, id, title, course);
 
         setData(model, courseRequestFormData, course, false);
         return "main";
     }
 
 
     @RequestMapping(value = "/{category}/{id:[\\d]+}/{title}", method = RequestMethod.POST)
     public String submitRequest(@ModelAttribute("courseRequest") CourseRequestFormData request,
                                 @PathVariable("category") String category,
                                 @PathVariable("title") String title,
                                 @PathVariable("id") Long id,
                                 BindingResult result, Model model, Locale locale,
                                 RedirectAttributes redirectAttrs)
                                 throws ProduxServiceException, MessagingException {
 
         courseRequestValidator.validate(request, result);
 
         if (!result.hasErrors()) {
             CourseRequestEntityDTO requestEntityDTO = courseRequestService.saveRequest(request);
             courseRequestMailer.sendCourseRequestMail(requestEntityDTO, locale);
             redirectAttrs.addFlashAttribute("formData", request);
             model.addAttribute("courseRequestSubmitSuccess", true);
             return String.format("redirect:/%s/%d/success", requestEntityDTO.getCourse().getCategory().getUrlTitle(),
                     requestEntityDTO.getCourse().getId());
         }
 
         CourseEntityDTO course = courseService.findFull(id);
 
         validateUrl(category, id, title, course);
         setData(model, request, course, false);
 
         model.addAttribute("includeCourseJs", true);
         model.addAttribute("scrolldown", true);
 
         return "main";
     }
 
     @RequestMapping(value = "/{category}/{id:[\\d]+}/success", method = RequestMethod.GET)
     public String requestSubmitSuccess(Model model) throws ProduxServiceException {
         CourseRequestFormData formData = (CourseRequestFormData) model.asMap().get("formData");
         if(formData == null){
             // The user pressed F5 or opened the page directly. Read here:
             // http://www.tikalk.com/java/redirectattributes-new-feature-spring-mvc-31
             return "redirect:/";
         }
 
         CourseEntityDTO course = courseService.findFull(formData.getCourseId());
         setData(model, formData, course, true);
         return "main";
     }
 
 
     private void validateUrl(String category, long id, String title, CourseEntityDTO course){
         if(!CourseUrl.createUrl(id, category, title).equals(course.getUrl())){
             throw new ResourceNotFoundException();
         }
     }
 
     private void setData(Model model, CourseRequestFormData formData, CourseEntityDTO course, boolean isSuccessView) throws ProduxServiceException {
 
         SearchResult otherCourses = searchService.findOtherCourses(course);
 
         model.addAttribute("rightColumn", "content/coursedetails");
         model.addAttribute("title", course.getName() + "- ICT Cursus");
         model.addAttribute("course", course);
         model.addAttribute("mainContent", "content/course");
         model.addAttribute("courseRequest", formData);
         model.addAttribute("prefixes", Arrays.asList(Prefixes.values()));
         model.addAttribute("numberOfParticipants", NumberOfParticipants.NUMBERS);
         model.addAttribute("isSuccessView", isSuccessView);
         model.addAttribute("isCourse", true);
         model.addAttribute("otherCourses", otherCourses);
         model.addAttribute("hasOtherCourses", otherCourses.getCourses().size() > 0);
         model.addAttribute("category", course.getCategory());
     }
 
 
 
 
 }
