 /*******************************************************************************
  * Copyright (c) 2013 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.ui.rcp.wizards.search.refactor.pages;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
 import org.eclipse.jface.databinding.viewers.ViewersObservables;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ComboViewer;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jubula.client.core.businessprocess.CompNamesBP;
 import org.eclipse.jubula.client.core.businessprocess.ComponentNamesBP;
 import org.eclipse.jubula.client.core.businessprocess.MasterSessionComponentNameMapper;
 import org.eclipse.jubula.client.core.model.ICompNamesPairPO;
 import org.eclipse.jubula.client.core.model.IComponentNamePO;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.ISpecTestCasePO;
 import org.eclipse.jubula.client.core.model.NodeMaker;
 import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.jubula.client.ui.rcp.provider.ControlDecorator;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.i18n.CompSystemI18n;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 /**
  * 
  * @author BREDEX GmbH
  *
  */
 public class ComponentNameMappingWizardPage extends AbstractMatchSelectionPage {
     
     /** Map of component GUID to comboViewer with data */
     private Map<String, ComboViewer> m_componentNamesMapping;
     /** the new spec Test Case */
     private ISpecTestCasePO m_newSpec;
     /** for Information Text purpose */
     private boolean m_noMatchingType = false;
     /**
      * Old component names pairs
      */
     private LinkedList<ICompNamesPairPO> m_oldCompNamePairs =
             new LinkedList<ICompNamesPairPO>();
     /** CompNamesBP */
     private CompNamesBP m_compNamesBP = new CompNamesBP();
     
     /**
      * 
      * @param pageName
      *            the page name
      * @param execTCList
      *            the selected exec Test Cases which should be replaced
      */
     public ComponentNameMappingWizardPage(String pageName,
             Set<IExecTestCasePO> execTCList) {
         super(pageName, Messages.ReplaceTCRWizard_matchComponentNames_title,
                 null);
         for (Iterator iterator = execTCList.iterator(); iterator.hasNext();) {
             IExecTestCasePO exec = (IExecTestCasePO) iterator.next();
             m_oldCompNamePairs.addAll(m_compNamesBP.getAllCompNamesPairs(exec));
         }
     }
 
     /**
      * 
      * @param newSpec
      *            new Spec Test case which should be used for replacement
      */
     public void setNewSpec(ISpecTestCasePO newSpec) {
         m_newSpec = newSpec;
         m_noMatchingType = false;
         m_componentNamesMapping = new HashMap<String, ComboViewer>();
     }
 
     /**
      * 
      * @param parent
      *            the parent in which the data should be rendered
      */
     private void createLayoutWithData(Composite parent) {
         IExecTestCasePO newExec = NodeMaker.createExecTestCasePO(m_newSpec);
 
         Collection<ICompNamesPairPO> compNamePairs = 
                 m_compNamesBP.getAllCompNamesPairs(newExec);  
         if (compNamePairs.size() == 0) {            
             return;
         }
         createMatchingFields(parent);
         parent.pack();
 
     }
     
     /**
      * Creates the GUI fields with component names
      * @param parent the parent composite
      */
     private void createMatchingFields(Composite parent) {
         IExecTestCasePO newExec = NodeMaker.createExecTestCasePO(m_newSpec);
 
         Collection<ICompNamesPairPO> compNamePairs = 
                 m_compNamesBP.getAllCompNamesPairs(newExec); 
                 
         for (Iterator compIterator = compNamePairs.iterator(); compIterator
                 .hasNext();) {
             ICompNamesPairPO compNamesPair = (ICompNamesPairPO) compIterator
                     .next();
             
             GridData leftGridData = new GridData();
             leftGridData.horizontalAlignment = SWT.LEFT;
             leftGridData.verticalAlignment = SWT.BEGINNING;
             GridData rightGridData = new GridData();
             rightGridData.horizontalAlignment = SWT.FILL;
             rightGridData.verticalAlignment = SWT.BEGINNING;
             rightGridData.minimumWidth = 200;
             rightGridData.grabExcessHorizontalSpace = true;
 
             IComponentNamePO newComponentName = ComponentNamesBP.getInstance()
                     .getCompNamePo(compNamesPair.getFirstName());
 
             String displayName = getDisplayName(newComponentName.getName(),
                     newComponentName.getComponentType());
             Label compname = new Label(parent, NONE);
             compname.setText(displayName); 
             compname.setLayoutData(leftGridData);
                     
             Control comboOrLabel = createControlWithOldComponentNames(
                     newComponentName, parent);
             comboOrLabel.setLayoutData(rightGridData);
             comboOrLabel.pack();
             
         }
     }
 
     /**
      * Creates a Combo if there are old component names, or a text with the message of the problem
      * 
      * @param componentName
      *            the new Component Names
      * @param parent
      *            the parent
      * @return a <code>Combo</code> or a <code>Label</code> 
      */
     private Control createControlWithOldComponentNames(
             IComponentNamePO componentName, Composite parent) {        
         int counter = 1;
         List<String> listOfMatchingCompNames = new LinkedList<String>();
         // this is for the empty line
         listOfMatchingCompNames.add(StringConstants.SPACE);
         int selection = 0;
         for (Iterator iterator = m_oldCompNamePairs.iterator(); 
                 iterator.hasNext();) {
             ICompNamesPairPO oldPairs = (ICompNamesPairPO) iterator.next();
             
             IComponentNamePO oldComponent = ComponentNamesBP.getInstance()
                     .getCompNamePo(oldPairs.getFirstName());
             
             String isCompatible = ComponentNamesBP.getInstance().isCompatible(
                     componentName.getComponentType(), oldComponent.getName(),
                     MasterSessionComponentNameMapper.getInstance(),
                     null, true);   
             if (isCompatible == null 
                     && !listOfMatchingCompNames
                         .contains(oldComponent.getGuid())) {
                 listOfMatchingCompNames.add(oldComponent.getGuid());
                 if (componentName.getName().equals(oldComponent.getName())
                         && componentName.getComponentType().equals(
                                 oldComponent.getComponentType())) {
                     selection = counter;
                 }
                 counter++;
             }
         }
         if (listOfMatchingCompNames.size() > 1) {
             return createCombo(componentName, parent, listOfMatchingCompNames,
                     selection);
         }
         
         Label label = new Label(parent, SWT.NONE);
         label.setText(Messages
                 .ReplaceTCRWizard_matchComponentNames_warningNoSameType);
         ControlDecorator.addWarningDecorator(label,
                 Messages
                 .ReplaceTCRWizard_matchComponentNames_warningNoSameTypeDesc
         );
         m_noMatchingType = true;
         return label;
     }
 
     /**
      * 
      * @param componentName
      *            the component name
      * @param parent
      *            the parent
      * @param listOfMatchingCompNames
      *            the list of matching component names
      * @param selection
      *            the selected index if one should be preselected
      * @return the Combo
      * 
      */
     private Combo createCombo(IComponentNamePO componentName,
             Composite parent, List<String> listOfMatchingCompNames,
             int selection) {
         final Combo oldCompNamesCombo = new Combo(parent, SWT.READ_ONLY);
         ComboViewer comboViewer = new ComboViewer(oldCompNamesCombo);
         comboViewer.setContentProvider(new ArrayContentProvider());
         comboViewer.setLabelProvider(new LabelProvider() {
             @Override
             public String getText(Object element) {
                 if (element instanceof String) {
                     String guid = (String) element;
                     if (!StringUtils.isBlank((String)element)) {
                         IComponentNamePO newComponentName = ComponentNamesBP
                                 .getInstance().getCompNamePo(guid);
                         return getDisplayName(newComponentName.getGuid(),
                                 newComponentName.getComponentType());
                     }
                 }
                 return StringConstants.SPACE;
             } 
         });
         comboViewer.setInput(listOfMatchingCompNames.toArray());
         m_componentNamesMapping.put(componentName.getGuid(), comboViewer);
         final ControlDecorator notMappedDecoration = ControlDecorator
                 .addWarningDecorator(
             oldCompNamesCombo,
             Messages
             .ReplaceTCRWizard_matchComponentNames_warningUnmatchedComp
         );
         oldCompNamesCombo.addSelectionListener(new SelectionListener() {
             public void widgetSelected(SelectionEvent e) {
                 notMappedDecoration.setVisible(oldCompNamesCombo
                         .getSelectionIndex() == 0);
             }
             
             public void widgetDefaultSelected(SelectionEvent e) {
                 // Do nothing
             }
         });
         if (selection != 0) {
             oldCompNamesCombo.select(selection);
             notMappedDecoration.setVisible(false);
         }
         return oldCompNamesCombo;
     }
     
     /**
      * 
      * @param guidName
      *            guid of the component name
      * @param guidType
      *            guid of the type
      * @return a String as <code> ComponentName > SecondComponentName [ComponentType]</code>
      */
     private String getDisplayName(String guidName, String guidType) {
         String firstName = ComponentNamesBP.getInstance().getName(
                 guidName);
         String type = CompSystemI18n.getString(guidType);
         String displayName = firstName;
         if (!StringUtils.isBlank(type)) {
             displayName += StringConstants.SPACE + StringConstants.LEFT_BRACKET
                     + type + StringConstants.RIGHT_BRACKET;
         }
         return displayName;
     }
 
     /**
      * 
      * @return a list of component pairs which are matched together from the
      *         selection
      */
     public Map<String, String>getCompMatching() {
         Map<String, String> mapping = new HashMap<String, String>();
         for (Entry<String, ComboViewer> entry : m_componentNamesMapping
                 .entrySet()) {
             String guidOfCompName = entry.getKey();
             IViewerObservableValue value = ViewersObservables
                     .observeSingleSelection(entry.getValue());
             String guidOfMappedCompName = (String) value.getValue();
             mapping.put(guidOfCompName, guidOfMappedCompName);
         }
         return mapping;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void createSelectionTable(Composite parent) {
         if (m_componentNamesMapping.size() == 0) {
             // remove the previously shown parameter names
             for (Control child : parent.getChildren()) {
                 child.dispose();
             }
             createHeadLabel(parent,
                     Messages.ReplaceTCRWizard_ComponentNameMapping_newTC);
             createHeadLabel(parent,
                     Messages.ReplaceTCRWizard_ComponentNameMapping_oldTC);
             createLayoutWithData(parent);
             updateAdditionalInformation();
         }
     }
 
     /**
      * updates the additional information text
      */
     private void updateAdditionalInformation() {
         IExecTestCasePO newExec = NodeMaker.createExecTestCasePO(m_newSpec);
         Collection<ICompNamesPairPO> compNamePairs = 
                 m_compNamesBP.getAllCompNamesPairs(newExec);
         List<String> messages = new ArrayList<String>();
        Set<String> oldCompNames = new HashSet<String>();
        for (ICompNamesPairPO pair : m_oldCompNamePairs) {
            oldCompNames.add(pair.getFirstName());
        }
        if (compNamePairs.size() == 0 && oldCompNames.size() == 0) {
             messages.add(Messages
                     .ReplaceTCRWizard_matchComponentNames_infoNotNecessary);
         }
        if (compNamePairs.size() < oldCompNames.size()) {
             messages.add(Messages
                     .ReplaceTCRWizard_matchComponentNames_infoOldMore);
         }
         if (m_noMatchingType) {
             messages.add(Messages
                     .ReplaceTCRWizard_matchComponentNames_infoNoType);
         }
         
         setAdditionalInformation(messages);
     }
     
     /**
      * Show help contend attached to wizard after selecting the ? icon,
      * or pressing F1 on Windows / Shift+F1 on Linux / Help on MAC.
      * {@inheritDoc}
      */
     public void performHelp() {
         Plugin.getHelpSystem().displayHelp(ContextHelpIds
                 .SEARCH_REFACTOR_REPLACE_EXECUTION_TEST_CASE_WIZARD);
     }
 }
