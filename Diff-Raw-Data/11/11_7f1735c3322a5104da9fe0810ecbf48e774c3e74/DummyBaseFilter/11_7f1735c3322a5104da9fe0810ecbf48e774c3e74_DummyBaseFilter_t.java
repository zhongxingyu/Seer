 /*===========================================================================
   Copyright (C) 2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.filters.tests;
 
 import java.io.InputStream;
 import java.net.URI;
 import java.util.ArrayList;
 
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.filters.BaseFilter;
 import net.sf.okapi.common.filters.PropertyTextUnitPlaceholder;
 import net.sf.okapi.common.filters.PropertyTextUnitPlaceholder.PlaceholderType;
 import net.sf.okapi.common.resource.TextFragment.TagType;
 import net.sf.okapi.common.skeleton.GenericSkeleton;
 
 public class DummyBaseFilter extends BaseFilter {
 
 	public void close() {
 	}
 
 	public String getName() {
 		return "DummyBaseFilter";
 	}
 
 	public IParameters getParameters() {
 		return null;
 	}
 
 	public void open (InputStream input) {
 	}
 
 	public void open(CharSequence inputText) {
 		if ( "2".equals(inputText) ) createCase2();
 		else createCase1();
 	}
 
 	public void open (URI inputURI) {
 	}
 
 	public void setParameters (IParameters params) {
 	}
 
 	private void createCase1 () {
 		setMimeType("text/xml");
		startFilter();
 		this.startTextUnit("Text.");
 		this.endTextUnit();
 		this.startDocumentPart("<docPart/>");
 		this.addToDocumentPart("<secondPart/>");
 		this.endDocumentPart();
		this.endFilter();
 	}
 
 	private void createCase2 () {
 		setMimeType("text/xml");
 		setNewlineType("\n");
		startFilter();
 		ArrayList<PropertyTextUnitPlaceholder> list = new ArrayList<PropertyTextUnitPlaceholder>();
 		list.add(new PropertyTextUnitPlaceholder(PlaceholderType.WRITABLE_PROPERTY, "attr", "val1", 10, 14));
 		//TODO: Skeleton should be GenericSkeleton since BaseFilter uses only that one
 		this.startTextUnit("Before ", new GenericSkeleton("<tu attr='val1'>"), list);
 		this.addToTextUnit(TagType.OPENING, "<b>", "bold");
 		this.addToTextUnit("Text");
 		this.addToTextUnit(TagType.CLOSING, "</b>", "bold");
		this.endFilter();
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sf.okapi.common.filters.BaseFilter#hasUtf8Bom()
 	 */
 	@Override
 	protected boolean hasUtf8Bom() {
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see net.sf.okapi.common.filters.BaseFilter#hasUtf8Encoding()
 	 */
 	@Override
 	protected boolean hasUtf8Encoding() {
 		return false;
 	}	
 }
