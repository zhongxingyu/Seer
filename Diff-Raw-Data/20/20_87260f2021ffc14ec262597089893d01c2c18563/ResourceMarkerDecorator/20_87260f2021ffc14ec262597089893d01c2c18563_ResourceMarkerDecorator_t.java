 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.eclipse.bpel.common.ui;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.IDecoration;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.ILightweightLabelDecorator;
 
 /**
  * Lightweight icon decorator for error/warning/info icons in Navigator view
  *
  * @see https://jira.jboss.org/browse/JBIDE-6016
  * @author Bob Brodt
  * @date Nov 16, 2010
  */
 public class ResourceMarkerDecorator implements ILightweightLabelDecorator, ICommonUIConstants {
 
	private static ImageDescriptor img_error = null;
	private static ImageDescriptor img_warning = null;
	private static ImageDescriptor img_info = null;
 	
 	public ResourceMarkerDecorator()
 	{
 		super();
 	}
 	
 	public void addListener(ILabelProviderListener listener) {
 	}
 
 	public void dispose() {
 	}
 
 	public boolean isLabelProperty(Object arg0, String arg1) {
 		return false;
 	}
 
 	public void removeListener(ILabelProviderListener listener) {
 	}
 
 	public void decorate(Object element, IDecoration decoration) {
 		try {
 			int severity = ((IResource)element).findMaxProblemSeverity(IMarker.PROBLEM, true, IResource.DEPTH_ONE);
 			switch (severity) {
 			case IMarker.SEVERITY_ERROR:
				if (img_error==null)
					img_error = CommonUIPlugin.getDefault().getImageRegistry().getDescriptor(ICommonUIConstants.ICON_ERROR);
 				decoration.addOverlay(img_error);
 				break;
 			case IMarker.SEVERITY_WARNING:
				if (img_warning==null)
					img_warning = CommonUIPlugin.getDefault().getImageRegistry().getDescriptor(ICommonUIConstants.ICON_WARNING);
 				decoration.addOverlay(img_warning);
 				break;
 			case IMarker.SEVERITY_INFO:
				if (img_info==null)
					img_info = CommonUIPlugin.getDefault().getImageRegistry().getDescriptor(ICommonUIConstants.ICON_INFO);
 				decoration.addOverlay(img_info);
 				break;
 			}
 		}
 		catch(Exception e) {
 		}
 	}
 }
