 /*******************************************************************************
  * Copyright (c) 2012 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.core.notifications.util;
 
 import static org.zend.core.notifications.util.FontName.BOLD;
 import static org.zend.core.notifications.util.FontName.DEFAULT;
 import static org.zend.core.notifications.util.FontName.ITALIC;
 import static org.zend.core.notifications.util.FontName.ITALIC_BOLD;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.widgets.Display;
 
 /**
  * Caching class for fonts. Also deals with re-creating fonts should they have
  * been disposed when the caller asks for a font.
  * 
  * @author Wojciech Galanciak, 2012
  * 
  */
 public class Fonts {
 
 	private static Map<String, Font> cache = new HashMap<String, Font>();
 
 	static {
		Font defaultFont = JFaceResources.getFontRegistry().defaultFont();
		cache.put(DEFAULT.getName(), defaultFont);
		Font boldFont = JFaceResources.getFontRegistry().getBold(
				JFaceResources.DEFAULT_FONT);
		cache.put(BOLD.getName(), boldFont);
		Font italicFont = JFaceResources.getFontRegistry().getItalic(
				JFaceResources.DEFAULT_FONT);
		cache.put(ITALIC.getName(), italicFont);
		Font italicBold = new Font(Display.getCurrent(), getModifiedFontData(
				get(ITALIC).getFontData(), SWT.BOLD | SWT.ITALIC));
		cache.put(ITALIC_BOLD.getName(), italicBold);
 	}
 
 	/**
 	 * Disposes all fonts and clears out the cache. Never call this unless you
 	 * are shutting down your code/client/etc.
 	 */
 	public static void dispose() {
 		if (cache.size() > 0) {
 			Set<String> keys = cache.keySet();
 			for (String key : keys) {
 				Font font = cache.get(key);
 				if (font != null && !font.isDisposed()) {
 					font.dispose();
 				}
 			}
 		}
 		cache.clear();
 	}
 
 	/**
 	 * Get font for specified {@link FontName}.
 	 * 
 	 * @param name
 	 *            font name
 	 * @return font
 	 */
 	public static Font get(FontName name) {
 		return cache.get(name.getName());
 	}
 
 	/**
 	 * Get font for specified name.
 	 * 
 	 * @param name
 	 *            font name
 	 * @return font or <code>null</code> if font with specified name is
 	 *         unavailable
 	 */
 	public static Font get(String name) {
 		return cache.get(name);
 	}
 
 	/**
 	 * Add new font to change with specified name.
 	 * 
 	 * @param name
 	 *            new font name
 	 * @param font
 	 * @return <code>true</code> if font added successfully; otherwise return
 	 *         <code>false</code>
 	 */
 	public static boolean add(String name, Font font) {
 		if (cache.containsKey(name)) {
 			return false;
 		}
 		cache.put(name, font);
 		return true;
 	}
 
 	private static FontData[] getModifiedFontData(FontData[] baseData, int style) {
 		FontData[] styleData = new FontData[baseData.length];
 		for (int i = 0; i < styleData.length; i++) {
 			FontData base = baseData[i];
 			styleData[i] = new FontData(base.getName(), base.getHeight(),
 					base.getStyle() | style);
 		}
 
 		return styleData;
 	}
 
 }
