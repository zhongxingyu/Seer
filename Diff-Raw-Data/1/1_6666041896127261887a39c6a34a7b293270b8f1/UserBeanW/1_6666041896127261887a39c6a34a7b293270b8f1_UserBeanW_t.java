 package eai.msejdf.user;
 
 import java.util.List;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 
 import eai.msejdf.session.SessionManager;
 import eai.msejdf.exception.ConfigurationException;
 import eai.msejdf.persistence.Address;
 import eai.msejdf.persistence.BankTeller;
 import eai.msejdf.persistence.Company;
 import eai.msejdf.persistence.User;
 
 @ManagedBean
 @ViewScoped
 public class UserBeanW {
 	private IUserBean bean;
 	private User user;
 	// private BankTeller bankTeller;
 	private String birthDate;
 	List<Company> followedCompanyList;
 	List<Company> companyList;
 	List<BankTeller> bankTellerList;
 	
 	public UserBeanW() throws NamingException, ConfigurationException
 	{
 		InitialContext ctx = new InitialContext();
 
 		// TODO: Remove hardcoded
 		this.bean = (IUserBean) ctx
 				.lookup("ejb:P2EARDeploy/EJBServer/UserBean!eai.msejdf.user.IUserBean");
 
 		// TODO
 		this.user = bean.getUser(SessionManager
 				.getProperty(SessionManager.USERNAME_PROPERTY));
 	}
 
 	public IUserBean bean() {
 		return this.bean;
 	}
 
 	public User getUser() {
 		return user;
 	}
 
 	public void setUser(User user) {
 		this.user = user;
 	}
 
 	public BankTeller getBankTeller(User user) {
 		BankTeller bankTeller;
 
 		try {
 			bankTeller = this.bean.getUserBankTeller(user);
 		} catch (ConfigurationException e) {
 			bankTeller = new BankTeller();
 			e.printStackTrace();
 		}
 
 		return bankTeller;
 	}
 
 	public void setBankTeller(BankTeller bankTeller) {
 		this.user.setBankTeller(bankTeller);
 	}
 
 	public boolean updateUser() {
 		// TODO: The exception should cause the user interface to be informed
 		try {
 			this.bean.updateUser(this.user);
 		} catch (Exception exception) {
 			exception.printStackTrace();
 			return false;
 		}
 
 		return true;
 	}
 
 	public boolean updateBankTeller() {
 		// TODO: The exception should cause the user interface to be informed
 		try {
 			this.bean.setBankTeller(this.user.getId(),
 					this.user.getBankTeller());
 		} catch (Exception exception) {
 			exception.printStackTrace();
 			return false;
 		}
 
 		return true;
 	}
 
 	public String getBirthDate() {
 		return birthDate;
 	}
 
 	public void setBirthDate(String birthDate) {
 		this.birthDate = birthDate;
 		// this.user.setBirthDate(new Date(birthDate));
 	}
 	
 	public List<Company> getFollowedCompanyList() throws ConfigurationException
 	{
 		if (FacesContext.getCurrentInstance().getRenderResponse()) {
 			this.followedCompanyList = this.bean.getfollowedCompanyList(user.getId()); // Reload to get most recent data.
         }		
 		return this.followedCompanyList;		
 	}
 	
 	public List<Company> getCompanyList() throws ConfigurationException
 	{
 		if (FacesContext.getCurrentInstance().getRenderResponse()) {
 			this.companyList = this.bean.getCompanyList("%"); // Reload to get most recent data.
         }		
 		return this.companyList;		
 	}
 	public List<BankTeller> getBankTellerList() throws ConfigurationException
 	{
 		if (FacesContext.getCurrentInstance().getRenderResponse()) {
 			this.bankTellerList = this.bean.getBankTellerList("%"); // Reload to get most recent data.
         }		
 		return this.bankTellerList;		
 	}
 	
 	public BankTeller getUserBankTeller() throws ConfigurationException
 	{
 		return this.bean.getUserBankTeller(user);
 	}
 
 	public String getBankTellerAddress() throws ConfigurationException {
 		return this.user.getBankTeller().getAddress().getAddress();
 	}
 
 	public void setBankTellerAddress(Address address)
 			throws ConfigurationException {
 		this.user.getBankTeller().setAddress(address);
 	}
 
 }
