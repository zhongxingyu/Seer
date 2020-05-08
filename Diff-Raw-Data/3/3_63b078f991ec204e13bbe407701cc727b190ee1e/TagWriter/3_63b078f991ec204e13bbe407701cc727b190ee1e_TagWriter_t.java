 package org.icemobile.jsp.tags;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.Stack;
 
 import javax.servlet.jsp.PageContext;
 
 public class TagWriter {
 	
 	private Writer out;
 	private Stack<String> elementStack = new Stack<String>();
 	private boolean lastElementEnded = true;
 	
 	private static final String SPACE = " ";
 	private static final String LT = "<";
 	private static final String GT = ">";
 	private static final String SQ = "=\"";
 	private static final String EM = "";
 	private static final String EQ = "\" ";
 	private static final String ET = "</";
 	private static final String DISABLED = "disabled";
 	private static final String STYLE = "style";
 	private static final String CLASS = "class";
 	private static final String DIV = "div";
 	private static final String SPAN = "span";
 	
 	/**
 	 * Create an instance of the TagWriter class. For use with tags that
 	 * do not require an initial element context, such as simple tags 
 	 * with no tag body content.
 	 * @param pageContext The JSP context
 	 */
 	public TagWriter(PageContext pageContext){
 		this.out = pageContext.getOut();
 	}
 	
 	/**
 	 * Create an instance of the TagWriter class. For use with tags that
 	 * use both doStartTag() and doEndTag() methods, and require an 
 	 * initial element context for the doEndTag() method.
 	 * @param pageContext The JSP context
 	 * @param elementContext The stack of starting elements.
 	 */
 	public TagWriter(PageContext pageContext, Stack<String> elementContext){
 		this(pageContext);
 		elementStack = elementContext;
 	}
 	
 	public void writeAttribute(String name, Object value) throws IOException{
 		 out.write(SPACE);
 		 out.write(name);
 		 out.write(SQ);
 		 out.write(value != null ? value.toString() : EM);
 		 out.write(EQ);
 	}
 	
 	public void writeAttribute(String name, boolean value) throws IOException{
 		out.write(SPACE);
 		out.write(name);
 		out.write(SQ);
 		out.write(Boolean.toString(value));
 		out.write(EQ);
 	}
 	
 	public void writeAttribute(String name, int value) throws IOException{
 		out.write(SPACE);
 		out.write(name);
 		out.write(SQ);
 		out.write(value);
 		out.write(EQ);
 	}
 	
 	public void startElement(String name) throws IOException{
 		if( !elementStack.isEmpty() && !lastElementEnded){
 			out.write(GT);
 		}
 		out.write(LT);
 		out.write(name);
 		lastElementEnded = false;
 		elementStack.push(name);
 	}
 	
 	public void endElement() throws IOException{
		if( !lastElementEnded){
			out.write(GT);
		}
 		out.write(ET);
 		out.write(elementStack.pop());
 		out.write(GT);
 		lastElementEnded = true;
 	}
 	
 	public void startSpan() throws IOException{
 		startElement(SPAN);
 	}
 	
 	public void startDiv() throws IOException{
 		startElement(DIV);
 	}
 	
 	public void writeDisabled(boolean disabled) throws IOException{
 		if( disabled ){
 			writeAttribute(DISABLED, disabled);
 		}
 	}
 	
 	public void writeStyle(String style) throws IOException{
 		if( style != null ){
 			writeAttribute(STYLE, style);
 		}
 	}
 	
 	public void writeStyleClassWithBase(String styleClass, String baseCSSClass) throws IOException{
 		if( styleClass != null ){
 			writeAttribute(CLASS, baseCSSClass + SPACE + styleClass);
 		}
 		else{
 			writeAttribute(CLASS, baseCSSClass);
 		}	
 	}
 	
 	public void writeStyleClass(String styleClass) throws IOException{
 		if( styleClass != null ){
 			writeAttribute(CLASS, styleClass);
 		}
 		else{
 			writeAttribute(CLASS, EM);
 		}	
 	}
 	
 	public void writeTextNode(String text) throws IOException{
 		if( !lastElementEnded ){
 			out.write(GT);
 		}
 		out.write(text);
 		lastElementEnded = true;
 	}
 	
 	
 
 }
