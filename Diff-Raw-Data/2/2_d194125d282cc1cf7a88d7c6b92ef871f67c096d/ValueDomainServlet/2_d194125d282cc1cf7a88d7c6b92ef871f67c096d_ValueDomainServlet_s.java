 package gov.nih.nci.cadsr.cdecurate.tool;
 
 import java.util.Vector;
 
 import gov.nih.nci.cadsr.cdecurate.ui.AltNamesDefsSession;
 import gov.nih.nci.cadsr.cdecurate.util.DataManager;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 public class ValueDomainServlet extends CurationServlet {
 
 	public ValueDomainServlet() {
 	}
 
 	public ValueDomainServlet(HttpServletRequest req, HttpServletResponse res,
 			ServletContext sc) {
 		super(req, res, sc);
 	}
 
 	public void execute(ACRequestTypes reqType) throws Exception {	
 		
 		switch (reqType){
 			case newVDFromMenu:
 				doOpenCreateNewPages(); 
 				break;
 			case newVDfromForm:
                 doCreateVDActions();
                 break;
 			case editVD:
                 doEditVDActions();
                 break;
 			case createNewVD:
                 doOpenCreateVDPage();
                 break;
 			case validateVDFromForm:
                 doInsertVD();
 				break;
 			case viewVALUEDOMAIN:
 				doOpenViewPage();
 				break;
 			case viewVDPVSTab:
 				doViewPageTab();
 				break;
 		}
 	}	
 	
 	
     /**
      * The doOpenCreateNewPages method will set some session attributes then forward the request to a Create page.
      * Called from 'service' method where reqType is 'newDEFromMenu', 'newDECFromMenu', 'newVDFromMenu' Sets some
      * initial session attributes. Calls 'getAC.getACList' to get the Data list from the database for the selected
      * context. Sets session Bean and forwards the create page for the selected component.
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
 
         DataManager.setAttribute(session, "originAction", "NewVDFromMenu");
         DataManager.setAttribute(session, "LastMenuButtonPressed", "CreateVD");
         VD_Bean m_VD = new VD_Bean();
         m_VD.setVD_ASL_NAME("DRAFT NEW");
         m_VD.setAC_PREF_NAME_TYPE("SYS");
         DataManager.setAttribute(session, "m_VD", m_VD);
         VD_Bean oldVD = new VD_Bean();
         oldVD.setVD_ASL_NAME("DRAFT NEW");
         oldVD.setAC_PREF_NAME_TYPE("SYS");
         DataManager.setAttribute(session, "oldVDBean", oldVD);
         EVS_Bean m_OC = new EVS_Bean();
         DataManager.setAttribute(session, "m_OC", m_OC);
         EVS_Bean m_PC = new EVS_Bean();
         DataManager.setAttribute(session, "m_PC", m_PC);
         EVS_Bean m_REP = new EVS_Bean();
         DataManager.setAttribute(session, "m_REP", m_REP);
         EVS_Bean m_OCQ = new EVS_Bean();
         DataManager.setAttribute(session, "m_OCQ", m_OCQ);
         EVS_Bean m_PCQ = new EVS_Bean();
         DataManager.setAttribute(session, "m_PCQ", m_PCQ);
         EVS_Bean m_REPQ = new EVS_Bean();
         DataManager.setAttribute(session, "m_REPQ", m_REPQ);
         ForwardJSP(m_classReq, m_classRes, "/CreateVDPage.jsp");
     } // end of doOpenCreateNewPages
 
     /**
      * The doCreateVDActions method handles CreateVD or EditVD actions of the request. Called from 'service' method
      * where reqType is 'newVDfromForm' Calls 'doValidateVD' if the action is Validate or submit. Calls 'doSuggestionDE'
      * if the action is open EVS Window.
      *
      * @throws Exception
      */
     private void doCreateVDActions() throws Exception
     {
         HttpSession session = m_classReq.getSession();
         String sMenuAction = (String) m_classReq.getParameter("MenuAction");
         if (sMenuAction != null)
             DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, sMenuAction);
         String sAction = (String) m_classReq.getParameter("pageAction");
         if (sAction ==null ) sAction ="";
         DataManager.setAttribute(session, "VDPageAction", sAction); // store the page action in attribute
         String sSubAction = (String) m_classReq.getParameter("VDAction");
         DataManager.setAttribute(session, "VDAction", sSubAction);
         String sOriginAction = (String) session.getAttribute("originAction");
         //System.out.println("create vd " + sAction);
        /* if (sAction.equals("changeContext"))
             doChangeContext(req, res, "vd");
         else */if (sAction.equals("validate"))
         {
             doValidateVD();
             ForwardJSP(m_classReq, m_classRes, "/ValidateVDPage.jsp");
         }
         else if (sAction.equals("submit"))
             doSubmitVD();
         else if (sAction.equals("createPV") || sAction.equals("editPV") || sAction.equals("removePV"))
             doOpenCreatePVPage(m_classReq, m_classRes, sAction, "createVD");
         else if (sAction.equals("removePVandParent") || sAction.equals("removeParent"))
             doRemoveParent(sAction, "createVD");
 //        else if (sAction.equals("searchPV"))
 //            doSearchPV(m_classReq, m_classRes);
         else if (sAction.equals("createVM"))
             doOpenCreateVMPage(m_classReq, m_classRes, "vd");
         else if (sAction.equals("Enum") || sAction.equals("NonEnum"))
         {
             doSetVDPage("Create");
             ForwardJSP(m_classReq, m_classRes, "/CreateVDPage.jsp");
         }
         else if (sAction.equals("clearBoxes"))
         {
             String ret = clearEditsOnPage(sOriginAction, sMenuAction); // , "vdEdits");
             ForwardJSP(m_classReq, m_classRes, "/CreateVDPage.jsp");
         }
         /*
          * else if (sAction.equals("refreshCreateVD")) { doSelectParentVD(req, res); ForwardJSP(req, res,
          * "/CreateVDPage.jsp"); return; }
          */else if (sAction.equals("UseSelection"))
         {
             String nameAction = "newName";
             if (sMenuAction.equals("NewVDTemplate") || sMenuAction.equals("NewVDVersion"))
                 nameAction = "appendName";
             doVDUseSelection(nameAction);
             ForwardJSP(m_classReq, m_classRes, "/CreateVDPage.jsp");
             return;
         }
         else if (sAction.equals("RemoveSelection"))
         {
             doRemoveBuildingBlocksVD();
             // re work on the naming if new one
             VD_Bean vd = (VD_Bean) session.getAttribute("m_VD");
             EVS_Bean nullEVS = null; 
             if (!sMenuAction.equals("NewVDTemplate") && !sMenuAction.equals("NewVDVersion"))
                 vd = (VD_Bean) this.getACNames(nullEVS, "Search", vd); // change only abbr pref name
             else
                 vd = (VD_Bean) this.getACNames(nullEVS, "Remove", vd); // need to change the long name & def also
             DataManager.setAttribute(session, "m_VD", vd);
             ForwardJSP(m_classReq, m_classRes, "/CreateVDPage.jsp");
             return;
         }
         else if (sAction.equals("changeNameType"))
         {
             this.doChangeVDNameType();
             ForwardJSP(m_classReq, m_classRes, "/CreateVDPage.jsp");
         }
         /*
          * else if (sAction.equals("CreateNonEVSRef")) { doNonEVSReference(req, res); ForwardJSP(req, res,
          * "/CreateVDPage.jsp"); }
          */else if (sAction.equals("addSelectedCon"))
         {
             doSelectVMConcept(m_classReq, m_classRes, sAction);
             ForwardJSP(m_classReq, m_classRes, "/CreateVDPage.jsp");
         }
         else if (sAction.equals("sortPV"))
         {
             GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, this);
             String sField = (String) m_classReq.getParameter("pvSortColumn");
             VD_Bean vd = (VD_Bean) session.getAttribute("m_VD");
             serAC.getVDPVSortedRows(vd,sField,"create",""); // call the method to sort pv attribute
             ForwardJSP(m_classReq, m_classRes, "/CreateVDPage.jsp");
             return;
         }
         else if (sAction.equals("Store Alternate Names") || sAction.equals("Store Reference Documents"))
             this.doMarkACBeanForAltRef(m_classReq, m_classRes, "ValueDomain", sAction, "createAC");
         // add/edit or remove contacts
         else if (sAction.equals("doContactUpd") || sAction.equals("removeContact"))
         {
             VD_Bean VDBean = (VD_Bean) session.getAttribute("m_VD");
             // capture all page attributes
             m_setAC.setVDValueFromPage(m_classReq, m_classRes, VDBean);
             VDBean.setAC_CONTACTS(this.doContactACUpdates(m_classReq, sAction));
             ForwardJSP(m_classReq, m_classRes, "/CreateVDPage.jsp");
         }
         // open the DE page or search page with
         else if (sAction.equals("goBack"))
         {
             String sFor = goBackfromVD(sOriginAction, sMenuAction, "", "", "create");
             ForwardJSP(m_classReq, m_classRes, sFor);
         }
         else if (sAction.equals("vdpvstab"))
         {
             DataManager.setAttribute(session, "TabFocus", "PV");
             doValidateVD();
             ForwardJSP(m_classReq, m_classRes, "/PermissibleValue.jsp");
         }
         else if (sAction.equals("vddetailstab"))
         {
             DataManager.setAttribute(session, "TabFocus", "VD");
             doValidateVD();
             ForwardJSP(m_classReq, m_classRes, "/CreateVDPage.jsp");
         }
     }
 
     /**
      * The doEditDEActions method handles EditDE actions of the request. Called from 'service' method where reqType is
      * 'EditVD' Calls 'ValidateDE' if the action is Validate or submit. Calls 'doSuggestionDE' if the action is open EVS
      * Window.
      *
      * @throws Exception
      */
     private void doEditVDActions() throws Exception
     {
         HttpSession session = m_classReq.getSession();
         String sMenuAction = (String) m_classReq.getParameter("MenuAction");
         if (sMenuAction != null)
             DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, sMenuAction);
         String sAction = (String) m_classReq.getParameter("pageAction");
         if (sAction ==null ) sAction ="";
         DataManager.setAttribute(session, "VDPageAction", sAction); // store the page action in attribute
         String sSubAction = (String) m_classReq.getParameter("VDAction");
         DataManager.setAttribute(session, "VDAction", sSubAction);
         String sButtonPressed = (String) session.getAttribute("LastMenuButtonPressed");
         String sSearchAC = (String) session.getAttribute("SearchAC");
         if (sSearchAC == null)
             sSearchAC = "";
         String sOriginAction = (String) session.getAttribute("originAction");
         if (sAction.equals("submit"))
             doSubmitVD();
         else if (sAction.equals("validate") && sOriginAction.equals("BlockEditVD"))
             doValidateVDBlockEdit();
         else if (sAction.equals("validate"))
         {
             doValidateVD();
             ForwardJSP(m_classReq, m_classRes, "/ValidateVDPage.jsp");
         }
         else if (sAction.equals("suggestion"))
             doSuggestionDE(m_classReq, m_classRes);
         else if (sAction.equals("UseSelection"))
         {
             String nameAction = "appendName";
             if (sOriginAction.equals("BlockEditVD"))
                 nameAction = "blockName";
             doVDUseSelection(nameAction);
             ForwardJSP(m_classReq, m_classRes, "/EditVDPage.jsp");
             return;
         }
         else if (sAction.equals("RemoveSelection"))
         {
             doRemoveBuildingBlocksVD();
             // re work on the naming if new one
             VD_Bean vd = (VD_Bean) session.getAttribute("m_VD");
             EVS_Bean nullEVS = null; 
             vd = (VD_Bean) this.getACNames(nullEVS, "Remove", vd); // change only abbr pref name
             DataManager.setAttribute(session, "m_VD", vd);
             ForwardJSP(m_classReq, m_classRes, "/EditVDPage.jsp");
             return;
         }
         else if (sAction.equals("changeNameType"))
         {
             this.doChangeVDNameType();
             ForwardJSP(m_classReq, m_classRes, "/EditVDPage.jsp");
         }
         else if (sAction.equals("sortPV"))
         {
             GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, this);
             String sField = (String) m_classReq.getParameter("pvSortColumn");
             VD_Bean vd = (VD_Bean) session.getAttribute("m_VD");
             serAC.getVDPVSortedRows(vd,sField,"edit",""); // call the method to sort pv attribute
             ForwardJSP(m_classReq, m_classRes, "/EditVDPage.jsp");
             return;
         }
         else if (sAction.equals("createPV") || sAction.equals("editPV") || sAction.equals("removePV"))
             doOpenCreatePVPage(m_classReq, m_classRes, sAction, "editVD");
         else if (sAction.equals("removePVandParent") || sAction.equals("removeParent"))
             doRemoveParent(sAction, "editVD");
         else if (sAction.equals("addSelectedCon"))
         {
             doSelectVMConcept(m_classReq, m_classRes, sAction);
             ForwardJSP(m_classReq, m_classRes, "/EditVDPage.jsp");
         }
         else if (sAction.equals("Enum") || sAction.equals("NonEnum"))
         {
             doSetVDPage("Edit");
             ForwardJSP(m_classReq, m_classRes, "/EditVDPage.jsp");
         }
         else if (sAction.equals("Store Alternate Names") || sAction.equals("Store Reference Documents"))
             this.doMarkACBeanForAltRef(m_classReq, m_classRes, "ValueDomain", sAction, "editAC");
         // add/edit or remove contacts
         else if (sAction.equals("doContactUpd") || sAction.equals("removeContact"))
         {
             VD_Bean VDBean = (VD_Bean) session.getAttribute("m_VD");
             // capture all page attributes
             m_setAC.setVDValueFromPage(m_classReq, m_classRes, VDBean);
             VDBean.setAC_CONTACTS(this.doContactACUpdates(m_classReq, sAction));
             ForwardJSP(m_classReq, m_classRes, "/EditVDPage.jsp");
         }
         else if (sAction.equals("clearBoxes"))
         {
             String ret = clearEditsOnPage(sOriginAction, sMenuAction); // , "vdEdits");
             ForwardJSP(m_classReq, m_classRes, "/EditVDPage.jsp");
         }
         // open the Edit DE page or search page with
         else if (sAction.equals("goBack"))
         {
             String sFor = goBackfromVD(sOriginAction, sMenuAction, sSearchAC, sButtonPressed, "edit");
             ForwardJSP(m_classReq, m_classRes, sFor);
         }
         else if (sAction.equals("vdpvstab"))
         {
             DataManager.setAttribute(session, "TabFocus", "PV");
             doValidateVD();
             ForwardJSP(m_classReq, m_classRes, "/PermissibleValue.jsp");
         }
         else if (sAction.equals("vddetailstab"))
         {
             DataManager.setAttribute(session, "TabFocus", "VD");
             doValidateVD();
             ForwardJSP(m_classReq, m_classRes, "/EditVDPage.jsp");
         }
     }
 
     /**
      * changes the dec name type as selected
      *
      * @param sOrigin
      *            string of origin action of the ac
      * @throws java.lang.Exception
      */
     private void doChangeVDNameType() throws Exception
     {
         HttpSession session = m_classReq.getSession();
         // get teh selected type from teh page
         VD_Bean pageVD = (VD_Bean) session.getAttribute("m_VD");
         m_setAC.setVDValueFromPage(m_classReq, m_classRes, pageVD); // capture all other attributes
         String sSysName = pageVD.getAC_SYS_PREF_NAME();
         String sAbbName = pageVD.getAC_ABBR_PREF_NAME();
         String sUsrName = pageVD.getAC_USER_PREF_NAME();
         String sNameType = (String) m_classReq.getParameter("rNameConv");
         if (sNameType == null || sNameType.equals(""))
             sNameType = "SYS"; // default
         // get the existing preferred name to make sure earlier typed one is saved in the user
         String sPrefName = (String) m_classReq.getParameter("txtPreferredName");
         if (sPrefName != null && !sPrefName.equals("") && !sPrefName.equals("(Generated by the System)")
                         && !sPrefName.equals(sSysName) && !sPrefName.equals(sAbbName))
             pageVD.setAC_USER_PREF_NAME(sPrefName); // store typed one in de bean
         // reset system generated or abbr accoring
         if (sNameType.equals("SYS"))
         {
             if (sSysName == null)
                 sSysName = "";
             // limit to 30 characters
             if (sSysName.length() > 30)
                 sSysName = sSysName.substring(sSysName.length() - 30);
             pageVD.setVD_PREFERRED_NAME(sSysName);
         }
         else if (sNameType.equals("ABBR"))
             pageVD.setVD_PREFERRED_NAME(sAbbName);
         else if (sNameType.equals("USER"))
             pageVD.setVD_PREFERRED_NAME(sUsrName);
         pageVD.setAC_PREF_NAME_TYPE(sNameType); // store the type in the bean
         // logger.debug(pageVD.getAC_PREF_NAME_TYPE() + " pref " + pageVD.getVD_PREFERRED_NAME());
         DataManager.setAttribute(session, "m_VD", pageVD);
     }
 
     /**
      * Does open editVD page action from DE page called from 'doEditDEActions' method. Calls
      * 'm_setAC.setDEValueFromPage' to store the DE bean for later use Using the VD idseq, calls 'SerAC.search_VD'
      * method to gets dec attributes to populate. stores VD bean in session and opens editVD page. goes back to editDE
      * page if any error.
      *
      * @throws Exception
      */
     public void doOpenEditVDPage() throws Exception
     {
         HttpSession session = m_classReq.getSession();
         DE_Bean m_DE = (DE_Bean) session.getAttribute("m_DE");
         if (m_DE == null)
             m_DE = new DE_Bean();
         // store the de values in the session
         m_setAC.setDEValueFromPage(m_classReq, m_classRes, m_DE);
         DataManager.setAttribute(session, "m_DE", m_DE);
         this.clearBuildingBlockSessionAttributes(m_classReq, m_classRes);
         String sVDID = null;
         String sVDid[] = m_classReq.getParameterValues("selVD");
         if (sVDid != null)
             sVDID = sVDid[0];
         // get the dec bean for this id
         if (sVDID != null)
         {
             Vector vList = new Vector();
             GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, this);
             serAC.doVDSearch(sVDID, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", 0, "", "", "", "", "", "",
                             "", "", vList);
             // forward editVD page with this bean
             if (vList.size() > 0)
             {
                 for (int i = 0; i < vList.size(); i++)
                 {
                     VD_Bean VDBean = new VD_Bean();
                     VDBean = (VD_Bean) vList.elementAt(i);
                     // check if the user has write permission
                     String contID = VDBean.getVD_CONTE_IDSEQ();
                     String sUser = (String) session.getAttribute("Username");
                     GetACService getAC = new GetACService(m_classReq, m_classRes, this);
                     String hasPermit = getAC.hasPrivilege("Create", sUser, "vd", contID);
                     // forward to editVD if has write permission
                     if (hasPermit.equals("Yes"))
                     {
                         String sMenuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
                         VDBean = serAC.getVDAttributes(VDBean, "Edit", sMenuAction); // get VD other Attributes
                         DataManager.setAttribute(session, "m_VD", VDBean);
                         VD_Bean oldVD = new VD_Bean();
                         oldVD = oldVD.cloneVD_Bean(VDBean);
                         DataManager.setAttribute(session, "oldVDBean", oldVD);
                         // DataManager.setAttribute(session, "oldVDBean", VDBean);
                         ForwardJSP(m_classReq, m_classRes, "/EditVDPage.jsp"); // forward to editVD page
                     }
                     // go back to editDE with message if no permission
                     else
                     {
                         DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, "No edit permission in "
                                         + VDBean.getVD_CONTEXT_NAME() + " context");
                         ForwardJSP(m_classReq, m_classRes, "/EditDEPage.jsp"); // forward to editDE page
                     }
                     break;
                 }
             }
             // display error message and back to edit DE page
             else
             {
                 DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE,
                                 "Unable to get Existing VD attributes from the database");
                 ForwardJSP(m_classReq, m_classRes, "/EditDEPage.jsp"); // forward to editDE page
             }
         }
         // display error message and back to editDE page
         else
         {
             DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, "Unable to get the VDid from the page");
             ForwardJSP(m_classReq, m_classRes, "/EditDEPage.jsp"); // forward to editDE page
         }
     }// end doEditDECAction
 
     /**
      * Called from doCreateVDActions. Calls 'setAC.setVDValueFromPage' to set the VD data from the page. Calls
      * 'setAC.setValidatePageValuesVD' to validate the data. Loops through the vector vValidate to check if everything
      * is valid and Calls 'doInsertVD' to insert the data. If vector contains invalid fields, forwards to validation
      * page
      *
      * @throws Exception
      */
     private void doSubmitVD() throws Exception
     {
         HttpSession session = m_classReq.getSession();
         DataManager.setAttribute(session, "sVDAction", "validate");
         VD_Bean m_VD = new VD_Bean();
         EVS_Bean m_OC = new EVS_Bean();
         EVS_Bean m_PC = new EVS_Bean();
         EVS_Bean m_REP = new EVS_Bean();
         EVS_Bean m_OCQ = new EVS_Bean();
         EVS_Bean m_PCQ = new EVS_Bean();
         EVS_Bean m_REPQ = new EVS_Bean();
         GetACService getAC = new GetACService(m_classReq, m_classRes, this);
         m_setAC.setVDValueFromPage(m_classReq, m_classRes, m_VD);
         m_OC = (EVS_Bean) session.getAttribute("m_OC");
         m_PC = (EVS_Bean) session.getAttribute("m_PC");
         m_OCQ = (EVS_Bean) session.getAttribute("m_OCQ");
         m_PCQ = (EVS_Bean) session.getAttribute("m_PCQ");
         m_REP = (EVS_Bean) session.getAttribute("m_REP");
         m_REPQ = (EVS_Bean) session.getAttribute("m_REPQ");
         m_setAC.setValidatePageValuesVD(m_classReq, m_classRes, m_VD, m_OC, m_PC, m_REP, m_OCQ, m_PCQ, m_REPQ, getAC);
         DataManager.setAttribute(session, "m_VD", m_VD);
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
             ForwardJSP(m_classReq, m_classRes, "/ValidateVDPage.jsp");
         }
         else
         {
             doInsertVD();
         }
     } // end of doSumitVD
 
     /**
      * The doValidateVD method gets the values from page the user filled out, validates the input, then forwards results
      * to the Validate page Called from 'doCreateVDActions', 'doSubmitVD' method. Calls 'setAC.setVDValueFromPage' to
      * set the data from the page to the bean. Calls 'setAC.setValidatePageValuesVD' to validate the data. Stores 'm_VD'
      * bean in session. Forwards the page 'ValidateVDPage.jsp' with validation vector to display.
      *
      * @throws Exception
      */
     private void doValidateVD() throws Exception
     {
         HttpSession session = m_classReq.getSession();
         String sAction = (String) m_classReq.getParameter("pageAction");
         if (sAction == null)
             sAction = "";
         // do below for versioning to check whether these two have changed
         VD_Bean m_VD = (VD_Bean) session.getAttribute("m_VD"); // new VD_Bean();
         EVS_Bean m_OC = new EVS_Bean();
         EVS_Bean m_PC = new EVS_Bean();
         EVS_Bean m_REP = new EVS_Bean();
         EVS_Bean m_OCQ = new EVS_Bean();
         EVS_Bean m_PCQ = new EVS_Bean();
         EVS_Bean m_REPQ = new EVS_Bean();
         GetACService getAC = new GetACService(m_classReq, m_classRes, this);
         DataManager.setAttribute(session, "VDPageAction", "validate"); // store the page action in attribute
         m_setAC.setVDValueFromPage(m_classReq, m_classRes, m_VD);
         m_OC = (EVS_Bean) session.getAttribute("m_OC");
         m_PC = (EVS_Bean) session.getAttribute("m_PC");
         m_OCQ = (EVS_Bean) session.getAttribute("m_OCQ");
         m_PCQ = (EVS_Bean) session.getAttribute("m_PCQ");
         m_REP = (EVS_Bean) session.getAttribute("m_REP");
         m_REPQ = (EVS_Bean) session.getAttribute("m_REPQ");
         m_setAC.setValidatePageValuesVD(m_classReq, m_classRes, m_VD, m_OC, m_PC, m_REP, m_OCQ, m_PCQ, m_REPQ, getAC);
         DataManager.setAttribute(session, "m_VD", m_VD);
         /*
          * if(sAction.equals("Enum") || sAction.equals("NonEnum") || sAction.equals("EnumByRef")) ForwardJSP(m_classReq, m_classRes,
          * "/CreateVDPage.jsp"); else if (!sAction.equals("vdpvstab") && !sAction.equals("vddetailstab"))
          * ForwardJSP(req, res, "/ValidateVDPage.jsp");
          */} // end of doValidateVD
 
     /**
      * The doSetVDPage method gets the values from page the user filled out, Calls 'setAC.setVDValueFromPage' to set the
      * data from the page to the bean. Stores 'm_VD' bean in session. Forwards the page 'CreateVDPage.jsp' with
      * validation vector to display.
      *
      * @param sOrigin
      *            origin where it is called from
      *
      * @throws Exception
      */
     private void doSetVDPage(String sOrigin) throws Exception
     {
         try
         {
             HttpSession session = m_classReq.getSession();
             String sAction = (String) m_classReq.getParameter("pageAction");
             if (sAction == null)
                 sAction = "";
             // do below for versioning to check whether these two have changed
             VD_Bean vdBean = (VD_Bean) session.getAttribute("m_VD"); // new VD_Bean();
             m_setAC.setVDValueFromPage(m_classReq, m_classRes, vdBean);
             // check if pvs are used in the form when type is changed to non enumerated.
             if (!sAction.equals("Enum"))
             {
                 // get vdid from the bean
                 // VD_Bean vdBean = (VD_Bean)session.getAttribute("m_VD");
                 String sVDid = vdBean.getVD_VD_IDSEQ();
                 boolean isExist = false;
                 if (sOrigin.equals("Edit"))
                 {
                     // call function to check if relationship exists
                     SetACService setAC = new SetACService(this);
                     isExist = setAC.checkPVQCExists(m_classReq, m_classRes, sVDid, "");
                     if (isExist)
                     {
                         String sMsg = "Unable to change Value Domain type to Non-Enumerated "
                                         + "because one or more Permissible Values are being used in a Case Report Form. \\n"
                                         + "Please create a new version of this Value Domain to change the type to Non-Enumerated.";
                         DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, sMsg);
                         vdBean.setVD_TYPE_FLAG("E");
                         DataManager.setAttribute(session, "m_VD", vdBean);
                     }
                 }
                 // mark all the pvs as deleted to remove them while submitting.
                 if (!isExist)
                 {
                     Vector<PV_Bean> vVDPVs = vdBean.getVD_PV_List(); // (Vector)session.getAttribute("VDPVList");
                     if (vVDPVs != null)
                     {
                         // set each bean as deleted to handle later
                         Vector<PV_Bean> vRemVDPV = vdBean.getRemoved_VDPVList();
                         if (vRemVDPV == null)
                             vRemVDPV = new Vector<PV_Bean>();
                         for (int i = 0; i < vVDPVs.size(); i++)
                         {
                             PV_Bean pvBean = (PV_Bean) vVDPVs.elementAt(i);
                             vRemVDPV.addElement(pvBean);
                         }
                         vdBean.setRemoved_VDPVList(vRemVDPV);
                         vdBean.setVD_PV_List(new Vector<PV_Bean>());
                     }
                 }
             }
             else
             {
                 // remove meta parents since it is not needed for enum types
                 Vector<EVS_Bean> vParentCon = vdBean.getReferenceConceptList(); // (Vector)session.getAttribute("VDParentConcept");
                 if (vParentCon == null)
                     vParentCon = new Vector<EVS_Bean>();
                 for (int i = 0; i < vParentCon.size(); i++)
                 {
                     EVS_Bean ePar = (EVS_Bean) vParentCon.elementAt(i);
                     if (ePar == null)
                         ePar = new EVS_Bean();
                     String parDB = ePar.getEVS_DATABASE();
                     // System.out.println(i + " setvdpage " + parDB);
                     if (parDB != null && parDB.equals("NCI Metathesaurus"))
                     {
                         ePar.setCON_AC_SUBMIT_ACTION("DEL");
                         vParentCon.setElementAt(ePar, i);
                     }
                 }
                 vdBean.setReferenceConceptList(vParentCon);
                 DataManager.setAttribute(session, "m_VD", vdBean);
                 // get back pvs associated with this vd
                 VD_Bean oldVD = (VD_Bean) session.getAttribute("oldVDBean");
                 if (oldVD == null)
                     oldVD = new VD_Bean();
                 if (oldVD.getVD_TYPE_FLAG() != null && oldVD.getVD_TYPE_FLAG().equals("E"))
                 {
                     if (oldVD.getVD_VD_IDSEQ() != null && !oldVD.getVD_VD_IDSEQ().equals(""))
                     {
                        // String pvAct = "Search";
                         String sMenu = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
                        // if (sMenu.equals("NewVDTemplate"))
                           //  pvAct = "NewUsing";
                       //  Integer pvCount = new Integer(0);
                         vdBean.setVD_PV_List(oldVD.cloneVDPVVector(oldVD.getVD_PV_List()));
                         vdBean.setRemoved_VDPVList(new Vector<PV_Bean>());
                         GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, this);
                         if (sMenu.equals("Questions"))
                             serAC.getACQuestionValue(vdBean);
                     }
                 }
             }
             DataManager.setAttribute(session, "m_VD", vdBean);
         }
         catch (Exception e)
         {
             logger.error("Error - doSetVDPage " + e.toString(), e);
         }
     } // end of doValidateVD
 
     /**
      * makes the vd's system generated name
      *
      * @param vd
      *            current vd bean
      * @param vParent
      *            vector of seelected parents
      * @return modified vd bean
       */
     public AC_Bean getSystemName(AC_Bean ac, Vector<EVS_Bean> vParent)
     {
     	VD_Bean vd = (VD_Bean)ac;
         try
         {
             // make the system generated name
             String sysName = "";
             for (int i = vParent.size() - 1; i > -1; i--)
             {
                 EVS_Bean par = (EVS_Bean) vParent.elementAt(i);
                 String evsDB = par.getEVS_DATABASE();
                 String subAct = par.getCON_AC_SUBMIT_ACTION();
                 if (subAct != null && !subAct.equals("DEL") && evsDB != null && !evsDB.equals("Non_EVS"))
                 {
                     // add the concept id to sysname if less than 20 characters
                     if (sysName.equals("") || sysName.length() < 20)
                         sysName += par.getCONCEPT_IDENTIFIER() + ":";
                     else
                         break;
                 }
             }
             // append vd public id and version in the end
             if (vd.getVD_VD_ID() != null)
                 sysName += vd.getVD_VD_ID();
             String sver = vd.getVD_VERSION();
             if (sver != null && sver.indexOf(".") < 0)
                 sver += ".0";
             if (vd.getVD_VERSION() != null)
                 sysName += "v" + sver;
             // limit to 30 characters
             if (sysName.length() > 30)
                 sysName = sysName.substring(sysName.length() - 30);
             vd.setAC_SYS_PREF_NAME(sysName); // store it in vd bean
             // make system name preferrd name if sys was selected
             String selNameType = (String) m_classReq.getParameter("rNameConv");
             // get it from the vd bean if null
             if (selNameType == null)
             {
                 selNameType = vd.getVD_TYPE_NAME();
             }
             else
             {
                 // store the keyed in text in the user field for later use.
                 String sPrefName = (String) m_classReq.getParameter("txPreferredName");
                 if (selNameType != null && selNameType.equals("USER") && sPrefName != null)
                     vd.setAC_USER_PREF_NAME(sPrefName);
             }
             if (selNameType != null && selNameType.equals("SYS"))
                 vd.setVD_PREFERRED_NAME(sysName);
         }
         catch (Exception e)
         {
             this.logger.error("ERROR - getSystemName : " + e.toString(), e);
         }
         return vd;
     }
 
     /**
      * marks the parent and/or its pvs as deleted from the session.
      *
      * @param sPVAction
      * @param vdPage
      * @throws java.lang.Exception
      */
     private void doRemoveParent(String sPVAction, String vdPage) throws Exception
     {
         HttpSession session = m_classReq.getSession();
         VD_Bean m_VD = (VD_Bean) session.getAttribute("m_VD"); // new VD_Bean();
         Vector<EVS_Bean> vParentCon = m_VD.getReferenceConceptList(); // (Vector)session.getAttribute("VDParentConcept");
         if (vParentCon == null)
             vParentCon = new Vector<EVS_Bean>();
         // get the selected parent info from teh request
         String sParentCC = (String) m_classReq.getParameter("selectedParentConceptCode");
         String sParentName = (String) m_classReq.getParameter("selectedParentConceptName");
         String sParentDB = (String) m_classReq.getParameter("selectedParentConceptDB");
         // for non evs parent compare the long names instead
         if (sParentName != null && !sParentName.equals("") && sParentDB != null && sParentDB.equals("Non_EVS"))
             sParentCC = sParentName;
         if (sParentCC != null)
         {
             for (int i = 0; i < vParentCon.size(); i++)
             {
                 EVS_Bean eBean = (EVS_Bean) vParentCon.elementAt(i);
                 if (eBean == null)
                     eBean = new EVS_Bean();
                 String thisParent = eBean.getCONCEPT_IDENTIFIER();
                 if (thisParent == null)
                     thisParent = "";
                 String thisParentName = eBean.getLONG_NAME();
                 if (thisParentName == null)
                     thisParentName = "";
                 String thisParentDB = eBean.getEVS_DATABASE();
                 if (thisParentDB == null)
                     thisParentDB = "";
                 // for non evs parent compare the long names instead
                 if (sParentDB != null && sParentDB.equals("Non_EVS"))
                     thisParent = thisParentName;
                 // look for the matched parent from the vector to remove
                 if (sParentCC.equals(thisParent))
                 {
                     @SuppressWarnings("unused") String strHTML = "";
                     EVSMasterTree tree = new EVSMasterTree(m_classReq, thisParentDB, this);
                     strHTML = tree.refreshTree(thisParentName, "false");
                     strHTML = tree.refreshTree("parentTree" + thisParentName, "false");
                     if (sPVAction.equals("removePVandParent"))
                     {
                         Vector<PV_Bean> vVDPVList = m_VD.getVD_PV_List(); // (Vector)session.getAttribute("VDPVList");
                         if (vVDPVList == null)
                             vVDPVList = new Vector<PV_Bean>();
                         // loop through the vector of pvs to get matched parent
                         for (int j = 0; j < vVDPVList.size(); j++)
                         {
                             PV_Bean pvBean = (PV_Bean) vVDPVList.elementAt(j);
                             if (pvBean == null)
                                 pvBean = new PV_Bean();
                             EVS_Bean pvParent = (EVS_Bean) pvBean.getPARENT_CONCEPT();
                             if (pvParent == null)
                                 pvParent = new EVS_Bean();
                             String pvParCon = pvParent.getCONCEPT_IDENTIFIER();
                             // match the parent concept with the pv's parent concept
                             if (thisParent.equals(pvParCon))
                             {
                                 pvBean.setVP_SUBMIT_ACTION("DEL"); // mark the vp as deleted
                                // String pvID = pvBean.getPV_PV_IDSEQ();
                                 vVDPVList.setElementAt(pvBean, j);
                             }
                         }
                         m_VD.setVD_PV_List(vVDPVList);
                         // DataManager.setAttribute(session, "VDPVList", vVDPVList);
                     }
                     // mark the parent as delected and leave
                     eBean.setCON_AC_SUBMIT_ACTION("DEL");
                     vParentCon.setElementAt(eBean, i);
                     break;
                 }
             }
         }
         // DataManager.setAttribute(session, "VDParentConcept", vParentCon);
         m_VD.setReferenceConceptList(vParentCon);
         // make sure all other changes are stored back in vd
         m_setAC.setVDValueFromPage(m_classReq, m_classRes, m_VD);
         // make vd's system preferred name
         m_VD = (VD_Bean) this.getSystemName(m_VD, vParentCon);
         DataManager.setAttribute(session, "m_VD", m_VD);
         // make the selected parent in hte session empty
         DataManager.setAttribute(session, "SelectedParentName", "");
         DataManager.setAttribute(session, "SelectedParentCC", "");
         DataManager.setAttribute(session, "SelectedParentDB", "");
         DataManager.setAttribute(session, "SelectedParentMetaSource", "");
         // forward teh page according to vdPage
         if (vdPage.equals("editVD"))
             ForwardJSP(m_classReq, m_classRes, "/EditVDPage.jsp");
         else
             ForwardJSP(m_classReq, m_classRes, "/CreateVDPage.jsp");
     }
 
     /**
      * splits the vd rep term from cadsr into individual concepts
      *
      * @param sComp
      *            name of the searched component
      * @param m_Bean
      *            selected EVS bean
      * @param nameAction
      *            string naming action
      *
      */
     private void splitIntoConceptsVD(String sComp, EVS_Bean m_Bean,String nameAction)
     {
         try
         {
             HttpSession session = m_classReq.getSession();
           //  String sSelRow = "";
             VD_Bean m_VD = (VD_Bean) session.getAttribute("m_VD");
             if (m_VD == null)
                 m_VD = new VD_Bean();
             m_setAC.setVDValueFromPage(m_classReq, m_classRes, m_VD);
             Vector vRepTerm = (Vector) session.getAttribute("vRepTerm");
             if (vRepTerm == null)
                 vRepTerm = new Vector();
             String sCondr = m_Bean.getCONDR_IDSEQ();
             String sLongName = m_Bean.getLONG_NAME();
             String sIDSEQ = m_Bean.getIDSEQ();
             if (sComp.equals("RepTerm") || sComp.equals("RepQualifier"))
             {
                 m_VD.setVD_REP_TERM(sLongName);
                 m_VD.setVD_REP_IDSEQ(sIDSEQ);
             }
            // String sRepTerm = m_VD.getVD_REP_TERM();
             if (sCondr != null && !sCondr.equals(""))
             {
                 GetACService getAC = new GetACService(m_classReq, m_classRes, this);
                 Vector vCon = getAC.getAC_Concepts(sCondr, null, true);
                 if (vCon != null && vCon.size() > 0)
                 {
                     for (int j = 0; j < vCon.size(); j++)
                     {
                         EVS_Bean bean = new EVS_Bean();
                         bean = (EVS_Bean) vCon.elementAt(j);
                         if (bean != null)
                         {
                             if (j == 0) // Primary Concept
                                 m_VD = this.addRepConcepts(nameAction, m_VD, bean, "Primary");
                             else
                                 // Secondary Concepts
                                 m_VD = this.addRepConcepts(nameAction, m_VD, bean, "Qualifier");
                         }
                     }
                 }
             }
         }
         catch (Exception e)
         {
             this.logger.error("ERROR - splitintoConceptVD : " + e.toString(), e);
         }
     }
 
     /**
      * this method is used to create preferred name for VD names of all three types will be stored in the bean for later
      * use if type is changed, it populates name according to type selected.
      *
      * @param newBean
      *            new EVS bean to append the name to
      * @param nameAct
      *            string new name or append name
      * @param pageVD
      *            current vd bean
      * @return VD bean
      */
     public AC_Bean getACNames(EVS_Bean newBean, String nameAct, AC_Bean pageAC)
     {
         HttpSession session = m_classReq.getSession();
         VD_Bean pageVD = (VD_Bean)pageAC;
         if (pageVD == null)
         	pageVD = (VD_Bean) session.getAttribute("m_VD");
         // get vd object class and property names
         String sLongName = "";
         String sPrefName = "";
         String sAbbName = "";
         String sDef = "";
         // get the existing one if not restructuring the name but appending it
         if (newBean != null)
         {
             sLongName = pageVD.getVD_LONG_NAME();
             if (sLongName == null)
                 sLongName = "";
             sDef = pageVD.getVD_PREFERRED_DEFINITION();
             if (sDef == null)
                 sDef = "";
         }
         // get the typed text on to user name
         String selNameType = "";
         if (nameAct.equals("Search") || nameAct.equals("Remove"))
         {
             selNameType = (String) m_classReq.getParameter("rNameConv");
             sPrefName = (String) m_classReq.getParameter("txPreferredName");
             if (selNameType != null && selNameType.equals("USER") && sPrefName != null)
                 pageVD.setAC_USER_PREF_NAME(sPrefName);
         }
         // get the object class into the long name and abbr name
         String sObjClass = pageVD.getVD_OBJ_CLASS();
         if (sObjClass == null)
             sObjClass = "";
         if (!sObjClass.equals(""))
         {
             // rearrange it long name
             if (newBean == null)
             {
                 if (!sLongName.equals(""))
                     sLongName += " "; // add extra space if not empty
                 sLongName += sObjClass;
                 EVS_Bean mOC = (EVS_Bean) session.getAttribute("m_OC");
                 if (mOC != null)
                 {
                     if (!sDef.equals(""))
                         sDef += "_"; // add definition
                     sDef += mOC.getPREFERRED_DEFINITION();
                 }
             }
             if (!sAbbName.equals(""))
                 sAbbName += "_"; // add underscore if not empty
             if (sObjClass.length() > 3)
                 sAbbName += sObjClass.substring(0, 4); // truncate to 4 letters
             else
                 sAbbName = sObjClass;
         }
         // get the property into the long name and abbr name
         String sPropClass = pageVD.getVD_PROP_CLASS();
         if (sPropClass == null)
             sPropClass = "";
         if (!sPropClass.equals(""))
         {
             // rearrange it long name
             if (newBean == null)
             {
                 if (!sLongName.equals(""))
                     sLongName += " "; // add extra space if not empty
                 sLongName += sPropClass;
                 EVS_Bean mPC = (EVS_Bean) session.getAttribute("m_PC");
                 if (mPC != null)
                 {
                     if (!sDef.equals(""))
                         sDef += "_"; // add definition
                     sDef += mPC.getPREFERRED_DEFINITION();
                 }
             }
             if (!sAbbName.equals(""))
                 sAbbName += "_"; // add underscore if not empty
             if (sPropClass.length() > 3)
                 sAbbName += sPropClass.substring(0, 4); // truncate to 4 letters
             else
                 sAbbName += sPropClass;
         }
         Vector vRep = (Vector) session.getAttribute("vRepTerm");
         if (vRep == null)
             vRep = new Vector();
         // add the qualifiers first
         for (int i = 1; vRep.size() > i; i++)
         {
             EVS_Bean eCon = (EVS_Bean) vRep.elementAt(i);
             if (eCon == null)
                 eCon = new EVS_Bean();
             String conName = eCon.getLONG_NAME();
             if (conName == null)
                 conName = "";
             if (!conName.equals(""))
             {
                 // rearrange it long name and definition
                 if (newBean == null)
                 {
                     if (!sLongName.equals(""))
                         sLongName += " ";
                     sLongName += conName;
                     if (!sDef.equals(""))
                         sDef += "_"; // add definition
                     sDef += eCon.getPREFERRED_DEFINITION();
                 }
                 if (!sAbbName.equals(""))
                     sAbbName += "_";
                 if (conName.length() > 3)
                     sAbbName += conName.substring(0, 4); // truncate to four letters
                 else
                     sAbbName += conName;
             }
         }
         // add the primary
         if (vRep != null && vRep.size() > 0)
         {
             EVS_Bean eCon = (EVS_Bean) vRep.elementAt(0);
             if (eCon == null)
                 eCon = new EVS_Bean();
             String sPrimary = eCon.getLONG_NAME();
             if (sPrimary == null)
                 sPrimary = "";
             if (!sPrimary.equals(""))
             {
                 // rearrange it only long name and definition
                 if (newBean == null)
                 {
                     if (!sLongName.equals(""))
                         sLongName += " ";
                     sLongName += sPrimary;
                     if (!sDef.equals(""))
                         sDef += "_"; // add definition
                     sDef += eCon.getPREFERRED_DEFINITION();
                 }
                 if (!sAbbName.equals(""))
                     sAbbName += "_";
                 if (sPrimary.length() > 3)
                     sAbbName += sPrimary.substring(0, 4); // truncate to four letters
                 else
                     sAbbName += sPrimary;
             }
         }
         // truncate to 30 characters
         if (sAbbName != null && sAbbName.length() > 30)
             sAbbName = sAbbName.substring(0, 30);
         // add the abbr name to vd bean and page is selected
         pageVD.setAC_ABBR_PREF_NAME(sAbbName);
         // make abbr name name preferrd name if sys was selected
         if (selNameType != null && selNameType.equals("ABBR"))
             pageVD.setVD_PREFERRED_NAME(sAbbName);
         if (newBean != null) // appending to the existing;
         {
             String sSelectName = newBean.getLONG_NAME();
             if (!sLongName.equals(""))
                 sLongName += " ";
             sLongName += sSelectName;
             if (!sDef.equals(""))
                 sDef += "_"; // add definition
             sDef += newBean.getPREFERRED_DEFINITION();
         }
         // store the long names, definition, and usr name in vd bean if searched
         if (nameAct.equals("Search"))
         {
             pageVD.setVD_LONG_NAME(sLongName);
             pageVD.setVD_PREFERRED_DEFINITION(sDef);
             pageVD.setVDNAME_CHANGED(true);
         }
         return pageVD;
     }
 
     /**
     *
     * @param nameAction
     *            stirng name action
     *
     */
     private void doVDUseSelection(String nameAction)
    {
        try
        {
            HttpSession session = m_classReq.getSession();
            String sSelRow = "";
          //  InsACService insAC = new InsACService(req, res, this);
            VD_Bean m_VD = (VD_Bean) session.getAttribute("m_VD");
            if (m_VD == null)
                m_VD = new VD_Bean();
            m_setAC.setVDValueFromPage(m_classReq, m_classRes, m_VD);
            Vector<EVS_Bean> vRepTerm = (Vector) session.getAttribute("vRepTerm");
            if (vRepTerm == null)
                vRepTerm = new Vector<EVS_Bean>();
            Vector vAC = new Vector();
            ;
            EVS_Bean m_REP = new EVS_Bean();
            String sComp = (String) m_classReq.getParameter("sCompBlocks");
            // get rep term components
            if (sComp.equals("RepTerm") || sComp.equals("RepQualifier"))
            {
                sSelRow = (String) m_classReq.getParameter("selRepRow");
                // vAC = (Vector)session.getAttribute("vRepResult");
                vAC = (Vector) session.getAttribute("vACSearch");
                if (vAC == null)
                    vAC = new Vector();
                if (sSelRow != null && !sSelRow.equals(""))
                {
                    String sObjRow = sSelRow.substring(2);
                    Integer intObjRow = new Integer(sObjRow);
                    int intObjRow2 = intObjRow.intValue();
                    if (vAC.size() > intObjRow2 - 1)
                        m_REP = (EVS_Bean) vAC.elementAt(intObjRow2);
                    // get name value pari
                    String sNVP = (String) m_classReq.getParameter("nvpConcept");
                    if (sNVP != null && !sNVP.equals(""))
                    {
                        m_REP.setNVP_CONCEPT_VALUE(sNVP);
                        String sName = m_REP.getLONG_NAME();
                        m_REP.setLONG_NAME(sName + "::" + sNVP);
                        m_REP.setPREFERRED_DEFINITION(m_REP.getPREFERRED_DEFINITION() + "::" + sNVP);
                    }
                    //System.out.println(sNVP + sComp + m_REP.getLONG_NAME());
                }
                else
                {
                    storeStatusMsg("Unable to get the selected row from the Rep Term search results.");
                    return;
                }
                // send it back if unable to obtion the concept
                if (m_REP == null || m_REP.getLONG_NAME() == null)
                {
                    storeStatusMsg("Unable to obtain concept from the selected row of the " + sComp
                                    + " search results.\\n" + "Please try again.");
                    return;
                }
                // handle the primary search
                if (sComp.equals("RepTerm"))
                {
                    if (m_REP.getEVS_DATABASE().equals("caDSR"))
                    {
                        // split it if rep term, add concept class to the list if evs id exists
                        if (m_REP.getCONDR_IDSEQ() == null || m_REP.getCONDR_IDSEQ().equals(""))
                        {
                            if (m_REP.getCONCEPT_IDENTIFIER() == null || m_REP.getCONCEPT_IDENTIFIER().equals(""))
                            {
                                storeStatusMsg("This Rep Term is not associated to a concept, so the data is suspect. \\n"
                                                                + "Please choose another Rep Term.");
                            }
                            else
                                m_VD = this.addRepConcepts(nameAction, m_VD, m_REP, "Primary");
                        }
                        else
                            splitIntoConceptsVD(sComp, m_REP, nameAction);
                    }
                    else
                        m_VD = this.addRepConcepts(nameAction, m_VD, m_REP, "Primary");
                }
                else if (sComp.equals("RepQualifier"))
                {
                    // Do this to reserve zero position in vector for primary concept
                    if (vRepTerm.size() < 1)
                    {
                        EVS_Bean OCBean = new EVS_Bean();
                        vRepTerm.addElement(OCBean);
                        DataManager.setAttribute(session, "vRepTerm", vRepTerm);
                    }
                    m_VD = this.addRepConcepts(nameAction, m_VD, m_REP, "Qualifier");
                }
            }
            else
            {
                EVS_Bean eBean = this.getEVSSelRow(m_classReq);
                if (eBean != null && eBean.getLONG_NAME() != null)
                {
 /*                   if (sComp.equals("VDObjectClass"))
                    {
                        m_VD.setVD_OBJ_CLASS(eBean.getLONG_NAME());
                        DataManager.setAttribute(session, "m_OC", eBean);
                    }
                    else if (sComp.equals("VDPropertyClass"))
                    {
                        m_VD.setVD_PROP_CLASS(eBean.getLONG_NAME());
                        DataManager.setAttribute(session, "m_PC", eBean);
                    }
 */                   if (nameAction.equals("appendName"))
                        m_VD = (VD_Bean) this.getACNames(eBean, "Search", m_VD);
                }
            }
            // rebuild new name if not appending
            EVS_Bean nullEVS = null; 
            if (nameAction.equals("newName"))
                m_VD = (VD_Bean) this.getACNames(nullEVS, "Search", m_VD);
            else if (nameAction.equals("blockName"))
                m_VD = (VD_Bean) this.getACNames(nullEVS, "blockName", m_VD);
            DataManager.setAttribute(session, "m_VD", m_VD);
        }
        catch (Exception e)
        {
            this.logger.error("ERROR - doVDUseSelection : " + e.toString(), e);
        }
    } // end of doVDUseSelection
 
    /**
     * adds the selected concept to the vector of concepts for property
     *
     * @param nameAction
     *            String naming action
     * @param vdBean
     *            selected DEC_Bean
     * @param eBean
     *            selected EVS_Bean
     * @param repType
     *            String property type (primary or qualifier)
     * @return DEC_Bean
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private VD_Bean addRepConcepts(String nameAction, VD_Bean vdBean,
                    EVS_Bean eBean, String repType) throws Exception
    {
        HttpSession session = m_classReq.getSession();
        // add the concept bean to the OC vector and store it in the vector
        Vector<EVS_Bean> vRepTerm = (Vector) session.getAttribute("vRepTerm");
        if (vRepTerm == null)
            vRepTerm = new Vector<EVS_Bean>();
        // get the evs user bean
        EVS_UserBean eUser = (EVS_UserBean) this.sessionData.EvsUsrBean; // (EVS_UserBean)session.getAttribute(EVSSearch.EVS_USER_BEAN_ARG);
                                                                            // //("EvsUserBean");
        if (eUser == null)
            eUser = new EVS_UserBean();
        eBean.setCON_AC_SUBMIT_ACTION("INS");
        eBean.setCONTE_IDSEQ(vdBean.getVD_CONTE_IDSEQ());
        String eDB = eBean.getEVS_DATABASE();
        if (eDB != null && eBean.getEVS_ORIGIN() != null && eDB.equalsIgnoreCase("caDSR"))
        {
            eDB = eBean.getVocabAttr(eUser, eBean.getEVS_ORIGIN(), EVSSearch.VOCAB_NAME, EVSSearch.VOCAB_DBORIGIN); // "vocabName",
                                                                                                                    // "vocabDBOrigin");
            if (eDB.equals(EVSSearch.META_VALUE)) // "MetaValue"))
                eDB = eBean.getEVS_ORIGIN();
            eBean.setEVS_DATABASE(eDB); // eBean.getEVS_ORIGIN());
        }
        // System.out.println(eBean.getEVS_ORIGIN() + " before thes concept for REP " + eDB);
        EVSSearch evs = new EVSSearch(m_classReq, m_classRes, this);
        eBean = evs.getThesaurusConcept(eBean);
        // add to the vector and store it in the session, reset if primary and alredy existed, add otehrwise
        if (repType.equals("Primary") && vRepTerm.size() > 0)
            vRepTerm.setElementAt(eBean, 0);
        else
            vRepTerm.addElement(eBean);
        DataManager.setAttribute(session, "vRepTerm", vRepTerm);
        DataManager.setAttribute(session, "newRepTerm", "true");
        // add rep primary attributes to the vd bean
        if (repType.equals("Primary"))
        {
            vdBean.setVD_REP_NAME_PRIMARY(eBean.getLONG_NAME());
            vdBean.setVD_REP_CONCEPT_CODE(eBean.getCONCEPT_IDENTIFIER());
            vdBean.setVD_REP_EVS_CUI_ORIGEN(eBean.getEVS_DATABASE());
            vdBean.setVD_REP_IDSEQ(eBean.getIDSEQ());
            DataManager.setAttribute(session, "m_REP", eBean);
        }
        else
        {
            // add rep qualifiers to the vector
            Vector<String> vRepQualifierNames = vdBean.getVD_REP_QUALIFIER_NAMES();
            if (vRepQualifierNames == null)
                vRepQualifierNames = new Vector<String>();
            vRepQualifierNames.addElement(eBean.getLONG_NAME());
            Vector<String> vRepQualifierCodes = vdBean.getVD_REP_QUALIFIER_CODES();
            if (vRepQualifierCodes == null)
                vRepQualifierCodes = new Vector<String>();
            vRepQualifierCodes.addElement(eBean.getCONCEPT_IDENTIFIER());
            Vector<String> vRepQualifierDB = vdBean.getVD_REP_QUALIFIER_DB();
            if (vRepQualifierDB == null)
                vRepQualifierDB = new Vector<String>();
            vRepQualifierDB.addElement(eBean.getEVS_DATABASE());
            vdBean.setVD_REP_QUALIFIER_NAMES(vRepQualifierNames);
            vdBean.setVD_REP_QUALIFIER_CODES(vRepQualifierCodes);
            vdBean.setVD_REP_QUALIFIER_DB(vRepQualifierDB);
            // if(vRepQualifierNames.size()>0)
            // vdBean.setVD_REP_QUAL((String)vRepQualifierNames.elementAt(0));
            DataManager.setAttribute(session, "vRepQResult", null);
            DataManager.setAttribute(session, "m_REPQ", eBean);
        }
        // DataManager.setAttribute(session, "selRepQRow", sSelRow);
        // add to name if appending
        if (nameAction.equals("appendName"))
            vdBean = (VD_Bean) this.getACNames(eBean, "Search", vdBean);
        return vdBean;
    } // end addRepConcepts
 
    /**
     * The doValidateVD method gets the values from page the user filled out, validates the input, then forwards results
     * to the Validate page Called from 'doCreateVDActions', 'doSubmitVD' method. Calls 'setAC.setVDValueFromPage' to
     * set the data from the page to the bean. Calls 'setAC.setValidatePageValuesVD' to validate the data. Stores 'm_VD'
     * bean in session. Forwards the page 'ValidateVDPage.jsp' with validation vector to display.
     *
     * @throws Exception
     */
    private void doValidateVDBlockEdit() throws Exception
    {
        HttpSession session = m_classReq.getSession();
        VD_Bean m_VD = (VD_Bean) session.getAttribute("m_VD"); // new VD_Bean();
        DataManager.setAttribute(session, "VDPageAction", "validate"); // store the page action in attribute
        m_setAC.setVDValueFromPage(m_classReq, m_classRes, m_VD);
        DataManager.setAttribute(session, "m_VD", m_VD);
        m_setAC.setValidateBlockEdit(m_classReq, m_classRes, "ValueDomain");
        DataManager.setAttribute(session, "VDEditAction", "VDBlockEdit");
        ForwardJSP(m_classReq, m_classRes, "/ValidateVDPage.jsp");
    } // end of doValidateVD
 
    /**
     * The doInsertVD method to insert or update record in the database. Called from 'service' method where reqType is
     * 'validateVDFromForm'. Retrieves the session bean m_VD. if the action is reEditVD forwards the page back to Edit
     * or create pages.
     *
     * Otherwise, calls 'doUpdateVDAction' for editing the vd. calls 'doInsertVDfromDEAction' for creating the vd from
     * DE page. calls 'doInsertVDfromMenuAction' for creating the vd from menu .
     *
     * @throws Exception
     */
    private void doInsertVD() throws Exception
    {
        HttpSession session = m_classReq.getSession();
        // make sure that status message is empty
        DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, "");
        Vector vStat = new Vector();
        DataManager.setAttribute(session, "vStatMsg", vStat);
        String sVDAction = (String) session.getAttribute("VDAction");
        if (sVDAction == null)
            sVDAction = "";
        String sVDEditAction = (String) session.getAttribute("VDEditAction");
        if (sVDEditAction == null)
            sVDEditAction = "";
        String sAction = (String) m_classReq.getParameter("ValidateVDPageAction");
       // String sMenuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
       // String sButtonPressed = (String) session.getAttribute("LastMenuButtonPressed");
        String sOriginAction = (String) session.getAttribute("originAction");
        if (sAction == null)
            sAction = "submitting"; // for direct submit without validating
       // String spageAction = (String) m_classReq.getParameter("pageAction");
        if (sAction != null)
        { // goes back to create/edit pages from validation page
            if (sAction.equals("reEditVD"))
            {
                String vdfocus = (String) session.getAttribute("TabFocus");
                if (vdfocus != null && vdfocus.equals("PV"))
                    ForwardJSP(m_classReq, m_classRes, "/PermissibleValue.jsp");
                else
                {
                    if (sVDAction.equals("EditVD") || sVDAction.equals("BlockEdit"))
                        ForwardJSP(m_classReq, m_classRes, "/EditVDPage.jsp");
                    else
                        ForwardJSP(m_classReq, m_classRes, "/CreateVDPage.jsp");
                }
            }
            else
            {
                // edit the existing vd
                if (sVDAction.equals("NewVD") && sOriginAction.equals("NewVDFromMenu"))
                    doInsertVDfromMenuAction();
                else if (sVDAction.equals("EditVD") && !sOriginAction.equals("BlockEditVD"))
                    doUpdateVDAction();
                else if (sVDEditAction.equals("VDBlockEdit"))
                    doUpdateVDActionBE();
                // if create new vd from create/edit DE page.
                else if (sOriginAction.equals("CreateNewVDfromCreateDE")
                                || sOriginAction.equals("CreateNewVDfromEditDE"))
                    doInsertVDfromDEAction(sOriginAction);
                // from the menu AND template/ version
                else
                {
                    doInsertVDfromMenuAction();
                }
            }
        }
    } // end of doInsertVD
 
    /**
     * update record in the database and display the result. Called from 'doInsertVD' method when the aciton is editing.
     * Retrieves the session bean m_VD. calls 'insAC.setVD' to update the database. updates the DEbean and sends back to
     * EditDE page if origin is form DEpage otherwise calls 'serAC.refreshData' to get the refreshed search result
     * forwards the page back to search page with refreshed list after updating.
     *
     * If ret is not null stores the statusMessage as error message in session and forwards the page back to
     * 'EditVDPage.jsp' for Edit.
     *
     * @throws Exception
     */
    private void doUpdateVDAction() throws Exception
    {
        HttpSession session = m_classReq.getSession();
        VD_Bean VDBean = (VD_Bean) session.getAttribute("m_VD");
        VD_Bean oldVDBean = (VD_Bean) session.getAttribute("oldVDBean");
      //  String sMenu = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
        InsACService insAC = new InsACService(m_classReq, m_classRes, this);
        doInsertVDBlocks(null);
        // udpate the status message with DE name and ID
        storeStatusMsg("Value Domain Name : " + VDBean.getVD_LONG_NAME());
        storeStatusMsg("Public ID : " + VDBean.getVD_VD_ID());
        // call stored procedure to update attributes
        String ret = insAC.setVD("UPD", VDBean, "Edit", oldVDBean);
        // forward to search page with refreshed list after successful update
        if ((ret == null) || ret.equals(""))
        {
            this.clearCreateSessionAttributes(m_classReq, m_classRes); // clear some session attributes
            String sOriginAction = (String) session.getAttribute("originAction");
            GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, this);
            // forward page back to EditDE
            if (sOriginAction.equals("editVDfromDE") || sOriginAction.equals("EditDE"))
            {
                DE_Bean DEBean = (DE_Bean) session.getAttribute("m_DE");
                if (DEBean != null)
                {
                    DEBean.setDE_VD_IDSEQ(VDBean.getVD_VD_IDSEQ());
                    DEBean.setDE_VD_PREFERRED_NAME(VDBean.getVD_PREFERRED_NAME());
                    DEBean.setDE_VD_NAME(VDBean.getVD_LONG_NAME());
                    // reset the attributes
                    DataManager.setAttribute(session, "originAction", "");
                    // add DEC Bean into DE BEan
                    DEBean.setDE_VD_Bean(VDBean);
                    DataManager.setAttribute(session, "m_DE", DEBean);
                    CurationServlet deServ = (DataElementServlet) getACServlet("DataElement");               
                    DEBean = (DE_Bean) deServ.getACNames("new", "editVD", DEBean);
                }
                ForwardJSP(m_classReq, m_classRes, "/EditDEPage.jsp");
            }
            // go to search page with refreshed list
            else
            {
                VDBean.setVD_ALIAS_NAME(VDBean.getVD_PREFERRED_NAME());
                // VDBean.setVD_TYPE_NAME("PRIMARY");
                DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, "editVD");
                String oldID = VDBean.getVD_VD_IDSEQ();
                serAC.refreshData(m_classReq, m_classRes, null, null, VDBean, null, "Edit", oldID);
                ForwardJSP(m_classReq, m_classRes, "/SearchResultsPage.jsp");
            }
        }
        // goes back to edit page if error occurs
        else
        {
            DataManager.setAttribute(session, "VDPageAction", "nothing");
            ForwardJSP(m_classReq, m_classRes, "/EditVDPage.jsp");
        }
    }
 
    /**
     * update record in the database and display the result. Called from 'doInsertVD' method when the aciton is editing.
     * Retrieves the session bean m_VD. calls 'insAC.setVD' to update the database. updates the DEbean and sends back to
     * EditDE page if origin is form DEpage otherwise calls 'serAC.refreshData' to get the refreshed search result
     * forwards the page back to search page with refreshed list after updating.
     *
     * If ret is not null stores the statusMessage as error message in session and forwards the page back to
     * 'EditVDPage.jsp' for Edit.
     *
      * @throws Exception
     */
    private void doUpdateVDActionBE() throws Exception
    {
        HttpSession session = m_classReq.getSession();
        VD_Bean VDBean = (VD_Bean) session.getAttribute("m_VD"); // validated edited m_VD
        boolean isRefreshed = false;
        String ret = ":";
        InsACService insAC = new InsACService(m_classReq, m_classRes, this);
        GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, this);
        GetACService getAC = new GetACService(m_classReq, m_classRes, this);
       // Vector vStatMsg = new Vector();
        String sNewRep = (String) session.getAttribute("newRepTerm");
        if (sNewRep == null)
            sNewRep = "";
        //System.out.println(" new rep " + sNewRep);
        Vector vBERows = (Vector) session.getAttribute("vBEResult");
        int vBESize = vBERows.size();
        Integer vBESize2 = new Integer(vBESize);
        m_classReq.setAttribute("vBESize", vBESize2);
        String sRep_IDSEQ = "";
        if (vBERows.size() > 0)
        {
            // Be sure the buffer is loaded when doing versioning.
            String newVersion = VDBean.getVD_VERSION();
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
                // String sVD_ID = ""; //out
                VD_Bean VDBeanSR = new VD_Bean();
                VDBeanSR = (VD_Bean) vBERows.elementAt(i);
                VD_Bean oldVDBean = new VD_Bean();
                oldVDBean = oldVDBean.cloneVD_Bean(VDBeanSR);
            //    String oldName = (String) VDBeanSR.getVD_PREFERRED_NAME();
                // updates the data from the page into the sr bean
                InsertEditsIntoVDBeanSR(VDBeanSR, VDBean);
                // create newly selected rep term
                if (i == 0 && sNewRep.equals("true"))
                {
                    doInsertVDBlocks(VDBeanSR); // create it
                    sRep_IDSEQ = VDBeanSR.getVD_REP_IDSEQ(); // get rep idseq
                    if (sRep_IDSEQ == null)
                        sRep_IDSEQ = "";
                    VDBean.setVD_REP_IDSEQ(sRep_IDSEQ); // add page vd bean
                    String sRep_Condr = VDBeanSR.getVD_REP_CONDR_IDSEQ(); // get rep condr
                    if (sRep_Condr == null)
                        sRep_Condr = "";
                    VDBean.setVD_REP_CONDR_IDSEQ(sRep_Condr); // add to page vd bean
                    // VDBean.setVD_REP_QUAL("");
                }
                // DataManager.setAttribute(session, "m_VD", VDBeanSR);
                String oldID = oldVDBean.getVD_VD_IDSEQ();
                // udpate the status message with DE name and ID
                storeStatusMsg("Value Domain Name : " + VDBeanSR.getVD_LONG_NAME());
                storeStatusMsg("Public ID : " + VDBeanSR.getVD_VD_ID());
                // insert the version
                if (newVers) // block version
                {
                    // creates new version first and updates all other attributes
                    String strValid = m_setAC.checkUniqueInContext("Version", "VD", null, null, VDBeanSR, getAC,
                                    "version");
                    if (strValid != null && !strValid.equals(""))
                        ret = "unique constraint";
                    else
                        ret = insAC.setAC_VERSION(null, null, VDBeanSR, "ValueDomain");
                    if (ret == null || ret.equals(""))
                    {
                        // PVServlet pvser = new PVServlet(req, res, this);
                        // pvser.searchVersionPV(VDBean, 0, "", "");
                        // get the right system name for new version
                        String prefName = VDBeanSR.getVD_PREFERRED_NAME();
                        String vdID = VDBeanSR.getVD_VD_ID();
                        String newVer = "v" + VDBeanSR.getVD_VERSION();
                        String oldVer = "v" + oldVDBean.getVD_VERSION();
                        // replace teh version number if system generated name
                        if (prefName.indexOf(vdID) > 0)
                        {
                            prefName = prefName.replaceFirst(oldVer, newVer);
                            VDBean.setVD_PREFERRED_NAME(prefName);
                        }
                        // keep the value and value count stored
                        String pvValue = VDBeanSR.getVD_Permissible_Value();
                        Integer pvCount = VDBeanSR.getVD_Permissible_Value_Count();
                        ret = insAC.setVD("UPD", VDBeanSR, "Version", oldVDBean);
                        if (ret == null || ret.equals(""))
                        {
                            VDBeanSR.setVD_Permissible_Value(pvValue);
                            VDBeanSR.setVD_Permissible_Value_Count(pvCount);
                            serAC.refreshData(m_classReq, m_classRes, null, null, VDBeanSR, null, "Version", oldID);
                            isRefreshed = true;
                            // reset the appened attributes to remove all the checking of the row
                            Vector vCheck = new Vector();
                            DataManager.setAttribute(session, "CheckList", vCheck);
                            DataManager.setAttribute(session, "AppendAction", "Not Appended");
                            // resetEVSBeans(req, res);
                        }
                    }
                    // alerady exists
                    else if (ret.indexOf("unique constraint") >= 0)
                        storeStatusMsg("\\t New version " + VDBeanSR.getVD_VERSION()
                                        + " already exists in the data base.\\n");
                    // some other problem
                    else
                        storeStatusMsg("\\t " + ret + " : Unable to create new version "
                                        + VDBeanSR.getVD_VERSION() + ".\\n");
                }
                else
                // block edit
                {
                    ret = insAC.setVD("UPD", VDBeanSR, "Edit", oldVDBean);
                    // forward to search page with refreshed list after successful update
                    if ((ret == null) || ret.equals(""))
                    {
                        serAC.refreshData(m_classReq, m_classRes, null, null, VDBeanSR, null, "Edit", oldID);
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
            serAC.getVDResult(m_classReq, m_classRes, vResult, "");
            DataManager.setAttribute(session, "results", vResult); // store the final result in the session
            DataManager.setAttribute(session, "VDPageAction", "nothing");
        }
        ForwardJSP(m_classReq, m_classRes, "/SearchResultsPage.jsp");
    }
 
    /**
     * updates bean the selected VD from the changed values of block edit.
     *
     * @param VDBeanSR
     *            selected vd bean from search result
     * @param vd
     *            VD_Bean of the changed values.
     *
     * @throws Exception
     */
    private void InsertEditsIntoVDBeanSR(VD_Bean VDBeanSR, VD_Bean vd) throws Exception
    {
        // get all attributes of VDBean, if attribute != "" then set that attribute of VDBeanSR
        String sDefinition = vd.getVD_PREFERRED_DEFINITION();
        if (sDefinition == null)
            sDefinition = "";
        if (!sDefinition.equals(""))
            VDBeanSR.setVD_PREFERRED_DEFINITION(sDefinition);
        String sCD_ID = vd.getVD_CD_IDSEQ();
        if (sCD_ID == null)
            sCD_ID = "";
        if (!sCD_ID.equals("") && !sCD_ID.equals(null))
            VDBeanSR.setVD_CD_IDSEQ(sCD_ID);
        String sCDName = vd.getVD_CD_NAME();
        if (sCDName == null)
            sCDName = "";
        if (!sCDName.equals("") && !sCDName.equals(null))
            VDBeanSR.setVD_CD_NAME(sCDName);
        String sAslName = vd.getVD_ASL_NAME();
        if (sAslName == null)
            sAslName = "";
        if (!sAslName.equals(""))
            VDBeanSR.setVD_ASL_NAME(sAslName);
        String sDtlName = vd.getVD_DATA_TYPE();
        if (sDtlName == null)
            sDtlName = "";
        if (!sDtlName.equals(""))
            VDBeanSR.setVD_DATA_TYPE(sDtlName);
        String sMaxLength = vd.getVD_MAX_LENGTH_NUM();
        if (sMaxLength == null)
            sMaxLength = "";
        if (!sMaxLength.equals(""))
            VDBeanSR.setVD_MAX_LENGTH_NUM(sMaxLength);
        String sFormlName = vd.getVD_FORML_NAME(); // UOM Format
        if (sFormlName == null)
            sFormlName = "";
        if (!sFormlName.equals(""))
            VDBeanSR.setVD_FORML_NAME(sFormlName);
        String sUomlName = vd.getVD_UOML_NAME();
        if (sUomlName == null)
            sUomlName = "";
        if (!sUomlName.equals(""))
            VDBeanSR.setVD_UOML_NAME(sUomlName);
        String sLowValue = vd.getVD_LOW_VALUE_NUM();
        if (sLowValue == null)
            sLowValue = "";
        if (!sLowValue.equals(""))
            VDBeanSR.setVD_LOW_VALUE_NUM(sLowValue);
        String sHighValue = vd.getVD_HIGH_VALUE_NUM();
        if (sHighValue == null)
            sHighValue = "";
        if (!sHighValue.equals(""))
            VDBeanSR.setVD_HIGH_VALUE_NUM(sHighValue);
        String sMinLength = vd.getVD_MIN_LENGTH_NUM();
        if (sMinLength == null)
            sMinLength = "";
        if (!sMinLength.equals(""))
            VDBeanSR.setVD_MIN_LENGTH_NUM(sMinLength);
        String sDecimalPlace = vd.getVD_DECIMAL_PLACE();
        if (sDecimalPlace == null)
            sDecimalPlace = "";
        if (!sDecimalPlace.equals(""))
            VDBeanSR.setVD_DECIMAL_PLACE(sDecimalPlace);
        String sBeginDate = vd.getVD_BEGIN_DATE();
        if (sBeginDate == null)
            sBeginDate = "";
        if (!sBeginDate.equals(""))
            VDBeanSR.setVD_BEGIN_DATE(sBeginDate);
        String sEndDate = vd.getVD_END_DATE();
        if (sEndDate == null)
            sEndDate = "";
        if (!sEndDate.equals(""))
            VDBeanSR.setVD_END_DATE(sEndDate);
        String sSource = vd.getVD_SOURCE();
        if (sSource == null)
            sSource = "";
        if (!sSource.equals(""))
            VDBeanSR.setVD_SOURCE(sSource);
        String changeNote = vd.getVD_CHANGE_NOTE();
        if (changeNote == null)
            changeNote = "";
        if (!changeNote.equals(""))
            VDBeanSR.setVD_CHANGE_NOTE(changeNote);
        // get cs-csi from the page into the DECBean for block edit
        Vector vAC_CS = vd.getAC_AC_CSI_VECTOR();
        if (vAC_CS != null)
            VDBeanSR.setAC_AC_CSI_VECTOR(vAC_CS);
        //get the Ref docs from the page into the DEBean for block edit
        Vector<REF_DOC_Bean> vAC_REF_DOCS = vd.getAC_REF_DOCS();
        if(vAC_REF_DOCS!=null){
        	Vector<REF_DOC_Bean> temp_REF_DOCS = new Vector<REF_DOC_Bean>();
        for(REF_DOC_Bean refBean:vAC_REF_DOCS )
        {
        	if(refBean.getAC_IDSEQ() == VDBeanSR.getVD_VD_IDSEQ())
        	{
        		temp_REF_DOCS.add(refBean);
        	}
        }
        VDBeanSR.setAC_REF_DOCS(temp_REF_DOCS);
        }
        String sRepTerm = vd.getVD_REP_TERM();
        if (sRepTerm == null)
            sRepTerm = "";
        if (!sRepTerm.equals(""))
            VDBeanSR.setVD_REP_TERM(sRepTerm);
        String sRepCondr = vd.getVD_REP_CONDR_IDSEQ();
        if (sRepCondr == null)
            sRepCondr = "";
        if (!sRepCondr.equals(""))
            VDBeanSR.setVD_REP_CONDR_IDSEQ(sRepCondr);
        String sREP_IDSEQ = vd.getVD_REP_IDSEQ();
        if (sREP_IDSEQ != null && !sREP_IDSEQ.equals(""))
            VDBeanSR.setVD_REP_IDSEQ(sREP_IDSEQ);
        /*
         * String sRepQual = vd.getVD_REP_QUAL(); if (sRepQual == null) sRepQual = ""; if (!sRepQual.equals(""))
         * VDBeanSR.setVD_REP_QUAL(sRepQual);
         */
        String version = vd.getVD_VERSION();
        String lastVersion = (String) VDBeanSR.getVD_VERSION();
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
            VDBeanSR.setVD_VERSION(sNewVersion);
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
            VDBeanSR.setVD_VERSION(sNewVersion);
        }
    }
 
    /**
     * creates new record in the database and display the result. Called from 'doInsertVD' method when the aciton is
     * create new VD from DEPage. Retrieves the session bean m_VD. calls 'insAC.setVD' to update the database. forwards
     * the page back to create DE page after successful insert.
     *
     * If ret is not null stores the statusMessage as error message in session and forwards the page back to
     * 'createVDPage.jsp' for Edit.
     *
     * @param sOrigin
     *            string value from where vd creation action was originated.
     *
     * @throws Exception
     */
    private void doInsertVDfromDEAction(String sOrigin)
                    throws Exception
    {
        HttpSession session = m_classReq.getSession();
        VD_Bean VDBean = (VD_Bean) session.getAttribute("m_VD");
        InsACService insAC = new InsACService(m_classReq, m_classRes, this);
     //   GetACSearch serAC = new GetACSearch(req, res, this);
     //   String sMenu = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
        // insert the building blocks attriubtes before inserting vd
        doInsertVDBlocks(null);
        String ret = insAC.setVD("INS", VDBean, "New", null);
        // updates the de bean with new vd data after successful insert and forwards to create page
        if ((ret == null) || ret.equals(""))
        {
            DE_Bean DEBean = (DE_Bean) session.getAttribute("m_DE");
            DEBean.setDE_VD_NAME(VDBean.getVD_LONG_NAME());
            DEBean.setDE_VD_IDSEQ(VDBean.getVD_VD_IDSEQ());
            // add DEC Bean into DE BEan
            DEBean.setDE_VD_Bean(VDBean);
            DataManager.setAttribute(session, "m_DE", DEBean);
            CurationServlet deServ = (DataElementServlet) getACServlet("DataElement");               
            DEBean = (DE_Bean) deServ.getACNames("new", "newVD", DEBean);  
            this.clearCreateSessionAttributes(m_classReq, m_classRes); // clear some session attributes
            if (sOrigin != null && sOrigin.equals("CreateNewVDfromEditDE"))
                ForwardJSP(m_classReq, m_classRes, "/EditDEPage.jsp");
            else
                ForwardJSP(m_classReq, m_classRes, "/CreateDEPage.jsp");
        }
        // goes back to create vd page if error
        else
        {
            DataManager.setAttribute(session, "VDPageAction", "validate");
            ForwardJSP(m_classReq, m_classRes, "/CreateVDPage.jsp"); // send it back to vd page
        }
    }
 
    /**
     * to create object class, property, rep term and qualifier value from EVS into cadsr. Retrieves the session bean
     * m_VD. calls 'insAC.setDECQualifier' to insert the database.
     *
      * @param VDBeanSR
     *            dec attribute bean.
     *
     * @throws Exception
     */
    private void doInsertVDBlocks(VD_Bean VDBeanSR) throws Exception
    {
        HttpSession session = m_classReq.getSession();
        if (VDBeanSR == null)
            VDBeanSR = (VD_Bean) session.getAttribute("m_VD");
        String sRemoveRepBlock = (String) session.getAttribute("RemoveRepBlock");
        if (sRemoveRepBlock == null)
            sRemoveRepBlock = "";
        EVS_Bean REPBean = (EVS_Bean) session.getAttribute("m_REP");
        if (REPBean == null)
            REPBean = new EVS_Bean();
        EVS_Bean REPQBean = (EVS_Bean) session.getAttribute("m_REPQ");
        if (REPQBean == null)
            REPQBean = new EVS_Bean();
        String sNewRep = (String) session.getAttribute("newRepTerm");
        if (sNewRep == null)
            sNewRep = "";
        String sREP_IDSEQ = "";
        @SuppressWarnings("unused") String retObj = "";
     //   String retProp = "";
     //   String retRep = "";
     //  String retObjQual = "";
     //   String retPropQual = "";
     //   String retRepQual = "";
        InsACService insAC = new InsACService(m_classReq, m_classRes, this);
        /*
         * if (sNewRep.equals("true")) retRepQual = insAC.setRepresentation("INS", sREP_IDSEQ, VDBeanSR, REPQBean, req);
         * else if(sRemoveRepBlock.equals("true"))
         */
        String sRep = VDBeanSR.getVD_REP_TERM();
        if (sRep != null && !sRep.equals(""))
            retObj = insAC.setRepresentation("INS", sREP_IDSEQ, VDBeanSR, REPBean, m_classReq);
        // create new version if not released
        sREP_IDSEQ = VDBeanSR.getVD_REP_IDSEQ();
        if (sREP_IDSEQ != null && !sREP_IDSEQ.equals(""))
        {
            // CALL to create new version if not released
            if (VDBeanSR.getVD_REP_ASL_NAME() != null && !VDBeanSR.getVD_REP_ASL_NAME().equals("RELEASED"))
            {
                sREP_IDSEQ = insAC.setOC_PROP_REP_VERSION(sREP_IDSEQ, "RepTerm");
                if (sREP_IDSEQ != null && !sREP_IDSEQ.equals(""))
                    VDBeanSR.setVD_REP_IDSEQ(sREP_IDSEQ);
            }
        }
        else
        {
            if (VDBeanSR.getVD_REP_CONDR_IDSEQ() != null && !VDBeanSR.getVD_REP_CONDR_IDSEQ().equals(""))
                VDBeanSR.setVD_REP_CONDR_IDSEQ("");
        }
        DataManager.setAttribute(session, "newRepTerm", "");
    }
 
    /**
     * creates new record in the database and display the result. Called from 'doInsertVD' method when the aciton is
     * create new VD from Menu. Retrieves the session bean m_VD. calls 'insAC.setVD' to update the database. calls
     * 'serAC.refreshData' to get the refreshed search result for template/version forwards the page back to create VD
     * page if new VD or back to search page if template or version after successful insert.
     *
     * If ret is not null stores the statusMessage as error message in session and forwards the page back to
     * 'createVDPage.jsp' for Edit.
     *
     * @throws Exception
     */
    private void doInsertVDfromMenuAction() throws Exception
    {
        HttpSession session = m_classReq.getSession();
        VD_Bean VDBean = (VD_Bean) session.getAttribute("m_VD");
        InsACService insAC = new InsACService(m_classReq, m_classRes, this);
        GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, this);
        String sMenuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
        VD_Bean oldVDBean = (VD_Bean) session.getAttribute("oldVDBean");
        if (oldVDBean == null)
            oldVDBean = new VD_Bean();
        String ret = "";
        boolean isUpdateSuccess = true;
        doInsertVDBlocks(null);
        if (sMenuAction.equals("NewVDVersion"))
        {
            // udpate the status message with DE name and ID
            storeStatusMsg("Value Domain Name : " + VDBean.getVD_LONG_NAME());
            storeStatusMsg("Public ID : " + VDBean.getVD_VD_ID());
            // creates new version first
            ret = insAC.setAC_VERSION(null, null, VDBean, "ValueDomain");
            if (ret == null || ret.equals(""))
            {
                // get pvs related to this new VD, it was created in VD_Version
                // TODO serAC.doPVACSearch(VDBean.getVD_VD_IDSEQ(), VDBean.getVD_LONG_NAME(), "Version");
                PVServlet pvser = new PVServlet(m_classReq, m_classRes, this);
                pvser.searchVersionPV(VDBean, 1, "", "");
                // update non evs changes
                Vector<EVS_Bean> vParent = VDBean.getReferenceConceptList(); // (Vector)session.getAttribute("VDParentConcept");
                if (vParent != null && vParent.size() > 0)
                    vParent = serAC.getNonEVSParent(vParent, VDBean, "versionSubmit");
                // get the right system name for new version; cannot use teh api because parent concept is not updated
                // yet
                String prefName = VDBean.getVD_PREFERRED_NAME();
                if (prefName == null || prefName.equalsIgnoreCase("(Generated by the System)"))
                {
                    VDBean = (VD_Bean) this.getSystemName(VDBean, vParent);
                    VDBean.setVD_PREFERRED_NAME(VDBean.getAC_SYS_PREF_NAME());
                }
                // and updates all other attributes
                ret = insAC.setVD("UPD", VDBean, "Version", oldVDBean);
                // resetEVSBeans(req, res);
                if (ret != null && !ret.equals(""))
                {
                    // add newly created row to searchresults and send it to edit page for update
                    isUpdateSuccess = false;
                    String oldID = oldVDBean.getVD_VD_IDSEQ();
                    String newID = VDBean.getVD_VD_IDSEQ();
                    String newVersion = VDBean.getVD_VERSION();
                    VDBean = VDBean.cloneVD_Bean(oldVDBean);
                    VDBean.setVD_VD_IDSEQ(newID);
                    VDBean.setVD_VERSION(newVersion);
                    VDBean.setVD_ASL_NAME("DRAFT MOD");
                    // refresh the result list by inserting newly created VD
                    serAC.refreshData(m_classReq, m_classRes, null, null, VDBean, null, "Version", oldID);
                }
            }
            else
                storeStatusMsg("\\t " + ret + " - Unable to create new version successfully.");
        }
        else
        {
            // creates new one
            ret = insAC.setVD("INS", VDBean, "New", oldVDBean); // create new one
        }
        if ((ret == null) || ret.equals(""))
        {
            this.clearCreateSessionAttributes(m_classReq, m_classRes); // clear some session attributes
            DataManager.setAttribute(session, "VDPageAction", "nothing");
            DataManager.setAttribute(session, "originAction", "");
            // forwards to search page with refreshed list if template or version
            if ((sMenuAction.equals("NewVDTemplate")) || (sMenuAction.equals("NewVDVersion")))
            {
                DataManager.setAttribute(session, "searchAC", "ValueDomain");
                DataManager.setAttribute(session, "originAction", "NewVDTemplate");
                VDBean.setVD_ALIAS_NAME(VDBean.getVD_PREFERRED_NAME());
                // VDBean.setVD_TYPE_NAME("PRIMARY");
                String oldID = oldVDBean.getVD_VD_IDSEQ();
                if (sMenuAction.equals("NewVDTemplate"))
                    serAC.refreshData(m_classReq, m_classRes, null, null, VDBean, null, "Template", oldID);
                else if (sMenuAction.equals("NewVDVersion"))
                    serAC.refreshData(m_classReq, m_classRes, null, null, VDBean, null, "Version", oldID);
                ForwardJSP(m_classReq, m_classRes, "/SearchResultsPage.jsp");
            }
            // forward to create vd page with empty data if new one
            else
            {
                doOpenCreateNewPages();   
            }
        }
        // goes back to create/edit vd page if error
        else
        {
            DataManager.setAttribute(session, "VDPageAction", "validate");
            // forward to create or edit pages
            if (isUpdateSuccess == false)
            {
                // insert the created NUE in the results.
                String oldID = oldVDBean.getVD_VD_IDSEQ();
                if (sMenuAction.equals("NewVDTemplate"))
                    serAC.refreshData(m_classReq, m_classRes, null, null, VDBean, null, "Template", oldID);
                ForwardJSP(m_classReq, m_classRes, "/SearchResultsPage.jsp");
            }
            else
                ForwardJSP(m_classReq, m_classRes, "/CreateVDPage.jsp");
        }
    }
 
    /**
     * The doOpenCreateVDPage method gets the session, gets some values from the createDE page and stores in bean m_DE,
     * sets some session attributes, then forwards to CreateVD page
     *
     * @throws Exception
     */
    public void doOpenCreateVDPage() throws Exception
    {
        HttpSession session = m_classReq.getSession();
        DE_Bean m_DE = (DE_Bean) session.getAttribute("m_DE");
        if (m_DE == null)
            m_DE = new DE_Bean();
        m_setAC.setDEValueFromPage(m_classReq, m_classRes, m_DE); // store VD bean
        DataManager.setAttribute(session, "m_DE", m_DE);
        // clear some session attributes
        this.clearCreateSessionAttributes(m_classReq, m_classRes);
        // reset the vd attributes
        VD_Bean m_VD = new VD_Bean();
        m_VD.setVD_ASL_NAME("DRAFT NEW");
        m_VD.setAC_PREF_NAME_TYPE("SYS");
        // call the method to get the QuestValues if exists
        String sMenuAction = (String) session.getAttribute(Session_Data.SESSION_MENU_ACTION);
        if (sMenuAction.equals("Questions"))
        {
            GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, this);
            serAC.getACQuestionValue(m_VD);
            // check if enumerated or not
            Vector vCRFval = (Vector) session.getAttribute("vQuestValue");
            if (vCRFval != null && vCRFval.size() > 0)
                m_VD.setVD_TYPE_FLAG("E");
            else
                m_VD.setVD_TYPE_FLAG("N");
            // read property file and set the VD bean for Placeholder data
            String VDDefinition = NCICurationServlet.m_settings.getProperty("VDDefinition");
            m_VD.setVD_PREFERRED_DEFINITION(VDDefinition);
            String DataType = NCICurationServlet.m_settings.getProperty("DataType");
            m_VD.setVD_DATA_TYPE(DataType);
            String MaxLength = NCICurationServlet.m_settings.getProperty("MaxLength");
            m_VD.setVD_MAX_LENGTH_NUM(MaxLength);
        }
        DataManager.setAttribute(session, "m_VD", m_VD);
        VD_Bean oldVD = new VD_Bean();
        oldVD = oldVD.cloneVD_Bean(m_VD);
        DataManager.setAttribute(session, "oldVDBean", oldVD);
        // DataManager.setAttribute(session, "oldVDBean", m_VD);
        ForwardJSP(m_classReq, m_classRes, "/CreateVDPage.jsp");
    }
 
    /**
    *
    * @throws Exception
    *
    */
   private void doRemoveBuildingBlocksVD() throws Exception
   {
       HttpSession session = m_classReq.getSession();
       String sSelRow = "";
       VD_Bean m_VD = (VD_Bean) session.getAttribute("m_VD");
       if (m_VD == null)
           m_VD = new VD_Bean();
       Vector<EVS_Bean> vRepTerm = (Vector) session.getAttribute("vRepTerm");
       if (vRepTerm == null)
           vRepTerm = new Vector<EVS_Bean>();
       String sComp = (String) m_classReq.getParameter("sCompBlocks");
       if (sComp == null)
           sComp = "";
       if (sComp.equals("RepTerm"))
       {
           EVS_Bean m_REP = new EVS_Bean();
           vRepTerm.setElementAt(m_REP, 0);
           DataManager.setAttribute(session, "vRepTerm", vRepTerm);
           m_VD.setVD_REP_NAME_PRIMARY("");
           m_VD.setVD_REP_CONCEPT_CODE("");
           m_VD.setVD_REP_EVS_CUI_ORIGEN("");
           m_VD.setVD_REP_IDSEQ("");
           DataManager.setAttribute(session, "RemoveRepBlock", "true");
           DataManager.setAttribute(session, "newRepTerm", "true");
       }
       else if (sComp.equals("RepQualifier"))
       {
           sSelRow = (String) m_classReq.getParameter("selRepQRow");
           if (sSelRow != null && !(sSelRow.equals("")))
           {
               Integer intObjRow = new Integer(sSelRow);
               int intObjRow2 = intObjRow.intValue();
               if (vRepTerm.size() > (intObjRow2 + 1))
               {
                   vRepTerm.removeElementAt(intObjRow2 + 1); // add 1 so zero element not removed
                   DataManager.setAttribute(session, "vRepTerm", vRepTerm);
               }
               // m_VD.setVD_REP_QUAL("");
               Vector vRepQualifierNames = m_VD.getVD_REP_QUALIFIER_NAMES();
               if (vRepQualifierNames == null)
                   vRepQualifierNames = new Vector();
               if (vRepQualifierNames.size() > intObjRow2)
                   vRepQualifierNames.removeElementAt(intObjRow2);
               Vector vRepQualifierCodes = m_VD.getVD_REP_QUALIFIER_CODES();
               if (vRepQualifierCodes == null)
                   vRepQualifierCodes = new Vector();
               if (vRepQualifierCodes.size() > intObjRow2)
                   vRepQualifierCodes.removeElementAt(intObjRow2);
               Vector vRepQualifierDB = m_VD.getVD_REP_QUALIFIER_DB();
               if (vRepQualifierDB == null)
                   vRepQualifierDB = new Vector();
               if (vRepQualifierDB.size() > intObjRow2)
                   vRepQualifierDB.removeElementAt(intObjRow2);
               m_VD.setVD_REP_QUALIFIER_NAMES(vRepQualifierNames);
               m_VD.setVD_REP_QUALIFIER_CODES(vRepQualifierCodes);
               m_VD.setVD_REP_QUALIFIER_DB(vRepQualifierDB);
               DataManager.setAttribute(session, "RemoveRepBlock", "true");
               DataManager.setAttribute(session, "newRepTerm", "true");
           }
       }
       else if (sComp.equals("VDObjectClass"))
       {
           m_VD.setVD_OBJ_CLASS("");
           DataManager.setAttribute(session, "m_OC", new EVS_Bean());
       }
       else if (sComp.equals("VDPropertyClass"))
       {
           m_VD.setVD_PROP_CLASS("");
           DataManager.setAttribute(session, "m_PC", new EVS_Bean());
       }
       m_setAC.setVDValueFromPage(m_classReq, m_classRes, m_VD);
       DataManager.setAttribute(session, "m_VD", m_VD);
   } // end of doRemoveQualifier
 
   /**method to go back from vd and pv edits
    * @param orgAct String value for origin where vd page was opened
    * @param menuAct String value of menu action where this use case started
    * @param actype String what action is expected
    * @param butPress STring last button pressed
    * @param vdPageFrom string to check if it was PV or VD page
    * @return String jsp to forward the page to
    */
   public String goBackfromVD(String orgAct, String menuAct, String actype, String butPress, String vdPageFrom)
   {
     try
     {
       //forward the page to editDE if originated from DE
       HttpSession session = m_classReq.getSession();
       clearBuildingBlockSessionAttributes(m_classReq, m_classRes);
       if (vdPageFrom.equals("create"))
       {
         clearCreateSessionAttributes(m_classReq, m_classRes);
         if (menuAct.equals("NewVDTemplate") || menuAct.equals("NewVDVersion"))
         {
            VD_Bean VDBean = (VD_Bean)session.getAttribute(PVForm.SESSION_SELECT_VD);
            GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, this);
            serAC.refreshData(m_classReq, m_classRes, null, null, VDBean, null, "Refresh", "");
            return "/SearchResultsPage.jsp";
         }
         else if (orgAct.equalsIgnoreCase("CreateNewVDfromEditDE"))
            return "/EditDEPage.jsp";
         else
            return "/CreateDEPage.jsp";
 
       }
       else if (vdPageFrom.equals("edit"))
       {
         if (orgAct.equalsIgnoreCase("editVDfromDE"))
            return "/EditDEPage.jsp";
         //forward the page to search if originated from Search
         else if (menuAct.equalsIgnoreCase("editVD") || orgAct.equalsIgnoreCase("EditVD") || orgAct.equalsIgnoreCase("BlockEditVD")  
         || (butPress.equals("Search") && !actype.equals("DataElement")))
         {
            VD_Bean VDBean = (VD_Bean)session.getAttribute(PVForm.SESSION_SELECT_VD);
            GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, this);
            serAC.refreshData(m_classReq, m_classRes, null, null, VDBean, null, "Refresh", "");
            return "/SearchResultsPage.jsp";
         }
         else
            return "/EditVDPage.jsp";
       }
     }
     catch (Exception e)
     {
       logger.error("ERROR - ", e);
     }
     
     return "";
   }
   
   /** to clear the edited data from the edit and create pages 
    * @param orgAct String value for origin where vd page was opened
    * @param menuAct String value of menu action where this use case started
    * @return String jsp to forward the page to
    */
   public String clearEditsOnPage(String orgAct, String menuAct)
   {
     try
     {
       HttpSession session = m_classReq.getSession();
       VD_Bean VDBean = (VD_Bean)session.getAttribute("oldVDBean");
       //clear related the session attributes 
       clearBuildingBlockSessionAttributes(m_classReq, m_classRes);
       String sVDID = VDBean.getVD_VD_IDSEQ();
       Vector vList = new Vector();           
       //get VD's attributes from the database again
       GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, this);
       if (sVDID != null && !sVDID.equals(""))
          serAC.doVDSearch(sVDID, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", 0, "", "", "", "", "", "", "", "",vList);
       //forward editVD page with this bean
       if (vList.size() > 0)
       {
         VDBean = (VD_Bean)vList.elementAt(0);
         VDBean = serAC.getVDAttributes(VDBean, orgAct, menuAct);
       }
       else
       {
         VDBean = new VD_Bean();
         VDBean.setVD_ASL_NAME("DRAFT NEW");
         VDBean.setAC_PREF_NAME_TYPE("SYS");
       }
       VD_Bean pgBean = new VD_Bean();
       DataManager.setAttribute(session, PVForm.SESSION_SELECT_VD, pgBean.cloneVD_Bean(VDBean));
     }
     catch (Exception e)
     {
       logger.error("ERROR - ", e);
     }
     return "/CreateVDPage.jsp";    
   }
 
   public void doOpenViewPage() throws Exception
   {
   	//System.out.println("I am here open view page");
   	HttpSession session = m_classReq.getSession();
   	String acID = (String) m_classReq.getAttribute("acIdseq");
   	if (acID.equals(""))
   		acID = m_classReq.getParameter("idseq");
       Vector<VD_Bean> vList = new Vector<VD_Bean>();
       // get DE's attributes from the database again
       GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, this);
       if (acID != null && !acID.equals(""))
       {
           serAC.doVDSearch(acID, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", 0, "", "", "", "", "",
                   "", "", "", vList);
       }
       if (vList.size() > 0) // get all attributes
       {
     	  VD_Bean VDBean = (VD_Bean) vList.elementAt(0);
           VDBean = serAC.getVDAttributes(VDBean, "openView", "viewVD");
           DataManager.setAttribute(session, "TabFocus", "VD");
           m_classReq.setAttribute("viewVDId", VDBean.getIDSEQ());
           String viewVD = "viewVD" + VDBean.getIDSEQ();
           DataManager.setAttribute(session, viewVD, VDBean);
           String title = "CDE Curation View VD "+VDBean.getVD_LONG_NAME()+ " [" + VDBean.getVD_VD_ID() + "v" + VDBean.getVD_VERSION() +"]";
 		  m_classReq.setAttribute("title", title);
 		  m_classReq.setAttribute("publicID", VDBean.getVD_VD_ID());
 		  m_classReq.setAttribute("version", VDBean.getVD_VERSION());
           m_classReq.setAttribute("IncludeViewPage", "EditVD.jsp") ;
      }
    }
 	
   public void doViewPageTab() throws Exception{
 	  String tab = m_classReq.getParameter("vdpvstab");
 	  String from = m_classReq.getParameter("from");
 	  String id = m_classReq.getParameter("id");
 	  String viewVD = "viewVD" + id;
 	  HttpSession session = m_classReq.getSession();
 	  VD_Bean VDBean = (VD_Bean)session.getAttribute(viewVD);
 	  String publicId = VDBean.getVD_VD_ID();
 	  String version = VDBean.getVD_VERSION();
 	  m_classReq.setAttribute("viewVDId", id);
 	  String title = "CDE Curation View VD "+VDBean.getVD_LONG_NAME()+ " [" + VDBean.getVD_VD_ID() + "v" + VDBean.getVD_VERSION() +"]";
 	  m_classReq.setAttribute("title", title);
 	  m_classReq.setAttribute("publicID", VDBean.getVD_VD_ID());
 	  m_classReq.setAttribute("version", VDBean.getVD_VERSION());
 	  if (from.equals("edit")){
 		  m_classReq.getSession().setAttribute("displayErrorMessage", "Yes");  
 	  }
 	  if (tab != null && tab.equals("PV")) {
 		  DataManager.setAttribute(session, "TabFocus", "PV");
 		  m_classReq.setAttribute("IncludeViewPage", "PermissibleValue.jsp") ;
           ForwardJSP(m_classReq, m_classRes, "/ViewPage.jsp");
 	  }else{
 		  DataManager.setAttribute(session, "TabFocus", "VD");
 		  m_classReq.setAttribute("IncludeViewPage", "EditVD.jsp") ;
           ForwardJSP(m_classReq, m_classRes, "/ViewPage.jsp");
 	  }
   }	
 }
