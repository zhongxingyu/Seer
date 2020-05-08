 package org.orbisgis.tinterface.main;
 
 import org.mt4j.MTApplication;
 import org.mt4j.components.visibleComponents.shapes.MTRectangle;
 import org.mt4j.components.visibleComponents.widgets.MTImage;
 import org.mt4j.components.visibleComponents.widgets.MTListCell;
 import org.mt4j.components.visibleComponents.widgets.MTTextArea;
 import org.mt4j.util.MTColor;
 import org.mt4j.util.font.IFont;
 import org.mt4j.util.math.Vector3D;
 import org.orbisgis.core.layerModel.ILayer;
 
 /**
  * Cell of the LayerList that has the properties of a MTListCell and new attributes and methods to link the cell with a layer
  * @author Adri
  *
  */
 public class LayerCell extends MTListCell {
 	/** State of the map (displayed or not) */
 	private boolean mapAlreadyInPlace;
 	
 	/** the Map/Layer */
 	ILayer layer;
 
 	/** the Thumbnail */
 	MTRectangle thumbnail;
 	
 	/** the Label */
 	String label;
 	
 	/** The actual color  */
 	MTColor actualColor = this.getFillColor();	
 	
 	public boolean isMapAlreadyInPlace() {
 		return mapAlreadyInPlace;
 	}
 	public void setMapAlreadyInPlace(boolean mapAlreadyInPlace) {
 		this.mapAlreadyInPlace = mapAlreadyInPlace;
 	}
 	
 	public ILayer getLayer() {
 		return layer;
 	}
 	public void setLayer(ILayer layer) {
 		this.layer = layer;
 	}
 
 	public String getLabel() {
 		return label;
 	}
 	public void setLabel(String label) {
 		this.label = label;
 	}
 	public void setLabel(String label, MTApplication mtApplication, IFont font) {
 		this.label = label;
 		
 		// TODO : Find a way to get the Label Child 
 		//this.removeChild(1);
 		
 		// Label is initialized here ..
 		MTTextArea listLabel = new MTTextArea(mtApplication, font);
 		listLabel.setNoFill(true);
 		listLabel.setNoStroke(true);
 		listLabel.setText(label);
 		Vector3D positionText = this.getCenterPointLocal();
 		positionText.y +=30; 
 		listLabel.setPositionRelativeToParent(positionText);
 		// .. and added here
 		this.addChild(listLabel);
 		
 	}
 	
 	public MTRectangle getThumbnail() {
 		return thumbnail;
 	}
 	public void setThumbnail(MTImage thumbnail) {
 		this.thumbnail = thumbnail;
 	}
 
 	public MTColor getActualColor() {
 		return actualColor;
 	}
 	public void setActualColor(MTColor actualColor) {
 		this.setFillColor(actualColor);
 		this.actualColor = actualColor;
 	}
 
 	/**
 	 * Constructor of our LayerCell 
 	 * @param mtApplication
 	 * @param font : font to write the label
 	 * @param cellFillColor : default color of the cell
 	 * @param cellWidth : Width of the cell
 	 * @param cellHeight : Height of the cell
 	 */
 	public LayerCell(ILayer layer, MTApplication mtApplication, IFont font, final MTColor cellFillColor,float cellWidth, float cellHeight) {
 		super(mtApplication, cellWidth, cellHeight);
 		this.setLayer(layer);
 		
		setMapAlreadyInPlace(false);
 		this.setChildClip(null); //FIXME TEST, no clipping for performance!
 
 		this.setFillColor(cellFillColor);
 		// Thumbnail is added here (now just a white rectangle)
 		MTRectangle thumbnail = new MTRectangle (mtApplication, 0,1,(int)(Math.floor(cellWidth)),(int)(Math.floor(0.70*cellHeight)) );
 		//thumbnail.setTexture(layer.getThumbnail());
 		this.addChild(thumbnail);
 
 		this.setLabel(layer.getName(), mtApplication, font);
 	}
 
 }
