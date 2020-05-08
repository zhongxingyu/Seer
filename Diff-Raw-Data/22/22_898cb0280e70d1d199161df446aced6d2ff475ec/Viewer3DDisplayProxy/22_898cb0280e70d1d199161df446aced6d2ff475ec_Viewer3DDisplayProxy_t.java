/*
  * Copyright (c) 2004-2007 Rensselaer Polytechnic Institute
 * Copyright (c) 2010 Rensselaer Polytechnic Institute
  * Copyright (c) 2007 NEES Cyberinfrastructure Center
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  *
  * For more information: http://nees.rpi.edu/3dviewer/
  */
 
 package org.nees.rpi.vis.viewer3d;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import org.nees.rpi.vis.ui.XYChartPanelProxy;
import org.nees.rpi.vis.ui.*;

 
 /**
  * This Interface is used by applications needing to use the GEO Viewer3D
  * in their display. It acts as a proxy for the various components used in the
 * display of the 3D universe.<br><br>
  * 
  * It is designed to create low coupling between the main application and the
  * 3D viewer.
  */
 public interface Viewer3DDisplayProxy
 {
 	/**
 	 * Returns the main application display frame. Used within the 3D panel
 	 * for modal and other dependant components.
 	 * @return
 	 * a JFrame pointing to the main application frame
 	 */
 	public JFrame getApplicationFrame();
	
 	/**
 	 * Returns the main panel used to display the 3D universe.
 	 * @return
 	 * a JPanel pointing to the main 3D panel
 	 */
 	public JPanel getMain3DPanel();
	
 	/**
 	 * Returns the helper panel used to display the orientation view.
 	 * @return
 	 * a JPanel pointing to the orientation 3D panel
 	 */
 	public JPanel getOrientation3DPanel();
	
 	/**
 	 * Returns the XYChartPanelProxy used to display data series.
 	 * @return
 	 * an XYChartPanelProxy declared by the main app
 	 */
 	public XYChartPanelProxy getXYChartProxy();
 	
 	public ShapeInfoPanel getShapeInfoPanel();
 }
