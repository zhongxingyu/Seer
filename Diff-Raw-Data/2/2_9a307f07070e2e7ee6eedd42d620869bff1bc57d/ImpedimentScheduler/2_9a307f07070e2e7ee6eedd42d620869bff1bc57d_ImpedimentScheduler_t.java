 package com.cisco.diddo.businesslogic;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.mail.javamail.JavaMailSenderImpl;
 import org.springframework.mail.javamail.MimeMessageHelper;
 import org.springframework.mail.javamail.MimeMessagePreparator;
 
 import com.cisco.diddo.dao.ImpedimentDao;
 import com.cisco.diddo.dao.SprintDao;
 import com.cisco.diddo.dao.TeamDao;
 import com.cisco.diddo.dao.UserDao;
 import com.cisco.diddo.entity.Impediment;
 import com.cisco.diddo.entity.Team;
 import com.cisco.diddo.entity.User;
 import com.sun.xml.internal.messaging.saaj.packaging.mime.MessagingException;
 
 public class ImpedimentScheduler {
 	@Autowired
 	public ImpedimentDao impedimentDao;
     
     @Autowired
     public SprintDao sprintDao;
     
     @Autowired
     public UserDao userDao;
     
     @Autowired
     public TeamDao teamDao;
     
     private long dateIntervalInSecs = 1000*60*60*24l;
     
     private DateFormat format = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
 
     
     public void init(){
     	//addInitialData();
     	Timer timer = new Timer();
     	timer.scheduleAtFixedRate(new SendMail(), getScheduleStartDate(), dateIntervalInSecs);
     }
     /*private void addInitialData() {
         User usr = new User();
         usr.setEmail("sbendige@cisco.com");
         usr.setPassword("sach123");
         usr.setScrumMaster(true);
         usr.setUsername("ravi");
         Team team = new Team();
                 
         team.setEmail("cuic_outlander@cisco.com");
         team.setName("CUIC-OUTLANDER");
         teamDao.save(team);
         
         usr.setTeam(team);
         userDao.save(usr);
         
         Impediment imp = new Impediment();
         imp.setClosed(false);
         imp.setDescription("Mac Pro to be provided to ALL");
         imp.setSubmittedDate(Calendar.getInstance());
         imp.setSubmitter(usr);
         impedimentDao.save(imp);
 	}*/
 	private Date getScheduleStartDate() {
     	try{
             return format.parse("08/21/2012 10:00:00");
     	}catch(ParseException ex ){
     		
     	}
     	return new Date();
     }
     private class SendMail extends TimerTask{
     	
 		@Override
 		public void run() {
 			sendMail();
 		}
 		
 		private void sendMail(){
 			JavaMailSenderImpl emailSender = new JavaMailSenderImpl();
 	        emailSender.setHost("72.163.197.20");
 //	        emailSender.setUsername("sbendige");
 //	        emailSender.setPassword("Sailingma%25");
 	        List<Team> teamList = teamDao.findAll();
 			for(Team team : teamList){
 				String ccMail = team.getEmail();
 				User scrumMaster = getScrumMaster(team);
 				if(scrumMaster != null){
 				   String toMail = scrumMaster.getEmail();
 				   List<Impediment> impedimentList = impedimentDao.findAllBySubmitterId_Team(team);
 				   String bodyText = getBodyText(impedimentList);
 				   try{
 					   if(!bodyText.equals("")){
 				          emailSender.send(new ImpedimentMimePreparator( bodyText , "scrum@cisco.com" , toMail ,ccMail ));
 					   }
 				   }catch(Exception ex){
 					   
 				   }
 				}
 	             
 			}
 		}
 		private String getBodyText(List<Impediment> impedimentList) {
 			StringBuffer buffer = new StringBuffer();
 			for(Impediment imp : impedimentList){
				if(!imp.isClosed()){
 					buffer.append("<br>");
 					buffer.append("     Description : " + imp.getDescription()+ "<br/>");
 					Calendar cal = imp.getSubmittedDate();
 					String dateText = format.format(cal.getTime());
 					buffer.append("     Raised on : " + dateText + "<br/>");
 					User submitter = imp.getSubmitter();
 					if(submitter != null){
 						buffer.append("     Submitter : " + submitter.getUsername() + "<br/>");
 					}
 					buffer.append("</br>");
 					buffer.append("<br/>");
 					buffer.append("<br/>");
 					buffer.append("<br/>");
 				}
 			}
 			return buffer.toString();
 		}
 			
 		private User getScrumMaster(Team team) {
 			List<User> userList = userDao.findAllByTeam(team);
 			for(User user : userList){
 				if(user.scrumMaster){
 					return user;
 				}
 			}
 			return null;
 		}
     }
     private static class ImpedimentMimePreparator implements MimeMessagePreparator{
     	
     	String bodyText = "";
     	String fromAddr = "";
     	String toAddr = "";
     	String ccAdr = "";
     	
     	ImpedimentMimePreparator(String bodyText , String fromAddr , String toAddr , String ccAdr){
     		this.bodyText = bodyText;
     		this.fromAddr = fromAddr;
     		this.toAddr = toAddr;
     		this.ccAdr = ccAdr;
     	}
     	public void prepare(javax.mail.internet.MimeMessage mimeMessage) throws MessagingException {
         	try {	          
 	            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
 	            message.setFrom(fromAddr);
 	            message.setTo(toAddr);
 	           // message.setCc(ccAdr);
 	            message.setSubject("Impdiments");
 	            message.setText(bodyText, true);
         	}catch(Exception ex){}
         }
     }
 }
