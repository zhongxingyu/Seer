 package is.idega.idegaweb.pheidippides.webservice.business;
 
 import is.idega.idegaweb.pheidippides.business.ParticipantHolder;
 import is.idega.idegaweb.pheidippides.business.PheidippidesService;
 import is.idega.idegaweb.pheidippides.business.RegistrationStatus;
 import is.idega.idegaweb.pheidippides.business.ShirtSizeGender;
 import is.idega.idegaweb.pheidippides.business.ShirtSizeSizes;
 import is.idega.idegaweb.pheidippides.dao.PheidippidesDao;
 import is.idega.idegaweb.pheidippides.data.Charity;
 import is.idega.idegaweb.pheidippides.data.Company;
 import is.idega.idegaweb.pheidippides.data.Distance;
 import is.idega.idegaweb.pheidippides.data.Event;
 import is.idega.idegaweb.pheidippides.data.Participant;
 import is.idega.idegaweb.pheidippides.data.Race;
 import is.idega.idegaweb.pheidippides.data.RaceShirtSize;
 import is.idega.idegaweb.pheidippides.data.Registration;
 import is.idega.idegaweb.pheidippides.data.ShirtSize;
 import is.idega.idegaweb.pheidippides.webservice.data.WebServiceLoginSession;
 import is.idega.idegaweb.pheidippides.webservice.data.WebServiceLoginSessionHome;
 import is.idega.idegaweb.pheidippides.webservice.isb.server.RelayPartnerInfo;
 import is.idega.idegaweb.pheidippides.webservice.server.CharityInformation;
 import is.idega.idegaweb.pheidippides.webservice.server.Session;
 import is.idega.idegaweb.pheidippides.webservice.server.SessionTimedOutException;
 
 import java.rmi.RemoteException;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.ejb.CreateException;
 import javax.ejb.FinderException;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 
 import com.idega.business.IBOLookup;
 import com.idega.business.IBOLookupException;
 import com.idega.business.IBORuntimeException;
 import com.idega.core.accesscontrol.business.LoginBusinessBean;
 import com.idega.core.accesscontrol.business.LoginDBHandler;
 import com.idega.core.accesscontrol.data.LoginTable;
 import com.idega.core.accesscontrol.data.LoginTableHome;
 import com.idega.core.contact.data.Email;
 import com.idega.core.contact.data.Phone;
 import com.idega.core.location.data.Address;
 import com.idega.core.location.data.Country;
 import com.idega.data.IDOLookup;
 import com.idega.data.IDOLookupException;
 import com.idega.idegaweb.IWMainApplication;
 import com.idega.user.business.NoPhoneFoundException;
 import com.idega.user.business.UserBusiness;
 import com.idega.user.data.User;
 import com.idega.util.IWTimestamp;
 import com.idega.util.LocaleUtil;
 
 @Scope("singleton")
 @Service("pheidippidesWebService")
 public class PheidippidesWebService {
 
 	public static final String DISTANCE1 = "3km";
 	public static final String DISTANCE2 = "10km";
 	public static final String DISTANCE3 = "21km";
 	public static final String DISTANCE4 = "42km";
 	public static final String DISTANCE5 = "Relay";
 
 	public static final String SIZE1 = "XS";
 	public static final String SIZE2 = "S";
 	public static final String SIZE3 = "M";
 	public static final String SIZE4 = "L";
 	public static final String SIZE5 = "XL";
 	public static final String SIZE6 = "XXL";
 
 	@Autowired
 	private PheidippidesDao dao;
 
 	@Autowired
 	private PheidippidesService service;
 
 	private static final Long RM_ID = 1L;
 	
 	public String registerRunner(
 			is.idega.idegaweb.pheidippides.webservice.isb.server.Session session,
 			String personalID, String distance, String shirtSize,
 			String shirtSizeGender, String email, String phone, String mobile,
 			String leg, RelayPartnerInfo[] partners, String registeredBy,
 			String charityPersonalID)
 			throws is.idega.idegaweb.pheidippides.webservice.isb.server.SessionTimedOutException {
 
 		WebServiceLoginSession loginSession = validateAndUpdateLoginSession(new is.idega.idegaweb.pheidippides.webservice.server.Session(session.getSessionID()));
 		if (loginSession != null) {
 		} else {
 			throw new is.idega.idegaweb.pheidippides.webservice.isb.server.SessionTimedOutException();
 		}
 
 		Participant participant = new Participant();
 		participant.setPersonalId(personalID);
 		participant.setEmail(email);
 		participant.setPhoneHome(phone);
 		participant.setPhoneMobile(mobile);
 		
 		Charity charity = null;
 		if (charityPersonalID != null) {
 			charity = dao.getCharity(charityPersonalID);
 		}
 		
 		Distance d = dao.getDistance(distance);
 		if (d == null) {
 			return "ERROR: Could not find distance";
 		}
 		
 		Company company = dao.getCompanyByUserUUID(loginSession.getUser().getUniqueId());
 
 		Race race = dao.getRace(company.getEvent(), d, IWTimestamp.RightNow().getYear(), (leg == null || "0".equals(leg)) ? false : true); //todo get the correct year
 		
 		if (race == null) {
 			return "ERROR: Could not find distance for the event this company can register to for this year";			
 		}
 
		/*ShirtSizeGender gender = null;
 		if (shirtSizeGender.equalsIgnoreCase("male")) {
 			gender = ShirtSizeGender.Male;
 		} else if (shirtSizeGender.equalsIgnoreCase("female")) {
 			gender = ShirtSizeGender.Female;
 		} else if (shirtSizeGender.equalsIgnoreCase("child")) {
 			gender = ShirtSizeGender.Child;
 		} 
  
 		if (gender == null) {
 			return "ERROR: Unknown gender";
 		}
 
 		ShirtSizeSizes size = null;
 		if (shirtSize.equalsIgnoreCase("XXXS")) {
 			size = ShirtSizeSizes.XXXS;
 		} else if (shirtSize.equalsIgnoreCase("XXS")) {
 			size = ShirtSizeSizes.XXS;			
 		} else if (shirtSize.equalsIgnoreCase("XS")) {
 			size = ShirtSizeSizes.XS;
 		} else if (shirtSize.equalsIgnoreCase("S")) {
 			size = ShirtSizeSizes.S;
 		} else if (shirtSize.equalsIgnoreCase("M")) {
 			size = ShirtSizeSizes.M;
 		} else if (shirtSize.equalsIgnoreCase("L")) {
 			size = ShirtSizeSizes.L;
 		} else if (shirtSize.equalsIgnoreCase("XL")) {
 			size = ShirtSizeSizes.XL;
 		} else if (shirtSize.equalsIgnoreCase("XXL")) {
 			size = ShirtSizeSizes.XXL;
 		} else if (shirtSize.equalsIgnoreCase("XXXL")) {
 			size = ShirtSizeSizes.XXXL;
 		}
 		
 		if (size == null) {
 			return "ERROR: Unknown shirt size";
 		}
 		
 		ShirtSize shirtSizeEntry = dao.getShirtSize(size, gender);
 		if (shirtSizeEntry == null) {
 			return "ERROR: Could not find shirt size for this gender";
 		}
 		
 		RaceShirtSize rss = dao.getRaceShirtSize(race, shirtSizeEntry);
 		if (rss == null) {
 			return "ERROR: Shirt size not available for this distance";
		}*/
 		
 		//Add checks and relay handling
 		
 		ParticipantHolder holder = new ParticipantHolder();
 		holder.setAcceptsWaiver(true);
 		holder.setAmount(0);
 		holder.setBestMarathonTime(null);
 		holder.setBestUltraMarathonTime(null);
 		holder.setCharity(charity);
 		holder.setDiscount(0);
 		holder.setHasDoneLVBefore(false);
 		holder.setHasDoneMarathonBefore(false);
 		holder.setLeg(null);
 		holder.setParticipant(participant);
 		holder.setRace(race);
 		holder.setRelayPartners(null);
		holder.setShirtSize(null);
 		holder.setTeam(null);
 		holder.setTrinkets(null);
 		
 		
 		return service.storeWebserviceRegistration(holder, company, loginSession.getUser().getUniqueId(), LocaleUtil.getIcelandicLocale());
 	}
 
 	public is.idega.idegaweb.pheidippides.webservice.server.Charity[] getCharities(String locale) {
 		List<Charity> charities = dao.getEvent(PheidippidesWebService.RM_ID).getCharities();
 		if (charities != null && !charities.isEmpty()) {
 			is.idega.idegaweb.pheidippides.webservice.server.Charity[] ret = new is.idega.idegaweb.pheidippides.webservice.server.Charity[charities.size()];
 			int counter = 0;
 			for (Charity charity : charities) {
 				ret[counter] = new is.idega.idegaweb.pheidippides.webservice.server.Charity();
 				ret[counter].setId(charity.getPersonalId());
 				ret[counter].setName(charity.getName());
 				if (locale == null || !"en".equalsIgnoreCase(locale)) {
 					ret[counter++].setDescription(charity.getDescription());
 				} else {
 					ret[counter++].setDescription(charity.getEnglishDescription());
 				}
 				
 			}
 			
 			return ret;
 		}
 		
 		return null;
 	}
 
 	public is.idega.idegaweb.pheidippides.webservice.server.Charity getCharity(
 			String charityPersonalID, String locale) {
 		Charity charity = dao.getCharity(charityPersonalID);
 		is.idega.idegaweb.pheidippides.webservice.server.Charity ret = null;
 		if (charity != null) {
 			ret = new is.idega.idegaweb.pheidippides.webservice.server.Charity();
 			if (locale == null || !"en".equalsIgnoreCase(locale)) {
 				ret.setDescription(charity.getDescription());
 			} else {
 				ret.setDescription(charity.getEnglishDescription());
 			}
 			ret.setId(charity.getPersonalId());
 			ret.setName(charity.getName());
 			
 			return ret;
 		}
 		
 		return null;
 	}
 
 	public User getUserFromIdentifier(String identifier) {
 		User user = null;
 		try {
 			user = getUserBusiness().getUser(identifier);
 		} catch (Exception e) {
 		} 
 
 		if (user == null) {
 			try {
 				LoginTable login = LoginDBHandler.getUserLoginByUserName(identifier);
 				if (login != null) {
 					user = login.getUser();
 				}
 			} catch (Exception e) {
 			}
 		}
 
 		return user;
 	}
 	
 	public CharityInformation getCharityInformation(String identifier) {
 		User user = getUserFromIdentifier(identifier); 
 				
 		if (user == null) {
 			return null;
 		}
 		
 		Event event = dao.getEvent(1L); //TODO get event some other way :D
 		List<Registration> regs = dao.getRegistrationForUser(event, IWTimestamp.RightNow().getYear(), user.getUniqueId());
 		if (regs == null || regs.isEmpty()) {
 			regs = dao.getRelayPartnerRegistrationForUser(event, IWTimestamp.RightNow().getYear(), user.getUniqueId());
 		}
 		if (regs != null) {
 			for (Registration registration : regs) {
 				if (registration.getStatus().equals(RegistrationStatus.OK) || registration.getStatus().equals(RegistrationStatus.RelayPartner)) {
 					CharityInformation info = new CharityInformation();
 					Address address = null;
 					try {
 						address = getUserBusiness().getUsersMainAddress(user);
 					} catch (IBOLookupException e) {
 					} catch (RemoteException e) {
 					}
 					
 					Email email = null;
 					try {
 						email = getUserBusiness().getUserMail(user);
 					} catch (IBOLookupException e) {
 					} catch (RemoteException e) {
 					}
 					
 					Phone phone = null;
 					try {
 						phone = getUserBusiness().getUsersHomePhone(user);
 					} catch (IBOLookupException e) {
 					} catch (RemoteException e) {
 					} catch (NoPhoneFoundException e) {
 					}
 
 					Phone mobile = null;
 					try {
 						mobile = getUserBusiness().getUsersMobilePhone(user);
 					} catch (IBOLookupException e) {
 					} catch (RemoteException e) {
 					} catch (NoPhoneFoundException e) {
 					}
 					
 					info.setAddress(address.getStreetAddress());
 					if (registration.getCharity() != null) {
 						info.setCharityID(registration.getCharity().getPersonalId());
 						info.setCharityName(registration.getCharity().getName());
 					}
 					info.setCity(address.getCity());
 					info.setCountry(address.getCountry().getName());
 					info.setDistance(registration.getRace().getDistance().getName());
 					if (email != null) {
 						info.setEmail(email.getEmailAddress());
 					}
 					info.setEmployee(false);
 					if (user.getGender().isFemaleGender()) {
 						info.setGender("female");
 					} else {
 						info.setGender("male");
 					}
 					if (mobile != null) {
 						info.setMobile(mobile.getNumber());						
 					}
 					info.setName(user.getName());
 					
 					Country country = null;
 					try {
 						country = getUserBusiness().getAddressBusiness().getCountryHome().findByPrimaryKey(new Integer(registration.getNationality()));
 					} catch (Exception e) {
 						e.printStackTrace();
 					} 
 					
 					if (country != null) {
 						info.setNationality(country.getName());
 					}
 					info.setPersonalID(user.getPersonalID());
 					if (phone != null) {
 						info.setPhone(phone.getNumber());
 					}
 					if (address.getPostalCode() != null) {
 						info.setPostalCode(address.getPostalCode().getPostalCode());
 					} else {
 						info.setPostalCode("");
 					}
 					
 					return info;
 				}
 			}
 		}
 		return null;
 	}
 
 	public is.idega.idegaweb.pheidippides.webservice.server.Session authenticateUser(
 			String userName, String password) {
 		try {
 
 			LoginBusinessBean loginBean = LoginBusinessBean
 					.getDefaultLoginBusinessBean();
 			LoginTable loginTable = getLoginTableHome().findByLogin(userName);
 			if (loginTable != null) {
 				// check if user is already verified
 				Collection<WebServiceLoginSession> sessions = getWebServiceLoginSessionHome()
 						.findAllByUser(loginTable.getUser());
 				if (sessions != null && !sessions.isEmpty()) {
 					IWTimestamp now = IWTimestamp.RightNow();
 					Iterator<WebServiceLoginSession> it = sessions.iterator();
 					while (it.hasNext()) {
 						WebServiceLoginSession loginSession = it.next();
 						IWTimestamp lastAccess = new IWTimestamp(
 								loginSession.getLastAccess());
 						if (IWTimestamp.getMilliSecondsBetween(lastAccess, now) <= getSessionTimeout()) {
 							loginSession.setLastAccess(now.getTimestamp());
 							loginSession.store();
 
 							return new is.idega.idegaweb.pheidippides.webservice.server.Session(
 									loginSession.getUniqueId());
 						} else {
 							loginSession.setIsClosed(true);
 							loginSession.store();
 						}
 					}
 				}
 
 				// verify password and create new ws session
 				if (loginBean.verifyPassword(loginTable, password)) {
 					// check if corresponds to a company that is allowed to use the webservice
 					try {
 						Company company = dao.getCompanyByUserUUID(loginTable.getUser().getUniqueId());
 						if (company == null || !company.getWebserviceUser()) {
 							return new is.idega.idegaweb.pheidippides.webservice.server.Session(
 									"-1");							
 						}
 					} catch (Exception e) {
 						return new is.idega.idegaweb.pheidippides.webservice.server.Session(
 								"-1");
 					}
 
 					WebServiceLoginSession loginSession = getWebServiceLoginSessionHome()
 							.create();
 					IWTimestamp now = IWTimestamp.RightNow();
 					loginSession.setCreated(now.getTimestamp());
 					loginSession.setLastAccess(now.getTimestamp());
 					loginSession.setIsClosed(false);
 					loginSession.setUser(loginTable.getUser());
 					loginSession.store();
 
 					return new is.idega.idegaweb.pheidippides.webservice.server.Session(
 							loginSession.getUniqueId());
 				}
 			}
 		} catch (FinderException e) {
 			e.printStackTrace();
 		} catch (CreateException e) {
 			e.printStackTrace();
 		}
 
 		return new is.idega.idegaweb.pheidippides.webservice.server.Session(
 				"-1");
 	}
 
 
 
 	private LoginTableHome getLoginTableHome() {
 		try {
 			return (LoginTableHome) IDOLookup.getHome(LoginTable.class);
 		} catch (IDOLookupException ile) {
 			throw new IBORuntimeException(ile);
 		}
 	}
 
 	private int getSessionTimeout() {
 		String timeout = IWMainApplication.getDefaultIWApplicationContext().getIWMainApplication().getSettings()
 				.getProperty("WEBSERVICE_TIMEOUT", "900000");
 
 		return Integer.parseInt(timeout);
 	}
 
 	private WebServiceLoginSession validateAndUpdateLoginSession(
 			is.idega.idegaweb.pheidippides.webservice.server.Session session) {
 		if (session == null) {
 			return null;
 		}
 
 		if (session.getSessionID() == null
 				|| "-1".equals(session.getSessionID())) {
 			return null;
 		}
 
 		try {
 			WebServiceLoginSession loginSession = getWebServiceLoginSessionHome()
 					.findByUniqueID(session.getSessionID());
 			if (loginSession != null && !loginSession.getIsClosed()) {
 				IWTimestamp now = IWTimestamp.RightNow();
 				IWTimestamp lastAccess = new IWTimestamp(
 						loginSession.getLastAccess());
 				if (IWTimestamp.getMilliSecondsBetween(lastAccess, now) <= getSessionTimeout()) {
 					loginSession.setLastAccess(now.getTimestamp());
 					loginSession.store();
 
 					return loginSession;
 				} else {
 					loginSession.setIsClosed(true);
 					loginSession.store();
 				}
 			}
 		} catch (FinderException e) {
 		}
 
 		return null;
 	}
 
 	private WebServiceLoginSessionHome getWebServiceLoginSessionHome() {
 		try {
 			return (WebServiceLoginSessionHome) IDOLookup
 					.getHome(WebServiceLoginSession.class);
 		} catch (IDOLookupException ile) {
 			throw new IBORuntimeException(ile);
 		}
 	}
 
 	private UserBusiness getUserBusiness() throws IBOLookupException {
 		return IBOLookup.getServiceInstance(
 				IWMainApplication.getDefaultIWApplicationContext(),
 				UserBusiness.class);
 	}
 
 	public boolean updateUserPassword(Session session, String personalID,
 			String password) throws RemoteException, SessionTimedOutException {
 
 		WebServiceLoginSession loginSession = validateAndUpdateLoginSession(session);
 		if (loginSession != null) {
 		} else {
 			throw new SessionTimedOutException();
 		}
 
 		try {
 			User user = getUserBusiness().getUser(personalID);
 			if (getUserBusiness().hasUserLogin(user)) {
 				LoginTable login = LoginDBHandler.getUserLogin(user);
 				LoginDBHandler.changePassword(login, password);
 			}
 		} catch (Exception e) {
 		}
 
 		return false;
 	}
 
 	public boolean updateUserCharity(Session session, String personalID,
 			String charityPersonalID) throws RemoteException,
 			SessionTimedOutException {
 
 		return false;
 	}
 
 	public boolean updateUserEmail(Session session, String personalID,
 			String email) throws RemoteException, SessionTimedOutException {
 		WebServiceLoginSession loginSession = validateAndUpdateLoginSession(session);
 		if (loginSession != null) {
 		} else {
 			throw new SessionTimedOutException();
 		}
 
 		try {
 			User user = getUserBusiness().getUser(personalID);
 			getUserBusiness().updateUserMail(user, email);
 
 			return true;
 		} catch (Exception e) {
 		}
 
 		return false;
 	}
}
