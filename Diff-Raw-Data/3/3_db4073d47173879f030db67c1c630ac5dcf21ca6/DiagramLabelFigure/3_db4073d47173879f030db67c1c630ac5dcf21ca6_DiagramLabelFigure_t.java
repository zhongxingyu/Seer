 package org.lh.dmlj.schema.editor.figure;
 
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.Figure;
 import org.eclipse.draw2d.FigureUtilities;
 import org.eclipse.draw2d.Label;
 import org.eclipse.draw2d.PositionConstants;
 import org.eclipse.draw2d.XYLayout;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.swt.graphics.Font;
 import org.lh.dmlj.schema.editor.Plugin;
 
 public class DiagramLabelFigure extends Figure {
 	
 	private Label descriptionFigure;
 	private Label organisationFigure;
 	private Label schemaIdentificationFigure;
 	
 	public static Dimension getInitialSize(String organisation, String schemaName, 
 										   short schemaVersion, String description) {
 				
 		// first calculate the size of each individual item...
 		Dimension sizeOrganisation =
 			FigureUtilities.getTextExtents(organisation, Plugin.getDefault().getFigureFont());
 		String schemaIdentification = getSchemaIdentificationText(schemaName, schemaVersion);
 		Dimension sizeSchemaIdentification = 
 			FigureUtilities.getTextExtents(schemaIdentification, 
 										   Plugin.getDefault().getFigureFontBold());
 		Dimension sizeDescription = 
			FigureUtilities.getTextExtents((description != null ? description : ""), 
										   Plugin.getDefault().getFigureFontItalic());
 		
 		// ... then determine the initial width; the width is the largest one required - because of 
 		// some strange behaviour issue when the zoom level exceeds 1.0 (100%), we add 5 pixels - 
 		// whereas the height is fixed to 35 pixels; at 75%, the diagram label seems oversized for
 		// what the width is concerned
 		Dimension size = new Dimension();
 		size.width = 
 			Math.max(sizeOrganisation.width, 
 					 Math.max(sizeSchemaIdentification.width, sizeDescription.width)) + 5;
 		size.height = 35;
 		
 		return size;
 	}
 
 	private static String getSchemaIdentificationText(String schemaName, short schemaVersion) {
 		String targetSchemaName = 
 			schemaName != null && !schemaName.trim().equals("") ? schemaName : "?";
 		String targetSchemaVersion = schemaVersion > 0 ? String.valueOf(schemaVersion) : "?";
 		return targetSchemaName + " version " + targetSchemaVersion;
 	}
 
 	public DiagramLabelFigure() {
 		super();
 		
 		setBackgroundColor(ColorConstants.white);
 		setOpaque(true);
 		setForegroundColor(ColorConstants.black);
 		
 		XYLayout layout = new XYLayout();
 		setLayoutManager(layout);
 		
 		setPreferredSize(10, 10);
 		
 		organisationFigure = addLabel(0, 0, 200, 14, Plugin.getDefault().getFigureFont());
 		schemaIdentificationFigure = 
 			addLabel(0, 10, 200, 14, Plugin.getDefault().getFigureFontBold());
 		descriptionFigure = addLabel(0, 21, 200, 14, Plugin.getDefault().getFigureFontItalic());
 		
 	}
 	
 	private Label addLabel(int x, int y, int width, int height, Font font) {
 		Label label = new Label();
 		label.setLabelAlignment(PositionConstants.LEFT);
 		label.setFont(font);
 		add(label, new Rectangle(new Point(x, y), new Dimension(width, height)));
 		return label;
 	}
 
 	public void setDescription(String description) {
 		String value = description != null ? description : "";
 		descriptionFigure.setText(value);
 	}
 	
 	public void setOrganisation(String organisation) {
 		String value = organisation != null ? organisation : "";
 		organisationFigure.setText(value);
 	}
 	
 	public void setSchemaIdentification(String schemaName, short schemaVersion) {
 		String value = getSchemaIdentificationText(schemaName, schemaVersion);
 		schemaIdentificationFigure.setText(value);
 	}
 	
 }
