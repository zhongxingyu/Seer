 package org.eclipse.jst.server.generic.ui.internal;
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jst.server.generic.core.internal.GenericServerRuntime;
 import org.eclipse.jst.server.generic.core.internal.Trace;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.wst.server.core.internal.IInstallableRuntime;
 import org.eclipse.wst.server.core.internal.ServerPlugin;
 
 public class InstallableRuntimeDecorator implements
 		GenericServerCompositeDecorator {
 
 	private GenericServerRuntime fRuntime;
 
 	public InstallableRuntimeDecorator(GenericServerRuntime runtime) {
 		fRuntime = runtime;
 	}
 
 	public void decorate(final GenericServerComposite composite) {
 		final IInstallableRuntime ir = ServerPlugin
 				.findInstallableRuntime(fRuntime.getRuntime().getRuntimeType()
 						.getId());
 
		Button install = SWTUtil.createButton(composite, "Install Server");
 		install.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent se) {
 				DirectoryDialog dialog = new DirectoryDialog(composite
 						.getShell());
				dialog.setMessage("Select installation directory");
 				String selectedDirectory = dialog.open();
 				if (selectedDirectory != null) {
 					try {
 						ir.install(new Path(selectedDirectory),
 								new NullProgressMonitor());
 					} catch (Exception e) {
 						Trace
 								.trace(Trace.SEVERE,
										"Error installing runtime", e);
 					}
 
 				}
 			}
 		});
 	}
 
 	public boolean validate() {
 		return false;
 	}
 
 }
