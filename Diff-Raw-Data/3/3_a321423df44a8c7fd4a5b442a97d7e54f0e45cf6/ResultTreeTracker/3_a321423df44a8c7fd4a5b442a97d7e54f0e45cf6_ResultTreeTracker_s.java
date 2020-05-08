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
 package org.eclipse.jubula.client.core.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.jubula.client.core.businessprocess.ExternalTestDataBP;
 import org.eclipse.jubula.client.core.businessprocess.TestExecution;
 import org.eclipse.jubula.client.core.i18n.Messages;
 import org.eclipse.jubula.client.core.persistence.Persistor;
 import org.eclipse.jubula.client.core.utils.ExecObject;
 import org.eclipse.jubula.client.core.utils.ModelParamValueConverter;
 import org.eclipse.jubula.client.core.utils.ParamValueConverter;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.InvalidDataException;
 import org.eclipse.jubula.tools.exception.JBException;
 import org.eclipse.jubula.tools.i18n.CompSystemI18n;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 
 
 
 /**
  * class for tracking of result tree for display of test results
  * @author BREDEX GmbH
  * @created 14.04.2005
  */
 public class ResultTreeTracker implements IExecStackModificationListener {
     
     /** the logger */
     private static Logger log = 
         LoggerFactory.getLogger(ResultTreeTracker.class);
 
     
     /**
      * <code>m_endNode</code> last result node in resultTree, which is associated 
      * with last execTestCase or with the testsuite
      */
     private TestResultNode m_endNode;
     
     /**
      * <code>m_lastCap</code> resultNode to actual executed cap
      */
     private TestResultNode m_lastNonCap;
 
     /**
      * here is saved how much extra hierarchy lvl we have caused by event
      * handler
      */
     private int m_eventHierarchy = 0;
 
     /** business process for retrieving test data */
     private ExternalTestDataBP m_externalTestDataBP;
 
     /**
      * @param root traverser for associated testexecution tree
      * @param externalTestDataBP business process for retrieving test data
      */
     public ResultTreeTracker(TestResultNode root, 
             ExternalTestDataBP externalTestDataBP) {
         m_endNode = root;
         m_lastNonCap = root;
         m_externalTestDataBP = externalTestDataBP;
     }
     
     /** 
      * {@inheritDoc}
      */
     public void stackIncremented(INodePO node) {
         if (m_lastNonCap.getStatus() == TestResultNode.NOT_YET_TESTED) {
             m_lastNonCap.setResult(TestResultNode.TESTING, null);
         }
         if (Persistor.isPoSubclass(node, IEventExecTestCasePO.class)
             || m_eventHierarchy > 0) {
             m_eventHierarchy++;
         }
         
         if (m_eventHierarchy > 0) {
             int nextIndex = m_lastNonCap.getNextChildIndex();
             m_lastNonCap = new TestResultNode(
                     node, 
                     m_lastNonCap, 
                     nextIndex);
             m_lastNonCap.getParent().updateResultNode(nextIndex, m_lastNonCap);
         } else {
             TestResultNode nextNonCap = m_lastNonCap.getResultNodeList().
                 get(m_lastNonCap.getNextChildIndex());
             while (nextNonCap.getNode() != node) {
                 nextNonCap = m_lastNonCap.getResultNodeList().
                     get(m_lastNonCap.getNextChildIndex());
             }
             m_lastNonCap = nextNonCap;
         }
         m_endNode = m_lastNonCap;
         if (m_lastNonCap.getStatus() == TestResultNode.NOT_YET_TESTED) {
             m_lastNonCap.setResult(TestResultNode.TESTING, null);
             if (node instanceof IParamNodePO) {
                 addParameters((IParamNodePO)node, m_lastNonCap);
             }
         }
         if (m_lastNonCap.getParent().getStatus() 
                 == TestResultNode.NOT_YET_TESTED) {
             m_lastNonCap.getParent().setResult(TestResultNode.TESTING, null);
         }
 
     }
 
     /** 
      * {@inheritDoc}
      */
     public void stackDecremented() {
         if (m_eventHierarchy > 0) {
             m_eventHierarchy--;
         }
         if (m_lastNonCap.getStatus() == TestResultNode.NOT_YET_TESTED
                 || m_lastNonCap.getStatus() == TestResultNode.TESTING) {
 
             m_lastNonCap.setResult(TestResultNode.SUCCESS, null);
 
             if (m_endNode.getStatus() == TestResultNode.NOT_YET_TESTED
                     || m_endNode.getStatus() == TestResultNode.TESTING) {
             
                 m_endNode.setResult(TestResultNode.SUCCESS, null);
             }
         }
         m_lastNonCap = m_lastNonCap.getParent();
         m_endNode = m_lastNonCap;
     }
 
     /** 
      * {@inheritDoc}
      */
     public void nextDataSetIteration() {
         if (m_lastNonCap.getStatus() == TestResultNode.NOT_YET_TESTED 
                 || m_lastNonCap.getStatus() == TestResultNode.TESTING) {
            
             m_lastNonCap.setResult(TestResultNode.SUCCESS, null);
 
             if (m_endNode.getStatus() == TestResultNode.NOT_YET_TESTED
                     || m_endNode.getStatus() == TestResultNode.TESTING) {
             
                 m_endNode.setResult(TestResultNode.SUCCESS, null);
             }
         }
         int nextIndex = m_lastNonCap.getParent().getNextChildIndex();
         if (m_eventHierarchy > 0) {
             m_lastNonCap = new TestResultNode(m_lastNonCap.getNode(), 
                     m_lastNonCap.getParent(), nextIndex);
             m_lastNonCap.getParent().updateResultNode(nextIndex, m_lastNonCap);
         } else {
             m_lastNonCap = m_lastNonCap.getParent().
                 getResultNodeList().
                     get(nextIndex);
         }
         if (m_lastNonCap.getStatus() == TestResultNode.NOT_YET_TESTED) {
             INodePO node = m_lastNonCap.getNode();
             if (node instanceof IParamNodePO) {
                 addParameters((IParamNodePO)node, m_lastNonCap);
             }
             m_lastNonCap.setResult(TestResultNode.TESTING, null);
         }
         if (m_lastNonCap.getParent().getStatus() 
             == TestResultNode.NOT_YET_TESTED) {
             m_lastNonCap.getParent().setResult(TestResultNode.TESTING, null);
         }
 
     }
 
     /** 
      * {@inheritDoc}
      */
     public void nextCap(ICapPO cap) {
         int nextIndex = m_lastNonCap.getNextChildIndex();
         
         if (m_eventHierarchy > 0) {
             m_endNode = new TestResultNode(cap, m_lastNonCap);
             m_endNode.getParent().updateResultNode(nextIndex, m_endNode);
             m_endNode.setActionName(
                     CompSystemI18n.getString(cap.getActionName()));
             m_endNode.setComponentType(
                     CompSystemI18n.getString(cap.getComponentType()));
         } else {
             m_endNode = m_lastNonCap.getResultNodeList().
                 get(nextIndex);
             while (m_endNode.getNode() != cap) {
                 nextIndex = m_lastNonCap.getNextChildIndex();
                 m_endNode = m_lastNonCap.getResultNodeList().
                     get(nextIndex);
             }
         }
         if (m_endNode.getStatus() == TestResultNode.NOT_YET_TESTED) {
             m_endNode.setResult(TestResultNode.TESTING, null);
         }
         addParameters(cap, m_endNode);
     }
 
     /**
      * Adds the parameters from the given cap to the given result node.
      * 
      * @param paramNode The node from which to copy the parameter info.
      * @param resultNode The result node to which the parameter info will
      *                   be copied.
      */
     private void addParameters(IParamNodePO paramNode, 
             TestResultNode resultNode) {
         
         List<IParamDescriptionPO> parameterList = paramNode.getParameterList();
         for (IParamDescriptionPO desc : parameterList) {
             
             List<ExecObject> execList = 
                     TestExecution.getInstance().getTrav().getExecStackAsList();
             ExecObject currentExecObj = execList.get(execList.size() - 1);
             
             resultNode.addParameter(new TestResultParameter(
                     desc.getName(), CompSystemI18n.getString(desc.getType()), 
                     currentExecObj.getParameterValue(desc.getUniqueId())));
         }
     }
 
     /**
      * Adds the parameters from the given Test Step to the given result node.
      * 
      * @param testStep The Test Step from which to copy the parameter info.
      * @param resultNode The result node to which the parameter info will
      *                   be copied.
      */
     private void addParameters(ICapPO testStep, 
             TestResultNode resultNode) {
         
         List<IParamDescriptionPO> parameterList = testStep.getParameterList();
         String value = null;
         for (IParamDescriptionPO desc : parameterList) {
             ITDManager tdManager = null;
             try {
                 tdManager = 
                     m_externalTestDataBP.getExternalCheckedTDManager(
                             testStep);
             } catch (JBException e) {
                 log.error(
                         Messages.TestDataNotAvailable + StringConstants.DOT, e);
             }
             TestExecution te = TestExecution.getInstance();
 
             List <ExecObject> stackList = 
                 new ArrayList<ExecObject>(te.getTrav().getExecStackAsList());
 
             // Special handling for Test Steps. Their test data manager has 
             // information about multiple Data Sets, but we are only interested 
             // in the first one.
             int dataSetIndex = 0;
 
             if (tdManager.findColumnForParam(desc.getUniqueId()) == -1) {
                 IParameterInterfacePO referencedDataCube = testStep
                         .getReferencedDataCube();
                 if (referencedDataCube != null) {
                     desc = referencedDataCube.getParameterForName(desc
                             .getName());
                 }
             }
             ITestDataPO date = tdManager.getCell(dataSetIndex, desc);
             ParamValueConverter conv = new ModelParamValueConverter(
                 date.getValue(te.getLocale()), testStep, te.getLocale(), desc);
             try {
                 value = conv.getExecutionString(
                         stackList, te.getLocale());
             } catch (InvalidDataException e) {
                 log.error(e.getMessage());
                 value = MessageIDs.getMessageObject(e.getErrorId()).
                     getMessage(new Object[] {});
             }
 
             resultNode.addParameter(new TestResultParameter(
                    desc.getUniqueId(), desc.getType(), 
                     StringUtils.defaultString(value)));
         }
     }
 
     /**
      * @return Returns the endNode.
      */
     public TestResultNode getEndNode() {
         return m_endNode;
     }
 
     /**
      * {@inheritDoc}
      */
     public void retryCap(ICapPO cap) {
         int nextIndex = m_lastNonCap.getNextChildIndex();
         m_endNode = new TestResultNode(cap, m_lastNonCap, nextIndex);
         if (m_endNode.getStatus() == TestResultNode.NOT_YET_TESTED) {
             m_endNode.setResult(TestResultNode.TESTING, null);
         }
         m_endNode.getParent().updateResultNode(nextIndex, m_endNode);
 
         addParameters(cap, m_endNode);
     }
 }
