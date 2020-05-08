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
 package org.eclipse.jubula.client.core.utils;
 
import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Locale;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import org.eclipse.jubula.client.core.businessprocess.ExternalTestDataBP;
 import org.eclipse.jubula.client.core.businessprocess.TestCaseParamBP;
 import org.eclipse.jubula.client.core.businessprocess.TestDataBP;
 import org.eclipse.jubula.client.core.i18n.Messages;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IParamDescriptionPO;
 import org.eclipse.jubula.client.core.model.IParamNodePO;
 import org.eclipse.jubula.client.core.model.IParameterInterfacePO;
 import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
 import org.eclipse.jubula.client.core.model.ITDManager;
 import org.eclipse.jubula.client.core.model.ITestDataCubePO;
 import org.eclipse.jubula.client.core.model.ITestDataPO;
 import org.eclipse.jubula.client.core.model.ITestSuitePO;
 import org.eclipse.jubula.client.core.utils.ParamValueConverter.ConvValidationState;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.Assert;
 import org.eclipse.jubula.tools.exception.InvalidDataException;
 import org.eclipse.jubula.tools.exception.JBException;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 
 
 /**
  * class to convert and validate a reference token
  * the conversion is carried out between gui and model representation and vice versa
  * the validation refers to semantical correctness of the gui representation of reference string
  * @author BREDEX GmbH
  * @created 14.08.2007
  */
 public class RefToken extends AbstractParamValueToken {
     
     /** prefix for a reference */
     private static final String PREFIX = "={"; //$NON-NLS-1$
     
     /**
      * <code>m_guiString</code> string represents the token in the GUI
      */
     private String m_guiString = null;
     
     /**
      * <code>m_modelString</code>string represents the token in the model<br>
      * e.g. <b>"=REF"</b> or <b>"={REF}"</b>  --> GUI representation <br>
      *      <b>"=GUID"</b> or <b>={GUID}</b> --> model representation
      */
     private String m_modelString = null; 
 
     /** flag for differentiation between token creation based on string in gui- or
      * model representation
      */
     private boolean m_isTokenGuiBased;
 
     /** node holding this reference */
     private IParameterInterfacePO m_currentNode;
     
     /**
      * use this constructor only for references coming from gui
      * @param string represents the token
      * @param isGuiString flag for differentiation between gui- and model representation of string
      * @param startPos index of first character of token in entire string
      * @param node holding this reference
      * @param desc param description belonging to this reference
      */
     public RefToken(String string, boolean isGuiString, int startPos, 
             IParameterInterfacePO node, IParamDescriptionPO desc) {
         
         super(string, startPos, desc);
         if (!isValid(string, isGuiString)) {
             throw new IllegalArgumentException(Messages.SyntaxErrorInReference
                 + StringConstants.SPACE
                 + new StringBuilder(string).toString()); 
         }
         m_isTokenGuiBased = isGuiString;
         if (isGuiString) {
             m_guiString = string;
         } else {
             m_modelString = string;
         }
         m_currentNode = node;
     }
     
     
     /**
      * creates the model represenation from the gui representation
      * @return modelString
      */
     public String getModelString() {
         if (m_modelString == null && m_guiString != null) {
             String guid = computeGuid();
             if (guid != null) {
                 m_modelString = replaceCore(computeGuid(), m_guiString);
             }
         }
         return m_modelString;
     }
 
     /**
      * hint: returned guid is null if reference is new and the associated parameter 
      * in parent node is not yet created
      * @return GUID belonging to this reference
      */
     private String computeGuid() {
         String guid = StringConstants.EMPTY;
         if (m_modelString != null) {
             guid = extractCore(m_modelString);
         } else if (m_guiString != null) {
             if (m_currentNode instanceof INodePO) {
                 INodePO parent = ((INodePO)m_currentNode).getParentNode();
                 String refName = extractCore(m_guiString);
                 if (parent instanceof IParamNodePO) {
                     IParamNodePO parentNode = (IParamNodePO)parent;
                     IParamDescriptionPO desc = 
                         parentNode.getParameterForName(refName);
                     if (desc != null) {
                         guid = desc.getUniqueId();
                     } else {
                         return null;
                     }
                 } else {
                     StringBuilder msg = new StringBuilder();
                     msg.append(Messages.Node);
                     msg.append(StringConstants.SPACE);
                     msg.append(m_currentNode.getName());
                     msg.append(StringConstants.SPACE);
                     msg.append(Messages.WithReferenceIsNotChildOfParamNode);
                     Assert.notReached(msg.toString());
                 }
             }
         }
         return guid;
     }
 
 
     /**
      * @param repl replacement (guid or reference name)
      * @param str base string
      * @return replaced string
      */
     public static String replaceCore(String repl, String str) {
         int start = -1;
         int end = -1;
         StringBuilder builder = new StringBuilder(str);
         if (str.startsWith(PREFIX)) {
             start = 2;
             end = str.length() - 1;
         } else {
             start = 1;
             end = str.length();
         }
         if (start < end) {
             builder.replace(start, end, repl);
             return builder.toString();
         }
         Assert.notReached(Messages.UnexpectedProblemWithStringReplacement);
         return str;
     }
 
     /**
      * @param s string for syntax validation
      * @param isGuiString flag to distinct gui- and modelStrings
      * @return if the syntax of guiString is correct
      */
     private boolean isValid(String s, boolean isGuiString) {
         if (!isGuiString) {
             String string = extractCore(s);
             final String wordRegex = "[0-9a-fA-F]{32}"; //$NON-NLS-1$
             return (Pattern.matches(wordRegex, string));
         }
         return true;
     }
     
     /**
      * @param s string in gui- or model representation
      * @return reference name respectively guid-portion of entire string
      */
     public static String extractCore(String s) {
         StringBuilder builder = new StringBuilder(s);
         if (s != null && s.length() != 0) {
             if (s.startsWith("={") && s.endsWith("}")) { //$NON-NLS-1$ //$NON-NLS-2$
                 builder.delete(0, 2);
                 builder.deleteCharAt(builder.length() - 1);
             } else if (s.startsWith("=")) { //$NON-NLS-1$
                 builder.deleteCharAt(0);
             }
         }
         return builder.toString();
 
     }
 
 
 
     /**
      * validates, if the reference name and the associated type is allowed and
      * the interface may be modified
      * {@inheritDoc}
      * @see IParamValueToken#validate(INodePO)
      */
     public ConvValidationState validate() {
         ConvValidationState state = ConvValidationState.notSet;
         if (m_currentNode instanceof ISpecTestCasePO) {
             setErrorKey(MessageIDs.E_NO_REF_FOR_SPEC_TC);
             return ConvValidationState.invalid;
         } else if (m_currentNode instanceof INodePO 
                 && ((INodePO)m_currentNode).getParentNode() 
                     instanceof ITestSuitePO) {
             setErrorKey(MessageIDs.E_REF_IN_TS);
             return ConvValidationState.invalid;
         } else if (m_currentNode instanceof ITestDataCubePO) {
             setErrorKey(MessageIDs.E_REF_IN_TDC);
             return ConvValidationState.invalid;
         }
         final boolean isModifiable = TestCaseParamBP.isReferenceValueAllowed(
                 m_currentNode);
         if (m_isTokenGuiBased) {
             INodePO parent = m_currentNode.getSpecificationUser();
             String refName = extractCore(m_guiString);
             if (parent instanceof ISpecTestCasePO) {
                 ISpecTestCasePO specTc = (ISpecTestCasePO)parent;
                 List<IParamDescriptionPO> descs = specTc.getParameterList();
                 Map<String, IParamDescriptionPO> paramNames = 
                     new HashMap<String, IParamDescriptionPO>();
                 for (IParamDescriptionPO desc : descs) {
                     paramNames.put(desc.getName(), desc);
                 }
                 if ((paramNames.keySet()).contains(refName)) {
                     IParamDescriptionPO desc = paramNames.get(refName);
                     if (desc.getType().equals(
                             getParamDescription().getType())) {
                         state = ConvValidationState.valid;
                     } else {
                         state = ConvValidationState.invalid;
                         setErrorKey(MessageIDs.E_INVALID_REF_TYPE);
                     }
                 } else {
                     if (isModifiable) {
                         state = ConvValidationState.valid;
                     } else {
                         state = ConvValidationState.invalid;
                         setErrorKey(MessageIDs.E_INVALID_REF);
                         for (String paramName : paramNames.keySet()) {
                             if (paramName.startsWith(refName)) {
                                 IParamDescriptionPO desc = 
                                     paramNames.get(paramName);
                                 if (desc.getType().equals(
                                         getParamDescription().getType())) {
                                     state = ConvValidationState.undecided;
                                     break;
                                 }
                             }
                         }
                     }
                 }
 
             } else {
                 throw new UnsupportedOperationException(
                     Messages.NotAllowedToAddReferenceToNodeASpecTestCase);
             }
         } else {
             // assumption, that semantic of modelString is correct
             state = ConvValidationState.valid;
         } 
         return state;
     }
 
     /**
      * gets the real value for a reference
      * @param stack current execution stack
      * @param locale currently used locale for testexecution
      * @return the real value for this reference token and given dataset number
      * @throws InvalidDataException if given reference is not resolvable
      */
     public String getExecutionString(List<ExecObject> stack, Locale locale) 
         throws InvalidDataException {
         String refGuid = extractCore(getModelString());
         IParamNodePO execNode = null;
         ListIterator <ExecObject> it = stack.listIterator(stack.size());
         final String refName = extractCore(getGuiString());
         while (it.hasPrevious()) {
             ExecObject obj = it.previous();
             if (obj.getExecNode() instanceof IParamNodePO) {
                 execNode = (IParamNodePO)obj.getExecNode();
                 int dsNumber = obj.getNumberDs();
                 for (IParamDescriptionPO parDesc 
                         : execNode.getParameterList()) {
                     if (parDesc.getUniqueId().equals(refGuid)) {
                         ITDManager man = null;
                         try {
                             // FIXME zeb instantiating a new BP object every 
                             //           time means that we do absolutely *NO* 
                             //           caching of retrieved test data. figure
                             //           out a way to allow caching in this 
                             //           situation.
                             man = new ExternalTestDataBP()
                                     .getExternalCheckedTDManager(execNode);
                         } catch (JBException e) {
                             throwInvalidDataException(refName);
                         }
 
                         // further reference?
                         ITestDataPO data = null;
 
                         // FIXME Andreas : this is only a hack for a TC with multiple
                         // parameters with a mix of refs and fixed values!!!
                         if (dsNumber < man.getDataSetCount()) {
                             data = TestDataBP.instance().getTestData(
                                     execNode, man, parDesc, dsNumber);
                         } else {
                             // FIXME Andreas : this is the data for the fixed value
                             data = TestDataBP.instance().getTestData(
                                     execNode, man, parDesc, 0);
                         }
                         ParamValueConverter conv = new ModelParamValueConverter(
                             data.getValue(locale), 
                             execNode, locale, getParamDescription());

                        // Look for the reference value further up (or down, 
                        // actually) the execution stack. We initialize a new 
                        // list here in order to avoid modifying the provided 
                        // list, which was the cause of bug 375632.
                        List<ExecObject> reducedStack = 
                                new ArrayList<ExecObject>(stack);
                        reducedStack.remove(reducedStack.size() - 1);
                        return conv.getExecutionString(reducedStack, locale); 
                     }
                 }
             }
         }
         throwInvalidDataException(refName);
         return null;
     }
     
     /**
      * throws an exception, if neither a value or a further reference is 
      * available for given reference
      * @param reference reference, which isn't resolvable
      * @throws InvalidDataException in case of missing testdate for given
      * reference
      */
     private void throwInvalidDataException(String reference) 
         throws InvalidDataException {
         throw new InvalidDataException(Messages.Reference + reference 
             + StringConstants.SPACE + Messages.NotResolvable, 
             MessageIDs.E_NO_REFERENCE);    
     }
     
     /**
      * {@inheritDoc}
      * @see org.eclipse.jubula.client.core.utils.IParamValueToken#getValue()
      */
     public String getGuiString() {
         if (m_modelString != null && m_guiString == null) {
             m_guiString = replaceCore(computeReferenceName(), m_modelString);
         }
         return m_guiString;
     }
 
 
     /**
      * compute reference name based on gui- or model string
      * @return reference name
      */
     private String computeReferenceName() {
         String refName = StringConstants.EMPTY;
         if (m_guiString != null) {
             refName = extractCore(m_guiString);
         } else if (m_modelString != null) {
             String guid = extractCore(m_modelString);
             INodePO parent = m_currentNode.getSpecificationUser();
             if (parent instanceof IParamNodePO) {
                 IParamNodePO parentNode = (IParamNodePO)parent;
                 IParamDescriptionPO desc = 
                     parentNode.getParameterForUniqueId(guid);
                 if (desc != null) {
                     refName = desc.getName();
                 } else {
                     String id = (guid != null) 
                         ? guid : StringConstants.EMPTY;
                     Assert.notReached(Messages.InvalidGuid 
                         + StringConstants.SPACE + id + StringConstants.SPACE
                         + Messages.InReferenceNoAppropriateParameter);
                 }
             } else {
                 Assert.notReached(
                     Messages.NodeWithReferenceIsNotChildOfParamNode);
             }
         }
         return refName;
     }
         
     /**
      * @param modelString The modelString to set.
      */
     void setModelString(String modelString) {
         m_modelString = modelString;
     }
     
     
 }
