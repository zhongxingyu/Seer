 package gov.nih.nci.evs.reportwriter.utils;
 
 import java.io.*;
 import java.util.*;
 import gov.nih.nci.evs.reportwriter.bean.*;
 
 import javax.faces.model.*;
 import org.LexGrid.LexBIG.DataModel.Collections.*;
 import org.LexGrid.LexBIG.DataModel.Core.*;
 import org.LexGrid.LexBIG.LexBIGService.*;
 import org.LexGrid.LexBIG.Utility.*;
 import org.LexGrid.concepts.*;
 import org.LexGrid.LexBIG.DataModel.InterfaceElements.*;
 import org.LexGrid.LexBIG.Utility.Iterators.*;
 import org.LexGrid.codingSchemes.*;
 import org.LexGrid.naming.*;
 import org.LexGrid.LexBIG.DataModel.Core.types.*;
 import org.LexGrid.LexBIG.Extensions.Generic.*;
 import gov.nih.nci.evs.reportwriter.properties.*;
 import org.LexGrid.LexBIG.Exceptions.*;
 import org.apache.log4j.*;
 
 /**
  * <!-- LICENSE_TEXT_START -->
  * Copyright 2008,2009 NGIT. This software was developed in conjunction
  * with the National Cancer Institute, and so to the extent government
  * employees are co-authors, any rights in such works shall be subject
  * to Title 17 of the United States Code, section 105.
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *   1. Redistributions of source code must retain the above copyright
  *      notice, this list of conditions and the disclaimer of Article 3,
  *      below. Redistributions in binary form must reproduce the above
  *      copyright notice, this list of conditions and the following
  *      disclaimer in the documentation and/or other materials provided
  *      with the distribution.
  *   2. The end-user documentation included with the redistribution,
  *      if any, must include the following acknowledgment:
  *      "This product includes software developed by NGIT and the National
  *      Cancer Institute."   If no such end-user documentation is to be
  *      included, this acknowledgment shall appear in the software itself,
  *      wherever such third-party acknowledgments normally appear.
  *   3. The names "The National Cancer Institute", "NCI" and "NGIT" must
  *      not be used to endorse or promote products derived from this software.
  *   4. This license does not authorize the incorporation of this software
  *      into any third party proprietary programs. This license does not
  *      authorize the recipient to use any trademarks owned by either NCI
  *      or NGIT
  *   5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED
  *      WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  *      OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE
  *      DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
  *      NGIT, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
  *      INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  *      BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *      LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  *      CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  *      LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
  *      ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  *      POSSIBILITY OF SUCH DAMAGE.
  * <!-- LICENSE_TEXT_END -->
  */
 
 /**
  * @author EVS Team (Kim Ong, David Yee)
  * @version 1.0
  */
 
 public class DataUtils {
     private static Logger _logger = Logger.getLogger(DataUtils.class);
     private int _maxReturn = 5000;
 
     private static List<SelectItem> _standardReportTemplateList = null;
     private static List<SelectItem> _adminTaskList = null;
     private static List<SelectItem> _userTaskList = null;
     private static List<SelectItem> _propertyTypeList = null;
     private static List<SelectItem> _ontologies = null;
 
     public org.LexGrid.LexBIG.Utility.ConvenienceMethods _lbConvMethods = null;
     public CodingSchemeRenderingList _csrl = null;
     private static HashMap<String, CodingScheme> _codingSchemeMap = null;
 
     private static HashMap<String, String> _csnv2codingSchemeNameMap = null;
     private static HashMap<String, String> _csnv2VersionMap = null;
 
     // =========================================================================
     // For customized query use
     // =========================================================================
     public static int ALL = 0;
     public static int PREFERRED_ONLY = 1;
     public static int NON_PREFERRED_ONLY = 2;
 
     // private static int RESOLVE_SOURCE = 1;
     // private static int RESOLVE_TARGET = -1;
     // private static int RESTRICT_SOURCE = -1;
     // private static int RESTRICT_TARGET = 1;
 
     public static final int SEARCH_NAME_CODE = 1;
     public static final int SEARCH_DEFINITION = 2;
 
     public static final int SEARCH_PROPERTY_VALUE = 3;
     public static final int SEARCH_ROLE_VALUE = 6;
     public static final int SEARCH_ASSOCIATION_VALUE = 7;
 
     // private static final List<String> STOP_WORDS =
     // Arrays.asList(new String[] { "a", "an", "and", "by", "for", "of", "on",
     // "in", "nos", "the", "to", "with" });
 
     // ==========================================================================
     public DataUtils() {
         _adminTaskList = new ArrayList<SelectItem>();
         _adminTaskList.add(new SelectItem("Administer Standard Reports"));
         _adminTaskList.add(new SelectItem("Maintain Report Status"));
         _adminTaskList.add(new SelectItem("Assign Report Status"));
         _adminTaskList.add(new SelectItem("Retrieve Standard Reports"));
 
         _userTaskList = new ArrayList<SelectItem>();
         _userTaskList.add(new SelectItem("Retrieve Standard Reports"));
 
         _standardReportTemplateList = new ArrayList<SelectItem>();
 
         setCodingSchemeMap();
 
         _maxReturn =
             ReportWriterProperties.getIntProperty(
                 ReportWriterProperties.MAXIMUM_RETURN, 5000);
     }
 
     public static List<SelectItem> getPropertyTypeList() {
         if (_propertyTypeList == null) {
             _propertyTypeList = new ArrayList<SelectItem>();
             _propertyTypeList.add(new SelectItem(""));
             _propertyTypeList.add(new SelectItem("COMMENT"));
             _propertyTypeList.add(new SelectItem("DEFINITION"));
             _propertyTypeList.add(new SelectItem("GENERIC"));
             _propertyTypeList.add(new SelectItem("INSTRUCTION"));
             _propertyTypeList.add(new SelectItem("PRESENTATION"));
         }
         return _propertyTypeList;
     }
 
     public static List<SelectItem> getTaskList(Boolean isAdmin) {
         if (isAdmin == null)
             return null;
         else if (isAdmin.equals(Boolean.TRUE)) {
             if (_adminTaskList == null) {
                 _adminTaskList = new ArrayList<SelectItem>();
                 _adminTaskList
                     .add(new SelectItem("Administer Standard Reports"));
                 _adminTaskList.add(new SelectItem("Maintain Report Status"));
                 _adminTaskList.add(new SelectItem("Assign Report Status"));
                 _adminTaskList.add(new SelectItem("Retrieve Standard Reports"));
             }
             return _adminTaskList;
         } else {
             if (_userTaskList == null) {
                 _userTaskList = new ArrayList<SelectItem>();
                 _userTaskList.add(new SelectItem("Retrieve Standard Reports"));
             }
             return _userTaskList;
         }
     }
 
     // public static List getStandardReportTemplateList(Boolean isAdmin) {
     public static List<SelectItem> getStandardReportTemplateList() {
         // if (isAdmin == null || isAdmin == Boolean.FALSE) return null;
 
         _standardReportTemplateList = new ArrayList<SelectItem>();
         try {
             SDKClientUtil util = new SDKClientUtil();
             String FQName =
                 "gov.nih.nci.evs.reportwriter.bean.StandardReportTemplate";
             Object[] objs = util.search(FQName);
             if (objs.length == 0)
                 return _standardReportTemplateList;
             Vector<String> v = new Vector<String>();
             for (int i = 0; i < objs.length; i++) {
                 StandardReportTemplate standardReportTemplate =
                     (StandardReportTemplate) objs[i];
                 // standardReportTemplateList.add(new
                 // SelectItem(standardReportTemplate.getLabel()));
                 v.add(standardReportTemplate.getLabel());
             }
             SortUtils.quickSort(v);
             for (int i = 0; i < v.size(); i++) {
                 String name = (String) v.elementAt(i);
                 _standardReportTemplateList.add(new SelectItem(name));
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return _standardReportTemplateList;
     }
 
     public static List<SelectItem> getOntologyList() {
         if (_ontologies == null)
             setCodingSchemeMap();
         return _ontologies;
     }
 
     public static Boolean validateCodingScheme(String formalname, String version) {
         // Note: To be implemented:
         // _logger.debug("Method: DataUtils.validateCodingScheme");
         //
         // if (csnv2codingSchemeNameMap == null ||
         // csnv2VersionMap == null) {
         // _logger.debug("DataUtils.validateCodingScheme "
         // + "calling setCodingSchemeMap");
         // setCodingSchemeMap();
         // return validateCodingScheme(formalname, version);
         // }
         //
         // String key = formalname + " (version: " + version + ")";
         // _logger.debug("DataUtils   validateCodingScheme key: " + key);
         // if (csnv2codingSchemeNameMap.get(key) == null ||
         // csnv2VersionMap.get(key) == null ) {
         // _logger.debug("DataUtils.validateCodingScheme "
         // + "csnv2codingSchemeNameMap.get(key) == null "
         // + "|| csnv2VersionMap.get(key) == null :??? ");
         // _logger.debug("* return Boolean.FALSE ");
         // return Boolean.FALSE;
         // }
         // _logger.debug("* return Boolean.TRUE ");
         return Boolean.TRUE;
     }
 
     private static void setCodingSchemeMap() {
         // if (_ontologies != null) return;
         _ontologies = new ArrayList<SelectItem>();
         _codingSchemeMap = new HashMap<String, CodingScheme>();
         _csnv2codingSchemeNameMap = new HashMap<String, String>();
         _csnv2VersionMap = new HashMap<String, String>();
 
         try {
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             CodingSchemeRenderingList csrl = lbSvc.getSupportedCodingSchemes();
             if (csrl == null)
                 _logger.debug("csrl is NULL");
 
             CodingSchemeRendering[] csrs = csrl.getCodingSchemeRendering();
             for (int i = 0; i < csrs.length; i++) {
                 CodingSchemeRendering csr = csrs[i];
                 Boolean isActive =
                     csr.getRenderingDetail().getVersionStatus().equals(
                         CodingSchemeVersionStatus.ACTIVE);
                 if (isActive != null && isActive.equals(Boolean.TRUE)) {
                     CodingSchemeSummary css = csr.getCodingSchemeSummary();
                     String formalname = css.getFormalName();
                     String representsVersion = css.getRepresentsVersion();
                     CodingSchemeVersionOrTag vt =
                         new CodingSchemeVersionOrTag();
                     vt.setVersion(representsVersion);
 
                     CodingScheme scheme = null;
                     try {
                         try {
                             scheme = lbSvc.resolveCodingScheme(formalname, vt);
                         } catch (Exception ex) {
                         }
                         if (scheme != null) {
                             _codingSchemeMap.put(formalname, scheme);
 
                             String value =
                                 formalname + " (version: " + representsVersion
                                     + ")";
                             _ontologies.add(new SelectItem(value, value));
 
                             _csnv2codingSchemeNameMap.put(value, formalname);
                             _csnv2VersionMap.put(value, representsVersion);
 
                         }
 
                     } catch (Exception e) {
                         String urn = css.getCodingSchemeURI();
                         try {
                             scheme = lbSvc.resolveCodingScheme(urn, vt);
                             if (scheme != null) {
                                 _codingSchemeMap.put(formalname, scheme);
 
                                 String value =
                                     formalname + " (version: "
                                         + representsVersion + ")";
                                 _ontologies.add(new SelectItem(value, value));
 
                                 _csnv2codingSchemeNameMap
                                     .put(value, formalname);
                                 _csnv2VersionMap.put(value, representsVersion);
                             }
 
                         } catch (Exception ex) {
 
                             String localname = css.getLocalName();
                             try {
                                 scheme =
                                     lbSvc.resolveCodingScheme(localname, vt);
                                 if (scheme != null) {
                                     _codingSchemeMap.put(formalname, scheme);
 
                                     String value =
                                         formalname + " (version: "
                                             + representsVersion + ")";
                                     _ontologies
                                         .add(new SelectItem(value, value));
 
                                     _csnv2codingSchemeNameMap.put(value,
                                         formalname);
                                     _csnv2VersionMap.put(value,
                                         representsVersion);
                                 }
                             } catch (Exception e2) {
                                 e2.printStackTrace();
                             }
                         }
                     }
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     public static Vector<String> getSupportedAssociationNames(String key) {
         if (_csnv2codingSchemeNameMap == null) {
             setCodingSchemeMap();
             return getSupportedAssociationNames(key);
         }
         String codingSchemeName = _csnv2codingSchemeNameMap.get(key);
         if (codingSchemeName == null)
             return null;
         String version = _csnv2VersionMap.get(key);
         if (version == null)
             return null;
         return getSupportedAssociationNames(codingSchemeName, version);
     }
 
     public static Vector<String> getSupportedAssociationNames(
         String codingSchemeName, String version) {
         CodingSchemeVersionOrTag vt = new CodingSchemeVersionOrTag();
         if (version != null) {
             vt.setVersion(version);
         }
 
         CodingScheme scheme = null;
         try {
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             scheme = lbSvc.resolveCodingScheme(codingSchemeName, vt);
             if (scheme == null) {
                 _logger.debug("scheme is NULL");
                 return null;
             }
 
             Vector<String> v = new Vector<String>();
             SupportedAssociation[] assos =
                 scheme.getMappings().getSupportedAssociation();
             for (int i = 0; i < assos.length; i++) {
                 SupportedAssociation sa = (SupportedAssociation) assos[i];
                 v.add(sa.getLocalId());
             }
             return v;
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return null;
     }
 
     public static Vector<String> getPropertyNameListData(String key) {
         if (_csnv2codingSchemeNameMap == null) {
             setCodingSchemeMap();
         }
 
         String codingSchemeName = _csnv2codingSchemeNameMap.get(key);
         if (codingSchemeName == null) {
             return null;
         }
         String version = _csnv2VersionMap.get(key);
         if (version == null) {
             return null;
         }
         return getPropertyNameListData(codingSchemeName, version);
     }
 
     public static Vector<String> getPropertyNameListData(
         String codingSchemeName, String version) {
         CodingSchemeVersionOrTag vt = new CodingSchemeVersionOrTag();
         if (version != null) {
             vt.setVersion(version);
         }
         CodingScheme scheme = null;
         try {
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             scheme = lbSvc.resolveCodingScheme(codingSchemeName, vt);
             if (scheme == null)
                 return null;
             Vector<String> propertyNameListData = new Vector<String>();
             SupportedProperty[] properties =
                 scheme.getMappings().getSupportedProperty();
             for (int i = 0; i < properties.length; i++) {
                 SupportedProperty property = properties[i];
                 propertyNameListData.add(property.getLocalId());
             }
             return propertyNameListData;
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return null;
     }
 
     public static String getCodingSchemeName(String key) {
         return _csnv2codingSchemeNameMap.get(key);
     }
 
     public static String getCodingSchemeVersion(String key) {
         return _csnv2VersionMap.get(key);
     }
 
     public static Vector<String> getRepresentationalFormListData(String key) {
         String codingSchemeName = _csnv2codingSchemeNameMap.get(key);
         if (codingSchemeName == null)
             return null;
         String version = _csnv2VersionMap.get(key);
         if (version == null)
             return null;
         return getRepresentationalFormListData(codingSchemeName, version);
     }
 
     public static Vector<String> getRepresentationalFormListData(
         String codingSchemeName, String version) {
         CodingSchemeVersionOrTag vt = new CodingSchemeVersionOrTag();
         if (version != null) {
             vt.setVersion(version);
         }
         CodingScheme scheme = null;
         try {
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             scheme = lbSvc.resolveCodingScheme(codingSchemeName, vt);
             if (scheme == null)
                 return null;
             Vector<String> propertyNameListData = new Vector<String>();
             SupportedRepresentationalForm[] forms =
                 scheme.getMappings().getSupportedRepresentationalForm();
             if (true) {
                 _logger.debug(StringUtils.SEPARATOR);
                 _logger.debug("MethodgetRepresentationalFormListData");
                 _logger.debug("* codingSchemeName: " + codingSchemeName);
                 _logger.debug("* version: " + version);
                 _logger.debug("* forms: ");
                 if (forms != null) {
                     for (int i = 0; i < forms.length; ++i)
                         _logger.debug("  " + i + ") " + forms[i].getLocalId());
                 }
             }
             if (forms != null) {
                 for (int i = 0; i < forms.length; i++) {
                     SupportedRepresentationalForm form = forms[i];
                     propertyNameListData.add(form.getLocalId());
                 }
             }
             return propertyNameListData;
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return null;
     }
 
     public static Vector<String> getPropertyQualifierListData(String key) {
         String codingSchemeName = _csnv2codingSchemeNameMap.get(key);
         if (codingSchemeName == null)
             return null;
         String version = _csnv2VersionMap.get(key);
         if (version == null)
             return null;
         return getPropertyQualifierListData(codingSchemeName, version);
     }
 
     public static Vector<String> getPropertyQualifierListData(
         String codingSchemeName, String version) {
         CodingSchemeVersionOrTag vt = new CodingSchemeVersionOrTag();
         if (version != null) {
             vt.setVersion(version);
         }
         CodingScheme scheme = null;
         try {
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             scheme = lbSvc.resolveCodingScheme(codingSchemeName, vt);
             if (scheme == null)
                 return null;
             Vector<String> propertyQualifierListData = new Vector<String>();
             SupportedPropertyQualifier[] qualifiers =
                 scheme.getMappings().getSupportedPropertyQualifier();
             for (int i = 0; i < qualifiers.length; i++) {
                 SupportedPropertyQualifier qualifier = qualifiers[i];
                 propertyQualifierListData.add(qualifier.getLocalId());
             }
 
             return propertyQualifierListData;
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return null;
     }
 
     public static Vector<String> getSourceListData(String key) {
         if (_csnv2codingSchemeNameMap == null) {
             setCodingSchemeMap();
             return getSourceListData(key);
         }
         String codingSchemeName = _csnv2codingSchemeNameMap.get(key);
         if (codingSchemeName == null)
             return null;
         String version = _csnv2VersionMap.get(key);
         if (version == null)
             return null;
         return getSourceListData(codingSchemeName, version);
     }
 
     public static Vector<String> getSourceListData(String codingSchemeName,
         String version) {
         CodingSchemeVersionOrTag vt = new CodingSchemeVersionOrTag();
         if (version != null) {
             vt.setVersion(version);
         }
         CodingScheme scheme = null;
         try {
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             scheme = lbSvc.resolveCodingScheme(codingSchemeName, vt);
             if (scheme == null)
                 return null;
             Vector<String> sourceListData = new Vector<String>();
 
             // Insert your code here
             SupportedSource[] sources =
                 scheme.getMappings().getSupportedSource();
             for (int i = 0; i < sources.length; i++) {
                 SupportedSource source = sources[i];
                 sourceListData.add(source.getLocalId());
             }
 
             return sourceListData;
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return null;
     }
 
     public static Vector<String> getReportStatusListData() {
         Vector<String> reportStatusListData = new Vector<String>();
         try {
             SDKClientUtil util = new SDKClientUtil();
             String FQName = "gov.nih.nci.evs.reportwriter.bean.ReportStatus";
             Object[] objs = util.search(FQName);
 
             if (objs != null && objs.length > 0) {
                 for (int i = 0; i < objs.length; i++) {
                     ReportStatus reportStatus = (ReportStatus) objs[i];
                     reportStatusListData.add(reportStatus.getLabel());
                 }
             } else {
                 // Initial status (DRAFT and APPROVED)
                 String label = "DRAFT";
                 String description =
                     "Report is a draft, not ready for download.";
                 boolean active = true;
                 try {
                     util.insertReportStatus(label, description, active);
                 } catch (Exception ex) {
                     _logger.error("*** insertReportStatus DRAFT failed.");
                 }
 
                 label = "APPROVED";
                 description = "Report has been approved for download by users";
                 active = true;
                 try {
                     util.insertReportStatus(label, description, active);
                 } catch (Exception ex) {
                     _logger.error("*** insertReportStatus APPROVED failed.");
                 }
 
                 objs = util.search(FQName);
                 if (objs != null && objs.length > 0) {
                     for (int i = 0; i < objs.length; i++) {
                         ReportStatus reportStatus = (ReportStatus) objs[i];
                         reportStatusListData.add(reportStatus.getLabel());
                     }
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return reportStatusListData;
     }
 
     public static Vector<String> getReportFormatListData() {
         Vector<String> reportFormatListData = new Vector<String>();
         try {
             SDKClientUtil util = new SDKClientUtil();
             String FQName = "gov.nih.nci.evs.reportwriter.bean.ReportFormat";
             Object[] objs = util.search(FQName);
 
             if (objs != null && objs.length > 0) {
                 for (int i = 0; i < objs.length; i++) {
                     ReportFormat reportFormat = (ReportFormat) objs[i];
                     reportFormatListData.add(reportFormat.getDescription());
                 }
             } else {
                 String description = "Text (tab delimited)";
                 try {
                     util.insertReportFormat(description);
                 } catch (Exception ex) {
                     _logger.debug("*** insertReportFormat " + description
                         + " failed.");
                 }
 
                 description = "Microsoft Office Excel";
                 try {
                     util.insertReportFormat(description);
                 } catch (Exception ex) {
                     _logger.debug("*** insertReportFormat " + description
                         + " failed.");
                 }
 
                 objs = util.search(FQName);
                 if (objs != null && objs.length > 0) {
                     for (int i = 0; i < objs.length; i++) {
                         ReportFormat reportFormat = (ReportFormat) objs[i];
                         reportFormatListData.add(reportFormat.getDescription());
                     }
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return reportFormatListData;
     }
 
     public static List<ReportFormat> getAvailableReportFormat() {
         List<ReportFormat> list = new ArrayList<ReportFormat>();
         try {
             SDKClientUtil util = new SDKClientUtil();
             String FQName = "gov.nih.nci.evs.reportwriter.bean.ReportFormat";
             Object[] objs = util.search(FQName);
 
             if (objs == null || objs.length == 0) {
                 String description = "Text (tab delimited)";
                 try {
                     util.insertReportFormat(description);
                 } catch (Exception ex) {
                     _logger.debug("*** insertReportFormat " + description
                         + " failed.");
                 }
 
                 description = "Microsoft Office Excel";
                 try {
                     util.insertReportFormat(description);
                 } catch (Exception ex) {
                     _logger.debug("*** insertReportFormat " + description
                         + " failed.");
                 }
 
                 objs = util.search(FQName);
                 if (objs != null && objs.length > 0) {
                     for (int i = 0; i < objs.length; i++) {
                         ReportFormat reportFormat = (ReportFormat) objs[i];
                         list.add(reportFormat);
                     }
                 }
             }
 
             else // if (objs != null && objs.length > 0)
             {
                 for (int i = 0; i < objs.length; i++) {
                     ReportFormat reportFormat = (ReportFormat) objs[i];
                     list.add(reportFormat);
                 }
             }
 
         } catch (Exception e) {
             e.printStackTrace();
         }
         return list;
     }
 
     public static String int2String(Integer int_obj) {
         if (int_obj == null) {
             return null;
         }
 
         String retstr = Integer.toString(int_obj);
         return retstr;
     }
 
     // =========================================================================
 
     public static Concept getConceptByCode(String codingSchemeName,
         String vers, String ltag, String code) {
         try {
             ReportWriterProperties
                 .getProperty(ReportWriterProperties.EVS_SERVICE_URL);
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             if (lbSvc == null) {
                 _logger.error("lbSvc == null???");
                 return null;
             }
 
             CodingSchemeVersionOrTag versionOrTag =
                 new CodingSchemeVersionOrTag();
             versionOrTag.setVersion(vers);
 
             ConceptReferenceList crefs =
                 createConceptReferenceList(new String[] { code },
                     codingSchemeName);
 
             CodedNodeSet cns = null;
 
             try {
                 _logger.debug("DataUtils calling getCodingSchemeConcepts: "
                     + code);
                 cns =
                     lbSvc.getCodingSchemeConcepts(codingSchemeName,
                         versionOrTag);
             } catch (Exception e1) {
                 _logger.error("DataUtils lbSvc.getCodingSchemeConcepts "
                     + "threw exception??? " + code);
                 // e1.printStackTrace();
                 return null;
             }
 
             cns = cns.restrictToCodes(crefs);
             ResolvedConceptReferenceList matches =
                 cns.resolveToList(null, null, null, 1);
 
             if (matches == null) {
                 _logger.warn("Concept not found.");
                 return null;
             }
 
             // Analyze the result ...
             if (matches.getResolvedConceptReferenceCount() > 0) {
                 ResolvedConceptReference ref =
                     (ResolvedConceptReference) matches
                         .enumerateResolvedConceptReference().nextElement();
 
                 Concept entry = ref.getReferencedEntry();
                 return entry;
             }
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }
         return null;
     }
 
     public static NameAndValueList createNameAndValueList(String[] names,
         String[] values) {
         NameAndValueList nvList = new NameAndValueList();
         for (int i = 0; i < names.length; i++) {
             NameAndValue nv = new NameAndValue();
             nv.setName(names[i]);
             if (values != null) {
                 nv.setContent(values[i]);
             }
             nvList.addNameAndValue(nv);
         }
         return nvList;
     }
 
     public Vector<Concept> getAssociationTargets(String scheme,
         String version, String code, String assocName) {
        return getAssociation(true, scheme, version, code, assocName);
     }
 
     public ResolvedConceptReferenceList getNext(
         ResolvedConceptReferencesIterator iterator) {
         return iterator.getNext();
     }
 
     /**
      * Dump_matches to output, for debug purposes
      * 
      * @param iterator
      *            the iterator
      * @param maxToReturn
      *            the max to return
      */
     public Vector<Concept> resolveIterator(
         ResolvedConceptReferencesIterator iterator, int maxToReturn) {
         Vector<Concept> v = new Vector<Concept>();
         if (iterator == null) {
             _logger.debug("No match.");
             return v;
         }
         try {
             int iteration = 0;
             while (iterator.hasNext()) {
                 iteration++;
                 iterator = iterator.scroll(maxToReturn);
                 ResolvedConceptReferenceList rcrl = iterator.getNext();
                 ResolvedConceptReference[] rcra =
                     rcrl.getResolvedConceptReference();
                 for (int i = 0; i < rcra.length; i++) {
                     ResolvedConceptReference rcr = rcra[i];
                     Concept ce = rcr.getReferencedEntry();
                     // _logger.debug("Iteration " + iteration + " " +
                     // ce.getId() + " " +
                     // ce.getEntityDescription().getContent());
                     v.add(ce);
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return v;
     }
 
     public Vector<Concept> getAssociationSources(String scheme, String version,
         String code, String assocName) {
        return getAssociation(false, scheme, version, code, assocName);
     }
     
    public Vector<Concept> getAssociation(boolean retrieveTargets, 
         String scheme, String version, String code, String assocName) {
         CodingSchemeVersionOrTag csvt = new CodingSchemeVersionOrTag();
         if (version != null)
             csvt.setVersion(version);
         ResolvedConceptReferenceList matches = null;
         Vector<Concept> v = new Vector<Concept>();
         try {
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             CodedNodeGraph cng = lbSvc.getNodeGraph(scheme, csvt, null);
 
             NameAndValueList nameAndValueList =
                 createNameAndValueList(new String[] { assocName }, null);
 
             NameAndValueList nameAndValueList_qualifier = null;
             cng =
                 cng.restrictToAssociations(nameAndValueList,
                     nameAndValueList_qualifier);
 
             boolean resolveForward = retrieveTargets;
             boolean resolveBackward = ! retrieveTargets;
             matches =
                 cng.resolveAsList(ConvenienceMethods.createConceptReference(
                     code, scheme), resolveForward, resolveBackward, 1, 1, new LocalNameList(),
                     null, null, _maxReturn);
 
             if (matches.getResolvedConceptReferenceCount() > 0) {
                 Enumeration<ResolvedConceptReference> refEnum =
                     matches.enumerateResolvedConceptReference();
 
                 while (refEnum.hasMoreElements()) {
                     ResolvedConceptReference ref = refEnum.nextElement();
                     AssociationList alist = retrieveTargets ? ref.getSourceOf() :
                         ref.getTargetOf();
                     if (alist == null)
                         continue;
                     Association[] associations = alist.getAssociation();
 
                     for (int i = 0; i < associations.length; i++) {
                         Association assoc = associations[i];
                         // KLO
                         assoc = processForAnonomousNodes(assoc);
                         AssociatedConcept[] acl =
                             assoc.getAssociatedConcepts()
                                 .getAssociatedConcept();
                         for (int j = 0; j < acl.length; j++) {
                             AssociatedConcept ac = acl[j];
                             v.add(ac.getReferencedEntry());
                         }
                     }
                 }
                 SortUtils.quickSort(v);
             }
 
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return v;
     }
     
     public Vector<String> getParentCodes(String scheme, String version,
         String code) {
         Vector<String> hierarchicalAssoName_vec =
             getHierarchyAssociationId(scheme, version);
         if (hierarchicalAssoName_vec == null
             || hierarchicalAssoName_vec.size() == 0) {
             return null;
         }
         String hierarchicalAssoName =
             (String) hierarchicalAssoName_vec.elementAt(0);
         // KLO, 01/23/2009
         // Vector<Concept> superconcept_vec = util.getAssociationSources(scheme,
         // version, code, hierarchicalAssoName);
         Vector<String> superconcept_vec =
             getAssociationSourceCodes(scheme, version, code,
                 hierarchicalAssoName);
         if (superconcept_vec == null)
             return null;
         // SortUtils.quickSort(superconcept_vec, SortUtils.SORT_BY_CODE);
         return superconcept_vec;
 
     }
 
     public Vector<String> getAssociationSourceCodes(String scheme,
         String version, String code, String assocName) {
         CodingSchemeVersionOrTag csvt = new CodingSchemeVersionOrTag();
         if (version != null)
             csvt.setVersion(version);
         ResolvedConceptReferenceList matches = null;
         Vector<String> v = new Vector<String>();
         try {
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             CodedNodeGraph cng = lbSvc.getNodeGraph(scheme, csvt, null);
 
             NameAndValueList nameAndValueList =
                 createNameAndValueList(new String[] { assocName }, null);
 
             NameAndValueList nameAndValueList_qualifier = null;
             cng =
                 cng.restrictToAssociations(nameAndValueList,
                     nameAndValueList_qualifier);
 
             matches =
                 cng.resolveAsList(ConvenienceMethods.createConceptReference(
                     code, scheme), false, true, 1, 1, new LocalNameList(),
                     null, null, _maxReturn);
 
             if (matches.getResolvedConceptReferenceCount() > 0) {
                 Enumeration<ResolvedConceptReference> refEnum =
                     matches.enumerateResolvedConceptReference();
 
                 while (refEnum.hasMoreElements()) {
                     ResolvedConceptReference ref = refEnum.nextElement();
                     AssociationList targetof = ref.getTargetOf();
                     Association[] associations = targetof.getAssociation();
 
                     for (int i = 0; i < associations.length; i++) {
                         Association assoc = associations[i];
                         // KLO
                         assoc = processForAnonomousNodes(assoc);
                         AssociatedConcept[] acl =
                             assoc.getAssociatedConcepts()
                                 .getAssociatedConcept();
                         for (int j = 0; j < acl.length; j++) {
                             AssociatedConcept ac = acl[j];
                             v.add(ac.getReferencedEntry().getEntityCode());
                         }
                     }
                 }
                 SortUtils.quickSort(v);
             }
 
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return v;
     }
 
     public static ConceptReferenceList createConceptReferenceList(
         String[] codes, String codingSchemeName) {
         if (codes == null) {
             return null;
         }
         ConceptReferenceList list = new ConceptReferenceList();
         for (int i = 0; i < codes.length; i++) {
             ConceptReference cr = new ConceptReference();
             cr.setCodingSchemeName(codingSchemeName);
             cr.setConceptCode(codes[i]);
             list.addConceptReference(cr);
         }
         return list;
     }
 
     public Vector<String> getSubconceptCodes(String scheme, String version,
         String code) { // throws LBException{
         Vector<String> v = new Vector<String>();
         try {
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             LexBIGServiceConvenienceMethods lbscm =
                 (LexBIGServiceConvenienceMethods) lbSvc
                     .getGenericExtension("LexBIGServiceConvenienceMethods");
             lbscm.setLexBIGService(lbSvc);
 
             CodingSchemeVersionOrTag csvt = new CodingSchemeVersionOrTag();
             csvt.setVersion(version);
             try {
                 lbscm.createCodeNodeSet(new String[] { code }, scheme, csvt)
                     .resolveToList(null, null, null, 1)
                     .getResolvedConceptReference(0).getEntityDescription()
                     .getContent();
 
             } catch (Exception e) {
             }
 
             // Iterate through all hierarchies and levels ...
             String[] hierarchyIDs = lbscm.getHierarchyIDs(scheme, csvt);
             for (int k = 0; k < hierarchyIDs.length; k++) {
                 String hierarchyID = hierarchyIDs[k];
                 AssociationList associations = null;
                 associations = null;
                 try {
                     associations =
                         lbscm.getHierarchyLevelNext(scheme, csvt, hierarchyID,
                             code, false, null);
                 } catch (Exception e) {
                     _logger.error("getSubconceptCodes "
                         + "Exception lbscm.getHierarchyLevelNext");
                     return v;
                 }
 
                 for (int i = 0; i < associations.getAssociationCount(); i++) {
                     Association assoc = associations.getAssociation(i);
                     AssociatedConceptList concepts =
                         assoc.getAssociatedConcepts();
                     for (int j = 0; j < concepts.getAssociatedConceptCount(); j++) {
                         AssociatedConcept concept =
                             concepts.getAssociatedConcept(j);
                         String nextCode = concept.getConceptCode();
                         v.add(nextCode);
                     }
                 }
             }
         } catch (Exception ex) {
             // ex.printStackTrace();
         }
         return v;
     }
 
     public Vector<String> getSuperconceptCodes(String scheme, String version,
         String code) { // throws LBException{
         long ms = System.currentTimeMillis();
         Vector<String> v = new Vector<String>();
         try {
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             LexBIGServiceConvenienceMethods lbscm =
                 (LexBIGServiceConvenienceMethods) lbSvc
                     .getGenericExtension("LexBIGServiceConvenienceMethods");
             lbscm.setLexBIGService(lbSvc);
             CodingSchemeVersionOrTag csvt = new CodingSchemeVersionOrTag();
             csvt.setVersion(version);
             try {
                 lbscm.createCodeNodeSet(new String[] { code }, scheme, csvt)
                     .resolveToList(null, null, null, 1)
                     .getResolvedConceptReference(0).getEntityDescription()
                     .getContent();
             } catch (Exception e) {
             }
 
             // Iterate through all hierarchies and levels ...
             String[] hierarchyIDs = lbscm.getHierarchyIDs(scheme, csvt);
             for (int k = 0; k < hierarchyIDs.length; k++) {
                 String hierarchyID = hierarchyIDs[k];
                 AssociationList associations =
                     lbscm.getHierarchyLevelPrev(scheme, csvt, hierarchyID,
                         code, false, null);
                 for (int i = 0; i < associations.getAssociationCount(); i++) {
                     Association assoc = associations.getAssociation(i);
                     AssociatedConceptList concepts =
                         assoc.getAssociatedConcepts();
                     for (int j = 0; j < concepts.getAssociatedConceptCount(); j++) {
                         AssociatedConcept concept =
                             concepts.getAssociatedConcept(j);
                         String nextCode = concept.getConceptCode();
                         v.add(nextCode);
                     }
                 }
             }
         } catch (Exception ex) {
             ex.printStackTrace();
         } finally {
             _logger
                 .debug("Run time (ms): " + (System.currentTimeMillis() - ms));
         }
         return v;
     }
 
     public Vector<String> getHierarchyAssociationId(String scheme,
         String version) {
 
         Vector<String> association_vec = new Vector<String>();
         try {
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
 
             // Will handle secured ontologies later.
             CodingSchemeVersionOrTag versionOrTag =
                 new CodingSchemeVersionOrTag();
             versionOrTag.setVersion(version);
             CodingScheme cs = lbSvc.resolveCodingScheme(scheme, versionOrTag);
             Mappings mappings = cs.getMappings();
             SupportedHierarchy[] hierarchies = mappings.getSupportedHierarchy();
             String[] ids = hierarchies[0].getAssociationNames();
 
             for (int i = 0; i < ids.length; i++) {
                 if (!association_vec.contains(ids[i])) {
                     association_vec.add(ids[i]);
                 }
             }
 
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return association_vec;
     }
 
     public static String getVocabularyVersionByTag(String codingSchemeName,
         String ltag) {
         if (codingSchemeName == null)
             return null;
         try {
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             CodingSchemeRenderingList lcsrl = lbSvc.getSupportedCodingSchemes();
             CodingSchemeRendering[] csra = lcsrl.getCodingSchemeRendering();
             for (int i = 0; i < csra.length; i++) {
                 CodingSchemeRendering csr = csra[i];
                 CodingSchemeSummary css = csr.getCodingSchemeSummary();
                 if (css.getFormalName().compareTo(codingSchemeName) == 0
                     || css.getLocalName().compareTo(codingSchemeName) == 0) {
                     if (ltag == null)
                         return css.getRepresentsVersion();
                     RenderingDetail rd = csr.getRenderingDetail();
                     CodingSchemeTagList cstl = rd.getVersionTags();
                     java.lang.String[] tags = cstl.getTag();
                     for (int j = 0; j < tags.length; j++) {
                         String version_tag = (String) tags[j];
                         if (version_tag.compareToIgnoreCase(ltag) == 0) {
                             return css.getRepresentsVersion();
                         }
                     }
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         _logger.debug("Version corresponding to tag " + ltag + " is not found "
             + " in " + codingSchemeName);
         return null;
     }
 
     public static Vector<String> getVersionListData(String codingSchemeName) {
 
         Vector<String> v = new Vector<String>();
         try {
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
             CodingSchemeRenderingList csrl = lbSvc.getSupportedCodingSchemes();
             if (csrl == null)
                 _logger.debug("csrl is NULL");
 
             CodingSchemeRendering[] csrs = csrl.getCodingSchemeRendering();
             for (int i = 0; i < csrs.length; i++) {
                 CodingSchemeRendering csr = csrs[i];
                 Boolean isActive =
                     csr.getRenderingDetail().getVersionStatus().equals(
                         CodingSchemeVersionStatus.ACTIVE);
                 if (isActive != null && isActive.equals(Boolean.TRUE)) {
                     CodingSchemeSummary css = csr.getCodingSchemeSummary();
                     String formalname = css.getFormalName();
                     if (formalname.compareTo(codingSchemeName) == 0) {
                         String representsVersion = css.getRepresentsVersion();
                         v.add(representsVersion);
                     }
                 }
             }
         } catch (Exception e) {
             MiscUtils.printException(_logger, e);
         }
         return v;
     }
 
     public static String getFileName(String pathname) {
         File file = new File(pathname);
         String filename = file.getName();
         return filename;
     }
 
     protected static Association processForAnonomousNodes(Association assoc) {
         // clone Association except associatedConcepts
         Association temp = new Association();
         temp.setAssociatedData(assoc.getAssociatedData());
         temp.setAssociationName(assoc.getAssociationName());
         temp.setAssociationReference(assoc.getAssociationReference());
         temp.setDirectionalName(assoc.getDirectionalName());
         temp.setAssociatedConcepts(new AssociatedConceptList());
 
         for (int i = 0; i < assoc.getAssociatedConcepts()
             .getAssociatedConceptCount(); i++) {
             // Conditionals to deal with anonymous nodes and UMLS top nodes
             // "V-X"
             // The first three allow UMLS traversal to top node.
             // The last two are specific to owl anonymous nodes which can act
             // like false
             // top nodes.
             if (assoc.getAssociatedConcepts().getAssociatedConcept(i)
                 .getReferencedEntry() != null
                 && assoc.getAssociatedConcepts().getAssociatedConcept(i)
                     .getReferencedEntry().getIsAnonymous() != null
                 && assoc.getAssociatedConcepts().getAssociatedConcept(i)
                     .getReferencedEntry().getIsAnonymous() != false
                 && !assoc.getAssociatedConcepts().getAssociatedConcept(i)
                     .getConceptCode().equals("@")
                 && !assoc.getAssociatedConcepts().getAssociatedConcept(i)
                     .getConceptCode().equals("@@")) {
                 // do nothing
             } else {
                 temp.getAssociatedConcepts().addAssociatedConcept(
                     assoc.getAssociatedConcepts().getAssociatedConcept(i));
             }
         }
         return temp;
     }
 
     // ==========================================================================
     public static LocalNameList vector2LocalNameList(Vector<String> v) {
         if (v == null)
             return null;
         LocalNameList list = new LocalNameList();
         for (int i = 0; i < v.size(); i++) {
             String vEntry = (String) v.elementAt(i);
             list.addEntry(vEntry);
         }
         return list;
     }
 
     protected static NameAndValueList createNameAndValueList(
         Vector<String> names, Vector<String> values) {
         if (names == null)
             return null;
         NameAndValueList nvList = new NameAndValueList();
         for (int i = 0; i < names.size(); i++) {
             String name = names.elementAt(i);
             String value = values.elementAt(i);
             NameAndValue nv = new NameAndValue();
             nv.setName(name);
             if (value != null) {
                 nv.setContent(value);
             }
             nvList.addNameAndValue(nv);
         }
         return nvList;
     }
 
     public static Vector<org.LexGrid.concepts.Concept> restrictToMatchingProperty(
         String codingSchemeName, String version, Vector<String> property_vec,
         Vector<String> source_vec, Vector<String> qualifier_name_vec,
         Vector<String> qualifier_value_vec, java.lang.String matchText,
         java.lang.String matchAlgorithm, java.lang.String language,
         int maxToReturn) {
 
         LocalNameList propertyList = vector2LocalNameList(property_vec);
         CodedNodeSet.PropertyType[] propertyTypes = null;
         LocalNameList sourceList = vector2LocalNameList(source_vec);
 
         NameAndValueList qualifierList =
             createNameAndValueList(qualifier_name_vec, qualifier_value_vec);
 
         return restrictToMatchingProperty(codingSchemeName, version,
             propertyList, propertyTypes, sourceList, qualifierList, matchText,
             matchAlgorithm, language, maxToReturn);
     }
 
     public static Vector<org.LexGrid.concepts.Concept> restrictToProperty(
         String codingSchemeName, String version, Vector<String> property_vec,
         Vector<String> source_vec, Vector<String> qualifier_name_vec,
         Vector<String> qualifier_value_vec, int maxToReturn) {
 
         LocalNameList propertyList = vector2LocalNameList(property_vec);
         CodedNodeSet.PropertyType[] propertyTypes = null;
         LocalNameList sourceList = vector2LocalNameList(source_vec);
         NameAndValueList qualifierList =
             createNameAndValueList(qualifier_name_vec, qualifier_value_vec);
         return restrictToProperty(codingSchemeName, version, propertyList,
             propertyTypes, sourceList, qualifierList, maxToReturn);
     }
 
     public static Vector<org.LexGrid.concepts.Concept> restrictToProperty(
         String codingSchemeName, String version,
 
         LocalNameList propertyList, CodedNodeSet.PropertyType[] propertyTypes,
         LocalNameList sourceList, NameAndValueList qualifierList,
         int maxToReturn) {
         CodedNodeSet cns = null;
         Vector<org.LexGrid.concepts.Concept> v =
             new Vector<org.LexGrid.concepts.Concept>();
         try {
             CodingSchemeVersionOrTag versionOrTag =
                 new CodingSchemeVersionOrTag();
             versionOrTag.setVersion(version);
 
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
 
             cns = lbSvc.getCodingSchemeConcepts(codingSchemeName, versionOrTag);
             if (cns == null)
                 return v;
 
             LocalNameList contextList = null;
             cns =
                 cns.restrictToProperties(propertyList, propertyTypes,
                     sourceList, contextList, qualifierList);
 
             LocalNameList restrictToProperties = new LocalNameList();
             SortOptionList sortCriteria =
                 Constructors.createSortOptionList(new String[] {
                     "matchToQuery", "code" });
 
             ResolvedConceptReferenceList list = null;
             try {
                 list =
                     cns.resolveToList(sortCriteria, restrictToProperties, null,
                         maxToReturn);
             } catch (Exception ex) {
                 throw new LBParameterException(ex.getMessage());
             }
 
             if (list == null)
                 return v;
             ResolvedConceptReference[] rcrArray =
                 list.getResolvedConceptReference();
             if (rcrArray == null) {
                 _logger
                     .warn("DLBWrapper getResolvedConceptReference returns null");
             }
 
             for (int i = 0; i < rcrArray.length; i++) {
                 ResolvedConceptReference rcr =
                     (ResolvedConceptReference) rcrArray[i];
                 v.add(rcr.getReferencedEntry());
             }
 
         } catch (Exception e) {
             e.printStackTrace();
             return v;
         }
         return v;
     }
 
     public static Vector<org.LexGrid.concepts.Concept> restrictToMatchingProperty(
         String codingSchemeName, String version, LocalNameList propertyList,
         CodedNodeSet.PropertyType[] propertyTypes, LocalNameList sourceList,
         NameAndValueList qualifierList, java.lang.String matchText,
         java.lang.String matchAlgorithm, java.lang.String language,
         int maxToReturn) {
         CodedNodeSet cns = null;
         Vector<org.LexGrid.concepts.Concept> v =
             new Vector<org.LexGrid.concepts.Concept>();
         try {
             CodingSchemeVersionOrTag versionOrTag =
                 new CodingSchemeVersionOrTag();
             versionOrTag.setVersion(version);
 
             LexBIGService lbSvc = RemoteServerUtil.createLexBIGService();
 
             if (lbSvc == null) {
                 _logger.error("lbSvc == null???");
                 return null;
             }
 
             cns = lbSvc.getCodingSchemeConcepts(codingSchemeName, versionOrTag);
             if (cns == null)
                 return v;
 
             LocalNameList contextList = null;
             cns =
                 cns.restrictToMatchingProperties(propertyList, propertyTypes,
                     sourceList, contextList, qualifierList, matchText,
                     matchAlgorithm, language);
 
             LocalNameList restrictToProperties = new LocalNameList();
             SortOptionList sortCriteria =
                 Constructors
                     .createSortOptionList(new String[] { "matchToQuery" });
 
             ResolvedConceptReferenceList list = null;
             try {
                 list =
                     cns.resolveToList(sortCriteria, restrictToProperties, null,
                         maxToReturn);
             } catch (Exception ex) {
                 throw new LBParameterException(ex.getMessage());
             }
 
             if (list == null)
                 return v;
             ResolvedConceptReference[] rcrArray =
                 list.getResolvedConceptReference();
             if (rcrArray == null) {
                 _logger.warn("getResolvedConceptReference returns null");
             }
 
             for (int i = 0; i < rcrArray.length; i++) {
                 ResolvedConceptReference rcr =
                     (ResolvedConceptReference) rcrArray[i];
                 v.add(rcr.getReferencedEntry());
             }
 
         } catch (Exception e) {
             e.printStackTrace();
             return v;
         }
 
         return toConcept(SortUtils.quickSort(v));
     }
 
     private static Vector<Concept> toConcept(Vector<?> v) {
         Iterator<?> iterator = v.iterator();
         Vector<Concept> newV = new Vector<Concept>();
         while (iterator.hasNext()) {
             Concept concept = (Concept) iterator.next();
             newV.add(concept);
         }
         return newV;
     }
 }
