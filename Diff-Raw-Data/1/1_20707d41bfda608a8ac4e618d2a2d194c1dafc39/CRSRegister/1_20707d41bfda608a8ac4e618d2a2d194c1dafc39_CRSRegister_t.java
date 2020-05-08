 package org.alt60m.crs.servlet;
 
 import org.alt60m.ministry.model.dbio.Staff;
 //import org.alt60m.ministry.model.dbio.Address;
 import org.alt60m.util.*;
 import org.apache.log4j.*;
 import org.alt60m.servlet.*;
 import java.util.*;
 //import org.alt60m.factory.ServiceFactory;
 import org.alt60m.security.dbio.manager.SimpleSecurityManager;
 import org.alt60m.security.dbio.manager.UserLockedOutException;
 import org.alt60m.security.dbio.manager.UserNotFoundException;
 import org.alt60m.crs.application.*;
 import org.alt60m.crs.model.*;
 import org.alt60m.ministry.model.dbio.TargetArea;
 import org.alt60m.ministry.servlet.MinistryLocatorInfo;
 import org.alt60m.security.dbio.model.User;
 
 import com.kenburcham.framework.dbio.DBIOEntityException;
 
 public class CRSRegister extends org.alt60m.servlet.Controller {
 	private CRSApplication crsApp;
 
 	public CRSRegister() {
 		crsApp = new CRSApplication();
 	}
 
 	private final String VIEWS_FILE = "/WEB-INF/crsregisterviews.xml";
 
 	private final String DEFAULT_ACTION = "showIndex";
 
 	// Error messages:
 	private final String ERR_username = "You must login before you can access this page. Click continue to be taken to the login page.";
 
 	private final String ERR_conferenceNotFound = "The conference could not be located. This most likely happened because your browser has been inactive too long. Click back to login to your conference again.";
 
 	public void init() {
 		log.debug("CRSRegister.init()");
 		super.setViewsFile(getServletContext().getRealPath(VIEWS_FILE));
 		super.setDefaultAction(DEFAULT_ACTION);
 	}
 
 	public void reload() {
 		super.setViewsFile(getServletContext().getRealPath(VIEWS_FILE));
 	}
 
 	/** ***************************************************************************** */
 	//Created: 10/29/2002 DMB
 	//Generates an exception for testing purposes.
 	public void sampleError(ActionContext ctx) {
 		try {
 			String test = null;
 			if (test.equals("EXCEPTION! OUCH"))
 				;
 		} catch (Exception e) {
 			goToErrorPage(ctx, e, "sampleError");
 		}
 	}
 
 	//Created: 10/29/2002 DMB
 	//default error page handler
 	private void goToErrorPage(ActionContext ctx, Exception e, String methodName) {
 		ActionResults ar = new ActionResults();
 		String exceptionText = e + "<BR>\n";
 		ar.putValue("errorMsg", exceptionText);
 		ar.putValue("nextAction", "");
 		ctx.setReturnValue(ar);
 		ctx.goToView("error");
 		log.error("Failed to perform " + methodName + "().", e);
 	}
 
 	/** ***************************************************************************** */
 
 	public void showIndex(ActionContext ctx) {
 		try {
 			if (ctx.getInputString("ConferenceID") != null)
 				if (ctx.getInputString("type") != null)
 					userLogin(ctx);
 				else
 					selectEvent(ctx);
 			else
 				listEvents(ctx);
 		} catch (Exception e) {
 			goToErrorPage(ctx, e, "showIndex");
 		}
 	}
 
 	public void listEvents(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults();
 			String currentRegion = "";
 			ctx.setSessionValue("userLoggedIn", null);
 			ctx.setSessionValue("userLoggedInSsm", null);
 			ctx.setSessionValue("spouseRegID", null);
 			ctx.setSessionValue("registrationID", null);
 
 			if (ctx.getInputString("currentRegion") != null)
 				currentRegion = ctx.getInputString("currentRegion");
 			else
 				currentRegion = "ALL";
 
 			ar.putValue("currentRegion", currentRegion);
 			ctx.setSessionValue("selectedEvent", null);
 			ctx.setSessionValue("userLoggedIn", null);
 			ctx.setSessionValue("userLoggedInSsm", null);
 			ctx.setSessionValue("spouseRegID", null);
 			ar.addCollection("Conferences", crsApp.listActiveConferences(
 					currentRegion, "region, name", "ASC"));
 
 			ctx.setReturnValue(ar);
 			ctx.goToView("listEvents");
 		} catch (Exception e) {
 			goToErrorPage(ctx, e, "listEvents");
 		}
 	}
 	
 	public void showEventDetails(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults();
 			if (ctx.getSessionValue("registrationID") == null) {
 				ar.putValue("errorMsg", ERR_conferenceNotFound);
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else {
 				Registration r = resolveRegistration(ctx, false);
 				String conferenceID = ctx.getInputString("ConferenceID");
 				conferenceID = conferenceID == null ? (String) ctx.getSessionValue("selectedEvent")
 						: conferenceID;
 				
 				ar.putObject("registration", r);
 				ar.putObject(
 						"conference",
 						crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 				ar.addCollection("items", crsApp.listCustomItems(conferenceID,
 						"displayOrder", "ASC"));
 	
 				ctx.setReturnValue(ar);
 				ctx.goToView("showEventDetails");
 			}
 		} catch (Exception e) {
 			goToErrorPage(ctx, e, "showEventDetails");
 		}
 	}
 
 	public void selectEvent(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults();
 			if ((ctx.getInputString("ConferenceID") == null || ctx.getInputString("ConferenceID").equals(""))
 					&& ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue(
 						"errorMsg",
 						"The requested event could not be located, click continue to select another event.");
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else {
 				String conferenceID = ctx.getInputString("ConferenceID");
 				conferenceID = conferenceID == null ? (String) ctx.getSessionValue("selectedEvent")
 						: conferenceID;
 				Conference c = crsApp.getConference(conferenceID);
 				Vector regTypesVector = new Vector();
 				Iterator regTypes = c.getRegistrationTypes().iterator();
 				while (regTypes.hasNext()) {
 					RegistrationType rt = (RegistrationType) regTypes.next();
 					Vector v = new Vector();
 					v.add(String.valueOf(rt.getRegistrationTypeID()));
 					v.add((String) rt.getLabel());
 					v.add((String) rt.getDescription());
 					regTypesVector.add(v);
 				}
 				ar.putObject("RegistrationTypes", regTypesVector);
 				ctx.setSessionValue("userLoggedIn", null);
 				ctx.setSessionValue("userLoggedInSsm", null);
 				ctx.setSessionValue("spouseRegID", null);
 				ctx.setSessionValue("selectedEvent", conferenceID);
 				ar.putObject("conference", c);
 				ar.addCollection("items", crsApp.listCustomItems(conferenceID,
 						"displayOrder", "ASC"));
 
 				ctx.setReturnValue(ar);
 				ctx.goToView("selectRegistrationType");
 			}
 		} catch (Exception e) {
 			goToErrorPage(ctx, e, "selectEvent");
 		}
 	}
 
 	public void userLogin(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults();
 			if (ctx.getInputString("ConferenceID") == null
 					&& ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue(
 						"errorMsg",
 						"The requested event could not be located, click continue to select another event.");
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else {
 				String conferenceID = ctx.getInputString("ConferenceID");
 				conferenceID = conferenceID == null ? (String) ctx.getSessionValue("selectedEvent")
 						: conferenceID;
 				conferenceID = conferenceID.trim();
 				String regTypeID = "";
 				if (ctx.getInputString("regTypeID") == null) {
 					regTypeID = ctx.getInputString("type");
 					selectEvent(ctx);
 					return;  // Avoid getting a NullPointerException
 				} else
 					regTypeID = ctx.getInputString("regTypeID");
 				
 				if ("existing".equals(regTypeID)) { // use an existing registration
 					ar.putValue("regTypeID", "existing");
 					ar.putValue("regTypeLabel",
 							"Continue Existing Registration");
 					ar.putValue("regTypeDescription", "");
 					
 				} else {							// start a new registration
 					RegistrationType rt = crsApp.getRegistrationType(regTypeID);
 					ar.putValue("regTypeID", regTypeID);
 					ar.putValue("regTypeLabel", rt.getLabel());
 					ar.putValue("regTypeDescription", rt.getDescription());
 					
 				}
 				
 				ctx.setSessionValue("selectedEvent", conferenceID);
 				ar.putValue("onlyOneRegType",crsApp.countRegistrationTypes(conferenceID)==1?"true":"false");
 				
 				Conference conference = crsApp.getConference(conferenceID);
 				Date thisDay = new Date();
 				Date preRegStartDate = conference.getPreRegStart(); 
 				Date preRegEnd = conference.getPreRegEnd();
 				boolean conferenceOpen;
 				if (preRegStartDate != null && preRegEnd != null) {
 					conferenceOpen = ((preRegStartDate != null) && (thisDay.after(preRegStartDate))
 						|| org.alt60m.util.DateUtils.isSameDay(thisDay, preRegStartDate)) 
 					&& (thisDay.before(preRegEnd) 
 							|| org.alt60m.util.DateUtils.isSameDay(thisDay, preRegEnd)); 
 				} else
 				{
 					conferenceOpen = false;
 				}
 				if (ctx.getProfile() != null && conferenceOpen) {	// user logged in
 					
 					if ("Y".equals(ctx.getInputString("preview"))) {
 						ctx.setSessionValue("userLoggedIn", null);
 						ctx.setSessionValue("userLoggedInSsm", null);
 						ar.putObject("conference",
 								crsApp.getConference(conferenceID));
 						ctx.setReturnValue(ar);
 						ctx.goToView("userLogin");
 					} else {
 						String username = (String) ctx.getProfile().get(
 								"UserName");
 						log.info("user " + username
 								+ " authenticated by staffsite.");
 						int ssmID = crsApp.getSsmID(username);
 						Person p = crsApp.getPersonBySsmID(ssmID);
 						if (p.isPKEmpty()) {
 							p.setFirstName((String) ctx.getProfile().get(
 									"FirstName"));
 							p.setLastName((String) ctx.getProfile().get(
 									"LastName"));
 							p.setFk_ssmUserID(ssmID);
 							p.setEmail(username);
 							p.insert();
 						}
 						ctx.setSessionValue("userLoggedInSsm", String.valueOf(ssmID));
 						ctx.setSessionValue("userLoggedIn", username);
 						userAuthenticated(ctx);
 					}
 				} else {		// not logged in
 					
 					ctx.setSessionValue("userLoggedIn", null);
 					ctx.setSessionValue("userLoggedInSsm", null);
 					ar.putObject("conference",
 							crsApp.getConference(conferenceID));
 					ctx.setReturnValue(ar);
 					ctx.goToView("userLogin");
 				}
 			}
 		} catch (Exception e) {
 			goToErrorPage(ctx, e, "userLogin");
 		}
 	}
 
 	public void userAuthenticate(ActionContext ctx) {
 		ActionResults ar = new ActionResults("userAuthenticate");
 		String username = "(none)";
 		try {
 			if (ctx.getInputString("ConferenceID") == null
 					&& ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue(
 						"errorMsg",
 						"The requested event could not be located, click continue to select another event.");
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getInputString("Username") == null
 					|| ctx.getInputString("Password") == null) {
 				ar.putValue("errorMsg",
 						"Invalid username or password click continue to try again.");
 				ar.putValue("nextAction", "userLogin&regTypeID="
 						+ ctx.getInputString("regTypeID"));
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else {
 				username = ctx.getInputString("Username").toLowerCase();
 				String password = ctx.getInputString("Password");
 
 				SimpleSecurityManager manager = new SimpleSecurityManager();
 
 				if (!manager.authenticate(username, password)) { //user
 					// authentication
 					// failed
 					log.info("authentication for user " + username
 							+ " failed.");
 					ar.putValue(
 							"errorMsg",
 							"Incorrect password. Click continue try again. You will be locked out after " + manager.getMaxFailedLogins() +" incorrect attempts.");
 					ar.putValue("nextAction", "userLogin&type="
 							+ ctx.getInputString("regTypeID") + "&regTypeID="
 							+ ctx.getInputString("regTypeID"));
 					ctx.setReturnValue(ar);
 					ctx.goToView("error");
 				} else { //user has been authenticated
 					log.info("user " + username + " authenticated.");
 					int ssmID = crsApp.getSsmID(username);
 					Person p = crsApp.getPersonBySsmID(ssmID);
 					if (p.isPKEmpty()) {
 						p.setFk_ssmUserID(ssmID);
 						p.setEmail(username);
 						p.insert();
 
 					}
 
 					ar.putValue("regTypeID", ctx.getInputString("regTypeID"));
 					/*
 					 * if (ctx.getInputString("type") != null)
 					 * ar.putValue("type", ctx.getInputString("type")); else
 					 * ar.putValue("type", "student");
 					 */
 					ctx.setSessionValue("userLoggedIn", username);
 					ctx.setSessionValue("userLoggedInSsm", String.valueOf(ssmID));
 					userAuthenticated(ctx);
 				}
 			}
 		} catch (UserLockedOutException e) {
 			log.info("user " + username + "'s account has been locked");
 			ar.putValue("errorMsg", "This account has been locked.");
 			ar.putValue("nextAction", "userLogin&type="
 					+ ctx.getInputString("regTypeID") + "&regTypeID="
 					+ ctx.getInputString("regTypeID"));
 			ctx.setReturnValue(ar);
 			ctx.goToView("error");
 		} catch (UserNotFoundException e) {
 			log.info("user " + username + " was not found");
 			ar.putValue("errorMsg",
 					"User not found, please go back and try again.");
 			ar.putValue("nextAction", "userLogin");
 			ctx.setReturnValue(ar);
 			ctx.goToView("error");
 		} catch (Exception e) {
 			goToErrorPage(ctx, e, "userAuthenticate");
 		}
 	}
 
 	public void userAuthenticated(ActionContext ctx) throws Exception {
 		ActionResults ar = new ActionResults();
 		if (ctx.getSessionValue("selectedEvent") == null) {
 			ar.putValue("errorMsg", ERR_conferenceNotFound);
 			ar.putValue("nextAction", "listEvents");
 			ctx.setReturnValue(ar);
 			ctx.goToView("error");
 		} else if (ctx.getSessionValue("userLoggedIn") == null) {
 			ar.putValue("errorMsg", ERR_username);
 			ar.putValue("nextAction", "userLogin");
 			ctx.setReturnValue(ar);
 			ctx.goToView("error");
 		} else {
 			Registration r = crsApp.getRegistrationBySsmID(
 					(String) ctx.getSessionValue("userLoggedInSsm"),
 					(String) ctx.getSessionValue("selectedEvent"));
 			Conference c = crsApp.getConference((String) ctx.getSessionValue("selectedEvent"));
 
 			if (r.isPKEmpty()
 					&& "existing".equals(ctx.getInputString("regTypeID"))) {
 
 				Vector regTypesVector = c.getRegistrationTypes();
 				
 				if(regTypesVector.size()>1)
 				{
 					Iterator regTypes = regTypesVector.iterator();
 					
 					String labels = "";
 					while (regTypes.hasNext()) {
 						RegistrationType rt = (RegistrationType) regTypes.next();
 						labels = labels + "[" + rt.getLabel() + "]";
 					}
 
 					ar.putValue("errorMsg",
 						"You do not have an existing registration for this conference, please choose from " + labels
 								+ " after clicking \"Continue\"");
 				}
 				else
 				{
 					ar.putValue("errorMsg",
 							"You do not have an existing registration for this conference, please begin a new registration after clicking \"Continue\"");
 				}
 				ar.putValue("nextAction", "selectEvent");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (r.isPKEmpty()) { // If new registration, create it and
 				// assoc the person
 				Person p = crsApp.getPersonBySsmID((String) ctx.getSessionValue("userLoggedInSsm"));
 				ctx.setSessionValue("regTypeID",
 						ctx.getInputString("regTypeID"));
 
 				int registrationID = crsApp.createRegistration(Integer.valueOf(
 						ctx.getInputString("regTypeID")).intValue(), p, c);
 
 				r = crsApp.getRegistration(registrationID);
 				r.setRegisteredFirst(true);
 				r.update();
 				ctx.setSessionValue("registrationID",
 						String.valueOf(registrationID));
 				ctx.setSessionValue("spouseRegID", null);
 				ctx.setSessionValue("spouseRegTypeID", null);
 				editPersonDetails(ctx);
 			} else if (String.valueOf(r.getRegistrationTypeID()).equals(
 					ctx.getInputString("regTypeID"))
 					|| "existing".equals(ctx.getInputString("regTypeID"))) {
 
 				ctx.setSessionValue("regTypeID",
 						ctx.getInputString("regTypeID"));
 				ctx.setSessionValue("registrationID",
 						String.valueOf(r.getRegistrationID()));
 
 				if (r.getSpouseComing() == 2 && r.getSpouseRegistrationID() > 0) {
 					// if we know their spouse is coming go ahead and put the
 					// spouseregID in the session
 					ctx.setSessionValue("spouseRegID",
 							String.valueOf(r.getSpouseRegistrationID()));
 					ctx.setSessionValue("spouseRegTypeID",
 							String.valueOf(crsApp.getRegistration(
 									r.getSpouseRegistrationID())
 									.getRegistrationTypeID()));
 				} else {
 					ctx.setSessionValue("spouseRegID", null);
 					ctx.setSessionValue("spouseRegTypeID", null);
 				}
 				editPersonDetails(ctx);
 			} else {
 				ar.putValue(
 						"errorMsg",
 						"You have previously registered for this conference as a <b>"
 								+ r.getRegistrationType().getLabel()
 								+ "</b> and you are currently trying to login as a <b>"
 								+ crsApp.getRegistrationType(
 										ctx.getInputString("regTypeID"))
 										.getLabel()
 								+ "</b>. Please hit continue and choose existing registration to edit your information, or contact the conference administrator to delete your previous registration.");
 				ar.putValue("nextAction", "selectEvent");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			}
 		}
 	}
 
 	public void editPersonDetails(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults();
 			if (ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue("errorMsg", ERR_conferenceNotFound);
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getSessionValue("userLoggedIn") == null) {
 				ar.putValue("errorMsg", ERR_username);
 				ar.putValue("nextAction", "userLogin");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else {
 				if ("T".equals(ctx.getInputString("spouseReg"))
 						&& ctx.getSessionValue("spouseRegID") == null) {
 					askSpouseQuestions(ctx);
 				} else {
 					Registration r = resolveRegistration(ctx, false);
 					ar.putObject("registration", r);
 					ar.putObject(
 							"conference",
 							crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 					ar.putValue(
 							"kids",
 							String.valueOf(crsApp.countChildren(crsApp.getFirstRegistrationID(r))));
 
 					ctx.setReturnValue(ar);
 					ctx.goToView("editPersonDetails");
 				}
 			}
 		} catch (Exception e) {
 			goToErrorPage(ctx, e, "editPersonDetails");
 		}
 	}
 
 	public void askSpouseQuestions(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults();
 			if (ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue("errorMsg", ERR_conferenceNotFound);
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getSessionValue("userLoggedIn") == null) {
 				ar.putValue("errorMsg", ERR_username);
 				ar.putValue("nextAction", "userLogin");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else {
 				Registration r = crsApp.getRegistration((String) ctx.getSessionValue("registrationID"));
 				if (r.getPerson().getSpouseID() == 0) {
 					// Doesn't have a spouse
 					addSpouse(ctx);
 				} else {
 					// has a spouse
 					Conference c = crsApp.getConference((String) ctx.getSessionValue("selectedEvent"));
 					Registration spouse = new Registration();
 					spouse.setPersonID(r.getPerson().getSpouseID());
 					spouse.setConference(c);
 					spouse.select();
 					if (!spouse.isPKEmpty()) {
 						// spouse is registered already
 						r.setSpouseComing(2);
 						spouse.setSpouseComing(2);
 						r.setSpouseRegistrationID(spouse.getRegistrationID());
 						spouse.setSpouseRegistrationID(r.getRegistrationID());
 						r.setRegisteredFirst(false);
 						spouse.setRegisteredFirst(true);
 						r.update();
 						spouse.update();
 						ctx.setSessionValue("spouseRegID", String.valueOf(spouse.getRegistrationID()));
 						editPersonDetails(ctx);
 					} else {
 						Vector regTypesVector = new Vector();
 						Iterator regTypes = c.getRegistrationTypes().iterator();
 						while (regTypes.hasNext()) {
 							RegistrationType rt = (RegistrationType) regTypes.next();
 							Vector v = new Vector();
 							v.add(String.valueOf(rt.getRegistrationTypeID()));
 							v.add((String) rt.getLabel());
 							v.add((String) rt.getDescription());
 							regTypesVector.add(v);
 						}
 						ar.putObject("registration", resolveRegistration(ctx,
 								false));
 						ar.putObject("spouse", crsApp.getPerson(r.getPerson()
 								.getSpouseID()));
 						ar.putObject("conference", c);
 						ar.putObject("RegistrationTypes", regTypesVector);
 
 						ctx.setReturnValue(ar);
 						ctx.goToView("askSpouseQuestions");
 					}
 				}
 			}
 		} catch (Exception e) {
 			goToErrorPage(ctx, e, "askSpouseQuestions");
 		}
 	}
 
 	public void saveSpouseQuestions(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults();
 			if (ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue("errorMsg", ERR_conferenceNotFound);
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getSessionValue("userLoggedIn") == null) {
 				ar.putValue("errorMsg", ERR_username);
 				ar.putValue("nextAction", "userLogin");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else {
 				String registrationID = (String) ctx.getSessionValue("registrationID");
 				Registration r = crsApp.getRegistration(registrationID);
 				if (r.getSpouseComing() == 2) {
 					Conference c = crsApp.getConference((String) ctx.getSessionValue("selectedEvent"));
 					Person spouse = new Person(r.getPerson().getSpouseID());
 					Registration spouseReg = new Registration();
 					spouseReg.setConference(c);
 					spouseReg.setPersonID(spouse.getPersonID());
 					spouseReg.select();
 					// Check for unique spouses based on conferenceID and
 					// personID
 					if (spouseReg.isPKEmpty()) {
 						spouseReg.setRegistrationDate(new Date());
 						spouseReg.setSpouseComing(2);
 						spouseReg.setSpouseRegistrationID(r.getRegistrationID());
 						//					spouseReg.setRegistrationTypeOld(crsApp.getRegistrationType(ctx.getInputString("spouseRegTypeID")).getLabel());
 						spouseReg.setRegistrationTypeID(Integer.valueOf(
 								ctx.getInputString("spouseRegTypeID"))
 								.intValue());
 						spouseReg.setSpouseRegistrationID(r.getRegistrationID());
 						spouseReg.setRegisteredFirst(false);
 						spouseReg.insert();
 						r.setSpouseComing(2);
 						r.setSpouseRegistrationID(spouseReg.getRegistrationID());
 						r.update();
 						ctx.setSessionValue("spouseRegID",
 								String.valueOf(spouseReg.getRegistrationID()));
 
 						editPersonDetails(ctx);
 					} else {
 						ar.putValue("errorMsg",
 								"Your spouse has already registered for this conference!");
 						ar.putObject(
 								"conference",
 								crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 	
 						ar.putValue("nextAction", "editPersonDetails");
 						ctx.setReturnValue(ar);
 						ctx.goToView("error");
 
 					}
 				} else {
 					ar.putValue("errorMsg",
 							"You shouldn't be here your spouse isn't coming!");
 					ar.putObject(
 							"conference",
 							crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 
 					ar.putValue("nextAction", "editPersonDetails");
 					ctx.setReturnValue(ar);
 					ctx.goToView("error");
 				}
 			}
 		} catch (Exception e) {
 			goToErrorPage(ctx, e, "saveSpouseQuestions");
 		}
 	}
 
 	public void saveSpouseDetails(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults();
 			if (ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue("errorMsg", ERR_conferenceNotFound);
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getSessionValue("userLoggedIn") == null) {
 				ar.putValue("errorMsg", ERR_username);
 				ar.putValue("nextAction", "userLogin");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else {
 				String spouseRegType = ctx.getInputString("spouseRegType");
 				int spouseRegTypeID = Integer.valueOf(
 						ctx.getInputString("spouseRegTypeID")).intValue();
 				String registrationID = (String) ctx.getSessionValue("registrationID");
 				Registration r = crsApp.getRegistration(registrationID);
 				if (r.getSpouseComing() != 2) {
 					Conference c = crsApp.getConference((String) ctx.getSessionValue("selectedEvent"));
 					if (!"not".equals(spouseRegType)) {
 						Person spouse = new Person(r.getPerson().getSpouseID());
 						Registration spouseReg = new Registration();
 						//						spouseReg.setRegistrationTypeOld(crsApp.getRegistrationType(spouseRegType).getLabel());
 						spouseReg.setRegistrationTypeID(spouseRegTypeID);
 						spouseReg.setRegistrationDate(new Date());
 						spouseReg.setConference(c);
 						spouseReg.setPerson(spouse);
 						spouseReg.setSpouseComing(2);
 						spouseReg.setSpouseRegistrationID(r.getRegistrationID());
 						spouseReg.insert();
 						r.setSpouseComing(2);
 						r.update();
 						ctx.setSessionValue("spouseRegID",
 								String.valueOf(spouseReg.getRegistrationID()));
 					} else {
 						r.setSpouseComing(1);
 						r.update();
 						ctx.setSessionValue("spouseRegID", null);
 					}
 				}
 				listQuestions(ctx);
 			}
 		} catch (Exception e) {
 			goToErrorPage(ctx, e, "saveSpouseDetails");
 		}
 	}
 
 	public void savePersonDetails(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults();
 			if (ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue("errorMsg", ERR_conferenceNotFound);
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getSessionValue("userLoggedIn") == null) {
 				ar.putValue("errorMsg", ERR_username);
 				ar.putValue("nextAction", "userLogin");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else {
 				boolean saveRegistration = true;
 				boolean doUpdate = false;
 				Registration r = resolveRegistration(ctx, true);
 				Conference c = crsApp.getConference((String) ctx.getSessionValue("selectedEvent"));
 
 				RegistrationType rt = r.getRegistrationType();
 				Hashtable req = ctx.getHashedRequest();
 				if (rt.decodeProfile(10) >= 1) {
 					Hashtable dates = new Hashtable();
 					if (req.get("ArriveDate") != null)
 						dates.put("ArriveDate", req.get("ArriveDate"));
 					if (req.get("LeaveDate") != null)
 						dates.put("LeaveDate", req.get("LeaveDate"));
 					r.setMappedValues(dates);
 					doUpdate = true;
 				} else { /* we did not ask for dates so set the defaults */
 					Hashtable dates = new Hashtable();
 					if (rt.getDefaultDateArrive()!=null)
 						dates.put("ArriveDate", rt.getDefaultDateArrive());
 					if (rt.getDefaultDateLeave()!=null)
 						dates.put("LeaveDate", rt.getDefaultDateLeave());
 					r.setMappedValues(dates);
 					doUpdate = true;
 				}
 
 				if (rt.getAskSpouse() && r.isRegisteredFirst()){
 			
 					int spouseComing = ctx.getInputString("SpouseComing") == null ? 1
 							: Integer.parseInt(ctx.getInputString("SpouseComing"));
 					
 					if (r.getSpouseComing() == 0 || r.getSpouseComing() == 1){	// if spouse is not coming or not indicated yet
 						if ( r.getSpouseComing() == 1 && spouseComing == 2) { // Change from spouse not coming to spouse coming
 							crsApp.checkSinglePaymentRemoval(r);
 							crsApp.checkSingleDiscountRemoval(r);
 						}
 						r.setSpouseComing(spouseComing);
 						doUpdate = true;
 					}
 					else if(r.getSpouseComing() == 2 && r.getSpouseRegistrationID()==0) // if spouse is previously indicated as coming but not registered yet 
 					{
 						// maybe only if (spouseComing == 1)  ?
 						// remove married payment
 						//crsApp.checkMarriedPaymentRemoval(r,String.valueOf(r.getSpouseRegistrationID()));
 						
 						r.setSpouseComing(spouseComing);
 						doUpdate = true;
 					}
 				}
 				if (doUpdate)
 					saveRegistration = r.update();
 				if (saveRegistration && crsApp.savePerson(req)) {
 					boolean kids = ctx.getInputString("kids") != null
 							&& !"0".equals(ctx.getInputString("kids"));
 					boolean askSpouse = "M".equals(ctx.getInputString("MaritalStatus"))
 							&& rt.getAskSpouse();
 					if ((String.valueOf(r.getRegistrationID())).equals(ctx.getSessionValue("spouseRegID")))
 						listQuestions(ctx);
 					else if (kids && r.isRegisteredFirst())
 						editChildRegistrations(ctx);
 					else
 						listQuestions(ctx);
 				} else {
 					ar.putValue("errorMsg",
 							"There was a problem saving your information, please try again.");
 					ar.putObject(
 							"conference",
 							crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 
 					ar.putValue("nextAction", "editPersonDetails");
 					ctx.setReturnValue(ar);
 					ctx.goToView("error");
 				}
 			}
 		} catch (Exception e) {
 			goToErrorPage(ctx, e, "savePersonDetails");
 		}
 	}
 
 	public void listQuestions(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults();
 			if (ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue("errorMsg", ERR_conferenceNotFound);
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getSessionValue("userLoggedIn") == null) {
 				ar.putValue("errorMsg", ERR_username);
 				ar.putValue("nextAction", "userLogin");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else {
 				if ("T".equals(ctx.getInputString("spouseReg"))
 						&& ctx.getSessionValue("spouseRegID") == null) {
 					askSpouseQuestions(ctx);
 				} else {
 					Registration r = resolveRegistration(ctx, false);
 
 					ar.addCollection("Questions", crsApp.listQuestions(
 							String.valueOf(r.getConferenceID()),
 							r.getRegistrationTypeID(), "displayOrder", "ASC"));
 
 					ar.putObject("registration", r);
 
 					ar.putObject(
 							"conference",
 							crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 
 					ar.addCollection(
 							"Answers",
 							crsApp.listRegistrationAnswers(r.getRegistrationID()));
 
 					ctx.setReturnValue(ar);
 
 					ctx.goToView("listQuestions");
 				}
 			}
 		} catch (Exception e) {
 			goToErrorPage(ctx, e, "listQuestions");
 		}
 	}
 
 	public void editChildRegistrations(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults();
 			if (ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue("errorMsg", ERR_conferenceNotFound);
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getSessionValue("userLoggedIn") == null) {
 				ar.putValue("errorMsg", ERR_username);
 				ar.putValue("nextAction", "userLogin");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else {
 				if (ctx.getSessionValue("registrationID") != null) {
 					Registration r = resolveRegistration(ctx, false);
 
 					if (!r.isRegisteredFirst())
 						r = r.getSpouse();
 
 					ar.putObject("registration", r);
 
 					ar.putObject(
 							"conference",
 							crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 
 					ar.addCollection(
 							"childRegistrations",
 							crsApp.listChildRegistrations(r.getRegistrationID()));
 
 					if ("".equals(ctx.getInputString("kids")))
 						ar.putValue("kids", "10");
 					else
 						ar.putValue("kids", ctx.getInputString("kids"));
 
 					ctx.setReturnValue(ar);
 
 					ctx.goToView("editChildRegistrations");
 				} else {
 					ar.putValue(
 							"errorMsg",
 							"The registration could not located, please try again or start over by hitting continue.");
 					ar.putObject(
 							"conference",
 							crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 
 					ar.putValue("nextAction", "listRegistrations");
 					ctx.setReturnValue(ar);
 					ctx.goToView("error");
 				}
 			}
 		} catch (Exception e) {
 			goToErrorPage(ctx, e, "editChildRegistrations");
 		}
 	}
 
 	public void saveChildRegistrations(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults();
 			if (ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue("errorMsg", ERR_conferenceNotFound);
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getSessionValue("userLoggedIn") == null) {
 				ar.putValue("errorMsg", ERR_username);
 				ar.putValue("nextAction", "userLogin");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else {
 				if (ctx.getSessionValue("registrationID") != null) {
 					Registration r = getFirstRegistration(ctx);
 					r.setAdditionalRooms(Integer.parseInt(ctx.getInputString("AdditionalRooms")));
 
 					r.update();
 					Conference c = crsApp.getConference((String) ctx.getSessionValue("selectedEvent"));
 
 					if (!"".equals(ctx.getInputString("kids"))) {
 						int kids = Integer.parseInt(ctx.getInputString("kids"));
 						for (int i = 0; i < kids; i++) {
 							if ("true".equals(ctx.getInputString("Remove"
 									+ String.valueOf(i)))) {
 								crsApp.deleteChildRegistration(ctx.getInputString("ChildRegistrationID"
 										+ String.valueOf(i)));
 							} else {
 								Hashtable saveIt = new Hashtable();
 								saveIt.put(
 										"ChildRegistrationID",
 										ctx.getInputString("ChildRegistrationID"
 												+ String.valueOf(i)));
 								saveIt.put("FirstName",
 										ctx.getInputString("FirstName"
 												+ String.valueOf(i)));
 								saveIt.put("LastName",
 										ctx.getInputString("LastName"
 												+ String.valueOf(i)));
 								saveIt.put("Gender",
 										ctx.getInputString("Gender"
 												+ String.valueOf(i)));
 								saveIt.put("ArriveDate",
 										ctx.getInputString("ArriveDate"
 												+ String.valueOf(i)));
 								saveIt.put("LeaveDate",
 										ctx.getInputString("LeaveDate"
 												+ String.valueOf(i)));
 								saveIt.put("BirthDate",
 										ctx.getInputString("BirthDate"
 												+ String.valueOf(i)));
 								saveIt.put(
 										"InChildCare",
 										"true".equals(ctx.getInputString("ChildCare"
 												+ String.valueOf(i))) ? "true"
 												: "false");
 								saveIt.put("RegistrationID", new Integer(
 										r.getRegistrationID()));
 
 								crsApp.saveChildRegistration(saveIt);
 							}
 							
 						}
 					}
 					listQuestions(ctx);
 				} else {
 					ar.putValue(
 							"errorMsg",
 							"The registration could not located, please try again or start over by hitting continue.");
 					ar.putObject(
 							"conference",
 							crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 
 					ar.putValue("nextAction", "listRegistrations");
 					ctx.setReturnValue(ar);
 					ctx.goToView("error");
 				}
 			}
 		} catch (Exception e) {
 			goToErrorPage(ctx, e, "saveChildRegistrations");
 		}
 	}
 
 	public void saveRegistrationQuestions(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults();
 			if (ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue("errorMsg", ERR_conferenceNotFound);
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getSessionValue("userLoggedIn") == null) {
 				ar.putValue("errorMsg", ERR_username);
 				ar.putValue("nextAction", "userLogin");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else {
 				Registration r = resolveRegistration(ctx, true);
 
 				Collection questions = new Vector();
 
 				questions = crsApp.listQuestions(
 						(String) ctx.getSessionValue("selectedEvent"),
 						r.getRegistrationTypeID(), "displayOrder", "ASC");
 
 				Iterator qi = questions.iterator();
 
 				while (qi.hasNext()) {
 					Question q = (Question) qi.next();
 					if (!q.getAnswerType().equals("divider")
 							&& !q.getAnswerType().equals("info")
 							&& !q.getAnswerType().equals("hide")) {
 						Hashtable values = new Hashtable();
 						String answer="";
 						if (q.getAnswerType().equals("textL")){	//paragraphs have a "t" preface
 							answer = ctx.getInputString("t"+String.valueOf(q.getQuestionID()));
 						} else {
 							answer = ctx.getInputString(String.valueOf(q.getQuestionID()));
 						}
 						values.put("QuestionID", String.valueOf(q.getQuestionID()));
 						answer = answer == null ? "" : answer;
 						if (q.getAnswerType().equals("checkbox"))
 							answer = "Yes".equals(answer) ? "Yes" : "No";
 						values.put("Body", answer);
 						
 						values.put("RegistrationID",
 								String.valueOf(r.getRegistrationID()));
 						crsApp.saveAnswer(values);
 						
 						if (q.getQuestionTextID() == 2) {
 							r.setIsOnsite(answer.equals("Y") ? true : false);
 							r.persist();
 						}
 					}
 				}
 				listMerchandise(ctx);
 
 			}
 		} catch (Exception e) {
 			goToErrorPage(ctx, e, "saveRegistrationQuestions");
 		}
 	}
 
 	public void listMerchandise(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults();
 			if (ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue("errorMsg", ERR_conferenceNotFound);
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getSessionValue("userLoggedIn") == null) {
 				ar.putValue("errorMsg", ERR_username);
 				ar.putValue("nextAction", "userLogin");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else {
 				if ("T".equals(ctx.getInputString("spouseReg"))
 						&& ctx.getSessionValue("spouseRegID") == null) {
 					askSpouseQuestions(ctx);
 				} else {
 					Registration r = resolveRegistration(ctx, false);
 
 					ar.putObject("registration", r);
 
 					ar.putObject(
 							"conference",
 							crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 
 					ar.addCollection("choices",
 							crsApp.listRegistrationMerchandise(
 									r.getRegistrationID(), "displayOrder",
 									"ASC"));
 
 					ar.addCollection("merchandise", crsApp.listMerchandise(
 							String.valueOf(r.getConferenceID()),
 							r.getRegistrationTypeID(), "displayOrder", "ASC"));
 
 					ctx.setReturnValue(ar);
 
 					ctx.goToView("listMerchandise");
 				}
 			}
 		} catch (Exception e) {
 			goToErrorPage(ctx, e, "listMerchandise");
 		}
 	}
 
 	public void saveRegistrationMerchandise(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults();
 			if (ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue("errorMsg", ERR_conferenceNotFound);
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getSessionValue("userLoggedIn") == null) {
 				ar.putValue("errorMsg", ERR_username);
 				ar.putValue("nextAction", "userLogin");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else {
 				Registration r = resolveRegistration(ctx, true);
 
 				MerchandiseChoice mc = new MerchandiseChoice();
 				mc.setRegistrationID(r.getRegistrationID());
 				mc.delete();
 
 				if (ctx.getInputStringArray("choices") != null) {
 					String[] choices = ctx.getInputStringArray("choices");
 
 					for (int i = 0; i < choices.length; i++) {
 						Merchandise m = crsApp.getMerchandise(choices[i]);
 						r.assocMerchandise(m);
 					}
 				}
 
 				/*
 				 * done with registration. If spouse coming now ask her
 				 * questions...
 				 */
 				if (r.getSpouseComing() == 2
 						&& (ctx.getSessionValue("spouseRegID") == null || String.valueOf(
 								r.getSpouseRegistrationID())
 								.equals(
 										(String) ctx.getSessionValue("spouseRegID"))))
 					editPersonDetails(ctx);
 				else
 					reviewPayments(ctx);
 			}
 		} catch (Exception e) {
 			goToErrorPage(ctx, e, "editMerchandise");
 		}
 	}
 
 	public void reviewPayments(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults();
 			if (ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue("errorMsg", ERR_conferenceNotFound);
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getSessionValue("userLoggedIn") == null) {
 				ar.putValue("errorMsg", ERR_username);
 				ar.putValue("nextAction", "userLogin");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getSessionValue("registrationID") == null) {// make sure we can load their registration
 				ar.putValue(
 						"errorMsg",
 						"The registration could not located, please try again or start over by hitting continue.");
 				ar.putValue("nextAction", "listRegistrations");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			}
 			else
 			{
 				String registrationID = (String) ctx.getSessionValue("registrationID");
 
 				Registration r = crsApp.getRegistration(registrationID);
 				
 				// check to see if they are ready to pay
 				// results:  "" means ready to pay
 				// 			 "vitals" means they need to edit their personal details
 				//			 "questions" means they need to answer some required question
 				String checkMsg= crsApp.checkReadyToPay(registrationID);
 				
 				if(!"".equals(checkMsg))
 				{
 					if("vitals".equals(checkMsg))
 					{
 						ar.putValue(
 								"errorMsg",
 								"You must enter your personal information (Your full name, email address, and gender) before you may make a payment. Click continue to edit your profile.");
 						ar.putValue("nextAction", "editPersonDetails");
 						
 					}
 					else if("questions".equals(checkMsg))
 					{
 						ar.putValue(
 								"errorMsg",
 								"You must answer all required questions before you may make a payment. Click continue to go to the questions page.");
 					
 						ar.putValue("nextAction", "listQuestions");
 						
 						
 					}	
 					ar.putObject(
 							"conference",
 							crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 
 					
 					ctx.setReturnValue(ar);
 					ctx.goToView("error");
 				}
 				else
 				{
 					if (r.getSpouseComing() == 2
 							&& r.getSpouseRegistrationID() == 0) {
 						askSpouseQuestions(ctx);
 					} else {
 						ar.putObject("registration", r);
 						if (r.getSpouseComing() == 2) {
 							ar.putObject("spouseRegistration",
 									crsApp.getRegistration(r.getSpouseRegistrationID()));
 							ar.addCollection("payments",
 									crsApp.listRegistrationPaymentsForRegistrationWithSpouse(
 											registrationID,
 											String.valueOf(r.getSpouseRegistrationID()),
 											"paymentDate", "ASC"));
 							ar.addHashtable("discountsAvailable",
 									crsApp.getDiscountsAvailableWithSpouse(
 											registrationID,
 											String.valueOf(r.getSpouseRegistrationID()),
 											new Date()));
 						} else {
 							ar.addCollection(
 									"payments",
 									crsApp.listRegistrationPaymentsForRegistration(
 											registrationID, "paymentDate",
 											"ASC"));
 							ar.addHashtable("discountsAvailable",
 									crsApp.getDiscountsAvailable(
 											registrationID, new Date()));
 						}
 
 						ar.putObject(
 								"conference",
 								crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 
 						ctx.setReturnValue(ar);
 
 						ctx.goToView("reviewPayments");
 					}
 				}
 			}
 		} catch (Exception e) {
 			goToErrorPage(ctx, e, "reviewPayments");
 		}
 	}
 
 	public void getPaymentInfo(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults();
 			if (ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue("errorMsg", ERR_conferenceNotFound);
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getSessionValue("userLoggedIn") == null) {
 				ar.putValue("errorMsg", ERR_username);
 				ar.putValue("nextAction", "userLogin");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if ((ctx.getInputString("staffEmail") == null || ctx.getInputString("staffEmail").equals(""))
 					&& (ctx.getInputString("PaymentMethod")!=null && (ctx.getInputString("PaymentMethod").equals(
 							"Scholarship") || ctx.getInputString(
 							"PaymentMethod").equals("ministry_transfer")))) {
 				findStaff(ctx);
 			} else {
 				if (ctx.getSessionValue("registrationID") != null) {
 					String registrationID = (String) ctx.getSessionValue("registrationID");
 					Registration r = crsApp.getRegistration(registrationID);
 					
 					if(ctx.getInputString("PaymentMethod")==null){
 						ar.putValue("errorMsg",
 								"You must select a payment method. Hit continue to try again.");
 						ar.putObject(
 								"conference",
 								crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 	
 						ar.putValue("nextAction", "reviewPayments");
 						ctx.setReturnValue(ar);
 						ctx.goToView("error");
 					}
 					else
 					{
 						ar.putValue("PaymentMethod",
 								ctx.getInputString("PaymentMethod"));
 						if (ctx.getInputString("PaymentAmount").equals("Other")) {
 							ar.putValue("PaymentAmount",
 									ctx.getInputString("PaymentAmountOther"));
 						} else {
 							ar.putValue("PaymentAmount",
 									ctx.getInputString("PaymentAmount"));
 						}
 	
 						ar.addHashtable("DiscountsAvailable",
 								crsApp.getDiscountsAvailable(registrationID,
 										new Date()));
 						ar.addHashtable("AccountSummary",
 								crsApp.getAccountSummary(registrationID));
 	
 						ar.putObject("registration", r);
 	
 						ar.putObject(
 								"conference",
 								crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 	
 						ctx.setReturnValue(ar);
 	
 						if (ctx.getInputString("PaymentMethod").equals("Check")) {
 							ctx.goToView("paymentByCheck");
 						} else if (ctx.getInputString("PaymentMethod").equals(
 								"Credit Card")) {
 							ctx.goToView("paymentByCreditCard");
 						} else if (ctx.getInputString("PaymentMethod").equals(
 								"staff_transfer")) {
 							Person p = r.getPerson();
 							ar.putValue("AccountNumber", p.getAccountNo());
 							ctx.goToView("paymentByAccountTransfer");
 						} else if (ctx.getInputString("PaymentMethod").equals(
 								"ministry_transfer")) {
 							ar.putValue("staffEmail",
 									ctx.getInputString("staffEmail"));
 							ar.putValue("staffName",
 									ctx.getInputString("staffName"));
 	
 							ctx.goToView("paymentByMinistryAccountTransfer");
 						} else if (ctx.getInputString("PaymentMethod").equals(
 								"Echeck")) {
 							ctx.goToView("paymentByECheck");
 						} else if (ctx.getInputString("PaymentMethod").equals(
 								"Scholarship")) {
 							ar.putValue("staffEmail",
 									ctx.getInputString("staffEmail"));
 							ar.putValue("staffName",
 									ctx.getInputString("staffName"));
 							ctx.goToView("paymentByScholarship");
 						} else {
 							ar.putValue("errorMsg",
 									"You must select a payment method. Hit continue to try again.");
 							ar.putValue("nextAction", "reviewPayments");
 							ar.putObject(
 									"conference",
 									crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 		
 							ctx.setReturnValue(ar);
 							ctx.goToView("error");
 						}
 					}
 				} else {
 					ar.putValue(
 							"errorMsg",
 							"The registration could not located, please try again or start over by hitting continue.");
 					ar.putValue("nextAction", "listRegistrations");
 					ctx.setReturnValue(ar);
 					ctx.goToView("error");
 				}
 			}
 		} catch (Exception e) {
 			goToErrorPage(ctx, e, "userPaymentInfo");
 		}
 	}
 
 	public void confirmPayment(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults();
 			if (ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue("errorMsg", ERR_conferenceNotFound);
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getSessionValue("userLoggedIn") == null) {
 				ar.putValue("errorMsg", ERR_username);
 				ar.putValue("nextAction", "userLogin");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else {
 				if (ctx.getSessionValue("registrationID") != null) {
 					String registrationID = (String) ctx.getSessionValue("registrationID");
 
 					ar.putValue("PaymentMethod",
 							ctx.getInputString("PaymentMethod"));
 					if ("Scholarship".equals(ctx.getInputString("PaymentMethod")))
 						ar.putValue("staffName",
 								ctx.getInputString("staffName"));
 					if ("staff_transfer".equals(ctx.getInputString("PaymentMethod"))) {
 						if (!(ctx.getInputString("AccountNumber") == null || "".equals(ctx.getInputString("AccountNumber")))) {
 							/*
 							 * TODO!! verify that this is a legitimate account
 							 * number
 							 */
 
 						} else { //redirect
 						}
 
 						ar.putValue("AccountNumber",
 								ctx.getInputString("AccountNumber"));
 						ar.putValue("Comments", ctx.getInputString("Comments"));
 					}
 					if ("ministry_transfer".equals(ctx.getInputString("PaymentMethod"))) {
 						ar.putValue("Ministry", ctx.getInputString("Ministry"));
 						ar.putValue("BU", ctx.getInputString("BU"));
 						ar.putValue("OU", ctx.getInputString("OU"));
 						ar.putValue("Dept", ctx.getInputString("Dept"));
 						ar.putValue("Project", ctx.getInputString("Project"));
 						ar.putValue("staffName",
 								ctx.getInputString("staffName"));
 						ar.putValue("staffEmail",
 								ctx.getInputString("staffEmail"));
 					}
 
 					Registration r = crsApp.getRegistration(registrationID);
 					ar.putObject("registration", r);
 
 					ar.putObject(
 							"conference",
 							crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 
 					ar.addHashtable("PaymentInfo", ctx.getHashedRequest());
 
 					ctx.setReturnValue(ar);
 					ctx.goToView("confirmPayment");
 				} else {
 					ar.putValue(
 							"errorMsg",
 							"The registration could not located, please try again or start over by hitting continue.");
 					ar.putValue("nextAction", "listRegistrations");
 					ctx.setReturnValue(ar);
 					ctx.goToView("error");
 				}
 			}
 		} catch (Exception e) {
 			goToErrorPage(ctx, e, "userPaymentConfirm");
 		}
 	}
 
 	public void reviewRegistration(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults();
 			if (ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue("errorMsg", ERR_conferenceNotFound);
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getSessionValue("userLoggedIn") == null) {
 				ar.putValue("errorMsg", ERR_username);
 				ar.putValue("nextAction", "userLogin");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getSessionValue("registrationID") == null){
 				ar.putValue(
 						"errorMsg",
 						"The registration could not located, please try again or start over by hitting continue.");
 				ar.putValue("nextAction", "listRegistrations");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			}
 			else
 			{
 				String registrationID = (String) ctx.getSessionValue("registrationID");
 				
 				Registration r = crsApp.getRegistration(registrationID);
 
 				// check to see if they are ready to pay
 				// results:  "" means ready to pay
 				// 			 "vitals" means they need to edit their personal details
 				//			 "questions" means they need to answer some required question
 				String checkMsg= crsApp.checkReadyToPay(registrationID);
 				
 				if(!"".equals(checkMsg))
 				{
 					if("vitals".equals(checkMsg))
 					{
 						ar.putValue(
 								"errorMsg",
 								"You must first enter your personal information (Your full name, email address, and gender). Click continue to edit your profile.");
 						ar.putValue("nextAction", "editPersonDetails");
 						
 					}
 					else// if("questions".equals(checkMsg))
 					{
 						ar.putValue(
 								"errorMsg",
 								"You must first answer all required questions. Click continue to go to the questions page.");
 						ar.putValue("nextAction", "listQuestions");
 						
 					}	
 					ar.putObject(
 							"conference",
 							crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 
 					ctx.setReturnValue(ar);
 					ctx.goToView("error");
 				}
 				else
 				{
 					if (r.getSpouseComing() == 2
 							&& r.getSpouseRegistrationID() == 0) 
 					{
 						askSpouseQuestions(ctx);
 					} 
 					else 
 					{
 						if (r.getSpouseComing() == 2) 
 						{
 							crsApp.updatePaymentsWithSpouse(registrationID, String.valueOf(r.getSpouseRegistrationID()));
 							crsApp.updatePreRegistered(registrationID);
 							crsApp.updatePreRegistered(String.valueOf(r.getSpouseRegistrationID()));
 						}
 						else 
 						{
 							crsApp.updatePayments(registrationID);
 							crsApp.updatePreRegistered(registrationID);
 						}
 					}
 					
 					ar.putObject("registration",crsApp.getRegistration(registrationID));
 
 					ar.putObject(
 							"conference",
 							crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 
 					ar.addHashtable("AccountSummary",
 							crsApp.getAccountSummary(registrationID));
 
 					ctx.setReturnValue(ar);
 
 					ctx.goToView("reviewRegistration");
 				}
 			}
 		} catch (Exception e) {
 			goToErrorPage(ctx, e, "reviewRegistration");
 		}
 	}
 
 	public void processCredtitCardPayment(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults();
 			if (ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue("errorMsg", ERR_conferenceNotFound);
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getSessionValue("userLoggedIn") == null) {
 				ar.putValue("errorMsg", ERR_username);
 				ar.putValue("nextAction", "userLogin");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else {
 
 				Hashtable paymentInfo = ctx.getHashedRequest();
 				String registrationID = (String) ctx.getSessionValue("registrationID");
 
 				Hashtable payment = crsApp.paymentCreditCard(registrationID,
 						paymentInfo);
 
 				boolean succeed = payment.get("Status").equals("Success");
 
 				String ccNum = (String) paymentInfo.get("CCNum");
 				paymentInfo.put("CCNum", "****"
 						+ ccNum.substring(ccNum.length() - 4, ccNum.length()));
 
 				Registration r = crsApp.getRegistration(registrationID);
 				ar.putObject("registration", r);
 				/*
 				 * if (r.getRegistrationType().equals("staff"))
 				 * ar.putObject("registration",
 				 * crsApp.getStaffRegistration(registrationID)); else if
 				 * (r.getRegistrationType().equals("student"))
 				 * ar.putObject("registration",
 				 * crsApp.getStudentRegistration(registrationID)); else if
 				 * (r.getRegistrationType().equals("guest"))
 				 * ar.putObject("registration",
 				 * crsApp.getGuestRegistration(registrationID));
 				 */
 				ar.addHashtable("paymentInfo", paymentInfo);
 				ar.putObject(
 						"conference",
 						crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 
 				if (succeed) {
 					ar.putObject(
 							"payment",
 							crsApp.getPayment((String) payment.get("PaymentID")));
 					ctx.setReturnValue(ar);
 					ctx.goToView("paymentSuccess");
 				} else {
 					ar.putValue("errorMsg", payment.get("ErrorMessage")
 							.toString());
 					ctx.setReturnValue(ar);
 					ctx.goToView("paymentFailure");
 				}
 
 			}
 		} catch (Exception e) {
 			goToErrorPage(ctx, e, "userPaymentProcessCredtitCard");
 		}
 	}
 
 	//TODO: This function is stubbed out, but not done.
 	public void userPaymentProcessECheck(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults();
 			if (ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue("errorMsg", ERR_conferenceNotFound);
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getSessionValue("userLoggedIn") == null) {
 				ar.putValue("errorMsg", ERR_username);
 				ar.putValue("nextAction", "userLogin");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else {
 				//				CRSInfo crsinfo = new CRSInfo();
 				Hashtable paymentInfo = ctx.getHashedRequest();
 
 				String bankABACode = (String) paymentInfo.get("bankABACode");
 				String codedBankABACode = "****"
 						+ bankABACode.substring(bankABACode.length() - 4,
 								bankABACode.length());
 				String bankAcctNum = (String) paymentInfo.get("bankAcctNum");
 				String codedBankAcctNum = "****"
 						+ bankAcctNum.substring(bankAcctNum.length() - 4,
 								bankAcctNum.length());
 
 				String registrationID = (String) ctx.getSessionValue("registrationID");
 				log.debug("getRemoteAddr: "
 						+ ctx.getRequest().getRemoteAddr());
 				log.debug("getRemoteHost: "
 						+ ctx.getRequest().getRemoteHost());
 				paymentInfo.put("customerIP", ctx.getRequest().getRemoteHost());
 				//				Hashtable payment = crsApp.paymentECheck(regID, paymentInfo);
 
 				paymentInfo.put("bankABACode", codedBankABACode);
 				paymentInfo.put("bankAcctNum", codedBankAcctNum);
 
 				crsApp.updatePreRegistered(registrationID);
 				//				boolean succeed = payment.get("Status").equals("Success");
 				ar.putValue("RegistrationID", registrationID);
 				ar.addHashtable("PaymentInfo", paymentInfo);
 				//				ar.addHashtable("Payment", payment);
 				//				results.addHashtable("Person",
 				// crsApp.getPersonByRegistrationID(regID));
 				ar.putObject(
 						"Conference",
 						crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 				ctx.setReturnValue(ar);
 				if (true) { //TODO: change
 					ctx.goToView("userPaymentSuccess");
 				} else {
 					ctx.goToView("userPaymentFailure");
 				}
 			}
 		} catch (Exception e) {
 			goToErrorPage(ctx, e, "userPaymentProcessECheck");
 		}
 	}
 
 	public void processAccountTransferPayment(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults();
 			if (ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue("errorMsg", ERR_conferenceNotFound);
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getSessionValue("userLoggedIn") == null) {
 				ar.putValue("errorMsg", ERR_username);
 				ar.putValue("nextAction", "userLogin");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else {
 
 				Hashtable paymentInfo = ctx.getHashedRequest();
 				String registrationID = (String) ctx.getSessionValue("registrationID");
 				Registration r = crsApp.getRegistration(registrationID);
 				Hashtable payment = crsApp.paymentStaffAccountTransfer(
 						registrationID, paymentInfo);
 
 				ar.putObject("registration",
 						crsApp.getRegistration(registrationID));
 				ar.putValue("RegistrationID", registrationID);
 				ar.addHashtable("paymentInfo", paymentInfo);
 				ar.putObject(
 						"conference",
 						crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 				crsApp.sendAccountTransferEmail(registrationID,
 						(String) ctx.getSessionValue("selectedEvent"),
 						ctx.getInputString("AccountNumber"),
 						// need to put the email address of the account owner
 						// here ->
 						ctx.getInputString("Comment"),
 						ctx.getInputString("PaymentAmt"));
 				ar.putObject("payment",
 						crsApp.getPayment((String) payment.get("PaymentID")));
 				ctx.setReturnValue(ar);
 				ctx.goToView("paymentSuccess");
 			}
 		} catch (Exception e) {
 			goToErrorPage(ctx, e, "userPaymentProcessScholarship");
 		}
 	}
 
 	public void processMinistryTransferPayment(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults();
 			if (ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue("errorMsg", ERR_conferenceNotFound);
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getSessionValue("userLoggedIn") == null) {
 				ar.putValue("errorMsg", ERR_username);
 				ar.putValue("nextAction", "userLogin");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else {
 
 				Hashtable paymentInfo = ctx.getHashedRequest();
 				String registrationID = (String) ctx.getSessionValue("registrationID");
 				Hashtable payment = crsApp.paymentMinistryAccountTransfer(
 						registrationID, paymentInfo);
 
 				ar.putObject("registration",
 						crsApp.getRegistration(registrationID));
 				ar.putValue("RegistrationID", registrationID);
 				ar.addHashtable("paymentInfo", paymentInfo);
 				ar.putObject(
 						"conference",
 						crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 				crsApp.sendMinistryAccountTransferEmail(registrationID,
 						(String) ctx.getSessionValue("selectedEvent"),
 						paymentInfo);
 				ar.putObject("payment",
 						crsApp.getPayment((String) payment.get("PaymentID")));
 				ctx.setReturnValue(ar);
 				ctx.goToView("paymentSuccess");
 			}
 		} catch (Exception e) {
 			goToErrorPage(ctx, e, "userPaymentProcessScholarship");
 		}
 	}
 
 	public void processScholarshipPayment(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults();
 			if (ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue("errorMsg", ERR_conferenceNotFound);
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getSessionValue("userLoggedIn") == null) {
 				ar.putValue("errorMsg", ERR_username);
 				ar.putValue("nextAction", "userLogin");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else {
 
 				Hashtable paymentInfo = ctx.getHashedRequest();
 				String registrationID = (String) ctx.getSessionValue("registrationID");
 				Hashtable payment = crsApp.paymentScholarship(registrationID,
 						paymentInfo);
 
 				ar.putValue("RegistrationID", registrationID);
 				ar.addHashtable("paymentInfo", paymentInfo);
 				ar.putObject("registration",
 						crsApp.getRegistration(registrationID));
 				ar.putObject(
 						"conference",
 						crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 				crsApp.sendScholarshipEmail(registrationID,
 						(String) ctx.getSessionValue("selectedEvent"),
 						ctx.getInputString("staffName"),
 						ctx.getInputString("Comment"),
 						ctx.getInputString("PaymentAmt"));
 				ar.putObject("payment",
 						crsApp.getPayment((String) payment.get("PaymentID")));
 				ctx.setReturnValue(ar);
 				ctx.goToView("paymentSuccess");
 			}
 		} catch (Exception e) {
 			goToErrorPage(ctx, e, "userPaymentProcessScholarship");
 		}
 	}
 
 	/* for scholarships and ministry account transfers */
 	public void findStaff(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults();
 			if (ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue("errorMsg", ERR_conferenceNotFound);
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getSessionValue("userLoggedIn") == null) {
 				ar.putValue("errorMsg", ERR_username);
 				ar.putValue("nextAction", "userLogin");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else {
 				String nameSubString;
 				nameSubString = ctx.getInputString("name") == null ? ""
 						: ctx.getInputString("name");
 				nameSubString = TextUtils.formatApostrophe(nameSubString);
 				ar.putValue("nameSubString", nameSubString);
 				log.debug("listStaff substring: " + nameSubString);
 
 				Collection list = new Vector();
 				if (nameSubString.length() > 0) {
 					/*
 					 * Searches Staff table, grabs those that match that aren't
 					 * removed from PS, and also those that aren't US Staff
 					 * (since they aren't in PS either). Based on
 					 * Review360Contoller
 					 */
 					Staff s = new Staff();
 					Iterator iStaff = s.selectList(
 							"UPPER(lastName) LIKE UPPER('"
 									+ nameSubString
 									+ "%') AND email LIKE '%@%' order by lastName, preferredName")
 							.iterator();
 					while (iStaff.hasNext()) {
 						Hashtable row = new Hashtable();
 						Staff staff = (Staff) iStaff.next();
 						org.alt60m.ministry.model.dbio.OldAddress address = staff.getPrimaryAddress();
 						row.put("PreferredName", staff.getPreferredName());
 						row.put("LastName", staff.getLastName());
 						row.put("Email", staff.getEmail());
 						if (address == null) {
 							row.put("City", "");
 							row.put("State", "");
 						} else {
 							row.put("City", address.getCity());
 							row.put("State", address.getState());
 						}
 						list.add(row);
 					}
 				}
 				String registrationID = (String) ctx.getSessionValue("registrationID");
 
 				Registration r = crsApp.getRegistration(registrationID);
 				ar.putObject("registration",
 						crsApp.getRegistration(registrationID));
 				ar.putObject(
 						"conference",
 						crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 				ar.addCollection("StaffList", list);
 				ar.putValue("PaymentMethod",
 						ctx.getInputString("PaymentMethod"));
 				ar.putValue("PaymentAmountOther",
 						ctx.getInputString("PaymentAmountOther"));
 				ar.putValue("PaymentAmount",
 						ctx.getInputString("PaymentAmount"));
 
 				ctx.setReturnValue(ar);
 
 				if ("Scholarship".equals(ctx.getInputString("PaymentMethod"))) {
 					ctx.goToView("findStaffScholarship");
 				} else {
 					ctx.goToView("findStaffMinAcctTransfer");
 				}
 			}
 		} catch (Exception e) {
 			log.error(e);
 			ctx.setError();
 			ctx.goToErrorView();
 		}
 	}
 
 	public void lookupPerson(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults();
 			if (ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue("errorMsg", ERR_conferenceNotFound);
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getSessionValue("userLoggedIn") == null) {
 				ar.putValue("errorMsg", ERR_username);
 				ar.putValue("nextAction", "userLogin");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else {
 				getSpouse(ctx, ar);
 				ctx.setReturnValue(ar);
 				ctx.goToView("lookupPerson");
 			}
 		} catch (Exception e) {
 			log.error(e);
 			ctx.setError();
 			ctx.goToErrorView();
 		}
 
 	}
 
 	/**
 	 * @param ctx
 	 * @param ar
 	 * @throws DBIOEntityException
 	 */
 	private void getSpouse(ActionContext ctx, ActionResults ar)
 			throws DBIOEntityException {
 		int orderCol = 0;
 		String order = "";
 		String[] orderFields = { "firstName", "lastName" };
 		int offset;
 		int size;
 
 		if (ctx.getInputString("orderCol") != null)
 			orderCol = Integer.parseInt(ctx.getInputString("orderCol"));
 		else
 			orderCol = 0;
 
 		if (ctx.getInputString("order") != null
 				&& ctx.getInputString("order").equals("DESC"))
 			order = "DESC";
 		else
 			order = "ASC";
 
 		if (ctx.getInputString("offset") != null)
 			offset = Integer.parseInt(ctx.getInputString("offset"));
 		else
 			offset = 1;
 
 		if (ctx.getInputString("size") != null)
 			size = Integer.parseInt(ctx.getInputString("size"));
 		else
 			size = 10;
 
 		ar.putValue("offset", String.valueOf(offset));
 		ar.putValue("size", String.valueOf(size));
 		ar.putValue("orderCol", String.valueOf(orderCol));
 		ar.putValue("order", order);
 		ar.putValue("nextAction", "confirmSpouse");
 		ar.putObject(
 				"conference",
 				crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 		Registration r = crsApp.getRegistration((String) ctx.getSessionValue("registrationID"));
 		ar.putObject("registration", r);
 
 		String firstName = ctx.getInputString("firstName") == null ? ""
 				: ctx.getInputString("firstName");
 		String lastName = ctx.getInputString("lastName") == null ? r.getPerson()
 				.getLastName()
 				: ctx.getInputString("lastName");
 		ar.putValue("firstName", firstName);
 		ar.putValue("lastName", lastName);
 		ar.putValue("nextVar", "foundID");
 		ar.putValue("lookupMessage", "Identify your spouse");
 		if (lastName != "") {
 			Vector sp = new Vector(crsApp.searchSpouses(firstName, lastName,
 					String.valueOf(r.getPerson().getPersonID()),
 					orderFields[orderCol], order, offset, size));
 			ar.putValue("maxSize", (sp.remove(0)).toString());
 			ar.addCollection("persons", sp);
 		}
 		ar.addHashtable("request", ctx.getHashedRequest());
 	}
 
 	public void lookupSSM(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults();
 			if (ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue("errorMsg", ERR_conferenceNotFound);
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getSessionValue("userLoggedIn") == null) {
 				ar.putValue("errorMsg", ERR_username);
 				ar.putValue("nextAction", "userLogin");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else {
 				int offset;
 				int size;
 
 				if (ctx.getInputString("offset") != null)
 					offset = Integer.parseInt(ctx.getInputString("offset"));
 				else
 					offset = 1;
 
 				if (ctx.getInputString("size") != null)
 					size = Integer.parseInt(ctx.getInputString("size"));
 				else
 					size = 10;
 
 				ar.putValue("offset", String.valueOf(offset));
 				ar.putValue("size", String.valueOf(size));
 				ar.putObject(
 						"conference",
 						crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 				ar.putObject(
 						"registration",
 						crsApp.getRegistration((String) ctx.getSessionValue("registrationID")));
 
 				String email = ctx.getInputString("email") == null ? ""
 						: ctx.getInputString("email");
 				ar.putValue("email", email);
 
 				if (email != "") {
 					Vector sp = new Vector(
 							crsApp.searchSSM(email, offset, size));
 					ar.putValue("maxSize", (sp.remove(0)).toString());
 					ar.addCollection("users", sp);
 				}
 				ar.addHashtable("request", ctx.getHashedRequest());
 				ctx.setReturnValue(ar);
 				ctx.goToView("lookupSSM");
 			}
 		} catch (Exception e) {
 			log.error(e);
 			ctx.setError();
 			ctx.goToErrorView();
 		}
 
 	}
 
 	public void addSpouse(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults();
 			if (ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue("errorMsg", ERR_conferenceNotFound);
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getSessionValue("userLoggedIn") == null) {
 				ar.putValue("errorMsg", ERR_username);
 				ar.putValue("nextAction", "userLogin");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getInputString("foundID") == null
 					&& ctx.getInputString("email") == null) {
 				Person p = crsApp.getPersonBySsmID((String) ctx.getSessionValue("userLoggedInSsm"));
 				Conference c = crsApp.getConference((String) ctx.getSessionValue("selectedEvent"));
 				ar.putObject("registration", p.getRegistration());
 				if (!"M".equals(p.getMaritalStatus())) {
 					ar.putValue("errorMsg", "Your marital status is <b>"
 							+ p.getMaritalStatus()
 							+ "</b> it should be <b>M</b>");
 					ar.putValue("nextAction", "listQuestions");
 					ctx.setReturnValue(ar);
 					ctx.goToView("error");
 				} else if (p.getSpouseID() == 0) {
 					getSpouse(ctx, ar);
 					ctx.setReturnValue(ar);
 					ctx.goToView("lookupPerson");
 				} else {
 					ar.putValue(
 							"errorMsg",
 							"You have previously indicated a spouse, contact your conference administrator via email at <a href='mailto:"
 									+ c.getContactEmail()
 									+ "'>"
 									+ c.getContactEmail()
 									+ "</a> if you believe this is an error.");
 					ar.putObject(
 							"conference",
 							crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 
 					ar.putValue("nextAction", "userLogin");
 					ctx.setReturnValue(ar);
 					ctx.goToView("error");
 				}
 			} else {
 				Person p = crsApp.getPersonBySsmID((String) ctx.getSessionValue("userLoggedInSsm"));
 				Person spouse = new Person();
 				if (ctx.getInputString("email") != null) {
 					User spouseSsm = crsApp.getSsmByUsername(ctx.getInputString("email"));					
 					spouse.setFk_ssmUserID(spouseSsm.getUserID());
 					spouse.select();
 					// don't double create
 					if (spouse.isPKEmpty()) {
 						spouse.setFk_ssmUserID(spouseSsm.getUserID());
 						spouse.insert();
 					}
 				} else {
 					spouse = new Person(new Integer(
 							ctx.getInputString("foundID")).intValue());
 				}
 				Conference c = crsApp.getConference((String) ctx.getSessionValue("selectedEvent"));
 				if (p.getSpouseID() == 0) {
 					if (spouse.getPersonID() == p.getPersonID()) {
 						ar.putValue("errorMsg",
 								"You cannot add yourself as your spouse, please select someone else.");
 						ar.putObject(
 								"conference",
 								crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 	
 						ar.putValue("nextAction", "addSpouse");
 						ctx.setReturnValue(ar);
 						ctx.goToView("error");
 					} else if (spouse.getSpouseID() != 0) {
 						ar.putValue(
 								"errorMsg",
 								spouse.getFirstName()
 										+ " "
 										+ spouse.getLastName()
 										+ " has already indicated a spouse, please select someone else.");
 						ar.putObject(
 								"conference",
 								crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 	
 						ar.putValue("nextAction", "addSpouse");
 						ctx.setReturnValue(ar);
 						ctx.goToView("error");
 					} else {
 						// Cross set spouse's
 						p.setSpouseID(spouse.getPersonID());
 						p.update();
 						spouse.setSpouseID(p.getPersonID());
 						spouse.update();
 						askSpouseQuestions(ctx);
 					}
 				} else {
 					ar.putValue(
 							"errorMsg",
 							"You have previously indicated a spouse, contact your conference administrator via email at <a href='mailto:"
 									+ c.getContactEmail()
 									+ "'>"
 									+ c.getContactEmail()
 									+ "</a> if you believe this is an error.");
 					ar.putObject(
 							"conference",
 							crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 
 					ar.putValue("nextAction", "userLogin");
 					ctx.setReturnValue(ar);
 					ctx.goToView("error");
 				}
 			}
 		} catch (Exception e) {
 			log.error(e);
 			ctx.setError();
 			ctx.goToErrorView();
 		}
 
 	}
 	public void searchSSM(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults();
 			if (ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue("errorMsg", ERR_conferenceNotFound);
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getSessionValue("userLoggedIn") == null) {
 				ar.putValue("errorMsg", ERR_username);
 				ar.putValue("nextAction", "userLogin");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else {
 				if (ctx.getInputString("email") != null
 						&& !ctx.getInputString("email").equals("")) {
 					Registration r = crsApp.getRegistration((String) ctx.getSessionValue("registrationID"));
 					Person p = r.getPerson();
 					User check = new User();
 					check.setUsername(ctx.getInputString("email"));
 					if (!check.select()) {  // check to see if username exists for this email address
 						ar.putValue("ssmExists", "false");	
 					}
 					else
 					{
 						ar.putValue("ssmExists", "true");
 					}
 					ar.putObject(
 							"conference",
 							crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 					ar.putObject(
 							"registration",
 							crsApp.getRegistration((String) ctx.getSessionValue("registrationID")));
 					ar.putValue("email", ctx.getInputString("email"));
 					
 					ar.addHashtable("request", ctx.getHashedRequest());
 					
 					ctx.setReturnValue(ar);
 					ctx.goToView("confirmSpouseLogin");
 				} else {
 					ar.putValue("errorMsg", ERR_username);
 					ar.putObject(
 							"conference",
 							crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 
 					ar.putValue("nextAction", "email null try again");
 					ctx.setReturnValue(ar);
 					ctx.goToView("error");
 				}
 			}
 		} catch (Exception e) {
 			log.error(e);
 			ctx.setError();
 			ctx.goToErrorView();
 		}
 
 	}
 
 	public void createSSM(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults();
 			if (ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue("errorMsg", ERR_conferenceNotFound);
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getSessionValue("userLoggedIn") == null) {
 				ar.putValue("errorMsg", ERR_username);
 				ar.putValue("nextAction", "userLogin");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else {
 				if (ctx.getInputString("email") != null
 						&& !ctx.getInputString("email").equals("")) {
 					Registration r = crsApp.getRegistration((String) ctx.getSessionValue("registrationID"));
 					Person p = r.getPerson();
 					User check = new User();
 					check.setUsername(ctx.getInputString("email"));
 					if (!check.select()) {  // Don't add double
 						User old = new User();
 						User spouse = new User();
 						old.setUsername(p.getEmail());
 						old.select();
 						spouse.setUsername(ctx.getInputString("email"));
 						spouse.setCreatedOn(new Date());
 						spouse.setPassword(old.getPassword());
 						spouse.setPasswordQuestion("what is your spouse's first name?");
 						spouse.setPasswordAnswer(p.getFirstName());
 						spouse.insert();
 						crsApp.sendSpouseEmail((String) ctx.getSessionValue("registrationID"), (String)ctx.getInputString("email"));
 					}
 					addSpouse(ctx);
 				} else {
 					ar.putValue("errorMsg", ERR_username);
 					ar.putValue("nextAction", "email null try again");
 					ctx.setReturnValue(ar);
 					ctx.goToView("error");
 				}
 			}
 		} catch (Exception e) {
 			log.error(e);
 			ctx.setError();
 			ctx.goToErrorView();
 		}
 
 	}
 
 	public void createSpouseLogin(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults();
 			if (ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue("errorMsg", ERR_conferenceNotFound);
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getSessionValue("userLoggedIn") == null) {
 				ar.putValue("errorMsg", ERR_username);
 				ar.putValue("nextAction", "userLogin");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else {
 				ar.putObject(
 						"conference",
 						crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 				ar.putObject(
 						"registration",
 						crsApp.getRegistration((String) ctx.getSessionValue("registrationID")));
 				ar.addHashtable("request", ctx.getHashedRequest());
 
 				ctx.setReturnValue(ar);
 				ctx.goToView("createSpouseLogin");
 			}
 		} catch (Exception e) {
 			log.error(e);
 			ctx.setError();
 			ctx.goToErrorView();
 		}
 
 	}
 	
 	public void confirmSpouseLogin(ActionContext ctx){
 		try{
 			ActionResults ar = new ActionResults();
 		
 			if (ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue("errorMsg", ERR_conferenceNotFound);
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getSessionValue("userLoggedIn") == null) {
 				ar.putValue("errorMsg", ERR_username);
 				ar.putValue("nextAction", "userLogin");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			}else {
 				ar.putObject(
 						"conference",
 						crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 				ar.putObject(
 						"registration",
 						crsApp.getRegistration((String) ctx.getSessionValue("registrationID")));
 				ar.putValue("email", ctx.getInputString("email"));
 				
 				ar.addHashtable("request", ctx.getHashedRequest());
 	
 				ctx.setReturnValue(ar);
 				ctx.goToView("confirmSpouseLogin");
 			}
 		} catch (Exception e) {
 			log.error(e);
 			ctx.setError();
 			ctx.goToErrorView();
 		}
 	}
 	
 	public void confirmSpouse(ActionContext ctx){
 		try{
 			ActionResults ar = new ActionResults();
 		
 			if (ctx.getSessionValue("selectedEvent") == null) {
 				ar.putValue("errorMsg", ERR_conferenceNotFound);
 				ar.putValue("nextAction", "listEvents");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			} else if (ctx.getSessionValue("userLoggedIn") == null) {
 				ar.putValue("errorMsg", ERR_username);
 				ar.putValue("nextAction", "userLogin");
 				ctx.setReturnValue(ar);
 				ctx.goToView("error");
 			}else if (ctx.getInputString("foundID") == null 
 					&& ctx.getInputString("ssm") == null){
 				ar.putObject(
 						"conference",
 						crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 				Registration r = crsApp.getRegistration((String) ctx.getSessionValue("registrationID"));
 				ar.putObject("registration", r);
 				ar.putValue("nextAction", "confirmSpouse");
 				
 				ctx.setReturnValue(ar);
 				ctx.goToView("addSpouse");
 			} else {
 				Person spouse = new Person();
 				if (ctx.getInputString("ssm") != null){
 					ar.putValue("ssm", ctx.getInputString("ssm"));
 					spouse.setFk_ssmUserID(Integer.parseInt(ctx.getInputString("ssm")));
 					spouse.select();
 				}
 				else
 				{
 					ar.putValue("foundID", ctx.getInputString("foundID"));
 					ar.putValue("nextVar", "foundID");
 					spouse = new Person(new Integer(ctx.getInputString("foundID")).intValue());
 				}
 				ar.putObject(
 						"conference",
 						crsApp.getConference((String) ctx.getSessionValue("selectedEvent")));
 				Registration r = crsApp.getRegistration((String) ctx.getSessionValue("registrationID"));
 				ar.putObject("registration", r);
 				ar.putValue("nextAction", "addSpouse");
 				
 				ar.addHashtable("request", ctx.getHashedRequest());
 				
 				ar.putObject("spouse",spouse);
 				ctx.setReturnValue(ar);
 				ctx.goToView("confirmSpouse");
 			}		
 		} catch (Exception e) {
 			log.error(e);
 			ctx.setError();
 			ctx.goToErrorView();
 		}		
 	}
 
 	/**
 	 * ***CAMPUS LOCATOR IS HERE TEMPORARILY. CAN BE MOVED LATER IF NEEDS BE,
 	 * MAB (8-21-02)*******
 	 */
 
 	public void outputCampusList(javax.servlet.http.HttpServletResponse res,
 			String searchText) throws Exception {
 		MinistryLocatorInfo mlInfo = new MinistryLocatorInfo();
 		Vector campuses = mlInfo.getAllTargetAreaNamesByState(searchText);
 		Iterator campusList = campuses.iterator();
 
 		res.setContentType("text/xml");
 		java.io.PrintWriter out = res.getWriter();
 		out.println("<campusList>");
 		while (campusList.hasNext()) {
 			String campusname;
 			String cityname;
 			TargetArea targetarea = new TargetArea();
 			targetarea = (TargetArea) campusList.next();
 			campusname = targetarea.getName();
 			cityname = targetarea.getCity();
 			out.println("<campus name=\""
 					+ ((campusname != null) ? org.alt60m.util.Escape.textToXML(campusname)
 							: "")
 					+ "\" city=\""
 					+ ((cityname != null) ? org.alt60m.util.Escape.textToXML(cityname)
 							: "")
 					+ "\" value=\""
 					+ ((campusname != null) ? org.alt60m.util.Escape.textToXML(
 							campusname).replaceAll("\'", "&#92;&#39;") : "")
 					+ "\" />");
 		}
 		out.println("</campusList>");
 		out.flush();
 	}
 
 	public void campusLocate(ActionContext ctx) {
 		try {
 			String searchText = ctx.getInputString("searchtext", true);
 			outputCampusList(ctx.getResponse(), searchText);
 		} catch (Exception e) {
			log.error("Failed to perform campusLocate", e);
 			goToErrorPage(ctx, e, "campusLocate");
 		}
 	}
 
 	private Registration resolveRegistration(ActionContext ctx, boolean saving) {
 		Registration r = crsApp.getRegistration((String) ctx.getSessionValue("registrationID"));
 
 		if (saving) {
 			r = crsApp.getRegistration((String) ctx.getInputString("activeID"));
 		} else if (r.getSpouseComing() == 2
 				&& "T".equals(ctx.getInputString("spouseReg"))
 				&& r.getSpouseRegistrationID() > 0) {
 			r = r.getSpouse();
 		}
 		return r;
 	}
 
 	private Registration getFirstRegistration(ActionContext ctx) {
 		Registration r = crsApp.getRegistration((String) ctx.getSessionValue("registrationID"));
 
 		if (!r.isRegisteredFirst()) {
 			r = r.getSpouse();
 		}
 		return r;
 	}
 }
