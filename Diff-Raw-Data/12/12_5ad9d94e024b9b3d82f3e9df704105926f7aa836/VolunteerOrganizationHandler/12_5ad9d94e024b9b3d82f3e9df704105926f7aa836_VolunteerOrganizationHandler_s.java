 package com.idega.volunteer.handler;
 
 import is.idega.idegaweb.egov.cases.business.CasesBusiness;
 
 import java.rmi.RemoteException;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.logging.Level;
 
 import javax.ejb.CreateException;
 import javax.ejb.FinderException;
 import javax.mail.MessagingException;
 
 import org.jbpm.graph.def.ActionHandler;
 import org.jbpm.graph.exe.ExecutionContext;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.config.BeanDefinition;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 
 import com.idega.block.process.business.CaseBusiness;
 import com.idega.block.process.data.Case;
 import com.idega.company.business.CompanyBusiness;
 import com.idega.company.data.Company;
 import com.idega.company.data.CompanyType;
 import com.idega.company.data.CompanyTypeHome;
 import com.idega.company.handler.CreateCompanyHandler;
 import com.idega.core.accesscontrol.business.LoginDBHandler;
 import com.idega.core.accesscontrol.data.LoginInfo;
 import com.idega.core.business.DefaultSpringBean;
 import com.idega.core.contact.data.Email;
 import com.idega.core.contact.data.EmailHome;
 import com.idega.core.contact.data.EmailType;
 import com.idega.core.contact.data.EmailTypeHome;
 import com.idega.core.contact.data.Phone;
 import com.idega.core.contact.data.PhoneHome;
 import com.idega.core.contact.data.PhoneType;
 import com.idega.core.location.data.Address;
 import com.idega.core.location.data.AddressHome;
 import com.idega.core.location.data.AddressType;
 import com.idega.core.location.data.AddressTypeHome;
 import com.idega.core.location.data.Commune;
 import com.idega.core.location.data.CommuneHome;
 import com.idega.core.messaging.MessagingSettings;
 import com.idega.data.IDOAddRelationshipException;
 import com.idega.data.IDOLookup;
 import com.idega.data.IDOLookupException;
 import com.idega.data.IDORemoveRelationshipException;
 import com.idega.idegaweb.IWResourceBundle;
 import com.idega.idegaweb.egov.bpm.data.CaseProcInstBind;
 import com.idega.idegaweb.egov.bpm.data.dao.CasesBPMDAO;
 import com.idega.jbpm.identity.UserPersonalData;
 import com.idega.jbpm.identity.authentication.CreateUserHandler;
 import com.idega.user.business.UserBusiness;
 import com.idega.user.data.User;
 import com.idega.util.CoreUtil;
 import com.idega.util.ListUtil;
 import com.idega.util.SendMail;
 import com.idega.util.SendMailMessageValue;
 import com.idega.util.StringHandler;
 import com.idega.util.StringUtil;
 import com.idega.util.expression.ELUtil;
 import com.idega.volunteer.VolunteerConstants;
 import com.idega.volunteer.business.VolunteerServices;
 
 @Scope(BeanDefinition.SCOPE_PROTOTYPE)
 @Service("volunteerOrganizationHandler")
 public class VolunteerOrganizationHandler extends DefaultSpringBean implements ActionHandler {
 
 	private static final long serialVersionUID = 6422874982048806524L;
 
 	private Long processInstanceId;
 	
 	private boolean userUpdated = false;
 	
 	@Autowired
 	private CasesBPMDAO casesDAO;
 	
 	@Autowired
 	private VolunteerServices volunteerServices;
 	
 	private CasesBPMDAO getCasesDAO() {
 		if (casesDAO == null)
 			ELUtil.getInstance().autowire(this);
 		return casesDAO;
 	}
 	
 	private VolunteerServices getVolunteerServices() {
 		if (volunteerServices == null)
 			ELUtil.getInstance().autowire(this);
 		return volunteerServices;
 	}
 	
 	private CreateUserHandler getUserHandler() {
 		CreateUserHandler userHandler = ELUtil.getInstance().getBean(CreateUserHandler.BEAN_NAME);
 		return userHandler;
 	}
 	
 	private CreateCompanyHandler getCompanyHandler() {
 		CreateCompanyHandler companyHandler = ELUtil.getInstance().getBean(CreateCompanyHandler.BEAN_NAME);
 		return companyHandler;
 	}
 	
 	public void execute(ExecutionContext executionContext) throws Exception {
 		CaseProcInstBind caseProcBind = getCasesDAO().getCaseProcInstBindByProcessInstanceId(getProcessInstanceId());
 		CaseBusiness caseBusiness = getServiceInstance(CasesBusiness.class);
 		Case theCase = caseBusiness.getCase(caseProcBind.getCaseId());
 		
 		Object registeredVar = executionContext.getVariable("string_volunteerOrganizationRegistered");
 		if (registeredVar == null || Boolean.valueOf(registeredVar.toString())) {
 			createOrEnableOrganization(executionContext, theCase);
 		} else if (registeredVar != null && !Boolean.valueOf(registeredVar.toString())) {
 			disableOrganization(executionContext, theCase);
 		} else {
 			getLogger().warning("Uncertain situation - not clear if organization should be created/enabled or disabled");
 		}
 	}
 	
 	private String getCompanyId(ExecutionContext context) {
 		Object companyID = context.getVariable("string_volunteerOrganizationIdNumber");
 		if (!(companyID instanceof String))
 			return null;
 		
 		return companyID.toString();
 	}
 	
 	private Company getCompany(ExecutionContext context) {
 		String companyID = getCompanyId(context);
 		if (StringUtil.isEmpty(companyID))
 			return null;
 		
 		Company company = null;
 		CompanyBusiness companyBusiness = getServiceInstance(CompanyBusiness.class);
 		try {
 			company = companyBusiness.getCompany(companyID);
 		} catch (RemoteException e) {
 			getLogger().log(Level.WARNING, "Error getting company by ID: " + companyID, e);
 		} catch (FinderException e) {}
 
 		if (company == null) {
 			try {
 				return createCompany(context);
 			} catch (Exception e) {
 				getLogger().log(Level.WARNING, "Error creating company by ID: " + companyID, e);
 			}
 		} else {
 			try {
 				updateCompany(company, context);
 			} catch (Exception e) {
 				getLogger().log(Level.WARNING, "Error updating company: " + company, e);
 			}
 		}
 		
 		return company;
 	}
 	
 	private Company createCompany(ExecutionContext context) throws IDOLookupException, CreateException, RemoteException, FinderException {
 		CompanyBusiness companyBusiness = getServiceInstance(CompanyBusiness.class);
 		String name = context.getVariable("string_caseDescription").toString();
 		String companyId = getCompanyId(context);
 		Company company = null;
 		try {
 			company = companyBusiness.storeCompany(name, companyId);
 		} catch (Exception e) {
 			getLogger().log(Level.WARNING, "Error creating company by name: " + name + " and ID: " + companyId, e);
 		}
 		if (company == null)
 			return null;
 		
 		updateCompany(company, context);
 		return company;
 	}
 	
 	private void updateCompany(Company company, ExecutionContext context) throws IDOLookupException, CreateException, RemoteException, FinderException {
 		Object phoneNumber = context.getVariable("string_volunteerOrganizationTelephoneNumber");
 		if (phoneNumber != null) {
 			Phone phone = ((PhoneHome) IDOLookup.getHome(Phone.class)).create();
 			phone.setNumber(phoneNumber.toString());
 			phone.setPhoneTypeId(PhoneType.WORK_PHONE_ID);
 			phone.store();
 			company.updatePhone(phone);
 		}
 		
 		Object address = context.getVariable("string_volunteerOrganizationAddress");
 		if (address != null) {
 			Address compAddress = ((AddressHome) IDOLookup.getHome(Address.class)).create();
 			compAddress.setStreetName(address.toString());
 			
 			Object city = context.getVariable("string_volunteerOrganizationCity");
 			if (city != null)
 				compAddress.setCity(city.toString());
 			
 			Object postalCode = context.getVariable("string_volunteerOrganizationPostalCode");
 			if (postalCode != null)
 				compAddress.setPOBox(postalCode.toString());
 
 			compAddress.setAddressType(((AddressTypeHome) IDOLookup.getHome(AddressType.class)).findAddressType1());
 			compAddress.store();
 			company.setAddress(compAddress);
 		}
 		
 		Object email = context.getVariable("string_volunteerOrganizationEmail");
 		if (email != null) {
 			Email compEmail = ((EmailHome) IDOLookup.getHome(Email.class)).create();
 			compEmail.setEmailAddress(email.toString());
 			compEmail.setEmailType(((EmailTypeHome) IDOLookup.getHome(EmailType.class)).findMainEmailType());
 			compEmail.store();
 			company.updateEmail(compEmail);
 		}
 		
 		Object type = context.getVariable("string_volunteerOrganizationType");
 		if (type != null) {
 			CompanyTypeHome companyTypeHome = (CompanyTypeHome) IDOLookup.getHome(CompanyType.class);
 			CompanyType companyType = company.getType();
 			if (companyType == null) {
 				Collection<CompanyType> types = companyTypeHome.findAll();
 				if (!ListUtil.isEmpty(types)) {
 					for (Iterator<CompanyType> typesIter = types.iterator(); typesIter.hasNext();) {
 						companyType = typesIter.next();
 						if (type.toString().equals(companyType.getType()))
 							break;
 						else
 							companyType = null;
 					}
 				}
 			}
 			if (companyType == null)
 				companyType = companyTypeHome.create();
 			companyType.setType(type.toString());
 			companyType.setName(type.toString());
 			companyType.store();
 			company.setType(companyType);
 		}
 		
 		Object workPlace = context.getVariable("string_volunteerOrganizationWorkplace");
 		if (workPlace != null) {
 			Commune workingArea = getCommune(workPlace.toString());
 			company.setWorkingArea(workingArea);
 		}
 		
 		Object headquarter = context.getVariable("string_volunteerOrganizationHeadquarter");
 		if (headquarter != null) {
 			Commune legalCommune = getCommune(headquarter.toString());
 			company.setLegalCommune(legalCommune);
 		}
 		
 		company.store();
 	}
 	
 	private Commune getCommune(String communeName) throws IDOLookupException, CreateException {
 		CommuneHome communeHome = (CommuneHome) IDOLookup.getHome(Commune.class);
 		Commune commune = null;
 		try {
 			commune = communeHome.findByCommuneName(communeName.toString());
 		} catch (FinderException e) {
 			commune = communeHome.create();
 			commune.setCommuneName(communeName.toString());
 			commune.store();
 		}
 		return commune;
 	}
 	
 	private String getUserPersonalId(ExecutionContext context) {
 		Object personalId = context.getVariable("string_volunteerOrganizationContactPersonId");
 		if (personalId == null)
 			return null;
 		return personalId.toString();
 	}
 	
 	private User getUser(ExecutionContext context) {
 		String personalId = getUserPersonalId(context);
 		UserBusiness userBusiness = getServiceInstance(UserBusiness.class);
 		try {
 			return userBusiness.getUser(personalId);
 		} catch (RemoteException e) {
 			getLogger().log(Level.WARNING, "Error getting user by personal ID: " + personalId, e);
 		} catch (FinderException e) {}
 		return null;
 	}
 	
 	private User createUser(ExecutionContext context) {
 		User user = getUser(context);
 		String personalId = getUserPersonalId(context);
 		
 		Object name = context.getVariable("string_volunteerOrganizationContactPersonName");
 		Object phone = context.getVariable("string_volunteerOrganizationContactPersonTelephoneNumber");
 		Object email = context.getVariable("string_volunteerOrganizationContactPersonEmail");
 		if (user == null) {
 			UserPersonalData upd = new UserPersonalData();
 			upd.setCreateWithLogin(true);
 			upd.setPersonalId(personalId);
 			
 			if (name != null)
 				upd.setFullName(name.toString());
 			
 			if (phone != null)
 				upd.setUserPhone(phone.toString());
 			
 			if (email != null)
 				upd.setUserEmail(email.toString());
 			
			getUserHandler().setPublishEvent(false);
 			try {
				user = getUserHandler().createUser(upd);
 			} catch (Exception e) {
 				getLogger().log(Level.WARNING, "Error creating user by personal ID: " + personalId, e);
 			}
 			
 			String password = upd.getUserPassword();
 			String userName = StringUtil.isEmpty(upd.getUserName()) ? user.getPersonalID() : upd.getUserName();
 			IWResourceBundle iwrb = getResourceBundle(getBundle(VolunteerConstants.IW_BUNDLE_IDENTIFIER));
 			String subject = iwrb.getLocalizedString("registration_successfull", "Registration successfull");
 			String text = iwrb.getLocalizedString("account_created_message", "Hello, {0}. \n\nYour registration was successful. You can login with your login name {1} and password {2} by going to {3}");
 			text = StringHandler.replace(text, "{0}", user.getName());
 			text = StringHandler.replace(text, "{1}", userName);
 			text = StringHandler.replace(text, "{2}", password);
 			text = StringHandler.replace(text, "{3}", CoreUtil.getIWContext().getDomain().getURL());
 			SendMailMessageValue message = new SendMailMessageValue(null, null, null, null, null, subject, text, null, null);
 			sendMail(user, message);
 		} else {
 			userUpdated = true;
 			if (personalId != null)
 				user.setPersonalID(personalId);
 			
 			if (name != null)
 				user.setName(name.toString());
 			
 			UserBusiness userBusiness = getServiceInstance(UserBusiness.class);
 			
 			if (phone != null)
 				try {
 					userBusiness.updateUserPhone(user, PhoneType.HOME_PHONE_ID, phone.toString());
 				} catch (Exception e) {}
 			
 			if (email != null)
 				try {
 					userBusiness.updateUserMail(user, email.toString());
 				} catch (Exception e) {}
 				
 			user.store();
 		}
 		
 		return user;
 	}
 	
 	private void createOrEnableOrganization(ExecutionContext context, Case theCase) {
 		Company company = getCompany(context);
 		if (company == null)
 			throw new RuntimeException("Company by ID '" + getCompanyId(context) + "' does not exist!");
 		
 		company.setValid(true);
 		company.store();
 		
 		User companyContactPerson = createUser(context);
 		if (companyContactPerson == null)
 			throw new RuntimeException("Contact person for company " + company + " can not be created!");
 		
 		getCompanyHandler().bindUserAndCompany(companyContactPerson, company);
 		getVolunteerServices().addUserToGroup(VolunteerConstants.GROUP_VOLUNTEERS_ORGANIZATION, companyContactPerson, null);
 		
 		if (userUpdated)
 			enableOrDisableLogin(companyContactPerson, false);
 		
 		try {
 			Collection<User> subscribers = theCase.getSubscribers();
 			if (ListUtil.isEmpty(subscribers) || !subscribers.contains(companyContactPerson)) {
 				theCase.addSubscriber(companyContactPerson);
 				theCase.store();
 			}
 		} catch (IDOAddRelationshipException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void enableOrDisableLogin(User user, boolean disable) {
 		LoginInfo loginInfo = LoginDBHandler.getLoginInfo(LoginDBHandler.getUserLogin(user));
 		
 		String subject = null;
 		String text = null;
 		IWResourceBundle iwrb = getResourceBundle(getBundle(VolunteerConstants.IW_BUNDLE_IDENTIFIER));
 		if (disable) {
 			loginInfo.setAccountEnabled(false);
 			loginInfo.store();
 			
 			subject = iwrb.getLocalizedString("account_is_disabled", "Account is disabled");
 			text = iwrb.getLocalizedString("account_disabled_message", "Hi {0}, \n\nYour account at {1} is currently disabled.");
 		} else {
 			if (!loginInfo.getAccountEnabled()) {
 				loginInfo.setAccountEnabled(true);
 				loginInfo.store();
 				
 				subject = iwrb.getLocalizedString("account_is_enabled", "Account is enabled");
 				text = iwrb.getLocalizedString("account_ebabled_message", "Hi {0}, \n\nYour account at {1} is currently enabled.");
 			}
 		}
 		
 		if (subject != null && text != null) {
 			text = StringHandler.replace(text, "{0}", user.getName());
 			text = StringHandler.replace(text, "{1}", CoreUtil.getIWContext().getDomain().getURL());
 			sendMail(user, new SendMailMessageValue(null, null, null, null, null, subject, text, null, null));
 		}
 	}
 	
 	private void disableOrganization(ExecutionContext context, Case theCase) {
 		Company company = getCompany(context);
 		if (context == null)
 			throw new RuntimeException("Company can not be resolved");
 		
 		User user = getUser(context);
 		if (user == null)
 			throw new RuntimeException("User can not be found");
 	
 		company.setValid(false);
 		
 		enableOrDisableLogin(user, true);
 		
 		try {
 			Collection<User> subscribers = theCase.getSubscribers();
 			if (!ListUtil.isEmpty(subscribers) && subscribers.contains(user)) {
 				theCase.removeSubscriber(user);
 				theCase.store();
 			}
 		} catch (IDORemoveRelationshipException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void sendMail(User user, final SendMailMessageValue message) {
 		UserBusiness userBusiness = getServiceInstance(UserBusiness.class);
 		Email email = null;
 		try {
 			email = userBusiness.getUserMail(user);
 		} catch (Exception e) {
 			getLogger().log(Level.WARNING, "Error getting email for user: " + user, e);
 		}
 		if (email == null)
 			return;
 		
 		message.setTo(email.getEmailAddress());
 		message.setFrom(getApplication().getSettings().getProperty(MessagingSettings.PROP_MESSAGEBOX_FROM_ADDRESS, "staff@idega.com"));
 		Thread sender = new Thread(new Runnable() {
 			public void run() {
 				try {
 					SendMail.send(message);
 				} catch (MessagingException e) {
 					getLogger().log(Level.WARNING, "Error sending message: " + message, e);
 				}
 			}
 		});
 		sender.start();
 	}
 
 	public Long getProcessInstanceId() {
 		return processInstanceId;
 	}
 
 	public void setProcessInstanceId(Long processInstanceId) {
 		this.processInstanceId = processInstanceId;
 	}
 
 }
