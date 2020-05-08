 /* gvSIG. Sistema de Informaci�n Geogr�fica de la Generalitat Valenciana
  *
  * Copyright (C) 2005 IVER T.I. and Generalitat Valenciana.
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
  *
  * For more information, contact:
  *
  *  Generalitat Valenciana
  *   Conselleria d'Infraestructures i Transport
  *   Av. Blasco Ib��ez, 50
  *   46010 VALENCIA
  *   SPAIN
  *
  *      +34 963862235
  *   gvsig@gva.es
  *      www.gvsig.gva.es
  *
  *    or
  *
  *   IVER T.I. S.A
  *   Salamanca 50
  *   46005 Valencia
  *   Spain
  *
  *   +34 963163400
  *   dac@iver.es
  */
 
 /* CVS MESSAGES:
  *
  * $Id: AdvancedSymbologyExtension.java 16205 2007-11-08 16:33:35Z jdominguez $
  * $Log: AdvancedSymbologyExtension.java,v $
  * Revision 1.12  2007/09/19 15:34:59  jaume
  * removed unnecessary imports
  *
  * Revision 1.11  2007/09/17 09:11:28  jaume
  * order of the elements inverted
  *
  * Revision 1.10  2007/09/13 11:37:09  jvidal
  * *** empty log message ***
  *
  * Revision 1.9  2007/09/04 10:53:10  caballero
  * show page
  *
  * Revision 1.8  2007/08/09 10:39:04  jaume
  * first round of found bugs fixed
  *
  * Revision 1.7  2007/08/01 13:03:31  jaume
  * plugable symbol editor
  *
  * Revision 1.6  2007/05/22 12:17:12  jaume
  * *** empty log message ***
  *
  * Revision 1.5  2007/05/17 09:32:37  jaume
  * *** empty log message ***
  *
  * Revision 1.4  2007/03/21 08:03:03  jaume
  * refactored to use ISymbol instead of FSymbol
  *
  * Revision 1.3  2007/03/13 16:57:35  jaume
  * Added MultiVariable legend
  *
  * Revision 1.2  2007/03/09 11:25:00  jaume
  * Advanced symbology (start committing)
  *
  * Revision 1.1.2.4  2007/02/21 07:35:14  jaume
  * *** empty log message ***
  *
  * Revision 1.1.2.3  2007/02/12 15:14:41  jaume
  * refactored interval legend and added graduated symbol legend
  *
  * Revision 1.1.2.2  2007/02/01 17:47:12  jaume
  * *** empty log message ***
  *
  * Revision 1.1.2.1  2007/02/01 12:12:41  jaume
  * theme manager window and all its components are now dynamic
  *
  */
 package com.iver.cit.gvsig;
 
 import org.geotools.styling.LineSymbolizer;
 import org.geotools.styling.PointSymbolizer;
 import org.geotools.styling.PolygonSymbolizer;
 import org.geotools.styling.TextSymbolizer;
 
 import com.iver.andami.PluginServices;
 import com.iver.andami.plugins.Extension;
 import com.iver.andami.preferences.IPreference;
 import com.iver.andami.preferences.IPreferenceExtension;
 import com.iver.cit.gvsig.gui.styling.SimpleFill;
 import com.iver.cit.gvsig.gui.styling.SimpleLine;
 import com.iver.cit.gvsig.gui.styling.SimpleMarker;
 import com.iver.cit.gvsig.gui.styling.SimpleText;
 import com.iver.cit.gvsig.gui.styling.SymbolEditor;
 import com.iver.cit.gvsig.project.documents.view.legend.gui.Categories;
 import com.iver.cit.gvsig.project.documents.view.legend.gui.Features;
 import com.iver.cit.gvsig.project.documents.view.legend.gui.LegendManager;
 import com.iver.cit.gvsig.project.documents.view.legend.gui.MultipleAttributes;
 import com.iver.cit.gvsig.project.documents.view.legend.gui.Quantities;
 import com.iver.cit.gvsig.project.documents.view.legend.gui.SingleSymbol;
 import com.iver.cit.gvsig.project.documents.view.legend.gui.ThemeManagerWindow;
 import com.iver.cit.gvsig.project.documents.view.legend.gui.VectorialInterval;
 import com.iver.cit.gvsig.project.documents.view.legend.gui.VectorialUniqueValue;
 
 /**
  * Extension for enable the symbology. It only installs the core symbology.
  * 
  * @author jaume dominguez faus - jaume.dominguez@iver.es
  * 
  */
 public class BasicSymbologyExtension extends Extension implements
 		IPreferenceExtension {
 
 	public void initialize() {
 		// Install required features
 
 		// modules for symbol editor
 		SymbolEditor.addSymbolEditorPanel(SimpleFill.class,
 				PolygonSymbolizer.class);
 		SymbolEditor.addSymbolEditorPanel(SimpleMarker.class,
 				PointSymbolizer.class);
 		SymbolEditor.addSymbolEditorPanel(SimpleLine.class,
 				LineSymbolizer.class);
 		SymbolEditor.addSymbolEditorPanel(SimpleText.class,
 				TextSymbolizer.class);
 
 		// pages
 		// TODO gtintegration
 		// ThemeManagerWindow.addPage(General.class);
 		ThemeManagerWindow.addPage(LegendManager.class);
 		// TODO gtintegration
 		// ThemeManagerWindow.addPage(LabelingManager.class);
 
 		// TODO gtintegration
 		// ThemeManagerWindow.setTabEnabledForLayer(General.class,
 		// FLyrVect.class,
 		// true);
 		// ThemeManagerWindow.setTabEnabledForLayer(LegendManager.class,
 		// FLyrVect.class, true);
 		// ThemeManagerWindow.setTabEnabledForLayer(LabelingManager.class,
 		// FLyrVect.class, true);
 		// // labeling strategies (inverse order to the wanted to be shown)
 		// LabelingManager.addLabelingStrategy(AttrInTableLabeling.class);
 
 		// legends available in the legend page
 		LegendManager.addLegendPage(Quantities.class);
 		LegendManager.addLegendPage(Features.class);
 		LegendManager.addLegendPage(Categories.class);
 		LegendManager.addLegendPage(MultipleAttributes.class);
 
 		LegendManager.addLegendPage(SingleSymbol.class);
 		LegendManager.addLegendPage(VectorialInterval.class);
 		LegendManager.addLegendPage(VectorialUniqueValue.class);
 
 		// TODO gtintegration
 		// LegendManager.addLegendDriver(FMapGVLDriver.class);
 
 		registerIcons();
 	}
 
 	private void registerIcons() {
 		PluginServices.getIconTheme().registerDefault(
 				"chain",
 				this.getClass().getClassLoader()
 						.getResource("images/chain.png"));
 		PluginServices.getIconTheme().registerDefault(
 				"broken-chain",
 				this.getClass().getClassLoader()
 						.getResource("images/broken-chain.png"));
 		PluginServices.getIconTheme().registerDefault(
 				"underline-icon",
 				this.getClass().getClassLoader()
 						.getResource("images/underlined.png"));
 		PluginServices.getIconTheme().registerDefault(
 				"italic-icon",
 				this.getClass().getClassLoader()
 						.getResource("images/italic.png"));
 		PluginServices.getIconTheme()
 				.registerDefault(
 						"bold-icon",
 						this.getClass().getClassLoader()
 								.getResource("images/bold.png"));
 		PluginServices.getIconTheme().registerDefault(
 				"symbol-pref",
 				this.getClass().getClassLoader()
 						.getResource("images/QuantitiesByCategory.png"));
 		PluginServices.getIconTheme().registerDefault(
 				"up-arrow",
 				this.getClass().getClassLoader()
 						.getResource("images/up-arrow.png"));
 		PluginServices.getIconTheme().registerDefault(
 				"down-arrow",
 				this.getClass().getClassLoader()
 						.getResource("images/down-arrow.png"));
 	}
 
 	public void execute(String actionCommand) {
 
 	}
 
 	public boolean isEnabled() {
 		return true; // or whatever
 	}
 
 	public boolean isVisible() {
 		return true; // or whatever
 	}
 
 	public IPreference[] getPreferencesPages() {
 		// TODO gtintegration return new IPreference[] { new
 		// CartographicSupportPage(),
 		// new SymbologyPage() };
 		return new IPreference[0];
 	}
 }
