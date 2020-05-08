 package com.hexcore.cas.ui.toolkit;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.media.opengl.GL;
 
 import com.hexcore.cas.math.Vector2i;
 
 
 /*
  *  Renders components relative to the top right of the layout.
  *  Performs no modifications to the positions of the contained components.
  *  
  *  Can be derived to implement positioning of contained components. e.g. LinearLayout
  */
 public class Layout extends Widget
 {
 	protected String		themeClass = "";
 	protected Fill 			background = null;
 	protected Fill			border = null;
 	protected List<Widget>	components;
 	
 	protected Vector2i		lastMouse;
 	
 	public Layout(Vector2i size)
 	{
 		super(size);
 		components = new ArrayList<Widget>();
 		lastMouse = new Vector2i();
 	}
 	
 	public Layout()
 	{
 		super(new Vector2i(0, 0));
 		components = new ArrayList<Widget>();
 		lastMouse = new Vector2i();
 	}
 			
 	public void clear()
 	{
		components.clear();
 	}
 	
 	@Override
 	public void relayout()
 	{
 		synchronized(components)
 		{
 			for (Widget component : components)
 			{
 				Vector2i cPos = component.getPosition(), cSize = component.getSize();
 				
 				if (component.isSet(FILL_HORIZONTAL))
 				{
 					cPos.x = component.getMargin().x; 
 					cSize.x = size.x - component.getMargin().x * 2; 
 				}
 				else if (component.isSet(CENTER_HORIZONTAL))
 					cPos.x = (size.x - cSize.x) / 2;
 				
 				if (component.isSet(FILL_VERTICAL))
 				{
 					cPos.y = component.getMargin().y; 
 					cSize.y = size.y - component.getMargin().y * 2; 
 				}
 				else if (component.isSet(CENTER_VERTICAL))
 					cPos.y = (size.y - cSize.y) / 2;
 			}
 			
 			for (Widget component : components) component.relayout();
 		}
 	}
 	
 	@Override
 	public void update(Vector2i position, float delta)
 	{
 		if (!visible) return;
 		
 		super.update(position, delta);
 		
 		Vector2i pos = this.position.add(position);
 		
 		synchronized(components)
 		{
 			for (Widget component : components) component.update(pos, delta);
 		}
 	}
 	
 	@Override
 	public void render(GL gl, Vector2i position)
 	{
 		if (!visible) return;
 		
 		Vector2i pos = this.position.add(position);
 		
 		if (background != null) 
 			Graphics.renderRectangle(gl, pos, size, 0, background);
 		else if ((window != null) && !themeClass.isEmpty())
 			Graphics.renderRectangle(gl, pos, size, 0, window.getTheme().getFill(themeClass, "background", Fill.NONE));
 		
 		synchronized(components)
 		{
 			for (Widget component : components)
 				component.render(gl, pos);
 		}
 				
 		if (border != null)
 			Graphics.renderBorder(gl, pos, size, 0, border);	
 		else if ((window != null) && !themeClass.isEmpty())
 			Graphics.renderBorder(gl, pos, size, 0, window.getTheme().getFill(themeClass, "border", Fill.NONE));
 				
 		if (window.isDebugLayout()) 
 			Graphics.renderBorder(gl, pos, size, new Colour(1.0f, 0.5f, 0.0f));
 	}
 	
 	@Override
 	public boolean handleEvent(Event event, Vector2i position)
 	{	
 		if (event.type == Event.Type.MOUSE_MOTION)
 		{
 			lastMouse = event.position;
 		}
 		
 		synchronized(components)
 		{
 			for (Widget component : components)
 				component.receiveEvent(event, position);
 		}
 		
 		return false;
 	}
 	
 	public void setThemeClass(String className)
 	{
 		this.themeClass = "." + className;
 	}
 	
 	public void clearThemeClass()
 	{
 		this.themeClass = "";
 	}
 	
 	public void setBackground(Fill fill)
 	{
 		background = fill;
 	}
 	
 	public void setBorder(Fill fill)
 	{
 		border = fill;
 	}
 	
 	public void add(Widget component) 
 	{
 		synchronized(components)
 		{
 			components.add(component); 
 			component.setParent(this);
 		}
 		
 		if (window == null)
 			relayout();
 		else
 			window.relayout();
 	}
 }
