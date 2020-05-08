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
 import com.jme3.scene.shape.PQTorus;
 
 /**
  * @author Caelum
  */
 public class TreeTurtle extends Turtle {
 	/** Turtle interpretation: make the turtle draw a leaf */
 	public static final int S_LEAF = 21;
 
 	/** The angle for the rotations between the tubes */
 	private float angle = 25;
 
 	/** The length of the tubes */
 	private float length = 10f;
 
 	/** The reduction of the length of the branches in % */
	private float lengthReduction = 1; // pour te faire plaisir
 
 	/** The width of the tubes */
 	float width = 1.5f;
 	
 	/** The reduction of the width of the branches in % */
 	private float widthReduction = 13;
 
 	/** The color of the branch */
 	private ColorRGBA colorBranch = ColorRGBA.Brown;
 
 	/** The color of the leaf */
 	private ColorRGBA colorLeaf = ColorRGBA.Green;
 
 	/** The material of the drawn branchs */
 	private Material materialBranch;
 
 	/** The material of the drawn leafs */
 	private Material materialLeaf;
 
 	/** The list of symbols known by the interpretation */
 	private final static ListSymbols authorizedSymbols;
 
 	// Initialisation of the authorized symbols list
 	static {
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
 
 		sym = new Symbol();
 		sym.setInterpretation(Symbol.S_UNDETERMINATE);
 		authorizedSymbols.append(sym);
 		
 		sym = new Symbol();
 		sym.setInterpretation(S_LEAF);
 		sym.setName("LEAF");
 		authorizedSymbols.append(sym);
 	}
 
 	/**
 	 * Default constructor.
 	 */
 	public TreeTurtle() {
 		super();
 		initParameters();
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param drawer
 	 *            The object drawer of the scene
 	 */
 	public TreeTurtle(Drawer drawer) {
 		super(drawer);
 		initParameters();
 	}
 
 	@Override
 	protected void initParameters() {
 		name = "Tree Turtle";
 		type = TYPE_TREE;
 		parameters.add(new Parameter("Angle", ParameterType.TYPE_INTEGER,
 				new Integer((int) angle)));
 		parameters.add(new Parameter("Length", ParameterType.TYPE_DOUBLE,
 				new Double(length)));
 		parameters.add(new Parameter("Length reduction (in %)",
 				ParameterType.TYPE_DOUBLE, new Double(lengthReduction)));
 		parameters.add(new Parameter("Width", ParameterType.TYPE_DOUBLE,
 				new Double(width)));
 		parameters.add(new Parameter("Width reduction (in %)",
 				ParameterType.TYPE_DOUBLE, new Double(widthReduction)));
 		parameters.add(new Parameter("Branch color", ParameterType.TYPE_COLOR,
 				colorBranch));
 		parameters.add(new Parameter("Leaf color", ParameterType.TYPE_COLOR,
 				colorLeaf));
 	}
 
 	/**
 	 * Checks whether or not the given list is compatible with this turtle. If
 	 * there is an interpretation that is unknown, returns false. Undefined
 	 * interpretations are accepted
 	 * 
 	 * @param symbols
 	 *            the list to be checked
 	 * @return true if the interpretation is OK, false if this turtle can't
 	 *         represent the list
 	 */
 	public static boolean checkSymbols(ListSymbols symbols) {
 		boolean found;
 		final int sizeCheck = symbols.size(), sizeInterpretation = authorizedSymbols
 				.size();
 
 		for (int i = 0; i < sizeCheck; i++) {
 			found = false;
 			for (int j = 0; j < sizeInterpretation; j++) {
 				if ((symbols.get(i).getInterpretation() == Symbol.S_UNDETERMINATE || (symbols
 						.get(i).getInterpretation() == authorizedSymbols.get(j)
 						.getInterpretation())))
 					found = true;
 			}
 			if (!found)
 				return false;
 		}
 		return true;
 	}
 
 	@Override
 	public boolean checkSymbols() {
 		boolean found;
 		final int sizeCheck = symbols.size(), sizeInterpretation = authorizedSymbols
 				.size();
 
 		for (int i = 0; i < sizeCheck; i++) {
 			found = false;
 			for (int j = 0; j < sizeInterpretation; j++) {
 				if (symbols.get(i).getInterpretation() == authorizedSymbols
 						.get(j).getInterpretation())
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
 	 * @param drawer
 	 *            The object drawer of the scene
 	 * @param symbols
 	 *            The symbols to represent
 	 */
 	public TreeTurtle(Drawer drawer, ListSymbols symbols) {
 		super(drawer, symbols);
 		name = "Tube Turle";
 	}
 
 	@Override
 	protected Node drawScene() throws BadSymbolException {
 		int i = 0;
 		materialBranch = new Material(drawer.getAssetManager(),
 				"Common/MatDefs/Light/Lighting.j3md");
 		materialBranch.setBoolean("UseMaterialColors", true); // needed for
 																// shininess
 		materialBranch.setColor("Specular", colorBranch); // needed for
 															// shininess
 		materialBranch.setColor("Diffuse", colorBranch); // needed for shininess
 		materialBranch.setFloat("Shininess", 1); // shininess from 1-128
 		materialBranch.setColor("GlowColor", colorBranch);
 
 		materialLeaf = new Material(drawer.getAssetManager(),
 				"Common/MatDefs/Light/Lighting.j3md");
 		materialLeaf.setBoolean("UseMaterialColors", true); // needed for
 															// shininess
 		materialLeaf.setColor("Specular", colorLeaf); // needed for shininess
 		materialLeaf.setColor("Diffuse", colorLeaf); // needed for shininess
 		materialLeaf.setFloat("Shininess", 1); // shininess from 1-128
 		materialLeaf.setColor("GlowColor", colorLeaf);
 
 		minCoord.zero();
 		maxCoord.zero();
 
 		Node tmp;
 		Node node = new Node();
 		Node returnNode = node;
 		Stack<Node> saveNode = new Stack<Node>();
 		Stack<Float> saveWidth = new Stack<Float>();
 		Stack<Float> saveLength = new Stack<Float>();
 		float width = this.width;
 		float length = this.length;
 		returnNode.rotate(-90 * FastMath.DEG_TO_RAD, 0, 0);
 
 		for (Symbol symbol : symbols.getSymbols()) {
 			switch (symbol.getInterpretation()) {
 			case Symbol.S_FORWARD:
 				drawBranch(node, width, length);
 				width -= width * widthReduction / 100;
 				length -= length * lengthReduction / 100;
 				i++;
 				tmp = new Node();
 				tmp.setLocalTranslation(0, 0, length);
 				node.attachChild(tmp);
 				node = tmp;
 				updateBoundsCoordinates(node.localToWorld(
 						new Vector3f(0, 0, 0), null));
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
 			case Symbol.S_SAVEPOSITION: // easier to create 3 node than only one
 										// keeping the rotation matrix
 				saveNode.push(node);
 				tmp = new Node();
 				node.attachChild(tmp);
 				node = tmp;
 				saveWidth.push(width);
 				saveLength.push(length);
 				break;
 			case Symbol.S_RESTOREPOSITION: // easier to create 3 node than only
 											// one keeping the rotation matrix
 				if (!saveNode.isEmpty()) {
 					node = saveNode.pop();
 					tmp = new Node();
 					node.attachChild(tmp);
 					node = tmp;
 					width = saveWidth.pop();
 					length = saveLength.pop();
 				}
 				break;
 			case S_LEAF:
 				drawLeaf(node);
 				i++;
 				updateBoundsCoordinates(node.localToWorld(
 						new Vector3f(0, 0, 0), null));
 				break;
 			case Symbol.S_UNDETERMINATE: // UNDEFINED
 				break;
 			default:
 				throw (new BadSymbolException(
 						"The turle has uncounter a symbol impossible to draw: "
 								+ symbol.toString()));
 			}
 			if (i > MAX_OBJECTS) {
 				node.detachAllChildren();
 				symbols.clear();
 				throw new BadSymbolException(
 						"The maximum number of objects in the scene has been reached: "
 								+ MAX_OBJECTS
 								+ ".\n Please, remove some symbols or decrease the number of iterations.");
 			}
 
 		}
 
 		Vector3f middlePoint = new Vector3f((maxCoord.x - minCoord.x) / 2, 0,
 				(maxCoord.z - minCoord.z) / 2);
 		float diff = Math.max(maxCoord.x - minCoord.x, maxCoord.y - minCoord.y);
 		drawer.getCamera().setLocation(
 				new Vector3f(middlePoint.x, middlePoint.y, middlePoint.z - diff
 						* 2.5f));
 		drawer.getCamera().lookAt(middlePoint, new Vector3f(0, 1, 0));
 		return returnNode;
 	}
 
 	/**
 	 * Draws a branch
 	 * 
 	 * @param node
 	 *            The node to link the drawn branch to
 	 * @param width
 	 *            The width of the branch to be drawn
 	 * @param length
 	 *            The length of the branch to be drawn
 	 */
 	private void drawBranch(Node node, float width, float length) {
 		Cylinder tube = new Cylinder(10, 10, width, length, true);
 		Geometry geom = new Geometry("Branch", tube);
 		geom.setMaterial(materialBranch);
 		geom.setLocalTranslation(0, 0, length / 2);
 		node.attachChild(geom);
 	}
 
 	/**
 	 * Draws a leaf
 	 * 
 	 * @param node
 	 *            The node to link the drawn leaf to
 	 */
 	private void drawLeaf(Node node) {
 		PQTorus flower = new PQTorus(3,8, 2f, 1f, 32, 32); // Flower torus
 		Geometry geom = new Geometry("Leaf", flower);
 		geom.setMaterial(materialLeaf);
 		//geom.setLocalTranslation(0, 0, length / 2);
 		node.attachChild(geom);
 	}
 
 	@Override
 	public void setParameters(ArrayList<Parameter> params) {
 		parameters = params;
 
 		angle = ((Integer) parameters.get(0).getValue()).floatValue();
 		length = ((Double) parameters.get(1).getValue()).floatValue();
 		lengthReduction = ((Double) parameters.get(2).getValue()).floatValue();
 		width = ((Double) parameters.get(3).getValue()).floatValue();
 		widthReduction = ((Double) parameters.get(4).getValue()).floatValue();
 		colorBranch = (ColorRGBA) parameters.get(5).getValue();
 		colorLeaf = (ColorRGBA) parameters.get(6).getValue();
 	}
 
 	@Override
 	public ListSymbols getAuthorizedInterpretation() {
 		return authorizedSymbols;
 	}
 }
