 
 package gov.nih.nci.evs.reportwriter.bean;
 
 import java.io.File;
 
 import gov.nih.nci.evs.reportwriter.utils.DataUtils;
 import gov.nih.nci.evs.reportwriter.utils.SDKClientUtil;
 import gov.nih.nci.evs.reportwriter.service.StandardReportService;
 import gov.nih.nci.evs.reportwriter.properties.ReportWriterProperties;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 import java.util.HashSet;
 import java.util.Date;
 
 import javax.faces.context.FacesContext;
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.model.SelectItem;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 
 import java.util.Collection;
 
 import org.LexGrid.concepts.Concept;
 
 
 /**
   * <!-- LICENSE_TEXT_START -->
 * Copyright 2008,2009 NGIT. This software was developed in conjunction with the National Cancer Institute,
 * and so to the extent government employees are co-authors, any rights in such works shall be subject to Title 17 of the United States Code, section 105.
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the disclaimer of Article 3, below. Redistributions
 * in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 * 2. The end-user documentation included with the redistribution, if any, must include the following acknowledgment:
 * "This product includes software developed by NGIT and the National Cancer Institute."
 * If no such end-user documentation is to be included, this acknowledgment shall appear in the software itself,
 * wherever such third-party acknowledgments normally appear.
 * 3. The names "The National Cancer Institute", "NCI" and "NGIT" must not be used to endorse or promote products derived from this software.
 * 4. This license does not authorize the incorporation of this software into any third party proprietary programs. This license does not authorize
 * the recipient to use any trademarks owned by either NCI or NGIT
 * 5. THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER INSTITUTE,
 * NGIT, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
   * <!-- LICENSE_TEXT_END -->
   */
 
 /**
   * @author EVS Team
   * @version 1.0
   *
   * Modification history
   *     Initial implementation kim.ong@ngc.com
   *
  */
 
 public class UserSessionBean extends Object
 {
 	  private static Logger KLO_log = Logger.getLogger("UserSessionBean KLO");
 
 	  Boolean isAdmin = null;
 	  String selectedTask = null;
 
 	  private List standardReportTemplateList = new ArrayList();
 	  private List standardReportTemplateList_draft = new ArrayList(); // for templates with reports already been generated
 	  private List standardReportTemplateList_approved = new ArrayList(); // for templates with reports already been generated
 
 	  private String selectedStandardReportTemplate = null;
 	  private String selectedStandardReportTemplate_draft = null;
 	  private String selectedStandardReportTemplate_approved = null;
 
 	  private String selectedPropertyType = null;
 	  private List propertyTypeList = new ArrayList();
 
 	  private String rootConceptCode = null;
 	  private String selectedOntology = null;
 
 	  private String selectedReportStatus = null;
       private List reportStatusList = null;
 	  private Vector<String> reportStatusListData = null;
 
 	  private String selectedReportFormat = null;
       private List reportFormatList = null;
 	  private Vector<String> reportFormatListData = null;
 
 
 	  public void setIsAdmin(Boolean bool_obj) {
 		  this.isAdmin = bool_obj;
 	  }
 
       public Boolean getIsAdmin() {
 		  return this.isAdmin;
 	  }
 
 	  public String getSelectedTask() {
 		  return this.selectedTask;
 	  }
 
 	  public void setSelectedTask(String selectedTask) {
 		  this.selectedTask = selectedTask;
 	  }
 
 	  public String getSelectedPropertyType() {
 		  return this.selectedPropertyType;
 	  }
 
 	  public void setSelectedPropertyType(String selectedPropertyType) {
 		  this.selectedPropertyType = selectedPropertyType;
 		  HttpServletRequest request = getHttpRequest();
 		  request.getSession().setAttribute("selectedPropertyType", selectedPropertyType);
 	  }
 
 
 	  public void propertyTypeSelectionChanged(ValueChangeEvent event) {
 		  if (event.getNewValue() == null) return;
 		  String newValue = (String) event.getNewValue();
 		  setSelectedPropertyType(newValue);
 	  }
 
 
 	  public String getSelectedOntology() {
 		  return this.selectedOntology;
 	  }
 
 	  public void setSelectedOntology(String selectedOntology) {
 		  this.selectedOntology = selectedOntology;
 	  }
 
 	  public HttpServletRequest getHttpRequest() {
 		  FacesContext context = FacesContext.getCurrentInstance();
 		  HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
 		  return request;
 	  }
 
 	  public List getTaskList() {
 		  HttpServletRequest request = getHttpRequest();
 		  HttpSession session = request.getSession(false);
 
 		  Boolean isAdmin = null;
 		  if (session != null) {
 			  isAdmin = (Boolean) request.getSession(true).getAttribute("isAdmin");
 		  }
 
 		  List list = DataUtils.getTaskList(isAdmin);
 		  if (list != null) {
 		      SelectItem item = (SelectItem) list.get(0);
 		      selectedTask = item.getLabel();
 		  }
 		  //return DataUtils.getTaskList(isAdmin);
 		  return list;
 	  }
 
 
 	  public List getPropertyTypeList() {
 			List list = DataUtils.getPropertyTypeList();
 			if (selectedPropertyType == null) {
 				SelectItem item = (SelectItem) list.get(0);
 				selectedPropertyType = item.getLabel();
 			}
 			return list;
 	  }
 
 
 	  public void changeTaskSelection(ValueChangeEvent vce) {
 		  String newValue = (String)vce.getNewValue();
  		  //KLO_log.warn("============== changeTaskSelection " + newValue);
 		  selectedTask = newValue;
 	  }
 
 
 	  public void reportSelectionChanged(ValueChangeEvent vce) {
 		  String newValue = (String) vce.getNewValue();
           setSelectedStandardReportTemplate(newValue);
 	  }
 
 	  public void reportSelectionChanged_draft(ValueChangeEvent vce) {
 		  String newValue = (String) vce.getNewValue();
           setSelectedStandardReportTemplate_draft(newValue);
 	  }
 
 	  public void reportSelectionChanged_approved(ValueChangeEvent vce) {
 		  String newValue = (String) vce.getNewValue();
           setSelectedStandardReportTemplate_approved(newValue);
 	  }
 
 	  public List getStandardReportTemplateList() {
 
 			List list = DataUtils.getStandardReportTemplateList();
 			if (selectedStandardReportTemplate == null)
 			{
 			    if (list != null && list.size() > 0)
 			    {
 					if (getSelectedStandardReportTemplate() == null)
 					{
 						SelectItem item = (SelectItem) list.get(0);
 						setSelectedStandardReportTemplate(item.getLabel());
 				    }
 			    }
 			}
 		    return list;
 	  }
 
 
 	  public String getSelectedStandardReportTemplate() {
 		  return this.selectedStandardReportTemplate;
 	  }
 
 
 	  public void setSelectedStandardReportTemplate(String selectedStandardReportTemplate) {
 		  this.selectedStandardReportTemplate = selectedStandardReportTemplate;
 		  HttpServletRequest request = getHttpRequest();
 		  request.getSession().setAttribute("selectedStandardReportTemplate", selectedStandardReportTemplate);
 	  }
 
 
 	  public List getStandardReportTemplateList_draft() {
           // Find all templates with reports already been generated
           List list = new ArrayList();
           HashSet hset = new HashSet();
           try{
         	  SDKClientUtil sdkclientutil = new SDKClientUtil();
 			  StandardReportTemplate standardReportTemplate = null;
 			  String FQName = "gov.nih.nci.evs.reportwriter.bean.StandardReport";
 			  Object[] objs = sdkclientutil.search(FQName);
 
 
 			  if (objs != null && objs.length > 0) {
 				  for (int i=0; i<objs.length; i++)
 				  {
 					  StandardReport standardReport = (StandardReport) objs[i];
 					  ReportStatus rs = standardReport.getStatus();
 					  String status = rs.getLabel();
 					  standardReportTemplate = standardReport.getTemplate();
 					  if (standardReportTemplate != null)
 					  {
 						  if (status.compareToIgnoreCase("DRAFT") == 0) {
 							  if (!hset.contains(standardReportTemplate.getLabel())) {
 								  hset.add(standardReportTemplate.getLabel());
 								  list.add(new SelectItem(standardReportTemplate.getLabel()));
 							  }
 						  }
 				      }
 				  }
 
 				  if (list != null && list.size() > 0)
 				  {
 					  SelectItem item = (SelectItem) list.get(0);
 					  setSelectedStandardReportTemplate_draft(item.getLabel());
 				  }
 
 		      }
 		  } catch (Exception ex) {
 			  ex.printStackTrace();
 		  }
 		  return list;
 	  }
 
 
 	  public String getSelectedStandardReportTemplate_draft() {
 		  return this.selectedStandardReportTemplate_draft;
 	  }
 
 
 	  public void setSelectedStandardReportTemplate_draft(String selectedStandardReportTemplate_draft) {
 		  this.selectedStandardReportTemplate_draft = selectedStandardReportTemplate_draft;
 		  HttpServletRequest request = getHttpRequest();
 		  request.getSession().setAttribute("selectedStandardReportTemplate_draft", selectedStandardReportTemplate_draft);
 	  }
 
 
 
 	  public List getStandardReportTemplateList_approved() {
           List list = new ArrayList();
           HashSet hset = new HashSet();
           try{
         	  SDKClientUtil sdkclientutil = new SDKClientUtil();
 			  StandardReportTemplate standardReportTemplate = null;
 			  String FQName = "gov.nih.nci.evs.reportwriter.bean.StandardReport";
 			  Object[] objs = sdkclientutil.search(FQName);
 			  if (objs != null && objs.length > 0) {
 				  for (int i=0; i<objs.length; i++)
 				  {
 					  StandardReport standardReport = (StandardReport) objs[i];
 					  ReportStatus rs = standardReport.getStatus();
 					  String status = rs.getLabel();
 					  if (status.compareTo("APPROVED") == 0) {
 						  standardReportTemplate = standardReport.getTemplate();
                           if (!hset.contains(standardReportTemplate.getLabel())) {
 							  hset.add(standardReportTemplate.getLabel());
                           	  list.add(new SelectItem(standardReportTemplate.getLabel()));
 						  }
 					  }
 				  }
 
 				  if (list == null)
 				  {
 					  if (list != null && list.size() > 0)
 					  {
 						  SelectItem item = (SelectItem) list.get(0);
 						  setSelectedStandardReportTemplate_approved(item.getLabel());
 					  }
 				  }
 		      }
 		  } catch (Exception ex) {
 			  ex.printStackTrace();
 		  }
 		  return list;
 	  }
 
 
 	  public String getSelectedStandardReportTemplate_approved() {
 		  return this.selectedStandardReportTemplate_approved;
 	  }
 
 
 	  public void setSelectedStandardReportTemplate_approved(String selectedStandardReportTemplate_draft) {
 		  this.selectedStandardReportTemplate_approved = selectedStandardReportTemplate_approved;
 		  HttpServletRequest request = getHttpRequest();
 		  request.getSession().setAttribute("selectedStandardReportTemplate_approved", selectedStandardReportTemplate_approved);
 	  }
 
 	  //taskSelectionChanged
 	  public void taskSelectionChanged(ValueChangeEvent event) {
 		  if (event.getNewValue() == null) return;
 		  String task = (String) event.getNewValue();
 		  setSelectedTask(task);
 	  }
 
 
 	  public String performTask() {
 		  if (this.selectedTask.compareTo("Administer Standard Reports") == 0)
 		  {
 			  List list = getStandardReportTemplateList();
 			  if (list == null || list.size() == 0)
 			  {
 				  return "add_standard_report_template";
 			  }
 			  return "administer_standard_reports";
 		  }
 		  else if (this.selectedTask.compareTo("Maintain Report Status") == 0)
 		      return "report_status";
 
 		  else if (this.selectedTask.compareTo("Assign Report Status") == 0) {
 		      // Check if there is any DRAFT report waiting for approval:
               standardReportTemplateList_draft = getStandardReportTemplateList_draft();
               if (standardReportTemplateList_draft != null && standardReportTemplateList_draft.size() > 0)
               {
 				  return "assign_report_status";
 			  }
 			  else
 			  {
 				  String message = "No draft report is found. ";
 				  HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
 				  request.getSession().setAttribute("message", message);
 				  return "message";
 			  }
 
 	      } else if (this.selectedTask.compareTo("Retrieve Standard Reports") == 0) {
 			  HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
 			  Boolean isAdmin = (Boolean) request.getSession().getAttribute("isAdmin");
 			  if (isAdmin != null && isAdmin.equals(Boolean.TRUE))
 			  {
 				  return "retrieve_standard_reports";
 			  }
 			  else
 			  {
 				  // Check if there is any APPROVED report waiting for approval:
 				  standardReportTemplateList_approved = getStandardReportTemplateList_approved();
 				  if (standardReportTemplateList_approved != null && standardReportTemplateList_approved.size() > 0)
 				  {
 					  return "retrieve_standard_reports";
 				  }
 				  else
 				  {
 					  String message = "No approved report is found. ";
 					  request.getSession().setAttribute("message", message);
 					  return "message";
 				  }
 		      }
 		  }
 		  return null;
 	  }
 
 
 	  public String addColumnAction() {
 		  //KLO_log.warn("******************************************* addColumnAction() ");
 		  // add_standard_report_column.jsp
 		  return "add_standard_report_column";
 	  }
 
 
 
 	  public String modifyColumnAction() {
           // not functional, to be modifid
           // need to track coding scheme
           // need to populate selected report_column data
 		  return "add_standard_report_column";
 	  }
 
 
 	  public String insertbeforeColumnAction() {
           // not functional, to be modifid
           // need to track coding scheme
           // track selected column number
 		  return "add_standard_report_column";
 	  }
 
 
 	  public String insertafterColumnAction() {
           // not functional, to be modifid
           // need to track coding scheme
           // track selected column number
 		  return "add_standard_report_column";
 	  }
 
 	  public String deleteColumnAction() {
           // not functional, to be modifid
           // need to track coding scheme
           // track selected column number
 
           //selectedcolumn
 		  HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
           String id_str = (String) request.getParameter("selectedcolumn");
           int id = Integer.parseInt(id_str);
 
 System.out.println("deleting column with ID = " + id + " (yet to be implemented)" );
 
           try{
 			  ReportColumn reportColumn = getReportColumn(id);
         	  SDKClientUtil sdkclientutil = new SDKClientUtil();
   			  sdkclientutil.deleteReportColumn(reportColumn);
   			  //setSelectedStandardReportTemplate(label);
 
           } catch(Exception e) {
         	  e.printStackTrace();
           }
 
 		  return "standard_report_column";
 	  }
 
 
 	  public String getRootConceptCode() {
 		  return this.rootConceptCode;
 	  }
 
 
 	  public void setRootConceptCode(String rootConceptCode) {
 		  if (rootConceptCode == null) return;
 		  this.rootConceptCode = rootConceptCode;
 	  }
 
 /*
 	  public String selectFileAction() {
           // pop-up file selection dialog box (JNLP.jar)
           // update selectedFile
           // show selectedFile to the user
 
 		  return "generate_standard_report";
 	  }
 */
 
       //public String addReportAction() {
 	  public String saveTemplateAction() {
 		  HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
           String codingSchemeNameAndVersion = (String) request.getSession().getAttribute("selectedOntology");
           String codingSchemeName = DataUtils.getCodingSchemeName(codingSchemeNameAndVersion);
           String codingSchemeVersion = DataUtils.getCodingSchemeVersion(codingSchemeNameAndVersion);
           String label = (String) request.getParameter("label");
 
           String rootConceptCode = (String) request.getParameter("rootConceptCode");
           String selectedAssociation = (String) request.getSession().getAttribute("selectedAssociation");
 		  String selectedLevel = (String) request.getSession().getAttribute("selectedLevel");
 		  //String selectedDirection = (String) request.getSession().getAttribute("selectedDirection");
 
 		  String direction_str = (String) request.getParameter("direction");
 
 		  Boolean direction = null;
 
 		  // return to error page
 		  if (label == null || label.compareTo("") == 0)
 		  {
 			  KLO_log.warn("Incomplete data entry -- form not saved.");
 			  return "add_standard_report_template";
 		  }
 		  if (rootConceptCode == null || rootConceptCode.compareTo("") == 0)
 		  {
 			  KLO_log.warn("Incomplete data entry -- form not saved.");
 			  return "add_standard_report_template";
 		  }
 		  if (selectedLevel == null || selectedLevel.compareTo("") == 0)
 		  {
 			  KLO_log.warn("Incomplete data entry -- form not saved.");
 			  return "add_standard_report_template";
 		  }
 
 /*
 		  if(selectedDirection.equals(Boolean.TRUE))
 			  direction = true;
 		  else
 			  direction = false;
 */
           //KLO
 		  if(direction_str.compareToIgnoreCase("source") == 0)
 			  direction = Boolean.FALSE;
 		  else
 			  direction = Boolean.TRUE;
 
 		  char delimiter = '$';
 
 		  KLO_log.warn("label: " + label);
           KLO_log.warn("codingSchemeName: " + codingSchemeName);
           KLO_log.warn("codingSchemeVersion: " + codingSchemeVersion);
           KLO_log.warn("rootConceptCode: " + rootConceptCode);
           KLO_log.warn("associationname: " + selectedAssociation);
 
           KLO_log.warn("direction: " + direction);
 
           KLO_log.warn("level: " + selectedLevel);
           KLO_log.warn("delimiter: " + delimiter);
 
           // Save results using SDK writable API.
 
           try{
         	  SDKClientUtil sdkclientutil = new SDKClientUtil();
 
 				StandardReportTemplate standardReportTemplate = null;
 				String FQName = "gov.nih.nci.evs.reportwriter.bean.StandardReportTemplate";
 				String methodName = "setLabel";
 				String key = label;
 				Object standardReportTemplate_obj = sdkclientutil.search(FQName, methodName, key);
 				if (standardReportTemplate_obj != null) {
 					  String message = "Unable to save -- the report template with the specified label, " + label + ", already exists. ";
 					  request.getSession().setAttribute("message", message);
 					  return "message";
 				}
 
 
         	  if(selectedLevel.equalsIgnoreCase("all"))
         	  {
         		  selectedLevel = "-1";
 			  }
   			  sdkclientutil.insertStandardReportTemplate(label, codingSchemeName, codingSchemeVersion, rootConceptCode, selectedAssociation, direction, Integer.parseInt(selectedLevel), delimiter);
   			  setSelectedStandardReportTemplate(label);
           } catch(Exception e) {
         	  e.printStackTrace();
           }
 
           //return "generate_standard_report";
           return "standard_report_template";
 	  }
 
 
 
 	  public String saveModifiedTemplateAction() {
 		  HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
 		  String label = (String) request.getSession().getAttribute("selectedStandardReportTemplate");
 
           String codingscheme = (String) request.getParameter("codingscheme");
           String version = (String) request.getParameter("version");
 
 System.out.println("saveModifiedTemplateAction: codingscheme " + codingscheme);
 System.out.println("saveModifiedTemplateAction: version " + version);
 
 
 
           Boolean csnv_valid = DataUtils.validateCodingScheme(codingscheme, version);
           if (csnv_valid == null || csnv_valid.equals(Boolean.FALSE))
           {
 			  String message = "Invalid coding scheme name " + codingscheme + " or version " + version + " -- The report template may be out of date. Please modify it and resubmit.";
 			  request.getSession().setAttribute("message", message);
 			  return "message";
 		  }
 
           String rootConceptCode = (String) request.getParameter("rootConceptCode");
           String ltag = null;
 	      Concept rootConcept = DataUtils.getConceptByCode(codingscheme, version, ltag, rootConceptCode);
 	      if (rootConcept == null)
 	      {
 			  String message = "Invalid root concept code " + rootConceptCode + " -- Please modify the report template and resubmit.";
 			  request.getSession().setAttribute("message", message);
 			  return "message";
 		  }
 
           String associationName = (String) request.getParameter("associationName");
           String key = codingscheme + " (version: " + version + ")";
           Vector<String> associationname_vec = DataUtils.getSupportedAssociationNames(key);
           if (!associationname_vec.contains(associationName)) {
 			  String message = "Invalid association name " + associationName + " -- Please modify the report template and resubmit.";
 			  request.getSession().setAttribute("message", message);
 			  return "message";
 		  }
 
           String direction_str = (String) request.getParameter("direction");
           Boolean direction = null;
           if (direction_str.compareTo("source") == 0)
              direction = Boolean.FALSE;
           else
              direction = Boolean.TRUE;
 
           String level_str = (String) request.getParameter("level");
 
  		  // return to error page
  		  if (label == null || label.compareTo("") == 0)
  		  {
  			  KLO_log.warn("Incomplete data entry -- form not saved.");
  			  return "modify_standard_report_template";
  		  }
  		  if (rootConceptCode == null || rootConceptCode.compareTo("") == 0)
  		  {
  			  KLO_log.warn("Incomplete data entry -- form not saved.");
  			  return "modify_standard_report_template";
  		  }
  		  if (level_str == null || level_str.compareTo("") == 0)
  		  {
  			  KLO_log.warn("Incomplete data entry -- form not saved.");
  			  return "modify_standard_report_template";
 		  }
 
           Integer level;
           if(level_str.equalsIgnoreCase("all"))
           {
 			  level = -1;
 		  }
 		  else
 		  {
 			  level = Integer.valueOf(level_str);
 		  }
 
 	      if (level < -1)
 	      {
 			  String message = "Invalid level " + level + " -- Please modify the report template and resubmit.";
 			  request.getSession().setAttribute("message", message);
 			  return "message";
 		  }
 
 		  //char delimiter = '$';
 
           try{
         	  SDKClientUtil sdkclientutil = new SDKClientUtil();
 
 			  StandardReportTemplate standardReportTemplate = null;
 			  String FQName = "gov.nih.nci.evs.reportwriter.bean.StandardReportTemplate";
 			  String methodName = "setLabel";
 			  key = label;
 
 			  Object standardReportTemplate_obj = sdkclientutil.search(FQName, methodName, key);
 			  if (standardReportTemplate_obj == null) {
 				  String message = "Unable to update template -- the report template with the specified label, " + label + " is not found. ";
 				  request.getSession().setAttribute("message", message);
 				  return "message";
 			  }
 
 			  standardReportTemplate = (StandardReportTemplate) standardReportTemplate_obj;
 			  standardReportTemplate.setLabel(label);
 			  standardReportTemplate.setCodingSchemeName(codingscheme);
 			  standardReportTemplate.setCodingSchemeVersion(version);
 			  standardReportTemplate.setRootConceptCode(rootConceptCode);
 			  standardReportTemplate.setAssociationName(associationName);
 			  standardReportTemplate.setDirection(direction);
 			  standardReportTemplate.setLevel(level);
  			  sdkclientutil.updateStandardReportTemplate(standardReportTemplate);
 
  			  key = codingscheme + " (version: " + version + ")";
  			  request.getSession().setAttribute("selectedOntology", key);
 
           } catch(Exception e) {
         	  e.printStackTrace();
           }
 
           return "standard_report_template";
 	  }
 
 
 
 
 	  public String deleteReportTemplateAction() {
 		  HttpServletRequest request = getHttpRequest();
 		  String template_label = (String) request.getSession().getAttribute("selectedStandardReportTemplate");
 
 		  KLO_log.warn("deleteReportTemplateAction: " + template_label);
 
           try{
 			  StandardReportTemplate template = getStandardReportTemplate(template_label);
         	  SDKClientUtil sdkclientutil = new SDKClientUtil();
   			  sdkclientutil.deleteStandardReportTemplate(template);
 
   			  //setSelectedStandardReportTemplate(label);
   			  List list = getStandardReportTemplateList();
 
           } catch(Exception e) {
         	  e.printStackTrace();
           }
 
 		  return "standard_report_template";
 	  }
 
 
 
 
 
       public StandardReportTemplate getStandardReportTemplate(String label) {
           try{
         	  SDKClientUtil sdkclientutil = new SDKClientUtil();
         	  String FQName = "gov.nih.nci.evs.reportwriter.bean.StandardReportTemplate";
         	  String methodName = "setLabel";
         	  Object obj = sdkclientutil.search(FQName, methodName, label);
 			  StandardReportTemplate standardReportTemplate = (StandardReportTemplate) obj;
 			  return standardReportTemplate;
           } catch(Exception e) {
         	  e.printStackTrace();
           }
           return null;
       }
 
 
       public ReportColumn getReportColumn(int id) {
           try{
         	  SDKClientUtil sdkclientutil = new SDKClientUtil();
         	  String FQName = "gov.nih.nci.evs.reportwriter.bean.ReportColumn";
         	  String methodName = "setId";
         	  Object obj = sdkclientutil.search(FQName, methodName, id);
 			  ReportColumn reportColumn = (ReportColumn) obj;
 			  return reportColumn;
           } catch(Exception e) {
         	  e.printStackTrace();
           }
           return null;
       }
 
 
       //public String addReportColumnAction() {
 	  public String saveReportColumnAction() {
 		  HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
           StandardReportTemplate standardReportTemplate = null;
           standardReportTemplate = getStandardReportTemplate(this.selectedStandardReportTemplate);
           if (standardReportTemplate == null)
           {
 			  String message = "ERROR saving ReportColumn -- Unable to identify report template " + this.selectedStandardReportTemplate;
 			  request.getSession().setAttribute("message", message);
 			  return "message";
 		  }
 
 		  String fieldlabel = (String) request.getParameter("fieldlabel");
 		  String columnNumber_str = (String) request.getParameter("columnNumber");
 
   		  String fieldType = (String) request.getSession().getAttribute("selectedDataCategory");
   		  String propertyType = (String) request.getSession().getAttribute("selectedPropertyType");
   		  String propertyName = (String) request.getSession().getAttribute("selectedPropertyName");
   		  String representationalForm = (String) request.getSession().getAttribute("selectedRepresentationalForm");
 		  String source = (String) request.getSession().getAttribute("selectedSource");
   		  String propertyQualifier = (String) request.getSession().getAttribute("selectedPropertyQualifier");
   		  String qualifierValue = (String) request.getParameter("qualifiervalue");
   		  String conditionalColumnId = (String) request.getParameter("dependentfield");
 
 		  if (columnNumber_str == null || fieldlabel == null)
 		  {
 			  String message = "Unable to save ReportColumn -- please complete data entry.";
 			  request.getSession().setAttribute("message", message);
 			  return "message";
 		  }
 		  columnNumber_str = columnNumber_str.trim();
 		  fieldlabel = fieldlabel.trim();
 		  if (columnNumber_str.length() == 0 || fieldlabel.length() == 0)
 		  {
 			  String message = "Unable to save ReportColumn -- please complete data entry.";
 			  request.getSession().setAttribute("message", message);
 			  return "message";
 		  }
 
 		  int columnNumber = Integer.parseInt(columnNumber_str);
   		  int ccid = -1;
   		  if(conditionalColumnId != null && conditionalColumnId != "")
   		  {
   			  ccid = Integer.parseInt(conditionalColumnId);
 		  }
 
 		  String preferred = (String) request.getParameter("preferred");
   		  Boolean isPreferred = null;
   		  if(preferred != null) {
 			  if(preferred.equalsIgnoreCase("yes"))
 				  isPreferred = Boolean.TRUE;
 			  if(preferred.equalsIgnoreCase("no"))
 				  isPreferred = Boolean.FALSE;
   		  }
 
 		  String delim = (String) request.getSession().getAttribute("selectedDelimiter");
 		  char delimiter = ' ';
 		  if(delim != null)
 		  {
 			  if(delim.length() > 0)
 			  {
 				  delimiter = delim.charAt(0);
 			  }
 		  }
 
 
 System.out.println("columnNumber: " + columnNumber);
 System.out.println("fieldlabel: " + fieldlabel);
 System.out.println("fieldType: " + fieldType);
 System.out.println("propertyType: " + propertyType);
 System.out.println("propertyName: " + propertyName);
 System.out.println("isPreferred: " + isPreferred);
 System.out.println("representationalForm: " + representationalForm);
 System.out.println("source: " + source);
 System.out.println("propertyQualifier: " + propertyQualifier);
 System.out.println("qualifierValue: " + qualifierValue);
 System.out.println("delim: " + delim);
 
 
           // Save results using SDK writable API.
           try {
         	  SDKClientUtil sdkclientutil = new SDKClientUtil();
               // check duplicate column number and column label
 			  java.util.Collection cc = standardReportTemplate.getColumnCollection();
 			  if (cc != null) {
 				  Object[] objs = cc.toArray();
 				  if (objs.length > 0) {
 			         for(int i=0; i<objs.length; i++) {
 						 gov.nih.nci.evs.reportwriter.bean.ReportColumn c = (gov.nih.nci.evs.reportwriter.bean.ReportColumn) objs[i];
 						 String col_label = c.getLabel();
 						 if (col_label.compareToIgnoreCase(fieldlabel) == 0)
 						 {
 							  String message = "Unable to save ReportColumn -- the column label already exists.";
 							  request.getSession().setAttribute("message", message);
 							  return "message";
 						 }
 						 Integer col_num = c.getColumnNumber();
 						 if (col_num.intValue() == columnNumber)
 						 {
 							  String message = "Unable to save ReportColumn -- the column number already exists.";
 							  request.getSession().setAttribute("message", message);
 							  return "message";
 						 }
 					 }
 			      }
 		      }
 
         	  ReportColumn col = sdkclientutil.createReportColumn(fieldlabel, columnNumber, fieldType, propertyType, propertyName, isPreferred, representationalForm, source, propertyQualifier, qualifierValue, delimiter, ccid);
 			  col.setReportTemplate(standardReportTemplate);
               sdkclientutil.insertReportColumn(col);
 			  System.out.println("completed insertReportColumn: ");
 
               request.getSession().setAttribute("selectedStandardReportTemplate", selectedStandardReportTemplate);
 
 
           } catch (Exception e) {
         	  e.printStackTrace();
           }
           return "standard_report_column";
 	  }
 
 
 
 	public List getReportFormatList() {
 		reportFormatListData = DataUtils.getReportFormatListData();
 		reportFormatList = new ArrayList();
 		for (int i=0; i<reportFormatListData.size(); i++) {
 			String t = (String) reportFormatListData.elementAt(i);
 			reportFormatList.add(new SelectItem(t));
 		}
 		if (reportFormatList != null && reportFormatList.size() > 0) {
 			selectedReportFormat = ((SelectItem) reportFormatList.get(0)).getLabel();
 		}
 
 		return reportFormatList;
 	}
 
 	public void setSelectedReportFormat(String selectedReportFormat) {
 		this.selectedReportFormat = selectedReportFormat;
 		HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
 		request.getSession().setAttribute("selectedReportFormat", selectedReportFormat);
 	}
 
 	public String getSelectedReportFormat() {
 		return this.selectedReportFormat;
 	}
 
 	public void ReportFormatSelectionChanged(ValueChangeEvent event) {
 		if (event.getNewValue() == null) return;
 		setSelectedReportFormat(selectedReportFormat);
 	}
 
 
 	public List getReportStatusList() {
 		reportStatusListData = DataUtils.getReportStatusListData();
 		reportStatusList = new ArrayList();
 		for (int i=0; i<reportStatusListData.size(); i++) {
 			String t = (String) reportStatusListData.elementAt(i);
 			reportStatusList.add(new SelectItem(t));
 		}
 		if (reportStatusList != null && reportStatusList.size() > 0) {
 			selectedReportStatus = ((SelectItem) reportStatusList.get(0)).getLabel();
 		}
 
 		return reportStatusList;
 	}
 
 	public void setSelectedReportStatus(String selectedReportStatus) {
 		this.selectedReportStatus = selectedReportStatus;
 		HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
 		request.getSession().setAttribute("selectedReportStatus", selectedReportStatus);
 	}
 
 	public String getSelectedReportStatus() {
 		return this.selectedReportStatus;
 	}
 
 	public void reportStatusSelectionChanged(ValueChangeEvent event) {
 		if (event.getNewValue() == null) return;
 		setSelectedReportStatus(selectedReportStatus);
 	}
 
 
 
 
 
     public String addStatusAction() {
 		// to be modified
 		HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
 		String statusValue = (String) request.getParameter("statusValue");
 		// save to database
 
 		return "report_status";
 	}
 
     public String assignStatusAction() {
 		HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
 		// save to database
 		String reportTemplate = (String) request.getSession().getAttribute("selectedStandardReportTemplate_draft");
 		String statusValue = (String) request.getSession().getAttribute("selectedReportStatus");
 
         try{
             SDKClientUtil sdkclientutil = new SDKClientUtil();
 		    StandardReportTemplate standardReportTemplate = null;
 		    String FQName = "gov.nih.nci.evs.reportwriter.bean.StandardReport";
 		    Object[] objs = sdkclientutil.search(FQName);
 		    if (objs != null && objs.length > 0) {
 			    for (int i=0; i<objs.length; i++)
 			    {
 			   	    StandardReport standardReport = (StandardReport) objs[i];
 			   	    standardReportTemplate = standardReport.getTemplate();
 			   	    if (standardReportTemplate != null)
 			   	    {
 						if (reportTemplate.compareTo(standardReportTemplate.getLabel()) == 0)
 						{
 							FQName = "gov.nih.nci.evs.reportwriter.bean.ReportStatus";
 							String methodName = "setLabel";
 							String key = statusValue;
 
 							Object status_obj = sdkclientutil.search(FQName, methodName, key);
 							if (status_obj != null)
 							{
 							    standardReport.setStatus((ReportStatus) status_obj);
 								java.util.Date lastModified = new Date(); // system date
                                 standardReport.setLastModified(lastModified);
 							    sdkclientutil.updateStandardReport(standardReport);
 							}
 						}
 					}
 				}
 			}
         } catch(Exception e) {
       	    e.printStackTrace();
         }
 
         standardReportTemplateList_draft = getStandardReportTemplateList_draft();
 
 		return "assign_report_status";
 	}
 
 
 	  public String modifyReportTemplateAction() {
 		  HttpServletRequest request = getHttpRequest();
 		  String templateLabel = (String) request.getSession().getAttribute("selectedStandardReportTemplate");
 
 		  // find thesaurus name through template
           try{
         	  SDKClientUtil sdkclientutil = new SDKClientUtil();
 
 System.out.println(	"modifyReportTemplateAction" + " " + templateLabel );
 
 
 				StandardReportTemplate standardReportTemplate = null;
 				String FQName = "gov.nih.nci.evs.reportwriter.bean.StandardReportTemplate";
 				String methodName = "setLabel";
 				String key = templateLabel;
 				Object standardReportTemplate_obj = sdkclientutil.search(FQName, methodName, key);
 				if (standardReportTemplate_obj != null) {
 					standardReportTemplate = (StandardReportTemplate) standardReportTemplate_obj;
 
 System.out.println(	"modifyReportTemplateAction" + " " + standardReportTemplate.getCodingSchemeName() );
 
 					versionList = getVersionList(standardReportTemplate.getCodingSchemeName());
 
 					  //StandardReportTemplate standardReportTemplate = getStandardReportTemplate(selectedStandardReportTemplate);
 					  String ontologyNameAndVersion = standardReportTemplate.getCodingSchemeName() + " (version: " + standardReportTemplate.getCodingSchemeVersion() + ")";
 					  request.getSession().setAttribute("selectedOntology", ontologyNameAndVersion);
 				}
 		  } catch (Exception ex) {
 			  String message = "Unable to construct available coding scheme version list.";
 			  request.getSession().setAttribute("message", message);
 			  return "message";
 		  }
 
 
 
 		  return "modify_standard_report_template";
 	  }
 
 	  public String editReportContentAction() {
 		  HttpServletRequest request = getHttpRequest();
 		  request.getSession().setAttribute("selectedStandardReportTemplate", selectedStandardReportTemplate);
 
           StandardReportTemplate standardReportTemplate = getStandardReportTemplate(selectedStandardReportTemplate);
           String ontologyNameAndVersion = standardReportTemplate.getCodingSchemeName() + " (version: " + standardReportTemplate.getCodingSchemeVersion() + ")";
           request.getSession().setAttribute("selectedOntology", ontologyNameAndVersion);
 
 		  return "standard_report_column";
 	  }
 
 
 	  public String generateStandardReportAction() {
 		  HttpServletRequest request = getHttpRequest();
           String templateId = (String) request.getSession().getAttribute("selectedStandardReportTemplate");
 
 System.out.println("generateStandardReportAction: " +  templateId);
 
           try{
         	    SDKClientUtil sdkclientutil = new SDKClientUtil();
 				StandardReportTemplate standardReportTemplate = null;
 				String FQName = "gov.nih.nci.evs.reportwriter.bean.StandardReportTemplate";
 				String methodName = "setLabel";
 				String key = templateId;
 				Object standardReportTemplate_obj = sdkclientutil.search(FQName, methodName, key);
 				if (standardReportTemplate_obj != null) {
 					standardReportTemplate = (StandardReportTemplate) standardReportTemplate_obj;
 
 					  String codingscheme = standardReportTemplate.getCodingSchemeName();
 					  String version = standardReportTemplate.getCodingSchemeVersion();
 
 System.out.println("generateStandardReportAction: codingscheme " +  codingscheme);
 System.out.println("generateStandardReportAction: version " +  version);
 
 
 					  Boolean csnv_valid = DataUtils.validateCodingScheme(codingscheme, version);
 					  if (csnv_valid == null || csnv_valid.equals(Boolean.FALSE))
 					  {
 						  String message = "Invalid coding scheme name " + codingscheme + " or version " + version + " -- The report template may be out of date. Please modify it and resubmit.";
 						  request.getSession().setAttribute("message", message);
 						  return "message";
 					  }
 
 					  String rootConceptCode = standardReportTemplate.getRootConceptCode();
 					  String ltag = null;
 					  Concept rootConcept = DataUtils.getConceptByCode(codingscheme, version, ltag, rootConceptCode);
 					  if (rootConcept == null)
 					  {
 						  String message = "Invalid root concept code " + rootConceptCode + " -- Please modify the report template and resubmit.";
 						  request.getSession().setAttribute("message", message);
 						  return "message";
 					  }
 
 					  String associationName = standardReportTemplate.getAssociationName();
 					  key = codingscheme + " (version: " + version + ")";
 					  Vector<String> associationname_vec = DataUtils.getSupportedAssociationNames(key);
 					  if (!associationname_vec.contains(associationName)) {
 						  String message = "Invalid association name " + associationName + " -- Please modify the report template and resubmit.";
 						  request.getSession().setAttribute("message", message);
 						  return "message";
 					  }
 			   }
 		   } catch (Exception ex) {
 			   String message = "Exception encountered when generating " + templateId + ".";
 			   request.getSession().setAttribute("message", message);
 			   return "message";
 		   }
 
 		  String uid = (String) request.getSession().getAttribute("uid");
 		  if (uid == null)
 		  {
 			  String message = "You must first login to perform this function.";
 			  request.getSession().setAttribute("message", message);
 			  return "message";
 		  }
 
           String reportFormat_value = "Text (tab delimited)";
           String reportStatus_value = "DRAFT";
 
 		  String message = new StandardReportService().validReport(
 			  selectedStandardReportTemplate,
 			  reportFormat_value,
 			  reportStatus_value,
 			  uid);
 
 		  if (message.compareTo("success") != 0)
 		  {
 			  request.getSession().setAttribute("message", message);
 			  return "message";
 		  }
 
           String download_dir = null;
           try {
         	  download_dir = ReportWriterProperties.getInstance().getProperty(ReportWriterProperties.REPORT_DOWNLOAD_DIRECTORY);
 		  } catch (Exception ex) {
 
 		  }
 
 		  System.out.println("download_dir " + download_dir);
           Boolean retval = new StandardReportService().generateStandardReport(download_dir, selectedStandardReportTemplate, uid);
 
           // Instantiate Report Generation Service
           // Generate report
 
           // Create a StandardReport Record
 
           // create messsage
 
           message = "You request has been received. The report, " + templateId
           + ", in tab-delimited and Microsft Excel formats will be generated and placed in the designated output directory."
           + " Please review and assign an APPROVED status before making it available to the users.";
           request.getSession().setAttribute("message", message);
 		  return "message"; // replaced by a messsage page (back button)
 	  }
 
 
 	  public String downloadReportAction() {
 		  HttpServletRequest request = getHttpRequest();
 		  request.getSession().setAttribute("selectedStandardReportTemplate", selectedStandardReportTemplate);
 
           StandardReportTemplate standardReportTemplate = getStandardReportTemplate(selectedStandardReportTemplate);
           //String ontologyNameAndVersion = standardReportTemplate.getCodingSchemeName() + " (version: " + standardReportTemplate.getCodingSchemeVersion() + ")";
 
 System.out.println("downloading report " + selectedStandardReportTemplate);
 
           String download_dir = null;
           try {
         	  download_dir = ReportWriterProperties.getInstance().getProperty(ReportWriterProperties.REPORT_DOWNLOAD_DIRECTORY);
 System.out.println("download_dir " + download_dir);
 
 		  } catch (Exception ex) {
 
 			  String message = "Unable to download the specified report -- download directory does not exist -- check with system administrator.";
 			  request.getSession().setAttribute("message", message);
 		  	  return "message";
 		  }
 
           File dir = new File(download_dir);
           if (!dir.exists())
           {
 				System.out.println("Unable to download the specified report -- download directory does not exist. ");
 				String message = "Unable to download " + selectedStandardReportTemplate + " -- download directory does not exist. ";
 				request.getSession().setAttribute("message", message);
 				return "message";
 		  }
 
 		  File[] fileList = dir.listFiles();
 		  int len=fileList.length;
 		  while (len > 0) {
 				len--;
 				if (!fileList[len].isDirectory()) {
 					String name = fileList[len].getName();
 					System.out.println("File found in the download directory: " + name);
 				}
 		  }
 
           //request.getSession().setAttribute("selectedOntology", ontologyNameAndVersion);
 
           // find available reports in the download directory
 
           // Check if selectedStandardReportTemplate has been approved.
 
           // to be implemented:
           boolean approved = true;
           if (approved)
           {
 			  return "download";
 		  }
 		  else
 		  {
 			  String message = "The " + selectedStandardReportTemplate + " has not been approved for download.";
 			  request.getSession().setAttribute("message", message);
 			  return "message";
 		  }
           // if not, display a message page indicating such.
 
 
           // otherwise, route to generate_standard_report
 
 
 		  //return "generate_standard_report";
 	  }
 
 
 	  public String saveStatusAction() {
 		  HttpServletRequest request = getHttpRequest();
 		  request.getSession().setAttribute("selectedStandardReportTemplate", selectedStandardReportTemplate);
 
           StandardReportTemplate standardReportTemplate = getStandardReportTemplate(selectedStandardReportTemplate);
 
           String message = "The status of the "
           + standardReportTemplate.getLabel() + " has been updated successfully.";
 
           request.getSession().setAttribute("message", message);
 
 		  return "message"; // replaced by a messsage page (back button)
 	  }
 
 
 
 	private String selectedVersion = null;
 	private List versionList = null;
 	private Vector<String> versionListData = null;
 
 
 	public List getVersionList(String codingschemename) {
 		versionListData = DataUtils.getVersionListData(codingschemename);
 		versionList = new ArrayList();
 		for (int i=0; i<versionListData.size(); i++) {
 			String t = (String) versionListData.elementAt(i);
 
 System.out.println("version: " + t);
 
 			versionList.add(new SelectItem(t));
 		}
 		if (versionList != null && versionList.size() > 0) {
 			selectedVersion = ((SelectItem) versionList.get(0)).getLabel();
 		}
 		return versionList;
 	}
 
 	public void setSelectedVersion(String selectedVersion) {
 		this.selectedVersion = selectedVersion;
 		HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
 		request.getSession().setAttribute("selectedVersion", selectedVersion);
 	}
 
 
 	public String getSelectedVersion() {
 		return this.selectedVersion;
 	}
 
 	public void versionSelectionChanged(ValueChangeEvent event) {
 		if (event.getNewValue() == null) return;
 		//int id = Integer.parseInt((String) event.getNewValue());
 		setSelectedVersion(selectedVersion);
 	}
 
 
   }
