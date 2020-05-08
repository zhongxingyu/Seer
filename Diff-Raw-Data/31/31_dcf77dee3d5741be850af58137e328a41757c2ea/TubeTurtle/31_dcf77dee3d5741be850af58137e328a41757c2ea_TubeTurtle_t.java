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
  * * Project: L-Systems File: TubeTurle.java Created on: 08 march. 2013 Author: Cepheus
  * *****************************************************
  */
 
 package ATuin;
 
 import java.util.ArrayList;
 import java.util.Stack;
 
 import parser.BadSymbolException;
 import parser.ListSymbols;
 import parser.Symbol;
 
 import ATuin.Parameter.ParameterType;
 
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.FastMath;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.shape.Cylinder;
 import com.jme3.scene.shape.Line;
 
 
 /**
  * @author Caelum
  */
 public class TubeTurtle extends Turtle
 {
 
 	/** The angle for the rotations between the tubes */
 	private float angle = 90;
 
 	/** The length of the tubes */
 	private float length = 10;
 
 	/** The width of the tubes */
 	private float width = 0.5f;
 
 	/** The color of the tubes */
 	private ColorRGBA color = ColorRGBA.Green;
 
 	/** The material of the drawn objects */
 	private Material material;
 
 	/** The list of symbols known by the interpretation */
 	public final static ListSymbols authorizedSymbols;
 
 	// Initialisation of the authorized symbols list
 	static
 	{
 		Symbol sym;
 		authorizedSymbols = new ListSymbols();
 
 		sym = new Symbol();
 		sym.setInterpretation(Symbol.S_FORWARD);
 		authorizedSymbols.append(sym);
 
 		sym = new Symbol();
 		sym.setInterpretation(Symbol.S_TURNLEFT);
 		authorizedSymbols.append(sym);
 
 		sym = new Symbol();
 		sym.setInterpretation(Symbol.S_TURNRIGHT);
 		authorizedSymbols.append(sym);
 
 		sym = new Symbol();
 		sym.setInterpretation(Symbol.S_TURNUP);
 		authorizedSymbols.append(sym);
 
 		sym = new Symbol();
 		sym.setInterpretation(Symbol.S_TURNDOWN);
 		authorizedSymbols.append(sym);
 
 		sym = new Symbol();
 		sym.setInterpretation(Symbol.S_ROLLLEFT);
 		authorizedSymbols.append(sym);
 
 		sym = new Symbol();
 		sym.setInterpretation(Symbol.S_ROLLRIGHT);
 		authorizedSymbols.append(sym);
 
 		sym = new Symbol();
 		sym.setInterpretation(Symbol.S_ABOUTTURN);
 		authorizedSymbols.append(sym);
 
 		sym = new Symbol();
 		sym.setInterpretation(Symbol.S_SAVEPOSITION);
 		authorizedSymbols.append(sym);
 
 		sym = new Symbol();
 		sym.setInterpretation(Symbol.S_RESTOREPOSITION);
 		authorizedSymbols.append(sym);
 	}
 
 	/**
 	 * Default constructor.
 	 */
 	public TubeTurtle ()
 	{
 		super();
 		name = "Tube Turtle";
 		type = TYPE_TUBE;

 		parameters.add(new Parameter("Angle", ParameterType.TYPE_INTEGER, new Integer((int) angle)));
 		parameters.add(new Parameter("Length", ParameterType.TYPE_DOUBLE, new Double(length)));
 		parameters.add(new Parameter("Width", ParameterType.TYPE_DOUBLE, new Double(width)));
 		parameters.add(new Parameter("Color", ParameterType.TYPE_COLOR, color));
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param drawer The object drawer of the scene
 	 */
 	public TubeTurtle (Drawer drawer)
 	{
 		super(drawer);
 		name = "Tube Turtle";
 		type = TYPE_TUBE;

 		parameters.add(new Parameter("Angle", ParameterType.TYPE_INTEGER, new Integer((int) angle)));
 		parameters.add(new Parameter("Length", ParameterType.TYPE_DOUBLE, new Double(length)));
 		parameters.add(new Parameter("Width", ParameterType.TYPE_DOUBLE, new Double(width)));
 		parameters.add(new Parameter("Color", ParameterType.TYPE_COLOR, color));
 	}
 
 	/**
 	 * Checks whether or not the given list is compatible with this turtle. If there is an interpretation that is unknown, returns false.
 	 * Undefined interpretations are accepted
 	 * 
 	 * @param symbols the list to be checked
 	 * @return true if the interpretation is OK, false if this turtle can't represent the list
 	 */
 	public static boolean checkSymbols (ListSymbols symbols)
 	{
 		boolean found;
 		final int sizeCheck = symbols.size(), sizeInterpretation = authorizedSymbols.size();
 
 		for (int i = 0; i < sizeCheck; i++)
 		{
 			found = false;
 			for (int j = 0; j < sizeInterpretation; j++)
 			{
 				if ((symbols.get(i).getInterpretation() == 0 || (symbols.get(i).getInterpretation() == authorizedSymbols.get(j)
 						.getInterpretation())))
 					found = true;
 			}
 			if (!found)
 				return false;
 		}
 		return true;
 	}
 
 	@Override
 	public boolean checkSymbols ()
 	{
 		boolean found;
 		final int sizeCheck = symbols.size(), sizeInterpretation = authorizedSymbols.size();
 
 		for (int i = 0; i < sizeCheck; i++)
 		{
 			found = false;
 			for (int j = 0; j < sizeInterpretation; j++)
 			{
 				if (symbols.get(i).getInterpretation() == authorizedSymbols.get(j).getInterpretation())
 					found = true;
 			}
 			if (!found)
 				return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param drawer The object drawer of the scene
 	 * @param symbols The symbols to represent
 	 */
 	public TubeTurtle (Drawer drawer, ListSymbols symbols)
 	{
 		super(drawer, symbols);
 		name = "Tube Turle";
 	}
 
 	@Override
 	protected Node drawScene () throws BadSymbolException
 	{
 		int i = 0;
 		material = new Material(drawer.getAssetManager(), "Common/MatDefs/Light/Lighting.j3md");
 		material.setBoolean("UseMaterialColors", true); // needed for shininess
 		material.setColor("Specular", color); // needed for shininess
 		material.setColor("Diffuse", color); // needed for shininess
 		material.setFloat("Shininess", 1); // shininess from 1-128
 		material.setColor("GlowColor", color);
 
 		minCoord.zero();
 		maxCoord.zero();
 
 		Node tmp;
 		Node node = new Node();
 		Node returnNode = node;
 		Stack<Node> saveNode = new Stack<Node>();
 		returnNode.rotate(-90 * FastMath.DEG_TO_RAD, 0, 0);

 		for (Symbol symbol : symbols.getSymbols())
 		{
 			switch (symbol.getInterpretation())
 			{
 				case Symbol.S_FORWARD:
 					drawTube(node);
 					i++;
 					tmp = new Node();
 					tmp.setLocalTranslation(0, 0, length);
 					node.attachChild(tmp);
 					node = tmp;
 					updateBoundsCoordinates(node.localToWorld(new Vector3f(0, 0, 0), null));
 					break;
 				case Symbol.S_TURNLEFT:
 					node.rotate(0, angle * FastMath.DEG_TO_RAD, 0);
 					break;
 				case Symbol.S_TURNRIGHT:
 					node.rotate(0, -angle * FastMath.DEG_TO_RAD, 0);
 					break;
 				case Symbol.S_TURNUP:
 					node.rotate(angle * FastMath.DEG_TO_RAD, 0, 0);
 					break;
 				case Symbol.S_TURNDOWN:
 					node.rotate(-angle * FastMath.DEG_TO_RAD, 0, 0);
 					break;
 				case Symbol.S_ROLLLEFT:
 					node.rotate(0, 0, angle * FastMath.DEG_TO_RAD);
 					break;
 				case Symbol.S_ROLLRIGHT:
 					node.rotate(0, 0, -angle * FastMath.DEG_TO_RAD);
 					break;
 				case Symbol.S_ABOUTTURN:
 					node.rotate(0, 180 * FastMath.DEG_TO_RAD, 0);
 					break;
 				case Symbol.S_SAVEPOSITION: // easier to create 3 node than only one keeping the rotation matrix
 					saveNode.push(node);
 					tmp = new Node();
 					node.attachChild(tmp);
 					node = tmp;
 					break;
 				case Symbol.S_RESTOREPOSITION: // easier to create 3 node than only one keeping the rotation matrix
 					if (!saveNode.isEmpty())
 					{
 						node = saveNode.pop();
 						tmp = new Node();
 						node.attachChild(tmp);
 						node = tmp;
 					}
 					break;
 				case 0: // UNDEFINED
 					break;
 				default:
 					throw (new BadSymbolException("The turle has uncounter a symbol impossible to draw: " + symbol.toString()));
 			}
 			if (i > MAX_OBJECTS)
			{
				node.detachAllChildren();
				symbols.clear();
 				throw new BadSymbolException("The maximum number of objects in the scene has been reached: " + MAX_OBJECTS
 						+ ".\n Please, remove some symbols or decrease the number of iterations.");
			}

 		}
 
 		Vector3f middlePoint = new Vector3f((maxCoord.x - minCoord.x) / 2, 0, (maxCoord.z - minCoord.z) / 2);
 		float diff = Math.max(maxCoord.x - minCoord.x, maxCoord.y - minCoord.y);
 		drawer.getCamera().setLocation(new Vector3f(middlePoint.x, middlePoint.y, middlePoint.z - diff * 2.5f));
 		drawer.getCamera().lookAt(middlePoint, new Vector3f(0, 1, 0));
 		return returnNode;
 	}
 
 	/**
 	 * Draws a tube
 	 * 
 	 * @param node The node to link the drawn tube to
 	 */
 	private void drawTube (Node node)
 	{
 		Cylinder tube = new Cylinder(10, 10, width, length, true);
 		Geometry geom = new Geometry("Tube", tube);
 		geom.setMaterial(material);
 		geom.setLocalTranslation(0, 0, length / 2);
 		node.attachChild(geom);
 	}
 
 	/**
 	 * Draws a line
 	 * 
 	 * @param node The node to link the drawn line to
 	 * @deprecated use drawTube with a length of 0.
 	 */
 	@SuppressWarnings ("unused")
 	private void drawLine (Node node)
 	{
 		Vector3f start = new Vector3f(0, 0, 0);
 		Vector3f end = new Vector3f(0, 0, -length);
 		Line line = new Line(start, end);
 		line.setLineWidth(width);
 		Geometry geom = new Geometry("Line", line);
 		geom.setMaterial(material);
 		node.setLocalTranslation(end);
 		node.attachChild(geom);
 	}
 
 	@Override
 	public void setParameters (ArrayList<Parameter> params)
 	{
 		parameters = params;

 		angle = ((Integer) parameters.get(0).getValue()).floatValue();
 		length = ((Double) parameters.get(1).getValue()).floatValue();
 		width = ((Double) parameters.get(2).getValue()).floatValue();
 		color = (ColorRGBA) parameters.get(3).getValue();
 	}
 }
