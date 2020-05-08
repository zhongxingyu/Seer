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
 package org.eclipse.jubula.client.ui.properties;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jubula.client.core.businessprocess.db.TimestampBP;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.model.IReusedProjectPO;
 import org.eclipse.jubula.client.core.model.PoMaker;
 import org.eclipse.jubula.client.core.persistence.EditSupport;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.Hibernator;
 import org.eclipse.jubula.client.core.persistence.NodePM;
 import org.eclipse.jubula.client.core.persistence.PMException;
 import org.eclipse.jubula.client.core.persistence.ProjectPM;
 import org.eclipse.jubula.client.ui.Plugin;
 import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
 import org.eclipse.jubula.client.ui.constants.IconConstants;
 import org.eclipse.jubula.client.ui.constants.Layout;
 import org.eclipse.jubula.client.ui.i18n.Messages;
 import org.eclipse.jubula.client.ui.properties.ProjectGeneralPropertyPage.IOkListener;
 import org.eclipse.jubula.client.ui.utils.Utils;
 import org.eclipse.jubula.client.ui.widgets.ListElementChooserComposite;
 import org.eclipse.jubula.client.ui.widgets.TreeElementChooserComposite;
 import org.eclipse.jubula.client.ui.widgets.TreeElementChooserComposite.IChooserCompositeGuiObject;
 import org.eclipse.jubula.client.ui.widgets.TreeElementChooserComposite.IUsedListModifiedListener;
 import org.eclipse.jubula.toolkit.common.businessprocess.ToolkitSupportBP;
 import org.eclipse.jubula.toolkit.common.exception.ToolkitPluginException;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.JBException;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.persistence.jpa.JpaEntityManager;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Tree;
 
 
 /**
  * This is the class for the test data property page of a project.
  *
  * @author BREDEX GmbH
  * @created 08.02.2005
  */
 public class ProjectUsedPropertyPage extends AbstractProjectPropertyPage 
     implements IOkListener {
 
     /** number of columns = 1 */
     private static final int NUM_COLUMNS_1 = 1; 
     /** number of columns = 2 */
     private static final int NUM_COLUMNS_2 = 2;
     /** the list field for the available projects */ 
     private Tree m_availableProjectsList;
     /** the list field for the used projects */ 
     private List m_usedProjectsList;
     /** mapping between Strings in the selection lists and reused project info objects */
     private Map<String, IReusedProjectPO> m_projectMap = 
         new HashMap<String, IReusedProjectPO>();
     /** the composite with 2 ListBoxes */
     private UsedProjectsChooserComposite m_chooseLists = null;
 
     /** The currently selected projects (display strings) for reuse */
     private String[] m_listEntries;
 
     /**
      * @param es the editSupport
      */
     public ProjectUsedPropertyPage(EditSupport es) {
         super(es);
     }
 
     /**
      * {@inheritDoc}
      */
     protected Control createContents(Composite parent) {
         Composite composite = createComposite(parent, NUM_COLUMNS_1,
             GridData.FILL, false);
         noDefaultAndApplyButton();
 
         createLabel(composite, 
             Messages.ProjectPropertyPageSelectReusedProjects);
         Composite innerComposite = new Composite(composite, SWT.NONE);
         GridLayout compositeLayout = new GridLayout();
         compositeLayout.marginHeight = 0;
         compositeLayout.marginWidth = 0;
         innerComposite.setLayout(compositeLayout);
         GridData compositeData = new GridData();
         compositeData.horizontalAlignment = SWT.FILL;
         compositeData.grabExcessHorizontalSpace = true;
         innerComposite.setLayoutData(compositeData);
         try {
             createProjectLists(innerComposite);
             getObjects();
             resizeLists();
         } catch (ToolkitPluginException tpe) {
             composite.dispose();
             composite = createComposite(parent, NUM_COLUMNS_1,
                 GridData.FILL, false); 
             
             new Label(composite, SWT.NONE).setText(
                 Messages.CouldNotLoadReusableProjects + StringConstants.COLON
                 + StringConstants.NEWLINE + StringConstants.TAB
                 + tpe.getLocalizedMessage());
         }
         Plugin.getHelpSystem().setHelp(parent,
             ContextHelpIds.PROJECT_USED_PROPERTY_PAGE);
         return composite;
     }
 
     /**
      * @param innerComposite the parent composite
      */
     private void createProjectLists(Composite innerComposite) 
         throws ToolkitPluginException {
         
         Set<IReusedProjectPO> usedProjects = getProject().getUsedProjects();
 
         Set<IChooserCompositeGuiObject> availableGuiObjects = 
             new HashSet<IChooserCompositeGuiObject>();
         Set<IChooserCompositeGuiObject> usedGuiObjects = 
             new HashSet<IChooserCompositeGuiObject>();
         
         try {
             String projectToolkit = getProject().getToolkit();
             java.util.List<IProjectPO> reusableProjects = 
                 ProjectPM.findReusableProjects(
                     getProject().getGuid(), 
                     getProject().getMajorProjectVersion(),
                     getProject().getMinorProjectVersion(),
                     projectToolkit, 
                     ToolkitSupportBP.getToolkitLevel(projectToolkit));
             for (IProjectPO proj : reusableProjects) {
                 IReusedProjectPO reused = 
                     PoMaker.createReusedProjectPO(proj);
                 IChooserCompositeGuiObject reusedGui = 
                     new ReusedProjectGuiObject(reused);
                 availableGuiObjects.add(reusedGui);
                 m_projectMap.put(reusedGui.getDisplayString(), reused);
             }
 
             for (IReusedProjectPO reused : usedProjects) {
                 IChooserCompositeGuiObject guiObj = 
                     new ReusedProjectGuiObject(reused);
                 usedGuiObjects.add(guiObj);
                 m_projectMap.put(guiObj.getDisplayString(), reused);
             }
             
             m_chooseLists = new UsedProjectsChooserComposite(innerComposite, 
                 availableGuiObjects, usedGuiObjects);
 
 
         } catch (JBException gde) {
             // Loading of a project failed
             Utils.createMessageDialog(gde, null, null);
         } catch (ToolkitPluginException tpe) {
             // Toolkit plugin for the current project is not loaded
             Utils.createMessageDialog(MessageIDs.E_READ_PROJECT, 
                 null, new String [] {
                     Messages.CannotFindReusableProjects
                 });
             throw tpe;
         }
     }
   
     /**
      * Resizes the two ListBoxes.
      */
     private void resizeLists() {
         ((GridData)m_usedProjectsList.getLayoutData()).widthHint = Dialog
             .convertHeightInCharsToPixels(Layout
                 .getFontMetrics(m_usedProjectsList), 15);
         ((GridData)m_availableProjectsList.getLayoutData()).widthHint = Dialog
             .convertHeightInCharsToPixels(Layout
                 .getFontMetrics(m_usedProjectsList), 15);
     }
     
     /**
      * Creates a new composite.
      * @param parent The parent composite.
      * @param numColumns the number of columns for this composite.
      * @param alignment The horizontalAlignment (grabExcess).
      * @param horizontalSpace The horizontalSpace.
      * @return The new composite.
      */
     private Composite createComposite(Composite parent, int numColumns, 
             int alignment, boolean horizontalSpace) {
         
         Composite composite = new Composite(parent, SWT.NONE);
         GridLayout compositeLayout = new GridLayout();
         compositeLayout.numColumns = numColumns;
         compositeLayout.marginHeight = 0;
         compositeLayout.marginWidth = 0;
         composite.setLayout(compositeLayout);
         GridData compositeData = new GridData();
         compositeData.horizontalAlignment = alignment;
         compositeData.grabExcessHorizontalSpace = horizontalSpace;
         composite.setLayoutData(compositeData);
         return composite;       
     }
     
     /**
      * Gets the listBoxes / Buttons from the ListElementChooserComposite composite
      */
     private void getObjects() {
         m_availableProjectsList = m_chooseLists.getAvailableTree();
         m_usedProjectsList = m_chooseLists.getUsedList();
     }
     
     /**
      * Creates a label for this page.
      * @param text The label text to set.
      * @param parent The composite.
      * @return a new label
      */
     private Label createLabel(Composite parent, String text) {
         Label label = new Label(parent, SWT.NONE);
         label.setText(text);
         GridData labelGrid = new GridData(GridData.BEGINNING, GridData.CENTER, 
             false , false, 1, 1);
         label.setLayoutData(labelGrid);
         return label;
     }
     
 
     /**
      * Updates the used projects of the model.
      * @param usedProjects The projects that should be used.
      */
     void updateProjects(String [] usedProjects) throws PMException {
 
         // Just clearing the list and re-adding all elements leads to 
         // Hibernate errors, so we must actually only add/remove the 
         // elements that have changed.
         Set<IReusedProjectPO> toAdd = new HashSet<IReusedProjectPO>();
         Set<IReusedProjectPO> toRemove = new HashSet<IReusedProjectPO>();
         for (String displayableProjectId : usedProjects) {
             toAdd.add(m_projectMap.get(displayableProjectId));
         }
 
         // toRemove: All elements that are currently used, but not in the new list
         toRemove.addAll(getEditSupport().getWorkProject().getUsedProjects());
         toRemove.removeAll(toAdd);
         
         // toAdd: All elements that are in the new list, but are currently used
         toAdd.removeAll(getEditSupport().getWorkProject().getUsedProjects());
         for (IReusedProjectPO remove : toRemove) {
             getEditSupport().getWorkProject().removeUsedProject(remove);
         }
 
         boolean isDirty = 
             getEditSupport().getSession().unwrap(JpaEntityManager.class)
                 .getUnitOfWork().hasChanges();
         // Prevents constraint violation from Hibernate
         Hibernator.instance().flushSession(getEditSupport().getSession());
         if (isDirty) {
             // the session will not be commited if there are no pending changes
             TimestampBP.refreshTimestamp(getEditSupport().getWorkProject());
         }
         
         for (IReusedProjectPO reuse : toAdd) {
             getEditSupport().getWorkProject().addUsedProject(reuse);
            reuse.setParentProjectId(getProject().getId());
         }
 
     }
     
     /**
      * GUI representation of a reused project.
      *
      * @author BREDEX GmbH
      * @created Aug 17, 2007
      */
     private class ReusedProjectGuiObject 
             implements IChooserCompositeGuiObject {
 
         /** string for separating major and minor version number */
         private static final String VER_SEP = "."; //$NON-NLS-1$
         
         /** string that is show directly before the version number */
         private static final String BEGIN_VER = " ["; //$NON-NLS-1$
         
         /** string that is show directly after the version number */
         private static final String END_VER = "]"; //$NON-NLS-1$
         
         /** the reused project that this GUI object represents. */
         private IReusedProjectPO m_modelObject;
         
         /**
          * Constructor
          * 
          * @param modelObject the reused project that this GUI object 
          *                    represents.
          */
         public ReusedProjectGuiObject(IReusedProjectPO modelObject) {
             m_modelObject = modelObject;
         }
         
         /**
          * {@inheritDoc}
          */
         public String getDisplayString() {
             String projectName = m_modelObject.getProjectName();
             if (projectName == null) {
                 projectName = m_modelObject.getProjectGuid();
             }
             StringBuffer sb = new StringBuffer(projectName);
             sb.append(BEGIN_VER);
             sb.append(m_modelObject.getMajorNumber());
             sb.append(VER_SEP);
             sb.append(m_modelObject.getMinorNumber());
             sb.append(END_VER);
             return sb.toString();
         }
 
         /**
          * {@inheritDoc}
          */
         public Object getModelObject() {
             return m_modelObject;
         }
 
         /**
          * {@inheritDoc}
          */
         public String getParent() {
             return m_modelObject.getProjectName();
         }
 
         /**
          * {@inheritDoc}
          */
         public String getTitle() {
             StringBuffer sb = new StringBuffer();
             sb.append(m_modelObject.getMajorNumber());
             sb.append(VER_SEP);
             sb.append(m_modelObject.getMinorNumber());
             return sb.toString();
         }
         
         /**
          * 
          * {@inheritDoc}
          */
         public String toString() {
             return getDisplayString();
         }
         
         /**
          * 
          * {@inheritDoc}
          */
         public boolean equals(Object obj) {
             if (this == obj) {
                 return true;
             }
             if (!(obj instanceof ReusedProjectGuiObject)) {
                 return false;
             }
             ReusedProjectGuiObject otherGui = (ReusedProjectGuiObject)obj;
 
             return getDisplayString().equals(otherGui.getDisplayString());
         }
 
         /**
          * {@inheritDoc}
          */
         public int hashCode() {
             return getDisplayString().hashCode();
         }
     }
 
     /**
      * @author BREDEX GmbH
      * @created Dec 20, 2006
      */
     private final class UsedProjectsChooserComposite 
             extends TreeElementChooserComposite 
             implements IUsedListModifiedListener {
 
         /**
          * Constructor
          * @param innerComposite The inner composite
          * @param availableGuiObjects All available objects.
          * @param usedGuiObjects Objects to be placed in the used list.
          */
         private UsedProjectsChooserComposite(Composite innerComposite, 
             Set<IChooserCompositeGuiObject> availableGuiObjects, 
             Set<IChooserCompositeGuiObject> usedGuiObjects) {
 
             super(innerComposite, Messages
                     .ProjectPropertyPageReusableProjectsUpperLabel,
                 availableGuiObjects, Messages
                     .ProjectPropertyPageReusableProjectsBottomLabel,
                 usedGuiObjects, 15, 
                     new Image[] { IconConstants.RIGHT_ARROW_IMAGE, 
                                   IconConstants.DOUBLE_RIGHT_ARROW_IMAGE, 
                                   IconConstants.LEFT_ARROW_IMAGE, 
                                   IconConstants.DOUBLE_LEFT_ARROW_IMAGE,
                                   IconConstants.SWAP_ARROW_IMAGE }, 
                     new Image[] { IconConstants.RIGHT_ARROW_DIS_IMAGE, 
                                   IconConstants.DOUBLE_RIGHT_ARROW_DIS_IMAGE, 
                                   IconConstants.LEFT_ARROW_DIS_IMAGE, 
                                   IconConstants.DOUBLE_LEFT_ARROW_DIS_IMAGE,
                                   IconConstants.SWAP_ARROW_DIS_IMAGE }, 
                     new String[] { 
                         Messages.ProjectPropertyPageReusableProjectsDownToolTip,
                         Messages
                             .ProjectPropertyPageReusableProjectsAllDownToolTip,
                         Messages.ProjectPropertyPageReusableProjectsUpToolTip,
                         Messages
                             .ProjectPropertyPageReusableProjectsAllUpToolTip,
                         Messages.ProjectPropertyPageReusableProjectsSwapToolTip
                     }, ListElementChooserComposite.VERTICAL);
             addListModifiedListener(this);
         }
 
         /**
          * {@inheritDoc}
          */
         protected String checkSelectionUsedToAvailable(String [] selection) {
 
             for (String reusedName : selection) {
                 IReusedProjectPO reused = m_projectMap.get(reusedName);
                 if (reused != null) {
                     java.util.List<IExecTestCasePO> execTcNames = 
                         NodePM.getUsedTestCaseNames(
                             GeneralStorage.getInstance().getProject(), 
                             reused);
                     if (execTcNames != null && !execTcNames.isEmpty()) {
                         return execTcNames.get(0).getName();
                     }
                 }
             }
 
             return null;
         }
 
         /**
          * {@inheritDoc}
          */
         public void usedListModified(String[] newListEntries) {
             m_listEntries = newListEntries;
         }
 
         
     }
 
     /**
      * {@inheritDoc}
      */
     public void okPressed() throws PMException {
         if (m_listEntries != null) {
             updateProjects(m_listEntries);
         }
     }
 }
