 /**
  *    Copyright 2012 meltmedia
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package com.meltmedia.cadmium.email.jersey;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.meltmedia.cadmium.core.CadmiumApiEndpoint;
 import com.meltmedia.cadmium.core.ContentService;
 import com.meltmedia.cadmium.email.model.EmailForm;
 import com.meltmedia.cadmium.email.EmailException;
 import com.meltmedia.cadmium.email.VelocityHtmlTextEmail;
 import com.meltmedia.cadmium.email.internal.EmailServiceImpl;
 
 @CadmiumApiEndpoint
 @Path("/email")
 public class EmailResource {
 	
 	private Logger log = LoggerFactory.getLogger(getClass());
 	
 	@Inject
 	private EmailServiceImpl emailService;
 	
 	@Inject
 	private ContentService contentService;
 
  	
 	@POST
   @Consumes("application/x-www-form-urlencoded")
   @Produces(MediaType.APPLICATION_JSON)
 	public Response emailThisPage(@Context HttpServletRequest request,@FormParam(Constants.DIR) String dir,@FormParam(Constants.TO_NAME) String toName,
 			                          @FormParam(Constants.TO_ADDRESS) String toAddress,@FormParam(Constants.FROM_NAME) String fromName,
 			                          @FormParam(Constants.FROM_ADDRESS) String fromAddress,@FormParam(Constants.MESSAGE) String message,
 			                          @FormParam(Constants.PAGE_PATH) String pagePath,@FormParam(Constants.SUBJECT) String subject) {
 
   	log.info("Entering Email This Method");
   	VelocityHtmlTextEmail email = new VelocityHtmlTextEmail();
   	
   	// Setting up template location/files
   	File absoluteTemplateDir = new File(contentService.getContentRoot(),"META-INF");
   	absoluteTemplateDir = new File(absoluteTemplateDir,dir);
 	  File textTemplateFile = new File(absoluteTemplateDir,Constants.TEMPLATE_NAME + ".txt");
 	  log.info("textTemplateFile: {}", textTemplateFile.getPath());
   	File htmlTemplateFile = new File(absoluteTemplateDir,Constants.TEMPLATE_NAME + ".html");
   	log.info("htmlTemplateFile: {}", htmlTemplateFile.getPath());
   	if (textTemplateFile.exists() && htmlTemplateFile.exists()) {
   		if (pageExists(pagePath)) {
 		  	try { 
 		  		EmailForm emailForm = new EmailForm(toName, toAddress, fromName, fromAddress, message, pagePath,subject);
 		  		log.info("Email Form: {}", emailForm.toString());
 					EmailFormValidator.validate(emailForm);
 			  	
 			  	email.addTo(emailForm.getToAddress());
 			  	email.setReplyTo(emailForm.getFromAddress());
 			  	email.setFrom(emailForm.getFromAddress()); 
 			  	email.setSubject(emailForm.getSubject());
 			  	// Set HTML Template
 			  	email.setHtml(readFromFile(htmlTemplateFile.getAbsolutePath()));
 			  	
 			  	// Set Text Template
 			  	email.setText(readFromFile(textTemplateFile.getAbsolutePath()));
 			  	
 			  	// Set Properties
 			  	email.setProperty(Constants.TO_NAME, emailForm.getToName());
 			  	email.setProperty(Constants.FROM_NAME, emailForm.getFromName());
			  	
			    // setting message to at least empty string to assure that ${message} gets replaced on the email form.
			  	if (emailForm.getMessage() == null) { 
			  		emailForm.setMessage("");
	        }
 			  	email.setProperty(Constants.MESSAGE, emailForm.getMessage());
 			  	
 			  	// Set up link
 			  	String link = "http://" + request.getServerName() + "/"  + emailForm.getPagePath();
 			  	log.info("Email This Page Link: {}",link);
 			  	email.setProperty(Constants.PATH,link);
 								  	
 			  	// Send Email
 			  	log.debug("Before Sending Email");  		
 			  	emailService.send(email);
 			  	log.debug("After Sending Email");
 				} catch (EmailException e) {
 					log.info("EmailException Caught " + e.getMessage());
 					return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
 				} catch (ValidationException e) {
 					log.info("ValidationException Caught");
 					log.info("First Error {}",e.getErrors()[0].getMessage());
 					return Response.status(Response.Status.BAD_REQUEST).entity(e.getErrors()).build();
 				} 	
 				return Response.ok().build();
   		} else {
     		log.info("Invalid Page");
     		return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Page").build();	
   		}
   	} else {
   		log.info("Couldn't Find Email Templates");
   		return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Template Location").build();		  		
   	}
 	}
 	
 	private boolean pageExists(String pagePath) {
 		if (pagePath.contains("-INF")) {
 			return false;
 		}
 		File pagePathFile = new File(contentService.getContentRoot(),pagePath);
 		return pagePathFile.exists();
 	}
 
 	/*
    * Reads data from a given file
    */
   public String readFromFile(String fileName) {
     String content = null;
     BufferedReader br = null;
     try {
       File inFile = new File(fileName);
       br = new BufferedReader(new InputStreamReader(
           new FileInputStream(inFile)));
       content = "";
       while(br.ready()) {
       	content += br.readLine();
       }
       br.close();
     } catch (FileNotFoundException ex) {
     	log.error("Failed to find file.", ex);
     } catch (IOException ex) {
     	log.error("Failed to read file.", ex);
     } finally {
     	if(br != null) {
     		try {
     			br.close();
     		} catch(Exception e){}
     	}
     }
     return content;
   }
   
 }
