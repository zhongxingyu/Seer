 //RYBColor.java
 
 
 //Copyright 2004-2005 MGX Team UMB.  All rights reserved.
 /* 
  * License Information
  * 
  * This  program  is  free  software;  you can redistribute it and/or modify it 
  * under  the  terms  of  the GNU General Public License  as  published  by the 
  * Free  Software  Foundation; either version 2  of  the  License,  or (at your 
  * option) any later version.
  */
 
 package molGenExp;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import preferences.GlobalDefaults;
 
 
 import biochem.AcidInChain;
 import biochem.AminoAcid;
 import biochem.ColorModel;
 import biochem.Direction;
 import biochem.Grid;
 import biochem.HexGrid;
 import biochem.PaintedInACornerFoldingException;
 
 /**
  * Class representing RYBColor chart. Model the standard RYB color model.
  * 
  * @author Namita Singla
  * 
  * -- modified by TJ, makes RYBColorChart singleton
  * modified by BW - totally different method
  */
 public class RYBColorModel extends ColorModel {
 
 	ArrayList<AcidInChain> hydrophobics;
 	ArrayList<AcidInChain> hydrophilics;
 	ArrayList<Color> coreColors;
 
 	private Color[] numberToColorMap = {
 			// colors are modeled by bits in integer
 			//    	1's = blue
 			//		2's = yellow
 			//		4's = red
 			Color.WHITE,			 // 000 0
 			Color.BLUE,			 // 001 1
 			Color.YELLOW,	     // 010 2 
 			Color.GREEN,			 // 011 3
 			Color.RED,			 // 100 4
 			new Color(138,43,226), // 101 5 Purple
 			new Color(255,140,0),	 // 110 6 Orange
 			Color.BLACK  
 	};
 
 	private String[] numberToColorNameMap = {
 			"White",
 			"Blue",
 			"Yellow",
 			"Green",
 			"Red",
 			"Purple",
 			"Orange",
 			"Black"
 	};
 
 	private HashMap<Color, Integer> colorToNumberMap;
 	private HashMap<String, Color> nameToColorMap;
 
 	/**
 	 * Constructor
 	 */
 	public RYBColorModel() { 
 		super();
 		
 		colorToNumberMap = new HashMap<Color, Integer>();
 		for (int i = 0; i < numberToColorMap.length; i++) {
 			colorToNumberMap.put((Color)numberToColorMap[i], new Integer(i));
 		}
 		
 		nameToColorMap = new HashMap<String, Color>();
 		for (int i = 0; i < numberToColorMap.length; i++) {
 			nameToColorMap.put(numberToColorNameMap[i], numberToColorMap[i]);
 		}
 	}
 
 	public Color getProteinColor(Grid grid) throws PaintedInACornerFoldingException {
 		Color color = Color.white;
 		hydrophobics = new ArrayList<AcidInChain>();
 		hydrophilics = new ArrayList<AcidInChain>();
 		coreColors = new ArrayList<Color>();
 
 		HexGrid realGrid = (HexGrid)grid;
 		int numAcids = grid.getPP().getLength();
 		Direction[] allDirections = grid.getAllDirections();
 		if (numAcids < 13)
 			return Color.white;
 		categorizeAcids(grid);
 		if (hydrophobics.size() < 7 || hydrophilics.size() < 6)
 			return Color.white;
 		Color c = Color.white;
 		for (int i = 0; i < hydrophobics.size(); i++) {
 			c = Color.white;
 			AcidInChain a = (AcidInChain) hydrophobics.get(i);
 			int d;
 			for (d = 0; d < allDirections.length; d++) {
 				AcidInChain ac = realGrid.get(
 						realGrid.nextCell(allDirections[d], a.xyz));
 
 				if (ac == null || !hydrophobics.contains(ac))
 					break;
 				else if (hydrophobics.contains(ac)) {
 					c = colorByAminoAcid(c, ac);
 				}
 			}
 			if (d == allDirections.length) {
 				c = colorByAminoAcid(c, a);
 				coreColors.add(c);
 			}
 		}
 		if (coreColors.size() > 0)
 			color = mixHexagonalCores();
 		return color;
 	}
 
 	public void categorizeAcids(Grid grid) {
 		int numAcids = grid.getPP().getLength();
 		AcidInChain[] acids = grid.getPP().getAcidArray();
 		for (int i = 0; i < numAcids; i++) {
 			AcidInChain a = acids[i];
 			if (a.getHydrophobicIndex() >= 0
 					|| a.getName().equalsIgnoreCase("tyr"))
 				hydrophobics.add(a);
 			else
 				hydrophilics.add(a);
 		}
 	}
 
 	private Color mixHexagonalCores() {
 		Color color = (Color) coreColors.get(0);
 		for (int i = 1; i < coreColors.size(); i++)
 			color = mixTwoColors(color, 
 					(Color) coreColors.get(i));
 		return color;
 	}
 
 
 	/**
 	 * Color by amino acids found in core
 	 * 	accumulate color as amino acids found
 	 */
 	private Color colorByAminoAcid(Color c, AcidInChain a) {
 		if (a.getName().equalsIgnoreCase("phe"))
 			c = mixTwoColors(c, Color.red);
 		if (a.getName().equalsIgnoreCase("tyr"))
 			c = mixTwoColors(c, Color.blue);
 		if (a.getName().equalsIgnoreCase("trp"))
 			c = mixTwoColors(c, Color.yellow);
 		return c;
 	}
 
 
 	/**
 	 * Mix two colors and return the result
 	 * @param a
 	 * @param b
 	 * @return
 	 */
 	public Color mixTwoColors(Color a, Color b) {
 		// null colors mean that it's a dead organism because
 		//  one or both proteins is folded in a corner
 		if ((a == null) || (b == null)) {
 			return GlobalDefaults.DEAD_COLOR;
 		}
 		int aNum = ((Integer)colorToNumberMap.get(a)).intValue();
 		int bNum = ((Integer)colorToNumberMap.get(b)).intValue();
 		return numberToColorMap[aNum | bNum];
 	}
 
 	public int getColorNumber(Color c) {
 		if (colorToNumberMap.get(c) == null) return -1;			// if not on the list, it's the
 																//  dead color
 		return ((Integer)colorToNumberMap.get(c)).intValue();
 	}
 
 	public Color colorAaNameText(AminoAcid a) {
 		if (a.getName().equals("Arg") ||
 				a.getName().equals("Lys") ||
 				a.getName().equals("His")) {
 			return Color.BLUE;
 		}
 		if (a.getName().equals("Asp") ||
 				a.getName().equals("Glu")) {
 			return Color.RED;
 		}
 		return Color.black;
 	}
 
 	public String getColorName(Color c) {
 		int colorNumber = getColorNumber(c);
 		if (colorNumber == -1) return null;
 		return numberToColorNameMap[getColorNumber(c)];
 	}
 	
 	public Color getColorFromString(String c) {
 		Color result = null;
 		if (nameToColorMap.containsKey(c)) result = nameToColorMap.get(c);
 		return result;
 	}
 }
