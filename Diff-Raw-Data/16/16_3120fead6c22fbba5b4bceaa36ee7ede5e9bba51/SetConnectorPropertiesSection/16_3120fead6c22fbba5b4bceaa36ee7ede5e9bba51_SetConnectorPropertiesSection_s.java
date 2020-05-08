 /**
 * Copyright (C) 2013  Luc Hermans
  * 
  * This program is free software: you can redistribute it and/or modify it under the terms of the
  * GNU General Public License as published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with this program.  If
  * not, see <http://www.gnu.org/licenses/>.
  * 
  * Contact information: kozzeluc@gmail.com.
  */
 package org.lh.dmlj.schema.editor.property.section;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.gef.commands.Command;
 import org.lh.dmlj.schema.Connector;
 import org.lh.dmlj.schema.SchemaPackage;
 import org.lh.dmlj.schema.editor.command.SetObjectAttributeCommand;
 import org.lh.dmlj.schema.editor.property.handler.IEditHandler;
 
 public class SetConnectorPropertiesSection extends AbstractSetPropertiesSection {
 
 	public SetConnectorPropertiesSection() {
 		super();
 	}
 	
 	@Override
 	protected EObject getAttributeOwner(EAttribute attribute) {
 		if (attribute == SchemaPackage.eINSTANCE.getConnector_Label()) {
 			return target.getConnectionParts().get(0).getConnector();
 		} else {
 			return super.getAttributeOwner(attribute);
 		}
 	}
 
 	@Override
 	public List<EAttribute> getAttributes() {
 		List<EAttribute> attributes = new ArrayList<>();
 		attributes.add(SchemaPackage.eINSTANCE.getConnector_Label());
 		return attributes;
 	}	
 	
 	@Override
 	public EObject getEditableObject(EAttribute attribute) {
 		if (attribute == SchemaPackage.eINSTANCE.getConnector_Label()) {
 			// although there will always be 2 connectors involved, capturing
 			// the model changes from the first will do
 			return target.getConnectionParts().get(0).getConnector();
 		}
 		return super.getEditableObject(attribute);
 	}
 	
 	@Override
 	public IEditHandler getEditHandler(EAttribute attribute, Object newValue) {
 		
 		if (attribute != SchemaPackage.eINSTANCE.getConnector_Label()) {		
 			return super.getEditHandler(attribute, newValue);
 		}
 		
 		// we need to set the label on both connectors, so go grab them (they 
 		// should always be there)
 		Connector[] connector = new Connector[2];
 		connector[0] = target.getConnectionParts().get(0).getConnector();
 		connector[1] = target.getConnectionParts().get(1).getConnector();
 		
 		// create 2 SetStringAttributeCommands to set the new label
 		String value = (String) newValue;
 		String label = getLabel(attribute);
 		Command[] command = new Command[2];
 		command[0] = new SetObjectAttributeCommand(connector[0], attribute, 
 												   value, label);
 		command[1] = new SetObjectAttributeCommand(connector[1], attribute, 
 												   value, label);
 		
		// chain the 2 commands and wrap the result in an IEditHandler
		final Command theCommand = command[0].chain(command[1]);		
 		return new IEditHandler() {	
 			@Override
 			public Command getEditCommand() {
				return theCommand;
 			}
 			@Override
 			public String getMessage() {
 				return null;
 			}
 			@Override
 			public boolean isValid() {
 				return true;
 			}
 		};
 	}	
 
 }
