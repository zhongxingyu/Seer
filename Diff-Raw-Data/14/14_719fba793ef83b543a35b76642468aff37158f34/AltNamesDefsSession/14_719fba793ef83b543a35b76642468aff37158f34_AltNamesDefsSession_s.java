 // Copyright (c) 2006 ScenPro, Inc.
 
 // $Header: /cvsshare/content/cvsroot/cdecurate/src/gov/nih/nci/cadsr/cdecurate/ui/AltNamesDefsSession.java,v 1.39 2008-05-04 19:32:58 chickerura Exp $
 // $Name: not supported by cvs2svn $
 
 package gov.nih.nci.cadsr.cdecurate.ui;
 
 import java.io.Serializable;
 import java.sql.CallableStatement;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.Vector;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import gov.nih.nci.cadsr.cdecurate.database.ACTypes;
 import gov.nih.nci.cadsr.cdecurate.database.Alternates;
 import gov.nih.nci.cadsr.cdecurate.database.DBAccess;
 import gov.nih.nci.cadsr.cdecurate.database.TreeNodeAlt;
 import gov.nih.nci.cadsr.cdecurate.database.TreeNodeCS;
 import gov.nih.nci.cadsr.cdecurate.tool.AC_Bean;
 import gov.nih.nci.cadsr.cdecurate.tool.DEC_Bean;
 import gov.nih.nci.cadsr.cdecurate.tool.DE_Bean;
 import gov.nih.nci.cadsr.cdecurate.tool.CurationServlet;
 import gov.nih.nci.cadsr.cdecurate.tool.PV_Bean;
 import gov.nih.nci.cadsr.cdecurate.tool.VD_Bean;
 import gov.nih.nci.cadsr.cdecurate.tool.VM_Bean;
 import gov.nih.nci.cadsr.cdecurate.util.DataManager;
 import gov.nih.nci.cadsr.cdecurate.util.ToolException;
 import gov.nih.nci.cadsr.cdecurate.util.Tree;
 import gov.nih.nci.cadsr.cdecurate.util.TreeNode;
 import gov.nih.nci.cadsr.common.Constants;
 
 /**
  * This class maps and manges the session data needed for processing Alternate Names and Definitions.
  * 
  * @author lhebel
  */
 public class AltNamesDefsSession implements Serializable
 {
     private static final String _beanDE = "m_DE";
 
     private static final String _beanDEC = "m_DEC";
 
     private static final String _beanVD = "m_VD";
 
     private static final String _beanVM = "selectVM";
 
     private static final String _beanBlock = "vBEResult";
 
     private static final String _sessName = AltNamesDefsSession.class.getName();
 
     private static final long serialVersionUID = 1092782526671820593L;
 
     public static final String _beanVMID = "vmID";
 
     public static final String _searchEVS = "searchEVS";
 
     public static final String _searchVD = "ValueDomain";
 
     public static final String _searchDEC = "DataElementConcept";
 
     public static final String _searchDE = "DataElement";
 
     public static final String _searchPVVM = "ValueMeaning";
 
     public static final String _searchVM = "ValueMeaningEdit";
 
     public static final String _showClear = "showClear";
 
     public static final String _sortName = "Name";
 
     public static final String _newPrefix = "$";
 
     public String _jsp;
 
     public String _viewJsp;
 
     public String _cacheSort;
 
     public String _cacheTitle;
 
     public Tree _cacheCSI;
 
     public String[] _cacheAltTypes;
 
     public String[] _cacheDefTypes;
 
     public String[] _cacheLangs;
 
     public String _cacheLangDefault;
 
     public String[] _cacheContextNames;
 
     public String[] _cacheContextIds;
 
     private AC_Bean[] _beans;
 
     private Alternates[] _alts;
 
     private String[] _acIdseq;
 
     private String[] _conteIdseq;
 
     private String[] _conteName;
 
     private String _sessType;
 
     private Alternates _editAlt;
 
     private int _newIdseq;
 
     private boolean _dbClearNamesDefs;
 
     private boolean _enableClear;
 
     private boolean _blockVersion;
 
     private boolean _showMC;
 
     /**
      * Constructor
      * 
      * @param bean_
      *        the AC bean object
      * @param acIdseq_
      *        the AC database id of interest
      * @param conteIdseq_
      *        the companion Context ID
      * @param conteName_
      *        the companion Context Name
      * @param sessType_
      *        the session type, AltNamesDefsSession._searchDE, _searchDEC, _searchVD, _searchPVVM
      */
     public AltNamesDefsSession(AC_Bean bean_, String acIdseq_, String conteIdseq_, String conteName_, String sessType_)
     {
         cleanBuffers();
         _beans = new AC_Bean[1];
         _beans[0] = bean_;
         _acIdseq = new String[1];
         _acIdseq[0] = (acIdseq_ == null || acIdseq_.length() == 0) ? null : acIdseq_;
         _conteIdseq = new String[1];
         _conteIdseq[0] = (conteIdseq_ == null || conteIdseq_.length() == 0) ? null : conteIdseq_;
         _conteName = new String[1];
         _conteName[0] = (conteName_ == null) ? "" : conteName_;
         _sessType = sessType_;
         _dbClearNamesDefs = false;
         _enableClear = true;
         _showMC = true;
     }
 
     /**
      * Constructor
      * 
      * @param bean_
      *        the AC bean object
      * @param acIdseq_
      *        the AC database id list of interest
      * @param conteIdseq_
      *        the companion Context ID list
      * @param conteName_
      *        the companion Context Name list
      * @param sessType_
      *        the session type, AltNamesDefsSession._searchDE, _searchDEC, _searchVD, _searchPVVM
      */
     public AltNamesDefsSession(AC_Bean bean_, String[] acIdseq_, String[] conteIdseq_, String[] conteName_, String sessType_)
     {
         cleanBuffers();
         _beans = new AC_Bean[1];
         _beans[0] = bean_;
         _acIdseq = acIdseq_;
         _conteIdseq = conteIdseq_;
         _conteName = conteName_;
         _sessType = sessType_;
         _dbClearNamesDefs = false;
         _enableClear = true;
         _showMC = true;
     }
 
     /**
      * Constructor
      * 
      * @param beans_
      *        the AC bean object list
      * @param acIdseq_
      *        the AC database id list of interest
      * @param conteIdseq_
      *        the companion Context ID list
      * @param conteName_
      *        the companion Context Name list
      * @param sessType_
      *        the session type, AltNamesDefsSession._searchDE, _searchDEC, _searchVD, _searchPVVM
      */
     public AltNamesDefsSession(AC_Bean[] beans_, String[] acIdseq_, String[] conteIdseq_, String[] conteName_, String sessType_)
     {
         cleanBuffers();
         _beans = beans_;
         _acIdseq = acIdseq_;
         _conteIdseq = conteIdseq_;
         _conteName = conteName_;
         _sessType = sessType_;
         _dbClearNamesDefs = false;
         _enableClear = true;
         _showMC = true;
     }
 
     public AltNamesDefsSession(Alternates[] alts) {
     	_alts = alts;
 	}
 
 	//GF30796
     public Alternates[] getAlternates() {
 		return _alts;
 	}
 
 	/**
      * Clear the session edit buffer
      */
     public void clearEdit(int inst_)
     {
         _editAlt = new Alternates();
         _editAlt.setAltIdseq(newIdseq());
         _editAlt.setInstance(inst_);
         _editAlt.setConteIdseq(_conteIdseq[0]);
         _editAlt.setConteName(_conteName[0]);
         _editAlt.setACIdseq(_acIdseq[0]);
     }
 
     /**
      * Get the CSI Tree from the edit buffer
      * 
      * @return the tree
      */
     public Tree getEditCSITree()
     {
         return _editAlt.getCSITree();
     }
 
     /**
      * Initialize the edit buffer
      * 
      * @param obj_
      *        the source object
      */
     public void editInit(Alternates obj_)
     {
         _editAlt = obj_.dupl();
     }
 
     /**
      * Get the edit buffer object
      * 
      * @return the edit buffer
      */
     public Alternates getEdit()
     {
         return _editAlt;
     }
 
     /**
      * Update the edit buffer with user selections.
      * 
      * @param name_
      *        the name/definition
      * @param type_
      *        the type
      * @param lang_
      *        the language
      * @param conteID_
      *        the context ID
      * @param conteName_
      *        the context name
      */
     public void editUpdates(String name_, String type_, String lang_, String conteID_, String conteName_)
     {
         _editAlt.setName(name_);
         _editAlt.setType(type_);
         _editAlt.setLanguage(lang_);
         _editAlt.setConteIdseq(conteID_);
         _editAlt.setConteName(conteName_);
     }
 
     /**
      * Clean all session buffers
      */
     public void cleanBuffers()
     {
         _editAlt = new Alternates();
         _cacheTitle = null;
         _cacheCSI = null;
         _cacheAltTypes = null;
         _cacheLangs = null;
         _cacheSort = AltNamesDefsServlet._sortName;
     }
 
     /**
      * Calculate a new object temporary database id
      * 
      * @return a session unique temporary identifier
      */
     public String newIdseq()
     {
         --_newIdseq;
         return _newPrefix + String.valueOf(_newIdseq);
     }
 
     /**
      * Get the session name. There are multiple sessions to allow the user to begin work on a DE and create a DEC prior to saving the new DE.
      * 
      * @param type_
      *        the session type, AltNamesDefsSession._searchDE, _searchDEC, _searchVD, _searchPVVM
      * @return the session name
      */
     private static String getSessName(String type_)
     {
         return _sessName + "." + type_;
     }
 
     /**
      * Get the data buffer for a block edit.
      * 
      * @param session_
      * @param sessName_
      * @param acBlock_
      * @return the block edit alt names/defs buffer
      * @throws Exception
      */
     private static AltNamesDefsSession getSessionDataBlockEdit(HttpSession session_, String sessName_, Vector<AC_Bean> acBlock_) throws Exception
     {
         AltNamesDefsSession altSess = null;
 
         // Get the block edit selections.
         String[] acIdseq = new String[acBlock_.size()];
         String[] conteIdseq = new String[acIdseq.length];
         String[] conteName = new String[acIdseq.length];
         AC_Bean[] beans = new AC_Bean[acIdseq.length];
         for (int i = 0; i < acIdseq.length; ++i)
         {
             beans[i] = acBlock_.get(i);
             acIdseq[i] = new String(beans[i].getIDSEQ());
             conteIdseq[i] = new String(beans[i].getContextIDSEQ());
             conteName[i] = new String(beans[i].getContextName());
         }
 
         // If no block edit buffer exists, create one.
         altSess = (AltNamesDefsSession) session_.getAttribute(sessName_);
         if (altSess == null)
         {
             altSess = new AltNamesDefsSession(beans, acIdseq, conteIdseq, conteName, _beanBlock);
             DataManager.setAttribute(session_, sessName_, altSess);
             return altSess;
         }
 
         // The existing block edit buffer must be validated to ensure we haven't changed the AC
         // list.
         boolean flag = false;
         for (int i = 0; i < acIdseq.length; ++i)
         {
             if (acIdseq[i].equals(altSess._acIdseq[i]) == false)
             {
                 flag = true;
                 break;
             }
         }
 
         // The buffer isn't correct for the list so create a new one.
         if (flag)
         {
             altSess = new AltNamesDefsSession(beans, acIdseq, conteIdseq, conteName, _beanBlock);
             DataManager.setAttribute(session_, sessName_, altSess);
         }
         return altSess;
     }
 
     /**
      * Get the data buffer for a VM
      * 
      * @param session_
      * @param req_
      * @return the alt name/def buffer
      * @throws Exception
      */
     private static AltNamesDefsSession getSessionDataPVVM(HttpSession session_, HttpServletRequest req_) throws Exception
     {
         // We identify the VM for the VD by an index value.
         AltNamesDefsSession altSess = null;
         String vmID = req_.getParameter(_beanVMID);
         if (vmID == null)
         {
             throw new Exception("Missing required request parameter \"" + _beanVMID + "\".");
         }
 
         // Have to keep this around.
         req_.setAttribute(_beanVMID, vmID);
 
         // Be sure we can get the VM and the class types are right.
         AC_Bean temp = (AC_Bean) session_.getAttribute(_beanVD);
         if (temp != null && temp instanceof VD_Bean)
         {
             VD_Bean vd = (VD_Bean) temp;
             Vector<PV_Bean> pvs = vd.getVD_PV_List();
             int vmid = Integer.parseInt(vmID);
             if (pvs.size() == 0 || vmid < 0 || pvs.size() <= vmid)
             {
                 throw new Exception("VM_Bean can not be found.");
             }
 
             // Get the VM and it's data buffer.
             PV_Bean pv = pvs.get(vmid);
             VM_Bean vm = pv.getPV_VM();
             altSess = vm.getAlternates();
 
             // The VM hasn't been used before so create a new buffer.
             if (altSess == null)
             {
                 altSess = new AltNamesDefsSession(vm, vm.getIDSEQ(), vd.getContextIDSEQ(), vd.getContextName(), _searchPVVM);
                 vm.setAlternates(altSess);
             }
 
             // From here always show the manually curated definitions.
             altSess._showMC = true;
         }
         return altSess;
     }
 
     /**
      * Get the data buffer for AC's except a VM
      * 
      * @param session_
      * @param launch_
      * @return the alt name/def buffer
      * @throws Exception
      */
     public static AltNamesDefsSession getSessionDataAC(HttpSession session_, String launch_) throws Exception
     {
         // VM Edit is special, don't show the manually curated definition.
         boolean showMC = true;
 
         // Determine the bean which holds the data.
         AltNamesDefsSession altSess = null;
         String beanName = null;
         if (launch_.equals(_searchDE))
             beanName = _beanDE;
         else if (launch_.equals(_searchDEC))
             beanName = _beanDEC;
         else if (launch_.equals(_searchVD))
             beanName = _beanVD;
         else if (launch_.equals(_searchVM))
         {
             beanName = _beanVM;
             showMC = false;
         }
 
         // Get the AC bean.
         AC_Bean ac = (AC_Bean) session_.getAttribute(beanName);
 //        if (ac == null)
 //            throw new Exception("Missing session Bean [" + beanName + "].");
         if(ac != null) {	//GF30796
         
         // If it hasn't been used before, create a new buffer.
         altSess = ac.getAlternates();
         if (altSess == null)
         {
             altSess = new AltNamesDefsSession(ac, ac.getIDSEQ(), ac.getContextIDSEQ(), ac.getContextName(), launch_);
             ac.setAlternates(altSess);
 
             // If we are creating a buffer for a new AC or an AC that "wants" to be new, set the Alternates list to
             // empty. Do NOT set the list to null as that means the database must be read to initialize it.
             if (ac.isNewAC())
             {
                 altSess._alts = new Alternates[0];
                 altSess._acIdseq = new String[1];
             }
         }
 
         //GF30798
 //        if(ac != null && ac.getAlternates() != null && ac.getAlternates()._alts != null && ac.getAlternates()._alts[0] != null) {
 //        	String altDef = ac.getAlternates()._alts[0].getName();
 //        	session_.setAttribute(Constants.FINAL_ALT_DEF_STRING, altDef);
 //        	System.out.println("AltNamesDefsSession: getSessionDataAC() altDef = >>>" + altDef + "<<<");
 //        }
         
         // Need to reset visible manually curated definitions.
         altSess._showMC = showMC;
         }
 
         return altSess;
     }
 
     /**
      * Load the session title.
      * 
      * @param db_
      *        database access object
      * @throws ToolException
      */
     public void loadTitle(DBAccess db_) throws ToolException
     {
         // If the title has not been determined, build it.
         if (_cacheTitle == null)
         {
             // A single AC edit so display it's name, etc.
             if (_acIdseq.length == 1)
                 _cacheTitle = db_.getACtitle(_sessType, _acIdseq[0]);
 
             // Multiple AC edit (block edit) so keep it simple.
             else
                 _cacheTitle = "Block Edit";
         }
     }
 
     /**
      * Load the Alternates and sort the results.
      * 
      * @param db_
      *        database access object
      * @param sort_
      * @throws ToolException
      */
     public void loadAlternates(DBAccess db_, String sort_) throws ToolException
     {
         boolean sortBy = (sort_ == null || sort_.equals(_sortName));
         if (_alts == null)
         {
             _alts = db_.getAlternates(_acIdseq, sortBy, _showMC);
             if (_acIdseq.length > 1)
                 sortBy(_alts, sortBy);
             for (int i = 0; i < _alts.length; ++i)
             {
                 if (getContextName(_alts[i].getConteIdseq()) == null)
                 {
                     _alts[i].makeReadOnly();
                 }
             }
         }
         else if (!sort_.equals(_cacheSort))
         {
             sortBy(_alts, sortBy);
         }
     }
 
     /**
      * Sort the "View by Name/Definition" using Name/Definition, Text and Type.
      * 
      * @param alts_
      *        the Alternates list
      * @param flag_
      *        true to sort by Name and false to sort by Type
      */
     private static void sortBy(Alternates[] alts_, boolean flag_)
     {
         // Determine number of Names and number of Definitions.
         int dCnt;
         int nCnt = 0;
         for (int i = 0; i < alts_.length; ++i)
         {
             if (alts_[i].isName())
                 ++nCnt;
         }
 
         // Create temporary buffers to separate Names and Definitions.
         Alternates[] altsName = new Alternates[nCnt];
         Alternates[] altsDef = new Alternates[alts_.length - nCnt];
 
         // Build Name and Deifnition buffer.
         nCnt = 0;
         dCnt = 0;
         for (int i = 0; i < alts_.length; ++i)
         {
             if (alts_[i].isName())
             {
                 altsName[nCnt] = alts_[i];
                 ++nCnt;
             }
             else
             {
                 altsDef[dCnt] = alts_[i];
                 ++dCnt;
             }
         }
 
         // Sort the buffers by Name (true) or Type (false)
         Alternates[] tempName;
         Alternates[] tempDef;
         if (flag_)
         {
             tempName = sortByName(altsName);
             tempDef = sortByName(altsDef);
         }
         else
         {
             tempName = sortByType(altsName);
             tempDef = sortByType(altsDef);
         }
 
         // Move the sorted lists back into the primary buffer. We didn't change the number of entries
         // in the buffer, just arranged them as specified.
         System.arraycopy(tempName, 0, alts_, 0, tempName.length);
         System.arraycopy(tempDef, 0, alts_, tempName.length, tempDef.length);
     }
 
     /**
      * Sort the specified buffer by Name. Sorting Definitions by "Name" uses the text of the Definition as expected. All sorts are case insensitive.
      * 
      * @param alts_
      *        the Name or Definition buffer.
      * @return the sorted list
      */
     private static Alternates[] sortByName(Alternates[] alts_)
     {
         // Perform a binary sort. The lists are typically very small and this algorythm
         // can also accommodate large lists.
         Alternates[] temp = new Alternates[alts_.length];
         if (alts_.length == 0)
             return temp;
         temp[0] = alts_[0];
         for (int top = 1; top < alts_.length; ++top)
         {
             int max = top;
             int min = 0;
             int pos = 0;
             while (true)
             {
                 pos = (max + min) / 2;
                 int compare = alts_[top].getName().compareToIgnoreCase(temp[pos].getName());
                 if (compare == 0)
                     break;
                 else if (compare < 0)
                 {
                     if (max == pos)
                         break;
                     max = pos;
                 }
                 else
                 {
                     if (min == pos)
                     {
                         ++pos;
                         break;
                     }
                     min = pos;
                 }
             }
             System.arraycopy(temp, pos, temp, pos + 1, top - pos);
             temp[pos] = alts_[top];
         }
         return temp;
     }
 
     /**
      * Sort the specified buffer by Type. All sorts are case insensitive.
      * 
      * @param alts_
      *        the Name or Definition buffer.
      * @return the sorted list
      */
     private static Alternates[] sortByType(Alternates[] alts_)
     {
         // Perform a binary sort. The lists are typically very small and this algorythm
         // can also accommodate large lists.
         Alternates[] temp = new Alternates[alts_.length];
         if (alts_.length == 0)
             return temp;
         temp[0] = alts_[0];
         for (int top = 1; top < alts_.length; ++top)
         {
             int max = top;
             int min = 0;
             int pos = 0;
             while (true)
             {
                 pos = (max + min) / 2;
                 int compare = alts_[top].getType().compareToIgnoreCase(temp[pos].getType());
                 if (compare == 0)
                     break;
                 else if (compare < 0)
                 {
                     if (max == pos)
                         break;
                     max = pos;
                 }
                 else
                 {
                     if (min == pos)
                     {
                         ++pos;
                         break;
                     }
                     min = pos;
                 }
             }
             System.arraycopy(temp, pos, temp, pos + 1, top - pos);
             temp[pos] = alts_[top];
         }
         return temp;
     }
 
     /**
      * Get the session data for this request.
      * 
      * @param req_
      *        the user HTTP request object
      * @return the session data buffer
      * @throws Exception
      */
     public static AltNamesDefsSession getSessionData(HttpServletRequest req_) throws Exception
     {
         // Ok this logic has nothing to do with EVS. The current field name used by
         // the curation tool is referenced for consistency.
         String launch = req_.getParameter(_searchEVS);
 
         // This would be a problem, the environment is not right for this request.
         if (launch == null)
         {
             throw new Exception("Unknown origination page.");
         }
         AltNamesDefsSession altSess = getAlternates(req_, launch);
         return altSess;
     }
 
     /**
      * Get the session data for this request.
      * 
      * @param req_
      *        the user HTTP request object
      * @param launch_
      * @return the session data buffer
      * @throws Exception
      */
     public static AltNamesDefsSession getAlternates(HttpServletRequest req_, String launch_) throws Exception
     {
         // This would be a problem, the environment is not right for this request.
         if (launch_ == null)
         {
             throw new Exception("Unknown launch.");
         }
         HttpSession session = req_.getSession();
         AltNamesDefsSession altSess = null;
         while (true)
         {
             // For block edit we will keep a separate session attribute. WARNING this first getAttribute
             // is for the Curation Tool block edit buffer and NOT the Alt Name/Def block edit buffer.
             String sessName = getSessName(_beanBlock);
             @SuppressWarnings("unchecked")
             Vector<AC_Bean> acBlock = (Vector) session.getAttribute(_beanBlock);
             if (acBlock != null && acBlock.size() > 0)
             {
                 altSess = getSessionDataBlockEdit(session, sessName, acBlock);
                 break;
             }
 
             // Remove any old Block edit buffer.
             session.removeAttribute(sessName);
 
             // Value Meanings are different than other AC's.
             if (launch_.equals(_searchPVVM))
             {
                 altSess = getSessionDataPVVM(session, req_);
                 break;
             }
 
             // Get the data buffer for this AC.
             altSess = getSessionDataAC(session, launch_);
             break;
         }
 //        if (altSess == null)
 //            throw new Exception("Unable to find or create a data buffer.");
       if (altSess != null) {	//GF30796
 
         // Update the display flag in the alternates.
         altSess.resetShowMC();
 
         // Set the request data for when the page is written.
         req_.setAttribute(AltNamesDefsServlet._reqIdseq, altSess._acIdseq[0]);
         req_.setAttribute(_searchEVS, launch_);
         req_.setAttribute(_showClear, (altSess._enableClear) ? "Y" : "N");
 
         //GF30798
 //		if(altSess != null && altSess._alts != null && altSess._alts[0] != null) {
 			//GF30796 need to check if it is already exists, if new add it
 //			session.setAttribute(Constants.FINAL_ALT_DEF_STRING, altDef);
 //		}
 
         
         // Must do any initialization that needs the session.
         altSess.setContexts(session);
       	} 
       
         return altSess;
     }
 
     /**
      * Reset the display flag in the alternates to match the session buffer setting.
      */
     private void resetShowMC()
     {
         if (_alts != null)
         {
             for (Alternates temp : _alts)
             {
                 temp.display(_showMC);
             }
         }
     }
 
     /**
      * Save the session data to the database.
      * 
      * @param conn_
      *        the database connection
      * @param idseq_
      *        the subject AC, for an Edit this wouldn't change, for an Add/New this is now a valid idseq
      * @param conteIdseq_
      *        the companion Context idseq
      * @throws SQLException
      */
     public void save(Connection conn_, String idseq_, String conteIdseq_) throws SQLException
     {
         // Save all the alternates
         DBAccess db = new DBAccess(conn_);
 
         // In the context of a Create Using, there may have been Alts copied from the source AC and
         // they should be removed because the other logic is not reliable.
         if (_dbClearNamesDefs)
             db.deleteAlternates(idseq_);
 
         // If the list has been wiped, nothing left to do.
         if (_alts == null)
             return;
 
         // Save every Alternate
         int total = _alts.length;
         for (int i = 0; i < _alts.length; ++i)
         {
             Alternates alt = _alts[i];
             if (alt.getAcIdseq() == null)
                 alt.setACIdseq(idseq_);
             if (alt.getConteIdseq() == null)
                 alt.setConteIdseq(conteIdseq_);
 
             // Data Elements may need an automatic USED_BY type
             ACTypes acType = (_beans != null && _beans.length > 0) ? _beans[0].getType() : ACTypes.UNKNOWN;
             if (acType == ACTypes.DataElement && alt.getConteIdseq().equals(conteIdseq_) == false)
                 db.saveUsedBy(alt);
             if (db.save(alt) == false)
             {
                 _alts[i] = null;
                 --total;
             }
         }
 
         // If any alternates are deleted then drop them from the list.
         if (total != _alts.length)
         {
             Alternates[] temp = new Alternates[total];
             total = 0;
             for (int i = 0; i < _alts.length; ++i)
             {
                 if (_alts[i] != null)
                 {
                     temp[total] = _alts[i];
                     ++total;
                 }
             }
             _alts = temp;
         }
     }
 
     public static void blockSave(CurationServlet serv_, HttpSession session_) throws SQLException
     {
         try
         {
             // Get the session buffer.
             String sessName = getSessName(_beanBlock);
             AltNamesDefsSession sess = (AltNamesDefsSession) session_.getAttribute(sessName);
 
             // Only if we have a block edit buffer.
             if (sess == null)
             {
                 return;
             }
 
             // Open a database connection.
             DBAccess db = new DBAccess(serv_.getConn());
 
             // For a "normal" block edit, the changed records are written back to their original Alternate.
             if (sess._blockVersion == false)
             {
                 // Apply the alternate objects to the appropriate AC
                 for (int i = 0; i < sess._alts.length; ++i)
                 {
                     // Data Elements may need an automatic USED_BY type
                     Alternates alt = sess._alts[i];
 
                     // This needs to be done but can't given the way block edit is currently coded!!
                     // if (acType == ACTypes.DataElement && alt.getConteIdseq().equals(sess._conteIdseq) == false)
                     // db.saveUsedBy(alt);
                     db.save(alt, sess._acIdseq, sess._conteIdseq);
                 }
             }
 
             // For a "versioned" block edit, changes are added to a single AC and new Alternates are
             // added to everything in the list.
             else
             {
                 // The new AC's must be cleaned of any Alternates prior to saving the buffer.
                 for (int i = 0; i < sess._acIdseq.length; ++i)
                 {
                     String newIdseq = sess._beans[i].getIDSEQ();
                     if (!sess._acIdseq[i].equals(newIdseq))
                     {
                         db.deleteAlternates(newIdseq);
                     }
                 }
 
                 // Process each Alternate
                 for (int i = 0; i < sess._alts.length; ++i)
                 {
                     // Data Elements may need an automatic USED_BY type
                     Alternates alt = sess._alts[i];
 
                     // This needs to be done but can't given the way block edit is currently coded!!
                     // if (acType == ACTypes.DataElement && alt.getConteIdseq().equals(sess._conteIdseq) == false)
                     // db.saveUsedBy(alt);
                     db.save(alt, sess._beans, sess._acIdseq, sess._conteIdseq);
                 }
             }
             session_.removeAttribute(sessName);
         }
         catch (SQLException ex)
         {
             throw ex;
         }
     }
 
     /**
      * Create and load a data buffer marking everything as new, i.e. "create using". This will discard any existing data buffer in the AC Bean and replace it
      * with a new buffer. The AC IDSEQ is cleared, the Context IDSEQ is retained.
      * 
      * @param serv_
      * @param session_
      * @param ac_
      * @throws Exception
      */
     public static void loadAsNew(CurationServlet serv_, HttpSession session_, AC_Bean ac_) throws Exception
     {
         try
         {
            AltNamesDefsSession.loadAsNew(serv_.getConn(), ac_);
         }
         catch (Exception ex)
         {
             throw ex;
         }
      }
 
     /**
      * Create and load a data buffer marking everything as new, i.e. "create using". This will discard any existing data buffer in the AC Bean and replace it
      * with a new buffer. The AC IDSEQ is cleared, the Context IDSEQ is retained.
      * 
      * @param conn_
      * @param ac_
      * @throws Exception
      */
     private static void loadAsNew(Connection conn_, AC_Bean ac_) throws Exception
     {
         // Determine the type of buffer.
         String launch = null;
         if (ac_ instanceof DE_Bean)
             launch = _searchDE;
         else if (ac_ instanceof DEC_Bean)
             launch = _searchDEC;
         else if (ac_ instanceof VD_Bean)
             launch = _searchVD;
         else if (ac_ instanceof VM_Bean)
             launch = _searchPVVM;
 
         // Create the new data buffer
         AltNamesDefsSession buffer = new AltNamesDefsSession(ac_, null, ac_.getContextIDSEQ(), ac_.getContextName(), launch);
         ac_.setAlternates(buffer);
         buffer._dbClearNamesDefs = true;
         buffer._enableClear = false;
 
         // Load the data buffer with existing data marking everything as "new"
         DBAccess db = new DBAccess(conn_);
         String[] acIdseq = new String[1];
         acIdseq[0] = ac_.getIDSEQ();
         try
         {
             buffer.loadTitle(db);
             buffer._alts = db.getAlternates(acIdseq, true, buffer._showMC);
 
             // Make all the alternates "new"
             for (int i = 0; i < buffer._alts.length; ++i)
             {
                 buffer._alts[i].markNew(buffer.newIdseq());
                 buffer._alts[i].setACIdseq(null);
             }
         }
         catch (ToolException ex)
         {
             throw new Exception(ex.toString());
         }
     }
 
     /**
      * Same as loadAsNew(...) but for Block Edit
      * 
      * @param serv_
      * @param session_
      * @param ac_
      * @throws Exception
      */
     public static void loadAsNew(CurationServlet serv_, HttpSession session_, Vector<AC_Bean> ac_) throws Exception
     {
         // Be sure there's something to do.
         if (ac_ == null || ac_.size() == 0)
             return;
         AltNamesDefsSession buffer = null;
         try
         {
             // Only create the buffer if it doesn't exist.
             String sessName = getSessName(_beanBlock);
             buffer = (AltNamesDefsSession) session_.getAttribute(sessName);
             if (buffer == null)
             {
                 buffer = getSessionDataBlockEdit(session_, sessName, ac_);
 
                 // Load the data
                 DBAccess db = new DBAccess(serv_.getConn());
                 try
                 {
                     buffer._alts = db.getAlternates(buffer._acIdseq, true, buffer._showMC);
                 }
                 catch (ToolException ex)
                 {
                     throw new Exception(ex.toString());
                 }
             }
         }
         catch (Exception ex)
         {
             throw ex;
         }
         // This is only done for a Block Edit version.
         buffer._blockVersion = true;
     }
 
     /**
      * Set the Context cache.
      * 
      * @param sess_
      *        the session
      */
     private void setContexts(HttpSession sess_)
     {
         // Only need to do this once.
         if (_cacheContextNames != null)
             return;
 
         // Copy the Vector - an array is much more efficient for such a short list.
         Vector vContext = (Vector) sess_.getAttribute("vWriteContextDE");
         Vector vContextID = (Vector) sess_.getAttribute("vWriteContextDE_ID");
        _cacheContextNames = new String[vContext.size()];
        _cacheContextIds = new String[_cacheContextNames.length];
        for (int i = 0; i < _cacheContextNames.length; ++i)
        {
            _cacheContextNames[i] = (String) vContext.get(i);
            _cacheContextIds[i] = (String) vContextID.get(i);
         }
     }
 
     /**
      * Get the Context name for this id
      * 
      * @param id_
      *        the Context idseq
      * @return the Context name
      */
     public String getContextName(String id_)
     {
         // It's typically a very short list.
         for (int i = 0; i < _cacheContextIds.length; ++i)
         {
             if (id_.equals(_cacheContextIds[i]))
                 return _cacheContextNames[i];
         }
         return null;
     }
 
     /**
      * Find an Alternate in the session data with a matching idseq
      * 
      * @param idseq_
      *        the desired idseq
      * @return -1 if not found, otherwise the index
      */
     private int findAltWithIdseq(String idseq_)
     {
         if (_alts == null || _alts.length == 0)
             return -1;
         int pos;
         for (pos = 0; pos < _alts.length; ++pos)
         {
             if (_alts[pos].isIdseq(idseq_))
             {
                 break;
             }
         }
         if (pos == _alts.length)
             return -1;
         return pos;
     }
 
     /**
      * Find an Alternate in the session data with a matching idseq
      * 
      * @param idseq_
      *        the desired idseq
      * @return the Alternate or null if not found
      */
     public Alternates getAltWithIdseq(String idseq_)
     {
         int pos = findAltWithIdseq(idseq_);
         return (pos < 0) ? null : _alts[pos];
     }
 
     /**
      * Find an Alternate in the session data with a matching type. Only the first one is found if more than one exists.
      * 
      * @param instance_
      *        the alternate instance, either _INSTANCENAME or _INSTANCEDEF
      * @param type_
      *        the type as defined in the corresponding LOV database table
      * @return the alternate object.
      */
     public Alternates findAltWithType(int instance_, String type_)
     {
         if (_alts == null || _alts.length == 0)
             return null;
         int pos;
         for (pos = 0; pos < _alts.length; ++pos)
         {
             if (_alts[pos].getInstance() == instance_ && _alts[pos].getType().equals(type_))
             {
                 break;
             }
         }
         if (pos == _alts.length)
             return null;
         return _alts[pos];
     }
 
     /**
      * Update the Alternates list in the session with the given object.
      * 
      * @param alt_
      *        the new/updated alternate
      * @param sort_
      *        the sort order, see sortBy()
      */
     public void updateAlternatesList(Alternates alt_, boolean sort_)
     {
         int pos = findAltWithIdseq(alt_.getAltIdseq());
         if (pos < 0)
         {
             // This is a new one NOT an edit of an existing entry.
             pos = _alts.length;
             Alternates[] temp = new Alternates[pos + 1];
             System.arraycopy(_alts, 0, temp, 0, pos);
             _alts = temp;
         }
 
         // Save the edit buffer to the internal buffer.
         alt_.display(_showMC);
         _alts[pos] = alt_;
 
         // Sort it into the list
         sortBy(_alts, sort_);
     }
 
     /**
      * Check the state of the session caches
      * 
      * @param db_
      *        the database access object if needed
      * @throws ToolException
      */
     public void checkCaches(DBAccess db_) throws ToolException
     {
         if (_cacheCSI == null)
         {
             _cacheCSI = db_.getCSI(_cacheContextIds);
         }
         if (_cacheAltTypes == null)
             _cacheAltTypes = db_.getDesignationTypes();
 
         // The _showMC can change because some session buffers are used from different
         // execution paths. One where the manually curated definitions are visible and one
         // where they are invisible. Fortunately the list is short and loads quickly.
         // if (_cacheDefTypes == null)
         _cacheDefTypes = db_.getDefinitionTypes(_showMC);
         if (_cacheLangs == null)
         {
             _cacheLangs = db_.getLangs();
             _cacheLangDefault = db_.getDefaultLanguage();
         }
     }
 
     /**
      * Return the Alternates in HTML format.
      * 
      * @return the Alternates.toHTML concatenated output.
      */
     public String getAltHTML()
     {
         int flag = -1;
         String attr = "";
         for (Alternates temp : _alts)
         {
             if (flag != temp.getInstance())
             {
                 flag = temp.getInstance();
                 attr += temp.getHtmlTitle();
             }
             attr += temp.toHTML();
         }
         return attr;
     }
 
     /**
      * Return the Alternates in HTML format grouped by CS/CSI
      * 
      * @param formats_
      *        the HTML CS/CSI/Alternate "tree"
      * @return
      */
     public String getAltGroupByCSI(String[] formats_)
     {
         String attr = "";
 
         // Get the Alternate Names and Definitions for the AC.
         Tree root = new Tree(new TreeNode("root", null, false));
         for (Alternates alt : _alts)
         {
             // This gets a little tricky. "altRoot" is the original internal buffer data, "temp" is the tree containing
             // only this orignal buffer data with
             // the Alternates add to each leaf, "root" is the composite page Tree. This is all necessary to ensure the
             // Alternates appear on every
             // leaf of the tree for which they are associated. This requires the data to be duplicated during this
             // process. It is released once the
             // response is sent back to the user.
             Tree temp = new Tree(new TreeNode("root", null, false));
             Tree altRoot = alt.getCSITree();
             if (altRoot.isEmpty())
             {
                 temp.addChild(new TreeNodeCS("(unclassified)", "(unclassified)", "Unclassified Alternate Names and Definitions.", null, null, false));
             }
             else
             {
                 temp = altRoot.dupl();
             }
             temp.addLeaf(new TreeNodeAlt(alt, ""));
             root.merge(temp);
         }
 
         // Format display
         attr = root.toHTML(formats_);
         return attr;
     }
 
     /**
      * Delete an alternate from the list. That means mark it for delete if read from the database and actually remove it from the internal buffer if it hasn't
      * been written to the database.
      * 
      * @param idseq_
      *        the IDSEQ of the Alternate
      */
     public void deleteAlt(String idseq_)
     {
         // Find the target
         Alternates[] alts = _alts;
         int pos = findAltWithIdseq(idseq_);
         Alternates del = alts[pos];
 
         // If it's new just get rid of it, nothing to do to the database.
         if (del.isNew())
         {
             Alternates[] temp = new Alternates[alts.length - 1];
             System.arraycopy(alts, 0, temp, 0, pos);
             ++pos;
             System.arraycopy(alts, pos, temp, pos - 1, alts.length - pos);
             _alts = temp;
         }
 
         // It's in the database so mark it for deletion.
         else
             del.toBeDeleted();
     }
 
     /**
      * Empty the Alternates list
      */
     public void clearAlts()
     {
         _alts = null;
     }
 
     /**
      * Check the uniqueness of the Name and verify only one manully curated definition exists.
      * 
      * @param alt_
      *        The Alternate to verify
      * @return null if valid, otherwise and error message.
      */
     public String check(Alternates alt_)
     {
         for (Alternates temp : _alts)
         {
             if (temp.equals(alt_))
                 return "This Alternate Name is not unique. Please change one or more of the Name, Context or Type.";
             if (!temp.isName()
                             && temp.getType().equals(DBAccess._manuallyCuratedDef)
                             && temp.getType().equals(alt_.getType())
                             && !temp.getAltIdseq().equals(alt_.getAltIdseq()))
                 return "An Alternate Defintion already exists with Type \""
                                 + temp.getType()
                                 + "\". In this release an Administered Component may have only one Definition of this Type.";
         }
         return null;
     }
 
     /**
      * Get the default context id
      * 
      * @return the context id
      */
     public String getDefaultConteID()
     {
         return _conteIdseq[0];
     }
 
     /**
      * Get the default context name
      * 
      * @return the context name
      */
     public String getDefaultConteName()
     {
         return _conteName[0];
     }
 
     // This will add alternate definition if one with exact content does not exist.
     public boolean addAlternateDefinition(String def, AC_Bean acb, Connection conn) throws ToolException{
     	boolean ret = false;
     	boolean exists = false;
     	if (_alts == null){
     		DBAccess db = new DBAccess(conn);
     		this.loadAlternates(db, _sortName);
     	}
     	
         //GF30796 just making the codes more robust
     	if(def != null && !def.trim().equals("")) {
     		def = def.trim();
 	    	for (Alternates alt: _alts) {
 	    		if (alt.isDef()) {
 	    			String temp = alt.getName();
 	    			if (def.equals(temp)) {
 	    				exists = true;
 	    			}
 	    		}
 	    	}
 	    	
 	    	if (!exists) {
 	    		Alternates newAlt = new Alternates(Alternates._INSTANCEDEF, def, "System-generated", "ENGLISH", acb.getIDSEQ(), this.newIdseq(), acb.getContextIDSEQ(), acb.getContextName());
 	            
 	    		updateAlternatesList(newAlt, false);
 	    		ret = true;
 	    	}
     	}
     	
     	return ret;
     }
     
     //begin GF32723 added alternate name
     //brand new method
     public boolean addAlternateName(String type, String name, AC_Bean acb, Connection conn) throws ToolException{
     	boolean ret = false;
     	boolean exists = false;
     	if (_alts == null){
     		DBAccess db = new DBAccess(conn);
     		this.loadAlternates(db, _sortName);
     	}
     	for (Alternates alt: _alts) {
     		
     		if (alt.isDef()) {
     			String temp = alt.getName();
     			if (name.equals(temp))
     				exists = true;
     		}
     	}
     	
     	if (!exists) {
 //    		Alternates newAlt = new Alternates(Alternates._INSTANCENAME, name, "Prior Preferred Name", "ENGLISH", acb.getIDSEQ(), this.newIdseq(), acb.getContextIDSEQ(), acb.getContextName());
     		Alternates newAlt = new Alternates(Alternates._INSTANCENAME, name, type, "ENGLISH", acb.getIDSEQ(), this.newIdseq(), acb.getContextIDSEQ(), acb.getContextName());
           	updateAlternatesList(newAlt, false);
     	
     		
     	}
     	
     	ret = true;
     	   	
     	return ret;
     }
 
     //brand new method
 //    public int insertAltName(String type, String name, AC_Bean acb, Connection conn) {
 //		// TODO Auto-generated method stub
 //	    CallableStatement cstmt = null;
 //	    
 //	    int rc  = 0;
 //	    
 //	    String insert = "begin insert into sbr.designations_view "
 //	                + "(ac_idseq, conte_idseq, name, detl_name, lae_name) "
 //	                + "values (?, ?, ?, ?, ?) return desig_idseq into ?; end;";
 //	    
 //	    System.out.println (acb.getIDSEQ());
 //	    System.out.println(acb.getContextIDSEQ());
 //	    System.out.println(name);
 //	    System.out.println(type);
 //	    
 //	        try
 //	        {
 //	        	     	
 //
 //	            cstmt = conn.prepareCall(insert);
 //	            cstmt.setString(1, acb.getIDSEQ());
 //	            cstmt.setString(2, acb.getContextIDSEQ());
 //	            cstmt.setString(3, name);
 //	            cstmt.setString(4, "LOINC_CODE");	//GF32723 TBD need to get this from somewhere
 //	            cstmt.setString(5, "ENGLISH");
 //	            cstmt.registerOutParameter(6, java.sql.Types.VARCHAR);
 //	            rc = cstmt.executeUpdate();
 //	         }
 //	        catch (SQLException ex)
 //	        {
 //	        System.out.println("caught exception while inserting to designations: " + ex.toString());
 //	      		}
 //	        
 //	        finally {
 //	        if (cstmt != null) {
 //	                try {
 //	                cstmt.close();
 //	               
 //	                } catch (SQLException e1) {
 //	                System.out.println("Exception is"+e1);
 //	              cstmt = null;
 //	            }
 //	        }
 //        }
 //        System.out.println(rc);
 //        return rc;
 //        
 //    }
 	//end GF32723
 }
