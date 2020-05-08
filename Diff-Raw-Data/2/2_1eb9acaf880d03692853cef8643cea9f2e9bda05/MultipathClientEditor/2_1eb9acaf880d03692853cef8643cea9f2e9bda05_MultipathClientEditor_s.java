 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - App
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.app.multipath;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.EditorPart;
 
 import de.tuilmenau.ics.fog.application.Application;
 import de.tuilmenau.ics.fog.application.observer.ApplicationEvent;
 import de.tuilmenau.ics.fog.application.observer.ApplicationEventConnectError;
 import de.tuilmenau.ics.fog.application.observer.ApplicationEventExit;
 import de.tuilmenau.ics.fog.application.observer.IApplicationEventObserver;
 import de.tuilmenau.ics.fog.eclipse.ui.EditorRowComposite;
 import de.tuilmenau.ics.fog.eclipse.ui.dialogs.MessageBoxDialog;
 import de.tuilmenau.ics.fog.eclipse.ui.editors.EditorInput;
 import de.tuilmenau.ics.fog.eclipse.utils.*;
 import de.tuilmenau.ics.fog.facade.NetworkException;
 import de.tuilmenau.ics.fog.facade.RequirementsException;
 import de.tuilmenau.ics.fog.facade.RoutingException;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 /**
  * Editor showing the internal parameters of a stream client.
  * Furthermore, they can be changed.
  */
 public class MultipathClientEditor extends EditorPart implements IApplicationEventObserver
 {
 	public MultipathClientEditor()
 	{
		Logging.log(this, "Created relay client");
 		mDisplay = Display.getCurrent();
 		mShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
 	}
 	
 	@Override
 	public void init(IEditorSite pSite, IEditorInput pInput) throws PartInitException
 	{
 		setSite(pSite);
 		setInput(pInput);
 		
 		// get selected object to show in editor
 		Object tSelection = null;
 		if (pInput instanceof EditorInput) {
 			tSelection = ((EditorInput) pInput).getObj();
 		}
 
 		Logging.log(this, "init relay client for " + tSelection + " (class=" + tSelection.getClass() +")");
 
 		mMultipathClient = (MultipathClient) pInput.getAdapter(MultipathClient.class);
 		if(mMultipathClient == null) {
 			throw new PartInitException(pInput +" does not provide a valid input for " + this);
 		}
 		
 		// add as observer for corresponding stream client application
 		mMultipathClient.addObserver(this);
 		
 		// update title of editor
 		setPartName(mMultipathClient.toString());		
 	}
 
 	@Override
 	public void dispose()
 	{
 		Logging.log(this, "Destroyed relay client");
 
 		if (mMultipathClient != null) {
 			// delete as observer for corresponding stream client application
 			mMultipathClient.deleteObserver(this);
 			
 			// terminate application
 			mMultipathClient.exit();
 		}
 		
 		super.dispose();
 	}
 	
 	/**
 	 * Called by application in case of new events for observer.
 	 */
 	@Override
 	public void handleEvent(Application pApplication, ApplicationEvent pEvent) 
 	{
 		Logging.log(this, "Got update event " +pEvent +" from " + pApplication);
 		
 		if(pEvent instanceof ApplicationEventConnectError) {
 			ApplicationEventConnectError tEvConErr = (ApplicationEventConnectError)pEvent;
 			NetworkException tExc = tEvConErr.getNetworkEception();
 			
 			if(tExc instanceof RoutingException) {
 				MessageBoxDialog.open(getSite().getShell(), "Routing error", "The routing wasn't able to find a path, message is " + tExc.getMessage(), SWT.ICON_ERROR);
 			}
 			else if(tExc instanceof RequirementsException) { 
 				MessageBoxDialog.open(getSite().getShell(), "Requirements error", "The given requirements \"" + ((RequirementsException)tExc).getRequirements() + "\" for the connection couldn't be fullfilled.", SWT.ICON_ERROR);
 			}
 			else {
 				MessageBoxDialog.open(getSite().getShell(), "Error", "Error: " +tExc.getMessage(), SWT.ICON_ERROR);
 			}
 		}
 		
 		if(pEvent instanceof ApplicationEventExit) {
 			EditorUtils.closeEditor(getSite(), this);
 		}
 	}
 
 	@Override
 	public void doSave(IProgressMonitor monitor)
 	{
 	}
 
 	@Override
 	public void doSaveAs()
 	{
 	}
 
 	@Override
 	public boolean isDirty()
 	{
 		return false;
 	}
 
 	@Override
 	public boolean isSaveAsAllowed()
 	{
 		return false;
 	}
 	
 	public void updateOutput() {
 		int tCurSendBytes = mMultipathClient.countSentSctpBytes();
 		int tCurSendPackets = mMultipathClient.countSentSctpPackets();
 		int tCurRecvBytes = mMultipathClient.countReceivedSctpBytes();
 		int tCurRecvPackets = mMultipathClient.countReceivedSctpPackets();
 		int tCurListenerPort = mMultipathClient.getListenerPort();
 		int tCurHighPrioBytes = mMultipathClient.countHighPrioritySctpBytes();
 		int tCurHighPrioPackets = mMultipathClient.countHighPrioritySctpPackets();
 		int tCurLowPrioBytes = mMultipathClient.countLowPrioritySctpBytes();
 		int tCurLowPrioPackets = mMultipathClient.countLowPrioritySctpPackets();
 		int tCurKnownSctpStreams = mMultipathClient.countKnownSctpStreams();
 		
 		mLbSentBytes.setText(Integer.toString(tCurSendBytes) + " bytes");
 		mLbSentPackets.setText(Integer.toString(tCurSendPackets) + " packets");
 		mLbReceivedBytes.setText(Integer.toString(tCurRecvBytes) + " bytes");
 		mLbReceivedPackets.setText(Integer.toString(tCurRecvPackets) + " packets");
 		
 		mLbIpListener.setText(Integer.toString(tCurListenerPort));
 		mLbIpDestination.setText(mMultipathClient.getIpDestination());
 		
 		mLbKnownSctpStreams.setText(Integer.toString(tCurKnownSctpStreams));
 		mLbHighPrioBytes.setText(Integer.toString(tCurHighPrioBytes) + " bytes");
 		mLbHighPrioPackets.setText(Integer.toString(tCurHighPrioPackets) + " packets");
 		mLbLowPrioBytes.setText(Integer.toString(tCurLowPrioBytes) + " bytes");
 		mLbLowPrioPackets.setText(Integer.toString(tCurLowPrioPackets) + " packets");
 	}
 
 	@Override
 	public void createPartControl(Composite pParent)
 	{
 		Logging.log(this, "Creating relay client GUI for " + mMultipathClient.toString());
 
 		EditorRowComposite tGrp = new EditorRowComposite(pParent, SWT.SHADOW_NONE);
 
 		Label tTitle = tGrp.createRow("Multipath client:", "");
 		mLbSentBytes = tGrp.createRow("Sent SCTP stream: ", " -1 bytes");
 		mLbSentPackets = tGrp.createRow("Sent SCTP data: ", "-1 packets");
 		mLbReceivedBytes = tGrp.createRow("Received SCTP stream: ", " -1 bytes");
 		mLbReceivedPackets = tGrp.createRow("Received SCTP data: ", "-1 packets");
 		
 		mLbIpListener = tGrp.createRow("Receiver on UDP port: ", "N/A");
 		mLbIpDestination = tGrp.createRow("IP destination: ", "N/A");
 
 		Label tSpacer = tGrp.createRow("", "");
 		Label tPrioTitle = tGrp.createRow("SCTP packets sent via high/low priority path:", "");
 		mLbKnownSctpStreams = tGrp.createRow("Known SCTP streams: ", "0");
 		mLbHighPrioBytes = tGrp.createRow("High priority stream: ", " -1 bytes");
 		mLbHighPrioPackets = tGrp.createRow("High priority data: ", "-1 packets");
 		mLbLowPrioBytes = tGrp.createRow("Low priority stream: ", " -1 bytes");
 		mLbLowPrioPackets = tGrp.createRow("Low priority data: ", "-1 packets");
 
 		mDisplay.timerExec(1000, mRepaintTimer);
 	}
 	
 	@Override
 	public void setFocus()
 	{
 	}
 	
 	private MultipathClient mMultipathClient = null;
 	private Display mDisplay = null;
 	private Shell mShell = null;
 	private Label mLbSentBytes = null;
 	private Label mLbSentPackets = null;
 	private Label mLbReceivedBytes = null;
 	private Label mLbReceivedPackets = null;
 	private Label mLbHighPrioBytes = null;
 	private Label mLbHighPrioPackets = null;
 	private Label mLbLowPrioBytes = null;
 	private Label mLbLowPrioPackets = null;
 	private Label mLbIpListener = null;
 	private Label mLbIpDestination = null;
 	private Label mLbKnownSctpStreams = null;
 	
 	private MultipathClientEditor mMPClientEditor = this;
 	private Runnable mRepaintTimer = new Runnable () {
 		public void run () {
 			if (mShell.isDisposed()) 
 				return;
 			if (mMPClientEditor == null)
 				return;
 			mMPClientEditor.updateOutput();
 			mDisplay.timerExec (400, this);
 		}
 	};
 }
 
