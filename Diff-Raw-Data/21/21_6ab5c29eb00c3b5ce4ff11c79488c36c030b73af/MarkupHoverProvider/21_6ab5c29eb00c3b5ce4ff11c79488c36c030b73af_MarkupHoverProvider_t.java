 /**
  * <copyright>
  *
  * Copyright (c) 2011 E.D.Willink and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     E.D.Willink - initial API and implementation
  *
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.ocl.examples.xtext.markup.ui.hover;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.ocl.examples.pivot.CallExp;
 import org.eclipse.ocl.examples.pivot.Element;
 import org.eclipse.ocl.examples.pivot.Namespace;
 import org.eclipse.ocl.examples.pivot.OclExpression;
 import org.eclipse.ocl.examples.pivot.Type;
 import org.eclipse.ocl.examples.pivot.VariableExp;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManager;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManagerResourceAdapter;
 import org.eclipse.ocl.examples.pivot.prettyprint.PrettyPrintOptions;
import org.eclipse.ocl.examples.pivot.prettyprint.PrettyPrinter;
 import org.eclipse.ocl.examples.pivot.util.Pivotable;
 import org.eclipse.ocl.examples.pivot.utilities.HTMLBuffer;
 import org.eclipse.ocl.examples.pivot.utilities.PivotUtil;
 import org.eclipse.ocl.examples.xtext.base.baseCST.ElementCS;
 import org.eclipse.ocl.examples.xtext.markup.Markup;
 import org.eclipse.ocl.examples.xtext.markup.MarkupUtils;
 import org.eclipse.xtext.nodemodel.INode;
 import org.eclipse.xtext.parser.IParseResult;
 import org.eclipse.xtext.ui.editor.hover.html.DefaultEObjectHoverProvider;
 
 public class MarkupHoverProvider extends DefaultEObjectHoverProvider
 {
 	@Override
 	protected String getDocumentation(EObject o) {
 		MetaModelManager metaModelManager = null;
 		Resource resource = o.eResource();
 		if (resource != null) {
 			MetaModelManagerResourceAdapter adapter = MetaModelManagerResourceAdapter.findAdapter(resource);
 			if (adapter != null) {
 				metaModelManager = adapter.getMetaModelManager();
 			}
 		}
 		String documentation = super.getDocumentation(o);
 		if (documentation == null) {
 			return null;
 		}
 		IParseResult parseResult = MarkupUtils.decode(documentation);
 		Markup markup = (Markup) parseResult.getRootASTElement();
 		if (markup == null) {
 			return null;
 		}
 		Iterable<INode> parseErrors = parseResult.getSyntaxErrors();
 		Map<Integer, Integer> errorMap = getErrorMap(parseErrors);
 		if (errorMap != null) {
 			HTMLBuffer htmlBuffer = new HTMLBuffer();
 			int i = 0;
 			List<Integer> starts = new ArrayList<Integer>(errorMap.keySet());
 			Collections.sort(starts);
 			for (int start : starts) {
 				int end = errorMap.get(start);
 				while (i < start) {
 					htmlBuffer.append(documentation.charAt(i++));
 				}
 				htmlBuffer.startFontColor("red"); 
 				htmlBuffer.startUnderline();
 				while (i < end) {
 					htmlBuffer.append(documentation.charAt(i++));
 				}
 				htmlBuffer.endUnderline();
 				htmlBuffer.endFontColor(); 
 			}
 			htmlBuffer.startFontColor("red"); 
 			htmlBuffer.startParagraph();
 			htmlBuffer.endParagraph();
 			for (INode parseError :parseErrors) {
 				htmlBuffer.startParagraph();
 				htmlBuffer.append(parseError.getSyntaxErrorMessage().getMessage());
 				htmlBuffer.endParagraph();
 			}
 			htmlBuffer.endFontColor(); 
 			return htmlBuffer.toString();
 		}
 		if (o instanceof Pivotable) {
 			o = ((Pivotable)o).getPivot();
 		}
 		try {
 			return MarkupUtils.toHTML(metaModelManager, o, markup);
 		} catch (Exception e) {
 			StringWriter s = new StringWriter();
 			e.printStackTrace(new PrintWriter(s));
 			return s.toString().replace("\n", "\n<br>");
 		}
 	}
 
 	protected Map<Integer, Integer> getErrorMap(Iterable<INode> parseErrors) {
 		Map<Integer, Integer> errorMap = null;
 		for (INode parseError : parseErrors) {
 			if (errorMap == null) {
 				errorMap = new HashMap<Integer, Integer>();
 			}
 			int start = parseError.getOffset();
 			errorMap.put(start, start + parseError.getLength());
 		}
 		if (errorMap != null) {
 			List<Integer> starts = new ArrayList<Integer>(errorMap.keySet());
 			Collections.sort(starts);
 			Integer currentStart = null;
 			int currentEnd = 0;
 			for (int i = 0; i < starts.size(); i++) {
 				Integer start = starts.get(i);
 				if (currentStart == null) {
 					currentStart = start;
 					currentEnd = errorMap.get(start);
 				}
 				else {
 					currentEnd = errorMap.get(start);
 					if (start > currentEnd) {
 						currentStart = start;
 					}
 					else {
 						errorMap.remove(start);
 						errorMap.put(currentStart, currentEnd);
 					}
 				}
 			}
 		}
 		return errorMap;
 	}
 
 	@Override
 	protected String getFirstLine(EObject eObject) {
 //		System.out.println("getFirstLine " + eObject.eClass().getName());
 		Element pivotElement = null;
 		if (eObject instanceof Pivotable) {
 			pivotElement = PivotUtil.getPivot(Element.class, (Pivotable)eObject);
 		}
 		else if (eObject instanceof Element) {
 			pivotElement = (Element)eObject;
 		}
 		if (pivotElement != null) {
 			Namespace namespace = getNamespace(pivotElement);
 			if ((namespace != null) && (namespace.eContainer() instanceof Namespace)) {
 				namespace = (Namespace) namespace.eContainer();
 			}
 			String description;
 			PrettyPrintOptions.Global prettyPrintOptions = new PrettyPrintOptions.Global(namespace)
 			{
 				@Override
 				public Set<String> getReservedNames() {
 					return null;
 				}
 
 				@Override
 				public Set<String> getRestrictedNames() {
 					return null;
 				}				
 			};
 			if (namespace != null) {
 				Resource eResource = EcoreUtil.getRootContainer(namespace).eResource();
 				if (eResource != null) {
 					prettyPrintOptions.setMetaModelManager(MetaModelManager.getAdapter(eResource.getResourceSet()));
 				}
 			}
 			if (pivotElement instanceof CallExp) {
 				description = PrettyPrinter.printType(pivotElement, prettyPrintOptions);
 			}
 			else if (pivotElement instanceof VariableExp) {
 				description = PrettyPrinter.print(((VariableExp)pivotElement).getReferredVariable(), prettyPrintOptions);
 			}
 			else if (pivotElement instanceof OclExpression) {
 				description = PrettyPrinter.printType(((OclExpression)pivotElement).getType(), prettyPrintOptions);
 			}
 			else {
 				description = PrettyPrinter.print(pivotElement, prettyPrintOptions);
 			}
 //			System.out.println(" => " + description);
 			return pivotElement.eClass().getName() + " <b>" + description + "</b>";
 		}
 		else {
 			String firstLine = super.getFirstLine(eObject);
 //			System.out.println(" => " + firstLine);
 			return firstLine + "\n<br>" + eObject.eClass().getName();		// FIXME do better					
 		}
 	}
 
 	public static Namespace getNamespace(EObject element) {
 		int count = 0;
 		for (EObject eObject = element; eObject != null; eObject = eObject.eContainer()) {
 			if (eObject instanceof Namespace) {
 				if (count++ >= 2) {
 					return (Namespace) eObject;
 				}
 			}
 			if (eObject instanceof Type) {
 				count++;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	protected boolean hasHover(EObject o) {
 		if (o instanceof Element) {
 			return true;
 		}
 		if (o instanceof ElementCS) {
 			return true;
 		}
 		return super.hasHover(o);
 	}
 }
