 package com.hexcore.cas.ui.toolkit;
 
 import javax.media.opengl.GL;
 
 import com.hexcore.cas.math.Vector2i;
 
 public class ScrollableContainer extends Container
 {	
 	enum DragState {NONE, VERTICAL, HORIZONTAL};
 	
 	private	Vector2i 	maxSize;
 	private Vector2i	viewSize;
 	private Vector2i	scrollPos;
 	private boolean		verticalScrollbar;
 	private boolean		horizontalScrollbar;
 	private DragState	dragState = DragState.NONE;
 	private Vector2i	dragStart;
 	
 	public ScrollableContainer(Vector2i size)
 	{
 		super(size);
 		scrollPos = new Vector2i();
 		dragStart = new Vector2i();
 		viewSize = new Vector2i(size);
 		maxSize = new Vector2i(size);
 	}
 	
 	public ScrollableContainer(Vector2i position, Vector2i size)
 	{
 		super(position, size);
 		scrollPos = new Vector2i();
 		dragStart = new Vector2i();
 		viewSize = new Vector2i(size);
 		maxSize = new Vector2i(size);
 	}
 	
 	public int		getInnerWidth() {return viewSize.x;}
 	public int		getInnerHeight() {return viewSize.y;}
 	public Vector2i	getInnerSize() {return viewSize;}
 
 	@Override
 	public void relayout()
 	{
 		if (contents == null) return;
 		
 		Vector2i cPos = contents.getPosition();
 		Vector2i contentsSize = contents.getSize();
 		Vector2i contentsOuterSize = contentsSize.add(contents.getMargin()).add(contents.getMargin());
 		
 		verticalScrollbar = horizontalScrollbar = false;
 
 		maxSize.x = size.x;
 		maxSize.y = size.y;
 		viewSize.x = size.x;
 		viewSize.y = size.y;
 		
 		if (!contents.isSet(FILL_HORIZONTAL)) maxSize.x = contentsOuterSize.x;
 		if (!contents.isSet(FILL_VERTICAL)) maxSize.y = contentsOuterSize.y;
 		
 		for (int i = 0; i < 2; i++) // Do this twice
 		{
 			if (maxSize.x > viewSize.x) 
 			{
 				horizontalScrollbar = true;
 				viewSize.y = size.y - window.getTheme().getScrollbarSize();
 			}
 			
 			if (maxSize.y > viewSize.y) 
 			{
 				verticalScrollbar = true;
 				viewSize.x = size.x - window.getTheme().getScrollbarSize();
 			}
 		}
 		
 		if (contents.isSet(FILL_HORIZONTAL))
 		{
 			cPos.x = contents.getMargin().x; 
 			contentsSize.x = maxSize.x - contents.getMargin().x * 2; 
 		}
 		else if (contents.isSet(CENTER_HORIZONTAL))
 			cPos.x = (maxSize.x - contentsSize.x) / 2;
 		else if (cPos.x < contents.getMargin().x)
 			cPos.x = contents.getMargin().x;
 		
 		if (contents.isSet(FILL_VERTICAL))
 		{
 			cPos.y = contents.getMargin().y; 
 			contentsSize.y = maxSize.y - contents.getMargin().y * 2; 
 		}
 		else if (contents.isSet(CENTER_VERTICAL))
 			cPos.y = (maxSize.y - contentsSize.y) / 2;
 		else if (cPos.y < contents.getMargin().y)
 			cPos.y = contents.getMargin().y;
 		
		scroll(0, 0);
 		contents.relayout();
 	}
 	
 	@Override
 	public void render(GL gl, Vector2i position)
 	{
 		if (!visible) return;
 		
 		Vector2i pos = this.position.add(position);	
 		
 		if (contents != null)
 		{
 			window.setClipping(gl, pos, size);
 			
 			if (background != null) 
 				Graphics.renderRectangle(gl, pos, size, 0, background);
 			else if ((window != null) && !themeClass.isEmpty())
 				Graphics.renderRectangle(gl, pos, size, 0, window.getTheme().getFill(themeClass, "background", Fill.NONE));
 			
 			contents.render(gl, pos.subtract(scrollPos));
 			window.resetView(gl);
 						
 			if (verticalScrollbar)
 				window.getTheme().renderVerticalScrollbar(gl, pos, size, scrollPos.y, maxSize.y, viewSize.y);
 
 			if (horizontalScrollbar) 
 				window.getTheme().renderHorizontalScrollbar(gl, pos, size, scrollPos.x, maxSize.x, viewSize.x);
 			
 			if (verticalScrollbar && horizontalScrollbar)
 				window.getTheme().renderScrollbarFill(gl, pos, size);
 			
 			Fill borderFill = Fill.NONE;
 			
 			if (border != null)
 				borderFill = border;
 			else if ((window != null) && !themeClass.isEmpty())
 				borderFill = window.getTheme().getFill(themeClass, "border", Fill.NONE);
 			
 			Graphics.renderBorder(gl, pos.subtract(1, 1), size.add(2, 2), 0, borderFill);
 		}
 	}
 	
 	@Override
 	public boolean handleEvent(Event event, Vector2i position)
 	{
 		boolean handled = false;
 		
 		if (event.type == Event.Type.MOUSE_MOTION)
 		{
 			if (dragState == DragState.VERTICAL)
 				setScrollY(event.position.y - dragStart.y);
 			else if (dragState == DragState.HORIZONTAL)
 				setScrollX(event.position.x - dragStart.x);			
 		}
 		else if (event.type == Event.Type.MOUSE_SCROLL)
 		{
 			if (contents.receiveEvent(event, position)) return true;
 			scroll(0, event.amount);
 			return true;
 		}
 		else if (event.type == Event.Type.MOUSE_CLICK)
 		{
 			if (event.pressed)
 			{
 				int scrollbarSize = window.getTheme().getScrollbarSize();
 				
 				if (event.position.x > position.x + size.x - scrollbarSize)
 				{
 					dragState = DragState.VERTICAL;
 					dragStart.set(event.position);
 				}
 				else if (event.position.y > position.y + size.y - scrollbarSize)
 				{
 					dragState = DragState.HORIZONTAL;
 					dragStart.set(event.position);
 				}				
 			}
 			else
 				dragState = DragState.NONE;
 		}
 		
 		if (!handled)
 		{
 			handled = contents.receiveEvent(event, position);
 		}
 		
 		return handled;
 	}
 	
 	private void setScrollX(int x)
 	{
 		scrollPos.x = x * maxSize.x / viewSize.x;
 		if (scrollPos.x < 0) scrollPos.x = 0;
 		if (scrollPos.x >= maxSize.x - viewSize.x) scrollPos.x = maxSize.x - viewSize.x;
 	}
 	
 	private void setScrollY(int y)
 	{
 		scrollPos.y = y * maxSize.y / viewSize.y;
 		if (scrollPos.y < 0) scrollPos.y = 0;
 		if (scrollPos.y >= maxSize.y - viewSize.y) scrollPos.y = maxSize.y - viewSize.y;
 	}
 	
 	private void scroll(int x, int y)
 	{
 		scrollPos.inc(x, y);
 		if (scrollPos.x >= maxSize.x - viewSize.x) scrollPos.x = maxSize.x - viewSize.x;
 		if (scrollPos.x < 0) scrollPos.x = 0;
 		
 		if (scrollPos.y >= maxSize.y - viewSize.y) scrollPos.y = maxSize.y - viewSize.y;
 		if (scrollPos.y < 0) scrollPos.y = 0;
 	}
 }
