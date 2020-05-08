 package no.niths.application.rest;
 
 import java.util.ArrayList;
 
 import no.niths.application.rest.exception.ObjectNotFoundException;
 import no.niths.application.rest.interfaces.ApplicationController;
 import no.niths.application.rest.lists.ApplicationList;
 import no.niths.application.rest.lists.ListAdapter;
 import no.niths.common.AppConstants;
 import no.niths.common.SecurityConstants;
 import no.niths.common.ValidationHelper;
 import no.niths.domain.Application;
 import no.niths.services.interfaces.ApplicationService;
 import no.niths.services.interfaces.GenericService;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 /**
  * Controller for handling applications
  * 
  */
 @Controller
 @RequestMapping(AppConstants.APPLICATIONS)
 public class ApplicationControllerImpl extends
 		AbstractRESTControllerImpl<Application> implements
 		ApplicationController {
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(ApplicationControllerImpl.class);
 
 	@Autowired
 	private ApplicationService service;
 
 	private ApplicationList applicationList = new ApplicationList();
 
 	@Override
 	public ArrayList<Application> getAll(Application domain) {
 		applicationList = (ApplicationList) super.getAll(domain);
 		return applicationList;
 	}
 
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR)
 	public void create(@RequestBody Application domain) {
 		super.create(domain);
 	}
 
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR)
 	public void hibernateDelete(@PathVariable long id) {
 		super.hibernateDelete(id);
 	}
 
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR)
 	public void update(@RequestBody Application domain) {
 		super.update(domain);
 	}
 
 	/**
 	 * Enables an application
 	 * <p>
 	 * Applications must be enabled to do request
 	 * <p>
 	 * 
 	 * @param applicationId
 	 *            id of the application
 	 * @throws ObjectNotFoundException
 	 *             if no application is found
 	 */
 	@Override
 	@PreAuthorize(SecurityConstants.ADMIN_AND_SR)
 	@RequestMapping(value = { "enable/{applicationId}" }, method = RequestMethod.PUT)
 	@ResponseStatus(value = HttpStatus.OK, reason = "Application enabled")
 	public void enableApplication(@PathVariable Long applicationId) {
 		Application app = service.getById(applicationId);
 		ValidationHelper.isObjectNull(app, "Application not found");
 		boolean update = false;
 		if (app.getEnabled() != null) {
 			if (!app.getEnabled()) {
 				update = true;
 			}
 		} else {
 			update = true;
 		}
 
 		if (update) {
 			app.setEnabled(true);
 			service.update(app);
 			logger.debug("application is enabled");
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public GenericService<Application> getService() {
 		return service;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public ListAdapter<Application> getList() {
 		return applicationList;
 	}
 
 }
