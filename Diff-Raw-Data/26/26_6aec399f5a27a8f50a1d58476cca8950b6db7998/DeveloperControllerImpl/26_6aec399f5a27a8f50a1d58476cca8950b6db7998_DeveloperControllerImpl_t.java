 package no.niths.application.rest;
 
 import java.util.ArrayList;
 
 import no.niths.application.rest.exception.UnvalidEmailException;
 import no.niths.application.rest.interfaces.DeveloperController;
 import no.niths.application.rest.lists.DeveloperList;
 import no.niths.application.rest.lists.ListAdapter;
 import no.niths.common.AppConstants;
 import no.niths.common.SecurityConstants;
 import no.niths.domain.Developer;
 import no.niths.services.interfaces.DeveloperService;
 import no.niths.services.interfaces.GenericService;
 import no.niths.services.interfaces.MailSenderService;
 
 import org.apache.commons.validator.routines.EmailValidator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 /**
  * Controller for handling developers and their applications
  * 
  */
 @Controller
 @RequestMapping(AppConstants.DEVELOPERS)
 public class DeveloperControllerImpl extends
 		AbstractRESTControllerImpl<Developer> implements DeveloperController {
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(DeveloperControllerImpl.class);
 
 	@Autowired
 	private DeveloperService service;
 
 	private DeveloperList developerList = new DeveloperList();
 
 	@Autowired
 	private MailSenderService mailService;
 
 	/**
	 * Register a developer!
	 * 
	 * Check if the email is valid, then send the developer a email confirmation
 	 * 
 	 * {@inheritDoc}
 	 */
 	@Override
	@RequestMapping(value = { "/gimme/" }, method = RequestMethod.POST, headers = RESTConstants.ACCEPT_HEADER)
 	@ResponseBody
 	public void requestAccess(@RequestBody Developer domain) {
 		logger.debug("A developer requests access! Email: " + domain.getEmail());
 
 		EmailValidator validator = EmailValidator.getInstance();
 		if (!validator.isValid(domain.getEmail())) {
 			throw new UnvalidEmailException("Email is not valid");
 		}
 		//Valid email, persist the domain
 		create(domain);
		logger.debug("Request success, sending email");
 		//Send confirmation to developer
		//mailService.composeAndSend(domain.getEmail());
 		
 		// generere token
 		// opprette en developer med den token -->Flere parametre enn email?
 		// sette boolean devEnabled til false (Developer har en bool enabled
 		// kanskje)
 		// sende token til dev på mail
 
 		// Dev mottar denne på mail
 		// sende token tilbake til APIet
 		// verifisere tokenen
 		// sette dev til enabled
 		// Sende mail til dev med du er enabled og ny token
 
 	}
 
 	@Override
 	public ArrayList<Developer> getAll(Developer domain) {
 		developerList = (DeveloperList) super.getAll(domain);
 		for (Developer d : developerList) {
 			d.setApps(null);
 		}
 		return developerList;
 	}
 
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR)
 	public void create(@RequestBody Developer domain) {
 		super.create(domain);
 	}
 
 	@SuppressWarnings("deprecation")
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR)
 	public void delete(@PathVariable Long id) {
 		// TODO Auto-generated method stub
 		super.delete(id);
 	}
 
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR)
 	public void hibernateDelete(@PathVariable long id) {
 		// TODO Auto-generated method stub
 		super.hibernateDelete(id);
 	}
 
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR)
 	public void update(@RequestBody Developer domain) {
 		// TODO Auto-generated method stub
 		super.update(domain);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public GenericService<Developer> getService() {
 		return service;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public ListAdapter<Developer> getList() {
 		return developerList;
 	}
 
 }
