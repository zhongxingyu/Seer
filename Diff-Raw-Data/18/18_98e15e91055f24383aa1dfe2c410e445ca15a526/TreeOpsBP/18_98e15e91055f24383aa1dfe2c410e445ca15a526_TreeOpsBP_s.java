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
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.persistence.EntityManager;
 
 import org.eclipse.jubula.client.core.i18n.Messages;
 import org.eclipse.jubula.client.core.model.ICompNamesPairPO;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IParamDescriptionPO;
 import org.eclipse.jubula.client.core.model.IParamNodePO;
 import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
 import org.eclipse.jubula.client.core.model.ITestDataPO;
 import org.eclipse.jubula.client.core.model.NodeMaker;
 import org.eclipse.jubula.client.core.model.PoMaker;
 import org.eclipse.jubula.client.core.model.TDCell;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.NodePM;
 import org.eclipse.jubula.client.core.persistence.NodePM.AbstractCmdHandleChild;
 import org.eclipse.jubula.client.core.utils.ModelParamValueConverter;
 import org.eclipse.jubula.client.core.utils.RefToken;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.constants.TestDataConstants;
 import org.eclipse.jubula.tools.exception.JBException;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 
 
 /**
  * A utility class to support tree operations
  * 
  * @author BREDEX GmbH
  * @created 09.09.2005
  */
 public class TreeOpsBP {
 
     /**
      * Exception for failed tree operations
      * 
      * @author BREDEX GmbH
      * @created 09.09.2005
      */
     public static class TreeOpFailedException extends JBException {
 
         /**
          * @param message message
          * @param id id
          */
         public TreeOpFailedException(String message, Integer id) {
             super(message, id);
         }
 
         /**
          * @param message message
          * @param cause cause
          * @param id id
          */
         public TreeOpFailedException(String message, 
             Throwable cause, Integer id) {
 
             super(message, cause, id);
         }
 
     }
 
     /**
      * Hidden default constructor
      */
     private TreeOpsBP() {
         super();
     }
 
     /**
      * Extracts a given List of nodes from a node to a new TestCase and
      * inserts the new created/extracted TestCase into the owner node as an ExecTestCase.
      * @param newTcName The name of the new SpecTestCase
      * @param ownerNode the edited node from which to extract
      * @param modNodes the node to be extracted
      * @param s the database session
      * @param mapper mapper to resolve param names
      * @return an ExecTestCasePO, the location of use of the extracted TestCase
      * @throws TreeOpFailedException if the operation failed
      */
     public static IExecTestCasePO extractTestCase(String newTcName,
         INodePO ownerNode, List<IParamNodePO> modNodes, EntityManager s, 
         ParamNameBPDecorator mapper) throws TreeOpFailedException {
 
         final boolean isOwnerSpecTestCase = 
             ownerNode instanceof ISpecTestCasePO;
         ISpecTestCasePO newTc = NodeMaker.createSpecTestCasePO(newTcName);
         s.persist(newTc); // to get an id for newTc
        IProjectPO project = GeneralStorage.getInstance().getProject();
        AbstractCmdHandleChild handler = NodePM.getCmdHandleChild(project, 
             newTc);
        handler.add(project, newTc, null);
         int pos = -1;
         Map<String, String> oldToNewParamGuids = new HashMap<String, String>();
         for (IParamNodePO selectecNode : modNodes) {
             INodePO moveNode = findNode(ownerNode, selectecNode);
             if (moveNode == null) {
                 throw new TreeOpFailedException(Messages.NodeMismatch,
                     MessageIDs.E_PO_NOT_FOUND);
             }
             if (isOwnerSpecTestCase) {
                 addParamsToParent(newTc, selectecNode, mapper, 
                     (ISpecTestCasePO)ownerNode, oldToNewParamGuids);
             }
             pos = ownerNode.indexOf(moveNode);
             AbstractCmdHandleChild childHandler = NodePM.getCmdHandleChild(
                 ownerNode, moveNode);
             childHandler.remove(ownerNode, moveNode);
             childHandler.add(newTc, moveNode, null);
         }
         IExecTestCasePO newExec = NodeMaker.createExecTestCasePO(newTc);
         if (isOwnerSpecTestCase) {
             propagateParams(newExec, (IParamNodePO)ownerNode);
         }
         propagateCompNames(modNodes, newExec);
         ownerNode.addNode(pos, newExec);
         return newExec;
     }
     
     /**
      * Adds all parameter references of any language of <code>child</code> to the
      * <code>parent</code> by adding new parameter descriptions to the
      * <code>parent</code>. As the result of this, the parent
      * will contain all references of any language of the child.
      * 
      * @param parent
      *            The parent node
      * @param child
      *            The child node
      * @param mapper mapper to resolve param names
      * @param ownerNode the edited node from which to extract
      * @param oldToNewGuids mapping between old and new paramter GUIDs
      */
     private static void addParamsToParent(
             ISpecTestCasePO parent, IParamNodePO child, 
             IParamNameMapper mapper, ISpecTestCasePO ownerNode, 
             Map<String, String> oldToNewGuids) {
         
         IProjectPO proj = GeneralStorage.getInstance().getProject();
         TDCell cell = null;
         List<Locale> langs = proj.getLangHelper().getLanguageList();
         for (Locale lang : langs) {
             for (Iterator<TDCell> it = child.getParamReferencesIterator(lang); 
                     it.hasNext();) {
                 cell = it.next();
                 String guid = child.getDataManager().getUniqueIds().get(
                     cell.getCol());
                 IParamDescriptionPO childDesc = 
                     child.getParameterForUniqueId(guid);
                 // The childDesc can be null if the parameter has been removed
                 // in another session and not yet updated in the current
                 // editor session.
                 if (childDesc != null) {
                     ModelParamValueConverter conv = 
                         new ModelParamValueConverter(
                             cell.getTestData(), child, lang, childDesc);
                     List<RefToken> refTokens = conv.getRefTokens();
                     for (RefToken refToken : refTokens) {
                         IParamDescriptionPO parentParamDescr = 
                             parent.addParameter(
                                     childDesc.getType(), 
                                     RefToken.extractCore(
                                             refToken.getGuiString()), 
                                     false, mapper);
                         // get old GUID from owner node
                         List<IParamDescriptionPO> ownerDescs = 
                             ownerNode.getParameterList();
                         String oldGuid = StringConstants.EMPTY;
                         for (IParamDescriptionPO ownerDesc : ownerDescs) {
                             if (ownerDesc.getName().equals(
                                     RefToken.extractCore(
                                             refToken.getGuiString()))) {
                                 oldGuid = ownerDesc.getUniqueId();
                                 break;
                             }
                         }
                         if (parentParamDescr != null) {
                             String newGuid = parentParamDescr.getUniqueId();
                             oldToNewGuids.put(oldGuid, newGuid);
                         }
                         
                     }
                     // update TestDataPO of child with GUID for reference
                     conv.replaceGuidsInReferences(oldToNewGuids);
                     cell.getTestData().setValue(
                             lang, conv.getModelString(), proj);
                 }
             }
         }
     }
     
     
     
     
     /**
      * @param modNodes the extracted nodes.
      * @param newExec the new ExecTestCasePO.
      */
     private static void propagateCompNames(List<IParamNodePO> modNodes, 
         IExecTestCasePO newExec) {
         
         for (IParamNodePO modNode : modNodes) {
             if (modNode instanceof IExecTestCasePO) {
                 final IExecTestCasePO execTc = (IExecTestCasePO)modNode;
                 for (ICompNamesPairPO pair : execTc.getCompNamesPairs()) {
                     if (pair.isPropagated()) {
                         // use secondName twice here! The new newExec only 
                         // delegates the second name.
                         final String secondName = pair.getSecondName();
                         final ICompNamesPairPO newPairPO = PoMaker
                             .createCompNamesPairPO(secondName, secondName,
                                 pair.getType());
                         newPairPO.setPropagated(true);
                         newExec.addCompNamesPair(newPairPO);
                     }
                 }
             }
         }
     }
 
     /**
      * 
      * Propagates the parameters of the given IExecTestCasePO.
      * @param execTc the IExecTestCasePO
      * @param ownerNode the edited node from which to extract
      */
     private static void propagateParams(IExecTestCasePO execTc, 
         IParamNodePO ownerNode) {
         
         execTc.resolveTDReference();
         final IProjectPO proj = GeneralStorage.getInstance().getProject();
         final List<Locale> languageList = proj.getLangHelper()
             .getLanguageList();
         final List<IParamDescriptionPO> parameterList = execTc
             .getParameterList();
         final List<IParamDescriptionPO> ownerParamList = ownerNode
             .getParameterList();
         for (IParamDescriptionPO descr : parameterList) {
             StringBuilder builder = new StringBuilder();
             final String paramName = descr.getName();
             for (IParamDescriptionPO ownerDesc : ownerParamList) {
                 if (ownerDesc.getName().equals(paramName)) {
                     builder.append(ownerDesc.getUniqueId());
                     break;
                 }
             }            
             ITestDataPO data = PoMaker.createTestDataPO();
             String value = 
                 TestDataConstants.REFERENCE_CHAR_DEFAULT + builder.toString();
             for (Locale locale : languageList) {
                 data.setValue(locale, value, proj);
                 execTc.getDataManager().updateCell(
                     data, 0, descr.getUniqueId());
             }
         }
         
     }
     
 
     /**
      * Moves the given node from the given old parent to the given new parent.
      * 
      * @param moveNode
      *            the node to move
      * @param oldParent
      *            the old parent
      * @param newParent
      *            the new parent
      * @param pos
      *            the position to insert
      */
     public static void moveNode(INodePO moveNode, INodePO oldParent,
         INodePO newParent, int pos) {
 
         AbstractCmdHandleChild childHandler = NodePM.getCmdHandleChild(
             oldParent, moveNode);
         childHandler.remove(oldParent, moveNode);
 
         childHandler = NodePM.getCmdHandleChild(newParent, moveNode);
         childHandler.add(newParent, moveNode, pos);
     }
 
     
 
     /**
      * Checks if the given selected node exsists in the given owner node
      * (comparing with equals()).
      * 
      * @param ownerNode
      *            the owner node to search in.
      * @param selectecNode
      *            the node to check
      * @return the selected node if the given SpecTestCase contains it, null
      *         otherwise.
      */
     private static INodePO findNode(INodePO ownerNode,
         INodePO selectecNode) {
         Iterator childIt = ownerNode.getNodeListIterator();
         while (childIt.hasNext()) {
             INodePO child = (INodePO)childIt.next();
             if (child.getId().equals(selectecNode.getId())) {
                 return child;
             }
         }
         return null;
     }
 
 }
