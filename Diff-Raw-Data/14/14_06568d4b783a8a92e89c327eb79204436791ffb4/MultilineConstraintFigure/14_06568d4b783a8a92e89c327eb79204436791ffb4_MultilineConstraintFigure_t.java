 /*
 * Copyright (c) 2006, 2009 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Michael Golubev (Borland) - initial API and implementation
  */
 package org.eclipse.uml2.diagram.common.draw2d;
 
 import org.eclipse.gmf.runtime.draw2d.ui.figures.WrappingLabel;
 
 public class MultilineConstraintFigure extends ConstraintFigureBase {
 
 	private WrappingLabel myLanguage;
 
 	private WrappingLabel myBody;
 
 	public MultilineConstraintFigure() {
 		super();

 		myLanguage = new WrappingLabel();
 		add(myLanguage);
 		myBody = new WrappingLabel();
 		add(myBody);
 	}
 
 	public WrappingLabel getBodyLabel() {
 		return myBody;
 	}
 
 	public WrappingLabel getLanguageLabel() {
 		return myLanguage;
 	}
 
 	protected void setTextLabelWrap(boolean wrap) {
 		getBodyLabel().setTextWrap(wrap);
 	}
 
 }
