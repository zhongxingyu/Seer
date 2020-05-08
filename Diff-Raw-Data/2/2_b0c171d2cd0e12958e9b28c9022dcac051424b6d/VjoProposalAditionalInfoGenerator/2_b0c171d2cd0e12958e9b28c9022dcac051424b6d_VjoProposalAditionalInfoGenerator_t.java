 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.eclipse.internal.ui.text.completion;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.dltk.mod.ui.DLTKUIPlugin;
 import org.eclipse.dltk.mod.ui.PreferenceConstants;
 import org.eclipse.dltk.mod.ui.text.completion.HTMLPrinter;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.vjet.dsf.jst.BaseJstNode;
 import org.eclipse.vjet.dsf.jst.IJstAnnotation;
 import org.eclipse.vjet.dsf.jst.IJstMethod;
 import org.eclipse.vjet.dsf.jst.IJstNode;
 import org.eclipse.vjet.dsf.jst.IJstProperty;
 import org.eclipse.vjet.dsf.jst.IJstType;
 import org.eclipse.vjet.dsf.jst.declaration.JstAnnotation;
 import org.eclipse.vjet.dsf.jst.declaration.JstArg;
 import org.eclipse.vjet.dsf.jst.declaration.JstModifiers;
 import org.eclipse.vjet.dsf.jst.expr.AssignExpr;
 import org.eclipse.vjet.dsf.jst.expr.JstArrayInitializer;
 import org.eclipse.vjet.dsf.jst.term.JstIdentifier;
 import org.eclipse.vjet.dsf.jst.token.IExpr;
 import org.eclipse.vjet.dsf.jst.util.JstCommentHelper;
 import org.eclipse.vjet.dsf.jstojava.translator.JsDocHelper;
 import org.eclipse.vjet.eclipse.internal.ui.scriptdoc.JavaDoc2HTMLTextReader;
 import org.eclipse.vjet.eclipse.ui.VjetUIPlugin;
 import org.osgi.framework.Bundle;
 
 /**
  * Generate additial info for vjo cc proposal
  * 
  * 
  * 
  */
 public class VjoProposalAditionalInfoGenerator {
 
 	private static final String PERS_FILENAME = "additionalCompletionTemplate.html"; //$NON-NLS-1$
 	private static String PERS_FOLDER = "ccAdditionalPanel";
 
 	public static String getAdditionalPropesalInfo(IJstNode node) {
 		String info = null;
 		String css = getCSSStyles();
 
 		List<String> allSupportedExplorers = new ArrayList<String>();
 		IJstProperty property = null;
 		IJstMethod method = null;
 		if (node instanceof IJstProperty) {
 			property = (IJstProperty) node;
 		} else if (node instanceof IJstMethod) {
 			method = (IJstMethod) node;
 		}
 		String briefInfo = "";
		if (property != null && property.getCommentLocations() != null && !node.getCommentLocations().isEmpty()) {
 			List<String> strings = JstCommentHelper.getCommentsAsString(node.getOwnerType(), node.getCommentLocations());
 			StringBuilder sb = new StringBuilder();
 			for(String str:strings){
 				if(str!=null)
 					sb.append(JsDocHelper.cleanJsDocComment(str));
 					sb.append("<br>");
 			}
 			info = sb.toString();
 			
 			briefInfo = getElementBriefDesc(property);
 			fillAllSupportedExplorer(property, allSupportedExplorers);
 		} else {
 			if ((method != null) && (method.getDoc() != null)) {
 				info = method.getDoc().getComment();
 				briefInfo = getElementBriefDesc(method);
 				fillAllSupportedExplorer(method, allSupportedExplorers);
 			}
 		}
 
 		if (info != null) {
 
 			try {
 				File persFile = getTemplateFile();
 				if (!persFile.exists()) {
 					try {
 						persFile.createNewFile();
 					} catch (IOException e) {
 					}
 					copyHtmlTemplateFromBundleToTemp();
 					copyIconsFromBundleToTemp();
 				}
 
 				// Replace the invalid character.
 				info = info.replace("/", "");
 				info = info.replace("*", "");
 
 				if (persFile != null) {
 					FileReader readerSuc = new FileReader(persFile);
 					JavaDoc2HTMLTextReader reader2 = new JavaDoc2HTMLTextReader(
 							readerSuc);
 					String wholeInfo = getString(reader2);
 
 					wholeInfo = wholeInfo.replace("<%=browserSupported%>",
 							getSupportedTypesStirng(allSupportedExplorers,
 									"BrowserType."));
 
 					String domLevel = getSupportedTypesStirng(
 							allSupportedExplorers, "DomLevel.");
 					if (domLevel.length() > 0) {
 						wholeInfo = wholeInfo.replace("<%=DOMLevel%>",
 								"<b>DOM level</b><br>"
 										+ getSupportedTypesStirng(
 												allSupportedExplorers,
 												"DomLevel.") + "<br>");
 					}else{
 						wholeInfo = wholeInfo.replace("<%=DOMLevel%>",
 								"");
 					}
 					wholeInfo = wholeInfo.replace("<%=MinJSVersion%>",
 							translateCharacterToNumber(getSupportedTypesStirng(
 									allSupportedExplorers, "JsVersion.")));
 					wholeInfo = wholeInfo.replace("<%=briefInfo%>", briefInfo);
 					wholeInfo = wholeInfo.replace("<%=images%>",
 							getIcon(allSupportedExplorers));
 					wholeInfo = wholeInfo.replace("<%=description%>", info);
 
 					// Add the css and HTML tags at the beginning and
 					// end.
 					StringBuffer buffer = new StringBuffer();
 					HTMLPrinter.insertPageProlog(buffer, 0, css);
 					buffer.append(wholeInfo);
 					HTMLPrinter.addPageEpilog(buffer);
 					wholeInfo = buffer.toString();
 
 					return wholeInfo;
 				}
 
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return info;
 	}
 
 	private static String fgCSSStyles;
 
 	private static String getCSSStyles() {
 		if (fgCSSStyles == null) {
 			Bundle bundle = Platform.getBundle(VjetUIPlugin.PLUGIN_ID);
 			URL url = bundle.getEntry("/DocumentationHoverStyleSheet.css"); //$NON-NLS-1$
 			if (url != null) {
 				try {
 					url = FileLocator.toFileURL(url);
 					BufferedReader reader = new BufferedReader(
 							new InputStreamReader(url.openStream()));
 					StringBuffer buffer = new StringBuffer(200);
 					String line = reader.readLine();
 					while (line != null) {
 						buffer.append(line);
 						buffer.append('\n');
 						line = reader.readLine();
 					}
 					fgCSSStyles = buffer.toString();
 				} catch (IOException ex) {
 					DLTKUIPlugin.log(ex);
 				}
 			}
 		}
 		String css = fgCSSStyles;
 		if (css != null) {
 			FontData fontData = JFaceResources.getFontRegistry().getFontData(
 					PreferenceConstants.APPEARANCE_DOCUMENTATION_FONT)[0];
 			css = HTMLPrinter.convertTopLevelFont(css, fontData);
 		}
 		return css;
 	}
 
 	/**
 	 * Gets the reader content as a String
 	 */
 	private static String getString(Reader reader) {
 		StringBuffer buf = new StringBuffer();
 		char[] buffer = new char[1024];
 		int count;
 		try {
 			while ((count = reader.read(buffer)) != -1)
 				buf.append(buffer, 0, count);
 		} catch (IOException e) {
 			return null;
 		}
 		return buf.toString();
 	}
 
 	public static String getElementBriefDesc(IJstProperty property) {
 		StringBuffer buffer = new StringBuffer();
 		buffer.append(property.getOwnerType().getName() + "\n");
 		// buffer.append("<dl><dt>");
 		buffer.append(getModifierListStr(property.getModifiers()) + " <b>");
 		buffer.append(property.getName() + "</b> ");
 		// buffer.append("</dt><dd></dd></dl>");
 
 		return buffer.toString();
 	}
 
 	public static String getElementBriefDesc(IJstMethod method) {
 		StringBuffer buffer = new StringBuffer();
 		// buffer.append("<dl><dt>");
 		String rtnTypeName = "";
 		IJstType rtnType = method.getRtnType();
 		if(rtnType!=null){
 			rtnTypeName = rtnType.getName();
 		}
 		buffer.append( " <b>" + rtnTypeName + "</b> ");
 		buffer.append(getModifierListStr(method.getModifiers()) + " "
 				+ method.getName());
 		// buffer.append("</dt></dl>");
 		buffer.append("(" + combineParameters(method) + ")");
 		return buffer.toString();
 	}
 
 	/**
 	 * @param method
 	 * @return
 	 */
 	private static String combineParameters(IJstMethod method) {
 		String par = "";
 		if (method != null) {
 			List<JstArg> args = method.getArgs();
 			String comma = "";
 			int i = 0;
 			for (JstArg arg : args) {
 				if (i != 0) {
 					comma = ", ";
 				}
 				par += comma + arg.getType().getSimpleName() + " "
 						+ arg.getName();
 				i++;
 			}
 		}
 		return par;
 	}
 
 	public static String getModifierListStr(JstModifiers jstModifiers) {
 		List<BaseJstNode> list = jstModifiers.getChildren();
 		if (list == null || list.isEmpty()) {
 			return "";
 		}
 		StringBuffer buffer = new StringBuffer();
 		Iterator<BaseJstNode> it = list.iterator();
 		while (it.hasNext()) {
 			BaseJstNode node = it.next();
 			buffer.append("");
 		}
 		return buffer.toString();
 	}
 
 	private static String getSupportedTypesStirng(List<String> supportedTypes,
 			String type) {
 		String imageLine = "";
 
 		String comma = "";
 		int i = 0;
 
 		for (Object oneType : supportedTypes) {
 			if (((String) oneType).startsWith(type)) {
 				if (i != 0) {
 					comma = ", ";
 				}
 				imageLine += comma
 						+ ((String) oneType).substring(type.length());
 				i++;
 			}
 
 		}
 		boolean noBrowserInfo = noBrowserInfo(supportedTypes);
 		if (noBrowserInfo && "BrowserType.".equals(type)) {
 			//imageLine = "FIREFOX,IE,OPERA,SAFARI,CHROME";
 		}
 
 		return imageLine;
 	}
 
 	/**
 	 * @param supportedTypes
 	 * @return
 	 */
 	private static String getIcon(List<String> supportedTypes) {
 		String imageLine = "";
 
 		String path = getDir().getPath();
 
     	boolean noBrowserInfo = false;
 
 		if (getDescOfSupport(supportedTypes, "FIREFOX").trim().length() > 0
 				|| noBrowserInfo) {
 			imageLine += "<img aligh=\"right\" src=\""
 					+ path
 					+ "/firefox.gif\" height=\"25\" width=\"25\" border=\"0\" title=\""
 					+ getDescOfSupport(supportedTypes, "MOZILLA") + "\">";
 		}
 		if (getDescOfSupport(supportedTypes, "IE").trim().length() > 0
 				|| noBrowserInfo) {
 			imageLine += "<img aligh=\"right\" src=\""
 					+ path
 					+ "/ie.gif\" height=\"25\" width=\"25\" border=\"0\" title=\""
 					+ getDescOfSupport(supportedTypes, "IE") + "\">";
 		}
 		if (getDescOfSupport(supportedTypes, "OPERA").trim().length() > 0
 				|| noBrowserInfo) {
 			imageLine += "<img aligh=\"right\" src=\""
 					+ path
 					+ "/opera.gif\" height=\"25\" width=\"25\" border=\"0\" title=\""
 					+ getDescOfSupport(supportedTypes, "OPERA") + "\">";
 		}
 		if (getDescOfSupport(supportedTypes, "SAFARI").trim().length() > 0
 				|| noBrowserInfo) {
 			imageLine += "<img aligh=\"right\" src=\""
 					+ path
 					+ "/safari.gif\" height=\"25\" width=\"25\" border=\"0\" title=\""
 					+ getDescOfSupport(supportedTypes, "SAFARI") + "\">";
 		}
 		if (getDescOfSupport(supportedTypes, "CHROME").trim().length() > 0
 				|| noBrowserInfo) {
 			imageLine += "<img aligh=\"right\" src=\""
 					+ path
 					+ "/chrome.gif\" height=\"25\" width=\"25\" border=\"0\" title=\""
 					+ getDescOfSupport(supportedTypes, "CHROME") + "\">";
 		}
 
 		return imageLine;
 	}
 
 	/**
 	 * @param supportedTypes
 	 * @return
 	 */
 	private static boolean noBrowserInfo(List supportedTypes) {
 		boolean noBrowserInfo = true;
 		for (Object type : supportedTypes) {
 			if (((String) type).contains("BrowserType.")) {
 				noBrowserInfo = false;
 				break;
 			}
 		}
 		return noBrowserInfo;
 	}
 
 	/**
 	 * @param jstElement
 	 * @return
 	 */
 	private static List<String> fillAllSupportedExplorer(Object jstElement,
 			List<String> allSupportedExplorers) {
 
 		if (jstElement instanceof IJstProperty) {
 			List<IJstAnnotation> annotations = ((IJstProperty) jstElement).getAnnotations();
 
 			getExplorerInfoFromAnnotation(allSupportedExplorers, annotations);
 
 			if (annotations.size() == 0) {
 				fillAllSupportedExplorer(((IJstProperty) jstElement)
 						.getOwnerType(), allSupportedExplorers);
 			}
 		} else if (jstElement instanceof IJstMethod) {
 			List<IJstAnnotation> annotations = ((IJstMethod) jstElement).getAnnotations();
 
 			getExplorerInfoFromAnnotation(allSupportedExplorers, annotations);
 
 			if (annotations.size() == 0) {
 				fillAllSupportedExplorer(((IJstMethod) jstElement)
 						.getOwnerType(), allSupportedExplorers);
 			}
 		} else if (jstElement instanceof IJstType) {
 			List<IJstAnnotation> annotations = ((IJstType) jstElement).getAnnotations();
 			getExplorerInfoFromAnnotation(allSupportedExplorers, annotations);
 		}
 		return allSupportedExplorers;
 	}
 	
 	public static boolean isBrowserNoneNode(IJstNode node) { 
 		if (!(node instanceof IJstMethod || node instanceof IJstProperty)) {
 			return false;
 		}
 		List<IJstAnnotation> annotations = node.getAnnotations();
 		if (annotations.size() != 1) {
 			return false;
 		}
 		List<String> allSupportedExplorers = new ArrayList<String>();
 		getExplorerInfoFromAnnotation(allSupportedExplorers, annotations);
 		if (allSupportedExplorers.size() != 0 && "BrowserType.NONE".equals(allSupportedExplorers.get(0))) {
 			return true;
 		} else {
 			return false;
 		}
 		
 	}
 
 	/**
 	 * @param allSupportedExplorers
 	 * @param annotations
 	 */
 	 static void getExplorerInfoFromAnnotation(
 			List<String> allSupportedExplorers, List<IJstAnnotation> annotations) {
 		for (Object annotation : annotations) {
 			if (annotation instanceof JstAnnotation
 					&& ("JsSupport".equals(((JstAnnotation) annotation)
 							.getName().getName())) || 
 							"BrowserSupport".equals(((JstAnnotation) annotation)
 									.getName().getName()) ||
 									"DOMSupport".equals(((JstAnnotation) annotation)
 											.getName().getName())) {
 				for (IExpr expr : ((JstAnnotation) annotation).values()) {
 					// array initializier
 					
 					if(expr instanceof JstIdentifier){
 						String typeName = expr.toExprText();
 						if (!allSupportedExplorers
 								.contains(typeName)) {
 							allSupportedExplorers.add(typeName);
 						}
 					}
 					
 					if(expr instanceof JstArrayInitializer){
 						JstArrayInitializer init = (JstArrayInitializer)expr;
 						String typeString = init.toExprText();
 						if (typeString.length() > 1) {
 							if (typeString.startsWith("[")
 									&& typeString.endsWith("]")) {
 								typeString = typeString.substring(1,
 										typeString.length() - 1);
 							}
 							String[] type = typeString.split(",");
 							for (String typeName : type) {
 								if (!allSupportedExplorers
 										.contains(typeName)) {
 									allSupportedExplorers.add(typeName);
 								}
 							}
 						}
 					}
 					if (expr instanceof AssignExpr) {
 						List<? extends IJstNode> supportTypes = ((AssignExpr) expr).getExpr()
 								.getChildren();
 						for (Object supportType : supportTypes) {
 							if (supportType instanceof JstIdentifier) {
 								String typeString = ((JstIdentifier) supportType)
 										.getParentNode().toString();
 								if (typeString.length() > 1) {
 									if (typeString.startsWith("[")
 											&& typeString.endsWith("]")) {
 										typeString = typeString.substring(1,
 												typeString.length() - 1);
 									}
 									String[] type = typeString.split(",");
 									for (String typeName : type) {
 										if (!allSupportedExplorers
 												.contains(typeName)) {
 											allSupportedExplorers.add(typeName);
 										}
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Copy the html template to temporary folder.
 	 */
 	private static void copyHtmlTemplateFromBundleToTemp() {
 		String[] htmlFiles = new String[] { PERS_FILENAME };
 		for (String file : htmlFiles) {
 			copyFileFromBundleToTemp("templates", file);
 		}
 	}
 
 	/**
 	 * Copy current icons to temporary folder.
 	 */
 	private static void copyIconsFromBundleToTemp() {
 		String[] icons = new String[] { "chrome.gif", "firefox.gif", "ie.gif",
 				"opera.gif", "safari.gif" };
 		for (String icon : icons) {
 			copyFileFromBundleToTemp("icons", icon);
 		}
 	}
 
 	private static File getDir() {
 		File dir = VjetUIPlugin.getDefault().getStateLocation().toFile();
 		dir = new File(dir, PERS_FOLDER);
 		if (!dir.exists()) {
 			dir.mkdirs();
 		}
 		return dir;
 	}
 
 	private static File getTemplateFile() {
 		File dir = getDir();
 		File persFile = new File(dir, PERS_FILENAME);
 		return persFile;
 	}
 
 	/**
 	 * Copy specified icon to temporary folder.
 	 * 
 	 * @param folderName
 	 *            TODO
 	 */
 	private static void copyFileFromBundleToTemp(String folderName,
 			String fileName) {
 		try {
 			File persFile = new File(getDir(), fileName);
 			URL url = VjetUIPlugin.getDefault().getBundle().getEntry(
 					folderName + "/" + fileName);
 			InputStream stream = url.openStream();
 			byte[] bs = new byte[stream.available()];
 			stream.read(bs);
 
 			FileOutputStream fop = new FileOutputStream(persFile);
 
 			if (persFile.exists()) {
 				fop.write(bs);
 				fop.flush();
 				fop.close();
 			}
 
 		} catch (IOException e) {
 		}
 	}
 
 	/**
 	 * @param supportedTypes
 	 * @param type
 	 * @return
 	 */
 	private static String getDescOfSupport(List<String> supportedTypes,
 			String type) {
 		for (String everyType : supportedTypes) {
 			if (everyType.contains(type)) {
 				return everyType;
 			}
 		}
 		return "";
 	}
 
 	/**
 	 * @param desc
 	 * @return
 	 */
 	private static String translateCharacterToNumber(String desc) {
 		if (desc != null) {
 			String[] numbers = { "_ZERO", "_ONE", "_TWO", "_THREE", "_FOUR",
 					"_FIVE", "_SIX", "_SEVEN", "_EIGHT", "_NINE" };
 			for (int i = 0; i < numbers.length; i++) {
 				if (desc.contains(numbers[i])) {
 					desc = desc.replace(numbers[i], String.valueOf(i));
 				}
 			}
 			if (desc.contains("_DOT")) {
 				desc = desc.replace("_DOT", ".");
 			}
 		}
 		return desc;
 	}
 
 	/**
 	 * @param baseUrl
 	 * @param create
 	 */
 	private static void generateTempFolder(URL baseUrl, boolean create) {
 		if (baseUrl == null) {
 			return;
 		}
 
 		try {
 			// make sure the directory exists
 			URL url = new URL(baseUrl, PERS_FOLDER);
 			File dir = new File(url.getFile());
 			if (!dir.exists() && create) {
 				dir.mkdir();
 			}
 
 		} catch (IOException e) {
 		}
 	}
 
 }
