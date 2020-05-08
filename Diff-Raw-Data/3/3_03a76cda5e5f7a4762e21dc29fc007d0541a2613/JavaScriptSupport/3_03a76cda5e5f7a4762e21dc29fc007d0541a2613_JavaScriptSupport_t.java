 /*******************************************************************************
  * Copyright (c) 2011 BestSolution.at and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
  ******************************************************************************/
 package org.eclipse.e4.tools.emf.ui.script.js;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.lang.annotation.Annotation;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.eclipse.e4.core.contexts.ContextInjectionFactory;
 import org.eclipse.e4.core.contexts.IEclipseContext;
 import org.eclipse.e4.core.di.annotations.Execute;
 import org.eclipse.e4.tools.emf.ui.common.IScriptingSupport;
 import org.eclipse.e4.tools.emf.ui.script.js.text.JavaScriptEditor;
import org.eclipse.e4.tools.services.IResourcePool;
 import org.eclipse.e4.ui.services.IStylingEngine;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.TitleAreaDialog;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Widget;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.FrameworkUtil;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.packageadmin.PackageAdmin;
 
 @SuppressWarnings({ "restriction", "deprecation" })
 public class JavaScriptSupport implements IScriptingSupport {
 	public void openEditor(Shell shell, final Object mainElement, final IEclipseContext context) {
 		final IEclipseContext childContext = context.createChild();
 		
 		TitleAreaDialog dialog = new TitleAreaDialog(shell) {
 			private JavaScriptEditor editor;
 			private Logger logger;
 			
 			@Override
 			protected Control createDialogArea(Composite parent) {
 				Composite container = (Composite) super.createDialogArea(parent);
 				logger = new Logger(getShell());
 				getShell().setText("Execute JavaScript");
 				setTitle("Execute JavaScript");
 				setMessage("Enter some JavaScript and execute it");
				setTitleImage(context.get(IResourcePool.class).getImageUnchecked(ResourceProvider.IMG_WIZBAN_JAVASCRIPT));
 				
 				childContext.set(Composite.class, container);
 				
 				editor = ContextInjectionFactory.make(JavaScriptEditor.class, childContext);
 				GridData gd = new GridData(GridData.FILL_BOTH);
 				gd.minimumHeight = 350;
 				gd.minimumWidth = 400;
 				editor.getControl().setLayoutData(gd);
 				return container;
 			}
 			
 			@Override
 			protected void okPressed() {
 				execute(logger, mainElement, context, editor.getContent());
 			}
 			
 			
 			@Override
 			protected Button createButton(Composite parent, int id,
 					String label, boolean defaultButton) {
 				return super.createButton(parent, id, id == IDialogConstants.OK_ID ? "Execute" : label, defaultButton);
 			}
 		};
 		
 		dialog.open();
 		childContext.dispose();
 	}
 
 	private void execute(Logger logger, Object mainElement, IEclipseContext context, String script) {
 		Context cx = Context.enter();
 		Scriptable sc = cx.initStandardObjects();
 		
 		ScriptableObject.putProperty(sc, "mainObject", mainElement);
 		ScriptableObject.putProperty(sc, "eclipseContext", context);
 		ScriptableObject.putProperty(sc, "swt", new SWTSupport(Display.getCurrent()));
 		ScriptableObject.putProperty(sc, "service", new ServiceProvider(context));
 		ScriptableObject.putProperty(sc, "di", new DiProvider(context));
 		ScriptableObject.putProperty(sc, "log", logger);
 		
 		try {
 			cx.evaluateString(sc, script, "<cmd>", 1, null);	
 		} catch (Exception e) {
 			try {
 				logger.error(e);
 			} catch (Exception e1) {
 			}
 		}
 	}
 	
 	public static class DiProvider {
 		private IEclipseContext context;
 		private PackageAdmin packageAdmin;
 		
 		public DiProvider(IEclipseContext context) {
 			this.context = context;
 		}
 		
 		public Object newInstance(String bundlename, String className) throws ClassNotFoundException {
 			Bundle bundle = getBundle(bundlename);
 			if( bundle != null ) {
 				Class<?> clazz = bundle.loadClass(className);
 				return ContextInjectionFactory.make(clazz, context);
 			}
 			return new IllegalArgumentException("Bundle '"+bundlename+"' is not known");
 		}
 		
 		public Object execute(Object object) {
 			return ContextInjectionFactory.invoke(object, Execute.class, context);
 		}
 		
 		public Object invokeByAnnotation(Object object, String bundlename, String className) throws ClassNotFoundException {
 			Bundle bundle = getBundle(bundlename);
 			if( bundle != null ) {
 				@SuppressWarnings("unchecked")
 				Class<? extends Annotation> clazz = (Class<? extends Annotation>) bundle.loadClass(className);
 				return ContextInjectionFactory.invoke(object, clazz, context);
 			}
 			return new IllegalArgumentException("Bundle '"+bundlename+"' is not known");
 		}
 		
 		private Bundle getBundle(String bundlename) {
 			if( packageAdmin == null ) {
 				Bundle bundle =  FrameworkUtil.getBundle(getClass());
 				BundleContext context = bundle.getBundleContext();
 				ServiceReference<PackageAdmin> reference = context.getServiceReference(PackageAdmin.class);
 				packageAdmin = context.getService(reference);
 			}
 			
 			Bundle[] bundles = packageAdmin.getBundles(bundlename, null);
 			if (bundles == null)
 				return null;
 			// Return the first bundle that is not installed or uninstalled
 			for (int i = 0; i < bundles.length; i++) {
 				if ((bundles[i].getState() & (Bundle.INSTALLED | Bundle.UNINSTALLED)) == 0) {
 					return bundles[i];
 				}
 			}
 			return null;
 		}
 	}
 	
 	public static class ServiceProvider {
 		private IEclipseContext context;
 		
 		public ServiceProvider(IEclipseContext context) {
 			this.context = context;
 		}
 		
 		public Object getStyleEngine() {
 			return context.get(IStylingEngine.class);
 		}
 		
 		public Object getPartService() {
 			return context.get("org.eclipse.e4.ui.workbench.modeling.EPartService");
 		}
 		
 		public Object getModelService() {
 			return context.get("org.eclipse.e4.ui.workbench.modeling.EModelService");
 		}
 	}
 	
 	public static class Logger {
 		private Shell parentShell;
 		
 		private Shell shell;
 		private Text text;
 		
 		private static SimpleDateFormat DATEFORMAT = new SimpleDateFormat("hh:mm:ss.SSS");
 		
 		public Logger(Shell parentShell) {
 			this.parentShell = parentShell;
 		}
 		
 		public void openLog() {
 			if( shell == null ) {
 				shell = new Shell(parentShell,SWT.SHELL_TRIM);
 				shell.setLayout(new GridLayout());
 				text = new Text(shell, SWT.MULTI|SWT.BORDER|SWT.V_SCROLL|SWT.H_SCROLL);
 				text.setLayoutData(new GridData(GridData.FILL_BOTH));
 				text.setFont(JFaceResources.getTextFont());
 				text.setEditable(false);
 				shell.setVisible(true);
 				shell.addDisposeListener(new DisposeListener() {
 					
 					@Override
 					public void widgetDisposed(DisposeEvent e) {
 						shell = null;
 						text = null;
 					}
 				});
 			}
 		}
 		
 		public void error(Object data) throws Exception {
 			_log(1, data);
 		}
 		
 		public void debug(Object data) throws Exception {
 			_log(0, data);
 		}
 		
 		private void _log(int type, Object data) throws Exception {
 			if( shell == null ) {
 				openLog();
 			}
 			shell.setVisible(true);
 			if( data instanceof Throwable ) {
 				StringWriter w = new StringWriter();
 				PrintWriter pw = new PrintWriter(w);
 				((Throwable)data).printStackTrace(pw);
 				text.append(DATEFORMAT.format(new Date()) + " - " + w + "\n");
 				pw.close();
 				w.close();
 			} else {
 				text.append(DATEFORMAT.format(new Date()) + " - " + data + "\n");	
 			}
 			
 		}
 		
 		public void clearLog() {
 			if( text != null ) {
 				text.setText("");
 			}
 		}
 		
 		public void closeLog() {
 			shell.dispose();
 			shell = null;
 			text = null;
 		}
 	}
 	
 	public static class SWTSupport {
 		private Display d;
 		
 		public static SWT SWT = new SWT();
 		
 		public SWTSupport(Display d) {
 			this.d = d;
 		}
 		
 		public Color newColor(String color) {
 			if( color.startsWith("#") ) {
 				if( color.length() == 7 ) {
 					return new Color(d, new RGB(
 							Integer.parseInt(color.substring(1,3), 16),
 							Integer.parseInt(color.substring(3,5), 16),
 							Integer.parseInt(color.substring(5,7), 16)));
 				} else {
 					return new Color(d, new RGB(
 							Integer.parseInt( color.charAt(1) + "" +color.charAt(1), 16),
 							Integer.parseInt( color.charAt(2) + "" +color.charAt(2), 16),
 							Integer.parseInt( color.charAt(3) + "" +color.charAt(3), 16)));
 				}
 			}
 			return null;
 		}
 		
 		public Text newText(Composite parent, int style) {
 			return new Text(parent, style);
 		}
 		
 		public Widget newLabel(Composite parent, int style) {
 			return new Label(parent, style);
 		}
 		
 		public GridData newGridData() {
 			return new GridData();
 		}
 		
 		public Combo newCombo(Composite parent, int style) {
 			return new Combo(parent, style);
 		}
 	}
 }
