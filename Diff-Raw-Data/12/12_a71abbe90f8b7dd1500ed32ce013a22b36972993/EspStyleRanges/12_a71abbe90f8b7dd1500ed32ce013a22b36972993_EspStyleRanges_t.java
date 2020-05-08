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
 
 import static org.oobium.build.esp.Constants.CSS_PROPERTIES;
 import static org.oobium.build.esp.Constants.HTML_TAGS;
 import static org.oobium.build.esp.Constants.JAVA_KEYWORDS;
 import static org.oobium.build.esp.Constants.JS_KEYWORDS;
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
 import org.oobium.build.esp.parts.StyleEntryPart;
 import org.oobium.eclipse.esp.EspPlugin;
 
 public class EspStyleRanges {
 
 	private static final StyleRange level = new StyleRange(-1, -1, color(0, 0, 0), color(225, 225, 225));
 	private static final StyleRange htmlTag = new StyleRange(-1, -1, color(0, 0, 128), null);
 	private static final StyleRange cssPropertyName = new StyleRange(-1, -1, color(0, 64, 128), null);
 	private static final StyleRange javaKeyword = new StyleRange(-1, -1, color(128, 0, 86), null, SWT.BOLD);
 	private static final StyleRange javaString = new StyleRange(-1, -1, color(0, 0, 128), null);
 	private static final StyleRange operator = new StyleRange(-1, -1, color(128, 32, 32), null);
 	private static final StyleRange comment = new StyleRange(-1, -1, color(32, 128, 32), null);
 	private static final StyleRange taskTag = new StyleRange(-1, -1, color(127, 159, 191), null, SWT.BOLD);
 	private static final StyleRange innerText = new StyleRange(-1, -1, color(128, 128, 128), null);
 
 	private static final char[][] TASK_TAGS = {
 		"TASK".toCharArray(),
 		"TODO".toCharArray(),
 		"XXX".toCharArray()
 	};
 
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
 		
 		EspElement current = null;
 		int offset = 0;
 		while(offset < dom.getEnd()) {
 			char c = dom.charAt(offset);
 			if(c == '\t' && 
 					(current == null || !current.isA(Type.ScriptElement)) && 
 					(offset == 0 || dom.charAt(offset-1) == '\n')) {
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
 						offset = evaluateComment(offset, null, part);
 						continue;
 					} else {
 						EspElement element = part.getElement();
 						current = element;
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
 		case MarkupCommentElement:
 		case MarkupElement:		return evaluateMarkup(offset, element, part);
 		case ImportElement:		return evaluateImport(offset, element, part);
 		case InnerTextElement:	return evaluateInnerText(offset, element, part);
 		case JavaElement:		return evaluateJava(offset, element, part);
 		case ScriptElement:		return evaluateScript(offset, element, part);
 		case StyleElement:
 		case StyleChildElement:
 								return evaluateStyle(offset, element, part);
 		case YieldElement:		return evaluateYield(offset, element, part);
 		default:				return offset + 1;
 		}
 	}
 	
 	private int evaluateComment(int offset, EspElement element, EspPart part) {
 		int end = part.getEnd();
 		for(int i = offset; i < end; i++) {
 			for(int j = 0; j < TASK_TAGS.length; j++) {
 				if(part.isNext(i, TASK_TAGS[j])) {
 					addRange(offset, i-offset, comment);
 					i = addRange(i, TASK_TAGS[j].length, taskTag);
 					offset = i;
 					break;
 				}
 			}
 		}
 		if(offset < end) {
 			addRange(offset, end-offset, comment);
 		}
 		return end+1;
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
 
 	private int evaluateMarkup(int offset, EspElement element, EspPart part) {
 		switch(part.getType()) {
 		case InnerTextPart:
 			return evaluateInnerText(offset, element, part);
 		case JavaPart:
 		case JavaSourcePart:
 			return evaluateJava(offset, element, part);
 		case JavaTypePart:
 			return part.getEnd();
 		case ScriptPart:
 			return evaluateScript(offset, element, part);
 		case StyleEntryPart:
 			if(((StyleEntryPart) part).isJava()) {
 				return evaluateJava(offset, element, part);
 			}
 			break;
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
 			while(s1 < end && !Character.isLetter(dom.charAt(s1))) {
 				if(dom.charAt(s1) == '"') {
 					int s = s1 + 1;
 					while(s < end) {
 						if(dom.charAt(s) == '"' && dom.charAt(s-1) != '\\') {
 							break;
 						}
 						s++;
 					}
 					EspPart sub = part.getNextSubPart(s1 + 1);
 					while(sub != null && sub.getStart() < s) {
						addRange(s1, sub.getStart()-s1+1, javaString);
 						s1 = evaluateJava(sub.getStart(), element, sub);
 						sub = part.getNextSubPart(s1);
 					}
 					if(s > s1) {
						addRange(s1, s-s1+1, javaString);
 						s1 = s + 1;
 					}
 				} else {
 					s1++;
 				}
 			}
 			if(s1 < end) {
 				int s2 = s1;
 				while(s2 < end && Character.isLetter(dom.charAt(s2))) {
 					s2++;
 				}
 				if(JAVA_KEYWORDS.contains(part.getDom().subSequence(s1, s2))) {
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
 			if(dom.charAt(offset) == '\t' && dom.charAt(offset-1) == '\n') {
 				addRange(offset, 1, level);
 			}
 			return offset + 1;
 		case JavaPart:
 		case JavaSourcePart:
 			return evaluateJava(offset, element, part);
 		case ScriptPart:
 			EspPart next = part.getNextSubPart(offset);
 			int end = (next != null) ? next.getStart() : part.getEnd();
 			if(!element.isA(ScriptElement)) {
 				if(dom.charAt(offset) == '"') offset++;
 				if(dom.charAt(end-1) == '"') end--;
 			}
 			for(int s1 = offset; s1 < end; s1++) {
 				while(s1 < end && !Character.isLetter(dom.charAt(s1))) {
 					char c = dom.charAt(s1);
 					if(c == '\t' && dom.charAt(s1-1) == '\n') {
 						int s = s1 + 1;
 						while(s < end && (s-s1) < element.getLevel() + 1) {
 							if(dom.charAt(s) != '\t') {
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
 							if(dom.charAt(s) == c && dom.charAt(s-1) != '\\') {
 								break;
 							}
 							s++;
 						}
						addRange(s1, s-s1+1, javaString);
 						s1 = s + 1;
 					} else {
 						s1++;
 					}
 				}
 				if(s1 < end) {
 					int s2 = s1;
 					while(s2 < end && Character.isLetter(dom.charAt(s2))) {
 						s2++;
 					}
 					if(JS_KEYWORDS.contains(part.getDom().subSequence(s1, s2))) {
 						s1 = addRange(s1, s2-s1, javaKeyword);
 					} else {
 						s1 = s2;
 					}
 				}
 			}
 			return (next != null) ? next.getStart() : part.getEnd();
 		}
 
 		return offset + 1;
 	}
 	
 	private int evaluateStyle(int offset, EspElement element, EspPart part) {
 		switch(part.getType()) {
 		case TagPart:
 			return addRange(offset, part.getEnd()-offset, htmlTag);
		case JavaPart:
		case JavaSourcePart:
			return evaluateJava(offset, element, part);
 		case VarNamePart:
 			return part.getEnd();
 		case StyleMixinPart:
 		case DefaultValuePart:
 		case VarTypePart:
 			return evaluateJava(offset, element, part);
 		case StyleSelectorPart:
 			return part.getEnd();
 		case StylePropertyNamePart:
 			if(CSS_PROPERTIES.containsKey(part.getText())) {
 				return addRange(offset, part.getEnd()-offset, cssPropertyName);
 			}
 			break;
 		case StylePropertyValuePart:
 			return part.getEnd();
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
