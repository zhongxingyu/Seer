 package org.alt60m.crs.application;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import org.alt60m.cms.util.CatPathMaker;
 import org.alt60m.crs.model.Answer;
 import org.alt60m.crs.model.ChildRegistration;
 import org.alt60m.crs.model.Conference;
 import org.alt60m.crs.model.CustomItem;
 //import org.alt60m.crs.model.GuestRegistration;
 import org.alt60m.crs.model.Merchandise;
 import org.alt60m.crs.model.MerchandiseChoice;
 import org.alt60m.crs.model.Payment;
 import org.alt60m.crs.model.Person;
 import org.alt60m.crs.model.Question;
 import org.alt60m.crs.model.QuestionText;
 import org.alt60m.crs.model.Registration;
 import org.alt60m.crs.model.RegistrationType;
 import org.alt60m.crs.model.Report;
 //import org.alt60m.crs.model.StaffRegistration;
 //import org.alt60m.crs.model.StudentRegistration;
 import org.alt60m.ministry.model.dbio.Staff;
 import org.alt60m.security.dbio.manager.SimpleSecurityManager;
 import org.alt60m.security.dbio.model.User;
 import org.alt60m.util.DBHelper;
 import org.alt60m.util.OnlinePayment;
 import org.alt60m.util.SendMessage;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.kenburcham.framework.dbio.DBIOEntityException;
 import com.kenburcham.framework.dbio.DBIOException;
 import com.kenburcham.framework.dbio.DBIOTransaction;
 
 public class CRSApplication {
 	public CRSApplication() {
 		try {
 		} catch (Exception e) {
 		}
 	}
 	private static Log log = LogFactory.getLog(CRSApplication.class); 
 
 	public static final String[] PREMADE_QUESTIONS = {
 			"Attending Spouse Name:", "Number of Children Attending:",
 			"Number of Previous Conferences:", "Are You Under 18?",
 			"Are You Staying at the Hotel?", "Roommate Preference:",
 			"Are You Interested in a Staff Interview?", "T-Shirt Size:",
 			"How Many Other People Can You Drive?",
 			"Where Are You Leaving From?", "Your Church Name:",
 			"Your Church City and State:" };
 
 	public static final String[] PREMADE_QUESTIONS_TYPES = { "textS", "textXS",
 			"textXS", "YN", "YN", "textS", "YN", "shirt", "textXS", "textS",
 			"textS", "textM" };
 
 	public Collection listConferences(String region, String orderField,
 			String orderDirection) {
 		Conference c = new Conference();
 		boolean DESC = orderDirection.equals("DESC");
 
 		if (region.equals("ALL"))
 			return c.selectList("1=1 ORDER BY "
 					+ fixOrderBy(orderField, (DESC ? "DESC" : "ASC")));
 		else
 			return c.selectList("region = '" + region + "'  ORDER BY "
 					+ fixOrderBy(orderField, (DESC ? "DESC" : "ASC")));
 	}
 
 	public Collection listCurrentConferences(String region, String orderField,
 			String orderDirection) {
 		Conference c = new Conference();
 		boolean DESC = orderDirection.equals("DESC");
 
 		if (region.equals("ALL"))
 			return c.selectList("preRegEnd >= '"
 					+ new java.sql.Date(new Date().getTime()).toString()
 					+ "' ORDER BY "
 					+ fixOrderBy(orderField, (DESC ? "DESC" : "ASC")));
 		else
 			return c.selectList("preRegEnd >= '"
 					+ new java.sql.Date(new Date().getTime()).toString()
 					+ "' AND region = '" + region + "' ORDER BY "
 					+ fixOrderBy(orderField, (DESC ? "DESC" : "ASC")));
 	}
 
 	public Collection listArchivedConferences(String region, String orderField,
 			String orderDirection) {
 		Conference c = new Conference();
 		boolean DESC = orderDirection.equals("DESC");
 
 		if (region.equals("ALL"))
 			return c.selectList("preRegEnd < '"
 					+ new java.sql.Date(new Date().getTime()).toString()
 					+ "' ORDER BY "
 					+ fixOrderBy(orderField, (DESC ? "DESC" : "ASC")));
 		else
 			return c.selectList("preRegEnd < '"
 					+ new java.sql.Date(new Date().getTime()).toString()
 					+ "' AND region = '" + region + "' ORDER BY "
 					+ fixOrderBy(orderField, (DESC ? "DESC" : "ASC")));
 	}
 
 	public Collection listActiveConferences(String region, String orderField,
 			String orderDirection) {
 		Conference c = new Conference();
 		boolean DESC = orderDirection.equals("DESC");
 
 		if (region.equals("ALL"))
 			return c.selectList("preRegStart <= '"
 					+ new java.sql.Date(new Date().getTime()).toString()
 					+ "' AND preRegEnd >= '"
 					+ new java.sql.Date(new Date().getTime()).toString()
 					+ "' ORDER BY "
 					+ fixOrderBy(orderField, (DESC ? "DESC" : "ASC")));
 		else
 			return c.selectList("preRegStart <= '"
 					+ new java.sql.Date(new Date().getTime()).toString()
 					+ "' AND preRegEnd >= '"
 					+ new java.sql.Date(new Date().getTime()).toString()
 					+ "' AND region = '" + region + "' ORDER BY "
 					+ fixOrderBy(orderField, (DESC ? "DESC" : "ASC")));
 	}
 
 	public boolean saveConference(Hashtable values) {
 		try {
 			if (values.containsKey("ConferenceID")) {
 				Conference c = getConference((String) values.get("ConferenceID"));
 				values.remove("ConferenceID");
 
 				c.setMappedValues(values);
 
 				return c.persist();
 			} else
 				return false;
 		} catch (Exception e) {
 			log.error(e, e);
 			return false;
 		}
 	}
 
 	public int createConference(Hashtable values) {
 		try {
 			Conference c = new Conference();
 			c.setMappedValues(values);
 			c.setCreateDate(new Date());
 			c.insert();
 
 			return c.getConferenceID();
 		} catch (Exception e) {
 			log.error(e, e);
 			return 0;
 		}
 	}
 
 	public boolean deleteConference(String conferenceIDString) {
 		try {
 			// Delete:
 			// Custom Items
 			// Questions -> Question Texts / Answers
 			// Merchandise -> Merchandise Choices
 			// Registrations -> ChildRegistration
 			// RegistrationTypes
 			// Payments
 			
 			int conferenceID = Integer.parseInt(conferenceIDString);
 
 			Conference c = getConference(conferenceID);
 
 			CustomItem ci = new CustomItem();
 			ci.setConferenceID(conferenceID);
 			ci.delete();
 
 			Iterator qs = c.getQuestions().iterator();
 			while (qs.hasNext()) {
 				Question q = (Question) qs.next();
 				Answer a = new Answer();
 				a.setQuestionID(q.getQuestionID());
 				a.delete();
 				deleteQuestion(String.valueOf(q.getQuestionID()));
 			}
 
 			Iterator rs = listRegistrations(conferenceIDString,
 					"registrationID", "DESC").iterator();
 			while (rs.hasNext()) {
 				Registration r = (Registration) rs.next();
 
 				Payment p = new Payment();
 				p.setRegistrationID(r.getRegistrationID());
 				p.delete();
 
 				ChildRegistration cr = new ChildRegistration();
 				cr.setRegistrationID(r.getRegistrationID());
 				cr.delete();
 
 				MerchandiseChoice mc = new MerchandiseChoice();
 				mc.setRegistrationID(r.getRegistrationID());
 				mc.delete();
 	
 				Answer a = new Answer();
 				a.setRegistrationID(r.getRegistrationID());
 				a.delete();
 
 				r.delete();
 			}
 
 			//remove any merchandise not deleted above (not associated with a registration)
 			Iterator<Merchandise> ms = c.getMerchandise().iterator();
 			deleteMerchandise(ms);
 			
 			Iterator rts = c.getRegistrationTypes().iterator();
 			while (rts.hasNext()) {
 				RegistrationType rt = (RegistrationType) rts.next();
 				rt.delete();
 			}
 
 			Registration r = new Registration();
 			r.setConferenceID(conferenceID);
 			r.delete();
 
 			c.delete();
 
 			return true;
 		} catch (Exception e) {
 			log.error(e,e);
 			return false;
 		}
 	}
 
 	private void deleteMerchandise(Iterator<Merchandise> ms) {
 		while (ms.hasNext()) {
 			Merchandise m = (Merchandise) ms.next();
 			MerchandiseChoice prototype = new MerchandiseChoice();
 			prototype.setMerchandiseID(m.getMerchandiseID());
 			List<MerchandiseChoice> merchandiseChoices = prototype.selectList();
 			for (MerchandiseChoice mc : merchandiseChoices) {
 				mc.delete();
 				/* Note: it would be faster to do prototype.delete(), but this can't be done, because
 				 * MerchandiseChoice doesn't have the 
 				 * foreign key for Merchandise set to "KEY" in the localinit().
 				 * And for the moment we can't set that because DBIO doesn't handle tables
 				 * with all "KEY" types at the moment.  ~JCS 10/9/2006
 				 */  
 			}
 			m.delete();
 		}
 	}
 
 	public int cloneConference(String conferenceIDString, String cloneName) {
 		try {
 			// Copy:
 			// Custom Items
 			// RegistrationTypes
 			// Questions -> Question Texts
 			// Merchandise
 
 			int conferenceID = Integer.parseInt(conferenceIDString);
 
 			Conference oldC = getConference(conferenceID);
 			Conference newC = getConference(conferenceID);
 
 			newC.setConferenceID(0);
 			newC.setName(cloneName);
 			newC.setAuthnetPassword("");
 			newC.setPassword("");
 			newC.setStaffPassword("");
 			newC.setCreateDate(new Date());
 			newC.insert();
 
 			Iterator cis = oldC.getCustomItems().iterator();
 			while (cis.hasNext()) {
 				CustomItem ci = (CustomItem) cis.next();
 				ci.setCustomItemID(0);
 				ci.setConference(newC);
 				ci.insert();
 			}
 
 			Iterator rts = oldC.getRegistrationTypes().iterator();
 			while (rts.hasNext()) {
 				RegistrationType rt = (RegistrationType) rts.next();
 				int oldRegTypeID = rt.getRegistrationTypeID();
 				
 				rt.setRegistrationTypeID(0);
 				rt.setConference(newC);
 				rt.insert();
 				
 				int newRegTypeID = rt.getRegistrationTypeID();
 				RegistrationType oldRT = getRegistrationType(oldRegTypeID);
 				
 				Iterator ms = oldRT.getMerchandise().iterator();
 				while (ms.hasNext()) {
 					Merchandise m = (Merchandise) ms.next();
 					m.setMerchandiseID(0);
 					m.setRegistrationType(rt);
 					m.setConference(newC);
 					m.insert();
 				}
 	
 				Iterator qs = oldRT.getQuestions().iterator();
 				while (qs.hasNext()) {
 					Question q = (Question) qs.next();
 					if (q.getQuestionText().getStatus().equals("common")) {
 						addCommonQuestion(
 								String.valueOf(newC.getConferenceID()),
 								newRegTypeID,
 								String.valueOf(q.getQuestionText().getQuestionTextID()),
 								q.getDisplayOrder(),
 								q.getRequired());
 					} else {
 						Hashtable newQ = new Hashtable();
 						newQ.put("QuestionID", "0");
 						newQ.put("QuestionTextID", "0");
 						newQ.put("ConferenceID",String.valueOf(newC.getConferenceID()));
 						newQ.put("RegistrationTypeID",String.valueOf(newRegTypeID));
 						newQ.put("Body", q.getQuestionText().getBody());
 						newQ.put("AnswerType", q.getQuestionText().getAnswerType());
 						newQ.put("Status", q.getQuestionText().getStatus());
 						newQ.put("DisplayOrder", String.valueOf(q.getDisplayOrder()));
 						newQ.put("Required", q.getRequired() ? "T" : "F");
 						saveQuestion(newQ);
 					}
 				}
 			}
 
 
 			return newC.getConferenceID();
 		} catch (Exception e) {
 			return 0;
 		}
 	}
 
 	/*
 	 * public boolean saveRegistrationType(Hashtable values) { try { if
 	 * (values.containsKey("registrationTypeID")) { RegistrationType rt =
 	 * getRegistrationType((String) values.get("registrationTypeID"));
 	 * values.remove("registrationTypeID"); rt.setMappedValues(values);
 	 * 
 	 * return rt.persist(); } else return false; } catch (Exception e) {
 	 * log.error(e, e); return false; } }
 	 */
 	public int createRegistrationType(int cID, String type) {
 		try {
 			RegistrationType rt = new RegistrationType();
 			rt.setConferenceID(cID);
 			rt.setRegistrationType(type);
 			rt.insert();
 			return rt.getRegistrationTypeID();
 		} catch (Exception e) {
 			log.error(e, e);
 			return 0;
 		}
 	}
 
 	public RegistrationType getRegistrationType(int ID) {
 		try {
 			RegistrationType rt = new RegistrationType();
 			rt.setRegistrationTypeID(ID);
 			rt.select();
 			return rt;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public RegistrationType getRegistrationType(String rid)
 			throws DBIOEntityException {
 		return getRegistrationType(Integer.parseInt(rid.trim()));
 	}
 
 	public RegistrationType getRegistrationType(String cID, String type) {
 		try {
 			RegistrationType rt = new RegistrationType();
 			rt.setConferenceID(new Integer(cID).intValue());
 			rt.setRegistrationType(type);
 			rt.select();
 			return rt;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 	
 	public boolean saveRegistrationType(Hashtable values) {
 		try {
 			if (values.containsKey("RegistrationTypeID")) {
 				RegistrationType rt = getRegistrationType((String) values.get("RegistrationTypeID"));
 				values.remove("RegistrationTypeID");
 
 				rt.setMappedValues(values);
 
 				return rt.persist();
 			} else
 				return false;
 		} catch (Exception e) {
 			log.error(e, e);
 			return false;
 		}
 	}
 
 	public int cloneRegistrationType(String regTypeIDString) {
 		try {
 			// Copy:
 			// Questions -> Question Texts
 			// Merchandise
 
 			int oldRegTypeID = Integer.parseInt(regTypeIDString);
 
 			RegistrationType oldRT = getRegistrationType(oldRegTypeID);
 			RegistrationType newRT = getRegistrationType(oldRegTypeID);
 
 			newRT.setRegistrationTypeID(0);
 			newRT.insert();
 			
 			int newRegTypeID =newRT.getRegistrationTypeID();
 			
 			Iterator ms = oldRT.getMerchandise().iterator();
 			while (ms.hasNext()) {
 				Merchandise m = (Merchandise) ms.next();
 				m.setMerchandiseID(0);
 				m.setRegistrationType(newRT);
 				m.insert();
 			}
 
 			Iterator qs = oldRT.getQuestions().iterator();
 			while (qs.hasNext()) {
 				Question q = (Question) qs.next();
 				if (q.getQuestionText().getStatus().equals("common")) {
 					addCommonQuestion(
 							String.valueOf(oldRT.getConferenceID()),
 							newRegTypeID,
 							String.valueOf(q.getQuestionText().getQuestionTextID()),
 							q.getDisplayOrder(),
 							q.getRequired());
 				} else {
 					Hashtable newQ = new Hashtable();
 					newQ.put("QuestionID", "0");
 					newQ.put("QuestionTextID", "0");
 					newQ.put("ConferenceID",String.valueOf(oldRT.getConferenceID()));
 					newQ.put("RegistrationTypeID",String.valueOf(newRegTypeID));
 					newQ.put("Body", q.getQuestionText().getBody());
 					newQ.put("AnswerType", q.getQuestionText().getAnswerType());
 					newQ.put("Status", q.getQuestionText().getStatus());
 					newQ.put("DisplayOrder", String.valueOf(q.getDisplayOrder()));
 					newQ.put("Required", q.getRequired() ? "T" : "F");
 					saveQuestion(newQ);
 				}
 			}
 			return newRegTypeID;
 		} catch (Exception e) {
 			return 0;
 		}
 	}
 
 
 	public boolean deleteRegistrationType(String regTypeIDString) {
 		try {
 			// Delete:
 			// Questions -> Question Texts / Answers
 			// Merchandise -> Merchandise Choices
 			// Registrations -> ChildRegistration
 			// Payments
 			int regTypeID = Integer.parseInt(regTypeIDString);
 
 			RegistrationType rt = getRegistrationType(regTypeID);
 			String conferenceIDString = String.valueOf(rt.getConferenceID());
 
 			Iterator qs = rt.getQuestions().iterator();
 			while (qs.hasNext()) {
 				Question q = (Question) qs.next();
 				Answer a = new Answer();
 				a.setQuestionID(q.getQuestionID());
 				a.delete();
 				deleteQuestion(String.valueOf(q.getQuestionID()));
 			}
 
 			Iterator rs = listRegistrations(conferenceIDString, regTypeIDString,
 					"registrationID", "DESC").iterator();
 			while (rs.hasNext()) {
 				Registration r = (Registration) rs.next();
 
 				Payment p = new Payment();
 				p.setRegistrationID(r.getRegistrationID());
 				p.delete();
 
 				ChildRegistration cr = new ChildRegistration();
 				cr.setRegistrationID(r.getRegistrationID());
 				cr.delete();
 
 				MerchandiseChoice mc = new MerchandiseChoice();
 				mc.setRegistrationID(r.getRegistrationID());
 				mc.delete();
 	
 				Answer a = new Answer();
 				a.setRegistrationID(r.getRegistrationID());
 				a.delete();
 
 				r.delete();
 			}
 			
 			//remove any merchandise not deleted above (not associated with a registration)
 			Iterator<Merchandise> ms = rt.getMerchandise().iterator();
 			deleteMerchandise(ms);
 			
 			rt.delete();
 
 			return true;
 		} catch (Exception e) {
 			log.error(e, e);
 			return false;
 		}
 	}
 
 	public Collection listMerchandise(String conferenceID, String orderField,
 			String orderDirection) throws Exception {
 		boolean DESC = orderDirection.equals("DESC");
 		Merchandise m = new Merchandise();
 		return m.selectList("fk_ConferenceID = '" + conferenceID
 				+ "' ORDER BY "
 				+ fixOrderBy(orderField, (DESC ? "DESC" : "ASC")));
 	}
 
 	public Collection listMerchandise(String registrationType,
 			String conferenceID, String orderField, String orderDirection)
 			throws Exception {
 		boolean DESC = orderDirection.equals("DESC");
 		Merchandise m = new Merchandise();
 		return m.selectList("fk_ConferenceID = '" + conferenceID
 				+ "' AND registrationType = '" + registrationType
 				+ "' ORDER BY "
 				+ fixOrderBy(orderField, (DESC ? "DESC" : "ASC")));
 	}
 
 	public Collection listMerchandise(String conferenceID,
 			int registrationTypeID, String orderField, String orderDirection)
 			throws Exception {
 		boolean DESC = orderDirection.equals("DESC");
 		Merchandise m = new Merchandise();
 		return m.selectList("fk_ConferenceID = '" + conferenceID
 				+ "' AND fk_RegistrationTypeID = '" + registrationTypeID
 				+ "' ORDER BY "
 				+ fixOrderBy(orderField, (DESC ? "DESC" : "ASC")));
 	}
 
 	public boolean saveMerchandise(Hashtable values) {
 		try {
 			if (values.containsKey("MerchandiseID")
 					&& values.containsKey("ConferenceID")) {
 				Merchandise m = getMerchandise((String) values.get("MerchandiseID"));
 				values.remove("MerchandiseID");
 				Conference c = getConference((String) values.get("ConferenceID"));
 				values.remove("ConferenceID");
 
 				m.setMappedValues(values);
 				m.setConference(c);
 
 				/*
 				 * //not sure how to get this to work! if
 				 * (values.containsKey("RegistrationTypeID")) { RegistrationType
 				 * rt = getRegistrationType((new
 				 * Integer((String)values.get("RegistrationTypeID"))).intValue());
 				 * rt.assocMerchandise(m); }
 				 */
 				return m.persist();
 			} else
 				return false;
 		} catch (Exception e) {
 			log.error("Failed to save merchandise", e);
 			return false;
 		}
 	}
 
 	public boolean deleteMerchandise(String merchandiseID) {
 		try {
 			Merchandise m = getMerchandise(merchandiseID);
 			return m.delete();
 		} catch (Exception e) {
 			return false;
 		}
 	}
 
 	public Collection listCustomItems(String conferenceID, String orderField,
 			String orderDirection) {
 		boolean DESC = orderDirection.equals("DESC");
 		CustomItem ci = new CustomItem();
 		return ci.selectList("fk_ConferenceID = '" + conferenceID
 				+ "' ORDER BY "
 				+ fixOrderBy(orderField, (DESC ? "DESC" : "ASC")));
 	}
 
 	public boolean saveCustomItem(Hashtable values) {
 		try {
 			if (values.containsKey("CustomItemID")
 					&& values.containsKey("ConferenceID")) {
 				CustomItem ci = getCustomItem((String) values.get("CustomItemID"));
 				values.remove("CustomItemID");
 				Conference c = getConference((String) values.get("ConferenceID"));
 				values.remove("ConferenceID");
 
 				ci.setMappedValues(values);
 				ci.setConference(c);
 
 				return ci.persist();
 			} else
 				return false;
 		} catch (Exception e) {
 			log.error(e, e);
 			return false;
 		}
 	}
 
 	public boolean deleteCustomItem(String customItemID) {
 		try {
 			CustomItem ci = getCustomItem(customItemID);
 			return ci.delete();
 		} catch (Exception e) {
 			return false;
 		}
 	}
 
 	//	 check to see if they are ready to pay
 	//  return values:	"" means ready to pay
 	// 					"vitals" means they need to edit their personal details
 	//					"questions" means they need to answer some required question
 	public String checkReadyToPay(String registrationID) throws Exception{
 		Registration r = getRegistration(registrationID);
 		boolean ready=true;
 		String returnMsg="";		
 		
 		// make sure they have answered all the required questions before they can make a payment 
 		// if they haven't vital info entered: name, gender, email, don't let them make payment
 		if("".equals(r.getFirstName())|"".equals(r.getLastName())|"".equals(r.getGender())|"".equals(r.getEmail()))
 		{
 			ready=false;
 			returnMsg= "vitals";
 		}
 		// if vital info is entered check if required questions are answered
 		else
 		{
 			Collection Questions= listQuestions(
 					String.valueOf(r.getConferenceID()),
 					r.getRegistrationTypeID(), "displayOrder", "ASC");
 
 			Collection Answers=	listRegistrationAnswers(r.getRegistrationID());
 			
 			if(Questions.size() > 0)	// only check if there are some questions
 			{		
 				Iterator questions = Questions.iterator();
 				Iterator answers = Answers.iterator();
 				
 				//build the hashtable of answers for this person's registration
 				Hashtable hashAnswers = new Hashtable();
 				while(answers.hasNext())
 				{
 					Answer nextAnswer = (Answer)answers.next();
 					hashAnswers.put(String.valueOf(nextAnswer.getQuestionID()), nextAnswer.getBody());
 				}
 				// as long as we haven't already found an unanswered required question and there are more questions
 				while(ready&&questions.hasNext())
 				{
 					Question question = (Question)questions.next();
 					// don't check the answers of unapplicable question types
 					if(!question.getAnswerType().equals("divider") && !question.getAnswerType().equals("info") && !question.getAnswerType().equals("hide") && !question.getAnswerType().equals("checkbox")){
 						String answer = hashAnswers.get(String.valueOf(question.getQuestionID())) == null ? "" : (String)hashAnswers.get(String.valueOf(question.getQuestionID()));
 						boolean required= question.getRequired();
 						// if it's a required question and it's not answered
 						if(required && answer.equals(""))
 						{
 							returnMsg= "questions";
 							ready= false;
 						}
 					}
 				}
 			}
 		}
 		
 		return returnMsg;
 	}
 	
 	public Collection listQuestions(String registrationType,
 			String conferenceID, String orderField, String orderDirection)
 			throws Exception {
 		boolean DESC = orderDirection.equals("DESC");
 		Question q = new Question();
 		Collection qs = q.selectList("fk_ConferenceID = '" + conferenceID
 				+ "' AND registrationType = '" + registrationType
 				+ "' ORDER BY "
 				+ fixOrderBy(orderField, (DESC ? "DESC" : "ASC")));
 		return qs;
 	}
 
 	public Collection listQuestions(String conferenceID,
 			int registrationTypeID, String orderField, String orderDirection)
 			throws Exception {
 		boolean DESC = orderDirection.equals("DESC");
 		Question q = new Question();
 		Collection qs = q.selectList("fk_RegistrationTypeID = '"
 				+ registrationTypeID + "' ORDER BY "
 				+ fixOrderBy(orderField, (DESC ? "DESC" : "ASC")));
 		return qs;
 	}
 
 	public Question newQuestion(Hashtable values) {
 		try {
 			if (values.containsKey("QuestionID")
 					&& values.containsKey("QuestionTextID")
 					&& values.containsKey("ConferenceID")) {
 				QuestionText qt = new QuestionText();
 				if (!((String) values.get("QuestionTextID")).equals("0"))
 					qt = getQuestionText((String) values.get("QuestionTextID"));
 				values.remove("QuestionTextID");
 				Question q = new Question();
 				if (!((String) values.get("QuestionID")).equals("0"))
 					q = getQuestion((String) values.get("QuestionID"));
 				values.remove("QuestionID");
 				Conference c = getConference((String) values.get("ConferenceID"));
 				values.remove("ConferenceID");
 				qt.setMappedValues(values);
 				boolean qtgood = qt.persist();
 				q.setMappedValues(values);
 				q.setConference(c);
 				q.setQuestionText(qt);
 				q.insert();
 				return q;
 			} else
 				return null;
 		} catch (Exception e) {
 			log.error(e, e);
 			return null;
 		}
 	}
 
 	public boolean saveQuestion(Hashtable values) {
 		try {
 			if (values.containsKey("QuestionID")
 					&& values.containsKey("QuestionTextID")
 					&& values.containsKey("ConferenceID")) {
 				QuestionText qt = new QuestionText();
 				if (!((String) values.get("QuestionTextID")).equals("0"))
 					qt = getQuestionText((String) values.get("QuestionTextID"));
 				values.remove("QuestionTextID");
 				Question q = new Question();
 				if (!((String) values.get("QuestionID")).equals("0"))
 					q = getQuestion((String) values.get("QuestionID"));
 				values.remove("QuestionID");
 				Conference c = getConference((String) values.get("ConferenceID"));
 				values.remove("ConferenceID");
 				qt.setMappedValues(values);
 				boolean qtgood = qt.persist();
 				q.setMappedValues(values);
 				q.setConference(c);
 				q.setQuestionText(qt);
 				return qtgood && q.persist();
 			} else
 				return false;
 		} catch (Exception e) {
 			log.error(e, e);
 			return false;
 		}
 	}
 
 	public boolean deleteQuestion(String questionID) {
 		Question q = getQuestion(questionID);
 		Answer a = new Answer();
 		a.setQuestion(q);
 		if ("common".equals(q.getQuestionText().getStatus())) {
 			return q.delete() && a.delete();
 		} else {
 			if (q.getQuestionText().delete())
 				return a.delete() && q.delete();
 			else
 				return false;
 		}
 	}
 
 	public boolean updateQuestion(Hashtable values) {
 		try {
 			Question q = getQuestion((String) values.get("QuestionID"));
 			q.setMappedValues(values);
 			return q.update();
 		} catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean updateMerchandise(Hashtable values) {
 		try {
 			Merchandise q = getMerchandise((String) values.get("MerchandiseID"));
 			q.setMappedValues(values);
 			return q.update();
 		} catch (Exception e) {
 			return false;
 		}
 	}
 
 	public Vector listCommonQuestions(String orderField, String orderDirection) {
 		boolean DESC = orderDirection.equals("DESC");
 		QuestionText qt = new QuestionText();
 		return qt.selectList("status = 'common' ORDER BY "
 				+ fixOrderBy(orderField, (DESC ? "DESC" : "ASC")));
 	}
 
 	public boolean addCommonQuestion(String conferenceID,
 			int registrationTypeID, String questionTextID) {
 		try {
 			Question q = new Question();
 			QuestionText qt = getQuestionText(questionTextID);
 			Conference c = getConference(conferenceID);
 
 			//			RegistrationType rt = getRegistrationType(registrationTypeID); /*
 			// temporary stopgap */
 			//			q.setRegistrationType(rt.getRegistrationType());
 
 			q.setRegistrationTypeID(registrationTypeID);
 			q.setQuestionText(qt);
 			if (q.insert()) {
 				c.assocQuestion(q);
 				return c.update();
 			} else {
 				return false;
 			}
 		} catch (Exception e) {
 			log.error(e, e);
 			return false;
 		}
 	}
 
 	public boolean addCommonQuestion(String conferenceID,
 			int registrationTypeID, String questionTextID, boolean req) {
 		try {
 			Question q = new Question();
 			QuestionText qt = getQuestionText(questionTextID);
 			Conference c = getConference(conferenceID);
 
 			//			RegistrationType rt = getRegistrationType(registrationTypeID); /*
 			// temporary stopgap */
 			//			q.setRegistrationType(rt.getRegistrationType());
 
 			q.setRegistrationTypeID(registrationTypeID);
 			q.setQuestionText(qt);
 			q.setConference(c);
 			Iterator qIter = q.selectList().iterator();
 
 			if (qIter.hasNext()) {
 				q = (Question) qIter.next();
 			}
 
 			q.setRequired(req);
 			boolean result = q.persist();
 			return result;
 		} catch (Exception e) {
 			log.error(e, e);
 			return false;
 		}
 	}
 
 	public boolean addCommonQuestion(String conferenceID,
 			int registrationTypeID, String questionTextID, int order,
 			boolean req) {
 		try {
 			Question q = new Question();
 			QuestionText qt = getQuestionText(questionTextID);
 			Conference c = getConference(conferenceID);
 
 			//			RegistrationType rt = getRegistrationType(registrationTypeID); /*
 			// temporary stopgap */
 			//			q.setRegistrationType(rt.getRegistrationType());
 
 			q.setRegistrationTypeID(registrationTypeID);
 			q.setQuestionText(qt);
 			q.setDisplayOrder(order);
 			q.setRequired(req);
 			if (q.insert()) {
 				c.assocQuestion(q);
 				return c.update();
 			} else {
 				return false;
 			}
 		} catch (Exception e) {
 			log.error(e, e);
 			return false;
 		}
 	}
 
 	public Question newCommonQuestion(String conferenceID,
 			int registrationTypeID, String questionTextID, int order,
 			boolean req) {
 		try {
 			Question q = new Question();
 			QuestionText qt = getQuestionText(questionTextID);
 			Conference c = getConference(conferenceID);
 
 			RegistrationType rt = getRegistrationType(registrationTypeID); /*
 																		    * temporary
 																		    * stopgap
 																		    */
 			//			q.setRegistrationType(rt.getRegistrationType());
 
 			q.setRegistrationTypeID(registrationTypeID);
 			q.setQuestionText(qt);
 			q.setDisplayOrder(order);
 			q.setRequired(req);
 			if (q.insert()) {
 				c.assocQuestion(q);
 				c.update();
 				return q;
 			} else {
 				return null;
 			}
 		} catch (Exception e) {
 			log.error(e, e);
 			return null;
 		}
 	}
 
 	public boolean saveAnswer(Hashtable values) {
 		try {
 			if (values.containsKey("QuestionID")
 					&& values.containsKey("RegistrationID")) {
 				Answer a = new Answer();
 				a.setQuestionID(Integer.parseInt((String) values.get("QuestionID")));
 				a.setRegistrationID(Integer.parseInt((String) values.get("RegistrationID")));
 				a.select();
 				a.setBody((String) values.get("Body"));
 				return a.persist();
 			} else
 				return false;
 		} catch (Exception e) {
 			log.error(e, e);
 			return false;
 		}
 	}
 
 	public boolean deleteAnswer(String answerID) {
 		try {
 			Answer a = getAnswer(answerID);
 			return a.delete();
 		} catch (Exception e) {
 			return false;
 		}
 	}
 
 	public Collection searchRegistrationsByName(String firstName,
 			String lastName, int type, String conferenceID, String orderField,
 			String orderDirection, int offset, int size)
 			throws DBIOEntityException {
 		boolean DESC = orderDirection.equals("DESC");
 		Registration r = new Registration();
 		Vector rv = new Vector();
 		Vector regs = new Vector();
 		firstName = firstName.replaceAll("'", "''");  // Takes care of problem apostrophes in subString
 		lastName = lastName.replaceAll("'", "''");  // Takes care of problem apostrophes in subString
 		if (type == -1)
 			regs = r.selectList("firstName LIKE '" + firstName
 					+ "%' AND lastName LIKE '" + lastName
 					+ "%' AND fk_ConferenceID = '" + conferenceID
 					+ "' ORDER BY "
 					+ fixOrderBy(orderField, (DESC ? "DESC" : "ASC")));
 		else
 			regs = r.selectList("firstName LIKE '" + firstName
 					+ "%' AND lastName LIKE '" + lastName
 					+ "%' AND fk_RegistrationTypeID = '" + type
 					+ "' AND fk_ConferenceID = '" + conferenceID
 					+ "' ORDER BY "
 					+ fixOrderBy(orderField, (DESC ? "DESC" : "ASC")));
 		rv.add(new Integer(regs.size()));
 		rv.addAll(fixList(regs, offset, size));
 		return rv;
 	}
 
 	public Collection searchRegistrationsAdvanced(String firstName,
 			String lastName, String region, String state, String localLevelID,
 			String Campus, int type, String conferenceID, String orderField,
 			String orderDirection, int offset, int size)
 			throws DBIOEntityException {
 		boolean DESC = orderDirection.equals("DESC");
 		Registration r = new Registration();
 		Vector rv = new Vector();
 		List regs = new ArrayList();
 		String query = "select crs_registration.registrationID, crs_registration.registrationDate, crs_registration.preRegistered, crs_registration.fk_PersonID, crs_registration.fk_ConferenceID, ministry_person.lastName, ministry_person.personID, ministry_person.dateCreated, ministry_person.firstName, ministry_person.middleName, ministry_person.birth_date, ministry_person.campus, ministry_person.yearInSchool ,ministry_person.graduation_date, ministry_person.greekAffiliation, ministry_person.gender, currentaddress.address1, currentaddress.address2, currentaddress.city, currentaddress.state, currentaddress.zip, currentaddress.homePhone, currentaddress.country, currentaddress.email, permanentaddress.address1 AS permanentAddress1, permanentaddress.address2 AS permanentAddress2, permanentaddress.city AS permanentCity, permanentaddress.state AS permanentState,permanentaddress.zip AS permanentZip,permanentaddress.homePhone AS permanentPhone, permanentaddress.country AS permanentCountry, ministry_person.maritalStatus, ministry_person.numberChildren AS numberOfKids, ministry_targetarea.name AS campusName, ministry_targetarea.state AS tastate, crs_registration.additionalRooms, crs_registration.leaveDate, crs_registration.arriveDate, ministry_person.accountNo, crs_registration.fk_RegistrationTypeID, crs_registration.spouseComing, crs_registration.spouseRegistrationID, crs_registration.registeredFirst, crs_registration.isOnsite, ministry_person.fk_spouseID AS spouseID, crs_registration.newPersonID, ministry_locallevel.teamID AS localLevelId, ministry_locallevel.region, ministry_locallevel.state AS llstate from ministry_person join crs_registration on (ministry_person.personID = crs_registration.fk_PersonID) join ministry_targetarea on (ministry_person.campus = ministry_targetarea.name) join ministry_activity on (ministry_activity.fk_targetAreaID = ministry_targetarea.TargetAreaID) join ministry_locallevel on(ministry_activity.fk_teamID = ministry_locallevel.teamID) join ministry_newaddress currentaddress on (ministry_person.personID = currentaddress.fk_PersonID) join ministry_newaddress permanentaddress on (ministry_person.personID = permanentaddress.fk_PersonID) where (ministry_activity.status <> 'IN') and permanentaddress.addressType = 'permanent' and currentaddress.addressType = 'current'";
 		String subQuery = "";
 		if (!"".equals(firstName.trim()))
 			subQuery += " AND firstName LIKE '" + firstName + "%'";
 		if (!"".equals(lastName.trim()))
 			subQuery += " AND lastName LIKE '" + lastName + "%'";
 
 		if (!"".equals(region.trim())) {
 			subQuery += " AND ministry_targetarea.region = '" + region + "'";
 		} 
 		if (!"".equals(localLevelID.trim())) {
 			subQuery += " AND ministry_locallevel.teamID = '" + localLevelID + "'";
 		} 
 		if (!"".equals(state.trim())) {
 			subQuery += " AND ministry_targetarea.state = '" + state + "'";
 		} 
 		if (!"".equals(Campus.trim())) {
 			subQuery += " AND ministry_targetarea.name = '" + Campus + "'";
 		}
 		
 		query = query + " and fk_ConferenceID = '" + conferenceID + "' ";
 		if (type != -1) {
 			query = query + " and fk_RegistrationTypeID = '" + type + "'";
 		}
 		query = query + subQuery + " ORDER BY "
 		+ fixOrderBy(orderField, (DESC ? "DESC" : "ASC"));
 		regs = r.selectSQLList(query);
 		regs = removeDupRegistrations(regs);
 		rv.add(new Integer(regs.size()));
 		rv.addAll(fixList(regs, offset, size));
 		return rv;
 	}
 
 	private List removeDupRegistrations(List vector) {
 		List newVector = new ArrayList();
 		Iterator iter = vector.iterator();
 		ArrayList ids = new ArrayList();
 		while (iter.hasNext()) {
 			Registration obj = (Registration) iter.next();
 			if (!ids.contains(String.valueOf(obj.getRegistrationID()))) {
 				ids.add(String.valueOf(obj.getRegistrationID()));
 				newVector.add(obj);
 			}
 		}
 		return newVector;
 	}
 
 	public Collection listRegistrations(String conferenceID, String orderField,
 			String orderDirection, int offset, int size)
 			throws DBIOEntityException {
 		boolean DESC = orderDirection.equals("DESC");
 		Registration r = new Registration();
 		Vector rv = new Vector();
 		Vector regs = r.selectList("fk_ConferenceID = '" + conferenceID
 				+ "' ORDER BY "
 				+ fixOrderBy(orderField, (DESC ? "DESC" : "ASC")));
 		rv.add(new Integer(regs.size()));
 		rv.addAll(fixList(regs, offset, size));
 		return rv;
 	}
 
 	public Collection listRegistrations(String conferenceID, String orderField,
 			String orderDirection) {
 		boolean DESC = orderDirection.equals("DESC");
 		Registration r = new Registration();
 		return r.selectList("fk_ConferenceID = '" + conferenceID
 				+ "' ORDER BY "
 				+ fixOrderBy(orderField, (DESC ? "DESC" : "ASC")));
 	}
 
 	public Collection listRegistrations(String conferenceID, String regTypeID,
 			String orderField,	String orderDirection) {
 		boolean DESC = orderDirection.equals("DESC");
 		Registration r = new Registration();
 		return r.selectList("fk_ConferenceID = '" + conferenceID
 				+"' AND fk_RegistrationTypeID = '" + regTypeID
 				+ "' ORDER BY "
 				+ fixOrderBy(orderField, (DESC ? "DESC" : "ASC")));
 	}
 
 	public int countRegistrations(String conferenceID)
 			throws DBIOEntityException {
 		Registration r = new Registration();
 		r.setConferenceID(Integer.parseInt(conferenceID));
 		return r.count();
 	}
 
 	public int countRegistrationsByType(String conferenceID,
 			int registrationTypeID) throws DBIOEntityException {
 		Registration r = new Registration();
 		r.setConferenceID(Integer.parseInt(conferenceID));
 		r.setRegistrationTypeID(registrationTypeID);
 		return r.count();
 	}
 	
 	public int countRegistrationTypes(String conferenceID)
 		throws DBIOEntityException {
 		RegistrationType rt = new RegistrationType();
 		rt.setConferenceID(Integer.parseInt(conferenceID));
 		return rt.count();
 	}
 	
 	public boolean deleteRegistration(String registrationIDString) {
 		try {
 			// Delete:
 			// Payments
 			// Merchandise Choices
 			// Answers
 			int registrationID = Integer.parseInt(registrationIDString);
 			Registration r = getRegistration(registrationID);
 			Registration spouse = r.getSpouse();
 			
 			// If spouse coming, move payments to spouse, delete/modify payments as necessary
 			if (r.getSpouseComing() == 2 && r.getSpouseRegistrationID() != 0) {
 				Payment p = new Payment();
 				p.setRegistrationID(registrationID);
 				Vector payments = p.selectList();
 				Iterator paymentsIter = payments.iterator();
 				while( paymentsIter.hasNext() ) {
 					// Delete old payment, create new payment 
 					// (to get around dbio bug with changing a foreign key)
 					Payment currPayment = (Payment)paymentsIter.next();
 					Payment delete = new Payment();
 					
 					int paymentID = currPayment.getPaymentID();
 					
 					currPayment.setPaymentID(0);
 					currPayment.setRegistration(spouse);
 					if (currPayment.insert()) {
 						delete.setPaymentID(paymentID);
 						delete.delete();
 					}
 				}
 				
 				// Negative Debit conference cost
 				Payment commute = new Payment();
 				Payment onsite = new Payment();
 				
 				commute.setRegistration(spouse);
 				Vector commuteList = commute.selectList("fk_RegistrationID = " + spouse.getRegistrationID() + " AND type LIKE 'Commuter Conference Cost%' ORDER BY paymentDate");
 
 				onsite.setRegistration(spouse);
 				Vector onsiteList = onsite.selectList("fk_RegistrationID = " + spouse.getRegistrationID() + " AND type LIKE 'Onsite Conference Cost%' ORDER BY paymentDate");
 				
 				if (commuteList.size() % 2 == 1) {
 					// Negative Debit
 					commute = (Payment)commuteList.get(commuteList.size() - 1);
 					commute.setPaymentID(0);
 					commute.setPaymentDate(new Date());
 					commute.setPostedDate(new Date());
 					commute.setPosted(true);
 					commute.setDebit(0 - onsite.getDebit());
 					commute.insert();
 				}
 				
 				if (onsiteList.size() % 2 == 1) {
 					// Negative Debit
 					onsite = (Payment)onsiteList.get(onsiteList.size() - 1);
 					onsite.setPaymentID(0);
 					onsite.setPaymentDate(new Date());
 					onsite.setPostedDate(new Date());
 					onsite.setPosted(true);
 					onsite.setDebit(0 - onsite.getDebit());
 					onsite.insert();
 				}
 				
 				// Check merchandise costs
 				Vector regMerchV = new Vector(listRegistrationMerchandise(
 						registrationID, "displayOrder", "DESC"));
 				Iterator regMerch = regMerchV.iterator();
 				Vector spouseMerchV = new Vector(listRegistrationMerchandise(
 						spouse.getRegistrationID(), "displayOrder", "DESC"));
 				Iterator spouseMerch = spouseMerchV.iterator();
 				Payment tempP = new Payment();
 
 				// Negative Debit all merchandise for to-be-deleted person
 				while (regMerch.hasNext()) {
 					Merchandise m = (Merchandise) regMerch.next();
 					tempP = new Payment();
 					tempP.setRegistration(spouse);
 					tempP.setType(m.getName() + " - " + r.getPerson().getFirstName());
 					Vector tempPayments = tempP.selectList("ORDER BY paymentDate");
 					if (tempPayments.size() % 2 == 1) {
 						tempP = (Payment)tempPayments.get(tempPayments.size() - 1);
 						tempP.setPaymentID(0);
 						tempP.setPaymentDate(new Date());
 						tempP.setPostedDate(new Date());
 						tempP.setPosted(true);
 						tempP.setDebit(0 - tempP.getDebit());
 						tempP.insert();
 					}
 				}
 				
 				// Modify merchandise for spouse
 				while (spouseMerch.hasNext()) {
 					Merchandise m = (Merchandise) spouseMerch.next();
 					tempP = new Payment();
 					tempP.setRegistration(spouse);
 					tempP.setType(m.getName() + " - " + spouse.getFirstName());
 					Vector tempPayments = tempP.selectList("ORDER BY paymentDate");
 					if (tempPayments.size() % 2 == 1) {
 						tempP = (Payment)tempPayments.get(tempPayments.size() - 1);
 						tempP.setType(m.getName());
 						tempP.persist();
 					}
 				}
 			}
 			
 			MerchandiseChoice mc = new MerchandiseChoice();
 			mc.setRegistrationID(registrationID);
 			mc.delete();
 
 			Answer a = new Answer();
 			a.setRegistrationID(registrationID);
 			a.delete();
 
 			Vector children = r.getChildRegistrations();
 			if (children.size() > 1) {
 				if (r.getSpouseComing() == 2 && r.getSpouseRegistrationID() != 0) {
 					// if spouse is coming move the children to the spouse
 					Iterator it = children.iterator();
 					while (it.hasNext()) {
 						// Delete old registration and create new (due to dbio bug with changing foreign keys)
 						ChildRegistration c = (ChildRegistration) it.next();
 						ChildRegistration delete = new ChildRegistration();
 						
 						int crID = c.getChildRegistrationID();
 						
 						c.setChildRegistrationID(0);
 						c.setRegistration(spouse);
 						if (c.insert()) {
 							delete.setChildRegistrationID(crID);
 							delete.delete();
 						}
 					}
 				} else {
 					// if spouse isn't coming just delete the kids
 					Iterator it = children.iterator();
 					while (it.hasNext()) {
 						ChildRegistration c = (ChildRegistration) it.next();
 						c.delete();
 					}
 				}
 			}
 
 			if (r.getSpouseComing() == 2 && r.getSpouseRegistrationID() != 0) {
 				spouse.setSpouseComing(1);
 				spouse.setRegisteredFirst(true);
 				spouse.update();
 				updatePayments(String.valueOf(spouse.getRegistrationID()));
 				updatePreRegistered(String.valueOf(spouse.getRegistrationID()));
 			}
 
 			r.delete();
 			
 			return true;
 		} catch (Exception e) {
 			log.error(e, e);
 			return false;
 		}
 	}
 
 	public int countReport(String reportID, String conferenceID)
 			throws DBIOEntityException {
 		Report r = getReport(reportID);
 		return countIt(r, "SELECT COUNT(*) FROM (" + r.getQuery()
 				+ conferenceID + ") DERIVEDTBL");
 	}
 
 	private List fixList(List list, int offset, int size) {
 		int maxSize = list.size();
 
 		offset = (offset <= maxSize) ? offset : maxSize;
 		int start = (offset >= 1) ? offset : 1;
 
 		int end = (offset + size - 1 > maxSize ? maxSize : offset + size - 1);
 
 		log.debug("MS-" + maxSize + " , Start-" + start + " , End-"
 				+ end);
 
 		if (maxSize > 1)
 			return list.subList(start - 1, end);
 		else
 			return list;
 	}
 
 	public boolean saveRegistration(Hashtable values) {
 		try {
 			if (values.containsKey("RegistrationID")) {
 				Registration r = getRegistration((String) values.get("RegistrationID"));
 				values.remove("RegistrationID");
 
 				r.setMappedValues(values);
 
 				return r.persist();
 			} else
 				return false;
 		} catch (Exception e) {
 			log.error(e, e);
 			return false;
 		}
 	}
 
 	public boolean saveRegistrationByType(Hashtable values) {
 		/* assumes that you pass it the appropriate registrationTypeID */
 		try {
 			if (values.containsKey("RegistrationID")) {
 				Registration r = getRegistration((String) values.get("RegistrationID"));
 				values.remove("RegistrationID");
 
 				r.setMappedValues(values);
 
 				return r.persist();
 			} else
 				return false;
 		} catch (Exception e) {
 			log.error(e, e);
 			return false;
 		}
 	}
 
 	public Collection listRegistrationAnswers(String registrationID) {
 		return listRegistrationAnswers(Integer.parseInt(registrationID));
 	}
 
 	public Collection listRegistrationAnswers(int registrationID) {
 		try {
 			Answer a = new Answer();
 			a.setRegistrationID(registrationID);
 			return a.selectList();
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public Collection listChildRegistrations(String registrationID) {
 		return listChildRegistrations(Integer.parseInt(registrationID));
 	}
 
 	public Collection listChildRegistrations(int registrationID) {
 		try {
 			ChildRegistration cr = new ChildRegistration();
 			cr.setRegistrationID(registrationID);
 			return cr.selectList();
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public Collection listRegistrationMerchandise(String registrationID,
 			String orderField, String orderDirection) {
 		return listRegistrationMerchandise(Integer.parseInt(registrationID),
 				orderField, orderDirection);
 	}
 
 	public Collection listRegistrationMerchandise(int registrationID,
 			String orderField, String orderDirection) {
 		try {
 			boolean DESC = orderDirection.equals("DESC");
 			Registration r = getRegistration(registrationID);
 
 			return r.getMerchandise(orderField, DESC);
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public Collection listRegistrationPayments(String registrationID,
 			String orderField, String orderDirection)
 			throws DBIOEntityException {
 		boolean DESC = orderDirection.equals("DESC");
 		Payment p = new Payment();
 		return p.selectList("fk_RegistrationID = '" + registrationID
 				+ "' ORDER BY "
 				+ fixOrderBy(orderField, (DESC ? "DESC" : "ASC")));
 	}
 
 	public Collection listRegistrationPaymentsWithSpouse(String registrationID,
 			String spouseRegistrationID, String orderField,
 			String orderDirection) throws DBIOEntityException {
 		boolean DESC = orderDirection.equals("DESC");
 		Payment p = new Payment();
 		return p.selectList("(fk_RegistrationID = '" + registrationID
 				+ "' OR fk_RegistrationID = '" + spouseRegistrationID
 				+ "') ORDER BY "
 				+ fixOrderBy(orderField, (DESC ? "DESC" : "ASC")));
 	}
 
 	public Collection listRegistrationPaymentsForRegistration(
 			String registrationID, String orderField, String orderDirection)
 			throws DBIOEntityException {
 		try {
 			updatePayments(registrationID);
 			updatePreRegistered(registrationID);
 			return listRegistrationPayments(registrationID, orderField,
 					orderDirection);
 		} catch (Exception e) {
 			log.error(e, e);
 			return new Vector();
 		}
 	}
 
 	public Collection listRegistrationPaymentsForRegistrationWithSpouse(
 			String registrationID, String spouseRegistrationID,
 			String orderField, String orderDirection)
 			throws DBIOEntityException {
 		try {
 			updatePaymentsWithSpouse(registrationID, spouseRegistrationID);
 			updatePreRegistered(registrationID);
 			updatePreRegistered(spouseRegistrationID);
 			return listRegistrationPaymentsWithSpouse(registrationID,
 					spouseRegistrationID, orderField, orderDirection);
 		} catch (Exception e) {
 			log.error(e, e);
 			return new Vector();
 		}
 	}
 
 	public void updatePayments(String registrationID)
 			throws DBIOEntityException, Exception {
 		Registration r = getRegistration(registrationID);
 		Conference c = r.getConference();
 		Payment commute = new Payment();
 		Payment onsite = new Payment();
 
 		RegistrationType regType = r.getRegistrationType();
 
 		commute.setRegistration(r);
 		commute.setType("Commuter Conference Cost");
 		Vector commuteList = commute.selectList("ORDER BY paymentDate");
 
 		onsite.setRegistration(r);
 		onsite.setType("Onsite Conference Cost");
 		Vector onsiteList = onsite.selectList("ORDER BY paymentDate");
 
 		int hotel = stayingInHotel(r.getRegistrationID());
 		// Check conference cost:
 		if (hotel == 0) {
 			if (onsiteList.size() % 2 == 1) {
 				// Negative Debit
 				onsite = (Payment)onsiteList.get(onsiteList.size() - 1);
 				onsite.setPaymentID(0);
 				onsite.setPaymentDate(new Date());
 				onsite.setPostedDate(new Date());
 				onsite.setPosted(true);
 				onsite.setDebit(0 - onsite.getDebit());
 				onsite.insert();
 			}
 
 			if (commuteList.size() % 2 == 0) {
 				commute.setPaymentDate(new Date());
 				commute.setPostedDate(new Date());
 				commute.setPosted(true);
 				commute.setDebit(regType.getSingleCommuteCost());
 				commute.insert();
 			}
 		} else {
 			if (commuteList.size() % 2 == 1) {
 				// Negative Debit
 				commute = (Payment)commuteList.get(commuteList.size() - 1);
 				commute.setPaymentID(0);
 				commute.setPaymentDate(new Date());
 				commute.setPostedDate(new Date());
 				commute.setPosted(true);
 				commute.setDebit(0 - commute.getDebit());
 				commute.insert();
 			}
 
 			if (onsiteList.size() % 2 == 0) {
 				onsite.setPaymentDate(new Date());
 				onsite.setPostedDate(new Date());
 				onsite.setPosted(true);
 				onsite.setDebit(regType.getSingleOnsiteCost());
 				onsite.insert();
 			}
 		}
 
 		// Delete all merchandise payments for this registration
 		/*
 		 * Merchandise merch = new Merchandise(); merch.setConference(c);
 		 * Collection confMerch = merch.selectList(); Iterator cmi =
 		 * confMerch.iterator(); while(cmi.hasNext()) { Merchandise currMerch =
 		 * (Merchandise)cmi.next(); Payment tempPayment = new Payment();
 		 * tempPayment.setRegistration(r);
 		 * tempPayment.setType(currMerch.getName()); tempPayment.select(); if (
 		 * !tempPayment.isPKEmpty() ) { tempPayment.delete(); } }
 		 */
 		// Check that all merchandise has been billed for
 		Collection mi = listRegistrationMerchandise(registrationID,	"displayOrder", "DESC");
 		Payment tempP = new Payment();
 
 		Iterator confMerch = listMerchandise(String.valueOf(r.getConferenceID()), 
 				r.getRegistrationTypeID(), "displayOrder", "DESC").iterator();
 		while (confMerch.hasNext()) {
 			Merchandise m = (Merchandise) confMerch.next();
 			tempP = new Payment();
 			tempP.setRegistration(r);
 			tempP.setType(m.getName());
 			Vector tempPayments = tempP.selectList("ORDER BY paymentDate");
 			
 			if( mi.contains(m) ) {
 				if (tempPayments.size() % 2 == 0) {
 					tempP.setPaymentDate(new Date());
 					tempP.setPostedDate(new Date());
 					tempP.setPosted(true);
 					tempP.setDebit(m.getAmount());
 					tempP.insert();
 				}
 			} else {
 				if (tempPayments.size() % 2 == 1) {
 					// Negative Debit
 					tempP = (Payment)tempPayments.get(tempPayments.size() - 1);
 					tempP.setPaymentID(0);
 					tempP.setPaymentDate(new Date());
 					tempP.setPostedDate(new Date());
 					tempP.setPosted(true);
 					tempP.setDebit(0 - tempP.getDebit());
 					tempP.insert();
 				}
 			}
 		}
 	}
 
 	public void updatePaymentsWithSpouse(String registrationID,
 			String spouseRegistrationID) throws DBIOEntityException, Exception {
 		Registration r = getRegistration(registrationID);
 		Registration spouse = getRegistration(spouseRegistrationID);
 		Registration firstReg = r.isRegisteredFirst() ? r : spouse;
 		String paymentTypesComment = "";
 		String paymentHousingComment = "";
 
 		RegistrationType regType = r.getRegistrationType();
 		RegistrationType sregType = spouse.getRegistrationType();
 
 		Conference c = r.getConference();
 		Payment commute = new Payment();
 		Payment onsite = new Payment();
 
 		int hotel = stayingInHotel(r.getRegistrationID());
 		int hotelSpouse = stayingInHotel(spouse.getRegistrationID());
 
 		/* add a string for married couples of different registration types */
 		if (hotel == hotelSpouse
 				&& regType.getRegistrationTypeID() == sregType.getRegistrationTypeID()) {
 			paymentTypesComment = " - Married";
 		} else {
 			paymentTypesComment = " - Married [average]";
 		}
 		/* add a string for married couples of different housing types */
 		/*
 		 * if (hotel==hotelSpouse){ paymentHousingComment=""; } else{
 		 * paymentHousingComment=""; }
 		 */
 		commute.setRegistration(firstReg);
 		commute.setType("Commuter Conference Cost" + paymentTypesComment);
 		Vector commuteList = commute.selectList("ORDER BY paymentDate");
 
 		onsite.setRegistration(firstReg);
 		onsite.setType("Onsite Conference Cost" + paymentTypesComment);
 		Vector onsiteList = onsite.selectList("ORDER BY paymentDate");
 
 		// Check conference cost:
 /*		if ((hotel == hotelSpouse) || (hotel == 1 && hotelSpouse == 3)
 				|| (hotel == 3 && hotelSpouse == 1)) {
 			/* both staying or commuting but different types */
 			if (hotel == 0 && hotelSpouse == 0) {
 				if (onsiteList.size() % 2 == 1) {
 					// Negative Debit
 					onsite = (Payment)onsiteList.get(onsiteList.size() - 1);
 					onsite.setPaymentID(0);
 					onsite.setPaymentDate(new Date());
 					onsite.setPostedDate(new Date());
 					onsite.setPosted(true);
 					onsite.setDebit(0 - onsite.getDebit());
 					onsite.insert();
 				}
 	
 				if (commuteList.size() % 2 == 0) {
 					commute.setPaymentDate(new Date());
 					commute.setPostedDate(new Date());
 					commute.setPosted(true);
 					commute.setDebit((regType.getMarriedCommuteCost() + sregType.getMarriedCommuteCost()) / 2);
 					commute.insert();
 				}
 			} else {  /* 	If only one is staying onsite, do the married onsite cost too, per Amy Weiss...
 							[15:48] amyweiss10: just charge them the onsite married rate.
 							[15:48] amyweiss10: That way, they will probably scream
 							[15:48] amyweiss10: and in that way,
 							[15:48] amyweiss10: a flag will be raised for the admin
 							[15:48] amyweiss10: who can then adjust things manually as appropriate. */
 				if (commuteList.size() % 2 == 1) {
 					// Negative Debit
 					commute = (Payment)commuteList.get(commuteList.size() - 1);
 					commute.setPaymentID(0);
 					commute.setPaymentDate(new Date());
 					commute.setPostedDate(new Date());
 					commute.setPosted(true);
 					commute.setDebit(0 - commute.getDebit());
 					commute.insert();
 				}
 	
 				if (onsiteList.size() % 2 == 0) {
 					onsite.setPaymentDate(new Date());
 					onsite.setPostedDate(new Date());
 					onsite.setPosted(true);
 					onsite.setDebit((regType.getMarriedOnsiteCost() + sregType.getMarriedOnsiteCost()) / 2);
 					onsite.insert();
 				}
 			}
 /*		} 
  		else { /* one commuting, the other staying STILL TODO */
 		/* FIXME: This may need to be fixed!! */
 /*			if (hotel == 0) {
 				if (commute.isPKEmpty()) {
 					commute.setPaymentDate(new Date());
 					commute.setPostedDate(new Date());
 					commute.setPosted(true);
 					commute.setDebit(regType.getMarriedCommuteCost() / 2);
 					commute.insert();
 				}
 			} else if (hotel == 1) {
 				if (onsite.isPKEmpty()) {
 					onsite.setPaymentDate(new Date());
 					onsite.setPostedDate(new Date());
 					onsite.setPosted(true);
 					onsite.setDebit(regType.getMarriedOnsiteCost() / 2);
 					onsite.insert();
 				}
 			} else {
 				if (!commute.isPKEmpty()) {
 					commute.delete();
 				}
 
 				if (onsite.isPKEmpty()) {
 					onsite.setPaymentDate(new Date());
 					onsite.setPostedDate(new Date());
 					onsite.setPosted(true);
 					onsite.setDebit(regType.getMarriedOnsiteCost() / 2);
 					onsite.insert();
 				}
 			}
 			if (hotelSpouse == 0) {
 				if (commute.isPKEmpty()) {
 					commute.setPaymentDate(new Date());
 					commute.setPostedDate(new Date());
 					commute.setPosted(true);
 					commute.setDebit(sregType.getMarriedCommuteCost() / 2);
 					commute.insert();
 				}
 			} else if (hotelSpouse == 1) {
 				if (onsite.isPKEmpty()) {
 					onsite.setPaymentDate(new Date());
 					onsite.setPostedDate(new Date());
 					onsite.setPosted(true);
 					onsite.setDebit(sregType.getMarriedOnsiteCost() / 2);
 					onsite.insert();
 				}
 			} else {
 				if (!commute.isPKEmpty()) {
 					commute.delete();
 				}
 
 				if (onsite.isPKEmpty()) {
 					onsite.setPaymentDate(new Date());
 					onsite.setPostedDate(new Date());
 					onsite.setPosted(true);
 					onsite.setDebit(sregType.getMarriedOnsiteCost() / 2);
 					onsite.insert();
 				}
 			}
 		}
 */
 		// Delete all merchandise payments for this registration
 		/*
 		 * Merchandise merch = new Merchandise(); merch.setConference(c);
 		 * Collection confMerch = merch.selectList(); Iterator cmi =
 		 * confMerch.iterator(); while(cmi.hasNext()) { Merchandise currMerch =
 		 * (Merchandise)cmi.next(); Payment tempPayment = new Payment();
 		 * tempPayment.setRegistration(r);
 		 * tempPayment.setType(currMerch.getName()); tempPayment.select(); if (
 		 * !tempPayment.isPKEmpty() ) { tempPayment.delete(); } }
 		 */
 		// Check that all merchandise has been billed for
 		Collection mi = listRegistrationMerchandise(registrationID,	"displayOrder", "DESC");
 		Collection spouseMerch = listRegistrationMerchandise(spouseRegistrationID,	"displayOrder", "DESC");
 		Payment tempP = new Payment();
 		
 		Iterator confMerch = listMerchandise(String.valueOf(r.getConferenceID()), 
 				r.getRegistrationTypeID(), "displayOrder", "DESC").iterator();
 		while (confMerch.hasNext()) {
 			Merchandise m = (Merchandise) confMerch.next();
 			tempP = new Payment();
 			tempP.setRegistration(firstReg);
 			tempP.setType(m.getName() + " - " + r.getFirstName());
 			Vector tempPayments = tempP.selectList("ORDER BY paymentDate");
 			
 			if( mi.contains(m) ) {
 				if (tempPayments.size() % 2 == 0) {
 					tempP.setPaymentDate(new Date());
 					tempP.setPostedDate(new Date());
 					tempP.setPosted(true);
 					tempP.setDebit(m.getAmount());
 					tempP.insert();
 				}
 			} else {
 				if (tempPayments.size() % 2 == 1) {
 					// Negative Debit
 					tempP = (Payment)tempPayments.get(tempPayments.size() - 1);
 					tempP.setPaymentID(0);
 					tempP.setPaymentDate(new Date());
 					tempP.setPostedDate(new Date());
 					tempP.setPosted(true);
 					tempP.setDebit(0 - tempP.getDebit());
 					tempP.insert();
 				}
 			}
 		}
 		
 		confMerch = listMerchandise(String.valueOf(spouse.getConferenceID()), 
 				spouse.getRegistrationTypeID(), "displayOrder", "DESC").iterator();
 		while (confMerch.hasNext()) {
 			Merchandise m = (Merchandise) confMerch.next();
 			tempP = new Payment();
 			tempP.setRegistration(firstReg);
 			tempP.setType(m.getName() + " - " + spouse.getFirstName());
 			Vector tempPayments = tempP.selectList("ORDER BY paymentDate");
 			
 			if( spouseMerch.contains(m) ) {
 				if (tempPayments.size() % 2 == 0) {
 					tempP.setPaymentDate(new Date());
 					tempP.setPostedDate(new Date());
 					tempP.setPosted(true);
 					tempP.setDebit(m.getAmount());
 					tempP.insert();
 				}
 			} else {
 				if (tempPayments.size() % 2 == 1) {
 					// Negative Debit
 					tempP = (Payment)tempPayments.get(tempPayments.size() - 1);
 					tempP.setPaymentID(0);
 					tempP.setPaymentDate(new Date());
 					tempP.setPostedDate(new Date());
 					tempP.setPosted(true);
 					tempP.setDebit(0 - tempP.getDebit());
 					tempP.insert();
 				}
 			}
 		}
 	}
 
 	public int stayingInHotel(int registrationID) {
 		// 0 = not staying in hotel
 		// 1 = staying in hotel
 		// 3 = No option to stay in a hotel(no quesitonTextID = 2)
 		Registration r = getRegistration(registrationID);
 
 		Question q = new Question();
 		if (r.getConferenceID() == 0) {
 			return 3;
 		}
 		q.setConferenceID(r.getConferenceID());
 		q.setQuestionTextID(2);
 		q.setRegistrationTypeID(r.getRegistrationTypeID());
 		q.select();
 
 		if (q.isPKEmpty()) {
 			return 3;
 		} else {
 			Answer a = new Answer();
 			a.setQuestionID(q.getQuestionID());
 			a.setRegistrationID(registrationID);
 			a.select();
 
 			return a.getBody().equals("N") ? 0 : 1;
 		}
 	}
 
 	//********************************************************************************//
 	//  payment Info Code
 	//  Mostly by David Bowdoin, starting 7/15/2002
 	//********************************************************************************//
 
 	//Created: 7/17/02, DMB
 	//  Hashtable paymentInfo expects
 	// (ccNum,ccExpM,ccExpY,ccAmt,FirstName,LastName,Address,City,State,Zip,Country,Phone,Email
 	// )
 	public Hashtable paymentCreditCard(String registrationID,
 			Hashtable ccPaymentInfo) throws Exception {
 		Registration registration = getRegistration(registrationID);
 		ccPaymentInfo.put("InvoiceNum", registrationID);
 		ccPaymentInfo.put("Description", registration.getConference().getName());
 		ccPaymentInfo.put("CustID", registration.getPerson().getEmail());
 
 		OnlinePayment onlinePay = new OnlinePayment();
 		// Debug Note:
 		// Uncomment the line below to use the Visa Test Card # 4007000000027
 		// (See OnlinePayment.java for more testing information)
 		// BE SURE to comment it back out before checking the code back in, or
 		// CC transactions WILL NOT WORK!
 		onlinePay.setTestMode(false); //TODO: change true to false, to disable
 		// testing mode. (Or comment out this
 		// line)
 		onlinePay.setMerchantInfo(registration.getConference()
 				.getMerchantAcctNum(), registration.getConference()
 				.getAuthnetPassword());
 		Hashtable results = onlinePay.processCreditCard(ccPaymentInfo);
 
 		Hashtable paymentHash = new Hashtable();
 
 		if (results.get("Status").equals("Success")) {
 			String authCode = (String) results.get("AuthCode");
 			log.info("Successful credit card transaction; AuthCode:" + authCode);
 			Payment payment = new Payment();
 			payment.setCredit(Float.parseFloat((String) ccPaymentInfo.get("PaymentAmt")));
 			payment.setAuthCode(authCode);
 			payment.setType("Credit card payment");
 			payment.setPosted(true);
 			payment.setPaymentDate(new Date());
 			payment.setPostedDate(new Date());
 			registration.assocPayment(payment);
 			paymentHash.put("PaymentID", String.valueOf(payment.getPaymentID()));
 
 			addPreRegDiscount(registrationID);
 			addFullPayDiscount(registrationID);
 			updatePreRegistered(registrationID);
 
 			String ccNum = (String) ccPaymentInfo.get("CCNum");
 			String codedCCNum = "****"
 					+ ccNum.substring(ccNum.length() - 4, ccNum.length());
 			//            paymentHash.put("codedCCNum", codedCCNum);
 
 			org.alt60m.crs.util.EmailConfirm email = new org.alt60m.crs.util.EmailConfirm();
 			email.createCreditCardReceipt(payment, codedCCNum,
 					(String) ccPaymentInfo.get("CCExpM"),
 					(String) ccPaymentInfo.get("CCExpY"),
 					ccPaymentInfo.get("PaymentAmt"));
 			email.send();
 		} else {
 			String response = (String) results.get("Response");
 			//TODO: Throws exception when it can't connect..
 			//Results: {Status=Could not connect to payment system. Please try
 			// again later.}
 			log.info("Unable to process credit card.  Reason from authnet: " + response);
 			paymentHash.put(
 					"ErrorMessage",
 					response == null ? "Could not connect to payment system. Please try again later."
 							: response);
 		}
 
 		paymentHash.put("Status", results.get("Status"));
 
 		return paymentHash;
 	}
 
 	public Hashtable paymentScholarship(String registrationID,
 			Hashtable paymentInfo) throws Exception {
 		Payment payment = new Payment();
 		Registration reg = getRegistration(registrationID);
 		Hashtable paymentHash = new Hashtable();
 
 		payment.setCredit(Float.parseFloat((String) paymentInfo.get("PaymentAmt")));
 		payment.setType("Scholarship");
 		payment.setComment((String) paymentInfo.get("Comment"));
 		payment.setPaymentDate(new Date());
 
 		reg.assocPayment(payment);
 		paymentHash.put("PaymentID", String.valueOf(payment.getPaymentID()));
 
 		org.alt60m.crs.util.EmailConfirm email = new org.alt60m.crs.util.EmailConfirm();
 		email.createScholarshipReceipt(payment, paymentInfo.get("PaymentAmt"));
 		email.send();
 
 		if (reg.getSpouseComing()==2){
 			String sregID = String.valueOf(reg.getSpouseRegistrationID());
 			addPreRegDiscountMarried(registrationID,sregID);
 			addFullPayDiscount(registrationID,sregID);
 			updatePreRegistered(registrationID,sregID);
 		} else{
 			addPreRegDiscount(registrationID);
 			addFullPayDiscount(registrationID);
 			updatePreRegistered(registrationID);
 		}
 
 		return paymentHash;
 	}
 
 	public Hashtable paymentStaffAccountTransfer(String registrationID,
 			Hashtable paymentInfo) throws Exception {
 		Payment payment = new Payment();
 		Registration reg = getRegistration(registrationID);
 		Hashtable paymentHash = new Hashtable();
 
 		payment.setCredit(Float.parseFloat((String) paymentInfo.get("PaymentAmt")));
 		payment.setType("Staff Account Transfer");
 		payment.setComment((String) paymentInfo.get("Comment"));
 		payment.setAccountNo((String) paymentInfo.get("AccountNumber"));
 		payment.setPaymentDate(new Date());
 
 		reg.assocPayment(payment);
 		paymentHash.put("PaymentID", String.valueOf(payment.getPaymentID()));
 
 		org.alt60m.crs.util.EmailConfirm email = new org.alt60m.crs.util.EmailConfirm();
 		email.createStaffAccountTransferReceipt(payment,
 				(String) paymentInfo.get("AccountNumber"),
 				paymentInfo.get("PaymentAmt"));
 		email.send();
 
 		if (reg.getSpouseComing()==2){
 			String sregID = String.valueOf(reg.getSpouseRegistrationID());
 			addPreRegDiscountMarried(registrationID,sregID);
 			addFullPayDiscount(registrationID,sregID);
 			updatePreRegistered(registrationID,sregID);
 		} else{
 			addPreRegDiscount(registrationID);
 			addFullPayDiscount(registrationID);
 			updatePreRegistered(registrationID);
 		}
 
 		return paymentHash;
 	}
 
 	public Hashtable paymentMinistryAccountTransfer(String registrationID,
 			Hashtable paymentInfo) throws Exception {
 		Payment payment = new Payment();
 		Registration reg = getRegistration(registrationID);
 		Hashtable paymentHash = new Hashtable();
 
 		payment.setCredit(Float.parseFloat((String) paymentInfo.get("PaymentAmt")));
 		payment.setType("Ministry Account Transfer");
 		payment.setComment((String) paymentInfo.get("Ministry"));
 		payment.setBusinessUnit((String) paymentInfo.get("BU"));
 		payment.setProject((String) paymentInfo.get("Project"));
 		payment.setDept((String) paymentInfo.get("Dept"));
 		payment.setOperatingUnit((String) paymentInfo.get("OU"));
 		payment.setPaymentDate(new Date());
 
 		reg.assocPayment(payment);
 		paymentHash.put("PaymentID", String.valueOf(payment.getPaymentID()));
 
 		org.alt60m.crs.util.EmailConfirm email = new org.alt60m.crs.util.EmailConfirm();
 		email.createMinistryAccountTransferReceipt(payment,
 				(String) paymentInfo.get("Ministry"),
 				paymentInfo.get("PaymentAmt"));
 		email.send();
 
 		if (reg.getSpouseComing()==2){
 			String sregID = String.valueOf(reg.getSpouseRegistrationID());
 			addPreRegDiscountMarried(registrationID,sregID);
 			addFullPayDiscount(registrationID,sregID);
 			updatePreRegistered(registrationID,sregID);
 		} else{
 			addPreRegDiscount(registrationID);
 			addFullPayDiscount(registrationID);
 			updatePreRegistered(registrationID);
 		}
 
 		return paymentHash;
 	}
 
 	public void forceAddPreRegDiscount(String registrationID) throws Exception {
 		Registration reg = getRegistration(registrationID);
 		int spouseComing = reg.getSpouseComing();
 		
 		float preRegDeposit = 0;
 		float earlyRegDisc = 0;
 		if (spouseComing == Registration.SPOUSE_COMING) {
 			Registration regSpouse = reg.getSpouse();
 			RegistrationType rt = reg.getRegistrationType();
 			RegistrationType rts = regSpouse.getRegistrationType();
 			preRegDeposit = (rt.getMarriedPreRegDeposit() + rts.getMarriedPreRegDeposit()) / 2;
 			earlyRegDisc = (rt.getMarriedDiscountEarlyReg() + rts.getMarriedDiscountEarlyReg()) / 2;
 		} else {
 			RegistrationType rt = reg.getRegistrationType();
 			preRegDeposit = rt.getSinglePreRegDeposit();
 			earlyRegDisc = rt.getSingleDiscountEarlyReg();
 		}
 
 		if ((!reg.getPreRegistered()) && (preRegDeposit > 0.0)
 				&& (earlyRegDisc > 0.0)) {
 			Payment discountEarlyRegPayment = new Payment();
 			discountEarlyRegPayment.setDebit(-(earlyRegDisc));
 			discountEarlyRegPayment.setType("Early Registration Discount");
 			discountEarlyRegPayment.setPosted(true);
 			discountEarlyRegPayment.setPaymentDate(new Date());
 			discountEarlyRegPayment.setPostedDate(new Date());
 			discountEarlyRegPayment.setRegistration(reg);
 			discountEarlyRegPayment.insert();
 		}
 	}
 
 	public void forceAddPreRegDiscountMarried(String registrationID,
 			String spouseRegistrationID) throws Exception {
 		Registration reg = getRegistration(registrationID);
 		RegistrationType rt = reg.getRegistrationType();
 
 		Registration sreg = getRegistration(spouseRegistrationID);
 		RegistrationType srt = sreg.getRegistrationType();
 
 		//average the costs of the 2 types
 		float preRegDeposit = (rt.getMarriedPreRegDeposit() + srt.getMarriedPreRegDeposit()) / (float)2;
 		float earlyRegDisc = (rt.getMarriedDiscountEarlyReg() + srt.getMarriedDiscountEarlyReg()) / (float)2;
 
 		if ((!reg.getPreRegistered()) && (!sreg.getPreRegistered())
 				&& (preRegDeposit > 0.0) && (earlyRegDisc > 0.0)) {
 			Payment discountEarlyRegPayment = new Payment();
 			discountEarlyRegPayment.setDebit(-(earlyRegDisc));
 			discountEarlyRegPayment.setType("Early Registration Discount");
 			discountEarlyRegPayment.setPosted(true);
 			discountEarlyRegPayment.setPaymentDate(new Date());
 			discountEarlyRegPayment.setPostedDate(new Date());
 			discountEarlyRegPayment.setRegistration(reg);
 			discountEarlyRegPayment.insert();
 		}
 	}
 
 	public void addPreRegDiscount(String registrationID)
 			throws DBIOEntityException, DBIOException, java.sql.SQLException,
 			Exception {
 		Registration reg = getRegistration(registrationID);
 
 		float credits = getRegistrationCredits(registrationID);
 
 		RegistrationType rt = reg.getRegistrationType();
 		float preRegDeposit = rt.getSinglePreRegDeposit();
 		float earlyRegDisc = rt.getSingleDiscountEarlyReg();
 
 		java.text.SimpleDateFormat redoDate = new java.text.SimpleDateFormat(
 				"MM-dd-yyyy"); //Strips off the minutes for date compare
 		Date today = redoDate.parse(redoDate.format(new Date()));
 
 		if (!((reg.getSpouseComing()==2)&&(reg.getSpouseRegistrationID()>0))){
 	//		if (!reg.getPreRegistered() && credits >= preRegDeposit
 			if (credits >= preRegDeposit  // getPreReg is better tested with checkForPreRegDiscount
 					&& earlyRegDisc > 0
 					&& rt.getSingleDiscountEarlyRegDate() != null
 					&& today.compareTo(rt.getSingleDiscountEarlyRegDate()) <= 0
 					&& !checkForPreRegDiscount(registrationID)) {
 				Payment payment = new Payment();
 				payment.setDebit(-earlyRegDisc);
 				payment.setType("Early Registration Discount");
 				payment.setPosted(true);
 				payment.setPaymentDate(new Date());
 				payment.setPostedDate(new Date());
 				payment.setRegistration(reg);
 				payment.insert();
 			}
 		} else {
 			String spouseRegistrationID = String.valueOf(reg.getSpouseRegistrationID());
 			Registration sreg = getRegistration(spouseRegistrationID);
 			RegistrationType srt = sreg.getRegistrationType();
 			//average the discount and deposit of the 2 types
 			preRegDeposit = (rt.getMarriedPreRegDeposit() + srt.getMarriedPreRegDeposit()) / (float)2;
 			earlyRegDisc = (rt.getMarriedDiscountEarlyReg() + srt.getMarriedDiscountEarlyReg()) / (float)2;
 			
 			if (credits >= preRegDeposit  // getPreReg is better tested with checkForPreRegDiscount
 					&& earlyRegDisc > 0
 					&& rt.getMarriedDiscountEarlyRegDate() != null
 					&& srt.getMarriedDiscountEarlyRegDate() != null
 					&& today.compareTo(rt.getMarriedDiscountEarlyRegDate()) <= 0
 					&& today.compareTo(srt.getMarriedDiscountEarlyRegDate()) <= 0
 					&& !checkForPreRegDiscount(registrationID,spouseRegistrationID)) {
 				Payment payment = new Payment();
 				payment.setDebit(-earlyRegDisc);
 				payment.setType("Early Registration Discount");
 				payment.setPosted(true);
 				payment.setPaymentDate(new Date());
 				payment.setPostedDate(new Date());
 				payment.setRegistration(reg);
 				payment.insert();
 			}
 		}
 	}
 
 	public void addPreRegDiscountMarried(String registrationID,
 			String spouseRegistrationID) throws DBIOEntityException,
 			DBIOException, java.sql.SQLException, Exception {
 		Registration reg = getRegistration(registrationID);
 		RegistrationType rt = reg.getRegistrationType();
 
 		Registration sreg = getRegistration(spouseRegistrationID);
 		RegistrationType srt = sreg.getRegistrationType();
 
 		float credits = getRegistrationCredits(registrationID)
 				+ getRegistrationCredits(spouseRegistrationID);
 
 		//average the costs of the 2 types
 		float preRegDeposit = (rt.getMarriedPreRegDeposit() + srt.getMarriedPreRegDeposit()) / (float)2;
 		float earlyRegDisc = (rt.getMarriedDiscountEarlyReg() + srt.getMarriedDiscountEarlyReg()) / (float)2;
 
 		java.text.SimpleDateFormat redoDate = new java.text.SimpleDateFormat(
 				"MM-dd-yyyy"); //Strips
 		// off the minutes for date compare
 
 		Date today = redoDate.parse(redoDate.format(new Date()));
 
 //		if ((!reg.getPreRegistered())
 //				&& (!sreg.getPreRegistered())
 		if (credits >= preRegDeposit  // getPreReg is better tested with checkForPreRegDiscount
 				&& earlyRegDisc > 0
 				&& rt.getMarriedDiscountEarlyRegDate() != null
 				&& srt.getMarriedDiscountEarlyRegDate() != null
 				&& today.compareTo(rt.getMarriedDiscountEarlyRegDate()) <= 0
 				&& today.compareTo(srt.getMarriedDiscountEarlyRegDate()) <= 0
 				&& !checkForPreRegDiscount(registrationID, spouseRegistrationID)) {
 			Payment payment = new Payment();
 			payment.setDebit(-earlyRegDisc);
 			payment.setType("Early Registration Discount (Married)");
 			payment.setPosted(true);
 			payment.setPaymentDate(new Date());
 			payment.setPostedDate(new Date());
 			payment.setRegistration(reg);
 			payment.insert();
 		}
 	}
 
 	//Returns True, after the discount has been applied
 	public boolean checkForPreRegDiscount(String registrationID)
 			throws DBIOEntityException {
 		Payment p = new Payment();
 		p.setRegistration(getRegistration(registrationID));
 		p.setType("Early Registration Discount");
 		Vector payments = p.selectList();
 
 		return (payments.size() % 2 == 1);
 	}
 
 	public boolean checkForPreRegDiscount(String registrationID,
 			String spouseRegistrationID) throws DBIOEntityException {
 		Payment p = new Payment();
 		p.setRegistration(getRegistration(registrationID));
 		p.setType("Early Registration Discount (Married)");
 		Vector payments = p.selectList();
 		//make sure we did not specify the wrong person
 		Payment q = new Payment();
 		q.setRegistration(getRegistration(spouseRegistrationID));
 		q.setType("Early Registration Discount (Married)");
 		Vector paymentsTwo = q.selectList();
 
 		return (payments.size() % 2 == 1 ||
 				paymentsTwo.size() % 2 == 1);
 	}
 
 	public void addFullPayDiscount(String registrationID) throws Exception {
 		Registration reg = getRegistration(registrationID);
 
 		RegistrationType rt = reg.getRegistrationType();
 		float fullRegDisc = rt.getSingleDiscountFullPayment();
 		float totalPayments = getRegistrationCredits(registrationID);
 		float totalDue = getRegistrationDebits(registrationID) - fullRegDisc;
 
 		if ((fullRegDisc > 0.0) && (totalPayments >= totalDue)
 				&& !checkForFullPayDiscount(registrationID)) {
 			Payment discountFullPayment = new Payment();
 			/*
 			 * float debitDisc =
 			 * -(reg.getConference().getDiscountFullPayment());
 			 */
 			discountFullPayment.setDebit(-fullRegDisc);
 			discountFullPayment.setType("Full Payment Discount");
 			discountFullPayment.setPosted(true);
 			discountFullPayment.setPaymentDate(new Date());
 			discountFullPayment.setPostedDate(new Date());
 			reg.assocPayment(discountFullPayment);
 		}
 	}
 
 	public void addFullPayDiscount(String registrationID,
 			String spouseRegistrationID) throws Exception {
 		Registration reg = getRegistration(registrationID);
 		RegistrationType rt = reg.getRegistrationType();
 		Registration sreg = getRegistration(spouseRegistrationID);
 		RegistrationType srt = sreg.getRegistrationType();
 
 		float fullRegDisc = (rt.getMarriedDiscountFullPayment() + srt.getMarriedDiscountFullPayment()) / (float)2;
 		float totalPayments = getRegistrationCredits(registrationID)
 				+ getRegistrationCredits(spouseRegistrationID);
 		float totalDue = getRegistrationDebits(registrationID)
 				+ getRegistrationDebits(spouseRegistrationID) - fullRegDisc;
 
 		if ((fullRegDisc > 0.0)
 				&& (totalPayments >= totalDue)
 				&& !checkForFullPayDiscount(registrationID,
 						spouseRegistrationID)) {
 			Payment discountFullPayment = new Payment();
 			discountFullPayment.setDebit(-fullRegDisc);
 			discountFullPayment.setType("Full Payment Discount (Married)");
 			discountFullPayment.setPosted(true);
 			discountFullPayment.setPaymentDate(new Date());
 			discountFullPayment.setPostedDate(new Date());
 			reg.assocPayment(discountFullPayment);
 		}
 	}
 
 	//Returns True, after the discount has been applied
 	public boolean checkForFullPayDiscount(String registrationID)
 			throws DBIOEntityException {
 		Payment p = new Payment();
 		p.setRegistration(getRegistration(registrationID));
 		p.setType("Full Payment Discount");
 		Vector payments = p.selectList();
 
 		return (payments.size() % 2 == 1);
 	}
 
 	public boolean checkForFullPayDiscount(String registrationID,
 			String spouseRegistrationID) throws DBIOEntityException {
 		Registration r = getRegistration(registrationID);
 		Payment p = new Payment();
 		p.setType("Full Payment Discount (Married)");
 		if (r.isRegisteredFirst()){
 			p.setRegistration(r);
 		}
 		else{
 			p.setRegistration(getRegistration(spouseRegistrationID));
 		}
 		Vector payments = p.selectList();
 		return (payments.size() % 2 == 1);
 	}
 
 	public void updatePreRegistered(String registrationID) throws Exception {
 		Registration reg = getRegistration(registrationID);
 		if (reg.getSpouseComing() == 2 && reg.getSpouseRegistrationID() > 0) {
 			updatePreRegistered(registrationID,String.valueOf(reg.getSpouseRegistrationID()));
 		}
 		else{
 			float preRegDeposit = reg.getRegistrationType()
 					.getSinglePreRegDeposit();
 	
 			float credits = getRegistrationCredits(registrationID);
 			if (credits >= preRegDeposit) {
 				reg.setPreRegistered(true);
 				reg.update();
 			}
 		}
 	}
 
 	public void updatePreRegistered(String registrationID,
 			String spouseRegistrationID) throws Exception {
 		Registration reg = getRegistration(registrationID);
 		Registration sreg = getRegistration(spouseRegistrationID);
 		float preRegDeposit = (
 			reg.getRegistrationType().getMarriedPreRegDeposit()
 			+ sreg.getRegistrationType().getMarriedPreRegDeposit()) / (float)2;
 
 		float credits = getRegistrationCredits(registrationID,
 				spouseRegistrationID);
 		if (credits >= preRegDeposit) {
 			reg.setPreRegistered(true);
 			reg.update();
 			sreg.setPreRegistered(true);
 			sreg.update();
 		}
 	}
 
 	public float getRegistrationBalance(String registrationID)
 			throws DBIOException, java.sql.SQLException {
 		return getRegistrationCredits(registrationID)
 				- getRegistrationDebits(registrationID);
 	}
 
 	public float getRegistrationBalance(String registrationID,
 			String spouseRegistrationID) throws DBIOException,
 			java.sql.SQLException {
 		return getRegistrationCredits(registrationID)
 				- getRegistrationDebits(registrationID)
 				+ getRegistrationCredits(spouseRegistrationID)
 				- getRegistrationDebits(spouseRegistrationID);
 	}
 
 	public float getRegistrationCredits(String registrationID) {
 		try {
 			if (!"".equals(registrationID)) {
 				Registration r = new Registration();
 
 				com.kenburcham.framework.dbio.DBIOTransaction tx = r.getTransaction();
 				tx
 						.setSQL("SELECT SUM(credit) FROM crs_payment WHERE fk_RegistrationID = '"
 								+ registrationID + "'");
 				tx.getRecords();
 				java.sql.ResultSet mine = tx.getResultSet();
 				if (mine.next()) {
 					return mine.getFloat(1);
 				} else {
 					return 0;
 				}
 			} else {
 				return 0;
 			}
 
 		} catch (Exception e) {
 			log.error(e, e);
 			return 0;
 		}
 	}
 
 	public float getRegistrationCredits(String registrationID,
 			String spouseRegistrationID) {
 		float credits = 0;
 		com.kenburcham.framework.dbio.DBIOTransaction tx = null;
 		try {
 			if ((!"".equals(registrationID))
 					&& (!"".equals(spouseRegistrationID))) {
 				Registration r = new Registration();
 
 				tx = r.getTransaction();
 				tx.setSQL("SELECT SUM(credit) FROM crs_payment WHERE fk_RegistrationID = '"
 						+ registrationID
 						+ "' or fk_RegistrationID = '"
 								+ spouseRegistrationID + "'");
 				tx.getRecords();
 				java.sql.ResultSet mine = tx.getResultSet();
 				if (mine.next()) {
 					credits = mine.getFloat(1);
 				}
 			}
 			return credits;
 
 		} catch (Exception e) {
 			log.error(e, e);
 			return 0;
 		} finally
 		{
 			if (tx != null)
 			{
 				tx.close();
 				tx = null;
 			}
 		}
 	}
 
 	public float getRegistrationDebits(String registrationID)
 			throws DBIOException, java.sql.SQLException {
 		Registration r = new Registration();
 
 		try {
 			com.kenburcham.framework.dbio.DBIOTransaction tx = r
 					.getTransaction();
 			tx
 					.setSQL("SELECT SUM(debit) FROM crs_payment WHERE fk_RegistrationID = '"
 							+ registrationID + "'");
 			tx.getRecords();
 			java.sql.ResultSet mine = tx.getResultSet();
 			if (mine.next()) {
 				return mine.getFloat(1);
 			} else {
 				return 0;
 			}
 		} finally {
 			r.close();
 		}
 	}
 
 	public float getRegistrationDebits(String registrationID,
 			String spouseRegistrationID) throws DBIOException,
 			java.sql.SQLException {
 		Registration r = new Registration();
 		try {
 		com.kenburcham.framework.dbio.DBIOTransaction tx = r.getTransaction();
 		tx.setSQL("SELECT SUM(debit) FROM crs_payment WHERE fk_RegistrationID = '"
 				+ registrationID
 				+ "' or fk_RegistrationID = '"
 				+ spouseRegistrationID + "'");
 		tx.getRecords();
 			java.sql.ResultSet mine = tx.getResultSet();
 			if (mine.next()) {
 				return mine.getFloat(1);
 			} else {
 				return 0;
 			}
 		} 
 		finally
 		{
 			r.close();
 		}
 	
 	}
 
 	//Created: 7/18/02, DMB
 	// Returns the discounts that are still available, if they paid on the date
 	// 'today'
 	public Hashtable getDiscountsAvailable(String registrationID, Date today)
 			throws Exception {
 		Hashtable results = new Hashtable();
 		Registration reg = getRegistration(registrationID);
 		float credits = getRegistrationCredits(registrationID);
 		float balanceDue = getRegistrationDebits(registrationID) - credits;
 
 		RegistrationType rt = reg.getRegistrationType();
 		Date discountEarlyRegDate = rt.getSingleDiscountEarlyRegDate();
 		float discountEarlyReg = rt.getSingleDiscountEarlyReg();
 		float preRegDeposit = rt.getSinglePreRegDeposit();
 		float discountFullPayment = rt.getSingleDiscountFullPayment();
 
 		java.text.SimpleDateFormat redoDate = new java.text.SimpleDateFormat(
 				"MM-dd-yyyy"); //Strips off the minutes for date compare
 		today = redoDate.parse(redoDate.format(today));
 
 		if (discountEarlyRegDate != null && (!reg.getPreRegistered())
 				&& discountEarlyReg > 0.0 && preRegDeposit > credits
 				&& discountEarlyRegDate.compareTo(today) >= 0
 				&& !checkForPreRegDiscount(registrationID)) {
 			results.put("preReg", new Boolean(true));
 			results.put("preReg_Date", discountEarlyRegDate);
 			results.put("preReg_Deposit", new Float(preRegDeposit));
 			results.put("preReg_DiscountAmount", new Float(discountEarlyReg));
 		} else
 			results.put("preReg", new Boolean(false));
 
 		//Even though there is no discount, their might still be a deposit required.
 		if (preRegDeposit > credits)
 			results.put("preReg_Deposit", new Float(preRegDeposit));
 
 		if (discountFullPayment > 0.0 && balanceDue > 0.0
 				&& !checkForFullPayDiscount(registrationID)) {
 			results.put("fullPay", new Boolean(true));
 			results.put("fullPay_Date", rt.getPreRegEnd());
 			results.put("fullPay_DiscountAmount", new Float(discountFullPayment));
 		} else
 			results.put("fullPay", new Boolean(false));
 
 		return results;
 	}
 
 	public Hashtable getDiscountsAvailableWithSpouse(String registrationID,
 			String spouseRegistrationID, Date today) throws Exception {
 		Hashtable results = new Hashtable();
 		Registration reg = getRegistration(registrationID);
 		Registration sreg = getRegistration(spouseRegistrationID);
 		float credits = getRegistrationCredits(registrationID,
 				spouseRegistrationID);
 		float balanceDue = getRegistrationDebits(registrationID,
 				spouseRegistrationID)
 				- credits;
 
 		RegistrationType rt = reg.getRegistrationType();
 		Date discountEarlyRegDate = rt.getMarriedDiscountEarlyRegDate();
 		RegistrationType srt = sreg.getRegistrationType();
 		Date discountEarlyRegDateSpouse = srt.getMarriedDiscountEarlyRegDate();
 
 		//average of the 2
 		float preRegDeposit = (rt.getMarriedPreRegDeposit() + srt.getMarriedPreRegDeposit()) / (float)2;
 		float discountEarlyReg = (rt.getMarriedDiscountEarlyReg() + srt.getMarriedDiscountEarlyReg()) /(float) 2;
 		float discountFullPayment = (rt.getMarriedDiscountFullPayment() + srt.getMarriedDiscountFullPayment()) /(float) 2;
 
 		java.text.SimpleDateFormat redoDate = new java.text.SimpleDateFormat(
 				"MM-dd-yyyy"); //Strips off the minutes for date compare
 		today = redoDate.parse(redoDate.format(today));
 
 		if ((discountEarlyRegDate != null || discountEarlyRegDateSpouse != null)
 		/* only need to have one discount  */
 				&& (!reg.getPreRegistered() && !sreg.getPreRegistered())
 				&& discountEarlyReg > 0.0
 				&& preRegDeposit > credits
 				&& discountEarlyRegDate.compareTo(today) >= 0 /*
 															   * both dates must
 															   * be met
 															   */
 				&& discountEarlyRegDateSpouse.compareTo(today) >= 0
 				&& !checkForPreRegDiscount(registrationID, spouseRegistrationID)) {
 			results.put("preReg", new Boolean(true));
 			results.put(
 					"preReg_Date",
 					(discountEarlyRegDate.before(discountEarlyRegDateSpouse)) ? discountEarlyRegDate
 							: discountEarlyRegDateSpouse);
 			results.put("preReg_Deposit", new Float(preRegDeposit));
 			results.put("preReg_DiscountAmount", new Float(discountEarlyReg));
 		} else
 			results.put("preReg", new Boolean(false));
 
 		//Even though there is no discount, their might still be a deposit
 		// required.
 		if (preRegDeposit > credits)
 			results.put("preReg_Deposit", new Float(preRegDeposit));
 
 		if (discountFullPayment > 0.0
 				&& balanceDue > 0.0
 				&& !checkForFullPayDiscount(registrationID,
 						spouseRegistrationID)) {
 			results.put("fullPay", new Boolean(true));
 			results.put(
 					"fullPay_Date",
 					(rt.getPreRegEnd().before(srt.getPreRegEnd())) ? rt.getPreRegEnd()
 							: srt.getPreRegEnd());
 
 			results.put("fullPay_DiscountAmount",
 					new Float(discountFullPayment));
 		} else
 			results.put("fullPay", new Boolean(false));
 
 		return results;
 	}
 
 	public Hashtable getAccountSummary(String registrationID) throws Exception {
 
 		Hashtable results = new Hashtable();
 		results.put("Credits",
 				new Float(getRegistrationCredits(registrationID)));
 		results.put("Debits", new Float(getRegistrationDebits(registrationID)));
 		results.put("BalanceDue", new Float(
 				getRegistrationDebits(registrationID)
 						- getRegistrationCredits(registrationID)));
 
 		log.debug("Credits: " + results.get("Credits"));
 		log.debug("Debits: " + results.get("Debits"));
 		log.debug("BalanceDue: " + results.get("BalanceDue"));
 
 		return results;
 	}
 
 	/*
 	 * //Created: 7/16/02, DMB // Hashtable paymentInfo
 	 * (PaymentDate,Credit,Debit,Type,AuthCode,AccountNo,Comment,Posted,PostedDate) //
 	 * Only called by CRSAdmin public void savePayment(String registrationID,
 	 * Hashtable paymentInfo) throws Exception { broker.begin(); CRSRegistration
 	 * registration =
 	 * broker.getRegistrationObject(Integer.parseInt(registrationID));
 	 * CRSPayment payment = new CRSPayment(); Collection payments =
 	 * registration.getCRSPayment(); Iterator i = payments.iterator(); boolean
 	 * exists = false; if (!(paymentInfo.get("PaymentID")==null ||
 	 * paymentInfo.get("PaymentID").equals("new"))) { while(i.hasNext()) {
 	 * CRSPayment testPayment = (CRSPayment)i.next();
 	 * if(testPayment.getPaymentID() ==
 	 * Integer.parseInt((String)paymentInfo.get("PaymentID"))) { payment =
 	 * testPayment; if (paymentInfo.get("Credit")!=null) {
 	 * payment.setCredit(Float.parseFloat((String)paymentInfo.get("Credit"))); }
 	 * if (paymentInfo.get("Debit")!=null) {
 	 * payment.setDebit(Float.parseFloat((String)paymentInfo.get("Debit"))); }
 	 * payment.setType((String)paymentInfo.get("Type"));
 	 * payment.setAccountNo((String)paymentInfo.get("AccountNo"));
 	 * payment.setBusinessUnit((String)paymentInfo.get("BusinessUnit"));
 	 * payment.setDept((String)paymentInfo.get("Dept"));
 	 * payment.setRegion((String)paymentInfo.get("Region"));
 	 * payment.setProject((String)paymentInfo.get("Project"));
 	 * payment.setComment((String)paymentInfo.get("Comment"));
 	 * payment.setCRSRegistration(registration); exists = true; } } } if
 	 * (!exists) { ObjectAdaptor.hash2obj(paymentInfo, payment);
 	 * payment.setPaymentDate(new Date()); //easiest way to set the payment
 	 * date, cuz hash2obj doesn't save it in the right format
 	 * registration.assocPayment(payment); } broker.commit();
 	 * addPreRegDiscount(registrationID, paymentInfo.get("preRegDisc"));
 	 * addFullPayDiscount(registrationID); updatePreRegistered(registrationID); }
 	 * //Created: 7/16/02, DMB // Same as above, but only for adding new charges
 	 * and reversing charges. // Only called by CRSRegister public void
 	 * addCharge(String registrationID, Hashtable paymentInfo) throws Exception {
 	 * broker.begin(); CRSPayment payment = new CRSPayment(); CRSRegistration
 	 * registration =
 	 * broker.getRegistrationObject(Integer.parseInt(registrationID));
 	 * ObjectAdaptor.hash2obj(paymentInfo, payment); payment.setPaymentDate(new
 	 * Date()); //easiest way to set the payment date, cuz hash2obj doesn't save
 	 * it in the right format registration.assocPayment(payment);
 	 * broker.commit(); } //Created: 7/17/02, DMB public Collection
 	 * getPayments(String registrationID) throws Exception { broker.begin();
 	 * CRSRegistration reg =
 	 * broker.getRegistrationObject(Integer.parseInt(registrationID));
 	 * Collection payments = ObjectAdaptor.list(reg.getCRSPayment());
 	 * broker.rollback(); sortCollectionInteger((List)payments,"PaymentID");
 	 * return listToWeb(payments); } //Created: 7/17/02, DMB // returns:
 	 * Hashtable (Credits,Debits,BalanceDue) //Created: 7/18/02, DMB // Returns
 	 * the discounts that are still available, if they paid on the date 'today'
 	 * public Hashtable getDiscountsAvailable(String registrationID, Date today)
 	 * throws Exception { Hashtable results = new Hashtable(); Hashtable
 	 * acctSummary = getAccountSummary(registrationID); broker.begin();
 	 * CRSRegistration reg =
 	 * broker.getRegistrationObject(Integer.parseInt(registrationID));
 	 * Conference conf = reg.getConference(); SimpleDateFormat redoDate = new
 	 * SimpleDateFormat("yyyy-MM-dd"); today =
 	 * redoDate.parse(redoDate.format(today)); if((!reg.getPreRegistered()) &&
 	 * conf.getDiscountEarlyReg() > 0.0 && conf.getPreRegDeposit() >
 	 * ((Float)acctSummary.get("Credits")).floatValue() &&
 	 * conf.getDiscountEarlyRegDate().compareTo(today) >=0) {
 	 * results.put("PreReg",new Boolean(true));
 	 * results.put("PreReg_Date",conf.getDiscountEarlyRegDate());
 	 * results.put("PreReg_Deposit",new Float(conf.getPreRegDeposit()));
 	 * results.put("PreReg_DiscountAmount",new
 	 * Float(conf.getDiscountEarlyReg())); } else results.put("PreReg",new
 	 * Boolean(false)); //Even though there is no discount, their might still be
 	 * a deposit required. if(conf.getPreRegDeposit() >
 	 * ((Float)acctSummary.get("Credits")).floatValue())
 	 * results.put("PreReg_Deposit",new Float(conf.getPreRegDeposit()));
 	 * if(conf.getDiscountFullPayment() > 0.0 &&
 	 * ((Float)acctSummary.get("BalanceDue")).floatValue() > 0.0) {
 	 * results.put("FullPay",new Boolean(true));
 	 * results.put("FullPay_Date",conf.getPreRegEnd());
 	 * results.put("FullPay_DiscountAmount",new
 	 * Float(conf.getDiscountFullPayment())); } else results.put("FullPay",new
 	 * Boolean(false)); broker.rollback(); return hashToWeb(results); }
 	 */
 	//------------------------End of Payment Code ------------------
 	public Collection listReports() {
 		Report r = new Report();
 		return r.selectList();
 	}
 
 	public Collection listTypeReports() {
 		Report r = new Report();
 		return r.selectList("reportGroup = 4");
 	}
 
 	public Collection listGeneralReports() {
 		Report r = new Report();
 		return r.selectList("reportGroup = 0");
 	}
 
 	public boolean saveReport(Hashtable values) {
 		try {
 			if (values.containsKey("ReportID")) {
 				Report ci = getReport((String) values.get("ReportID"));
 				values.remove("ReportID");
 
 				ci.setMappedValues(values);
 
 				return ci.persist();
 			} else
 				return false;
 		} catch (Exception e) {
 			log.error(e, e);
 			return false;
 		}
 	}
 
 	public boolean deleteReport(String reportID) {
 		try {
 			Report ci = getReport(reportID);
 			return ci.delete();
 		} catch (Exception e) {
 			return false;
 		}
 	}
 
 	// Single Objects
 	public Conference getConference(int ID) {
 		try {
 			Conference c = new Conference();
 			c.setConferenceID(ID);
 			c.select();
 			return c;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public Conference getConference(String conferenceIDString)
 			throws DBIOEntityException {
		return (conferenceIDString != null && !conferenceIDString.equals("")) ? getConference(Integer.parseInt(conferenceIDString.trim()))
 				: null;
 	}
 
 	public Merchandise getMerchandise(int ID) {
 		try {
 			Merchandise m = new Merchandise();
 			if (ID > 0) {
 				m.setMerchandiseID(ID);
 				m.select();
 			}
 			return m;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public Merchandise getMerchandise(String ID) {
 		return getMerchandise(Integer.parseInt(ID));
 	}
 
 	public Answer getAnswer(int ID) {
 		try {
 			Answer a = new Answer();
 			a.setAnswerID(ID);
 			a.select();
 			return a;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public Answer getAnswer(String ID) {
 		return getAnswer(Integer.parseInt(ID));
 	}
 
 	public Question getQuestion(int ID) {
 		try {
 			Question m = new Question();
 			m.setQuestionID(ID);
 			m.select();
 			return m;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public Question getQuestion(String ID) {
 		return getQuestion(Integer.parseInt(ID));
 	}
 
 	public QuestionText getQuestionText(int ID) {
 		try {
 			QuestionText m = new QuestionText();
 			m.setQuestionTextID(ID);
 			m.select();
 			return m;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public QuestionText getQuestionText(String ID) {
 		return getQuestionText(Integer.parseInt(ID));
 	}
 
 	public ChildRegistration getChildRegistration(int ID) {
 		try {
 			ChildRegistration r = new ChildRegistration();
 			r.setChildRegistrationID(ID);
 			r.select();
 			return r;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public ChildRegistration getChildRegistration(
 			String childRegistrationIDString) {
 		return getChildRegistration(Integer.parseInt(childRegistrationIDString));
 	}
 
 	public Registration getRegistration(int ID) {
 		Registration r = new Registration();
 		r.setRegistrationID(ID);
 		r.select();
 		return r;
 	}
 
 	public Registration getRegistration(String registrationIDString) {
 		return getRegistration(Integer.parseInt(registrationIDString));
 	}
 
 	public Payment getPayment(int ID) {
 		try {
 			Payment r = new Payment();
 			r.setPaymentID(ID);
 			r.select();
 			return r;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public Payment getPayment(String IDString) {
 		return getPayment(Integer.parseInt(IDString));
 	}
 
 	public CustomItem getCustomItem(int ID) {
 		try {
 			CustomItem ci = new CustomItem();
 			ci.setCustomItemID(ID);
 			ci.select();
 			return ci;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public CustomItem getCustomItem(String ID) {
 		return getCustomItem(Integer.parseInt(ID));
 	}
 
 	public Report getReport(int ID) {
 		try {
 			Report r = new Report();
 			r.setReportID(ID);
 			r.select();
 			return r;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public Report getReport(String ID) {
 		return getReport(Integer.parseInt(ID));
 	}
 
 	public Person getPerson(int ID) {
 		try {
 			Person p = new Person();
 			p.setPersonID(ID);
 			p.select();
 			return p;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public Person getPerson(String ID) {
 		return getPerson(Integer.parseInt(ID));
 	}
 
 	private int countIt(com.kenburcham.framework.dbio.DBIOEntity o, String qry) {
 		try {
 			com.kenburcham.framework.dbio.DBIOTransaction tx = o.getTransaction();
 			tx.setSQL(qry);
 			tx.getRecords();
 				java.sql.ResultSet mine = tx.getResultSet();
 				if (mine.next()) {
 					return mine.getInt(1);
 				} else {
 					return 0;
 				}
 		} catch (SQLException e) {
 			log.error(e, e);
 			return 0;
 		} finally {
 			o.close();
 		}
 	}
 
 	public String reportXML(String reportID, String conferenceID,
 			int orderField, String orderDirection, int offset, int size)
 			throws Exception {
 		Report r = new Report();
 		r.setReportID(Integer.parseInt(reportID));
 		r.select();
 		return getXML(r, r.getQuery() + conferenceID,
 				r.getSorts().split("\n")[orderField], orderDirection, offset,
 				size);
 	}
 
 	public String reportXML(String reportID, String conferenceID,
 			String registrationTypeID, int orderField, String orderDirection,
 			int offset, int size) throws Exception {
 		Report r = new Report();
 		r.setReportID(Integer.parseInt(reportID));
 		r.select();
 		String qry = r.getQuery() + conferenceID
 				+ " AND (registration.fk_RegistrationTypeID = '"
 				+ registrationTypeID + "')";
 		return getXML(r, qry, r.getSorts().split("\n")[orderField],
 				orderDirection, offset, size);
 	}
 
 	private String getXML(com.kenburcham.framework.dbio.DBIOEntity o,
 			String qry, String orderField, String orderDirection, int offset,
 			int pageSize) {
 		boolean DESC = orderDirection.equals("DESC");
 		StringBuffer xmlString = new StringBuffer();
 		StringBuffer headerString = new StringBuffer();
 		StringBuffer returnString = new StringBuffer();
 
 		try {
 			com.kenburcham.framework.dbio.DBIOTransaction tx = o.getTransaction();
 			tx.setSQL(qry + " ORDER BY "
 					+ fixOrderBy(orderField, (DESC ? "DESC" : "ASC")));
 			if (tx.getRecords(java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE,
 					java.sql.ResultSet.CONCUR_UPDATABLE)) {
 				java.sql.ResultSet rset = tx.getResultSet();
 				java.sql.ResultSetMetaData rstMeta = rset.getMetaData();
 
 				int returnSize = pageSize == 0 ? rset.getFetchSize() : pageSize;
 
 				if (offset > 1)
 					rset.absolute(offset - 1);
 				int j = 0;
 				while (rset.next() && j < returnSize) {
 					j++;
 					xmlString.append("<ROW>\n");
 					for (int i = 1; i <= rstMeta.getColumnCount(); i++) {
 						String val = rset.getString(i);
 						xmlString.append("<" + rstMeta.getColumnName(i) + ">");
 						if (val != null) {
 							if (val.equals("null")) {
 								xmlString.append("");
 							} else {
 								xmlString.append(org.alt60m.util.Escape.textToXML(val));
 							}
 						}
 						xmlString.append("</" + rstMeta.getColumnName(i) + ">");
 					}
 					xmlString.append("</ROW> \n");
 				}
 
 				returnString.append("<?xml version=\"1.0\"?>\n");
 				returnString.append("<!DOCTYPE RESULTSET [\n");
 				returnString.append("<!ENTITY eacute \"&#233;\">\n");
 				returnString.append("<!ENTITY iacute \"&#237;\">\n");
 				returnString.append("<!ENTITY oacute \"&#243;\">\n");
 				returnString.append("]>\n");
 				returnString.append("<RESULTSET>\n");
 
 				returnString.append(headerString.toString());
 				returnString.append(xmlString.toString());
 				returnString.append("</RESULTSET>\n");
 			}
 			return returnString.toString();
 		} catch (Exception e) {
 			log.error(e, e);
 			return "";
 		}
 	}
 
 /*	public Registration getRegistrationByUsername(String username,
 			String conferenceID) {
 		Person p = new Person();
 		Registration r = new Registration();
 		p.setUsername(username);
 		p.select();
 
 		r.setConferenceID(Integer.parseInt(conferenceID.trim()));
 		r.setPersonID(p.getPersonID());
 		r.select();
 		return r;
 	}
 */
 	public Registration getRegistrationBySsmID(String ssmID, String conferenceID) {
 		Person p = getPersonBySsmID(ssmID);
 		Registration r = new Registration();
 		r.setConferenceID(Integer.parseInt(conferenceID.trim()));
 		r.setPersonID(p.getPersonID());
 		r.select();
 		return r;
 	}
 
 	public Registration getRegistrationByPersonID(String id, String conferenceID) {
 		Person p = getPerson(id);
 		Registration r = new Registration();
 
 		r.setConferenceID(Integer.parseInt(conferenceID));
 		r.setPersonID(p.getPersonID());
 		r.select();
 
 		return r;
 	}
 
 	public User getSsmByUsername(String username) {
 		User p = new User();
 		p.setUsername(username);
 		p.select();
 
 		return p;
 	}
 
 	public int getSsmID(String username) {
 		User u = new User();
 		u.setUsername(username);
 		u.select();
 		return u.getUserID();
 	}
 	
 	public Person getPersonBySsmID(String ssmID) {
 		Person p = new Person();
 		p.setFk_ssmUserID(Integer.parseInt(ssmID));
 		p.select();
 		return p;
 	}
 	
 	public Person getPersonBySsmID(int ssmID) {
 		Person p = new Person();
 		p.setFk_ssmUserID(ssmID);
 		p.select();
 		return p;
 	}
 
 	public int createRegistration(String type, Person p, Conference c) {
 		/* TODO: DEPRECATED "type" is being phased out */
 		int retval = 0;
 		if (!p.isPKEmpty() && !c.isPKEmpty()) {
 			Registration r = new Registration();
 			r.setRegistrationTypeOld(type);
 			r.setPerson(p);
 			r.setRegistrationDate(new Date());
 			r.setConference(c);
 			r.insert();
 			retval = r.getRegistrationID();
 		}
 		return retval;
 	}
 
 	public int createRegistration(int registrationTypeID, Person p, Conference c) {
 		int retval = 0;
 		if (!p.isPKEmpty() && !c.isPKEmpty()) {
 			Registration r = new Registration();
 			r.setRegistrationType(getRegistrationType(registrationTypeID));
 
 			r.setPerson(p);
 
 			r.setRegistrationDate(new Date());
 			r.setConference(c);
 			r.insert();
 			retval = r.getRegistrationID();
 		}
 		return retval;
 	}
 
 	public boolean savePerson(Hashtable values) {
 		try {
 			if (values.containsKey("PersonID")) {
 				Person p = getPerson((String) values.get("PersonID"));
 				values.remove("PersonID");
 
 				p.setMappedValues(values);
 
 				return p.persist();
 			} else
 				return false;
 		} catch (Exception e) {
 			log.error(e, e);
 			return false;
 		}
 	}
 
 	public boolean saveChildRegistration(Hashtable values) {
 		try {
 			//if(values.containsKey("ChildRegistrationID") &&
 			// values.containsKey("RegistrationID")){
 			if (values.containsKey("ChildRegistrationID")) {
 				ChildRegistration p = "0".equals(values.get("ChildRegistrationID")) ? new ChildRegistration()
 						: getChildRegistration((String) values.get("ChildRegistrationID"));
 				values.remove("ChildRegistrationID");
 
 				p.setMappedValues(values);
 
 				return p.persist();
 			} else
 				return false;
 		} catch (Exception e) {
 			log.error(e, e);
 			return false;
 		}
 	}
 
 	public boolean deleteChildRegistration(String ID) {
 		try {
 			ChildRegistration cr = getChildRegistration(ID);
 			return cr.delete();
 		} catch (Exception e) {
 			return false;
 		}
 	}
 
 	public boolean savePayment(Hashtable values) {
 		try {
 			if (values.containsKey("PaymentID")
 					&& values.containsKey("RegistrationID")) {
 				Payment p = "0".equals(values.get("PaymentID")) ? new Payment()
 						: getPayment((String) values.get("PaymentID"));
 				values.remove("PaymentID");
 
 				p.setMappedValues(values);
 
 				boolean retVal = p.persist();
 				if ((values.get("preRegDisc") != null)
 						&& (values.get("preRegDisc").equals("yes")))
 					forceAddPreRegDiscount(String.valueOf(p.getRegistrationID()));
 				else
 					addPreRegDiscount(String.valueOf(p.getRegistrationID()));
 				addFullPayDiscount(String.valueOf(p.getRegistrationID()));
 				updatePreRegistered(String.valueOf(p.getRegistrationID()));
 
 				return retVal;
 
 			} else
 				return false;
 		} catch (Exception e) {
 			log.error(e, e);
 			return false;
 		}
 	}
 
 	public boolean deletePayment(String paymentID) {
 		try {
 			Payment p = getPayment(paymentID);
 			return p.delete();
 		} catch (Exception e) {
 			return false;
 		}
 	}
 
 	public Collection searchPersons(String firstName, String lastName,
 			String orderField, String orderDirection, int offset, int size)
 			throws DBIOEntityException {
 		boolean DESC = orderDirection.equals("DESC");
 		Person p = new Person();
 		Vector retval = new Vector();
 		Vector ps = p.selectList("firstName LIKE '" + DBHelper.escape(firstName)
 				+ "%' AND lastName LIKE '" + DBHelper.escape(lastName) + "%' ORDER BY "
 				+ fixOrderBy(orderField, (DESC ? "DESC" : "ASC")));
 		retval.add(new Integer(ps.size()));
 		retval.addAll(fixList(ps, offset, size));
 		return retval;
 	}
 
 	public static String escape(String string) {
 		return DBHelper.escape(string);
 	}
 
 	public Collection searchSpouses(String firstName, String lastName,
 			String personID, String orderField, String orderDirection,
 			int offset, int size) throws DBIOEntityException {
 		boolean DESC = orderDirection.equals("DESC");
 		Person p = new Person();
 		Vector retval = new Vector();
 		Vector ps = p.selectList("(fk_spouseID IS NULL OR fk_spouseID < 1) AND firstName LIKE '"
 				+ DBHelper.escape(firstName)
 				+ "%' AND lastName LIKE '"
 				+ DBHelper.escape(lastName)
 				+ "%' AND personID <> "
 				+ personID
 				+ " ORDER BY "
 				+ fixOrderBy(orderField, (DESC ? "DESC" : "ASC")));
 		retval.add(new Integer(ps.size()));
 		retval.addAll(fixList(ps, offset, size));
 		return retval;
 	}
 
 	public Collection searchSSM(String email, int offset, int size)
 			throws DBIOEntityException {
 		User u = new User();
 		u.changeTargetTable("crs_NotUserYet");
 		Vector retval = new Vector();
 		Vector us = u.selectList("username LIKE '" + email + "%'");
 		retval.add(new Integer(us.size()));
 		retval.addAll(fixList(us, offset, size));
 		return retval;
 	}
 
 	private String fixOrderBy(String orderBy, String direction) {
 		return orderBy.replaceFirst("([\\w|\\.]*)", "$1 " + direction);
 	}
 
 	public void sendScholarshipEmail(String registrationID,
 			String conferenceID, String name, String address, String amount) {
 		try {
 			Registration r = getRegistration(registrationID);
 			Conference c = getConference(conferenceID);
 			Person p = r.getPerson();
 			SendMessage email = new SendMessage();
 			org.alt60m.html.SelectRegion sr = new org.alt60m.html.SelectRegion();
 			sr.setCurrentValue(c.getRegion());
 
 			email.setFrom(c.getContactEmail());
 			email.setTo(address);
 			email.setSubject("Please verify a scholarship for "
 					+ p.getFirstName() + " " + p.getLastName());
 			String heshe = "0".equals(p.getGender()) ? "she" : "he";
 			String HeShe = "0".equals(p.getGender()) ? "She" : "He";
 			String hisher = "0".equals(p.getGender()) ? "her" : "his";
 			String msg = "";
 			msg += "Dear " + name + ",\n";
 			msg += "\n";
 			msg += p.getFirstName()
 					+ " "
 					+ p.getLastName()
 					+ " has indicated that "
 					+ heshe
 					+ " is eligible for a scholarship in the amount of $"
 					+ amount
 					+ " for the "
 					+ c.getName()
 					+ ". "
 					+ HeShe
 					+ " selected you as the staff that will verify "
 					+ hisher
 					+ " eligibility for this scholarship. Please log on to https://staff.campuscrusadeforchrist.com/servlet/StaffController to verify this scholarship. If you don't know how to verify a scholarship, please go to the following link for instructions:\n";
 			msg += "\n";
 			msg += "https://staff.campuscrusadeforchrist.com/cms/content/3508.pdf\n";
 			msg += "\n";
 			msg += "Thank you very much for helping make it possible for "
 					+ p.getFirstName() + " " + p.getLastName()
 					+ " to attend the " + c.getName() + ".\n";
 			msg += "\n";
 			msg += "Yours Sincerely,\n";
 			msg += "\n";
 			msg += c.getContactName() + " \n";
 
 			email.setBody(msg);
 			email.send();
 			//			log.debug(msg);
 		} catch (Exception e) {
 		}
 	}
 
 	public void sendSpouseEmail(String registrationID, String spouseEmail) {
 		try {
 			Registration r = getRegistration(registrationID);
 			Conference c = r.getConference();
 			
 			SimpleSecurityManager ssm = new SimpleSecurityManager();
 			User ssmUser = ssm.getUserObjectByUsername(spouseEmail);
 			
 			SendMessage email = new SendMessage();
 
 			email.setFrom(c.getContactEmail());
 			email.setTo(spouseEmail);
 			email.setSubject("You have been added as a spouse");
 			String msg = "";
 			msg += "Dear " + spouseEmail + ",\n";
 			msg += "\n";
 			msg += "This email is to notify you that your spouse, " + r.getFirstName() + " " + r.getLastName() + ", " +
 					"has created a login on your behalf for the Campus Crusade for Christ Conference Registration System.";
 			msg += "\n\n";
 			msg += "Please retain the following information for your records, as you will need it in the future " +
 					"to register for Campus Crusade for Christ conferences or apply for " +
 					"Summer Project/STINT/Internships.";
 			msg += "\n\n";
 			msg += "Your username is: " + spouseEmail;
 			msg += "\n\n";
 			msg += "Your password is the same as your spouse's. Please ask your spouse for this information " +
 					"and remember it for future reference.";
 			msg += "\n\n";
 			msg += "Your secret question (for password retrieval in the case of forgotten/unknown password)" +
 					" is: " + ssmUser.getPasswordQuestion();
 			msg += "\n\n";
 			msg += "Your secret answer is: " + ssmUser.getPasswordAnswer();
 			msg += "\n\n";
 			msg += "If you would like to have your password reset, please contact " +
 					"help@campuscrusadeforchrist.com, or call 1-888-222-5462.";
 			msg += "\n\n";
 			msg += "Yours Sincerely,";
 			msg += "\n";
 			msg += "The " + c.getName() + " Team";
 			email.setBody(msg);
 			email.send();
 			//			log.debug(msg);
 		} catch (Exception e) {
 		}
 	}
 
 	public void sendAccountTransferEmail(String registrationID,
 			String conferenceID, String acctNum, String address, String amount) {
 		try {
 			Registration r = getRegistration(registrationID);
 			Conference c = getConference(conferenceID);
 			Person p = r.getPerson();
 			SendMessage email = new SendMessage();
 			org.alt60m.html.SelectRegion sr = new org.alt60m.html.SelectRegion();
 			sr.setCurrentValue(c.getRegion());
 
 			String toEmail = getStaffAccountContactEmail(acctNum);
 			boolean toConfAdmin = false;
 			if (toEmail == null) {
 				toConfAdmin = true;
 				toEmail = c.getContactEmail();
 			}
 			email.setTo(toEmail);
 			
 			String heshe = "0".equals(p.getGender()) ? "she" : "he";
 			String HeShe = "0".equals(p.getGender()) ? "She" : "He";
 			String hisher = "0".equals(p.getGender()) ? "her" : "his";
 			
 			String msg = "";
 			
 			if(toConfAdmin)
 			{
 				email.setFrom("help@campuscrusadeforchrist.com");
 				email.setSubject("Unverified Account Transfer for "
 						+ p.getFirstName() + " " + p.getLastName());
 				msg += "Dear " + c.getContactName()+ ",\n";
 				msg += "\n";
 				msg += "This email is to notify you that " + p.getFirstName() + " "
 						+ p.getLastName() + " has attempted to submit a payment for "+ c.getName() +" via staff account transfer, but the system was unable to verify the validity of the transaction.  The staff account number indicated was "+ acctNum +". ";
 				msg += "You are receiving this email because you are listed as the contact person for "+ c.getName()+".";
 				msg += "\n\n";
 				msg += "Normally, the system would send an email to the address associated with that account number, notifying the recipient that their staff account would be charged. However, in this case the system doesn't have an email address for that account number.  This could be either because it is an invalid account number, or because Campus Crusade's personnel records don't have an email address on file for that account number.";
 				msg += "\n\n";
 				msg += "To ensure that you have a good account to charge for this transfer, please verify that the account number indicated is valid and notify its owner (keeping in mind that this might NOT be "
 						+ p.getFirstName() + " " + p.getLastName()+") that it is going to be charged for " + p.getFirstName() + " " + p.getLastName()+ "'s registration.";
 				msg += "\n\n";
 				msg += "If you need some help figuring out who the owner of this account is, or have any other questions, please feel free to contact us at help@campuscrusadeforchrist.com.\n\n";
 				msg += "Sincerely,\n";
 				msg += "\n";
 				msg += "The Campus Ministry Information & Technology Team\n";
 
 				
 			}
 			else
 			{
 				email.setFrom(c.getContactEmail());
 				email.setSubject("Please verify an Account Transfer for "
 						+ p.getFirstName() + " " + p.getLastName());
 				msg += "Dear " + getStaffAccountName(acctNum)+ ",\n";
 				msg += "\n";
 				msg += "This email is to notify you that " + p.getFirstName() + " "
 						+ p.getLastName() + " has requested that we charge"
 						+ " staff account " + acctNum + " for the " + c.getName()
 						+ ". The amount of the charge is $" + amount
 						+ ".";
 				msg += "\n\n";
 				msg += "If "
 						+ acctNum
 						+ " is not your staff account number or you feel this email to be in error, please reply to this "
 						+ "email or call us at " + c.getContactPhone() + ".";
 				msg += "\n\n";
 				msg += "Sincerely,\n";
 				msg += "\n";
 				msg += "The " + c.getName() + " Team\n";
 
 			}
 			
 			email.setBody(msg);
 			email.send();
 			//			log.debug(msg);
 		} catch (Exception e) {
 		}
 	}
 
 	/**
 	 * @param contactEmail
 	 * @return
 	 */
 	private String getStaffAccountContactEmail(String acctNum) {
 		String email = null;
 		Staff acctNumStaff = new Staff(acctNum);
 		String acctNumEmail = acctNumStaff.getEmail();
 		if (acctNumEmail != null && !acctNumEmail.equals("")) {
 			email = acctNumEmail;
 		}
 		return email;
 	}
 
 	private String getStaffAccountContactName(String acctNum, String contactName) {
 		String name = contactName;
 		String acctNumName = getStaffAccountName(acctNum);
 		
 		if (acctNumName != null && !acctNumName.equals("")) {
 			name = acctNumName;
 		}
 		return name;
 	}
 	
 	private String getStaffAccountName(String acctNum) {
 		Staff acctNumStaff = new Staff(acctNum);
 		if (acctNumStaff.getPreferredName() == null || acctNumStaff.getLastName() == null)
 			return null;
 		String acctNumName = acctNumStaff.getPreferredName() + " "
 				+ acctNumStaff.getLastName();
 		return acctNumName;
 	}
 	
 
 	public void sendMinistryAccountTransferEmail(String registrationID,
 			String conferenceID, Hashtable paymentInfo) {
 		//String staffName, String staffEmail, String ministry, String amount)
 		// {
 		try {
 			Registration r = getRegistration(registrationID);
 			Conference c = getConference(conferenceID);
 			Person p = r.getPerson();
 			SendMessage email = new SendMessage();
 			org.alt60m.html.SelectRegion sr = new org.alt60m.html.SelectRegion();
 			sr.setCurrentValue(c.getRegion());
 
 			email.setFrom(r.getEmail());
 			email.setTo((String) paymentInfo.get("staffEmail"));
 			email.setSubject("Please verify an Account Transfer for "
 					+ p.getFirstName() + " " + p.getLastName());
 			String heshe = "0".equals(p.getGender()) ? "she" : "he";
 			String HeShe = "0".equals(p.getGender()) ? "She" : "He";
 			String hisher = "0".equals(p.getGender()) ? "her" : "his";
 			String msg = "";
 			msg += "Dear " + (String) paymentInfo.get("staffName") + ",\n";
 			msg += "\n";
 			msg += "This email is to notify you that "
 					+ p.getFirstName()
 					+ " "
 					+ p.getLastName()
 					+ " has requested that we charge the "
 					+ (String) paymentInfo.get("Ministry")
 					+ " ministry account for "
 					+ hisher
 					+ " "
 					+ c.getName()
 					+ " registration, and has indicated that you authorized this charge.";
 			msg += "\n\n";
 			msg += "Amount of charge: $" + (String) paymentInfo.get("PaymentAmt")
 					+ "\nChartfield indicated:\n  Business Unit: "
 					+ (String) paymentInfo.get("BU") + "\n  Operating Unit: "
 					+ (String) paymentInfo.get("OU") + "\n  Department: "
 					+ (String) paymentInfo.get("Dept") + "\n  Project: "
 					+ (String) paymentInfo.get("Project");
 			msg += "\n\n";
 			msg += "If you feel this information to be in error, have any objections "
 					+ "to this pending transfer, or don't think you are the appropriate "
 					+ "person to authorize this transfer, please reply to this email or "
 					+ "call us at "
 					+ c.getContactPhone()
 					+ ". Otherwise, we will charge the chartfield that was indicated.\n";
 			msg += "Thank you very much for your attention to this matter.\n";
 			msg += "\n";
 			msg += "Yours Sincerely,\n";
 			msg += "\n";
 			msg += "The " + c.getName() + " Team\n";
 			email.setBody(msg);
 			email.send();
 			log.debug(msg);
 		} catch (Exception e) {
 		}
 	}
 
 	public int getFirstRegistrationID(String inID) {
 		return getFirstRegistrationID(getRegistration(inID));
 	}
 
 	public int getFirstRegistrationID(int inID) {
 		return getFirstRegistrationID(getRegistration(inID));
 	}
 
 	public int getFirstRegistrationID(Registration r) {
 		return r.isRegisteredFirst() ? r.getRegistrationID()
 				: r.getSpouseRegistrationID();
 	}
 
 	public int countChildren(String regID) {
 		return countChildren(Integer.parseInt(regID));
 	}
 
 	public int countChildren(int regID) {
 		ChildRegistration cr = new ChildRegistration();
 		cr.setRegistrationID(regID);
 		return cr.count();
 	}
 
 	/**
 	 * @param conferenceID
 	 * @param commonQuestions
 	 * @return
 	 */
 
 	public Vector listAddedQuestions(String conferenceID, String view,
 			Collection commonQuestions) {
 		Vector result = new Vector();
 		Iterator qtIter = commonQuestions.iterator();
 		while (qtIter.hasNext()) {
 			QuestionText currQT = (QuestionText) qtIter.next();
 			Question q = new Question();
 			q.setConferenceID(Integer.parseInt(conferenceID));
 			q.setQuestionTextID(currQT.getQuestionTextID()); //instead of teh
 															 // whole body text!
 			q.setRegistrationTypeID(view);
 			Vector questions = q.selectList();
 			String checked = "";
 
 			for (int i = 0; i < questions.size(); i++) {
 				Question currQ = (Question) questions.get(i);
 				if (currQ.getRequired()) {
 					checked = "R";
 				} else {
 					checked = "I";
 				}
 			}
 			result.add(checked);
 		}
 		return result;
 	}
 
 	/**
 	 * 
 	 */
 	public void checkSinglePaymentRemoval(Registration r) {
 		// Check for removing Onsite Conference Cost
 		Payment p = new Payment();
 		p.setRegistration(r);
 		p.setType("Onsite Conference Cost");
 		Vector payments = p.selectList("ORDER BY paymentDate");
 		if (payments.size() % 2 == 1) { // Odd number of payments, need to reimburse
 			Payment current = (Payment)payments.get(payments.size() - 1); // Get last payment
 			Payment negativeDebit = new Payment();
 			negativeDebit.setRegistration(r);
 			negativeDebit.setType("Onsite Conference Cost");
 			negativeDebit.setDebit(0 - current.getDebit());
 			negativeDebit.setPaymentDate(new Date());
 			negativeDebit.setPostedDate(new Date());
 			negativeDebit.setPosted(true);
 			negativeDebit.persist();
 		}
 		
 		// Check for removing Commuter Conference Cost
 		p = new Payment();
 		p.setRegistration(r);
 		p.setType("Commuter Conference Cost");
 		payments = p.selectList("ORDER BY paymentDate");
 		if (payments.size() % 2 == 1) { // Odd number of payments, need to reimburse
 			Payment current = (Payment)payments.get(payments.size() - 1); // Get last payment
 			Payment negativeDebit = new Payment();
 			negativeDebit.setRegistration(r);
 			negativeDebit.setType("Commuter Conference Cost");
 			negativeDebit.setDebit(0 - p.getDebit());
 			negativeDebit.setPaymentDate(new Date());
 			negativeDebit.setPostedDate(new Date());
 			negativeDebit.setPosted(true);
 			negativeDebit.persist();
 		}
 		
 		// Check for modifying Additional Expenses
 		Vector regMerchV = new Vector(listRegistrationMerchandise(
 				r.getRegistrationID(), "displayOrder", "DESC"));
 		Iterator regMerch = regMerchV.iterator();
 		Payment tempP;
 		
 		while (regMerch.hasNext()) {
 			Merchandise m = (Merchandise) regMerch.next();
 			tempP = new Payment();
 			tempP.setRegistration(r);
 			tempP.setType(m.getName());
 			if (tempP.select()) {
 				tempP.setType(m.getName() + " - " + r.getPerson().getFirstName());
 				tempP.persist();
 			}
 		}
 	}
 	
 	public void checkSingleDiscountRemoval(Registration r) {
 //		 Check for removing single full payment discount
 		Payment p = new Payment();
 		p.setRegistration(r);
 		p.setType("Full Payment Discount");
 		Vector payments = p.selectList("ORDER BY paymentDate");
 		if (payments.size() % 2 == 1) { // Odd number of payments, need to recharge
 			Payment current = (Payment)payments.get(payments.size() - 1); // Get last payment
 			Payment debit = new Payment();
 			debit.setRegistration(r);
 			debit.setType("Full Payment Discount");
 			debit.setDebit(0 - current.getDebit());
 			debit.setPaymentDate(new Date());
 			debit.setPostedDate(new Date());
 			debit.setPosted(true);
 			debit.persist();
 		}
 	
 	//	 Check for removing single early registration discount
 		p = new Payment();
 		p.setRegistration(r);
 		p.setType("Early Registration Discount");
 		payments = p.selectList("ORDER BY paymentDate");
 		if (payments.size() % 2 == 1) { // Odd number of payments, need to recharge
 			Payment current = (Payment)payments.get(payments.size() - 1); // Get last payment
 			Payment debit = new Payment();
 			debit.setRegistration(r);
 			debit.setType("Early Registration Discount");
 			debit.setDebit(0 - current.getDebit());
 			debit.setPaymentDate(new Date());
 			debit.setPostedDate(new Date());
 			debit.setPosted(true);
 			debit.persist();
 		}
 	}
 	
 	public void checkMarriedPaymentRemoval(Registration r, String spouseRegistrationID) throws DBIOEntityException, Exception{
 		String registrationID = String.valueOf(r.getRegistrationID());
 		Registration spouse = getRegistration(spouseRegistrationID);
 		Registration firstReg = r.isRegisteredFirst() ? r : spouse;
 		String paymentTypesComment = "";
 		String paymentHousingComment = "";
 
 		RegistrationType regType = r.getRegistrationType();
 		RegistrationType sregType = spouse.getRegistrationType();
 
 		int hotel = stayingInHotel(r.getRegistrationID());
 		int hotelSpouse = stayingInHotel(spouse.getRegistrationID());
 
 		/* add a string for married couples of different registration types */
 		if (hotel == hotelSpouse
 				&& regType.getRegistrationTypeID() == sregType.getRegistrationTypeID()) {
 			paymentTypesComment = " - Married";
 		} else {
 			paymentTypesComment = " - Married [average]";
 		}
 
 		// Check for removing Onsite Conference Cost
 		Payment p = new Payment();
 		p.setRegistration(r);
 		p.setType("Onsite Conference Cost" + paymentTypesComment);
 		Vector payments = p.selectList("ORDER BY paymentDate");
 		if (payments.size() % 2 == 1) { // Odd number of payments, need to reimburse
 			Payment current = (Payment)payments.get(payments.size() - 1); // Get last payment
 			Payment negativeDebit = new Payment();
 			negativeDebit.setRegistration(r);
 			negativeDebit.setType("Onsite Conference Cost" + paymentTypesComment);
 			negativeDebit.setDebit(0 - current.getDebit());
 			negativeDebit.setPaymentDate(new Date());
 			negativeDebit.setPostedDate(new Date());
 			negativeDebit.setPosted(true);
 			negativeDebit.persist();
 		}
 		
 		// Check for removing Commuter Conference Cost
 		p = new Payment();
 		p.setRegistration(r);
 		p.setType("Commuter Conference Cost" + paymentTypesComment);
 		payments = p.selectList("ORDER BY paymentDate");
 		if (payments.size() % 2 == 1) { // Odd number of payments, need to reimburse
 			Payment current = (Payment)payments.get(payments.size() - 1); // Get last payment
 			Payment negativeDebit = new Payment();
 			negativeDebit.setRegistration(r);
 			negativeDebit.setType("Commuter Conference Cost" + paymentTypesComment);
 			negativeDebit.setDebit(0 - current.getDebit());
 			negativeDebit.setPaymentDate(new Date());
 			negativeDebit.setPostedDate(new Date());
 			negativeDebit.setPosted(true);
 			negativeDebit.persist();
 		}
 		
 		// Check that all merchandise has been billed for
 		Collection mi = listRegistrationMerchandise(registrationID,	"displayOrder", "DESC");
 		Collection spouseMerch = listRegistrationMerchandise(spouseRegistrationID,	"displayOrder", "DESC");
 		Payment tempP = new Payment();
 
 		Iterator confMerch = listMerchandise(String.valueOf(r.getConferenceID()), r.getRegistrationTypeID(), "displayOrder", "DESC").iterator();
 		while (confMerch.hasNext()) {
 			Merchandise m = (Merchandise) confMerch.next();
 			tempP = new Payment();
 			tempP.setRegistration(firstReg);
 			tempP.setType(m.getName() + " - " + r.getFirstName());
 			Vector tempPayments = tempP.selectList("ORDER BY paymentDate");
 			
 			if( mi.contains(m) ) {
 				if (tempPayments.size() % 2 == 0) {
 					tempP.setPaymentDate(new Date());
 					tempP.setPostedDate(new Date());
 					tempP.setPosted(true);
 					tempP.setDebit(m.getAmount());
 					tempP.insert();
 				}
 			} else {
 				if (tempPayments.size() % 2 == 1) {
 					// Negative Debit
 					tempP = (Payment)tempPayments.get(tempPayments.size() - 1);
 					tempP.setPaymentID(0);
 					tempP.setPaymentDate(new Date());
 					tempP.setPostedDate(new Date());
 					tempP.setPosted(true);
 					tempP.setDebit(0 - tempP.getDebit());
 					tempP.insert();
 				}
 			}
 		}
 		// Check for modifying Additional Expenses
 		Vector regMerchV = new Vector(listRegistrationMerchandise(
 				r.getRegistrationID(), "displayOrder", "DESC"));
 		Iterator regMerch = regMerchV.iterator();
 		
 		while (regMerch.hasNext()) {
 			Merchandise m = (Merchandise) regMerch.next();
 			tempP = new Payment();
 			tempP.setRegistration(r);
 			tempP.setType(m.getName() + " - " + r.getPerson().getFirstName());
 			if (tempP.select()) {
 				tempP.setType(m.getName());
 				tempP.persist();
 			}
 		}
 		
 		
 		// DO WE NEED THIS?
 		// should never have spouse merchandise because registrant hasn't chosen a spouse yet
 		// you can't say your spouse isn't coming if you have already chosen your spouse
 		confMerch = listMerchandise(String.valueOf(spouse.getConferenceID()), spouse.getRegistrationTypeID(), "displayOrder", "DESC").iterator();
 		while (confMerch.hasNext()) {
 			Merchandise m = (Merchandise) confMerch.next();
 			tempP = new Payment();
 			tempP.setRegistration(firstReg);
 			tempP.setType(m.getName() + " - " + spouse.getFirstName());
 			Vector tempPayments = tempP.selectList("ORDER BY paymentDate");
 			
 			if( spouseMerch.contains(m) ) {
 				if (tempPayments.size() % 2 == 0) {
 					tempP.setPaymentDate(new Date());
 					tempP.setPostedDate(new Date());
 					tempP.setPosted(true);
 					tempP.setDebit(m.getAmount());
 					tempP.insert();
 				}
 			} else {
 				if (tempPayments.size() % 2 == 1) {
 					// Negative Debit
 					tempP = (Payment)tempPayments.get(tempPayments.size() - 1);
 					tempP.setPaymentID(0);
 					tempP.setPaymentDate(new Date());
 					tempP.setPostedDate(new Date());
 					tempP.setPosted(true);
 					tempP.setDebit(0 - tempP.getDebit());
 					tempP.insert();
 				}
 			}
 		}
 	}	
 }
