 // Copyright (c) 2002 ScenPro, Inc.
 
 // $Header: /cvsshare/content/cvsroot/cdecurate/src/gov/nih/nci/cadsr/cdecurate/tool/EVS_UserBean.java,v 1.53 2008-12-26 19:13:24 chickerura Exp $
 // $Name: not supported by cvs2svn $
 
 package gov.nih.nci.cadsr.cdecurate.tool;
 
 //import gov.nih.nci.evs.domain.Source;
 /*import gov.nih.nci.evs.query.EVSQuery;
 import gov.nih.nci.evs.query.EVSQueryImpl;
 import gov.nih.nci.system.applicationservice.EVSApplicationService;*/
 import gov.nih.nci.system.client.ApplicationServiceProvider;
 
 import java.io.Serializable;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.LexGrid.LexBIG.DataModel.Collections.CodingSchemeRenderingList;
 import org.LexGrid.LexBIG.DataModel.Core.CodingSchemeSummary;
 import org.LexGrid.LexBIG.DataModel.InterfaceElements.CodingSchemeRendering;
 import org.LexGrid.LexBIG.LexBIGService.LexBIGService;
 import org.apache.log4j.Logger;
 
 /**
  * The UserBean encapsulates the EVS information from caDSR database and will be stored in the
  * session after the user has logged on.
  * <P>
  * @author Sumana
  * @version 3.0
  */
 
  /*
 The CaCORE Software License, Version 3.0 Copyright 2002-2005 ScenPro, Inc. ("ScenPro")
 Copyright Notice.  The software subject to this notice and license includes both
 human readable source code form and machine readable, binary, object code form
 ("the CaCORE Software").  The CaCORE Software was developed in conjunction with
 the National Cancer Institute ("NCI") by NCI employees and employees of SCENPRO.
 To the extent government employees are authors, any rights in such works shall
 be subject to Title 17 of the United States Code, section 105.
 This CaCORE Software License (the "License") is between NCI and You.  "You (or "Your")
 shall mean a person or an entity, and all other entities that control, are
 controlled by, or are under common control with the entity.  "Control" for purposes
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
 include the following acknowledgment: "This product includes software developed
 by SCENPRO and the National Cancer Institute."  If You do not include such end-user
 documentation, You shall include this acknowledgment in the Software itself,
 wherever such third-party acknowledgments normally appear.
 3.	You may not use the names "The National Cancer Institute", "NCI" "ScenPro, Inc."
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
 
 public final class EVS_UserBean implements Serializable
 {
   /**
    *
    */
   private static final long serialVersionUID = 1L;
   // Attributes
   private String m_evsConURL;   //connection string
   private Vector m_vocabNameList;  //index name of the vocab that are not displayed
   private Vector m_vocabDisplayList;  //drop down list of names
   private String m_vocabName;  //vocab name used to query cacore api
   private String m_vocabDisplay;  //vocab name displayed in the jsp
   private String m_vocabDBOrigin;  //vocab name stored in cadsr table as origin and vocab name in the search results
   private String m_vocabMetaSource;  //Meta source to the specific vocabulary
   private boolean m_vocabUseParent;  //true or false value to mark it to be used for parent search
   private String m_SearchInName;  //display term for search in of name option
   private String m_SearchInConCode;  //display term for search in of Concept Code option
   private String m_SearchInMetaCode;  //display term for search in of MetaCode option
   private String m_NameType;  //Vocab type to search concept name
   private String m_PropName;  //evs property for concept name attribute
   private String m_PropNameDisp;  //evs property for concept name attribute
   private String m_PropDefinition;  //evs property for definition attribute
   private String m_PropHDSyn;  //evs property for header concept attribute
   private String m_PropRetCon;  //evs property for retired concept property
   private String m_PropSemantic;  //evs property for Symantic Type property
   private String m_retSearch;  //retired option for search filter
   private String m_treeSearch;  //tree display option for search filter
   private String m_includeMeta;  //retired option for search filter
   private String m_codeType;   //code types specific to each vocab
   private String m_defDefaultValue;   //definition default value if value doesn't exists
   private String m_metaDispName;   //meta display name used for commapring
   private String m_dsrDispName;   //dsr database display name used for commapring
   private Vector m_NCIDefSrcList;  //list of definition sources for NCI in the priority order
   private Hashtable m_metaCodeType;  //code type for meta thesaurus with filter value
   private Hashtable m_vocab_attr;   //attributes specific to vocabs
   private String PrefVocabSrc;   //source of the preferred vocabulary
   private String PrefVocab;   //name of the preferred vocabulary
   private String _vocabAccess;
 
   /**
    * initialize the logger for the class
    */
   Logger logger = Logger.getLogger(EVS_UserBean.class.getName());
 
   /**
    * Constructor
    */
   public EVS_UserBean()
   {
   }
 
   /**
    * The getEVSConURL method returns the evs connection string  for this bean.
    *
    * @return String The connection string
    */
   public String getEVSConURL()
   {
     return m_evsConURL;
   }
 
   /**
    * The setEVSConURL method sets the evs connection string for this bean.
    *
    * @param evsURL The connection string to set
    */
   public void setEVSConURL(String evsURL)
   {
     m_evsConURL = evsURL;
   }
 
   /**
    * gets the list of vocabs from the bean
    *
    * @return m_vocabNameList list of vocabulary names
    */
   public Vector getVocabNameList()
   {
     return m_vocabNameList;
   }
 
   /**
    * this sets the list of vocab names into the bean stored in database
    *
    * @param vName list of vocab names from the database
    */
   public void setVocabNameList(Vector vName)
   {
     m_vocabNameList = vName;
   }
 
   /**
    * gets the list of vocabs from the bean
    *
    * @return m_vocabDisplayList list of vocabulary Display names
    */
   public Vector getVocabDisplayList()
   {
     return m_vocabDisplayList;
   }
 
   /**
    * this sets the list of vocab Display names into the bean stored in database
    *
    * @param vDisplay list of vocab Display names from the database
    */
   public void setVocabDisplayList(Vector vDisplay)
   {
     m_vocabDisplayList = vDisplay;
   }
 
  /**
    * The getVocabName method returns the name of the vocab used for quering the cacore api.
    *
    * @return String The name of the vocab
    */
   public String getVocabName()
   {
     return m_vocabName;
   }
 
   /**
    * The setVocabName method sets the name of the vocab for querying the cacore api.
    *
    * @param sName The name of the vocab
    */
   public void setVocabName(String sName)
   {
     m_vocabName = sName;
   }
 
  /**
    * The getVocabDisplay method returns the Display of the vocab used for quering the cacore api.
    *
    * @return String The Display of the vocab
    */
   public String getVocabDisplay()
   {
     return m_vocabDisplay;
   }
 
   /**
    * The setVocabDisplay method sets the Display of the vocab for querying the cacore api.
    *
    * @param sDisplay The Display of the vocab
    */
   public void setVocabDisplay(String sDisplay)
   {
     m_vocabDisplay = sDisplay;
   }
 
  /**
    * The getVocabDBOrigin method returns the DBOrigin of the vocab used for quering the cacore api.
    *
    * @return String The DBOrigin of the vocab
    */
   public String getVocabDBOrigin()
   {
     return m_vocabDBOrigin;
   }
 
   /**
    * The setVocabDBOrigin method sets the DBOrigin of the vocab for querying the cacore api.
    *
    * @param sDBOrigin The DBOrigin of the vocab
    */
   public void setVocabDBOrigin(String sDBOrigin)
   {
     m_vocabDBOrigin = sDBOrigin;
   }
 
  /**
    * The getVocabMetaSource method returns the MetaSource of the vocab used for quering the cacore api.
    *
    * @return String The MetaSource of the vocab
    */
   public String getVocabMetaSource()
   {
     return m_vocabMetaSource;
   }
 
   /**
    * The setVocabMetaSource method sets the MetaSource of the vocab for querying the cacore api.
    *
    * @param sMetaSource The MetaSource of the vocab
    */
   public void setVocabMetaSource(String sMetaSource)
   {
     m_vocabMetaSource = sMetaSource;
   }
 
  /**
    * The getVocabUseParent method returns the the true or false value to use as vocab parent.
    *
    * @return boolean true or false value The parent use of the vocab
    */
   public boolean getVocabUseParent()
   {
     return m_vocabUseParent;
   }
 
   /**
    * The setVocabUseParent method sets the true or false value to use as vocab parent.
    *
    * @param bUseParent The True or False value to use the vocab as parent
    */
   public void setVocabUseParent(boolean bUseParent)
   {
     m_vocabUseParent = bUseParent;
   }
 
   /**
    * gets the display name for the Name option of evs searchin
    *
    * @return m_SearchInName display name option of evs searchin
    */
   public String getSearchInName()
   {
     return m_SearchInName;
   }
 
   /**
    * this sets the display name for the Name option of evs searchin into the bean stored in database
    *
    * @param sData the display name for the Name option of evs searchin from the database
    */
   public void setSearchInName(String sData)
   {
     m_SearchInName = sData;
   }
 
   /**
    * gets the display name for the Concept Code option of evs searchin
    *
    * @return m_SearchInName display Concept Code option of evs searchin
    */
   public String getSearchInConCode()
   {
     return m_SearchInConCode;
   }
 
   /**
    * this sets the display name for the Concept Code option of evs searchin into the bean stored in database
    *
    * @param sData the display name for the Concept Code option of evs searchin from the database
    */
   public void setSearchInConCode(String sData)
   {
     m_SearchInConCode = sData;
   }
 
   /**
    * gets the display name for the Meta Code option of evs searchin
    *
    * @return m_SearchInName display Meta Code option of evs searchin
    */
   public String getSearchInMetaCode()
   {
     return m_SearchInMetaCode;
   }
 
   /**
    * this sets the display name for the Meta Code option of evs searchin into the bean stored in database
    *
    * @param sData the display name for the Meta Code option of evs searchin from the database
    */
   public void setSearchInMetaCode(String sData)
   {
     m_SearchInMetaCode = sData;
   }
 
   /**
    * The getNameType method returns the Type for name search in for the vocab for this bean.
    *
    * @return String the Type for name search in for the vocab
    */
   public String getNameType()
   {
     return m_NameType;
   }
 
   /**
    * The setType method sets the Type for name search in for the vocab for this bean.
    *
    * @param sNType the sNType for name search in for the vocab
    */
   public void setNameType(String sNType)
   {
     m_NameType = sNType;
   }
 
   /**
    * The getPropName method returns the concept property string for concept for this bean.
    *
    * @return String The property string for concept name
    */
   public String getPropName()
   {
     return m_PropName;
   }
 
   /**
    * The setPropName method sets the concept property string used to search concept name for this bean.
    *
    * @param sName The property string for concept name to set
    */
   public void setPropName(String sName)
   {
     m_PropName = sName;
   }
 
   /**
    * The getPropNameDisp method returns the concept name disp property string for concept for this bean.
    *
    * @return String The property string for concept name disp
    */
   public String getPropNameDisp()
   {
     return m_PropNameDisp;
   }
 
   /**
    * The setPropNameDisp method sets the concept property string used to search concept name for this bean.
    *
    * @param sName The property string for concept name display to set
    */
   public void setPropNameDisp(String sName)
   {
     m_PropNameDisp = sName;
   }
 
   /**
    * The getPropDefinition method returns the concept property string for concept for this bean.
    *
    * @return String The property string for concept Definition
    */
   public String getPropDefinition()
   {
     return m_PropDefinition;
   }
 
   /**
    * The setPropDefinition method sets the concept property string used to search concept Definition for this bean.
    *
    * @param sDefinition The property string for concept Definition to set
    */
   public void setPropDefinition(String sDefinition)
   {
     m_PropDefinition = sDefinition;
   }
 
   /**
    * The getPropHDSyn method returns the concept property string for concept for this bean.
    *
    * @return String The property string for concept header concept
    */
   public String getPropHDSyn()
   {
     return m_PropHDSyn;
   }
 
   /**
    * The setPropHDSyn method sets the concept property string used to search concept header concept for this bean.
    *
    * @param sHDSyn The property string for concept header concept to set
    */
   public void setPropHDSyn(String sHDSyn)
   {
     m_PropHDSyn = sHDSyn;
   }
 
   /**
    * The getPropRetCon method returns the concept property string for concept for this bean.
    *
    * @return String The property string for retired concept
    */
   public String getPropRetCon()
   {
     return m_PropRetCon;
   }
 
   /**
    * The setPropRetCon method sets the concept property string used to search retired concept for this bean.
    *
    * @param sRetCon The property string for retired concept to set
    */
   public void setPropRetCon(String sRetCon)
   {
     m_PropRetCon = sRetCon;
   }
 
   /**
    * The getPropSemantic method returns the concept property string for concept for this bean.
    *
    * @return String The property string for concept Semantic Type
    */
   public String getPropSemantic()
   {
     return m_PropSemantic;
   }
 
   /**
    * The setPropSemantic method sets the concept property string used to search concept Semantic Type for this bean.
    *
    * @param sSemantic The property string for concept Semantic Type to set
    */
   public void setPropSemantic(String sSemantic)
   {
     m_PropSemantic = sSemantic;
   }
 
   /**
    * The getRetSearch method returns the RetSearch status for this bean.
    *
    * @return String Whether this vocab is to display is a RetSearch or not
    */
   public String getRetSearch()
   {
     return m_retSearch;
   }
 
   /**
    * The setRetSearch method sets the RetSearch status for this bean.
    *
    * @param isRetSearch The RetSearch option for the vocabulary for JSP
    */
   public void setRetSearch(String isRetSearch)
   {
     m_retSearch = isRetSearch;
   }
 
   /**
    * @return Returns the m_treeSearch.
    */
   public String getTreeSearch()
   {
     return m_treeSearch;
   }
 
   /**
    * @param search The m_treeSearch to set.
    */
   public void setTreeSearch(String search)
   {
     m_treeSearch = search;
   }
 
 
   /**
    * The getIncludeMeta method returns the IncludeMeta vocabulary name for this bean.
    *
    * @return String Whether this vocab is associated with another vocab like Meta thesarus
    */
   public String getIncludeMeta()
   {
     return (m_includeMeta == null) ? "" : m_includeMeta;
   }
 
   /**
    * The setIncludeMeta method sets the IncludeMeta vocabulary name for this bean.
    *
    * @param sMetaName The Meta vocab name associated with another vocab
    */
   public void setIncludeMeta(String sMetaName)
   {
     m_includeMeta = sMetaName;
   }
 
   /**
    * The getCode_Type method returns the concept code type (altname type or evs source) specific to the vocabulary.
    *
    * @return String m_codeType is a string
    */
   public String getVocabCodeType()
   {
     return m_codeType;
   }
 
   /**
    * stores the vocab code type in the bean
    *
    * @param sType evs source type or altname type of the vocabulary
    */
   public void setVocabCodeType(String sType)
   {
     m_codeType = sType;
   }
 
   /**
    * @return boolean to mark web access
    */
   public boolean vocabIsSecure()
   {
       return _vocabAccess != null;
   }
 
   /**
    * @return string code of we access
    */
   public String getVocabAccess()
   {
       return (_vocabAccess == null) ? "" :_vocabAccess;
   }
 
   /**
    * @param code_ string code of we access
    */
   public void setVocabAccess(String code_)
   {
       _vocabAccess = code_;
   }
 
   /**
    * The getDefDefaultValue method returns the default definition value.
    *
    * @return String m_defDefaultValue is a string
    */
   public String getDefDefaultValue()
   {
     return m_defDefaultValue;
   }
 
   /**
    * stores the default value for the defiinition used if definition from api is empty
    *
    * @param sDef default definition
    */
   public void setDefDefaultValue(String sDef)
   {
     m_defDefaultValue = sDef;
   }
 
   /**
    * The getMetaDispName method returns the meta thesaurs name display.
    *
    * @return String m_metaDispName is a string
    */
   public String getMetaDispName()
   {
     return m_metaDispName;
   }
 
   /**
    * stores the meta name for display
    *
    * @param sName meta name
    */
   public void setMetaDispName(String sName)
   {
     m_metaDispName = sName;
   }
 
   /**
    * The getDSRDispName method returns the DSR name display.
    *
    * @return String m_dsrDispName is a string
    */
   public String getDSRDispName()
   {
     return m_dsrDispName;
   }
 
   /**
    * stores the DSR name for display
    *
    * @param sName DSR name
    */
   public void setDSRDispName(String sName)
   {
     m_dsrDispName = sName;
   }
 
   /**
    * gets the list of NCI definition sources to filter out
    *
    * @return m_NCIDefSrcList list of defintion sources
    */
   public Vector getNCIDefSrcList()
   {
     return m_NCIDefSrcList;
   }
 
   /**
    * this sets the list of NCI definition sources to filter out
    *
    * @param vName list of NCI definition sources
    */
   public void setNCIDefSrcList(Vector vName)
   {
     m_NCIDefSrcList = vName;
   }
 
   /**
    * The getVocab_Attr method returns the attributes specific to the vocabulary.
    *
    * @return Hashtable m_vocab_attr is a hash table with
    */
   public Hashtable getVocab_Attr()
   {
     return m_vocab_attr;
   }
   /**
    * stores the vocab specific attributes in the hash table
    *
    * @param vocAttr hashtable with vocab name and user bean as objects
    */
   public void setVocab_Attr(Hashtable vocAttr)
   {
     m_vocab_attr = vocAttr;
   }
 
   /**
    * The getMetaCodeType method returns code type for the meta thesaurus.
    *
    * @return Hashtable m_metaCodeType is a hash table with code type and filter value
    */
   public Hashtable getMetaCodeType()
   {
     return m_metaCodeType;
   }
   /**
    * stores the vocab specific attributes in the hash table
    *
    * @param metaType hashtable with vocab name and user bean as objects
    */
   public void setMetaCodeType(Hashtable metaType)
   {
     m_metaCodeType = metaType;
   }
 
   /**
    * @return Returns the prefVocabSrc.
    */
   public String getPrefVocabSrc()
   {
     return PrefVocabSrc;
   }
 
   /**
    * @param prefVocabSrc The prefVocabSrc to set.
    */
   public void setPrefVocabSrc(String prefVocabSrc)
   {
     PrefVocabSrc = prefVocabSrc;
   }
 
   /**
    * @return Returns the prefVocab.
    */
   public String getPrefVocab()
   {
     return PrefVocab;
   }
 
   /**
    * @param prefVocab The prefVocab to set.
    */
   public void setPrefVocab(String prefVocab)
   {
     PrefVocab = prefVocab;
   }
 
 
 /*  /**
    * The setvocab_attr method sets the vocab searchin, concept name property and concept Definition property specific to the vocab.
    * key is vocab_attr and values are stored as objects. if attr doesn't exist for the selected vocab, go get the  attr.
    *
    * @param vocabName name of the vocabulary
    * @param vSearchIn vector of searchin for the vocabulary
    * @param conProp string concept name property
    * @param defProp string definition property
    */
 /*  public void setVocab_Attr(String vocabName, Vector vSearchIn, String conProp, String conPropDisp,
     String defProp, String retConProp, String semProp, String hdSynProp,
     String retSearch, String sMeta, String vocabType)
   {
     m_vocab_attr = new Hashtable();
     //try storing vocabName"_SearchIn" as key and vSearchIn as value
     if (vSearchIn != null && vSearchIn.size() > 0)
       m_vocab_attr.put(vocabName + "_SearchIn", vSearchIn);
     //and vocabName"_PropNameIn" as key and conProp as value
     if (conProp != null && !conProp.equals(""))
       m_vocab_attr.put(vocabName + "_PropNameIN", conProp);
     //and vocabName"_PropNameDisp" as key and conProp as value
     if (conPropDisp != null && !conPropDisp.equals(""))
       m_vocab_attr.put(vocabName + "_PropNameDisp", conPropDisp);
     //and vocabName"_PropDefinition" as key and defProp as value
     if (defProp != null && !defProp.equals(""))
       m_vocab_attr.put(vocabName + "_PropDefinition", defProp);
     //and vocabName"_PropRetCon" as key and retConProp as value
     if (retConProp != null && !retConProp.equals(""))
       m_vocab_attr.put(vocabName + "_PropRetCon", retConProp);
     //and vocabName"_PropSemantic" as key and semProp as value
     if (semProp != null && !semProp.equals(""))
       m_vocab_attr.put(vocabName + "_PropSemantic", semProp);
     //and vocabName"_PropHDSyn" as key and hdSynProp as value
     if (hdSynProp != null && !hdSynProp.equals(""))
       m_vocab_attr.put(vocabName + "_PropHDSyn", hdSynProp);
     //and vocabName"_RetSearch" as key and retSearch as value
     if (retSearch != null && !retSearch.equals(""))
       m_vocab_attr.put(vocabName + "_RetSearch", retSearch);
     //and vocabName"_IncludeMeta" as key and sMeta as value
     if (sMeta != null && !sMeta.equals(""))
       m_vocab_attr.put(vocabName + "_IncludeMeta", sMeta);
     //and vocabName"_VocabType" as key and vocabType as value
     if (vocabType != null && !vocabType.equals(""))
       m_vocab_attr.put(vocabName + "_VocabType", vocabType);
     //after getting this out of hashtable, check if the key exists for each vocab before
   } */
 
 
   public java.util.List getEVSVocabs(String eURL)
   {
       //ApplicationService evsService = ApplicationService.getRemoteInstance(eURL);
 	  java.util.List<String> vocabList = null;
 	  try
       {
 		  LexBIGService lbs = (LexBIGService) ApplicationServiceProvider.getApplicationServiceFromUrl(eURL, "EvsServiceInfo");
 		  CodingSchemeRenderingList csrl = lbs.getSupportedCodingSchemes();
 
 		  CodingSchemeRendering[] csra = csrl.getCodingSchemeRendering();
 
 		  if (csra != null)
 			  vocabList = new Vector<String>();
 		  for (CodingSchemeRendering csr: csra) {
 			  CodingSchemeSummary css = csr.getCodingSchemeSummary();
 			  String formalName = css.getFormalName();
 			  String localName = css.getLocalName();
			  String descriptionContent = css.getCodingSchemeDescription().getContent();
 			  //System.out.println(formalName + " - " +localName + " - " + descriptionContent);
 			  vocabList.add(formalName);
 		  }
       }
       catch(Exception ex)
       {
         logger.error(" Error - get vocab names ex " + ex.toString(), ex);
       }
       return vocabList;
   }
 
   /**
    * gets EVS related data from tools options table at login instead of hardcoding
    * @param req request object
    * @param res rsponse object
    * @param servlet servlet object
    */
   public void getEVSInfoFromDSR(HttpServletRequest req, HttpServletResponse res, CurationServlet servlet)
   {
     try
     {
       //HttpSession session = req.getSession();
       Vector vList = new Vector();
       String eURL = "";
       //right now use the hard coded vector. later query the database
       GetACService getAC = new GetACService(req, res, servlet);
       /*//get the curation specific url to test
       vList = getAC.getToolOptionData("EVSAPI", "URL", "");
       if (vList != null && vList.size()>0)
       {
         TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean)vList.elementAt(0);
         if (tob != null) eURL = tob.getVALUE();
         //System.out.println(tob.getVALUE() + " evs link cadsr for curation " + eURL);
         if (eURL == null || eURL.equals("")) vList = new Vector();  //check it again
       }*/
       //get it again for all tools property
       if (vList == null || vList.size()<1)
       {
         vList = getAC.getToolOptionData("EVSAPI", "URL", "");
         if (vList != null && vList.size()>0)
         {
           TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean)vList.elementAt(0);
           if (tob != null) eURL = tob.getVALUE();
         }
       }
    //   if (eURL != null) System.out.println(" evs url " + eURL);
       this.setEVSConURL(eURL);
      // if (arrVocab == null) arrVocab = new java.util.List();
       //make sure of the matching before store it in he bean
       this.setDSRDispName("caDSR");
       vList = getAC.getToolOptionData("CURATION", "EVS.DSRDISPLAY", "");
       if (vList != null && vList.size()>0)
       {
         TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean)vList.elementAt(0);
         if (tob != null) this.setDSRDispName(tob.getVALUE());
       }
       //get the source type for the preferred vocabulary
       this.setPrefVocab("");
       vList = getAC.getToolOptionData("CURATION", "EVS.PREFERREDVOCAB", "");
       if (vList != null && vList.size()>0)
       {
         TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean)vList.elementAt(0);
         if (tob != null)
           this.setPrefVocab(tob.getVALUE());
       }
       //get the source type for the preferred vocabulary
       this.setPrefVocabSrc("");
       vList = getAC.getToolOptionData("CURATION", "EVS.PREFERREDVOCAB.SOURCE", "");
       if (vList != null && vList.size()>0)
       {
         TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean)vList.elementAt(0);
         if (tob != null)
           this.setPrefVocabSrc(tob.getVALUE());
       }
 
       //get vocab names
       Vector<String> vocabname = new Vector<String>();
       vList = getAC.getToolOptionData("CURATION", "EVS.VOCAB.%.EVSNAME", "");  //
       if (vList != null && vList.size() > 0)
       {
         for (int i=0; i<vList.size(); i++)
         {
           TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean)vList.elementAt(i);
           if (tob != null) vocabname.addElement(tob.getVALUE());
         }
       }
 
       //get vocab display
       Vector<String> vocabdisp = new Vector<String>();
       vList = getAC.getToolOptionData("CURATION", "EVS.VOCAB.%.DISPLAY", "");  //
       if (vList != null && vList.size() > 0)
       {
         for (int i=0; i<vList.size(); i++)
         {
           TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean)vList.elementAt(i);
           if (tob != null) vocabdisp.addElement(tob.getVALUE());
         }
       }
       //get include meta vocabs
       Vector<String> metavocab = new Vector<String>();
       vList = getAC.getToolOptionData("CURATION", "%.INCLUDEMETA", "");  //
       if (vList != null && vList.size() > 0)
       {
         for (int i=0; i<vList.size(); i++)
         {
           TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean)vList.elementAt(i);
           if (tob != null) metavocab.addElement(tob.getVALUE());
         }
       }
 
       //get vocab names from the evs and make sure they match with the cadsr.
       java.util.List arrEVSVocab = this.getEVSVocabs(eURL);
       if (vocabname != null && arrEVSVocab != null)
       {
         for (int i = 0; i<vocabname.size(); i++)
         {
           String sVocab = (String)vocabname.elementAt(i);
           //compare with evs vocab names  and also the vocab name that only does meta search
           if (!arrEVSVocab.contains(sVocab) && !metavocab.contains(sVocab))
           {
             logger.error(sVocab + " from caDSR does not contain in EVS Vocab list.");
             vocabname.removeElement(sVocab);  //put this back later
             vocabdisp.removeElementAt(i);
           }
         }
       }
       //store the vocab evs name and vocab display name in the bean
       if (vocabname != null && vocabname.size()>0)
         this.setVocabNameList(vocabname);
       if (vocabdisp != null && vocabdisp.size()>0)
         this.setVocabDisplayList(vocabdisp);
       //store meta code separately
       vList = getAC.getToolOptionData("CURATION", "EVS.METACODETYPE.%", "");
       if (vList != null && vList.size()>0)
       {
         Hashtable<String, EVS_METACODE_Bean> hMeta = new Hashtable<String, EVS_METACODE_Bean>();
         for (int i=0; i<vList.size(); i++)
         {
           TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean)vList.elementAt(i);
           if (tob != null)
           {
             EVS_METACODE_Bean metaBean = new EVS_METACODE_Bean();
             String sProp = tob.getPROPERTY().replace("EVS.METACODETYPE.", "");
             if (sProp != null && !sProp.equals(""))
             {
               int iPos = sProp.indexOf('.');
               String sKey = sProp.substring(0, iPos);
               metaBean.setMETACODE_KEY(sKey);
               String sType = sProp.substring(iPos + 1);
               metaBean.setMETACODE_TYPE(sType);
               String sValue = tob.getVALUE();
               metaBean.setMETACODE_FILTER(sValue);
               hMeta.put(sKey, metaBean);
             }
           }
         }
         this.setMetaCodeType(hMeta);
       }
 
       // this.setDefDefaultValue("No Value Exists.");  //default value for the definition for all vocabs
 
       //store teh list of definition sources to filter out for duplicates store them in this order
       vList = getAC.getToolOptionData("CURATION", "EVS.DEFSOURCE.%", "");  //
       if (vList != null && vList.size() > 0)
       {
         Vector<String> vDefSrc = new Vector<String>();
         for (int i=0; i<vList.size(); i++)
         {
           TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean)vList.elementAt(i);
           if (tob != null) vDefSrc.addElement(tob.getVALUE());
         }
         if (vDefSrc != null && vDefSrc.size()>0)
           this.setNCIDefSrcList(vDefSrc);
       }
 
       //get vocab attributes
       Hashtable<String, EVS_UserBean> hvoc = new Hashtable<String, EVS_UserBean>();
       vList = getAC.getToolOptionData("CURATION", "EVS.VOCAB.ALL.%", "");  //
       //do the looping way right now
       if (vocabname != null)
       {
         for (int i = 0; i<vocabname.size(); i++)
         {
           String sVocab = (String)vocabname.elementAt(i);
           EVS_UserBean vBean = new EVS_UserBean();
           String sDisp = sVocab;
           if (vocabdisp != null && vocabdisp.size()> i)
             sDisp = (String)vocabdisp.elementAt(i);
           // vBean.setEVSConURL(eURL);
           vBean.setVocabName(sVocab);
           vBean.setVocabDisplay(sDisp);
           vBean.setVocabDBOrigin(sDisp);
           // vBean.setVocabUseParent(false);
           vBean = this.storeVocabAttr(vBean, vList);  //call method to store all attributes
           /*
 
 
           int vocabInd=0;
           if(sVocab.equals("Zebrafish"))
         	 vocabInd = 9;
           else
         	  vocabInd=i+1;*/
         String toolprop = getAC.getVocabInd(sVocab)+"%"; //"EVS.VOCAB." + vocabInd + "%";
       //System.out.println(sVocab + toolprop);
           Vector vocabList = getAC.getToolOptionData("CURATION", toolprop,"");
           vBean = this.storeVocabAttr(vBean, vocabList);  //call method to add vocab specific attributges
           //put this bean in the hash table
           hvoc.put(sVocab, vBean);
         }
       }
       this.setVocab_Attr(hvoc);
       servlet.sessionData.EvsUsrBean = this;    //session.setAttribute("EvsUserBean", this);
 
     }
     catch(Exception e)
     {
       logger.error("Error: getEVSInfoFromDSR " + e.toString(), e);
     }
   }
 
 
   public EVS_UserBean storeVocabAttr(EVS_UserBean vuBean, Vector vAttr)
   {
     try
     {
       if (vAttr == null || vAttr.size()<1) return vuBean;
       //continue adding the attributes to the bean
       for (int k=0; k<vAttr.size(); k++)
       {
         TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean)vAttr.elementAt(k);
         if (tob != null)
         {
           String sType = tob.getPROPERTY();
           if (sType == null) sType = "";
           String sValue = tob.getVALUE();
           if (sValue == null) sValue = "";
           //check if the attr exists in the database for the vocab and add to the bean
           if (sType.indexOf("DBNAME")>0)
             vuBean.setVocabDBOrigin(sValue);  //("NCI Thesaurus");
           if (sType.indexOf("VOCABCODETYPE")>0)
             vuBean.setVocabCodeType(sValue);  //("MEDDRA_CODE");
           if (sType.indexOf("ACCESSREQUIRED")>0)
               vuBean.setVocabAccess(sValue);  //("MEDDRA_CODE");
           if (sType.indexOf("USEPARENT")>0)
           {
             boolean bValue = false;
             if (sValue.equalsIgnoreCase("true")) bValue = true;
             vuBean.setVocabUseParent(bValue);  //(true);
           }
           if (sType.indexOf("METASOURCE")>0)
             vuBean.setVocabMetaSource(sValue);  //("MDR-60");
           if (sType.indexOf("SEARCH_IN.NAME")>0)
             vuBean.setSearchInName(sValue);  //("Name");
           if (sType.indexOf("SEARCH_IN.CONCODE")>0)
             vuBean.setSearchInConCode(sValue);  //("Concept Code");
           if (sType.indexOf("SEARCH_IN.METACODE")>0)
             vuBean.setSearchInMetaCode(sValue);  //("");
           if (sType.indexOf("RETSEARCH")>0)
             vuBean.setRetSearch(sValue);  //("false");
           if (sType.indexOf("TREESEARCH")>0)
             vuBean.setTreeSearch(sValue);  //("false");
             //get property values
           if (sType.indexOf("PROPERTY.DEFINITION")>0)
             vuBean.setPropDefinition(sValue);  //("DEFINITION");
           if (sType.indexOf("PROPERTY.HDSYNONYM")>0)
             vuBean.setPropHDSyn(sValue);  //("FULL_SYN");
           if (sType.indexOf("PROPERTY.RETIRED")>0)
             vuBean.setPropRetCon(sValue);  //("Concept_Status");
           if (sType.indexOf("PROPERTY.SEMANTIC")>0)
             vuBean.setPropSemantic(sValue);  //("Semantic");
           if (sType.indexOf("PROPERTY.NAMESEARCH")>0)
             vuBean.setPropName(sValue);  //("");  //vocab property used to search the concept name
           if (sType.indexOf("PROPERTY.NAMEDISPLAY")>0)
             vuBean.setPropNameDisp(sValue);  //("");  //vocab proeprty used to get the concept name display
           if (sType.indexOf("SEARCHTYPE")>0)  //get vocab search type
             vuBean.setNameType(sValue);  //("NameType");
           if (sType.indexOf("INCLUDEMETA")>0)
           {
             vuBean.setIncludeMeta(sValue);  //("");   //name of meta vocab to include with the other vocab
             this.setMetaDispName(sValue);  //("NCI Metathesaurus");  //store it in the main bean too for easy access
           }
           if (sType.indexOf("DEFAULT_DEFINITION")>0)
           {
             vuBean.setDefDefaultValue(sValue);  //("No Value Exists.");  //default value for the definition
             this.setDefDefaultValue(sValue);  //("No Value Exists.");  //also for the main beandefault value for the definition
           }
         }
       }
     }
     catch(Exception e)
     {
       logger.error("Error: storeVocabAttr " + e.toString(), e);
     }
     return vuBean;
   }
 
 }
 
 
