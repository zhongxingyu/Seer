 /**
  * <copyright>
  *
  * Copyright (c) 2011 E.D.Willink and others.
  * All rights reserved.   This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   E.D. Willink - Initial API and implementation
  *
  * </copyright>
  *
  * $Id: PrettyPrintTypeVisitor.java,v 1.7 2011/05/22 21:06:19 ewillink Exp $
  */
 package org.eclipse.ocl.examples.pivot.prettyprint;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.ocl.examples.pivot.Element;
 import org.eclipse.ocl.examples.pivot.ExpressionInOCL;
 import org.eclipse.ocl.examples.pivot.Iteration;
 import org.eclipse.ocl.examples.pivot.NamedElement;
 import org.eclipse.ocl.examples.pivot.Namespace;
 import org.eclipse.ocl.examples.pivot.OCLExpression;
 import org.eclipse.ocl.examples.pivot.Operation;
 import org.eclipse.ocl.examples.pivot.Parameter;
 import org.eclipse.ocl.examples.pivot.PivotConstants;
 import org.eclipse.ocl.examples.pivot.PivotPackage;
 import org.eclipse.ocl.examples.pivot.Precedence;
 import org.eclipse.ocl.examples.pivot.TemplateBinding;
 import org.eclipse.ocl.examples.pivot.TemplateParameter;
 import org.eclipse.ocl.examples.pivot.TemplateParameterSubstitution;
 import org.eclipse.ocl.examples.pivot.TemplateSignature;
 import org.eclipse.ocl.examples.pivot.TemplateableElement;
 import org.eclipse.ocl.examples.pivot.Type;
 import org.eclipse.ocl.examples.pivot.TypedMultiplicityElement;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManager;
 import org.eclipse.ocl.examples.pivot.prettyprint.PrettyPrintOptions.Global;
 import org.eclipse.ocl.examples.pivot.util.AbstractVisitor;
 import org.eclipse.ocl.examples.pivot.util.Visitable;
 import org.eclipse.ocl.examples.pivot.utilities.PathElement;
 import org.eclipse.ocl.examples.pivot.utilities.PivotUtil;
 
 /**
  * The PrettyPrinter supports pretty printing.
  * PrettyPrintOptions may be used to configure the printing.
  */
 public class PrettyPrinter
 {
 	public static final String NULL_PLACEHOLDER = "<null>";
 	public static List<String> reservedNameList = Arrays.asList("and", "else", "endif", "false", "if", "implies", "in", "invalid", "let", "not", "null", "or", "self", "then", "true", "xor");
 	public static List<String> restrictedNameList = Arrays.asList("Bag", "Boolean", "Collection", "Integer", "OclAny", "OclInvalid", "OclVoid", "OrderedSet", "Real", "Sequence", "Set", "String", "Tuple", "UnlimitedNatural");
 
 	public static interface Factory
 	{
 		AbstractVisitor<Object, PrettyPrinter> createPrettyPrintVisitor(PrettyPrinter printer);
 	}
 	
 	private static Map<EPackage, Factory> factoryMap = new HashMap<EPackage, Factory>();
 	
 	public static void addFactory(EPackage ePackage, Factory factory) {
 		factoryMap.put(ePackage, factory);
 	}
 	
 	private static class Fragment
 	{
 		private final int depth;
 		private final String prefix;		// null for manditory continuation  of previous fragment
 		private final String text;
 		private final String suffix;
 		private Fragment parent = null;
 		private List<Fragment> children = null;
 		private boolean lineWrap = true;
 		private boolean exdented = false;
 		
 		public Fragment(Fragment parent, int depth, String prefix, String text, String suffix) {
 			this.parent = parent;
 			this.depth = depth;
 			this.prefix = prefix;
 			this.text = text;
 			this.suffix = suffix;
 		}
 		
 		public Fragment addChild(String prefix, String text, String suffix) {
 //			assert (prefix.length() + text.length() + suffix.length()) > 0;
 			if (children == null) {
 				children = new ArrayList<Fragment>();
 			}
 			Fragment child = new Fragment(this, depth+1, prefix, text, suffix);
 			children.add(child);
 			return child;
 		}
 		
 		public void configureLineWrapping(int spacesPerIndent, int lineLength) {
 			int firstColumn = depth * spacesPerIndent;
 			int lastColumn = firstColumn + text.length();
 			if (prefix != null) {
 				lastColumn += prefix.length();
 			}
 			if (suffix != null) {
 				lastColumn += suffix.length();
 			}
 			if (children != null) {
 				for (Fragment child : children) {
 					child.lineWrap = true;
 					child.configureLineWrapping(spacesPerIndent, lineLength);
 				}
 				int allChildrenLength = getChildrenLength(true);
 				if (lastColumn + allChildrenLength <= lineLength) {
 //					System.out.println(depth + " '" + prefix + "'+'" + text + "'+'" + suffix + "' "
 //							+ lastColumn + "+" + allChildrenLength + "<=" + lineLength);
 					for (Fragment child : children) {
 						child.lineWrap = false;
 					}
 				}
 				else {
 //					System.out.println(depth + " '" + prefix + "'+'" + text + "'+'" + suffix + "' "
 //							+ lastColumn + "+" + allChildrenLength + ">" + lineLength);
 //					int firstChildLength = getChildLength(0);
 //					if (lastColumn + allChildrenLength <= lineLength) {
 						for (Fragment child : children) {
 							child.lineWrap = child.exdented;
 						}
 //					}
 				}
 //				while (lastColumn < lineLength) {
 //					lastColumn = getChildrenLength(spacesPerIndent, lineLength, lastColumn);				
 //				}
 			}
 			else {
 //				System.out.println(depth + " '" + prefix + "'+'" + text + "'+'" + suffix + "' "
 //						+ lastColumn);
 			}
 			if (parent == null) {
 				lineWrap = false;
 			}
 		}
 
 		public int getChildrenLength(Boolean concatenate) {
 			int childrenLength = 0;
 			for (int iChild = 0; iChild < children.size(); iChild++) {
 				int childLength = getChildLength(iChild);
 				if (concatenate == Boolean.TRUE) {
 					childrenLength += childLength;
 				}
 				else if (childLength > childrenLength) {
 					childrenLength = childLength;
 				}
 			}
 			return childrenLength;
 		}
 
 		public int getChildLength(int iChild) {
 			Fragment child = children.get(iChild);
 			int childLength = child.length();
 			for (int jChild = iChild+1; jChild < children.size(); jChild++) {
 				Fragment nextChild = children.get(jChild);
 				if ((nextChild.prefix != null) && nextChild.lineWrap) {
 					break;
 				}
 				childLength += child.length();
 			}
 			return childLength;
 		}
 		
 		public int length() {
 			int length = text.length();
 			if (prefix != null) {
 				length += prefix.length();
 			}
 			if (suffix != null) {
 				length += suffix.length();
 			}
 			if (children != null) {
 				length += getChildrenLength(null);
 			}
 			return length;
 		}
 
 		public Fragment getParent() {
 			return parent;
 		}
 
 		@Override
 		public String toString() {
 			StringBuilder s = new StringBuilder();
 			toString(s, null, "  ");
 			return s.toString();
 		}
 		
 		public String toString(StringBuilder s, String newLine, String indent) {
 			if ((lineWrap || (newLine != null)) && (prefix != null)) {
 				if (lineWrap) {
 					newLine = "\n";
 				}
 				s.append(newLine);
 				if (text.length() > 0) {
 					if ((newLine != null) && newLine.equals("\n")) {
 						for (int i = 1; i < depth; i++) {
 							s.append(indent);
 						}
 					}
 					else {
 						s.append(prefix);
 					}
 				}
 				else if (prefix.length() > 0) {
 					s.append(prefix);
 				}
 			}
 			s.append(text);
 //			newLine = suffix != null ? lineWrap ? "\n" : suffix : null;
 			newLine = suffix;
 			if (children != null) {
 				for (Fragment child : children) {
 					newLine = child.toString(s, newLine, indent);
 				}				
 			}
 			return newLine;
 		}
 	}
 
 	public static PrettyPrinter createNamePrinter(Element element, PrettyPrintOptions options) {
 		return new PrettyPrinter(options, Mode.NAME, element);
 	}
 
 	public static PrettyPrinter createPrinter(Element element, PrettyPrintOptions options) {
 		return new PrettyPrinter(options, Mode.FULL, element);
 	}
 
 	public static Global createOptions(Namespace scope) {
 		PrettyPrintOptions.Global options = new PrettyPrintOptions.Global(scope);
 		options.addReservedNames(PrettyPrinter.reservedNameList);
 		options.addRestrictedNames(PrettyPrinter.reservedNameList);
		options.setUseParentheses(true);
 		return options;
 	}
 
 	public static String print(Element element) {
 		return print(element, createOptions(null));
 	}
 	public static String print(Element element, Namespace namespace) {
 		return print(element, createOptions(namespace));
 	}
 	public static String print(Element element, PrettyPrintOptions options) {
 		if (element == null) {
 			return NULL_PLACEHOLDER;
 		}
 		PrettyPrinter printer = new PrettyPrinter(options, Mode.FULL, element);
 		try {
 			printer.appendElement(element);
 			return printer.toString();
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			return printer.toString() + " ... " + e.getClass().getName() + " - " + e.getLocalizedMessage();
 		}
 	}
 
 	public static String printName(Element element) {
 		return printName(element, createOptions(null));
 	}
 	public static String printName(Element element, Namespace namespace) {
 		return printName(element, createOptions(namespace));
 	}
 	public static String printName(Element element, PrettyPrintOptions options) {
 		if (element == null) {
 			return NULL_PLACEHOLDER;
 		}
 		PrettyPrinter printer = createNamePrinter(element, options);
 		try {
 			printer.appendElement(element);
 			return printer.toString();
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			return printer.toString() + " ... " + e.getClass().getName() + " - " + e.getLocalizedMessage();
 		}
 	}
 
 	public static String printType(Element element) {
 		return printType(element, createOptions(null));
 	}
 	public static String printType(Element element, Namespace namespace) {
 		return printType(element, createOptions(namespace));
 	}
 	public static String printType(Element element, PrettyPrintOptions options) {
 		if (element == null) {
 			return NULL_PLACEHOLDER;
 		}
 		PrettyPrinter printer = new PrettyPrinter(options, Mode.TYPE, element);
 		try {
 			printer.appendElement(element);
 			return printer.toString();
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			return printer.toString() + " ... " + e.getClass().getName() + " - " + e.getLocalizedMessage();
 		}
 	}
 	
 	private enum Mode { TYPE, NAME, FULL };
 	
 	private final PrettyPrintOptions options;
 	private String pendingPrefix = "";
 	private StringBuilder pendingText;
 	protected Fragment fragment;
 	private Mode mode;
 	private final AbstractVisitor<Object, PrettyPrinter> visitor;
 	private Namespace scope;
 	private Precedence currentPrecedence = null;
 
 	/**
 	 * Initializes me.
 	 * @param element 
 	 */
 	private PrettyPrinter(PrettyPrintOptions options, Mode mode, Element element) {
 		this.options = options;
 		this.mode = mode;
 		this.scope = options.getScope();
 		pendingText = new StringBuilder();
 		fragment = new Fragment(null, 0, "", "", "");
 		EObject rootObject = EcoreUtil.getRootContainer(element);	// root is a dialect-dependent Model class.
 		EPackage rootPackage = rootObject.eClass().getEPackage();	// rootPackage is dialect-dependent EPackage.
 		Factory factory = factoryMap.get(rootPackage);
 		this.visitor = factory.createPrettyPrintVisitor(this);
 	}
 
 	public void append(Number number) {
 		if (number != null) {
 			append(number.toString());
 		}
 		else {
 			append(NULL_PLACEHOLDER);
 		}
 	}
 
 	protected void append(String string) {
 		if (string != null) {
 			pendingText.append(string);
 		}
 		else {
 			pendingText.append(NULL_PLACEHOLDER);
 		}
 	}
 
 	public void appendElement(Element element) {
 		visitor.safeVisit(element);
 	}
 
 	public void appendMultiplicity(int lower, int upper) {
 		PivotUtil.appendMultiplicity(pendingText, lower, upper);
 	}
 
 	public void appendName(NamedElement object) {
 		appendName(object, options.getRestrictedNames());
 	}
 		
 	public void appendName(NamedElement object, Set<String> keywords) {
 		append(getName(object, keywords));
 	}
 
 	public void appendParameters(Operation operation, boolean withNames) {
 		append("(");
 		String prefix = ""; //$NON-NLS-1$
 		if (operation instanceof Iteration) {
 			Iteration iteration = (Iteration)operation;
 			for (Parameter parameter : iteration.getOwnedIterator()) {
 				append(prefix);
 				if (withNames) {
 					appendName(parameter);
 					append(" : ");
 				}
 				appendTypedMultiplicity(parameter);
 				prefix = ", ";
 			}
 			if (iteration.getOwnedAccumulator().size() > 0) {
 				prefix = "; ";
 				for (Parameter parameter : iteration.getOwnedAccumulator()) {
 					if (withNames) {
 						appendName(parameter);
 						append(" : ");
 					}
 					append(prefix);
 					appendTypedMultiplicity(parameter);
 					prefix = ", ";
 				}
 			}
 			prefix = " | ";
 		}
 		for (Parameter parameter : operation.getOwnedParameter()) {
 			append(prefix);
 			if (withNames) {
 				appendName(parameter);
 				append(" : ");
 			}
 			appendTypedMultiplicity(parameter);
 			prefix = ", ";
 		}
 		append(")");
 	}
 
 	public void appendParent(EObject scope, Element element, String parentSeparator) { // FIXME Use appendQualifiedName instead
     	Mode savedMode = pushMode(Mode.TYPE);
     	try {
 			for (EObject eObject = scope; eObject != null; eObject = eObject.eContainer()) {
 				if (element == eObject) {
 					return;	
 				}
 			}
 	//		if (toString().length() >= MONIKER_OVERFLOW_LIMIT) {
 	//			append(OVERFLOW_MARKER);
 	//		}
 			if (element == null) {
 				append(NULL_PLACEHOLDER);	
 			}
 			else {
 	//			EObject parent = element.eContainer();
 				EObject unspecializedElement = element instanceof TemplateableElement ? ((TemplateableElement)element).getUnspecializedElement() : element;
 				EObject parent = PivotUtil.getNamespace((unspecializedElement != null ? unspecializedElement : element).eContainer());
 	            if (parent instanceof org.eclipse.ocl.examples.pivot.Package) {
 	                String name = ((org.eclipse.ocl.examples.pivot.Package)parent).getName();
 	                if (PivotConstants.ORPHANAGE_NAME.equals(name)) {
 	                    return;
 	                }
 	                if (PivotPackage.eNAME.equals(name)) {
 	                    return;
 	                }
 	                if ("ocl".equals(name)) {            // FIXME constant needed
 	                    return;
 	                }
 				}
 				if ((element instanceof Operation) &&
 					(parent instanceof Type) &&
 						PivotConstants.ORPHANAGE_NAME.equals(((Type)parent).getName())) {
 					Operation operation = (Operation)element;
 					append(operation.getOwningType().getName());
 					appendTemplateBindings(operation);
 					append(parentSeparator);
 					return;
 				}
 	            MetaModelManager metaModelManager = options.getGlobalOptions().getMetaModelManager();
 	            if ((metaModelManager != null) && (parent instanceof Type)) {
 	            	parent = (Namespace) metaModelManager.getPrimaryType((Type) parent);
 	            }
 				if (parent == scope) {
 					return;
 				}
 	            if (parent instanceof Visitable) {
 	                List<PathElement> parentPath = PathElement.getPath(parent, metaModelManager);
 	                int iMax = parentPath.size();
 	                int i = 0;
 	                if (scope != null) {
 	                    List<PathElement> scopePath = PathElement.getPath(scope, metaModelManager);
 	                    i = PathElement.getCommonLength(parentPath, scopePath);
 	                }
 	                if (i < iMax) {
 	//                    append(parentPath.get(i++).getName());
 	                	appendElement(parentPath.get(i++).getElement());
 	                    while (i < iMax) {
 	                        append("::");               
 	//                        append(parentPath.get(i++).getName());
 	                        appendElement(parentPath.get(i++).getElement());
 	                    }
 	                }
 	//                safeVisit((Visitable) parent);
 				}
 				else  {
 					assert element instanceof org.eclipse.ocl.examples.pivot.Package || element instanceof ExpressionInOCL : element.eClass().getName();	
 				}
 			}
 			append(parentSeparator);
     	}
     	finally {
         	popMode(savedMode);
     	}
 	}
 
     public void appendQualifiedType(Element element) {
     	Mode savedMode = pushMode(Mode.TYPE);
     	try {
 	        MetaModelManager metaModelManager = options.getGlobalOptions().getMetaModelManager();
 	        Namespace parent = PivotUtil.getNamespace(element.eContainer());
 	        List<PathElement> parentPath = PathElement.getPath(parent, metaModelManager);
 	        int iMax = parentPath.size();
 	        int i = 0;
 	        Namespace scope = options.getScope();
 	        if (scope != null) {
 				List<PathElement> scopePath = PathElement.getPath(scope, metaModelManager);
 	            i = PathElement.getCommonLength(parentPath, scopePath);
 	        }
 	        if ((i == 0) && (i < iMax)) {
 	            PathElement rootPathElement = parentPath.get(0);
 				String name = rootPathElement.getName();
 	        	String alias = options.getAlias((Namespace)rootPathElement.getElement());
 	        	if (alias != null) {
 	        		append(getName(alias, options.getReservedNames()));
 	        		append("::");               
 	                i++;
 	            }
 	            else if (PivotConstants.ORPHANAGE_NAME.equals(name)) {
 	                i++;
 	            }
 	            else if (PivotPackage.eNAME.equals(name)) {
 	                i++;
 	            }
 	            else if ("ocl".equals(name)) {            // FIXME constant needed
 	                i++;
 	            }
 	            else {
 	            	URI uri = rootPathElement.getElement().eResource().getURI();
 	            	if (uri != null) {
 	                	if (PivotUtil.isPivotURI(uri)) {
 	                		uri = PivotUtil.getNonPivotURI(uri);
 	                	}
 	                	URI baseURI = options.getBaseURI();
 	                	if (baseURI != null) {
 	                		uri = uri.deresolve(baseURI);
 	                	}
 	            		append(getName(uri.toString(), options.getReservedNames()));
 	            		append("::");               
 	                    i++;
 	            	}
 	            }
 	        }
 	        while (i < iMax) {
 	            appendElement(parentPath.get(i++).getElement());
 	            append("::");               
 	        }
 	        appendElement(element);
     	}
     	finally {
         	popMode(savedMode);
     	}
     }
 
     public void appendTemplateBindings(TemplateableElement typeRef) {
     	Mode savedMode = pushMode(Mode.TYPE);
 		try {
 			List<TemplateBinding> templateBindings = typeRef.getTemplateBinding();
 			if (!templateBindings.isEmpty()) {
 				append("(");
 				String prefix = ""; //$NON-NLS-1$
 				for (TemplateBinding templateBinding : templateBindings) {
 					for (TemplateParameterSubstitution templateParameterSubstitution : templateBinding.getParameterSubstitution()) {
 						append(prefix);
 						Namespace savedScope = pushScope((Namespace) typeRef);
 						try {
 							appendElement(templateParameterSubstitution.getActual());
 							//					appendName((NamedElement) templateParameterSubstitution.getActual());	// FIXME cast, selective scope
 						}
 						finally {
 							popScope(savedScope);
 						}
 						prefix = ", ";
 					}
 				}
 				append(")");
 			}
 		}
 		finally {
         	popMode(savedMode);
 		}
 	}
 
 	public void appendTemplateParameters(TemplateableElement templateableElement) {
 		TemplateSignature templateSignature = templateableElement.getOwnedTemplateSignature();
 		if (templateSignature != null) {
 			List<TemplateParameter> templateParameters = templateSignature.getOwnedParameter();
 			if (!templateParameters.isEmpty()) {
 				append("(");
 				String prefix = ""; //$NON-NLS-1$
 				for (TemplateParameter templateParameter : templateParameters) {
 					append(prefix);
 //					emittedTemplateParameter(templateParameter);
 //					appendName((NamedElement) templateParameter.getParameteredElement(), restrictedNames);
 					Namespace savedScope = pushScope((Namespace) templateableElement);
 					try {
 						appendElement(templateParameter);
 					}
 					finally {
 						popScope(savedScope);
 					}
 					prefix = ", ";
 				}
 				append(")");
 			}
 		}
 	}
 
 	public void appendTypedMultiplicity(TypedMultiplicityElement object) {
 		int lower = object.getLower().intValue();
 		int upper = object.getUpper().intValue();
 		if (upper != 1) {
 			if (object.isOrdered()) {
 				if (object.isUnique()) {
 					append("OrderedSet");
 				}
 				else {
 					append("Sequence");
 				}
 			}
 			else {
 				if (object.isUnique()) {
 					append("Set");
 				}
 				else {
 					append("Bag");
 				}
 			}
 			append("(");
 			appendElement(object.getType());
 			if ((lower > 0) || (upper >= 0)) {
 				appendMultiplicity(lower, upper);
 			}
 			append(")");
 		}
 		else {
 			appendElement(object.getType());
 			appendMultiplicity(lower, upper);
 		}
 	}
 
 	public Precedence getCurrentPrecedence() {
 		return currentPrecedence;
 	}
 
 	public Set<String> getReservedNames() {
 		return options.getReservedNames();
 	}
 
 	public Set<String> getRestrictedNames() {
 		return options.getRestrictedNames();
 	}
 
 	public Namespace getScope() {
 		return scope;
 	}
 
 	/**
 	 * Emit text to the current indented region.
 	 * Start a new indented region.
 	 * 
 	 * If it is not necessary to start a new-line after text, emit suffix instead of the new-line.
 	 */
 	public void push(String text, String suffix) {
 		append(text);
 //		if ((pendingPrefix.length() > 0) || (pendingText.length() > 0)) {
 			fragment = fragment.addChild(pendingPrefix, pendingText.toString(), suffix);
 			fragment.exdented = true;
 			pendingPrefix = "";
 			pendingText.setLength(0);
 //		}
 	}
 
 	/**
 	 * Flush the current indented region.
 	 * Emit text exdented with respect to the current indented region.
 	 * Start a new indented region.
 	 * 
 	 * If it is not necessary to start a new-line before text, emit prefix instead of the new-line.
 	 * 
 	 * If it is not necessary to start a new-line after text, emit suffix instead of the new-line.
 	 */
 	public void exdent(String prefix, String text, String suffix) {
 		assert (fragment != null) && (fragment.getParent() != null);
 		if (((pendingPrefix != null) && (pendingPrefix.length() > 0)) || (pendingText.length() > 0)) {
 			fragment.addChild(pendingPrefix, pendingText.toString(), "");
 			pendingPrefix = "";
 			pendingText.setLength(0);
 		}
 		if ((prefix.length() > 0) || (text.length() > 0)) {
 			fragment = fragment.getParent().addChild(prefix, text.toString(), suffix);
 			fragment.exdented = true;
 		}
 	}
 
 	public String getName(NamedElement object, Set<String> keywords) {
 		if (object == null) {
 			return NULL_PLACEHOLDER;
 		}
 		return getName(object.getName(), keywords);
 	}
 
 	public String getName(String name, Set<String> keywords) {
 		if ((keywords == null) || (!keywords.contains(name)) && PivotUtil.isValidIdentifier(name)) {
 			return name;
 		}
 		StringBuilder s = new StringBuilder();
 		s.append("_'");
 		s.append(PivotUtil.convertToOCLString(name));
 		s.append("'");
 		return s.toString();
 	}
 
 	/**
 	 * Flush the current indented region.
 	 * Emit text indented with respect to the current indented region.
 	 * Start a new indented region.
 	 * 
 	 * If it is not necessary to start a new-line before text, emit prefix instead of the new-line.
 	 * 
 	 * If it is not necessary to start a new-line after text, emit suffix instead of the new-line.
 	 */
 	public void next(String prefix, String text, String suffix) {
 		assert fragment != null;
 		if (((pendingPrefix != null) && (pendingPrefix.length() > 0)) || (pendingText.length() > 0)) {
 			fragment.addChild(pendingPrefix, pendingText.toString(), "");
 			pendingPrefix = "";
 			pendingText.setLength(0);
 		}
 //		if ((prefix.length() > 0) || (text.length() > 0)) {
 			fragment.addChild(prefix, text, suffix);
 //		}
 	}
 
 	/**
 	 * Flush the current indented region.
 	 * Resume output with one less indentation depth.
 	 */
 	public void pop() {
 		assert fragment != null;
 		if (((pendingPrefix != null) && (pendingPrefix.length() > 0)) || (pendingText.length() > 0)) {
 			fragment.addChild(pendingPrefix, pendingText.toString(), "");
 		}
 		pendingPrefix = "";
 		pendingText.setLength(0);
 		assert fragment.getParent() != null;
 		fragment = fragment.getParent();
 	}
 
     public void popMode(Mode oldMode) {
     	mode = oldMode;
     }
 
 	public void popScope(Namespace oldScope) {
 		scope = oldScope;
 	}
 
 	public void precedenceVisit(OCLExpression expression, Precedence newPrecedence) {
 		Precedence savedPrecedcence = currentPrecedence;
 		try {
 			currentPrecedence = newPrecedence;
 			appendElement(expression);
 		}
 		finally {
 			currentPrecedence = savedPrecedcence;
 		}
 	}
 
     public Mode pushMode(Mode newMode) {
     	Mode oldMode = mode;
     	mode = newMode;
     	return oldMode;
     }
 
 	public Namespace pushScope(Namespace newScope) {
 		Namespace oldscope = scope;
     	scope = newScope;
     	return oldscope;
 	}
 
 	public boolean showNames() {
 		return (mode == Mode.NAME) || (mode == Mode.FULL);
 	}
 
 	@Override
 	public String toString() {
 		if (fragment == null) {
 			return pendingPrefix + pendingText.toString();
 		}
 		fragment.configureLineWrapping(options.getIndentStep().length(), options.getLinelength());
 		StringBuilder s = new StringBuilder();
 		String newLine = fragment.toString(s, null, "  ");
 		return s.toString() + newLine + pendingPrefix + pendingText.toString();
 	}
 	
 	public String toString(String indent, int lineLength) {
 		if (fragment == null) {
 			return pendingPrefix + pendingText.toString();
 		}
 		fragment.configureLineWrapping(indent.length(), lineLength);
 		StringBuilder s = new StringBuilder();
 		fragment.toString(s, null, indent);
 //		System.out.println(s.toString() + "--" + pendingPrefix + "--" + pendingText.toString());
 		return s.toString() + pendingPrefix + pendingText.toString();
 	}
 }
