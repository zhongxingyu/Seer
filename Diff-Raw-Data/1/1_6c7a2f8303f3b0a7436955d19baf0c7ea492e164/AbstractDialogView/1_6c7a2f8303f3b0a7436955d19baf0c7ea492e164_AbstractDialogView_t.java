 /*******************************************************************************
  * Copyright (c) 2007, 2012 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.ridgets.swt.views;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 
 import org.eclipse.riena.internal.ui.swt.utils.ShellHelper;
 import org.eclipse.riena.ui.ridgets.controller.AbstractWindowController;
 import org.eclipse.riena.ui.swt.GrabCorner;
 import org.eclipse.riena.ui.swt.RienaWindowRenderer;
 import org.eclipse.riena.ui.swt.lnf.LnFUpdater;
 import org.eclipse.riena.ui.swt.lnf.LnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.riena.ui.swt.utils.SWTControlFinder;
 import org.eclipse.riena.ui.swt.utils.UIControlsFactory;
 
 /**
  * Base class for Riena Dialogs. This class enhances JFace dialogs by adding:
  * (a) theming capabilities based on the current Look-and-Feel, (b) providing
  * View / Controller separation, (c) binding the View's widgets to the
  * Controller's ridgets.
  * <p>
  * Implementors have to subclass this class and provide these methods:
  * <ol>
  * <li>createController() - returns the Controller for this dialog</li>
  * <li>buildView() - creates the UI for this dialog. This includes creating the
  * appropriate buttons, such as Ok and Cancel.</li>
  * </ol>
  * This subclass can then be used as any other JFace dialog: create a new
  * instance and invoke dialog.open() to show the dialog. Open() blocks as long
  * as the dialog is open. It returns an integer code. By default this is
  * {@link Window#OK} ({@value Window#OK}). You can change the return code via
  * {@link AbstractWindowController}{@link #setReturnCode(int)}.
  * <p>
  * <b>How to migrate from DialogView</b>
  * <p>
  * If you have been using DialogView and want to use this class instead you
  * should:
  * <ol>
  * <li>createContentView() does not need to call super anymore</li>
  * <li>onClose() becomes close() - remember to invoke super.close() when
  * overriding</li>
  * <li>build() is deprecated - invoke open() in client code instead</li>
  * </ol>
  */
 public abstract class AbstractDialogView extends Dialog {
 
 	private static final LnFUpdater LNF_UPDATER = LnFUpdater.getInstance();
 
 	private final RienaWindowRenderer dlgRenderer;
 	private final ControlledView controlledView;
 
 	private String title;
 	private boolean isClosing;
 
 	private static Shell getShellByGuessing() {
 		if (PlatformUI.isWorkbenchRunning()) {
 			final IWorkbench workbench = PlatformUI.getWorkbench();
 			if (workbench != null) {
 				final IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
 				if (activeWorkbenchWindow != null) {
 					return activeWorkbenchWindow.getShell();
 				}
 			}
 		} else if (Display.getCurrent() != null) {
 			return Display.getCurrent().getActiveShell();
 		}
 		// may return null, but that is ok; Dialog does not require a parent shell
 		return null;
 	}
 
 	/**
 	 * Create a new instance of this class.
 	 * 
 	 * @param parentShell
 	 *            the parent Shell. It is recommended to supply one. If you use
 	 *            {@code null}, this class will try to guess the most
 	 *            appropriate parent shell.
 	 * @throws RuntimeException
 	 *             if no shell instance could be obtained - this can only happen
 	 *             when parentShell the value {@code null} and the class failed
 	 *             to obtain an appropriate shell.
 	 */
 	protected AbstractDialogView(final Shell parentShell) {
 		super(parentShell != null ? parentShell : getShellByGuessing());
 		title = ""; //$NON-NLS-1$
 		dlgRenderer = new RienaWindowRenderer(this, isPaintTitlebar());
 		controlledView = new ControlledView();
 		controlledView.setController(createController());
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	protected boolean isPaintTitlebar() {
 		return true;
 	}
 
 	@Override
 	public void create() {
 		// compute the 'styled' shell style, before creating the shell
 		setShellStyle(dlgRenderer.computeShellStyle());
 		super.create();
 		applyTitle(getShell());
 
 		addUIControls(getShell());
 		bindController();
 		LNF_UPDATER.updateUIControls(getShell(), true);
 		// after binding the controller it is necessary to calculate the bounds of the dialog again
 		// because the controller can add some data that influences the size of some widgets (e.g. ChoiceComposite)
 		initializeBounds();
 
 		ShellHelper.center(getShell());
 
 		getShell().addDisposeListener(new DisposeListener() {
 			public void widgetDisposed(final DisposeEvent e) {
 				if (!isClosing) {
 					close();
 				} else {
 					final AbstractWindowController controller = getController();
 					controller.setReturnCode(CANCEL);
 					setReturnCode(controller.getReturnCode());
 				}
 				isClosing = false;
 			}
 		});
 
		getShell().setMinimumSize(100, 50);
 	}
 
 	@Override
 	public boolean close() {
 		final AbstractWindowController controller = getController();
 		isClosing = true;
 		setReturnCode(controller.getReturnCode());
 		controlledView.unbind(controller);
 		return super.close();
 	}
 
 	/**
 	 * Returns the controller instance for this dialog.
 	 * 
 	 * @return an AbstractWindowController; never null
 	 */
 	public final AbstractWindowController getController() {
 		return controlledView.getController();
 	}
 
 	/**
 	 * Sets the default button for the dialog.
 	 * 
 	 * @param defaultButton
 	 *            the button that should be "focused" by default.
 	 * @since 3.0
 	 */
 	public void setDefaultButton(final Button defaultButton) {
 		getShell().setDefaultButton(defaultButton);
 	}
 
 	/**
 	 * Sets the title of this dialog (convenience method).
 	 * <p>
 	 * Implementation note: if you set the title both from the view (here) and
 	 * the controller (via windowRidget.setTitle(...)), the value used in the
 	 * controller will prevail.
 	 * 
 	 * @param title
 	 *            the title; never null.
 	 */
 	public final void setTitle(final String title) {
 		Assert.isNotNull(title);
 		this.title = title;
 	}
 
 	/**
 	 * @deprecated use {@link #open()}
 	 */
 	@Deprecated
 	public final void build() {
 		open();
 	}
 
 	// protected methods
 	////////////////////
 
 	/**
 	 * Add a control to the list of 'bound' controls. These controls will be
 	 * bound to ridgets by the framework.
 	 * 
 	 * @param uiControl
 	 *            the UI control to bind; never null
 	 * @param bindingId
 	 *            a non-empty non-null binding id for the control. Must be
 	 *            unique within this composite
 	 */
 	protected final void addUIControl(final Object uiControl, final String bindingId) {
 		controlledView.addUIControl(uiControl, bindingId);
 	}
 
 	@Override
 	protected final Control createButtonBar(final Composite parent) {
 		return dlgRenderer.createButtonBar(parent);
 	}
 
 	@Override
 	protected final Control createContents(final Composite parent) {
 		final Control result = dlgRenderer.createContents(parent);
 		super.createContents(dlgRenderer.getCenterComposite());
 		return result;
 	}
 
 	@Override
 	protected final Control createDialogArea(final Composite parent) {
 		final Composite mainComposite = createMainComposite(parent);
 		if (isResizable() && isHideOsBorder() && isPaintTitlebar()) {
 			new GrabCorner(mainComposite, mainComposite.getStyle());
 		}
 		createContentComposite(mainComposite);
 		return mainComposite;
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	protected void createOkCancelButtons(final Composite parent) {
 		// do nothing by default
 	}
 
 	private Composite createMainComposite(final Composite parent) {
 		final Composite mainComposite = UIControlsFactory.createComposite(parent, parent.getStyle());
 		mainComposite.setBackground(parent.getBackground());
 		mainComposite.setLayout(new FormLayout());
 		GridDataFactory.fillDefaults().grab(true, true).applyTo(mainComposite);
 		return mainComposite;
 	}
 
 	private void createContentComposite(final Composite parent) {
 		final Composite mainContentComposite = UIControlsFactory.createComposite(parent, parent.getStyle());
 		GridLayoutFactory.fillDefaults().applyTo(mainContentComposite);
 		final Composite contentComposite = UIControlsFactory.createComposite(mainContentComposite, parent.getStyle());
 		GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(contentComposite);
 		GridLayoutFactory.fillDefaults().applyTo(contentComposite);
 		buildView(contentComposite);
 		addUIControl(getShell(), AbstractWindowController.RIDGET_ID_WINDOW);
 		final FormData resultFormData = new FormData();
 		resultFormData.top = new FormAttachment(0, 0);
 		resultFormData.left = new FormAttachment(0, 0);
 		resultFormData.right = new FormAttachment(100, 0);
 		resultFormData.bottom = new FormAttachment(100, 0);
 		mainContentComposite.setLayoutData(resultFormData);
 		createOkCancelButtons(mainContentComposite);
 	}
 
 	@Override
 	protected boolean isResizable() {
 		return (getShellStyle() & SWT.RESIZE) == SWT.RESIZE;
 	}
 
 	private boolean isHideOsBorder() {
 		return LnfManager.getLnf().getBooleanSetting(LnfKeyConstants.DIALOG_HIDE_OS_BORDER);
 	}
 
 	/**
 	 * Creates the UI for this dialog. This includes creating the appropriate
 	 * buttons, such as Ok and Cancel.
 	 * 
 	 * @wbp.parser.entryPoint
 	 */
 	protected abstract Control buildView(Composite parent);
 
 	/**
 	 * Create the controller for this dialog.
 	 * 
 	 * @return a subclass of AbstractWindowController; never null
 	 */
 	protected abstract AbstractWindowController createController();
 
 	// helping methods
 	//////////////////
 
 	private void applyTitle(final Shell shell) {
 		if (shell.getText().length() == 0) {
 			shell.setText(title);
 		}
 	}
 
 	private void addUIControls(final Composite composite) {
 		final SWTControlFinder finder = new SWTControlFinder(composite) {
 			@Override
 			public void handleBoundControl(final Control control, final String bindingProperty) {
 				addUIControl(control, bindingProperty);
 			}
 		};
 		finder.run();
 	}
 
 	private void bindController() {
 		controlledView.initialize(getController());
 		controlledView.bind(getController());
 	}
 
 	private static final class ControlledView extends AbstractControlledView<AbstractWindowController> {
 		@Override
 		protected void addUIControl(final Object uiControl, final String propertyName) {
 			super.addUIControl(uiControl, propertyName);
 		}
 
 		@Override
 		protected void setController(final AbstractWindowController controller) {
 			super.setController(controller);
 		}
 	}
 
 }
