 package org.tekkotsu.gef;
 
 
 
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.gef.ContextMenuProvider;
 import org.eclipse.gef.DefaultEditDomain;
 import org.eclipse.gef.GraphicalViewer;
 import org.eclipse.gef.KeyHandler;
 import org.eclipse.gef.KeyStroke;
 import org.eclipse.gef.MouseWheelHandler;
 import org.eclipse.gef.MouseWheelZoomHandler;
 import org.eclipse.gef.editparts.ScalableRootEditPart;
 import org.eclipse.gef.editparts.ZoomManager;
 import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
 import org.eclipse.gef.palette.MarqueeToolEntry;
 import org.eclipse.gef.palette.PaletteDrawer;
 import org.eclipse.gef.palette.PaletteGroup;
 import org.eclipse.gef.palette.PaletteRoot;
 import org.eclipse.gef.palette.PaletteSeparator;
 import org.eclipse.gef.palette.SelectionToolEntry;
 import org.eclipse.gef.ui.actions.GEFActionConstants;
 import org.eclipse.gef.ui.actions.ZoomInAction;
 import org.eclipse.gef.ui.actions.ZoomOutAction;
 import org.eclipse.gef.ui.parts.GraphicalEditor;
 import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.actions.ActionFactory;
 import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
 import org.tekkotsu.api.ConstructorCall;
 import org.tekkotsu.api.DefaultClassReader;
 import org.tekkotsu.api.NodeClass;
 import org.tekkotsu.api.NodeInstance;
 import org.tekkotsu.api.SetupMachine;
 import org.tekkotsu.api.TransitionClass;
 import org.tekkotsu.factories.NodeCreationFactory;
 import org.tekkotsu.factories.*;
 
 public class StateMachineEditor extends GraphicalEditorWithFlyoutPalette {
 	
 	//ID of the editor
 	public static final String ID = "editor.statemachineeditor";
 	
 	// Hashmaps to store the nodeclasses and names.
 	static final HashMap<String, NodeClass> nodesMap = new HashMap<String, NodeClass>();
 	static final HashMap<String, TransitionClass> transitionsMap = new HashMap<String, TransitionClass>();
 	
 	//Constructor
 	public StateMachineEditor(){
 		
 		//Specify the domain for the editor
 		setEditDomain(new DefaultEditDomain(this));
 		
 	}
 
 	
 	//TODO
 	protected void initializeGraphicalViewer() {
 		
 		GraphicalViewer viewer = getGraphicalViewer();
 		
 		NodeClass beh = composer.ClassView.getNodeClass();
 		SetupMachine setup = beh.getSetupMachine();
		ArrayList<NodeInstance> nodes = setup.getNodes();
 		
 		
 		for(int i = 0; i < nodes.size(); i++){
 			int x = 200*(i+1);
 			nodes.get(i).setShape(new Rectangle(x,40,150,150));
 		}
 		
 		//Get the list of subclasses
 		ArrayList<NodeClass> subs = beh.getSubClasses();
 		
 		//Cast and add all the nodes from the list to the children list
 		for(int i = 0; i < subs.size(); i++){
 			int x = 200*(i+1);
 			subs.get(i).setShape(new Rectangle(x,40,150,150));
 			beh.addChild2(subs.get(i));
 		}
 		
 		//Add the setupmachine if exists to the list of children too.
 		if(beh.getSetupMachine() != null){
 			beh.getSetupMachine().setShape(new Rectangle(25,300,1000,300));
 			beh.addChild2(beh.getSetupMachine());
 		}
 		
 		/*
 		SetupMachine s = new SetupMachine();
 		NodeClass n = new NodeClass("Albert", new ConstructorCall("const"));
 		n.setSetupMachine(s);*/
 		
 		viewer.setContents(composer.ClassView.getNodeClass());
 		//viewer.setContents(CreateBehavior());
 		//viewer.setContents(n);
 	}
 
 
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	protected void configureGraphicalViewer() {
 
 		// Adds Zoom support
 		double[] zoomLevels;
 		ArrayList<String> zoomContributions;
 
 		super.configureGraphicalViewer();
 		GraphicalViewer viewer = getGraphicalViewer();
 		viewer.setEditPartFactory(new AppEditPartFactory());
 
 		ScalableRootEditPart rootEditPart = new ScalableRootEditPart();
 		viewer.setRootEditPart(rootEditPart);
 		ZoomManager manager = rootEditPart.getZoomManager();
 		getActionRegistry().registerAction(new ZoomInAction(manager));
 		getActionRegistry().registerAction(new ZoomOutAction(manager));
 		// list of possible zooms
 		zoomLevels = new double[] { 0.25, 0.5, 0.75, 1.0, 1.5, 2.0, 2.5, 3.0,
 				4.0, 5.0, 10.0, 20.0 };
 		manager.setZoomLevels(zoomLevels);
 		// zoom predefinitions
 		zoomContributions = new ArrayList<String>();
 		zoomContributions.add(ZoomManager.FIT_ALL);
 		zoomContributions.add(ZoomManager.FIT_HEIGHT);
 		zoomContributions.add(ZoomManager.FIT_WIDTH);
 		manager.setZoomLevelContributions(zoomContributions);
 
 		// Keyboard handler that allows for view manipulation with '+' and '-'
 		KeyHandler keyHandler = new KeyHandler();
 
 		keyHandler.put(KeyStroke.getPressed(SWT.DEL, 127, 0),
 				getActionRegistry().getAction(ActionFactory.DELETE.getId()));
 
 		keyHandler.put(KeyStroke.getPressed('+', SWT.KEYPAD_ADD, 0),
 				getActionRegistry().getAction(GEFActionConstants.ZOOM_IN));
 		keyHandler.put(KeyStroke.getPressed('-', SWT.KEYPAD_SUBTRACT, 0),
 				getActionRegistry().getAction(GEFActionConstants.ZOOM_OUT));
 
 		viewer.setProperty(MouseWheelHandler.KeyGenerator.getKey(SWT.NONE),
 				MouseWheelZoomHandler.SINGLETON);
 
 		viewer.setKeyHandler(keyHandler);
 
 		// Keyhander as it pertains to the outline
 		keyHandler = new KeyHandler();
 		keyHandler.put(KeyStroke.getPressed(SWT.DEL, 127, 0),
 				getActionRegistry().getAction(ActionFactory.DELETE.getId()));
 		keyHandler.put(KeyStroke.getPressed('+', SWT.KEYPAD_ADD, 0),
 				getActionRegistry().getAction(GEFActionConstants.ZOOM_IN));
 		keyHandler.put(KeyStroke.getPressed('-', SWT.KEYPAD_SUBTRACT, 0),
 				getActionRegistry().getAction(GEFActionConstants.ZOOM_OUT));
 		viewer.setProperty(MouseWheelHandler.KeyGenerator.getKey(SWT.NONE),
 				MouseWheelZoomHandler.SINGLETON);
 
 		viewer.setKeyHandler(keyHandler);
 
 		// adds context menu to right click
 		ContextMenuProvider provider = new AppContextMenuProvider(viewer,
 				getActionRegistry());
 		viewer.setContextMenu(provider);
 
 	}
 
 	// Nested class called using this adapter method
 	public Object getAdapter(Class type) {
 		if (type == ZoomManager.class)
 			return ((ScalableRootEditPart) getGraphicalViewer()
 					.getRootEditPart()).getZoomManager();
 		/*if (type == IContentOutlinePage.class) {
 			return new OutlinePage();
 		}*/
 		return super.getAdapter(type);
 	}
 	
 	//Return the map of the nodes
 	public static HashMap<String, NodeClass> getNodesMap(){
 		return nodesMap;
 	}
 
 
 	@Override
 	protected PaletteRoot getPaletteRoot() {
 		PaletteRoot root = new PaletteRoot();
 
 		PaletteGroup manipGroup = new PaletteGroup("Manipulation objets");
 		root.add(manipGroup);
 
 		SelectionToolEntry selectionToolEntry = new SelectionToolEntry();
 		manipGroup.add(selectionToolEntry);
 		manipGroup.add(new MarqueeToolEntry());
 
 		root.setDefaultEntry(selectionToolEntry);
 
 		// Ads separator
 
 		PaletteSeparator sep2 = new PaletteSeparator();
 		root.add(sep2);
 		PaletteGroup instGroup = new PaletteGroup("Creation elements");
 		root.add(instGroup);
 
 		// Drag and drop Node
 
 		{
 
 			try {
 
 				// Read nodes in
 				final ArrayList<NodeClass> nodesList = new DefaultClassReader()
 				.getNodes();
 
 				for (int i = 0; i < nodesList.size(); i++) {
 
 					// Current node to operate with.
 					NodeClass currentNode = nodesList.get(i);
 
 					// Add name and the nodeclass object to the map
 					nodesMap.put(currentNode.getName(), currentNode);
 
 					// Create instance object
 					NodeInstance instance = new NodeInstance(currentNode);
 
 					CombinedTemplateCreationEntry entry = new CombinedTemplateCreationEntry(
 							currentNode.getName(), "Creation of a "
 									+ currentNode.getName(), instance,
 									new NodeCreationFactory(NodeInstance.class,
 											currentNode.getName()), null, null);
 					// entry.addTemplateTransferDragSourceListener();
 
 					instGroup.add(entry);
 				}
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		}
 
 		PaletteSeparator sep3 = new PaletteSeparator();
 		root.add(sep3);
 
 		PaletteDrawer connectionElements = new PaletteDrawer(
 				"Connecting Elements");
 		root.add(connectionElements);
 
 		{
 			/*
 			try {
 
 				// Read nodes in
 				final ArrayList<TransitionClass> transitionsList = new DefaultClassReader()
 				.getTransitions();
 
 				for (int i = 0; i < transitionsList.size(); i++) {
 
 					// Current node to operate with.
 					TransitionClass currentTransition = transitionsList.get(i);
 
 					// Add name and the nodeclass object to the map
 					transitionsMap.put(currentTransition.getName(),
 							currentTransition);
 
 					// Create instance object
 					TransitionInstance instance = new TransitionInstance(
 							currentTransition);
 
 					connectionElements.add(new ConnectionCreationToolEntry(
 							currentTransition.getName(), "Create "
 									+ currentTransition.getName()
 									+ " Connections",
 									new TransitionInstanceCreationFactory(
 											TransitionInstance.class, currentTransition
 											.getName()), null, null));
 
 				}
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}*/
 
 		}
 
 		return root;
 
 	}
 	
 	
 
 }
