 /*
 * Copyright (c) 2006 Borland Software Corporation
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
 
import org.eclipse.draw2d.MarginBorder;
 import org.eclipse.gmf.runtime.draw2d.ui.figures.WrappingLabel;
 
/**
 * XXX: It is temporary hack.
 * 
 * To be removed when GMF graph will support multiline labels. If it is not
 * achiveable, move this into the generation using custom viewmap attributes.
 */
 public class MultilineConstraintFigure extends ConstraintFigureBase {
 
 	private WrappingLabel myLanguage;
 
 	private WrappingLabel myBody;
 
 	public MultilineConstraintFigure() {
 		super();
		setBorder(new MarginBorder(MARGIN_DP, MARGIN_DP, MARGIN_DP, CLIP_MARGIN_DP));
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
