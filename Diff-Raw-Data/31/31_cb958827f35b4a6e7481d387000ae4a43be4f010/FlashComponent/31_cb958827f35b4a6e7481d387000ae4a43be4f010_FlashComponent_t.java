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
 
 import java.io.IOException;
 import java.util.*;
 
 import om.*;
 import om.stdquestion.*;
 
 import org.w3c.dom.*;
 
 import util.misc.IO;
 import util.xml.XML;
 import util.xml.XMLException;
 
 /** 
 Represents a flash applet
 <p/>
 <h2>XML usage</h2>
 &lt;flash id="orangePic" filePath="media/orange.swf" alt="orange animation" width="100" height="80"/&gt;
 <h2>Properties</h2>
 <table border="1">
 <tr><th>Property</th><th>Values</th><th>Effect</th></tr>
 <tr><td>id</td><td>(string)</td><td>Specifies unique ID</td></tr>
 <tr><td>display</td><td>(boolean)</td><td>Includes in/removes from output</td></tr>
 <tr><td>enabled</td><td>(boolean)</td><td>Activates/deactivates children</td></tr>
 <tr><td>alt</td><td>(string)</td><td>Alternative text for those who can't use the actual image</td></tr>
 <tr><td>width</td><td>(int)</td><td>default displayed width in pixels (optional)</td></tr>
 <tr><td>height</td><td>(int)</td><td>default displayedheight in pixels (optional)</td></tr>
 <tr><td>filePath</td><td>(string)</td><td>Path to file (relative to class)</td></tr>
 </table>
 <h3>Changing which applet is shown</h3>
 The file and the alt-text can be changed from Java via a call to
 <br/>
 setApplet(String filePath, String altText)
 */
 public class FlashComponent extends QComponent
 {
 	private static final String FLASH_MIMETYPE = "application/x-shockwave-flash";
 	private static final String EXPRESSINSTALL_SWF = "expressinstall.swf";
 	private static final String PROPERTY_FILEPATH="filePath";
 	private static final String PROPERTY_WIDTH="width";
 	private static final String PROPERTY_HEIGHT="height";
 
 	/** @return Tag name (introspected; this may be replaced by a 1.5 annotation) */
 	public static String getTagName()
 	{
 		return "flash";
 	}
 	
 	/** Property for the alt text. */
 	public final static String PROPERTY_ALT="alt";
 	
 	/** path to image file */
 	private String sFilePath=null; // Currently-loaded file
 	private int iWidth = 300;
 	private int iHeight = 300;
 	private byte[] movieData;
 	private String sMimeType;
 	
 	/** 
 	 * Keep track of resources we added to users so we can save SOAP time by
 	 * not transferring them again.
 	 */ 
 	private Set<String> sAddedResources=new HashSet<String>();
 	
 	/** True if there was whitespace before or after the &lt;flash&gt; tag */
 	private boolean bSpaceBefore,bSpaceAfter;
 	
 	/** Specifies attributes required */
 	@Override
 	protected String[] getRequiredAttributes()
 	{
 		return new String[] {PROPERTY_ALT,PROPERTY_FILEPATH, PROPERTY_WIDTH,PROPERTY_HEIGHT};
 	}
 	
 	/** Specifies possible attributes */
 	@Override
 	protected void defineProperties() throws OmDeveloperException
 	{
 		super.defineProperties();
 		defineString(PROPERTY_ALT);
 		defineString(PROPERTY_FILEPATH);
 		defineInteger(PROPERTY_WIDTH);
 		defineInteger(PROPERTY_HEIGHT);
 	}
 	
 	/** parses internals of tag to create java component*/
 	@Override
 	protected void initChildren(Element eThis) throws OmException
 	{
 		Node nPrevious=eThis.getPreviousSibling();
 		if(nPrevious!=null && nPrevious instanceof Text)
 		{
 			String sText=((Text)nPrevious).getData();
 			if(sText.length()>0 && Character.isWhitespace(sText.charAt(sText.length()-1)))
 				bSpaceBefore=true;
 		}
 		Node nAfter=eThis.getNextSibling();
 		if(nAfter!=null && nAfter instanceof Text)
 		{
 			String sText=((Text)nAfter).getData();
 			if(sText.length()>0 && Character.isWhitespace(sText.charAt(0)))
 				bSpaceAfter=true;
 		}
 	}
 	
 	/** @return FilePath of Image 
 	 * @throws OmDeveloperException */
 	public String getFilePath() throws OmDeveloperException
 	{ 
 		return getString(PROPERTY_FILEPATH); 
 	}
 	
 	/** 
 	 * Sets the Image file path and alt text.
 	 * <p>
 	 * @param sFilePath New value for filePath
 	 * @param sAlt New value for screenreader alternative 
 	 * @throws OmDeveloperException -- when?
 	 */
	public void setApplet(String sFilePath, String sAlt) throws OmDeveloperException
 	{ 
		setString(PROPERTY_FILEPATH, sFilePath);
		setString(PROPERTY_ALT, sAlt);
 	}
 
 	@Override
 	public void produceVisibleOutput(QContent qc,boolean bInit,boolean bPlain) throws OmException
 	{
 		double dZoom=getQuestion().getZoom();
 		if(bPlain)
 		{
 			// Put text equivalent
 			Element eDiv=qc.createElement("div");
 			qc.addInlineXHTML(eDiv);
 			XML.createText(eDiv,getString(PROPERTY_ALT));
 			qc.addTextEquivalent(getString(PROPERTY_ALT));
 		}
 		else
 		{
 			iWidth = getInteger("width");
 			iHeight = getInteger("height");
 
 			/** loads image data from file if not yet loaded */
 			if (sFilePath == null || !sFilePath.equals(getString(PROPERTY_FILEPATH)))
 			{
 				sFilePath=getString(PROPERTY_FILEPATH);
 				
 				//get image mime type
 				String sFL = sFilePath.toLowerCase();
 				if (sFL.endsWith(".swf")) sMimeType = FLASH_MIMETYPE;
 				else throw new OmException("Invalid flash file type: "+sFilePath);
 	
 				try 
 				{
 					movieData=getQuestion().loadResource(sFilePath);
 				} 
 				catch (IOException e) 
 				{
 					throw new OmDeveloperException("Flash file not found: "+sFilePath,e);
 				}
 				if(!sAddedResources.contains(sFilePath))
 				{
 					sAddedResources.add(sFilePath);
 					qc.addResource(sFilePath,sMimeType,movieData);
 				}
 			}
 
 			if (bInit) {
 				byte[] installMovieData;
 				try 
 				{
 					installMovieData=IO.loadResource(FlashComponent.class,EXPRESSINSTALL_SWF);
 				} 
 				catch (IOException e) 
 				{
 					throw new OmUnexpectedException("Flash file not found: "+EXPRESSINSTALL_SWF);
 				}
 				if(!sAddedResources.contains(EXPRESSINSTALL_SWF))
 				{
 					sAddedResources.add(EXPRESSINSTALL_SWF);
 					qc.addResource(EXPRESSINSTALL_SWF,FLASH_MIMETYPE,installMovieData);
 				}
 			}
 
 			// Outer element
 			Element eEnsureSpaces=qc.createElement("div");
 			eEnsureSpaces.setAttribute("class","flash");
 			qc.addInlineXHTML(eEnsureSpaces);
 			
 			int 
 				iActualWidth=(int)(iWidth*dZoom+0.5),
 				iActualHeight=(int)(iHeight*dZoom+0.5);
 			
 			// If there's a space before, add one here too (otherwise IE eats it)
 			if(bSpaceBefore)
 				XML.createText(eEnsureSpaces," ");
 			
 			// Put in placeholder that UFO will eat.
 			String movieId = QDocument.ID_PREFIX+getID()+"_movie";
 			Element placeholderSpan=XML.createChild(eEnsureSpaces,"span");
 			try {
 				XML.importChildren(placeholderSpan, XML.parse(
 						"<span>[To do this question, you will need to " +
 						"<a href='http://www.adobe.com/go/getflashplayer' " +
 						"title='Get Flash Player'>install the latest Flash plug-in" +
 						"<img src='http://www.adobe.com/images/shared/download_buttons/get_flash_player.gif' " +
 						"alt='Get Flash Player' border='0' /></a>]</span>").getDocumentElement());
 			} catch (XMLException e) {
 				throw new OmUnexpectedException(e);
 			}
 			placeholderSpan.setAttribute("id", movieId);
 
 			// Create JavaScrip
 			String js = 
 					"var FO = { movie:'%%RESOURCES%%/" + sFilePath + "', width:'" + iActualWidth +
 						"', height:'" + iActualHeight + "', majorversion:\"9\", build:\"0\"," +
 						"ximovie:\"%%RESOURCES%%/"+EXPRESSINSTALL_SWF+"\", xi:\"true\" };\n" + 
 					"UFO.create(FO, '" + movieId + "');";
 			Element scriptTag=XML.createChild(eEnsureSpaces,"script");
 			XML.createText(scriptTag, js);
 			scriptTag.setAttribute("type", "text/javascript");
 			
 			if(bSpaceAfter)
 				XML.createText(eEnsureSpaces," ");
 		}
 	}
 
 } // end of Image Component class
