 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ui.text.completion;
 
 import java.io.IOException;
 import java.io.Reader;
 
 import org.eclipse.dltk.ui.text.HTMLUtils;
 import org.eclipse.dltk.utils.TextUtils;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.RGB;
 
 /**
  * Provides a set of convenience methods for creating HTML pages.
  */
 public class HTMLPrinter {
 
 	private HTMLPrinter() {
 	}
 
 	/**
 	 * @param content
 	 * @return
 	 * @deprecated
 	 */
 	public static String convertToHTMLContent(String content) {
 		return TextUtils.escapeHTML(content);
 	}
 
 	public static String read(Reader rd) {
 		final StringBuffer buffer = new StringBuffer();
 		if (read(rd, buffer)) {
 			return buffer.toString();
 		}
 		return null;
 	}
 
 	public static boolean read(Reader rd, StringBuffer buffer) {
 		char[] readBuffer = new char[2048];
 		try {
 			int n;
 			while ((n = rd.read(readBuffer)) > 0) {
 				buffer.append(readBuffer, 0, n);
 			}
 			return true;
 		} catch (IOException e) {
 			return false;
 		}
 	}
 
 	public static void insertPageProlog(StringBuffer buffer, int position,
			RGB bgRGB, RGB fgRGB, String styleSheet) {
 
 		if (bgRGB == null)
 			insertPageProlog(buffer, position, styleSheet);
 		else {
 			StringBuffer pageProlog = new StringBuffer(300);
 
 			pageProlog.append("<html>"); //$NON-NLS-1$
 
 			appendStyleSheet(pageProlog, styleSheet);
 
			pageProlog.append("<body text=\""); //$NON-NLS-1$
			appendColor(pageProlog, fgRGB);
			pageProlog.append("\" bgcolor=\""); //$NON-NLS-1$
 			appendColor(pageProlog, bgRGB);
 			pageProlog.append("\">"); //$NON-NLS-1$
 
 			buffer.insert(position, pageProlog.toString());
 		}
 	}
 
 	public static void insertStyles(StringBuffer buffer, String[] styles) {
 		if (styles == null || styles.length == 0)
 			return;
 
 		StringBuffer styleBuf = new StringBuffer(10 * styles.length);
 		for (int i = 0; i < styles.length; i++) {
 			styleBuf.append(" style=\""); //$NON-NLS-1$
 			styleBuf.append(styles[i]);
 			styleBuf.append('"');
 		}
 
 		// Find insertion index
 		int index = buffer.indexOf("<body "); //$NON-NLS-1$
 		if (index == -1)
 			return;
 
 		buffer.insert(index + 5, styleBuf);
 	}
 
 	public static void insertPageProlog(StringBuffer buffer, int position,
 			RGB bgRGB) {
 		if (bgRGB == null)
 			insertPageProlog(buffer, position);
 		else {
 			StringBuffer pageProlog = new StringBuffer(60);
 			pageProlog.append("<html><body text=\"#000000\" bgcolor=\""); //$NON-NLS-1$
 			appendColor(pageProlog, bgRGB);
 			pageProlog.append("\">"); //$NON-NLS-1$
 			buffer.insert(position, pageProlog.toString());
 		}
 	}
 
 	private static void appendStyleSheet(StringBuffer buffer, String styleSheet) {
 		if (styleSheet == null)
 			return;
 
 		buffer.append("<head><style CHARSET=\"ISO-8859-1\" TYPE=\"text/css\">"); //$NON-NLS-1$
 		buffer.append(styleSheet);
 		buffer.append("</style></head>"); //$NON-NLS-1$
 	}
 
 	private static void appendColor(StringBuffer buffer, RGB rgb) {
 		buffer.append('#');
 		buffer.append(Integer.toHexString(rgb.red));
 		buffer.append(Integer.toHexString(rgb.green));
 		buffer.append(Integer.toHexString(rgb.blue));
 	}
 
 	public static void insertPageProlog(StringBuffer buffer, int position) {
 		insertPageProlog(buffer, position, HTMLUtils.getBgColor());
 	}
 
 	public static void insertPageProlog(StringBuffer buffer, int position,
 			String styleSheet) {
		insertPageProlog(buffer, position, HTMLUtils.getBgColor(), HTMLUtils.getFgColor(), styleSheet);
 	}
 
 	public static void addPageProlog(StringBuffer buffer) {
 		insertPageProlog(buffer, buffer.length());
 	}
 
 	public static void addPageEpilog(StringBuffer buffer) {
 		buffer.append("</font></body></html>"); //$NON-NLS-1$
 	}
 
 	public static void startBulletList(StringBuffer buffer) {
 		buffer.append("<ul>"); //$NON-NLS-1$
 	}
 
 	public static void endBulletList(StringBuffer buffer) {
 		buffer.append("</ul>"); //$NON-NLS-1$
 	}
 
 	public static void addBullet(StringBuffer buffer, String bullet) {
 		if (bullet != null) {
 			buffer.append("<li>"); //$NON-NLS-1$
 			buffer.append(bullet);
 			buffer.append("</li>"); //$NON-NLS-1$
 		}
 	}
 
 	public static void addSmallHeader(StringBuffer buffer, String header) {
 		if (header != null) {
 			buffer.append("<h5>"); //$NON-NLS-1$
 			buffer.append(header);
 			buffer.append("</h5>"); //$NON-NLS-1$
 		}
 	}
 
 	public static void addParagraph(StringBuffer buffer, String paragraph) {
 		if (paragraph != null) {
 			buffer.append("<p>"); //$NON-NLS-1$
 			buffer.append(paragraph);
 		}
 	}
 
 	public static void addParagraph(StringBuffer buffer, Reader paragraphReader) {
 		if (paragraphReader != null) {
 			final int startPos = buffer.length();
 			if (read(paragraphReader, buffer)) {
 				if (startPos != 0 || !hasProlog(buffer, startPos)) {
 					buffer.insert(startPos, "<p>"); //$NON-NLS-1$
 				}
 			}
 		}
 	}
 
 	private static boolean hasProlog(StringBuffer buffer, int pos) {
 		while (pos < buffer.length()
 				&& Character.isWhitespace(buffer.charAt(pos))) {
 			++pos;
 		}
 		for (int i = 0; i < PROLOG_MARKS.length; ++i) {
 			if (startsWithIgnoreCase(buffer, pos, PROLOG_MARKS[i])) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private static final String[] PROLOG_MARKS = new String[] { "<!DOCTYPE>", //$NON-NLS-1$
 			"<HTML>" }; //$NON-NLS-1$
 
 	/**
 	 * Checks if the <code>buffer</code> content ends with "</HTML>"
 	 * 
 	 * @param buffer
 	 * @return
 	 */
 	public static boolean hasEpilog(StringBuffer buffer) {
 		int pos = buffer.length();
 		while (pos > 0 && Character.isWhitespace(buffer.charAt(pos - 1))) {
 			--pos;
 		}
 		return pos >= EPILOG_MARK.length()
 				&& startsWithIgnoreCase(buffer, pos - EPILOG_MARK.length(),
 						EPILOG_MARK);
 	}
 
 	private static final String EPILOG_MARK = "</HTML>"; //$NON-NLS-1$
 
 	/**
 	 * Checks if the <code>buffer</code> content at the specified
 	 * <code>pos</code> contains the <code>prefix</code>
 	 * 
 	 * @param buffer
 	 * @param pos
 	 * @param prefix
 	 *            should be already upper-cased
 	 * @return
 	 */
 	private static boolean startsWithIgnoreCase(StringBuffer buffer, int pos,
 			String prefix) {
 		final int prefixLen = prefix.length();
 		if (pos + prefixLen <= buffer.length()) {
 			for (int i = 0; i < prefixLen; ++i) {
 				if (Character.toUpperCase(buffer.charAt(pos + i)) != prefix
 						.charAt(i)) {
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Replaces the following style attributes of the font definition of the
 	 * <code>html</code> element:
 	 * <ul>
 	 * <li>font-size</li>
 	 * <li>font-weight</li>
 	 * <li>font-style</li>
 	 * <li>font-family</li>
 	 * </ul>
 	 * The font's name is used as font family, a <code>sans-serif</code> default
 	 * font family is appended for the case that the given font name is not
 	 * available.
 	 * <p>
 	 * If the listed font attributes are not contained in the passed style list,
 	 * nothing happens.
 	 * </p>
 	 * 
 	 * @param styles
 	 *            CSS style definitions
 	 * @param fontData
 	 *            the font information to use
 	 * @return the modified style definitions
 	 * @since 3.3
 	 */
 	public static String convertTopLevelFont(String styles, FontData fontData) {
 		boolean bold = (fontData.getStyle() & SWT.BOLD) != 0;
 		boolean italic = (fontData.getStyle() & SWT.ITALIC) != 0;
 
 		// See: https://bugs.eclipse.org/bugs/show_bug.cgi?id=155993
 		String size = Integer.toString(fontData.getHeight())
 				+ ("carbon".equals(SWT.getPlatform()) ? "px" : "pt"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 
 		String family = "'" + fontData.getName() + "',sans-serif"; //$NON-NLS-1$ //$NON-NLS-2$
 		styles = styles
 				.replaceFirst(
 						"(html\\s*\\{.*(?:\\s|;)font-size:\\s*)\\d+pt(\\;?.*\\})", "$1" + size + "$2"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		styles = styles
 				.replaceFirst(
 						"(html\\s*\\{.*(?:\\s|;)font-weight:\\s*)\\w+(\\;?.*\\})", "$1" + (bold ? "bold" : "normal") + "$2"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
 		styles = styles
 				.replaceFirst(
 						"(html\\s*\\{.*(?:\\s|;)font-style:\\s*)\\w+(\\;?.*\\})", "$1" + (italic ? "italic" : "normal") + "$2"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
 		styles = styles
 				.replaceFirst(
 						"(html\\s*\\{.*(?:\\s|;)font-family:\\s*).+?(;.*\\})", "$1" + family + "$2"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		return styles;
 	}
 }
