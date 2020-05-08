 package com.orangeleap.tangerine.service.communication;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.OutputStream;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.mail.MessagingException;
 import javax.mail.internet.MimeMessage;
 
 import net.sf.jasperreports.engine.JRException;
 import net.sf.jasperreports.engine.JasperExportManager;
 import net.sf.jasperreports.engine.JasperPrint;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.core.io.FileSystemResource;
 import org.springframework.mail.javamail.JavaMailSenderImpl;
 import org.springframework.mail.javamail.MimeMessageHelper;
 import org.springframework.stereotype.Service;
 
 import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.ResourceDescriptor;
 import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.ResourceProperty;
 import com.jaspersoft.jasperserver.irplugin.JServer;
 import com.jaspersoft.jasperserver.irplugin.RepositoryReportUnit;
 import com.jaspersoft.jasperserver.irplugin.wsclient.WSClient;
 import com.orangeleap.tangerine.domain.Person;
 import com.orangeleap.tangerine.domain.Site;
 import com.orangeleap.tangerine.domain.communication.Email;
 import com.orangeleap.tangerine.domain.paymentInfo.Gift;
 
 //@Service("emailSendingService")
 public class EmailService {
 	protected final Log logger = LogFactory.getLog(getClass());
 	private JServer jserver = null;
 	private String userName = null;
 	private String password = null;
 	private String uri = null;
 	private String templateName = null;
 	private String subject = null;
	
 	private java.util.Map map = new HashMap();
 	private Site site;
 	
 	private JasperPrint print;
 
 	private String runReport() {
 		File temp = null;
 		jserver = new JServer();
 		jserver.setUsername(userName);
 		jserver.setPassword(password);
 		jserver.setUrl(uri);
 		try {
 			WSClient client = jserver.getWSClient();
 
 			Map params = getReportParameters();
 
 			print = getServer().getWSClient().runReport(
 					getReportUnit().getDescriptor(), params);
 
 			temp = File.createTempFile("orangeleap", ".pdf");
 			temp.deleteOnExit();
 			OutputStream out= new FileOutputStream(temp);
 			JasperExportManager.exportReportToPdfStream(print,out);
 			out.close();
 			
 		} catch (JRException e) {
 
 			logger.error(e.getMessage());
 			return null;
 		} catch (Exception e) {
 			logger.error(e.getMessage());
 			return null;
 		}
 		return temp.getAbsolutePath();
 	}
 
 	@SuppressWarnings("unchecked")
 	private void setParameters(Person p, Gift g) 
 	{
 		Class c = p.getClass();
 		Field[] fields = c.getDeclaredFields();
 		for (Field f: fields) {
 			try {
 				map.put(f.getName(),f.get(p).toString());
 			} catch (IllegalArgumentException e) {
 				logger.error(e.getMessage());
 			} catch (IllegalAccessException e) {
 				logger.error(e.getMessage());				
 			}
 		}
 		
 		c = g.getClass();
 		fields = c.getDeclaredFields();
 		for (Field f: fields) {
 			try {
 				map.put(f.getName(),f.get(p).toString());
 			} catch (IllegalArgumentException e) {
 				logger.error(e.getMessage());
 			} catch (IllegalAccessException e) {
 				logger.error(e.getMessage());
 			}
 		}		
 	}
 	
 	public void sendMail(Person p, Gift g, String subject, String templateName) {
 
 		setSubject(subject);
 		setTemplateName(templateName);
 		
 		Site s = p.getSite();
 		setSite(s);
 		
 
 		//
 		// first we run the report passing in the person.id as a parameter
 		Map params = getReportParameters();
 		params.clear();
 
 		
 
 		params.put("Id",p.getId().toString());
 		params.put("GiftId", g.getId().toString());
 		params.put("GiftAmount", g.getAmount().toString());
 
 		
 		String tempFileName = runReport();
 
 		if (tempFileName != null) {
 		//
 		// next we extract the output of the report and put it into a mime
 		// message
 		JavaMailSenderImpl sender = new JavaMailSenderImpl();
 		sender.setHost("localhost");
 		
 		MimeMessage message = sender.createMimeMessage();
 		MimeMessageHelper helper;
 		try {
 			helper = new MimeMessageHelper(message,true,"UTF-8");
 		} catch (MessagingException e2) {
 			logger.error(e2.getMessage());
 			return;
 		}
 		List<Email> emailAddresses = p.getEmails();
 		
 		for (Email e : emailAddresses) {
 			if (e.isReceiveMail() && !e.isInactive() && e.isValid()) {
 				try {
 					helper.addTo(e.getEmailAddress());
 				} catch (MessagingException e1) {
 					logger.error(e1.getMessage());
 				}
 			}
 		}
 		
 		FileSystemResource file = new FileSystemResource(new File(tempFileName));
 		try {
 			helper.addAttachment(getTemplateName() + ".pdf",file);
 			helper.setText("Thank you for your recent donation!");
 			helper.setSubject(subject);
 			helper.setFrom("ondemaind@orangeleap.com");
 			//
 			// finally we mail the message
 			sender.send(message);
 			
 		} catch (MessagingException e1) {
 			logger.error(e1.getMessage());
 			return;
 		}
 		
 
 
 		}
 	}
 
 	private void setTemplateName(String templateName) {
 		this.templateName = templateName;
 		
 	}
 	
 	private String getTemplateName()
 	{
 		return this.templateName;
 	}
 
 	private Map getReportParameters() {
 
 		return map;
 	}
 
 	private RepositoryReportUnit getReportUnit() 
 	{
 			ResourceDescriptor rd = new ResourceDescriptor();
 			
 			rd.setName(this.getTemplateName());
 			rd.setParentFolder("/Reports/" + getSite().getName() + "/emailTemplates");
 			rd.setUriString(rd.getParentFolder() + "/" + rd.getName());
 			rd.setWsType(ResourceDescriptor.TYPE_REPORTUNIT);
 			List<ResourceProperty> p = new ArrayList<ResourceProperty>();
 			
 			Iterator it = map.entrySet().iterator();
 			while (it.hasNext()) {
 				Map.Entry me = (Map.Entry) it.next();
 			
 				ResourceProperty rp = new ResourceProperty(me.getKey().toString(),me.getValue().toString());
 				p.add(rp);
 			}
 			
 			rd.setParameters(p);
 			
 			return new RepositoryReportUnit(getServer(),rd);
 	}
 
 	private JServer getServer() {
 		return jserver;
 	}
 
 	public String getUserName() {
 		return userName;
 	}
 
 	public void setUserName(String userName) {
 		this.userName = userName;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public String getUri() {
 		return uri;
 	}
 
 	public void setUri(String uri) {
 		this.uri = uri;
 	}
 
 	public Log getLogger() {
 		return logger;
 	}
 
 	public Site getSite() {
 		return site;
 	}
 
 	public void setSite(Site site) {
 		this.site = site;
 	}
 
 	public String getSubject() {
 		return subject;
 	}
 
 	public void setSubject(String subject) {
 		this.subject = subject;
 	}
 }
