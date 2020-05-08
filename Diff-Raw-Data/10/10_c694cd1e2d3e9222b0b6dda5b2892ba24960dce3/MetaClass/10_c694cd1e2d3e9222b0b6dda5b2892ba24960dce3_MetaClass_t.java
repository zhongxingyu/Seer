 package org.gemsjax.client.admin.model.metamodel;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.text.html.parser.AttributeList;
 
 import org.gemsjax.client.admin.model.metamodel.exception.AttributeNameException;
 import org.gemsjax.client.canvas.Drawable;
 import org.gemsjax.client.canvas.ResizeArea;
 import org.gemsjax.client.canvas.events.MoveEvent;
 import org.gemsjax.client.canvas.events.ResizeEvent;
 import org.gemsjax.client.canvas.handler.MouseOverHandler;
 import org.gemsjax.client.canvas.handler.MoveHandler;
 import org.gemsjax.client.canvas.handler.ResizeHandler;
 
 import com.google.gwt.canvas.dom.client.Context2d;
 import com.google.gwt.user.client.ui.RichTextArea.FontSize;
 
 
 
 
 /**
  * This class represents a MetaClass. A MetaClass must be added to a {@link MetaModel} and sho
  * @author Hannes Dorfmann
  *
  */
 public class MetaClass implements Drawable, ResizeHandler, MoveHandler, MouseOverHandler{
 
 	// Drawable fields
 	
 	private double x, y,z;
 	private double width = 100, height = 200, minWidth = 30, minHeight = 30;
 	
 	private String borderColor;
 	private String backgroundColor;
 	
 	/**
 	 * The font family name. For an easier calculation of the width of a text you should allways use a monospace font.
 	 * <b>If you change the font, you also have to recalculate {@link #nameFontCharWidth} and {@link #attributeFontCharWidth}. <b>
 	 * @see MetaClass#nameFontCharWidth
 	 */
 	private String fontFamily ="Courier";
 	
 	/**
 	 * The font color of the class name
 	 */
 	private String nameFontColor;
 	
 	/**
 	 * The text color for attributes
 	 */
 	private String attributeFontColor;
 	
 	/**
 	 * The size in pixel of the class name
 	 */
 	private int nameFontSize;
 	
 	/**
 	 * The font size of the attributes
 	 */
 	private int attributeFontSize;
 	
 	/**
 	 * This field will store the width of a single character in the given {@link #fontFamily} for the meta class name.
 	 * This field is used to calculate the width of the meta class name.
 	 * @see #fontFamily
 	 */
 	private double nameFontCharWidth;
 	
 	/**
 	 * This field will store the width of a single character in the given {@link #fontFamily} for the attribute text.
 	 * This field is used to calculate the width of a whole attribute text.
 	 * @see #fontFamily
 	 */
 	private double attributeFontCharWidth;
 	
 	
 	
 	
 	/**
 	 * The free space (in pixel) between one attribute to the other attribute (which is displayed in the line below)
 	 */
 	private double attributeToAttributeSpace= 5;
 	
 	/**
 	 * The space(in pixel) between the painted name of this meta class and the list of attributes
 	 */
 	private double nameToAttributeSpace = 10;
 	
 	/**
 	 * The space (in pixel) between the border and the top of the name of this meta class
 	 */
 	private double nameTopSpace=3;
 	
 	/**
 	 * The space (in pixel) between the left border and the (painted) name of this meta class
 	 */
 	private double nameLeftSpace=3;
 	
 	/**
 	 * The space (in pixel) between the left border and the textual (painted) attribute
 	 */
 	private double attributeLeftSpace = 3;
 	
 	
 	private boolean canBeMoved;
 	private boolean canBeResized;
 	private boolean selected;
 	private boolean mouseOver;
 	
 	
 	private int borderSize;
 	
 	private List<ResizeArea> resizeAreas;
 	
 	private List<MoveHandler> moveHandlers;
 	private List<ResizeHandler> resizeHandlers;
 	private List<MouseOverHandler> mouseOverHandlers;
 	
 
 	
 	// MetaClass Data fields
 
 	/**
 	 * The name of this MetaClass
 	 */
 	private String name;
 	
 	/**
 	 * TODO What is Root?
 	 */
 	private boolean isRoot;
 	/**
 	 * Is this MetaClass abstract?
 	 */
 	private boolean isAbstract;
 	
 	private List <Attribute>attributeList;
 	
 	private List <Connection> connectionList;
 	
 	private List<InheritanceRelation> inheritanceRelationList;
 
 	public MetaClass(String name, double x, double y)
 	{
 		this(x,y);
 		this.name = name;
 	}
 	
 	
 	public MetaClass(String name)
 	{
 		this(100,100);
 		this.name = name;
 	}
 		
 	public MetaClass(double x, double y) {
 		 
		this.x = x;
		this.y = y;
 
 		 // Drawbale Settings
 		 backgroundColor = "white";
 		
 		 borderSize = 1;
 		 borderColor = "black";
 		 nameFontColor ="black";
 		 attributeFontColor="black";
 		 nameFontSize = 18;
 		 attributeFontSize=14;
 		 
 		 canBeMoved = true;
 		 selected = false;
 		 mouseOver = false;
 		 canBeResized = true;
 		 
 		 // Handlers
 		 resizeAreas = new LinkedList<ResizeArea>();
 		 moveHandlers = new LinkedList<MoveHandler>();
 		 resizeHandlers = new LinkedList<ResizeHandler>();
 		 mouseOverHandlers = new LinkedList<MouseOverHandler>();
 		 
 		 // a Resize Area
 		 resizeAreas.add(new ResizeArea(x+width-6, y+height-6, 6, 6));
 		 
 		 // Lists
 		 attributeList = new ArrayList<Attribute>();
 		 inheritanceRelationList = new ArrayList<InheritanceRelation>();
 		 connectionList = new ArrayList<Connection>();
 		 
 
 		 this.addMouseOverHandler(this);
 		 this.addMoveHandler(this);
 		 this.addResizeHandler(this);
 		
 	}
 	
 	@Override
 	public double getX() {
 		return x;
 	}
 
 	@Override
 	public double getY() {
 		return y;
 	}
 
 	@Override
 	public double getZ() {
 		return z;
 	}
 
 	@Override
 	public void setX(double x) {
 		this.x = x;
 		
 	}
 
 	@Override
 	public void setY(double y) {
 		this.y=y;
 	}
 
 	@Override
 	public void setZ(double z) {
 		this.z=z;
 	}
 
 	
 	private boolean isBetween(double minValue, double maxValue, double valueToCheck)
 	{
 		return valueToCheck>=minValue && valueToCheck<=maxValue;
 	}
 	
 	@Override
 	public boolean hasCoordinate(double x, double y) {
 		return (isBetween(this.x, this.x+width, x) && isBetween(this.y, this.y+height,y));
 	}
 
 	@Override
 	public void draw(Context2d context) {
 		
 		
 		context.setFillStyle(borderColor);
 		context.fillRect(x, y, width, height);
 		context.setFillStyle(backgroundColor);
 		context.fillRect(x+borderSize, y+borderSize, width-2*borderSize, height-2*borderSize);
 		
 		drawName(context);
 		drawAttributes(context);
 		
 	}
 
 	@Override
 	public void drawOnMouseOver(Context2d context) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void drawOnSelected(Context2d context) {
 		
 		draw(context);

 		if (isSelected())
 		for (ResizeArea ra : resizeAreas)
 			ra.draw(context);
				
 	}
 	
 	
 	/**
 	 * Draw the Attributes, which should be displayed for this class
 	 */
 	public void drawAttributes(Context2d context){
 		
 		if (attributeList.size()==0)
 			return;
 		
 		context.setFillStyle(attributeFontColor);
 		context.setFont(""+attributeFontSize+"px "+fontFamily);
 		context.setTextAlign("left");
 		
 		double x = this.x + attributeLeftSpace, y=this.y+nameTopSpace+nameFontSize+nameToAttributeSpace;
 		
 		for (Attribute a: attributeList)
 		{
 			context.fillText(a.getName()+" : "+a.getType(), x, y);
 			y+=attributeFontSize+attributeToAttributeSpace;
 		}
 		
 	}
 	
 	/**
 	 * Draw the Classname somewhere
 	 */
 	private void drawName(Context2d context){
 		
 		context.setFillStyle(nameFontColor);
 		context.setFont("bold "+nameFontSize+"px "+fontFamily);
 		
 		context.setTextAlign("left");
 		context.fillText(name, x+nameLeftSpace, y+nameTopSpace+nameFontSize);
 		
 		
 		// if there is at least one attribute, draw a horizontal line
 		if (attributeList.size()>0)
 		{
 			context.setFillStyle(borderColor);
 			context.fillRect(x, y+nameFontSize+(nameToAttributeSpace/2), width, borderSize);
 		}
 			
 			
 		
 	}
 
 	@Override
 	public boolean isMoveable() {
 		return canBeMoved;
 	}
 
 	@Override
 	public double getWidth() {
 		return width;
 	}
 
 	@Override
 	public double getHeight() {
 		return height;
 	}
 
 	@Override
 	public void setWidth(double width) {
 		this.width = width;
 	}
 
 	@Override
 	public void setHeight(double height) {
 		this.height = height;
 	}
 
 	@Override
 	public boolean isResizeable() {
 			return canBeResized;
 	}
 
 	@Override
 	public void setMinWidth(double minWidth) {
 		this.minWidth = minWidth;
 	}
 
 	@Override
 	public double getMinWidth() {
 		
 		return minWidth;
 	}
 
 	@Override
 	public void setMinHeight(double minHeight) {
 		this.minHeight = minHeight;
 	}
 
 	@Override
 	public double getMinHeight() {
 		return minHeight;
 	}
 
 	@Override
 	public void setSelected(boolean selected) {
 		this.selected = selected;
 	}
 
 	@Override
 	public boolean isSelected() {
 		return selected;
 	}
 
 	@Override
 	public void onResize(ResizeEvent event) {
 		
 		if (isResizeable() && event.getWidth()>getMinWidth() && event.getHeight()>getMinHeight())
 		{
 			
 			for (ResizeArea r : resizeAreas)
 			{
 				r.setX(r.getX()+ (event.getWidth()-this.getWidth()));
 				r.setY(r.getY()+ (event.getHeight()-this.getHeight()));
 			}
 			
 			this.setWidth(event.getWidth());
 			this.setHeight(event.getHeight());
 			
 		}
 		
 		
 		
 		
 	}
 
 	@Override
 	public void onMove(MoveEvent e) {
 		
 		double oldX = getX();
 		double oldY = getY();
 		
 		this.setX(e.getX()-e.getDistanceToTopLeftX());
 		this.setY(e.getY()-e.getDistanceToTopLeftY());
 		
 		//this.setX(getX()+e.getX() - e.getStartX());
 		//this.setY(getY()+e.getY() - e.getStartY());
 		
 		// Set the Position of the ResizeAreas
 		for (ResizeArea ra : resizeAreas)
 		{
 			ra.setX(ra.getX() + (getX()-oldX));
 			ra.setY(ra.getY() + (getY()-oldY));
 		}
 		
 		
 		
 	}
 
 	@Override
 	public void addResizeHandler(ResizeHandler resizeHandler) {
 		resizeHandlers.add(resizeHandler);
 	}
 
 	@Override
 	public void removeResizeHandler(ResizeHandler resizeHandler) {
 		resizeHandlers.remove(resizeHandler);
 	}
 
 	@Override
 	public void addMoveHandler(MoveHandler moveHandler) {
 		moveHandlers.add(moveHandler);
 	}
 
 	@Override
 	public void removeMoveHandler(MoveHandler moveHandler) {
 		moveHandlers.remove(moveHandler);
 	}
 
 	@Override
 	public List<MoveHandler> getMoveHandlers() {
 		return moveHandlers;
 	}
 
 	@Override
 	public List<ResizeHandler> getResizeHandlers() {
 		return resizeHandlers;
 	}
 
 	@Override
 	public boolean isMouseOver() {
 		return mouseOver;
 	}
 
 	@Override
 	public void setMouseOver(boolean mouseOver) {
 		this.mouseOver = mouseOver;
 	}
 
 	@Override
 	public void addMouseOverHandler(MouseOverHandler mouseOverHandler) {
 		mouseOverHandlers.add(mouseOverHandler);
 	}
 
 	@Override
 	public void removeMouseOverHandler(MouseOverHandler mouseOverHandler) {
 		mouseOverHandlers.remove(mouseOverHandler);
 	}
 
 	@Override
 	public List<MouseOverHandler> getMouseOverHandlers() {
 		return mouseOverHandlers;
 	}
 
 	@Override
 	public void onMouseOver(double x, double y) {
 		// TODO What to do when mouse is over. Let a Menu appear
 	}
 
 	@Override
 	public ResizeArea isResizerAreaAt(double x, double y) {
 		for (ResizeArea r :resizeAreas)
 			if (r.hasCoordinate(x, y))
 				return r;
 		
 		return null;
 	}
 
 	public String getBorderColor() {
 		return borderColor;
 	}
 
 	public void setBorderColor(String borderColor) {
 		this.borderColor = borderColor;
 	}
 
 	public int getBorderSize() {
 		return borderSize;
 	}
 
 	public void setBorderSize(int borderSize) {
 		this.borderSize = borderSize;
 	}
 
 	public String getBackgroundColor() {
 		return backgroundColor;
 	}
 
 	public void setBackgroundColor(String backgroundColor) {
 		this.backgroundColor = backgroundColor;
 	}
 
 	
 	
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public boolean isRoot() {
 		return isRoot;
 	}
 
 	public void setRoot(boolean isRoot) {
 		this.isRoot = isRoot;
 	}
 
 	public boolean isAbstract() {
 		return isAbstract;
 	}
 
 	public void setAbstract(boolean isAbstract) {
 		this.isAbstract = isAbstract;
 	}
 
 	/*
 	public List<Attribute> getAttributeList() {
 		return attributeList;
 	}
 
 	
 	public List<Connection> getConnectionList() {
 		return connectionList;
 	}
 
 	public List<InheritanceRelation> getInheritanceRelationList() {
 		return inheritanceRelationList;
 	}
 	*/
 	
 	
 	public void addAttribute(String name, String type) throws AttributeNameException
 	{
 		for(Attribute a : attributeList)
 			if (a.getName().equals(name))
 				throw new AttributeNameException(name,this, "An Attribute with the name "+name+" already exists");
 		
 		attributeList.add(new Attribute(name, type));
 	}
 	
 	/**
 	 * Remove a {@link Attribute} (by searching the attribute according to the attribute name)
 	 * If the Attribute was not found, nothing will be done.
 	 * @param name
 	 */
 	public void removeAttribute(String name)
 	{
 		for (Attribute a: attributeList)
 		{
 			if (a.getName().equals(name))
 			{
 				// TODO check if this will throw a java iterator exception
 				attributeList.remove(a);
 				return;
 			}
 		}
 	}
 	
 	
 	
 	
 
 }
