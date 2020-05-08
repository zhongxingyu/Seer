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
 package org.eclipse.jst.pagedesigner.editors.palette.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
import org.eclipse.core.runtime.Assert;
 import org.eclipse.gef.palette.PaletteDrawer;
 import org.eclipse.gef.palette.PaletteEntry;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jst.pagedesigner.IJMTConstants;
 import org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemDescriptor;
 import org.eclipse.jst.pagedesigner.utils.JSPUtil;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * @author mengbo
  */
 public class PaletteItemDescriptor implements IPaletteItemDescriptor {
 	private Map _map;
 
 	private String _id;
 
 	private String _tagName;
 
 	private String _uri;
 
 	private boolean _isVisible = true;
 
 	private String _description;
 
 	private String _label;
 
 	private ImageDescriptor _smallIcon;
 
 	private ImageDescriptor _largeIcon;
 
 	private String _smallIconString;
 
 	private String _largeIconString;
 
 	private String _prefix;
 
 	private boolean _requireHForm = false; // default to false
 
 	private PaletteEntry _entry;
 
 	private int _initState = PaletteDrawer.INITIAL_STATE_CLOSED;
 
 	private NodeList _templateSubNodes;
 
 	private boolean _jsfComponent = false;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemDescriptor#getSmallIcon()
 	 */
 	public ImageDescriptor getSmallIcon() {
 		return this._smallIcon;
 	}
 
 	public void setSmallIcon(ImageDescriptor icon) {
 		this._smallIcon = icon;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemDescriptor#getLargeIcon()
 	 */
 	public ImageDescriptor getLargeIcon() {
 		return this._largeIcon;
 	}
 
 	public void setLargeIcon(ImageDescriptor icon) {
 		this._largeIcon = icon;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemDescriptor#getURI()
 	 */
 	public String getURI() {
 		return this._uri;
 	}
 
 	public void setURI(String uri) {
 		this._uri = uri;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemDescriptor#getTagName()
 	 */
 	public String getTagName() {
 		return this._tagName;
 	}
 
 	public void setTagName(String tag) {
 		this._tagName = tag;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemDescriptor#getInitialAttributes()
 	 */
 	public Map getInitialAttributes() {
 		return this._map;
 	}
 
 	public void setInitialAttributes(Map map) {
 		this._map = map;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemDescriptor#setDefaultPrefix(java.lang.String)
 	 */
 	public void setDefaultPrefix(String defaultPrefix) {
 		this._prefix = defaultPrefix;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemDescriptor#getDefaultPrefix()
 	 */
 	public String getDefaultPrefix() {
 		return this._prefix;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemDescriptor#getSmallIconString()
 	 */
 	public String getSmallIconString() {
 		return this._smallIconString;
 	}
 
 	public void setSmallIconString(String icon) {
 		this._smallIconString = icon;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemDescriptor#getLargeIconString()
 	 */
 	public String getLargeIconString() {
 		return this._largeIconString;
 	}
 
 	public void setLargeIconString(String icon) {
 		this._largeIconString = icon;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemDescriptor#getPaletteEntry()
 	 */
 	public PaletteEntry getPaletteEntry() {
 		return this._entry;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemDescriptor#setPaletteEntry(org.eclipse.gef.palette.PaletteEntry)
 	 */
 	public void setPaletteEntry(PaletteEntry entry) {
 		this._entry = entry;
 		/*
 		 * if (entry.getDescription()==null && this.getDescription()!=null) {
 		 * entry.setDescription(this.getDescription()); } if
 		 * (entry.getId()==null && this.getId()!=null) {
 		 * entry.setId(this.getId()); } if (entry.getLabel()==null &&
 		 * this.getLabel()!=null) { entry.setLabel(this.getLabel()); }
 		 * entry.setVisible(this.isVisible());
 		 */
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemEntry#getId()
 	 */
 	public String getId() {
 		if (_id == null) {
 			_id = _tagName;
 		}
 		return _id;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemEntry#setId(java.lang.String)
 	 */
 	public void setId(String id) {
 		this._id = id;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemEntry#setlabel(java.lang.String)
 	 */
 	public void setLabel(String label) {
 		this._label = label;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemEntry#getDescription()
 	 */
 	public String getDescription() {
 		return _description;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemEntry#setDescription(java.lang.String)
 	 */
 	public void setDescription(String desc) {
 		this._description = desc;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemEntry#isVisible()
 	 */
 	public boolean isVisible() {
 		return _isVisible;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemEntry#setVisible(boolean)
 	 */
 	public void setVisible(boolean visible) {
 		this._isVisible = visible;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemEntry#getLabel()
 	 */
 	public String getLabel() {
 		return _label;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemDescriptor#isRequireHForm()
 	 */
 	public boolean isRequireHForm() {
 		return this._requireHForm;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemDescriptor#setRequireHForm()
 	 */
 	public void setRequireHForm(boolean r) {
 		this._requireHForm = r;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemDescriptor#isJSFComponent()
 	 */
 	public boolean isJSFComponent() {
 		if (IJMTConstants.URI_JSF_CORE.equals(this._uri)
 				|| IJMTConstants.URI_JSF_HTML.equals(this._uri)) {
 			return true;
 		}
 		return this._jsfComponent;
 	}
 
 	public void setJSFComponent(boolean b) {
 		this._jsfComponent = b;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemEntry#setInitialState(int)
 	 */
 	public void setInitialState(int state) {
 		_initState = state;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.editors.palette.IPaletteItemEntry#getInitialState()
 	 */
 	public int getInitialState() {
 		// TODO Auto-generated method stub
 		return _initState;
 	}
 
 	/**
 	 * Returns the template nodes with prefixs resolved from IDOMModel.
 	 * 
 	 * @return Returns the _templateSubNodes.
 	 */
 	public Node[] getTemplateSubNodes(IDOMModel model) {
 		if (_templateSubNodes == null) {
 			return null;
 		}
 		if (!this.getURI().equalsIgnoreCase(IJMTConstants.URI_JSF_HTML) && //
 				!getURI().equalsIgnoreCase(IJMTConstants.URI_JSF_CORE)) {
 			List nodes = new ArrayList();
 			for (int i = 0, n = _templateSubNodes.getLength(); i < n; i++) {
 				String prefixH = JSPUtil.getPrefix(model,
 						IJMTConstants.URI_JSF_HTML);
 				String prefixC = JSPUtil.getPrefix(model,
 						IJMTConstants.URI_JSF_CORE);
 				Node node = PaletteElementTemplateHelper.cloneNodeDeep(model
 						.getDocument(), _templateSubNodes.item(i), prefixH,
 						prefixC);
 				nodes.add(node);
 			}
 			return (Node[]) nodes.toArray(new Node[nodes.size()]);
 		}
         Node[] result = null;
         if (model != null) {
         	NodeList nl = _templateSubNodes;
         	if (nl != null) {
         		String prefixH = JSPUtil.getPrefix(model,
         				IJMTConstants.URI_JSF_HTML);
         		String prefixC = JSPUtil.getPrefix(model,
         				IJMTConstants.URI_JSF_CORE);
 
         		Assert.isTrue(prefixH != null && prefixC != null);
         		result = PaletteElementTemplateHelper.applyPrefixes(
         				prefixH, prefixC, nl, model.getDocument());
         	}
         }
         return result;
 	}
 
 	/**
 	 * This method should be called by config file initialization class only.
 	 * 
 	 * @param subNodes
 	 *            The _templateSubNodes to set.
 	 */
 	public void setTemplateSubNodes(NodeList subNodes) {
 		_templateSubNodes = subNodes;
 	}
 }
