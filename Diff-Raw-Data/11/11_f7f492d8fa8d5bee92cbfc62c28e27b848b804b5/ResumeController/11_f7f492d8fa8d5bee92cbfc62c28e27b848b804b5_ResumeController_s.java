 package org.synyx.skills.web;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.joda.time.DateMidnight;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 
 import org.springframework.stereotype.Controller;
 
 import org.springframework.ui.Model;
 
 import org.springframework.util.FileCopyUtils;
 import org.springframework.util.StringUtils;
 
 import org.springframework.validation.DataBinder;
 import org.springframework.validation.Errors;
 
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
 import static org.springframework.web.bind.annotation.RequestMethod.GET;
 import static org.springframework.web.bind.annotation.RequestMethod.POST;
 import static org.springframework.web.bind.annotation.RequestMethod.PUT;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.bind.support.SessionStatus;
 import org.springframework.web.context.request.WebRequest;
 import org.springframework.web.multipart.MultipartFile;
 
 import org.synyx.hades.domain.Page;
 import org.synyx.hades.domain.Pageable;
 
 import org.synyx.minos.core.Core;
 import org.synyx.minos.core.domain.Image;
 import org.synyx.minos.core.domain.User;
 import org.synyx.minos.core.web.CurrentUser;
 import org.synyx.minos.core.web.DateTimeEditor;
 import org.synyx.minos.core.web.Message;
 import org.synyx.minos.core.web.PageWrapper;
 import org.synyx.minos.core.web.UrlUtils;
 import org.synyx.minos.core.web.validation.MultipartFileValidator;
 import org.synyx.skills.domain.MatrixTemplate;
 import org.synyx.skills.domain.Resume;
 import org.synyx.skills.domain.SkillMatrix;
 import org.synyx.skills.domain.resume.ResumeAttributeFilter;
 import org.synyx.skills.domain.resume.ResumeFilter;
 import org.synyx.skills.service.DocbookCreationException;
 import org.synyx.skills.service.PdfDocbookCreator;
 import org.synyx.skills.service.ResumeAdminstration;
 import org.synyx.skills.service.ResumeManagement;
 import org.synyx.skills.service.ResumeZipCreator;
 import org.synyx.skills.service.SkillManagement;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import javax.annotation.security.RolesAllowed;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import org.springframework.validation.BeanPropertyBindingResult;
 import org.synyx.minos.umt.service.UserManagement;
 import org.synyx.skills.SkillzPermissions;
 import org.synyx.skills.service.SkillsAuthenticationServiceWrapper;
 
 
 /**
  * Controller for web actions against {@link ResumeManagement}.
  *
  * @author Oliver Gierke - gierke@synyx.de
  * @author Markus Knittig - knittig@synyx.de
  * @author Stefan Kuhn - kuhn@synyx.de
  */
 @Controller
 @SessionAttributes(types = { SkillMatrix.class, Resume.class })
 public class ResumeController {
 
     private static final Log LOG = LogFactory.getLog(ResumeController.class);
 
     private static final int THUMBNAIL_WIDTH = 200;
 
     private SkillsAuthenticationServiceWrapper authenticationService = null;
 
     private SkillManagement skillManagement = null;
     private ResumeManagement resumeManagement = null;
     private ResumeAdminstration resumeAdminstration = null;
 
     @Qualifier("pdfDocbookCreator")
     private PdfDocbookCreator pdfDocbookCreator = null;
     @Qualifier("pdfDocbookCreatorAnonymous")
     private PdfDocbookCreator pdfDocbookCreatorAnonymous = null;
 
     private ResumeZipCreator resumeZipCreator = null;
 
     private MultipartFileValidator multipartValidator = new MultipartFileValidator();
 
     /**
      * Standard constructor just for enabling AOP.
      */
     protected ResumeController() { }
 
     /**
      * Creates a new {@link ResumeController}.
      *
      * @param resumeManagement
      * @param skillManagement
      * @param resumeAdminstration
      * @param pdfDocbookCreator
      * @param resumeZipCreator
      */
     @Autowired
     public ResumeController(SkillsAuthenticationServiceWrapper authenticationService, ResumeManagement resumeManagement,
             SkillManagement skillManagement, ResumeAdminstration resumeAdminstration, PdfDocbookCreator pdfDocbookCreator,
             ResumeZipCreator resumeZipCreator, PdfDocbookCreator pdfDocbookCreatorAnonymous) {
 
         this.authenticationService = authenticationService;
         this.skillManagement = skillManagement;
         this.resumeManagement = resumeManagement;
         this.resumeAdminstration = resumeAdminstration;
         this.pdfDocbookCreator = pdfDocbookCreator;
         this.resumeZipCreator = resumeZipCreator;
         this.pdfDocbookCreatorAnonymous = pdfDocbookCreatorAnonymous;
     }
 
     /**
      * Configure a custom {@link MultipartFileValidator}. By default the controller will use a
      * {@link MultipartFileValidator}.
      *
      * @param multipartValidator the multipartValidator to set
      */
     public void setMultipartValidator(MultipartFileValidator multipartValidator) {
 
         this.multipartValidator = multipartValidator;
     }
 
 
     @InitBinder
     public void initBinder(DataBinder binder, Locale locale) {
 
         DateTimeEditor editor = new DateTimeEditor(locale, "dd.MM.yyyy");
         editor.withAdditionalParsersFor("MM/dd/yyyy", "dd.MM.yyyy");
         editor.forDateMidnight();
 
         binder.registerCustomEditor(DateMidnight.class, editor);
     }
 
 
     @RequestMapping(value = "/skillz/resume/matrix/form", method = GET)
     public String matrix(Model model, @CurrentUser User user) {
 
         SkillMatrix matrix = resumeManagement.getResume(user).getSkillz();
 
         model.addAttribute("matrix", matrix);
         model.addAttribute("map", matrix.getMap());
         model.addAttribute("entry", matrix.getEntries().get(0));
         model.addAttribute("levels", skillManagement.getLevels());
 
         return "skillz/matrix";
     }
 
 
     @RequestMapping(value = "/skillz/resume/matrix/{id}", method = PUT)
     public String saveExistingMatrix(@ModelAttribute("matrix") SkillMatrix matrix, SessionStatus session) {
 
         resumeManagement.save(matrix);
         session.setComplete();
 
         return UrlUtils.redirect("/skillz/resume#tabs-3");
     }
 
 
     /**
      * Show a {@link User}'s {@link Resume}. Only admin may show the resume of
      * another user.
      *
      * @param username
      * @param model
      * @return
      */
     @RolesAllowed({SkillzPermissions.SKILLZ_USER, SkillzPermissions.SKILLZ_ADMINISTRATION})
     @RequestMapping(value = "/skillz/user/{username:[a-zA-Z_]\\w*}/resume", method = GET)
     public String resume(@PathVariable("username") String username, Model model) {
 
         User user = authenticationService.getUserIfCurrentUserOrAdmin(username);
 
         Resume resume = resumeManagement.getResume(user);
 
         model.addAttribute("owner", user);
         model.addAttribute("filters", resumeManagement.getResumeAttributeFilters());
         model.addAttribute("resume", resume);
         model.addAttribute("levels", skillManagement.getLevels());
 
         return "skillz/resume";        
     }
 
 
     /**
      * Creates a {@link User}'s {@link Resume} as a PDF file in a temporary directory and streams it
      * to the response.
      *
      * Only admin may generate PDFs of another user's resume.
      *
      * @param username
      * @param response
      * @param session
      * @param outputStream
      * @param webRequest
      */
     @RolesAllowed({SkillzPermissions.SKILLZ_USER, SkillzPermissions.SKILLZ_ADMINISTRATION})
     @RequestMapping(value = "/skillz/user/{username:[a-zA-Z_]\\w*}/resume", method = GET, params = "pdf")
     public void resumePdf(@PathVariable("username") String username, HttpServletResponse response, HttpSession session,
             OutputStream outputStream, WebRequest webRequest) {
 
         User user = authenticationService.getUserIfCurrentUserOrAdmin(username);
 
         // generate resume pdf file
         File file = null;
         try {
             file = pdfDocbookCreator.createTempPdfFile(getServletTempDirectory(session.getServletContext()),
                     resumeManagement.getFilteredResume(user, getResumeAttributeFilters(webRequest)),
                     skillManagement.getLevels());
         } catch (DocbookCreationException ex) {
 
             throw new RuntimeException("Failed to create a pdf.", ex);
         }
 
         // stream file to response
         response.setContentType("application/pdf");
         response.setHeader("Content-disposition", "attachment; filename=resume.pdf");
 
         try {
             streamFile(session.getServletContext(), file.getName(), outputStream);
         } catch (IOException ex) {
 
             throw new RuntimeException("Failed stream pdf file to response.", ex);
         }
     }
 
 
     /**
      * Creates a {@link User}'s {@link Resume} as a PDF file (anonymized form) in a temporary directory
      * and streams it to the response.
      *
      * Only admin may generate PDFs of another user's resume.
      *
      * @param username
      * @param response
      * @param session
      * @param outputStream
      * @param webRequest
      * @return
      */
     @RolesAllowed({SkillzPermissions.SKILLZ_USER, SkillzPermissions.SKILLZ_ADMINISTRATION})
     @RequestMapping(value = "/skillz/user/{username:[a-zA-Z_]\\w*}/resume", method = GET, params = "pdfanonymous")
     public void resumePdfAnonymous(@PathVariable("username") String username, HttpServletResponse response,
             HttpSession session, OutputStream outputStream, WebRequest webRequest) {
 
         User user = authenticationService.getUserIfCurrentUserOrAdmin(username);
 
         // generate resume pdf file
         File file = null;
         try {
             file = pdfDocbookCreatorAnonymous.createTempPdfFile(getServletTempDirectory(session.getServletContext()),
                     resumeManagement.getFilteredResume(user, getResumeAttributeFilters(webRequest)),
                     skillManagement.getLevels());
         } catch (DocbookCreationException ex) {
 
             throw new RuntimeException("Failed to create a pdf.", ex);
         }
 
         // stream file to response
         response.setContentType("application/pdf");
         response.setHeader("Content-disposition", "attachment; filename=resume.pdf");
 
         try {
             streamFile(session.getServletContext(), file.getName(), outputStream);
         } catch (IOException ex) {
 
             throw new RuntimeException("Failed stream pdf file to response.", ex);
         }
     }
 
 
     /**
      * Creates the current {@link User}'s {@link Resume} as as a ZIP file in a temporary directory
      * and streams it to the response.
      *
      * Only admin may generate ZIPs of another user's resume.
      *
      * @param username
      * @param response
      * @param session
      * @param outputStream
      * @param webRequest
      * @return
      */
     @RolesAllowed({SkillzPermissions.SKILLZ_USER, SkillzPermissions.SKILLZ_ADMINISTRATION})
     @RequestMapping(value = "/skillz/user/{username:[a-zA-Z_]\\w*}/resume", method = GET, params = "zip")
     public void resumeZip(@PathVariable("username") String username, HttpServletResponse response,
             HttpSession session, OutputStream outputStream, WebRequest webRequest) {
 
         User user = authenticationService.getUserIfCurrentUserOrAdmin(username);
 
         // generate resume pdf file
         File file = null;
         try {
             file = resumeZipCreator.createTempZipFile(getServletTempDirectory(session.getServletContext()),
                     resumeManagement.getFilteredResume(user, getResumeAttributeFilters(webRequest)),
                     skillManagement.getLevels());
         } catch (DocbookCreationException ex) {
 
             throw new RuntimeException("Failed to create a zip.", ex);
         }
 
         // stream file to response
         response.setContentType("application/zip");
         response.setHeader("Content-disposition", "attachment; filename=resume.zip");
 
         try {
             streamFile(session.getServletContext(), file.getName(), outputStream);
         } catch (IOException ex) {
 
             throw new RuntimeException("Failed stream zip file to response.", ex);
         }
     }
 
 
     /**
      * Streams a {@link File} from the temporary directory of the servlet container and deletes it afterwards.
      *
      * @param servletContext
      * @param filename
      * @param outputStream
      * @throws IOException
      */
     private void streamFile(ServletContext servletContext, String filename, OutputStream outputStream)
         throws IOException {
 
         File file = new File(getServletTempDirectory(servletContext), filename);
         InputStream inputStream = null;
 
         try {
             inputStream = FileUtils.openInputStream(file);
             IOUtils.copy(inputStream, outputStream);
         } finally {
             IOUtils.closeQuietly(inputStream);
             FileUtils.deleteQuietly(file);
         }
     }
 
 
     /**
      * Gets the temporary directory of the servlet container from a {@link ServletContext}.
      *
      * @param servletContext
      * @return
      */
     private File getServletTempDirectory(ServletContext servletContext) {
 
         return (File) servletContext.getAttribute("javax.servlet.context.tempdir");
     }
 
 
     /**
      * Returns all {@link ResumeAttributeFilter}s which are requested in the {@link WebRequest}.
      *
      * @param webRequest
      * @return
      */
     private List<ResumeAttributeFilter> getResumeAttributeFilters(WebRequest webRequest) {
 
         List<ResumeAttributeFilter> filters = new ArrayList<ResumeAttributeFilter>();
 
         for (ResumeAttributeFilter filter : resumeManagement.getResumeAttributeFilters()) {
             String parameterValue = webRequest.getParameter(filter.getMessageKey());
 
             if ("1".equals(parameterValue)) {
                 filters.add(filter);
             }
         }
 
         return filters;
     }
 
 
     // Skillz administration
 
     /**
      * Saves a resume instance. Only admin may save another user's resume.
      *
      * @param resume
      * @param model
      * @param conversation
      * @param username
      * @return
      */
     @RolesAllowed({SkillzPermissions.SKILLZ_USER, SkillzPermissions.SKILLZ_ADMINISTRATION})
     @RequestMapping(value = "/skillz/user/{username:[a-zA-Z_]\\w*}/resume", method = PUT)
     public String saveResume(@ModelAttribute("resume") Resume resume, Model model, SessionStatus conversation,
         @PathVariable("username") String username) {
 
         User user = authenticationService.getUserIfCurrentUserOrAdmin(username);
 
         if (!user.equals(resume.getSubject())) {
 
             throw new IllegalArgumentException("Resume does not belong to chosen user.");
         }
 
         resumeManagement.save(resume);
 
         model.addAttribute(Core.MESSAGE, Message.success("skillz.resume.save.success"));
         conversation.setComplete();
 
         if (resume.getSubject().equals(user)) {
             return UrlUtils.redirect("/skillz/user/" + user.getUsername() + "/resume");
         } else {
             return UrlUtils.redirect("/skillz/resumes");
         }
     }
 
     /**
      * Get a users resume photo. Only admin may get the photo of another user's resume.
      *
      * @param username
      * @param outputStream
      * @throws IOException
      */
     @RolesAllowed({SkillzPermissions.SKILLZ_USER, SkillzPermissions.SKILLZ_ADMINISTRATION})
     @RequestMapping(value = "/skillz/user/{username:[a-zA-Z_]\\w*}/resume/photo", method = GET)
     public void showResumePhoto(@PathVariable("username") String username, OutputStream outputStream) throws IOException {
 
         User user = authenticationService.getUserIfCurrentUserOrAdmin(username);
 
         Resume resume = resumeManagement.getResume(user);
 
         if (resume.getPhoto() != null) {
             FileCopyUtils.copy(resume.getPhoto().getThumbnail(), outputStream);
         }
     }
 
     /**
      * Upload a photo to a user's resume. Only admin may upload a photo to another
      * user's resume.
      *
      * @param username
      * @param errors
      * @param model
      * @param image
      * @return
      * @throws IOException
      */
     // TODO: Use only PUT request. Doesn't work currently with multipart
     // requests. See https://jira.springsource.org/browse/SPR-6594
     @RolesAllowed({SkillzPermissions.SKILLZ_USER, SkillzPermissions.SKILLZ_ADMINISTRATION})
     @RequestMapping(value = "/skillz/user/{username:[a-zA-Z_]\\w*}/resume/photo", method = { POST, PUT })
     public String saveResumePhoto(@PathVariable("username") String username, Model model,
         @RequestParam("photoBinary") MultipartFile image) throws IOException {
 
         User user = authenticationService.getUserIfCurrentUserOrAdmin(username);
 
         Resume resume = resumeManagement.getResume(user);
 
         Errors errors = new BeanPropertyBindingResult(resume, Resume.class.getName());
         multipartValidator.validate(image, errors);
 
         if (errors.hasErrors()) {
             model.addAttribute(Core.MESSAGE, Message.error(errors.getGlobalError().getCode()));
         } else {
             
 
             String fileExtension = StringUtils.getFilenameExtension(image.getOriginalFilename());
             resume.setPhoto(new Image(image.getBytes(), THUMBNAIL_WIDTH, fileExtension));
             resumeManagement.save(resume);
 
             model.addAttribute(Core.MESSAGE, Message.success("skillz.resume.photo.save.success"));
         }
 
         return UrlUtils.redirect("/skillz/user/" + user.getUsername() + "/resume");
     }
 
     /**
      * Delete a user's resume photo. Only admin may delete another user's resume photo.
      * 
      * @param username
      * @param model
      * @return
      */
     @RolesAllowed({SkillzPermissions.SKILLZ_USER, SkillzPermissions.SKILLZ_ADMINISTRATION})
     @RequestMapping(value = "/skillz/user/{username:[a-zA-Z_]\\w*}/resume/photo", method = DELETE)
     public String deleteResumePhoto(@PathVariable("username") String username, Model model) {
 
         User user = authenticationService.getUserIfCurrentUserOrAdmin(username);
 
         Resume resume = resumeManagement.getResume(user);
         resume.setPhoto(null);
         resumeManagement.save(resume);
 
         model.addAttribute(Core.MESSAGE, Message.success("skillz.resume.photo.delete.success"));
 
         return UrlUtils.redirect("/skillz/user/" + user.getUsername() + "/resume");
     }
 
     /**
      * Show a list of all resumes. Only admin may do that.
      *
      * @param model
      * @param pageable
      * @return
      */
     @RolesAllowed({SkillzPermissions.SKILLZ_ADMINISTRATION})
     @RequestMapping(value = "/skillz/resumes", method = GET)
     public String resumes(Model model, Pageable pageable) {
 
         Page<Resume> resumes = resumeAdminstration.getResumes(pageable);
         model.addAttribute("resumes", PageWrapper.wrap(resumes));
         model.addAttribute("templates", skillManagement.getTemplates());
 
         // if no filter is selected, preselect the first one
         if (!model.containsAttribute("resumeFilter") && !resumeAdminstration.getResumeFilters().isEmpty()) {
             ResumeFilter resumeFilter = resumeAdminstration.getResumeFilters().get(0);
             model.addAttribute("resumeFilter", resumeFilter);
         }
 
         model.addAttribute("resumeFilters", resumeAdminstration.getResumeFilters());
 
         return "skillz/resumes";
     }
 
     /**
      * Show a list of all resumes and provide input for filter parameters according to the
      * selected filter. Only admin may do that.
      * 
      * @param model
      * @param pageable
      * @param selectFilter
      * @return
      */
     @RolesAllowed({SkillzPermissions.SKILLZ_ADMINISTRATION})
     @RequestMapping(value = "/skillz/resumes", method = GET, params = "selectFilter")
     public String resumes(Model model, Pageable pageable, @RequestParam String selectFilter) {
 
         ResumeFilter resumeFilter = resumeAdminstration.getResumeFilter(selectFilter);
         model.addAttribute("resumeFilter", resumeFilter);
 
         return resumes(model, pageable);
     }
 
     /**
      * Show a list of all resumes filtered by the named filter, which may be configured by
      * filter specific parameters. Only admin may do that.
      *
      * @param model
      * @param pageable
      * @param filterName
      * @param webRequest
      * @return
      */
     @RolesAllowed({SkillzPermissions.SKILLZ_ADMINISTRATION})
     @RequestMapping(value = "/skillz/resumes", method = GET, params = "filterName")
     public String resumes(Model model, Pageable pageable, @RequestParam String filterName, WebRequest webRequest) {
 
         ResumeFilter resumeFilter = resumeAdminstration.getResumeFilter(filterName);
         Page<Resume> resumes = resumeAdminstration.getResumesByFilter(pageable, resumeFilter,
                 requestParametersAsMap(webRequest));
         model.addAttribute("resumeFilter", resumeFilter);
         model.addAttribute("resumeFilters", resumeAdminstration.getResumeFilters());
         model.addAttribute("resumes", PageWrapper.wrap(resumes));
         model.addAttribute("templates", skillManagement.getTemplates());
 
         return "skillz/resumes";
     }
 
     /**
      * Assigns a list of resumes to the given matrix template. Only admin may do that.
      *
      * @param resumes
      * @param template
      * @return
      */
     @RolesAllowed({SkillzPermissions.SKILLZ_ADMINISTRATION})
     @RequestMapping(value = "/skillz/resumes", method = POST, params = "resumes")
     public String assignResumesToTemplate(@RequestParam("resumes") List<Resume> resumes,
         @RequestParam("template") MatrixTemplate template) {
 
         for (Resume resume : resumes) {
             resumeManagement.save(resume, template);
         }
 
         return UrlUtils.redirect("/skillz/resumes");
     }
 
     /**
      * Fetch all parameters from the given {@link WebRequest} and create a key/value map
      * of them. This is used to fetch all filter specific parameters and provide them to
      * selected filter.
      * 
      * @param webRequest
      * @return
      */
     private Map<String, String[]> requestParametersAsMap(WebRequest webRequest) {
 
         Map<String, String[]> parameters = new HashMap<String, String[]>();
         Iterator<String> iter = webRequest.getParameterNames();
 
         while (iter.hasNext()) {
             String parameterName = iter.next();
             parameters.put(parameterName, webRequest.getParameterValues(parameterName));
         }
 
         return parameters;
     }
  
 }
