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
 
 import java.util.*;
 
 import om.*;
 import om.question.ActionParams;
 import om.stdquestion.*;
 
 import org.w3c.dom.Element;
 
 import util.xml.XML;
 
 /**
 A component onto which DragBoxComponents may be dropped.
 <h2>XML usage</h2>
 &lt;dropbox/&gt;
 <h2>Properties</h2>
 <table border="1">
 <tr><th>Property</th><th>Values</th><th>Effect</th></tr>
 <tr><td>id</td><td>(string)</td><td>Specifies unique ID</td></tr>
 <tr><td>display</td><td>(boolean)</td><td>Includes in/removes from output</td></tr>
 <tr><td>enabled</td><td>(boolean)</td><td>Activates/deactivates this control</td></tr>
 <tr><td>lang</td><td>(string)</td><td>Specifies the language of the content, like the HTML lang attribute. For example 'en' = English, 'el' - Greek, ...</td></tr>
 <tr><td>group</td><td>(string)</td><td>Optional group ID (should match that on dragboxes; will change colour)</td></tr>
 <tr><td>value</td><td>(string)</td><td>Value (= ID of the dragbox that was dragged into this box)</td></tr>
 <tr><td>forceborder</td><td>(boolean)</td><td>If true, displays faint grey border (use e.g. when over blank white image or in equation)</td></tr>
 <tr><td>sidelabel</td><td>(string)</td><td>Optional label that appears in small text beside the box</td></tr>
 </table>
 <h2>Sizing</h2>
 <p>Drop boxes are automatically made the same size as the largest dragbox
 that can be dropped in them (i.e. has the same group).
 There is no way to change their size.</p>
 */
 public class DropBoxComponent extends QComponent
 {
 	private static final String NOANSWEROPTION="(Select answer)";
 	/** Property name for value of box */
 	public final static String PROPERTY_VALUE="value";
 	/** Property name for group attribute */
 	public final static String PROPERTY_GROUP="group";
 	/** Property name for group attribute */
 	public final static String PROPERTYREGEXP_GROUP="[a-zA-Z0-9_]*";
 	/** Property name to force border on */
 	public final static String PROPERTY_FORCEBORDER="forceborder";
 	/** Property name for label alongside box */
 	public final static String PROPERTY_SIDELABEL="sidelabel";
 
 	/** @return Tag name (introspected; this may be replaced by a 1.5 annotation) */
 	public static String getTagName()
 	{
 		return "dropbox";
 	}
 
 	@Override
 	protected void defineProperties() throws OmDeveloperException
 	{
 		super.defineProperties();
 		defineString(PROPERTY_GROUP,PROPERTYREGEXP_GROUP);
 		defineString(PROPERTY_VALUE);
 		defineBoolean(PROPERTY_FORCEBORDER);
 		defineString(PROPERTY_SIDELABEL);
 		setString(PROPERTY_GROUP,"");
 		setString(PROPERTY_VALUE,"");
 		setBoolean(PROPERTY_FORCEBORDER,false);
 	}
 
 	@Override
 	protected void initChildren(Element eThis) throws OmException
 	{
 		if(eThis.getFirstChild()!=null)
 			throw new OmFormatException("<dropbox> may not include children");
 	}
 
 	private StringBuffer sbCrap=new StringBuffer();
 
 	/** @return Map of strings to component ID for the answers in plain mode (built on request) */
 	private Map<String, String> getPlainAnswers() throws OmException
 	{
 		// Build map from plain answers -> ID
 		Map<String, String> m=new HashMap<String, String>();
 		m.put(NOANSWEROPTION,"");
 
 		// Find all applicable dragboxes
 		List<DragBoxComponent> dragBoxes = getQDocument().find(DragBoxComponent.class);
 		for(DragBoxComponent dragBox : dragBoxes)
 		{
 			if(!dragBox.getString(PROPERTY_GROUP).equals(getString(PROPERTY_GROUP)))
 				continue;
 
 			String sOption=dragBox.getPlainDropboxContent(false);
 			m.put(sOption,dragBox.getID());
 
 			sbCrap.append(sOption +"="+dragBox.getID()+"\n");
 		}
 
 		return m;
 	}
 
 	@Override
 	public void produceVisibleOutput(QContent qc,boolean bInit,boolean bPlain) throws OmException
 	{
 		// Plain version is a standard dropdown list of text alternates
 		if(bPlain)
 		{
 			// Create select
 			Element eSelect=qc.createElement("select");
 			qc.addInlineXHTML(eSelect);
 			eSelect.setAttribute("name",QDocument.ID_PREFIX+QDocument.VALUE_PREFIX+getID());
 
 			Element eFirst=XML.createChild(eSelect,"option");
 			XML.createText(eFirst,NOANSWEROPTION);
 
 			// Find all applicable dragboxes
 			boolean bSomethingSelected=false;
 			List<DragBoxComponent> dragBoxes = getQDocument().find(DragBoxComponent.class);
 			for(DragBoxComponent dragBox : dragBoxes)
 			{
 				if(!dragBox.getString(PROPERTY_GROUP).equals(getString(PROPERTY_GROUP)))
 					continue;
 
 				String sOption=dragBox.getPlainDropboxContent(bInit);
 				boolean bSelected=getValue()==dragBox.getID();
 				bSomethingSelected|=bSelected;
 
 				Element eOption=XML.createChild(eSelect,"option");
 				if(bSelected) eOption.setAttribute("selected","selected");
 				XML.createText(eOption,sOption);
 			}
 			if(!bSomethingSelected) eFirst.setAttribute("selected","selected");
 		}
 		else
 		{
 			Element eImg=qc.createElement("img");
 			eImg.setAttribute("class","dropboximg");
 			eImg.setAttribute("src","%%RESOURCES%%/clear.gif");
 			eImg.setAttribute("id",QDocument.ID_PREFIX+getID()+"img");
 			qc.addInlineXHTML(eImg);
 			// Needed for IE (no it's not! now it screws it in IE! argh)
 			//qc.addInlineXHTML(qc.getOutputDocument().createTextNode(" "));
 
 			Element eBox=qc.createElement("div");
 			eBox.setAttribute("class","dropbox");
 			String sGroup=getString(PROPERTY_GROUP);
 			String sColour=convertHash("innerbg"+
 					(getQDocument().getGroupIndex(sGroup)%5) );
 			String sBorderColour=sColour;
 			// If on white background or forced, use a light grey
 			if(
 				getBoolean(PROPERTY_FORCEBORDER) ||
 				convertHash(getBackground()).equals(sBorderColour))
 				sBorderColour="#ccc";
 
 			eBox.setAttribute("style",
 				"background:"+sColour+"; border-color: "+sBorderColour+";");
 			eBox.setAttribute("id",QDocument.ID_PREFIX+getID()+"box");
 			if(isEnabled())
 				eBox.setAttribute("tabindex","0");
 			qc.addInlineXHTML(eBox);
 
 			Element eInput=qc.createElement("input");
 			eInput.setAttribute("type","hidden");
 			eInput.setAttribute("name",QDocument.ID_PREFIX+QDocument.VALUE_PREFIX+getID());
 			eInput.setAttribute("id",QDocument.ID_PREFIX+QDocument.VALUE_PREFIX+getID());
 			eInput.setAttribute("value",getValue());
 			qc.addInlineXHTML(eInput);
 
 			Element eScript=qc.createElement("script");
 			eScript.setAttribute("type","text/javascript");
 			XML.createText(eScript,"addOnLoad(function() { dropboxFix('"+getID()+"','"+QDocument.ID_PREFIX+"',"+
 				(isEnabled() ? "true" : "false") + ",'"+sGroup+"','"+sBorderColour+"'); });");
 			qc.addInlineXHTML(eScript);
 
 			if(isEnabled()) qc.informFocusable(eBox.getAttribute("id"),bPlain);
 		}
 
 		String sSideLabel=null;
 		if(isPropertySet(PROPERTY_SIDELABEL))
 		{
 			sSideLabel=getString(PROPERTY_SIDELABEL);
 			Element eSideLabel=qc.createElement("span");
 			eSideLabel.setAttribute("class","dropboxsidelabel");
 			eSideLabel.appendChild(
 					qc.getOutputDocument().createTextNode(sSideLabel));
 			qc.addInlineXHTML(eSideLabel);
 		}
 
 		qc.addTextEquivalent("[Dropbox: "+getValue()+"]"+
 				(sSideLabel!=null ? sSideLabel + " " : ""));
 
 	}
 
 	@Override
 	protected void formSetValue(String sValue,ActionParams ap) throws OmException
 	{
 		if(ap.hasParameter("plain"))
 		{
 			// In plain mode, value = text of the selected option (ARGH)
 			String sAnswer=getPlainAnswers().get(sValue);
 			if(sAnswer==null)
 				throw new OmException("Unexpected dropdown value: "+sValue+"\n"+sbCrap.toString());
 
 			setString(PROPERTY_VALUE,sAnswer);
 		}
 		else
 		{
 			// Not in plain mode, then the value = ID
 			setString(PROPERTY_VALUE,sValue);
 		}
 	}
 
 	/** @return ID of dragbox that's placed in box (null if none) */
 	public String getValue()
 	{
 		try
 		{
 			return getString(PROPERTY_VALUE);
 		}
 		catch(OmDeveloperException e)
 		{
 			throw new OmUnexpectedException(e);
 		}
 	}
 
 	/** @param sValue ID of dragbox that should be placed in box */
 	public void setValue(String sValue)
 	{
 		try
 		{
 			setString(PROPERTY_VALUE,sValue);
 		}
 		catch(OmDeveloperException e)
 		{
 			throw new OmUnexpectedException(e);
 		}
 	}
 
 	/** Clears the box, removing any component */
 	public void clear()
 	{
 		setValue("");
 	}
 }
