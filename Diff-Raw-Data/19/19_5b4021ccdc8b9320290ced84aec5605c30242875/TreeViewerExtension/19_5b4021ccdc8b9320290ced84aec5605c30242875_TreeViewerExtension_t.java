 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.ui.trees;
 
 import java.lang.reflect.Constructor;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.eclipse.core.expressions.EvaluationContext;
 import org.eclipse.core.expressions.EvaluationResult;
 import org.eclipse.core.expressions.Expression;
 import org.eclipse.core.expressions.ExpressionConverter;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.SafeRunner;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.util.LocalSelectionTransfer;
 import org.eclipse.jface.util.SafeRunnable;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DragSourceListener;
 import org.eclipse.swt.dnd.DropTargetListener;
 import org.eclipse.swt.dnd.FileTransfer;
 import org.eclipse.swt.dnd.ImageTransfer;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.graphics.Image;
 import org.osgi.framework.Bundle;
 
 /**
  * The implementation of the tree viewer extension which is used to parse
  * the columns and the filters declared in this extension for a specified tree viewer.
  */
 public class TreeViewerExtension {
 	// The extension point id constant.
 	private static final String EXTENSION_POINT_ID = "org.eclipse.tcf.te.ui.viewers"; //$NON-NLS-1$
 	// The id of the tree viewer for which the extension is parsed.
 	private String viewerId;
 
 	/**
 	 * Create an instance and parse all tree viewer extensions to get the 
 	 * column descriptors and filter descriptors for the specified viewer.
 	 * 
 	 * @param viewerId The tree viewer's id.
 	 * @param viewer The tree viewer to parse the extension for.
 	 */
 	public TreeViewerExtension(String viewerId) {
 		this.viewerId = viewerId;
 	}
 
 	/**
 	 * Parse the viewer extensions and return the descriptor of the tree viewer.
 	 * 
 	 * @return The viewer descriptor.
 	 */
 	public ViewerDescriptor parseViewer() {
 		Assert.isNotNull(viewerId);
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENSION_POINT_ID);
 		IConfigurationElement[] configurations = extensionPoint.getConfigurationElements();
 		for (IConfigurationElement configuration : configurations) {
 			String name = configuration.getName();
 			if ("viewer".equals(name)) { //$NON-NLS-1$
 				String id = configuration.getAttribute("id"); //$NON-NLS-1$
 				if (viewerId.equals(id)) {
 					return createViewerDescriptor(configuration);
 				}
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Create a viewer descriptor from the given configuration element.
 	 * 
 	 * @param configuration The configuration element that defines the viewer.
 	 * @return The viewer descriptor.
 	 */
 	private ViewerDescriptor createViewerDescriptor(final IConfigurationElement configuration) {
 		final ViewerDescriptor descriptor = new ViewerDescriptor();
 		IConfigurationElement[] children = configuration.getChildren("creation"); //$NON-NLS-1$
 		if (children != null && children.length > 0) {
 			Assert.isTrue(children.length == 1);
 			descriptor.setStyleConfig(children[0]);
 		}
 		children = configuration.getChildren("dragSupport"); //$NON-NLS-1$
 		if (children != null && children.length > 0) {
 			Assert.isTrue(children.length == 1);
 			descriptor.setDragConfig(children[0]);
 		}
 		children = configuration.getChildren("dropSupport"); //$NON-NLS-1$
 		if (children != null && children.length > 0) {
 			Assert.isTrue(children.length == 1);
 			descriptor.setDropConfig(children[0]);
 		}
 		SafeRunner.run(new SafeRunnable() {
 			@Override
 			public void run() throws Exception {
 				ITreeContentProvider contentProvider = (ITreeContentProvider) configuration.createExecutableExtension("contentProvider"); //$NON-NLS-1$
 				descriptor.setContentProvider(contentProvider);
 			}
 		});
 		String value = configuration.getAttribute("persistent"); //$NON-NLS-1$
 		if (value != null) {
 			descriptor.setPersistent(Boolean.valueOf(value).booleanValue());
 		}
 		value = configuration.getAttribute("autoExpandLevel"); //$NON-NLS-1$
 		if (value != null) {
 			try {
 				int level = Integer.parseInt(value);
 				descriptor.setAutoExpandLevel(level);
 			}
 			catch (NumberFormatException nfe) {
 			}
 		}
 		value = configuration.getAttribute("menuId"); //$NON-NLS-1$
 		if (value != null) {
			descriptor.setContextMenuId(value);
 		}
 		value = configuration.getAttribute("doubleClickCommand"); //$NON-NLS-1$
 		if (value != null) {
 			descriptor.setDoubleClickCommand(value);
 		}
 		value = configuration.getAttribute("helpId"); //$NON-NLS-1$
 		if (value != null) {
 			descriptor.setHelpId(value);
 		}
 		return descriptor;
 	}
 
 	/**
 	 * Parse the column declarations of this extension point and return the 
 	 * column descriptors.
 	 * 
 	 * @param input The input used to initialize the columns.
 	 * @param viewer The viewer used to initialize the columns.
 	 * @return The column descriptors from this extension point.
 	 */
 	public ColumnDescriptor[] parseColumns(Object input, TreeViewer viewer) {
 		Assert.isNotNull(viewerId);
 		List<ColumnDescriptor> columns = Collections.synchronizedList(new ArrayList<ColumnDescriptor>());
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENSION_POINT_ID);
 		IConfigurationElement[] configurations = extensionPoint.getConfigurationElements();
 		for (IConfigurationElement configuration : configurations) {
 			String name = configuration.getName();
 			if ("columnContribution".equals(name)) { //$NON-NLS-1$
 				String aViewerId = configuration.getAttribute("viewerId"); //$NON-NLS-1$
 				if (viewerId.equals(aViewerId)) {
 					IConfigurationElement[] children = configuration.getChildren("column"); //$NON-NLS-1$
 					if (children != null && children.length > 0) {
 						for (IConfigurationElement child : children) {
 							createColumnDescriptor(input, columns, child, viewer);
 						}
 					}
 				}
 			}
 		}
 		return columns.toArray(new ColumnDescriptor[columns.size()]);
 	}
 
 	/**
 	 * Parse the viewer filter declarations of this extension point and return the 
 	 * filter descriptors.
 	 * 
 	 * @param input the new input
 	 * @return The column descriptors from this extension point.
 	 */
 	public FilterDescriptor[] parseFilters(Object input) {
 		Assert.isNotNull(viewerId);
 		List<FilterDescriptor> descriptors = Collections.synchronizedList(new ArrayList<FilterDescriptor>());
 		IExtensionRegistry registry = Platform.getExtensionRegistry();
 		IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENSION_POINT_ID);
 		IConfigurationElement[] configurations = extensionPoint.getConfigurationElements();
 		for (IConfigurationElement configuration : configurations) {
 			String name = configuration.getName();
 			if ("filterContribution".equals(name)) { //$NON-NLS-1$
 				String aViewerId = configuration.getAttribute("viewerId"); //$NON-NLS-1$
 				if (viewerId.equals(aViewerId)) {
 					IConfigurationElement[] children = configuration.getChildren("filter"); //$NON-NLS-1$
 					if (children != null && children.length > 0) {
 						for (IConfigurationElement child : children) {
 							createFilterDescriptor(input, descriptors, child);
 						}
 					}
 				}
 			}
 		}
 		return descriptors.toArray(new FilterDescriptor[descriptors.size()]);
 	}
 
 	/**
 	 * Create an filter descriptor from the specified configuration element and 
 	 * add it to the filter list.
 	 * 
 	 * @param input the input of the viewer to initialize the descriptors.
 	 * @param descriptors The filter list to add the created descriptor to.
 	 * @param configuration The extension configuration element to create the descriptor from.
 	 */
 	private void createFilterDescriptor(Object input, List<FilterDescriptor> descriptors, final IConfigurationElement configuration) {
 		if (isElementActivated(input, configuration)) {
 			String id = configuration.getAttribute("id"); //$NON-NLS-1$
 			Assert.isNotNull(id);
 			final FilterDescriptor descriptor = new FilterDescriptor();
 			descriptor.setId(id);
 			descriptors.add(descriptor);
 			SafeRunner.run(new SafeRunnable() {
 				@Override
 				public void run() throws Exception {
 					initFilter(descriptor, configuration);
 				}
 			});
 		}
 	}
 
 	/**
 	 * Initialize the filter descriptor from the specified configuration element.
 	 * 
 	 * @param descriptor The new descriptor to be initialized.
 	 * @param configuration The configuration element to initialize the filter.
 	 * @throws CoreException Thrown during parsing.
 	 */
 	void initFilter(FilterDescriptor descriptor, IConfigurationElement configuration) throws CoreException {
 		String attribute = configuration.getAttribute("name"); //$NON-NLS-1$
 		Assert.isNotNull(attribute);
 		descriptor.setName(attribute);
 		attribute = configuration.getAttribute("description"); //$NON-NLS-1$
 		if (attribute != null) {
 			descriptor.setDescription(attribute);
 		}
 		attribute = configuration.getAttribute("image"); //$NON-NLS-1$
 		if (attribute != null) {
 			String symbolicName = configuration.getContributor().getName();
 			URL resource = Platform.getBundle(symbolicName).getResource(attribute);
 			Image image = ImageDescriptor.createFromURL(resource).createImage();
 			descriptor.setImage(image);
 		}
 		attribute = configuration.getAttribute("enabled"); //$NON-NLS-1$
 		if (attribute != null) {
 			descriptor.setEnabled(Boolean.valueOf(attribute).booleanValue());
 		}
 		attribute = configuration.getAttribute("class"); //$NON-NLS-1$
 		Assert.isNotNull(attribute);
 		ViewerFilter filter = (ViewerFilter) configuration.createExecutableExtension("class"); //$NON-NLS-1$
 		Assert.isNotNull(filter);
 		descriptor.setFilter(filter);
 	}
 
 	/**
 	 * Create a column descriptor from the specified configuration element and add it to
 	 * the column descriptor list.
 	 * 
 	 * @param input the new input.
 	 * @param columns The column descriptor.
 	 * @param configuration The configuration element to read the descriptor from.
 	 * @param viewer The tree viewer to add the column to.
 	 */
 	private void createColumnDescriptor(Object input, final List<ColumnDescriptor> columns, final IConfigurationElement configuration, final TreeViewer viewer) {
 		if (isElementActivated(input, configuration)) {
 			String id = configuration.getAttribute("id"); //$NON-NLS-1$
 			Assert.isNotNull(id);
 			final ColumnDescriptor column = new ColumnDescriptor(id);
 			columns.add(column);
 			SafeRunner.run(new SafeRunnable() {
 				@Override
 				public void run() throws Exception {
 					initColumn(column, configuration, viewer);
 					column.setOrder(columns.size());
 				}
 			});
 		}
 	}
 	
 	/**
 	 * If the specified configuration element is activated under the current input.
 	 * 
 	 * @param input The input object.
 	 * @param configuration The configuration element that defines the activation element.
 	 * @return true if it is activated.
 	 */
 	private boolean isElementActivated(Object input, final IConfigurationElement configuration) {
 		IConfigurationElement[] children = configuration.getChildren("activation"); //$NON-NLS-1$
 		if(children == null || children.length == 0)
 			return true;
 		children = children[0].getChildren();
 		if(children == null || children.length == 0)
 			return true;
 		final IConfigurationElement config = children[0];
 		final EvaluationContext context = new EvaluationContext(null, input);
 		context.addVariable("input", input); //$NON-NLS-1$
 		final boolean[] result = new boolean[1];
 		SafeRunner.run(new SafeRunnable() {
 			@Override
 			public void run() throws Exception {
 				Expression expression = ExpressionConverter.getDefault().perform(config);
 				EvaluationResult evaluate = expression.evaluate(context);
 				if (evaluate == EvaluationResult.TRUE) {
 					result[0] = true;
 				}
 			}
 		});
 		return result[0];
 	}
 
 	/**
 	 * Initialize the column descriptor by reading the attributes from the configuration element.
 	 * 
 	 * @param column The column descriptor to be initialized.
 	 * @param configuration The configuration element.
 	 * @param viewer The tree viewer of the column.
 	 * @throws CoreException Thrown during parsing.
 	 */
 	@SuppressWarnings("rawtypes")
 	void initColumn(ColumnDescriptor column, IConfigurationElement configuration, TreeViewer viewer) throws CoreException {
 		String name = configuration.getAttribute("name"); //$NON-NLS-1$
 		Assert.isNotNull(name);
 		column.setName(name);
 		column.setDescription(configuration.getAttribute("description")); //$NON-NLS-1$
 		String attribute = configuration.getAttribute("moveable"); //$NON-NLS-1$
 		if (attribute != null) {
 			column.setMoveable(Boolean.valueOf(attribute).booleanValue());
 		}
 		attribute = configuration.getAttribute("resizable"); //$NON-NLS-1$
 		if (attribute != null) {
 			column.setResizable(Boolean.valueOf(attribute).booleanValue());
 		}
 		attribute = configuration.getAttribute("visible"); //$NON-NLS-1$
 		if (attribute != null) {
 			column.setVisible(Boolean.valueOf(attribute).booleanValue());
 		}
 		attribute = configuration.getAttribute("style"); //$NON-NLS-1$
 		if (attribute != null) {
 			column.setStyle(parseAlignment(attribute));
 		}
 		attribute = configuration.getAttribute("alignment"); //$NON-NLS-1$
 		if (attribute != null) {
 			column.setAlignment(parseAlignment(attribute));
 		}
 		attribute = configuration.getAttribute("width"); //$NON-NLS-1$
 		if (attribute != null) {
 			try {
 				column.setWidth(Integer.parseInt(attribute));
 			}
 			catch (NumberFormatException e) {
 			}
 		}
 		attribute = configuration.getAttribute("image"); //$NON-NLS-1$
 		if (attribute != null) {
 			String symbolicName = configuration.getContributor().getName();
 			URL resource = Platform.getBundle(symbolicName).getResource(attribute);
 			Image image = ImageDescriptor.createFromURL(resource).createImage();
 			column.setImage(image);
 		}
 		attribute = configuration.getAttribute("labelProvider"); //$NON-NLS-1$
 		if (attribute != null) {
 			TreeColumnLabelProvider labelProvider = (TreeColumnLabelProvider) configuration.createExecutableExtension("labelProvider"); //$NON-NLS-1$
 			if (labelProvider != null) {
 				labelProvider.setViewer(viewer);
 				column.setLabelProvider(labelProvider);
 			}
 		}
 		attribute = configuration.getAttribute("comparator"); //$NON-NLS-1$
 		if (attribute != null) {
 			Comparator comparator = (Comparator) configuration.createExecutableExtension("comparator"); //$NON-NLS-1$
 			if (comparator != null) {
 				column.setComparator(comparator);
 			}
 		}
 	}
 
 	/**
 	 * Parse the alignment attribute from a string to integer value.
 	 * 
 	 * @param attribute The attribute value.
 	 * @return The alignment/style value.
 	 */
 	private int parseAlignment(String attribute) {
 		if ("SWT.LEFT".equals(attribute)) { //$NON-NLS-1$
 			return SWT.LEFT;
 		}
 		else if ("SWT.RIGHT".equals(attribute)) { //$NON-NLS-1$
 			return SWT.RIGHT;
 		}
 		else if ("SWT.CENTER".equals(attribute)) { //$NON-NLS-1$
 			return SWT.CENTER;
 		}
 		return SWT.NONE;
 	}
 	
 	/**
 	 * Parse the style name from a string to integer value.
 	 * 
 	 * @param attribute The attribute value.
 	 * @return The alignment/style value.
 	 */
 	private int parseStyleName(String name) {
 		if ("SWT.NONE".equals(name)) { //$NON-NLS-1$
 			return SWT.NONE;
 		}
 		else if ("SWT.SINGLE".equals(name)) { //$NON-NLS-1$
 			return SWT.SINGLE;
 		}
 		else if ("SWT.MULTI".equals(name)) { //$NON-NLS-1$
 			return SWT.MULTI;
 		}
 		else if ("SWT.CHECK".equals(name)) { //$NON-NLS-1$
 			return SWT.CHECK;
 		}
 		else if ("SWT.FULL_SELECTION".equals(name)) { //$NON-NLS-1$
 			return SWT.FULL_SELECTION;
 		}
 		else if ("SWT.VIRTUAL".equals(name)) { //$NON-NLS-1$
 			return SWT.VIRTUAL;
 		}
 		else if ("SWT.NO_SCROLL".equals(name)) { //$NON-NLS-1$
 			return SWT.NO_SCROLL;
 		}
 		return SWT.NONE;
 	}
 	
 	/**
 	 * Parse the DND operation style and return a operation.
 	 * 
 	 * @param name The name of the DND operation.
 	 * @return an integer that represents the operation.
 	 */
 	private int parseDndOp(String name) {
 		if("DND.DROP_COPY".equals(name)) { //$NON-NLS-1$
 			return DND.DROP_COPY;
 		}
 		else if("DND.DROP_MOVE".equals(name)) { //$NON-NLS-1$
 			return DND.DROP_MOVE;
 		}
 		else if("DND.DROP_LINK".equals(name)) { //$NON-NLS-1$
 			return DND.DROP_LINK;
 		}
 		return 0;	
 	}
 	
 	/**
 	 * Parse and calculate the style from the give configuration element.
 	 * 
 	 * @param configuration The configuration element that defines the styles.
 	 * @return An integer that represents the defined styles.
 	 */
 	public int parseStyle(IConfigurationElement configuration) {
 		IConfigurationElement[] children = configuration.getChildren();
 		int style = SWT.NONE;
 		for(IConfigurationElement child : children) {
 			String name = child.getAttribute("name"); //$NON-NLS-1$
 			style |= parseStyleName(name);
 		}
 	    return style;
     }
 
 	/**
 	 * Parse the DND operation including DROP_COPY,
 	 * DROP_MOVE, DROP_LINK
 	 * 
 	 * @param configuration The configuration element in which the DND operation is defined.
 	 * @return The operations.
 	 */
 	public int parseDnd(IConfigurationElement configuration) {
 		IConfigurationElement[] children = configuration.getChildren("operations"); //$NON-NLS-1$
 		Assert.isTrue(children != null && children.length == 1);
 		children = children[0].getChildren();
 		int operations = 0;
 		for(IConfigurationElement child : children) {
 			String name = child.getAttribute("name"); //$NON-NLS-1$
 			operations |= parseDndOp(name);
 		}
 	    return operations;
     }
 
 	/**
 	 * Parse the transfer type from the give configuration element.
 	 * 
 	 * @param configuration The configuration element.
 	 * @return An array of transfer object that represents the transfer types.
 	 */
 	public Transfer[] parseTransferTypes(IConfigurationElement configuration) {
 		List<Transfer> transferTypes = new ArrayList<Transfer>();
 		IConfigurationElement[] children = configuration.getChildren("transferTypes"); //$NON-NLS-1$
 		Assert.isTrue(children != null && children.length == 1);
 		children = children[0].getChildren();
 		for(IConfigurationElement child : children) {
 			String name = child.getAttribute("name"); //$NON-NLS-1$
 			Transfer transfer = parseTransferType(name);
 			if (transfer != null) transferTypes.add(transfer);
 		}
 	    return transferTypes.toArray(new Transfer[transferTypes.size()]);
     }
 
 	/**
 	 * Translate the transfer type from this element.
 	 * 
 	 * @param name The attribute name.
 	 * @return The transfer instance.
 	 */
 	private Transfer parseTransferType(String name) {
 		if("TextTransfer".equals(name)) { //$NON-NLS-1$
 			return TextTransfer.getInstance();
 		}
 		if("ImageTransfer".equals(name)) { //$NON-NLS-1$
 			return ImageTransfer.getInstance();
 		}
 		if("FileTransfer".equals(name)) { //$NON-NLS-1$
 			return FileTransfer.getInstance();
 		}
 		if("LocalSelectionTransfer".equals(name)) { //$NON-NLS-1$
 			return LocalSelectionTransfer.getTransfer();
 		}
 	    return null;
     }
 
 	/**
 	 * Parse a DragSourceListener and return its instance.
 	 * 
 	 * @param viewer The tree viewer to create an element. 
 	 * @param configuration The configuration that wraps the instance.
 	 * @return The drag source listener created.
 	 */
 	public DragSourceListener parseDragSourceListener(final TreeViewer viewer, final IConfigurationElement configuration) {
 		final AtomicReference<DragSourceListener> reference = new AtomicReference<DragSourceListener>();
 		SafeRunner.run(new SafeRunnable(){
 			@Override
             public void run() throws Exception {
 				reference.set((DragSourceListener) createExecutableExtension(DragSourceListener.class, viewer, configuration));
             }});
 	    return reference.get();
     }
 	
 	/**
 	 * Create an executable instance from the given configuration element, with the tree viewer
 	 * as the constructor parameter.
 	 * 
 	 * @param aInterface The interface of the element should implement.
 	 * @param viewer The tree viewer to be passed
 	 * @param configuration The configuration element.
 	 * @return The object created.
 	 * @throws Exception
 	 */
 	Object createExecutableExtension(Class<?> aInterface, TreeViewer viewer, IConfigurationElement configuration) throws Exception{
 		String classname = configuration.getAttribute("class"); //$NON-NLS-1$
 		Assert.isNotNull(classname);
 		String contributorId = configuration.getContributor().getName();
 		Bundle bundle = Platform.getBundle(contributorId);
 		Assert.isNotNull(bundle);
 		Class<?> clazz = bundle.loadClass(classname);
 		Assert.isTrue(aInterface.isAssignableFrom(clazz));
 		try {
 			Constructor<?> constructor = clazz.getConstructor(TreeViewer.class);
 			return constructor.newInstance(viewer);
 		}
 		catch (NoSuchMethodException e) {
 			Constructor<?> constructor = clazz.getConstructor();
 			return constructor.newInstance();
 		}
 	}
 
 	/**
 	 * Parse a DropTargetListener and return its instance.
 	 * 
 	 * @param viewer The tree viewer to create an element. 
 	 * @param configuration The configuration that wraps the instance.
 	 * @return The drop target listener created.
 	 */
 	public DropTargetListener parseDropTargetListener(final TreeViewer viewer, final IConfigurationElement configuration) {
 		final AtomicReference<DropTargetListener> reference = new AtomicReference<DropTargetListener>();
 		SafeRunner.run(new SafeRunnable(){
 			@Override
             public void run() throws Exception {
 				reference.set((DropTargetListener) createExecutableExtension(DropTargetListener.class, viewer, configuration));
             }});
 	    return reference.get();
    }
 }
