 /**
  * Copyright (c) 2013 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  * 
  */
 package org.obeonetwork.dsl.smartdesigner.design.popups;
 
 import java.util.List;
 
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.jface.dialogs.PopupDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * Popup showing a clickable list of EReference.
  * 
  * @author Stephane Drapeau - Obeo
  * 
  */
 public class MagicConnectorPopup extends PopupDialog {
 
 	/**
 	 * The Ereference selected.
 	 */
 	EReference result;
 
 	/**
 	 * The list of EReference to display in the popup.
 	 */
 	List<EReference> refs;
 
 	/**
 	 * Constructs a new instance of <code>MagicConnectorPopup</code>.
 	 * 
 	 * @param parent
 	 *            The parent shell.
 	 * @param references
 	 *            The list of EReference to display in the popup.
 	 */
 	public MagicConnectorPopup(Shell parent, List<EReference> references) {
 		super(parent, 0, true, true, true, false, true, null, null);
 		refs = references;
 	}
 
 	/**
 	 * @return the EReference selected.
 	 */
 	public EReference getResult() {
 		return result;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.dialogs.PopupDialog#getInitialLocation(org.eclipse.swt.graphics.Point)
 	 */
 	@Override
 	protected Point getInitialLocation(Point initialSize) {
 		// show popup relative to cursor
 		Display display = getShell().getDisplay();
 		Point location = display.getCursorLocation();
 		final int cursorSize = 15;
 		location.x += cursorSize;
 		location.y += cursorSize;
 		return location;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.jface.dialogs.PopupDialog#createDialogArea(org.eclipse.swt
 	 * .widgets.Composite)
 	 */
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NONE);
 		final org.eclipse.swt.widgets.List list = new org.eclipse.swt.widgets.List(
 				composite, SWT.V_SCROLL);
 		for (EReference r : refs)
 			list.add(r.getName());
 		Rectangle clientArea = composite.getClientArea();
 		list.setBounds(clientArea.x, clientArea.y, 250, 75);
		list.addListener(SWT.Selection, new org.eclipse.swt.widgets.Listener() {
			public void handleEvent(Event e) {
				int[] selection = list.getSelectionIndices();
				if (selection.length > 0) {
					result = refs.get(selection[0]);
 					getShell().dispose();
 				}
 			}
 		});
		list.addListener(SWT.DefaultSelection,
				new org.eclipse.swt.widgets.Listener() {
					public void handleEvent(Event e) {

					}
				});
 		return composite;
 	}
 
 	/**
 	 * Runs the event loop for the given shell.
 	 * 
 	 * @param loopShell
 	 *            the shell
 	 */
 	private void runEventLoop(Shell loopShell) {
 		// Use the display provided by the shell if possible
 		Display display;
 		if (getShell() == null) {
 			display = Display.getCurrent();
 		} else {
 			display = loopShell.getDisplay();
 		}
 		while (loopShell != null && !loopShell.isDisposed()) {
 			try {
 				if (!display.readAndDispatch()) {
 					display.sleep();
 				}
 			} catch (Throwable e) {
				// exceptionHandler.handleException(e);
 			}
 		}
 		if (!display.isDisposed())
 			display.update();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.dialogs.PopupDialog#open()
 	 */
 	public int open() {
 		int returnCode = super.open();
 		runEventLoop(getShell());
 		return returnCode;
 	}
 
 }
