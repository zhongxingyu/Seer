 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.navigation.ui.swt.splashHandlers;
 
 import org.eclipse.equinox.app.IApplication;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.splash.AbstractSplashHandler;
 
 import org.eclipse.riena.core.wire.InjectExtension;
 import org.eclipse.riena.core.wire.Wire;
 import org.eclipse.riena.internal.navigation.ui.swt.Activator;
 import org.eclipse.riena.navigation.ui.swt.login.ILoginSplashView;
 import org.eclipse.riena.navigation.ui.swt.login.ILoginSplashViewExtension;
 
 /**
  * Riena base class for splash implementations.
  */
 public abstract class AbstractLoginSplashHandler extends AbstractSplashHandler {
 
 	protected ILoginSplashViewExtension loginSplashViewExtension;
 	private Composite loginComposite;
 	private ILoginSplashView loginView;
 
 	/**
 	 * 
 	 */
 	public AbstractLoginSplashHandler() {
 
 		super();
 
 		initialzeLoginSplashViewDefinition();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.splash.AbstractSplashHandler#init(org.eclipse.swt.widgets
 	 * .Shell)
 	 */
 	@Override
 	public void init(final Shell splash) {
 		// Store the shell
 		super.init(splash);
 
 		if (loginSplashViewExtension != null) {
 			// Configure the shell layout
 			configureUISplash();
 			// Create UI
 			createUI();
 			// Force the splash screen to layout
 			splash.layout(true);
 			// If the splash screen is already visible, keep visible and prevent the RCP application from 
 			// loading until the close button is clicked.
 			if (splash.isVisible()) {
 				doEventLoop();
 			}
 		}
 	}
 
 	/**
 	 * @see ILoginSplashView
 	 * 
 	 * @return the result of the login.
 	 */
 	public int getResult() {
 		return loginView.getResult();
 	}
 
 	/*
 	 * Keep the splash screen visible and prevent the RCP application from
 	 * loading until the close button is clicked.
 	 */
 	private void doEventLoop() {
 		Shell splash = getSplash();
 		while (!splash.isDisposed()) {
 			if (!splash.getDisplay().readAndDispatch()) {
 				splash.getDisplay().sleep();
 			}
 		}
 		if (!IApplication.EXIT_OK.equals(loginView.getResult())) {
 			// Abort the loading of the RCP application
 			PlatformUI.getWorkbench().getDisplay().close();
 			// This:
 			// PlatformUI.getWorkbench().close();
 			// will not work because the {@ IWorkbench} is not yet started.
 			System.exit(0);
 		}
 	}
 
 	private void createUI() {
 		// Create the login panel
		loginComposite = new Composite(getSplash(), SWT.BORDER);
		GridLayout layout = new GridLayout(1, false);
		loginComposite.setLayout(layout);
		loginComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		// Force composite to inherit the splash background
		loginComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
 		loginView = loginSplashViewExtension.createViewClass();
 		loginView.build(loginComposite);
 	}
 
 	protected void configureUISplash() {
 		// Configure layout
 		GridLayout layout = new GridLayout(1, false);
 		getSplash().setLayout(layout);
		// Force shell to inherit the splash background
 		getSplash().setBackgroundMode(SWT.INHERIT_DEFAULT);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.splash.AbstractSplashHandler#dispose()
 	 */
 	@Override
 	public void dispose() {
 
 		if (!getSplash().isDisposed()) {
 			getSplash().close();
 		}
 
 		super.init(null);
 		loginComposite = null;
 		loginView = null;
 	}
 
 	@InjectExtension
 	public void update(ILoginSplashViewExtension[] data) {
 
 		if (data.length > 0) {
 			loginSplashViewExtension = data[0];
 		}
 	}
 
 	private void initialzeLoginSplashViewDefinition() {
 		Wire.instance(this).andStart(Activator.getDefault().getContext());
 	}
 }
