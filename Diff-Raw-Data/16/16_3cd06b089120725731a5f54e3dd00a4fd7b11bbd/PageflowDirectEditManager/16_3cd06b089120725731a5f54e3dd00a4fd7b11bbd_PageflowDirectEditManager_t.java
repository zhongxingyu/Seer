 /*******************************************************************************
  * Copyright (c) 2004, 2005 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.jst.jsf.facesconfig.ui.pageflow.editpart;
 
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.Label;
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.gef.GraphicalEditPart;
 import org.eclipse.gef.tools.CellEditorLocator;
 import org.eclipse.gef.tools.DirectEditManager;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.events.VerifyListener;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * DirectEditManager for Pageflow node
  * 
  * @author - Xiaoguang Zhang
  */
 public class PageflowDirectEditManager extends DirectEditManager {
 	/** text font */
 	protected Font scaledFont;
 
 	/** verify listener for the text control */
 	protected VerifyListener verifyListener;
 
 	/** the direct editor's parent label. */
 	protected Label label;
 
 	/**
 	 * Creates a new PageflowDirectEditManager with the given attributes.
 	 * 
 	 * @param source
 	 *            the source EditPart
 	 * @param editorType
 	 *            type of editor
 	 * @param locator
 	 *            the CellEditorLocator
 	 */
 	public PageflowDirectEditManager(GraphicalEditPart source,
 			Class editorType, CellEditorLocator locator, Label label) {
 		super(source, editorType, locator);
 		this.label = label;
 	}
 
 	/*
 	 * (non-javadoc)
 	 * 
 	 * @see org.eclipse.gef.tools.DirectEditManager#bringDown()
 	 */
 	protected void bringDown() {
 		// This method might be re-entered when super.bringDown() is called.
 		Font disposeFont = scaledFont;
 		scaledFont = null;
 		super.bringDown();
		if (disposeFont != null && !disposeFont.isDisposed()) {
 			disposeFont.dispose();
 		}
 	}
 
 	/*
 	 * (non-javadoc)
 	 * 
 	 * @see org.eclipse.gef.tools.DirectEditManager#initCellEditor()
 	 */
 	protected void initCellEditor() {
 		Text text = (Text) getCellEditor().getControl();
 		verifyListener = new VerifyListener() {
 			public void verifyText(VerifyEvent event) {
 				Text text_ = (Text) getCellEditor().getControl();
 				String oldText = text_.getText();
 				// get the left string of the new input character
 				String leftText = oldText.substring(0, event.start);
 				// get the right string of the new input charactor
 				String rightText = oldText.substring(event.end, oldText
 						.length());
 
 				GC gc = new GC(text_);
 				Point size = gc.textExtent(leftText + event.text + rightText);
 				gc.dispose();
 				if (size.x != 0) {
 					size = text_.computeSize(size.x, SWT.DEFAULT);
 				}
 				getCellEditor().getControl().setSize(size.x, size.y);
 			}
 		};
 		text.addVerifyListener(verifyListener);
 
 		// set the initial text, font, to the direct editor
 		String initialLabelText = label.getText();
 		getCellEditor().setValue(initialLabelText);
 		IFigure figure = getEditPart().getFigure();
		final Font figureFont = figure.getFont();
        // take a copy of the font data for the label we are cell editing
		FontData data = figureFont.getFontData()[0];
 		Dimension fontSize = new Dimension(0, data.getHeight());
 		label.translateToAbsolute(fontSize);
 		data.setHeight(fontSize.height);
 
		if (scaledFont != null && !scaledFont.isDisposed())
        {
 			scaledFont.dispose();
        }
		scaledFont = new Font(figureFont.getDevice(), data);
 
 		text.setFont(scaledFont);
 		text.selectAll();
 	}
 
 	/*
 	 * (non-javadoc)
 	 * 
 	 * @see org.eclipse.gef.tools.DirectEditManager#unhookListeners()
 	 */
 	protected void unhookListeners() {
 		super.unhookListeners();
 		Text text = (Text) getCellEditor().getControl();
 		text.removeVerifyListener(verifyListener);
 		verifyListener = null;
 	}
 
 }
