 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.navigation.ui.swt.views;
 
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.riena.ui.ridgets.controller.AbstractWindowController;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * base class for SWT dialogs.
  */
 public abstract class DialogView extends AbstractControlledView<AbstractWindowController> {
 
 	private Composite parent;
 	protected Dialog dialog;
 
 	/**
 	 * @param parent
 	 *            the parent control.
 	 */
 	public DialogView(Composite parent) {
 
 		super();
 
 		initializeParent(parent);
 	}
 
 	/**
 	 * Creates the dialog of this view
 	 */
 	protected RienaDialog createDialog() {
 
		return new RienaDialog(getParentShell());
 	}
 
 	protected void createAndBindController() {
 
 		AbstractWindowController controller = createController();
 		initialize(controller);
 		bind(controller);
 	}
 
 	protected Control buildView(Composite parent) {
 
 		addUIControl(dialog.getShell(), AbstractWindowController.RIDGET_ID_WINDOW);
 
 		return parent;
 	}
 
 	protected abstract AbstractWindowController createController();
 
 	/**
 	 * Build and open the a dialog.
 	 */
 	public void build() {
 
 		if (dialog == null) {
 			dialog = createDialog();
 		}
 
 		dialog.open();
 	}
 
 	private void initializeParent(Composite parent) {
 		if (parent == null) {
			this.parent = getWorkbenchShell();
 		} else {
 			this.parent = parent;
 		}
 	}
 
	private Shell getWorkbenchShell() {

		if (PlatformUI.isWorkbenchRunning()) {
			return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		} else {
			return null;
		}
	}

	private Shell getParentShell() {

		if (parent != null) {
			return parent.getShell();
		} else {
			return null;
		}
 	}
 
 	private class RienaDialog extends Dialog {
 
 		private boolean closing;
 
 		private RienaDialog(Shell shell) {
 
 			super(shell);
 
 			closing = false;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.jface.dialogs.Dialog#close()
 		 */
 		@Override
 		public boolean close() {
 
 			closing = true;
 			unbind(getController());
 			boolean result = super.close();
 			closing = false;
 
 			return result;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.jface.dialogs.Dialog#create()
 		 */
 		@Override
 		public void create() {
 
 			super.create();
 
 			createAndBindController();
 			getShell().addDisposeListener(new DisposeListener() {
 				/*
 				 * (non-Javadoc)
 				 * 
 				 * @see
 				 * org.eclipse.swt.events.DisposeListener#widgetDisposed(org
 				 * .eclipse.swt.events.DisposeEvent)
 				 */
 				public void widgetDisposed(DisposeEvent e) {
 					if (!closing) {
 						close();
 					}
 				}
 			});
 			getShell().pack();
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * org.eclipse.jface.dialogs.Dialog#createContents(org.eclipse.swt.widgets
 		 * .Composite)
 		 */
 		@Override
 		protected Control createContents(Composite parent) {
 			return buildView(parent);
 		}
 	}
 }
