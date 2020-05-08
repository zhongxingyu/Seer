 // Copyright ScenPro, Inc 2007
 
 // $Header: /cvsshare/content/cvsroot/cdecurate/src/gov/nih/nci/cadsr/cdecurate/tool/PVServlet.java,v 1.42 2009-02-11 17:25:41 veerlah Exp $
 // $Name: not supported by cvs2svn $
 
 package gov.nih.nci.cadsr.cdecurate.tool;
 
 import gov.nih.nci.cadsr.cdecurate.database.Alternates;
 import gov.nih.nci.cadsr.cdecurate.ui.AltNamesDefsServlet;
 import gov.nih.nci.cadsr.cdecurate.util.DataManager;
 
 import java.io.Serializable;
 import java.text.SimpleDateFormat;
 import java.util.Vector;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import org.apache.log4j.Logger;
 
 /**
  * @author shegde
  *
  */
 public class PVServlet implements Serializable
 {
   private static final Logger logger = Logger.getLogger(PVServlet.class.getName());
   private PVForm data = null;
   private PVAction pvAction = new PVAction();
 
   /**constructor  
    * @param req HttpServletRequest Object
    * @param res HttpServletResponse object
    * @param ser CurationServlet object
    * */
   public PVServlet(HttpServletRequest req, HttpServletResponse res, CurationServlet ser)
   {
     data = new PVForm();
     data.setRequest(req);
     data.setResponse(res);
     data.setCurationServlet(ser);
     UtilService util = new UtilService();
     data.setUtil(util);
   }
 
  /**
    * The doEditPVActions method handles the submission of a CreatePV form
    * Calls 'doValidatePV' if the action is Validate or submit.
    * @return String JSP name to forward to
    *
    */
   public String doEditPVActions()
   {
       String retData = "/PermissibleValue.jsp";
       try
       {
          HttpSession session = data.getRequest().getSession();
          DataManager.setAttribute(session, "vStatMsg", null);  //reset the status message
          String sAction = (String)data.getRequest().getParameter("pageAction");
          data.setPageAction(sAction);
          String sMenuAction = (String)data.getRequest().getParameter("MenuAction");
          if (sMenuAction != null)
            DataManager.setAttribute(session, Session_Data.SESSION_MENU_ACTION, sMenuAction);
          sMenuAction = (String)session.getAttribute(Session_Data.SESSION_MENU_ACTION);
          data.setMenuAction(sMenuAction);
          String sButtonPressed = (String)session.getAttribute("LastMenuButtonPressed");
          data.setLastButtonPressed(sButtonPressed);
          String sOriginAction = (String)session.getAttribute("originAction");
          data.setOriginAction(sOriginAction);
         // String sSubAction = (String)data.getRequest().getParameter("VDAction");
          String sSubAction = (String)session.getAttribute("VDAction");  
          if (sSubAction == null)sSubAction ="";
          data.setVDAction(sSubAction);
          String vdPageFrom = "create";
          if (!sSubAction.equals("NewVD")) 
            vdPageFrom = "edit";
          
          doViewTypes();
          
          //clear searched data from teh session attributes
          if (sAction.equals("vddetailstab"))
          {
            DataManager.setAttribute(session, "TabFocus", "VD");
            retData = "/EditVDPage.jsp";
            if (vdPageFrom.equals("create"))
              retData = "/CreateVDPage.jsp";
            return retData;
          }
          else if (sAction.equals("vdpvstab"))
          {
            DataManager.setAttribute(session, "TabFocus", "PV");
            return "/PermissibleValue.jsp";
          }
          else if (sAction.equals("goBack"))
          {
            ValueDomainServlet vdser = (ValueDomainServlet) data.getCurationServlet().getACServlet("ValueDomain");
            return vdser.goBackfromVD(sOriginAction, sMenuAction, "", sButtonPressed, vdPageFrom);             
          }
          else if (sAction.equals("validate"))
          {
            //System.out.println("validate PVs and submit VD edits if all valid");
            return addPVValidates(null);
          }
          else if (sAction.equals("clearBoxes"))
          {
            //System.out.println("clear edits");
            ValueDomainServlet vdser = (ValueDomainServlet) data.getCurationServlet().getACServlet("ValueDomain");        
            vdser.clearEditsOnPage(sOriginAction, sMenuAction);  //, "pvEdits");
            return "/PermissibleValue.jsp";
          }
          else if (sAction.equals("save"))
          {
            //System.out.println("mark the pv to be saved");
            retData = savePVAttributes();
          }
          else if (sAction.equals("cancelNewPV"))
          {
            //System.out.println("mark the pv cancelled");
            return this.cancelNewPVEdits();
          }
          else if (sAction.equals("openCreateNew") || sAction.equals("addNewPV"))
          {
            //System.out.println("refresh the page");
            retData = readNewPVAttributes(sAction);
          }
          else if (sAction.equals("addSelectedCon"))
          {
            //System.out.println("add selected vm to the pv");
             data.getCurationServlet().doSelectVMConcept(data.getRequest(), data.getResponse(), sAction);
             return "/PermissibleValue.jsp";
          }
          else if (sAction.equals("selectParent"))
          {
            this.selectParents();
            return "/PermissibleValue.jsp";
          }
          else if (sAction.equals("CreateNonEVSRef"))
          {
            this.selectNonEVSParents();
            return "/PermissibleValue.jsp";
          }
          else if(sAction.equals("removePVandParent") || sAction.equals("removeParent"))
          {
            this.removeParents(sAction);
            return "/PermissibleValue.jsp";
          }
          else if (sAction.equals("restore"))
          {
            //System.out.println("put back the old data of the selected PV and refresh the data");
            return doRestorePV();
          }
          else if (sAction.equals("remove"))
          {
            //System.out.println("mark the pv as deleted and refresh the page");
            return doRemovePV();
          }
          else if (sAction.equals("removeAll"))
          {
            //System.out.println("mark the pv as deleted and refresh the page");
            return doRemoveAllPV();
          }
          else if (sAction.equals("changeAll"))
          {
            //System.out.println("do hte block edit pv");
            this.addPVOtherAttributes(null, "changeAll", "");
            data.getRequest().setAttribute(PVForm.REQUEST_FOCUS_ELEMENT, "pv0");  //"pv0View");
            return "/PermissibleValue.jsp";
          }
          else if (sAction.equals("appendSearchVM"))
          {
            //System.out.println("append Selected VM and refresh the page");
            return this.appendSearchVM();
          }
          else if (sAction.equals("sortPV"))
          {
            //System.out.println("sort the pvs by the heading");
            GetACSearch serAC = new GetACSearch(data.getRequest(), data.getResponse(), data.getCurationServlet());
            String sField = (String)data.getRequest().getParameter("pvSortColumn");
            VD_Bean vd = (VD_Bean) session.getAttribute("m_VD");
            serAC.getVDPVSortedRows(vd,sField,"edit","");          //call the method to sort pv attribute
            data.getRequest().setAttribute(PVForm.REQUEST_FOCUS_ELEMENT, "pv0");  //"pv0View");
            return "/PermissibleValue.jsp";
          }
          else if (sAction.equals("openEditVM"))
          {
            //System.out.println("open vm to edit with Selected VM");
            return openVMPageEdit();
          }
          else if (sAction.equals("continueVM"))
          {
            //System.out.println("continue to create another vm with different defn or concepts");
            return continueDuplicateVM();
          }
          else
            retData = "/PermissibleValue.jsp";
       }
       catch (RuntimeException e)
       {
         logger.error("ERROR - doEditPVAction ", e);
         //e.printStackTrace();
       }
       return retData;
   }
 
    /**
    * to mark the bean with last view type
    */
   @SuppressWarnings("unchecked")
    private void doViewTypes()
    {
      HttpSession session = (HttpSession)data.getRequest().getSession(); 
      VD_Bean vd = (VD_Bean)session.getAttribute(PVForm.SESSION_SELECT_VD);
      Vector<PV_Bean> vVDPV = null;
      if (vd != null) 
     	 vVDPV = vd.getVD_PV_List();  // (Vector<PV_Bean>)session.getAttribute("VDPVList");
      if (vVDPV == null) vVDPV = new Vector<PV_Bean>();
      String[] vwTypes = data.getRequest().getParameterValues("PVViewTypes"); 
      if (vwTypes != null)
      {
        pvAction.doResetViewTypes(vVDPV, vwTypes);
       // DataManager.setAttribute(session, "VDPVList", vVDPV);
        vd.setVD_PV_List(vVDPV);
        DataManager.setAttribute(session, PVForm.SESSION_SELECT_VD, vd);
      }
    }
    
    /**to reset the data for canceling out the New PV edits
    * @return String JSP to forward to
    */
   @SuppressWarnings("unchecked")
    private String cancelNewPVEdits()
    {
      HttpSession session = (HttpSession)data.getRequest().getSession(); 
      //put back the selected valid value if there was one
      PV_Bean pv = (PV_Bean)session.getAttribute("NewPV");
      if (pv != null && pv.getQUESTION_VALUE() != null && !pv.getQUESTION_VALUE().equals(""))
      {
        Vector<String> vQVList = (Vector)session.getAttribute("NonMatchVV");
        if (vQVList == null) vQVList = new Vector<String>();
        if (!vQVList.contains(pv.getQUESTION_VALUE()))
          vQVList.addElement(pv.getQUESTION_VALUE());
        DataManager.setAttribute(session, "NonMatchVV", vQVList);
        session.removeAttribute("NewPV");
      }
      return "/PermissibleValue.jsp";
    }
 
    /**New PV action opens the UI to create one or to save the edits for new one
    * @param sAct String PV page action
    * @return String JSP to forward to
    */
   @SuppressWarnings("unchecked")
    private String readNewPVAttributes(String sAct)
    {
       HttpSession session = (HttpSession)data.getRequest().getSession(); 
       if (sAct.equals("openCreateNew")) 
       {
         savePVAttributes();  //save the edited ones before refreshing
         //clean up the new bean
         PV_Bean pv = new PV_Bean();
         //inititalize date and origins
         SimpleDateFormat formatter = new SimpleDateFormat ("MM/dd/yyyy");
         pv.setPV_BEGIN_DATE(formatter.format(new java.util.Date()));
         data.getRequest().setAttribute("refreshPageAction", "openNewPV");
         DataManager.setAttribute(session, "NewPV", pv);  
         DataManager.setAttribute(session, "VMEditMsg", new Vector<VM_Bean>());
         data.getRequest().setAttribute(PVForm.REQUEST_FOCUS_ELEMENT, "divpvnew");
       }
       else if (sAct.equals("addNewPV"))
       {
         PV_Bean pv = (PV_Bean)session.getAttribute("NewPV");
         if (pv == null) pv = new PV_Bean();
         //make sure all the hand typed data is captured
         String sPV = (String)data.getRequest().getParameter("pvNewValue");  //value
         if (sPV == null) sPV = "";
         sPV = sPV.trim();
         pv.setPV_VALUE(sPV);
         readValidValueData(pv, "pvNew");
         //add pv other attribtutes 
         addPVOtherAttributes(pv, "changeOne", "pvNew");
         //if no concepts, read the user entered vm /desc
         VM_Bean vm = new VM_Bean();
         if (this.getDuplicateVMUse() != null)
           vm = data.getNewVM();
         else
         {
           VMServlet vmser = new VMServlet(data.getRequest(), data.getResponse(), data.getCurationServlet());
           vmser.readDataForCreate(pv, -1);
           vm = vmser.vmData.getVMBean();
           data.setStatusMsg(data.getStatusMsg() + vmser.vmData.getStatusMsg());
         }
         pv.setPV_VM(vm);
         pv.setPV_VIEW_TYPE("expand");
         pv.setVP_SUBMIT_ACTION(PVForm.CADSR_ACTION_INS);
         String erVM = (String)data.getRequest().getAttribute("ErrMsgAC");
         //update it only if there was no duplicates exisitng
         if (erVM == null || erVM.equals(""))
             updateVDPV(pv, -1);
         else
         {
           data.getRequest().setAttribute("refreshPageAction", "openNewPV");
           DataManager.setAttribute(session, "NewPV", pv);        
           data.getRequest().setAttribute(PVForm.REQUEST_FOCUS_ELEMENT, "divpvnew");          
         }
       }
       return "/PermissibleValue.jsp";
    }
 
    /**to save the edits for the existing PVs
    * @return String JSP to forward to
    */
   @SuppressWarnings("unchecked")
    private String savePVAttributes()
    {
       HttpSession session = data.getRequest().getSession();
       int pvInd = getSelectedPV();
       if (pvInd > -1)
       {
         //add pv other attribtutes 
         addPVOtherAttributes(null, "changeOne", "pv" + pvInd);
         PV_Bean selectPV = data.getSelectPV();
         //get the pv name from teh page
         String chgName = (String)data.getRequest().getParameter("txtpv" + pvInd + "Value");  //pvName  
         chgName = chgName.trim();
         //handle pv changes
         VM_Bean useVM = this.getDuplicateVMUse();
         if (useVM == null)
         {
           VMServlet vmser = new VMServlet(data.getRequest(), data.getResponse(), data.getCurationServlet());
           //go back it vm was not changed
           VM_Bean newVM = new VM_Bean().copyVMBean(selectPV.getPV_VM());
           String editVM = (String)data.getRequest().getParameter("currentVM");
           if (editVM != null && !editVM.equals(""))
           {
             vmser.readDataForCreate(selectPV, pvInd);
             newVM = vmser.vmData.getVMBean();
             data.setStatusMsg(data.getStatusMsg() + vmser.vmData.getStatusMsg());
           }
           if (newVM.getVM_SUBMIT_ACTION().equals(VMForm.CADSR_ACTION_INS))
             newVM._alts = null;
           data.setNewVM(newVM);
           selectPV = pvAction.changePVAttributes(chgName, pvInd, data);
         }
         else
             selectPV.setPV_VM(useVM);
         
         String erVM = (String)data.getRequest().getAttribute("ErrMsgAC");
         if (erVM == null || erVM.equals(""))
             updateVDPV(selectPV, pvInd);
         else
         {
             //store it in the session
             DataManager.setAttribute(session, PVForm.SESSION_SELECT_VD, data.getVD());
             data.getRequest().setAttribute(PVForm.REQUEST_FOCUS_ELEMENT, "pv" + pvInd);  //"pv" + pvInd + "View");
         }
         //status messae
         if (!data.getStatusMsg().equals(""))
           logger.info("PV Status " + data.getStatusMsg());
       }
       return "/PermissibleValue.jsp";
    }
 
   /**
    * continue with the vm changes when definition/concept match occur
    * @return name of the jsp
    */
   private String continueDuplicateVM()
   {
       //find the selected pv ind
       int pvInd = getSelectedPV();      
       //make it as ins vm and update/edit pv
       PV_Bean pv = data.getSelectPV();
       VM_Bean vm = pv.getPV_VM();
       vm.setVM_SUBMIT_ACTION(VMForm.CADSR_ACTION_INS);
       pv.setPV_VM(vm);
       updateVDPV(pv, pvInd);
       return "/PermissibleValue.jsp";
   }
   
   /**
    * update changes for the pv on vd
    * @param pv PV_Bean object
    * @param pvInd int pv index
    */
   private void updateVDPV(PV_Bean pv, int pvInd)
   {
       HttpSession session = (HttpSession)data.getRequest().getSession(); 
       VD_Bean vd = (VD_Bean)session.getAttribute(PVForm.SESSION_SELECT_VD);
       if (pvInd > -1)
       {
           String retMsg = pvAction.changePVQCAttributes(pv, pvInd, vd, data);
           if (!retMsg.equals(""))
               data.getCurationServlet().storeStatusMsg(retMsg);
               //DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, retMsg);
       }
       else
       {
           Vector<PV_Bean> vdpvList = vd.getVD_PV_List();  
           if (vdpvList == null) vdpvList = new Vector<PV_Bean>();          
           vdpvList.insertElementAt(pv, 0);
           vd.setVD_PV_List(vdpvList);
           pvInd = 0;
       }
       DataManager.setAttribute(session, PVForm.SESSION_SELECT_VD, vd);
       data.getRequest().setAttribute(PVForm.REQUEST_FOCUS_ELEMENT, "pv" + pvInd); 
       //remove the vector of duplicate vm 
       data.getRequest().getSession().removeAttribute("VMEditMsg");
   }
   
   /**adds the Valid value changes from the page to the PV Bean
    * @param pv PV BEan object
    * @param pvID String selected pv index
    */
   @SuppressWarnings("unchecked")
   private void readValidValueData(PV_Bean pv, String pvID)
    {
      HttpSession session = data.getRequest().getSession();
      String sVVid = "";
      if (pvID.equals("pvNew"))
        sVVid = (String)data.getRequest().getParameter("selValidValue");  //valid value
      else
        sVVid = (String)data.getRequest().getParameter(pvID + "selValidValue");  //valid value       
      if (sVVid == null) //modify if only changed
        return;
      //get the earlier value
      String sOldValue = pv.getQUESTION_VALUE();
      if (sOldValue == null) sOldValue = "";
      pv.setQUESTION_VALUE_IDSEQ(sVVid);
      //set name to empty reset it once found the right one
      pv.setQUESTION_VALUE("");
      Vector<Quest_Value_Bean> vQuest = (Vector)session.getAttribute("vQuestValue");
      if (vQuest == null) vQuest = new Vector<Quest_Value_Bean>();
      Vector<String> vQVList = (Vector)session.getAttribute("NonMatchVV");
      if (vQVList == null) vQVList = new Vector<String>();
      String sSelValue = "";
      for (int i =0; i<vQuest.size(); i++)
      {
        Quest_Value_Bean qvBean = (Quest_Value_Bean)vQuest.elementAt(i);
        String sQValue = qvBean.getQUESTION_VALUE();
        String sQVid = qvBean.getQUESTION_VALUE_IDSEQ();
        if (sQVid.equals(sVVid)) //not assigned yet
        {
          pv.setQUESTION_VALUE(sQValue); 
          if (vQVList.contains(sQValue))
            vQVList.removeElement(sQValue);
          sSelValue = sQValue;
          break;
        }
      }
      if (!sOldValue.equals( "") && !sSelValue.equals(sOldValue) && !vQVList.contains(sOldValue))
        vQVList.addElement(sOldValue);
      DataManager.setAttribute(session, "NonMatchVV", vQVList);
    }
    
    /** to get the selected pv on from the page
    * @return int index of the PV that is selected
    */
   private int getSelectedPV()
    {
      int pvInd = -1;
      //read edited pv
      String selPVInd = (String)data.getRequest().getParameter("editPVInd");  //index
      //System.out.println(selPVInd + " edited PV " + pvInd);
      if (selPVInd != null && !selPVInd.equals("")) 
      {
        selPVInd = selPVInd.substring(2);
        if (selPVInd != null && !selPVInd.equals(""))
        {
          if (selPVInd.equalsIgnoreCase("New"))
            pvInd = -1;
          else
            pvInd = new Integer(selPVInd).intValue();
        }
      }
 
      HttpSession session = data.getRequest().getSession();
      PV_Bean selectPV = new PV_Bean();
      if (pvInd > -1)
      {
        VD_Bean vd = (VD_Bean)session.getAttribute(PVForm.SESSION_SELECT_VD);      
        data.setVD(vd);
        
        Vector<PV_Bean> vVDPVList = vd.getVD_PV_List();  // (Vector)session.getAttribute("VDPVList");
        if (vVDPVList == null) vVDPVList = new Vector<PV_Bean>();
        selectPV = (PV_Bean)vVDPVList.elementAt(pvInd);
        if (selectPV != null && selectPV.getPV_VALUE() != null && !selectPV.getPV_VALUE().equals(""))
          data.setSelectPV(selectPV);
      }
      else
      {
        selectPV = (PV_Bean)session.getAttribute("NewPV");
        if (selectPV == null) 
          selectPV = new PV_Bean();
        data.setSelectPV(selectPV);
      }
      return pvInd;
    }
 
    /**to get the selected vm from the PV page
    * @return int edited pv indicator
    */
   private int getSelectedVM()
    {
      int pvInd = -1;
      //read edited pv
      String selPVInd = (String)data.getRequest().getParameter("editPVInd");  //index
     // System.out.println(selPVInd + " edited PV " + pvInd);
      if (selPVInd != null && !selPVInd.equals("")) 
      {
        selPVInd = selPVInd.substring(2);
        if (selPVInd != null && !selPVInd.equals(""))
        {
          if (selPVInd.equalsIgnoreCase("New"))
            pvInd = -1;
          else
            pvInd = new Integer(selPVInd).intValue();
        }
      }
     // System.out.println(selPVInd + " edited PV " + pvInd);
      HttpSession session = data.getRequest().getSession();
      VM_Bean selVM = (VM_Bean)session.getAttribute(VMForm.SESSION_SELECT_VM);
      //if (selVM == null || selVM.getVM_SHORT_MEANING().equals(""))
      if (selVM == null || selVM.getVM_LONG_NAME().equals(""))
      {
        PV_Bean selectPV = new PV_Bean();
        if (pvInd > -1)
        {
          VD_Bean vd = (VD_Bean)session.getAttribute(PVForm.SESSION_SELECT_VD);      
          data.setVD(vd);
          
          Vector<PV_Bean> vVDPVList = vd.getVD_PV_List();  // (Vector)session.getAttribute("VDPVList");
          if (vVDPVList == null) vVDPVList = new Vector<PV_Bean>();
          selectPV = (PV_Bean)vVDPVList.elementAt(pvInd);
          if (selectPV != null && selectPV.getPV_VALUE() != null && !selectPV.getPV_VALUE().equals(""))
            selVM = new VM_Bean().copyVMBean(selectPV.getPV_VM());
        }
        else
        {
          selectPV = (PV_Bean)session.getAttribute("NewPV");
          if (selectPV != null) 
            selVM = selectPV.getPV_VM();
        }     
      }
      data.setNewVM(selVM);
      return pvInd;
    }
 
    /**Adds the pv attributes from the page to the selected PV
    * @param pv PV_Bean object
    * @param changeType String single or multiple pvs to change
    * @param pvID String selected pvindex from the page
    */
   private void addPVOtherAttributes(PV_Bean pv, String changeType, String pvID)
    {
      if (pv == null)
        pv = data.getSelectPV();
      String chgOrg = (String)data.getRequest().getParameter("currentOrg");  //edited origin
      if (chgOrg != null && !chgOrg.equals(""))
      {
        if (changeType.equals("changeAll"))
        {
          //System.out.println("all origins");
          HttpSession session = data.getRequest().getSession();
          VD_Bean vd = (VD_Bean)session.getAttribute(PVForm.SESSION_SELECT_VD);
          data.setVD(vd);
          pvAction.doBlockEditPV(data, "origin", chgOrg);
          DataManager.setAttribute(session, PVForm.SESSION_SELECT_VD, data.getVD());
          return;
        }
        //else  
        pv.setPV_VALUE_ORIGIN(chgOrg);
      }
      
      String chgBD = (String)data.getRequest().getParameter("currentBD");  //edited begom date
      if (chgBD != null && !chgBD.equals(""))
      {
        if (changeType.equals("changeAll"))
        {
          //System.out.println("all begin date");
          HttpSession session = data.getRequest().getSession();
          VD_Bean vd = (VD_Bean)session.getAttribute(PVForm.SESSION_SELECT_VD);
          data.setVD(vd);
          pvAction.doBlockEditPV(data, "begindate", chgBD);
          DataManager.setAttribute(session, PVForm.SESSION_SELECT_VD, data.getVD());
          return;
        }
       // else
        pv.setPV_BEGIN_DATE(chgBD);
      }
      
      String chgED = (String)data.getRequest().getParameter("currentED");  //edited end date
      if (chgED != null && !chgED.equals(""))
      {
        if (changeType.equals("changeAll"))
        {
          //System.out.println("all end date");
          HttpSession session = data.getRequest().getSession();
          VD_Bean vd = (VD_Bean)session.getAttribute(PVForm.SESSION_SELECT_VD);
          data.setVD(vd);
          pvAction.doBlockEditPV(data, "enddate", chgED);
          DataManager.setAttribute(session, PVForm.SESSION_SELECT_VD, data.getVD());
          return;
        }
        //else
        pv.setPV_END_DATE(chgED);
      }
      //valid values
      if (pv != null)
        this.readValidValueData(pv, pvID);
      
      data.setSelectPV(pv);
    }
    
    /** add teh pv attributes to the validate vector
    * @param vd VD_Bean object
    * @return string page to return back
    */
   public String addPVValidates(VD_Bean vd)
   {
      HttpSession session = data.getRequest().getSession();
      if (vd ==null)
        vd = (VD_Bean)session.getAttribute(PVForm.SESSION_SELECT_VD);
      data.setVD(vd);
      String sOriginAction = (String)session.getAttribute("originAction");
      data.setOriginAction(sOriginAction);
      //add values to validate bean
      Vector<String> vValString = pvAction.addValidateVDPVS(data);
      //add the vectors to the session
      data.getRequest().setAttribute("vValidate", vValString);  
      vd = data.getVD();
      data.getRequest().setAttribute(PVForm.SESSION_SELECT_VD, vd);        
      return "/ValidateVDPage.jsp";
   }
 
    /** collects the pv attributes when opened to edit vd
    * @param vd  VD_Bean object
    * @param sMenu menu action to get teh original action it started
    */
   public void getPVAttributes(VD_Bean vd, String sMenu)  // 
    {
       try
       {
         String pvAct = "Search";
          if (sMenu.equals("NewVDTemplate") || sMenu.equals("NewVDVersion")) 
              pvAct = "NewUsing"; 
 
          String acIdseq = vd.getVD_VD_IDSEQ();
          //String acName = vd.getVD_LONG_NAME();
          Vector<PV_Bean> vdpv = pvAction.doPVACSearch(acIdseq, pvAct, data);
          vd.setVD_PV_List(vdpv);
          //pvCount = this.doPVACSearch(VDBean.getVD_VD_IDSEQ(), VDBean.getVD_LONG_NAME(), pvAct);
          GetACSearch serAC = new GetACSearch(data.getRequest(), data.getResponse(), data.getCurationServlet());
          if (sMenu.equals("Questions"))
            serAC.getACQuestionValue(vd);
    
          //get vd parent attributes
          GetACService getAC = new GetACService(data.getRequest(), data.getResponse(), data.getCurationServlet());
          Vector<EVS_Bean> vParent = new Vector<EVS_Bean>();
          String sCondr = vd.getVD_PAR_CONDR_IDSEQ();
          if (sCondr != null && !sCondr.equals(""))
            vParent = getAC.getAC_Concepts(vd.getVD_PAR_CONDR_IDSEQ(), vd, true);
          //get the system name and for new template make the vd_id null
          if (sMenu.equals("NewVDTemplate")) 
              vd.setVD_VD_ID("");
          vd = (VD_Bean) data.getCurationServlet().getSystemName(vd, vParent);
          vParent = serAC.getNonEVSParent(vParent, vd, sMenu);
           
          //DataManager.setAttribute(session, "VDParentConcept", vParent);  
          vd.setReferenceConceptList(vParent);
       }
       catch (Exception e)
       {
         logger.error("Error getPVattributes - " + e.toString(), e);
       }
    }  //doPVACSearch search
 
    /** call to do the pv search 
    * @param InString String keyword search
    * @param cd_idseq  String cd idseq to filter by Conceptual domain
    * @param conName String  concept name filter
    * @param conID String concept id filter
    * @return vector of PV Bean object from search results
    */
    public Vector<PV_Bean> searchPVAttributes(String InString, String cd_idseq, String conName, String conID, String sRecordsDisplayed)
    {
     Vector<PV_Bean> vdpv = pvAction.doPVVMSearch(InString, cd_idseq, conName, conID, data, sRecordsDisplayed);
      return vdpv;
    }
 
    /** to display / get the pv resutls from the ac
    * @param vd VD Bean object
    * @param iUPD int value to reset after versioning
    * @param acID String ac_idseq to filter
    * @param acName String ac name to  display
    */
    public void searchVersionPV(VD_Bean vd, int iUPD, String acID, String acName)
    {
      if (vd != null)
        acID = vd.getVD_VD_IDSEQ();
      Vector<PV_Bean> verList = pvAction.doPVACSearch(acID, "", data);
      if (iUPD == 1)
        pvAction.doResetVersionVDPV(vd, verList);
      data.getRequest().setAttribute("PermValueList", verList);
      data.getRequest().setAttribute("ACName", acName);
    }
    
    /** store the concept information in the vector and bean after searching for the concept
    * @return jsp page to return
    */
    @SuppressWarnings("unchecked")
    public String storeConceptAttributes()
    {
      //get the editing VM from teh page
      getSelectedVM(); 
      VM_Bean selectVM = data.getNewVM();
      if (selectVM != null)
      {   
        VMServlet VMSer = new VMServlet(data.getRequest(), data.getResponse(), data.getCurationServlet());
        String errMsg = VMSer.appendConceptVM(selectVM, ConceptForm.FOR_PV_PAGE_CONCEPT);
        if (errMsg.equals(""))
        {
          HttpSession session = data.getRequest().getSession();
          Vector<String> vRes = new Vector<String>();
          DataManager.setAttribute(session, "results", vRes);
        }
      }       
      return "/OpenSearchWindowBlocks.jsp";
    }
    
    /** to submit the permissible value to caDSR
     * first removes deleted ones, loops through the insert or updated ones in the order
     * create/update Concept, VM, PV, VDPVS, CRF 
    * @param vd VDBean object
    * @return String status message
    */
   @SuppressWarnings("unchecked")
    public String submitPV(VD_Bean vd)
    {
      String errMsg = "";
      //delete teh vdpv relationship if it was deleted from teh page
      errMsg = doRemoveVDPV(vd);
      
      //insert or update vdpvs relationship
      Vector<PV_Bean> vVDPVS = vd.getVD_PV_List();
      if (vVDPVS == null) vVDPVS = new Vector<PV_Bean>();
      for (int j=0; j<vVDPVS.size(); j++)
      {
          PV_Bean pvBean = (PV_Bean)vVDPVS.elementAt(j);
          //submit the pv to either insert or update if something done to this pv
          String vpAction = pvBean.getVP_SUBMIT_ACTION();
          if (vpAction == null) vpAction = "NONE";
          //udpate pv and vdpvs only if edited      
          if (!vpAction.equals("NONE") && !vpAction.equals("DEL"))
          {
              //submit the vm if edited
              VM_Bean vm = pvBean.getPV_VM();
              if (vm.getVM_CD_IDSEQ() == null || vm.getVM_CD_IDSEQ().equals(""))
                vm.setVM_CD_IDSEQ(vd.getVD_CD_IDSEQ());
              VMServlet vmser = new VMServlet(data.getRequest(), data.getResponse(), data.getCurationServlet());
              String err = vmser.submitVM(vm);
              //capture the message if any
              if (err != null && !err.equals(""))
              {
                errMsg += "\\n" + err;
                continue;
              }
              // create pv
              err = doSubmitPV(vpAction, pvBean, vm);
              //capture the message if any
              if (err != null && !err.equals(""))
              {
                errMsg += "\\n" +  err;
                continue;
              }
 
              //update pv to vd 
              data.setSelectPV(pvBean);
              data.setVD(vd);
              err = pvAction.setVD_PVS(data);
              //capture the message if any
              if (err != null && !err.equals(""))
              {
                errMsg += "\\n" + err;
                continue;
              }
 
              //create crf value pv relationship in QC table.
              pvBean = data.getSelectPV();
              String vpID = pvBean.getPV_VDPVS_IDSEQ();
              if (pvBean.getVP_SUBMIT_ACTION().equals(PVForm.CADSR_ACTION_INS) && (vpID != null  || !vpID.equals("")))
              {
                 InsACService insac = new InsACService(data.getRequest(), data.getResponse(), data.getCurationServlet());
                 insac.UpdateCRFValue(pvBean);
              }
              //update teh collection
              pvBean.setVP_SUBMIT_ACTION(PVForm.CADSR_ACTION_NONE);
              vVDPVS.setElementAt(pvBean, j);
          }
        }  //end loop
        vd.setVD_PV_List(vVDPVS);
        data.getRequest().setAttribute("retcode", data.getRetErrorCode()); 
        //log the error message
        if (!errMsg.equals(""))
          logger.error("ERROR at submit - PV : " + errMsg);
 
        return errMsg; //data.getStatusMsg();
    }
   
    /**To delete the removed VD PV from the database
    * @param vd VDBean object
    * @return String status message
    */
    public String doRemoveVDPV(VD_Bean vd)
    {
      String errMsg = "";
      //delete the record when items were added to the deleted vector
      Vector<PV_Bean> delVDPV = vd.getRemoved_VDPVList();  // (Vector)session.getAttribute("RemovedPVList");
      if (delVDPV != null)
      {
        //reset the status message
        for (int i =0; i<delVDPV.size(); i++)
        {
          data.setStatusMsg("");
          PV_Bean pv = delVDPV.elementAt(i);
          String idseq = pv.getPV_PV_IDSEQ();
          //call the method to remove from the database
          if (idseq != null && !idseq.equals("") && !idseq.contains("EVS"))
          {
            pv.setVP_SUBMIT_ACTION(PVForm.CADSR_ACTION_DEL);
            data.setSelectPV(pv);
            data.setVD(vd);
            String ret = pvAction.setVD_PVS(data);
            if (ret != null && !ret.equals(""))
              errMsg += "\\n" + ret;             
          }
        }
        vd.setRemoved_VDPVList(new Vector<PV_Bean>());
      }
      //log the error message
      if (!errMsg.equals(""))
        logger.error("ERROR at submit - doRemoveVDPV : " + errMsg);
      return errMsg;
    }
 
    /**
     * to submit the changes for permissible value
     * 
     * @param vpAction String sql action
     * @param pvBean PV_Bean object
     * @param vm VM_Bean object
     * @return String success message
     */
    private String doSubmitPV(String vpAction, PV_Bean pvBean, VM_Bean vm)
    {
      data.setStatusMsg("");
      String sPVid = pvBean.getPV_PV_IDSEQ();
      String vmName = pvBean.getPV_SHORT_MEANING();
      if (vmName == null) vmName = "";
      //short meaning don't match from pv to vm
      //  if (vpAction.equals("INS") || vmName.equals("")  || !vmName.equals(vm.getVM_SHORT_MEANING()))
      if (vpAction.equals("INS") || vmName.equals("")  || !vmName.equals(vm.getVM_LONG_NAME()))
      {
        sPVid = "";
        pvBean.setPV_PV_IDSEQ(sPVid);
        pvBean.setPV_VDPVS_IDSEQ(sPVid);
      }
     // pvBean.setPV_SHORT_MEANING(vm.getVM_SHORT_MEANING());  //need to update PV vm befor esubmit
      pvBean.setPV_SHORT_MEANING(vm.getVM_LONG_NAME());  //need to update PV vm befor esubmit
      data.setSelectPV(pvBean);
      String ret = "";
      if (sPVid == null || sPVid.equals("") || sPVid.contains("EVS"))   
        ret = pvAction.setPV(data);      
      
      return ret;
    }
    
    /**
     * initilize the data before opening the VM edit page
     * @return String jsp name
     */
    private String openVMPageEdit()
    {
      String jsp = VMForm.JSP_PV_DETAIL; 
      try
      {
        int pvInd = getSelectedPV();
        if (pvInd > -1)
        {
          HttpSession session = data.getRequest().getSession();
          PV_Bean selPV = data.getSelectPV();
          //get the vm
          VMAction vmact = new VMAction();
          VM_Bean selVM = vmact.getVM(selPV, 0);       
          //set the attributes
          DataManager.setAttribute(session, VMForm.SESSION_SELECT_VM, selVM);
          DataManager.setAttribute(session, VMForm.SESSION_SELECT_PV, selPV);
          DataManager.setAttribute(session, PVForm.SESSION_PV_INDEX, pvInd);
          DataManager.setAttribute(session, VMForm.SESSION_VM_TAB_FOCUS, VMForm.ELM_ACT_DETAIL_TAB);
          DataManager.setAttribute(session, VMForm.SESSION_RET_PAGE, VMForm.ACT_BACK_PV);
          //get vm alt names
          AltNamesDefsServlet altSer = new AltNamesDefsServlet(data.getCurationServlet(), data.getCurationServlet().sessionData.UsrBean);
          Alternates alt = altSer.getManualDefinition(data.getRequest(), VMForm.ELM_FORM_SEARCH_EVS);
          if (alt != null && !alt.getName().equals(""))
            selVM.setVM_ALT_DEFINITION(alt.getName());
          //write the jsp
          VMServlet vmser = new VMServlet(data.getRequest(), data.getResponse(), data.getCurationServlet());
          VM_Bean vm = (VM_Bean)session.getAttribute(VMForm.SESSION_SELECT_VM);
          vmser.writeDetailJsp(vm);
          jsp = VMForm.JSP_VM_DETAIL;     
        }
      }
      catch (Exception e)
      {
       logger.error("ERROR -load vm ", e);
      }
      return jsp;     
    }
    
    /**
     * initilize the data before opening the VM edit page
     * @return String jsp name
     */
    public String openVMPageEdit(VM_Bean selVM, PV_Bean selPV)
    {
      String jsp = VMForm.JSP_PV_DETAIL; 
      try
      {
          HttpSession session = data.getRequest().getSession();
              
          //set the attributes
          DataManager.setAttribute(session, VMForm.SESSION_SELECT_VM, selVM);
          DataManager.setAttribute(session, VMForm.SESSION_SELECT_PV, selPV);
          DataManager.setAttribute(session, VMForm.SESSION_VM_TAB_FOCUS, VMForm.ELM_ACT_DETAIL_TAB);
          DataManager.setAttribute(session, VMForm.SESSION_RET_PAGE, VMForm.ACT_BACK_SEARCH);
          //get vm alt names
          AltNamesDefsServlet altSer = new AltNamesDefsServlet(data.getCurationServlet(), data.getCurationServlet().sessionData.UsrBean);
          Alternates alt = altSer.getManualDefinition(data.getRequest(), VMForm.ELM_FORM_SEARCH_EVS);
          if (alt != null && !alt.getName().equals(""))
            selVM.setVM_ALT_DEFINITION(alt.getName());
          //write the jsp
          VMServlet vmser = new VMServlet(data.getRequest(), data.getResponse(), data.getCurationServlet());
          VM_Bean vm = (VM_Bean)session.getAttribute(VMForm.SESSION_SELECT_VM);
          vmser.writeDetailJsp(vm);
          jsp = VMForm.JSP_VM_DETAIL;     
        }
      catch (Exception e)
      {
       logger.error("ERROR -load vm ", e);
      }
      return jsp;     
    } 
    /**To mark PV to be removed from the page 
    * @return String JSP name to forward to
    */
    private String doRemovePV()
    {
      int pvInd = getSelectedPV();
      if (pvInd > -1)
      {
        HttpSession session = data.getRequest().getSession();
        //remove the pv from the current vd list
        VD_Bean vd = data.getVD();
        PV_Bean selPV = data.getSelectPV();
        //check if associated with the form
        boolean isExists = false; 
        String vpIDseq = selPV.getPV_VDPVS_IDSEQ();
        if (vpIDseq != null && !vpIDseq.equals(""))
            isExists = pvAction.checkPVQCExists("", vpIDseq, data);
        //do not remove if assoicated to a form
        if (isExists)
        {
          String sCRFmsg = "Unable to remove the Permissible Value (PV) (" + selPV.getPV_VALUE() + ") because the Permissible Value is used in a CRF." +
                      "\\n You may remove the PV after dis-associating it from the CRF.";
          //DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, sCRFmsg);
          data.getCurationServlet().storeStatusMsg(sCRFmsg);
          selPV.setVP_SUBMIT_ACTION(PVForm.CADSR_ACTION_NONE);
        }
        else
        {
            pvAction.doRemovePV(vd, pvInd, selPV, 0);
           //make the one before to be in view
            if (pvInd > 0)
              pvInd = (pvInd - 1);           
            DataManager.setAttribute(session, PVForm.SESSION_SELECT_VD, vd);
         }
      }
      data.getRequest().setAttribute(PVForm.REQUEST_FOCUS_ELEMENT, "pv" + pvInd);  //"pv" + pvInd + "View");
      return "/PermissibleValue.jsp";
    }
 
    /**To mark PV to be removed from the page 
     * @return String JSP name to forward to
     */
     private String doRemoveAllPV()
     {
       String jsp = "/PermissibleValue.jsp";
       HttpSession session = data.getRequest().getSession();
       VD_Bean vd = (VD_Bean)session.getAttribute(PVForm.SESSION_SELECT_VD);
       Vector<PV_Bean> vdpv = vd.getVD_PV_List();
       if (vdpv == null || vdpv.size() == 0)
           return jsp;
       String sCRFmsg = "";
       //loop through each pv to delete them all
       for (int i=0; i<vdpv.size(); i++)
       {
         PV_Bean selPV = vdpv.elementAt(i);
         //check if associated with the form
         boolean isExists = false; 
         String vpIDseq = selPV.getPV_VDPVS_IDSEQ();
         if (vpIDseq != null && !vpIDseq.equals(""))
             isExists = pvAction.checkPVQCExists("", vpIDseq, data);
         //do not remove if assoicated to a form
         if (isExists)
         {
           sCRFmsg += "\\n\\t" + selPV.getPV_VALUE();
           selPV.setVP_SUBMIT_ACTION(PVForm.CADSR_ACTION_NONE);
         }
         else
         {
             pvAction.doRemovePV(vd, i, selPV, 0);
            //make the one before to be in view
             i -= 1;
         }
       }
       if (!sCRFmsg.equals(""))
       {
           sCRFmsg = "Unable to remove the following Permissible Value(s) because it may be used in a CRF." +
           "\\n You may remove the PV after dis-associating it from the CRF." + sCRFmsg;
           data.getCurationServlet().storeStatusMsg(sCRFmsg);
       }
       DataManager.setAttribute(session, PVForm.SESSION_SELECT_VD, vd);      
       //DataManager.setAttribute(session, Session_Data.SESSION_STATUS_MESSAGE, sCRFmsg);
       return jsp;
     }
 
    /**To restore the PV edits on the page.
     * Also to canceel the use selection when existing vm was found.
    * @return String JSP name to forward to
    */
   @SuppressWarnings("unchecked")
    private String doRestorePV()
    {
      HttpSession session = data.getRequest().getSession();
      int pvInd = getSelectedPV();
      Vector<VM_Bean> errVMs = (Vector<VM_Bean>)session.getAttribute("VMEditMsg");
      if (errVMs != null && errVMs.size() > 0)
      {
        session.removeAttribute("VMEditMsg");  
        if (pvInd == -1)
        {
          data.getRequest().setAttribute("refreshPageAction", "openNewPV");
          data.getRequest().setAttribute(PVForm.REQUEST_FOCUS_ELEMENT, "divpvnew");          
          data.getRequest().setAttribute("editPVInd", pvInd);
        }
        else
        {
          data.getRequest().setAttribute("editPVInd", pvInd);
          data.getRequest().setAttribute("refreshPageAction", "restore");
          data.getRequest().setAttribute(PVForm.REQUEST_FOCUS_ELEMENT, "pv" + pvInd);  //"pv" + pvInd + "View");     
        }
      }
      else
      {
        if (pvInd > -1)
        {
          //remove the pv from the current vd list
          PV_Bean selPV = data.getSelectPV();
          VD_Bean vd = data.getVD();
          if (selPV != null)
          {
            //check it idseq was from cadsr
            String idseq = selPV.getPV_PV_IDSEQ();
            PV_Bean orgPV = new PV_Bean();
            Vector<PV_Bean> vCurVP = vd.getVD_PV_List();
            if (idseq != null && !idseq.equals("") && !idseq.contains("EVS"))
            {
              VD_Bean oldvd = (VD_Bean)session.getAttribute("oldVDBean");
              Vector<PV_Bean> vdpvs = oldvd.getVD_PV_List();
              if (vdpvs.size() > 0)  
              {
                for (int i=0; i<vdpvs.size(); i++)
                {
                  PV_Bean thisPV =  (PV_Bean)vdpvs.elementAt(i);
                  if (thisPV.getPV_PV_IDSEQ() != null && selPV.getPV_PV_IDSEQ() != null && thisPV.getPV_PV_IDSEQ().equals(selPV.getPV_PV_IDSEQ()))
                  {
                    orgPV = orgPV.copyBean(thisPV);
                    pvAction.putBackRemovedPV(vd, thisPV.getPV_PV_IDSEQ());
                    break;
                  }
                }
              }
            }
            else
              orgPV = orgPV.copyBean(selPV);
            //reset it only if not null
            if (orgPV != null)
            {
              orgPV.setPV_VIEW_TYPE("expand");
              vCurVP.setElementAt(orgPV, pvInd);
            }
            //put it back in the vd
            vd.setVD_PV_List(vCurVP); 
            DataManager.setAttribute(session, PVForm.SESSION_SELECT_VD, vd);
          }
          data.getRequest().setAttribute(PVForm.REQUEST_FOCUS_ELEMENT, "pv" + pvInd);  //"pv" + pvInd + "View");     
        }
      }
      return "/PermissibleValue.jsp";
    }
 
    /**
     * gets the selected vm from the resutls and append the concepts and other attributes to the pv bean
     * @return String  JSP name to forward to
     */
    @SuppressWarnings("unchecked")
    public String appendSearchVM()
    {
      try
     {
       //read the selected row from the request
        HttpSession session = (HttpSession)data.getRequest().getSession();
        Vector<VM_Bean> vRSel = (Vector)session.getAttribute("vACSearch");
        if (vRSel == null) vRSel = new Vector<VM_Bean>();
 
        //get the array from teh hidden list
        String selRows[] = data.getRequest().getParameterValues("hiddenSelRow");  //("hiddenSelRow");
        if (selRows == null)
          data.setStatusMsg(data.getStatusMsg() + "\\tUnable to select value meaning, please try again");    
        else
        {
          PV_Bean pv = (PV_Bean)session.getAttribute("NewPV");
          if (pv == null) pv = new PV_Bean();
          //make sure all the hand typed data is captured
          String sPV = (String)data.getRequest().getParameter("pvNewValue");  //value
          if (sPV == null) sPV = "";
          pv.setPV_VALUE(sPV);
          //add pv other attribtutes 
          addPVOtherAttributes(pv, "changeOne", "pvNew");
          //get the vm bean
          VMAction vmAct = new VMAction();
          vmAct.doAppendSelectVM(selRows, vRSel, pv);
          DataManager.setAttribute(session, "NewPV", pv);
        }
        data.getRequest().setAttribute(PVForm.REQUEST_FOCUS_ELEMENT, "divpvnew");
        data.getRequest().setAttribute("refreshPageAction", "openNewPV");
     }
     catch (RuntimeException e)
     {
       logger.error("ERROR - appendSearch VM ", e);
     }
      return "/PermissibleValue.jsp";
    }
    
    /**to mark removal of Parent concepts from the page
    * @param sAction String PV Page Action
    * @return String  JSP name to forward to
    */
    public String removeParents(String sAction)
    {
       try
       {
         HttpSession session = data.getRequest().getSession();
          VD_Bean vd = (VD_Bean)session.getAttribute(PVForm.SESSION_SELECT_VD);  //new VD_Bean();
          //get the selected parent info from teh request
          data.setVD(vd);
          String sParentCC = (String)data.getRequest().getParameter("selectedParentConceptCode");
          String sParentName = (String)data.getRequest().getParameter("selectedParentConceptName");
          String sParentDB = (String)data.getRequest().getParameter("selectedParentConceptDB");
          pvAction.doRemoveParent(sParentCC, sParentName, sParentDB, sAction, data);
          //make vd's system preferred name
          vd = data.getVD();
          Vector<EVS_Bean> vParentCon = vd.getReferenceConceptList();
          vd = (VD_Bean) data.getCurationServlet().getSystemName(vd, vParentCon);
          DataManager.setAttribute(session, PVForm.SESSION_SELECT_VD, vd); 
          //make the selected parent in hte session empty
          DataManager.setAttribute(session, "SelectedParentName", "");
          DataManager.setAttribute(session, "SelectedParentCC", "");
          DataManager.setAttribute(session, "SelectedParentDB", "");
          DataManager.setAttribute(session, "SelectedParentMetaSource", "");
       }
       catch (Exception e)
       {
         logger.error("ERROR - remove parents ", e);
       }
       //forward teh page according to vdPage 
       return "/PermissibleValue.jsp";                 
    }
 
    /**
     * called when parent is added to the page
     * @return String JSP name to forward to
     */
    @SuppressWarnings("unchecked")
    public String selectParents()
    {
      try
      {
        HttpSession session = data.getRequest().getSession();        
        //store the evs bean for the parent concepts in vector and in session.    
        VD_Bean vd = (VD_Bean)session.getAttribute(PVForm.SESSION_SELECT_VD);  //new VD_Bean();
        data.setVD(vd);
        //get the result vector from the session
        Vector<EVS_Bean> vRSel = (Vector)session.getAttribute("vACSearch");
        if (vRSel == null) vRSel = new Vector<EVS_Bean>();
        //get the array from teh hidden list
        String selRows[] = data.getRequest().getParameterValues("hiddenSelRow");
        if (selRows != null)
          pvAction.getEVSSelRowVector(vRSel, selRows, data);
 
        //make vd's system preferred name
        vd = data.getVD();
        Vector<EVS_Bean> vParentCon = vd.getReferenceConceptList();  
        vd = (VD_Bean) data.getCurationServlet().getSystemName(vd, vParentCon);
        vd.setVDNAME_CHANGED(true);
        DataManager.setAttribute(session, PVForm.SESSION_SELECT_VD, vd);
        //store the last page action in request
        data.getRequest().setAttribute("LastAction", "parSelected");
      }
      catch (Exception e)
      {
        logger.error("ERROR - " + e);
      }
      //forward teh page according to vdPage 
      return "/PermissibleValue.jsp";                 
    } // end
    
    /**
     * stores the non evs parent reference information in evs bean and to parent list.
     * reference document is matched like this with the evs bean adn stored in parents vector as a evs bean
     * setCONCEPT_IDENTIFIER as document type (VD REFERENCE)
     * setLONG_NAME as document name
     * setEVS_DATABASE as Non_EVS text 
     * setPREFERRED_DEFINITION as document text
     * setEVS_DEF_SOURCE as document url
     * 
     * @return String JSP name to forward to 
     */
    public String selectNonEVSParents()
    {
      try
      {
        HttpSession session = data.getRequest().getSession();   
        //document name  (concept long name)
        String sParName = (String)data.getRequest().getParameter("hiddenParentName");
        if(sParName == null) sParName = "";
        //document text  (concept definition)
        String sParDef = (String)data.getRequest().getParameter("hiddenParentCode");
        if(sParDef == null) sParDef = "";
        //document url  (concept defintion source)
        String sParDefSource = (String)data.getRequest().getParameter("hiddenParentDB");
        if(sParDefSource == null) sParDefSource = "";
        
        VD_Bean m_VD = (VD_Bean)session.getAttribute(PVForm.SESSION_SELECT_VD);  //new VD_Bean();
        data.setVD(m_VD);
        pvAction.doNonEVSReference(sParName, sParDef, sParDefSource, data);
 
        DataManager.setAttribute(session, PVForm.SESSION_SELECT_VD, data.getVD()); 
        //store the last page action in request
        data.getRequest().setAttribute("LastAction", "parSelected");
      }
      catch (RuntimeException e)
      {
        logger.error("ERROR - selectNonEVSParents", e);
      }
      //forward teh page according to vdPage 
      return "/PermissibleValue.jsp";                 
    } // end
    
    /** returns the selected VM to use when existing VMs are found while saving
    * @return VM_Bean object
    */
   @SuppressWarnings("unchecked")
    private VM_Bean getDuplicateVMUse()
    {
      try
     {
       HttpSession session = data.getRequest().getSession();
        Vector<VM_Bean> errVMs = (Vector<VM_Bean>)session.getAttribute("VMEditMsg");
        if (errVMs != null && errVMs.size() > 0)
        {
          String vmUse = data.getRequest().getParameter("rUse");
          int vmInd = -1;
          if (vmUse != null && !vmUse.equals(""))
          {
            vmUse = vmUse.substring(4);
            if (vmUse != null && !vmUse.equals(""))
              vmInd = new Integer(vmUse).intValue();
          }
          if (vmInd > -1 && errVMs.size() > vmInd)
          {
            VM_Bean vm = errVMs.elementAt(vmInd);
            data.setNewVM(vm);
            vm._alts = null;
            session.removeAttribute("VMEditMsg");
            return vm;
          }
        }
     }
     catch (Exception e)
     {
       logger.error("ERROR - getDuplicateVMUse", e);
     }
      return null;
    }
 
   /**
    * to put the back vm edits back in the list of permissible value and set the session attributes
    * @param vm VM_Bean object
    */
    public void putBackVMEdits(VM_Bean vm)
    {
        HttpSession session = data.getRequest().getSession(); 
        PV_Bean selPV = (PV_Bean)session.getAttribute(VMForm.SESSION_SELECT_PV);
        selPV.setPV_VM(vm);
        //refresh the vd bean
        VD_Bean vd = (VD_Bean)session.getAttribute(PVForm.SESSION_SELECT_VD);
        Vector<PV_Bean> vdpv = vd.getVD_PV_List();
        int pvInd = (Integer)session.getAttribute(PVForm.SESSION_PV_INDEX);
        vdpv.setElementAt(selPV, pvInd);
        vd.setVD_PV_List(vdpv);
        DataManager.setAttribute(session, PVForm.SESSION_SELECT_VD, vd);
        data.getRequest().setAttribute(PVForm.REQUEST_FOCUS_ELEMENT, "pv" + pvInd);
    }
    
    public String getViewVMId() {
 	   String vmIdseq = null;
 	   HttpSession session = data.getRequest().getSession();
 	   int pvInd = -1;
 	   String id = (String) data.getRequest().getParameter("id"); 
 	   String selPVInd = (String) data.getRequest().getParameter("viewPVInd"); //index
 	   if (selPVInd != null && !selPVInd.equals("")) {
 			selPVInd = selPVInd.substring(2);
 			if (selPVInd != null && !selPVInd.equals("")) {
 				if (selPVInd.equalsIgnoreCase("New"))
 					pvInd = -1;
 				else
 					pvInd = new Integer(selPVInd).intValue();
 			}
 		}
 	   	if (pvInd > -1) {
 			String viewVD = "viewVD" + id;
 	   		VD_Bean vd = (VD_Bean) session.getAttribute(viewVD);
 			Vector<PV_Bean> vVDPVList = vd.getVD_PV_List(); 
 			if (vVDPVList != null){
 				PV_Bean selectPV = (PV_Bean) vVDPVList.elementAt(pvInd);
 			    vmIdseq = selectPV.getPV_VM().getIDSEQ();
 			}    
 		}
 			return vmIdseq;
     }
    public String doViewPVActions(){
 	   String action = data.getRequest().getParameter("action");
 	   String id = data.getRequest().getParameter("id");
 	   String path = "";
 	   HttpSession session = data.getRequest().getSession();
 	   
 	   if (action != null){
 		   if (action.equals("sort")){
 			 GetACSearch serAC = new GetACSearch(data.getRequest(), data.getResponse(), data.getCurationServlet());
 	         String sField = (String)data.getRequest().getParameter("pvSortColumn");
 	         String viewVD = "viewVD" + id;
 	         VD_Bean m_VD = (VD_Bean) session.getAttribute(viewVD);
 	         serAC.getVDPVSortedRows(m_VD,sField,"view",id);  //call the method to sort pv attribute
 	         data.getRequest().setAttribute(PVForm.REQUEST_FOCUS_ELEMENT, "pv0"); 
 	         data.getRequest().setAttribute("viewVDId", id);
 	         String title = "CDE Curation View VD "+m_VD.getVD_LONG_NAME()+ " [" + m_VD.getVD_VD_ID() + "v" + m_VD.getVD_VERSION() +"]";
 	         data.getRequest().setAttribute("title", title);
 	         data.getRequest().setAttribute("publicID", m_VD.getVD_VD_ID());
 	         data.getRequest().setAttribute("version", m_VD.getVD_VERSION());
 	         data.getRequest().setAttribute("IncludeViewPage", "PermissibleValue.jsp"); 
 			 path = "/jsp/ViewPage.jsp" ;  
 		   }else if (action.equals("viewVM")){
 			 String vmIDSEQ = getViewVMId();
 		     path = "/NCICurationServlet?reqType=view&idseq=" +vmIDSEQ ;
 		   }
 	   }
 	   return path;
    }
 	
 } //end of teh class
