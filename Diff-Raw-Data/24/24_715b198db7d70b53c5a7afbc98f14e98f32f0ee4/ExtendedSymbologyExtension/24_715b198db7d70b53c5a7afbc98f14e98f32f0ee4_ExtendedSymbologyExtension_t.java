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
 package org.gvsig.symbology;
 
 import org.gvsig.symbology.gui.layerproperties.GraduatedSymbols;
 import org.gvsig.symbology.gui.layerproperties.ProportionalSymbols;
 
 import com.iver.andami.plugins.Extension;
 import com.iver.cit.gvsig.project.documents.view.legend.gui.LegendManager;
 
 public class ExtendedSymbologyExtension extends Extension {
 
 	@Override
 	public void execute(String actionCommand) {
 		// do nothing
 	}
 
 	public void initialize() {
 		// TODO gtintegration
 		// PluginServices.getIconTheme().registerDefault(
 		// "high-density",
 		// DotDensity.class.getClassLoader().getResource(
 		// "images/high-density-sample.png"));
 		// PluginServices.getIconTheme().registerDefault(
 		// "medium-density",
 		// DotDensity.class.getClassLoader().getResource(
 		// "images/medium-density-sample.png"));
 		// PluginServices.getIconTheme().registerDefault(
 		// "low-density",
 		// DotDensity.class.getClassLoader().getResource(
 		// "images/low-density-sample.png"));
 		// PluginServices.getIconTheme().registerDefault(
 		// "quantities-by-category",
 		// QuantityByCategory.class.getClassLoader().getResource(
 		// "images/QuantitiesByCategory.png"));
 		// PluginServices.getIconTheme().registerDefault(
 		// "dot-density",
 		// QuantityByCategory.class.getClassLoader().getResource(
 		// "images/DotDensity.PNG"));
 		//
 		// PluginServices.getIconTheme().registerDefault(
 		// "graduated-symbols",
 		// QuantityByCategory.class.getClassLoader().getResource(
 		// "images/GraduatedSymbols.PNG"));
 		//
 		// PluginServices.getIconTheme().registerDefault(
 		// "proportional-symbols",
 		// QuantityByCategory.class.getClassLoader().getResource(
 		// "images/ProportionalSymbols.PNG"));
 		//
 		// PluginServices.getIconTheme().registerDefault(
 		// "filter-expressions",
 		// QuantityByCategory.class.getClassLoader().getResource(
 		// "images/FilterExpressions.PNG"));
 		// PluginServices.getIconTheme().registerDefault(
 		// "add-text-icon",
 		// this.getClass().getClassLoader()
 		// .getResource("images/add-text.png"));
 		// PluginServices.getIconTheme().registerDefault(
 		// "remove-text-icon",
 		// this.getClass().getClassLoader()
 		// .getResource("images/remove-text.png"));
 		// PluginServices.getIconTheme()
 		// .registerDefault(
 		// "hand-icon",
 		// this.getClass().getClassLoader()
 		// .getResource("images/hand.gif"));
 		//
 		// PluginServices.getIconTheme().registerDefault(
 		// "set-high-precedence-point-label-icon",
 		// this.getClass().getClassLoader()
 		// .getResource("images/high-precedence.png"));
 		// PluginServices.getIconTheme().registerDefault(
 		// "set-normal-precedence-point-label-icon",
 		// this.getClass().getClassLoader()
 		// .getResource("images/normal-precedence.png"));
 		// PluginServices.getIconTheme().registerDefault(
 		// "set-low-precedence-point-label-icon",
 		// this.getClass().getClassLoader()
 		// .getResource("images/low-precedence.png"));
 		// PluginServices.getIconTheme().registerDefault(
 		// "set-forbidden-precedence-point-label-icon",
 		// this.getClass().getClassLoader()
 		// .getResource("images/forbidden-precedence.png"));
 
 		// modules for symbol editor
 		// TODO gtintegration
 		// SymbolEditor.addSymbolEditorPanel(MarkerFill.class, FShape.POLYGON);
 		// SymbolEditor.addSymbolEditorPanel(PictureFill.class, FShape.POLYGON);
 		// SymbolEditor.addSymbolEditorPanel(LineFill.class, FShape.POLYGON);
 		// SymbolEditor.addSymbolEditorPanel(GradientFill.class,
 		// FShape.POLYGON);
 		// SymbolEditor.addSymbolEditorPanel(CharacterMarker.class,
 		// FShape.POINT);
 		// SymbolEditor.addSymbolEditorPanel(PictureMarker.class, FShape.POINT);
 		// SymbolEditor.addSymbolEditorPanel(PictureLine.class, FShape.LINE);
 		// SymbolEditor.addSymbolEditorPanel(MarkerLine.class, FShape.LINE);
 
 		// legends available in the legend page
 		// TODO gtintegration
 		// LegendManager.addLegendPage(DotDensity.class);
 		// LegendManager.addLegendPage(QuantityByCategory.class);
 
 		// LegendManager.addLegendPage(Statistics.class);
 		// LegendManager.addLegendPage(BarChart3D.class);
 		// LegendManager.addLegendPage(PieChart3D.class);
 
 		// Editor tools
 		// TODO gtintegration
 		// StyleEditor.addEditorTool(LabelStylePanTool.class);
 		// StyleEditor.addEditorTool(LabelStyleNewTextFieldTool.class);
 		// StyleEditor.addEditorTool(LabelStyleRemoveLastTextField.class);
 		// StyleEditor.addEditorTool(LabelStyleOpenBackgroundFile.class);
 		// StyleEditor.addEditorTool(PointLabelHighPrecedenceTool.class);
 		// StyleEditor.addEditorTool(PointLabelNormalPrecedenceTool.class);
 		// StyleEditor.addEditorTool(PointLabelLowPrecedenceTool.class);
 		// StyleEditor.addEditorTool(PointLabelForbiddenPrecedenceTool.class);
 
 		// LabelingManager.addLabelingStrategy(GeneralLabeling.class);
 
 		// labeling methods in the labeling page
 		// (inverse order to the wanted to be shown)
 		// TODO gtintegration
 		// GeneralLabeling.addLabelingMethod(OnSelection.class);
 		// GeneralLabeling.addLabelingMethod(FeatureDependent.class);
 		// GeneralLabeling.addLabelingMethod(DefaultLabeling.class);
 		//
 		// PlacementManager.addLabelPlacement(LinePlacementAtExtremities.class);
 		// PlacementManager.addLabelPlacement(LinePlacementAtBest.class);
 		// PlacementManager.addLabelPlacement(LinePlacementInTheMiddle.class);
 		// PlacementManager.addLabelPlacement(MarkerPlacementOnPoint.class);
 		// // PlacementManager.addLabelPlacement(MarkerCenteredAtPoint.class);
 		// PlacementManager.addLabelPlacement(MarkerPlacementAroundPoint.class);
 		// PlacementManager.addLabelPlacement(PolygonPlacementOnCentroid.class);
 		// PlacementManager.addLabelPlacement(PolygonPlacementInside.class);
 		// PlacementManager.addLabelPlacement(PolygonPlacementParallel.class);
 		//
 		// LabelingFactory
 		// .setDefaultLabelingStrategy(GeneralLabelingStrategy.class);
 		//
 		// OperatorsFactory.getInstance().addOperator(SubstringFunction.class);
 		// OperatorsFactory.getInstance().addOperator(IndexOfFunction.class);
 	}
 
 	@Override
 	public boolean isEnabled() {
 		return true; // or whatever
 	}
 
 	@Override
 	public boolean isVisible() {
 		return true; // or whatever
 	}
 }
