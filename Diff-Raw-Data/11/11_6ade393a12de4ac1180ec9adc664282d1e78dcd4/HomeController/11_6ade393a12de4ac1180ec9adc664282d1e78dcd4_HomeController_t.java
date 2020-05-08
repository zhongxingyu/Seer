 package com.google.code.qualitas.webapp.home;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.annotation.Secured;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.multipart.MultipartFile;
 
 import com.google.code.qualitas.engines.api.core.ProcessType;
 import com.google.code.qualitas.integration.api.InstallationException;
 import com.google.code.qualitas.integration.api.InstallationService;
 import com.google.code.qualitas.integration.api.InstallationOrder;
 
 /**
  * The Class HomeController.
  */
 @Controller
 @Secured("ROLE_USER")
 @RequestMapping("/home")
 @SessionAttributes("username")
 public class HomeController {
 
     /** The installation service. */
     @Autowired
     private InstallationService installationService;
 
     /** The Constant LOG. */
     private static final Log LOG = LogFactory.getLog(HomeController.class);
 
     /**
      * Index.
      * 
      * @param request
      *            the request
      * @param model
      *            the model
      * @return the string
      */
     @RequestMapping(value = "/index", method = RequestMethod.GET)
     public String index(HttpServletRequest request, Model model) {
 
         String username = request.getUserPrincipal().getName();
 
         model.addAttribute("username", username);
 
         return "home/index";
     }
 
     /**
      * Index upload.
      * 
      * @param model
      *            the model
      * @param username
      *            the username
      * @param file
      *            the file
      * @return the string
      * @throws IOException
     *             Signals that an I/O exception has occurred.
      * @throws InstallationException
      *             the installation exception
      */
     @RequestMapping(value = "/index", method = RequestMethod.POST)
     public String indexUpload(Model model, @ModelAttribute("username") String username,
             @RequestParam("file") MultipartFile file) throws IOException, InstallationException {
 
         LOG.debug("Uploaded file " + file.getOriginalFilename());
 
         byte[] bundle = IOUtils.toByteArray(file.getInputStream());
 
         InstallationOrder installationOrder = new InstallationOrder();

         installationOrder.setBundle(bundle);
         installationOrder.setUsername(username);
         installationOrder.setContentType(file.getContentType());
         installationOrder.setProcessId(System.currentTimeMillis());
         installationOrder.setProcessType(ProcessType.WS_BPEL_2_0_APACHE_ODE);

         installationService.install(installationOrder);
 
         LOG.debug("File sent");
 
         return "home/index";
     }
 
 }
