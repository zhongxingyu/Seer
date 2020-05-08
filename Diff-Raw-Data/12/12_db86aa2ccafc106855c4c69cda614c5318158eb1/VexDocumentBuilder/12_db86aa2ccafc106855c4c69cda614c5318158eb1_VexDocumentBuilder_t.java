 /*******************************************************************************
  * Copyright (c) 2011 Florian Thienel and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * 		Florian Thienel - initial API and implementation
  *******************************************************************************/
 package ft.vex.wikitext;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Stack;
 
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.mylyn.wikitext.core.parser.Attributes;
 import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder;
 import org.eclipse.vex.core.internal.dom.Content;
 import org.eclipse.vex.core.internal.dom.Document;
 import org.eclipse.vex.core.internal.dom.Element;
 import org.eclipse.vex.core.internal.dom.GapContent;
 import org.eclipse.vex.core.internal.dom.RootElement;
 
 /**
  * @author Florian Thienel
  */
 public class VexDocumentBuilder extends DocumentBuilder {
 
 	public static final String WIKITEXT_NAMESPACE = null;
 
 	public static final QualifiedName ROOT = new QualifiedName(WIKITEXT_NAMESPACE, "wikitext");
 	public static final QualifiedName HEADING = new QualifiedName(WIKITEXT_NAMESPACE, "heading");
 	public static final QualifiedName ENTITY = new QualifiedName(WIKITEXT_NAMESPACE, "entity");
 	public static final QualifiedName IMAGE = new QualifiedName(WIKITEXT_NAMESPACE, "image");
 	public static final QualifiedName LINK = new QualifiedName(WIKITEXT_NAMESPACE, "link");
 	public static final QualifiedName ACRONYM = new QualifiedName(WIKITEXT_NAMESPACE, "acronym");
 	public static final QualifiedName LINEBREAK = new QualifiedName(WIKITEXT_NAMESPACE, "linebreak");
 	public static final QualifiedName UNESCAPED = new QualifiedName(WIKITEXT_NAMESPACE, "unescaped");
 
 	public static final Map<BlockType, QualifiedName> BLOCK_ELEMENTS = new HashMap<BlockType, QualifiedName>();
 	static {
 		for (BlockType blockType : BlockType.values())
 			BLOCK_ELEMENTS.put(blockType, new QualifiedName(WIKITEXT_NAMESPACE, blockType.name().replaceAll("_", "")));
 	}
 
 	public static final Map<SpanType, QualifiedName> SPAN_ELEMENTS = new HashMap<SpanType, QualifiedName>();
 	static {
 		for (SpanType spanType : SpanType.values())
 			SPAN_ELEMENTS.put(spanType, new QualifiedName(WIKITEXT_NAMESPACE, spanType.name().replaceAll("_", "")));
 	}
 
 	private Document document;
 
 	private Content content = new GapContent(100);
 
 	private Stack<StackEntry> currentElement = new Stack<StackEntry>();
 	
 	private static class StackEntry {
 		public final Element element;
 		public final int offset;
 		
 		public StackEntry(final Element element, final int offset) {
 			this.element = element;
 			this.offset = offset;
 		}
 	}
 
 	public Document getDocument() {
 		return document;
 	}
 
 	@Override
 	public void beginDocument() {
 		final RootElement rootElement = new RootElement(ROOT);
 		currentElement.push(new StackEntry(rootElement, content.getLength()));
 		content.insertString(content.getLength(), "\0");
 	}
 
 	@Override
 	public void endDocument() {
 		final StackEntry entry = currentElement.pop();
 		final RootElement rootElement = (RootElement) entry.element;
 		content.insertString(content.getLength(), "\0");
 		rootElement.setContent(content, entry.offset, content.getLength() - 1);
 		content.insertString(content.getLength(), "\0");
 		document = new Document(content, rootElement);
 		rootElement.setDocument(document);
 	}
 
 	@Override
 	public void beginBlock(BlockType type, Attributes attributes) {
 		final Element element = new Element(BLOCK_ELEMENTS.get(type));
 		startElement(element, attributes);
 	}
 
 	private void startElement(final Element element, Attributes attributes) {
 		currentElement.peek().element.addChild(element);
 		currentElement.push(new StackEntry(element, content.getLength()));
 		element.setAttribute("id", attributes.getId());
 		element.setAttribute("title", attributes.getTitle());
 		element.setAttribute("language", attributes.getLanguage());
 		element.setAttribute("class", attributes.getCssClass());
 		element.setAttribute("style", attributes.getCssStyle());
 		content.insertString(content.getLength(), "\0");
 	}
 
 	@Override
 	public void endBlock() {
 		endElement();
 	}
 
 	private void endElement() {
 		final StackEntry entry = currentElement.pop();
 		content.insertString(content.getLength(), "\0");
 		entry.element.setContent(content, entry.offset, content.getLength() - 1);
 	}
 
 	@Override
 	public void beginSpan(SpanType type, Attributes attributes) {
 		final Element element = new Element(SPAN_ELEMENTS.get(type));
 		startElement(element, attributes);
 	}
 
 	@Override
 	public void endSpan() {
 		endElement();
 	}
 
 	@Override
 	public void beginHeading(int level, Attributes attributes) {
 		final Element element = new Element(HEADING);
 		element.setAttribute("level", Integer.toString(level));
 		startElement(element, attributes);
 	}
 
 	@Override
 	public void endHeading() {
 		endElement();
 	}
 
 	@Override
 	public void characters(String text) {
 		content.insertString(content.getLength(), text);
 	}
 
 	@Override
 	public void entityReference(String entity) {
 		final Element element = new Element(ENTITY);
 		element.setAttribute("entity", entity);
 		startElement(element, new Attributes());
 		endElement();
 	}
 
 	@Override
 	public void image(Attributes attributes, String url) {
 		final Element element = new Element(IMAGE);
 		element.setAttribute("url", url);
 		startElement(element, attributes);
 		endElement();
 	}
 
 	@Override
 	public void link(Attributes attributes, String hrefOrHashName, String text) {
 		final Element element = new Element(LINK);
 		element.setAttribute("href", hrefOrHashName);
 		startElement(element, attributes);
 		
 		characters(text);
 		
 		endElement();
 	}
 
 	@Override
 	public void imageLink(Attributes linkAttributes, Attributes imageAttributes, String href, String imageUrl) {
 		final Element linkElement = new Element(LINK);
 		linkElement.setAttribute("href", href);
 		startElement(linkElement, linkAttributes);
 		
 		image(imageAttributes, imageUrl);
 		
 		endElement();
 	}
 
 	@Override
 	public void acronym(String text, String definition) {
 		final Element element = new Element(ACRONYM);
 		element.setAttribute("definition", definition);
 		startElement(element, new Attributes());
 		
 		characters(text);
 		
 		endElement();
 	}
 
 	@Override
 	public void lineBreak() {
 		final Element element = new Element(LINEBREAK);
 		startElement(element, new Attributes());
 		endElement();
 	}
 
 	@Override
 	public void charactersUnescaped(String literal) {
 		final Element element = new Element(UNESCAPED);
 		startElement(element, new Attributes());
 		
 		characters(literal);
 		
 		endElement();
 	}
 
 }
