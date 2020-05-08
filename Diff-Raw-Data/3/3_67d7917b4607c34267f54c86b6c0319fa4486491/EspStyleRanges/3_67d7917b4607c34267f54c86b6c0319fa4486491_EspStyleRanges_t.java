 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.eclipse.esp.editor;
 
 import static org.oobium.build.esp.Constants.*;
 import static org.oobium.build.esp.EspPart.Type.*;
 
 import java.util.Arrays;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.graphics.Color;
 import org.oobium.build.esp.EspDom;
 import org.oobium.build.esp.EspElement;
 import org.oobium.build.esp.EspPart;
 import org.oobium.build.esp.EspPart.Type;
 import org.oobium.build.esp.parts.StylePropertyPart;
 import org.oobium.eclipse.esp.EspPlugin;
 
 public class EspStyleRanges {
 
 	private static final StyleRange level = new StyleRange(-1, -1, color(0, 0, 0), color(225, 225, 225));
 	private static final StyleRange htmlTag = new StyleRange(-1, -1, color(0, 0, 128), null);
 	private static final StyleRange cssPropertyName = new StyleRange(-1, -1, color(0, 64, 128), null);
 	private static final StyleRange javaKeyword = new StyleRange(-1, -1, color(128, 0, 86), null, SWT.BOLD);
 	private static final StyleRange javaString = new StyleRange(-1, -1, color(0, 0, 128), null);
 	private static final StyleRange operator = new StyleRange(-1, -1, color(128, 32, 32), null);
 	private static final StyleRange comment = new StyleRange(-1, -1, color(32, 128, 32), null);
 	private static final StyleRange innerText = new StyleRange(-1, -1, color(128, 128, 128), null);
 
 	private static Color color(int r, int g, int b) {
 		return EspPlugin.getDefault().getEspColorProvider().getColor(r, g, b);
 	}
 	
 	private EspDom dom;
 	private int styleCount;
 	private StyleRange[] styles;
 	private int[] ranges; // int[0] = offset; int[1] = length
 	
 	public EspStyleRanges(EspDom dom) {
 		this.dom = dom;
 		evaluate();
 	}
 
 	private int addRange(int offset, EspPart part, StyleRange style) {
 		EspPart next = part.getNextSubPart(offset);
 		int end = (next != null) ? next.getStart() : part.getEnd();
 		return addRange(offset, end-offset, style);
 	}
 	
 	private int addRange(int offset, int length, StyleRange style) {
 		if(styleCount >= styles.length) {
 			styles = Arrays.copyOf(styles, styles.length + 1000);
 			ranges = Arrays.copyOf(ranges, ranges.length + 2000);
 		}
 		ranges[styleCount*2] = offset;
 		ranges[styleCount*2+1] = length;
 		styles[styleCount] = style;
 		styleCount++;
 		return offset + length;
 	}
 	
 	public boolean applyRanges(StyledText widget) {
 		try {
 			if(ranges.length > 1 && (widget.getCharCount() >= (ranges[ranges.length-2] + ranges[ranges.length-1]))) {
 				widget.setStyleRanges(ranges, styles);
 				return true;
 			}
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	private void evaluate() {
 		styleCount = 0;
 		styles = new StyleRange[1000];
 		ranges = new int[2000];
 		
 		int offset = 0;
 		while(offset < dom.getEnd()) {
 			char c = dom.charAt(offset);
 			if(c == '\t' && (offset == 0 || dom.charAt(offset-1) == '\n')) {
 				int end = offset;
 				while(end < dom.getEnd()) {
 					if(dom.charAt(end) == '\t') end++;
 					else break;
 				}
 				if(dom.isScript()) {
 					offset = end;
 				} else {
 					offset = addRange(offset, end-offset, level);
 				}
 			} else if(c == '<' && offset < (dom.getEnd()-1) && dom.charAt(offset+1) == '-') {
 				offset = addRange(offset, 2, operator);
 			} else {
 				EspPart part = dom.getPart(offset);
 				if(part != null) {
 					if(part.isA(CommentPart)) {
 						offset = addRange(offset, part.getEnd()-offset, comment);
 						continue;
 					} else {
 						EspElement element = part.getElement();
 						if(element != null) {
 							offset = evaluate(offset, element, part);
 							continue;
 						}
 					}
 				}
 				offset++;
 			}
 		}
 		
 		styles = Arrays.copyOf(styles, styleCount);
 		ranges = Arrays.copyOf(ranges, styleCount*2);
 	}
 	
 	private int evaluate(int offset, EspElement element, EspPart part) {
 		switch(element.getType()) {
 		case CommentElement:	return evaluateComment(offset, element, part);
 		case ConstructorElement:return evaluateConstructor(offset, element, part);
 		case HtmlElement:		return evaluateHtml(offset, element, part);
 		case ImportElement:		return evaluateImport(offset, element, part);
 		case InnerTextElement:	return evaluateInnerText(offset, element, part);
 		case JavaElement:		return evaluateJava(offset, element, part);
 		case ScriptElement:		return evaluateScript(offset, element, part);
 		case StyleElement:		return evaluateStyle(offset, element, part);
 		case YieldElement:		return evaluateYield(offset, element, part);
 		default:				return offset + 1;
 		}
 	}
 	
 	private int evaluateComment(int offset, EspElement element, EspPart part) {
 		return addRange(element.getStart(), element.getLength(), comment);
 	}
 
 	private int evaluateConstructor(int offset, EspElement element, EspPart part) {
 		switch(part.getType()) {
 		case TagPart:
 			return addRange(part.getStart(), part.getLength(), javaKeyword);
 		case VarNamePart:
 			return part.getEnd();
 		case DefaultValuePart:
 		case VarTypePart:
 			return evaluateJava(offset, element, part);
 		}
 		return offset + 1;
 	}
 
 	private int evaluateHtml(int offset, EspElement element, EspPart part) {
 		switch(part.getType()) {
 		case InnerTextPart:
 			return evaluateInnerText(offset, element, part);
 		case JavaPart:
 			return evaluateJava(offset, element, part);
 		case JavaTypePart:
 			return part.getEnd();
 		case StylePropertyNamePart:
 			return evaluateStyle(offset, element, part);
 		case StylePropertyValuePart:
 			return part.getEnd();
 		case StylePart:
 			if(part.getLength() == 4 && "hide".equals(part.getText())) {
 				return addRange(offset, 4, operator);
 			}
 			return part.getEnd();
 		case TagPart:
 			if(HTML_TAGS.containsKey(part.getText())) {
 				return addRange(offset, part.getEnd()-offset, htmlTag);
 			} else {
 				break;
 			}
 		case YieldElement:
 			return addRange(offset, part.getEnd()-offset, javaKeyword);
 		}
 		
 		return offset + 1;
 	}
 
 	private int evaluateImport(int offset, EspElement element, EspPart part) {
 		if(part.isA(ImportPart)) {
 			String s = part.getText();
 			if("import".equals(s) || "static".equals(s)) {
 				return addRange(offset, part.getEnd()-offset, javaKeyword);
 			}
 		}
 		return offset + 1;
 	}
 
 	private int evaluateInnerText(int offset, EspElement element, EspPart part) {
		if(part.isA(JavaPart)) {
			return evaluateJava(offset, element, part);
		}
 		return addRange(offset, part, innerText);
 	}
 
 	private int evaluateJava(int offset, EspElement element, EspPart part) {
 		if(part.isA(Type.JavaElement)) {
 			return offset + 1;
 		}
 		
 		int end = part.getEnd();
 		for(int s1 = offset; s1 < end; s1++) {
 			while(s1 < end && !Character.isLetter(part.charAt(s1))) {
 				if(part.charAt(s1) == '"') {
 					int s = s1 + 1;
 					while(s < end) {
 						if(part.charAt(s) == '"' && part.charAt(s-1) != '\\') {
 							break;
 						}
 						s++;
 					}
 					addRange(s1, s-s1, javaString);
 					s1 = s + 1;
 				} else {
 					s1++;
 				}
 			}
 			if(s1 < end) {
 				int s2 = s1;
 				while(s2 < end && Character.isLetter(part.charAt(s2))) {
 					s2++;
 				}
 				if(JAVA_KEYWORDS.contains(part.subSequence(s1, s2))) {
 					s1 = addRange(s1, s2-s1, javaKeyword);
 				} else {
 					s1 = s2;
 				}
 			}
 		}
 		return end;
 	}
 
 	private int evaluateScript(int offset, EspElement element, EspPart part) {
 		switch(part.getType()) {
 		case TagPart:
 			return addRange(offset, part.getEnd()-offset, htmlTag);
 		case ScriptElement:
 		case ScriptPart:
 			int end = part.getEnd();
 			for(int s1 = offset; s1 < end; s1++) {
 				while(s1 < end && !Character.isLetter(part.charAt(s1))) {
 					char c = part.charAt(s1);
 					if(c == '\t' && part.charAt(s1-1) == '\n') {
 						int s = s1 + 1;
 						while(s < end && (s-s1) < element.getLevel() + 1) {
 							if(part.charAt(s) != '\t') {
 								break;
 							}
 							s++;
 						}
 						if(!dom.isScript()) {
 							addRange(s1, s-s1, level);
 						}
 						s1 = s;
 					} else if(c == '"' || c == '\'') {
 						int s = s1 + 1;
 						while(s < end) {
 							if(part.charAt(s) == c && part.charAt(s-1) != '\\') {
 								break;
 							}
 							s++;
 						}
 						addRange(s1, s-s1, javaString);
 						s1 = s + 1;
 					} else {
 						s1++;
 					}
 				}
 				if(s1 < end) {
 					int s2 = s1;
 					while(s2 < end && Character.isLetter(part.charAt(s2))) {
 						s2++;
 					}
 					if(JS_KEYWORDS.contains(part.subSequence(s1, s2))) {
 						s1 = addRange(s1, s2-s1, javaKeyword);
 					} else {
 						s1 = s2;
 					}
 				}
 			}
 			return end;
 		}
 
 		return offset + 1;
 	}
 
 	private int evaluateStyle(int offset, EspElement element, EspPart part) {
 		switch(part.getType()) {
 		case TagPart:
 			return addRange(offset, part.getEnd()-offset, htmlTag);
 		case StyleSelectorPart:
 			return part.getEnd();
 		case StylePropertyNamePart:
 			if(CSS_PROPERTIES.containsKey(part.getText())) {
 				return addRange(offset, part.getEnd()-offset, cssPropertyName);
 			}
 			break;
 		case StylePropertyValuePart:
 			if(((StylePropertyPart) part.getParent()).isValueJava()) {
 				return evaluateJava(offset, element, part);
 			} else {
 				return part.getEnd();
 			}
 		}
 
 		return offset + 1;
 	}
 
 	private int evaluateYield(int offset, EspElement element, EspPart part) {
 		if(part.getType() == TagPart) {
 			return addRange(offset, part.getEnd()-offset, javaKeyword);
 		}
 		return element.getEnd();
 	}
 	
 }
