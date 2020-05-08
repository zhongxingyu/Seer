 package com.appspot.thejobmap.server;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Properties;
 
 import javax.activation.DataHandler;
 import javax.activation.DataSource;
 import javax.mail.Message;
import javax.mail.MessagingException;
 import javax.mail.Multipart;
 import javax.mail.Session;
 import javax.mail.Transport;
import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 import javax.mail.util.ByteArrayDataSource;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.appspot.thejobmap.shared.ApplyObj;
 import com.appspot.thejobmap.shared.MarkerObj;
 import com.appspot.thejobmap.shared.ResultObj;
 import com.appspot.thejobmap.shared.UserObj;
 import com.google.appengine.api.blobstore.BlobInfo;
 import com.google.appengine.api.blobstore.BlobInfoFactory;
 import com.google.appengine.api.blobstore.BlobKey;
 import com.google.appengine.api.blobstore.BlobstoreService;
 import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.gson.Gson;
 
 /**
  * This servlet handles job applications.
  * 
  * @author Stefan Sundin
  * @author Alexandra Tsampikakis
  */
 public class ApplyServlet extends HttpServlet {
 
 	private static final long serialVersionUID = -265508910555704883L;
 
 	UserServlet userServlet = new UserServlet();
 	MarkerServlet markerServlet = new MarkerServlet();
 	
 	/**
 	 * POST - Apply for a job.
 	 */
 	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
 		// Initialize stuff like streams
 		req.setCharacterEncoding("UTF-8");
 		res.setContentType("application/json; charset=UTF-8");
 		TheJobMap.setAccessControl(req, res);
 		BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
 		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(res.getOutputStream()));
 		DatastoreService db = DatastoreServiceFactory.getDatastoreService();
 		Gson gson = new Gson();
 		UserObj me = new UserObj();
 		Entity entityMarker = null;
 		MarkerObj dbMarker = new MarkerObj();
 
 		// Parse path
 		String path = req.getPathInfo();
 		path = (path==null?"/":path);
 		System.out.println("POST /apply"+path);
 		System.out.flush();
 		//System.out.close();
 		String[] resource = path.split("/");
 		
 		// Fetch user details
 		Entity entityMe = userServlet.getUser();
 		if (entityMe == null) {
 			res.setStatus(403);
 			writer.write(gson.toJson(new ResultObj("fail", "not logged in")));
 			writer.close();
 			return;
 		}
 		me.convertFromEntity(entityMe);
 		
 		// Check so that the user has provided enough information
 		if (!me.canApply()) {
 			res.setStatus(403);
 			writer.write(gson.toJson(new ResultObj("fail", "not enough user info")));
 			writer.close();
 			return;
 		}
 		
 		// Parse input
 		ApplyObj application = gson.fromJson(reader, ApplyObj.class);
 		application.sanitize();
 		reader.close();
 		
 		if (resource.length == 2) {
 			// POST /apply/<id>
 			// Apply for a job
 			// Sends an email to the author of the pin
 			entityMarker = markerServlet.getMarker(resource[1], false);
 			if (entityMarker == null) {
 				res.setStatus(403);
 				writer.write(gson.toJson(new ResultObj("fail", "no such marker")));
 				writer.close();
 				return;
 			}
 			dbMarker.convertFromEntity(entityMarker);
 			if (!"company".equals(dbMarker.type)) {
 				res.setStatus(403);
 				writer.write(gson.toJson(new ResultObj("fail", "not a company marker")));
 				writer.close();
 				return;
 			}
 			
 			try {
 				Properties props = new Properties();
 				Session session = Session.getDefaultInstance(props, null);
 				Multipart mp = new MimeMultipart();
 				
 				// Set metadata
 				Message msg = new MimeMessage(session);
				msg.setFrom(new InternetAddress("thejobmap@appspot.gserviceaccount.com", "The Job Map"));
 				msg.addRecipient(Message.RecipientType.TO, new InternetAddress(dbMarker.author));
 				msg.setSubject("Job Application: "+dbMarker.title);
 				
 				// Get birthday in "July 9, 1989" format.
 				String months[] = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
 				Calendar bday = Calendar.getInstance();
 				bday.setTime(new Date(me.birthday));
 				String birthday = months[bday.get(Calendar.MONTH)]+" "+bday.get(Calendar.DATE)+", "+bday.get(Calendar.YEAR);
 				
 				// Compose message
 				String msgBody = "<p><a href=\"http://www.thejobmap.se/\"><img src=\"http://www.thejobmap.se/images/logo.png\" /></a></p>\n"+
 						"\n"+
 						"<p>A job application has been submitted to this job offer: <b>"+dbMarker.title+"</b>.</p>\n"+
 						"\n"+
 						"<b>Information about the applicant:</b><br/>\n"+
 						"<b>Name:</b> "+me.name+"<br/>\n"+
 						"<b>Date of birth:</b> "+birthday+"<br/>\n"+
 						"<b>Sex:</b> "+me.sex+"<br/>\n"+
 						"<b>Phone number:</b> "+me.phonenumber+"<br/>\n"+
 						"<b>Email:</b> "+me.email+"<br/>\n"+
 						"<b>CV:</b> "+(me.cvUploaded?"Attached":"Not supplied")+"<br/>\n"+
 						"<br/>\n"+
 						"<b>Motivation supplied:</b><br/>\n"+
 						"<p>"+application.motivation+"</p>";
 				
 				// Add HTML and plain text parts
 				MimeBodyPart htmlPart = new MimeBodyPart();
 				htmlPart.setContent(msgBody, "text/html");
 				mp.addBodyPart(htmlPart);
 				msg.setText(msgBody.replaceAll("\\<.*?>",""));
 				
 				// Attach CV, if it exists
 				if (me.cvUploaded) {
 					// Get blob
 					res.setContentType("application/pdf");
 					BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
 					BlobKey blobKey = new BlobKey((String) entityMe.getProperty("cv"));
 					BlobInfoFactory blobInfoFactory = new BlobInfoFactory(db);
 					BlobInfo blobInfo = blobInfoFactory.loadBlobInfo(blobKey);
 					byte[] cv = blobstoreService.fetchData(blobKey, 0, blobInfo.getSize());
 					
 					// Attach CV
 					MimeBodyPart attachment = new MimeBodyPart();
 					attachment.setFileName(blobInfo.getFilename());
 					DataSource src = new ByteArrayDataSource(cv, "application/pdf");
 					attachment.setDataHandler(new DataHandler(src));
 					mp.addBodyPart(attachment);
 				}
 				
 				// Set contents
 				msg.setContent(mp);
 				msg.saveChanges();
 				
 				// Send email
 				Transport.send(msg);
 				System.out.println("Sent job application to "+dbMarker.author+" concerning \""+dbMarker.title+"\"");
			} catch (AddressException e) {
				throw new ServletException("AddressException");
			} catch (MessagingException e) {
				throw new ServletException("MessagingException");
 			}
 			
 			// Update numApply
 			dbMarker.incApply();
 			dbMarker.updateEntity(entityMarker);
 			db.put(entityMarker);
 			
 			// Send response
 			writer.write(gson.toJson(new ResultObj("ok")));
 		}
 		else {
 			throw new ServletException("Unimplemented request.");
 		}
 		writer.close();
 	}
 
 	/**
 	 * For cross-site scripting.
 	 */
 	protected void doOptions(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
 		TheJobMap.setAccessControl(req, res);
 	}
 	
 }
