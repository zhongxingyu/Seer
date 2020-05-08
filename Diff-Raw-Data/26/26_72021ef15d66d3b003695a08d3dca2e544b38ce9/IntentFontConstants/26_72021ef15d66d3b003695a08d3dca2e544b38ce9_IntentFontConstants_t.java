 /*******************************************************************************
  * Copyright (c) 2010, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.client.ui.editor.configuration;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.widgets.Display;
 
 /**
  * Constants for all the font used by an Intent Editor.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public final class IntentFontConstants {
 
 	public static final int DU_FONT_SIZE = 10;
 
	public static final int DU_TITLE_SIZE = 10;
 
 	public static final Font MU_DEFAULT_FONT = new Font(Display.getCurrent(), "Courier New", 9, SWT.NONE);
 
 	public static final Font MU_KW_FONT = new Font(Display.getCurrent(), "Courier New", 9, SWT.BOLD);
 
 	/**
 	 * The name of the font to use to render documentation.
 	 */
 	private static final String DOCUMENTATION_FONT_NAME = "ComputerModern";
 
 	/**
 	 * The name of the font to use to render documentation, if the DOCUMENTATION_FONT_NAME is unavailable.
 	 */
 	private static final String DOCUMENTATION_DEFAULT_FONT_NAME = "Verdana";
 
 	private static Font DU_FONT;
 
 	private static Font TITLE_FONT;
 
 	/**
 	 * IntentFontConstants constructor.
 	 */
 	private IntentFontConstants() {
 	}
 
 	/**
 	 * Initialize the font to associate with not keyWords elements.
 	 */
 	private static void initializeWikiFont() {
		IntentFontConstants.TITLE_FONT = new Font(Display.getCurrent(), DOCUMENTATION_DEFAULT_FONT_NAME,
				IntentFontConstants.DU_TITLE_SIZE, SWT.BOLD);

 		// We try to initialize the description font to Computer Modern (used by Latex)
 		IntentFontConstants.DU_FONT = new Font(Display.getCurrent(), DOCUMENTATION_FONT_NAME,
 				IntentFontConstants.DU_FONT_SIZE, SWT.NONE);
 
 		if (IntentFontConstants.DU_FONT == null) {
 			// If this font isn't available, we use the Verdana font.
 			IntentFontConstants.DU_FONT = new Font(Display.getCurrent(), DOCUMENTATION_DEFAULT_FONT_NAME,
 					DU_FONT_SIZE, SWT.NONE);
 			IntentFontConstants.TITLE_FONT = new Font(Display.getCurrent(), DOCUMENTATION_DEFAULT_FONT_NAME,
 					IntentFontConstants.DU_TITLE_SIZE, SWT.BOLD);
 		}
 
 	}
 
 	/**
 	 * Returns the font associated to description units.
 	 * 
 	 * @return the font associated to description units.
 	 */
 	public static Font getDescriptionFont() {
 		if (DU_FONT == null) {
 			initializeWikiFont();
 		}
 		return DU_FONT;
 	}
 
 	/**
 	 * Returns the font associated to structured elements title.
 	 * 
 	 * @return the font associated to structured elements title.
 	 */
 	public static Font getTitleFont() {
 		if (TITLE_FONT == null) {
 			initializeWikiFont();
 		}
 		return TITLE_FONT;
 	}
 
 }
