 /**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003-2005 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
 *
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 *
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/
 
 package org.sakaiproject.tool.assessment.qti.helper;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import javax.faces.context.FacesContext;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.xml.sax.SAXException;
 
 import org.sakaiproject.tool.assessment.data.dao.assessment.Answer;
 import org.sakaiproject.tool.assessment.data.dao.assessment.AnswerFeedback;
 import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
 import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentFeedback;
 import org.sakaiproject.tool.assessment.data.dao.assessment.EvaluationModel;
 import org.sakaiproject.tool.assessment.data.dao.assessment.ItemText;
 import org.sakaiproject.tool.assessment.data.dao.assessment.SecuredIPAddress;
 import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerFeedbackIfc;
 import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
 import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
 import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
 import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
 import org.sakaiproject.tool.assessment.facade.ItemFacade;
 import org.sakaiproject.tool.assessment.facade.SectionFacade;
 import org.sakaiproject.tool.assessment.qti.asi.ASIBaseClass;
 import org.sakaiproject.tool.assessment.qti.asi.Assessment;
 import org.sakaiproject.tool.assessment.qti.asi.Item;
 import org.sakaiproject.tool.assessment.qti.asi.Section;
 import org.sakaiproject.tool.assessment.qti.constants.AuthoringConstantStrings;
 import org.sakaiproject.tool.assessment.qti.constants.QTIVersion;
 import org.sakaiproject.tool.assessment.qti.exception.Iso8601FormatException;
 import org.sakaiproject.tool.assessment.qti.helper.item.ItemTypeExtractionStrategy;
 import org.sakaiproject.tool.assessment.qti.util.Iso8601DateFormat;
 import org.sakaiproject.tool.assessment.qti.util.Iso8601TimeInterval;
 import org.sakaiproject.tool.assessment.qti.util.XmlMapper;
 import org.sakaiproject.tool.assessment.qti.util.XmlUtil;
 import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
 import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
 
 /**
  * <p>Has helper methods for data extraction (import) from QTI</p>
  * <p> </p>
  * <p>Copyright: Copyright (c) 2005 Sakai</p>
  * @author Ed Smiley esmiley@stanford.edu
  * @version $Id$
  */
 
 public class ExtractionHelper
 {
   private static final String QTI_VERSION_1_2_PATH = "v1p2";
   private static final String QTI_VERSION_2_0_PATH = "v2p0";
   private static final String TRANSFORM_PATH =
       "/xml/xsl/dataTransform/import";
 
   private static final String ASSESSMENT_TRANSFORM =
       "extractAssessment.xsl";
   private static final String SECTION_TRANSFORM = "extractSection.xsl";
   private static final String ITEM_TRANSFORM = "extractItem.xsl";
   private static Log log = LogFactory.getLog(ExtractionHelper.class);
 
   private int qtiVersion = QTIVersion.VERSION_1_2;
   private String overridePath = null; // override defaults and settings
   private String FIB_BLANK_INDICATOR = " {} ";
 
   // versioning title string that it will look for/use, followed by a number
   private static final String VERSION_START = "  - ";
 
   /**
    * @deprecated
    */
   public ExtractionHelper()
   {
     this.setQtiVersion(QTIVersion.VERSION_1_2);
 
   }
 
   /**
    * Get ExtractionHelper for QTIVersion.VERSION_1_2
    * or QTIVersion.VERSION_2_0
    * @param qtiVersion
    */
   public ExtractionHelper(int qtiVersion)
   {
     this.setQtiVersion(qtiVersion);
   }
 
   /**
    * Path to XSL transform code.
    * @return context-relative path to XSL transform code.
    */
   public String getTransformPath()
   {
     // first check to see if normal computed path has been overridden
     if (overridePath != null)
     {
       return overridePath;
     }
 
     return TRANSFORM_PATH + "/" + getQtiPath();
   }
 
   private String getQtiPath()
   {
     return qtiVersion == QTIVersion.VERSION_1_2 ? QTI_VERSION_1_2_PATH :
         QTI_VERSION_2_0_PATH;
   }
 
   /**
    * Get QTI version flag.
    * Either QTIVersion.VERSION_1_2 or QTIVersion.VERSION_2_0;
    * @return QTI version flag
    */
   public int getQtiVersion()
   {
     return qtiVersion;
   }
 
   /**
    * Set QTI version flag.
    * Either QTIVersion.VERSION_1_2 or QTIVersion.VERSION_2_0;
    * @param qtiVersion
    */
   public void setQtiVersion(int qtiVersion)
   {
     if (!QTIVersion.isValid(qtiVersion))
     {
       throw new IllegalArgumentException("NOT Legal Qti Version.");
     }
     this.qtiVersion = qtiVersion;
   }
 
   /**
    * Get an XML document for the transform
    * @param template
    * @return
    */
   public Document getTransformDocument(String template)
   {
     Document document = null;
 
     if (!isOKtransform(template))
     {
       throw new IllegalArgumentException("NOT valid template.");
     }
     String templateContextPath = this.getTransformPath() + "/" + template;
     FacesContext context = FacesContext.getCurrentInstance();
     if (context == null)
     {
       return XmlUtil.readDocument(templateContextPath);
     }
     document = XmlUtil.readDocument(context, templateContextPath);
 
     return document;
   }
 
   /**
    * Get map of data to set from assessment XML
    * @param assessmentXml
    * @return a Map
    */
   public Map mapAssessment(Assessment assessmentXml)
   {
     log.debug("inside: mapAssessment");
     return map(ASSESSMENT_TRANSFORM, assessmentXml);
   }
 
   /**
    * Get map of data to set from section XML
    * @param sectionXml
    * @return a Map
    */
   public Map mapSection(Section sectionXml)
   {
     return map(SECTION_TRANSFORM, sectionXml);
   }
 
   /**
    * Get map of data to set from item XML
    * @param itemXml
    * @return a Map
    */
   public Map mapItem(Item itemXml)
   {
     return map(ITEM_TRANSFORM, itemXml);
   }
 
   /**
    * Helper method
        * @param transformType ASSESSMENT_TRANSFORM, SECTION_TRANSFORM, ITEM_TRANSFORM
    * @param asi ASIBaseClass: Assessment, Section, or Item XML
    * @return
    */
   private Map map(String transformType, ASIBaseClass asi)
   {
     if (!isOKasi(asi))
     {
       throw new IllegalArgumentException("Incorrect ASI subclass.");
     }
     if (!isOKtransform(transformType))
     {
       throw new IllegalArgumentException("Incorrect transform: " +
                                          transformType + ".");
     }
     Map map = null;
     try
     {
       Document transform = getTransformDocument(transformType);
       Document xml = asi.getDocument();
       Document model = XmlUtil.transformDocument(xml, transform);
       map = XmlMapper.map(model);
     }
     catch (IOException ex)
     {
       log.error(ex);
       ex.printStackTrace(System.out);
     }
     catch (SAXException ex)
     {
       log.error(ex);
       ex.printStackTrace(System.out);
     }
     catch (ParserConfigurationException ex)
     {
       log.error(ex);
       ex.printStackTrace(System.out);
     }
     return map;
 
   }
 
   /**
    * Look up a List of Section XML from Assessment Xml
    * @return a List of Section XML objects
    */
   public List getSectionXmlList(Assessment assessmentXml)
   {
     List nodeList = assessmentXml.selectNodes("//section");
     List sectionXmlList = new ArrayList();
 
     // now convert our list of Nodes to a list of section xml
     for (int i = 0; i < nodeList.size(); i++)
     {
       try
       {
         Node node = (Node) nodeList.get(i);
         // create a document for a section xml object
         Document sectionDoc = XmlUtil.createDocument();
         // Make a copy for inserting into the new document
         Node importNode = sectionDoc.importNode(node, true);
         // Insert the copy into sectionDoc
         sectionDoc.appendChild(importNode);
         Section sectionXml = new Section(sectionDoc,
                this.getQtiVersion());
         // add the new section xml object to the list
         sectionXmlList.add(sectionXml);
       }
       catch (DOMException ex)
       {
         log.error(ex);
         ex.printStackTrace(System.out);
       }
     }
     return sectionXmlList;
   }
 
   /**
    * Look up a List of Item XML from Section Xml
    * @param Section sectionXml
    * @return a List of Item XML objects
    */
   public List getItemXmlList(Section sectionXml)
   {
     String itemElementName =
         qtiVersion == QTIVersion.VERSION_1_2 ? "//item" : "//assessmentItem";
 
     // now convert our list of Nodes to a list of section xml
     List nodeList = sectionXml.selectNodes(itemElementName);
     List itemXmlList = new ArrayList();
     for (int i = 0; i < nodeList.size(); i++)
     {
       try
       {
         Node node = (Node) nodeList.get(i);
         // create a document for a item xml object
         Document itemDoc = XmlUtil.createDocument();
         // Make a copy for inserting into the new document
         Node importNode = itemDoc.importNode(node, true);
         // Insert the copy into itemDoc
         itemDoc.appendChild(importNode);
         Item itemXml = new Item(itemDoc,
                this.getQtiVersion());
         // add the new section xml object to the list
         itemXmlList.add(itemXml);
       }
       catch (DOMException ex)
       {
         log.error(ex);
         ex.printStackTrace(System.out);
       }
     }
     return itemXmlList;
   }
 
   /**
    * Used internally.
    * @param transform
    * @return true if OK
    */
   private boolean isOKtransform(String transform)
   {
     return (transform == this.ASSESSMENT_TRANSFORM ||
             transform == this.SECTION_TRANSFORM ||
             transform == this.ITEM_TRANSFORM) ? true : false;
   }
 
   /**
    * Used internally.
    * @param asi
    * @return true if OK
    */
   private boolean isOKasi(ASIBaseClass asi)
   {
     return (asi instanceof Assessment ||
             asi instanceof Section ||
             asi instanceof Item) ? true : false;
   }
 
 //  /**
 //   * Create assessment from the extracted properties.
 //   * @param assessmentMap the extracted properties
 //   * @return an assessment, which has been persisted
 //   */
 //  public AssessmentFacade createAssessment(Map assessmentMap)
 //  {
 //    String description = (String) assessmentMap.get("description");
 //    String title = (String) assessmentMap.get("title");
 //    AssessmentService assessmentService = new AssessmentService();
 //    AssessmentFacade assessment = assessmentService.createAssessment(
 //        title, description, null, null);
 //    return assessment;
 //  }
 
   /**
    * Update assessment from the extracted properties.
    * Note: you need to do a save when you are done.
    * @param assessment the assessment, which will  be persisted
    * @param assessmentMap the extracted properties
    */
   public void updateAssessment(AssessmentFacade assessment,
                                Map assessmentMap)
   {
     String title;
     String displayName;
     String description;
     String comments;
 
     String instructorNotification;
     String testeeNotification;
     String multipartAllowed;
     String createdBy;
     String createdDate;
 
     title = (String) assessmentMap.get("title");
     displayName = (String) assessmentMap.get("title");
     comments = (String) assessmentMap.get("comments");
 
     log.debug("ASSESSMENT updating metadata information");
     // set meta data
     List metalist = (List) assessmentMap.get("metadata");
     MetaDataList metadataList = new MetaDataList(metalist);
     metadataList.setDefaults(assessment);
     metadataList.addTo(assessment);
     createdBy = assessment.getAssessmentMetaDataByLabel("CREATOR");
 
     log.debug("ASSESSMENT updating basic information");
     // set basic properties
     assessment.setCreatedBy(createdBy);
     assessment.setComments(comments);
     assessment.setCreatedDate(new Date());
     assessment.setLastModifiedBy("Sakai Import");
     assessment.setLastModifiedDate(new Date());
 
     // additional information
 
     // restricted IP address
     log.debug("ASSESSMENT updating access control, evaluation model, feedback");
 
     // access control
     String duration = (String) assessmentMap.get("duration");
     log.debug("duration: " + duration);
 
     makeAccessControl(assessment, duration);
 
     // evaluation model control
     makeEvaluationModel(assessment);
 
     // assessment feedback control
     makeAssessmentFeedback(assessment);
 
   }
 
   /**
    * Put feedback settings into assessment (bi-directional)
    * @param assessment
    */
   private void makeAssessmentFeedback(AssessmentFacade assessment)
   {
     AssessmentFeedback feedback =
         (AssessmentFeedback) assessment.getAssessmentFeedback();
     if (feedback == null){
       feedback = new AssessmentFeedback();
       // Need to fix AssessmentFeedback so it can take AssessmentFacade later
       feedback.setAssessmentBase(assessment.getData());
     }
 
     if ("TRUE".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
         "FEEDBACK_SHOW_QUESTION")))
     {
       feedback.setShowQuestionText(Boolean.TRUE);
     }
     else
     {
       feedback.setShowQuestionText(Boolean.FALSE);
     }
 
     if ("TRUE".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
         "FEEDBACK_SHOW_RESPONSE")))
     {
       feedback.setShowStudentResponse(Boolean.TRUE);
     }
     else
     {
       feedback.setShowStudentResponse(Boolean.FALSE);
     }
 
     if ("TRUE".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
         "FEEDBACK_SHOW_CORRECT_RESPONSE")))
     {
       feedback.setShowCorrectResponse(Boolean.TRUE);
     }
     else
     {
       feedback.setShowCorrectResponse(Boolean.FALSE);
     }
 
     if ("TRUE".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
         "FEEDBACK_SHOW_STUDENT_SCORE")))
     {
       feedback.setShowStudentScore(Boolean.TRUE);
     }
     else
     {
       feedback.setShowStudentScore(Boolean.FALSE);
     }
 
     if ("TRUE".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
         "FEEDBACK_SHOW_STUDENT_QUESTIONSCORE")))
     {
       feedback.setShowStudentQuestionScore(Boolean.TRUE);
     }
     else
     {
       feedback.setShowStudentQuestionScore(Boolean.FALSE);
     }
 
     if ("TRUE".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
         "FEEDBACK_SHOW_ITEM_LEVEL")))
     {
       feedback.setShowQuestionLevelFeedback(Boolean.TRUE);
     }
     else
     {
       feedback.setShowQuestionLevelFeedback(Boolean.FALSE);
     }
 
     if ("TRUE".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
         "FEEDBACK_SHOW_SELECTION_LEVEL")))
     {
       feedback.setShowSelectionLevelFeedback(Boolean.TRUE);
     }
     else
     {
       feedback.setShowSelectionLevelFeedback(Boolean.FALSE);
     }
 
     if ("TRUE".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
         "FEEDBACK_SHOW_GRADER_COMMENT")))
     {
       feedback.setShowGraderComments(Boolean.TRUE);
     }
     else
     {
       feedback.setShowGraderComments(Boolean.FALSE);
     }
 
     if ("TRUE".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
         "FEEDBACK_SHOW_STATS")))
     {
       feedback.setShowStatistics(Boolean.TRUE);
     }
     else
     {
       feedback.setShowStatistics(Boolean.FALSE);
     }
 
     if (
         this.notNullOrEmpty(assessment.getAssessmentMetaDataByLabel(
         "FEEDBACK_DELIVERY_DATE") )  ||
         "DATED".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
         "FEEDBACK_DELIVERY")))
     {
       feedback.setFeedbackDelivery(feedback.FEEDBACK_BY_DATE);
     }
     else if ("IMMEDIATE".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
         "FEEDBACK_DELIVERY")))
     {
       feedback.setFeedbackDelivery(feedback.IMMEDIATE_FEEDBACK);
     }
     else
     {
       feedback.setFeedbackDelivery(feedback.NO_FEEDBACK);
     }
 
  if (
         "QUESTION".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
         "FEEDBACK_AUTHORING")))
     {
       feedback.setFeedbackAuthoring(feedback.QUESTIONLEVEL_FEEDBACK);
     }
     else if ("SECTION".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
         "FEEDBACK_AUTHORING")))
     {
       feedback.setFeedbackAuthoring(feedback.SECTIONLEVEL_FEEDBACK);
     }
     else
     {
       feedback.setFeedbackAuthoring(feedback.BOTH_FEEDBACK);
     }
 
     assessment.setAssessmentFeedback(feedback);
   }
 
   /**
    * Put evaluation settings into assessment (bi-directional)
    * @param assessment
    */
   private void makeEvaluationModel(AssessmentFacade assessment)
   {
     EvaluationModel evaluationModel =
         (EvaluationModel) assessment.getEvaluationModel();
     if (evaluationModel == null){
       evaluationModel = new EvaluationModel();
       // Need to fix EvaluationModel so it can take AssessmentFacade later
       evaluationModel.setAssessmentBase(assessment.getData());
     }
 
     // anonymous
     if ("TRUE".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
         "ANONYMOUS_GRADING")))
     {
       evaluationModel.setAnonymousGrading(EvaluationModel.ANONYMOUS_GRADING);
     }
     else
     {
       evaluationModel.setAnonymousGrading(EvaluationModel.NON_ANONYMOUS_GRADING);
     }
 
     // gradebook options, don't know how this is supposed to work, leave alone for now
     if ("DEFAULT".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
         "GRADEBOOK_OPTIONS")))
     {
       evaluationModel.setToGradeBook(EvaluationModel.TO_DEFAULT_GRADEBOOK.toString());
     }
     else if ("SELECTED".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
         "GRADEBOOK_OPTIONS")))
     {
       evaluationModel.setToGradeBook(EvaluationModel.TO_SELECTED_GRADEBOOK.toString());
     }
 
     // highest or last
     if ("HIGHEST".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
         "GRADE_SCORE")))
     {
       evaluationModel.setScoringType(EvaluationModel.HIGHEST_SCORE);
     }
     /*
        // not implementing average for now
     else if ("AVERAGE".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
         "GRADE_SCORE")))
     {
       evaluationModel.setScoringType(EvaluationModel.AVERAGE_SCORE);
     }
     */
     else if ("LAST".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
         "GRADE_SCORE")))
     {
       evaluationModel.setScoringType(EvaluationModel.LAST_SCORE);
     }
     assessment.setEvaluationModel(evaluationModel);
   }
 
   /**
    * Put access control settings into assessment (bi-directional)
    * @param assessment
    * @param duration Time interval for timed assessment (Iso8601 format)
    */
   private void makeAccessControl(AssessmentFacade assessment, String duration)
   {
     AssessmentAccessControl control =
         (AssessmentAccessControl)assessment.getAssessmentAccessControl();
     if (control == null){
       control = new AssessmentAccessControl();
       // need to fix accessControl so it can take AssessmentFacade later
       control.setAssessmentBase(assessment.getData());
     }
 
     // Control dates
     Iso8601DateFormat iso = new Iso8601DateFormat();
     String startDate = assessment.getAssessmentMetaDataByLabel("START_DATE");
     String dueDate = assessment.getAssessmentMetaDataByLabel("END_DATE");
     String retractDate = assessment.getAssessmentMetaDataByLabel("RETRACT_DATE");
     String feedbackDate = assessment.getAssessmentMetaDataByLabel(
         "FEEDBACK_DELIVERY_DATE");
 
     try
     {
       control.setStartDate(iso.parse(startDate).getTime());
       assessment.getData().addAssessmentMetaData("hasAvailableDate", "true");
 
     }
     catch (Iso8601FormatException ex)
     {
       log.debug("Cannot set startDate.");
     }
     try
     {
       control.setDueDate(iso.parse(dueDate).getTime());
 //      assessment.getData().addAssessmentMetaData("hasDueDate", "true");
       assessment.getData().addAssessmentMetaData("dueDate", "true");
     }
     catch (Iso8601FormatException ex)
     {
       log.debug("Cannot set dueDate.");
     }
     try
     {
       control.setRetractDate(iso.parse(retractDate).getTime());
       assessment.getData().addAssessmentMetaData("hasRetractDate", "true");
     }
     catch (Iso8601FormatException ex)
     {
       log.debug("Cannot set retractDate.");
     }
     try
     {
       control.setFeedbackDate(iso.parse(feedbackDate).getTime());
       assessment.getData().addAssessmentMetaData("FEEDBACK_DELIVERY","DATED");
     }
     catch (Iso8601FormatException ex)
     {
       log.debug("Cannot set feedbackDate.");
     }
 
     // don't know what site you will have in a new environment
     // but registered as a BUG in SAM-271 so turning it on.
 
     String releasedTo = assessment.getAssessmentMetaDataByLabel(
         "ASSESSMENT_RELEASED_TO");
 
     // for backwards compatibility with version 1.5 exports.
     if (releasedTo != null && releasedTo.indexOf("Authenticated Users") > -1)
     {
       log.debug(
           "Fixing obsolete reference to 'Authenticated Users', setting released to 'Anonymous Users'.");
       releasedTo = "Anonymous Users";
     }
 
 
       // for backwards compatibility with version 1.5 exports.
       if (releasedTo != null && releasedTo.indexOf("Authenticated Users") > -1)
       {
         log.debug(
           "Fixing obsolete reference to 'Authenticated Users', setting released to 'Anonymous Users'.");
         releasedTo = "Anonymous Users";
       }
 
       log.debug("control.setReleaseTo(releasedTo)='"+releasedTo+"'.");
       control.setReleaseTo(releasedTo);
 
     // Timed Assessment
     if (duration != null)
     {
       try
       {
         Iso8601TimeInterval tiso = new Iso8601TimeInterval(duration);
         log.debug("tiso.getDuration(): " + tiso.getDuration());
 
         if(tiso==null)
         {
           throw new Iso8601FormatException("Assessment duration could not be resolved.");
         }
         long millisecondsDuration = tiso.getDuration();
         int seconds = (int) millisecondsDuration /1000;
         control.setTimeLimit(new Integer(seconds));
        control.setTimedAssessment(AssessmentAccessControl.TIMED_ASSESSMENT);
        assessment.getData().addAssessmentMetaData("hasTimeAssessment", "true");
       }
       catch (Iso8601FormatException ex)
       {
         log.warn("Can't format assessment duration. " + ex);
         control.setTimeLimit(new Integer(0));
         control.setTimedAssessment(AssessmentAccessControl.
                                    DO_NOT_TIMED_ASSESSMENT);
       }
     }
     else
     {
       control.setTimeLimit(new Integer(0));
       control.setTimedAssessment(AssessmentAccessControl.
                                  DO_NOT_TIMED_ASSESSMENT);
     }
 
     log.debug("assessment.getAssessmentMetaDataByLabel(AUTO_SUBMIT): " +
              assessment.getAssessmentMetaDataByLabel("AUTO_SUBMIT"));
 
 
     if ("TRUE".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
         "AUTO_SUBMIT")))
     {
       log.debug("AUTO SUBMIT IS TRUE");
       control.setAutoSubmit(AssessmentAccessControl.AUTO_SUBMIT);
       assessment.getData().addAssessmentMetaData("hasAutoSubmit", "true");
     }
     else
     {
       control.setAutoSubmit(AssessmentAccessControl.DO_NOT_AUTO_SUBMIT);
     }
 
     // Assessment Organization
     // navigation
     if ("LINEAR".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
         "NAVIGATION")))
     {
       control.setItemNavigation(control.LINEAR_ACCESS);
     }
     else
     {
       control.setItemNavigation(control.RANDOM_ACCESS);
     }
 
     // numbering
     if ("CONTINUOUS".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
         "QUESTION_NUMBERING")))
     {
       control.setItemNumbering(control.CONTINUOUS_NUMBERING);
     }
     else if ("RESTART".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
         "QUESTION_NUMBERING")))
     {
       control.setItemNumbering(control.RESTART_NUMBERING_BY_PART);
     }
 
     //question layout
     if ("I".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
         "QUESTION_LAYOUT")))
     {
       control.setAssessmentFormat(control.BY_QUESTION);
     }
     else if ("S".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
         "QUESTION_LAYOUT")))
     {
       control.setAssessmentFormat(control.BY_PART);
     }
     else
     {
       control.setAssessmentFormat(control.BY_ASSESSMENT);
     }
 
     //Submissions
     // submissions allowed
     String maxAttempts =
         "" + assessment.getAssessmentMetaDataByLabel("MAX_ATTEMPTS");
     String unlimited = AuthoringConstantStrings.UNLIMITED_SUBMISSIONS;
     log.debug("maxAttempts: '" + maxAttempts + "'");
     log.debug("unlimited: '" + unlimited + "'");
 
     if (
         unlimited.equals(maxAttempts.trim()))
     {
       log.debug("unlimited.equals(maxAttempts.trim()");
       control.setUnlimitedSubmissions(Boolean.TRUE);
       control.setSubmissionsAllowed(AssessmentAccessControlIfc.
                                     UNLIMITED_SUBMISSIONS);
     }
     else
     {
       control.setUnlimitedSubmissions(Boolean.FALSE);
       try
       {
         control.setSubmissionsAllowed(new Integer(maxAttempts));
       }
       catch (NumberFormatException ex1)
       {
         control.setSubmissionsAllowed(new Integer("1"));
       }
     }
     log.debug("Set: control.getSubmissionsAllowed()="+control.getSubmissionsAllowed());
     log.debug("Set: control.getUnlimitedSubmissions()="+control.getUnlimitedSubmissions());
 
     // late submissions
     // I am puzzled as to why there is no ACCEPT_LATE_SUBMISSION, assuming it =T
     if ("FALSE".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
         "LATE_HANDLING")))
     {
       control.setLateHandling(control.NOT_ACCEPT_LATE_SUBMISSION);
     }
     else
     {
       control.setLateHandling(new Integer(1));
 
     }
 
     // auto save
     if ("TRUE".equalsIgnoreCase(assessment.getAssessmentMetaDataByLabel(
         "AUTO_SAVE")))
     {
       control.setAutoSubmit(control.AUTO_SAVE);
     }
 
     // Submission Message
     String submissionMessage = assessment.getAssessmentMetaDataByLabel(
         "SUBMISSION_MESSAGE");
     if (submissionMessage != null)
     {
       control.setSubmissionMessage(submissionMessage);
     }
 
     // Username, password, finalPageUrl
 //    String considerUserId = assessment.getAssessmentMetaDataByLabel(
 //        "CONSIDER_USERID"); //
     String userId = assessment.getAssessmentMetaDataByLabel("USERID");
     String password = assessment.getAssessmentMetaDataByLabel("PASSWORD");
     String finalPageUrl = assessment.getAssessmentMetaDataByLabel("FINISH_URL");
 
     if (//"TRUE".equalsIgnoreCase(considerUserId) &&
         notNullOrEmpty(userId) && notNullOrEmpty(password))
     {
       control.setUsername(userId);
       control.setPassword(password);
       assessment.getData().addAssessmentMetaData("hasUsernamePassword", "true");
     }
     control.setFinalPageUrl(finalPageUrl);
 
     assessment.setAssessmentAccessControl(control);
   }
 
   /**
    * the ip address is in a newline delimited string
    * @param assessment
    */
   public void makeSecuredIPAddressSet(AssessmentFacade assessment, String ipList)
   {
     Set securedIPAddressSet = (Set) assessment.getSecuredIPAddressSet();
     AssessmentBaseIfc data = assessment.getData();
 
     if (securedIPAddressSet == null)
     {
       securedIPAddressSet = new HashSet();
     }
     log.info("Getting securedIPAddressSet=" + securedIPAddressSet);
 
     log.info("ipList: " + ipList);
 
     if (ipList == null)
       ipList = "";
     String[] ip = ipList.split("\\n");
 
     for (int j = 0; j < ip.length; j++)
     {
       log.info("ip # " + j + ": " + ip[j]);
       if (ip[j] != null)
       {
         SecuredIPAddress sip = new SecuredIPAddress(data, null, ip[j]);
         //sip.setAssessment(data);
         securedIPAddressSet.add(sip);
       }
     }
 
     log.info("securedIPAddressSet.size()=" + securedIPAddressSet.size());
     if (securedIPAddressSet.size()>0)
     {
       log.info("Setting securedIPAddressSet;addAssessmentMetaData(hasIpAddress, true)");
       AssessmentService assessmentService = new AssessmentService();
 //      assessment.getData().setSecuredIPAddressSet(securedIPAddressSet);
 //      assessment.getData().addAssessmentMetaData("hasIpAddress", "true");
 //      assessment.getData().addAssessmentMetaData("hasSpecificIP", "true");
 //      data.setSecuredIPAddressSet(securedIPAddressSet);
       data.addAssessmentMetaData("hasIpAddress", "true");
       data.addAssessmentMetaData("hasSpecificIP", "true");
       assessment.updateData(data);
       assessment.setSecuredIPAddressSet(securedIPAddressSet);
 
     }
   }
   /**
    * Update section from the extracted properties.
    * Note: you need to do a save when you are done.
    * @param section the section, which will  be persisted
    * @param sectionMap the extracted properties
    */
   public void updateSection(SectionFacade section, Map sectionMap)
   {
     section.setTitle( (String) sectionMap.get("title"));
     section.setDescription( (String) sectionMap.get("description"));
     section.setLastModifiedBy("Sakai Import");
     section.setLastModifiedDate(new Date());
   }
 
   /**
    * Update item from the extracted properties.
    * Note: you need to do a save when you are done.
    * @param item the item, which will  be persisted
    * @param itemMap the extracted properties
    */
   public void updateItem(ItemFacade item, Map itemMap)
   {
     // type and title
     String title = (String) itemMap.get("title");
     item.setDescription(title);
 
     // set meta data
     List metalist = (List) itemMap.get("metadata");
     MetaDataList metadataList = new MetaDataList(metalist);
     metadataList.addTo(item);
 
     // type
     log.debug("itemMap="+itemMap);
     String qmd = item.getItemMetaDataByLabel("qmd_itemtype");
     String itemIntrospect = (String) itemMap.get("itemIntrospect");
     log.debug("Calling ItemTypeExtractionStrategy.calculate(");
     log.debug("    title="+title);
     log.debug("    , itemIntrospect="+itemIntrospect);
     log.debug("    ,  qmd="+qmd);
     log.debug(");");
 
     Long typeId = ItemTypeExtractionStrategy.calculate(title, itemIntrospect, qmd);
     item.setTypeId(typeId);
 
     // basic properties
     addItemProperties(item, itemMap);
 
     // feedback
     // correct, incorrect, general
     addFeedback(item, itemMap, typeId);
 
     // item text and answers
     if (TypeIfc.FILL_IN_BLANK.longValue() == typeId.longValue())
     {
       addFibTextAndAnswers(item, itemMap);
     }
     else if (TypeIfc.MATCHING.longValue() == typeId.longValue())
     {
       addMatchTextAndAnswers(item, itemMap);
     }
     else
     {
       addTextAndAnswers(item, itemMap);
     }
 
   }
 
 
   /**
    *
    * @param item
    * @param itemMap
    */
   private void addItemProperties(ItemFacade item, Map itemMap)
   {
     String duration = (String) itemMap.get("duration");
     String triesAllowed = (String) itemMap.get("triesAllowed");
     String score = (String) itemMap.get("score");
     String hasRationale =  item.getItemMetaDataByLabel("hasRationale");//rshastri :SAK-1824
     String status = (String) itemMap.get("status");
     String createdBy = (String) itemMap.get("createdBy");
 
     // not being set yet
     String instruction = (String) itemMap.get("instruction");
     String hint = (String) itemMap.get("hint");
 
     // created by is not nullable
     if (createdBy == null)
     {
       createdBy = "Imported by Sakai";
     }
 
     String createdDate = (String) itemMap.get("createdDate");
 
     if (notNullOrEmpty(duration))
     {
       item.setDuration(new Integer(duration));
     }
     if (notNullOrEmpty(triesAllowed))
     {
       item.setTriesAllowed(new Integer(triesAllowed));
     }
     item.setInstruction( (String) itemMap.get("instruction"));
     if (notNullOrEmpty(score))
     {
       item.setScore(new Float(score));
     }
     item.setHint( (String) itemMap.get("hint"));
     if (notNullOrEmpty(hasRationale))
     {
       item.setHasRationale(new Boolean(hasRationale));
     }
     if (notNullOrEmpty(status))
     {
       item.setStatus(new Integer(status));
     }
     item.setCreatedBy(createdBy);
     try
     {
       Iso8601DateFormat iso = new Iso8601DateFormat();
       Calendar cal = iso.parse(createdDate);
       item.setCreatedDate(cal.getTime());
     }
     catch (Exception ex)
     {
       item.setCreatedDate(new Date());
     }
     item.setLastModifiedBy("Sakai Import");
     item.setLastModifiedDate(new Date());
   }
 
   /**
    * add feedback
    * @param item
    * @param itemMap
    * @param typeId
    */
   private void addFeedback(ItemFacade item, Map itemMap, Long typeId)
   {
     // write the map out
     Iterator iter = itemMap.keySet().iterator();
     while (iter.hasNext())
     {
       String key = (String) iter.next();
       Object o = itemMap.get(key);
       log.debug("itemMap: " + key + "=" + itemMap.get(key));
     }
 
     String correctItemFeedback = (String) itemMap.get("correctItemFeedback");
     String incorrectItemFeedback = (String) itemMap.get("incorrectItemFeedback");
     String generalItemFeedback = (String) itemMap.get("generalItemFeedback");
     if (generalItemFeedback==null) generalItemFeedback = "";
 
     // NOTE:
     // in early Samigo (aka Navigo) general feedback exported as "InCorrect"!
     // now if this is an Audio, File Upload or Short Answer question additional
     // feedback will append feedback to general, this should be OK, since
     // QTI with general feedback for these types will leave them empty
     if (TypeIfc.AUDIO_RECORDING.longValue() == typeId.longValue() ||
         TypeIfc.FILE_UPLOAD.longValue() == typeId.longValue() ||
         TypeIfc.ESSAY_QUESTION.longValue() == typeId.longValue())
     {
       if (notNullOrEmpty(incorrectItemFeedback))
       {
         generalItemFeedback += " " + incorrectItemFeedback;
       }
       if (notNullOrEmpty(correctItemFeedback))
       {
         generalItemFeedback += " " + correctItemFeedback;
       }
     }
 
     if (notNullOrEmpty(correctItemFeedback))
     {
       item.setCorrectItemFeedback(correctItemFeedback);
     }
     if (notNullOrEmpty(incorrectItemFeedback))
     {
       item.setInCorrectItemFeedback(incorrectItemFeedback);
     }
     if (notNullOrEmpty(generalItemFeedback))
     {
       item.setGeneralItemFeedback(generalItemFeedback);
     }
 
   }
 
   /**
    * create the answer feedback set for an answer
    * @param item
    * @param itemMap
    */
   private void addAnswerFeedback(Answer answer, String value)
   {
     HashSet answerFeedbackSet = new HashSet();
     answerFeedbackSet.add(new AnswerFeedback(answer,
                                              AnswerFeedbackIfc.ANSWER_FEEDBACK,
                                              value));
     answer.setAnswerFeedbackSet(answerFeedbackSet);
   }
 
   /**
    * @param item
    * @param itemMap
    */
   private void addTextAndAnswers(ItemFacade item, Map itemMap)
   {
     List itemTextList = (List) itemMap.get("itemText");
     HashSet itemTextSet = new HashSet();
     for (int i = 0; i < itemTextList.size(); i++)
     {
       ItemText itemText = new ItemText();
       String text = (String) itemTextList.get(i);
       // should be allow this or, continue??
       // for now, empty string OK, setting to empty string if null
       if (text == null)
       {
         text = "";
       }
       text=text.replaceAll("\\?\\?"," ");//SAK-2298
       log.debug("text: " + text);
 
       itemText.setText(text);
       itemText.setItem(item.getData());
       itemText.setSequence(new Long(i + 1));
       List answerList = new ArrayList();
       List aList = (List) itemMap.get("itemAnswer");
       answerList = aList == null ? answerList : aList;
       HashSet answerSet = new HashSet();
       char answerLabel = 'A';
       List answerFeedbackList = (List) itemMap.get("itemAnswerFeedback");
 
       ArrayList correctLabels;
       correctLabels = (ArrayList) itemMap.get("itemAnswerCorrectLabel");
       if (correctLabels == null)
       {
         correctLabels = new ArrayList();
       }
       for (int a = 0; a < answerList.size(); a++)
       {
         Answer answer = new Answer();
         String answerText = (String) answerList.get(a);
         // these are not supposed to be empty
         if (notNullOrEmpty(answerText))
         {
           answerText=answerText.replaceAll("\\?\\?"," ");//SAK-2298
           log.debug("answerText: " + answerText);
 
           // normalize all true/false questions
           if (answerList.size()==2)
           {
             if (answerText.equalsIgnoreCase("true")) answerText = "true";
             if (answerText.equalsIgnoreCase("false")) answerText = "false";
           }
           String label = "" + answerLabel++;
           answer.setLabel(label); // up to 26, is this a problem?
 
           // correct answer and score
           float score = 0;
           // if label matches correct answer it is correct
           if (isCorrectLabel(label, correctLabels))
           {
             answer.setIsCorrect(Boolean.TRUE);
             // manual authoring disregards correctness
             // commented out: what we'd have if we looked at correctness
 //            score = getCorrectScore(item, 1);
           }
           else
           {
             answer.setIsCorrect(Boolean.FALSE);
           }
           // manual authoring disregards correctness
           // so we will do the same.
           score = getCorrectScore(item, 1);
           log.debug("setting answer" + label + " score to:" + score);
           answer.setScore(new Float(score));
 
           answer.setText(answerText);
           answer.setItemText(itemText);
           answer.setItem(item.getData());
           int sequence = a + 1;
           answer.setSequence(new Long(sequence));
           // prepare answer feedback - daisyf added this on 2/21/05
           // need to check if this works for question type other than
           // MC
           HashSet set = new HashSet();
           if (answerFeedbackList != null)
           {
             AnswerFeedback answerFeedback = new AnswerFeedback();
             answerFeedback.setAnswer(answer);
             answerFeedback.setTypeId(AnswerFeedbackIfc.GENERAL_FEEDBACK);
             if (answerFeedbackList.get(sequence - 1) != null)
             {
               answerFeedback.setText( (String) answerFeedbackList.get(sequence -
                   1));
               set.add(answerFeedback);
               answer.setAnswerFeedbackSet(set);
             }
           }
 
           answerSet.add(answer);
         }
       }
       itemText.setAnswerSet(answerSet);
       itemTextSet.add(itemText);
     }
     item.setItemTextSet(itemTextSet);
   }
 
   private float getCorrectScore(ItemDataIfc item, int answerSize)
   {
     float score =0;
     if (answerSize>0 && item!=null && item.getScore()!=null)
     {
       score = item.getScore().floatValue()/answerSize;
     }
     return score;
   }
 
   /**
    * Check to find out it response label is in the list of correct responses
    * @param testLabel response label
    * @param labels the list of correct responses
    * @return
    */
   private boolean isCorrectLabel(String testLabel, ArrayList labels)
   {
     if (testLabel == null || labels == null
         || labels.indexOf(testLabel) == -1)
     {
       return false;
     }
 
     return true;
   }
 
   /**
    * FIB questions ONLY
    * @param item
    * @param itemMap
    */
   private void addFibTextAndAnswers(ItemFacade item, Map itemMap)
   {
     List itemTextList = new ArrayList();
     List iList = (List) itemMap.get("itemFibText");
     itemTextList = iList == null ? itemTextList : iList;
 
     List itemTList = new ArrayList();
     List tList = (List) itemMap.get("itemText");
     itemTList = iList == null ? itemTList : tList;
 
     HashSet itemTextSet = new HashSet();
     ItemText itemText = new ItemText();
     String itemTextString = "";
     List answerFeedbackList = (List) itemMap.get("itemFeedback");
 
     List answerList = new ArrayList();
     List aList = (List) itemMap.get("itemFibAnswer");
     answerList = aList == null ? answerList : aList;
 
     // handle FIB with instructional text
     // sneak it into first text
     if (   !itemTList.isEmpty()
         && !itemTextList.isEmpty()
         && !(itemTextList.size()>1))
     {
       try
       {
         String firstFib = (String) itemTextList.get(0);
         String firstText = (String) itemTList.get(0);
         if (firstFib.equals(firstText))
         {
           log.debug("Setting FIB instructional text.");
 //          itemTextList.remove(0);
           String newFirstFib
             = firstFib + "<br />" + itemTextList.get(0);
           itemTextList.set(0, newFirstFib);
         }
       }
       catch (Exception ex)
       {
         log.warn("Thought we found an instructional text but couldn't put it in."
                 + " " + ex);
       }
     }
     // loop through all our extracted FIB texts interposing FIB_BLANK_INDICATOR
     for (int i = 0; i < itemTextList.size(); i++)
     {
       String text = (String) itemTextList.get(i);
       // we are assuming non-empty text/answer/non-empty text/answer etc.
       if (text == null || text=="")
       {
         continue;
       }
       itemTextString += text;
       if (i < answerList.size())
       {
         itemTextString += FIB_BLANK_INDICATOR;
       }
     }
     itemTextString=itemTextString.replaceAll("\\?\\?"," ");//SAK-2298
     log.debug("itemTextString="+itemTextString);
     itemText.setText(itemTextString);
     itemText.setItem(item.getData());
     itemText.setSequence(new Long(0));
     HashSet answerSet = new HashSet();
     char answerLabel = 'A';
     for (int a = 0; a < answerList.size(); a++)
     {
       Answer answer = new Answer();
       String answerText = (String) answerList.get(a);
       // these are not supposed to be empty
       if (notNullOrEmpty(answerText))
       {
         answerText=answerText.replaceAll("\\?\\?"," ");//SAK-2298
         log.debug("answerText="+answerText);
 
         String label = "" + answerLabel++;
         answer.setLabel(label); // up to 26, is this a problem?
         answer.setText(answerText);
         answer.setItemText(itemText);
 
         // correct answer and score
         answer.setIsCorrect(Boolean.TRUE);
         // manual authoring disregards the number of partial answers
         // so we will do the same.
         float score = getCorrectScore(item, 1);
 //        float score = getCorrectScore(item, answerList.size());
 
         log.debug("setting answer " + label + " score to:" + score);
         answer.setScore(new Float(score));
 
         answer.setItem(item.getData());
         int sequence = a + 1;
         answer.setSequence(new Long(sequence));
         HashSet set = new HashSet();
         if (answerFeedbackList != null)
         {
           AnswerFeedback answerFeedback = new AnswerFeedback();
           answerFeedback.setAnswer(answer);
           answerFeedback.setTypeId(AnswerFeedbackIfc.GENERAL_FEEDBACK);
           if (answerFeedbackList.get(sequence - 1) != null)
           {
             answerFeedback.setText( (String) answerFeedbackList.get(
                 sequence - 1));
             set.add(answerFeedback);
             answer.setAnswerFeedbackSet(set);
           }
         }
         answerSet.add(answer);
       }
     }
 
     itemText.setAnswerSet(answerSet);
     itemTextSet.add(itemText);
     item.setItemTextSet(itemTextSet);
   }
 
   /**
    * MATCHING questions ONLY
    * @param item
    * @param itemMap
    */
   private void addMatchTextAndAnswers(ItemFacade item, Map itemMap)
   {
 
     List sourceList = (List) itemMap.get("itemMatchSourceText");
     List targetList = (List) itemMap.get("itemMatchTargetText");
     List indexList = (List) itemMap.get("itemMatchIndex");
     List answerFeedbackList = (List) itemMap.get("itemFeedback");
     List correctMatchFeedbackList = (List) itemMap.get(
       "itemMatchCorrectFeedback");
     List incorrectMatchFeedbackList = (List) itemMap.get(
       "itemMatchIncorrectFeedback");
     List itemTextList = (List) itemMap.get("itemText");
 
     sourceList = sourceList == null ? new ArrayList() : sourceList;
     targetList = targetList == null ? new ArrayList() : targetList;
     indexList = indexList == null ? new ArrayList() : indexList;
     answerFeedbackList =
       answerFeedbackList == null ? new ArrayList() : answerFeedbackList;
     correctMatchFeedbackList =
       correctMatchFeedbackList ==
       null ? new ArrayList() : correctMatchFeedbackList;
     incorrectMatchFeedbackList =
       incorrectMatchFeedbackList ==
       null ? new ArrayList() : incorrectMatchFeedbackList;
 
     log.debug("*** original order");
     for (int i = 0; i < correctMatchFeedbackList.size(); i++) {
       log.debug("incorrectMatchFeedbackList.get(" + i + ")="+
                          incorrectMatchFeedbackList.get(i));
     }
     int maxNumCorrectFeedback = sourceList.size();
     int numIncorrectFeedback = incorrectMatchFeedbackList.size();
 
     if (maxNumCorrectFeedback>0 && numIncorrectFeedback>0)
     {
       incorrectMatchFeedbackList =
         reassembleIncorrectMatches(
           incorrectMatchFeedbackList, maxNumCorrectFeedback);
     }
 
     log.debug("*** NEW order");
     for (int i = 0; i < correctMatchFeedbackList.size(); i++) {
       log.debug("incorrectMatchFeedbackList.get(" + i + ")="+
                          incorrectMatchFeedbackList.get(i));
 
     }
 
     itemTextList =
       itemTextList == null ? new ArrayList() : itemTextList;
 
     if (targetList.size() <indexList.size())
     {
       log.debug("targetList.size(): " + targetList.size());
       log.debug("indexList.size(): " + indexList.size());
     }
 
     String itemTextString = "";
     if (itemTextList.size()>0)
     {
       itemTextString = (String) itemTextList.get(0);
     }
 
     HashSet itemTextSet = new HashSet();
 
     // first, add the question text
     if (itemTextString==null) itemTextString = "";
     itemTextString=itemTextString.replaceAll("\\?\\?"," ");//SAK-2298
     log.debug("item.setInstruction itemTextString: " + itemTextString);
     item.setInstruction(itemTextString);
 
     // loop through source texts indicating answers (targets)
     for (int i = 0; i < sourceList.size(); i++)
     {
       // create the entry for the matching item (source)
       String sourceText = (String) sourceList.get(i);
       if (sourceText == null) sourceText="";
       sourceText=sourceText.replaceAll("\\?\\?"," ");//SAK-2298
       log.debug("sourceText: " + sourceText);
 
       ItemText sourceItemText = new ItemText();
       sourceItemText.setText(sourceText);
       sourceItemText.setItem(item.getData());
       sourceItemText.setSequence(new Long(i + 1));
 
       // find the matching answer (target)
       HashSet targetSet = new HashSet();
       String targetString;
       int targetIndex = 999;// obviously not matching value
       try
       {
         targetIndex = Integer.parseInt( (String) indexList.get(i));
       }
       catch (NumberFormatException ex)
       {
         log.warn("No match for " + sourceText + "."); // default to no match
       }
       catch (IndexOutOfBoundsException ex)
       {
         log.error("Corrupt index list.  Cannot assign match for: " +sourceText + ".");
       }
       // loop through all possible targets (matching answers)
       char answerLabel = 'A';
       for (int a = 0; a < targetList.size(); a++)
       {
         targetString = (String) targetList.get(a);
         if (targetString == null)
         {
           targetString = "";
         }
         targetString=targetString.replaceAll("\\?\\?"," ");//SAK-2298
         log.debug("targetString: " + targetString);
 
 
         Answer target = new Answer();
 
         //feedback
         HashSet answerFeedbackSet = new HashSet();
 
         if (correctMatchFeedbackList.size() > i)
         {
           String fb = (String) correctMatchFeedbackList.get(i);
           answerFeedbackSet.add( new AnswerFeedback(
             target, AnswerFeedbackIfc.CORRECT_FEEDBACK, fb));
         }
         if (incorrectMatchFeedbackList.size() > i)
         {
           String fb = (String) incorrectMatchFeedbackList.get(i);
           log.debug("setting incorrect fb="+fb);
           answerFeedbackSet.add( new AnswerFeedback(
             target, AnswerFeedbackIfc.INCORRECT_FEEDBACK, fb));
         }
 
         target.setAnswerFeedbackSet(answerFeedbackSet);
 
         String label = "" + answerLabel++;
         target.setLabel(label); // up to 26, is this a problem?
         target.setText(targetString);
         target.setItemText(sourceItemText);
         target.setItem(item.getData());
         target.setSequence(new Long(a + 1));
 
         // correct answer and score
         // manual authoring disregards the number of partial answers
         // or whether the answer is correct so we will do the same.
 //        float score = 0;
         float score = getCorrectScore(item, 1);
 
         // if this answer is the indexed one, flag as correct
         if (a + 1 == targetIndex)
         {
           target.setIsCorrect(Boolean.TRUE);
 //          score = getCorrectScore(item, targetList.size());
           log.debug("source: " + sourceText + " matches target: " + targetString);
         }
         else
         {
           target.setIsCorrect(Boolean.FALSE);
         }
         log.debug("setting answer " + a + " score to:" + score);
         target.setScore(new Float(score));
 
         if (answerFeedbackList != null)
         {
           Set targetFeedbackSet = new HashSet();
           AnswerFeedback tAnswerFeedback = new AnswerFeedback();
           tAnswerFeedback.setAnswer(target);
           tAnswerFeedback.setTypeId(AnswerFeedbackIfc.GENERAL_FEEDBACK);
           String targetFeedback = "";
           if (answerFeedbackList.size()>0)
           {
             targetFeedback = (String) answerFeedbackList.get(targetIndex);
           }
           if (targetFeedback.length()>0)
           {
             tAnswerFeedback.setText( targetFeedback);
             targetFeedbackSet.add(tAnswerFeedback);
             target.setAnswerFeedbackSet(targetFeedbackSet);
           }
         }
         targetSet.add(target);
       }
 
       sourceItemText.setAnswerSet(targetSet);
       itemTextSet.add(sourceItemText);
     }
 
     item.setItemTextSet(itemTextSet);
   }
 
   /**
    * Helper method rotates the first n.
    * This will work with Samigo matching where
    * incorrect matches (n) = the square of the correct matches (n**2)
    * and the 0th displayfeedback is correct and the next n are incorrect
    * feedback.  In export Samigo uses the incorrect feedback redundantly.
    *
    * For example, if there are 5 matches, there are 25 matches and mismatched,
    * 5 of which are correct and 20 of which are not, so there is redundancy in
    * Samigo.
    *
    * In non-Samigo matching, there may be more than one incorrect
    * feedback for a failed matching.
    *
    * @param list the list
    * @return a reassembled list of size n
    */
   private List reassembleIncorrectMatches(List list, int n)
   {
     // make sure we have a reasonable value
     if (n<0) n = -n;
     if (n==0) return list;
 
     // pad input list if too small or null
     if (list == null)
       list = new ArrayList();
     for (int i = 0; i < n && list.size()<n +1; i++)
     {
       list.add("");
     }
 
     // our output value
     List newList = new ArrayList();
 
     // move the last of the n entries (0-index) up to the front
     newList.add(list.get(n-1));
 
     // add the 2nd entry and so forth
     for (int i = 0; i < n-1 ; i++)
     {
       String s = (String) list.get(i);
       newList.add(s);
     }
 
     return newList;
   }
 
   /**
    * helper method
    * @param s
    * @return
    */
   private boolean notNullOrEmpty(String s)
   {
     return s != null && s.trim().length() > 0 ?
         true : false;
   }
 
   /**
    * Append "  - 2", "  - 3", etc. incrementing as you go.
    * @param title the original
    * @return the title with versioning appended
    */
   public String renameDuplicate(String title)
   {
     if (title==null) title = "";
 
     String rename = "";
     int index = title.lastIndexOf(VERSION_START);
 
     if (index>-1)//if is versioned
     {
       String mainPart = "";
       String versionPart = title.substring(index);
       if (index > 0)
       {
         mainPart = title.substring(0, index);
       }
 
       int nindex = index + VERSION_START.length();
 
       String version = title.substring(nindex);
 
       int versionNumber = 0;
       try
       {
         versionNumber = Integer.parseInt(version);
         if (versionNumber < 2) versionNumber = 2;
         versionPart = VERSION_START + (versionNumber + 1);
 
         rename = mainPart + versionPart;
       }
       catch (NumberFormatException ex)
       {
         rename = title + VERSION_START + "2";
       }
     }
     else
     {
       rename = title + VERSION_START + "2";
     }
 
     return rename;
 
   }
 
   /**
    * Primarily for testing purposes.
    * @return an overridden path if not null
    */
   public String getOverridePath()
   {
     return overridePath;
   }
 
   /**
    * Primarily for testing purposes.
    * @param overridePath an overriding path
    */
   public void setOverridePath(String overridePath)
   {
     this.overridePath = overridePath;
   }
 
 }
