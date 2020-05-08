 package com.nacre.service;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.lang.ObjectUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.WordUtils;
 import org.apache.commons.lang.math.NumberUtils;
 import org.xml.sax.Attributes;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.EntityResolver;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.InputSource;
 import org.xml.sax.Locator;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import com.nacre.service.vo.ComplexType;
 import com.nacre.service.vo.Decoration;
 import com.nacre.service.vo.Field;
 import com.nacre.service.vo.SimpleType;
 import com.nacre.service.vo.ComplexType.CombinationType;
 import com.sun.xml.xsom.XSAnnotation;
 import com.sun.xml.xsom.XSAttGroupDecl;
 import com.sun.xml.xsom.XSAttributeDecl;
 import com.sun.xml.xsom.XSAttributeUse;
 import com.sun.xml.xsom.XSComplexType;
 import com.sun.xml.xsom.XSContentType;
 import com.sun.xml.xsom.XSElementDecl;
 import com.sun.xml.xsom.XSFacet;
 import com.sun.xml.xsom.XSIdentityConstraint;
 import com.sun.xml.xsom.XSModelGroup;
 import com.sun.xml.xsom.XSModelGroupDecl;
 import com.sun.xml.xsom.XSNotation;
 import com.sun.xml.xsom.XSParticle;
 import com.sun.xml.xsom.XSRestrictionSimpleType;
 import com.sun.xml.xsom.XSSchema;
 import com.sun.xml.xsom.XSSimpleType;
 import com.sun.xml.xsom.XSWildcard;
 import com.sun.xml.xsom.XSXPath;
 import com.sun.xml.xsom.XmlString;
 import com.sun.xml.xsom.parser.AnnotationContext;
 import com.sun.xml.xsom.parser.AnnotationParser;
 import com.sun.xml.xsom.parser.AnnotationParserFactory;
 import com.sun.xml.xsom.visitor.XSFunction;
 
 public class ParsingUtils {
 	public static EntityResolver nacreXSOMEntityResolver = new EntityResolver() {
 		public InputSource resolveEntity(String publicId, String systemId)
 				throws SAXException, IOException {
 			System.out.println("looking for sysid: [" + systemId + "]");
 			try {
 				return new InputSource(new FileReader(new File(new URI(systemId))));
 			} catch (URISyntaxException e) {
 				System.err.println("failed to locate URI: " + systemId);
 				return null;
 			}
 		}
 	};
 	public static ErrorHandler nacreXSOMErrorHandler = new ErrorHandler() {
 		public void error(SAXParseException exception) throws SAXException {
 			log(exception);
 		}
 		public void fatalError(SAXParseException exception)
 				throws SAXException {
 			log(exception);			
 		}
 		public void warning(SAXParseException exception)
 				throws SAXException {
 			log(exception);
 		}
 		private void log(SAXParseException exception) {
 			System.out.println("Error in " + exception.getPublicId() + " at " +
 					exception.getLineNumber() + ":" + exception.getColumnNumber());
 			System.out.println(exception.getMessage());
 			System.out.println(exception.getCause());
 			exception.printStackTrace();
 		}
 	};
 	public static AnnotationParserFactory nacreXSOMAnnotationParserFactory = new AnnotationParserFactory() {
 		public AnnotationParser create() {
 			return nacreXSOMAnnotationParser;
 		}
 	};
 	
 	private static AnnotationParser nacreXSOMAnnotationParser = new AnnotationParser() {
 		Map<String, Decoration> decorations = new HashMap<String,Decoration>();
 		String locatorKey;
 		@Override
 		public ContentHandler getContentHandler(
 				AnnotationContext ctx, String parent,
 				ErrorHandler errorHandler, EntityResolver entityResolver) {
 			System.out.println("got an annotation, parent [" + parent + "] type [" + ctx.toString() + "]");
 			return new DefaultHandler() {
 				public void setDocumentLocator(Locator locator) {
 					System.out.println("Annotation location: " + locator.getLineNumber() + ":" + locator.getColumnNumber());
 					locatorKey = locatorKey(locator);
 				}
 
 				public void startElement(String uri,
 						String localName, String qName,
 						Attributes atts) throws SAXException {
 					System.out.println("Got annotation for " + localName + " qname " + qName);
 					if (uri.equals("http://www.nacre.com/decorations")) {
 						if (localName.equals("label")) {
 							Decoration decoration = new Decoration();
 							decoration.setLabel(atts.getValue("label"));
 							decorations.put(locatorKey, decoration);
 						}
 					}
 				}
 			}; 				
 		}
 
 		@Override
 		public Object getResult(Object o) {
 			return decorations;
 		}
 		
 	};
 	
 	public static String locatorKey(Locator l) {
 		return l.getPublicId() + ":" + l.getLineNumber() + ":" + l.getColumnNumber();
 	}
 
 	public static XSFunction<Field> nacreXSOMFunction = new XSFunction<Field>() {
 	
 		public Field annotation(XSAnnotation arg0) {
 			System.out.println("Annotation?");
 			// TODO Auto-generated method stub
 			return null;
 		}
 	
 		public Field attGroupDecl(XSAttGroupDecl arg0) {
 			System.out.println("Attr group decl?");
 			// TODO Auto-generated method stub
 			return null;
 		}
 	
 		public Field attributeDecl(XSAttributeDecl arg0) {
 			System.out.println("Attr decl?");
 			SimpleType st;
 			if (arg0.getType() == null || arg0.getType().getName() == null) {
 				System.out.println("attr type is null");
 				st = (SimpleType) arg0.getType().apply(nacreXSOMFunction);
 			} else {
 				st = new SimpleType();
 				st.setBaseType(arg0.getType().getName());
 				System.out.println("attr type is there " + st.getName() + ", " + st.getBaseType());
 			}
 			st.setName(arg0.getName());
 			return st;
 		}
 	
 		public Field attributeUse(XSAttributeUse arg0) {
 			System.out.println("Attr use?");
 			SimpleType st = (SimpleType) arg0.getDecl().apply(nacreXSOMFunction);
 			st.setDefault(ObjectUtils.defaultIfNull(arg0.getDefaultValue(),"").toString());
 			st.setFixed(ObjectUtils.defaultIfNull(arg0.getFixedValue(),"").toString());
 			st.setAttribute(true);
 			return st;
 		}
 	
 		public Field particle(XSParticle particle) {
 			System.out.println("query returned particle");
 			Field vo = particle.getTerm().apply(nacreXSOMFunction);
 			vo.setMaxOccurs(particle.getMaxOccurs());
 			vo.setMinOccurs(particle.getMinOccurs());
 			return vo;
 		}
 
 		public Field identityConstraint(XSIdentityConstraint arg0) {
 			System.out.println("id constraint?");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public Field facet(XSFacet arg0) {
 			System.out.println("Facet ?");
 			// TODO Auto-generated method stub
 			return null;
 		}
 	
 		public Field notation(XSNotation arg0) {
 			System.out.println("Note?");
 			// TODO Auto-generated method stub
 			return null;
 		}
 	
 		public Field schema(XSSchema arg0) {
 			System.out.println("Schema?");
 			// TODO Auto-generated method stub
 			return null;
 		}
 	
 		public Field xpath(XSXPath arg0) {
 			System.out.println("XPath?");
 			// TODO Auto-generated method stub
 			return null;
 		}
 	
 		public Field empty(XSContentType arg0) {
 			System.out.println("content type?");
 			// TODO Auto-generated method stub
 			return null;
 		}
 	
 		public Field simpleType(XSSimpleType type) {
 			System.out.println("simple type " + type.getName() + " is " + (type.isRestriction() ? "restriction" : "simple simple") + " base type " + type.isPrimitive());
 			SimpleType st;
 			if (type.isRestriction()) {
 				st = getRestriction(type.asRestriction());
 			} else {
 				st = new SimpleType();
 			}
 			st.setNamespace(type.getTargetNamespace());
 			st.setName(type.getName());
 			if (type.isPrimitive()) {
 				st.setBaseType(type.getPrimitiveType().getName());
 			}
 			return st;
 		}
 	
 		private SimpleType getRestriction(XSRestrictionSimpleType restriction) {
 			SimpleType field = new SimpleType();
 			field.setMaxLength(NumberUtils.createInteger(getFacet(restriction, XSFacet.FACET_MAXLENGTH)));
 			field.setMinLength(NumberUtils.createInteger(getFacet(restriction, XSFacet.FACET_MINLENGTH)));
 			field.setLength(NumberUtils.createInteger(getFacet(restriction, XSFacet.FACET_LENGTH)));
 	
 			field.setPattern(getFacet(restriction, XSFacet.FACET_PATTERN));
 	
 			field.setMaxExclusive(NumberUtils.createDouble(getFacet(restriction, XSFacet.FACET_MAXEXCLUSIVE)));
 			field.setMaxInclusive(NumberUtils.createDouble(getFacet(restriction, XSFacet.FACET_MAXINCLUSIVE)));
 			field.setMinExclusive(NumberUtils.createDouble(getFacet(restriction, XSFacet.FACET_MINEXCLUSIVE)));
 			field.setMinInclusive(NumberUtils.createDouble(getFacet(restriction, XSFacet.FACET_MININCLUSIVE)));
 	
 			field.setFractionDigits(NumberUtils.createInteger(getFacet(restriction, XSFacet.FACET_FRACTIONDIGITS)));
 			field.setTotalDigits(NumberUtils.createInteger(getFacet(restriction, XSFacet.FACET_TOTALDIGITS)));
 			
 			String ws = getFacet(restriction, XSFacet.FACET_WHITESPACE);
 			if (ws != null) {
 				field.setWhitespace(SimpleType.WhiteSpaceRestriction.valueOf(ws));
 			}
 	
 			if (restriction.getBaseType() != null) {
 				field.setBaseType(restriction.getBaseType().getName());
 			}
 			return field;
 		}
 	
 		private String getFacet(XSRestrictionSimpleType r, String name) {
 			XSFacet facet = r.getDeclaredFacet(name);
 			if (facet != null) {
 				XmlString xs = facet.getValue();
 				return xs.toString();
 			}
 			return null;
 		}
 		
 		@SuppressWarnings("unchecked")
 		public Field elementDecl(XSElementDecl elem) {
 			Field field;
 			System.out.println("\telem " + elem.getName() + " " + elem.getType().getName() + " ns: " + elem.getType().getTargetNamespace());
 			if (elem.getType().getName() == null) {
 				// anonymous restriction or complex type
 				System.out.println("type name is null, simple? " + elem.getType().isSimpleType() + ", complex? " + elem.getType().isComplexType());
 				field = elem.getType().isSimpleType() ?
 							elem.getType().asSimpleType().apply(nacreXSOMFunction) :
 							elem.getType().asComplexType().apply(nacreXSOMFunction);
 			} else if (!elem.getType().getTargetNamespace().equals(FormFactory.XSD_NAMESPACE)) {
 				// user-defined type
 				if (elem.getType().isComplexType()) {
 					field = elem.getType().asComplexType().apply(nacreXSOMFunction);
 				} else {
 					field = elem.getType().asSimpleType().apply(nacreXSOMFunction);
 				}
 			} else {
 				// simple type, no restriction
 				field = new SimpleType();
 				((SimpleType)field).setBaseType(elem.getType().getName());
 			}
 			com.sun.xml.xsom.XSAnnotation annotation = elem.getAnnotation();
 			if (annotation != null) {
 				System.out.println("adding decoration " + annotation.getLocator().getLineNumber() + ":" + annotation.getLocator().getColumnNumber());
 				field.setDecoration((Decoration) ((Map<String,Decoration>)annotation.getAnnotation()).get(locatorKey(annotation.getLocator())));
 				System.out.println("Added annot label " + field.getDecoration().getLabel());
 			} else {
 				// beautify field name if no label is set
 				// TODO this should be configurable via schema-level annotation, options for delimiter-base parsing as well
 				Decoration dec = new Decoration();
 				dec.setLabel(WordUtils.capitalize(StringUtils.join(
 						StringUtils.splitByCharacterTypeCamelCase(elem.getName()), " ")));
 				field.setDecoration(dec);
 			}
 			field.setName(elem.getName());
 			field.setNamespace(elem.getTargetNamespace());
 			if (field.getFieldType().equals(Field.FieldType.SimpleType)) {
 				((SimpleType)field).setDefault(ObjectUtils.defaultIfNull(elem.getDefaultValue(), "").toString());
 				((SimpleType)field).setFixed(ObjectUtils.defaultIfNull(elem.getFixedValue(), "").toString());
 			}
 			return field;
 		}
 	
 		public Field complexType(XSComplexType type) {
 			XSParticle particle = type.getContentType().asParticle();
 			Field vo;
 			if (particle != null) {
 				vo = particle.apply(nacreXSOMFunction);
 			} else {
 				// this is a simple type with attributes
 				vo = type.getContentType().asSimpleType().apply(nacreXSOMFunction);
 			}
 			for (XSAttributeUse attr : type.getAttributeUses()) {
				vo.getAttributes().add(attr.apply(nacreXSOMFunction));
 			}
 			vo.setName(type.getName());
 			
 			return vo;
 		}
 
 		public Field modelGroup(XSModelGroup group) {
 			ComplexType vo = new ComplexType();
 			System.out.println(group.getChildren().length + " children");
 			for (XSParticle child : group.getChildren()) {
 				System.out.println("child is " + child.getTerm());
 				Field field = child.apply(nacreXSOMFunction);
 				vo.getFields().add(field);
 			}
 			vo.setCombinationType(group.getCompositor().equals(XSModelGroup.CHOICE) ? CombinationType.or : CombinationType.and);
 			return vo;
 		}
 	
 		public Field modelGroupDecl(XSModelGroupDecl decl) {
 			return decl.getModelGroup().apply(nacreXSOMFunction);
 		}
 		public Field wildcard(XSWildcard arg0) {
 			System.out.println("wildcard?");
 			// TODO Auto-generated method stub
 			return null;
 		}
 	};
 	
 }
