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
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.compare.CompareConfiguration;
 import org.eclipse.compare.CompareEditorInput;
 import org.eclipse.compare.ICompareContainer;
 import org.eclipse.compare.ITypedElement;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.SubMonitor;
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.util.WrappedException;
 import org.eclipse.emf.diffmerge.api.Role;
 import org.eclipse.emf.diffmerge.api.scopes.IFeaturedModelScope;
 import org.eclipse.emf.diffmerge.api.scopes.IPhysicalModelScope;
 import org.eclipse.emf.diffmerge.diffdata.EComparison;
 import org.eclipse.emf.diffmerge.diffdata.impl.EComparisonImpl;
 import org.eclipse.emf.diffmerge.ui.EMFDiffMergeUIPlugin;
 import org.eclipse.emf.diffmerge.ui.Messages;
 import org.eclipse.emf.diffmerge.ui.diffuidata.UIComparison;
 import org.eclipse.emf.diffmerge.ui.diffuidata.impl.UIComparisonImpl;
 import org.eclipse.emf.diffmerge.ui.diffuidata.util.UidiffdataResourceFactoryImpl;
 import org.eclipse.emf.diffmerge.ui.specification.IComparisonSpecification;
 import org.eclipse.emf.diffmerge.ui.specification.IScopeSpecification;
 import org.eclipse.emf.diffmerge.ui.util.DiffMergeLabelProvider;
 import org.eclipse.emf.diffmerge.ui.util.MiscUtil;
 import org.eclipse.emf.diffmerge.ui.viewers.ComparisonViewer;
 import org.eclipse.emf.diffmerge.ui.viewers.ModelComparisonDiffNode;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
 import org.eclipse.emf.ecore.xmi.PackageNotFoundException;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.domain.IEditingDomainProvider;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.emf.transaction.util.TransactionUtil;
 import org.eclipse.emf.workspace.ResourceUndoContext;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.swt.custom.BusyIndicator;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchSite;
 import org.eclipse.ui.PlatformUI;
 
 
 /**
  * A CompareEditorInput dedicated to model Diff/Merge.
  * @see CompareEditorInput
  * @author Olivier Constant
  */
 public class EMFDiffMergeEditorInput extends CompareEditorInput
 implements IEditingDomainProvider {
   
   /** The non-null specification of the comparison **/
   protected IComparisonSpecification _specification;
   
   /** The initially null resource that holds the comparison */
   protected Resource _comparisonResource;
   
   /** The non-null, potentially empty set of resources initially present */
   protected final Collection<Resource> _initialResources;
   
   /** The comparison scopes (initially null iff URIs are not null) **/
   protected IFeaturedModelScope _leftScope, _rightScope, _ancestorScope;
   
   /** The initially null viewer */
   protected ComparisonViewer _viewer;
   
   /** Whether the comparison originally contained differences (initially true) */
   private boolean _foundDifferences;
   
   /** Whether the editor is dirty (required for compatibility with Indigo) */ //OCO
   private boolean _isDirty;
   
   
   /**
    * Constructor
    * @param specification_p a non-null specification of the comparison
    */
   public EMFDiffMergeEditorInput(IComparisonSpecification specification_p) {
     super(new CompareConfiguration());
     _specification = specification_p;
     _leftScope = null;
     _rightScope = null;
     _ancestorScope = null;
     _comparisonResource = null;
     _initialResources = new ArrayList<Resource>();
     _foundDifferences = true;
     _isDirty = false;
     initializeCompareConfiguration();
   }
   
   /**
    * @see org.eclipse.compare.CompareEditorInput#canRunAsJob()
    */
   @Override
   public boolean canRunAsJob() {
     return true;
   }
   
   /**
    * @see org.eclipse.compare.CompareEditorInput#contentsCreated()
    */
   @Override
   protected void contentsCreated() {
     _viewer.getControl().addDisposeListener(new DisposeListener() {
       /**
        * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
        */
       public void widgetDisposed(DisposeEvent ev) {
         handleDispose();
       }
     });
   }
   
   /**
    * @see org.eclipse.compare.CompareEditorInput#contributeToToolBar(org.eclipse.jface.action.ToolBarManager)
    */
   @Override
   public void contributeToToolBar(ToolBarManager toolBarManager) {
     // Nothing
   }
   
   /**
    * @see org.eclipse.compare.CompareEditorInput#createContents(org.eclipse.swt.widgets.Composite)
    */
   @Override
   public Control createContents(Composite parent_p) {
     // Create viewer
     _viewer = new ComparisonViewer(parent_p, getActionBars());
     _viewer.addPropertyChangeListener(new IPropertyChangeListener() {
       /**
        * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
        */
       public void propertyChange(PropertyChangeEvent event_p) {
         String propertyName = event_p.getProperty();
         if (CompareEditorInput.DIRTY_STATE.equals(propertyName)) {
           boolean dirty = ((Boolean)event_p.getNewValue()).booleanValue();
           setDirty(dirty);
         }
       }
     });
     // Register viewer as selection provider of the site
     IWorkbenchSite site = getSite();
     if (site != null)
       site.setSelectionProvider(_viewer);
     // Create viewer contents
     _viewer.setInput(getCompareResult());
     contentsCreated();
     return _viewer.getControl();
   }
   
   /**
    * Dispose the resources which have been added during the comparison process
    */
   protected void disposeResources() {
     final EditingDomain domain = getEditingDomain();
     final Set<Resource> addedResources = new HashSet<Resource>(
         domain.getResourceSet().getResources());
     addedResources.removeAll(_initialResources);
     MiscUtil.executeAndForget(domain, new Runnable() {
       /**
        * @see java.lang.Runnable#run()
        */
       public void run() {
         if (_comparisonResource != null) {
           for (EObject root : _comparisonResource.getContents()) {
             if (root instanceof UIComparison) {
               UIComparison uiComparison = (UIComparison)root;
               uiComparison.dispose();
             }
           }
         }
         for (Resource resource : addedResources) {
           for (Adapter adapter : new ArrayList<Adapter>(resource.eAdapters())) {
             if (adapter instanceof ECrossReferenceAdapter)
               resource.eAdapters().remove(adapter);
           }
         }
         for (Resource resource : addedResources) {
           resource.unload();
         }
         domain.getResourceSet().getResources().removeAll(addedResources);
       }
     });
     domain.getCommandStack().flush();
     if (domain instanceof TransactionalEditingDomain) {
       for (Resource resource : addedResources) {
         TransactionUtil.disconnectFromEditingDomain(resource);
         // Cleaning up Eclipse operation history
         try {
           ResourceUndoContext context = new ResourceUndoContext(
               (TransactionalEditingDomain)domain, resource);
           PlatformUI.getWorkbench().getOperationSupport().getOperationHistory().dispose(
               context, true, true, false);
         } catch (Exception e) {
           // Workbench being disposed or EMF Workspace dependency not available: proceed
         }
       }
     }
   }
   
   /**
    * @see org.eclipse.compare.CompareEditorInput#flushViewers(org.eclipse.core.runtime.IProgressMonitor)
    */
   @Override
   protected void flushViewers(IProgressMonitor monitor_p) {
     _viewer.flush(monitor_p);
   }
   
   /**
    * Return whether the comparison originally contained differences
    * @return true by default before the comparison has actually been computed
    */
   public boolean foundDifferences() {
     return _foundDifferences;
   }
   
   /**
    * @see org.eclipse.compare.CompareEditorInput#getCompareResult()
    */
   @Override
   public ModelComparisonDiffNode getCompareResult() {
     return (ModelComparisonDiffNode)super.getCompareResult();
   }
   
   /**
    * Return the editing domain in which comparison takes place
    * @return a non-null editing domain
    * @see IEditingDomainProvider#getEditingDomain()
    */
   public EditingDomain getEditingDomain() {
     return _specification.getEditingDomain();
   }
   
   /**
    * Return the contextual workbench site, if any
    * @return a potentially null workbench site
    */
   protected IWorkbenchSite getSite() {
     IWorkbenchSite result = null;
     ICompareContainer container = getCompareConfiguration().getContainer();
     IWorkbenchPart part = container.getWorkbenchPart();
     if (part != null)
       result = part.getSite();
     return result;
   }
   
   /**
    * @see org.eclipse.compare.CompareEditorInput#handleDispose()
    */
   @Override
   protected void handleDispose() {
     IWorkbenchSite site = getSite();
     if (site != null)
       site.setSelectionProvider(null);
     super.handleDispose();
     Runnable disposeBehavior = new Runnable() {
       /**
        * @see java.lang.Runnable#run()
        */
       public void run() {
         if (getCompareResult() != null)
           getCompareResult().dispose();
         disposeResources();
         _specification.dispose();
         _specification = null;
         _ancestorScope = null;
         _leftScope = null;
         _rightScope = null;
         _viewer = null;
         _comparisonResource = null;
         _initialResources.clear();
       }
     };
     Display display = Display.getDefault();
     boolean inUIThread = display.getThread() == Thread.currentThread();
     if (inUIThread)
       BusyIndicator.showWhile(display, disposeBehavior);
     else
       disposeBehavior.run();
     // Clear the input to avoid memory leak
     try {
       super.run(null);
     } catch (Exception e) {
       // Nothing
     }
   }
   
   /**
    * Display appropriate messages according to the problem that happened during the comparison process
    */
   protected void handleExecutionProblem(Throwable problem_p) {
     Throwable diagnostic = problem_p;
     if (diagnostic instanceof WrappedException)
       diagnostic = ((WrappedException)diagnostic).exception();
     String message;
     if (diagnostic instanceof PackageNotFoundException) {
       PackageNotFoundException pnfe = (PackageNotFoundException)diagnostic;
       message = MiscUtil.buildString(
           Messages.EMFDiffMergeEditorInput_WrongMetamodel, "\n", //$NON-NLS-1$
           pnfe.getLocation(), ".\n", //$NON-NLS-1$
           Messages.EMFDiffMergeEditorInput_MigrationNeeded);
     } else {
       String msg = diagnostic.getLocalizedMessage();
       if (msg == null)
         msg = diagnostic.toString();
       message = MiscUtil.buildString(
           Messages.EMFDiffMergeEditorInput_Failure, "\n", msg);  //$NON-NLS-1$
     }
     Shell shell = null;
     if (getWorkbenchPart() != null && getWorkbenchPart().getSite() != null)
       shell = getWorkbenchPart().getSite().getShell();
     if (shell != null) {
       final String finalMessage = message;
       final Shell finalShell = shell;
       shell.getDisplay().syncExec(new Runnable() {
         /**
          * @see java.lang.Runnable#run()
          */
         public void run() {
           MessageDialog.openError(finalShell, EMFDiffMergeUIPlugin.LABEL, finalMessage);
         }
       });
     }
   }
   
   /**
    * Initialize the CompareConfiguration of this CompareEditorInput
    */
   protected void initializeCompareConfiguration() {
     CompareConfiguration cc = getCompareConfiguration();
     cc.setLeftLabel(_specification.getScopeSpecification(Role.TARGET).getLabel());
     cc.setRightLabel(_specification.getScopeSpecification(Role.REFERENCE).getLabel());
     IScopeSpecification ancestorSpecification =
       _specification.getScopeSpecification(Role.ANCESTOR);
     cc.setAncestorLabel((ancestorSpecification == null) ? "" : ancestorSpecification.getLabel()); //$NON-NLS-1$
     cc.setLeftEditable(_specification.getScopeSpecification(Role.TARGET).isEditable());
     cc.setRightEditable(_specification.getScopeSpecification(Role.REFERENCE).isEditable());
   }
   
   /**
    * Create and return the diff node for the given comparison
    * @param comparison_p a non-null comparison
    * @return a non-null diff node
    */
   protected ModelComparisonDiffNode initializeDiffNode(EComparison comparison_p) {
     UIComparison uiComparison = new UIComparisonImpl(comparison_p);
     EditingDomain domain = getEditingDomain();
     domain.getResourceSet().getResourceFactoryRegistry().getExtensionToFactoryMap().
       put(EMFDiffMergeUIPlugin.UI_DIFF_DATA_FILE_EXTENSION, new UidiffdataResourceFactoryImpl());
     _comparisonResource = getEditingDomain().createResource(
         "platform:/resource/comparison/comparison." + //$NON-NLS-1$
         EMFDiffMergeUIPlugin.UI_DIFF_DATA_FILE_EXTENSION);
     ModelComparisonDiffNode result = new ModelComparisonDiffNode(uiComparison, domain);
     result.updateDifferenceNumbers();
     return result;
   }
   
   /**
    * @see org.eclipse.compare.CompareEditorInput#isSaveNeeded()
    */
   @Override
   public boolean isSaveNeeded() {
     // Redefined for compatibility with Indigo
     return _isDirty;
   }
   
   /**
    * Load the model scopes
    * @param monitor_p a non-null monitor for reporting progress
    */
   protected void loadScopes(IProgressMonitor monitor_p) {
     EditingDomain domain = getEditingDomain();
     _initialResources.addAll(domain.getResourceSet().getResources());
     boolean threeWay = _specification.isThreeWay();
     String mainTaskName = Messages.EMFDiffMergeEditorInput_Loading;
     SubMonitor loadingMonitor = SubMonitor.convert(
         monitor_p, mainTaskName, threeWay ? 4 : 3);
     loadingMonitor.worked(1);
     // Loading left
     loadingMonitor.subTask(Messages.EMFDiffMergeEditorInput_LoadingLeft);
     _leftScope = _specification.getScopeSpecification(Role.TARGET).createScope(domain);
     if (_leftScope instanceof IPhysicalModelScope) {
       try {
         ((IPhysicalModelScope)_leftScope).load();
       } catch (Exception e) {
         throw new WrappedException(e);
       }
     }
     loadingMonitor.worked(1);
     if (loadingMonitor.isCanceled())
       throw new OperationCanceledException();
     // Loading right
     loadingMonitor.subTask(Messages.EMFDiffMergeEditorInput_LoadingRight);
     _rightScope = _specification.getScopeSpecification(Role.REFERENCE).createScope(domain);
     if (_rightScope instanceof IPhysicalModelScope) {
       try {
         ((IPhysicalModelScope)_rightScope).load();
       } catch (Exception e) {
         throw new WrappedException(e);
       }
     }
     loadingMonitor.worked(1);
     if (loadingMonitor.isCanceled())
       throw new OperationCanceledException();
     // Loading ancestor
     if (threeWay) {
       loadingMonitor.subTask(Messages.EMFDiffMergeEditorInput_LoadingAncestor);
       _ancestorScope = _specification.getScopeSpecification(Role.ANCESTOR).createScope(domain);
       if (_ancestorScope instanceof IPhysicalModelScope) {
         try {
           ((IPhysicalModelScope)_ancestorScope).load();
         } catch (Exception e) {
           throw new WrappedException(e);
         }
       }
       loadingMonitor.worked(1);
       if (loadingMonitor.isCanceled())
         throw new OperationCanceledException();
     }
   }
   
   /**
    * Return whether merge can be considered complete
    */
   public boolean mergeIsComplete() {
     return !isDirty() && (_viewer.getInput() == null || _viewer.getInput().isEmpty());
   }
   
   /**
    * @see org.eclipse.compare.CompareEditorInput#prepareInput(org.eclipse.core.runtime.IProgressMonitor)
    */
   @Override
   protected Object prepareInput(IProgressMonitor monitor_p) throws
       InvocationTargetException, InterruptedException {
     if (monitor_p == null) // True when called from handleDispose()
       return null;
     boolean scopesReady = _leftScope != null;
     SubMonitor monitor = SubMonitor.convert(monitor_p, EMFDiffMergeUIPlugin.LABEL, 2);
     ModelComparisonDiffNode result = null;
     try {
       if (!scopesReady)
         loadScopes(monitor.newChild(1));
       EComparison comparison = new EComparisonImpl(_leftScope, _rightScope, _ancestorScope);
       comparison.compute(_specification.getMatchPolicy(), _specification.getDiffPolicy(),
           _specification.getMergePolicy(), monitor.newChild(scopesReady? 2: 1));
       _foundDifferences = comparison.hasRemainingDifferences();
       if (_foundDifferences)
         result = initializeDiffNode(comparison);
       else
         handleDispose();
     } catch (OperationCanceledException e) {
       // No user feedback is needed
       handleDispose();
     } catch (Throwable t) {
       // Cannot load models
       handleExecutionProblem(t);
       handleDispose();
     }
     return result;
   }
   
   /**
    * @see org.eclipse.compare.CompareEditorInput#run(org.eclipse.core.runtime.IProgressMonitor)
    */
   @Override
   public void run(IProgressMonitor monitor_p) throws InterruptedException, InvocationTargetException {
     if (getCompareResult() != null)
       getCompareResult().dispose();
     super.run(monitor_p);
     if (getCompareResult() != null) {
       if (foundDifferences()) {
         // This is done here because the compare result must have been assigned:
         // directly referencing the UIComparison would result in a memory leak
         MiscUtil.executeAndForget(getEditingDomain(), new Runnable() {
           /**
            * @see java.lang.Runnable#run()
            */
           public void run() {
             _comparisonResource.getContents().add(getCompareResult().getUIComparison());
           }
         });
         getEditingDomain().getCommandStack().flush();
       }
     }
   }
   
   /**
    * @see org.eclipse.compare.CompareEditorInput#setDirty(boolean)
    */
   @Override
   public void setDirty(boolean dirty_p) {
     // Redefined for compatibility with Indigo
     boolean oldDirty = isDirty();
     if (dirty_p != oldDirty) {
       _isDirty = dirty_p;
       PropertyChangeEvent event = new PropertyChangeEvent(this, DIRTY_STATE, oldDirty, _isDirty);
       firePropertyChange(event);
     }
   }
   
   
   /**
    * A wrapper for model scopes that implements ITypedElement for ICompareInputs
    */
   public static class ScopeTypedElementWrapper implements ITypedElement {
     /** The non-null scope being wrapped */
     private final IFeaturedModelScope _scope;
     /**
      * Constructor
      * @param scope_p a non-null model scope
      */
     public ScopeTypedElementWrapper(IFeaturedModelScope scope_p) {
       _scope = scope_p;
     }
     /**
      * @see org.eclipse.compare.ITypedElement#getImage()
      */
     public Image getImage() {
       Image result = null;
       if (!_scope.getContents().isEmpty()) {
         EObject root = _scope.getContents().get(0);
         if (root.eResource() != null)
           result = DiffMergeLabelProvider.getInstance().getImage(root.eResource());
       }
       return result;
     }
     /**
      * @see org.eclipse.compare.ITypedElement#getName()
      */
     public String getName() {
       String result = null;
       if (!_scope.getContents().isEmpty()) {
         EObject root = _scope.getContents().get(0);
         if (root.eResource() != null)
           result = DiffMergeLabelProvider.getInstance().getText(root.eResource());
       }
       return result;
     }
     /**
      * @see org.eclipse.compare.ITypedElement#getType()
      */
     public String getType() {
       return ITypedElement.UNKNOWN_TYPE;
     }
   }
   
 }
