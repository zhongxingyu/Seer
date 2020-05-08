 package de.fhb.autobday.manager.account;
 
 import de.fhb.autobday.commons.CipherHelper;
 import de.fhb.autobday.commons.EMailValidator;
 import de.fhb.autobday.commons.PropertyLoader;
 import de.fhb.autobday.dao.AbdAccountFacade;
 import de.fhb.autobday.dao.AbdContactFacade;
 import de.fhb.autobday.dao.AbdUserFacade;
 import de.fhb.autobday.data.*;
 import de.fhb.autobday.exception.account.AccountAlreadyExsistsException;
 import de.fhb.autobday.exception.account.AccountNotFoundException;
 import de.fhb.autobday.exception.connector.ConnectorCouldNotLoginException;
 import de.fhb.autobday.exception.connector.ConnectorInvalidAccountException;
 import de.fhb.autobday.exception.connector.ConnectorNoConnectionException;
 import de.fhb.autobday.exception.user.NoValidUserNameException;
 import de.fhb.autobday.exception.user.UserNotFoundException;
 import de.fhb.autobday.manager.LoggerInterceptor;
 import de.fhb.autobday.manager.connector.google.GoogleImporter;
 import java.io.IOException;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.crypto.BadPaddingException;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 import javax.ejb.EJB;
 import javax.ejb.Local;
 import javax.ejb.Stateless;
 import javax.interceptor.Interceptors;
 
 /**
  * The AccountManager processes all accountData specific things.
  *
  * @author 
  * Andy Klay <klay@fh-brandenburg.de>
  * Michael Koppen <koppen@fh-brandenburg.de>
  * 
  */
 @Stateless
 @Local
 @Interceptors(LoggerInterceptor.class)
 public class AccountManager implements AccountManagerLocal {
 	
 	private final static Logger LOGGER = Logger.getLogger(AccountManager.class.getName());
 	
 	@EJB
 	private AbdAccountFacade accountDAO;
 	
 	@EJB
 	private AbdContactFacade contactDAO;
 	
 	@EJB
 	private AbdUserFacade userDAO;
 	
 	@EJB
 	private GoogleImporter importer;
 	
 	private PropertyLoader propLoader;
 	
 	
 	public AccountManager() {
 		propLoader = new PropertyLoader();
 	}
 
 	
 	/**
 	 * (non-Javadoc)
 	 * @throws AccountAlreadyExsistsException 
 	 * @throws NoValidUserNameException 
 	 * @see de.fhb.autobday.manager.account.AccountManagerLocal#addAccount(int, java.lang.String, java.lang.String, java.lang.String)
 	 */
 	@Override
 	public AbdAccount addAccount(int abdUserId, String password, String userName, String type) 
 			throws UserNotFoundException, AccountAlreadyExsistsException, NoValidUserNameException {
 		
 		AbdUser actualUser=null;
 		String passwordChipher="";
 		Properties masterPassword;
 		
 		//search User
 		actualUser=userDAO.find(abdUserId);
 		
 		//if account not found
 		if(actualUser==null){
 			LOGGER.log(Level.SEVERE, "User {0} not found!", abdUserId);
 			throw new UserNotFoundException("User " + abdUserId + " not found!");
 		}
 		
 		//search if account already exists
 		for( AbdAccount actAccount : actualUser.getAbdAccountCollection()){
 			if(actAccount.getUsername().equals(userName)&&actAccount.getType().equals(type)){
 				LOGGER.log(Level.SEVERE, "Account already exists!");
 				throw new AccountAlreadyExsistsException("Account already exists!");
 			}
 		}
 		
 		//check if userName is mailaddress
 		if(!EMailValidator.isGoogleMail(userName)){
 			LOGGER.log(Level.SEVERE, "UserName is no GoogleMail-address!");
 			throw new NoValidUserNameException("UserName is no GoogleMail-address!");
 		}
 		
 		try {
 			masterPassword = propLoader.loadSystemchiperPasswordProperty("/SystemChiperPassword.properties");
 			passwordChipher = CipherHelper.cipher(password, masterPassword.getProperty("master"));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvalidKeyException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchPaddingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalBlockSizeException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (BadPaddingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		//add new Account
 		AbdAccount createdAccount=new AbdAccount();	
 		createdAccount.setId(Integer.SIZE);
 		createdAccount.setAbduser(actualUser);		
 		createdAccount.setPasswort(passwordChipher);
 		createdAccount.setUsername(userName);
 		createdAccount.setType("google");
 
 		//create and save into db
 		accountDAO.create(createdAccount);
 		userDAO.refresh(actualUser);
 		
 		return createdAccount;
 	}
 
 	/**
 	 * (non-Javadoc)
 	 * @see de.fhb.autobday.manager.account.AccountManagerLocal#removeAccount(de.fhb.autobday.data.AbdAccount)
 	 */
 	@Override
 	public void removeAccount(AbdAccount account) 
 			throws AccountNotFoundException{
 		
 		removeAccount(account.getId());
 	}
 	
 	/**
 	 * (non-Javadoc)
 	 * @see de.fhb.autobday.manager.account.AccountManagerLocal#removeAccount(int)
 	 */
 	@Override
 	public void removeAccount(int accountId) 
 			throws AccountNotFoundException {
 		
 		AbdAccount account=null;
 		ArrayList<AbdContact> contactList = new ArrayList<AbdContact>();
 		
 		//search
 		account=accountDAO.find(accountId);
 		
 		//if account not found
 		if(account==null){
 			LOGGER.log(Level.SEVERE, "Account {0} not found!", accountId);
 			throw new AccountNotFoundException("Account " + accountId + " not found!");
 		}
 		//notice the contacts of this account
 		for (AbdGroup group : account.getAbdGroupCollection()) {
 			for (AbdGroupToContact gtc : group.getAbdGroupToContactCollection()) {
 				if (!contactList.contains(gtc.getAbdContact())) {
 					contactList.add(gtc.getAbdContact());
 				}
 				
 			}	
 		}
 		//delete acccount itself
 		accountDAO.remove(account);
 		
 		for (AbdContact contact : contactList) {
 			contactDAO.edit(contact);
 			contactDAO.remove(contact);
 		}
 		//and remove addtionally the contacts
 		
 		
 		
 	}
 
 	/**
 	 * (non-Javadoc)
 	 * @throws AccountNotFoundException 
 	 * @throws ConnectorNoConnectionException 
 	 * @throws NoConnectionException 
 	 * @throws ConnectorInvalidAccountException 
 	 * @throws ConnectorCouldNotLoginException 
 	 * @see de.fhb.autobday.manager.account.AccountManagerLocal#importGroupsAndContacts(int)
 	 */
 	@Override
 	public void importGroupsAndContacts(int accountId) 
 			throws AccountNotFoundException, ConnectorCouldNotLoginException, ConnectorInvalidAccountException, ConnectorNoConnectionException{
 		
 		
 		AbdAccount account=null;
 		
 		//search
 		account=accountDAO.find(accountId);
 		
 		//if account not found
 		if(account==null){
 			LOGGER.log(Level.SEVERE, "Account {0} not found!", accountId);
 			throw new AccountNotFoundException("Account " + accountId + " not found!");
 		}
 		
 		
 		//connect and import
 		importer.getConnection(account);
 		
 		importer.importContacts();
 		
 		accountDAO.refresh(account);
 	}
 	
 	/**
 	 * (non-Javadoc)
 	 * @see de.fhb.autobday.manager.account.AccountManagerLocal#getAllGroupsFromAccount(de.fhb.autobday.data.AbdAccount)
 	 */
 	@Override
 	public List<AbdGroup> getAllGroupsFromAccount(AbdAccount account) 
 			throws AccountNotFoundException{
 		return getAllGroupsFromAccount(account.getId());
 	}
 	
 	
 	/**
 	 * (non-Javadoc)
 	 * @see de.fhb.autobday.manager.account.AccountManagerLocal#getAllGroupsFromAccount(int)
 	 */
 	@Override
 	public List<AbdGroup> getAllGroupsFromAccount(int accountId) 
 			throws AccountNotFoundException{
 		
 		
 		AbdAccount account=null;
 		ArrayList<AbdGroup> outputCollection=new ArrayList<AbdGroup>();
 		
 		//find object, verify input
 		account=accountDAO.find(accountId);
 		
 		if(account==null){
 			LOGGER.log(Level.SEVERE, "Account does not exist!");
 			throw new AccountNotFoundException("Account does not exist!");
 		}
 		
 		outputCollection=new ArrayList<AbdGroup>(account.getAbdGroupCollection());
 		
 		return outputCollection;
 	}

	public PropertyLoader getPropLoader() {
		return propLoader;
	}

	public void setPropLoader(PropertyLoader propLoader) {
		this.propLoader = propLoader;
	}
 	
 }
