 package se.idega.idegaweb.commune.school.presentation;
 
 import is.idega.idegaweb.member.business.MemberFamilyLogic;
 import is.idega.idegaweb.member.business.NoCustodianFound;
 
 import java.rmi.RemoteException;
 import java.text.DateFormat;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import javax.ejb.FinderException;
 
 import se.idega.idegaweb.commune.childcare.business.ChildCareSession;
 import se.idega.idegaweb.commune.childcare.check.business.CheckBusiness;
 import se.idega.idegaweb.commune.presentation.CitizenChildren;
 import se.idega.idegaweb.commune.presentation.CommuneBlock;
 import se.idega.idegaweb.commune.school.business.SchoolChoiceBusiness;
 import se.idega.idegaweb.commune.school.business.SchoolCommuneBusiness;
 import se.idega.idegaweb.commune.school.data.SchoolChoice;
 import se.idega.util.PIDChecker;
 
 import com.idega.block.navigation.presentation.UserHomeLink;
 import com.idega.block.school.business.SchoolBusiness;
 import com.idega.block.school.data.School;
 import com.idega.block.school.data.SchoolArea;
 import com.idega.block.school.data.SchoolCategoryBMPBean;
 import com.idega.block.school.data.SchoolClass;
 import com.idega.block.school.data.SchoolClassMember;
 import com.idega.block.school.data.SchoolSeason;
 import com.idega.block.school.data.SchoolType;
 import com.idega.block.school.data.SchoolTypeHome;
 import com.idega.block.school.data.SchoolYear;
 import com.idega.business.IBOLookup;
 import com.idega.business.IBOLookupException;
 import com.idega.business.IBORuntimeException;
 import com.idega.core.builder.data.ICPage;
 import com.idega.core.localisation.data.ICLanguage;
 import com.idega.core.localisation.data.ICLanguageHome;
 import com.idega.core.location.data.Address;
 import com.idega.data.IDOEntity;
 import com.idega.data.IDOLookup;
 import com.idega.idegaweb.IWApplicationContext;
 import com.idega.idegaweb.IWBundle;
 import com.idega.idegaweb.IWPropertyList;
 import com.idega.idegaweb.IWResourceBundle;
 import com.idega.presentation.IWContext;
 import com.idega.presentation.Page;
 import com.idega.presentation.PresentationObject;
 import com.idega.presentation.Script;
 import com.idega.presentation.Table;
 import com.idega.presentation.text.Break;
 import com.idega.presentation.text.Link;
 import com.idega.presentation.text.Text;
 import com.idega.presentation.ui.CheckBox;
 import com.idega.presentation.ui.DataTable;
 import com.idega.presentation.ui.DateInput;
 import com.idega.presentation.ui.DropdownMenu;
 import com.idega.presentation.ui.Form;
 import com.idega.presentation.ui.HiddenInput;
 import com.idega.presentation.ui.RadioButton;
 import com.idega.presentation.ui.SubmitButton;
 import com.idega.presentation.ui.TextArea;
 import com.idega.presentation.ui.TextInput;
 import com.idega.user.business.UserBusiness;
 import com.idega.user.data.User;
 import com.idega.util.Age;
 import com.idega.util.IWCalendar;
 import com.idega.util.IWTimestamp;
 import com.idega.util.PersonalIDFormatter;
 
 /**
  * <p>
  * Title:
  * </p>
  * <p>
  * Description:
  * </p>
  * <p>
  * Copyright: Copyright (c) 2002
  * </p>
  * <p>
  * Company: idega
  * </p>
  * 
  * @author <br>
  *         <a href="mailto:aron@idega.is">Aron Birkir </a> <br>
  * @version 1.0
  */
 
 public class SchoolChoiceApplication extends CommuneBlock {
 
 	IWBundle iwb;
 	IWResourceBundle iwrb;
 	UserBusiness userbuiz;
 	Collection schoolTypes = null;
 	List[] schools = null;
 	int preTypeId = -1;
 	int preAreaId = -1;
 	DateFormat df;
 
 	private String prefix = "sch_app_";
 	private String prmSendCatalogue = prefix + "snd_cat";
 	private String prmCustodiansAgree = prefix + "cus_agr";
 	private String prmFirstSchool = prefix + "fst_scl";
 	private String prmSecondSchool = prefix + "snd_scl";
 	private String prmThirdSchool = prefix + "trd_scl";
 	private String prmFirstArea = prefix + "fst_ara";
 	private String prmSecondArea = prefix + "snd_ara";
 	private String prmThirdArea = prefix + "trd_ara";
 	private String prmPreSchool = prefix + "pre_scl";
 	private String prmPreArea = prefix + "pre_ara";
 	private String prmPreType = prefix + "pre_typ";
 	private String prmType = prefix + "cho_typ";
 	//private String prmPreGrade = prefix + "pre_grd";
 	private String prmYear = prefix + "year";
 	private String prmPreYear = prefix + "pre_year";
 	private String prmMessage = prefix + "msg";
 	//private String prmAutoAssign = prefix + "aut_ass";
 	//private String prmSchoolChange = prefix + "scl_chg";
 	private String prmSixYearCare = prefix + "six_car";
 	private String prmLanguage = prefix + "cho_lng";
 	private String prmAfterschool = prefix + "aft_schl";
 	private String prmAction = prefix + "snd_frm";
 	private String prmChildId = CitizenChildren.getChildIDParameterName();
 	private String prmParentId = CitizenChildren.getParentIDParameterName();
 	private String prmForm = prefix + "the_frm";
 	//private String prmCaseOwner = prefix+"cse_own";
 	private String prmNativeLangIsChecked = prefix + "native_lang_is_checked";
 	private String prmNativeLang = prefix + "native_lang";
 	private String prmExtraChoiceMessage = prefix + "choice_message";
 
 	private boolean valSendCatalogue = false;
 	private boolean valSixyearCare = false;
 	private boolean valAutoAssign = false;
 	private boolean valWantsAfterSchool = false;
 	//private boolean valSchoolChange = false;
 	private boolean valCustodiansAgree = false;
 	private String valMessage = "";
 	private String valLanguage = "";
 	private int valFirstSchool = -1;
 	private int valSecondSchool = -1;
 	private int valThirdSchool = -1;
 	private int valFirstArea = -1;
 	private int valSecondArea = -1;
 	private int valThirdArea = -1;
 	//private int valPreGrade = -1;
 	private int valYear = -1;
 	private int valPreYear = -1;
 	private int valPreSchool = -1;
 	private int valType = -1;
 	private int childId = -1;
 	private int valCaseOwner = -1;
 	private int valMethod = 1; // Citizen:1; Quick: 2; Automatic: 3
 	private boolean valNativeLangIsChecked = false;
 	private int valNativeLang = -1;
 	private String[] valExtraChoiceMessages = new String[3];
 	private boolean showAgree = false;
 	protected boolean quickAdmin = false;
 	SchoolChoiceBusiness schBuiz;
 	SchoolClassMember schoolClassMember = null;
 	SchoolCommuneBusiness schCommBiz;
 	SchoolClass schoolClass = null;
 	School school = null;
 	SchoolYear schoolYear = null;
 	SchoolArea schoolArea = null;
 	SchoolType schoolType = null;
 	SchoolSeason season = null;
 
 	Map schoolsByType = null;
 
 	private boolean hasPreviousSchool = false;
 	private boolean schoolChange = false;
 	private boolean[] canApply = { false, false, false};
 	private Age age;
 	private Form myForm;
 
 	private Integer afterSchoolPageID = null;
 	private Integer checkPageID = null;
 
 	private boolean isOwner = true;
 	private User owner;
 	private Date choiceDate;
 	private boolean hasLanguageSelection = false;
 	private boolean _useOngoingSeason = false;
 	private String prmPlacementDate = "prm_placement_date";
 	private java.sql.Date valPlacementDate = null;
 	private boolean _isForTesting = false;
 	private boolean _showChoiceMessage = false;
 
 	private boolean _forwardToCheckPage = true;
 	private int _maxAge = 0;
 	private boolean _useCheckBoxForAfterSchoolCare = false;
 	private boolean _showChildCareTypes = false;
 
 	/**
 	 * @param ongoingSeason
 	 */
 	public void setUseOngoingSeason(boolean ongoingSeason) {
 		_useOngoingSeason = ongoingSeason;
 	}
 
 	public void main(IWContext iwc) throws Exception {
 		iwb = getBundle(iwc);
 		iwrb = getResourceBundle(iwc);
 		df = DateFormat.getDateInstance(DateFormat.SHORT, iwc.getCurrentLocale());
 		schBuiz = (SchoolChoiceBusiness) IBOLookup.getServiceInstance(iwc, SchoolChoiceBusiness.class);
 		canApply = checkCanApply(iwc);
 		control(iwc);
 	}
 
 	public void control(IWContext iwc) throws Exception {
 		//debugParameters(iwc);
 		String ID = iwc.getParameter(prmChildId);
 		if (iwc.isLoggedOn() && canApply[0]) {
 			if (ID != null) {
 				childId = Integer.parseInt(ID);
 				userbuiz = (UserBusiness) IBOLookup.getServiceInstance(iwc, UserBusiness.class);
 				schCommBiz = (SchoolCommuneBusiness) IBOLookup.getServiceInstance(iwc, SchoolCommuneBusiness.class);
 				boolean custodiansAgree = true;
 
 				if (_useOngoingSeason) {
 					try {
 						season = schBuiz.getSchoolSeasonHome().findSeasonByDate(new IWTimestamp().getDate());
 					}
 					catch (FinderException e) {
 						season = schBuiz.getCurrentSeason();
 					}
 				}
 				else
 					season = schBuiz.getCurrentSeason();
 
 				User child = userbuiz.getUser(childId);
 				if (child != null) {
 					initSchoolChild(child);
 					boolean hasChoosed = false;
 					Collection currentChildChoices = null;
 					parse(iwc);
 					boolean saved = false;
 					if (iwc.isParameterSet(prmAction) && iwc.getParameter(prmAction).equals("true")) {
 						saved = saveSchoolChoice();
 					}
 					else {
 						int seasonID = ((Integer) season.getPrimaryKey()).intValue();
 						String[] statuses = { schBuiz.getCaseStatusDeleted().getStatus()};
 						currentChildChoices = schBuiz.getSchoolChoiceHome().findByChildAndSeason(childId, seasonID, statuses);
 						if (!currentChildChoices.isEmpty()) {
 							Iterator iter = currentChildChoices.iterator();
 							int count = 1;
 							while (iter.hasNext()) {
 								SchoolChoice element = (SchoolChoice) iter.next();
 								School school = element.getChosenSchool();
 								int schoolID = ((Integer) school.getPrimaryKey()).intValue();
 								int areaID = school.getSchoolAreaId();
 								valExtraChoiceMessages[count - 1] = element.getExtraChoiceMessage();
 
 								if (count == 1) {
 									valFirstSchool = schoolID;
 									valFirstArea = areaID;
 									/*
 									 * if (element.getFreetimeInThisSchool()) valSixyearCare =
 									 * true; else if (element.getFreetimeOther() != null)
 									 * valSixyearCare = true; else
 									 */
 									valSixyearCare = element.getKeepChildrenCare();
 									if (element.getLanguageChoice() != null) valLanguage = element.getLanguageChoice();
 									if (element.getMessage() != null) valMessage = element.getMessage();
 									count++;
 									if (!hasPreviousSchool) {
 										initSchoolFromChoice(element);
 									}
 									owner = element.getOwner();
 									choiceDate = element.getCreated();
 									isOwner = ((IDOEntity) iwc.getCurrentUser()).equals(owner);
 									custodiansAgree = element.getCustodiansAgree();
 									valType = element.getSchoolTypeId();
 									valYear = element.getSchoolYearID();
 								}
 								else if (count == 2) {
 									valSecondSchool = schoolID;
 									valSecondArea = areaID;
 									count++;
 								}
 								else {
 									valThirdSchool = schoolID;
 									valThirdArea = areaID;
 									count++;
 								}
 							}
 						}
 					}
 
 					if (valType == -1) {
 						if (hasPreviousSchool)
 							valType = 4;
 						else
 							valType = 5;
 					}
 
 					// Application has been saved
 
 					schoolTypes = getSchoolTypes(schBuiz.getSchoolBusiness().getElementarySchoolSchoolCategory());
 
 					if (saved) {
 						/*
 						 * if (valSixyearCare && childcarePage != null) {
 						 * iwc.setSessionAttribute(CitizenChildren.getChildIDParameterName(),
 						 * new Integer(childId)); iwc.forwardToIBPage(getParentPage(),
 						 * childcarePage); } else add(getSchoolChoiceAnswer(iwc, child));
 						 */
 						// User wants to choose
 						if (valWantsAfterSchool) {
 							boolean hasApprovedCheck = getCheckBusiness(iwc).hasGrantedCheck(child);
 							getChildCareSession(iwc).setChildID(((Integer) child.getPrimaryKey()).intValue());
 							if (!_forwardToCheckPage) {
 								hasApprovedCheck = true;
 							}
 							// forward to afterschool page
 							if (hasApprovedCheck && afterSchoolPageID != null) {
 								iwc.forwardToIBPage(getParentPage(), afterSchoolPageID.intValue());
 							}
 							// forward to check application page
 							else if (checkPageID != null) {
 								iwc.forwardToIBPage(getParentPage(), checkPageID.intValue());
 							}
 							add(getSchoolChoiceAnswer(child));
 						}
 						else {
 							if (getResponsePage() != null) iwc.forwardToIBPage(getParentPage(), getResponsePage());
 							add(getSchoolChoiceAnswer(child));
 						}
 					}
 					else if (hasChoosed) {
 						add(getAlreadyChosenAnswer(child));
 					}
 					else {
 						if (custodiansAgree || isOwner)
 							add(getSchoolChoiceForm(iwc, child));
 						else
 							add(getLocalizedHeader("school.cannot_alter_choice", "You cannot alter the school choice already made."));
 					}
 				}
 			}
 			else
 				add(getLocalizedHeader("school.no_student_id_provided", "No student provided"));
 		}
 		else if (!iwc.isLoggedOn())
 			add(getLocalizedHeader("school.need_to_be_logged_on", "You need to log in"));
 		else if (!canApply[0]) add(getLocalizedHeader("school_choice.last_date_expired", "Time limits to apply expired"));
 	}
 
 	private boolean saveSchoolChoice() {
 		try {
 			//			schBuiz.createSchoolChoices(valCaseOwner, childId, valType,
 			// valPreSchool, valFirstSchool, valSecondSchool, valThirdSchool,
 			// valPreGrade, valMethod, -1, -1, valLanguage, valMessage,
 			// schoolChange, valSixyearCare, valAutoAssign, valCustodiansAgree,
 			// valSendCatalogue, valPlacementDate, season);
 			schBuiz.createSchoolChoices(valCaseOwner, childId, valType, valPreSchool, valFirstSchool, valSecondSchool, valThirdSchool, valYear, valPreYear, valMethod, -1, -1, valLanguage, valMessage, schoolChange, valSixyearCare, valAutoAssign, valCustodiansAgree, valSendCatalogue, valPlacementDate, season, valNativeLangIsChecked, valNativeLang, valExtraChoiceMessages);
 			return true;
 		}
 		catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return false;
 	}
 
 	private void parse(IWContext iwc) {
 		valSendCatalogue = iwc.isParameterSet(prmSendCatalogue);
 		valSixyearCare = iwc.isParameterSet(prmSixYearCare);
 		if (iwc.isParameterSet(prmAfterschool)) {
 			valWantsAfterSchool = iwc.getParameter(prmAfterschool).equalsIgnoreCase(Boolean.TRUE.toString());
 		}
 
 		valAutoAssign = true;
 		//valSchoolChange = iwc.isParameterSet(prmSchoolChange);
 		valCustodiansAgree = iwc.isParameterSet(prmFirstSchool) ? Boolean.valueOf(iwc.getParameter(prmCustodiansAgree)).booleanValue() : false;
 		valMessage = iwc.getParameter(prmMessage);
 		valLanguage = iwc.getParameter(prmLanguage);
 		valPlacementDate = iwc.isParameterSet(prmPlacementDate) ? new IWTimestamp(iwc.getParameter(prmPlacementDate)).getDate() : null;
 		valFirstSchool = iwc.isParameterSet(prmFirstSchool) ? Integer.parseInt(iwc.getParameter(prmFirstSchool)) : -1;
 		valSecondSchool = iwc.isParameterSet(prmSecondSchool) ? Integer.parseInt(iwc.getParameter(prmSecondSchool)) : -1;
 		valThirdSchool = iwc.isParameterSet(prmThirdSchool) ? Integer.parseInt(iwc.getParameter(prmThirdSchool)) : -1;
 		valFirstArea = iwc.isParameterSet(prmFirstArea) ? Integer.parseInt(iwc.getParameter(prmFirstArea)) : -1;
 		valSecondArea = iwc.isParameterSet(prmSecondArea) ? Integer.parseInt(iwc.getParameter(prmSecondArea)) : -1;
 		valThirdArea = iwc.isParameterSet(prmThirdArea) ? Integer.parseInt(iwc.getParameter(prmThirdArea)) : -1;
 		valYear = iwc.isParameterSet(prmYear) ? Integer.parseInt(iwc.getParameter(prmYear)) : -1;
 		valPreYear = iwc.isParameterSet(prmPreYear) ? Integer.parseInt(iwc.getParameter(prmPreYear)) : -1;
 		valPreSchool = iwc.isParameterSet(prmPreSchool) ? Integer.parseInt(iwc.getParameter(prmPreSchool)) : -1;
 		valType = iwc.isParameterSet(prmType) ? Integer.parseInt(iwc.getParameter(prmType)) : -1;
 		valCaseOwner = iwc.isParameterSet(prmParentId) ? Integer.parseInt(iwc.getParameter(prmParentId)) : -1;
 		valNativeLangIsChecked = iwc.isParameterSet(prmNativeLangIsChecked) ? true : false;
 		valNativeLang = iwc.isParameterSet(prmNativeLang) ? Integer.parseInt(iwc.getParameter(prmNativeLang)) : -1;
 		valExtraChoiceMessages = iwc.getParameterValues(prmExtraChoiceMessage) != null ? iwc.getParameterValues(prmExtraChoiceMessage) : new String[3];
 		if (!quickAdmin) {
 			valCaseOwner = iwc.getUserId();
 		}
 		else {
 			valMethod = 2;
 		}
 	}
 
 	public PresentationObject getSchoolChoiceForm(IWContext iwc, User child) throws java.rmi.RemoteException {
 		if (this.schoolChange) {
 			if (!canApply[2]) return getSmallHeader(localize("school_choice.not_possible_to_change_school", "It is not possible to change school until after the 22nd of February."));
 		}
 
 		myForm = new Form();
 		myForm.setName(prmForm);
 
 		Table T = new Table();
 		T.setCellpadding(0);
 		T.setCellspacing(0);
 		T.setBorder(0);
 		myForm.add(T);
 		int row = 1;
 
 		if (!isOwner) {
 			T.add(getAlterChoiceInfo(iwc), 1, row++);
 			T.setHeight(row++, 12);
 		}
 
 		T.add(getCurrentSchoolSeasonInfo(iwc), 1, row++);
 		T.setHeight(row++, 12);
 		T.add(getChildInfo(iwc, child), 1, row);
 		T.mergeCells(1, row, 3, row);
 		T.add(getCurrentSchool(), 2, row++);
 		T.setHeight(row++, 12);
 		//		T.add(getCurrentSchool(), 1, row++);
 		//		T.setHeight(row++, 12);
 		T.add(getChoiceSchool(), 1, row++);
 		T.setHeight(row++, 12);
 		T.add(getMessagePart(), 1, row++);
 		//T.setHeight(row++, 12);
 
 		/*
 		 * SubmitButton button = (SubmitButton) getButton(new SubmitButton("submit",
 		 * localize("school_choice.ready", "Ready"))); //T.setAlignment(1, row,
 		 * Table.HORIZONTAL_ALIGN_RIGHT); T.add(button, 1, row++);
 		 * 
 		 * button.setOnSubmitFunction("checkApplication", getSchoolCheckScript());
 		 * myForm.setToDisableOnSubmit(button, true);
 		 */
 		T.add(new HiddenInput(prmAction, "true"), 1, 1);
 		T.add(new HiddenInput(prmChildId, child.getPrimaryKey().toString()), 1, 1);
 
 		Page p = this.getParentPage();
 		if (p != null) {
 			Script S = p.getAssociatedScript();
 			Script F = new Script();
 
 			S.addVariable("sportOrMusicSchools", "new Array('2', '3', '4')");
 			S.addFunction("getAlertIfSportsOrMusicSchool", getAlertIfSportsOrMusicSchool());
 
 			S.addFunction("initFilter", getInitFilterScript());
 			S.addFunction("setSelected", getSetSelectedScript());
 
 			S.addFunction("changeFilter", getFilterScript());
 			S.addFunction("changeFilter2", getFilterScript2(iwc));
 
 			T.add(F, 1, T.getColumns());
 			if (valType > 0) {
 				Script initScript = myForm.getAssociatedFormScript();
 				if (initScript == null) {
 					initScript = new Script();
 					myForm.setAssociatedFormScript(initScript);
 				}
 
 				if (!schoolChange) {
 					String initFunction2 = getInitFilterCallerScript(prmType, prmSecondArea, prmSecondSchool, valType, valSecondArea, valSecondSchool, false);
 					String initFunction3 = getInitFilterCallerScript(prmType, prmThirdArea, prmThirdSchool, valType, valThirdArea, valThirdSchool, false);
 					initScript.addFunction("sch_init3", initFunction3);
 					initScript.addFunction("sch_init2", initFunction2);
 				}
 
 				String initFunction = getInitFilterCallerScript(prmType, prmFirstArea, prmFirstSchool, valType, valFirstArea, valFirstSchool, false);
 				initScript.addFunction("sch_init", initFunction);
 				getParentPage().setOnLoad(initFunction);
 			}
 		}
 
 		return myForm;
 	}
 
 	public PresentationObject getSchoolChoiceAnswer(User child) {
 		Table T = new Table();
 		T.setCellpadding(getCellpadding());
 		T.setCellspacing(0);
 		String text1 = iwrb.getLocalizedString("school_choice.receipt_1", "The school choice for ");
 		String text2 = iwrb.getLocalizedString("school_choice.receipt_2", "has been received. ");
 		String text3 = iwrb.getLocalizedString("school_choice.receipt_3", "The application will be processed by each school you applied for .");
 		String text4 = iwrb.getLocalizedString("school_choice.receipt_4", "Thank you");
 
 		int row = 1;
 		T.add(getText(text1), 1, row);
 		T.add(getHeader(child.getName()), 1, row);
 		T.add(getText(text2), 1, row++);
 		T.add(getText(text3), 1, row++);
 		T.add(getText(text4), 1, row++);
 		T.setHeight(row++, 12);
 		T.add(getUserHomePageLink(), 1, row++);
 
 		return T;
 	}
 
 	public PresentationObject getAlreadyChosenAnswer(User child) {
 		DataTable T = new DataTable();
 		T.setUseBottom(false);
 		T.setUseTop(false);
 		T.setUseTitles(false);
 		String text1 = iwrb.getLocalizedString("school_choice.already_1", "The school choice for ");
 		String text2 = iwrb.getLocalizedString("school_choice.already_2", "has already been received. ");
 
 		int row = 1;
 		T.add(getText(text1), 1, row);
 		T.add(getHeader(child.getName()), 1, row);
 		T.add(getText(text2), 1, row);
 		row++;
 		return T;
 	}
 
 	public PresentationObject getAlterChoiceInfo(IWContext iwc) {
 		Table T = new Table();
 		if (owner != null && choiceDate != null) {
 			Object[] arguments = { owner.getName(), new IWCalendar(choiceDate).getLocaleDate(iwc.getCurrentLocale(), IWCalendar.SHORT)};
 			String message = iwrb.getLocalizedString("school_choice.school_choice_already_made", "The school choice has already been done by {0} on {1}.");
 			Text t = getHeader(MessageFormat.format(message, arguments));
 			t.setFontColor("FF0000");
 			T.add(t, 1, 1);
 		}
 		return T;
 	}
 
 	public PresentationObject getCurrentSchoolSeasonInfo(IWContext iwc) throws RemoteException {
 		SchoolChoiceBusiness choiceBean = (SchoolChoiceBusiness) IBOLookup.getServiceInstance(iwc, SchoolChoiceBusiness.class);
 		SchoolSeason season = null;
 		try {
 			season = choiceBean.getCurrentSeason();
 		}
 		catch (RemoteException e) {
 		}
 		catch (FinderException e) {
 		}
 		Table T = new Table();
 		if (season != null) {
 			String message = iwrb.getLocalizedString("school_choice.last_date_for_choice_is", "Last date to choose is");
 			String date = df.format(season.getSchoolSeasonDueDate());
 			Text t = getHeader(message + " " + date);
 			if (canApply[1]) t.setFontColor("FF0000");
 
 		}
 		return T;
 
 	}
 
 	private void initSchoolChild(User child) {
 		try {
 			Date d = child.getDateOfBirth();
 			if (d == null) {
 				if (child.getPersonalID() != null) {
 					d = PIDChecker.getInstance().getDateFromPersonalID(child.getPersonalID());
 				}
 				if (d == null) d = new Date();
 			}
 			age = new Age(d);
 		}
 		catch (Exception e) {
 		}
 
 		try {
 			SchoolSeason previousSeason = null;
 			if (_useOngoingSeason)
 				previousSeason = season;
 			else
 				previousSeason = schCommBiz.getPreviousSchoolSeason(season);
 
 			try {
 				schoolClassMember = schBuiz.getSchoolBusiness().getSchoolClassMemberHome().findLatestByUserAndSchCategoryAndSeason(child, schBuiz.getSchoolBusiness().getCategoryElementarySchool(), previousSeason);
 			}
 			catch (FinderException e) {
 				if (_showChildCareTypes) {
 					// No elementary school placement found, look for one in child care
 					try {
 						schoolClassMember = schBuiz.getSchoolBusiness().getSchoolClassMemberHome().findLatestByUserAndSchCategory(child, schBuiz.getSchoolBusiness().getCategoryChildcare());
 					}
 					catch (FinderException fe) {
 						hasPreviousSchool = false;
 						return;
 					}
 				}
 				else {
 					hasPreviousSchool = false;
 					return;
 				}
 			}
 
 			schoolClass = schoolClassMember.getSchoolClass();
 			school = schoolClass.getSchool();
 			if (school != null) {
 				hasPreviousSchool = true;
 			}
 			if (school != null) {
 				schoolArea = school.getSchoolArea();
 			}
 
 			schoolType = schoolClassMember.getSchoolType();
 
 			schoolYear = schoolClassMember.getSchoolYear();
 			valPreYear = schoolClassMember.getSchoolYearId();
 		}
 		catch (RemoteException re) {
 			hasPreviousSchool = false;
 		}
 	}
 
 	private void initSchoolFromChoice(SchoolChoice choice) {
 		school = choice.getCurrentSchool();
 		if (school == null) {
 			hasPreviousSchool = false;
 		}
 		schoolYear = choice.getSchoolYear();
 		schoolType = choice.getSchoolType();
 		valPreYear = choice.getCurrentSchoolYearID();
 		valPreSchool = choice.getCurrentSchoolId();
 	}
 
 	private PresentationObject getChildInfo(IWContext iwc, User child) throws java.rmi.RemoteException {
 		Table table = new Table();
 		table.setColumns(3);
 		//table.setWidth(1, "100");
 		table.setWidth(2, "8");
 		table.setWidth(4, "16");
 		table.setCellpadding(2);
 		table.setCellspacing(0);
 		table.setCellpaddingLeft(1, 1, 6);
 		table.setCellpaddingLeft(1, 2, 6);
 		table.setCellpaddingLeft(1, 3, 6);
 		table.setCellpaddingRight(5, 1, 6);
 		table.setCellpaddingTop(1, 1, 6);
 		table.setCellpaddingTop(3, 1, 6);
 		table.setStyleAttribute("background:#f4f4f4;");
 		table.setStyleAttribute("border:1px solid #cccccc;");
 
 		table.setBorder(0);
 		table.add(getSmallHeader(iwrb.getLocalizedString("school.name", "Name") + ":"), 1, 1);
 		table.add(getSmallHeader(iwrb.getLocalizedString("school.personal_id", "Personal ID") + ":"), 1, 2);
 		table.add(getSmallHeader(iwrb.getLocalizedString("school.address", "Address") + ":"), 1, 3);
 
 		table.add(getSmallText(child.getNameLastFirst(true)), 3, 1);
 		String personalID = PersonalIDFormatter.format(child.getPersonalID(), iwc.getIWMainApplication().getSettings().getApplicationLocale());
 		table.add(getSmallText(personalID), 3, 2);
 
 		Address childAddress = userbuiz.getUsersMainAddress(child);
 		if (childAddress != null) {
 			table.add(getSmallText(childAddress.getStreetAddress()), 3, 3);
 			if (childAddress.getPostalAddress() != null) {
 				table.add(getSmallText(", " + childAddress.getPostalAddress()), 3, 3);
 			}
 		}
 
 		MemberFamilyLogic mlogic = (MemberFamilyLogic) IBOLookup.getServiceInstance(iwc, MemberFamilyLogic.class);
 		try {
 			Collection parents = mlogic.getCustodiansFor(child);
 			Iterator iter = parents.iterator();
 			String address = "";
 			boolean caseOwning = true;
 			boolean parentsSeparated = false;
 			while (iter.hasNext()) {
 				User parent = (User) iter.next();
 
 				Address parAddress = userbuiz.getUsersMainAddress(parent);
 				String sParAddress = null;
 				if (parAddress != null) {
 					sParAddress = parAddress.getStreetAddress();
 				}
 				else {
 					sParAddress = "";
 				}
 
 				if (address == null) {
 					address = sParAddress;
 				}
 				else {
 					if (!parentsSeparated) parentsSeparated = address.equals(sParAddress);
 				}
 
 				if (quickAdmin && caseOwning && valCaseOwner == -1) {
 					valCaseOwner = ((Integer) parent.getPrimaryKey()).intValue();
 					caseOwning = false;
 				}
 			}
 			if (valCaseOwner != -1) table.add(new HiddenInput(prmParentId, String.valueOf(valCaseOwner)), 1, 1);
 			showAgree = parentsSeparated;
 		}
 		catch (NoCustodianFound ex) {
 			ex.printStackTrace();
 			table.add(getSmallErrorText(iwrb.getLocalizedString("school.no_registered_custodians", "No registered custodians")), 3, 4);
 		}
 
 		table.add(new Break(3), 1, 5);
 		table.mergeCells(5, 1, 5, 5);
 		table.setVerticalAlignment(5, 1, Table.VERTICAL_ALIGN_TOP);
 		table.add(getCurrentSchool(), 5, 1);
 		return table;
 	}
 
 	private PresentationObject getCurrentSchool() {
 		Table table = new Table();
 		table.setColumns(3);
 		table.setCellpadding(2);
 		table.setCellspacing(0);
 		table.setBorder(0);
 
 		table.setCellpaddingTop(1, 1, 6);
 
 		table.mergeCells(1, 1, table.getColumns(), 1);
 
 		table.add(getHeader(iwrb.getLocalizedString("school.child_is_now_in", "Child is now in :")), 1, 1);
 		table.add(getSmallHeader(iwrb.getLocalizedString("school.school_type", "Type") + ":"), 1, 2);
 		table.add(getSmallHeader(iwrb.getLocalizedString("school.school_area", "Area") + ":"), 1, 3);
 		table.add(getSmallHeader(iwrb.getLocalizedString("school.school_name", "School name") + ":"), 1, 4);
 		table.add(getSmallHeader(iwrb.getLocalizedString("school.school_year", "School year") + ":"), 1, 5);
 
 		Text schoolTypeCurr = null;
 		Text schoolAreaCurr = null;
 		Text schoolYearCurr = null;
 		Text schoolCurr = null;
 
 		if (schoolType != null) {
 			schoolTypeCurr = getSmallText(schoolType.toString());
 			table.add(new HiddenInput(prmPreType, String.valueOf(schoolType.getPrimaryKey())), 1, 6);
 		}
 		else {
 			schoolTypeCurr = getSmallText("-");
 		}
 		if (schoolArea != null) {
 			schoolAreaCurr = getSmallText(schoolArea.toString());
 			table.add(new HiddenInput(prmPreArea, String.valueOf(schoolArea.getPrimaryKey())), 1, 6);
 		}
 		else {
 			schoolAreaCurr = getSmallText("-");
 		}
 		if (school != null) {
 			schoolCurr = getSmallText(school.getName());
 			table.add(new HiddenInput(prmPreSchool, String.valueOf(school.getPrimaryKey())), 1, 6);
 		}
 		else {
 			schoolCurr = getSmallText("-");
 		}
 
 		if (schoolYear != null) {
 			schoolYearCurr = getSmallText(schoolYear.toString());
 			table.add(new HiddenInput(prmPreYear, String.valueOf(schoolYear.getPrimaryKey().toString())), 1, 6);
 		}
 		else {
 			schoolYearCurr = getSmallText("-");
 		}
 
 		//table.add(drpTypes, 3, 2);
 		table.add(schoolTypeCurr, 3, 2);
 		//table.add(drpAreas, 3, 3);
 		table.add(schoolAreaCurr, 3, 3);
 		//table.add(drpSchools, 3, 4);
 		table.add(schoolCurr, 3, 4);
 		//table.add(drpGrade, 3, 5);
 		table.add(schoolYearCurr, 3, 5);
 		table.setWidth(1, 2, "100");
 		table.setWidth(2, 2, "8");
 
 		return table;
 	}
 
 	private DropdownMenu getTypeDrop(String name, boolean useRestrictions, boolean showChildCare) throws java.rmi.RemoteException {
 		DropdownMenu drp = (DropdownMenu) getStyledInterface(new DropdownMenu(name));
 		drp.addMenuElement("-1", iwrb.getLocalizedString("school.school_type_select", "School type select"));
 
 		if (showChildCare) {
 			SchoolTypeHome tHome = (SchoolTypeHome) IDOLookup.getHome(SchoolType.class);
 			try {
 				Collection ccTypes = tHome.findAllByCategory(SchoolCategoryBMPBean.CATEGORY_CHILD_CARE, false);
 				for (Iterator iter = ccTypes.iterator(); iter.hasNext();) {
 					SchoolType tmpType = (SchoolType) iter.next();
 					drp.addMenuElement(tmpType.getPrimaryKey().toString(), localize(schCommBiz.getLocalizedSchoolTypeKey(tmpType), tmpType.getSchoolTypeName()));
 					schoolTypes.add(tmpType);
 				}
 			}
 			catch (FinderException e) {
 				logWarning("No childcare categories found");
 				log(e);
 			}
 		}
 
 		Iterator iter = schoolTypes.iterator();
 		boolean canChoose = true;
 
 		while (iter.hasNext()) {
 			SchoolType type = (SchoolType) iter.next();
 			if (age.getYears() <= type.getMaxSchoolAge())
 				canChoose = true;
 			else
 				canChoose = false;
 			if (!useRestrictions) canChoose = true;
 
 			if (canChoose) drp.addMenuElement(type.getPrimaryKey().toString(), localize(schCommBiz.getLocalizedSchoolTypeKey(type), type.getSchoolTypeName()));
 		}
 
 		return drp;
 	}
 
 	/**
 	 * Method getChoiceSchool.
 	 * 
 	 * @param iwc
 	 * @param child
 	 * @return PresentationObject
 	 * @throws RemoteException
 	 */
 	private PresentationObject getChoiceSchool() throws java.rmi.RemoteException {
 		Table table = new Table();
 		table.setCellpadding(2);
 		table.setCellspacing(0);
 		table.setColumns(5);
 		table.mergeCells(1, 1, 4, 1);
 		table.setBorder(0);
 		table.setWidthAndHeightToHundredPercent();
 		table.setWidth(5, 1, "100%");
 		table.add(getHeader(iwrb.getLocalizedString("school.choice_for_schoolyear", "Choice for the schoolyear")), 1, 1);
 
 		DropdownMenu typeDrop = getTypeDrop(prmType, true, false);
 		typeDrop.setOnChange(getFilterCallerScript(prmType, prmFirstArea, prmFirstSchool, 1, false));
 
 		DropdownMenu drpGrade = (DropdownMenu) getStyledInterface(new DropdownMenu(prmYear));
 		Collection coll = getSchoolYears();
 		if (coll != null) {
 			Iterator iter = coll.iterator();
 			while (iter.hasNext()) {
 				SchoolYear element = (SchoolYear) iter.next();
 				drpGrade.addMenuElement(element.getPrimaryKey().toString(), element.getName());
 			}
 		}
 		drpGrade.addMenuElementFirst("-1", iwrb.getLocalizedString("school.select_year", "Select year"));
 		drpGrade.setSelectedElement(String.valueOf(valYear));
 
 		CheckBox chkChildCare = getCheckBox(prmSixYearCare, "true");
 		chkChildCare.setChecked(valSixyearCare);
 
 		DropdownMenu txtLangChoice = (DropdownMenu) getStyledInterface(new DropdownMenu(prmLanguage));
 		txtLangChoice.addMenuElement(-1, localize("school.language", "Language"));
 		txtLangChoice.addMenuElement("school.language_german", localize("school.language_german", "German"));
 		txtLangChoice.addMenuElement("school.language_french", localize("school.language_french", "French"));
 		txtLangChoice.addMenuElement("school.language_spanish", localize("school.language_spanish", "Spanish"));
 		txtLangChoice.addMenuElement("school.language_swedish_english", localize("school.language_swedish_english", "Swedish/English"));
 		if (valLanguage != null) txtLangChoice.setSelectedElement(valLanguage);
 
 		DropdownMenu drpFirstArea = getDropdown(prmFirstArea, prmType, prmFirstArea, prmFirstSchool, 2, iwrb.getLocalizedString("school.area_first", "School Area...................."));
 		DropdownMenu drpFirstSchool = (DropdownMenu) getStyledInterface(new DropdownMenu(prmFirstSchool));
 		drpFirstSchool.addMenuElementFirst("-1", iwrb.getLocalizedString("school.school_first", "School........................."));
 		drpFirstSchool.setOnChange("alertIfSportsOrMusicSchool(this)");
 
 		int row = 2;
 		table.add(getSmallHeader(iwrb.getLocalizedString("school.school_type", "SchoolType") + ":"), 1, row);
 		table.add(typeDrop, 3, row++);
 
 		table.add(getSmallHeader(iwrb.getLocalizedString("school.school_tear", "School year") + ":"), 1, row);
 		table.add(drpGrade, 3, row++);
 
 		table.add(getSmallHeader(iwrb.getLocalizedString("school.first_choice", "First choice") + ":"), 1, row);
 		table.add(drpFirstArea, 3, row);
 		table.add(drpFirstSchool, 5, row++);
 
 		if (this._showChoiceMessage) {
 			table.setHeight(row++, 2);
 			TextInput choiceMessage = (TextInput) getStyledInterface(new TextInput(prmExtraChoiceMessage));
 			if (valExtraChoiceMessages[0] != null) {
 				choiceMessage.setContent(valExtraChoiceMessages[0]);
 			}
 			choiceMessage.setLength(50);
 			table.mergeCells(3, row, 5, row);
 			table.add(getSmallHeader(iwrb.getLocalizedString("school.extra_message", "Extra message") + ":"), 3, row);
 			table.add(new Break(), 3, row);
 			table.add(choiceMessage, 3, row++);
 			table.setHeight(row++, 6);
 		}
 
 		if (!this.schoolChange) {
 			typeDrop.setOnChange(getFilterCallerScript(prmType, prmSecondArea, prmSecondSchool, 1, false));
 			typeDrop.setOnChange(getFilterCallerScript(prmType, prmThirdArea, prmThirdSchool, 1, false));
 
 			DropdownMenu drpSecondArea = getDropdown(prmSecondArea, prmType, prmSecondArea, prmSecondSchool, 2, iwrb.getLocalizedString("school.area_second", "School Area...................."));
 			DropdownMenu drpSecondSchool = (DropdownMenu) getStyledInterface(new DropdownMenu(prmSecondSchool));
 			drpSecondSchool.addMenuElementFirst("-1", iwrb.getLocalizedString("school.school_second", "School........................."));
 			drpSecondSchool.setOnChange("alertIfSportsOrMusicSchool(this)");
 
 			DropdownMenu drpThirdArea = getDropdown(prmThirdArea, prmType, prmThirdArea, prmThirdSchool, 2, iwrb.getLocalizedString("school.area_third", "School Area...................."));
 			DropdownMenu drpThirdSchool = (DropdownMenu) getStyledInterface(new DropdownMenu(prmThirdSchool));
 			drpThirdSchool.addMenuElementFirst("-1", iwrb.getLocalizedString("school.school_third", "School........................."));
 			drpThirdSchool.setOnChange("alertIfSportsOrMusicSchool(this)");
 
 			table.add(getSmallHeader(iwrb.getLocalizedString("school.second_choice", "Second choice") + ":"), 1, row);
 			table.add(drpSecondArea, 3, row);
 			table.add(drpSecondSchool, 5, row++);
 
 			if (this._showChoiceMessage) {
 				table.setHeight(row++, 2);
 				TextInput choiceMessage = (TextInput) getStyledInterface(new TextInput(prmExtraChoiceMessage));
 				if (valExtraChoiceMessages[1] != null) {
 					choiceMessage.setContent(valExtraChoiceMessages[1]);
 				}
 				choiceMessage.setLength(50);
 				table.mergeCells(3, row, 5, row);
 				table.add(getSmallHeader(iwrb.getLocalizedString("school.extra_message", "Extra message") + ":"), 3, row);
 				table.add(new Break(), 3, row);
 				table.add(choiceMessage, 3, row++);
 				table.setHeight(row++, 6);
 			}
 
 			table.add(getSmallHeader(iwrb.getLocalizedString("school.third_choice", "Third choice") + ":"), 1, row);
 			table.add(drpThirdArea, 3, row);
 			table.add(drpThirdSchool, 5, row++);
 
 			if (this._showChoiceMessage) {
 				table.setHeight(row++, 2);
 				TextInput choiceMessage = (TextInput) getStyledInterface(new TextInput(prmExtraChoiceMessage));
 				if (valExtraChoiceMessages[2] != null) {
 					choiceMessage.setContent(valExtraChoiceMessages[2]);
 				}
 				choiceMessage.setLength(50);
 				table.mergeCells(3, row, 5, row);
 				table.add(getSmallHeader(iwrb.getLocalizedString("school.extra_message", "Extra message") + ":"), 3, row);
 				table.add(new Break(), 3, row);
 				table.add(choiceMessage, 3, row++);
 				table.setHeight(row++, 6);
 			}
 		}
 
 		if ((schoolYear != null && schoolYear.getSchoolYearAge() >= 12) || age.getYears() >= 11) {
 			table.setHeight(row++, 5);
 			table.add(getSmallHeader(iwrb.getLocalizedString("school.six_year_language", "Language") + ":"), 1, row);
 			table.add(txtLangChoice, 3, row);
 			table.mergeCells(3, row, 5, row++);
 			hasLanguageSelection = true;
 		}
 
 		if (_useOngoingSeason) {
 			IWTimestamp stamp = new IWTimestamp();
 			stamp.addDays(1);
 			DateInput date = (DateInput) getStyledInterface(new DateInput(prmPlacementDate));
 			date.setDate(stamp.getDate());
 			date.setEarliestPossibleDate(stamp.getDate(), iwrb.getLocalizedString("school.dates_back_in_time_not_allowed", "You can not choose a date back in time."));
 			table.setHeight(row++, 5);
 			table.add(getSmallHeader(iwrb.getLocalizedString("school.placement_date", "Placement date") + ":"), 1, row);
 			table.add(date, 3, row);
 			table.mergeCells(3, row, 5, row++);
 		}
 
 		if (_maxAge <= 0) {
 			_maxAge = 10;
 		}
 
 		if (age.getYears() <= _maxAge) {
 			table.setHeight(row++, 5);
 			table.mergeCells(1, row, 5, row);
 			table.add(getHeader(iwrb.getLocalizedString("school.after_school_choice", "Choice of after school care")), 1, row);
 			table.add(Text.getNonBrakingSpace(), 1, row);
 			Link infoLink = new Link(this.getInformationIcon(iwrb.getLocalizedString("school.after_school_choice_information", "Information about the after school choice.")));
 			infoLink.setToOpenAlert(iwrb.getLocalizedString("school.after_school_choice_message", "Information about the after school choice..."));
 			table.add(infoLink, 1, row++);
 
 			table.mergeCells(1, row, 5, row);
 			if (_useCheckBoxForAfterSchoolCare) {
 				CheckBox wantsAfterSchool = getCheckBox(prmAfterschool, Boolean.TRUE.toString());
 				table.add(wantsAfterSchool, 1, row);
 				table.add(getSmallHeader(Text.getNonBrakingSpace() + iwrb.getLocalizedString("school.want_after_school_care", "I want afterschool care")), 1, row++);
 			}
 			else {
 				RadioButton rbWantsAfterSchool = getRadioButton(prmAfterschool, Boolean.TRUE.toString());
 				RadioButton rbNotAfterSchool = getRadioButton(prmAfterschool, Boolean.FALSE.toString());
 				rbNotAfterSchool.setMustBeSelected(iwrb.getLocalizedString("school.must_select_after_school_option", "You have to select an after school option"));
 				table.add(rbWantsAfterSchool, 1, row);
 				table.add(getSmallHeader(Text.getNonBrakingSpace() + iwrb.getLocalizedString("school.want_after_school_care", "I want afterschool care")), 1, row++);
 				table.mergeCells(1, row, 5, row);
 				table.add(rbNotAfterSchool, 1, row);
 				table.mergeCells(1, row, 5, row);
 				table.add(getSmallHeader(Text.getNonBrakingSpace() + iwrb.getLocalizedString("school.not_want_after_school_care", "I do not want afterschool care")), 1, row++);
 			}
 			table.setHeight(row++, 5);
 		}
 
 		// School choice message link
 		table.mergeCells(1, row, 5, row);
 		table.add(getHeader(iwrb.getLocalizedString("school.language_choice", "Choice of language")), 1, row);
 		table.add(Text.getNonBrakingSpace(), 1, row);
 		Link msgLink = new Link(this.getInformationIcon(localize("school_choice.form_message_link_text", "School choice message")));
 		msgLink.setToOpenAlert(localize("school_choice_form_message.message", "Localized School choice message ... "));
 		table.add(msgLink, 1, row++);
 
 		CheckBox langChecked = getCheckBox(prmNativeLangIsChecked, Boolean.TRUE.toString());
 		table.add(langChecked, 1, row);
 		table.add(getSmallHeader(Text.getNonBrakingSpace() + iwrb.getLocalizedString("school.native_lang_prefix", "I would like")), 1, row);
 		table.add(Text.getNonBrakingSpace(), 1, row);
 		table.add(getNativeLanguagesDropdown(), 1, row);
 		table.mergeCells(1, row, 5, row);
 
 		table.add(new HiddenInput(prmCustodiansAgree, String.valueOf(showAgree)), 1, row);
 
 		return table;
 	}
 
 	private DropdownMenu getDropdown(String name, String type, String area, String school, int index, String firstElement) {
 		DropdownMenu menu = (DropdownMenu) getStyledInterface(new DropdownMenu(name));
 		menu.addMenuElementFirst("-1", firstElement);
 		menu.setOnChange(getFilterCallerScript(type, area, school, index, false));
 		return menu;
 	}
 
 	private DropdownMenu getNativeLanguagesDropdown() {
 		DropdownMenu drop = (DropdownMenu) getStyledInterface(new DropdownMenu(prmNativeLang));
 		drop.addMenuElement("-1", localize("school.drp_chose_native_lang", "- Chose languge -"));
 		try {
 			Collection langs = getICLanguageHome().findAll();
 			if (langs != null) {
 				for (Iterator iter = langs.iterator(); iter.hasNext();) {
 					ICLanguage aLang = (ICLanguage) iter.next();
 					int langPK = ((Integer) aLang.getPrimaryKey()).intValue();
 					drop.addMenuElement(langPK, aLang.getName());
 				}
 			}
 		}
 		catch (RemoteException re) {
 			re.printStackTrace();
 		}
 		catch (FinderException fe) {
 
 		}
 		return drop;
 	}
 
 	private PresentationObject getMessagePart() {
 		Table table = new Table(1, 3);
 		table.setWidth(300);
 		table.setCellpadding(2);
 		table.setCellspacing(0);
 		table.setBorder(0);
 		SubmitButton button = (SubmitButton) getButton(new SubmitButton("submit", localize("school_choice.ready", "Ready")));
 		table.setHeight(1, 3, 50);
 		table.setVerticalAlignment(1, 3, Table.VERTICAL_ALIGN_BOTTOM);
 		table.setAlignment(1, 3, Table.HORIZONTAL_ALIGN_RIGHT);
 
 		button.setOnSubmitFunction("checkApplication", getSchoolCheckScript());
 		myForm.setToDisableOnSubmit(button, true);
 
 		TextArea message = (TextArea) getStyledInterface(new TextArea(prmMessage));
 		message.setRows(6);
 		message.setColumns(65);
 		if (valMessage != null) message.setValue(valMessage);
 
 		if (schoolChange)
 			table.add(getSmallHeader(localize("school.application_change_required_reason", "Reason for schoolchange (Required)")), 1, 1);
 		else
 			table.add(getSmallHeader(localize("school.application_extra_info", "Extra info")), 1, 1);
 		table.add(message, 1, 2);
 		table.add(button, 1, 3);
 		return table;
 	}
 
 	private Collection getSchoolTypes(String category) {
 		try {
 			return schCommBiz.getSchoolBusiness().findAllSchoolTypesInCategory(category);
 		}
 		catch (Exception ex) {
 
 		}
 		return null;
 	}
 
 	private Collection getSchoolYears() {
 		Collection tmpVec;
 		Vector schYears = null;
 		schYears = new Vector();
 		try {
 			// Add school years for school type "Forskola"
 			SchoolType preSch = schCommBiz.getSchoolBusiness().getSchoolTypeHome().findByTypeKey("sch_type.school_type_forskoleklass");
 			int preID = ((Integer) preSch.getPrimaryKey()).intValue();
 			tmpVec = schCommBiz.getSchoolBusiness().findAllSchoolYearsBySchoolType(preID);
 
 			for (Iterator iter = tmpVec.iterator(); iter.hasNext();) {
 				SchoolYear element = (SchoolYear) iter.next();
 				schYears.add(element);
 			}
 		}
 		catch (Exception e) {
 			logWarning("Error loading school years for school type pre school class");
 			log(e);
 		}
 
 		try {
 			// Add school years for school type "Grundskola"
 			SchoolType elemSch = schCommBiz.getSchoolBusiness().getSchoolTypeHome().findByTypeKey("sch_type.school_type_grundskola");
 			int elemID = ((Integer) elemSch.getPrimaryKey()).intValue();
 			tmpVec = schCommBiz.getSchoolBusiness().findAllSchoolYearsBySchoolType(elemID);
 			for (Iterator iter = tmpVec.iterator(); iter.hasNext();) {
 				SchoolYear element = (SchoolYear) iter.next();
 				schYears.add(element);
 			}
 		}
 		catch (Exception e) {
 			logWarning("Error loading school years for school type elementary school");
 			log(e);
 		}
 
 		return schYears;
 	}
 
 	private Collection getSchoolAreasWithType(int type) {
 		try {
 			return schCommBiz.getSchoolBusiness().findAllSchoolAreasByType(type);
 		}
 		catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return null;
 	}
 
 	private Collection getSchoolByAreaAndType(IWContext iwc, int area, int type) {
 		try {
 			SchoolBusiness sBuiz = (SchoolBusiness) IBOLookup.getServiceInstance(iwc, SchoolBusiness.class);
 			Collection c = sBuiz.findAllSchoolsByAreaAndType(area, type);
 			return c;
 
 		}
 		catch (RemoteException ex) {
 			ex.printStackTrace();
 		}
 		return null;
 	}
 
 	private String getFilterCallerScript(String typeName, String areaName, String schoolName, int Type, boolean showAll) {
 		StringBuffer script = new StringBuffer("changeFilter(");
 		script.append(Type);
 		script.append(",");
 		script.append("findObj('").append(typeName).append("')");
 		script.append(",");
 		script.append("findObj('").append(areaName).append("')");
 		script.append(",");
 		script.append("findObj('").append(schoolName).append("')");
 		script.append(",");
 		script.append(showAll);
 		script.append(")");
 		return script.toString();
 	}
 
 	private String getInitFilterCallerScript(String typeName, String areaName, String schoolName, int typeSel, int areaSel, int schoolSel, boolean showAll) {
 		StringBuffer script = new StringBuffer("initFilter(");
 		script.append("findObj('").append(typeName).append("')");
 		script.append(",");
 		script.append("findObj('").append(areaName).append("')");
 		script.append(",");
 		script.append("findObj('").append(schoolName).append("')");
 		script.append(",");
 		script.append(typeSel);
 		script.append(",");
 		script.append(areaSel);
 		script.append(",");
 		script.append(schoolSel);
 		script.append(",");
 		script.append(showAll);
 		script.append(")");
 		return script.toString();
 	}
 
 	private String getFilterScript() {
 		StringBuffer s = new StringBuffer();
 		s.append("function changeFilter(index,type,area,school,showAll){").append(" \n\t");
 		s.append("changeFilter2(index,type,area,school,-1,showAll);").append("\n").append("}");
 		return s.toString();
 	}
 
 	private String getFilterScript2(IWContext iwc) throws java.rmi.RemoteException {
 		StringBuffer s = new StringBuffer();
 		s.append("function changeFilter2(index,type,area,school,selectedValue,showAll){").append(" \n\t");
 		s.append("var typeSelect = type;").append(" \n\t");
 		s.append("var areaSelect = area;").append(" \n\t");
 		s.append("var schoolSelect = school;").append(" \n\t");
 		s.append("var selectedType = typeSelect.options[typeSelect.selectedIndex].value;").append(" \n\t");
 		s.append("var selected = 0;").append(" \n\t");
 		s.append("if(index == 1){").append(" \n\t\t");
 		s.append("selected = typeSelect.options[typeSelect.selectedIndex].value;").append("\n\t\t");
 		s.append("areaSelect.options.length = 0;").append("\n\t\t");
 		s.append("schoolSelect.options.length = 0;").append("\n\t\t");
 		s.append("if (selectedType == -2)\n\t\t");
 		s.append("\tareaSelect.options[areaSelect.options.length] = new Option(\"");
 		s.append("\",\"-1\",true,true);").append("\n\t\t");
 		s.append("else\n\t\t");
 		s.append("\tareaSelect.options[areaSelect.options.length] = new Option(\"");
 		s.append(iwrb.getLocalizedString("choose_area", "Choose Area")).append("\",\"-1\",true,true);").append("\n\t\t");
 		s.append("}else if(index == 2){").append(" \n\t\t");
 		s.append("selected = areaSelect.options[areaSelect.selectedIndex].value;").append("\n\t\t");
 		s.append("if (selectedValue > -1) \n\t\t\tselected = selectedValue;").append("\n\t\t");
 		s.append("schoolSelect.options.length = 0;").append("\n\t\t");
 		s.append("if (selectedType == -2)\n\t\t");
 		s.append("\tschoolSelect.options[schoolSelect.options.length] = new Option(\"");
 		s.append("\",\"-1\",true,true);").append("\n\t\t");
 		s.append("else\n\t\t");
 		s.append("\tschoolSelect.options[schoolSelect.options.length] = new Option(\"");
 		s.append(iwrb.getLocalizedString("choose_school", "Choose School")).append("\",\"-1\",true,true);").append("\n\t");
 		s.append("} else if(index == 3){").append("\n\t\t");
 		s.append("selected = schoolSelect.options[schoolSelect.selectedIndex].value;").append("\n\t");
 		s.append("if (selectedValue > -1) \n\t\t\tselected = selectedValue;").append("\n\t\t");
 		s.append("}").append("\n\t\t\t");
 
 		// Data Filling ::
 
 		StringBuffer t = new StringBuffer("if(index==1){\n\t");
 		StringBuffer a = new StringBuffer("else if(index==2){\n\t");
 		StringBuffer c = new StringBuffer("else if(index==3){\n\t");
 
 		schoolsByType = new HashMap();
 
 		Collection Types = this.schoolTypes;
 		if (Types != null && !Types.isEmpty()) {
 			Iterator iter = Types.iterator();
 			SchoolType type;
 			SchoolArea area;
 			School school;
 			Collection areas;
 			Collection schools;
 			Collection schoolsInType;
 
 			SchoolYear yearAppliedFor = null;
 			if (schoolYear != null) {
 				if (this._useOngoingSeason) {
 					yearAppliedFor = schoolYear;
 				}
 				else {
 					yearAppliedFor = schCommBiz.getNextSchoolYear(schoolYear);
 				}
 			}
 			// iterate through schooltypes
 			while (iter.hasNext()) {
 				type = (SchoolType) iter.next();
 				//if (age.getYears() <= type.getMaxSchoolAge()) {
 
 				Integer tPK = (Integer) type.getPrimaryKey();
 				//System.err.println("checking type "+tPK.toString());
 				areas = getSchoolAreasWithType(tPK.intValue());
 				if (areas != null && !areas.isEmpty()) {
 					Iterator iter2 = areas.iterator();
 					t.append("if(selected == \"").append(tPK.toString()).append("\"){").append("\n\t\t");
 
 					Hashtable aHash = new Hashtable();
 
 					schoolsInType = new ArrayList();
 
 					// iterate through areas whithin types
 					while (iter2.hasNext()) {
 						area = (SchoolArea) iter2.next();
 						Integer aPK = (Integer) area.getPrimaryKey();
 						// System.err.println("checking area "+aPK.toString());
 						if (!aHash.containsKey(aPK)) {
 							aHash.put(aPK, aPK);
 							schools = getSchoolByAreaAndType(iwc, aPK.intValue(), tPK.intValue());
 							if (schools != null) {
 								schoolsInType.addAll(schools);
 								Iterator iter3 = schools.iterator();
 								a.append("if(selected == \"").append(aPK.toString()).append("\" && selectedType == \"").append(tPK.toString()).append("\"){").append("\n\t\t");
 								Hashtable hash = new Hashtable();
 								// iterator through schools whithin area and
 								// type
 								while (iter3.hasNext()) {
 									school = (School) iter3.next();
 									boolean hasRelation = true;
 									if (yearAppliedFor != null) {
 										hasRelation = schBuiz.getSchoolBusiness().hasSchoolRelationToYear(school, yearAppliedFor);
 									}
 									String pk = school.getPrimaryKey().toString();
 									//System.err.println("checking school
 									// "+pk.toString());
 									boolean invisibleForCitizen = false;
 									invisibleForCitizen = school.getInvisibleForCitizen();
 									if (!hash.containsKey(pk) && !invisibleForCitizen) {
 										if (!hasRelation) a.append("if (showAll == true)\n\t\t\t");
 										a.append("schoolSelect.options[schoolSelect.options.length] = new Option(\"");
 										a.append(school.getSchoolName()).append("\",\"");
 										a.append(pk).append("\");\n\t\t");
 										hash.put(pk, pk);
 									}
 
 								}
 								a.append("}\n\t\t");
 							}
 						}
 						else {
 							System.err.println("shools empty");
 						}
 						t.append("areaSelect.options[areaSelect.options.length] = new Option(\"");
 						t.append(area.getSchoolAreaName()).append("\",\"");
 						t.append(area.getPrimaryKey().toString()).append("\");").append("\n\t\t");
 
 					}
 					t.append("}\n\t");
 					schoolsByType.put(tPK, schoolsInType);
 				}
 				else {
 					System.err.println("areas empty");
 					schoolsByType.put(tPK, new ArrayList());
 				}
 			}
 		}
 		else
 			System.err.println("types empty");
 
 		s.append("\n\n");
 
 		t.append("\n\t }");
 		a.append("\n\t }");
 		c.append("\n\t }");
 
 		s.append(t.toString());
 		s.append(a.toString());
 		s.append(c.toString());
 		s.append("\n}");
 
 		return s.toString();
 	}
 
 	public String getInitFilterScript() {
 		StringBuffer s = new StringBuffer();
 		s.append("function initFilter(type,area,school,type_sel,area_sel,school_sel,showAll){ \n  ");
 		s.append("changeFilter2(1,type,area,school,type_sel,showAll); \n  ");
 		s.append("setSelected(type,type_sel); \n  ");
 		s.append("changeFilter2(2,type,area,school,area_sel,showAll); \n  ");
 		s.append("setSelected(area,area_sel); \n  ");
 		s.append("changeFilter2(3,type,area,school,school_sel,showAll); \n  ");
 		s.append("setSelected(school,school_sel); \n  ");
 		s.append("}");
 		return s.toString();
 	}
 
 	public String getSetSelectedScript() {
 		StringBuffer s = new StringBuffer();
 		s.append("function setSelected(input,selectedValue) { \n");
 		s.append("\tif (input.length > 1) { \n");
 		s.append("\t\tfor (var a = 0; a < input.length; a++) { \n");
 		s.append("\t\t\tif (input.options[a].value == selectedValue) \n");
 		s.append("\t\t\t\tinput.options[a].selected = true; \n");
 		s.append("\t\t} \n ");
 		s.append("\t} \n}");
 		return s.toString();
 	}
 
 	public String getSchoolCheckScript() {
 		StringBuffer s = new StringBuffer();
 		s.append("\nfunction checkApplication(){\n\t");
 
 		//s.append("\n\t\t alert('").append("checking choices").append("');");
 		//if (!this.hasPreviousSchool && !hasChosen)
 		//s.append("\n\t var currSchool =
 		// ").append("findObj('").append(prmPreSchool).append("');");
 		s.append("\n\t var gradeDrop = ").append("findObj('").append(prmYear).append("');");
 		s.append("\n\t var dropOne = ").append("findObj('").append(prmFirstSchool).append("');");
 
 		if (!schoolChange) {
 			s.append("\n\t var dropTwo = ").append("findObj('").append(prmSecondSchool).append("');");
 			s.append("\n\t var dropThree = ").append("findObj('").append(prmThirdSchool).append("');");
 		}
 		//if (!this.hasPreviousSchool && !hasChosen)
 		//s.append("\n\t var gradeDrop =
 		// ").append("findObj('").append(prmPreGrade).append("');");
 		s.append("\n\t var dropNativeLang = ").append("findObj('").append(prmNativeLang).append("');");
 		s.append("\n\t var checkNativeLang = ").append("findObj('").append(prmNativeLangIsChecked).append("');");
 
 		s.append("\n\n\t if (dropNativeLang.options[dropNativeLang.selectedIndex].value > 0 && checkNativeLang.checked == false){");
 		s.append("\n\t\t alert('" + localize("school.must_fill_native_language", "You must check the checkbox if you want native language ") + "');");
 		s.append("\n\t\t return false; \n}");
 
 		if (hasLanguageSelection) s.append("\n\t var languageDrop = ").append("findObj('").append(prmLanguage).append("');");
 
 		s.append("\n\t var one = 0;");
 		if (!schoolChange) {
 			s.append("\n\t var two = 0;");
 			s.append("\n\t var three = 0;");
 		}
 		s.append("\n\t var year = 0;");
 		s.append("\n\t var school = 0;");
 
 		s.append("\n\n\t if (gradeDrop.selectedIndex > 0) grade = gradeDrop.options[gradeDrop.selectedIndex].value;");
 		s.append("\n\n\t if (dropOne.selectedIndex > 0) one = dropOne.options[dropOne.selectedIndex].value;");
 		if (!schoolChange) {
 			s.append("\n\t if (dropTwo.selectedIndex > 0) two = dropTwo.options[dropTwo.selectedIndex].value;");
 			s.append("\n\t if (dropThree.selectedIndex > 0) three = dropThree.options[dropThree.selectedIndex].value;");
 		}
 
 		if (hasLanguageSelection) {
 			s.append("\n\n\t if (languageDrop.options[languageDrop.selectedIndex].value == -1) {");
 			s.append("\n\t\t alert('" + localize("school.must_fill_language", "You must select a language choice") + "');");
 			s.append("\n\t\t return false; \n}");
 		}
 		//if (!this.hasPreviousSchool)
 		//s.append("\n\t if (gradeDrop.selectedIndex > 0) year =
 		// gradeDrop.options?gradeDrop.options[gradeDrop.selectedIndex].value:document.sch_app_the_frm.elements['"
 		// + prmPreGrade + "'].value;");
 		//if (!this.hasPreviousSchool)
 		//s.append("\n\t if (currSchool.selectedIndex > 0) school =
 		// currSchool.options?currSchool.options[currSchool.selectedIndex].value:document.sch_app_the_frm.elements['"
 		// + prmPreSchool + "'].value;");
 
 		// current school check
 		/*
 		 * if (!this.hasPreviousSchool) { s.append("\n\n\t if(school <= 0){");
 		 * String msg1 =
 		 * iwrb.getLocalizedString("school_choice.must_set_current_school", "You
 		 * must provide current shool"); s.append("\n\t\t\t
 		 * alert('").append(msg1).append("');"); s.append("\n\t\t return false;");
 		 * s.append("\n\t\t "); s.append("\n\t }");
 		 */
 
 		// year check
 		/*
 		 * s.append("\n\t if(year <= 0 && school > 0){"); String msg2 =
 		 * iwrb.getLocalizedString("school_choice.must_set_grade", "You must provide
 		 * current shool year"); s.append("\n\t\t\t
 		 * alert('").append(msg2).append("');"); s.append("\n\t\t return false;");
 		 * s.append("\n\t\t ");
 		 */
 
 		// schoolchoices checked
 		if (!schoolChange) {
 			s.append("\n\t if(one > 0 && two > 0 && three > 0){");
 			s.append("\n\t if(one == two || two == three || three == one){");
 			String msg = iwrb.getLocalizedString("school_school.must_not_be_the_same", "Please do not choose the same school more than once");
 			s.append("\n\t\t\t alert('").append(msg).append("');");
 			s.append("\n\t\t\t return false;");
 			s.append("\n\t\t }");
 			s.append("\n\t }");
 			s.append("\n\t else{");
 			msg = iwrb.getLocalizedString("school_school.must_fill_out", "Please fill out all choices");
 			s.append("\n\t\t alert('").append(msg).append("');");
 			s.append("\n\t\t return false;");
 			s.append("\n\t }");
 		}
 		else {
 			s.append("\n\t if (one <= 0) {");
 			String msg = iwrb.getLocalizedString("school_school.must_fill_out", "Please fill out all choices");
 			s.append("\n\t\t alert('").append(msg).append("');");
 			s.append("\n\t\t return false;");
 			s.append("\n\t }");
 			s.append("\n\t var reason =  findObj('").append(prmMessage).append("');");
 			s.append("\n\t if(reason.value.length<=10){");
 			msg = iwrb.getLocalizedString("school_school.change_must_give_reason", "You must give a reason for  the change !");
 			s.append("\n\t\t alert('").append(msg).append("');");
 			s.append("\n\t\t return false;");
 			s.append("\n\t }");
 		}
 		s.append("\n\t if (grade <= 0) {");
 		String msg = iwrb.getLocalizedString("school_school.must_fill_out_year", "Please select a year to apply to");
 		s.append("\n\t\t alert('").append(msg).append("');");
 		s.append("\n\t\t return false;");
 		s.append("\n\t }");
 		//s.append("\n\t\t
 		// findObj('").append("submit").append("').disabled=true;");
 		s.append("\n\t document.body.style.cursor = 'wait'");
 		s.append("\n\t return true;");
 		s.append("\n}\n");
 		return s.toString();
 	}
 
 	/*
 	 * We hard code the schools for which an alert message is shown, until further
 	 * notice, since this applies only for the nacka24 site.
 	 */
 	private String getAlertIfSportsOrMusicSchool() {
 		StringBuffer sb = new StringBuffer();
 		sb.append("function alertIfSportsOrMusicSchool(thisObject) {\n").append("\t var schools = new Array('Eklidens skola - Idrottsklass', 'Jrla skola - Musikklass');\n").append("\t for (loop=0; loop < schools.length; loop++) {\n").append("\t\t tmpSchool = schools[loop];").append("\t\t if (tmpSchool == thisObject.options[thisObject.selectedIndex].text) {\n").append("\t\t\t alert('").append(iwrb.getLocalizedString("school_choice.alert_if_sports_or_music_school_msg", "Information: An approved application test is required for")).append("' + ' ' + tmpSchool);\n").append("\t\t\t return false;\n").append("\t\t }\n").append("\t }\n").append("}\n");
 
 		return sb.toString();
 	}
 
 	private boolean[] checkCanApply(IWContext iwc) throws RemoteException {
 
 		if (_useOngoingSeason) {
 			boolean[] canApply = { true, true, true};
 			return canApply;
 		}
 
 		boolean[] checkCanApply = { false, false, false};
 		try {
 			SchoolSeason season = schBuiz.getCurrentSeason();
 			if (season != null) {
 				IWPropertyList properties = iwc.getSystemProperties().getProperties("school_properties");
 				String choiceStart = properties.getProperty("choice_start_date");
 				String choiceEnd = properties.getProperty("choice_end_date");
 				String choiceRed = properties.getProperty("choice_critical_date");
 				String changeStart = properties.getProperty("school_change_start");
 
 				IWTimestamp seasonStart = new IWTimestamp(season.getSchoolSeasonStart());
 				IWTimestamp dateNow = new IWTimestamp();
 				if (choiceStart != null && choiceEnd != null && choiceRed != null) {
 					SchoolChoiceBusiness business = (SchoolChoiceBusiness) IBOLookup.getServiceInstance(iwc, SchoolChoiceBusiness.class);
 					IWTimestamp start = business.getSchoolChoiceStartDate();
 					IWTimestamp end = business.getSchoolChoiceEndDate();
 					IWTimestamp red = business.getSchoolChoiceCriticalDate();
 
 					if (dateNow.isBetween(start, end))
 						checkCanApply[0] = true;
 					else
 						checkCanApply[0] = false;
 
 					if (red.isEarlierThan(dateNow)) checkCanApply[1] = true;
 
 					//temporary, for testing only...
 					if (_isForTesting) {
 						checkCanApply[0] = true;
 						checkCanApply[1] = false;
 					}
 				}
 				if (changeStart != null) {
 					IWTimestamp change = new IWTimestamp(seasonStart);
 					change.setDay(Integer.parseInt(changeStart.substring(0, 2)));
 					change.setMonth(Integer.parseInt(changeStart.substring(3)));
 					if (dateNow.isLaterThan(change)) {
 						checkCanApply[2] = true;
 					}
 
 					if (dateNow.getYear() <= 2002) checkCanApply[2] = true;
 
 					if (schoolChange) checkCanApply[0] = true;
 				}
 			}
 			return checkCanApply;
 		}
 		catch (FinderException fe) {
 			return checkCanApply;
 		}
 	}
 
 	public void setAsAdminQuickChoice(boolean quick) {
 		this.quickAdmin = quick;
 	}
 
 	public void setAsSchoolChange(boolean change) {
 		this.schoolChange = change;
 	}
 
 	/**
 	 * Sets the childcarePage.
 	 * 
 	 * @param childcarePage
 	 *          The childcarePage to set
 	 */
 	public void setChildcarePage(ICPage childcarePage) {
 		afterSchoolPageID = (Integer) childcarePage.getPrimaryKey();
 	}
 
 	/**
 	 * Sets the childcarePage.
 	 * 
 	 * @param childcarePage
 	 *          The childcarePage to set
 	 */
 	public void setAfterschoolPage(ICPage afterSchoolPage) {
 		this.afterSchoolPageID = (Integer) afterSchoolPage.getPrimaryKey();
 	}
 
 	/**
 	 * Sets the childcarePage ID.
 	 * 
 	 * @param childcarePageID
 	 *          The childcarePage to set
 	 */
 	public void setCheckPage(ICPage checkPage) {
 		this.checkPageID = (Integer) checkPage.getPrimaryKey();
 	}
 
 	private UserHomeLink getUserHomePageLink() {
 		return new UserHomeLink();
 	}
 
 	private CheckBusiness getCheckBusiness(IWApplicationContext iwc) {
 		try {
 			return (CheckBusiness) IBOLookup.getServiceInstance(iwc, CheckBusiness.class);
 		}
 		catch (RemoteException e) {
 			throw new IBORuntimeException(e.getMessage());
 		}
 	}
 
 	private ICLanguageHome getICLanguageHome() throws RemoteException {
 		return (ICLanguageHome) IDOLookup.getHome(ICLanguage.class);
 	}
 
 	private ChildCareSession getChildCareSession(IWContext iwc) {
 		try {
 			return (ChildCareSession) IBOLookup.getSessionInstance(iwc, ChildCareSession.class);
 		}
 		catch (IBOLookupException e) {
 			throw new IBORuntimeException(e.getMessage());
 		}
 	}
 
 	/**
 	 * @param isForTesting
 	 *          The isForTesting to set.
 	 */
 	public void setForTesting(boolean isForTesting) {
 		this._isForTesting = isForTesting;
 	}
 
 	/**
 	 * @param showChoiceMessage
 	 *          The showChoiceMessage to set.
 	 */
 	public void setShowExtraChoiceMessage(boolean showChoiceMessage) {
 		this._showChoiceMessage = showChoiceMessage;
 	}
 
 	/**
 	 * @param forwardToCheckPage
 	 *          The forwardToCheckPage to set.
 	 */
 	public void setForwardToCheckPage(boolean forwardToCheckPage) {
 		this._forwardToCheckPage = forwardToCheckPage;
 	}
 
 	public void setMaxAfterCareAge(int age) {
 		_maxAge = age;
 	}
 
 	/**
 	 * @param useCheckBoxForAfterSchoolCare
 	 *          The useCheckBoxForAfterSchoolCare to set.
 	 */
 	public void setUseCheckBoxForAfterSchoolCare(boolean useCheckBoxForAfterSchoolCare) {
 		this._useCheckBoxForAfterSchoolCare = useCheckBoxForAfterSchoolCare;
 	}
 
 	/**
 	 * @param showChildCareTypes
 	 *          The showChildCareTypes to set.
 	 */
 	public void setShowChildCareTypes(boolean showChildCareTypes) {
 		this._showChildCareTypes = showChildCareTypes;
 	}
 }
