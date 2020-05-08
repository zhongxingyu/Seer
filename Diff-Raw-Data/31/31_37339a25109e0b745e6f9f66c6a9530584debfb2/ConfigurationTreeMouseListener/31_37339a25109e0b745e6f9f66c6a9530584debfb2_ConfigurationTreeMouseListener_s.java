 package confdb.gui;
 
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.tree.*;
 import java.awt.*;
 import java.awt.event.*;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import confdb.gui.menu.ScrollableMenu;
 
 import confdb.data.*;
 
 
 /**
  * ConfigurationTreeMouseListener
  * ------------------------------
  * @author Philipp Schieferdecker
  *
  */
 public class ConfigurationTreeMouseListener extends    MouseAdapter 
                                             implements TreeModelListener
 {
     //
     // member data
     //
 
     /** reference to the tree to be manipulated */
     private JTree tree = null;
     
     /** reference to the configuration tree-model */
     private ConfigurationTreeModel treeModel = null;
     
     /** current software release */
     private SoftwareRelease release = null;
     
     /** popup mneu associated with global psets */
     private JPopupMenu popupPSets = null;
 
     /** popup menu showing all available service templates */
     private JPopupMenu popupServices = null;
 
     /** popup menu showing all available event data source templates */
     private JPopupMenu popupEDSources = null;
 
     /** popup menu showing all available event setup source templates */
     private JPopupMenu popupESSources = null;
 
     /** popup menu showing all available event setup module templates */
     private JPopupMenu popupESModules = null;
 
     /** popup menu associated with paths */
     private JPopupMenu popupPaths = null;
     
     /** popup menu associated with modules */
     private JPopupMenu popupModules = null;
     
     /** popup menu associated with sequences */
     private JPopupMenu popupSequences = null;
     
     /** action listener for psets-menu actions */
     private PSetMenuListener psetListener = null;
     
     /** action listener for services-menu actions */
     private ServiceMenuListener serviceListener = null;
     
     /** action listener for edsources-menu actions */
     private EDSourceMenuListener edsourceListener = null;
 
     /** action listener for essources-menu actions */
     private ESSourceMenuListener essourceListener = null;
     
     /** action listener for esmodules-menu actions */
     private ESModuleMenuListener esmoduleListener = null;
     
     /** action listener for paths-menu actions */
     private PathMenuListener pathListener = null;
 
     /** action listener for modules-menu actions */
     private ModuleMenuListener moduleListener = null;
 
     /** action listener for sequences-menu actions */
     private SequenceMenuListener sequenceListener = null;
 
 
     //
     // construction
     //
     
     /** standard constructor */
     public ConfigurationTreeMouseListener(JTree tree, JFrame frame,
 					  SoftwareRelease release)
     {
 	this.tree      = tree;
 	this.treeModel = (ConfigurationTreeModel)tree.getModel();
 	this.release   = release;
 	
 	psetListener     = new PSetMenuListener(tree,frame);
 	edsourceListener = new EDSourceMenuListener(tree);
 	essourceListener = new ESSourceMenuListener(tree);
 	esmoduleListener = new ESModuleMenuListener(tree);
 	serviceListener  = new ServiceMenuListener(tree);
 	pathListener     = new PathMenuListener(tree);
 	moduleListener   = new ModuleMenuListener(tree);
 	sequenceListener = new SequenceMenuListener(tree);
     }
     
     
     //
     // member functions
     //
     
     /** MouseAdapter: mousePressed() */
     public void mousePressed(MouseEvent e)
     {
 	maybeShowPopup(e);
     }
     
     /** MouseAdapter: mouseReleased() */
     public void mouseReleased(MouseEvent e)
     {
 	maybeShowPopup(e);
     }
     
     /** check if this event should really trigger the menu to be displayed */
     private void maybeShowPopup(MouseEvent e)
     {
 	if (!e.isPopupTrigger()) return;
 	
 	Configuration config = (Configuration)treeModel.getRoot();
 	if (config.name().length()==0) return;
 	
 	tree = (JTree)e.getComponent();
 	TreePath treePath = tree.getPathForLocation(e.getX(), e.getY());
 	if(treePath==null) return;
 	int      depth = treePath.getPathCount();
 	if(depth<=1) return;
 	
 	tree.setSelectionPath(treePath);
 	
 	Object node = treePath.getLastPathComponent();
 	
 	// show the 'PSets' popup?
 	if (isInTreePath(treePath,treeModel.psetsNode())&&depth<=3) {
 	    updatePSetMenu();
 	    popupPSets.show(e.getComponent(),e.getX(),e.getY());
 	    return;
 	}
 	
 	// show the 'EDSources' popup?
 	if (isInTreePath(treePath,treeModel.edsourcesNode())&&depth<=3) {
 	    updateEDSourceMenu();
 	    popupEDSources.show(e.getComponent(),e.getX(),e.getY());
 	    return;
 	}
 	
 	// show the 'ESSources' popup?
 	if (isInTreePath(treePath,treeModel.essourcesNode())&&depth<=3) {
 	    updateESSourceMenu();
 	    popupESSources.show(e.getComponent(),e.getX(),e.getY());
 	    return;
 	}
 	
 	// show the 'ESModules' popup?
 	if (isInTreePath(treePath,treeModel.esmodulesNode())&&depth<=3) {
 	    updateESModuleMenu();
 	    popupESModules.show(e.getComponent(),e.getX(),e.getY());
 	    return;
 	}
 	
 	// show the 'Services' popup?
 	if (isInTreePath(treePath,treeModel.servicesNode())&&depth<=3) {
 	    updateServiceMenu();
 	    popupServices.show(e.getComponent(),e.getX(),e.getY());
 	    return;
 	}
 	
 	// show the 'Paths' popup?
 	if (isInTreePath(treePath,treeModel.pathsNode())&&depth<=4) {
 	    updatePathMenu();
 	    popupPaths.show(e.getComponent(),e.getX(),e.getY());
 	    return;
 	}
 	
 	// show the 'Modules' popup?
 	if (node instanceof ModuleInstance) {
 	    updateModuleMenu();
 	    popupModules.show(e.getComponent(),e.getX(),e.getY());
 	    return;
 	}
 	
 	// show the 'Sequences' popup?
 	if (isInTreePath(treePath,treeModel.sequencesNode())&&depth<=4) {
 	    updateSequenceMenu();
 	    popupSequences.show(e.getComponent(),e.getX(),e.getY());
 	    return;
 	}
 	
     }
     
     /** check if a node is in a tree path */
     private boolean isInTreePath(TreePath treePath,Object node)
     {
 	for (int i=0;i<treePath.getPathCount();i++)
 	    if (treePath.getPathComponent(i).equals(node)) return true;
 	return false;
     }
     
     /** update 'PSets' menu */
     public void updatePSetMenu()
     {
 	JMenuItem menuItem;
 	popupPSets = new JPopupMenu();
 	
 	TreePath treePath = tree.getSelectionPath();
 	int      depth    = treePath.getPathCount();
 	Object   node     = treePath.getLastPathComponent();
 	
 	menuItem = new JMenuItem("Add PSet");
 	menuItem.addActionListener(psetListener);
 	menuItem.setActionCommand("NEWPSET");
 	popupPSets.add(menuItem);
     
 	if (depth==3) {
 	    menuItem = new JMenuItem("Remove PSet");
 	    menuItem.addActionListener(psetListener);
 	    popupPSets.add(menuItem);
 	}
     }
     
     /** update 'EDSource' menu */
     public void updateEDSourceMenu()
     {
 	JMenuItem menuItem;
 	popupEDSources = new JPopupMenu();
 	
 	TreePath treePath = tree.getSelectionPath();
 	int      depth    = treePath.getPathCount();
 	Object   node     = treePath.getLastPathComponent();
 	
 	JMenu edsourceMenu = new ScrollableMenu("Add EDSource");
 	if (depth==3||
 	    (depth==2&&treeModel.getChildCount(node)>0)) {
 	    edsourceMenu.setEnabled(false);
 	    popupEDSources.add(edsourceMenu);
 	}
 	else {
 	    Iterator it = release.edsourceTemplateIterator();
 	    while (it.hasNext()) {
 		EDSourceTemplate t = (EDSourceTemplate)it.next();
 		menuItem = new JMenuItem(t.name());
 		menuItem.addActionListener(edsourceListener);
 		edsourceMenu.add(menuItem);
 	    }
 	    popupEDSources.add(edsourceMenu);
 	}
 	if (depth==3) {
 	    menuItem = new JMenuItem("Remove EDSource");
 	    menuItem.addActionListener(edsourceListener);
 	    popupEDSources.add(menuItem);
 	}
     }
     
     /** update 'ESSources' menu */
     public void updateESSourceMenu()
     {
 	JMenuItem menuItem;
 	popupESSources    = new JPopupMenu();
 	
 	TreePath treePath = tree.getSelectionPath();
 	int      depth    = treePath.getPathCount();
 	
 	// 'ESSources' or specific event setup source
 	if (depth>=2&&depth<=3) {
 	    JMenu essourceMenu = new ScrollableMenu("Add ESSource");
 	    Iterator it = release.essourceTemplateIterator();
 	    while (it.hasNext()) {
 		ESSourceTemplate t = (ESSourceTemplate)it.next();
 		if (t.instanceCount()>0) {
 		    JMenu instanceMenu = new JMenu(t.name());
 		    for (int i=0;i<t.instanceCount();i++) {
 			Instance instance = t.instance(i);
 			menuItem = new JMenuItem(instance.name());
 			menuItem.setEnabled(false);
 			instanceMenu.add(menuItem);
 		    }
 		    instanceMenu.addSeparator();
 		    menuItem = new JMenuItem("New Instance");
 		    menuItem.addActionListener(essourceListener);
 		    menuItem.setActionCommand(t.name());
 		    instanceMenu.add(menuItem);
 		    essourceMenu.add(instanceMenu);
 		}
 		else {
 		    menuItem = new JMenuItem(t.name());
 		    menuItem.addActionListener(essourceListener);
 		    essourceMenu.add(menuItem);
 		}
 	    }
 	    popupESSources.add(essourceMenu);
 	}
 	
 	// a specific event setup source is selected
 	if (depth==3) {
 	    menuItem = new JMenuItem("Remove ESSource");
 	    menuItem.addActionListener(essourceListener);
 	    popupESSources.add(menuItem);
 	}
     }
     
     /** update 'ESModules' menu */
     public void updateESModuleMenu()
     {
 	JMenuItem menuItem;
 	popupESModules    = new JPopupMenu();
 	
 	TreePath treePath = tree.getSelectionPath();
 	int      depth    = treePath.getPathCount();
 	
 	// 'ESModules' or specific event setup module
 	if (depth>=2&&depth<=3) {
 	    JMenu esmoduleMenu = new ScrollableMenu("Add ESModule");
 	    Iterator it = release.esmoduleTemplateIterator();
 	    while (it.hasNext()) {
 		ESModuleTemplate t = (ESModuleTemplate)it.next();
 		if (t.instanceCount()>0) {
 		    JMenu instanceMenu = new JMenu(t.name());
 		    for (int i=0;i<t.instanceCount();i++) {
 			Instance instance = t.instance(i);
 			menuItem = new JMenuItem(instance.name());
 			menuItem.setEnabled(false);
 			instanceMenu.add(menuItem);
 		    }
 		    instanceMenu.addSeparator();
 		    menuItem = new JMenuItem("New Instance");
 		    menuItem.addActionListener(esmoduleListener);
 		    menuItem.setActionCommand(t.name());
 		    instanceMenu.add(menuItem);
 		    esmoduleMenu.add(instanceMenu);
 		}
 		else {
 		    menuItem = new JMenuItem(t.name());
 		    menuItem.addActionListener(esmoduleListener);
 		    esmoduleMenu.add(menuItem);
 		}
 	    }
 	    popupESModules.add(esmoduleMenu);
 	}
 	
 	// a specific event setup source is selected
 	if (depth==3) {
 	    menuItem = new JMenuItem("Remove ESModule");
 	    menuItem.addActionListener(esmoduleListener);
 	    popupESModules.add(menuItem);
 	}
     }
     
     /** update 'Services' menu */
     public void updateServiceMenu()
     {
 	JMenuItem menuItem;
 	popupServices = new JPopupMenu();
 	int depth = tree.getSelectionPath().getPathCount();
 	
 	JMenu serviceMenu = new ScrollableMenu("Add Service");
 	Iterator it = release.serviceTemplateIterator();
 	while (it.hasNext()) {
 	    ServiceTemplate t = (ServiceTemplate)it.next();
 	    menuItem = new JMenuItem(t.name());
 	    if (t.instanceCount()>0) menuItem.setEnabled(false);
 	    else menuItem.addActionListener(serviceListener);
 	    serviceMenu.add(menuItem);
 	}
 	popupServices.add(serviceMenu);
 	
 	if (depth==3) {
 	    menuItem = new JMenuItem("Remove Service");
 	    menuItem.addActionListener(serviceListener);
 	    popupServices.add(menuItem);
 	}
 	
     }
     
     /** update 'Paths' menu */
     public void updatePathMenu()
     {
 	JMenuItem menuItem;
 	popupPaths = new JPopupMenu();
 
 	TreePath  treePath = tree.getSelectionPath();
 	int       depth    = treePath.getPathCount();
 	Object    node     = treePath.getPathComponent(depth-1);
 	Object    parent   = treePath.getPathComponent(depth-2);
 
 	Configuration config = (Configuration)treeModel.getRoot();
 	
 	// 'Paths' selectd
 	if (depth==2) {
 	    menuItem = new JMenuItem("Add Path");
 	    menuItem.addActionListener(pathListener);
 	    menuItem.setActionCommand("NEWPATH");
 	    popupPaths.add(menuItem);
 	    return;
 	}
 	
 	// specific path is selected
 	if (depth==3) {
 	    Path path = (Path)node;
 
 	    JMenu addModuleMenu = createAddModuleMenu(path,pathListener);
 	    popupPaths.add(addModuleMenu);
 	    
 	    JMenu addPathMenu = createAddPathMenu(path);
 	    popupPaths.add(addPathMenu);
 	    
 	    JMenu addSequenceMenu = createAddSequenceMenu(path,pathListener);
 	    popupPaths.add(addSequenceMenu);
 	    
 	    popupPaths.addSeparator();
 	    
 	    menuItem = new JMenuItem("Rename Path");
 	    menuItem.addActionListener(pathListener);
 	    popupPaths.add(menuItem);
 	    
 	    menuItem = new JMenuItem("Remove Path");
 	    menuItem.addActionListener(pathListener);
 	    popupPaths.add(menuItem);
 
 	    return;
 	}
 	
 	// a specific module/path/sequence reference is selected
 	if (depth==4) {
 	    Path path = (Path)parent;
 	    JMenu addModuleMenu = createAddModuleMenu(path,pathListener);
 	    popupPaths.add(addModuleMenu);
 
 	    JMenu addPathMenu = createAddPathMenu(path);
 	    popupPaths.add(addPathMenu);
 	    
 	    JMenu addSequenceMenu = createAddSequenceMenu(path,pathListener);
 	    popupPaths.add(addSequenceMenu);
 	    
 	    popupPaths.addSeparator();
 	    
 	    if (node instanceof ModuleReference) {
 		menuItem = new JMenuItem("Remove Module");
 		menuItem.addActionListener(pathListener);
 		popupPaths.add(menuItem);
 	    }
 	    if (node instanceof PathReference) {
 		menuItem = new JMenuItem("Remove Path");
 		menuItem.addActionListener(pathListener);
 		popupPaths.add(menuItem);
 	    }
 	    if (node instanceof SequenceReference) {
 		menuItem = new JMenuItem("Remove Sequence");
 		menuItem.addActionListener(pathListener);
 		popupPaths.add(menuItem);
 	    }
 	}
     }
     
     /** update 'Sequences' Menu */
     public void updateSequenceMenu()
     {
 	JMenuItem menuItem;
 	popupSequences = new JPopupMenu();
 	 
 	TreePath treePath = tree.getSelectionPath();
 	int      depth    = treePath.getPathCount();
 	Object   node     = treePath.getPathComponent(depth-1);
 	Object   parent   = treePath.getPathComponent(depth-2);
 	
 	Configuration config = (Configuration)treeModel.getRoot();
 	
 	if (depth==2) {
 	    menuItem = new JMenuItem("Add Sequence");
 	    menuItem.addActionListener(sequenceListener);
 	    popupSequences.add(menuItem);
 	}
 	else if (depth==3) {
 	    Sequence sequence = (Sequence)node;
 
 	    JMenu addModuleMenu = createAddModuleMenu(sequence,sequenceListener);
 	    popupSequences.add(addModuleMenu);
 	    
 	    JMenu addSequenceMenu = createAddSequenceMenu(sequence,sequenceListener);
 	    popupSequences.add(addSequenceMenu);
 	    
 	    popupSequences.addSeparator();
 	    
 	    menuItem = new JMenuItem("Rename Sequence");
 	    menuItem.addActionListener(sequenceListener);
 	    popupSequences.add(menuItem);
 	    
 	    menuItem = new JMenuItem("Remove Sequence");
 	    menuItem.addActionListener(sequenceListener);
 	    popupSequences.add(menuItem);
 	}
 	else if (depth==4) {
 	    Sequence sequence = (Sequence)parent;
 
 	    JMenu addModuleMenu = createAddModuleMenu(sequence,sequenceListener);
 	    popupSequences.add(addModuleMenu);
 
 	    JMenu addSequenceMenu = createAddSequenceMenu(sequence,sequenceListener);
 	    popupSequences.add(addSequenceMenu);
 	    
 	    menuItem = new JMenuItem("Remove Module");
 	    menuItem.addActionListener(sequenceListener);
 	    popupSequences.add(menuItem);
 	}
     }
     
     /** update 'Modules' Menu */
     public void updateModuleMenu()
     {
 	popupModules = new JPopupMenu();
 	JMenuItem menuItem = new JMenuItem("Rename Module");
 	menuItem.addActionListener(moduleListener);
 	popupModules.add(menuItem);
     }
     
     /** create the 'Add Module' submenu */
     private JMenu createAddModuleMenu(ReferenceContainer container,
 				      ActionListener     listener)
     {
 	JMenu     addModuleMenu = new JMenu("Add Module");
 	JMenuItem menuItemAll;
 	JMenuItem menuItem;
 	
 	HashMap<String,JMenu> menuHashMap = new HashMap<String,JMenu>();
 	
 	Iterator it = release.moduleTemplateIterator();
 	while (it.hasNext()) {
 	    ModuleTemplate t = (ModuleTemplate)it.next();
 	    
 	    JMenu moduleTypeMenu;
 	    JMenu moduleTypeAllMenu;
 	    JMenu moduleTypeAndLetterMenu;
 	    
 	    String moduleType = t.type();
 	    String moduleTypeAll = moduleType + "All";
 	    if (!menuHashMap.containsKey(moduleType)) {
 		moduleTypeMenu = new ScrollableMenu(moduleType);
 		moduleTypeAllMenu = new ScrollableMenu("All");
 		menuHashMap.put(moduleType,moduleTypeMenu);
 		menuHashMap.put(moduleTypeAll,moduleTypeAllMenu);
 		addModuleMenu.add(moduleTypeMenu);
 		moduleTypeMenu.add(moduleTypeAllMenu);
 	    }
 	    else {
 		moduleTypeMenu = menuHashMap.get(moduleType);
 		moduleTypeAllMenu = menuHashMap.get(moduleTypeAll);
 	    }
 	    
 	    String moduleLetter = t.name().substring(0,1);
 	    String moduleTypeAndLetter = t.type() + moduleLetter;
 	    if (!menuHashMap.containsKey(moduleTypeAndLetter)) {
 		moduleTypeAndLetterMenu = new ScrollableMenu(moduleLetter);
 		menuHashMap.put(moduleTypeAndLetter,moduleTypeAndLetterMenu);
 		moduleTypeMenu.add(moduleTypeAndLetterMenu);
 	    }
 	    else {
 		moduleTypeAndLetterMenu = menuHashMap.get(moduleTypeAndLetter);
 	    }
 	    
 	    if (t.instanceCount()>0) {
 		JMenu instanceMenuAll = new JMenu(t.name());
 		JMenu instanceMenu = new JMenu(t.name());
 		for (int i=0;i<t.instanceCount();i++) {
 		    ModuleInstance instance = (ModuleInstance)t.instance(i);
 		    menuItemAll = new JMenuItem(instance.name());
 		    menuItemAll.addActionListener(listener);
 		    menuItemAll.setActionCommand(t.name());
 		    menuItem = new JMenuItem(instance.name());
 		    menuItem.addActionListener(listener);
 		    menuItem.setActionCommand(t.name());
 		    
 		    for (int j=0;j<container.entryCount();j++) {
 			Reference reference = container.entry(j);
 			if (instance.isReferencedBy(reference))
 			    menuItemAll.setEnabled(false);
 			    menuItem.setEnabled(false);
 		    }
 		    instanceMenuAll.add(menuItemAll);
 		    instanceMenu.add(menuItem);
 		}
 		instanceMenuAll.addSeparator();
 		instanceMenu.addSeparator();
 		menuItemAll = new JMenuItem("New Instance");
 		menuItemAll.addActionListener(listener);
 		menuItemAll.setActionCommand(t.name());
 		instanceMenuAll.add(menuItemAll);
 		menuItem = new JMenuItem("New Instance");
 		menuItem.addActionListener(listener);
 		menuItem.setActionCommand(t.name());
 		instanceMenu.add(menuItem);
 		moduleTypeAllMenu.add(instanceMenuAll);
 		moduleTypeAndLetterMenu.add(instanceMenu);
 	    }
 	    else {
 		menuItemAll = new JMenuItem(t.name());
 		menuItem = new JMenuItem(t.name());
 		menuItemAll.setActionCommand("");
 		menuItem.setActionCommand("");
 		menuItemAll.addActionListener(listener);
 		menuItem.addActionListener(listener);
 		moduleTypeAllMenu.add(menuItemAll);
 		moduleTypeAndLetterMenu.add(menuItem);
 	    }
 	}
 	return addModuleMenu;
     }
 
     
     /**create 'Add Path' submenu */
     private JMenu createAddPathMenu(Object node)
     {
 	JMenuItem menuItem;
 	JMenu     addPathMenu = new ScrollableMenu("Add Path");
 	
 	ArrayList<Path> forbiddenPaths = new ArrayList<Path>();
 	
 	if (node instanceof Path) {
 	    Path path = (Path)node;
 	    forbiddenPaths.add(path);
 	    menuItem = new JMenuItem("New Path");
 	    menuItem.addActionListener(pathListener);
 	    menuItem.setActionCommand("NEWPATH");
 	    addPathMenu.add(menuItem);
 	    addPathMenu.addSeparator();
 	}
 	else if (node instanceof Reference) {
 	    Reference reference  = (Reference)node;
 	    Path      parentPath = (Path)reference.container();
 	    forbiddenPaths.add(parentPath);
 	    for (int i=0;i<parentPath.entryCount();i++) {
 		Reference r = parentPath.entry(i);
 		if (r instanceof PathReference) {
 		    PathReference pathreference = (PathReference)r;
 		    Path          path          = (Path)reference.parent();
 		    forbiddenPaths.add(path);
 		}
 	    }
 	}
 	Configuration config = (Configuration)treeModel.getRoot();
 	for (int i=0;i<config.pathCount();i++) {
 	    Path path = config.path(i);
 	    menuItem = new JMenuItem(path.name());
 	    menuItem.addActionListener(pathListener);
 	    menuItem.setActionCommand("PATHREF");
 	    if (forbiddenPaths.contains(path)) menuItem.setEnabled(false);
 	    addPathMenu.add(menuItem);
 	}
 	return addPathMenu;
     }
     
     /** create 'Add Sequence' Menu */
     private JMenu createAddSequenceMenu(ReferenceContainer pathOrSequence,
 					ActionListener     listener)
     {
 	JMenu     addSequenceMenu = new ScrollableMenu("Add Sequence");
 	JMenuItem menuItem;
 	ArrayList<Sequence> forbiddenSequences = new ArrayList<Sequence>();
 	
 	if (pathOrSequence instanceof Sequence) {
 	    Sequence sequence = (Sequence)pathOrSequence;
 	    forbiddenSequences.add(sequence);
 	    menuItem = new JMenuItem("New Sequence");
 	    menuItem.addActionListener(listener);
 	    menuItem.setActionCommand("NEWSEQ");
 	    addSequenceMenu.add(menuItem);
 	    addSequenceMenu.addSeparator();
 	}
 	
 	for (int i=0;i<pathOrSequence.entryCount();i++) {
 	    Reference reference = pathOrSequence.entry(i);
 	    if (reference instanceof SequenceReference) {
 		SequenceReference seqreference = (SequenceReference)reference;
 		Sequence          sequence     = (Sequence)seqreference.parent();
 		forbiddenSequences.add(sequence);
 	    }
 	}
 	
 	Configuration config = (Configuration)treeModel.getRoot();
 	for (int i=0;i<config.sequenceCount();i++) {
 	    Sequence sequence = config.sequence(i);
 	    menuItem = new JMenuItem(sequence.name());
 	    menuItem.addActionListener(listener);
 	    menuItem.setActionCommand("SEQREF");
 	    if (forbiddenSequences.contains(sequence)) menuItem.setEnabled(false);
 	    addSequenceMenu.add(menuItem); 
 	}
 	return addSequenceMenu;
     }
 
 
     /** TreeModelListener: treeNodesChanged() */
     public void treeNodesChanged(TreeModelEvent e)
     {
 	TreePath      treePath      = e.getTreePath(); if (treePath==null) return;
 	int           depth         = treePath.getPathCount(); if (depth<2) return;
 	int           index         = e.getChildIndices()[0];
 	Object        child         = e.getChildren()[0];
 	Object        parent        = treePath.getLastPathComponent();
 	TreePath      childTreePath = treePath.pathByAddingChild(child);
 	Configuration config        = (Configuration)treeModel.getRoot();
 	
 	if (config==null) return;
 	
 	boolean valid = false;
 	
 	if (child instanceof Referencable) {
 	    Referencable referencable = (Referencable)child;
 	    String       name = referencable.name();
 	    if (!name.equals("<ENTER UNIQUE NAME>")) valid = true;
 	}
 	if (child instanceof Reference) {
 	    Reference    reference    = (Reference)child;
 	    Referencable referencable = reference.parent();
 	    String       name         = referencable.name();
 	    if (!name.equals("<ENTER UNIQUE NAME>")) valid = true;
 	}
 	else if (child instanceof Instance) {
 	    Instance instance = (Instance)child;
 	    String   name     = instance.name();
 	    if (!name.equals("<ENTER UNIQUE NAME>")) valid = true;
 	}
 	else {
 	    valid = true;
 	}
 	
 	if (!valid) {
 	    tree.setSelectionPath(childTreePath);
 	    ConfigurationTreeActions.editNodeName(tree);
 	}
     }
     
     /** TreeModelListener: treeNodesInserted() */
     public void treeNodesInserted(TreeModelEvent e) {}
     
     /** TreeModelListener: treeNodesRemoved() */
     public void treeNodesRemoved(TreeModelEvent e) {}
 
     /** TreeModelListener: treeStructureChanged() */
     public void treeStructureChanged(TreeModelEvent e) {}
     
 }
 
 
 /**
  * listen to actions from the 'PSets' popup menu
  */
 class PSetMenuListener implements ActionListener
 {
     /** reference to the tree to be manipulated */
     private JTree tree = null;
     
     /** reference to the parent frame */
     private JFrame frame = null;
     
     /** standard constructor */
     public PSetMenuListener(JTree tree,JFrame frame)
     {
 	this.tree = tree;
 	this.frame = frame;
     }
     
     /** ActionListener interface */
     public void actionPerformed(ActionEvent e)
     {
 	JMenuItem source = (JMenuItem)(e.getSource());
 	String    cmd    = source.getText();
 	
 	if (cmd.equals("Remove PSet")) {
 	    PSetParameter pset =
 		(PSetParameter)tree.getSelectionPath().getLastPathComponent();
 	    ConfigurationTreeActions.removePSet(tree,pset);
 	}
 	else if (cmd.equals("Add PSet")) {
 	    AddParameterDialog dlg = new AddParameterDialog(frame,true);
 	    dlg.addParameterSet();
 	    dlg.pack();
 	    dlg.setLocationRelativeTo(frame);
 	    dlg.setVisible(true);
 	    if (dlg.validChoice()) {
 		PSetParameter pset =
 		    (PSetParameter)ParameterFactory.create(dlg.type(),
 							   dlg.name(),
 							   dlg.valueAsString(),
 							   dlg.isTracked(),
 							   false);
 		ConfigurationTreeActions.insertPSet(tree,pset);
 	    }
 	}
     }
     
 }
 
 
 /**
  * listen to actions from the 'EDSource' popup menu
  */
 class EDSourceMenuListener implements ActionListener
 {
     /** reference to the tree to be manipulated */
     private JTree tree = null;
     
     /** standard constructor */
     public EDSourceMenuListener(JTree tree) { this.tree = tree; }
     
     /** ActionListener interface */
     public void actionPerformed(ActionEvent e)
     {
 	JMenuItem source   = (JMenuItem)(e.getSource());
 	String    cmd      = source.getText();
 	TreePath  treePath = tree.getSelectionPath();
 	Object    node     = treePath.getLastPathComponent();
 	
 	if (cmd.equals("Remove EDSource")) {
 	    EDSourceInstance edsource = (EDSourceInstance)node;
 	    ConfigurationTreeActions.removeEDSource(tree,edsource);
 	}
 	else { // cmd = template name of new EDSource !
 	    ConfigurationTreeActions.insertEDSource(tree,cmd);
 	}
     }
     
 }
 
 
 /**
  * listen to actions from the 'ESSources' popup menu
  */
 class ESSourceMenuListener implements ActionListener
 {
     /** reference to the tree to be manipulated */
     private JTree tree = null;
 
     /** standard constructor */
     public ESSourceMenuListener(JTree tree) { this.tree = tree; }
     
     /** ActionListener interface */
     public void actionPerformed(ActionEvent e)
     {
 	JMenuItem source   = (JMenuItem)(e.getSource());
 	String    cmd      = source.getText();
 	TreePath  treePath = tree.getSelectionPath();
 	Object    node     = treePath.getLastPathComponent();
 	
 	if (cmd.equals("Remove ESSource")) {
 	    ESSourceInstance essource = (ESSourceInstance)node;
 	    ConfigurationTreeActions.removeESSource(tree,essource);
  	}
 	else {
 	    String templateName = 
 		(cmd.equals("New Instance")) ? source.getActionCommand() : cmd;
 	    ConfigurationTreeActions.insertESSource(tree,templateName);
 	}
     }
 }
 
 
 /**
  * listen to actions from the 'ESModules' popup menu
  */
 class ESModuleMenuListener implements ActionListener
 {
     /** reference to the tree to be manipulated */
     private JTree tree = null;
 
     /** standard constructor */
     public ESModuleMenuListener(JTree tree) { this.tree = tree; }
     
     /** ActionListener interface */
     public void actionPerformed(ActionEvent e)
     {
 	JMenuItem source   = (JMenuItem)(e.getSource());
 	String    cmd      = source.getText();
 	TreePath  treePath = tree.getSelectionPath();
 	Object    node     = treePath.getLastPathComponent();
 	
 	if (cmd.equals("Remove ESModule")) {
 	    ESModuleInstance esmodule = (ESModuleInstance)node;
 	    ConfigurationTreeActions.removeESModule(tree,esmodule);
  	}
 	else {
 	    String templateName = (cmd.equals("New Instance")) ?
 		source.getActionCommand() : cmd;
 	    ConfigurationTreeActions.insertESModule(tree,templateName);
 	}
     }
 }
 
 
 /**
  * listen to actions from the 'Services' popup menu
  */
 class ServiceMenuListener implements ActionListener
 {
     /** reference to the tree to be manipulated */
     private JTree tree = null;
     
     /** standard constructor */
     public ServiceMenuListener(JTree tree) { this.tree = tree; }
     
     /** ActionListener interface */
     public void actionPerformed(ActionEvent e)
     {
 	JMenuItem source   = (JMenuItem)(e.getSource());
 	String    cmd      = source.getText();
 	TreePath  treePath = tree.getSelectionPath();
 	Object    node     = treePath.getLastPathComponent();
 	
 	if (cmd.equals("Remove Service")) {
 	    ServiceInstance service = (ServiceInstance)node;	
 	    ConfigurationTreeActions.removeService(tree,service);
 	}
 	else {
 	    String templateName = cmd;
 	    ConfigurationTreeActions.insertService(tree,templateName);
 	}
     }
     
 }
 
 
 /**
  * listen to actions from the 'Paths' popup menu
  */
 class PathMenuListener implements ActionListener
 {
     /** reference to the tree to be manipulated */
     private JTree tree = null;
 
     /** standard constructor */
     public PathMenuListener(JTree tree) { this.tree = tree; }
     
     /** ActionListener interface */
     public void actionPerformed(ActionEvent e)
     {
 	JMenuItem source   = (JMenuItem)(e.getSource());
 	String    cmd      = source.getText();
 	String    action   = source.getActionCommand();
 	TreePath  treePath = tree.getSelectionPath();
 	Object    node     = treePath.getLastPathComponent();
 	
 	if (action.equals("NEWPATH")) {
 	    ConfigurationTreeActions.insertPath(tree);
 	}
 	else if (action.equals("PATHREF")) {
 	    ConfigurationTreeActions.insertReference(tree,"Path",cmd);
 	}
 	else if (action.equals("SEQREF")) {
 	    ConfigurationTreeActions.insertReference(tree,"Sequence",cmd);
 	}
 	else if (cmd.equals("Rename Path")) {
 	    ConfigurationTreeActions.editNodeName(tree);
 	}
 	else if (cmd.equals("Remove Path")) {
 	    if (node instanceof Path)
 		ConfigurationTreeActions.removeReferenceContainer(tree);
 	    else if (node instanceof PathReference)
 		ConfigurationTreeActions.removeReference(tree);
 	}
 	else if (cmd.equals("Remove Module")) {
 	    ConfigurationTreeActions.removeReference(tree);
 	}
 	else if (cmd.equals("Remove Sequence")) {
 	    ConfigurationTreeActions.removeReference(tree);
 	}
  	// add a module(-reference) to the currently selected path
 	else {
 	    String name = cmd;
 	    if (action.length()>0) {
 		if (cmd.equals("New Instance")) name = action;
 		else name = action + ":" + cmd;
 	    }
 	    ConfigurationTreeActions.insertReference(tree,"Module",name);
 	}
     }
 
 }
 
 
 /**
  * listen to actions from the 'Sequences' popup menu
 x */
 class SequenceMenuListener implements ActionListener
 {
     /** reference to the tree to be manipulated */
     private JTree tree = null;
 
     /** standard constructor */
     public SequenceMenuListener(JTree tree) { this.tree = tree; }
     
     /** ActionListener interface */
     public void actionPerformed(ActionEvent e)
     {
 	JMenuItem source   = (JMenuItem)(e.getSource());
 	String    cmd      = source.getText();
 	String    action   = source.getActionCommand();
 	TreePath  treePath = tree.getSelectionPath();
 	Object    node     = treePath.getLastPathComponent();
 	
 	if (action.equals("NEWSEQ")) {
 	    ConfigurationTreeActions.insertSequence(tree);
 	}
 	else if (action.equals("SEQREF")) {
 	    ConfigurationTreeActions.insertReference(tree,"Sequence",cmd);
 	}
 	else if (cmd.equals("Add Sequence")) {
 	    ConfigurationTreeActions.insertSequence(tree);
 	}
 	else if (cmd.equals("Rename Sequence")) {
 	    ConfigurationTreeActions.editNodeName(tree);
 	}
 	else if (cmd.equals("Remove Sequence")) {
 	    if (node instanceof Sequence)
 		ConfigurationTreeActions.removeReferenceContainer(tree);
 	    else if (node instanceof SequenceReference)
 		ConfigurationTreeActions.removeReference(tree);
  	}
 	else if (cmd.equals("Remove Module")) {
 	    ConfigurationTreeActions.removeReference(tree);
 	}
 	// add a module to the selected sequence
 	else {
 	    String name = cmd;
 	    if (action.length()>0) {
 		if (cmd.equals("New Instance")) name = action;
 		else name = action + ":" + cmd;
 	    }
 	    ConfigurationTreeActions.insertReference(tree,"Module",name);
 	}
     }
 
 }
 
 
 /**
  * listen to actions from the 'Modules' popup menu
  */
 class ModuleMenuListener implements ActionListener
 {
     /** reference to the tree to be manipulated */
     private JTree tree = null;
     
     /** the application frame */
     private JFrame frame = null;
     
     /** standard constructor */
     public ModuleMenuListener(JTree tree) { this.tree = tree; }
     
     /** ActionListener interface */
     public void actionPerformed(ActionEvent e)
     {
 	JMenuItem source   = (JMenuItem)(e.getSource());
 	String    cmd      = source.getText();
 	
 	if (cmd.equals("Rename Module")) {
 	    ConfigurationTreeActions.editNodeName(tree);
  	}
     }
 }
 
 
