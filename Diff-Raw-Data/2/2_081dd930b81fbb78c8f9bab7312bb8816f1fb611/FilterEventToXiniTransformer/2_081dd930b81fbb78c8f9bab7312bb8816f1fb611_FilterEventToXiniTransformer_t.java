 /*===========================================================================
   Copyright (C) 2011 by the Okapi Framework contributors
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
 
 package net.sf.okapi.filters.xini;
 
 import java.io.OutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.resource.Segment;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextFragment.TagType;
 import net.sf.okapi.filters.xini.jaxb.Element;
 import net.sf.okapi.filters.xini.jaxb.EndPlaceHolder;
 import net.sf.okapi.filters.xini.jaxb.Field;
 import net.sf.okapi.filters.xini.jaxb.Fields;
 import net.sf.okapi.filters.xini.jaxb.Main;
 import net.sf.okapi.filters.xini.jaxb.ObjectFactory;
 import net.sf.okapi.filters.xini.jaxb.Page;
 import net.sf.okapi.filters.xini.jaxb.PlaceHolder;
 import net.sf.okapi.filters.xini.jaxb.PlaceHolderType;
 import net.sf.okapi.filters.xini.jaxb.Seg;
 import net.sf.okapi.filters.xini.jaxb.StartPlaceHolder;
 import net.sf.okapi.filters.xini.jaxb.Trans;
 import net.sf.okapi.filters.xini.jaxb.Xini;
 import net.sf.okapi.filters.xini.jaxb.Page.Elements;
 
 public class FilterEventToXiniTransformer {
 
 	private ObjectFactory objectFactory = new ObjectFactory();
 	private Marshaller m;
 	private JAXBContext jc;
 
 	private Xini xini;
 	private Main main;
 	private Page currentPage;
 
 	private int currentPageId;
 	private int currentElementId;
 	private int currentFieldId;
 
 	public void init() {
 		try {
 
 			jc = JAXBContext.newInstance(ObjectFactory.class);
 			m = jc.createMarshaller();
 			m.setProperty("jaxb.noNamespaceSchemaLocation", "http://www.ontram.com/xsd/xini.xsd");
 
 		} catch (JAXBException e) {
 			throw new RuntimeException(e);
 		}
 		
 		currentPageId = 0;
 		currentElementId = 10;
 		currentFieldId = 0;
 		
 		xini = objectFactory.createXini();
 		xini.setSchemaVersion("1.0");
 		main = objectFactory.createMain();
 		xini.setMain(main);
 	}
 
 	protected void startPage(String name) {
 		currentPage = new Page();
 		currentPageId += 1;
 		currentElementId = 10;
 		currentFieldId = 0;
 
 		currentPage.setPageID(currentPageId);
 		currentPage.setPageName(name);
 
 		currentPage.setElements(new Elements());
 		xini.getMain().getPage().add(currentPage);
 	}
 	
 	public void transformTextUnit(ITextUnit tu) {
 
 		// Get the source container
 		TextContainer textContainer = tu.getSource();
 		Set<LocaleId> targetLocals = tu.getTargetLocales();
 
 		// Skip non-translatable TextUnits
 		if ( !tu.isTranslatable() ) {
 			return;
 		}
 
 		Field field = prepareXiniStructure(tu);
 
 		int currentSegmentId = 0;
 		StringBuilder emptySegsFlags = new StringBuilder();
 		
 		for (Segment okapiSegment : textContainer.getSegments()) {
 			
 			TextFragment textFragment = okapiSegment.getContent();
 			
 			if (!textFragment.isEmpty()) {
 
 				Seg xiniSegment = objectFactory.createSeg();
 				xiniSegment.setSegID(currentSegmentId);
 				field.getSegAndTrans().add(xiniSegment);
 				
 				List<Code> codes = textFragment.getCodes();
 				
 				if (codes.size() > 0)
 					xiniSegment.getContent().addAll(
 							transformInlineTags(textFragment.getCodedText(), codes));
 				else
 					xiniSegment.getContent().add(textFragment.getText());
 				
 				emptySegsFlags.append("0");
 			}
 			else {
 				emptySegsFlags.append("1");
 			}
 
 			for (LocaleId trgLoc : targetLocals) {
 				Segment trgSegment = tu.getTargetSegment(trgLoc, okapiSegment.id, false);
 
 				TextFragment trgTextFragment = trgSegment.getContent();
 				if (!trgTextFragment.isEmpty()) {
 					Trans xiniTrans = objectFactory.createTrans();
 					xiniTrans.setSegID(currentSegmentId);
 					xiniTrans.setLanguage(trgLoc.toBCP47());
 					field.getSegAndTrans().add(xiniTrans);
 
 					List<Code> codes = trgTextFragment.getCodes();
 					if (codes.size() > 0)
 						xiniTrans.getContent().addAll(transformInlineTags(
 										trgTextFragment.getCodedText(), codes));
 					else
 						xiniTrans.getContent().add(trgTextFragment.getText());
 				}
 
 			}
 
 			currentSegmentId++;
 		}
 		
 		field.setEmptySegmentsFlags(emptySegsFlags.toString());
 
 	}
 
 	private Field prepareXiniStructure(ITextUnit tu) {
 		// Create XML elements
 		Element element = objectFactory.createElement();
 		Element.ElementContent elementContent = objectFactory.createElementElementContent();
 		Fields fields = objectFactory.createFields();
 		Field field = objectFactory.createField();
 
 		// Connect XML elements
 		currentPage.getElements().getElement().add(element);
 		element.setElementContent(elementContent);
 		elementContent.setFields(fields);
 		fields.getField().add(field);
 
 		// Set IDs and add meta-data
 		element.setElementID(currentElementId);
 		field.setFieldID(currentFieldId);
 		field.setExternalID(tu.getId());
 		field.setLabel(tu.getName());
 		return field;
 	}
 
 	private ArrayList<Serializable> transformInlineTags(String codedText, List<Code> codes) {
 		ArrayList<Serializable> parts = new ArrayList<Serializable>();
 		StringBuilder tempString = new StringBuilder();
 
 		for (int charIndex = 0; charIndex < codedText.length(); charIndex++) {
 			
 			char chr = codedText.charAt(charIndex);
 			
 			if (!TextFragment.isMarker(chr)) {
 
 				// This is a regular character
 				tempString.append(chr);
 			}
 			
 			else {
 				
 				// This is a code
 				int codePoint = codedText.codePointAt(charIndex);
 				Integer codeIndex = TextFragment.toIndex(codedText.charAt(++charIndex));
 				Code code = codes.get(codeIndex);
 				boolean codeIsIsolated = false;
 
 				// Save last part of the text that had no codes
 				if (tempString.length() > 0)
 					parts.add(tempString.toString());
 				tempString = new StringBuilder();
 
 				switch(codePoint) {
 				case TextFragment.MARKER_OPENING:
 
 					Integer endMarkerIndex = findEndMark(codes, code, codedText, charIndex);
 					String innerCodedText = null;
 					
 					if(endMarkerIndex != null) {
 						
 						innerCodedText = codedText.substring(charIndex + 1, endMarkerIndex - 1);
 						charIndex = endMarkerIndex;
 					}
 					else {
 						codeIsIsolated = true;
 					}
 					
 					parts.add(codeToXMLObject(code, codes, innerCodedText, codeIsIsolated));
 					break;
 				
 				case TextFragment.MARKER_CLOSING:
 					
 					// This closing code does not have it's corresponding opening code in the same segment
 					parts.add(codeToXMLObject(code, codes, null, true));
 					break;
 				
 				case TextFragment.MARKER_ISOLATED:
 					
 					parts.add(codeToXMLObject(code, codes, null, true));
 					break;
 				}
 			}
 		}
 
 		if (tempString.length() > 0)
 			parts.add(tempString.toString());
 		return parts;
 	}
 
 	private Integer findEndMark(List<Code> codes, Code code, String codedText, int startCharIndex) {
 
 		for (int charIndex = startCharIndex; charIndex < codedText.length(); charIndex++) {
 			
 			int codePoint = codedText.codePointAt(charIndex);
 			
 			if (codePoint == TextFragment.MARKER_CLOSING && codedText.length() > charIndex + 1) {
 
 				int endCodeIndex = TextFragment.toIndex(codedText.charAt(++charIndex));
 				Code endCode = codes.get(endCodeIndex);
 
				if (endCode.getType() == code.getType() && endCode.getId() == code.getId())
 					return charIndex;
 			}
 		}
 
 		// No closing marker found
 		return null;
 	}
 
 	private Serializable codeToXMLObject(Code code, List<Code> codes, String innerCodedText, boolean codeIsIsolated) {
 		
 		Serializable phelement;
 
 		if (!codeIsIsolated || codeIsIsolated && code.getTagType() == TagType.PLACEHOLDER) {
 			// use g/x-style placeholder
 			PlaceHolder ph = new PlaceHolder();
 			ph.setID(code.getId());
 			ph.setType(PlaceHolderType.PH);
 			
 			if (innerCodedText != null && !innerCodedText.isEmpty()) {
 				ph.getContent().addAll(transformInlineTags(innerCodedText, codes));
 			}
 			
 			phelement = objectFactory.createTextContentPh(ph);
 		}
 		else if (code.getTagType() == TagType.OPENING) {
 			// use bpt style placeholder
 			StartPlaceHolder sph = new StartPlaceHolder();
 			sph.setID(code.getId());
 			sph.setType(PlaceHolderType.PH);
 			phelement = objectFactory.createTextContentSph(sph);
 		}
 		else {
 			// use ept-style placeholder
 			EndPlaceHolder eph = new EndPlaceHolder();
 			eph.setID(code.getId());
 			eph.setType(PlaceHolderType.PH);
 			phelement = objectFactory.createTextContentEph(eph);
 		}
 
 		return phelement;
 	}
 
 	public void marshall(OutputStream os) {
 		try {
 			m.marshal(xini, os);
 		}
 		catch (JAXBException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 }
