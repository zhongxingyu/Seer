 /* **************************************************************************** 
  * CIShell: Cyberinfrastructure Shell, An Algorithm Integration Framework.
  * 
  * All rights reserved. This program and the accompanying materials are made
  * available under the terms of the Apache License v2.0 which accompanies
  * this distribution, and is available at:
  * http://www.apache.org/licenses/LICENSE-2.0.html
  * 
  * Created on Aug 21, 2006 at Indiana University.
  * Changed on Dec 19, 2006 at Indiana University
  * 
  * Contributors:
  * 	   Weixia(Bonnie) Huang, Bruce Herr, Ben Markines
  *     School of Library and Information Science, Indiana University 
  * ***************************************************************************/
 package org.cishell.reference.gui.workflow.views;
 
 
 import java.io.File;
 import java.util.Calendar;
 import java.util.Dictionary;
 import java.util.Map;
 
 
 
 import org.cishell.app.service.scheduler.SchedulerListener;
 import org.cishell.framework.algorithm.Algorithm;
 import org.cishell.framework.algorithm.AlgorithmCreationFailedException;
 import org.cishell.framework.algorithm.AlgorithmFactory;
 import org.cishell.framework.algorithm.AlgorithmProperty;
 import org.cishell.framework.algorithm.ParameterMutator;
 import org.cishell.framework.data.Data;
 import org.cishell.reference.gui.menumanager.menu.AlgorithmWrapper;
 import org.cishell.reference.gui.workflow.Utilities.Constant;
 
 import org.cishell.reference.gui.workflow.controller.WorkflowManager;
 import org.cishell.reference.gui.workflow.controller.WorkflowSaver;
 import org.cishell.reference.gui.workflow.model.AlgorithmWorkflowItem;
 import org.cishell.reference.gui.workflow.model.SchedulerContentModel;
 import org.cishell.reference.gui.workflow.model.Workflow;
 import org.cishell.reference.gui.workflow.model.WorkflowItem;
 import org.cishell.reference.gui.workflow.views.WorkflowGUI;
 import org.cishell.reference.gui.workflow.views.DataTreeContentProvider;
 import org.cishell.reference.gui.workflow.views.DataTreeLabelProvider;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.TreeEditor;
 import org.eclipse.swt.events.FocusAdapter;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.log.LogService;
 import org.osgi.service.metatype.AttributeDefinition;
 import org.osgi.service.metatype.MetaTypeProvider;
 import org.osgi.service.metatype.MetaTypeService;
 import org.osgi.service.metatype.ObjectClassDefinition;
 
 import org.cishell.reference.gui.workflow.Activator;
 import org.osgi.framework.Constants;
 //import org.cishell.service.metadata.*;
 
 /**
  * Creates and maintains the overall GUI for the scheduler.  Controls the
  * table and controls (moving, removing, etc.).
  * 
  */
 public class WorkflowView extends ViewPart implements SchedulerListener {
     private static WorkflowView workFlowView;
     public static final String ID_VIEW = "org.cishell.reference.gui.workflow.views.WorkflowView";
 	private TreeViewer viewer;
 	private WorkflowGUI rootItem, currentWorkFlowItem;
 	private Tree tree;    
 	private Map<Algorithm, WorkflowGUI> alToDataGUIItem;
 	private Menu menu;
 	private Menu whiteSpacemenu;
 	private SaveListener saveListener;
 	private RunListener runListener;
 	private DeleteListener deleteListener;
 	private Algorithm errorAlgorithm;
 	private WorkflowMode mode;
 
 	private TreeEditor editor;
 	private Text newEditor;
 	private boolean updatingTreeItem;
 	private WorkflowTreeItem currentParentItem;
     
 	/**
      * Registers itself to a model, and creates the map from algorithm to 
      * GUI item.
      */
     public WorkflowView() {
  		workFlowView = this;
     }
     
     /**
      * Get the current scheduler view
      * @return The scheduler view
      */
     public static WorkflowView getDefault() {
     	return workFlowView;
     }
 
     /**
      * Creates buttons, table, and registers listeners
      * 
 	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
 	 * @param parent The SWT parent
 	 */
     @Override
     public void createPartControl(Composite parent) {
     	
         /*Composite control = new Composite(parent, SWT.NONE);
         GridLayout layout = new GridLayout();
         layout.numColumns = 1;
         control.setLayout(layout);
         //control.setSize(parent.getSize());
         Button removeButton = new Button(control, SWT.PUSH);
         removeButton.setText("New Workflow");
         removeButton.setEnabled(true); */
         
     	this.viewer = new TreeViewer(parent);
 		this.viewer.setContentProvider(new DataTreeContentProvider());
 		this.viewer.setLabelProvider(new DataTreeLabelProvider());
 		
 
 		this.rootItem = new WorkflowGUI(null, null,2);
 		this.viewer.setInput(this.rootItem);
 		this.viewer.expandAll();
 		this.tree = this.viewer.getTree();
 		this.tree.addSelectionListener(new DatamodelSelectionListener());
 		this.tree.addMouseListener(new ContextMenuListener());
          
 	   // Setup the context menu for the tree.
 		
 		this.menu = new Menu(tree);
 		this.menu.setVisible(false);
 
 		MenuItem saveItem = new MenuItem(this.menu, SWT.PUSH);
 		saveItem.setText("Save");
 		this.saveListener = new SaveListener();
 		saveItem.addListener(SWT.Selection, this.saveListener);
 		
 		MenuItem runItem = new MenuItem(this.menu, SWT.PUSH);
 		runItem.setText("Run");
 		this.runListener = new RunListener();
 		runItem.addListener(SWT.Selection,runListener);
 		
 		MenuItem deleteItem = new MenuItem(this.menu, SWT.PUSH);
 		deleteItem.setText("Delete");
 		this.deleteListener = new DeleteListener();
 		deleteItem.addListener(SWT.Selection, this.deleteListener);
 		
 		MenuItem changeItem = new MenuItem(this.menu, SWT.PUSH);
 		changeItem.setText("Change");
 		changeItem.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				handleInput();
 			}
 		});
 		
 		this.editor = new TreeEditor(this.tree);
 		this.editor.horizontalAlignment = SWT.LEFT;
 		this.editor.grabHorizontal = true;
 		this.editor.minimumWidth = 50;
 		
 		//create white spacemennu
 		this.whiteSpacemenu = new Menu(tree);
 		this.whiteSpacemenu.setVisible(false);
 		
 		MenuItem newItem = new MenuItem(this.whiteSpacemenu, SWT.PUSH);
 		newItem.setText("New Workflow");
 
 		newItem.addListener(SWT.Selection, new NewWorkflow());
 		
         addNewWorkflow("Default Workflow");
 		SchedulerContentModel.getInstance().register(this);             
      }
     protected String getMetaTypeID(ServiceReference ref) {
 		String pid = (String) ref.getProperty(Constants.SERVICE_PID);
 		String metatype_pid = (String) ref.getProperty(AlgorithmProperty.PARAMETERS_PID);
 		//String metatype_pid ="parameters_pid2;
 		if (metatype_pid == null) {
 			metatype_pid = pid;
 		}
 
 		return metatype_pid;
 	}
 
 
 	public void setFocus() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void algorithmScheduled(Algorithm algorithm, Calendar time) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void algorithmRescheduled(Algorithm algorithm, Calendar time) {
 		
 	}
 
 	@Override
 	public void algorithmUnscheduled(Algorithm algorithm) {
 		
 	}
 
 	@Override
 	public void algorithmStarted(Algorithm algorithm) {
 		
 	}
 	
 	public void addNewWorkflow(String name)
 	{
 		Workflow workfFlow = WorkflowManager.getInstance().createWorkflow(name, Constant.NormalWorkflow);
 		final WorkflowGUI dataItem=	new WorkflowGUI(workfFlow, this.currentWorkFlowItem, 1);
 		this.currentWorkFlowItem =  dataItem;
 		this.currentParentItem = dataItem;
 		this.rootItem.addChild(dataItem);
 		guiRun(new Runnable() {
 			public void run() {
 				if (!tree.isDisposed()) {
 					// update the TreeView
 					WorkflowView.this.viewer.refresh();
 					// context menu may need to have options enabled/disabled
 					// based on the new selection
 					// update the global selection
 					WorkflowView.this.viewer.expandToLevel(dataItem, 0);
 				}
 			}
 		});	
 	}
 	
 	
 	@Override
 	public void algorithmFinished(Algorithm algorithm, Data[] createdData) {
 		if(algorithm.equals(errorAlgorithm) || mode == WorkflowMode.RUNNING) return;
 		System.out.println("Algorithm class name="+algorithm.getClass());
 		Dictionary<String, Object> parameters = null;
 		if(algorithm instanceof AlgorithmWrapper){
 		
 			AlgorithmWrapper algo = (AlgorithmWrapper)algorithm;
 			parameters= algo.getParameters();
 		}
 		//get service reference
 		ServiceReference serviceReference = Activator
 				.getSchedulerService().getServiceReference(algorithm);
 		String algorithmLabel = "";
 		if (serviceReference != null) {
 			algorithmLabel = (String) serviceReference
 					.getProperty(AlgorithmProperty.LABEL);
 		}
 		AlgorithmFactory factory =
 				(AlgorithmFactory) Activator.getContext().getService(serviceReference);
 
 		String pid = (String) serviceReference.getProperty(Constants.SERVICE_PID);
 		AlgorithmWorkflowItem wfi = new AlgorithmWorkflowItem(algorithmLabel, WorkflowManager.getInstance().getUniqueInternalId(),serviceReference);
 		wfi.setParameters(parameters);
 		wfi.setWorkflow(currentWorkFlowItem.getWorkflow());
 		currentWorkFlowItem.getWorkflow().add(wfi);
 		System.out.println("Algorithm with name"+algorithmLabel+"started");
 		final AlgorithmItemGUI dataItem=	new AlgorithmItemGUI(wfi, this.currentParentItem);
 		this.currentParentItem.addChild(dataItem);
 		System.out.println("current Parent Item !!!!!!!!!!"+ currentParentItem.getType()+" "+currentParentItem.getLabel());
 		this.currentParentItem = dataItem;
 		//this.currentWorkFlowItem.addChild(dataItem);
 		
 		guiRun(new Runnable() {
 			public void run() {
 				if (!tree.isDisposed()) {
 					// update the TreeView
 					WorkflowView.this.viewer.refresh();
 					WorkflowView.this.viewer.expandToLevel(dataItem, 0);
 				}
 			}
 		});
 		
 		// Create algorithm parameters.
 		String metatypePID = getMetaTypeID(serviceReference);
 		//get the input parameters 
 		MetaTypeProvider provider = null;
 
 		try {
 			provider = getPossiblyMutatedMetaTypeProvider(metatypePID, pid, factory, serviceReference);
 		} catch (AlgorithmCreationFailedException e) {
 			String format =
 				"An error occurred when creating the algorithm \"%s\" with the data you " +
 				"provided.  (Reason: %s)";
 			String logMessage = String.format(
 				format,
 				serviceReference.getProperty(AlgorithmProperty.LABEL),
 				e.getMessage());
 			//log(LogService.LOG_ERROR, logMessage, e);
 
 			return ;
 		} catch (Exception e) {
 			return ;
 		}	
 		
 		if(parameters == null || parameters.isEmpty() ) return;
 				
 		final GeneralTreeItem  paramItem = new GeneralTreeItem("Parameters",Constant.Label, dataItem,getImage("matrix.png","org.cishell.reference.gui.workflow"));
 		dataItem.addChild(paramItem);
 		ObjectClassDefinition obj = provider.getObjectClassDefinition(metatypePID, null);		
 		if (obj != null) {
 			AttributeDefinition[] attr =
 				obj.getAttributeDefinitions(ObjectClassDefinition.ALL);
 			
 			for (int i = 0; i < attr.length; i++) {
 				String id = attr[i].getID();
 				String name = attr[i].getName();
 				// add this into the hashmap of Algorithm Item
 				wfi.add(name, id);
 				Object valueRaw = parameters.get(id);
 				String value = "";
 				if(valueRaw != null)
 				{
 					value = valueRaw.toString();
 				}					
 				//System.out.println( "id=" +id +"name="+ name +"\n");
 				GeneralTreeItem  paramName = new GeneralTreeItem( name, Constant.ParameterName, paramItem, getImage("matrix.png","org.cishell.reference.gui.workflow"));
 				paramItem.addChildren(paramName);
 				GeneralTreeItem  paramValue = new GeneralTreeItem( value, Constant.ParameterValue, paramName, getImage("matrix.png","org.cishell.reference.gui.workflow"));
 				paramName.addChildren(paramValue);				
 			}
 		}
 		
 		guiRun(new Runnable() {
 			public void run() {
 				if (!tree.isDisposed()) {
 					// update the TreeView
 					WorkflowView.this.viewer.refresh();
 					// context menu may need to have options enabled/disabled
 					// based on the new selection
 					// update the global selection
 					WorkflowView.this.viewer.expandToLevel(paramItem, 0);
 				}
 			}
 		});
 	}
 
 	@Override
 	public void algorithmError(Algorithm algorithm, Throwable error) {
 		this.errorAlgorithm = algorithm;
 		
 		
 	}
 
 	@Override
 	public void schedulerRunStateChanged(boolean isRunning) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void schedulerCleared() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	private void guiRun(Runnable run) {
 		if (Thread.currentThread() == Display.getDefault().getThread()) {
 			run.run();
 		} else {
 			Display.getDefault().syncExec(run);
 		}
 	}
 	
 	public WorkflowMode getMode() {
 		return mode;
 	}
 	protected MetaTypeProvider getPossiblyMutatedMetaTypeProvider(
 			String metatypePID, String pid,	AlgorithmFactory factory, ServiceReference serviceRef)
 			throws AlgorithmCreationFailedException {
 		MetaTypeProvider provider = null;
 		MetaTypeService metaTypeService = (MetaTypeService)
 			Activator.getService(MetaTypeService.class.getName());
 		
 		//ParameterMetaTypeProvider provider2  =(ParameterMetaTypeProvider)Activator.getService(ParameterMetaTypeProvider.class.getName());
 		if (metaTypeService != null) {
 			provider = metaTypeService.getMetaTypeInformation(serviceRef.getBundle());
 		}
 		
 
 		if ((factory instanceof ParameterMutator) && (provider != null)) {
 			try {
 				ObjectClassDefinition objectClassDefinition =
 					provider.getObjectClassDefinition(metatypePID, null);
 
 				if (objectClassDefinition == null) {
 					//logNullOCDWarning(pid, metatypePID);
 				}

				/*try {
					objectClassDefinition =
						((ParameterMutator) factory).mutateParameters(data, objectClassDefinition);

					if (objectClassDefinition != null) {
						provider = new BasicMetaTypeProvider(objectClassDefinition);
					}
				} catch (AllParametersMutatedOutException e) {
					provider = null;
				}*/
 			} catch (IllegalArgumentException e) {
 				//log(LogService.LOG_DEBUG, pid + " has an invalid metatype id: " + metatypePID, e);
 			}
 		}
 
 	/*	if (provider != null) {
 			provider = wrapProvider(serviceRef, provider);
 		}*/
 
 		return provider;
 	}
 
 
 	/*
 	 * Listens for right-clicks on TreeItems and opens the context menu when
 	 * needed.
 	 */
 	private class ContextMenuListener extends MouseAdapter {
 		public void mouseUp(MouseEvent event) {
 			//System.out.println("Mouse Event");
 			if (event.button == 3) {
 				System.out.println(" Inside Mouse Event");
 
 				TreeItem item =
 					WorkflowView.this.tree.getItem(new Point(event.x, event.y));
 
 				if (item != null) {
 					System.out.println("Item is not null");
 					WorkflowView.this.tree.setMenu(WorkflowView.this.menu);
 					WorkflowView.this.tree.getMenu().setVisible(true);
 				} else {
 					WorkflowView.this.tree.setMenu(WorkflowView.this.whiteSpacemenu);
 					WorkflowView.this.tree.getMenu().setVisible(true);
 				}
 			}
 		}
 	}
     
 	public boolean isRootItem(WorkflowGUI wfg )
 	{
 		if (this.rootItem.equals(wfg))
 			return true;
 		return false;
 	}
 	
 	 public static Image getImage(String name, String brandPluginID) {
 	        if (Platform.isRunning()) {
 	        	String imageLocation =
 	        		String.format("%sicons%s%s", File.separator, File.separator, name);
 	            ImageDescriptor imageDescriptor =
 	            	AbstractUIPlugin.imageDescriptorFromPlugin(brandPluginID, imageLocation);
 
 	            if (imageDescriptor != null) {
 	            	return imageDescriptor.createImage(); 
 	            } else {
 	            	String errorMessage = String.format(
 	            		"Could not find the icon '%s' in '%s'. Using the default image instead.",
 	            		imageLocation,
 	            		brandPluginID);
 	            	System.err.println(errorMessage);
                     //need to change
 	            	return AlgorithmItemGUI.getDefaultImage();
 	            }
 	   
 	        } else {
 	        	String format =
 	        		"Could not obtain the image '%s' in '%s', since the platform was not " +
 	        		"running (?). Using the default image instead.";
 	        	String errorMessage = String.format(format, name, brandPluginID);
 	        	System.err.println(errorMessage);
                 //need to change
 	        	return AlgorithmItemGUI.getDefaultImage();
 	        }            
 	    }
 
 	 private void handleInput() {
 			// Clean up any previous editor control
 			Control oldEditor = this.editor.getEditor();
 
 			if (oldEditor != null) {
 				oldEditor.dispose();
 			}
 
 			// Identify the selected row, only allow input if there is a single
 			// selected row
 			TreeItem[] selection = this.tree.getSelection();
 
 			if (selection.length != 1) {
 				return;
 			}
 
 			final TreeItem item = selection[0];
 
 			if (item == null) {
 				return;
 			}
 
 			// The control that will be the editor must be a child of the Table
 			this.newEditor = new Text(this.tree, SWT.NONE);
 			this.newEditor.setText(item.getText());
 			this.newEditor.addFocusListener(new FocusAdapter() {
 				public void focusLost(FocusEvent e) {
 					if (!updatingTreeItem) {
 						updateText(newEditor.getText(), item);
 						
 						
 						// FELIX.  This is not > stupidness.
 					}
 				}
 			});
 			// ENTER ESC
 			this.newEditor.addKeyListener(new KeyAdapter() {
 				public void keyReleased(KeyEvent e) {
 					if ((e.character == SWT.CR) && !WorkflowView.this.updatingTreeItem) {
 						updateText(WorkflowView.this.newEditor.getText(), item);
 					} else if (e.keyCode == SWT.ESC) {
 						WorkflowView.this.newEditor.dispose();
 					}
 				}
 			});
 			this.newEditor.selectAll();
 			this.newEditor.setFocus();
 			this.editor.setEditor(this.newEditor, item);
 		}
 
 	
 	 private void updateText(String newLabel, TreeItem item) {
          System.out.println("Inside Paramete text!!!!!!!!!!!!!");
 
 			this.updatingTreeItem = true;
 	     
 			if (newLabel.startsWith(">"))
 				newLabel = newLabel.substring(1);
 		
 			this.editor.getItem().setText(newLabel);
 			WorkflowTreeItem wfTreeItem =  (WorkflowTreeItem)item.getData();
 			wfTreeItem.setLabel(newLabel);
 			if(wfTreeItem.getType() == Constant.ParameterValue)
 			{
                 System.out.println("Inside Parameter Value!!!!!!!!!!!!!");
 
 				String paramName = wfTreeItem.getParent().getLabel();
 				WorkflowTreeItem alfoITem = wfTreeItem.getParent().getParent().getParent();
 				
 				System.out.println(" !!!!Type of the Object"+alfoITem.getType());
 			    AlgorithmWorkflowItem wfg = (AlgorithmWorkflowItem)((AlgorithmItemGUI)alfoITem).getWfItem();	
 			    // we are getting the type of the parameter and  using reflection we are creating the object
 			   Object obj = wfg.getParameterValue(paramName);
 			   if(obj != null)
 			   {
 				   //As reflecion does not work it is a work around
 				   if(obj instanceof String)
 				   {
 					   obj = newLabel;
 				   }
 				   else if(obj instanceof Integer)
 				   {
 					   obj = Integer.getInteger(newLabel);
 				   }
 				   else if(obj instanceof java.lang.Boolean)
 				   {
 					   obj=  Boolean.getBoolean(newLabel);
 				   }
 				   else if(obj instanceof java.lang.Float)
 				   {
 					   obj=  Float.parseFloat(newLabel);
 				   }
 				   else if(obj instanceof java.lang.Double)
 				   {
 					   obj=  Double.parseDouble(newLabel);
 				   }
 				   else if(obj instanceof java.lang.Long)
 				   {
 					   obj=  Long.parseLong(newLabel);
 				   }
 				   else if(obj instanceof java.lang.Short)
 				   {
 					   obj=  Short.parseShort(newLabel);
 				   }
 			   }
 			   else
 			   {
 				   obj = newLabel;
 			   }
 			    wfg.addParameter(paramName, obj);
                 System.out.println("parameter is" + obj);
 			} 					
 			else if(wfTreeItem.getType() == Constant.Workflow)
 			{			
 				
 			}
 			else if(wfTreeItem.getType() == Constant.AlgorithmItem)
 			{				
 			}
 			viewer.refresh(); 
 			this.newEditor.dispose();
 			updatingTreeItem = false;
 		}
 
 	 
 	 
 	 
 	private class DatamodelSelectionListener extends SelectionAdapter {
 		public void widgetSelected(SelectionEvent e) {
 			//System.out.println("DatamodelSelectionListener called ");
 		     Tree tree = (Tree) e.getSource();
 			 TreeItem[] selection = tree.getSelection();
 			 
 			for (int i = 0; i < selection.length; i++) {
 				//String = ((DataGUIItem) selection[i].getData()).getModel();	
 			}
 		}
 	}
 
 	private class SaveListener implements Listener {
 		public void handleEvent(Event event) {
 			System.out.println("Save button is clicked");			
 			WorkflowSaver state = new WorkflowSaver();
 			state.write();
 		}
 	}
 	
 	private class DeleteListener implements Listener {
 
 		@Override
 		public void handleEvent(Event arg0) {
 			try{
 			TreeItem[] items = WorkflowView.this.tree.getSelection();
 			if(items.length !=1) return;
 			 WorkflowTreeItem itm =(WorkflowTreeItem)items[0].getData();
 			 String type = itm.getType();
 			 if(type == Constant.Workflow)
 			 {	
 				 WorkflowGUI wfGUI = (WorkflowGUI) itm;
 				 System.out.println("Delete "+ wfGUI.getLabel() + " Type:"+ type);
 				 WorkflowManager.getInstance().removeWorkflow(wfGUI.getWorkflow());//model
 				 itm.removeAllChildren();//GUI
 				 rootItem.removeChild(wfGUI);//GUI
 				 WorkflowView.this.viewer.refresh();
 			 }else if (type==Constant.AlgorithmUIItem){
 				 AlgorithmItemGUI aiGUI = (AlgorithmItemGUI) itm;
 				 System.out.println("Delete "+ aiGUI.getLabel() + " Type:"+ type);
				 AlgorithmWorkflowItem wfItem = (AlgorithmWorkflowItem) aiGUI.getWfItem();
				 
 				 Workflow wf = wfItem.getWorkflow();
 				 
 				 WorkflowTreeItem parent = itm.getParent();//GUI
 				 itm.removeAllChildren();
 				 parent.removeChild(itm);
 				 rootItem.removeChild(aiGUI);
 				 WorkflowView.this.viewer.refresh();			
 				 wf.remove(wfItem);//model
 
 			 }else{
 				 System.out.println("Cant Delete GeneralItem");
 			 }
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 			 
 		}
 		
 	}
 	
 	private class RunListener implements Listener {
 		public void handleEvent(Event event) {
 			System.out.print("Inside the Run Listner");
 		    TreeItem[] items =	WorkflowView.this.tree.getSelection();
 		    if(items.length !=1) return;
 			System.out.print("only one item selected"+items[0].getClass());
 			
 		    WorkflowTreeItem itm =(WorkflowTreeItem)items[0].getData();
 		    String type = itm.getType();
 		    if(type == Constant.Workflow)
 		    {
                 WorkflowView.this.mode = WorkflowMode.RUNNING;
 			   ((WorkflowGUI)itm).getWorkflow().run();
 			   WorkflowView.this.mode = WorkflowMode.STOPPED;
 		    }
 		}
 	}
 
 	private class NewWorkflow implements Listener {
 		public void handleEvent(Event event) {
 			WorkflowView.this.addNewWorkflow("Test3");
 					}
 	}
 	
 }
