 package com.hexcore.cas.ui.toolkit;
 
 import com.hexcore.cas.math.Vector2i;
 
 public class ClickableWidget extends Widget
 {
 	public ClickableWidget(Vector2i position, Vector2i size)
 	{
 		super(position, size);
 	}
 
 	public ClickableWidget(Vector2i size)
 	{
 		super(size);
 	}
 	
 	public void 	handleClick() {}
 	
 	@Override	
 	public boolean handleEvent(Event event, Vector2i position)
 	{
 		boolean handled = super.handleEvent(event, position);
 		
 		if (event.type == Event.Type.MOUSE_CLICK)
 		{
 			boolean wasActive = active;
 			active = event.pressed;
 	
 			if (wasActive && !active && mouseover)
 			{
 				handleClick();
 				
 				Event clickEvent = new Event(Event.Type.ACTION);
 				clickEvent.target = this;
 				window.sendWindowEvent(clickEvent);
 			}
 			
 			handled = true;
 		}
 		else if (event.type == Event.Type.MOUSE_MOTION)
		{
			if ((position.x <= event.position.x) && (position.y <= event.position.y) &&
					(position.x + this.size.x >= event.position.x) && (position.y + this.size.y >= event.position.y))
				window.requestTooltip(this);
		}
 		
 		return handled;
 	}
 }
