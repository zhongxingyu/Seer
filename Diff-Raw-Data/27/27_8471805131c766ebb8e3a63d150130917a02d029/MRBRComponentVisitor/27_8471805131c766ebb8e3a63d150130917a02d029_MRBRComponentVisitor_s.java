 package us.northdeck.xsomtest;
 
 
 
 import java.util.Stack;
 
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 
 import com.sun.xml.xsom.XSAnnotation;
 import com.sun.xml.xsom.XSAttGroupDecl;
 import com.sun.xml.xsom.XSAttributeDecl;
 import com.sun.xml.xsom.XSAttributeUse;
 import com.sun.xml.xsom.XSComplexType;
 import com.sun.xml.xsom.XSComponent;
 import com.sun.xml.xsom.XSContentType;
 import com.sun.xml.xsom.XSElementDecl;
 import com.sun.xml.xsom.XSFacet;
 import com.sun.xml.xsom.XSIdentityConstraint;
 import com.sun.xml.xsom.XSModelGroup;
 import com.sun.xml.xsom.XSModelGroup.Compositor;
 
 import com.sun.xml.xsom.XSModelGroupDecl;
 import com.sun.xml.xsom.XSNotation;
 import com.sun.xml.xsom.XSParticle;
 import com.sun.xml.xsom.XSSchema;
 import com.sun.xml.xsom.XSSimpleType;
 import com.sun.xml.xsom.XSType;
 import com.sun.xml.xsom.XSWildcard;
 import com.sun.xml.xsom.XSXPath;
 import com.sun.xml.xsom.visitor.XSVisitor;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 public class MRBRComponentVisitor implements XSVisitor {
 	private Document document;
 
 
 	private MRBRSchema doc;
 	private Document bxrDoc;
 	private Integer indentLevel;
 	private Element table;
 	//private Element currentEl;
 	private Stack<Element> elStack;
 	private Stack<String> pathStack;
 	private Stack<Integer> choiceStack;
 	public String MAX_LENGTH = "maxLength";
 	public String MIN_LENGTH = "minLength";
 	public String ENUMERATION = "ennumeration";
 	public String PATTERN = "pattern";
 	private Cardinality card;
 	private boolean inChoice = false;
 	private int choiceNumber = 0;
 	private Integer choiceOption = 1;
 	
 	class AnyTypeRestrictionException extends Exception {
 		private static final long serialVersionUID = -1357312370127919817L;}
 	
 	class Cardinality {
 		int min;
 		int max;
 		String desc;
 		
 		public Cardinality(int min, int max) {
 			desc = new String();
 			desc = "?";
 			this.min = min;
 			this.max = max;
 			if (min == 0) {
 				if (max == 1) {
 					desc = "0..1";
 				} 
 				if (max == -1) {
 					desc = "0..*";
 				}
 			}
 			if (min == 1) {
 				if (max == 1) {
 					desc = "1..1";
 				}
 				if (max == -1) {
 					desc = "1..*";
 				}
 			}
 		}
 	}
 
 	public void initDoc() throws ParserConfigurationException {
 		this.document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
 		this.bxrDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
 		
 		doc = new MRBRSchema(document);
 		table = doc.createElement("schema");
 		table.setPrefix("xs");
 		
 		
 		document.appendChild(table);
 		
 		elStack = new Stack<Element>();
 		elStack.push(table);
 		
 		choiceStack = new Stack<Integer>();
 		
 		this.indentLevel = 0;
 		
 		pathStack = new Stack<String>();
 		
		Element bxrRoot = this.bxrDoc.createElementNS("bxr", "appinfo");
 		bxrRoot.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
 		bxrRoot.setAttribute("xsi:schemaLocation", "com.bestbuy.bbym.bxr ../XSD/SimpleRulesSyntax.xsd");
		bxrRoot.setAttribute("xmlns:bxr", "com.bestbuy.bbym.bxr");
 		bxrRoot.setAttribute("xmlns", "com.bestbuy.bbym.bxr");
 		this.bxrDoc.appendChild(bxrRoot);
 		
 	}
 
 	public Document getDocument() {
 		return document;
 	}
 	
 	
 	
 	private static void print(String s) {
 		System.out.println(s);
 	}
 	
 	public void process(XSComponent c) {
 		c.visit(this);
 	}
 	
 	@Override
 	public void wildcard(XSWildcard wc) {
 		Element any = doc.createElement("any");
 		any.setAttribute("namespace", "##any");
 		any.setAttribute("minOccurs", "0");
 		any.setAttribute("maxOccurs", "unbounded");
 		any.setAttribute("processContents", "skip");
 		elStack.push(any);
 	}
 
 	@Override
 	public void modelGroupDecl(XSModelGroupDecl decl) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void modelGroup(XSModelGroup group) {
 		
 		Compositor compositor = group.getCompositor();
 		if (compositor == XSModelGroup.CHOICE) {
 			incrementChoiceStack();
 			choiceOption = 1;
 		}
 			//this.inChoice = true;
 		print(compositor.toString());
 		Element c = doc.createElement(compositor.toString());
 		elStack.push(c);
 		for ( XSParticle p : group.getChildren() ){
 			if (compositor == XSModelGroup.CHOICE) this.inChoice = true;
 			setCardinality(p);
 			p.visit(this);
 			if (compositor == XSModelGroup.CHOICE) choiceOption++;
 		}
 		if (compositor == XSModelGroup.CHOICE) choiceStack.pop();
 	}
 
 	private void incrementChoiceStack() {
 		choiceNumber++;
 		choiceStack.push(choiceNumber);
 		
 	}
 
 	private void setCardinality(XSParticle p) {
 		int maxOccurs = p.getMaxOccurs().intValue();
 		int minOccurs = p.getMinOccurs().intValue();
 		this.card = new Cardinality(minOccurs, maxOccurs);		
 	}
 
 	@Override
 	public void elementDecl(XSElementDecl decl) {
 		String name = decl.getName();
 		elStack.push(doc.createElement("element"));
 		elStack.peek().setAttribute("name", name);
 		
 		//maintain xpath stack
 		pathStack.push(name);
 		
 		//add bxr location
 		addBxrLocation();
 		
 		if (document.getElementsByTagName("element").getLength() == 0) {
 			document.getDocumentElement().appendChild(elStack.peek());
 		}
 		decl.getType().visit(this);
 		
 		pathStack.pop(); //move up the xpath tree		
 		
 		
 	}
 
 	private void addBxrLocation() {
 		String xpath = "";
 		for (String s  : this.pathStack) {
 			xpath = xpath + "/" + s;
 		}
 		
		Element el = this.bxrDoc.createElementNS("bxr", "location");
 		el.setAttribute("path", xpath );
 		if (this.card != null) {
 			el.setAttribute("cardinality", this.card.desc);
 		}
 		if (!choiceStack.empty()  ) {
 			el.setAttribute("choice", choiceStack.peek().toString());
 			//el.setAttribute("option", this.choiceOption.toString());
 			if (this.inChoice) {
 				el.setAttribute("firstEl", "true");
 				this.inChoice = false;
 			}
 			
 		}
 		this.bxrDoc.getDocumentElement().appendChild( el);
 		
 	}
 
 	@Override
 	public void simpleType(XSSimpleType simpleType)  {
 		this.closeCurrentEl();
 		try {
 			if (simpleType.getBaseType().getName() == "anySimpleType")
 				throw new AnyTypeRestrictionException();
 			Element e = doc.createElement("simpleType");
 			elStack.push(e);
 	
 			switch (simpleType.getDerivationMethod()) {
 
 			case XSType.RESTRICTION:
 				Element e1 = doc.createElement("restriction");
 				e1.setAttribute("base", "xs:"
 						+ simpleType.getBaseType().getName());
 				elStack.push(e1);
 				break;
 
 			}
 			for (XSFacet facet : simpleType.asRestriction().getDeclaredFacets()) {
 				Element facetEl = doc.createElement(facet.getName());
 				facetEl.setAttribute("value", facet.getValue().value);
 				elStack.peek().appendChild(facetEl);
 			}
 			Element restriction = elStack.pop();
 			elStack.peek().appendChild(restriction);
 			Element simpleTypeEl = elStack.pop();
 			Element el = elStack.peek();
 			el.appendChild(simpleTypeEl);
 		} catch (AnyTypeRestrictionException e) {
 			elStack.peek().setAttribute("type", "xs:anySimpleType");
 		}
 	}
 
 	@Override
 	public void particle(XSParticle p) {
 		p.getTerm().visit(this);
 		addOccurs(p);
 		Element el = elStack.pop();
 		elStack.peek().appendChild(el);
 		
 	}
 
 	/**
 	 * Add min and max occurrence attributes if not the default "1" value
 	 * @param p XSParticle of the element on the top of the elStack stack
 	 */
 	private void addOccurs(XSParticle p) {
 		int maxOccurs = p.getMaxOccurs().intValue();
 		int minOccurs = p.getMinOccurs().intValue();
 		
 		if (maxOccurs != 1) {
 			if (maxOccurs == -1) {
 				elStack.peek().setAttribute("maxOccurs", "unbounded");
 			} else {
 				elStack.peek().setAttribute("maxOccurs", p.getMaxOccurs().toString());
 			}
 		}
 		
 		if (minOccurs != 1) {
 			
 			elStack.peek().setAttribute("minOccurs", p.getMinOccurs().toString());
 		}
 		
 	}
 
 	@Override
 	public void empty(XSContentType empty) {
 	}
 
 	@Override
 	public void annotation(XSAnnotation ann) {
 		ann.toString();
 	}
 
 	@Override
 	public void attGroupDecl(XSAttGroupDecl decl) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void attributeDecl(XSAttributeDecl decl) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void attributeUse(XSAttributeUse use) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void complexType(XSComplexType type) {
 		closeCurrentEl();
 		indentLevel++;
 		print(type.getName());
 		//currentEl = doc.createElement("complexType");
 		//currentEl.setAttribute("name", value)
 		elStack.push(doc.createElement("complexType"));
 		elStack.peek().appendChild(document.createComment(type.getTargetNamespace()+":"+type.getName()));
 		type.getContentType().visit(this);
 		Element el = elStack.pop();
 		elStack.peek().appendChild(el);
 	}
 
 	private void closeCurrentEl() {
 	}
 
 	@Override
 	public void schema(XSSchema schema) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void facet(XSFacet facet) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void notation(XSNotation notation) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void identityConstraint(XSIdentityConstraint decl) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void xpath(XSXPath xp) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public Document getPaths() {
 		return this.bxrDoc;
 	}
 
 }
