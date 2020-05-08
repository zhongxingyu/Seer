 /**
  * Copyright (C) 2013  Luc Hermans
  * 
  * This program is free software: you can redistribute it and/or modify it under the terms of the
  * GNU General Public License as published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with this program.  If
  * not, see <http://www.gnu.org/licenses/>.
  * 
  * Contact information: kozzeluc@gmail.com.
  */
 package org.lh.dmlj.schema.editor.wizard._import.schema;
 
 import static org.lh.dmlj.schema.editor.extension.ExtensionPointConstants.ELEMENT_IMPORT_TOOL;
 import static org.lh.dmlj.schema.editor.extension.ExtensionPointConstants.ELEMENT_IMPORT_TOOLS;
 import static org.lh.dmlj.schema.editor.extension.ExtensionPointConstants.ELEMENT_LAYOUT_MANAGER;
 import static org.lh.dmlj.schema.editor.extension.ExtensionPointConstants.ELEMENT_LAYOUT_MANAGERS;
 import static org.lh.dmlj.schema.editor.extension.ExtensionPointConstants.EXTENSION_POINT_IMPORT_ID;
 
 import java.io.File;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.ui.IImportWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.part.ISetSelectionTarget;
 import org.lh.dmlj.schema.ConnectionLabel;
 import org.lh.dmlj.schema.ConnectionPart;
 import org.lh.dmlj.schema.Connector;
 import org.lh.dmlj.schema.DiagramLabel;
 import org.lh.dmlj.schema.DiagramLocation;
 import org.lh.dmlj.schema.Schema;
 import org.lh.dmlj.schema.SchemaFactory;
 import org.lh.dmlj.schema.SchemaRecord;
 import org.lh.dmlj.schema.Set;
 import org.lh.dmlj.schema.editor.Plugin;
 import org.lh.dmlj.schema.editor.extension.DataEntryPageExtensionElement;
 import org.lh.dmlj.schema.editor.extension.ExtensionElementFactory;
 import org.lh.dmlj.schema.editor.extension.ImportToolExtensionElement;
 import org.lh.dmlj.schema.editor.extension.LayoutManagerExtensionElement;
 import org.lh.dmlj.schema.editor.extension.OptionsExtensionElement;
 import org.lh.dmlj.schema.editor.extension.PostOptionsPagesExtensionElement;
 import org.lh.dmlj.schema.editor.extension.PreOptionsPagesExtensionElement;
 import org.lh.dmlj.schema.editor.importtool.AbstractDataEntryPage;
 import org.lh.dmlj.schema.editor.importtool.AbstractRecordLayoutManager;
 import org.lh.dmlj.schema.editor.importtool.IDataEntryContext;
 import org.lh.dmlj.schema.editor.importtool.IDataEntryPageController;
 import org.lh.dmlj.schema.editor.importtool.ISchemaImportTool;
 import org.lh.dmlj.schema.editor.preference.PreferenceConstants;
 
 /**
  * This wizard is used for both importing (import mode; this is the default 
  * operating mode) and updating (update  mode) a schema.  Setting the operating 
  * mode must be done by calling the setUpdateMode PRIOR to calling init(...). 
  */
 public class SchemaImportWizard extends Wizard implements IImportWizard {
 
 	private ImportToolExtensionElement 	  	 	activeImportToolExtensionElement;	
 	private IDataEntryContext 		  	  	 	context = new DataEntryContext();
 	private List<ImportWizardPage>	   	  		dataEntryWizardPages;
 	private File 							    fileToUpdate;
 	private List<ImportToolExtensionElement> 	importToolExtensionElements;
 	private ImportToolSelectionPage    	  	 	importToolSelectionPage;	
 	private List<LayoutManagerExtensionElement> layoutManagerExtensionElements;
 	private LayoutManagerSelectionPage	  		layoutManagerSelectionPage;
 	private OutputFileSelectionPage    	  		outputFileSelectionPage;
 	private IStructuredSelection 	   	  		selection;
 	private boolean 							updateMode= false;
 	
 	/** 
 	 * Orders the items in the target list so that the sort order matches that  
 	 * of the reference list; new items are placed at the back and sorted by the
 	 * key generated by the key generator
 	 *
 	 * @param targetItems the target list that will be reordered 
 	 * @param referenceItems the reference list providing the desired order
 	 * @param keyProvider the provider of keys allowing item matching
 	 */
 	private <T extends EObject> void sortTargetList(EList<T> targetItems,
 										  			EList<T> referenceItems,
 										  			final IKeyProvider<T> keyProvider) {
 		
 		Map<String, T> targetItemsMap = new HashMap<>();
 		for (T targetItem : targetItems) {
 			String key = keyProvider.getKey(targetItem);
 			Assert.isNotNull(key, "missing key (target item): " +
 							 targetItem.toString());
 			Assert.isTrue(!targetItemsMap.containsKey(key), 
 						  "duplicate key (target item): " + 
 						  targetItem.toString());
 			targetItemsMap.put(key, targetItem);
 		}
 		targetItems.removeAll(new ArrayList<>(targetItems));		
 
 		for (T referenceItem : referenceItems) {
 			String key = keyProvider.getKey(referenceItem);
 			Assert.isNotNull(key, "missing key (reference item): " +
 					 		 referenceItem.toString());
 			if (targetItemsMap.containsKey(key)) {
 				T targetItem = targetItemsMap.get(key);
 				targetItems.add(targetItem);
 				targetItemsMap.remove(key);
 			}
 		}
 
 		List<T> targetLeftoverItems = new ArrayList<>(targetItemsMap.values());
 		Collections.sort(targetLeftoverItems, new Comparator<T>() {
 		@Override
 		public int compare(T item1, T item2) {	
 			String key1 = keyProvider.getKey(item1);
 			String key2 = keyProvider.getKey(item2);
 			return key1.compareTo(key2);				
 		}
 		});		
 		targetItems.addAll(targetLeftoverItems);
 	}	
 	
 	public SchemaImportWizard() {
 		super();
 	}	
 
 	@Override
 	public void addPages() {
 		
 		importToolSelectionPage = 
 			new ImportToolSelectionPage(importToolExtensionElements,
 										updateMode);		
 		addPage(importToolSelectionPage);
 		
 		// although we don't need the output file selection page when running in
 		// update mode, we add it to the wizard because, if we don't, we will 
 		// not have the opportunity to add the data entry pages for the selected 
 		// import tool (including the options page); when in update mode, we 
 		// will NEVER show the output file selection page however
 		outputFileSelectionPage = new OutputFileSelectionPage(selection);
 		outputFileSelectionPage.setUpdateMode(updateMode);
 		addPage(outputFileSelectionPage);
 		
 	}
 	
 	@Override
 	public boolean canFinish() {
 		if (dataEntryWizardPages == null) {
 			// avoid the 'Finish' button to be enabled too soon
 			return false;
 		} else {
 			return super.canFinish();
 		}
 	}
 	
 	private void createAndInjectController(final ImportWizardPage wizardPage) {
 		// create a controller and inject it in the data entry page's 
 		// @Controller annotated field
 		IDataEntryPageController controller = 
 			new IDataEntryPageController() {
 				@Override
 				public void setErrorMessage(String message) {
 					wizardPage.setErrorMessage(message);
 				}
 				@Override
 				public void setPageComplete(boolean pageComplete) {								
 					wizardPage.setPageComplete(pageComplete);
 				}						
 			};
 		inject(AbstractDataEntryPage.class, wizardPage.getDataEntryPage(), 
 			   Controller.class, controller);		
 	}
 	
 	private ImportWizardPage createImportWizardPage(DataEntryPageExtensionElement configElement,
 													boolean firstDataEntryPageInUpdateMode) {
 
 		// create the data entry page
 		AbstractDataEntryPage dataEntryPage = 
 			configElement.createDataEntryPage();		
 		
 		// wrap the data entry page in a wizard page
 		ImportWizardPage importWizardPage = 
 			new ImportWizardPage(dataEntryPage, configElement.getName(),
 								 configElement.getMessage());
 		importWizardPage.setFirstDataEntryPageInUpdateMode(firstDataEntryPageInUpdateMode);
 		
 		// inject the context in the data entry page's @Context annotated field 
 		injectContext(dataEntryPage);		
 			
 		// create a controller and inject it in the data entry page's 
 		// @Controller annotated field
 		createAndInjectController(importWizardPage);
 		
 		// return the import wizard page
 		return importWizardPage;
 		
 	}
 	
 	protected void doPostProcessing(Schema targetSchema, Schema referenceSchema) {
 		
 		// we'll only do some post-processing when in update mode, allowing the diagram label, if
 		// present, to be retained, and to allow for easier validation of the correctness of 
 		// updating diagrams
 		
 		DiagramLabel referenceDiagramLabel = referenceSchema.getDiagramData().getLabel();					
 		if (referenceDiagramLabel != null) {
 			
 			DiagramLabel targetDiagramLabel = SchemaFactory.eINSTANCE.createDiagramLabel();
 			targetSchema.getDiagramData().setLabel(targetDiagramLabel);
 			targetDiagramLabel.setWidth(referenceDiagramLabel.getWidth());
 			targetDiagramLabel.setHeight(referenceDiagramLabel.getHeight());
 			targetDiagramLabel.setDescription(referenceDiagramLabel.getDescription());
 			
 			DiagramLocation targetDiagramLabelLocation = 
 				SchemaFactory.eINSTANCE.createDiagramLocation();
 			targetSchema.getDiagramData().getLocations().add(targetDiagramLabelLocation);
 			targetDiagramLabel.setDiagramLocation(targetDiagramLabelLocation);
 			targetDiagramLabelLocation.setX(referenceDiagramLabel.getDiagramLocation().getX());
 			targetDiagramLabelLocation.setY(referenceDiagramLabel.getDiagramLocation().getY());
 			targetDiagramLabelLocation.setEyecatcher("diagram label");
 			
 		}		
 		
 		sortTargetList(targetSchema.getDiagramData().getConnectionLabels(), 
 				   	   referenceSchema.getDiagramData().getConnectionLabels(),
 				   	   new IKeyProvider<ConnectionLabel>() {
 			@Override
 			public String getKey(ConnectionLabel item) {
 				if (item.getMemberRole() == null) {
 					return null;
 				}
 				Set set = item.getMemberRole().getSet();
 				if (set == null) {
 					return null;
 				}
 				SchemaRecord record = item.getMemberRole().getRecord(); 
 				if (record == null) {
 					return null;
 				}
 				return set.getName() + ":" + record.getName();				
 			}			
 		});	
 		sortTargetList(targetSchema.getDiagramData().getConnectionParts(), 
 				   	   referenceSchema.getDiagramData().getConnectionParts(),
 				   	   new IKeyProvider<ConnectionPart>() {
 			@Override
 			public String getKey(ConnectionPart item) {
 				if (item.getMemberRole() == null) {
 					return null;
 				}
 				if (item.getMemberRole().getSet() == null) {
 					return null;
 				}
 				if (item.getMemberRole().getRecord() == null) {
 					return null;
 				}
 				String setName = item.getMemberRole().getSet().getName();
 				String recordName = item.getMemberRole().getRecord().getName();
 				int i = item.getMemberRole().getConnectionParts().indexOf(item);
 				return setName + ":" + recordName + ":" + i;				
 			}			
 		});		
 		sortTargetList(targetSchema.getDiagramData().getConnectors(), 
 					   referenceSchema.getDiagramData().getConnectors(),
 					   new IKeyProvider<Connector>() {
 			@Override
 			public String getKey(Connector item) {
 				if (item.getConnectionPart() == null) {
 					return null;
 				}	
 				if (item.getConnectionPart().getMemberRole() == null) {
 					return null;
 				}
 				if (item.getConnectionPart().getMemberRole().getSet() == null) {
 					return null;
 				}
 				if (item.getConnectionPart().getMemberRole().getRecord() == null) {
 					return null;
 				}
 				String setName = 
 					item.getConnectionPart().getMemberRole().getSet().getName();
 				String recordName = 
 					item.getConnectionPart().getMemberRole().getRecord().getName();
 				int i = item.getConnectionPart()
 							.getMemberRole()
 							.getConnectionParts()
 							.indexOf(item.getConnectionPart());
 				return setName + ":" + recordName + ":" + i;
 			}			
 		});
 		sortTargetList(targetSchema.getDiagramData().getLocations(), 
 				   	   referenceSchema.getDiagramData().getLocations(),
 				   	   new IKeyProvider<DiagramLocation>() {
 			@Override
 			public String getKey(DiagramLocation item) {
 				return item.getEyecatcher();
 			}			
 		});
 		sortTargetList(targetSchema.getRecords(), referenceSchema.getRecords(),
 					   new IKeyProvider<SchemaRecord>() {
 			@Override
 			public String getKey(SchemaRecord item) {
 				return item.getName();
 			}			
 		});
 		sortTargetList(targetSchema.getSets(), referenceSchema.getSets(),
 					   new IKeyProvider<Set>() {
 			@Override
 			public String getKey(Set item) {
 				return item.getName();
 			}			
 		});
 	}	
 
 	@Override
 	public IWizardPage getNextPage(IWizardPage page) {
 		if (page == importToolSelectionPage) {
 			
 			// The user has chosen an import tool and cannot change his/her
 			// selection; go get the pre and post data entry pages from the 
 			// import tool extension element and add all import wizard pages 
 			// (including the options page which sits in the middle between pre
 			// and post options pages); after that we will add the layout
 			// manager selection page, which will always be the very last page.
 			// Make sure we build and add the wizard pages only once
 			
 			// step 1 :  data entry wizard pages
 			
 			ImportToolExtensionElement importToolExtensionElement =
 				importToolSelectionPage.getExtensionElement();			
 			
 			if (dataEntryWizardPages == null) {				
 				
 				// keep a reference to the active import tool extension element
 				activeImportToolExtensionElement = importToolExtensionElement;
 				
 				// create the list of data entry wizard pages
 				dataEntryWizardPages = new ArrayList<>();
 				
 				// deal with the pre options pages first
 				PreOptionsPagesExtensionElement preOptionsPagesElement = 
 					activeImportToolExtensionElement.getPreOptionsDataEntryPageExtensionElement();
 				List<DataEntryPageExtensionElement> dataEntryPagesElements = 
 						preOptionsPagesElement.getDataEntryPageExtensionElements();
 				for (DataEntryPageExtensionElement dataEntryPageExtensionElement : 
 					 dataEntryPagesElements) {
 						
 					boolean firstDataEntryPageInUpdateMode = 
 						updateMode && dataEntryWizardPages.isEmpty();
 					
 					// create a data entry wizard page and add it to our list
 					// and to the wizard
 					ImportWizardPage fimportWizardPage = 
 						createImportWizardPage(dataEntryPageExtensionElement,
 											   firstDataEntryPageInUpdateMode);					
 					dataEntryWizardPages.add(fimportWizardPage);
 					addPage(fimportWizardPage);
 				}				
 				
 				// deal with the options wizard page
 				OptionsExtensionElement optionsExtensionElement = 
 					activeImportToolExtensionElement.getOptionsExtensionElement();
 				ImportWizardPage optionsWizardPage = 
 					new ImportWizardPage(new OptionsPage(optionsExtensionElement), 
 										 "_optionsPage", "Set options");
 				injectContext(optionsWizardPage.getDataEntryPage());
 				createAndInjectController(optionsWizardPage);
 				dataEntryWizardPages.add(optionsWizardPage);
 				addPage(optionsWizardPage);
 								
 				// finally, deal with the post options pages (which is also a
 				// data entry page)
 				PostOptionsPagesExtensionElement postOptionsPagesElement = 
 					activeImportToolExtensionElement.getPostOptionsDataEntryPageExtensionElement();
 				dataEntryPagesElements = 
 					postOptionsPagesElement.getDataEntryPageExtensionElements();
 				for (DataEntryPageExtensionElement dataEntryPageExtensionElement : 
 					 dataEntryPagesElements) {
 						
 					// create a data entry wizard page and add it to our list
 					// and to the wizard
 					ImportWizardPage fimportWizardPage = 
 						createImportWizardPage(dataEntryPageExtensionElement, false);					
 					dataEntryWizardPages.add(fimportWizardPage);
 					addPage(fimportWizardPage);
 				}
 												
 				// step 2 :  layout manager selection wizard page (import mode 
 				//           (only)				
 				if (!updateMode) {
 					layoutManagerSelectionPage = 
 						new LayoutManagerSelectionPage();
 					addPage(layoutManagerSelectionPage);
 				}
 				
 			}
 			
 			if (!updateMode) {
 				// import mode: return the output file selection page
 				outputFileSelectionPage.setImportToolExtensionElement(importToolExtensionElement);
 				return outputFileSelectionPage;
 			} else {
 				// update mode: return the first import tool data entry page
 				dataEntryWizardPages.get(0).aboutToShow();
 				return dataEntryWizardPages.get(0);
 			}
 			
 		} else if (page == outputFileSelectionPage) {			
 			
 			// the output file selection page is NEVER shown when in update mode
 			// and only added to the wizard for technical reasons
 			if (updateMode) {
 				throw new RuntimeException("logic error: the output file " +
 										   "selection page should never have " +
 										   "been shown when in update mode");
 			}
 			
 			if (!dataEntryWizardPages.get(0).isRelevant()) {
 				String message = "The first page in import contributions " +
 								 "must ALWAYS be relevant.\n\nplug-in: " +
 								 activeImportToolExtensionElement.getPluginId() + 
 								 "\nimportTool: " +
 								 activeImportToolExtensionElement.getName();
 				throw new RuntimeException(message);
 			}
 			dataEntryWizardPages.get(0).aboutToShow();
 			return dataEntryWizardPages.get(0);
 			
 		} else if (dataEntryWizardPages != null &&
 				   dataEntryWizardPages.contains(page)) {
 			
 			int i = dataEntryWizardPages.indexOf(page);
 			if (i < 0) {
 				throw new RuntimeException("logic error");
 			} else {
 				// get the next relevant data entry page, if any
 				ImportWizardPage nextPage = getNextRelevantPageIndex(i);
 				if (nextPage != null) {
 					
 					// there is a next relevant data entry page, so return it
 					nextPage.aboutToShow();
 					return nextPage;
 					
 				} else if (!updateMode) {
 					
 					// we are in import mode and there is no next relevant data
 					// entry page; prepare the layout manager selection page and 
 					// return it
 					List<LayoutManagerExtensionElement> suitableLayoutManagers = 
 						new ArrayList<>();
 					for (LayoutManagerExtensionElement extensionElement :
 						 layoutManagerExtensionElements) {
 						
 						String schemaName = 
 							context.getAttribute(IDataEntryContext.SCHEMA_NAME);
 						short schemaVersion = 
 							((Short) context.getAttribute(IDataEntryContext.SCHEMA_VERSION))
 							.shortValue();
 						if (extensionElement.isValidFor(schemaName, schemaVersion)) {
 							suitableLayoutManagers.add(extensionElement);
 						}
 					}
 					layoutManagerSelectionPage.setExtensionElements(suitableLayoutManagers);
 					return layoutManagerSelectionPage;
 					
 				} else {
 					
 					// update mode; there is no next data entry page
 					return null;
 					
 				}
 			}			
 			
 		}
 		return null;
 	}
 	
 	private <T extends Annotation> void inject(Class<?> annotatedClass,
 											   Object target,
 											   Class<T> annotationClass, 
 											   Object value) {
 		
 		for (Field field : annotatedClass.getDeclaredFields()) {
 			if (field.getAnnotation(annotationClass) != null) {
 				field.setAccessible(true);
 				try {
 					field.set(target, value);					
 				} catch (IllegalArgumentException | IllegalAccessException e) {
 					throw new RuntimeException(e);
 				}
 			}						
 		}		
 	}
 	
 	private void injectContext(AbstractDataEntryPage dataEntryPage) {
 		// inject the context in the data entry page's @Context annotated field
 		inject(AbstractDataEntryPage.class, dataEntryPage, Context.class, 
 			   context);		
 	}
 
 	private ImportWizardPage getNextRelevantPageIndex(int i) {
 		for (int j = i + 1; j < dataEntryWizardPages.size(); j++) {
 			if (dataEntryWizardPages.get(j).isRelevant()) {
 				return dataEntryWizardPages.get(j);
 			}
 		}		
 		return null;
 		
 	}
 
 	@Override
 	public void init(IWorkbench workbench, IStructuredSelection selection) {		
 		
 		this.selection = selection;
 		
 		if (updateMode) {
 			setWindowTitle("Update");
 		} else {
 			setWindowTitle("Import");
 		}
 		
 		if (!selection.isEmpty() &&
 			 selection.getFirstElement() instanceof IFile) {
 			
 			IFile iFile = (IFile) selection.getFirstElement();
 			fileToUpdate = iFile.getLocation().toFile();			
 		}
 		
 		// build the list of import tool extension elements...
 		importToolExtensionElements = 
 			ExtensionElementFactory.getExtensionElements(EXTENSION_POINT_IMPORT_ID, 
 														 ELEMENT_IMPORT_TOOLS, 
 														 ELEMENT_IMPORT_TOOL, 
 														 ImportToolExtensionElement.class);
 		
 		// build the list of layout manager extension elements...
 		layoutManagerExtensionElements = 
 			ExtensionElementFactory.getExtensionElements(EXTENSION_POINT_IMPORT_ID, 
 														 ELEMENT_LAYOUT_MANAGERS, 
 														 ELEMENT_LAYOUT_MANAGER, 
 														 LayoutManagerExtensionElement.class);		
 	}
 
 	@Override
 	public boolean performFinish() {		
 		
 		// get a hold of the import tool and the parameters configured for it
 		// in the defining extension
 		ISchemaImportTool importTool = 
 			activeImportToolExtensionElement.getSchemaImportTool();
 		Properties importToolParms = 
 			activeImportToolExtensionElement.getParameters();	
 		
 		// store the list of compression routines in the plug-in's preference store if it was
 		// modified
 		List<String> contextCompressionProcedures = 
 			context.getAttribute(GeneralContextAttributeKeys.COMPRESSION_PROCEDURE_NAMES);
 		IPreferenceStore store = Plugin.getDefault().getPreferenceStore();
 		String p = store.getString(PreferenceConstants.COMPRESSION_PROCEDURES);
 		if (!p.equals(contextCompressionProcedures)) {
 			String q = contextCompressionProcedures.toString();
 			store.setValue(PreferenceConstants.COMPRESSION_PROCEDURES, 
 						   q.substring(1, q.length() - 1));
 		}
 		
 		// create the import tool proxy
 		final SchemaImportToolProxy proxy = 
 			new SchemaImportToolProxy(importTool, context, importToolParms);					
 		
 		// get a hold of the record layout manager and its (optional) parameters
 		final AbstractRecordLayoutManager recordLayoutManager;
 		final Properties configuredLayoutManagerParms;
 		final Properties userSpecifiedLayoutManagerParms;
 		if (!updateMode) {
 			recordLayoutManager =
 				layoutManagerSelectionPage.getExtensionElement()
 										  .getLayoutManager();	
 			configuredLayoutManagerParms =
 				layoutManagerSelectionPage.getExtensionElement()
 										  .getConfiguredParameters();		 
 			userSpecifiedLayoutManagerParms =
 				layoutManagerSelectionPage.getUserEnteredParameters();			
 		} else {
 			recordLayoutManager = null;
 			configuredLayoutManagerParms = null;
 			userSpecifiedLayoutManagerParms = null;
 		}
 		
 		// copy the default diagram data properties to the data entry context (import mode only)
 		if (!updateMode) {
 			context.setAttribute(IDataEntryContext.DIAGRAMDATA_SHOW_RULERS, 
 								 store.getBoolean(PreferenceConstants.SHOW_RULERS));
 			context.setAttribute(IDataEntryContext.DIAGRAMDATA_SHOW_GRID, 
 								 store.getBoolean(PreferenceConstants.SHOW_GRID));
 			context.setAttribute(IDataEntryContext.DIAGRAMDATA_SNAP_TO_GUIDES, 
 								 store.getBoolean(PreferenceConstants.SNAP_TO_GUIDES));
 			context.setAttribute(IDataEntryContext.DIAGRAMDATA_SNAP_TO_GRID, 
 								 store.getBoolean(PreferenceConstants.SNAP_TO_GRID));
 			context.setAttribute(IDataEntryContext.DIAGRAMDATA_SNAP_TO_GEOMETRY, 
 								 store.getBoolean(PreferenceConstants.SNAP_TO_GEOMETRY));
 		}
 		
 		// populate the schema and persist it to the file specified by the user;
     	// do the work within an operation.		
 		IPath fullPath;
 		if (!updateMode) {
 			fullPath = new Path(outputFileSelectionPage.getOutputFile()
 													   .getAbsolutePath());
 		} else {
 			fullPath = new Path(fileToUpdate.getAbsolutePath());
 		}
 		IPath workspacePath = 
 			ResourcesPlugin.getWorkspace().getRoot().getLocation();
 		p = fullPath.toString().substring(workspacePath.toString().length());
 		IPath path = new Path(p);		
 		final IFile modelFile = 
 			ResourcesPlugin.getWorkspace().getRoot().getFile(path);		
 		IWorkbench workbench = PlatformUI.getWorkbench();
 		final IWorkbenchWindow workbenchWindow = 
 			workbench.getActiveWorkbenchWindow();				
		WorkspaceModifyOperation operation =
			new WorkspaceModifyOperation() {
 			
 			@Override
			protected void execute(IProgressMonitor progressMonitor) {
 				
 				progressMonitor.beginTask(updateMode ? "Update Schema" : "Import Schema", 100);				
 				
 				try {
 											
 					// create the schema and perform validations as we go
 					Schema schema = proxy.invokeImportTool(progressMonitor);					
 					
 					// create a layout manager and invoke its layout() method to 
 					// set the diagram location for all records, system owners
 					// and set labels - perform validations as we go					
 					ILayoutManager layoutManager;
 					if (!updateMode) {
 						// import mode: use the layout manager selected by the
 						// user
 						layoutManager = 
 							new ImportLayoutManager(schema, recordLayoutManager, 
 											  		configuredLayoutManagerParms,
 											  		userSpecifiedLayoutManagerParms);
 						
 					} else {
 						// update mode: use a dedicated layout manager that will
 						// reuse as much as possible from the schema contained
 						// in the file referred to by the current selection
 						layoutManager = 
 							new UpdateLayoutManager(schema, fileToUpdate);						
 					}
 					layoutManager.layout();					
 					
 					// perform post-processing
 					if (updateMode) {
 						doPostProcessing(schema, 
 								 		 layoutManager.getReferenceSchema());
 					}
 					
 					// Create a resource set
 					ResourceSet resourceSet = new ResourceSetImpl();
 
 					// Get the URI of the model file.
 					//
 					URI fileURI = 
 						URI.createPlatformResourceURI(modelFile.getFullPath().toString(), true);
 
 					// Create a resource for this file.
 					Resource resource = resourceSet.createResource(fileURI);
 
 					// Add the initial model object to the contents.												
 					resource.getContents().add(schema);						
 
 					// Save the contents of the resource to the file system.
 					// (currently no backup of the original file in update mode)
 					resource.save(null);					
 					
 				} catch (Exception exception) {
 					
 					// try to delete the file created if it exists
 					if (outputFileSelectionPage.getOutputFile().exists()) {
 						boolean deleted = false;
 						try {
 							deleted = 
 								outputFileSelectionPage.getOutputFile().delete();
 						} catch (Throwable t) {
 							deleted = false;							
 						}
 						if (!deleted) {
 							String message = 
 								"could not delete file " + 
 								outputFileSelectionPage.getOutputFile()
 													   .getAbsolutePath() +
 								" after a failed CA IDMS/DB schema import";
 							System.out.println(message);
 						}
 					}
 					
 					throw new RuntimeException(exception);
 					
 				}
 				finally {
 					if (!proxy.isImportToolDisposed()) {
 						// make sure the import tool is ALWAYS disposed of
 						proxy.disposeImportTool();
 					}
 				}
 				
 				progressMonitor.done();
 				
 			}
 		};		
 			
 		try {
			new ProgressMonitorDialog(getShell()).run(false, false, operation);
 		} catch (Throwable e) {
 			e.printStackTrace();
 			Throwable cause = e.getCause();
 			if (cause != null) {
 				// file niet in een project (folder) of project niet open
 				MessageDialog.openError(workbenchWindow.getShell(), 
 										"File Creation", cause.getMessage());
 			} else {
 				MessageDialog.openError(workbenchWindow.getShell(), 
 										"File Creation", e.getMessage());
 			}
 			return false;
 		}		
 		
 		// refresh the resource in the workspace to avoid 'Resource is out of 
 		// sync with the file system' messages
 		try {
 			IResource resource = 
 				ResourcesPlugin.getWorkspace()
 							   .getRoot()
 							   .findMember(modelFile.getFullPath());
 			resource.refreshLocal(IResource.DEPTH_ZERO, null);
 		} catch (Throwable e) {
 			e.printStackTrace(); // just log whatever problem we encounter
 		}
 		
 		// Select the new file resource in the current view.
 		//		
 		IWorkbenchPage page = workbenchWindow.getActivePage();
 		final IWorkbenchPart activePart = page.getActivePart();
 		if (activePart instanceof ISetSelectionTarget) {
 			ISelection targetSelection = new StructuredSelection(modelFile);
 			((ISetSelectionTarget)activePart).selectReveal(targetSelection);
 		}
 		// Open an editor on the new file (this will only work if the file 
 		// extension is .schema)
 		try {
 			page.openEditor(new FileEditorInput(modelFile),
 							workbench.getEditorRegistry()
 									 .getDefaultEditor(modelFile.getFullPath()
 											 				    .toString())
 											 				    .getId());	
 		} catch (PartInitException exception) {
 			MessageDialog.openError(workbenchWindow.getShell(), "Open Editor", 
 									exception.getMessage());
 			return false;
 		}
 		
 		// signal the success of the wizard operation...
 		return true;		
 		
 	}	
 
 	public void setUpdateMode(boolean newValue) {
 		updateMode = newValue;		
 	}
 	
 	private static interface IKeyProvider<T> {
 		String getKey(T item);
 	}
 	
 }
