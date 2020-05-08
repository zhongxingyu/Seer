 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.eclipse.esp.outline;
 
 import static org.oobium.build.esp.EspPart.Type.*;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.List;
 
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.BadPositionCategoryException;
 import org.eclipse.jface.text.DefaultPositionUpdater;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IPositionUpdater;
 import org.eclipse.jface.text.Position;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.widgets.Display;
 import org.oobium.build.esp.EspDom;
 import org.oobium.build.esp.EspElement;
 import org.oobium.build.esp.EspPart;
 import org.oobium.build.esp.elements.ImportElement;
 import org.oobium.build.esp.elements.JavaElement;
 import org.oobium.build.esp.elements.MarkupElement;
 import org.oobium.build.esp.elements.StyleChildElement;
 import org.oobium.build.esp.elements.StyleElement;
 import org.oobium.eclipse.esp.EspCore;
 
 /**
  * Divides the editor's document into ten segments and provides elements for them.
  */
 class EspContentProvider implements ITreeContentProvider, PropertyChangeListener {
 
 	private Comparator<Object> sorter = new Comparator<Object>() {
 		private EspLabelProvider lp = new EspLabelProvider();
 		@Override
 		public int compare(Object o1, Object o2) {
 			String s1 = lp.getText(o1).toLowerCase();
 			String s2 = lp.getText(o2).toLowerCase();
 			return s1.compareTo(s2);
 		}
 	};
 
 	private final static String SEGMENTS= "__esp_segments"; //$NON-NLS-1$
 	private IPositionUpdater positionUpdater= new DefaultPositionUpdater(SEGMENTS);
 	
 	private TreeViewer viewer;
 	private IDocument document;
 	private Imports imports;
 	
 	private boolean sort;
 	
 	public void dispose() {
 		if(document != null) {
 //			EspCore.get(document).removeListener(this);
 		}
 	}
 
 	public Object[] getChildren(Object element) {
 		Object[] oa = null;
 		if(element instanceof MarkupElement) {
 			oa = ((MarkupElement) element).getChildren().toArray();
 		}
 		if(element instanceof JavaElement) {
 			oa = ((JavaElement) element).getChildren().toArray();
 		}
 		if(element instanceof StyleElement) {
 			List<Object> selectors = new ArrayList<Object>();
 			for(EspElement child : ((MarkupElement) element).getChildren()) {
 				for(EspPart selector : ((StyleChildElement) child).getSelectorGroups()) {
 					selectors.add(selector);
 				}
 			}
 			oa = selectors.toArray();
 		}
 		if(element instanceof Imports) {
 			oa = ((Imports) element).getChildren().toArray();
 		}
 		
 		if(oa == null) {
 			return new Object[0];
 		} else {
 			if(sort) {
 				Arrays.sort(oa, sorter);
 			}
 			return oa;
 		}
 	}
 
 	public Object[] getElements(Object element) {
 		if(element instanceof IDocument) {
 			List<Object> elements = new ArrayList<Object>();
 			imports = null;
 			EspDom dom = EspCore.get(document);
 			for(int i = 0; i < dom.size(); i++) {
 				EspElement e = dom.get(i);
 				if(e.isA(ImportElement)) {
 					if(imports == null) imports = new Imports();
 					imports.addChild(e);
 				} else {
 					int level = e.getLevel();
 					if(level == 0) {
 						elements.add(e);
 					} else if(level < 1 && e instanceof StyleElement) { // CSS or ESS file
						for(EspElement child : ((StyleElement) e).getChildren()) {
 							for(EspPart selector : ((StyleChildElement) child).getSelectorGroups()) {
 								elements.add(selector);
 							}
 						}
 					}
 				}
 			}
 			if(imports != null) {
 				elements.add(0, imports);
 			}
 			Object[] oa = elements.toArray();
 			if(dom.isStyle() && oa.length > 0 && sort) {
 				Arrays.sort(oa, sorter);
 			}
 			return oa;
 		}
 		return new Object[0];
 	}
 
 	public Object getParent(Object element) {
 		if(element instanceof ImportElement) {
 			return imports;
 		}
 		if(element instanceof EspElement) {
 			return ((EspElement) element).getParent();
 		}
 		if(element instanceof EspPart) {
 			return ((EspPart) element).getElement();
 		}
 		if(element instanceof Imports) {
 			return imports;
 		}
 		return null;
 	}
 
 	public boolean hasChildren(Object element) {
 		if(element instanceof MarkupElement) {
 			return ((MarkupElement) element).hasChildren();
 		}
 		if(element instanceof JavaElement) {
 			return ((JavaElement) element).hasChildren();
 		}
 		if(element instanceof StyleElement) {
 			return ((StyleElement) element).hasChildren();
 		}
 		if(element instanceof Imports) {
 			return ((Imports) element).hasChildren();
 		}
 		return false;
 	}
 
 	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		this.viewer = (TreeViewer) viewer;
 		
 		if (oldInput != null) {
 //			EspCore.get(document).removeListener(this);
 			if (document != null) {
 				try {
 					document.removePositionCategory(SEGMENTS);
 				} catch (BadPositionCategoryException x) {
 				}
 				document.removePositionUpdater(positionUpdater);
 			}
 		}
 
 		document = (IDocument) newInput;
 		
 		if (newInput != null) {
 //			EspCore.get(document).addListener(this);
 			if (document != null) {
 				document.addPositionCategory(SEGMENTS);
 				document.addPositionUpdater(positionUpdater);
 			}
 		}
 	}
 
 	protected void parse(IDocument document) {
 		int lines= document.getNumberOfLines();
 		int increment= Math.max(Math.round(lines / 10), 10);
 
 		for (int line= 0; line < lines; line += increment) {
 
 			int length= increment;
 			if (line + increment > lines)
 				length= lines - line;
 
 			try {
 
 				int offset= document.getLineOffset(line);
 				int end= document.getLineOffset(line + length);
 				length= end - offset;
 				Position p= new Position(offset, length);
 				document.addPosition(SEGMENTS, p);
 //				content.add(new Segment(MessageFormat.format(EspEditorMessages.getString("OutlinePage.segment.title_pattern"), new Object[] { new Integer(offset) }), p)); //$NON-NLS-1$
 
 			} catch (BadPositionCategoryException x) {
 			} catch (BadLocationException x) {
 			}
 		}
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt) {
 		Display.getDefault().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				viewer.refresh();
 			}
 		});
 	}
 	
 	public void setSort(boolean sort) {
 		if(this.sort != sort) {
 			this.sort = sort;
 			Display.getDefault().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					viewer.refresh();
 				}
 			});
 		}
 	}
 	
 }
