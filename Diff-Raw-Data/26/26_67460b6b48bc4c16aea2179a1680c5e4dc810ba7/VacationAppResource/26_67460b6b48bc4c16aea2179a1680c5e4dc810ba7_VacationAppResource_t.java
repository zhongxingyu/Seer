 package com.gtech.webservices;
 
 import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
 
 //import javax.xml.bind.DatatypeConverter;
 import javax.annotation.Resource;
 //import javax.mail.Message;
 //import javax.mail.MessagingException;
 //import javax.mail.Transport;
 //import javax.mail.internet.InternetAddress;
 //import javax.mail.internet.MimeMessage;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 //import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 //import javax.ws.rs.QueryParam;
 //import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 
import org.jboss.resteasy.spi.NotFoundException;

 
 public class VacationAppResource implements VacationAppInterface{
 	 @Resource(mappedName = "java:jboss/mail/VacApp_GMAIL")
 	 javax.mail.Session mailSession;
 	 
 	private VacationDao vacationDAO;
 	@Override
 	@GET
 	@Path("/VacationList/{vacationSince}/{vacationUntil}")
 	@Produces({ "application/json", "application/xml" })
 	public VacationList getVacationList(String auth, String vacationSince, String vacationUntil) {		
		DateFormat formatter ; 
		
		formatter = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date dateVacationSince, dateVacationUntil;
			dateVacationSince = formatter.parse(vacationSince);
			dateVacationUntil = formatter.parse(vacationUntil);
			
			if( dateVacationSince.compareTo(dateVacationUntil) > 0 )
				throw new NotFoundException("It is not possible to add vacation. Date \"Since\" is after date \"Until\".");
		
		} catch (ParseException e) {
			throw new NotFoundException("It is not possible to add vacation. Invalid date format.");
		}
		
 		
 		//vacationDAO.save(vacationDAO.fakeVacation());
 		VacationList vacationList = new VacationList();
 		//vacationDAO.fakeVacation();
 		vacationList.setVacations(vacationDAO.getVacationList(getUserFromAuth(auth), vacationSince, vacationUntil));
		
 /*************************
         //
         // Creates email message
         //
 		   String to = "grzegorzbielanski@gmail.com";
 		   String from = "vacationapp@gmail.com";
 		   String subject = "Testing...";
 		   Message msg = new MimeMessage(mailSession);
 		    try {
 		      msg.setFrom(new InternetAddress(from));
 		      msg.setRecipient(Message.RecipientType.TO , new InternetAddress(to));
 		      msg.setSubject(subject);
 		      msg.setText("Working fine..!");
 		    }  catch(Exception exc) {
 		       }
 
         //
         // Send a message
         //
         try {
 			Transport.send(msg);
 		} catch (MessagingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}		
 		
 ***************************/		
 		
 		return vacationList;
 	}
 
 	@GET
 	@Path("/VacationSummary")
 	@Produces({ "application/json", "application/xml" })
 	public VacationSummary getVacationSummary(String auth) {
         
         //vacationDAO.saveSummary(vacationDAO.fakeVacationSummary());
 		//return vacationDAO.fakeVacationSummary();
         
         return vacationDAO.getVacationSummary(getUserFromAuth(auth));
 	}
 
 	@Override
 	@POST
 	@Path("/NewVacation")
 	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
 	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
 	public Vacation addVacation(String auth, Vacation vacation) {
 		// TODO Auto-generated method stub
 		//EscalationList escalation = 
 	    vacationDAO.addVacationRequest(vacation, getUserFromAuth(auth));
 		//TODO ESCALATE!!!
 		//vacationDAO.save(vacation);
 		return vacation;
 		
 	}
 
 	@Override
 	@POST
 	@Path("/ExistingVacation")
 	@Consumes({ "application/json", "application/xml" })
 	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
 	public Vacation updateVacation(String auth, Vacation vacation) {
 		
 		Vacation vUpdated = vacationDAO.updateVacationRequest(vacation, getUserFromAuth(auth));
 		return vUpdated;
 	}
 	
 	public VacationDao getVacationDao() {
 		return vacationDAO;
 	}
 
 	public void setVacationDao(VacationDao vacationDao) {
 		this.vacationDAO = vacationDao;
 		}
 	
 	private String getUserFromAuth(String auth)
 	{
 		String userAndPassword64 = auth.replace("Basic ", "");
 		
 		byte[] encodedDataAsBytes =  javax.xml.bind.DatatypeConverter.parseBase64Binary(userAndPassword64);
 		String val ="";
 		try {
 			val = new String(encodedDataAsBytes, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
         String user = val.substring(0, val.indexOf(':'));
         /*String pass = val.substring(val.indexOf(':') + 1);*/	
 		
         return user;
 	}
 }
