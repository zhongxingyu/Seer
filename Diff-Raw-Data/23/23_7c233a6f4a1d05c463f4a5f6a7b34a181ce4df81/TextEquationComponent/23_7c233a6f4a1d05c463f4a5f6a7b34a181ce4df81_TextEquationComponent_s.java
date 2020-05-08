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
 
 import om.*;
 import om.equation.TextEquation;
 import om.stdquestion.*;
 
 import org.w3c.dom.*;
 
 import util.xml.XML;
 
 /** 
 Represents simple equations that can be produced as XHTML. Uses the same 
 LaTeX-like format as {@link EquationComponent}, so not sensitive to whitespace,
 but does not support any advanced formatting. (You can use superscript and subscript
 and the usual variable names etc., but none of the fancy formatting tags - fractions 
 or sigmas or square roots or anything like that.) 
 <p>
 Doesn't always need an alt property because it generates one itself. x^2 gives 'x to the power 2',
 while \log_10 2 gives 'log subscript 10 2'. In the latter case you might wish to override the 
 alt property to make it say something more sensible.  
 <h2>XML usage</h2>
 &lt;eq&gt;z^2=x^2+y^2&lt;/eq&gt;
 <h2>Properties</h2>
 <table border="1">
 <tr><th>Property</th><th>Values</th><th>Effect</th></tr>
 <tr><td>id</td><td>(string)</td><td>Specifies unique ID</td></tr>
 <tr><td>display</td><td>(boolean)</td><td>Includes in/removes from output</td></tr>
 <tr><td>enabled</td><td>(boolean)</td><td>No effect</td></tr>
 <tr><td>textfont</td><td>(boolean)</td><td>Uses default text font instead of the equation font</td></tr>
 <tr><td>alt</td><td>(string)</td><td>Optional alt tag, use if the default alternate text is not clear enough.</td></tr>
 <tr><td>italic</td><td>(boolean)</td><td>Set 'no' to stop letters being italicised.</td></tr>
 </table>
 */
 public class TextEquationComponent extends QComponent
 {
 	/** Boolean property: if true, uses Verdana instead of Times */
 	public final static String PROPERTY_TEXTFONT="textfont";
 	/** Optional alt tag, if not included uses default */
 	public final static String PROPERTY_ALT="alt";
 	/** Boolean property: if false, does not make letters italic */
 	public final static String PROPERTY_ITALIC="italic";
 	
 	/** @return Tag name (introspected; this may be replaced by a 1.5 annotation) */
 	public static String getTagName()
 	{
 		return "eq";
 	}
 	
 	/** Actual content of required equation */
 	private String sEquation;
 	
 	protected void defineProperties() throws OmDeveloperException
 	{
 		super.defineProperties();
 		defineBoolean(PROPERTY_TEXTFONT);
 		setBoolean(PROPERTY_TEXTFONT,false);
 		defineBoolean(PROPERTY_ITALIC);
 		setBoolean(PROPERTY_ITALIC,true);
 		defineString(PROPERTY_ALT);
 	}
 	
 	protected void initChildren(Element eThis) throws OmException
 	{
 		StringBuffer sbText=new StringBuffer();
 		for(Node n=eThis.getFirstChild();n!=null;n=n.getNextSibling())
 		{
 			if(n instanceof Element)
 			{
 				throw new OmFormatException(
 					"<eq> may not contain children other than text");
 			}
 			else if(n instanceof Text)
 			{
 				sbText.append(n.getNodeValue());
 			}			
 		}
 		sEquation=sbText.toString();
 	}
 	
 	/** @return Text of equation */
 	public String getEquation() { return sEquation; }
 	
 	/** 
 	 * Sets the equation to a different value.
 	 * @param sEquation New value for equation content
 	 */
 	public void setEquation(String sEquation)
 	{ 
 		this.sEquation=sEquation;
 	}
 	
 	public void produceVisibleOutput(QContent qc,boolean bInit,boolean bPlain) throws OmException
 	{
 		// Get actual current value of string
 		String sCurrent=getQuestion().applyPlaceholders(sEquation);
 		
 		// Convert equation to XHTML
 		Element eContent=TextEquation.process(sCurrent,qc.getOutputDocument(),getBoolean(PROPERTY_ITALIC));
 
 		// Get text equivalent
 		String sAlt=eContent.getAttribute("alt");
 		if(isPropertySet(PROPERTY_ALT)) sAlt=getString(PROPERTY_ALT);
 		
 		eContent.removeAttribute("alt");
 		qc.addTextEquivalent(sAlt);
 
 		if(!bPlain)
 		{
			// Add XHTML
 			if(getBoolean(PROPERTY_TEXTFONT))
 			{
 				// Use default font
 				eContent.setAttribute("class","textequationtextfont");
 			}
 			else
 			{
 				if(getQuestion().getZoom()>1.0)
 				{					
 					// This needs to be changed here and in CSS if size is not 14px.
 					eContent.setAttribute("style","font-size: "+Math.round(getQuestion().getZoom()*14)+"px");
 				}
 			}
			qc.addInlineXHTML(eContent);
 			
			// Hack in the 
 		}
 		else
 		{
 			// We have to use the alt text for plain mode - screenreaders cannot
 			// even cope with <sup> and <sub>!! 
 			Element eSpan=qc.createElement("span");
 			XML.createText(eSpan,sAlt);
 			qc.addInlineXHTML(eSpan);
 		}
 		
 	}
 	
 }
