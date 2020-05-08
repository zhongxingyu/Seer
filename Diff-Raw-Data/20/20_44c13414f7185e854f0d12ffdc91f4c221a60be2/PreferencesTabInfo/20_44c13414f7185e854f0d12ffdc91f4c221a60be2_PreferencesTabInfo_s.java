 /*******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation
 
 *******************************************************************************/
 
 package org.eclipse.imp.prefspecs.pageinfo;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.imp.preferences.IPreferencesService;
 
 
 public class PreferencesTabInfo
 {
 	private PreferencesPageInfo parent = null;
 	private String name = null;
 	
 	private List<ConcreteFieldInfo> concreteFields = new ArrayList<ConcreteFieldInfo>();
 	
 	/**
 	 * Can the fields on this tab be edited by default?
 	 */
 	private boolean isEditable = true;
 	
 	/**
 	 * Can the values of fields on this tab be removed by default?
 	 */
 	private boolean isRemovable = true;
 	
 	/**
 	 * Does the tab appear on the preferences page?
 	 */
 	private boolean isUsed = true;
 
 
 	public PreferencesTabInfo(PreferencesPageInfo parent, String name)
 	{
 		// All tabInfos must have a parentInfo
 		if (parent == null) {
 			throw new IllegalArgumentException(
				"PreferencesIabInfo(..):  page is null; not allowed");		
 		}
 		
 		// All tabInfos must have a name that is a valid tab name
 		if (name == null) {
 			throw new IllegalArgumentException(
				"PreferencesIabInfo(..):  name is null; not allowed");		
 		}
 		String[] levels = IPreferencesService.levels;
 		boolean nameOK = false;
 		for (int i = 0; i < levels.length; i++) {
 			if (name.equals(levels[i])) {
 				nameOK = true;
 				break;
 			}
 		}
 		if (!nameOK) {
 			throw new IllegalArgumentException(
				"PreferencesIabInfo.setName(..):  name = '" + name + "' does not correspond to a legal tab name");
 		}
 
 		// All tabInfos must represent a unique tab within their parentInfo
 		if (parent.hasTabInfo(name))  {
 			throw new IllegalArgumentException(
				"PreferencesIabInfo.setName(..):  name = '" + name + "' represents a duplicate tab; not allowed");
 		}
 		
 		// Okay
 		this.parent = parent;
 		parent.addTabInfo(this);
 		this.name = name;
 	}
 	
 	
 	// TODO:  Consider adding other constructors to enable attribute values to be set
 	
 	//
	// Parent and name are only defined through the constuctor,
 	// so only "get" methods are defined for those
 	//
 	
 	
 	public PreferencesPageInfo getParent() {
 		return parent;
 	}
 	
 	
 	public String getName() {
 		return name;
 	}
 	
 
 	
 
 	public void setIsEditable(boolean isEditable) {
		this.isEditable = isEditable	;
 	}
 	
 	public boolean getIsEditable() {
 		return 	isEditable;
 	}
 
 	/**
 	 * Constraint:  For the default tab "isRemovable" must be false
 	 * 
 	 * @param isRemovable
 	 */
 	public void setIsRemovable(boolean isRemovable) {
 		if (getName().equals(IPreferencesService.DEFAULT_LEVEL))
 			if (isRemovable) {
 				throw new IllegalArgumentException(
					"PreferencesIabInfo.setIsRemovable(..):  cannot set isRemovable ");
 			}
 		this.isRemovable = isRemovable;
 	}
 	
 	
 	/**
 	 * Whether the values of fields on this tab can, by default, be
 	 * removed.  Removal of a field value on one tab triggers inheritance
 	 * of the value from the corresponding field on the next higher level.
 	 * Individual fields may override this setting, except that it is
 	 * always false on the default tab (from which there is no higher tab).
 	 * 
 	 * @return	False for the default tab; the set value of isUsed otherwise
 	 */
 	public boolean getIsRemovable() {
 		if (getName().equals(IPreferencesService.DEFAULT_LEVEL))
 			return false;
 		return isRemovable;
 	}
 	
 	
 	public void setIsUsed(boolean isUsed) {
 		this.isUsed = isUsed;
 	}
 	
 	public boolean getIsUsed() {
 		return isUsed;
 	}
 	
 	
 	//
 	// For working with concrete fields of the tab
 	//
 	
 	
 	public void add(ConcreteFieldInfo field) {
 		// TODO:  add check for default tab that field !isRemovable
 		if (field == null || concreteFields.contains(field))
 			return;
 		concreteFields.add(field);
 	}
 	
 	public void removeConcreteField(ConcreteFieldInfo vField) {
 		if (vField == null)
 			return;
 		concreteFields.remove(vField);
 	}
 	
 	public Iterator<ConcreteFieldInfo> getConcreteFields() {
 		return concreteFields.iterator(); 
 	}
 	
 	
 	//
 	// For reporting on the contents of the tab
 	//
 	
 	public void dump(String prefix) {
 		String indent = prefix + "  ";
 		
 		System.out.println(prefix + "Tab '" + getName() + "'");
 		System.out.println(indent + "isUsed = " + getIsUsed());
 		System.out.println(indent + "isEditable = " + getIsEditable());
 		System.out.println(indent + "isRemovable = " + getIsRemovable());
 		System.out.println(indent + "Concrete fields:");
 
 		for(ConcreteFieldInfo field: concreteFields) {
 			field.dump(indent + " ");	
 		}
 	}
 }
