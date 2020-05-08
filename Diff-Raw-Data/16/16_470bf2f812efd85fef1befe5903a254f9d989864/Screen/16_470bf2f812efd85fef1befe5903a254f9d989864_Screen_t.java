 /**
  * rarity – An extensible tiling window manager
  * 
  * Copyright © 2013  Mattias Andrée (maandree@member.fsf.org)
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package rarity;
 
 import java.util.*;
 
 
 /**
  * The class is used to get information about the existing screens in the environment
  * 
  * @author  Mattias Andrée, <a href="mailto:maandree@member.fsf.org">maandree@member.fsf.org</a>
  */
 public class Screen
 {
     /**
      * The screen's virtual resolution on the X-axis
      */
     public static final String RESOLUTION_X = "resolutionX";
     
     /**
      * The screen's virtual resolution on the Y-axis
      */
     public static final String RESOLUTION_Y = "resolutionY";
     
     /**
      * The screen's offset on the X-axis
      */
     public static final String OFFSET_X = "offsetX";
     
     /**
      * The screen's offset on the Y-axis
      */
     public static final String OFFSET_Y = "offsetY";
     
     /**
      * The screen's dots per inch setting
      */
     public static final String DOTS_PER_INCH = "dotsPerInch";
     
     /**
      * The left margin of the screen, that is how much on the left side that is occupied by docked windows
      */
     public static final String MARGIN_LEFT = "marginLeft";
     
     /**
      * The right margin of the screen, that is how much on the right side that is occupied by docked windows
      */
     public static final String MARGIN_RIGHT = "marginRight";
     
     /**
      * The top margin of the screen, that is how much on the top side that is occupied by docked windows
      */
     public static final String MARGIN_TOP = "marginTop";
     
     /**
      * The bottom margin of the screen, that is how much on the bottom side that is occupied by docked windows
      */
     public static final String MARGIN_BOTTOM = "marginBottom";
     
     /**
      * The unoccupied screen width
      */
     public static final String WIDTH = "width";
     
     /**
      * The unoccupied screen height
      */
     public static final String HEIGHT = "height";
     
     
     
     /**
      * Constructor
      * 
      * @param  width        The screen's virtual resolution on the X-axis
      * @param  height       The screen's virtual resolution on the Y-axis
      * @param  left         The screen's offset on the X-axis
      * @param  top          The screen's offset on the Y-axis
      * @param  dotsPerInch  The screen's dots per inch setting
      */
     protected Screen(final int width, final int height, final int left, final int top, final int dotsPerInch)
     {
 	this.set(RESOLUTION_X, width);
 	this.set(RESOLUTION_Y, height);
 	this.set(OFFSET_X, left);
 	this.set(OFFSET_Y, top);
 	this.set(DOTS_PER_INCH, dotsPerInch);
 	this.set(MARGIN_LEFT, 0);
 	this.set(MARGIN_RIGHT, 0);
 	this.set(MARGIN_TOP, 0);
 	this.set(MARGIN_BOTTOM, 0);
 	this.set(WIDTH, new Lambda()
 	    {
 		@Override
 		public Object evaluate(final Object... params)
 		{
 		    final Screen screen = (Screen)(params[0]);
 		    return screen.getInteger(RESOLUTION_X) - screen.getInteger(MARGIN_LEFT) - screen.getInteger(MARGIN_RIGHT);
 		}
 	    });
 	this.set(HEIGHT, new Lambda()
 	    {
 		@Override
 		public Object evaluate(final Object... params)
 		{
 		    final Screen screen = (Screen)(params[0]);
 		    return screen.getInteger(RESOLUTION_Y) - screen.getInteger(MARGIN_TOP) - screen.getInteger(MARGIN_BOTTOM);
 		}
 	    });
     }
     
     
     
     /**
      * Map for the screens properties
      */
     private HashMap<String, Object> properties = new HashMap<String, Object>();
     
     
     /**
      * All screen in index order
      */
    protected static Vector<Screen> screens = new Vector<Screen>();
     
     
     
     /**
      * Blackboard message for {@link Screen} property updates
      * 
      * @author  Mattias Andrée, <a href="mailto:maandree@member.fsf.org">maandree@member.fsf.org</a>
      */
    public static class Message implements Blackboard.BlackboardMessage
     {
 	/**
 	 * Constructor
 	 * 
 	 * @param  property  The updated property
 	 */
 	public Message(final Screen screen, final String property)
 	{
 	    this.screen = screen;
 	    this.property = property;
 	    int i = 0;
 	    for (final Screen scr : Screen.screens)
 		if (scr == screen)
 		    break;
 		else
 		    i++;
 	    this.index = i;
 	}
 	
 	
 	
 	/**
 	 * The updated screen
 	 */
 	public final Screen screen;
 	
 	/**
 	 * The index of the updated screen
 	 */
 	public final int index;
 	
 	/**
 	 * The updated property
 	 */
 	public final String property;
 	
     }
     
     
     
     /**
      * Gets a screen by its index
      * 
      * @param   index  The screen's index
      * @return         The screen
      */
     public static Screen getScreen(final int index)
     {
 	return screens.get(index);
     }
     
     /**
      * Gets the number of screens on the system
      * 
      * @return  The number of screens on the system
      */
     public static int getScreenCount()
     {
 	return screens.size();
     }
     
     
     /**
      * Gets a property from the screen
      * 
      * @param   property  The property
      * @return            The property value as an {@link Object}
      */
     public Object get(final String property)
     {
 	Object obj = this.properties.get(property);
 	if ((obj == null) || (obj instanceof Lambda == false))
 	    return obj;
 	return ((Lambda)obj).evaluate(this);
     }
     
     /**
      * Gets a property from the screen
      * 
      * @param   property  The property
      * @return            The property value as an {@link String}
      */
     public String getString(final String property)
     {
 	Object obj = get(property);
 	if ((obj == null) || (obj instanceof String == false))
 	    return null;
 	return (String)obj;
     }
     
     /**
      * Gets a property from the screen
      * 
      * @param   property  The property
      * @return            The property value as an {@code int}
      */
     public int getInteger(final String property)
     {
 	Object obj = this.properties.get(property);
 	if ((obj == null) || (obj instanceof Integer == false))
 	    return 0;
	return ((Integer)obj).intValue();
     }
     
     
     /**
      * Sets a property for the screen
      * 
      * @param  property  The property
      * @param  value     The property value
      */
     public void set(final String property, final Object value)
     {
	this.properties.put(property, value);
	Blackboard.getInstance(Screen.class).broadcastMessage(new Screen.Message(this, property));
     }
     
     /**
      * Sets a property for the screen
      * 
      * @param  property  The property
      * @param  value     The property value
      */
     public void set(final String property, final int value)
     {
	this.set(property, Integer.valueOf(value));
     }
     
 }
 
