 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.dawb.workbench.ui.views;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 import org.dawb.common.services.IExpressionObject;
 import org.dawb.common.services.IVariableManager;
 import org.dawb.common.ui.editors.ICheckableObject;
 import org.dawb.common.ui.editors.IDatasetEditor;
 import org.dawb.common.ui.editors.IPlotUpdateParticipant;
 import org.dawb.common.ui.plot.AbstractPlottingSystem;
 import org.dawb.common.ui.slicing.SliceComponent;
 import org.dawb.common.ui.util.EclipseUtils;
 import org.dawb.workbench.ui.editors.CheckableObject;
 import org.dawb.workbench.ui.editors.PlotDataComponent;
 import org.dawnsci.plotting.api.PlotType;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.part.Page;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.io.DataHolder;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 
 public class PlotDataPage extends Page implements IPlotUpdateParticipant, IAdaptable {
 
 	private final static Logger logger = LoggerFactory.getLogger(PlotDataPage.class);
 	
 	private IDatasetEditor          editor;
 	private PlotDataComponent       dataSetComponent;
 	private IResourceChangeListener resourceListener;
 	private SliceComponent          sliceComponent;
 	private Composite               content;
 	
 	private static final Collection<String> INACTIVE_PERSPECTIVES;
 	static {
 		INACTIVE_PERSPECTIVES = Arrays.asList("uk.ac.diamond.scisoft.ncd.rcp.ncdperspective",
 				                              "uk.ac.diamond.scisoft.ncd.rcp.ncdcalibrationperspective",
 				                              "uk.ac.diamond.scisoft.dataexplorationperspective");
 	}
 	/**
 	 * Checks perspective to see if a 'Data' page is required.
 	 * @param ed
 	 * @return
 	 */
 	public static PlotDataPage getPageFor(IDatasetEditor ed) {
 		// Fix http://jira.diamond.ac.uk/browse/DAWNSCI-273
 		// for DExplore and NCD to not show 'Data' pages
 		try {
 			final String id = EclipseUtils.getActivePage().getPerspective().getId();
 			if (INACTIVE_PERSPECTIVES.contains(id)) return null;
 		} catch (Throwable ne) {
 			// ignored
 		}
 		return new PlotDataPage(ed);
 	}
 
 	private PlotDataPage(IDatasetEditor ed) {
 		this.editor = ed;
 	}
 
 	@Override
 	public void createControl(Composite parent) {
 		
 		this.content = new Composite(parent, SWT.NONE);
 		content.setLayout(new GridLayout(1, true));
 		
 		final SashForm form = new SashForm(content, SWT.VERTICAL);
 		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		
 		this.dataSetComponent = new PlotDataComponent(editor);		
 		if (editor!=null && editor.getEditorInput()!=null && dataSetComponent!=null) {
 			dataSetComponent.setFileName(editor.getEditorInput().getName());
 		}
 		dataSetComponent.createPartControl(form);
 		
 		if (dataSetComponent.getDataReductionAction()!=null) {
 			getSite().getActionBars().getToolBarManager().add(dataSetComponent.getDataReductionAction());
 			getSite().getActionBars().getToolBarManager().add(new Separator("data.reduction.separator"));
 		}
 
 		final List<IAction> extras = new ArrayList<IAction>(7);
 		extras.addAll(dataSetComponent.getDimensionalActions());
 		for (IAction iAction : extras) {
 			getSite().getActionBars().getToolBarManager().add(iAction);
 			
 			// Stinky warning, we do not know which actions are menu bar stuff, so 
 			// we add any action with 'preference' in the text.
 			if (iAction.getText()!=null&&iAction.getText().toLowerCase().contains("preference")) {
 				getSite().getActionBars().getMenuManager().add(iAction);
 			}
 		}
 		getSite().setSelectionProvider(dataSetComponent.getViewer());
 				
 		dataSetComponent.addSelectionListener(new ISelectionChangedListener() {
 			@Override
 			public void selectionChanged(final SelectionChangedEvent event) {
 				
 				@SuppressWarnings("unchecked")
 				final List<CheckableObject> sels = ((StructuredSelection)event.getSelection()).toList();
 				if (sels!=null) editor.updatePlot(sels.toArray(new CheckableObject[sels.size()]), PlotDataPage.this, true);
 
 			}
 		});
 		
 		try {
 			
 			
 			IWorkspace workspace = ResourcesPlugin.getWorkspace();
 			this.resourceListener = new IResourceChangeListener() {
 				public void resourceChanged(IResourceChangeEvent event) {
 					
 					if (event==null || event.getDelta()==null) return;
 					
 					final IFile content = EclipseUtils.getIFile(editor.getEditorInput());
 					if (content==null) return;
 					
 					if (event.getDelta().findMember(content.getFullPath())!=null) {
 						getSite().getShell().getDisplay().asyncExec(new Runnable() {
 							@Override
 							public void run() {
 								try {
 									content.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
 								} catch (CoreException e) {
 									logger.error("Cannot refresh "+content, e);
 								}
 								editor.setInput(new FileEditorInput(content));
 								final List<ICheckableObject> sels = dataSetComponent.getSelections();
 								if (sels!=null) editor.updatePlot(sels.toArray(new ICheckableObject[sels.size()]), PlotDataPage.this, false);
 							}
 						});
 					}
 				}
 			};
 			workspace.addResourceChangeListener(resourceListener);
 			
 			this.sliceComponent = new SliceComponent("org.dawb.workbench.views.h5GalleryView");
 			sliceComponent.setPlottingSystem(this.dataSetComponent.getPlottingSystem());
 			sliceComponent.setDataReductionAction(dataSetComponent.getDataReductionAction());
 			sliceComponent.createPartControl(form);
 			sliceComponent.setVisible(false);
 	
 			form.setWeights(new int[] {40, 60});
 		} catch (Exception ne) {
 			logger.error("Cannot create "+getClass().getName(), ne);
 		}
 	}
 
 	@Override
 	public Control getControl() {
 		return content;
 	}
 
 	@Override
 	public void setFocus() {
 		dataSetComponent.setFocus();
 	}
 
 	public void dispose() {
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		if (workspace!=null && resourceListener!=null) {
             workspace.removeResourceChangeListener(resourceListener);
 		}
 		if (dataSetComponent!=null) dataSetComponent.dispose();
 		if (sliceComponent!=null)   sliceComponent.dispose();
  		super.dispose();
 	}
 
 	public IVariableManager getDataSetComponent() {
 		return dataSetComponent;
 	}
 
 	@Override
 	public void setSlicerVisible(boolean vis) {
 		sliceComponent.setVisible(vis);
 	}
 
 	@Override
 	public int getDimensionCount(ICheckableObject checkableObject) {
 		return dataSetComponent.getActiveDimensions(checkableObject, true);
 	}
 
 	@Override
 	public void setSlicerData(ICheckableObject object, String filePath, int[] dims,
 			                  AbstractPlottingSystem plottingSystem) {
 		
 		if (object.isExpression()) {
 			final ILazyDataset lazy = object.getExpression().getLazyDataSet(object.getVariable(), new IMonitor.Stub());
 		    sliceComponent.setData(lazy, object.getName(), filePath, true);
 		} else {
 			try {
 				final DataHolder holder = LoaderFactory.getData(filePath, new IMonitor.Stub());
 				final ILazyDataset lazy = holder.getLazyDataset(object.getName());
 			    sliceComponent.setData(lazy, object.getName(), filePath, false);
 			} catch (Throwable e) {
 				logger.error("Cannot load lazy data!", e);
 			}
 		}
 	}
 
 	@Override
 	public PlotType getPlotMode() {
 		return dataSetComponent.getPlotMode();
 	}
 
 	public SliceComponent getSliceComponent() {
 		return sliceComponent;
 	}
 	
 	@Override
 	public Object getAdapter(Class type) {
 		if (type == String.class) {
 			return "Data";
 		} else if (type == List.class) {
 			final List<IExpressionObject> exprs = new ArrayList<IExpressionObject>();
 			for (ICheckableObject ob : dataSetComponent.getData()) {
 				if (ob.getExpression()!=null) {
 					exprs.add(ob.getExpression());
 				}
 			}
 			return exprs;
 		} else if (type == IFile.class) {
			return dataSetComponent.getIFile(false);
 		}
 		return null;
 	}
 
 }
