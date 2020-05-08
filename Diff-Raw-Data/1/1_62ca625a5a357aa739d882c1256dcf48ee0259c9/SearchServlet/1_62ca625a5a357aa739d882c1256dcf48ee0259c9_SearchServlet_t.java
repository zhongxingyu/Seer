 package gov.nih.nci.cadsr.cdecurate.tool;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Vector;
 
 import gov.nih.nci.cadsr.cdecurate.database.SQLHelper;
 import gov.nih.nci.cadsr.cdecurate.ui.DesDEServlet;
 import gov.nih.nci.cadsr.cdecurate.util.DataManager;
 import gov.nih.nci.cadsr.cdecurate.util.ToolURL;
 import gov.nih.nci.cadsr.persist.ac.Admin_Components_Mgr;
 import gov.nih.nci.cadsr.persist.exception.DBException;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 public class SearchServlet extends CurationServlet {
 
 
 	public SearchServlet() {
 		super();
 	}
 	public SearchServlet(HttpServletRequest req, HttpServletResponse res,
 			ServletContext sc) {
 		super(req, res, sc);
 	}
 	
 	public void execute(ACRequestTypes reqType) throws Exception {	
 		
 		switch (reqType){
 			case homePage:
 				doHomePage();
 				break;			
 			case searchACs:
 				doGetACSearchActions(); 
 				break;
 			case showResult:
 				doSearchResultsAction();
                 break;
 			case showBEDisplayResult:
 				doDisplayWindowBEAction();
                 break;
 			case showDECDetail:
 				doDECDetailDisplay();
                 break;
 			case doSortCDE:
 				doSortACActions();
 				break;			
 			case doSortBlocks:
 				doSortBlockActions();
 				break;			
 			case getSearchFilter:
 				doOpenSearchPage();
 				break;			
 			case searchBlocks:
 				doBlockSearchActions();
 				break;			
 			case actionFromMenu:
 				doMenuAction();
 				break;			
 			case getRefDocument:
                 doRefDocSearchActions();
                 break;
 			case getAltNames:
                 doAltNameSearchActions();
                 break;
 			case getPermValue:
                doPermValueSearchActions();
                break;
 			case getProtoCRF:
                doProtoCRFSearchActions();
                break;
 			case getConClassForAC:
                doConClassSearchActions();
                break;
 			case showCDDetail:
                doConDomainSearchActions();
                break;
  		}
 	}
 	
     /**
      * The doHomePage method gets the session, set some session attributes, then connects to the database. Called from
      * 'service' method where reqType is 'login', 'homePage' calls 'getAC.getACList' to get the Data list from the
      * database for the selected Context at login page. calls 'doOpenSearchPage' to open the home page.
      *
      */
     public void doHomePage()
     {
         try
         {
             HttpSession session = m_classReq.getSession();
             DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, "nothing");
             DataManager.setAttribute(session, "originAction", "nothing");
             DataManager.setAttribute(session, "DDEAction", "nothing"); // to separate from DDE with simple de
             DataManager.setAttribute(session, "VMMeaning", "nothing");
             DataManager.setAttribute(session, "ConnectedToDB", "nothing");
             m_classReq.setAttribute("UISearchType", "nothing");
             DataManager.setAttribute(session, "OpenTreeToConcept", "false");
             DataManager.setAttribute(session, "strHTML", "");
             DataManager.setAttribute(session, "creSearchAC", "");
             DataManager.setAttribute(session, "searchAC", "DataElement");
             DataManager.setAttribute(session, "ParentConcept", "");
             DataManager.setAttribute(session, "SelectedParentName", "");
             DataManager.setAttribute(session, "SelectedParentCC", "");
             DataManager.setAttribute(session, "SelectedParentDB", "");
             DataManager.setAttribute(session, "SelectedParentMetaSource", "");
             DataManager.setAttribute(session, "ConceptLevel", "0");
             DataManager.setAttribute(session, "sDefaultStatus", "DRAFT NEW");
             DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, "");
             //String Username = m_classReq.getParameter("Username");
             //String Password = m_classReq.getParameter("Password");
             UserBean userbean= (UserBean) session.getAttribute("Userbean");
             //Userbean.setUsername(Username);
             //Userbean.setPassword(Password);
             if (userbean != null)
             {
                 userbean.setDBAppContext("/cdecurate");
             	DataManager.setAttribute(session, "Userbean", userbean);
             	DataManager.setAttribute(session, "Username", userbean.getUsername());
             }
             sessionData.UsrBean = userbean;
             GetACService getAC = new GetACService(m_classReq, m_classRes, this);
             getAC.verifyConnection(m_classReq, m_classRes);
             // dbcon.verifyConnection(req);
             String ConnectedToDB = (String) session.getAttribute("ConnectedToDB");
             if (ConnectedToDB != null && !ConnectedToDB.equals("No"))
             {
             	if (userbean != null)
             	userbean.setSuperuser(getAC.getSuperUserFlag(userbean.getUsername()));
 
                 // get initial list from cadsr
             	getInitialListFromCadsr(getAC);
                 getAC.getACList(m_classReq, m_classRes, "", true, "ALL");
                 doOpenSearchPage();
                 getCompAttrList("DataElement", "searchForCreate");
                    
                 
                 // get EVS info
                 try
                 {
                     EVS_UserBean eUser = new EVS_UserBean();
                     eUser.getEVSInfoFromDSR(m_classReq, m_classRes, this);
                     EVSSearch evs = new EVSSearch(m_classReq, m_classRes, this);
                     evs.getMetaSources();
                     session.setAttribute("preferredVocab", eUser.getPrefVocab());
                     // m_EVS_CONNECT = euBean.getEVSConURL();
                     // getVocabHandles(m_classReq, m_classRes);
                     // DoHomepageThread thread = new DoHomepageThread(m_classReq, m_classRes, this);
                     // thread.start();
                 }
                 catch (Exception ee)
                 {
                     logger.error("Servlet-doHomePage-evsthread : " + ee.toString(), ee);
                 }
             }
             else
             {
                 DataManager.setAttribute(session, "ConnectedToDB", "nothing"); // was No, so reset value
                 ForwardErrorJSP(m_classReq, m_classRes,
                                 "Problem with login. User name/password may be incorrect, or database connection can not be established.");
                 // ForwardErrorJSP(m_classReq, m_classRes, "Unable to connect to the database. Please log in again.");
             }
         }
         catch (Exception e)
         {
             try
             {
                 // if error, forward to login page to re-enter username and password
                 logger.error("Servlet-doHomePage : " + e.toString(), e);
                 String msg = e.getMessage().substring(0, 12);
                 if (msg.equals("Io exception"))
                     ForwardErrorJSP(m_classReq, m_classRes, "Io Exception. Session Terminated. Please log in again.");
                 else
                     ForwardErrorJSP(m_classReq, m_classRes, "Could not validate the User Name and Password, please try again.");
             }
             catch (Exception ee)
             {
                 logger.error("Servlet-doHomePage, display error page : " + ee.toString(), ee);
             }
         }
     }
     
     /**
      * To search a component or to display more attributes after the serach. Called from 'service' method where reqType
      * is 'searchACs' calls 'getACSearch.getACKeywordResult' method when the action is a new search. calls
      * 'getACSearch.getACShowResult' method when the action is a display attributes. calls 'doRefreshPageForSearchIn'
      * method when the action is searchInSelect. forwards JSP 'SearchResultsPage.jsp' if the action is not
      * searchForCreate. if action is searchForCreate forwards OpenSearchWindow.jsp
      *
      * @throws Exception
      */
     private void doGetACSearchActions() throws Exception
     {
         HttpSession session = m_classReq.getSession();
         String actType = (String) m_classReq.getParameter("actSelect");
         String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
         String sUISearchType = (String) m_classReq.getAttribute("UISearchType");
         if (sUISearchType == null || sUISearchType.equals("nothing"))
             sUISearchType = "";
        // String sSearchInEVS = "";
         GetACSearch getACSearch = new GetACSearch(m_classReq, m_classRes, this);
         if ((menuAction != null) && (actType != null))
         {
             // start a new search from search parameter
             if (actType.equals("Search"))
             {
                 session.setAttribute("showDefaultSortBtn", "Yes");
             	// search is from create page
                 if (menuAction.equals("searchForCreate"))
                 {
                     getACSearch.getACSearchForCreate(m_classReq, m_classRes, false);
                     ForwardJSP(m_classReq, m_classRes, "/OpenSearchWindow.jsp");
                 }
                 // search is from regular search page
                 else
                 {
                     String sComponent = (String) m_classReq.getParameter("listSearchFor");
                     if (sComponent != null && sComponent.equals("Questions"))
                     {
                         DataManager.setAttribute(session, "originAction", "QuestionSearch");
                         getACSearch.getACQuestion();
                     }
                     else
                         getACSearch.getACKeywordResult(m_classReq, m_classRes);
                     // forward to search result page of main search
                     ForwardJSP(m_classReq, m_classRes, "/SearchResultsPage.jsp");
                 }
             }
             // set the attribute send the page back to refresh.
             else if (actType.equals("SearchDef"))
             {
                 getACSearch.doSearchEVS(m_classReq, m_classRes);
                 ForwardJSP(m_classReq, m_classRes, "/EVSSearchPage.jsp");
             }
             else if (actType.equals("SearchDefVM"))
             {
                 getACSearch.doSearchEVS(m_classReq, m_classRes);
                 ForwardJSP(m_classReq, m_classRes, "/EVSSearchPageVM.jsp");
             }
             // show the selected attributes (update button)
             else if (actType.equals("Attribute"))
             {
                 getACSearch.getACShowResult(m_classReq, m_classRes, "Attribute");
                 if (menuAction.equals("searchForCreate"))
                     ForwardJSP(m_classReq, m_classRes, "/OpenSearchWindow.jsp");
                 else
                     ForwardJSP(m_classReq, m_classRes, "/SearchResultsPage.jsp");
             }
             else if (actType.equals("AttributeRef"))
             {
                 getACSearch.getACShowResult(m_classReq, m_classRes, "Attribute");
                 ForwardJSP(m_classReq, m_classRes, "/OpenSearchWindowReference.jsp");
             }
             // set the attribute send the page back to refresh.
             else if (actType.equals("searchInSelect"))
                 doRefreshPageForSearchIn();
             // set the attribute send the page back to refresh.
             else if (actType.equals("searchForSelectOther")){
             	session.setAttribute("showDefaultSortBtn", "No");
                 doRefreshPageOnSearchFor("Other");
             }// set the attribute send the page back to refresh.
             else if (actType.equals("searchForSelectCRF"))
                 doRefreshPageOnSearchFor("CRFValue");
             // call method to UI filter change when hyperlink if pressed.
             else if (actType.equals("advanceFilter") || actType.equals("simpleFilter"))
                 this.doUIFilterChange(menuAction, actType);
             // call method when hyperlink if pressed.
             else if (actType.equals("term") || actType.equals("tree"))
             {
                 EVSSearch evs = new EVSSearch(m_classReq, m_classRes, this);
                 evs.doTreeSearch(actType, "EVSValueMeaning");
             }
             // something is wrong, send error page
             else
                 ForwardJSP(m_classReq, m_classRes, "/ErrorPage.jsp");
         }
         else
             ForwardJSP(m_classReq, m_classRes, "/ErrorPage.jsp");
     }
 
     /**
      * To search a component or to display more attributes after the serach. Called from 'service' method where reqType
      * is 'searchACs' calls 'getACSearch.getACKeywordResult' method when the action is a new search. calls
      * 'getACSearch.getACShowResult' method when the action is a display attributes. forwards JSP
      * 'SearchResultsPage.jsp' if the action is not searchForCreate. if action is searchForCreate forwards
      * OpenSearchWindow.jsp
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     private void doBlockSearchActions() throws Exception
     {
         HttpSession session = m_classReq.getSession();
         Boolean approvedRep = new Boolean(false);
         String actType = (String) m_classReq.getParameter("actSelect");
         if (actType == null)
             actType = "";
         String sSearchFor = (String) m_classReq.getParameter("listSearchFor");
         String sKeyword = (String) m_classReq.getParameter("keyword");
         String dtsVocab = m_classReq.getParameter("listContextFilterVocab");
         // String sSearchInEVS = "";
        // String sUISearchType = "";
         String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
         String sMetaSource = m_classReq.getParameter("listContextFilterSource");
         if (sMetaSource == null)
             sMetaSource = "";
         GetACSearch getACSearch = new GetACSearch(m_classReq, m_classRes, this);
         DataManager.setAttribute(session, "creSearchAC", sSearchFor);
         DataManager.setAttribute(session, "dtsVocab", dtsVocab);
         DataManager.setAttribute(session, "creKeyword", sKeyword);
         getCompAttrList(sSearchFor, "searchForCreate");
         // System.out.println(sSearchFor + " block actions " + actType);
         if ((menuAction != null) && (actType != null))
         {
             if (actType.equals("Search"))
             {
 
             	session.setAttribute("ApprovedRepTerm", approvedRep);
             	getACSearch.getACSearchForCreate(m_classReq, m_classRes, false);
                 ForwardJSP(m_classReq, m_classRes, "/OpenSearchWindowBlocks.jsp");
             }
             else if (actType.equals("Attribute"))
             {
             	getACSearch.getACShowResult(m_classReq, m_classRes, "Attribute");
                 ForwardJSP(m_classReq, m_classRes, "/OpenSearchWindowBlocks.jsp");
             }
             else if (actType.equals("FirstSearch"))
             {
                 //this.getDefaultBlockAttr(m_classReq, m_classRes, "NCI Thesaurus"); // "Thesaurus/Metathesaurus");
             	EVS_UserBean eUser = (EVS_UserBean) this.sessionData.EvsUsrBean;
                 this.getDefaultBlockAttr(eUser.getPrefVocab()); // "Thesaurus/Metathesaurus");
                 //to display the pre-populated table with the list of approved Rep Terms.
                 if(sSearchFor.equals("RepTerm"))
                 {
                 System.out.println(m_classReq.getParameter("nonEVSRepTermSearch"));
                  this.getRepTermDefaultContext();
                  approvedRep=true;
                  session.setAttribute("ApprovedRepTerm", approvedRep);
               // get default attributes
                  Vector vSel = (Vector) session.getAttribute("creSelectedAttr");
                  Vector vSelClone = (Vector) vSel.clone();
                  vSelClone.remove("Public ID");
                  vSelClone.remove("EVS Identifier");
                  vSelClone.remove("Workflow Status");
                  vSelClone.remove("Semantic Type");
                  vSelClone.remove("Context");
                  vSelClone.remove("Vocabulary");
                  vSelClone.remove("caDSR Component");
                  DataManager.setAttribute(session, "creSelectedAttr", vSelClone);
                  getApprovedRepTerm();
 
                 }
                 ForwardJSP(m_classReq, m_classRes, "/OpenSearchWindowBlocks.jsp");
             }
             else if (actType.equals("OpenTreeToConcept") || actType.equals("OpenTreeToParentConcept")
                             || actType.equals("term") || actType.equals("tree"))
             {
             	session.setAttribute("ApprovedRepTerm", approvedRep);
             	this.doEVSSearchActions(actType, m_classReq, m_classRes);
             }
             else if (actType.equals("doVocabChange"))
             {
             	session.setAttribute("ApprovedRepTerm", approvedRep);
             	this.getDefaultBlockAttr(dtsVocab);
                 m_classReq.setAttribute("UISearchType", "term");
                 ForwardJSP(m_classReq, m_classRes, "/OpenSearchWindowBlocks.jsp");
             }
             else if (actType.equals("nonEVS"))
                 ForwardJSP(m_classReq, m_classRes, "/NonEVSSearchPage.jsp");
         }
         else
             ForwardJSP(m_classReq, m_classRes, "/ErrorPage.jsp");
     }
 
     
     /**
      * To do edit/create from template/new version of a component, clear all records or to display only selected rows
      * after the serach. Called from 'service' method where reqType is 'showResult'. calls 'getACSearch.getSelRowToEdit'
      * method when the action is a edit/create from template/new version. if user doesn't have write permission to
      * edit/create new version forwards the page back to SearchResultsPage.jsp with an error message. For edit, forwards
      * the edit page for the selected component. For new Version/create new from template forwards the create page for
      * the selected component. calls 'getACSearch.getACShowResult' method when the action is show only selected rows and
      * forwards JSP 'SearchResultsPage.jsp'. forwards the page 'SearchResultsPage.jsp' with empty result vector if
      * action is clear records.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param ub_
      *
      * @throws Exception
      */
     private void doSearchResultsAction() throws Exception
     {
     	String hidaction = (String)m_classReq.getParameter("hidaction");
         if (hidaction.equals("newUsingExisting") || hidaction.equals("newVersion") || hidaction.equals("edit")){
         	doMenuAction();
         }
     	HttpSession session = m_classReq.getSession();
         String actType = (String) m_classReq.getParameter("actSelected");
         String sSearchAC = (String) session.getAttribute("searchAC"); // get the selected component
         String sAction = (String) m_classReq.getParameter("pageAction");
         DataManager.setAttribute(session, "originAction", "");
         GetACSearch getACSearch = new GetACSearch(m_classReq, m_classRes, this);
         String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
         // get the sort string parameter
         @SuppressWarnings("unused") String sSortType = "";
         if (actType == null)
             sSortType = (String) m_classReq.getParameter("sortType");
         // sort the header click
         if (actType.equals("sort"))
             doSortACActions();
         // edit selection button from search results page
         else if (actType.equals("Edit"))
             doSearchSelectionAction(m_classReq, m_classRes);
         // edit selection button from search results page
         else if (actType.equals("BlockEdit"))
             doSearchSelectionBEAction();
         // open the designate de page
         else if (actType.equals("EditDesignateDE"))
             new DesDEServlet(this, null).doAction(m_classReq, m_classRes, "Open");
         // open Ref Document Upload page
         else if (actType.equals("RefDocumentUpload"))
             this.doRefDocumentUpload(m_classReq, m_classRes, "Open");
         else if (actType.equals("pvEdits")) // fromForm
         {
         	 Integer curInd = new Integer((String) m_classReq.getParameter("hiddenSelectedRow"));
              if (curInd != null)
              { int thisInd = curInd.intValue();
                Vector results =(Vector)session.getAttribute("vSelRows");
                session.setAttribute("creKeyword", session.getAttribute("serKeyword"));
                PV_Bean pv = (PV_Bean)results.get(thisInd);
                VM_Bean vm = pv.getPV_VM();
                this.doOpenEditVM(m_classReq, m_classRes,vm,pv);
              }
         }
          // store empty result vector in the attribute
         else if (actType.equals("clearRecords"))
         {
             Vector vResult = new Vector();
             DataManager.setAttribute(session, "results", vResult);
             DataManager.setAttribute(session, "vSelRows", vResult);
             DataManager.setAttribute(session, "CheckList", vResult);
             DataManager.setAttribute(session, "AppendAction", "Not Appended");
             session.setAttribute("recsFound", "No ");
             DataManager.setAttribute(session, "serKeyword", "");
             DataManager.setAttribute(session, "serProtoID", "");
             DataManager.setAttribute(session, "LastAppendWord", "");
             ForwardJSP(m_classReq, m_classRes, "/SearchResultsPage.jsp");
         }
         // use permissible value for selected crf value
         else if (actType.equals("usePVforCRFValue"))
         {
             PV_Bean m_PV = new PV_Bean();
             doRefreshPVSearchPage(m_classReq, m_classRes, m_PV, "Search");
         }
             // get Associate AC
         else if (actType.equals("AssocDEs") || actType.equals("AssocDECs") || actType.equals("AssocVDs"))
             doGetAssociatedAC(actType, sSearchAC);
         else if (sAction.equals("backFromGetAssociated"))
         {
             DataManager.setAttribute(session, "backFromGetAssociated", "backFromGetAssociated");
             DataManager.setAttribute(session, "CheckList", null);
             DataManager.setAttribute(session, "LastAppendWord", "");
             DataManager.setAttribute(session, "serProtoID", "");
             DataManager.setAttribute(session, "labelKeyword", "");
             ForwardJSP(m_classReq, m_classRes, "/SearchResultsPage.jsp");
         }
         else if (!menuAction.equals("searchForCreate") && actType.equals("Monitor"))
             doMonitor(m_classReq, m_classRes);
         else if (!menuAction.equals("searchForCreate") && actType.equals("UnMonitor"))
             doUnmonitor(m_classReq, m_classRes);
         else
         { // show selected rows only.
         	getACSearch.getACShowResult(m_classReq, m_classRes, actType);
             String show = (String) m_classReq.getParameter("show");
             if ((show != null)&&(show.equals("No"))){
             	session.setAttribute("showDefaultSortBtn", "No");
             }
             if (menuAction.equals("searchForCreate"))
                 ForwardJSP(m_classReq, m_classRes, "/OpenSearchWindow.jsp");
             else
                 ForwardJSP(m_classReq, m_classRes, "/SearchResultsPage.jsp");
         }
     }
 
     /**
      * to display the selected elements for block edit, opened from create/edit pages.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     private void doDisplayWindowBEAction() throws Exception
     {
         HttpSession session = m_classReq.getSession();
         GetACSearch getACSearch = new GetACSearch(m_classReq, m_classRes, this);
         String sMenu = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
         DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, "BEDisplay"); // set the menu to BEDisplay to get the
                                                                                 // results properly
         getACSearch.getACShowResult(m_classReq, m_classRes, "BEDisplayRows");
         DataManager.setAttribute(session, "BEDisplaySubmitted", "true");
         DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, sMenu); // set the menu back to way it was
         ForwardJSP(m_classReq, m_classRes, "/OpenBlockEditWindow.jsp");
     }
 
     /**
      * to display the associated DEC for the selected oc or prop.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     private void doDECDetailDisplay() throws Exception
     {
        // HttpSession session = m_classReq.getSession();
         GetACSearch getACSearch = new GetACSearch(m_classReq, m_classRes, this);
         String acID = (String) m_classReq.getParameter("acID");
         String acName = (String) m_classReq.getParameter("acName");
         if (acName == null || acName.equals(""))
             acName = "doneSearch";
         Vector vList = new Vector();
         if (acID != null && !acID.equals(""))
         {
             if (acName != null && acName.equals("ObjectClass"))
                 getACSearch.doDECSearch("", "", "", "", "", "", "", "", "", "", "", "", "", acID, "", "", 0, "", "",
                                 "", "", "", vList);
             if (acName != null && acName.equals("Property"))
                 getACSearch.doDECSearch("", "", "", "", "", "", "", "", "", "", "", "", "", "", acID, "", 0, "", "",
                                 "", "", "", vList);
         }
         m_classReq.setAttribute("pageAct", acName);
         m_classReq.setAttribute("lstDECResult", vList);
         ForwardJSP(m_classReq, m_classRes, "/DECDetailWindow.jsp");
     }
 
     /**
      * To sort the search results by clicking on the column heading. Called from 'service' method where reqType is
      * 'doSortCDE' calls 'getACSearch.getACSortedResult' method and forwards page 'SearchResultsPage.jsp'.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     private void doSortACActions() throws Exception
     {
         HttpSession session = m_classReq.getSession();
         GetACSearch getACSearch = new GetACSearch(m_classReq, m_classRes, this);
         getACSearch.getACSortedResult(m_classReq, m_classRes);
         String sMenuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION); // get the selected menu
                                                                                                 // action
         if (sMenuAction.equals("searchForCreate"))
             ForwardJSP(m_classReq, m_classRes, "/OpenSearchWindow.jsp");
         else
             ForwardJSP(m_classReq, m_classRes, "/SearchResultsPage.jsp");
     }
 
     /**
      * To sort the search results of the blocks by clicking on the column heading. Called from 'service' method where
      * reqType is 'doSortBlocks' calls 'getACSearch.getBlocksSortedResult' method and forwards page
      * 'OpenSearchWindowBlocks.jsp' or 'OpenSearchWindowQualifiers.jsp'
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param ACType
      *
      * @throws Exception
      */
     private void doSortBlockActions() throws Exception
     {
         HttpSession session = m_classReq.getSession();
        // GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, this);
         // EVSSearch evs = new EVSSearch(m_classReq, m_classRes, this);
         String actType = (String) m_classReq.getParameter("actSelected");
         String sComp = (String) m_classReq.getParameter("searchComp");
         String sSelectedParentCC = (String) m_classReq.getParameter("selectedParentConceptCode");
         if (sSelectedParentCC == null)
             sSelectedParentCC = "";
         String sSelectedParentName = (String) m_classReq.getParameter("selectedParentConceptName");
         if (sSelectedParentName == null)
             sSelectedParentName = "";
         String sSelectedParentDB = (String) m_classReq.getParameter("selectedParentConceptDB");
         if (sSelectedParentDB == null)
             sSelectedParentDB = "";
         String sSelectedParentMetaSource = (String) m_classReq.getParameter("selectedParentConceptMetaSource");
         if (sSelectedParentMetaSource == null)
             sSelectedParentMetaSource = "";
         DataManager.setAttribute(session, "ParentMetaSource", sSelectedParentMetaSource);
         if (actType.equals("FirstSearch"))
         {
             if (sComp.equals("ParentConceptVM"))
             {
                 DataManager.setAttribute(session, "SelectedParentCC", sSelectedParentCC);
                 DataManager.setAttribute(session, "SelectedParentDB", sSelectedParentDB);
                 DataManager.setAttribute(session, "SelectedParentMetaSource", sSelectedParentMetaSource);
                 DataManager.setAttribute(session, "SelectedParentName", sSelectedParentName);
             }
             getCompAttrList(sComp, "searchForCreate");
             DataManager.setAttribute(session, "creContext", "");
             DataManager.setAttribute(session, "creSearchAC", sComp);
             ForwardJSP(m_classReq, m_classRes, "/OpenSearchWindowBlocks.jsp");
         }
         else if (actType.equals("showConceptInTree"))
             this.doEVSSearchActions(actType, m_classReq, m_classRes);
         // evs.showConceptInTree(sComp, actType);
         else if (actType.equals("appendConcept"))
         {
         	ValueDomainServlet vdServ = (ValueDomainServlet) this.getACServlet("ValueDomain");
             PVServlet pvSer = new PVServlet(m_classReq, m_classRes, vdServ);
             @SuppressWarnings("unused") String sPage = pvSer.storeConceptAttributes();
             ForwardJSP(m_classReq, m_classRes, "/OpenSearchWindowBlocks.jsp");
         }
         else
         {
             GetACSearch getACSearch = new GetACSearch(m_classReq, m_classRes, this);
             getACSearch.getBlockSortedResult(m_classReq, m_classRes, "Blocks");
             ForwardJSP(m_classReq, m_classRes, "/OpenSearchWindowBlocks.jsp");
         }
     }
 
     /**
      * To open search page after login or click search on the menu. Called from 'service' method where reqType is
      * 'getSearchFilter' Adds default attributes to 'selectedAttr' session vector. Makes empty 'results' session vector.
      * stores 'No ' to 'recsFound' session attribute. forwards page 'CDEHomePage.jsp'.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     private void doOpenSearchPage() throws Exception
     {
         HttpSession session = m_classReq.getSession();
         DataManager.setAttribute(session, "vStatMsg", new Vector());
         DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, "nothing");
         DataManager.setAttribute(session, "LastMenuButtonPressed", "Search");
         Vector vDefaultAttr = new Vector();
         String searchAC = (String) session.getAttribute("searchAC");
         if (searchAC == null)
             searchAC = "DataElement";
         // make the default to longName if not Questions
         String sSearchIn = (String) session.getAttribute("serSearchIn");
         if ((sSearchIn == null) || (!searchAC.equals("Questions")))
             sSearchIn = "longName";
         DataManager.setAttribute(session, "serSearchIn", sSearchIn);
         vDefaultAttr = getDefaultAttr(searchAC, sSearchIn); // default display attributes
         DataManager.setAttribute(session, "selectedAttr", vDefaultAttr);
         this.getDefaultFilterAtt(); // default filter by attributes
         //doInitDDEInfo(m_classReq, m_classRes);  ** put back later; do we need this?? **
         clearSessionAttributes(m_classReq, m_classRes);
         // call the method to get attribute list for the selected AC
         getCompAttrList(searchAC, "nothing");
        session.setAttribute("showDefaultSortBtn", "No");
         ForwardJSP(m_classReq, m_classRes, "/SearchResultsPage.jsp");
     }
 
     /**
      * To refresh the page when the search in changed from drop down list. Called from 'doGetACSearchActions' method
      * modifies the session attribute 'selectedAttr' or 'creSelectedAttr' according to what is selected. forwards JSP
      * 'SearchResultsPage.jsp' if the action is not searchForCreate. if action is searchForCreate forwards
      * OpenSearchWindow.jsp
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     @SuppressWarnings("unchecked")
     private void doRefreshPageForSearchIn() throws Exception
     {
         HttpSession session = m_classReq.getSession();
         // same for both searchfor create and regular search
         String sSearchIn = (String) m_classReq.getParameter("listSearchIn");
         if (sSearchIn == null)
             sSearchIn = "longName";
         // same for both searchfor create and regular search
         String sSearchAC = (String) m_classReq.getParameter("listSearchFor");
         // set the selected display attributes so they persist through refreshes
         String selAttrs[] = m_classReq.getParameterValues("listAttrFilter");
        // int selLength = selAttrs.length;
         Vector<String> vSelAttrs = new Vector<String>();
         String sID = "";
         if (selAttrs != null)
         {
             for (int i = 0; i < selAttrs.length; i++)
             {
                 sID = selAttrs[i];
                 if ((sID != null) && (!sID.equals("")))
                     vSelAttrs.addElement(sID);
             }
         }
         String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
         // add/remove protocol and crf from the displayable attriubtes according to the search in.
         Vector<String> vCompAttr = new Vector<String>();
         if (menuAction.equals("searchForCreate"))
             vCompAttr = (Vector) session.getAttribute("creAttributeList");
         else
             vCompAttr = (Vector) session.getAttribute("serAttributeList");
         if (vCompAttr != null && sSearchIn.equals("CRFName"))
         {
             if (!vCompAttr.contains("Protocol ID"))
                 vCompAttr.insertElementAt("Protocol ID", 13);
             if (!vCompAttr.contains("CRF Name"))
                 vCompAttr.insertElementAt("CRF Name", 14);
         }
         else
         {
             if (vCompAttr.contains("Protocol ID"))
                 vCompAttr.removeElement("Protocol ID");
             if (vCompAttr.contains("CRF Name"))
                 vCompAttr.removeElement("CRF Name");
         }
         // put it back in the session
         if (menuAction.equals("searchForCreate"))
             DataManager.setAttribute(session, "creAttributeList", vCompAttr);
         else
             DataManager.setAttribute(session, "serAttributeList", vCompAttr);
         // store the all the selected attributes in search parameter jsp
         this.getSelectedAttr(menuAction, "ChangeSearchIn");
         // gets selected attributes and sets session attributes.
         if (!menuAction.equals("searchForCreate"))
         {
             DataManager.setAttribute(session, "serSearchIn", sSearchIn); // set the search in attribute
             // call method to add or remove selected display attributes as search in changes
             Vector vSelectedAttr = getDefaultSearchInAttr(sSearchAC, sSearchIn, vSelAttrs, vCompAttr);
             // Store the session attributes
             DataManager.setAttribute(session, "selectedAttr", vSelectedAttr);
             GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, this);
             Vector vResult = serAC.refreshSearchPage(sSearchAC);
             DataManager.setAttribute(session, "results", vResult);
             DataManager.setAttribute(session, "serKeyword", "");
             DataManager.setAttribute(session, "serProtoID", "");
             // send page
             ForwardJSP(m_classReq, m_classRes, "/SearchResultsPage.jsp");
         }
         else
         // menu action searchForCreate
         {
             m_classReq.setAttribute("creSearchIn", sSearchIn); // set the search in attribute
             // call method to add or remove selected display attributes as search in changes
             Vector vSelectedAttr = getDefaultSearchInAttr(sSearchAC, sSearchIn, vSelAttrs, vCompAttr);
             // Store the session attributes
             DataManager.setAttribute(session, "creSelectedAttr", vSelectedAttr);
             // m_classReq.setAttribute("creSelectedAttrBlocks", vSelectedAttr);
             GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, this);
             Vector vResult = serAC.refreshSearchPage(sSearchAC);
             DataManager.setAttribute(session, "results", vResult);
             DataManager.setAttribute(session, "creKeyword", "");
             // set the session attribute for searchAC
             ForwardJSP(m_classReq, m_classRes, "/OpenSearchWindow.jsp");
         }
     }
 
     /**
      * To refresh the page when the search For changed from drop down list. Called from 'doGetACSearchActions' method
      * modifies the session attribute 'selectedAttr' or 'creSelectedAttr' according to what is selected. forwards JSP
      * 'SearchResultsPage.jsp' if the action is not searchForCreate. if action is searchForCreate forwards
      * OpenSearchWindow.jsp
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param sOrigin
      *
      * @throws Exception
      */
     private void doRefreshPageOnSearchFor(String sOrigin)
                     throws Exception
     {
         HttpSession session = m_classReq.getSession();
         // clearSessionAttributes(m_classReq, m_classRes);
         // get the search for parameter from the request
         String sSearchAC = (String) m_classReq.getParameter("listSearchFor");
         String sSearchIn = "longName";
         // call the method to get attribute list for the selected AC
         String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
         getCompAttrList(sSearchAC, menuAction);
         // change the selected attributes according to what is selected
         Vector vSelectedAttr = new Vector();
         vSelectedAttr = getDefaultAttr(sSearchAC, sSearchIn);
         this.getDefaultFilterAtt(); // get the default filter by attributes
         if (!menuAction.equals("searchForCreate"))
         {
             // Store the session attributes
             DataManager.setAttribute(session, "selectedAttr", vSelectedAttr);
             DataManager.setAttribute(session, "searchAC", sSearchAC);
             DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, "nothing");
             // new searchFor, reset the stacks
             clearSessionAttributes(m_classReq, m_classRes);
             if (sSearchAC.equals("ConceptClass"))
             {
                 Vector<String> vStatus = new Vector<String>();
                 vStatus.addElement("RELEASED");
                 DataManager.setAttribute(session, "serStatus", vStatus);
             }
             ForwardJSP(m_classReq, m_classRes, "/SearchResultsPage.jsp");
         }
         else
         {
             // Store the session attributes
             DataManager.setAttribute(session, "creSelectedAttr", vSelectedAttr);
             DataManager.setAttribute(session, "creSearchAC", sSearchAC);
             DataManager.setAttribute(session, "vACSearch", new Vector());
             // do the basic search for conceptual domain
             if (sSearchAC.equals("ConceptualDomain")) // || sSearchAC.equals("ValueMeaning"))
             {
                 GetACSearch getACSearch = new GetACSearch(m_classReq, m_classRes, this);
                 getACSearch.getACSearchForCreate(m_classReq, m_classRes, true);
             }
             if (sSearchAC.equals("ValueMeaning"))
             {
                 VD_Bean vd = (VD_Bean) session.getAttribute("m_VD");
                 String sCDid = "";
                 if (vd != null)
                     sCDid = vd.getVD_CD_IDSEQ();
                 DataManager.setAttribute(session, "creSelectedCD", sCDid);
             }
             // forward the page with crfresults if it is crf value search, otherwise searchResults
             if (sOrigin.equals("CRFValue"))
                 ForwardJSP(m_classReq, m_classRes, "/CRFValueSearchWindow.jsp");
             else
                 ForwardJSP(m_classReq, m_classRes, "/OpenSearchWindow.jsp");
         }
     }
 
     /**
      * To add or remove search in attributes as seach in changed.
      *
      * @param sSearchAC
      *            String searching component
      * @param sSearchIn
      *            String searching in attribute
      * @param vSelectedAttr
      *            Vector selected attribute
      * @param vComp
      *            Vector of all attributes of the selected component.
      *
      * @return Vector selected attribute vector
      * @throws Exception
      */
     private Vector getDefaultSearchInAttr(String sSearchAC, String sSearchIn, Vector<String> vSelectedAttr,
                     Vector<String> vComp) throws Exception
     {
         // first remove all the searchIn from the selected attribute list
         if (vSelectedAttr.contains("Protocol ID"))
             vSelectedAttr.remove("Protocol ID");
         if (vSelectedAttr.contains("CRF Name"))
             vSelectedAttr.remove("CRF Name");
         // add public id to selected attribute seperately for each type
         if (sSearchIn.equals("minID"))
         {
             if (!vSelectedAttr.contains("Public ID"))
                 vSelectedAttr.add("Public ID");
         }
         // select the hist cde id if not selected and remove crf/protocol for hist cdeid searchin
         else if (sSearchIn.equals("histID"))
         {
             // if (!vSelectedAttr.contains("Historical CDE ID"))
             // vSelectedAttr.add("Historical CDE ID");
             if (!vSelectedAttr.contains("Alternate Names"))
                 vSelectedAttr.add("Alternate Names");
         }
         else if (sSearchIn.equals("origin"))
         {
             if (!vSelectedAttr.contains("Origin"))
                 vSelectedAttr.add("Origin");
         }
         else if (sSearchIn.equals("concept"))
         {
             if (sSearchAC.equals("DataElement") || sSearchAC.equals("DataElementConcept")
                             || sSearchAC.equals("ValueDomain") || sSearchAC.equals("ConceptualDomain")
                             || sSearchAC.equals("ClassSchemeItems"))
             {
                 if (!vSelectedAttr.contains("Concept Name"))
                     vSelectedAttr.add("Concept Name");
             }
         }
         /*
          * else if (sSearchIn.equals("NamesAndDocText")) { if (!vSelectedAttr.contains("Preferred Question Text Document
          * Text")) vSelectedAttr.add("Preferred Question Text Document Text"); if (!vSelectedAttr.contains("Historic
          * Short CDE Name Document Text")) vSelectedAttr.add("Historic Short CDE Name Document Text"); } else if
          * (sSearchIn.equals("docText")) { if (!vSelectedAttr.contains("Preferred Question Text Document Text"))
          * vSelectedAttr.add("Preferred Question Text Document Text"); if (!vSelectedAttr.contains("Historic Short CDE
          * Name Document Text")) vSelectedAttr.add("Historic Short CDE Name Document Text"); if
          * (!vSelectedAttr.contains("Reference Documents")) vSelectedAttr.add("Reference Documents"); }
          */
         // add ref docs in the displayable list if doc text is selected
         else if (sSearchIn.equals("docText") || sSearchIn.equals("NamesAndDocText"))
         {
             if (!vSelectedAttr.contains("Reference Documents"))
                 vSelectedAttr.add("Reference Documents");
         }
         else if (sSearchIn.equals("permValue"))
         {
             if (!vSelectedAttr.contains("Permissible Value"))
                 vSelectedAttr.add("Permissible Value");
         }
         // add proto and crf and remove cde id if crf name is search in
         else if (sSearchIn.equals("CRFName"))
         {
             if (sSearchAC.equals("DataElement"))
             {
                 if (!vSelectedAttr.contains("Protocol ID"))
                     vSelectedAttr.add("Protocol ID");
                 if (!vSelectedAttr.contains("CRF Name"))
                     vSelectedAttr.add("CRF Name");
             }
             else if (sSearchAC.equals("Questions"))
             {
                 if (!vSelectedAttr.contains("Question Text"))
                     vSelectedAttr.add("Question Text");
                 if (!vSelectedAttr.contains("DE Long Name"))
                     vSelectedAttr.add("DE Long Name");
                 if (!vSelectedAttr.contains("DE Public ID"))
                     vSelectedAttr.add("DE Public ID");
                 if (!vSelectedAttr.contains("Workflow Status"))
                     vSelectedAttr.add("Workflow Status");
                 if (!vSelectedAttr.contains("Protocol ID"))
                     vSelectedAttr.add("Protocol ID");
             }
         }
         // call method to resort the display attributes
         vSelectedAttr = this.resortDisplayAttributes(vComp, vSelectedAttr);
         return vSelectedAttr;
     } // end of getDefaultSearchInAttr
 
     /**
      * To refresh the page when filter hyperlink is pressed. Called from 'doGetACSearchActions' method gets request
      * parameters to store the selected values in the session according to what the menu action is forwards JSP
      * 'SearchResultsPage.jsp' if the action is not searchForCreate. if action is searchForCreate forwards
      * OpenSearchWindow.jsp
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param menuAction
      *            to distinguish between main search and search for create windows
      * @param actType
      *            type of filter a simple or advanced
      *
      * @throws Exception
      */
     private void doUIFilterChange(String menuAction, String actType)
                     throws Exception
     {
         HttpSession session = m_classReq.getSession();
         GetACSearch getACSearch = new GetACSearch(m_classReq, m_classRes, this);
         String sSearchAC = m_classReq.getParameter("listSearchFor");
         // store the all the selected attributes in search parameter jsp
         this.getSelectedAttr(menuAction, "ChangeUIFilter");
         // get list of search previous search results
         Vector vResult = getACSearch.refreshSearchPage(sSearchAC);
         DataManager.setAttribute(session, "results", vResult);
         // set the session attributes send the page back to refresh for simple filter.
         if (menuAction.equals("searchForCreate"))
         {
             if (actType.equals("advanceFilter"))
                 DataManager.setAttribute(session, "creUIFilter", "advanced");
             else if (actType.equals("simpleFilter"))
                 DataManager.setAttribute(session, "creUIFilter", "simple");
             ForwardJSP(m_classReq, m_classRes, "/OpenSearchWindow.jsp");
         }
         // set session the attribute send the page back to refresh for advanced filter.
         else
         {
             if (actType.equals("advanceFilter"))
                 DataManager.setAttribute(session, "serUIFilter", "advanced");
             else if (actType.equals("simpleFilter"))
                 DataManager.setAttribute(session, "serUIFilter", "simple");
             ForwardJSP(m_classReq, m_classRes, "/SearchResultsPage.jsp");
         }
     }
 
     /**
      * gets the selected row from the search result to forward the data. Called from 'doSearchResultsAction' method
      * where actType is 'AssocDEs', AssocDECs or AssocVDs gets the index and ID/Names from the session attributes to get
      * the row bean. calls 'getACSearch.getAssociatedDESearch', 'getACSearch.getAssociatedDECSearch', or
      * 'getACSearch.getAssociatedVDSearch' method to get search associated results depending actType. calls
      * 'getACSearch.getDEResult', 'getACSearch.getDECResult', or 'getACSearch.getVDResult' method to get final result
      * vector which is stored in the session. resets default attributes and other session attributes forwards to
      * SearchResultsPage to display the search results.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param assocAC
      *            String actType of the search result page.
      * @param sSearchAC
      *            String search type from the drop down list.
      *
      * @throws Exception
      */
     @SuppressWarnings("unchecked")
     private void doGetAssociatedAC(String assocAC, String sSearchAC)
                     throws Exception
     {
         HttpSession session = m_classReq.getSession();
         Admin_Components_Mgr acMgr = new Admin_Components_Mgr();
         ArrayList list = null;
         int thisInd = 0;
         Vector vSRows = (Vector) session.getAttribute("vSelRows");
         // get the searched ID and Name vectors
         Vector vIDs = (Vector) session.getAttribute("SearchID");
         // get the long / names of the selected ac
         Vector vNames = new Vector();
         if (sSearchAC.equals("DataElementConcept") || sSearchAC.equals("ValueDomain")
                         || sSearchAC.equals("ConceptualDomain") || sSearchAC.equals("DataElement")|| sSearchAC.equals("ValueMeaning"))
         {
             vNames = (Vector) session.getAttribute("SearchLongName");
         }
         // PermissibleValue, ClassSchemeItems, ObjectClass, Property
         else
         {
             vNames = (Vector) session.getAttribute("SearchName");
         }
       //  Vector oldVResult = (Vector) session.getAttribute("results");
         // get the selected row index from the hidden field.
         String sID = "";
         String sName = "";
         // convert the string to integer and to int.
         Integer curInd = null;
         if(m_classReq.getParameter("unCheckedRowId")!=null){
         	curInd = new Integer((String) m_classReq.getParameter("unCheckedRowId"));
         }
         if (curInd != null)
             thisInd = curInd.intValue();
         if (vIDs != null && !vIDs.equals("") && vIDs.size() > 0 && (thisInd < vIDs.size()))
         {
             sID = (String) vIDs.elementAt(thisInd);
             if (vNames != null && vNames.size() > thisInd)
                 sName = (String) vNames.elementAt(thisInd);
         }
         if (sID != null && !sID.equals(""))
         {
         	try{
           	  list = acMgr.getPublicIDVersion(sID, m_conn);
           	}catch (DBException e){
           		logger.error(e.getMessage());
       	   	}
         	// reset the default attributes
             Vector vSelVector = new Vector();
             String sSearchIn = (String) session.getAttribute("serSearchIn");
             GetACSearch getACSearch = new GetACSearch(m_classReq, m_classRes, this);
             Vector vResult = new Vector();
             String oldSearch = "";
             String newSearch = "";
            // String retCode = "";
             String pvID = "", cdID = "", deID = "", decID = "", vdID = "", cscsiID = "", ocID = "", propID = "", conID = "";
             String vmID="";
             PV_Bean pvBean = null;
             CSI_Bean csiBean = null;
             DE_Bean deBean = null;
             DEC_Bean decBean = null;
             VD_Bean vdBean = null;
             VM_Bean vmBean = null;
             if (sSearchAC.equals("PermissibleValue")){
            	  	 if ((vSRows != null) && (vSRows.size()>0))
              	   pvBean = (PV_Bean) vSRows.elementAt(thisInd);
             } 
             if (sSearchAC.equals("ClassSchemeItems")){
           	  	 if ((vSRows != null) && (vSRows.size()>0))
           	       csiBean = (CSI_Bean) vSRows.elementAt(thisInd);
             } 
             if (sSearchAC.equals("DataElement")){
          	  	 if ((vSRows != null) && (vSRows.size()>0))
          	  		deBean = (DE_Bean) vSRows.elementAt(thisInd);
             } 
             if (sSearchAC.equals("DataElementConcept")){
         	  	 if ((vSRows != null) && (vSRows.size()>0))
         	  		decBean = (DEC_Bean) vSRows.elementAt(thisInd);
             } 
             if (sSearchAC.equals("ValueDomain")){
         	  	 if ((vSRows != null) && (vSRows.size()>0))
         	  		vdBean = (VD_Bean) vSRows.elementAt(thisInd);
             } 
             if (sSearchAC.equals("ValueMeaning")){
        	  	   if ((vSRows != null) && (vSRows.size()>0))
        	  		  vmBean = (VM_Bean) vSRows.elementAt(thisInd);
             } 
     
             // get the search results from the database.
            
             if (assocAC.equals("AssocDEs"))
             {
                 m_classReq.setAttribute("GetAssocSearchAC", "true");
                 // retCode = getACSearch.doAssociatedDESearch(sID, sSearchAC);
                 if (sSearchAC.equals("PermissibleValue"))
                 	 pvID = sID;
                 else if (sSearchAC.equals("DataElementConcept"))
                     decID = sID;
                 else if (sSearchAC.equals("ValueDomain"))
                     vdID = sID;
                 else if (sSearchAC.equals("ConceptualDomain"))
                     cdID = sID;
                 else if (sSearchAC.equals("ClassSchemeItems"))
                     cscsiID = sID;
                 else if (sSearchAC.equals("ConceptClass"))
                     conID = sID;
                 else if (sSearchAC.equals("ValueMeaning"))
                 	vmID = sID;
                 // do the search only if id is not null
                 Vector vRes = new Vector();
                 if (sID != null && !sID.equals(""))
                 	getACSearch.doDESearch("", "", "", "", "", "", 0, "", "", "", "", "", "", "", "", "", "", "", "",
                                     "", "", "", "", pvID, vdID,vmID,decID, cdID, cscsiID, conID, "", "", vRes);
                 DataManager.setAttribute(session, "vSelRows", vRes);
                 // do attributes after the search so no "two simultaneous request" errors
                 vSelVector = this.getDefaultAttr("DataElement", sSearchIn);
                 DataManager.setAttribute(session, "selectedAttr", vSelVector);
                 getCompAttrList("DataElement", "nothing");
                 // if (retCode.equals("0"))
                 getACSearch.getDEResult(m_classReq, m_classRes, vResult, "");
                 DataManager.setAttribute(session, "searchAC", "DataElement");
                 newSearch = "Data Element";
             }
             else if (assocAC.equals("AssocDECs"))
             {
                 m_classReq.setAttribute("GetAssocSearchAC", "true");
                 // retCode = getACSearch.doAssociatedDECSearch(sID, sSearchAC);
                 if (sSearchAC.equals("ObjectClass"))
                     ocID = sID;
                 else if (sSearchAC.equals("Property"))
                     propID = sID;
                 else if (sSearchAC.equals("DataElement"))
                     deID = sID;
                 else if (sSearchAC.equals("ConceptualDomain"))
                     cdID = sID;
                 else if (sSearchAC.equals("ClassSchemeItems"))
                     cscsiID = sID;
                 else if (sSearchAC.equals("ConceptClass"))
                     conID = sID;
                 Vector vRes = new Vector();
                 getACSearch.doDECSearch("", "", "", "", "", "", "", "", "", "", "", "", "", ocID, propID, "", 0, cdID,
                                 deID, cscsiID, conID, "", vRes);
                 DataManager.setAttribute(session, "vSelRows", vRes);
                 // do attributes after the search so no "two simultaneous request" errors
                 vSelVector = this.getDefaultAttr("DataElementConcept", sSearchIn);
                 DataManager.setAttribute(session, "selectedAttr", vSelVector);
                 getCompAttrList("DataElementConcept", "nothing");
                 // if (retCode.equals("0"))
                 getACSearch.getDECResult(m_classReq, m_classRes, vResult, "");
                 DataManager.setAttribute(session, "searchAC", "DataElementConcept");
                 newSearch = "Data Element Concept";
             }
             else if (assocAC.equals("AssocVDs"))
             {
                 m_classReq.setAttribute("GetAssocSearchAC", "true");
                 if (sSearchAC.equals("PermissibleValue"))
                     pvID = sID;
                 else if (sSearchAC.equals("DataElement"))
                     deID = sID;
                 else if (sSearchAC.equals("ConceptualDomain"))
                     cdID = sID;
                 else if (sSearchAC.equals("ClassSchemeItems"))
                     cscsiID = sID;
                 else if (sSearchAC.equals("ConceptClass"))
                     conID = sID;
                 else if (sSearchAC.equals("ValueMeaning"))
                 	vmID = sID;
                 Vector vRes = new Vector();
                 getACSearch.doVDSearch("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", 0, cdID, pvID,
                                 deID, cscsiID, conID,vmID, "", "", vRes);
                 DataManager.setAttribute(session, "vSelRows", vRes);
                 // do attributes after the search so no "two simultaneous request" errors
                 vSelVector = this.getDefaultAttr("ValueDomain", sSearchIn);
                 DataManager.setAttribute(session, "selectedAttr", vSelVector);
                 getCompAttrList("ValueDomain", "nothing");
                 getACSearch.getVDResult(m_classReq, m_classRes, vResult, "");
                 DataManager.setAttribute(session, "searchAC", "ValueDomain");
                 newSearch = "Value Domain";
             }
             // get the old search for the label
             if (sSearchAC.equals("ConceptualDomain"))
                 oldSearch = "Conceptual Domain";
             else if (sSearchAC.equals("DataElementConcept"))
                 oldSearch = "Data Element Concept";
             else if (sSearchAC.equals("ValueDomain"))
                 oldSearch = "Value Domain";
             else if (sSearchAC.equals("PermissibleValue"))
                 oldSearch = "Permissible Value";
             else if (sSearchAC.equals("DataElement"))
                 oldSearch = "Data Element";
             else if (sSearchAC.equals("ClassSchemeItems"))
                 oldSearch = "Class Scheme Items";
             else if (sSearchAC.equals("ConceptClass"))
                 oldSearch = "Concept Class";
             else if (sSearchAC.equals("ObjectClass"))
                 oldSearch = "Object Class";
             else if (sSearchAC.equals("Property"))
                 oldSearch = "Property ";
             else if (sSearchAC.equals("ValueMeaning"))
                 oldSearch = "Value Meaning";
             // make keyword empty and label for search result page.
             //DataManager.setAttribute(session, "serKeyword", "");
             String labelWord = "";
             String labelWord2 = "";
             labelWord = " associated with " + oldSearch + " - " + sName; // make the label
             labelWord2 = " associated to " + oldSearch + " - " + sName ;
             if (sSearchAC.equals("PermissibleValue") && (pvBean != null) ){
               labelWord2 = labelWord2 + " [" + pvBean.getPV_VM().getVM_ID()+ "v" +pvBean.getPV_VM().getVM_VERSION()+"]";
             }else if (sSearchAC.equals("ClassSchemeItems") && (csiBean != null)){
               labelWord2 = labelWord2 + " [" + csiBean.getCSI_CS_PUBLICID()+ "v" +csiBean.getCSI_CS_VERSION()+"]";
             }else if (sSearchAC.equals("DataElement") && (deBean != null)){
                 labelWord2 = labelWord2 + " [" + deBean.getDE_MIN_CDE_ID()+ "v" +deBean.getDE_VERSION()+"]";
             }else if (sSearchAC.equals("DataElementConcept") && (decBean != null)){
                 labelWord2 = labelWord2 + " [" + decBean.getDEC_DEC_ID()+ "v" +decBean.getDEC_VERSION()+"]";
             }else if (sSearchAC.equals("ValueDomain") && (vdBean != null)){
                 labelWord2 = labelWord2 + " [" + vdBean.getVD_VD_ID()+ "v" +vdBean.getVD_VERSION()+"]";
             }else if (sSearchAC.equals("ValueMeaning") && (vmBean != null)){
                 labelWord2 = labelWord2 + " [" + vmBean.getVM_ID()+ "v" +vmBean.getVM_VERSION()+"]";   
             }else if(list != null && list.size()>0){	
               String version = (String)list.get(1);
               labelWord2 =labelWord2 + " [" + list.get(0)+ "v" +Double.parseDouble(version)+"]";
             }
             m_classReq.setAttribute("labelKeyword1", newSearch); // make the label
             m_classReq.setAttribute("labelKeyword2", labelWord2);
             // save the last word in the request attribute
             DataManager.setAttribute(session, "LastAppendWord", labelWord);
             DataManager.setAttribute(session, "results", vResult); // store result vector in the attribute
             Vector vCheckList = new Vector();
             DataManager.setAttribute(session, "CheckList", vCheckList); // empty the check list in the new search when not appended.
             DataManager.setAttribute(session, "backFromGetAssociated", "");
         }
         // couldnot find a id, go back to search results
         else
         {
             DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE,
                             "Unable to determine the ID of the selected item. ");
         }
         ForwardJSP(m_classReq, m_classRes, "/SearchResultsPage.jsp");
     }
 
     
     /**
      * To refresh the page when filter hyperlink is pressed. Called from 'doGetACSearchActions' method gets request
      * parameters to store the selected values in the session according to what the menu action is forwards JSP
      * 'SearchResultsPage.jsp' if the action is not searchForCreate. if action is searchForCreate forwards
      * OpenSearchWindow.jsp
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param menuAction
      *            to distinguish between main search and search for create windows
      * @param actType
      *            type of filter a simple or advanced
      *
      * @throws Exception
      */
     private void getSelectedAttr(String menuAction, String actType)
                     throws Exception
     {
         HttpSession session = m_classReq.getSession();
         GetACSearch getACSearch = new GetACSearch(m_classReq, m_classRes, this);
         String sSearchAC = "";
         Vector vDocType = new Vector();
         // store the all the attributes in search parameter jsp
         String sProtoID = (String) m_classReq.getParameter("protoKeyword");
         String sKeyword = (String) m_classReq.getParameter("keyword"); // the keyword
         String sContext = (String) m_classReq.getParameter("listContextFilter"); // filter by context
         String sContextUse = (String) m_classReq.getParameter("rContextUse"); // filter by contextUse
         String sVersion = (String) m_classReq.getParameter("rVersion"); // filter by version
         String sVDTypeEnum = (String) m_classReq.getParameter("typeEnum"); // filter by value domain type enumerated
         String sVDTypeNonEnum = (String) m_classReq.getParameter("typeNonEnum"); // filter by value domain type non
                                                                             // enumerated
         String sVDTypeRef = (String) m_classReq.getParameter("typeEnumRef"); // filter by value domain type enumerated by
                                                                         // reference
         String sRegStatus = (String) m_classReq.getParameter("listRegStatus"); // filter by registration status
         @SuppressWarnings("unused") String sStatus = "";
         String sCreFrom = "", sCreTo = "", sModFrom = "", sModTo = "", sCre = "", sMod = "";
         if (actType.equals("ChangeSearchIn"))
         {
             sCreFrom = (String) m_classReq.getParameter("createdFrom"); // filter by createdFrom
             sCreTo = (String) m_classReq.getParameter("createdTo"); // filter by createdTo
             sModFrom = (String) m_classReq.getParameter("modifiedFrom"); // filter by modifiedFrom
             sModTo = (String) m_classReq.getParameter("modifiedTo"); // filter by modifiedTo
             sCre = (String) m_classReq.getParameter("creator"); // filter by creator
             sMod = (String) m_classReq.getParameter("modifier"); // filter by modifier
         }
         // set the session attributes send the page back to refresh for simple filter.
         if (menuAction.equals("searchForCreate"))
         {
             DataManager.setAttribute(session, "creKeyword", sKeyword); // keep the old context criteria
             DataManager.setAttribute(session, "creProtoID", sProtoID); // keep the old protocol id criteria
             DataManager.setAttribute(session, "creContext", sContext); // keep the old context criteria
             m_classReq.setAttribute("creContextBlocks", sContext);
             DataManager.setAttribute(session, "creContextUse", sContextUse); // store contextUse in the session
             DataManager.setAttribute(session, "creVersion", sVersion); // store version in the session
             DataManager.setAttribute(session, "creVDTypeEnum", sVDTypeEnum); // store VDType Enum in the session
             DataManager.setAttribute(session, "creVDTypeNonEnum", sVDTypeNonEnum); // store VDType Non Enum in the session
             DataManager.setAttribute(session, "creVDTypeRef", sVDTypeRef); // store VDType Ref in the session
             DataManager.setAttribute(session, "creRegStatus", sRegStatus); // store regstatus in the session
             DataManager.setAttribute(session, "creCreatedFrom", sCreFrom); // empty the date attributes
             DataManager.setAttribute(session, "creCreatedTo", sCreTo); // empty the date attributes
             DataManager.setAttribute(session, "creModifiedFrom", sModFrom); // empty the date attributes
             DataManager.setAttribute(session, "creModifiedTo", sModTo); // empty the date attributes
             DataManager.setAttribute(session, "creCreator", sCre); // empty the creator attributes
             DataManager.setAttribute(session, "creModifier", sMod); // empty the modifier attributes
             DataManager.setAttribute(session, "creDocTyes", vDocType);
             sSearchAC = (String) session.getAttribute("creSearchAC");
             sStatus = getACSearch.getMultiReqValues(sSearchAC, "searchForCreate", "Context");
             sStatus = getACSearch.getStatusValues(m_classReq, m_classRes, sSearchAC, "searchForCreate", false); // to get a string
                                                                                                     // from multiselect
                                                                                                     // list
         }
         // set session the attribute send the page back to refresh for advanced filter.
         else
         {
             DataManager.setAttribute(session, "serKeyword", sKeyword); // keep the old criteria
             DataManager.setAttribute(session, "serProtoID", sProtoID); // keep the old protocol id criteria
             DataManager.setAttribute(session, "LastAppendWord", sKeyword);
             DataManager.setAttribute(session, "serContext", sContext); // keep the old context criteria
             DataManager.setAttribute(session, "serContextUse", sContextUse); // store contextUse in the session
             DataManager.setAttribute(session, "serVersion", sVersion); // store version in the session
             DataManager.setAttribute(session, "serVDTypeEnum", sVDTypeEnum); // store VDType Enum in the session
             DataManager.setAttribute(session, "serVDTypeNonEnum", sVDTypeNonEnum); // store VDType Non Enum in the session
             DataManager.setAttribute(session, "serVDTypeRef", sVDTypeRef); // store VDType Ref in the session
             DataManager.setAttribute(session, "serRegStatus", sRegStatus); // store regstatus in the session
             DataManager.setAttribute(session, "serCreatedFrom", sCreFrom); // empty the date attributes
             DataManager.setAttribute(session, "serCreatedTo", sCreTo); // empty the date attributes
             DataManager.setAttribute(session, "serModifiedFrom", sModFrom); // empty the date attributes
             DataManager.setAttribute(session, "serModifiedTo", sModTo); // empty the date attributes
             DataManager.setAttribute(session, "serCreator", sCre); // empty the creator attributes
             DataManager.setAttribute(session, "serModifier", sMod); // empty the modifier attributes
             DataManager.setAttribute(session, "serDocTyes", vDocType); // empty doctype list
             sSearchAC = (String) session.getAttribute("searchAC");
             sStatus = getACSearch.getMultiReqValues(sSearchAC, "MainSearch", "Context");
             sStatus = getACSearch.getStatusValues(m_classReq, m_classRes, sSearchAC, "MainSearch", false); // to get a string from
                                                                                                 // multiselect list
         }
     }
 
     /**
      * To get the list of attributes for the selected search component. Called from 'doRefreshPageOnSearchFor',
      * 'doMenuAction', 'doOpenSearchPage' methods stores the vector in the session attribute.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      * @param selSearch
      *            the component to search for.
      * @param sMenu
      *
      * @throws Exception
      */
     @SuppressWarnings("unchecked")
     private void getCompAttrList(String selSearch, String sMenu)
                     throws Exception
     {
         HttpSession session = m_classReq.getSession();
         Vector vCompAtt = new Vector();
         if (selSearch.equals("DataElement"))
         {
             vCompAtt.addElement("Long Name");
             vCompAtt.addElement("Public ID");
             vCompAtt.addElement("Version");
             vCompAtt.addElement("Registration Status");
             vCompAtt.addElement("Workflow Status");
             vCompAtt.addElement("Owned By Context");
             vCompAtt.addElement("Used By Context");
             vCompAtt.addElement("Definition");
             vCompAtt.addElement("Data Element Concept");
             vCompAtt.addElement("Value Domain");
             vCompAtt.addElement("Name");
             vCompAtt.addElement("Origin");
             vCompAtt.addElement("Concept Name");
             // protocol id and crf name is not default and positioned at 13th & 14th place
             // vCompAtt.addElement("Protocol ID");
             // vCompAtt.addElement("CRF Name");
             vCompAtt.addElement("Effective Begin Date");
             vCompAtt.addElement("Effective End Date");
             vCompAtt.addElement("Creator");
             vCompAtt.addElement("Date Created");
             vCompAtt.addElement("Modifier");
             vCompAtt.addElement("Date Modified");
             vCompAtt.addElement("Change Note");
             // vCompAtt.addElement("Historical CDE ID");
             vCompAtt.addElement("Permissible Value");
             // vCompAtt.addElement("Preferred Question Text Document Text");
             // vCompAtt.addElement("Historic Short CDE Name Document Text");
             vCompAtt.addElement("Alternate Names");
             vCompAtt.addElement("Reference Documents");
             vCompAtt.addElement("Derivation Relationship");
             vCompAtt.addElement("All Attributes");
         }
         else if (selSearch.equals("DataElementConcept"))
         {
             vCompAtt.addElement("Long Name");
             vCompAtt.addElement("Public ID");
             vCompAtt.addElement("Version");
             vCompAtt.addElement("Workflow Status");
             vCompAtt.addElement("Context");
             vCompAtt.addElement("Definition");
             vCompAtt.addElement("Name");
             vCompAtt.addElement("Conceptual Domain");
             vCompAtt.addElement("Origin");
             vCompAtt.addElement("Concept Name");
             vCompAtt.addElement("Effective Begin Date");
             vCompAtt.addElement("Effective End Date");
             vCompAtt.addElement("Creator");
             vCompAtt.addElement("Date Created");
             vCompAtt.addElement("Modifier");
             vCompAtt.addElement("Date Modified");
             vCompAtt.addElement("Change Note");
             vCompAtt.addElement("Alternate Names");
             vCompAtt.addElement("Reference Documents");
             vCompAtt.addElement("All Attributes");
         }
         else if (selSearch.equals("ValueDomain"))
         {
             vCompAtt.addElement("Long Name");
             vCompAtt.addElement("Public ID");
             vCompAtt.addElement("Version");
             vCompAtt.addElement("Workflow Status");
             vCompAtt.addElement("Context");
             vCompAtt.addElement("Definition");
             vCompAtt.addElement("Name");
             vCompAtt.addElement("Conceptual Domain");
             vCompAtt.addElement("Data Type");
             vCompAtt.addElement("Origin");
             vCompAtt.addElement("Concept Name");
             vCompAtt.addElement("Effective Begin Date");
             vCompAtt.addElement("Effective End Date");
             vCompAtt.addElement("Creator");
             vCompAtt.addElement("Date Created");
             vCompAtt.addElement("Modifier");
             vCompAtt.addElement("Date Modified");
             vCompAtt.addElement("Change Note");
             vCompAtt.addElement("Unit of Measures");
             vCompAtt.addElement("Display Format");
             vCompAtt.addElement("Maximum Length");
             vCompAtt.addElement("Minimum Length");
             vCompAtt.addElement("High Value Number");
             vCompAtt.addElement("Low Value Number");
             vCompAtt.addElement("Decimal Place");
             vCompAtt.addElement("Type Flag");
             vCompAtt.addElement("Permissible Value");
             vCompAtt.addElement("Alternate Names");
             vCompAtt.addElement("Reference Documents");
             vCompAtt.addElement("All Attributes");
         }
         else if (selSearch.equals("PermissibleValue"))
         {
             vCompAtt.addElement("Value");
             vCompAtt.addElement("Effective Begin Date");
             vCompAtt.addElement("Effective End Date");
             vCompAtt.addElement("Value Meaning Long Name");
             vCompAtt.addElement("VM Public ID");
             vCompAtt.addElement("VM Version");
             vCompAtt.addElement("VM Description");
             vCompAtt.addElement("Conceptual Domain");
             vCompAtt.addElement("EVS Identifier");
             vCompAtt.addElement("Description Source");
             vCompAtt.addElement("Vocabulary");
             vCompAtt.addElement("All Attributes");
         }
         else if (selSearch.equals("ValueMeaning"))
         {
         	vCompAtt.addElement("Long Name");
         	vCompAtt.addElement("Public ID");
         	vCompAtt.addElement("Version");
         	vCompAtt.addElement("Workflow Status");
         	vCompAtt.addElement("EVS Identifier");
         	vCompAtt.addElement("Conceptual Domain");
         	vCompAtt.addElement("Definition");
             vCompAtt.addElement("All Attributes");
             DataManager.setAttribute(session, "creSelectedAttr", vCompAtt);
         }
         else if (selSearch.equals("ParentConcept") || selSearch.equals("PV_ValueMeaning"))
         {
             vCompAtt.addElement("Concept Name");
             vCompAtt.addElement("EVS Identifier");
             vCompAtt.addElement("Definition");
             vCompAtt.addElement("Definition Source");
             vCompAtt.addElement("Workflow Status");
             vCompAtt.addElement("Semantic Type");
             vCompAtt.addElement("Vocabulary");
             DataManager.setAttribute(session, "creSelectedAttr", vCompAtt);
         }
         else if (selSearch.equals("ParentConceptVM"))
         {
             vCompAtt.addElement("Concept Name");
             vCompAtt.addElement("EVS Identifier");
             vCompAtt.addElement("Definition");
             vCompAtt.addElement("Definition Source");
             vCompAtt.addElement("Workflow Status");
             vCompAtt.addElement("Semantic Type");
             vCompAtt.addElement("Vocabulary");
             vCompAtt.addElement("Level");
             DataManager.setAttribute(session, "creSelectedAttr", vCompAtt);
         }
         else if (selSearch.equals("Questions"))
         {
             vCompAtt.addElement("Question Text");
             vCompAtt.addElement("DE Long Name");
             vCompAtt.addElement("DE Public ID");
             vCompAtt.addElement("Question Public ID");
             vCompAtt.addElement("Origin");
             vCompAtt.addElement("Workflow Status");
             vCompAtt.addElement("Value Domain");
             vCompAtt.addElement("Context");
             vCompAtt.addElement("Protocol ID");
             vCompAtt.addElement("CRF Name");
             vCompAtt.addElement("Highlight Indicator");
             vCompAtt.addElement("All Attributes");
         }
         else if (selSearch.equals("ConceptualDomain"))
         {
             vCompAtt.addElement("Long Name");
             vCompAtt.addElement("Public ID");
             vCompAtt.addElement("Version");
             vCompAtt.addElement("Workflow Status");
             vCompAtt.addElement("Context");
             vCompAtt.addElement("Definition");
             vCompAtt.addElement("Name");
             vCompAtt.addElement("Origin");
             // vCompAtt.addElement("Concept Name");
             vCompAtt.addElement("Effective Begin Date");
             vCompAtt.addElement("Effective End Date");
             vCompAtt.addElement("Creator");
             vCompAtt.addElement("Date Created");
             vCompAtt.addElement("Modifier");
             vCompAtt.addElement("Date Modified");
             vCompAtt.addElement("Change Note");
             vCompAtt.addElement("All Attributes");
         }
         else if (selSearch.equals("ClassSchemeItems"))
         {
             vCompAtt.addElement("CSI Name");
             vCompAtt.addElement("CSI Type");
             vCompAtt.addElement("CSI Definition");
             vCompAtt.addElement("CS Long Name");
             vCompAtt.addElement("CS Public ID");
             vCompAtt.addElement("CS Version");
             // vCompAtt.addElement("Concept Name");
             vCompAtt.addElement("Context");
             vCompAtt.addElement("All Attributes");
         }
         else if (selSearch.equals("ObjectClass") || selSearch.equals("Property") || selSearch.equals("PropertyClass")
                         || selSearch.equals("RepTerm") || selSearch.equals("VDObjectClass")
                         || selSearch.equals("VDProperty") || selSearch.equals("VDPropertyClass")
                         || selSearch.equals("VDRepTerm") || selSearch.equals("RepQualifier")
                         || selSearch.equals("ObjectQualifier") || selSearch.equals("PropertyQualifier")
                         || selSearch.equals("VDRepQualifier") || selSearch.equals("VDObjectQualifier")
                         || selSearch.equals("VDPropertyQualifier") || selSearch.equals("EVSValueMeaning")
                         || selSearch.equals("CreateVM_EVSValueMeaning") || selSearch.equals("ConceptClass")
                         || selSearch.equals("VMConcept") || selSearch.equals("EditVMConcept"))
         {
             boolean isMainConcept = false;
             if (!sMenu.equals("searchForCreate") && selSearch.equals("ConceptClass"))
                 isMainConcept = true;
             vCompAtt.addElement("Concept Name");
             vCompAtt.addElement("Public ID");
             vCompAtt.addElement("Version");
             vCompAtt.addElement("EVS Identifier");
             if (isMainConcept) // add here if main concept search
                 vCompAtt.addElement("Vocabulary");
             vCompAtt.addElement("Definition");
             vCompAtt.addElement("Definition Source");
             vCompAtt.addElement("Workflow Status");
             if (sMenu.equals("searchForCreate")) // add this only if search for create which as evs search
                 vCompAtt.addElement("Semantic Type");
             vCompAtt.addElement("Context");
             if (!isMainConcept) // add here if not main concept search
                 vCompAtt.addElement("Vocabulary");
             vCompAtt.addElement("caDSR Component");
             if (selSearch.equals("ObjectClass") || selSearch.equals("Property") || selSearch.equals("PropertyClass")
                             || selSearch.equals("VDObjectClass") || selSearch.equals("VDProperty")
                             || selSearch.equals("VDPropertyClass"))
                 vCompAtt.addElement("DEC's Using");
             DataManager.setAttribute(session, "creSelectedAttr", vCompAtt);
         }
         // store it in the session
         if (sMenu.equals("searchForCreate"))
             DataManager.setAttribute(session, "creAttributeList", vCompAtt);
         else
             DataManager.setAttribute(session, "serAttributeList", vCompAtt);
         Vector vSelectedAttr = (Vector) session.getAttribute("creSelectedAttr");
         if (vSelectedAttr == null || selSearch.equals("ReferenceValue"))
         {
             DataManager.setAttribute(session, "creSelectedAttr", vCompAtt);
             DataManager.setAttribute(session, "creSearchAC", selSearch);
         }
     } // end compattlist
 
     /**
      * To get the default filter by attributes for the selected Component.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     private void getDefaultFilterAtt() throws Exception
     {
         HttpSession session = m_classReq.getSession();
         String menuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
         // reset to default filter by criteria
         if (!menuAction.equals("searchForCreate"))
         {
             DataManager.setAttribute(session, "serStatus", new Vector());
             DataManager.setAttribute(session, "serMultiContext", new Vector());
             DataManager.setAttribute(session, "serContext", ""); // keep the old context criteria
             DataManager.setAttribute(session, "serContextUse", ""); // store contextUse in the session
             DataManager.setAttribute(session, "serVersion", ""); // store version in the session
             DataManager.setAttribute(session, "serVDTypeEnum", ""); // store VDType Enum in the session
             DataManager.setAttribute(session, "serVDTypeNonEnum", ""); // store VDType Non Enum in the session
             DataManager.setAttribute(session, "serVDTypeRef", ""); // store VDType Ref in the session
             DataManager.setAttribute(session, "serRegStatus", ""); // store regstatus in the session
             DataManager.setAttribute(session, "serDerType", ""); // store derivation Type in the session
             DataManager.setAttribute(session, "serCreatedFrom", "");
             DataManager.setAttribute(session, "serCreatedTo", "");
             DataManager.setAttribute(session, "serModifiedFrom", "");
             DataManager.setAttribute(session, "serModifiedTo", "");
             DataManager.setAttribute(session, "serCreator", "");
             DataManager.setAttribute(session, "serModifier", "");
             DataManager.setAttribute(session, "serKeyword", "");
             DataManager.setAttribute(session, "serProtoID", "");
             DataManager.setAttribute(session, "serSearchIn", "longName"); // make default to longName
             DataManager.setAttribute(session, "selCS", "");
             DataManager.setAttribute(session, "serSelectedCD", "");
             // reset the appened attributes
             session.setAttribute("recsFound", "No ");
             DataManager.setAttribute(session, "CheckList", new Vector());
             DataManager.setAttribute(session, "AppendAction", "Not Appended");
             DataManager.setAttribute(session, "vSelRows", new Vector());
             DataManager.setAttribute(session, "results", new Vector());
         }
         else
         {
             DataManager.setAttribute(session, "creStatus", new Vector());
             DataManager.setAttribute(session, "creMultiContext", new Vector()); // keep the old context criteria
             DataManager.setAttribute(session, "creContext", ""); // keep the old context criteria
             DataManager.setAttribute(session, "creContextUse", ""); // store contextUse in the session
             DataManager.setAttribute(session, "creVersion", ""); // store version in the session
             DataManager.setAttribute(session, "creVDTypeEnum", ""); // store VDType Enum in the session
             DataManager.setAttribute(session, "creVDTypeNonEnum", ""); // store VDType Non Enum in the session
             DataManager.setAttribute(session, "creVDTypeRef", ""); // store VDType Ref in the session
             DataManager.setAttribute(session, "creRegStatus", ""); // store regstatus in the session
             DataManager.setAttribute(session, "creDerType", ""); // store derivation Type in the session
             DataManager.setAttribute(session, "creCreatedFrom", "");
             DataManager.setAttribute(session, "creCreatedTo", "");
             DataManager.setAttribute(session, "creModifiedFrom", "");
             DataManager.setAttribute(session, "creModifiedTo", "");
             DataManager.setAttribute(session, "creCreator", "");
             DataManager.setAttribute(session, "creModifier", "");
             DataManager.setAttribute(session, "creKeyword", "");
             DataManager.setAttribute(session, "creProtoID", "");
             m_classReq.setAttribute("creSearchIn", "longName"); // make default to longName
             DataManager.setAttribute(session, "creSelectedCD", "");
         }
     }
 
     /**
      * To get the default attributes for the selected Component.
      *
      * @param searchAC
      *            String The selected Administered component
      * @param sSearchIn
      *            String The selected search in filter.
      *
      * @return Vector selected attribute Vector
      *
      * @throws Exception
      */
     private Vector<String> getDefaultAttr(String searchAC, String sSearchIn) throws Exception
     {
         Vector<String> vDefaultAttr = new Vector<String>();
         if (searchAC == null)
             searchAC = "DataElement";
         if (sSearchIn == null)
             sSearchIn = "longName";
         // store the default attributes to select and set some default attributes
         if (searchAC.equals("PermissibleValue"))
         {
             vDefaultAttr.addElement("Value");
             vDefaultAttr.addElement("Value Meaning Long Name");
             vDefaultAttr.addElement("VM Public ID");
             vDefaultAttr.addElement("VM Version");
             vDefaultAttr.addElement("VM Description");
             vDefaultAttr.addElement("Conceptual Domain");
             vDefaultAttr.addElement("EVS Identifier");
             vDefaultAttr.addElement("Definition Source");
             vDefaultAttr.addElement("Vocabulary");
         }
         else if (searchAC.equals("ValueMeaning"))
         {
             vDefaultAttr.addElement("Long Name");
             vDefaultAttr.addElement("Public ID");
             vDefaultAttr.addElement("Version");
             vDefaultAttr.addElement("Workflow Status");
             vDefaultAttr.addElement("EVS Identifier");
             vDefaultAttr.addElement("Conceptual Domain");
             vDefaultAttr.addElement("Definition");
 
         }
         else if (searchAC.equals("Questions"))
         {
             vDefaultAttr.addElement("Question Text");
             vDefaultAttr.addElement("DE Long Name");
             vDefaultAttr.addElement("DE Public ID");
             vDefaultAttr.addElement("Workflow Status");
             vDefaultAttr.addElement("Value Domain");
         }
         else if (searchAC.equals("ObjectClass") || searchAC.equals("Property"))
         {
             vDefaultAttr.addElement("Concept Name");
             vDefaultAttr.addElement("Public ID");
             vDefaultAttr.addElement("Version");
             vDefaultAttr.addElement("EVS Identifier");
             vDefaultAttr.addElement("Definition");
             vDefaultAttr.addElement("Definition Source");
             vDefaultAttr.addElement("Context");
             vDefaultAttr.addElement("Vocabulary");
             vDefaultAttr.addElement("DEC's Using");
         }
         else if (searchAC.equals("ConceptClass"))
         {
             vDefaultAttr.addElement("Concept Name");
             vDefaultAttr.addElement("Public ID");
             vDefaultAttr.addElement("EVS Identifier");
             vDefaultAttr.addElement("Vocabulary");
             vDefaultAttr.addElement("Definition");
             vDefaultAttr.addElement("Definition Source");
             vDefaultAttr.addElement("Context");
         }
         else if (searchAC.equals("ClassSchemeItems"))
         {
             vDefaultAttr.addElement("CSI Name");
             vDefaultAttr.addElement("CSI Type");
             vDefaultAttr.addElement("CSI Definition");
             vDefaultAttr.addElement("CS Long Name");
             vDefaultAttr.addElement("CS Public ID");
             vDefaultAttr.addElement("CS Version");
             vDefaultAttr.addElement("Context");
         }
        /* else if (searchAC.equals("RepTerm"))
         {
             vDefaultAttr.addElement("Concept Name");
             vDefaultAttr.addElement("Definition");
             vDefaultAttr.addElement("Definition Source");
         }*/
         else
         {
             vDefaultAttr.addElement("Long Name");
             vDefaultAttr.addElement("Public ID");
             vDefaultAttr.addElement("Version");
             if (searchAC.equals("DataElement"))
                 vDefaultAttr.addElement("Registration Status");
             vDefaultAttr.addElement("Workflow Status");
             // only if search is Data element
             if (searchAC.equals("DataElement"))
             {
                 vDefaultAttr.addElement("Owned By Context");
                 vDefaultAttr.addElement("Used By Context");
             }
             else
                 vDefaultAttr.addElement("Context");
             vDefaultAttr.addElement("Definition");
             // only if search is Data element
             if (searchAC.equals("DataElement"))
             {
                 vDefaultAttr.addElement("Data Element Concept");
                 vDefaultAttr.addElement("Value Domain");
             }
         }
         return vDefaultAttr;
     }
 
     /**
      * default attributes for evs searches
      *
      * @param req
      *            HttpServletRequest
      * @param res
      *            HttpServletResponse
      * @param dtsVocab
      *            String vocab name
      * @throws Exception
      */
     private void getDefaultBlockAttr(String dtsVocab) throws Exception
     {
         HttpSession session = m_classReq.getSession();
         DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, "searchForCreate");
         Vector vResult = new Vector();
         DataManager.setAttribute(session, "results", vResult);
         DataManager.setAttribute(session, "creRecsFound", "No ");
         // defuault filter attributes
         String sSearchInEVS = "Name";
         DataManager.setAttribute(session, "dtsVocab", dtsVocab);
         DataManager.setAttribute(session, "SearchInEVS", sSearchInEVS);
         DataManager.setAttribute(session, "creSearchInBlocks", "longName");
         DataManager.setAttribute(session, "creContextBlocks", "All Contexts");
         DataManager.setAttribute(session, "creStatusBlocks", "RELEASED");
         DataManager.setAttribute(session, "creRetired", "Exclude");
         DataManager.setAttribute(session, "MetaSource", "All Sources");
         // get default attributes
         Vector vSel = (Vector) session.getAttribute("creAttributeList");
         Vector vSelClone = (Vector) vSel.clone();
         vSelClone.remove("Version");
         DataManager.setAttribute(session, "creSelectedAttr", vSelClone);
         // make default tree
         this.doEVSSearchActions("defaultBlock", m_classReq, m_classRes);
         // this.doCollapseAllNodes(req, dtsVocab);
     }
 
 	/**
 	 * Gets the default Rep term Context
 	 */
 	private void getRepTermDefaultContext()
     {
 		String defaultContext=null;
 		Statement stm =null;
 		ResultSet rs =null;
 		HttpSession session = m_classReq.getSession(true);
         try
         {
             String sQuery = "select value from sbrext.tool_options_view_ext where property like 'REPTERM.DEFAULT.CONTEXT'";
             stm = m_conn.createStatement();
 			rs = stm.executeQuery(sQuery);
 			while(rs.next())
 			{
 				defaultContext= rs.getString(1);
 			}
             session.setAttribute("defaultRepTermContext", defaultContext);
         }
         catch (Exception e)
         {
             logger.error("Error - getRepTermDefaultContext : " + e.toString(), e);
         }finally{
         	SQLHelper.closeResultSet(rs);
         	SQLHelper.closeStatement(stm);
         }
     }
 
 	/**
 	 * Get the approved list of Rep Terms for display
 	 */
 	private void getApprovedRepTerm() {
 		Vector vResult = new Vector();
 		String valueString = new String();
 		HttpSession session = m_classReq.getSession(true);
 		ConceptServlet conSer = new ConceptServlet(m_classReq, m_classRes, this);
 		ConceptAction conact = new ConceptAction();
 		ResultSet rs =null;
 		Statement stm=null;
 		ResultSet rs1 =null;
 		Statement stm1=null;
 		try {
 			 stm = m_conn.createStatement();
 			rs = stm
 					.executeQuery("SELECT rep.Preferred_name FROM (SELECT xx.rep_idseq, COUNT(*) AS cnt FROM sbrext.representations_view_ext xx, sbrext.component_concepts_view_ext cc WHERE xx.asl_name = 'RELEASED' AND cc.condr_idseq = xx.condr_idseq GROUP BY xx.rep_idseq ORDER BY cnt ASC) hits, sbrext.representations_view_ext rep, sbr.ac_registrations_view reg WHERE hits.cnt = 1 AND rep.rep_idseq = hits.rep_idseq AND reg.ac_idseq = rep.rep_idseq AND reg.registration_status = 'Standard'");
 			if (rs.next()) {
 				do {
 					valueString += "'" + rs.getString(1) + "',";
 				} while (rs.next());
 				String valu = valueString
 						.substring(0, valueString.length() - 1);
 				String sql = "SELECT con.*,cont.name as Context FROM CONCEPTS_VIEW_EXT con,CONTEXTS_VIEW cont WHERE con.CONTE_IDSEQ=cont.CONTE_IDSEQ and PREFERRED_NAME IN ("
 						+ valu + ") ORDER BY con.long_name ASC";
 			   stm1 = m_conn.createStatement();
 			   rs1 = stm1.executeQuery(sql);
 				if (rs1 != null) {
 					conact.getApprovedRepTermConcepts(rs1, conSer.getData());
 					DataManager.setAttribute(session, "vACSearch", conSer
 							.getData().getConceptList());
 				}
 			} else {
 				Boolean approvedRep = new Boolean(false);
 				session.setAttribute("ApprovedRepTerm", approvedRep);
 			}
 
 		} catch (SQLException e) {
 			logger.error("Error getting the Approved Rep Terms",e);
 		}
 		finally{
 			SQLHelper.closeResultSet(rs);
 			SQLHelper.closeStatement(stm);
 			SQLHelper.closeResultSet(rs1);
 			SQLHelper.closeStatement(stm1);
 		}
 		EVSSearch evs = new EVSSearch(m_classReq, m_classRes, this);
 		evs.get_Result(m_classReq, m_classRes, vResult, "");
 		DataManager.setAttribute(m_classReq.getSession(), "results", vResult);
 	}
 
     /**
      * To open search page when clicked on edit, create new from template, new version on the menu. Called from
      * 'service' method where reqType is 'actionFromMenu' Sets the attribte 'searchAC' to the selected component. Sets
      * the attribte 'MenuAction' to the selected menu action. Makes empty 'results' session vector. stores 'No ' to
      * 'recsFound' session attribute. forwards page 'SearchResultsPage.jsp'.
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     private void doMenuAction() throws Exception
     {
         HttpSession session = m_classReq.getSession();
        // this.clearSessionAttributes(m_classReq, m_classRes);
        // this.clearBuildingBlockSessionAttributes(m_classReq, m_classRes);
        // this.clearCreateSessionAttributes(m_classReq, m_classRes);
         String sMAction = (String) m_classReq.getParameter("hidMenuAction");
         if (sMAction == null)
             sMAction = "nothing";
         DataManager.setAttribute(session, "DDEAction", "nothing"); // reset from "CreateNewDEFComp"
         String searchAC = "DataElement";
         // sets the session attributes of the selection menu action and selected component
         if (sMAction.equals("editDE") || sMAction.equals("editDEC") || sMAction.equals("editVD"))
             DataManager.setAttribute(session, "LastMenuButtonPressed", "Edit");
         else if (sMAction.equals("NewDETemplate") || sMAction.equals("NewDEVersion")
                         || sMAction.equals("NewDECTemplate") || sMAction.equals("NewDECVersion")
                         || sMAction.equals("NewVDTemplate") || sMAction.equals("NewVDVersion"))
             DataManager.setAttribute(session, "LastMenuButtonPressed", "CreateTemplateVersion");
         if ((sMAction == null) || (sMAction.equals("nothing")) || (sMAction.equals("Questions")))
             sMAction = "nothing";
         else
         {
             if ((sMAction.equals("NewDETemplate")) || (sMAction.equals("NewDEVersion")) || (sMAction.equals("editDE")))
                 searchAC = "DataElement";
             else if ((sMAction.equals("NewDECTemplate")) || (sMAction.equals("NewDECVersion"))
                             || (sMAction.equals("editDEC")))
                 searchAC = "DataElementConcept";
             else if ((sMAction.equals("NewVDTemplate")) || (sMAction.equals("NewVDVersion"))
                             || (sMAction.equals("editVD")))
             {
                 searchAC = "ValueDomain";
                 DataManager.setAttribute(session, "originAction", "NewVDTemplate");
                 DataManager.setAttribute(session, "VDEditAction", "editVD");
                 this.clearBuildingBlockSessionAttributes(m_classReq, m_classRes);
             }
         }
         DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, sMAction);
         DataManager.setAttribute(session, "searchAC", searchAC);
         // sets the default attributes and resets to empty result vector
         Vector vResult = new Vector();
         DataManager.setAttribute(session, "results", vResult);
         session.setAttribute("recsFound", "No ");
        
         //DataManager.setAttribute(session, "serKeyword", "");
         DataManager.setAttribute(session, "serProtoID", "");
         DataManager.setAttribute(session, "LastAppendWord", "");
         // remove the status message if any
         DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, "");
         DataManager.setAttribute(session, "vStatMsg", new Vector());
         // set it to longname be default
        // String sSearchIn = "longName";
         //Vector vSelVector = new Vector();
         // call the method to get default attributes
         //vSelVector = getDefaultAttr(searchAC, sSearchIn);
         //DataManager.setAttribute(session, "selectedAttr", vSelVector);
         //this.getDefaultFilterAtt(); // default filter by attributes
         //this.getCompAttrList(searchAC, sMAction); // call the method to get attribute list for the selected AC
         //ForwardJSP(m_classReq, m_classRes, "/SearchResultsPage.jsp");
     }
     
     public void getInitialListFromCadsr(GetACService getAC){
     	HttpSession session = m_classReq.getSession();
     	// get initial list from cadsr
         Vector vList = null;
         String aURL = null;
         String dName = null;
         vList = new Vector();
         vList = getAC.getToolOptionData("EVS", "NEWTERM.URL", "");
         aURL = null;
         if (vList != null && vList.size() > 0)
         {
             TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean) vList.elementAt(0);
             if (tob != null)
                 aURL = tob.getVALUE();
         }
         ToolURL.setEVSNewTermURL(session, aURL);
         vList = new Vector();
         vList = getAC.getToolOptionData("CDEBrowser", "URL", "");
         aURL = null;
         if (vList != null && vList.size() > 0)
         {
             TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean) vList.elementAt(0);
             if (tob != null)
                 aURL = tob.getVALUE();
         }
         ToolURL.setBrowserUrl(session, aURL);
         vList = new Vector();
         vList = getAC.getToolOptionData("CDEBrowser", "DISPLAY.NAME", "");
         dName = null;
         if (vList != null && vList.size() > 0)
         {
             TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean) vList.elementAt(0);
             if (tob != null)
             	dName = tob.getVALUE();
         }
         if (dName != null){
            ToolURL.setBrowserDispalyName(session, dName);
         }else{
            ToolURL.setBrowserDispalyName(session, "CDE Browser");
         }
         
         vList = new Vector();
         vList = getAC.getToolOptionData("SENTINEL", "URL", "");
         aURL = null;
         if (vList != null && vList.size() > 0)
         {
             TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean) vList.elementAt(0);
             if (tob != null)
                 aURL = tob.getVALUE();
         }
         ToolURL.setSentinelUrl(session, aURL);
         vList = new Vector();
         vList = getAC.getToolOptionData("SENTINEL", "DISPLAY.NAME", "");
         dName = null;
         if (vList != null && vList.size() > 0)
         {
             TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean) vList.elementAt(0);
             if (tob != null)
             	dName = tob.getVALUE();
         }
         if (dName != null){
           ToolURL.setSentinelDispalyName(session, dName);
         }else{
           ToolURL.setSentinelDispalyName(session, "caDSR Sentinel Tool");
         }
         vList = new Vector();
         vList = getAC.getToolOptionData("UMLBrowser", "URL", "");
         aURL = null;
         if (vList != null && vList.size() > 0)
         {
             TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean) vList.elementAt(0);
             if (tob != null)
                 aURL = tob.getVALUE();
         }
         ToolURL.setUmlBrowserUrl(session, aURL);
         vList = new Vector();
         vList = getAC.getToolOptionData("UMLBrowser", "DISPLAY.NAME", "");
         dName = null;
         if (vList != null && vList.size() > 0)
         {
             TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean) vList.elementAt(0);
             if (tob != null)
             	dName = tob.getVALUE();
         }
         if (dName != null){
           ToolURL.setUmlBrowserDispalyName(session, dName);
         }else{
         	ToolURL.setUmlBrowserDispalyName(session, "UML Model Browser");
         }
        
         vList = new Vector();
         vList = getAC.getToolOptionData("FREESTYLE", "URL", "");
         aURL = null;
         if (vList != null && vList.size() > 0)
         {
             TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean) vList.elementAt(0);
             if (tob != null)
                 aURL = tob.getVALUE();
         }
         ToolURL.setFreeStyleUrl(session, aURL);
         vList = new Vector();
         vList = getAC.getToolOptionData("FREESTYLE", "DISPLAY.NAME", "");
         dName = null;
         if (vList != null && vList.size() > 0)
         {
             TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean) vList.elementAt(0);
             if (tob != null)
             	dName = tob.getVALUE();
         }
         if (dName!= null){
           ToolURL.setFreeStyleDispalyName(session, dName);
         }else{
         	ToolURL.setFreeStyleDispalyName(session, "caDSR Freestyle");
         }
         vList = new Vector();
         vList = getAC.getToolOptionData("AdminTool", "URL", "");
         aURL = null;
         if (vList != null && vList.size() > 0)
         {
             TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean) vList.elementAt(0);
             if (tob != null)
                 aURL = tob.getVALUE();
         }
         ToolURL.setAdminToolUrl(session, aURL);
         vList = new Vector();
         vList = getAC.getToolOptionData("AdminTool", "DISPLAY.NAME", "");
         dName = null;
         if (vList != null && vList.size() > 0)
         {
             TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean) vList.elementAt(0);
             if (tob != null)
             	dName = tob.getVALUE();
         }
         if (dName != null){
            ToolURL.setAdminToolDispalyName(session, dName);
         }else{
            ToolURL.setAdminToolDispalyName(session, "Admin Tool");
         }
         
         vList = new Vector();
         vList = getAC.getToolOptionData("CADSRAPI", "URL", "");
         aURL = null;
         if (vList != null && vList.size() > 0)
         {
             TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean) vList.elementAt(0);
             if (tob != null)
                 aURL = tob.getVALUE();
         }
         ToolURL.setCadsrAPIUrl(session, aURL);
         vList = new Vector();
         vList = getAC.getToolOptionData("CADSRAPI", "DISPLAY.NAME", "");
         dName = null;
         if (vList != null && vList.size() > 0)
         {
             TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean) vList.elementAt(0);
             if (tob != null)
             	dName = tob.getVALUE();
         }
         if(dName != null){
            ToolURL.setCadsrAPIDispalyName(session, dName);
         }else{
            ToolURL.setCadsrAPIDispalyName(session, "caDSR API Home");
         }
         
         vList = new Vector();
         vList = getAC.getToolOptionData("CURATION", "HELP.HOME", "");
         aURL = null;
         if (vList != null && vList.size() > 0)
         {
             TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean) vList.elementAt(0);
             if (tob != null)
                 aURL = tob.getVALUE();
         }
         ToolURL.setCurationToolHelpURL(session, aURL);
         session.setAttribute("curationToolHelpURL", aURL);
         
         vList = new Vector();
         vList = getAC.getToolOptionData("CURATION", "BUSINESS.RULES.URL", "");
         aURL = null;
         if (vList != null && vList.size() > 0)
         {
             TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean) vList.elementAt(0);
             if (tob != null)
                 aURL = tob.getVALUE();
         }
         ToolURL.setCurationToolBusinessRulesURL(session, aURL);
       
     }
     
     /**
      * to get reference documents for the selected ac and doc type called when the reference docuemnts window opened
      * first time and calls 'getAC.getReferenceDocuments' forwards page back to reference documents
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     private void doRefDocSearchActions() throws Exception
     {
         GetACSearch getAC = new GetACSearch(m_classReq, m_classRes, this);
         String acID = m_classReq.getParameter("acID");
         String itemType = m_classReq.getParameter("itemType");
         @SuppressWarnings("unused") Vector vRef = getAC.doRefDocSearch(acID, itemType, "open");
         ForwardJSP(m_classReq, m_classRes, "/ReferenceDocumentWindow.jsp");
     }
 
     /**
      * to get alternate names for the selected ac and doc type called when the alternate names window opened first time
      * and calls 'getAC.getAlternateNames' forwards page back to alternate name window jsp
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     private void doAltNameSearchActions() throws Exception
     {
         GetACSearch getAC = new GetACSearch(m_classReq, m_classRes, this);
         String acID = m_classReq.getParameter("acID");
         String CD_ID = m_classReq.getParameter("CD_ID");
         if (CD_ID == null)
             CD_ID = "";
         String itemType = m_classReq.getParameter("itemType");
         if (itemType != null && itemType.equals("ALL"))
             itemType = "";
         @SuppressWarnings("unused") Vector vAlt = getAC.doAltNameSearch(acID, itemType, CD_ID, "other", "open");
         ForwardJSP(m_classReq, m_classRes, "/AlternateNameWindow.jsp");
     }
 
     /**
      * to get Permissible Values for the selected ac called when the permissible value window opened first time and
      * calls 'getAC.doPVACSearch' forwards page back to Permissible Value window jsp
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     private void doPermValueSearchActions() throws Exception
     {
       //  GetACSearch getAC = new GetACSearch(req, res, this);
         String acID = m_classReq.getParameter("acID");
         String acName = m_classReq.getParameter("itemType"); // ac name for pv
         if (acName != null && acName.equals("ALL"))
             acName = "-";
         String sConteIdseq = (String) m_classReq.getParameter("sConteIdseq");
         if (sConteIdseq == null)
             sConteIdseq = "";
     	ValueDomainServlet vdServ = (ValueDomainServlet) this.getACServlet("ValueDomain");
         PVServlet pvser = new PVServlet(m_classReq, m_classRes, vdServ);
         pvser.searchVersionPV(null, 0, acID, acName);
         ForwardJSP(m_classReq, m_classRes, "/PermissibleValueWindow.jsp");
     }
 
     /**
      * display all the concepts for the selected ac from search results page
      *
      * @param req
      *            HttpServletRequest
      * @param res
      *            HttpServletResponse
      * @throws Exception
      */
     private void doConClassSearchActions() throws Exception
     {
         GetACSearch getAC = new GetACSearch(m_classReq, m_classRes, this);
         String acID = m_classReq.getParameter("acID"); // dec id
         String ac2ID = m_classReq.getParameter("ac2ID"); // vd id
         //String acType = req.getParameter("acType"); // actype to search
         String acName = m_classReq.getParameter("acName"); // ac name for pv
         // call the api to return concept attributes according to ac type and ac idseq
         Vector<EVS_Bean> conList = new Vector<EVS_Bean>();
         conList = getAC.do_ConceptSearch("", "", "", "", "", acID, ac2ID, conList);
         m_classReq.setAttribute("ConceptClassList", conList);
         m_classReq.setAttribute("ACName", acName);
         // store them in request parameter to display and forward the page
         ForwardJSP(m_classReq, m_classRes, "/ConceptClassDetailWindow.jsp");
     }
 
     /**
      * display conceptual for the selected vm from the search results.
      *
      * @param req
      *            HttpServletRequest
      * @param res
      *            HttpServletResponse
      * @throws Exception
      */
     private void doConDomainSearchActions() throws Exception
     {
         GetACSearch getAC = new GetACSearch(m_classReq, m_classRes, this);
         String sVM = m_classReq.getParameter("acName"); // ac name for pv
         // call the api to return concept attributes according to ac type and ac idseq
         Vector cdList = new Vector();
         cdList = getAC.doCDSearch("", "", "", "", "", "", "", "", "", "", "", "", "", "", 0, "", "", sVM, cdList); 
         m_classReq.setAttribute("ConDomainList", cdList);
         m_classReq.setAttribute("VMName", sVM);
         // store them in request parameter to display and forward the page
         ForwardJSP(m_classReq, m_classRes, "/ConDomainDetailWindow.jsp");
     }
 
     /**
      * to get Protocol CRF for the selected ac called when the ProtoCRF window opened first time and calls
      * 'getAC.doProtoCRFSearch' forwards page back to ProtoCRFwindow jsp
      *
      * @param req
      *            The HttpServletRequest from the client
      * @param res
      *            The HttpServletResponse back to the client
      *
      * @throws Exception
      */
     private void doProtoCRFSearchActions() throws Exception
     {
         GetACSearch getAC = new GetACSearch(m_classReq, m_classRes, this);
         String acID = m_classReq.getParameter("acID");
         String acName = m_classReq.getParameter("itemType"); // ac name for proto crf
         if (acName != null && acName.equals("ALL"))
             acName = "-";
         @SuppressWarnings("unused") Integer pvCount = getAC.doProtoCRFSearch(acID, acName);
         ForwardJSP(m_classReq, m_classRes, "/ProtoCRFWindow.jsp");
     }
 
     
 }
