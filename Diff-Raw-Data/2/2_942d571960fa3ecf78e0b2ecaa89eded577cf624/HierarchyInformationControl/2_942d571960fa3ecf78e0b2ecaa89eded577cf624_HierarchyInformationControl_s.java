 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.ui.typehierarchy;
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.eclipse.dltk.core.ScriptModelUtil;
 import org.eclipse.dltk.core.IMember;
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.ITypeHierarchy;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.internal.core.util.MethodOverrideTester;
 import org.eclipse.dltk.internal.corext.util.Messages;
 import org.eclipse.dltk.internal.ui.text.AbstractInformationControl;
 import org.eclipse.dltk.internal.ui.typehierarchy.SuperTypeHierarchyViewer.SuperTypeHierarchyContentProvider;
 import org.eclipse.dltk.internal.ui.typehierarchy.TraditionalHierarchyViewer.TraditionalHierarchyContentProvider;
 import org.eclipse.dltk.ui.DLTKUIPlugin;
 import org.eclipse.dltk.ui.ProblemsLabelDecorator;
 import org.eclipse.dltk.ui.ScriptElementLabels;
 import org.eclipse.dltk.ui.actions.IScriptEditorActionDefinitionIds;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.viewers.AbstractTreeViewer;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.ui.keys.KeySequence;
 import org.eclipse.ui.keys.SWTKeySupport;
 
 /**
  * Show hierarchy in light-weight control.
  * 
  * @since 3.0
  */
 public abstract class HierarchyInformationControl extends AbstractInformationControl {
 	
 	private TypeHierarchyLifeCycle fLifeCycle;
 	private HierarchyLabelProvider fLabelProvider;
 	private KeyAdapter fKeyAdapter;
 	
 	private Object[] fOtherExpandedElements;
 	private TypeHierarchyContentProvider fOtherContentProvider;
 	
 	private IMethod fFocus; // method to filter for or null if type hierarchy
 	private boolean fDoFilter;
 	
 	private MethodOverrideTester fMethodOverrideTester;
 	private IPreferenceStore fPreferenceStore;
 
 	public HierarchyInformationControl(Shell parent, int shellStyle, int treeStyle) {
 		super(parent, shellStyle, treeStyle, IScriptEditorActionDefinitionIds.OPEN_HIERARCHY, true);
 		fOtherExpandedElements= null;
 		fDoFilter= true;
 		fMethodOverrideTester= null;
 	}
 	
 	private KeyAdapter getKeyAdapter() {
 		if (fKeyAdapter == null) {
 			fKeyAdapter= new KeyAdapter() {
 				public void keyPressed(KeyEvent e) {
 					int accelerator = SWTKeySupport.convertEventToUnmodifiedAccelerator(e);
 					KeySequence keySequence = KeySequence.getInstance(SWTKeySupport.convertAcceleratorToKeyStroke(accelerator));
 					KeySequence[] sequences= getInvokingCommandKeySequences();
 					if (sequences == null)
 						return;
 					
 					for (int i= 0; i < sequences.length; i++) {
 						if (sequences[i].equals(keySequence)) {
 							e.doit= false;
 							toggleHierarchy();
 							return;
 						}
 					}
 				}
 			};			
 		}
 		return fKeyAdapter;		
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	protected boolean hasHeader() {
 		return true;
 	}
 
 	protected Text createFilterText(Composite parent) {
 		// text set later
 		Text text= super.createFilterText(parent);
 		text.addKeyListener(getKeyAdapter());
 		return text;
 	}	
 		
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jdt.internal.ui.text.JavaOutlineInformationControl#createTreeViewer(org.eclipse.swt.widgets.Composite, int)
 	 */
 	protected TreeViewer createTreeViewer(Composite parent, int style) {
 		Tree tree= new Tree(parent, SWT.SINGLE | (style & ~SWT.MULTI));
 		GridData gd= new GridData(GridData.FILL_BOTH);
 		gd.heightHint= tree.getItemHeight() * 12;
 		tree.setLayoutData(gd);
 
 		TreeViewer treeViewer= new TreeViewer(tree);
 		treeViewer.addFilter(new ViewerFilter() {
 			public boolean select(Viewer viewer, Object parentElement, Object element) {
				return element instanceof IType;
 			}
 		});		
 		
 		fLifeCycle= new TypeHierarchyLifeCycle(false);
 
 		treeViewer.setSorter(new HierarchyViewerSorter(fLifeCycle));
 		treeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
 
 		fLabelProvider= new HierarchyLabelProvider(fLifeCycle, getPreferenceStore ());
 		fLabelProvider.setFilter(new ViewerFilter() {
 			public boolean select(Viewer viewer, Object parentElement, Object element) {
 				return hasFocusMethod((IType) element);
 			}
 		});	
 
 		fLabelProvider.setTextFlags(ScriptElementLabels.ALL_DEFAULT | ScriptElementLabels.T_POST_QUALIFIED);
 		fLabelProvider.addLabelDecorator(new ProblemsLabelDecorator(null));
 		treeViewer.setLabelProvider(fLabelProvider);
 		
 		treeViewer.getTree().addKeyListener(getKeyAdapter());	
 		
 		return treeViewer;
 	}
 	
 	protected abstract IPreferenceStore getPreferenceStore();
 
 	protected boolean hasFocusMethod(IType type) {
 		if (fFocus == null) {
 			return true;
 		}
 		if (type.equals(fFocus.getDeclaringType())) {
 			return true;
 		}
 		
 		try {
 			IMethod method= findMethod(fFocus, type);
 			if (method != null) {
 				// check visibility
 //				IProjectFragment pack= (IProjectFragment) fFocus.getAncestor(IModelElement.PROJECT_FRAGMENT);
 //				if (DLTKModelUtil.isVisibleInHierarchy(method, pack)) {
 //					return true;
 //				}
 				return true;
 			}
 		} catch (ModelException e) {
 			// ignore
 			DLTKUIPlugin.log(e);
 		}
 		return false;			
 		
 	}
 	
 	private IMethod findMethod(IMethod filterMethod, IType typeToFindIn) throws ModelException {
 		IType filterType= filterMethod.getDeclaringType();
 		ITypeHierarchy hierarchy= fLifeCycle.getHierarchy();
 		
 		boolean filterOverrides= ScriptModelUtil.isSuperType(hierarchy, typeToFindIn, filterType);
 		IType focusType= filterOverrides ? filterType : typeToFindIn;
 		
 		if (fMethodOverrideTester == null || !fMethodOverrideTester.getFocusType().equals(focusType)) {
 			fMethodOverrideTester= new MethodOverrideTester(focusType, hierarchy);
 		}
 
 		if (filterOverrides) {
 			return fMethodOverrideTester.findOverriddenMethodInType(typeToFindIn, filterMethod);
 		} else {
 			return fMethodOverrideTester.findOverridingMethodInType(typeToFindIn, filterMethod);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void setInput(Object information) {
 		if (!(information instanceof IModelElement)) {
 			inputChanged(null, null);
 			return;
 		}
 		IModelElement input= null;
 		IMethod locked= null;
 		try {
 			IModelElement elem= (IModelElement) information;
 			
 			switch (elem.getElementType()) {
 				case IModelElement.SCRIPT_PROJECT :
 				case IModelElement.PROJECT_FRAGMENT :
 				case IModelElement.TYPE :
 				case IModelElement.SOURCE_MODULE :
 					input= elem;
 					break;
 								
 				case IModelElement.METHOD :
 					IMethod method= (IMethod) elem;
 					if (!method.isConstructor()) {
 						locked= method;				
 					}
 					input= method.getDeclaringType();
 					break;
 				case IModelElement.FIELD :				
 					input= ((IMember) elem).getDeclaringType();
 					break;
 				case IModelElement.PACKAGE_DECLARATION :
 					input= elem.getParent().getParent();
 					break;				
 				default :
 					DLTKUIPlugin.logErrorMessage("Element unsupported by the hierarchy: " + elem.getClass()); //$NON-NLS-1$
 					input= null;
 			}
 		} catch (ModelException e) {
 			DLTKUIPlugin.log(e);
 		}
 		
 		super.setTitleText(getHeaderLabel(locked == null ? input : locked));
 		try {
 			fLifeCycle.ensureRefreshedTypeHierarchy(input, DLTKUIPlugin.getActiveWorkbenchWindow());
 		} catch (InvocationTargetException e1) {
 			input= null;
 		} catch (InterruptedException e1) {
 			dispose();
 			return;
 		}
 		IMember[] memberFilter= locked != null ? new IMember[] { locked } : null;
 		
 		TraditionalHierarchyContentProvider contentProvider= new TraditionalHierarchyContentProvider(fLifeCycle);
 		contentProvider.setMemberFilter(memberFilter);
 		getTreeViewer().setContentProvider(contentProvider);		
 		
 		fOtherContentProvider= new SuperTypeHierarchyContentProvider(fLifeCycle);
 		fOtherContentProvider.setMemberFilter(memberFilter);
 		
 		fFocus= locked;
 		
 		Object[] topLevelObjects= contentProvider.getElements(fLifeCycle);
 		if (topLevelObjects.length > 0 && contentProvider.getChildren(topLevelObjects[0]).length > 40) {
 			fDoFilter= false;
 		} else {
 			getTreeViewer().addFilter(new NamePatternFilter());
 		}
 
 		Object selection= null;
 		if (input instanceof IMember) {
 			selection=  input;
 		} else if (topLevelObjects.length > 0) {
 			selection=  topLevelObjects[0];
 		}
 		inputChanged(fLifeCycle, selection);
 	}
 	
 	protected void stringMatcherUpdated() {
 		if (fDoFilter) {
 			super.stringMatcherUpdated(); // refresh the view
 		} else {
 			selectFirstMatch();
 		}
 	}
 	
 	protected void toggleHierarchy() {
 		TreeViewer treeViewer= getTreeViewer();
 		
 		treeViewer.getTree().setRedraw(false);
 		
 		Object[] expandedElements= treeViewer.getExpandedElements();
 		TypeHierarchyContentProvider contentProvider= (TypeHierarchyContentProvider) treeViewer.getContentProvider();
 		treeViewer.setContentProvider(fOtherContentProvider);
 
 		treeViewer.refresh();
 		if (fOtherExpandedElements != null) {
 			treeViewer.setExpandedElements(fOtherExpandedElements);
 		} else {
 			treeViewer.expandAll();
 		}
 		
 		treeViewer.getTree().setRedraw(true);
 		
 		fOtherContentProvider= contentProvider;
 		fOtherExpandedElements= expandedElements;
 		
 		updateStatusFieldText();
 	}
 	
 	
 	private String getHeaderLabel(IModelElement input) {
 		if (input instanceof IMethod) {
 			String[] args= { input.getParent().getElementName(), 
 					ScriptElementLabels.getDefault().getElementLabel(input, ScriptElementLabels.ALL_DEFAULT) };
 			return Messages.format(TypeHierarchyMessages.HierarchyInformationControl_methodhierarchy_label, args); 
 		} else if (input != null) {
 			String arg= 
 				ScriptElementLabels.getDefault().getElementLabel(input, ScriptElementLabels.DEFAULT_QUALIFIED);
 			return Messages.format(TypeHierarchyMessages.HierarchyInformationControl_hierarchy_label, arg);	 
 		} else {
 			return ""; //$NON-NLS-1$
 		}
 	}
 	
 	protected String getStatusFieldText() {
 		KeySequence[] sequences= getInvokingCommandKeySequences();
 		String keyName= ""; //$NON-NLS-1$
 		if (sequences != null && sequences.length > 0)
 			keyName= sequences[0].format();
 		
 		if (fOtherContentProvider instanceof TraditionalHierarchyContentProvider) {
 			return Messages.format(TypeHierarchyMessages.HierarchyInformationControl_toggle_traditionalhierarchy_label, keyName); 
 		} else {
 			return Messages.format(TypeHierarchyMessages.HierarchyInformationControl_toggle_superhierarchy_label, keyName); 
 		}
 	}
 	
 	/*
 	 * @see org.eclipse.jdt.internal.ui.text.AbstractInformationControl#getId()
 	 */
 	protected String getId() {
 		return "org.eclipse.jdt.internal.ui.typehierarchy.QuickHierarchy"; //$NON-NLS-1$
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	protected Object getSelectedElement() {
 		Object selectedElement= super.getSelectedElement();
 		if (selectedElement instanceof IType && fFocus != null) {
 			IType type= (IType) selectedElement;
 			try {
 				return findMethod(fFocus, type);
 			} catch (ModelException e) {
 				DLTKUIPlugin.log(e);
 			}
 		}
 		return selectedElement;
 	}
 }
