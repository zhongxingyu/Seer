 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.core.businessprocess;
 
 import java.util.Date;
 import java.util.Map;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.time.DurationFormatUtils;
 import org.dom4j.Document;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.eclipse.jubula.client.core.model.IAUTMainPO;
 import org.eclipse.jubula.client.core.model.ICapPO;
 import org.eclipse.jubula.client.core.model.IEventExecTestCasePO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.ITestCasePO;
 import org.eclipse.jubula.client.core.model.ITestResult;
 import org.eclipse.jubula.client.core.model.ITestSuitePO;
 import org.eclipse.jubula.client.core.model.TestResultNode;
 import org.eclipse.jubula.client.core.model.TestResultParameter;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.i18n.CompSystemI18n;
 import org.eclipse.jubula.tools.i18n.I18n;
 import org.eclipse.jubula.tools.objects.event.TestErrorEvent;
 import org.eclipse.jubula.tools.utils.TimeUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * @author BREDEX GmbH
  * @created 05.08.2005
  */
 public abstract class AbstractXMLReportGenerator {
     
     /** The logger */
     private static final Logger LOG = LoggerFactory.getLogger(
         AbstractXMLReportGenerator.class);
 
     /** the generated report */
     private Document m_document;
 
     /** the Test Result for which to write a report */
     private ITestResult m_testResult;
     
     /**
      * Constructor
      * 
      * @param testResult The Test Result for which to write a report.
      */
     public AbstractXMLReportGenerator(ITestResult testResult) {
         m_document = DocumentHelper.createDocument();
         m_testResult = testResult;
     }
     
     /**
      * generates a readable xml file
      * @return
      *      XML File as String
      */
     public abstract Document generateXmlReport();
     
     /**
      * Generates the basic, generic information for an XML report.
      * 
      * @return The "project" element of the XML document.
      */
     protected Element generateHeader() {
         if (LOG.isInfoEnabled()) {
             LOG.info("Generating Result XML Report"); //$NON-NLS-1$
         }
         
         m_document.normalize();
         m_document.addComment("<?xml-stylesheet type=\"text/xsl\" href=\"format.xsl\"?>"); //$NON-NLS-1$
 
         Element root = m_document.addElement("report"); //$NON-NLS-1$
         root.addAttribute("style", getStyleName()); //$NON-NLS-1$
         Element general = root.addElement("project"); //$NON-NLS-1$
         
         general.addElement("name").//$NON-NLS-1$
             addText(m_testResult.getProjectName());
 
        general.addElement("version").//$NON-NLS-1$
                addText(m_testResult.getProjectMajorVersion()
                        + StringConstants.DOT
                        + m_testResult.getProjectMinorVersion());
 
         Date startTime = m_testResult.getStartTime();
         if (startTime != null) {
             general.addElement("test-start").addText(startTime.toString()); //$NON-NLS-1$
         }
         Date endTime = m_testResult.getEndTime();
         if (endTime != null) {
             general.addElement("test-end").addText(endTime.toString()); //$NON-NLS-1$
         }
 
         if (startTime != null && endTime != null) {
             general.addElement("test-length").//$NON-NLS-1$
                 addText(TimeUtil.getDurationString(startTime, endTime));
         }
         
         addStepCountElements(m_testResult, general);
         
         general.addElement("language").addText(m_testResult.getTestLanguage()); //$NON-NLS-1$
         
         return general;
     }
 
     /**
      * Generates and adds test step count sub-elements to the given 
      * element.
      * 
      * @param result The Test Result from which to retrieve the step count 
      *               information.
      * @param general The parent element to which the step count elements
      *                will be added.
      */
     private void addStepCountElements(ITestResult result, Element general) {
         int expectedNumberOfSteps = 
             result.getExpectedNumberOfSteps();
         int numberOfStepsExecuted = 
             result.getNumberOfTestedSteps();
         int numberOfEventHandlerSteps =
             result.getNumberOfEventHandlerSteps();
         int numberOfFailedTestSteps = 
             result.getNumberOfFailedSteps();
         
         general.addElement("expectedNumSteps").addText(//$NON-NLS-1$
                 String.valueOf(expectedNumberOfSteps));
         general.addElement("numStepsTested").addText(//$NON-NLS-1$
             String.valueOf(numberOfStepsExecuted));
         general.addElement("numEventHandlerSteps").addText(//$NON-NLS-1$
                 String.valueOf(numberOfEventHandlerSteps));
         general.addElement("numFailedSteps").addText(//$NON-NLS-1$
                 String.valueOf(numberOfFailedTestSteps));
     }
     
     /**
      * 
      * @param parent
      *      The XML element to which the result for node will be added.
      *      This can be considered the parent of the returned element.
      * @return the created element.
      */
     protected Element buildRootElement(Element parent) {
         return buildElement(parent, m_testResult.getRootResultNode());
     }
     
     /**
      * Builds and returns a test report element. Subclasses can create an entire
      * test result hierarchy by recursively calling this method, using the 
      * returned element as an argument for the next method call.
      * 
      * @param resultNode
      *      <code>TestResultNode</code> to translate to an XML element.
      * @param element
      *      The XML element to which the result for node will be added.
      *      This can be considered the parent of the returned element.
      * @return The <code>Element</code> created.
      */
     protected Element buildElement(Element element, 
         TestResultNode resultNode) {
         Element insertInto = element;
         Object node = resultNode.getNode();
         if (node instanceof ITestSuitePO) {
             ITestSuitePO ts = (ITestSuitePO) node;
             Element suite = element.addElement("testsuite"); //$NON-NLS-1$
             insertInto = suite;
             addGeneralElements(resultNode, insertInto);
             IAUTMainPO aut = ts.getAut();
             Element autEl = suite.addElement("aut"); //$NON-NLS-1$
             autEl.addElement("name").addText(aut.getName()); //$NON-NLS-1$
             autEl.addElement("config").addText(m_testResult.getAutConfigName()); //$NON-NLS-1$
             autEl.addElement("server").addText(m_testResult.getAutAgentHostName()); //$NON-NLS-1$
             autEl.addElement("cmdline-parameter").setText(m_testResult.getAutArguments()); //$NON-NLS-1$
             insertInto = suite.addElement("test-run"); //$NON-NLS-1$
         } else if (node instanceof IEventExecTestCasePO) {
             insertInto = element.addElement("eventhandler"); //$NON-NLS-1$
             addGeneralElements(resultNode, insertInto);
             Element typeEl = insertInto.addElement("type"); //$NON-NLS-1$
             typeEl.addText(I18n.getString(
                 ((IEventExecTestCasePO)node).getEventType()));
             Element reentryEl = insertInto.addElement("reentry-property"); //$NON-NLS-1$
             reentryEl.addText(((IEventExecTestCasePO)node).
                 getReentryProp().toString());
         } else if (node instanceof ITestCasePO) {
             insertInto = element.addElement("testcase"); //$NON-NLS-1$
             addGeneralElements(resultNode, insertInto);
         } else if (node instanceof ICapPO) {
             insertInto = element.addElement("step"); //$NON-NLS-1$
             addGeneralElements(resultNode, insertInto);
             addCapElements(resultNode, insertInto, (ICapPO)node);
         }
 
         addParamNodeElements(resultNode, insertInto);
         
         return insertInto;
     }
 
     /**
      * Adds Parameter elements to the given element based on the given 
      * Test Result Node.
      * 
      * @param resultNode The source for Parameter data.
      * @param insertInto The target for the Parameter data.
      */
     protected void addParamNodeElements(
             TestResultNode resultNode, Element insertInto) {
         
         for (TestResultParameter parameter : resultNode.getParameters()) {
             String name = parameter.getName();
             String type = parameter.getType();
             String value = parameter.getValue();
             
             Element paramEl = insertInto.addElement("parameter"); //$NON-NLS-1$
             
             if (name != null) {
                 Element paramNameEl = paramEl.addElement("parameter-name"); //$NON-NLS-1$
                 paramNameEl.addText(name);
             }
 
             if (type != null) {
                 Element paramTypeEl = paramEl.addElement("parameter-type"); //$NON-NLS-1$
                 paramTypeEl.addText(type);
             }
 
             if (value != null) {
                 Element paramValueEl = paramEl.addElement("parameter-value"); //$NON-NLS-1$
                 paramValueEl.addText(value);
             }
 
             if (!paramEl.hasContent()) {
                 insertInto.remove(paramEl);
             }
         }
     }
     
     /**
      * adds information for a Cap to the XML file
      * 
      * @param resultNode
      *      the actual node
      * @param insertInto
      *      where to insert elements in xml
      * @param node
      *      NodePO
      */
     protected void addCapElements(TestResultNode resultNode, 
             Element insertInto, ICapPO node) {
         ICapPO cap = node;
         getTimestampFromResultNode(resultNode, insertInto);
         Element compEl = insertInto.addElement("component-name"); //$NON-NLS-1$
         compEl.addText(
             StringUtils.defaultString(resultNode.getComponentName()));
         Element compTypeEl = insertInto.addElement("component-type"); //$NON-NLS-1$
         compTypeEl.addText(CompSystemI18n.getString(cap.getComponentType(), 
             true));
         Element actionEl = insertInto.addElement("action-type"); //$NON-NLS-1$
         actionEl.addText(CompSystemI18n.getString(cap.getActionName(), true));
         
         if (resultNode.getStatus() == TestResultNode.ERROR 
                 || resultNode.getStatus() == TestResultNode.RETRYING) {
             
             Element error = insertInto.addElement("error"); //$NON-NLS-1$
             Element errorType = error.addElement("type"); //$NON-NLS-1$
             TestErrorEvent event = resultNode.getEvent();
             if (event != null) {
                 errorType.addText(I18n.getString(event.getId(), true));
                 Map<Object, Object> eventProps = event.getProps();
                 if (eventProps.containsKey(
                         TestErrorEvent.Property.DESCRIPTION_KEY)) {
                     String key = (String) eventProps.get(
                             TestErrorEvent.Property.DESCRIPTION_KEY);
                     Object[] args = (Object[]) eventProps.get(
                             TestErrorEvent.Property.PARAMETER_KEY);
                     args = args != null ? args : new Object[0];
                     Element mapEntry = error.addElement("description"); //$NON-NLS-1$
                     if (mapEntry != null && key != null) {
                         mapEntry.addText(resultNode.hasBackingNode() ? String
                                 .valueOf(I18n.getString(key, args)) : key);
                     }
                 } else {
                     for (Map.Entry<Object, Object> entry 
                             : eventProps.entrySet()) {
                         if (!TestErrorEvent.Property.DESCRIPTION_KEY.equals(
                                 entry.getKey())) {
                             Element mapEntry = 
                                     error.addElement((String) entry.getKey());
                             mapEntry.addText(String.valueOf(entry.getValue()));
                         }
                     }
                 }
             }
             final byte[] screenshotData = resultNode.getScreenshot();
             if (screenshotData != null) {
                 Element screenshotElement = error.addElement("screenshot"); //$NON-NLS-1$
                 screenshotElement.addText(new String(
                         Base64.encodeBase64(screenshotData, false)));
             }
         }
     }
 
     /**
      * @param resultNode
      *      the actual node
      * @param insertInto
      *      where to insert elements in xml 
      */
     private void getTimestampFromResultNode(TestResultNode resultNode,
             Element insertInto) {
         Element timestampEL = insertInto.addElement("timestamp"); //$NON-NLS-1$
         Date time = resultNode.getTimeStamp();
         if (time != null) {
             String timestamp = time.toString();
             timestampEL.addText(timestamp);
         } else {
             timestampEL.addText(StringConstants.EMPTY);
         }
     }
     /**
      * @param resultNode
      *      TestResultNode
      * @param insertInto
      *      Element
      */
     protected void addGeneralElements(TestResultNode resultNode, 
         Element insertInto) {
         Element name = insertInto.addElement("name"); //$NON-NLS-1$
         
         final INodePO resNode = resultNode.getNode();
         name.addText(resNode.getName());
         if (resNode.getComment() != null) {
             Element comment = insertInto.addElement("comment"); //$NON-NLS-1$
             comment.addText(resNode.getComment());
         }
         Element status = insertInto.addElement("status"); //$NON-NLS-1$
         status.addText(String.valueOf(resultNode.getStatus()));
         
         long durationMillis = 
             resultNode.getDuration(m_testResult.getEndTime());
         if (durationMillis != -1) {
             insertInto.addAttribute("duration", //$NON-NLS-1$
                     DurationFormatUtils.formatDurationHMS(durationMillis));
         }
     }
     /**
      * 
      * @return The <code>Document</code> associated with this report generator.
      */
     protected Document getDocument() {
         return m_document;
     }
     
     /**
      * Hook method called when generating basic XML data.
      * 
      * @return A user-readable <code>String</code> representing the style of 
      *         this report generator.
      */
     protected abstract String getStyleName();
 
 }
