 /*******************************************************************************
  * Copyright (c) 2006-2011
  * Software Technology Group, Dresden University of Technology
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   Software Technology Group - TU Dresden, Germany 
  *      - initial API and implementation
  ******************************************************************************/
 package org.emftext.sdk.codegen.resource.ui.generators.ui;
 
 import static org.emftext.sdk.codegen.resource.ui.IUIClassNameConstants.BAD_LOCATION_EXCEPTION;
 import static org.emftext.sdk.codegen.resource.ui.IUIClassNameConstants.ECORE_UTIL;
 import static org.emftext.sdk.codegen.resource.ui.IUIClassNameConstants.E_OBJECT;
 import static org.emftext.sdk.codegen.resource.ui.IUIClassNameConstants.I_HYPERLINK;
 import static org.emftext.sdk.codegen.resource.ui.IUIClassNameConstants.I_HYPERLINK_DETECTOR;
 import static org.emftext.sdk.codegen.resource.ui.IUIClassNameConstants.I_REGION;
 import static org.emftext.sdk.codegen.resource.ui.IUIClassNameConstants.I_TEXT_VIEWER;
 import static org.emftext.sdk.codegen.resource.ui.IUIClassNameConstants.LIST;
 import static org.emftext.sdk.codegen.resource.ui.IUIClassNameConstants.REGION;
 import static org.emftext.sdk.codegen.resource.ui.IUIClassNameConstants.RESOURCE;
 
 import org.emftext.sdk.codegen.composites.JavaComposite;
 import org.emftext.sdk.codegen.composites.StringComposite;
 import org.emftext.sdk.codegen.parameters.ArtifactParameter;
 import org.emftext.sdk.codegen.resource.GenerationContext;
 import org.emftext.sdk.codegen.resource.ui.generators.UIJavaBaseGenerator;
 
 public class HyperlinkDetectorGenerator extends UIJavaBaseGenerator<ArtifactParameter<GenerationContext>> {
 
 	public void generateJavaContents(JavaComposite sc) {
 		
 		sc.add("package " + getResourcePackageName() + ";");
 		sc.addLineBreak();
 		
 		sc.addJavadoc(
 			"A hyperlink detector returns hyperlink if the token, where the mouse cursor " +
 			"hovers, is a proxy."
 		);
 		sc.add("public class " + getResourceClassName() + " implements " + I_HYPERLINK_DETECTOR + " {");
 		sc.addLineBreak();
 
 		addFields(sc);
 		addConstructor(sc);
 		addDetectHyperlinksMethod(sc);
 		
 		sc.add("}");
 	}
 
 	private void addFields(StringComposite sc) {
 		sc.add("private " + iTextResourceClassName + " textResource;");
 		sc.addLineBreak();
 	}
 
	private void addDetectHyperlinksMethod(StringComposite sc) {
 		sc.add("public " + I_HYPERLINK + "[] detectHyperlinks(" + I_TEXT_VIEWER + " textViewer, " + I_REGION + " region, boolean canShowMultipleHyperlinks) {");
 		sc.add(iLocationMapClassName + " locationMap = textResource.getLocationMap();");
 		sc.add(LIST + "<" + E_OBJECT + "> elementsAtOffset = locationMap.getElementsAt(region.getOffset());");
 		sc.add(E_OBJECT + " resolvedEObject = null;");
 		sc.add("for (" + E_OBJECT + " eObject : elementsAtOffset) {");
 		sc.add("if (eObject.eIsProxy()) {");
 		sc.add("resolvedEObject = " + ECORE_UTIL + ".resolve(eObject, textResource);");
 		sc.add("if (resolvedEObject == eObject) {");
 		sc.add("continue;");
 		sc.add("}");
 		sc.add("int offset = locationMap.getCharStart(eObject);");
 		sc.add("int length = locationMap.getCharEnd(eObject) - offset + 1;");
 		sc.add("String text = null;");
 		sc.add("try {");
 		sc.add("text = textViewer.getDocument().get(offset, length);");
 		sc.add("} catch (" + BAD_LOCATION_EXCEPTION + " e) {");
 		sc.add("}");
 		sc.add(I_HYPERLINK + " hyperlink = new " + hyperlinkClassName + "(new " + REGION + "(offset, length), resolvedEObject, text);");
 		sc.add("return new " + I_HYPERLINK + "[] { hyperlink };");
 		sc.add("}");
 		sc.add("}");
 		sc.add("return null;");
 		sc.add("}");
 		sc.addLineBreak();
 	}
 
 	private void addConstructor(JavaComposite sc) {
 		sc.addJavadoc(
 			"Creates a hyperlink detector.",
 			"@param resource the resource to use for calculating the locations."
 		);
 		sc.add("public " + getResourceClassName() + "(" + RESOURCE + " resource) {");
 		sc.add("textResource = (" + iTextResourceClassName + ") resource;");
 		sc.add("}");
 		sc.addLineBreak();
 	}
 
 	
 }
