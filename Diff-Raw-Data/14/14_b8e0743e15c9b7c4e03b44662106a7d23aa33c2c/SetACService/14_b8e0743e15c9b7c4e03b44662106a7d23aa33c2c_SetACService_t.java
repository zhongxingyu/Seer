 
 // Copyright (c) 2000 ScenPro, Inc.
 package com.scenpro.NCICuration;
 
 import java.io.*;
 import java.io.Serializable;
 import java.math.*;
 import java.sql.*;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import oracle.jdbc.driver.*;
 import org.apache.log4j.*;
 
 /**
  * This utility class is used during the validation of the create/edit action of the tool.
  * <P>
  * @author Sumana Hegde
  * @version 3.0
  */
 
  /*
 The CaCORE Software License, Version 3.0 Copyright 2002-2005 ScenPro, Inc. (ScenPro)  
 Copyright Notice.  The software subject to this notice and license includes both
 human readable source code form and machine readable, binary, object code form
 (the CaCORE Software).  The CaCORE Software was developed in conjunction with
 the National Cancer Institute (NCI) by NCI employees and employees of SCENPRO.
 To the extent government employees are authors, any rights in such works shall
 be subject to Title 17 of the United States Code, section 105.    
 This CaCORE Software License (the License) is between NCI and You.  You (or Your)
 shall mean a person or an entity, and all other entities that control, are 
 controlled by, or are under common control with the entity.  Control for purposes
 of this definition means (i) the direct or indirect power to cause the direction
 or management of such entity, whether by contract or otherwise, or (ii) ownership
 of fifty percent (50%) or more of the outstanding shares, or (iii) beneficial 
 ownership of such entity.  
 This License is granted provided that You agree to the conditions described below.
 NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up, no-charge,
 irrevocable, transferable and royalty-free right and license in its rights in the
 CaCORE Software to (i) use, install, access, operate, execute, copy, modify, 
 translate, market, publicly display, publicly perform, and prepare derivative 
 works of the CaCORE Software; (ii) distribute and have distributed to and by 
 third parties the CaCORE Software and any modifications and derivative works 
 thereof; and (iii) sublicense the foregoing rights set out in (i) and (ii) to 
 third parties, including the right to license such rights to further third parties.
 For sake of clarity, and not by way of limitation, NCI shall have no right of 
 accounting or right of payment from You or Your sublicensees for the rights 
 granted under this License.  This License is granted at no charge to You.
 1.	Your redistributions of the source code for the Software must retain the above
 copyright notice, this list of conditions and the disclaimer and limitation of
 liability of Article 6, below.  Your redistributions in object code form must
 reproduce the above copyright notice, this list of conditions and the disclaimer
 of Article 6 in the documentation and/or other materials provided with the 
 distribution, if any.
 2.	Your end-user documentation included with the redistribution, if any, must 
 include the following acknowledgment: This product includes software developed 
 by SCENPRO and the National Cancer Institute.  If You do not include such end-user
 documentation, You shall include this acknowledgment in the Software itself, 
 wherever such third-party acknowledgments normally appear.
 3.	You may not use the names "The National Cancer Institute", "NCI" ScenPro, Inc.
 and "SCENPRO" to endorse or promote products derived from this Software.  
 This License does not authorize You to use any trademarks, service marks, trade names,
 logos or product names of either NCI or SCENPRO, except as required to comply with
 the terms of this License.
 4.	For sake of clarity, and not by way of limitation, You may incorporate this
 Software into Your proprietary programs and into any third party proprietary 
 programs.  However, if You incorporate the Software into third party proprietary
 programs, You agree that You are solely responsible for obtaining any permission
 from such third parties required to incorporate the Software into such third party
 proprietary programs and for informing Your sublicensees, including without 
 limitation Your end-users, of their obligation to secure any required permissions
 from such third parties before incorporating the Software into such third party
 proprietary software programs.  In the event that You fail to obtain such permissions,
 You agree to indemnify NCI for any claims against NCI by such third parties, 
 except to the extent prohibited by law, resulting from Your failure to obtain
 such permissions.
 5.	For sake of clarity, and not by way of limitation, You may add Your own 
 copyright statement to Your modifications and to the derivative works, and You 
 may provide additional or different license terms and conditions in Your sublicenses
 of modifications of the Software, or any derivative works of the Software as a 
 whole, provided Your use, reproduction, and distribution of the Work otherwise 
 complies with the conditions stated in this License.
 6.	THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, 
 NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED.  
 IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE, SCENPRO, OR THEIR AFFILIATES 
 BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
 LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 public class SetACService implements Serializable
 {
   UtilService m_util = new UtilService();
   NCICurationServlet m_servlet;
   Logger logger = Logger.getLogger(GetACSearch.class.getName());
   Vector m_vRetWFS = new Vector();
  /**
   * Instantiate the class
   */
   public SetACService(NCICurationServlet CurationServlet)
   {
     m_servlet = CurationServlet;
     
     m_vRetWFS.addElement("RETIRED ARCHIVED");
     m_vRetWFS.addElement("RETIRED DELETED");
     m_vRetWFS.addElement("RETIRED PHASED OUT");
     m_vRetWFS.addElement("RETIRED WITHDRAWN");
   }
  /**
   * To check validity of the data for Data Element component before submission, called from NCICurationServlet.
   * Validation is done against Database restriction and ISO1179 standards.
   * Some validations are seperated according to Edit or Create actions.
   * calls various methods to get validity messages and store it into the vector.
   * Valid/Invalid Messages are stored in request Vector 'vValidate' along with the field, data.
   *
   * @param req The HttpServletRequest object.
   * @param res HttpServletResponse object.
   * @param m_DE DataElement Bean.
   * @param getAC reference to GetACService class.
   *
   * @throws IOException  If an input or output exception occurred
   * @throws ServletException  If servlet exception occured
   */
   public void setValidatePageValuesDE(HttpServletRequest req,
           HttpServletResponse res, DE_Bean m_DE, GetACService getAC) throws ServletException,IOException
   {
       HttpSession session = req.getSession();
       Vector vValidate = new Vector();
       String s;
       String s2;
       boolean bMandatory = true;
       boolean bNotMandatory = false;
       String strInValid = "";
       String strUnique = "";
       int iLengthLimit = 30;
       int iNoLengthLimit = -1;
       String sDEAction = (String)session.getAttribute("DEAction");
       String sOriginAction = (String)session.getAttribute("originAction");
       String sMenu = (String)session.getAttribute("MenuAction");
 
       //check edit or create
       if (sDEAction.equals("EditDE"))
          sDEAction = "Edit";
       else
          sDEAction = "Create";
 
       String sUser = (String)session.getAttribute("Username");
       // mandatory
       s = m_DE.getDE_CONTEXT_NAME();
       if (s == null) s = "";
       String sID = m_DE.getDE_CONTE_IDSEQ();
 
           if ((sUser != null) && (sID != null))
               strInValid = checkWritePermission("de", sUser, sID, getAC);
           if (strInValid.equals("")) session.setAttribute("sDefaultContext", s);
           setValPageVector(vValidate, "Context", s, bMandatory, iNoLengthLimit, strInValid, sOriginAction);
 
           s = m_DE.getDE_DEC_NAME();
           if (s == null) s = "";
           //strUnique = checkUniqueInContext("Version", "DE", m_DE, null, null, getAC, sDEAction);
           strUnique = this.checkUniqueDECVDPair(m_DE, getAC, sDEAction, sMenu);
           setValPageVector(vValidate, "Data Element Concept", s, bMandatory, iNoLengthLimit, strUnique, sOriginAction);
 
           //strUnique is same as the above.
           s = m_DE.getDE_VD_NAME();
           if (s == null) s = "";
           setValPageVector(vValidate, "Value Domain", s, bMandatory, iNoLengthLimit, strUnique, sOriginAction);
 
           s = m_DE.getDE_LONG_NAME();
           if (s == null) s = "";
           strInValid = "";
           setValPageVector(vValidate, "Long Name", s, bMandatory, 255, strInValid, sOriginAction);
 
           s = m_DE.getDE_PREFERRED_NAME();
           if (s == null) s = "";
           strInValid = "";
           //checks uniuqe in the context and name differred for Released
           if (s.equals("")== false)
           {
              if (!s.equalsIgnoreCase("(Generated by the System)"))
                 strInValid = checkUniqueInContext("Name", "DE", m_DE, null, null, getAC, sDEAction);
              DE_Bean oldDE = (DE_Bean)session.getAttribute("oldDEBean");
              if (oldDE != null && sDEAction.equals("Edit"))
              {
                 String oName = oldDE.getDE_PREFERRED_NAME();
                 String oStatus = oldDE.getDE_ASL_NAME();
                 strInValid = strInValid + checkNameDiffForReleased(oName, s, oStatus);
              }
           }
           setValPageVector(vValidate, "Preferred Name", s, bMandatory, 30, strInValid, sOriginAction);
 
       s = m_DE.getDE_PREFERRED_DEFINITION();
       if (s == null) s = "";
       strInValid = "";
         setValPageVector(vValidate, "Definition", s, bMandatory, 2000, strInValid, sOriginAction);
 
       //workflow status
       s = m_DE.getDE_ASL_NAME();
       if (s == null) s = "";
   
       setValPageVector(vValidate, "Workflow Status", s, bMandatory, 20, "", sOriginAction);
 
       s = m_DE.getDE_VERSION();
       if (s == null) s = "";
       strInValid = "";
       if (s.equals("")==false)
       {
          //strInValid = strUnique + this.checkVersionDimension(s);   //checkValueIsNumeric(s);                
          strInValid = this.checkVersionDimension(s);   //checkValueIsNumeric(s);                
          DE_Bean oldDE = (DE_Bean)session.getAttribute("oldDEBean");
          String menuAction = (String)session.getAttribute("MenuAction");
          if (oldDE != null && menuAction.equals("NewDEVersion"))
          {
             String oVer = oldDE.getDE_VERSION();
             if (s.equals(oVer))
                strInValid = strInValid + "Must change the version number to create a new version.\n";
             //check if new verison is unique within the public id
             strInValid = strInValid + checkUniqueInContext("Version", "DE", m_DE, null, null, getAC, sDEAction);
          }
       }
       setValPageVector(vValidate, "Version", s, bMandatory, iNoLengthLimit, strInValid, sOriginAction);
       //make it empty 
       strInValid = "";
 
       //registration status
       s = m_DE.getDE_REG_STATUS();
       if (s == null) s = "";
       strInValid = "";
  
       if (s.equalsIgnoreCase("Standard") || s.equalsIgnoreCase("Candidate") || s.equalsIgnoreCase("Proposed"))
         strInValid = this.checkDECOCExist(m_DE.getDE_DEC_IDSEQ(), req, res);
       setValPageVector(vValidate, "Registration Status", s, bNotMandatory, 50, strInValid, sOriginAction);
 
       s = m_DE.getDE_BEGIN_DATE();
       if (s == null) s = "";
       if (sDEAction.equals("Edit"))
           setValPageVector(vValidate, "Effective Begin Date", s, bNotMandatory, iNoLengthLimit, "", sOriginAction);
       else
           setValPageVector(vValidate, "Effective Begin Date", s, bMandatory, iNoLengthLimit, "", sOriginAction);
 
       s = m_DE.getDE_END_DATE();
       if (s == null) s = "";
       String wfs = m_DE.getDE_ASL_NAME();
       wfs = wfs.toUpperCase();
      // if (wfs.equals("RETIRED ARCHIVED") || wfs.equals("RETIRED DELETED") || wfs.equals("RETIRED PHASED OUT"))
       if (m_vRetWFS.contains(wfs))
         setValPageVector(vValidate, "Effective End Date", s, bMandatory, iNoLengthLimit, "", sOriginAction);
       else
         setValPageVector(vValidate, "Effective End Date", s, bNotMandatory, iNoLengthLimit, "", sOriginAction);
 
       s = m_DE.getDOC_TEXT_LONG_NAME();
       if (s == null) s = "";
       setValPageVector(vValidate, "Document Text", s, bNotMandatory, iNoLengthLimit, "", sOriginAction);        
 
       //add cs-csi to validate page
       String sCS = ""; 
       String sCSI = "";
       String sAC = "";
       if(m_DE.getDE_CS_NAME() != null)
       {
         Vector vACCSI = (Vector)m_DE.getDE_AC_CSI_VECTOR();    //req.getAttribute("vACCSIList");
         if (vACCSI != null && !vACCSI.isEmpty())
         {
           for (int i=0; i<vACCSI.size(); i++)
           {
             AC_CSI_Bean accsiBean = (AC_CSI_Bean)vACCSI.elementAt(i);
             CSI_Bean csiBean = (CSI_Bean)accsiBean.getCSI_BEAN();
             if (i>0) 
             {
               sCS += ", ";
               sCSI += ", ";
               sAC += ", ";
             }
             sCS += csiBean.getCSI_CS_LONG_NAME();  //accsiBean.getCS_LONG_NAME();
             sCSI += csiBean.getCSI_NAME(); //accsiBean.getCSI_NAME();
             sAC += accsiBean.getAC_LONG_NAME();
           }
         }
       }
  
           setValPageVector(vValidate, "Classification Scheme", sCS, bNotMandatory, iNoLengthLimit, "", sOriginAction);
           strInValid = "";
           if (sCS.equals("")==false && (sCSI.equals("") || sCSI == null))   //it is a pair
             strInValid = "Items must be selected for the selected Classification Scheme.";
           setValPageVector(vValidate, "Classification Scheme Items", sCSI, bNotMandatory, iNoLengthLimit, strInValid, sOriginAction);
 
   
       s = m_DE.getDE_SOURCE();
       if (s == null) s = "";
       setValPageVector(vValidate, "Data Element Origin", s, bNotMandatory, iNoLengthLimit, "", sOriginAction);
 
       s = m_DE.getDE_CHANGE_NOTE();
       if (s == null) s = "";
       setValPageVector(vValidate, "Comment/Change Note", s, bNotMandatory, 2000, "", sOriginAction);
      
       // add DDE info to DE validate page
      // if (!sOriginAction.equals("BlockEditDE") && !sOriginAction.equals("CreateNewDEFComp"))
       if (!sOriginAction.equals("CreateNewDEFComp"))
         addDDEToDEValidatePage(req, res, vValidate, sOriginAction);
 
       // finaly, send vector to JSP
       req.setAttribute("vValidate", vValidate);
   } // end of setValidatePageValues
 
  /**
   * To add DDE info to DE validate vector, called from setValidatePageValues.
   * All data are valid because they are either pick from list or no validate rule
   *
   * @param req The HttpServletRequest object.
   * @param res HttpServletResponse object.
   * @param Vector vValidate.
   * @param String sOriginAction.
   *
   * @throws IOException  If an input or output exception occurred
   * @throws ServletException  If servlet exception occured
   */
   public void addDDEToDEValidatePage(HttpServletRequest req,
           HttpServletResponse res, Vector vValidate, String sOriginAction) throws ServletException,IOException
   {
       String strInValid = "valid";
       boolean bNotMandatory = false;
       boolean bMandatory = true;
       int iNoLengthLimit = -1;
 
       HttpSession session = req.getSession();
       String sDDERepTypes[] = req.getParameterValues("selRepType");
       String sRepType = sDDERepTypes[0];
       String sRule = (String)req.getParameter("DDERule");
       String sMethod = (String)req.getParameter("DDEMethod");
       String sConcatChar = (String)req.getParameter("DDEConcatChar");
 
       if(sRepType == null || sRepType.length() < 1)
           return;
       else
           setValPageVector(vValidate, "Representation Type", sRepType, bMandatory, iNoLengthLimit, "", sOriginAction);
 
       if(sRule == null)
           sRule = "";
       setValPageVector(vValidate, "Rule", sRule, bMandatory, iNoLengthLimit, "", sOriginAction);
 
       if(sMethod == null)
           sMethod = "";
       setValPageVector(vValidate, "Method", sMethod, bNotMandatory, iNoLengthLimit, "", sOriginAction);
 
       if(sConcatChar == null)
           sConcatChar = "";
       setValPageVector(vValidate, "Concatenate Character", sConcatChar, bNotMandatory, iNoLengthLimit, "", sOriginAction);
 
       String sDEComps[] = req.getParameterValues("selDECompHidden");
       String sDECompOrders[] = req.getParameterValues("selDECompOrderHidden");
       Vector vDEComp = new Vector();
       Vector vDECompOrder = new Vector();
       String sName = "";
       String sOrder = "";
       if(sDEComps != null)
       {
         for (int i = 0; i<sDEComps.length; i++) 
         {
             String sDEComp = sDEComps[i];
             String sDECompOrder = sDECompOrders[i];
             if(i < sDEComps.length - 1)
             {
                sName = sName + sDEComp + ", ";
                sOrder = sOrder + sDECompOrder + ", ";
             }
             else
             {
                sName = sName + sDEComp;
                sOrder = sOrder + sDECompOrder;
             }
         }
         setValPageVector(vValidate, "Data Element Component", sName, bNotMandatory, iNoLengthLimit, "", sOriginAction);
         setValPageVector(vValidate, "Data Element Component Order", sOrder, bNotMandatory, iNoLengthLimit, "", sOriginAction);
       }
   }  // end of addDDEToDEValidatePage
    
  /**
   * To check validity of the data for Data Element Concept component before submission, called from NCICurationServlet.
   * Validation is done against Database restriction and ISO1179 standards.
   * Some validations are seperated according to Edit or Create actions.
   * calls various methods to get validity messages and store it into the vector.
   * Valid/Invalid Messages are stored in request Vector 'vValidate' along with the field, data.
   *
   * @param req The HttpServletRequest object.
   * @param res HttpServletResponse object.
   * @param m_DEC DataElementConcept Bean.
   * @param getAC reference to GetACService class.
   *
   * @throws IOException  If an input or output exception occurred
   * @throws ServletException  If servlet exception occured
   */
   public void setValidatePageValuesDEC(HttpServletRequest req,
           HttpServletResponse res, DEC_Bean m_DEC, EVS_Bean m_OC, EVS_Bean m_PC, 
            GetACService getAC, EVS_Bean m_OCQ, EVS_Bean m_PCQ) 
           throws ServletException,IOException, Exception
   {
 //System.out.println("setValidatePageValuesDEC");
       HttpSession session = req.getSession();
       GetACSearch getACSer = new GetACSearch(req, res, m_servlet);
       String sDECAction = (String)session.getAttribute("DECAction");
       String sUser = (String)session.getAttribute("Username");
       String sOriginAction = (String)session.getAttribute("originAction");
       //check edit or create
       if (sDECAction.equals("EditDEC"))
          sDECAction = "Edit";
       else
          sDECAction = "Create";
 
       Vector vValidate = new Vector();
       String s;
       boolean bMandatory = true;
       boolean bNotMandatory = false;
       String strInValid = "";
       int iLengthLimit = 30;
       int iNoLengthLimit = -1;
 
       // mandatory for both Edit and New
       s = m_DEC.getDEC_CONTEXT_NAME();
       String sID = m_DEC.getDEC_CONTE_IDSEQ();
  // System.out.println("setValidatePageValuesDEC0");
       if ((sUser != null) && (sID != null))
         strInValid = checkWritePermission("dec", sUser, sID, getAC);
       if (strInValid.equals("")) session.setAttribute("sDefaultContext", s);
       setValPageVector(vValidate, "Context", s, bMandatory, 36, strInValid, sOriginAction);
       //validate naming components
       vValidate = this.setValidateNameComp(vValidate, "DataElementConcept", req, res, m_DEC, m_OC, m_PC, null, null);
  // System.out.println("setValidatePageValuesDEC2");
       s = m_DEC.getDEC_LONG_NAME();
       if (s == null) s = "";
       strInValid = "";
       setValPageVector(vValidate, "Long Name", s, bMandatory, 255, strInValid, sOriginAction);
 
       s = m_DEC.getDEC_PREFERRED_NAME();
       if (s == null) s = "";
       strInValid = "";
       //check if valid system generated name
       String nameType = m_DEC.getAC_PREF_NAME_TYPE();
       if (nameType != null && nameType.equals("SYS"))
       {
         String ocID = m_DEC.getDEC_OCL_IDSEQ();
         String propID = m_DEC.getDEC_PROPL_IDSEQ();
         //no object and no property
         if ((ocID == null || ocID.equals("")) && (propID == null || propID.equals("")))
         {
           strInValid = "Requires Object Class or Property to create System Generated Preferred Name.";
           s = "";
         }
       }
       //checks uniuqe in the context and name differred for Released
       if (!s.equals(""))
       {
          if (!s.equalsIgnoreCase("(Generated by the System)"))
             strInValid = checkUniqueInContext("Name", "DEC", null, m_DEC, null, getAC, sDECAction);
          DEC_Bean oldDEC = (DEC_Bean)session.getAttribute("oldDECBean");
          if (oldDEC != null && sDECAction.equals("Edit"))
          {
             String oName = oldDEC.getDEC_PREFERRED_NAME();
             String oStatus = oldDEC.getDEC_ASL_NAME();
             strInValid = strInValid + checkNameDiffForReleased(oName, s, oStatus);
          }
       }
       setValPageVector(vValidate, "Preferred Name", s, bMandatory, 30, strInValid, sOriginAction);
 
       s = m_DEC.getDEC_PREFERRED_DEFINITION();
       if (s == null) s = "";
       strInValid = "";
         setValPageVector(vValidate, "Definition", s, bMandatory, 2000, strInValid, sOriginAction);
       //validation for both edit and DEc
       s = m_DEC.getDEC_CD_NAME();
       if (s == null) s = "";
       setValPageVector(vValidate, "Conceptual Domain", s, bMandatory, iNoLengthLimit, "", sOriginAction);
 //System.out.println("setValidatePageValuesDEC3");
       s = m_DEC.getDEC_ASL_NAME();
       if (s == null) s = "";  
       if(s.equals("Released") || s.equals("RELEASED"))
       {
         if (sDECAction.equals("Edit"))
           strInValid = checkOCPropWorkFlowStatuses(req, res, m_DEC.getDEC_OCL_IDSEQ(), m_DEC.getDEC_PROPL_IDSEQ(), strInValid);
         else if(sDECAction.equals("Create"))
         {
           EVS_Bean OCBean = (EVS_Bean)session.getAttribute("m_OC");
           EVS_Bean PCBean = (EVS_Bean)session.getAttribute("m_PC");
           String oc_id = "";
           String prop_id = "";
           if(m_OC != null)
             oc_id = m_OC.getIDSEQ();
           if(m_PC != null)
             prop_id = m_PC.getIDSEQ();
           strInValid = checkOCPropWorkFlowStatuses(req, res, oc_id, prop_id, strInValid);
         }
       }
       setValPageVector(vValidate, "Workflow Status", s, bMandatory, 20, strInValid, sOriginAction);   
    
       s = m_DEC.getDEC_VERSION();
       strInValid = "";
       if (s.equals("")==false)
       {
          strInValid = this.checkVersionDimension(s);   //checkValueIsNumeric(s);
          DEC_Bean oldDEC = (DEC_Bean)session.getAttribute("oldDECBean");
          String menuAction = (String)session.getAttribute("MenuAction");
          if (oldDEC != null && menuAction.equals("NewDECVersion"))
          {
             String oVer = oldDEC.getDEC_VERSION();
             if (s.equals(oVer))
                strInValid = strInValid + "Must change the version number to create a new version.\n";
             //check if new verison is unique within the public id
             strInValid = strInValid + checkUniqueInContext("Version", "DEC", null, m_DEC, null, getAC, sDECAction);
          }
       }
       setValPageVector(vValidate, "Version", s, bMandatory, iNoLengthLimit, strInValid, sOriginAction);
 
       s = m_DEC.getDEC_BEGIN_DATE();
       if (s == null) s = "";
       if (sDECAction.equals("Edit"))  // || sOriginAction.equals("BlockEditDEC"))
          setValPageVector(vValidate, "Effective Begin Date", s, bNotMandatory, iNoLengthLimit, "", sOriginAction);
       else
          setValPageVector(vValidate, "Effective Begin Date", s, bMandatory, iNoLengthLimit, "", sOriginAction);
 
       s = m_DEC.getDEC_END_DATE();
       if (s == null) s = "";
       String wfs = m_DEC.getDEC_ASL_NAME();
       wfs = wfs.toUpperCase();
      // if (wfs.equals("RETIRED ARCHIVED") || wfs.equals("RETIRED DELETED") || wfs.equals("RETIRED PHASED OUT"))
       if (m_vRetWFS.contains(wfs))
         setValPageVector(vValidate, "Effective End Date", s, bMandatory, iNoLengthLimit, "", sOriginAction);
       else
         setValPageVector(vValidate, "Effective End Date", s, bNotMandatory, iNoLengthLimit, "", sOriginAction);
 
       //add cs-csi to validate page
       String sCS = ""; 
       String sCSI = "";
       String sAC = "";
       if(m_DEC.getDEC_CS_NAME() != null)
       {
         Vector vACCSI = (Vector)m_DEC.getDEC_AC_CSI_VECTOR();    //req.getAttribute("vACCSIList");
         if (vACCSI != null && !vACCSI.isEmpty())
         {
           for (int i=0; i<vACCSI.size(); i++)
           {
             AC_CSI_Bean accsiBean = (AC_CSI_Bean)vACCSI.elementAt(i);
             CSI_Bean csiBean = (CSI_Bean)accsiBean.getCSI_BEAN();
             if (i>0) 
             {
               sCS += ", ";
               sCSI += ", ";
               sAC +=", ";
             }
             sCS += csiBean.getCSI_CS_LONG_NAME();  //accsiBean.getCS_LONG_NAME();
             sCSI += csiBean.getCSI_NAME(); // accsiBean.getCSI_NAME();
             sAC += accsiBean.getAC_LONG_NAME();
           }
         }
       }
   
       setValPageVector(vValidate, "Classification Scheme", sCS, bNotMandatory, iNoLengthLimit, "", sOriginAction);
       strInValid = "";
       if (sCS.equals("")==false && (sCSI.equals("") || sCSI == null))   //it is a pair
         strInValid = "Items must be selected for the selected Classification Scheme.";
       setValPageVector(vValidate, "Classification Scheme Items", sCSI, bNotMandatory, iNoLengthLimit, strInValid, sOriginAction);
   
       s = m_DEC.getDEC_SOURCE();
       if (s == null) s = "";
       setValPageVector(vValidate, "Data Element Concept Origin", s, bNotMandatory, iNoLengthLimit, "", sOriginAction);
 
       s = m_DEC.getDEC_CHANGE_NOTE();
       if (s == null) s = "";
       setValPageVector(vValidate, "Comment/Change Note", s, bNotMandatory, 2000, "", sOriginAction);
         
       // finaly, send vector to JSP
       req.setAttribute("vValidate", vValidate);
 // System.err.println("done validateDEC");
   }
 
  /**
   * To check validity of the data for Value Domain component before submission, called from NCICurationServlet.
   * Validation is done against Database restriction and ISO1179 standards.
   * Some validations are seperated according to Edit or Create actions.
   * calls various methods to get validity messages and store it into the vector.
   * Valid/Invalid Messages are stored in request Vector 'vValidate' along with the field, data.
   *
   * @param req The HttpServletRequest object.
   * @param res HttpServletResponse object.
   * @param m_VD ValueDomain Bean.
   * @param getAC reference to GetACService class.
   *
   * @throws IOException  If an input or output exception occurred
   * @throws ServletException  If servlet exception occured
   */
   public void setValidatePageValuesVD(HttpServletRequest req, HttpServletResponse res, 
         VD_Bean m_VD, EVS_Bean m_OC, EVS_Bean m_PC, EVS_Bean m_REP, EVS_Bean m_OCQ, 
         EVS_Bean m_PCQ, EVS_Bean m_REPQ, GetACService getAC) throws ServletException,IOException, Exception
   {
 //System.out.println("setValidatePageValuesVD");
       HttpSession session = req.getSession();
       GetACSearch getACSer = new GetACSearch(req, res, m_servlet);
       Vector vValidate = new Vector();
       String s;
       boolean bMandatory = true;
       boolean bNotMandatory = false;
       String strInValid = "";
       int iLengthLimit = 30;
       int iNoLengthLimit = -1;
       String sVDAction = (String)session.getAttribute("VDAction");
       String sOriginAction = (String)session.getAttribute("originAction");
       String sUser = (String)session.getAttribute("Username");
 
       //check edit or create
       if (sVDAction.equals("EditVD"))
          sVDAction = "Edit";
       else
          sVDAction = "Create";
 
       // mandatory for both EDit + editSQL); and Create
       s = m_VD.getVD_CONTEXT_NAME();
       String sID = m_VD.getVD_CONTE_IDSEQ();
       if ((sUser != null) && (sID != null))
         strInValid = checkWritePermission("vd", sUser, sID, getAC);
       if (strInValid.equals("")) session.setAttribute("sDefaultContext", s);
         setValPageVector(vValidate, "Context", s, bMandatory, 36, strInValid, sOriginAction);
 
       s = m_VD.getVD_TYPE_FLAG();
       String q = "";
       if (s == null) s = "";
       if(s.equals("N"))
         setValPageVector(vValidate, "Type", "Non-Enumerated", bMandatory, iNoLengthLimit, "", sOriginAction);
       else if(s.equals("E"))
         setValPageVector(vValidate, "Type", "Enumerated", bMandatory, iNoLengthLimit, "", sOriginAction);
       else
         setValPageVector(vValidate, "Type", s, bMandatory, iNoLengthLimit, "", sOriginAction);
 
       //validate hte naming component
       vValidate = this.setValidateNameComp(vValidate, "ValueDomain", req, res, null, null, null, m_VD, m_REP);
 
       s = m_VD.getVD_LONG_NAME();
       if (s == null) s = "";
       strInValid = "";
       setValPageVector(vValidate, "Long Name", s, bMandatory, 255, strInValid, sOriginAction);
 
       s = m_VD.getVD_PREFERRED_NAME();
       if (s == null) s = "";
       strInValid = "";
       //checks uniuqe in the context and name differred for Released
       if (!s.equals(""))
       {
          if (!s.equalsIgnoreCase("(Generated by the System)"))
             strInValid = checkUniqueInContext("Name", "VD", null, null, m_VD, getAC, sVDAction);
          VD_Bean oldVD = (VD_Bean)session.getAttribute("oldVDBean");
          if (oldVD != null && sVDAction.equals("Edit"))
          {
             String oName = oldVD.getVD_PREFERRED_NAME();
             String oStatus = oldVD.getVD_ASL_NAME();
             strInValid = strInValid + checkNameDiffForReleased(oName, s, oStatus);
          }
       }
       setValPageVector(vValidate, "Preferred Name", s, bMandatory, 30, strInValid, sOriginAction);
 
       s = m_VD.getVD_PREFERRED_DEFINITION();
       if (s == null) s = "";
       strInValid = "";
       setValPageVector(vValidate, "Definition", s, bMandatory, 2000, strInValid, sOriginAction);
 
       //same for both edit and new
       s = m_VD.getVD_CD_NAME();
       if (s == null) s = "";
   
       setValPageVector(vValidate, "Conceptual Domain", s, bMandatory, iNoLengthLimit, "", sOriginAction);
   
       s = m_VD.getVD_ASL_NAME();
       if (s == null) s = "";  
       setValPageVector(vValidate, "Workflow Status", s, bMandatory, 20, "", sOriginAction);
 
       s = m_VD.getVD_VERSION();
       if (s == null) s = "";
       strInValid = "";
       if (s.equals("")==false)
       {
           strInValid = this.checkVersionDimension(s);   //checkValueIsNumeric(s);
           VD_Bean oldVD = (VD_Bean)session.getAttribute("oldVDBean");
           String menuAction = (String)session.getAttribute("MenuAction");
           if (oldVD != null && menuAction.equals("NewVDVersion"))
           {
               String oVer = oldVD.getVD_VERSION();
               if (s.equals(oVer))
                  strInValid = strInValid + "Must change the version number to create a new version.\n";
             //check if new verison is unique within the public id
             strInValid = strInValid + checkUniqueInContext("Version", "VD", null, null, m_VD, getAC, sVDAction);
           }
       }
       setValPageVector(vValidate, "Version", s, bMandatory, iNoLengthLimit, strInValid, sOriginAction);
 
       s = m_VD.getVD_DATA_TYPE();
       if (s == null) s = "";
       setValPageVector(vValidate, "Data Type", s, bMandatory, 20, "", sOriginAction);
    
       //get the parent concept
       s = "";
       strInValid = "";
       Vector vParList = (Vector)session.getAttribute("VDParentConcept");
       InsACService insAC = new InsACService(req, res, m_servlet);
       if(vParList != null)
             strInValid = checkConceptCodeExistsInOtherDB(vParList, insAC, null);
       if (vParList != null && vParList.size()>0)
       {
 //System.out.println("val VD vParList.size(): " + vParList.size());
         for (int i =0; i<vParList.size(); i++)
         {
           EVS_Bean parBean = (EVS_Bean)vParList.elementAt(i);
           if (!parBean.getCON_AC_SUBMIT_ACTION().equals("DEL"))
           {
             String parString = "";
             if (parBean.getLONG_NAME() != null) parString = parBean.getLONG_NAME() + "   ";
             if (parBean.getEVS_DATABASE() != null)
             {
               //do not add this if non evs
               if (parBean.getNCI_CC_VAL() != null && !parBean.getEVS_DATABASE().equals("Non_EVS")) 
                   parString += parBean.getNCI_CC_VAL() + "   ";
               parString += parBean.getEVS_DATABASE();
             }
             if (!parString.equals(""))
             {
               if (s.equals("")) s = parString;
               else s = s + ", " + parString;
             }
           }
         }
       }
 
       setValPageVector(vValidate, "Parent Concept", s, bNotMandatory, iNoLengthLimit, strInValid, sOriginAction);
       //no need to put values as it does not exist.
       if (m_VD.getVD_TYPE_FLAG().equals("E"))
         vValidate = this.validateVDPVS(req, res, m_VD, vValidate, sOriginAction);
 
       s = m_VD.getVD_BEGIN_DATE();
       if (s == null) s = "";
       if (sVDAction.equals("Edit"))  // || sOriginAction.equals("BlockEditVD"))
           setValPageVector(vValidate, "Effective Begin Date", s, bNotMandatory, iNoLengthLimit, "", sOriginAction);
       else
           setValPageVector(vValidate, "Effective Begin Date", s, bMandatory, iNoLengthLimit, "", sOriginAction);
 
       s = m_VD.getVD_END_DATE();
       if (s == null) s = "";
       String wfs = m_VD.getVD_ASL_NAME();
       wfs = wfs.toUpperCase();
      // if (wfs.equals("RETIRED ARCHIVED") || wfs.equals("RETIRED DELETED") || wfs.equals("RETIRED PHASED OUT"))
       if (m_vRetWFS.contains(wfs))
         setValPageVector(vValidate, "Effective End Date", s, bMandatory, iNoLengthLimit, "", sOriginAction);
       else
         setValPageVector(vValidate, "Effective End Date", s, bNotMandatory, iNoLengthLimit, "", sOriginAction);
 
       //get value domain other attributes
       vValidate = this.setValidateVDOtherAttr(vValidate, m_VD, sOriginAction);
       
       //add cs-csi to validate page
       String sCS = ""; 
       String sCSI = "";
       String sAC = "";
       if(m_VD.getVD_CS_NAME() != null)
       {
         Vector vACCSI = (Vector)m_VD.getVD_AC_CSI_VECTOR();    //req.getAttribute("vACCSIList");
         if (vACCSI != null && !vACCSI.isEmpty())
         {
           for (int i=0; i<vACCSI.size(); i++)
           {
             AC_CSI_Bean accsiBean = (AC_CSI_Bean)vACCSI.elementAt(i);
             CSI_Bean csiBean = (CSI_Bean)accsiBean.getCSI_BEAN();
             if (i>0) 
             {
               sCS += ", ";
               sCSI += ", ";
               sAC += ", ";
             }
             sCS += csiBean.getCSI_CS_LONG_NAME();  //accsiBean.getCS_LONG_NAME();
             sCSI += csiBean.getCSI_NAME();  // accsiBean.getCSI_NAME();
             sAC += accsiBean.getAC_LONG_NAME();
           }
         }
       }
       setValPageVector(vValidate, "Classification Scheme", sCS, bNotMandatory, iNoLengthLimit, "", sOriginAction);
       strInValid = "";
       if (sCS.equals("")==false && (sCSI.equals("") || sCSI == null))   //it is a pair
         strInValid = "Items must be selected for the selected Classification Scheme.";
       setValPageVector(vValidate, "Classification Scheme Items", sCSI, bNotMandatory, iNoLengthLimit, strInValid, sOriginAction);
   
       s = m_VD.getVD_SOURCE();
       if (s == null) s = "";
       setValPageVector(vValidate, "Value Domain Origin", s, bNotMandatory, iNoLengthLimit, "", sOriginAction);
 
       s = m_VD.getVD_CHANGE_NOTE();
       if (s == null) s = "";
       setValPageVector(vValidate, "Comment/Change Note", s, bNotMandatory, 2000, "", sOriginAction);
      
       // finaly, send vector to JSP
       req.setAttribute("vValidate", vValidate);
   }  // end of setValidatePageValuesVD
 
   /**
   * To check validity of the data for Permissible Values component before submission, called from NCICurationServlet.
   * Validation is done against Database restriction and ISO1179 standards.
   * calls various methods to get validity messages and store it into the vector.
   * Valid/Invalid Messages are stored in request Vector 'vValidate' along with the field, data.
   *
   * @param req The HttpServletRequest object.
   * @param res HttpServletResponse object.
   * @param m_PV Permissible value Bean.
   * @param getAC reference to GetACService class.
   *
   * @throws IOException  If an input or output exception occurred
   * @throws ServletException  If servlet exception occured
   */
   public void setValidatePageValuesPV(HttpServletRequest req, HttpServletResponse res, 
         PV_Bean m_PV, GetACService getAC) throws ServletException,IOException, Exception
   {
       HttpSession session = req.getSession();
       Vector vValidate = new Vector();
       String s;
       boolean bMandatory = true;
       boolean bNotMandatory = false;
       String strInValid = "";
       int iLengthLimit = 30;
       int iNoLengthLimit = -1;
       String pvAction = (String)session.getAttribute("PVAction");
       
       s = m_PV.getPV_PV_IDSEQ();
       if (s == null) s = "";
       //validate these only if not block edit
       if (pvAction == null || !(pvAction.equals("editPV") && s.equals("")))
       {
         // mandatory
         s = m_PV.getQUESTION_VALUE();
         if (s != null && !s.equals(""))
           setValPageVector(vValidate, "Valid Value", s, bNotMandatory, iNoLengthLimit, strInValid, "");
           
         s = m_PV.getPV_VALUE();
         if (s == null) s = "";
         setValPageVector(vValidate, "Value", s, bMandatory, iNoLengthLimit, strInValid, "");
   
         s = m_PV.getPV_SHORT_MEANING();
         if (s == null) s = "";
         strInValid = "";
         setValPageVector(vValidate, "Value Meaning", s, bMandatory, 2000, strInValid, "");
   
         s = m_PV.getPV_MEANING_DESCRIPTION();
         if (s == null) s = "";
         setValPageVector(vValidate, "Value Meaning Description", s, bNotMandatory, 2000, "", "");
   
         EVS_Bean evs = (EVS_Bean)m_PV.getVM_CONCEPT();
         if (evs == null) evs = new EVS_Bean();
         s = evs.getNCI_CC_VAL();
         if (s != null && !s.equals("") && evs.getEVS_DATABASE() != null) 
           s = s + " : " + evs.getEVS_DATABASE();
         if (s == null) s = "";
         setValPageVector(vValidate, "EVS Concept Code", s, bNotMandatory, iNoLengthLimit, "", "");        
       }
 
       s = m_PV.getPV_VALUE_ORIGIN();
       if (s == null) s = "";
       setValPageVector(vValidate, "Value Origin", s, bNotMandatory, iNoLengthLimit, "", "");
 
       s = m_PV.getPV_BEGIN_DATE();
       if (s == null) s = "";
       setValPageVector(vValidate, "Effective Begin Date", s, bNotMandatory, iNoLengthLimit, "", "");
 
       s = m_PV.getPV_END_DATE();
       if (s == null) s = "";
       setValPageVector(vValidate, "Effective End Date", s, bNotMandatory, iNoLengthLimit, "", "");
 
       // finaly, send vector to JSP
       req.setAttribute("vValidate", vValidate);
    }  // end of setValidatePageValuesPV
 
   /**
    * makes validation for block edit
    * @param req
    * @param res
    * @param sACType
    * @throws java.lang.Exception
    */
   public void setValidateBlockEdit(HttpServletRequest req, HttpServletResponse res, 
       String sACType) throws Exception
   {
 //System.out.println("in setValidateBlockEdit");
      HttpSession session = req.getSession();
      Vector vValidate = new Vector();
      boolean bMandatory = true;
      boolean bNotMandatory = false;
      String strInValid = "";
      int iLengthLimit = 30;
      int iNoLengthLimit = -1;
      String sDEAction = (String)session.getAttribute("DEAction");
      String sOriginAction = (String)session.getAttribute("originAction");
      String sMenu = (String)session.getAttribute("MenuAction");
      String s = "", sVer = "", sWF = "", sOrigin = "", sBD = "", sED = "", sLan = "", sCN = "";
      Vector vACCSI = new Vector(), vAC_CS = new Vector(), vACNames = new Vector();   
      //call the method to check validation rules
      this.checkBlockEditRules(req, res, sACType);
      //get request and session attributes
      String decvdValid = (String)req.getAttribute("DECVDValid");
      String regValid = (String)req.getAttribute("REGValid");
      String begValid = (String)req.getAttribute("BEGValid");
      String endValid = (String)req.getAttribute("ENDValid");
      String wfValid = (String)req.getAttribute("WFValid");
      //get the common attributes for all three
      DE_Bean de = (DE_Bean)session.getAttribute("m_DE");
      DEC_Bean dec = (DEC_Bean)session.getAttribute("m_DEC");
      VD_Bean vd = (VD_Bean)session.getAttribute("m_VD");
      EVS_Bean oc = (EVS_Bean)session.getAttribute("m_OC");
      EVS_Bean pc = (EVS_Bean)session.getAttribute("m_PC");
      EVS_Bean rep = (EVS_Bean)session.getAttribute("m_REP");
      EVS_Bean ocq = (EVS_Bean)session.getAttribute("m_OCQ");
      EVS_Bean pcq = (EVS_Bean)session.getAttribute("m_PCQ");
      EVS_Bean repq = (EVS_Bean)session.getAttribute("m_REPQ");
      if (sACType.equals("DataElement"))
      {
         sVer = de.getDE_VERSION();
         sWF = de.getDE_ASL_NAME();
         sOrigin = de.getDE_SOURCE();
         sCN = de.getDE_CHANGE_NOTE();
         sBD = de.getDE_BEGIN_DATE();
         sED = de.getDE_END_DATE();
         vACNames = de.getDE_CS_NAME();
         vACCSI = (Vector)de.getDE_AC_CSI_VECTOR();
         vAC_CS = de.getDE_AC_CSI_VECTOR();
      } 
      else if (sACType.equals("DataElementConcept"))
      {
         sVer = dec.getDEC_VERSION();
         sWF = dec.getDEC_ASL_NAME();
         sOrigin = dec.getDEC_SOURCE();
         sCN = dec.getDEC_CHANGE_NOTE();
         sBD = dec.getDEC_BEGIN_DATE();
         sED = dec.getDEC_END_DATE();
         vACNames = dec.getDEC_CS_NAME();
         vACCSI = (Vector)dec.getDEC_AC_CSI_VECTOR();
         vAC_CS = dec.getDEC_AC_CSI_VECTOR();
      }
      else if (sACType.equals("ValueDomain"))
      {
         sVer = vd.getVD_VERSION();
         sWF = vd.getVD_ASL_NAME();
         sOrigin = vd.getVD_SOURCE();
         sCN = vd.getVD_CHANGE_NOTE();
         sBD = vd.getVD_BEGIN_DATE();
         sED = vd.getVD_END_DATE();
         vACNames = vd.getVD_CS_NAME();
         vACCSI = (Vector)vd.getVD_AC_CSI_VECTOR();
         vAC_CS = vd.getVD_AC_CSI_VECTOR();
      }
      
      //add them to validate pages
      if (sACType.equals("DataElement"))
      {
         //dec attribute
         s = de.getDE_DEC_NAME();
         if (s == null) s = "";
         setValPageVector(vValidate, "Data Element Concept", s, bNotMandatory, iNoLengthLimit, "", sOriginAction);        
         //vd attribute
         s = de.getDE_VD_NAME();
         if (s == null) s = "";
         setValPageVector(vValidate, "Value Domain", s, bNotMandatory, iNoLengthLimit, "", sOriginAction);        
      } 
      else if (sACType.equals("DataElementConcept"))
       {
         //validate naming components
         vValidate = this.setValidateNameComp(vValidate, sACType, req, res, dec, oc, pc, null, null);
         //cd attribute
         s = dec.getDEC_CD_NAME();
         if (s == null) s = "";
         setValPageVector(vValidate, "Conceptual Domain", s, bNotMandatory, iNoLengthLimit, "", sOriginAction);        
       }
       else if (sACType.equals("ValueDomain"))
       {
         //validate naming components
         vValidate = this.setValidateNameComp(vValidate, sACType, req, res, null, null, null, vd, rep);
         //cd attribute
         s = vd.getVD_CD_NAME();
         if (s == null) s = "";
         setValPageVector(vValidate, "Conceptual Domain", s, bNotMandatory, iNoLengthLimit, "", sOriginAction);        
       }
       //workflow status
       if (sWF == null) sWF = "";
       setValPageVector(vValidate, "Workflow Status", sWF, bNotMandatory, 20, "", sOriginAction);
     
       //version
       if (sVer == null) sVer = "";
       strInValid = "";
       setValPageVector(vValidate, "Version", sVer, bNotMandatory, iNoLengthLimit, strInValid, sOriginAction);
       
         //registration status
       if (sACType.equals("DataElement"))
       {
         s = de.getDE_REG_STATUS();
         if (s == null) s = "";
         strInValid = "";
         setValPageVector(vValidate, "Registration Status", s, bNotMandatory, 50, strInValid, sOriginAction);
       }     
       //data type attributes
       if (sACType.equals("ValueDomain"))
       {
         s = vd.getVD_DATA_TYPE();
         if (s == null) s = "";
         setValPageVector(vValidate, "Data Type", s, bNotMandatory, 20, "", sOriginAction);        
       }
       //begin date
       if (sBD == null) sBD = "";
       strInValid = "";
       if (begValid != null && !begValid.equals(""))
         strInValid = "Begin Date is null for " + begValid;
       setValPageVector(vValidate, "Effective Begin Date", sBD, bNotMandatory, iNoLengthLimit, strInValid, sOriginAction);
       //edn date
       if (sED == null) sED = "";
       strInValid = "";
       if (!sWF.equals("") && wfValid != null && !wfValid.equals(""))
         strInValid = "End Date does not exist in " + wfValid;
       if (endValid != null && !endValid.equals(""))
       {
         if (!strInValid.equals("")) strInValid = "\n";
         strInValid = "Begin Date is not before the End Date for " + endValid;
       }
       setValPageVector(vValidate, "Effective End Date", sED, bNotMandatory, iNoLengthLimit, strInValid, sOriginAction);
       //document text for DE
       if (sACType.equals("DataElement"))
       {
         s = de.getDOC_TEXT_LONG_NAME();
         if (s == null) s = "";
         setValPageVector(vValidate, "Document Text", s, bNotMandatory, iNoLengthLimit, "", sOriginAction);        
       }
       //other value domain attributes
       if (sACType.equals("ValueDomain"))
         vValidate = this.setValidateVDOtherAttr(vValidate, vd, sOriginAction);    
 
       //add cs-csi to validate page
       String sCS = ""; 
       String sCSI = "";
       String sAC = "";
       if(vACNames != null)
       {
         //Vector vACCSI = (Vector)de.getDE_AC_CSI_VECTOR();    //req.getAttribute("vACCSIList");
         if (vACCSI != null && !vACCSI.isEmpty())
         {
           for (int i=0; i<vACCSI.size(); i++)
           {
             AC_CSI_Bean accsiBean = (AC_CSI_Bean)vACCSI.elementAt(i);
             CSI_Bean csiBean = (CSI_Bean)accsiBean.getCSI_BEAN();
             if (i>0) 
             {
               sCS += ", ";
               sCSI += ", ";
               sAC += ", ";
             }
             sCS += csiBean.getCSI_CS_LONG_NAME();  //accsiBean.getCS_LONG_NAME();
             sCSI += csiBean.getCSI_NAME(); //accsiBean.getCSI_NAME();
             sAC += accsiBean.getAC_LONG_NAME();
           }
         }
       }   
       //Vector vAC_CS = de.getDE_AC_CSI_VECTOR();
       Vector vOriginal_ACCS = (Vector)session.getAttribute("vAC_CSI");
       if (vAC_CS == null) vAC_CS = new Vector();
       if (vOriginal_ACCS == null) vOriginal_ACCS = new Vector();
       strInValid = "";
        //no change occured
       if (vAC_CS.size() == vOriginal_ACCS.size())
         strInValid = "No Change";
        //some added or removed cs-csis to/from the existing list.
       else if (vAC_CS.size() != vOriginal_ACCS.size())
         strInValid = "Valid";
       setValPageVector(vValidate, "Classification Scheme", "", bNotMandatory, iNoLengthLimit, strInValid, sOriginAction);
       setValPageVector(vValidate, "Classification Scheme Items", "", bNotMandatory, iNoLengthLimit, strInValid, sOriginAction);     
 
       //origin or source
       if (sOrigin == null) sOrigin = "";
       setValPageVector(vValidate, "Data Element Origin", sOrigin, bNotMandatory, iNoLengthLimit, "", sOriginAction);
 
       //comment or change note
       if (sCN == null) sCN = "";
       setValPageVector(vValidate, "Comment/Change Note", sCN, bNotMandatory, 2000, "", sOriginAction);
 
        // finaly, send vector to JSP
        req.setAttribute("vValidate", vValidate);
     // }
   }
 
   /**
    * makes validation for naming components
    * @param vValidate validate vector
    * @param acType type of ac
    * @param req HttpServletRequest
    * @param res HttpServletResponse
    * @param m_DEC DEC_Bean
    * @param m_OC OC_Bean
    * @param m_PC PC_Bean
    * @param m_VD VD_Bean
    * @param m_REP REP_Bean
    * @return validate vector
    * @throws javax.servlet.ServletException
    * @throws java.io.IOException
    * @throws java.lang.Exception
    */
   private Vector setValidateNameComp(Vector vValidate, String acType, HttpServletRequest req,
           HttpServletResponse res, DEC_Bean m_DEC, EVS_Bean m_OC, EVS_Bean m_PC, 
           VD_Bean m_VD, EVS_Bean m_REP) 
           throws ServletException,IOException, Exception
   {
 //System.out.println("setValidateNameComp");
       HttpSession session = req.getSession();
       GetACSearch getACSer = new GetACSearch(req, res, m_servlet);
       InsACService insAC = new InsACService(req, res, m_servlet);
       String sOriginAction = (String)session.getAttribute("originAction");
       Vector vOC = new Vector();
       Vector vPROP = new Vector();
       Vector vREP = new Vector();
       vOC = (Vector)session.getAttribute("vObjectClass");
       vPROP = (Vector)session.getAttribute("vProperty");
       vREP = (Vector)session.getAttribute("vRepTerm");
       if (vOC == null) vOC = new Vector();
       if (vPROP == null) vPROP = new Vector();
       if (vREP == null) vREP = new Vector();
       String s = "";
       boolean bMandatory = true;
       boolean bNotMandatory = false;
       String strInValid = "";
       int iLengthLimit = 30;
       int iNoLengthLimit = -1;
       if (acType.equals("DataElementConcept"))
       {
         String sDECAction = (String)session.getAttribute("DECAction");
         String sDECActionOriginal = sDECAction;
         //check edit or create
         if (sDECAction.equals("EditDEC"))
            sDECAction = "Edit";
         else
            sDECAction = "Create";
         String ss = "";
         String sP = m_DEC.getDEC_OCL_NAME_PRIMARY();
         if (sP == null) sP = "";
         String sQ = "";
         Vector vQ = m_DEC.getDEC_OC_QUALIFIER_NAMES();
         if (vQ != null && vQ.size() > 0)
           sQ = (String)vQ.elementAt(0);
         if (sQ == null) sQ = "";
         //check validity of object class before creating one
         String sOCL = m_DEC.getDEC_OCL_NAME();
         String strOCInvalid = "";
         if(!sQ.equals("") && sP.equals(""))
           strOCInvalid = "Cannot have Secondary Concepts without a Primary Concept.\n";
         if(!sQ.equals("") || !sP.equals("") && m_OC != null)
           strOCInvalid = strOCInvalid + checkConceptCodeExistsInOtherDB(vOC, insAC, null);
 //System.out.println("setValidateNameComp05");
         if ((sOCL == null || sOCL.equals("")) && sDECAction.equals("Edit"))  
           strOCInvalid = strOCInvalid + checkDEUsingDEC("ObjectClass", m_DEC, getACSer);
         else if (sOCL == null || sOCL.equals(""))
           m_DEC.setDEC_OCL_IDSEQ("");
 
         //check vaiidity of property before createing one
         String strPropInvalid = "";
         String sProp = m_DEC.getDEC_PROPL_NAME_PRIMARY();
         if (sProp == null) sProp = "";
         String sQual = "";
         Vector vQual = m_DEC.getDEC_PROP_QUALIFIER_NAMES();
         if (vQual != null && vQual.size() > 0)
           sQual = (String)vQual.elementAt(0);
         String sPropL = m_DEC.getDEC_PROPL_NAME();
         if (sPropL == null || sPropL.equals(""))
           m_DEC.setDEC_PROPL_IDSEQ("");
           
         if(!sQual.equals("") && sProp.equals(""))
           strPropInvalid = "Cannot have Secondary Concepts without a Primary Concept.\n"; 
          if(!sQual.equals("") || !sProp.equals("") && m_PC != null)
           strPropInvalid = strPropInvalid + checkConceptCodeExistsInOtherDB(vPROP, insAC, null);
         //create object class and property only if valid
         if ((strOCInvalid == null || strOCInvalid.equals("")) && (strPropInvalid == null || strPropInvalid.equals("")))
             m_DEC = m_servlet.doInsertDECBlocks(req, res, m_DEC);        
         //check oc prop combination already exists in the database         
         String objID = m_DEC.getDEC_OCL_IDSEQ();
         String propID = m_DEC.getDEC_PROPL_IDSEQ();
         //display error if not created properly.
         String retCode = (String)req.getAttribute("retcode");
  // System.out.println(retCode + " : " + objID + " : " + propID);
         if (retCode != null && !retCode.equals(""))
         {
           //display error if unable to create object class.
           String sMsg = (String)session.getAttribute("statusMessage");
           if (sMsg == null) sMsg = "";
           if (sOCL != null && !sOCL.equals("") && (objID == null || objID.equals("")))
             strOCInvalid = strOCInvalid + sMsg;
           if (sPropL != null && !sPropL.equals("") && (propID == null || propID.equals("")))
             strPropInvalid = strPropInvalid + sMsg;
         }
         //do only new version of oc/prop if necessary for DEC without unique check
         else if (!sOriginAction.equals("BlockEditDEC"))
         {
           if ((objID != null && !objID.equals("")) || (propID != null && !propID.equals("")))
             strInValid = insAC.checkUniqueOCPropPair(m_DEC, "UniqueAndVersion", sOriginAction); 
            // strInValid = checkUniqueOCPropPair(m_DEC, req, res, sOriginAction); 
         }
    // System.out.println(sOriginAction + " oc prop invalid " + strInValid);
         //create new version if unique for single or block edit dec
         if (strInValid == null || strInValid.equals("") || strInValid.indexOf("Warning") > 0)
         {
           //allow creating new version of object class here if asl name is not released
           if (objID != null && !objID.equals(""))
           {
             req.setAttribute("retcode", "");
             String sOCasl = m_DEC.getDEC_OBJ_ASL_NAME();
             String newID = "";
             System.out.println(sOCasl + " create block OC new version here");
             if (sOCasl != null && !sOCasl.equals("RELEASED"))
             {
               newID = insAC.setOC_PROP_REP_VERSION(objID, "ObjectClass");
               if (newID != null && !newID.equals(""))
                 m_DEC.setDEC_OCL_IDSEQ(newID);
               else
               {
                 String errCode = (String)req.getAttribute("retcode");
                 strOCInvalid += errCode + " : Unable to create new version of the Object Class";
               }
             }
           }
           //allow creating new version of property here if asl name is not released
           if (propID != null && !propID.equals(""))
           {
             req.setAttribute("retcode", "");
             String sPROPasl = m_DEC.getDEC_PROP_ASL_NAME();
             String newID = "";
             System.out.println(sPROPasl + " create block PROP new version here");
             if (sPROPasl != null && !sPROPasl.equals("RELEASED"))
             {
               newID = insAC.setOC_PROP_REP_VERSION(propID, "Property");
               if (newID != null && !newID.equals(""))
                 m_DEC.setDEC_PROPL_IDSEQ(newID);
               else
               {
                 String errCode = (String)req.getAttribute("retcode");
                 strPropInvalid += errCode + " : Unable to create new version of the Property";
               }
             }
           }
         }
         //append it to oc invalid
         strOCInvalid = strOCInvalid + strInValid; 
         //check for warning of oc existance          
         if((sOCL == null || sOCL.equals("")) && !sOriginAction.equals("BlockEditDEC"))
           strOCInvalid = strOCInvalid + "Warning: a Data Element Concept should have an Object Class.\n";
         setValPageVector(vValidate, "Object Class", sOCL, bNotMandatory, 255, strOCInvalid, sOriginAction);
         //append it to prop invalid
         strPropInvalid  = strPropInvalid + strInValid; 
         s = m_DEC.getDEC_PROPL_NAME();
         setValPageVector(vValidate, "Property", s, bNotMandatory, 255, strPropInvalid, sOriginAction);
       }
       else
       {
         String sVDAction = (String)session.getAttribute("VDAction");
         //check edit or create
         if (sVDAction.equals("EditVD"))
            sVDAction = "Edit";
         else
            sVDAction = "Create";
 
         String ss = "";
         String repID = m_VD.getVD_REP_IDSEQ();
         String sP = m_VD.getVD_REP_NAME_PRIMARY();
         if (sP == null || sP.equals(" ")) sP = "";
         String sQ = "";
         Vector vQual = m_VD.getVD_REP_QUALIFIER_NAMES();
         if (vQual != null && vQual.size() > 0)
           sQ = (String)vQual.elementAt(0);
         if(!sQ.equals("") && sP.equals(""))
           strInValid = "Cannot have Secondary Concepts without a Primary Concept.\n";
         if(!sQ.equals("") || !sP.equals("") && m_REP != null)
           strInValid = strInValid + checkConceptCodeExistsInOtherDB(vREP, insAC, null);
         ss = m_VD.getVD_REP_TERM();
         setValPageVector(vValidate, "Rep Term", ss, bNotMandatory, 255, strInValid, sOriginAction);
       }
       return vValidate;
   }
   
   /**
    * to get all other value domain attributes
    * @param vValidate
    * @param m_VD
    * @param sOriginAction
    * @return vValidate the vector to display validate status
    * @throws java.lang.Exception
    */
   private Vector setValidateVDOtherAttr(Vector vValidate, VD_Bean m_VD, String sOriginAction) throws Exception
   {
       String s = "";
       String strInValid = "";
       boolean bNotMandatory = false;
     
       s = m_VD.getVD_UOML_NAME();
       if (s == null) s = "";
       strInValid = checkValueDomainIsTypeMeasurement();
       setValPageVector(vValidate, "Unit Of Measure", s, bNotMandatory, 20, strInValid, sOriginAction);
 
       s = m_VD.getVD_FORML_NAME();
       if (s == null) s = "";
       strInValid = checkValueDomainIsTypeMeasurement();
       setValPageVector(vValidate, "Display Format", s, bNotMandatory, 20, strInValid, sOriginAction);
 
       s = m_VD.getVD_MIN_LENGTH_NUM();
       if (s == null) s = "";
       strInValid = checkValueIsNumeric(s, "Minimum Length");
       setValPageVector(vValidate, "Minimum Length", s, bNotMandatory, 8, strInValid, sOriginAction);
 
       s = m_VD.getVD_MAX_LENGTH_NUM();
       if (s == null) s = "";
       strInValid = checkValueIsNumeric(s, "Maximum Length");  // + checkLessThan8Chars(s);
       setValPageVector(vValidate, "Maximum Length", s, bNotMandatory, 8, strInValid, sOriginAction);
       s = m_VD.getVD_LOW_VALUE_NUM();
 
       strInValid = "";
       strInValid = checkValueIsNumeric(s, "Low Value");   //+ checkValueDomainIsNumeric(s, "Low Value Number Attribute");
       setValPageVector(vValidate, "Low Value", s, bNotMandatory, 255, strInValid, sOriginAction);
 
       s = m_VD.getVD_HIGH_VALUE_NUM();
       if (s == null) s = "";
       strInValid = "";
       strInValid = checkValueIsNumeric(s, "High Value");  //+ checkValueDomainIsNumeric(s, "High Value Number Attribute");
       setValPageVector(vValidate, "High Value", s, bNotMandatory, 255, strInValid, sOriginAction);
 
       s = m_VD.getVD_DECIMAL_PLACE();
       if (s == null) s = "";
       strInValid = "";
       strInValid = checkValueIsNumeric(s, "Decimal Place");    //checkValueDomainIsNumeric(s, "Decimal Place Attribute");
       setValPageVector(vValidate, "Decimal Place", s, bNotMandatory, 2, strInValid, sOriginAction);
       
       return vValidate;
   }
 
 
   /**
   * To check for existence of value meaning
   * called from NCICurationServlet.
   * @param req The HttpServletRequest object.
   * @param res HttpServletResponse object
   * @param sMeaning
   * @param sCDIDSEQ conceptual domain
   *
   */
   public String checkVMExists(HttpServletRequest req, HttpServletResponse res,
                         String sMeaning, String sMeaningDesc, String sCDIDSEQ)
   {
       HttpSession session = req.getSession();
       ResultSet rs = null;
       Statement CStmt = null;
       Connection conn = null;
       String retValue = "";
 
       try
       {
           String retCode = "";
           //set the bean values
           VM_Bean vm = new VM_Bean();
           vm.setVM_SHORT_MEANING(sMeaning);
           vm.setVM_DESCRIPTION(sMeaningDesc);
           vm.setVM_CD_IDSEQ(sCDIDSEQ);
           //check if the meaning exists in the caDSR
           retCode = "";
           retCode = this.getVM(req, res, vm);
           if (retCode != null && !retCode.equals("")) 
           {
             // return no meaning exists in caDSR
             return "false";
           }
           else  
           {
             // return meaning exists in caDSR
             return "true";
           }
       }
       catch(Exception e)
       {
         logger.fatal("ERROR in checkVM " + e.toString());
       }
       try
       {
         if(rs!=null) rs.close();
         if(CStmt!=null) CStmt.close();
         if(conn != null) conn.close();
       }
     
       catch(Exception ee)
       {
         logger.fatal("ERROR in checkPVVM closing : " + ee.toString());
       }
       return retValue;
   } //end checkVM
   
   
 /**
   * @param req The HttpServletRequest object.
   * @param res HttpServletResponse object
   * @param String oc_idseq
   * @param String prop_idseq
   * @param String strInvalid
   *
 */
   public String checkOCPropWorkFlowStatuses(HttpServletRequest req,
           HttpServletResponse res, String oc_idseq, String prop_idseq, String strInvalid)
   {
     HttpSession session = req.getSession();
     ResultSet rs = null;
     Statement CStmt = null;
     Connection conn = null;
     String sOC_WFS = "";
     String sProp_WFS = "";
     String sOCSQL = "Select asl_name from object_classes_view_ext where oc_idseq = '" + oc_idseq + "'";
     String sPropSQL = "Select asl_name from PROPERTIES_EXT where prop_idseq = '" + prop_idseq + "'";
     
     if (oc_idseq == null) oc_idseq = "";
     if (prop_idseq == null) prop_idseq = "";
     if(!(oc_idseq.equals("") && prop_idseq.equals(""))) // at least one is in database
     {
       try
       {
         conn = m_servlet.connectDB(req, res);
         if (conn == null)  // still null to login page
           m_servlet.ErrorLogin(req, res);
         else
         {
           if(!oc_idseq.equals(""))
           {
             CStmt = conn.createStatement();
             rs = CStmt.executeQuery(sOCSQL);
             //loop through to printout the outstrings
             while(rs.next())
             {
               sOC_WFS = rs.getString(1); 
             }
             if (sOC_WFS == null) sOC_WFS = "";
             sOC_WFS = sOC_WFS.toUpperCase();
             if (!sOC_WFS.equals("RELEASED"))
               strInvalid = "For DEC Work Flow Status to be 'Released', " +
               "the Object Class and Property Work Flow Statuses must be 'Released'.";
             if(rs!=null) rs.close();
             if(CStmt!=null) CStmt.close();
           }
           else if(!prop_idseq.equals(""))
           {    
             // Now check Property WFStatus  
             CStmt = conn.createStatement();
             rs = CStmt.executeQuery(sPropSQL);
             while(rs.next())
             {
               sProp_WFS = rs.getString(1); 
             }
             if (sProp_WFS == null) sProp_WFS = "";
             sProp_WFS = sProp_WFS.toUpperCase();
             if (!sProp_WFS.equals("RELEASED"))
               strInvalid = "For DEC Work Flow Status to be 'Released', " +
               "the Object Class and Property Work Flow Statuses must be 'Released'.";
           }
         }
       }
       catch(Exception e)
       {
         //System.out.println("ERROR in checkOCPropWorkFlowStatuses" + e.toString());
         logger.fatal("ERROR in checkOCPropWorkFlowStatuses " + e.toString());
       }
     }
     try
     {
       if(rs!=null) rs.close();
       if(CStmt!=null) CStmt.close();
       if(conn != null) conn.close();
     }
     catch(Exception ee)
     {
       logger.fatal("ERROR in checkOCPropWorkFlowStatuses closing : " + ee.toString());
     }
     return strInvalid;
   } //end checkOCPropWorkFlowStatuses
   
 
 
    /**
   * To check for existence of value-meaning pair, return idseq if exist, else create new and return idseq
   * called from NCICurationServlet.
   *
   * @param req The HttpServletRequest object.
   * @param res HttpServletResponse object.
   * @param sValue
   * @param sMeaning
   * @param sCD conceptual domain
   *
   */
   public String createNewPVVM(HttpServletRequest req, HttpServletResponse res,
                         String sValue, String sMeaning, String sCD)
   {
     ResultSet rs = null;
     Statement CStmt = null;
     Connection conn = null;
     String pvIdseq = "";
     String sSQL = "Select pv_idseq from permissible_values where value = '" + sValue + "'" +
            " and short_meaning = '" + sMeaning + "'";
     try
     {
       conn = m_servlet.connectDB(req, res);
       if (conn == null)  // still null to login page
         m_servlet.ErrorLogin(req, res);
       else
       {
         CStmt = conn.createStatement();
         rs = CStmt.executeQuery(sSQL);
         //loop through to printout the outstrings
         while(rs.next())
         {
           pvIdseq = rs.getString(1);
         }// end of while
         if (pvIdseq == null)
           createNewPVVM(req, res, sValue, sMeaning, sCD);
       }
     }
     catch(Exception e)
     {
       logger.fatal("ERROR in checkPVVM " + e.toString());
     }
     try
     {
       if(rs!=null) rs.close();
       if(CStmt!=null) CStmt.close();
       if(conn != null) conn.close();
     }
     catch(Exception ee)
     {
       logger.fatal("ERROR in checkPVVM closing : " + ee.toString());
     }
     return pvIdseq;
   } //end checkPVVM
 
  /**
   * To check validity of the data for Value Meanings component before submission, called from NCICurationServlet.
   * Validation is done against Database restriction and ISO1179 standards.
   * calls various methods to get validity messages and store it into the vector.
   * Valid/Invalid Messages are stored in request Vector 'vValidate' along with the field, data.
   *
   * @param req The HttpServletRequest object.
   * @param res HttpServletResponse object.
   * @param m_VM Value Meanings Bean.
   * @param getAC reference to GetACService class.
   *
   * @throws IOException  If an input or output exception occurred
   * @throws ServletException  If servlet exception occured
   */
   public void setValidatePageValuesVM(HttpServletRequest req,
           HttpServletResponse res, VM_Bean m_VM, GetACService getAC) 
           throws ServletException,IOException, Exception
   {
       HttpSession session = req.getSession();
       Vector vValidate = new Vector();
       String s;
       boolean bMandatory = true;
       boolean bNotMandatory = false;
       String strInValid = "";
       int iLengthLimit = 30;
       int iNoLengthLimit = -1;
         s = m_VM.getVM_SHORT_MEANING();
         if (s == null) s = "";
         req.setAttribute("VMExist", "false");
         String vmExist = checkVMExists(req, res, s, m_VM.getVM_DESCRIPTION(), m_VM.getVM_CD_IDSEQ());
         if (vmExist.equals("true"))
         {
           strInValid = "Value Meaning exists in caDSR.";
           req.setAttribute("VMExist", "true");
         }
         InsACService insAC = new InsACService(req, res, m_servlet);
         if(!s.equals("") && m_VM != null)
           strInValid = strInValid + checkConceptCodeExistsInOtherDB(null, insAC, m_VM);
         setValPageVector(vValidate, "Value Meaning", s, bMandatory, 2000, strInValid, "");
 
         s = m_VM.getVM_CD_NAME();
         if (s == null) s = "";
         setValPageVector(vValidate, "Conceptual Domain", s, bMandatory, iNoLengthLimit, "", "");
 
         s = m_VM.getVM_BEGIN_DATE();
         if (s == null) s = "";
         setValPageVector(vValidate, "Effective Begin Date", s, bMandatory, iNoLengthLimit, "", "");
 
         s = m_VM.getVM_END_DATE();
         if (s == null) s = "";
         setValPageVector(vValidate, "Effective End Date", s, bNotMandatory, iNoLengthLimit, "", "");
 
         s = m_VM.getVM_DESCRIPTION();
         if (s == null) s = "";
         setValPageVector(vValidate, "Description", s, bNotMandatory, 2000, "", "");
 
         s = m_VM.getVM_COMMENTS();
         if (s == null) s = "";
         setValPageVector(vValidate, "Comments", s, bNotMandatory, 2000, "", "");
 
         // finaly, send vector to JSP
         req.setAttribute("vValidate", vValidate);
    }  // end of setValidatePageValuesVM
 
  /**
   * To check write permission for the user, in selected context and selected component,
   * called from setValidatePageValuesDE, setValidatePageValuesDEC, setValidatePageValuesVD methods.
   * Calls 'getAC.hasPrivilege' method to get yes or no string value.
   *
   * @param ACType Selected component type.
   * @param sUserName User login Name.
   * @param ContID Conte_idseq for the selected context.
   * @param getAC reference to GetACService class.
   *
   * @return String strValid message if no permit or other problem. empty string if has permission.
   */
   public String checkWritePermission(String ACType, String sUserName, String ContID, GetACService getAC)
   {
       // validation code here
       String sPermit = "";
       String sErrorMessage = "";
       sPermit = getAC.hasPrivilege("Create", sUserName, ACType, ContID);
 
       if (sPermit.equals("Yes"))
         sErrorMessage = "";
       else if (sPermit.equals("No"))
         sErrorMessage = sUserName + " does not have authorization to create/edit in this context";
       else
         sErrorMessage = "Problem with write privileges.";
 
       return sErrorMessage;
   }
 
   /**
   * For Block Edit, checks whether Begin Date is after any End dates being edited.
   * called from setValidatePageValuesDE, setValidatePageValuesDEC, setValidatePageValuesVD methods.
   *
   * @return String strValid message if begin date is after end dates.
   */
   public String validateBegDateVsEndDates(HttpServletRequest req, String sBeg, String sACType)
   {
     String strInvalid = "";
     String sPrefName = "";
     String sEndDate = "";
     String bDateOK = "";
     HttpSession session = req.getSession();
     Vector vBERows = (Vector)session.getAttribute("vBEResult");
     if (vBERows.size()>0)
     {
       for(int i=0; i<(vBERows.size()); i++)
       {
         if (sACType.equals("DE"))
         {
           DE_Bean de = new DE_Bean();
           de = (DE_Bean)vBERows.elementAt(i);
           sPrefName = de.getDE_PREFERRED_NAME();
           sEndDate = de.getDE_END_DATE();
           if (sEndDate == null) sEndDate = "";
           if (!sEndDate.equals(""))
             bDateOK = compareDates(sEndDate, sBeg);
           if (bDateOK.equals("true"))
             strInvalid = strInvalid + "Beg Date exceeds End Date on DE " + sPrefName + ". ";
         }
         else if (sACType.equals("DEC"))
         {
           DEC_Bean dec = new DEC_Bean();
           dec = (DEC_Bean)vBERows.elementAt(i);
           sPrefName = dec.getDEC_PREFERRED_NAME();
           sEndDate = dec.getDEC_END_DATE();
           if (sEndDate == null) sEndDate = "";
           if (!sEndDate.equals(""))
             bDateOK = compareDates(sEndDate, sBeg);
           if (bDateOK.equals("true"))
             strInvalid = strInvalid + "Beg Date exceeds End Date on DEC " + sPrefName + ". ";
         }
         else if (sACType.equals("VD"))
         {
           VD_Bean vd = new VD_Bean();
           vd = (VD_Bean)vBERows.elementAt(i);
           sPrefName = vd.getVD_PREFERRED_NAME();
           sEndDate = vd.getVD_END_DATE();
           if (sEndDate == null) sEndDate = "";
           if (!sEndDate.equals(""))
             bDateOK = compareDates(sEndDate, sBeg);
           if (bDateOK.equals("true"))
             strInvalid = strInvalid + "Beg Date exceeds End Date on VD " + sPrefName + ". ";
         }
       }
     }
     return strInvalid;
   }
 
   /**
   * For Block Edit, checks whether End Date is before any Begin dates being edited.
   * called from setValidatePageValuesDE, setValidatePageValuesDEC, setValidatePageValuesVD methods.
   *
   * @return String strValid message if end date before begin dates.
   */
   public String validateEndDateVsBeginDates(HttpServletRequest req, String sEnd, String sACType)
   {
     String strInvalid = "";
     String sPrefName = "";
     String sBegDate = "";
     String bDateOK = "";
     HttpSession session = req.getSession();
     Vector vBERows = (Vector)session.getAttribute("vBEResult");
     if (vBERows.size()>0)
     {
       for(int i=0; i<(vBERows.size()); i++)
       {
         if (sACType.equals("DE"))
         {
           DE_Bean de = new DE_Bean();
           de = (DE_Bean)vBERows.elementAt(i);
           sPrefName = de.getDE_PREFERRED_NAME();
           sBegDate = de.getDE_BEGIN_DATE();
           if (sBegDate == null) sBegDate = "";
           if (!sBegDate.equals(""))
             bDateOK = compareDates(sEnd, sBegDate);
           if (bDateOK.equals("true"))
             strInvalid = strInvalid + "End Date is before Begin Date on DE " + sPrefName + ". ";
         }
         else if (sACType.equals("DEC"))
         {
           DEC_Bean dec = new DEC_Bean();
           dec = (DEC_Bean)vBERows.elementAt(i);
           sPrefName = dec.getDEC_PREFERRED_NAME();
           sBegDate = dec.getDEC_BEGIN_DATE();
           if (sBegDate == null) sBegDate = "";
           if (!sBegDate.equals(""))
             bDateOK = compareDates(sEnd, sBegDate);
           if (bDateOK.equals("true"))
             strInvalid = strInvalid + "End Date is before Begin Date on DEC " + sPrefName + ". ";
         }
         else if (sACType.equals("VD"))
         {
           VD_Bean vd = new VD_Bean();
           vd = (VD_Bean)vBERows.elementAt(i);
           sPrefName = vd.getVD_PREFERRED_NAME();
           sBegDate = vd.getVD_BEGIN_DATE();
           if (sBegDate == null) sBegDate = "";
           if (!sBegDate.equals(""))
             bDateOK = compareDates(sEnd, sBegDate);
           if (bDateOK.equals("true"))
             strInvalid = strInvalid + "End Date is before Begin Date on VD " + sPrefName + ". ";
         }
       }
     }
     return strInvalid;
   }
 
   /**
   * For Block Edit, checks whether Date1 is before Date2.
   * called from setValidatePageValuesDE, setValidatePageValuesDEC, setValidatePageValuesVD methods.
   *
   * @return String strFail message if date2 before date1.
   */
   public String compareDates(String sDate1, String sDate2)
   {
     String strFail = "";
     java.util.Date date1;
     java.util.Date date2;
     java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MM/dd/yyyy");
     try
     {
       date1 = dateFormat.parse(sDate1);
       date2 = dateFormat.parse(sDate2);
       if (date1.before(date2))
         strFail = "true";
       else
         strFail = "false";
     }
     catch (Exception e)
     {
       logger.fatal("ERROR in setacservice_compareDates  : " + e.toString());
       return "error";
     }
     return strFail;
   }
 
   /**
    * For Block Edit, check for various business rules.
    * 
    * @param req  HttpServletRequest
    * @param res  HttpServletResponse
    * @param sACType type ac for the validation
    */
   public void checkBlockEditRules(HttpServletRequest req, HttpServletResponse res, 
               String sACType)
   {
     HttpSession session = req.getSession();
     Vector vBERows = (Vector)session.getAttribute("vBEResult");
     String sDECVal = "", sVDVal = "", dvValid = "";
     String regValid = "", sReg = "";
     String pgBDate = "", pgEDate = "", bdValid = "", edValid = "";
     String pgWFStatus = "", wfValid = "";
     GetACService getAC = new GetACService(req, res, m_servlet);
     //get the page attributes
     if (sACType.equals("DataElement"))
     {
       DE_Bean pageDE = (DE_Bean)session.getAttribute("m_DE");
       //check the validation for dec-vd combination
       sDECVal = pageDE.getDE_DEC_IDSEQ();
       sVDVal = pageDE.getDE_VD_IDSEQ();
       sReg = pageDE.getDE_REG_STATUS();
       pgBDate = pageDE.getDE_BEGIN_DATE();
       pgEDate = pageDE.getDE_END_DATE();
       pgWFStatus = pageDE.getDE_ASL_NAME();
     }
     else if (sACType.equals("DataElementConcept"))
     {
       DEC_Bean pageDEC = (DEC_Bean)session.getAttribute("m_DEC");
       pgBDate = pageDEC.getDEC_BEGIN_DATE();
       pgEDate = pageDEC.getDEC_END_DATE();
       pgWFStatus = pageDEC.getDEC_ASL_NAME();      
     }
     else if (sACType.equals("ValueDomain"))
     {
       VD_Bean pageVD = (VD_Bean)session.getAttribute("m_VD");
       pgBDate = pageVD.getVD_BEGIN_DATE();
       pgEDate = pageVD.getVD_END_DATE();
       pgWFStatus = pageVD.getVD_ASL_NAME();      
     }
     //loop through the selected acs and check the changed data against the existing one
     if (vBERows.size()>0)
     {
       for(int i=0; i<(vBERows.size()); i++)
       {
         String sWF = "", sBD = "", sED = "", acName = "";
         if (sACType.equals("DataElement"))
         {
           DE_Bean de = (DE_Bean)vBERows.elementAt(i);
           if (de == null) de = new DE_Bean();
           acName = de.getDE_LONG_NAME();
           //dec and vd attributes
           sWF = de.getDE_ASL_NAME();    //workflow status attributes   
           sBD = de.getDE_BEGIN_DATE();    //begin date attribute             
           sED = de.getDE_END_DATE();      //end date attributes          
         }
         else if (sACType.equals("DataElementConcept"))
         {
           DEC_Bean dec = (DEC_Bean)vBERows.elementAt(i);
           if (dec == null) dec = new DEC_Bean();
           acName = dec.getDEC_LONG_NAME();   //long name
           sWF = dec.getDEC_ASL_NAME();    //workflow status attributes          
           sBD = dec.getDEC_BEGIN_DATE();    //begin date attribute          
           sED = dec.getDEC_END_DATE();      //end date attributes 
          // String sCont = dec.getDEC_CONTE_IDSEQ();
          // req.setAttribute("blockContext", sCont);
         }
         else if (sACType.equals("ValueDomain"))
         {
           VD_Bean vd = (VD_Bean)vBERows.elementAt(i);
           if (vd == null) vd = new VD_Bean();
           acName = vd.getVD_LONG_NAME();     //long name
           sWF = vd.getVD_ASL_NAME();    //workflow status attributes          
           sBD = vd.getVD_BEGIN_DATE();    //begin date attribute          
           sED = vd.getVD_END_DATE();      //end date attributes          
         }
         //below validation is same for all acs
         //store page attribute in the bean attr variable according to its value on page
         if (pgWFStatus != null && !pgWFStatus.equals(""))
           sWF = pgWFStatus;
         if (pgBDate != null && !pgBDate.equals(""))
           sBD = pgBDate;
         if (pgEDate != null && !pgEDate.equals(""))
           sED = pgEDate;
         //check begin date end date relationship
         if (sED != null && !sED.equals(""))
         {
         /*  if (sBD == null || sBD.equals(""))
           {
             if (!bdValid.equals("")) bdValid = bdValid + ", ";    //add the comma for next selected ac
             bdValid = bdValid + acName;           //begin date cannot be null 
           }
           else */
           if (sBD != null && !sBD.equals(""))
           {
             String dValid = compareDates(sED, sBD);
             if (dValid == null) dValid = "";
             if (dValid.equals("true"))
             {
               if (!edValid.equals("")) edValid = edValid + ", ";    //add the comma for next selected ac
               edValid = edValid + acName;
             }
             else if (dValid.equals("error"))
               edValid = "End date is not a valid date."; 
           }
         }
         else  //end date cannot be null for some workflow status
         {
           sWF = sWF.toUpperCase();
           //if(sWF.equalsIgnoreCase("RETIRED ARCHIVED") || sWF.equalsIgnoreCase("RETIRED DELETED")
           //      || sWF.equalsIgnoreCase("RETIRED PHASED OUT"))
           if (m_vRetWFS.contains(sWF))
           {
             if (!wfValid.equals("")) wfValid = wfValid + ", ";    //add the comma for next selected ac
             wfValid = wfValid + acName;
           }
         }
       }   //end loop
       req.setAttribute("DECVDValid", dvValid);  
       req.setAttribute("REGValid", regValid);
       req.setAttribute("BEGValid", bdValid);
       req.setAttribute("ENDValid", edValid);
       req.setAttribute("WFValid", wfValid);
     }  //end if not null
   }
 
  /**
   * To check whether data contains only alpha numeric values, called from setValidatePageValuesDE,
   * setValidatePageValuesDEC, setValidatePageValuesVD, setValidatePageValuesPV, setValidatePageValuesVM methods.
   * First character must be only alphabet or empty space.
   * Name field must contain only alphanumeric and '-' character.
   *
   * @param sValue input data.
   * @param sField selected field.
   *
   * @return String strValid message if invlid. otherwise empty string.
   */
   public String checkValidAlphanumeric(String sValue, String sField)
   {
       boolean bValidFlag = true;
       String strString = "";
       sValue = sValue.trim();
       // the first character must be alphabets
       if(sValue.length() < 1)  return "Must be a character. \n";
       char firstLetter = sValue.charAt(0);
       if ((Character.isUpperCase(firstLetter)) || (Character.isLowerCase(firstLetter))|| (Character.isWhitespace(firstLetter)))
         bValidFlag = true;
       else
       {
         bValidFlag = false;
         strString = "First letter must be alphabetic. ";
       }
       //only alphabets and numbers only for Name
       if (sField.equals("Name"))
       {
         for (int i=1; i < sValue.length(); i++)
         {
           firstLetter = sValue.charAt(i);
           if ((Character.isUpperCase(firstLetter)) || (Character.isLowerCase(firstLetter)))
             bValidFlag = true;
           else if ((Character.isDigit(firstLetter)) || (Character.isWhitespace(firstLetter)) || (firstLetter == '_'))
             bValidFlag = true;
           else
           {
             bValidFlag = false;
             strString = strString + "\n Name can only be alphanumeric characters. ";
             break;
           }
         }
       }
       // return the string value
       return strString;
    }
    
  /**
   * To check whether data is unique value in the database for the selected component,
   * called from setValidatePageValuesDE, setValidatePageValuesDEC, setValidatePageValuesVD methods.
   * Creates the sql queries for the selected field, to check if the value exists in the database.
   * Calls 'getAC.doComponentExist' to execute the query.
   *
   * @param sField selected field.
   * @param ACType input data.
   * @param mDE Data Element Bean.
   * @param mDEC Data Element Concept Bean.
   * @param mVD Value Domain Bean.
   * @param getAC reference to GetACService class.
   *
   * @return String retValue message if exists already. Otherwise empty string.
   */
   public String checkUniqueOCPropPair(DEC_Bean mDEC, HttpServletRequest req, 
       HttpServletResponse res, String setAction)
   {
     boolean bValidFlag = false;
     HttpSession session = req.getSession();
     GetACService getAC = new GetACService(req, res, m_servlet);
     String sSQL="", editSQL="", versSQL="", propSQL = "", ocSQL = "";
     String menuAction = (String)session.getAttribute("MenuAction");
     //check unique if id is not the same for update
     String sContID = mDEC.getDEC_CONTE_IDSEQ();
     String sPublicID = mDEC.getDEC_DEC_ID();
 
     if (setAction.equalsIgnoreCase("EditDEC") || setAction.equalsIgnoreCase("editDECfromDE") 
             || menuAction.equals("NewDECVersion"))
        editSQL = " AND DEC.DEC_ID <> '" +  mDEC.getDEC_DEC_ID() + "'";
 
     String sOCID = mDEC.getDEC_OCL_IDSEQ();
     String sPropID = mDEC.getDEC_PROPL_IDSEQ();
     //get oc sql
     if (sOCID != null && !sOCID.equals("")) 
       ocSQL = " AND DEC.OC_IDSEQ = '" + sOCID + "'";
     else
       ocSQL = " AND DEC.OC_IDSEQ IS NULL";
     //get prop sql
     if (sPropID != null && !sPropID.equals("")) 
       propSQL = " AND DEC.PROP_IDSEQ = '" + sPropID + "'";  
     else
       propSQL = " AND DEC.PROP_IDSEQ IS NULL";
      //make the query 
     sSQL = "SELECT distinct DEC_ID FROM DATA_ELEMENT_CONCEPTS_VIEW DEC WHERE DEC.CONTE_IDSEQ = '" + sContID + "'" 
             + ocSQL + propSQL + editSQL;      //versSQL + editSQL;           
   logger.debug("oc prop pair " + sSQL); 
 
     String sDECID = getAC.isUniqueInContext(sSQL);
     if (sDECID == null || sDECID.equals(""))
     {
        sSQL = "SELECT distinct DEC_ID FROM DATA_ELEMENT_CONCEPTS_VIEW DEC WHERE DEC.CONTE_IDSEQ <> '" + sContID + "'" 
             + ocSQL + propSQL + editSQL;      //versSQL + editSQL;           
        String sContexts = getAC.isUniqueInContext(sSQL);
        if(sContexts == null || sContexts.equals(""))
           return "";
        else
        {
           return "Warning: DEC's with combination of Object Class and Property already exists in other contexts with Public ID(s): " + sContexts;
        }
     }
     else
         return "Combination of Object Class, Property and Context already exists in DEC with Public ID(s): " + sDECID;
   }
 
 
  /**
   * To check whether data is unique value in the database for the selected component,
   * called from setValidatePageValuesDE, setValidatePageValuesDEC, setValidatePageValuesVD methods.
   * Creates the sql queries for the selected field, to check if the value exists in the database.
   * Calls 'getAC.doComponentExist' to execute the query.
   *
   * @param sField selected field.
   * @param ACType input data.
   * @param mDE Data Element Bean.
   * @param mDEC Data Element Concept Bean.
   * @param mVD Value Domain Bean.
   * @param getAC reference to GetACService class.
   *
   * @return String retValue message if exists already. Otherwise empty string.
   */
   public String checkUniqueDECVDPair(DE_Bean mDE, GetACService getAC, String setAction, String sMenu)
   {    
     boolean bValidFlag = false;
     String sSQL="", editSQL="", versSQL="";
 
     //check unique if id is not the same for update
     String sPublicID = mDE.getDE_MIN_CDE_ID();
     
     if (setAction.equals("Edit") || sMenu.equals("NewDEVersion"))
        editSQL = " AND DE.CDE_ID <> '" +  sPublicID + "'";
 /*    if (setAction.equals("Edit"))
        editSQL = " AND DE.DE_IDSEQ <> '" +  mDE.getDE_DE_IDSEQ() + "'";
     else if(setAction.equals("NewDEVersion"))
        versSQL =  "' AND DE.CDE_ID <> '" + sPublicID;  */
     String sVDID = mDE.getDE_VD_IDSEQ();
     String sDECID = mDE.getDE_DEC_IDSEQ();
     String sContID = mDE.getDE_CONTE_IDSEQ();
     if (sPublicID == null) sPublicID = "";
     sSQL = "SELECT LONG_NAME FROM DATA_ELEMENTS_VIEW DE WHERE DE.VD_IDSEQ = '" + sVDID + 
             "' AND DE.DEC_IDSEQ = '" + sDECID +   //versSQL +
             "' AND DE.CONTE_IDSEQ = '" + sContID + 
             "'" + editSQL ;
             
     String sRegStat = mDE.getDE_REG_STATUS();
     if (sRegStat == null) sRegStat = "";
     String sName = getAC.isUniqueInContext(sSQL);
     if(sName == null || sName.equals(""))
       return "";
     else
     {
        if (sRegStat.equalsIgnoreCase("Standard") || sRegStat.equalsIgnoreCase("Candidate")
        || sRegStat.equalsIgnoreCase("Proposed"))
         return "Combination of DEC, VD and Context already exists in DE: " + sName;
       else
         return "Warning: Combination of DEC, VD and Context already exists in DE: " + sName;
     }
   }
 
   
  /**
   * To check whether data is unique value in the database for the selected component,
   * called from setValidatePageValuesDE, setValidatePageValuesDEC, setValidatePageValuesVD methods.
   * Creates the sql queries for the selected field, to check if the value exists in the database.
   * Calls 'getAC.doComponentExist' to execute the query.
   *
   * @param sField selected field.
   * @param ACType input data.
   * @param mDE Data Element Bean.
   * @param mDEC Data Element Concept Bean.
   * @param mVD Value Domain Bean.
   * @param getAC reference to GetACService class.
   *
   * @return String retValue message if exists already. Otherwise empty string.
   */
   public String checkUniqueInContext(String sField, String ACType, DE_Bean mDE,
          DEC_Bean mDEC, VD_Bean mVD, GetACService getAC, String setAction)
   {
     boolean bValidFlag = false;
     String sSQL=""; String sContext=""; String sValue=""; String sVersion=""; String editSQL="";
     String retValue = "Not unique within Context and Version. ";
     if (ACType.equals("DE"))
     {
       sContext = mDE.getDE_CONTEXT_NAME();
       if(sContext == null) sContext = "";
       sVersion = mDE.getDE_VERSION();
       if(sVersion == null) sVersion = "";
       //check unique if id is not the same for update
       if (setAction.equals("Edit"))
          editSQL = " AND DE.DE_IDSEQ <> '" +  mDE.getDE_DE_IDSEQ() + "'";
      /* if (sField.equals("LongName"))
       {
         sValue = mDE.getDE_LONG_NAME();
         sValue = m_util.parsedStringSingleQuoteOracle(sValue);
         sSQL = "SELECT COUNT(*) FROM DATA_ELEMENTS_VIEW DE, CONTEXTS_VIEW CV" +
                     " WHERE DE.CONTE_IDSEQ = CV.CONTE_IDSEQ AND DE.LONG_NAME = '" + sValue + "'" +
                     " AND DE.VERSION = '" + sVersion + "' AND CV.NAME = '" + sContext + "'" + editSQL;
       }
       else*/ if (sField.equals("Name"))
       {
         sValue = mDE.getDE_PREFERRED_NAME();
         sValue = m_util.parsedStringSingleQuoteOracle(sValue);
         sSQL = "SELECT COUNT(*) FROM DATA_ELEMENTS_VIEW DE, CONTEXTS_VIEW CV" +
                     " WHERE DE.CONTE_IDSEQ = CV.CONTE_IDSEQ AND DE.PREFERRED_NAME = '" + sValue + "'" +
                     " AND DE.VERSION = '" + sVersion + "' AND CV.NAME = '" + sContext + "'" + editSQL;
       }
   /*    else if (sField.equals("Definition"))
       {
         sValue = mDE.getDE_PREFERRED_DEFINITION();
         sValue = m_util.parsedStringSingleQuoteOracle(sValue);
         sSQL = "SELECT COUNT(*) FROM DATA_ELEMENTS_VIEW DE, CONTEXTS_VIEW CV" +
                     " WHERE DE.CONTE_IDSEQ = CV.CONTE_IDSEQ AND DE.PREFERRED_DEFINITION = '" + sValue + "'" +
                     " AND CV.NAME = '" + sContext + "'"  + editSQL;
         retValue = "Not unique within the Context. ";
       } */
       else if (sField.equals("Version"))
       {
         String deID = mDE.getDE_MIN_CDE_ID();
         sSQL = "SELECT COUNT(*) FROM DATA_ELEMENTS_VIEW DE WHERE DE.CDE_ID = '" + deID + 
                 "' AND DE.VERSION = '" + sVersion + "'";
         retValue = "Not unique within the Public ID.";
         
       /*  String sVDID = mDE.getDE_VD_IDSEQ();
         String sDECID = mDE.getDE_DEC_IDSEQ();
         String sContID = mDE.getDE_CONTE_IDSEQ();
         if(sVersion == null) sVersion = "";
         sSQL = "SELECT LONG_NAME FROM DATA_ELEMENTS_VIEW DE WHERE DE.VD_IDSEQ = '" + sVDID + "' AND DE.DEC_IDSEQ = '" +
                 sDECID + "' AND DE.VERSION = '" + sVersion + "' AND DE.CONTE_IDSEQ = '" + sContID + "'" + editSQL; */
       }
     }
     else if (ACType.equals("DEC"))
     {
       sContext = mDEC.getDEC_CONTEXT_NAME();
       sVersion = mDEC.getDEC_VERSION();
       //check unique if id is not the same for update
       if (setAction.equals("Edit"))
          editSQL = " AND DEC.DEC_IDSEQ <> '" +  mDEC.getDEC_DEC_IDSEQ() + "'";
 
      /* if (sField.equals("LongName"))
       {
         sValue = mDEC.getDEC_LONG_NAME();
         sValue = m_util.parsedStringSingleQuoteOracle(sValue);
         sSQL = "SELECT COUNT(*) FROM DATA_ELEMENT_CONCEPTS_VIEW DEC, CONTEXTS_VIEW CV" +
                     " WHERE DEC.CONTE_IDSEQ = CV.CONTE_IDSEQ AND DEC.LONG_NAME = '" + sValue + "'" +
                     " AND DEC.VERSION = '" + sVersion + "' AND CV.NAME = '" + sContext + "'" + editSQL;
       }
       else*/ if (sField.equals("Name"))
       {
         sValue = mDEC.getDEC_PREFERRED_NAME();
         sValue = m_util.parsedStringSingleQuoteOracle(sValue);
         sSQL = "SELECT COUNT(*) FROM DATA_ELEMENT_CONCEPTS_VIEW DEC, CONTEXTS_VIEW CV" +
                     " WHERE DEC.CONTE_IDSEQ = CV.CONTE_IDSEQ AND DEC.PREFERRED_NAME = '" + sValue + "'" +
                     " AND DEC.VERSION = '" + sVersion + "' AND CV.NAME = '" + sContext + "'" + editSQL;
       }
       else if (sField.equals("Version"))
       {
         String decID = mDEC.getDEC_DEC_ID();
         sSQL = "SELECT COUNT(*) FROM DATA_ELEMENT_CONCEPTS_VIEW DEC WHERE DEC.DEC_ID = '" + decID + 
                 "' AND DEC.VERSION = '" + sVersion + "'";
         retValue = "Not unique within the Public ID.";
       }
    /*   else if (sField.equals("Definition"))
       {
         sValue = mDEC.getDEC_PREFERRED_DEFINITION();
         sValue = m_util.parsedStringSingleQuoteOracle(sValue);
         sSQL = "SELECT COUNT(*) FROM DATA_ELEMENT_CONCEPTS_VIEW DEC, CONTEXTS_VIEW CV" +
                     " WHERE DEC.CONTE_IDSEQ = CV.CONTE_IDSEQ AND DEC.PREFERRED_DEFINITION = '" + sValue + "'" +
                     " AND CV.NAME = '" + sContext + "'" + editSQL;
         retValue = "Not unique within the Context. ";
       } */
     }
     else if (ACType.equals("VD"))
     {
       sContext = mVD.getVD_CONTEXT_NAME();
       sVersion = mVD.getVD_VERSION();
       //check unique if id is not the same for update
       if (setAction.equals("Edit"))
          editSQL = " AND VD.VD_IDSEQ <> '" +  mVD.getVD_VD_IDSEQ() + "'";
 
     /*  if (sField.equals("LongName"))
       {
         sValue = mVD.getVD_LONG_NAME();
         sValue = m_util.parsedStringSingleQuoteOracle(sValue);
         sSQL = "SELECT COUNT(*) FROM VALUE_DOMAINS_VIEW VD, CONTEXTS_VIEW CV" +
                     " WHERE VD.CONTE_IDSEQ = CV.CONTE_IDSEQ AND VD.LONG_NAME = '" + sValue + "'" +
                     " AND VD.VERSION = '" + sVersion + "' AND CV.NAME = '" + sContext + "'" + editSQL;
       }
       else*/ if (sField.equals("Name"))
       {
         sValue = mVD.getVD_PREFERRED_NAME();
         sValue = m_util.parsedStringSingleQuoteOracle(sValue);
         sSQL = "SELECT COUNT(*) FROM VALUE_DOMAINS_VIEW VD, CONTEXTS_VIEW CV" +
                     " WHERE VD.CONTE_IDSEQ = CV.CONTE_IDSEQ AND VD.PREFERRED_NAME = '" + sValue + "'" +
                     " AND VD.VERSION = '" + sVersion + "' AND CV.NAME = '" + sContext + "'" + editSQL;
       }
       else if (sField.equals("Version"))
       {
         String vdID = mVD.getVD_VD_ID();
         sSQL = "SELECT COUNT(*) FROM VALUE_DOMAINS_VIEW VD WHERE VD.VD_ID = '" + vdID + 
                 "' AND VD.VERSION = '" + sVersion + "'";
         retValue = "Not unique within the Public ID.";
       }
     /*  else if (sField.equals("Definition"))
       {
         sValue = mVD.getVD_PREFERRED_DEFINITION();
         sValue = m_util.parsedStringSingleQuoteOracle(sValue);
         sSQL = "SELECT COUNT(*) FROM VALUE_DOMAINS_VIEW VD, CONTEXTS_VIEW CV" +
                     " WHERE VD.CONTE_IDSEQ = CV.CONTE_IDSEQ AND VD.PREFERRED_DEFINITION = '" + sValue + "'" +
                     " AND CV.NAME = '" + sContext + "'" + editSQL;
         retValue = "Not unique within the Context. ";
       } */
     }
  /*   if(sField.equals("Version"))
     {
       String sName = getAC.isUniqueInContext(sSQL);
       if(sName == null || sName.equals(""))
         return "";
       else
         return "Combination of DEC, VD, and Version already exists in this context as DE:  " + sName;
     }
     else
     { */
         bValidFlag = getAC.doComponentExist(sSQL);
         if (bValidFlag == true)
           return retValue;
         else
           return "";
    // }
   }
 
   
   public String checkDECOCExist(String sDECid, HttpServletRequest req, HttpServletResponse res)
   {
     String strInvalid = "Associated Data Element Concept must have an Object Class.";
     try
     {
       Vector vList = new Vector();
       //get the DEC attributes from the ID
       GetACSearch serAC = new GetACSearch(req, res, m_servlet);
       serAC.doDECSearch(sDECid, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", 0, "", "", vList); 
       if (vList != null)
       {
         //loop through hte list and find if oc exists
         for (int i=0; i<vList.size(); i++)
         {
           DEC_Bean dec = (DEC_Bean)vList.elementAt(i);
           String sOC = dec.getDEC_OC_CONDR_IDSEQ();
           if (sOC != null && !sOC.equals(""))
             return "";  //found one
         }
       }
     }
     catch (Exception e)
     {
       logger.fatal("SetACService_checkDECOCExist- Unable to check if OC exists " + e.toString());
     }
     return strInvalid;
   }
   
   
  /**
   * To check whether data is unique value in the database for the building blocks,
   * called from setValidatePageValuesDEC, setValidatePageValuesVD methods.
   * Creates the sql queries for the selected type, to check if the value exists in the database.
   * Calls 'getAC.doBlockExist' to execute the query.
   *
   * @param ACType String selected block type.
   * @param sContext String selected context.
   * @param sValue String field value.
   * @param getAC reference to GetACService class.
   *
   * @return String retValue message if exists already. Otherwise empty string.
   */
   public String checkConceptCodeExistsInOtherDB(Vector vAC, InsACService insAC, VM_Bean m_VM) throws Exception
   {
       String strInValid = "";
       String sRet = "";
       boolean blnBadData = false;
       if(vAC != null)
       {
         for (int m=0; m<vAC.size(); m++)
         {
           EVS_Bean evsBean = (EVS_Bean)vAC.elementAt(m);
           if(evsBean != null && evsBean.getCON_AC_SUBMIT_ACTION() != null && !evsBean.getCON_AC_SUBMIT_ACTION().equals("DEL"))
           {
             strInValid = insAC.getConcept(sRet, evsBean, true);
             if(strInValid == null)
               strInValid = "";
             if(strInValid.length()>2)
             {
               if(!strInValid.substring(0,2).equals("An"))
                 strInValid = "";
               else
                 break;
             }
           }
         }
       }
       else if(m_VM != null)
       {
         EVS_Bean evsBean = m_VM.getVM_CONCEPT();
         if(evsBean != null)
         {
             strInValid = insAC.getConcept(sRet, evsBean, true);
             if(strInValid == null)
               strInValid = "";
             if(strInValid.length()>2)
             {
               if(!strInValid.substring(0,2).equals("An"))
                 strInValid = "";
             }
         }
       }
 //System.out.println("done checkConceptCodeExistsInOtherDB");
       return strInValid;
 
   }
               
   /**
   * To check whether data is unique value in the database for the building blocks,
   * called from setValidatePageValuesDEC, setValidatePageValuesVD methods.
   * Creates the sql queries for the selected type, to check if the value exists in the database.
   * Calls 'getAC.doBlockExist' to execute the query.
   *
   * @param ACType String selected block type.
   * @param sContext String selected context.
   * @param sValue String field value.
   * @param getAC reference to GetACService class.
   *
   * @return String retValue message if exists already. Otherwise empty string.
   */
   public String checkDEUsingDEC(String ACType, DEC_Bean m_DEC, GetACSearch getAC) throws Exception
   {
       String strInValid = "";
      // Vector vList = new Vector();
       if (ACType != null && ACType.equals("ObjectClass"))
       {
         Vector vRes = new Vector();
         String sID = m_DEC.getDEC_DEC_IDSEQ();
         if (sID != null && !sID.equals(""))
           getAC.doDESearch("", "", "","","","", 0, "", "", "", "", "", "", "", "", "", 
                     "", "", "", "", "", "", "", "", "", sID, "", "", vRes);
         if (vRes != null && vRes.size()>0)
         {
           String sRegStatus = "", sDEName = "";
           for (int i=0; i<vRes.size(); i++)
           {
             DE_Bean de = (DE_Bean)vRes.elementAt(i);
             String sReg = de.getDE_REG_STATUS();
             if (sReg == null) sReg = "";
             String sDE = de.getDE_LONG_NAME();
             if(sReg.equalsIgnoreCase("Standard") || sReg.equalsIgnoreCase("Candidate")
               || sReg.equalsIgnoreCase("Proposed"))
             {
                if (!sRegStatus.equals("")) sRegStatus = sRegStatus + ", ";
                sRegStatus = sRegStatus + sReg;
                if (!sDEName.equals("")) sDEName = sDEName + ", ";
                sDEName = sDEName + sDE;
             }            
           }
           if (!sDEName.equals(""))
               strInValid = "A Data Element of name " + sDEName + " uses this DEC " +
               "and the DE has a Registration Status of " + sRegStatus + " so the DEC must have an Object Class. \n";          
         }
       }
       return strInValid;
   }
 
 
  /**
   * To check whether data is unique value in the database for the building blocks,
   * called from setValidatePageValuesDEC, setValidatePageValuesVD methods.
   * Creates the sql queries for the selected type, to check if the value exists in the database.
   * Calls 'getAC.doBlockExist' to execute the query.
   *
   * @param ACType String selected block type.
   * @param sContext String selected context.
   * @param sValue String field value.
   * @param getAC reference to GetACService class.
   *
   * @return String retValue message if exists already. Otherwise empty string.
   */
   public String checkUniqueBlock(String ACType, String sContext, String sValue, GetACSearch getAC, String strInValid, String sASLName)
   {
       String sSQL = "";
       Vector vList = new Vector();
       if (ACType != null && ACType.equals("ObjectClass"))
       {
          getAC.do_caDSRSearch(sValue, sContext, sASLName, "", vList, "OC");
          if (vList.size() > 0)
          {
             EVS_Bean OCBean = new EVS_Bean();
             OCBean = (EVS_Bean)vList.elementAt(0);
             String name = OCBean.getLONG_NAME();
             strInValid = "Object Class already exists in this context as name " + name + ". Select this name from caDSR search results. \n";
             return strInValid;
          }
          else
               return strInValid;
       }
       else if (ACType.equals("Property"))
       {
          getAC.do_caDSRSearch(sValue, sContext, sASLName, "", vList, "PROP");
          if (vList.size() > 0)
          {
             EVS_Bean PCBean = new EVS_Bean();
             PCBean = (EVS_Bean)vList.elementAt(0);
             String name = PCBean.getLONG_NAME();
             strInValid = "Property already exists in this context as name " + name + ". Select this name from caDSR search results. \n";
             return strInValid;
          }
          else
             return strInValid;
       }
       else if (ACType.equals("RepTerm"))
       {
          getAC.do_caDSRSearch(sValue, sContext, sASLName, "", vList, "REP");
          if (vList.size() > 0)
          {
             EVS_Bean PCBean = new EVS_Bean();
             PCBean = (EVS_Bean)vList.elementAt(0);
             String name = PCBean.getLONG_NAME();
             strInValid = "Rep Term already exists in this context as name " + name + ". Select this name from caDSR search results. \n";
             return strInValid;
          }
          else
             return strInValid;
       }
       else if (ACType.equals("ObjectQualifier") || ACType.equals("PropertyQualifier") || ACType.equals("RepQualifier"))
       {
          getAC.do_caDSRSearch(sValue, sContext, "", "", vList, "Q");
          if (vList.size() > 0)
          {
             EVS_Bean PCBean = new EVS_Bean();
             PCBean = (EVS_Bean)vList.elementAt(0);
             String name = PCBean.getLONG_NAME();
             strInValid = "Qualifier already exists in this context as name " + name + ". Select this name from caDSR search results. \n";
             return strInValid;
          }
          else
             return strInValid;
       }
       return strInValid;
   }
 
  /**
   * This method is no longer in use.
   * To check whether DE name contains Object Class, Property and Rep Term, called from nowhere.
   *
   * @param req HttpServletRequest object.
   *
   * @return String strValid message if invalid. otherwise empty string.
   */
   public String checkObjectPropertyAndRepTerms(HttpServletRequest req)
   {
     String returnString;
     String returnString1;
     String returnString2;
     String returnString3;
     String sTmp1, sTmp2, sTmp3;
 
     sTmp1 = (String)req.getParameter("selObjectClass");
     sTmp2 = (String)req.getParameter("selPropertyClass");
     sTmp3 = (String)req.getParameter("RepTerm");
 
     if (sTmp1 != null && !(sTmp1.equals("")))
       returnString1 = "";
     else
       returnString1 = "Object term must be found in DE Long Name. \n";
 
     if (sTmp2 != null && !(sTmp2.equals("")))
       returnString2 = "";
     else
       returnString2 = "Property term must be present in DE Long Name. \n";
       
     if (sTmp3 != null && !(sTmp3.equals("")))
       returnString3 = "";
     else
       returnString3 = "Representation term must be present in DE Long Name. \n";
       
     return returnString1 + returnString2 + returnString3;
   }
 
 
  /**
   * This method is no longer in use.
   * To check whether DEC name contains Object Class, Property, called from nowhere.
   *
   * @param s DEC long name.
   * @param sDEObj Object class name.
   * @param sDEProp Property Name.
   *
   * @return String strValid message if not found in the name. otherwise empty string.
   */
   public String checkForLongNameObjectAndPropertyInDECName(String s, String sDEObj, String sDEProp)
   {
     String returnString;
     String returnString2;
     //check whether sDECObj and DECProp substrings are in Long Name string s
     boolean bContainsDEObjSubstring = (s.indexOf(sDEObj) >= 0);
     boolean bContainsDEPropSubstring = (s.indexOf(sDEProp) >= 0);
 
     if (bContainsDEObjSubstring == true)
       returnString = "";
     else
       returnString = "DE Long Name Object term must be found in DEC Name. \n";
 
     if (bContainsDEPropSubstring == true)
       returnString2 = "";
     else
       returnString2 = "DE Long Name Property term must be found in DEC Name. \n";
       
     return returnString + returnString2;
   }
 
  /**
   * This method is no longer in use.
   * To check whether VD name contains Rep Term, called from nowhere.
   *
   * @param s VD long name.
   * @param sDERep Represention term name.
   *
   * @return String strValid message if not found in the name. otherwise empty string.
   */
   public String checkForVDRepresentationInDELongName(String s, String sDERep)
   {
       // Will check whether DE Long Name representation term is in the VD Name;
       // if not, the VD Name Representation term is not equal to the DE Representation term
       boolean bVDNameContainsDERepSubstring = (s.indexOf(sDERep) >= 0);
       if (bVDNameContainsDERepSubstring == true)
         return "";
       else
         return "Value Domain Representation term not found in DE Long Name. \n";
    }
 
  /**
   * To check Unit of Measurement and Format attributes of Value Domain components are valid, called from setValidatePageValuesVD method.
   *
   * @return String strValid message if not valid. otherwise empty string.
   */
   public String checkValueDomainIsTypeMeasurement()
   {
     boolean bValidFlag = true;
     // validation code here
     if (bValidFlag == true)
       return "";
     else
       return "Value Domain is not of type Measurement. \n";
   }
 
    /**
   * To check data is less than 8 characters, called from setValidatePageValuesVD method.
   *
   * @param sValue data to check.
   *
   * @return String strValid message if character is greater than 8 characters. otherwise empty string.
   */
    public String checkLessThan8Chars(String s)
    { 
       int s_length = s.length();
       if(s_length < 9)
         return "";
       else
         return " Maximum Length must be less than 100,000,000. \n"; 
    }
 
    /**
   * To version data dimension is 4,2, called from setValidatePageValuesVD method.
   * So, Version should be less that eqaal or less than 99.99 with two decimal character length and two whole number legnth.
   *
   * @param sValue data to check.
   *
   * @return String strValid message if character is greater than 99.99. otherwise empty string.
   */
    public String checkVersionDimension(String sValue)
    { 
       String strValid = this.checkValueIsNumeric(sValue, "Version");
       //return if not a numeric data
       if (strValid != null && !strValid.equals(""))
         return strValid;
       else
       {
         strValid = "Version number must be less than 100 with only two digit decimal values. \n";
 
         //invalid if length is greater than five
         if (sValue.length()>5)
           return strValid; //isValid = false;
         //invalid if point is not found and whole number is greater than 2.  
         else if (sValue.indexOf(".") == -1 && sValue.length()>2)
           return strValid;
         //invalid if first half from the point is greater than 2
         else if (sValue.indexOf(".") != -1 && sValue.substring(0, sValue.indexOf(".")).length()>2)
           return strValid;
         //invalid if the second half from the point is greater than 2
         else if (sValue.indexOf(".") != -1 && sValue.substring(sValue.indexOf(".") + 1).length()>2)
           return strValid;
         //invalid if another point is found in the data
         else if (sValue.indexOf(".") != -1 && sValue.substring(sValue.indexOf(".") + 1).indexOf(".") >= 0)
           return strValid;
       }
       return "";
    }
 
  /**
   * To check data is numeric, called from setValidatePageValuesVD method.
   *
   * @param sValue data to check.
   *
   * @return String strValid message if character is not numeric. otherwise empty string.
   */
   public String checkValueIsNumeric(String sValue, String sField)
   {
     String sValid = "";
     char aLetter;
     if(sValue == null) sValue = "";
     for (int i=0; i < sValue.length(); i++)
     {
       aLetter = sValue.charAt(i);
       if ((Character.isDigit(aLetter)) || (Character.isWhitespace(aLetter)) || (aLetter == '.'))
       {
         if (aLetter == '.' && sField.equals("Decimal Place"))
         {
           sValid = "Must contain only positive numbers. \n";
           break;
         }
         else
           sValid = "";
       }
       else
       {
         sValid = "Must contain only positive numbers. \n";
         break;
       }
     }
     return sValid;
   }
 
   /**
   * To truncate term from EVS to 30 characters
   *
   * @param sValue data to check.
   *
   * @return String strValid message if character is not numeric. otherwise empty string.
   */
   public String truncateTerm(String sValue)
   {
     if(sValue.length() > 0)
       sValue = sValue.substring(0,30);
     return sValue;
   }
 
  /**
   * To check data is Integer, called from setValidatePageValuesVD method.
   *
   * @param sValue data to check.
   * @param sField attribute name.
   *
   * @return String strValid message if Value Domain is not a numeric Data Type.
   * Otherwise empty string.
   */
   public String checkValueDomainIsNumeric(String sValue, String sField)
   {
     boolean bValidFlag = true;
     // validation code here
     if (bValidFlag == true)
       return "";
     else
       return sField + " must contain only positive numbers.";
   }
 
  /**
   * To set the valid page vector with attribute, data and message, called from setValidatePageValuesDE,
   * setValidatePageValuesDEC, setValidatePageValuesVD, setValidatePageValuesPV, setValidatePageValuesVM methods.
   * Attribute Name and Data added to the vector.
   * Checks if satisfies mandatory, length limit valid and adds the appropriate messages along with earlier message to the vecotr.
   *
   * @param v vector to display data on the page.
   * @param sItem Name of the attribute.
   * @param sContent input value of the attribute.
   * @param bMandatory true if attribute is a mandatory for submit.
   * @param iLengLimit integer value for length limit if any for the attribute.
   * @param strInValid invalid messages from other validation checks.
   *
   * @return String strValid message if character is not a number. otherwise empty string.
   */
    public void setValPageVector(Vector v, String sItem, String sContent, boolean bMandatory, int iLengLimit, String strInValid, String sOriginAction)
    {
       String sValid = "Valid";
       String sNoChange = "No Change";
       String sMandatory = "This field is Mandatory. \n";
       if(sItem.equals("Effective End Date"))
         sMandatory = "Effective End Date field is Mandatory for this workflow status. \n";
       
       v.addElement(sItem);
       if(sContent == null || sContent.equals("") || sContent.length() < 1)   // content emplty
       {
           v.addElement("");   // content
           if(bMandatory)
           {
               v.addElement(sMandatory + strInValid);   //status, required field
           }
           else if(strInValid.equals(""))
           {
               if (sOriginAction.equals("BlockEditDE") || sOriginAction.equals("BlockEditDEC") || sOriginAction.equals("BlockEditVD"))
                 v.addElement(sNoChange);   //status, OK, even empty, not require
               else
                 v.addElement(sValid);
           }
           else
               v.addElement(strInValid);
       }
       else                      // have something in content
       {
           v.addElement(sContent);   // content
           if(iLengLimit > 0)    // has length limit
           {
               if(sContent.length() > iLengLimit)  // not valid
               {
                   v.addElement(sItem + " is too long. \n" + strInValid);
               }
               else
               {
                 if (strInValid.equals(""))
                   v.addElement(sValid);   //status, OK, not exceed limit
                 else
                   v.addElement(strInValid);
               }
           }
           else
           {
             if(strInValid.equals(""))
                 v.addElement(sValid);   //status, OK, not exceed limit
             else
                 v.addElement(strInValid);
           }
       }
   }
 
 /**
  * To set the values from request to Data Element Bean, called from NCICurationServlet.
  *
  * @param req The HttpServletRequest object.
  * @param res HttpServletResponse object.
  * @param m_DE Data Element Bean.
  *
  * @throws IOException  If an input or output exception occurred
  * @throws ServletException  when servlet exception occured
  */
  public void setDEValueFromPage(HttpServletRequest req,
           HttpServletResponse res, DE_Bean m_DE) throws ServletException,IOException
  {
     HttpSession session = req.getSession();
     String sOriginAction = (String)session.getAttribute("originAction");
     if (sOriginAction == null) sOriginAction.equals("");
     String sMenu = (String)session.getAttribute("MenuAction");
       
     //get the selected contexts from the DE bean
     DE_Bean selDE = (DE_Bean)session.getAttribute("m_DE");      
     //keep the seach de attributes if menu action is editdesde
     //make sure the selected context is saved 
     m_DE.setDE_SELECTED_CONTEXT_ID(selDE.getDE_SELECTED_CONTEXT_ID());
     String sIdx, sID;
     String sName = "";
     
     if(sOriginAction.equals("BlockEditDE"))
       sID = "";
     else
       sID = (String)req.getParameter("selContext");
 
     if ((sID != null) || (!sID.equals("")))
     {
       sName = m_util.getNameByID((Vector)session.getAttribute("vContext"),
             (Vector)session.getAttribute("vContext_ID"), sID);
       m_DE.setDE_CONTE_IDSEQ(sID);
       if(sName != null) m_DE.setDE_CONTEXT_NAME(sName);
     }
 
  //   if (!sMenu.equals("EditDesDE"))
  //   {
     sID = (String)req.getParameter("deIDSEQ");
     if(sID != null)
     {
       m_DE.setDE_DE_IDSEQ(sID);
     }
 
     sID = (String)req.getParameter("CDE_IDTxt");
     if(sID != null)
     {
       m_DE.setDE_MIN_CDE_ID(sID);
     }
 
     String sDECid[] = req.getParameterValues("selDEC");
     if(sDECid != null)
     {
       sID = sDECid[0];
       m_DE.setDE_DEC_IDSEQ(sID);
       sName = (String)req.getParameter("selDECText");
       if(sName != null) m_DE.setDE_DEC_NAME(sName);
     }
 
     String sVDid[] = req.getParameterValues("selVD");
     if(sVDid != null)
     {
       sID = sVDid[0];
       m_DE.setDE_VD_IDSEQ(sID);
       sName = (String)req.getParameter("selVDText");
       if (sName != null) m_DE.setDE_VD_NAME(sName);
     }
 
     //set LONG_NAME
     String sTmp;
     String sName2;
 
     if(sOriginAction.equals("BlockEditDE"))
       sName = "";
     else
       sName = (String)req.getParameter("txtLongName");
     if(sName != null)
     {
       sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
       m_DE.setDE_LONG_NAME(sName);
     }
 
     if(sOriginAction.equals("BlockEditDE"))
       sName = "";
     else
       sName = (String)req.getParameter("txtPreferredName");
     if(sName != null)
     {
       sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
       m_DE.setDE_PREFERRED_NAME(sName);
     /*  String sNameType = (String)req.getParameter("rNameConv");
       m_DE.setAC_PREF_NAME_TYPE(sNameType);
       String sSysName = m_DE.getAC_SYS_PREF_NAME();
       String sAbbName = m_DE.getAC_ABBR_PREF_NAME();
       //make sure to capture the user typed name at any page refresh.
       if (sName != null && !sName.equals("") && !sName.equals("(Generated by the System)") 
         && !sName.equals(sSysName) && !sName.equals(sAbbName) && sNameType != null && sNameType.equals("USER"))
         m_DE.setAC_USER_PREF_NAME(sName);  */
     }
 
     //set DE_PREFERRED_DEFINITION
     sName = (String)req.getParameter("CreateDefinition");
     if (sName != null)
     {
       sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
       m_DE.setDE_PREFERRED_DEFINITION(sName);
     }
 
     //set DOC_TEXT_LONG_NAME
     sName = (String)req.getParameter("CreateDocumentText");
     if(sName != null)
     {
       sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
       m_DE.setDOC_TEXT_LONG_NAME(sName);
     }
     //set DOC_TEXT_LONG_NAME
     sName = (String)req.getParameter("doctextIDSEQ");
     if(sName != null)
     {
       m_DE.setDOC_TEXT_LONG_NAME_IDSEQ(sName);
     }
 
     //set DE_SOURCE
     sName = (String)req.getParameter("selSource");
     if(sName != null)
     {
       m_DE.setDE_SOURCE(sName);
     }
     //set DE_SOURCE
     sName = (String)req.getParameter("sourceIDSEQ");
     if(sName != null)
     {
       m_DE.setDE_SOURCE_IDSEQ(sName);
     }
 
     //set DE_BEGIN_DATE
     sName = (String)req.getParameter("BeginDate");
     if(sName != null)
     {
       m_DE.setDE_BEGIN_DATE(sName);
     }
 
     //set DE_END_DATE
     sName = (String)req.getParameter("EndDate");
     if(sName != null)
     {
       m_DE.setDE_END_DATE(sName);
     }
 
     //set DE_CHANGE_NOTE
     sName = (String)req.getParameter("CreateChangeNote");
     if(sName != null)
     {
       sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
       m_DE.setDE_CHANGE_NOTE(sName);
     }
 
     //set DE_VERSION
     if(sOriginAction.equals("BlockEditDE"))
     {
       sName = (String)req.getParameter("VersionCheck");
       if(sName == null)
         sName = "";
       else
       {
         sName = (String)req.getParameter("WholeCheck");
         if(sName == null)
         {
           sName = (String)req.getParameter("PointCheck");
           if(sName != null)
             m_DE.setDE_VERSION("Point");
         }
         else
           m_DE.setDE_VERSION("Whole");
       }
     }
     else
     {
       sName = (String)req.getParameter("Version");
       if(sName != null)
       {
         sName = sName.trim();
         String isNum = this.checkValueIsNumeric(sName, "Version");
         //if numeric and no . and less than 2 length add .0 in the end.
         if ((isNum == null || isNum.equals("")) && (sName.indexOf(".") == -1 && sName.length() < 3))
           sName = sName + ".0";          
         m_DE.setDE_VERSION(sName);          
       }
     }
 
     //set DE_ASL_NAME
     sName = (String)req.getParameter("selStatus");
     if (sName != null)
       m_DE.setDE_ASL_NAME(sName);
 
     //set DE_REG_STATUS
     sName = (String)req.getParameter("selRegStatus");
     if (sName != null)
       m_DE.setDE_REG_STATUS(sName);
 
     //set DE_REG_STATUS_ID
     sName = (String)req.getParameter("regStatusIDSEQ");
     if (sName != null)
       m_DE.setDE_REG_STATUS_IDSEQ(sName);
     
     sName = (String)req.getParameter("DECDefinition");
     if(sName != null)
     {
       sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
       m_DE.setDE_DEC_Definition(sName);
     }
 
     sName = (String)req.getParameter("VDDefinition");
     if(sName != null)
     {
       sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
       m_DE.setDE_VD_Definition(sName);
     }
       
     //cs-csi relationship
     m_DE = this.setDECSCSIfromPage(req, m_DE);
  } // end of setDEValueFromPage
 
 
   /**
    * To set the values from request to Data ElementConcept Bean, called from NCICurationServlet.
    *
    * @param req The HttpServletRequest object.
    * @param res HttpServletResponse object.
    * @param m_DEC Data Element Concept Bean.
    *
    * @throws IOException  If an input or output exception occurred
    * @throws ServletException  when servlet exception occured
    */
   public void setDECValueFromPage(HttpServletRequest req,
           HttpServletResponse res, DEC_Bean m_DEC) throws ServletException,IOException
   {
  System.err.println("in setDECVals");
       HttpSession session = req.getSession();
       String sOriginAction = (String)session.getAttribute("originAction");
       if (sOriginAction == null) sOriginAction.equals("");
 
       //get the selected contexts from the DE bean
       DEC_Bean selDEC = (DEC_Bean)session.getAttribute("m_DEC");  
       m_DEC.setDEC_SELECTED_CONTEXT_ID(selDEC.getDEC_SELECTED_CONTEXT_ID());
            
       String sIdx, sID;
       String sName = "";
  
       sID = (String)req.getParameter("decIDSEQ");
       if(sID != null)
         m_DEC.setDEC_DEC_IDSEQ(sID);
 
       sID = (String)req.getParameter("CDE_IDTxt");
       if(sID != null)
         m_DEC.setDEC_DEC_ID(sID);
 
       if(sOriginAction.equals("BlockEditDEC"))
         sID = "";
       else
         sID = (String)req.getParameter("selContext");
 //System.err.println("in setDECVals1");
       if ((sID != null) || (!sID.equals("")))
       {
         sName = m_util.getNameByID((Vector)session.getAttribute("vContext"), (Vector)session.getAttribute("vContext_ID"), sID);
         m_DEC.setDEC_CONTE_IDSEQ(sID);
         m_DEC.setDEC_CONTEXT_NAME(sName);
       }
       
       String s = (String)req.getParameter("txtObjClass");
       if(s != null)
          m_DEC.setDEC_OCL_NAME(s);
          
       s = (String)req.getParameter("txtPropClass");
       if(s != null)
         m_DEC.setDEC_PROPL_NAME(s);
         
  System.err.println("in setDECVals2");
       sName = "";
       if(sOriginAction.equals("BlockEditDEC"))
         sName = "";
       else
         sName = (String)req.getParameter("txtLongName");
       if(sName != null)
       {
         sName = m_util.removeNewLineChar(sName);
         m_DEC.setDEC_LONG_NAME(sName);
       }
 
       //set PREFERRED_NAME
       if(sOriginAction.equals("BlockEditDEC"))
         sName = "";
       else
         sName = (String)req.getParameter("txtPreferredName");
       if(sName != null)
       {
         sName = m_util.removeNewLineChar(sName);
         m_DEC.setDEC_PREFERRED_NAME(sName);
       }
 
       //set DEC_PREFERRED_DEFINITION
       sName = (String)req.getParameter("CreateDefinition");
       if(sName != null)
       {
         sName = m_util.removeNewLineChar(sName);
         m_DEC.setDEC_PREFERRED_DEFINITION(sName);
       }
 
       sID = (String)req.getParameter("selConceptualDomain");
       if(sID != null)
       {
         m_DEC.setDEC_CD_IDSEQ(sID);
         sName = (String)req.getParameter("selConceptualDomainText");
         if ((sName == null) || (sName.equals("")))
         {
           if ((Vector)session.getAttribute("vCD") != null)
             sName = m_util.getNameByID((Vector)session.getAttribute("vCD"),(Vector)session.getAttribute("vCD_ID"), sID);
         }
         if(sName != null) m_DEC.setDEC_CD_NAME(sName);
       }
 
       //set DEC_SOURCE
       sName = (String)req.getParameter("selSource");
       if(sName != null)
         m_DEC.setDEC_SOURCE(sName);
 
 
       //set DEC_BEGIN_DATE
       sName = (String)req.getParameter("BeginDate");
       if(sName != null);
          m_DEC.setDEC_BEGIN_DATE(sName);
 
       //set DEC_END_DATE
       sName = (String)req.getParameter("EndDate");
       if(sName != null)
         m_DEC.setDEC_END_DATE(sName);
 
       //set DE_CHANGE_NOTE
       sName = (String)req.getParameter("CreateChangeNote");
       if(sName != null)
       {
         sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
         m_DEC.setDEC_CHANGE_NOTE(sName);
       }
 
        //set DEC_VERSION
       if(sOriginAction.equals("BlockEditDEC"))
       {
         sName = (String)req.getParameter("VersionCheck");
         if(sName == null)
           sName = "";
         else
         {
           sName = (String)req.getParameter("WholeCheck");
           if(sName == null)
           {
             sName = (String)req.getParameter("PointCheck");
             if(sName != null)
               m_DEC.setDEC_VERSION("Point");
           }
           else
             m_DEC.setDEC_VERSION("Whole");
         }
       }
       else
       {
         sName = (String)req.getParameter("Version");
         if(sName != null)
         {
           sName = sName.trim();
           String isNum = this.checkValueIsNumeric(sName, "Version");
           //if numeric and no . and less than 2 length add .0 in the end.
           if ((isNum == null || isNum.equals("")) && (sName.indexOf(".") == -1 && sName.length() < 3))
             sName = sName + ".0";          
           m_DEC.setDEC_VERSION(sName);          
         }
       }
 
       //set DEC_ASL_NAME
       sName = (String)req.getParameter("selStatus");
       if(sName != null)
         m_DEC.setDEC_ASL_NAME(sName);
 
        //set DE_CHANGE_NOTE
       sName = (String)req.getParameter("CreateChangeNote");
       if(sName != null)
       {
         sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
         m_DEC.setDEC_CHANGE_NOTE(sName);
       }
        
       //cs-csi relationship
       String[] sNAMEs = req.getParameterValues("selCSNAMEHidden");
       m_DEC.setDEC_CS_NAME(this.getSelectionFromPage(sNAMEs));
 
       //get associated ac-csi
       Vector vCSCSIs = new Vector(), vACCSIs = new Vector(), vACs = new Vector(), vACNames = new Vector();
       String[] sIDs, sACCSIs, sACs;
       String sACCSI, sAC;
       //get selected cs-csi
       sIDs = req.getParameterValues("selCSCSIHidden");
       sACCSIs = req.getParameterValues("selACCSIHidden");
       sACs = req.getParameterValues("selACHidden");
       Vector vNames = (Vector)session.getAttribute("vACName");
       Vector vIDs = (Vector)session.getAttribute("vACId");
       
       if(sIDs != null)
       {
         for (int i=0;i<sIDs.length;i++)
         {
           sID = sIDs[i];
           sACCSI = sACCSIs[i];
           sAC = sACs[i];
           if (sACCSI == null)  sACCSI = "";
           if (sAC == null)  sAC = m_DEC.getDEC_DEC_IDSEQ();
           if ((sID != null) && (!sID.equals("")))
           {
             vCSCSIs.addElement(sID);
             vACCSIs.addElement(sACCSI);
             vACs.addElement(sAC);
             //get the ac name   
             String acName = m_DEC.getDEC_LONG_NAME();
             if (sAC != null && vNames != null && vIDs != null)
             {
               if (vIDs.indexOf(sAC) >= 0)              
                 acName = (String)vNames.elementAt(vIDs.indexOf(sAC));
             }
             vACNames.addElement(acName);
           }
         }
       }
       m_DEC.setDEC_CS_CSI_ID(vCSCSIs);
       
       //store accsi bean list list in the session
       Vector vCSList = (Vector)session.getAttribute("CSCSIList");
       Vector vList = getACCSIFromPage(vCSCSIs, vACCSIs, vCSList, vACs, vACNames);
       m_DEC.setDEC_AC_CSI_VECTOR(vList);
       
       //get associated ac-csi
       sIDs = req.getParameterValues("selACCSIHidden");
       m_DEC.setDEC_AC_CSI_ID(this.getSelectionFromPage(sIDs));
       
       //get associated cs-id
       sIDs = req.getParameterValues("selectedCS");
       m_DEC.setDEC_CS_ID(this.getSelectionFromPage(sIDs));    
 //System.err.println("done setDECVals");
   } // end of setDECValueFromPage
 
   /**
    * stores the values from string array into a vector
    * returns back the vector.
    * 
    * @param sSelectionList array of string.
    * @return Vector of elements from the array.
    * @throws ServletException, IOException
    */
   private Vector getSelectionFromPage(String[] sSelectionList) throws ServletException,IOException
   {
     Vector vSelections = new Vector();
       
     if(sSelectionList != null)
     {
       for (int i=0;i<sSelectionList.length;i++)
       {
         String sID = sSelectionList[i];
         if ((sID != null) && (!sID.equals("")))
           vSelections.addElement(sID);
       }
     }
     return vSelections;
   }
   
   /**
    * 
    * @param vCSCSIs
    * @param vACCSIs
    * @param vCSCSIList
    * @param vACs
    * @param vAC_Name
    * @return 
    * @throws javax.servlet.ServletException
    * @throws java.io.IOException
    */
  private Vector getACCSIFromPage(Vector vCSCSIs, Vector vACCSIs, Vector vCSCSIList, 
       Vector vACs, Vector vAC_Name) throws ServletException,IOException
  {
     Vector vACCSIList = new Vector();   //get selected CSCSI atributes of this AC
     //loop through the cscsilist to get csi attributes
     if (vCSCSIs != null && vCSCSIs.size()>0)
     {        
       Vector vCSINames = new Vector();
       Vector vCSNames = new Vector();
       //get all cs-csi attributes from the list
       for (int i=0; i<vCSCSIList.size(); i++)
       {
         CSI_Bean csiBean = (CSI_Bean)vCSCSIList.elementAt(i);
         String sCSCSIid = csiBean.getCSI_CSCSI_IDSEQ();
           
         //match the cscsiid from the bean with the selected cscsi id vector
         for (int j=0; j<vCSCSIs.size(); j++)
         {
           String sCSIID = (String)vCSCSIs.elementAt(j);
           if (sCSIID.equalsIgnoreCase(sCSCSIid))
           {
             //store the attributes in ac-csi bean to retain the selected ones.
             AC_CSI_Bean accsiBean = new AC_CSI_Bean();
             accsiBean.setCSCSI_IDSEQ(sCSCSIid);
             accsiBean.setCSI_BEAN(csiBean);
             vCSNames.addElement(csiBean.getCSI_CS_LONG_NAME());
             vCSINames.addElement(csiBean.getCSI_NAME());
             String ACCSI = "";
             if (vACCSIs != null)
             {
               ACCSI = (String)vACCSIs.elementAt(j);
               if (ACCSI == null) ACCSI = "";                
             }
             accsiBean.setAC_CSI_IDSEQ(ACCSI);              //get its ac-csi id 
             //ac id
             String sAC = "";
             if (vACs != null)
             {
               sAC = (String)vACs.elementAt(j);      //add ac id
               if (sAC == null) sAC = "";
             }
             accsiBean.setAC_IDSEQ(sAC);
             //ac name
             if (vAC_Name != null)
             {
               sAC = (String)vAC_Name.elementAt(j);      //add ac name
               if (sAC == null) sAC = "";
             }
             accsiBean.setAC_LONG_NAME(sAC);
             //add bean to the vector
             vACCSIList.addElement(accsiBean);
           }
         }
       }
     }
     return vACCSIList;   
   }
  
   /**
    * 
    * @param req
    * @param deBean
    * @return 
    */
   public DE_Bean setDECSCSIfromPage(HttpServletRequest req, DE_Bean deBean)  
   {
     try
     {
       HttpSession session = req.getSession();
           //cs-csi relationship
       String[] sNAMEs = req.getParameterValues("selCSNAMEHidden");
       deBean.setDE_CS_NAME(this.getSelectionFromPage(sNAMEs));
   
       //get associated ac-csi
       Vector vCSCSIs = new Vector(), vACCSIs = new Vector(), vACs = new Vector(), vACNames = new Vector();
       String[] sIDs, sACCSIs, sACs;
       String sACCSI, sAC, sID;
       //get selected cs-csi
       sIDs = req.getParameterValues("selCSCSIHidden");
       sACCSIs = req.getParameterValues("selACCSIHidden");
       sACs = req.getParameterValues("selACHidden");
       Vector vNames = (Vector)session.getAttribute("vACName");
       Vector vIDs = (Vector)session.getAttribute("vACId");
         
       if(sIDs != null)
       {
         for (int i=0;i<sIDs.length;i++)
         {
           sID = sIDs[i];
           sACCSI = sACCSIs[i];
           sAC = sACs[i];
           if (sACCSI == null)  sACCSI = "";
           if (sAC == null)  sAC = deBean.getDE_DE_IDSEQ();
           if ((sID != null) && (!sID.equals("")))
           {
             vCSCSIs.addElement(sID);
             vACCSIs.addElement(sACCSI);
             vACs.addElement(sAC);
             //get the ac name            
             String acName = deBean.getDE_LONG_NAME();
             if (sAC != null && vNames != null && vIDs != null)
             {
               if (vIDs.indexOf(sAC) >= 0)              
                 acName = (String)vNames.elementAt(vIDs.indexOf(sAC));
             }
             vACNames.addElement(acName);
           }
         }
       }
       deBean.setDE_CS_CSI_ID(vCSCSIs);
         
       //store accsi bean list list in the session
       Vector vCSList = (Vector)session.getAttribute("CSCSIList");
       Vector vList = getACCSIFromPage(vCSCSIs, vACCSIs, vCSList, vACs, vACNames);
       deBean.setDE_AC_CSI_VECTOR(vList);//req.setAttribute("vACCSIList", vACCSIList);
         
       //get associated ac-csi
       sIDs = req.getParameterValues("selACCSIHidden");
       deBean.setDE_AC_CSI_ID(this.getSelectionFromPage(sIDs));
         
       //get associated cs-id
       sIDs = req.getParameterValues("selectedCS");
       deBean.setDE_CS_ID(this.getSelectionFromPage(sIDs));
     }
     catch (Exception e)
     {
       logger.fatal("ERROR in setacservice_setdecscsivaluefrompage  : " + e.toString());      
     }    
     //return the bean
     return deBean;
   }
   /**
   * To set the values from request to Value Domain Bean, called from NCICurationServlet.
   *
   * @param req The HttpServletRequest object.
   * @param res HttpServletResponse object.
   * @param m_VD Value Domain Bean.
   *
   * @throws IOException  If an input or output exception occurred
   * @throws ServletException  when servlet exception occured
   * @throws Exception  when exception occured
   */
   public void setVDValueFromPage(HttpServletRequest req,
           HttpServletResponse res, VD_Bean m_VD) throws ServletException,IOException, Exception
   {
       HttpSession session = req.getSession();
       String sOriginAction = (String)session.getAttribute("originAction");
       if (sOriginAction == null) sOriginAction.equals("");
 
       //get the selected contexts from the DE bean
       VD_Bean selVD = (VD_Bean)session.getAttribute("m_VD");      
       m_VD.setVD_SELECTED_CONTEXT_ID(selVD.getVD_SELECTED_CONTEXT_ID());   
       String sIdx, sID;
       String sName = "";
       String sObjRow = "";
       String sObjRowCK = "";
       String sPropRowCK = "";
       String sObjQRowCK = "";
       String sPropQRowCK = "";
       String sRepRowCK = "";
       String sRepQRowCK = "";
       String sPropRow = "";
       String sObjQRow = "";
       String sPropQRow = "";
       String sRepRow = "";
       String sRepQRow = "";
 
       Integer intObjRow;
       int intObjRow2;
       Integer intPropRow;
       int intPropRow2;
       Integer intRepRow;
       int intRepRow2;
 
       sID = (String)req.getParameter("vdIDSEQ");
       if(sID != null)
         m_VD.setVD_VD_IDSEQ(sID);
       sID = (String)req.getParameter("CDE_IDTxt");
       if(sID != null)
         m_VD.setVD_VD_ID(sID);
 
       if(sOriginAction.equals("BlockEditVD"))
         sID = "";
       else
         sID = (String)req.getParameter("selContext");
 
       if ((sID != null) || (!sID.equals("")))
       {
         sName = m_util.getNameByID((Vector)session.getAttribute("vContext"), (Vector)session.getAttribute("vContext_ID"), sID);
         m_VD.setVD_CONTE_IDSEQ(sID);
         m_VD.setVD_CONTEXT_NAME(sName);
       }  
      
       String s = (String)req.getParameter("txtRepTerm");
       if(s != null)
          m_VD.setVD_REP_TERM(s);
          
       sName = "";
       if(sOriginAction.equals("BlockEditVD"))
         sName = "";
       else
         sName = (String)req.getParameter("txtLongName");
       if(sName != null)
       {
         sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
         m_VD.setVD_LONG_NAME(sName);
       }
       
        //set PREFERRED_NAME
       if(sOriginAction.equals("BlockEditVD"))
         sName = "";
       else
         sName = (String)req.getParameter("txtPreferredName");
       if(sName != null)
       {
         sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
         m_VD.setVD_PREFERRED_NAME(sName);
       }
 
       //set VD_PREFERRED_DEFINITION
       sName = (String)req.getParameter("CreateDefinition");
       if(sName != null)
       {
         sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
         m_VD.setVD_PREFERRED_DEFINITION(sName);
       }
       
       sID = (String)req.getParameter("selConceptualDomain");
       if(sID != null)
       {
         m_VD.setVD_CD_IDSEQ(sID);
         sName = (String)req.getParameter("selConceptualDomainText");
         if ((sName == null) || (sName.equals("")))
         {
           if ((Vector)session.getAttribute("vCD") != null)
             sName = m_util.getNameByID((Vector)session.getAttribute("vCD"), (Vector)session.getAttribute("vCD_ID"), sID);
         }
         if(sName != null) m_VD.setVD_CD_NAME(sName);
       }
 
       //set VD_VERSION
       if(sOriginAction.equals("BlockEditVD"))
       {
         sName = (String)req.getParameter("VersionCheck");
         if(sName == null)
           sName = "";
         else
         {
           sName = (String)req.getParameter("WholeCheck");
           if(sName == null)
           {
             sName = (String)req.getParameter("PointCheck");
             if(sName != null)
               m_VD.setVD_VERSION("Point");
 
           }
           else
             m_VD.setVD_VERSION("Whole");
         }
       }
       else
       {
         sName = (String)req.getParameter("Version");
         if(sName != null)
         {
           sName = sName.trim();
           String isNum = this.checkValueIsNumeric(sName, "Version");
           //if numeric and no . and less than 2 length add .0 in the end.
           if ((isNum == null || isNum.equals("")) && (sName.indexOf(".") == -1 && sName.length() < 3))
             sName = sName + ".0";          
           m_VD.setVD_VERSION(sName);
         }
       }
 
       //set VD_ASL_NAME
       sName = (String)req.getParameter("selStatus");
       if(sName != null)
         m_VD.setVD_ASL_NAME(sName);
 
        //set VD_DATA_TYPE
       sName = (String)req.getParameter("selDataType");
       if(sName != null)
         m_VD.setVD_DATA_TYPE(sName);
 
       //set VD_SOURCE
       sName = (String)req.getParameter("selSource");
       if(sName != null)
         m_VD.setVD_SOURCE(sName);
 
       //set VD_TYPE_FLAG
       String sVDType = "";
       if(sOriginAction.equals("BlockEditVD"))
         sVDType = "";
       else
         sVDType = (String)req.getParameter("listVDType");
       if(sVDType != null)
       {
         m_VD.setVD_TYPE_FLAG(sVDType);
       }
   
        //set VD_BEGIN_DATE
       sName = (String)req.getParameter("BeginDate");
       if(sName != null)
         m_VD.setVD_BEGIN_DATE(sName);
 
       //set VD_UOML_NAME
       sName = (String)req.getParameter("selUOM");
       if(sName != null)
         m_VD.setVD_UOML_NAME(sName);
 
       //set VD_FORML_NAME
       sName = (String)req.getParameter("selUOMFormat");
       if(sName != null)
         m_VD.setVD_FORML_NAME(sName);
 
       //set VD_MIN_LENGTH_NUM
       sName = (String)req.getParameter("tfMinLength");
       if(sName != null)
         m_VD.setVD_MIN_LENGTH_NUM(sName);
 
       //set VD_MAX_LENGTH_NUM
       sName = (String)req.getParameter("tfMaxLength");
       if(sName != null)
         m_VD.setVD_MAX_LENGTH_NUM(sName);
 
       //set VD_LOW_VALUE_NUM
       sName = (String)req.getParameter("tfLowValue");
       if(sName != null)
         m_VD.setVD_LOW_VALUE_NUM(sName);
 
       //set VD_HIGH_VALUE_NUM
       sName = (String)req.getParameter("tfHighValue");
       if(sName != null)
         m_VD.setVD_HIGH_VALUE_NUM(sName);
 
       //set VD_DECIMAL_PLACE
       sName = (String)req.getParameter("tfDecimal");
       if(sName != null)
         m_VD.setVD_DECIMAL_PLACE(sName);
 
       //set VD_END_DATE
       sName = (String)req.getParameter("EndDate");
       if(sName != null)
         m_VD.setVD_END_DATE(sName);
 
       //set VD_CHANGE_NOTE
       sName = (String)req.getParameter("CreateChangeNote");
       if(sName != null)
       {
         sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
         m_VD.setVD_CHANGE_NOTE(sName);
       }
       
       sName = (String)req.getParameter("ObjDefinition");
       if(sName != null)
       {
         sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
         m_VD.setVD_Obj_Definition(sName);
       }
 
       sName = (String)req.getParameter("PropDefinition");
       if(sName != null)
       {
         sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
         m_VD.setVD_Prop_Definition(sName);
       }
 
       sName = (String)req.getParameter("RepDefinition");
       if(sName != null)
       {
         sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
         m_VD.setVD_Rep_Definition(sName);
       }
 
       //cs-csi relationship
       String[] sNAMEs = req.getParameterValues("selCSNAMEHidden");
       m_VD.setVD_CS_NAME(this.getSelectionFromPage(sNAMEs));
 
       //get associated ac-csi
       Vector vCSCSIs = new Vector(), vACCSIs = new Vector(), vACs = new Vector(), vACNames = new Vector();
       String[] sIDs, sACCSIs, sACs;
       String sACCSI, sAC;
       //get selected cs-csi
       sIDs = req.getParameterValues("selCSCSIHidden");
       sACCSIs = req.getParameterValues("selACCSIHidden");
       sACs = req.getParameterValues("selACHidden");
       Vector vNames = (Vector)session.getAttribute("vACName");
       Vector vIDs = (Vector)session.getAttribute("vACId");
       
       if(sIDs != null)
       {
         for (int i=0;i<sIDs.length;i++)
         {
           sID = sIDs[i];
           sACCSI = sACCSIs[i];
           sAC = sACs[i];
           if (sACCSI == null)  sACCSI = "";
           if (sAC == null)  sAC = m_VD.getVD_VD_IDSEQ();
           if ((sID != null) && (!sID.equals("")))
           {
             vCSCSIs.addElement(sID);
             vACCSIs.addElement(sACCSI);
             vACs.addElement(sAC);
             //get the ac name   
             String acName = m_VD.getVD_LONG_NAME();
             if (sAC != null && vNames != null && vIDs != null)
             {
               if (vIDs.indexOf(sAC) >= 0)              
                 acName = (String)vNames.elementAt(vIDs.indexOf(sAC));
             }
             vACNames.addElement(acName);
           }
         }
       }
       m_VD.setVD_CS_CSI_ID(vCSCSIs);
       
       //store accsi bean list list in the session
       Vector vCSList = (Vector)session.getAttribute("CSCSIList");
       Vector vList = getACCSIFromPage(vCSCSIs, vACCSIs, vCSList, vACs, vACNames);
       m_VD.setVD_AC_CSI_VECTOR(vList);
       
       
       Vector vParentCodes = (Vector)session.getAttribute("vParentCodes");
       Vector vParentNames = (Vector)session.getAttribute("vParentNames");
       Vector vParentDB = (Vector)session.getAttribute("vParentDB");
       Vector vParentMetaSource = (Vector)session.getAttribute("vParentMetaSource");
       Vector vParentList = (Vector)session.getAttribute("vParentList");
       m_VD.setVD_PARENT_CODES(vParentCodes);
       m_VD.setVD_PARENT_NAMES(vParentNames);
       m_VD.setVD_PARENT_DB(vParentDB);
       m_VD.setVD_PARENT_META_SOURCE(vParentMetaSource);
       m_VD.setVD_PARENT_LIST(vParentList);
       
      
       //get associated ac-csi
       sIDs = req.getParameterValues("selACCSIHidden");
       m_VD.setVD_AC_CSI_ID(this.getSelectionFromPage(sIDs));
       
       //get associated cs-id
       sIDs = req.getParameterValues("selectedCS");
       m_VD.setVD_CS_ID(this.getSelectionFromPage(sIDs));
  
   } // end of setVDValueFromPage
 
   /**
    * To set the values from request to Permissible Value Bean, called from NCICurationServlet.
    *
    * @param req The HttpServletRequest object.
    * @param res HttpServletResponse object.
    * @param m_PV PV Bean.
    *
    * @throws IOException  If an input or output exception occurred
    * @throws ServletException  when servlet exception occured
    */
   public void setPVValueFromPage(HttpServletRequest req,
           HttpServletResponse res, PV_Bean m_PV) throws ServletException,IOException
   {
       HttpSession session = req.getSession();
       String sIdx, sID;
       String sName = "";
       //set PV_ID
       PV_Bean oldPV = new PV_Bean();
       oldPV = oldPV.copyBean(m_PV);
       
       sName = (String)req.getParameter("selValidValue");
       if(sName == null) sName = "";
       m_PV.setQUESTION_VALUE_IDSEQ(sName); 
 
       sName = (String)req.getParameter("txValidValue");
       if(sName == null) sName = "";
       m_PV.setQUESTION_VALUE(sName); 
 
       //set PV_VALUE
       sName = (String)req.getParameter("txtPermValue");
       if(sName == null) sName = "";
       sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
       m_PV.setPV_VALUE(sName);
       m_PV = this.getModifiedPV(m_PV, oldPV, req);   //handle the changed pv
 
        //set PV_VERSION
       sName = (String)req.getParameter("selShortMeanings");
       if(sName == null) sName = "";
       sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
       m_PV.setPV_SHORT_MEANING(sName);
 
       //set PV_ASL_NAME
       sName = (String)req.getParameter("CreateDescription");
       if(sName == null) sName = "";
       sName = m_util.removeNewLineChar(sName);   //replace newline with empty string
       m_PV.setPV_MEANING_DESCRIPTION(sName);
 
       //set PV_Origin
       sName = (String)req.getParameter("selPVSource");
       if(sName == null) sName = "";
       m_PV.setPV_VALUE_ORIGIN(sName);
 
        //set PV_BEGIN_DATE
       sName = (String)req.getParameter("BeginDate");
       if(sName == null) sName = "";
       m_PV.setPV_BEGIN_DATE(sName);
 
       //set PV_END_DATE
       sName = (String)req.getParameter("EndDate");
       if(sName == null) sName = "";
       m_PV.setPV_END_DATE(sName);
   } // end of setPVValueFromPage
 
   /**
    * need to allow editing of the existing pv. mark the pv as new and 
    * update vdpvs list with the old one marking as deleted to remove its relationship with the vd. 
    * @param pv current pv bean
    * @param req request variable.
    * @return PV_Bean modified current pv bean
    */
   private PV_Bean getModifiedPV(PV_Bean pv, PV_Bean oldPV, HttpServletRequest req)
   {
       HttpSession session = req.getSession();
       if (oldPV == null) oldPV = new PV_Bean();
       String sOldName = oldPV.getPV_VALUE();
       if (sOldName == null) sOldName = "";
       String sOldID = oldPV.getPV_PV_IDSEQ();
       if (sOldID == null) sOldID = "";
       String sName = pv.getPV_VALUE();
       if (sName == null) sName = "";
       //check if name was changed
       if (!sName.equals(sOldName) && !sOldID.equals(""))
       {
         //make current pv as new
       // sName = m_util.parsedStringJSPDoubleQuote(sName);
         pv.setPV_PV_IDSEQ("EVS_" + sName);
         pv.setVP_SUBMIT_ACTION("INS");  
         pv.setPV_VDPVS_IDSEQ("");
         //mark the old pv as deleted and add it the vector in the end.
         oldPV.setVP_SUBMIT_ACTION("DEL");
        oldPV.setPV_CHECKED(false);
         Vector vVDPVList = (Vector)session.getAttribute("VDPVList");
         if (vVDPVList == null) vVDPVList = new Vector();
         vVDPVList.addElement(oldPV);
         session.setAttribute("VDPVList", vVDPVList);        
       }
       return pv;
   }
   /**
    * To set the values from request to Value Meanings Bean and Permissible Values Bean, called from NCICurationServlet.
    *
    * @param req The HttpServletRequest object.
    * @param res HttpServletResponse object.
    * @param m_VM VM Bean.
    * @param m_PV PV Bean.
    *
    * @throws IOException  If an input or output exception occurred
    * @throws ServletException  when servlet exception occured
    */
   public void setVMValueFromPage(HttpServletRequest req, HttpServletResponse res,
           VM_Bean m_VM) throws ServletException,IOException
   {
       HttpSession session = req.getSession();
       String sIdx, sID;
       String sName = "";
 
       String sEVSSearch = (String)req.getParameter("hiddenEVSSearch");
       session.setAttribute("EVSSearched", sEVSSearch);
       //make the vm bean empty if not evs search
       if (sEVSSearch == null || sEVSSearch.equals(""))
         m_VM.setVM_CONCEPT(new EVS_Bean());
         
       sName = (String)req.getParameter("selShortMeanings");
       if(sName != null)
       {
         sName = m_util.removeNewLineChar(sName);
         m_VM.setVM_SHORT_MEANING(sName);
       }
 
       sName = (String)req.getParameter("CreateDescription");
       if(sName != null)
       {
         sName = m_util.removeNewLineChar(sName);
         m_VM.setVM_DESCRIPTION(sName);
       }
 
       // set VM Conceptual Domain
       sID = (String)req.getParameter("selConceptualDomain");
       if(sID != null)
       {
         m_VM.setVM_CD_IDSEQ(sID);
         sName = (String)req.getParameter("selConceptualDomainText");
         if ((sName == null) || (sName.equals("")))
         {
           if ((Vector)session.getAttribute("vCD") != null)
             sName = m_util.getNameByID((Vector)session.getAttribute("vCD"), (Vector)session.getAttribute("vCD_ID"), sID);
         }
         if(sName != null) m_VM.setVM_CD_NAME(sName);
       }
 
        //set VM_BEGIN_DATE
       sName = (String)req.getParameter("BeginDate");
       if(sName != null)
         m_VM.setVM_BEGIN_DATE(sName);
 
        //set VM_COMMENTS
       sName = (String)req.getParameter("taComments");
       if(sName != null)
       {
         sName = m_util.removeNewLineChar(sName);
         m_VM.setVM_COMMENTS(sName);
       }
 
       //set VM_END_DATE
       sName = (String)req.getParameter("EndDate");
       if(sName != null)
         m_VM.setVM_END_DATE(sName);
        
   } // end of setVMValueFromPage
 
   /**
   * Method to check if name has changed for released element.
   *
   * @param String oldName
   * @param String newName
   * @param String oldStatus
   *
   * @return String strValid message if not valid. otherwise empty string.
   */
   public String checkNameDiffForReleased(String oldName, String newName, String oldStatus)
   {
       if (oldStatus == null || oldStatus.equals(""))
          return "";
 
       if (oldStatus.equals("RELEASED"))
       {
          oldName = oldName.trim();
          newName = newName.trim();
          if (oldName.equals(newName))
             return "";
          else
             return "Cannot update the preferred name if the workflow status is RELEASED. \n";
       }
       else
         return "";
   }  //end of checkNameDiffForReleased
 
  /**
   * To get the existing Permissible value idseq from the database for the selected value meaning.
   * Called from setVDvalue from page.
   * Gets all the attribute values from the bean, sets in parameters, and registers output parameter.
   * Calls oracle stored procedure
   *   "{call SBREXT_Get_Row.GET_PV(?,?,?,?,?,?,?,?,?,?,?,?,?,?)}" to submit
   *
   * @param req The HttpServletRequest object.
   * @param res HttpServletResponse object.
   * @param pv PV Bean.
   *
   * @return String return code from the stored procedure call. null if no error occurred.
   */
   public String getPV(HttpServletRequest req, HttpServletResponse res, PV_Bean pv)  
   {
     Connection sbr_db_conn = null;
     ResultSet rs = null;
     CallableStatement CStmt = null;
     String sReturnCode = "";  //out
 
     try
     {
       String sValue = pv.getPV_VALUE();
       String sShortMeaning = pv.getPV_SHORT_MEANING();
 
        //Create a Callable Statement object.
        sbr_db_conn = m_servlet.connectDB(req, res);
        if (sbr_db_conn == null)
           m_servlet.ErrorLogin(req, res);
        else
        {
           CStmt = sbr_db_conn.prepareCall("{call SBREXT_get_Row.GET_PV(?,?,?,?,?,?,?,?,?,?,?,?,?)}");
           // register the Out parameters
           CStmt.registerOutParameter(1,java.sql.Types.VARCHAR);       //return code
           CStmt.registerOutParameter(2,java.sql.Types.VARCHAR);       //pv id
           CStmt.registerOutParameter(3,java.sql.Types.VARCHAR);       //value
           CStmt.registerOutParameter(4,java.sql.Types.VARCHAR);       //short meaning
           CStmt.registerOutParameter(5,java.sql.Types.VARCHAR);       //meaning description
           CStmt.registerOutParameter(6,java.sql.Types.VARCHAR);       // high value num
           CStmt.registerOutParameter(7,java.sql.Types.VARCHAR);       //low value num
           CStmt.registerOutParameter(8,java.sql.Types.VARCHAR);       //  begin date
           CStmt.registerOutParameter(9,java.sql.Types.VARCHAR);       //end date
           CStmt.registerOutParameter(10,java.sql.Types.VARCHAR);       //created by
           CStmt.registerOutParameter(11,java.sql.Types.VARCHAR);       //date created
           CStmt.registerOutParameter(12,java.sql.Types.VARCHAR);       //modified by
           CStmt.registerOutParameter(13,java.sql.Types.VARCHAR);       //date modified
 
           // Set the In parameters (which are inherited from the PreparedStatement class)
           CStmt.setString(3,sValue);
           CStmt.setString(4,sShortMeaning);
 
            // Now we are ready to call the stored procedure
           boolean bExcuteOk = CStmt.execute();
           sReturnCode = CStmt.getString(1);
           pv.setPV_PV_IDSEQ(CStmt.getString(2));
        }
     }
     catch(Exception e)
     {
       //System.err.println("ERROR in setACService-getPV: " + e);
       logger.fatal("ERROR in setACService-getPV for other : " + e.toString());
     }
     try
     {
       if(rs!=null) rs.close();
       if(CStmt!=null) CStmt.close();
       if(sbr_db_conn != null) sbr_db_conn.close();
     }
     catch(Exception ee)
     {
       //System.err.println("Problem closing: " + ee);
       logger.fatal("ERROR in setACService-getPV for close : " + ee.toString());
     }
     return sReturnCode;
   }
 
  /**
   * To check a Value Meaing exist in the database for this meaning.
   * Called from setVDfromPage method.
   * Gets all the attribute values from the bean, sets in parameters, and registers output parameter.
   * Calls oracle stored procedure
   *   "{call SBREXT_get_Row.GET_VM(?,?,?,?,?,?,?,?,?,?,?)}" to submit
   *
   * @param req The HttpServletRequest object.
   * @param res HttpServletResponse object.
   * @param vm VM Bean.
   *
   * @return String return code from the stored procedure call. null if no error occurred.
   */
   public String getVM(HttpServletRequest req, HttpServletResponse res, VM_Bean vm)
   {
     Connection sbr_db_conn = null;
     ResultSet rs = null;
     CallableStatement CStmt = null;
     String sReturnCode = "";  //out
     String ret2 = "";
     try
     {
         //Create a Callable Statement object.
         sbr_db_conn = m_servlet.connectDB(req, res);
         if (sbr_db_conn == null)
           m_servlet.ErrorLogin(req, res);
         else
         {
           CStmt = sbr_db_conn.prepareCall("{call SBREXT_get_Row.GET_VM(?,?,?,?,?,?,?,?,?,?)}");
           // register the Out parameters
           CStmt.registerOutParameter(1,java.sql.Types.VARCHAR);       //return code
           CStmt.registerOutParameter(2,java.sql.Types.VARCHAR);       //short meaning
           CStmt.registerOutParameter(3,java.sql.Types.VARCHAR);       //meaning description
           CStmt.registerOutParameter(4,java.sql.Types.VARCHAR);       //comments
           CStmt.registerOutParameter(5,java.sql.Types.VARCHAR);       //begin date
           CStmt.registerOutParameter(6,java.sql.Types.VARCHAR);       //end date
           CStmt.registerOutParameter(7,java.sql.Types.VARCHAR);       //created by
           CStmt.registerOutParameter(8,java.sql.Types.VARCHAR);       //date created
           CStmt.registerOutParameter(9,java.sql.Types.VARCHAR);       //modified by
           CStmt.registerOutParameter(10,java.sql.Types.VARCHAR);       //date modified
          // CStmt.registerOutParameter(11,java.sql.Types.VARCHAR);       //condr_idseq
           // Set the In parameters (which are inherited from the PreparedStatement class)
           CStmt.setString(2, vm.getVM_SHORT_MEANING());
 
            // Now we are ready to call the stored procedure
           boolean bExcuteOk = CStmt.execute();
           sReturnCode = CStmt.getString(1);
           //update vm bean if found
           if (sReturnCode == null || sReturnCode.equals(""))
           {
             vm.setVM_SHORT_MEANING(CStmt.getString(2));
             vm.setVM_DESCRIPTION(CStmt.getString(3));
             vm.setVM_COMMENTS(CStmt.getString(4));
             String sCondr = "";    //CStmt.getString(11);
            // String sCondr = "EAEA6FFC-6370-24B5-E034-0003BA0B1A09";  //for test only
             if (sCondr != null && !sCondr.equals(""))
             {
               //get the concept attributes from the condr idseq
               GetACService getAC = new GetACService(req, res, m_servlet);
               Vector vCon = getAC.getAC_Concepts(sCondr, null, true);
               //get the evs bean from teh vector and store it in vm concept
               if (vCon != null && vCon.size() > 0)
                 vm.setVM_CONCEPT((EVS_Bean)vCon.elementAt(0));
             }                        
           }
       }
     }
     catch(Exception e)
     {
       logger.fatal("ERROR in setACService-getVM for other : " + e.toString());
     }
     try
     {
       if(rs!=null) rs.close();
       if(CStmt!=null) CStmt.close();
       if(sbr_db_conn != null) sbr_db_conn.close();
     }
     catch(Exception ee)
     {
       logger.fatal("ERROR in setACService-getVM for close : " + ee.toString());
     }
     return sReturnCode;
   }
 
   /** 
    * Check if the permissible values associated with the form for the selected VD
    * 
    * @param req The HttpServletRequest object.
    * @param res HttpServletResponse object.
    * @param vdIDseq string unique id for value domain.
    * @param vpIDseq string unique id for vd pvs table.
    * 
    * @return boolean true if pv is associated with the form false otherwise
    * 
    * @throws exception
    */
   public boolean checkPVQCExists(HttpServletRequest req, HttpServletResponse res, 
         String vdIDseq, String vpIDseq) throws Exception
   {
     Connection sbr_db_conn = null;
     ResultSet rs = null;
     //CallableStatement CStmt = null;
     PreparedStatement pstmt = null;
     boolean isValid = false;
     try
     {
       //Create a Callable Statement object.
       sbr_db_conn = m_servlet.connectDB(req, res);
       if (sbr_db_conn == null)
         m_servlet.ErrorLogin(req, res);
       else
       {
         pstmt = sbr_db_conn.prepareStatement("select SBREXT_COMMON_ROUTINES.VD_PVS_QC_EXISTS(?,?) from DUAL");
         // register the Out parameters
         pstmt.setString(1, vpIDseq);
         pstmt.setString(2, vdIDseq);
          // Now we are ready to call the function
         rs = pstmt.executeQuery();
         while (rs.next())
         {
           if (rs.getString(1).equalsIgnoreCase("TRUE"))
             isValid = true;
         }
       }
     }
     catch(Exception e)
     {
       //System.err.println("ERROR in setACService-checkPVQCExists for other : " + e);
       logger.fatal("ERROR in setACService-checkPVQCExists for other : " + e.toString());
     }
     try
     {
       if(rs!=null) rs.close();
       if(pstmt!=null) pstmt.close();
       if(sbr_db_conn != null) sbr_db_conn.close();
     }
     catch(Exception ee)
     {
       //System.err.println("ERROR in setACService-checkPVQCExists for close : " + ee);
       logger.fatal("ERROR in setACService-checkPVQCExists for close : " + ee.toString());
     }
     return isValid;
    }   //end checkPVQCExists
 
   /**
    * checks if removed pvs are associated with the form 
    * send back the validation message for pv vds data.
    * 
    * @param req servlet request
    * @param res servlet response
    * @param m_VD VD_Bean of the selected vd.
    * @param vValidate vector of validation data
    * @param sOriginAction string page action
    * 
    * @throws exception
    */
    private Vector validateVDPVS(HttpServletRequest req, HttpServletResponse res, 
           VD_Bean m_VD, Vector vValidate, String sOriginAction) throws Exception
    {
         HttpSession session = req.getSession(); 
         String strInvalid = "";
         //get current value domains vd-pv attributes
         Vector vVDPVS = (Vector)session.getAttribute("VDPVList");  
         
         String vdID = m_VD.getVD_VD_IDSEQ();
         //remove vdidseq if new vd
         if (vdID == null || sOriginAction.equalsIgnoreCase("newVD")) vdID = "";
         //make long string of values/meanings
         String sPVVal = "";
         String sPVMean = "";        
         if (vVDPVS != null && !vVDPVS.isEmpty())
         {
           boolean isChanged = false;
           for (int i=0; i<vVDPVS.size(); i++)
           {
             PV_Bean thisPV = (PV_Bean)vVDPVS.elementAt(i);
             //check its relationship with the form if removed 
             String vpID = thisPV.getPV_VDPVS_IDSEQ();
             if (vpID == null) vpID = "";
             if (!vdID.equals("") && !vpID.equals("") && thisPV.getVP_SUBMIT_ACTION().equals("DEL"))
             {
               boolean isExists = this.checkPVQCExists(req, res, vdID, vpID);
               if (isExists)
               {
                 isChanged = true;
                 if (strInvalid.equals(""))
                   strInvalid = "Unable to remove the following Permissible Values because they are used in a CRF : \n";
                 strInvalid += thisPV.getPV_VALUE() + ",\n";
                 thisPV.setVP_SUBMIT_ACTION("NONE");
                 vVDPVS.setElementAt(thisPV, i);
               }
             }
             //add to validation only if not deleted
             if (!thisPV.getVP_SUBMIT_ACTION().equals("DEL"))
             {
               if (sPVVal != null && !sPVVal.equals(""))
               {
                 sPVVal += ",\n ";
                 sPVMean += ",\n ";
               }
               sPVVal += thisPV.getPV_VALUE();
               sPVMean += thisPV.getPV_SHORT_MEANING();              
             }
           }
           if (isChanged)
             session.setAttribute("VDPVList", vVDPVS);           
         }
 
         String s = m_VD.getVD_TYPE_FLAG();
         if (s == null) s = "";
         if (s.equals("E"))
         {
             setValPageVector(vValidate, "Values", sPVVal, true, -1, strInvalid, sOriginAction);
             setValPageVector(vValidate, "Value Meanings", sPVMean, true, -1, "", sOriginAction);
         }
         return vValidate;
    }    //end validateVDPVS
 
   /**
    * gets pv ids from the page and checks if there are any new ones or any removed ones 
    * updates the pv list vector both for id and bean and stores them in the session.
    * 
    * @param req servlet request
    * @param res servlet response
    * 
    * @throws exception
    */
   public void addRemovePageVDPVs(HttpServletRequest req, HttpServletResponse res) throws Exception
   {
     HttpSession session = req.getSession(); 
     //get the current pvs on the page
     String pvAction = (String)session.getAttribute("PVAction");
     //do this right now since there no pvs on the page that are not in the bean vector already 
     if (pvAction == null || pvAction.equals("") || pvAction.equalsIgnoreCase("createPV")
           || pvAction.equalsIgnoreCase("addSelectedVM"))
       return;
     
     VD_Bean vd = (VD_Bean)session.getAttribute("m_VD");
     if (vd == null) vd = new VD_Bean();
     String strInvalid = "";
     Vector vPagePVID = new Vector();
     Vector vVDPVList = (Vector)session.getAttribute("VDPVList");
     Vector vCheckRow = new Vector();
     if (vVDPVList == null) vVDPVList = new Vector();
     String sPV_IDs[] = req.getParameterValues("hiddenPVID");  //get pv id
     if (sPV_IDs != null && sPV_IDs.length > 0)
     {
       //do the following only if edit or remove  
       int iPVChecked = 0;
       for (int j=0; j<sPV_IDs.length; j++)  //loop through pvid lists
       {
         String rSel = (String)req.getParameter("ck"+j);
         //check if this id is selected
         if (rSel != null)
         {                
           String pvID = sPV_IDs[j];    //(String)vPVIDList.elementAt(j);
          if (pvID == null) pvID = "";
           //reset the bean with selected row for the selected component
           for (int k=0; k<vVDPVList.size(); k++)
           {
             PV_Bean pvBean = (PV_Bean)vVDPVList.elementAt(k);
            //since double quote from remove the double quote to match id from the jsp.  
            String beanPVID = pvBean.getPV_PV_IDSEQ();
            if (beanPVID == null) beanPVID = "";
            beanPVID = beanPVID.replace('"', ' ');    
    //  System.out.println(pvID + " sel pv " + pvBean.getPV_PV_IDSEQ() + " changed " + beanPVID);
             //match hte ids to get the correct bean
            if (beanPVID.equalsIgnoreCase(pvID))
             {
               //store match pv id in the vector
               if (pvAction.equals("removePV"))
               {
                 //check if it is associated with the valid value in the form
                 String vdID = vd.getVD_VD_IDSEQ();
                 String vpID = pvBean.getPV_VDPVS_IDSEQ();
                 boolean isExists = false;
                 if (vdID != null && !vdID.equals("") && vpID != null && !vpID.equals(""))
                 {
                   isExists = this.checkPVQCExists(req, res, vdID, vpID);
                   if (isExists)
                   {
                     strInvalid = strInvalid + "Unable to remove the Permissible Value " +
                         pvBean.getPV_VALUE() + " because it is used in a CRF.\\n";
                     pvBean.setVP_SUBMIT_ACTION("NONE");
                   }
                 }
                 if (!isExists)
                 {
                   pvBean.setVP_SUBMIT_ACTION("DEL");  //mark as del if removed from the page
                   //put crf value back to non matched when deleted pv
                   String sVV = pvBean.getQUESTION_VALUE();
                   if (sVV != null && !sVV.equals(""))
                   {
                     Vector vVV = (Vector)session.getAttribute("NonMatchVV");
                     if (vVV == null) vVV = new Vector();
                     if (!vVV.contains(sVV))
                       vVV.addElement(sVV);
                     session.setAttribute("NonMatchVV", vVV);
                   }
                 }
               }
               else
               {  //set edit/create attributes
                 pvBean.setVP_SUBMIT_ACTION("UPD");
                 pvBean.setPV_CHECKED(true);
                 vCheckRow.addElement(pvID);
                 iPVChecked += 1;
                 if (iPVChecked == 1)
                   session.setAttribute("m_PV", pvBean);                
               } 
               vVDPVList.setElementAt(pvBean, k);  //reset the attribute with the bean attributes
               break;
             }
           }
         }  //end vdpv loop      
       } //end pvid loop
       //store it in session
     //  session.setAttribute("PVIDList", vPVIDList);
       session.setAttribute("VDPVList", vVDPVList);
       if (iPVChecked >1)
         session.setAttribute("m_PV", new PV_Bean());
     }
     if (!strInvalid.equals(""))
     {
       InsACService insAC = new InsACService(req, res, m_servlet);
       insAC.storeStatusMsg(strInvalid);
       //session.setAttribute("statusMessage", strInvalid);
     }
   }  //end addremovepagevdpvs
   
 }   //close the class
