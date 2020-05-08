 /*******************************************************************************
  * Copyright (c) 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.pagedesigner.commands.single;
 
 import java.util.Iterator;
 import java.util.Map;
 
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
 
 /**
  * This command is for change an single attribute of an IDOMElement. Normally
  * used by the properties view.
  * 
  * @author mengbo
  */
 public class ChangeAttributeCommand extends SingleNodeCommand {
 	private IDOMElement _element;
 
 	private String _attrValue;
 
 	private String _attrName;
 
 	private Map _attributes;
 
 	private boolean _keepEmptyAttribute = false;
 
 	/**
 	 * 
 	 * @param label
 	 * @param node
 	 * @param attrName
 	 * @param attrValue
 	 *            if null means remove the specified attribute
 	 */
 	public ChangeAttributeCommand(String label, IDOMElement node,
 			String attrName, String attrValue) {
 		super(label, node);
 		_element = node;
 		_attrName = attrName;
 		_attrValue = attrValue;
 		_attributes = null;
 	}
 
 	/** TODO: can these two constructors be merged?
 	 * @param label
 	 * @param node
 	 * @param attributes
 	 */
 	public ChangeAttributeCommand(String label, IDOMElement node, Map attributes) {
 		super(label, node);
 		_element = node;
 		_attributes = attributes;
 		_attrName = null;
 		_attrValue = null;
 	}
 
 	protected void doExecute() {
 		if (_attrName != null) {
 			updateElement(_attrName, _attrValue);
 		} else if (_attributes != null) {
 			for (Iterator iterator = _attributes.keySet().iterator(); iterator
 					.hasNext();) {
 				String name = (String) iterator.next();
 				String value = (String) _attributes.get(name);
 				if (isSameValue(value, _element.getAttribute(name))) {
 					continue;
 				}
 				updateElement(name, value);
 			}
 		}
 	}
 
 	private void updateElement(String name, String value) {
 		if (_element.hasAttribute(name) && isEmptyString(value)
 				&& !_keepEmptyAttribute) {
 			_element.removeAttribute(name);
 		}
 		if (!isEmptyString(value) || _keepEmptyAttribute) {
 			_element.setAttribute(name, value);
 		}
		//Bug 330413 - [WPE] Modifying tag attribute using property sheet doesn't notify team system of edit
		notifyTeamFrameworkOfEdit();
 	}
 
 	private boolean isSameValue(String value1, String value2) {
 		value1 = value1 == null ? "" : value1; //$NON-NLS-1$
 		value2 = value2 == null ? "" : value2; //$NON-NLS-1$
 		return value1.equals(value2);
 	}
 
 	private boolean isEmptyString(String str) {
 		if (str == null || str.equals("")) { //$NON-NLS-1$
 			return true;
 		}
         return false;
 	}
 
 	/**
 	 * @return Returns the keepEmptyAttribute.
 	 */
 	public boolean isKeepEmptyAttribute() {
 		return _keepEmptyAttribute;
 	}
 
 	/**
 	 * @param keepEmptyAttribute
 	 *            The keepEmptyAttribute to set.
 	 */
 	public void setKeepEmptyAttribute(boolean keepEmptyAttribute) {
 		this._keepEmptyAttribute = keepEmptyAttribute;
 	}
 }
