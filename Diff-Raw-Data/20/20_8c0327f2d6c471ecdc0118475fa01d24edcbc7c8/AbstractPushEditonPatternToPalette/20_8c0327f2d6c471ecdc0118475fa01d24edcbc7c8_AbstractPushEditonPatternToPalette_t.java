 /*
  * (c) Copyright 2010-2011 AgileBirds
  *
  * This file is part of OpenFlexo.
  *
  * OpenFlexo is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * OpenFlexo is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with OpenFlexo. If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package org.openflexo.foundation.view.diagram.viewpoint.action;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Vector;
 import java.util.logging.Logger;
 
 import org.openflexo.fge.ConnectorGraphicalRepresentation;
 import org.openflexo.fge.GraphicalRepresentation;
 import org.openflexo.fge.ShapeGraphicalRepresentation;
 import org.openflexo.fge.ShapeGraphicalRepresentation.DimensionConstraints;
import org.openflexo.fge.ShapeGraphicalRepresentation.LocationConstraints;
 import org.openflexo.fge.graphics.BackgroundImageBackgroundStyle;
 import org.openflexo.fge.graphics.ForegroundStyle;
 import org.openflexo.fge.graphics.ShadowStyle;
 import org.openflexo.fge.graphics.TextStyle;
 import org.openflexo.fge.shapes.Shape.ShapeType;
 import org.openflexo.foundation.FlexoEditor;
 import org.openflexo.foundation.FlexoObject;
 import org.openflexo.foundation.action.FlexoAction;
 import org.openflexo.foundation.action.FlexoActionType;
 import org.openflexo.foundation.gen.ScreenshotGenerator.ScreenshotImage;
 import org.openflexo.foundation.view.diagram.viewpoint.ConnectorPatternRole;
 import org.openflexo.foundation.view.diagram.viewpoint.DiagramPalette;
 import org.openflexo.foundation.view.diagram.viewpoint.DiagramPaletteElement;
 import org.openflexo.foundation.view.diagram.viewpoint.DiagramPaletteElement.ConnectorOverridingGraphicalRepresentation;
 import org.openflexo.foundation.view.diagram.viewpoint.DiagramPaletteElement.ShapeOverridingGraphicalRepresentation;
 import org.openflexo.foundation.view.diagram.viewpoint.DiagramSpecification;
 import org.openflexo.foundation.view.diagram.viewpoint.DropScheme;
 import org.openflexo.foundation.view.diagram.viewpoint.GraphicalElementPatternRole;
 import org.openflexo.foundation.view.diagram.viewpoint.ShapePatternRole;
 import org.openflexo.foundation.viewpoint.EditionPattern;
 import org.openflexo.swing.ImageUtils;
 import org.openflexo.swing.ImageUtils.ImageType;
 import org.openflexo.toolbox.JavaUtils;
 import org.openflexo.toolbox.StringUtils;
 
 public abstract class AbstractPushEditonPatternToPalette<A extends AbstractPushEditonPatternToPalette<A, T1, T2>, T1 extends FlexoObject & GRTemplate, T2 extends FlexoObject>
 		extends FlexoAction<A, T1, T2> {
 
 	private static final Logger logger = Logger.getLogger(AbstractPushEditonPatternToPalette.class.getPackage().getName());
 
 	public Object graphicalRepresentation;
 	public DiagramPalette palette;
	public int xLocation;
	public int yLocation;;
 	private EditionPattern editionPattern;
 	public DropScheme dropScheme;
 	private String newElementName;
 	public boolean takeScreenshotForTopLevelElement = false;
 	public boolean overrideDefaultGraphicalRepresentations = false;
 
 	private ScreenshotImage screenshot;
 	public int imageWidth;
 	public int imageHeight;
 
 	private DiagramPaletteElement _newPaletteElement;
 
 	protected AbstractPushEditonPatternToPalette(FlexoActionType actionType, T1 focusedObject, Vector<T2> globalSelection,
 			FlexoEditor editor) {
 		super(actionType, focusedObject, globalSelection, editor);
 		drawingObjectEntries = new Vector<DrawingObjectEntry>();
 		updateDrawingObjectEntries();
 	}
 
 	@Override
 	protected void doAction(Object context) {
 		logger.info("Push to palette");
 
 		if (getFocusedObject() != null && palette != null) {
 
 			if (takeScreenshotForTopLevelElement) {
 				File screenshotFile = saveScreenshot();
 				ShapeGraphicalRepresentation gr = new ShapeGraphicalRepresentation();
 				gr.setShapeType(ShapeType.RECTANGLE);
 				gr.setForeground(ForegroundStyle.makeNone());
 				gr.setBackground(new BackgroundImageBackgroundStyle(screenshotFile));
 				gr.setShadowStyle(ShadowStyle.makeNone());
 				gr.setTextStyle(TextStyle.makeDefault());
 				gr.setText("");
 				gr.setWidth(imageWidth);
 				gr.setHeight(imageHeight);
 				gr.setDimensionConstraints(DimensionConstraints.UNRESIZABLE);
 				gr.setIsFloatingLabel(false);
 				graphicalRepresentation = gr;
 			} else {
 				GraphicalRepresentation gr = getFocusedObject().getGraphicalRepresentation();
 				if (gr instanceof ShapeGraphicalRepresentation) {
 					graphicalRepresentation = new ShapeGraphicalRepresentation();
 					((ShapeGraphicalRepresentation) graphicalRepresentation).setsWith(gr);
 				} else if (gr instanceof ConnectorGraphicalRepresentation) {
 					graphicalRepresentation = new ConnectorGraphicalRepresentation();
 					((ConnectorGraphicalRepresentation) graphicalRepresentation).setsWith(gr);
 				}
 			}
 
 			if (overrideDefaultGraphicalRepresentations) {
 				for (DrawingObjectEntry entry : drawingObjectEntries) {
 					if (entry.getSelectThis()) {
 						if (entry.graphicalObject instanceof GRShapeTemplate) {
 							_newPaletteElement.addToOverridingGraphicalRepresentations(new ShapeOverridingGraphicalRepresentation(
 									entry.patternRole, (ShapeGraphicalRepresentation) entry.graphicalObject.getGraphicalRepresentation()));
 						} else if (entry.graphicalObject instanceof GRConnectorTemplate) {
 							_newPaletteElement.addToOverridingGraphicalRepresentations(new ConnectorOverridingGraphicalRepresentation(
 									entry.patternRole, (ConnectorGraphicalRepresentation) entry.graphicalObject
 											.getGraphicalRepresentation()));
 						}
 					}
 				}
 			}
 
			if (graphicalRepresentation instanceof ShapeGraphicalRepresentation) {
				((ShapeGraphicalRepresentation) graphicalRepresentation).setX(xLocation);
				((ShapeGraphicalRepresentation) graphicalRepresentation).setY(yLocation);
			}
			
			_newPaletteElement = palette.addPaletteElement(newElementName, graphicalRepresentation);
			_newPaletteElement.setEditionPattern(editionPattern);
			_newPaletteElement.setDropScheme(dropScheme);
			_newPaletteElement.setBoundLabelToElementName(!takeScreenshotForTopLevelElement);
			
 		} else {
 			logger.warning("Focused role is null !");
 		}
 	}
 
 	public DiagramPaletteElement getNewPaletteElement() {
 		return _newPaletteElement;
 	}
 
 	public String getNewElementName() {
 		return newElementName;
 	}
 
 	public void setNewElementName(String newElementName) {
 		this.newElementName = newElementName;
 	}
 
 	@Override
 	public boolean isValid() {
 		return StringUtils.isNotEmpty(newElementName) && palette != null && editionPattern != null && dropScheme != null;
 	}
 
 	private Vector<DrawingObjectEntry> drawingObjectEntries;
 
 	public Vector<DrawingObjectEntry> getDrawingObjectEntries() {
 		return drawingObjectEntries;
 	}
 
 	public void setDrawingObjectEntries(Vector<DrawingObjectEntry> drawingObjectEntries) {
 		this.drawingObjectEntries = drawingObjectEntries;
 	}
 
 	public class DrawingObjectEntry {
 		private boolean selectThis;
 		public GRTemplate graphicalObject;
 		public String elementName;
 		public GraphicalElementPatternRole patternRole;
 
 		public DrawingObjectEntry(GRTemplate graphicalObject, String elementName) {
 			super();
 			this.graphicalObject = graphicalObject;
 			this.elementName = elementName;
 			this.selectThis = isMainEntry();
 			if (isMainEntry() && editionPattern != null) {
 				patternRole = editionPattern.getDefaultPrimaryRepresentationRole();
 			}
 		}
 
 		public boolean isMainEntry() {
 			return graphicalObject == getFocusedObject();
 		}
 
 		public boolean getSelectThis() {
 			if (isMainEntry()) {
 				return true;
 			}
 			return selectThis;
 		}
 
 		public void setSelectThis(boolean aFlag) {
 			selectThis = aFlag;
 			if (patternRole == null && graphicalObject instanceof GRShapeTemplate) {
 				GraphicalElementPatternRole parentEntryPatternRole = getParentEntry().patternRole;
 				for (ShapePatternRole r : editionPattern.getShapePatternRoles()) {
 					if (r.getParentShapePatternRole() == parentEntryPatternRole && patternRole == null) {
 						patternRole = r;
 					}
 				}
 			}
 		}
 
 		public DrawingObjectEntry getParentEntry() {
 			return getEntry(graphicalObject.getParent());
 		}
 
 		public List<? extends GraphicalElementPatternRole> getAvailablePatternRoles() {
 			if (graphicalObject instanceof GRShapeTemplate) {
 				return editionPattern.getPatternRoles(ShapePatternRole.class);
 			} else if (graphicalObject instanceof GRConnectorTemplate) {
 				return editionPattern.getPatternRoles(ConnectorPatternRole.class);
 			}
 			return null;
 		}
 	}
 
 	public int getSelectedEntriesCount() {
 		int returned = 0;
 		for (DrawingObjectEntry e : drawingObjectEntries) {
 			if (e.selectThis) {
 				returned++;
 			}
 		}
 		return returned;
 	}
 
 	public DrawingObjectEntry getEntry(GRTemplate o) {
 		for (DrawingObjectEntry e : drawingObjectEntries) {
 			if (e.graphicalObject == o) {
 				return e;
 			}
 		}
 		return null;
 	}
 
 	public EditionPattern getEditionPattern() {
 		return editionPattern;
 	}
 
 	public void setEditionPattern(EditionPattern editionPattern) {
 		this.editionPattern = editionPattern;
 		updateDrawingObjectEntries();
 	}
 
 	private void updateDrawingObjectEntries() {
 		drawingObjectEntries.clear();
 		int shapeIndex = 1;
 		int connectorIndex = 1;
 		for (GRTemplate o : getFocusedObject().getDescendants()) {
 			if (o instanceof GRShapeTemplate) {
 				GRShapeTemplate shape = (GRShapeTemplate) o;
 				String shapeRoleName = StringUtils.isEmpty(shape.getName()) ? "shape" + (shapeIndex > 1 ? shapeIndex : "") : shape
 						.getName();
 				drawingObjectEntries.add(new DrawingObjectEntry(shape, shapeRoleName));
 				shapeIndex++;
 			}
 			if (o instanceof GRConnectorTemplate) {
 				GRConnectorTemplate connector = (GRConnectorTemplate) o;
 				String connectorRoleName = "connector" + (connectorIndex > 1 ? connectorIndex : "");
 				drawingObjectEntries.add(new DrawingObjectEntry(connector, connectorRoleName));
 				connectorIndex++;
 			}
 		}
 
 	}
 
 	public ScreenshotImage getScreenshot() {
 		return screenshot;
 	}
 
 	public void setScreenshot(ScreenshotImage screenshot) {
 		this.screenshot = screenshot;
 		imageWidth = screenshot.image.getWidth(null);
 		imageHeight = screenshot.image.getHeight(null);
 	}
 
 	public File saveScreenshot() {
 		File imageFile = new File(getFocusedObject().getDiagramSpecification().getResource().getDirectory(),
 				JavaUtils.getClassName(newElementName) + ".palette-element" + ".png");
 		logger.info("Saving " + imageFile);
 		try {
 			ImageUtils.saveImageToFile(getScreenshot().image, imageFile, ImageType.PNG);
 			return imageFile;
 		} catch (IOException e) {
 			e.printStackTrace();
 			logger.warning("Could not save " + imageFile.getAbsolutePath());
 			return null;
 		}
 	}
 
 	public DiagramSpecification getDiagramSpecification() {
 		return getFocusedObject().getDiagramSpecification();
 	}
 }
