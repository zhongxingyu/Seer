 /**********************************************************************************
 * $HeadURL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
 
 package org.sakaiproject.tool.assessment.ui.listener.delivery;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Random;
 import java.util.StringTokenizer;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.ActionListener;
 import javax.faces.model.SelectItem;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentAccessControl;
 import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
 import org.sakaiproject.tool.assessment.data.dao.grading.ItemGradingData;
 import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
 import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
 import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
 import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
 import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;
 import org.sakaiproject.tool.assessment.facade.AgentFacade;
 import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
 import org.sakaiproject.tool.assessment.services.GradingService;
 import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
 import org.sakaiproject.tool.assessment.ui.bean.delivery.ContentsDeliveryBean;
 import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
 import org.sakaiproject.tool.assessment.ui.bean.delivery.FibBean;
 import org.sakaiproject.tool.assessment.ui.bean.delivery.ItemContentsBean;
 import org.sakaiproject.tool.assessment.ui.bean.delivery.MatchingBean;
 import org.sakaiproject.tool.assessment.ui.bean.delivery.SectionContentsBean;
 import org.sakaiproject.tool.assessment.ui.bean.delivery.SelectionBean;
 import org.sakaiproject.tool.assessment.ui.bean.evaluation.StudentScoresBean;
 import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
 
 /**
  * <p>Title: Samigo</p>
  * <p>Purpose:  this module creates the lists of published assessments for the select index
  * <p>Description: Sakai Assessment Manager</p>
  * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
  * <p>Organization: Sakai Project</p>
  * @author Ed Smiley
  * @version $Id$
  */
 
 public class DeliveryActionListener
   implements ActionListener
 {
 
   static String alphabet = new String("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
   private static Log log = LogFactory.getLog(DeliveryActionListener.class);
   private static ContextUtil cu;
 
   /**
    * ACTION.
    * @param ae
    * @throws AbortProcessingException
    */
   public void processAction(ActionEvent ae) throws
     AbortProcessingException
   {
     log.info("DeliveryActionListener.processAction() ");
 
     try
     {
       // get managed bean
       DeliveryBean delivery = (DeliveryBean) cu.lookupBean("delivery");
 
       // Clear elapsed time, set not timed out
       delivery.setTimeElapse(null);
       delivery.setTimeOutSubmission("false");
 
       String id = cu.lookupParam("publishedId");
       if (id == null)
       {
         id = delivery.getAssessmentId();
 
       }
       String showfeedbacknow = cu.lookupParam("showfeedbacknow");
       if (showfeedbacknow != null && showfeedbacknow.equals("true"))
       {
         delivery.setFeedback("true");
       }
       else
       {
         delivery.setFeedback("false");
 
       }
       String nofeedback = cu.lookupParam("nofeedback");
       if (nofeedback != null && nofeedback.equals("true"))
       {
         delivery.setNoFeedback("true");
       }
       else
       {
         delivery.setNoFeedback("false");
 
       }
       if (cu.lookupParam("partnumber") != null &&
           !cu.lookupParam("partnumber").trim().equals(""))
       {
         goToRightQuestionFromTOC(delivery);
       }
 
       String agent = AgentFacade.getAgentString();
       boolean forEvaluation = false;
       if (cu.lookupParam("studentid") != null &&
           !cu.lookupParam("studentid").trim().equals(""))
       {
         agent = cu.lookupParam("studentid");
         forEvaluation = true;
         if (cu.lookupParam("publishedIdd") != null)
         {
           id = cu.lookupParam("publishedIdd");
 
           // Reset all feedback to true, since this is the grader view
         }
       }
 
       // get service
       PublishedAssessmentService publishedAssessmentService = new
         PublishedAssessmentService();
 
       String previewAssessment = (String) cu.lookupParam("previewAssessment");
       String assessmentId = (String) cu.lookupParam("assessmentId");
 
       if (previewAssessment != null)
       {
         delivery.setPreviewAssessment(previewAssessment);
 
       }
       if ("true".equals(delivery.getPreviewAssessment()) && assessmentId != null)
       {
 
         id = publishedAssessmentService.getPublishedAssessmentId(assessmentId).
           toString();
       }
 
       // get assessment
       // Daisy, you need to check if it's null and if it's the right
       // one. :) --rmg
       PublishedAssessmentFacade publishedAssessment = null;
       if (delivery.getPublishedAssessment() != null &&
           delivery.getPublishedAssessment().getPublishedAssessmentId().toString().
           equals(id))
       {
         publishedAssessment = delivery.getPublishedAssessment();
       }
       else
       {
         publishedAssessment =
           publishedAssessmentService.getPublishedAssessment(id);
 
         // determine if scores will display to student
       }
       if (Boolean.TRUE.equals(
         publishedAssessment.getAssessmentFeedback().getShowStudentScore()))
       {
         delivery.setShowStudentScore(true);
       }
       else
       {
         delivery.setShowStudentScore(false);
       }
 
       GradingService service = new GradingService();
 
       HashMap itemData = null;
 
       // If this is a review, get everything submitted
       if (cu.lookupParam("review") != null &&
           cu.lookupParam("review").equals("true"))
       {
         itemData = service.getSubmitData(id, agent);
         setAssessmentGradingFromItemData(delivery, itemData);
       }
 
       // If this is for grading a student's responses, get those
       // responses.
       else if (forEvaluation)
       {
         itemData = service.getStudentGradingData
           (cu.lookupParam("gradingData"));
         setAssessmentGradingFromItemData(delivery, itemData);
       }
 
       // If we're reviewing an assessment and we're not showing
       // student responses, don't get them from the database.
       else if (delivery.getPreviewMode().equals("true") &&
                !delivery.getFeedbackComponent().getShowResponse())
       {
         itemData = new HashMap();
 
         // Otherwise, get them if they exist.
       }
       else
       {
         itemData = service.getLastItemGradingData(id, agent);
 
         // Get the assessmentGradingData to set timeElapsed
         Iterator keys = itemData.keySet().iterator();
         if (keys.hasNext())
         {
           ItemGradingData igd = (ItemGradingData) ( (ArrayList) itemData.get(
             keys.next())).toArray()[0];
           AssessmentGradingData agd =
             (AssessmentGradingData) igd.getAssessmentGrading();
           if (agd.getTimeElapsed() != null)
           {
             delivery.setTimeElapse(agd.getTimeElapsed().toString());
           }
           delivery.setAssessmentGrading(agd);
         }
         else
         {
           delivery.setAssessmentGrading(null);
         }
       }
       if (delivery.getTimeElapse() == null)
       {
         delivery.setTimeElapse("0");
 
         // If this was called instead of Begin
       }
       if (forEvaluation || delivery.getSettings() == null ||
           !delivery.getAssessmentId().equals
           (publishedAssessment.getPublishedAssessmentId().toString()))
       {
         BeginDeliveryActionListener listener =
           new BeginDeliveryActionListener();
         listener.populateBeanFromPub(delivery, publishedAssessment);
       }
 
       // If we're reviewing an assessment, set things differently
       String review = cu.lookupParam("review");
       if (forEvaluation || (review != null && review.equals("true")))
       {
         delivery.getSettings().setFormatByAssessment(true);
         delivery.getSettings().setFormatByPart(false);
         delivery.getSettings().setFormatByQuestion(false);
 
         delivery.setPreviewMode(true);
 
         if (forEvaluation ||
             (delivery.getFeedbackComponent().getShowImmediate() ||
              (delivery.getFeedbackComponent().getShowDateFeedback() &&
               delivery.getSettings().getFeedbackDate() != null &&
               delivery.getSettings().getFeedbackDate().before(new Date()))))
         {
           delivery.setFeedback("true");
 
         }
         if (forEvaluation)
         {
           setDeliveryFeedbackOnforEvaluation(delivery);
         }
         else
         {
           if (!delivery.getFeedbackComponent().getShowResponse())
           {
             itemData = new HashMap();
           }
         }
       }
 
       // We're going to overload itemData with the sequence in case
       // renumbering is turned off.
       itemData.put("sequence", new Long(0));
       long items = 0;
       int sequenceno = 1;
       Iterator i1 = publishedAssessment.getSectionArraySorted().iterator();
       while (i1.hasNext())
       {
         SectionDataIfc section = (SectionDataIfc) i1.next();
 
         //    items += section.getItemSet().size();  // bug 464
         Iterator i2 = null;
 
     	if (delivery.getForGrading()) {
 
        	  StudentScoresBean studentscorebean = (StudentScoresBean) cu.lookupBean("studentScores");
           long seed = (long) studentscorebean.getStudentId().hashCode();
           i2 = section.getItemArraySortedWithRandom(seed).iterator();
         }
         else {
           i2 = section.getItemArraySorted().iterator();
     }
 
 
 
         while (i2.hasNext())
         {
           items = items + 1; // bug 464
           ItemDataIfc item = (ItemDataIfc) i2.next();
           itemData.put("sequence" + item.getItemId().toString(),
                        new Integer(sequenceno++));
         }
       }
       itemData.put("items", new Long(items));
 
       if (delivery.getAssessmentGrading() != null)
       {
         delivery.setGraderComment
           (delivery.getAssessmentGrading().getComments());
       }
       else
       {
         delivery.setGraderComment(null);
 
 
         // Set the begin time if we're just starting
       }
       if (delivery.getBeginTime() == null)
       {
        log.info("grading delivery time = " + delivery.getAssessmentGrading().getAttemptDate());
         if (delivery.getAssessmentGrading() != null &&
             delivery.getAssessmentGrading().getAttemptDate() != null)
         {
           delivery.setBeginTime(delivery.getAssessmentGrading()
                                 .getAttemptDate());
         }
         else
         {
           delivery.setBeginTime(new Date());
         }
       }
 
       log.info("Set begin time " + delivery.getBeginTime());
       // get table of contents
       delivery.setTableOfContents(getContents(publishedAssessment, itemData,
                                               delivery));
 
       // get current page contents
       delivery.setPageContents(getPageContents(publishedAssessment,
                                                delivery, itemData));
     }
     catch (Exception e)
     {
       e.printStackTrace();
     }
 
   }
 
   private void setAssessmentGradingFromItemData(DeliveryBean delivery,
                                                 HashMap itemData)
   {
     Iterator keys = itemData.keySet().iterator();
     if (keys.hasNext())
     {
       ItemGradingData igd = (ItemGradingData) ( (ArrayList) itemData.get(
         keys.next())).toArray()[0];
       AssessmentGradingData agd =
         (AssessmentGradingData) igd.getAssessmentGrading();
       delivery.setAssessmentGrading(agd);
     }
   }
 
   /**
    * Put the setShows on.
    * @param delivery the delivery bean
    */
   private void setDeliveryFeedbackOnforEvaluation(DeliveryBean delivery)
   {
     delivery.getFeedbackComponent().setShowCorrectResponse(true);
     delivery.getFeedbackComponent().setShowGraderComment(true);
     delivery.getFeedbackComponent().setShowItemLevel(true);
     delivery.getFeedbackComponent().setShowQuestion(true);
     delivery.getFeedbackComponent().setShowResponse(true);
     delivery.getFeedbackComponent().setShowSelectionLevel(true);
     delivery.getFeedbackComponent().setShowStats(true);
     delivery.getFeedbackComponent().setShowStudentScore(true);
   }
 
   /**
    * Sets the delivery bean to the right place when navigating from TOC
    * @param delivery
    * @throws java.lang.NumberFormatException
    */
   private void goToRightQuestionFromTOC(DeliveryBean delivery) throws
     NumberFormatException
   {
     if (delivery.getSettings().isFormatByPart() ||
         delivery.getSettings().isFormatByQuestion())
     {
       delivery.setPartIndex(new Integer
                             (cu.lookupParam("partnumber")).intValue() - 1);
     }
     if (delivery.getSettings().isFormatByQuestion())
     {
       delivery.setQuestionIndex(new Integer
           (cu.lookupParam("questionnumber")).intValue() - 1);
 
     }
   }
 
   /**
    * Gets a table of contents bean
    * @param publishedAssessment the published assessment
    * @return
    */
   private ContentsDeliveryBean getContents(PublishedAssessmentFacade
                                            publishedAssessment,
                                            HashMap itemData,
                                            DeliveryBean delivery)
   {
     ContentsDeliveryBean contents = new ContentsDeliveryBean();
     float currentScore = 0;
     float maxScore = 0;
 
     // get parts
     ArrayList partSet = publishedAssessment.getSectionArraySorted();
     Iterator iter = partSet.iterator();
     ArrayList partsContents = new ArrayList();
     while (iter.hasNext())
     {
       SectionContentsBean partBean = getPartBean( (SectionDataIfc) iter.next(),
                                                  itemData, delivery);
       partBean.setNumParts(new Integer(partSet.size()).toString());
       currentScore += partBean.getPoints();
       maxScore += partBean.getMaxPoints();
       partsContents.add(partBean);
     }
 
     contents.setCurrentScore(currentScore);
     contents.setMaxScore(maxScore);
     contents.setPartsContents(partsContents);
     return contents;
   }
 
   /**
    * Gets a contents bean for the current page.
    * Really, just a wrapper utility to delegate to whichever
    * method handles the format being used.
    *
    * @todo these should actually take a copy of contents and filter it
    * for the page unstead of doing a recompute, which is less efficient
    * @param publishedAssessment the published assessment
    * @return
    */
   public ContentsDeliveryBean getPageContents(
     PublishedAssessmentFacade publishedAssessment,
     DeliveryBean delivery, HashMap itemData)
   {
 
     if (delivery.getSettings().isFormatByAssessment())
     {
       return getPageContentsByAssessment(publishedAssessment, itemData,
                                          delivery);
     }
 
     int itemIndex = delivery.getQuestionIndex();
     int sectionIndex = delivery.getPartIndex();
 
     if (delivery.getSettings().isFormatByPart())
     {
       return getPageContentsByPart(publishedAssessment, itemIndex, sectionIndex,
                                    itemData, delivery);
     }
     else if (delivery.getSettings().isFormatByQuestion())
     {
       return getPageContentsByQuestion(publishedAssessment, itemIndex,
                                        sectionIndex, itemData, delivery);
     }
 
     // default... ...shouldn't get here :O
     log.warn("delivery.getSettings().isFormatBy... is NOT set!");
     return getPageContentsByAssessment(publishedAssessment, itemData, delivery);
 
   }
 
   /**
    * Gets a contents bean for the current page if is format by assessment.
    *
    * @param publishedAssessment the published assessment
    * @return ContentsDeliveryBean for page
    */
   private ContentsDeliveryBean getPageContentsByAssessment(
     PublishedAssessmentFacade publishedAssessment, HashMap itemData,
     DeliveryBean delivery)
   {
     ContentsDeliveryBean contents = new ContentsDeliveryBean();
     float currentScore = 0;
     float maxScore = 0;
 
     // get parts
     ArrayList partSet = publishedAssessment.getSectionArraySorted();
     Iterator iter = partSet.iterator();
     ArrayList partsContents = new ArrayList();
     while (iter.hasNext())
     {
       SectionContentsBean partBean = getPartBean( (SectionDataIfc) iter.next(),
                                                  itemData, delivery);
       partBean.setNumParts(new Integer(partSet.size()).toString());
       currentScore += partBean.getPoints();
       maxScore += partBean.getMaxPoints();
       partsContents.add(partBean);
     }
 
     delivery.setPrevious(false);
     delivery.setContinue(false);
     contents.setCurrentScore(currentScore);
     contents.setMaxScore(maxScore);
     contents.setPartsContents(partsContents);
     contents.setShowStudentScore(delivery.isShowStudentScore());
     return contents;
   }
 
   /**
    * Gets a contents bean for the current page if is format by part.
    *
    * @param publishedAssessment the published assessment
    * @param itemIndex zero based item offset in part
    * @param sectionIndex zero based section offset in assessment
    * @return ContentsDeliveryBean for page
    */
   private ContentsDeliveryBean getPageContentsByPart(
     PublishedAssessmentFacade publishedAssessment,
     int itemIndex, int sectionIndex, HashMap itemData, DeliveryBean delivery)
   {
     ContentsDeliveryBean contents = new ContentsDeliveryBean();
     float currentScore = 0;
     float maxScore = 0;
     int sectionCount = 0;
 
     // get parts
     ArrayList partSet = publishedAssessment.getSectionArraySorted();
     Iterator iter = partSet.iterator();
     ArrayList partsContents = new ArrayList();
     while (iter.hasNext())
     {
       SectionContentsBean partBean = getPartBean( (SectionDataIfc) iter.next(),
                                                  itemData, delivery);
       partBean.setNumParts(new Integer(partSet.size()).toString());
       currentScore += partBean.getPoints();
       maxScore += partBean.getMaxPoints();
       if (sectionCount++ == sectionIndex)
       {
         partsContents.add(partBean);
         if (iter.hasNext())
         {
           delivery.setContinue(true);
         }
         else
         {
           delivery.setContinue(false);
         }
         if (sectionCount > 1)
         {
           delivery.setPrevious(true);
         }
         else
         {
           delivery.setPrevious(false);
         }
       }
     }
 
     contents.setCurrentScore(currentScore);
     contents.setMaxScore(maxScore);
     contents.setPartsContents(partsContents);
     contents.setShowStudentScore(delivery.isShowStudentScore());
     return contents;
   }
 
   /**
    * Gets a contents bean for the current page if is format by question.
    *
    * @param publishedAssessment the published assessment
    * @param itemIndex zero based item offset in part
    * @param sectionIndex zero based section offset in assessment
    * @return ContentsDeliveryBean for page
    */
   private ContentsDeliveryBean getPageContentsByQuestion(
     PublishedAssessmentFacade publishedAssessment,
     int itemIndex, int sectionIndex, HashMap itemData, DeliveryBean delivery)
   {
     ContentsDeliveryBean contents = new ContentsDeliveryBean();
     float currentScore = 0;
     float maxScore = 0;
     int sectionCount = 0;
     int questionCount = 0; // This is to increment the part if we run
     // out of questions
     // get parts
     ArrayList partSet = publishedAssessment.getSectionArraySorted();
     Iterator iter = partSet.iterator();
     ArrayList partsContents = new ArrayList();
     if (itemIndex < 0)
     {
       sectionIndex--;
       delivery.setPartIndex(sectionIndex);
     }
     while (iter.hasNext())
     {
       SectionDataIfc secFacade = (SectionDataIfc) iter.next();
       SectionContentsBean partBean = getPartBean(secFacade, itemData, delivery);
       partBean.setNumParts(new Integer(partSet.size()).toString());
       currentScore += partBean.getPoints();
       maxScore += partBean.getMaxPoints();
 
       //questionCount = secFacade.getItemSet().size();
       // need to  get ItemArraySort, insteand of getItemSet, to return corr number for random draw parts
       questionCount = secFacade.getItemArraySorted().size();
 
       if (itemIndex > (questionCount - 1) && sectionCount == sectionIndex)
       {
         sectionIndex++;
         delivery.setPartIndex(sectionIndex);
         itemIndex = 0;
         delivery.setQuestionIndex(itemIndex);
       }
       if (itemIndex < 0 && sectionCount == sectionIndex)
       {
         itemIndex = questionCount - 1;
         delivery.setQuestionIndex(itemIndex);
       }
 
       if (sectionCount++ == sectionIndex)
       {
         SectionContentsBean partBeanWithQuestion =
           this.getPartBeanWithOneQuestion(secFacade, itemIndex, itemData,
                                           delivery);
         partBeanWithQuestion.setNumParts(new Integer(partSet.size()).toString());
         partsContents.add(partBeanWithQuestion);
 
         if (iter.hasNext() || itemIndex < (questionCount - 1))
         {
           delivery.setContinue(true);
         }
         else
         {
           delivery.setContinue(false);
         }
         if (itemIndex > 0 || sectionIndex > 0)
         {
           delivery.setPrevious(true);
         }
         else
         {
           delivery.setPrevious(false);
         }
       }
     }
 
     contents.setCurrentScore(currentScore);
     contents.setMaxScore(maxScore);
     contents.setPartsContents(partsContents);
     contents.setShowStudentScore(delivery.isShowStudentScore());
     return contents;
   }
 
   /**
      * Populate a SectionContentsBean properties and populate with ItemContentsBean
    * @param part this section
    * @return
    */
   private SectionContentsBean getPartBean(SectionDataIfc part, HashMap itemData,
                                           DeliveryBean delivery)
   {
     float maxPoints = 0;
     float points = 0;
     int unansweredQuestions = 0;
 
     SectionContentsBean sec = new SectionContentsBean();
 
     ArrayList itemSet = null;
     if (delivery.getForGrading()) {
 
       StudentScoresBean studentscorebean = (StudentScoresBean) cu.lookupBean("studentScores");
       long seed = (long) studentscorebean.getStudentId().hashCode();
       itemSet = part.getItemArraySortedWithRandom(seed);
     }
     else {
       itemSet = part.getItemArraySorted();
     }
 
     sec.setQuestions(itemSet.size());
 
     if (delivery.getSettings().getItemNumbering().equals
         (AssessmentAccessControl.RESTART_NUMBERING_BY_PART.toString()))
     {
       sec.setNumbering(itemSet.size());
     }
     else
     {
       sec.setNumbering( ( (Long) itemData.get("items")).intValue());
     }
 
     sec.setText(part.getTitle());
     sec.setDescription(part.getDescription());
     sec.setNumber("" + part.getSequence());
 
 // check metadata for authoring type
     sec.setMetaData(part);
 
     Iterator iter = itemSet.iterator();
     ArrayList itemContents = new ArrayList();
     int i = 0;
     while (iter.hasNext())
     {
       ItemDataIfc thisitem = (ItemDataIfc) iter.next();
       ItemContentsBean itemBean = getQuestionBean(thisitem,
                                                   itemData, delivery);
 
       // Deal with numbering
       itemBean.setNumber(++i);
       if (delivery.getSettings().getItemNumbering().equals
           (AssessmentAccessControl.RESTART_NUMBERING_BY_PART.toString()))
       {
         itemBean.setSequence(new Integer(itemBean.getNumber()).toString());
       }
       else
       {
         itemBean.setSequence( ( (Integer) itemData.get("sequence" +
           thisitem.getItemId().toString())).toString());
       }
 
       // scoring
       maxPoints += itemBean.getMaxPoints();
       points += itemBean.getPoints();
       itemBean.setShowStudentScore(delivery.isShowStudentScore());
 
       if (itemBean.isUnanswered())
       {
         unansweredQuestions++;
       }
       itemContents.add(itemBean);
     }
 
     // scoring information
     // Round to the nearest 1/10th.
     int tmp = Math.round(maxPoints * 10.0f);
     maxPoints = (float) tmp / 10.0f;
     sec.setMaxPoints(maxPoints);
 
     tmp = Math.round(points * 10.0f);
     points = (float) tmp / 10.0f;
     sec.setPoints(points);
 
     sec.setShowStudentScore(delivery.isShowStudentScore());
 
     sec.setUnansweredQuestions(unansweredQuestions);
     sec.setItemContents(itemContents);
 
     return sec;
   }
 
   /**
      * Populate a SectionContentsBean properties and populate with ItemContentsBean
    * @param part this section
    * @return
    */
   private SectionContentsBean getPartBeanWithOneQuestion(
     SectionDataIfc part, int itemIndex, HashMap itemData, DeliveryBean delivery)
   {
     float maxPoints = 0;
     float points = 0;
     int unansweredQuestions = 0;
     int itemCount = 0;
 
     SectionContentsBean sec = new SectionContentsBean();
     ArrayList itemSet = part.getItemArraySorted();
 
     sec.setQuestions(itemSet.size());
 
     if (delivery.getSettings().getItemNumbering().equals
         (AssessmentAccessControl.RESTART_NUMBERING_BY_PART.toString()))
     {
       sec.setNumbering(itemSet.size());
     }
     else
     {
       sec.setNumbering( ( (Long) itemData.get("items")).intValue());
     }
 
     sec.setText(part.getTitle());
     sec.setDescription(part.getDescription());
     sec.setNumber("" + part.getSequence());
 
     // get items
     Iterator iter = itemSet.iterator();
     ArrayList itemContents = new ArrayList();
     int i = 0;
     while (iter.hasNext())
     {
       ItemDataIfc thisitem = (ItemDataIfc) iter.next();
       ItemContentsBean itemBean = getQuestionBean(thisitem,
                                                   itemData, delivery);
 
       // Numbering
       itemBean.setNumber(++i);
       if (delivery.getSettings().getItemNumbering().equals
           (AssessmentAccessControl.RESTART_NUMBERING_BY_PART.toString()))
       {
         itemBean.setSequence(new Integer(itemBean.getNumber()).toString());
       }
       else
       {
         itemBean.setSequence( ( (Integer) itemData.get("sequence" +
           thisitem.getItemId().toString())).toString());
       }
 
       // scoring
       maxPoints += itemBean.getMaxPoints();
       points += itemBean.getPoints();
       itemBean.setShowStudentScore(delivery.isShowStudentScore());
 
       if (itemBean.isUnanswered())
       {
         unansweredQuestions++;
       }
       if (itemCount++ == itemIndex)
       {
         itemContents.add(itemBean);
       }
     }
 
     // scoring information
     // Round to the nearest 1/10th.
     int tmp = Math.round(maxPoints * 10.0f);
     maxPoints = (float) tmp / 10.0f;
     sec.setMaxPoints(maxPoints);
 
     tmp = Math.round(points * 10.0f);
     points = (float) tmp / 10.0f;
     sec.setPoints(points);
 
     sec.setShowStudentScore(delivery.isShowStudentScore());
 
     sec.setUnansweredQuestions(unansweredQuestions);
     sec.setItemContents(itemContents);
 
     return sec;
   }
 
   /**
    * populate a single ItemContentsBean from an item for delivery
    * @param item  an Item
    * @return
    */
   private ItemContentsBean getQuestionBean(ItemDataIfc item, HashMap itemData,
                                            DeliveryBean delivery)
   {
     ItemContentsBean itemBean = new ItemContentsBean();
     itemBean.setItemData(item);
     itemBean.setMaxPoints(item.getScore().floatValue());
     itemBean.setPoints( (float) 0);
 
     // update maxNumAttempts for audio
     if (item.getTriesAllowed() != null)
     {
       itemBean.setTriesAllowed(item.getTriesAllowed());
     }
 
     // save timeallowed for audio recording
     if (item.getDuration() != null)
     {
       itemBean.setDuration(item.getDuration());
     }
 
     itemBean.setItemGradingDataArray
       ( (ArrayList) itemData.get(item.getItemId()));
 
     // Set comments and points
     Iterator i = itemBean.getItemGradingDataArray().iterator();
     while (i.hasNext())
     {
       ItemGradingData data = (ItemGradingData) i.next();
       // All itemgradingdata comments for the same item are identical
       itemBean.setGradingComment(data.getComments());
       if (data.getAutoScore() != null)
       {
         itemBean.setPoints(itemBean.getPoints() +
                            data.getAutoScore().floatValue());
       }
     }
 
     if (item.getTypeId().toString().equals("5") ||
         item.getTypeId().toString().equals("6") ||
         item.getTypeId().toString().equals("3") ||
         item.getTypeId().toString().equals("7"))
     {
       itemBean.setFeedback(item.getGeneralItemFeedback());
     }
     else if (itemBean.getPoints() >= itemBean.getMaxPoints())
     {
       itemBean.setFeedback(item.getCorrectItemFeedback());
     }
     else
     {
       itemBean.setFeedback(item.getInCorrectItemFeedback());
 
       // Do we randomize answer list?
     }
     boolean randomize = false;
     i = item.getItemMetaDataSet().iterator();
     while (i.hasNext())
     {
       ItemMetaDataIfc meta = (ItemMetaDataIfc) i.next();
       if (meta.getLabel().equals(ItemMetaDataIfc.RANDOMIZE))
       {
         if (meta.getEntry().equals("true"))
         {
           randomize = true;
           break;
         }
       }
     }
 
     ArrayList myanswers = new ArrayList();
 
     // Generate the answer key
     String key = "";
     Iterator key1 = item.getItemTextArraySorted().iterator();
     int j = 1;
     while (key1.hasNext())
     {
       // We need to store the answers in an arraylist in case they're
       // randomized -- we assign labels here, and then step through
       // them again later, and we have to make sure the order is the
       // same each time.
       myanswers = new ArrayList(); // Start over each time so we don't
       // get duplicates.
       ItemTextIfc text = (ItemTextIfc) key1.next();
       Iterator key2 = null;
 
       // Never randomize Fill-in-the-blank, always randomize matching
       if ( (randomize && !item.getTypeId().toString().equals("8")) ||
           item.getTypeId().toString().equals("9"))
       {
         ArrayList shuffled = new ArrayList();
         Iterator i1 = text.getAnswerArraySorted().iterator();
         while (i1.hasNext())
         {
           shuffled.add(i1.next());
 
           // Randomize matching the same way for each
         }
         if (item.getTypeId().toString().equals("9"))
         {
 /*
           Collections.shuffle(shuffled,
                               new Random( (long) item.getText().hashCode()));
 */
           Collections.shuffle(shuffled,
                               new Random( (long) item.getText().hashCode() +
           			AgentFacade.getAgentString().hashCode()));
         }
         else
         {
           Collections.shuffle(shuffled,
                               new Random( (long) item.getText().hashCode()));
 /*
           Collections.shuffle(shuffled,
                               new Random( (long) item.getText().hashCode() +
                                          AgentFacade.getAgentString().hashCode()));
 */
         }
         key2 = shuffled.iterator();
       }
       else
       {
         key2 = text.getAnswerArraySorted().iterator();
       }
       int k = 0;
       while (key2.hasNext())
       {
         AnswerIfc answer = (AnswerIfc) key2.next();
 
         // Don't save the answer if it has no text
         if ( (answer.getText() == null || answer.getText().trim().equals(""))
             && (item.getTypeId().toString().equals("1") ||
                 item.getTypeId().toString().equals("2") ||
                 item.getTypeId().toString().equals("3")))
         {
           // Ignore, it's a null answer
         }
         else
         {
           // Set the label and key
           if (item.getTypeId().toString().equals("1") ||
               item.getTypeId().toString().equals("2") ||
               item.getTypeId().toString().equals("9"))
           {
             answer.setLabel(new Character(alphabet.charAt(k++)).toString());
             if (answer.getIsCorrect() != null &&
                 answer.getIsCorrect().booleanValue())
             {
               String addition = "";
               if (item.getTypeId().toString().equals("9"))
               {
                 addition = new Integer(j++).toString() + ":";
               }
               if (key.equals(""))
               {
                 key += addition + answer.getLabel();
               }
               else
               {
                 key += ", " + addition + answer.getLabel();
               }
             }
           }
           if (item.getTypeId().toString().equals("4") &&
               answer.getIsCorrect() != null &&
               answer.getIsCorrect().booleanValue())
           {
             key = (answer.getText().equalsIgnoreCase("true") ? "True" : "False");
           }
           if (item.getTypeId().toString().equals("5") ||
               item.getTypeId().toString().equals("6") ||
               item.getTypeId().toString().equals("7"))
           {
             key += answer.getText();
           }
           if (item.getTypeId().toString().equals("8"))
           {
             if (key.equals(""))
             {
               key += answer.getText();
             }
             else
             {
               key += ", " + answer.getText();
             }
           }
           myanswers.add(answer);
         }
       }
     }
     itemBean.setKey(key);
 
     // Delete this
     itemBean.setShuffledAnswers(myanswers);
 
     // This creates the list of answers for an item
     ArrayList answers = new ArrayList();
     if (item.getTypeId().toString().equals("1") ||
         item.getTypeId().toString().equals("2") ||
         item.getTypeId().toString().equals("3") ||
         item.getTypeId().toString().equals("4") ||
         item.getTypeId().toString().equals("9"))
     {
       Iterator iter = myanswers.iterator();
       while (iter.hasNext())
       {
         SelectionBean selectionBean = new SelectionBean();
         selectionBean.setItemContentsBean(itemBean);
         AnswerIfc answer = (AnswerIfc) iter.next();
         selectionBean.setAnswer(answer);
 
         // It's saved lower case in the db -- this is a kludge
         if (item.getTypeId().toString().equals("4") && // True/False
             answer.getText().equals("true"))
         {
           answer.setText("True");
         }
         if (item.getTypeId().toString().equals("4") && // True/False
             answer.getText().equals("false"))
         {
           answer.setText("False");
 
         }
         String label = "";
         if (answer.getLabel() == null)
         {
           answer.setLabel("");
 
           // Delete this when everything works.
         }
         if (!answer.getLabel().equals(""))
         {
           label += answer.getLabel() + ". " + answer.getText();
         }
         else
         {
           label = answer.getText();
 
           // Set the response to true or false for each answer
         }
         selectionBean.setResponse(false);
         Iterator iter1 = itemBean.getItemGradingDataArray().iterator();
         while (iter1.hasNext())
         {
           ItemGradingData data = (ItemGradingData) iter1.next();
           if (data.getPublishedAnswer() != null &&
               (data.getPublishedAnswer().equals(answer) ||
                data.getPublishedAnswer().getId().equals(answer.getId())))
           {
             selectionBean.setItemGradingData(data);
             selectionBean.setResponse(true);
           }
         }
 
         if (delivery.getFeedbackComponent() != null &&
             delivery.getFeedback().equals("true") &&
             delivery.getFeedbackComponent().getShowSelectionLevel())
         {
           // If right answer, set feedback to correct, otherwise incorrect
           if (answer.getIsCorrect() == null)
           {
             selectionBean.setFeedback(answer.getGeneralAnswerFeedback());
           }
           else if (selectionBean.getResponse() &&
                    answer.getIsCorrect().booleanValue() ||
                    !selectionBean.getResponse() &&
                    !answer.getIsCorrect().booleanValue())
           {
             selectionBean.setFeedback(answer.getCorrectAnswerFeedback());
           }
           else
           {
             selectionBean.setFeedback(answer.getInCorrectAnswerFeedback());
 
           }
         }
 
         // Delete this
         String description = "";
         if (delivery.getFeedback().equals("true") &&
             delivery.getFeedbackComponent().getShowCorrectResponse() &&
             answer.getIsCorrect() != null)
         {
           description = answer.getIsCorrect().toString();
 
           // Delete this
         }
         SelectItem newItem =
           new SelectItem(answer.getId().toString(), label, description);
 
         if (item.getTypeId().toString().equals("4"))
         {
           answers.add(newItem);
         }
         else
         {
           answers.add(selectionBean);
         }
       }
     }
     // Delete this
     itemBean.setAnswers(answers);
     itemBean.setSelectionArray(answers);
 
     if (item.getTypeId().toString().equals("9")) // matching
     {
       populateMatching(item, itemBean);
 
     }
     if (item.getTypeId().toString().equals("8")) // fill in the blank
     {
       populateFib(item, itemBean);
 
       // round the points to the nearest tenth
 
     }
     float alignment = itemBean.getMaxPoints();
     // Round to the nearest 1/10th.
     int tmp = Math.round(alignment * 10.0f);
     alignment = (float) tmp / 10.0f;
     itemBean.setMaxPoints(alignment);
 
     alignment = itemBean.getPoints();
     // Round to the nearest 1/10th.
     tmp = Math.round(alignment * 10.0f);
     alignment = (float) tmp / 10.0f;
     itemBean.setPoints(alignment);
 
     return itemBean;
   }
 
   public void populateMatching(ItemDataIfc item, ItemContentsBean bean)
   {
     Iterator iter = item.getItemTextArraySorted().iterator();
     int j = 1;
     ArrayList beans = new ArrayList();
     ArrayList newAnswers = null;
     while (iter.hasNext())
     {
       ItemTextIfc text = (ItemTextIfc) iter.next();
       MatchingBean mbean = new MatchingBean();
       newAnswers = new ArrayList();
       mbean.setText(new Integer(j++).toString() + ". " + text.getText());
       mbean.setItemText(text);
       mbean.setItemContentsBean(bean);
 
       ArrayList choices = new ArrayList();
       ArrayList shuffled = new ArrayList();
       Iterator iter2 = text.getAnswerArraySorted().iterator();
       while (iter2.hasNext())
       {
         shuffled.add(iter2.next());
 
       }
       Collections.shuffle(shuffled,
 	new Random( (long) item.getText().hashCode() +
 	AgentFacade.getAgentString().hashCode()));
 
 /*
       Collections.shuffle
         (shuffled, new Random( (long) item.getText().hashCode()));
 */
       iter2 = shuffled.iterator();
 
       int i = 0;
       choices.add(new SelectItem("0", "select", "")); // default value for choice
       while (iter2.hasNext())
       {
         AnswerIfc answer = (AnswerIfc) iter2.next();
         newAnswers.add(new Character(alphabet.charAt(i)).toString() +
                        ". " + answer.getText());
         choices.add(new SelectItem(answer.getId().toString(),
                                    new Character(alphabet.charAt(i++)).toString(),
                                    ""));
       }
 
       mbean.setChoices(choices); // Set the A/B/C... pulldown
 
       iter2 = bean.getItemGradingDataArray().iterator();
       while (iter2.hasNext())
       {
 
         ItemGradingData data = (ItemGradingData) iter2.next();
 
         if (data.getPublishedItemText().getId().equals(text.getId()))
         {
           // We found an existing grading data for this itemtext
           mbean.setItemGradingData(data);
           if (data.getPublishedAnswer() != null)
           {
             mbean.setResponse(data.getPublishedAnswer().getId()
                               .toString());
             if (data.getPublishedAnswer().getIsCorrect() != null &&
                 data.getPublishedAnswer().getIsCorrect().booleanValue())
             {
               mbean.setFeedback(data.getPublishedAnswer()
                                 .getCorrectAnswerFeedback());
             }
             else
             {
               mbean.setFeedback(data.getPublishedAnswer()
                                 .getInCorrectAnswerFeedback());
             }
           }
           break;
         }
       }
 
       beans.add(mbean);
     }
     bean.setMatchingArray(beans);
     bean.setAnswers(newAnswers); // Change the answers to just text
   }
 
   public void populateFib(ItemDataIfc item, ItemContentsBean bean)
   {
     // Only one text in FIB
     ItemTextIfc text = (ItemTextIfc) item.getItemTextArraySorted().toArray()[0];
     ArrayList fibs = new ArrayList();
     ArrayList texts = new ArrayList();
     String alltext = new String(text.getText());
     while (alltext.indexOf("{") > -1)
     {
       String tmp = alltext.substring(0, alltext.indexOf("{"));
       alltext = alltext.substring(alltext.indexOf("}") + 1);
       texts.add(tmp);
     }
     texts.add(alltext);
     int i = 0;
     Iterator iter = text.getAnswerArraySorted().iterator();
     while (iter.hasNext())
     {
       AnswerIfc answer = (AnswerIfc) iter.next();
       FibBean fbean = new FibBean();
       fbean.setItemContentsBean(bean);
       fbean.setAnswer(answer);
       fbean.setText( (String) texts.toArray()[i++]);
       fbean.setHasInput(true);
 
       ArrayList datas = bean.getItemGradingDataArray();
       if (datas == null || datas.isEmpty())
       {
         fbean.setIsCorrect(false);
       }
       else
       {
         Iterator iter2 = datas.iterator();
         while (iter2.hasNext())
         {
           ItemGradingData data = (ItemGradingData) iter2.next();
           if (data.getPublishedAnswer().getId().equals(answer.getId()))
           {
             fbean.setItemGradingData(data);
             fbean.setResponse(data.getAnswerText());
             fbean.setIsCorrect(false);
             if (answer.getText() == null)
             {
               answer.setText("");
             }
             StringTokenizer st2 = new StringTokenizer(answer.getText(), "|");
             while (st2.hasMoreTokens())
             {
               String nextT = st2.nextToken();
               if (data.getAnswerText() != null &&
                   data.getAnswerText().equalsIgnoreCase(nextT))
               {
                 fbean.setIsCorrect(true);
               }
             }
           }
         }
       }
       fibs.add(fbean);
     }
 
     FibBean fbean = new FibBean();
     fbean.setText( (String) texts.toArray()[i]);
     fbean.setHasInput(false);
     fibs.add(fbean);
 
     bean.setFibArray(fibs);
   }
 }
