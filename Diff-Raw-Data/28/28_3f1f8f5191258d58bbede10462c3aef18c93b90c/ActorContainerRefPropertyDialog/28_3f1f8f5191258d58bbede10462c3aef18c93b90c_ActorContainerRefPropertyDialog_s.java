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
 import org.eclipse.etrice.core.room.ActorClass;
 import org.eclipse.etrice.core.room.ActorContainerClass;
 import org.eclipse.etrice.core.room.ActorContainerRef;
 import org.eclipse.etrice.core.room.ActorRef;
 import org.eclipse.etrice.core.room.LogicalSystem;
 import org.eclipse.etrice.core.room.RoomPackage;
 import org.eclipse.etrice.core.room.StructureClass;
 import org.eclipse.etrice.core.room.SubSystemClass;
 import org.eclipse.etrice.core.room.SubSystemRef;
 import org.eclipse.etrice.ui.common.dialogs.AbstractPropertyDialog;
 import org.eclipse.etrice.ui.structure.Activator;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.xtext.resource.IEObjectDescription;
 import org.eclipse.xtext.scoping.IScope;
 
 public class ActorContainerRefPropertyDialog extends AbstractPropertyDialog {
 	
 	class NameValidator implements IValidator {
 
 		@Override
 		public IStatus validate(Object value) {
 			if (value instanceof String) {
 				String name = (String) value;
 				
 				if (name.isEmpty())
 					return ValidationStatus.error("name must not be empty");
 				
 				// TODOHRR: check valid identifier
 				// TODOHRR: use ValidationUtil
 				
 				if (sc instanceof ActorClass) {
 					if (nameExists((ActorClass) sc, name))
 						return ValidationStatus.error("name already exists");
 				}
 				else if (sc instanceof SubSystemClass) {
 					SubSystemClass ssc = (SubSystemClass) sc;
 					for (ActorRef ar : ssc.getActorRefs()) {
 						if (ar!=ref && ar.getName().equals(name))
 							return ValidationStatus.error("name already exists");
 					}
 				}
 				else if (sc instanceof LogicalSystem) {
 					LogicalSystem ssc = (LogicalSystem) sc;
 					for (SubSystemRef ssr : ssc.getSubSystems()) {
 						if (ssr!=ref && ssr.getName().equals(name))
 							return ValidationStatus.error("name already exists");
 					}
 				}
 				else {
 					assert(false): "unexpected type";
 				}
 				return Status.OK_STATUS;
 			}
 			return Status.OK_STATUS;
 		}
 
 		private boolean nameExists(ActorClass ac, String name) {
 			for (ActorRef ar : ac.getActorRefs()) {
 				if (ar!=ref && ar.getName().equals(name))
 					return true;
 			}
 			if (ac.getBase()!=null)
 				return nameExists(ac.getBase(), name);
 			
 			return false;
 		}
 	}
 	
 	class ProtocolValidator implements IValidator {
 
 		@Override
 		public IStatus validate(Object value) {
 			if (value==null)
 				return ValidationStatus.error("select a class");
 			
 			return Status.OK_STATUS;
 		}
 	}
 
 	private ActorContainerRef ref;
 	private IScope scope;
 	private StructureClass sc;
 	private boolean newRef;
 
 	public ActorContainerRefPropertyDialog(Shell shell, ActorContainerRef ref, IScope scope, StructureClass sc, boolean newRef) {
 		super(shell, "Edit Reference");
 		this.ref = ref;
 		this.scope = scope;
 		this.sc = sc;
 		this.newRef = newRef;
 	}
 
 	@Override
 	protected void initializeBounds() {
		getShell().setSize(500, 300);
 	}
 
 	@Override
 	protected void createContent(IManagedForm mform, Composite body,
 			DataBindingContext bindingContext) {
 
 		NameValidator nv = new NameValidator();
 		ProtocolValidator pv = new ProtocolValidator();
 
 		boolean isActor = sc instanceof ActorContainerClass;
 
 		ArrayList<IEObjectDescription> actors = new ArrayList<IEObjectDescription>();
         Iterator<IEObjectDescription> it = scope.getAllElements().iterator();
         while (it.hasNext()) {
         	IEObjectDescription desc = it.next();
         	EObject obj = desc.getEObjectOrProxy();
         	if (isActor && obj instanceof ActorClass)
         		actors.add(desc);
         	if (!isActor && obj instanceof SubSystemClass)
         		actors.add(desc);
 		}
 		
 		Text name = createText(body, "Name:", ref, RoomPackage.eINSTANCE.getActorContainerRef_Name(), nv);
 		Combo refClass = isActor?
 				createComboUsingDesc(body, "Actor Class:", ref, ActorClass.class, RoomPackage.eINSTANCE.getActorRef_Type(), actors, RoomPackage.eINSTANCE.getRoomClass_Name(), pv)
 			:	createComboUsingDesc(body, "SubSystem Class:", ref, SubSystemClass.class, RoomPackage.eINSTANCE.getSubSystemRef_Type(), actors, RoomPackage.eINSTANCE.getRoomClass_Name(), pv);
 
 		createDecorator(name, "invalid name");
 		createDecorator(refClass, "no class selected");
 
 		if (!newRef) {
 			refClass.setEnabled(false);
 		}
 
 		name.selectAll();
 		name.setFocus();
 	}
 
 	@Override
 	protected Image getImage() {
 		return Activator.getImage("icons/Structure.gif");
 	}
 
 }
