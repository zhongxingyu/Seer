 /*
  * Copyright (c) 2011-2012, Tobi Vollebregt
  *
  * Licensed under the GNU Lesser General Public License, v2.1
  */
 package org.spoofax.interpreter.library.xml;
 
 import java.util.ArrayList;
 import java.util.Stack;
 
 import org.spoofax.interpreter.terms.IStrategoConstructor;
 import org.spoofax.interpreter.terms.IStrategoList;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.interpreter.terms.ITermFactory;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.helpers.DefaultHandler;
 
 /**
  * SAX ContentHandler which builds IStrategoTerms that represent the
  * XML document using the ITermFactory passed to the constructor.
  *
  * If the parser is namespace aware, then the resulting ATerm has the structure:
  *
  *  Element   : Name * List(Attribute) * List(Element) -> Element
  *  Attribute : Name * String -> Attribute
  *  Text      : String -> Element
 *  Name      : Option(String) * String
  *
  * If the parser is not namespace aware, then it has this structure:
  *
  *  Element   : String * List(Attribute) * List(Element) -> Element
  *  Attribute : String * String -> Attribute
  *  Text      : String -> Element
  *
  * For text-content-only elements, Text nodes are included if the
  * http://spoofax.org/sax/features/character-data feature is enabled.
  *
  * For mixed-content elements, Text nodes are included if the
  * http://spoofax.org/sax/features/mixed-content feature is enabled.
  *
  * @author Tobi Vollebregt
  */
 public class StrategoTermBuilder extends DefaultHandler {
 
 	private static class Element {
 		public final String uri;
 
 		public final String localName;
 
 		public final String qName;
 
 		public final IStrategoList attributes;
 
 		public final ArrayList<IStrategoTerm> childs;
 
 		public Element(String uri, String localName, String qName, IStrategoList attributes) {
 			this.uri = uri;
 			this.localName = localName;
 			this.qName = qName;
 			this.attributes = attributes;
 			this.childs = new ArrayList<IStrategoTerm>();
 		}
 	}
 
 	private final IStrategoConstructor elementCons;
 
 	private final IStrategoConstructor attributeCons;
 
 	private final IStrategoConstructor textCons;
 
 	private final IStrategoConstructor nameCons;
 
 	private final IStrategoConstructor noneCons;
 
	private final IStrategoConstructor someCons;

 	private final ITermFactory factory;
 
 	private final Stack<Element> stack = new Stack<Element>();
 
 	private final StringBuilder textBuilder = new StringBuilder();
 
 	private final ArrayList<SAXParseException> errors = new ArrayList<SAXParseException>();
 
 	private final boolean allowMixedContent;
 
 	private final boolean allowTextContent;
 
 	private final boolean namespaceAware;
 
 	public StrategoTermBuilder(ITermFactory factory, XMLLibrary library) {
 		this.factory = factory;
 
 		elementCons = factory.makeConstructor("Element", 3);
 		attributeCons = factory.makeConstructor("Attribute", 2);
 		textCons = factory.makeConstructor("Text", 1);
 		nameCons = factory.makeConstructor("Name", 2);
 		noneCons = factory.makeConstructor("None", 0);
		someCons = factory.makeConstructor("Some", 1);
 
 		allowMixedContent = library.getAllowMixedContent();
 		allowTextContent = library.getAllowCharacterContent();
 		namespaceAware = library.obtainParser().isNamespaceAware();
 
 		// This dummy element collects the root element.
 		stack.push(new Element(null, null, null, null));
 	}
 
 	public IStrategoTerm getRootElement() {
 		return stack.peek().childs.get(0);
 	}
 
 	public ArrayList<SAXParseException> getErrors() {
 		return errors;
 	}
 
 	@Override
 	public void startElement(String uri, String localName, String qName, Attributes attributes) {
 		// Store text parsed before this start tag if mixed content is allowed.
 		if (allowMixedContent && textBuilder.length() > 0) {
 			Element parent = stack.peek();
 			IStrategoTerm contents = factory.makeString(textBuilder.toString());
 			IStrategoTerm textNode = factory.makeAppl(textCons, contents);
 
 			parent.childs.add(textNode);
 		}
 
 		textBuilder.setLength(0);
 		stack.push(new Element(uri, localName, qName, attributesToStrategoList(attributes)));
 	}
 
 	private IStrategoTerm makeName(String uri, String localName, String qName)
 	{
 		if (namespaceAware) {
 			boolean uriIsEmpty = uri.length() == 0;
			IStrategoTerm uriTerm = uriIsEmpty ? factory.makeAppl(noneCons) : factory.makeAppl(someCons, factory.makeString(uri));
 			IStrategoTerm localNameTerm = factory.makeString(uriIsEmpty ? qName : localName);
 			return factory.makeAppl(nameCons, uriTerm, localNameTerm);
 		}
 		else {
 			return factory.makeString(qName);
 		}
 	}
 
 	private IStrategoTerm makeName(Element element)
 	{
 		return makeName(element.uri, element.localName, element.qName);
 	}
 
 	private IStrategoTerm makeName(Attributes attributes, int index)
 	{
 		return makeName(attributes.getURI(index), attributes.getLocalName(index), attributes.getQName(index));
 	}
 
 	private IStrategoList attributesToStrategoList(Attributes attributes) {
 		final int sz = attributes.getLength();
 		final IStrategoTerm[] strategoAttributes = new IStrategoTerm[sz];
 		for (int i = 0; i < sz; ++i) {
 			IStrategoTerm name = makeName(attributes, i);
 			IStrategoTerm value = factory.makeString(attributes.getValue(i));
 			strategoAttributes[i] = factory.makeAppl(attributeCons, name, value);
 		}
 		return factory.makeList(strategoAttributes);
 	}
 
 	@Override
 	public void endElement(String uri, String localName, String qName) {
 		Element element = stack.pop();
 		Element parent = stack.peek();
 
 		// Store text parsed before this end tag if either mixed content is allowed
 		// or text-only content is allowed and the element has no children.
 		if ((allowMixedContent || (allowTextContent && element.childs.isEmpty())) && textBuilder.length() > 0) {
 			IStrategoTerm contents = factory.makeString(textBuilder.toString());
 			IStrategoTerm textNode = factory.makeAppl(textCons, contents);
 
 			element.childs.add(textNode);
 		}
 
 		textBuilder.setLength(0);
 		parent.childs.add(elementToStrategoTerm(element));
 	}
 
 	private IStrategoTerm elementToStrategoTerm(Element element) {
 		IStrategoTerm name = makeName(element);
 		IStrategoTerm childs = childsToStrategoList(element.childs);
 
 		return factory.makeAppl(elementCons, name, element.attributes, childs);
 	}
 
 	private IStrategoTerm childsToStrategoList(ArrayList<IStrategoTerm> childs) {
 		IStrategoTerm[] strategoChilds = new IStrategoTerm[childs.size()];
 		childs.toArray(strategoChilds);
 		return factory.makeList(strategoChilds);
 	}
 
 	@Override
 	public void characters(char[] ch, int start, int length) {
 		if (allowMixedContent || allowTextContent) {
 			textBuilder.append(ch, start, length);
 		}
 	}
 
 	@Override
 	public void ignorableWhitespace(char[] ch, int start, int length) {
 		characters(ch, start, length);
 	}
 
 	@Override
 	public void warning(SAXParseException pe) {
 		errors.add(pe);
 	}
 
 	@Override
 	public void error(SAXParseException pe) {
 		errors.add(pe);
 	}
 
 }
