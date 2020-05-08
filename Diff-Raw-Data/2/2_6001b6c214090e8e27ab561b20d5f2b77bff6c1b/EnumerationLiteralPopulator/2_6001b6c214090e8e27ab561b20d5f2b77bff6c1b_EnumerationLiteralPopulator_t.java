 /*
  * Copyright (c) 2012 Vienna University of Technology.
  * All rights reserved. This program and the accompanying materials are made 
  * available under the terms of the Eclipse Public License v1.0 which accompanies 
  * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Philip Langer - initial API and generator
  */
 package org.modelexecution.fuml.convert.xmof.internal.ecore;
 
 import org.eclipse.emf.ecore.EEnumLiteral;
 import org.eclipse.emf.ecore.EModelElement;
 import org.modelexecution.fuml.convert.impl.ConversionResultImpl;
 import org.modelexecution.fuml.convert.xmof.internal.IElementPopulator;
 
 import fUML.Syntax.Classes.Kernel.Element;
 import fUML.Syntax.Classes.Kernel.Enumeration;
 import fUML.Syntax.Classes.Kernel.EnumerationLiteral;
 
 public class EnumerationLiteralPopulator implements IElementPopulator {
 
 	@Override
 	public void populate(Element fumlElement, EModelElement element,
 			ConversionResultImpl result) {
 		if (!(element instanceof EEnumLiteral)
 				|| !(fumlElement instanceof EnumerationLiteral)) {
 			return;
 		}
 
 		EEnumLiteral eEnumLiteral = (EEnumLiteral) element;
 		EnumerationLiteral umlEnumerationLiteral = (EnumerationLiteral) fumlElement;
 
 		umlEnumerationLiteral.enumeration = (Enumeration) result
 				.getFUMLElement(eEnumLiteral.getEEnum());
		
		umlEnumerationLiteral.classifier.add(umlEnumerationLiteral.enumeration);
 	}
 }
