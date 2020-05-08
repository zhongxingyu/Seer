 /*******************************************************************************
  * Copyright (c) 2011 Formal Mind GmbH and University of Dusseldorf.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Lukas Ladenberger - initial API and implementation
  ******************************************************************************/
 package org.eclipse.rmf.pror.reqif10.editor.util;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.rmf.pror.reqif10.configuration.Column;
 import org.eclipse.rmf.pror.reqif10.configuration.ProrPresentationConfiguration;
 import org.eclipse.rmf.pror.reqif10.configuration.ProrSpecViewConfiguration;
 import org.eclipse.rmf.pror.reqif10.editor.presentation.service.PresentationEditorManager;
 import org.eclipse.rmf.pror.reqif10.editor.presentation.service.PresentationService;
 import org.eclipse.rmf.pror.reqif10.util.ConfigurationUtil;
 import org.eclipse.rmf.reqif10.AttributeValue;
 import org.eclipse.rmf.reqif10.DatatypeDefinition;
 import org.eclipse.rmf.reqif10.EnumValue;
 import org.eclipse.rmf.reqif10.SpecHierarchy;
 import org.eclipse.rmf.reqif10.SpecObject;
 import org.eclipse.rmf.reqif10.Specification;
 import org.eclipse.rmf.reqif10.util.ReqIF10Util;
 
 public class ProrEditorUtil {
 
 	private static String createHtmlHeader(Specification spec) {
 		StringBuilder sb = new StringBuilder();
 		String title = ConfigurationUtil.getSpecElementLabel(spec);
 
 		sb.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n");
 		sb.append("<html>\n");
 		sb.append("<head>\n");
 		sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n");
 		sb.append("<meta name=\"GENERATOR\" content=\"ProR (www.pror.org)\">\n");
 		sb.append("<title>" + title + "</title>\n");
 		sb.append("<style type=\"text/css\">\n");
 		sb.append("body {font-family: Arial, sans-serif;}\n");
 		sb.append("h1 {text-align: center;}\n");
 		sb.append("table, th, td { border-bottom: 1px solid #cccccc; }\n");
 		sb.append("td { padding: 2pt; }\n");
 		sb.append("table { border-collapse: collapse; }");
 		sb.append("</style>\n");
 		sb.append("</head>\n\n");
 		sb.append("<body>\n");
 		sb.append("<h1>" + title + "</h1>\n");
 
 		return sb.toString();
 	}
 	
 	private static String getDefaultValue(AttributeValue av) {
 		Object value = av == null ? null : ReqIF10Util.getTheValue(av);
 		String textValue;
 		if (value == null) {
 			textValue = "";
 		} else if (value instanceof List<?>) {
 			textValue = "";
 			for (Iterator<?> i = ((List<?>) ((List<?>) value)).iterator(); i
 					.hasNext();) {
 				EnumValue enumValue = (EnumValue) i.next();
 				textValue += enumValue.getLongName();
 				if (i.hasNext()) {
 					textValue += ", ";
 				}
 			}
 		} else {
 			textValue = value.toString();
 		}
 		return textValue;
 	}
 
 	private static void printRecursive(StringBuilder html,
 			ProrSpecViewConfiguration config, int indent,
 			EList<SpecHierarchy> children, EditingDomain domain,
 			List<PresentationService> presentations) {
 		for (SpecHierarchy child : children) {
 			if (child.getObject() != null) {
 				SpecObject specObject = child.getObject();
 				boolean first = true;
 				html.append("<tr>");
 				for (Column col : config.getColumns()) {
 					html.append("<td valign='top'>");
 
 					// Handle indenting TODO use something better than spaces.
 					if (first) {
 						html.append("<div style='margin-left: " + (indent * 20)
								+ "px;'>");
 					}
 					AttributeValue av = ReqIF10Util.getAttributeValueForLabel(
 							specObject, col.getLabel());
 					DatatypeDefinition dd = ReqIF10Util
 							.getDatatypeDefinition(av);
 					ProrPresentationConfiguration configuration = ConfigurationUtil
 							.getConfiguration(dd, domain);
 
 					if (configuration != null) {
 						
 						PresentationService service = null;
 
 						if (presentations != null) {
 							for (PresentationService serv : presentations) {
 								if (serv.getConfigurationInterface()
 										.isInstance(configuration))
 									service = serv;
 							}
 						} else {
 							service = PresentationEditorManager
 									.getPresentationService(configuration);
 						}
 
 						if (service != null)
 							html.append(service.getCellRenderer(av)
 									.doDrawHtmlContent(av));
 
 					} else {
 						html.append(getDefaultValue(av));
 					}
 
 					if (first) {
 						first = false;
 						html.append("</div>");
 					}
 					html.append("</td>");
 				}
 				html.append("</tr>\n");
 			}
 			printRecursive(html, config, indent + 1, child.getChildren(),
 					domain, presentations);
 		}
 	}
 
 	public static String createHtmlContent(Specification spec,
 			EditingDomain domain) {
 		return createHtmlContent(spec, domain, null);
 	}
 
 	public static String createHtmlContent(Specification spec,
 			EditingDomain domain, List<PresentationService> presentations) {
 
 		ProrSpecViewConfiguration config = ConfigurationUtil
 				.getSpecViewConfiguration(spec, domain);
 
 		StringBuilder html = new StringBuilder();
 
 		// Draw the header
 		html.append(ProrEditorUtil.createHtmlHeader(spec));
 		html.append("<table><tr>");
 		EList<Column> cols = config.getColumns();
 		for (Column col : cols) {
 			html.append("<td><b>" + col.getLabel() + "</b></td>");
 		}
 		html.append("</tr>\n");
 		printRecursive(html, config, 0, spec.getChildren(), domain,
 				presentations);
 		html.append("</table>");
 
 		return html.toString();
 
 	}
 	
 }
