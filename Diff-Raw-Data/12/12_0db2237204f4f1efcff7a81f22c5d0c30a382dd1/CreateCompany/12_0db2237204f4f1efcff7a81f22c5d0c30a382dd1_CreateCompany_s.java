// $Id: CreateCompany.java,v 1.1 2000/06/13 16:00:07 locher Exp $
 package jobmatch.presentation.admin;
 
 import com.lutris.xml.xmlc.*;
 import jobmatch.presentation.*;
 import com.lutris.appserver.server.httpPresentation.*;
 
 import jobmatch.business.provider.account.*;
 
 /**
  * Creates a new Company Account
  * @since June 13 2000
  * @author $Author: locher $
 * @version $Revision: 1.1 $
  **/
 public class CreateCompany extends AuthentificationPage implements HttpPresentation {
 
     public void run(HttpPresentationComms comms) 
         throws HttpPresentationException {
 	
 	this.assertLegitimation(comms, Account.TYPE_PROVIDER);
 	final String action = comms.request.getParameter("action");
 	if (action != null && action.equals("create")) {
 	    // create a new account and then go to the administrator home
 	    final String username = comms.request.getParameter("username");
 	    final String passphrase = comms.request.getParameter("passphrase");
 	    final String confirm = comms.request.getParameter("passphraseConfirm");
 	    final String email = comms.request.getParameter("eMail");
 	  
 	    if (passphrase != null && passphrase.equals(confirm)) {
 		this.createAccount(username, passphrase, email);
		final String loginURL = "./ControlCenter.po";
 		throw new ClientPageRedirectException(
 						      comms.request.getAppFileURIPath(loginURL));
 	    }
 
 	    else {
 		CreateCompanyHTML page = (CreateCompanyHTML)comms.xmlcFactory.create(CreateCompanyHTML.class);
 		page.setTextMessage("confirmation failed");
 		comms.response.writeHTML(page);
 	    }
 	} else {
 	    // Simply show the page
 	    CreateCompanyHTML page = (CreateCompanyHTML)comms.xmlcFactory.create(CreateCompanyHTML.class);
 	    page.getElementMessageDiv().removeChild(page.getElementMessageContainer());
 	    comms.response.writeHTML(page);
 	}
     }
 
     /**
      * Creates a new Companyaccount 
      **/
     private void createAccount(String username, String passphrase, String email) {
 	if (!AccountManager.getUniqueInstance().candidateUsernameExists(username)) {
 	    try {
 		AccountManager.getUniqueInstance().createCompanyAccount(username, 
 									  passphrase, 
 									  email);
 	    } catch (Exception e) {
 		throw new RuntimeException("creation failed");
 	    }
 	} else {
 	    throw new  RuntimeException("username already exists");
 	}
     }
 
 } // class
 
 // Document history
 /**
  * $Log: CreateCompany.java,v $
  * Revision 1.1  2000/06/13 16:00:07  locher
  * moved CreateCompany to the right place
  *
  * Revision 1.1  2000/06/13 10:12:56  dniederm
  * Firmen koennen erstellt werden
  *
  *
  **/
