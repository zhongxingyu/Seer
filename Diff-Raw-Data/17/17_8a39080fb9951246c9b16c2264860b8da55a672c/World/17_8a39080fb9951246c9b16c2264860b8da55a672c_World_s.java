 /* OpenMark online assessment system
    Copyright (C) 2007 The Open University
 
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package om.graph;
 
 import java.awt.*;
 import java.lang.reflect.*;
import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.w3c.dom.*;
 
 import util.xml.XML;
 import util.xml.XMLException;
 
 /**
  * Represents a co-ordinate system for use in a graph.
  */
 public class World
 {
 	/** ID for this world */
 	private String sID;
 
 	/** Pixel co-ordinates in target context */
 	private int iPixelX,iPixelY,iPixelW,iPixelH;
 
 	/** World co-ordinates */
 	private double dLeftX,dRightX,dTopY,dBottomY;
 
 	/** List of contained items */
 	private LinkedList<GraphItem> llItems=new LinkedList<GraphItem>();
 
 	/** Default font family from context */
 	private String sFontFamily;
 
 	/** Default font size from context */
 	private int iFontSize;
 
 	/** Pre-built default fonts */
 	private Font fSmall,fNormal;
 
 	/** Owner context */
 	private Context cContext;
 
 	/** Interface creator must implement to provide information */
 	public static interface Context
 	{
 		/**
 		 * Will be called to resolve colours that do not begin with a #.
 		 * @param sConstant Colour constant
 		 * @return Colour ID or null if it doesn't exist
 		 */
 		public Color getColour(String sConstant);
 
 		/**
 		 * If true, alternate colours - where both a colour and a constant are
 		 * given - will use the alternate constant rather than the colour.
 		 * @return True to use alternate colours
 		 */
 		public boolean useAlternates();
 
 		/**
 		 * Called to obtain default typeface.
 		 * @return Default font family name
 		 */
 		public String getFontFamily();
 
 		/**
 		 * Called to obtain default font size.
 		 * @return Default font size
 		 */
 		public int getFontSize();
 	}
 
 	/**
 	 * Constructs from XML.
 	 * <p>
 	 * XML syntax for worlds is:<br/>
 	 * &lt;world id="w1" px="20" py="10" pw="260" ph="280"
 	 * xleft="0.0" ybottom="-1.0" xright="10.0" ytop="1.0"/&gt;
 	 * <p>
 	 * May contain all other graph items, which will be constructed and added.
 	 * @param cs Source for colour constants
 	 * @param e &lt;world&gt; element
 	 * @throws GraphFormatException If anything's wrong with the XML
 	 */
 	public World(Context cs,Element e) throws GraphFormatException
 	{
 		// Check tag
 		if(!e.getTagName().equals("world")) throw new GraphFormatException(
 			"Expected <world> element, not <"+e.getTagName()+">");
 		this.cContext=cs;
 
 		// Set up fonts from context
 		sFontFamily=cs.getFontFamily();
 		iFontSize=cs.getFontSize();
 		fNormal=new Font(sFontFamily,Font.PLAIN,iFontSize);
 		fSmall=new Font(sFontFamily,Font.PLAIN,(iFontSize*3+2)/4);
 
 		// Set up attributes
 		try
 		{
 			sID=XML.getRequiredAttribute(e,"id");
 
 			iPixelX=Integer.parseInt(XML.getRequiredAttribute(e,"px"));
 			iPixelY=Integer.parseInt(XML.getRequiredAttribute(e,"py"));
 			iPixelW=Integer.parseInt(XML.getRequiredAttribute(e,"pw"));
 			iPixelH=Integer.parseInt(XML.getRequiredAttribute(e,"ph"));
 
 			dLeftX=Double.parseDouble(XML.getRequiredAttribute(e,"xleft"));
 			dRightX=Double.parseDouble(XML.getRequiredAttribute(e,"xright"));
 			dTopY=Double.parseDouble(XML.getRequiredAttribute(e,"ytop"));
 			dBottomY=Double.parseDouble(XML.getRequiredAttribute(e,"ybottom"));
 		}
 		catch(NumberFormatException nfe)
 		{
 			throw new GraphFormatException("<world>: Invalid number in attribute");
 		}
 		catch(XMLException xe)
 		{
 			throw new GraphFormatException("<world>: Format error - "+xe.getMessage());
 		}
 
 		// Construct child items
 		Element[] aeChildren=XML.getChildren(e);
 		for(int iChild=0;iChild<aeChildren.length;iChild++)
 		{
 			add(construct(aeChildren[iChild]));
 		}
 	}
 
 	/** @return ID of world */
 	public String getID()
 	{
 		return sID;
 	}
 
 	/**
 	 * Paints the world's contents.
 	 * @param g2 Target graphics context
 	 */
 	public void paint(Graphics2D g2)
 	{
		for(Iterator i=llItems.iterator();i.hasNext();)
 		{
			GraphItem gi=(GraphItem)i.next();
 			gi.paint(g2);
 		}
 	}
 
 	/**
 	 * Adds a new GraphItem to the end of the paint-order tree.
 	 * @param gi New item
 	 * @throws GraphFormatException If there's something wrong with it
 	 */
 	public void add(GraphItem gi) throws GraphFormatException
 	{
 		if(gi.getWorld()!=this)
 			throw new GraphFormatException("Graph item does not belong to this <world>");
 		gi.checkInit();
 		llItems.add(gi);
 	}
 
 	/**
 	 * Adds a new GraphItem to the specified position in paint order.
 	 * (0 = first/bottom.)
 	 * @param gi Item to add
 	 * @param iPos New index in paint order
 	 * @throws GraphFormatException
 	 */
 	public void add(GraphItem gi,int iPos) throws GraphFormatException
 	{
 		if(gi.getWorld()!=this)
 			throw new GraphFormatException("Graph item does not belong to this <world>");
 		gi.checkInit();
 		llItems.add(iPos,gi);
 	}
 
 	/**
 	 * Removes a GraphItem from the world.
 	 * @param gi Old item to remove
 	 */
 	public void remove(GraphItem gi)
 	{
 		llItems.remove(gi);
 	}
 
 	/**
 	 * Converts a graph co-ordinate to pixel co-ordinate.
 	 * @param gs X co-ordinate
 	 * @return Pixel X co-ordinate
 	 */
 	public int convertX(GraphScalar gs)
 	{
 		double dProportion=	(gs.getWorldPosition()-dLeftX)/(dRightX-dLeftX);
 		return (int)(dProportion*iPixelW+0.5) + gs.getPixelOffset() + iPixelX;
 	}
 
 	/**
 	 * Converts a graph co-ordinate to pixel co-ordinate.
 	 * @param dX X co-ordinate
 	 * @return Pixel X co-ordinate
 	 */
 	public int convertX(double dX)
 	{
 		double dProportion=	(dX-dLeftX)/(dRightX-dLeftX);
 		return (int)(dProportion*iPixelW+0.5) + iPixelX;
 	}
 
 	/**
 	 * Converts a graph co-ordinate to pixel co-ordinate.
 	 * @param dX X co-ordinate
 	 * @return Pixel X co-ordinate (as float)
 	 */
 	public float convertXFloat(double dX)
 	{
 		double dProportion=	(dX-dLeftX)/(dRightX-dLeftX);
 		return (float)((dProportion*iPixelW+0.5) + iPixelX);
 	}
 
 	/**
 	 * Converts a pixel co-ordinate to world co-ordinate.
 	 * @param iX Pixel X co-ordinate
 	 * @return World X co-ordinate
 	 */
 	public double convertXBack(int iX)
 	{
 		return dLeftX + ((double)(iX-iPixelX))/(double)iPixelW*(dRightX-dLeftX);
 	}
 
 	/**
 	 * Converts a graph co-ordinate to pixel co-ordinate.
 	 * @param gs Y co-ordinate
 	 * @return Pixel Y co-ordinate
 	 */
 	public int convertY(GraphScalar gs)
 	{
 		double dProportion=	(gs.getWorldPosition()-dTopY)/(dBottomY-dTopY);
 		return (int)(dProportion*iPixelH+0.5) + gs.getPixelOffset() + iPixelY;
 	}
 
 	/**
 	 * Converts a graph co-ordinate to pixel co-ordinate.
 	 * @param dY Y co-ordinate
 	 * @return Pixel Y co-ordinate
 	 */
 	public int convertY(double dY)
 	{
 		double dProportion=	(dY-dTopY)/(dBottomY-dTopY);
 		return (int)(dProportion*iPixelH+0.5) + iPixelY;
 	}
 
 	/**
 	 * Converts a graph co-ordinate to pixel co-ordinate.
 	 * @param dY Y co-ordinate
 	 * @return Pixel Y co-ordinate
 	 */
 	public float convertYFloat(double dY)
 	{
 		double dProportion=	(dY-dTopY)/(dBottomY-dTopY);
 		return (float)((dProportion*iPixelH) + iPixelY);
 	}
 
 	/**
 	 * Converts a pixel co-ordinate to world co-ordinate.
 	 * @param iY Pixel Y co-ordinate
 	 * @return World Y co-ordinate
 	 */
 	public double convertYBack(int iY)
 	{
 		return dTopY + ((double)(iY-iPixelY))/(double)iPixelH*(dBottomY-dTopY);
 	}
 
 	/**
 	 * @param itemId Graph item ID
 	 * @return Item with that ID
 	 * @throws IllegalArgumentException If there isn't anything with that ID
 	 */
 	public GraphItem getItem(String itemId) throws IllegalArgumentException
 	{
 		for(GraphItem gi : llItems)
 		{
 			if(itemId.equals(gi.getID())) return gi;
 		}
 		throw new IllegalArgumentException("<world>: Couldn't find item: "+itemId);
 	}
 
 	/**
 	 * Converts a colour string using the world's colour source.
 	 * <p>
 	 * You should use this method to obtain Color objects representing the
 	 * graph colour constants 'fg', 'graph1', etc.
 	 * @param sValue #rgb, #rrggbb, or colour constant
 	 * @return Java Color object
 	 * @throws GraphFormatException If a constant isn't found or value format wrong
 	 */
 	public Color convertColour(String sValue) throws GraphFormatException
 	{
 		if(sValue.startsWith("#"))
 		{
 			// See if there's a comma and alternate
 			int iComma=sValue.indexOf(',');
 			if(iComma!=-1)
 			{
 				// Split to main colour and alternate (which should be a constant,
 				// but we don't actually enforce that...)
 				String sAlternate=sValue.substring(iComma+1);
 				sValue=sValue.substring(0,iComma);
 
 				// Use alternate if requested
 				if(cContext.useAlternates())
 					return convertColour(sAlternate);
 			}
 			else
 			{
 				throw new GraphFormatException(
 					"<world>: Invalid colour specification "+sValue+". Please use a " +
 					"graph constant such as fg,bg, or graph1/graph2/graph3. If you need to " +
 					"specify a precise colour, follow it with a constant which will be used " +
 					"as backup for accessibility, e.g. '#080,fg'.");
 			}
 
 			// OK it's RGB. Get rid of # and process
 			String sRGB=sValue.substring(1);
 			try
 			{
 				// Convert to integers
 				int iR,iG,iB;
 				if(sRGB.length()==3)
 				{
 					iR=Integer.parseInt(sRGB.substring(0,1),16);
 					iR=16*iR+iR;
 					iG=Integer.parseInt(sRGB.substring(1,2),16);
 					iG=16*iG+iG;
 					iB=Integer.parseInt(sRGB.substring(2,3),16);
 					iB=16*iB+iB;
 				}
 				else if(sRGB.length()==6)
 				{
 					iR=Integer.parseInt(sRGB.substring(0,2),16);
 					iG=Integer.parseInt(sRGB.substring(2,4),16);
 					iB=Integer.parseInt(sRGB.substring(4,6),16);
 				}
 				else throw new GraphFormatException("<world>: Invalid RGB string: "+sValue);
 
 				return new Color(iR,iG,iB);
 			}
 			catch(NumberFormatException nfe)
 			{
 				throw new GraphFormatException("<world>: Invalid RGB string: "+sValue);
 			}
 		}
 		else
 		{
 			Color c=cContext.getColour(sValue);
 			if(c==null)
 				throw new GraphFormatException("<world>: Unknown colour constant: "+sValue);
 			return c;
 		}
 	}
 
 	/**
 	 * Constructs a GraphItem by reflection from the element.
 	 * @param e Element to construct
 	 * @return New GraphItem
 	 */
 	private GraphItem construct(Element e) throws GraphFormatException
 	{
 		String sTag=e.getTagName();
 
 		// Find class
 		Class<? extends GraphItem> c;
 		try
 		{
 			c = Class.forName("om.graph."+
 				sTag.substring(0,1).toUpperCase()+sTag.substring(1)+
 				"Item").asSubclass(GraphItem.class);
 		}
 		catch(ClassNotFoundException e1)
 		{
 			throw new GraphFormatException(
 				"Couldn't find matching item for tag name: "+sTag);
 		}
 
 		// Get constructor
 		Constructor<? extends GraphItem> cConstructor;
 		try
 		{
 			cConstructor=c.getConstructor(new Class[]{World.class});
 		}
 		catch(NoSuchMethodException e2)
 		{
 			throw new GraphFormatException(
 				"Couldn't find constructor taking World parameter for: "+sTag);
 		}
 
 		// Construct item
 		GraphItem giNew;
 		try
 		{
 			giNew=cConstructor.newInstance(new Object[]{this});
 		}
 		catch(Throwable t)
 		{
 			throw new GraphFormatException(
 				"Failed to construct graph item <"+sTag+">",t);
 		}
 
 		// Set each attribute
 		NamedNodeMap nnm=e.getAttributes();
 		Method[] am=c.getMethods();
 		attrloop: for(int iAttribute=0;iAttribute<nnm.getLength();iAttribute++)
 		{
 			Attr a=(Attr)nnm.item(iAttribute);
 			String
 				sName=a.getName(),sValue=a.getValue(),
 				sMethod="set"+sName;
 
 			// Find method
 			for(int iMethod=0;iMethod<am.length;iMethod++)
 			{
 				if(am[iMethod].getName().equalsIgnoreCase(sMethod))
 				{
 					// Check modifiers; ignore non-public, or static, methods
 					int iMod=am[iMethod].getModifiers();
 					if(!Modifier.isPublic(iMod) || Modifier.isStatic(iMod)) continue;
 
					// Ignore methods that don't take single param
					Class[] ac=am[iMethod].getParameterTypes();
 					if(ac.length!=1) continue;
					Class cParam=ac[0];
 
					// Ok this is the right method, now convert/validate the parameter
 					Object oParam;
 					try
 					{
 						if(cParam == int.class) oParam=new Integer(sValue);
 						else if(cParam == double.class) oParam=new Double(sValue);
 						else if(cParam == String.class) oParam=sValue;
 						else if(cParam == boolean.class)
 						{
 							if(sValue.equals("yes"))
 								oParam=Boolean.TRUE;
 							else if(sValue.equals("no"))
 								oParam=Boolean.FALSE;
 							else throw new GraphFormatException(
 								"<"+sTag+">: Unexpected boolean value for "+sName+": "+sValue);
 						}
 						else if(cParam == GraphScalar.class)
 							oParam=new GraphScalar(sValue);
 						else if(cParam == GraphRange.class)
 							oParam=new GraphRange(sValue);
 						else if(cParam == Color.class)
 							oParam=convertColour(sValue);
 						else if(cParam==Font.class)
 							oParam=convertFont(sValue);
 						else continue; // Ignore this method after all!
 					}
 					catch(NumberFormatException nfe)
 					{
 						throw new GraphFormatException(
 							"<"+sTag+">: Unexpected number value for "+sName+": "+sValue);
 					}
 
 					try
 					{
 						// Woo! All done. Now call method.
 						am[iMethod].invoke(giNew,new Object[]{oParam});
 					}
 					catch(Throwable t)
 					{
 						throw new GraphFormatException(
 							"<"+sTag+">: Error setting attribute "+sName+" to: "+sValue,t);
 					}
 
 					// OK, onto next attribute
 					continue attrloop;
 				}
 			}
 			throw new GraphFormatException(
 				"<"+sTag+">: Attribute "+sName+" does not exist.");
 		}
 
 		giNew.checkInit();
 		return giNew;
 	}
 
 	/** @return Left extent of world co-ordinates */
 	public double getLeftX()
 	{
 		return dLeftX;
 	}
 	/** @return Right extent of world co-ordinates */
 	public double getRightX()
 	{
 		return dRightX;
 	}
 	/** @return Top extent of world co-ordinates */
 	public double getTopY()
 	{
 		return dTopY;
 	}
 	/** @return Bottom extent of world co-ordinates */
 	public double getBottomY()
 	{
 		return dBottomY;
 	}
 
 	/**
 	 * @param bSmall True if we want the small version
 	 * @return Default font
 	 */
 	public Font getDefaultFont(boolean bSmall)
 	{
 		if(bSmall)
 			return fSmall;
 		else
 			return fNormal;
 	}
 
 	/**
 	 * @param bItalic
 	 * @param bBold
 	 * @param iSize
 	 * @return the default font, the specified size, boldness and italicity.
 	 */
 	public Font getDefaultFont(boolean bItalic,boolean bBold,int iSize)
 	{
 		return new Font(sFontFamily,
 			(bItalic ? Font.ITALIC :0) | (bBold ? Font.BOLD: 0),iSize);
 	}
 
 	/**
 	 * Obtains a Font object from the given string value in the format:
 	 * [bold/italic] [7px] [Verdana]. Leaving out any part of the
 	 * string results in the defaults being used.
 	 * @param sValue Input string
 	 * @return Font object
 	 */
 	private Font convertFont(String sValue) throws GraphFormatException
 	{
 		// Bold/italic
 		boolean bBold=false,bItalic=false;
 		while(true)
 		{
 			if(sValue.startsWith("bold"))
 			{
 				bBold=true;
 				sValue=sValue.substring("bold".length());
 				while(sValue.startsWith(" ")) sValue=sValue.substring(1);
 			}
 			else if(sValue.startsWith("italic"))
 			{
 				bItalic=true;
 				sValue=sValue.substring("italic".length());
 				while(sValue.startsWith(" ")) sValue=sValue.substring(1);
 			}
 			else break;
 		}
 
 		// Size
 		int iSize=iFontSize;
 		Matcher m=Pattern.compile("^([0-9]+)px(\\ )?(.*)$").matcher(sValue);
 		if(m.matches())
 		{
 			iSize=Integer.parseInt(m.group(1));
 			sValue=m.group(3);
 		}
 
 		// Family
 		String sFamily=sFontFamily;
 		if(sValue.length()>0)
 		{
 			sFamily=sValue;
 		}
 
 		return new Font(sFamily,
 			(bItalic ? Font.ITALIC :0) | (bBold ? Font.BOLD: 0),iSize);
 	}
 }
