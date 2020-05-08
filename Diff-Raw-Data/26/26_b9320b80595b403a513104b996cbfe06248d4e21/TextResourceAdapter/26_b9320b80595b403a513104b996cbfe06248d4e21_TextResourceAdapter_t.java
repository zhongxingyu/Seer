 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.wst.server.ui.internal.view.tree;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.wst.server.core.ServerCore;
 import org.eclipse.wst.server.ui.internal.ImageResource;
 import org.eclipse.wst.server.ui.internal.Messages;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.ide.IDE;
 import org.eclipse.ui.model.IWorkbenchAdapter;
 /**
  * 
  */
 public class TextResourceAdapter implements IAdaptable, IWorkbenchAdapter, IServerElementTag {
 	protected Object parent;
 	protected byte thisStyle;
 	
 	public static Object deleted;
 	
 	public final static byte STYLE_SERVERS = 0;
 	public final static byte STYLE_NO_MODULES = 4;
 	public final static byte STYLE_NO_SERVERS = 5;
 
 	public TextResourceAdapter(Object parent, byte thisStyle) {
 		this.parent = parent;
 		this.thisStyle = thisStyle;
 	}
 	
 	public byte getStyle() {
 		return thisStyle;
 	}
 	
 	public void setStyle(byte b) {
 		thisStyle = b;
 	}
 
 	/*
 	 * @see IAdaptable#getAdapter(Class)
 	 */
 	public Object getAdapter(Class adapter) {
 		if (adapter.equals(IAdaptable.class))
 			return this;
 		else if (adapter.equals(IWorkbenchAdapter.class))
 			return this;
 		else
 			return Platform.getAdapterManager().getAdapter(this, adapter);
 	}
 
 	/*
 	 * @see IWorkbenchAdapter#getChildren(Object)
 	 */
 	public Object[] getChildren(Object o) {
 		if (thisStyle == STYLE_NO_MODULES || thisStyle == STYLE_NO_SERVERS)
 			return new Object[0];
 		/*else if (thisStyle == STYLE_SERVERS_AND_CONFIGURATIONS) 
 			return new Object[] {
 				new TextResourceAdapter(this, STYLE_SERVERS),
 				new TextResourceAdapter(this, STYLE_CONFIGURATIONS)
 			};*/
 
 		Object[] elements = null;
 		if (thisStyle == STYLE_SERVERS)
 			elements = ServerCore.getServers();
 
 		List list = new ArrayList();
 		if (elements != null) {
 			int size = elements.length;
 			for (int i = 0; i < size; i++) {
 				if (elements[i] != deleted)
 					list.add(new ServerElementAdapter(this, elements[i]));
 			}
 		}
 		return list.toArray();
 	}
 
 	/*
 	 * @see IWorkbenchAdapter#getImageDescriptor(Object)
 	 */
 	public ImageDescriptor getImageDescriptor(Object object) {
 		if (thisStyle == STYLE_SERVERS)
 			return null;
 		//else if (thisStyle == STYLE_SERVERS_AND_CONFIGURATIONS)
 		//	return ServerImageResource.getImageDescriptor(ServerImageResource.IMG_SERVER_PROJECT);
 		//else if (thisStyle == STYLE_NO_CONFIGURATION)
 		//	return ServerImageResource.getImageDescriptor(ServerImageResource.IMG_SERVER_CONFIGURATION_NONE);
 		else if (thisStyle == STYLE_NO_MODULES) {
 			ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
 			return sharedImages.getImageDescriptor(IDE.SharedImages.IMG_OBJ_PROJECT);
 		}
 		else if (thisStyle == STYLE_NO_SERVERS)
 			return ImageResource.getImageDescriptor(ImageResource.IMG_SERVER_CONFIGURATION_MISSING);
 		else
 			return null;
 	}
 
 	/*
 	 * @see IWorkbenchAdapter#getLabel(Object)
 	 */
 	public String getLabel(Object o) {
 		if (thisStyle == STYLE_SERVERS)
 			return Messages.viewServers;
 		//else if (thisStyle == STYLE_SERVERS_AND_CONFIGURATIONS)
 		//	return "Server Info";
 		//else if (thisStyle == STYLE_NO_CONFIGURATION)
 		//	return "No configuration";
 		else if (thisStyle == STYLE_NO_MODULES)
 			return Messages.viewNoModules;
		//else if (thisStyle == STYLE_NO_SERVERS)
		//	return Messages.viewConfigurationUnused;
 		else
 			return "n/a";
 	}
 
 	/*
 	 * @see IWorkbenchAdapter#getParent(Object)
 	 */
 	public Object getParent(Object o) {
 		return parent;
 	}
 	
 	public boolean equals(Object obj) {
 		if (!(obj instanceof TextResourceAdapter))	
 			return false;
 		
 		TextResourceAdapter adapter = (TextResourceAdapter) obj;
 		return (adapter.getStyle() == thisStyle);
 	}
 	
 	public int hashCode() {
 		return thisStyle;
 	}
 }
