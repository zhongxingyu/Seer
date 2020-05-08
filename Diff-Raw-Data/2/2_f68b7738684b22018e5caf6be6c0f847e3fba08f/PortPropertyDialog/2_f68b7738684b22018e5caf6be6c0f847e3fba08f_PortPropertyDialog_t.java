 /*******************************************************************************
  * Copyright (c) 2010 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * CONTRIBUTORS:
  * 		Thomas Schuetz and Henrik Rentz-Reichert (initial contribution)
  * 
  *******************************************************************************/
 
 package org.eclipse.etrice.ui.structure.dialogs;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.validation.IValidator;
 import org.eclipse.core.databinding.validation.ValidationStatus;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.etrice.core.validation.ValidationUtil;
 import org.eclipse.etrice.core.validation.ValidationUtil.Result;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.xtext.resource.IEObjectDescription;
 import org.eclipse.xtext.scoping.IScope;
 
 import org.eclipse.etrice.core.room.ActorClass;
 import org.eclipse.etrice.core.room.ActorContainerClass;
 import org.eclipse.etrice.core.room.ExternalPort;
 import org.eclipse.etrice.core.room.Port;
 import org.eclipse.etrice.core.room.ProtocolClass;
 import org.eclipse.etrice.core.room.RoomFactory;
 import org.eclipse.etrice.core.room.RoomPackage;
 import org.eclipse.etrice.core.room.SubSystemClass;
 import org.eclipse.etrice.ui.common.dialogs.AbstractPropertyDialog;
 import org.eclipse.etrice.ui.structure.Activator;
 
 public class PortPropertyDialog extends AbstractPropertyDialog {
 
 	class NameValidator implements IValidator {
 
 		@Override
 		public IStatus validate(Object value) {
 			if (value instanceof String) {
 				String name = (String) value;
 				
 				Result result = ValidationUtil.isUniqueName(port, name);
 				if (!result.isOk())
 					return ValidationStatus.error(result.getMsg());
 			}
 			return Status.OK_STATUS;
 		}
 	}
 	
 	class ProtocolValidator implements IValidator {
 
 		@Override
 		public IStatus validate(Object value) {
 			if (value==null)
 				return ValidationStatus.error("select a protocol");
 			
 			return Status.OK_STATUS;
 		}
 	}
 	
 	class MultiplicityValidator implements IValidator {
 
 		private boolean mayChange;
 		private int old;
 
 		public MultiplicityValidator(boolean mayChange, int old) {
 			this.mayChange = mayChange;
 			this.old = old;
 		}
 
 		@Override
 		public IStatus validate(Object value) {
 			if (value instanceof Integer) {
 				int m = (Integer) value;
 				if (m<=0)
 					return ValidationStatus.error("multiplicity must be positive");
 				if (!mayChange) {
 					if (old==1 && m>1)
 						return ValidationStatus.error("cannot make connected port replicated");
 					if (old>1 && m==1)
 						return ValidationStatus.error("cannot make connected port not replicated");
 				}
 			}
 			return Status.OK_STATUS;
 		}
 	}
 	
 	private Port port;
 	private IScope scope;
 	private ActorContainerClass acc;
 	private boolean newPort;
 	private boolean refitem;
 	private boolean internal;
 	private Button relayCheck = null;
 	private boolean relay;
 
 	public PortPropertyDialog(Shell shell, Port port, IScope scope, ActorContainerClass acc, boolean newPort, boolean refitem, boolean internal) {
 		super(shell, "Edit Port");
 		this.port = port;
 		this.scope = scope;
 		this.acc = acc;
 		this.newPort = newPort;
 		this.refitem = refitem;
 		this.internal = internal;
 		
 		relay = isPortRelay();
 	}
 
 	private boolean isPortRelay() {
 		if (internal)
 			return false;
 		
 		if (acc instanceof SubSystemClass)
 			return true;
 		else if (acc instanceof ActorClass) {
 			for (ExternalPort xp : ((ActorClass) acc).getExtPorts()) {
 				if (xp.getIfport()==port)
 					return false;
 			}
 			return true;
 		}
 		
 		return false;
 	}
 
 	@Override
 	protected void initializeBounds() {
 		getShell().setSize(500, 300);
 	}
 	
 	@Override
 	protected void createContent(IManagedForm mform, Composite body, DataBindingContext bindingContext) {
 		boolean connected = ValidationUtil.isConnected(port, null, acc);
 		NameValidator nv = new NameValidator();
 		ProtocolValidator pv = new ProtocolValidator();
 		MultiplicityValidator mv = new MultiplicityValidator(newPort || !connected, port.getMultiplicity());
 
 		ArrayList<IEObjectDescription> protocols = new ArrayList<IEObjectDescription>();
         Iterator<IEObjectDescription> it = scope.getAllElements().iterator();
         while (it.hasNext()) {
         	IEObjectDescription desc = it.next();
         	EObject obj = desc.getEObjectOrProxy();
         	if (obj instanceof ProtocolClass)
         		protocols.add(desc);
 		}
 		
 		Text name = createText(body, "Name:", port, RoomPackage.eINSTANCE.getInterfaceItem_Name(), nv);
 		Combo protocol = createComboUsingDesc(body, "Protocol:", port, ProtocolClass.class, RoomPackage.eINSTANCE.getInterfaceItem_Protocol(), protocols, RoomPackage.eINSTANCE.getRoomClass_Name(), pv);
 		Button conj = createCheck(body, "Conjugated:", port, RoomPackage.eINSTANCE.getPort_Conjugated());
 		if (!internal && !refitem && (acc instanceof ActorClass))
 			createRelayCheck(body, mform.getToolkit());
 		Text multi = createText(body, "Multiplicity:", port, RoomPackage.eINSTANCE.getPort_Multiplicity(), mv);
 		
 		if (!newPort) {
 			// TODOHRR: check whether port is used externally?
 			if (connected) {
 				protocol.setEnabled(false);
 				conj.setEnabled(false);
 				if (port.getMultiplicity()==1)
 					multi.setEnabled(false);
 			}
 			
 			if (refitem) {
 				name.setEnabled(false);
 				protocol.setEnabled(false);
 				conj.setEnabled(false);
 				multi.setEnabled(false);
 			}
 		}
 		
 		createDecorator(name, "invalid name");
 		createDecorator(protocol, "no protocol selected");
 		createDecorator(multi, "multiplicity must be greater 1");
 		
 		name.selectAll();
 		name.setFocus();
 	}
 
 	private void createRelayCheck(Composite parent, FormToolkit toolkit) {
 		Label l = toolkit.createLabel(parent, "Is Relay Port:", SWT.NONE);
 		l.setLayoutData(new GridData(SWT.NONE));
 		
 		relayCheck = toolkit.createButton(parent, "", SWT.CHECK);
 		relayCheck.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		relayCheck.setSelection(relay);
 		
 		if (ValidationUtil.isConnected(port, null, acc))
 			relayCheck.setEnabled(false);
 	}
 	
 	@Override
 	protected void okPressed() {
 		if (relayCheck!=null) {
 			if (relay!=relayCheck.getSelection()) {
 				relay = relayCheck.getSelection();
 				
 				// we know it's an ActorClass if we created the relayCheck in the first place
 				ActorClass ac = (ActorClass) acc;
 				
 				if (relay) {
 					for (ExternalPort xp : ac.getExtPorts()) {
 						if (xp.getIfport()==port) {
 							ac.getExtPorts().remove(xp);
 							break;
 						}
 					}
 				}
 				else {
 					ExternalPort xp = RoomFactory.eINSTANCE.createExternalPort();
 					xp.setIfport(port);
 					ac.getExtPorts().add(xp);
 				}
 			}
 		}
 		
 		super.okPressed();
 	}
 
 	@Override
 	protected Image getImage() {
 		return Activator.getImage("icons/Structure.gif");
 	}
 }
