 package envite.web.controller;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Properties;
 import java.io.File;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.ByteArrayInputStream;
 
 import javax.mail.Message;
 import javax.mail.Message.RecipientType;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.mail.MessagingException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletResponse;
 
 import org.hibernate.Hibernate;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.support.SessionStatus;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.core.io.FileSystemResource;
 
 import org.springframework.security.core.Authentication;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.security.core.context.SecurityContextHolder;
 
 import javax.servlet.http.HttpSession;
 
 import org.springframework.mail.javamail.JavaMailSenderImpl;
 import org.springframework.mail.javamail.MimeMessageHelper;
 
 import java.io.IOException;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 
 import org.apache.commons.io.FileUtils;
 
 import envite.model.User;
 import envite.model.Guest;
 import envite.model.dao.UserDao;
 import envite.model.Event;
 import envite.model.dao.EventDao;
 import envite.web.validator.EventValidator;
 
 @Controller
 @SessionAttributes("event")
 public class EventController {
 
     @Autowired
     UserDao userDao;
 
     @Autowired
     EventDao eventDao;
 
     @Autowired
     JavaMailSenderImpl mailSender;
 
     @Autowired
     EventValidator eventValidator;
 
     @RequestMapping(value = "/create.html", method = RequestMethod.GET)
     public String create( ModelMap models,
                 HttpSession session )
     {
 
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         String name = auth.getName(); //get logged in username
 
         models.put("username", name);
 
         models.put( "event", new Event() );
 
         return "create";
     }
 
     @RequestMapping(value="/create.html", method = RequestMethod.POST)
     public @ResponseBody Event created( @RequestBody Event event,
                 HttpSession session ) throws ServletException, MessagingException, IOException {   
 
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         String name = auth.getName(); //get logged in username
 
         User user = userDao.getUser(name);
 
         event.setCreator(user);
 
         eventDao.saveEvent( event );
 
         event = eventDao.getEvents(user).get(eventDao.getEvents(user).size()-1);
 
         return event;
     }
 
     @RequestMapping(value="/upload.html", method = RequestMethod.GET)
     @Transactional
     public String upload( ModelMap models, @RequestParam Integer id,
                 HttpSession session ) throws IOException {
 
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         String name = auth.getName(); //get logged in username
 
         models.put("username", name);
 
         models.put( "id", id );
 
         return "upload";
     }
 
     @RequestMapping(value="/{id}/upload.html", method = RequestMethod.POST)
     public String upload( @RequestParam("banner") MultipartFile banner, 
         HttpServletRequest request,
         @PathVariable Integer id ) throws ServletException, MessagingException, IOException {
         Event event = eventDao.getEvent(id);
         event.setBanner(banner.getBytes());
 
         String baseUrl = String.format("%s://%s:%d/",request.getScheme(),  request.getServerName(), request.getServerPort());
 
         for ( Guest g : event.getGuests() ) {
             String from = event.getCreator().getEmail();
             String to = g.getEmail();
             String subject = event.getTitle();
             String content = event.getMessage();
 
             String guestNameHashed = null;
 
             try {
              
                 //Create MessageDigest object for MD5
                 MessageDigest digest = MessageDigest.getInstance("MD5");
 
                 String secret = g.getName() + ":" + g.getEmail();
 
                 //Update password string in message digest
                 digest.update(secret.getBytes(), 0, secret.length());
 
                 //Converts message digest value in base 16 (hex)
                 guestNameHashed = new BigInteger(1, digest.digest()).toString(16);
 
             } catch (NoSuchAlgorithmException e) {
                 e.printStackTrace();
             }
 
             MimeMessage message = mailSender.createMimeMessage();
 
             MimeMessageHelper helper;
 
             helper = new MimeMessageHelper(message, true);
             helper.setFrom("no.reply@envite.com <" + from + ">");
             helper.setTo(to);
             helper.setSubject(subject);
 
             helper.setText("<html><body><img src='cid:identifier1234'><hr><h1>Dear " + g.getName()
                     + ": </h1><h2>You have been invited by "
                     + event.getCreator().getFirstName() + " "
                     + event.getCreator().getLastName() + " to the event "
                     + subject + "<h2><p>Message from "
                     + event.getCreator().getFirstName() + ": </p><p>"
                     + content
                     + "<a href=\"" + baseUrl + "520-Envite/guestRespond.html?id=" + event.getId() + "&guestName=" + guestNameHashed + "&respond=true\">Accept</a></br>"
                     + "<a href=\"" + baseUrl + "520-Envite/guestRespond.html?id=" + event.getId() + "&guestName=" + guestNameHashed + "&respond=false\">Reject</a>"
                     + "</body></html>"
             , true);
 
             File image = File.createTempFile("imageAttachment", ".tmp");
 
             FileUtils.writeByteArrayToFile(image, event.getBanner());
             FileSystemResource res = new FileSystemResource(image);
 
             helper.addInline("identifier1234", res);
 
             mailSender.send(message);
             g.setEmailed(true);
             
         }
 
         event.setCreator( userDao.getUser( event.getCreator().getUsername() ) );
 
         eventDao.saveEvent( event );
 
         return "redirect:/events.html";
     }
 
     @RequestMapping("/bannerImg.html")
     @Transactional
     public String download( @RequestParam Integer id,
         HttpServletResponse response,
         HttpSession session ) throws IOException {
 
         byte[] buffer = eventDao.getEvent( id ).getBanner();
 
         InputStream in = (buffer == null) ? session.getServletContext().getResourceAsStream("images/turkey_icon.png") : 
             new ByteArrayInputStream(buffer);
         OutputStream out = response.getOutputStream();
         byte buffer2[] = new byte[2048];
         int bytesRead;
         while( (bytesRead = in.read( buffer2 )) > 0 )
             out.write( buffer2, 0, bytesRead );
         in.close();
 
         return null;
     }
 
     @RequestMapping(value = "/events.html", method = RequestMethod.GET)
     @Transactional
     public String list( ModelMap models,
                 HttpSession session )
     {
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         String name = auth.getName(); //get logged in username
 
         models.put("username", name);
 
         User user = userDao.getUser(name);
 
         models.put( "events", eventDao.getEvents( user ) );
 
         return "list";
     }
 
     @RequestMapping(value="/email.html", method = RequestMethod.POST)
     @Transactional
     public String email( HttpServletRequest request ) throws MessagingException, IOException {
         int eventId = Integer.parseInt(request.getParameter("id"));
         String[] emailListing = request.getParameterValues("emails");
         String content = request.getParameter("message");
 
         String baseUrl = String.format("%s://%s:%d/",request.getScheme(),  request.getServerName(), request.getServerPort());
 
         @SuppressWarnings("unchecked")
         Event event = eventDao.getEvent(eventId);
 
         for ( Guest g : event.getGuests() ) {
             String from = event.getCreator().getEmail();
             String to = g.getEmail();
             String subject = event.getTitle();
 
             String guestNameHashed = null;
 
             try {
              
                 //Create MessageDigest object for MD5
                 MessageDigest digest = MessageDigest.getInstance("MD5");
 
                 String secret = g.getName() + ":" + g.getEmail();
 
                 //Update password string in message digest
                 digest.update(secret.getBytes(), 0, secret.length());
 
                 //Converts message digest value in base 16 (hex)
                 guestNameHashed = new BigInteger(1, digest.digest()).toString(16);
 
             } catch (NoSuchAlgorithmException e) {
                 e.printStackTrace();
             }
 
             MimeMessage message = mailSender.createMimeMessage();
 
             MimeMessageHelper helper;
 
             helper = new MimeMessageHelper(message, true);
             helper.setFrom("no.reply@envite.com <" + from + ">");
             helper.setTo(to);
             helper.setSubject(subject);
 
             helper.setText("<html><body><img src='cid:identifier1234'><hr><h1>Dear " + g.getName()
                     + ": </h1><h2>You have been invited by "
                     + event.getCreator().getFirstName() + " "
                     + event.getCreator().getLastName() + " to the event "
                     + subject + "<h2><p>Message from "
                     + event.getCreator().getFirstName() + ": </p><p>"
                     + content
                     + "<a href=\"" + baseUrl + "520-Envite/guestRespond.html?id=" + event.getId() + "&guestName=" + guestNameHashed + "&respond=true\">Accept</a></br>"
                     + "<a href=\"" + baseUrl + "520-Envite/guestRespond.html?id=" + event.getId() + "&guestName=" + guestNameHashed + "&respond=false\">Reject</a>"
                     + "</body></html>"
             , true);
 
             File image = File.createTempFile("imageAttachment", ".tmp");
 
             FileUtils.writeByteArrayToFile(image, event.getBanner());
             FileSystemResource res = new FileSystemResource(image);
 
             helper.addInline("identifier1234", res);
 
             mailSender.send(message);
             g.setEmailed(true);
             
         }
 
         return "redirect:/events.html";
     }
 
     @RequestMapping("/editEvent.html")
     @Transactional
     public String edit( @RequestParam Integer id, ModelMap models, HttpSession session )
     {
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         String name = auth.getName(); //get logged in username
 
         models.put("username", name);
 
         User user = userDao.getUser(name);
 
         if ( !user.getUsername().equals(eventDao.getEvent( id ).getCreator().getUsername()) )
             return "redirect:/index.html";
 
         models.put( "event", eventDao.getEvent( id ) );
 
         return "edit";
     }
 
     @PreAuthorize("principal.username == #event.creator.username")
     @RequestMapping(value="/editEvent.html", method = RequestMethod.POST)
     @Transactional
     public @ResponseBody Event edited( @RequestBody final Event event,
                 HttpSession session ) throws ServletException {
 
        event.setCreator( userDao.getUser( event.getCreator().getUsername() ) );
 
         eventDao.saveEvent( event );
 
         return event;
     }
 
     @RequestMapping("/eventDetail.html")
     @Transactional
     public String detail( @RequestParam Integer id, ModelMap models, HttpSession session )
     {
         models.put( "event", eventDao.getEvent( id ) );
 
         return "detail";
     }
 
     @RequestMapping("/guestRespond.html")
     @Transactional
     public String download( @RequestParam Integer id,
         @RequestParam boolean respond,
         @RequestParam String guestName,
         ModelMap models ) throws IOException {
 
         Event event = eventDao.getEvent( id );
 
         boolean found = false;
 
         for ( Guest g: event.getGuests() ) {
             String guestNameHashed = null;
 
             try {
              
                 //Create MessageDigest object for MD5
                 MessageDigest digest = MessageDigest.getInstance("MD5");
 
                 String secret = g.getName() + ":" + g.getEmail();
 
                 //Update password string in message digest
                 digest.update(secret.getBytes(), 0, secret.length());
 
                 //Converts message digest value in base 16 (hex)
                 guestNameHashed = new BigInteger(1, digest.digest()).toString(16);
 
             } catch (NoSuchAlgorithmException e) {
                 e.printStackTrace();
             }
 
             if (guestNameHashed.equals(guestName)) {
                 g.setRespond(respond);
                 models.put( "guestResult", "Guest (" + g.getName() + ") respond: " + respond );
                 found = true;
             }
         }
 
         eventDao.saveEvent( event );
 
         if (!found) models.put("guestResult", "guest not found!");
         
         models.put("event", event);
 
         return "detail";
     }
 
     @RequestMapping("/guests.html")
     @Transactional
     public @ResponseBody List<Guest> download( @RequestParam String guestName,
         @RequestParam String username ) throws IOException {
 
         List<Guest> result = new ArrayList<Guest>();
         
         User user = userDao.getUser( username );
 
         for ( Guest g: user.getGuests() ) {
             if ( g.getName().toLowerCase().contains( guestName.toLowerCase() ) ) {
                 result.add(g);
             }
         }
         
         return result;
     }
 }
