 package gov.nih.nci.cadsr.cdecurate.tool;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Vector;
 
 import gov.nih.nci.cadsr.cdecurate.ui.AltNamesDefsSession;
 import gov.nih.nci.cadsr.cdecurate.util.AdministeredItemUtil;
 import gov.nih.nci.cadsr.cdecurate.util.DataManager;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import oracle.net.aso.e;
 
 public class DataElementConceptServlet extends CurationServlet {
 
 	public DataElementConceptServlet() {		
 	}
 
 	public DataElementConceptServlet(HttpServletRequest req, HttpServletResponse res,
 			ServletContext sc) {
 		super(req, res, sc);
 	}
 
 	public void execute(ACRequestTypes reqType) throws Exception {	
 
 		switch (reqType){
 		case newDECFromMenu:
 			doOpenCreateNewPages(); 
 			break;
 		case newDECfromForm:
 			doCreateDECActions();
 			break;
 		case editDEC:
 			doEditDECActions();
 			break;
 		case createNewDEC:
 			doOpenCreateDECPage();
 			break;
 		case validateDECFromForm:
 			doInsertDEC();
 			break;
 		case viewDE_CONCEPT:
 			doOpenViewPage();
 			break;
 
 		}
 	}	
 
 	/**
 	 * The doOpenCreateNewPages method will set some session attributes then forward the request to a Create page.
 	 * Called from 'service' method where reqType is 'newDEFromMenu', 'newDECFromMenu', 'newVDFromMenu' Sets some
 	 * initial session attributes. Calls 'getAC.getACList' to get the Data list from the database for the selected
 	 * context. Sets session Bean and forwards the create page for the selected component.
 	 *
 	 * @throws Exception
 	 */
 	private void doOpenCreateNewPages() throws Exception
 	{
 		HttpSession session = m_classReq.getSession();
 		clearSessionAttributes(m_classReq, m_classRes);
 		this.clearBuildingBlockSessionAttributes(m_classReq, m_classRes);
 		String context = (String) session.getAttribute("sDefaultContext"); // from Login.jsp
 
 		DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, "nothing");
 		DataManager.setAttribute(session, "DDEAction", "nothing"); // reset from "CreateNewDEFComp"
 
 		DataManager.setAttribute(session, "sCDEAction", "nothing");
 		DataManager.setAttribute(session, "VDPageAction", "nothing");
 		DataManager.setAttribute(session, "DECPageAction", "nothing");
 		DataManager.setAttribute(session, "sDefaultContext", context);
 		this.clearCreateSessionAttributes(m_classReq, m_classRes); // clear some session attributes
 
 		DataManager.setAttribute(session, "originAction", "NewDECFromMenu");
 		DataManager.setAttribute(session, "LastMenuButtonPressed", "CreateDEC");
 		DEC_Bean m_DEC = new DEC_Bean();
 		m_DEC.setDEC_ASL_NAME("DRAFT NEW");
 		m_DEC.setAC_PREF_NAME_TYPE("SYS");
 		DataManager.setAttribute(session, "m_DEC", m_DEC);
 		DEC_Bean oldDEC = new DEC_Bean();
 		oldDEC.setDEC_ASL_NAME("DRAFT NEW");
 		oldDEC.setAC_PREF_NAME_TYPE("SYS");
 		DataManager.setAttribute(session, "oldDECBean", oldDEC);
 		EVS_Bean m_OC = new EVS_Bean();
 		DataManager.setAttribute(session, "m_OC", m_OC);
 		EVS_Bean m_PC = new EVS_Bean();
 		DataManager.setAttribute(session, "m_PC", m_PC);
 		EVS_Bean m_OCQ = new EVS_Bean();
 		DataManager.setAttribute(session, "m_OCQ", m_OCQ);
 		EVS_Bean m_PCQ = new EVS_Bean();
 		DataManager.setAttribute(session, "m_PCQ", m_PCQ);
 		DataManager.setAttribute(session, "selPropRow", "");
 		DataManager.setAttribute(session, "selPropQRow", "");
 		DataManager.setAttribute(session, "selObjQRow", "");
 		DataManager.setAttribute(session, "selObjRow", "");
 		ForwardJSP(m_classReq, m_classRes, "/CreateDECPage.jsp");
 
 	} // end of doOpenCreateNewPages
 
 	/**
 	 * The doCreateDECActions method handles CreateDEC or EditDEC actions of the request. Called from 'service' method
 	 * where reqType is 'newDECfromForm' Calls 'doValidateDEC' if the action is Validate or submit. Calls
 	 * 'doSuggestionDE' if the action is open EVS Window.
 	 *
 	 * @throws Exception
 	 */
 	private void doCreateDECActions() throws Exception
 	{
 		HttpSession session = m_classReq.getSession();
 		String sMenuAction = (String) m_classReq.getParameter("MenuAction");
 		if (sMenuAction != null)
 			DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, sMenuAction);
 		String sAction = (String) m_classReq.getParameter("pageAction");
 		DataManager.setAttribute(session, "DECPageAction", sAction); // store the page action in attribute
 		String sSubAction = (String) m_classReq.getParameter("DECAction");
 		DataManager.setAttribute(session, "DECAction", sSubAction);
 		String sOriginAction = (String) session.getAttribute("originAction");
 		/* if (sAction.equals("changeContext"))
             doChangeContext(m_classReq, m_classRes, "dec");
         else*/ if (sAction.equals("submit"))
         	doSubmitDEC();
         else if (sAction.equals("validate"))
         	doValidateDEC();
         else if (sAction.equals("suggestion"))
         	doSuggestionDE(m_classReq, m_classRes);
         else if (sAction.equals("UseSelection"))
         {
         	String nameAction = "newName";
         	if (sMenuAction.equals("NewDECTemplate") || sMenuAction.equals("NewDECVersion"))
         		nameAction = "appendName";
         	doDECUseSelection(nameAction);
         	ForwardJSP(m_classReq, m_classRes, "/CreateDECPage.jsp");
         	return;
         }
         else if (sAction.equals("RemoveSelection"))
         {
         	doRemoveBuildingBlocks();
         	// re work on the naming if new one
         	DEC_Bean dec = (DEC_Bean) session.getAttribute("m_DEC");
         	EVS_Bean nullEVS = null;            
         	if (!sMenuAction.equals("NewDECTemplate") && !sMenuAction.equals("NewDECVersion"))
         		dec = (DEC_Bean) this.getACNames(nullEVS, "Search", dec); // change only abbr pref name
         	else
         		dec = (DEC_Bean) this.getACNames(nullEVS, "Remove", dec); // need to change the long name & def also
         	DataManager.setAttribute(session, "m_DEC", dec);
         	ForwardJSP(m_classReq, m_classRes, "/CreateDECPage.jsp");
         	return;
         }
         else if (sAction.equals("changeNameType"))
         {
         	this.doChangeDECNameType();
         	ForwardJSP(m_classReq, m_classRes, "/CreateDECPage.jsp");
         }
         else if (sAction.equals("Store Alternate Names") || sAction.equals("Store Reference Documents"))
         	this.doMarkACBeanForAltRef(m_classReq, m_classRes, "DataElementConcept", sAction, "createAC");
         // add, edit and remove contacts
         else if (sAction.equals("doContactUpd") || sAction.equals("removeContact"))
         {
         	DEC_Bean DECBean = (DEC_Bean) session.getAttribute("m_DEC");
         	// capture all page attributes
         	m_setAC.setDECValueFromPage(m_classReq, m_classRes, DECBean);
         	DECBean.setAC_CONTACTS(this.doContactACUpdates(m_classReq, sAction));
         	ForwardJSP(m_classReq, m_classRes, "/CreateDECPage.jsp");
         }
         else if (sAction.equals("clearBoxes"))
         {
         	DEC_Bean DECBean = (DEC_Bean) session.getAttribute("oldDECBean");
         	this.clearBuildingBlockSessionAttributes(m_classReq, m_classRes);
         	String sDECID = DECBean.getDEC_DEC_IDSEQ();
         	Vector vList = new Vector();
         	// get VD's attributes from the database again
         	GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, this);
         	if (sDECID != null && !sDECID.equals(""))
         		serAC.doDECSearch(sDECID, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", 0, "", "", "",
         				"", "", vList, "0");//===gf32398== added one more parameter regstatus
         	// forward editVD page with this bean
         	if (vList.size() > 0)
         	{
         		DECBean = (DEC_Bean) vList.elementAt(0);
         		DECBean = serAC.getDECAttributes(DECBean, sOriginAction, sMenuAction);
         	}
         	else
         		// new one
         	{
         		DECBean = new DEC_Bean();
         		DECBean.setDEC_ASL_NAME("DRAFT NEW");
         		DECBean.setAC_PREF_NAME_TYPE("SYS");
         	}
         	DEC_Bean pgBean = new DEC_Bean();
         	DataManager.setAttribute(session, "m_DEC", pgBean.cloneDEC_Bean(DECBean));
         	ForwardJSP(m_classReq, m_classRes, "/CreateDECPage.jsp");
         }
         // open the create DE page or search result page
         else if (sAction.equals("backToDE"))
         {
         	this.clearCreateSessionAttributes(m_classReq, m_classRes);
         	this.clearBuildingBlockSessionAttributes(m_classReq, m_classRes);
         	if (sMenuAction.equals("NewDECTemplate") || sMenuAction.equals("NewDECVersion"))
         	{
         		DEC_Bean DECBean = (DEC_Bean) session.getAttribute("m_DEC");
         		GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, this);
         		serAC.refreshData(m_classReq, m_classRes, null, DECBean, null, null, "Refresh", "");
         		ForwardJSP(m_classReq, m_classRes, "/SearchResultsPage.jsp");
         	}
         	else if (sOriginAction.equalsIgnoreCase("CreateNewDECfromEditDE"))
         		ForwardJSP(m_classReq, m_classRes, "/EditDEPage.jsp");
         	else
         		ForwardJSP(m_classReq, m_classRes, "/CreateDEPage.jsp");
         }
 	}
 
 	/**
 	 * The doEditDECActions method handles EditDEC actions of the request. Called from 'service' method where reqType is
 	 * 'EditDEC' Calls 'ValidateDEC' if the action is Validate or submit. Calls 'doSuggestionDEC' if the action is open
 	 * EVS Window.
 	 *
 	 * @throws Exception
 	 */
 	private void doEditDECActions() throws Exception
 	{
 		HttpSession session = m_classReq.getSession();
 		String sMenuAction = (String) m_classReq.getParameter("MenuAction");
 		if (sMenuAction != null)
 			DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, sMenuAction);
 		String sAction = (String) m_classReq.getParameter("pageAction");
 		DataManager.setAttribute(session, "DECPageAction", sAction); // store the page action in attribute
 		String sSubAction = (String) m_classReq.getParameter("DECAction");
 		DataManager.setAttribute(session, "DECAction", sSubAction);
 		String sButtonPressed = (String) session.getAttribute("LastMenuButtonPressed");
 		String sOriginAction = (String) session.getAttribute("originAction");
 		if (sAction.equals("submit"))
 			doSubmitDEC();
 		else if (sAction.equals("validate") && sOriginAction.equals("BlockEditDEC"))
 			doValidateDECBlockEdit();
 		else if (sAction.equals("validate"))
 			doValidateDEC();
 		else if (sAction.equals("suggestion"))
 			doSuggestionDE(m_classReq, m_classRes);
 		else if (sAction.equals("UseSelection"))
 		{
 			String nameAction = "appendName";
 			if (sOriginAction.equals("BlockEditDEC"))
 				nameAction = "blockName";
 			doDECUseSelection(nameAction);
 			ForwardJSP(m_classReq, m_classRes, "/EditDECPage.jsp");
 			return;
 		}
 		else if (sAction.equals("RemoveSelection"))
 		{
 			doRemoveBuildingBlocks();
 			// re work on the naming if new one
 			DEC_Bean dec = (DEC_Bean) session.getAttribute("m_DEC");
 			EVS_Bean nullEVS = null; 
 			dec = (DEC_Bean) this.getACNames(nullEVS, "Remove", dec); // need to change the long name & def also
 			DataManager.setAttribute(session, "m_DEC", dec);
 			ForwardJSP(m_classReq, m_classRes, "/EditDECPage.jsp");
 			return;
 		}
 		else if (sAction.equals("changeNameType"))
 		{
 			this.doChangeDECNameType();
 			ForwardJSP(m_classReq, m_classRes, "/EditDECPage.jsp");
 		}
 		else if (sAction.equals("Store Alternate Names") || sAction.equals("Store Reference Documents"))
 			this.doMarkACBeanForAltRef(m_classReq, m_classRes, "DataElementConcept", sAction, "editAC");
 		// add, edit and remove contacts
 		else if (sAction.equals("doContactUpd") || sAction.equals("removeContact"))
 		{
 			DEC_Bean DECBean = (DEC_Bean) session.getAttribute("m_DEC");
 			// capture all page attributes
 			m_setAC.setDECValueFromPage(m_classReq, m_classRes, DECBean);
 			DECBean.setAC_CONTACTS(this.doContactACUpdates(m_classReq, sAction));
 			ForwardJSP(m_classReq, m_classRes, "/EditDECPage.jsp");
 		}
 		else if (sAction.equals("clearBoxes"))
 		{
 			DEC_Bean DECBean = (DEC_Bean) session.getAttribute("oldDECBean");
 			this.clearBuildingBlockSessionAttributes(m_classReq, m_classRes);
 			String sDECID = DECBean.getDEC_DEC_IDSEQ();
 			Vector vList = new Vector();
 			// get VD's attributes from the database again
 			GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, this);
 			if (sDECID != null && !sDECID.equals(""))
 				serAC.doDECSearch(sDECID, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", 0, "", "", "",
 						"", "", vList, "0");//===gf32398== added one more parameter regstatus
 			if (vList.size() > 0)
 			{
 				DECBean = (DEC_Bean) vList.elementAt(0);
 				// logger.debug("cleared name " + DECBean.getDEC_PREFERRED_NAME());
 				DECBean = serAC.getDECAttributes(DECBean, sOriginAction, sMenuAction);
 			}
 			DEC_Bean pgBean = new DEC_Bean();
 			DataManager.setAttribute(session, "m_DEC", pgBean.cloneDEC_Bean(DECBean));
 			ForwardJSP(m_classReq, m_classRes, "/EditDECPage.jsp");
 		}
 		// open the create DE page or search result page
 		else if (sAction.equals("backToDE"))
 		{
 			this.clearBuildingBlockSessionAttributes(m_classReq, m_classRes); // clear session attributes
 			if (sOriginAction.equalsIgnoreCase("editDECfromDE"))
 				ForwardJSP(m_classReq, m_classRes, "/EditDEPage.jsp");
 			else if (sMenuAction.equalsIgnoreCase("editDEC") || sOriginAction.equalsIgnoreCase("BlockEditDEC")
 					|| sButtonPressed.equals("Search") || sOriginAction.equalsIgnoreCase("EditDEC"))
 			{
 				DEC_Bean DECBean = (DEC_Bean) session.getAttribute("m_DEC");
 				if (DECBean == null){
 					DECBean = new DEC_Bean();
 				}
 				GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, this);
 				serAC.refreshData(m_classReq, m_classRes, null, DECBean, null, null, "Refresh", "");
 				ForwardJSP(m_classReq, m_classRes, "/SearchResultsPage.jsp");
 			}
 			else
 				ForwardJSP(m_classReq, m_classRes, "/EditDECPage.jsp");
 		}
 	}
 
 	/**
 	 * changes the dec name type as selected
 	 *
 	 * @throws java.lang.Exception
 	 */
 	private void doChangeDECNameType() throws Exception
 	{
 		HttpSession session = m_classReq.getSession();
 		// get teh selected type from teh page
 		DEC_Bean pageDEC = (DEC_Bean) session.getAttribute("m_DEC");
 		m_setAC.setDECValueFromPage(m_classReq, m_classRes, pageDEC); // capture all other attributes
 		String sSysName = pageDEC.getAC_SYS_PREF_NAME();
 		String sAbbName = pageDEC.getAC_ABBR_PREF_NAME();
 		String sUsrName = pageDEC.getAC_USER_PREF_NAME();
 		String sNameType = (String) m_classReq.getParameter("rNameConv");
 		if (sNameType == null || sNameType.equals(""))
 			sNameType = "SYS"; // default
 		// logger.debug(sSysName + " name type " + sNameType);
 		// get the existing preferred name to make sure earlier typed one is saved in the user
 		String sPrefName = (String) m_classReq.getParameter("txtPreferredName");
 		if (sPrefName != null && !sPrefName.equals("") && !sPrefName.equals("(Generated by the System)")
 				&& !sPrefName.equals(sSysName) && !sPrefName.equals(sAbbName))
 			pageDEC.setAC_USER_PREF_NAME(sPrefName); // store typed one in de bean
 		// reset system generated or abbr accoring
 		if (sNameType.equals("SYS"))
 			pageDEC.setDEC_PREFERRED_NAME(sSysName);
 		else if (sNameType.equals("ABBR"))
 			pageDEC.setDEC_PREFERRED_NAME(sAbbName);
 		else if (sNameType.equals("USER"))
 			pageDEC.setDEC_PREFERRED_NAME(sUsrName);
 		// store the type in the bean
 		pageDEC.setAC_PREF_NAME_TYPE(sNameType);
 		// logger.debug(pageDEC.getAC_PREF_NAME_TYPE() + " pref " + pageDEC.getDEC_PREFERRED_NAME());
 		DataManager.setAttribute(session, "m_DEC", pageDEC);
 	}
 
 	/**
 	 * Does open editDEC page action from DE page called from 'doEditDEActions' method. Calls
 	 * 'm_setAC.setDEValueFromPage' to store the DE bean for later use Using the DEC idseq, calls 'SerAC.search_DEC'
 	 * method to gets dec attributes to populate. stores DEC bean in session and opens editDEC page. goes back to editDE
 	 * page if any error.
 	 *
 	 * @throws Exception
 	 */
 	public void doOpenEditDECPage() throws Exception
 	{
 		HttpSession session = m_classReq.getSession();
 		DE_Bean m_DE = (DE_Bean) session.getAttribute("m_DE");
 		if (m_DE == null)
 			m_DE = new DE_Bean();
 		// store the de values in the session
 		m_setAC.setDEValueFromPage(m_classReq, m_classRes, m_DE);
 		DataManager.setAttribute(session, "m_DE", m_DE);
 		this.clearBuildingBlockSessionAttributes(m_classReq, m_classRes);
 		String sDEC_ID = null;
 		String sDECid[] = m_classReq.getParameterValues("selDEC");
 		if (sDECid != null)
 			sDEC_ID = sDECid[0];
 		// get the dec bean for this id
 		if (sDEC_ID != null)
 		{
 			Vector vList = new Vector();
 			GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, this);
 			serAC.doDECSearch(sDEC_ID, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", 0, "", "", "", "",
 					"", vList, "0");//===gf32398== added one more parameter regstatus
 			// forward editDEC page with this bean
 			if (vList.size() > 0)
 			{
 				for (int i = 0; i < vList.size(); i++)
 				{
 					DEC_Bean DECBean = new DEC_Bean();
 					DECBean = (DEC_Bean) vList.elementAt(i);
 					// check if the user has write permission
 					String contID = DECBean.getDEC_CONTE_IDSEQ();
 					String sUser = (String) session.getAttribute("Username");
 					GetACService getAC = new GetACService(m_classReq, m_classRes, this);
 					String hasPermit = getAC.hasPrivilege("Create", sUser, "dec", contID);
 					// forward to editDEC if has write permission
 					if (hasPermit.equals("Yes"))
 					{
 						DECBean = serAC.getDECAttributes(DECBean, "Edit", "Edit"); // get DEC other Attributes
 						// store the bean in the session attribute
 						DataManager.setAttribute(session, "m_DEC", DECBean);
 						DEC_Bean oldDEC = new DEC_Bean();
 						oldDEC = oldDEC.cloneDEC_Bean(DECBean);
 						DataManager.setAttribute(session, "oldDECBean", oldDEC);
 						ForwardJSP(m_classReq, m_classRes, "/EditDECPage.jsp"); // forward to editDEC page
 					}
 					// go back to editDE with message if no permission
 					else
 					{
 						DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, "No edit permission in "
 								+ DECBean.getDEC_CONTEXT_NAME() + " context");
 						ForwardJSP(m_classReq, m_classRes, "/EditDEPage.jsp"); // forward to editDE page
 					}
 					break;
 				}
 			}
 			// display error message and back to edit DE page
 			else
 				DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE,
 						"Unable to get Existing DEConcept attributes from the database");
 		}
 		// display error message and back to editDE page
 		else
 		{
 			DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, "Unable to get the DEConcept id from the page");
 			// forward the depage when error occurs
 			ForwardJSP(m_classReq, m_classRes, "/EditDEPage.jsp"); // forward to editDE page
 		}
 	}
 
 	/**
 	 * Called from doCreateDECActions. Calls 'setAC.setDECValueFromPage' to set the DEC data from the page. Calls
 	 * 'setAC.setValidatePageValuesDEC' to validate the data. Loops through the vector vValidate to check if everything
 	 * is valid and Calls 'doInsertDEC' to insert the data. If vector contains invalid fields, forwards to validation
 	 * page
 	 *
 	 * @throws Exception
 	 */
 	private void doSubmitDEC() throws Exception
 	{
 		HttpSession session = m_classReq.getSession();
 		DataManager.setAttribute(session, "sDECAction", "validate");
 		DEC_Bean m_DEC = (DEC_Bean) session.getAttribute("m_DEC");
 		if (m_DEC == null)
 			m_DEC = new DEC_Bean();
 		EVS_Bean m_OC = new EVS_Bean();
 		EVS_Bean m_PC = new EVS_Bean();
 		EVS_Bean m_OCQ = new EVS_Bean();
 		EVS_Bean m_PCQ = new EVS_Bean();
 		GetACService getAC = new GetACService(m_classReq, m_classRes, this);
 		m_setAC.setDECValueFromPage(m_classReq, m_classRes, m_DEC);
 		m_OC = (EVS_Bean) session.getAttribute("m_OC");
 		m_PC = (EVS_Bean) session.getAttribute("m_PC");
 		m_OCQ = (EVS_Bean) session.getAttribute("m_OCQ");
 		m_PCQ = (EVS_Bean) session.getAttribute("m_PCQ");
 		m_setAC.setValidatePageValuesDEC(m_classReq, m_classRes, m_DEC, m_OC, m_PC, getAC, m_OCQ, m_PCQ);
 		DataManager.setAttribute(session, "m_DEC", m_DEC);
 		boolean isValid = true;
 		Vector vValidate = new Vector();
 		vValidate = (Vector) m_classReq.getAttribute("vValidate");
 		if (vValidate == null)
 			isValid = false;
 		else
 		{
 			for (int i = 0; vValidate.size() > i; i = i + 3)
 			{
 				String sStat = (String) vValidate.elementAt(i + 2);
 				if (sStat.equals("Valid") == false)
 				{
 					isValid = false;
 				}
 			}
 		}
 		if (isValid == false)
 		{
 			ForwardJSP(m_classReq, m_classRes, "/ValidateDECPage.jsp");
 		}
 		else
 		{
 			doInsertDEC();
 		}
 	} // end of doSumitDE
 
 	/**
 	 * The doValidateDEC method gets the values from page the user filled out, validates the input, then forwards
 	 * results to the Validate page Called from 'doCreateDECActions' 'doSubmitDEC' method. Calls
 	 * 'setAC.setDECValueFromPage' to set the data from the page to the bean. Calls 'setAC.setValidatePageValuesDEC' to
 	 * validate the data. Stores 'm_DEC' bean in session. Forwards the page 'ValidateDECPage.jsp' with validation vector
 	 * to display.
 	 *
 	 * @throws Exception
 	 */
 	private void doValidateDEC() throws Exception
 	{
 		// System.err.println("in doValidateDEC");
 		HttpSession session = m_classReq.getSession();
 		// do below for versioning to check whether these two have changed
 		DataManager.setAttribute(session, "DECPageAction", "validate"); // store the page action in attribute
 		DEC_Bean m_DEC = (DEC_Bean) session.getAttribute("m_DEC");
 		String sOriginAction = (String)session.getAttribute("originAction");
 		if (m_DEC == null)
 			m_DEC = new DEC_Bean();
 		String oldOCIdseq = (String)session.getAttribute("oldOCIdseq");
 		String oldPropIdseq = (String)session.getAttribute("oldPropIdseq");
 		String checkValidityOC = "Yes";
 		String checkValidityProp = "Yes";
 		//begin GF30681
 		session.setAttribute("checkValidityOC", "Yes");
 		session.setAttribute("checkValidityProp", "Yes");
 		//end GF30681
 		EVS_Bean m_OC = new EVS_Bean();
 		EVS_Bean m_PC = new EVS_Bean();
 		EVS_Bean m_OCQ = new EVS_Bean();
 		EVS_Bean m_PCQ = new EVS_Bean();
 		GetACService getAC = new GetACService(m_classReq, m_classRes, this);
 		m_setAC.setDECValueFromPage(m_classReq, m_classRes, m_DEC);
 		if ( (m_DEC.getDEC_PROPL_NAME_PRIMARY()== null ||  m_DEC.getDEC_PROPL_NAME_PRIMARY()!= null && m_DEC.getDEC_PROPL_NAME_PRIMARY().equals("")) 
 				&& (m_DEC.getDEC_PROP_QUALIFIER_NAMES()==null || m_DEC.getDEC_PROP_QUALIFIER_NAMES()!=null && m_DEC.getDEC_PROP_QUALIFIER_NAMES().equals(""))){
 			checkValidityProp = "No"; 
 		}
 		if (sOriginAction!= null && !sOriginAction.equals("NewDECFromMenu")){
 			if (m_DEC.getDEC_OCL_IDSEQ() != null && !m_DEC.getDEC_OCL_IDSEQ().equals("") && m_DEC.getDEC_OCL_IDSEQ().equals(oldOCIdseq)){
 				checkValidityOC = "No";
 			}
 			if  (m_DEC.getDEC_PROPL_IDSEQ() != null && !m_DEC.getDEC_PROPL_IDSEQ().equals("") && m_DEC.getDEC_PROPL_IDSEQ().equals(oldPropIdseq)){
 				checkValidityProp = "No";
 			}
 		}
 		DataManager.setAttribute(session, "checkValidityOC", checkValidityOC);
 		DataManager.setAttribute(session, "checkValidityProp", checkValidityProp);
 		m_OC = (EVS_Bean) session.getAttribute("m_OC");
 		m_PC = (EVS_Bean) session.getAttribute("m_PC");
 		m_OCQ = (EVS_Bean) session.getAttribute("m_OCQ");
 		m_PCQ = (EVS_Bean) session.getAttribute("m_PCQ");
 		// System.err.println("in doValidateDEC call setValidate");
 		m_setAC.setValidatePageValuesDEC(m_classReq, m_classRes, m_DEC, m_OC, m_PC, getAC, m_OCQ, m_PCQ);
 		DataManager.setAttribute(session, "m_DEC", m_DEC);
 		ForwardJSP(m_classReq, m_classRes, "/ValidateDECPage.jsp");
 	} // end of doValidateDEC
 
 	/**
 	 * The doValidateDECBlockEdit method gets the values from page the user filled out, validates the input, then
 	 * forwards results to the Validate page Called from 'doCreateDECActions' 'doSubmitDEC' method. Calls
 	 * 'setAC.setDECValueFromPage' to set the data from the page to the bean. Calls 'setAC.setValidatePageValuesDEC' to
 	 * validate the data. Stores 'm_DEC' bean in session. Forwards the page 'ValidateDECPage.jsp' with validation vector
 	 * to display.
 	 *
 	 * @throws Exception
 	 */
 	private void doValidateDECBlockEdit() throws Exception
 	{
 		HttpSession session = m_classReq.getSession();
 		DataManager.setAttribute(session, "DECPageAction", "validate"); // store the page action in attribute
 		DEC_Bean m_DEC = (DEC_Bean) session.getAttribute("m_DEC");
 		if (m_DEC == null)
 			m_DEC = new DEC_Bean();
 		m_setAC.setDECValueFromPage(m_classReq, m_classRes, m_DEC);
 		DataManager.setAttribute(session, "m_DEC", m_DEC);
 		m_setAC.setValidateBlockEdit(m_classReq, m_classRes, "DataElementConcept");
 		DataManager.setAttribute(session, "DECEditAction", "DECBlockEdit");
 		ForwardJSP(m_classReq, m_classRes, "/ValidateDECPage.jsp");
 	} // end of doValidateDEC
 
 	/**
 	 * splits the dec object class or property from cadsr into individual concepts
 	 *
 	 * @param sComp
 	 *            name of the searched component
 	 * @param m_Bean
 	 *            selected EVS bean
 	 * @param nameAction
 	 *            string naming action
 	 *
 	 */
 	private void splitIntoConcepts(String sComp, EVS_Bean m_Bean, String nameAction)
 	{
 		try
 		{
 			HttpSession session = m_classReq.getSession();
 			//  String sSelRow = "";
 			DEC_Bean m_DEC = (DEC_Bean) session.getAttribute("m_DEC");
 			if (m_DEC == null)
 				m_DEC = new DEC_Bean();
 			m_setAC.setDECValueFromPage(m_classReq, m_classRes, m_DEC);
 			Vector vObjectClass = (Vector) session.getAttribute("vObjectClass");
 			if (vObjectClass == null)
 				vObjectClass = new Vector();
 			Vector vProperty = (Vector) session.getAttribute("vProperty");
 			if (vProperty == null)
 				vProperty = new Vector();
 			String sCondr = m_Bean.getCONDR_IDSEQ();
 			String sLongName = m_Bean.getLONG_NAME();
 			String sIDSEQ = m_Bean.getIDSEQ();
 			for (int i=0; i<vObjectClass.size();i++){
 				EVS_Bean eBean =(EVS_Bean)vObjectClass.get(i);
 				logger.debug("At line 598 of DECServlet.java "+eBean.getPREFERRED_DEFINITION());
 			}
 			for (int i=0; i<vProperty.size();i++){
 				EVS_Bean eBean =(EVS_Bean)vProperty.get(i);
 				logger.debug("At line 602 of DECServlet.java "+eBean.getPREFERRED_DEFINITION());
 			}
 			if (sIDSEQ == null)
 				sIDSEQ = "";
 			if (sComp.equals("ObjectClass") || sComp.equals("ObjectQualifier"))
 			{
 				m_DEC.setDEC_OCL_NAME(sLongName);
 				m_DEC.setDEC_OCL_IDSEQ(sIDSEQ);
 			}
 			else if (sComp.equals("Property") || sComp.equals("PropertyClass") || sComp.equals("PropertyQualifier"))
 			{
 				m_DEC.setDEC_PROPL_NAME(sLongName);
 				m_DEC.setDEC_PROPL_IDSEQ(sIDSEQ);
 			}
 			//  String sObjClass = m_DEC.getDEC_OCL_NAME();
 			if (sCondr != null && !sCondr.equals(""))
 			{
 				GetACService getAC = new GetACService(m_classReq, m_classRes, this);
 				Vector vCon = getAC.getAC_Concepts(sCondr, null, true);
 				for (int i=0; i<vCon.size();i++){
 					EVS_Bean eBean =(EVS_Bean)vCon.get(i);
 					logger.debug("At line 623 of DECServlet.java "+eBean.getLONG_NAME());
 					logger.debug("At line 624 of DECServlet.java "+eBean.getEVS_ORIGIN());
 					logger.debug("At line 625 of DECServlet.java "+eBean.getEVS_DATABASE());
 					logger.debug("At line 626 of DECServlet.java "+eBean.getCONCEPT_IDENTIFIER());
 					logger.debug("At line 627 of DECServlet.java "+eBean.getPREFERRED_DEFINITION());
 				}
 				if (vCon != null && vCon.size() > 0)
 				{
 					for (int j = 0; j < vCon.size(); j++)
 					{
 						EVS_Bean bean = new EVS_Bean();
 						bean = (EVS_Bean) vCon.elementAt(j);
 						if (bean != null)
 						{
 							if (sComp.equals("ObjectClass") || sComp.equals("ObjectQualifier"))
 							{
 								logger.debug("At line 622 of DECServlet.java");
 								if (j == 0) // Primary Concept
 								m_DEC = this.addOCConcepts(nameAction, m_DEC, bean, "Primary");
 								else
 									// Secondary Concepts
 									m_DEC = this.addOCConcepts(nameAction, m_DEC, bean, "Qualifier");
 							}
 							else if (sComp.equals("Property") || sComp.equals("PropertyClass")
 									|| sComp.equals("PropertyQualifier"))
 							{
 								logger.debug("At line 632 of DECServlet.java");
 								if (j == 0) // Primary Concept
 									m_DEC = this.addPropConcepts(nameAction, m_DEC, bean, "Primary");
 								else
 									// Secondary Concepts
 									m_DEC = this.addPropConcepts(nameAction, m_DEC, bean, "Qualifier");
 							}
 						}
 					}
 				}
 			}// sCondr != null
 		}
 		catch (Exception e)
 		{
 			this.logger.error("ERROR - splitintoConcept : " + e.toString(), e);
 		}
 	}
 
 	/**
 	 * makes three types of preferred names and stores it in the bean
 	 *
 	 * @param m_classReq
 	 *            HttpServletRequest object
 	 * @param m_classRes
 	 *            HttpServletResponse object
 	 * @param newBean
 	 *            new evs bean
 	 * @param nameAct
 	 *            string new name or apeend name
 	 * @param pageDEC
 	 *            current dec bean
 	 * @return dec bean
 	 */
 	public AC_Bean getACNames(EVS_Bean newBean, String nameAct, AC_Bean pageAC)
 	{
 		HttpSession session = m_classReq.getSession();
 		DEC_Bean pageDEC = (DEC_Bean)pageAC;
 		if (pageDEC == null)
 			pageDEC = (DEC_Bean) session.getAttribute("m_DEC");
 		// get DEC object class and property names
 		String sLongName = "";
 		String sPrefName = "";
 		String sAbbName = "";
 		String sOCName = "";
 		String sPropName = "";
 		String sDef = "";
 		//======================GF30798==============START
 				String sComp = (String) m_classReq.getParameter("sCompBlocks");
 				InsACService ins = new InsACService(m_classReq, m_classRes, this);
 				Vector vObjectClass = (Vector) session.getAttribute("vObjectClass");
 				Vector vProperty = (Vector) session.getAttribute("vProperty");
				logger.debug("at Line 700 of DEC.java" + "***" + sComp + "***" + newBean);
 				if(newBean != null) {
 					if (sComp.startsWith("Object")) {
 						logger.debug("at Line 703 of DEC.java" + newBean.getEVS_DATABASE());
 						if (!(newBean.getEVS_DATABASE().equals("caDSR"))) {
 							for(int i=0; i<vObjectClass.size(); i++){
 								EVS_Bean conceptBean = (EVS_Bean) vObjectClass.elementAt(i);
 								String conIdseq = ins.getConcept("", conceptBean, false);
 								if (conIdseq == null || conIdseq.equals("")){
 									logger.debug("at Line 709 of DEC.java");
 									break;
 								}else {
 									newBean = conceptBean;
 									logger.debug("at Line 713 of DEC.java"+newBean.getPREFERRED_DEFINITION());
 								}
 					        }
 						}
 					}else if (sComp.startsWith("Prop")) {
 						logger.debug("at Line 717 of DEC.java" + newBean.getEVS_DATABASE());
 						if (!(newBean.getEVS_DATABASE().equals("caDSR"))) {
 							for(int i=0; i<vProperty.size(); i++){
 								EVS_Bean conceptBean = (EVS_Bean) vProperty.elementAt(i);
 								String conIdseq = ins.getConcept("", conceptBean, false);
 								if (conIdseq == null || conIdseq.equals("")){
 									logger.debug("at Line 724 of DEC.java");
 									break;
 								}else {
 									newBean = conceptBean;
 									logger.debug("at Line 728 of DEC.java"+newBean.getPREFERRED_DEFINITION());
 								}
 					        }
 						}
 					}
 				}
 				//======================GF30798==============END
 		// get the existing one if not restructuring the name but appending it
 		if (newBean != null)
 		{
 			sLongName = pageDEC.getDEC_LONG_NAME();
 			if (sLongName == null)
 				sLongName = "";
 			sDef = pageDEC.getDEC_PREFERRED_DEFINITION();
 			if (sDef == null)
 				sDef = "";
 			logger.debug("At line 688 of DECServlet.java"+sLongName+"**"+sDef);
 		}
 		// get the typed text on to user name
 		String selNameType = "";
 		if (nameAct.equals("Search") || nameAct.equals("Remove"))
 		{
 			logger.info("At line 750 of DECServlet.java");
 			selNameType = (String) m_classReq.getParameter("rNameConv");
 			sPrefName = (String) m_classReq.getParameter("txPreferredName");
 			if (selNameType != null && selNameType.equals("USER") && sPrefName != null)
 				pageDEC.setAC_USER_PREF_NAME(sPrefName);
 		}
 		// get the object class into the long name and abbr name
 		//Vector vObjectClass = (Vector) session.getAttribute("vObjectClass");	//GF30798
 		if (vObjectClass == null) {
 			vObjectClass = new Vector();
 		}		
 		//begin of GF30798
 		for (int i=0; i<vObjectClass.size();i++){
 			EVS_Bean eBean =(EVS_Bean)vObjectClass.get(i);
 			logger.debug("At line 762 of DECServlet.java "+eBean.getPREFERRED_DEFINITION()+"**"+eBean.getLONG_NAME()+"**"+eBean.getCONCEPT_IDENTIFIER());
 		}
 		//end of GF30798
 		// add the Object Class qualifiers first
 		for (int i = 1; vObjectClass.size() > i; i++)
 		{
 			EVS_Bean eCon = (EVS_Bean) vObjectClass.elementAt(i);
 			if (eCon == null)
 				eCon = new EVS_Bean();
 			String conName = eCon.getLONG_NAME();
 			if (conName == null)
 				conName = "";
 			if (!conName.equals(""))
 			{
 				logger.info("At line 778 of DECServlet.java");
 				String nvpValue = "";
 				if (this.checkNVP(eCon))
 					nvpValue = "::" + eCon.getNVP_CONCEPT_VALUE();
 
 				// rearrange it long name and definition
 				if (newBean == null)
 				{
 					if (!sLongName.equals(""))
 						sLongName += " ";
 					sLongName += conName + nvpValue;
 					if (!sDef.equals(""))
 						sDef += "_"; // add definition
 					sDef += eCon.getPREFERRED_DEFINITION() + nvpValue;
 					logger.debug("At line 792 of DECServlet.java"+sLongName+"**"+sDef);
 
 				}
 				if (!sAbbName.equals(""))
 					sAbbName += "_";
 				if (conName.length() > 3)
 					sAbbName += conName.substring(0, 4); // truncate to four letters
 				else
 					sAbbName += conName;
 				// add object qualifiers to object class name
 				if (!sOCName.equals(""))
 					sOCName += " ";
 				sOCName += conName + nvpValue;
 				logger.debug("At line 805 of DECServlet.java"+conName+"**"+nvpValue+"**"+sLongName+"**"+sDef+"**"+sOCName);
 			}
 		}
 		// add the Object Class primary
 		if (vObjectClass != null && vObjectClass.size() > 0)
 		{
 			EVS_Bean eCon = (EVS_Bean) vObjectClass.elementAt(0);
 			if (eCon == null)
 				eCon = new EVS_Bean();
 			String sPrimary = eCon.getLONG_NAME();
 			if (sPrimary == null)
 				sPrimary = "";
 			if (!sPrimary.equals(""))
 			{
 				logger.info("At line 819 of DECServlet.java");
 				String nvpValue = "";
 				if (this.checkNVP(eCon))
 					nvpValue = "::" + eCon.getNVP_CONCEPT_VALUE();
 
 				// rearrange it only long name and definition
 				if (newBean == null)
 				{
 					if (!sLongName.equals(""))
 						sLongName += " ";
 					sLongName += sPrimary + nvpValue;
 					if (!sDef.equals(""))
 						sDef += "_"; // add definition
 					sDef += eCon.getPREFERRED_DEFINITION() + nvpValue;
 					logger.debug("At line 833 of DECServlet.java"+sLongName+"**"+sDef);	
 				}
 				if (!sAbbName.equals(""))
 					sAbbName += "_";
 				if (sPrimary.length() > 3)
 					sAbbName += sPrimary.substring(0, 4); // truncate to four letters
 				else
 					sAbbName += sPrimary;
 				// add primary object to object name
 				if (!sOCName.equals(""))
 					sOCName += " ";
 				sOCName += sPrimary + nvpValue;
 				logger.debug("At line 778 of DECServlet.java"+sPrimary+"**"+nvpValue+"**"+sLongName+"**"+sDef+"**"+sOCName);
 			}
 		}
 		// get the Property into the long name and abbr name
 		//Vector vProperty = (Vector) session.getAttribute("vProperty");	//GF30798
 		if (vProperty == null)
 			vProperty = new Vector();
 		//begin of GF30798
 		for (int i=0; i<vProperty.size();i++){
 			EVS_Bean eBean =(EVS_Bean)vProperty.get(i);
 			logger.debug("At line 853 of DECServlet.java "+eBean.getPREFERRED_DEFINITION()+"**"+eBean.getLONG_NAME()+"**"+eBean.getCONCEPT_IDENTIFIER());
 		}
 		//begin of GF30798
 		// add the property qualifiers first
 		for (int i = 1; vProperty.size() > i; i++)
 		{
 			EVS_Bean eCon = (EVS_Bean) vProperty.elementAt(i);
 			if (eCon == null)
 				eCon = new EVS_Bean();
 			String conName = eCon.getLONG_NAME();
 			if (conName == null)
 				conName = "";
 			if (!conName.equals(""))
 			{
 				logger.info("At line 869 of DECServlet.java");
 				String nvpValue = "";
 				if (this.checkNVP(eCon))
 					nvpValue = "::" + eCon.getNVP_CONCEPT_VALUE();
 
 				// rearrange it long name and definition
 				if (newBean == null)
 				{
 					if (!sLongName.equals(""))
 						sLongName += " ";
 					sLongName += conName + nvpValue;
 					if (!sDef.equals(""))
 						sDef += "_"; // add definition
 					sDef += eCon.getPREFERRED_DEFINITION() + nvpValue;
 					logger.debug("At line 883 of DECServlet.java"+sLongName+"**"+sDef);
 				}
 				if (!sAbbName.equals(""))
 					sAbbName += "_";
 				if (conName.length() > 3)
 					sAbbName += conName.substring(0, 4); // truncate to four letters
 				else
 					sAbbName += conName;
 				// add property qualifiers to property name
 				if (!sPropName.equals(""))
 					sPropName += " ";
 				sPropName += conName + nvpValue;
 				logger.debug("At line 895 of DECServlet.java"+conName+"**"+nvpValue+"**"+sLongName+"**"+sDef+"**"+sOCName);
 			}
 		}
 		// add the property primary
 		if (vProperty != null && vProperty.size() > 0)
 		{
 			EVS_Bean eCon = (EVS_Bean) vProperty.elementAt(0);
 			if (eCon == null)
 				eCon = new EVS_Bean();
 			String sPrimary = eCon.getLONG_NAME();
 			if (sPrimary == null)
 				sPrimary = "";
 			if (!sPrimary.equals(""))
 			{
 				logger.info("At line 909 of DECServlet.java");
 				String nvpValue = "";
 				if (this.checkNVP(eCon))
 					nvpValue = "::" + eCon.getNVP_CONCEPT_VALUE();
 
 				// rearrange it only long name and definition
 				if (newBean == null)
 				{
 					if (!sLongName.equals(""))
 						sLongName += " ";
 					sLongName += sPrimary + nvpValue;
 					if (!sDef.equals(""))
 						sDef += "_"; // add definition
 					sDef += eCon.getPREFERRED_DEFINITION() + nvpValue;
 					logger.debug("At line 923 of DECServlet.java"+sLongName+"**"+sDef);
 				}
 				if (!sAbbName.equals(""))
 					sAbbName += "_";
 				if (sPrimary.length() > 3)
 					sAbbName += sPrimary.substring(0, 4); // truncate to four letters
 				else
 					sAbbName += sPrimary;
 				// add primary property to property name
 				if (!sPropName.equals(""))
 					sPropName += " ";
 				sPropName += sPrimary + nvpValue;
 				logger.debug("At line 935 of DECServlet.java"+sPrimary+"**"+nvpValue+"**"+sLongName+"**"+sDef+"**"+sOCName);
 			}
 		}
 		// truncate to 30 characters
 		if (sAbbName != null && sAbbName.length() > 30)
 			sAbbName = sAbbName.substring(0, 30);
 		// add the abbr name to vd bean and page is selected
 		pageDEC.setAC_ABBR_PREF_NAME(sAbbName);
 		// make abbr name name preferrd name if sys was selected
 		if (selNameType != null && selNameType.equals("ABBR"))
 			pageDEC.setDEC_PREFERRED_NAME(sAbbName);
 		// appending to the existing;
 		if (newBean != null)
 		{
 			String sSelectName = newBean.getLONG_NAME();
 			if (!sLongName.equals(""))
 				sLongName += " ";
 			sLongName += sSelectName;
 			if (!sDef.equals(""))
 				sDef += "_"; // add definition
 			sDef += newBean.getPREFERRED_DEFINITION();
 			logger.debug("At line 956 of DECServlet.java"+sLongName+"**"+sDef);
 		}
 		// store the long names, definition, and usr name in vd bean if searched
 		if (nameAct.equals("Search"))
 		{
 			pageDEC.setDEC_LONG_NAME(AdministeredItemUtil.handleLongName(sLongName));	//GF32004;
 			pageDEC.setDEC_PREFERRED_DEFINITION(sDef);
 			logger.debug("DEC_LONG_NAME at Line 963 of DECServlet.java"+pageDEC.getDEC_LONG_NAME()+"**"+pageDEC.getDEC_PREFERRED_DEFINITION());
 		}
 		if (!nameAct.equals("OpenDEC")){
 			pageDEC.setDEC_OCL_NAME(sOCName);
 			pageDEC.setDEC_PROPL_NAME(sPropName);
 			logger.debug("At line 968 of DECServlet.java"+sOCName+"**"+sPropName);
 		}   
 		if (nameAct.equals("Search") || nameAct.equals("Remove"))
 		{
 			pageDEC.setAC_SYS_PREF_NAME("(Generated by the System)"); // only for dec
 			if (selNameType != null && selNameType.equals("SYS"))
 				pageDEC.setDEC_PREFERRED_NAME(pageDEC.getAC_SYS_PREF_NAME());
 		}
 		return pageDEC;
 	}
 
 	/**
 	 *
 	 * @param nameAction
 	 *            string naming action
 	 *
 	 */
 	@SuppressWarnings("unchecked")
 	private void doDECUseSelection(String nameAction)
 	{
 		try
 		{
 			HttpSession session = m_classReq.getSession();
 			String sSelRow = "";
 			boolean selectedOCQualifiers = false;
 			boolean selectedPropQualifiers =  false;
 			//   InsACService insAC = new InsACService(m_classReq, m_classRes, this);
 			DEC_Bean m_DEC = (DEC_Bean) session.getAttribute("m_DEC");
 			if (m_DEC == null)
 				m_DEC = new DEC_Bean();
 			m_setAC.setDECValueFromPage(m_classReq, m_classRes, m_DEC);
 			Vector<EVS_Bean> vObjectClass = (Vector) session.getAttribute("vObjectClass");
 			//begin of GF30798
 			for (int i=0; i<vObjectClass.size();i++){
 				EVS_Bean eBean =(EVS_Bean)vObjectClass.get(i);
 				logger.debug("At line 1001 of DECServlet.java "+eBean.getPREFERRED_DEFINITION()+"**"+eBean.getLONG_NAME()+"**"+eBean.getCONCEPT_IDENTIFIER());
 			}
 			//end of GF30798
 			if (vObjectClass == null || vObjectClass.size() == 0) {
 				vObjectClass = new Vector<EVS_Bean>();
 				//reset the attributes for keeping track of non-caDSR choices...
 				session.removeAttribute("chosenOCCodes");
 				session.removeAttribute("chosenOCDefs");
 				session.removeAttribute("changedOCDefsWarning");
 			}
 			Vector<EVS_Bean> vProperty = (Vector) session.getAttribute("vProperty");
 			//begin of GF30798
 			for (int i=0; i<vProperty.size();i++){
 				EVS_Bean eBean =(EVS_Bean)vProperty.get(i);
 				logger.debug("At line 1015 of DECServlet.java "+eBean.getPREFERRED_DEFINITION()+"**"+eBean.getLONG_NAME()+"**"+eBean.getCONCEPT_IDENTIFIER());
 			}
 			//end of GF30798
 			if (vProperty == null || vProperty.size() == 0) {
 				vProperty = new Vector<EVS_Bean>();
 				//reset the attributes for keeping track of non-caDSR choices...
 				session.removeAttribute("chosenPropCodes");
 				session.removeAttribute("chosenPropDefs");
 				session.removeAttribute("changedPropDefsWarning");
 			}
 			if (vObjectClass.size()>1){
 				selectedOCQualifiers = true; 
 			}
 			if (vProperty.size()>1){
 				selectedPropQualifiers = true;
 			}
 			Vector vAC = null;
 			EVS_Bean blockBean = new EVS_Bean();
 			String sComp = (String) m_classReq.getParameter("sCompBlocks");
 			if (sComp == null)
 				sComp = "";
 			// get the search bean from the selected row
 			sSelRow = (String) m_classReq.getParameter("selCompBlockRow");
 			vAC = (Vector) session.getAttribute("vACSearch");
 			logger.debug("At Line 951 of DECServlet.java"+Arrays.asList(vAC));
 			if (vAC == null)
 				vAC = new Vector();
 			if (sSelRow != null && !sSelRow.equals(""))
 			{
 				String sObjRow = sSelRow.substring(2);
 				Integer intObjRow = new Integer(sObjRow);
 				int intObjRow2 = intObjRow.intValue();
 				if (vAC.size() > intObjRow2 - 1)
 					blockBean = (EVS_Bean) vAC.elementAt(intObjRow2);
 				String sNVP = (String) m_classReq.getParameter("nvpConcept");
 				if (sNVP != null && !sNVP.equals(""))
 				{
 					blockBean.setNVP_CONCEPT_VALUE(sNVP);
 					String sName = blockBean.getLONG_NAME();
 					blockBean.setLONG_NAME(sName + "::" + sNVP);
 					blockBean.setPREFERRED_DEFINITION(blockBean.getPREFERRED_DEFINITION() + "::" + sNVP);
 				}
 				logger.debug("At Line 977 of DECServlet.java"+sNVP+"**"+blockBean.getLONG_NAME()+"**"+ blockBean.getPREFERRED_DEFINITION());
 
 				//System.out.println(sNVP + sComp + blockBean.getLONG_NAME() + blockBean.getPREFERRED_DEFINITION());
 			}
 			else
 			{
 				storeStatusMsg("Unable to get the selected row from the " + sComp + " search results.\\n"
 						+ "Please try again.");
 				return;
 			}
 			// send it back if unable to obtion the concept
 			if (blockBean == null || blockBean.getLONG_NAME() == null)
 			{
 				storeStatusMsg("Unable to obtain concept from the selected row of the " + sComp
 						+ " search results.\\n" + "Please try again.");
 				return;
 			}
 
 			//Store chosen concept code and definition for later use in alt. definition.
 			String code = blockBean.getCONCEPT_IDENTIFIER();
 			String def = blockBean.getPREFERRED_DEFINITION();
 			Vector<String> codes = null;
 			Vector<String> defs = null;
 
 			if (sComp.startsWith("Object")) {
 				if (session.getAttribute("chosenOCCodes") == null || ((Vector)session.getAttribute("chosenOCCodes")).size() == 0) {
 					codes = new Vector<String>();
 					defs = new Vector<String>();
 				} else {
 					codes =(Vector<String>) session.getAttribute("chosenOCCodes"); 
 					defs = (Vector<String>) session.getAttribute("chosenOCDefs");
 				}
 				logger.debug("At line 1009 of DECServlet.java"+codes +"***"+defs);
 			}
 
 			else if (sComp.startsWith("Prop")) {
 				if (session.getAttribute("chosenPropCodes") == null) {
 					codes = new Vector<String>();
 					defs = new Vector<String>();
 				} else {
 					codes =(Vector<String>) session.getAttribute("chosenPropCodes"); 
 					defs = (Vector<String>) session.getAttribute("chosenPropDefs");
 				}
 				logger.debug("At line 1102 of DECServlet.java"+Arrays.asList(codes) +"***"+Arrays.asList(defs));
 			}
 			
 			if (!codes.contains(code)) {	
 				codes.add(code);    	   
 				defs.add(def);
 				logger.debug("At line 1108 of DECServlet.java"+Arrays.asList(codes) +"***"+Arrays.asList(defs));
 			}
 
 			// do the primary search selection action
 			if (sComp.equals("ObjectClass") || sComp.equals("Property") || sComp.equals("PropertyClass"))
 			{
 				logger.debug("At line 1114 of DECServlet.java");
 				if (blockBean.getEVS_DATABASE().equals("caDSR"))
 				{
 					logger.debug("At line 1117 of DECServlet.java");
 					// split it if rep term, add concept class to the list if evs id exists
 					if (blockBean.getCONDR_IDSEQ() == null || blockBean.getCONDR_IDSEQ().equals(""))
 					{
 						logger.debug("At line 1121 of DECServlet.java");
 						if (blockBean.getCONCEPT_IDENTIFIER() == null || blockBean.getCONCEPT_IDENTIFIER().equals(""))
 						{
 							storeStatusMsg("This " + sComp
 									+ " is not associated to a concept, so the data is suspect. \\n"
 									+ "Please choose another " + sComp + " .");
 						}
 						else
 							// concept class search results
 						{
 							if (sComp.equals("ObjectClass"))
 								m_DEC = this.addOCConcepts(nameAction, m_DEC, blockBean, "Primary");
 
 							else
 								m_DEC = this.addPropConcepts(nameAction, m_DEC, blockBean, "Primary");
 						}
 					}
 					else{
 						logger.debug("At line 1139 of DECServlet.java ");
 						// split it into concepts for object class or property search results
 						splitIntoConcepts(sComp, blockBean, nameAction);
 					}    
 				}
 				else
 					// evs search results
 				{
 					logger.debug("At line 1147 of DECServlet.java ");
 					if (sComp.equals("ObjectClass"))
 						m_DEC = this.addOCConcepts(nameAction, m_DEC, blockBean, "Primary");
 					else
 						m_DEC = this.addPropConcepts(nameAction, m_DEC, blockBean, "Primary");
 				}
 			}
 			else if (sComp.equals("ObjectQualifier"))
 			{
 				logger.debug("At line 1081 of DECServlet.java ");
 				// Do this to reserve zero position in vector for primary concept
 				if (vObjectClass.size() < 1)
 				{
 					EVS_Bean OCBean = new EVS_Bean();
 					vObjectClass.addElement(OCBean);
 					DataManager.setAttribute(session, "vObjectClass", vObjectClass);
 				}
 				m_DEC.setDEC_OCL_IDSEQ("");
 				m_DEC = this.addOCConcepts(nameAction, m_DEC, blockBean, "Qualifier");
 			}
 			else if (sComp.equals("PropertyQualifier"))
 			{
 				logger.debug("At line 1078 of DECServlet.java ");
 				// Do this to reserve zero position in vector for primary concept
 				if (vProperty.size() < 1)
 				{
 					EVS_Bean PCBean = new EVS_Bean();
 					vProperty.addElement(PCBean);
 					DataManager.setAttribute(session, "vProperty", vProperty);
 				}
 				m_DEC.setDEC_PROPL_IDSEQ("");
 				m_DEC = this.addPropConcepts(nameAction, m_DEC, blockBean, "Qualifier");
 			}
 
 
 			if ( sComp.equals("ObjectClass") || sComp.equals("ObjectQualifier")){
 				vObjectClass = (Vector) session.getAttribute("vObjectClass");
 				for (int i=0; i<vObjectClass.size();i++){
 					EVS_Bean eBean =(EVS_Bean)vObjectClass.get(i);
 					logger.debug("At line 1186 of DECServlet.java "+eBean.getPREFERRED_DEFINITION()+"**"+eBean.getLONG_NAME()+"**"+eBean.getCONCEPT_IDENTIFIER());
 				}
 				if (vObjectClass != null && vObjectClass.size()>0){ 
 					logger.debug("at line 1189 of DECServlet.java"+vObjectClass.size());
 					vObjectClass = this.getMatchingThesarusconcept(vObjectClass, "Object Class");	//get the matching concept from EVS based on the caDSR's CDR (object class)
 					for (int i=0; i<vObjectClass.size();i++){
 						EVS_Bean eBean =(EVS_Bean)vObjectClass.get(i);
 						logger.debug("At line 1193 of DECServlet.java "+eBean.getPREFERRED_DEFINITION()+"**"+eBean.getLONG_NAME()+"**"+eBean.getCONCEPT_IDENTIFIER()+"**"+vObjectClass.size());
 					}
 					m_DEC = this.updateOCAttribues(vObjectClass, m_DEC);	//populate caDSR's DEC bean based on VO (from EVS results)
 
 					this.checkChosenConcepts(session,codes, defs, vObjectClass, "OC");	//make sure user's chosen/edited definition is not different from the EVS definition???
 				} 
 
 				if (blockBean.getcaDSR_COMPONENT() != null && blockBean.getcaDSR_COMPONENT().equals("Concept Class")){
 					logger.debug("At line 1139 of DECServlet.java");
 					m_DEC.setDEC_OCL_IDSEQ("");  
 				}else {//Object Class or from vocabulary
 					if(blockBean.getcaDSR_COMPONENT() != null && !selectedOCQualifiers){//if selected existing object class (JT just checking getcaDSR_COMPONENT???)
 						logger.debug("At line 1108 of DECServlet.java");
 						ValidationStatusBean statusBean = new ValidationStatusBean();
 						statusBean.setStatusMessage("**  Using existing "+blockBean.getcaDSR_COMPONENT()+" "+blockBean.getLONG_NAME()+" ("+blockBean.getID()+"v"+blockBean.getVERSION()+") from "+blockBean.getCONTEXT_NAME());
 						statusBean.setCondrExists(true);
 						statusBean.setCondrIDSEQ(blockBean.getCONDR_IDSEQ());
 						statusBean.setEvsBeanExists(true);
 						statusBean.setEvsBeanIDSEQ(blockBean.getIDSEQ());
 						session.setAttribute("ocStatusBean", statusBean);
 					}else{
 						m_DEC.setDEC_OCL_IDSEQ("");   
 					}
 				}
 				DataManager.setAttribute(session, "vObjectClass", vObjectClass);	//save EVS VO in session
 				for (int i=0; i<vObjectClass.size();i++){
 					EVS_Bean eBean =(EVS_Bean)vObjectClass.get(i);
 					logger.debug("At line 1220 of DECServlet.java "+eBean.getPREFERRED_DEFINITION()+"**"+eBean.getLONG_NAME()+"**"+eBean.getCONCEPT_IDENTIFIER());
 				}
 			}
 			if (sComp.equals("Property") || sComp.equals("PropertyClass") || sComp.equals("PropertyQualifier")){
 				vProperty = (Vector) session.getAttribute("vProperty");
 				if (vProperty != null && vProperty.size()>0){  		    	   
 					vProperty  = this.getMatchingThesarusconcept(vProperty, "Property");
 					logger.debug("At line 1127 of DECServlet.java "+Arrays.asList(vProperty));
 					m_DEC = this.updatePropAttribues(vProperty, m_DEC);
 
 					boolean warning = false;            	   
 
 					//Rebuilding codes and defs to get rid of unused codes
 					Vector<String> rebuiltCodes = new Vector<String>();
 					Vector<String> rebuiltDefs = new Vector<String>();
 
 					//added in 4.1 to check if OC exists and set alternate def. if used an EVS concept	   
 					this.checkChosenConcepts(session,codes, defs, vProperty, "Prop");
 
 				}
 				if (blockBean.getcaDSR_COMPONENT()!= null && blockBean.getcaDSR_COMPONENT().equals("Concept Class")){
 					logger.debug("At line 1141 of DECServlet.java");
 					m_DEC.setDEC_PROPL_IDSEQ("");  
 				}else{//Property or from vocabulary
 					if(blockBean.getcaDSR_COMPONENT() != null && !selectedPropQualifiers){//if selected existing property
 						logger.debug("At line 1145 of DECServlet.java");
 						ValidationStatusBean statusBean = new ValidationStatusBean();
 						statusBean.setStatusMessage("**  Using existing "+blockBean.getcaDSR_COMPONENT()+" "+blockBean.getLONG_NAME()+" ("+blockBean.getID()+"v"+blockBean.getVERSION()+") from "+blockBean.getCONTEXT_NAME());
 						statusBean.setCondrExists(true);
 						statusBean.setCondrIDSEQ(blockBean.getCONDR_IDSEQ());
 						statusBean.setEvsBeanExists(true);
 						statusBean.setEvsBeanIDSEQ(blockBean.getIDSEQ());
 						session.setAttribute("propStatusBean", statusBean);
 					}else{
 						m_DEC.setDEC_PROPL_IDSEQ("");   
 					}
 				}
 				DataManager.setAttribute(session, "vProperty", vProperty);
 				logger.debug("At line 1158 of DECServlet.java"+Arrays.asList(vProperty));
 			}
 			// rebuild new name if not appending
 			EVS_Bean nullEVS = null; 
 			if (nameAction.equals("newName"))
 				m_DEC = (DEC_Bean) this.getACNames(nullEVS, "Search", m_DEC);
 			else if (nameAction.equals("blockName"))
 				m_DEC = (DEC_Bean) this.getACNames(nullEVS, "blockName", m_DEC);
 			DataManager.setAttribute(session, "m_DEC", m_DEC);	//set the user's selection + data from EVS in the DEC in session (for submission later on)
 		}
 		catch (Exception e)
 		{
 			this.logger.error("ERROR - doDECUseSelection : " + e.toString(), e);
 		}
 	} // end of doDECUseSelection
 	
 	public static void checkChosenConcepts(HttpSession session, Vector<String> codes, Vector<String> defs, Vector<EVS_Bean> vConcepts, String type) {
 		
 		if (codes == null && session.getAttribute("chosen"+type+"Codes") != null) {
 			codes = (Vector<String>) session.getAttribute("chosen"+type+"Codes");
 			defs = (Vector<String>) session.getAttribute("chosen"+type+"Defs");
 		} else if (codes == null)
 			return;
 		logger.debug("At line 1207 of DECServlet.java "+Arrays.asList(codes)+"**"+Arrays.asList(defs));
 		boolean warning = false;
 		//Rebuilding codes and defs to get rid of unused codes
 		Vector<String> rebuiltCodes = new Vector<String>();
 		Vector<String> rebuiltDefs = new Vector<String>();
 
 		
 		//vConcepts is a list of all concepts currently used for this element
 		//Chosen codes and defs are contained in [ ] if are originating from a caDSR element.
 		//The codes list should not include anything that's not in vConcept (in case of removal)
 		//Also need to make sure that the new concepts are at the end of the list, like they're supposed to be.
 		//added in 4.1 to check if OC exists and set alternate def. if used an EVS concept	   
 		for(EVS_Bean eb: vConcepts){
 			String usedCode = eb.getCONCEPT_IDENTIFIER();
 			String usedDef = eb.getPREFERRED_DEFINITION();
 			logger.debug("At line 1222 of DECServlet.java "+usedCode+"**"+usedDef);
 			boolean found = false;
 			for(int codeCounter = 0; codeCounter < codes.size(); codeCounter++){
 				String chosenCode = codes.get(codeCounter);
 				logger.debug("At line 1183 of DECServlet.java "+chosenCode);		
 				if (chosenCode.equals(usedCode) || chosenCode.equals("*"+usedCode) || (chosenCode.contains(usedCode) && chosenCode.endsWith("]"))){
 					//Code is used, transfer to rebuilt list
 					found = true;
 					
 						rebuiltCodes.add("*"+chosenCode); //Adding * to mark a new concept (need to be added at the end of old definition)
 						rebuiltDefs.add("*"+defs.get(codeCounter));
 						logger.debug("At line 1233 of DECServlet.java"+defs.get(codeCounter)+"**"+usedDef);
 						//if definitions don't match, put up a warning.
 						if (defs  != null && usedDef != null && !defs.get(codeCounter).equals(usedDef))	//GF32004 not related to this ticket, but found NPE during the test
 							warning = true;
 					
 				}
 				logger.debug("At line 1239 of DECServlet.java"+Arrays.asList(rebuiltCodes)+"**"+Arrays.asList(rebuiltDefs));
 			}
 			logger.debug("At line 1241 of DECServlet.java"+found);
 			//if you got to here without finding usedCode in chosenCodes, it means you're Editing.
 			//Add current used code to fill in the gap.
 			if (!found) {
 				rebuiltCodes.add(usedCode);
 				rebuiltDefs.add(usedDef);
 				logger.debug("At line 1247 of DECServlet.java"+Arrays.asList(rebuiltCodes)+"**"+Arrays.asList(rebuiltDefs));
 			}
 		}
 		
 		logger.debug("At line 1251 of DECServlet.java"+warning+"**");
 		session.setAttribute("chosen"+type+"Codes", rebuiltCodes);
 		session.setAttribute("chosen"+type+"Defs", rebuiltDefs);
 
 		if (warning)
 			session.setAttribute("changed"+type+"DefsWarning", Boolean.toString(warning));
 		else
 			session.removeAttribute("changed"+type+"DefsWarning");
 	}
 	
 	/**
 	 * adds the selected concept to the vector of concepts for property
 	 *
 	 * @param m_classReq
 	 *            HttpServletRequest
 	 * @param m_classRes
 	 *            HttpServletResponse
 	 * @param nameAction
 	 *            String naming action
 	 * @param decBean
 	 *            selected DEC_Bean
 	 * @param eBean
 	 *            selected EVS_Bean
 	 * @param ocType
 	 *            String property type (primary or qualifier)
 	 * @return DEC_Bean
 	 * @throws Exception
 	 */
 	@SuppressWarnings("unchecked")
 	private DEC_Bean addOCConcepts(String nameAction,
 			DEC_Bean decBean, EVS_Bean eBean, String ocType) throws Exception
 			{
 		HttpSession session = m_classReq.getSession();
 		// add the concept bean to the OC vector and store it in the vector
 		Vector<EVS_Bean> vObjectClass = (Vector) session.getAttribute("vObjectClass");
 		if (vObjectClass == null)
 			vObjectClass = new Vector<EVS_Bean>();
 			// get the evs user bean
 			EVS_UserBean eUser = (EVS_UserBean) this.sessionData.EvsUsrBean; // (EVS_UserBean)session.getAttribute(EVSSearch.EVS_USER_BEAN_ARG);
 			// //("EvsUserBean");
 			if (eUser == null)
 				eUser = new EVS_UserBean();
 			eBean.setCON_AC_SUBMIT_ACTION("INS");
 			eBean.setCONTE_IDSEQ(decBean.getDEC_CONTE_IDSEQ());
 			String eDB = eBean.getEVS_DATABASE();
 			logger.debug("At line 1278 of DECServlet.java "+eDB);
 			if (eDB != null && eBean.getEVS_ORIGIN() != null && eDB.equalsIgnoreCase("caDSR"))
 			{
 				logger.debug("At line 1271 of DECServlet.java");
 				eDB = eBean.getVocabAttr(eUser, eBean.getEVS_ORIGIN(), EVSSearch.VOCAB_NAME, EVSSearch.VOCAB_DBORIGIN); // "vocabName",
 				// "vocabDBOrigin");
 				logger.debug("At line 1274 of DECServlet.java"+eDB);
 				if (eDB.equals(EVSSearch.META_VALUE)) // "MetaValue"))
 					eDB = eBean.getEVS_ORIGIN();
 				eBean.setEVS_DATABASE(eDB); // eBean.getEVS_ORIGIN());
 			}
 
 			// get its matching thesaurus concept
 			// System.out.println(eBean.getEVS_ORIGIN() + " before thes concept for OC " + eDB);
 			//EVSSearch evs = new EVSSearch(m_classReq, m_classRes, this);
 			//eBean = evs.getThesaurusConcept(eBean);
 
 			// add to the vector and store it in the session, reset if primary and alredy existed, add otehrwise
 			if (ocType.equals("Primary") && vObjectClass.size() > 0)
 				vObjectClass.setElementAt(eBean, 0);
 			else
 				vObjectClass.addElement(eBean);
 			for (int i=0; i<vObjectClass.size();i++){
 				EVS_Bean Bean =(EVS_Bean)vObjectClass.get(i);
 				logger.debug("At line 1394 of DECServlet.java "+Bean.getPREFERRED_DEFINITION()+"**"+eBean.getLONG_NAME()+"**"+eBean.getCONCEPT_IDENTIFIER());
 			}
 			DataManager.setAttribute(session, "vObjectClass", vObjectClass);
 			DataManager.setAttribute(session, "newObjectClass", "true");
 			// DataManager.setAttribute(session, "selObjQRow", sSelRow);
 			// add to name if appending
 			if (nameAction.equals("appendName"))
 				decBean = (DEC_Bean) this.getACNames(eBean, "Search", decBean);
 			return decBean;
 			} // end addOCConcepts
 
 	/**
 	 * adds the selected concept to the vector of concepts for property
 	 *
 	 * @param m_classReq
 	 *            HttpServletRequest
 	 * @param m_classRes
 	 *            HttpServletResponse
 	 * @param nameAction
 	 *            String naming action
 	 * @param decBean
 	 *            selected DEC_Bean
 	 * @param eBean
 	 *            selected EVS_Bean
 	 * @param propType
 	 *            String property type (primary or qualifier)
 	 * @return DEC_Bean
 	 * @throws Exception
 	 */
 	@SuppressWarnings("unchecked")
 	private DEC_Bean addPropConcepts(String nameAction,
 			DEC_Bean decBean, EVS_Bean eBean, String propType) throws Exception
 			{
 		HttpSession session = m_classReq.getSession();
 		// add the concept bean to the OC vector and store it in the vector
 		Vector<EVS_Bean> vProperty = (Vector) session.getAttribute("vProperty");
 		if (vProperty == null)
 			vProperty = new Vector<EVS_Bean>();
 			// get the evs user bean
 			EVS_UserBean eUser = (EVS_UserBean) this.sessionData.EvsUsrBean; // (EVS_UserBean)session.getAttribute(EVSSearch.EVS_USER_BEAN_ARG);
 			// //("EvsUserBean");
 			if (eUser == null)
 				eUser = new EVS_UserBean();
 			eBean.setCON_AC_SUBMIT_ACTION("INS");
 			eBean.setCONTE_IDSEQ(decBean.getDEC_CONTE_IDSEQ());
 			String eDB = eBean.getEVS_DATABASE();
 			if (eDB != null && eBean.getEVS_ORIGIN() != null && eDB.equalsIgnoreCase("caDSR"))
 			{
 				eDB = eBean.getVocabAttr(eUser, eBean.getEVS_ORIGIN(), EVSSearch.VOCAB_NAME, EVSSearch.VOCAB_DBORIGIN); // "vocabName",
 				// "vocabDBOrigin");
 				logger.debug("At line 1339 of DECServlet.java"+eDB);
 				if (eDB.equals(EVSSearch.META_VALUE)) // "MetaValue"))
 					eDB = eBean.getEVS_ORIGIN();
 				eBean.setEVS_DATABASE(eDB); // eBean.getEVS_ORIGIN());
 			}
 			// System.out.println(eBean.getEVS_ORIGIN() + " before thes concept for PROP " + eDB);
 			// EVSSearch evs = new EVSSearch(m_classReq, m_classRes, this);
 			//eBean = evs.getThesaurusConcept(eBean);
 			// add to the vector and store it in the session, reset if primary and alredy existed, add otehrwise
 			if (propType.equals("Primary") && vProperty.size() > 0)
 				vProperty.setElementAt(eBean, 0);
 			else
 				vProperty.addElement(eBean);
 			DataManager.setAttribute(session, "vProperty", vProperty);
 			DataManager.setAttribute(session, "newProperty", "true");
 			logger.debug("At line 1354 of DECServlet.java**"+Arrays.asList(vProperty));
 			// DataManager.setAttribute(session, "selObjQRow", sSelRow);
 			// add to name if appending
 			if (nameAction.equals("appendName"))
 				decBean = (DEC_Bean) this.getACNames(eBean, "Search", decBean);
 			return decBean;
 			} // end addPropConcepts
 
 	/**
 	 * The doInsertDEC method to insert or update record in the database. Called from 'service' method where reqType is
 	 * 'validateDECFromForm'. Retrieves the session bean m_DEC. if the action is reEditDEC forwards the page back to
 	 * Edit or create pages.
 	 *
 	 * Otherwise, calls 'doUpdateDECAction' for editing the vd. calls 'doInsertDECfromDEAction' for creating the vd from
 	 * DE page. calls 'doInsertDECfromMenuAction' for creating the vd from menu .
 	 *
 	 * @throws Exception
 	 */
 	private void doInsertDEC() throws Exception
 	{
 		HttpSession session = m_classReq.getSession();
 		// make sure that status message is empty
 		DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, "");
 		Vector vStat = new Vector();
 		DataManager.setAttribute(session, "vStatMsg", vStat);
 		String sOriginAction = (String) session.getAttribute("originAction");
 		String sDECAction = (String) session.getAttribute("DECAction");
 		if (sDECAction == null)
 			sDECAction = "";
 		String sDECEditAction = (String) session.getAttribute("DECEditAction");
 		if (sDECEditAction == null)
 			sDECEditAction = "";
 		String sAction = (String) m_classReq.getParameter("ValidateDECPageAction");
 		if (sAction == null)
 			sAction = "submitting"; // for direct submit without validating
 		if (sAction != null)
 		{// going back to create or edit pages from validation page
 			if (sAction.equals("reEditDEC"))
 			{
 				if (sDECAction.equals("EditDEC") || sDECAction.equals("BlockEdit"))
 					ForwardJSP(m_classReq, m_classRes, "/EditDECPage.jsp");
 				else
 					ForwardJSP(m_classReq, m_classRes, "/CreateDECPage.jsp");
 			}
 			else
 			{
 				// update the database for edit action
 				if (sDECAction.equals("EditDEC") && !sOriginAction.equals("BlockEditDEC"))
 					doUpdateDECAction();
 				else if (sDECEditAction.equals("DECBlockEdit"))
 					doUpdateDECActionBE();
 				// if create new dec from create DE page.
 				else if (sOriginAction.equals("CreateNewDECfromCreateDE")
 						|| sOriginAction.equals("CreateNewDECfromEditDE"))
 					doInsertDECfromDEAction(sOriginAction);
 				// FROM MENU, TEMPLATE, VERSION
 				else{
 					logger.info("*****************doInsertDEC called");
 					doInsertDECfromMenuAction();
 				}
 					
 			}
 		}
 	} // end of doInsertDEC
 
 	/**
 	 * update record in the database and display the result. Called from 'doInsertDEC' method when the aciton is
 	 * editing. Retrieves the session bean m_DEC. calls 'insAC.setDEC' to update the database. updates the DEbean and
 	 * sends back to EditDE page if origin is form DEpage otherwise calls 'serAC.refreshData' to get the refreshed
 	 * search result forwards the page back to search page with refreshed list after updating. If ret is not null stores
 	 * the statusMessage as error message in session and forwards the page back to 'EditDECPage.jsp' for Edit.
 	 *
 	 * @throws Exception
 	 */
 	private void doUpdateDECAction() throws Exception
 	{
 		HttpSession session = m_classReq.getSession();
 		DEC_Bean DECBean = (DEC_Bean) session.getAttribute("m_DEC");
 		DEC_Bean oldDECBean = (DEC_Bean) session.getAttribute("oldDECBean");
 		//String sMenu = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
 		InsACService insAC = new InsACService(m_classReq, m_classRes, this);
 		// doInsertDECBlocks(m_classReq, m_classRes, null); //insert any building blocks from Thesaurus first
 		// udpate the status message with DEC name and ID
 		storeStatusMsg("Data Element Concept Name : " + DECBean.getDEC_LONG_NAME());
 		storeStatusMsg("Public ID : " + DECBean.getDEC_DEC_ID());
 		// call stored procedure to update attributes
 		String ret = insAC.doSetDEC("UPD", DECBean, "Edit", oldDECBean);
 		// after succcessful update
 		if ((ret == null) || ret.equals(""))
 		{
 			String sOriginAction = (String) session.getAttribute("originAction");
 			// forward page back to EditDE
 			if (sOriginAction.equals("editDECfromDE"))
 			{
 				DE_Bean DEBean = (DE_Bean) session.getAttribute("m_DE");
 				DEBean.setDE_DEC_IDSEQ(DECBean.getDEC_DEC_IDSEQ());
 				DEBean.setDE_DEC_NAME(DECBean.getDEC_LONG_NAME());
 				// reset the attributes
 				DataManager.setAttribute(session, "originAction", "");
 				// add DEC Bean into DE BEan
 				DEBean.setDE_DEC_Bean(DECBean);
 				CurationServlet deServ = (DataElementServlet) getACServlet("DataElement");
 				DEBean = (DE_Bean) deServ.getACNames("new", "editDEC", DEBean); 
 				DataManager.setAttribute(session, "m_DE", DEBean);
 				ForwardJSP(m_classReq, m_classRes, "/EditDEPage.jsp");
 			}
 			// go to search page with refreshed list
 			else
 			{
 				DECBean.setDEC_ALIAS_NAME(DECBean.getDEC_PREFERRED_NAME());
 				DECBean.setDEC_TYPE_NAME("PRIMARY");
 				DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, "editDEC");
 				GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, this);
 				String oldID = DECBean.getDEC_DEC_IDSEQ();
 				serAC.refreshData(m_classReq, m_classRes, null, DECBean, null, null, "Edit", oldID);
 				ForwardJSP(m_classReq, m_classRes, "/SearchResultsPage.jsp");
 			}
 		}
 		// go back to edit page if not successful in update
 		else
 			ForwardJSP(m_classReq, m_classRes, "/EditDECPage.jsp"); // back to DEC Page
 	}
 
 	/**
 	 * creates new record in the database and display the result. Called from 'doInsertDEC' method when the aciton is
 	 * create new DEC from DEPage. Retrieves the session bean m_DEC. calls 'insAC.setDEC' to update the database.
 	 * forwards the page back to create DE page after successful insert.
 	 *
 	 * If ret is not null stores the statusMessage as error message in session and forwards the page back to
 	 * 'createDECPage.jsp' for Edit.
 	 *
 	 * @param sOrigin
 	 *            String value to check where this action originated.
 	 *
 	 * @throws Exception
 	 */
 	private void doInsertDECfromDEAction(String sOrigin)
 	throws Exception
 	{
 		HttpSession session = m_classReq.getSession();
 		InsACService insAC = new InsACService(m_classReq, m_classRes, this);
 		DEC_Bean DECBean = (DEC_Bean) session.getAttribute("m_DEC");
 		DEC_Bean oldDECBean = (DEC_Bean) session.getAttribute("oldDECBean");
 		// String sMenu = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
 		// doInsertDECBlocks(m_classReq, m_classRes, null); //insert any building blocks from Thesaurus first
 		String ret = insAC.doSetDEC("INS", DECBean, "New", oldDECBean);
 		// add new dec attributes to de bean and forward to create de page if success.
 		if ((ret == null) || ret.equals(""))
 		{
 			DE_Bean DEBean = (DE_Bean) session.getAttribute("m_DE");
 			DEBean.setDE_DEC_NAME(DECBean.getDEC_LONG_NAME());
 			DEBean.setDE_DEC_IDSEQ(DECBean.getDEC_DEC_IDSEQ());
 			// add DEC Bean into DE BEan
 			DEBean.setDE_DEC_Bean(DECBean);
 			CurationServlet deServ = (DataElementServlet) getACServlet("DataElement");               
 			DEBean = (DE_Bean) deServ.getACNames("new", "newDEC", DEBean); 
 			DataManager.setAttribute(session, "m_DE", DEBean);
 			// String sContext = (String) session.getAttribute("sDefaultContext");
 			// boolean bNewContext = true;
 			DataManager.setAttribute(session, "originAction", ""); // reset this session attribute
 			if (sOrigin != null && sOrigin.equals("CreateNewDECfromEditDE"))
 				ForwardJSP(m_classReq, m_classRes, "/EditDEPage.jsp");
 			else
 				ForwardJSP(m_classReq, m_classRes, "/CreateDEPage.jsp");
 		}
 		// go back to create dec page if error
 		else
 		{
 			DataManager.setAttribute(session, "DECPageAction", "validate");
 			ForwardJSP(m_classReq, m_classRes, "/CreateDECPage.jsp"); // send it back to dec page
 		}
 	}
 
 	/**
 	 * creates new record in the database and display the result. Called from 'doInsertDEC' method when the aciton is
 	 * create new DEC from Menu. Retrieves the session bean m_DEC. calls 'insAC.setVD' to update the database. calls
 	 * 'serAC.refreshData' to get the refreshed search result for template/version forwards the page back to create DEC
 	 * page if new DEC or back to search page if template or version after successful insert.
 	 *
 	 * If ret is not null stores the statusMessage as error message in session and forwards the page back to
 	 * 'createDECPage.jsp' for Edit.
 	 *
 	 * @throws Exception
 	 */
 	private void doInsertDECfromMenuAction() throws Exception
 	{
 		HttpSession session = m_classReq.getSession();
 		InsACService insAC = new InsACService(m_classReq, m_classRes, this);
 		GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, this);
 		String ret = "";
 		//  String ret2 = "";
 		boolean isUpdateSuccess = true;
 		String sMenuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
 		DEC_Bean DECBean = (DEC_Bean) session.getAttribute("m_DEC");
 		DEC_Bean oldDECBean = (DEC_Bean) session.getAttribute("oldDECBean");
 		// doInsertDECBlocks(m_classReq, m_classRes, null); //insert any building blocks from Thesaurus first
 		if (sMenuAction.equals("NewDECVersion"))
 		{
 			logger.info("**************called at 1513 of DECs.java");
 			// udpate the status message with DEC name and ID
 			storeStatusMsg("Data Element Concept Name : " + DECBean.getDEC_LONG_NAME());
 			storeStatusMsg("Public ID : " + DECBean.getDEC_DEC_ID());
 			// creates new version first and updates all other attributes
 			ret = insAC.setAC_VERSION(null, DECBean, null, "DataElementConcept");
 			if (ret == null || ret.equals(""))
 			{
 				// update other attributes
 				ret = insAC.doSetDEC("UPD", DECBean, "Version", oldDECBean);
 				// resetEVSBeans(m_classReq, m_classRes);
 				if (ret != null && !ret.equals(""))
 				{
 					// DataManager.setAttribute(session, "statusMessage", ret + " - Created new version but unable to update
 					// attributes successfully.");
 					// add newly created row to searchresults and send it to edit page for update
 					isUpdateSuccess = false;
 					// put back old attributes except version, idseq and workflow status
 					String oldID = oldDECBean.getDEC_DEC_IDSEQ();
 					String newID = DECBean.getDEC_DEC_IDSEQ();
 					String newVersion = DECBean.getDEC_VERSION();
 					DECBean = DECBean.cloneDEC_Bean(oldDECBean);
 					DECBean.setDEC_DEC_IDSEQ(newID);
 					DECBean.setDEC_VERSION(newVersion);
 					DECBean.setDEC_ASL_NAME("DRAFT MOD");
 					// add newly created dec into the resultset.
 					serAC.refreshData(m_classReq, m_classRes, null, DECBean, null, null, "Version", oldID);
 				}
 			}
 			else
 				storeStatusMsg("\\t " + ret + " - Unable to create new version successfully.");
 		}
 		else
 		{
 			logger.info("**************doSetDEC called at 1546 of DECs.java");
 			ret = insAC.doSetDEC("INS", DECBean, "New", oldDECBean);
 		}
 		
 		if ((ret == null) || ret.equals(""))
 		{
 			logger.info("**************called at 1551 of DECs.java");
 			// DataManager.setAttribute(session, "statusMessage", "New Data Element Concept is created successfully.");
 			DataManager.setAttribute(session, "DECPageAction", "nothing");
 			// String sContext = (String) session.getAttribute("sDefaultContext");
 			// boolean bNewContext = true;
 			DataManager.setAttribute(session, "originAction", ""); // reset this session attribute
 			// forwards to search page with the refreshed list after success if template or version
 			if ((sMenuAction.equals("NewDECTemplate")) || (sMenuAction.equals("NewDECVersion")))
 			{
 				DataManager.setAttribute(session, "searchAC", "DataElementConcept");
 				DECBean.setDEC_ALIAS_NAME(DECBean.getDEC_PREFERRED_NAME());
 				DECBean.setDEC_TYPE_NAME("PRIMARY");
 				String oldID = oldDECBean.getDEC_DEC_IDSEQ();
 				if (sMenuAction.equals("NewDECTemplate"))
 					serAC.refreshData(m_classReq, m_classRes, null, DECBean, null, null, "Template", oldID);
 				else if (sMenuAction.equals("NewDECVersion"))
 					serAC.refreshData(m_classReq, m_classRes, null, DECBean, null, null, "Version", oldID);
 				ForwardJSP(m_classReq, m_classRes, "/SearchResultsPage.jsp");
 			}
 			// go back to create dec page to create another one
 			else
 			{
 				logger.info("**************called at 1573 of DECs.java");
 				DECBean = new DEC_Bean();
 				DECBean.setDEC_ASL_NAME("DRAFT NEW");
 				DECBean.setAC_PREF_NAME_TYPE("SYS");
 				DataManager.setAttribute(session, "m_DEC", DECBean);
 				EVS_Bean m_OC = new EVS_Bean();
 				DataManager.setAttribute(session, "m_OC", m_OC);
 				EVS_Bean m_PC = new EVS_Bean();
 				DataManager.setAttribute(session, "m_PC", m_PC);
 				EVS_Bean m_OCQ = new EVS_Bean();
 				DataManager.setAttribute(session, "m_OCQ", m_OCQ);
 				EVS_Bean m_PCQ = new EVS_Bean();
 				DataManager.setAttribute(session, "m_PCQ", m_PCQ);
 				DataManager.setAttribute(session, "selObjQRow", "");
 				DataManager.setAttribute(session, "selObjRow", "");
 				DataManager.setAttribute(session, "selPropQRow", "");
 				DataManager.setAttribute(session, "selPropRow", "");
 				ForwardJSP(m_classReq, m_classRes, "/CreateDECPage.jsp");
 			}
 		}
 		// go back to create dec page if error occurs
 		else
 		{
 			DataManager.setAttribute(session, "DECPageAction", "validate");
 			// forward to create or edit pages
 			if (isUpdateSuccess == false){
 				logger.info("**************called at 1599 of DECs.java");
 				ForwardJSP(m_classReq, m_classRes, "/SearchResultsPage.jsp");
 			}
 				
 			else{
 				logger.info("**************called at 1604 of DECs.java");
 				ForwardJSP(m_classReq, m_classRes, "/CreateDECPage.jsp");
 			}
 				
 		}
 	}
 
 	/**
 	 * updates bean the selected DEC from the changed values of block edit.
 	 *
 	 * @param DECBeanSR
 	 *            selected DEC bean from search result
 	 * @param dec
 	 *            DEC_Bean of the changed values.
 	 *
 	 * @throws Exception
 	 */
 	private void InsertEditsIntoDECBeanSR(DEC_Bean DECBeanSR, DEC_Bean dec) throws Exception
 	{
 		// get all attributes of DECBean, if attribute != "" then set that attribute of DECBeanSR
 		String sDefinition = dec.getDEC_PREFERRED_DEFINITION();
 		if (sDefinition == null)
 			sDefinition = "";
 		if (!sDefinition.equals(""))
 			DECBeanSR.setDEC_PREFERRED_DEFINITION(sDefinition);
 		String sCD_ID = dec.getDEC_CD_IDSEQ();
 		if (sCD_ID == null)
 			sCD_ID = "";
 		if (!sCD_ID.equals(""))
 			DECBeanSR.setDEC_CD_IDSEQ(sCD_ID);
 		String sCDName = dec.getDEC_CD_NAME();
 		if (sCDName == null)
 			sCDName = "";
 		if (!sCDName.equals("") && !sCDName.equals(null))
 			DECBeanSR.setDEC_CD_NAME(sCDName);
 		//begin===============GF32398========
 		  String RegStatus = dec.getDEC_REG_STATUS();
           if (RegStatus == null)
               RegStatus = "";
           if (!RegStatus.equals(""))
               DECBeanSR.setDEC_REG_STATUS(RegStatus);
         //end=================GF32398========
 		String sBeginDate = dec.getDEC_BEGIN_DATE();
 		if (sBeginDate == null)
 			sBeginDate = "";
 		if (!sBeginDate.equals(""))
 			DECBeanSR.setDEC_BEGIN_DATE(sBeginDate);
 		String sEndDate = dec.getDEC_END_DATE();
 		if (sEndDate == null)
 			sEndDate = "";
 		if (!sEndDate.equals(""))
 			DECBeanSR.setDEC_END_DATE(sEndDate);
 		String sSource = dec.getDEC_SOURCE();
 		if (sSource == null)
 			sSource = "";
 		if (!sSource.equals(""))
 			DECBeanSR.setDEC_SOURCE(sSource);
 		String changeNote = dec.getDEC_CHANGE_NOTE();
 		if (changeNote == null)
 			changeNote = "";
 		if (!changeNote.equals(""))
 			DECBeanSR.setDEC_CHANGE_NOTE(changeNote);
 		//begin===========GF32398======
 		  String oldReg = DECBeanSR.getDEC_REG_STATUS();
           if (oldReg == null)
               oldReg = "";
          //end=========GF32398=========
 		// get cs-csi from the page into the DECBean for block edit
 		Vector<AC_CSI_Bean> vAC_CS = dec.getAC_AC_CSI_VECTOR();
 		if (vAC_CS != null)
 			DECBeanSR.setAC_AC_CSI_VECTOR(vAC_CS);
 		//get the Ref docs from the page into the DEBean for block edit
 		Vector<REF_DOC_Bean> vAC_REF_DOCS = dec.getAC_REF_DOCS();
 		if(vAC_REF_DOCS!=null){
 			Vector<REF_DOC_Bean> temp_REF_DOCS = new Vector<REF_DOC_Bean>();
 			for(REF_DOC_Bean refBean:vAC_REF_DOCS )
 			{
 				if(refBean.getAC_IDSEQ() == DECBeanSR.getDEC_DEC_IDSEQ())
 				{
 					temp_REF_DOCS.add(refBean);
 				}
 			}
 			DECBeanSR.setAC_REF_DOCS(temp_REF_DOCS);
 		}
 		String sOCL = dec.getDEC_OCL_NAME();
 		if (sOCL == null)
 			sOCL = "";
 		if (!sOCL.equals(""))
 		{
 			DECBeanSR.setDEC_OCL_NAME(sOCL);
 			String sOCCondr = dec.getDEC_OC_CONDR_IDSEQ();
 			if (sOCCondr == null)
 				sOCCondr = "";
 			if (!sOCCondr.equals(""))
 				DECBeanSR.setDEC_OC_CONDR_IDSEQ(sOCCondr);
 			String sOCL_IDSEQ = dec.getDEC_OCL_IDSEQ();
 			if (sOCL_IDSEQ != null && !sOCL_IDSEQ.equals(""))
 				DECBeanSR.setDEC_OCL_IDSEQ(sOCL_IDSEQ);
 		}
 		String sPropL = dec.getDEC_PROPL_NAME();
 		if (sPropL == null)
 			sPropL = "";
 		if (!sPropL.equals(""))
 		{
 			DECBeanSR.setDEC_PROPL_NAME(sPropL);
 			String sPCCondr = dec.getDEC_PROP_CONDR_IDSEQ();
 			if (sPCCondr == null)
 				sPCCondr = "";
 			if (!sPCCondr.equals(""))
 				DECBeanSR.setDEC_PROP_CONDR_IDSEQ(sPCCondr);
 			String sPROPL_IDSEQ = dec.getDEC_PROPL_IDSEQ();
 			if (sPROPL_IDSEQ != null && !sPROPL_IDSEQ.equals(""))
 				DECBeanSR.setDEC_PROPL_IDSEQ(sPROPL_IDSEQ);
 		}
 		// update dec pref type and abbr name
 		DECBeanSR.setAC_PREF_NAME_TYPE(dec.getAC_PREF_NAME_TYPE());
 		String status = dec.getDEC_ASL_NAME();
 		if (status == null)
 			status = "";
 		if (!status.equals(""))
 			DECBeanSR.setDEC_ASL_NAME(status);
 		String version = dec.getDEC_VERSION();
 		String lastVersion = (String) DECBeanSR.getDEC_VERSION();
 		int index = -1;
 		String pointStr = ".";
 		String strWhBegNumber = "";
 		int iWhBegNumber = 0;
 		index = lastVersion.indexOf(pointStr);
 		String strPtBegNumber = lastVersion.substring(0, index);
 		String afterDecimalNumber = lastVersion.substring((index + 1), (index + 2));
 		if (index == 1)
 			strWhBegNumber = "";
 		else if (index == 2)
 		{
 			strWhBegNumber = lastVersion.substring(0, index - 1);
 			Integer WhBegNumber = new Integer(strWhBegNumber);
 			iWhBegNumber = WhBegNumber.intValue();
 		}
 		String strWhEndNumber = ".0";
 		String beforeDecimalNumber = lastVersion.substring((index - 1), (index));
 		String sNewVersion = "";
 		Integer IadNumber = new Integer(0);
 		Integer IbdNumber = new Integer(0);
 		String strIncADNumber = "";
 		String strIncBDNumber = "";
 		if (version == null)
 			version = "";
 		else if (version.equals("Point"))
 		{
 			// Point new version
 			int incrementADNumber = 0;
 			int incrementBDNumber = 0;
 			Integer adNumber = new Integer(afterDecimalNumber);
 			Integer bdNumber = new Integer(strPtBegNumber);
 			int iADNumber = adNumber.intValue(); // after decimal
 			int iBDNumber = bdNumber.intValue(); // before decimal
 			if (iADNumber != 9)
 			{
 				incrementADNumber = iADNumber + 1;
 				IadNumber = new Integer(incrementADNumber);
 				strIncADNumber = IadNumber.toString();
 				sNewVersion = strPtBegNumber + "." + strIncADNumber; // + strPtEndNumber;
 			}
 			else
 				// adNumber == 9
 			{
 				incrementADNumber = 0;
 				incrementBDNumber = iBDNumber + 1;
 				IbdNumber = new Integer(incrementBDNumber);
 				strIncBDNumber = IbdNumber.toString();
 				IadNumber = new Integer(incrementADNumber);
 				strIncADNumber = IadNumber.toString();
 				sNewVersion = strIncBDNumber + "." + strIncADNumber; // + strPtEndNumber;
 			}
 			DECBeanSR.setDEC_VERSION(sNewVersion);
 		}
 		else if (version.equals("Whole"))
 		{
 			// Whole new version
 			Integer bdNumber = new Integer(beforeDecimalNumber);
 			int iBDNumber = bdNumber.intValue();
 			int incrementBDNumber = iBDNumber + 1;
 			if (iBDNumber != 9)
 			{
 				IbdNumber = new Integer(incrementBDNumber);
 				strIncBDNumber = IbdNumber.toString();
 				sNewVersion = strWhBegNumber + strIncBDNumber + strWhEndNumber;
 			}
 			else
 				// before decimal number == 9
 				{
 				int incrementWhBegNumber = iWhBegNumber + 1;
 				Integer IWhBegNumber = new Integer(incrementWhBegNumber);
 				String strIncWhBegNumber = IWhBegNumber.toString();
 				IbdNumber = new Integer(0);
 				strIncBDNumber = IbdNumber.toString();
 				sNewVersion = strIncWhBegNumber + strIncBDNumber + strWhEndNumber;
 				}
 			DECBeanSR.setDEC_VERSION(sNewVersion);
 		}
 	}
 
 	/**
 	 * update record in the database and display the result. Called from 'doInsertDEC' method when the aciton is
 	 * editing. Retrieves the session bean m_DEC. calls 'insAC.setDEC' to update the database. otherwise calls
 	 * 'serAC.refreshData' to get the refreshed search result forwards the page back to search page with refreshed list
 	 * after updating.
 	 *
 	 * If ret is not null stores the statusMessage as error message in session and forwards the page back to
 	 * 'EditDECPage.jsp' for Edit.
 	 *
 	 * @throws Exception
 	 */
 	private void doUpdateDECActionBE() throws Exception
 	{
 		HttpSession session = m_classReq.getSession();
 		DEC_Bean DECBean = (DEC_Bean) session.getAttribute("m_DEC");
 		DataManager.setAttribute(session, "DECEditAction", ""); // reset this
 		boolean isRefreshed = false;
 		String ret = ":";
 		InsACService insAC = new InsACService(m_classReq, m_classRes, this);
 		GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, this);
 		GetACService getAC = new GetACService(m_classReq, m_classRes, this);
 		// String sNewOC = (String)session.getAttribute("newObjectClass");
 		// String sNewProp = (String)session.getAttribute("newProperty");
 		Vector vBERows = (Vector) session.getAttribute("vBEResult");
 		int vBESize = vBERows.size();
 		Integer vBESize2 = new Integer(vBESize);
 		m_classReq.setAttribute("vBESize", vBESize2);
 		// String sOC_IDSEQ = "";
 		// String sProp_IDSEQ = "";
 		if (vBERows.size() > 0)
 		{
 			// Be sure the buffer is loaded when doing versioning.
 			String newVersion = DECBean.getDEC_VERSION();
 			if (newVersion == null)
 				newVersion = "";
 			boolean newVers = (newVersion.equals("Point") || newVersion.equals("Whole"));
 			if (newVers)
 			{
 				@SuppressWarnings("unchecked")
 				Vector<AC_Bean> tvec = vBERows;
 				AltNamesDefsSession.loadAsNew(this, session, tvec);
 			}
 			for (int i = 0; i < (vBERows.size()); i++)
 			{
 				DEC_Bean DECBeanSR = new DEC_Bean();
 				DECBeanSR = (DEC_Bean) vBERows.elementAt(i);
 				DEC_Bean oldDECBean = new DEC_Bean();
 				oldDECBean = oldDECBean.cloneDEC_Bean(DECBeanSR);
 				//  String oldName = (String) DECBeanSR.getDEC_PREFERRED_NAME();
 				// gets all the changed attrributes from the page
 				InsertEditsIntoDECBeanSR(DECBeanSR, DECBean);
 				// DataManager.setAttribute(session, "m_DEC", DECBeanSR);
 				String oldID = oldDECBean.getDEC_DEC_IDSEQ();
 				// udpate the status message with DEC name and ID
 				storeStatusMsg("Data Element Concept Name : " + DECBeanSR.getDEC_LONG_NAME());
 				storeStatusMsg("Public ID : " + DECBeanSR.getDEC_DEC_ID());
 				// creates a new version
 				if (newVers) // block version
 				{
 					// creates new version first and updates all other attributes
 					String strValid = m_setAC.checkUniqueInContext("Version", "DEC", null, DECBeanSR, null, getAC,
 					"version");
 					if (strValid != null && !strValid.equals(""))
 						ret = "unique constraint";
 					else
 						ret = insAC.setAC_VERSION(null, DECBeanSR, null, "DataElementConcept");
 					if (ret == null || ret.equals(""))
 					{
 						ret = insAC.doSetDEC("UPD", DECBeanSR, "BlockVersion", oldDECBean);
 						// resetEVSBeans(m_classReq, m_classRes);
 						// add this bean into the session vector
 						if (ret == null || ret.equals(""))
 						{
 							serAC.refreshData(m_classReq, m_classRes, null, DECBeanSR, null, null, "Version", oldID);
 							isRefreshed = true;
 							// reset the appened attributes to remove all the checking of the row
 							Vector vCheck = new Vector();
 							DataManager.setAttribute(session, "CheckList", vCheck);
 							DataManager.setAttribute(session, "AppendAction", "Not Appended");
 						}
 					}
 					// alerady exists
 					else if (ret.indexOf("unique constraint") >= 0)
 						storeStatusMsg("\\t The version " + DECBeanSR.getDEC_VERSION()
 								+ " already exists in the data base.\\n");
 					// some other problem
 					else
 						storeStatusMsg("\\t " + ret + " : Unable to create new version "
 								+ DECBeanSR.getDEC_VERSION() + ".\\n");
 				}
 				else
 					// block edit
 				{
 					ret = insAC.doSetDEC("UPD", DECBeanSR, "BlockEdit", oldDECBean);
 					// forward to search page with refreshed list after successful update
 					if ((ret == null) || ret.equals(""))
 					{
 						serAC.refreshData(m_classReq, m_classRes, null, DECBeanSR, null, null, "Edit", oldID);
 						isRefreshed = true;
 					}
 				}
 			}
 			AltNamesDefsSession.blockSave(this, session);
 		}
 		// to get the final result vector if not refreshed at all
 		if (!(isRefreshed))
 		{
 			Vector<String> vResult = new Vector<String>();
 			serAC.getDECResult(m_classReq, m_classRes, vResult, "");
 			DataManager.setAttribute(session, "results", vResult); // store the final result in the session
 			DataManager.setAttribute(session, "DECPageAction", "nothing");
 		}
 		// forward to search page.
 		ForwardJSP(m_classReq, m_classRes, "/SearchResultsPage.jsp");
 	}
 
 	/**
 	 * The doOpenCreateDECPage method gets the session, gets some values from the createDE page and stores in bean m_DE,
 	 * sets some session attributes, then forwards to CreateDEC page
 	 *
 	 * @throws Exception
 	 */
 	public void doOpenCreateDECPage() throws Exception
 	{
 		HttpSession session = m_classReq.getSession();
 		// DataManager.setAttribute(session, "originAction", fromWhere); //"CreateNewDECfromCreateDE");
 		DE_Bean m_DE = (DE_Bean) session.getAttribute("m_DE");
 		if (m_DE == null)
 			m_DE = new DE_Bean();
 		m_setAC.setDEValueFromPage(m_classReq, m_classRes, m_DE); // store DEC bean
 		DataManager.setAttribute(session, "m_DE", m_DE);
 		DEC_Bean m_DEC = new DEC_Bean();
 		m_DEC.setDEC_ASL_NAME("DRAFT NEW");
 		m_DEC.setAC_PREF_NAME_TYPE("SYS");
 		DataManager.setAttribute(session, "m_DEC", m_DEC);
 		DEC_Bean oldDEC = new DEC_Bean();
 		oldDEC = oldDEC.cloneDEC_Bean(m_DEC);
 		DataManager.setAttribute(session, "oldDECBean", oldDEC);
 		this.clearCreateSessionAttributes(m_classReq, m_classRes); // clear some session attributes
 		// DataManager.setAttribute(session, "oldDECBean", m_DEC);
 		ForwardJSP(m_classReq, m_classRes, "/CreateDECPage.jsp");
 	}
 
 	/**
 	 *
 	 * @throws Exception
 	 *
 	 */
 	@SuppressWarnings("unchecked")
 	private void doRemoveBuildingBlocks() throws Exception
 	{
 		HttpSession session = m_classReq.getSession();
 		String sSelRow = "";
 		DEC_Bean m_DEC = (DEC_Bean) session.getAttribute("m_DEC");
 		if (m_DEC == null)
 			m_DEC = new DEC_Bean();
 		Vector<EVS_Bean> vObjectClass = (Vector) session.getAttribute("vObjectClass");
 		if (vObjectClass == null)
 			vObjectClass = new Vector<EVS_Bean>();
 			//    int iOCSize = vObjectClass.size();
 			Vector<EVS_Bean> vProperty = (Vector) session.getAttribute("vProperty");
 			if (vProperty == null)
 				vProperty = new Vector<EVS_Bean>();
 				//   int iPropSize = vProperty.size();
 				String sComp = (String) m_classReq.getParameter("sCompBlocks");
 				if (sComp == null)
 					sComp = "";
 				if (sComp.equals("ObjectClass"))
 				{
 					EVS_Bean m_OC = new EVS_Bean();
 					vObjectClass.setElementAt(m_OC, 0);
 					DataManager.setAttribute(session, "vObjectClass", vObjectClass);
 					m_DEC.setDEC_OCL_NAME_PRIMARY("");
 					m_DEC.setDEC_OC_CONCEPT_CODE("");
 					m_DEC.setDEC_OC_EVS_CUI_ORIGEN("");
 					m_DEC.setDEC_OCL_IDSEQ("");
 					DataManager.setAttribute(session, "RemoveOCBlock", "true");
 					DataManager.setAttribute(session, "newObjectClass", "true");
 				}
 				else if (sComp.equals("Property") || sComp.equals("PropertyClass"))
 				{
 					EVS_Bean m_PC = new EVS_Bean();
 					vProperty.setElementAt(m_PC, 0);
 					DataManager.setAttribute(session, "vProperty", vProperty);
 					m_DEC.setDEC_PROPL_NAME_PRIMARY("");
 					m_DEC.setDEC_PROP_CONCEPT_CODE("");
 					m_DEC.setDEC_PROP_EVS_CUI_ORIGEN("");
 					m_DEC.setDEC_PROPL_IDSEQ("");
 					DataManager.setAttribute(session, "RemovePropBlock", "true");
 					DataManager.setAttribute(session, "newProperty", "true");
 				}
 				else if (sComp.equals("ObjectQualifier"))
 				{
 					sSelRow = (String) m_classReq.getParameter("selObjQRow");
 					if (sSelRow != null && !(sSelRow.equals("")))
 					{
 						Integer intObjRow = new Integer(sSelRow);
 						int intObjRow2 = intObjRow.intValue();
 						// add 1 because 0 element is OC, not a qualifier
 						int int1 = intObjRow2 + 1;
 						if (vObjectClass.size() > (int1))
 						{
 							vObjectClass.removeElementAt(int1);
 							DataManager.setAttribute(session, "vObjectClass", vObjectClass);
 						}
 						// m_DEC.setDEC_OBJ_CLASS_QUALIFIER("");
 						Vector vOCQualifierNames = m_DEC.getDEC_OC_QUALIFIER_NAMES();
 						if (vOCQualifierNames == null)
 							vOCQualifierNames = new Vector();
 						if (vOCQualifierNames.size() > intObjRow2)
 							vOCQualifierNames.removeElementAt(intObjRow2);
 						Vector vOCQualifierCodes = m_DEC.getDEC_OC_QUALIFIER_CODES();
 						if (vOCQualifierCodes == null)
 							vOCQualifierCodes = new Vector();
 						if (vOCQualifierCodes.size() > intObjRow2)
 							vOCQualifierCodes.removeElementAt(intObjRow2);
 						Vector vOCQualifierDB = m_DEC.getDEC_OC_QUALIFIER_DB();
 						if (vOCQualifierDB == null)
 							vOCQualifierDB = new Vector();
 						if (vOCQualifierDB.size() > intObjRow2)
 							vOCQualifierDB.removeElementAt(intObjRow2);
 						m_DEC.setDEC_OC_QUALIFIER_NAMES(vOCQualifierNames);
 						m_DEC.setDEC_OC_QUALIFIER_CODES(vOCQualifierCodes);
 						m_DEC.setDEC_OC_QUALIFIER_DB(vOCQualifierDB);
 						m_DEC.setDEC_OCL_IDSEQ("");
 						DataManager.setAttribute(session, "RemoveOCBlock", "true");
 						DataManager.setAttribute(session, "newObjectClass", "true");
 						DataManager.setAttribute(session, "m_OCQ", null);
 
 					}
 				}
 				else if (sComp.equals("PropertyQualifier"))
 				{
 					sSelRow = (String) m_classReq.getParameter("selPropQRow");
 					if (sSelRow != null && !(sSelRow.equals("")))
 					{
 						Integer intPropRow = new Integer(sSelRow);
 						int intPropRow2 = intPropRow.intValue();
 						// add 1 because 0 element is OC, not a qualifier
 						int int1 = intPropRow2 + 1;
 						// invert because the list on ui is i9nverse to vector
 						if (vProperty.size() > (int1))
 						{
 							vProperty.removeElementAt(int1);
 							DataManager.setAttribute(session, "vProperty", vProperty);
 						}
 						// m_DEC.setDEC_PROPERTY_QUALIFIER("");
 						Vector vPropQualifierNames = m_DEC.getDEC_PROP_QUALIFIER_NAMES();
 						if (vPropQualifierNames == null)
 							vPropQualifierNames = new Vector();
 						if (vPropQualifierNames.size() > intPropRow2)
 							vPropQualifierNames.removeElementAt(intPropRow2);
 						Vector vPropQualifierCodes = m_DEC.getDEC_PROP_QUALIFIER_CODES();
 						if (vPropQualifierCodes == null)
 							vPropQualifierCodes = new Vector();
 						if (vPropQualifierCodes.size() > intPropRow2)
 							vPropQualifierCodes.removeElementAt(intPropRow2);
 						Vector vPropQualifierDB = m_DEC.getDEC_PROP_QUALIFIER_DB();
 						if (vPropQualifierDB == null)
 							vPropQualifierDB = new Vector();
 						if (vPropQualifierDB.size() > intPropRow2)
 							vPropQualifierDB.removeElementAt(intPropRow2);
 						m_DEC.setDEC_PROP_QUALIFIER_NAMES(vPropQualifierNames);
 						m_DEC.setDEC_PROP_QUALIFIER_CODES(vPropQualifierCodes);
 						m_DEC.setDEC_PROP_QUALIFIER_DB(vPropQualifierDB);
 						m_DEC.setDEC_PROPL_IDSEQ("");
 						DataManager.setAttribute(session, "RemovePropBlock", "true");
 						DataManager.setAttribute(session, "newObjectClass", "true");
 						DataManager.setAttribute(session, "m_PCQ", null);
 					}
 				}
 				if ( sComp.equals("ObjectClass") || sComp.equals("ObjectQualifier")){
 					vObjectClass = (Vector)session.getAttribute("vObjectClass");
 					logger.debug("at line 2225 of DECServlet.java"+vObjectClass.size());
 					if (vObjectClass != null && vObjectClass.size()>0){
 						vObjectClass = this.getMatchingThesarusconcept(vObjectClass, "Object Class");
 						//begin of GF30798
 						for (int i=0; i<vObjectClass.size();i++){
 							EVS_Bean eBean =(EVS_Bean)vObjectClass.get(i);
 							logger.debug("At line 2229 of DECServlet.java "+eBean.getPREFERRED_DEFINITION()+"**"+eBean.getLONG_NAME()+"**"+eBean.getCONCEPT_IDENTIFIER()+"**"+vObjectClass.size());
 						}
 						//end of GF30798
 						m_DEC = this.updateOCAttribues(vObjectClass, m_DEC);
 						
 						this.checkChosenConcepts(session, null, null, vObjectClass, "OC");
 					} 
 					DataManager.setAttribute(session, "vObjectClass", vObjectClass);
 				}
 				if (sComp.equals("Property") || sComp.equals("PropertyClass") || sComp.equals("PropertyQualifier")){
 					vProperty = (Vector)session.getAttribute("vProperty");
 					if (vProperty != null && vProperty.size()>0){
 						vProperty = this.getMatchingThesarusconcept(vProperty, "Property");
 						m_DEC = this.updatePropAttribues(vProperty, m_DEC);
 						
 						this.checkChosenConcepts(session,null, null, vProperty, "Prop");
 					} 
 					DataManager.setAttribute(session, "vProperty", vProperty);
 				}
 
 				m_setAC.setDECValueFromPage(m_classReq, m_classRes, m_DEC);
 				DataManager.setAttribute(session, "m_DEC", m_DEC);
 	} // end of doRemoveQualifier
 
 	public void doOpenViewPage() throws Exception
 	{
 		//System.out.println("I am here open view page");
 		HttpSession session = m_classReq.getSession();
 		String acID = (String) m_classReq.getAttribute("acIdseq");
 		if (acID.equals(""))
 			acID = m_classReq.getParameter("idseq");
 		Vector<DEC_Bean> vList = new Vector<DEC_Bean>();
 		// get DE's attributes from the database again
 		GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, this);
 		if (acID != null && !acID.equals(""))
 		{
 			serAC.doDECSearch(acID, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", 0, "", "", "",
 					"", "", vList, "0");//===gf32398== added one more parameter regstatus
 		}
 		if (vList.size() > 0) // get all attributes
 		{
 			DEC_Bean DECBean = (DEC_Bean) vList.elementAt(0);
 			DECBean = serAC.getDECAttributes(DECBean, "openView", "viewDEC");
 			m_classReq.setAttribute("viewDECId", DECBean.getIDSEQ());
 			String viewDEC = "viewDEC" + DECBean.getIDSEQ();
 			DataManager.setAttribute(session, viewDEC, DECBean);
 			String title = "CDE Curation View DEC "+DECBean.getDEC_LONG_NAME()+ " [" +  DECBean.getDEC_DEC_ID() + "v" + DECBean.getDEC_VERSION() +"]";
 			m_classReq.setAttribute("title", title);
 			m_classReq.setAttribute("publicID", DECBean.getDEC_DEC_ID());
 			m_classReq.setAttribute("version", DECBean.getDEC_VERSION());
 			m_classReq.setAttribute("IncludeViewPage", "EditDEC.jsp") ;
 		}
 	}
 
 
 	private DEC_Bean updateOCAttribues(Vector vObjectClass, DEC_Bean decBean) {
 
 		HttpSession session = m_classReq.getSession();
 		// add oc primary attributes to the dec bean
 		EVS_Bean pBean =(EVS_Bean)vObjectClass.get(0); 
 		String nvpValue = "";
 		if (checkNVP(pBean))	//JT what is this check for?
 			nvpValue="::"+pBean.getNVP_CONCEPT_VALUE();
 		if (pBean.getLONG_NAME() != null)
 			decBean.setDEC_OCL_NAME_PRIMARY(pBean.getLONG_NAME()+nvpValue);
 		decBean.setDEC_OC_CONCEPT_CODE(pBean.getCONCEPT_IDENTIFIER());
 		decBean.setDEC_OC_EVS_CUI_ORIGEN(pBean.getEVS_DATABASE());
 		//if (pBean.getIDSEQ() != null && pBean.getIDSEQ().length() > 0)
 		//	  decBean.setDEC_OCL_IDSEQ(pBean.getIDSEQ());
 		logger.debug("At line 2218 of DECServlet.java"+decBean.getDEC_OCL_NAME_PRIMARY()+"**"+decBean.getDEC_OC_CONCEPT_CODE()+"**"+decBean.getDEC_OC_EVS_CUI_ORIGEN()+"**"+pBean.getCONTEXT_NAME()+"**"+pBean.getPREFERRED_DEFINITION());
 		DataManager.setAttribute(session, "m_OC", pBean);
 
 		// update qualifier vectors
 		decBean.setDEC_OC_QUALIFIER_NAMES(null);
 		decBean.setDEC_OC_QUALIFIER_CODES(null);
 		decBean.setDEC_OC_QUALIFIER_DB(null);
 		for (int i=1; i<vObjectClass.size();i++){
 			EVS_Bean eBean =(EVS_Bean)vObjectClass.get(i);
 			nvpValue = "";
 			if (checkNVP(eBean))
 				nvpValue="::"+eBean.getNVP_CONCEPT_VALUE();
 			// update qualifier vectors
 			// add it othe qualifiers attributes of the selected DEC
 			Vector<String> vOCQualifierNames = decBean.getDEC_OC_QUALIFIER_NAMES();
 			if (vOCQualifierNames == null)
 				vOCQualifierNames = new Vector<String>();
 			vOCQualifierNames.addElement(eBean.getLONG_NAME()+nvpValue);
 			Vector<String> vOCQualifierCodes = decBean.getDEC_OC_QUALIFIER_CODES();
 			if (vOCQualifierCodes == null)
 				vOCQualifierCodes = new Vector<String>();
 			vOCQualifierCodes.addElement(eBean.getCONCEPT_IDENTIFIER());
 			Vector<String> vOCQualifierDB = decBean.getDEC_OC_QUALIFIER_DB();
 			if (vOCQualifierDB == null)
 				vOCQualifierDB = new Vector<String>();
 			vOCQualifierDB.addElement(eBean.getEVS_DATABASE());
 			decBean.setDEC_OC_QUALIFIER_NAMES(vOCQualifierNames);
 			decBean.setDEC_OC_QUALIFIER_CODES(vOCQualifierCodes);
 			decBean.setDEC_OC_QUALIFIER_DB(vOCQualifierDB);
 			logger.debug("At line 2247 of DECServlet.java"+Arrays.asList(vOCQualifierNames)+"**"+Arrays.asList(vOCQualifierCodes)+"**"+Arrays.asList(vOCQualifierDB));
 			//           if (vOCQualifierNames.size()>0)
 				//           decBean.setDEC_OBJ_CLASS_QUALIFIER((String)vOCQualifierNames.elementAt(0));
 			// store it in the session
 			DataManager.setAttribute(session, "m_OCQ", eBean);
 		}
 		return decBean;  
 	}
 	private DEC_Bean updatePropAttribues(Vector vProperty, DEC_Bean decBean) {
 
 		HttpSession session = m_classReq.getSession();
 		// add prop primary attributes to the dec bean
 		EVS_Bean pBean =(EVS_Bean)vProperty.get(0); 
 		String nvpValue = "";
 		if (checkNVP(pBean))
 			nvpValue="::"+pBean.getNVP_CONCEPT_VALUE();
 		if (pBean.getLONG_NAME() != null)
 			decBean.setDEC_PROPL_NAME_PRIMARY(pBean.getLONG_NAME()+nvpValue);
 		decBean.setDEC_PROP_CONCEPT_CODE(pBean.getCONCEPT_IDENTIFIER());
 		decBean.setDEC_PROP_EVS_CUI_ORIGEN(pBean.getEVS_DATABASE());
 		//decBean.setDEC_PROPL_IDSEQ(pBean.getIDSEQ());
 		DataManager.setAttribute(session, "m_PC", pBean);
 		logger.debug("At line 2269 of DECServlet.java"+decBean.getDEC_PROPL_NAME_PRIMARY()+"**"+decBean.getDEC_PROP_CONCEPT_CODE()+"**"+decBean.getDEC_PROP_EVS_CUI_ORIGEN()+"**"+pBean.getCONTEXT_NAME()+"**"+pBean.getPREFERRED_DEFINITION());
 
 		// update qualifier vectors
 		decBean.setDEC_PROP_QUALIFIER_NAMES(null);
 		decBean.setDEC_PROP_QUALIFIER_CODES(null);
 		decBean.setDEC_PROP_QUALIFIER_DB(null);
 		for (int i=1; i<vProperty.size();i++){
 			EVS_Bean eBean =(EVS_Bean)vProperty.get(i);
 			nvpValue="";
 			if (checkNVP(eBean))
 				nvpValue="::"+eBean.getNVP_CONCEPT_VALUE();
 			// update qualifier vectors
 			// add it the qualifiers attributes of the selected DEC
 			Vector<String> vPropQualifierNames = decBean.getDEC_PROP_QUALIFIER_NAMES();
 			if (vPropQualifierNames == null)
 				vPropQualifierNames = new Vector<String>();
 			vPropQualifierNames.addElement(eBean.getLONG_NAME()+nvpValue);
 			Vector<String> vPropQualifierCodes = decBean.getDEC_PROP_QUALIFIER_CODES();
 			if (vPropQualifierCodes == null)
 				vPropQualifierCodes = new Vector<String>();
 			vPropQualifierCodes.addElement(eBean.getCONCEPT_IDENTIFIER());
 			Vector<String> vPropQualifierDB = decBean.getDEC_PROP_QUALIFIER_DB();
 			if (vPropQualifierDB == null)
 				vPropQualifierDB = new Vector<String>();
 			vPropQualifierDB.addElement(eBean.getEVS_DATABASE());
 			decBean.setDEC_PROP_QUALIFIER_NAMES(vPropQualifierNames);
 			decBean.setDEC_PROP_QUALIFIER_CODES(vPropQualifierCodes);
 			decBean.setDEC_PROP_QUALIFIER_DB(vPropQualifierDB);
 			logger.debug("At line 2253 of DECServlet.java"+Arrays.asList(vPropQualifierNames)+"**"+Arrays.asList(vPropQualifierCodes)+"**"+Arrays.asList(vPropQualifierDB));
 			// if(vPropQualifierNames.size()>0)
 				// decBean.setDEC_PROPERTY_QUALIFIER((String)vPropQualifierNames.elementAt(0));
 			DataManager.setAttribute(session, "m_PCQ", eBean);
 		}
 		return decBean;  
 	}
 
 	public static boolean checkNVP(EVS_Bean eCon) {
 
 		return (eCon.getNAME_VALUE_PAIR_IND() > 0 && eCon.getLONG_NAME().indexOf("::") < 1 && eCon.getNVP_CONCEPT_VALUE().length() > 0);	//JT not sure what is this checking for, second portion could be buggy!!!
 	}
 }
