 package org.openmrs.module.hr.web.servlet;
 
 import org.openmrs.api.context.Context;
 import org.openmrs.module.hr.HrIscoCodes;
 import org.openmrs.module.hr.api.HRPostService;
 import org.openmrs.module.hr.api.HRQualificationService;
 import org.openmrs.util.OpenmrsUtil;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: sandeep
  * Date: 25/6/12
  * Time: 12:31 AM
  * To change this template use File | Settings | File Templates.
  */
 public class ImageServlet extends HttpServlet {
 
     private static final int IMAGE_SIZE = 102400 * 5;
 
     public ImageServlet(){
 
     }
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         String id=request.getParameter("staffCertId");
         String size = request.getParameter("size");
         HRQualificationService hrQualificationService = Context.getService(HRQualificationService.class);
         response.setHeader("Cache-Control", "no-cache");
         response.setContentType("images");
         String imagePath = OpenmrsUtil.getApplicationDataDirectory() + "//hr_certificates";
 
        File imageFile = new File(imagePath,"staff_cert_"+size+"_"+hrQualificationService.getStaffCertificateById(Integer.parseInt(id)).getId());
         FileInputStream image = new FileInputStream(imageFile);
         response.setContentLength(IMAGE_SIZE);
         byte[] imageBytes = new byte[IMAGE_SIZE];
         image.read(imageBytes);
         response.getOutputStream().write(imageBytes);
     }
 
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
     }
 
 
 }
