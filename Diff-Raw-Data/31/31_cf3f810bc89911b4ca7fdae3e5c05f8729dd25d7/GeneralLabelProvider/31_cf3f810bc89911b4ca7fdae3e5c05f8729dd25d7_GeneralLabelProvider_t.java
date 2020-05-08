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
 package org.eclipse.jubula.client.ui.rcp.provider.labelprovider;
 
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.commons.lang.ObjectUtils;
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.IColorProvider;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jubula.client.core.businessprocess.ComponentNamesBP;
 import org.eclipse.jubula.client.core.businessprocess.IComponentNameMapper;
 import org.eclipse.jubula.client.core.businessprocess.db.NodeBP;
 import org.eclipse.jubula.client.core.businessprocess.problems.ProblemFactory;
 import org.eclipse.jubula.client.core.model.ICapPO;
 import org.eclipse.jubula.client.core.model.ICategoryPO;
 import org.eclipse.jubula.client.core.model.IEventExecTestCasePO;
 import org.eclipse.jubula.client.core.model.IExecObjContPO;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IParamDescriptionPO;
 import org.eclipse.jubula.client.core.model.IParamNodePO;
 import org.eclipse.jubula.client.core.model.IRefTestSuitePO;
 import org.eclipse.jubula.client.core.model.IReusedProjectPO;
 import org.eclipse.jubula.client.core.model.ISpecObjContPO;
 import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
 import org.eclipse.jubula.client.core.model.ITestJobPO;
 import org.eclipse.jubula.client.core.model.ITestSuitePO;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.utils.StringHelper;
 import org.eclipse.jubula.client.ui.constants.Constants;
 import org.eclipse.jubula.client.ui.constants.IconConstants;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
 import org.eclipse.jubula.client.ui.rcp.businessprocess.WorkingLanguageBP;
 import org.eclipse.jubula.client.ui.rcp.constants.RcpIconConstants;
 import org.eclipse.jubula.client.ui.rcp.controllers.dnd.LocalSelectionClipboardTransfer;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.jubula.client.ui.utils.LayoutUtil;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.Clipboard;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.PlatformUI;
 
 
 
 /**
  * Base class for all label probider
  *
  * @author BREDEX GmbH
  * @created May 11, 2010
  */
 public class GeneralLabelProvider extends ColumnLabelProvider 
     implements IColorProvider, ILabelProvider {
     /** close bracked */
     public static final String CLOSE_BRACKED = "]"; //$NON-NLS-1$
 
     /** open bracked */
     public static final String OPEN_BRACKED = " ["; //$NON-NLS-1$
     
     /** <code>SEPARATOR</code> */
     public static final String SEPARATOR = "; "; //$NON-NLS-1$
     
     /**
      * <code>UNNAMED_NODE</code>
      */
     private static final String UNNAMED_NODE = 
             Messages.GeneralGDLabelProviderUnnamedNode;
     /**
      * <code>COMMENT_PREFIX</code>
      */
     private static final String COMMENT_PREFIX = 
         Messages.AbstractGuiNodePropertySourceComment + StringConstants.COLON
         + StringConstants.SPACE;
     
     /** The color for disabled elements */
     private static final Color DISABLED_COLOR = LayoutUtil.GRAY_COLOR;
     /** The color for reusedProjects */
     private static final Color REUSED_PROJECTS_COLOR = Display.getDefault()
             .getSystemColor(SWT.COLOR_BLUE);
     
     /** clipboard */
     private static Clipboard clipboard = new Clipboard(PlatformUI
             .getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay());
 
     /** {@inheritDoc} */
     public void dispose() {
         // empty
     }
 
     /** {@inheritDoc} */
     public String getText(Object element) {
         return getGDText(element);
     }
 
     /** {@inheritDoc} */
     public Image getImage(Object element) {
         Image image = getImageImpl(element);
         
         // generated elements
         if (element instanceof INodePO
                 && ((INodePO)element).isGenerated()) {
             image = IconConstants.getGeneratedImage(image);
         }
         
         // elements that have been "cut" to the clipboard should be grayscale
         Object cbContents = clipboard.getContents(
                 LocalSelectionClipboardTransfer.getInstance());
         if (cbContents instanceof IStructuredSelection) {
             IStructuredSelection sel = (IStructuredSelection)cbContents;
             if (sel.toList().contains(element)) {
                 image = RcpIconConstants.getCutImage(image);
             }
         }
         
         return image;
     }
     
     /**
      * {@inheritDoc}
      */
     public String getToolTipText(Object element) {
         if (element instanceof INodePO) {
             INodePO node = (INodePO)element;
             StringBuilder toolTip = new StringBuilder();
             String comment = node.getComment();
             if (!StringUtils.isBlank(comment)) {
                 toolTip.append(COMMENT_PREFIX);
                 toolTip.append(ObjectUtils.toString(comment));
                 return toolTip.toString();
             }
         }
         return super.getToolTipText(element);
     }
     
 
     /** {@inheritDoc} */
     public Point getToolTipShift(Object object) {
         return new Point(5, 5);
     }
 
     /** {@inheritDoc} */
     public int getToolTipDisplayDelayTime(Object object) {
         return 50;
     }
 
     /** {@inheritDoc} */
     public int getToolTipTimeDisplayed(Object object) {
         return 5000;
     }
     
     /** {@inheritDoc} */
     public Image getToolTipImage(Object object) {
         if (object instanceof INodePO) {
             INodePO node = (INodePO)object;
             if (ProblemFactory.hasProblem(node)) {
                 switch (ProblemFactory.getWorstProblem(node.getProblems())
                         .getStatus().getSeverity()) {
                     case IStatus.INFO:
                         return IconConstants.INFO_IMAGE;
                     case IStatus.WARNING:
                         return IconConstants.WARNING_IMAGE;
                     case IStatus.ERROR:
                         return IconConstants.ERROR_IMAGE;
                     default:
                         break;
                 }
             }
         }
         return super.getToolTipImage(object);
     }
     
     /**
      * @param element the element to get the text for 
      * @return a descriptive text for the given element
      */
     public static String getGDText (Object element) {
         if (element instanceof INodePO) {
             INodePO node = (INodePO)element;
             if (node.getName() == null) {
                 return UNNAMED_NODE;
             }
             if (node instanceof ICapPO) {
                 return getText((ICapPO)node);
             } 
             
             if (node instanceof IExecTestCasePO) {
                 return getText((IExecTestCasePO)node);
             }
 
             if (node instanceof ISpecTestCasePO) {
                 return getText((ISpecTestCasePO)node);
             }
             
            if (node instanceof IRefTestSuitePO) {
                return getText((IRefTestSuitePO)node);
            }
            
             return node.getName();
         }
 
         if (element instanceof IReusedProjectPO) {
             IReusedProjectPO reusedProject = (IReusedProjectPO)element;
             String projectName = reusedProject.getProjectName();
             if (projectName == null) {
                 projectName = reusedProject.getProjectGuid();
             }
             return projectName + reusedProject.getVersionString();
         }
         if (element instanceof ISpecObjContPO) {
             return Messages.TreeBuilderTestCases;
         } else if (element instanceof IExecObjContPO) { 
             return GeneralStorage.getInstance().getProject().getName();
         }
         return element == null ? StringConstants.EMPTY : element.toString();
     }
     
     /**
      * @param element the element to get the image for 
      * @return an image for the given element
      */
     public static Image getImageImpl(Object element) {
         if (element instanceof ITestSuitePO) {
             ITestSuitePO testSuite = (ITestSuitePO)element;
             Locale workLang = WorkingLanguageBP.getInstance()
                 .getWorkingLanguage();
             if (testSuite.getAut() != null 
                 && !WorkingLanguageBP.getInstance().isTestSuiteLanguage(
                     workLang, testSuite)) {
                 return IconConstants.TS_DISABLED_IMAGE;
             }
             return IconConstants.TS_IMAGE;
         }
         
         if (element instanceof ICapPO) {
             return IconConstants.CAP_IMAGE;
         }
         
         if (element instanceof IExecObjContPO) {
             return IconConstants.PROJECT_IMAGE;
         }
 
         if (element instanceof IEventExecTestCasePO) {
             return IconConstants.EH_IMAGE;
         }
         
         if (element instanceof IExecTestCasePO) {
             return IconConstants.TC_REF_IMAGE;
         }
 
         if (element instanceof ISpecTestCasePO) {
             return IconConstants.TC_IMAGE;
         }
 
         if (element instanceof ITestJobPO) {
             return IconConstants.TJ_IMAGE;
         }
 
         if (element instanceof ICategoryPO
                 || element instanceof IReusedProjectPO) {
             return IconConstants.CATEGORY_IMAGE;
         }
 
         if (element instanceof IRefTestSuitePO) {
             return IconConstants.TS_REF_IMAGE;
         }
         
         return null;
     }
 
     /**
      * {@inheritDoc}
      */
     public Color getBackground(Object element) {
         return null;
     }
 
     /**
      * {@inheritDoc}
      */
     public Color getForeground(Object element) {
        if (element instanceof IExecTestCasePO || element instanceof ICapPO
                || element instanceof IRefTestSuitePO) { 
             return DISABLED_COLOR;
         }
 
         if (element instanceof ISpecTestCasePO) {
             return null;
         }
         
         if (element instanceof IReusedProjectPO
                 || (element instanceof INodePO 
                 && !NodeBP.isEditable((INodePO)element))) {
             return REUSED_PROJECTS_COLOR;
         }
 
         return null;
     }
    
     /**
      * 
      * @param testStep The Test Step to examine.
      * @return label text for the given Test Step.
      */
     private static String getText(ICapPO testStep) {
         if (Plugin.getDefault().getPreferenceStore().getBoolean(
                 Constants.SHOWCAPINFO_KEY)) {
             StringBuilder nameBuilder = 
                 new StringBuilder(testStep.getName());
             nameBuilder.append(GeneralLabelProvider.OPEN_BRACKED);
             final Map<String, String> map = 
                 StringHelper.getInstance().getMap();
             IComponentNameMapper compMapper = 
                 Plugin.getActiveCompMapper();
             nameBuilder.append(Messages.CapGUIType)
                 .append(map.get(testStep.getComponentType()))
                 .append(GeneralLabelProvider.SEPARATOR)
                 .append(Messages.CapGUIName);
             String componentName = testStep.getComponentName();
             if (compMapper != null) {
                 componentName = 
                     compMapper.getCompNameCache().getName(componentName);
             } else {
                 componentName = 
                     ComponentNamesBP.getInstance().getName(componentName);
             }
             if (componentName != null) {
                 nameBuilder.append(componentName);
             }
             nameBuilder.append(GeneralLabelProvider.SEPARATOR)
                 .append(Messages.CapGUIAction)
                 .append(map.get(testStep.getActionName()))
                 .append(GeneralLabelProvider.CLOSE_BRACKED);
             return nameBuilder.toString();
         } 
         
         return testStep.getName();
     }
 
     /**
      * 
      * @param testCaseRef The Test Case Reference to examine.
      * @return label text for the given Test Case Reference.
      */
     private static String getText(IExecTestCasePO testCaseRef) {
         StringBuilder nameBuilder = new StringBuilder();
         
         if (!StringUtils.isBlank(testCaseRef.getRealName())) {
             nameBuilder.append(testCaseRef.getRealName());
             if (Plugin.getDefault().getPreferenceStore().getBoolean(
                     Constants.SHOWORIGINALNAME_KEY)) {
                 ISpecTestCasePO testCase = testCaseRef.getSpecTestCase();
                 String testCaseName = testCase != null 
                         ? testCase.getName() 
                         : StringUtils.EMPTY;
                 nameBuilder.append(StringConstants.SPACE)
                     .append(StringConstants.LEFT_PARENTHESES)
                     .append(testCaseName)
                     .append(StringConstants.RIGHT_PARENTHESES);
             }
             
         } else {
             ISpecTestCasePO testCase = testCaseRef.getSpecTestCase();
             String testCaseName = testCase != null 
                     ? testCase.getName() 
                     : StringUtils.EMPTY;
             nameBuilder.append(StringConstants.LEFT_INEQUALITY_SING)
                 .append(testCaseName)
                 .append(StringConstants.RIGHT_INEQUALITY_SING); 
         }
 
         nameBuilder.append(getParameterString(testCaseRef));
         
         return nameBuilder.toString();
     }
 
     /**
      * 
      * @param testCase The Test Case to examine.
      * @return label text for the given Test Case.
      */
     private static String getText(ISpecTestCasePO testCase) {
         return testCase.getName() + getParameterString(testCase);
     }
     
     /**
      * 
      * @param paramNode The Parameter Node to examine.
      * @return a label representing all Parameters used by the given node.
      */
     private static String getParameterString(IParamNodePO paramNode) {
         StringBuilder nameBuilder = new StringBuilder();
         Iterator<IParamDescriptionPO> iter = 
             paramNode.getParameterList().iterator();
         boolean parameterExist = false;
         if (iter.hasNext()) {
             parameterExist = true;
             nameBuilder.append(GeneralLabelProvider.OPEN_BRACKED);
         }
         if (iter.hasNext()) {
             while (iter.hasNext()) {
                 IParamDescriptionPO descr = iter.next();
                 nameBuilder.append(descr.getName());
                 if (iter.hasNext()) {
                     nameBuilder.append(GeneralLabelProvider.SEPARATOR);
                 }
             }
         }
         if (parameterExist) {
             nameBuilder.append(GeneralLabelProvider.CLOSE_BRACKED);
         }
         
         return nameBuilder.toString();
     }
     
    /** 
     * 
     * @param testSuitRef The Reference Test Suit to examine.
     * @return label text for the given Reference Test Suit.
     */
    private static String getText(IRefTestSuitePO testSuitRef) {
        StringBuilder nameBuilder = new StringBuilder();
        
        String testSuitRefName = testSuitRef != null 
                ? testSuitRef.getName() 
                : StringUtils.EMPTY;
        nameBuilder.append(StringConstants.LEFT_INEQUALITY_SING)
            .append(testSuitRefName)
            .append(StringConstants.RIGHT_INEQUALITY_SING); 
    
        return nameBuilder.toString();
    }

    
 }
