 package de.enwida.web.controller;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.security.Principal;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.annotation.PostConstruct;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.MessageSource;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BeanPropertyBindingResult;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.DataBinder;
 import org.springframework.validation.ObjectError;
 import org.springframework.validation.Validator;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 import de.enwida.web.db.model.UploadedFile;
 import de.enwida.web.db.model.UserLines;
 import de.enwida.web.db.model.UserLinesMetaData;
 import de.enwida.web.model.FileUpload;
 import de.enwida.web.model.User;
 import de.enwida.web.service.implementation.MailServiceImpl;
 import de.enwida.web.service.interfaces.IUserLinesService;
 import de.enwida.web.service.interfaces.IUserService;
 import de.enwida.web.utils.Constants;
 import de.enwida.web.utils.EnwidaUtils;
 import de.enwida.web.utils.LogoFinder;
 import de.enwida.web.validator.FileValidator;
 import de.enwida.web.validator.UserValidator;
 
 /**
  * Handles requests for the user service.
  */
 @Controller
 @RequestMapping("/user")
 public class UserController {
 	
 	@Autowired
 	private IUserService userService;
 
 	@Autowired
 	private IUserLinesService userLineService;
 
 	@Autowired
 	private UserSessionManager userSession;
 	
 	@Autowired
 	private UserValidator userValidator;
 
 	@Autowired
 	private FileValidator fileValidator;
     
 	@Autowired
 	private MessageSource messageSource;
  
 	@Autowired	
 	private MailServiceImpl mail;	
 
     private static org.apache.log4j.Logger logger = Logger.getLogger(AdminController.class);
 
     @Value("#{applicationProperties['fileUploadDirectory']}")
 	private String fileUploadDirectory;
 
 	@Value("#{applicationProperties['file.upload.parse.success']}")
 	private String uploadsuccessmsg;
 	
 	@PostConstruct
 	public void init() {
 		fileUploadDirectory = EnwidaUtils.resolveEnvVars(fileUploadDirectory);
 	}
 
 	@RequestMapping(value="/user", method = RequestMethod.GET)
 	public String displayDashboard(Model model, Locale locale) {
 		try{
 	        User u = userService.fetchUser(new Long(0));
 	        model.addAttribute("user", u);		    
 		}catch(Exception e){
             logger.info(e.getMessage());
             model.addAttribute("Error", messageSource.getMessage("de.enwida.userManagement.notAllowed", null, locale)); 		    
 		}
 		
 		return "user";
 	}
 
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String home(HttpServletRequest request,ModelMap model, Principal principal) {
 		return index(request,model, principal);
 	}
 	
 	   @RequestMapping(value = "/index", method = RequestMethod.GET)
 	    public String index(HttpServletRequest request,ModelMap model, Principal principal) {
 	        String name,userStatus,userStatusURL;
 	        
 	        if(principal!=null){
 	            name = principal.getName();
 	            userStatus=messageSource.getMessage("de.enwida.userManagement.logout", null, request.getLocale());
 	            userStatusURL="../j_spring_security_logout";
 	        }else{
 	            name="anonymous";
 	            userStatusURL="login";
 	            userStatus=messageSource.getMessage("de.enwida.userManagement.login", null, request.getLocale());
 	        }
 	        model.addAttribute("username", name);
 	        model.addAttribute("userStatus", userStatus);
 	        model.addAttribute("userStatusURL", userStatusURL);
 	        model.addAttribute("content", "user/index");
 	        return "master";
 	    }
 	
 	@RequestMapping(value = "/test", method = RequestMethod.GET)
 	public String test(ModelMap model, Principal principal) {
 		String name,userStatus,userStatusURL;
 		
 		if(principal!=null){
 			name = principal.getName();
 			userStatus="logout";
 			userStatusURL="../j_spring_security_logout";
 		}else{
 			name="anonymous";
 			userStatusURL=userStatus="login";
 		}
 		model.addAttribute("username", name);
 		model.addAttribute("userStatus", userStatus);
 		model.addAttribute("userStatusURL", userStatusURL);
 		return "user/test";
 	}
 	
 	@RequestMapping(value="/login", method = RequestMethod.GET)
 	public String login(ModelMap model,HttpServletRequest request,Principal principal) {
 
 	    String referrer = request.getHeader("Referer");
 	    if(referrer!=null){
 	        request.getSession().setAttribute("url_prior_login", referrer);
 	    }
 	    if(principal!=null){
 	        return index(request, model, principal);
 	    }else{
 	        return "user/login";
 	    }
 	}
 	
 	@RequestMapping(value="/logout", method = RequestMethod.GET)
 	public String logout(ModelMap model) {
 		return "logout";
 	}
 	
 	@RequestMapping(value="/loginFailed", method = RequestMethod.GET)
     public String loginFailed(HttpServletRequest request,HttpServletResponse response,ModelMap model,Principal principal) {
         //if user is logged in, dont return him to loginFailed page
 	    if(principal!=null){
             try {
                 response.sendRedirect("index");
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
         model.addAttribute("error", "true");
         return "user/login";
     }
 	
 	@RequestMapping(value="/download", method = RequestMethod.GET)
 	public String download(ModelMap model) {
 		return "user/download";
 	}
 	
 	public void preProcessRegisterForm(HttpServletRequest request,HttpServletResponse response,ModelMap model) throws Exception{
 	    
 	    if(request.getMethod().equalsIgnoreCase("GET")){  
 	        User user=new User();
             model.addAttribute("USER", user);
             model.addAttribute("content", "user/register");
 	    }else{
 	        User user=(User) model.get("USER");
 	        BindingResult result =new BeanPropertyBindingResult(user, "USER");
 	        userValidator.validate(user, result);       
 
             if (!result.hasErrors())
             {
                 if(userService.saveUser(user,"http://localhost:8080/enwida/user/"))
                 {                           
                     String name = user.getFirstName() + " " + user.getLastName();
                     String userStatus="logout";
                     String userStatusURL="../j_spring_security_logout";
     
                     model.addAttribute("username", name);
                     model.addAttribute("userStatus", userStatus);
                     model.addAttribute("userStatusURL", userStatusURL);  
                     model.addAttribute("content","user/index");    
                     //We dont want prelogin
                     //clear the session and security context and redirect user to main page
                     SecurityContextHolder.clearContext();
                     HttpSession session = request.getSession(false);
                     if (session != null) {
                         session.invalidate();
                     }     
                     response.sendRedirect("index");
                 }
             }else{
                 model.addAttribute("content","user/register");
             }
             model.addAllAttributes(result.getModel());
 	    }
 	}
 	
 	@RequestMapping(value="/register",method=RequestMethod.GET)
     public String showForm(ModelMap model, HttpServletRequest request,HttpServletResponse response,Principal principal){
 	    try {
             preProcessRegisterForm(request,response,model);
         } catch (Exception e) {
             logger.info(e.getMessage());
             model.addAttribute("Error", messageSource.getMessage("de.enwida.userManagement.notAllowed", null, request.getLocale()));
         }
 	    if(principal!=null){
             return index(request, model, principal);
         }else{
             return "master";
         }
     }
 	
 	@RequestMapping(value="/register",method=RequestMethod.POST)
 	public String processForm(@ModelAttribute(value="USER") User user, ModelMap model, HttpServletRequest request,HttpServletResponse response)
 	{
 		try {
             preProcessRegisterForm(request,response,model);
         } catch (Exception e) {
             logger.info(e.getMessage());
             model.addAttribute("Error",messageSource.getMessage("de.enwida.userManagement.notAllowed", null, request.getLocale()));
         }
         return "master";
 	}
 	
 	@RequestMapping(value="/checkEmail",method=RequestMethod.GET)
 	public @ResponseBody String checkEmail(HttpServletRequest request,ModelMap model,String email){
 		
 		boolean availabilityCheck = false;
         try {
             availabilityCheck = userService.userNameAvailability(email);
         } catch (Exception e) {
             logger.info(e.getMessage());
             model.addAttribute("Error", messageSource.getMessage("de.enwida.userManagement.notAllowed", null, request.getLocale()));
         }
 		
 		if(availabilityCheck)
 		{
 			model.addAttribute("emailAvailabilityError",messageSource.getMessage("de.enwida.userManagement.userNameNotAvailable", null, request.getLocale()));
 		}
 		
 		return availabilityCheck + "";
 	}
 
 	@RequestMapping(value="/activateuser.html",method=RequestMethod.GET)
 	public String activateUser(HttpServletRequest request,ModelMap model, String username, String actId){
 		
 		boolean activated = false;
         try {
             activated = userService.activateUser(username, actId);
         } catch (Exception e) {
             logger.info(e.getMessage());
             model.addAttribute("Error", messageSource.getMessage("de.enwida.userManagement.notAllowed", null, request.getLocale()));
         }
 		if(activated)
 		{
 			String name = "Test Test";
     		String userStatus="logout";
     		String userStatusURL="../j_spring_security_logout";
 
     		model.addAttribute("username", name);
     		model.addAttribute("userStatus", userStatus);
     		model.addAttribute("userStatusURL", userStatusURL);
             model.addAttribute("info", "Activated");  
 		}else{
 	        model.addAttribute("error", messageSource.getMessage("de.enwida.userManagement.notAllowed", null, request.getLocale()));  
 		}
    
         model.addAttribute("content", "user/index");
         return "master";
 	}
 	
 	@RequestMapping(value="/forgotPassword",method=RequestMethod.GET)
     public String showForgotPassForm(ModelMap model){
 		return "user/forgotPassword";
     }
 	
    @RequestMapping(value="/checkImages",method=RequestMethod.GET)
     public @ResponseBody String checkImages(String company){
        LogoFinder logoFinder=new LogoFinder();
        return logoFinder.getImages(company);
     }
 	
 	@RequestMapping(value="/forgotPassword",method=RequestMethod.POST)
 	public String forgotPassword(HttpServletRequest request,ModelMap model,String email){
 		
 		String password = null;
         try {
             password = userService.getPassword(email);
         } catch (Exception e) {
             logger.error(e.getMessage());
         }
 		if(password==null){
 			model.addAttribute("error", messageSource.getMessage("de.enwida.userManagement.userNotFound", null, request.getLocale()));
 		}else{
 			try {
 				mail.SendEmail(email, messageSource.getMessage("de.enwida.userManagement.yourPassword", null, request.getLocale())+":",password);
 			} catch (Exception e) {
 	            logger.error(e.getMessage());
 				model.addAttribute("error", messageSource.getMessage("de.enwida.userManagement.mailingError", null, request.getLocale()));
 			}
 		}
 		return "user/forgotPassword";
 	}
 	
 	@RequestMapping(value="/upload", method = RequestMethod.GET)
 	public ModelAndView getUplaodUserData(ModelMap model) throws Exception {
 		User user = userSession.getUser();
 		if (user != null) {
 			List<UploadedFile> filetable = new ArrayList<UploadedFile>(
 					user.getUploadedFiles());
 			Collections.sort(filetable);
 			model.put("uploadedfiletable", filetable);
 			model.put("fileUpload", new FileUpload());
 		}
 		return new ModelAndView("user/upload", model);
 	}
 	
 	@SuppressWarnings("unchecked")
 	@RequestMapping(value="/upload", method = RequestMethod.POST)
 	public ModelAndView postUplaodUserData(ModelMap model,
 			@ModelAttribute(value = "fileUpload") FileUpload fileUpload,
 			BindingResult result, HttpServletRequest request) throws Exception {
 		
 		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
 		// fetch all files related to user
 		User user = userSession.getUser();
 
 
 		String displayfileName = null;
 		if (user != null && isMultipart) {
 			List<UploadedFile> filetable = new ArrayList<UploadedFile>(
 					user.getUploadedFiles());
             try {
             	FileItem item = fileUpload.getFile().getFileItem();
 				File filetobeuploaded = null;
             	if (!item.isFormField()) {
 					// save file in temporary directory
 					filetobeuploaded = getTemporaryFile(item);
 					// do validation here
 					BindingResult results = validateFile(filetobeuploaded,
 							fileValidator);
 					ObjectError status = results.getGlobalError();
 
 					if (status.getCode().equalsIgnoreCase(
 							"file.upload.parse.success")) {
 
 						Map<String, Object> parsedData = (Map<String, Object>) status
 								.getArguments()[0];
 						List<UserLines> userlines = (List<UserLines>) parsedData
 								.get(Constants.UPLOAD_LINES_KEY);
 						UserLinesMetaData metaData = (UserLinesMetaData) parsedData
 								.get(Constants.UPLOAD_LINES_METADATA_KEY);
 
 						boolean recordsInserted = userLineService
 								.createUserLines(userlines, metaData);
 						if (recordsInserted) {
 							// if atleast one record is written then upload
 							// file.
 							UploadedFile file = saveFile(filetobeuploaded,
 									user, 0);
 							// update user in session as well
 							userSession.setUserInSession(user);
 							// update file Id (which already have owner details)
 							metaData.setFile(file);
 							userLineService.updateUserLineMetaData(metaData);
 
 							EnwidaUtils.removeTemporaryFile(filetobeuploaded);
 
 							model.put("successmsg", uploadsuccessmsg);
 							// update uploaded files list
							filetable.clear();
							filetable.addAll(user.getUploadedFiles());
 						} else {
 							model.put("errormsg", "Duplicate file uploaded");
 						}
 					} else if (status.getCode().equalsIgnoreCase(
 							"file.upload.parse.error")) {
 						model.put("errormsg", status.getDefaultMessage());
 					}
 	            }
             } catch (Exception e) {
 				logger.error("Unable to upload file : " + displayfileName, e);
             }
 			Collections.sort(filetable);
 			model.put("uploadedfiletable", filetable);
         }
 		model.put("fileUpload", new FileUpload());
 		return new ModelAndView("user/upload", model);
 	}
 
 	@RequestMapping(value = "/replaceupload", method = RequestMethod.POST)
 	public ModelAndView replaceUplaodUserData(ModelMap model,
 			@ModelAttribute(value = "fileReplace") FileUpload fileUpload,
 			BindingResult result, HttpServletRequest request) throws Exception {
 
 		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
 		// fetch all files related to user
 		User user = userService.fetchUser("username1");
 		List<UploadedFile> filetable = new ArrayList<UploadedFile>(
 				user.getUploadedFiles());
 
 		String displayfileName = null;
 		if (isMultipart) {
 			try {
 				FileItem item = fileUpload.getFile().getFileItem();
 				if (!item.isFormField()) {
 
 				}
 			} catch (Exception e) {
 				logger.error("Unable to upload file : " + displayfileName, e);
 			}
 		}
 		Collections.sort(filetable);
 		model.put("uploadedfiletable", filetable);
 		model.put("fileUpload", new FileUpload());
 		return new ModelAndView("user/upload", model);
 	}
 
 	public BindingResult validateFile(File file, Validator validator) {
 		// Map<String, Object> objectMap = new LinkedHashMap<String, Object>();
 		// objectMap.put("file", file);
 		DataBinder binder = new DataBinder(file);
 		binder.setValidator(validator);
 		// validate the target object
 		binder.validate();
 		// get BindingResult that includes any validation errors
 		return binder.getBindingResult();
 	}
 
 
 	private File getTemporaryFile(FileItem item) throws Exception {
 		String tempFile = fileUploadDirectory + File.separator + "temp"
 				+ File.separator + EnwidaUtils.extractFileName(item.getName());
 		EnwidaUtils.createDirectory(fileUploadDirectory + File.separator
 				+ "temp");
 		// do validation here
 		File filetobeuploaded = new File(tempFile);
 		item.write(filetobeuploaded);
 		return filetobeuploaded;
 	}
 
 
 	private UploadedFile saveFile(File file, User user, int fileId)
 			throws Exception {
 		String displayfileName = file.getName();
 
 		// Get the next generated value of file sequence
 		Long generatedId = null;
 		try {
 			generatedId = userService.getNextSequence(
 					Constants.UPLOADED_FILE_SEQUENCE_SCHEMA_NAME,
 					Constants.UPLOADED_FILE_SEQUENCE_NAME);
 		} catch (Exception e) {
 			// TODO: handle exception
 			generatedId = new Long(1);
 		}
 		String fileFormat = EnwidaUtils.extractFileFormat(displayfileName);
 		String fileName = "file";
 		if (fileFormat != null && generatedId != null)
 			fileName += "_" + generatedId + "." + fileFormat;
 		else {
 			throw new Exception("No file format");
 		}
 		// make sure that file directory is present
 		// before uploading file
 		EnwidaUtils.createDirectory(fileUploadDirectory);
 
 		// upload file
 		File uploadedFile = new File(fileUploadDirectory + File.separator
 				+ fileName);
 		FileUtils.copyFile(file, uploadedFile);
 		// IOUtils.copyLarge(new FileInputStream(file), new FileOutputStream(
 		// uploadedFile));
 		// item.write(uploadedFile);
 
 		// update file manifest data
 		File manifestFile = new File(fileUploadDirectory + File.separator
 				+ fileName + ".mfst");
 		try {
 			EnwidaUtils.writeAllText(manifestFile, displayfileName);
 		} catch (Exception e) {
 			logger.error(
 					"Update Manifest data for uploaded File: " + file.getName(),
 					e);
 		}
 
 		// create entry in file upload table
 		// UploadedFile uploadedfile = fileId > 0 ? userService.getFile(fileId)
 		// : new UploadedFile();
 		UploadedFile uploadedfile = new UploadedFile();
 		uploadedfile.setDisplayFileName(displayfileName);
 		uploadedfile.setFileName(fileName);
 		uploadedfile.setFilePath(fileUploadDirectory + File.separator
 				+ fileName);
 		uploadedfile.setFormat(fileFormat);
 		uploadedfile.setUploadDate(new Date());
 
 		uploadedfile.setUploader(user);
 		// User user = userSession.getUser();
 		// update revision based on
 		// int revision = userService.getUploadedFileVersion(uploadedfile,
 		// user);
 		if (fileId == 0) {
 			uploadedfile.setRevision(1);
 		} else {
 			// replace option means create new revision and insert new file
 			// update modification date
 			uploadedfile.setModificationDate(new Date());
 		}
 		user = userService.saveUserUploadedFile(user, uploadedfile);
 
 		return uploadedfile;
 	}
 
 	@RequestMapping(value = "/files/{fileId}", method = RequestMethod.GET)
 	@ResponseBody
 	public String downloadFile(@PathVariable("fileId") String fileId,
 			HttpServletResponse reponse) {
 		if (fileId != null) {
 			int fileid = Integer.parseInt(fileId);
 			// System.out.println("File Id : " + userService.getFile(fileid));
 			UploadedFile downloadFile = userService.getFile(fileid);
 			if (downloadFile == null)
 				return null;
 			String filePath = downloadFile.getFilePath();
 			File file = new File(filePath);
 			reponse.setHeader("Content-Disposition", "attachment; filename=\""
 					+ downloadFile.getDisplayFileName() + "\"");
 			OutputStream out = null;
 			try {
 				out = reponse.getOutputStream();
 				reponse.setContentType("text/plain; charset=utf-8");
 				FileInputStream fi = new FileInputStream(file);
 
 				IOUtils.copy(fi, out);
 				out.flush();
 				out.close();
 			} catch (IOException e) {
 				logger.error("Unable to download file : ", e);
 			} finally {
 				out = null;
 			}
 		}
 		return null;
 	}
 }
