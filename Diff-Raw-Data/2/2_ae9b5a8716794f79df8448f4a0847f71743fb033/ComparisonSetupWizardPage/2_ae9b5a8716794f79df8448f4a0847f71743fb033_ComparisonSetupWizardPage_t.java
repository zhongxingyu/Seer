 /**
  * <copyright>
  * 
  * Copyright (c) 2010-2012 Thales Global Services S.A.S.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Thales Global Services S.A.S. - initial API and implementation
  * 
  * </copyright>
  */
 package org.eclipse.emf.diffmerge.ui.actions;
 
 import static org.eclipse.emf.diffmerge.ui.actions.ComparisonSetup.PROPERTY_COMPARISON_METHOD;
 
 import org.eclipse.emf.diffmerge.api.Role;
 import org.eclipse.emf.diffmerge.ui.EMFDiffMergeUIPlugin;
 import org.eclipse.emf.diffmerge.ui.EMFDiffMergeUIPlugin.ImageID;
 import org.eclipse.emf.diffmerge.ui.Messages;
 import org.eclipse.emf.diffmerge.ui.specification.IComparisonSpecification;
 import org.eclipse.emf.diffmerge.ui.specification.IComparisonSpecificationFactory;
 import org.eclipse.emf.diffmerge.ui.specification.IScopeSpecification;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.ComboViewer;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 
 /**
  * The wizard page for setting up a comparison.
  * @author Olivier Constant
  */
 public class ComparisonSetupWizardPage extends WizardPage {
   
   /** The non-null data object for the wizard */
   final protected ComparisonSetup _setup;
   
   
   /**
    * Constructor
    * @param name_p the non-null name of the page
    * @param setup_p a non-null data object for the wizard
    */
   public ComparisonSetupWizardPage(String name_p, ComparisonSetup setup_p) {
     super(name_p);
     _setup = setup_p;
   }
   
   /**
    * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
    */
   public void createControl(Composite parent_p) {
     setTitle(Messages.ComparisonSetupWizardPage_Title);
     setDescription(Messages.ComparisonSetupWizardPage_Description);
     // Main composite
     Composite composite = new Composite(parent_p, SWT.NONE);
     setControl(composite);
     composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
     composite.setLayout(new GridLayout(1, false));
     // Sections
     createRolesSection(composite);
     createComparisonMethodSection(composite);
     // Init
     _setup.swapScopeSpecifications(Role.TARGET, Role.TARGET);
     Point size = composite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
     ((GridData)parent_p.getLayoutData()).heightHint = size.y + 5;
   }
   
   /**
    * Create the section for selecting the comparison method
    * @param parent_p a non-null composite
    */
   protected void createComparisonMethodSection(Composite parent_p) {
     // Group
     Group group = new Group(parent_p, SWT.NONE);
     group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
     group.setLayout(new GridLayout(1, false));
     group.setText(Messages.ComparisonSetupWizardPage_GroupMethod);
     // Widgets and viewers
     ComboViewer methodViewer = createComparisonMethodViewer(group);
     createConfigureMethodButton(group);
     // Init
     methodViewer.setInput(_setup);
     Object first = methodViewer.getElementAt(0);
     IStructuredSelection viewerSelection;
     if (first != null)
       viewerSelection = new StructuredSelection(first);
     else
       viewerSelection = new StructuredSelection();
     methodViewer.setSelection(viewerSelection);
//    methodViewer.getControl().setEnabled(_setup.getCompatibleFactories().size() > 1);
   }
   
   /**
    * Create and return a button for configuring the comparison method
    * @param parent_p a non-null composite
    * @return a non-null button
    */
   protected Button createConfigureMethodButton(Composite parent_p) {
     final Button result = new Button(parent_p, SWT.PUSH);
     result.setText(Messages.ComparisonSetupWizardPage_ConfigureButton);
     result.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
     result.addSelectionListener(new SelectionAdapter() {
       /**
        * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
        */
       @Override
       public void widgetSelected(SelectionEvent e_p) {
         IComparisonSpecification specification = _setup.getComparisonSpecification();
         if (specification != null)
           specification.configure();
       }
     });
     _setup.addPropertyChangeListener(new IPropertyChangeListener() {
       /**
        * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
        */
       public void propertyChange(PropertyChangeEvent event_p) {
         if (PROPERTY_COMPARISON_METHOD.equals(event_p.getProperty())) {
           IComparisonSpecification specification = _setup.getComparisonSpecification();
           result.setEnabled(specification != null && specification.isConfigurable());
         }
       }
     });
     return result;
   }
   
   /**
    * Create and return a viewer for selecting the comparison method
    * @param parent_p a non-null composite
    * @return a non-null viewer
    */
   protected ComboViewer createComparisonMethodViewer(Composite parent_p) {
     final ComboViewer result = new ComboViewer(parent_p);
     result.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
     // Behavior
     result.setContentProvider(new IStructuredContentProvider() {
       /**
        * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
        */
       public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
         // Nothing to do
       }
       /**
        * @see org.eclipse.jface.viewers.IContentProvider#dispose()
        */
       public void dispose() {
         // Nothing to do
       }
       /**
        * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
        */
       public Object[] getElements(Object inputElement_p) {
         Object[] localResult = new Object[0];
         if (inputElement_p instanceof ComparisonSetup) {
           ComparisonSetup selection =
             (ComparisonSetup)inputElement_p;
           localResult = selection.getCompatibleFactories().toArray();
         }
         return localResult;
       }
     });
     result.setLabelProvider(new LabelProvider() {
       /**
        * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
        */
       @Override
       public String getText(Object element_p) {
         String localResult;
         if (element_p instanceof IComparisonSpecificationFactory) {
           IComparisonSpecificationFactory factory = (IComparisonSpecificationFactory)element_p;
           localResult = factory.getLabel();
         } else {
           localResult = super.getText(element_p);
         }
         return localResult;
       }
     });
     result.setSorter(new ViewerSorter());
     result.addSelectionChangedListener(new ISelectionChangedListener() {
       /**
        * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
        */
       public void selectionChanged(SelectionChangedEvent event_p) {
         ISelection selection = event_p.getSelection();
         if (selection instanceof IStructuredSelection) {
           Object selected = ((IStructuredSelection)selection).getFirstElement();
           if (selected instanceof IComparisonSpecificationFactory)
             _setup.setSelectedFactory((IComparisonSpecificationFactory)selected);
         }
       }
     });
     return result;
   }
   
   /**
    * Create the section for selecting the roles
    * @param parent_p a non-null composite
    */
   protected void createRolesSection(Composite parent_p) {
     // Group
     Group group = new Group(parent_p, SWT.NONE);
     group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
     group.setLayout(new GridLayout(2, false));
     group.setText(Messages.ComparisonSetupWizardPage_GroupRoles);
     // Subsections
     createCheckRolesSubsection(group);
     createSwapRolesSubsection(group);
   }
   
   /**
    * Create the subsection for checking the roles
    * @param parent_p a non-null composite
    */
   protected void createCheckRolesSubsection(Composite parent_p) {
     // Composite
     Composite subsection = new Composite(parent_p, SWT.NONE);
     subsection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
     GridLayout rolesLayout = new GridLayout(2, false);
     rolesLayout.marginHeight = 0;
     rolesLayout.marginWidth = 0;
     subsection.setLayout(rolesLayout);
     // Left
     new Label(subsection, SWT.NONE).setText(Messages.ComparisonSetupWizardPage_RoleLeft);
     final Text leftText = new Text(subsection, SWT.READ_ONLY | SWT.BORDER);
     leftText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
     _setup.addPropertyChangeListener(new IPropertyChangeListener() {
       /**
        * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
        */
       public void propertyChange(PropertyChangeEvent event_p) {
         if (ComparisonSetup.PROPERTY_ROLES.equals(event_p.getProperty())) {
           IScopeSpecification scope = _setup.getScopeSpecification(Role.TARGET);
           leftText.setText(scope.getLabel());
         }
       }
     });
     // Right
     new Label(subsection, SWT.NONE).setText(Messages.ComparisonSetupWizardPage_RoleRight);
     final Text rightText = new Text(subsection, SWT.READ_ONLY | SWT.BORDER);
     rightText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
     _setup.addPropertyChangeListener(new IPropertyChangeListener() {
       /**
        * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
        */
       public void propertyChange(PropertyChangeEvent event_p) {
         if (ComparisonSetup.PROPERTY_ROLES.equals(event_p.getProperty())) {
           IScopeSpecification scope = _setup.getScopeSpecification(Role.REFERENCE);
           rightText.setText(scope.getLabel());
         }
       }
     });
     if (_setup.isThreeWay()) {
       // Ancestor
       new Label(subsection, SWT.NONE).setText(
           Messages.ComparisonSetupWizardPage_RoleAncestor);
       final Text ancestorText = new Text(subsection, SWT.READ_ONLY | SWT.BORDER);
       ancestorText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
       _setup.addPropertyChangeListener(new IPropertyChangeListener() {
         /**
          * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
          */
         public void propertyChange(PropertyChangeEvent event_p) {
           if (ComparisonSetup.PROPERTY_ROLES.equals(event_p.getProperty())) {
             IScopeSpecification scope = _setup.getScopeSpecification(Role.ANCESTOR);
             ancestorText.setText(scope.getLabel());
           }
         }
       });
     }
   }
   
   /**
    * Create the subsection for swapping roles
    * @param parent_p a non-null composite
    */
   protected void createSwapRolesSubsection(Composite parent_p) {
     // Composite
     Composite buttonSubsection = new Composite(parent_p, SWT.NONE);
     buttonSubsection.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));
     RowLayout buttonLayout = new RowLayout(SWT.VERTICAL);
     buttonLayout.justify = true;
     buttonSubsection.setLayout(buttonLayout);
     // Left/right swap button
     final Button leftRightSwap = new Button(buttonSubsection, SWT.PUSH);
     leftRightSwap.setImage(EMFDiffMergeUIPlugin.getDefault().getImage(ImageID.SWAP));
     leftRightSwap.setToolTipText(Messages.ComparisonSetupWizardPage_SwapLeftRight);
     leftRightSwap.addSelectionListener(new SelectionAdapter() {
       /**
        * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
        */
       @Override
       public void widgetSelected(SelectionEvent event_p) {
         _setup.swapScopeSpecifications(Role.TARGET, Role.REFERENCE);
       }
     });
     if (_setup.isThreeWay()) {
       // Right/ancestor swap button
       final Button rightAnSwap = new Button(buttonSubsection, SWT.PUSH);
       rightAnSwap.setImage(EMFDiffMergeUIPlugin.getDefault().getImage(ImageID.SWAP));
       rightAnSwap.setToolTipText(Messages.ComparisonSetupWizardPage_SwapRightAncestor);
       rightAnSwap.addSelectionListener(new SelectionAdapter() {
         /**
          * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
          */
         @Override
         public void widgetSelected(SelectionEvent event_p) {
           _setup.swapScopeSpecifications(Role.REFERENCE, Role.ANCESTOR);
         }
       });
     }
   }
   
 }
