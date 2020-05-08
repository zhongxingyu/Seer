 package nl.sogyo.boilerplate.web.rest;
 
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import nl.sogyo.boilerplate.domain.SavingsAccount;
 import nl.sogyo.boilerplate.domain.Todo;
 import nl.sogyo.boilerplate.service.AccountService;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 // The Java class will be hosted at the URI path "/helloworld"
 @Path("/savingsaccounts")
 @Component
 public class SavingsAccountResource {
 
 	private static final Logger logger = LoggerFactory.getLogger(SavingsAccountResource.class);
 	
 	private final AccountService accountService;
 	
     @Autowired
     public SavingsAccountResource(AccountService accountService) {
         this.accountService = accountService;
     }
 	
 	@GET // This method is called if XMLis request
 	// This will produce content identified by the MIME Media type "application/xml" or "application/json" 
 	// depending on the "Accept: {type}" in the HTTP-Header
 	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public SavingsAccount[] getSavingsAccounts() {
 		logger.debug("getSavingsAccounts()");
 		List<SavingsAccount> savingsAccounts = accountService.getSavingsAccounts();
		return savingsAccounts.toArray(new SavingsAccount[savingsAccounts.size()]);
 	}
 
 	@GET // This can be used to test the integration with the browser
	@Path("/{id}")
 	// This will produce content identified by the MIME Media type "text/xml"
 	// depending on the "Accept: {type}" in the HTTP-Header
 	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
 	public SavingsAccount getSavingsAccount(@PathParam("id") int accountId) {
 		logger.debug("getSavingsAccount()");
 		SavingsAccount savingsAccount = accountService.getSavingsAccount(accountId);
		return new SavingsAccount();
		//return savingsAccount;
 	}
 	
 	@POST // This method is called if XMLis request
 	// This will consume content identified by the MIME Media type "application/xml" or "application/json" 
 	// depending on the "Content-Type: {type}" in the HTTP-Header
 	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
 	public void createSavingsAccount(SavingsAccount savingsAccount) {
 		logger.debug("createSavingsAccount()");
 		accountService.createSavingsAccount(savingsAccount);
 	}
 	
 	// This method is called if XMLis request
 	@PUT
 	@Path("/savingsaccounts/{id}")
 	// This will consume content identified by the MIME Media type "application/xml" or "application/json" 
 	// depending on the "Accept: {type}" in the HTTP-Header	
 	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
 	public void updateSavingsAccount(@PathParam("id") int accountId, SavingsAccount savingsAccount) {
 		logger.debug("updateSavingsAccount()");
 		accountService.updateSavingsAccount(accountId, savingsAccount);
 	}
 }
