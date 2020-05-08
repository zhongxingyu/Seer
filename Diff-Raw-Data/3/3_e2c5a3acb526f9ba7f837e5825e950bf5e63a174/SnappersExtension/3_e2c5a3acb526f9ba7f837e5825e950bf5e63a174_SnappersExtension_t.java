 /*
  * Copyright 2008 Deputacin Provincial de A Corua
  * Copyright 2009 Deputacin Provincial de Pontevedra
  * Copyright 2010 CartoLab, Universidad de A Corua
  *
  * This file is part of openCADTools, developed by the Cartography
  * Engineering Laboratory of the University of A Corua (CartoLab).
  * http://www.cartolab.es
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
  */
 package com.iver.cit.gvsig;
 
 import java.util.ArrayList;
 
 import com.iver.andami.PluginServices;
 import com.iver.andami.plugins.Extension;
 import com.iver.cit.gvsig.project.documents.view.snapping.EIELFinalPointSnapper;
 import com.iver.cit.gvsig.project.documents.view.snapping.EIELNearestPointSnapper;
 import com.iver.cit.gvsig.project.documents.view.snapping.SnapperStatus;
 import com.iver.cit.gvsig.layers.ILayerEdited;
 import com.iver.cit.gvsig.layers.VectorialLayerEdited;
 import com.iver.cit.gvsig.project.documents.view.gui.View;
 import com.iver.utiles.console.JConsole;
 
 /**
  * Extension to manage the activation and deactivation of the snappers
  *
  * @author Jose Ignacio Lamas [LBD]
  * @author Javier Estvez [Cartolab]
  */
 public class SnappersExtension extends Extension {
 
 	private SnapperStatus snapperStatus = null;
 
 	/**
 	 * @see com.iver.andami.plugins.IExtension#initialize()
 	 */
 	public void initialize() {
 		snapperStatus = SnapperStatus.getSnapperStatus();

		PluginServices.getIconTheme().registerDefault("Snapper",
				this.getClass().getClassLoader().getResource("images/Snapper.png"));
 	}
 
 	/**
 	 * @see com.iver.andami.plugins.IExtension#execute(java.lang.String)
 	 */
 	public void execute(String s) {
 		CADExtension.initFocus();
 
 		if (s.equals("_snappers")) {
 		        ILayerEdited lyrEdit = CADExtension.getEditionManager()
 			.getActiveLayerEdited();
 			ArrayList snappers = ((VectorialLayerEdited)lyrEdit).getSnappers();
 			String message = new String();
 
 			if (snapperStatus.isVertexActivated() || snapperStatus.isNearLineActivated()){
 				snappers.clear();
 				snapperStatus.setVertexActivated(false);
 				snapperStatus.setNearLineActivated(false);
 				message = PluginServices.getText(this, "snappers_desactivated");
 			} else {
 				//Creating snappers and added to the layer
 				EIELNearestPointSnapper eielNearestSnap = new EIELNearestPointSnapper();
 				EIELFinalPointSnapper eielFinalSnap = new EIELFinalPointSnapper();
 
 				snappers.clear();
 				snappers.add(eielFinalSnap);
 				snappers.add(eielNearestSnap);
 				snapperStatus.setVertexActivated(true);
 				snapperStatus.setNearLineActivated(true);
 
 				message = PluginServices.getText(this, "snappers_activated");
 			}
 
 			//Printing in console if the snapper is activated
 			if (PluginServices.getMDIManager().getActiveWindow() instanceof View)
 			{
 				View vista = (View) PluginServices.getMDIManager().getActiveWindow();
 				vista.getConsolePanel().addText("\n" +message, JConsole.INSERT);
 			}
 		}
 	}
 
 
 	public boolean isEnabled() {
 
 		if (EditionUtilities.getEditionStatus() == EditionUtilities.EDITION_STATUS_ONE_VECTORIAL_LAYER_ACTIVE_AND_EDITABLE) {
 			if (CADExtension.getEditionManager().getActiveLayerEdited()==null){
 				return false;
 			}
 			return true;
 		}
 
 		return false;
 	}
 
 	public boolean isVisible() {
 		if (EditionUtilities.getEditionStatus() == EditionUtilities.EDITION_STATUS_ONE_VECTORIAL_LAYER_ACTIVE_AND_EDITABLE)
 			return true;
 		return false;
 	}
 
 }
