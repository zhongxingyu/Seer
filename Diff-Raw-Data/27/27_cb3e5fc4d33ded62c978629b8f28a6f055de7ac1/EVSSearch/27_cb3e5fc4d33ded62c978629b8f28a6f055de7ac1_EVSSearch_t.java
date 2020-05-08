 //Copyright (c) 2000 ScenPro, Inc.
 
 //$Header: /cvsshare/content/cvsroot/cdecurate/src/gov/nih/nci/cadsr/cdecurate/tool/EVSSearch.java,v 1.67 2009-04-28 15:22:31 veerlah Exp $
 //$Name: not supported by cvs2svn $
 
 package gov.nih.nci.cadsr.cdecurate.tool;
 
 import gov.nih.nci.cadsr.cdecurate.util.DataManager;
 import gov.nih.nci.evs.security.SecurityToken;
 import gov.nih.nci.system.client.ApplicationServiceProvider;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.Stack;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.LexGrid.LexBIG.DataModel.Collections.AssociatedConceptList;
 import org.LexGrid.LexBIG.DataModel.Collections.AssociationList;
 import org.LexGrid.LexBIG.DataModel.Collections.ConceptReferenceList;
 import org.LexGrid.LexBIG.DataModel.Collections.LocalNameList;
 import org.LexGrid.LexBIG.DataModel.Collections.NameAndValueList;
 import org.LexGrid.LexBIG.DataModel.Collections.ResolvedConceptReferenceList;
 import org.LexGrid.LexBIG.Exceptions.LBException;
 import org.LexGrid.LexBIG.Exceptions.LBResourceUnavailableException;
 import org.LexGrid.LexBIG.Extensions.Generic.LexBIGServiceConvenienceMethods;
 import org.LexGrid.LexBIG.DataModel.Core.AssociatedConcept;
 import org.LexGrid.LexBIG.DataModel.Core.Association;
 import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeVersionOrTag;
 import org.LexGrid.LexBIG.DataModel.Core.ConceptReference;
 import org.LexGrid.LexBIG.DataModel.Core.NameAndValue;
 import org.LexGrid.LexBIG.DataModel.Core.ResolvedConceptReference;
 import org.LexGrid.LexBIG.LexBIGService.CodedNodeGraph;
 import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet;
 import org.LexGrid.LexBIG.LexBIGService.LexBIGService;
 import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet.ActiveOption;
 import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet.PropertyType;
 import org.LexGrid.LexBIG.Utility.Constructors;
 import org.LexGrid.LexBIG.Utility.ConvenienceMethods;
 import org.LexGrid.LexBIG.Utility.Iterators.ResolvedConceptReferencesIterator;
 import org.LexGrid.LexBIG.caCore.interfaces.LexEVSApplicationService;
 import org.LexGrid.codingSchemes.CodingScheme;
 import org.LexGrid.commonTypes.Property;
 import org.LexGrid.commonTypes.Source;
 import org.LexGrid.concepts.Definition;
 import org.LexGrid.concepts.Presentation;
 import org.LexGrid.naming.Mappings;
 import org.LexGrid.naming.SupportedHierarchy;
 import org.LexGrid.naming.SupportedSource;
 import org.apache.log4j.Logger;
 
 /**
  * EVSSearch class is for search action of the tool for all components.
  * <P>
  * @author Tom Phillips
  * @version 3.0.1
  *
  */
 
 public class EVSSearch implements Serializable {
 	private static final long serialVersionUID = 1606675727846409378L;
 
 	//constant variables
 	/** constant value for empty data to get the vocab attribute from the user bean according to what is needed  */
 	public static final int VOCAB_NULL = 0;
 	/** constant value to return display vocab name*/
 	public static final int VOCAB_DISPLAY = 1;
 	/** constant value to return database vocab orgin*/
 	public static final int VOCAB_DBORIGIN = 2;
 	/** constant value to return database vocab name*/
 	public static final int VOCAB_NAME = 3;
 
 	public static final String NCIT_SCHEME_NAME = "NCI Thesaurus";
 	
 	public static LexBIGServiceConvenienceMethods conMthds = null;
 	public static ConceptReference retiredRootCon = null;
 	/** constant value to recorgnize meta data */
 	public static final String META_VALUE = "MetaValue";
 	//class variables
 	CurationServlet m_servlet = null;
 	HttpServletRequest m_classReq = null;
 	HttpServletResponse m_classRes = null;
 	UtilService m_util = new UtilService();
 	LexBIGService evsService = null;
 	EVS_UserBean m_eUser = null;
 	EVS_Bean m_eBean = null;
 	Logger logger = Logger.getLogger(EVSSearch.class.getName());
 
 	/**
 	 * Constructs a new instance.
 	 * @param req The HttpServletRequest object.
 	 * @param res HttpServletResponse object.
 	 * @param CurationServlet NCICuration servlet object.
 	 */
 	public EVSSearch(HttpServletRequest req, HttpServletResponse res,
 			CurationServlet CurationServlet) {
 		m_classReq = req;
 		m_classRes = res;
 		m_servlet = CurationServlet;
 		m_eUser = (EVS_UserBean) m_servlet.sessionData.EvsUsrBean;
 		initialize();
 	}
 
 	/**
 	 * Constructor
 	 * 
 	 * @param user_ The EVS User Bean
 	 */
 	public EVSSearch(EVS_UserBean user_) {
 		m_eUser = user_;
 		initialize();
 	}
 
 	/**
 	 * Initializes the EVS Service.
 	 */
 	private void initialize() {
 		try {
 			m_eBean = new EVS_Bean();
 			if (m_eUser == null)
 				m_eUser = new EVS_UserBean(); //to be safe use the default props
 			if (m_eUser.getEVSConURL() != null
 					&& !m_eUser.getEVSConURL().equals(""))
 				evsService = (LexBIGService) ApplicationServiceProvider.getApplicationServiceFromUrl(m_eUser.getEVSConURL(), "EvsServiceInfo");	
 				
 		} catch (Exception e) {
 			e.printStackTrace();
 			logger.error("EVS Service not obtained from the URL");
 		}
 
 	}
 
 	/**
 	 * Performs the EVS code search
 	 * @param prefName string to search for
 	 * @param dtsVocab string selected vocabulary name
 	 * @return string of evs code
 	 */
 	public String do_getEVSCode(String prefName, String dtsVocab) {
 		if (prefName == null || prefName.equals(""))
 			return "";
 		//check if valid dts vocab
 		if (dtsVocab == null)
 			dtsVocab = "";
 		dtsVocab = m_eBean.getVocabAttr(m_eUser, dtsVocab,
 				EVSSearch.VOCAB_NULL, EVSSearch.VOCAB_NAME); // "", "vocabName");    
 		String CCode = "";
 
 		//TODO what does MetaValue search?
 		//TODO why does this return possible many results when it requires only one?
 
 		if (dtsVocab.equals(EVSSearch.META_VALUE)) // "MetaValue"))
 		{
 
 			ResolvedConceptReferenceList codes2 = null;
 			int codesSize = 0;
 
 			try {
 				this.registerSecurityToken((LexEVSApplicationService)evsService, dtsVocab, m_eUser);
 				CodedNodeSet metaNodes = evsService.getNodeSet("NCI MetaThesaurus", null, null); 
 
 				metaNodes = metaNodes.restrictToMatchingDesignations(prefName, //the text to match 
 						CodedNodeSet.SearchDesignationOption.PREFERRED_ONLY,  //whether to search all designation, only Preferred or only Non-Preferred
 						"exactMatch", //the match algorithm to use
 						null); //the language to match (null matches all)
 
 
 				codes2 = metaNodes.resolveToList(
 						null, //Sorts used to sort results (null means sort by match score)
 						null, //PropertyNames to resolve (null resolves all)
 						new CodedNodeSet.PropertyType[] {PropertyType.PRESENTATION},  //PropertyTypess to resolve (null resolves all)
 						10	  //cap the number of results returned (-1 resolves all)
 				);
 				codesSize = codes2.getResolvedConceptReferenceCount();
 
 			} catch (Exception ex) {
 				logger.error("Error do_getEVSCode:resolveToList: " + ex.toString(), ex);
 			}
 
 			if (codes2 != null) {
 				ResolvedConceptReference conceptReference = new ResolvedConceptReference();
 				logger.debug("Got "+codesSize+" results for the do_getEVSCode search using prefName and exactMatch");
 				for (int i = 0; i < codesSize; i++) {
 					conceptReference = (ResolvedConceptReference) codes2.getResolvedConceptReference(i);
 					CCode = (String) conceptReference.getConceptCode();
 
 				}
 			}
 		}else
 		{	
 			CCode = prefName;
 		}
 		return CCode;
 	}
 
 	/**
 	 * does evs code search
 	 * @param CCode string to search for
 	 * @param dtsVocab string selected vocabulary name
 	 * @return string of evs code
 	 */
 	public String do_getConceptName(String CCode, String dtsVocab) {
 		//check if valid dts vocab
 		if (dtsVocab == null)
 			dtsVocab = "";
 		dtsVocab = m_eBean.getVocabAttr(m_eUser, dtsVocab,
 				EVSSearch.VOCAB_NULL, EVSSearch.VOCAB_NAME); // "", "vocabName");
 		String sConceptName = "";
 
 		ResolvedConceptReferenceList concepts2 = null;
 		int codesSize = 0;
 
 		try {
 			CodedNodeSet metaNodes = evsService.getNodeSet(dtsVocab, null, null); 
 
 			this.registerSecurityToken((LexEVSApplicationService)evsService, dtsVocab, m_eUser);
 			ConceptReferenceList crefs = ConvenienceMethods.
         	createConceptReferenceList(new String[]{CCode}, dtsVocab);
 			metaNodes = metaNodes.restrictToCodes(crefs);
 
 
 			concepts2 = metaNodes.resolveToList(
 					null, //Sorts used to sort results (null means sort by match score)
 					null, //PropertyNames to resolve (null resolves all)
 					new CodedNodeSet.PropertyType[] {PropertyType.PRESENTATION},  //PropertyTypess to resolve (null resolves all)
 					10	  //cap the number of results returned (-1 resolves all)
 			);
 			codesSize = concepts2.getResolvedConceptReferenceCount();
 
 		} catch (Exception ex) {
 			logger.error("Error do_getConceptName:resolveToList: " + ex.toString(), ex);
 		}
 
 		if (concepts2 != null) {
 			ResolvedConceptReference conceptReference = new ResolvedConceptReference();
 			for (int i = 0; i < codesSize; i++) {
 				conceptReference = (ResolvedConceptReference) concepts2.getResolvedConceptReference(i);
 				sConceptName = conceptReference.getEntityDescription().getContent();   //To see the name of the code, use 'getEntityDescription' on the resulting ResolvedConceptReference. The 'EntityDescription' will always be equal to the Preferred Presentation in the Default Language. 					
 			}
 		}
 
 		return sConceptName;
 	}
 
 	/**
 	 *
 	 * @param oc_condr_idseq  id of condr.
 	 * @param m_DEC
 	 * @param sMenu
 	 * 
 	 * @throws Exception
 	 * 
 	 */
 	public void fillOCVectors(String oc_condr_idseq, DEC_Bean m_DEC,
 			String sMenu) throws Exception {
 		if (m_DEC != null) {
 			HttpSession session = m_classReq.getSession();
 			//get vd parent attributes
 			GetACService getAC = new GetACService(m_classReq, m_classRes,
 					m_servlet);
 			Vector vOCConcepts = new Vector();
 			if (oc_condr_idseq != null && !oc_condr_idseq.equals(""))
 				vOCConcepts = getAC.getAC_Concepts(oc_condr_idseq, null, true);
 			if (vOCConcepts != null && vOCConcepts.size() > 0) {
 				DataManager.setAttribute(session, "vObjectClass", vOCConcepts);
 				// Primary concept
 				EVS_Bean m_OC = (EVS_Bean) vOCConcepts.elementAt(0);
 				if (m_OC == null)
 					m_OC = new EVS_Bean();
 				m_DEC.setDEC_OCL_NAME_PRIMARY(m_OC.getLONG_NAME());
 				m_DEC.setDEC_OC_CONCEPT_CODE(m_OC.getCONCEPT_IDENTIFIER());
 				m_DEC.setDEC_OC_EVS_CUI_ORIGEN(m_OC.getEVS_DATABASE());
 				for (int i = 1; i < vOCConcepts.size(); i++) {
 					EVS_Bean m_OCQ = (EVS_Bean) vOCConcepts.elementAt(i);
 					if (m_OCQ == null)
 						m_OCQ = new EVS_Bean();
 					//System.out.println(m_OCQ.getCONCEPT_IDENTIFIER()
 					//+ " con Name " + m_OCQ.getLONG_NAME());
 					Vector<String> vOCQualifierNames = m_DEC
 					.getDEC_OC_QUALIFIER_NAMES();
 					if (vOCQualifierNames == null)
 						vOCQualifierNames = new Vector<String>();
 					vOCQualifierNames.addElement(m_OCQ.getLONG_NAME());
 
 					Vector<String> vOCQualifierCodes = m_DEC
 					.getDEC_OC_QUALIFIER_CODES();
 					if (vOCQualifierCodes == null)
 						vOCQualifierCodes = new Vector<String>();
 					vOCQualifierCodes.addElement(m_OCQ.getCONCEPT_IDENTIFIER());
 
 					Vector<String> vOCQualifierDB = m_DEC
 					.getDEC_OC_QUALIFIER_DB();
 					if (vOCQualifierDB == null)
 						vOCQualifierDB = new Vector<String>();
 					vOCQualifierDB.addElement(m_OCQ.getEVS_DATABASE());
 
 					m_DEC.setDEC_OC_QUALIFIER_NAMES(vOCQualifierNames);
 					m_DEC.setDEC_OC_QUALIFIER_CODES(vOCQualifierCodes);
 					m_DEC.setDEC_OC_QUALIFIER_DB(vOCQualifierDB);
 				}
 			}
 		}
 	}
 
 	/**
 	 *
 	 * @param prop_condr_idseq  id of condr.
 	 * @param m_DEC
 	 * @param sMenu
 	 *
 	 * @throws Exception
 	 * 
 	 */
 	@SuppressWarnings("unchecked")
 	public void fillPropVectors(String prop_condr_idseq, DEC_Bean m_DEC,
 			String sMenu) throws Exception {
 		if (m_DEC != null) {
 			HttpSession session = m_classReq.getSession();
 			//get vd parent attributes
 			GetACService getAC = new GetACService(m_classReq, m_classRes,
 					m_servlet);
 			Vector vPCConcepts = new Vector();
 			if (prop_condr_idseq != null && !prop_condr_idseq.equals(""))
 				vPCConcepts = getAC
 				.getAC_Concepts(prop_condr_idseq, null, true);
 
 			if (vPCConcepts != null && vPCConcepts.size() > 0) {
 				DataManager.setAttribute(session, "vProperty", vPCConcepts);
 				EVS_Bean m_PC = (EVS_Bean) vPCConcepts.elementAt(0);
 				if (m_PC == null)
 					m_PC = new EVS_Bean();
 				m_DEC.setDEC_PROPL_NAME_PRIMARY(m_PC.getLONG_NAME());
 				m_DEC.setDEC_PROP_CONCEPT_CODE(m_PC.getCONCEPT_IDENTIFIER());
 				m_DEC.setDEC_PROP_EVS_CUI_ORIGEN(m_PC.getEVS_DATABASE());
 
 				// Secondary 
 				for (int i = 1; i < vPCConcepts.size(); i++) {
 					EVS_Bean m_PCQ = (EVS_Bean) vPCConcepts.elementAt(i);
 					if (m_PCQ == null)
 						m_PCQ = new EVS_Bean();
 					//System.out.println(m_PCQ.getCONCEPT_IDENTIFIER()
 					//+ " con Name " + m_PCQ.getLONG_NAME());
 					Vector vPropQualifierNames = m_DEC
 					.getDEC_PROP_QUALIFIER_NAMES();
 					if (vPropQualifierNames == null)
 						vPropQualifierNames = new Vector();
 					vPropQualifierNames.addElement(m_PCQ.getLONG_NAME());
 					Vector vPropQualifierCodes = m_DEC
 					.getDEC_PROP_QUALIFIER_CODES();
 					if (vPropQualifierCodes == null)
 						vPropQualifierCodes = new Vector();
 					vPropQualifierCodes.addElement(m_PCQ
 							.getCONCEPT_IDENTIFIER());
 					Vector vPropQualifierDB = m_DEC.getDEC_PROP_QUALIFIER_DB();
 					if (vPropQualifierDB == null)
 						vPropQualifierDB = new Vector();
 					vPropQualifierDB.addElement(m_PCQ.getEVS_DATABASE());
 					m_DEC.setDEC_PROP_QUALIFIER_NAMES(vPropQualifierNames);
 					m_DEC.setDEC_PROP_QUALIFIER_CODES(vPropQualifierCodes);
 					m_DEC.setDEC_PROP_QUALIFIER_DB(vPropQualifierDB);
 				}
 			}
 		}
 	}
 
 	/**
 	 *
 	 * @param rep_condr_idseq  id of condr.
 	 * @param m_VD
 	 * @param sMenu
 	 *
 	 * @throws Exception
 	 * 
 	 */
 	@SuppressWarnings("unchecked")
 	public void fillRepVectors(String rep_condr_idseq, VD_Bean m_VD,
 			String sMenu) throws Exception {
 		if (m_VD != null) {
 			HttpSession session = m_classReq.getSession();
 			//get vd parent attributes
 			GetACService getAC = new GetACService(m_classReq, m_classRes,
 					m_servlet);
 			Vector vRepConcepts = new Vector();
 			if (rep_condr_idseq != null && !rep_condr_idseq.equals(""))
 				vRepConcepts = getAC
 				.getAC_Concepts(rep_condr_idseq, null, true);
 
 			if (vRepConcepts != null && vRepConcepts.size() > 0) {
 				DataManager.setAttribute(session, "vRepTerm", vRepConcepts);
 				EVS_Bean m_Rep = (EVS_Bean) vRepConcepts.elementAt(0);
 				if (m_Rep == null)
 					m_Rep = new EVS_Bean();
 				m_VD.setVD_REP_NAME_PRIMARY(m_Rep.getLONG_NAME());
 				m_VD.setVD_REP_CONCEPT_CODE(m_Rep.getCONCEPT_IDENTIFIER());
 				m_VD.setVD_REP_EVS_CUI_ORIGEN(m_Rep.getEVS_DATABASE());
 				//if (!sMenu.equals("NewVDTemplate")
 				//&& !sMenu.equals("NewVDVersion"))
 				//m_VD.setVD_REP_IDSEQ(m_Rep.getIDSEQ());
 
 				// Secondary 
 				for (int i = 1; i < vRepConcepts.size(); i++) {
 					EVS_Bean m_RepQ = (EVS_Bean) vRepConcepts.elementAt(i);
 					if (m_Rep == null)
 						m_Rep = new EVS_Bean();
 					Vector vRepQualifierNames = m_VD
 					.getVD_REP_QUALIFIER_NAMES();
 					if (vRepQualifierNames == null)
 						vRepQualifierNames = new Vector();
 					vRepQualifierNames.addElement(m_RepQ.getLONG_NAME());
 					Vector vRepQualifierCodes = m_VD
 					.getVD_REP_QUALIFIER_CODES();
 					if (vRepQualifierCodes == null)
 						vRepQualifierCodes = new Vector();
 					vRepQualifierCodes.addElement(m_RepQ
 							.getCONCEPT_IDENTIFIER());
 					Vector vRepQualifierDB = m_VD.getVD_REP_QUALIFIER_DB();
 					if (vRepQualifierDB == null)
 						vRepQualifierDB = new Vector();
 					vRepQualifierDB.addElement(m_RepQ.getEVS_DATABASE());
 					m_VD.setVD_REP_QUALIFIER_NAMES(vRepQualifierNames);
 					m_VD.setVD_REP_QUALIFIER_CODES(vRepQualifierCodes);
 					m_VD.setVD_REP_QUALIFIER_DB(vRepQualifierDB);
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param condr_idseq 
 	 * @return sCUIString
 	 */
 	public String getEVSIdentifierString(String condr_idseq) // returns list of Data Element Concepts
 	{
 		String sCondr = condr_idseq;
 		String sCUIString = "";
 		String sCUI = "";
 		try {
 			if (sCondr != null && !sCondr.equals("")) {
 				GetACService getAC = new GetACService(m_classReq, m_classRes,
 						m_servlet);
 				Vector vCon = getAC.getAC_Concepts(sCondr, null, false);
 				if (vCon != null && vCon.size() > 0) {
 					for (int j = 0; j < vCon.size(); j++) {
 						EVS_Bean bean = new EVS_Bean();
 						bean = (EVS_Bean) vCon.elementAt(j);
 						if (bean != null) {
 							sCUI = bean.getCONCEPT_IDENTIFIER();
 							if (sCUI == null)
 								sCUI = "";
 							if (sCUIString.equals(""))
 								sCUIString = sCUI;
 							else
 								sCUIString = sCUIString + ", " + sCUI;
 						}
 					}
 				}
 			}
 		} catch (Exception e) {
 			logger.error(
 					"ERROR - EVSSearch-getEVSIdentifierString for other : "
 					+ e.toString(), e);
 		}
 		return sCUIString;
 	} //end getEVSIdentifierString
 
 	/**
 	 * sorts the dlc objects
 	 * @param vocabRoots list of vocabsroots
 	 * @return sort list of vocab roots
 	 */
 	@SuppressWarnings("unchecked")
 	private ResolvedConceptReferenceList sortDLCObjects(ResolvedConceptReferenceList vocabRoots) {
 		int Swap = 0;
 		if (vocabRoots != null && vocabRoots.getResolvedConceptReferenceCount() > 0) {
 			do {
 				ResolvedConceptReference dlc1 = new ResolvedConceptReference();
 				ResolvedConceptReference dlc2 = new ResolvedConceptReference();
 				ResolvedConceptReference dlcT = new ResolvedConceptReference();
 				Swap = 0;
 				for (int m = 0; m < vocabRoots.getResolvedConceptReferenceCount() - 1; m++) {
 					dlc1 = vocabRoots.getResolvedConceptReference(m);
 					dlc2 = vocabRoots.getResolvedConceptReference(m + 1);
 					String name1 = dlc1.getEntityDescription().getContent();
 					String name2 = dlc2.getEntityDescription().getContent();
 
 					try {
 						if (ComparedValue("", name1, name2) > 0) {
 							dlcT = dlc1;
 							vocabRoots.setResolvedConceptReference(m, dlc2);
 							vocabRoots.setResolvedConceptReference(m + 1, dlc1);
 							Swap = 1;
 						}
 					} catch (Exception ee) {
 						logger.error("problem in EVSSearch-SortDLCObject: "
 								+ ee, ee);
 					}
 				}
 			} while (Swap != 0);
 		}
 		return vocabRoots;
 	}
 
 	/**
 	 * To get compared value to sort.
 	 * called from getDESortedRows, getDECSortedRows, getVDSortedRows, getPVSortedRows methods.
 	 * empty strings are considered as strings.
 	 * according to the fields, converts the string object into integer, double or dates.
 	 *
 	 * @param sField field name to sort.
 	 * @param firstName first name to compare.
 	 * @param SecondName second name to compare.
 	 *
 	 * @return int ComparedValue using compareto method of the object.
 	 *
 	 * @throws Exception
 	 */
 	private int ComparedValue(String sField, String firstName, String SecondName)
 	throws Exception {
 		firstName = firstName.trim();
 		SecondName = SecondName.trim();
 		//this allows to put empty cells at the bottom by specify the return 
 		if (firstName.equals(""))
 			return 1;
 		else if (SecondName.equals(""))
 			return -1;
 		//compare the names
 		return firstName.compareToIgnoreCase(SecondName);
 	}
 
 	/**
 	 * 
 	 * @param dtsVocab
 	 * @return vRoot vector of Root concepts
 	 */
 	public ResolvedConceptReferenceList getRootConcepts(String dtsVocab, CodingScheme cs) //, boolean codeOrNames) 
 	{
 		ResolvedConceptReferenceList vocabRoots = null;
 		//check if valid dts vocab
 		dtsVocab = m_eBean.getVocabAttr(m_eUser, dtsVocab,
 				EVSSearch.VOCAB_NULL, EVSSearch.VOCAB_NAME); // "", "vocabName");
 
 		if (!dtsVocab.equals(EVSSearch.META_VALUE)) // "MetaValue"))  //not for meta
 		{
 
 			try {
 				//TODO Add security token
 				this.registerSecurityToken((LexEVSApplicationService)evsService, dtsVocab,m_eUser);
 				CodedNodeSet ret = null;
 				
 		        if (cs == null) {
 		            throw new LBResourceUnavailableException("Coding scheme not found.");
 		        }
 		        Mappings mappings = cs.getMappings();
 		        SupportedHierarchy[] shs =  mappings.getSupportedHierarchy();
 		        for (int i = 0; i < shs.length; i++) {
 		        	SupportedHierarchy sh = shs[i];
 		        	String[] assns = sh.getAssociationNames();
 		        	String code = sh.getRootCode();
 		            if (code.equals("@") || code.equals("@@")) {
 		                // Need to resolve first level; only want to return 'real'
 		                // concepts ...
 		                ConceptReference cr = new ConceptReference();
 		                cr.setCodingSchemeName(cs.getCodingSchemeName());
 		                cr.setCode(code);
 		                CodedNodeGraph cng = evsService.getNodeGraph(dtsVocab, null, null);
 		                cng = cng.restrictToAssociations(ConvenienceMethods.createNameAndValueList(sh.getAssociationNames()),
 		                        null);
 		                try {
 		                    CodedNodeSet cns = cng.toNodeList(cr, sh.getIsForwardNavigable(), !sh.getIsForwardNavigable(), 0, -1);
 		                    if (ret == null)
 		                    	ret = cns;
 		                    else
 		                    	ret.union(cns);
 		                } catch (Exception e) {
 		                    logger.error("Unable to resolve hierarchy root nodes.", e);
 		                }
 		            } else {
 		                // Root was a real concept; add it explicitly ...
 		                CodedNodeSet toCode = evsService.getCodingSchemeConcepts(dtsVocab, null);
 		                toCode.restrictToCodes(ConvenienceMethods.createConceptReferenceList(new String[] { sh.getRootCode() },
 		                        dtsVocab));
 		                if (ret == null)
 		                	ret = toCode;
 		                else 
 		                	ret.union(toCode);
 		            }
 		        }
 				vocabRoots =  ret.resolveToList(null, null, new CodedNodeSet.PropertyType[] {PropertyType.PRESENTATION}, -1);
 				
 				if (vocabRoots != null && vocabRoots.getResolvedConceptReferenceCount() > 0)
 					vocabRoots = this.sortDLCObjects(vocabRoots);
 
 			} catch (Exception ex) {
 				logger.error("Error EVSSearch-getRootConcepts :resolveToList: " + ex.toString(), ex);
 			}			
 		}
 
 		return vocabRoots;
 	}
 
 	/**
 	 * This method searches EVS vocabularies and returns subconcepts, which are used
 	 * to construct an EVS Tree. 
 	 * @param dtsVocab the EVS Vocabulary
 	 * @param conceptName the root concept.
 	 * @param type
 	 * @param conceptCode the root concept.
 	 * @param defSource
 	 * @return vector of concept objects
 	 *
 	 */
 //	@SuppressWarnings("unchecked")
 //	private Vector getAllSubConceptCodes(String dtsVocab, String conceptName,
 //			String type, String conceptCode, String defSource) {
 //		//logger.debug("getAllSubConceptCodes conceptName: " + conceptName);
 //		Vector vSub = new Vector();
 //		dtsVocab = m_eBean.getVocabAttr(m_eUser, dtsVocab,
 //				EVSSearch.VOCAB_NULL, EVSSearch.VOCAB_NAME); // "", "vocabName");   //check if valid dts vocab 
 //		if (!dtsVocab.equals(EVSSearch.META_VALUE)) // "MetaValue"))
 //		{
 //			try {
 //				String prefName = "";
 //				if (!conceptCode.equals(""))//try it with ConceptCode
 //				{
 //					this.registerSecurityToken((LexEVSApplicationService)evsService, dtsVocab,m_eUser);
 //					
 //					LexBIGServiceConvenienceMethods lbscm = 
 //						(LexBIGServiceConvenienceMethods) evsService.getGenericExtension("LexBIGServiceConvenienceMethods"); 						
 //
 //					lbscm.setLexBIGService(evsService);
 //					
 //					AssociationList al = lbscm.getHierarchyLevelNext(dtsVocab, 
 //							null, //CodingScheme Version or Tag
 //							null, //hierarchy id
 //							conceptCode, // Concept code
 //							true, //Resolve concepts?
 //							null); //Any qualifiers for the association?
 //
 //					int size = al.getAssociationCount();
 //
 //					for (int i = 0; i < size; i++) {
 //						Association assn = al.getAssociation(i);
 //						AssociatedConceptList acl = assn.getAssociatedConcepts();
 //						AssociatedConcept[] aca = acl.getAssociatedConcept();
 //						for (AssociatedConcept ac:aca) {
 //							String temp = ac.getCode();
 //							if (temp != null && !temp.equals(conceptCode))
 //								vSub.addElement(temp);
 //						}
 //					}
 //				}
 //			} catch (Exception ee) {
 //				logger.error(
 //						"ERROR - EVSSearch-getSubConcepts for Thesaurus : "
 //						+ ee.toString(), ee);
 //				return vSub;
 //			}
 //		}
 //		return vSub;
 //	}
 
 	/**
 	 * This method searches EVS vocabularies and returns subconcepts, which are used
 	 * to construct an EVS Tree. 
 	 * @param dtsVocab the EVS Vocabulary
 	 * @param conceptCode the root concept.
 	 * @return vector of concept objects
 	 *
 	 */
 //	@SuppressWarnings("unchecked")
 //	public Vector getAllSubConceptCodes(String dtsVocab, String conceptCode) {
 //		Vector vSub = new Vector();
 //		dtsVocab = m_eBean.getVocabAttr(m_eUser, dtsVocab,
 //				EVSSearch.VOCAB_NULL, EVSSearch.VOCAB_NAME); // "", "vocabName");   //check if valid dts vocab 
 //		if (!dtsVocab.equals(EVSSearch.META_VALUE)) // "MetaValue"))
 //		{
 //			try {
 //				if (!conceptCode.equals(""))//try it with ConceptCode
 //				{
 //					
 //					
 //					LexBIGServiceConvenienceMethods lbscm = 
 //						(LexBIGServiceConvenienceMethods) evsService.getGenericExtension("LexBIGServiceConvenienceMethods"); 						
 //
 //					lbscm.setLexBIGService(evsService);
 //
 //					
 //					AssociationList al = lbscm.getHierarchyLevelNext(dtsVocab, 
 //							null, //CodingScheme Version or Tag
 //							null, //hierarchy id
 //							conceptCode, // Concept code
 //							true, //Resolve concepts?
 //							null); //Any qualifiers for the association?
 //
 //					int size = al.getAssociationCount();
 //
 //					for (int i = 0; i < size; i++) {
 //						Association assn = al.getAssociation(i);
 //						AssociatedConceptList acl = assn.getAssociatedConcepts();
 //						AssociatedConcept[] aca = acl.getAssociatedConcept();
 //						for (AssociatedConcept ac:aca) {
 //							String temp = ac.getCode();
 //							if (temp != null && !temp.equals(conceptCode))
 //								vSub.addElement(temp);
 //						}
 //					}
 //				}
 //			} catch (Exception ee) {
 //				logger.error(
 //						"ERROR - EVSSearch-getSubConcepts for Thesaurus : "
 //						+ ee.toString(), ee);
 //				return vSub;
 //			}
 //		}
 //		return vSub;
 //	}
 
 	/**
 	 * This method searches EVS vocabularies and returns subconcepts, which are used
 	 * to construct an EVS Tree. 
 	 * 
 	 * 
 	 * @param dtsVocab the EVS Vocabulary
 	 * @param conceptName the root concept.
 	 * @param type
 	 * @param conceptCode the root concept.
 	 * @param defSource
 	 * @return vector of concept objects
 	 *
 	 */
 	@SuppressWarnings("unchecked")
 	public HashMap<String, String> getSubConcepts(String dtsVocab, String conceptName,
 			String type, String conceptCode) {
 		//Vector vSub = new Vector();
 		HashMap<String, String> hSub = new HashMap<String,String>();
 		//check if valid dts vocab
 		dtsVocab = m_eBean.getVocabAttr(m_eUser, dtsVocab,
 				EVSSearch.VOCAB_NULL, EVSSearch.VOCAB_NAME); // "", "vocabName");
 		try {
 			this.registerSecurityToken((LexEVSApplicationService) evsService, dtsVocab, m_eUser);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		if (!dtsVocab.equals(EVSSearch.META_VALUE)) // "MetaValue"))  
 		{
 			try {
 				//String prefName = "";
 				if (type.equals("Immediate") || type.equals("")) {
 
 					if (!conceptCode.equals("")) //try it with ConceptCode
 					{
 						try {
 							boolean forwardNavigable = false;
 							
 							HashMap<String, String> hSubs = returnSubConceptNames(conceptCode, dtsVocab);
 
 							hSub.putAll(hSubs);
 //							LexBIGServiceConvenienceMethods lbscm = 
 //								(LexBIGServiceConvenienceMethods) evsService.getGenericExtension("LexBIGServiceConvenienceMethods"); 						
 //							lbscm.setLexBIGService(evsService);
 //							
 //							AssociationList al = lbscm.getHierarchyLevelNext(dtsVocab, 
 //									null, //CodingScheme Version or Tag
 //									null, //hierarchy id
 //									conceptCode, // Concept code
 //									true, //Resolve concepts?
 //									null); //Any qualifiers for the association?
 //
 //							int size = al.getAssociationCount();
 //
 //							for (int i = 0; i < size; i++) {
 //								Association assn = al.getAssociation(i);
 //								AssociatedConceptList acl = assn.getAssociatedConcepts();
 //								AssociatedConcept[] aca = acl.getAssociatedConcept();
 //								for (AssociatedConcept ac:aca) {
 //									String temp = ac.getCode();
 //									if (temp != null && !temp.equals(conceptCode))
 //										hSub.put(temp, ac.getEntityDescription().getContent());
 //								}
 //							}
 
 						} catch (Exception ee) {
 							logger.error(
 									"ERROR - EVSSearch-getSubConcepts for Thesaurus : "
 									+ ee.toString(), ee);
 						}
 					} else if (conceptName != null
 							&& !conceptName.equals("")) {
 						try {  System.out.println("asking for subconcepts using concept Name");
 
 						LexBIGServiceConvenienceMethods lbscm = 
 							(LexBIGServiceConvenienceMethods) evsService.getGenericExtension("LexBIGServiceConvenienceMethods"); 
 
 						lbscm.setLexBIGService(evsService);
 						
 						String retrievedCode = this.do_getEVSCode(conceptName, dtsVocab);
 
 						AssociationList al = lbscm.getHierarchyLevelNext(dtsVocab, 
 								null, //CodingScheme Version or Tag
 								null, //hierarchy id
 								retrievedCode, // Concept code
 								true, //Resolve concepts?
 								null); //Any qualifiers for the association?
 
 						int size = al.getAssociationCount();
 
 						for (int i = 0; i < size; i++) {
 							Association assn = al.getAssociation(i);
 							AssociatedConceptList acl = assn.getAssociatedConcepts();
 							AssociatedConcept[] aca = acl.getAssociatedConcept();
 							for (AssociatedConcept ac:aca) {
 								String temp = ac.getCode();
 								if (temp != null && !temp.equals(retrievedCode))
 									hSub.put(temp,ac.getEntityDescription().getContent());
 							}
 						}
 
 
 						} catch (Exception ee) {
 							logger.error(
 									"problem0 in Thesaurus EVSSearch-getSubConceptNames: "
 									+ ee, ee);
 						}
 					} else if (type.equals("All")) {
 
 						String[] codesToExpand = null;
 						if (!conceptCode.equals(""))//try it with ConceptCode
 						{
 
 							try {
 								LexBIGServiceConvenienceMethods lbscm = 
 									(LexBIGServiceConvenienceMethods) evsService.getGenericExtension("LexBIGServiceConvenienceMethods"); 						
 
 								lbscm.setLexBIGService(evsService);
 
 								AssociationList al = lbscm.getHierarchyLevelNext(dtsVocab, 
 										null, //CodingScheme Version or Tag
 										null, //hierarchy id
 										conceptCode, // Concept code
 										true, //Resolve concepts?
 										null); //Any qualifiers for the association?
 
 								int size = al.getAssociationCount();
 								codesToExpand = new String[size];
 								for (int i = 0; i < size; i++) {
 									Association assn = al.getAssociation(i);
 									AssociatedConceptList acl = assn.getAssociatedConcepts();
 									AssociatedConcept[] aca = acl.getAssociatedConcept();
 									for (AssociatedConcept ac:aca) {
 										String temp = ac.getCode();
 										if (temp != null && !temp.equals(conceptCode))
 											codesToExpand[i] = temp;
 									}
 								}
 							} catch (Exception ee) {
 								logger.error(
 										"ERROR - EVSSearch-getSubConcepts for Thesaurus : "
 										+ ee.toString(), ee);
 							}
 						} else if (conceptName != null && !conceptName.equals("")) {
 							try {
 								LexBIGServiceConvenienceMethods lbscm = 
 									(LexBIGServiceConvenienceMethods) evsService.getGenericExtension("LexBIGServiceConvenienceMethods"); 
 								lbscm.setLexBIGService(evsService);
 
 								String retrievedCode = this.do_getEVSCode(conceptName, dtsVocab);
 
 								AssociationList al = lbscm.getHierarchyLevelNext(dtsVocab, 
 										null, //CodingScheme Version or Tag
 										null, //hierarchy id
 										retrievedCode, // Concept code
 										true, //Resolve concepts?
 										null); //Any qualifiers for the association?
 
 								int size = al.getAssociationCount();
 								codesToExpand = new String[size];
 								for (int i = 0; i < size; i++) {
 									Association assn = al.getAssociation(i);
 									AssociatedConceptList acl = assn.getAssociatedConcepts();
 									AssociatedConcept[] aca = acl.getAssociatedConcept();
 									for (AssociatedConcept ac:aca) {
 										String temp = ac.getCode();
 										if (temp != null && !temp.equals(conceptCode))
 											codesToExpand[i] = temp;
 									}
 								}
 							} catch (Exception ee) {
 								logger.error(
 										"ERROR - EVSSearch-getSubConcepts for Thesaurus : "
 										+ ee.toString(), ee);
 							}
 						}
 						if (codesToExpand != null)
 							codesToExpand = getAllSubConceptNames(dtsVocab,
 									codesToExpand, hSub);
 					}
 				}
 			} catch (Exception ee) {
 				logger.error(
 						"ERROR - EVSSearch-getSubConcepts for Thesaurus : "
 						+ ee.toString(), ee);
 				return hSub;
 			}
 		}
 		return hSub;
 	}
 
 	/**
 	 * This method searches EVS vocabularies and returns subconcepts, which are used
 	 * to construct an EVS Tree. 
 	 * @param dtsVocab String the EVS Vocabulary
 	 * @param conceptName String the root concept.
 	 * @param type String 
 	 * @param conceptCode the root concept.
 	 * @param defSource
 	 * @return Vector of concept objectgs
 	 *
 	 */
 	//Seems to be unused method
 	/*
 	@SuppressWarnings("unchecked")
 	private Vector getSubConceptCodes(String dtsVocab, String conceptName,
 			String type, String conceptCode, String defSource) {
 		String[] stringArray = new String[10000];
 		Vector vSub = new Vector();
 		dtsVocab = m_eBean.getVocabAttr(m_eUser, dtsVocab,
 				EVSSearch.VOCAB_NULL, EVSSearch.VOCAB_NAME); // "", "vocabName");
 		EVSQuery query = new EVSQueryImpl();
 		List subs = null;
 		if (!dtsVocab.equals(EVSSearch.META_VALUE)) // "MetaValue"))  
 		{
 			try {
 				//String prefName = "";
 				if (type.equals("Immediate") || type.equals("")) {
 					try {
 						if (conceptCode != null && !conceptCode.equals("")) {
 							try {
 								query = this.registerSecurityToken(query, "",
 										dtsVocab);
 								query.getSubConcepts(dtsVocab, conceptCode);
 								subs = evsService.evsSearch(query);
 							} catch (Exception ex) {
 								ex.printStackTrace();
 							}
 							if (subs != null && subs.size() > 0) {
 								for (int i = 0; i < subs.size(); i++) {
 									stringArray[i] = (String) subs.get(i);
 									vSub.addElement((String) subs.get(i));
 								}
 							} else if (!conceptName.equals("")) //try it with ConceptName
 							{
 								try {
 									query = this.registerSecurityToken(query, "",
 											dtsVocab);
 									query.getSubConcepts(dtsVocab, conceptName);
 									subs = evsService.evsSearch(query);
 								} catch (Exception ex) {
 									ex.printStackTrace();
 								}
 								if (subs != null && subs.size() > 0) {
 									for (int b = 0; b < subs.size(); b++) {
 										stringArray[b] = (String) subs.get(b);
 										vSub.addElement((String) subs.get(b));
 									}
 								}
 							}
 						}
 					} catch (Exception ee) {
 						logger.error(
 								"problem in Thesaurus EVSSearch-getSubConceptCodes: "
 								+ ee, ee);
 						stringArray = null;
 					}
 				}
 				if (type.equals("All")) {
 					String sDefSource = (String) m_classReq
 					.getParameter("defSource");
 					if (sDefSource == null)
 						sDefSource = "";
 					vSub = this.getAllSubConceptCodes(dtsVocab, conceptName,
 							"All", conceptCode, sDefSource);
 				}
 			} catch (Exception ee) {
 				logger.error(
 						"ERROR - EVSSearch-getSubConceptCodes for Thesaurus : "
 						+ ee.toString(), ee);
 				return vSub;
 			}
 		}
 		return vSub;
 	}
 	 */
 	/**
 	 * For a referenced value domain, this method calculates how many levels down in
 	 * the heirarchy a subconcept is from the parent concept
 	 * @param CCode stirng code id
 	 * @param dtsVocab string of vocab name
 	 * @return int level
 	 */
 	/*
 	private int getLevelDownFromParent(String CCode, String dtsVocab) {
 		int level = 0;
 		//check if valid dts vocab and get out if meta search
 		dtsVocab = m_eBean.getVocabAttr(m_eUser, dtsVocab,
 				EVSSearch.VOCAB_NULL, EVSSearch.VOCAB_NAME); // "", "vocabName");
 		if (!dtsVocab.equals(EVSSearch.META_VALUE)) // "MetaValue")) 
 			return level;
 
 		String[] stringArrayInit = new String[20];
 		String[] stringArray = null;
 		Boolean flagOne = new java.lang.Boolean(true);
 		Boolean flagTwo = new Boolean(true);
 		String sParent = "";
 		String sSuperConceptCode = "";
 		int index = 0;
 		int loopCheck = 0;
 		String supName = "";
 		HttpSession session = m_classReq.getSession();
 		String sSearchAC = (String) session.getAttribute("creSearchAC");
 		if (sSearchAC == null)
 			sSearchAC = "";
 		String matchParent = "false";
 		if (sSearchAC.equals("ParentConceptVM")) {
 			sParent = (String) session.getAttribute("ParentConceptCode");
 			if (CCode.equals(sParent))
 				matchParent = "true";
 			if (sParent != null && !sParent.equals("")) {
 				while (matchParent.equals("false")) {
 					loopCheck++;
 					if (loopCheck > 20) // exit gracefully from an infinite loop
 					{
 						level = 0;
 						break;
 					}
 					try {
 						if (CCode != null && !CCode.equals("")) {
 							EVSQuery query = new EVSQueryImpl();
 							query = this.registerSecurityToken(query, "", dtsVocab);
 							query.getSuperConcepts(dtsVocab, CCode);
 							List supers = null;
 							try {
 								supers = evsService.evsSearch(query);
 							} catch (Exception ex) {
 								ex.printStackTrace();
 							}
 							if (supers != null && supers.size() > 0) {
 								for (int i = 0; i < supers.size(); i++) {
 									supName = (String) supers.get(i);
 									if (supName != null && !supName.equals(""))
 										stringArrayInit[i] = supName;
 									index = i;
 								}
 								stringArray = new String[index + 1];
 								for (int k = 0; k < (index + 1); k++) {
 									supName = stringArrayInit[k];
 									if (supName != null && !supName.equals(""))
 										stringArray[k] = stringArrayInit[k];
 								}
 							}
 						}
 					} catch (Exception ee) {
 						logger.error(
 								"problem2 in Thesaurus EVSSearch-getLevelDownFromParent: "
 								+ ee, ee);
 						stringArray = null;
 					}
 					if (stringArray != null && stringArray.length == 1) // == 1
 					{
 						level++;
 						sSuperConceptCode = (String) stringArray[0];
 						if (sSuperConceptCode.equals(sParent)) {
 							matchParent = "true";
 						} else
 							CCode = sSuperConceptCode;
 					} else if (stringArray != null && stringArray.length > 1) {
 						level++;
 						sSuperConceptCode = findThePath(dtsVocab, stringArray,
 								sParent);
 						if (sSuperConceptCode.equals(""))
 							sSuperConceptCode = (String) stringArray[0];
 						if (sSuperConceptCode.equals(sParent)) {
 							matchParent = "true";
 						} else if (!sSuperConceptCode.equals(""))
 							CCode = sSuperConceptCode;
 						else if (sSuperConceptCode.equals(""))
 							matchParent = "true"; 
 					} else
 						matchParent = "true";
 				}
 			}
 		}
 		return level;
 	}
 	 */
 	/**
 	 * When getting superConcepts, sometimes more than one is returned in the superConcepts array. 
 	 * This method looks at each member of the array and checks which one leads up
 	 * to the parent concept, then returns the superconcept which leads up to parent
 	 * to construct an EVS Tree. 
 	 * @param dtsVocab the EVS Vocabulary
 	 * @param stringArray array of string
 	 * @param sParent 
 	 * @return sCorrectSuperConceptCode
 	 *
 	 */
 	/*
 	private String findThePath(String dtsVocab, String[] stringArray,
 			String sParent) {
 		Boolean flagOne = new java.lang.Boolean(true);
 		//  Boolean flagTwo = new Boolean(false);
 		String[] stringArray2 = null;
 		String sSuperConceptCode = "";
 		String sCorrectSuperConceptCode = "";
 		String matchParent = "false";
 		String prefName = "";
 		String prefNameCurrent = "";
 
 		//check if valid dts vocab and get out if meta search
 		dtsVocab = m_eBean.getVocabAttr(m_eUser, dtsVocab,
 				EVSSearch.VOCAB_NULL, EVSSearch.VOCAB_NAME); // "", "vocabName");
 		if (dtsVocab.equals(EVSSearch.META_VALUE)) // "MetaValue")) 
 			return "";
 
 		for (int j = 0; j < stringArray.length; j++) {
 			matchParent = "false";
 			prefName = (String) stringArray[j];
 			prefNameCurrent = (String) stringArray[j];
 			if (prefName != null && prefName.equals(sParent)) {
 				matchParent = "true";
 				sCorrectSuperConceptCode = prefName;
 			}
 			while (matchParent.equals("false")) {
 				try {
 					if (prefName != null && !prefName.equals("")) {
 						EVSQuery query = new EVSQueryImpl();
 						query = this.registerSecurityToken(query, "", dtsVocab);
 						query.getSuperConcepts(dtsVocab, prefName);
 						List supers = null;
 						try {
 							supers = evsService.evsSearch(query);
 						} catch (Exception ex) {
 							logger.error("ERROR findThePath: " + ex.toString(),
 									ex);
 						}
 						if (supers != null && supers.size() > 0) {
 							String supName = "";
 							stringArray2 = new String[supers.size()];
 							for (int i = 0; i < supers.size(); i++) {
 								supName = (String) supers.get(i);
 								if (supName != null && !supName.equals(""))
 									stringArray2[i] = supName;
 							}
 						} else
 							break;
 					}
 				} catch (Exception ee) {
 					stringArray2 = null;
 				}
 				if (stringArray2 != null && stringArray2.length == 1) {
 					sSuperConceptCode = (String) stringArray2[0];
 					if (sSuperConceptCode.equals(sParent)) {
 						matchParent = "true";
 						sCorrectSuperConceptCode = prefNameCurrent;
 						break;
 					} else
 						prefName = sSuperConceptCode;
 				} else if (stringArray2 != null && stringArray2.length > 1) // == 1
 				{
 					for (int i = 0; i < stringArray2.length; i++) {
 						sSuperConceptCode = (String) stringArray2[i];
 						if (sSuperConceptCode.equals(sParent)) {
 							matchParent = "true";
 							sCorrectSuperConceptCode = prefNameCurrent;
 							break;
 						}
 					}
 					if (matchParent.equals("false")) {
 						prefName = findThePath(dtsVocab, stringArray2, sParent);
 						if (prefName.equals(""))
 							matchParent = "true"; // stop the loop
 					}
 				} else
 					matchParent = "true";
 			}
 		}
 		return sCorrectSuperConceptCode;
 	}
 	 */
 
 	/**
 	 * This method returns all subconcepts of a concept
 	 * @param dtsVocab the EVS Vocabulary
 	 * @param stringArray array of strings
 	 * @param vSub vector of subs
 	 * @return stringArray
 	 *
 	 */
 
 
 	public boolean getSubConceptCount(String dtsVocab, ResolvedConceptReference rcr) {
 
 		ResolvedConceptReferencesIterator ret = null;
 		try {
 //			LexBIGServiceConvenienceMethods lbscm = 
 //				(LexBIGServiceConvenienceMethods) evsService.getGenericExtension("LexBIGServiceConvenienceMethods");
 //			lbscm.setLexBIGService(evsService);
 
 			CodedNodeSet matches = 
 				evsService.getCodingSchemeConcepts(dtsVocab, null)
 							.restrictToCodes(ConvenienceMethods.createConceptReferenceList(new String[] { rcr.getCode() }));
 						
 			ret = matches.resolve(null, null, null);
 			if (ret != null && ret.hasNext())
 				return true;
 			else 
 				return false;
 		} catch (LBException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 		return false;
 	}
 
 	@SuppressWarnings("unchecked")
 	public String[] getAllSubConceptNames(String dtsVocab,
 			String[] stringArray, HashMap<String, String> hSub) {
 		String[] stringArray2 = null;
 		String[] stringArray3 = new String[10000];
 		String conceptCode = "";
 		List subs = null;
 		dtsVocab = m_eBean.getVocabAttr(m_eUser, dtsVocab,
 				EVSSearch.VOCAB_NULL, EVSSearch.VOCAB_NAME); // "", "vocabName");
 		if (dtsVocab.equals(EVSSearch.META_VALUE)) // "MetaValue")) 
 			return stringArray;
 		try {
 			if (stringArray != null && stringArray.length > 0) {
 				for (int j = 0; j < stringArray.length; j++) {
 					if (stringArray[j] != null) {
 						conceptCode = stringArray[j];
 						try {
 							this.registerSecurityToken((LexEVSApplicationService)evsService, dtsVocab,m_eUser);
 							LexBIGServiceConvenienceMethods lbscm = 
 								(LexBIGServiceConvenienceMethods) evsService.getGenericExtension("LexBIGServiceConvenienceMethods"); 
 							lbscm.setLexBIGService(evsService);
 
 							AssociationList al = lbscm.getHierarchyLevelNext(dtsVocab, 
 									null, //CodingScheme Version or Tag
 									null, //hierarchy id
 									conceptCode, // Concept code
 									true, //Resolve concepts?
 									null); //Any qualifiers for the association?
 
 							int size = al.getAssociationCount();
 							if (size > 0)
 								stringArray2 = new String[size];
 							else 
 								stringArray = null;
 
 							for (int i = 0; i < size; i++) {
 								Association assn = al.getAssociation(i);
 								AssociatedConceptList acl = assn.getAssociatedConcepts();
 								AssociatedConcept[] aca = acl.getAssociatedConcept();
 								for (AssociatedConcept ac:aca) {
 									String temp = ac.getCode();
 									if (temp != null && !temp.equals(conceptCode)) {
 										stringArray2[i] = temp;
 										hSub.put(temp, ac.getEntityDescription().getContent());
 									}
 								}
 							}
 						} catch (Exception ee) {
 							logger.error(
 									"problem2a in Thesaurus EVSSearch-getAllSubConceptNames: "
 									+ ee, ee);
 						}
 						if (stringArray2 != null && stringArray2.length > 0) {
 							stringArray3 = getAllSubConceptNames(dtsVocab,
 									stringArray2, hSub);
 						}
 					}
 				}
 			}
 		} catch (Exception f) {
 			logger.error(
 					"problem in Thesaurus EVSSearch-getAllSubConceptNames: "
 					+ f, f);
 		}
 		// evsService = null;
 		return stringArray;
 	}
 
 	/**
 	 *  Puts in and takes out "_"
 	 *  @param nodeName
 	 *  @param type
 	 *  @return String return fitlered 
 	 */
 	private final String filterName(String nodeName, String type) {
 		if (type.equals("display"))
 			nodeName = nodeName.replaceAll("_", " ");
 		else if (type.equals("js"))
 			nodeName = nodeName.replaceAll(" ", "_");
 		return nodeName;
 	}
 
 	/**
 	 * This method searches EVS vocabularies and returns superconcepts name of subconcepts
 	 * on level below concept in heirarchy
 	 * @param dtsVocab the EVS Vocabulary
 	 * @param conceptName the root concept.
 	 * @param conceptCode
 	 * @return vSub
 	 */
 	@SuppressWarnings("unchecked")
 	public HashMap<String, String> getSuperConceptNamesImmediate(String dtsVocab,
 			String conceptName, String conceptCode) {
 		HashMap<String, String> concepts = new HashMap<String, String>();
 		//check if valid dts vocab and get out if meta search
 		dtsVocab = m_eBean.getVocabAttr(m_eUser, dtsVocab,
 				EVSSearch.VOCAB_NULL, EVSSearch.VOCAB_NAME); // "", "vocabName");
 
 		try {
 			this.registerSecurityToken((LexEVSApplicationService)evsService, dtsVocab,m_eUser);
 		} catch (Exception e1) {
 			e1.printStackTrace();
 			logger.error("EVSSearch:getSuperConceptNamesImmediate() registerSecurityToken [" + e1 + "]");	//JT
 		}
 		
 		if (dtsVocab.equals(EVSSearch.META_VALUE)) // "MetaValue")) 
 			return concepts;
 
 		try {
 			LexBIGServiceConvenienceMethods lbscm = 
 				(LexBIGServiceConvenienceMethods) evsService.getGenericExtension("LexBIGServiceConvenienceMethods"); 
 			lbscm.setLexBIGService(evsService);
 
 			if (conceptCode.equals("") && !conceptName.equals(""))
 				conceptCode = this.do_getEVSCode(conceptName, dtsVocab);
 
 			concepts = returnSuperConceptNames(conceptCode, dtsVocab);
 			
 		} catch (IndexOutOfBoundsException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (LBException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return concepts;
 	}
 
 	/**
 	 * This method searches EVS vocabularies and returns superconcepts, which are used
 	 * to construct an EVS Tree. 
 	 * @param dtsVocab the EVS Vocabulary
 	 * @param conceptName
 	 * @param conceptCode
 	 * @return vSub
 	 */
 	@SuppressWarnings("unchecked")
 	public HashMap<String,String> getSuperConceptNames(String dtsVocab, String conceptName,
 			String conceptCode) {
 		
 		HashMap<String,String> compound = new HashMap<String,String>();
 		 HashMap<String,String> concepts = new HashMap<String,String>();
 		 
 		//check if valid dts vocab and get out if meta search
 		dtsVocab = m_eBean.getVocabAttr(m_eUser, dtsVocab,
 				EVSSearch.VOCAB_NULL, EVSSearch.VOCAB_NAME); // "", "vocabName");
 		if (dtsVocab.equals(EVSSearch.META_VALUE)) // "MetaValue")) 
 			return compound;
 
 		try {
 			LexBIGServiceConvenienceMethods lbscm = 
 				(LexBIGServiceConvenienceMethods) evsService.getGenericExtension("LexBIGServiceConvenienceMethods"); 
 			lbscm.setLexBIGService(evsService);
 			try {
 				this.registerSecurityToken((LexEVSApplicationService)evsService, dtsVocab, m_eUser);
 			} catch (Exception e) {
 				e.printStackTrace();
 				logger.error("EVSSearch:getSuperConceptNames() registerSecurityToken [" + e + "]");	//JT
 			}
 			
 			if (conceptCode.equals("") && !conceptName.equals(""))
 				conceptCode = this.do_getEVSCode(conceptName, dtsVocab);
 
 			concepts = returnSuperConceptNames(conceptCode, dtsVocab);
 
 			compound.putAll(concepts);
 			/*AssociationList al = lbscm.getHierarchyLevelPrev(dtsVocab, 
 					null, //CodingScheme Version or Tag
 					null, //hierarchy id
 					conceptCode, // Concept code
 					true, //Resolve concepts?
 					null); //Any qualifiers for the association?
 
 			int size = al.getAssociationCount();
 			stringArray = new String[size];
 			for (int i = 0; i < size; i++) {
 				Association assn = al.getAssociation(i);
 				AssociatedConceptList acl = assn.getAssociatedConcepts();
 				AssociatedConcept[] aca = acl.getAssociatedConcept();
 				for (AssociatedConcept ac:aca) {
 					String temp = ac.getCode();
 					if (temp != null && !temp.equals(conceptCode) && !vSub.contains(temp)) {
 						vSub.addElement(temp);
 						concepts.put(temp, ac.getEntityDescription().getContent());
 						stringArray[i] = temp;
 					}
 				}
 			}*/
 		} catch (IndexOutOfBoundsException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (LBException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		getAllSuperConceptNames(dtsVocab,
 				compound, concepts);
 
 		return compound;
 	}
 
 	/**
 	 * This method returns all superconceptnames of a concept
 	 * @param dtsVocab the EVS Vocabulary
 	 * @param stringArray
 	 * @param vSub
 	 * @return vSub
 	 *
 	 */
 	@SuppressWarnings("unchecked")
 	private void getAllSuperConceptNames(String dtsVocab, HashMap<String, String> compound, HashMap<String,String> concepts) {
 		HashMap<String,String> compoundLevel = new HashMap<String, String>();
 		String[] stringArray2 = null;
 		String[] stringArray3 = null;
 		String[] stringArray4 = null;
 
 		//check if valid dts vocab and get out if meta search
 		dtsVocab = m_eBean.getVocabAttr(m_eUser, dtsVocab,
 				EVSSearch.VOCAB_NULL, EVSSearch.VOCAB_NAME); // "", "vocabName");
 		if (dtsVocab.equals(EVSSearch.META_VALUE)) // "MetaValue")) 
 			return;
 
 		List subs = null;
 		int index = 0;
 		Iterator<String> conceptIter = concepts.keySet().iterator();
 		if (conceptIter.hasNext()) {
 			try {
 				while (conceptIter.hasNext()) {
 						try {
 							LexBIGServiceConvenienceMethods lbscm = 
 								(LexBIGServiceConvenienceMethods) evsService.getGenericExtension("LexBIGServiceConvenienceMethods"); 
 							lbscm.setLexBIGService(evsService);
 							
 							concepts = returnSuperConceptNames(conceptIter.next(), dtsVocab);
 
 							if (concepts.size() > 0 ){
 								compound.putAll(concepts);	
 								compoundLevel.putAll(concepts);
 							}
 							/*AssociationList al = lbscm.getHierarchyLevelPrev(dtsVocab, 
 									null, //CodingScheme Version or Tag
 									null, //hierarchy id
 									stringArray[j], // Concept code
 									true, //Resolve concepts?
 									null); //Any qualifiers for the association?
 
 							int size = al.getAssociationCount();
 
 							for (int i = 0; i < size; i++) {
 								Association assn = al.getAssociation(i);
 								AssociatedConceptList acl = assn.getAssociatedConcepts();
 								AssociatedConcept[] aca = acl.getAssociatedConcept();
 								for (AssociatedConcept ac:aca) {
 									String temp = ac.getCode();
 									if (temp != null && !temp.equals(stringArray[j]) && !vSub.contains(temp)) {
 										vSub.addElement(temp);
 										concepts.put(temp, ac.getEntityDescription().getContent());
 										compoundResults.add(temp);
 									}
 								}
 							}*/
 						} catch (IndexOutOfBoundsException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						} catch (LBException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 				}
 			} catch (Exception ee) {
 				logger.error(
 						"problem2a in Thesaurus EVSSearch-getAllSuperConceptNames: "
 						+ ee, ee);
 			}
 			if (compoundLevel.size() > 0)
 				getAllSuperConceptNames(dtsVocab, compound, compoundLevel);
 		}
 		return;
 	}
 
 
 	/**
 	 * To get final result vector of selected attributes/rows to display for Object Class component,
 	 * called from getACKeywordResult, getACSortedResult and getACShowResult methods.
 	 * gets the selected attributes from session vector 'selectedAttr'.
 	 * loops through the OCBean vector 'vACSearch' and adds the selected fields to result vector.
 	 *
 	 * @param req The HttpServletRequest object.
 	 * @param res HttpServletResponse object.
 	 * @param vResult output result vector.
 	 * @param refresh
 	 *
 	 */
 	@SuppressWarnings("unchecked")
 	public void get_Result(HttpServletRequest req, HttpServletResponse res,
 			Vector vResult, String refresh) {
 		Vector vOC = new Vector();
 		try {
 			HttpSession session = req.getSession();
 			String menuAction = (String) session
 			.getAttribute(Session_Data.SESSION_MENU_ACTION);
 			Vector vSearchASL = new Vector();
 			Vector vSelAttr = new Vector();
 			if (menuAction.equals("searchForCreate")
 					|| menuAction.equals("BEDisplay"))
 				vSelAttr = (Vector) session.getAttribute("creSelectedAttr");
 			else
 				vSelAttr = (Vector) session.getAttribute("selectedAttr");
 
 			vOC = (Vector) session.getAttribute("vACSearch");
 			Vector vRSel = new Vector();
 			if (menuAction.equals("searchForCreate")) //|| menuAction.equals("BEDisplay")) 
 				vRSel = (Vector) session.getAttribute("vACSearch"); //from selected rows   //null;
 			else
 				vRSel = (Vector) session.getAttribute("vSelRows"); //from selected rows   //null;
 			if (vRSel == null)
 				vRSel = new Vector();
 			Integer recs = new Integer(0);
 			if (vRSel.size() > 0)
 				recs = new Integer(vRSel.size());
 			String recs2 = "";
 			if (recs != null)
 				recs2 = recs.toString();
 			String sKeyword = "";
 			if (menuAction.equals("searchForCreate")
 					|| menuAction.equals("BEDisplay")) {
 				session.setAttribute("creRecsFound", recs2);
 				sKeyword = (String) session.getAttribute("creKeyword");
 			} else {
 				session.setAttribute("recsFound", recs2);
 				sKeyword = (String) session.getAttribute("serKeyword");
 			}
 			if (sKeyword == null)
 				sKeyword = "";
 			String sSearchAC = "";
 			String sSelectedParent = "";
 			if (menuAction.equals("searchForCreate")) {
 				sSearchAC = (String) session.getAttribute("creSearchAC");
 				sSelectedParent = (String) session
 				.getAttribute("SelectedParent");
 				if (sSelectedParent == null)
 					sSelectedParent = "";
 			} else
 				sSearchAC = (String) session.getAttribute("searchAC");
 			if (sSearchAC.equals("EVSValueMeaning")
 					|| sSearchAC.equals("ParentConceptVM")
 					|| sSearchAC.equals("VMConcept")
 					|| sSearchAC.equals("EditVMConcept")
 					|| sSearchAC.equals("ValueMeaning")
 					|| sSearchAC.equals("CreateVM_EVSValueMeaning"))
 				sSearchAC = "Value Meaning";
 			boolean isMainConcept = false;
 			if (!menuAction.equals("searchForCreate")
 					&& sSearchAC.equals("ConceptClass"))
 				isMainConcept = true;
 
 			String evsDB = "";
 			String ccode = "";
 			//prepare 2 session vectors for getAssociatedAC
 			Vector vSearchID = new Vector();
 			Vector vSearchName = new Vector();
 			for (int i = 0; i < (vRSel.size()); i++) {
 				EVS_Bean OCBean = new EVS_Bean();
 				OCBean = (EVS_Bean) vRSel.elementAt(i);
 				evsDB = OCBean.getEVS_DATABASE();
 
 				ccode = OCBean.getCONCEPT_IDENTIFIER();
 				String sLevel = "";
 				int iLevel = OCBean.getLEVEL();
 				Integer Int = new Integer(iLevel);
 				if (Int != null)
 					sLevel = Int.toString();
 
 				//assign 2 session vectors for getAssociatedAC
 				vSearchID.addElement(OCBean.getIDSEQ());
 				vSearchName.addElement(OCBean.getLONG_NAME());
 
 				if (vSelAttr.contains("Concept Name") || refresh.equals("DEF"))
 					vResult.addElement(OCBean.getLONG_NAME());
 				if (vSelAttr.contains("Public ID"))
 					vResult.addElement(OCBean.getID());
 				if (vSelAttr.contains("Version"))
 					vResult.addElement(OCBean.getVERSION());
 				if (vSelAttr.contains("EVS Identifier"))
 					vResult.addElement(OCBean.getCONCEPT_IDENTIFIER());
 				if (isMainConcept) //main page concept class search addd it here with evs origin
 					if (vSelAttr.contains("Vocabulary")
 							|| refresh.equals("DEF"))
 						vResult.addElement(OCBean.getEVS_DATABASE());
 				if (vSelAttr.contains("Definition") || refresh.equals("DEF"))
 					vResult.addElement(OCBean.getPREFERRED_DEFINITION());
 				if (vSelAttr.contains("Definition Source")
 						|| refresh.equals("DEF"))
 					vResult.addElement(OCBean.getEVS_DEF_SOURCE());
 				if (vSelAttr.contains("Workflow Status"))
 					vResult.addElement(OCBean.getASL_NAME());
 				if (vSelAttr.contains("Semantic Type"))
 					vResult.addElement(OCBean.getEVS_SEMANTIC());
 				if (vSelAttr.contains("Context") && !refresh.equals("DEF"))
 					vResult.addElement(OCBean.getCONTEXT_NAME());
 				if (vSelAttr.contains("Comments"))
 					vResult.addElement(OCBean.getCOMMENTS());
 				if (!isMainConcept) //add it here only if not the main concept 
 					if (vSelAttr.contains("Vocabulary")
 							|| refresh.equals("DEF"))
 						vResult.addElement(OCBean.getEVS_DATABASE());
 				if (vSelAttr.contains("caDSR Component"))
 					vResult.addElement(OCBean.getcaDSR_COMPONENT());
 				if (vSelAttr.contains("DEC's Using"))
 					vResult.addElement(OCBean.getDEC_USING());
 				if (vSelAttr.contains("Level"))
 					vResult.addElement(sLevel);
 			}
 
 			// set session var for getAssociatedAC
 			DataManager.setAttribute(session, "SearchID", vSearchID);
 			DataManager.setAttribute(session, "SearchName", vSearchName);
 			// push info on stacks for back button
 			GetACSearch serAC = new GetACSearch(m_classReq, m_classRes,
 					m_servlet);
 			if (!refresh.equals("true")
 					&& (!menuAction.equals("searchForCreate")))
 				serAC.stackSearchComponents(sSearchAC, vOC, vRSel, vSearchID,
 						vSearchName, vResult, vSearchASL, vSearchName);
 
 			if (sSearchAC.equals("PropertyClass"))
 				sSearchAC = "Property";
 			else if (sSearchAC.equals("ObjectClass"))
 				sSearchAC = "Object Class";
 			else if (sSearchAC.equals("Property"))
 				sSearchAC = "Property";
 			else if (sSearchAC.equals("ObjectQualifier"))
 				sSearchAC = "Object Qualifier";
 			else if (sSearchAC.equals("PropertyQualifier"))
 				sSearchAC = "Property Qualifier";
 			else if (sSearchAC.equals("RepQualifier"))
 				sSearchAC = "Rep Qualifier";
 			else if (sSearchAC.equals("RepTerm"))
 				sSearchAC = "Rep Term";
 			else if (sSearchAC.equals("ConceptClass"))
 				sSearchAC = "Concept Class";
 
 			if (evsDB.equals(m_eUser.getPrefVocab())
 					|| evsDB.equals("Thesaurus/Metathesaurus"))
 				sKeyword = filterName(sKeyword, "display");
 			if (!sSearchAC.equals("ParentConcept"))
 				session.setAttribute("labelKeyword", sSearchAC + " - " + sKeyword); //make the label
 			else if (sSearchAC.equals("ParentConcept"))
 				session.setAttribute("labelKeyword", " - " + sKeyword);
 
 		} catch (Exception e) {
 			logger.error("ERROR in EVSSearch-get_Result : " + e.toString(), e);
 		}
 	}
 
 	/**
 	 * gets the non evs parent from reference documents table for the vd
 	 * @param sParent String parent name
 	 * @param sCui String meta cui
 	 * @param PageVD String page VD
 	 * @return String document name 
 	 * @throws java.lang.Exception
 	 */
 	private String getMetaParentSource(String sParent, String sCui, VD_Bean PageVD) throws Exception
 	{ 
 		//search the existing reference document 
 		String sRDMetaCUI = "";
 
 		if(PageVD != null)
 		{
 			GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, m_servlet);
 			Vector vRef = serAC.doRefDocSearch(PageVD.getVD_VD_IDSEQ(), "META_CONCEPT_SOURCE", "open");
 			Vector vList = (Vector)m_classReq.getAttribute("RefDocList");
 			if (vList != null && vList.size() > 0)
 			{
 				for (int i=0; i<vList.size(); i++)
 				{
 					REF_DOC_Bean RDBean = (REF_DOC_Bean)vList.elementAt(i);
 					//copy rd attributes to evs attribute
 					if (RDBean != null && RDBean.getDOCUMENT_NAME() != null && !RDBean.getDOCUMENT_NAME().equals(""))
 					{
 						sRDMetaCUI = RDBean.getDOCUMENT_TEXT();
 						if(sRDMetaCUI.equals(sCui))
 							return RDBean.getDOCUMENT_NAME();
 						else if(sCui.equals("None"))
 							return RDBean.getDOCUMENT_NAME();
 					}
 				}
 			}
 		}
 		return "";
 	}
 
 	/**
 	 * makes the comma delimited context name for used by contexts
 	 * @param sUsed
 	 * @param desContext
 	 * @return subUsed
 	 */
 	private String ParseString (String sUsed, String desContext)
 	{
 		String subUsed = "";
 		if ((sUsed != null) && (!sUsed.equals("")))
 		{
 			StringTokenizer desTokens = new StringTokenizer(sUsed, ",");
 			while (desTokens.hasMoreTokens())
 			{
 				String thisToken = desTokens.nextToken().trim();
 				if (!thisToken.equalsIgnoreCase(desContext))
 				{
 					if (subUsed.equals(""))
 						subUsed = thisToken;
 					else
 						subUsed = subUsed + ", " + thisToken;
 				}
 			}
 		}
 		return subUsed;
 	}
 
 	/**
 	 * 
 	 * @param sSynonym
 	 * @return isHeader
 	 */
 	private String parseSynonymForHD(String sSynonym) {
 		String isHeader = "false";
 		if ((sSynonym != null) && (!sSynonym.equals(""))) {
 			if (sSynonym.indexOf(">HD<") != -1)
 				isHeader = "true";
 		}
 		return isHeader;
 	}
 
 	/**
 	 * To get final result vector of selected attributes/rows to display for Rep Term component,
 	 * gets the selected attributes from session vector 'selectedAttr'.
 	 * loops through the RepBean vector 'vACSearch' and adds the selected fields to result vector.
 	 *
 	 */
 	@SuppressWarnings("unchecked")
 	public void getMetaSources() {
 		try {
 			if (evsService == null) {
 				logger
 				.error("Error - EVSSearch-getMetaSources : no evs connection");
 				return;
 			}
 			Vector vMetaSources = new Vector();
 			HttpSession session = m_classReq.getSession();
 			String source = "";
 
 			CodingScheme cs = evsService.resolveCodingScheme("NCI MetaThesaurus", null);
 			SupportedSource[] ssa = cs.getMappings().getSupportedSource();
 
 
 			for (SupportedSource ss: ssa) {
 				String sourceName = ss.getLocalId();	
 				vMetaSources.addElement(sourceName);
 			}
 			DataManager.setAttribute(session, "MetaSources", vMetaSources);
 		} catch (Exception ex) {
 			logger.error("Error - EVSSearch-getMetaSources : " + ex.toString(),
 					ex);
 		}
 	}
 
 	/**
 	 * various search for concept method including name, cui etc for all vocabularies
 	 * stores the results in EVS bean which then stored in the vector to send back.
 	 * @param vConList vector existing vector of concepts 
 	 * @param termStr String search term
 	 * @param dtsVocab STring vocabulary name
 	 * @param sSearchIn String search in attribute
 	 * @param namePropIn String property name to search in
 	 * @param sSearchAC String AC name for the search
 	 * @param sIncludeRet String include or exclude the retired search
 	 * @param sMetaSource String meta source selected
 	 * @param iMetaLimit int meta search result limit
 	 * @param isMetaSearch boolean to meta search or not
 	 * @param ilevel int current level of the concept
 	 * @param subConType String immediate or all sub concepts to return
 	 * @return VEctor of concept bean 
 	 */
 	public Vector<EVS_Bean> doVocabSearch(Vector<EVS_Bean> vConList,
 			String termStr, String dtsVocab, String sSearchIn,
 			String namePropIn, String sSearchAC, String sIncludeRet,
 			String sMetaSource, int iMetaLimit, boolean isMetaSearch,
 			int ilevel, String subConType, HashSet<String> sConSet) {
 		try {
 			//do not continue if empty string search.
 			if (termStr == null || termStr.equals(""))
 				return vConList;
 			//check if the concept name property exists for the vocab
 			if (vConList == null)
 				vConList = new Vector<EVS_Bean>();
 				ilevel += 1;
 				//get vocab specific propeties stored in the vocab bean 
 				//get the vocab specific attributes
 				Hashtable hVoc = (Hashtable) m_eUser.getVocab_Attr();
 				if (hVoc == null)
 					hVoc = new Hashtable();
 				EVS_UserBean vocabBean = null;
 				if (hVoc.containsKey(dtsVocab))
 					vocabBean = (EVS_UserBean) hVoc.get(dtsVocab);
 				if (vocabBean == null)
 					vocabBean = (EVS_UserBean) m_servlet.sessionData.EvsUsrBean; //(EVS_UserBean)session.getAttribute(EVS_USER_BEAN_ARG);  //("EvsUserBean");
 				String namePropDisp = vocabBean.getPropNameDisp();
 				if (namePropDisp == null)
 					namePropDisp = "";
 				if (namePropIn == null || namePropIn.equals("")) //get the vocab specific prop to search in
 					namePropIn = vocabBean.getPropName();
 				String conCodeType = vocabBean.getVocabCodeType();
 				String sMetaName = vocabBean.getIncludeMeta();
 				String vocabDisp = vocabBean.getVocabName();
 				//int ilevel = 0;  //it is zero for keyword search
 				String defnProp = vocabBean.getPropDefinition();
 				String hdSynProp = vocabBean.getPropHDSyn();
 				String retConProp = vocabBean.getPropRetCon();
 				String semProp = vocabBean.getPropSemantic();
 				String vocabType = vocabBean.getNameType();
 				String sDefDefault = vocabBean.getDefDefaultValue(); // "No Value Exists.";
 				try {
 					//call method to do the search from EVS vocab
 					ResolvedConceptReferenceList lstResults = null;
 					//do not do vocab search if it is meta code search
 					if (!sSearchIn.equals("MetaCode")
 							&& !sMetaName.equals(vocabDisp))
 						lstResults = this.doConceptQuery(	//GF29786
 								vocabBean.getVocabAccess(), termStr, dtsVocab,
 								sSearchIn, vocabType, namePropIn, sSearchAC);
 					//get the desc object from the list
 					if (lstResults != null) {
 						Hashtable hType = m_eUser.getMetaCodeType();
 						if (hType == null)
 							hType = new Hashtable();
 						skipConcept: for (int i = 0; i < lstResults.getResolvedConceptReferenceCount(); i++) {
 
 							String sFullSyn = "", sSemantic = "", sStatus = "Active"; //sPrefName = "", 
 							String sDispName = "";
 							String vocabMetaType = "", vocabMetaCode = "";
 
 							ResolvedConceptReference rcr = new ResolvedConceptReference();
 							rcr = (ResolvedConceptReference) lstResults.getResolvedConceptReference(i);
 							if (!rcr.getEntity().isIsActive()){
 								if (sIncludeRet != null
 										&& sIncludeRet.equals("Include"))
 									sStatus = "Retired"; //property.getValue();
 								else
 									continue skipConcept;
 							}
 
 							String sConName = rcr.getEntityDescription().getContent();
 							if (sConName == null)
 								sConName = "";
 							String sConID = rcr.getCode();
 							if (sConID == null)
 								sConID = "";
 							sDispName = sConName;
 
 							Property[] props = rcr.getEntity().getProperty();
 							Presentation[] presentations = rcr.getEntity().getPresentation();
 							Definition[] definitions = rcr.getEntity().getDefinition();
 
 							sDispName = rcr.getEntityDescription().getContent();
 
 							if (sSearchAC.equals("ParentConceptVM")){
 								for (Presentation presentation: presentations) {
 									if (presentation.getPropertyName().indexOf(hdSynProp) > 0)
 										sFullSyn = presentation.getValue().getContent();
 									String isHeader = this.parseSynonymForHD(sFullSyn);
 									if (isHeader.equals("true"))
 										continue skipConcept; //do not add to the list if header concept for parent
 								}
 
 								for (Property property: props) {
 									if (property != null) {
 										String propName = property.getPropertyName();
 										if (propName == null)
 											propName = "";
 										String propValue = property.getValue().getContent();
 										if (propValue == null)
 											propValue = "";
 
 										if (propName.indexOf(semProp) >= 0) //"Semantic" property
 										{
 											if (!sSemantic.equals(""))
 												sSemantic += ", ";
 											sSemantic += propValue; // property.getValue();
 										}
 										//									if (!namePropDisp.equals("")
 										//											&& propName.indexOf(namePropDisp) >= 0) //"Preferred_Name" prop for concept name if not emtpy
 										//										sDispName = propValue; // property.getValue();
 										String sMeta = this.getNCIMetaCodeType(
 												propName, "byKey");
 										if (sMeta != null && !sMeta.equals("")) // (hType.containsKey(propName))  //evs source type for nci- meta
 										{
 											vocabMetaType = propName;
 											vocabMetaCode = propValue; // property.getValue();
 										}
 									}
 								}
 							}
 							//store to concept according to the number of defitions exist for a concept if already not stored
 							if (!sConSet.contains(sConID)) {	//GF29786 - comparing concepts based on EVS identifier and save it
 								vConList = this.storeConceptToBean(vConList,
 										definitions, dtsVocab, sConName, sDispName,
 										conCodeType, sConID, ilevel, sStatus,
 										sSemantic, sDefDefault, defnProp,
 										vocabMetaType, vocabMetaCode);
 								
 								sConSet.add(sConID);
 								//repeat the sub concept query for child concepts if all sub concept action
 								
 								if (sSearchIn.equals("subConcept")
 										&& subConType.equals("All")) {						
 	
 									boolean bool = getSubConceptCount(dtsVocab, rcr);
 	
 									if (bool) {
 										this.doVocabSearch(vConList, sConID, dtsVocab,
 												sSearchIn, "", sSearchAC, sIncludeRet,
 												sMetaSource, iMetaLimit, isMetaSearch,
 												ilevel, subConType, sConSet);
 									}
 								}
 							}
 						}
 					}
 				} catch (Exception ee) {
 					logger.error(
 							"ERROR - EVSSearch-doVocabSearch - vocab result : "
 							+ ee.toString(), ee);
 				}
		        logger.debug("sMetaName = [" + sMetaName + "] isMetaSearch = [" + isMetaSearch + "]");
 				//do the meta thesaurus search if meta name exists and if meta search is true
				if (sMetaName != null && !sMetaName.equals("") && isMetaSearch)	{	//GF32446 - "Semantic Type" empty issue due to sMetaName is empty
			        logger.debug("doMetaSearch calling with termStr = [" + termStr + "] sSearchIn = [" + sSearchIn + "] sMetaSource = [" + sMetaSource + "] iMetaLimit = [" + iMetaLimit + "] sMetaName = [" + sMetaName + "]");
 					vConList = this.doMetaSearch(vConList, termStr, sSearchIn,
 							sMetaSource, iMetaLimit, sMetaName);
			        logger.info("doMetaSearch called");
				}
 		} catch (Exception ee) {
 			logger.error("ERROR - EVSSearch-doVocabSearch : " + ee.toString(),
 					ee);
 		}
 		return vConList;
 	}
 
 	/**
 	 * gets the display name of the concepts
 	 * @param dtsVocab STring vocabulary name
 	 * @param dlc concepts desclogicobject from the API call
 	 * @param sName name of the concept
 	 * @return String display name
 	 */
 	public String getDisplayName(String dtsVocab, ResolvedConceptReference dlc,
 			String sName) {
 		sName = this.do_getConceptName(sName,dtsVocab);
 		String dispName = sName;
 
 		try {
 			Vector vVocabs = (Vector) m_eUser.getVocabNameList();
 			if (vVocabs == null)
 				vVocabs = new Vector();
 
 			//make sure the vocab exists in the list
 			if (!vVocabs.contains(dtsVocab))
 				return sName;
 			//get the property from cadsr
 			Hashtable vHash = (Hashtable) m_eUser.getVocab_Attr();
 			EVS_UserBean eUser = (EVS_UserBean) vHash.get(dtsVocab);
 			if (eUser == null)
 				return sName;
 			String sPropDisp = eUser.getPropNameDisp();
 			if (sPropDisp == null || sPropDisp.equals(""))
 				return dispName;
 			//get the value of the display property
 			if (dlc == null) //get it from evs query
 			{
 				/*EVSQuery query = new EVSQueryImpl();
 				query = this.registerSecurityToken(query, "", dtsVocab);
 				//getPropertyValues(String vocabularyName,String conceptCode, String propertyName)
 				 query.getPropertyValues(dtsVocab, sName, sPropDisp);
 				List lstResult = evsService.evsSearch(query);
 				if (lstResult != null && lstResult.size() > 0)
 					return (String) lstResult.get(0);
 				else*/
 				return sName;
 			} else //get it from dlc object
 			{//get Preferred Name (stored in entity description)
 				dispName = dlc.getEntityDescription().getContent();
 				if (dispName != null && !dispName.equals(""))
 					return dispName;
 				else
 					return sName;
 			}
 		} catch (Exception ee) {
 			logger.error("ERROR - EVSSearch-getDisplayName : " + ee.toString(),
 					ee);
 		}
 		return sName;
 	}
 
 	/**
 	 * @param vocabAccess
 	 * @param termStr
 	 * @param dtsVocab
 	 * @param sSearchIn
 	 * @param vocabType
 	 * @param sPropIn
 	 * @return
 	 */
 	private ResolvedConceptReferenceList doConceptQuery(String vocabAccess, String termStr,
 			String dtsVocab, String sSearchIn, String vocabType, String sPropIn, String sSearchAC) {
 		ResolvedConceptReferenceList lstResult = null;
 		List lstResult1 = null;
 		List lstResult2= new ArrayList();
 
 		String algorithm = getAlgorithm(termStr);
 		termStr = cleanTerm(termStr);
 		
 		try {
 			// check if valid dts vocab
 			dtsVocab = m_eBean.getVocabAttr(m_eUser, dtsVocab,
 					EVSSearch.VOCAB_NULL, EVSSearch.VOCAB_NAME); // "",
 			// "vocabName");
 			if (dtsVocab.equals(EVSSearch.META_VALUE)) // "MetaValue"))
 				return lstResult;
 
 			this.registerSecurityToken((LexEVSApplicationService)evsService, dtsVocab, m_eUser);
 			
 			CodedNodeSet nodeSet = evsService.getNodeSet(dtsVocab, null, null);
 
 			if (sSearchIn.equals("ConCode")) {
 				
 				ConceptReferenceList crefs = ConvenienceMethods.
                 	createConceptReferenceList(new String[]{termStr}, "NCI Thesaurus");
 				nodeSet = nodeSet.restrictToCodes(crefs);
 
 			}
 			else if (sSearchIn.equals("subConcept"))
 				//query.getChildConcepts(dtsVocab, termStr);
 				try {
 								
 					HashMap<String, ResolvedConceptReference> hSubs = returnSubConcepts(termStr, dtsVocab);
 
 					lstResult = new ResolvedConceptReferenceList();
 					Iterator<String> iter = hSubs.keySet().iterator();
 					while (iter.hasNext()) {
 						String code = iter.next();
 						ResolvedConceptReference ac = hSubs.get(code);													
 							if (code != null && !code.equals(termStr)) {
 								lstResult.addResolvedConceptReference(ac);	
 						}
 					}
 				} catch (IndexOutOfBoundsException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (LBException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				else {
 					if (vocabType.equals("") || vocabType.equals("NameType")) // do concept name search
 						nodeSet = nodeSet.restrictToMatchingDesignations(termStr, //the text to match 
 								CodedNodeSet.SearchDesignationOption.PREFERRED_ONLY,  //whether to search all designation, only Preferred or only Non-Preferred
 								algorithm, //the match algorithm to use
 								null); //the language to match (null matches all)
 					else if (vocabType.equals("PropType")) { // do concept prop search
 						LocalNameList lnl = new LocalNameList();
 						lnl.addEntry(sPropIn);
 						nodeSet = nodeSet.restrictToMatchingProperties(
 								lnl, //the Property Name to match
 								null, //the Property Type to match (null matches all)
 								termStr, //the text to match
 								algorithm, //the match algorithm to use
 								null );//the language to match (null matches all)
 						logger.debug("EVSSearch:doConceptQuery() nodeSet retrieved from lexEVS.");	//GF29786 - geting the concept list from lexevs based on synonyms done
 					}
 				}
 			// call the evs to get resutls
 			if(!sSearchIn.equals("subConcept"))
 			{
 				LocalNameList lnl = new LocalNameList();
 				Hashtable hType = m_eUser.getMetaCodeType();
 				Iterator iter = hType.keySet().iterator();
 				while (iter.hasNext()){
 					String propName = (String) iter.next();
 					lnl.addEntry(propName);
 				}
 				
 				if (sSearchAC.equals("ParentConceptVM"))
 				lstResult = nodeSet.resolveToList(
 						null, //Sorts used to sort results (null means sort by match score)
 						lnl, //PropertyNames to resolve (null resolves all)
 						new CodedNodeSet.PropertyType[] {PropertyType.DEFINITION, PropertyType.PRESENTATION},  //PropertyTypess to resolve (null resolves all)  //PropertyTypess to resolve (null resolves all)
 						100	  //cap the number of results returned (-1 resolves all)
 				);
 				else
 					lstResult = nodeSet.resolveToList(
 							null, //Sorts used to sort results (null means sort by match score)
 							null, //PropertyNames to resolve (null resolves all)
 							new CodedNodeSet.PropertyType[] {PropertyType.DEFINITION, PropertyType.PRESENTATION},  //PropertyTypess to resolve (null resolves all)
 							100	  //cap the number of results returned (-1 resolves all)
 					);
 					
 			}
 		} catch (Exception ex) {
 			logger.error(evsService.toString()
 					+ " :conceptNameSearch lstResults: " + ex.toString(), ex);
 		}
 		return lstResult;
 	}
 
 	/**
 	 * @param vCons
 	 * @param vProp
 	 * @param dtsVocab
 	 * @param sConName
 	 * @param sDispName
 	 * @param conCodeType
 	 * @param sConID
 	 * @param ilevel
 	 * @param sStatus
 	 * @param sSemantic
 	 * @param sDefDefault
 	 * @param defnProp
 	 * @param sMType
 	 * @param sMCode
 	 * @return
 	 */
 	private Vector<EVS_Bean> storeConceptToBean(Vector<EVS_Bean> vCons,
 			Definition[] definitions, String dtsVocab, String sConName, String sDispName,
 			String conCodeType, String sConID, int ilevel, String sStatus,
 			String sSemantic, String sDefDefault, String defnProp,
 			String sMType, String sMCode) {
 		try {
 			EVS_Bean conBean = new EVS_Bean();
 			//get the db origin vocab name from the vocab name
 			String dbVocab = conBean.getVocabAttr(m_eUser, dtsVocab,
 					EVSSearch.VOCAB_NAME, EVSSearch.VOCAB_DBORIGIN); // "vocabName", "vocabDBOrigin");
 
 			HttpSession session = m_classReq.getSession();
 			//do the definition separatly so that concept for each definition is displayed in new row and bean.
 			boolean defExists = false;
 			for (Definition def: definitions) {
 
 				String sDef = def.getValue().getContent();
 				String sDefSrc = "";
 				if (def.getSourceCount() > 0) {
 					sDefSrc = def.getSource()[0].getContent(); //get def source
 				}
 				defExists = true;
 				//add the properties to the bean
 				conBean = new EVS_Bean();
 				conBean.setEVSBean(sDef, sDefSrc, sConName, sDispName,
 						conCodeType, sConID, dtsVocab, dbVocab, ilevel,
 						"", "", "", sStatus, sSemantic, sMType, sMCode);
 				conBean.markNVPConcept(conBean, session);
 //				if(conBean.getCONCEPT_NAME().contains("Breast Cancer pTX TNM")) {	//GF29786
 //					logger.info(conBean.toString());
 //				}
 				vCons.addElement(conBean); //add concept bean to the vector
 			}
 
 			//add concept to the bean if not done by definition loop already
 			if (!defExists) {
 				//add the properties to the bean
 				String sDef = sDefDefault;
 				conBean = new EVS_Bean();
 				conBean.setEVSBean(sDef, "", sConName, sDispName, conCodeType,
 						sConID, dtsVocab, dbVocab, ilevel, "", "", "", sStatus,
 						sSemantic, sMType, sMCode);
 				conBean.markNVPConcept(conBean, session);
 				vCons.addElement(conBean);
 			}
 		} catch (Exception ex) {
 			logger.error("storeConceptToBean: " + ex.toString(), ex);
 		}
 		return vCons;
 	}
 
 	/**
 	 * @param vList
 	 * @param termStr
 	 * @param sSearchIn
 	 * @param sMetaSource
 	 * @param iMetaLimit
 	 * @param sVocab
 	 * @return
 	 */
 	private Vector<EVS_Bean> doMetaSearch(Vector<EVS_Bean> vList,
 			String termStr, String sSearchIn, String sMetaSource,
 			int iMetaLimit, String sVocab) {
 
 		ResolvedConceptReferenceList concepts = null;
 
 		if (vList == null)
 			vList = new Vector<EVS_Bean>();
 			try {
 				if (termStr == null || termStr.equals(""))
 					return vList;
 				List metaResults = null;
 				CodedNodeSet nodeSet = evsService.getNodeSet("NCI MetaThesaurus", null, null);
 				try {
 					if (sSearchIn.equalsIgnoreCase("MetaCode")) { //do meta code specific to vocabulary source
 						// In the NCI MetaThesaurus, fidning the 'source' of an 'Atom' is equivalent to finding the 
 						//'source' of a given Property of an Entity. Each CUI (which is equivalent to an Entity in 
 						//LexEVS) may contain several Presentation Properties (Atoms or AUI's of that CUI). 
 						//Each of these Presentation Properties is Qualified by a 'source-code' Qualifier, 
 						//which reflects the code of this Atom in its original source, and a 'source' qualifier, 
 						//which states the source itself that this Atom came from
 
 						//query.searchSourceByAtomCode(termStr, sMetaSource);
 
 						CodedNodeSet.PropertyType[] types = new CodedNodeSet.PropertyType[1];
 						types[0] = CodedNodeSet.PropertyType.PRESENTATION;
 
 						nodeSet = nodeSet.restrictToProperties(
 								Constructors.createLocalNameList("propertyType"), 
 								types, 
 								Constructors.createLocalNameList(sMetaSource), 
 								null, 
 								null);
 
 						nodeSet = nodeSet.restrictToMatchingProperties(
 								Constructors.createLocalNameList("value"), //the Property Name to match
 								null, //the Property Type to match (null matches all)
 								termStr, //the text to match
 								"contains", //the match algorithm to use
 								null );//the language to match (null matches all)
 
 					}
 					else if (sSearchIn.equalsIgnoreCase("ConCode")) //meta cui search
 						nodeSet = nodeSet.restrictToMatchingProperties(
 								Constructors.createLocalNameList("code"), //the Property Name to match
 								null, //the Property Type to match (null matches all)
 								termStr, //the text to match
 								"exactMatch", //the match algorithm to use
 								null //the language to match (null matches all)
 						);
 					else
 						//meta keyword search
 						nodeSet = nodeSet.restrictToMatchingDesignations(termStr, //the text to match 
 								CodedNodeSet.SearchDesignationOption.PREFERRED_ONLY,  //whether to search all designation, only Preferred or only Non-Preferred
 								"contains", //the match algorithm to use
 								null); //the language to match (null matches all)
 
 					concepts = nodeSet.resolveToList(
 							null, //Sorts used to sort results (null means sort by match score)
 							null, //PropertyNames to resolve (null resolves all)
 							null,  //PropertyTypess to resolve (null resolves all)
 							1000	  //cap the number of results returned (-1 resolves all)
 					);
 
 				} catch (Exception ex) {
 					logger.error("doMetaSearch evsSearch: " + ex.toString(), ex);	//JT - lexvs error here!!!
 				}
 				if (concepts != null && concepts.getResolvedConceptReferenceCount() > 0) {
 					String sConName = "";
 					String sConID = "";
 					String sCodeType = "";
 					String sSemantic = "";
 					String sCodeSrc = "";
 					int iLevel = 0;
 					for (int i = 0; i < concepts.getResolvedConceptReferenceCount(); i++) {
 						// Do this so only one result is returned on Meta code search (API is dupicating a result)
 						if (sSearchIn.equals("MetaCode") && i > 0)
 							break;
 						//get concept properties
 						ResolvedConceptReference rcr = concepts.getResolvedConceptReference(i);
 
 						if (rcr != null) {
 							Property[] props = rcr.getEntity().getProperty();
 							Presentation[] presentations = rcr.getEntity().getPresentation();
 							Definition[] definitions = rcr.getEntity().getDefinition();
 
 							sConName = rcr.getEntityDescription().getContent();
 							sConID = rcr.getCode();
 
 							sCodeType = this.getNCIMetaCodeType(sConID, "byID");
 
 							//get semantic types
 							sSemantic = this.getMetaSemantics(props);	//TBD - JT "Semantic Type" might be empty here?
 							//get preferred source code from atom collection
 							sCodeSrc = this.getPrefMetaCode(presentations);
 
 							//get definition attributes
 							String sDefSource = "";
 							String sDefinition = m_eUser.getDefDefaultValue();
 							//add sepeate record for each definition
 							if (definitions != null && definitions.length > 0) {
 								for (Definition defType: definitions) {
 									sDefinition = defType.getValue().getContent();
 									sDefSource = defType.getSource()[0].getContent();
 
 									EVS_Bean conBean = new EVS_Bean();
 									conBean.setEVSBean(sDefinition, sDefSource,
 											sConName, sConName, sCodeType, sConID,
 											sVocab, sVocab, iLevel, "", "", "", "",
 											sSemantic, "", "");
 									conBean.setPREF_VOCAB_CODE(sCodeSrc); //store pref code in the bean
 									vList.addElement(conBean); //add concept bean to vector
 								}
 							} else {
 								EVS_Bean conBean = new EVS_Bean();
 								conBean.setEVSBean(sDefinition, sDefSource,
 										sConName, sConName, sCodeType, sConID,
 										sVocab, sVocab, iLevel, "", "", "", "",
 										sSemantic, "", "");
 								conBean.setPREF_VOCAB_CODE(sCodeSrc); //store pref code in the bean
 								vList.addElement(conBean); //add concept bean to vector              
 							}
 						}
 					}
 				}
 			} catch (Exception ex) {
 				logger.error("doMetaSearch exception : " + ex.toString(), ex);
 			}
 			return vList;
 	}
 
 	/**
 	 * @param conID
 	 * @param ftrType
 	 * @return
 	 * @throws Exception
 	 */
 	private String getNCIMetaCodeType(String conID, String ftrType)
 	throws Exception {
 		String sCodeType = "";
 		//get the hash table of meta code property types
 		Hashtable hType = m_eUser.getMetaCodeType();
 		if (hType == null)
 			hType = new Hashtable();
 		//define code type according to the con id
 		Enumeration enum1 = hType.keys();
 		while (enum1.hasMoreElements()) {
 			String sKey = (String) enum1.nextElement();
 			EVS_METACODE_Bean metaBean = (EVS_METACODE_Bean) hType.get(sKey);
 			if (metaBean == null)
 				metaBean = new EVS_METACODE_Bean();
 			String sMCode = metaBean.getMETACODE_TYPE();
 			String sCFilter = metaBean.getMETACODE_FILTER().toUpperCase(); //  (String)hType.get(sMCode);
 			if (sCFilter == null)
 				sCFilter = "";
 			if (ftrType.equals("byID")) {
 				//get the default value regardless
 				if (sCFilter.equals("DEFAULT"))
 					sCodeType = sMCode;
 				//use the fitlered one if exists and leave
 				else if (!sCFilter.equals("")
 						&& conID.toUpperCase().indexOf(sCFilter) >= 0) {
 					sCodeType = sMCode;
 					break;
 				}
 			} else //by key
 			{
 				if (conID.toUpperCase().indexOf(sKey.toUpperCase()) >= 0) {
 					sCodeType = sMCode;
 					break;
 				}
 			}
 		}
 		return sCodeType;
 	}
 
 	/**
 	 * to get the semantic value for meta thesarurs concept from the collection
 	 * @param mtcCon MetaThesaurusConcept object
 	 * @return sSemantic
 	 */
 	private String getMetaSemantics(Property[] properties) {
 		String sSemantic = "";
 
 		for (Property prop: properties) {
 			String name = prop.getPropertyName();
 			if (name != null && name.equals("Semantic_Type")) {
 				if (!sSemantic.equals(""))
 					sSemantic += "; ";
 				sSemantic += prop.getValue().getContent();
 			}
 		}
 		return sSemantic;
 	}
 
 	/**
 	 * to get the NCI thesaurus code from the atom collection
 	 * @param mtcCon MetaThesaurusConcept object
 	 * @return sCode
 	 */
 	private String getPrefMetaCode(Presentation[] presentations) {
 		String sCode = "";
 		String prefSrc = "";
 		for (Presentation pres: presentations) {
 			Source[] sources = pres.getSource();
 			prefSrc = m_eUser.getPrefVocabSrc();
 			if (prefSrc != null) {
 				for (Source src : sources) {
 					String sConSrc = src.getContent();
 					if (src != null && !sConSrc.equals("")) {
 						if (sConSrc.contains(prefSrc))
 							System.out.println("GOT THE PREFSRC");
 						if (sCode == null)
 							sCode = "";
 					}
 				}
 			}
 		}
 		return sCode;
 	}
 
 	/**
 	 * to get the vocabulary name
 	 * @param sMetaName
 	 * @return String vocabulary name from the vector
 	 */
 	private String getMetaVocabName(String sMetaName) {
 		String sVocab = "";
 		Vector vName = m_eUser.getVocabNameList();
 		Hashtable eHash = (Hashtable) m_eUser.getVocab_Attr();
 		//get the two vectors to check
 		for (int i = 0; i < vName.size(); i++) {
 			String sName = (String) vName.elementAt(i);
 			EVS_UserBean usrVocab = (EVS_UserBean) eHash.get(sName);
 			//get the thesaurs name for the meta thesarurs
 			String sMeta = usrVocab.getIncludeMeta();
 			if (sMeta != null && !sMeta.equals("") && sMetaName.equals(sMeta)) {
 				sVocab = sName;
 				break;
 			}
 		}
 		if (sVocab.equals(""))
 			sVocab = (String) vName.elementAt(0); //get the first one
 		return sVocab;
 	}
 
 	/**
 	 * gets request parameters to store the selected values in the session according to what the menu action is
 	 *
 	 * @throws Exception
 	 */
 	@SuppressWarnings("unchecked")
 	public void doGetSuperConcepts() throws Exception {
 		try {
 			HttpSession session = m_classReq.getSession();
 			String sConceptName = (String) m_classReq.getParameter("nodeName");
 			String sConceptCode = (String) m_classReq.getParameter("nodeCode");
 			String dtsVocab = (String) m_classReq.getParameter("vocab");
 			String sUISearchType = (String) m_classReq
 			.getParameter("UISearchType");
 			if (sUISearchType == null || sUISearchType.equals("nothing"))
 				sUISearchType = "term";
 			String sRetired = (String) m_classReq.getParameter("rRetired");
 			if (sRetired == null)
 				sRetired = "Include";
 			String sConteIdseq = (String) m_classReq
 			.getParameter("sConteIdseq");
 			if (sConteIdseq == null)
 				sConteIdseq = "";
 
 			if (sConceptName == null || sConceptCode == null
 					|| dtsVocab == null)
 				return;
 			Vector vAC = new Vector();
 			Vector vResult = new Vector();
 			Vector vSuperConceptNamesUnique = new Vector();
 			String sSearchAC = (String) session.getAttribute("creSearchAC");
 			if (sSearchAC == null)
 				sSearchAC = "";
 			Vector vSuperConceptNames = null;
 			String sName = "";
 			String sCode = "";
 			String sParentName = "";
 			dtsVocab = m_eBean.getVocabAttr(m_eUser, dtsVocab,
 					EVSSearch.VOCAB_DBORIGIN, EVSSearch.VOCAB_NAME); // "vocabDBOrigin", "vocabName");
 			if (sSearchAC.equals("ParentConceptVM"))
 				sParentName = (String) session.getAttribute("ParentConcept");
 			HashMap<String,String> compound = this.getSuperConceptNames(dtsVocab,
 					sConceptName, sConceptCode);
 			Iterator<String> compoundIter = compound.keySet().iterator();
 			
 			while (compoundIter.hasNext()) {
 				String tCode = compoundIter.next();
 				sName = compound.get(tCode);
 				if (!vSuperConceptNamesUnique.contains(sName)) {
 					vSuperConceptNamesUnique.addElement(sName);
 					if (sName != null && !sName.equals(""))
 						sCode = tCode;
 					if (sCode != null && !sCode.equals(""))
 						vAC = this.doVocabSearch(vAC, sCode, dtsVocab,
 								"ConCode", "", sSearchAC, sRetired, "", 100,
 								false, -1, "", new HashSet<String>());
 					if (sName.equals(sParentName))
 						break;
 				}
 			}
 			DataManager.setAttribute(session, "vACSearch", vAC);
 			if (sSearchAC.equals("ParentConcept"))
 				DataManager.setAttribute(session, "vParResult", vAC);
 			else if (sSearchAC.equals("ParentConceptVM"))
 				DataManager.setAttribute(session, "vParResultVM", vAC);
 			DataManager.setAttribute(session, "creRetired", sRetired);
 			DataManager.setAttribute(session, "dtsVocab", dtsVocab);
 			//get the final result vector
 			this.get_Result(m_classReq, m_classRes, vResult, "");
 			DataManager.setAttribute(session, "results", vResult);
 			DataManager.setAttribute(session, "creKeyword", sConceptCode);
 			session.setAttribute("labelKeyword", sConceptName);
 			Integer recs = new Integer(vAC.size());
 			String recs2 = recs.toString();
 			session.setAttribute("creRecsFound", recs2);
 			DataManager.setAttribute(session, "vACSearch", vAC);
 			if (sSearchAC.equals("ParentConceptVM"))
 				sUISearchType = "tree";
 			m_classReq.setAttribute("UISearchType", sUISearchType);
 
 			m_servlet.ForwardJSP(m_classReq, m_classRes,
 			"/OpenSearchWindowBlocks.jsp");
 		} catch (Exception ex) {
 			logger.error("doGetSuperConcepts : " + ex.toString(), ex);
 		}
 	}
 
 	/**
 	 * to show the concept in a tree
 	 * @param actType String action type
 	 */
 	public void showConceptInTree(String actType) {
 		try {
 			HttpSession session = m_classReq.getSession();
 			String sComp = (String) m_classReq.getParameter("searchComp");
 			if (sComp == null)
 				sComp = "";
 			DataManager.setAttribute(session, "creSearchAC", sComp);
 			String sCCodeDB = (String) m_classReq.getParameter("OCCCodeDB");
 			Vector vVocabList = m_eUser.getVocabNameList();
 			if (vVocabList == null)
 				vVocabList = new Vector();
 			String sCCode = (String) m_classReq.getParameter("OCCCode");
 			String sCCodeName = (String) m_classReq.getParameter("OCCCodeName");
 			String sNodeID = (String) m_classReq.getParameter("nodeID");
 			String dtsVocab = m_eBean.getVocabAttr(m_eUser, sCCodeDB,
 					EVSSearch.VOCAB_DBORIGIN, EVSSearch.VOCAB_NAME); // "vocabDBOrigin", "vocabName");
 			if (sCCode == null || sCCode.equals(""))
 				sCCode = this.do_getEVSCode(sCCodeName, dtsVocab);
 			logger.debug(sCCodeDB + " show concept " + sCCode + " dtsvocab "
 					+ dtsVocab);
 			if (!sCCode.equals("")) // && !sComp.equals("ParentConcept"))
 			{
 				//this.doCollapseAllNodes(dtsVocab);
 				//first get the details from evs as well as from cadsr
 				this
 				.doCallConceptSearch(sCCode, sCCodeDB, sCCodeName,
 						dtsVocab);
 				//open the tree if from the vocab list else just display teh details
 				if (vVocabList.contains(dtsVocab))
 					this.doCallTreeSearch("OpenTreeToConcept", dtsVocab,
 							sCCodeName, sCCode);
 			} else if (!sComp.equals("ParentConcept")) {
 				this.doCollapseAllNodes(sCCodeDB);
 				this.doTreeSearch(actType, "Blocks");
 				return;
 			}
 			session.setAttribute("labelKeyword", sCCodeName);
 			m_servlet.ForwardJSP(m_classReq, m_classRes,
 			"/OpenSearchWindowBlocks.jsp");
 		} catch (Exception e) {
 			logger.error("showConceptInTree : " + e.toString(), e);
 		}
 	}
 
 	/**
 	 * Opens teh tree to the selected concept
 	 * @param actType string action type
 	 * @param searchType string search tye of filter a simple or advanced
 	 * @param sCCode string concept identifier
 	 * @param sCCodeDB string concept database
 	 * @param sCCodeName string concept name
 	 * @param sNodeID string current node id
 	 *
 	 * @throws Exception
 	 */
 	private void doTreeOpenToConcept(String actType, String searchType,
 			String sCCode, String sCCodeDB, String sCCodeName, String sNodeID)
 	throws Exception {
 		HttpSession session = m_classReq.getSession();
 		String strHTML = "";
 		String sOpenToTree = (String) m_classReq.getParameter("openToTree");
 		DataManager.setAttribute(session, "OpenTreeToConcept", "true");
 		String sOpenTreeToConcept = (String) session
 		.getAttribute("OpenTreeToConcept");
 		if (sOpenTreeToConcept == null)
 			sOpenTreeToConcept = "";
 		if (sCCode.equals("") && !sCCodeName.equals(""))
 			sCCode = this.do_getEVSCode(sCCodeName, sCCodeDB);
 
 		if (sOpenToTree == null)
 			sOpenToTree = "";
 		if (actType.equals("term") || actType.equals(""))
 			m_classReq.setAttribute("UISearchType", "term");
 		else if (actType.equals("tree")) {
 			this.doTreeSearchRequest("", sCCode, "false", sCCodeDB);
 			m_classReq.setAttribute("UISearchType", "tree");
 			EVSMasterTree tree = new EVSMasterTree(m_classReq, sCCodeDB,
 					m_servlet);
 			Vector vStackVector = new Vector();
 			Vector vStackVector2 = new Vector();
 			strHTML = tree.populateTreeRoots(sCCodeDB);
 			Stack stackSuperConcepts = new Stack();
 			HashMap<String, String> hSuperImmediate = this.getSuperConceptNamesImmediate(
 					sCCodeDB, sCCodeName, sCCode);
 			Vector vSuperImmediate = new Vector();
 			vSuperImmediate.addAll(hSuperImmediate.values());
 			DataManager.setAttribute(session, "vSuperImmediate",
 					vSuperImmediate);
 			vStackVector2 = tree.buildVectorOfSuperConceptStacks(
 					stackSuperConcepts, sCCodeDB, sCCode, vStackVector);
 			if (vStackVector.size() < 1) // must be a root concept
 			{
 				Tree RootTree = (Tree) tree.m_treesHash.get(sCCodeDB);
 				if (RootTree != null)
 					strHTML = tree.renderHTML(RootTree);
 			} else {
 				Stack vStack = new Stack();
 				for (int j = 0; j < vStackVector.size(); j++) {
 					vStack = (Stack) vStackVector.elementAt(j);
 					if (vStack.size() > 0)
 						strHTML = tree.expandTreeToConcept(vStack, sCCodeDB,
 								sCCode);
 				}
 			}
 			DataManager.setAttribute(session, "strHTML", strHTML);
 			DataManager.setAttribute(session, "vSuperImmediate", null);
 		} else if (actType.equals("parentTree")) {
 			DataManager.setAttribute(session, "ParentConceptCode", sCCode);
 			DataManager.setAttribute(session, "ParentConcept", sCCodeName);
 			this.doTreeSearchRequest(sCCodeName, sCCode, "false", sCCodeDB);
 			m_classReq.setAttribute("UISearchType", "tree");
 			EVSMasterTree tree = new EVSMasterTree(m_classReq, sCCodeDB,
 					m_servlet);
 			if (!sCCodeDB.equals("NCI Metathesaurus"))
 				strHTML = tree.populateTreeRoots(sCCodeDB);
 			strHTML = tree.showParentConceptTree(sCCode, sCCodeDB, sCCodeName);
 			DataManager.setAttribute(session, "strHTML", strHTML);
 		}
 		m_servlet.ForwardJSP(m_classReq, m_classRes,
 		"/OpenSearchWindowBlocks.jsp");
 	}
 
 	/**
 	 * gets request parameters to store the selected values in the session according to what the menu action is
 	 * @param sConceptName string concept name
 	 * @param sConceptID string concept identifier
 	 * @param strForward string true or false values to forward the page or not
 	 * @param dtsVocab string vocab name
 	 *
 	 * @throws Exception
 	 */
 	public void doTreeSearchRequest(String sConceptName, String sConceptID,
 			String strForward, String dtsVocab) throws Exception {
 		HttpSession session = m_classReq.getSession();
 		DataManager.setAttribute(session, "ConceptLevel", "0");
 		String sUISearchType = (String) m_classReq.getAttribute("UISearchType");
 		if (sUISearchType == null || sUISearchType.equals("nothing"))
 			sUISearchType = "tree";
 		String sKeywordID = (String) m_classReq.getParameter("keywordID");
 		String sKeywordName = (String) m_classReq.getParameter("keywordName");
 		String conLevel = (String) m_classReq.getParameter("nodeLevel");
 		int iLevel = -1;
 		if (conLevel != null && !conLevel.equals(""))
 			iLevel = Integer.parseInt(conLevel);
 		if (sKeywordID == null || sKeywordID.equals(""))
 			sKeywordID = sConceptID;
 		if (sKeywordName == null || sKeywordName.equals(""))
 			sKeywordName = sConceptName;
 		//since dtsvocab is empty, get it from url vocab, otherwise get it from dropdown
 		if (dtsVocab == null || dtsVocab.equals(""))
 			dtsVocab = (String) m_classReq.getParameter("vocab");
 		if (dtsVocab == null || dtsVocab.equals(""))
 			dtsVocab = (String) m_classReq
 			.getParameter("listContextFilterVocab");
 		if (dtsVocab == null)
 			dtsVocab = ""; //dtsVocab;
 		if (sKeywordID.equals("") && !sKeywordName.equals(""))
 			sKeywordID = this.do_getEVSCode(sKeywordName, dtsVocab);
 		String sOpenToTree = (String) m_classReq.getParameter("openToTree");
 		if (sOpenToTree == null)
 			sOpenToTree = "";
 
 		String sRetired = (String) m_classReq.getParameter("rRetired");
 		if (sRetired == null)
 			sRetired = "Include";
 		String sConteIdseq = (String) m_classReq.getParameter("sConteIdseq");
 		if (sConteIdseq == null)
 			sConteIdseq = "";
 		DataManager.setAttribute(session, "creRetired", sRetired);
 		DataManager.setAttribute(session, "dtsVocab", dtsVocab);
 		String sSearchType = "";
 		String sSearchAC = (String) session.getAttribute("creSearchAC");
 
 		if (sSearchAC == null)
 			sSearchAC = "";
 		else if (sSearchAC.equals("ObjectClass"))
 			sSearchType = "OC";
 		else if (sSearchAC.equals("Property"))
 			sSearchType = "PROP";
 		else if (sSearchAC.equals("RepTerm"))
 			sSearchType = "REP";
 		else if (sSearchAC.equals("ObjectQualifier"))
 			sSearchType = "ObjQ";
 		else if (sSearchAC.equals("PropertyQualifier"))
 			sSearchType = "PropQ";
 		else if (sSearchAC.equals("RepQualifier"))
 			sSearchType = "RepQ";
 		Vector<EVS_Bean> vAC = new Vector<EVS_Bean>();
 		Vector vResult = new Vector();
 		GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, m_servlet);
 		if (sKeywordID != null) {
 			if (!sSearchAC.equals("ParentConcept")
 					&& !sSearchAC.equals("ParentConceptVM")) {
 				if (sSearchAC.equals("ObjectClass")
 						|| sSearchAC.equals("Property")
 						|| sSearchAC.equals("RepTerm"))
 					serAC.do_caDSRSearch(sKeywordID, "", "RELEASED", "", vAC,
 							sSearchType, "", "", "0");
 				vAC = serAC.do_ConceptSearch(sKeywordID, "", "", "RELEASED",
 						"", "", "", vAC, "0");
 			}
 
 			DataManager.setAttribute(session, "creKeyword", sKeywordID);
 			String sMetaSource = "";
 			int intMetaLimit = 100;
 			if (sKeywordID != null && !sKeywordID.equals(""))
 				vAC = this.doVocabSearch(vAC, sKeywordID, dtsVocab, "ConCode",
 						"", sSearchAC, sRetired, sMetaSource, intMetaLimit,
 						false, iLevel, "", new HashSet<String>());
 			else if (sKeywordName != null && !sKeywordName.equals(""))
 				vAC = this.doVocabSearch(vAC, sKeywordName, dtsVocab, "Name",
 						"", sSearchAC, sRetired, sMetaSource, intMetaLimit,
 						false, iLevel, "", new HashSet<String>());
 
 			DataManager.setAttribute(session, "vACSearch", vAC);
 			if (sSearchAC.equals("ParentConcept"))
 				DataManager.setAttribute(session, "vParResult", vAC);
 			else if (sSearchAC.equals("ParentConceptVM"))
 				DataManager.setAttribute(session, "vParResultVM", vAC);
 
 			this.get_Result(m_classReq, m_classRes, vResult, "");
 			DataManager.setAttribute(session, "results", vResult);
 
 			session.setAttribute("labelKeyword", sKeywordName);
 			DataManager.setAttribute(session, "labelKeyword", sKeywordName);
 			Integer recs = new Integer(vAC.size());
 			String recs2 = recs.toString();
 			sKeywordID = "";
 			sKeywordName = "";
 			session.setAttribute("creRecsFound", recs2);
 		}
 
 		m_classReq.setAttribute("UISearchType", "tree");
 		if (!strForward.equals("false")) {
 			m_servlet.ForwardJSP(m_classReq, m_classRes,
 			"/OpenSearchWindowBlocks.jsp");
 		}
 	}
 
 	/**
 	 *
 	 * @param actType String  type of filter a simple or advanced
 	 * @param searchType  String type of filter a simple or advanced
 	 *
 	 * @throws Exception
 	 */
 	public void doTreeSearch(String actType, String searchType)
 	throws Exception {
 		HttpSession session = m_classReq.getSession();
 		String dtsVocab = m_classReq.getParameter("listContextFilterVocab");
 		if (actType.equals("term") || actType.equals(""))
 			m_classReq.setAttribute("UISearchType", "term");
 		else if (actType.equals("tree")) {
 			m_classReq.setAttribute("UISearchType", "tree");
 			EVSMasterTree tree = new EVSMasterTree(m_classReq, dtsVocab,
 					m_servlet);
 			String strHTML = tree.populateTreeRoots(dtsVocab);
 			DataManager.setAttribute(session, "strHTML", strHTML);
 		}
 		m_servlet.ForwardJSP(m_classReq, m_classRes,
 		"/OpenSearchWindowBlocks.jsp");
 	}
 
 	/**
 	 * to collapse all the nodes of the tree
 	 * @param dtsVocab String vocabulary name
 	 * @throws Exception
 	 */
 	public void doCollapseAllNodes(String dtsVocab) throws Exception {
 		HttpSession session = m_classReq.getSession();
 		DataManager.setAttribute(session, "results", null);
 		DataManager.setAttribute(session, "vACSearch", null);
 		DataManager.setAttribute(session, "ConceptLevel", "0");
 		m_classReq.setAttribute("UISearchType", "term");
 		EVSMasterTree tree = new EVSMasterTree(m_classReq, dtsVocab, m_servlet);
 		tree.collapseAllNodes();
 	}
 
 	/**
 	 * gets request parameters to store the selected values in the session according to what the menu action is
 	 *
 	 * @throws Exception
 	 */
 	public void doTreeRefreshRequest() throws Exception {
 		HttpSession session = m_classReq.getSession();
 		String vocab = m_classReq.getParameter("vocab");
 		vocab = m_eBean.getVocabAttr(m_eUser, vocab, EVSSearch.VOCAB_DISPLAY,
 				EVSSearch.VOCAB_NAME); // "vocabDisplay", "vocabName");
 		DataManager.setAttribute(session, "dtsVocab", vocab);
 		this.doCollapseAllNodes(vocab);
 		EVSMasterTree tree = new EVSMasterTree(m_classReq, vocab, m_servlet);
 		String strHTML = tree.refreshTree(vocab, "");
 		DataManager.setAttribute(session, "strHTML", strHTML);
 		m_classReq.setAttribute("UISearchType", "tree");
 		m_servlet.ForwardJSP(m_classReq, m_classRes,
 		"/OpenSearchWindowBlocks.jsp");
 	}
 
 	/**
 	 * gets request parameters to store the selected values in the session according to what the menu action is
 	 * forwards JSP 'SearchResultsPage.jsp' if the action is not searchForCreate.
 	 * if action is searchForCreate forwards OpenSearchWindow.jsp
 	 *
 	 * @throws Exception
 	 */
 	public void doTreeExpandRequest() throws Exception {
 		HttpSession session = m_classReq.getSession();
 		String sSearchAC = (String) session.getAttribute("creSearchAC");
 		DataManager.setAttribute(session, "creKeyword", "");
 		if (sSearchAC == null)
 			sSearchAC = "";
 		else {
 			GetACSearch getACSearch = new GetACSearch(m_classReq, m_classRes,
 					m_servlet);
 			Vector vResult = getACSearch.refreshSearchPage(sSearchAC);
 			DataManager.setAttribute(session, "results", vResult);
 		}
 		String nodeName = m_classReq.getParameter("nodeName");
 		String nodeCode = m_classReq.getParameter("nodeCode");
 		String nodeID = m_classReq.getParameter("nodeID");
 		String vocab = m_classReq.getParameter("vocab");
 		if (vocab == null)
 			vocab = m_eUser.getPrefVocab();
 		DataManager.setAttribute(session, "dtsVocab", vocab);
 		if (nodeCode.equals("") && !nodeName.equals(""))
 			nodeCode = this.do_getEVSCode(nodeName, vocab);
 		EVSMasterTree tree = new EVSMasterTree(m_classReq, vocab, m_servlet);
 		String strHTML = tree.expandNode(nodeName, vocab, "", nodeCode, "", 0,
 				nodeID);
 		DataManager.setAttribute(session, "strHTML", strHTML);
 		m_classReq.setAttribute("UISearchType", "tree");
 		session.setAttribute("labelKeyword", nodeName);
 		m_servlet.ForwardJSP(m_classReq, m_classRes,
 		"/OpenSearchWindowBlocks.jsp");
 	}
 
 	/**
 	 * gets request parameters to store the selected values in the session according to what the menu action is
 	 * forwards JSP 'SearchResultsPage.jsp' if the action is not searchForCreate.
 	 * if action is searchForCreate forwards OpenSearchWindow.jsp
 	 *
 	 * @throws Exception
 	 */
 	public void doTreeCollapseRequest() throws Exception {
 		HttpSession session = m_classReq.getSession();
 		String sSearchAC = (String) session.getAttribute("creSearchAC");
 		if (sSearchAC == null)
 			sSearchAC = "";
 		else {
 			GetACSearch getACSearch = new GetACSearch(m_classReq, m_classRes,
 					m_servlet);
 			Vector vResult = getACSearch.refreshSearchPage(sSearchAC);
 			DataManager.setAttribute(session, "results", vResult);
 		}
 		String nodeName = m_classReq.getParameter("nodeName");
 		String vocab = m_classReq.getParameter("vocab");
 		String nodeID = m_classReq.getParameter("nodeID");
 		if (vocab == null)
 			vocab = m_eUser.getPrefVocab();
 		DataManager.setAttribute(session, "dtsVocab", vocab);
 		EVSMasterTree tree = new EVSMasterTree(m_classReq, vocab, m_servlet);
 		String strHTML = tree.collapseNode(nodeID, vocab, "", nodeName);
 		DataManager.setAttribute(session, "strHTML", strHTML);
 		m_classReq.setAttribute("UISearchType", "tree");
 		session.setAttribute("labelKeyword", nodeName);
 		m_servlet.ForwardJSP(m_classReq, m_classRes,
 		"/OpenSearchWindowBlocks.jsp");
 	}
 
 	/**
 	 * gets request parameters to store the selected values in the session according to what the menu action is
 	 *
 	 *
 	 * @throws Exception
 	 */
 	public void doGetSubConcepts() throws Exception {
 		HttpSession session = m_classReq.getSession();
 		String sConceptName = (String) m_classReq.getParameter("nodeName");
 		String sConceptCode = (String) m_classReq.getParameter("nodeCode");
 		String dtsVocab = (String) m_classReq.getParameter("vocab");
 		if (dtsVocab == null)
 			dtsVocab = "";
 		dtsVocab = m_eBean.getVocabAttr(m_eUser, dtsVocab,
 				EVSSearch.VOCAB_DBORIGIN, EVSSearch.VOCAB_NAME); // "vocabDBOrigin", "vocabName");
 		String sDefSource = (String) m_classReq.getParameter("defSource");
 		int ilevelStartingConcept = 0;
 		int ilevelImmediate = 0;
 		String sSearchType = (String) m_classReq.getParameter("searchType");
 		String sRetired = ""; //(String)m_classReq.getParameter("rRetired");
 		String sConteIdseq = (String) m_classReq.getParameter("sConteIdseq");
 		if (sConteIdseq == null)
 			sConteIdseq = "";
 
 		String sUISearchType = (String) m_classReq.getParameter("UISearchType");
 		if (sUISearchType == null || sUISearchType.equals("nothing"))
 			sUISearchType = "term";
 		if (dtsVocab == null)
 			dtsVocab = "";
 		if (sConceptName == null || sConceptCode == null || dtsVocab == null)
 			return;
 		Vector<EVS_Bean> vAC = new Vector<EVS_Bean>();
 		Vector vResult = new Vector();
 		String sSearchAC = (String) session.getAttribute("creSearchAC");
 		if (sSearchAC == null)
 			sSearchAC = "";
 		GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, m_servlet);
 		String sParent = (String) session.getAttribute("ParentConcept");
 		if (sParent == null)
 			sParent = "";
 		String sParentSource = "";
 		if (dtsVocab.equals(EVSSearch.META_VALUE)
 				&& sSearchAC.equals("ParentConceptVM"))
 			sParentSource = serAC.getMetaParentSource(sParent, "None", null);
 		if (!sParentSource.equals(""))
 			sDefSource = sParentSource;
 
 		if (sConceptName.equals(sParent))
 			ilevelStartingConcept = 0;
 		else if (sSearchAC.equals("ParentConceptVM")) {
 			String slevel = (String) m_classReq.getParameter("conLevel");
 			if (slevel != null && !slevel.equals(""))
 				ilevelStartingConcept = Integer.parseInt(slevel);
 		}
 		ilevelImmediate = ilevelStartingConcept + 1;
 		String sLevelStartingConcept = "";
 		Integer ILevelStartingConcept = new Integer(ilevelStartingConcept);
 		sLevelStartingConcept = ILevelStartingConcept.toString();
 		DataManager
 		.setAttribute(session, "ConceptLevel", sLevelStartingConcept);
 		if (dtsVocab.equals("MetaValue")) {
 			sDefSource = (String) session.getAttribute("ParentMetaSource");
 			sDefSource = getSourceToken(sDefSource);
 			if (sDefSource == null)
 				sDefSource = "";
 		}
 		//  EVSSearch evs = new EVSSearch(m_classReq, m_classRes, this); 
 		EVSMasterTree tree = new EVSMasterTree(m_classReq, dtsVocab, m_servlet);
 		vAC = this.doVocabSearch(vAC, sConceptCode, dtsVocab, "subConcept", "",
 				sSearchAC, "Exclude", "", 0, false, ilevelStartingConcept,
 				sSearchType, new HashSet<String>());
 		DataManager.setAttribute(session, "vACSearch", vAC);
 		if (sSearchAC.equals("ParentConcept"))
 			DataManager.setAttribute(session, "vParResult", vAC);
 		else if (sSearchAC.equals("ParentConceptVM"))
 			DataManager.setAttribute(session, "vParResultVM", vAC);
 		else
 			//for all other
 			DataManager.setAttribute(session, "vACSearch", vAC);
 
 		DataManager.setAttribute(session, "creRetired", sRetired);
 		DataManager.setAttribute(session, "dtsVocab", dtsVocab);
 
 		//get the final result vector
 		this.get_Result(m_classReq, m_classRes, vResult, "");
 		DataManager.setAttribute(session, "results", vResult);
 
 		DataManager.setAttribute(session, "creKeyword", sConceptCode);
 		session.setAttribute("labelKeyword", sConceptName);
 		Integer recs = new Integer(vAC.size());
 		String recs2 = recs.toString();
 		session.setAttribute("creRecsFound", recs2);
 		DataManager.setAttribute(session, "vACSearch", vAC);
 		if (sSearchAC.equals("ParentConceptVM"))
 			sUISearchType = "tree";
 		m_classReq.setAttribute("UISearchType", sUISearchType);
 		m_servlet.ForwardJSP(m_classReq, m_classRes,
 		"/OpenSearchWindowBlocks.jsp");
 	}
 
 	/**
 	 * @param sDefSource
 	 *            string def source selected
 	 * @return String defintion source
 	 * @throws Exception
 	 */
 	private String getSourceToken(String sDefSource) throws Exception
 	{
 		int index = -1;
 		int length = 0;
 		if (sDefSource != null)
 			length = sDefSource.length();
 		String pointStr = ": Concept Source ";
 		index = sDefSource.indexOf(pointStr);
 		if (index == -1)
 			index = 0;
 		if (index > 0 && length > 18)
 			sDefSource = sDefSource.substring((index + 17), sDefSource.length());
 		return sDefSource;
 	}
 
 
 	/**
 	 * to open the tree to the selected concept 
 	 * @param actType String action type
 	 */
 	public void openTreeToConcept(String actType) {
 		try {
 			HttpSession session = (HttpSession) m_classReq.getSession();
 			String sCCodeDB = (String) m_classReq.getParameter("sCCodeDB");
 			String sCCode = (String) m_classReq.getParameter("sCCode");
 			String sCCodeName = (String) m_classReq.getParameter("sCCodeName");
 			String sNodeID = (String) m_classReq.getParameter("nodeID");
 			String dtsVocab = m_eBean.getVocabAttr(m_eUser, sCCodeDB,
 					EVSSearch.VOCAB_DBORIGIN, EVSSearch.VOCAB_NAME); // "vocabDBOrigin", "vocabName");
 			Vector vVocabs = m_eUser.getVocabNameList();
 			if (vVocabs == null)
 				vVocabs = new Vector();
 			if (!sCCode.equals("")) {
 				if (actType.equals("OpenTreeToConcept"))
 					this.doCollapseAllNodes(dtsVocab);
 				else if (actType.equals("OpenTreeToParentConcept")) {
 					String treeName = "parentTree" + sCCodeName;
 					DataManager.setAttribute(session, "SelectedParentName",
 							sCCodeName);
 					DataManager.setAttribute(session, "SelectedParentCC",
 							sCCode);
 					DataManager.setAttribute(session, "SelectedParentDB",
 							sCCodeDB);
 					this.doCollapseAllNodes(treeName);
 				}
 				//first get the details from evs as well as from cadsr
 				this
 				.doCallConceptSearch(sCCode, sCCodeDB, sCCodeName,
 						dtsVocab);
 				//open the tree if from the vocab list else just display teh details
 				if (vVocabs.contains(dtsVocab)) {
 					DataManager.setAttribute(session, "dtsVocab", dtsVocab);
 					this
 					.doCallTreeSearch(actType, dtsVocab, sCCodeName,
 							sCCode);
 				}
 			} else {
 				this.doCollapseAllNodes(dtsVocab);
 				this.doTreeSearch(actType, "Blocks");
 			}
 			session.setAttribute("labelKeyword", sCCodeName);
 			m_servlet.ForwardJSP(m_classReq, m_classRes,
 			"/OpenSearchWindowBlocks.jsp");
 		} catch (Exception e) {
 			logger.error("Error - openTreeToConcept : " + e.toString(), e);
 		}
 	}
 
 	/**
 	 * to open the tree to the selected concept from the parent level 
 	 * @param actType String action type
 	 */
 	private void openTreeToParentConcept(String actType) {
 		try {
 			HttpSession session = m_classReq.getSession();
 			String sCCodeDB = (String) m_classReq.getParameter("sCCodeDB");
 			String sCCode = (String) m_classReq.getParameter("sCCode");
 			String sCCodeName = (String) m_classReq.getParameter("sCCodeName");
 			String sNodeID = (String) m_classReq.getParameter("nodeID");
 			String treeName = "parentTree" + sCCodeName;
 			DataManager.setAttribute(session, "SelectedParentName", sCCodeName);
 			DataManager.setAttribute(session, "SelectedParentCC", sCCode);
 			DataManager.setAttribute(session, "SelectedParentDB", sCCodeDB);
 			sCCodeDB = m_eBean.getVocabAttr(m_eUser, sCCodeDB,
 					EVSSearch.VOCAB_DBORIGIN, EVSSearch.VOCAB_NAME); // "vocabDBOrigin", "vocabName");
 			if (sCCode != null && !sCCode.equals("")) {
 				this.doCollapseAllNodes(treeName);
 				this.doTreeOpenToConcept("parentTree", "Blocks", sCCode,
 						sCCodeDB, sCCodeName, sNodeID);
 			} else {
 				this.doCollapseAllNodes(sCCodeDB);
 				this.doTreeSearch(actType, "Blocks");
 			}
 		} catch (Exception e) {
 			logger
 			.error("Error - openTreeToParentConcept : " + e.toString(),
 					e);
 		}
 	}
 
 	/**
 	 * @param searchID
 	 * @param sCCodeDB
 	 * @param sCodeName
 	 * @param dtsVocab
 	 */
 	private void doCallConceptSearch(String searchID, String sCCodeDB,
 			String sCodeName, String dtsVocab) {
 		try {
 			HttpSession session = (HttpSession) m_classReq.getSession();
 			Vector<EVS_Bean> vAC = new Vector<EVS_Bean>();
 			Vector vResult = new Vector();
 			String sSearchType = "";
 			m_classReq.setAttribute("UISearchType", "term");
 			String sSearchAC = (String) session.getAttribute("creSearchAC");
 			//first do cadsr search
 			if (!sSearchAC.equals("ParentConcept")
 					&& !sSearchAC.equals("ParentConceptVM")) {
 				if (sSearchAC.equals("ObjectClass"))
 					sSearchType = "OC";
 				else if (sSearchAC.equals("Property"))
 					sSearchType = "PROP";
 				else if (sSearchAC.equals("RepTerm"))
 					sSearchType = "REP";
 				else if (sSearchAC.equals("ObjectQualifier"))
 					sSearchType = "ObjQ";
 				else if (sSearchAC.equals("PropertyQualifier"))
 					sSearchType = "PropQ";
 				else if (sSearchAC.equals("RepQualifier"))
 					sSearchType = "RepQ";
 				GetACSearch serAC = new GetACSearch(m_classReq, m_classRes,
 						m_servlet);
 				if (searchID != null && !searchID.equals("")) {
 					if (sSearchAC.equals("ObjectClass")
 							|| sSearchAC.equals("Property")
 							|| sSearchAC.equals("RepTerm"))
 						serAC.do_caDSRSearch(searchID, "", "RELEASED", "", vAC,
 								sSearchType, "", "", "0");
 					vAC = serAC.do_ConceptSearch(searchID, "", "", "RELEASED",
 							"", "", "", vAC, "0");
 				}
 			}
 
 			//call vocab search without meta if one of the vocabs
 			Vector vVocabs = m_eUser.getVocabNameList();
 			if (vVocabs.contains(dtsVocab)) {
 				vAC = this.doVocabSearch(vAC, searchID, dtsVocab, "ConCode",
 						"", sSearchAC, "Include", "", 10, false, -1, "", new HashSet<String>());
 				m_classReq.setAttribute("UISearchType", "tree");
 			} else if (dtsVocab.equals(EVSSearch.META_VALUE)) // "MetaValue"))
 			{
 				vAC = this.doMetaSearch(vAC, searchID, "ConCode", "", 10,
 						sCCodeDB);
 				String vName = this.getMetaVocabName(sCCodeDB);
 				DataManager.setAttribute(session, "dtsVocab", vName);
 			}
 			//store it in the session  
 			DataManager.setAttribute(session, "creKeyword", searchID);
 			DataManager.setAttribute(session, "vACSearch", vAC);
 			this.get_Result(m_classReq, m_classRes, vResult, "");
 			DataManager.setAttribute(session, "results", vResult);
 		} catch (Exception e) {
 			logger.error("Error - doCallConceptSearch : " + e.toString(), e);
 		}
 	}
 
 	/**
 	 * @param actType
 	 * @param dtsVocab
 	 * @param sCodeName
 	 * @param sCode
 	 */
 	private void doCallTreeSearch(String actType, String dtsVocab,
 			String sCodeName, String sCode) {
 		try {
 			//do the tree search
 			HttpSession session = (HttpSession) m_classReq.getSession();
 			String strHTML = "";
 			EVSMasterTree tree = new EVSMasterTree(m_classReq, dtsVocab,
 					m_servlet);
 			if (actType.equals("OpenTreeToConcept")) {
 				Vector vStackVector = new Vector();
 				Vector vStackVector2 = new Vector();
 				strHTML = tree.populateTreeRoots(dtsVocab);
 				Stack stackSuperConcepts = new Stack();
 				HashMap<String, String> hSuperImmediate = this.getSuperConceptNamesImmediate(
 						dtsVocab, sCodeName, sCode);
 				
 				Vector vSuperImmediate = new Vector();
 				vSuperImmediate.addAll(hSuperImmediate.values());
 				DataManager.setAttribute(session, "vSuperImmediate",
 						vSuperImmediate);
 				vStackVector2 = tree.buildVectorOfSuperConceptStacks(
 						stackSuperConcepts, dtsVocab, sCode, vStackVector);
 				if (vStackVector.size() < 1) // must be a root concept
 				{
 					Tree RootTree = (Tree) tree.m_treesHash.get(dtsVocab);
 					if (RootTree != null)
 						strHTML = tree.renderHTML(RootTree);
 				} else {
 					Stack vStack = new Stack();
 					for (int j = 0; j < vStackVector.size(); j++) {
 						vStack = (Stack) vStackVector.elementAt(j);
 						if (vStack.size() > 0)
 							strHTML = tree.expandTreeToConcept(vStack,
 									dtsVocab, sCode);
 					}
 				}
 				DataManager.setAttribute(session, "vSuperImmediate", null);
 			}
 			if (actType.equals("OpenTreeToParentConcept")) {
 				DataManager.setAttribute(session, "ParentConceptCode", sCode);
 				DataManager.setAttribute(session, "ParentConcept", sCodeName);
 				this.doTreeSearchRequest(sCodeName, sCode, "false", dtsVocab);
 				m_classReq.setAttribute("UISearchType", "tree");
 				strHTML = tree.populateTreeRoots(dtsVocab);
 				strHTML = tree
 				.showParentConceptTree(sCode, dtsVocab, sCodeName);
 			}
 			DataManager.setAttribute(session, "strHTML", strHTML);
 		} catch (Exception e) {
 			logger.error("Error - doCallConceptSearch : " + e.toString(), e);
 		}
 	}
 
 	/**
 	 * to get the matching Thesaurus concept
 	 * @param eBean EVS Bean of the concept
 	 * @return return the EVS_Bean
 	 */
 	public EVS_Bean getThesaurusConcept(EVS_Bean eBean) {
 		try {
 			HttpSession session = m_classReq.getSession();
 			String conID = ""; // eBean.getCONCEPT_IDENTIFIER();
 			String conName = "";
 			String conType = ""; // eBean.getNCI_CC_TYPE();
 			String eDB = eBean.getEVS_DATABASE();
 			String metaName = m_eUser.getMetaDispName();
 			String nciVocab = this.getMetaVocabName(metaName);
 			String dtsVocab = eBean.getVocabAttr(m_eUser, eDB,
 					EVSSearch.VOCAB_DBORIGIN, EVSSearch.VOCAB_NAME); // "vocabDBOrigin", "vocabName");
 			Vector<EVS_Bean> vList = new Vector<EVS_Bean>();
 			EVS_Bean metaBean = new EVS_Bean();
 			//get preferred vocab name
 			String prefVocab = m_eUser.getPrefVocab();
 			if (prefVocab == null)
 				prefVocab = "";
 			if (dtsVocab.equals(prefVocab))
 				return eBean; //System.out.println(dtsVocab + " vocab match " + prefVocab);
 			//continue only if term is not from Thesaururs
 			if (dtsVocab != null && !dtsVocab.equals(nciVocab)) {
 				if (!dtsVocab.equals(EVSSearch.META_VALUE)) // "MetaValue"))
 				{
 					conType = eBean.getMETA_CODE_TYPE();
 					logger.debug(conType + " evs " + eBean.getMETA_CODE_VAL()
 							+ " con " + eBean.getLONG_NAME());
 					//check if can be searched by meta type properlty (UMLS or NCI_META)
 					if (conType != null && !conType.equals("")) {
 						conType = this.getNCIMetaCodeType(conType, "byKey");
 						conID = eBean.getMETA_CODE_VAL();
 						conName = eBean.getLONG_NAME();
 						vList = this.doVocabSearch(vList, conID, nciVocab,
 								"Name", conType, "", "Exclude", "", 10, false,
 								-1, "", new HashSet<String>());
 						if (vList != null && vList.size() > 0) {
 							eBean = this
 							.getNCIDefinition(vList, conID, conName); //get the right definition
 							return eBean;
 						}
 						conID = "";
 					}
 					if (conID == null || conID.equals("")) {
 						Hashtable vhash = m_eUser.getVocab_Attr();
 						if (vhash == null)
 							return eBean;
 						//get the vocab source
 						EVS_UserBean eUser = (EVS_UserBean) vhash.get(dtsVocab);
 						if (eUser == null)
 							return eBean;
 						String vSource = eUser.getVocabMetaSource();
 						if (vSource == null || vSource.equals(""))
 							return eBean;
 						//get its equivalent meta code
 						conID = eBean.getCONCEPT_IDENTIFIER();
 						vList = new Vector<EVS_Bean>();
 						vList = this.doMetaSearch(vList, conID, "MetaCode",
 								vSource, 10, metaName);
 						//no matching found in meta, return the original concept
 						if (vList == null || vList.size() < 1) {
 							return eBean;
 						}
 						metaBean = (EVS_Bean) vList.elementAt(0);
 						//reset the concept id to empty value
 						conID = "";
 					}
 				}
 				//get the thesaurus concept from nci source and code using atom property
 				String NCISrcCode = "";
 				vList = new Vector<EVS_Bean>();
 				//get the code from matched meta bean
 				if (metaBean != null && metaBean.getPREF_VOCAB_CODE() != null
 						&& !metaBean.getPREF_VOCAB_CODE().equals("")) {
 					NCISrcCode = metaBean.getPREF_VOCAB_CODE();
 					conName = metaBean.getCONCEPT_NAME();
 				}
 				//get the code from the searched metabean
 				else {
 					NCISrcCode = eBean.getPREF_VOCAB_CODE();
 					conName = eBean.getCONCEPT_NAME();
 				}
 				//call the search by concode method to get the Thes concept 
 				vList = new Vector<EVS_Bean>();
 				vList = this.doVocabSearch(vList, NCISrcCode, prefVocab,
 						"ConCode", "", "", "Exclude", "", 10, false, -1, "", new HashSet<String>());
 				if (vList != null && vList.size() > 0)
 					eBean = this.getNCIDefinition(vList, NCISrcCode, conName); // (EVS_Bean)vList.elementAt(0);        
 				else {
 					//call method to use cui property to search
 					if (conID == null || conID.equals(""))
 						conID = eBean.getCONCEPT_IDENTIFIER(); //go back to original id if not found
 					conName = eBean.getCONCEPT_NAME();
 					//now get the meta concept        
 					conType = this.getNCIMetaCodeType(conID, "byID");
 					vList = new Vector<EVS_Bean>();
 					vList = this.doVocabSearch(vList, conID, prefVocab, "Name",
 							conType, "", "Exclude", "", 10, false, -1, "", new HashSet<String>());
 					if (vList != null && vList.size() > 0)
 						eBean = this.getNCIDefinition(vList, conID, conName); // (EVS_Bean)vList.elementAt(0);   
 				}
 			}
 		} catch (Exception e) {
 			logger.error("Error - getThesaurusConcept : " + e.toString(), e);
 		}
 		return eBean;
 	}
 
 	/**
 	 * gets the nci definition and stores the message if occured
 	 * @param vList vector object
 	 * @param sID String concept id
 	 * @param sName String concept name
 	 * @return EVS_Bean object matched
 	 */
 	private EVS_Bean getNCIDefinition(Vector vList, String sID, String sName) {
 		EVS_Bean eBean = getDefinition(vList);
 		//update the status message
 		m_servlet.storeStatusMsg("The selected Concept [" + sName + " : " + sID
 				+ "] will be replaced by the matching NCI Thesaurus Concept ["
 				+ eBean.getCONCEPT_NAME() + " : "
 				+ eBean.getCONCEPT_IDENTIFIER() + "]");
 
 		return eBean; //return teh bean
 	}
 
 	/**
 	 * loops through the list to get the list of concepts and calls method to get the definition
 	 * @param selList Vector of EVS_Bean ojbect
 	 * @param firstRow int begining number of the list
 	 * @param lastRow int ending number of the list
 	 * @return EVS_Bean object
 	 */
 	private EVS_Bean getNCIPrefDefinition(Vector<EVS_Bean> selList,
 			int firstRow, int lastRow) {
 		EVS_Bean eBean = new EVS_Bean();
 		if (firstRow == lastRow - 1)
 			eBean = selList.elementAt(firstRow);
 		else if (firstRow < lastRow) {
 			Vector<EVS_Bean> vList = new Vector<EVS_Bean>();
 			for (int i = firstRow; i < lastRow; i++) {
 				vList.addElement(selList.elementAt(i));
 				if (i == lastRow)
 					break;
 			}
 			eBean = getDefinition(vList);
 		} else
 			logger.error(eBean.getLONG_NAME() + " : " + firstRow + " & "
 					+ lastRow + " : " + eBean.getCONCEPT_NAME());
 		return eBean; //return the bean
 	}
 
 	/**
 	 * gets the nci preferred concepts for the duplicate concepts selected on the search results for pv
 	 * @param vList Vector<EVS_Bean> object of the checked concepts
 	 * @return Vector<EVS_Bean> object of the nci selected concepts
 	 */
 	public Vector<EVS_Bean> getNCIPrefConcepts(Vector<EVS_Bean> vList) {
 		Vector<EVS_Bean> selList = new Vector<EVS_Bean>();
 		//sort the selected rows by name
 		GetACSearch serAC = new GetACSearch(m_classReq, m_classRes, m_servlet);
 		vList = serAC.sortConcepts(vList, "name");
 
 		EVS_Bean eBean = vList.elementAt(0);
 		String firstName = eBean.getLONG_NAME(); //.getCONCEPT_NAME();  //first row name
 		int firstRec = 0; //first rec
 		int lastRec = 1; //last rec
 		//loop through the vlist to find duplicates
 		for (int i = 1; i < vList.size(); i++) {
 			EVS_Bean nBean = vList.elementAt(i);
 			String nextName = nBean.getLONG_NAME(); //.getCONCEPT_NAME();     //next row name
 			lastRec = i; //next last rec to be the current if new group
 
 			if (!firstName.equalsIgnoreCase(nextName)) //if firstname equal nextname
 			{
 				//get the nci pref concept if duplicate exist or get the one record into the sublist.
 				EVS_Bean evsbean = getNCIPrefDefinition(vList, firstRec,
 						lastRec);
 				selList.addElement(evsbean); //add evsbean to vector
 				firstName = nextName; //firstname = this row name;
 				firstRec = i;
 				if (i == vList.size() - 1) //store the last record
 					lastRec = vList.size();
 			}
 		}
 		//add teh last row in the selection
 		eBean = getNCIPrefDefinition(vList, firstRec, lastRec);
 		selList.addElement(eBean); //add evsbean to vector
 
 		//return the selected rows vector
 		return selList;
 	}
 
 	/**
 	 * gets the nci preferred concepts from the definition source order
 	 * @param vList Vector<EVS_Bean> object
 	 * @return EVS_Bean object of the concept
 	 */
 	private EVS_Bean getDefinition(Vector vList) {
 		//logger.debug("get nci def " + vList.size());
 		EVS_Bean eBean = (EVS_Bean) vList.elementAt(0);
 		Vector vNCIsrc = m_eUser.getNCIDefSrcList();
 		if (vNCIsrc == null)
 			vNCIsrc = new Vector();
 		boolean isDefMatched = false;
 		//loop through list of nci sources to get the right order
 		for (int i = 0; i < vNCIsrc.size(); i++) {
 			String srcNCI = (String) vNCIsrc.elementAt(i);
 			//loop through list of def sources for the concept
 			for (int k = 0; k < vList.size(); k++) {
 				EVS_Bean thisBean = (EVS_Bean) vList.elementAt(k);
 				String sSrc = thisBean.getEVS_DEF_SOURCE();
 				//match def source order to the bean
 				if (sSrc.equalsIgnoreCase(srcNCI)) {
 					isDefMatched = true;
 					eBean = (EVS_Bean) vList.elementAt(k); //to return the def matched bean
 					break;
 				}
 			}
 			if (isDefMatched)
 				break; //no need to continue if found the right def source
 		}
 		return eBean; //return teh bean
 	}
 	/**
 	 * adds the security token to the query for some vocabularies
 	 * @param query
 	 * @param vocabAccess
 	 * @param vocab
 	 * @return evsquery
 	 * @throws Exception 
 	 */
 
 
 	public static LexEVSApplicationService registerSecurityToken(LexEVSApplicationService lexevsService, String codingScheme, EVS_UserBean userBean) throws Exception {
 		
 		String token = "";
 		Hashtable ht = userBean.getVocab_Attr();
 		if(ht == null) {
             throw new Exception("Not able to register security token (The vocabulary returns NULL for the coding schema [" + codingScheme + "]).");
 		}
 		EVS_UserBean eu = (EVS_UserBean) ht.get(codingScheme);
 		token = eu.getVocabAccess();
 		SecurityToken securityToken = new SecurityToken();
 		securityToken.setAccessToken(token);
 		Boolean retval = null;
 		try {
 			retval = lexevsService.registerSecurityToken(codingScheme, securityToken);
 			if(retval != null && retval.equals(Boolean.TRUE)) {
 				System.out.println("Registration of SecurityToken was successful.");
 			}
 			else {
 				System.out.println("WARNING: Registration of SecurityToken failed.");
 			}
 		} catch (Exception e) {
 			System.out.println("WARNING: Registration of SecurityToken failed.");
 		}
 		return lexevsService;
 	}
 
 
 	public Vector<EVS_Bean> getThesaurusConceptBean(Vector vEvsBean){
 		Vector<EVS_Bean>  vEvsBeann = new Vector<EVS_Bean>();
 		if (vEvsBean != null){  
 			for (int i = 0; i < vEvsBean.size(); i++) {
 				EVS_Bean eBean = (EVS_Bean) vEvsBean.elementAt(i);
 				eBean = this.getThesaurusConcept(eBean);
 				vEvsBeann.addElement(eBean);
 			}
 		}
 		return vEvsBeann;
 	}
 
 	private HashMap<String,ResolvedConceptReference> returnSubConcepts(String code, String scheme) throws LBException {
         HashMap<String, ResolvedConceptReference> ret = new HashMap<String, ResolvedConceptReference>();
         
         CodingScheme cs = evsService.resolveCodingScheme(scheme, null);
         boolean forwardNavigable = cs.getMappings().getSupportedHierarchy()[0].isIsForwardNavigable();
 		String relation = returnAssociations(cs);
 		
 		// Perform the query ...
         NameAndValue nv = new NameAndValue();
         NameAndValueList nvList = new NameAndValueList();
         nv.setName(relation);
         nvList.addNameAndValue(nv);
  
         ResolvedConceptReferenceList matches = evsService.getNodeGraph(scheme, null, null).restrictToAssociations(nvList,
                 null).resolveAsList(ConvenienceMethods.createConceptReference(code, scheme), forwardNavigable, !forwardNavigable, 1, 1,
                 new LocalNameList(), null, null, 1024);
  
         // Analyze the result ...
         ret = getAssociatedConcepts(matches, true);
         return ret;
     }
 
 	private HashMap<String,ResolvedConceptReference> returnSuperConcepts(String code, String scheme) throws LBException {
         HashMap<String, ResolvedConceptReference> ret = new HashMap<String, ResolvedConceptReference>();
         
         CodingScheme cs = evsService.resolveCodingScheme(scheme, null);
         boolean forwardNavigable = cs.getMappings().getSupportedHierarchy()[0].isIsForwardNavigable();
 		String relation = returnAssociations(cs);
         
 		// Perform the query ...
         NameAndValue nv = new NameAndValue();
         NameAndValueList nvList = new NameAndValueList();
         nv.setName(relation);
         nvList.addNameAndValue(nv);
  
         ResolvedConceptReferenceList matches = evsService.getNodeGraph(scheme, null, null).restrictToAssociations(nvList,
                 null).resolveAsList(ConvenienceMethods.createConceptReference(code, scheme), !forwardNavigable, forwardNavigable, 1, 1,
                 new LocalNameList(), null, null, 1024);
  
         // Analyze the result ...
         ret = getAssociatedConcepts(matches, true);
         return ret;
     }
 	
 	private HashMap<String,String> returnSubConceptNames(String code, String scheme) throws LBException {
         HashMap<String, String> ret = new HashMap<String, String>();
         
         CodingScheme cs = evsService.resolveCodingScheme(scheme, null);
         boolean forwardNavigable = cs.getMappings().getSupportedHierarchy()[0].isIsForwardNavigable();
 		String relation = returnAssociations(cs);
         
 		// Perform the query ...
         NameAndValue nv = new NameAndValue();
         NameAndValueList nvList = new NameAndValueList();
         nv.setName(relation);
         nvList.addNameAndValue(nv);
  
         ResolvedConceptReferenceList matches = evsService.getNodeGraph(scheme, null, null).restrictToAssociations(nvList,
                 null).resolveAsList(ConvenienceMethods.createConceptReference(code, scheme), forwardNavigable, !forwardNavigable, 1, 1,
                 new LocalNameList(), new PropertyType[]{PropertyType.PRESENTATION},null, 1024);
  
         // Analyze the result ...
         ret = getAssociatedConcepts(matches, false);
         return ret;
     }
 	
 	private HashMap<String,String> returnSuperConceptNames(String code, String scheme) throws LBException {
         HashMap<String, String> ret = new HashMap<String, String>();
         
         
         CodingScheme cs = evsService.resolveCodingScheme(scheme, null);
         boolean forwardNavigable = cs.getMappings().getSupportedHierarchy()[0].isIsForwardNavigable();
 		String relation = returnAssociations(cs);
 		
 		
 		// Perform the query ...
         NameAndValue nv = new NameAndValue();
         NameAndValueList nvList = new NameAndValueList();
         nv.setName(relation);
         nvList.addNameAndValue(nv);
  
         ResolvedConceptReferenceList matches = evsService.getNodeGraph(scheme, null, null).restrictToAssociations(nvList,
                 null).resolveAsList(ConvenienceMethods.createConceptReference(code, scheme), !forwardNavigable, forwardNavigable, 1, 1,
                 new LocalNameList(), new PropertyType[]{PropertyType.PRESENTATION},null, 1024);
  
         // Analyze the result ...
         ret = getAssociatedConcepts(matches, false);
         return ret;
     }
 	
 	private String returnAssociations(CodingScheme cs) throws LBException {
 			
 		String ret = new String();
 		
 		Mappings mappings = cs.getMappings();
 		SupportedHierarchy[] hierarchies = mappings.getSupportedHierarchy();
 		SupportedHierarchy hierarchyDefn = hierarchies[0];
 		String[] associationsToNavigate = hierarchyDefn.getAssociationNames();// associations
 		
 		for (String assn:associationsToNavigate ) 
 		{
 			if (assn.equals("subClassOf")){
 				ret = assn;
 				//we prefer this association
 				break;
 			}
 			if (assn.equals("is_a")) {
 				ret = assn;
 				break;
 			}
 			if (ret.length() == 0 && hierarchyDefn.getLocalId().equals("is_a"))
 				ret = assn;
 			
 		}
 			
 		return ret;
 
 	}
 	
 	private HashMap getAssociatedConcepts(ResolvedConceptReferenceList matches, boolean resolveConcepts) {
 		HashMap ret = new HashMap();
         
 		
 		if (matches.getResolvedConceptReferenceCount() > 0) {
             ResolvedConceptReference ref = (ResolvedConceptReference) matches.enumerateResolvedConceptReference()
                     .nextElement();
  
             // Print the associations
             AssociationList targetof = ref.getTargetOf();
             if (targetof != null) {
 	            Association[] associations = targetof.getAssociation();
 	            for (int i = 0; i < associations.length; i++) {
 	                Association assoc = associations[i];
 	                if (assoc!= null && assoc.getAssociatedConcepts() != null &&
 	                		assoc.getAssociatedConcepts().getAssociatedConcept() != null) {	//blank screen due to NPE on superconcept
 		                AssociatedConcept[] acl = assoc.getAssociatedConcepts().getAssociatedConcept();
 		                for (int j = 0; j < acl.length; j++) {
 		                    AssociatedConcept ac = acl[j];
 		                    if (resolveConcepts)
 		                    	ret.put(ac.getCode(), ac);
 		                    else
 		                    	ret.put(ac.getCode(), ac.getEntityDescription().getContent());
 		                }
 	                }
 	            }
             } else {
             
             	AssociationList sourceOf = ref.getSourceOf();
             
 	            if (sourceOf != null) {
 		            Association[] associations = sourceOf.getAssociation();
 		            for (int i = 0; i < associations.length; i++) {
 		                Association assoc = associations[i];
 		                if (assoc!= null && assoc.getAssociatedConcepts() != null &&
 		                		assoc.getAssociatedConcepts().getAssociatedConcept() != null) {	//blank screen due to NPE on superconcept
 			                AssociatedConcept[] acl = assoc.getAssociatedConcepts().getAssociatedConcept();
 			                for (int j = 0; j < acl.length; j++) {
 			                    AssociatedConcept ac = acl[j];
 			                    if (resolveConcepts)
 			                    	ret.put(ac.getCode(), ac);
 			                    else
 			                    	ret.put(ac.getCode(), ac.getEntityDescription().getContent());
 			                }
 		                }
 		            }    
 	            }
             }
         }
 		
 		return ret;
 	}
 	
 	private static ResolvedConceptReferenceList searchPrefTerm(LexEVSApplicationService evsService, String dtsVocab, String prefName, int sMetaLimit, String algorithm, String designation) {
 
 
 		ResolvedConceptReferenceList concepts = new ResolvedConceptReferenceList();
 		int codesSize = 0;
 		try {
 			CodedNodeSet metaNodes = evsService.getNodeSet(dtsVocab, null, null); 
 
 			metaNodes = metaNodes.restrictToMatchingDesignations(prefName, //the text to match 
 					CodedNodeSet.SearchDesignationOption.ALL,  //whether to search all designation, only Preferred or only Non-Preferred
 					algorithm, //the match algorithm to use
 					null); //the language to match (null matches all)
 
 			metaNodes = metaNodes.restrictToStatus(ActiveOption.ACTIVE_ONLY, null);
 			
 			concepts = metaNodes.resolveToList(
 					null, //Sorts used to sort results (null means sort by match score)
 					null, //PropertyNames to resolve (null resolves all)
 					new CodedNodeSet.PropertyType[] {PropertyType.DEFINITION, PropertyType.PRESENTATION},  //PropertyTypess to resolve (null resolves all)
 					sMetaLimit	  //cap the number of results returned (-1 resolves all)
 			);
 			codesSize = concepts.getResolvedConceptReferenceCount();
 		} catch (Exception ex) {
 			//ex.printStackTrace();
 			System.out.println("Error searchPrefTerm: " + 
 					ex.toString());
 		}
 
 		return concepts;
 	}
 	
 	private String getAlgorithm(String termStr) {
 		
 		String algorithm = "exactMatch";
 		
 		boolean starts = false;
 		boolean ends = false;
 		boolean contains = false;
 		boolean multiple = false;
 		boolean embed = false;
 		
 		termStr = termStr.trim();
 		ends = termStr.startsWith("*"); // Term ends with rest of the string
 		starts = termStr.endsWith("*"); // Term starts with rest of the string
 		
 		contains = termStr.substring(1,termStr.length()-1).indexOf(" *") >= 0 ||
 					termStr.substring(1,termStr.length()-1).indexOf("* ") >= 0;
 		if (!contains)		
 			embed = termStr.substring(1,termStr.length()-1).indexOf("*") >= 0;
 		
 		multiple = termStr.indexOf(' ') > 0;
 		
 		
 		if (starts)
 			algorithm = "startsWith";
 		if (contains || ends)
 			algorithm = "contains";
 		if (multiple && starts && ends)
 			algorithm = "nonLeadingWildcardLiteralSubString";
 		if (multiple && starts && ends && contains)
 			algorithm = "contains";
 		
 		return algorithm;
 	}
 	
 	private String cleanTerm(String termStr) {		
 		termStr = termStr.trim();
 		termStr = termStr.replace("*","");
 		return termStr;
 	}
 	
 	//close the class
 }
