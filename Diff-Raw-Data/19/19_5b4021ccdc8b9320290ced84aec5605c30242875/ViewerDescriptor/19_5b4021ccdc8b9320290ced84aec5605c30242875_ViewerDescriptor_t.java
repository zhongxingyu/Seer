 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.ui.trees;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 
 /**
  * A data structure to describe a viewer.
  */
 public class ViewerDescriptor {
 	// The style configuration element.
 	private IConfigurationElement styleConfig;
 	// The drag support configuration.
 	private IConfigurationElement dragConfig;
 	// The drop support configuration.
 	private IConfigurationElement dropConfig;
 	// The content provider for the tree viewer.
 	private ITreeContentProvider contentProvider;
 	// If the state of the tree viewer is persistent.
 	private boolean persistent = false;
 	// The auto expand level.
 	private int autoExpandLevel = 2;
 	// The context menu id.
 	private String menuId;
 	// The command invoked when double clicked.
 	private String doubleClickCommand;
 	// The help id of the tree viewer.
 	private String helpId;
 
 	public IConfigurationElement getStyleConfig() {
 		return styleConfig;
 	}
 
 	public void setStyleConfig(IConfigurationElement styleConfig) {
 		this.styleConfig = styleConfig;
 	}
 
 	public IConfigurationElement getDragConfig() {
 		return dragConfig;
 	}
 
 	public void setDragConfig(IConfigurationElement dragConfig) {
 		this.dragConfig = dragConfig;
 	}
 
 	public IConfigurationElement getDropConfig() {
 		return dropConfig;
 	}
 
 	public void setDropConfig(IConfigurationElement dropConfig) {
 		this.dropConfig = dropConfig;
 	}
 
 	public ITreeContentProvider getContentProvider() {
 		return contentProvider;
 	}
 
 	public void setContentProvider(ITreeContentProvider contentProvider) {
 		this.contentProvider = contentProvider;
 	}
 
 	public boolean isPersistent() {
 		return persistent;
 	}
 
 	public void setPersistent(boolean persistent) {
 		this.persistent = persistent;
 	}
 
 	public int getAutoExpandLevel() {
 		return autoExpandLevel;
 	}
 
 	public void setAutoExpandLevel(int autoExpandLevel) {
 		this.autoExpandLevel = autoExpandLevel;
 	}
 
	public String getContextMenuId() {
 		return menuId;
 	}
 
	public void setContextMenuId(String menuId) {
 		this.menuId = menuId;
 	}
 
 	public String getDoubleClickCommand() {
 		return doubleClickCommand;
 	}
 
 	public void setDoubleClickCommand(String doubleClickCommand) {
 		this.doubleClickCommand = doubleClickCommand;
 	}
 
 	public String getHelpId() {
 		return helpId;
 	}
 
 	public void setHelpId(String helpId) {
 		this.helpId = helpId;
 	}
 }
