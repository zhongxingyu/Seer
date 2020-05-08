 /*
  * This source file is part of CaesarJ 
  * For the latest info, see http://caesarj.org/
  * 
  * Copyright  2003-2005 
  * Darmstadt University of Technology, Software Technology Group
  * Also see acknowledgements in readme.txt
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  * 
 * $Id: CaesarJContentOutlinePage.java,v 1.5 2005-04-28 12:40:52 meffert Exp $
  */
 
 package org.caesarj.ui.editor;
 
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 import org.aspectj.asm.IProgramElement;
 import org.aspectj.asm.internal.ProgramElement;
 import org.aspectj.bridge.ISourceLocation;
 import org.caesarj.compiler.asm.CaesarProgramElement;
 import org.caesarj.ui.CaesarPlugin;
 import org.caesarj.ui.editor.model.LinkNode;
 import org.caesarj.ui.util.ProjectProperties;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.viewers.AbstractTreeViewer;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPathEditorInput;
 import org.eclipse.ui.ide.IDE;
 import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
 
 /**
  * Content Outline Page for Caesar Compilation Unit
  * 
  * 
  * @author Ivica Aracic <ivica.aracic@bytelords.de>
  * @author Thiago Tonelli Bartolomei <bart@macacos.org>
  */
 
 public class CaesarJContentOutlinePage extends ContentOutlinePage {
 
 	/**
 	 * TODO COMMENT!!
 	 */
 	public static final Point BIG_SIZE = new Point(22, 16);
 
 	/**
 	 * Contains the node categories that are diplayed in the outline 
 	 */
 	protected static HashMap categoryMap = categoryMap = new HashMap();
 	
 	/**
 	 * Loads the node categories
 	 */
 	static {
 		categoryMap.put(ProgramElement.Kind.PACKAGE, new Integer(0));
 		categoryMap.put(ProgramElement.Kind.FILE_JAVA, new Integer(1));
 		categoryMap.put(ProgramElement.Kind.ASPECT, new Integer(5));
 		categoryMap.put(ProgramElement.Kind.INTERFACE, new Integer(6));
 		categoryMap.put(ProgramElement.Kind.CLASS, new Integer(7));
 		categoryMap.put(ProgramElement.Kind.FIELD, new Integer(10));
 		categoryMap.put(ProgramElement.Kind.CONSTRUCTOR, new Integer(11));
 		categoryMap.put(ProgramElement.Kind.METHOD, new Integer(12));
 		categoryMap.put(ProgramElement.Kind.ADVICE, new Integer(13));
 		categoryMap.put(ProgramElement.Kind.CODE, new Integer(14));
 	}
 	
 	/**
 	 * Log4j logger for the class
 	 */
 	static Logger logger = Logger.getLogger(CaesarJContentOutlinePage.class);
 	
 	/**
 	 * Keep track of all Instances, so they can be updated when needed.
 	 * The keys for the hastable are project objects and the values are lists
 	 * of instances for each project.
 	 * When a Project is compiled, only the instance in the list for a project
 	 * are updated.
 	 */
 	private static Hashtable instances = new Hashtable();
 
 	/**
 	 * The CaesarEditor related to this outline page
 	 */
 	protected CaesarEditor caesarEditor;
 	
 	/**
 	 * The project object related to the page the editor is using
 	 */
 	protected IProject project;
 	
 	/**
 	 * The content provider for the tree
 	 */
 	protected CaesarOutlineViewContentProvider contentProvider;
 	
 	/**
 	 * Creates a new ContentOutlinePage using the given caesarEditor. Puts this
 	 * new page in the list of instances
 	 * 
 	 * @param caesarEditorArg
 	 *            the caesareditor of this new outline page
 	 */
 	public CaesarJContentOutlinePage(CaesarEditor caesarEditorArg, IProject project) {
 		super();
 		this.caesarEditor = caesarEditorArg;
 		this.project = project;
 		// Check if the project already has a instance list. If not, create a new
 		List projectInstances = (List) instances.get(project);
 		if (projectInstances == null)
 			projectInstances = new Vector();
 		
 		// Put this instance in the project list and put the list in the hash
 		projectInstances.add(this);
 		CaesarJContentOutlinePage.instances.put(project, projectInstances);
 	}
 
 	/**
 	 * Creates the JFace control that displays the content
 	 */
 	public void createControl(Composite parent) {
 		super.createControl(parent);
 		
 		contentProvider = new CaesarOutlineViewContentProvider();
 		
 		// Creates the helper classes and associates them to the tree viewer
 		TreeViewer viewer = getTreeViewer();
 		//viewer.setSorter(new CaesarOutlineViewLexicalSorter());
 		viewer.setSorter(new CaesarOutlineViewLineSorter());
 		viewer.setContentProvider(contentProvider);
 		viewer.setLabelProvider(new CaesarOutlineViewLabelProvider());
 		viewer.addSelectionChangedListener(this);
 
 		// Call update to create the initial state
 		ProjectProperties properties = ProjectProperties.create(project);
 		if (properties.getAsmManager() != null) {
 			update(ProjectProperties.create(project));
 		}
 	}
 
 	/**
 	 * This method is called when the user select a node in the content tree.
 	 * It searches the location of this node and opens it in the editor.
 	 * 
 	 * @see org.eclipse.jface.viewers.ISelectionChangedListener 
 	 */
 	public void selectionChanged(SelectionChangedEvent event) {
 
 		super.selectionChanged(event);
 
 		ISelection selection = event.getSelection();
 		if (selection.isEmpty()) {
 		} else {
 			Object item = ((IStructuredSelection) selection).getFirstElement();
 
 			if (item instanceof LinkNode) {
 			    if (((LinkNode) item).getType() == LinkNode.LINK_NODE_RELATIONSHIP) {
 			        return;
 			    }
 				item = ((LinkNode) item).getTargetElement();
 			}
 
 			IProgramElement selectedNode = (IProgramElement) item;
 			ISourceLocation sourceLocation = selectedNode.getSourceLocation();
 
 			if (sourceLocation != null) {
 				try {
 
 					IWorkspaceRoot root = ResourcesPlugin.getWorkspace()
 							.getRoot();
 
 					IPath path = new Path(sourceLocation.getSourceFile()
 							.getAbsolutePath());
 					IResource resource = root.getFileForLocation(path);
 					IMarker marker;
 
 					if (resource != null) {
 						marker = resource.createMarker(IMarker.MARKER);
 						marker.setAttribute(IMarker.LINE_NUMBER, sourceLocation
 								.getLine());
 						marker.setAttribute(IMarker.CHAR_START, sourceLocation
 								.getColumn());
 						IDE.openEditor(CaesarPlugin.getDefault().getWorkbench()
 								.getActiveWorkbenchWindow().getActivePage(),
 								marker);
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 
 			}
 		}
 	}
 
 
 
	//Iterates through the StructureModel and return the sourcefile node for 
	// the active editor input.
 	protected IProgramElement getInput(IProgramElement node) {
		String filename = null;
		IEditorInput editorInput = this.caesarEditor.getEditorInput();
		if(editorInput instanceof IPathEditorInput){
			filename = ((IPathEditorInput)editorInput).getPath().toFile().getAbsolutePath();
		}
 		if (node == null) {
 			return null;
 		}
		if(filename == null){
			return null;
		}
 		IProgramElement r = null;
		//if (node.getName().equals(this.caesarEditor.getEditorInput().getName())) {
		if (node.getKind().isSourceFile()
				&& node.getSourceLocation().getSourceFile().getAbsolutePath().equals(filename)) {
 			r = node;
 		} else {
 		    IProgramElement res = null;
 			for (Iterator it = node.getChildren().iterator(); it.hasNext()
 					&& res == null;) {
 				res = getInput((IProgramElement) it.next());
 			}
 			r = res;
 		}
 		return r;
 	}
 
 	/**
 	 * Updates the outline page.
 	 */
 	public void update(ProjectProperties properties) {
 	    
 	    contentProvider.setProjectProperties(properties);
 	    IProgramElement input = getInput(properties.getAsmManager().getHierarchy().getRoot());
 		
 		TreeViewer viewer = getTreeViewer();
 		if (viewer != null && input != null) {
 			Control control = viewer.getControl();
 			if (control != null && !control.isDisposed()) {
 				control.setRedraw(false);
 				viewer.setInput(input);
 				viewer.expandAll();
 				if (input instanceof CaesarProgramElement) {
 					// find and collapses the import node:
 					Iterator it = ((CaesarProgramElement) input)
 							.getChildren().iterator();
 					while (it.hasNext()) {
 						Object next = it.next();
 						if (next instanceof CaesarProgramElement) {
 							if (((CaesarProgramElement) next)
 									.getCaesarKind() == CaesarProgramElement.Kind.IMPORTS) {
 								viewer.collapseToLevel(next,
 										AbstractTreeViewer.ALL_LEVELS);
 							}
 						}
 					}
 				}
 
 				control.setRedraw(true);
 			}
 		}
 
 	}
 
 	/**
 	 * This method is called when all the instances for a project must be updated. It iterates
 	 * over the list and call their update method.
 	 */
 	public static void updateAll(ProjectProperties properties) {
 		List list = (List) instances.get(properties.getProject());
 		if (list != null) {
 			for (Iterator it = list.iterator(); it.hasNext();) {
 				((CaesarJContentOutlinePage) it.next()).update(properties);
 			}
 		}
 	}
 
 	/**
 	 * Removes the anInstance from the instance list.
 	 * 
 	 * @param anInstance a page instance on the list
 	 * @return true if the instance existed and was removed. False otherwise.
 	 */
 	public static boolean removeInstance(CaesarJContentOutlinePage anInstance) {
 		List list = (List) instances.get(anInstance.project);
 		if (list != null) {
 			// Remove the instance and keep track of the result
 			boolean r = list.remove(anInstance);
 			// If the list is empty, remove it from the hash
 			if (list.size() == 0) {
 				instances.remove(list);
 			}
 			return r;
 		}
 		return false;
 	}
 	
 	/**
 	 * 
 	 * The sorter for the tree view.
 	 * 
 	 * This class sorts the nodes according to the line they appear in the 
 	 * code. 
 	 *
 	 * @author Thiago Tonelli Bartolomei <bart@macacos.org>
 	 *
 	 */
 	public class CaesarOutlineViewLineSorter extends ViewerSorter {
 	    
 	    /**
 	     * The comparator used to sort
 	     */
 	    private Comparator comparator;
 	    
 	    /**
 	     * Constructor, creates a comparator
 	     */
 	    public CaesarOutlineViewLineSorter() { 
 	        
 	    	comparator = new Comparator() {
 			    public int compare(Object o1, Object o2) {
 			        if (o1 instanceof IProgramElement && o2 instanceof IProgramElement) {
 			            int l1 = ((IProgramElement) o1).getSourceLocation().getLine();
 			        	int l2 = ((IProgramElement) o2).getSourceLocation().getLine();
 			        	return l1 - l2;
 			        }
 			        if (o1 instanceof IProgramElement) {
 			        	return 1;
 			        }
 			        if (o2 instanceof IProgramElement) {
 			            return -1;
 			        }
 			        return 0;
 			    }
 	    	};
 		}
 		
 	    /**
 	     * Uses the comparator to sort the elements
 	     */
 		public void sort(final Viewer viewer, Object[] elements) {
 		    if (elements.length <= 1) {
 		        return;
 		    }
 			Arrays.sort(elements, comparator); 
 		}
 	}
 }
