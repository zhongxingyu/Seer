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
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
 import org.eclipse.jface.databinding.viewers.ViewersObservables;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ComboViewer;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.wizard.WizardPage;
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
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.i18n.CompSystemI18n;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 /**
  * 
  * @author BREDEX GmbH
  *
  */
 public class ComponentNameMappingWizardPage extends WizardPage {
     
     /** Map of component GUID to comboViewer with data */
     private Map<String, ComboViewer> m_componentNamesMapping;
     /** the scrolled composite which is used as parent */
     private ScrolledComposite m_scroll;
     /** the composite with all data*/
     private Composite m_composite;
     /** the new spec Test Case */
     private ISpecTestCasePO m_newSpec;
     /**
      * 
      */
     private LinkedList<ICompNamesPairPO> m_oldCompNamePairs =
             new LinkedList<ICompNamesPairPO>();
     /** */
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
      * {@inheritDoc}
      */
     public void createControl(Composite parent) {
         m_scroll = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
         
         m_scroll.setLayout(GridLayoutFactory.fillDefaults()
                 .numColumns(1).create());
         m_scroll.setMinSize(parent.getSize());
         m_composite = new Composite(m_scroll,
                 SWT.NONE);
         setControl(m_scroll);
     }
 
     /**
      * 
      * @param newSpec
      *            new Spec Test case which should be used for replacement
      */
     public void setNewSpec(ISpecTestCasePO newSpec) {
         m_newSpec = newSpec;
         
         m_composite.dispose(); // Disposing old and generating new composite
         m_componentNamesMapping = new HashMap<String, ComboViewer>();
         Composite mappingGrid = new Composite(m_scroll, SWT.NONE);
         mappingGrid.setLayout(GridLayoutFactory.fillDefaults()
                 .numColumns(3).spacing(10, 10).create());
         GridData tableGridData = GridDataFactory.fillDefaults()
                 .grab(false, false).align(SWT.CENTER, SWT.CENTER).create();
         mappingGrid.setLayoutData(tableGridData);
         
         createLayoutWithData(mappingGrid);
         
         m_scroll.setContent(mappingGrid);
         m_composite = mappingGrid;
     }
 
     /**
      * 
      * @param parent
      *            the parent in which the data should be rendered
      */
     private void createLayoutWithData(Composite parent) {
         
         new Label(parent, SWT.NONE).setText(
                 Messages.ReplaceTCRWizard_ComponentNameMapping_newTC);
         new Label(parent, SWT.SEPARATOR | SWT.VERTICAL);
         new Label(parent, SWT.NONE).setText(
                 Messages.ReplaceTCRWizard_ComponentNameMapping_oldTC);
 
         Label seperatorHorizontal = new Label(parent, SWT.HORIZONTAL
                 | SWT.SEPARATOR);
         
         GridData seperatorGridHorizontal = new GridData(GridData.FILL,
                 GridData.CENTER, true, false);
         seperatorGridHorizontal.horizontalSpan = 3;
         seperatorHorizontal.setLayoutData(seperatorGridHorizontal);
         
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
         
         GridData seperatorVertical = new GridData(
                 GridData.CENTER, GridData.FILL, false, true);
         seperatorVertical.verticalSpan = compNamePairs.size();
         
         boolean first = true;
         for (Iterator compIterator = compNamePairs.iterator(); compIterator
                 .hasNext();) {
             ICompNamesPairPO compNamesPair = (ICompNamesPairPO) compIterator
                     .next();
             
             GridData leftGridData = new GridData();
             leftGridData.horizontalAlignment = SWT.LEFT;
             leftGridData.verticalAlignment = SWT.BEGINNING;
             GridData rightGridData = new GridData();
             rightGridData.horizontalAlignment = SWT.LEFT;
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
             if (first) {
                 Label seperator = new Label(parent, SWT.VERTICAL
                         | SWT.SEPARATOR);
                 seperator.setLayoutData(seperatorVertical);
                 first = false;
             }
             
             Combo oldCompNames = new Combo(parent, SWT.READ_ONLY);
             oldCompNames.setLayoutData(rightGridData); 
             
             fillComboWithOldNames(newComponentName, oldCompNames);
             oldCompNames.pack();
             
         }
     }
 
     /**
      * Fills the combo box with the old component names of the right type
      * 
      * @param componentName
      *            the new Component Names
      * @param oldCompNames
      *            the Combo Box to fill
      */
     private void fillComboWithOldNames(IComponentNamePO componentName,
             Combo oldCompNames) {
         ComboViewer comboViewer = new ComboViewer(oldCompNames);
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
         
         m_componentNamesMapping.put(componentName.getGuid(), comboViewer);
         int counter = 1;
         List<String> listOfMatchingCompNames = new LinkedList<String>();
         // this is for the empty line
         listOfMatchingCompNames.add(StringConstants.SPACE);
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
                     oldCompNames.select(counter);
                 }
                 counter++;
             }
         }
         comboViewer.setInput(listOfMatchingCompNames.toArray());
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
      * Show help contend attached to wizard after selecting the ? icon,
      * or pressing F1 on Windows / Shift+F1 on Linux / Help on MAC.
      * {@inheritDoc}
      */
     public void performHelp() {
         Plugin.getHelpSystem().displayHelp(ContextHelpIds
                .SEARCH_REFACTOR_REPLACE_EXECUTION_TEST_CASE_WIZARD);
     }
 
 }
