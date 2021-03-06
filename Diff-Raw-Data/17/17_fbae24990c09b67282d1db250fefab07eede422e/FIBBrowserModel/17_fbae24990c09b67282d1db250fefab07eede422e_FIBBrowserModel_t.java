 /*
  * (c) Copyright 2010-2011 AgileBirds
  *
  * This file is part of OpenFlexo.
  *
  * OpenFlexo is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * OpenFlexo is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package org.openflexo.fib.view.widget.browser;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Set;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreePath;
 
 import org.openflexo.antar.binding.BindingEvaluationContext;
 import org.openflexo.antar.binding.BindingVariable;
 import org.openflexo.antar.binding.DataBinding;
 import org.openflexo.antar.binding.DependingObjects;
 import org.openflexo.antar.binding.DependingObjects.HasDependencyBinding;
 import org.openflexo.antar.binding.TargetObject;
 import org.openflexo.antar.expr.NullReferenceException;
 import org.openflexo.antar.expr.TypeMismatchException;
 import org.openflexo.fib.controller.FIBController;
 import org.openflexo.fib.model.FIBBrowser;
 import org.openflexo.fib.model.FIBBrowserElement;
 import org.openflexo.fib.model.FIBBrowserElement.FIBBrowserElementChildren;
 import org.openflexo.fib.view.widget.FIBBrowserWidget;
 import org.openflexo.toolbox.ToolBox;
 
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.Multimap;
 import com.google.common.collect.Multimaps;
 
 public class FIBBrowserModel extends DefaultTreeModel implements TreeModel {
 
 	private static final Logger logger = Logger.getLogger(FIBBrowserModel.class.getPackage().getName());
 
 	private Map<FIBBrowserElement, FIBBrowserElementType> _elementTypes;
 	private FIBBrowser _fibBrowser;
 	private final Multimap<Object, BrowserCell> contents;
 
 	private FIBBrowserWidget widget;
 
 	/**
 	 * Stores controls: key is the JButton and value the PropertyListActionListener
 	 */
 	// private Hashtable<JButton,PropertyListActionListener> _controls;
 
 	public FIBBrowserModel(FIBBrowser fibBrowser, FIBBrowserWidget widget, FIBController controller) {
 		super(null);
 		contents = Multimaps.synchronizedMultimap(ArrayListMultimap.<Object, BrowserCell> create());
 		_fibBrowser = fibBrowser;
 		this.widget = widget;
 		_elementTypes = new Hashtable<FIBBrowserElement, FIBBrowserElementType>();
 		for (FIBBrowserElement browserElement : fibBrowser.getElements()) {
 			addToElementTypes(browserElement, buildBrowserElementType(browserElement, controller));
 		}
 
 		//
 	}
 
 	public void delete() {
 		for (FIBBrowserElement c : _elementTypes.keySet()) {
 			_elementTypes.get(c).delete();
 		}
 		_elementTypes.clear();
 
 		_elementTypes = null;
 		_fibBrowser = null;
 	}
 
 	public FIBBrowserElement elementForClass(Class aClass) {
 		return _fibBrowser.elementForClass(aClass);
 	}
 
 	public FIBBrowserElementType elementTypeForClass(Class aClass) {
 		FIBBrowserElement element = elementForClass(aClass);
 		if (element != null) {
 			return _elementTypes.get(element);
 		} else {
 			logger.warning("Could not find element for class " + aClass);
 			return null;
 		}
 	}
 
 	public FIBBrowser getBrowser() {
 		return _fibBrowser;
 	}
 
 	/**
 	 * @param root
 	 * @return flag indicating if change was required
 	 */
 	public boolean updateRootObject(Object root) {
 		if (root == null) {
 			return false;
 		}
 
 		// nbOfBrowserCells = 0;
 
 		BrowserCell rootCell = retrieveBrowserCell(root, null);
 		if (getRoot() != rootCell) {
 			if (getRoot() != null) {
 				((BrowserCell) getRoot()).delete();
 			}
 			logger.fine("updateRootObject() with " + root + " rootCell=" + rootCell);
 			setRoot(rootCell);
 
 			return true;
 		}
 		return false;
 	}
 
 	public void fireTreeRestructured() {
 		if (getRoot() instanceof BrowserCell) {
 			((BrowserCell) getRoot()).update(true);
 			// nodeStructureChanged((BrowserCell)getRoot());
 		}
 	}
 
 	public void addToElementTypes(FIBBrowserElement element, FIBBrowserElementType elementType) {
 		_elementTypes.put(element, elementType);
 		elementType.setModel(this);
 	}
 
 	public void removeFromElementTypes(FIBBrowserElement element, FIBBrowserElementType elementType) {
 		_elementTypes.remove(element);
 		elementType.setModel(null);
 	}
 
 	public Map<FIBBrowserElement, FIBBrowserElementType> getElementTypes() {
 		return _elementTypes;
 	}
 
 	private FIBBrowserElementType buildBrowserElementType(FIBBrowserElement browserElement, FIBController controller) {
 		return new FIBBrowserElementType(browserElement, this, controller);
 	}
 
 	public TreePath[] getPaths(Object o) {
 		Collection<BrowserCell> cells = contents.get(o);
 		if (cells == null) {
 			return new TreePath[0];
 		}
 		TreePath[] paths = new TreePath[cells.size()];
 		int i = 0;
 		for (BrowserCell cell : cells) {
 			paths[i++] = getTreePath(cell);
 		}
 		return paths;
 	}
 
 	private TreePath getTreePath(BrowserCell cell) {
 		Object[] path = getPathToRoot(cell);
 		TreePath returned = new TreePath(path);
 		return returned;
 	}
 
 	@Override
 	public void valueForPathChanged(TreePath path, Object newValue) {
 		BrowserCell cell = (BrowserCell) path.getLastPathComponent();
 		if (cell.getBrowserElementType().isLabelEditable() && newValue instanceof String) {
 			cell.getBrowserElementType().setEditableLabelFor(cell.getRepresentedObject(), (String) newValue);
 		}
 	}
 
 	public Multimap<Object, BrowserCell> getContents() {
 		return contents;
 	}
 
 	/**
 	 * Obtain an iteration on all values which may be contained in this browser, by explicitely deeply-exploring all the tree.<br>
 	 * Warning ! This method is dangerous for the perfs, and should never be called on a big "model"
 	 * 
 	 * @return
 	 */
 	public Iterator<Object> recursivelyExploreModelToRetrieveContents() {
 		// We load all when not already up-t-date
 		if (!exhaustiveContentsIsUpToDate) {
 			computedExhaustiveContents.clear();
 			if (getRoot() instanceof BrowserCell) {
 				logger.info("!!!!! called recursivelyExploreModelToRetrieveContents() !!!!");
 				((BrowserCell) getRoot()).update(true);
 				exhaustiveContentsIsUpToDate = true;
 			}
 		}
 		return contents.keys().iterator();
 	}
 
 	/**
 	 * This set is used during exploration of all exhaustive contents, in order not no enter in an infinite loop
 	 */
 	private Set<Object> computedExhaustiveContents = new HashSet<Object>();
 
 	/**
 	 * Flag indicating is exhaustive contents (obtained after deep-browsing) is up-to-date
 	 */
 	boolean exhaustiveContentsIsUpToDate = false;
 
 	private BrowserCell retrieveBrowserCell(Object representedObject, BrowserCell parent) {
 		/*System.out.println("retrieveBrowserCell for " + representedObject + " parent="
 				+ (parent != null ? parent.getRepresentedObject() : "null"));*/
 		ArrayList<BrowserCell> cells = new ArrayList<FIBBrowserModel.BrowserCell>(contents.get(representedObject));
 		// Collection<BrowserCell> cells = contents.get(representedObject);
 		if (cells != null) {
 			for (BrowserCell cell : cells) {
 				if (cell.getParent() == parent) {
 					return cell;
 				}
 			}
 		}
 		BrowserCell returned = new BrowserCell(representedObject, parent);
 		contents.put(representedObject, returned);
 		// returned.update(false);
 		return returned;
 	}
 
 	private void removeBrowserCell(BrowserCell cell) {
 		contents.remove(cell.getRepresentedObject(), cell);
 	}
 
 	public boolean containsObject(Object representedObject) {
 		return contents.get(representedObject) != null;
 	}
 
 	public Collection<BrowserCell> getBrowserCell(Object representedObject) {
 		return contents.get(representedObject);
 	}
 
 	public class LoadingCell extends DefaultMutableTreeNode {
 		public LoadingCell() {
 			super("Loading...", false);
 		}
 	}
 
 	public class BrowserCell extends DefaultMutableTreeNode implements Observer, HasDependencyBinding {
 
 		private boolean loaded = false;
 
 		private FIBBrowserElementType browserElementType;
 		private boolean isDeleted = false;
 		private boolean isVisible = true;
 		private DependingObjects dependingObjects;
 
 		public BrowserCell(Object representedObject, BrowserCell father) {
 			setParent(father);
 			setAllowsChildren(true);
 			setUserObject(representedObject);
 			browserElementType = elementTypeForClass(representedObject.getClass());
 
			if (browserElementType != null) {
 
				final List<?> newChildrenObjects = browserElementType.getChildrenFor(getRepresentedObject());

				if (newChildrenObjects.size() > 0) {
					// System.out.println("For " + representedObject + " found " + newChildrenObjects.size() + " children: " +
					// newChildrenObjects);
					add(new LoadingCell());
				}
 
 				dependingObjects = new DependingObjects(this);
 				dependingObjects.refreshObserving(browserElementType);
 			}
 		}
 
 		@Override
 		public BrowserCell getParent() {
 			return (BrowserCell) super.getParent();
 		}
 
 		public Object getRepresentedObject() {
 			return getUserObject();
 		}
 
 		public void loadChildren(final DefaultTreeModel model, final PropertyChangeListener progressListener) {
 			if (loaded) {
 				return;
 			}
 
 			loaded = true;
 
 			update(false);
 
 		}
 
 		public boolean isLoaded() {
 			return loaded;
 		}
 
 		@Override
 		public List<DataBinding<?>> getDependencyBindings() {
 			return getBrowserElementType().getDependencyBindings(getRepresentedObject());
 		}
 
 		@Override
 		public List<TargetObject> getChainedBindings(DataBinding binding, final TargetObject object) {
 			for (FIBBrowserElementChildren child : browserElementType.getBrowserElement().getChildren()) {
 				if (binding.equals(child.getData()) && child.getCast().isSet() && binding.toString().endsWith(object.propertyName)) {
 					try {
 						final Object bindingValue = child.getData().getBindingValue(browserElementType);
 						List<?> list = ToolBox.getListFromIterable(bindingValue);
 						if (list != null) {
 							List<TargetObject> targetObjects = new ArrayList<TargetObject>();
 							for (final Object o : list) {
 								List<TargetObject> targetObjects2 = child.getCast().getTargetObjects(new BindingEvaluationContext() {
 
 									@Override
 									public Object getValue(BindingVariable variable) {
 										if (variable.getVariableName().equals("child")) {
 											return o;
 										} else {
 											return browserElementType.getValue(variable);
 										}
 									}
 								});
 								if (targetObjects2 != null) {
 									targetObjects.addAll(targetObjects2);
 								}
 							}
 							return targetObjects;
 						} else {
 							return child.getCast().getTargetObjects(new BindingEvaluationContext() {
 
 								@Override
 								public Object getValue(BindingVariable variable) {
 									if (variable.getVariableName().equals("child")) {
 										return bindingValue;
 									} else {
 										return browserElementType.getValue(variable);
 									}
 								}
 							});
 
 						}
 					} catch (TypeMismatchException e) {
 						continue;
 					} catch (NullReferenceException e) {
 						continue;
 					} catch (InvocationTargetException e) {
 						continue;
 					}
 
 				}
 			}
 			return null;
 		}
 
 		public void delete() {
 
 			logger.fine("Delete BrowserCell for " + getRepresentedObject());
 
 			if (children != null) {
 				for (Object c : children) {
 					if (c instanceof BrowserCell) {
 						((BrowserCell) c).delete();
 					}
 				}
 			}
 
 			if (dependingObjects != null) {
 				dependingObjects.stopObserving();
 			}
 
 			if (getRepresentedObject() != null) {
 				removeBrowserCell(this);
 			}
 
 			/*
 			 * GPO: Commented next line. We should check why we drop this representedObject from the selection
 			 * Why not also check if we are the current selected object?
 			 * By all means, we should find another way to do this than by doing it in the TreeModel. We could do that in FIBBrowserWidget.treeNodesRemoved. 
 			if (selection.contains(representedObject)) {
 				selection.remove(representedObject);
 			}
 			 */
 			setUserObject(null);
 			browserElementType = null;
 			setParent(null);
 			if (children != null) {
 				children.clear();
 			}
 			isDeleted = true;
 		}
 
 		public void update(boolean recursively) {
 
 			List<BrowserCell> cellsToForceUpdate = new ArrayList<BrowserCell>();
 
 			loaded = true;
 
 			// During exploration of all exhaustive contents, in order not no enter in an infinite loop
 			if (recursively) {
 				computedExhaustiveContents.add(getRepresentedObject());
 			}
 
 			// logger.info("**************** update() "+this);
 			if (browserElementType == null) {
 				logger.warning("No element type registered for " + getRepresentedObject());
 				return;
 			}
 
 			// Special case for cells that were declared as invisible
 			// When becomes visible, must tells to parent to update
 			if (!isVisible) {
 				if (browserElementType.isVisible(getRepresentedObject())) {
 					logger.fine("Cell " + this + " becomes visible");
 					getParent().update(recursively);
 				}
 			}
 
 			List<BrowserCell> oldChildren;
 			List<BrowserCell> removedChildren;
 			List<BrowserCell> newChildren = new ArrayList<BrowserCell>();
 
 			if (children == null) {
 				oldChildren = new ArrayList<BrowserCell>();
 				removedChildren = new ArrayList<BrowserCell>();
 			} else {
 				if (children.size() == 1 && children.firstElement() instanceof LoadingCell) {
 					removeAllChildren();
 				}
 				oldChildren = new ArrayList<BrowserCell>(children);
 				removedChildren = new ArrayList<BrowserCell>(children);
 			}
 
 			final List<?> newChildrenObjects = /*(isEnabled ?*/browserElementType.getChildrenFor(getRepresentedObject()) /*: new Vector())*/;
 			int index = 0;
 
 			for (Object o : newChildrenObjects) {
 
 				if (o != null && o != getRepresentedObject()) {
 					BrowserCell cell = retrieveBrowserCell(o, this);
 					FIBBrowserElementType childElementType = elementTypeForClass(o.getClass());
 					if (childElementType != null && childElementType.isVisible(o)) {
 						if (children != null && children.contains(cell)) {
 							// OK, child still here
 							removedChildren.remove(cell);
 							if (recursively) {
 								cell.update(true);
 							}
 							index = children.indexOf(cell) + 1;
 						} else {
 							newChildren.add(cell);
 							if (children == null) {
 								children = new Vector();
 							}
 							children.insertElementAt(cell, index);
 							index++;
 							// In order not to enter in possibly infinite loop, force update contents of this cell
 							// only if cell not loaded and if represented object was not already registered
 							if (recursively && !cell.isLoaded() && !computedExhaustiveContents.contains(cell.getRepresentedObject())) {
 								// Do it at the end
 								cellsToForceUpdate.add(cell);
 								// cell.update(true);
 							}
 						}
 					} else {
 						cell.isVisible = false;
 					}
 				}
 			}
 			for (BrowserCell c : removedChildren) {
 				if (children != null) {
 					children.remove(c);
 				}
 				c.delete();
 			}
 
 			boolean requireSorting = false;
 			if (children != null) {
 				for (int i = 0; i < children.size() - 1; i++) {
 					BrowserCell c1 = (BrowserCell) children.elementAt(i);
 					BrowserCell c2 = (BrowserCell) children.elementAt(i + 1);
 					if (newChildrenObjects.indexOf(c1.getRepresentedObject()) != newChildrenObjects.indexOf(c2.getRepresentedObject()) - 1) {
 						requireSorting = true;
 					}
 				}
 			}
 
 			if (requireSorting) {
 				if (logger.isLoggable(Level.FINE)) {
 					logger.fine("Detected sorting required");
 				}
 				// Sort children according to supplied list
 				Collections.sort(children, new Comparator<BrowserCell>() {
 					@Override
 					public int compare(BrowserCell o1, BrowserCell o2) {
 						return newChildrenObjects.indexOf(o1.getRepresentedObject())
 								- newChildrenObjects.indexOf(o2.getRepresentedObject());
 					}
 				});
 			}
 
 			// System.out.println("removedChildren ["+removedChildren.size()+"] "+removedChildren);
 			// System.out.println("newChildren ["+newChildren.size()+"] "+newChildren);
 			// System.out.println("children ["+children.size()+"] "+children);
 
 			boolean structureChanged = false;
 
 			if (removedChildren.size() > 0 || newChildren.size() > 0) {
 				structureChanged = true;
 				exhaustiveContentsIsUpToDate = false;
 				if (oldChildren.size() == 0) {
 					// Special case, i don't undertand why (SGU)
 					// OK, issue seems to be MacOS only but workaround works on all platforms.
 					// To observe the issue, load WKF module on a project that imports other projects
 					// Imported workflow tree is not correctly initiated after reload of project.
 					try {
 						nodeStructureChanged(this);
 					} catch (Exception e) {
 						// Might happen when a structural modification will call parent's nodeChanged()
 						// An Exception might be raised here
 						// We should investigate further, but since no real consequences are raised here, we just ignore exception
 						e.printStackTrace();
 						logger.warning("Unexpected " + e.getClass().getSimpleName()
 								+ " when refreshing browser, no severity but please investigate");
 					}
 				} else {
 					if (removedChildren.size() > 0) {
 						int[] childIndices = new int[removedChildren.size()];
 						Object[] removedChildrenObjects = new Object[removedChildren.size()];
 						for (int i = 0; i < removedChildren.size(); i++) {
 							childIndices[i] = oldChildren.indexOf(removedChildren.get(i));
 							removedChildrenObjects[i] = removedChildren.get(i);
 						}
 						nodesWereRemoved(this, childIndices, removedChildrenObjects);
 					}
 					if (newChildren.size() > 0) {
 						int[] childIndices = new int[newChildren.size()];
 						for (int i = 0; i < newChildren.size(); i++) {
 							childIndices[i] = children.indexOf(newChildren.get(i));
 						}
 						nodesWereInserted(this, childIndices);
 					}
 				}
 			}
 
 			try {
 				nodeChanged(this);
 			} catch (ArrayIndexOutOfBoundsException e) {
 				// Might happen when a structural modification will call parent's nodeChanged()
 				// An ArrayIndexOutOfBoundsException might be raised here
 				// We should investigate further, but since no real consequences are raised here, we just ignore exception
 				// e.printStackTrace();
 				logger.warning("Unexpected ArrayIndexOutOfBoundsException when refreshing browser, no severity but please investigate");
 				nodeStructureChanged(this);
 			} catch (NullPointerException e) {
 				// Might happen when a structural modification will call parent's nodeChanged()
 				// An NullPointerException might be raised here
 				// We should investigate further, but since no real consequences are raised here, we just ignore exception
 				// e.printStackTrace();
 				logger.warning("Unexpected NullPointerException when refreshing browser, no severity but please investigate");
 			}
 
 			if (requireSorting) {
 
 				/*Object wasSelected = widget.getSelectedObject();
 				if (logger.isLoggable(Level.FINE)) {
 					logger.fine("Will reselect " + wasSelected);
 				}*/
 
 				try {
 					nodeStructureChanged(this);
 				} catch (Exception e) {
 					// Might happen when a structural modification will call parent's nodeChanged()
 					// An Exception might be raised here
 					// We should investigate further, but since no real consequences are raised here, we just ignore exception
 					e.printStackTrace();
 					logger.warning("Unexpected " + e.getClass().getSimpleName()
 							+ " when refreshing browser, no severity but please investigate");
 				}
 
 				/*if (wasSelected != null) {
 					widget.resetSelection();
 					widget.addToSelection(wasSelected);
 				}*/
 
 			}
 
 			/*if (wasSelected) {
 				widget.addToSelection(representedObject);
 			}*/
 
 			dependingObjects.refreshObserving(browserElementType);
 
 			for (BrowserCell cell : cellsToForceUpdate) {
 				cell.update(true);
 			}
 		}
 
 		@Override
 		public void update(Observable o, Object arg) {
 			// logger.info("Object " + o + " received " + arg);
 			if (!isDeleted && o == getRepresentedObject()) {
 				update(false);
 			}
 		}
 
 		@Override
 		public void propertyChange(PropertyChangeEvent evt) {
 			// logger.info("Object " + representedObject + " received " + evt);
 			if (!isDeleted) {
 				// System.out.println("cell " + this + " propertyChanged " + evt.getPropertyName() + " for " + evt.getSource());
 				update(false);
 			}
 		}
 
 		public FIBBrowserElement getBrowserElement() {
 			return browserElementType.getBrowserElement();
 		}
 
 		public FIBBrowserElementType getBrowserElementType() {
 			return browserElementType;
 		}
 
 		public TreePath getTreePath() {
 			return new TreePath(getPathToRoot(this, 0));
 		}
 
 	}
 
 }
