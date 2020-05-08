 package eai.msejdf.pagedata;
 
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 
 import eai.msejdf.exception.ConfigurationException;
 import eai.msejdf.persistence.User;
 import eai.msejdf.user.IUserBean;
 import eai.msejdf.utils.EJBLookupConstants;
 import eai.msejdf.web.session.SessionManager;
 
 public class UserPageData{
 	
 	private IUserBean userBean;
 	private User user;
	
 	public UserPageData() throws NamingException, ConfigurationException {
 		InitialContext ctx = new InitialContext();
 		
 		this.userBean = (IUserBean) ctx.lookup(EJBLookupConstants.EJB_I_USER);
 		
 		this.user = this.userBean.getUser(SessionManager.getProperty(SessionManager.USERNAME_PROPERTY));
 	}
 
 	public User getUser() {
 		return user;
 	}
 
 	public void setUser(User user) {
 		this.user = user;
 	}
 
 	public boolean updateUser() {
		// TODO: The exception should cause the user interface to be informed
 		try {
 			this.userBean.updateUser(this.user);
 		} catch (Exception exception) {
 			exception.printStackTrace();
 			return false;
 		}

 		return true;
 	}
 
	
 }
