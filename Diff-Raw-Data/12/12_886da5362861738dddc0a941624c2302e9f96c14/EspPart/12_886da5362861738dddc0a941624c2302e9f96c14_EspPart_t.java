 package org.oobium.build.esp.dom;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class EspPart implements CharSequence {
 
 	public enum Type {
 		DOM, Separator, Level, Comment,
 		ImportElement,
 		 ImportPart,
 		Constructor,
 		MarkupElement, MarkupTag, MarkupId, MarkupClass, InnerTextPart,
 		 MethodSigArgs, MethodSigArg,
 		 MethodArgs, MethodArg,
 		 VarName, // <- variable name, attribute name, method argument name, etc... 
 		 InnerTextElement, MarkupComment,
 
 		JavaElement,
 		JavaContainer,
 		 JavaEscape, // the escape char that opens a JavaContainer: tag#id_{h var} <- 'h' is a JavaEscape
 		 JavaSource,
 		  JavaKeyword,
 		  JavaString,
 		
 		ScriptElement,
 		 ScriptPart,
 		StyleElement,
 		 StylePart, // one
 		  StyleRuleset, // many (regular and parametric)
 		   // may have nested Rulesets
 		   StyleSelectorGroup, // many
 		    StyleSelector, // many
 		   StyleDeclaration, // one
 		    StyleProperty, // many (regular, mixin, and parametric mixin)
 		     StylePropertyName, // one
 		     // may also have MethodArgs
 		     StylePropertyValue, // one
 	}
 
 
 	protected EspDom dom;
 	protected EspPart parent;
 	protected EspElement element;
 	protected Type type;
 	protected int start;
 	protected int end;
 	protected List<EspPart> parts;
 
 	public EspPart(Type type) {
 		this.type = type;
 	}
 	
 	private EspPart addPart(EspPart part) {
 		if(parts == null) {
 			parts = new ArrayList<EspPart>();
 		}
 		parts.add(part);
 		return this;
 	}
 	
 	public char charAt(int offset) {
         if(offset < 0)            throw new StringIndexOutOfBoundsException(offset);
         if(start + offset >= end) throw new StringIndexOutOfBoundsException(offset);
         return dom.ca[start + offset];
 	}
 
 	public EspDom getDom() {
 		return dom;
 	}
 	
 	public EspElement getElement() {
 		return element;
 	}
 	
 	public int getEnd() {
 		return end;
 	}
 	
 	public EspPart getNextSubPart(int offset) {
 		if(parts != null) {
 			for(EspPart part : parts) {
 				if(part.start > offset) {
 					return part.getPart(part.start);
 				}
 			}
 		}
 		return null;
 	}
 	
 	public EspPart getParent() {
 		return parent;
 	}
 	
 	public EspPart getPart(int offset) {
 		if(parts != null) {
 			for(EspPart part : parts) {
 				if(part.start <= offset && offset < part.end) {
 					return part.getPart(offset);
 				}
 			}
 		}
 		if(start <= offset && offset < end) {
 			return this;
 		}
 		return null;
 	}
 	
 	public List<EspPart> getParts() {
 		return (parts != null) ? parts : new ArrayList<EspPart>(0);
 	}
 	
 	public int getStart() {
 		return start;
 	}
 
 	public String getText() {
 		return (end >= start) ? subSequence(0, length()) : "<incomplete>";
 	}
 	
 	public Type getType() {
 		return type;
 	}
 
 	public boolean hasParts() {
 		return parts != null && !parts.isEmpty();
 	}
 	
 	public boolean isA(Type type) {
 		return this.type == type;
 	}
 
 	public boolean isLast(EspPart part) {
 		return parts != null && part == parts.get(parts.size()-1);
 	}
 	
 	public int length() {
 		return end-start;
 	}
 
 	private EspPart removePart(EspPart part) {
 		if(parts != null) {
 			if(parts.remove(part)) {
 				part.parent = null;
 			}
 		}
 		return this;
 	}
 	
 	public EspPart setStart(int start) {
 		this.start = start;
 		return this;
 	}
 
 	public EspPart setEnd(int end) {
 		this.end = end;
 		return this;
 	}
 	
 	public EspPart setElement(EspElement element) {
 		this.element = element;
 		return this;
 	}
 	
 	public EspPart setParent(EspPart newParent) {
 		if(parent != null) {
 			parent.removePart(this);
 		}
 		parent = newParent;
 		if(parent != null) {
 			parent.addPart(this);
 			dom = parent.dom;
 		}
 		return this;
 	}
 	
 	public EspPart setType(Type type) {
 		this.type = type;
 		return this;
 	}
 
 	public boolean startsWith(char[] prefix) {
 		return startsWith(prefix, 0);
 	}
 	
 	public boolean startsWith(char[] prefix, int offset) {
 		if((start+offset+prefix.length) >= dom.ca.length) {
 			return false;
 		}
		int o = start+offset;
		for(int i = 0; i < prefix.length; i++) {
			if(prefix[i] == dom.ca[o+i] || (prefix[i] == ' ' && Character.isWhitespace(dom.ca[o+i]))) {
				continue;
 			}
			return false;
 		}
 		return true;
 	}
 	
 	public boolean startsWith(String prefix) {
 		return startsWith(prefix.toCharArray(), 0);
 	}
 	
 	public boolean startsWith(String prefix, int offset) {
 		return startsWith(prefix.toCharArray(), offset);
 	}
 	
 	public String subSequence(int beginIndex, int endIndex) {
         if(beginIndex < 0)         throw new StringIndexOutOfBoundsException(beginIndex);
         if(start + endIndex > end) throw new StringIndexOutOfBoundsException(endIndex);
         if(beginIndex > endIndex)  throw new StringIndexOutOfBoundsException(endIndex - beginIndex);
         return new String(dom.ca, start + beginIndex, endIndex - beginIndex);
 	}
 
 	public String substring(int beginIndex) {
 		return subSequence(beginIndex, length());
 	}
 	
 	public String substring(int beginIndex, int endIndex) {
 		return subSequence(beginIndex, endIndex);
 	}
 	
 	@Override
 	public String toString() {
 		return type + "(" + start + "," + end + "): " + getText();
 	}
 	
 }
