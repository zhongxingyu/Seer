 // Copyright ScenPro, Inc 2007
 
// $Header: /cvsshare/content/cvsroot/cdecurate/src/gov/nih/nci/cadsr/cdecurate/tool/PVAction.java,v 1.34 2009-03-03 20:12:32 veerlah Exp $
 // $Name: not supported by cvs2svn $
 
 package gov.nih.nci.cadsr.cdecurate.tool;
 
 import gov.nih.nci.cadsr.cdecurate.database.SQLHelper;
 
 import java.io.Serializable;
 import java.sql.CallableStatement;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.text.SimpleDateFormat;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import javax.servlet.http.HttpSession;
 
 import oracle.jdbc.driver.OracleTypes;
 
 import org.apache.log4j.Logger;
 
 /**
  * @author shegde
  *
  */
 public class PVAction implements Serializable {
 	private static final Logger logger = Logger.getLogger(PVAction.class
 			.getName());
 
 	/**
 	 *
 	 */
 	public PVAction() {
 	}
 
 	/** marks the bean with view type to retain the view
 	 * @param vVDPV PV Bean vector
 	 * @param vwTypes String array existing view types
 	 */
 	public void doResetViewTypes(Vector<PV_Bean> vVDPV, String[] vwTypes) {
 		for (int i = 0; i < vwTypes.length; i++) {
 			String sType = vwTypes[i];
 			if (sType != null && vVDPV.size() > i) {
 				PV_Bean pv = (PV_Bean) vVDPV.elementAt(i);
 				if (!sType.equals("collapse") && !sType.equals("expand"))
 					sType = "expand";
 				pv.setPV_VIEW_TYPE(sType);
 				vVDPV.setElementAt(pv, i);
 			}
 		}
 	}
 
 	/**
 	 * stores the pv changes into the list
 	 * @param chgName String changed name of the pv
 	 * @param pvInd int index of the editing pv
 	 * @param data PVForm object
 	 * @return PV_Bean modified pv bean
 	 */
 	public PV_Bean changePVAttributes(String chgName, int pvInd, PVForm data) {
 		VD_Bean vd = data.getVD();
 		Vector<PV_Bean> vdpvs = vd.getVD_PV_List();
 		PV_Bean selPV = data.getSelectPV();
 		PV_Bean newPV = new PV_Bean();
 		if (selPV != null) //copy other attributes
 			newPV.copyBean(selPV);
 		newPV.setPV_VALUE(chgName); //add the new name
 		if (data.getNewVM() != null)
 			newPV.setPV_VM(data.getNewVM()); //copy the changed vm
 
 		selPV.setVP_SUBMIT_ACTION(PVForm.CADSR_ACTION_DEL);
 		vdpvs.removeElementAt(pvInd);
 		vd.getRemoved_VDPVList().addElement(selPV);
 
 		//insert the new one
 		newPV.setVP_SUBMIT_ACTION(PVForm.CADSR_ACTION_INS);
 		newPV.setPV_VIEW_TYPE("expand");
 		//newPV.setPV_VDPVS_IDSEQ("");  //do not remove it here
 		vdpvs.insertElementAt(newPV, pvInd);
 		//put it back in vd
 		vd.setVD_PV_List(vdpvs);
 		data.setVD(vd);
 		return newPV;
 	}
 
 	/**
 	 * validates pvs associated to the form; creates new pv if associated
 	 * @param selPV PV_Bean object
 	 * @param pvInd   int editing pv index
 	 * @param vd VD_Bean object that is editing
 	 * @param data PVForm object
 	 * @return String error message
 	 */
 	public String changePVQCAttributes(PV_Bean selPV, int pvInd, VD_Bean vd,
 			PVForm data) {
 		String sCRFmsg = "";
 		try {
 			//check if associated with the form
 			Vector<PV_Bean> vdpvs = vd.getVD_PV_List();
 			boolean isExists = false;
 			String selID = selPV.getPV_VDPVS_IDSEQ();
 			if (!selID.equals(""))
 				isExists = this.checkPVQCExists("", selID, data);
 			//associated to the form
 			if (isExists) {
 				PV_Bean oldPV = vd.getRemoved_VDPVList().lastElement();
 				sCRFmsg = "Unable to edit the Permissible Value (PV) ("
 						+ oldPV.getPV_VALUE()
 						+ ") and/or its Value Meaning (VM) because the Permissible Value is used in a CRF."
 						+ "\\n A new PV and VM pair will be created with the edited attributes."
 						+ "\\n After successful submission of the Value Domain, you may remove the original PV after disassociating the PV from the CRF.";
 				oldPV.setVP_SUBMIT_ACTION(PVForm.CADSR_ACTION_UPD);
 				//check if it was reused
 				VMAction vmact = new VMAction();
 				VM_Bean vm = vmact.checkExactMatch(oldPV.getPV_VM(), selPV
 						.getPV_VM());
 				//if pv or vm didn't change put back the old pv as it was
 				if (vm != null
 						&& oldPV.getPV_VALUE().equals(selPV.getPV_VALUE())) {
 					vdpvs.setElementAt(oldPV, pvInd);
 					vd.setVD_PV_List(vdpvs);
 					vd.getRemoved_VDPVList().removeElement(oldPV);
 					return "";
 				}
 				//continue with creating new one otherwise
 				data.setStatusMsg(data.getStatusMsg() + sCRFmsg);
 				vdpvs.insertElementAt(oldPV, pvInd); //insert it back
 				vd.getRemoved_VDPVList().removeElement(oldPV);
 				pvInd += 1; //reset it
 			}
 			//ready the new pv for insert
 			selPV.setVP_SUBMIT_ACTION(PVForm.CADSR_ACTION_INS);
 			selPV.setPV_PV_IDSEQ("");
 			selPV.setPV_VDPVS_IDSEQ("");
 			vdpvs.setElementAt(selPV, pvInd);
 			vd.setVD_PV_List(vdpvs);
 		} catch (RuntimeException e) {
 			logger.error("ERROR at changePVQCAttributes: ", e);
 		}
 
 		return sCRFmsg;
 	}
 
 	/**
 	 * Check if the permissible values associated with the form for the selected VD
 	 * @param vdIDseq String idseq of the vd
 	 * @param vpIDseq String idseq of the vd-pv
 	 * @param data PVForm data
 	 *
 	 * @return boolean true if pv is associated with the form false otherwise
 	 */
 	public boolean checkPVQCExists(String vdIDseq, String vpIDseq, PVForm data) //throws Exception
 	{
 		ResultSet rs = null;
 		PreparedStatement pstmt = null;
 		boolean isValid = false;
 		try {
 			if ((vpIDseq != null && !vpIDseq.equals(""))
 					|| (vdIDseq != null && !vdIDseq.equals(""))) {
 				if (data.getCurationServlet().getConn() != null) {
 					pstmt = data
 							.getCurationServlet()
 							.getConn()
 							.prepareStatement(
 									"select SBREXT_COMMON_ROUTINES.VD_PVS_QC_EXISTS(?,?) from DUAL");
 					// register the Out parameters
 					pstmt.setString(1, vpIDseq);
 					pstmt.setString(2, vdIDseq);
 					// Now we are ready to call the function
 					rs = pstmt.executeQuery();
 					while (rs.next()) {
 						if (rs.getString(1).equalsIgnoreCase("TRUE"))
 							isValid = true;
 					}
 				}
 			}
 		} catch (Exception e) {
 			logger.error("ERROR - checkPVQCExists for other : ", e);
 			data
 					.setStatusMsg(data.getStatusMsg()
 							+ "\\tError : Unable to get existing pv-qc."
 							+ e.toString());
 			data.setActionStatus(ConceptForm.ACTION_STATUS_FAIL);
 		}finally{
 			rs = SQLHelper.closeResultSet(rs);
             pstmt = SQLHelper.closePreparedStatement(pstmt);
 		}
 		return isValid;
 	} //end checkPVQCExists
 
 	/**
 	 * checks if removed pvs are associated with the form
 	 * send back the validation message for pv vds data.
 	 * @param data PVForm Object
 	 *
 	 * @return vector of validate
 	 */
 	public Vector<String> addValidateVDPVS(PVForm data) //throws Exception
 	{
 		VD_Bean vd = data.getVD();
 		Vector<ValidateBean> vValidate = vd.getValidateList();
 		try {
 			String sOrgAct = data.getOriginAction();
 			this.addValidateParentCon(vd, vValidate, sOrgAct);
 			//do this only if enumerated value domain
 			String s = vd.getVD_TYPE_FLAG();
 			if (s == null)
 				s = "";
 			if (s.equals("E")) {
 				//get current value domains vd-pv attributes
 				Vector<PV_Bean> vVDPVS = vd.getVD_PV_List();
 				String vdID = vd.getVD_VD_IDSEQ();
 				//remove vdidseq if new vd
 				if (vdID == null || sOrgAct.equalsIgnoreCase("newVD"))
 					vdID = "";
 				//make long string of values/meanings
 				String sPVVal = "";
 				String sPVMean = "";
 				String existVMs = "";
 				if (vVDPVS != null && !vVDPVS.isEmpty()) {
 					existVMs = readExistingVMs(vVDPVS, data);
 					for (int i = 0; i < vVDPVS.size(); i++) {
 						PV_Bean thisPV = (PV_Bean) vVDPVS.elementAt(i);
 						VM_Bean thisVM = thisPV.getPV_VM();
 						//check its relationship with the form if removed
 						if (sPVVal != null && !sPVVal.equals("")) {
 							sPVVal += ",\n ";
 							sPVMean += ",\n ";
 						}
 						sPVVal += thisPV.getPV_VALUE();
 						/*&& thisVM.getVM_SHORT_MEANING() != null
 						&& !thisVM.getVM_SHORT_MEANING().equals(""))
 					sPVMean += thisVM.getVM_SHORT_MEANING(); // thisPV.getPV_SHORT_MEANING();
 					
 */					if (thisVM != null && thisVM.getVM_LONG_NAME() != null
 								&& !thisVM.getVM_LONG_NAME().equals(""))
 							sPVMean += thisVM.getVM_LONG_NAME(); // thisPV.getPV_SHORT_MEANING();
 						}
 				}
 				//get the values element from the vector to update it
 				boolean matchFound = false;
 				for (int i = 0; i < vValidate.size(); i++) {
 					ValidateBean val = (ValidateBean) vValidate.elementAt(i);
 					if (val == null)
 						val = new ValidateBean();
 					if (val.getACAttribute().equals("Values")) {
 						val.setAttributeContent(sPVVal);
 						val.setAttributeStatus("Valid");
 						if (sPVVal == null || sPVVal.equals(""))
 							val
 									.setAttributeStatus("Value is required for Enumerated Value Domain");
 						matchFound = true;
 						vValidate.setElementAt(val, i);
 					} else if (val.getACAttribute().equals("Value Meanings")) {
 						val.setAttributeContent(sPVMean);
 						val.setAttributeStatus("Valid");
 						if (sPVMean == null || sPVMean.equals(""))
 							val
 									.setAttributeStatus("Value Meaning is required for Enumerated Value Domain");
 						else if (!existVMs.equals("")) //found matching vm
 							val
 									.setAttributeStatus("Warning: The following Value Meaning(s) exist in the database with the same name and will be reused.  Other attributes may not match exactly.  You may go back to review the existing Value Meaning(s): "
 											+ existVMs);
 
 						matchFound = true;
 						vValidate.setElementAt(val, i);
 					}
 				}
 				//add the values and meanings row if doesn't exist already
 				if (!matchFound) {
 					UtilService.setValPageVector(vValidate, "Values", sPVVal,
 							true, -1, "", sOrgAct);
 					UtilService.setValPageVector(vValidate, "Value Meanings",
 							sPVMean, true, -1, "", sOrgAct);
 				}
 			}
 		} catch (Exception e) {
 			logger.error("Error Occurred in addValidateVDPVS " + e.toString(),
 					e);
 			ValidateBean vbean = new ValidateBean();
 			vbean.setACAttribute("Error addValidateVDPVS");
 			vbean.setAttributeContent("Error message " + e.toString());
 			vbean
 					.setAttributeStatus("Error Occurred.  Please report to the help desk");
 			vValidate.addElement(vbean);
 		}
 		// finaly, send vector to JSP
 		vd.setValidateList(vValidate);
 		data.setVD(vd);
 		Vector<String> vValString = SetACService.makeStringVector(vValidate);
 		return vValString;
 	} //end addvalidateVDPVS
 
 	/**add teh parent concept to the validate vector
 	 * @param vd VDBean object
 	 * @param vValidate Validate bean object
 	 * @param sOrgAct String Origin action
 	 */
 	public void addValidateParentCon(VD_Bean vd,
 			Vector<ValidateBean> vValidate, String sOrgAct) {
 		String s = "";
 		String strInValid = "";
 		Vector vParList = vd.getReferenceConceptList(); //  (Vector)session.getAttribute("VDParentConcept");
 
 		if (vParList != null && vParList.size() > 0) {
 			for (int i = 0; i < vParList.size(); i++) {
 				EVS_Bean parBean = (EVS_Bean) vParList.elementAt(i);
 				if (!parBean.getCON_AC_SUBMIT_ACTION().equals("DEL")) {
 					String parString = "";
 					if (parBean.getLONG_NAME() != null)
 						parString = parBean.getLONG_NAME() + "   ";
 					if (parBean.getEVS_DATABASE() != null) {
 						//do not add this if non evs
 						if (parBean.getCONCEPT_IDENTIFIER() != null
 								&& !parBean.getEVS_DATABASE().equals("Non_EVS"))
 							parString += parBean.getCONCEPT_IDENTIFIER()
 									+ "   ";
 						parString += parBean.getEVS_DATABASE();
 					}
 					if (!parString.equals("")) {
 						if (s.equals(""))
 							s = parString;
 						else
 							s = s + ", " + parString;
 					}
 				}
 			}
 		}
 		//get the values element from the vector to update it
 		boolean matchFound = false;
 		for (int i = 0; i < vValidate.size(); i++) {
 			ValidateBean val = (ValidateBean) vValidate.elementAt(i);
 			if (val == null)
 				val = new ValidateBean();
 			if (val.getACAttribute().equals("Parent Concept")) {
 				val.setAttributeContent(s);
 				val.setAttributeStatus("Valid");
 				matchFound = true;
 				vValidate.setElementAt(val, i);
 				break;
 			}
 		}
 		if (!matchFound) {
 			UtilService.setValPageVector(vValidate, "Parent Concept", s, false,
 					-1, strInValid, sOrgAct);
 		}
 
 		vd.setValidateList(vValidate);
 	}
 
 	/**
 	 * To get the Permissible Values for the selected AC  from the database.
 	 *
 	 * calls oracle stored procedure
 	 *  "{call SBREXT_CDE_CURATOR_PKG.SEARCH_PV(AC_IDSEQ, OracleTypes.CURSOR)}"
 	 *
 	 * loop through the ResultSet and add them to PV bean
 	 *
 	 * @param acIdseq String id of the selected ac.
 	 * @param sAction String search action.
 	 * @param data PVForm object
 	 *
 	 * @return Vector of PVBean
 	 */
 	public Vector<PV_Bean> doPVACSearch(String acIdseq, String sAction,
 			PVForm data) //
 	{
 		ResultSet rs = null;
 		CallableStatement cstmt = null;
 		Vector<PV_Bean> vList = new Vector<PV_Bean>();
 		try {
 			if (data.getCurationServlet().getConn() != null) {
 				cstmt = data.getCurationServlet().getConn().prepareCall(
 						"{call SBREXT.SBREXT_CDE_CURATOR_PKG.SEARCH_PV(?,?)}");
 				// Now tie the placeholders for out parameters.
 				cstmt.registerOutParameter(2, OracleTypes.CURSOR);
 				// Now tie the placeholders for In parameters.
 				cstmt.setString(1, acIdseq);
 				// Now we are ready to call the stored procedure
 				cstmt.execute();
 				//store the output in the resultset
 				rs = (ResultSet) cstmt.getObject(2);
 				//  String s;
 				if (rs != null) {
 					//loop through the resultSet and add them to the bean
 					while (rs.next()) {
 						PV_Bean pvBean = new PV_Bean();
 						pvBean.setPV_PV_IDSEQ(rs.getString("pv_idseq"));
 						pvBean.setPV_VALUE(rs.getString("value"));
 						pvBean.setPV_SHORT_MEANING(rs
 								.getString("short_meaning"));
 						if (sAction.equals("NewUsing"))
 							pvBean.setPV_VDPVS_IDSEQ("");
 						else
 							pvBean.setPV_VDPVS_IDSEQ(rs.getString("vp_idseq"));
 /*
 						pvBean.setPV_MEANING_DESCRIPTION(rs
 								.getString("vm_description"));*/
 						pvBean.setPV_MEANING_DESCRIPTION(rs
 								.getString("PREFERRED_DEFINITION"));
 						pvBean.setPV_VALUE_ORIGIN(rs.getString("origin"));
 						String sDate = rs.getString("begin_date");
 						if (sDate != null && !sDate.equals(""))
 							sDate = data.getUtil().getCurationDate(sDate);
 						pvBean.setPV_BEGIN_DATE(sDate);
 						sDate = rs.getString("end_date");
 						if (sDate != null && !sDate.equals(""))
 							sDate = data.getUtil().getCurationDate(sDate);
 						pvBean.setPV_END_DATE(sDate);
 						if (sAction.equals("NewUsing"))
 							pvBean.setVP_SUBMIT_ACTION("INS");
 						else
 							pvBean.setVP_SUBMIT_ACTION("NONE");
 						//get valid value attributes
 						pvBean.setQUESTION_VALUE("");
 						pvBean.setQUESTION_VALUE_IDSEQ("");
 						//get vm concept attributes
 						// String sCondr = rs.getString("vm_condr_idseq");
 						VMAction vmact = new VMAction();
 						pvBean.setPV_VM(vmact.doSetVMAttributes(rs, data
 								.getCurationServlet().getConn()));
 						//get parent concept attributes
 						String sCon = rs.getString("con_idseq");
 						this.doSetParentAttributes(sCon, pvBean, data);
 
 						pvBean.setPV_VIEW_TYPE("expand");
 						//add pv idseq in the pv id vector
 						vList.addElement(pvBean); //add the bean to a vector
 					} //END WHILE
 				} //END IF
 			}
 		} catch (Exception e) {
 			logger.error("ERROR - doPVACSearch for other : " + e.toString(), e);
 		}
 		finally{
 			rs = SQLHelper.closeResultSet(rs);
 			cstmt = SQLHelper.closeCallableStatement(cstmt);
 		}
 		return vList;
 	} //doPVACSearch search
 
 	/** reset the version value domain with pv idseqs
 	 * @param vd VD Bean object
 	 * @param verList PVBEan vector of the existing in cadsr
 	 */
 	public void doResetVersionVDPV(VD_Bean vd, Vector<PV_Bean> verList) {
 		Vector<PV_Bean> rmVDPV = vd.getRemoved_VDPVList();
 		Vector<PV_Bean> pgVDPV = vd.getVD_PV_List();
 		for (int i = 0; i < verList.size(); i++) {
 			PV_Bean pv = verList.elementAt(i);
 			String pvID = pv.getPV_PV_IDSEQ();
 			//check if it is removed from the page
 			for (int j = 0; j < rmVDPV.size(); j++) {
 				PV_Bean rmPV = rmVDPV.elementAt(j);
 				String rmID = rmPV.getPV_PV_IDSEQ();
 				if (rmID != null && !rmID.equals("") && rmID.equals(pvID)) {
 					rmPV.setPV_VDPVS_IDSEQ(pv.getPV_VDPVS_IDSEQ());
 					rmPV.setVP_SUBMIT_ACTION(PVForm.CADSR_ACTION_DEL);
 					rmVDPV.setElementAt(rmPV, j);
 					verList.removeElementAt(i); //remove it from the vers list
 					i -= 1; //move back teh index
 					break;
 				}
 			}
 		}
 		vd.setRemoved_VDPVList(rmVDPV);
 		//add the newly added ones to the list
 		for (int i = 0; i < pgVDPV.size(); i++) {
 			PV_Bean pv = pgVDPV.elementAt(i);
 			String value = pv.getPV_VALUE();
 			//String vm = pv.getPV_VM().getVM_SHORT_MEANING();
 			String vm = pv.getPV_VM().getVM_LONG_NAME();
 			//check if it exists in versioned list
 			boolean isNew = true;
 			for (int j = 0; j < verList.size(); j++) {
 				PV_Bean verPV = verList.elementAt(j);
 				String verVal = verPV.getPV_VALUE();
 				//String verVM = verPV.getPV_VM().getVM_SHORT_MEANING();
 				String verVM = verPV.getPV_VM().getVM_LONG_NAME();
 				if (verVal.equals(value) && verVM.equals(vm)) {
 					isNew = false;
 					break;
 				}
 			}
 			if (isNew) {
 				pv.setVP_SUBMIT_ACTION(PVForm.CADSR_ACTION_INS);
 				verList.addElement(pv);
 			}
 		}
 		vd.setVD_PV_List(verList);
 	}
 
 	/** get concept attributes for parent concept id
 	 * @param conIDseq String con idseq
 	 * @param pvBean PVBean object
 	 * @param data PVForm object
 	 */
 	private void doSetParentAttributes(String conIDseq, PV_Bean pvBean,
 			PVForm data) {
 		EVS_Bean parConcept = new EVS_Bean();
 		parConcept.setIDSEQ(conIDseq);
 		if (conIDseq != null && !conIDseq.equals("")) {
 			InsACService insAC = new InsACService(data.getRequest(), data
 					.getResponse(), data.getCurationServlet());
 			String sRet = "";
 			conIDseq = insAC.getConcept(sRet, parConcept, false);
 		}
 		pvBean.setPARENT_CONCEPT(parConcept);
 	}
 
 	/**
 	 * To get resultSet from database for Permissible Value Component called from getACKeywordResult and getCDEIDSearch methods.
 	 *
 	 * calls oracle stored procedure
 	 *  "{call SBREXT_CDE_CURATOR_PKG.SEARCH_PVVM(InString, OracleTypes.CURSOR)}"
 	 *
 	 * loop through the ResultSet and add them to bean which is added to the vector to return
 	 *
 	 * @param InString Keyword value to filter by name
 	 * @param cd_idseq String cdidseq to filter by CD
 	 * @param conName String to filter by concept name
 	 * @param conID STring value to filter by concept id
 	 * @param data PVForm object
 	 * @return PV_Bean object
 	 *
 	 */
 	public Vector<PV_Bean> doPVVMSearch(String InString, String cd_idseq,
 			String conName, String conID, PVForm data) // returns list of Data Elements
 	{
 		ResultSet rs = null;
 		CallableStatement cstmt = null;
 		Vector<PV_Bean> vList = new Vector<PV_Bean>();
 		try {
 			if (data.getCurationServlet().getConn() != null) {
 				cstmt = data
 						.getCurationServlet()
 						.getConn()
 						.prepareCall(
 								"{call SBREXT.SBREXT_CDE_CURATOR_PKG.SEARCH_PVVM(?,?,?,?,?)}");
 				// Now tie the placeholders for out parameters.
 				cstmt.registerOutParameter(3, OracleTypes.CURSOR);
 				// Now tie the placeholders for In parameters.
 				cstmt.setString(1, InString);
 				cstmt.setString(2, cd_idseq);
 				cstmt.setString(4, conName);
 				cstmt.setString(5, conID);
 				// Now we are ready to call the stored procedure
 				cstmt.execute();
 				//store the output in the resultset
 				rs = (ResultSet) cstmt.getObject(3);
 
 				String s;
 				if (rs != null) {
 					//loop through the resultSet and add them to the bean
 					while (rs.next()) {
 						PV_Bean PVBean = new PV_Bean();
 						PVBean.setPV_PV_IDSEQ(rs.getString("pv_idseq"));
 						PVBean.setPV_VALUE(rs.getString("value"));
 						PVBean.setPV_SHORT_MEANING(rs
 								.getString("short_meaning"));
 						s = rs.getString("begin_date");
 						if (s != null)
 							s = data.getUtil().getCurationDate(s); //convert to dd/mm/yyyy format
 						PVBean.setPV_BEGIN_DATE(s);
 						s = rs.getString("end_date");
 						if (s != null)
 							s = data.getUtil().getCurationDate(s);
 						PVBean.setPV_END_DATE(s);
 						//String sdef = rs.getString("vm_description");
 						String sdef = rs.getString("PREFERRED_DEFINITION");
 						if (sdef == null || sdef.equals(""))
 							sdef = rs.getString("preferred_definition");
 						PVBean.setPV_MEANING_DESCRIPTION(sdef); //from meanings table
 						PVBean.setPV_CONCEPTUAL_DOMAIN(rs.getString("cd_name"));
 
 						//get vm concept attributes
 						VMAction vmact = new VMAction();
 						PVBean.setPV_VM(vmact.doSetVMAttributes(rs, data
 								.getCurationServlet().getConn()));
 						//get database attribute
 						PVBean.setPV_EVS_DATABASE("caDSR");
 						vList.addElement(PVBean); //add the bean to a vector
 					} //END WHILE
 				} //END IF
 			}
 		} catch (Exception e) {
 			logger.error("ERROR - GetACSearch-searchPVVM for other : "
 					+ e.toString(), e);
 		}finally{
 			rs = SQLHelper.closeResultSet(rs);
 			cstmt = SQLHelper.closeCallableStatement(cstmt);
 		}
 		return vList;
 	} //endPVVM search
 
 	/**
 	 * To insert a new Permissible value in the database after the validation.
 	 * Called from CurationServlet.
 	 * Gets all the attribute values from the bean, sets in parameters, and registers output parameter.
 	 * Calls oracle stored procedure
 	 *   "{call SBREXT_Set_Row.SET_PV(?,?,?,?,?,?,?,?,?,?,?,?,?,?)}" to submit
 	 * @param data PVForm data
 	 *
 	 * @return String return code from the stored procedure call. null if no error occurred.
 	 */
 	public String setPV(PVForm data) {
 		PV_Bean pv = data.getSelectPV();
 		ResultSet rs = null;
 		HttpSession session = data.getRequest().getSession();
 		CallableStatement cstmt = null;
 		String sMsg = ""; //out
 		try {
 			String sAction = PVForm.CADSR_ACTION_INS;
 			String sValue = pv.getPV_VALUE();
 			VM_Bean vm = pv.getPV_VM();
 			String sShortMeaning = pv.getPV_SHORT_MEANING();
 			if (vm != null && vm.getVM_LONG_NAME()!= null
 					&& !vm.getVM_LONG_NAME().equals(""))
 				sShortMeaning = vm.getVM_LONG_NAME();
 			String sMeaningDescription = pv.getPV_MEANING_DESCRIPTION();
 			if (vm != null && vm.getVM_PREFERRED_DEFINITION() != null
 					&& !vm.getVM_PREFERRED_DEFINITION().equals(""))
 				sMeaningDescription = vm.getVM_PREFERRED_DEFINITION();
 			// if (sMeaningDescription == null) sMeaningDescription = "";
 			//  if (sMeaningDescription.length() > 2000) sMeaningDescription = sMeaningDescription.substring(0, 2000);
 			String sBeginDate = pv.getPV_BEGIN_DATE();
 			if (sBeginDate == null || sBeginDate.equals("")) {
 				SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
 				sBeginDate = formatter.format(new java.util.Date());
 			}
 			sBeginDate = data.getUtil().getOracleDate(sBeginDate);
 			String sEndDate = data.getUtil().getOracleDate(pv.getPV_END_DATE());
 
 			//check if it already exists
 			String sPV_ID = this.getExistingPV(data, sValue, sShortMeaning,vm.getVM_IDSEQ());
 			if (sPV_ID != null && !sPV_ID.equals("")) {
 				pv.setPV_PV_IDSEQ(sPV_ID); //update the pvbean with the id
 				return sMsg;
 			}
 
 		if (data.getCurationServlet().getConn() != null) {
 				//cstmt = conn.prepareCall("{call SBREXT_Set_Row.SET_PV(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
 				cstmt = data
 						.getCurationServlet()
 						.getConn()
 						.prepareCall(
 								"{call SBREXT_SET_ROW.SET_PV(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
 				// register the Out parameters
 				cstmt.registerOutParameter(2, java.sql.Types.VARCHAR); //return code
 				cstmt.registerOutParameter(4, java.sql.Types.VARCHAR); //pv id
 				cstmt.registerOutParameter(5, java.sql.Types.VARCHAR); //value
 				cstmt.registerOutParameter(6, java.sql.Types.VARCHAR); //short meaning
 				cstmt.registerOutParameter(7, java.sql.Types.VARCHAR); //begin date
 				cstmt.registerOutParameter(8, java.sql.Types.VARCHAR); //meaning description
 				cstmt.registerOutParameter(9, java.sql.Types.VARCHAR); //low value num
 				cstmt.registerOutParameter(10, java.sql.Types.VARCHAR); //high value num
 				cstmt.registerOutParameter(11, java.sql.Types.VARCHAR); //end date
 				//cstmt.registerOutParameter(12, java.sql.Types.VARCHAR); //pv_vm_idseq
 				cstmt.registerOutParameter(13, java.sql.Types.VARCHAR); //created by
 				cstmt.registerOutParameter(14, java.sql.Types.VARCHAR); //date created
 				cstmt.registerOutParameter(15, java.sql.Types.VARCHAR); //modified by
 				cstmt.registerOutParameter(16, java.sql.Types.VARCHAR); //date modified
 
 				// Set the In parameters (which are inherited from the PreparedStatement class)
 				//			Get the username from the session.
 				String userName = (String) session.getAttribute("Username");
 				cstmt.setString(1, userName); //set ua_name
 				cstmt.setString(3, sAction); //ACTION - INS, UPD or DEL
 				if ((sAction.equals("UPD")) || (sAction.equals("DEL"))) {
 					sPV_ID = pv.getPV_PV_IDSEQ();
 					cstmt.setString(4, sPV_ID);
 				} else {
 					cstmt.setString(5, sValue);
 					cstmt.setString(6, sShortMeaning);
 				}
 				cstmt.setString(7, sBeginDate);
 				cstmt.setString(8, sMeaningDescription);
 				cstmt.setString(11, sEndDate);
 				cstmt.setString(12, vm.getVM_IDSEQ());
 				// Now we are ready to call the stored procedure
 				cstmt.execute();
 				String sReturnCode = cstmt.getString(2);
 				sPV_ID = cstmt.getString(4);
 				pv.setPV_PV_IDSEQ(sPV_ID);
 				if (sReturnCode != null && !sReturnCode.equals("API_PV_300")) {
 					sMsg += "\\t " + sReturnCode
 							+ " : Unable to update Permissible Value - "
 							+ sValue + ".";
 					data.setRetErrorCode(sReturnCode); //store returncode in request to track it all through this request
 					data.setPvvmErrorCode(sReturnCode); //store it capture check for pv creation
 				}
 			}
 		} catch (Exception e) {
 			logger.error("ERROR in setPV for other : " + e.toString(), e);
 			data.setRetErrorCode("Exception");
 			sMsg += "\\t Exception : Unable to update Permissible Value attributes.";
 		}
 		finally{
 			rs = SQLHelper.closeResultSet(rs);
 			cstmt = SQLHelper.closeCallableStatement(cstmt);
 		}
 		return sMsg;
 	}
 
 	/**
 	 * Called from 'setPV' method for insert of PV.
 	 * Sets in parameters, and registers output parameter.
 	 * Calls oracle stored procedure
 	 *   "{call SBREXT_GET_ROW.GET_PV(?,?,?,?,?,?,?,?,?,?,?,?,?)}" to submit
 	 * @param data PVForm object
 	 * @param sValue   existing Value.
 	 * @param sMeaning  existing meaning.
 	 *
 	 *  @return String existing pv_idseq from the stored procedure call.
 	 */
 	public String getExistingPV(PVForm data, String sValue, String sMeaning,String vmidseq) {
 		
 		String sPV_IDSEQ = "";
 		CallableStatement cstmt = null;
 		try {
 			if (data.getCurationServlet().getConn() != null) {
 				cstmt = data
 						.getCurationServlet()
 						.getConn()
 						.prepareCall(
 								"{call SBREXT_GET_ROW.GET_PV(?,?,?,?,?,?,?,?,?,?,?,?,?)}");
 				cstmt.registerOutParameter(1, java.sql.Types.VARCHAR); //return code
 				cstmt.registerOutParameter(2, java.sql.Types.VARCHAR); //PV_IDSEQ
 				cstmt.registerOutParameter(5, java.sql.Types.VARCHAR); //MEANING_DESCRIPTION
 				cstmt.registerOutParameter(6, java.sql.Types.VARCHAR); // HIGH_VALUE_NUM
 				cstmt.registerOutParameter(7, java.sql.Types.VARCHAR); // LOW_VALUE_NUM
 				cstmt.registerOutParameter(8, java.sql.Types.VARCHAR); // BEGIN_DATE
 				cstmt.registerOutParameter(9, java.sql.Types.VARCHAR); // END_DATE
 				cstmt.registerOutParameter(10, java.sql.Types.VARCHAR); // CREATED_BY
 				cstmt.registerOutParameter(11, java.sql.Types.VARCHAR); // Date Created
 				cstmt.registerOutParameter(12, java.sql.Types.VARCHAR); // MODIFIED_BY
 				cstmt.registerOutParameter(13, java.sql.Types.VARCHAR); // DATE_MODIFIED
 
 				cstmt.setString(3, sValue); // Value
 				cstmt.setString(4, vmidseq); // vm idseq
 				cstmt.setString(5, sMeaning); // Meaning
 				// Now we are ready to call the stored procedure
 				cstmt.execute();
 				sPV_IDSEQ = (String) cstmt.getObject(2);
 				if (sPV_IDSEQ == null)
 					sPV_IDSEQ = "";
 			}
 		} catch (Exception e) {
 			logger.error("ERROR in getExistingPV for exception : "
 					+ e.toString(), e);
 		}finally{
 			cstmt = SQLHelper.closeCallableStatement(cstmt);
 		}
 		return sPV_IDSEQ;
 	}
 
 	/**
 	 * To remove exisitng one in VD_PVS relationship table after the validation.
 	 * Called from 'setVD_PVS' method.
 	 * Sets in parameters, and registers output parameter.
 	 * Calls oracle stored procedure
 	 *   "{call SBREXT_Set_Row.SET_VD_PVS(?,?,?,?,?,?,?,?,?,?)}" to submit
 	 * @param data PVForm object
 	 *
 	 * @return String of return code
 	 */
 	public String setVD_PVS(PVForm data) {
 		PV_Bean pvBean = data.getSelectPV();
 		VD_Bean vdBean = data.getVD();
 		HttpSession session = data.getRequest().getSession();
 		CallableStatement cstmt = null;
 		String sMsg = "";
 		try {
 			String sAction = pvBean.getVP_SUBMIT_ACTION();
 			String vpID = pvBean.getPV_VDPVS_IDSEQ();
 			//deleting newly selected/created pv don't do anything since it doesn't exist in cadsr to remove.
 			if (sAction.equals("DEL") && (vpID == null || vpID.equals("")))
 				return sMsg;
 			// create parent concept
 			String parIdseq = this.setParentConcept(pvBean, vdBean);
 			if (data.getCurationServlet().getConn() != null) {
 				// cstmt = conn.prepareCall("{call sbrext_set_row.SET_VD_PVS(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
 				cstmt = data
 						.getCurationServlet()
 						.getConn()
 						.prepareCall(
 								"{call SBREXT_SET_ROW.SET_VD_PVS(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}");
 				cstmt.registerOutParameter(2, java.sql.Types.VARCHAR); //return code
 				cstmt.registerOutParameter(4, java.sql.Types.VARCHAR); //vd_PVS id
 				cstmt.registerOutParameter(5, java.sql.Types.VARCHAR); //vd id
 				cstmt.registerOutParameter(6, java.sql.Types.VARCHAR); //pvs id
 				cstmt.registerOutParameter(7, java.sql.Types.VARCHAR); //context id
 				cstmt.registerOutParameter(8, java.sql.Types.VARCHAR); //date created
 				cstmt.registerOutParameter(9, java.sql.Types.VARCHAR); //created by
 				cstmt.registerOutParameter(10, java.sql.Types.VARCHAR); //modified by
 				cstmt.registerOutParameter(11, java.sql.Types.VARCHAR); //date modified
 
 				// Set the In parameters (which are inherited from the PreparedStatement class)
 				//create a new row if vpIdseq is empty for updates
 				//			Get the username from the session.
 				String userName = (String) session.getAttribute("Username");
 				cstmt.setString(1, userName); //set ua_name
 				if (sAction.equals("UPD") && (vpID == null || vpID.equals("")))
 					sAction = "INS";
 
 				cstmt.setString(3, sAction); //ACTION - INS, UPD  or DEL
 				cstmt.setString(4, pvBean.getPV_VDPVS_IDSEQ()); //VPid);       //vd_pvs ideq - not null
 				cstmt.setString(5, vdBean.getVD_VD_IDSEQ()); // sVDid);       //value domain id - not null
 				cstmt.setString(6, pvBean.getPV_PV_IDSEQ()); // sPVid);       //permissible value id - not null
 				cstmt.setString(7, vdBean.getVD_CONTE_IDSEQ()); // sContextID);       //context id - not null for INS, must be null for UPD
 				String pvOrigin = pvBean.getPV_VALUE_ORIGIN();
 				//believe that it is defaulted to vd's origin
 				//if (pvOrigin == null || pvOrigin.equals(""))
 				//   pvOrigin = vdBean.getVD_SOURCE();
 				cstmt.setString(12, pvOrigin); // sOrigin);
 				String sDate = pvBean.getPV_BEGIN_DATE();
 				if (sDate != null && !sDate.equals(""))
 					sDate = data.getUtil().getOracleDate(sDate);
 				cstmt.setString(13, sDate); // begin date);
 				sDate = pvBean.getPV_END_DATE();
 				if (sDate != null && !sDate.equals(""))
 					sDate = data.getUtil().getOracleDate(sDate);
 				cstmt.setString(14, sDate); // end date);
 				cstmt.setString(15, parIdseq);
 				//execute the qury
 				cstmt.execute();
 				String retCode = cstmt.getString(2);
 				//store the status message if children row exist; no need to display message if doesn't exist
 				if (retCode != null && !retCode.equals("")
 						&& !retCode.equals("API_VDPVS_005")) {
 					String sPValue = pvBean.getPV_VALUE();
 					// String sVDName = vdBean.getVD_LONG_NAME();
 					if (sAction.equals("INS") || sAction.equals("UPD"))
 						sMsg += "\\t " + retCode
 								+ " : Unable to update permissible value "
 								+ sPValue + ".";
 					else if (sAction.equals("DEL")
 							&& retCode.equals("API_VDPVS_006")) {
 						sMsg += "\\t This Value Domain is used by a form. "
 								+ "Create a new version of the Value Domain to remove permissible value "
 								+ sPValue + ".";
 					} else if (!sAction.equals("DEL")
 							&& !retCode.equals("API_VDPVS_005"))
 						sMsg += "\\t " + retCode
 								+ " : Unable to remove permissible value "
 								+ sPValue + ".";
 
 					data.setRetErrorCode(retCode);
 				} else {
 					retCode = "";
 					pvBean.setPV_VDPVS_IDSEQ(cstmt.getString(4));
 				}
 			}
 			
 		} catch (Exception e) {
 			logger.error("ERROR in setVD_PVS for other : " + e.toString(), e);
 			data.setRetErrorCode("Exception");
 			sMsg += "\\t Exception : Unable to update or remove PV of VD.";
 		}finally{
 		  cstmt = SQLHelper.closeCallableStatement(cstmt);
 		}
 		return sMsg;
 	} //END setVD_PVS
 
 	/**add the block changed data of the PV
 	 * @param data PVForm object
 	 * @param changeField pv attributes that is being changed
 	 * @param changeData STring value that is changed
 	 */
 	public void doBlockEditPV(PVForm data, String changeField, String changeData) {
 		VD_Bean vd = data.getVD();
 		Vector<PV_Bean> vdpv = vd.getVD_PV_List();
 		for (int i = 0; i < vdpv.size(); i++) {
 			PV_Bean pv = (PV_Bean) vdpv.elementAt(i);
 			if (changeField.equals("origin"))
 				pv.setPV_VALUE_ORIGIN(changeData);
 			else if (changeField.equals("begindate"))
 				pv.setPV_BEGIN_DATE(changeData);
 			else if (changeField.equals("enddate"))
 				pv.setPV_END_DATE(changeData);
 			//change the submit action
 			pv.setVP_SUBMIT_ACTION(PVForm.CADSR_ACTION_UPD);
 			vdpv.setElementAt(pv, i);
 		}
 		vd.setVD_PV_List(vdpv);
 		data.setVD(vd);
 	}
 
 	/**get the conidseq to assign to the pv vd relationship from teh list of concepts bean
 	 * @param pvBean PVBean object
 	 * @param vd VDBean object
 	 * @return String conidseq
 	 */
 	private String setParentConcept(PV_Bean pvBean, VD_Bean vd) {
 		String conIDseq = "";
 		//create parent concept if exists
 		EVS_Bean pCon = (EVS_Bean) pvBean.getPARENT_CONCEPT();
 		if (pCon != null) {
 			conIDseq = pCon.getIDSEQ(); // pCon.getCONDR_IDSEQ();
 			String pConID = pCon.getCONCEPT_IDENTIFIER();
 			if (pConID != null && !pConID.equals("")
 					&& (conIDseq == null || conIDseq.equals(""))) {
 				Vector<EVS_Bean> vPar = vd.getReferenceConceptList();
 				if (vPar != null) {
 					for (int i = 0; i < vPar.size(); i++) {
 						EVS_Bean ePar = (EVS_Bean) vPar.elementAt(i);
 						if (ePar.getCONCEPT_IDENTIFIER().equals(pConID)
 								&& ePar.getIDSEQ() != null
 								&& !ePar.getIDSEQ().equals("")) {
 							conIDseq = ePar.getIDSEQ();
 							break;
 						}
 					}
 				}
 			}
 		}
 		return conIDseq;
 	}
 
 	/**to mark removal of parent from the page
 	 * @param sParentCC String parent concept ID
 	 * @param sParentName STring parent concept name
 	 * @param sParentDB String parent concept vocabulary
 	 * @param sPVAction string pv page action
 	 * @param data PVFrom object
 	 */
 	public void doRemoveParent(String sParentCC, String sParentName,
 	String sParentDB, String sPVAction, PVForm data) {
 	VD_Bean vd = data.getVD();
 		Vector<EVS_Bean> vParentCon = vd.getReferenceConceptList(); // (Vector)session.getAttribute("VDParentConcept");
 		if (vParentCon == null)
 			vParentCon = new Vector<EVS_Bean>();
 		//for non evs parent compare the long names instead
 		if (sParentName != null && !sParentName.equals("") && sParentDB != null
 				&& sParentDB.equals("Non_EVS"))
 			sParentCC = sParentName;
 		if (sParentCC != null) {
 			for (int i = 0; i < vParentCon.size(); i++) {
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
 				//for non evs parent compare the long names instead
 				if (sParentDB != null && sParentDB.equals("Non_EVS"))
 					thisParent = thisParentName;
 				//look for the matched parent from the vector to remove
 				if (sParentCC.equals(thisParent)) {
 					if (sPVAction.equals("removePVandParent")) {
 						Vector<PV_Bean> vVDPVList = vd.getVD_PV_List(); // (Vector)session.getAttribute("VDPVList");
 						if (vVDPVList == null)
 							vVDPVList = new Vector<PV_Bean>();
 						//loop through the vector of pvs to get matched parent
 						for (int j = 0; j < vVDPVList.size(); j++) {
 							PV_Bean pvBean = (PV_Bean) vVDPVList.elementAt(j);
 							if (pvBean == null)
 								pvBean = new PV_Bean();
 							EVS_Bean pvParent = (EVS_Bean) pvBean
 									.getPARENT_CONCEPT();
 							if (pvParent == null)
 								pvParent = new EVS_Bean();
 							String pvParCon = pvParent.getCONCEPT_IDENTIFIER();
 							//match the parent concept with the pv's parent concept
 							if (thisParent.equals(pvParCon)) {
 								doRemovePV(vd, j, pvBean, -1);
 								j -= 1;
 							}
 						}
 						vd.setVD_PV_List(vVDPVList);
 					}
 					//mark the parent as delected and leave
 					eBean.setCON_AC_SUBMIT_ACTION("DEL");
 					vParentCon.setElementAt(eBean, i);
 					break;
 				}
 			}
 		}
 		vd.setReferenceConceptList(vParentCon);
 		data.setVD(vd);
 	}
 
 	/** mark removal of pv from teh page
 	 * @param vd VDBEan object
 	 * @param pvInd int selected pv indicator
 	 * @param selPV PVBean object of the selected pv
 	 * @param iSetVDPV int set vdpv indicator
 	 */
 	public void doRemovePV(VD_Bean vd, int pvInd, PV_Bean selPV,
 			@SuppressWarnings("unused")
 			int iSetVDPV) {
 		Vector<PV_Bean> vdpvs = vd.getVD_PV_List();
 		vdpvs.removeElementAt(pvInd);
 		//  if (iSetVDPV == 0)
 		vd.setVD_PV_List(vdpvs);
 		//add the removed pv to the removed pv list
 		if (selPV != null) {
 			selPV.setVP_SUBMIT_ACTION(PVForm.CADSR_ACTION_DEL);
 			Vector<PV_Bean> rmList = vd.getRemoved_VDPVList(); // data.getRemovedPVList();
 			rmList.addElement(selPV);
 			//session.setAttribute("RemovedPVList", rmList);
 			vd.setRemoved_VDPVList(rmList);
 		}
 	}
 
 	/**
 	 * put back the removed pv so that it doesn't get deleted
 	 * @param vd
 	 * @param pvIDseq
 	 */
 	public void putBackRemovedPV(VD_Bean vd, String pvIDseq) {
 		Vector<PV_Bean> rmList = vd.getRemoved_VDPVList();
 		for (int i = 0; i < rmList.size(); i++) {
 			PV_Bean pv = (PV_Bean) rmList.elementAt(i);
 			String thisID = pv.getPV_PV_IDSEQ();
 			if (thisID != null && thisID.equals(pvIDseq)) {
 				rmList.removeElementAt(i);
 				break;
 			}
 		}
 		vd.setRemoved_VDPVList(rmList);
 	}
 
 	/**
 	 * gets the row number from the hiddenSelRow
 	 * Loops through the selected row and gets the evs bean for that row from the vector of evs search results.
 	 * adds it to vList vector and return the vector back
 	 * @param vRSel Vector of EVS Bean of search results
 	 * @param selRows String array of rows selected
 	 * @param data PVForm object
 	 * @throws java.lang.Exception
 	 */
 	public void getEVSSelRowVector(Vector<EVS_Bean> vRSel, String[] selRows,
 			PVForm data) throws Exception {
 		try {
 			VD_Bean vd = data.getVD();
 			Vector<EVS_Bean> vList = vd.getReferenceConceptList(); // (Vector)session.getAttribute("VDParentConcept");
 			if (vList == null)
 				vList = new Vector<EVS_Bean>();
 
 			//loop through the array of strings
 			for (int i = 0; i < selRows.length; i++) {
 				String thisRow = selRows[i];
 				Integer IRow = new Integer(thisRow);
 				int iRow = IRow.intValue();
 				if (iRow < 0 || iRow > vRSel.size())
 					data.setStatusMsg(data.getStatusMsg()
 							+ "\\tRow size is either too big or too small.");
 				else {
 					EVS_Bean eBean = (EVS_Bean) vRSel.elementAt(iRow);
 					//send it back if unable to obtion the concept
 					if (eBean == null || eBean.getLONG_NAME() == null) {
 						data.setStatusMsg(data.getStatusMsg()
 								+ "\\tUnable to obtain concept from the "
 								+ thisRow + " row of the search results.\\n"
 								+ "Please try again.");
 						continue;
 					}
 
 					String eBeanDB = eBean.getEVS_DATABASE();
 					//make sure it doesn't exist in the list
 					boolean isExist = false;
 					if (vList != null && vList.size() > 0) {
 						for (int k = 0; k < vList.size(); k++) {
 							EVS_Bean thisBean = (EVS_Bean) vList.elementAt(k);
 							String thisBeanDB = thisBean.getEVS_DATABASE();
 							if (thisBean.getCONCEPT_IDENTIFIER().equals(
 									eBean.getCONCEPT_IDENTIFIER())
 									&& eBeanDB.equals(thisBeanDB)) {
 								String acAct = thisBean
 										.getCON_AC_SUBMIT_ACTION();
 								//put it back if was deleted
 								if (acAct != null && acAct.equals("DEL")) {
 									thisBean.setCON_AC_SUBMIT_ACTION("INS");
 									vList.setElementAt(thisBean, k);
 								}
 								isExist = true;
 							}
 						}
 					}
 					if (isExist == false) {
 						eBean.setCON_AC_SUBMIT_ACTION("INS");
 						//get the evs user bean
 						EVS_UserBean eUser = (EVS_UserBean) data
 								.getCurationServlet().sessionData.EvsUsrBean; //(EVS_UserBean)session.getAttribute(EVSSearch.EVS_USER_BEAN_ARG);  //("EvsUserBean");
 						if (eUser == null)
 							eUser = new EVS_UserBean();
 
 						//get origin for cadsr result
 						String eDB = eBean.getEVS_DATABASE();
 						if (eDB != null && eBean.getEVS_ORIGIN() != null
 								&& eDB.equalsIgnoreCase("caDSR")) {
 							eDB = eBean.getVocabAttr(eUser, eBean
 									.getEVS_ORIGIN(), EVSSearch.VOCAB_NAME,
 									EVSSearch.VOCAB_DBORIGIN); // "vocabName", "vocabDBOrigin");
 							eBean.setEVS_DATABASE(eDB); //eBean.getEVS_ORIGIN());
 						}
 						vList.addElement(eBean);
 					}
 				}
 			}
 			vd.setReferenceConceptList(vList);
 			data.setVD(vd);
 		} catch (Exception e) {
 			logger.error("ERROR - ", e);
 		}
 	}
 
 	/**
 	 * stores the non evs parent reference information in evs bean and to parent list.
 	 * reference document is matched like this with the evs bean adn stored in parents vector as a evs bean
 	 * setCONCEPT_IDENTIFIER as document type (VD REFERENCE)
 	 * setLONG_NAME as document name
 	 * setEVS_DATABASE as Non_EVS text
 	 * setPREFERRED_DEFINITION as document text
 	 * setEVS_DEF_SOURCE as document url
 	 * @param sParName String Parence concept name
 	 * @param sParDef String parent concept definition
 	 * @param sParDefSource  String parent concept definition source
 	 * @param data PVForm object
 	 */
 	public void doNonEVSReference(String sParName, String sParDef,
 			String sParDefSource, PVForm data) {
 		try {
 			//document type  (concept code)
 			String sParCode = "VD REFERENCE";
 			//parent type (concept database)
 			String sParDB = "Non_EVS";
 
 			//make a string for view
 			String sParListString = sParName + "        " + sParDB;
 			if (sParListString == null)
 				sParListString = "";
 			VD_Bean vd = data.getVD();
 
 			//store the evs bean for the parent concepts in vector and in session.
 			Vector<EVS_Bean> vParentCon = vd.getReferenceConceptList();
 			if (vParentCon == null)
 				vParentCon = new Vector<EVS_Bean>();
 			EVS_Bean parBean = new EVS_Bean();
 			parBean.setCONCEPT_IDENTIFIER(sParCode); //doc type
 			parBean.setLONG_NAME(sParName); //doc name
 			parBean.setEVS_DATABASE(sParDB); //ref type (non evs)
 			parBean.setPREFERRED_DEFINITION(sParDef); //doc text
 			parBean.setEVS_DEF_SOURCE(sParDefSource); //doc url
 			parBean.setCON_AC_SUBMIT_ACTION("INS");
 			vParentCon.addElement(parBean);
 			vd.setReferenceConceptList(vParentCon);
 			data.setVD(vd);
 		} catch (RuntimeException e) {
 			logger.error("ERROR - ", e);
 		}
 	} // end
 
 	/**
 	 * to read the existing value meanings for the concepts selected from evs
 	 * @param vdpvList vector of pv beans
 	 * @param data pv form object
 	 * @return String list of vm names that existed in cadsr that are not exact match
 	 */
 	public String readExistingVMs(Vector<PV_Bean> vdpvList, PVForm data) {
 		String vNames = "";
 		try {
 			//make the string array of names
 			Hashtable<String, String> vmHash = new Hashtable<String, String>();
 			for (int i = 0; i < vdpvList.size(); i++) {
 				PV_Bean pv = vdpvList.elementAt(i);
 				if (pv.getPV_PV_IDSEQ().contains("EVS_")) {
 					//String vmName = pv.getPV_VM().getVM_SHORT_MEANING();
 					String vmName = pv.getPV_VM().getVM_LONG_NAME();
 					vNames = setName(vmHash, vmName, vNames, i);
 				}
 			}
 		
			if (!(vNames.equals(""))){
			  int index = vNames.lastIndexOf(",") ;
			  if (index>0){
			    vNames = vNames.substring(0,index);
			  }  
			}  
		
 			Vector<VM_Bean> vVMs = new Vector<VM_Bean>();
 			//query the database
 			if (!vNames.equals("")) {
 				//get the connection
 				VMAction vmact = new VMAction();
 				vVMs = vmact.searchMultipleVM(data.getCurationServlet()
 						.getConn(), vNames);
 				vNames = "";
 				//set the returned vms into the pv list
 				for (int i = 0; i < vVMs.size(); i++) {
 					VM_Bean vm = vVMs.elementAt(i);
 					vNames = getName(vmHash, vm, vdpvList, vNames);
 				}
 			}
 			//make sure emtpy string is passed if no matched vm found
 			if (vVMs.size() < 1)
 				vNames = "";
 		} catch (Exception e) {
 			logger.error("ERROR - : " + e.toString(), e);
 		}
 		return vNames;
 	}
 
 	/**
 	 * to get the names to search for; if name alrady exists, it appends number to the name to store the pv index
 	 * @param vmHash Hash table of vm names and pv index
 	 * @param vmName string to search for
 	 * @param vNames list of vm names to search for
 	 * @param i pv index number
 	 * @return list of vm names separated by comma
 	 */
 	private String setName(Hashtable<String, String> vmHash, String vmName,
 			String vNames, int i) {
 		String multiName = vmName;
 		int multi = 0;
 		//find the next one if multiple exists
 		while (vmHash.containsKey(multiName)) {
 			multi += 1;
 			multiName = vmName + "." + multi;
 		}
 		String ind = String.valueOf(i);
 		//add name and index to the hashtable and to names since it doesn't exist
 		if (vmName.equals(multiName)) {
 			vmHash.put(vmName, ind);
 			vNames +=  vmName + ",";
 					
 		} else
 			vmHash.put(multiName, ind);
 
 		return vNames;
 	}
 
 	/**
 	 * to get the name of the existing vms ; reset the pv beans with the existing vm
 	 * @param vmHash hash table of vm names and pv index
 	 * @param vm existing vm bean
 	 * @param vdpv list of pvs existed for vd
 	 * @param vNames existed vm list
 	 * @return string existed vm list separated by comma
 	 */
 	private String getName(Hashtable<String, String> vmHash, VM_Bean vm,
 			Vector<PV_Bean> vdpv, String vNames) {
 		String vmName = vm.getVM_LONG_NAME();
 		int multi = 0;
 		String multiName = vmName;
 		boolean isExact = false;
 		do {
 			int i = Integer.parseInt(vmHash.get(multiName));
 			PV_Bean pv = vdpv.elementAt(i);
 			//get the exact match
 			VMAction vmact = new VMAction();
 			if (vmact.checkExactMatch(vdpv.elementAt(i).getPV_VM(), vm) != null)
 				isExact = true;
 			//reset the pv
 			pv.setPV_VM(vm);
 			vdpv.setElementAt(pv, i);
 			//check if another with same vm exists
 			multi += 1;
 			multiName = vmName + "." + multi;
 		} while (vmHash.containsKey(multiName));
 		//append the names to display if not the exact match
 		if (!isExact) {
 			if (!vNames.equals(""))
 				vNames += ", ";
 			vNames += vmName;
 		}
 		//return chagnged names to display
 		return vNames;
 	}
 
 }//end of class
