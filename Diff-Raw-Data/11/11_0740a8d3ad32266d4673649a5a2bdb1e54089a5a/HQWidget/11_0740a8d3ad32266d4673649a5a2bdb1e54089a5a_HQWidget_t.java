 package ui;
 
 import graphic.Camera;
 import graphic.Render;
 
 import java.util.List;
 
 import org.lwjgl.input.Mouse;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 
 import state.HQ;
 import state.HQ.QueuedUnit;
 import state.Unit;
 
 public class HQWidget extends IndexedDialog
 {
 // Internals ==========================================================================================================	
 	private HQ hq;
 	private Image[] backgrounds;
 	private int accumulated = 0;
 
 // Public Methods =====================================================================================================
 	public HQWidget() throws SlickException
 	{
 		this.hq = null;
 		
 		backgrounds = new Image[] 
 			{
 				new Image("resources/ui_base.png"),
 				new Image("resources/fleetExt1.png"),
 				new Image("resources/fleetExt2.png"),
 				new Image("resources/fleetExt3.png"),
 				new Image("resources/ui_hover.png"),
 			};
 	}
 
 	/**
 	 * Sets the task fleet to be displayed by this
 	 * @param hq
 	 */
 	public void showHQ(HQ hq)
 	{
 		this.hq = hq;
 	}
 
 	public void render(GameContainer gc, Graphics g)
 	{
 		if(hq == null)
 			return;
 		
 		// Make it so drawing stars is always done in local coordinates.
 		Camera.instance().pushLocalTransformation(g, hq.location().getPos());
 		
 		// Paint all backgrounds. TODO mix with single resource set or create single static background.
 		backgrounds[0].draw(-74, -119);
 		backgrounds[1].draw(-60, -121);
 		backgrounds[2].draw(-121, -105);
 
 		List<QueuedUnit> queue = hq.queue();
 		if(!queue.isEmpty())
 			backgrounds[3].draw(-168, -129);
 
 		// Display available units to build
 		List<Unit> options = hq.availableUnits();
 		for(int i=0; i<options.size(); i++)
 		{
 			Vector2f pos = indexToCoord(i);
 			options.get(i).image().draw(pos.x-15, pos.y-15);
 			// Check if we also display the local information.
 
 			if(hoverIndex == i)
 			{
 				backgrounds[4].draw(62, -119);
 				Render.titles.drawString(120, -100, options.get(i).name());
 			}
 		}
 		
 		// Display current queue (with numbers)
 		for(int i=0; i<5; i++)
 		{
 			Vector2f pos = indexToCoord(i+12);
 			if(i<queue.size())
 			{
 				QueuedUnit qu = queue.get(i);
 				qu.design.image().draw(pos.x-15, pos.y-15);
 				
 				// Calculate location and draw the count for the stack.
 				g.setColor(Color.orange);
 				float length = pos.length();
 				String number = Integer.toString((int)Math.ceil(qu.queued));
 				pos.normalise().scale(length + 10.0f);
 				g.fillRect(
 							pos.x - Render.normal.getWidth(number)/2,
 							pos.y - Render.normal.getHeight()/2,
 							Render.normal.getWidth(number),
 							Render.normal.getHeight());
 				Render.normal.drawString(
 							pos.x - Render.normal.getWidth(number)/2,
 							pos.y - Render.normal.getHeight()/2,
 							number, Color.black);
 				
 				if(hoverIndex == i+12)
 				{
 					backgrounds[4].draw(62, -119);
 					Render.titles.drawString(120, -100, qu.design.name());
 				}
 			}
 			else if(!queue.isEmpty())
 			{
 				String number = Integer.toString(i+1);
 				Render.normal.drawString(
 						pos.x - Render.normal.getWidth(number)/2,
 						pos.y - Render.normal.getHeight()/2,
 						number, Color.white);
 			}
 		}
 		
 		// Buttons
 		Vector2f pos = indexToCoord(-1);
 		String[] configs = {"n", "h", "f"};
 		String conf = configs[hq.outputConfig().ordinal()];
 		Render.normal.drawString(
 				pos.x - Render.normal.getWidth(conf)/2,
 				pos.y - Render.normal.getHeight()/2,
 				conf, Color.white);
 
 		pos = indexToCoord(-2);
 		Render.normal.drawString(
 				pos.x - Render.normal.getWidth("c")/2,
 				pos.y - Render.normal.getHeight()/2,
 				"c", Color.white);
 		
 		
 		g.popTransform();
 	}
 
 	/**
 	 * Updates this widget by looking for new input.
 	 */
 	public void mouseClick(int button, int delta)
 	{
 		// Not a valid action.
		if(hq == null || hoverIndex <= NO_INDEX)
 			return;
 		
 		// Buttons
 		if(hoverIndex < 0)
 		{
 			if(delta == 0)
 			{
 				if(hoverIndex == -1)
 					hq.setOutputConfig(HQ.OutputLevel.values()[(hq.outputConfig().ordinal() + 1) % HQ.OutputLevel.values().length]);
 				else if(hoverIndex == -2)
 					hq.queue().clear();
 				// TODO Other Buttons
 			}
 			return;
 		}
 
 		List<QueuedUnit> queue = hq.queue();
 		List<Unit> options = hq.availableUnits();
 		QueuedUnit unit = null;
 		
 		if(hoverIndex >= 12)
 		{
 			// Already existing item in the queue.
 			if(hoverIndex - 12 < queue.size())
 				unit = queue.get(hoverIndex - 12);
 		}
 		else if(hoverIndex < options.size())
 		{
 			// A valid design was clicked. If it is the same as last, no need to create a new one.
 			// Check if I need to add a new unit in the queue (if selected is not equal to last one)
 			if(!queue.isEmpty() && (options.get(hoverIndex) == queue.get(queue.size()-1).design))
 				unit = queue.get(queue.size()-1);
 			else if(queue.size() < 5)
 			{
 				unit = new QueuedUnit();
 				unit.design = options.get(hoverIndex);
 				unit.queued = 0;
 				queue.add(unit);
 			}
 		}
 		
 		// Add or remove from this queued unit.
 		if(unit != null)
 		{
 			// This is fairly complicated:
 			// - We don't want to add a fraction of a unit, since that would result in a unit being produced at fractional cost :-S
 			// - Also, we don't want to add depending on the number of times this method gets called.
 			// Result: calculate how many ships should I have added by now, and update.
 			if(delta == 0)
 				accumulated = 0;
 			int target = 1 + (int) Math.floor((double)delta * delta * delta / 1e9);
 
 			if(Mouse.isButtonDown(0))
 				unit.queued += target - accumulated;
 			else if(Mouse.isButtonDown(1))
 			{
 				unit.queued -= target - accumulated;
 				if(unit.queued < 1)
 					queue.remove(unit);
 			}
 			accumulated = target;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see ui.IndexedDialog#indexToCoord(int)
 	 */
 	@Override
 	protected Vector2f indexToCoord(int index)
 	{
 		// Determine first 12 segments.
 		if(index < 0)
 		{
 			float angle = -(1 + index)*20.0f -40.0f;
 			return new Vector2f(angle).scale(64.0f);
 		}
 		else if(index < 12)
 		{
 			float angle = index*15.0f;
 			if(index%2 == 0)
 				angle = -angle - 15.0f;
 			
 			return new Vector2f(angle).scale(98.5f);
 		}
 		else
 		{
 			float angle = -(index-12)*20.0f - 140.0f;
 			return new Vector2f(angle).scale(145.5f);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see ui.IndexedDialog#location()
 	 */
 	@Override
 	public Vector2f location()
 	{
 		return hq == null ? null : hq.location().getPos();
 	}
 
 	/* (non-Javadoc)
 	 * @see ui.IndexedDialog#coordToIndex(org.newdawn.slick.geom.Vector2f)
 	 */
 	@Override
 	protected int coordToIndex(Vector2f vector)
 	{
 		double angle = vector.getTheta();
 		double radius = vector.length();
 		
 		if(54 < radius && radius < 74 )
 		{
 			// Buttons
 			if(angle >= 310)
 				return -1 - (int)((angle - 310.0) / 20.0);
 			
 			if(angle <= 50)
 				return -1 - (int)((angle + 50) / 20.0);
 		}
 		if(76 < radius && radius < 121 )
 		{
 			// First circle, all of it works
 			int aux = (int)(360 - angle) / 30;
 			if(aux < 6)
 				return aux*2;
 			else
 				return 23 - aux*2; 
 		}
 		else if(123 < radius && radius < 168 && angle >= 130 && angle < 230)
 		{
 			return (int) ((130 - angle)/20.0f + 17);
 		}
 
 		return -10;
 	}
 
 
 }
