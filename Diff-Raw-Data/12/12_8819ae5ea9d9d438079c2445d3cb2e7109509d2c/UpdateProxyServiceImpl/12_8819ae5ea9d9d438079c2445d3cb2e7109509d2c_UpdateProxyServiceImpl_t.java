 package com.scholastic.sbam.server.servlets;
 
 import java.util.Date;
 import java.util.List;
 
 import com.scholastic.sbam.client.services.UpdateProxyService;
 import com.scholastic.sbam.server.database.codegen.Proxy;
 import com.scholastic.sbam.server.database.objects.DbProxy;
 import com.scholastic.sbam.server.database.util.HibernateUtil;
 import com.scholastic.sbam.server.validation.AppProxyValidator;
 import com.scholastic.sbam.shared.objects.UpdateResponse;
 import com.scholastic.sbam.shared.objects.ProxyInstance;
 import com.scholastic.sbam.shared.security.SecurityManager;
 import com.scholastic.sbam.shared.util.AppConstants;
 
 /**
  * The server side implementation of the RPC service.
  */
 @SuppressWarnings("serial")
 public class UpdateProxyServiceImpl extends AuthenticatedServiceServlet implements UpdateProxyService {
 
 	@Override
 	public UpdateResponse<ProxyInstance> updateProxy(ProxyInstance instance) throws IllegalArgumentException {
 		
 		boolean newCreated				= false;
 		
 		String	messages				= null;
 		
 		Proxy dbInstance = null;
 		
 		authenticate("update proxy", SecurityManager.ROLE_MAINT);
 		
 		HibernateUtil.openSession();
 		HibernateUtil.startTransaction();
 
 		try {
 			
 			//	Pre-edit/fix values
 			validateInput(instance);
 			
 			//	Get existing, or create new
 			if (instance.getProxyId() > 0 && !instance.isNewRecord()) {
 				dbInstance = DbProxy.getById(instance.getProxyId());
 			}
 
 			//	If none found, create new
 			if (dbInstance == null) {
 				newCreated = true;
 				dbInstance = new Proxy();
 				//	Set the create date/time and status
 				dbInstance.setCreatedDatetime(new Date());
 			}
 
 			//	Update values
 
 			if (instance.getDescription() != null)
 				dbInstance.setDescription(instance.getDescription());
 			if (instance.getSearchKeys() != null)
 				dbInstance.setSearchKeys(instance.getSearchKeys());
 			if (instance.getStatus() != 0)
 				dbInstance.setStatus(instance.getStatus());
 			if (instance.getNote() != null) {
 				if("<br>".equals(instance.getNote()))
 					instance.setNote("");
 				dbInstance.setNote(instance.getNote());
 			}
 				
 			//	Fix any nulls
 			if (instance.getDescription() == null)
 				dbInstance.setDescription("Missing Description");
 			if (instance.getSearchKeys() == null)
 				dbInstance.setSearchKeys("");
 			if (dbInstance.getStatus() == AppConstants.STATUS_ANY_NONE)
 				dbInstance.setStatus(AppConstants.STATUS_ACTIVE);
 			if (dbInstance.getNote() == null)
 				dbInstance.setNote("");
 			
 			//	Persist in database
 			DbProxy.persist(dbInstance);
 			
 			//	Refresh when new row is created, to get assigned ID
 			if (newCreated) {
 			//	DbProxy.refresh(dbInstance);	// This may not be necessary, but just in case
 				instance.setProxyId(dbInstance.getProxyId());
 				instance.setCreatedDatetime(dbInstance.getCreatedDatetime());
				
				//	Need to set the check digit version, and update
				dbInstance.setIdCheckDigit(instance.getProxyIdCheckDigit());
				DbProxy.persist(dbInstance);
 			}
 			
 		} catch (IllegalArgumentException exc) {
 			silentRollback();
 			throw exc;
 		} catch (Exception exc) {
 			silentRollback();
 			exc.printStackTrace();
 			throw new IllegalArgumentException("The proxy update failed unexpectedly.");
 		} finally {
 			if (HibernateUtil.isTransactionInProgress())
 				HibernateUtil.endTransaction();
 			HibernateUtil.closeSession();
 		}
 		
 		return new UpdateResponse<ProxyInstance>(instance, messages);
 	}
 	
 	private void validateInput(ProxyInstance instance) throws IllegalArgumentException {
 		AppProxyValidator validator = new AppProxyValidator();
 		validator.setOriginal(instance);	//	This isn't really the original, but it's good enough, because it has the original ID
 		testMessages(validator.validateProxy(instance));
 	}
 	
 	private void testMessages(List<String> messages) throws IllegalArgumentException {
 		if (messages != null)
 			for (String message: messages)
 				testMessage(message);
 	}
 	
 	private void testMessage(String message) throws IllegalArgumentException {
 		if (message != null && message.length() > 0)
 			throw new IllegalArgumentException(message);
 	}
 	
 	private void silentRollback() {
 		try {
 			if (HibernateUtil.isTransactionInProgress())
 				HibernateUtil.getSession().getTransaction().rollback();	
 		} catch (Exception exc) { }
 	}
 }
