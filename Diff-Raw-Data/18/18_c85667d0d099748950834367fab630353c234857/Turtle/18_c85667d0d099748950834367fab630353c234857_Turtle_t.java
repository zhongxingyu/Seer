 /* ******************************************************
  * * Copyright (C) 2013 Cepheus This file is part of L-Systems** L-Systems is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by* the Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should
  * have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
  * *****************************************************
  */
 /* ******************************************************
  * *
  * * Project: L-Systems File: Turtle.java Created on: 08 march. 2013 Author: Cepheus *****************************************************
  */
 
 package ATuin;
 
 import java.util.ArrayList;
 import java.util.concurrent.Callable;
 
 import com.jme3.math.Quaternion;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Node;
 
 import parser.BadSymbolException;
 import parser.GeneratorPseudoListener;
 import parser.ListSymbols;
 
 
 /**
  * Default class of a turtle interpretation. This class factorise the differents turtle interpretation. If you wish to create a new turtle
  * interpretation you should extends this class and implements its methods.
  * 
  * @author Caelum
  */
 public abstract class Turtle
 {
 	/** Type of the turtle is unknown */
 	public final static int TYPE_UNKNOWN = 0;
 	/** The turtle is a TubeTurlte */
 	public final static int TYPE_TUBE = 1;
 	/** the maximal number of objects we can display at once */
 	public static int MAX_OBJECTS = 50000;
 
 	/** The name of the turtle */
 	protected String name = "";
 	/** The type of the turtle */
 	protected int type = TYPE_UNKNOWN;
 	/** The list of symbols to interpret */
 	protected ListSymbols symbols;
 	/** the parameters of this turtle */
 	protected ArrayList<Parameter> parameters = new ArrayList<Parameter>();
 	/** The Drawer of the 3D scene */
 	protected Drawer drawer;
 	/** The node to add in the scene when display this turtle */
 	private final Node rootNode = new Node();
 	/** The minimum coordinates of the drawing */
 	protected Vector3f minCoord = new Vector3f(0, 0, 0);
 	/** The maximum coordinates of the drawing */
 	protected Vector3f maxCoord = new Vector3f(0, 0, 0);
 
 	/**
 	 * Default constructor.
 	 */
 	public Turtle ()
 	{
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param drawer The object drawer of the scene
 	 */
 	public Turtle (Drawer drawer)
 	{
 		this.drawer = drawer;
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param drawer The object drawer of the scene
 	 * @param symbols The symbols to represent
 	 */
 	public Turtle (Drawer drawer, ListSymbols symbols)
 	{
 		this(drawer);
 		this.symbols = symbols;
 	}
 
 	/**
 	 * Checks if the interpretation can interpret all the symbols.
 	 * 
 	 * @return true if all is ok, false if all symbols can't be interpreted
 	 */
 	public abstract boolean checkSymbols ();
 
 	/**
 	 * Draws the list of symbols depending of the turtle's interpretation.
 	 * @param listener the listener to tell the advencement
 	 * 
 	 * @throws BadSymbolException
 	 */
 	public void drawSymbols (final GeneratorPseudoListener listener) throws BadSymbolException
 	{
 		final Node root = drawer.getRootNode(), nodeTmp;
 		listener.begin();
 		nodeTmp = drawScene();
 
 		drawer.enqueue(new Callable<Void>()
 		{
 			@Override
 			public Void call () throws Exception
 			{
 				root.detachAllChildren();
 				rootNode.detachAllChildren();
 				rootNode.attachChild(nodeTmp);
 				root.attachChild(rootNode);
				if (root.getChildren().size() > 3)
				{
					root.center();
					root.setLocalRotation(Quaternion.DIRECTION_Z);
					root.center();
					root.setLocalRotation(Quaternion.DIRECTION_Z); // on le fait 2 fois pour plus de sûreté
				}
 				listener.finished();
 				return null;
 			}
 		});
 	}
 
 	/**
 	 * clear the scene: remove all the objects
 	 */
 	public void clearScene ()
 	{
 		final Node root = drawer.getRootNode();
 		symbols.clear();
 
 		drawer.enqueue(new Callable<Void>()
 		{
 			@Override
 			public Void call () throws Exception
 			{
 				root.detachAllChildren();
 				return null;
 			}
 		});
 	}
 
 	/**
 	 * Create the scene and put all the elements in the returned node
 	 * 
 	 * @return the node to attach to the Root Node to display all the elements.
 	 * @throws BadSymbolException
 	 */
 	protected abstract Node drawScene () throws BadSymbolException;
 
 	/**
 	 * Update the max and min coordinates of the drawing. This is used to position de camera at the center of the drawing.
 	 * 
 	 * @param position the last position drawn
 	 */
 	protected void updateBoundsCoordinates (Vector3f position)
 	{
 		if (position.x < minCoord.x)
 			minCoord.x = position.x;
 		if (position.y < minCoord.y)
 			minCoord.y = position.y;
 		if (position.z < minCoord.z)
 			minCoord.z = position.z;
 		if (position.x > maxCoord.x)
 			maxCoord.x = position.x;
 		if (position.y > maxCoord.y)
 			maxCoord.y = position.y;
 		if (position.z > maxCoord.z)
 			maxCoord.z = position.z;
 	}
 
 	/**
 	 * @param drawer the drawer to set
 	 */
 	public void setDrawer (Drawer drawer)
 	{
 		this.drawer = drawer;
 	}
 
 	/**
 	 * @param symbols the symbols to set
 	 */
 	public void setSymbols (ListSymbols symbols)
 	{
 		this.symbols = symbols;
 	}
 
 	/**
 	 * @return the symbols
 	 */
 	public ListSymbols getSymbols ()
 	{
 		return symbols;
 	}
 
 	/**
 	 * @return the name
 	 */
 	public String getName ()
 	{
 		return name;
 	}
 
 	/**
 	 * @return the type
 	 */
 	public int getType ()
 	{
 		return type;
 	}
 
 	/**
 	 * @return the parameters
 	 */
 	public ArrayList<Parameter> getParameters ()
 	{
 		return parameters;
 	}
 
 	/**
 	 * set the parameters
 	 * 
 	 * @param params
 	 */
 	public abstract void setParameters (ArrayList<Parameter> params);
 }
