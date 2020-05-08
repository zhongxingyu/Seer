 /*******************************************************************************
  * Copyright (c) 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.pagedesigner.css2.font;
 
 import org.eclipse.jst.pagedesigner.css2.ICSSStyle;
 import org.eclipse.jst.pagedesigner.css2.property.FontFamilyMeta;
 import org.eclipse.jst.pagedesigner.css2.property.FontSizeMeta;
 import org.eclipse.jst.pagedesigner.css2.property.FontWeightMeta;
 import org.eclipse.jst.pagedesigner.css2.property.ICSSPropertyID;
 import org.eclipse.jst.pagedesigner.css2.value.Length;
 import org.eclipse.jst.pagedesigner.utils.CacheManager;
 import org.eclipse.jst.pagedesigner.utils.ICacheEntryCreator;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.widgets.Display;
 
 /**
  * @author mengbo
  */
 public class CSSFontManager implements ICSSFontManager {
 	private static CSSFontManager _instance;
 
 	private static final boolean DEBUG = false;
 
 	private int _totalFont = 0;
 
 	// private static FontPoolManager _fontPoolManager;
 	// Map _cache = new HashMap();
 
 	private static final int CACHESIZE = 100; // we cache 100 font.
 
 	// the scale to convert the px to pt.
	private final static double FONT_SCALE = ((double) Display.getCurrent()
			.getDPI().x) / 72;
 
 	static String cssFontToLocalFont(String original) {
 		if ("serif".equalsIgnoreCase(original)) { //$NON-NLS-1$
 			return "Georgia"; //$NON-NLS-1$
 		} else if ("sans-serif".equalsIgnoreCase(original)) { //$NON-NLS-1$
 			return "Arial"; //$NON-NLS-1$
 		} else if ("cursive".equalsIgnoreCase(original)) { //$NON-NLS-1$
 			// FIXME: MS windows does not support the alternative fonts that
 			// match cursive defined at
 			// http://www.w3.org/TR/REC-CSS2/fonts.html#generic-font-families,
 			// We use Comic Sans MS font family
 			// because it is MS alternative.
 			return "Comic Sans MS"; //$NON-NLS-1$
 		} else if ("fantasy".equalsIgnoreCase(original)) { //$NON-NLS-1$
 			return cssFontToLocalFont("serif"); //$NON-NLS-1$
 		} else if ("monospace".equalsIgnoreCase(original)) { //$NON-NLS-1$
 			return "Courier New"; //$NON-NLS-1$
 		} else {
 			return original;
 		}
 	}
 
 	private CacheManager _cacheManager = new CacheManager(
 			new ICacheEntryCreator<CSSFont, Font>() {
 				public Font createEntry(CSSFont key) {
 					if (DEBUG) {
 						_totalFont++;
 						System.out.println("TotalFont++: " + _totalFont); //$NON-NLS-1$
 					}
 					Font font = new Font(null, cssFontToLocalFont(key
 							.getFontFamily()), (int) Math.round(key
 							.getFontSize()
 							/ FONT_SCALE), key.getSwtFontStyle());
 					return font;
 				}
 
 				public void dispose(CSSFont key, Font entry) {
 					if (DEBUG) {
 						_totalFont--;
 						System.out.println("TotalFont--: " + _totalFont); //$NON-NLS-1$
 					}
 					entry.dispose();
 
 				}
 			}, CACHESIZE);
 
 	/**
 	 * constructor
 	 */
 	private CSSFontManager() {
 		super();
 	}
 
 	private String resolveFontStyleString(ICSSStyle style) {
 		StringBuffer sb = new StringBuffer();
 		sb.append(ICSSPropertyID.ATTR_FONT_FAMILY).append(":"); //$NON-NLS-1$
 		sb.append("'").append( //$NON-NLS-1$
 				(String) style
 						.getStyleProperty(ICSSPropertyID.ATTR_FONT_FAMILY))
 				.append("'"); //$NON-NLS-1$
 		sb.append(";"); //$NON-NLS-1$
 		sb.append(ICSSPropertyID.ATTR_FONT_STYLE).append(":"); //$NON-NLS-1$
 		sb
 				.append(
 						(String) style
 								.getStyleProperty(ICSSPropertyID.ATTR_FONT_STYLE))
 				.append(";"); //$NON-NLS-1$
 		sb.append(ICSSPropertyID.ATTR_FONT_WEIGHT).append(":"); //$NON-NLS-1$
 		sb.append(
 				((Integer) style
 						.getStyleProperty(ICSSPropertyID.ATTR_FONT_WEIGHT))
 						.toString()).append(";"); //$NON-NLS-1$
 		sb.append(ICSSPropertyID.ATTR_FONT_SIZE).append(":"); //$NON-NLS-1$
 		int fontSize = getFontSize(style, style
 				.getStyleProperty(ICSSPropertyID.ATTR_FONT_SIZE));
 		sb.append(Integer.toString(fontSize));
 		return sb.toString();
 	}
 
 	public ICSSFont createFont(ICSSStyle style) {
 		String fontfamily = (String) style
 				.getStyleProperty(ICSSPropertyID.ATTR_FONT_FAMILY);
 		Object fontsizeobj = style
 				.getStyleProperty(ICSSPropertyID.ATTR_FONT_SIZE);
 		int fontsize;
 		fontsize = getFontSize(style, fontsizeobj);
 		int fontstyle = getFontStyle(style);
 		int fontweight = ((Integer) style
 				.getStyleProperty(ICSSPropertyID.ATTR_FONT_WEIGHT)).intValue();
 
 		return new CSSFont(fontfamily, fontsize, fontstyle, fontweight,
 				resolveFontStyleString(style));
 	}
 
 	private int getFontSize(ICSSStyle style, Object fontsizeobj) {
 		int fontsize;
 		if (fontsizeobj instanceof Length) {
 			fontsize = ((Length) fontsizeobj).getValue();
 		} else {
 			fontsize = style.getParentStyle().getCSSFont().getFontSize();
 		}
 		return fontsize;
 	}
 
 	/**
 	 * @param style
 	 */
 	private int getFontStyle(ICSSStyle style) {
 		int fontstyle;
 		String fontstylestr = (String) style
 				.getStyleProperty(ICSSPropertyID.ATTR_FONT_STYLE);
 		if (ICSSPropertyID.VAL_ITALIC.equals(fontstylestr)
 				|| ICSSPropertyID.VAL_OBLIQUE.equals(fontstylestr)) {
 			fontstyle = SWT.ITALIC;
 		} else {
 			fontstyle = SWT.NORMAL;
 		}
 		return fontstyle;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.css2.font.ICSSFontManager#dispose()
 	 */
 	public void dispose() {
 		_cacheManager.disposeAll();
 	}
 
 	/**
 	 * @return the default css font
 	 */
 	public ICSSFont createDefaultFont() {
 		CSSFont result = new CSSFont(FontFamilyMeta.DEFAULT_FONT,
 				(int) FontSizeMeta.MEDIUM_VAL_INT, SWT.NORMAL,
 				FontWeightMeta.NORMAL_WEIGHT.intValue(), ""); //$NON-NLS-1$
 		return result;
 	}
 
 	/**
 	 * @param f
 	 * @return the swt font for f
 	 */
 	public Font getSwtFont(CSSFont f) {
 		return (Font) _cacheManager.getEntry(f);
 	}
 
 	/**
 	 * @return the singleton font manager
 	 */
 	public static CSSFontManager getInstance() {
 		if (_instance == null) {
 			_instance = new CSSFontManager();
 		}
 		return _instance;
 	}
 }
