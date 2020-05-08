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
 package org.eclipse.riena.navigation.ui.swt.application;
 
 import org.osgi.framework.Bundle;
 
 import org.eclipse.core.databinding.observable.Realm;
 import org.eclipse.equinox.app.IApplication;
 import org.eclipse.equinox.app.IApplicationContext;
 import org.eclipse.jface.databinding.swt.SWTObservables;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.application.ActionBarAdvisor;
 import org.eclipse.ui.application.IActionBarConfigurer;
 import org.eclipse.ui.internal.Workbench;
 import org.eclipse.ui.splash.AbstractSplashHandler;
 
 import org.eclipse.riena.core.exception.ExceptionFailure;
 import org.eclipse.riena.core.injector.Inject;
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.internal.navigation.ui.swt.Activator;
 import org.eclipse.riena.internal.navigation.ui.swt.IAdvisorHelper;
 import org.eclipse.riena.internal.ui.swt.utils.RcpUtilities;
 import org.eclipse.riena.navigation.IApplicationNode;
 import org.eclipse.riena.navigation.listener.ApplicationNodeListener;
 import org.eclipse.riena.navigation.ui.application.AbstractApplication;
 import org.eclipse.riena.navigation.ui.controllers.ApplicationController;
 import org.eclipse.riena.navigation.ui.login.ILoginDialogView;
 import org.eclipse.riena.navigation.ui.swt.login.ILoginSplashViewDefinition;
 import org.eclipse.riena.navigation.ui.swt.splashHandlers.AbstractLoginSplashHandler;
 import org.eclipse.riena.navigation.ui.swt.views.ApplicationAdvisor;
 import org.eclipse.riena.ui.swt.utils.ImageStore;
 
 /**
  * Creates and starts an empty swt application Subclass to create own model or
  * controller.
  */
 public abstract class SwtApplication extends AbstractApplication {
 
 	protected ILoginSplashViewDefinition loginSplashViewDefinition;
 	private LoginNonActivityTimer loginNonActivityTimer;
 
 	@Override
 	public Object createView(IApplicationContext context, IApplicationNode pNode) {
 		Display display = PlatformUI.createDisplay();
 		try {
 			ApplicationAdvisor advisor = new ApplicationAdvisor(createApplicationController(pNode), new AdvisorHelper());
 			initializeLoginNonActivityTimer(display, pNode, context);
 			int returnCode = PlatformUI.createAndRunWorkbench(display, advisor);
 			if (returnCode == PlatformUI.RETURN_RESTART) {
 				return IApplication.EXIT_RESTART;
 			}
 			return IApplication.EXIT_OK;
 		} finally {
 			display.dispose();
 		}
 	}
 
 	/**
 	 * Creates an action bar advisor for a window.
 	 * <p>
 	 * Subclasses may override.
 	 * 
 	 * @return an AdvisorBarAdvisor; never null
 	 * @since 1.2
 	 */
 	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
 		return new ActionBarAdvisor(configurer);
 	}
 
 	/**
 	 * Return the default keyboard scheme for this applications.
 	 * <p>
 	 * Subclasses defining their own scheme using the '...' extension point,
 	 * should override this method and return the appropriate id.
 	 * <p>
 	 * When defining your own scheme, you can specify '...' as the parent
 	 * scheme, to inherit the default Riena keybindings. If you wish to start
 	 * with a blank scheme, leave the parent attribute empty.
 	 * <p>
 	 * Example:
 	 * 
 	 * <pre>
 	 * &lt;extension
 	 *          point=&quot;org.eclipse.ui.bindings&quot;&gt;
 	 *       &lt;scheme
 	 *             id=&quot;org.eclipse.riena.example.client.scheme&quot;
 	 *             name=&quot;Riena Client Key Bindings&quot;
 	 *             parentId=&quot;org.eclipse.riena.ui.defaultBindings&quot;&gt;
 	 *       &lt;/scheme&gt;
 	 *       &lt;key
 	 *             commandId=&quot;org.eclipse.riena.example.client.exitCommand&quot;
 	 *             contextId=&quot;org.eclipse.ui.contexts.window&quot;
 	 *             schemeId=&quot;org.eclipse.riena.example.client.scheme&quot;
 	 *             sequence=&quot;F10&quot;&gt;
 	 *       &lt;/key&gt;
 	 * &lt;/extension&gt;
 	 * </pre>
 	 * 
 	 * @return the identifer of a valid scheme as String; never null.
 	 */
 	protected String getKeyScheme() {
 		return "org.eclipse.riena.ui.defaultBindings"; //$NON-NLS-1$
 	}
 
 	public void stop() {
 		final IWorkbench workbench = PlatformUI.getWorkbench();
 		if (workbench == null) {
 			return;
 		}
 		final Display display = workbench.getDisplay();
 		display.syncExec(new Runnable() {
 			public void run() {
 				if (!display.isDisposed()) {
 					workbench.close();
 				}
 			}
 		});
 	}
 
 	/*
 	 * keep public, called via reflection
 	 */
 	public void update(ILoginSplashViewDefinition[] data) {
 		if (data.length > 0) {
 			loginSplashViewDefinition = data[0];
 		}
 	}
 
 	protected ApplicationController createApplicationController(IApplicationNode pModel) {
 		return new ApplicationController(pModel);
 	}
 
 	protected abstract Bundle getBundle();
 
 	protected void prePerformLogin(IApplicationContext context) {
 		if (RcpUtilities.getWorkbenchShell() != null) {
 			// To minimize the workbench and show the login dialog later, the workbench has be made first invisible and then minimized.
 			RcpUtilities.getWorkbenchShell().setVisible(false);
 			RcpUtilities.getWorkbenchShell().setMinimized(true);
 			// Make workbench visible to be shown as minimized in the (windows) task bar.
 			RcpUtilities.getWorkbenchShell().setVisible(true);
 		}
 	}
 
 	protected void postPerformLogin(IApplicationContext context, Object result) {
 		if (!EXIT_OK.equals(result)) {
 			PlatformUI.getWorkbench().close();
 		} else {
 			RcpUtilities.getWorkbenchShell().setMinimized(false);
 			loginNonActivityTimer.schedule();
 		}
 	}
 
 	@Override
 	protected Object doPerformLogin(IApplicationContext context) {

		final ILoginDialogView loginDialogView = loginDialogViewDefinition.createViewClass();
 		Realm realm = SWTObservables.getRealm(getDisplay());
 		do {
 			Realm.runWithDefault(realm, new Runnable() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see java.lang.Runnable#run()
				 */
 				public void run() {
 					loginDialogView.build();
 				}
 			});
 		} while (EXIT_RESTART.equals(loginDialogView.getResult()));
 
 		return loginDialogView.getResult();
 	}
 
 	@Override
 	protected Object doPerformSplashLogin(IApplicationContext context) {
 
 		Shell shell = new Shell(getDisplay(), SWT.NO_TRIM | SWT.APPLICATION_MODAL);
 		initilizeShellBackgroundImage(shell, getBackgroundImagePath(context));
 		AbstractLoginSplashHandler loginSplashHandler = getLoginSplashHandler();
 		loginSplashHandler.init(shell);
 		shell.open();
 		while (!shell.isDisposed()) {
 			if (!getDisplay().readAndDispatch()) {
 				getDisplay().sleep();
 			}
 		}
 
 		return loginSplashHandler.getResult();
 	}
 
 	@Override
 	protected boolean isSplashLogin(IApplicationContext context) {
 		return loginSplashViewDefinition != null && getLoginSplashHandler() != null;
 	}
 
 	@Override
 	protected boolean isDialogLogin(IApplicationContext context) {
 		return super.isDialogLogin(context) && loginSplashViewDefinition == null;
 	}
 
 	@Override
 	protected void initializeLoginViewDefinition() {
 		super.initializeLoginViewDefinition();
 
 		initializeLoginSplashViewDefinition();
 	}
 
 	// helping methods
 	//////////////////
 
 	private String getBackgroundImagePath(IApplicationContext context) {
 		return "splash.bmp"; //$NON-NLS-1$
 	}
 
 	private Display getDisplay() {
 		if (PlatformUI.isWorkbenchRunning()) {
 			return PlatformUI.getWorkbench().getDisplay();
 		} else {
 			return PlatformUI.createDisplay();
 		}
 	}
 
 	private AbstractLoginSplashHandler getLoginSplashHandler() {
 		if (!PlatformUI.isWorkbenchRunning()) {
 			return null;
 		}
 
 		// Use the splash handler of the workbench both for the login and for
 		// a later re-login after inactivity timeout. Unfortunately it is not
 		// accessible and an enhancement request to change that was rejected:
 		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=260736.
 		AbstractSplashHandler loginSplashHandler = ReflectionUtils.invokeHidden(Workbench.class, "getSplash"); //$NON-NLS-1$
 		if (loginSplashHandler instanceof AbstractLoginSplashHandler) {
 			return (AbstractLoginSplashHandler) loginSplashHandler;
 		} else {
 			return null;
 		}
 	}
 
 	private void initializeLoginNonActivityTimer(final Display display, IApplicationNode pNode,
 			final IApplicationContext context) {
 		pNode.addListener(new ApplicationNodeListener() {
 			@Override
 			public void afterActivated(IApplicationNode source) {
 				if (isSplashLogin(context) && loginSplashViewDefinition.getNonActivityDuration() > 0) {
 					loginNonActivityTimer = new LoginNonActivityTimer(display, context, loginSplashViewDefinition
 							.getNonActivityDuration());
 					loginNonActivityTimer.schedule();
 				} else if (isDialogLogin(context) && loginDialogViewDefinition.getNonActivityDuration() > 0) {
 					loginNonActivityTimer = new LoginNonActivityTimer(display, context, loginDialogViewDefinition
 							.getNonActivityDuration());
 					loginNonActivityTimer.schedule();
 				}
 			}
 		});
 	}
 
 	private void initializeLoginSplashViewDefinition() {
 		Inject.extension(ILoginSplashViewDefinition.EP_TYPE).useType(ILoginSplashViewDefinition.class).into(this)
 				.andStart(Activator.getDefault().getContext());
 	}
 
 	private void initilizeShellBackgroundImage(Shell shell, String imageName) {
 		Image bi = ImageStore.getInstance().getImage(imageName);
 		shell.setSize(bi.getBounds().width, bi.getBounds().height);
 		shell.setBackgroundImage(bi);
 	}
 
 	// helping classes
 	//////////////////
 
 	private final class AdvisorHelper implements IAdvisorHelper {
 		public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
 			return SwtApplication.this.createActionBarAdvisor(configurer);
 		}
 
 		public String getKeyScheme() {
 			return SwtApplication.this.getKeyScheme();
 		}
 	}
 
 	private static final class EventListener implements Listener {
 		private boolean activity;
 		private long activityTime;
 
 		private EventListener() {
 			activity = false;
 			activityTime = -1;
 		}
 
 		public void handleEvent(Event event) {
 			activity = true;
 			activityTime = System.currentTimeMillis();
 		}
 	}
 
 	private final class LoginNonActivityTimer implements Runnable {
 		private Display display;
 		private IApplicationContext context;
 		private EventListener eventListener;
 		private int nonActivityDuration;
 
 		private LoginNonActivityTimer(Display display, IApplicationContext context, int nonActivityDuration) {
 			super();
 
 			this.display = display;
 			this.context = context;
 			this.nonActivityDuration = nonActivityDuration;
 			initializeEventListener();
 		}
 
 		public void run() {
 			try {
 				if (eventListener.activity) {
 					schedule();
 					return;
 				}
 
 				prePerformLogin(context);
 				postPerformLogin(context, performLogin(context));
 			} catch (Exception e) {
 				throw new ExceptionFailure(e.getLocalizedMessage(), e);
 			}
 		}
 
 		private void initializeEventListener() {
 			eventListener = new EventListener();
 			display.addFilter(SWT.KeyDown, eventListener);
 			display.addFilter(SWT.KeyUp, eventListener);
 			display.addFilter(SWT.MouseDoubleClick, eventListener);
 			display.addFilter(SWT.MouseDown, eventListener);
 			//display.addFilter(SWT.MouseEnter, eventListener);
 			display.addFilter(SWT.MouseExit, eventListener);
 			//display.addFilter(SWT.MouseHover, eventListener);
 			display.addFilter(SWT.MouseMove, eventListener);
 			display.addFilter(SWT.MouseUp, eventListener);
 			display.addFilter(SWT.MouseWheel, eventListener);
 			display.addFilter(SWT.Traverse, eventListener);
 		}
 
 		private void schedule() {
 			initializeForSchedule();
 			display.timerExec(getTimerDelay(), this);
 		}
 
 		private void initializeForSchedule() {
 			if (eventListener.activityTime == -1) {// initialize on first schedule
 				eventListener.activityTime = System.currentTimeMillis();
 			}
 			eventListener.activity = false;
 		}
 
 		private int getTimerDelay() {
 			return nonActivityDuration - (int) (System.currentTimeMillis() - eventListener.activityTime);
 		}
 	}
 
 }
