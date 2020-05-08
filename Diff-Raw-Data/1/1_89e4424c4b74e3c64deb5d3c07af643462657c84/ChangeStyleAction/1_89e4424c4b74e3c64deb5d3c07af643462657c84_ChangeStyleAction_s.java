 /*******************************************************************************
  * Copyright (c) 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.pagedesigner.editors.actions;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jst.pagedesigner.commands.DesignerCommand;
 import org.eclipse.jst.pagedesigner.commands.range.ApplyStyleCommand;
 import org.eclipse.jst.pagedesigner.range.RangeUtil;
 import org.eclipse.jst.pagedesigner.viewer.DesignRange;
 import org.eclipse.jst.pagedesigner.viewer.IHTMLGraphicalViewer;
 import org.eclipse.jst.pagedesigner.viewer.IHTMLGraphicalViewerListener;
 import org.eclipse.ui.texteditor.IUpdate;
 
 /**
  * @author mengbo
  */
 public abstract class ChangeStyleAction extends Action implements IUpdate {
 	protected IHTMLGraphicalViewer _viewer;
 
 	String _expectedTag;
 
 	String _expectedCSSProperty;
 
 	String _expectedCSSPropertyValue;
 
 	IHTMLGraphicalViewerListener _listener = new IHTMLGraphicalViewerListener() {
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.jst.pagedesigner.viewer.IHTMLGraphicalViewerListener#selectionAboutToChange()
 		 */
 		public void selectionAboutToChange() {
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.jst.pagedesigner.viewer.IHTMLGraphicalViewerListener#selectionChangeFinished()
 		 */
 		public void selectionChangeFinished() {
 			update();
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
 		 */
 		public void selectionChanged(SelectionChangedEvent event) {
 			update();
 		}
 	};
 
 	/**
 	 * @param text
 	 * @param image
 	 */
 	public ChangeStyleAction(String text, String name, ImageDescriptor image,
 			int style) {
 		super(text, style);
 		_expectedTag = name;
 		this.setImageDescriptor(image);
 	}
 
 	public void setViewer(IHTMLGraphicalViewer viewer) {
 		if (viewer == _viewer) {
 			return;
 		}
 		if (_viewer != null) {
 			_viewer.removeSelectionChangedListener(_listener);
 		}
 		_viewer = viewer;
 		if (_viewer != null) {
 			_viewer.addSelectionChangedListener(_listener);
 		}
 		update();
 	}
 
 	/**
 	 * 
 	 */
 	public void update() {
 		if (_viewer == null) {
 			this.setChecked(false);
 			this.setEnabled(false);
 			return;
 		}
 		if (!_viewer.isInRangeMode()) {
 			// XXX: later we may support in range mode.
 			this.setChecked(false);
 			this.setEnabled(false);
 			return;
 		}
 		DesignRange range = _viewer.getRangeSelection();
 		if (range == null || !range.isValid()) {
 			this.setChecked(false);
 			this.setEnabled(false);
 			return;
 		}
 		updateStatus(RangeUtil.normalize(range));
 	}
 
 	/**
 	 * @param range
 	 */
 	private void updateStatus(DesignRange range) {
 		if (range.isEmpty()) {
 			this.setEnabled(false);
 			this.setChecked(false); // FIXME: not handling checked status yet.
 		} else {
 			this.setEnabled(true);
 			this.setChecked(false);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.action.Action#run()
 	 */
 	public void run() {
 		if (_viewer == null || !_viewer.isInRangeMode()) {
 			return;
 		}
 		DesignRange range = _viewer.getRangeSelection();
 		if (range == null || !range.isValid()) {
 			return;
 		}
 		if (range.isEmpty())
 			return; // nothing to do to empty range.
 
 		// if currently checked, means unapply the style. If current not
 		// checked, means apply the style
 		boolean apply = !this.isChecked();
 		if (apply) {
 			applyStyle();
 		} else {
 			// not supported yet.
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private void applyStyle() {
 		DesignerCommand command = new ApplyStyleCommand(_viewer,
 				getExpectedTag(), getExpectedCSSProperty(),
 				getExpectedCSSPropertyValue());
 		command.execute();
 	}
 
 	/**
 	 * @return
 	 */
 	protected abstract String getExpectedCSSPropertyValue();
 
 	/**
 	 * @return
 	 */
 	protected abstract String getExpectedCSSProperty();
 
 	/**
 	 * @return
 	 */
 	protected String getExpectedTag() {
 		return _expectedTag;
 	}
 }
