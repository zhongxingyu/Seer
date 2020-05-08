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
 package om.stdcomponent;
 
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.*;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 
 import om.*;
 import om.graph.GraphFormatException;
 import om.graph.World;
 import om.question.ActionParams;
 import om.stdquestion.*;
 
 import org.w3c.dom.Element;
 
 import util.misc.IO;
 import util.misc.Strings;
 import util.xml.XML;
 import util.xml.XMLException;
 
 /**
 Represents a Java BufferedImage so that you can draw graphics from Java code.
 Also includes graph support, but you're perfectly well able to use it for
 drawing your own graphics too.
 <h2>XML usage</h2>
 <pre>&lt;canvas alt="Fancy graph" width="200" height="200"&gt;
  &lt;world id='w1' px='10' py='10' pw='180' ph='180'
    xleft='-1.0' xright='1.0' ytop='1.0' ybottom='-1.0'&gt;
   &lt;xAxis ticks='0.2,0.1' numbers='0.5' omitNumbers='0.0' tickSide='both'/&gt;
   &lt;yAxis ticks='0.2,0.1' numbers='0.5' omitNumbers='0.0' tickSide='both'/&gt;
  &lt;/world&gt;
  &lt;marker x='10' y='10'/&gt;
  &lt;marker x='30' y='10' labelJS='label=x+","+y;' world='w1'/&gt;
  &lt;markerline from='0' to='1' labelJS='label=x1+','+y1;' world='w1'/&gt;
 &lt;/canvas&gt;
 <h2>Properties</h2>
 <table border="1">
 <tr><th>Property</th><th>Values</th><th>Effect</th></tr>
 <tr><td>id</td><td>(string)</td><td>Specifies unique ID</td></tr>
 <tr><td>display</td><td>(boolean)</td><td>Includes in/removes from
 output</td></tr>
 <tr><td>enabled</td><td>(boolean)</td><td>Activates/deactivates interactive
 features</td></tr>
 <tr><td>lang</td><td>(string)</td><td>Specifies the language of the content, like the HTML lang attribute. For example 'en' = English, 'el' - Greek, ...</td></tr>
 <tr><td>alt</td><td>(string)</td><td>Alternative text for those who can't read
 the bitmap</td></tr>
 <tr><td>width</td><td>(int)</td><td>Width in pixels</td></tr>
 <tr><td>height</td><td>(int)</td><td>Height in pixels</td></tr>
 <tr><td>type</td><td>png / jpg</td><td>Compression format used for output
 (JPG for photos, otherwise PNG)</td></tr>
 <tr><td>antialias</td><td>(boolean)</td><td>Whether to anti-alias text and
 graphics (default true)</td></tr>
 <tr><td>filePath</td><td>(string)</td><td>Optional path to image file
 (relative to class) to initialise canvas with</td></tr>
 <tr><td>requirebg</td><td>(boolean)</td><td>If true, includes background image
 even if question is being viewed in 'fixed colour' mode; otherwise does not.
 Setting requirebg='yes' also makes any graph use normal colours even if the
 question is in fixed-colour accessibility mode; be aware that this option
 is bad for accessibility.
 </td></tr>
 <tr><td>markerimage</td><td>(string)</td><td>If specified, use a custom marker image
 instead of the standard cross-hairs.
 If you specify markerimage="arrow", you must supply images arrow15.gif, arrow23.gif,
 arrow31.gif, arrow15d.gif, arrow23d.gif, arrow31d.gif. These must be square images
 with size 15, 23 and 31 pixels respectively. The 'd' images are for the disabled state.
 </td></tr>
 <tr><td>markerhotspot</td><td>(string)</td><td>Specify the position of the
 marker hotspot within the image, in the form x,y|x,y|x,y, for the three sizes.
 If you omit this, the hotspot is in the centre of the image, that is 8,8|11,11|15,15.
 </td></tr>
 </table>
 <h2>Contents</h2>
 <h3>Graphs</h3>
 <p>
 You can include any number of &lt;world&gt; elements. See the {@link om.graph}
 package for more information. These are painted in the order you include them;
 they are painted automatically the first time it's sent, but if you make other
 changes to the graphs you'll need to use the manual repaint() method.
 </p>
 <h3>Interactive features</h3>
 <p>
 You can have any number of &lt;marker&gt; elements. These are crosshairs that
 the user can drag around on the canvas. They can be dragged anywhere on the
 canvas (but not off it). The X/Y positions of the marker centre can be obtained
 in code. Marker positions are always in pixel co-ordinates, at the moment,
 and not world co-ordinates.
 </p>
 <p>
 If you've got at least 2 markers you can also have &lt;markerline&gt; elements.
 Specify a 'from' and 'to' marker and it'll draw a line between the two markers
 (the user moves the two markers as usual by dragging each one).
 </p>
 <p>
 You can add a label to the centre of the line by specifying
 'labelJS' which contains JavaScript statements that should set a 'label' variable based
 on the x1, y1, x2, and y2 variables (see XML usage example above). JavaScript is not
 exactly like Java so be careful if you don't know it. If you make JavaScript
 errors in the attribute, they will carry through as JS errors at runtime.
 </p>
 <p>
 x1,y1,x2, and y2 are in pixel co-ordinates unless you specify
 a 'world' attribute. Then they are in world co-ordinates.
 </p>
 <p>
 Labels on markers appear above and right of the marker. They are set in the
 same way, but for obvious reasons you have x,y instead of x1,y1,x2,y2.
 <p>
 (All mentions of pixel co-ordinates above are supposed to relate to 'original-size'
 pixels, i.e. if accessibility zoom is turned on, the same numbers should work
 as if it wasn't.)
 </p>
 */
 public class CanvasComponent extends QComponent implements World.Context
 {
 	private static final String PROPERTY_WIDTH="width";
 	private static final String PROPERTY_HEIGHT="height";
 	private static final String PROPERTY_ALT="alt";
 	private static final String PROPERTY_TYPE="type";
 	private static final String PROPERTY_ANTIALIAS="antialias";
 	private static final String PROPERTY_FILEPATH="filePath";
 	private static final String PROPERTY_REQUIREBG="requirebg";
 	private static final String PROPERTY_MARKERIMAGE="markerimage";
 	private static final String PROPERTY_MARKERHOTSPOT="markerhotspot";
 
 	/** @return Tag name (introspected; this may be replaced by a 1.5 annotation) */
 	public static String getTagName()
 	{
 		return "canvas";
 	}
 
 	/** Actual image data */
 	private BufferedImage bi=null;
 
 	/** Graphics context for it */
 	private Graphics2D g2=null;
 
 	/** Random number used to ensure we don't duplicate image filenames */
 	private String filename;
 
 	/** Changed flag, true if we need to send a new bitmap */
 	private boolean bChanged=true;
 
 	/** 'Clear' image, if there's a background in place (to save reloading each time) */
 	private BufferedImage biBackground=null;
 	
 	/** List of graph worlds, in paint order */
 	private List<World> lWorlds=new LinkedList<World>();
 
 	/** List of markers */
 	private List<Marker> lMarkers=new LinkedList<Marker>();
 
 	/** Information stored on each marker */
 	private static class Marker
 	{
 		/** X/Y co-ords */
 		int iX,iY;
 
 		/** Javascript expression for label text */
 		String sLabelJS;
 
 		/** ID of world for co-ordinates in label expression */
 		String sWorld;
 	}
 
 	/** List of lines */
 	private List<MarkerLine> lLines=new LinkedList<MarkerLine>();
 
 	/** Information stored for line between markers */
 	private static class MarkerLine
 	{
 		/** Indices of markers */
 		int iFrom,iTo;
 
 		/** Javascript expression for label text */
 		String sLabelJS;
 
 		/** ID of world for co-ordinates in label expression */
 		String sWorld;
 	}
 
 	@Override
 	protected String[] getRequiredAttributes()
 	{
 		return new String[]
 		{
 			PROPERTY_WIDTH,
 			PROPERTY_HEIGHT
 		};
 	}
 
 	@Override
 	protected void defineProperties() throws OmDeveloperException
 	{
 		super.defineProperties();
 
 		defineString(PROPERTY_ALT);
 		defineInteger(PROPERTY_WIDTH);
 		defineInteger(PROPERTY_HEIGHT);
 		defineString(PROPERTY_TYPE,"(png|jpg)");
 		defineBoolean(PROPERTY_ANTIALIAS);
 		defineString(PROPERTY_FILEPATH);
 		defineBoolean(PROPERTY_REQUIREBG);
 		defineString(PROPERTY_MARKERIMAGE);
 		defineString(PROPERTY_MARKERHOTSPOT);
 
 		setString(PROPERTY_ALT,"");
 		setString(PROPERTY_TYPE,"png");
 		setBoolean(PROPERTY_ANTIALIAS,true);
 		setBoolean(PROPERTY_REQUIREBG,false);
 		setString(PROPERTY_MARKERIMAGE,null);
 		setString(PROPERTY_MARKERHOTSPOT,"8,8|11,11|15,15");
 	}
 
 	@Override
 	protected void initChildren(Element eThis) throws OmException
 	{
 		clear();
 
 		Element[] aeChildren=XML.getChildren(eThis);
 		for(int iChild=0;iChild<aeChildren.length;iChild++)
 		{
 			Element e=aeChildren[iChild];
 			String sTag=e.getTagName();
 			if(sTag.equals("world"))
 			{
 				// Create the world
 				World w;
 				try
 				{
 					w=new World(this,e);
 				}
 				catch(GraphFormatException gfe)
 				{
 					throw new OmFormatException(gfe.getMessage(),gfe);
 				}
 				lWorlds.add(w);
 
 				// Paint it now
 				w.paint(getGraphics());
 			}
 			else if(sTag.equals("marker"))
 			{
 				Marker m=new Marker();
 				try
 				{
 					m.iX=Integer.parseInt(XML.getRequiredAttribute(e,"x"));
 					m.iY=Integer.parseInt(XML.getRequiredAttribute(e,"y"));
 
 					m.sLabelJS=e.hasAttribute("labelJS") ? e.getAttribute("labelJS") : "";
 					m.sWorld=e.hasAttribute("world") ? e.getAttribute("world") : null;
 				}
 				catch(XMLException xe)
 				{
 					throw new OmFormatException("<canvas>: Missing x/y attribute for marker");
 				}
 				catch(NumberFormatException nfe)
 				{
 					throw new OmFormatException("<canvas>: x/y marker attribute not valid integer");
 				}
 
 				lMarkers.add(m);
 			}
 			else if(sTag.equals("markerline"))
 			{
 				MarkerLine ml=new MarkerLine();
 				try
 				{
 					ml.iFrom=Integer.parseInt(XML.getRequiredAttribute(e,"from"));
 					ml.iTo=Integer.parseInt(XML.getRequiredAttribute(e,"to"));
 					if(ml.iFrom>=lMarkers.size() || ml.iTo>=lMarkers.size())
 						throw new OmFormatException(
 							"<canvas>: from/to for markerline is out of range (remember markers start at 0)");
 
 					ml.sLabelJS=e.hasAttribute("labelJS") ? e.getAttribute("labelJS") : "";
 					ml.sWorld=e.hasAttribute("world") ? e.getAttribute("world") : null;
 				}
 				catch(XMLException xe)
 				{
 					throw new OmFormatException("<canvas>: Missing x/y attribute for marker");
 				}
 				catch(NumberFormatException nfe)
 				{
 					throw new OmFormatException("<canvas>: x/y marker attribute not valid integer");
 				}
 
 				lLines.add(ml);
 			}
 
 			else throw new OmFormatException(
 				"<canvas>: Unexpected content (accepts <world>, <marker>): <"+sTag+">");
 		}
 	}
 
 	/**
 	 * Obtains a graphics context which you can use to draw on the image. Please
 	 * note:
 	 * <ul>
 	 * <li> Image is initially filled with background colour. This may change
 	 *   depending on which box the component is placed in, and the accessibility
 	 *   fixed-colour option.</li>
 	 * <li> After drawing on the image, you must call markChanged().</li>
 	 * <li> Draw using co-ordinates based on the requested pixel size: if zoom is
 	 *   necessary, this will have been automatically added to the context.</li>
 	 * <li> The context is shared, so if you set it to red it will stay red next
 	 *   time (i.e. make sure to set colour properly).</li>
 	 * <li> Remember to support the accessibility colour-fixing features. These
 	 *   require that you draw in appropriate colours. Use convertRGB() to
 	 *   access standard colour constants such as 'text' and getBackground() to
 	 *   determine the background colour in force at present.</li>
 	 * <li> Unless you have set requirebg='yes', the background image will not be
 	 *   loaded when the question is in fixed-colour mode.</li>
 	 * </ul>
 	 * @return Graphics context
 	 */
 	public Graphics2D getGraphics()
 	{
 		if(g2==null)
 		{
 			g2=getImage().createGraphics();
 			boolean bAntiAlias;
 			try
 			{
 				bAntiAlias=getBoolean("antialias");
 			}
 			catch(OmDeveloperException e)
 			{
 				throw new OmUnexpectedException(e);
 			}
 			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				bAntiAlias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
 			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
 				bAntiAlias ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
 
 			double dZoom=getQuestion().getZoom();
 			if(dZoom!=1.0) g2.scale(dZoom,dZoom);
 		}
 		return g2;
 	}
 
 	/**
 	 * Obtains the BufferedImage this canvas represents. You should avoid this
 	 * method and use getGraphics() where possible. If you use this method, you
 	 * are responsible for rescaling the image (if zoom is on). Also see the
 	 * warnings in getGraphics().
 	 * @return The BufferedImage
 	 */
 	public BufferedImage getImage()
 	{
 		try
 		{
 			if(bi==null)
 			{
 				int
 					iWidth=(int)(getInteger(PROPERTY_WIDTH)*getQuestion().getZoom()),
 					iHeight=(int)(getInteger(PROPERTY_HEIGHT)*getQuestion().getZoom());
 
 				// Create image
 				bi=new BufferedImage(iWidth,iHeight,BufferedImage.TYPE_INT_RGB);
 
 				// Fill to background
 				Graphics g=bi.getGraphics();
 				g.setColor(getBackground());
 				g.fillRect(0,0,iWidth,iHeight);
 			}
 			return bi;
 		}
 		catch(OmDeveloperException ode)
 		{
 			throw new OmUnexpectedException(ode);
 		}
 	}
 
 	/**
 	 * Clears the image to background colour or image. Also marks changed.
 	 * @throws OmDeveloperException If can't load the image
 	 */
 	public void clear() throws OmDeveloperException
 	{
 		// Load image if needed
 		if(isPropertySet(PROPERTY_FILEPATH) &&
 			(!getQuestion().isFixedColour() || getBoolean(PROPERTY_REQUIREBG)))
 		{
 			if(biBackground!=null)
 			{
 				getImage().setData(biBackground.getRaster());
 			}
 			else
 			{
 				// Fill to background
 				Graphics g=getImage().getGraphics();
 				g.setColor(getBackground());
 				g.fillRect(0,0,bi.getWidth(),bi.getHeight());
 
 				// Load file into it
 				try
 				{
 					byte[] abData=getQuestion().loadResource(getString(PROPERTY_FILEPATH));
 					BufferedImage loadedImage=ImageIO.read(new ByteArrayInputStream(abData));
 					getGraphics().drawImage(loadedImage,0,0,null);
 				}
 				catch(IOException ioe)
 				{
 					throw new OmDeveloperException("Failed to load image: "+getString(PROPERTY_FILEPATH),ioe);
 				}
 
 				// Cache
 				biBackground=new BufferedImage(getImage().getWidth(),getImage().getHeight(),
 					BufferedImage.TYPE_INT_RGB);
 				biBackground.setData(getImage().getRaster());
 			}
 		}
 		else
 		{
 			// Fill to background
 			Graphics g=getImage().getGraphics();
 			g.setColor(getBackground());
 			g.fillRect(0,0,bi.getWidth(),bi.getHeight());
 		}
 
 		bChanged=true;
 	}
 
 	/**
 	 * Clears the image and paints all the graph worlds. Also marks changed.
 	 * @throws OmDeveloperException If can't load the background image
 	 */
 	public void repaint() throws OmDeveloperException
 	{
 		clear();
 		for(World w : lWorlds)
 		{
 			w.paint(getGraphics());
 		}
 	}
 
 	/**
 	 * @param sID ID of desired <world>
 	 * @return Specified World object
 	 * @throws OmDeveloperException If there isn't a world with that ID
 	 */
 	public World getWorld(String sID) throws OmDeveloperException
 	{
 		for(World w : lWorlds)
 		{
 			if(w.getID().equals(sID)) return w;
 		}
 		throw new OmDeveloperException("<canvas>: World not found: "+sID);
 	}
 
 	/**
 	 * Obtains position of the given marker.
 	 * @param iMarker Marker index, beginning at zero
 	 * @return Marker position
 	 * @throws OmDeveloperException If you specified invalid index
 	 */
 	public Point getMarkerPos(int iMarker) throws OmDeveloperException
 	{
 		if(iMarker >= lMarkers.size() || iMarker<0)
 			throw new OmDeveloperException("<canvas>: No marker "+iMarker);
 		Marker m=lMarkers.get(iMarker);
 
 		return new Point(m.iX,m.iY);
 	}
 
 	/** @return Number of markers */
 	public int getNumMarkers()
 	{
 		return lMarkers.size();
 	}
 
 	/**
 	 * Adds a new marker at given co-ordinates.
 	 * @param iX X pixel co-ordinate
 	 * @param iY Y pixel co-ordinate
 	 * @param sLabelJS JavaScript label (may be null)
 	 * @param sWorld World ID for converting co-ordinates in JS expression (may be null)
 	 */
 	public void addMarker(int iX,int iY,String sLabelJS,String sWorld)
 	{
 		Marker m=new Marker();
 		m.iX=iX;
 		m.iY=iY;
 		m.sLabelJS=sLabelJS==null ? "" : sLabelJS;
 		m.sWorld=sWorld;
 		lMarkers.add(m);
 	}
 
 	/**
 	 * Removes marker of given index. Be careful - removing this means all other
 	 * markers above this change their index, so it might do weird things to lines
 	 * (e.g. a line moves).
 	 * @param iIndex Index of marker to remove (beginning at 0)
 	 */
 	public void removeMarker(int iIndex)
 	{
 		lMarkers.remove(iIndex);
 	}
 
 	/**
 	 * Adds a line.
 	 * @param iFromIndex Index (0-based) of one marker
 	 * @param iToIndex Index (0-based) of other marker
 	 * @param sLabelJS JavaScript label (may be null)
 	 * @param sWorld World ID for converting co-ordinates in JS expression (may be null)
 	 */
 	public void addLine(int iFromIndex,int iToIndex,String sLabelJS,String sWorld)
 	{
 		MarkerLine ml=new MarkerLine();
 		ml.iFrom=iFromIndex;
 		ml.iTo=iToIndex;
 		ml.sLabelJS=sLabelJS==null ? "" : sLabelJS;
 		ml.sWorld=sWorld;
 		lLines.add(ml);
 	}
 
 	/**
 	 * Removes the line between specified markers. Does nothing if there is
 	 * no such line.
 	 * @param iFromIndex Index (0-based) of one marker
 	 * @param iToIndex Index (0-based) of other marker
 	 * @return True if something was deleted, false if we didn't find one
 	 */
 	public boolean removeLine(int iFromIndex,int iToIndex)
 	{
 		for(Iterator<MarkerLine> i=lLines.iterator();i.hasNext();)
 		{
 			MarkerLine ml=i.next();
 			if(ml.iFrom==iFromIndex && ml.iTo==iToIndex)
 			{
 				i.remove();
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Marks the image as changed. After this is called, a new version of the
 	 * image will be sent next time. (If you don't call this, it won't. So if
 	 * your image doesn't change, that's why.) Calling this multiple times
 	 * doesn't hurt.
 	 */
 	public void markChanged()
 	{
 		bChanged=true;
 	}
 
 	@Override
 	protected void produceVisibleOutput(QContent qc,boolean bInit,boolean bPlain)
 		throws OmException
 	{
 		// Plain mode just shows text equivalent (at present)
 		if(bPlain)
 		{
 			// Put text equivalent
 			Element eDiv=qc.createElement("div"); // Can't use span because they aren't allowed to contain things
 			eDiv.setAttribute("style","display:inline");
 			addLangAttributes(eDiv);
 			qc.addInlineXHTML(eDiv);
 			XML.createText(eDiv,getString("alt"));
 			qc.addTextEquivalent(getString("alt"));
 
 			// Markers are not supported in plain mode
 			return;
 		}
 
 		// If image has changed, send new version
 		if(bChanged)
 		{
 			byte[] imageData;
 			String mimeType;
 			if (getString(PROPERTY_TYPE).equals("png")) {
 				imageData = QContent.convertPNG(bi);
 				mimeType = "image/png";
 			} else if (getString(PROPERTY_TYPE).equals("jpg")) {
 				imageData = QContent.convertJPG(bi);
 				mimeType = "image/jpeg";
 			} else {
 				throw new OmUnexpectedException("Unknown canvas type. Only png and jpg are valid.");
 			}
 			MessageDigest md;
 			try {
 				md = MessageDigest.getInstance("SHA-1");
 			} catch (NoSuchAlgorithmException e) {
 				throw new OmUnexpectedException(e);
 			}
 			filename = "canvas-" + getID() + "-" +
 					Strings.byteArrayToHexString(md.digest(imageData)) + "." + getString(PROPERTY_TYPE);
 			qc.addResource(filename, mimeType, imageData);
 		}
 
 		Element eEnsureSpaces=qc.createElement("div");
 		eEnsureSpaces.setAttribute("class","canvas");
 		addLangAttributes(eEnsureSpaces);
 		qc.addInlineXHTML(eEnsureSpaces);
 		XML.createText(eEnsureSpaces," ");
 
 		String sImageID=QDocument.ID_PREFIX+getID()+"_img";
 		Element eImg=XML.createChild(eEnsureSpaces,"img");
 		eImg.setAttribute("id",sImageID);
 		eImg.setAttribute("onmousedown","return false;"); // Prevent Firefox drag/drop
 		eImg.setAttribute("src","%%RESOURCES%%/"+filename);
 		eImg.setAttribute("alt",getString("alt"));
 
 		// Get zoom, marker size and hotspot position.
 		double dZoom=getQuestion().getZoom();
 		int iMarkerSize;
 		if (dZoom>=2.0)
 		{
 			iMarkerSize=31;
 		}
 		else if (dZoom>=1.5)
 		{
 			iMarkerSize=23;
 		}
 		else
 		{
 			iMarkerSize=15;
 		}
 
 		int[] hotspotPositions=parseHotspotProperty(getString(PROPERTY_MARKERHOTSPOT),iMarkerSize);
 
 		String sMarkerPrefix=getString(PROPERTY_MARKERIMAGE);
 		if(bInit && !lMarkers.isEmpty())
 		{
 			if (sMarkerPrefix==null)
 			{
 				setString(PROPERTY_MARKERIMAGE, "canvasm");
 				sMarkerPrefix="canvasm"+iMarkerSize;
 				try
 				{
 					qc.addResource(sMarkerPrefix+".gif","image/gif",
 						IO.loadResource(CanvasComponent.class,sMarkerPrefix+".gif"));
 					qc.addResource(sMarkerPrefix+"d.gif","image/gif",
 						IO.loadResource(CanvasComponent.class,sMarkerPrefix+"d.gif"));
 				}
 				catch(IOException e)
 				{
 					throw new OmUnexpectedException(e);
 				}
 			}
 			else
 			{
 				sMarkerPrefix+=iMarkerSize;
 				try
 				{
 					qc.addResource(sMarkerPrefix+".gif","image/gif",
 							getQuestion().loadResource(sMarkerPrefix+".gif"));
 					qc.addResource(sMarkerPrefix+"d.gif","image/gif",
 							getQuestion().loadResource(sMarkerPrefix+"d.gif"));
 				}
 				catch(IOException e)
 				{
 					throw new OmDeveloperException("Marker image not found: "+sMarkerPrefix, e);
 				}
 			}
 		}
 		else
 		{
 			sMarkerPrefix+=iMarkerSize;
 		}
 		if(!lMarkers.isEmpty())
 		{
 			Element eScript=XML.createChild(eEnsureSpaces,"script");
 			eScript.setAttribute("type","text/javascript");
 			XML.createText(eScript,
 				"addOnLoad(function() { canvasInit('"+getID()+"','"+QDocument.ID_PREFIX+"',"+
 				isEnabled()+","+hotspotPositions[0]+","+hotspotPositions[1]+","+
 				((int)(dZoom * 4.0)) + ",'"+
 				(getQuestion().isFixedColour() ? getQuestion().getFixedColourFG() : "black")+
 				"','"+
 				(getQuestion().isFixedColour() ? getQuestion().getFixedColourBG() :
 					convertHash(getBackground()))+
 				"'," + (int)(dZoom * 10.0)+
 				"); });");
 			Element eDynamic=XML.createChild(eEnsureSpaces,"div");
 			eDynamic.setAttribute("id",QDocument.ID_PREFIX+getID()+"_dynamic");
 		}
 
 		int index=0;
 		for(Marker m : lMarkers)
 		{
 			Element eMarker=XML.createChild(eEnsureSpaces,"img");
 			eMarker.setAttribute("id",QDocument.ID_PREFIX+getID()+"_marker"+index);
 			eMarker.setAttribute("src","%%RESOURCES%%/"+sMarkerPrefix+
 				(isEnabled() ? "" : "d") + ".gif");
 			eMarker.setAttribute("class","canvasmarker");
 			if(isEnabled())	eMarker.setAttribute("tabindex","0");
 			Element eScript=XML.createChild(eEnsureSpaces,"script");
 			eScript.setAttribute("type","text/javascript");
 			World w=m.sWorld==null ? null : getWorld(m.sWorld);
 			XML.createText(eScript,
 				"addOnLoad(function() { canvasMarkerInit('"+getID()+"','"+QDocument.ID_PREFIX+"','"+
 				m.sLabelJS.replaceAll("'","\\\\'")+"',"+
 				getWorldFactors(w,dZoom)+"); });");
 			Element eInputX=XML.createChild(eEnsureSpaces,"input");
 			eInputX.setAttribute("type","hidden");
 			eInputX.setAttribute("value",""+(int)(m.iX*dZoom));
 			eInputX.setAttribute("name",QDocument.ID_PREFIX+"canvasmarker_"+getID()+"_"+index+"x");
 			eInputX.setAttribute("id",eInputX.getAttribute("name"));
 			Element eInputY=XML.createChild(eEnsureSpaces,"input");
 			eInputY.setAttribute("type","hidden");
 			eInputY.setAttribute("value",""+(int)(m.iY*dZoom));
 			eInputY.setAttribute("name",QDocument.ID_PREFIX+"canvasmarker_"+getID()+"_"+index+"y");
 			eInputY.setAttribute("id",eInputY.getAttribute("name"));
 
 			if(isEnabled()) qc.informFocusable(QDocument.ID_PREFIX+getID()+"_marker"+index,bPlain);
 
 			index++;
 		}
 		for(MarkerLine ml : lLines)
 		{
 			World w=ml.sWorld==null ? null : getWorld(ml.sWorld);
 
 			Element eScript=XML.createChild(eEnsureSpaces,"script");
 			eScript.setAttribute("type","text/javascript");
 			XML.createText(eScript,
 				"addOnLoad(function() { canvasLineInit('"+getID()+"','"+QDocument.ID_PREFIX+"',"+
 				ml.iFrom+","+ml.iTo+",'"+ml.sLabelJS.replaceAll("'","\\\\'")+"'," +
 				getWorldFactors(w,dZoom)+
 				"); });");
 		}
 
 		XML.createText(eEnsureSpaces," ");
 		qc.addTextEquivalent(getString("alt"));
 	}
 
 	private int[] parseHotspotProperty(String property, int markerSize) throws OmDeveloperException
 	{
 		OmDeveloperException hotspotException = new OmDeveloperException(
 				"The " + PROPERTY_MARKERHOTSPOT +
 				" property must be of the form \"8,8|11,11|15,15\". You specified \"" + property + "\".");
 		String[] hotspotPositions = property.split("\\|");
 		if (hotspotPositions.length != 3) throw hotspotException;
 		// Converts 15, 23, 31 to 0, 1, 2 respectively.
 		String[] coords = hotspotPositions[markerSize/10 - 1].split(",");
 		if (coords.length != 2) throw hotspotException;
 		int[] result = new int[2];
 		try {
 			result[0] = Integer.parseInt(coords[0]);
 			result[1] = Integer.parseInt(coords[1]);
 		} catch (Exception e) {
 			throw hotspotException;
 		}
 		return result;
 	}
 	private String getWorldFactors(World w,double dZoom)
 	{
 		return (w==null ? "0,0,"+dZoom+","+dZoom :
 			(w.convertX(0.0)*dZoom)+","+(w.convertY(0.0)*dZoom)+","+
 			((w.convertX(1.0)-w.convertX(0.0))*dZoom)+","+((w.convertY(1.0)-w.convertY(0.0))*dZoom));
 	}
 
 	@Override
 	protected void formAllValuesSet(ActionParams ap) throws OmException
 	{
 		if(!isEnabled()) return;
 
 		// Get marker data
 		double dZoom=getQuestion().getZoom();
 		int i=0;
 		for(Marker m : lMarkers)
 		{
 			if(ap.hasParameter("canvasmarker_"+getID()+"_"+i+"x"))
 			{
 				try
 				{
 					m.iX=(int)(Integer.parseInt(
 						ap.getParameter("canvasmarker_"+getID()+"_"+i+"x")) / dZoom);
 					m.iY=(int)(Integer.parseInt(
 						ap.getParameter("canvasmarker_"+getID()+"_"+i+"y")) / dZoom);
 				}
 				catch(NumberFormatException nfe)
 				{
 					throw new OmException("Unexpected canvas marker value");
 				}
 			}
			i++;
 		}
 	}
 
 	/** Map from colour constant (String) to #colour (String) */
   private static final Map<String, String> mColourConstants=new HashMap<String, String>();
   static
   {
   	String[] COLOURDEFAULTS=
   	{
   		"fg","#000000",
   		"graph1","#ef6820",
   		"graph2","#00afad",
   		"graph3","#780032",
   		"whitebg","#ffffff",
   	};
   	for(int i=0;i<COLOURDEFAULTS.length;i+=2)
   	{
   		mColourConstants.put(COLOURDEFAULTS[i],COLOURDEFAULTS[i+1]);
   	}
   }
 
 
 	// Implementation for World.Context
 	public Color getColour(String sConstant)
 	{
 		try
 		{
 			if(getQuestion().isFixedColour() && !getBoolean(PROPERTY_REQUIREBG))
 			{
 				if(sConstant.equals("bg") || sConstant.equals("whitebg"))
 					return convertRGBOnly(getQuestion().getFixedColourBG());
 				else
 					return convertRGBOnly(getQuestion().getFixedColourFG());
 			}
 			else
 			{
 				// Special-case for background
 				if(sConstant.equals("bg"))
 					return getBackground();
 				else
 				{
 					String s=mColourConstants.get(sConstant);
 					if(s==null) return null;
 					return convertRGBOnly(s);
 				}
 			}
 		}
 		catch(OmDeveloperException e)
 		{
 			return null;
 		}
 	}
 
 	public boolean useAlternates()
 	{
 		return getQuestion().isFixedColour();
 	}
 
 	public String getFontFamily()
 	{
 		return "Verdana";
 	}
 
 	public int getFontSize()
 	{
 		// Note that this doesn't change on zoom because the whole graph is scaled
 		return 13;
 	}
 
 	@Override
 	public String setString(String sName,String sValue) throws OmDeveloperException
 	{
 		if(sName.equals(PROPERTY_FILEPATH)) biBackground=null;
 		return super.setString(sName,sValue);
 	}
 
 }
