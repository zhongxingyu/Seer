 package com.hexcore.cas.ui;
 
 import javax.media.opengl.GL;
 
 import com.hexcore.cas.math.Vector2i;
 
 
 /*
  * Only contains one component, a decorator in design patterns.
  * 
  * Treats inner component as is. No positioning is done except
  * if the inner component has either a FILL or CENTER flag set.
  * It is recommended that a Layout is used to positions 
  * components and a Container is used to decorate a Layout.
  * 
  */
 public class Container extends Widget
 {
 	protected Widget contents = null;
 	protected Fill background = null;
 	
 	public Container(Vector2i size)
 	{
 		super(size);
 	}
 	
 	public Container(Vector2i position, Vector2i size)
 	{
 		super(position, size);
 	}
 	
 	@Override
 	public void relayout()
 	{
 		if (contents == null) return;
 
 		Vector2i cPos = contents.getPosition(), cSize = contents.getSize();
 		
 		if (contents.isSet(FILL_HORIZONTAL))
 		{
 			cPos.x = contents.getMargin().x; 
 			cSize.x = size.x - contents.getMargin().x * 2; 
 		}
 		else if (contents.isSet(CENTER_HORIZONTAL))
 			cPos.x = (size.x - cSize.x) / 2;
 		
 		if (contents.isSet(FILL_VERTICAL))
 		{
 			cPos.y = contents.getMargin().y; 
 			cSize.y = size.y - contents.getMargin().y * 2; 
 		}
 		else if (contents.isSet(CENTER_VERTICAL))
 			cPos.y = (size.y - cSize.y) / 2;	
 		
 		contents.relayout();
 	}
 	
 	@Override
 	public void update(Vector2i position, float delta)
 	{
 		if (!visible) return;
 		
 		super.update(position, delta);
 		
 		Vector2i pos = this.position.add(position);
 		if (contents != null) contents.update(pos, delta);
 	}
 	
 	@Override
 	public void render(GL gl, Vector2i position)
 	{
 		if (!visible) return;
 		
 		Vector2i pos = this.position.add(position);
 		window.setClipping(gl, pos, size);
 		if (background != null) window.renderRectangle(gl, pos, size, 0, background);
 		if (contents != null) contents.render(gl, pos);
 		window.resetView(gl);
 		
 		if (window.isDebugLayout())
 			window.renderBorder(gl, pos, size, new Colour(0.0f, 1.0f, 0.5f));
 	}
 		
 	@Override
 	public boolean handleEvent(Event event, Vector2i position)
 	{
 		if (contents == null) return false;
 		
 		boolean handled = false;
 				
 		if (contents.receiveEvent(event, position)) handled = true;
 		
 		return handled;
 	}
 	
 	public void setBackground(Fill fill)
 	{
 		background = fill;
 	}
 	
 	public void setContents(Widget component) 
 	{
 		this.contents = component;
 		component.setParent(this);
		relayout();
 	}
 }
