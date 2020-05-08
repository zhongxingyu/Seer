 /*
  * Copyright © 2011 Diamond Light Source Ltd.
  * Contact :  ScientificSoftware@diamond.ac.uk
  * 
  * This is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  * 
  * This software is distributed in the hope that it will be useful, but 
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
  * Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along
  * with this software. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.diamond.sda.navigator.views;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.WeakHashMap;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingDeque;
 
 import org.eclipse.jface.viewers.ILazyTreeContentProvider;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.ui.PlatformUI;
 
 import uk.ac.gda.util.io.SortingUtils;
 
 public class FileContentProvider implements ILazyTreeContentProvider {
 
 	public enum FileSortType {
 		ALPHA_NUMERIC, ALPHA_NUMERIC_DIRS_FIRST;
 	}
 	
 	private TreeViewer treeViewer;
 	private FileSortType sort = FileSortType.ALPHA_NUMERIC_DIRS_FIRST;
 	private BlockingQueue<UpdateRequest> elementQueue;
 	private BlockingQueue<UpdateRequest> childQueue;
 	
 	/**
 	 * Two threads with different priorities used.
 	 */
 	private Thread updateElementThread;
 
 	/**
 	 * Two threads with different priorities used.
 	 */
 	private Thread updateChildThread;
 
 	/**
 	 * Caching seems to be needed to keep the path sorting
 	 * fast. 
 	 * NOTE Is there a better way of doing this.
 	 */
 	private Map<File, List<File>> cachedSorting;
 
 	public FileContentProvider() {
 		this.cachedSorting = new WeakHashMap<File, List<File>>(89);
 		this.elementQueue  = new LinkedBlockingDeque<UpdateRequest>(Integer.MAX_VALUE);
 		this.childQueue    = new LinkedBlockingDeque<UpdateRequest>(Integer.MAX_VALUE);
 	}
 
 	
 	@Override
 	public void dispose() {
 		clearAndStop();
 		elementQueue = null;
 		childQueue = null;
 	}
 
 	private void clear() {
 		if (elementQueue!=null)  elementQueue.clear();
 		if (childQueue!=null)    childQueue.clear();
 		if (cachedSorting!=null) cachedSorting.clear();
 	}
 	
 	public void clearAndStop() {
 		clear();
 		elementQueue.add(new BlankUpdateRequest()); // break the queue
 		updateElementThread = null;
 		
 		childQueue.add(new BlankUpdateRequest()); // break the queue
 		updateChildThread = null;
 	}
 
 
 	@Override
 	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		treeViewer = (TreeViewer) viewer;
 		treeViewer.refresh();
 	}
 
 	@Override
 	public void updateElement(Object parent, int index) {
 
 		if (elementQueue==null) return;
 		if (PlatformUI.isWorkbenchRunning()) {
 			if (updateElementThread==null) updateElementThread = createUpdateThread(elementQueue, 9, "Update directory contents");
 			elementQueue.add(new ElementUpdateRequest(parent, index));
 		} else {
 			final File node = (File) parent;
 			final List<File> fa = getFileList(node);
 			updateElementInternal(node, index, fa);
 		}
 	}
 
 	public void updateElementInternal(Object parent, int index, List<File> fa) {
 		
 		
 		if (fa!=null && index < fa.size()) {
 			File element = fa.get(index);
 			treeViewer.replace(parent, index, element);
 			
 			// We correct when they expand, listFiles() could be slow.
 			if (element.isDirectory()) {
 				treeViewer.setChildCount(element, 1); // 1 for now
 				if (updateChildThread==null) updateChildThread = createUpdateThread(childQueue, 2, "Update child size");
				childQueue.add(new ChildUpdateRequest(element)); // process size from queue
 			} else {
 				treeViewer.setChildCount(element, 0);
 			}
 		}
 	}
 
 	@Override
 	public void updateChildCount(Object element, int currentChildCount) {
 		
 		if (childQueue==null) return;
 		
 		final File node = (File)element;
 		if (!node.isDirectory()) {
 			treeViewer.setChildCount(node, 0);
 			return;
 		}
 		
 		if (PlatformUI.isWorkbenchRunning()) {
 			if (updateChildThread==null) updateChildThread = createUpdateThread(childQueue, 2, "Update child size");
			childQueue.add(new ChildUpdateRequest(element));
 		} else {
 			updateChildCountInternal(element, currentChildCount);
 		}
 				
 	}
 	
 	private void updateChildCountInternal(Object element, int size) {
 		
 		final File node = (File) element;
 		if (node==null) return;
 		
 		if (node.isDirectory()) {
 			treeViewer.setChildCount(node, size);
 		} else {
 			treeViewer.setChildCount(node, 0);
 		}
 		
 	}
 
 
 	private List<File> getFileList(File node) {
 		
 		if (!node.isDirectory()) return null;
 		if (cachedSorting==null) return null;
 		
 		if (sort==FileSortType.ALPHA_NUMERIC) {
 			final File[] fa = node.listFiles();
 			if (fa==null) return null;
 			return Arrays.asList(fa);
 		}
 		
 		if (cachedSorting.containsKey(node)) {
 			return cachedSorting.get(node);
 		}
 		
 		final List<File> sorted;
 		if (sort==FileSortType.ALPHA_NUMERIC) {
 			sorted = SortingUtils.getSortedFileList(node, false);
 		} else {
 			sorted = SortingUtils.getSortedFileList(node, true);
 		}
 		cachedSorting.put(node, sorted);
 		return sorted;
 	}
 
 
 	@Override
 	public Object getParent(Object element) {
 		if (element==null || !(element instanceof File)) {
 			return null;
 		}
 		final File node = ((File) element);
 		return node.getParentFile();
 	}
 
 
 	public FileSortType getSort() {
 		return sort;
 	}
 
 
 	public void setSort(FileSortType sort) {
 		this.sort = sort;
 	}
 	
 	
 	private static Cursor  busy;
 	private static boolean isBusy = false;
 
 	private abstract class UpdateRequest {
 		
 		protected Object element;
 		protected int index;
 
 		UpdateRequest() {
 			element=null;
 			index  =-1;
 			if (busy ==null) busy   = treeViewer.getControl().getDisplay().getSystemCursor(SWT.CURSOR_WAIT);
 		}
 
 		UpdateRequest(final Object element, final int index) {
 			this.element = element;
 			this.index   = index;
 		}
 
 		public Object getElement() {
 			return element;
 		}
 
 		public int getIndex() {
 			return index;
 		}
 
 		public abstract boolean process() throws Exception;
 		
 		protected void updateBusy(final BlockingQueue<UpdateRequest> queue, boolean start) {
 			
 			if (start) {
 				if (!isBusy) {
 					isBusy = true;
 					if (treeViewer.getControl().isDisposed()) return;
 					treeViewer.getControl().getDisplay().syncExec(new Runnable() {
 						@Override
 						public void run() {
 							if (treeViewer.getControl().isDisposed()) return;
 							treeViewer.getControl().setCursor(busy);
 						}
 					});
 	
 				}
 			}
 			
 			// They requested us to stop
 			if (!start) {				
 				if (queue.isEmpty()) { // Nothing more in queue
            			if (treeViewer.getControl().isDisposed()) return;
                     treeViewer.getControl().getDisplay().syncExec(new Runnable() {
 						@Override
 						public void run() {
 							if (treeViewer.getControl().isDisposed()) return;
 							treeViewer.getControl().setCursor(null);
 							isBusy = false;
 						}
         			});
             	}
 			}
 		}
 	}
 	
 	private class BlankUpdateRequest extends UpdateRequest {
 
 		@Override
 		public boolean process() throws Exception {
 			return false;
 		}
 		
 	}
 	
 	private class ElementUpdateRequest extends UpdateRequest {
 
 		public ElementUpdateRequest(Object element, int index) {
 			super(element, index);
 		}
 
 		@Override
 		public boolean process() throws Exception {
 			
 			try {
 				updateBusy(elementQueue, true);
 	
 				final File node = (File) getElement();
 				final List<File> fa = getFileList(node);
 
 				if (treeViewer.getControl().isDisposed()) return false;
 				treeViewer.getControl().getDisplay().asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						if (treeViewer.getControl().isDisposed()) return;
 
 						updateElementInternal(node, getIndex(), fa);
 					}
 				});
 
 				
 			} finally {
 				updateBusy(elementQueue, false);
 
 			}
 			
 			return true;
 
 		}
 		
 	}
 	
 	
 	private class ChildUpdateRequest extends UpdateRequest {
 
		public ChildUpdateRequest(Object element) {
 			super();
 			this.element = element;
 		}
 
 
 		@Override
 		public boolean process() throws Exception {
 			
 			try {
 				
				updateBusy(childQueue, true);
 				
 				final File node = (File)element;
 				final File[] fa = node.listFiles();
 
 				if (treeViewer.getControl().isDisposed()) return false;
 				treeViewer.getControl().getDisplay().asyncExec(new Runnable() {
 					@Override
 					public void run() {
 						if (treeViewer.getControl().isDisposed()) return;
 
 						updateChildCountInternal(node, fa==null||fa.length<1 ? 0 : fa.length);
 					}
 				});
 				    
 				
 			} finally {
 				
			    updateBusy(childQueue, true);
 			}
 			
 			return true;
 
 		}
 		
 	}
 	
 
 	/**
 	 * Method creates a thread to process a queue
 	 */
 	private Thread createUpdateThread(final BlockingQueue<UpdateRequest> queue, final int priority, String name) {
 
 		final Thread thread = new Thread(name) {
 
 			@Override
 			public void run() {
 
 				while(!treeViewer.getControl().isDisposed() && queue!=null) {
 					try {
 
 						final UpdateRequest req = queue.take();
 
 						// Blank object added to break the queue
 						if (req.getElement()==null && req.getIndex()==-1) return;
                         if (req instanceof BlankUpdateRequest) return;
                         
 						final boolean ok = req.process();
 						if (!ok) break;
 
 					} catch (InterruptedException ne) {
 						break;
 
 					} catch (org.eclipse.swt.SWTException swtE) {
 						queue.clear();
 						break;
 
 					} catch (Exception ne) {
 						queue.clear();
 						continue;
 					}
 				}
 			}	
 		};
 		thread.setPriority(priority);
 		thread.setDaemon(true);
 		thread.start();
 
 		return thread;
 	}
 
 }
