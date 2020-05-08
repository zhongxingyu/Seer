 /*
 This file is part of leafdigital textlayout.
 
 textlayout is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 textlayout is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with textlayout. If not, see <http://www.gnu.org/licenses/>.
 
 Copyright 2011 Samuel Marshall.
 */
 package textlayout.stylesheet;
 
 import java.awt.*;
 import java.io.*;
 import java.util.*;
 
 import javax.swing.JLabel;
 
 import textlayout.*;
 import util.*;
 
 /**
  * Contains one or more stylesheets.
  */
 public class StyleContext
 {
 	/** Stylesheet processing order constants */
 	private static final int ORDER_USER=1000,ORDER_DEFAULT=0;
 
 	/** Default stylesheets */
 	private static StyleContext defaultWithUser,defaultWithoutUser;
 	
 	/** True if we've looked for a user stylesheet */
 	private static boolean userChecked;
 	
 	/** User stylesheet */
 	private static Stylesheet user;
 
 	/** This is used for default font size */
 	private final static Font defaultLabelFont=(new JLabel()).getFont();
 	
 
 	/**
 	 * Used to mark whether the stylesheet cannot be changed.
 	 */
 	private boolean fixed=false;
 
 	/** Tracks order numbers for stylesheets. Map of Stylesheet->Integer */ 
 	private Map<Stylesheet, Integer> stylesheetNumbers=new HashMap<Stylesheet, Integer>();
 
 	/**
 	 * Map storing all stylesheet property values. The format is as follows:
 	 * Property (desired property) -> Map:
 	 *   Last element name, or * -> TreeSet (sorted by specificity, best first):
 	 *   	 PropertyDeclarationInfo 
 	 * When querying the map for a particular element, * must always be queried
 	 * in addition. Queries from this map should result in a list of possibly-
 	 * relevant definitions which can then be filtered.
 	 */
 	private Map<Property, Map<String, TreeSet<PropertyDeclarationInfo>>> propertyValues =
 		new HashMap<Property, Map<String, TreeSet<PropertyDeclarationInfo>>>();
 	
 	/** 
 	 * Current file number (used in determining which is the 'last' property
 	 * when they have the same specificity). We start with 100 for added 
 	 * stylesheets; the user stylesheet is always 1000 and the default one is 0
 	 */
 	private int currentFile=100;
 
 	/** 
 	 * Map from colour keyword -> TreeSet of RGBDeclarationInfo, of which first 
 	 * element is the currently active one 
 	 */
 	private Map<String, TreeSet<RGBDeclarationInfo>> colours =
 		new HashMap<String, TreeSet<RGBDeclarationInfo>>();
 	
 	/** Cache property values to speed calculation */
 	private Map<String, Serializable> 	cacheFontSize,	cacheNumber,	cacheString,	cacheRGB,	cacheFont,cacheInsets;
 	{
 		wipeCache();
 	}
 
 	/**
 	 * Creates a clone of an existing style context that you can modify.
 	 * @param cloneSource Source of clone
 	 */
 	public StyleContext(StyleContext cloneSource) 
 	{
 		for(Map.Entry<Stylesheet, Integer> me : cloneSource.stylesheetNumbers.entrySet())
 		{
 			stylesheetNumbers.put(me.getKey(),me.getValue());
 		}
 		for(Map.Entry<Property, Map<String, TreeSet<PropertyDeclarationInfo>>> me :
 			cloneSource.propertyValues.entrySet())
 		{
 		  Map<String, TreeSet<PropertyDeclarationInfo>> oldMap = me.getValue(),
 		  	newMap = new HashMap<String, TreeSet<PropertyDeclarationInfo>>();
 			propertyValues.put(me.getKey(),newMap);
 			for(Map.Entry<String, TreeSet<PropertyDeclarationInfo>> me2 :
 				oldMap.entrySet())
 			{
 				newMap.put(me2.getKey(),
 					new TreeSet<PropertyDeclarationInfo>(me2.getValue()));
 			}			
 		}
 		currentFile=cloneSource.currentFile;
 		for(Map.Entry<String, TreeSet<RGBDeclarationInfo>> me :
 			cloneSource.colours.entrySet())
 		{
 			colours.put(me.getKey(), new TreeSet<RGBDeclarationInfo>(me.getValue()));
 		}
 	}
 	
 	/**
 	 * Marks styles as fixed (no changes).
 	 */
 	public void fix()
 	{
 		fixed=true;
 	}
 	
 	private void addUserStylesheet() throws LayoutException,IOException
 	{
 		if(!userChecked)
 		{
 			File fUser=new File(PlatformUtils.getUserFolder(),"userstyle.css");
 			if(fUser.exists()) 
 				user=new Stylesheet(new FileInputStream(fUser));
 			else
 				user=null;
 			userChecked=true;
 		}
 		if(user!=null)
 			addStylesheet(user,ORDER_USER);
 	}
 	
 	/**
 	 * Clears cached user styles (for use when userstyle.css has changed). Does
 	 * not actually do anything, just changes the result of getDefault; you need
 	 * to call {@link ScrollingLayout#updateStyle(StyleContext)} on things that
 	 * must change.
 	 */
 	public static void refreshUserStyle()
 	{
 		defaultWithUser=null;
 	}
 	
 	/**
 	 * Gets default context.
 	 * @param includeUserStyles Include user stylesheets
 	 * @return Context
 	 * @throws LayoutException
 	 * @throws IOException
 	 */
 	public static StyleContext getDefault(boolean includeUserStyles) throws LayoutException,IOException
 	{
 		if(includeUserStyles)
 		{
 			if(defaultWithUser==null)
 			{
 				defaultWithUser=new StyleContext(true);
 				defaultWithUser.fixed=true;
 			}
 			return defaultWithUser;
 		}
 		else
 		{
 			if(defaultWithoutUser==null)
 			{
 				defaultWithoutUser=new StyleContext(false);
 				defaultWithoutUser.fixed=true;
 			}
 			return defaultWithoutUser;
 		}		
 	}
 	
 	/**
 	 * Constructs a style context.
 	 * @param includeUserStyles Include user stylesheets
 	 * @throws LayoutException
 	 * @throws IOException
 	 */
 	public StyleContext(boolean includeUserStyles) throws LayoutException,IOException
 	{
 		// Add system colours
 		initSystemRGB("_window",SystemColor.window);
 		initSystemRGB("_windowText",SystemColor.windowText);
 		initSystemRGB("_controlText",SystemColor.controlText);
 		initSystemRGB("_control",SystemColor.control);
 		
 		// Add default stylesheet(s)
		addStylesheet(
			new Stylesheet(StyleContext.class.getResourceAsStream("DefaultStylesheet.css")),
				StyleContext.ORDER_DEFAULT);
 		if(includeUserStyles)
 			addUserStylesheet();
 	}
 	
 
 	/**
 	 * Adds a system colour. May only be called at start of constructor.
 	 * @param keyword Keyword for colour e.g. _window
 	 * @param c Colour
 	 */
 	private void initSystemRGB(String keyword,SystemColor c)
 	{
 		TreeSet<RGBDeclarationInfo> ts=new TreeSet<RGBDeclarationInfo>();
 		colours.put(keyword,ts);
 		ts.add(new RGBDeclarationInfo(new RGBDeclaration(keyword,c,null,null),-1));
 	}
 	
 	private static class PropertyDeclarationInfo implements Comparable<Object>
 	{
 		private PropertyDeclaration pd;
 		private int file;
 		
 		@Override
 		public int compareTo(Object o)
 		{
 			if(o==this) return 0;
 			PropertyDeclarationInfo other=(PropertyDeclarationInfo)o;
 			if(pd.getSpecificity() != other.pd.getSpecificity())
 			{
 				return other.pd.getSpecificity()-pd.getSpecificity();
 			}
 			if(file!=other.file)
 			{
 				return other.file-file;
 			}
 			if(pd.getFilePos()!=other.pd.getFilePos())
 			{
 				return other.pd.getFilePos()-pd.getFilePos();
 			}
 			return 1; // Arbitrary case. Used when there are two selectors x,y { prop:val },
 			  					// so the position of property is same.
 		}
 			
 
 		public PropertyDeclarationInfo(PropertyDeclaration pd,int file)
 		{
 			this.pd=pd;
 			this.file=file;
 		}
 	}
 	
 	private static class RGBDeclarationInfo implements Comparable<Object>	
 	{
 		RGBDeclaration rb;
 		int file;
 		
 		@Override
 		public int compareTo(Object o)
 		{
 			if(o==this) return 0;
 			RGBDeclarationInfo other=(RGBDeclarationInfo)o;
 			if(file!=other.file)
 			{
 				return other.file-file;
 			}
 			throw new Error("Can't have two declarations of same colour in same file?");
 		}
 
 		public RGBDeclarationInfo(RGBDeclaration rb,int file)
 		{
 			this.rb=rb;
 			this.file=file;
 		}
 	}
 	
 	/**
 	 * @param s Stylesheet to remove
 	 * @throws LayoutException Any error
 	 */
 	public synchronized void removeStylesheet(Stylesheet s) throws LayoutException
 	{
 		if(fixed) throw new LayoutException("Cannot alter default stylesheet");
 		
 		int fileNumber = stylesheetNumbers.get(s);
 
 		for(Map<String, TreeSet<PropertyDeclarationInfo>> props :
 			propertyValues.values())
 		{
 			for(TreeSet<PropertyDeclarationInfo> declarations : props.values())
 			{
 				for(Iterator<PropertyDeclarationInfo> i = declarations.iterator(); i.hasNext();)
 				{
 					PropertyDeclarationInfo pdi = i.next();
 					if(pdi.file==fileNumber)
 					{
 						i.remove();
 					}
 				}
 			}
 		}
 		
 		for(Iterator<TreeSet<RGBDeclarationInfo>> rgbs=colours.values().iterator();rgbs.hasNext();)
 		{
 			TreeSet<RGBDeclarationInfo> ts = rgbs.next();
 			for(Iterator<RGBDeclarationInfo> i=ts.iterator();i.hasNext();)
 			{
 				RGBDeclarationInfo rdi = i.next();
 				if(rdi.file==fileNumber)
 				{
 					i.remove();
 				}
 			}
 		}
 		
 		wipeCache();
 	}
 
 	/**
 	 * Adds a stylesheet to the context.
 	 * @param s New stylesheet
 	 * @throws LayoutException
 	 */
 	public synchronized void addStylesheet(Stylesheet s) throws LayoutException
 	{
 		addStylesheet(s,currentFile++);
 	}
 	
 	/**
 	 * Adds a stylesheet to the context.
 	 * @param s New stylesheet
 	 * @param thisFile Index of stylesheet
 	 * @throws LayoutException
 	 */
 	public synchronized void addStylesheet(Stylesheet s,int thisFile) throws LayoutException
 	{
 		if(fixed) throw new LayoutException("Cannot alter default stylesheet");
 		
 		// Give error if number is already in use, & record it
 		for(Integer i : stylesheetNumbers.values())
 		{
 			if(thisFile == i)
 			{
 				throw new LayoutException("Attempt to add stylesheet number "+thisFile+" twice");
 			}
 		}
 		stylesheetNumbers.put(s,new Integer(thisFile));
 		
 		// Add properties
 		PropertyDeclaration[] declarations = s.getPropertyDeclarations();
 		for(int i=0;i<declarations.length;i++)
 		{
 			PropertyDeclaration declaration=declarations[i];
 			
 			// Get map for this property
 			Property p=declaration.getProperty();
 			Map<String, TreeSet<PropertyDeclarationInfo>> propertyMap = propertyValues.get(p);
 			if(propertyMap==null)
 			{
 				propertyMap = new HashMap<String, TreeSet<PropertyDeclarationInfo>>();
 				propertyValues.put(p, propertyMap);
 			}
 			
 			// Get list for last element name
 			String last=declaration.getLastElement();
 			TreeSet<PropertyDeclarationInfo> ts = propertyMap.get(last);
 			if(ts==null)
 			{
 				ts=new TreeSet<PropertyDeclarationInfo>();
 				propertyMap.put(last,ts);
 			}
 			
 			// Add this declaration to list
 			ts.add(new PropertyDeclarationInfo(declaration,thisFile));
 		}
 		
 		// Add colours
 		RGBDeclaration[] colourInfo=s.getColours();
 		for(int i=0;i<colourInfo.length;i++)
 		{			
 			String keyword=colourInfo[i].getKeyword();
 			TreeSet<RGBDeclarationInfo> ts = colours.get(keyword);
 			if(ts==null)
 			{
 				ts=new TreeSet<RGBDeclarationInfo>();
 				colours.put(keyword,ts);
 			}
 			ts.add(new RGBDeclarationInfo(colourInfo[i],thisFile));
 		}
 		
 		wipeCache();
 	}
 	
 	/**
 	 * Gets colour.
 	 * @param keyword Colour keyword
 	 * @param opacity Opacity to use
 	 * @return Colour
 	 */
 	public synchronized Color getColour(String keyword,int opacity)
 	{
 		TreeSet<RGBDeclarationInfo> ts = colours.get(keyword);
 		if(ts==null || ts.isEmpty()) 
 			return getColour("fg",opacity); // Default if an invalid keyword is specified
 		RGBDeclaration rgbd = ts.first().rb;
 		if(rgbd.getDefaultKeyword()!=null)
 			return getColour(rgbd.getDefaultKeyword(),opacity);
 		
 		Color c=rgbd.getRGB();
 		if(opacity==255)
 			return c;
 		else
 			return GraphicsUtils.combineOpacity(c,opacity);
 	}
 	
 	/**
 	 * Remove the attribute part from a context string.
 	 * @param contextElement Context string
 	 * @return Tagname from string
 	 */
 	static String stripAttributes(String contextElement)
 	{
 		int one=contextElement.indexOf('\u0001');
 		if(one==-1) 
 			return contextElement;
 		else
 			return contextElement.substring(0,one);
 	}
 	
 	private synchronized PropertyData getPropertyValue(Property p,String[] context) throws LayoutException
 	{
 		// Automatic fallback to root
 		if(context.length==0) context=new String[] {"_root"};
 		String last=context[context.length-1];
 		
 		Map<String, TreeSet<PropertyDeclarationInfo>> propertyMap =
 			propertyValues.get(p);
 		if(propertyMap==null) return p.getDefaultValue();
 		
 		LinkedList<TreeSet<PropertyDeclarationInfo>> declarations =
 			new LinkedList<TreeSet<PropertyDeclarationInfo>>();
 		declarations.add(propertyMap.get(stripAttributes(last)));
 		declarations.add(propertyMap.get("*"));
 
 		int maxSpecificity=-1,maxFile=-1,maxFilePos=-1;
 		PropertyData pd=null;
 		for(TreeSet<PropertyDeclarationInfo> currentDeclarations : declarations)
 		{
 			if(currentDeclarations == null) continue; // E.g. no *
 			for(PropertyDeclarationInfo pdi : currentDeclarations)
 			{
 				// Don't bother trying to match it (or anything else in this set) 
 				// if its specificity is less than something we already found
 				int specificity=pdi.pd.getSpecificity();
 				if(specificity < maxSpecificity) break;
 				if(specificity==maxSpecificity)
 				{
 					if(pdi.file < maxFile) break;
 					if(pdi.file==maxFile)
 					{
 						if(pdi.pd.getFilePos()<maxFilePos) break;						
 					}
 				}
 				
 				if(pdi.pd.matches(context))
 				{
 					pd=pdi.pd.getValue();
 					maxSpecificity=specificity;
 					maxFile=pdi.file;
 					maxFilePos=pdi.pd.getFilePos();
 					// Everything following in this list is of lower specificity
 					break;
 				}
 			}
 		}
 		
 		if(pd==null)
 		{
 			pd=p.getDefaultValue();
 		}
 
 		return pd;		
 	}
 	
 	private void wipeCache()
 	{
 		cacheFontSize=new HashMap<String, Serializable>();
 		cacheNumber=new HashMap<String, Serializable>();
 		cacheString=new HashMap<String, Serializable>();
 		cacheRGB=new HashMap<String, Serializable>();
 		cacheFont=new HashMap<String, Serializable>();
 		cacheInsets=new HashMap<String, Serializable>();
 	}
 	
 	private static String getCacheKey(String[] context, Property p)
 	{
 		return StringUtils.join("\n",context)+(p==null?"":"\n\n"+p);
 	}
 
 	/**
 	 * Obtains the font size in pixels for a given context.
 	 * @param context Context
 	 * @return Value in pixels
 	 * @throws LayoutException Only if there's something screwy with the system's
 	 *   property definitions
 	 */
 	private synchronized int getFontSize(String[] context) throws LayoutException
 	{
 		String key=getCacheKey(context,null);
 		Integer i=(Integer)cacheFontSize	.get(key);
 		if(i!=null)
 			return i.intValue();
 		
 		int iReturn;
 		NumberPropertyData npd=(NumberPropertyData)getPropertyValue(Property.FONT_SIZE,context);
 		if(npd.isDefault())
 			return defaultLabelFont.getSize();
 		else if(npd.isAbsolute()) 
 			iReturn=npd.getAbsoluteValue();
 		else
 		  iReturn=npd.getRelativeValue(getFontSize(StringUtils.removeLast(context)));
 		cacheFontSize.put(key,new Integer(iReturn));
 		return iReturn;
 	}
 	
 	/**
 	 * Obtains the value in pixels of a number property.
 	 * @param p Property object (Property.xx)
 	 * @param context Context
 	 * @return Value in pixels
 	 * @throws LayoutException If property doesn't exist or isn't a number
 	 */
 	public synchronized int getNumber(Property p,String[] context) throws LayoutException
 	{
 		if(p==Property.FONT_SIZE)
 		{
 			return getFontSize(context);
 		}
 
 		String key=getCacheKey(context, p);
 		Integer i=(Integer)cacheNumber.get(key);
 		if(i!=null)
 			return i.intValue();
 
 		int iReturn;
 		try
 		{
 			PropertyData pd=getPropertyValue(p,context);		
 			if(pd.inherit())
 			{
 				iReturn=getNumber(p,StringUtils.removeLast(context));
 			}
 			else
 			{
 				NumberPropertyData npd=(NumberPropertyData)pd;			
 				if(npd.isAbsolute()) 
 					iReturn=npd.getAbsoluteValue();
 				else
 					iReturn=npd.getRelativeValue(getFontSize(context));
 			}
 		}
 		catch(ClassCastException cce)
 		{
 			throw new LayoutException("Property "+p+" is not a number property");
 		}
 		cacheNumber.put(key,new Integer(iReturn));
 		return iReturn;
 	}
 	
 	/**
 	 * Obtains the value of a string property.
 	 * @param p Property object (Property.xx)
 	 * @param context Context
 	 * @return Value
 	 * @throws LayoutException If property doesn't exist or isn't a string
 	 */
 	public synchronized String getString(Property p,String[] context) throws LayoutException
 	{
 		String key=getCacheKey(context, p);
 		String s=(String)cacheString.get(key);
 		if(s!=null)
 			return s;
 		
 		try
 		{
 			PropertyData pd=getPropertyValue(p,context);		
 			if(pd.inherit())
 			{
 				s=getString(p,StringUtils.removeLast(context));
 			}
 			else
 			{
 				StringPropertyData spd=(StringPropertyData)pd;
 				s=spd.getValue();
 			}
 		}
 		catch(ClassCastException cce)
 		{
 			throw new LayoutException("Property "+p+" is not a string property");
 		}
 		cacheString.put(key,s);
 		return s;
 	}
 	
 	/**
 	 * Obtains the value of an RGB property.
 	 * @param p Property object (Property.xx)
 	 * @param context Context
 	 * @return Value
 	 * @throws LayoutException If property doesn't exist or isn't a colour
 	 */
 	public synchronized Color getRGB(Property p,String[] context) throws LayoutException
 	{
 		String key=getCacheKey(context, p);
 		Color c=(Color)cacheRGB.get(key);
 		if(c!=null)
 			return c;
 		
 		try
 		{
 			PropertyData pd=getPropertyValue(p,context);
 			if(pd.inherit())
 			{
 				c=getRGB(p,StringUtils.removeLast(context));
 			}
 			else
 			{
 				RGBPropertyData cpd=(RGBPropertyData)pd;
 				c=cpd.getValue(this);
 			}
 		}
 		catch(ClassCastException cce)
 		{
 			throw new LayoutException("Property "+p+" is not an RGB property");
 		}
 		cacheRGB.put(key,c);
 		return c;
 	}
 	
 	/**
 	 * Obtains the value of insets.
 	 * @param p Array of properties (use Property.I_xx constant)
 	 * @param context Context
 	 * @return Value
 	 * @throws LayoutException If one of the properties doesn't exist or isn't a number
 	 */
 	public synchronized Insets getInsets(Property[] p,String[] context) throws LayoutException
 	{
 		String key=getCacheKey(context,p[0]);
 		Insets i=(Insets)cacheInsets.get(key);
 		if(i!=null)
 			return i;
 		
 		i=new Insets(getNumber(p[0],context),getNumber(p[1],context),getNumber(p[2],context),getNumber(p[3],context));
 		cacheInsets.put(key,i);
 		return i;
 	}
 	
 	/**
 	 * Gets the font for a particular tag stack. 
 	 * @param context XML context as tag stack
 	 * @return Current font
 	 * @throws LayoutException
 	 */
 	public synchronized Font getFont(String[] context) throws LayoutException
 	{
 		String key=getCacheKey(context,null);
 		Font f=(Font)cacheFont.get(key);
 		if(f!=null)
 			return f;
 		
 		String name=getString(Property.FONT_NAME,context);
 		if(name.equals(FontProperty.DEFAULT))
 		{
 			name=defaultLabelFont.getFamily();
 		}
 		
 		f=new Font(
 			name,
 			(getString(Property.FONT_STYLE,context).equals(Property.V_FONT_STYLE_ITALIC) ? Font.ITALIC : 0) |			
 			(getString(Property.FONT_WEIGHT,context).equals(Property.V_FONT_WEIGHT_BOLD) ? Font.BOLD : 0),
 			getFontSize(context));
 		cacheFont.put(key,f);
 		return f;
 	}
 	
 	/**
 	 * @param context Context
 	 * @return Whether this element should be considered as an inline item
 	 *   (inline or margin-note)
 	 * @throws LayoutException If there's an error getting property
 	 */
 	public boolean isInline(String[] context) throws LayoutException
 	{
 		String type=getString(Property.TYPE,context);
 		return type.equals(Property.V_TYPE_INLINE);
 	}
 	/**
 	 * @param context Context
 	 * @return Whether this element should be considered as a block item
 	 * @throws LayoutException If there's an error getting property
 	 */
 	public boolean isBlock(String[] context) throws LayoutException
 	{
 		String type=getString(Property.TYPE,context);
 		return type.equals(Property.V_TYPE_BLOCK) || type.equals(Property.V_TYPE_MARGIN_BLOCK);
 	}
 	/**
 	 * @param context Context
 	 * @return Whether this element is a margin-block
 	 * @throws LayoutException If there's an error getting property
 	 */
 	public boolean isMarginBlock(String[] context) throws LayoutException
 	{
 		String type=getString(Property.TYPE,context);
 		return type.equals(Property.V_TYPE_MARGIN_BLOCK);
 	}
 	/**
 	 * @param context Context
 	 * @return Whether this element has unknown type
 	 * @throws LayoutException If there's an error getting property
 	 */
 	public boolean isUnknown(String[] context) throws LayoutException
 	{
 		return getString(Property.TYPE,context).equals(Property.V_TYPE_UNKNOWN);
 	}
 }
