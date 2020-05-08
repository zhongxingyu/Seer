 /*-
  * Copyright © 2009 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.gda.views.baton.dialogs;
 
 import gda.jython.InterfaceProvider;
 import gda.jython.batoncontrol.ClientDetails;
 import gda.rcp.GDAClientActivator;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.gda.preferences.PreferenceConstants;
 import uk.ac.gda.views.baton.BatonView;
 
 /**
  *
  */
 public class BatonRequestDialog extends Dialog {
 	
 	private static final Logger logger = LoggerFactory.getLogger(BatonRequestDialog.class);
 	
 	private Label lblARequestFor;
 	private ClientDetails request;
	private boolean open = true;
 
 	/**
 	 * Create the dialog.
 	 * @param parentShell
 	 */
 	private BatonRequestDialog(Shell parentShell, final ClientDetails request) {
 		super(parentShell);
 		this.request = request;
 	}
 
 	/**
 	 * Create contents of the dialog.
 	 * @param parent
 	 */
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		
 		getShell().setText(String.format("%s has requested the baton", request.getUserID()));
 		
 		Composite container = (Composite) super.createDialogArea(parent);
 		{
 			lblARequestFor = new Label(container, SWT.NONE);
 			lblARequestFor.setText("The user '"+request.getUserID()+"' has requested the baton.\n\n"+
 			                       "Would you like to pass control to them?");
 		}
 
 		return container;
 	}
 
 	/**
 	 * Create contents of the button bar.
 	 * @param parent
 	 */
 	@Override
 	protected void createButtonsForButtonBar(Composite parent) {
 		createButton(parent, IDialogConstants.OK_ID, "Yes", true);
 		createButton(parent, IDialogConstants.CANCEL_ID, "No", false);
 	}
 
 	/**
 	 * Return the initial size of the dialog.
 	 */
 	@Override
 	protected Point getInitialSize() {
 		return new Point(450, 177);
 	}
 	
 	public static void openPassBatonForm(final Shell shell, final ClientDetails request, boolean batonAlwaysHeld) {
 				
 		final BatonRequestDialog currentOpenDialog = new BatonRequestDialog(shell, request);
 		
 		if (!batonAlwaysHeld) { // We close it in 2 minutes.
 			Job job = new Job("Automatically accepting baton change.") {
 				@Override
 				protected IStatus run(IProgressMonitor monitor) {
  					
					if (!currentOpenDialog.isOpen()) {
 						return new Status(IStatus.OK, BatonView.ID, "Choosing ok for the user.");
 					}
 					try {
 						
 						final Shell shell = currentOpenDialog.getShell();
 						if (shell==null) return new Status(IStatus.OK, BatonView.ID, "Choosing ok for the user.");
 						shell.getDisplay().asyncExec(new Runnable() {
 							@Override
 							public void run() {
 								shell.dispose();
 							}
 						});
 						return new Status(IStatus.OK, BatonView.ID, "Choosing ok for the user.");
 						
 					} catch (Exception ne) {
 						logger.error("Cannot process dialog kill", ne);
 						return  new Status(IStatus.ERROR, BatonView.ID, "Choosing ok for the user.");
 					} finally {
 	 					doPass(request, true);
 					}
 				}
 			};
 			job.setUser(false);
 			job.setSystem(true);
 			job.setThread(Display.getDefault().getThread());
 			
 			final int minutes = GDAClientActivator.getDefault().getPreferenceStore().getInt(PreferenceConstants.BATON_REQUEST_TIMEOUT);
 			job.schedule(minutes*60*1000);// 2 minutes.
 		}
  		
 		final int returnCode = currentOpenDialog.open();
		currentOpenDialog.setOpen(false);
 		doPass(request, returnCode == OK);
 
 	}
 
 	private static void doPass(final ClientDetails request, boolean ok) {
 		if (ok) {
 			InterfaceProvider.getBatonStateProvider().assignBaton(request.getIndex());
 		} else {
 			InterfaceProvider.getBatonStateProvider().sendMessage("Baton request denied.");
 		}
 	}
 
	public boolean isOpen() {
		return open;
 	}
 
	public void setOpen(boolean open) {
		this.open = open;
 	}
 }
