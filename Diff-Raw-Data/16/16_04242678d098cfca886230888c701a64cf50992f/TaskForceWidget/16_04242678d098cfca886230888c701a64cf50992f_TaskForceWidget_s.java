 package graphic;
 
 import java.util.Map.Entry;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 
 import state.Design;
 import state.TaskForce;
 
 public class TaskForceWidget implements UIListener
 {
 	private class Stack
 	{
 		float selected;
 		int max;
 		Design design;
 	}
 	
 // Internals ==========================================================================================================	
 	private TaskForce force;
 	private Image[] backgrounds;
 	private int[][] bckDeltas;
 	private Stack[] cache;
 	int hoverIndex;
 	int numSteps;
 
 // Public Methods =====================================================================================================
 	TaskForceWidget() throws SlickException
 	{
 		this.force = null;
 		this.hoverIndex = -1;
 		this.numSteps = 6;
 		backgrounds = new Image[] 
 			{
 				new Image("resources/fleetBase.png"),
 				new Image("resources/fleetExt1.png"),
 				new Image("resources/fleetExt2.png"),
 				new Image("resources/fleetExt3.png"),
 				new Image("resources/fleetExt4.png")
 			};
 		
 		bckDeltas = new int[][]
 			{
 				{	-74,	-60,	-121, -168,	-108 },
 				{	-119,	-121,	-105,	-129,	-169 }
 			};
 	}
 
 	/**
 	 * Sets the task force to be displayed by this
 	 * @param force
 	 */
 	void showForce(TaskForce force)
 	{
 		this.force = force;
 		
 		// Reset the selected values of the ships for this force to their maximum value.
 		if(force != null)
 		{
 			cache = new Stack[force.stacks().size()];
 			int i=0;
 			for(Entry<Design, Integer> entry : force.stacks().entrySet())
 			{
 				cache[i] = new Stack();
 				cache[i].design = entry.getKey();
 				cache[i].max = entry.getValue();
 				cache[i].selected = cache[i].max;
 				i++;
 			}
 		}
 	}
 
 	public void render(GameContainer gc, Graphics g)
 	{
 		// If no star is being displayed, do nothing.
 		if(force == null)
 			return;
 		
 		// Make it so drawing stars is always done in local coordinates.
 		Camera.instance().pushLocalTransformation(g, force.position());
 
 		// Decide how many segments to show.
 		int numStacks = force.stacks().size();
		for(int i=0; i<=numStacks/4 && i<5; i++)
 			backgrounds[i].draw(bckDeltas[0][i], bckDeltas[1][i]);
 
 		// Paint the icons and numbers.
 		for(int i=0; i<cache.length; i++)
 		{
 			// Draw the icon.
 			Vector2f pos = indexToCenterCoord(i);
 			cache[i].design.image().draw(pos.x-15, pos.y-15);
 			
 			// Calculate location and draw the count for the stack.
 			g.setColor(Color.orange);
 			float length = pos.length();
 			String number = Integer.toString((int)cache[i].selected);
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
 			
 			// Check if we also display the local information.
 			if(hoverIndex == i)
 			{
 				Render.titles.drawString(120, -100, cache[i].design.name());
 			}
 		}
 		
 		g.popTransform();
 	}
 	
 	
 	/**
 	 * Translates a placeholder index for a stack in this widget to a local coordinates (around widget's center).
 	 * @param index Index to translate, must be in the range 0-22.
 	 * @return A vector with the local coordinates, or null if not
 	 */
 	private Vector2f indexToCenterCoord(int index)
 	{
 		// Determine first 12 segments.
 		if(index < 12)
 		{
 			float angle = index*15.0f;
 			if(index%2 == 0)
 				angle = -angle - 15.0f;
 			
 			return new Vector2f(angle).scale(98.5f);
 		}
 		else
 		{
 			float angle = -(index-12)*10.0f - 180.0f;
 			if(index%2 == 1)
 				angle = -angle + 10.0f;
 
 			return new Vector2f(angle).scale(145.5f);
 		}
 	}
 	
 	/**
 	 * Finds the potential element in the UI that corresponds to a particular coordinate.
 	 * This method does not consider what parts of the widget are current being displayed or their content, just placejolder locations.
 	 * @param vector A 2-dimensional vector of screen coordinates centered on the current position.
 	 * @return An index between 0 and 22 that corresponds to the slot in the widget that matches that point. OR a code between -1 to -5 to indicate one of the available buttons (top to bottom) OR -10 if there is no match whatsoever.
 	 */
 	private int coordToIndex(Vector2f vector)
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
 		else if(123 < radius && radius < 168 )
 		{
 			// Second circle
 			int aux = (int)(angle - 10) / 20;
 			if(aux > 2)
 			{
 				if(aux < 9)
 					return 28 - aux*2;
 				else if(aux < 14)
 					return aux*2 - 5;
 			}
 		}
 
 		return -10;
 	}
 
 	@Override
 	public boolean screenCLick(float x, float y, int button)
 	{
 		// Check if visible.
 		if(force == null)
 			return false;
 		
 		// Get the index.
 		Vector2f local = new Vector2f(x, y).sub(Camera.instance().worldToScreen(force.position()));
 		int index = coordToIndex(local);
		if(index == -10)
 			return false;
 		
 		// Process if it's a button.
 		if(index < 0)
 		{
 			// TODO
 		}
 		
 		// Process if its a stack.
 		else
 		{
 			// This calculation may seem rather convoluted and the % operator may sound like a better idea, but this behavior is rather rare. 
 			// If we are close to the maximum, we want to go to 12 before going pass 12. Example to avoid: 0, 3, 6, 9, 12, 2, 5, 8, 11...
 			float step = Math.max(1.0f * cache[index].max / numSteps, 1.0f);
 			if(button == 0)
 			{
 				if(cache[index].selected == cache[index].max)
 					cache[index].selected = 0;
 				else
 					cache[index].selected = Math.min(cache[index].selected + step, cache[index].max);
 			}
 			else if(button == 1)
 			{
 				if(cache[index].selected < 1.0f)
 					cache[index].selected = cache[index].max;
 				else
 					cache[index].selected = Math.max(cache[index].selected - step, 0.0f);
 			}
 			System.out.println(cache[index].selected);
 		}
 		
 		return true;
 	}
 	
 	public void mouseMoved(int oldx, int oldy, int newx, int newy) 
 	{
 		// Check if we are active.
 		if(force == null)
 			return;
 		
 		// Get the index to display.
 		Vector2f local = new Vector2f(newx, newy).sub(Camera.instance().worldToScreen(force.position()));
 		hoverIndex = coordToIndex(local);
 	}
 
 }
