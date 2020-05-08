 package localdomain.localhost;
 
 import java.security.MessageDigest;
 
 import javax.ejb.Stateless;
 import javax.faces.context.FacesContext;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.mail.DefaultAuthenticator;
 import org.apache.commons.mail.Email;
 
 
 import org.apache.commons.mail.EmailException;
 import org.apache.commons.mail.SimpleEmail;
 
 import sun.misc.BASE64Encoder;
 
 
 
 @Stateless
 public class RegistrationService {
 
 	@PersistenceContext()
 	EntityManager em;
 	
 	public void register(User user){
 		
 		em.persist(user);
 		em.flush();
 	}
 	
 	public void sendRegistrationMail(User user)
 	{
 		HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
 		String url = req.getRequestURL().toString();
 		url=url.substring(0, url.lastIndexOf("/"));
 		Email email = new SimpleEmail();
 		email.setHostName("smtp.1und1.de");
 		email.setSmtpPort(465);
 		email.setAuthenticator(new DefaultAuthenticator("macherey@vc-solution.de", "ValeriaS"));
 		email.setSSLOnConnect(true);
 		try {
 		email.setFrom("macherey@vc-solution.de");
 		email.setSubject("CafePEP Account Verification ");
		email.setMsg(url+"/activation.xhtml?key="+user.getVerification_key().replace("+", "%2B"));
 		email.addTo(user.getEmail());
 		email.send();
 		} catch (EmailException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
 
