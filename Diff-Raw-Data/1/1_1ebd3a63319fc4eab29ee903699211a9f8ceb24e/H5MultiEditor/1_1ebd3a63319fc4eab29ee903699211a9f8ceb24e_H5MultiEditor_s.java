 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 
 package org.dawb.workbench.ui.editors;
 
 import java.util.Collection;
 
 import org.dawb.common.services.IVariableManager;
 import org.dawb.common.ui.editors.EditorExtensionFactory;
 import org.dawb.common.ui.slicing.ISlicablePlottingPart;
 import org.dawb.common.ui.slicing.SliceComponent;
 import org.dawb.common.ui.util.EclipseUtils;
 import org.dawb.hdf5.editor.H5Editor;
 import org.dawb.hdf5.editor.H5ValuePage;
 import org.dawb.hdf5.editor.IH5Editor;
 import org.dawb.workbench.ui.views.PlotDataPage;
 import org.dawnsci.plotting.api.IPlottingSystemSelection;
 import org.dawnsci.plotting.api.PlotType;
 import org.dawnsci.plotting.api.tool.IToolPageSystem;
 import org.dawnsci.plotting.api.trace.ColorOption;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.viewers.IContentProvider;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IReusableEditor;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.MultiPageEditorPart;
 import org.eclipse.ui.part.Page;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.io.IMetaData;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 import uk.ac.diamond.scisoft.analysis.rcp.editors.HDF5TreeEditor;
 
 
 public class H5MultiEditor extends MultiPageEditorPart  implements ISlicablePlottingPart, IReusableEditor, IPlottingSystemSelection, IH5Editor {
 
 	// The property org.dawb.editor.h5.use.default is set by default in dawb / dawn vanilla
 	// The property org.dawb.editor.h5.use.default is not set in SDA.
 	private static final String ORG_DAWB_EDITOR_H5_USE_DEFAULT = "org.dawb.editor.h5.use.default";
 
 	private static final Logger logger = LoggerFactory.getLogger(H5MultiEditor.class);
 	private PlotDataEditor dataSetEditor;
 	private IReusableEditor treePage;
 	
 	@Override
 	public void init(IEditorSite site, IEditorInput input) throws PartInitException{
         super.init(site, input);
 	    setPartName(input.getName());
     }
 	
 	@Override
 	public void setInput(final IEditorInput input) {
 		super.setInput(input);
 		setPartName(input.getName());
 		if (dataSetEditor!=null) dataSetEditor.setInput(input);
 		if (treePage!=null)      treePage.setInput(input);
 	}	
 	/**
 	 * It might be necessary to show the tree editor on the first page.
 	 * A property can be introduced to change the page order if this is required.
 	 */
 	@Override
 	protected void createPages() {
 		
         IMetaData metaData = null;
 		try {
 			metaData = LoaderFactory.getMetaData(EclipseUtils.getFilePath(getEditorInput()), null);
 		} catch (Exception e1) {
 			// Allowed to have no meta data at this point.
 		}
 		
 		boolean treeOnTop = false;
 		if (metaData!=null) {
 			if (metaData.getDataNames()==null || metaData.getDataNames().size()<1) {
 				treeOnTop = true;
 			} else {
 				if (getEditorSite().getPage().findViewReference("uk.ac.diamond.scisoft.analysis.rcp.views.DatasetInspectorView")!=null) {
 					treeOnTop = true;
 				}
 			}
 		}
 
 		int index = 0;
 		try {
 			String defaultEditorSetting = System.getProperty(ORG_DAWB_EDITOR_H5_USE_DEFAULT);
 			boolean useH5Editor = defaultEditorSetting == null || defaultEditorSetting.equals("true");
 			if (treeOnTop) {
 				this.treePage = useH5Editor ? new H5Editor() : new HDF5TreeEditor();
 				addPage(index, treePage,   getEditorInput());
 				setPageText(index, "Tree");
 				index++;
 			}
 			
 			try {
 				Collection<IEditorPart> extensions = EditorExtensionFactory.getEditors(this);
 				if (extensions!=null && extensions.size()>0) {
 					for (IEditorPart iEditorPart : extensions) {
 						addPage(index, iEditorPart,  getEditorInput());
 						setPageText(index, iEditorPart.getTitle());
 						index++;
 					}
 				}
 			} catch (Exception e) {
 				logger.error("Cannot read editor extensions!", e);
 			}
 
 			/**
 			 * TODO This list of data sets can be expensive to extract. Consider
 			 * a lazy loading checkbox tree. This means a new PlotDataComponent
 			 * for H5 and a new PlotDataEditor which does not extract meta data
 			 * at all but loads sets as it sees them.
 			 */
 
 			if (!treeOnTop) {
 				this.dataSetEditor = new PlotDataEditor(PlotType.XY);
 				dataSetEditor.getPlottingSystem().setColorOption(ColorOption.BY_NAME);	
 				addPage(index, dataSetEditor, getEditorInput());
 				setPageText(index, "Plot");
 				index++;
 
 				this.treePage = useH5Editor ? new H5Editor() : new HDF5TreeEditor();
 				addPage(index, treePage,   getEditorInput());
 				setPageText(index, "Tree");
 				index++;
 			}
 		} catch (PartInitException e) {
 			logger.error("Cannot initiate "+getClass().getName()+"!", e);
 		}
 		
 		
  	}
 	
 	
 	public void dispose() {
 		dataSetEditor = null;
 		treePage      = null;
 		super.dispose();
 	}
 
 	/** 
 	 * No Save
 	 */
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/** 
 	 * No Save
 	 */
 	@Override
 	public void doSaveAs() {
 		// TODO Auto-generated method stub
 
 	}
 
 	/** 
 	 * We are not saving this class
 	 */
 	@Override
 	public boolean isSaveAsAllowed() {
 		return false;
 	}
 	
 	@Override
 	public IVariableManager getDataSetComponent() {
 		if (dataSetEditor==null) return null;
 		return dataSetEditor.getDataSetComponent();
 	}
 	@Override
 	public SliceComponent getSliceComponent() {
 		if (dataSetEditor==null) return null;
 		return  dataSetEditor.getSliceComponent();
 	}	
 	@Override
 	public void setActivePage(final int ipage) {
 		super.setActivePage(ipage);
 	}
 
 	@Override
 	public IEditorPart getActiveEditor() {
 		return super.getActiveEditor();
 	}
 
 	public PlotDataEditor getDataSetEditor() {
 		if (dataSetEditor==null) return null;
 		return dataSetEditor;
 	}
 
     public Object getAdapter(@SuppressWarnings("rawtypes") final Class clazz) {
 		
 		if (clazz == Page.class) {
 			final PlotDataEditor      ed  = getDataSetEditor();
 			return PlotDataPage.getPageFor(ed);
 			
 		} else if (clazz == IContentProvider.class) {
 			return new H5ValuePage();
 		} else if (clazz == IToolPageSystem.class) {
 			if (dataSetEditor!=null) return dataSetEditor.getPlottingSystem();
 		}
 		
 		return super.getAdapter(clazz);
 	}
     
 
 	@Override
 	public AbstractDataset setDatasetSelected(String name, boolean clearOthers) {
 		return (AbstractDataset)((IPlottingSystemSelection)getDataSetComponent()).setDatasetSelected(name, clearOthers);
 	}
 
 	@Override
 	public void setAll1DSelected(boolean overide) {
 		((IPlottingSystemSelection)getDataSetComponent()).setAll1DSelected(overide);
 	}
 
 	@Override
 	public String getFilePath() {
 		return EclipseUtils.getFilePath(getEditorInput());
 	}
 
 }
